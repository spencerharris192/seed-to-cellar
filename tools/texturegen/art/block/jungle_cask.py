"""Jungle cask: the oak cask's design (cellar.py) in jungle's pinkish tan, with the same iron hoops."""
from texturegen.compose import sibling
from texturegen.palettes import IRON, WOOD_JUNGLE

cellar = sibling(__file__, "cellar")

LEGEND = {**dict(zip("012345", WOOD_JUNGLE)), **dict(zip("ijklmn", IRON))}

TEXTURES = {
    "jungle_cask_side": cellar.rows(cellar.cask_side),
    "jungle_cask_head": cellar.rows(cellar.cask_head),
    "jungle_cask_side_charred": cellar.charred(cellar.rows(cellar.cask_side)),
    "jungle_cask_head_charred": cellar.charred(cellar.rows(cellar.cask_head)),
}

COMPARE = ["block/jungle_planks", "block/barrel_side"]
