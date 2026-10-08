"""The poplar Bar Counter's and Bar Stool's wood (see _bar.py)."""
from texturegen.compose import sibling
from texturegen.palettes import WOOD_POPLAR

bar = sibling(__file__, "_bar")

LEGEND = {**dict(zip("012345", WOOD_POPLAR))}

TEXTURES = {
    "bar_poplar_planks": bar.planks(),
    "bar_poplar_trim": bar.trim(),
    "bar_poplar_panel": bar.panel(),
}

COMPARE = ["block/poplar_planks"]
