# Bastion 配置示例

本文档提供 Bastion 系统的配置示例和说明。

## 配置文件位置

基地类型配置位于：
```
data/guzhenrenext/bastion_type/<type_id>.json
```

## 配置结构概览

```json
{
  "id": "default",
  "display_name": "默认基地",
  "primary_dao": "mu_dao",
  "max_tier": 9,
  
  "anchors_weight": 10,
  "mycelium_weight": 1,
  
  "upkeep": { ... },
  "spawning": { ... },
  "expansion": { ... },
  "connectivity": { ... },
  "decay": { ... },
  "evolution": { ... },
  "aura": { ... },
  "energy": { ... },
  "energy_loss": { ... },
  "node_content": { ... },
  "shell": { ... },
  "elite": { ... },
  "boss": { ... },
  "threat": { ... },
  "pollution": { ... },
  "capture": { ... }
}
```

## 主要配置项说明

### 基础配置

| 字段 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `id` | string | - | 配置唯一标识 |
| `display_name` | string | - | 显示名称 |
| `primary_dao` | string | - | 主道途 (zhi_dao/hun_dao/mu_dao/li_dao) |
| `max_tier` | int | 9 | 最大转数 |
| `anchors_weight` | int | 10 | Anchor 权重（有效节点数计算） |
| `mycelium_weight` | int | 1 | 菌毯权重（有效节点数计算） |

### 守卫维护费 (upkeep)

```json
"upkeep": {
  "enabled": true,
  "cost_per_guardian": 0.1,
  "shutdown_threshold": 0.0
}
```

### 扩张配置 (expansion)

```json
"expansion": {
  "base_interval": 100,
  "base_cost": 1.0,
  "max_radius": 128
}
```

### 能源配置 (energy)

```json
"energy": {
  "enabled": true,
  "scan_radius": 16,
  "photosynthesis_multiplier": 1.0,
  "hydration_multiplier": 1.0,
  "geothermal_multiplier": 1.0,
  "priority_order": ["PHOTOSYNTHESIS", "HYDRATION", "GEOTHERMAL", "WIND", "NIGHT"]
}
```

### 能源损耗 (energy_loss)

```json
"energy_loss": {
  "enabled": true,
  "base_distance": 32,
  "loss_per_block": 0.01,
  "max_loss_ratio": 0.5
}
```

### 孵化巢配置 (node_content.hatchery)

```json
"node_content": {
  "hatchery": {
    "enabled": true,
    "spawn_per_cycle": 2,
    "max_alive": 10,
    "cooldown_ticks": 1200,
    "spawn_weights": {
      "minion": 50,
      "ranged": 30,
      "support": 20
    }
  }
}
```

### 炮台配置 (node_content.turret)

```json
"node_content": {
  "turret": {
    "enabled": true,
    "damage": 4.0,
    "range": 16,
    "cooldown_ticks": 40
  }
}
```

### 陷阱配置 (node_content.trap)

```json
"node_content": {
  "trap": {
    "enabled": true,
    "trigger_radius": 4,
    "effect_duration": 100,
    "cooldown_ticks": 60
  }
}
```

### 光环配置 (aura)

```json
"aura": {
  "base_radius": 32,
  "tier_exponent": 1.2,
  "min_falloff": 0.05,
  "falloff_power": 2.0,
  "build_cost": 10.0,
  "max_count": 4
}
```

### 威胁值配置 (threat)

```json
"threat": {
  "enabled": true,
  "decay_rate": 1,
  "decay_interval": 1200,
  "event_cooldown": 200
}
```

### 污染配置 (pollution)

```json
"pollution": {
  "enabled": true,
  "base_rate": 0.001,
  "max_value": 1.0
}
```

### 接管配置 (capture)

```json
"capture": {
  "enabled": true,
  "capturable_timeout": 12000
}
```

### Boss 配置 (boss)

```json
"boss": {
  "enabled": true,
  "health_multiplier": 2.0,
  "damage_multiplier": 1.5,
  "spawn_cooldown": 6000,
  "threat_multipliers": [
    { "threat_level": 1, "stat_multiplier": 1.0, "reward_multiplier": 1.0 },
    { "threat_level": 2, "stat_multiplier": 1.5, "reward_multiplier": 2.0 },
    { "threat_level": 3, "stat_multiplier": 2.0, "reward_multiplier": 3.0 }
  ]
}
```

### 反爆外壳 (node_content.anti_explosion)

```json
"node_content": {
  "anti_explosion": {
    "enabled": true,
    "build_cost": 20.0,
    "max_count": 2
  }
}
```

### 反火外壳 (node_content.anti_fire)

```json
"node_content": {
  "anti_fire": {
    "enabled": true,
    "build_cost": 15.0,
    "max_count": 2
  }
}
```

## 完整示例

```json
{
  "id": "mu_dao_forest",
  "display_name": "木道森林基地",
  "primary_dao": "mu_dao",
  "max_tier": 9,
  "anchors_weight": 10,
  "mycelium_weight": 1,
  
  "expansion": {
    "base_interval": 80,
    "base_cost": 0.8,
    "max_radius": 160
  },
  
  "energy": {
    "enabled": true,
    "photosynthesis_multiplier": 1.5
  },
  
  "aura": {
    "base_radius": 40,
    "build_cost": 8.0
  },
  
  "node_content": {
    "hatchery": {
      "enabled": true,
      "spawn_per_cycle": 3,
      "max_alive": 15
    }
  }
}
```

## 注意事项

1. 所有配置项都有默认值，只需配置需要修改的项
2. 使用 `optionalFieldOf` 确保向后兼容
3. 道途类型：`zhi_dao`（智）、`hun_dao`（魂）、`mu_dao`（木）、`li_dao`（力）
4. 时间单位为 tick（20 tick = 1 秒）
