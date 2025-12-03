package com.Kizunad.customNPCs.ai.llm;

import com.Kizunad.customNPCs.ai.NpcMindRegistry;
import com.Kizunad.customNPCs.ai.actions.IAction;
import com.Kizunad.customNPCs.ai.logging.MindLog;
import com.Kizunad.customNPCs.ai.logging.MindLogLevel;
import com.Kizunad.customNPCs.capabilities.mind.NpcMind;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * LLM 计划选择协调器：按固定间隔异步生成 OpenRouter 风格请求与候选计划 JSON，
 * 仅提供“选择”建议，不直接执行。
 */
public class LlmPlanner {

    private static final Logger LOGGER = LoggerFactory.getLogger("LLMPlanner");
    private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();
    private static final String OPENROUTER_ENDPOINT =
        "https://openrouter.ai/api/v1/chat/completions";
    private static final int HTTP_STATUS_SUCCESS_MIN = 200;
    private static final int HTTP_STATUS_SUCCESS_MAX = 299;
    private static final String MEMORY_PLAN_JSON = "llm_plan_options";
    private static final String MEMORY_PLAN_SELECTED = "llm_plan_selected";
    private static final String MEMORY_REQUEST_JSON = "llm_plan_request";
    private static final String MEMORY_PLAN_RECENT = "llm_recent_plan";
    private static final String MEMORY_PLAN_ACTIVE = "llm_plan_active";
    private static final String MEMORY_PLAN_QUEUE_SIZE = "llm_plan_queue_size";
    private static final int REQUEST_MIN_INTERVAL = 20;
    private static final double NEARBY_SCAN_RADIUS = 16.0D;
    private static final int NEARBY_SCAN_LIMIT = 8;
    private static final int SUMMARY_ACTION_LIMIT = 3;
    private static final int SUMMARY_NOTES_MAX = 80;
    private static final int LONG_TERM_SUMMARY_LIMIT = 3;
    private static final int PLAN_QUEUE_LIMIT = 10;
    private static final int QUEUE_INFO_TTL = 40;
    private static final int INVENTORY_SNAPSHOT_LIMIT = 3 * 9; // LLM 上下文中背包摘要的截断上限 这里不做限制了

    private final LlmConfig config = LlmConfig.getInstance();
    private long lastRequestTick = 0;
    private CompletableFuture<LlmPlanResult> pending = null;
    private String lastPlanId = "";
    private String lastPlanJson = "";
    private final Deque<LlmQueuedPlan> planQueue = new ArrayDeque<>();

