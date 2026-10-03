"""Kettle dishes served in a bowl: one wide oak bowl, seen from above, filled per dish.

The bowl is wide and shallow like the vanilla bowl the player puts in, so the dish's surface is a
big oval that shows what's in it. Lit from the top-left: a pale far rim over a shadowed inner
wall, the lit front lip, a turned groove under it, and the round body below. The contents are a
shaded surface with small hand-drawn chunks on top (a 2x2 chunk: lit corner top-left).

- beef_and_ale_stew: dark ale gravy with beef, carrot and potato chunks.
- mutton_and_barley_stew: golden broth studded with barley grains, mutton, carrot and a herb.
- chili_con_carne: deep red with dark kidney beans, crumbled beef and a green herb.
- borscht: crimson beet soup with a dollop of sour cream and a sprig of dill.
- tomato_soup: smooth orange-red with a cream swirl and a basil leaf.
- porridge: cream-colored oats heaped up, a drizzle of honey.
- cooked_rice: a white heap of grains.
- cranberry_sauce: glossy dark red with whole berries.
- harvest_feast_serving: gravy with roast chicken, roast potato, carrot and a spoon of cranberry sauce.
- salad: a heap of bright leaves with tomato wedges, cucumber slices and onion rings.
- risotto: a creamy, golden heap of rice with sliced mushrooms and parsley.
- coq_au_vin: chicken and mushrooms in a dark red-brown wine sauce, with pearl onions.
"""
from texturegen.compose import sibling
from texturegen.palettes import (BARLEY, BEET, CLAY, CORN, FIRE, FLOUR, FOLIAGE_FRESH, FRUIT_RED, HOP_CONE, MALT_AMBER,
                                 MALT_BLACK, OAT, ROOT, SORGHUM, WOOD_OAK)

shapes = sibling(__file__, "_shapes")

LEGEND = {
    # bowl wood, one step darker than planks so it matches the vanilla bowl it's served in
    "0": MALT_BLACK[2], **dict(zip("12345", WOOD_OAK[:5])),
    **dict(zip("ABCD", MALT_AMBER[1:5])),        # ale gravy
    **dict(zip("EFG", BARLEY[2:5])),             # barley broth
    **dict(zip("HIJKLN", FRUIT_RED)),            # red: chili, tomato, cranberry
    **dict(zip("OPQR", BEET[2:6])),              # beet
    **dict(zip("yxwW", FLOUR[2:6])),             # cream, rice
    **dict(zip("rstu", OAT[2:6])),               # oats
    **dict(zip("XYZ", CORN[3:6])),               # honey
    "b": MALT_BLACK[3], "e": MALT_BLACK[5],      # beef
    "c": FIRE[2], "j": FIRE[3],                  # carrot
    "p": OAT[3], "q": OAT[5],                    # potato
    "g": BARLEY[6], "h": BARLEY[5],              # barley grains
    "m": CLAY[2], "M": CLAY[4],                  # mutton
    "v": FOLIAGE_FRESH[2], "V": FOLIAGE_FRESH[4],  # herbs
    "a": MALT_AMBER[3], "f": MALT_AMBER[5],      # roast chicken skin
    "k": ROOT[1], "n": ROOT[3],                  # sliced mushroom: gills, cap
    **dict(zip("STU", SORGHUM[1:4])),            # red wine sauce
    **dict(zip("6789", FOLIAGE_FRESH[2:6])),     # salad leaves
    "l": FOLIAGE_FRESH[1], "i": HOP_CONE[4],     # cucumber: peel, flesh
}

# '@' is the opening, filled by each dish.
BOWL = """
................
................
................
................
....00000000....
..005444444300..
.04@@@@@@@@@@30.
04@@@@@@@@@@@@30
03@@@@@@@@@@@@20
034@@@@@@@@@@320
0234455555443210
.01222222221110.
.02333333222210.
...0233222110...
....00000000....
................
"""

# chunks: top-left pixel lit
BEEF, CARROT, POTATO = "eb\nbb", "jc", "qp\npp"
MUTTON, BEAN, CRANBERRY = "Mm\nmm", "HJ", "LK\nKJ"
CHICKEN, DRUMSTICK = "fa\naa", "Wfa\n.aa"
MUSHROOM, CUCUMBER, WEDGE = "nn\nk.", "lil", "LK\n.J"


