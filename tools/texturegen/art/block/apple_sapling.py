"""Apple sapling: oak-brown stem, a pair of red apples starting (see _sapling.py)."""
from texturegen.compose import sibling
from texturegen.palettes import FOLIAGE_CROP, FRUIT_RED, ROOT

sapling = sibling(__file__, "_sapling")

LEGEND = {
    "t": ROOT[1], "T": ROOT[3],
    **dict(zip("asdfg", FOLIAGE_CROP[0:5])),
    "x": FRUIT_RED[2], "y": FRUIT_RED[4],
}

GRID = sapling.draw()

COMPARE = ["block/oak_sapling", "block/cherry_sapling", "block/birch_sapling"]