    public void tick(ServerLevel level, LivingEntity entity, NpcMind mind) {
        if (!config.isEnabled() || !config.hasApiKey()) {
            return;
        }
        long gameTime = level.getGameTime();

        dispatchIfIdle(mind, entity);

        // 处理已完成的任务
        if (pending != null && pending.isDone()) {
            try {
                LlmPlanResult result = pending.join();
                if (result != null) {
                    lastPlanId = result.planId();
                    lastPlanJson = result.planJson();
                    mind
                        .getMemory()
                        .rememberShortTerm(
                            MEMORY_PLAN_JSON,
                            lastPlanJson,
                            config.getPlanTtlTicks()
                        );
                    mind
                        .getMemory()
                        .rememberShortTerm(
                            MEMORY_PLAN_RECENT,
                            buildPlanSummary(lastPlanJson, lastPlanId),
                            config.getPlanTtlTicks()
                        );
                    mind
                        .getMemory()
                        .rememberShortTerm(
                            MEMORY_PLAN_SELECTED,
                            lastPlanId,
                            config.getPlanTtlTicks()
                        );
                    mind
                        .getMemory()
                        .rememberShortTerm(
                            MEMORY_REQUEST_JSON,
                            result.requestJson(),
                            config.getPlanTtlTicks()
                        );
                    rebuildQueueFromPlan(lastPlanJson, mind);
                    dispatchIfIdle(mind, entity);
                    logIfNeeded(
                        mind,
                        "[LLM] 收到计划建议: plan=%s".formatted(lastPlanId)
                    );
                }
            } catch (CompletionException ex) {
                logWarn(mind, "LLM 请求失败: %s".formatted(ex.getMessage()));
            }
            pending = null;
        }

        if (pending != null) {
            return;
        }

        if (
            gameTime - lastRequestTick <
            Math.max(REQUEST_MIN_INTERVAL, config.getRequestIntervalTicks())
        ) {
            return;
        }

        lastRequestTick = gameTime;
        String context = buildContextSnapshot(level, entity, mind);
        String requestJson = buildOpenRouterRequest(context);
        pending = sendRequestAsync(requestJson, mind);
        logIfNeeded(mind, "[LLM] 异步提交计划生成请求");
    }

    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        tag.putLong("lastRequestTick", lastRequestTick);
        if (!lastPlanId.isEmpty()) {
            tag.putString("lastPlanId", lastPlanId);
        }
        if (!lastPlanJson.isEmpty()) {
            tag.putString("lastPlanJson", lastPlanJson);
        }
        return tag;
    }

    public void deserializeNBT(
        HolderLookup.Provider provider,
        CompoundTag tag
    ) {
        lastRequestTick = tag.getLong("lastRequestTick");
        if (tag.contains("lastPlanId")) {
            lastPlanId = tag.getString("lastPlanId");
        }
        if (tag.contains("lastPlanJson")) {
            lastPlanJson = tag.getString("lastPlanJson");
        }
    }

    private void logIfNeeded(NpcMind mind, String message) {
        if (config.isLogResponse()) {
            LOGGER.info(message);
        }
    }

    private void logWarn(NpcMind mind, String message) {
        LOGGER.warn(message);
        MindLog.planning(MindLogLevel.WARN, message);
    }

    /**
     * 构造 OpenRouter 请求 JSON（仅字符串，便于外部直接调用真实接口）。
     */
    private String buildOpenRouterRequest(String contextSnapshot) {
        JsonObject root = new JsonObject();
        root.addProperty("model", config.getModel());
        root.addProperty(
            "temperature",
            Double.parseDouble(
                String.format(Locale.ROOT, "%.2f", config.getTemperature())
            )
        );
        root.addProperty("max_tokens", config.getMaxTokens());
        root.addProperty("stream", false);
        JsonObject responseFormat = new JsonObject();
        responseFormat.addProperty("type", "json_object");
        root.add("response_format", responseFormat);

        JsonArray messages = new JsonArray();
        JsonObject sys = new JsonObject();
        sys.addProperty("role", "system");
        sys.addProperty("content", PromptLibrary.SYSTEM_PROMPT);
        messages.add(sys);

        JsonObject user = new JsonObject();
        user.addProperty("role", "user");
        user.addProperty("content", contextSnapshot);
        messages.add(user);

        root.add("messages", messages);
        return root.toString();
    }

    /**
     * 构造上下文（环境 + 角色信息）。
     */
    private String buildContextSnapshot(
        ServerLevel level,
        LivingEntity entity,
        NpcMind mind
    ) {
        String dimension = level.dimension().location().toString();
        String position = String.format(
            Locale.ROOT,
            "(%.1f, %.1f, %.1f)",
            entity.getX(),
            entity.getY(),
            entity.getZ()
        );
        double health = entity.getHealth();
        double maxHealth = entity.getMaxHealth();
        String inventory = buildInventorySnapshot(mind);
        String equipment = buildEquipmentSnapshot(entity);
        Object threat = mind.getMemory().getMemory("threat_detected");
        Object currentTarget = mind.getMemory().getMemory("current_threat_id");
        String nearby = buildNearbyEntitiesSnapshot(level, entity);
        String recentPlan = mind
            .getMemory()
            .getShortTerm(MEMORY_PLAN_RECENT, String.class, "none");
        String longTerm = mind
            .getLongTermMemory()
            .summarize(LONG_TERM_SUMMARY_LIMIT);
        return """
        角色: CustomNPC
        维度: %s
        坐标: %s
        生命: %.1f/%.1f
        背包: %s
        装备: %s
        威胁: %s
        当前目标: %s
        周围实体: %s
        长期记忆: %s
        最近计划: %s
        上下文: 仅生成 plan/action，下一次评估直接使用选择结果
        """.formatted(
                dimension,
                position,
                health,
                maxHealth,
                inventory,
                equipment,
                threat == null ? "none" : threat,
                currentTarget == null ? "none" : currentTarget,
                nearby,
                longTerm,
                recentPlan
            );
    }

    private String buildInventorySnapshot(NpcMind mind) {
        var inventory = mind.getInventory();
        if (inventory == null || inventory.isEmpty()) {
            return "empty";
        }
        List<String> items = new ArrayList<>();
        for (int i = 0; i < inventory.size(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (!stack.isEmpty()) {
                items.add(describeItem(stack));
                if (items.size() >= INVENTORY_SNAPSHOT_LIMIT) {
                    break;
                }
            }
        }
        if (items.isEmpty()) {
            return "empty";
        }
        return String.join(", ", items);
    }

    private String buildEquipmentSnapshot(LivingEntity entity) {
        String mainHand = describeItem(entity.getMainHandItem());
        String offHand = describeItem(entity.getOffhandItem());
        String head = describeItem(entity.getItemBySlot(EquipmentSlot.HEAD));
        String chest = describeItem(entity.getItemBySlot(EquipmentSlot.CHEST));
        String legs = describeItem(entity.getItemBySlot(EquipmentSlot.LEGS));
        String feet = describeItem(entity.getItemBySlot(EquipmentSlot.FEET));
        return "主手:%s 副手:%s 头:%s 胸:%s 腿:%s 脚:%s".formatted(
            mainHand,
            offHand,
            head,
            chest,
            legs,
            feet
        );
    }

    private String describeItem(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return "empty";
        }
        String id = stack
            .getItem()
            .builtInRegistryHolder()
            .key()
            .location()
            .toString();
        return "%dx %s".formatted(stack.getCount(), id);
    }

    private String buildNearbyEntitiesSnapshot(
        ServerLevel level,
        LivingEntity self
    ) {
        List<LivingEntity> nearby = level.getEntitiesOfClass(
            LivingEntity.class,
            self.getBoundingBox().inflate(NEARBY_SCAN_RADIUS),
            other ->
                other.isAlive() &&
                other != self &&
                !shouldIgnoreEntityForContext(other)
        );
        if (nearby.isEmpty()) {
            return "none";
        }
        nearby.sort(Comparator.comparingDouble(self::distanceToSqr));

        StringBuilder sb = new StringBuilder();
        sb.append(
            "半径" + String.format(Locale.ROOT, "%.0f", NEARBY_SCAN_RADIUS)
        );
        int count = 0;
        for (LivingEntity target : nearby) {
            double distance = Math.sqrt(self.distanceToSqr(target));
            sb
                .append("\n- ")
                .append(target.getType().toShortString())
                .append(" \"")
                .append(target.getName().getString())
                .append("\"")
                .append(" dist=")
                .append(String.format(Locale.ROOT, "%.1f", distance))
                .append(" hp=")
                .append(
                    String.format(
                        Locale.ROOT,
                        "%.1f/%.1f",
                        target.getHealth(),
                        target.getMaxHealth()
                    )
                );
            count++;
            if (count >= NEARBY_SCAN_LIMIT) {
                break;
            }
        }
        if (nearby.size() > NEARBY_SCAN_LIMIT) {
            sb
                .append("\n- ... total ")
                .append(nearby.size())
                .append(" entities");
        }
        return sb.toString();
    }

    /**
     * 过滤对策规划无意义/不可交互的目标（例如旁观者、隐身）。
     */
    private boolean shouldIgnoreEntityForContext(LivingEntity entity) {
        if (entity instanceof Player player && player.isSpectator()) {
            return true;
        }
        return entity.isInvisible();
    }

    private String buildPlanSummary(String planJson, String planId) {
        try {
            JsonObject root = JsonParser.parseString(
                planJson
            ).getAsJsonObject();
            JsonArray plans = root.getAsJsonArray("plans");
            if (plans == null || plans.isEmpty()) {
                return "none";
            }
            JsonObject first = plans.get(0).getAsJsonObject();
            String id = first.has("id")
                ? first.get("id").getAsString()
                : planId;
            String title = first.has("title")
                ? first.get("title").getAsString()
                : "";
            JsonArray actions = first.getAsJsonArray("actions");
            StringBuilder actionBuilder = new StringBuilder();
            if (actions != null) {
                int limit = Math.min(SUMMARY_ACTION_LIMIT, actions.size());
                for (int i = 0; i < limit; i++) {
                    if (i > 0) {
                        actionBuilder.append(',');
                    }
                    actionBuilder.append(actions.get(i).getAsString());
                }
            }
            String notes = first.has("notes")
                ? first.get("notes").getAsString()
                : "";
            if (notes.length() > SUMMARY_NOTES_MAX) {
                notes = notes.substring(0, SUMMARY_NOTES_MAX) + "...";
            }
            return String.format(
                Locale.ROOT,
                "id=%s title=%s actions=[%s] notes=%s",
                id,
                title,
                actionBuilder,
                notes
            );
        } catch (Exception e) {
            return "none";
        }
    }

    private void rebuildQueueFromPlan(String planJson, NpcMind mind) {
        planQueue.clear();
        try {
            JsonObject root = JsonParser.parseString(
                planJson
            ).getAsJsonObject();
            JsonArray plans = root.getAsJsonArray("plans");
            if (plans == null || plans.isEmpty()) {
                mind
                    .getMemory()
                    .rememberShortTerm(
                        MEMORY_PLAN_QUEUE_SIZE,
                        0,
                        QUEUE_INFO_TTL
                    );
                return;
            }
            List<LlmQueuedPlan> collected = new ArrayList<>();
            for (JsonElement element : plans) {
                JsonObject obj = element.getAsJsonObject();
                JsonArray actions = obj.getAsJsonArray("actions");
                if (actions == null || actions.isEmpty()) {
                    continue;
                }
                List<String> names = new ArrayList<>();
                for (JsonElement act : actions) {
                    names.add(act.getAsString());
                }
                double priority = obj.has("priority")
                    ? obj.get("priority").getAsDouble()
                    : 0.0D;
                String id = obj.has("id") ? obj.get("id").getAsString() : "";
                String title = obj.has("title")
                    ? obj.get("title").getAsString()
                    : "";
                collected.add(new LlmQueuedPlan(id, title, priority, names));
            }
            collected.sort(
                Comparator.comparingDouble(LlmQueuedPlan::priority).reversed()
            );
            int added = 0;
            for (LlmQueuedPlan plan : collected) {
                planQueue.addLast(plan);
                added++;
                if (added >= PLAN_QUEUE_LIMIT) {
                    break;
                }
            }
            mind
                .getMemory()
                .rememberShortTerm(
                    MEMORY_PLAN_QUEUE_SIZE,
                    added,
                    QUEUE_INFO_TTL
                );
            logIfNeeded(
                mind,
                "[LLM] 计划队列刷新: total=%d (clip=%d)".formatted(
                    added,
                    PLAN_QUEUE_LIMIT
                )
            );
        } catch (Exception e) {
            logWarn(mind, "解析计划队列失败: " + e.getMessage());
        }
    }

    private void dispatchIfIdle(NpcMind mind, LivingEntity entity) {
        if (!mind.getActionExecutor().isIdle()) {
            return;
        }
        while (!planQueue.isEmpty()) {
            LlmQueuedPlan plan = planQueue.pollFirst();
            List<IAction> mapped = new ArrayList<>();
            for (String name : plan.actions()) {
                IAction action = NpcMindRegistry.createAction(name);
                if (action != null) {
                    mapped.add(action);
                } else {
                    logWarn(
                        mind,
                        "[LLM] 未知动作名称: %s，已跳过".formatted(name)
                    );
                }
            }
            if (mapped.isEmpty()) {
                continue;
            }
            mind.getActionExecutor().submitPlan(mapped);
            mind
                .getMemory()
                .rememberShortTerm(
                    MEMORY_PLAN_ACTIVE,
                    plan.id().isBlank() ? plan.title() : plan.id(),
                    config.getPlanTtlTicks()
                );
            MindLog.planning(
                MindLogLevel.INFO,
                "执行 LLM 计划: id=%s title=%s priority=%.2f actions=%d",
                plan.id(),
                plan.title(),
                plan.priority(),
                mapped.size()
            );
            break;
        }
    }

    private record LlmQueuedPlan(
        String id,
        String title,
        double priority,
        List<String> actions
    ) {}

    /**
     * 解析 OpenRouter 响应（骨架）：提取 choices[0].message.content 作为计划 JSON。
     * 未找到有效数据时返回 null。
     */
    private LlmPlanResult parseOpenRouterResponse(String responseJson) {
        try {
            JsonObject root = JsonParser.parseString(
                responseJson
            ).getAsJsonObject();
            JsonArray choices = root.getAsJsonArray("choices");
            if (choices == null || choices.isEmpty()) {
                return null;
            }
            JsonObject first = choices.get(0).getAsJsonObject();
            JsonObject message = first.getAsJsonObject("message");
            if (message == null) {
                JsonObject delta = first.getAsJsonObject("delta");
                message = delta; // 兼容 streaming delta
            }
            if (message == null) {
                return null;
            }
            String content = extractContent(message);
            if (content == null || content.trim().isEmpty()) {
                // 兼容 streaming delta 场景
                JsonObject delta = first.getAsJsonObject("delta");
                content = extractContent(delta);
            }
            if (content == null) {
                return null;
            }
            String normalized = normalizeContentJson(content);
            String planId = extractPlanIdFromContent(normalized);
            return new LlmPlanResult(planId, normalized, responseJson);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 提取 message/delta 的 content，兼容字符串与 content parts 数组。
     */
    private String extractContent(JsonObject message) {
        if (message == null) {
            return null;
        }
        JsonElement contentEl = message.get("content");
        if (contentEl == null || contentEl.isJsonNull()) {
            return null;
        }
        if (contentEl.isJsonArray()) {
            StringBuilder sb = new StringBuilder();
            JsonArray arr = contentEl.getAsJsonArray();
            for (JsonElement el : arr) {
                if (el.isJsonPrimitive()) {
                    sb.append(el.getAsString());
                    continue;
                }
                if (el.isJsonObject()) {
                    JsonObject obj = el.getAsJsonObject();
                    if (obj.has("text")) {
                        sb.append(obj.get("text").getAsString());
                    } else if (obj.has("content")) {
                        sb.append(obj.get("content").getAsString());
                    }
                }
            }
            return sb.toString();
        }
        if (contentEl.isJsonPrimitive()) {
            return contentEl.getAsString();
        }
        return contentEl.toString();
    }

    private String extractPlanIdFromContent(String content) {
        try {
            JsonObject obj = JsonParser.parseString(content).getAsJsonObject();
            JsonArray plans = obj.has("plans")
                ? obj.getAsJsonArray("plans")
                : obj.getAsJsonArray("candidates");
            if (plans != null && !plans.isEmpty()) {
                JsonObject first = plans.get(0).getAsJsonObject();
                if (first.has("id")) {
                    return first.get("id").getAsString();
                }
            }
        } catch (Exception ignore) {
            // 不是 JSON 时退回默认
        }
        return "parsed_plan";
    }

    /**
     * 处理两类情况：
     * 1) content 已是 JSON 对象/数组 -> 直接转字符串
     * 2) content 是被转义的 JSON 字符串 -> 再解析一次
     */
    private String normalizeContentJson(String content) {
        try {
            JsonElement first = JsonParser.parseString(content);
            if (
                first.isJsonPrimitive() && first.getAsJsonPrimitive().isString()
            ) {
                String raw = first.getAsString();
                try {
                    JsonElement nested = JsonParser.parseString(raw);
                    return nested.toString();
                } catch (Exception inner) {
                    return raw;
                }
            }
            return first.toString();
        } catch (Exception e) {
            return content;
        }
    }

    /**
     * 本地占位计划（不访问外部网络），便于前端/命令消费；同时时间窗口内为下一次选择提供 plan JSON。
     */
    private LlmPlanResult buildLocalSuggestion(
        String requestJson,
        String contextSnapshot
    ) {
        return new LlmPlanResult("defend_and_kite", "{}", requestJson);
    }

    private CompletableFuture<LlmPlanResult> sendRequestAsync(
        String requestJson,
        NpcMind mind
    ) {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
            .uri(URI.create(OPENROUTER_ENDPOINT))
            .timeout(Duration.ofMillis(config.getHttpTimeoutMs()))
            .header("Authorization", "Bearer " + config.getApiKey())
            .header("Content-Type", "application/json");

        if (!config.getApiReferer().isBlank()) {
            builder.header("HTTP-Referer", config.getApiReferer());
        }
        if (!config.getApiTitle().isBlank()) {
            builder.header("X-Title", config.getApiTitle());
        }

        HttpRequest request = builder
            .POST(
                HttpRequest.BodyPublishers.ofString(
                    requestJson,
                    StandardCharsets.UTF_8
                )
            )
            .build();

        if (config.isLogRequest()) {
            LOGGER.info("[LLM] 请求体: {}", requestJson);
        }

        return HTTP_CLIENT.sendAsync(
            request,
            HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8)
        )
            .thenApply(response ->
                handleHttpResponse(response, mind, requestJson)
            )
            .exceptionally(ex -> {
                if (ex.getCause() instanceof HttpTimeoutException) {
                    logWarn(mind, "LLM 请求超时");
                } else {
                    logWarn(mind, "LLM 请求异常: " + ex.getMessage());
                }
                return null;
            });
    }

    private LlmPlanResult handleHttpResponse(
        HttpResponse<String> response,
        NpcMind mind,
        String requestJson
    ) {
        int status = response.statusCode();
        String body = response.body();
        if (
            status < HTTP_STATUS_SUCCESS_MIN || status > HTTP_STATUS_SUCCESS_MAX
        ) {
            logWarn(
                mind,
                "LLM HTTP 状态异常: %d body=%s".formatted(
                    status,
                    body == null ? "" : body
                )
            );
            return null;
        }
        String safeBody = body == null ? "" : body;
        if (config.isLogResponse()) {
            LOGGER.info("[LLM] 响应状态: {} 内容: {}", status, safeBody);
        }
        if (safeBody.trim().isEmpty()) {
            logWarn(mind, "LLM 响应为空或仅空白，status=" + status);
            return new LlmPlanResult("llm_empty_body", safeBody, requestJson);
        }
        LlmPlanResult parsed = parseOpenRouterResponse(body);
        if (parsed == null) {
            return new LlmPlanResult("llm_none", body, requestJson);
        }
        return new LlmPlanResult(
            parsed.planId(),
            parsed.planJson(),
            requestJson
        );
    }

    private record LlmPlanResult(
        String planId,
        String planJson,
        String requestJson
    ) {}
}
