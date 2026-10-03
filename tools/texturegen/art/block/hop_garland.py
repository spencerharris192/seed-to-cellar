"""The Hop Garland's parts (model hop_garland; its cones are bundle_hop). Each face takes the whole texture squeezed onto a
small part, so the leaf is drawn as its own outline and cut out:

- garland_bine: a fresh, living hop bine: green, twisting, with the fine pale hairs it climbs by.
- garland_leaf: a fresh hop leaf, three-lobed like a vine's: a deep green blade, toothed edges, a pale vein to each lobe
  from the stalk at its top (it hangs from the bine).
"""
from texturegen.palettes import FOLIAGE_CROP, FOLIAGE_FRESH

# M-R crop green (bine); a-f fresh green (leaf)
LEGEND = {
    **dict(zip("MNOPQR", FOLIAGE_CROP)),
    **dict(zip("abcdef", FOLIAGE_FRESH)),
}

LEAF = """
.......bb.......
.......dd.......
..b...cede...b..
.bcc..cede..ccb.
.bdec.cdec.cedb.
bcddecdeedceddcb
.cdddeedddeddcb.
..cddeddeddedc..
.bcddddeddddcb..
bcdddddeddddcb..
.bcddddedddddcb.
..bcdddedddcb...
...bcddedddcb...
....bccdccb.....
......bcb.......
.......b........
"""


def rows(fn):
    return "\n".join("".join(fn(x, y) for x in range(16)) for y in range(16))


def bine(x, y):
    twist = (x + y * 2) % 8
    if twist == 0:
        return "N"                                       # the groove of its twist
    if (x * 5 + y * 3) % 13 == 0:
        return "R"                                       # its climbing hairs
    return "Q" if twist in (1, 2) else "P" if twist < 6 else "O"


TEXTURES = {
    "garland_bine": rows(bine),
    "garland_leaf": LEAF.strip(),
}

COMPARE = ["block/vine", "block/oak_leaves"]
