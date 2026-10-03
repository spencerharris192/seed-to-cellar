"""Helper (not a texture): shades rounded food shapes the way vanilla items are lit.

A shape is an ellipse (optionally tilted) treated as a dome: each pixel's brightness comes from
how much its surface faces the light, which falls from the top-left like every vanilla item.
Pixels on the rim become the darkest tone, so shapes get the soft outline vanilla foods have.
Callers pick the palette letters and add their own details (scores, flecks, char marks).
"""
import math

LIGHT = (-0.55, -0.65, 0.52)   # from the top-left, slightly in front


def _normalized(v):
    length = math.sqrt(sum(c * c for c in v))
    return tuple(c / length for c in v)


def dome(cx: float, cy: float, rx: float, ry: float, angle: float = 0.0, height: float = 1.0) -> dict:
    """{(x, y): brightness 0..1} for pixels inside the ellipse. `height` < 1 flattens the dome
    (a tortilla is nearly flat, a boule is tall); `angle` tilts the long axis (degrees, clockwise)."""
    lx, ly, lz = _normalized(LIGHT)
    a = math.radians(angle)
    cos, sin = math.cos(a), math.sin(a)
    out = {}
    for y in range(16):
        for x in range(16):
            dx, dy = x + 0.5 - cx, y + 0.5 - cy
            u = (dx * cos + dy * sin) / rx
            v = (-dx * sin + dy * cos) / ry
            r2 = u * u + v * v
            if r2 > 1.0:
                continue
            # surface normal of the dome, turned back into screen space
            nz = math.sqrt(max(0.0, 1.0 - r2)) / max(height, 0.05)
            nu, nv = u, v
            nx = nu * cos - nv * sin
            ny = nu * sin + nv * cos
            n = _normalized((nx, ny, nz))
            out[(x, y)] = max(0.0, n[0] * lx + n[1] * ly + n[2] * lz)
    return out


def shade(shape: dict, letters: str) -> list[list[str]]:
    """A 16x16 grid: rim pixels get letters[0]; inside pixels step through letters[1:] by brightness."""
    g = [["."] * 16 for _ in range(16)]
    inner = letters[1:]
    for (x, y), b in shape.items():
        rim = any((x + dx, y + dy) not in shape for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)))
        if rim:
            g[y][x] = letters[0]
        else:
            g[y][x] = inner[min(len(inner) - 1, int(b * len(inner)))]
    return g


def to_str(g: list[list[str]]) -> str:
    return "\n".join("".join(r) for r in g)


def put(g: list[list[str]], points, ch: str, only_on=None) -> None:
    """Paint single pixels (only over the shape unless `only_on` is None and the pixel is blank)."""
    for x, y in points:
        if 0 <= x < 16 and 0 <= y < 16 and g[y][x] != "." and (only_on is None or g[y][x] in only_on):
            g[y][x] = ch
