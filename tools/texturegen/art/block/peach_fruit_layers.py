"""Peach tree leaves (layers over the tinted leaves): pink blossom, green peaches, then apricot peaches with a red blush."""
from texturegen.compose import sibling
from texturegen.palettes import BLOSSOM, HOP_CONE, ROOT, PEACH

fruit = sibling(__file__, "_fruit_leaves")

LEGEND = {
    "p": BLOSSOM[1], "q": BLOSSOM[0], "y": BLOSSOM[4],
    "u": HOP_CONE[3], "v": HOP_CONE[2], "w": ROOT[1],
    "L": PEACH[4], "R": PEACH[3], "r": PEACH[2], "d": PEACH[1],
}

RIPE = """
.w.
LRR
rRd
"""

TEXTURES = fruit.textures("peach", RIPE)
