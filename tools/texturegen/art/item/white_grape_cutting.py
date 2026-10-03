"""White grape cutting: the red grape cutting with a green-gold dot on its tag."""
from texturegen.compose import sibling
from texturegen.palettes import FLOUR, FOLIAGE_CROP, ROOT, WHITE_GRAPE

red = sibling(__file__, "red_grape_cutting")

LEGEND = {
    "w": ROOT[1], "W": ROOT[3], "x": ROOT[4],
    "g": FOLIAGE_CROP[3], "h": FOLIAGE_CROP[4],
    "p": FLOUR[2], "P": FLOUR[4],
    "t": ROOT[2],
    "1": WHITE_GRAPE[2], "2": WHITE_GRAPE[4],
}

GRID = red.GRID

COMPARE = red.COMPARE
