"""Oat seeds: long, pointed, pale hulled grains, scattered like barley seeds.

Oat grains keep their papery hull: cream, slim and pointed at both ends, lying on the
diagonal. The palest seeds in the mod, so they read apart from barley's gold.
"""
from texturegen.compose import blank, stamp, to_grid
from texturegen.palettes import OAT

LEGEND = {str(i): OAT[i] for i in range(6)}

GRAIN = """
...5
..54
.543
.32.
2...
"""

SPOTS = ((9, 1), (3, 3), (10, 7), (1, 8), (8, 11))


def _build():
    c = blank()
    for x, y in SPOTS:
        stamp(c, GRAIN, x, y)
    return to_grid(c)


GRID = _build()

COMPARE = ["item/wheat_seeds", "item/beetroot_seeds"]
