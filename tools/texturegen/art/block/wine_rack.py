"""Wine Rack: an oak frame with six round-cornered holes in two rows. Racked bottles lie in the holes, neck out;
the renderer draws them from wine_rack_bottle (dark bottle glass) and wine_rack_foil (grey, tinted with the wine's
color), taking each face's pixels from the top-left corner. Sides and top are plain boards with the same dark edge,
so racks built into a wall read as separate cubbies.

The holes sit at x 1-4, 6-9, 11-14 and y 3-6, 9-12 of the front: WineryRenderers.RACK_X / RACK_Y must match.

rack_board and rack_back: the boards and back panel of the Bottle Shelf and the Wine Display.
"""
from texturegen.palettes import BOTTLE_GLASS, LIQUID, WOOD_DARK_OAK, WOOD_OAK

# 0-5 oak; p q the deep shadow inside a hole; a-f bottle glass; u-z foil greys
LEGEND = {
    **dict(zip("012345", WOOD_OAK)),
    **dict(zip("pq", WOOD_DARK_OAK)),
    **dict(zip("abcdef", BOTTLE_GLASS)),
    **dict(zip("uvwxyz", LIQUID)),
}

HOLES_X = [(1, 4), (6, 9), (11, 14)]
HOLES_Y = [(3, 6), (9, 12)]
RAILS = {1: "4", 2: "3", 7: "4", 8: "3", 13: "4", 14: "3"}   # lit top edge, shaded lower edge


def rows(fn):
    return "\n".join("".join(fn(x, y) for x in range(16)) for y in range(16))


def hole_at(x, y):
    for hx in HOLES_X:
        for hy in HOLES_Y:
            if hx[0] <= x <= hx[1] and hy[0] <= y <= hy[1]:
                return hx, hy
    return None


def front(x, y):
    if x in (0, 15) or y in (0, 15):
        return "1"                                   # dark outer edge
    hole = hole_at(x, y)
    if hole:
        (x0, x1), (y0, y1) = hole
        if x in (x0, x1) and y in (y0, y1):
            return "0"                               # rounded corners
        return "p" if y == y0 else "q"               # deep inside, darkest under the rail above
    if y in RAILS:
        if RAILS[y] == "4" and hole_at(x, y - 1):
            return "5"                               # the lit lip under each hole
        return "2" if (x * 7 + y) % 11 == 0 else RAILS[y]   # a little grain
    return "3" if x in (5, 10) else "4"              # the posts between the holes


def side(x, y):
    if x in (0, 15) or y in (0, 15):
        return "1"
    if y % 5 == 0:
        return "2"                                   # seams between the boards
    return "4" if y % 5 == 1 else ("2" if (x * 5 + y) % 13 == 0 else "3")


def top(x, y):
    if x in (0, 15) or y in (0, 15):
        return "1"
    if x % 5 == 0:
        return "2"
    return "4" if x % 5 == 1 else ("2" if (x + y * 5) % 13 == 0 else "3")


# Racked-bottle parts use each face's top-left pixels: the shoulder's 4x4 front is the largest face.
BOTTLE = """
dcdc............
cccb............
ccbb............
cbbb............
""" + "\n".join("c" * 16 for _ in range(12))
BOTTLE = "\n".join(row.replace(".", "c") for row in BOTTLE.strip().splitlines())

FOIL = """
zyyx............
yyxw............
yxww............
xwwv............
""" + "\n".join("x" * 16 for _ in range(12))
FOIL = "\n".join(row.replace(".", "x") for row in FOIL.strip().splitlines())

def board(x, y):
    """The Bottle Shelf's and Wine Display's boards: oak planks running across, lit along each board's top edge,
    their joints staggered board by board."""
    if y % 4 == 3:
        return "1"                                   # the seam under each board
    if x == (y // 4 * 5 + 3) % 16:
        return "2"                                   # a joint between two lengths
    if y % 4 == 0:
        return "5" if x % 7 else "4"                 # the lit top edge
    return "2" if (x * 3 + y * 7) % 13 == 0 else ("4" if y % 4 == 1 else "3")


def back(x, y):
    """The back panel behind the bottles: darker boards running up, in shadow."""
    if x % 5 == 4:
        return "0"                                   # gaps between the boards
    return "1" if (x * 7 + y * 3) % 11 == 0 else ("3" if x % 5 == 0 else "2")


TEXTURES = {
    "rack_board": rows(board),
    "rack_back": rows(back),
    "wine_rack_front": rows(front),
    "wine_rack_side": rows(side),
    "wine_rack_top": rows(top),
    "wine_rack_bottle": BOTTLE,
    "wine_rack_foil": FOIL,
}

COMPARE = ["block/chiseled_bookshelf_empty", "block/barrel_side", "block/oak_planks"]
