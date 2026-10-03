"""Renders block models (JSON + textures) from a few angles, so shapes can be reviewed without
launching the game. It's a simple software renderer: each texel of each face becomes a small
polygon, shaded like Minecraft shades faces, drawn back to front (so translucent glass blends).

    python -m texturegen.model_preview preserving_jar preserving_jar_closed
    python -m texturegen.model_preview preserving_jar --liquid 4.25,0.25,4.25,11.75,8.75,11.75,c86a20e0
    python -m texturegen.model_preview preserving_jar_closed --px 28     # zoom in
    python -m texturegen.model_preview display/gin --tint 0=e8f0f4,1=f0ecdf,2=4b3a78,3=a8c8ec   # tinted faces

Join models with + to stack them a block apart, bottom first (two-block things): pot_still_lower+pot_still_upper
--liquid adds a flat-colored box (block pixels 0-16, RRGGBBAA), standing in for liquid a block
entity renderer draws in game. --tint colors the faces of each tintindex (index=RRGGBB, comma-separated), as the game's
color handlers or renderers would; untinted it draws them as they are. Writes tools/out/model_<names>.png. Element rotations (one axis, as in game) are
drawn turned about their origin, the way Minecraft bakes them; "rescale" is ignored.
"""
import json
import math
import sys
from pathlib import Path

from PIL import Image, ImageDraw

from .paths import OUT, PROJECT, VANILLA

ASSETS = PROJECT / "src" / "main" / "resources" / "assets"
GENERATED = PROJECT / "src" / "generated" / "resources" / "assets"   # models made by runData
SHADE = {"up": 1.0, "down": 0.5, "north": 0.8, "south": 0.8, "east": 0.6, "west": 0.6}
VIEWS = [("front-left", 225, 30), ("front-right", 135, 30), ("back", 45, 30), ("top", 225, 70)]
TINTS: dict[int, tuple[int, int, int]] = {}   # --tint: tintindex -> RGB
PX = 14          # screen pixels per model pixel
SIZE = 30 * PX   # canvas per view


def load_model(name: str) -> tuple[dict, list]:
    """Textures (merged down the parent chain) and elements (nearest model that has any)."""
    textures, elements = {}, None
    ref = f"seedtocellar:block/{name}"
    while ref:
        ns, path = ref.split(":") if ":" in ref else ("minecraft", ref)
        file = ASSETS / ns / "models" / f"{path}.json"
        if not file.exists():
            file = GENERATED / ns / "models" / f"{path}.json"
        if not file.exists():
            break  # vanilla parents like block/block add nothing we draw
        data = json.loads(file.read_text(encoding="utf-8"))
        textures = {**data.get("textures", {}), **textures}
        if elements is None and "elements" in data:
            elements = data["elements"]
        ref = data.get("parent")
    return textures, elements or []


def texture(textures: dict, key: str, cache: dict) -> Image.Image:
    seen = set()
    while key.startswith("#") and key not in seen:
        seen.add(key)
        key = textures.get(key[1:], key)
    if key not in cache:
        ns, path = key.split(":") if ":" in key else ("minecraft", key)
        file = ASSETS / ns / "textures" / f"{path}.png" if ns != "minecraft" else VANILLA / f"{path}.png"
        cache[key] = Image.open(file).convert("RGBA") if file.exists() else Image.new("RGBA", (16, 16), (255, 0, 255, 255))
    return cache[key]


def face_point(face: str, f, t, s: float, v: float):
    """3D point on a face at (s, v) in 0-1: s runs along the texture's u, v down its v."""
    (x0, y0, z0), (x1, y1, z1) = f, t
    y = y1 - v * (y1 - y0)
    return {
        "north": (x1 - s * (x1 - x0), y, z0),
        "south": (x0 + s * (x1 - x0), y, z1),
        "west": (x0, y, z0 + s * (z1 - z0)),
        "east": (x1, y, z1 - s * (z1 - z0)),
        "up": (x0 + s * (x1 - x0), y1, z0 + v * (z1 - z0)),
        "down": (x0 + s * (x1 - x0), y0, z1 - v * (z1 - z0)),
    }[face]


