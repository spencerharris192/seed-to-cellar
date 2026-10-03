"""Pot Still (models block/pot_still_lower and _upper; parts sample these 16x16 sheets where they sit in the block):

- pot_still_copper: riveted copper sheet like the Brew Kettle's, seams every 8 pixels, two rivet rows; the belly's sides show
  a rivet row across the middle.
- pot_still_top: copper seen from above: dark at the middle where the neck sits, a bright riveted ring round it.
- pot_still_neck: plainer copper for the narrow neck and the head, without the seams that would stripe such small parts;
  a rivet row shows on each.
- pot_still_band: the iron ring the pot stands on, and the stillage tap's handle.
- pot_still_pipe: the swan neck and pipes. A pipe is 2 pixels wide at columns 1-2, 7-8 or 13-14 (wherever its faces sample),
  so each of those pairs is lit on the left and shaded on the right; joints every 8 rows.
- pot_still_basket: the Gin Basket's mesh drum on the swan neck: a fine copper lattice over dark gaps, with green
  botanicals glimpsed through it.
- pot_still_safe: the spirit safe's sides. Its front samples columns 5-10, its sides columns 0-2 and 13-15, all rows 10-15:
  a copper frame round see-through glass (cut out), with a glint, so the spirit standing inside shows.
"""
from texturegen.palettes import COPPER, FOLIAGE_DUSTY, GLASS, IRON

# 0-5 copper, a-f iron, g-j clear glass (edge, light, pale, glint), v w botanicals behind the mesh
LEGEND = {
    **{str(i): COPPER[i] for i in range(6)},
    **dict(zip("abcdef", IRON)),
    **dict(zip("ghij", GLASS)),
    "v": FOLIAGE_DUSTY[2], "w": FOLIAGE_DUSTY[3],
}


def rows(fn):
    return "\n".join("".join(fn(x, y) for x in range(16)) for y in range(16))


def copper(x, y):
    if y in (3, 11) and x % 4 == 1:
        return "5"                       # rivet heads
    if y in (4, 12) and x % 4 == 1:
        return "1"                       # their shadows
    if x % 8 == 7:
        return "2"                       # sheet seams
    if x % 8 == 0:
        return "4"                       # the lit edge of each sheet
    return "3"


def neck(x, y):
    if y in (5, 10) and x % 3 == 1:
        return "5"                       # rivet rows (on the head, and on the neck)
    if y in (6, 11) and x % 3 == 1:
        return "2"
    return "3"


def top(x, y):
    d = max(abs(x - 7.5), abs(y - 7.5))
    if d < 2.5:
        return "1"                       # where the neck sits
    if d < 3.5:
        return "2"
    if d < 5.5:
        if d > 4.5 and (x + y) % 3 == 0:
            return "5"                   # rivets round the ring
        return "4" if x < y + 1 else "3"  # the ring, lit on its top-left
    if d < 6.5:
        return "3"
    return "2"


def band(x, y):
    if y % 4 == 0:
        return "e" if x % 5 == 2 else "d"   # a rivet now and then on the lit edge
    if y % 4 == 3:
        return "b"
    return "c"


def pipe(x, y):
    if y % 8 == 0:
        return "1" if x % 6 in (2, 3) else "2"   # a joint ring
    if x in (1, 7, 13):
        return "5" if y % 8 == 3 else "4"         # the lit side, with a glint
    if x in (2, 8, 14):
        return "2"
    return "3"


def basket(x, y):
    if x % 3 == 0 or y % 3 == 0:
        return "4" if (x + y) % 2 == 0 else "3"   # the copper lattice, glinting at the crossings
    return "w" if (x * 5 + y * 3) % 7 < 2 else ("v" if (x + y) % 4 == 0 else "1")   # botanicals, or the dark inside


def safe(x, y):
    if y < 10:
        return copper(x, y)
    front = 5 <= x <= 10
    left_edge = x in (0, 5, 13)
    right_edge = x in (2, 10, 15)
    if not (front or x <= 2 or x >= 13):
        return copper(x, y)
    if y == 10 or left_edge:
        return "4"                       # frame, lit
    if y == 15 or right_edge:
        return "2"                       # frame, shaded
    if (x, y) in ((6, 11), (7, 12), (14, 11)):
        return "j"                       # a glint on the glass
    if (x, y) in ((6, 12), (8, 11)):
        return "h"
    return "."                           # clear: the spirit inside shows


TEXTURES = {
    "pot_still_copper": rows(copper),
    "pot_still_top": rows(top),
    "pot_still_neck": rows(neck),
    "pot_still_band": rows(band),
    "pot_still_pipe": rows(pipe),
    "pot_still_safe": rows(safe),
    "pot_still_basket": rows(basket),
}

COMPARE = ["block/copper_block", "block/cut_copper", "block/iron_block"]
