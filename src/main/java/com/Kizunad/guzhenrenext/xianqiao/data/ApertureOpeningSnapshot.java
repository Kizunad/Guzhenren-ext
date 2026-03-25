package com.Kizunad.guzhenrenext.xianqiao.data;

public record ApertureOpeningSnapshot(
    int zhuanshu,
    int jieduan,
    int heavenScore,
    int earthScore,
    int humanScore,
    int balanceScore,
    boolean ascensionAttemptInitiated,
    boolean snapshotFrozen
) {

    public ApertureOpeningSnapshot {
        zhuanshu = Math.max(0, zhuanshu);
        jieduan = Math.max(0, jieduan);
        heavenScore = ApertureWorldDataSchema.clampScore(heavenScore);
        earthScore = ApertureWorldDataSchema.clampScore(earthScore);
        humanScore = ApertureWorldDataSchema.clampScore(humanScore);
        balanceScore = ApertureWorldDataSchema.clampScore(balanceScore);
    }
}
