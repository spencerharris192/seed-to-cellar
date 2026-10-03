"""Rye (item): one long, slim spike on its stalk.

Where barley's item is a plump zigzag ear with long awns streaming off, rye is a straight,
narrow spike, longer, with a few short awns ticking up its lit side, in cooler grey-gold.
Light from the top-left: the upper-left edge is bright, a dark edge runs along the
lower-right so the spike holds its shape at 1x.
"""
from texturegen.compose import blank, to_grid
from texturegen.palettes import RYE

LEGEND = {str(i): RYE[i] for i in range(6)}


def _build():
    c = blank()
    for y in range(1, 11):                 # the spike climbs to the top-right
        x = 13 - y
        c[y][x] = "5" if y % 2 else "4"    # lit edge (kernel highlights alternate)
        c[y][x + 1] = "3"
        if x + 2 < 16:
            c[y][x + 2] = "1"              # shaded edge
        if y % 3 == 1 and y > 1:           # a few short awns up the lit side
            c[y - 1][x - 1] = "4"
    c[0][13] = "5"
    for i, (x, y) in enumerate(((2, 11), (2, 12), (1, 13), (1, 14))):
        c[y][x], c[y][x + 1] = ("3", "1") if i % 2 == 0 else ("2", "1")   # the stalk
    return to_grid(c)


GRID = _build()

COMPARE = ["item/wheat", "item/wheat_seeds"]
