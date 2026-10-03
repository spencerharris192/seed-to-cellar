"""Jams: one small glass jar with a cloth cover tied with string, filled per fruit (one base,
recolored; GDD section 20: "jam, pie, dried fruit, juice: one base each, fruit color by tint").

The glass shows as a thin pale outline with a glint on the left; the jam fills it, darkest at the
bottom and right, with a lighter top surface under the cover. Marmalade is the orange member, with dark
shreds of peel suspended in it.
"""
from texturegen.compose import recolor
from texturegen.palettes import BLACKBERRY, BLUEBERRY, CLOTH, FRUIT_RED, GLASS, ORANGE, PEACH, PLUM, ROOT

# 1 glass edge, 3 glint; d-e cloth cover, t string; x y z jam (dark, mid, light) -> per fruit letters
LEGEND = {
    "1": GLASS[0],
    "3": GLASS[3],
    "d": CLOTH[3],
    "e": CLOTH[5],
    "t": ROOT[1],
    **dict(zip("ABC", [BLUEBERRY[1], BLUEBERRY[2], BLUEBERRY[4]])),
    **dict(zip("DEF", [BLACKBERRY[1], BLACKBERRY[3], BLACKBERRY[4]])),
    **dict(zip("GHI", [BLACKBERRY[0], BLACKBERRY[2], BLACKBERRY[3]])),
    **dict(zip("JKL", [FRUIT_RED[1], FRUIT_RED[2], FRUIT_RED[4]])),
    **dict(zip("MNO", [FRUIT_RED[0], FRUIT_RED[1], FRUIT_RED[2]])),   # cherry: darker than sweet berry
    **dict(zip("PQR", [PLUM[1], PLUM[2], PLUM[4]])),
    **dict(zip("STU", [PEACH[2], PEACH[3], PEACH[4]])),
    **dict(zip("VWX", [ORANGE[2], ORANGE[3], ORANGE[4]])),
    "Y": ORANGE[1],                                                   # marmalade's peel
}

JAR = """
................
................
.....eeeeee.....
....edeeeede....
....dtttttttd...
.....1zzzzz1....
....1zzyyyyy1...
...13yyyyyyyx1..
...13yyyyyyxx1..
...13yyyyyyxx1..
...13yyyyyyxx1..
...1yyyyyyxxx1..
....1xxxxxxx1...
.....1111111....
................
................
"""

FRUITS = {
    "blueberry_jam": "ABC",
    "blackberry_jam": "DEF",
    "elderberry_jam": "GHI",
    "sweet_berry_jam": "JKL",
    "cherry_jam": "MNO",
    "plum_jam": "PQR",
    "peach_jam": "STU",
    "marmalade": "VWX",
}

TEXTURES = {name: recolor(JAR, dict(zip("xyz", letters))) for name, letters in FRUITS.items()}


def with_peel(grid: str) -> str:
    """Marmalade: short dark shreds of peel in the jam."""
    rows = [list(r) for r in grid.strip("\n").splitlines()]
    for x, y in ((7, 7), (8, 8), (10, 7), (5, 9), (6, 10), (9, 10), (10, 11), (11, 9)):
        if rows[y][x] in "VW":
            rows[y][x] = "Y"
    return "\n".join("".join(r) for r in rows)


TEXTURES["marmalade"] = with_peel(TEXTURES["marmalade"])

COMPARE = ["item/honey_bottle", "item/potion", "item/sweet_berries"]
