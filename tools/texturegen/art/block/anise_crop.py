"""Anise crop, 4 growth looks (crop model: the texture shows on 4 crossed planes).

Feathery, finely cut leaves on a taller plant than coriander. When ripe it carries broad,
flat umbrellas of creamy flowers going to seed on top of tall stems, the anise look.
Two plants per texture.
"""
from texturegen.compose import plants, to_grid
from texturegen.palettes import FOLIAGE_FRESH, OAT

# q-y = FOLIAGE_FRESH (green, dark -> light), 0-5 = OAT (cream umbels and seeds)
LEGEND = {**dict(zip("qwerty", FOLIAGE_FRESH)), **{str(i): OAT[i] for i in range(6)}}

SPROUT = """
y.y
.t.
.r.
"""

YOUNG = """
y.y.y
.tyt.
y.t.y
.trt.
..r..
"""

MID = """
..y..
y.t.y
.yty.
y.t.y
.ttt.
y.t.y
.trt.
..r..
"""

# Ripe: a wide cream umbel (with seeds setting, 3) on a tall stem over feathery leaves.
RIPE = """
.45454.
4535354
..eee..
...e...
y..e..y
.y.e.y.
..yty..
y.ttt.y
.yttty.
..trt..
...r...
"""

TEXTURES = {
    "anise_stage0": to_grid(plants(SPROUT, (4, 11))),
    "anise_stage1": to_grid(plants(YOUNG, (4, 11))),
    "anise_stage2": to_grid(plants(MID, (4, 11))),
    "anise_stage3": to_grid(plants(RIPE, (4, 11))),
}

COMPARE = ["block/carrots_stage3", "block/potatoes_stage3"]
