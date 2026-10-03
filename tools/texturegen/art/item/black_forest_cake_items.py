"""Black Forest Cake items.

- black_forest_cake: the whole cake from the side and a little above: a cream-covered drum, chocolate shavings round its
  foot and heaped on top, a ring of cream rosettes each holding a dark red cherry.
- black_forest_cake_slice: a tall wedge cut from it: cream on top with a rosette and cherry, the cut side showing dark
  chocolate sponge, cream and cherry layers.
"""
from texturegen.palettes import FLOUR, FRUIT_RED, MALT_BLACK

# c C W cream; 1-5 chocolate; r R g cherry
LEGEND = {
    "c": FLOUR[2], "C": FLOUR[3], "W": FLOUR[5],
    **{str(i): MALT_BLACK[i] for i in range(1, 6)},
    "r": FRUIT_RED[1], "R": FRUIT_RED[2], "g": FRUIT_RED[4],
}

BLACK_FOREST_CAKE = """
................
................
....gR.gR.gR....
...WRWWRWWRWW...
..CWWW1W3WWWWC..
.cCWW12312WWWWc.
.cWRgWWWWWWgRWc.
.cWWWWWWWWWWWWc.
.cWWWWWWWWWWWCc.
.cCWWWWWWWWWWCc.
.c3W1W3W1W3W1Cc.
.c13131313131cc.
..c131313131cc..
...cccccccccc...
................
................
"""

BLACK_FOREST_CAKE_SLICE = """
................
................
................
...........gR...
.........WWRWW..
......WWWWWWWWc.
....WWWWWWWWWCc.
..WWWWWWWWWWWCc.
.cc2323232323Cc.
.cRCRCRCRCRCRCc.
.c2323232323Cc..
.cCCCCCCCCCCCc..
.c3232323232c...
..cccccccccc....
................
................
"""

TEXTURES = {
    "black_forest_cake": BLACK_FOREST_CAKE,
    "black_forest_cake_slice": BLACK_FOREST_CAKE_SLICE,
}

COMPARE = ["item/cake", "item/pumpkin_pie"]
