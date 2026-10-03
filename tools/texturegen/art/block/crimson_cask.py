"""Crimson cask: the oak cask's design (cellar.py) in crimson stem's plum-magenta, with the same iron hoops."""
from texturegen.compose import sibling
from texturegen.palettes import IRON, WOOD_CRIMSON

cellar = sibling(__file__, "cellar")

LEGEND = {**dict(zip("012345", WOOD_CRIMSON)), **dict(zip("ijklmn", IRON))}

TEXTURES = {
    "crimson_cask_side": cellar.rows(cellar.cask_side),
    "crimson_cask_head": cellar.rows(cellar.cask_head),
}

COMPARE = ["block/crimson_planks", "block/barrel_side"]
