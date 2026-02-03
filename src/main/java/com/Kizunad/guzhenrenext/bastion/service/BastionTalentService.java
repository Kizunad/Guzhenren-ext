package com.Kizunad.guzhenrenext.bastion.service;

import com.Kizunad.guzhenrenext.bastion.BastionData;
import com.Kizunad.guzhenrenext.bastion.talent.BastionTalentData;
import com.Kizunad.guzhenrenext.bastion.talent.BastionTalentNode;
import com.Kizunad.guzhenrenext.bastion.talent.BastionTalentRegistry;

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
     * 解锁结果封装。
     * <p>
     * 成功时返回更新后的基地数据；失败时 updatedBastion 为空。
     * </p>
     *
     * @param updatedBastion 更新后的基地数据（成功时非空）
     * @param success        是否成功
     * @param message        结果信息（失败原因或成功提示）
     */
    public record UnlockResult(BastionData updatedBastion, boolean success, String message) {
        public static UnlockResult success(BastionData bastion) {
            return new UnlockResult(bastion, true, "解锁成功");
        }

        public static UnlockResult failure(String msg) {
            return new UnlockResult(null, false, msg);
        }
    }

    /**
     * 尝试解锁指定天赋节点。
     * <p>
     * 按顺序检查节点存在性、重复解锁、前置节点、点数是否充足。
     * </p>
     *
     * @param bastion 基地数据
     * @param nodeId  节点 id
     * @return 解锁结果（失败包含原因）
     */
    public static UnlockResult tryUnlockNode(BastionData bastion, String nodeId) {
        if (bastion == null) {
            return UnlockResult.failure("基地数据为空");
        }
        if (nodeId == null || nodeId.isBlank()) {
            return UnlockResult.failure("节点 ID 不能为空");
        }

        BastionTalentNode node = BastionTalentRegistry.getNode(nodeId);
        if (node == null) {
            return UnlockResult.failure("节点不存在");
        }

        BastionTalentData talentData = bastion.talentData();
        if (talentData.isUnlocked(nodeId)) {
            return UnlockResult.failure("节点已解锁");
        }

        for (String prerequisite : node.prerequisites()) {
            if (!talentData.isUnlocked(prerequisite)) {
                return UnlockResult.failure("前置节点未解锁: " + prerequisite);
            }
        }

        if (talentData.availablePoints() < node.cost()) {
            return UnlockResult.failure("天赋点不足");
        }

        BastionTalentData newTalent = talentData.withUnlock(nodeId, node.cost());
        BastionData updated = bastion.withTalentData(newTalent);
        return UnlockResult.success(updated);
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
