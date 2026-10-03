"""Coffee bush, 4 looks: planted, young, flowering (white star flowers), ripe (clusters of red
coffee cherries). Glossy, deep green leaves on brown stems.
"""
from texturegen.compose import sibling
from texturegen.palettes import FLOUR, FOLIAGE_CROP, FRUIT_RED, ROOT

_b = sibling(__file__, "_bushes")

# q-y = FOLIAGE_CROP leaves (the deeper greens: glossy), 0-5 = FRUIT_RED, a/b = FLOUR, n = ROOT
LEGEND = {**dict(zip("qwerty", FOLIAGE_CROP)), **{str(i): FRUIT_RED[i] for i in range(6)},
          "a": FLOUR[5], "b": FLOUR[3], "n": ROOT[2]}

LEAVES = {"L": "r", "M": "e", "D": "w", "S": "n"}
FLOWERS = ".a\nab"
CHERRIES = "43\n32"

TEXTURES = {f"coffee_stage{i}": look for i, look in enumerate(_b.four(LEAVES, FLOWERS, CHERRIES))}

COMPARE = ["block/sweet_berry_bush_stage3"]
