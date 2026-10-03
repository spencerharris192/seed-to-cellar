"""Trellis: a garden lattice panel between two posts. Tiles vertically (trellises stack).

Posts on both edges, diagonal slats every 8 px in both directions. Where slats
cross, one passes over (highlight) and the other under (shadow), like a real weave.
Light from the top-left: left post lit, right post shadowed.
"""
from texturegen.palettes import WOOD_OAK

LEGEND = {str(i): WOOD_OAK[i] for i in range(6)}


def _build():
    grid = [["."] * 16 for _ in range(16)]
    for y in range(16):
        grid[y][0], grid[y][1] = "2", "4"      # left post
        grid[y][14], grid[y][15] = "3", "1"    # right post
    for y in range(16):
        for x in range(2, 14):
            down = (x - y) % 8 == 0            # "\" slats
            up = (x + y) % 8 == 7              # "/" slats
            if down and up:
                grid[y][x] = "5"               # "\" passes over here
            elif down:
                grid[y][x] = "4"
            elif up:
                # the "/" slat dips under: darken right next to a crossing
                near_cross = any((x + d - y) % 8 == 0 for d in (-1, 1))
                grid[y][x] = "2" if near_cross else "3"
    return "\n".join("".join(r) for r in grid)


GRID = _build()

COMPARE = ["block/ladder", "block/oak_planks", "block/scaffolding_side"]
