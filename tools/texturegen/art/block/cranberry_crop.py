"""Cranberry, 4 looks: planted, young, flowering (little pink flowers), ripe (bright red
berries). A low, creeping mat of tiny leaves, not a bush: it hugs the ground by the water.
"""
from texturegen.compose import sibling
from texturegen.palettes import FOLIAGE_CROP, FRUIT_RED, GARLIC, ROOT

_b = sibling(__file__, "_bushes")

# q-y = FOLIAGE_CROP leaves, 0-5 = FRUIT_RED, a = GARLIC (pink flowers), n = ROOT (runners)
LEGEND = {**dict(zip("qwerty", FOLIAGE_CROP)), **{str(i): FRUIT_RED[i] for i in range(6)},
          "a": GARLIC[3], "n": ROOT[2]}

LEAVES = {"L": "t", "M": "r", "D": "e", "S": "n"}

MAT = """
.L.L..L.L.L.L..
LMLMLLMLMLMLML.
MDMDMMDMDMDMDML
.DSDSDDSDSDSDD.
"""

YOUNG = """
.L.L.L..
LMLMLML.
MDMDMDML
.DSDSD..
"""

SPOTS = ((1, 0), (4, 1), (7, 0), (10, 1), (13, 1), (3, 2), (9, 2))

looks = [_b.stage(_b.SPROUT, LEAVES), _b.stage(YOUNG, LEAVES),
         _b.stage(MAT, LEAVES, "a", SPOTS), _b.stage(MAT, LEAVES, "4\n2", SPOTS)]
TEXTURES = {f"cranberry_stage{i}": look for i, look in enumerate(looks)}

COMPARE = ["block/sweet_berry_bush_stage3"]
