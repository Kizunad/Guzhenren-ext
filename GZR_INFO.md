伤害遵循:
  1转: 1-10
  2转: 10-100
  3转: 100-1000
  4转: 1000-10000

蛊虫转数对照表:
  LibSourceCodes/guzhenren/src/main/resources/data/guzhenren/tags/item/z5.json
  LibSourceCodes/guzhenren/src/main/resources/data/guzhenren/tags/item/z4.json
  LibSourceCodes/guzhenren/src/main/resources/data/guzhenren/tags/item/z1.json
  LibSourceCodes/guzhenren/src/main/resources/data/guzhenren/tags/item/z3.json
  LibSourceCodes/guzhenren/src/main/resources/data/guzhenren/tags/item/z2.json

真元消耗:
  真元消耗必须遵循 ZhenYuanHelper.calculateGuCost 的转数-阶段计算方式 作为标准的消耗量化方式。
  一转青铜真元、二转赤铁真元、三转白银真元、四转黄金真元、五转紫晶真元。

蛊真人额外资源类型:
  真元: 催动蛊虫、杀招基本必要的一种资源量;
     真元消耗量: 对应转数，阶段边界应为(0,1000);
  精力：偏体能的一种资源量，可作为力道，剑道，刀道等资源消耗的途径;
     精力消耗量: 此类无缩放算法，玩家初始值为100，推荐低转(1-3)消耗<100，高转(3-5)消耗<200;
  念头：偏智力的一种资源量，可作为智道，法术类型道资源消耗的途径，当念头小于0时玩家将**无法使蛊虫**，会被持续沉默;
     念头消耗量: 此类同样无缩放算法，玩家初始念头上限为1000，自然可以回复的念头上限为500，推荐低转消耗<100，高转<500;
  魂魄: 魂道资源量，魂魄为零0时玩家将会死亡。魂魄损失后无法自动回复，需要使用蛊虫或者睡觉回复
     魂魄消耗量: 此类同样无缩放算法，玩家初始魂魄为100，可以通过后天使用蛊虫来增加上限(上限1万左右)，推荐低转<100，高转<1000;
  道痕：偏珍贵的一种资源，获取难度较大，推荐可选强力/厉害的杀招、蛊虫催动的一种消耗方式;
     道痕消耗量：推荐1-100 
  道德：道德初始为0，玩家道德过低时正道蛊师或NPC将主动攻击玩家，魔道手段可以消耗道德之类的，影响心境;
     道德消耗量：  
  人气：玩家所有流派经验的总和，人气越高玩家突破打境界的成功率越高，可作为特殊攻击手段;
     人气消耗量:   
  气运：玩家初始气运为0，气运越高开赌石出货，捕捉野外蛊虫，突破大境界的概率将会提高;
     气运消耗量: 推荐0.01-1;
  体质：开窍后玩家会有不同体质，不同体质带来的增幅也不同。
     体质无法被消耗? 或许可以，在不同体质之间转化(此处待解析源码体质)
       细节: 已知十绝体：太日阳莽体、古月阴荒体、北冥冰魄体、森海轮迴体、炎煌雷泽体、万金妙华体、大力真武体、逍遥智心体。第十一绝体：纯梦求真体。太日阳莽体(宇道)：将前闪替换为超远的空间移动，同时消耗更少体力并自带生命恢复 Buff；古月阴荒体(宙道)：修炼速度加快三倍；北冥冰魄体(魂道/冰雪道)：免疫寒冷效果及低级魂魄攻击；森海轮回体(木道)：有生命提升，生命回复，永久饱和；炎煌雷泽体(炎道/雷道)：免疫麻痹及火焰伤害；万金妙华体(金道)：自带急迫效果，生命恢复；大力真武体(力道)：更快的精力恢复，精力150；逍遥智心体(智道)：念头上限提升为10000.5并加快恢复速度，推算加快；厚土元央体(土道)：拥有20的格挡值，抗性;宇宙大衍体(律道)：自带威力翻倍IV Buff。气运之子：自带50上下的运气，可用于开赌石/捕捉野生蛊虫成功率提高/突破概率提升，并自带 0.1 运道道痕。未完成：至尊仙胎/纯梦求真/天外之魔/正德善身。

  境界：目前分为1-5转，每转分为初阶，中阶，高阶，巅峰。境界越高真元越耐用。
(小建议: 推荐同时搭配Minecraft自带资源量:血量、饱食度以多样化)

## Task39 验证机制笔记（结构化写回）

