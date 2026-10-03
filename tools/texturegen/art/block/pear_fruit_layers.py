"""Pear tree leaves (layers over the tinted leaves): white blossom, green pears, then golden pears, narrow at the top."""
from texturegen.compose import sibling
from texturegen.palettes import BLOSSOM, HOP_CONE, ROOT, PEAR

fruit = sibling(__file__, "_fruit_leaves")

LEGEND = {
    "p": BLOSSOM[3], "q": BLOSSOM[2], "y": BLOSSOM[4],
    "u": HOP_CONE[3], "v": HOP_CONE[2], "w": ROOT[1],
    "L": PEAR[5], "R": PEAR[3], "r": PEAR[2], "d": PEAR[1],
}

RIPE = """
w.
R.
LR
rd
"""

TEXTURES = fruit.textures("pear", RIPE)
