"""Drawing helpers for container screens in vanilla's style (panel, bevels, slots).

Screens are drawn in code rather than as character grids: they are large, and made of
simple shapes. Only palette colors are used (core checks), so they match the game.
"""
from PIL import Image, ImageDraw

from texturegen.core import hex_to_rgba
from texturegen.palettes import GUI

OUTLINE, SLOT_DARK, SHADOW, SLOT_FILL, PANEL, LIGHT = (hex_to_rgba(c) for c in GUI)


def canvas() -> Image.Image:
    return Image.new("RGBA", (256, 256), (0, 0, 0, 0))


def panel(img: Image.Image, x: int, y: int, w: int, h: int) -> None:
    """Vanilla window: black rounded outline, white top-left bevel, grey bottom-right bevel."""
    d = ImageDraw.Draw(img)
    d.rectangle([x + 2, y + 2, x + w - 3, y + h - 3], fill=PANEL)
    d.line([x + 2, y, x + w - 4, y], fill=OUTLINE)
    d.line([x, y + 2, x, y + h - 4], fill=OUTLINE)
    d.line([x + 3, y + h - 1, x + w - 3, y + h - 1], fill=OUTLINE)
    d.line([x + w - 1, y + 3, x + w - 1, y + h - 3], fill=OUTLINE)
    for px, py in ((x + 1, y + 1), (x + w - 3, y + 1), (x + w - 2, y + 2), (x + 1, y + h - 3), (x + 2, y + h - 2), (x + w - 2, y + h - 2)):
        d.point((px, py), fill=OUTLINE)
    d.rectangle([x + 2, y + 1, x + w - 4, y + 2], fill=LIGHT)
    d.rectangle([x + 1, y + 2, x + 2, y + h - 4], fill=LIGHT)
    d.point((x + 3, y + 3), fill=LIGHT)
    d.rectangle([x + 3, y + h - 3, x + w - 3, y + h - 2], fill=SHADOW)
    d.rectangle([x + w - 3, y + 3, x + w - 2, y + h - 3], fill=SHADOW)
    d.point((x + w - 4, y + h - 4), fill=SHADOW)


def slot(img: Image.Image, x: int, y: int, size: int = 18) -> None:
    """An item slot: dark top-left edge, white bottom-right edge, grey fill."""
    d = ImageDraw.Draw(img)
    d.rectangle([x, y, x + size - 1, y + size - 1], fill=SLOT_FILL)
    d.line([x, y, x + size - 2, y], fill=SLOT_DARK)
    d.line([x, y, x, y + size - 2], fill=SLOT_DARK)
    d.line([x + 1, y + size - 1, x + size - 1, y + size - 1], fill=LIGHT)
    d.line([x + size - 1, y + 1, x + size - 1, y + size - 1], fill=LIGHT)


def player_inventory(img: Image.Image, x: int = 7, y: int = 83) -> None:
    for row in range(3):
        for col in range(9):
            slot(img, x + col * 18, y + row * 18)
    for col in range(9):
        slot(img, x + col * 18, y + 58)


def sprite(img: Image.Image, grid: str, legend: dict, x: int, y: int) -> None:
    """Paint a small hand-drawn grid (flame, arrow) at x, y."""
    for dy, row in enumerate(grid.strip("\n").splitlines()):
        for dx, ch in enumerate(row):
            if ch != "." and legend.get(ch) is not None:
                img.putpixel((x + dx, y + dy), hex_to_rgba(legend[ch]))
