"""Vanilla items.

- vanilla_pod: two long green pods crossed, plump, lit along their top edges, a darker tip at each end.
- cured_vanilla: the same pods cured on the rack: glossy dark brown, tied in a little bundle with twine.
"""
from texturegen.compose import recolor
from texturegen.palettes import CLOTH, FOLIAGE_FRESH, MALT_BLACK

# 1-5 green pod; a-e cured brown; t twine
LEGEND = {**dict(zip("12345", FOLIAGE_FRESH[1:6])), **dict(zip("abcde", MALT_BLACK[1:6])), "t": CLOTH[4]}

VANILLA_POD = """
................
..............1.
.............23.
............243.
...........243..
..........243...
.1.......243....
.32.....243.....
..43...243......
...43.243.......
....4343........
.....343........
....3434........
...343.43.......
..2321..32......
..11.....1......
"""

# Cured: the green swapped for dark glossy browns, and a twine tie where the pods cross.
_CURED = [list(row) for row in recolor(VANILLA_POD, {"1": "a", "2": "b", "3": "c", "4": "d", "5": "e"}).strip("\n").splitlines()]
for _x in range(3, 9):
    _CURED[10][_x] = "t"
CURED_VANILLA = "\n".join("".join(row) for row in _CURED)

TEXTURES = {
    "vanilla_pod": VANILLA_POD,
    "cured_vanilla": CURED_VANILLA,
}

COMPARE = ["item/cocoa_beans", "item/stick"]
