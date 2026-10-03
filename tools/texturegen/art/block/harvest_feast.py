"""Harvest Feast (placed):

- feast_platter: a wooden serving board: straight oak grain lines, a darker worn edge ring.
- harvest_feast: one small sheet the feast model maps its food parts onto, one patch each:
    (0,0)-(8,8)   roast chicken skin: glossy golden-brown with darker crisp patches
    (8,0)-(12,4)  roast potato: golden with brown roasted spots
    (12,0)-(16,4) carrot: orange with a light streak
    (8,4)-(12,8)  bread crust
    (12,4)-(16,8) cranberry sauce: dark glossy red
    (0,8)-(4,12)  bone (the leftovers)
"""
from texturegen.palettes import BARLEY, FIRE, FLOUR, FRUIT_RED, MALT_AMBER, OAT, WOOD_OAK

LEGEND = {
    **{str(i): WOOD_OAK[i] for i in range(6)},
    **dict(zip("abcde", [MALT_AMBER[1], MALT_AMBER[2], MALT_AMBER[3], MALT_AMBER[4], BARLEY[5]])),   # chicken skin
    **dict(zip("pqr", [MALT_AMBER[3], BARLEY[4], BARLEY[5]])),                                     # potato
    **dict(zip("stu", [FIRE[1], FIRE[2], FIRE[3]])),                                               # carrot
    **dict(zip("vw", [MALT_AMBER[2], MALT_AMBER[3]])),                                             # bread crust
    **dict(zip("xyz", [FRUIT_RED[0], FRUIT_RED[1], FRUIT_RED[3]])),                                # cranberry
    **dict(zip("fg", [FLOUR[2], OAT[5]])),                                                         # bone
}

PLATTER = """
1111111111111111
1233333333333321
1344444444444431
1333333333333331
1344444444444431
1333333333333331
1344444444444431
1333333333333331
1344444444444431
1333333333333331
1344444444444431
1333333333333331
1344444444444431
1333333333333331
1233333333333321
1111111111111111
"""

FEAST = """
bccdcbcbqpqrtsut
cddecdbcpqrqsuts
cdecdcbcqrpqttsu
bcddcbcdpqqrstut
cdcbcdecvwvwyzyx
cbcdcdcbwvwvyyzy
dcbcddcdvwwvxyzy
bcdcbcbcwvvwyxyy
gfgf............
fggf............
gfgf............
fgfg............
................
................
................
................
"""

TEXTURES = {"feast_platter": PLATTER, "harvest_feast": FEAST}

COMPARE = ["block/oak_planks", "block/cake_top", "item/cooked_chicken"]
