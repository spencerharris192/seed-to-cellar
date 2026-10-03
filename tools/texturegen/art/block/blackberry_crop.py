"""Blackberry bramble, 4 looks: planted, young, flowering (pale pink-white flowers), ripe
(glossy purple-black berries). Not a rounded bush: arching, thorny canes with leaf clusters
along them, darker and wilder than the other bushes, so the thorns you feel make sense.
"""
from texturegen.compose import sibling
from texturegen.palettes import BLACKBERRY, FLOUR, FOLIAGE_CROP, GARLIC, ROOT

_b = sibling(__file__, "_bushes")

# q-y = FOLIAGE_CROP leaves, 0-5 = BLACKBERRY, a = FLOUR white, b = GARLIC pink tint, n/m = ROOT canes
LEGEND = {**dict(zip("qwerty", FOLIAGE_CROP)), **{str(i): BLACKBERRY[i] for i in range(6)},
          "a": FLOUR[5], "b": GARLIC[3], "n": ROOT[1], "m": ROOT[3]}

LEAVES = {"L": "t", "M": "r", "D": "e", "S": "n", "T": "m"}

# Arching canes (S, with a few lit thorn points T) carrying clusters of leaves.
BRAMBLE = """
....LL......L.
..LLMML...LML.
.LMMDML..LMDML
LMMDSSTSSSSDM.
.MDS...S..MDL.
..S.LLMS.LMMD.
.S.LMMDMSMMDML
S..MMDMT.SDMD.
..LMDDS...SD..
...MDS......S.
.....S........
"""

YOUNG = """
..LL.....
.LMML.L..
LMDSSTML.
.MS..SDM.
.S.LMSD..
S..MDS...
....S....
"""

SPOTS = ((1, 1), (5, 0), (11, 1), (12, 4), (4, 5), (9, 5), (2, 7), (10, 7))
FLOWERS = "ab"
BERRIES = "43\n21"

looks = [_b.stage(_b.SPROUT, LEAVES), _b.stage(YOUNG, LEAVES),
         _b.stage(BRAMBLE, LEAVES, FLOWERS, SPOTS), _b.stage(BRAMBLE, LEAVES, BERRIES, SPOTS)]
TEXTURES = {f"blackberry_stage{i}": look for i, look in enumerate(looks)}

COMPARE = ["block/sweet_berry_bush_stage3"]
