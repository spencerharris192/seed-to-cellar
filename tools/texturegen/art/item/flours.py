"""Milled grains: the wheat flour sack, filled with something else (one base, recolored):

- rye_flour: a grey-brown heap.
- cornmeal: a gritty yellow heap.
- rolled_oats: a heap of pale flakes, each flake edged in shadow so it reads as flakes, not powder.
"""
from texturegen.compose import sibling
from texturegen.palettes import CLOTH, CORN, OAT, RYE

flour = sibling(__file__, "wheat_flour")

# A-F sack cloth (as wheat flour); each heap's digits 0-5 become that grain's letters.
LEGEND = {
    **dict(zip("ABCDEF", CLOTH)),
    **dict(zip("ghijkl", RYE)),
    **dict(zip("mnopqr", CORN)),
    **dict(zip("stuvwx", OAT)),
}


def filled(letters: str, flakes: bool = False) -> str:
    rows = [[letters[int(ch)] if ch.isdigit() else ch for ch in r] for r in flour.GRID.strip("\n").splitlines()]
    if flakes:   # shadowed edges between flakes on the heap
        for x, y in ((6, 1), (8, 2), (5, 3), (10, 3), (7, 3)):
            if rows[y][x] != ".":
                rows[y][x] = letters[2]
    return "\n".join("".join(r) for r in rows)


TEXTURES = {
    "rye_flour": filled("ghijkl"),
    "cornmeal": filled("mnopqr"),
    "rolled_oats": filled("stuvwx", flakes=True),
}

COMPARE = ["item/sugar", "item/wheat", "item/bone_meal"]
