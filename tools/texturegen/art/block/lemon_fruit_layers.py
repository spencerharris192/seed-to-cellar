"""Lemon tree leaves (layers over the tinted leaves): white blossom, green lemons, then bright yellow lemons."""
from texturegen.compose import sibling
from texturegen.palettes import BLOSSOM, HOP_CONE, ROOT, LEMON

fruit = sibling(__file__, "_fruit_leaves")

LEGEND = {
    "p": BLOSSOM[3], "q": BLOSSOM[2], "y": BLOSSOM[4],
    "u": HOP_CONE[3], "v": HOP_CONE[2], "w": ROOT[1],
    "L": LEMON[5], "R": LEMON[3], "r": LEMON[2], "d": LEMON[1],
}

RIPE = """
.w.
LRR
rRd
"""

TEXTURES = fruit.textures("lemon", RIPE)
