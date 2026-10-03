"""Orchard fruit (the apple is vanilla's). Round fruit are shaded domes (the _shapes helper, lit from the
top-left like vanilla foods), each with its own telltale detail:

- cherries: two dark red cherries on long stalks joined at the top, with a leaf.
- plum: an upright oval, deep purple with a dusty bloom highlight, stalk and leaf.
- peach: round and blushing, with the soft crease down one side and a leaf.
- pear: the pear shape (small top on a wide bottom), golden green, curved stalk.
- lemon: an oval with pointed ends lying on the diagonal, bright yellow.
- orange: round, deep orange with a pitted skin, a small leaf at the stalk.
- olives: a sprig with narrow silvery leaves and three small olives.
"""
from texturegen.compose import sibling
from texturegen.palettes import FOLIAGE_CROP, FOLIAGE_DUSTY, FRUIT_RED, LEMON, OLIVE, ORANGE, PEACH, PEAR, PLUM, ROOT

shapes = sibling(__file__, "_shapes")

LEGEND = {
    "w": ROOT[1], "W": ROOT[2],
    "k": FOLIAGE_CROP[1], "l": FOLIAGE_CROP[3], "m": FOLIAGE_CROP[4],
    **dict(zip("ABCDEF", PLUM)),
    **dict(zip("GHIJKL", PEACH)),
    **dict(zip("MNOPQR", PEAR)),
    **dict(zip("STUVXY", LEMON)),
    **dict(zip("abcdef", ORANGE)),
    # cherries: a deep crimson, darker than the coral-red tomatoes (one shade repeated keeps them dark)
    **dict(zip("012345", [FRUIT_RED[0], FRUIT_RED[1], FRUIT_RED[1], FRUIT_RED[2], FRUIT_RED[3], FRUIT_RED[4]])),
    **dict(zip("ghij", OLIVE[0:4])),        # ripe (dark) olives
    **dict(zip("pqrs", OLIVE[2:6])),        # a green one
    "n": FOLIAGE_DUSTY[2], "o": FOLIAGE_DUSTY[4],
}

LEAF = """
.ml
mlk
lk.
"""


def paint(g, shape_grid) -> None:
    for y in range(16):
        for x in range(16):
            if shape_grid[y][x] != ".":
                g[y][x] = shape_grid[y][x]


def blank():
    return [["."] * 16 for _ in range(16)]


def stamp(g, sprite: str, x: int, y: int) -> None:
    for dy, row in enumerate(sprite.strip("\n").splitlines()):
        for dx, ch in enumerate(row):
            if ch != ".":
                g[y + dy][x + dx] = ch


def plum() -> str:
    g = blank()
    paint(g, shapes.shade(shapes.dome(8.0, 9.4, 4.6, 5.4, angle=-15, height=1.0), "ABCDE"))
    shapes.put(g, [(6, 6), (7, 5), (6, 7)], "F")                  # the dusty bloom
    for x, y in [(8, 4), (9, 3), (9, 2)]:
        g[y][x] = "w"
    stamp(g, LEAF, 10, 1)
    return shapes.to_str(g)


def peach() -> str:
    g = blank()
    paint(g, shapes.shade(shapes.dome(8.0, 9.2, 5.6, 5.3, height=1.0), "GHIJK"))
    shapes.put(g, [(11, 11), (12, 10), (11, 12), (10, 12), (12, 11)], "H", only_on="IJK")   # the red blush
    shapes.put(g, [(9, 5), (9, 6), (10, 7), (10, 8), (10, 9)], "I", only_on="JKL")          # the crease
    shapes.put(g, [(5, 6), (6, 5)], "L")
    g[4][8] = "w"
    stamp(g, LEAF, 9, 2)
    return shapes.to_str(g)


def pear() -> str:
    g = blank()
    paint(g, shapes.shade(shapes.dome(7.8, 10.6, 4.8, 4.5, height=1.0), "MNOPQ"))
    paint(g, shapes.shade(shapes.dome(8.2, 5.8, 2.6, 3.0, height=1.0), "MNOPQ"))
    for x, y in [(7, 9), (6, 8)]:                                   # blend the neck into the body
        if g[y][x] == "M":
            g[y][x] = "O"
    shapes.put(g, [(5, 9), (6, 10), (7, 5)], "R")
    for x, y in [(8, 3), (9, 2), (9, 1)]:
        g[y][x] = "w"
    return shapes.to_str(g)


def lemon() -> str:
    g = blank()
    paint(g, shapes.shade(shapes.dome(8.0, 8.4, 6.0, 4.2, angle=-35, height=0.9), "STUVX"))
    g[13][2] = "S"                                                  # the pointed tips
    g[3][13] = "S"
    shapes.put(g, [(6, 7), (7, 6), (8, 5)], "Y")
    return shapes.to_str(g)


def orange() -> str:
    g = blank()
    paint(g, shapes.shade(shapes.dome(8.0, 9.0, 5.6, 5.6, height=1.0), "abcde"))
    shapes.put(g, [(6, 8), (9, 7), (7, 11), (11, 10), (10, 12), (5, 10)], "c", only_on="de")   # pitted skin
    shapes.put(g, [(5, 6), (6, 5)], "f")
    g[4][8] = "w"
    stamp(g, LEAF, 8, 2)
    return shapes.to_str(g)


def cherries() -> str:
    g = blank()
    for x, y in [(5, 10), (5, 9), (6, 8), (6, 7), (7, 6), (7, 5), (8, 4), (9, 3)]:       # stalks up to the joint
        g[y][x] = "w"
    for x, y in [(10, 10), (10, 9), (10, 8), (10, 7), (10, 6), (9, 5), (9, 4)]:
        g[y][x] = "W"
    paint(g, shapes.shade(shapes.dome(5.4, 12.0, 2.8, 2.7, height=1.0), "01234"))
    paint(g, shapes.shade(shapes.dome(10.8, 12.4, 2.8, 2.7, height=1.0), "01234"))
    shapes.put(g, [(4, 11), (10, 11)], "5")
    stamp(g, LEAF, 10, 1)
    return shapes.to_str(g)


def olives() -> str:
    g = blank()
    for i in range(11):                                             # the twig
        g[14 - i][2 + i] = "W"
    for stroke in ([(5, 9), (4, 8), (3, 7)], [(8, 8), (8, 7), (9, 6)], [(10, 3), (11, 2), (12, 1)],
                   [(12, 5), (13, 5), (14, 6)], [(6, 13), (7, 13), (8, 14)]):   # narrow silvery leaves
        for i, (x, y) in enumerate(stroke):
            g[y][x] = "o" if i < 2 else "n"
    paint(g, shapes.shade(shapes.dome(6.4, 11.2, 2.2, 2.7, height=1.0), "ghij"))
    paint(g, shapes.shade(shapes.dome(11.8, 10.6, 2.2, 2.7, height=1.0), "ghij"))
    paint(g, shapes.shade(shapes.dome(9.6, 6.4, 2.1, 2.6, height=1.0), "pqrs"))
    return shapes.to_str(g)


TEXTURES = {
    "cherries": cherries(),
    "plum": plum(),
    "peach": peach(),
    "pear": pear(),
    "lemon": lemon(),
    "orange": orange(),
    "olives": olives(),
}

COMPARE = ["item/apple", "item/sweet_berries", "item/glow_berries"]
