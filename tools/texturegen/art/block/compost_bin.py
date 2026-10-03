"""Compost Bin: a slatted oak box, plus the pile inside it.

Walls: horizontal planks with dark gaps between them and a nail at each plank end, lit from
above (light top edge, shaded underside). Faces use automatic UVs, so only rows 2-15 and
columns 1-14 show; the nails sit on columns 2 and 13 so every side looks the same.
Inside: the same planks in shadow.
Contents: while filling, a loose mix of green leaves, straw and brown stalks; when ready,
dark crumbly compost (the same rich soil as Fertile Farmland).
"""
from texturegen.palettes import BARLEY, FOLIAGE_CROP, IRON, ROOT, SOIL_RICH, WOOD_OAK

# 0-5 oak, i-n iron, A-F rich soil, p-s leaves, w-z straw, t-v stalks
LEGEND = {
    **{str(i): WOOD_OAK[i] for i in range(6)},
    **dict(zip("ijklmn", IRON)),
    **dict(zip("ABCDEF", SOIL_RICH)),
    **{ch: FOLIAGE_CROP[i + 1] for i, ch in enumerate("pqrs")},
    **{ch: BARLEY[i + 2] for i, ch in enumerate("wxyz")},
    **{ch: ROOT[i + 1] for i, ch in enumerate("tuv")},
}

# One plank course per row: top edge, body, underside, then the gap.
SLATS = """
3333333333333333
2222222222222222
4444444444444444
3334333333433333
2232222222222322
0000000000000000
4444444444444444
3333343333333433
2222222222222222
0000000000000000
4444444444444444
3343333333334333
2222223222222222
0000000000000000
4444444444444444
2222222222222222
"""

NAIL_ROWS = (3, 7, 11, 14)   # one nail per plank, at each end
NAIL_COLUMNS = (2, 13)


def planks(shade: int, nails: bool) -> str:
    rows = [list(r) for r in SLATS.strip("\n").splitlines()]
    for row in rows:
        for x, ch in enumerate(row):
            row[x] = str(max(0, int(ch) - shade))
    if nails:
        for y in NAIL_ROWS:
            for x in NAIL_COLUMNS:
                rows[y][x] = "j"
    return "\n".join("".join(r) for r in rows)


FILLING = """
uttuvqrtuuwxtutu
tuqrrsqutuxytuqr
uuuqrqtuuwytvuqs
tvtuqtuutxyutuqq
uuwtuttvtuuqrtut
tuxyutuuuqrsqtwu
utuxyuvtuuqrtwxt
uqrtuwutvuutuxyu
qrsqtuxttuwxuuut
uqrtutuyuutxytuu
tutuuvtuytuqrsqu
uwxtutuutuuuqrtu
utxyuuqrtuvtutwx
tuuytqrsqutuuxyt
uvtutuqrtuuxytuu
tuuutuuttuuyutvu
"""

READY = """
DCBCDEDCBCDCCBDC
CBABCDCBACBBCACB
BCDCBCBCDDCBBCDC
CDEDCBCDEDCBCDEC
BCDCBABCDCBACCDB
ABBCBCDCBCBCDCBA
CBCDECBBACBCDEDC
DCDEDCBCBCDCBCDC
CBCDCBADCDECBABC
BABCBCBCBCDCBCDB
CBCDEDCBABCBCDEC
DCBCDCBCBCDCBCDC
CBABCBCDEDCBABCB
BCBCDCBCDCBCBCDC
CDCBCBABCBCDEDCB
DEDCBCBCBABCDCBC
"""

TEXTURES = {
    "compost_bin_side": planks(0, True),
    "compost_bin_inner": planks(2, False),
    "compost_fill": FILLING,
    "compost_ready": READY,
}

COMPARE = ["block/composter_side", "block/composter_compost", "block/composter_ready"]
