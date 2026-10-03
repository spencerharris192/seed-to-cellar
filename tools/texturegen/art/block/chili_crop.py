"""Chili plant, 4 looks: planted, young, flowering (small white flowers), ripe (slim red peppers
hanging down, each with a green cap). A compact, bright green bush.
"""
from texturegen.compose import sibling
from texturegen.palettes import FLOUR, FOLIAGE_CROP, FRUIT_RED

_b = sibling(__file__, "_bushes")

# q-y = FOLIAGE_CROP leaves, 0-5 = FRUIT_RED, a/b = FLOUR (white flowers)
LEGEND = {**dict(zip("qwerty", FOLIAGE_CROP)), **{str(i): FRUIT_RED[i] for i in range(6)},
          "a": FLOUR[5], "b": FLOUR[3]}

LEAVES = {"L": "y", "M": "t", "D": "r", "S": "e"}
FLOWERS = "a"
PEPPER = "e\n4\n3\n2"

SPOTS = ((2, 4), (5, 3), (9, 2), (12, 4), (7, 6), (3, 7), (10, 7))

looks = _b.four(LEAVES, FLOWERS, PEPPER, spots=SPOTS)
TEXTURES = {f"chili_stage{i}": look for i, look in enumerate(looks)}

COMPARE = ["block/sweet_berry_bush_stage3"]
