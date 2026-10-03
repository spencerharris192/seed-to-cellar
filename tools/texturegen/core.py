"""Turns character-grid art into images, and finds art modules."""
import importlib.util
from pathlib import Path
from types import ModuleType, SimpleNamespace
from typing import Iterator

from PIL import Image

from . import palettes
from .paths import ART


class ArtError(Exception):
    pass


def hex_to_rgba(value: str) -> tuple[int, int, int, int]:
    h = value.lstrip("#")
    if len(h) == 6:
        return int(h[0:2], 16), int(h[2:4], 16), int(h[4:6], 16), 255
    if len(h) == 8:
        return int(h[0:2], 16), int(h[2:4], 16), int(h[4:6], 16), int(h[6:8], 16)
    raise ArtError(f"bad color {value!r}")


def render(grid: str, legend: dict[str, str | None], name: str = "?") -> Image.Image:
    """Render a grid. '.' or a None legend entry means transparent."""
    rows = [row for row in grid.strip("\n").splitlines()]
    if not rows:
        raise ArtError(f"{name}: empty grid")
    width = len(rows[0])
    if any(len(r) != width for r in rows):
        raise ArtError(f"{name}: rows have different lengths")
    effect_icon = name.startswith("mob_effect/") and (width, len(rows)) == (18, 18)  # vanilla's effect icon size
    if (width % 16 or len(rows) % 16) and not effect_icon:
        raise ArtError(f"{name}: size {width}x{len(rows)} is not a multiple of 16 (effect icons: 18x18)")

    allowed = palettes.all_colors()
    for char, color in legend.items():
        if color is not None and color.lower() not in allowed:
            raise ArtError(f"{name}: legend '{char}' uses {color}, which is not in palettes.py")

    img = Image.new("RGBA", (width, len(rows)), (0, 0, 0, 0))
    for y, row in enumerate(rows):
        for x, char in enumerate(row):
            if char == ".":
                continue
            if char not in legend:
                raise ArtError(f"{name}: character '{char}' at ({x},{y}) has no legend entry")
            color = legend[char]
            if color is not None:
                img.putpixel((x, y), hex_to_rgba(color))
    return img


def load_module(path: Path) -> ModuleType:
    spec = importlib.util.spec_from_file_location(f"texturegen_art_{path.stem}", path)
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    return module


def iter_art() -> Iterator[tuple[str, ModuleType]]:
    """Yield (relative texture path like 'item/barley', module) for every art file.

    A module either defines GRID (one texture named after the file) or TEXTURES,
    a dict of {texture name: grid} sharing one LEGEND (e.g. all growth stages).
    Files starting with '_' are shared helpers, not textures.
    """
    for path in sorted(ART.rglob("*.py")):
        if path.name.startswith("_"):
            continue
        module = load_module(path)
        folder = path.parent.relative_to(ART).as_posix()
        if hasattr(module, "TEXTURES"):
            for name, grid in module.TEXTURES.items():
                yield f"{folder}/{name}", SimpleNamespace(
                    GRID=grid, LEGEND=module.LEGEND, COMPARE=getattr(module, "COMPARE", []))
        else:
            yield path.relative_to(ART).with_suffix("").as_posix(), module


def render_module(rel: str, module) -> Image.Image:
    """GRID -> one image; FRAMES -> an animation strip (frames stacked vertically);
    build_image() -> an image drawn in code (GUI screens), still limited to palette colors."""
    if hasattr(module, "FRAMES"):
        frames = [render(g, module.LEGEND, f"{rel}[{i}]") for i, g in enumerate(module.FRAMES)]
        strip = Image.new("RGBA", (frames[0].width, frames[0].height * len(frames)), (0, 0, 0, 0))
        for i, frame in enumerate(frames):
            strip.paste(frame, (0, i * frame.height))
        return strip
    if hasattr(module, "build_image"):
        img = module.build_image()
        allowed = palettes.all_colors()
        for c in {p for p in img.get_flattened_data() if p[3] > 0}:
            if "#%02x%02x%02x" % c[:3] not in allowed:
                raise ArtError(f"{rel}: color #%02x%02x%02x is not in palettes.py" % c[:3])
        return img
    return render(module.GRID, module.LEGEND, rel)


def mcmeta(module) -> str | None:
    """Animation metadata for FRAMES textures."""
    if hasattr(module, "FRAMES"):
        return '{\n  "animation": {\n    "frametime": %d\n  }\n}\n' % getattr(module, "FRAMETIME", 4)
    return None
