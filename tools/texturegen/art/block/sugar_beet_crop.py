"""Sugar beet crop, 4 growth looks (crop model: the texture shows on 4 crossed planes).

A rosette of broad, spoon-shaped glossy leaves on pale stalks; when ripe, the fat cream
shoulder of the root shows at the soil. Two plants per texture. Reads apart from vanilla
beetroot (red veins, red root) by its cream root and plainer green leaves.
"""
from texturegen.compose import plants, to_grid
from texturegen.palettes import FOLIAGE_CROP, SUGAR_BEET

# digits = SUGAR_BEET (cream root), q-y = FOLIAGE_CROP (green, dark -> light)
LEGEND = {**{str(i): SUGAR_BEET[i] for i in range(6)}, **dict(zip("qwerty", FOLIAGE_CROP))}

SPROUT = """
y.y
.t.
.r.
"""

YOUNG = """
y...y
ty.yt
.trt.
..r..
"""

MID = """
yy.y.yy
yttytty
.ttrtt.
..ttr..
..rtr..
...r...
"""

RIPE = """
yy.y.yy
yttytty
ytrttry
.ttrtt.
..t4r..
.r454r.
.24542.
..232..
"""

TEXTURES = {
    "sugar_beet_stage0": to_grid(plants(SPROUT, (4, 11))),
    "sugar_beet_stage1": to_grid(plants(YOUNG, (4, 11))),
    "sugar_beet_stage2": to_grid(plants(MID, (4, 11))),
    "sugar_beet_stage3": to_grid(plants(RIPE, (4, 11))),
}

COMPARE = ["block/beetroots_stage3", "block/carrots_stage3", "block/potatoes_stage3"]
