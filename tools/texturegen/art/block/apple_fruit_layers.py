"""Apple tree leaves (layers over the tinted leaves): pink-white blossom, small green apples, then red apples."""
from texturegen.compose import sibling
from texturegen.palettes import BLOSSOM, HOP_CONE, ROOT, FRUIT_RED

fruit = sibling(__file__, "_fruit_leaves")

LEGEND = {
    "p": BLOSSOM[2], "q": BLOSSOM[1], "y": BLOSSOM[4],
    "u": HOP_CONE[3], "v": HOP_CONE[2], "w": ROOT[1],
    "L": FRUIT_RED[4], "R": FRUIT_RED[3], "r": FRUIT_RED[2], "d": FRUIT_RED[1],
}

RIPE = """
.w.
LRr
Rrd
"""

TEXTURES = fruit.textures("apple", RIPE)
