"""Textures for the 3D drinks the Bottle Shelf and Wine Display show (models block/drink_wine_bottle, drink_mug,
drink_flask; each face takes the whole texture, stretched to fit):

- display_glass: dark wine-bottle glass, a pale glint running down the left of every face and a dark edge on the right.
- display_label: the bottle's cream paper label, its edges a shade darker.
- display_clear: clear glass for the mug and flask: a pale frame and a little glint, the middle see-through so the drink
  shows (the models render cut out).
- display_foam: a beer's foam head, cream with a few bubbles.
- display_cork: a cork stopper.
- display_paper and display_accent: a spirit bottle's label and its print, wax, foil or cap (models block/display/<spirit>):
  near-white greys the game tints with each spirit's BottleLook colors. The paper has a faint edge and a few flecks; the
  accent is almost flat, lit a touch along its top, so it keeps its color however small it's drawn.
The foil and the label band reuse wine_rack_foil (grey, tinted with the drink); the drink in a mug or flask uses
liquid_still's own grey tinted the same way.
"""
from texturegen.palettes import BOTTLE_GLASS, FLOUR, GLASS, LIQUID, WOOD_OAK

# a-f bottle glass; g-j clear glass; k-m paper and foam; 1-4 cork; n-s tinted greys
LEGEND = {
    **dict(zip("abcdef", BOTTLE_GLASS)),
    **dict(zip("ghij", GLASS[0:4])),
    **dict(zip("klm", FLOUR[3:6])),
    **dict(zip("1234", WOOD_OAK[1:5])),
    **dict(zip("nopqrs", LIQUID)),
}


def rows(fn):
    return "\n".join("".join(fn(x, y) for x in range(16)) for y in range(16))


def glass(x, y):
    if x == 15:
        return "a"
    if x in (13, 14):
        return "b"
    if x in (1, 2):
        return "e" if (y // 3) % 2 == 0 or x == 1 else "d"   # the glint, broken here and there
    return "c" if (x * 7 + y * 3) % 17 else "d"


def label(x, y):
    if y in (0, 15) or x in (0, 15):
        return "k"
    return "l" if (x * 5 + y * 3) % 13 == 0 else "m"


def clear(x, y):
    if x in (0, 15) or y in (0, 15):
        return "h"                                       # the glass's edge
    if (x, y) in ((2, 2), (3, 3), (2, 3), (3, 4), (4, 5)):
        return "j"                                       # a glint
    return "."                                           # see-through


def foam(x, y):
    return "l" if (x * 3 + y * 5) % 7 == 0 else "m"


def cork(x, y):
    return "2" if (x + y) % 5 == 0 else ("4" if y < 4 else "3")


def paper(x, y):
    if y in (0, 15) or x in (0, 15):
        return "q"                                       # the label's edge
    return "r" if (x * 5 + y * 3) % 11 == 0 else "s"     # flecks in the paper


def accent(x, y):
    if y < 3:
        return "s"                                       # lit along its top
    return "q" if x == 15 or y == 15 else ("r" if (x * 3 + y * 7) % 13 == 0 else "s")


TEXTURES = {
    "display_glass": rows(glass),
    "display_label": rows(label),
    "display_clear": rows(clear),
    "display_foam": rows(foam),
    "display_cork": rows(cork),
    "display_paper": rows(paper),
    "display_accent": rows(accent),
}

COMPARE = ["block/glass", "item/potion"]
