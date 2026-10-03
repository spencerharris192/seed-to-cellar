"""Red grape vine on a trellis, growth stages 0-5. Drawn on the trellis plane (transparent elsewhere)
like the hop bine, so stacked trellises join into one continuous vine.

A gnarled woody trunk (thicker at the foot) climbs the middle, with curling tendrils and broad lobed
leaves. 0 a woody stub in leaf, 1 climbing, 2 reaching the top, 3 leafy (resting after a picking),
4 flowering (little pale green clusters), 5 ripe bunches hanging: the "ready" signal.
white_grape_crop.py reuses this drawing with pale green-gold bunches.
"""
from texturegen.compose import blank, recolor, stamp, to_grid
from texturegen.palettes import FOLIAGE_CROP, HOP_CONE, RED_GRAPE, ROOT

# w W trunk (dark, lit side); a-h leaves (FOLIAGE_CROP dark -> light); n b flowers; 1-5 grapes (dark -> bloom)
LEGEND = {
    "w": ROOT[1], "W": ROOT[2],
    # leaves a shade deeper than young crops: a vine in full leaf
    **dict(zip("asdfgh", [FOLIAGE_CROP[0], FOLIAGE_CROP[0], FOLIAGE_CROP[1], FOLIAGE_CROP[2], FOLIAGE_CROP[3], FOLIAGE_CROP[4]])),
    "n": HOP_CONE[4], "b": HOP_CONE[3],
    **dict(zip("12345", RED_GRAPE[1:6])),
}

# Woody trunk, bottom to top (x, y); enters and leaves at column 7.
TRUNK = [(7, 15), (7, 14), (7, 13), (8, 12), (8, 11), (8, 10), (7, 9), (7, 8), (7, 7), (8, 6), (8, 5), (8, 4),
         (7, 3), (7, 2), (7, 1), (7, 0)]
FOOT = [(8, 15), (8, 14), (6, 15)]          # the old, thick base of the vine
TENDRILS = [(9, 12), (10, 11), (10, 10), (6, 8), (5, 7), (5, 6), (9, 4), (10, 3)]

# Broad, lobed grape leaves (two notches along the top), facing right and left.
LEAF_R = """
.h.h.
hghgh
fgggf
.fgf.
..d..
"""
LEAF_L = "\n".join(line[::-1] for line in LEAF_R.strip("\n").splitlines())

SMALL_R = """
gh
d.
"""
SMALL_L = """
hg
.d
"""

FLOWERS = """
nb
.b
"""

# A hanging bunch: stalk, then berries packed in a narrowing cone, lit from the top-left.
BUNCH = """
.w.
453
342
232
.2.
.1.
"""

LEAVES_LOW = [(LEAF_L, 2, 10), (SMALL_R, 10, 13)]
LEAVES_MID = [(LEAF_R, 9, 6), (LEAF_L, 2, 7)]
LEAVES_HIGH = [(LEAF_L, 2, 1), (LEAF_R, 9, 0), (SMALL_R, 11, 5)]
BLOSSOMS = [(FLOWERS, 5, 12), (FLOWERS, 10, 9), (FLOWERS, 4, 5), (FLOWERS, 11, 2)]
BUNCHES = [(BUNCH, 4, 11), (BUNCH, 10, 9), (BUNCH, 3, 5), (BUNCH, 11, 2)]


def draw(trunk_top: int, placements, tendrils: bool = True) -> str:
    c = blank()
    for x, y in TRUNK:
        if y >= trunk_top:
            c[y][x] = "w" if y > 11 else "W"
    for x, y in FOOT:
        c[y][x] = "w"
    if tendrils:
        for x, y in TENDRILS:
            if y >= trunk_top:
                c[y][x] = "d"
    for sprite, x, y in placements:
        stamp(c, sprite, x, y)
    return to_grid(c)


def stages(prefix: str) -> dict[str, str]:
    return {
        f"{prefix}_stage0": draw(11, [(SMALL_R, 9, 12), (SMALL_L, 5, 12)], tendrils=False),
        f"{prefix}_stage1": draw(6, LEAVES_LOW + [(SMALL_R, 9, 7)]),
        f"{prefix}_stage2": draw(0, LEAVES_LOW + LEAVES_MID + [(SMALL_L, 5, 2)]),
        f"{prefix}_stage3": draw(0, LEAVES_LOW + LEAVES_MID + LEAVES_HIGH),
        f"{prefix}_stage4": draw(0, LEAVES_LOW + LEAVES_MID + LEAVES_HIGH + BLOSSOMS),
        f"{prefix}_stage5": draw(0, LEAVES_LOW + LEAVES_MID + LEAVES_HIGH + BUNCHES),
    }


TEXTURES = stages("red_grape")

COMPARE = ["block/sweet_berry_bush_stage3", "block/vine", "block/cave_vines_lit"]
