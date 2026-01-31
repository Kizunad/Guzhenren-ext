# DevNote：Universal DamageType Tag（跨模组伤害分类）

## 结论（TL;DR）

- **统一分类入口用 DamageType Tag**，namespace 固定为 `guzhenren`。
- 其他模组只要在自己资源里追加同名 tag 文件，就能被本模组/其他模组用同一套规则识别。
- **永远使用 `"replace": false`**，否则会覆盖其他模组/数据包的 tag 内容。

---

## 设计目标

1. **跨模组兼容**：不同 modId 的 `DamageType` 都能被统一识别为某个道途（如魂道/智道）或机制类型（direct/dot/pierce）。
2. **语义与机制解耦**：
   - 道途：`guzhenren:<dao>_dao`（例如 `guzhenren:hun_dao`）
   - 机制：`guzhenren:damage_kind_*`（例如 `guzhenren:damage_kind_pierce`）
3. **扩展不需要改代码**：新增模组只要“定义 DamageType + 给它打 tag”。

---

## 现有 Tag 清单

### 1) Universal Identifier（本模组伤害来源集合）

- `guzhenren:damage_from_guzhenrenext`
  - 作用：快速判断“这个伤害是否来自 guzhenrenext”。

> 约定：如果你是其他模组，建议定义自己的聚合 tag：`guzhenren:damage_from_<your_modid>`。

### 2) 机制标签（Damage Kind）

- `guzhenren:damage_kind_direct`
- `guzhenren:damage_kind_dot`
- `guzhenren:damage_kind_pierce`

### 3) 道途标签（Dao tags）

- `guzhenren:an_dao`
- `guzhenren:bian_hua_dao`
- `guzhenren:bing_dao`
- `guzhenren:bing_xue_dao`
- `guzhenren:dan_dao`
- `guzhenren:dao_dao`
- `guzhenren:du_dao`
- `guzhenren:fei_xing_dao`
- `guzhenren:feng_dao`
- `guzhenren:gu_dao`
- `guzhenren:guang_dao`
- `guzhenren:hua_dao`
- `guzhenren:huan_dao`
- `guzhenren:hun_dao`
- `guzhenren:jian_dao`
- `guzhenren:jin_dao`
- `guzhenren:jin_dao_forbidden`（禁道）
- `guzhenren:lei_dao`
- `guzhenren:li_dao`
- `guzhenren:lian_dao`
- `guzhenren:lu_dao`
- `guzhenren:meng_dao`
- `guzhenren:mu_dao`
- `guzhenren:nu_dao`
- `guzhenren:qi_dao`
- `guzhenren:qing_mei_dao`（情魅/魅情：已合并）
- `guzhenren:ren_dao`
- `guzhenren:shi_dao`
- `guzhenren:shui_dao`
- `guzhenren:tian_dao`
- `guzhenren:tou_dao`
- `guzhenren:tu_dao`
- `guzhenren:xie_dao`
- `guzhenren:xin_dao`
- `guzhenren:xing_dao`
- `guzhenren:xu_dao`
- `guzhenren:yan_dao`
- `guzhenren:yin_dao`
- `guzhenren:ying_dao`
- `guzhenren:yu_dao`（宇道）
- `guzhenren:yue_dao`
- `guzhenren:yun_dao`（运道）
- `guzhenren:yun_dao_cloud`（云道）
- `guzhenren:zhen_dao`
- `guzhenren:zhi_dao`
- `guzhenren:zhou_dao`（宙道）

---

## 其他模组如何接入（必须按这个做，才能“自动兼容”）

### Step 1：定义你自己的 DamageType

在你的模组资源中新增：

- `src/main/resources/data/<your_modid>/damage_type/<your_damage_type>.json`

示例（机制参数按需调整）：

```json
{
  "message_id": "<your_modid>.<your_damage_type>",
  "exhaustion": 0.0,
  "scaling": "never",
  "effects": "hurt",
  "death_message_type": "default"
}
```

### Step 2：把你的 DamageType 加入 guzhenren 的统一 Tag

在你的模组资源中新增/追加同名 tag 文件（关键点：**namespace 必须是 `guzhenren`**）：

- `src/main/resources/data/guzhenren/tags/damage_type/hun_dao.json`

内容示例：

```json
{
  "replace": false,
  "values": [
    "<your_modid>:<your_hun_dao_damage_type>"
  ]
}
```

如果这个伤害属于穿刺：

- `src/main/resources/data/guzhenren/tags/damage_type/damage_kind_pierce.json`

```json
{
  "replace": false,
  "values": [
    "<your_modid>:<your_hun_dao_damage_type>"
  ]
}
```

### Step 3（可选）：提供你自己的“模组聚合 tag”

这样别人可以快速判断“伤害来自哪个模组”：

- `src/main/resources/data/guzhenren/tags/damage_type/damage_from_<your_modid>.json`

```json
{
  "replace": false,
  "values": [
    "<your_modid>:<your_damage_type_1>",
    "<your_modid>:<your_damage_type_2>"
  ]
}
```

---

## 代码侧识别方式（跨模组统一写法）

Java 侧不要硬编码某个模组的 `DamageType` id，优先用 tag：

- `DamageSource#is(TagKey<DamageType>)`

本仓库常量入口：
- `src/main/java/com/Kizunad/guzhenrenext/damage/GuzhenrenDamageTypeTags.java:1`

如果你在其他模组不想依赖本仓库代码，可以自己创建：

```java
var hunDao = TagKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("guzhenren", "hun_dao"));
if (source.is(hunDao)) {
    // 魂道伤害统一处理
}
```

---

## 常见坑

- `replace=true`：会把整个 tag 重置成你的 values，直接把别人都干掉（严重不兼容）。
- 只加 tag 不定义 DamageType：会导致 tag 引用不存在的 registry entry（可能报错/丢失）。
- 想用 `*:hun_dao` 这种“通配符 tag”：Minecraft 的 tag id 不支持通配符，必须约定统一 namespace（这里就是 `guzhenren`）。
