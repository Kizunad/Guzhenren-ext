package com.Kizunad.customNPCs_test.tests;

import com.Kizunad.customNPCs.ai.personality.DriveType;
import com.Kizunad.customNPCs.ai.personality.EmotionType;
import com.Kizunad.customNPCs.capabilities.mind.INpcMind;
import com.Kizunad.customNPCs_test.utils.NpcTestHelper;
import com.Kizunad.customNPCs_test.utils.TestBatches;
import com.Kizunad.customNPCs_test.utils.TestEntityFactory;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;

@GameTestHolder("guzhenrenext")
@SuppressWarnings("checkstyle:MagicNumber")
public class PersistenceTests {

    private static final int MEMORY_DURATION = 200;

    @GameTest(
        templateNamespace = "minecraft",
        template = "empty",
        batch = TestBatches.BASE
    )
    public static void testMindPersistence(GameTestHelper helper) {
        Zombie zombie = TestEntityFactory.createSimpleTestNPC(
            helper,
            new BlockPos(2, 2, 2),
            EntityType.ZOMBIE
        );
        INpcMind mind = NpcTestHelper.getMind(helper, zombie);

        UUID attackerUuid = UUID.randomUUID();
        Vec3 soundLocation = new Vec3(1.5, 2.5, 3.5);
        List<String> soundTypes = List.of("movement", "pain");

        mind.getMemory().rememberLongTerm("last_attacker", attackerUuid);
        mind
            .getMemory()
            .rememberShortTerm(
                "loudest_sound_location",
                soundLocation,
                MEMORY_DURATION
            );
        mind
            .getMemory()
            .rememberShortTerm(
                "loudest_sound_types",
                soundTypes,
                MEMORY_DURATION
            );
        mind
            .getMemory()
            .rememberShortTerm("threat_level", 0.75f, MEMORY_DURATION);

        mind.getPersonality().triggerEmotion(EmotionType.ANGER, 0.6f);
        mind.getPersonality().triggerEmotion(EmotionType.FEAR, 0.4f);
        mind.getPersonality().setDrive(DriveType.PRIDE, 0.9f);
        mind.getPersonality().setDrive(DriveType.SURVIVAL, 0.3f);

        CompoundTag saved = mind.serializeNBT(helper.getLevel().registryAccess());

        Zombie restored = TestEntityFactory.createSimpleTestNPC(
            helper,
            new BlockPos(6, 2, 2),
            EntityType.ZOMBIE
        );
        INpcMind restoredMind = NpcTestHelper.getMind(helper, restored);
        restoredMind.deserializeNBT(helper.getLevel().registryAccess(), saved);

        helper.succeedWhen(() -> {
            helper.assertTrue(
                restoredMind.getMemory().hasMemory("last_attacker"),
                "攻击者记忆应被持久化"
            );
            Object restoredAttacker = restoredMind
                .getMemory()
                .getMemory("last_attacker");
            helper.assertTrue(
                attackerUuid.equals(restoredAttacker),
                "攻击者 UUID 应保持一致"
            );

            Vec3 restoredLocation = restoredMind
                .getMemory()
                .getShortTerm(
                    "loudest_sound_location",
                    Vec3.class,
                    Vec3.ZERO
                );
            helper.assertTrue(
                restoredLocation.distanceTo(soundLocation) < 0.001,
                "声音位置应保持一致"
            );

            @SuppressWarnings("unchecked")
            List<String> restoredTypes = restoredMind
                .getMemory()
                .getShortTerm(
                    "loudest_sound_types",
                    List.class,
                    List.of()
                );
            helper.assertTrue(
                restoredTypes.equals(soundTypes),
                "声音类型列表应保持一致"
            );

            float restoredThreat = restoredMind
                .getMemory()
                .getShortTerm("threat_level", Float.class, 0.0f);
            helper.assertTrue(
                Math.abs(restoredThreat - 0.75f) < 0.0001f,
                "威胁等级应保持一致"
            );

            float anger = restoredMind
                .getPersonality()
                .getEmotion(EmotionType.ANGER);
            float fear = restoredMind
                .getPersonality()
                .getEmotion(EmotionType.FEAR);
            helper.assertTrue(
                Math.abs(anger - 0.6f) < 0.0001f &&
                Math.abs(fear - 0.4f) < 0.0001f,
                "情绪值应保持一致"
            );

            float pride = restoredMind.getPersonality().getDrive(DriveType.PRIDE);
            float survival = restoredMind
                .getPersonality()
                .getDrive(DriveType.SURVIVAL);
            helper.assertTrue(
                Math.abs(pride - 0.9f) < 0.0001f &&
                Math.abs(survival - 0.3f) < 0.0001f,
                "性格驱动力应保持一致"
            );
        });
    }
}
