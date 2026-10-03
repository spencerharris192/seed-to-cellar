"""Birch cask: the oak cask's design (cellar.py) in birch's pale cream, with the same iron hoops."""
from texturegen.compose import sibling
from texturegen.palettes import IRON, WOOD_BIRCH

cellar = sibling(__file__, "cellar")

LEGEND = {**dict(zip("012345", WOOD_BIRCH)), **dict(zip("ijklmn", IRON))}

TEXTURES = {
    "birch_cask_side": cellar.rows(cellar.cask_side),
    "birch_cask_head": cellar.rows(cellar.cask_head),
    "birch_cask_side_charred": cellar.charred(cellar.rows(cellar.cask_side)),
    "birch_cask_head_charred": cellar.charred(cellar.rows(cellar.cask_head)),
}

COMPARE = ["block/birch_planks", "block/barrel_side"]
