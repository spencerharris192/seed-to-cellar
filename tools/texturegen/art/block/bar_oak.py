"""The oak Bar Counter's and Bar Stool's wood (see _bar.py)."""
from texturegen.compose import sibling
from texturegen.palettes import WOOD_OAK

bar = sibling(__file__, "_bar")

LEGEND = {**dict(zip("012345", WOOD_OAK))}

TEXTURES = {
    "bar_oak_planks": bar.planks(),
    "bar_oak_trim": bar.trim(),
    "bar_oak_panel": bar.panel(),
}

COMPARE = ["block/oak_planks"]
