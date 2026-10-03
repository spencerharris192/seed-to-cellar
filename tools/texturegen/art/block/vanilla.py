"""Vanilla on a jungle log (models block/vanilla_stage0-3, built in datagen):

- vanilla_vine: the vine lying flat on the bark (cut out): a fleshy green stem winding up the middle, broad oval leaves
  alternating left and right, small aerial roots gripping toward the edges. The shoot (stage 0) shows its lower middle.
- vanilla_flower: the pale yellow-green orchid flower, one small patch (the parts sample its top-left).
- vanilla_pod: a long green pod, lit down one side (sampled top-left).
"""
from texturegen.palettes import FOLIAGE_FRESH, LEMON, PEAR

# 0-5 leaves and stem; f g h flower; p q r pod
LEGEND = {
    **{str(i): FOLIAGE_FRESH[i] for i in range(6)},
    "f": PEAR[3], "g": LEMON[4], "h": LEMON[5],
    "p": FOLIAGE_FRESH[2], "q": FOLIAGE_FRESH[3], "r": FOLIAGE_FRESH[4],
}

VINE = """
.......32.......
.......32..34...
..43...32.3443..
.3443..321443...
..343..3211.....
...11.23........
.....223........
.....32...43....
..1..32..3443...
.343.32.34443...
34443.3214......
.3341.321.......
....11.32.......
.......32..1....
......232.343...
......32.3443...
"""




def rows(fn):
    return "\n".join("".join(fn(x, y) for x in range(16)) for y in range(16))


def flower(x, y):
    if x == 0 or y == 0:
        return "h"                    # the lit edge of the petals
    return "g" if (x + y) % 3 else "f"


def pod(x, y):
    return "r" if x == 0 else ("q" if (x + y) % 4 else "p")


TEXTURES = {
    "vanilla_vine": VINE,
    "vanilla_flower": rows(flower),
    "vanilla_pod": rows(pod),
}

COMPARE = ["block/vine", "block/cocoa_stage1"]
