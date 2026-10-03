"""Rye seeds: long, slim grey-gold grains, scattered like barley seeds.

Same layout as barley seeds so the seed family reads together, but each grain is thinner
and longer (rye kernels are slender) and cooler in color.
"""
from texturegen.compose import blank, stamp, to_grid
from texturegen.palettes import RYE

LEGEND = {str(i): RYE[i] for i in range(6)}

GRAIN = """
.5
54
43
32
2.
"""

# Top-left corners, matching the spread of barley seeds.
SPOTS = ((10, 1), (4, 3), (10, 7), (2, 8), (9, 11))


def _build():
    c = blank()
    for x, y in SPOTS:
        stamp(c, GRAIN, x, y)
    return to_grid(c)


GRID = _build()

COMPARE = ["item/wheat_seeds", "item/beetroot_seeds"]
