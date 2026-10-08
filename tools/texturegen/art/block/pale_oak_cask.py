"""Pale oak cask: the oak cask's design (cellar.py) in pale oak's blush white, with the same iron hoops."""
from texturegen.compose import sibling
from texturegen.palettes import IRON, WOOD_PALE_OAK

cellar = sibling(__file__, "cellar")

LEGEND = {**dict(zip("012345", WOOD_PALE_OAK)), **dict(zip("ijklmn", IRON))}

TEXTURES = {
    "pale_oak_cask_side": cellar.rows(cellar.cask_side),
    "pale_oak_cask_head": cellar.rows(cellar.cask_head),
    "pale_oak_cask_side_charred": cellar.charred(cellar.rows(cellar.cask_side)),
    "pale_oak_cask_head_charred": cellar.charred(cellar.rows(cellar.cask_head)),
}

COMPARE = ["block/pale_oak_planks", "block/barrel_side"]
