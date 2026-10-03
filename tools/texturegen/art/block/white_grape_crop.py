"""White grape vine on a trellis, growth stages 0-5: the red grape vine's drawing with pale
green-gold bunches (see red_grape_crop.py)."""
from texturegen.compose import sibling
from texturegen.palettes import FOLIAGE_CROP, HOP_CONE, ROOT, WHITE_GRAPE

red = sibling(__file__, "red_grape_crop")

LEGEND = {
    "w": ROOT[1], "W": ROOT[2],
    **dict(zip("asdfgh", [FOLIAGE_CROP[0], FOLIAGE_CROP[0], FOLIAGE_CROP[1], FOLIAGE_CROP[2], FOLIAGE_CROP[3], FOLIAGE_CROP[4]])),
    "n": HOP_CONE[4], "b": HOP_CONE[3],
    **dict(zip("12345", WHITE_GRAPE[1:6])),
}

TEXTURES = red.stages("white_grape")

COMPARE = red.COMPARE
