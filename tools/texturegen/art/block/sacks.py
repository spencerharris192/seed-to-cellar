"""Sacks (wheat flour, pale malt, coffee beans, sugar).

Side: woven cloth (a fine checker of two tones with a few slubs), stitched seams down both
edges, and a stenciled mark in the middle saying what's inside: a wheat ear for flour, a kernel
for malt, a bean for coffee, a cube for sugar, each in its own ink. Top: the open mouth, a
folded rim around the contents, drawn the way the items look. Bottom: plain cloth.

Flour, malt and coffee come in brown burlap. Sugar comes in white cotton with two blue stripes,
like old sugar sacks, so it can't be mistaken for flour (playtest 3: the two looked alike).
"""
from texturegen.compose import stamp
from texturegen.palettes import BARLEY, CLOTH, FLOUR, FOLIAGE_DUSTY, FRUIT_RED, MALT_AMBER, WATER

# 0-5 burlap, A-F flour/sugar/cotton, G-L pale malt, M-R coffee beans (unroasted); inks m r b k, w stripe
LEGEND = {
    **{str(i): CLOTH[i] for i in range(6)},
    **dict(zip("ABCDEF", FLOUR)),
    **dict(zip("GHIJKL", BARLEY[0:6])),
    **dict(zip("MNOPQR", FOLIAGE_DUSTY)),
    "k": CLOTH[0],
    "m": MALT_AMBER[1],
    "r": FRUIT_RED[1],
    "b": WATER[1],
    "w": WATER[3],
}

# Letters for each cloth: two weave tones, slubs, dark threads, the seam, stitches.
CLOTHS = {
    "burlap": {"a": "3", "b": "4", "slub": "5", "dark": "2", "seam": "1", "stitch": "5"},
    "cotton": {"a": "D", "b": "E", "slub": "F", "dark": "C", "seam": "B", "stitch": "C"},
}


def cloth_of(content: str) -> dict:
    return CLOTHS["cotton" if content == "sugar" else "burlap"]


def weave(c: dict) -> list[list[str]]:
    g = [[c["b"] if (x + y) % 2 else c["a"] for x in range(16)] for y in range(16)]
    for x, y in ((5, 2), (11, 7), (3, 12), (13, 13), (8, 4)):
        g[y][x] = c["slub"]   # slubs: thicker threads
    for x, y in ((9, 10), (2, 6), (12, 2)):
        g[y][x] = c["dark"]
    return g


def seamed(g: list[list[str]], c: dict) -> list[list[str]]:
    for y in range(16):
        g[y][0] = c["seam"]
        g[y][15] = c["seam"]
        if y % 3 == 1:
            g[y][1] = c["stitch"]
            g[y][14] = c["stitch"]
    return g


def grid(g: list[list[str]]) -> str:
    return "\n".join("".join(r) for r in g)


EMBLEMS = {
    "wheat_flour": ("k", """
...k...
..k.k..
...k...
..k.k..
...k...
...k...
...k...
"""),
    "pale_malt": ("m", """
...k...
..kkk..
.kk.kk.
.kk.kk.
.kk.kk.
..kkk..
...k...
"""),
    "coffee": ("r", """
..kkk..
.kkk.k.
.kkk.k.
.kk.kk.
.k.kkk.
.k.kkk.
..kkk..
"""),
    # Even-sized (6x6, drawn in an 8x8 box) so it sits dead center between the stripes;
    # a 5-wide square can't be centered on a 16-pixel face.
    "sugar": ("b", """
........
.kkkkkk.
.k....k.
.k....k.
.k....k.
.k....k.
.kkkkkk.
........
"""),
}


def side(content: str) -> str:
    ink, emblem = EMBLEMS[content]
    c = cloth_of(content)
    g = weave(c)
    g = seamed(g, c)
    if content == "sugar":   # two woven blue stripes, mirror images, running seam to seam
        for y, ch in ((2, "w"), (3, "b"), (12, "b"), (13, "w")):
            g[y][1:15] = [ch] * 14
    stamp(g, emblem.replace("k", ink), 4, 4 if content == "sugar" else 5)
    return grid(g)


# One plump grain (a malt kernel or an unroasted coffee bean with its crease), lit from the
# top-left; placed staggered like the malting tub's grain so the pile doesn't look like bricks.
KERNEL = """
.c.
cbb
bba
ab.
"""
BEAN = """
.c.
cab
cab
.b.
"""
SPOTS = [(0, 0), (4, 1), (8, 0), (2, 4), (6, 5), (10, 4), (0, 8), (4, 9), (8, 8)]


def contents(content: str) -> list[list[str]]:
    """The 12x12 inside of the mouth."""
    if content == "wheat_flour":   # soft and even, a few specks
        return [["C" if (x * 7 + y * 11) % 13 == 0 else "DE"[(x + y) % 2] for x in range(12)] for y in range(12)]
    if content == "sugar":         # crystals: bright glints and grey facets
        return [["C" if (x * 3 + y * 5) % 7 == 0 else "F" if (x * 5 + y * 2) % 6 == 0 else "E"
                 for x in range(12)] for y in range(12)]
    if content == "pale_malt":
        sprite, colors, gap = KERNEL, {"a": "J", "b": "K", "c": "L"}, "I"
    else:
        sprite, colors, gap = BEAN, {"a": "O", "b": "P", "c": "Q"}, "N"
    g = [[gap] * 12 for _ in range(12)]
    rows = sprite.strip("\n").splitlines()
    for sx, sy in SPOTS:
        for dy, row in enumerate(rows):
            for dx, ch in enumerate(row):
                if ch != ".":
                    g[(sy + dy) % 12][(sx + dx) % 12] = colors[ch]
    return g


def top(content: str) -> str:
    c = cloth_of(content)
    g = weave(c)
    for y in range(16):
        for x in range(16):
            if x in (0, 15) or y in (0, 15):
                g[y][x] = c["seam"]    # outer edge of the fold
            elif x in (1, 14) or y in (1, 14):
                g[y][x] = c["slub"] if (x + y) % 2 else c["b"]   # folded rim catching the light
    if content == "sugar":   # the stripes show on the folded rim too
        for i in range(2, 14):
            g[1][i] = g[14][i] = "b"
    inner = contents(content)
    for y in range(12):
        for x in range(12):
            g[y + 2][x + 2] = inner[y][x]
    return grid(g)


TEXTURES = {}
for name in EMBLEMS:
    TEXTURES[f"{name}_sack_side"] = side(name)
    TEXTURES[f"{name}_sack_top"] = top(name)
    TEXTURES[f"{name}_sack_bottom"] = grid(seamed(weave(cloth_of(name)), cloth_of(name)))

COMPARE = ["block/hay_block_side", "block/brown_wool", "block/composter_side"]
