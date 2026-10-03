"""Lemon sapling: jungle-wood stem, bright lemons (see _sapling.py)."""
from texturegen.compose import sibling
from texturegen.palettes import FOLIAGE_FRESH, LEMON, ROOT

sapling = sibling(__file__, "_sapling")

LEGEND = {
    "t": ROOT[1], "T": ROOT[3],
    **dict(zip("asdfg", FOLIAGE_FRESH[0:5])),
    "x": LEMON[2], "y": LEMON[4],
}

GRID = sapling.draw()

COMPARE = ["block/oak_sapling", "block/cherry_sapling", "block/birch_sapling"]
