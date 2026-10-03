"""Ginger crop, 4 growth looks (crop model: the texture shows on 4 crossed planes).

Reed-like stems with narrow leaves stepping up them in alternating pairs, like a small
bamboo: nothing else in the mod grows like that. When ripe, the knobbly tan rhizome swells
out of the soil at the base. Two clumps per texture.
"""
from texturegen.compose import blank, plants, stamp_bottom, to_grid
from texturegen.palettes import FOLIAGE_FRESH, ROOT

# digits = ROOT (tan rhizome, dark -> light), q-y = FOLIAGE_FRESH (green, dark -> light)
LEGEND = {**{str(i): ROOT[i] for i in range(6)}, **dict(zip("qwerty", FOLIAGE_FRESH))}

SPROUT = """
.y
yt
.r
"""

YOUNG = """
y...
.t.y
..tt
y.t.
.tt.
..r.
"""

MID = """
y.....
.t...y
..t.t.
y.tt..
.ttt.y
..t.t.
y.tt..
.tt...
..r...
"""

RIPE_STEMS = """
y....y
.t..t.
..tt..
y.t..y
.tt.t.
..tt..
y.t...
.tt.y.
..tt..
..rt..
"""

RHIZOME = """
.4.45.
3443432
2321221
"""


def ripe():
    c = plants(RIPE_STEMS, (4, 11), bottom=12)
    for cx in (4, 11):
        stamp_bottom(c, RHIZOME, cx - 3, 15)
    return to_grid(c)


TEXTURES = {
    "ginger_stage0": to_grid(plants(SPROUT, (4, 11))),
    "ginger_stage1": to_grid(plants(YOUNG, (4, 11))),
    "ginger_stage2": to_grid(plants(MID, (4, 11))),
    "ginger_stage3": ripe(),
}

COMPARE = ["block/carrots_stage3", "block/sugar_cane"]
