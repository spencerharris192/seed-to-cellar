"""Wormwood, 4 looks: planted, cut back, growing, full (a tall clump of finely cut, silvery
grey-green foliage dotted with tiny yellow flowers). The silver sheen sets it apart from
every green herb.
"""
from texturegen.compose import plants, to_grid
from texturegen.palettes import CORN, FLOUR, FOLIAGE_DUSTY

# q-y = FOLIAGE_DUSTY, S/U = FLOUR (silver sheen), a = CORN (tiny yellow flowers)
LEGEND = {**dict(zip("qwerty", FOLIAGE_DUSTY)), "S": FLOUR[3], "U": FLOUR[1], "a": CORN[4]}

SPROUT = """
y.y
SyS
.e.
"""

YOUNG = """
.y.y.y.
ySySySy
.tUtUt.
..e.e..
"""

MID = """
.y..y..y..
ySy.ySyySy
.ySySytyS.
ytyUtyUty.
.tetetet..
..e.e.e...
"""

FULL = """
.a...a...a...
.y.a.y.a.y...
ySy.ySy.ySy..
.y.ySy.ySy.y.
ySyty.ytySySy
.tySytySytyU.
ytyUy.tyUytyt
.tetet.tetet.
..e.e.e.e.e..
...e.e.e.e...
"""

TEXTURES = {f"wormwood_stage{i}": to_grid(plants(s, (8,))) for i, s in enumerate((SPROUT, YOUNG, MID, FULL))}

COMPARE = ["block/fern", "block/dead_bush"]
