"""The dark_oak Bar Counter's and Bar Stool's wood (see _bar.py)."""
from texturegen.compose import sibling
from texturegen.palettes import WOOD_DARK_OAK

bar = sibling(__file__, "_bar")

LEGEND = {**dict(zip("012345", WOOD_DARK_OAK))}

TEXTURES = {
    "bar_dark_oak_planks": bar.planks(),
    "bar_dark_oak_trim": bar.trim(),
    "bar_dark_oak_panel": bar.panel(),
}

COMPARE = ["block/dark_oak_planks"]
