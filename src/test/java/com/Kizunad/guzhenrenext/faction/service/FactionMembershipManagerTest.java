package com.Kizunad.guzhenrenext.faction.service;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class FactionMembershipManagerTest {

    private static final long TEST_JOINED_AT_SEED = 88L;
    private static final int TEST_CONTRIBUTION_SEED = 123;
    private static final long TEST_PROMOTE_JOINED_AT = 12345L;
    private static final int TEST_PROMOTE_CONTRIBUTION = 678;
    private static final int TEST_CONTRIBUTION_SEED_MAX = 99_990;
    private static final int TEST_CONTRIBUTION_INCREMENT = 200;
    private static final int TEST_CONTRIBUTION_MAX = 100_000;
    private static final long TEST_MEMBER_A_JOINED_AT = 10L;
    private static final int TEST_MEMBER_A_CONTRIBUTION = 500;
    private static final long TEST_MEMBER_B_JOINED_AT = 20L;
    private static final int TEST_MEMBER_B_CONTRIBUTION = 200;
    private static final long TEST_MEMBER_C_JOINED_AT = 11L;
    private static final int TEST_MEMBER_C_CONTRIBUTION = 50;
    private static final long TEST_MEMBER_D_JOINED_AT = 3L;
    private static final int TEST_MEMBER_D_CONTRIBUTION = 3;
    private static final int TEST_MEMBER_COUNT = 3;
    private static final int TEST_FACTION_CORE_POWER = 100;
    private static final int TEST_FACTION_CORE_RESOURCES = 100;

    private static final MembershipManagerHarness HARNESS = new MembershipManagerHarness();

    @Test
    void testCanRecruitTrueWhenFactionExistsAndCandidateNotInAnyFaction() throws Exception {
        Object level = HARNESS.newServerLevel();
        UUID factionId = UUID.randomUUID();
        UUID candidateId = UUID.randomUUID();
        HARNESS.seedFaction(level, HARNESS.newFactionCore(factionId, "测试宗门A"));

        boolean result = HARNESS.canRecruit(level, factionId, candidateId);

        assertTrue(result, "势力存在且候选人未加入任意势力时，应允许招募");
    }

    @Test
    void testCanRecruitFalseWhenCandidateAlreadyInAnotherFaction() throws Exception {
        Object level = HARNESS.newServerLevel();
        UUID factionA = UUID.randomUUID();
        UUID factionB = UUID.randomUUID();
        UUID candidateId = UUID.randomUUID();

        HARNESS.seedFaction(level, HARNESS.newFactionCore(factionA, "测试宗门A"));
        HARNESS.seedFaction(level, HARNESS.newFactionCore(factionB, "测试宗门B"));
        HARNESS.seedMembership(
            level,
            HARNESS.newMembership(
                candidateId,
                factionB,
                "MEMBER",
                TEST_JOINED_AT_SEED,
                TEST_CONTRIBUTION_SEED
            )
        );

        boolean result = HARNESS.canRecruit(level, factionA, candidateId);

        assertFalse(result, "候选人已在其他势力中时，不应允许招募");
    }

    @Test
    void testPromoteChangesRoleAndPreservesJoinedAtAndContribution() throws Exception {
        Object level = HARNESS.newServerLevel();
        UUID factionId = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();
        long joinedAt = TEST_PROMOTE_JOINED_AT;
        int contribution = TEST_PROMOTE_CONTRIBUTION;

        HARNESS.seedFaction(level, HARNESS.newFactionCore(factionId, "测试宗门"));
        HARNESS.seedMembership(level, HARNESS.newMembership(memberId, factionId, "MEMBER", joinedAt, contribution));

        boolean promoted = HARNESS.promote(level, factionId, memberId, "ELDER");
        List<Object> members = HARNESS.getMembers(level, factionId);

        assertTrue(promoted, "成员存在时应晋升成功");
        assertEquals(1, members.size(), "晋升后成员数量不应变化");
        Object updated = members.get(0);
        assertEquals("ELDER", HARNESS.getMemberRoleName(updated), "角色应更新为新角色");
        assertEquals(joinedAt, HARNESS.getMemberJoinedAt(updated), "joinedAt 应保持不变");
        assertEquals(contribution, HARNESS.getMemberContribution(updated), "contribution 应保持不变");
    }

    @Test
    void testAddContributionClampsToMax() throws Exception {
        Object level = HARNESS.newServerLevel();
        UUID factionId = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();
        HARNESS.seedFaction(level, HARNESS.newFactionCore(factionId, "测试宗门"));
        HARNESS.seedMembership(
            level,
            HARNESS.newMembership(
                memberId,
                factionId,
                "MEMBER",
                1L,
                TEST_CONTRIBUTION_SEED_MAX
            )
        );

        boolean updated = HARNESS.addContribution(level, factionId, memberId, TEST_CONTRIBUTION_INCREMENT);
        List<Object> members = HARNESS.getMembers(level, factionId);

        assertTrue(updated, "成员存在时应更新贡献值成功");
        assertEquals(1, members.size(), "贡献更新后成员数量应保持不变");
        assertEquals(
            TEST_CONTRIBUTION_MAX,
            HARNESS.getMemberContribution(members.get(0)),
            "贡献值应被限制在上限 100000"
        );
    }

    @Test
    void testGetMembersReturnsSeededMembersListContent() throws Exception {
        Object level = HARNESS.newServerLevel();
        UUID factionId = UUID.randomUUID();
        UUID memberA = UUID.randomUUID();
        UUID memberB = UUID.randomUUID();
        HARNESS.seedFaction(level, HARNESS.newFactionCore(factionId, "测试宗门"));
        HARNESS.seedMembership(
            level,
            HARNESS.newMembership(
                memberA,
                factionId,
                "LEADER",
                TEST_MEMBER_A_JOINED_AT,
                TEST_MEMBER_A_CONTRIBUTION
            )
        );
        HARNESS.seedMembership(
            level,
            HARNESS.newMembership(
                memberB,
                factionId,
                "MEMBER",
                TEST_MEMBER_B_JOINED_AT,
                TEST_MEMBER_B_CONTRIBUTION
            )
        );

        List<Object> members = HARNESS.getMembers(level, factionId);

        assertEquals(2, members.size(), "应返回全部种子成员");
        List<UUID> memberIds = new ArrayList<>();
        for (Object membership : members) {
            memberIds.add(HARNESS.getMemberId(membership));
        }
        assertTrue(memberIds.contains(memberA), "返回列表应包含成员 A");
        assertTrue(memberIds.contains(memberB), "返回列表应包含成员 B");
    }

    @Test
    void testIsMemberReturnsTrueAndFalseCorrectly() throws Exception {
        Object level = HARNESS.newServerLevel();
        UUID factionId = UUID.randomUUID();
        UUID inFaction = UUID.randomUUID();
        UUID outFaction = UUID.randomUUID();
        HARNESS.seedFaction(level, HARNESS.newFactionCore(factionId, "测试宗门"));
        HARNESS.seedMembership(
            level,
            HARNESS.newMembership(
                inFaction,
                factionId,
                "MEMBER",
                TEST_MEMBER_C_JOINED_AT,
                TEST_MEMBER_C_CONTRIBUTION
            )
        );

        boolean trueResult = HARNESS.isMember(level, factionId, inFaction);
        boolean falseResult = HARNESS.isMember(level, factionId, outFaction);

        assertTrue(trueResult, "已在势力中的成员应返回 true");
        assertFalse(falseResult, "不在势力中的成员应返回 false");
    }

    @Test
    void testGetMemberCountReturnsCorrectCount() throws Exception {
        Object level = HARNESS.newServerLevel();
        UUID factionId = UUID.randomUUID();
        HARNESS.seedFaction(level, HARNESS.newFactionCore(factionId, "测试宗门"));
        HARNESS.seedMembership(level, HARNESS.newMembership(UUID.randomUUID(), factionId, "LEADER", 1L, 1));
        HARNESS.seedMembership(level, HARNESS.newMembership(UUID.randomUUID(), factionId, "ELDER", 2L, 2));
        HARNESS.seedMembership(
            level,
            HARNESS.newMembership(
                UUID.randomUUID(),
                factionId,
                "MEMBER",
                TEST_MEMBER_D_JOINED_AT,
                TEST_MEMBER_D_CONTRIBUTION
            )
        );

        int count = HARNESS.getMemberCount(level, factionId);

        assertEquals(TEST_MEMBER_COUNT, count, "成员计数应与种子数据一致");
    }

    private static final class MembershipManagerHarness {

        private static final String MANAGER_CLASS_NAME =
            "com.Kizunad.guzhenrenext.faction.service.FactionMembershipManager";

        private static final String FACTION_CORE_CLASS_NAME = "com.Kizunad.guzhenrenext.faction.core.FactionCore";

        private static final String FACTION_TYPE_CLASS_NAME =
            "com.Kizunad.guzhenrenext.faction.core.FactionCore$FactionType";

        private static final String FACTION_STATUS_CLASS_NAME =
            "com.Kizunad.guzhenrenext.faction.core.FactionCore$FactionStatus";

        private static final String MEMBERSHIP_CLASS_NAME =
            "com.Kizunad.guzhenrenext.faction.core.FactionMembership";

        private static final String MEMBER_ROLE_CLASS_NAME =
            "com.Kizunad.guzhenrenext.faction.core.FactionMembership$MemberRole";

        private static final String SERVER_LEVEL_CLASS_NAME = "net.minecraft.server.level.ServerLevel";

        private static final String WORLD_DATA_CLASS_NAME =
            "com.Kizunad.guzhenrenext.faction.data.FactionWorldData";

        private static final String SOURCE_MANAGER =
            "src/main/java/com/Kizunad/guzhenrenext/faction/service/FactionMembershipManager.java";

        private static final String SOURCE_CORE =
            "src/main/java/com/Kizunad/guzhenrenext/faction/core/FactionCore.java";

        private static final String SOURCE_MEMBERSHIP =
            "src/main/java/com/Kizunad/guzhenrenext/faction/core/FactionMembership.java";

        private final Class<?> managerClass;

        private final Class<?> factionCoreClass;

        private final Class<?> factionTypeClass;

        private final Class<?> factionStatusClass;

        private final Class<?> membershipClass;

        private final Class<?> memberRoleClass;

        private final Class<?> serverLevelClass;

        private final Class<?> worldDataClass;

        private final Constructor<?> factionCoreConstructor;

        private final Constructor<?> membershipConstructor;

        private final Constructor<?> serverLevelConstructor;

        private final Method canRecruitMethod;

        private final Method promoteMethod;

        private final Method addContributionMethod;

        private final Method getMembersMethod;

        private final Method isMemberMethod;

        private final Method getMemberCountMethod;

        private final Method worldDataSeedFactionMethod;

        private final Method worldDataSeedMembershipMethod;

        private final Method memberIdMethod;

        private final Method memberRoleMethod;

        private final Method memberJoinedAtMethod;

        private final Method memberContributionMethod;

        private MembershipManagerHarness() {
            try {
                IsolatedCompilation isolatedCompilation = compileIsolated();

                managerClass = isolatedCompilation.loadClass(MANAGER_CLASS_NAME);
                factionCoreClass = isolatedCompilation.loadClass(FACTION_CORE_CLASS_NAME);
                factionTypeClass = isolatedCompilation.loadClass(FACTION_TYPE_CLASS_NAME);
                factionStatusClass = isolatedCompilation.loadClass(FACTION_STATUS_CLASS_NAME);
                membershipClass = isolatedCompilation.loadClass(MEMBERSHIP_CLASS_NAME);
                memberRoleClass = isolatedCompilation.loadClass(MEMBER_ROLE_CLASS_NAME);
                serverLevelClass = isolatedCompilation.loadClass(SERVER_LEVEL_CLASS_NAME);
                worldDataClass = isolatedCompilation.loadClass(WORLD_DATA_CLASS_NAME);

                factionCoreConstructor = factionCoreClass.getDeclaredConstructor(
                    UUID.class,
                    String.class,
                    factionTypeClass,
                    long.class,
                    factionStatusClass,
                    int.class,
                    int.class
                );
                membershipConstructor = membershipClass.getDeclaredConstructor(
                    UUID.class,
                    UUID.class,
                    memberRoleClass,
                    long.class,
                    int.class
                );
                serverLevelConstructor = serverLevelClass.getDeclaredConstructor();
                canRecruitMethod = managerClass.getMethod("canRecruit", serverLevelClass, UUID.class, UUID.class);
                promoteMethod = managerClass.getMethod(
                    "promote",
                    serverLevelClass,
                    UUID.class,
                    UUID.class,
                    memberRoleClass
                );
                addContributionMethod = managerClass.getMethod(
                    "addContribution",
                    serverLevelClass,
                    UUID.class,
                    UUID.class,
                    int.class
                );
                getMembersMethod = managerClass.getMethod("getMembers", serverLevelClass, UUID.class);
                isMemberMethod = managerClass.getMethod("isMember", serverLevelClass, UUID.class, UUID.class);
                getMemberCountMethod = managerClass.getMethod("getMemberCount", serverLevelClass, UUID.class);

                worldDataSeedFactionMethod = worldDataClass.getMethod("seedFaction", factionCoreClass);
                worldDataSeedMembershipMethod = worldDataClass.getMethod("seedMembership", membershipClass);

                memberIdMethod = membershipClass.getMethod("memberId");
                memberRoleMethod = membershipClass.getMethod("role");
                memberJoinedAtMethod = membershipClass.getMethod("joinedAt");
                memberContributionMethod = membershipClass.getMethod("contribution");
            } catch (ReflectiveOperationException exception) {
                throw new IllegalStateException("无法初始化成员管理器反射测试支架", exception);
            }
        }

        private IsolatedCompilation compileIsolated() {
            try {
                Path sourceRoot = Files.createTempDirectory("membership-manager-src");
                Path classesRoot = Files.createTempDirectory("membership-manager-classes");

                Path managerSource = resolveRequiredSource(SOURCE_MANAGER);
                Path coreSource = resolveRequiredSource(SOURCE_CORE);
                Path membershipSource = resolveRequiredSource(SOURCE_MEMBERSHIP);

                List<Path> stubSources = List.of(
                    writeStub(sourceRoot, "net/minecraft/server/level/ServerLevel.java", stubServerLevel()),
                    writeStub(
                        sourceRoot,
                        "com/Kizunad/guzhenrenext/faction/data/FactionWorldData.java",
                        stubFactionWorldData()
                    ),
                    writeStub(sourceRoot, "net/minecraft/nbt/Tag.java", stubTag()),
                    writeStub(sourceRoot, "net/minecraft/nbt/CompoundTag.java", stubCompoundTag())
                );

                JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
                if (compiler == null) {
                    throw new IllegalStateException("当前运行环境不提供 JavaCompiler");
                }

                DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
                try (StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, null)) {
                    List<java.io.File> sourceFiles = new ArrayList<>();
                    sourceFiles.add(managerSource.toFile());
                    sourceFiles.add(coreSource.toFile());
                    sourceFiles.add(membershipSource.toFile());
                    for (Path stubSource : stubSources) {
                        sourceFiles.add(stubSource.toFile());
                    }

                    Iterable<? extends JavaFileObject> units = fileManager.getJavaFileObjectsFromFiles(sourceFiles);
                    List<String> options = List.of(
                        "-classpath",
                        System.getProperty("java.class.path"),
                        "-d",
                        classesRoot.toString()
                    );

                    boolean success = compiler.getTask(null, fileManager, diagnostics, options, null, units).call();
                    if (!success) {
                        StringBuilder message = new StringBuilder("隔离编译 FactionMembershipManager 失败：\n");
                        for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics.getDiagnostics()) {
                            message.append(diagnostic.toString()).append('\n');
                        }
                        throw new IllegalStateException(message.toString());
                    }
                }

                return new IsolatedCompilation(classesRoot);
            } catch (Exception exception) {
                throw new IllegalStateException("无法构建隔离版成员管理器测试运行时", exception);
            }
        }

        private Path resolveRequiredSource(String sourcePath) {
            Path resolved = Path.of(System.getProperty("user.dir")).resolve(sourcePath);
            if (!Files.exists(resolved)) {
                throw new IllegalStateException("未找到目标源码：" + resolved);
            }
            return resolved;
        }

        private Path writeStub(Path sourceRoot, String relativePath, String source) throws Exception {
            Path stubPath = sourceRoot.resolve(relativePath);
            Files.createDirectories(stubPath.getParent());
            Files.writeString(stubPath, source);
            return stubPath;
        }

        private String stubServerLevel() {
            return "package net.minecraft.server.level;\n"
                + "\n"
                + "public class ServerLevel {\n"
                + "}\n";
        }

        private String stubFactionWorldData() {
            return "package com.Kizunad.guzhenrenext.faction.data;\n"
                + "\n"
                + "import com.Kizunad.guzhenrenext.faction.core.FactionCore;\n"
                + "import com.Kizunad.guzhenrenext.faction.core.FactionMembership;\n"
                + "import java.util.ArrayList;\n"
                + "import java.util.Collection;\n"
                + "import java.util.Collections;\n"
                + "import java.util.HashMap;\n"
                + "import java.util.List;\n"
                + "import java.util.Map;\n"
                + "import java.util.UUID;\n"
                + "import net.minecraft.server.level.ServerLevel;\n"
                + "\n"
                + "public class FactionWorldData {\n"
                + "    private static final Map<ServerLevel, FactionWorldData> INSTANCES = "
                + "new HashMap<>();\n"
                + "\n"
                + "    private final Map<UUID, FactionCore> factions = new HashMap<>();\n"
                + "\n"
                + "    private final Map<UUID, List<FactionMembership>> memberships = new HashMap<>();\n"
                + "\n"
                + "    public static FactionWorldData get(ServerLevel level) {\n"
                + "        return INSTANCES.computeIfAbsent(level, key -> new FactionWorldData());\n"
                + "    }\n"
                + "\n"
                + "    public static void seedForLevel(ServerLevel level, FactionWorldData data) {\n"
                + "        INSTANCES.put(level, data);\n"
                + "    }\n"
                + "\n"
                + "    public void seedFaction(FactionCore faction) {\n"
                + "        if (faction != null) {\n"
                + "            factions.put(faction.id(), faction);\n"
                + "        }\n"
                + "    }\n"
                + "\n"
                + "    public void seedMembership(FactionMembership membership) {\n"
                + "        if (membership != null) {\n"
                + "            memberships.computeIfAbsent(membership.factionId(), key -> "
                + "new ArrayList<>()).add(membership);\n"
                + "        }\n"
                + "    }\n"
                + "\n"
                + "    public FactionCore getFaction(UUID factionId) {\n"
                + "        return factions.get(factionId);\n"
                + "    }\n"
                + "\n"
                + "    public Collection<FactionCore> getAllFactions() {\n"
                + "        return Collections.unmodifiableCollection(factions.values());\n"
                + "    }\n"
                + "\n"
                + "    public List<FactionMembership> getMemberships(UUID factionId) {\n"
                + "        List<FactionMembership> list = memberships.get(factionId);\n"
                + "        if (list == null) {\n"
                + "            return Collections.emptyList();\n"
                + "        }\n"
                + "        return Collections.unmodifiableList(list);\n"
                + "    }\n"
                + "\n"
                + "    public void removeMembership(UUID memberId, UUID factionId) {\n"
                + "        if (memberId == null || factionId == null) {\n"
                + "            return;\n"
                + "        }\n"
                + "        List<FactionMembership> list = memberships.get(factionId);\n"
                + "        if (list != null) {\n"
                + "            list.removeIf(membership -> membership.memberId().equals(memberId));\n"
                + "        }\n"
                + "    }\n"
                + "\n"
                + "    public void addMembership(FactionMembership membership) {\n"
                + "        if (membership == null) {\n"
                + "            return;\n"
                + "        }\n"
                + "        memberships.computeIfAbsent(membership.factionId(), key -> "
                + "new ArrayList<>()).add(membership);\n"
                + "    }\n"
                + "}\n";
        }

        private String stubTag() {
            return "package net.minecraft.nbt;\n"
                + "\n"
                + "public class Tag {\n"
                + "}\n";
        }

        private String stubCompoundTag() {
            return "package net.minecraft.nbt;\n"
                + "\n"
                + "import java.util.HashMap;\n"
                + "import java.util.Map;\n"
                + "import java.util.UUID;\n"
                + "\n"
                + "public class CompoundTag extends Tag {\n"
                + "    private final Map<String, Object> data = new HashMap<>();\n"
                + "\n"
                + "    public void putUUID(String key, UUID value) {\n"
                + "        data.put(key, value);\n"
                + "    }\n"
                + "\n"
                + "    public UUID getUUID(String key) {\n"
                + "        Object value = data.get(key);\n"
                + "        return value instanceof UUID ? (UUID) value : null;\n"
                + "    }\n"
                + "\n"
                + "    public boolean hasUUID(String key) {\n"
                + "        return data.get(key) instanceof UUID;\n"
                + "    }\n"
                + "\n"
                + "    public void putString(String key, String value) {\n"
                + "        data.put(key, value);\n"
                + "    }\n"
                + "\n"
                + "    public String getString(String key) {\n"
                + "        Object value = data.get(key);\n"
                + "        return value instanceof String ? (String) value : \"\";\n"
                + "    }\n"
                + "\n"
                + "    public void putLong(String key, long value) {\n"
                + "        data.put(key, value);\n"
                + "    }\n"
                + "\n"
                + "    public long getLong(String key) {\n"
                + "        Object value = data.get(key);\n"
                + "        return value instanceof Long ? ((Long) value).longValue() : 0L;\n"
                + "    }\n"
                + "\n"
                + "    public void putInt(String key, int value) {\n"
                + "        data.put(key, value);\n"
                + "    }\n"
                + "\n"
                + "    public int getInt(String key) {\n"
                + "        Object value = data.get(key);\n"
                + "        return value instanceof Integer ? ((Integer) value).intValue() : 0;\n"
                + "    }\n"
                + "\n"
                + "    public boolean contains(String key) {\n"
                + "        return data.containsKey(key);\n"
                + "    }\n"
                + "}\n";
        }

        Object newServerLevel() throws Exception {
            return serverLevelConstructor.newInstance();
        }

        Object newFactionCore(UUID factionId, String factionName) throws Exception {
            Object type = enumConstant(factionTypeClass, "SECT");
            Object status = enumConstant(factionStatusClass, "ACTIVE");
            return factionCoreConstructor.newInstance(
                factionId,
                factionName,
                type,
                0L,
                status,
                TEST_FACTION_CORE_POWER,
                TEST_FACTION_CORE_RESOURCES
            );
        }

        Object newMembership(UUID memberId, UUID factionId, String roleName, long joinedAt, int contribution)
            throws Exception {
            Object role = enumConstant(memberRoleClass, roleName);
            return membershipConstructor.newInstance(memberId, factionId, role, joinedAt, contribution);
        }

        private Object enumConstant(Class<?> enumClass, String name) throws Exception {
            Method valueOf = enumClass.getMethod("valueOf", String.class);
            return valueOf.invoke(null, name);
        }

        void seedFaction(Object level, Object faction) throws Exception {
            Object worldData = worldDataForLevel(level);
            worldDataSeedFactionMethod.invoke(worldData, faction);
        }

        void seedMembership(Object level, Object membership) throws Exception {
            Object worldData = worldDataForLevel(level);
            worldDataSeedMembershipMethod.invoke(worldData, membership);
        }

        private Object worldDataForLevel(Object level) throws Exception {
            return worldDataClass.getMethod("get", serverLevelClass).invoke(null, level);
        }

        boolean canRecruit(Object level, UUID factionId, UUID candidateId) throws Exception {
            return ((Boolean) canRecruitMethod.invoke(null, level, factionId, candidateId)).booleanValue();
        }

        boolean promote(Object level, UUID factionId, UUID memberId, String newRole) throws Exception {
            Object roleEnum = enumConstant(memberRoleClass, newRole);
            return ((Boolean) promoteMethod.invoke(null, level, factionId, memberId, roleEnum)).booleanValue();
        }

        boolean addContribution(Object level, UUID factionId, UUID memberId, int amount) throws Exception {
            return ((Boolean) addContributionMethod.invoke(null, level, factionId, memberId, amount)).booleanValue();
        }

        List<Object> getMembers(Object level, UUID factionId) throws Exception {
            Object value = getMembersMethod.invoke(null, level, factionId);
            List<Object> converted = new ArrayList<>();
            for (Object member : (List<?>) value) {
                converted.add(member);
            }
            return converted;
        }

        boolean isMember(Object level, UUID factionId, UUID memberId) throws Exception {
            return ((Boolean) isMemberMethod.invoke(null, level, factionId, memberId)).booleanValue();
        }

        int getMemberCount(Object level, UUID factionId) throws Exception {
            return ((Integer) getMemberCountMethod.invoke(null, level, factionId)).intValue();
        }

        UUID getMemberId(Object membership) throws Exception {
            return (UUID) memberIdMethod.invoke(membership);
        }

        String getMemberRoleName(Object membership) throws Exception {
            Object role = memberRoleMethod.invoke(membership);
            return String.valueOf(role);
        }

        long getMemberJoinedAt(Object membership) throws Exception {
            return ((Long) memberJoinedAtMethod.invoke(membership)).longValue();
        }

        int getMemberContribution(Object membership) throws Exception {
            return ((Integer) memberContributionMethod.invoke(membership)).intValue();
        }
    }

    private static final class IsolatedCompilation {

        private final URLClassLoader classLoader;

        private IsolatedCompilation(Path classesRoot) throws Exception {
            classLoader = new URLClassLoader(new URL[]{classesRoot.toUri().toURL()},
                FactionMembershipManagerTest.class.getClassLoader()) {
                @Override
                protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
                    synchronized (getClassLoadingLock(name)) {
                        Class<?> loaded = findLoadedClass(name);
                        if (loaded == null && shouldLoadFromIsolatedOutput(name)) {
                            try {
                                loaded = findClass(name);
                            } catch (ClassNotFoundException exception) {
                                loaded = super.loadClass(name, false);
                            }
                        }
                        if (loaded == null) {
                            loaded = super.loadClass(name, false);
                        }
                        if (resolve) {
                            resolveClass(loaded);
                        }
                        return loaded;
                    }
                }
            };
        }

        private Class<?> loadClass(String className) throws ClassNotFoundException {
            return Class.forName(className, true, classLoader);
        }

        private boolean shouldLoadFromIsolatedOutput(String className) {
            return className.startsWith("com.Kizunad.guzhenrenext.faction.")
                || className.startsWith("net.minecraft.");
        }
    }
}
