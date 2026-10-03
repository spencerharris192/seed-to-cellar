"""Barley crop, growth stages 0-7 (crop model: the texture shows on 4 crossed planes).

Reads as one plant growing: leafy green tufts (0-3), stalks rising with ear buds (4),
upright green bearded ears (5), ears turning gold (6), then ripe ears that NOD
over under their weight, with trailing awns and dry leaves (7). The nodding,
bearded heads and warmer gold keep ripe barley distinct from upright vanilla wheat.
"""
from texturegen.compose import blank, stamp, stamp_bottom, to_grid
from texturegen.palettes import BARLEY, FOLIAGE_CROP

# digits = BARLEY (gold, dark -> light), letters q-y = FOLIAGE_CROP (green, dark -> light)
LEGEND = {**{str(i): BARLEY[i] for i in range(7)}, **dict(zip("qwerty", FOLIAGE_CROP))}

# Leafy tufts, drawn by hand at each size. Centre stalk is column 3 (col 2 for TUFT2).
TUFT0 = """
t.y
.e.
"""

TUFT1 = """
...y
y.t.
.rr.
.we.
"""

TUFT2 = """
....y
y..t.
.t.r.
.r.r.
..e..
..w..
"""

TUFT3 = """
....y..
....t..
y..r..y
t..r.t.
.t.r.r.
.r.rr..
r.ee...
.e.e...
.w.w...
"""

# The same tuft drying out as the grain ripens.
TUFT3_DRY = """
.......
.......
5..4..5
4..4.4.
.4.4.4.
.3.44..
3.33...
.3.3...
.2.2...
"""

# One ear shape for stages 5-7 so the head reads as the same plant ripening.
# Awns sweep up-right like a breeze through the field; the stalk continues below column 1.
#   A/B = awns (light/dark), K/L = kernels (light/dark), H = kernel highlight, S = stalk top
EAR_SHAPE = """
..A.B
.A.B.
A.B..
HL...
KL...
KL...
.S...
"""

EAR_COLORS = {
    "green":   {"A": "y", "B": "t", "H": "y", "K": "t", "L": "r", "S": "r"},
    "turning": {"A": "5", "B": "4", "H": "5", "K": "y", "L": "4", "S": "t"},
    "ripe":    {"A": "5", "B": "4", "H": "6", "K": "5", "L": "3", "S": "3"},
}


def darker(sprite: str) -> str:
    """The same sprite one grain shade darker (for dry leaves under pale heads: rye, oats)."""
    return "".join(str(int(ch) - 1) if ch.isdigit() and ch != "0" else ch for ch in sprite)


def ear(kind: str) -> str:
    return "".join(EAR_COLORS[kind].get(ch, ch) for ch in EAR_SHAPE)


def mirror(sprite: str) -> str:
    return "\n".join(line[::-1] for line in sprite.strip("\n").splitlines())


def plant(canvas, cx, tuft, ear_sprite, ear_top, stalk, flip=False):
    """A stalk rising from a leafy tuft to an ear. cx = stalk column."""
    stamp(canvas, ear_sprite, cx - 1, ear_top)
    for yy in range(ear_top + 7, 11):
        canvas[yy][cx] = stalk
    stamp_bottom(canvas, mirror(tuft) if flip else tuft, cx - (3 if not flip else 3))


def stage(tufts):
    c = blank()
    for i, (cx, tuft, dx) in enumerate(tufts):
        stamp_bottom(c, mirror(tuft) if i % 2 else tuft, cx - dx)
    return to_grid(c)


# Plant positions (stalk column, ear top row) shared by stages 5-7 so heads rise in place.
PLANTS = ((2, 3), (6, 2), (10, 4), (13, 3))


def eared_stage(kind, tuft, stalk, lift):
    c = blank()
    for i, (cx, top) in enumerate(PLANTS):
        plant(c, cx, tuft, ear(kind), top - lift, stalk, flip=bool(i % 2))
    return to_grid(c)


def stage4():
    c = blank()
    for i, (cx, top) in enumerate(PLANTS):
        top += 2
        c[top][cx], c[top][cx + 1] = "y", "t"
        c[top + 1][cx], c[top + 1][cx + 1] = "t", "r"
        for yy in range(top + 2, 9):
            c[yy][cx] = "r"
        stamp_bottom(c, mirror(TUFT3) if i % 2 else TUFT3, cx - 3)
    return to_grid(c)


TEXTURES = {
    "barley_stage0": stage([(2, TUFT0, 1), (7, TUFT0, 1), (12, TUFT0, 1)]),
    "barley_stage1": stage([(2, TUFT1, 1), (7, TUFT1, 1), (12, TUFT1, 1)]),
    "barley_stage2": stage([(2, TUFT2, 2), (6, TUFT2, 2), (10, TUFT2, 2), (13, TUFT2, 2)]),
    "barley_stage3": stage([(2, TUFT3, 3), (6, TUFT3, 3), (10, TUFT3, 3), (13, TUFT3, 3)]),
    "barley_stage4": stage4(),
    "barley_stage5": eared_stage("green", TUFT3, "r", 0),
    "barley_stage6": eared_stage("turning", TUFT3, "t", 1),
    "barley_stage7": eared_stage("ripe", TUFT3_DRY, "4", 2),
}
