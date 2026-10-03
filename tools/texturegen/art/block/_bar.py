"""Helper (not a texture): the Bar Counter's and Bar Stool's wood, drawn from one wood ramp (letters 0-5, dark to light).

- planks(): polished boards running across, four to the block: each lit along its top edge, a fine grain, a dark seam
  under it, and its end joint staggered from the board above. Reads as joinery rather than rough planks.
- trim(): the framing stiles, rails, legs and kick plate: the same wood two shades darker, its grain running the length.
- panel(): the counter's raised front panel: lit along its top and left bevel, shadowed along its bottom and right,
  boards running up inside it.
"""


def rows(fn):
    return "\n".join("".join(fn(x, y) for x in range(16)) for y in range(16))


def _plank(x, y):
    if y % 4 == 3:
        return "1"                                   # the seam under each board
    if x == (y // 4 * 7 + 5) % 16:
        return "1"                                   # an end joint, staggered board to board
    if y % 4 == 0:
        return "5" if x % 6 else "4"                 # the lit top edge
    return "2" if (x * 5 + y * 3) % 11 == 0 else "4" if y % 4 == 1 else "3"


def _trim(x, y):
    if x in (0, 15):
        return "0"
    return "0" if (y * 3 + x) % 13 == 0 else "1" if x % 5 == 2 else "2"


def _panel(x, y):
    if y == 0 or x == 0:
        return "5"                                   # the bevel catching the light
    if y == 15 or x == 15:
        return "1"                                   # and in shadow
    if y in (1, 14) or x in (1, 14):
        return "2"
    return "2" if x % 4 == 1 else "3" if (x + y * 3) % 9 else "4"


def planks():
    return rows(_plank)


def trim():
    return rows(_trim)


def panel():
    return rows(_panel)
