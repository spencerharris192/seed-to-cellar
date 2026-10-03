"""Contact sheets for reviewing textures against vanilla.

    python -m texturegen.preview item/barley [--compare item/wheat item/bread]

Writes tools/out/<name>.png containing:
  1. the texture at 8x on light and dark backgrounds, beside vanilla references at 8x
  2. a grayscale row (value/silhouette check: every crop must read in grayscale)
  3. a hotbar mockup at GUI scale 3 and a 1x row (readability at real size)
  4. the palette actually used, with hex codes
Vanilla textures come from tools/vanilla (local reference only; see vanilla.py).
"""
import sys

from PIL import Image, ImageDraw, ImageFont, ImageOps

from .core import iter_art, render_module
from .paths import OUT, VANILLA

BG = (60, 60, 66, 255)
TEXT = (235, 235, 235, 255)
MUTED = (160, 160, 170, 255)
SCALE = 8
GAP = 16


def font(size: int) -> ImageFont.ImageFont:
    return ImageFont.load_default(size=size)


def checker(w: int, h: int, cell: int, a=(205, 205, 205, 255), b=(180, 180, 180, 255)) -> Image.Image:
    img = Image.new("RGBA", (w, h), a)
    draw = ImageDraw.Draw(img)
    for y in range(0, h, cell):
        for x in range(0, w, cell):
            if (x // cell + y // cell) % 2:
                draw.rectangle([x, y, x + cell - 1, y + cell - 1], fill=b)
    return img


def upscale(img: Image.Image, s: int) -> Image.Image:
    return img.resize((img.width * s, img.height * s), Image.NEAREST)


def vanilla(rel: str) -> Image.Image | None:
    path = VANILLA / f"{rel}.png"
    if not path.exists():
        return None
    img = Image.open(path).convert("RGBA")
    return img.crop((0, 0, img.width, img.width))  # first frame of animations


def gray(img: Image.Image) -> Image.Image:
    g = ImageOps.grayscale(img.convert("RGB")).convert("RGBA")
    g.putalpha(img.getchannel("A"))
    return g


def hotbar_mockup(items: list[Image.Image]) -> Image.Image:
    gs = 3
    sky = Image.new("RGBA", (182 * gs + 40, 22 * gs + 40), (121, 166, 255, 255))
    ImageDraw.Draw(sky).rectangle([0, sky.height // 2, sky.width, sky.height], fill=(95, 140, 60, 255))
    widgets = VANILLA / "gui" / "widgets.png"
    ox, oy = 20, 20
    if widgets.exists():
        bar = Image.open(widgets).convert("RGBA").crop((0, 0, 182, 22))
        sky.alpha_composite(upscale(bar, gs), (ox, oy))
    for i, item in enumerate(items[:9]):
        sky.alpha_composite(upscale(item, gs), (ox + (3 + i * 20) * gs, oy + 3 * gs))
    return sky


def palette_strip(img: Image.Image) -> list[tuple[int, int, int, int]]:
    colors = {c for c in img.get_flattened_data() if c[3] > 0}
    return sorted(colors, key=lambda c: 0.299 * c[0] + 0.587 * c[1] + 0.114 * c[2])


def build_sheet(rel: str, ours: Image.Image, compare: list[str]) -> Image.Image:
    refs = [(name, vanilla(name)) for name in compare]
    refs = [(n, i) for n, i in refs if i is not None]
    tile = 16 * SCALE
    cols = 2 + len(refs)
    width = max(GAP + cols * (tile + GAP), 182 * 3 + 80)
    height = 900
    sheet = Image.new("RGBA", (width, height), BG)
    d = ImageDraw.Draw(sheet)
    y = GAP
    d.text((GAP, y), f"seedtocellar:{rel}", font=font(22), fill=TEXT)
    y += 36

    # 1. 8x views
    x = GAP
    for label, bgimg in (("ours (light)", checker(tile, tile, SCALE * 2)), ("ours (dark)", Image.new("RGBA", (tile, tile), (32, 32, 36, 255)))):
        bgimg.alpha_composite(upscale(ours, SCALE))
        sheet.alpha_composite(bgimg, (x, y + 18))
        d.text((x, y), label, font=font(13), fill=MUTED)
        x += tile + GAP
    for name, img in refs:
        panel = checker(tile, tile, SCALE * 2)
        panel.alpha_composite(upscale(img, SCALE))
        sheet.alpha_composite(panel, (x, y + 18))
        d.text((x, y), f"vanilla {name}", font=font(13), fill=MUTED)
        x += tile + GAP
    y += tile + 18 + GAP

    # 2. grayscale (value / silhouette)
    d.text((GAP, y), "grayscale (shape and value must read without color)", font=font(13), fill=MUTED)
    y += 18
    x = GAP
    for img in [ours] + [i for _, i in refs]:
        panel = Image.new("RGBA", (16 * 4, 16 * 4), (200, 200, 200, 255))
        panel.alpha_composite(upscale(gray(img), 4))
        sheet.alpha_composite(panel, (x, y))
        x += 16 * 4 + GAP
    y += 16 * 4 + GAP

    # 3. hotbar at GUI scale 3, then true 1x
    d.text((GAP, y), "hotbar mockup (GUI scale 3): ours first, then vanilla", font=font(13), fill=MUTED)
    y += 18
    bar = hotbar_mockup([ours] + [i for _, i in refs])
    sheet.alpha_composite(bar, (GAP, y))
    y += bar.height + GAP
    d.text((GAP, y), "1x (actual pixels)", font=font(13), fill=MUTED)
    y += 18
    x = GAP
    for img in [ours] + [i for _, i in refs]:
        sheet.alpha_composite(img, (x, y))
        x += 16 + 8
    y += 16 + GAP

    # 4. palette used
    d.text((GAP, y), "palette used (dark to light)", font=font(13), fill=MUTED)
    y += 18
    x = GAP
    for c in palette_strip(ours):
        d.rectangle([x, y, x + 39, y + 39], fill=c)
        d.text((x, y + 42), "#%02x%02x%02x" % c[:3], font=font(10), fill=MUTED)
        x += 52
    y += 70
    return sheet.crop((0, 0, width, y))


def build_strip(title: str, ours: list[tuple[str, Image.Image]], compare: list[str]) -> Image.Image:
    """Growth stages side by side over a soil strip, with vanilla stages below for reference."""
    s = 6
    tile = 16 * s
    refs = [(n, vanilla(n)) for n in compare]
    refs = [(n, i) for n, i in refs if i is not None]
    cols = max(len(ours), len(refs), 1)
    width = GAP + cols * (tile + GAP)
    sheet = Image.new("RGBA", (max(width, 400), 2000), BG)
    d = ImageDraw.Draw(sheet)
    y = GAP
    d.text((GAP, y), title, font=font(22), fill=TEXT)
    y += 36
    soil = vanilla("block/farmland_moist")

    def row(items: list[tuple[str, Image.Image]], label: str, y0: int) -> int:
        d.text((GAP, y0), label, font=font(13), fill=MUTED)
        y0 += 18
        for i, (name, img) in enumerate(items):
            x = GAP + i * (tile + GAP)
            panel = Image.new("RGBA", (tile, tile + 4 * s), (121, 166, 255, 255))
            if soil is not None:
                panel.alpha_composite(upscale(soil.crop((0, 0, 16, 4)), s), (0, tile))
            panel.alpha_composite(upscale(img, s), (0, 0))
            sheet.alpha_composite(panel, (x, y0))
            d.text((x, y0 + tile + 4 * s + 2), name.split("/")[-1], font=font(10), fill=MUTED)
        return y0 + tile + 4 * s + 20

    y = row(ours, "ours", y)
    if refs:
        y = row(refs, "vanilla reference", y)
    d.text((GAP, y), "grayscale + 1x", font=font(13), fill=MUTED)
    y += 18
    for i, (_, img) in enumerate(ours):
        x = GAP + i * (16 * 3 + 8)
        panel = Image.new("RGBA", (48, 48), (200, 200, 200, 255))
        panel.alpha_composite(upscale(gray(img), 3))
        sheet.alpha_composite(panel, (x, y))
        sheet.alpha_composite(img, (x + 16, y + 56))
    y += 48 + 8 + 16 + GAP
    return sheet.crop((0, 0, sheet.width, y))


def build_gallery(items: list[tuple[str, Image.Image]]) -> Image.Image:
    """Many textures at 6x on light and dark tiles, plus a 1x hotbar row."""
    s, per_row = 6, 6
    tile = 16 * s
    rows = (len(items) + per_row - 1) // per_row
    sheet = Image.new("RGBA", (GAP + per_row * (tile * 2 + GAP * 2), rows * (tile + 40) + 200), BG)
    d = ImageDraw.Draw(sheet)
    for i, (name, img) in enumerate(items):
        x = GAP + (i % per_row) * (tile * 2 + GAP * 2)
        y = GAP + (i // per_row) * (tile + 40)
        light = checker(tile, tile, s * 2)
        light.alpha_composite(upscale(img, s))
        dark = Image.new("RGBA", (tile, tile), (32, 32, 36, 255))
        dark.alpha_composite(upscale(img, s))
        sheet.alpha_composite(light, (x, y + 16))
        sheet.alpha_composite(dark, (x + tile, y + 16))
        d.text((x, y), name, font=font(12), fill=MUTED)
    y = GAP + rows * (tile + 40)
    bar = hotbar_mockup([img for _, img in items])
    sheet.alpha_composite(bar, (GAP, y))
    return sheet.crop((0, 0, sheet.width, y + bar.height + GAP))


def main(argv: list[str]) -> int:
    if not argv:
        print(__doc__)
        return 1
    if argv[0] == "--gallery":
        # --gallery <out-name> <texture names...>
        wanted = argv[2:]
        found = {rel: render_module(rel, m) for rel, m in iter_art() if rel in wanted}
        missing = [w for w in wanted if w not in found]
        if missing:
            print("Unknown:", ", ".join(missing))
            return 1
        OUT.mkdir(parents=True, exist_ok=True)
        out = OUT / f"{argv[1]}.png"
        build_gallery([(w, found[w]) for w in wanted]).save(out)
        print(f"Wrote {out}")
        return 0
    if argv[0] == "--strip":
        # --strip <name-prefix> [--compare vanilla names...]: e.g. --strip block/barley_stage --compare block/wheat_stage0 ...
        prefix = argv[1]
        compare = argv[argv.index("--compare") + 1:] if "--compare" in argv else []
        ours = [(rel, render_module(rel, m)) for rel, m in iter_art() if rel.startswith(prefix)]
        if not ours:
            print(f"No art starting with {prefix}")
            return 1
        OUT.mkdir(parents=True, exist_ok=True)
        out = OUT / (prefix.replace("/", "_") + "_strip.png")
        build_strip(f"seedtocellar:{prefix}*", ours, compare).save(out)
        print(f"Wrote {out}")
        return 0
    compare_override: list[str] | None = None
    if "--compare" in argv:
        i = argv.index("--compare")
        compare_override, argv = argv[i + 1:], argv[:i]
    wanted = set(argv)
    OUT.mkdir(parents=True, exist_ok=True)
    found = False
    for rel, module in iter_art():
        if rel not in wanted:
            continue
        found = True
        compare = compare_override if compare_override is not None else getattr(module, "COMPARE", [])
        sheet = build_sheet(rel, render_module(rel, module), compare)
        out = OUT / (rel.replace("/", "_") + ".png")
        sheet.save(out)
        print(f"Wrote {out}")
    if not found:
        print(f"No art named {', '.join(sorted(wanted))}")
        return 1
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
