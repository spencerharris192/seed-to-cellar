"""Orange sapling: jungle-wood stem, little oranges (see _sapling.py)."""
from texturegen.compose import sibling
from texturegen.palettes import FOLIAGE_FRESH, ORANGE, ROOT

sapling = sibling(__file__, "_sapling")

LEGEND = {
    "t": ROOT[1], "T": ROOT[3],
    **dict(zip("asdfg", FOLIAGE_FRESH[0:5])),
    "x": ORANGE[2], "y": ORANGE[4],
}

GRID = sapling.draw()

COMPARE = ["block/oak_sapling", "block/cherry_sapling", "block/birch_sapling"]
