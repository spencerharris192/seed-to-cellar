"""Mangrove cask: the oak cask's design (cellar.py) in mangrove's deep red, with the same iron hoops."""
from texturegen.compose import sibling
from texturegen.palettes import IRON, WOOD_MANGROVE

cellar = sibling(__file__, "cellar")

LEGEND = {**dict(zip("012345", WOOD_MANGROVE)), **dict(zip("ijklmn", IRON))}

TEXTURES = {
    "mangrove_cask_side": cellar.rows(cellar.cask_side),
    "mangrove_cask_head": cellar.rows(cellar.cask_head),
    "mangrove_cask_side_charred": cellar.charred(cellar.rows(cellar.cask_side)),
    "mangrove_cask_head_charred": cellar.charred(cellar.rows(cellar.cask_head)),
}

COMPARE = ["block/mangrove_planks", "block/barrel_side"]
