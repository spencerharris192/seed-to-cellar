"""White grapes: the red grapes' bunch, in pale green-gold."""
from texturegen.compose import sibling
from texturegen.palettes import FOLIAGE_CROP, ROOT, WHITE_GRAPE

red = sibling(__file__, "red_grapes")

LEGEND = {
    **dict(zip("012345", WHITE_GRAPE)),
    "w": ROOT[1], "W": ROOT[2],
    **dict(zip("dfgh", FOLIAGE_CROP[1:5])),
}

GRID = red.GRID

COMPARE = red.COMPARE
