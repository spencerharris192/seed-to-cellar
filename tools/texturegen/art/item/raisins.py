"""Raisins and golden raisins: a small heap of wrinkled dried grapes, each a lumpy oval with a dull
highlight and a dark crease (like the dried berries, but all one kind and browner).
"""
from texturegen.compose import blank, to_grid
from texturegen.palettes import MALT_AMBER, MALT_BLACK, RED_GRAPE

# raisins: A outline, B body, C highlight; golden raisins: D E F
LEGEND = {
    "A": RED_GRAPE[0], "B": MALT_BLACK[3], "C": RED_GRAPE[3],
    "D": MALT_AMBER[1], "E": MALT_AMBER[3], "F": MALT_AMBER[5],
}

# Back row first, so the front raisins sit over the ones behind.
PILE = [(6, 6), (9, 6), (4, 8), (7, 8), (10, 8), (12, 9), (3, 10), (6, 10), (9, 10), (11, 11), (5, 12), (8, 12)]


def heap(outline: str, body: str, light: str) -> str:
    c = blank()
    for x, y in PILE:
        for dx, dy in ((0, 0), (1, 0), (2, 0), (0, 1), (1, 1), (2, 1)):
            c[y + dy][x + dx] = body
        c[y + 1][x] = outline                 # the shadowed underside
        c[y + 1][x + 2] = outline
        c[y][x + 1] = light                   # a dull, wrinkled highlight
    return to_grid(c)


TEXTURES = {
    "raisins": heap("A", "B", "C"),
    "golden_raisins": heap("D", "E", "F"),
}

COMPARE = ["item/sweet_berries", "item/cocoa_beans", "item/pumpkin_seeds"]
