"""The agave's 3D rosette (block models agave_stage0-4, built in datagen) samples these:

- agave_leaf: one thick leaf, read lengthwise. The broad faces show columns 6-9 (tip at the top: a dark terminal spine;
  the blade lit down its middle with darker toothed edges; paler toward the base); the thin edges show column 10;
  the ends row 0.
- agave_heart: the upright young leaves in the middle, drawn as a fan of pointed blades on clear background (two
  crossed planes use the bottom-middle of it, as wide and tall as that stage's heart).
- agave_stalk: the flower stalk and its branches, green going woody (they use the top-left of it).
- agave_flower: the yellow blossom clusters at the ends of the branches (top-left too).
"""
from texturegen.palettes import AGAVE, LEMON, OLIVE, WOOD_OAK

# 0-5 agave; s t spine (dark wood); y1-y4 blossom; o1-o4 stalk
LEGEND = {
    **{str(i): AGAVE[i] for i in range(6)},
    "s": WOOD_OAK[1], "t": WOOD_OAK[2],
    **dict(zip("ABCD", LEMON[1:5])),
    **dict(zip("wxyz", OLIVE[1:5])),
}


def rows(fn):
    return "\n".join("".join(fn(x, y) for x in range(16)) for y in range(16))


def leaf(x, y):
    if y == 0:
        return "s" if x in (7, 8) else "t"       # the terminal spine, at the very point
    if y == 1:
        return "2" if x in (6, 9, 10) else "3"
    edge_tooth = y % 3 == 0
    if x in (6, 10):
        return "1" if edge_tooth else "2"         # toothed edges (and the thin edge faces)
    if x == 9:
        return "1" if edge_tooth else "3"
    if y >= 13:
        return "5" if x == 7 else "4"             # the pale base
    return "4" if x == 7 else "3"                 # lit down the middle


# A fan of five pointed blades from the bottom middle (the heart planes take the bottom-middle 5-11 wide, 4-10 tall).
HEART = """
................
................
................
................
.......4........
.......43.......
....4..43..4....
....43.43.43....
.....3.43.3.....
..4..43432.3.4..
..43.3434323.3..
...3343433332...
...34434333322..
....3343332222..
....2333222221..
.....22222211...
"""


def stalk(x, y):
    # the parts sample the top-left: a lit column, then mid tones; a ring at the joints
    if y % 6 == 5:
        return "x"
    if x == 0:
        return "z"
    return "y" if (x + y // 4) % 3 else "x"


def flower(x, y):
    if (x + 2 * y) % 5 == 0:
        return "A"                               # shadow between the florets
    if (x * 3 + y) % 7 == 0:
        return "D"                               # bright pollen
    return "C" if (x + y) % 2 else "B"


TEXTURES = {
    "agave_leaf": rows(leaf),
    "agave_heart": HEART,
    "agave_stalk": rows(stalk),
    "agave_flower": rows(flower),
}

COMPARE = ["block/sweet_berry_bush_stage3", "block/cactus_side"]
