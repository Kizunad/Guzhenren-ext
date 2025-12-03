package com.Kizunad.customNPCs.util;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class NamePool {

    // 包含主角、配角、尊者及各方势力代表人物
    private static final List<String> NAMES = Arrays.asList(
        // --- 核心人物 ---
        "古月方源",
        "白凝冰",
        "黑楼兰",
        "太白云生",
        "方正",
        // --- 中洲 / 天庭 / 灵缘斋 ---
        "凤九歌",
        "秦百胜",
        "赵怜云",
        "薄青",
        "紫薇仙子",
        "龙公",
        "监天塔主",
        // --- 北原 / 长生天 ---
        "巨阳仙尊",
        "马鸿运",
        "药乐",
        "东方长凡",
        "耶律群星",
        "黑城",
        "雪胡老祖",
        // --- 南疆 / 商家 / 铁家 ---
        "商心慈",
        "铁若男",
        "武庸",
        "池曲由",
        "夏查",
        "白相",
        "翼家老祖",
        // --- 东海 / 僵盟 ---
        "宋启元",
        "鲨魔",
        "苏白曼",
        "庙明神",
        "华文洞天",
        // --- 西漠 / 房家 ---
        "房睇长",
        "唐方明",
        "万家老祖",
        // --- 影宗 / 尊者 / 特殊存在 ---
        "幽魂魔尊",
        "影无邪",
        "紫山真君",
        "乐土仙尊",
        "星宿仙尊",
        "狂蛮魔尊",
        "盗天魔尊",
        "红莲魔尊",
        "元始仙尊",
        "琅琊地灵"
    );
    private static final Random RANDOM = new Random();

    public static String getRandomName() {
        return NAMES.get(RANDOM.nextInt(NAMES.size()));
    }
}
