"""The Vintner's clothes (a 64x64 villager overlay, drawn over the villager's body and biome clothes):

- a soft beret in plum purple: the whole top of the hat layer and a band round its sides, drooping a pixel
  lower on one side, with a darker band edge;
- a long wine-red apron over the robe, from the chest to the hem, splashed with purple grape juice, a pocket
  on the front; brown leather straps over the shoulders and crossing the back, and a tie round the waist.

UV regions (vanilla villager model): hat layer at (32, 0) (top 40-47 x 0-7, sides y 8-17: right 32-39,
front 40-47, left 48-55, back 56-63); robe at (0, 38) (top 6-13 x 38-43, sides y 44-63: right 0-5,
front 6-13, left 14-19, back 20-27).
"""
from PIL import Image

from texturegen.core import hex_to_rgba
from texturegen.palettes import CLOTH, FRUIT_RED, PLUM

BERET = [hex_to_rgba(c) for c in (PLUM[1], PLUM[2], PLUM[3], PLUM[4])]
APRON = [hex_to_rgba(c) for c in (FRUIT_RED[0], FRUIT_RED[1], FRUIT_RED[2])]
STAIN = hex_to_rgba(PLUM[3])
LEATHER = [hex_to_rgba(c) for c in (CLOTH[0], CLOTH[1], CLOTH[2])]


def build_image() -> Image.Image:
    img = Image.new("RGBA", (64, 64), (0, 0, 0, 0))
    px = img.putpixel

    # beret: the top, shaded lighter toward the front-left, and a two-row band round the sides
    for x in range(40, 48):
        for y in range(0, 8):
            px((x, y), BERET[3] if (x - 40) + y < 5 else BERET[2] if (x - 40) + y < 10 else BERET[1])
    for x in range(32, 64):
        px((x, 8), BERET[2])
        px((x, 9), BERET[0] if x % 8 in (0, 7) else BERET[1])     # the band's shadowed edge
    for x in range(49, 56):                                       # drooping over one side
        px((x, 10), BERET[0])

    # apron: the robe's front from the chest down, the shading darker toward the hem
    for x in range(6, 14):
        for y in range(46, 64):
            px((x, y), APRON[2] if y < 52 else APRON[1] if y < 60 else APRON[0])
    for x, y in ((7, 49), (8, 50), (12, 48), (10, 53), (7, 61), (12, 62)):   # grape-juice splashes
        px((x, y), STAIN)
    for y in range(56, 59):                                        # a pocket, open at the top
        px((8, y), APRON[0])
        px((11, y), APRON[0])
    for x in range(8, 12):
        px((x, 59), APRON[0])

    # leather: straps over the shoulders (robe top and front), crossing on the back, and the waist tie all round
    for y in range(38, 44):
        px((7, y), LEATHER[1])
        px((12, y), LEATHER[1])
    for y in range(44, 46):
        px((7, y), LEATHER[2])
        px((12, y), LEATHER[2])
    for i in range(8):                                             # an X across the back
        px((20 + i, 44 + i), LEATHER[1])
        px((27 - i, 44 + i), LEATHER[1])
    for x in range(0, 28):
        px((x, 52), LEATHER[0] if x in (5, 6, 13, 14) else LEATHER[1])
    px((24, 53), LEATHER[1])                                       # the knot's ends hanging at the back
    px((25, 54), LEATHER[1])
    return img


COMPARE = ["entity/villager/profession/farmer", "entity/villager/profession/butcher"]
