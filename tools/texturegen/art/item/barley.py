"""Barley (item): one ripe ear on its stalk.

Reads apart from vanilla wheat by silhouette: a single plump, zigzag ear with
long awns streaming to the top-right, where wheat is a thick bundle.
Light from the top-left: upper-left kernels bright, lower-right kernels shadowed.
"""
from texturegen.palettes import BARLEY

LEGEND = {
    "0": BARLEY[0],
    "1": BARLEY[1],
    "2": BARLEY[2],
    "3": BARLEY[3],
    "4": BARLEY[4],
    "5": BARLEY[5],
    "6": BARLEY[6],
}

GRID = """
............4..3
...........5..4.
..........6..5..
.........5434..3
........6234324.
.......5434121..
......623432....
.....5434121....
....623432......
...5434121......
....3432........
....2121........
...31...........
..31............
.21.............
................
"""

COMPARE = ["item/wheat", "item/bread", "item/wheat_seeds", "item/pumpkin_seeds"]
