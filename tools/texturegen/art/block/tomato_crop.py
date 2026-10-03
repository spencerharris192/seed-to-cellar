"""Tomato plant, 4 looks: planted, young, flowering (yellow flowers), ripe (round red tomatoes
with a lit top-left and a green cap). Soft fresh-green leaves, green stems.
"""
from texturegen.compose import sibling
from texturegen.palettes import CORN, FOLIAGE_FRESH, FRUIT_RED

_b = sibling(__file__, "_bushes")

# q-y = FOLIAGE_FRESH leaves, 0-5 = FRUIT_RED, a/b = CORN (yellow flowers)
LEGEND = {**dict(zip("qwerty", FOLIAGE_FRESH)), **{str(i): FRUIT_RED[i] for i in range(6)},
          "a": CORN[4], "b": CORN[3]}

LEAVES = {"L": "y", "M": "t", "D": "r", "S": "e"}
FLOWERS = "a\nb"
TOMATO = ".w.\n453\n432\n.1."

SPOTS = ((2, 3), (8, 1), (11, 4), (5, 5), (1, 6), (9, 6))

looks = _b.four(LEAVES, FLOWERS, TOMATO, spots=SPOTS)
TEXTURES = {f"tomato_stage{i}": look for i, look in enumerate(looks)}

COMPARE = ["block/sweet_berry_bush_stage3"]
