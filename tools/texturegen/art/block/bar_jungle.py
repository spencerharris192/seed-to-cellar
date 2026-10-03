"""The jungle Bar Counter's and Bar Stool's wood (see _bar.py)."""
from texturegen.compose import sibling
from texturegen.palettes import WOOD_JUNGLE

bar = sibling(__file__, "_bar")

LEGEND = {**dict(zip("012345", WOOD_JUNGLE))}

TEXTURES = {
    "bar_jungle_planks": bar.planks(),
    "bar_jungle_trim": bar.trim(),
    "bar_jungle_panel": bar.panel(),
}

COMPARE = ["block/jungle_planks"]
