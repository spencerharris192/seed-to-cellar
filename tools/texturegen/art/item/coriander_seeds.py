"""Coriander seeds: small round beige husks, the spice as well as what you plant.

Little ribbed globes, lit on the top-left, in warm beige.
"""
from texturegen.compose import blank, stamp, to_grid
from texturegen.palettes import OAT

LEGEND = {str(i): OAT[i] for i in range(6)}

SEED = """
34.
452
.21
"""

SPOTS = ((10, 1), (4, 3), (11, 6), (2, 8), (7, 9), (12, 11), (4, 12))


def _build():
    c = blank()
    for x, y in SPOTS:
        stamp(c, SEED, x, y)
    return to_grid(c)


GRID = _build()

COMPARE = ["item/wheat_seeds", "item/pumpkin_seeds"]
