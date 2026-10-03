"""Pear sapling: pale birch stem, golden pears (see _sapling.py)."""
from texturegen.compose import sibling
from texturegen.palettes import FLOUR, FOLIAGE_CROP, PEAR

sapling = sibling(__file__, "_sapling")

LEGEND = {
    "t": FLOUR[1], "T": FLOUR[3],
    **dict(zip("asdfg", FOLIAGE_CROP[0:5])),
    "x": PEAR[2], "y": PEAR[4],
}

GRID = sapling.draw()

COMPARE = ["block/oak_sapling", "block/cherry_sapling", "block/birch_sapling"]
