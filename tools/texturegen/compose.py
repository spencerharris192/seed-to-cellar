"""Helpers for building a grid from smaller hand-drawn sprites.

Still hand-placed pixel art: sprites are drawn by hand, and we only choose where
they go. Useful for crops (reuse one drawn ear or leaf across stalks) and variants.
"""


def sibling(this_file: str, name: str):
    """Loads another art file in the same folder (e.g. a wild plant reusing its crop's sprites)."""
    import importlib.util
    from pathlib import Path
    spec = importlib.util.spec_from_file_location(f"_art_{name}", Path(this_file).with_name(f"{name}.py"))
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    return module


def blank(width: int = 16, height: int = 16) -> list[list[str]]:
    return [["."] * width for _ in range(height)]


def sprite_rows(sprite: str) -> list[str]:
    return [row for row in sprite.strip("\n").splitlines()]


def stamp(canvas: list[list[str]], sprite: str, x: int, y: int) -> None:
    """Paint sprite with its top-left corner at (x, y). '.' in the sprite is transparent."""
    for dy, row in enumerate(sprite_rows(sprite)):
        for dx, ch in enumerate(row):
            cx, cy = x + dx, y + dy
            if ch != "." and 0 <= cy < len(canvas) and 0 <= cx < len(canvas[0]):
                canvas[cy][cx] = ch


def stamp_bottom(canvas: list[list[str]], sprite: str, x: int, bottom: int = 15) -> None:
    """Paint sprite so its last row sits on row `bottom`."""
    stamp(canvas, sprite, x, bottom - len(sprite_rows(sprite)) + 1)


def to_grid(canvas: list[list[str]]) -> str:
    return "\n".join("".join(row) for row in canvas)


def mirror(sprite: str) -> str:
    """The sprite flipped left-to-right (a plant leaning the other way)."""
    return "\n".join(line[::-1] for line in sprite_rows(sprite))


def recolor(sprite: str, colors: dict[str, str]) -> str:
    """Swaps sprite characters: the same drawing in other colors (green -> ripe, and so on)."""
    return "".join(colors.get(ch, ch) for ch in sprite)


def plants(sprite: str, centers, bottom: int = 15, flip_alternate: bool = True) -> list[list[str]]:
    """A canvas with the sprite standing on row `bottom`, centered on each x in `centers`,
    every other one mirrored so a row of plants doesn't look stamped."""
    canvas = blank()
    width = len(sprite_rows(sprite)[0])
    for i, cx in enumerate(centers):
        stamp_bottom(canvas, mirror(sprite) if flip_alternate and i % 2 else sprite, cx - width // 2, bottom)
    return canvas


# Copper weathering (the Brew Kettle and Pot Still): the stages after fresh copper, and how much of the copper the green
# patina has taken at each (patina where the blotch field is above the threshold; oxidized copper is green all over).
WEATHERING = {"exposed": 0.5, "weathered": -0.15, "oxidized": -2.0}


def patina_blotch(x: int, y: int) -> float:
    """A smooth field in [-1, 1] that makes rounded blotches over a 16x16 sheet, so verdigris spreads in patches."""
    import math
    return (math.sin(x * 0.85 + 1.3) + math.sin(y * 0.9 + x * 0.35 + 0.4) + math.sin((x - y) * 0.6 + 2.1)) / 3


def weathered(grid: str, stage: str, copper: str, exposed: str, patina: str) -> str:
    """The same drawing with its copper aged: each copper letter (`copper`, dark to light) becomes the same shade of
    dulled `exposed` copper, or of green `patina` where the blotches have reached; everything else is left alone."""
    threshold = WEATHERING[stage]
    out = []
    for y, line in enumerate(grid.strip("\n").splitlines()):
        row = []
        for x, ch in enumerate(line):
            shade = copper.find(ch)
            if shade < 0:
                row.append(ch)
            else:
                row.append((patina if patina_blotch(x, y) > threshold else exposed)[shade])
        out.append("".join(row))
    return "\n".join(out)
