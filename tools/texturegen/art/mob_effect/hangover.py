"""Hangover effect icon (18x18): a heavy grey storm cloud with a throbbing lightning bolt."""
from texturegen.palettes import GOLD as Y, STONE as S

LEGEND = {
    "o": S[0], "1": S[1], "2": S[2], "3": S[3], "4": S[4], "5": S[5],
    "q": Y[1], "y": Y[3], "Y": Y[5],
}

# The bolt's filled pixels (lit edge Y on the left of each row); its outline is added around it.
BOLT = """
....Yy
...Yy.
..Yy..
.Yyyyy
...Yy.
..Yy..
.Yy...
.y....
"""


def _grid() -> str:
    g = [["."] * 18 for _ in range(18)]
    bumps = [(5.5, 6.8, 3.1), (10.5, 5.2, 4.0), (14.2, 7.8, 2.7)]   # (x, y, radius) puffs
    inside = set()
    for y in range(18):
        for x in range(18):
            px, py = x + 0.5, y + 0.5
            if any((px - bx) ** 2 + (py - by) ** 2 <= r * r for bx, by, r in bumps) or (2.4 <= px <= 16.6 and 7 <= py <= 10.6):
                inside.add((x, y))
    top = min(y for _, y in inside)
    for x, y in inside:
        rim = any((x + dx, y + dy) not in inside for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)))
        # lit from the top-left: lighter toward the top and left, darkest along the heavy base
        t = (y - top) / 9 + (x - 2) / 40
        g[y][x] = "o" if rim else "5" if t < 0.22 else "4" if t < 0.45 else "3" if t < 0.7 else "2"
    bolt = {}
    for y, row in enumerate(BOLT.strip().splitlines()):
        for x, ch in enumerate(row):
            if ch != ".":
                bolt[(6 + x, 9 + y)] = ch
    for (x, y), ch in bolt.items():
        g[y][x] = ch
        for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)):
            n = (x + dx, y + dy)
            if n not in bolt and 0 <= n[0] < 18 and 0 <= n[1] < 18:
                g[n[1]][n[0]] = "q"
    return "\n".join("".join(r) for r in g)


GRID = _grid()
