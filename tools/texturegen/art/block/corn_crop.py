"""Corn crop: 8 lower-block stages, plus the top half for stages 4-7 (a two-block plant).

One big plant per texture (the crop model shows each texture four times per block, so a
field still looks full): a thick central stalk with long leaves that arch out to both sides
and droop at the tips. From stage 5 ears grow on the stalk (green husk, a tuft of silk);
the top half ends in a branching tassel. Ripe corn (7): husks dried and opened on yellow
kernels, dry tan leaves, straw tassels. Reads apart from sorghum (one block, round red head
on top) by its height, the side ears and the tassel.
"""
from texturegen.compose import blank, stamp, to_grid
from texturegen.palettes import CORN, FOLIAGE_CROP, OAT, SORGHUM

# digits = CORN kernels, q-y = FOLIAGE_CROP (green), A-F = OAT (dry husk, tassel, straw),
# S/T = SORGHUM (corn silk)
LEGEND = {
    **{str(i): CORN[i] for i in range(6)},
    **dict(zip("qwerty", FOLIAGE_CROP)),
    **dict(zip("ABCDEF", OAT)),
    "S": SORGHUM[4], "T": SORGHUM[2],
}

# The stalk is 2 wide (lit left, shaded right) in the middle of the texture.
STALK = 7

# A long leaf arching out to the right from a node, then drooping; mirrored for the left.
LEAF = """
.yyt..
ttr.tr
.....e
.....e
"""

SPROUT = """
.y.
yty
.r.
.r.
"""

# An ear on the left of the stalk (it touches the stalk at its bottom-right), leaning up
# and out: silk (S) at the tip, then the husk (h lit, H, k shaded). Mirrored for the right.
EAR = """
S..
.S.
hhk
hHk
hHk
.hk
"""

# The ripe ear: the husk has dried and opened, showing rows of yellow kernels.
EAR_RIPE = """
T..
.45
445
343
34D
.DD
"""

TASSEL = """
.a.a.
..a..
a.a.a
.a.a.
..a..
"""

GREEN = {"y": "y", "t": "t", "r": "r", "e": "e"}
DRY = {"y": "E", "t": "D", "r": "C", "e": "B"}
EAR_COLORS = {
    "bud":   {"S": "F", "h": "t", "H": "r", "k": "r"},
    "green": {"S": "S", "h": "y", "H": "t", "k": "r"},
}
TASSEL_COLORS = {"bud": {"a": "y"}, "green": {"a": "E"}, "ripe": {"a": "C"}}


def mirror(sprite: str) -> str:
    return "\n".join(line[::-1] for line in sprite.strip("\n").splitlines())


def recolor(sprite: str, colors: dict) -> str:
    return "".join(colors.get(ch, ch) for ch in sprite)


def stalk(c, top: int, dry: bool, leaf_rows, first_right: bool = True):
    """The stalk from `top` down to the ground, with leaves alternating sides at `leaf_rows`."""
    lit, shade = ("D", "C") if dry else ("t", "r")
    leaf = recolor(LEAF, DRY if dry else GREEN)
    for yy in range(max(top, 0), 16):
        c[yy][STALK], c[yy][STALK + 1] = lit, shade
    for n, row in enumerate(leaf_rows):
        right = (n % 2 == 0) == first_right
        stamp(c, leaf if right else mirror(leaf), STALK + 2 if right else STALK - 6, row)


def ear(kind: str) -> str:
    return EAR_RIPE if kind == "ripe" else recolor(EAR, EAR_COLORS[kind])


def lower(stage: int) -> str:
    c = blank()
    if stage == 0:
        stamp(c, SPROUT, STALK - 1, 12)
        return to_grid(c)
    height = {1: 6, 2: 10, 3: 15}.get(stage, 16)
    top = 16 - height
    dry = stage == 7
    stalk(c, top, dry, range(top + 1, 14, 3))
    if stage >= 5:
        kind = {5: "bud", 6: "green", 7: "ripe"}[stage]
        stamp(c, ear(kind), STALK - 3, 3)                 # upper ear, left side
        if stage >= 6:
            stamp(c, mirror(ear(kind)), STALK + 2, 7)     # a second, lower ear on the right
    return to_grid(c)


def upper(stage: int) -> str:
    c = blank()
    height = {4: 5, 5: 9, 6: 12, 7: 12}[stage]
    top = 16 - height
    stalk(c, top, stage == 7, range(top + 2, 15, 4), first_right=False)
    if stage >= 5:
        kind = {5: "bud", 6: "green", 7: "ripe"}[stage]
        stamp(c, recolor(TASSEL, TASSEL_COLORS[kind]), STALK - 1, top - 5)
    return to_grid(c)


TEXTURES = {
    **{f"corn_stage{s}": lower(s) for s in range(8)},
    **{f"corn_stage{s}_top": upper(s) for s in range(4, 8)},
}

COMPARE = ["block/wheat_stage7", "block/sunflower_bottom", "block/sunflower_top"]
