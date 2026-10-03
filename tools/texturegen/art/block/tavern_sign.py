"""The Tavern Sign (models tavern_sign_<emblem>): a painted board on a wrought-iron bracket. The board is 12 by 12 pixels
and shows the middle 12 by 12 of its picture texture, a pixel to a pixel, on both faces (mirrored on the far one, so the
pictures don't depend on which way they face).

- sign_ale / sign_wine / sign_spirits / sign_cask: the four pictures, each painted on deep bottle green inside a gilt
  frame (lit along the top and left, shaded on the bottom and right): a glass mug of amber ale under a head of foam that
  spills over its rim (an alehouse); a bunch of red grapes under a vine leaf (a wine bar); a copper pot still, its swan
  neck arching over to the lyne arm and a dram dripping into a glass (a distillery); an oak cask, iron-hooped (a cellar).
- sign_edge: the board's edges, gilt like the frame.
- tavern_iron: the bracket, plate and hooks: black wrought iron, lit along the top.
"""
from texturegen.palettes import (BOTTLE_GLASS, COPPER, FLOUR, FOLIAGE_FRESH, GLASS, GOLD, IRON, MALT_AMBER, RED_GRAPE,
                                 WOOD_OAK)

# A-F gold; G-L grape; M-R leaf; S-X foam; a-f oak; g-l iron; m-r painted green; s-x amber; 0-3 glass; 4-9 copper
LEGEND = {
    **dict(zip("ABCDEF", GOLD)),
    **dict(zip("GHIJKL", RED_GRAPE)),
    **dict(zip("MNOPQR", FOLIAGE_FRESH)),
    **dict(zip("STUVWX", FLOUR)),
    **dict(zip("abcdef", WOOD_OAK)),
    **dict(zip("ghijkl", IRON)),
    **dict(zip("mnopqr", BOTTLE_GLASS)),
    **dict(zip("stuvwx", MALT_AMBER)),
    **dict(zip("0123", GLASS)),
    **dict(zip("456789", COPPER)),
}

# The pictures inside the frame, 10 by 10; "n" is the painted ground.
ALE = """
nnnWXnnnnn
nnWXXWXnnn
nWXXXXXWnn
n2WVWWV2nn
n2wxwww211
n2wwwxw2n1
n2vwwww2n1
n2vvwwv211
n2uvvvu2nn
nn11111nnn
"""

WINE = """
nnnnbnPQQn
nnnnbPQRPn
nLJLJLJLJn
nJHJHJHJHn
nnLJLJLJnn
nnJHJHJHnn
nnnLJLJnnn
nnnJHJHnnn
nnnnLJnnnn
nnnnJHnnnn
"""

SPIRITS = """
nnn888nnnn
nn89nn8nnn
nn88nnn7nn
nn88nnnn7n
n7887nnn6n
789987nn6n
789987nnxn
678876n2x2
677776n2x2
n5555nn222
"""

CASK = """
nnhjjjjhnn
nnbdeedbnn
nbcdeedcbn
nhjjjjjjhn
nbcdeedcbn
nbcdfedcbn
nhjjjjjjhn
nbcdeedcbn
nnbdeedbnn
nnhjjjjhnn
"""


def ground(x, y):
    """The painted green, a brush stroke of lighter paint here and there."""
    return "o" if (x * 3 + y * 7) % 11 == 0 else "n"


def picture(art):
    grid = [line for line in art.strip().splitlines()]
    out = []
    for y in range(16):
        row = []
        for x in range(16):
            px, py = x - 2, y - 2                        # the board's 12 by 12 starts 2 pixels in
            if not (0 <= px < 12 and 0 <= py < 12):
                row.append("n")                          # off the board: never shown
            elif py == 0 or px == 0:
                row.append("D" if (px, py) == (0, 11) or (px, py) == (11, 0) else "E")   # the gilt frame, lit
            elif py == 11 or px == 11:
                row.append("C")                          # and shaded
            else:
                c = grid[py - 1][px - 1]
                row.append(ground(x, y) if c == "n" else c)
        out.append("".join(row))
    return "\n".join(out)


def rows(fn):
    return "\n".join("".join(fn(x, y) for x in range(16)) for y in range(16))


def edge(x, y):
    return "E" if y % 4 == 0 else "D" if (x + y) % 5 else "C"


def iron(x, y):
    if y < 3:
        return "j"                                       # lit along the top
    return "h" if (x * 3 + y) % 7 else "i"


TEXTURES = {
    "sign_ale": picture(ALE),
    "sign_wine": picture(WINE),
    "sign_spirits": picture(SPIRITS),
    "sign_cask": picture(CASK),
    "sign_edge": rows(edge),
    "tavern_iron": rows(iron),
}

COMPARE = ["item/oak_hanging_sign", "block/chain"]
