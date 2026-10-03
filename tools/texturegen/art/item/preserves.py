"""Preserves and dried snacks:

- mother_of_vinegar: the culture kept in its vinegar: a corked flask of amber vinegar with the pale,
  cloudy mother hanging in it (a bare disc read as a flatbread or a bun; an open jar as juice).
- pickles: two whole pickled gherkins side by side, olive green and bumpy.
- sauerkraut: pale shredded cabbage heaped in a small stoneware crock (the crock says "fermented").
- kimchi: the same crock, heaped with chili-red cabbage, pale ribs and green scallion.
- dried_berries: a small mound of shriveled berries in mixed dark reds and purples.
- granola: a thick baked oat bar lying at an angle, lumpy with golden clusters, berries pressed in.
"""
import math

from texturegen.compose import sibling
from texturegen.palettes import (BLACKBERRY, BLUEBERRY, CLAY, FOLIAGE_CROP, FOLIAGE_FRESH, FRUIT_RED, GLASS,
                                 HOP_CONE, MALT_AMBER, OAT, SUGAR_BEET)

shapes = sibling(__file__, "_shapes")

# A C D E mother (dark -> light), g glint; d e f vinegar; 1 3 glass; G-K gherkin (rim, dark -> light), W warts;
# L-P cabbage (pale); Q-U red cabbage; c scallion; k-p crock; v w x berries; h y z i oat bar; a b bar crust
LEGEND = {
    **dict(zip("ACDE", SUGAR_BEET[1:5])),
    "g": GLASS[3],
    **dict(zip("fde", MALT_AMBER[3:6])),
    "1": GLASS[0],
    "3": GLASS[3],
    **dict(zip("GHIJK", [FOLIAGE_CROP[0], FOLIAGE_CROP[1], HOP_CONE[1], HOP_CONE[2], HOP_CONE[3]])),
    "W": HOP_CONE[4],
    **dict(zip("LMNOP", [OAT[2], OAT[3], HOP_CONE[4], OAT[5], HOP_CONE[5]])),   # pale, faintly green cabbage
    **dict(zip("QRSTU", [FRUIT_RED[1], FRUIT_RED[2], FRUIT_RED[3], FRUIT_RED[4], FRUIT_RED[5]])),
    "c": FOLIAGE_FRESH[4],
    **dict(zip("klmnop", CLAY)),
    "v": FRUIT_RED[1],
    "w": BLACKBERRY[2],
    "x": BLUEBERRY[2],
    **dict(zip("hyz", [OAT[3], OAT[4], OAT[5]])),
    "i": MALT_AMBER[4],
    "a": MALT_AMBER[2],
    "b": MALT_AMBER[3],
}

# A corked flask (glass 1, glint 3) of amber vinegar, deeper toward the bottom and right, with the pale
# culture hanging in it: a glinting lumpy cloud trailing darker strands.
MOTHER = """
................
......aiia......
......abba......
......1331......
.....13ee31.....
....1eeeeee1....
...1eDEEDeee1...
..1eDEgDDCeed1..
..13DDDDDCeed1..
..13eCDDCAedd1..
..13eeAeeAddd1..
..1eeeAeeddff1..
...1ddddddff1...
....11111111....
................
................
"""


def pickles() -> str:
    g = [["."] * 16 for _ in range(16)]
    a = math.radians(-38)
    along, across = (math.cos(a), math.sin(a)), (math.sin(a), -math.cos(a))   # up-right; up-left (lit side)
    for cx, cy, rx, ry in ((6.9, 6.9, 4.8, 2.6), (9.3, 10.0, 5.1, 2.8)):      # back one first, side by side
        shape = shapes.dome(cx, cy, rx, ry, angle=-38, height=0.9)
        part = shapes.shade(shape, "GHIJK")
        for x, y in shape:
            g[y][x] = part[y][x]
        # bumpy skin: pale warts in a row along the lit side, darker ones along the shadow side
        for t, offset, ch in ((-0.5, 0.45, "W"), (-0.1, 0.45, "W"), (0.3, 0.45, "W"), (-0.3, -0.35, "H"), (0.1, -0.35, "H"),
                              (0.5, -0.35, "H")):
            x = round(cx - 0.5 + t * rx * along[0] + offset * ry * across[0])
            y = round(cy - 0.5 + t * rx * along[1] + offset * ry * across[1])
            if (x, y) in shape and g[y][x] != "G":
                g[y][x] = ch
    return shapes.to_str(g)


# Small stoneware crock: lit rim lip, a shadow under it, a rounded body darkening to the right.
CROCK = """
................
................
................
................
................
................
................
.kooppoooonnmmk.
.kmmnnnnnnmmllk.
..knooonnnnmlk..
..knoonnnnmmlk..
..knoonnnmmmlk..
..kmnnnnmmmllk..
...kmmmmmlllk...
....kkkkkkkk....
................
"""


