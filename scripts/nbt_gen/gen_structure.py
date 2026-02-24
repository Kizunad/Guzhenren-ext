import nbtlib
from nbtlib.tag import Compound, List, Int, String

def create_structure_nbt(size_x, size_y, size_z, palette_blocks, blocks_data):
    """
    Creates a Minecraft structural NBT (.nbt) file using nbtlib.
    """
    
    # 1. Size
    size_tag = List[Int]([size_x, size_y, size_z])
    
    # 2. Palette
    palette_tag = List[Compound]()
    for pb in palette_blocks:
        comp = Compound({
            "Name": String(pb["Name"])
        })
        if "Properties" in pb:
            props = Compound({k: String(v) for k, v in pb["Properties"].items()})
            comp["Properties"] = props
        palette_tag.append(comp)
        
    # 3. Blocks
    blocks_tag = List[Compound]()
    for bd in blocks_data:
        comp = Compound({
            "pos": List[Int](bd["pos"]),
            "state": Int(bd["state"])
        })
        if "nbt" in bd:
            comp["nbt"] = bd["nbt"]
        blocks_tag.append(comp)
        
    # 4. Entities (Empty by default for simple structures)
    entities_tag = List[Compound]()
    
    # Assembly
    root = Compound({
        "DataVersion": Int(3465), # 1.20.1+
        "size": size_tag,
        "palette": palette_tag,
        "blocks": blocks_tag,
        "entities": entities_tag
    })
    
    return nbtlib.File({"": root})

def generate_tier2_formation():
    """Generates a sample Tier 2 Formation (e.g. 5x1x5)"""
    size = 5
    
    palette = [
        {"Name": "minecraft:stone_bricks"}, # 0 (Floor)
        {"Name": "minecraft:gold_block"},   # 1 (Corners)
        {"Name": "minecraft:air"}           # 2
    ]
    
    blocks = []
    
    for x in range(size):
        for y in range(1): # Just a flat platform for now
            for z in range(size):
                state = 0 # Default stone bricks
                
                # Corners are gold
                if (x == 0 and z == 0) or (x == size-1 and z == 0) or \
                   (x == 0 and z == size-1) or (x == size-1 and z == size-1):
                    state = 1
                
                # Center is where the core block will be
                if x == 2 and y == 0 and z == 2:
                    state = 2
                    
                blocks.append({
                    "pos": [x, y, z],
                    "state": state
                })
                
    nbt_file = create_structure_nbt(size, 1, size, palette, blocks)
    nbt_file.save("tier2_formation_sample.nbt")
    print("Generated tier2_formation_sample.nbt")

if __name__ == "__main__":
    generate_tier2_formation()
