"""Hop bine on a trellis, growth stages 0-5. Drawn on the trellis plane (transparent
elsewhere) and rendered in front of and behind the trellis panel.

The twining stem enters at the bottom edge and leaves at the top edge at the same
column, so stacked trellis blocks join into one continuous vine.
0 sprout, 1 climbing, 2 reaching the top, 3 leafy (resting after harvest),
4 flowering (pale burrs), 5 ripe cones hanging - the clear "ready" signal.
"""
from texturegen.compose import blank, stamp, to_grid
from texturegen.palettes import FOLIAGE_FRESH, HOP_CONE

# letters a-h: FOLIAGE_FRESH dark -> light; z-n: HOP_CONE dark -> light
LEGEND = {**dict(zip("asdfgh", FOLIAGE_FRESH)), **dict(zip("zxcvbn", HOP_CONE))}

# Twining stem, bottom to top (x, y).
STEM = [(7, 15), (7, 14), (8, 13), (8, 12), (7, 11), (7, 10), (8, 9), (8, 8),
        (7, 7), (7, 6), (8, 5), (8, 4), (7, 3), (7, 2), (8, 1), (8, 0)]

LEAF_R = """
.gh.
fghg
.df.
"""
LEAF_L = "\n".join(line[::-1] for line in LEAF_R.strip("\n").splitlines())

SMALL_R = """
gh
f.
"""
SMALL_L = """
hg
.f
"""

BURR = """
.n
nb
"""

CONE = """
nb
bv
vc
.x
"""

# (sprite, x, y) placements, added stage by stage.
LEAVES_LOW = [(LEAF_L, 3, 11), (SMALL_R, 9, 13)]
LEAVES_MID = [(LEAF_R, 9, 7), (SMALL_L, 5, 8)]
LEAVES_HIGH = [(LEAF_L, 3, 3), (LEAF_R, 9, 0), (SMALL_R, 9, 4)]
BURRS = [(BURR, 5, 9), (BURR, 10, 10), (BURR, 4, 5), (BURR, 11, 3)]
CONES = [(CONE, 5, 9), (CONE, 10, 10), (CONE, 4, 5), (CONE, 11, 3), (CONE, 5, 0)]


def draw(stem_top, placements):
    c = blank()
    for x, y in STEM:
        if y >= stem_top:
            c[y][x] = "d" if y > 11 else "f"
    for sprite, x, y in placements:
        stamp(c, sprite, x, y)
    return to_grid(c)


TEXTURES = {
    "hops_stage0": draw(12, [(SMALL_R, 9, 12), (SMALL_L, 5, 13)]),
    "hops_stage1": draw(7, LEAVES_LOW + [(SMALL_R, 9, 8)]),
    "hops_stage2": draw(0, LEAVES_LOW + LEAVES_MID + [(SMALL_L, 5, 2)]),
    "hops_stage3": draw(0, LEAVES_LOW + LEAVES_MID + LEAVES_HIGH),
    "hops_stage4": draw(0, LEAVES_LOW + LEAVES_MID + LEAVES_HIGH + BURRS),
    "hops_stage5": draw(0, LEAVES_LOW + LEAVES_MID + LEAVES_HIGH + CONES),
}
