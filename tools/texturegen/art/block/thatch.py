"""Thatch: overlapping courses of straw, like a thatched roof seen up close.

Two 8-pixel courses. Each starts with the shadow where the course above overlaps it, then the
straws run downward getting lighter, each column keeping its own shade so single straws read
as lines; the bottom edge is ragged, with short straws letting the shadow below show through.
The second course is shifted half a block so the rows don't line up.
"""
from texturegen.palettes import STRAW

LEGEND = {str(i): STRAW[i] for i in range(6)}

# Shade of each row of a course (0 = the overlap shadow) and each straw's own offset.
ROW_TONE = [1, 2, 3, 3, 3, 4, 4, 3]
STRAW_OFFSET = [0, -1, 1, 0, -1, 0, 1, -1, 0, 1, -1, 0, 0, -1, 1, 0]
# How many rows each straw reaches (short ones leave the next course's shadow showing).
STRAW_LENGTH = [8, 7, 8, 8, 6, 8, 7, 8, 8, 8, 7, 6, 8, 7, 8, 8]


def course() -> list[list[str]]:
    rows = [["0"] * 16 for _ in range(8)]
    for x in range(16):
        for y in range(8):
            if y == 0:
                tone = 0 if x % 5 == 2 else 1
            elif y >= STRAW_LENGTH[x]:
                tone = 1   # gap: shadow of the course below
            else:
                tone = max(1, min(5, ROW_TONE[y] + STRAW_OFFSET[x]))
            rows[y][x] = str(tone)
    return rows


def thatch() -> str:
    top = course()
    bottom = [row[8:] + row[:8] for row in course()]
    return "\n".join("".join(r) for r in top + bottom)


GRID = thatch()

COMPARE = ["block/hay_block_side", "block/hay_block_top", "block/oak_planks"]
