"""Grain bales (barley, rye, oats, rice): one drawing, recolored with each grain's palette.

Side: upright stalks, each column keeping its own shade, bound by two twine bands (brown, where
vanilla's hay bale has red ones). Top: the cut ends of the stalks, bright dots over darker
gaps, inside a slightly darker rim.
"""
from texturegen.compose import recolor
from texturegen.palettes import BARLEY, OAT, RICE, ROOT, RYE

# Each grain's ramp (dark to light) gets six letters; u-w twine.
GRAINS = {"barley": "ABCDEF", "rye": "GHIJKL", "oat": "MNOPQR", "rice": "STUVWX"}
LEGEND = {
    **dict(zip("ABCDEF", BARLEY[0:6])),
    **dict(zip("GHIJKL", RYE)),
    **dict(zip("MNOPQR", OAT)),
    **dict(zip("STUVWX", RICE)),
    **dict(zip("uvw", ROOT[1:4])),
}

COLUMN_TONE = [3, 4, 3, 2, 4, 3, 4, 3, 2, 3, 4, 3, 3, 4, 2, 3]
BAND_TOP = "wvwwvwwvwwvwwvwv"
BAND_BOTTOM = "vuvvuvvuvvuvvuvu"


def side() -> str:
    rows = []
    for y in range(16):
        if y in (3, 11):
            rows.append(BAND_TOP)
            continue
        if y in (4, 12):
            rows.append(BAND_BOTTOM)
            continue
        row = ""
        for x in range(16):
            tone = COLUMN_TONE[x]
            if (x * 7 + y * 3) % 11 == 0:
                tone -= 1
            elif (x * 5 + y * 13) % 17 == 0:
                tone += 1
            if y in (0, 15):
                tone -= 1   # the bale's squared-off ends
            row += str(max(1, min(5, tone)))
        rows.append(row)
    return "\n".join(rows)


def top() -> str:
    rows = []
    for y in range(16):
        row = ""
        for x in range(16):
            if x in (0, 15) or y in (0, 15):
                tone = 2   # rim
            elif (x * 7 + y * 5) % 9 == 0:
                tone = 1   # gap between stalks
            elif (x + 2 * y) % 4 == 0:
                tone = 5 if (x * 3 + y) % 5 == 0 else 4   # a cut stalk end
            else:
                tone = 3
            row += str(tone)
        rows.append(row)
    return "\n".join(rows)


def colored(grid: str, letters: str) -> str:
    return recolor(grid, dict(zip("012345", letters)))


TEXTURES = {}
for grain, letters in GRAINS.items():
    TEXTURES[f"{grain}_bale_side"] = colored(side(), letters)
    TEXTURES[f"{grain}_bale_top"] = colored(top(), letters)

COMPARE = ["block/hay_block_side", "block/hay_block_top", "block/oak_log"]
