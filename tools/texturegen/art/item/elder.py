"""Elder items: the two harvests of one bush.

- elderflowers: a flat, lacy head of tiny cream flowers on branching green stems
- elderberries: a drooping cluster of tiny purple-black berries on red-brown stems
"""
from texturegen.compose import blank, stamp, to_grid
from texturegen.palettes import BLACKBERRY, FOLIAGE_FRESH, OAT, ROOT

# 0-5 BLACKBERRY, a b c d u v OAT (cream flowers), q-y FOLIAGE_FRESH (stems), m/n ROOT (berry stems)
LEGEND = {
    **{str(i): BLACKBERRY[i] for i in range(6)},
    **dict(zip("abcduv", OAT)),
    **dict(zip("qwerty", FOLIAGE_FRESH)),
    "m": ROOT[2], "n": ROOT[3],
}

FLOWERS = """
................
...v.u.v.u.v....
..uvdvuvdvuvu...
...udcudcudc....
....r..r..r.....
.....r.r.r......
......rrr.......
.......r........
.......e........
......e.........
.....e..........
....e...........
...e............
................
................
................
"""

BERRY = """
43
21
"""


def elderberries():
    c = blank()
    stems = ((3, 13, "m"), (4, 12, "m"), (5, 11, "n"), (6, 10, "n"), (7, 9, "n"), (7, 8, "m"), (8, 7, "n"),
             (9, 6, "m"), (6, 7, "n"), (5, 6, "m"), (10, 8, "n"), (11, 9, "m"))
    for x, y, ch in stems:
        c[y][x] = ch
    for x, y in ((9, 2), (11, 4), (7, 3), (4, 4), (3, 7), (11, 10), (12, 7), (5, 8), (9, 9)):
        stamp(c, BERRY, x, y)
    return to_grid(c)


TEXTURES = {
    "elderflowers": FLOWERS,
    "elderberries": elderberries(),
}

COMPARE = ["item/sweet_berries", "item/glow_berries"]