def fill(colors: str, chunks=(), heap: str = "", top=()) -> str:
    """Fills the opening with a shaded surface (colors: dark -> light, lighter toward the back-left)
    and stamps chunk sprites at (sprite, x, y). `heap` letters pile the contents up above the rim
    (rice, porridge); `top` sprites go anywhere on the dish, heap included."""
    g = [list(r) for r in BOWL.strip("\n").splitlines()]
    for y in range(16):
        for x in range(16):
            if g[y][x] == "@":
                t = 0.9 - (x - 2) * 0.035 - (y - 6) * 0.17
                g[y][x] = colors[max(0, min(len(colors) - 1, int(t * len(colors))))]
    if heap:
        pile = shapes.dome(8.0, 7.6, 5.7, 4.6, height=0.9)
        for (x, y), b in pile.items():
            if g[y][x] == "." or y < 9 or g[y][x] not in "012345":
                g[y][x] = heap[min(len(heap) - 1, int(b * len(heap)))]
        for (x, y) in pile:   # the heap's edge above the bowl gets the darkest tone
            if y < 6 and ((x, y - 1) not in pile or (x - 1, y) not in pile or (x + 1, y) not in pile):
                g[y][x] = heap[0]
    for sprite, sx, sy in chunks:
        for dy, row in enumerate(sprite.splitlines()):
            for dx, ch in enumerate(row):
                x, y = sx + dx, sy + dy
                if ch != "." and g[y][x] not in "012345.":
                    g[y][x] = ch
    for sprite, sx, sy in top:
        for dy, row in enumerate(sprite.splitlines()):
            for dx, ch in enumerate(row):
                if ch != ".":
                    g[sy + dy][sx + dx] = ch
    return shapes.to_str(g[1:] + [["."] * 16])   # drawn a row low; lift it so the dish sits centered


TEXTURES = {
    "beef_and_ale_stew": fill("ABBC", [(BEEF, 4, 6), (BEEF, 9, 7), (CARROT, 7, 6), (CARROT, 2, 8), (CARROT, 11, 6),
                                       (POTATO, 6, 8), (POTATO, 12, 8)]),
    "mutton_and_barley_stew": fill("EFFG", [(MUTTON, 4, 7), (MUTTON, 10, 6), (CARROT, 7, 8), ("g", 3, 6), ("g", 7, 6),
                                            ("h", 9, 7), ("g", 12, 8), ("g", 5, 9), ("g", 10, 9), ("h", 2, 8),
                                            ("g", 13, 7), ("g", 8, 6), ("V", 6, 7), ("v", 12, 7)]),
    "chili_con_carne": fill("IJJK", [(BEAN, 3, 7), (BEAN, 8, 6), (BEAN, 10, 8), (BEAN, 5, 9), ("b", 6, 6), ("b", 12, 7),
                                     ("b", 7, 8), ("b", 4, 6), ("V", 9, 7), ("v", 10, 7), ("V", 11, 6)]),
    "borscht": fill("OPPQ", [("xWW\nyxx", 6, 7), ("V", 10, 6), ("v", 11, 7), ("V", 12, 6), ("v", 3, 7)]),
    "tomato_soup": fill("JKKL", [("WWW", 5, 7), ("W", 4, 8), ("WW", 8, 8), ("W", 8, 6), ("xx", 6, 8), ("Vv", 10, 6),
                                 ("v", 11, 7)]),
    "porridge": fill("stu", heap="rsttu", top=[("Y", 6, 4), ("XY", 7, 5), ("Z", 9, 4), ("Y", 10, 5), ("X", 11, 6),
                                                  ("Y", 5, 6), ("X", 4, 7), ("Y", 8, 6)]),
    "cooked_rice": fill("xwW", heap="yxwwW", top=[("y", 6, 5), ("y", 9, 4), ("y", 11, 6), ("y", 5, 7), ("x", 8, 6),
                                                     ("y", 10, 8), ("y", 3, 8), ("x", 7, 4), ("y", 12, 8)]),
    "cranberry_sauce": fill("HIIJ", [(CRANBERRY, 3, 6), (CRANBERRY, 9, 7), (CRANBERRY, 6, 8), ("LK", 11, 6),
                                     ("N", 7, 6), ("LK", 12, 8)]),
    "harvest_feast_serving": fill("BBCD", [(DRUMSTICK, 3, 6), (CHICKEN, 5, 7), (POTATO, 9, 6), (CARROT, 8, 8),
                                           (CRANBERRY, 11, 8), ("jc", 3, 8), ("V", 12, 6)]),
}

TEXTURES.update({
    "salad": fill("789", heap="677899", top=[(WEDGE, 5, 4), (WEDGE, 10, 6), (CUCUMBER, 7, 6), (CUCUMBER, 3, 7),
                                               ("W.W\n.W.", 9, 3), ("LK", 12, 8), (CUCUMBER, 8, 8)]),
    "risotto": fill("twx", heap="tuxwwW", top=[(MUSHROOM, 5, 4), (MUSHROOM, 9, 5), (MUSHROOM, 3, 7), ("nk", 11, 7),
                                              ("u", 7, 4), ("u", 10, 3), ("u", 8, 7), ("V", 6, 6), ("v", 7, 6), ("V", 12, 5),
                                              ("V", 5, 8)]),
    "coq_au_vin": fill("STTU", [(CHICKEN, 4, 6), (DRUMSTICK, 8, 6), (MUSHROOM, 11, 7), (MUSHROOM, 3, 8), ("W", 7, 8),
                                ("W", 10, 9), ("W", 12, 6), ("V", 6, 8), ("v", 9, 8)]),
})

COMPARE = ["item/mushroom_stew", "item/beetroot_soup", "item/rabbit_stew"]
