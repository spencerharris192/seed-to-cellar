"""The birch Bar Counter's and Bar Stool's wood (see _bar.py)."""
from texturegen.compose import sibling
from texturegen.palettes import WOOD_BIRCH

bar = sibling(__file__, "_bar")

LEGEND = {**dict(zip("012345", WOOD_BIRCH))}

TEXTURES = {
    "bar_birch_planks": bar.planks(),
    "bar_birch_trim": bar.trim(),
    "bar_birch_panel": bar.panel(),
}

COMPARE = ["block/birch_planks"]
