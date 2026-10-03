"""Malting Tub: oak staves bound by two iron hoops, plus the surfaces shown inside it.

Outside: vertical staves lit from the left (light left edge, dark seam on the right).
Inside: the same staves in shadow. Contents (drawn on a plane inside the tub):
water, dry grain, steeping (grain under water), sprouting (rootlets appear),
and finished green malt (fluffy rootlets, green tips).
"""
from texturegen.palettes import BARLEY, FLOUR, FOLIAGE_CROP, IRON, WATER, WOOD_OAK

# 0-5 wood, i-n iron, A-F water, a-g barley, w-z rootlets, p-s shoots
LEGEND = {
    **{str(i): WOOD_OAK[i] for i in range(6)},
    **dict(zip("ijklmn", IRON)),
    **dict(zip("ABCDEF", WATER)),
    **dict(zip("abcdefg", BARLEY)),
    **{ch: FLOUR[i + 2] for i, ch in enumerate("wxyz")},
    **{ch: FOLIAGE_CROP[i + 2] for i, ch in enumerate("pqrs")},
}

STAVE = "4332"      # one stave: lit left edge, body, dark seam
STAVE_INNER = "2110"


def staves(pattern: str, hoops: bool) -> str:
    rows = []
    for y in range(16):
        row = (pattern * 4)
        if hoops and y in (8, 13):
            row = "llmllllmllllmlll"
        elif hoops and y in (9, 14):
            row = "jjkjjjjkjjjjkjjj"
        rows.append(row)
    # a couple of knots so staves don't look stamped
    grid = [list(r) for r in rows]
    for x, y in ((1, 3), (10, 11), (5, 6)):
        grid[y][x] = "2" if pattern == STAVE else "0"
    return "\n".join("".join(r) for r in grid)


BOTTOM = "\n".join(("2221" * 4) if y % 4 == 3 else ("3332" if y % 8 < 4 else "3322") * 4 for y in range(16))

WATER_SURFACE = """
CCCCDDCCCCCCCCCC
CCCCCCCCCDDDCCCC
CCDEECCCCCCCCCCC
CCCCCCCCCCCCDDCC
CCCCCCCDDCCCCCCC
CEECCCCCCCCCCCCC
CCCCCCCCCCEEDCCC
CCCCCDDCCCCCCCCC
CCCCCCCCCCCCCCDC
CCDDCCCCCCCCCCCC
CCCCCCCCDEECCCCC
CCCCCCCCCCCCCCCC
CCCCCDCCCCCCCDDC
CEECCCCCCCCCCCCC
CCCCCCCCCDDCCCCC
CCCCCCCCCCCCCCCC
"""

# One plump kernel seen from above (same shape as the malt items), lit from the top-left.
KERNEL = """
.f.
ffe
fed
dc.
"""

# Where kernels sit: staggered rows, nudged so the surface doesn't look stamped.
KERNEL_SPOTS = [(0, 0), (4, 1), (8, 0), (12, 1),
                (2, 4), (6, 5), (10, 4), (14, 5),
                (0, 8), (4, 9), (8, 8), (12, 9),
                (2, 12), (6, 13), (10, 12), (14, 13)]


def grain() -> list[list[str]]:
    """Packed grain seen from above: kernels over dark gaps. Wraps at the edges so it tiles."""
    g = [["b"] * 16 for _ in range(16)]
    rows = KERNEL.strip("\n").splitlines()
    for sx, sy in KERNEL_SPOTS:
        for dy, row in enumerate(rows):
            for dx, ch in enumerate(row):
                if ch != ".":
                    g[(sy + dy) % 16][(sx + dx) % 16] = ch
    return g


def grid_str(g) -> str:
    return "\n".join("".join(r) for r in g)


def steeping() -> str:
    """Calm water with grains resting on the bottom, glimpsed as dim gold dashes."""
    g = [list(r) for r in WATER_SURFACE.strip("\n").splitlines()]
    for x, y in ((3, 1), (9, 3), (13, 2), (1, 5), (6, 6), (11, 7), (3, 9), (14, 10), (8, 11), (5, 13), (11, 14), (1, 14)):
        g[y][x], g[y][(x + 1) % 16] = "c", "b"
    return grid_str(g)


def sprouting(done: bool) -> str:
    g = grain()
    roots = [(1, 1), (6, 2), (11, 1), (3, 6), (9, 5), (14, 6), (1, 10), (7, 9), (12, 10), (4, 13), (10, 14)]
    for x, y in roots:
        g[y][x] = "y"
        if done:
            g[y][(x + 1) % 16] = "z"
            g[(y + 1) % 16][x] = "x"
    if done:
        for x, y in ((2, 4), (8, 12), (13, 3), (5, 8)):
            g[y][x] = "r"
    return grid_str(g)


TEXTURES = {
    "malting_tub_side": staves(STAVE, True),
    "malting_tub_inner": staves(STAVE_INNER, False),
    "malting_tub_bottom": BOTTOM,
    "tub_water": WATER_SURFACE,
    "malting_grain": grid_str(grain()),
    "malting_steeping": steeping(),
    "malting_sprouting": sprouting(False),
    "malting_green_malt": sprouting(True),
}

COMPARE = ["block/barrel_side", "block/composter_side", "block/water_still"]
