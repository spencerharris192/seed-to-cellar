"""Fertile Farmland: tilled rows of dark, rich soil with flecks of half-rotted straw and leaf.

Darker and redder than vanilla farmland so a fertile patch is easy to spot, and the moist
version darker still (like vanilla, wet soil goes almost black). Four furrows, each lit on
one slope and dark in the groove; the grooves wander a pixel so the rows don't look ruled.
"""
from texturegen.palettes import BARLEY, FOLIAGE_CROP, SOIL_RICH

# 0-5 rich soil, w-x straw flecks, p-q leaf flecks
LEGEND = {
    **{str(i): SOIL_RICH[i] for i in range(6)},
    **{ch: BARLEY[i + 2] for i, ch in enumerate("wx")},
    **{ch: FOLIAGE_CROP[i + 1] for i, ch in enumerate("pq")},
}

# Dry fertile farmland, drawn by hand: furrows run lengthwise like vanilla's. Each ridge is lit on
# its left slope (4), then body (3), then the groove (1); crumbs (5 bright, 2 and 0 dark) break it up.
DRY = """
4331433143314331
4331433145314331
4530433143314331
4331433143314331
4331443214304531
4321443214314314
4332143302314314
4332153314314314
4332143314314314
4432243314331214
4432143314531431
4331423214332431
4331443214331451
4531433144321430
4331435243314331
4331433143315331
"""

FLECKS_DRY = {(2, 1): "x", (10, 5): "w", (6, 9): "q", (13, 13): "x", (1, 12): "p", (9, 2): "w"}
FLECKS_WET = {(2, 1): "w", (10, 5): "w", (6, 9): "p", (13, 13): "w", (1, 12): "p", (9, 2): "w"}


def soil(shift: int, flecks: dict) -> str:
    rows = [[str(max(0, int(ch) - shift)) for ch in r] for r in DRY.strip("\n").splitlines()]
    for (x, y), ch in flecks.items():
        rows[y][x] = ch
    return "\n".join("".join(r) for r in rows)


TEXTURES = {
    "fertile_farmland": soil(0, FLECKS_DRY),
    "fertile_farmland_moist": soil(2, FLECKS_WET),
}

COMPARE = ["block/farmland", "block/farmland_moist", "block/dirt"]
