"""Fermenting Vat screen. Panel: wide liquid gauge (8,16 26x54), yeast slot (43,16), lees slot
(43,52), lid button (64,16), progress track (86,64 80x5).
Sprites: gauge marks (176,0 24x52), lid open (200,0), lid closed (218,0), hover frame (200,18),
progress fill (176,52 80x5)."""
import importlib.util
from pathlib import Path

from PIL import ImageDraw

from texturegen.core import hex_to_rgba
from texturegen.palettes import GUI, HOP_CONE, IRON, WOOD_OAK

_spec = importlib.util.spec_from_file_location("_gui", Path(__file__).with_name("_gui.py"))
_gui = importlib.util.module_from_spec(_spec)
_spec.loader.exec_module(_gui)

LID_OPEN = """
..................
..................
.ddddddddddddddd..
.dcccccccccccccb..
..bbbbbbbbbbbbb...
..................
..................
..a............a..
..a............a..
..aeddddddddddea..
..aedcccccccccea..
..aedcccccccccea..
..aedcccccccccea..
..aebbbbbbbbbbea..
..aaaaaaaaaaaaaa..
..................
..................
..................
"""

LID_CLOSED = """
..................
.......nn.........
.......mm.........
..ddddddddddddddd.
..dccccccccccccbb.
..aaaaaaaaaaaaaaa.
..aeddddddddddea..
..aedcccccccccea..
..aedcccccccccea..
..aedcccccccccea..
..aedcccccccccea..
..aedcccccccccea..
..aedcccccccccea..
..aebbbbbbbbbbea..
..aaaaaaaaaaaaaa..
..................
..................
..................
"""

LEGEND = {**dict(zip("abcdef", WOOD_OAK)), "m": IRON[2], "n": IRON[4]}


def build_image():
    img = _gui.canvas()
    _gui.panel(img, 0, 0, 176, 166)
    d = ImageDraw.Draw(img)
    d.rectangle([8, 16, 33, 69], fill=hex_to_rgba(GUI[3]))
    d.line([8, 16, 32, 16], fill=hex_to_rgba(GUI[1]))
    d.line([8, 16, 8, 68], fill=hex_to_rgba(GUI[1]))
    d.line([9, 69, 33, 69], fill=hex_to_rgba(GUI[5]))
    d.line([33, 17, 33, 69], fill=hex_to_rgba(GUI[5]))
    _gui.slot(img, 43, 16)
    _gui.slot(img, 43, 52)
    _gui.slot(img, 64, 16)
    d.rectangle([86, 64, 165, 68], fill=hex_to_rgba(GUI[2]))
    _gui.player_inventory(img)

    for i in range(1, 4):
        ty = round(52 * i / 4)
        d.line([176, ty, 176 + (5 if i == 2 else 3), ty], fill=hex_to_rgba(GUI[1]))
    _gui.sprite(img, LID_OPEN, LEGEND, 200, 0)
    _gui.sprite(img, LID_CLOSED, LEGEND, 218, 0)
    d.rectangle([200, 18, 217, 35], outline=hex_to_rgba(GUI[5]))
    d.rectangle([176, 52, 255, 56], fill=hex_to_rgba(HOP_CONE[3]))
    d.line([176, 52, 255, 52], fill=hex_to_rgba(HOP_CONE[5]))
    d.line([176, 56, 255, 56], fill=hex_to_rgba(HOP_CONE[1]))
    return img
