"""Helper for berry items (not a texture): little round berries piled in a cluster.

Each berry is drawn in digits 1-5 of the item's fruit ramp (5 = highlight, lit top-left) with
a darker crown dot; cluster() places them so they overlap like a handful of picked berries.
"""
from texturegen.compose import blank, stamp, to_grid

BERRY = """
.45.
4543
3432
.21.
"""

SMALL = """
45.
432
.1.
"""

# A loose handful: back row first so front berries overlap them.
HANDFUL = ((7, 3), (3, 5), (10, 6), (6, 7), (2, 9), (9, 10), (5, 11))


def cluster(sprite: str = BERRY, spots=HANDFUL, stem: tuple | None = None) -> str:
    c = blank()
    if stem:
        for x, y, ch in stem:
            c[y][x] = ch
    for x, y in spots:
        stamp(c, sprite, x, y)
    return to_grid(c)
