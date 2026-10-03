"""The Brewer's Almanac's page filler (Patchouli draws it, 128x128, on a page left blank): a small ink drawing in the book's
brown, like an old almanac's tailpiece. A cask lies on its cradle, its staves curving over the bulge, two iron hoops at
each end and a tap in its head; behind it two ears of barley cross, their kernels in pairs up the stalk and their long
awns fanning out. Lines only, light on the page, so it never competes with the text beside it.
"""
import math

from PIL import Image, ImageDraw

from texturegen.core import hex_to_rgba
from texturegen.palettes import CLOTH

INK = hex_to_rgba(CLOTH[3])        # the main line
DARK = hex_to_rgba(CLOTH[2])       # hoops, kernels' shade, the tap
WASH = hex_to_rgba(CLOTH[4])       # a light touch on the staves

SIZE = 128
# The cask: its middle, half its length, half its height at the ends, and how much it bulges in the middle.
CX, CY, HALF_W, HALF_H, BULGE = 64, 86, 30, 15, 4


def _edge(x: float, sign: int) -> float:
    """The cask's top (sign -1) or bottom (+1) edge at x: rounded out toward the middle."""
    t = (x - CX) / HALF_W
    return CY + sign * (HALF_H + BULGE * (1 - t * t))


def _cask(d: ImageDraw.ImageDraw) -> None:
    left, right = CX - HALF_W, CX + HALF_W
    for sign in (-1, 1):                                       # the outline, top and bottom
        d.line([(x, round(_edge(x, sign))) for x in range(left, right + 1)], fill=INK)
    for x in (left, right):                                    # the heads, seen edge on: a gentle curve
        bow = -1 if x == left else 1
        top, bottom = round(_edge(x, -1)), round(_edge(x, 1))
        d.line([(x + round(bow * 2 * math.sin(math.pi * (y - top) / (bottom - top))), y) for y in range(top, bottom + 1)], fill=INK)
    for k in (-0.5, 0, 0.5):                                   # stave lines, following the bulge
        pts = []
        for x in range(left + 3, right - 2):
            t = (x - CX) / HALF_W
            pts.append((x, round(CY + k * (HALF_H + BULGE * (1 - t * t)))))
        d.line(pts, fill=WASH if k else INK)
    for x in (left + 6, left + 9, right - 9, right - 6):      # two hoops at each end
        d.line([(x, round(_edge(x, -1)) + 1), (x, round(_edge(x, 1)) - 1)], fill=DARK)
    # The tap in the right-hand head: a short spout and its handle.
    d.line([(right + 2, CY + 2), (right + 7, CY + 2)], fill=DARK)
    d.line([(right + 7, CY + 2), (right + 7, CY + 6)], fill=DARK)
    d.line([(right + 4, CY - 1), (right + 4, CY + 1)], fill=DARK)
    d.point((right + 7, CY + 8), fill=WASH)                    # a drop
    # The cradle: two wedges under it, and the ground line.
    for x in (left + 8, right - 8):
        base = round(_edge(x, 1))
        d.polygon([(x - 5, base + 6), (x + 5, base + 6), (x + 2, base + 1), (x - 2, base + 1)], outline=INK)
    d.line([(left - 10, CY + HALF_H + BULGE + 7), (right + 14, CY + HALF_H + BULGE + 7)], fill=WASH)


def _ear(d: ImageDraw.ImageDraw, base: tuple[int, int], angle: float) -> None:
    """An ear of barley on its stalk, leaning `angle` degrees from upright (negative to the left)."""
    a = math.radians(angle)
    ux, uy = math.sin(a), -math.cos(a)                         # up the stalk
    px, py = -uy, ux                                           # across it
    bx, by = base
    length = 74
    tip = (bx + ux * length, by + uy * length)
    d.line([base, (round(tip[0]), round(tip[1]))], fill=INK)  # the stalk, behind the cask lower down

    def at(along: float, across: float) -> tuple[int, int]:
        return round(bx + ux * along + px * across), round(by + uy * along + py * across)

    for i in range(6):                                         # kernels in pairs up the ear, each a short grain
        along = 50 + i * 4
        for side in (-1, 1):
            d.line([at(along - 1.5, side * 2), at(along + 1.5, side * 2.6)], fill=INK)
            d.point(at(along, side * 1.2), fill=WASH)
            d.line([at(along + 1.5, side * 2.6), at(along + 12, side * 6.5)], fill=WASH)   # its awn, fanning out
    d.line([at(length, 0), at(length + 10, 0)], fill=WASH)    # the top awn


def build_image() -> Image.Image:
    img = Image.new("RGBA", (SIZE, SIZE), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    _ear(d, (46, 98), 30)                                      # crossing behind the cask
    _ear(d, (82, 98), -30)
    # Clear the ears where the cask stands in front of them, then draw the cask.
    for x in range(CX - HALF_W, CX + HALF_W + 1):
        for y in range(round(_edge(x, -1)), round(_edge(x, 1)) + 1):
            img.putpixel((x, y), (0, 0, 0, 0))
    _cask(d)
    return img


COMPARE = []
