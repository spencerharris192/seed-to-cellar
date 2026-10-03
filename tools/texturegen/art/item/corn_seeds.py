"""Corn seeds: dried kernels, scattered like the other seeds.

Each kernel is a little wedge: a pale, flat crown on top narrowing to a darker point, in
corn yellow. Wedges read apart from barley's ovals, rye's slivers and sorghum's red beads.
"""
from texturegen.compose import blank, stamp, to_grid
from texturegen.palettes import CORN

LEGEND = {str(i): CORN[i] for i in range(6)}

KERNEL = """
545
432
.2.
"""

SPOTS = ((10, 1), (4, 3), (11, 6), (2, 8), (7, 9), (12, 11), (4, 12))


def _build():
    c = blank()
    for x, y in SPOTS:
        stamp(c, KERNEL, x, y)
    return to_grid(c)


GRID = _build()

COMPARE = ["item/wheat_seeds", "item/pumpkin_seeds", "item/melon_seeds"]