def crock(heap_letters: str, streaks) -> str:
    """The crock heaped with shredded cabbage: a dome striped with diagonal strands (each pixel
    steps one shade lighter or darker than its lighting says, in bands), then extra streaks
    (x, y, letters) running down-left, then the crock drawn over the heap's base."""
    shape = shapes.dome(8.0, 7.4, 6.0, 4.4, height=0.9)
    g = shapes.shade(shape, heap_letters)
    inner = heap_letters[1:]
    for (x, y), b in shape.items():
        if g[y][x] != heap_letters[0]:
            band = (x + y * 2) % 3 - 1          # -1, 0, +1 in diagonal bands: the strands
            g[y][x] = inner[max(0, min(len(inner) - 1, int(b * len(inner)) + band))]
    for x, y, letters in streaks:
        for i, ch in enumerate(letters):
            shapes.put(g, [(x - i, y + i)], ch)
    for y, row in enumerate(CROCK.strip("\n").splitlines()):
        for x, ch in enumerate(row):
            if ch != ".":
                g[y][x] = ch
    return shapes.to_str(g)


def sauerkraut() -> str:
    return crock("LMNOP", [(5, 4, "PO"), (8, 3, "PN"), (11, 5, "OM"), (7, 6, "MPO"), (10, 6, "PM"), (4, 6, "OM"),
                           (13, 7, "M"), (6, 3, "L")])


def kimchi() -> str:
    return crock("QRSTU", [(5, 4, "UT"), (8, 3, "ON"), (11, 5, "TR"), (7, 6, "OON"), (10, 6, "UT"), (4, 6, "cc"),
                           (9, 4, "c"), (12, 6, "c"), (13, 7, "R")])


def dried_berries() -> str:
    g = [["."] * 16 for _ in range(16)]
    # packed in a mound: back row first so the front berries overlap it
    berries = [(6, 7, "w"), (9, 7, "v"), (4, 9, "x"), (7, 9, "v"), (10, 9, "w"), (12, 10, "x"),
               (3, 11, "v"), (5, 11, "w"), (8, 11, "x"), (11, 11, "v"), (6, 12, "v"), (9, 12, "w")]
    for x, y, ch in berries:   # each berry: a wrinkled lump with a pale highlight and a dark crease
        for dx, dy in ((0, 0), (1, 0), (2, 0), (0, 1), (1, 1), (2, 1)):
            g[y + dy][x + dx] = ch
        g[y][x] = "U" if ch == "v" else "T"
        g[y + 1][x + 2] = "Q"
    return shapes.to_str(g)


def granola() -> str:
    """A bar turned 28 degrees: a clustered top face, the toasted front side below it, a dark rim."""
    g = [["."] * 16 for _ in range(16)]
    a = math.radians(-28)
    ca, sa = math.cos(a), math.sin(a)
    top, side = set(), set()
    for y in range(16):
        for x in range(16):
            dx, dy = x + 0.5 - 8.0, y + 0.5 - 7.4
            u, v = dx * ca + dy * sa, -dx * sa + dy * ca    # along the bar; across it (+ = toward the front)
            if abs(u) <= 6.7 and -2.7 <= v <= 1.9 and not (abs(u) > 6.0 and abs(v) > 1.6):
                top.add((x, y))
            elif abs(u) <= 6.5 and 1.9 < v <= 3.6:
                side.add((x, y))
    shape = top | side
    for x, y in shape:
        rim = any((x + ox, y + oy) not in shape for ox, oy in ((1, 0), (-1, 0), (0, 1), (0, -1)))
        if rim:
            g[y][x] = "a"
        elif (x, y) in side:
            g[y][x] = "i" if (x, y - 1) in top else "b"
        else:   # baked oat clusters: golden lumps over darker toasted gaps, a few pale flakes
            g[y][x] = "iyhbiyzhibyhiy"[(x * 5 + y * 3 + (x * y) % 4) % 14]
    for x, y, ch in ((5, 8, "v"), (9, 6, "w"), (8, 9, "x"), (11, 7, "v"), (4, 10, "w"), (12, 5, "x")):
        if (x, y) in top and g[y][x] != "a":
            g[y][x] = ch
    return shapes.to_str(g)


TEXTURES = {
    "mother_of_vinegar": MOTHER,
    "pickles": pickles(),
    "sauerkraut": sauerkraut(),
    "kimchi": kimchi(),
    "dried_berries": dried_berries(),
    "granola": granola(),
}

COMPARE = ["item/dried_kelp", "item/sweet_berries", "item/cookie"]
