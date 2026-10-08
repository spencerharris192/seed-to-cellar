"""The pale oak Bar Counter's and Bar Stool's wood (see _bar.py)."""
from texturegen.compose import sibling
from texturegen.palettes import WOOD_PALE_OAK

bar = sibling(__file__, "_bar")

LEGEND = {**dict(zip("012345", WOOD_PALE_OAK))}

TEXTURES = {
    "bar_pale_oak_planks": bar.planks(),
    "bar_pale_oak_trim": bar.trim(),
    "bar_pale_oak_panel": bar.panel(),
}

COMPARE = ["block/pale_oak_planks"]
