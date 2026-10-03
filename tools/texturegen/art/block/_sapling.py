"""Helper (not a texture): a fruit tree sapling, like vanilla's saplings: a thin young stem that forks into
two side branches, each ending in a small clump of leaves, with a larger clump at the top; lit from the
top-left, and a couple of blossoms or tiny fruit on it so each tree's sapling reads apart.

Letters: t T stem (dark, lit); a s d f g leaves (outline, dark -> light); x y accent (dark, light).
"""
from texturegen.compose import blank, to_grid

STEM = [(7, 15), (7, 14), (7, 13), (8, 12), (8, 11), (7, 10), (7, 9), (7, 8), (7, 7), (8, 6)]
BRANCHES = [(6, 10), (5, 9), (4, 9), (9, 8), (10, 7), (11, 7)]
# Leaf clumps (x, y, radius), back to front: the side clumps, then the crown.
CLUMPS = [(4.0, 7.4, 2.5), (11.6, 5.8, 2.5), (7.8, 3.6, 3.2)]
ACCENTS = [(3, 7), (11, 4), (8, 2)]


def clump(c, cx: float, cy: float, r: float) -> None:
    inside = {(x, y) for y in range(16) for x in range(16) if (x + 0.5 - cx) ** 2 + ((y + 0.5 - cy) * 1.15) ** 2 <= r * r}
    for x, y in inside:
        rim = any((x + dx, y + dy) not in inside for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)))
        lit = (cx - x - 0.5) + (cy - y - 0.5)
        c[y][x] = "a" if rim else "g" if lit > 1.2 else "f" if lit > -0.4 else "d" if lit > -1.6 else "s"


def draw() -> str:
    c = blank()
    for x, y in STEM:
        c[y][x] = "T" if y > 11 else "t"
    c[15][6] = "t"                          # a root flare
    for x, y in BRANCHES:
        c[y][x] = "t"
    for cx, cy, r in CLUMPS:
        clump(c, cx, cy, r)
    for x, y in ACCENTS:                    # a blossom or a tiny fruit: light pixel over a dark one
        c[y][x] = "y"
        c[y + 1][x] = "x"
    return to_grid(c)
