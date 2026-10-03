"""Olive leaves (the base under the olive tree's fruit layers; not biome-tinted): narrow, silvery grey-green
leaves, lit on their upper sides, with small gaps showing through like any leaves.

Drawn on a wrap-around grid (strokes and gaps continue across the edges) so it tiles seamlessly over
a tree's many leaf blocks.
"""
from texturegen.palettes import FOLIAGE_DUSTY

# a-h dark -> silver (FOLIAGE_DUSTY); '.' a gap
LEGEND = dict(zip("asdfgh", FOLIAGE_DUSTY))

# Each leaf: a short slanting stroke (x, y) of 3 pixels up-right, silver on top, shadowed beneath.
LEAVES = [(1, 2), (6, 1), (11, 3), (14, 0), (3, 6), (8, 5), (13, 7), (0, 9), (5, 10), (10, 9), (15, 11),
          (2, 13), (7, 14), (12, 13), (9, 12), (4, 3)]
GAPS = [(4, 0), (13, 2), (0, 5), (9, 7), (15, 8), (6, 11), (11, 15), (3, 15), (14, 14)]


def build() -> str:
    g = [["f" if (x * 3 + y * 5) % 7 else "d" for x in range(16)] for y in range(16)]
    for x, y in LEAVES:
        for i in range(3):
            g[(y - i) % 16][(x + i) % 16] = "h" if i == 1 else "g"
            g[(y - i + 1) % 16][(x + i) % 16] = "d" if g[(y - i + 1) % 16][(x + i) % 16] not in "gh" else g[(y - i + 1) % 16][(x + i) % 16]
            g[(y - i + 2) % 16][(x + i) % 16] = "s" if g[(y - i + 2) % 16][(x + i) % 16] in "df" else g[(y - i + 2) % 16][(x + i) % 16]
    for x, y in GAPS:
        g[y][x] = "."
    return "\n".join("".join(r) for r in g)


GRID = build()

COMPARE = ["block/oak_leaves", "block/azalea_leaves", "block/mangrove_leaves"]
