"""The acacia Bar Counter's and Bar Stool's wood (see _bar.py)."""
from texturegen.compose import sibling
from texturegen.palettes import WOOD_ACACIA

bar = sibling(__file__, "_bar")

LEGEND = {**dict(zip("012345", WOOD_ACACIA))}

TEXTURES = {
    "bar_acacia_planks": bar.planks(),
    "bar_acacia_trim": bar.trim(),
    "bar_acacia_panel": bar.panel(),
}

COMPARE = ["block/acacia_planks"]
