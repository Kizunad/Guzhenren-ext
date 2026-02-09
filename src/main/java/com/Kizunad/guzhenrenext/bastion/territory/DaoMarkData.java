package com.Kizunad.guzhenrenext.bastion.territory;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * 区块的道痕数据。
 * <p>
 * 记录区块内智、魂、木、力四种道痕的强度。
 * 道痕强度决定了领域的性质和强度。
 * </p>
 *
 * @param zhiDao 智道道痕强度
 * @param hunDao 魂道道痕强度
 * @param muDao  木道道痕强度
 * @param liDao  力道道痕强度
 */
public record DaoMarkData(float zhiDao, float hunDao, float muDao, float liDao) {

    /** 空道痕数据。 */
    public static final DaoMarkData EMPTY = new DaoMarkData(0.0f, 0.0f, 0.0f, 0.0f);

    /** 编解码器。 */
    public static final Codec<DaoMarkData> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            Codec.FLOAT.optionalFieldOf("zhi_dao", 0.0f).forGetter(DaoMarkData::zhiDao),
            Codec.FLOAT.optionalFieldOf("hun_dao", 0.0f).forGetter(DaoMarkData::hunDao),
            Codec.FLOAT.optionalFieldOf("mu_dao", 0.0f).forGetter(DaoMarkData::muDao),
            Codec.FLOAT.optionalFieldOf("li_dao", 0.0f).forGetter(DaoMarkData::liDao)
        ).apply(instance, DaoMarkData::new)
    );

    /**
     * 判断是否为空（所有道痕均为 0）。
     */
    public boolean isEmpty() {
        return zhiDao == 0.0f && hunDao == 0.0f && muDao == 0.0f && liDao == 0.0f;
    }

    /**
     * 合并两个道痕数据（相加）。
     */
    public DaoMarkData merge(DaoMarkData other) {
        if (other == null || other.isEmpty()) {
            return this;
        }
        return new DaoMarkData(
            this.zhiDao + other.zhiDao,
            this.hunDao + other.hunDao,
            this.muDao + other.muDao,
            this.liDao + other.liDao
        );
    }
    
    /**
     * 缩放道痕数据（乘以系数）。
     */
    public DaoMarkData scale(float factor) {
        return new DaoMarkData(
            this.zhiDao * factor,
            this.hunDao * factor,
            this.muDao * factor,
            this.liDao * factor
        );
    }
}
