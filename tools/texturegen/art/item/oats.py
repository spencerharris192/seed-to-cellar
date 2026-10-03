"""Oats (item): a sprig of hanging oat bells.

A thin, darker stem runs up the diagonal and nods over at the tip; pale spikelets hang
from it on short stalks, each a lit top over a shaded bottom. Reads as "oats" by the
dangling bells, where barley, rye and wheat are solid ears.
"""
from texturegen.compose import blank, stamp, to_grid
from texturegen.palettes import OAT

LEGEND = {str(i): OAT[i] for i in range(6)}

BELL = """
2.
54
32
"""


def _build():
    c = blank()
    for y in range(2, 15):                 # the stem, bottom-left to top-right
        c[y][14 - y] = "1" if y % 2 else "2"
    c[1][13], c[1][14], c[2][14] = "2", "2", "1"     # nodding tip
    for x, y in ((13, 3), (10, 5), (7, 8), (4, 11)):  # bells hang off the lower-right side
        stamp(c, BELL, x, y)
    return to_grid(c)


GRID = _build()

COMPARE = ["item/wheat", "item/wheat_seeds"]
