"""Malt and grist families (one design each, recolored per roast so the family reads together).

- Malt: a mound of plump kernels (like barley seeds, heaped).
- Green malt: the same mound in barley gold, with white rootlets and green shoots.
- Grist: the same colors crushed into flakes on a low heap.
Roast colors: pale = barley gold, amber = caramel, black = coffee brown.
"""
from texturegen.compose import blank, stamp, to_grid
from texturegen.palettes import BARLEY, FLOUR, FOLIAGE_CROP, MALT_AMBER, MALT_BLACK

# Neutral shape symbols 0 (dark) .. 5 (light) are mapped onto a ramp per texture.
RAMPS = {
    "pale": ("0123456", BARLEY),
    "amber": ("abcdef", MALT_AMBER),
    "black": ("ghijkl", MALT_BLACK),
}
LEGEND = {}
for chars, ramp in RAMPS.values():
    LEGEND.update({ch: ramp[i] for i, ch in enumerate(chars)})
LEGEND.update({ch: FLOUR[i + 2] for i, ch in enumerate("wxyz")})        # rootlets
LEGEND.update({ch: FOLIAGE_CROP[i + 2] for i, ch in enumerate("pqrs")})  # shoots

KERNEL = """
.5.
554
543
32.
"""

# Back row first, front row last (front kernels overlap the ones behind).
PILE = [(5, 2), (8, 1), (2, 5), (6, 5), (10, 4), (12, 6), (0, 9), (4, 9), (8, 8), (11, 9)]


def recolor(grid: str, ramp: str) -> str:
    """Map shape symbols 0-5 onto a ramp (ramp[0] = darkest)."""
    table = {str(i): ramp[min(i, len(ramp) - 1)] for i in range(6)}
    return "".join(table.get(ch, ch) for ch in grid)


def malt(ramp: str) -> str:
    c = blank()
    for x, y in PILE:
        stamp(c, recolor(KERNEL, ramp), x, y + 2)
    return to_grid(c)


def green_malt() -> str:
    c = [list(row) for row in malt("123456").split("\n")]  # a touch paler than pale malt
    # white rootlets curling out of kernels, green shoots peeking from the top
    for x, y, ch in [(4, 12, "y"), (3, 13, "x"), (8, 11, "z"), (8, 12, "y"), (13, 12, "y"), (14, 13, "x"),
                     (1, 10, "y"), (0, 11, "x"), (11, 7, "z"), (12, 8, "y")]:
        c[y][x] = ch
    for x, y, ch in [(6, 2, "r"), (7, 1, "s"), (9, 2, "q"), (10, 1, "r"), (3, 6, "r"), (2, 5, "s")]:
        c[y][x] = ch
    return "\n".join("".join(r) for r in c)


GRIST = """
................
................
................
................
................
................
.......54.......
.....5424351....
....54352344531.
...5424354233421
..54352435432431
..42354234354231
.543542343543221
.432243542323211
..12132132211211
................
"""

TEXTURES = {
    "green_barley_malt": green_malt(),
    "pale_malt": malt("0123456"),
    "amber_malt": malt(RAMPS["amber"][0]),
    "black_malt": malt(RAMPS["black"][0]),
    "pale_grist": recolor(GRIST, "0123456"),
    "amber_grist": recolor(GRIST, RAMPS["amber"][0]),
    "black_grist": recolor(GRIST, RAMPS["black"][0]),
}

COMPARE = ["item/wheat_seeds", "item/sugar", "item/gunpowder", "item/cocoa_beans"]