def default_uv(face: str, f, t):
    (x0, y0, z0), (x1, y1, z1) = f, t
    return {
        "north": (16 - x1, 16 - y1, 16 - x0, 16 - y0), "south": (x0, 16 - y1, x1, 16 - y0),
        "west": (z0, 16 - y1, z1, 16 - y0), "east": (16 - z1, 16 - y1, 16 - z0, 16 - y0),
        "up": (x0, z0, x1, z1), "down": (x0, 16 - z1, x1, 16 - z0),
    }[face]


def rotate_uv(s: float, v: float, rotation: int):
    """Minecraft turns a face's texture clockwise by `rotation` degrees."""
    return {0: (s, v), 90: (v, 1 - s), 180: (1 - s, 1 - v), 270: (1 - v, s)}[rotation % 360]


def turned(point, rotation):
    """A model point turned about rotation["origin"] by its angle around its axis (right-handed, as Minecraft bakes it)."""
    if not rotation or not rotation.get("angle", 0):
        return point
    a = math.radians(rotation["angle"])
    c, s = math.cos(a), math.sin(a)
    ox, oy, oz = rotation.get("origin", [8, 8, 8])
    x, y, z = point[0] - ox, point[1] - oy, point[2] - oz
    axis = rotation.get("axis", "y")
    if axis == "x":
        y, z = y * c - z * s, y * s + z * c
    elif axis == "y":
        z, x = z * c - x * s, z * s + x * c
    else:
        x, y = x * c - y * s, x * s + y * c
    return [x + ox, y + oy, z + oz]


