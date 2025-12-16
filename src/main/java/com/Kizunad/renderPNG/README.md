此原生库用于提供一个便捷的“加载外部 PNG 并在世界中渲染”的能力，典型用途是技能特效（例如在玩家身后显示一张 PNG）。

## 当前实现

- `com.Kizunad.renderPNG.client.PngTextureLoader`
  - 使用 `NativeImage` 读取 PNG、用 `DynamicTexture` 上传 GPU、通过 `TextureManager` 注册到游戏。
  - 提供文件/URL 的异步加载（IO 在线程池、注册在客户端线程），并带缓存/释放能力。

- `com.Kizunad.renderPNG.client.BackPngEffect*`
  - 以实体 ID 为键管理“背后 PNG”渲染参数，并在 `RenderLevelStageEvent.Stage.AFTER_ENTITIES` 阶段绘制。
  - 默认使用 Billboard（面向摄像机）方式渲染，适合作为技能特效展示。

## 使用示例（技能开始/结束）

1) 加载纹理（文件或 URL）并设置特效：

```java
PngTextureLoader.loadFromFileAsync("skill_fire", Path.of("/abs/path/fire.png"))
    .thenAccept(location -> {
        if (location == null) {
            return;
        }
        int playerId = Minecraft.getInstance().player.getId();
        BackPngEffect effect = new BackPngEffect(
            location,
            1.2F,
            1.2F,
            0.6F,
            0.0F,
            0xCCFFFFFF,
            true
        );
        BackPngEffectManager.setForEntity(playerId, effect);
    });
```

2) 技能结束时清理：

```java
BackPngEffectManager.clearForEntity(Minecraft.getInstance().player.getId());
PngTextureLoader.release("skill_fire");
```
