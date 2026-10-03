"""Anise seeds: small curved grey-green seeds.

Short crescents in a dusty grey-green, lit on the upper edge. Greener and shorter than rye's
slim grey-gold grains.
"""
from texturegen.compose import blank, stamp, to_grid
from texturegen.palettes import FOLIAGE_DUSTY

LEGEND = dict(zip("qwerty", FOLIAGE_DUSTY))

SEED = """
.t
ty
re
"""

SPOTS = ((11, 1), (4, 2), (8, 5), (13, 7), (2, 8), (7, 10), (11, 12), (3, 13))


def _build():
    c = blank()
    for x, y in SPOTS:
        stamp(c, SEED, x, y)
    return to_grid(c)


GRID = _build()

COMPARE = ["item/wheat_seeds", "item/pumpkin_seeds"]
