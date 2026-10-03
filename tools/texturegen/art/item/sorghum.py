"""Sorghum (item): one dense seed head on a short stalk.

A chunky oval of rust-red grain, lit from the top-left with copper highlights and wine-dark
shadows, on a thick green stalk. Nothing like the slim grains: it's a round red blob.
"""
from texturegen.palettes import FOLIAGE_CROP, SORGHUM

LEGEND = {**{str(i): SORGHUM[i] for i in range(6)}, **dict(zip("qwerty", FOLIAGE_CROP))}

GRID = """
................
.........454....
.......4554343..
......455434432.
......54543432..
.....45434343321
.....5434343322.
.....4343433221.
......343332211.
......24322211..
.......22211....
......tr.11.....
.....tr.........
....tr..........
...tr...........
..te............
"""

COMPARE = ["item/wheat", "item/beetroot"]
