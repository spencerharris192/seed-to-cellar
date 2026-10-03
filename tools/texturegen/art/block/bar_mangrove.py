"""The mangrove Bar Counter's and Bar Stool's wood (see _bar.py)."""
from texturegen.compose import sibling
from texturegen.palettes import WOOD_MANGROVE

bar = sibling(__file__, "_bar")

LEGEND = {**dict(zip("012345", WOOD_MANGROVE))}

TEXTURES = {
    "bar_mangrove_planks": bar.planks(),
    "bar_mangrove_trim": bar.trim(),
    "bar_mangrove_panel": bar.panel(),
}

COMPARE = ["block/mangrove_planks"]
