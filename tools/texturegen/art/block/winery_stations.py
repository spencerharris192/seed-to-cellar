"""Crushing Tub and Fruit Press textures (models: ModBlockStateProvider.crushingTub / fruitPress).

Tub: oak staves bound with two iron hoops; inside, the wood is stained dark with old juice; the rim
shows the staves' end grain; planks underneath. The tub is 12 pixels tall, so its sides show rows 4-15
of the side texture (faces take their UVs from their position).

Press: heavy oak beams with a straight grain; a cage of vertical slats with gaps (the model is cutout)
between an iron hoop at the top and bottom; a juice-stained tray; and the iron screw, whose thread
shows as light and dark steps running up the one-pixel shaft.
"""
from texturegen.palettes import BEET, IRON, ROOT, WOOD_OAK

# 0-5 oak; A-D stained oak (inner); i-l iron; r s root-dark grain
LEGEND = {
    **{str(i): WOOD_OAK[i] for i in range(6)},
    "A": ROOT[0], "B": ROOT[1], "C": BEET[1], "D": ROOT[2],
    **dict(zip("ijkl", IRON[1:5])),
}


def rows(*lines: str) -> str:
    return "\n".join(lines)


def tub_side() -> str:
    out = []
    for y in range(16):
        if y in (7, 13):                          # iron hoops (rows 7 and 13 = 9 and 3 pixels up the tub)
            out.append("l" + "k" * 14 + "j")
        elif y in (8, 14):
            out.append("j" + "i" * 14 + "i")
        else:
            row = ""
            for x in range(16):
                stave = x // 4                     # four staves, a dark seam between each
                if x % 4 == 0:
                    row += "1"
                else:
                    shade = [4, 3, 3, 2][stave] if y > 1 else 3
                    row += str(shade + (1 if x % 4 == 1 and shade < 5 else 0))
            out.append(row)
    return "\n".join(out)


def tub_inner() -> str:
    out = []
    for y in range(16):
        row = ""
        for x in range(16):
            if x % 4 == 0:
                row += "A"                         # seams
            elif (x * 3 + y * 5) % 11 == 0:
                row += "C"                         # old grape stains
            else:
                row += "B" if (x + y) % 5 else "D"
        out.append(row)
    return "\n".join(out)


def tub_rim() -> str:
    out = []
    for y in range(16):
        row = ""
        for x in range(16):
            edge = x in (0, 15) or y in (0, 15)
            seam = (x + y) % 4 == 0
            row += "1" if edge else ("2" if seam else "5" if (x * y) % 7 == 1 else "4")
        out.append(row)
    return "\n".join(out)


def planks(light: str, mid: str, seam: str) -> str:
    out = []
    for y in range(16):
        if y % 4 == 3:
            out.append(seam * 16)
        else:
            out.append("".join(light if (x + y * 3) % 7 == 0 else mid for x in range(16)))
    return "\n".join(out)


def beam() -> str:
    out = []
    for y in range(16):
        row = ""
        for x in range(16):
            grain = x % 5 == 2 or (x % 5 == 4 and y % 6 < 3)
            row += "1" if x in (0, 15) else ("2" if grain else "3" if (x + y) % 9 else "4")
        out.append(row)
    return "\n".join(out)


def cage() -> str:
    out = []
    for y in range(16):
        if y in (6, 12):                          # hoops at the top and bottom of the cage (y 10 and 4)
            out.append("j" + "k" * 14 + "j")
        else:
            out.append("".join("." if x % 3 == 2 else ("4" if x % 3 == 0 else "2") for x in range(16)))
    return "\n".join(out)


def tray() -> str:
    out = []
    for y in range(16):
        row = ""
        for x in range(16):
            stain = (x - 8) ** 2 + (y - 7) ** 2 < 22           # a soft patch where juice has soaked in
            seam = y % 4 == 3
            row += ("B" if seam else "D" if (x * 3 + y) % 7 else "B") if stain else ("2" if seam else "3")
        out.append(row)
    return "\n".join(out)


def screw() -> str:
    return "\n".join(("l" if (y // 2) % 2 else "j") * 16 for y in range(16))


TEXTURES = {
    "crushing_tub_side": tub_side(),
    "crushing_tub_inner": tub_inner(),
    "crushing_tub_rim": tub_rim(),
    "crushing_tub_bottom": planks("4", "3", "1"),
    "fruit_press_wood": beam(),
    "fruit_press_cage": cage(),
    "fruit_press_tray": tray(),
    "fruit_press_iron": screw(),
}

COMPARE = ["block/barrel_side", "block/composter_side", "block/oak_planks"]