```yaml
template_version: "task6-gzr-note-v1"
writeback_target: "GZR_INFO.md"
confidence_enum:
  - HIGH
  - MED
  - LOW

mechanism_notes:
  - mechanism_name: "真元变量生命周期与同步"
    claims:
      - claim: "真元读写统一通过 GuzhenrenModVariables.PlayerVariables，读取入口是 getAmount/getMaxAmount，写入入口是 modify。"
        confidence: "HIGH"
        source_path: "src/main/java/com/Kizunad/guzhenrenext/guzhenrenBridge/ZhenYuanHelper.java"
        source_line_range: "20-43,50-57,139-143"
        variable_lifecycle: "初始态来自 entity.getData(GuzhenrenModVariables.PLAYER_VARIABLES)；读取经 vars.zhenyuan/vars.zuida_zhenyuan；写入在 modify 内对 vars.zhenyuan 赋新值。"
        breakthrough_factor: "N/A，当前证据只覆盖变量承载与读写入口，不直接定义突破概率公式。"
        cap_clamp_rule: "写入前走 [0, zuida_zhenyuan] 区间钳制，避免负真元和超上限。"
        initialization_flow: "变量容器由 PLAYER_VARIABLES 附着提供，业务侧通过 helper 读取并在修改点写回。"
        validation_evidence:
          - "Read: src/main/java/com/Kizunad/guzhenrenext/guzhenrenBridge/ZhenYuanHelper.java，确认 getVariables/getAmount/getMaxAmount/modify 调用链。"
          - "Read: src/main/java/com/Kizunad/guzhenrenext/guzhenrenBridge/README.md，字段命名包含 zhenyuan、zuida_zhenyuan、zhuanshu、jieduan。"

      - claim: "modify 仅在数值发生变化时触发同步标记，避免无变化写回导致额外同步。"
        confidence: "HIGH"
        source_path: "src/main/java/com/Kizunad/guzhenrenext/guzhenrenBridge/ZhenYuanHelper.java"
        source_line_range: "62-66"
        variable_lifecycle: "original 与 newValue 比较，不相等才写 vars.zhenyuan 并 markSyncDirty。"
        breakthrough_factor: "N/A，属于状态同步策略，不是突破计算。"
        cap_clamp_rule: "newValue 先被钳制后再比较，保证同步出去的值始终合法。"
        initialization_flow: "先取 vars 与 original/max，再计算 newValue，最后条件写回并置脏。"
        validation_evidence:
          - "Read: src/main/java/com/Kizunad/guzhenrenext/guzhenrenBridge/ZhenYuanHelper.java:62-66。"
          - "结构字段检查命令会覆盖该 claim 的 source_path/source_line_range 存在性。"

  - mechanism_name: "真元上限钳制"
    claims:
      - claim: "modify 对真元执行双边界钳制，结果始终满足 0 <= zhenyuan <= zuida_zhenyuan。"
        confidence: "HIGH"
        source_path: "src/main/java/com/Kizunad/guzhenrenext/guzhenrenBridge/ZhenYuanHelper.java"
        source_line_range: "59-61"
        variable_lifecycle: "输入是 original+amount，经过 Math.max(0, Math.min(max, ...)) 后生成 newValue，作为终态候选。"
        breakthrough_factor: "N/A，钳制规则与突破无直接耦合。"
        cap_clamp_rule: "下限固定 0，上限取 vars.zuida_zhenyuan。"
        initialization_flow: "先读取 original/max，再统一计算 newValue，无分支公式差异。"
        validation_evidence:
          - "Read: src/main/java/com/Kizunad/guzhenrenext/guzhenrenBridge/ZhenYuanHelper.java:55-61。"
          - "该规则与 Task39 要求的 HIGH claim 完全一致。"

  - mechanism_name: "蛊虫消耗折算公式"
    claims:
      - claim: "calculateGuCostDenominator 使用 zhuanshu 与 jieduan 计算分母；当 zhuanshu < 1 时按 1 处理；分母 <= 0 时回退 1。"
        confidence: "HIGH"
        source_path: "src/main/java/com/Kizunad/guzhenrenext/guzhenrenBridge/ZhenYuanHelper.java"
        source_line_range: "112-133"
        variable_lifecycle: "读取 vars.zhuanshu/vars.jieduan，先修正 zhuanshu，再计算 power 与 denominator，最后返回有效分母。"
        breakthrough_factor: "输入项是 jieduan 与 zhuanshu，且 zhuanshu 进入指数项与线性乘项，整体呈阶段与转数共同放大。"
        cap_clamp_rule: "zhuanshu 的下界钳制为 1；denominator 的下界回退为 1。"
        initialization_flow: "空实体直接返回 1；正常路径读取变量、执行修正、计算并返回。"
        validation_evidence:
          - "Read: src/main/java/com/Kizunad/guzhenrenext/guzhenrenBridge/ZhenYuanHelper.java:97-137。"
          - "Read: src/main/java/com/Kizunad/guzhenrenext/guzhenrenBridge/README.md，确认 zhuanshu/jieduan 字段存在于 player_variables。"

      - claim: "原模组流程中，修炼与开窍确认过程多次读写 PlayerVariables，可支持按阶段推进后再进入后续确认。"
        confidence: "MED"
        source_path: "LibSourceCodes/guzhenren/src/main/java/net/guzhenren/procedures/UIxiuliananjianProcedure.java"
        source_line_range: "83-267"
        variable_lifecycle: "UIxiuliananjianProcedure 在多段流程中反复出现 PlayerVariables 局部变量，体现阶段性更新与状态承接。"
        breakthrough_factor: "可确认存在分阶段推进，但仅凭行号表无法断言全部数值公式细节。"
        cap_clamp_rule: "存在变量更新链，但具体每一步是否钳制需源码正文，当前仅给 MED。"
        initialization_flow: "修炼按钮触发 execute，流程内多次进入变量读写与后续动作。"
        validation_evidence:
          - "Read: LibSourceCodes/guzhenren/src/main/java/net/guzhenren/procedures/UIxiuliananjianProcedure.java，源码文件在当前 worktree 可读，source line 83-267 区段可定位。"
          - "Bash: javap -classpath \"local_libs/guzhenren.jar\" -p -l net.guzhenren.procedures.UIxiuliananjianProcedure，用于交叉核对行号表与 PlayerVariables 局部变量分布。"

  - mechanism_name: "登录初始化 / 修炼推进 / 空窍确认流程"
    claims:
      - claim: "登录初始化流程包含 PlayerVariables 写入与后续状态设置，说明玩家进入世界时存在一轮初始化动作。"
        confidence: "MED"
        source_path: "LibSourceCodes/guzhenren/src/main/java/net/guzhenren/procedures/JiarushijieshiProcedure.java"
        source_line_range: "35-83"
        variable_lifecycle: "登录事件触发 execute，局部变量表多处出现 PlayerVariables，体现登录期写入与状态延续。"
        breakthrough_factor: "N/A，证据聚焦初始化链路，不直接给出突破公式。"
        cap_clamp_rule: "行号表能确认变量操作存在，但具体钳制条件无法直接从字节码行号表还原，保守处理。"
        initialization_flow: "onPlayerLoggedIn -> execute -> 私有 execute(event, entity) 链路内执行变量相关写入。"
        validation_evidence:
          - "Read: LibSourceCodes/guzhenren/src/main/java/net/guzhenren/procedures/JiarushijieshiProcedure.java，源码文件在当前 worktree 可读，source line 35-83 区段可定位。"
          - "Bash: javap -classpath \"local_libs/guzhenren.jar\" -p -l net.guzhenren.procedures.JiarushijieshiProcedure，用于交叉核对行号表覆盖。"

      - claim: "空窍确认流程可见多段 PlayerVariables 读写与条件推进，符合‘确认后推进状态’的分段执行特征。"
        confidence: "LOW"
        source_path: "LibSourceCodes/guzhenren/src/main/java/net/guzhenren/procedures/UikongqiaoquedingProcedure.java"
        source_line_range: "27-347"
        variable_lifecycle: "execute 内多个区段出现 PlayerVariables 局部变量，显示连续分支更新。"
        breakthrough_factor: "可能关联阶段跃迁与确认后资源变化，但无源码正文时不做强结论。"
        cap_clamp_rule: "可确认存在更新链，无法仅凭行号表断言具体边界值。"
        initialization_flow: "空窍确认入口 execute(world, entity) 触发，随后在多段流程中推进。"
        validation_evidence:
          - "Read: LibSourceCodes/guzhenren/src/main/java/net/guzhenren/procedures/UikongqiaoquedingProcedure.java，源码文件在当前 worktree 可读，source line 27-347 区段可定位。"
          - "Bash: javap -classpath \"local_libs/guzhenren.jar\" -p -l net.guzhenren.procedures.UikongqiaoquedingProcedure，用于交叉核对行号表覆盖与局部变量分段。"
```
