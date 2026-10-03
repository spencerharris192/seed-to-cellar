"""Rye crop, growth stages 0-7 (crop model: the texture shows on 4 crossed planes).

Young rye is a blue-green (glaucous) grass, so even the seedlings read cooler than barley.
It grows taller and slimmer than barley: long straight stalks, a thin leaf on each, and
narrow upright spikes with short awns ticking out on both sides. Ripe rye is a dusty
grey-gold, where barley is warm gold with long sweeping awns and heads that nod over.
"""
import importlib.util
from pathlib import Path

from texturegen.compose import blank, stamp, stamp_bottom, to_grid
from texturegen.palettes import FOLIAGE_DUSTY, RYE

_spec = importlib.util.spec_from_file_location("_barley", Path(__file__).with_name("barley_crop.py"))
_barley = importlib.util.module_from_spec(_spec)
_spec.loader.exec_module(_barley)

# digits = RYE (grey-gold, dark -> light), letters q-y = FOLIAGE_DUSTY (blue-green, dark -> light)
LEGEND = {**{str(i): RYE[i] for i in range(6)}, **dict(zip("qwerty", FOLIAGE_DUSTY))}

# Barley's hand-drawn tufts use the same letters, so they show here in rye's colors.
TUFT0, TUFT1, TUFT2, TUFT3, TUFT3_DRY = (_barley.TUFT0, _barley.TUFT1, _barley.TUFT2,
                                          _barley.TUFT3, _barley.darker(_barley.TUFT3_DRY))
mirror = _barley.mirror

# One narrow spike, the same shape from emerging to ripe. The stalk continues below column 1.
#   A = awn tick, K/L = kernels (lit / shaded side)
SPIKE = """
A.A
.K.
AKL
.KL
AKL
.KL
.L.
"""

SPIKE_COLORS = {
    "green":   {"A": "y", "K": "t", "L": "r"},
    "turning": {"A": "5", "K": "4", "L": "t"},
    "ripe":    {"A": "5", "K": "4", "L": "3"},
}

# Stalk column and spike top for each plant; rye stands taller than barley.
PLANTS = ((2, 0), (6, 1), (10, 0), (13, 2))


def spike(kind: str) -> str:
    return "".join(SPIKE_COLORS[kind].get(ch, ch) for ch in SPIKE)


def stage(tufts):
    c = blank()
    for i, (cx, tuft, dx) in enumerate(tufts):
        stamp_bottom(c, mirror(tuft) if i % 2 else tuft, cx - dx)
    return to_grid(c)


def tall_stage(head, stalk, leaf, tuft, drop):
    """Stalks with a thin leaf each, topped by `head` (a sprite, or None for the boot stage)."""
    c = blank()
    for i, (cx, top) in enumerate(PLANTS):
        top += drop
        if head:
            stamp(c, head, cx - 1, top)
            first = top + 7
        else:  # boot stage: the closed head is a pale swelling on the stalk
            c[top + 2][cx], c[top + 3][cx], c[top + 4][cx] = "y", "t", "t"
            first = top + 5
        for yy in range(first, 12):
            c[yy][cx] = stalk
        side = 1 if i % 2 == 0 else -1           # leaves alternate sides
        c[top + 9][cx + side] = leaf
        c[top + 8][cx + 2 * side] = leaf
        stamp_bottom(c, mirror(tuft) if i % 2 else tuft, cx - 3)
    return to_grid(c)


TEXTURES = {
    "rye_stage0": stage([(2, TUFT0, 1), (7, TUFT0, 1), (12, TUFT0, 1)]),
    "rye_stage1": stage([(2, TUFT1, 1), (7, TUFT1, 1), (12, TUFT1, 1)]),
    "rye_stage2": stage([(2, TUFT2, 2), (6, TUFT2, 2), (10, TUFT2, 2), (13, TUFT2, 2)]),
    "rye_stage3": stage([(2, TUFT3, 3), (6, TUFT3, 3), (10, TUFT3, 3), (13, TUFT3, 3)]),
    "rye_stage4": tall_stage(None, "r", "t", TUFT3, 2),
    "rye_stage5": tall_stage(spike("green"), "r", "t", TUFT3, 2),
    "rye_stage6": tall_stage(spike("turning"), "t", "4", TUFT3, 1),
    # ripe: darker straw stalks and leaves so the pale spikes stand out
    "rye_stage7": tall_stage(spike("ripe"), "2", "2", TUFT3_DRY, 0),
}

COMPARE = ["block/wheat_stage7", "block/wheat_stage4"]
