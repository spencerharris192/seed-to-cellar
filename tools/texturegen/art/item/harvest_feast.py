"""Harvest Feast (item): a golden roast bird on the oak carving board the placed feast sits on,
drumsticks raised in front of its rounded back with the bone ends showing (the classic roast
silhouette), carrots and a dab of cranberry sauce on the side. Reads as "a whole dinner", apart from
the single-serving bowls; a dark outline keeps the bird off the board. (A plain shaded dome read as
a bun on a plate.)
"""
from texturegen.palettes import BARLEY, FIRE, FLOUR, FRUIT_RED, MALT_AMBER, WOOD_OAK

# k-o board (rim, dark -> light); a-e bird (outline, dark -> light); W j bone; s t carrot; x y sauce
LEGEND = {
    **dict(zip("klmno", WOOD_OAK[1:6])),
    **dict(zip("abcde", [MALT_AMBER[0], MALT_AMBER[2], MALT_AMBER[3], MALT_AMBER[4], BARLEY[5]])),
    "W": FLOUR[5], "j": FLOUR[2],
    **dict(zip("st", [FIRE[2], FIRE[3]])),
    **dict(zip("xy", [FRUIT_RED[1], FRUIT_RED[3]])),
}

# Drumsticks (a c b) rise from the front to the bone ends; the back (d e) shows between them.
GRID = """
................
................
..WW........WW..
..Wja.aaaa.aWj..
...acbcddcbca...
...acbdeedbca...
..aacbdeddbcaa..
.acdeeddddddcba.
.acdeedddddccba.
.acddddddddccba.
kacdddddddccbbak
kmaaccccccbbaamk
kmxyaaaaaaaatsmk
kmnnnnnnnnnnnnmk
.kkkkkkkkkkkkkk.
................
"""

COMPARE = ["item/cooked_chicken", "item/cake", "item/pumpkin_pie"]
