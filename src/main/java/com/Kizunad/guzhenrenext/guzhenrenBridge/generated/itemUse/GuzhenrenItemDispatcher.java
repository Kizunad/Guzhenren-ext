package com.Kizunad.guzhenrenext.guzhenrenBridge.generated.itemUse;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.guzhenren.item.*;
import net.guzhenren.procedures.*;

public class GuzhenrenItemDispatcher {

    public static boolean dispatch(LivingEntity npc, ItemStack stack) {
        if (stack.isEmpty()) return false;
        Level world = npc.level();
        // 尝试匹配物品类型并调用对应 Procedure
        if (stack.getItem() instanceof WeiLianHuaXiongLiGuItem) {
            WeiLianHuaXiongLiGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaShuiJiaGuItem) {
            WeiLianHuaShuiJiaGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof HuangJinSheLiGuItem) {
            HuangJinSheLiGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof YijieguItem) {
            YijieguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuajizhiguItem) {
            WeilianhuajizhiguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof XueyiguItem) {
            XueyiguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaYuHuGu3Item) {
            WeiLianHuaYuHuGu3YouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof ZiLiGengShengGuItem) {
            ZiLiGengShengGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof JiaItem) {
            JiaYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc);
            return true;
        }
        if (stack.getItem() instanceof XueHeMangGuCanGuFangItem) {
            ShiJunZhiLiGuCanGuFangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof XindejiandaoputongjiweicanwuItem) {
            CanPoHuoYaoJinJingYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc);
            return true;
        }
        if (stack.getItem() instanceof XindehundaodashijiItem) {
            XindehundaodashijiWuPinZaiWuPinLanShiMeiKeFaShengProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaShuiLongGu4Item) {
            WeiLianHuaShuiLongGu4YouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof YuLingGu3Item) {
            YuLingGu3YouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuePenXieGuItem) {
            WeiLianHuePenXieGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof YuheguItem) {
            YuheguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuabufengguItem) {
            WeilianhuabufengguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuafudichouxinguItem) {
            WeilianhuafudichouxinguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof JunHaoGu2Item) {
            JunHaoGu2YouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof JiangherixiaguItem) {
            JiangherixiaguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaHuYaoGuItem) {
            WeiLianHuaHuYaoGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof DilaotongxinglingItem) {
            DilaotongxinglingYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuasanzhuanzhanyiguItem) {
            WeilianhuasanzhuanzhanyiguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof Weilianhuaguiyangu1Item) {
            Weilianhuaguiyangu1YouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuaerzhuanshexinguItem) {
            WeilianhuaerzhuanshexinguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof ShuiJiaGuItem) {
            ShuiJiaGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof HuorengucanpoItem) {
            ShiJunZhiLiGuCanGuFangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof ShiSuoGuItem) {
            ShiSuoGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof BaiXiangSheGuItem) {
            BaiXiangSheGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof ZiyanchancanfangItem) {
            ShiJunZhiLiGuCanGuFangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaDianJinGuItem) {
            WeiLianHuaDianJinGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof XiongHaoGuItem) {
            XiongHaoGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof DaligucanfangItem) {
            ShiJunZhiLiGuCanGuFangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof LianHuGu4Item) {
            LianHuGu4YouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuakongsuoguItem) {
            WeilianhuakongsuoguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaJuChiJinWuGuItem) {
            WeiLianHuaJuChiJinWuGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof NuoyigucanfangItem) {
            ShiJunZhiLiGuCanGuFangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof YunJianQingLianGuCanGuFangItem) {
            ShiJunZhiLiGuCanGuFangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof YuShouLingGu5CanGuFangItem) {
            ShiJunZhiLiGuCanGuFangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuahupoguItem) {
            WeilianhuahupoguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof YuSuiGuCanGuFangItem) {
            ShiJunZhiLiGuCanGuFangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuakuliguItem) {
            WeilianhuakuliguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaYuQuanGu4Item) {
            WeiLianHuaYuLingGu4YouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuadieyingguItem) {
            WeilianhuadieyingguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaYinLinGuItem) {
            WeiLianHuaYinLinGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof XindeyuedaoputongyicanwuItem) {
            XindeyuedaoputongyicanwuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuadianjiangguItem) {
            WeilianhuadianjiangguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof SuanShuiGuItem) {
            SuanShuiGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof PanShiGuItem) {
            PanShiGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof HuoxinguItem) {
            HuoxinguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuajianshangguItem) {
            WeilianhuajianshangguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaYuLinGuItem) {
            WeiLianHuaYuLinGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuahuangjinjiguItem) {
            WeilianhuahuangjinjiguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaGongBeiGu3Item) {
            WeiLianHuaGongBeiGu3DangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaShuiJiaGu4Item) {
            WeiLianHuaShuiJiaGu4YouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof DaliguItem) {
            DaliguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof BingpoguItem) {
            BingpoguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuajinfengsongshuangguItem) {
            WeilianhuajinfengsongshuangguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof TianmoshiItem) {
            TianmoshiYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaTieShouQinNaGuItem) {
            WeiLianHuaTieShouQinNaGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof JianQiaoGuItem) {
            JianQiaoGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof CanposhexindunhunItem) {
            CanPoHuoYaoJinJingYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaXieZouGuItem) {
            WeiLianHuaXieZouGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof HuYaoGuItem) {
            HuYaoGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuadunyuguItem) {
            WeilianhuadunyuguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuajinzhongguItem) {
            WeilianhuajinzhongguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof QiangShenJianTiGuCanGuFangItem) {
            ShiJunZhiLiGuCanGuFangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof Jieligu2Item) {
            Jieligu2YouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaHuaShiGuItem) {
            WeiLianHuaHuaShiGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaYanZhouGuItem) {
            WeiLianHuaYanZhouGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuachixinjinweiliguItem) {
            WeilianhuachixinjinweiliguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof YuHuGu5CanGuFangItem) {
            ShiJunZhiLiGuCanGuFangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaDiXiongZhuaGuItem) {
            WeiLianHuaDiXiongZhuaGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaXingMenGuItem) {
            WeiLianHuaXingMenGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaYuShouLingGu4Item) {
            WeiLianHuaYuShouLingGu4YouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof BianhuadaoxindedashiweicanwuItem) {
            CanPoHuoYaoJinJingYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc);
            return true;
        }
        if (stack.getItem() instanceof YuXiongGu5CanGuFangItem) {
            ShiJunZhiLiGuCanGuFangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof XueMuTianHuaGuItem) {
            XueMuTianHuaGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof YuShouLingGuItem) {
            YuShouLingGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof ChunGuangWuXianGuItem) {
            ChunGuangWuXianGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuadingkongguItem) {
            WeilianhuadingkongguWuPinZaiWuPinLanShiMeiKeFaShengProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof YuXiongGuItem) {
            YuXiongGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaYuHuGu4Item) {
            WeiLianHuaYuHuGu4YouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaJiuChongItem) {
            WeiLianHuaJiuChongDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof YueGuangGuItem) {
            YueGuangGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaJunTuanYiHouGuItem) {
            WeiLianHuaJunTuanYiHouGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaTongPiGuItem) {
            WeiLianHuaTongPiGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof ShouPiGuItem) {
            ShouPiGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuahuanweiguItem) {
            WeilianhuahuanweiguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof JingganggucanfangItem) {
            ShiJunZhiLiGuCanGuFangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof BianhuadaoxindezhundaoshiweicanwuItem) {
            CanPoHuoYaoJinJingYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc);
            return true;
        }
        if (stack.getItem() instanceof ELiGuItem) {
            ELiGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaHenShiGuItem) {
            WeiLianHuaHenShiGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof JizhiguItem) {
            JizhiguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof HuoYouGuItem) {
            HuoYouGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuahuanjianyuguItem) {
            WeilianhuahuanjianyuguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof MudaoxindezhundashiyicanwuItem) {
            Mudaoxindezhundashi2Procedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof HuanYingYueGuItem) {
            HuanYingYueGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof XiuluoshiguItem) {
            XiuluoshiguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof PokongucanfangItem) {
            ShiJunZhiLiGuCanGuFangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof DianJinGuCanGuFangItem) {
            ShiJunZhiLiGuCanGuFangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuakuangfengguItem) {
            WeilianhuakuangfengguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuatiexueguItem) {
            WeilianhuatiexueguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof HuoGuangZhuTianGuItem) {
            HuoGuangZhuTianGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof SuiRenGuCanGuFangItem) {
            ShiJunZhiLiGuCanGuFangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof WeilianhualinghunguItem) {
            WeilianhualinghunguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof BaDaoZhanGuItem) {
            BaDaoZhanGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaXingBanDianGuItem) {
            WeiLianHuaXingBanDianGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaLanNiaoBingGuanGuItem) {
            WeiLianHuaLanNiaoBingGuanGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof LongLiGuCanGuFangItem) {
            ShiJunZhiLiGuCanGuFangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof ShuiLongGu4Item) {
            ShuiLongGu4YouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuafengliguItem) {
            WeilianhuafengliguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuajianrenguItem) {
            WeilianhuajianrenguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof RuChengLingItem) {
            RuChengLingYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(npc);
            return true;
        }
        if (stack.getItem() instanceof YueyingguItem) {
            YueyingguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof Xindedashiji2Item) {
            Xindedashijiliucheng2Procedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof XindeliandaodashiweicanwuItem) {
            CanPoHuoYaoJinJingYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaBingBaoGuItem) {
            WeiLianHuaBingBaoGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof DuZhenGuItem) {
            DuZhenGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaYinYunGuItem) {
            WeiLianHuaYinYunGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuajianxiaguItem) {
            WeilianhuajianxiaguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuajiansuoguItem) {
            WeilianhuajiansuoguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaQuanTuGuItem) {
            WeiLianHuaQuanTuGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof LuoXuanShuiJianGuItem) {
            LuoXuanShuiJianGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaYuYuGuItem) {
            WeiLianHuaYuYuGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof DiYuGuItem) {
            DiYuGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaDaBuLiuXingGuItem) {
            WeiLianHuaDaBuLiuXingGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuaxionghunguItem) {
            WeilianhuaxionghunguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof XindebingxuedaozhunzongshijiweicanwuItem) {
            CanPoHuoYaoJinJingYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaYinYangZhuanShenGUItem) {
            WeiLianHuaYinYangZhuanShenGUYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof YueguItem) {
            YueguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuanuoyiguItem) {
            WeilianhuanuoyiguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof YuHuGu2Item) {
            YuHuGu2YouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuashuipuguItem) {
            WeilianhuashuipuguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuajianluguItem) {
            WeilianhuajianluguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaQingNiuLaoLiGuItem) {
            WeiLianHuaQingNiuLaoLiGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof JiTuGuCanGuFangItem) {
            ShiJunZhiLiGuCanGuFangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuaziligengshenguItem) {
            WeilianhuaziligengshenguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof NianfengzhanguItem) {
            NianfengzhanguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof JuchifengrenguItem) {
            JuchifengrenguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof JieyungucanfangItem) {
            ShiJunZhiLiGuCanGuFangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof HuanjianyuguItem) {
            HuanjianyuguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaXiongTuGuItem) {
            WeiLianHuaXiongTuGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof XindelidaoputongyicanwuItem) {
            Xindelidaoputong2Procedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof HuachunniguItem) {
            HuachunniguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof LingHuGuItem) {
            LingHuGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof XindejindaodashiyicanwuItem) {
            XindejindaodashiyicanwuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof JianyugucanfangItem) {
            ShiJunZhiLiGuCanGuFangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof WuchangguguItem) {
            WuchangguguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof XindeyuedaoputongItem) {
            CanPoHuoYaoJinJingYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc);
            return true;
        }
        if (stack.getItem() instanceof JinlvyigucanfangItem) {
            ShiJunZhiLiGuCanGuFangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuahupiguItem) {
            WeilianhuahupiguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof XuehegucanfangItem) {
            ShiJunZhiLiGuCanGuFangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof XindeguangdaozhundashijiyicanwuItem) {
            XindeguangdaozhundashijiyicanwuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaTianPengGuItem) {
            WeiLianHuaTianPengGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuajiandangguItem) {
            WeilianhuajiandangguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuamuganguItem) {
            WeilianhuamuganguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaLianHuGu4Item) {
            WeiLianHuaLianHuGu4YouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof HunfeiguItem) {
            HunfeiguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuajinlongguItem) {
            WeilianhuahulongguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof GutongpigucanfangItem) {
            ShiJunZhiLiGuCanGuFangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof JianYuYiGuItem) {
            JianYuYiGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof DuochongjianyingItem) {
            DuochongjianyingYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaShuiJiGuItem) {
            WeiLianHuaShuiJiGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof XindeyuedaodashiyicanwuItem) {
            XindeyuedaodashiyicanwuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaBingJiGuItem) {
            WeiLianHuaBingJiGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof ShuiJiaGu5Item) {
            ShuiJiaGu5YouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof DingkonggucanfangItem) {
            ShiJunZhiLiGuCanGuFangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaXuanFengLunGuItem) {
            WeiLianHuaXuanFengLunGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaShuiLongGuItem) {
            WeiLianHuaShuiLongGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuabainianshouguItem) {
            WeilianhuabainianshouguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof HanYueGuCanGuFangItem) {
            ShiJunZhiLiGuCanGuFangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaENianGu5Item) {
            WeiLianHuaENianGu5YouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuaerzhuansuiyiguItem) {
            WeilianhuaerzhuansuiyiguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeilianshilongqunguItem) {
            WeilianshilongqunguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof YuXiongGu4Item) {
            YuXiongGu4YouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaShuiShengHuaGuItem) {
            WeiLianHuaShuiShengHuaGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof ZhangyanguItem) {
            ZhangyanguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof DianwangguItem) {
            DianwangguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaYueHenGuItem) {
            WeiLianHuaYueHenGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaShuiHuGu5Item) {
            WeiLianHuaShuiHuGu5YouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuasanzhuanyuanlaoguItem) {
            WeilianhuasanzhuanyuanlaoguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof HeiShiGuItem) {
            HeiShiGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof YanLeiGuItem) {
            YanLeiGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof XindeshuidaoputongItem) {
            CanPoHuoYaoJinJingYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc);
            return true;
        }
        if (stack.getItem() instanceof YinjiangucanpoItem) {
            ShiJunZhiLiGuCanGuFangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof XieLuoGuItem) {
            XieLuoGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuayicuerjiuguItem) {
            WeilianhuayicuerjiuguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof XueyinguItem) {
            XueyinguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof HuguguItem) {
            HuguguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof JiuXieShengJiCaoItem) {
            JiuXieShengJiCaoDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc, stack);
            return true;
        }
        if (stack.getItem() instanceof JunTuanYiHouGuItem) {
            JunTuanYiHouGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof ShuixiangjiaItem) {
            ShuixiangjiaYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WuzhuanyumaogucanfangItem) {
            ShiJunZhiLiGuCanGuFangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof DuochongjianyinggucanpoItem) {
            ShiJunZhiLiGuCanGuFangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaMoDiGuItem) {
            WeiLianHuaMoDiGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof LeidianlangliguItem) {
            LeidianlangliguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof HunBaoGuCanGuFangItem) {
            ShiJunZhiLiGuCanGuFangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof QingFengLunGuItem) {
            QingFengLunGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaFanShuiGuItem) {
            WeiLianHuaFanShuiGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof JiansuoguItem) {
            JiansuoguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuasongzhenguItem) {
            WeilianhuasongzhenguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof YinYunGuItem) {
            YinYunGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof ShengjijiuhuluItem) {
            ShengjijiuhuluYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof LieZhuaGuItem) {
            LieZhuaGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof YingyanggucanfangItem) {
            ShiJunZhiLiGuCanGuFangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof HuoZhuaGuItem) {
            HuoZhuaGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuafengshangguItem) {
            WeilianhuafengshangguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof CanpojianyulieyingItem) {
            CanPoHuoYaoJinJingYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc);
            return true;
        }
        if (stack.getItem() instanceof JiezeguItem) {
            JiezeguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof XieHuLuGuItem) {
            XieHuLuGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof XindexuedaoputongxindeweicanfuItem) {
            CanPoHuoYaoJinJingYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc);
            return true;
        }
        if (stack.getItem() instanceof KuliguItem) {
            KuliguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof MuganguItem) {
            MuganguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof JianjiaogucanfnagItem) {
            ShiJunZhiLiGuCanGuFangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof WoYiGuCanGuFangItem) {
            ShiJunZhiLiGuCanGuFangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof YantonggucanpoItem) {
            ShiJunZhiLiGuCanGuFangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof SizhuanyuanlaogucanfangItem) {
            ShiJunZhiLiGuCanGuFangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof WuchanggugucanfangItem) {
            ShiJunZhiLiGuCanGuFangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaHuLiGuItem) {
            WeiLianHuaHuLiGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof TieShouQinNaGuItem) {
            TieShouQinNaGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof BingwenshiguItem) {
            BingwenshiguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuadalujuanfengItem) {
            WeilianhuadalujuanfengYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof BaDaoZhan5Item) {
            BaDaoZhan5YouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaBaiGuCheLunGuItem) {
            WeiLianHuaBaiGuCheLunGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeilianhualeixiaoItem) {
            WeilianhualeixiaoYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuadahunguItem) {
            WeilianhuadahunguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof BiKongGuItem) {
            BiKongGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof YueMangGuItem) {
            YueMangGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuatianyuanbaowanglianItem) {
            WeilianhuatianyuanbaowanglianYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof YinLinGuItem) {
            YinLinGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaTiePiGu3Item) {
            WeiLianHuaTiePiGu3DangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof LongliguItem) {
            LongliguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof DingHunGuItem) {
            DingHunGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuajianmaiguItem) {
            WeilianhuajianmaiguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof XindeliandaozhundashiyicanwuItem) {
            XindeliandaozhundashiyicanwuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof XindexuedaodashiyicanwuItem) {
            Xindexuedaodashi2Procedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof CanPoTanZhiJianGuangItem) {
            CanPoHuoYaoJinJingYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuajianzhiguItem) {
            WeilianhuajianzhiguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuayijieguItem) {
            WeilianhuayijieguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuayuheguItem) {
            WeilianhuayuheguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof YuJunGuItem) {
            YuJunGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuajianfengguItem) {
            WeilianhuajianfengguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuajingyueguItem) {
            WeilianhuajingyueguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof XiangyabaijiagucanfangItem) {
            ShiJunZhiLiGuCanGuFangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof XindeliandaodashiyicanwuItem) {
            XindeliandaodashiyicanwuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof FengshangguItem) {
            FengshangguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof ZuanshigucanfangItem) {
            ShiJunZhiLiGuCanGuFangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof GuQiangGuItem) {
            GuQiangGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof CanyuexueyiItem) {
            CanyuexueyiYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaDaDuWa3Item) {
            WeiLianHuaDaDuWa3YouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaYuXiongGu4Item) {
            WeiLianHuaYuXiongGu4YouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof XindejindaoputongweicanwuItem) {
            CanPoHuoYaoJinJingYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuazhanyiguItem) {
            WeilianhuazhanyiguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuahundunguItem) {
            WeilianhuahundunguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaLinJiaGuItem) {
            WeiLianHuaLinJiaGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof XindeguangdaodashijiyicanwuItem) {
            XindeguangdaodashijiyicanwuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof YuLingGu4Item) {
            YuLingGu4YouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof XindeleidaodashiItem) {
            CanPoHuoYaoJinJingYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuaguiqiguItem) {
            WeilianhuaguiqiguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof SheXingGu5Item) {
            SheXingGu5YouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof JianluguItem) {
            JianluguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof JingtieguguItem) {
            JingtieguguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof BingBuGuItem) {
            BingBuGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof LongJuanFengGuItem) {
            LongJuanFengGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuashengjiuItem) {
            WeilianhuashengjiuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuaxueyinguItem) {
            WeilianhuaxueyinguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof QuanTuGuCanGuFangItem) {
            ShiJunZhiLiGuCanGuFangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaBaiYunGuItem) {
            WeiLianHuaBaiYunGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaHuDuGuItem) {
            WeiLianHuaHuDuGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuagangjingtieguItem) {
            WeilianhuagangjingtieguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof GongbeiguItem) {
            GongbeiguDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof HongbiangucanfangItem) {
            ShiJunZhiLiGuCanGuFangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof TongPiGuItem) {
            TongPiGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuayoulongguItem) {
            WeilianhuayoulongguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof YuYuGuItem) {
            YuYuGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof JinlvyiguItem) {
            JinlvyiguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaBaDaoZhanGu4Item) {
            WeiLianHuaBaDaoZhanGu4YouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof YuanLaoGu4Item) {
            YuanLaoGu4YouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof GuilianguItem) {
            GuilianguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaXueHeMangGuItem) {
            WeiLianHuaXueHeMangGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof XindeguangdaopuzhuandashiweicanwuItem) {
            CanPoHuoYaoJinJingYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaQingFengLunGuItem) {
            WeiLianHuaQingFengLunGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof XindeyandaoputongyicanwuItem) {
            XindeyandaoputongyicanwuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof ErzhuanshexinguItem) {
            ErzhuanshexinguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaNuMuXieYanGuItem) {
            WeiLianHuaNuMuXieYanGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaYuQuanGuItem) {
            WeiLianHuaYuQuanGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof BaiGuCheLunGuItem) {
            BaiGuCheLunGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof FengliguItem) {
            FengliguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof FudichouxinguItem) {
            FudichouxinguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof YicuerjiugucanfangItem) {
            ShiJunZhiLiGuCanGuFangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof YanLeiGuCanGuFangItem) {
            ShiJunZhiLiGuCanGuFangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof LuohuguItem) {
            LuohuguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof BingRenGuItem) {
            BingRenGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaXieNingJianGuItem) {
            WeiLianHuaXieNingJianGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof JinZhenGuItem) {
            JinZhenGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuasuiyiguItem) {
            WeilianhuasuiyiguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof ShouGuItem) {
            ShengJiXieDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaYuanLaoGu5Item) {
            WeiLianHuaYuanLaoGu5YouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof Gudaoxindezhunzongshi2Item) {
            Gudaoxindezhunzongshiliucheng2Procedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuayizhuanyumaoguItem) {
            WeilianhuayizhuanyumaoguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof XieKuangZhenFengGuItem) {
            XieKuangZhenFengGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof XindeleidaozhunzongshiyicanwuItem) {
            XindeleidaozhunzongshiyicanwuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof LangTuGuCanGuFangItem) {
            ShiJunZhiLiGuCanGuFangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof TianyuanbaojunlianItem) {
            TianyuanbaojunlianYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof XindejiandaoputongjiyicanwuItem) {
            XindejiandaoputongjiyicanwuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaDiYuGuItem) {
            WeiLianHuaDiYuGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof QuanliyifugusizhuanItem) {
            QuanliyifugusizhuanYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof JianhenguItem) {
            JianhenguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof YuzengguItem) {
            YuzengguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaXieWangGuItem) {
            WeiLianHuaXieWangGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof LeGuDunGuItem) {
            LeGuDunGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeifengguItem) {
            WeifengguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof GucaiyuanshiItem) {
            GucaiyuanshiDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(npc);
            return true;
        }
        if (stack.getItem() instanceof WeiShaoHuaDanQiaoHuoTanGuItem) {
            WeiShaoHuaDanQiaoHuoTanGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof RenShouZangShengGuItem) {
            RenShouZangShengGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof WeilianhualongliguItem) {
            WeilianhualongliguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuasanzhuanbufengguItem) {
            WeilianhuasanzhuanbufengguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof HengChongGuItem) {
            HengChongGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof QunLiGuCanGuFangItem) {
            ShiJunZhiLiGuCanGuFangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaShuiLianGuItem) {
            WeiLianHuaShuiLianGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaSuiRenGuItem) {
            WeiLianHuaSuiRenGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof YuQuanGu5CanGuFangItem) {
            ShiJunZhiLiGuCanGuFangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof CanpoyijiezhenxinbogujuanItem) {
            CanPoHuoYaoJinJingYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc);
            return true;
        }
        if (stack.getItem() instanceof YuLangGuItem) {
            YuLangGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof XieCangGongGuCanGuFangItem) {
            ShiJunZhiLiGuCanGuFangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaJunHaoGu3Item) {
            WeiLianHuaJunHaoGu3YouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof XindejindaoputongyicanwuItem) {
            XindejindaoputongyicanwuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof XuelugucanfangItem) {
            ShiJunZhiLiGuCanGuFangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof XuanwuzhongqucanfangItem) {
            ShiJunZhiLiGuCanGuFangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof JiandangguItem) {
            JiandangguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuagutongpiguItem) {
            WeilianhuagutongpiguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaENianGuItem) {
            WeiLianHuaENianGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof XieZouGuItem) {
            XieZouGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof LeixiaoguItem) {
            LeixiaoguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeilianhusanzhuanhuguguItem) {
            WeilianhusanzhuanhuguguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof ShexingusizhuanItem) {
            ShexingusizhuanYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof SanzhuanyuanlaoguItem) {
            SanzhuanyuanlaoguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof BaiYinSheLiGuItem) {
            BaiYinSheLiGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof JinzhonggucanfangItem) {
            ShiJunZhiLiGuCanGuFangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuajianmuguItem) {
            WeilianhuajianmuguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof DianjiangguItem) {
            DianjiangguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaTuBaWangGuItem) {
            WeiLianHuaTuBaWangGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaBaiYuGuItem) {
            WeiLianHuaBaiYuGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof XieDiGuItem) {
            XieDiGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof HuLiGuItem) {
            HuLiGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof MudaoxindedashiweicanwuItem) {
            CanPoHuoYaoJinJingYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc);
            return true;
        }
        if (stack.getItem() instanceof ShuiHuGuCanGuFangItem) {
            ShiJunZhiLiGuCanGuFangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaTiePiGu2Item) {
            WeiLianHuaTiePiGu2DangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaJieLiGuItem) {
            WeiLianHuaJieLiGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof HongShuiGuItem) {
            HongShuiGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaYuLangGu3Item) {
            WeiLianHuaYuLangGu3YouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof LiaoGuangGuItem) {
            LiaoGuangGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaHuoGuItem) {
            WeiLianHuaHuoGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaMaoJiangGuItem) {
            WeiLianHuaMaoJiangGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof XieCangGongGuItem) {
            XieCangGongGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof JianYingGuItem) {
            JianYingGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WWeiLianHuYingShiGuItem) {
            WWeiLianHuYingShiGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof ErzhuanweilianhuazhanyiguItem) {
            ErzhuanweilianhuazhanyiguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaShuangLinYueGuItem) {
            WeiLianHuaShuangLinYueGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof XindeshuidaodashiyicanwuItem) {
            XindeshuidaodashiyicanwuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof JinrenhuguguItem) {
            JinrenhuguguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaZiLiGengShengGu3Item) {
            WeiLianHuaZiLiGengShengGu3DangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuahuoxinguItem) {
            WeilianhuahuoxinguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaBaDaoZhanGu5Item) {
            WeiLianHuaBaDaoZhanGu5YouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaYueXuanGuItem) {
            WeiLianHuaYueXuanGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof YYinShiGuItem) {
            YYinShiGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof JianzhiguItem) {
            JianzhiguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof BaiShiGuItem) {
            BaiShiGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof DandaoItem) {
            DandaoYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaJianXinGuItem) {
            WeiLianHuaJianXinGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof FanShuiGuItem) {
            FanShuiGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof XindeyandaodashiyicanwuItem) {
            XindeyandaodashiyicanwuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof TianyuanbaowangliancanfangItem) {
            ShiJunZhiLiGuCanGuFangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaHeZiShiItem) {
            WeiLianHuaHeZiShiDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaGuangHongGuItem) {
            WeiLianHuaGuangHongGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuazuanshiguItem) {
            WeilianhuazuanshiguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaYuYingGuItem) {
            WeiLianHuaYuYingGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof XindelidaozhunzongshiweicanwuItem) {
            CanPoHuoYaoJinJingYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuazhiliaoguItem) {
            WeilianhuazhiliaoguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof QingTongSheLiGuItem) {
            QingTongSheLiGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof BaDaoZhanGu5Item) {
            BaDaoZhanGu5YouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof Weilianhuanifengzhangu4Item) {
            Weilianhuanifengzhangu4YouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof GangZongGuItem) {
            GangZongGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof YuanLaoGu1Item) {
            YuanLaoGu1YouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof KurongyouItem) {
            KurongyouYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof HuTuGuItem) {
            HuTuGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof XindeguangdaoputongjiweicanwuItem) {
            CanPoHuoYaoJinJingYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc);
            return true;
        }
        if (stack.getItem() instanceof QuanLiYiFuGuItem) {
            QuanLiYiFuGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuaqingtengjianyiItem) {
            WeilianhuaqingtengjianyiYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaELiGuItem) {
            WeiLianHuaELiGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof HuangjingugufangcanpoItem) {
            ShiJunZhiLiGuCanGuFangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaJingLiGuItem) {
            WeiLianHuaJingLiGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof FengBaWangGuItem) {
            FengBaWangGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeilianhualieyanxiongliguItem) {
            WeilianhualieyanxiongliguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaSiWeiJiuChongItem) {
            WeiLianHuaSiWeiJiuChongDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaTieZhuaYingLiGuItem) {
            WeiLianHuaTieZhuaYingLiGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof QunLiGuItem) {
            QunLiGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof SanzhuanbufengguItem) {
            SanzhuanbufengguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof XieZiShiItem) {
            XieZiShiDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaLiZhanXueQiGuItem) {
            WeiLianHuaLiZhanXueQiGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof TuQiangGuItem) {
            TuQiangGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof BaiguiyexingguItem) {
            BaiguiyexingguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuayugouguItem) {
            WeilianhuayugouguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof XindeleidaozhundashiweicanwuItem) {
            CanPoHuoYaoJinJingYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc);
            return true;
        }
        if (stack.getItem() instanceof XiaozhiguItem) {
            XiaozhiguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuashijunzhiliguItem) {
            WeilianhuashijunzhiliguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaQuanYongMingGuItem) {
            WeiLianHuaQuanYongMingGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof JiuyanjiuchongItem) {
            JiuyanjiuchongWuPinZaiWuPinLanShiMeiKeFaShengProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof JunHaoGu4Item) {
            JunHaoGu4YouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaHuoYanGuItem) {
            WeiLianHuaHuoYanGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuajingtieguItem) {
            WeilianhuajingtieguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuahuguguItem) {
            WeilianhuahuguguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof DalongjuanfengucanfangItem) {
            ShiJunZhiLiGuCanGuFangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof WuwangucanfangItem) {
            ShiJunZhiLiGuCanGuFangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuatupiguItem) {
            WeilianhuatupiguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuacanyuexueyiguItem) {
            WeilianhuacanyuexueyiguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof SanzhuanshexingguItem) {
            SanzhuanshexingguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof ShuiZuanGuItem) {
            ShuiZuanGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof XueDiZiGuItem) {
            XueDiZiGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof SizhuanWSTJgucanfangItem) {
            SizhuanWSTJgucanfangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof ZhongShuiGuCanGuFangItem) {
            ShiJunZhiLiGuCanGuFangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof YuShouLingGu5Item) {
            YuShouLingGu5YouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof YuLingGuItem) {
            YuLingGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof DalongjuanfengguItem) {
            DalongjuanfengguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof XingMenMuGuItem) {
            XingMenMuGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof JinfeiguItem) {
            JinfeiguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof XieZhuiItem) {
            JianzhiYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof JianXinGuCanGuFangItem) {
            ShiJunZhiLiGuCanGuFangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof JunTuanYiHouGuCanGuFangItem) {
            ShiJunZhiLiGuCanGuFangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof QiXiangJiuChongItem) {
            QiXiangJiuChongDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaFengBaWangGuItem) {
            WeiLianHuaFengBaWangGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaYuLangGu2Item) {
            WeiLianHuaYuLangGu2YouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof SizhuanyuhugucanfangItem) {
            SizhuanyuhugucanfangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof YubenggucanfangItem) {
            ShiJunZhiLiGuCanGuFangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof ShuiGuangGuItem) {
            ShuiGuangGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof SanXingZaiTianGuItem) {
            SanXingZaiTianGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof YueDuGuItem) {
            YueDuGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaTongPiGu2Item) {
            WeiLianHuaTongPiGu2DangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof HuangjinjiaguItem) {
            HuangjinjiaguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof XindehundaozhundashijiweicanwuItem) {
            CanPoHuoYaoJinJingYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc);
            return true;
        }
        if (stack.getItem() instanceof JianyinguItem) {
            JianyinguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaJianYuYiGuItem) {
            WeiLianHuaJianYuYiGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuaqingtongguItem) {
            WeilianhuaqingtongguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof GanjingucanfangItem) {
            ShiJunZhiLiGuCanGuFangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof AiBieChiItem) {
            AiBieChiDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof ENianGu5CanGuFangItem) {
            ShiJunZhiLiGuCanGuFangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuajiedanguItem) {
            WeilianhuajiedanguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof SizhuanziligengshenggucanfangItem) {
            ShiJunZhiLiGuCanGuFangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuaerzhuanyuanlaoguItem) {
            WeilianhuaerzhuanyuanlaoguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeilianhujingtiepiguItem) {
            WeilianhujingtiepiguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof XindeyuedaozhunzongshijicanwuItem) {
            XindeyuedaozhunzongshijicanwuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof YuGuGuItem) {
            YuGuGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof LiandaoguItem) {
            LiandaoguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaJianFengGu5Item) {
            WeiLianHuaJianFengGu5YouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof DaoGuangGuItem) {
            DaoGuangGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof HongbianguItem) {
            HongbianguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof LiejiangucanfangItem) {
            ShiJunZhiLiGuCanGuFangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof DianyanguItem) {
            DianyanguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaBiGuYiItem) {
            WeiLianHuaBiGuYiDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof TieShouQinNaGuCanGuFangItem) {
            ShiJunZhiLiGuCanGuFangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof YicuerjiuguItem) {
            YicuerjiuguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaXiongHaoGuItem) {
            WeiLianHuaXiongHaoGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaGuZhuGuItem) {
            WeiLianHuaGuZhuGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaBiKongGuItem) {
            WeiLianHuaBiKongGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof ChunGuangWuXianGuCanGuFangItem) {
            ShiJunZhiLiGuCanGuFangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof XuanwuzhongquItem) {
            XuanwuzhongquYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof ShuiBiGuItem) {
            ShuiBiGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaChiTieSheLiGuItem) {
            WeiLianHuaChiTieSheLiGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaJunHaoGuItem) {
            WeiLianHuaJunHaoGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof GongbeiguwuzhuanItem) {
            GongbeiguwuzhuanYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof JingtiepiguItem) {
            JingtiepiguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof HuangjinguItem) {
            HuangjinguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof XindelianzhunzongshijiyicanwuItem) {
            XindelianzhunzongshijiyicanwuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof ShuiJiaGu5CanGuFangItem) {
            ShiJunZhiLiGuCanGuFangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof YueShengGuItem) {
            YueShengGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof CanpocananfanghuazhouItem) {
            CanPoHuoYaoJinJingYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc);
            return true;
        }
        if (stack.getItem() instanceof DiXiongZhuaGuItem) {
            DiXiongZhuaGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof TianYuanBaoLianItem) {
            TianYuanBaoLianDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof ChangqingguItem) {
            ChangqingguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof YantongguItem) {
            YantongguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof XindeguangdaozhunzongshijiweicanwuItem) {
            CanPoHuoYaoJinJingYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaShiPiGuItem) {
            WeiLianHuaShiPiGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaYuLangGu4Item) {
            WeiLianHuaYuLangGu4YouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaHunBaoGuItem) {
            WeiLianHuaHunBaoGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof YuShouLingGu3Item) {
            YuShouLingGu3YouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof ZhiLuGu5CanGuFangItem) {
            ShiJunZhiLiGuCanGuFangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaQingReGuItem) {
            WeiLianHuaQingReGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof GuihuoItem) {
            GuihuoYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof XindeguangdaodashijiweiciwuItem) {
            CanPoHuoYaoJinJingYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuayuzangjianguguItem) {
            WeilianhuayuzangjianguguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof YuzenggucanfangItem) {
            ShiJunZhiLiGuCanGuFangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof JianyuguItem) {
            JianyuguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof JiuyanjiuchongcanfangItem) {
            ShiJunZhiLiGuCanGuFangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuasizhuanenianguItem) {
            WeilianhuasizhuanenianguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof YugougucanfangItem) {
            ShiJunZhiLiGuCanGuFangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof BianhuadaoxindeputongweicanwuItem) {
            CanPoHuoYaoJinJingYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuasixinglifangguItem) {
            WeilianhuasixinglifangguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaXieQiGuItem) {
            WeiLianHuaXieQiGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof TunyueguItem) {
            TunyueguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof TianPengGuItem) {
            TianPengGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaShuiLaoGuItem) {
            WeiLianHuaShuiLaoGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof HunJinGunGuItem) {
            HunJinGunGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuayujianguItem) {
            WeilianhuayujianguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaXieLuGu5Item) {
            WeiLianHuaXieLuGu5YouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaYuanLaoGu1Item) {
            WeiLianHuaYuanLaoGu1YouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuaxuezhanguItem) {
            WeilianhuaxuezhanguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof MoDiGuItem) {
            MoDiGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc, stack);
            return true;
        }
        if (stack.getItem() instanceof Gudaoxindezhunzongshi1Item) {
            CanPoHuoYaoJinJingYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc);
            return true;
        }
        if (stack.getItem() instanceof MudaoxindezhunzongshiweicanwuItem) {
            CanPoHuoYaoJinJingYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaYuYuGu4Item) {
            WeiLianHuaYuYuGu4YouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof TieGuGuItem) {
            TieGuGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof BianhuadaoxindezhunzongshiweicanwuItem) {
            CanPoHuoYaoJinJingYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc);
            return true;
        }
        if (stack.getItem() instanceof JianfengguItem) {
            JianfengguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof XieCangGongItem) {
            XieCangGongYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(npc, stack);
            return true;
        }
        if (stack.getItem() instanceof FanShuiGuCanGuFangItem) {
            ShiJunZhiLiGuCanGuFangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof YuLingGu5Item) {
            YuLingGu5YouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof YuQuanGu4Item) {
            YuQuanGu4YouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof CanpoleiyongItem) {
            CanPoHuoYaoJinJingYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc);
            return true;
        }
        if (stack.getItem() instanceof YuzangjianguangucanfangItem) {
            ShiJunZhiLiGuCanGuFangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof NuMuJinGangGuItem) {
            NuMuJinGangGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof Chanchengzuobiao2Item) {
            Chanchengzuobiao2YouJiKongQiShiShiTiDeWeiZhiProcedure.execute(npc);
            return true;
        }
        if (stack.getItem() instanceof BeiShuiYiZhanGuItem) {
            BeiShuiYiZhanGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof ShuiJiaGu4Item) {
            ShuiJiaGu4YouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuashexinguItem) {
            WeilianhuashexinguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof TipoguItem) {
            TipoguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof SanzhuanzhanyiguItem) {
            SanzhuanzhanyiguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof ShuoyuguItem) {
            ShuoyuguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof HanYueGuItem) {
            HanYueGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof ZuanshiguItem) {
            ZuanshiguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuajintangguItem) {
            WeilianhuajintangguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaJunHaoGu5Item) {
            WeiLianHuaJunHaoGu5YouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof XuebaogucanfangItem) {
            ShiJunZhiLiGuCanGuFangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof ENianGuItem) {
            ENianGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof JianZhiGu3Item) {
            JianZhiGu3YouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaShuangJianGuItem) {
            WeiLianHuaShuangJianGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof NuMuXieYanGuCanGuFangItem) {
            ShiJunZhiLiGuCanGuFangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuajinfeiguItem) {
            WeilianhuajinfeiguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof TaiguangguItem) {
            TaiguangguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof JiTuGuItem) {
            JiTuGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaShuiGuangGuItem) {
            WeiLianHuaShuiGuangGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaXieZhuiGuItem) {
            WeiLianHuaXieZhuiGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaShuangQiaoHuoLuGuItem) {
            WeiLianHuaShuangQiaoHuoLuGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof GuyiguItem) {
            GuyiguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof XionghunguItem) {
            XionghunguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof JinxiagucanfangItem) {
            ShiJunZhiLiGuCanGuFangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof ErzhansuijiguItem) {
            ErzhansuijiguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuadoukongguItem) {
            WeilianhuadoukongguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaQiXiangJiuChongItem) {
            WeiLianHuaQiXiangJiuChongDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuatianyuanbaojunlianItem) {
            WeilianhuatianyuanbaojunlianYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaYuPiGuItem) {
            WeiLianHuaYuPiGuDangWuPinZaiBeiBaoZhongShiMeiKeFaShengProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof XindeliandaoputongweicanwuItem) {
            CanPoHuoYaoJinJingYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaYuXiongGu2Item) {
            WeiLianHuaYuXiongGu2YouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaLeGuDunItem) {
            WeiLianHuaLeGuDunDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof ShuangJianGuItem) {
            ShuangJianGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuaguroutuanyuanguItem) {
            WeilianhuaguroutuanyuanguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof LingXianGuItem) {
            LingXianGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof CanpobingrenfengbaoItem) {
            CanPoHuoYaoJinJingYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaYuQuanGu5Item) {
            WeiLianHuaYuQuanGu5YouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof QuXieDaoItem) {
            QuXieDaoYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WelianhuashishenguItem) {
            WelianhuashishenguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof YuLangGu2Item) {
            YuLangGu2YouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuadiejieguItem) {
            WeilianhuadiejieguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaGuangZhaGuItem) {
            WeiLianHuaGuangZhaGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof HuangJinJiaGuCanGuFangItem) {
            ShiJunZhiLiGuCanGuFangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaZiJinSheLiGuItem) {
            WeiLianHuaZiJinSheLiGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof JiansuoguwuzhuanItem) {
            JiansuoguwuzhuanYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaXieDiGuItem) {
            WeiLianHuaXieDiGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof LieyanxiongliguItem) {
            LieyanxiongliguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof XindebingxuedaozhundashijiweicanwuItem) {
            CanPoHuoYaoJinJingYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc);
            return true;
        }
        if (stack.getItem() instanceof XindehundaodashijiweicanwuItem) {
            CanPoHuoYaoJinJingYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc);
            return true;
        }
        if (stack.getItem() instanceof JunHaoGu5Item) {
            JunHaoGu5YouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof JianrenguItem) {
            JianrenguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaYuLingGu5Item) {
            WeiLianHuaYuLingGu5YouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof SizhuanfengligucanfangItem) {
            ShiJunZhiLiGuCanGuFangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuacanfengguItem) {
            WeilianhuacanfengguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuaxiaoguangguItem) {
            WeilianhuaxiaoguangguDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof MengyanshiItem) {
            MengyanshiYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaHuanYingYueItem) {
            WeiLianHuaHuanYingYueDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof ShengJiXieItem) {
            SshengjiyeProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof XueRenGuItem) {
            XueRenGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuasanzhuanshexinguItem) {
            WeilianhuasanzhuanshexinguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof XindexuedaozhunzongshiyicanwuItem) {
            Xindexuedaozhunzongshi2Procedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof YuanqigucanfangItem) {
            ShiJunZhiLiGuCanGuFangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaYuLangGuItem) {
            WeiLianHuaYuLangGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaGuiYanGuItem) {
            WeiLianHuaGuiYanGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof XingBanDianGuItem) {
            XingBanDianGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaLingHuGuItem) {
            WeiLianHuaLingHuGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaJianYingGuItem) {
            WeiLianHuaJianYingGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuatiekehuazhongguItem) {
            WeilianhuatiekehuazhongguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof YunJianQingLianItem) {
            YunJianQingLianYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof BaiGuCheLunGuCanGuFangItem) {
            ShiJunZhiLiGuCanGuFangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof HuoGuItem) {
            HuoGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof Jianzhi2Item) {
            JianzhiYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaJiuXieShengJiCaoItem) {
            WeiLianHuaJiuXieShengJiCaoDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof XiangyabaijiaguItem) {
            XiangyabaijiaguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaBaiXiangSheGuItem) {
            WeiLianHuaBaiXiangSheGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuajinlvyiguItem) {
            WeilianhuajinlvyiguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof DahunguItem) {
            DahunguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaHuoYouGuItem) {
            WeiLianHuaHuoYouGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaGuQiangGuItem) {
            WeiLianHuaGuQiangGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof QianqigucanpoItem) {
            ShiJunZhiLiGuCanGuFangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof XingguangguItem) {
            XingguangguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaYuHuGu2Item) {
            WeiLianHuaYuHuGu2YouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuajinhenguItem) {
            WeilianhuajinhenguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof JunHaoGu3Item) {
            JunHaoGu3YouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof GuQiangItem) {
            GuQiangDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof YuanqiguItem) {
            YuanqiguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof DunyuguItem) {
            DunyuguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuaqiejieguItem) {
            WeilianhuaqiejieguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuashuchongItem) {
            WeilianhuashuchongYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaShuiMuTianHuaGuItem) {
            WeiLianHuaShuiMuTianHuaGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof ShuiKeGuItem) {
            ShuiKeGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof JiuNangHuaGuItem) {
            JiuNangHuaGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaQingTongSheLiGuItem) {
            WeiLianHuaQingTongSheLiGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof DingItem) {
            DingYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(npc);
            return true;
        }
        if (stack.getItem() instanceof YuYingGuItem) {
            YuYingGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WuXingLianZhuGuCanGuFangItem) {
            ShiJunZhiLiGuCanGuFangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaYuHuGu5Item) {
            WeiLianHuaYuHuGu5YouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuayangshengguItem) {
            WeilianhuayangshengguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof XuLiGuItem) {
            XuLiGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuachikongguItem) {
            WeilianhuachikongguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof ShuipuguwanchengItem) {
            ShiJunZhiLiGuCanGuFangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof WeilianhualanghunguItem) {
            WeilianhualanghunguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof BianhuadaoxindezhunzongshiyicanwuItem) {
            Bianhuadaoxindezhunzongshi2Procedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof SizhuanyushoulinggucanfangItem) {
            ShiJunZhiLiGuCanGuFangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof ShuiWanGuItem) {
            ShuiWanGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof YuLangGu4Item) {
            YuLangGu4YouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaJunZhiLiGuItem) {
            WeiLianHuaJunZhiLiGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuabingwenshiItem) {
            WeilianhuabingwenshiYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuagongbeiguItem) {
            WeilianhuagongbeiguDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuaxiuluoshiItem) {
            WeilianhuaxiuluoshiYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaRiGuItem) {
            WeiLianHuaRiGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof JiShuiGuItem) {
            JiShuiGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeilianhualiuxingtianzuiItem) {
            WeilianhualiuxingtianzuiYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaSuanShuiGuItem) {
            WeiLianHuaSuanShuiGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof XindejiandaodashiweicanwuItem) {
            CanPoHuoYaoJinJingYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc);
            return true;
        }
        if (stack.getItem() instanceof Baiyugu2Item) {
            Baiyugu2YouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof YangjianhucanfangItem) {
            ShiJunZhiLiGuCanGuFangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof JianshenggucanfangItem) {
            ShiJunZhiLiGuCanGuFangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaShiGuiFuLiGuItem) {
            WeiLianHuaShiGuiFuLiGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof TengyungucanpoItem) {
            ShiJunZhiLiGuCanGuFangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof SongGuGuCanGuFangItem) {
            ShiJunZhiLiGuCanGuFangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof JinTangGuCanGuFangItem) {
            ShiJunZhiLiGuCanGuFangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof QiGaiEItem) {
            QiGaiEDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof BaDaoZhanItem) {
            BaDaoZhanYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof CanyuexueyigucanfangItem) {
            ShiJunZhiLiGuCanGuFangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof CanpotongtoutiebiItem) {
            CanPoHuoYaoJinJingYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaJianLiaoGuItem) {
            WeiLianHuaJianLiaoGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuayueguItem) {
            WeilianhuayueguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof JianjiguItem) {
            JianjiguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof DianJinGuItem) {
            DianJinGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaZhiLuGu5Item) {
            WeiLianHuaZhiLuGu5YouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof NuMuJinGangGuCanGuFangItem) {
            ShiJunZhiLiGuCanGuFangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuafengrenguItem) {
            WeilianhuafengrenguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof XindeshuidaozhunzongshiItem) {
            CanPoHuoYaoJinJingYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc);
            return true;
        }
        if (stack.getItem() instanceof JianshangguItem) {
            JianshangguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuasanzhuanyumaoguItem) {
            WeilianhuasanzhuanyumaoguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuajinwenjianxiaguItem) {
            WeilianhuajinwenjianxiaguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof Nifengzhangu4Item) {
            Nifengzhangu4YouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuajiansuoguwuzhuanItem) {
            WeilianhuajiansuoguwuzhuanYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof XiongTuGuCanGuFangItem) {
            ShiJunZhiLiGuCanGuFangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuatianjiguItem) {
            WeilianhuatianjiguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof JianxiaguItem) {
            JianxiaguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof ErzhuanzhanyiguItem) {
            ErzhuanzhanyiguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaShuiTiGuItem) {
            WeiLianHuaShuiTiGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WuzhuanjianjiaogugufangcanpoItem) {
            ShiJunZhiLiGuCanGuFangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof GuangHongGuItem) {
            GuangHongGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaShuiKeGuItem) {
            WeiLianHuaShuiKeGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuaxingguangguItem) {
            WeilianhuaxingguangguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuaduochongjianyingItem) {
            WeilianhuaduochongjianyingYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof TianjiguItem) {
            TianjiguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof ChiMaJunLiGuItem) {
            ChiMaJunLiGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaHongShuiGuItem) {
            WeiLianHuaHongShuiGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaManLiTianNiuGuItem) {
            WeiLianHuaManLiTianNiuGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaXieYanGuItem) {
            WeiLianHuaXieYanGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuajuchifengrenguItem) {
            WeilianhuajuchifengrenguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof JianmaiguItem) {
            JianmaiguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof KuligucanpoItem) {
            ShiJunZhiLiGuCanGuFangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaDuHeGuItem) {
            WeiLianHuaDuHeGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof YouYingSuiXingGuItem) {
            YouYingSuiXingGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof ShuiHuGu5Item) {
            ShuiHuGu5YouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof YuegucanfangItem) {
            ShiJunZhiLiGuCanGuFangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaYuLangGu5Item) {
            WeiLianHuaYuLangGu4YouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof YdItem) {
            YdDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuajianyuguItem) {
            WeilianhuajianyuguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaHeiZongGuItem) {
            WeiLianHuaHeiZongGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuabifenglunguItem) {
            WeilianhuabifenglunguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof YangshengguItem) {
            YangshengguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof ZhanjuguItem) {
            ZhanjuguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof YuYingGu4Item) {
            YuYingGu4YouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof XindejindaodashiweicanwuItem) {
            CanPoHuoYaoJinJingYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc);
            return true;
        }
        if (stack.getItem() instanceof BianhuadaoxindeyicanwuItem) {
            Bianhuadaoxinde2Procedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof XindeliandaoputongyicanwuItem) {
            XindeliandaoputongyicanwuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaZhuiFengGuItem) {
            WeiLianHuaZhuiFengGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof JinlonggucanfangItem) {
            ShiJunZhiLiGuCanGuFangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof SizhuanyumaoguItem) {
            SizhuanyumaoguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof KongBaiGuFangItem) {
            KongBaiGuFangDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuaTianYuanBaoLianItem) {
            WeilianhuaTianYuanBaoLianDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof HuachunnigucanfangItem) {
            ShiJunZhiLiGuCanGuFangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof ShiSuoGuCanGuFangItem) {
            ShiJunZhiLiGuCanGuFangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaLangTuGuItem) {
            WeiLianHuaLangTuGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaXueShouYinGuItem) {
            WeiLianHuaXueShouYinGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof BaDaoZhanGu4Item) {
            BaDaoZhanGu4YouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof LieZhuaItem) {
            LieZhuaYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(npc, stack);
            return true;
        }
        if (stack.getItem() instanceof BufengguItem) {
            BufengguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof RouBaiGuItem) {
            RouBaiGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof ShiShuiGuItem) {
            ShiShuiGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaLongWanQuQuGuItem) {
            WeiLianHuaLongWanQuQuGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuayuzenguItem) {
            WeilianhuayuzenguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof JianmuguItem) {
            JianmuguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuahuachunniItem) {
            WeilianhuahuachunniYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof MudaoxindedashiyicanwuItem) {
            Mudaoxindedashi2Procedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof XindejiandaozhundashijiyicanwuItem) {
            XindejiandaozhundashijiyicanwuWuPinZaiWuPinLanShiMeiKeFaShengProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof TunDuWaGuItem) {
            TunDuWaGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof LeichuiguItem) {
            LeichuiguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof QuanTuGuItem) {
            QuanTuGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof YubengguItem) {
            YubengguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaYuQuanGu3Item) {
            WeiLianHuaYuQuanGu3YouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaLieZhuaGuItem) {
            WeiLianHuaLieZhuaGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuashimuyiItem) {
            WeilianhuazhangyanguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof FengrenguItem) {
            FengrenguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof ChongZhiGuItem) {
            ChongZhiGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof ZhanyiguItem) {
            ZhanyiguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof YanchiguItem) {
            YanchiguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof QijieguItem) {
            QijieguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof XindeshuidaoputongyicanwuItem) {
            XindeshuidaoputongyicanwuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof XueHeMangGuItem) {
            XueHeMangGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof CanpoxijuanbafangItem) {
            CanPoHuoYaoJinJingYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc);
            return true;
        }
        if (stack.getItem() instanceof BingJiGuItem) {
            BingJiGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeilianhualuohuaguItem) {
            WeilianhualuohuaguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof JingyueguItem) {
            JingyueguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaBaiShiGuItem) {
            WeiLianHuaBaiShiGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaKongNianGuItem) {
            WeiLianHuaKongNianGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaJiuNangHuaGuItem) {
            WeiLianHuaJiuNangHuaGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof TieZhuaYingLiGuItem) {
            TieZhuaYingLiGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof ShijunzhiliguItem) {
            ShijunzhiliguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof ZiJinSheLiGuItem) {
            ZiJinSheLiGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaNuMuJinGangGuItem) {
            WeiLianHuaNuMuJinGangGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuaxiaozhiguItem) {
            WeilianhuaxiaozhiguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuajianyinguItem) {
            WeilianhuajianyinguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof ShuiyueguItem) {
            ShuiyueguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof SanzhuanyumaoguItem) {
            SanzhuanyumaoguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof KuangfengguItem) {
            KuangfengguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaShuiJiaGu5Item) {
            WeiLianHuaShuiJiaGu5YouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuadandaoguItem) {
            WeilianhuadandaoguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof GuroutuanyuangucanpoItem) {
            ShiJunZhiLiGuCanGuFangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaYuYingGu5Item) {
            WeiLianHuaYuYingGu5YouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaQianShouShiXinGuItem) {
            WeiLianHuaQianShouShiXinGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaXueMuTianHuaGuItem) {
            WeiLianHuaXueMuTianHuaGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuajingqiguItem) {
            WeilianhuajingqiguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeilianhualiejianguItem) {
            WeilianhualiejianguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof PenXieGuItem) {
            PenXieGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof YuYuGu5Item) {
            YuYuGu5YouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof XueDiZiGuCanGuFangItem) {
            ShiJunZhiLiGuCanGuFangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof XindebingxuedaozhunzongshijiyicanwuItem) {
            XindebingxuedaozhunzongshijiyicanwuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof YueHenGuItem) {
            YueHenGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaWoYiGuItem) {
            WeiLianHuaWoYiGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof XindexuedaodashiweicanwuItem) {
            CanPoHuoYaoJinJingYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc);
            return true;
        }
        if (stack.getItem() instanceof XindelianzhunzongshijiweicanwuItem) {
            CanPoHuoYaoJinJingYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc);
            return true;
        }
        if (stack.getItem() instanceof JingjietishengItem) {
            JingjiedaohentishengProcedure.execute(npc);
            return true;
        }
        if (stack.getItem() instanceof GuroutuanyuanguItem) {
            GuroutuanyuanguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof JinfengsongshuangguItem) {
            JinfengsongshuangguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuazhangyanguItem) {
            WeilianhuazhangyanguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuayunshouxueguItem) {
            WeilianhuayunshouxueguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaShuiJianGuItem) {
            WeiLianHuaShuiJianGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof YuXiongGu3Item) {
            YuXiongGu3YouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof XindeyandaodashiItem) {
            CanPoHuoYaoJinJingYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc);
            return true;
        }
        if (stack.getItem() instanceof TunduwacanfangItem) {
            ShiJunZhiLiGuCanGuFangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuaxueshuguItem) {
            WeilianhuaxueshuguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuajieyunguItem) {
            WeilianhuajieyunguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuaqixingjianxiaguItem) {
            WeilianhuaqixingjianxiaguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof LeiChuiGuCanGuFangItem) {
            ShiJunZhiLiGuCanGuFangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof MudaoxindeputongweicanwuItem) {
            CanPoHuoYaoJinJingYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaHuangJinSheLiGuItem) {
            WeiLianHuaHuangJinSheLiGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof SizhuanzhanyigucanfangItem) {
            ShiJunZhiLiGuCanGuFangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof XindeshuidaozhunzongshishiyicanwuItem) {
            XindeshuidaozhunzongshishiyicanwuWuPinZaiWuPinLanShiMeiKeFaShengProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof CunGuangYinItem) {
            CunGuangYinDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof YuQuanGu2Item) {
            YuQuanGu2YouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof TengyunguItem) {
            TengyunguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuaximoguItem) {
            WeilianhuaximoguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof XueShouYinGuCanGuFangItem) {
            ShiJunZhiLiGuCanGuFangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof HuaXieLuGuItem) {
            HuaXieLuGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeilianhualishiguItem) {
            WeilianhualishiguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof CanpoqingpogujuanItem) {
            CanPoHuoYaoJinJingYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc);
            return true;
        }
        if (stack.getItem() instanceof LanNiaoBingGuanGuItem) {
            LanNiaoBingGuanGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof SizhuanyulangucanfangItem) {
            ShiJunZhiLiGuCanGuFangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof BaiGuiYeXingGuCanGuFangItem) {
            ShiJunZhiLiGuCanGuFangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaChongZhiGuItem) {
            WeiLianHuaChongZhiGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaXieKuangZhenFengGuItem) {
            WeiLianHuaXieKuangZhenFengGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof JjianshouItem) {
            JjianshouYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(npc);
            return true;
        }
        if (stack.getItem() instanceof CanpodangjianbafangcanpoItem) {
            CanPoHuoYaoJinJingYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc);
            return true;
        }
        if (stack.getItem() instanceof XindehundaoputongjiyicanwuItem) {
            XindehundaoputongjiyicanwuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaDaoGuangGuItem) {
            WeiLianHuaDaoGuangGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof ENianGu5Item) {
            ENianGu5YouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof ShengJieItem) {
            ShengJieDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(npc);
            return true;
        }
        if (stack.getItem() instanceof FeishazoushiguItem) {
            FeishazoushiguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaYuYingGu4Item) {
            WeiLianHuaYuYingGu4YouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuaqingfengchengyuanguItem) {
            WeilianhuaqingfengchengyuanguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaHengChongGuItem) {
            WeiLianHuaHengChongGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof XuezhanguItem) {
            XuezhanguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof YunshouxueguItem) {
            YunshouxueguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof TupiguItem) {
            TupiguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof ManLiTianNiuGuItem) {
            ManLiTianNiuGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof JianYuYiGuCanGuFangItem) {
            ShiJunZhiLiGuCanGuFangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof SizhuanjiansuogugfangcanpoItem) {
            ShiJunZhiLiGuCanGuFangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuagongbeigusizhuanItem) {
            WeilianhuagongbeigusizhuanYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuaqisirenItem) {
            WeilianhuaqisirenYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof SizhuanenianguItem) {
            SizhuanenianguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof ShiShuiGuCanGuFangItem) {
            ShiJunZhiLiGuCanGuFangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaQunLiGuItem) {
            WeiLianHuaQunLiGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof ChixinjinweiguItem) {
            ChixinjinweiguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WuzhuanyumaoguItem) {
            WuzhuanyumaoguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof QixingjianxiaguItem) {
            QixingjianxiaguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof LvyaogucanfangItem) {
            ShiJunZhiLiGuCanGuFangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuapokongguItem) {
            WeilianhuapokongguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof DangjiangucanfangItem) {
            ShiJunZhiLiGuCanGuFangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof ShuchongItem) {
            ShuchongYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof YuLingGu5CanGuFangItem) {
            ShiJunZhiLiGuCanGuFangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof XindeleidaodashiyicanwuItem) {
            XindeleidaodashiyicanwuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof YuYingGu2Item) {
            YuYingGu2YouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuajinxiaguItem) {
            WeilianhuajinxiaguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuayangjianguItem) {
            WeilianhuayangjianguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof JianzhiItem) {
            JianzhiYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaXunDianLiuGuangGuItem) {
            WeiLianHuaXunDianLiuGuangGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof JuChiJinWuGuItem) {
            JuChiJinWuGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof XindejindaozhundashiyicanwuItem) {
            XindejindaozhundashiyicanwuWuPinZaiWuPinLanShiMeiKeFaShengProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof XindebingxuedaodashijiyicanwuItem) {
            XindebingxuedaodashijiyicanwuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof LiZhanXueQiGuItem) {
            LiZhanXueQiGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof XindeyandaoputongItem) {
            CanPoHuoYaoJinJingYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc);
            return true;
        }
        if (stack.getItem() instanceof HuangLuoTianNiuGuItem) {
            HuangLuoTianNiuGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof HenShiGuItem) {
            HenShiGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaShiQiaoGuItem) {
            WeiLianHuaShiQiaoGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuaxueyiguItem) {
            WeilianhuaxueyiguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaShouPiGuItem) {
            WeiLianHuaShouPiGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaShuiBiGuItem) {
            WeiLianHuaShuiBiGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof DikuishiItem) {
            DikuishiYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaYuXiongGu3Item) {
            WeiLianHuaYuXiongGu3YouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof ChikongguItem) {
            ChikongguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof GongBeiGu3Item) {
            GongBeiGu3DangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof SheXinGu5CanGuFangItem) {
            ShiJunZhiLiGuCanGuFangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaYueNiGuItem) {
            WeiLianHuaYueNiGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof QingReGuItem) {
            QingReGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof ChouPiFeiChongItem) {
            ChouPiFeiChongDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaShiShuiGuItem) {
            WeiLianHuaShiShuiGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof ShuiLongGuItem) {
            ShuiLongGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof XunDianLiuGuangGuItem) {
            XunDianLiuGuangGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof YuLinGuItem) {
            YuLinGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof XindeleidaoputongweicanwuItem) {
            CanPoHuoYaoJinJingYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc);
            return true;
        }
        if (stack.getItem() instanceof XueShouYinGuItem) {
            XueShouYinGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaJunHaoGu4Item) {
            WeiLianHuaJunHaoGu4YouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuasizhuanshexinguItem) {
            WeilianhuasizhuanshexinguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof YuShuGuItem) {
            YuShuGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof LvyaoguItem) {
            LvyaoguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof LinJiaGuItem) {
            LinJiaGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof JingyuegucanfangItem) {
            ShiJunZhiLiGuCanGuFangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof YanZhouGuItem) {
            YanZhouGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof HehungucanfangItem) {
            ShiJunZhiLiGuCanGuFangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof WeilianhualeichuiguItem) {
            WeilianhualeichuiguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof SizhaunyulinggucnafangItem) {
            SizhaunyulinggucnafangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof XindelidaodashiweicanwuItem) {
            CanPoHuoYaoJinJingYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc);
            return true;
        }
        if (stack.getItem() instanceof JunHaoGuItem) {
            JunHaoGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaLingXianGuItem) {
            WeiLianHuaLingXianGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof YoulongguItem) {
            YoulongguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianBaiShouShiRouGuItem) {
            WeiLianBaiShouShiRouGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuabaiguiyexingguItem) {
            WeilianhuabaiguiyexingguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof XieZhuiGuItem) {
            XieZhuiGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaRouBaiGuItem) {
            WeiLianHuaRouBaiGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof XunDianLiuGuangGuCanGuFangItem) {
            ShiJunZhiLiGuCanGuFangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuaerzhuanyumaoguItem) {
            WeilianhuaerzhuanyumaoguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaLuoXuanGuQiangGuItem) {
            WeiLianHuaLuoXuanGuQiangGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof HuaShiGuItem) {
            HuaShiGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaSanXingZaiTianGuItem) {
            WeiLianHuaSanXingZaiTianGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof CanPoSiBiDiWangItem) {
            CanPoHuoYaoJinJingYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc);
            return true;
        }
        if (stack.getItem() instanceof YuXiong5Item) {
            YuXiong5YouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof Xindegudaozhundaoshi1Item) {
            CanPoHuoYaoJinJingYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc);
            return true;
        }
        if (stack.getItem() instanceof ErzhuanenianguItem) {
            ErzhuanenianguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuadazhiguItem) {
            WeilianhuadazhiguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof ShiPiGuItem) {
            ShiPiGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof LiuShuiGuItem) {
            LiuShuiGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof ShengZhuanItem) {
            ShengZhuanDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(npc);
            return true;
        }
        if (stack.getItem() instanceof NuoyiguItem) {
            NuoyiguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof JunHaoGu5CanGuFangItem) {
            ShiJunZhiLiGuCanGuFangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaXieHuLuGuItem) {
            WeiLianHuaXieHuLuGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof TiaojiangguItem) {
            TiaojiangguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof BaiyindushiItem) {
            BaiyindushiDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuajianwenguItem) {
            WeilianhuajianwenguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof CanposhuilanglongxiItem) {
            CanPoHuoYaoJinJingYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc);
            return true;
        }
        if (stack.getItem() instanceof YizhuanyumaoguItem) {
            YizhuanyumaoguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof HundunguItem) {
            HundunguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof XindejiandaozhunzongshiweixideItem) {
            XindejiandaozhunzongshiweixideYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof CanpogangjintieguItem) {
            CanPoHuoYaoJinJingYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaHeHunGuItem) {
            WeiLianHuaHeHunGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof ShouXieQingItem) {
            ShouXieQingYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaTuQiangGuItem) {
            WeiLianHuaTuQiangGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof YuYingGu5Item) {
            YuYingGu5YouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof YewuhenweizhizuobiaoItem) {
            YewuhenweizhizuobiaoYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(npc);
            return true;
        }
        if (stack.getItem() instanceof YangjianhuItem) {
            YangjianhuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof HupogugucanfangItem) {
            ShiJunZhiLiGuCanGuFangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof XindejiandaodashicanwuItem) {
            XindejiandaodashicanwuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof SizhuanquanliyifucanfangItem) {
            ShiJunZhiLiGuCanGuFangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuwuchangguguItem) {
            WeilianhuwuchangguguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaHeiShiGuItem) {
            WeiLianHuaHeiShiGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof XindeyuedaozhundashiyicanwuItem) {
            XindeyuedaozhundashiyicanwuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof ShuiZuanItem) {
            ShuiZuanDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof CanpobaibengliejieItem) {
            CanPoHuoYaoJinJingYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaXieLuoGuItem) {
            WeiLianHuaXieLuoGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuadianliuguItem) {
            WeilianhuadianliuguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof Xindedashiji1Item) {
            CanPoHuoYaoJinJingYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc);
            return true;
        }
        if (stack.getItem() instanceof ZouXueGuItem) {
            ZouXueGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof ShexinguItem) {
            ShexinguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof KongNianGuItem) {
            KongNianGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof JinzhongguItem) {
            JinzhongguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaChouPiFeiChongItem) {
            WeiLianHuaChouPiFeiChongDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof TongPiGuSanZhuanItem) {
            TongPiGuSanZhuanDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof ZiligengshenggusizhuanItem) {
            ZiligengshenggusizhuanYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof SizhuanyuqunagucanfangItem) {
            SizhuanyuqunagucanfangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaFenShenGuItem) {
            WeiLianHuaFenShenGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof XiaoguangguItem) {
            XiaoguangguDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaLingGuangYiShanGuItem) {
            WeiLianHuaLingGuangYiShanGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof XindehundaozhunzongshiyicanwuItem) {
            XindehundaozhunzongshiyicanwuWuPinZaiWuPinLanShiMeiKeFaShengProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof JingqiguItem) {
            JingqiguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuahulongguItem) {
            WeilianhuahulongguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof GuiYanGuItem) {
            GuiYanGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuayunfengkuiguItem) {
            WeilianhuayunfengkuiguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuasizhaunzhanyiguItem) {
            WeilianhuasizhaunzhanyiguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof BiGuYiGuItem) {
            BiGuYiGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof ChuanchengzuobiaodaojuItem) {
            ChuanchengzuobiaodaojuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(npc);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaHunJinGunGuItem) {
            WeiLianHuaHunJinGunGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof JinfengsongshuanggucanfangItem) {
            ShiJunZhiLiGuCanGuFangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuaguangjinguItem) {
            WeilianhuaguangjinguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof YuYuGu2Item) {
            YuYuGu2YouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuadaliguItem) {
            WeilianhuadaliguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof JieLiGuItem) {
            JieLiGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuadusongzhenguItem) {
            WeilianhuadusongzhenguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuabaonaoguItem) {
            WeilianhuabaonaoguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof HengchongzhizhuangguItem) {
            HengchongzhizhuangguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof XindeguangdaozunzongshijiyicanwuItem) {
            XindeguangdaozunzongshijiyicanwuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaHuTuGuItem) {
            WeiLianHuaHuTuGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuajianjiaoguwuzhuanItem) {
            WeilianhuajianjiaoguwuzhuanYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof HupiguItem) {
            HupiguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaZhiLuGu4Item) {
            WeiLianHuaZhiLuGu4YouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof ShuiLianGuItem) {
            ShuiLianGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof XindelidaozhundashiyicanwuItem) {
            Xindelidaozhundashi2Procedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof XindeguangdaoputongjiyicanwuItem) {
            XindeguangdaoputongjiyicanwuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof XindeleidaozhunzongshiweicanwuItem) {
            CanPoHuoYaoJinJingYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc);
            return true;
        }
        if (stack.getItem() instanceof YuPiGuItem) {
            YuPiGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaYuYuGu2Item) {
            WeiLianHuaYuYuGu2YouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof CanPoWanLiJianShenShaZhaoItem) {
            CanPoHuoYaoJinJingYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaYangGuItem) {
            WeiLianHuaYangGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof LingTuGuItem) {
            LingTuGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof XindexuedaoputongweicanfuItem) {
            Xindexuedaoputongxinde2Procedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof CaoqunguItem) {
            CaoqunguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaBingBuGuItem) {
            WeiLianHuaBingBuGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaShiJinLiGuItem) {
            WeiLianHuaShiJinLiGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaHanYueGuItem) {
            WeiLianHuaHanYueGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof HuoYanGuItem) {
            HuoYanGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaChiMaJunLiGuItem) {
            WeiLianHuaChiMaJunLiGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuShouGuItem) {
            WeiLianHuShouGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof DandaogucanfangItem) {
            ShiJunZhiLiGuCanGuFangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof BaDaoZhan4Item) {
            BaDaoZhan4YouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaYuQuanGu2Item) {
            WeiLianHuaYuQuanGu2YouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof XindejiandaozhunzongshiweicanwuItem) {
            CanPoHuoYaoJinJingYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc);
            return true;
        }
        if (stack.getItem() instanceof XindejiandaozhundashijiweicanwuItem) {
            CanPoHuoYaoJinJingYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaJiShuiGuItem) {
            WeiLianHuaJiShuiGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof JunZhiLiGuItem) {
            JunZhiLiGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuaxuejiguItem) {
            WeilianhuaxuejiguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof PokongguItem) {
            PokongguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof QingshanzaiguItem) {
            QingshanzaiguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof BifenglunguItem) {
            BifenglunguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof ShiQiaoGuItem) {
            ShiQiaoGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaRenShouZangShengGuItem) {
            WeiLianHuaRenShouZangShengGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof ZhiliaoguItem) {
            ZhiliaoguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof CanpobingjiyufuItem) {
            CanPoHuoYaoJinJingYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc);
            return true;
        }
        if (stack.getItem() instanceof TiePiGu2Item) {
            TiePiGu2DangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof GanjinguItem) {
            GanjinguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WuzhuanyuanlaogucanfangItem) {
            ShiJunZhiLiGuCanGuFangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaDaDuWa2Item) {
            WeiLianHuaDaDuWa2YouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof GguiyanguItem) {
            GguiyanguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof SanzhuandongchaguItem) {
            SanzhuandongchaguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof RongYanZhaLieGuItem) {
            RongYanZhaLieGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuaxueheguItem) {
            WeilianhuaxueheguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaLeiYiGuItem) {
            WeiLianHuaLeiYiGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof GuyigucanfangItem) {
            ShiJunZhiLiGuCanGuFangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof YuHuGu3Item) {
            YuHuGu3YouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeilianhualiehuoxiongquItem) {
            WeilianhualiehuoxiongquYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaTunDuWaGuItem) {
            WeiLianHuaTunDuWaGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof HunBaoGuItem) {
            HunBaoGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof XindelidaodashiyicanwuItem) {
            Xindelidaodashi2Procedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof XuanFengLunGuItem) {
            XuanFengLunGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof JianXinGuItem) {
            JianXinGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuayuanqieguItem) {
            WeilianhuayuanqieguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaQuanLiYiFuGuItem) {
            WeiLianHuaQuanLiYiFuGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof XindebingxuedaodashijiweicanwuItem) {
            CanPoHuoYaoJinJingYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuashuiyueguItem) {
            WeilianhuashuiyueguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof JianLiaoGuItem) {
            JianLiaoGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof LiangXingHuiYingGuItem) {
            LiangXingHuiYingGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof MantianfeixueguItem) {
            MantianfeixueguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof XingJianGuItem) {
            XingJianGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaWuZuNianItem) {
            WeiLianHuaWuZuNianYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof XindeyuedaozhundashiItem) {
            CanPoHuoYaoJinJingYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaXieCangGongGuItem) {
            WeiLianHuaXieCangGongGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof XinghuoguItem) {
            XinghuoguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof QingfengchengyuanguItem) {
            QingfengchengyuanguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(npc, stack);
            return true;
        }
        if (stack.getItem() instanceof JingtiepogucanfangItem) {
            ShiJunZhiLiGuCanGuFangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof CanfengguItem) {
            CanfengguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof Xindegudaoputong2Item) {
            Xindegudaoputong2liuchengProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof XindejindaozhunzongshiweicanwuItem) {
            CanPoHuoYaoJinJingYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc);
            return true;
        }
        if (stack.getItem() instanceof Xindegudaoputong1Item) {
            CanPoHuoYaoJinJingYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuayubaiguItem) {
            WeilianhuayubaiguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuajiangherixiaguItem) {
            WeilianhuajiangherixiaguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof HupoguItem) {
            HupoguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaYuLingGuItem) {
            WeiLianHuaYuLingGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof QingtengjianyiguItem) {
            QingtengjianyiguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuasizhuanyumaoguItem) {
            WeilianhuasizhuanyumaoguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaBingZhuiGuItem) {
            WeiLianHuaBingZhuiGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof LuohuagucanfangItem) {
            ShiJunZhiLiGuCanGuFangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaTiePiGuItem) {
            WeiLianHuaTiePiGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaZhaLeiGuItem) {
            WeiLianHuaZhaLeiGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof GutongpiguItem) {
            GutongpiguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuaguilianguItem) {
            WeilianhuaguilianguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaZhongShuiGuItem) {
            WeiLianHuaZhongShuiGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof MantianfeixuegucanfangItem) {
            ShiJunZhiLiGuCanGuFangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof SizhuanzhanyiguItem) {
            SizhuanzhanyiguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof HuTuGuCanGuFangItem) {
            ShiJunZhiLiGuCanGuFangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaShuiZuanGuItem) {
            WeiLianHuaShuiZuanGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaSongGuGuItem) {
            WeiLianHuaSongGuGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof HuDuGuItem) {
            HuDuGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof DingkongguItem) {
            DingkongguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof GuZhuGuItem) {
            GuZhuGuDangWuPinZaiBeiBaoZhongShiMeiKeFaShengProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof DandaoguItem) {
            DandaoguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaYouYingSuiXingGuItem) {
            WeiLianHuaYouYingSuiXingGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof YuYuGu4Item) {
            YuYuGu4YouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuadaduwaItem) {
            WeilianhuadaduwaYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuazhanjuguItem) {
            WeilianhuazhanjuguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof ShuishenguItem) {
            ShuishenguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof BingBaoGuItem) {
            BingBaoGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaZhiZhuangGuItem) {
            WeiLianHuaZhiZhuangGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuasizhuanfengliguItem) {
            WeilianhuasizhuanfengliguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuahuhuaguItem) {
            WeilianhuahuhuaguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof JingtieguItem) {
            JingtieguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof ShiGuiFuLiGuItem) {
            ShiGuiFuLiGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof DuHeGuItem) {
            DuHeGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof SizhuaneniangucanfangItem) {
            ShiJunZhiLiGuCanGuFangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof XindebingxuedaoputongjiItem) {
            XindebingxuedaoputongjiYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof QuanpuguItem) {
            QuanpuguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof ZhuiFengGuItem) {
            ZhuiFengGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof YunfengkuiguItem) {
            YunfengkuiguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof LiehuoxiongquItem) {
            LiehuoxiongquYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof XueguishiItem) {
            XueguishiYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof XieNingJianGuCanGuFangItem) {
            ShiJunZhiLiGuCanGuFangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof XueshuguItem) {
            XueshuguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof CanpobingshuanglingyuItem) {
            CanPoHuoYaoJinJingYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaYanLeiGuItem) {
            WeiLianHuaYanLeiGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WoYiGuItem) {
            WoYiGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaZiLiGengShengGuItem) {
            WeiLianHuaZiLiGengShengGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaShuangXiGuItem) {
            WeiLianHuaShuangXiGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof XindeshuidaondashiItem) {
            CanPoHuoYaoJinJingYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc);
            return true;
        }
        if (stack.getItem() instanceof ShuangXiGuItem) {
            ShuangXiGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaXuLiGuItem) {
            WeiLianHuaXuLiGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuamengyanshiItem) {
            WeilianhuamengyanshiYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof XindexuedaozhunzongshiweicanwuItem) {
            CanPoHuoYaoJinJingYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaYuLingGu4Item) {
            WeiLianHuaYuLingGu4YouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuaguijiaoguItem) {
            WeilianhuaguijiaoguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof LingHuGu4GuGuCanFangItem) {
            ShiJunZhiLiGuCanGuFangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof SiWeiJiuChongItem) {
            SiWeiJiuChongDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof JiansuogusizhuanItem) {
            JiansuogusizhuanYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaWuXingLianZhuGuItem) {
            WeiLianHuaWuXingLianZhuGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof ShuipuguItem) {
            ShuipuguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof ChouPiGuItem) {
            ChouPiGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof ShuoyugucanfangItem) {
            ShiJunZhiLiGuCanGuFangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof YuYingGu3Item) {
            YuYingGu3YouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuadafupianpianguItem) {
            WeilianhuadafupianpianguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof HeiZongGuItem) {
            HeiZongGu1XiaoGuoChiXuShiMeiKeFaShengProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof XindehundaozhundashijiyicanwuItem) {
            XindehundaozhundashijiyicanwuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof YuLangGu3Item) {
            YuLangGu3YouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WwupinItem) {
            WwupinDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(npc);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaZouXueGuItem) {
            WeiLianHuaZouXueGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof XindeyandaozhunzongshiyicanwuItem) {
            XindeyandaozhunzongshiyicanwuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof XindeshuidaozhundashiItem) {
            CanPoHuoYaoJinJingYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc);
            return true;
        }
        if (stack.getItem() instanceof YinGUItem) {
            YinGUYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof SongGuGuItem) {
            SongGuGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeilianhualvyaoguItem) {
            WeilianhualvyaoguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof LongWanQuQuGuItem) {
            LongWanQuQuGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaShouWangTunJunGuItem) {
            WeiLianHuaShouWangTunJunGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof ShuiJiGuItem) {
            ShuiJiGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuaguyiguItem) {
            WeilianhuaguyiguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuaxingdunguItem) {
            WeilianhuaxingdunguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof BianhuadaoxindedashiyicanwuItem) {
            Bianhuadaoxindedashi2Procedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof YuLangGu5CanGuFangItem) {
            ShiJunZhiLiGuCanGuFangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof XindeyuedaozhunzongshiItem) {
            CanPoHuoYaoJinJingYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaLuoXuanShuiJianGuItem) {
            WeiLianHuaLuoXuanShuiJianGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof CanPoHuoYaoJinJingItem) {
            CanPoHuoYaoJinJingYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuacaoqunguItem) {
            WeilianhuacaoqunguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaYueGuangGuItem) {
            WeiLianHuaYueGuangGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuachunniguItem) {
            WeilianhuachunniguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaZhiLuGuItem) {
            WeiLianHuaZhiLuGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuayantouguItem) {
            WeilianhuayantouguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof QuanYongMingGuCanGuFuanItem) {
            ShiJunZhiLiGuCanGuFangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof SizhuanfengliguItem) {
            SizhuanfengliguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof JianrengucanfangItem) {
            ShiJunZhiLiGuCanGuFangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuaziyanchanItem) {
            WeilianhuaziyanchanYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof ShengtieguItem) {
            ShengtieguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuatiaojiangguItem) {
            WeilianhuatiaojiangguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof XindeyandaozhundashiItem) {
            CanPoHuoYaoJinJingYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc);
            return true;
        }
        if (stack.getItem() instanceof XindunguItem) {
            XindunguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaGengYunGuItem) {
            WeiLianHuaGengYunGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof XindexuedaozundashiweicanwuItem) {
            CanPoHuoYaoJinJingYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaYuLingGu2Item) {
            WeiLianHuaYuLingGu2YouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuabingpoguItem) {
            WeilianhuabingpoguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof ShuiLaoGuItem) {
            ShuiLaoGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof XingMenZiGuItem) {
            XingMenZiGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof YuQuanGu3Item) {
            YuQuanGu3YouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof FanDaiCaoGuItem) {
            FanDaiCaoGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaXiaoHunGuItem) {
            WeiLianHuaXiaoHunGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof XiongLiGuItem) {
            XiongLiGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof GuijiaoguItem) {
            GuijiaoguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof HuolongguItem) {
            HuolongguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof ShuiHuGuItem) {
            ShuiHuGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuayingyangguItem) {
            WeilianhuayingyangguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof ShiNianShouGuItem) {
            ShiNianShouGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaShuiHuGuItem) {
            WeiLianHuaShuiHuGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof XieWangGuItem) {
            XieWangGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof JianFengGu5Item) {
            JianFengGu5YouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof XueqiguItem) {
            XueqiguDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof LanghunguItem) {
            LanghunguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof HuLiGuCanGuFangItem) {
            ShiJunZhiLiGuCanGuFangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof LuoXuanGuQiangGuItem) {
            LuoXuanGuQiangGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof QuanYongMingGuItem) {
            QuanYongMingGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaDuZhenGuItem) {
            WeiLianHuaDuZhenGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof DahuiguItem) {
            DahuiguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof LingGuangYiShanGuItem) {
            LingGuangYiShanGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof ChixinjinwengucanfangItem) {
            ShiJunZhiLiGuCanGuFangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof QiangShenJianTiGuItem) {
            QiangShenJianTiGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuajiuxingguItem) {
            WeilianhuajiuxingguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof YuanLaoGu5Item) {
            YuanLaoGu5YouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof XieLuGu5Item) {
            XieLuGu5YouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof NuMuXieYanGuItem) {
            NuMuXieYanGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaJiTuGuItem) {
            WeiLianHuaJiTuGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuataiguangguItem) {
            WeilianhuataiguangguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaSheXingGu5Item) {
            WeiLianHuaSheXingGu5YouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof XiaoHunGuItem) {
            XiaoHunGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof JianqiguItem) {
            JianqiguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof YuHuGu5Item) {
            YuHuGu5YouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof XindehundaozhunzongshiweicanwuItem) {
            CanPoHuoYaoJinJingYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaQiGaiEItem) {
            WeiLianHuaQiGaiEDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaYuYuGu5Item) {
            WeiLianHuaYuYuGu5YouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof CanpodaoguangjianyingItem) {
            CanPoHuoYaoJinJingYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuaxuebaoguItem) {
            WeilianhuaxuebaoguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof XindejindaodashiItem) {
            XindejindaodashiYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof ShuangjiangucanfangItem) {
            ShiJunZhiLiGuCanGuFangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaLongJuanFengGuItem) {
            WeiLianHuaLongJuanFengGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof CanposifangyuanqiItem) {
            CanPoHuoYaoJinJingYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc);
            return true;
        }
        if (stack.getItem() instanceof HuorenguItem) {
            HuorenguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuadianwangguItem) {
            WeilianhuadianwangguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof JieligucanfangItem) {
            ShiJunZhiLiGuCanGuFangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof JiezegucanpoItem) {
            ShiJunZhiLiGuCanGuFangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof ZiyanchanItem) {
            ZiyanchanYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof BaonaoguItem) {
            BaonaoguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof CanpoxiuluobianItem) {
            CanPoHuoYaoJinJingYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuahuanyueguItem) {
            WeilianhuahuanyueguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof JiedanguItem) {
            JiedanguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof FeiGuDunGuItem) {
            FeiGuDunGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuajiansuogusizhuanItem) {
            WeilianhuajiansuogusizhuanYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof XindexuedaozundashiyicanwuItem) {
            Xindexuedaozundashi2Procedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof BuxiguItem) {
            BuxiguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaJinZhenGuItem) {
            WeiLianHuaJinZhenGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuayueyinguItem) {
            WeilianhuayueyinguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuatianmoshiItem) {
            WeilianhuatianmoshiYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof YuLingGu2Item) {
            YuLingGu2YouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof XindelidaozhunzongshiyicanwuItem) {
            Xindelidaozhunzongshi2Procedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof CanPoSiBiFengWangItem) {
            CanPoHuoYaoJinJingYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc);
            return true;
        }
        if (stack.getItem() instanceof GucaixianxueItem) {
            GucaixianxueYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaShiSuoGuItem) {
            WeiLianHuaShiSuoGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof HuolonggucanpoItem) {
            ShiJunZhiLiGuCanGuFangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof SizhuannifangzhangucanfangItem) {
            ShiJunZhiLiGuCanGuFangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof DianliuguItem) {
            DianliuguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof HuangJinYueGuItem) {
            HuangJinYueGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaTongPiGusanzhuanItem) {
            WeiLianHuaTongPiGusanzhuanDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuahongbianguItem) {
            WeilianhuahongbianguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof XueLuGu5CanGuFangItem) {
            ShiJunZhiLiGuCanGuFangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof ZhuifenggucanfangItem) {
            ShiJunZhiLiGuCanGuFangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof YujiangucanfangItem) {
            ShiJunZhiLiGuCanGuFangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof TianyuanbaojunliancanfangItem) {
            ShiJunZhiLiGuCanGuFangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaLiangXingHuiYingGuItem) {
            WeiLianHuaLiangXingHuiYingGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuashengtieguItem) {
            WeilianhuashengtieguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaYueDuGuItem) {
            WeiLianHuaYueDuGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaLingTuGuItem) {
            WeiLianHuaLingTuGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof TuBaWangGuItem) {
            TuBaWangGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof DieyingguItem) {
            DieyingguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof DusongzhenguItem) {
            DusongzhenguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaXueShuiGuItem) {
            WeiLianHuaXueShuiGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaXiongShouYinXieGuItem) {
            WeiLianHuaXiongShouYinXieGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaShengJiYeItem) {
            WeiLianHuaShengJiYeDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof JinJingGuItem) {
            JinJingGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuajianjiaoguItem) {
            WeilianhuajianjiaoguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof ShilongqunjiaguItem) {
            ShilongqunjiaguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof HeHunGuItem) {
            HeHunGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaXueDiZiGuItem) {
            WeiLianHuaXueDiZiGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuaquanpoguItem) {
            WeilianhuaquanpoguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof HuanweiguItem) {
            HuanweiguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof MudaoxindezhunzongshiyicanwuItem) {
            Mudaoxindezhunzongshi2Procedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof ShuiJiaGu4CanGuFangItem) {
            ShiJunZhiLiGuCanGuFangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof WeilianhualiandaoguItem) {
            WeilianhualiandaoguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof CanpoliubitianshiwangItem) {
            CanPoHuoYaoJinJingYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaRongYanZhaLieGuItem) {
            WeiLianHuaRongYanZhaLieGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof GongbeigusizhuanItem) {
            GongbeigusizhuanYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaYueShengGuItem) {
            WeiLianHuaYueShengGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof YueNiGuItem) {
            YueNiGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof TiexueguItem) {
            TiexueguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof LiehuoxiongqugucanfangItem) {
            ShiJunZhiLiGuCanGuFangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuamantianfeixueguItem) {
            WeilianhuamantianfeixueguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuaxinghuoguItem) {
            WeilianhuaxinghuoguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuaxiangyaibaijiaguItem) {
            WeilianhuaxiangyaibaijiaguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof YuXiongGu2Item) {
            YuXiongGu2YouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof LuoXuanGuQiangItem) {
            LuoXuanGuQiangDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof YuQuanGu5Item) {
            YuQuanGu5YouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof TTiePiGuItem) {
            TTiePiGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuanifengzhanguItem) {
            WeilianhuanifengzhanguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuashuixiangjiaguItem) {
            WeilianhuashuixiangjiaguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof XindeleidaozhundashiyicanwuItem) {
            XindeleidaozhundashiyicanwuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof BingZhuiGuItem) {
            BingZhuiGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuaweifengguItem) {
            WeilianhuaweifengguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaXingJianGuItem) {
            WeiLianHuaXingJianGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof XuemutianhuagucanfangItem) {
            ShiJunZhiLiGuCanGuFangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaChunYuGuItem) {
            WeiLianHuaChunYuGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof JianwenguItem) {
            JianwenguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuafeishazoushiguItem) {
            WeilianhuafeishazoushiguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof YingyangguItem) {
            YingyangguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaYuShouLingGuItem) {
            WeiLianHuaYuShouLingGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof BeiJiaGuItem) {
            BeiJiaGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaShuiZhaoGuItem) {
            WeiLianHuaShuiZhaoGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof LeidunguItem) {
            LeidunguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaXieFeiGuItem) {
            WeiLianHuaXieFeiGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaCunGuangYinItem) {
            WeiLianHuaCunGuangYinDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof YuShouLingGu4Item) {
            YuShouLingGu4YouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaYuLingGu3Item) {
            WeiLianHuaYuLingGu3YouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaLiuShuiGuItem) {
            WeiLianHuaLiuShuiGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof KongNianGuCanGuFangItem) {
            ShiJunZhiLiGuCanGuFangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof YoulonggucanpoItem) {
            ShiJunZhiLiGuCanGuFangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof ShuangQiaoHuoLuGuItem) {
            ShuangQiaoHuoLuGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof SizhuanshexincanfangItem) {
            ShiJunZhiLiGuCanGuFangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof DoukongguItem) {
            DoukongguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof BaDaoZhan4GuCanGuFangItem) {
            ShiJunZhiLiGuCanGuFangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof XindebingxuedaozhundashijiyicanwuItem) {
            XindebingxuedaozhundashijiyicanwuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaBingRenGuItem) {
            WeiLianHuaBingRenGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaLiaoGuangGuItem) {
            WeiLianHuaLiaoGuangGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof QiangQuGuItem) {
            QiangQuGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof TiePiGu3Item) {
            TiePiGu3DangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaCunGuangYin3Item) {
            WeiLianHuaCunGuangYin3DangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof XindebingxuedaoputongjiweicanwuItem) {
            CanPoHuoYaoJinJingYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuatipoguItem) {
            WeilianhuatipoguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof XieNingJianItem) {
            XieNingJianYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaTieGuGuItem) {
            WeiLianHuaTieGuGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaJianQiaoGuItem) {
            WeiLianHuaJianQiaoGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof TieZhuaYingLiGuCanGuFangItem) {
            ShiJunZhiLiGuCanGuFangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof CanPoXuanLiuHuJingJieItem) {
            CanPoHuoYaoJinJingYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaBaiYinSheLiGuItem) {
            WeiLianHuaBaiYinSheLiGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof ShiJinLiGuItem) {
            ShiJinLiGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof LangTuGuItem) {
            LangTuGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof SuiRenGuItem) {
            SuiRenGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof ZiLiGengShengGu3Item) {
            ZiLiGengShengGu3DangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuaqingshanzaiItem) {
            WeilianhuaqingshanzaiYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof DiejieguItem) {
            DiejieguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof HuhuagucanfangItem) {
            ShiJunZhiLiGuCanGuFangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaYunJianQingLianItem) {
            WeiLianHuaYunJianQingLianYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof YueyinggucanfangItem) {
            ShiJunZhiLiGuCanGuFangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof YinYangZhuanShenGUItem) {
            YinYangZhuanShenGUYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof DieyinggucanfangItem) {
            ShiJunZhiLiGuCanGuFangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaYuShouLingGu3Item) {
            WeiLianHuaYuShouLingGu3YouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaYueMangGuItem) {
            WeiLianHuaYueMangGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaQiangQuGuItem) {
            WeiLianHuaQiangQuGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof DingHunGuCanGuFangItem) {
            ShiJunZhiLiGuCanGuFangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaYinGUItem) {
            WeiLianHuaYinGUYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof JinxiaguItem) {
            JinxiaguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaBeiShuiYiZhanGuItem) {
            WeiLianHuaBeiShuiYiZhanGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof HuahuanyueguItem) {
            HuahuanyueguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof YuQuanGuItem) {
            YuQuanGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaYuJunGuItem) {
            WeiLianHuaYuJunGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof JinlongguItem) {
            JinlongguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuashuoyuguItem) {
            WeilianhuashuoyuguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof YuzangjianguanguItem) {
            YuzangjianguanguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaFanDaiCaoGuItem) {
            WeiLianHuaFanDaiCaoGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuahunfeiguItem) {
            WeilianhuahunfeiguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof ShuiHuGu5CanGuFangItem) {
            ShiJunZhiLiGuCanGuFangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof ShuiShengHuaGuItem) {
            ShuiShengHuaGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaHuangJinYueGuItem) {
            WeiLianHuaHuangJinYueGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuaxueguishiguItem) {
            WeilianhuaxueguishiguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WuwangguItem) {
            WuwangguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof MudaoxindezhundashiweicanwuItem) {
            CanPoHuoYaoJinJingYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc);
            return true;
        }
        if (stack.getItem() instanceof HuanjianyugucanfangItem) {
            ShiJunZhiLiGuCanGuFangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaYuShouLingGu5Item) {
            WeiLianHuaYuShouLingGu5YouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WuZuNianItem) {
            WuZuNianYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof XieFeiGuItem) {
            XieFeiGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof YuYuGu3Item) {
            YuYuGu3YouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof XuelichuanweizhiItem) {
            XuelichuanweizhiYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(npc);
            return true;
        }
        if (stack.getItem() instanceof XueShuiGuItem) {
            XueShuiGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WuzhuanjiansuogugufangcanpiItem) {
            ShiJunZhiLiGuCanGuFangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof HuhuaguItem) {
            HuhuaguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WanshiItem) {
            WanshiDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaYuShouLingGu2Item) {
            WeiLianHuaYuShouLingGu2YouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof BaonaogucanpoItem) {
            ShiJunZhiLiGuCanGuFangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof TiaojianggucanfangItem) {
            ShiJunZhiLiGuCanGuFangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof CanpoyuhuhuxintaItem) {
            CanPoHuoYaoJinJingYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuatunyueguItem) {
            WeilianhuatunyueguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof QuanLiYiFuGu5CanGuFangItem) {
            ShiJunZhiLiGuCanGuFangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaYuYingGu2Item) {
            WeiLianHuaYuYingGu2YouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof JingLiGuItem) {
            JingLiGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof XieNingJianGuItem) {
            XieNingJianGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaYuShuGuItem) {
            WeiLianHuaYuShuGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof ZhaLeiGuItem) {
            ZhaLeiGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuajiezeguItem) {
            WeilianhuajiezeguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof JuchifengrengucanfangItem) {
            ShiJunZhiLiGuCanGuFangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof JiuxingguItem) {
            JiuxingguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof XindeyandaozhunzongshiItem) {
            CanPoHuoYaoJinJingYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaChouPiGuItem) {
            WeiLianHuaChouPiGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof XindeyuedaodashiItem) {
            CanPoHuoYaoJinJingYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc);
            return true;
        }
        if (stack.getItem() instanceof YujiangguItem) {
            YujiangguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuadongchaguItem) {
            WeilianhuadongchaguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof QingtongguItem) {
            QingtongguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof QisirengucanpoItem) {
            ShiJunZhiLiGuCanGuFangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuakurongyouItem) {
            WeilianhuakurongyouYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof XindeliandaozhundashiweicanwuItem) {
            CanPoHuoYaoJinJingYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc);
            return true;
        }
        if (stack.getItem() instanceof BaiYuGuItem) {
            BaiYuGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuatengyunguItem) {
            WeilianhuatengyunguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuawuwangguItem) {
            WeilianhuawuwangguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaJinJingGuItem) {
            WeiLianHuaJinJingGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof ShuiMuTianHuaGuItem) {
            ShuiMuTianHuaGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof LishiguItem) {
            LishiguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof JiuChongItem) {
            JiuChongDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuagongbeiguwuzhuanItem) {
            WeilianhuagongbeiguwuzhuanYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof TongPiGu2Item) {
            TongPiGu2DangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaShuiWanGuItem) {
            WeiLianHuaShuiWanGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof ShimuyiItem) {
            ShimuyiYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaYuYingGu3Item) {
            WeiLianHuaYuYingGu3YouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof HuoGuangZhuTianGuCanGuFangItem) {
            ShiJunZhiLiGuCanGuFangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof YueXuanGuItem) {
            YueXuanGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof ZhiLuGu4CanGuFangItem) {
            ShiJunZhiLiGuCanGuFangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof ChunYuGuItem) {
            ChunYuGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof ShiJunZhiLiGuCanGuFangItem) {
            ShiJunZhiLiGuCanGuFangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof ShuiyuegucanfangItem) {
            ShiJunZhiLiGuCanGuFangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof XindejindaozhundasihiweicanwuItem) {
            CanPoHuoYaoJinJingYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc);
            return true;
        }
        if (stack.getItem() instanceof XindeyandaozhundashiyicasnwuItem) {
            XindeyandaozhundashiyicasnwuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof QuanLiYiFuGu5Item) {
            QuanLiYiFuGu5YouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof XueRenItem) {
            XueRenYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(npc, stack);
            return true;
        }
        if (stack.getItem() instanceof XindelidaozhundashiweicanwuItem) {
            CanPoHuoYaoJinJingYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaYuanLaoGu4Item) {
            WeiLianHuaYuanLaoGu4YouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof SixinglifangtiItem) {
            SixinglifangtiYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof GuangZhaGuItem) {
            GuangZhaGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHunHuoZhuaGuItem) {
            WeiLianHunHuoZhuaGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof DanQiaoHuoTanGuItem) {
            DanQiaoHuoTanGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof SongzhenguItem) {
            SongzhenguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuajiuyanjiuchongItem) {
            WeilianhuajiuyanjiuchongYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof ChunniguItem) {
            ChunniguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof XindejindaozhunzongshiyicanwuItem) {
            XindejindaozhunzongshiyicanwuWuPinZaiWuPinLanShiMeiKeFaShengProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof SuiyiguItem) {
            SuiyiguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaFeiGuDunGuItem) {
            WeiLianHuaFeiGuDunGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaYuGuGuItem) {
            WeiLianHuaYuGuGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof BainianshouguItem) {
            BainianshouguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof ShuiTiGuItem) {
            ShuiTiGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof XuehenguItem) {
            XuehenguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof TunyuegucanfangItem) {
            ShiJunZhiLiGuCanGuFangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof YuhegucanfangItem) {
            ShiJunZhiLiGuCanGuFangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof Weilianhuajieligu2Item) {
            Weilianhuajieligu2YouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaGangZongGuItem) {
            WeiLianHuaGangZongGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaJiaoWeiGuItem) {
            WeiLianHuaJiaoWeiGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof EYuanlaogurzhuanItem) {
            EYuanlaogurzhuanYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof Xindegudaozhundashi2Item) {
            Gudaoxindezhundashiliucheng2Procedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof TianyuanbaowanglianItem) {
            TianyuanbaowanglianYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof ZijingdushiItem) {
            ZijingdushiliuchengProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof BianhuadaoxindezhundashiyicanwuItem) {
            Bianhuadaoxindezhundashi2Procedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaQuanLiYiFuGu5Item) {
            WeiLianHuaQuanLiYiFuGu5YouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof ZhongShuiGuItem) {
            ZhongShuiGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof DafupianpianguItem) {
            DafupianpianguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof CunGuangYin3Item) {
            CunGuangYin3DangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaYuXiong5Item) {
            WeiLianHuaYuXiong5YouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof FudichouxingucanfangItem) {
            ShiJunZhiLiGuCanGuFangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof JianjiaoguwuzhuanItem) {
            JianjiaoguwuzhuanYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof SizhuanyumaocangufangItem) {
            ShiJunZhiLiGuCanGuFangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaJianZhiGu3Item) {
            WeiLianHuaJianZhiGu3YouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof LeiYiGuItem) {
            LeiYiGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaWanShouTunJunGuItem) {
            WeiLianHuaWanShouTunJunGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof MudaoxindeputongyicanwuItem) {
            Mudaoxindeputong2Procedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaXieYueGuItem) {
            WeiLianHuaXieYueGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof YuShouLingGu2Item) {
            YuShouLingGu2YouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof ErzhuanyumaoguItem) {
            ErzhuanyumaoguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof GuCiGuItem) {
            GuCiGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof LiejianguItem) {
            LiejianguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof FenShenGuItem) {
            FenShenGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof ShuiLongGu4CanGuFangItem) {
            ShiJunZhiLiGuCanGuFangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof WeilianhualeidunguItem) {
            WeilianhualeidunguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof XindeshuidaozhundashiyicanwuItem) {
            XindeshuidaozhundashiyicanwuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof TiekehuazhongguItem) {
            TiekehuazhongguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof QingNiuLaoLiGuItem) {
            QingNiuLaoLiGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof XindelidaoputongweicanwuItem) {
            CanPoHuoYaoJinJingYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc);
            return true;
        }
        if (stack.getItem() instanceof YuLangGu5Item) {
            YuLangGu5YouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof SifangxingligucanfangItem) {
            ShiJunZhiLiGuCanGuFangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaHuangLuoTianNiuGuItem) {
            WeiLianHuaHuangLuoTianNiuGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof JuChiJinWuItem) {
            JuChiJinWuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof ShuiZhaoGuItem) {
            ShuiZhaoGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuaguihuoguItem) {
            WeilianhuaguihuoguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof SizhuanjunhaogucanfangItem) {
            ShiJunZhiLiGuCanGuFangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuajianjiguItem) {
            WeilianhuajianjiguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof JiaoWeiGuItem) {
            JiaoWeiGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof GuiqiguItem) {
            GuiqiguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuaerzhuanenianguItem) {
            WeilianhuaerzhuanenianguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof ShuangLinYueGuItem) {
            ShuangLinYueGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof JieyunguItem) {
            JieyunguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof LingTuGuCanGuFangItem) {
            ShiJunZhiLiGuCanGuFangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof WuXingLianZhuGuItem) {
            WuXingLianZhuGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof CanporongyaowuqiangItem) {
            CanPoHuoYaoJinJingYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc);
            return true;
        }
        if (stack.getItem() instanceof XimoguItem) {
            XimoguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof YugouguItem) {
            YugouguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof ZhiZhuangGuItem) {
            ZhiZhuangGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaChunGuangWuXianGuItem) {
            WeiLianHuaChunGuangWuXianGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof DongchaguItem) {
            DongchaguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof JianFengGuCanGuFangItem) {
            ShiJunZhiLiGuCanGuFangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuasanzhuandongchaguItem) {
            WeilianhuasanzhuandongchaguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof CanpojinghongyijiItem) {
            CanPoHuoYaoJinJingYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc);
            return true;
        }
        if (stack.getItem() instanceof XingMenGuItem) {
            XingMenGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuawuzhuanyumaoguItem) {
            WeilianhuawuzhuanyumaoguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof YuHuGu4Item) {
            YuHuGu4YouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaQiangShenJianTiGuItem) {
            WeiLianHuaQiangShenJianTiGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaXieLuGuItem) {
            WeiLianHuaXieLuGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof YanchigucanfangItem) {
            ShiJunZhiLiGuCanGuFangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof ShuiJianGuItem) {
            ShuiJianGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuaxuanwuzhongquItem) {
            WeilianhuaxuanwuzhongquYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaGuCiGuItem) {
            WeiLianHuaGuCiGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaDingHunGuItem) {
            WeiLianHuaDingHunGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof BaiXiangXianSheGuCanGuFangItem) {
            ShiJunZhiLiGuCanGuFangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaJunHaoGu2Item) {
            WeiLianHuaJunHaoGu2YouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof BaiYunGuItem) {
            BaiYunGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaYuYuGu3Item) {
            WeiLianHuaYuYuGu3YouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof JintangguItem) {
            JintangguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof DaBuLiuXingGuItem) {
            DaBuLiuXingGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof CanposhijiezhanyueItem) {
            CanPoHuoYaoJinJingYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc);
            return true;
        }
        if (stack.getItem() instanceof HuanyuegucanfangItem) {
            ShiJunZhiLiGuCanGuFangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuahuarenguItem) {
            WeilianhuahuarenguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof SizhuanGongbeigucanfangItem) {
            ShiJunZhiLiGuCanGuFangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof HuanjindushiItem) {
            HhuanjinsheliguProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuachangqingguItem) {
            WeilianhuachangqingguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof LinghunguItem) {
            LinghunguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuahuangjinguItem) {
            WeilianhuahuangjinguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuahengchongzhizhuangguItem) {
            WeilianhuahengchongzhizhuangguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaXueRenGuItem) {
            WeiLianHuaXueRenGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof YangGuItem) {
            YangGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof MaoJiangGuItem) {
            MaoJiangGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuadikuishiItem) {
            WeilianhuadikuishiYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaPanShiGuItem) {
            WeiLianHuaPanShiGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof GongBeiGu5CanGuFangItem) {
            ShiJunZhiLiGuCanGuFangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaCunGuangYin2Item) {
            WeiLianHuaCunGuangYin2DangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof XieYanGuItem) {
            XieYanGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuajianqiguItem) {
            WeilianhuajianqiguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaBaDaoZhanGuItem) {
            WeiLianHuaBaDaoZhanGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuashanguangguItem) {
            WeilianhuashanguangguDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof KongsuoguItem) {
            KongsuoguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof ShanGuangGuItem) {
            ShanGuangGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof ChiTieSheLiGuItem) {
            ChiTieSheLiGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaBeiJiaGuItem) {
            WeiLianHuaBeiJiaGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaHuoGuangZhuTianGuItem) {
            WeiLianHuaHuoGuangZhuTianGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof BaDaoZhan5GuCanGuFangItem) {
            ShiJunZhiLiGuCanGuFangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof JianrenItem) {
            JianrenYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof RiGuItem) {
            RiGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuabuxiguItem) {
            WeilianhuabuxiguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof ShangfenggucanfangItem) {
            ShiJunZhiLiGuCanGuFangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaYinXueGuItem) {
            WeiLianHuaYinXueGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof JiShuiGuCanGuFangItem) {
            ShiJunZhiLiGuCanGuFangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof XuehuguItem) {
            XuehuguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuaquanliyifugusizhuanItem) {
            WeilianhuaquanliyifugusizhuanYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaShouXieQingItem) {
            WeiLianHuaShouXieQingYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof CanpoyehuoItem) {
            CanPoHuoYaoJinJingYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc);
            return true;
        }
        if (stack.getItem() instanceof JinweijianxiaguItem) {
            JinweijianxiaguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof CaopozangfurunxueItem) {
            CanPoHuoYaoJinJingYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc);
            return true;
        }
        if (stack.getItem() instanceof YizhuanrendaoxiwangguItem) {
            YizhuanrendaoxiwangguDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof XiongTuGuItem) {
            XiongTuGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof JianjiaoguItem) {
            JianjiaoguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof SizhuanyuxionggucanfangItem) {
            SizhuanyuxionggucanfangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof XieYueGuItem) {
            XieYueGuDangWuPinZaiBeiBaoZhongShiMeiKeFaShengProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof CunGuangYin2Item) {
            CunGuangYin2DangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaYuXiongGuItem) {
            WeiLianHuaYuXiongGuYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuayanchiguItem) {
            WeilianhuayanchiguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof XuebaoguItem) {
            XuebaoguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaShiNianShouGuItem) {
            WeiLianHuaShiNianShouGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuayubenguItem) {
            WeilianhuayubenguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeiLianHuaAiBieChiItem) {
            WeiLianHuaAiBieChiDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeilianhualeidianlangliguItem) {
            WeilianhualeidianlangliguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof WeilianhuadianyanguItem) {
            WeilianhuadianyanguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof ChiTieDuShiItem) {
            ChiTieDuShiDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof LiuxingtianzhuiguItem) {
            LiuxingtianzhuiguYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc, stack);
            return true;
        }
        if (stack.getItem() instanceof HengchongzhizhuanggucanfangItem) {
            ShiJunZhiLiGuCanGuFangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc.getX(), npc.getY(), npc.getZ(), npc);
            return true;
        }
        if (stack.getItem() instanceof XindehundaoputongjiweicanwuItem) {
            CanPoHuoYaoJinJingYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, npc);
            return true;
        }
        return false;
    }
}
