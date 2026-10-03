"""Olive sapling: grey stem, silvery leaves, dark olives (see _sapling.py)."""
from texturegen.compose import sibling
from texturegen.palettes import FOLIAGE_DUSTY, OLIVE, STONE

sapling = sibling(__file__, "_sapling")

LEGEND = {
    "t": STONE[1], "T": STONE[3],
    **dict(zip("asdfg", FOLIAGE_DUSTY[0:5])),
    "x": OLIVE[1], "y": OLIVE[3],
}

GRID = sapling.draw()

COMPARE = ["block/oak_sapling", "block/cherry_sapling", "block/birch_sapling"]
