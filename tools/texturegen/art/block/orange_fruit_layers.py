"""Orange tree leaves (layers over the tinted leaves): white blossom, green oranges, then round orange oranges."""
from texturegen.compose import sibling
from texturegen.palettes import BLOSSOM, HOP_CONE, ROOT, ORANGE

fruit = sibling(__file__, "_fruit_leaves")

LEGEND = {
    "p": BLOSSOM[3], "q": BLOSSOM[2], "y": BLOSSOM[4],
    "u": HOP_CONE[3], "v": HOP_CONE[2], "w": ROOT[1],
    "L": ORANGE[4], "R": ORANGE[3], "r": ORANGE[2], "d": ORANGE[1],
}

RIPE = """
.w.
LRR
Rrd
"""

TEXTURES = fruit.textures("orange", RIPE)
