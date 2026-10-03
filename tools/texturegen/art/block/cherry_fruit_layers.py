"""Cherry tree leaves (layers over the tinted leaves): white blossom, green cherries, then pairs of dark red cherries on their stalks."""
from texturegen.compose import sibling
from texturegen.palettes import BLOSSOM, HOP_CONE, ROOT, FRUIT_RED

fruit = sibling(__file__, "_fruit_leaves")

LEGEND = {
    "p": BLOSSOM[3], "q": BLOSSOM[2], "y": BLOSSOM[4],
    "u": HOP_CONE[3], "v": HOP_CONE[2], "w": ROOT[1],
    "L": FRUIT_RED[3], "R": FRUIT_RED[2], "r": FRUIT_RED[1], "d": FRUIT_RED[0],
}

RIPE = """
.w.
w.w
L.R
d.d
"""

TEXTURES = fruit.textures("cherry", RIPE)
