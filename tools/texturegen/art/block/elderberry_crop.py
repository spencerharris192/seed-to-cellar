"""Elderberry shrub, 4 looks: planted, young, flowering (flat, lacy cream flower heads on top:
the elderflowers you can pick), ripe (heavy clusters of dark purple berries drooping).
Fresh green leaves on pale brown stems.
"""
from texturegen.compose import sibling
from texturegen.palettes import BLACKBERRY, FOLIAGE_FRESH, OAT, ROOT

_b = sibling(__file__, "_bushes")

# q-y = FOLIAGE_FRESH leaves, 0-5 = BLACKBERRY (berries), a/b = OAT (cream flowers), n = ROOT
LEGEND = {**dict(zip("qwerty", FOLIAGE_FRESH)), **{str(i): BLACKBERRY[i] for i in range(6)},
          "a": OAT[5], "b": OAT[3], "n": ROOT[3]}

LEAVES = {"L": "y", "M": "t", "D": "r", "S": "n"}

# Flat-topped umbels sit on top of the bush; berry clusters hang lower.
UMBEL = "abab\n.bb."
CLUSTER = "4.4\n323\n.2."

UMBEL_SPOTS = ((0, 0), (5, 0), (10, 1), (2, 3), (8, 3))
CLUSTER_SPOTS = ((1, 3), (5, 2), (10, 3), (3, 6), (8, 6), (11, 6))

looks = [_b.stage(_b.SPROUT, LEAVES), _b.stage(_b.YOUNG, LEAVES),
         _b.stage(_b.FULL, LEAVES, UMBEL, UMBEL_SPOTS), _b.stage(_b.FULL, LEAVES, CLUSTER, CLUSTER_SPOTS)]
TEXTURES = {f"elderberry_stage{i}": look for i, look in enumerate(looks)}

COMPARE = ["block/sweet_berry_bush_stage3"]
