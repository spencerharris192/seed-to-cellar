"""Black Forest Cake (models block/black_forest_cake and _bite1-5, built in datagen). The cake is 8 pixels tall, so its
sides sample rows 8-15 of these sheets (row 8 the top edge, row 15 the foot):

- black_forest_cake_side: whipped cream all round, dark chocolate shavings pressed into its lower half.
- black_forest_cake_inner: the cut face: cream coating at the top and at both edges (columns 2 and 13), then dark
  chocolate sponge, a layer of cream studded with kirsch cherries, sponge, a thin cream layer, sponge at the foot.
- black_forest_cake_top: cream, chocolate shavings heaped in the middle.
- black_forest_cake_bottom: the sponge's dark underside.
- black_forest_cake_cream / _cherry: the rosettes on top and the cherry on each (small parts: they sample the top-left).
"""
from texturegen.palettes import FLOUR, FRUIT_RED, MALT_BLACK

# c C W cream (shade, mid, bright); 1-5 chocolate (dark to light); r R g cherry (deep, red, glint)
LEGEND = {
    "c": FLOUR[2], "C": FLOUR[3], "W": FLOUR[5],
    **{str(i): MALT_BLACK[i] for i in range(1, 6)},
    "r": FRUIT_RED[1], "R": FRUIT_RED[2], "g": FRUIT_RED[4],
}


def rows(fn):
    return "\n".join("".join(fn(x, y) for x in range(16)) for y in range(16))


def cream(x, y):
    return "W" if (x * 3 + y * 5) % 11 else "C"


def side(x, y):
    # a band of chocolate shavings pressed round the foot, its top edge ragged, a darker curl here and there
    if y >= 13 or (y == 12 and x % 3 != 1):
        return "1" if (x * 3 + y) % 7 == 0 else "3"
    return cream(x, y)


def inner(x, y):
    if y <= 8 or x in (2, 13):
        return "W" if y <= 8 else "C"            # the cream coating: top and both edges
    if y == 11:
        return "R" if x % 3 == 1 else ("g" if x % 6 == 4 else "C")   # cream with cherries
    if y == 14:
        return "C"                               # a thin cream layer
    return "2" if (x + y) % 4 else "3"           # chocolate sponge


def top(x, y):
    if 5 <= x <= 10 and 5 <= y <= 10 and (x * 5 + y * 7) % 4 != 0:
        return "1" if (x + 2 * y) % 3 == 0 else ("4" if (x + y) % 2 else "2")   # shavings heaped in the middle
    return cream(x, y)


def bottom(x, y):
    return "2" if (x + y) % 3 else "1"


def cherry(x, y):
    # each cherry is a pixel cube showing the sheet's top-left: deep red there, a glint just beside
    if x < 2 and y < 2:
        return "R"
    if (x, y) == (2, 0):
        return "g"
    return "R" if (x + y) % 3 else "r"


TEXTURES = {
    "black_forest_cake_side": rows(side),
    "black_forest_cake_inner": rows(inner),
    "black_forest_cake_top": rows(top),
    "black_forest_cake_bottom": rows(bottom),
    "black_forest_cake_cream": rows(lambda x, y: "W" if (x + y) % 3 else "C"),
    "black_forest_cake_cherry": rows(cherry),
}

COMPARE = ["block/cake_side", "block/cake_top", "block/cake_inner"]
