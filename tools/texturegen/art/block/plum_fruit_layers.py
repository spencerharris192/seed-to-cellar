"""Plum tree leaves (layers over the tinted leaves): white blossom, green plums, then purple plums with a dusty bloom."""
from texturegen.compose import sibling
from texturegen.palettes import BLOSSOM, HOP_CONE, ROOT, PLUM

fruit = sibling(__file__, "_fruit_leaves")

LEGEND = {
    "p": BLOSSOM[3], "q": BLOSSOM[2], "y": BLOSSOM[4],
    "u": HOP_CONE[3], "v": HOP_CONE[2], "w": ROOT[1],
    "L": PLUM[5], "R": PLUM[3], "r": PLUM[2], "d": PLUM[1],
}

RIPE = """
.w
LR
Rr
rd
"""

TEXTURES = fruit.textures("plum", RIPE)
