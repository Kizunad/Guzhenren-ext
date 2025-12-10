package com.Kizunad.guzhenrenext.guzhenrenBridge.generated.itemUse;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;

/**
 * 通过反射自动匹配蛊真人物品对应的 Procedure，避免每次模组更新都手动维护。
 */
public class GuzhenrenItemDispatcher {

    private static final Logger LOGGER = LoggerFactory.getLogger(
        GuzhenrenItemDispatcher.class
    );
    private static final String GUZHENREN_NAMESPACE = "guzhenren";
    private static final String PROCEDURE_PACKAGE = "net.guzhenren.procedures";
    private static final List<String> PROCEDURE_SUFFIXES = List.of(
        "DangYouJiKongQiShiShiTiDeWeiZhi",
        "YouJiKongQiShiShiTiDeWeiZhi",
        "WuPinZaiWuPinLanShiMeiKeFaSheng",
        "DangWuPinZaiBeiBaoZhongShiMeiKeFaSheng",
        "liucheng",
        "1XiaoGuoChiXuShiMeiKeFaSheng"
    );
    private static final Map<Item, ProcedureInvoker> ITEM_INVOKERS = buildInvokerMap();

    private GuzhenrenItemDispatcher() {
    }

    public static boolean dispatch(LivingEntity npc, ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        ProcedureInvoker invoker = ITEM_INVOKERS.get(stack.getItem());
        if (invoker == null) {
            return false;
        }
        return invoker.invoke(npc, stack);
    }

    private static Map<Item, ProcedureInvoker> buildInvokerMap() {
        Map<Item, ProcedureInvoker> invokers = new IdentityHashMap<>();
        for (Item item : BuiltInRegistries.ITEM) {
            var id = BuiltInRegistries.ITEM.getKey(item);
            if (id == null || !GUZHENREN_NAMESPACE.equals(id.getNamespace())) {
                continue;
            }
            ProcedureInvoker invoker = resolveProcedureForItem(item);
            if (invoker != null) {
                invokers.put(item, invoker);
            } else {
                LOGGER.debug("未能为蛊真人物品 {} 构建 Procedure 映射。", id);
            }
        }
        LOGGER.info("GuzhenrenItemDispatcher 已缓存 {} 个蛊真人 Procedure 映射。", invokers.size());
        return Collections.unmodifiableMap(invokers);
    }

    private static ProcedureInvoker resolveProcedureForItem(Item item) {
        String simpleName = item.getClass().getSimpleName();
        if (!simpleName.endsWith("Item")) {
            return null;
        }
        String baseName = simpleName.substring(0, simpleName.length() - 4);
        for (String suffix : PROCEDURE_SUFFIXES) {
            String className = PROCEDURE_PACKAGE + "." + baseName + suffix + "Procedure";
            try {
                Class<?> procedureClass = Class.forName(className);
                Method execute = Arrays
                    .stream(procedureClass.getDeclaredMethods())
                    .filter(method -> method.getName().equals("execute"))
                    .findFirst()
                    .orElse(null);
                if (execute == null) {
                    continue;
                }
                execute.setAccessible(true);
                if (!supportsSignature(execute.getParameterTypes())) {
                    LOGGER.warn("Procedure {} 的参数签名未被支持，跳过。", className);
                    continue;
                }
                return new ReflectiveInvoker(execute);
            } catch (ClassNotFoundException ignored) {
                // 忽略未找到的 Procedure，继续尝试下一个后缀
            } catch (RuntimeException | LinkageError exception) {
                LOGGER.error("构建 Procedure {} 失败。", className, exception);
            }
        }
        return null;
    }

    private static boolean supportsSignature(Class<?>[] parameterTypes) {
        for (Class<?> type : parameterTypes) {
            if (LevelAccessor.class.isAssignableFrom(type)) {
                continue;
            }
            if (type == double.class) {
                continue;
            }
            if (ItemStack.class.isAssignableFrom(type)) {
                continue;
            }
            if (net.minecraft.world.entity.Entity.class.isAssignableFrom(type)) {
                continue;
            }
            return false;
        }
        return true;
    }

    private interface ProcedureInvoker {

        boolean invoke(LivingEntity npc, ItemStack stack);
    }

    private static final class ReflectiveInvoker implements ProcedureInvoker {

        private final Method method;
        private final Class<?>[] parameterTypes;

        private ReflectiveInvoker(Method method) {
            this.method = method;
            this.parameterTypes = method.getParameterTypes();
        }

        @Override
        public boolean invoke(LivingEntity npc, ItemStack stack) {
            Object[] arguments = buildArguments(parameterTypes, npc, stack);
            if (arguments == null) {
                LOGGER.error(
                    "Procedure {} 参数解析失败，已跳过。",
                    method.getDeclaringClass().getName()
                );
                return false;
            }
            try {
                method.invoke(null, arguments);
                return true;
            } catch (IllegalAccessException | InvocationTargetException exception) {
                LOGGER.error(
                    "调用 Procedure {} 时发生异常。",
                    method.getDeclaringClass().getName(),
                    exception
                );
                return false;
            }
        }

        private Object[] buildArguments(
            Class<?>[] parameterTypes,
            LivingEntity npc,
            ItemStack stack
        ) {
            Object[] args = new Object[parameterTypes.length];
            double[] coords = new double[] {npc.getX(), npc.getY(), npc.getZ()};
            int coordIndex = 0;
            LevelAccessor level = npc.level();
            for (int i = 0; i < parameterTypes.length; i++) {
                Class<?> type = parameterTypes[i];
                if (LevelAccessor.class.isAssignableFrom(type)) {
                    args[i] = level;
                } else if (type == double.class) {
                    if (coordIndex >= coords.length) {
                        return null;
                    }
                    args[i] = coords[coordIndex++];
                } else if (net.minecraft.world.entity.Entity.class.isAssignableFrom(type)) {
                    args[i] = npc;
                } else if (ItemStack.class.isAssignableFrom(type)) {
                    args[i] = stack;
                } else {
                    return null;
                }
            }
            return args;
        }
    }
}
