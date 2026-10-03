"""Spruce cask: the oak cask's design (cellar.py) in spruce's dark, resinous brown, with the same iron hoops."""
from texturegen.compose import sibling
from texturegen.palettes import IRON, WOOD_SPRUCE

cellar = sibling(__file__, "cellar")

LEGEND = {**dict(zip("012345", WOOD_SPRUCE)), **dict(zip("ijklmn", IRON))}

TEXTURES = {
    "spruce_cask_side": cellar.rows(cellar.cask_side),
    "spruce_cask_head": cellar.rows(cellar.cask_head),
    "spruce_cask_side_charred": cellar.charred(cellar.rows(cellar.cask_side)),
    "spruce_cask_head_charred": cellar.charred(cellar.rows(cellar.cask_head)),
}

COMPARE = ["block/spruce_planks", "block/barrel_side"]
