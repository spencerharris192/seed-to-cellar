"""Writes the Vineyard village buildings (GDD section 18.2); the output is committed:

- village/plains/vineyard   (oak, white grapes: plains are temperate)
- village/savanna/vineyard  (acacia, red grapes: savannas are warm)

An 11x11 fenced plot, entered by a gate in the middle of the front (the jigsaw that joins it to the street sits under
the gate and becomes a dirt path). Four rows of trellised vines, two trellises tall, run back from the front either
side of a central path. At the back, a slab-roofed shed on fence posts holds the vintner's Fruit Press at the end of
the path, a tapped Oak Cask, a Wine Rack and a barrel of vineyard odds and ends (loot); beside it, open to the sky so
you can jump in, the Crushing Tub and a composter for the pomace. Everything inside the plot up to the roof that isn't
built is air, so the building clears grass and bumps.

    .venv/Scripts/python make_vineyard.py
"""
from structgen import Plot

SX, SY, SZ = 11, 4, 11
GATE_X = 5
ROWS_X = (2, 4, 6, 8)
ROWS_Z = range(2, 7)
SHED_Z = (8, 9)
SHED_X = range(1, 7)      # the roofed part; x 7-9 behind the vines is an open yard for stomping
# the ripeness of each vine along a row (leafy 3, flowering 4, ripe 5), varied row by row
AGES = [5, 4, 5, 3, 5, 4, 5]


def build(wood: str, grape: str) -> Plot:
    p = Plot(SX, SY, SZ)
    for x in range(SX):
        for z in range(SZ):
            if x == GATE_X and z < SHED_Z[0]:
                p.set(x, 0, z, "minecraft:dirt_path")
            elif x in ROWS_X and z in ROWS_Z:
                p.set(x, 0, z, "minecraft:coarse_dirt")          # the vines' soil
            elif z in SHED_Z and x in SHED_X:
                p.set(x, 0, z, f"minecraft:{wood}_planks")      # the shed floor
            else:
                p.set(x, 0, z, "minecraft:grass_block", snowy=False)
            p.fill(x, 1, z, x, SY - 1, z, "minecraft:air")

    # the fence around the plot, with the gate at the front over the street entrance
    for x in range(SX):
        for z in range(SZ):
            if x in (0, SX - 1) or z in (0, SZ - 1):
                p.fence(x, 1, z, wood)
    p.gate(GATE_X, 1, 0, wood, "north")
    p.entrance(GATE_X, 0, 0, "north_up", "minecraft:dirt_path")

    # four rows of vines, two trellises tall
    for i, x in enumerate(ROWS_X):
        for j, z in enumerate(ROWS_Z):
            age = AGES[(i * 2 + j) % len(AGES)]
            p.set(x, 1, z, f"seedtocellar:{grape}_vine", age=age, axis="z", root=True)
            p.set(x, 2, z, f"seedtocellar:{grape}_vine", age=age, axis="z", root=False)

    # the press shed: fence posts at its corners, a slab roof, a lantern under it
    for x in (SHED_X[0], SHED_X[-1]):
        for z in SHED_Z:
            for y in (1, 2):
                p.fence(x, y, z, wood)
    for x in SHED_X:
        for z in SHED_Z:
            p.set(x, 3, z, f"minecraft:{wood}_slab", type="bottom", waterlogged=False)
    back = SHED_Z[1]
    p.set(2, 1, back, "seedtocellar:oak_cask", facing="north", tap=True)
    p.set(3, 1, back, "seedtocellar:wine_rack", facing="north")
    p.loot(4, 1, back, "minecraft:barrel", "seedtocellar:chests/vineyard", facing="up", open=False)
    p.set(GATE_X, 1, back, "seedtocellar:fruit_press", facing="north", powered=False)
    p.set(3, 2, SHED_Z[0], "minecraft:lantern", hanging=True, waterlogged=False)

    # the open yard: stomp grapes in the tub under the sky, pomace into the composter
    p.set(8, 1, back, "seedtocellar:crushing_tub")
    p.set(9, 1, back, "minecraft:composter", level=0)
    return p


build("oak", "white_grape").write("village/plains/vineyard")
build("acacia", "red_grape").write("village/savanna/vineyard")
