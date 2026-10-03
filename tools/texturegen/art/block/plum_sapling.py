"""Plum sapling: dark stem, little purple plums (see _sapling.py)."""
from texturegen.compose import sibling
from texturegen.palettes import FOLIAGE_CROP, PLUM, ROOT

sapling = sibling(__file__, "_sapling")

LEGEND = {
    "t": ROOT[1], "T": ROOT[3],
    **dict(zip("asdfg", FOLIAGE_CROP[0:5])),
    "x": PLUM[2], "y": PLUM[4],
}

GRID = sapling.draw()

COMPARE = ["block/oak_sapling", "block/cherry_sapling", "block/birch_sapling"]
