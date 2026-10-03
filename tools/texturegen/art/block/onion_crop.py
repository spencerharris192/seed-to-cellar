"""Onion crop, 4 growth looks (crop model: the texture shows on 4 crossed planes).

Hollow, tube-like leaves in dusty blue-green, standing up in thin fans. When ripe the tops
flop over and turn straw-yellow, and the round golden-brown bulb sits half out of the soil.
Three plants per texture.
"""
from texturegen.compose import plants, to_grid
from texturegen.palettes import FOLIAGE_DUSTY, ONION

# digits = ONION (bulb skin, dark -> light), q-y = FOLIAGE_DUSTY (blue-green, dark -> light)
LEGEND = {**{str(i): ONION[i] for i in range(6)}, **dict(zip("qwerty", FOLIAGE_DUSTY))}

SPROUT = """
y
t
r
"""

YOUNG = """
y..
t.y
t.t
.tt
.r.
"""

MID = """
y...y
t..y.
t.yt.
.tt.t
.tt..
..tt.
..tr.
..r..
"""

# Ripe: tops fallen over and drying (5/4 straw), the bulb sitting on the soil.
RIPE = """
5.....
.4...5
..44t.
..tt4.
.3553.
34554.
23443.
.212..
"""

TEXTURES = {
    "onion_stage0": to_grid(plants(SPROUT, (3, 8, 13))),
    "onion_stage1": to_grid(plants(YOUNG, (3, 8, 13))),
    "onion_stage2": to_grid(plants(MID, (3, 8, 13))),
    "onion_stage3": to_grid(plants(RIPE, (3, 8, 13))),
}

COMPARE = ["block/carrots_stage3", "block/potatoes_stage3"]
