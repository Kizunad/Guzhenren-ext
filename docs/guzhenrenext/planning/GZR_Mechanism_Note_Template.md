# GZR 机制笔记执行模板（Task6）

## 使用范围
- 本模板仅用于 Task6，目标是为后续实现任务提供可校验、可追溯、可写回的机制笔记结构。
- 本模板不是业务正文，不在本任务中填写最终机制结论。

## 执行规则
1. 后续实现阶段必须按本模板填充已验证内容，不得填入未经源码证据支持的 claim。
2. 每个 claim 必须带 `confidence`，且只能是 `HIGH`、`MED`、`LOW`。
3. 每个 claim 必须带源码引用字段 `source_path` 与 `source_line_range`。
4. 完成验证后，必须把最终版本写回 `GZR_INFO.md`。

## 字段合同
- mechanism_name
- claim
- confidence（HIGH/MED/LOW）
- source_path
- source_line_range
- variable_lifecycle
- breakthrough_factor
- cap_clamp_rule
- initialization_flow
- validation_evidence
- writeback_target（固定为 `GZR_INFO.md`）

## 模板（逐条填写）

```yaml
template_version: "task6-gzr-note-v1"
writeback_target: "GZR_INFO.md"
confidence_enum:
  - HIGH
  - MED
  - LOW

mechanism_notes:
  - mechanism_name: "<机制名称，例如：真元上限钳制>"
    claims:
      - claim: "<结论，必须可被源码验证>"
        confidence: "MED"
        source_path: "LibSourceCodes/guzhenren/src/main/java/net/guzhenren/procedures/KE1Procedure.java"
        source_line_range: "217-258"
        variable_lifecycle: "<变量生命周期，含初始态、更新点、终态或重置点>"
        breakthrough_factor: "<突破因子，含公式/输入项/增减方向；若不适用写 N/A 并说明>"
        cap_clamp_rule: "<上限钳制规则，含边界、触发条件与钳制后行为>"
        initialization_flow: "<初始化流程，含触发入口、初始赋值、后续分配步骤>"
        validation_evidence:
          - "<命令与结果摘要，例如：./gradlew test --tests '*Task6*' 通过>"
          - "<补充证据路径，例如：.sisyphus/evidence/task-6-gzr-template-happy.txt>"

      - claim: "<可选：同一机制的第二条结论>"
        confidence: "LOW"
        source_path: "LibSourceCodes/guzhenren/src/main/java/net/guzhenren/procedures/UIxiuliananjianProcedure.java"
        source_line_range: "83-150"
        variable_lifecycle: "<按证据填写>"
        breakthrough_factor: "<按证据填写>"
        cap_clamp_rule: "<按证据填写>"
        initialization_flow: "<按证据填写>"
        validation_evidence:
          - "<命令与结果摘要>"
```

## 推荐源码锚点（只用于填写时定位，不代表结论本身）
- `LibSourceCodes/guzhenren/src/main/java/net/guzhenren/procedures/KE1Procedure.java:217-258`
- `LibSourceCodes/guzhenren/src/main/java/net/guzhenren/procedures/UIxiuliananjianProcedure.java:83-150`
- `LibSourceCodes/guzhenren/src/main/java/net/guzhenren/procedures/JiarushijieshiProcedure.java:35-82`
- `LibSourceCodes/guzhenren/src/main/java/net/guzhenren/procedures/UikongqiaoquedingProcedure.java:27-345`

## 写回要求（硬性）
- 写回目标固定为 `GZR_INFO.md`，不得改写为其他文件。
- 写回前必须完成字段完整性校验，并保留 happy/failure 两类证据。
- 写回内容中的每个 claim 必须可回溯到 `source_path + source_line_range`。
