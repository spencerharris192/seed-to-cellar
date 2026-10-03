"""Corn (item): an ear of corn with its husk peeled back.

A plump diagonal ear, five pixels across: a lit upper-left edge, rows of yellow kernels,
a dark lower-right edge. Green husk leaves hug both sides of its lower half and flare out
at the base. The universal "corn" icon; nothing else in the mod is a yellow grid.
"""
from texturegen.compose import blank, to_grid
from texturegen.palettes import CORN, FOLIAGE_CROP

# digits = CORN (dark -> light), q-y = FOLIAGE_CROP (husk green, dark -> light)
LEGEND = {**{str(i): CORN[i] for i in range(6)}, **dict(zip("qwerty", FOLIAGE_CROP))}


def _build():
    c = blank()

    def put(x, y, ch):
        if 0 <= x < 16 and 0 <= y < 16:
            c[y][x] = ch

    for y in range(1, 11):                    # the ear climbs to the top-right
        s = 12 - y
        if y == 1:                            # rounded tip
            put(s + 1, y, "5"), put(s + 2, y, "4"), put(s + 3, y, "2")
            continue
        put(s, y, "5")                        # lit edge
        put(s + 1, y, "4")
        put(s + 2, y, "4" if (s + y) % 2 else "3")   # kernel rows
        put(s + 3, y, "3")
        put(s + 4, y, "1")                    # shaded edge
    put(11, 0, "5")
    # husk leaves hugging the lower half of the ear, flaring out at the base
    for y in range(6, 12):
        s = 12 - y
        put(s - 1, y, "y" if y < 9 else "t")                 # upper-left leaf
        put(s + 5, y + 1, "t" if y < 9 else "r")             # lower-right leaf
    for x, y, ch in ((1, 12, "t"), (0, 13, "r"), (3, 12, "e"), (2, 13, "e"), (1, 14, "w"),
                     (5, 13, "r"), (6, 13, "e"), (4, 14, "e"), (5, 14, "w")):
        put(x, y, ch)
    return to_grid(c)


GRID = _build()

COMPARE = ["item/carrot", "item/wheat", "item/golden_carrot"]
