"""The crimson Bar Counter's and Bar Stool's wood (see _bar.py)."""
from texturegen.compose import sibling
from texturegen.palettes import WOOD_CRIMSON

bar = sibling(__file__, "_bar")

LEGEND = {**dict(zip("012345", WOOD_CRIMSON))}

TEXTURES = {
    "bar_crimson_planks": bar.planks(),
    "bar_crimson_trim": bar.trim(),
    "bar_crimson_panel": bar.panel(),
}

COMPARE = ["block/crimson_planks"]
