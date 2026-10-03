"""Cabbage crop, 4 growth looks (crop model: the texture shows on 4 crossed planes).

Pale, waxy blue-green leaves. Young cabbage is an open rosette; the ripe plant has a firm
round head in the middle, veined and lit from the top-left, cupped by big outer leaves
spreading low on both sides. One big plant per texture.
"""
from texturegen.compose import plants, to_grid
from texturegen.palettes import FOLIAGE_CABBAGE

# q-y = FOLIAGE_CABBAGE (dark -> light)
LEGEND = dict(zip("qwerty", FOLIAGE_CABBAGE))

SPROUT = """
y.y
.t.
.r.
"""

YOUNG = """
y.....y
ty.y.yt
.tytyt.
..rtr..
"""

MID = """
.yy.....yy.
yttt.y.ttty
.yttyyytty.
..rttttr...
...rrtr....
"""

# Ripe: the head outlined in dark green and shaded like a ball (lit top-left), with darker
# outer leaves flaring low on both sides.
RIPE = """
.....eyyye.....
....eytyyte....
...eytyytyte...
..yetyytttre.y.
.ytrerttttre.ty
ytte.errrre.rty
.yttr.....rtty.
..rtr.....rtr..
"""

TEXTURES = {
    "cabbage_stage0": to_grid(plants(SPROUT, (5, 11))),
    "cabbage_stage1": to_grid(plants(YOUNG, (8,))),
    "cabbage_stage2": to_grid(plants(MID, (8,))),
    "cabbage_stage3": to_grid(plants(RIPE, (8,))),
}

COMPARE = ["block/beetroots_stage3", "block/carrots_stage3", "block/potatoes_stage3"]
