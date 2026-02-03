package com.Kizunad.guzhenrenext.bastion.service;

import com.Kizunad.guzhenrenext.bastion.BastionData;
import com.Kizunad.guzhenrenext.bastion.talent.BastionTalentData;

/**
 * 基地天赋点兑换服务。
 * <p>
 * 提供将资源池资源按固定比例转换为天赋点的逻辑，并返回更新后的基地数据。
 * </p>
 */
public final class BastionTalentService {

    private BastionTalentService() {
    }

    /**
     * 兑换结果封装。
     *
     * @param updatedBastion 兑换后更新的基地数据
     * @param pointsGained   获得的天赋点数量
     * @param resourceSpent  实际消耗的资源量
     */
    public record ConversionResult(BastionData updatedBastion, int pointsGained, double resourceSpent) {
    }

    /**
     * 将指定资源量转换为天赋点。
     *
     * @param bastion       基地数据
     * @param resourceAmount 请求消耗的资源量
     * @return 兑换结果（包含更新后的基地数据和获得的点数）
     * @throws IllegalArgumentException 当参数或资源不足时抛出
     */
    public static ConversionResult convertResourceToTalentPoints(BastionData bastion, double resourceAmount) {
        if (bastion == null) {
            throw new IllegalArgumentException("基地数据为空");
        }
        if (resourceAmount <= 0.0) {
            throw new IllegalArgumentException("资源消耗必须大于 0");
        }

        int points = (int) Math.floor(resourceAmount / TalentConstants.RESOURCE_PER_POINT);
        if (points <= 0) {
            throw new IllegalArgumentException("资源不足以兑换 1 点天赋（100 资源 = 1 点）");
        }

        double requiredResource = points * TalentConstants.RESOURCE_PER_POINT;
        if (bastion.resourcePool() < requiredResource) {
            throw new IllegalArgumentException("基地资源不足，无法完成兑换");
        }

        double newPool = bastion.resourcePool() - requiredResource;
        BastionTalentData newTalent = bastion.talentData().withAddPoints(points);
        BastionData updated = bastion.withResourcePool(newPool).withTalentData(newTalent);
        return new ConversionResult(updated, points, requiredResource);
    }

    /**
     * 常量集中处，避免魔法数字。
     */
    private static final class TalentConstants {
        static final double RESOURCE_PER_POINT = 100.0;

        private TalentConstants() {
        }
    }
}
