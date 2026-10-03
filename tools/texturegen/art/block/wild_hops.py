"""Wild hops (cross model): a low sprawling tangle of bine with leaves and a few cones.

Found at forest edges. Same leaf and cone shapes as the trellis crop.
"""
from texturegen.compose import blank, stamp, to_grid
from texturegen.palettes import FOLIAGE_FRESH, HOP_CONE

LEGEND = {**dict(zip("asdfgh", FOLIAGE_FRESH)), **dict(zip("zxcvbn", HOP_CONE))}

LEAF_R = """
.gh.
fghg
.df.
"""
LEAF_L = "\n".join(line[::-1] for line in LEAF_R.strip("\n").splitlines())

CONE = """
nb
bv
vc
.x
"""


def _build():
    c = blank()
    # arching stems from the ground
    for x, y in [(7, 15), (7, 14), (6, 13), (5, 12), (4, 11), (3, 10), (3, 9), (4, 8)]:
        c[y][x] = "d"
    for x, y in [(8, 15), (8, 14), (9, 13), (10, 12), (11, 11), (12, 10), (12, 9), (11, 8), (10, 7)]:
        c[y][x] = "d"
    for x, y in [(7, 13), (7, 12), (8, 11), (8, 10), (7, 9), (7, 8), (8, 7), (8, 6)]:
        c[y][x] = "f"
    for sprite, x, y in [(LEAF_L, 0, 11), (LEAF_R, 12, 11), (LEAF_L, 1, 6), (LEAF_R, 11, 5),
                         (LEAF_L, 4, 4), (LEAF_R, 8, 3), (LEAF_R, 9, 12), (LEAF_L, 3, 13)]:
        stamp(c, sprite, x, y)
    for x, y in [(5, 8), (10, 8), (7, 4)]:
        stamp(c, CONE, x, y)
    return to_grid(c)


GRID = _build()

COMPARE = ["block/sweet_berry_bush_stage3", "block/fern", "block/vine"]
