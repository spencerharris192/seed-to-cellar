"""The spruce Bar Counter's and Bar Stool's wood (see _bar.py)."""
from texturegen.compose import sibling
from texturegen.palettes import WOOD_SPRUCE

bar = sibling(__file__, "_bar")

LEGEND = {**dict(zip("012345", WOOD_SPRUCE))}

TEXTURES = {
    "bar_spruce_planks": bar.planks(),
    "bar_spruce_trim": bar.trim(),
    "bar_spruce_panel": bar.panel(),
}

COMPARE = ["block/spruce_planks"]
