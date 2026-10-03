"""Grape leaves: two broad, five-lobed vine leaves, one over the other, with pale veins running
from the stalk; lit from the top-left.
"""
import math

from texturegen.compose import blank, to_grid
from texturegen.palettes import FOLIAGE_CROP

# a-h leaf (FOLIAGE_CROP dark -> light); v veins
LEGEND = {**dict(zip("asdfgh", FOLIAGE_CROP)), "v": FOLIAGE_CROP[5]}


def leaf(c, cx: float, cy: float, r: float, back: bool) -> None:
    """A palmate leaf: five round lobes around its heart, the stalk at the bottom."""
    lobes = [(0, -1.25), (-1.05, -0.55), (1.05, -0.55), (-0.8, 0.55), (0.8, 0.55)]
    inside = set()
    for y in range(16):
        for x in range(16):
            px, py = x + 0.5, y + 0.5
            if (px - cx) ** 2 + (py - cy) ** 2 <= (r * 1.05) ** 2 or any(
                    (px - (cx + lx * r)) ** 2 + (py - (cy + ly * r)) ** 2 <= (r * 0.62) ** 2 for lx, ly in lobes):
                inside.add((x, y))
    for x, y in inside:
        rim = any((x + dx, y + dy) not in inside for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)))
        lit = (cx - x) + (cy - y)             # toward the top-left is lit
        if back:
            c[y][x] = "a" if rim else ("d" if lit > 0 else "s")
        else:
            c[y][x] = "s" if rim else ("g" if lit > 2.5 else "f" if lit > -1 else "d")
    if not back:
        base = (round(cx - 0.5), round(cy - 0.5 + r * 0.5))
        for lx, ly in lobes:                  # veins from the base out toward each lobe
            for t in (0.35, 0.7):
                x = round(base[0] + (cx + lx * r * 0.9 - base[0] - 0.5) * t)
                y = round(base[1] + (cy + ly * r * 0.9 - base[1] - 0.5) * t)
                if (x, y) in inside and c[y][x] != "s":
                    c[y][x] = "v" if lx <= 0 else "h"


def build() -> str:
    c = blank()
    leaf(c, 10.0, 6.2, 3.2, back=True)       # the second leaf, behind and to the right
    leaf(c, 6.8, 8.6, 3.6, back=False)
    for x, y in [(7, 13), (7, 14), (6, 15)]:  # the stalk
        c[y][x] = "d"
    return to_grid(c)


GRID = build()

COMPARE = ["item/kelp", "block/oak_leaves", "item/lily_pad"]
