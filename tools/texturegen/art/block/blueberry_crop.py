"""Blueberry bush, 4 looks: planted, young, flowering (little white bells), ripe (clusters of
dusty-blue berries). A rounded bush of dusty blue-green leaves on brown twigs.
"""
from texturegen.compose import sibling
from texturegen.palettes import BLUEBERRY, FLOUR, FOLIAGE_DUSTY, ROOT

_b = sibling(__file__, "_bushes")

# q-y = FOLIAGE_DUSTY leaves, 0-5 = BLUEBERRY, a/b = FLOUR (flowers), n = ROOT (twigs)
LEGEND = {**dict(zip("qwerty", FOLIAGE_DUSTY)), **{str(i): BLUEBERRY[i] for i in range(6)},
          "a": FLOUR[5], "b": FLOUR[2], "n": ROOT[2]}

LEAVES = {"L": "t", "M": "r", "D": "e", "S": "n"}
FLOWERS = "a\nb"
BERRIES = "54\n32"

TEXTURES = {f"blueberry_stage{i}": look for i, look in enumerate(_b.four(LEAVES, FLOWERS, BERRIES))}

COMPARE = ["block/sweet_berry_bush_stage3", "block/sweet_berry_bush_stage2"]
