"""Juniper bush, 4 looks: planted, young, flowering (tiny pale cones), ripe (frosty blue
berries). A small, spiky cone of dark blue-green needles: a conifer, not a leafy bush.
"""
from texturegen.compose import sibling
from texturegen.palettes import BLUEBERRY, FOLIAGE_DUSTY, OAT, ROOT

_b = sibling(__file__, "_bushes")

# q-y = FOLIAGE_DUSTY needles, 0-5 = BLUEBERRY (frosted berries), a = OAT (pale cones), n = ROOT
LEGEND = {**dict(zip("qwerty", FOLIAGE_DUSTY)), **{str(i): BLUEBERRY[i] for i in range(6)},
          "a": OAT[4], "n": ROOT[1]}

LEAVES = {"L": "r", "M": "e", "D": "w", "S": "n"}

# Alternating strokes read as needles; the outline zigzags.
CONE = """
......L.......
.....LML......
....LMDML.....
...LMDMDML....
..LMDMDMDML...
.LMDMDMDMDML..
LMDMDMDMDMDML.
.MDMDMDMDMDM..
..DMDMSMDMD...
....DDSDD.....
......S.......
"""

YOUNG = """
....L....
...LML...
..LMDML..
.LMDMDML.
LMDMSMDML
..DDSDD..
....S....
"""

SPOTS = ((5, 3), (8, 4), (3, 5), (10, 6), (6, 6), (2, 7), (8, 8), (4, 8))

looks = [_b.stage(_b.SPROUT, LEAVES), _b.stage(YOUNG, LEAVES),
         _b.stage(CONE, LEAVES, "a", SPOTS), _b.stage(CONE, LEAVES, "5\n3", SPOTS)]
TEXTURES = {f"juniper_stage{i}": look for i, look in enumerate(looks)}

COMPARE = ["block/spruce_sapling", "block/sweet_berry_bush_stage3"]
