"""Sickles, six tiers: a curved blade on an oak handle, drawn once and recolored per tier.

Laid out like vanilla tools (handle from the bottom-left, head at the top-right). The blade
sweeps up from the handle, over the top, and ends in a sharp point on the left, so the handle
sits inside the curve: the hook shape reads as "sickle" even at 1x. The inner (cutting) edge is
the brightest line; the outer spine is the darkest. Redrawn slimmer after playtest 3 (the first
version read as a thick grey hook in the hand).
"""
from texturegen.compose import recolor
from texturegen.palettes import DIAMOND, GOLD, IRON, NETHERITE, STONE, WOOD_OAK

# g-j handle; each tier's blade ramp (outline, then body to bright edge) gets its own five letters.
# Blades skip the ramp's second-darkest step so metal reads bright beside vanilla tools.
BLADE = [0, 2, 3, 4, 5]
LEGEND = {
    **{ch: WOOD_OAK[i] for i, ch in enumerate("ghij")},
    **{ch: WOOD_OAK[i] for ch, i in zip("ABCDE", [0, 3, 4, 5, 5])},
    **{ch: STONE[i] for ch, i in zip("FGHIJ", [0, 1, 2, 3, 4])},   # duller than iron
    **{ch: IRON[i] for ch, i in zip("KLMNO", BLADE)},
    **{ch: GOLD[i] for ch, i in zip("PQRST", BLADE)},
    **{ch: DIAMOND[i] for ch, i in zip("UVWXY", BLADE)},
    **{ch: NETHERITE[i] for ch, i in zip("Zklmn", BLADE)},
}

# 1 = spine (darkest) ... 5 = cutting edge (brightest); g-j = handle. A slim crescent: three pixels
# thick over the top and down the right, thinning to a sharp point on the left, with the bright
# edge running unbroken along the inside; a dark collar where it meets the handle.
SICKLE = """
................
.....111111.....
....14444331....
...1455554431...
..145....55431..
.145.......5431.
.15.........541.
.1..........541.
............541.
...........5431.
..........1231..
.........jh1....
........jhg.....
.......jhg......
......jhg.......
.....ghg........
"""

TIERS = {
    "wooden_sickle": "ABCDE",
    "stone_sickle": "FGHIJ",
    "iron_sickle": "KLMNO",
    "golden_sickle": "PQRST",
    "diamond_sickle": "UVWXY",
    "netherite_sickle": "Zklmn",
}


def chipped(sprite: str) -> str:
    """Stone: knapped flecks along the blade, so it doesn't read as a dull iron sickle."""
    rows = [list(r) for r in sprite.strip("\n").splitlines()]
    for x, y in ((6, 2), (9, 2), (13, 5), (13, 8), (12, 3)):
        rows[y][x] = "2"
    return "\n".join("".join(r) for r in rows)


TEXTURES = {name: recolor(chipped(SICKLE) if name == "stone_sickle" else SICKLE, dict(zip("12345", letters)))
            for name, letters in TIERS.items()}

COMPARE = ["item/iron_hoe", "item/golden_hoe", "item/diamond_hoe"]
