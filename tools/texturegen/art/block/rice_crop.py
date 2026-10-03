"""Rice crop: 4 looks, in a paddy. The lower texture fills the water block; stages 1-3 also
have a top texture that stands above the water (the model has a second set of planes one
block up).

Rice grows in clumps: three thin blades per clump, bundled at the base and spreading apart
as they rise, so the plant stays airy with sky between the blades. Stage 0: seedlings just
poking out of the water. 1: clumps rising above it. 2: green grain heads out and nodding.
3: ripe golden heads drooping heavily to one side, blades yellowing. The drooping golden
head over water is rice's silhouette.
"""
from texturegen.compose import blank, stamp, to_grid
from texturegen.palettes import FOLIAGE_CROP, RICE

# digits = RICE (golden straw, dark -> light), q-y = FOLIAGE_CROP (green, dark -> light)
LEGEND = {**{str(i): RICE[i] for i in range(6)}, **dict(zip("qwerty", FOLIAGE_CROP))}

CLUMPS = (4, 11)                                  # clump centers
GREEN = {"L": "t", "C": "y", "R": "r", "base": "e"}
DRY = {"L": "3", "C": "4", "R": "2", "base": "1"}

# A drooping head hanging off the center blade's tip to the right: grains get heavier toward
# the end. G = grain (lit), H = grain (shaded), s = stem.
PANICLE = """
sG..
.sGG
..GH
...H
"""

# The ripe head bends right over, heavy with grain.
PANICLE_RIPE = """
sGG.
.sGG
..GH
..HH
...H
"""

PANICLE_COLORS = {
    "green": {"s": "t", "G": "y", "H": "t"},
    "ripe":  {"s": "3", "G": "5", "H": "3"},
}


def recolor(sprite: str, colors: dict) -> str:
    return "".join(colors.get(ch, ch) for ch in sprite)


def put(c, x, y, ch):
    if 0 <= x < 16 and 0 <= y < 16:
        c[y][x] = ch


def lower(stage: int) -> str:
    """Under water: three blades per clump, bundled at the base, spreading above row 8."""
    c = blank()
    colors = DRY if stage == 3 else GREEN
    top = 3 if stage == 0 else 0                  # seedlings only just reach the waterline
    for cx in CLUMPS:
        for y in range(15, top - 1, -1):
            spread = max(0, 8 - y) // 3 if stage else 0
            put(c, cx - 1 - spread, y, colors["L"])
            put(c, cx, y, colors["C"])
            put(c, cx + 1 + spread, y, colors["R"])
        put(c, cx, 15, colors["base"])
    return to_grid(c)


def upper(stage: int) -> str:
    """Above the water: the blades carry on up and apart (1); heads come out and nod (2, 3)."""
    c = blank()
    colors = DRY if stage == 3 else GREEN
    top = {1: 9, 2: 6, 3: 7}[stage]
    for cx in CLUMPS:
        for y in range(15, top - 1, -1):
            spread = 2 + (15 - y) // 4             # the lower texture ended 2 apart
            put(c, cx - 1 - spread, y, colors["L"])
            put(c, cx, y, colors["C"])
            put(c, cx + 1 + spread, y, colors["R"])
        if stage >= 2:
            head = PANICLE_RIPE if stage == 3 else PANICLE
            stamp(c, recolor(head, PANICLE_COLORS["ripe" if stage == 3 else "green"]), cx, top - 1)
    return to_grid(c)


TEXTURES = {
    **{f"rice_stage{s}": lower(s) for s in range(4)},
    **{f"rice_stage{s}_top": upper(s) for s in range(1, 4)},
}

COMPARE = ["block/wheat_stage7", "block/seagrass", "block/tall_seagrass_top"]
