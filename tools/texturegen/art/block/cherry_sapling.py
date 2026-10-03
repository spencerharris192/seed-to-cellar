"""Cherry sapling: reddish cherry-wood stem, dark red cherries (see _sapling.py)."""
from texturegen.compose import sibling
from texturegen.palettes import FOLIAGE_CROP, FRUIT_RED, MALT_BLACK

sapling = sibling(__file__, "_sapling")

LEGEND = {
    "t": MALT_BLACK[3], "T": MALT_BLACK[5],
    **dict(zip("asdfg", FOLIAGE_CROP[0:5])),
    "x": FRUIT_RED[1], "y": FRUIT_RED[3],
}

GRID = sapling.draw()

COMPARE = ["block/oak_sapling", "block/cherry_sapling", "block/birch_sapling"]
