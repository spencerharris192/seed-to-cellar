"""Sorghum crop, growth stages 0-7 (crop model: the texture shows on 4 crossed planes).

Sorghum is built differently from the small grains: thick two-pixel stalks, broad leaves
arching out and down like corn, and a dense oval seed head on top that ripens from green
to rust red. Two plants per block, since each needs room for its leaves. Silhouette:
chunky stalks with a round dark head, nothing like the thin grasses.
"""
from texturegen.compose import blank, stamp, stamp_bottom, to_grid
from texturegen.palettes import FOLIAGE_CROP, SORGHUM

# digits = SORGHUM (rust, dark -> light), letters q-y = FOLIAGE_CROP (green, dark -> light)
LEGEND = {**{str(i): SORGHUM[i] for i in range(6)}, **dict(zip("qwerty", FOLIAGE_CROP))}

# Seedlings: separate little plants of broad leaves, then a small fan.
SPROUT = """
y.y
.t.
.r.
"""

FAN = """
..y..
y.t.y
.trt.
..r..
..e..
"""

# One leaf, arching up and out to the right from a node, then drooping at the tip;
# mirrored for the left. Lit along its upper edge.
LEAF = """
.yt..
t..r.
...r.
....e
"""

# The dense head, lit from the top-left. Stalk continues below columns 1-2.
HEAD_SHAPE = """
.HL.
HHLM
HLMM
LMMD
.MD.
"""

HEAD_COLORS = {
    "green":   {"H": "y", "L": "t", "M": "r", "D": "e"},
    "turning": {"H": "5", "L": "4", "M": "t", "D": "r"},
    "ripe":    {"H": "5", "L": "4", "M": "3", "D": "1"},
}

# Left stalk column (the stalk is 2 wide) and head top for each plant. Two big plants per
# block leave room for the leaves to droop instead of bridging from stalk to stalk.
PLANTS = ((3, 2), (10, 0))


def mirror(sprite: str) -> str:
    return "\n".join(line[::-1] for line in sprite.strip("\n").splitlines())


def head(kind: str) -> str:
    return "".join(HEAD_COLORS[kind].get(ch, ch) for ch in HEAD_SHAPE)


def young(sprite, positions):
    c = blank()
    for i, x in enumerate(positions):
        stamp_bottom(c, mirror(sprite) if i % 2 else sprite, x)
    return to_grid(c)


def plants(top_drop: int, height: int, kind: str | None):
    """Full plants: stalks `height` tall from the ground, leaves at nodes, optional head."""
    c = blank()
    for i, (x, top) in enumerate(PLANTS):
        stalk_top = 16 - height + top // 2
        if kind:
            stamp(c, head(kind), x - 1, stalk_top - 5 + top_drop)
        for yy in range(stalk_top, 16):
            c[yy][x], c[yy][x + 1] = "t", "r"          # lit left, shaded right
        # leaves alternate sides at nodes up the stalk
        # (both plants start on the right, so leaves in the middle sit at different heights)
        for n, row in enumerate(range(stalk_top + 2, 14, 3)):
            right = n % 2 == 0
            stamp(c, LEAF if right else mirror(LEAF), x + 2 if right else x - 5, row)
    return to_grid(c)


TEXTURES = {
    "sorghum_stage0": young(SPROUT, (1, 7, 12)),
    "sorghum_stage1": young(FAN, (0, 6, 11)),
    "sorghum_stage2": plants(0, 7, None),
    "sorghum_stage3": plants(0, 10, None),
    "sorghum_stage4": plants(0, 11, None),
    "sorghum_stage5": plants(1, 11, "green"),
    "sorghum_stage6": plants(0, 11, "turning"),
    "sorghum_stage7": plants(0, 11, "ripe"),
}

COMPARE = ["block/wheat_stage7", "block/sweet_berry_bush_stage3"]
