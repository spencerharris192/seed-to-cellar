"""Oat crop, growth stages 0-7 (crop model: the texture shows on 4 crossed planes).

Oats don't make a spike at all: the stalk opens into a loose, drooping panicle whose
spikelets hang from thin branches like little bells. That open, dangling shape is what
tells oats apart from barley, rye and wheat at a glance. Ripe oats are the palest grain,
almost cream. Young oats are ordinary fresh crop green (the same tufts as barley).
"""
import importlib.util
from pathlib import Path

from texturegen.compose import blank, stamp, stamp_bottom, to_grid
from texturegen.palettes import FOLIAGE_CROP, OAT

_spec = importlib.util.spec_from_file_location("_barley", Path(__file__).with_name("barley_crop.py"))
_barley = importlib.util.module_from_spec(_spec)
_spec.loader.exec_module(_barley)

# digits = OAT (straw, dark -> light), letters q-y = FOLIAGE_CROP (green, dark -> light)
LEGEND = {**{str(i): OAT[i] for i in range(6)}, **dict(zip("qwerty", FOLIAGE_CROP))}

TUFT0, TUFT1, TUFT2, TUFT3, TUFT3_DRY = (_barley.TUFT0, _barley.TUFT1, _barley.TUFT2,
                                          _barley.TUFT3, _barley.darker(_barley.TUFT3_DRY))
mirror = _barley.mirror

# One panicle: a nodding stem (S) with thin stalks (B) out to hanging spikelets, each a lit
# top (H) over a shaded bottom (K), staggered left and right so it droops rather than
# branching like a cross. The stalk continues below column 2.
PANICLE = """
...S..
..S...
.BS.B.
H.S..H
K.SB.K
.BS.H.
H.S.K.
K.S...
"""

# Just emerging: the panicle is still folded tight against the stem.
PANICLE_YOUNG = """
..S..
.HS..
.KSH.
..SK.
..S..
"""

PANICLE_COLORS = {
    "green":   {"S": "r", "B": "r", "H": "y", "K": "t"},
    "turning": {"S": "t", "B": "t", "H": "5", "K": "y"},
    # ripe: darker straw stems so the cream bells are the brightest thing on the plant
    "ripe":    {"S": "2", "B": "2", "H": "5", "K": "3"},
}

# Stalk column and panicle top for each plant: three plants, each needing room to spread.
PLANTS = ((2, 2), (7, 1), (12, 3))


def recolor(sprite: str, kind: str) -> str:
    return "".join(PANICLE_COLORS[kind].get(ch, ch) for ch in sprite)


def stage(tufts):
    c = blank()
    for i, (cx, tuft, dx) in enumerate(tufts):
        stamp_bottom(c, mirror(tuft) if i % 2 else tuft, cx - dx)
    return to_grid(c)


def headed_stage(head, stalk, tuft, drop, tufts_at=(2, 7, 12)):
    c = blank()
    rows = len(head.strip("\n").splitlines()) if head else 0
    for i, (cx, top) in enumerate(PLANTS):
        top += drop
        if head:
            stamp(c, head, cx - 2, top)
        for yy in range(top + rows, 12):
            c[yy][cx] = stalk
    for i, cx in enumerate(tufts_at):
        stamp_bottom(c, mirror(tuft) if i % 2 else tuft, cx - 3)
    return to_grid(c)


TEXTURES = {
    "oat_stage0": stage([(2, TUFT0, 1), (7, TUFT0, 1), (12, TUFT0, 1)]),
    "oat_stage1": stage([(2, TUFT1, 1), (7, TUFT1, 1), (12, TUFT1, 1)]),
    "oat_stage2": stage([(2, TUFT2, 2), (6, TUFT2, 2), (10, TUFT2, 2), (13, TUFT2, 2)]),
    "oat_stage3": stage([(2, TUFT3, 3), (6, TUFT3, 3), (10, TUFT3, 3), (13, TUFT3, 3)]),
    "oat_stage4": headed_stage(None, "r", TUFT3, 3),
    "oat_stage5": headed_stage(recolor(PANICLE_YOUNG, "green"), "r", TUFT3, 2),
    "oat_stage6": headed_stage(recolor(PANICLE, "turning"), "t", TUFT3, 1),
    "oat_stage7": headed_stage(recolor(PANICLE, "ripe"), "2", TUFT3_DRY, 0),
}

COMPARE = ["block/wheat_stage7", "block/wheat_stage4"]
