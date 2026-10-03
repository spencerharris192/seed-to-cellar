"""Cucumber vine, 4 looks: planted, young, flowering (yellow flowers), ripe (long dark-green
cucumbers with pale speckles lying among the leaves). A sprawling vine of big, broad leaves
on twisting stems, lower and wider than the bushes.
"""
from texturegen.compose import sibling
from texturegen.palettes import CORN, FOLIAGE_FRESH, FOLIAGE_CROP

_b = sibling(__file__, "_bushes")

# q-y = FOLIAGE_FRESH leaves, 0-5 = FOLIAGE_CROP (the cucumbers' deeper greens), a/b = CORN
LEGEND = {**dict(zip("qwerty", FOLIAGE_FRESH)), **{str(i): FOLIAGE_CROP[i] for i in range(6)},
          "a": CORN[4], "b": CORN[3]}

LEAVES = {"L": "y", "M": "t", "D": "r", "S": "e"}

VINE = """
..LLL.....LL..
.LMMML...LMML.
LMMDMML.LMMDML
LMDMDML.LMDMDM
.MDDDM..SMDDM.
..MSS.SS..SM..
.LLS.S..LLL...
LMMML...LMMML.
LMDDMSSSMMDDM.
.MDDM....MDD..
"""

CUCUMBER = "25\n12\n21\n10"
SPOTS = ((5, 3), (12, 4), (6, 6), (1, 5), (11, 7))

looks = [_b.stage(_b.SPROUT, LEAVES), _b.stage(_b.YOUNG, LEAVES),
         _b.stage(VINE, LEAVES, "a\nb", SPOTS), _b.stage(VINE, LEAVES, CUCUMBER, SPOTS)]
TEXTURES = {f"cucumber_stage{i}": look for i, look in enumerate(looks)}

COMPARE = ["block/melon_stem", "block/sweet_berry_bush_stage3"]
