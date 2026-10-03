"""The cherry Bar Counter's and Bar Stool's wood (see _bar.py)."""
from texturegen.compose import sibling
from texturegen.palettes import WOOD_CHERRY

bar = sibling(__file__, "_bar")

LEGEND = {**dict(zip("012345", WOOD_CHERRY))}

TEXTURES = {
    "bar_cherry_planks": bar.planks(),
    "bar_cherry_trim": bar.trim(),
    "bar_cherry_panel": bar.panel(),
}

COMPARE = ["block/cherry_planks"]
