"""Pot Still screen (176x186 panel). Pot gauge (8,16 18x54), the Gin Basket's four slots (29/47, 16/34) over a faint copper
basket, the charcoal filter slot under them (38,53, a faint lump of charcoal as a hint), the empty arrow (70,35) toward the
receiver gauge (98,16 18x54) and the narrow stillage gauge (120,16 12x54), an unlit flame (9,75) by the status text, and the
player inventory (7,103).
Sprites on the right: lit flame (176,0), full arrow (176,14), gauge marks 16x52 (176,31) and 10x52 (192,31), and the
"Run again" button: normal (202,31), hovered (202,49), unavailable (202,67), 18x18 each.
"""
import importlib.util
from pathlib import Path

from PIL import ImageDraw

from texturegen.core import hex_to_rgba
from texturegen.palettes import COPPER, FIRE, GUI


def _load(name):
    spec = importlib.util.spec_from_file_location(name, Path(__file__).with_name(name + ".py"))
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    return module


_gui = _load("_gui")
_kiln = _load("kiln")
_kettle = _load("brew_kettle")

# A lump of charcoal, in the slot's darker grey (like vanilla's empty-slot hints).
CHARCOAL_HINT = """
..............
..............
..............
.....111......
...1122211....
..122222221...
..1222222221..
...122222221..
....11222211..
......1111....
..............
..............
..............
..............
"""

# "Run again": an arrow bending back round on itself.
RUN_AGAIN = """
................
................
................
.....11111......
....1.....1.....
...1.......1....
...1.......1....
...1......111...
...1.......1....
...1............
....1...........
.....11111......
................
................
................
................
"""


def gauge(img, x, y, w, h):
    """A sunken tank: slot-style border round w x h."""
    d = ImageDraw.Draw(img)
    d.rectangle([x, y, x + w + 1, y + h + 1], fill=hex_to_rgba(GUI[3]))
    d.line([x, y, x + w, y], fill=hex_to_rgba(GUI[1]))
    d.line([x, y, x, y + h], fill=hex_to_rgba(GUI[1]))
    d.line([x + 1, y + h + 1, x + w + 1, y + h + 1], fill=hex_to_rgba(GUI[5]))
    d.line([x + w + 1, y + 1, x + w + 1, y + h + 1], fill=hex_to_rgba(GUI[5]))


def basket(img):
    """A faint copper mesh behind the basket slots, so the four read as one basket."""
    d = ImageDraw.Draw(img)
    rim = hex_to_rgba(COPPER[3])
    shade = hex_to_rgba(COPPER[2])
    d.rectangle([27, 14, 66, 53], outline=rim)
    d.line([28, 53, 66, 53], fill=shade)
    d.line([66, 15, 66, 53], fill=shade)


def button(img, x, y, face, light, dark, arrow):
    d = ImageDraw.Draw(img)
    d.rectangle([x, y, x + 17, y + 17], fill=hex_to_rgba(GUI[0]))
    d.rectangle([x + 1, y + 1, x + 16, y + 16], fill=hex_to_rgba(face))
    d.line([x + 1, y + 1, x + 15, y + 1], fill=hex_to_rgba(light))
    d.line([x + 1, y + 1, x + 1, y + 15], fill=hex_to_rgba(light))
    d.line([x + 2, y + 16, x + 16, y + 16], fill=hex_to_rgba(dark))
    d.line([x + 16, y + 2, x + 16, y + 16], fill=hex_to_rgba(dark))
    _gui.sprite(img, RUN_AGAIN, {"1": arrow}, x + 1, y + 1)


def build_image():
    img = _gui.canvas()
    _gui.panel(img, 0, 0, 176, 186)
    gauge(img, 8, 16, 16, 52)
    basket(img)
    for sx, sy in ((29, 16), (47, 16), (29, 34), (47, 34)):
        _gui.slot(img, sx, sy)
    _gui.slot(img, 38, 53)
    _gui.sprite(img, CHARCOAL_HINT, {"1": GUI[2], "2": GUI[2]}, 40, 55)
    grey = {c: GUI[3] for c in "12345"}
    _gui.sprite(img, _kiln.ARROW, grey, 70, 35)
    _gui.sprite(img, _kiln.FLAME, grey, 9, 75)
    gauge(img, 98, 16, 16, 52)
    gauge(img, 120, 16, 10, 52)
    _gui.player_inventory(img, 7, 103)

    fire = {"1": FIRE[1], "2": FIRE[2], "3": FIRE[3], "4": FIRE[4], "5": FIRE[5]}
    _gui.sprite(img, _kiln.FLAME, fire, 176, 0)
    _gui.sprite(img, _kiln.ARROW, {"5": GUI[5]}, 176, 14)
    _kettle.gauge_marks(img, 176, 31, 16, 52)
    _kettle.gauge_marks(img, 192, 31, 10, 52)
    button(img, 202, 31, GUI[3], GUI[5], GUI[2], GUI[1])          # normal
    button(img, 202, 49, GUI[4], GUI[5], GUI[2], GUI[0])          # hovered: lighter
    button(img, 202, 67, GUI[2], GUI[3], GUI[1], GUI[3])          # unavailable: dim
    return img
