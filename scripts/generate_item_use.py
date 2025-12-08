import os
import re

# 配置
SOURCE_DIR = "LibSourceCodes/guzhenren/src/main/java/net/guzhenren/item"
OUTPUT_FILE = "src/main/java/com/Kizunad/guzhenrenext/guzhenrenBridge/generated/itemUse/GuzhenrenItemDispatcher.java"
PACKAGE_NAME = "com.Kizunad.guzhenrenext.guzhenrenBridge.generated.itemUse"

# 正则表达式
# 匹配 public class XxxItem extends Item ...
CLASS_PATTERN = re.compile(r"public class (\w+) extends Item")
# 匹配 use 方法体
USE_METHOD_PATTERN = re.compile(r"public InteractionResultHolder<ItemStack> use\(Level world, Player entity, InteractionHand hand\) \{(.*?)\}", re.DOTALL)
# 匹配 Procedure.execute 调用
# 示例: ShuiJiaGuDangYouJiKongQiShiShiTiDeWeiZhiProcedure.execute(world, entity.getX(), entity.getY(), entity.getZ(), entity, ar.getObject());
# 我们只提取 Procedure 类名和参数部分
PROCEDURE_CALL_PATTERN = re.compile(r"(\w+Procedure)\.execute\((.*?)\);")

def generate_dispatcher():
    items = []
    
    # 扫描文件
    for root, dirs, files in os.walk(SOURCE_DIR):
        for file in files:
            if file.endswith("Item.java"):
                file_path = os.path.join(root, file)
                with open(file_path, "r", encoding="utf-8") as f:
                    content = f.read()
                    
                    class_match = CLASS_PATTERN.search(content)
                    if not class_match:
                        continue
                        
                    class_name = class_match.group(1)
                    
                    # 查找 use 方法
                    use_match = USE_METHOD_PATTERN.search(content)
                    if use_match:
                        method_body = use_match.group(1)
                        # 查找 Procedure 调用
                        # 我们假设一个 use 方法里只有一个主要的 Procedure 调用，或者我们只关心第一个
                        proc_match = PROCEDURE_CALL_PATTERN.search(method_body)
                        if proc_match:
                            proc_name = proc_match.group(1)
                            args_str = proc_match.group(2)
                            
                            items.append({
                                "item_class": class_name,
                                "proc_name": proc_name,
                                "args": args_str
                            })

    # 生成 Java 代码
    with open(OUTPUT_FILE, "w", encoding="utf-8") as f:
        f.write(f"package {PACKAGE_NAME};\n\n")
        f.write("import net.minecraft.world.entity.LivingEntity;\n")
        f.write("import net.minecraft.world.entity.Entity;\n")
        f.write("import net.minecraft.world.item.ItemStack;\n")
        f.write("import net.minecraft.world.level.Level;\n")
        f.write("import net.guzhenren.item.*;\n")
        f.write("import net.guzhenren.procedures.*;\n\n")
        
        f.write("public class GuzhenrenItemDispatcher {\n\n")
        f.write("    public static boolean dispatch(LivingEntity npc, ItemStack stack) {\n")
        f.write("        if (stack.isEmpty()) return false;\n")
        f.write("        Level world = npc.level();\n")
        f.write("        // 尝试匹配物品类型并调用对应 Procedure\n")
        
        for item in items:
            f.write(f"        if (stack.getItem() instanceof {item['item_class']}) {{\n")
            
            # 参数转换: 
            # entity -> npc
            # ar.getObject() -> stack
            # itemstack -> stack
            # entity.getX() -> npc.getX() ...
            
            original_args = item['args']
            new_args = original_args.replace("entity", "npc")
            new_args = new_args.replace("ar.getObject()", "stack")
            new_args = new_args.replace("itemstack", "stack")
            
            # 处理可能的强制转换错误 (Player) npc -> (Entity) npc 或直接忽略转换
            # 简单的正则替换可能不够，但先试水
            
            f.write(f"            {item['proc_name']}.execute({new_args});\n")
            f.write("            return true;\n")
            f.write("        }\n")
            
        f.write("        return false;\n")
        f.write("    }\n")
        f.write("}\n")
    
    print(f"Generated {len(items)} handlers in {OUTPUT_FILE}")

if __name__ == "__main__":
    # 创建目录
    os.makedirs(os.path.dirname(OUTPUT_FILE), exist_ok=True)
    generate_dispatcher()
