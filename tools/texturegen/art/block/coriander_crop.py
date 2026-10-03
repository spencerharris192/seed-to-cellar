"""Coriander crop, 4 growth looks (crop model: the texture shows on 4 crossed planes).

Low mounds of small, round-lobed leaves (like flat parsley). When ripe it bolts: thin
stems rise from the leaves carrying small, flat clusters of white flowers with a faint
pink tint. Three plants per texture. Anise is the taller cousin with bigger cream umbels.
"""
from texturegen.compose import plants, to_grid
from texturegen.palettes import FLOUR, FOLIAGE_FRESH, GARLIC

# q-y = FOLIAGE_FRESH (green, dark -> light), 0-5 = FLOUR (white flowers), p = pink tint
LEGEND = {**dict(zip("qwerty", FOLIAGE_FRESH)), **{str(i): FLOUR[i] for i in range(6)}, "p": GARLIC[3]}

SPROUT = """
y.y
.r.
"""

YOUNG = """
y.y.y
tyt.t
.trt.
..r..
"""

MID = """
.y.y.
ytyty
tyrty
.ttrt
..r..
"""

RIPE = """
.5p5..
..e..5
..e.5p
y.ey.e
tyeyte
tyrtt.
.ttrt.
..r...
"""

TEXTURES = {
    "coriander_stage0": to_grid(plants(SPROUT, (3, 8, 13))),
    "coriander_stage1": to_grid(plants(YOUNG, (3, 8, 13))),
    "coriander_stage2": to_grid(plants(MID, (3, 8, 13))),
    "coriander_stage3": to_grid(plants(RIPE, (3, 8, 13))),
}

COMPARE = ["block/carrots_stage3", "block/potatoes_stage3"]
