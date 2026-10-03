"""Warped cask: the oak cask's design (cellar.py) in warped stem's teal, with the same iron hoops."""
from texturegen.compose import sibling
from texturegen.palettes import IRON, WOOD_WARPED

cellar = sibling(__file__, "cellar")

LEGEND = {**dict(zip("012345", WOOD_WARPED)), **dict(zip("ijklmn", IRON))}

TEXTURES = {
    "warped_cask_side": cellar.rows(cellar.cask_side),
    "warped_cask_head": cellar.rows(cellar.cask_head),
}

COMPARE = ["block/warped_planks", "block/barrel_side"]
