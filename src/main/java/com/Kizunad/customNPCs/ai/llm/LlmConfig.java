package com.Kizunad.customNPCs.ai.llm;

/**
 * LLM 调用配置（单例），支持开关、间隔与 OpenRouter 请求参数。
 */
public final class LlmConfig {

    private static final LlmConfig INSTANCE = new LlmConfig();

    private static final boolean DEFAULT_ENABLED = true;
    private static final int DEFAULT_INTERVAL_TICKS = 200;
    private static final int DEFAULT_PLAN_TTL_TICKS = 400;
    private static final String DEFAULT_MODEL = "openrouter/auto";
    private static final double DEFAULT_TEMPERATURE = 0.4d;
    private static final int DEFAULT_MAX_TOKENS = 512;
    private static final boolean DEFAULT_LOG_REQUEST = true;
    private static final boolean DEFAULT_LOG_RESPONSE = true;
    private static final int DEFAULT_HTTP_TIMEOUT_MS = 120_000;
    private static final int MIN_INTERVAL_TICKS = 20;
    private static final int MIN_PLAN_TTL_TICKS = 40;
    private static final int MIN_MAX_TOKENS = 64;
    private static final int MIN_HTTP_TIMEOUT_MS = 1000;

    private boolean enabled = DEFAULT_ENABLED;
    private int requestIntervalTicks = DEFAULT_INTERVAL_TICKS;
    private int planTtlTicks = DEFAULT_PLAN_TTL_TICKS;
    private String model = DEFAULT_MODEL;
    private double temperature = DEFAULT_TEMPERATURE;
    private int maxTokens = DEFAULT_MAX_TOKENS;
    private boolean logRequest = DEFAULT_LOG_REQUEST;
    private boolean logResponse = DEFAULT_LOG_RESPONSE;
    private String apiKey = "";
    private String apiReferer = "";
    private String apiTitle = "";
    private int httpTimeoutMs = DEFAULT_HTTP_TIMEOUT_MS;

    private LlmConfig() {}

    public static LlmConfig getInstance() {
        return INSTANCE;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public LlmConfig setEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    public int getRequestIntervalTicks() {
        return requestIntervalTicks;
    }

    public LlmConfig setRequestIntervalTicks(int requestIntervalTicks) {
        this.requestIntervalTicks =
            Math.max(MIN_INTERVAL_TICKS, requestIntervalTicks);
        return this;
    }

    public int getPlanTtlTicks() {
        return planTtlTicks;
    }

    public LlmConfig setPlanTtlTicks(int planTtlTicks) {
        this.planTtlTicks = Math.max(MIN_PLAN_TTL_TICKS, planTtlTicks);
        return this;
    }

    public String getModel() {
        return model;
    }

    public LlmConfig setModel(String model) {
        this.model = model;
        return this;
    }

    public double getTemperature() {
        return temperature;
    }

    public LlmConfig setTemperature(double temperature) {
        this.temperature = Math.max(0.0d, Math.min(2.0d, temperature));
        return this;
    }

    public int getMaxTokens() {
        return maxTokens;
    }

    public LlmConfig setMaxTokens(int maxTokens) {
        this.maxTokens = Math.max(MIN_MAX_TOKENS, maxTokens);
        return this;
    }

    public boolean isLogRequest() {
        return logRequest;
    }

    public LlmConfig setLogRequest(boolean logRequest) {
        this.logRequest = logRequest;
        return this;
    }

    public boolean isLogResponse() {
        return logResponse;
    }

    public LlmConfig setLogResponse(boolean logResponse) {
        this.logResponse = logResponse;
        return this;
    }

    public String getApiKey() {
        return apiKey;
    }

    public LlmConfig setApiKey(String apiKey) {
        this.apiKey = apiKey == null ? "" : apiKey;
        return this;
    }

    public boolean hasApiKey() {
        return apiKey != null && !apiKey.isBlank();
    }

    public String getApiReferer() {
        return apiReferer;
    }

    public LlmConfig setApiReferer(String apiReferer) {
        this.apiReferer = apiReferer == null ? "" : apiReferer;
        return this;
    }

    public String getApiTitle() {
        return apiTitle;
    }

    public LlmConfig setApiTitle(String apiTitle) {
        this.apiTitle = apiTitle == null ? "" : apiTitle;
        return this;
    }

    public int getHttpTimeoutMs() {
        return httpTimeoutMs;
    }

    public LlmConfig setHttpTimeoutMs(int httpTimeoutMs) {
        this.httpTimeoutMs = Math.max(MIN_HTTP_TIMEOUT_MS, httpTimeoutMs);
        return this;
    }
}