def cells(elements, textures):
    """Every texel of every face: (3D corners, face name, RGBA)."""
    cache = {}
    out = []
    for el in elements:
        rotation = el.get("rotation")
        f, t = el["from"], el["to"]
        for face, spec in el.get("faces", {}).items():
            if "color" in spec:  # a flat-colored stand-in (e.g. --liquid)
                img, uv = None, (0, 0, 1, 1)
            else:
                img = texture(textures, spec["texture"], cache)
                uv = spec.get("uv") or default_uv(face, f, t)
            u0, v0, u1, v1 = uv
            scale = img.width / 16 if img else 1
            n = max(1, round(abs(u1 - u0))) if img else 4
            m = max(1, round(abs(v1 - v0))) if img else 4
            for i in range(n):
                for j in range(m):
                    s0, s1, w0, w1 = i / n, (i + 1) / n, j / m, (j + 1) / m
                    su, sv = rotate_uv((s0 + s1) / 2, (w0 + w1) / 2, spec.get("rotation", 0))
                    if img:
                        px = min(img.width - 1, int((u0 + su * (u1 - u0)) * scale))
                        py = min(img.height - 1, int((v0 + sv * (v1 - v0)) * scale))
                        color = img.getpixel((px, py))
                    else:
                        color = spec["color"]
                    if color[3] == 0:
                        continue
                    tint = TINTS.get(spec.get("tintindex", -1))
                    if tint:
                        color = tuple(color[k] * tint[k] // 255 for k in range(3)) + (color[3],)
                    corners = [turned(face_point(face, f, t, a, b), rotation) for a, b in ((s0, w0), (s1, w0), (s1, w1), (s0, w1))]
                    out.append((corners, face, color))
    return out


def render_view(all_cells, yaw: float, pitch: float) -> Image.Image:
    ya, pa = math.radians(yaw), math.radians(pitch)
    cy, sy, cp, sp = math.cos(ya), math.sin(ya), math.cos(pa), math.sin(pa)

    def project(p):
        x, y, z = p[0] - 8, p[1] - 8 - Y_SHIFT, p[2] - 8
        rx, rz = x * cy - z * sy, x * sy + z * cy          # turn around the vertical axis
        ry, near = y * cp - rz * sp, y * sp + rz * cp      # tilt to look down; near = toward camera
        return SIZE / 2 + rx * PX, SIZE / 2 - ry * PX + 2 * PX, near

    # Grass-ish backdrop. RGB on purpose: Pillow only blends translucent fills onto RGB images.
    img = Image.new("RGB", (SIZE, SIZE), (125, 160, 95))
    draw = ImageDraw.Draw(img, "RGBA")
    polys = []
    for corners, face, (r, g, b, a) in all_cells:
        pts = [project(c) for c in corners]
        near = sum(p[2] for p in pts) / 4
        k = SHADE[face]
        polys.append((near, [(p[0], p[1]) for p in pts], (int(r * k), int(g * k), int(b * k), a)))
    for _, pts, color in sorted(polys, key=lambda p: p[0]):   # far to near
        draw.polygon(pts, fill=color)
    return img


Y_SHIFT = 0.0   # half the extra height of stacked models, so they stay centred


def stacked(name: str) -> tuple[dict, list]:
    """Textures and elements of `a+b+...`, each model a block (16 pixels) above the one before."""
    textures, elements = {}, []
    for i, part in enumerate(name.split("+")):
        t, els = load_model(part)
        for key, value in t.items():
            textures.setdefault(key, value)
        for e in els:
            e = dict(e)
            e["from"] = [e["from"][0], e["from"][1] + 16 * i, e["from"][2]]
            e["to"] = [e["to"][0], e["to"][1] + 16 * i, e["to"][2]]
            # each part keeps its own textures: prefix its #refs with the part's index
            e["faces"] = {f: (dict(v, texture="#" + str(i) + "_" + v["texture"][1:]) if "texture" in v else v)
                          for f, v in e["faces"].items()}
            elements.append(e)
        for key, value in t.items():
            textures[str(i) + "_" + key] = value if not str(value).startswith("#") else "#" + str(i) + "_" + str(value)[1:]
    return textures, elements


def main(argv: list[str]) -> int:
    global PX, SIZE, Y_SHIFT
    if "--px" in argv:  # zoom: screen pixels per model pixel
        i = argv.index("--px")
        PX = int(argv[i + 1])
        SIZE = 30 * PX
        argv = argv[:i] + argv[i + 2:]
    if "--tint" in argv:
        i = argv.index("--tint")
        for part in argv[i + 1].split(","):
            index, rgb = part.split("=")
            TINTS[int(index)] = tuple(int(rgb[k:k + 2], 16) for k in (0, 2, 4))
        argv = argv[:i] + argv[i + 2:]
    liquid = None
    if "--liquid" in argv:
        i = argv.index("--liquid")
        *box, rgba = argv[i + 1].split(",")
        x0, y0, z0, x1, y1, z1 = map(float, box)
        color = tuple(int(rgba[k:k + 2], 16) for k in (0, 2, 4, 6))
        liquid = {"from": [x0, y0, z0], "to": [x1, y1, z1],
                  "faces": {f: {"color": color} for f in ("north", "south", "east", "west", "up")}}
        argv = argv[:i] + argv[i + 2:]
    if not argv:
        print(__doc__)
        return 1
    rows = []
    tallest = max(name.count("+") for name in argv)
    if tallest:
        Y_SHIFT = 8.0 * tallest
        SIZE = (30 + 16 * tallest) * PX
    for name in argv:
        textures, elements = stacked(name) if "+" in name else load_model(name)
        if not elements:
            print(f"No elements found for {name}")
            return 1
        all_cells = cells(elements + ([liquid] if liquid else []), textures)
        rows.append([render_view(all_cells, yaw, pitch) for _, yaw, pitch in VIEWS])
    sheet = Image.new("RGBA", (SIZE * len(VIEWS), SIZE * len(rows)), (60, 60, 66, 255))
    for r, row in enumerate(rows):
        for c, view in enumerate(row):
            sheet.paste(view, (c * SIZE, r * SIZE))
    OUT.mkdir(parents=True, exist_ok=True)
    out = OUT / f"model_{'_'.join(argv).replace('/', '-')}.png"
    sheet.save(out)
    print(f"Wrote {out}")
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
