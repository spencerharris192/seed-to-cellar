"""Olive tree leaves (layers over the tinted leaves): tiny cream blossom, green olives, then clusters of dark olives."""
from texturegen.compose import sibling
from texturegen.palettes import BLOSSOM, HOP_CONE, ROOT, OLIVE

fruit = sibling(__file__, "_fruit_leaves")

LEGEND = {
    "p": BLOSSOM[3], "q": BLOSSOM[2], "y": BLOSSOM[4],
    "u": HOP_CONE[3], "v": HOP_CONE[2], "w": ROOT[1],
    "L": OLIVE[3], "R": OLIVE[2], "r": OLIVE[1], "d": OLIVE[0],
}

RIPE = """
w.w
L.R
d.d
"""

TEXTURES = fruit.textures("olive", RIPE)
