"""Roasted corn: the corn ear, toasted a shade darker with charred kernels, its husk dried to
brown and peeled right back. Same drawing as the raw ear (so it reads as corn), different colors.
"""
from texturegen.compose import sibling
from texturegen.palettes import CORN, ROOT

corn = sibling(__file__, "corn")

# digits: kernels one step toastier than raw corn; q-y: husk, now dry brown; x char
LEGEND = {
    **{str(i): CORN[max(0, i - 1)] for i in range(6)},
    **dict(zip("qwerty", [ROOT[0], ROOT[1], ROOT[2], ROOT[2], ROOT[3], ROOT[4]])),
    "x": ROOT[0],
}

CHAR = [(8, 4), (7, 6), (6, 5), (5, 8), (4, 7), (9, 2)]


def _build() -> str:
    rows = [list(r) for r in corn.GRID.strip("\n").splitlines()]
    for x, y in CHAR:
        if rows[y][x] in "234":
            rows[y][x] = "x"
    return "\n".join("".join(r) for r in rows)


GRID = _build()

COMPARE = ["item/baked_potato", "item/golden_carrot", "item/carrot"]
