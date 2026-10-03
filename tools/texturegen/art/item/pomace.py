"""What the Fruit Press leaves behind:

- grape_pomace: a pressed clump of dark red-purple skins, pale seeds showing.
- fruit_pomace: a clump of tan-orange pulp with darker bits of peel.
- olive_pomace: a glossy clump of dark olive-brown paste, a pit or two.
- bagasse: a loose bundle of dry, pale, stringy stalk fiber.

The clumps are lumpy (a few overlapping domes, lit from the top-left, like the other foods).
"""
from texturegen.compose import sibling
from texturegen.palettes import MALT_AMBER, OAT, OLIVE, RED_GRAPE, STRAW

shapes = sibling(__file__, "_shapes")

LEGEND = {
    **dict(zip("abcde", RED_GRAPE[0:5])),
    **dict(zip("fghij", MALT_AMBER[0:5])),
    **dict(zip("klmno", OLIVE[0:5])),
    **dict(zip("pqrst", STRAW[0:5])),
    "S": OAT[5],
}


def clump(letters: str) -> list[list[str]]:
    g = [["."] * 16 for _ in range(16)]
    for cx, cy, rx, ry in ((6.0, 9.6, 4.2, 3.6), (10.4, 9.0, 3.8, 3.4), (8.2, 6.8, 3.6, 3.0), (8.6, 11.4, 4.6, 2.8)):
        part = shapes.shade(shapes.dome(cx, cy, rx, ry, height=0.8), letters)
        for y in range(16):
            for x in range(16):
                if part[y][x] != ".":
                    g[y][x] = part[y][x]
    # mashed, not smooth: flecks a shade darker and lighter all over the inside
    inside = letters[1:]
    for y in range(16):
        for x in range(16):
            if g[y][x] in inside and g[y][x] != letters[0]:
                i = inside.index(g[y][x])
                if (x * 7 + y * 3) % 5 == 0 and i > 0:
                    g[y][x] = inside[i - 1]
                elif (x * 3 + y * 11) % 7 == 0 and i < len(inside) - 1:
                    g[y][x] = inside[i + 1]
    return g


def grape_pomace() -> str:
    g = clump("abcde")
    shapes.put(g, [(6, 8), (9, 10), (11, 7), (7, 12)], "S", only_on="bcd")    # seeds
    shapes.put(g, [(8, 9), (10, 12), (5, 11)], "a", only_on="cde")          # folds in the skins
    return shapes.to_str(g)


def fruit_pomace() -> str:
    g = clump("fghij")
    shapes.put(g, [(6, 9), (10, 8), (8, 12), (11, 11)], "g", only_on="ij")  # bits of peel
    return shapes.to_str(g)


def olive_pomace() -> str:
    g = clump("klmno")
    shapes.put(g, [(7, 7), (8, 7)], "o")                                     # the oily shine
    shapes.put(g, [(10, 10), (6, 11)], "k", only_on="lmn")                   # pits
    return shapes.to_str(g)


def bagasse() -> str:
    g = [["."] * 16 for _ in range(16)]
    # long fibers lying on the diagonal, in a loose bundle, a few strands straying
    for i, (x0, y0, length, ch) in enumerate([(2, 12, 11, "r"), (3, 12, 11, "s"), (3, 13, 10, "q"), (4, 11, 10, "t"),
                                              (2, 11, 9, "s"), (5, 12, 9, "r"), (4, 13, 8, "q"), (3, 10, 8, "t")]):
        for k in range(length):
            x, y = x0 + k, y0 - k + (k // 4 if i % 3 == 0 else 0)
            if 0 <= x < 16 and 0 <= y < 16:
                g[y][x] = ch
    for x, y in [(13, 2), (14, 2), (12, 4), (1, 13)]:
        g[y][x] = "p"
    return shapes.to_str(g)


TEXTURES = {
    "grape_pomace": grape_pomace(),
    "fruit_pomace": fruit_pomace(),
    "olive_pomace": olive_pomace(),
    "bagasse": bagasse(),
}

COMPARE = ["item/brown_mushroom", "item/cocoa_beans", "item/wheat"]
