"""Wild red grape (cross model): a low, sprawling vine scrambling over the ground, broad leaves and
two bunches showing. Same leaf and bunch shapes as the trellis vine (red_grape_crop.py);
wild_white_grape.py recolors it."""
from texturegen.compose import blank, sibling, stamp, to_grid

vine = sibling(__file__, "red_grape_crop")

LEGEND = vine.LEGEND


def build() -> str:
    c = blank()
    # woody runners arching out from the root
    for x, y in [(7, 15), (7, 14), (6, 13), (5, 12), (4, 11), (3, 10), (3, 9), (4, 8)]:
        c[y][x] = "w"
    for x, y in [(8, 15), (8, 14), (9, 13), (10, 12), (11, 11), (12, 10), (12, 9), (11, 8)]:
        c[y][x] = "w"
    for x, y in [(7, 13), (7, 12), (8, 11), (8, 10), (7, 9), (7, 8)]:
        c[y][x] = "W"
    for x, y in [(2, 8), (13, 8), (6, 7)]:      # tendrils
        c[y][x] = "d"
    for sprite, x, y in [(vine.LEAF_L, 0, 10), (vine.LEAF_R, 11, 10), (vine.LEAF_L, 1, 4), (vine.LEAF_R, 10, 4),
                         (vine.LEAF_R, 6, 3)]:
        stamp(c, sprite, x, y)
    for x, y in [(4, 7), (9, 8)]:
        stamp(c, vine.BUNCH, x, y)
    return to_grid(c)


GRID = build()

COMPARE = ["block/sweet_berry_bush_stage3", "block/fern", "block/vine"]
