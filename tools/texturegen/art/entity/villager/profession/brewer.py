"""The Brewer's clothes (a 64x64 villager overlay, drawn over the villager's body and biome clothes), unlike the
Vintner's beret and red apron at a glance:

- a hop-green flat cap: the top of the hat layer, a band round its sides, and a peak over the face;
- a heavy brown leather apron over the robe, chest to hem, with a brass buckle at the chest, a pale linen towel
  tucked in at one hip and a darker scuffed hem; straps over the shoulders, straight down the back, and a waist tie.

UV regions (vanilla villager model): hat layer at (32, 0) (top 40-47 x 0-7, sides y 8-17: right 32-39,
front 40-47, left 48-55, back 56-63); robe at (0, 38) (top 6-13 x 38-43, sides y 44-63: right 0-5,
front 6-13, left 14-19, back 20-27).
"""
from PIL import Image

from texturegen.core import hex_to_rgba
from texturegen.palettes import FLOUR, GOLD, HOP_CONE, ROOT

CAP = [hex_to_rgba(c) for c in (HOP_CONE[0], HOP_CONE[1], HOP_CONE[2], HOP_CONE[3])]
LEATHER = [hex_to_rgba(c) for c in (ROOT[0], ROOT[1], ROOT[2], ROOT[3])]
BRASS = [hex_to_rgba(c) for c in (GOLD[2], GOLD[4])]
TOWEL = [hex_to_rgba(c) for c in (FLOUR[2], FLOUR[3], FLOUR[5])]


def build_image() -> Image.Image:
    img = Image.new("RGBA", (64, 64), (0, 0, 0, 0))
    px = img.putpixel

    # flat cap: the top, lit toward the front-left; a band round the sides; a peak over the face
    for x in range(40, 48):
        for y in range(0, 8):
            px((x, y), CAP[3] if (x - 40) + y < 6 else CAP[2])
    for x in range(32, 64):
        px((x, 8), CAP[2])
        px((x, 9), CAP[1])
    for x in range(40, 48):                                        # the peak, shadowed underneath
        px((x, 10), CAP[0])

    # apron: heavy leather from the chest down, darker and scuffed toward the hem
    for x in range(6, 14):
        for y in range(45, 64):
            px((x, y), LEATHER[3] if y < 50 else LEATHER[2] if y < 60 else LEATHER[1])
    for x, y in ((8, 54), (11, 57), (9, 61), (12, 62)):          # scuffs
        px((x, y), LEATHER[1])
    for x in (9, 10):                                              # the brass buckle at the chest
        px((x, 46), BRASS[1])
        px((x, 47), BRASS[0])
    for y in range(53, 59):                                        # a linen towel tucked in at the hip
        px((6, y), TOWEL[2] if y < 55 else TOWEL[1])
        px((7, y), TOWEL[1] if y < 57 else TOWEL[0])

    # straps over the shoulders and straight down the back; a tie round the waist
    for y in range(38, 45):
        px((7, y), LEATHER[1])
        px((12, y), LEATHER[1])
    for y in range(44, 53):
        px((21, y), LEATHER[1])
        px((26, y), LEATHER[1])
    for x in range(0, 28):
        px((x, 52), LEATHER[0] if x in (5, 6, 13, 14) else LEATHER[1])
    return img


COMPARE = ["entity/villager/profession/butcher", "entity/villager/profession/leatherworker"]
