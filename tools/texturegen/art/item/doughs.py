"""Doughs and batters: one soft lump, recolored and marked per kind.

- dough: pale cream, smooth.
- rye_dough: grey-brown and a little flatter.
- sourdough_dough: cream with small fermentation bubbles on its skin.
- spent_grain_dough: cream flecked with brown brewing grain.
- beer_bread_dough: warmer tan with amber flecks from the ale.
- cornbread_batter: glossy golden-yellow.
- masa: pale corn-yellow, matte.
"""
from texturegen.compose import sibling
from texturegen.palettes import CORN, FLOUR, MALT_AMBER, OAT, RYE

shapes = sibling(__file__, "_shapes")

# Ramps (rim, then dark -> light): a-e dough, f-j rye, k-o tan, p-t corn, u-y masa; flecks F G, bubble h
LEGEND = {
    **dict(zip("abcde", [OAT[2], OAT[3], OAT[4], OAT[5], FLOUR[5]])),
    **dict(zip("fghij", RYE[1:6])),
    **dict(zip("klmno", [OAT[1], OAT[2], OAT[3], OAT[4], OAT[5]])),
    **dict(zip("pqrst", CORN[1:6])),
    **dict(zip("uvwxy", [OAT[2], OAT[3], OAT[4], CORN[5], CORN[5]])),
    "F": MALT_AMBER[1],
    "G": MALT_AMBER[3],
}


def lump(letters: str, ry: float = 4.6, height: float = 0.9) -> list[list[str]]:
    return shapes.shade(shapes.dome(8.0, 9.2, 6.2, ry, angle=-8, height=height), letters)


def with_marks(g, points, ch):
    shapes.put(g, points, ch)
    return shapes.to_str(g)


BUBBLES = [(5, 7), (9, 6), (11, 9), (7, 10), (4, 10), (10, 12)]
FLECKS = [(5, 8), (8, 7), (11, 8), (6, 11), (9, 10), (12, 11), (4, 9), (10, 6)]

TEXTURES = {
    "dough": shapes.to_str(lump("abcde")),
    "rye_dough": shapes.to_str(lump("fghij", ry=4.2)),
    "sourdough_dough": with_marks(lump("abcde"), BUBBLES, "a"),
    "spent_grain_dough": with_marks(lump("abcde"), FLECKS, "F"),
    "beer_bread_dough": with_marks(lump("klmno"), FLECKS[::2], "G"),
    "cornbread_batter": shapes.to_str(lump("pqrst", ry=4.0, height=0.7)),
    "masa": shapes.to_str(lump("uvwxy", ry=4.4)),
}

COMPARE = ["item/slime_ball", "item/bread", "item/cookie"]
