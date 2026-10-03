"""Peach sapling: grey acacia stem, blushing peaches (see _sapling.py)."""
from texturegen.compose import sibling
from texturegen.palettes import FOLIAGE_FRESH, PEACH, STONE

sapling = sibling(__file__, "_sapling")

LEGEND = {
    "t": STONE[1], "T": STONE[3],
    **dict(zip("asdfg", FOLIAGE_FRESH[0:5])),
    "x": PEACH[2], "y": PEACH[4],
}

GRID = sapling.draw()

COMPARE = ["block/oak_sapling", "block/cherry_sapling", "block/birch_sapling"]
