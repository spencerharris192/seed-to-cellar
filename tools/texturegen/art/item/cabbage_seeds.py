"""Cabbage seeds: many tiny dark round seeds.

The smallest seeds in the mod: dark brown pinheads with one lit pixel, lots of them.
"""
from texturegen.compose import blank, stamp, to_grid
from texturegen.palettes import ROOT

LEGEND = {str(i): ROOT[i] for i in range(6)}

SEED = """
31
10
"""

SPOTS = ((11, 1), (5, 2), (8, 5), (2, 6), (12, 6), (5, 9), (10, 10), (2, 12), (7, 13), (12, 13))


def _build():
    c = blank()
    for x, y in SPOTS:
        stamp(c, SEED, x, y)
    return to_grid(c)


GRID = _build()

COMPARE = ["item/beetroot_seeds", "item/melon_seeds"]
