"""The mod list's logo (mods.toml logoFile, at the jar's root): The Brewer's Almanac's tailpiece, a cask on its cradle under
two crossed ears of barley, on a page of the book's paper inside a ruled double border. Drawn at 128 and doubled with
nearest-neighbour scaling, so the pixels stay sharp (mods.toml sets logoBlur off).
"""
from PIL import Image, ImageDraw

from texturegen.art.gui import almanac_filler
from texturegen.core import hex_to_rgba
from texturegen.palettes import CLOTH, FLOUR

DEST = "logo.png"

PAPER = hex_to_rgba(FLOUR[4])
RULE = hex_to_rgba(CLOTH[3])
FAINT = hex_to_rgba(CLOTH[5])


def build_image() -> Image.Image:
    size = almanac_filler.SIZE
    page = Image.new("RGBA", (size, size), PAPER)
    d = ImageDraw.Draw(page)
    d.rectangle([2, 2, size - 3, size - 3], outline=RULE)       # the ruled border, doubled
    d.rectangle([5, 5, size - 6, size - 6], outline=FAINT)
    page.alpha_composite(almanac_filler.build_image(), (0, -6))  # the drawing, lifted to sit in the middle
    return page.resize((size * 2, size * 2), Image.NEAREST)


COMPARE = []
