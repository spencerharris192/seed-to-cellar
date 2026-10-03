"""Kiln screen (256x256 sheet). Furnace-like layout plus roast swatches.

Panel (0,0 176x166): input slot (55,16), fuel slot (55,52), empty flame (57,37),
empty arrow (79,34), big output slot (111,30), swatch slots (143,16/34/52), player inventory.
Sprites on the right: lit flame (176,0 14x14), full arrow (176,14 24x17),
roast swatches pale/amber/black (176/192/208,31 16x16), selection frame (176,47 18x18).
"""
import importlib.util
from pathlib import Path

from PIL import ImageDraw

from texturegen.core import hex_to_rgba
from texturegen.palettes import BARLEY, FIRE, GUI, MALT_AMBER, MALT_BLACK

_spec = importlib.util.spec_from_file_location("_gui", Path(__file__).with_name("_gui.py"))
_gui = importlib.util.module_from_spec(_spec)
_spec.loader.exec_module(_gui)

FLAME = """
......4.......
.....45.......
.....44.4.....
....343.45....
....3443443...
...33443344...
...3234433.3..
..323344433...
..3223443323..
.322233443323.
.3221233322...
..22112222.2..
...21112212...
....111111....
"""

ARROW = """
..............5.........
..............55........
..............555.......
..............5555......
..............55555.....
555555555555555555555...
5555555555555555555555..
55555555555555555555555.
555555555555555555555555
55555555555555555555555.
5555555555555555555555..
555555555555555555555...
..............55555.....
..............5555......
..............555.......
..............55........
..............5.........
"""


def swatch(img, x, y, ramp):
    """A 16x16 color chip with its own light/dark bevel."""
    d = ImageDraw.Draw(img)
    d.rectangle([x, y, x + 15, y + 15], fill=hex_to_rgba(ramp[3]))
    d.rectangle([x + 2, y + 2, x + 13, y + 13], fill=hex_to_rgba(ramp[2]))
    d.rectangle([x + 3, y + 3, x + 12, y + 12], fill=hex_to_rgba(ramp[3]))
    d.line([x, y, x + 14, y], fill=hex_to_rgba(ramp[5]))
    d.line([x, y, x, y + 14], fill=hex_to_rgba(ramp[5]))
    d.line([x + 1, y + 15, x + 15, y + 15], fill=hex_to_rgba(ramp[0]))
    d.line([x + 15, y + 1, x + 15, y + 15], fill=hex_to_rgba(ramp[0]))
    d.point((x + 5, y + 5), fill=hex_to_rgba(ramp[4]))
    d.point((x + 6, y + 5), fill=hex_to_rgba(ramp[4]))
    d.point((x + 5, y + 6), fill=hex_to_rgba(ramp[4]))


def build_image():
    img = _gui.canvas()
    _gui.panel(img, 0, 0, 176, 166)
    _gui.slot(img, 55, 16)
    _gui.slot(img, 55, 52)
    _gui.slot(img, 111, 30, 26)
    _gui.player_inventory(img)
    # empty flame and arrow in the panel (grey), filled versions drawn over them in game
    grey = {"1": GUI[3], "2": GUI[3], "3": GUI[3], "4": GUI[3], "5": GUI[3]}
    _gui.sprite(img, FLAME, grey, 57, 37)
    _gui.sprite(img, ARROW, grey, 79, 34)
    # recess behind the roast swatches
    for i in range(3):
        _gui.slot(img, 143, 16 + i * 18)

    fire = {"1": FIRE[1], "2": FIRE[2], "3": FIRE[3], "4": FIRE[4], "5": FIRE[5]}
    _gui.sprite(img, FLAME, fire, 176, 0)
    _gui.sprite(img, ARROW, {"5": GUI[5]}, 176, 14)
    swatch(img, 176, 31, BARLEY)
    swatch(img, 192, 31, MALT_AMBER)
    swatch(img, 208, 31, MALT_BLACK)
    frame = ImageDraw.Draw(img)
    frame.rectangle([176, 47, 193, 64], outline=hex_to_rgba(GUI[5]))
    frame.rectangle([177, 48, 192, 63], outline=hex_to_rgba(GUI[0]))
    return img
