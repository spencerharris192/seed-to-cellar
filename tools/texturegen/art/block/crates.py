"""Fruit crates: one slatted oak crate (sides and bottom shared by every fruit), each crate's top heaped
with its fruit inside the frame.

Sides: two corner posts and three nailed boards with dark gaps between them, lit from the top-left. Tops: the
crate's rim, then the fruit packed in staggered rows over the shadow between them (round fruit are
3x3, small fruit like grapes and cherries 2x2), a highlight on each one's upper left.
"""
from texturegen.compose import blank, stamp, to_grid
from texturegen.palettes import (FRUIT_RED, LEMON, ORANGE, PEACH, PEAR, PLUM, RED_GRAPE, ROOT, WHITE_GRAPE,
                                 WOOD_OAK)

# a-f wood (dark -> light); each fruit: shadow, body, highlight
FRUIT_LETTERS = {
    "apple": ("123", [FRUIT_RED[1], FRUIT_RED[3], FRUIT_RED[4]]),
    "red_grape": ("456", [RED_GRAPE[1], RED_GRAPE[3], RED_GRAPE[5]]),
    "white_grape": ("789", [WHITE_GRAPE[1], WHITE_GRAPE[3], WHITE_GRAPE[5]]),
    "cherry": ("ABC", [FRUIT_RED[0], FRUIT_RED[1], FRUIT_RED[3]]),
    "plum": ("DEF", [PLUM[1], PLUM[3], PLUM[5]]),
    "peach": ("GHI", [PEACH[1], PEACH[3], PEACH[4]]),
    "pear": ("JKL", [PEAR[1], PEAR[3], PEAR[4]]),
    "lemon": ("MNO", [LEMON[1], LEMON[3], LEMON[4]]),
    "orange": ("PQR", [ORANGE[1], ORANGE[3], ORANGE[4]]),
}
SMALL = {"red_grape", "white_grape", "cherry"}

LEGEND = {**dict(zip("abcdef", WOOD_OAK)), "w": ROOT[1]}
for letters, colors in FRUIT_LETTERS.values():
    LEGEND.update(dict(zip(letters, colors)))


def side() -> str:
    """Two corner posts (lit left, shaded right) framing three boards with dark gaps between them; each board lit
    along its top edge, a little grain, and a nail at each end where it's fixed to the posts."""
    boards = {1: "e", 2: "d", 3: "d", 4: "c", 6: "e", 7: "d", 8: "d", 9: "c", 11: "e", 12: "d", 13: "d", 14: "c"}
    rows = []
    for y in range(16):
        row = []
        for x in range(16):
            if x in (0, 15) or y in (0, 15):
                row.append("a")                                   # outline
            elif x == 1:
                row.append("e")                                   # left post, lit
            elif x == 2:
                row.append("d")
            elif x == 13:
                row.append("c")                                   # right post, shaded
            elif x == 14:
                row.append("b")
            elif y not in boards:
                row.append("a")                                   # the gaps between boards
            elif x in (4, 11) and y in (2, 7, 12):
                row.append("b")                                   # nails
            elif boards[y] == "d" and (x * 5 + y * 3) % 11 == 0:
                row.append("c")                                   # grain
            else:
                row.append(boards[y])
        rows.append("".join(row))
    return "\n".join(rows)


def bottom() -> str:
    rows = []
    for y in range(16):
        if y in (0, 15):
            rows.append("a" * 16)
        elif y in (5, 10):
            rows.append("a" + "b" * 14 + "a")
        else:
            rows.append("a" + ("d" if y % 5 != 1 else "e") * 13 + "ca")
    return "\n".join(rows)


def top(fruit: str) -> str:
    shadow, body, light = FRUIT_LETTERS[fruit][0]
    c = blank()
    for y in range(16):
        for x in range(16):
            edge = x in (0, 15) or y in (0, 15)
            rim = x in (1, 14) or y in (1, 14)
            c[y][x] = "a" if edge else ("e" if (x == 1 or y == 1) else "c") if rim else shadow
    if fruit in SMALL:
        sprite = f"{light}{body}\n{body}{shadow}"
        spots = [(x, y) for y in range(2, 14, 2) for x in range(2 + (y // 2) % 2, 13, 2)]
    else:
        sprite = f".{light}{body}\n{light}{body}{body}\n{body}{body}{shadow}"
        spots = [(x, y) for y in range(2, 13, 3) for x in range(2 + (y // 3) % 2 * 1, 12, 3)]
    for x, y in spots:
        stamp(c, sprite, x, y)
    return to_grid(c)


TEXTURES = {"crate_side": side(), "crate_bottom": bottom(), **{f"{fruit}_crate_top": top(fruit) for fruit in FRUIT_LETTERS}}

COMPARE = ["block/barrel_side", "block/composter_side", "block/oak_planks"]
