"""Garlic crop, 4 growth looks (crop model: the texture shows on 4 crossed planes).

Flat, strap-like leaves in dusty green that arch outward (onion leaves are round tubes and
stand straighter). When ripe, the lower leaves brown and the white bulb, streaked with
purple, shows at the soil. Three plants per texture.
"""
from texturegen.compose import plants, recolor, to_grid
from texturegen.palettes import FOLIAGE_DUSTY, GARLIC

# digits = GARLIC (white bulb, purple shadows; dark -> light), q-y = FOLIAGE_DUSTY
LEGEND = {**{str(i): GARLIC[i] for i in range(6)}, **dict(zip("qwerty", FOLIAGE_DUSTY))}

SPROUT = """
y.
.t
.r
"""

YOUNG = """
y...
.t.y
.tt.
..r.
"""

MID = """
y....y
.t..t.
.ty.t.
..tty.
..tt..
..rt..
..r...
"""

RIPE = """
y....y
.t..t.
..tt..
.ytty.
.rtr..
.4554.
35542.
.1321.
"""

# Garlic leaves sit one shade darker than onion's, so the flat straps hold against the sky.
DARKER = {"y": "t", "t": "r", "r": "e"}

TEXTURES = {
    f"garlic_stage{i}": to_grid(plants(recolor(sprite, DARKER), (3, 8, 13)))
    for i, sprite in enumerate((SPROUT, YOUNG, MID, RIPE))
}

COMPARE = ["block/carrots_stage3", "block/potatoes_stage3"]
