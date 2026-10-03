"""Pie icons: a whole pie in its tin seen from a little above, and a single slice. One base each,
filling recolored per fruit (the same filling letters as the placed pie).

Whole pie: an oval lattice top (crust strips crossing over the filling) with a crimped rim, and
the tin's side band below. Slice: a wedge with the lattice on top and the filling showing on its cut face.
"""
from texturegen.compose import recolor
from texturegen.palettes import BARLEY, BLACKBERRY, BLUEBERRY, FRUIT_RED, IRON, MALT_AMBER, OAT, PEACH, PEAR, PLUM

# 1-5 crust (deep bake -> highlight), t u tin; filling letters per fruit: x dark, y mid, z light
LEGEND = {
    "1": MALT_AMBER[1],
    "2": MALT_AMBER[2],
    "3": MALT_AMBER[3],
    "4": BARLEY[4],
    "5": BARLEY[5],
    "t": IRON[2],
    "u": IRON[3],
    **dict(zip("ABC", [OAT[2], OAT[4], OAT[5]])),   # apple: pale cooked apple, so the golden lattice shows
    **dict(zip("DEF", BLUEBERRY[1:4])),
    **dict(zip("GHI", BLACKBERRY[2:5])),
    **dict(zip("JKL", FRUIT_RED[1:4])),
    **dict(zip("MNO", FRUIT_RED[0:3])),   # cherry: darker than sweet berry
    **dict(zip("PQR", PLUM[1:4])),
    **dict(zip("STU", PEACH[1:4])),   # deeper than the golden crust
    **dict(zip("VWX", PEAR[2:5])),
}

FRUITS = {"apple": "ABC", "blueberry": "DEF", "blackberry": "GHI", "sweet_berry": "JKL",
          "cherry": "MNO", "plum": "PQR", "peach": "STU", "pear": "VWX"}

PIE = """
................
................
................
.....3454543....
...345xy4yx543..
..34yzy4yzxy43..
.345444544454543
.34yxy4yyx4yxy43
.343yy4zyy4yy343
..3454544454543.
..u33434343433t.
..tuuuuuuuuuutt.
...tttttttttt...
................
................
................
"""

SLICE = """
................
................
................
................
.........5......
........454.....
.......4y4z4....
......45454544..
.....4yz4yy4y4..
....454545454543
...zyyxyyzyxyyx3
...yxyyxyyxyyxy3
...2323232323232
....2222222222..
................
................
"""

TEXTURES = {}
for fruit, letters in FRUITS.items():
    colors = dict(zip("xyz", letters))
    TEXTURES[f"{fruit}_pie"] = recolor(PIE, colors)
    TEXTURES[f"{fruit}_pie_slice"] = recolor(SLICE, colors)

COMPARE = ["item/pumpkin_pie", "item/cake", "item/cookie"]
