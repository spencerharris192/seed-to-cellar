"""The warped Bar Counter's and Bar Stool's wood (see _bar.py)."""
from texturegen.compose import sibling
from texturegen.palettes import WOOD_WARPED

bar = sibling(__file__, "_bar")

LEGEND = {**dict(zip("012345", WOOD_WARPED))}

TEXTURES = {
    "bar_warped_planks": bar.planks(),
    "bar_warped_trim": bar.trim(),
    "bar_warped_panel": bar.panel(),
}

COMPARE = ["block/warped_planks"]
