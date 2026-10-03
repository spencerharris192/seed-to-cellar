"""Apple Crown Whiskey, the one drink drawn all its own (not on the shared spirit bottle).

- apple_crown_whiskey: a plump apple of clear glass (widest high up, rounding in below), its two lobes rising round the neck like a stem from the dimple, a
  green leaf off the neck, full of apple-gold whiskey (lit at the top left, deepening low right, a glint down the glass's
  left). A little deep red label on its middle, banded in gold, a gold crown-jewel at its center. On the neck, for a stopper, a
  gold crown: three points round red velvet, an orb on top, its band set with two rubies and an emerald.
- apple_crown_whiskey_crowned: the secret six-star bottle: the leaf turned gold (a golden apple), the label gold banded in
  red with a red jewel, and sparkles round it (the game adds the enchanted shimmer).
"""
from texturegen.palettes import FOLIAGE_FRESH, FRUIT_RED, GLASS, GOLD, MALT_AMBER

# A-F gold; r-w red; K k m n o p whiskey amber; L-P leaf green; e h j i glass
LEGEND = {
    **dict(zip("ABCDEF", GOLD)),
    **dict(zip("rstuvw", FRUIT_RED)),
    **dict(zip("Kkmnop", MALT_AMBER)),
    **dict(zip("LMNOP", FOLIAGE_FRESH[1:6])),
    "e": GLASS[0], "h": GLASS[1], "j": GLASS[2], "i": GLASS[3],
}

APPLE_CROWN_WHISKEY = """
....E..F..E.....
....DtDEDtD.....
....EFEEEFE.....
....CuCOCuC.....
....BCCCCCB.OP..
......eieLMNO...
...ee.eie.ee....
..eijeeieejje...
.eippooooooone..
.eipooooooonne..
.eiooEEEEEonne..
.eioDvuFutCnne..
..eoDuuuttCme...
..eonCCCCCmme...
...eonnmmmke....
....eeeeeee.....
"""

# Crowned: a golden leaf, a gold label banded in red with a red crown, and sparkles in the air round it.
APPLE_CROWN_WHISKEY_CROWNED = """
.F..E..F..E.....
....DtDEDtD...F.
....EFEEEFE.....
....CuCOCuC.....
....BCCCCCB.EF..
......eieBCDE...
...ee.eie.ee...F
..eijeeieejje...
.eippooooooone..
.eipooooooonne..
.eioouuuuuonne..
.eiotEEtEDsnne..
..eotEEEDDsme...
..eonsssssmme...
...eonnmmmke..F.
....eeeeeee.....
"""

TEXTURES = {
    "apple_crown_whiskey": APPLE_CROWN_WHISKEY,
    "apple_crown_whiskey_crowned": APPLE_CROWN_WHISKEY_CROWNED,
}

COMPARE = ["item/golden_apple", "item/apple", "item/honey_bottle"]
