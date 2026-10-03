"""Dark oak cask: the oak cask's design (cellar.py) in dark oak's near-black brown, with the same iron hoops."""
from texturegen.compose import sibling
from texturegen.palettes import IRON, WOOD_DARK_OAK

cellar = sibling(__file__, "cellar")

LEGEND = {**dict(zip("012345", WOOD_DARK_OAK)), **dict(zip("ijklmn", IRON))}

TEXTURES = {
    "dark_oak_cask_side": cellar.rows(cellar.cask_side),
    "dark_oak_cask_head": cellar.rows(cellar.cask_head),
    "dark_oak_cask_side_charred": cellar.charred(cellar.rows(cellar.cask_side)),
    "dark_oak_cask_head_charred": cellar.charred(cellar.rows(cellar.cask_head)),
}

COMPARE = ["block/dark_oak_planks", "block/barrel_side"]
