"""Acacia cask: the oak cask's design (cellar.py) in acacia's bright orange, with the same iron hoops."""
from texturegen.compose import sibling
from texturegen.palettes import IRON, WOOD_ACACIA

cellar = sibling(__file__, "cellar")

LEGEND = {**dict(zip("012345", WOOD_ACACIA)), **dict(zip("ijklmn", IRON))}

TEXTURES = {
    "acacia_cask_side": cellar.rows(cellar.cask_side),
    "acacia_cask_head": cellar.rows(cellar.cask_head),
    "acacia_cask_side_charred": cellar.charred(cellar.rows(cellar.cask_side)),
    "acacia_cask_head_charred": cellar.charred(cellar.rows(cellar.cask_head)),
}

COMPARE = ["block/acacia_planks", "block/barrel_side"]
