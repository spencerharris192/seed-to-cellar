"""Shared shapes for bushes (a helper, not a texture: files starting with _ aren't rendered).

Drawn in role letters: L = lit leaf, M = mid leaf, D = shaded leaf, S = stem/wood. Each bush
file maps those onto its own leaf ramp and adds its flowers (stage 2) and fruit (stage 3)
at the spots listed here, so every bush grows the same way but looks like itself.
"""
from texturegen.compose import blank, plants, recolor, stamp, stamp_bottom, to_grid

SPROUT = """
L.L
MLM
.S.
.S.
"""

YOUNG = """
..L.L....
.LMLMLL..
LMMDMMML.
.MDMMDML.
..MDSDM..
...DSD...
....S....
"""

FULL = """
.....LL.L.....
...LLMLLML.L..
..LMMLMMLMLL..
.LMMDMMLMMDML.
LMMDMMMDMMDMML
LMDMMDMMMDMMD.
.MMDMMDMMDMMD.
..DMMDSMDMMD..
...DDMSSMDD...
.....DSD......
......S.......
"""

# Where flowers or fruit sit on FULL (top-left of each small sprite), as (x, y) within it.
SPOTS = ((2, 3), (5, 1), (9, 1), (11, 3), (1, 5), (6, 4), (10, 5), (3, 7), (8, 7))


def stage(shape: str, leaves: dict, dots: str | None = None, spots=SPOTS, center: int = 8) -> str:
    """One bush look: `shape` in `leaves` colors, with `dots` (a small sprite) at each spot."""
    body = recolor(shape, leaves)
    c = plants(body, (center,))
    if dots:
        rows = body.strip("\n").splitlines()
        left, top = center - len(rows[0]) // 2, 16 - len(rows)
        for x, y in spots:
            stamp(c, dots, left + x, top + y)
    return to_grid(c)


def four(leaves: dict, flowers: str, fruit: str, young=YOUNG, full=FULL, spots=SPOTS) -> list[str]:
    """The usual four looks: planted, young, flowering, ripe."""
    return [stage(SPROUT, leaves), stage(young, leaves), stage(full, leaves, flowers, spots), stage(full, leaves, fruit, spots)]


__all__ = ["SPROUT", "YOUNG", "FULL", "SPOTS", "stage", "four", "blank", "stamp", "stamp_bottom", "to_grid", "recolor"]
