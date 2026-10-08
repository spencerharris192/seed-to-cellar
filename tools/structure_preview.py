"""Renders a structure template (village building NBT) as an isometric picture for review, local only:

    .venv/Scripts/python structure_preview.py village/plains/brewhouse [--cut] [--from-y N]

Each block is drawn as a small cube coloured from its texture (vanilla references in tools/vanilla, or the mod's own
textures), lit from the top-left like the game. --cut removes the front half so you can see inside; --from-y hides
everything below a height (the cellar) or --to-y everything above (the roof). Writes tools/out/structure_<name>.png.
"""
import gzip
import struct
import sys
from pathlib import Path

from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parent
STRUCTURES = ROOT.parent / "src/main/resources/data/seedtocellar/structure"
TEXTURES = [ROOT / "vanilla/block", ROOT.parent / "src/main/resources/assets/seedtocellar/textures/block"]
SKIP = {"minecraft:air", "minecraft:structure_void", "minecraft:jigsaw", "minecraft:cave_air"}
# blocks whose texture name differs from their id, or that read better as a fixed colour
ALIAS = {"grass_block": "grass_block_top", "dirt_path": "dirt_path_top", "campfire": "campfire_log_lit", "barrel": "barrel_side",
         "composter": "composter_side", "chest": "oak_planks", "lantern": "lantern", "ladder": "ladder", "cobweb": "cobweb",
         "snow": "snow", "glass_pane": "glass", "brew_kettle": "brew_kettle_copper", "kiln": "kiln_side", "millstone": "millstone_side"}
SHAPES = {"slab": 0.5, "stairs": 0.75, "fence": 0.4, "pane": 0.3, "door": 0.35, "trapdoor": 0.2, "lantern": 0.4, "snow": 0.15,
          "ladder": 0.2, "cobweb": 0.6, "vine": 0.5, "hops": 0.5, "campfire": 0.45, "cask": 0.85, "wine_rack": 1.0, "shelf": 0.7}


def read_nbt(path):
    data = gzip.decompress(path.read_bytes())

    def read(pos, t):
        if t == 1: return struct.unpack_from(">b", data, pos)[0], pos + 1
        if t == 2: return struct.unpack_from(">h", data, pos)[0], pos + 2
        if t == 3: return struct.unpack_from(">i", data, pos)[0], pos + 4
        if t == 4: return struct.unpack_from(">q", data, pos)[0], pos + 8
        if t == 5: return struct.unpack_from(">f", data, pos)[0], pos + 4
        if t == 6: return struct.unpack_from(">d", data, pos)[0], pos + 8
        if t == 8:
            n = struct.unpack_from(">H", data, pos)[0]
            return data[pos + 2:pos + 2 + n].decode(), pos + 2 + n
        if t == 9:
            et, n = data[pos], struct.unpack_from(">i", data, pos + 1)[0]
            pos += 5
            out = []
            for _ in range(n):
                v, pos = read(pos, et)
                out.append(v)
            return out, pos
        if t == 10:
            out = {}
            while True:
                ct = data[pos]
                pos += 1
                if ct == 0: return out, pos
                nl = struct.unpack_from(">H", data, pos)[0]
                key = data[pos + 2:pos + 2 + nl].decode()
                out[key], pos = read(pos + 2 + nl, ct)
        raise ValueError(t)

    nl = struct.unpack_from(">H", data, 1)[0]
    return read(3 + nl, 10)[0]


_colors = {}


def color_of(name: str):
    if name in _colors: return _colors[name]
    short = name.split(":")[1]
    candidates = [ALIAS.get(short, short), short, short + "_side", short + "_top", short.replace("_stairs", "_planks").replace("_slab", "_planks")
                  .replace("_fence_gate", "_planks").replace("_fence", "_planks").replace("_door", "_planks").replace("_trapdoor", "_planks"),
                  short.replace("stripped_", "stripped_") + "_top"]
    if short.endswith("_cask"): candidates.insert(0, short + "_side")
    for c in candidates:
        for root in TEXTURES:
            p = root / f"{c}.png"
            if p.exists():
                im = Image.open(p).convert("RGBA").resize((1, 1), Image.BOX)
                col = im.getpixel((0, 0))
                _colors[name] = col[:3] if col[3] > 0 else (200, 200, 200)
                return _colors[name]
    _colors[name] = (190, 120, 200)   # unknown: magenta-ish so it stands out
    return _colors[name]


FIXED = {"minecraft:grass_block": (106, 160, 70), "seedtocellar:hops": (92, 150, 60), "seedtocellar:white_grape_vine": (120, 160, 70),
         "seedtocellar:red_grape_vine": (110, 70, 80), "minecraft:lantern": (240, 190, 90), "minecraft:campfire": (230, 120, 40)}


def shade(c, f):
    return tuple(max(0, min(255, int(v * f))) for v in c)


def main(argv):
    rel = argv[0]
    cut = "--cut" in argv
    lo = int(argv[argv.index("--from-y") + 1]) if "--from-y" in argv else -1
    hi = int(argv[argv.index("--to-y") + 1]) if "--to-y" in argv else 999
    root = read_nbt(STRUCTURES / f"{rel}.nbt")
    sx, sy, sz = root["size"]
    pal = root["palette"]
    blocks = []
    for b in root["blocks"]:
        name = pal[b["state"]]["Name"]
        if name in SKIP: continue
        x, y, z = b["pos"]
        if y < lo or y > hi: continue
        if cut and z < 4 and y > 3: continue
        if "--back" not in argv:                       # turn it round so the front (z = 0, the street side) faces you
            x, z = sx - 1 - x, sz - 1 - z
        blocks.append((x, y, z, name))
    S = 18                                             # pixels per block edge
    W = (sx + sz) * S + 40
    H = (sx + sz) * S // 2 + sy * S + 40
    img = Image.new("RGB", (W, H), (126, 160, 96))
    d = ImageDraw.Draw(img)
    ox, oy = sz * S + 20, sy * S + 20

    def iso(x, y, z):
        return ox + (x - z) * S, oy + (x + z) * S // 2 - y * S

    # draw back to front: far (low x+z) first, low y first
    for x, y, z, name in sorted(blocks, key=lambda b: (b[0] + b[2], b[1])):
        h = 1.0
        for key, v in SHAPES.items():
            if key in name: h = v
        c = FIXED.get(name) or color_of(name)
        top = [iso(x, y + h, z), iso(x + 1, y + h, z), iso(x + 1, y + h, z + 1), iso(x, y + h, z + 1)]
        left = [iso(x, y + h, z + 1), iso(x + 1, y + h, z + 1), iso(x + 1, y, z + 1), iso(x, y, z + 1)]
        right = [iso(x + 1, y + h, z), iso(x + 1, y + h, z + 1), iso(x + 1, y, z + 1), iso(x + 1, y, z)]
        d.polygon(top, fill=shade(c, 1.1), outline=shade(c, 0.6))
        d.polygon(left, fill=shade(c, 0.8), outline=shade(c, 0.5))
        d.polygon(right, fill=shade(c, 0.62), outline=shade(c, 0.4))
    out = ROOT / "out" / f"structure_{rel.replace('/', '_')}{'_cut' if cut else ''}.png"
    out.parent.mkdir(exist_ok=True)
    img.save(out)
    print(f"Wrote {out}")


if __name__ == "__main__":
    main(sys.argv[1:])
