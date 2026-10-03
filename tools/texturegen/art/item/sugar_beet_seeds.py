"""Sugar beet seeds: knobbly brown seed balls (beet 'seeds' are corky clusters).

Irregular lumps, lit on the top-left, scattered like the other seeds.
"""
from texturegen.compose import blank, stamp, to_grid
from texturegen.palettes import ROOT

LEGEND = {str(i): ROOT[i] for i in range(6)}

SEED = """
34.
432
.21
"""

SPOTS = ((10, 1), (3, 3), (11, 7), (2, 9), (7, 10), (11, 12))


def _build():
    c = blank()
    for x, y in SPOTS:
        stamp(c, SEED, x, y)
    return to_grid(c)


GRID = _build()

COMPARE = ["item/beetroot_seeds", "item/wheat_seeds"]
