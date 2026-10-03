"""Refreshed effect icon (18x18): a fresh mint leaf with a drop of cool water above it."""
import math

from texturegen.palettes import FOLIAGE_FRESH as G, WATER as W

LEGEND = {
    "o": G[0], "a": G[1], "b": G[2], "c": G[3], "e": G[4], "f": G[5],
    "0": W[0], "2": W[2], "3": W[3], "4": W[4], "5": W[5],
}

DROP = """
..0..
.050.
05540
05430
04320
.000.
"""


def _grid() -> str:
    g = [["."] * 18 for _ in range(18)]
    # Leaf: an ellipse tilted 45 degrees, tip to the top-right, stem to the bottom-left.
    cx, cy, rx, ry, a = 10.5, 9.5, 7.4, 4.0, math.radians(-45)
    inside = set()
    for y in range(18):
        for x in range(18):
            dx, dy = x + 0.5 - cx, y + 0.5 - cy
            u = (dx * math.cos(a) + dy * math.sin(a)) / rx
            v = (-dx * math.sin(a) + dy * math.cos(a)) / ry
            if u * u + v * v <= 1.0:
                inside.add((x, y))
    for x, y in inside:
        rim = any((x + dx, y + dy) not in inside for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)))
        side = (x - cx) + (y - cy)   # below zero: the upper-left half, facing the light
        g[y][x] = "o" if rim else ("f" if side < -3.5 else "e" if side < -1 else "c" if side < 1.5 else "b")
    midrib = []
    for i in range(-5, 5):
        x, y = round(cx - 0.5 + i * 0.707), round(cy - 0.5 - i * 0.707)
        if (x, y) in inside and g[y][x] != "o":
            g[y][x] = "a"
            midrib.append((x, y))
    # side veins: short strokes angled toward the tip, a shade darker than the leaf around them
    for x, y in ((7, 9), (9, 7), (11, 5), (9, 11), (11, 9), (13, 7)):
        if (x, y) in inside and g[y][x] not in "oa":
            g[y][x] = "c" if g[y][x] in "ef" else "a"
    for x, y in ((4, 14), (3, 15), (2, 16)):   # stem
        g[y][x] = "a"
    for y, row in enumerate(DROP.strip().splitlines()):
        for x, ch in enumerate(row):
            if ch != ".":
                g[y][1 + x] = ch
    return "\n".join("".join(r) for r in g)


GRID = _grid()
