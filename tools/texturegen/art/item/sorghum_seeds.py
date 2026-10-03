"""Sorghum seeds: small round rust-red beads.

Sorghum grain is round, not oval, so the seeds are little beads with a copper highlight on
the top-left, in a loose cluster. Round red dots read apart from every other grain's ovals.
"""
from texturegen.compose import blank, stamp, to_grid
from texturegen.palettes import SORGHUM

LEGEND = {str(i): SORGHUM[i] for i in range(6)}

BEAD = """
54
31
"""

SPOTS = ((10, 2), (4, 3), (7, 6), (12, 7), (2, 9), (6, 11), (10, 12), (13, 11))


def _build():
    c = blank()
    for x, y in SPOTS:
        stamp(c, BEAD, x, y)
    return to_grid(c)


GRID = _build()

COMPARE = ["item/wheat_seeds", "item/beetroot_seeds", "item/melon_seeds"]
