"""Kiln: a brick oven. Sides reuse vanilla bricks; these are the functional faces.

Front: brick courses (matched to vanilla bricks) around an arched iron firebox door.
Lit: the door's vent slits and the gap under it glow. Top: a brick rim around an iron
drying grate; lit, the gaps between the bars glow orange.
"""
from texturegen.palettes import BRICK, FIRE, IRON, MORTAR

# 0-5 brick, m-p mortar, i-n iron... use distinct symbols: A-F iron, a-f fire
LEGEND = {
    **{str(i): BRICK[i] for i in range(6)},
    **dict(zip("mnop", MORTAR)),
    **dict(zip("ABCDEF", IRON)),
    **dict(zip("abcdef", FIRE)),
}


def bricks() -> list[list[str]]:
    """Courses 4 rows tall; mortar every 4th row and staggered vertical joints."""
    g = []
    for y in range(16):
        row = []
        course = y // 4
        for x in range(16):
            if y % 4 == 3:
                row.append("n")
            elif (x + (4 if course % 2 else 0)) % 8 == 7:
                row.append("m")
            else:
                shade = "4" if y % 4 == 0 else ("2" if y % 4 == 2 else "3")
                if (x + (4 if course % 2 else 0)) % 8 == 0 and y % 4 != 3:
                    shade = "5" if y % 4 == 0 else "4"
                row.append(shade)
        g.append(row)
    return g


DOOR = """
..BCCB..
.BDEEDB.
BDEDDEDB
CE0000EC
CD0000DC
CE0000EC
CDDDDDDC
BBCCCCBB
"""

DOOR_LIT = """
..BCCB..
.BDEEDB.
BDEDDEDB
CEeddeEC
CDcddcDC
CEeccdEC
CDDDDDDC
BBCCCCBB
"""


def front(door: str, lit: bool) -> str:
    g = bricks()
    rows = door.strip("\n").splitlines()
    for dy, row in enumerate(rows):
        for dx, ch in enumerate(row):
            if ch != ".":
                g[7 + dy][4 + dx] = ch
    if lit:  # firelight spilling from under the door
        for x in range(5, 11):
            g[15][x] = "c" if x in (5, 10) else "d"
    return "\n".join("".join(r) for r in g)


def top(lit: bool) -> str:
    g = bricks()
    for y in range(2, 14):
        for x in range(2, 14):
            if y % 2 == 0:
                g[y][x] = "D" if x % 4 else "E"   # iron bars with rivet highlights
            else:
                g[y][x] = ("c" if (x + y) % 3 else "b") if lit else "A"
    for i in range(2, 14):  # frame around the grate
        g[1][i] = "C"
        g[14][i] = "B"
        g[i][1] = "C"
        g[i][14] = "B"
    return "\n".join("".join(r) for r in g)


TEXTURES = {
    "kiln_front": front(DOOR, False),
    "kiln_front_lit": front(DOOR_LIT, True),
    "kiln_top": top(False),
    "kiln_top_lit": top(True),
}

COMPARE = ["block/bricks", "block/furnace_front_on", "block/smoker_front_on"]
