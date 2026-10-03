"""Brew Kettle screen. Panel: liquid gauge (8,16 18x54), ingredient slots in a 2x2 block
(34/52, 16/34), heat flame under them (44,54), progress arrow (84,34), bowl/bottle slot (124,12)
above the output slot (124,34). The empty bowl slot shows a faint bowl as a hint.
Sprites: lit flame (176,0), full arrow (176,14), gauge measuring marks (176,31 16x52)."""
import importlib.util
from pathlib import Path

from PIL import ImageDraw

from texturegen.core import hex_to_rgba
from texturegen.palettes import FIRE, GUI


def _load(name):
    spec = importlib.util.spec_from_file_location(name, Path(__file__).with_name(name + ".py"))
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    return module


_gui = _load("_gui")
_kiln = _load("kiln")

# A bowl seen from the side, drawn in the slot's own darker grey (like vanilla's empty armor slots).
BOWL_HINT = """
..............
..............
..............
..............
..............
.11111111111..
.12222222221..
..122222221...
..112222211...
...1111111....
.....111......
..............
..............
..............
"""


def gauge_marks(img, x, y, w, h):
    """Short tick marks on the left of a gauge, drawn over the liquid."""
    d = ImageDraw.Draw(img)
    for i in range(1, 4):
        ty = y + round(h * i / 4)
        d.line([x, ty, x + (5 if i == 2 else 3), ty], fill=hex_to_rgba(GUI[1]))


def build_image():
    img = _gui.canvas()
    _gui.panel(img, 0, 0, 176, 166)
    # tall gauge: slot-style border around 16x52
    d = ImageDraw.Draw(img)
    d.rectangle([8, 16, 25, 69], fill=hex_to_rgba(GUI[3]))
    d.line([8, 16, 24, 16], fill=hex_to_rgba(GUI[1]))
    d.line([8, 16, 8, 68], fill=hex_to_rgba(GUI[1]))
    d.line([9, 69, 25, 69], fill=hex_to_rgba(GUI[5]))
    d.line([25, 17, 25, 69], fill=hex_to_rgba(GUI[5]))
    for sx, sy in ((34, 16), (52, 16), (34, 34), (52, 34)):
        _gui.slot(img, sx, sy)
    _gui.slot(img, 124, 12)
    _gui.sprite(img, BOWL_HINT, {"1": GUI[2], "2": GUI[3]}, 126, 14)
    _gui.slot(img, 124, 34)
    grey = {c: GUI[3] for c in "12345"}
    _gui.sprite(img, _kiln.FLAME, grey, 44, 54)   # unlit flame and empty arrow, as on every station screen
    _gui.sprite(img, _kiln.ARROW, grey, 84, 34)
    _gui.player_inventory(img)

    fire = {"1": FIRE[1], "2": FIRE[2], "3": FIRE[3], "4": FIRE[4], "5": FIRE[5]}
    _gui.sprite(img, _kiln.FLAME, fire, 176, 0)
    _gui.sprite(img, _kiln.ARROW, {"5": GUI[5]}, 176, 14)
    gauge_marks(img, 176, 31, 16, 52)
    return img
