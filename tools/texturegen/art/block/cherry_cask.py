"""Cherry cask: the oak cask's design (cellar.py) in cherry's blush pink, with the same iron hoops."""
from texturegen.compose import sibling
from texturegen.palettes import IRON, WOOD_CHERRY

cellar = sibling(__file__, "cellar")

LEGEND = {**dict(zip("012345", WOOD_CHERRY)), **dict(zip("ijklmn", IRON))}

TEXTURES = {
    "cherry_cask_side": cellar.rows(cellar.cask_side),
    "cherry_cask_head": cellar.rows(cellar.cask_head),
    "cherry_cask_side_charred": cellar.charred(cellar.rows(cellar.cask_side)),
    "cherry_cask_head_charred": cellar.charred(cellar.rows(cellar.cask_head)),
}

COMPARE = ["block/cherry_planks", "block/barrel_side"]
