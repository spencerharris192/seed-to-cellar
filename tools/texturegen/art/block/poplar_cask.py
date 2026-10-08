"""Poplar cask: the oak cask's design (cellar.py) in poplar's grey taupe, with the same iron hoops."""
from texturegen.compose import sibling
from texturegen.palettes import IRON, WOOD_POPLAR

cellar = sibling(__file__, "cellar")

LEGEND = {**dict(zip("012345", WOOD_POPLAR)), **dict(zip("ijklmn", IRON))}

TEXTURES = {
    "poplar_cask_side": cellar.rows(cellar.cask_side),
    "poplar_cask_head": cellar.rows(cellar.cask_head),
    "poplar_cask_side_charred": cellar.charred(cellar.rows(cellar.cask_side)),
    "poplar_cask_head_charred": cellar.charred(cellar.rows(cellar.cask_head)),
}

COMPARE = ["block/poplar_planks", "block/barrel_side"]
