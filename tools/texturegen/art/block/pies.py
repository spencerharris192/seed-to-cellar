"""Placed pies: one design, fruit color by recolor (GDD section 20: "pie: one base, fruit color").

The pie block is 12x12 pixels and 4 tall (block pixels 2-14), with automatic UVs, so only part
of each texture shows:
- <fruit>_pie_top: columns/rows 2-13: a crimped crust rim and a lattice of crust strips over
  the filling, with a few whole fruits showing between the strips.
- pie_side (shared): rows 12-15: a crimped top edge, the golden crust wall, a darker baked foot.
- pie_bottom (shared): pale baked pastry.
- <fruit>_pie_inner: rows 12-15: the cut face of a slice: top crust, two rows of filling, bottom crust.
"""
from texturegen.compose import recolor
from texturegen.palettes import BARLEY, BLACKBERRY, BLUEBERRY, FRUIT_RED, MALT_AMBER, OAT, PEACH, PEAR, PLUM

# Crust: 1 deep bake, 2 bake, 3 golden, 4 light, 5 highlight. Filling letters per fruit: x dark, y mid, z light.
LEGEND = {
    "1": MALT_AMBER[1],
    "2": MALT_AMBER[2],
    "3": MALT_AMBER[3],
    "4": BARLEY[4],
    "5": BARLEY[5],
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

# Only columns/rows 2-13 show on the pie's top.
TOP = """
3333333333333333
3333333333333333
3345454545454533
33533333333334 3
334xyy3yyxy34533
335yzy3yxyy35433
334yyx4yyzy34533
3353343333433433
334xyy3yyyx34533
335yyz3zyxy35433
334yxy4yyyy34533
3353343333433433
334yyy3xyzy34533
3354545454545433
3333333333333333
3333333333333333
""".replace(" ", "3")

SIDE = """
3333333333333333
3333333333333333
3333333333333333
3333333333333333
3333333333333333
3333333333333333
3333333333333333
3333333333333333
3333333333333333
3333333333333333
3333333333333333
3333333333333333
4545454545454545
3434343434343434
3333333333333333
2222222222222222
"""

BOTTOM = "\n".join(("4344" * 4) if y % 3 else ("3434" * 4) for y in range(16))

INNER = """
3333333333333333
3333333333333333
3333333333333333
3333333333333333
3333333333333333
3333333333333333
3333333333333333
3333333333333333
3333333333333333
3333333333333333
3333333333333333
3333333333333333
4545454545454545
zyyzyxyyzyyxyzyy
xyxyyxxyxyyxyxxy
2323232323232323
"""

TEXTURES = {"pie_side": SIDE, "pie_bottom": BOTTOM}
for fruit, letters in FRUITS.items():
    colors = dict(zip("xyz", letters))
    TEXTURES[f"{fruit}_pie_top"] = recolor(TOP, colors)
    TEXTURES[f"{fruit}_pie_inner"] = recolor(INNER, colors)

COMPARE = ["block/cake_top", "block/cake_side", "block/cake_inner"]
