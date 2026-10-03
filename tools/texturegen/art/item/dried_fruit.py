"""The orchard's dried fruit (Drying Rack), each shaped the way it's cut and dried:

- prunes: a heap of big, wrinkled, near-black plums with a dusty purple sheen.
- dried_cherries: a heap of small, shriveled dark red cherries (the raisin heap, in cherry colors).
- dried_apples: two overlapping apple rings: pale chewy flesh, a thin red skin edge and the cored hole.
- dried_peaches: three leathery orange half-moon slices, the curved skin side darker.
- dried_pears: three flat pear-shaped slices, tan-gold (browner than fresh pear) with a darker rim and a speck of core.
"""
import math

from texturegen.compose import blank, sibling, to_grid
from texturegen.palettes import CORN, FRUIT_RED, OAT, ONION, PEACH, PLUM

shapes = sibling(__file__, "_shapes")
raisins = sibling(__file__, "raisins")

# p-t prune; A B C cherry; a-d apple flesh, r s apple skin; e-h peach; i-l pear, k core
LEGEND = {
    **dict(zip("pqrst", [PLUM[0], PLUM[1], PLUM[2], PLUM[3], PLUM[5]])),
    "A": FRUIT_RED[0], "B": FRUIT_RED[1], "C": FRUIT_RED[3],
    **dict(zip("abcd", OAT[2:6])),
    "x": FRUIT_RED[2], "y": FRUIT_RED[3],
    **dict(zip("efgh", PEACH[1:5])),
    "i": ONION[2], "j": ONION[3], "l": CORN[4], "m": CORN[5],
    "k": OAT[1],
}


def prunes() -> str:
    g = blank()
    # back to front: big wrinkled ovals, each shaded with a crease across it
    for cx, cy, angle in ((6.0, 6.4, -20), (10.6, 6.9, 15), (4.6, 10.4, 10), (8.6, 10.6, -10), (12.0, 11.0, 25)):
        shape = shapes.dome(cx, cy, 2.9, 2.2, angle=angle, height=0.8)
        part = shapes.shade(shape, "pqrrs")
        for x, y in shape:
            g[y][x] = part[y][x]
        shapes.put(g, [(round(cx - 1.5), round(cy)), (round(cx - 0.5), round(cy + 0.4)), (round(cx + 0.5), round(cy))], "q",
                   only_on="rs")                                # the wrinkle
        shapes.put(g, [(round(cx - 1.2), round(cy - 1.2))], "t", only_on="rs")   # a dusty sheen
    return to_grid(g)


def rings() -> str:
    g = blank()
    for cx, cy in ((6.0, 6.5), (10.0, 10.0)):                 # back to front
        for y in range(16):
            for x in range(16):
                d = math.hypot(x + 0.5 - cx, (y + 0.5 - cy) * 1.15)
                if d <= 4.1:
                    lit = (cx - x - 0.5) + (cy - y - 0.5)
                    if d <= 1.1:
                        g[y][x] = "."                           # the cored hole
                    elif d > 3.4:
                        g[y][x] = "y" if lit > 0 else "x"       # the skin edge
                    elif d <= 1.8:
                        g[y][x] = "b"                           # the hole's shaded rim
                    else:
                        g[y][x] = "d" if lit > 1.2 else "c" if lit > -1.0 else "b"
    return to_grid(g)


def slices(skin: str, flesh: str, pear: bool) -> str:
    """Three slices lying flat, overlapping. Peach: half-moons; pear: the pear's outline with a core speck."""
    g = blank()
    for cx, cy, turn in ((5.0, 5.5, -30), (10.5, 7.0, 35), (6.5, 11.0, 5)):
        cells = {}
        a = math.radians(turn)
        for y in range(16):
            for x in range(16):
                ox, oy = x + 0.5 - cx, y + 0.5 - cy
                dx, dy = ox * math.cos(a) + oy * math.sin(a), -ox * math.sin(a) + oy * math.cos(a)
                if pear:
                    # a pear outline: a small circle on top of a larger one
                    inside = math.hypot(dx, dy - 0.9) <= 2.6 or math.hypot(dx, dy + 1.6) <= 1.6
                else:
                    # a half-moon: a disc cut straight across (the stone side), the skin round the curve
                    inside = math.hypot(dx, dy * 1.1) <= 3.4 and dy <= 0.8
                if inside:
                    cells[(x, y)] = (cx - x - 0.5) + (cy - y - 0.5)
        for (x, y), lit in cells.items():
            rim = any((x + ex, y + ey) not in cells for ex, ey in ((1, 0), (-1, 0), (0, 1), (0, -1)))
            g[y][x] = skin[0] if rim and lit < 0 else skin[1] if rim else flesh[2] if lit > 1.0 else flesh[1] if lit > -1.0 else flesh[0]
        if pear:
            shapes.put(g, [(round(cx - 0.5), round(cy + 0.4))], "k")   # a speck of core
    return to_grid(g)


TEXTURES = {
    "prunes": prunes(),
    "dried_cherries": raisins.heap("A", "B", "C"),
    "dried_apples": rings(),
    "dried_peaches": slices("ef", "fgh", pear=False),
    "dried_pears": slices("ij", "jlm", pear=True),
}

COMPARE = ["item/dried_kelp", "item/sweet_berries", "item/cookie"]
