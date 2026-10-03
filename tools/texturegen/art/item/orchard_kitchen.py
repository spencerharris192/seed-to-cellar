"""Orchard-kitchen foods and pantry bottles (GDD sections 15, 17):

- olive_oil: a tall, slim glass bottle with square shoulders and a cork, full of golden-green oil.
- sorghum_syrup: a round glass flask filled to the neck with dark amber syrup, a drip running down it.
- cured_olives: a glistening heap of dark and green olives, a pale shine on each.
- stuffed_grape_leaves: three dull-green leaf rolls stacked two and one, the leaf's veins wrapping round.
- bruschetta: an oval slice of toasted bread piled with red tomato and green basil, oil glinting.
- garlic_bread: a split baguette, golden and buttery on the cut side, flecked with herbs and garlic.
"""
from texturegen.compose import blank, sibling, to_grid
from texturegen.palettes import (CORN, FOLIAGE_FRESH, FRUIT_RED, GARLIC, GLASS, HOP_CONE, MALT_AMBER, OAT, OLIVE, PEAR,
                                 SORGHUM, WOOD_OAK)

shapes = sibling(__file__, "_shapes")

# e g glass (edge, glint); c C cork; a b d oil; r s t u syrup; h-k dark olives, p q m o green olives;
# 1-5 leaf rolls; H-L crust, M-O crumb, P-R buttered crumb; x y z tomato; v V basil and herbs; G garlic
LEGEND = {
    "e": GLASS[0], "g": GLASS[3],
    "c": WOOD_OAK[3], "C": WOOD_OAK[4],
    "a": PEAR[2], "b": PEAR[3], "d": PEAR[4],
    **dict(zip("rstu", SORGHUM[1:5])),
    **dict(zip("hijk", OLIVE[0:4])),
    **dict(zip("pqmo", OLIVE[2:6])),
    **dict(zip("12345", [OLIVE[1], OLIVE[2], HOP_CONE[1], HOP_CONE[2], HOP_CONE[3]])),   # cooked, olive-green leaves
    **dict(zip("HIJKL", MALT_AMBER[0:5])),
    **dict(zip("MNO", OAT[3:6])),
    **dict(zip("PQR", CORN[3:6])),
    "x": FRUIT_RED[2], "y": FRUIT_RED[3], "z": FRUIT_RED[4],
    "v": FOLIAGE_FRESH[2], "V": FOLIAGE_FRESH[4],
    "G": GARLIC[4],
}

OLIVE_OIL = """
................
.......Cc.......
.......cc.......
......eeee......
......egde......
......egde......
.....eegdbee....
.....egddbbe....
.....egdbbbe....
.....egdbbbe....
.....egbbbae....
.....ebbbbae....
.....ebbbaae....
.....ebbaaae....
......eeeee.....
................
"""

SORGHUM_SYRUP = """
................
.......Cc.......
.......cc.......
......egge......
.......eu.......
......euue......
.....eguutse....
....egutttsse...
...egutttsssre..
...egttttssrre..
...egtttssrrre..
...egttssrrrre..
....ettssrrrre..
.....esrrrrre...
......eeeeee....
................
"""


def syrup() -> str:
    rows = [list(r) for r in SORGHUM_SYRUP.strip("\n").splitlines()]
    for x, y in ((12, 6), (12, 7)):          # a drip down the shoulder
        rows[y][x] = "s"
    rows[8][12] = "r"
    return "\n".join("".join(r) for r in rows)


def olives() -> str:
    g = blank()
    heap = [(6.0, 6.2, "hiiijk", 20), (10.6, 6.8, "hpqqmo", -20), (4.4, 10.4, "hpqqmo", -10), (8.4, 10.0, "hiiijk", 10),
            (12.0, 11.2, "hiiijk", 30)]
    for cx, cy, ramp, angle in heap:           # back to front; a dark rim keeps them apart, a glossy highlight
        shape = shapes.dome(cx, cy, 2.8, 2.0, angle=angle, height=1.0)
        part = shapes.shade(shape, ramp)
        for x, y in shape:
            g[y][x] = part[y][x]
    return to_grid(g)


def rolls() -> str:
    g = blank()
    for cx, cy in ((5.2, 10.2), (10.8, 10.2), (8.0, 6.2)):   # two below, one on top
        shape = shapes.dome(cx, cy, 3.3, 2.0, height=0.8)
        part = shapes.shade(shape, "12345")
        for x, y in shape:
            g[y][x] = part[y][x]
        for dx in (-2, 0, 2):                     # the leaf wrapping round: veins across the roll
            shapes.put(g, [(round(cx + dx), round(cy - 1)), (round(cx + dx - 1), round(cy))], "2", only_on="345")
    return to_grid(g)


def bruschetta() -> str:
    shape = shapes.dome(8.0, 8.4, 6.8, 4.0, angle=-18, height=0.5)
    g = shapes.shade(shape, "HIJKL")
    top = shapes.dome(7.8, 8.0, 5.4, 2.8, angle=-18, height=0.5)
    for (x, y), b in top.items():                 # the toasted crumb face
        g[y][x] = "M" if b < 0.45 else "N" if b < 0.8 else "O"
    for x, y, ch in ((4, 7, "x"), (5, 7, "y"), (6, 8, "x"), (7, 6, "z"), (8, 7, "x"), (9, 8, "y"), (10, 6, "x"),
                     (11, 7, "y"), (6, 9, "y"), (9, 9, "x"), (12, 8, "x"), (7, 8, "y"), (10, 8, "z"),
                     (8, 6, "V"), (5, 8, "v"), (11, 9, "V"), (9, 7, "v")):
        shapes.put(g, [(x, y)], ch)
    return shapes.to_str(g)


def garlic_bread() -> str:
    shape = shapes.dome(8.0, 8.0, 7.2, 3.1, angle=-35, height=0.8)
    g = shapes.shade(shape, "HIJKL")
    top = shapes.dome(7.6, 7.6, 5.8, 1.9, angle=-35, height=0.6)
    for (x, y), b in top.items():                 # the cut, buttery face
        g[y][x] = "P" if b < 0.4 else "Q" if b < 0.75 else "R"
    for x, y, ch in ((4, 10, "v"), (6, 8, "V"), (8, 7, "v"), (10, 5, "V"), (5, 9, "G"), (9, 6, "G"), (11, 5, "v"),
                     (7, 9, "V")):
        shapes.put(g, [(x, y)], ch, only_on="PQR")
    return shapes.to_str(g)


TEXTURES = {
    "olive_oil": OLIVE_OIL,
    "sorghum_syrup": syrup(),
    "cured_olives": olives(),
    "stuffed_grape_leaves": rolls(),
    "bruschetta": bruschetta(),
    "garlic_bread": garlic_bread(),
}

COMPARE = ["item/honey_bottle", "item/bread", "item/cookie"]
