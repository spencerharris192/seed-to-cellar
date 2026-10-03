"""Mint, 4 looks: planted, cut back (a small clump), growing, full (a lush clump of paired,
rounded leaves with a few pale lilac flower spikes). Bright, fresh green.
"""
from texturegen.compose import plants, to_grid
from texturegen.palettes import FOLIAGE_FRESH, LAVENDER

# q-y = FOLIAGE_FRESH, a = LAVENDER (pale lilac flowers)
LEGEND = {**dict(zip("qwerty", FOLIAGE_FRESH)), "a": LAVENDER[4]}

SPROUT = """
y.y
tyt
.e.
"""

YOUNG = """
.y.y.y.
ytytyty
.tetet.
..e.e..
...e...
"""

MID = """
..y.y..y...
.ytyty.yty.
ytytetytyty
.teyty.tet.
yty.e.ytyt.
.tetetete..
..e.e.e....
...eee.....
"""

FULL = """
...a.....a...
..yay.y.yay..
.ytytyty.yty.
ytyteytytytey
.tetytetytet.
ytytetyty.yty
.teyty.tetyt.
..etetetete..
...e.e.e.e...
....eeeee....
"""

TEXTURES = {f"mint_stage{i}": to_grid(plants(s, (8,))) for i, s in enumerate((SPROUT, YOUNG, MID, FULL))}

COMPARE = ["block/sweet_berry_bush_stage1", "block/fern"]
