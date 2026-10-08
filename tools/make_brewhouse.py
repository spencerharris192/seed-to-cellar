"""Writes the Brewhouse village buildings (GDD section 18.2); the output is committed:

- village/plains/brewhouse  (oak frame, cobblestone footing, stone-brick cellar, oak casks, a hop garden)
- village/taiga/brewhouse   (spruce, mossy stone, spruce casks, a hop garden)
- village/snowy/brewhouse   (stripped spruce frame, stone bricks, dark oak casks; barley bales and a woodpile in the
                             snow instead of hops, which won't grow there)
- and each one's village/<type>/brewhouse_cellar: the 4 layers under the floor, built under the hall by the game

An 11x13 plot. Out front, a fenced garden with a gate onto the street (the jigsaw under the path) and two rows of hops
three trellises tall. The brewhouse itself is a 9x8 timber-framed hall with a gable roof: the Brew Kettle (the
Brewer's workstation) sits on a sunken campfire against the back wall under a brick chimney, between the Millstone
and the Kiln; down the left wall the Malting Tub and two Fermenting Vats, down the right a chest of brewing supplies
(loot), malt sacks and a barley bale; by the door a Keg, and a crafting table under a Bottle Shelf of ales; lanterns
hang from a cross beam. Outside, shutters stand open beside the front windows, with a bench and a barrel
under the eaves. A trapdoor in the floor by the right wall, clear on two sides, opens on a ladder down to the cellar: stone walls, two tiers of
tapped casks (one holding Old Ale already two years old: a year more in the cask earns its aging star), wine racks,
a keg, a bottle shelf, a tasting table with two stools, a barrel of cellar odds and ends (loot) and a cobweb or two.

    .venv/Scripts/python make_brewhouse.py
"""
from structgen import Plot, compound_payload, tag_byte, tag_compound, tag_int, tag_list, tag_long, tag_string, \
    TAG_COMPOUND

SX, SY, SZ = 11, 16, 13
FLOOR = 4                  # the ground: the street joins here, the hall's floor, the cellar's ceiling
X0, X1, Z0, Z1 = 1, 9, 4, 11   # the hall's walls (and the cellar's)
DOOR_X = 5
HATCH_Z = 9                # the cellar hatch at (8, FLOOR, 9): the hall floor at (7, 9) and (8, 10) stays clear beside it
TOP = 8                    # the wall plate; the roof starts above it

STYLES = {
    "plains": dict(wood="oak", log="minecraft:oak_log", footing="minecraft:cobblestone", stone="minecraft:stone_bricks",
                   stone_alt="minecraft:cracked_stone_bricks", cask="oak", garden="hops", snowy=False),
    "taiga": dict(wood="spruce", log="minecraft:spruce_log", footing="minecraft:mossy_cobblestone", stone="minecraft:stone_bricks",
                  stone_alt="minecraft:mossy_stone_bricks", cask="spruce", garden="hops", snowy=False),
    "snowy": dict(wood="spruce", log="minecraft:stripped_spruce_log", footing="minecraft:stone_bricks", stone="minecraft:stone_bricks",
                  stone_alt="minecraft:cracked_stone_bricks", cask="dark_oak", garden="bales", snowy=True),
}


def shelf_of(items: list[str]) -> bytes:
    """A Bottle Shelf's block entity, already holding these drinks (one per place, in order; 26.3's item handler format)."""
    entries = [compound_payload(tag_string("id", item), tag_int("count", 1)) for item in items]
    bottles = compound_payload(tag_list("stacks", TAG_COMPOUND, entries))
    return compound_payload(tag_string("id", "seedtocellar:wine_rack"), tag_compound("Bottles", bottles))


def aged_old_ale() -> bytes:
    """A cask of Old Ale (4 buckets, cultured yeast and the right temperature) that has rested two years: its tank as the
    cask saves it, and a relative age (AgeTicks) the cask turns into a fill time once it's in a world."""
    unit = compound_payload()   # a quality check passed: a marker component with no value
    fluid = compound_payload(tag_string("id", "seedtocellar:old_ale"), tag_int("amount", 4000), tag_compound("components", compound_payload(
        tag_compound("seedtocellar:brew", compound_payload(tag_compound("Brew", compound_payload()))),
        tag_compound("seedtocellar:quality_yeast", unit), tag_compound("seedtocellar:quality_temperature", unit))))
    tank = compound_payload(tag_list("stacks", TAG_COMPOUND, [fluid]))
    return compound_payload(tag_string("id", "seedtocellar:cask"), tag_compound("Tank", tank), tag_long("AgeTicks", 2 * 24000))


def build(style: dict) -> Plot:
    p = Plot(SX, SY, SZ)
    wood, log = style["wood"], style["log"]
    planks, stairs, slab = f"minecraft:{wood}_planks", f"minecraft:{wood}_stairs", f"minecraft:{wood}_slab"
    snowy = style["snowy"]

    # --- the ground, and air above it so the plot is clear -------------------------------------
    for x in range(SX):
        for z in range(SZ):
            p.set(x, FLOOR, z, "minecraft:grass_block", snowy=snowy)
            p.fill(x, FLOOR + 1, z, x, SY - 1, z, "minecraft:air")
    for z in range(0, Z0):
        p.set(DOOR_X, FLOOR, z, "minecraft:dirt_path")
    p.entrance(DOOR_X, FLOOR, 0, "north_up", "minecraft:dirt_path")

    # --- the cellar ------------------------------------------------------------------------------
    for x in range(X0, X1 + 1):
        for z in range(Z0, Z1 + 1):
            for y in range(0, FLOOR):
                wall = x in (X0, X1) or z in (Z0, Z1) or y == 0
                if wall:
                    p.set(x, y, z, style["stone_alt"] if (x * 3 + y * 5 + z * 7) % 9 == 0 else style["stone"])
                else:
                    p.set(x, y, z, "minecraft:air")
    for z in range(6, 10):                                    # two tiers of casks down the left wall
        p.set(2, 1, z, f"seedtocellar:{style['cask']}_cask", facing="east", tap=True)
        p.set(2, 2, z, f"seedtocellar:{style['cask']}_cask", facing="east", tap=False)
    p.block_entity(2, 1, 7, f"seedtocellar:{style['cask']}_cask", aged_old_ale(), facing="east", tap=True)
    p.set(3, 1, 10, "seedtocellar:wine_rack", facing="north")
    p.set(4, 1, 10, "seedtocellar:wine_rack", facing="north")
    p.set(6, 1, 10, "seedtocellar:keg", facing="north")
    for y in range(1, FLOOR):                                 # the ladder up to the hatch, against the east wall
        p.set(8, y, HATCH_Z, "minecraft:ladder", facing="west", waterlogged=False)
    p.set(8, 1, 10, "minecraft:barrel", facing="up", open=False)
    p.set(8, 1, 5, "minecraft:barrel", facing="up", open=False)
    p.loot(8, 1, 6, "minecraft:barrel", "seedtocellar:chests/brewhouse_cellar", facing="up", open=False)
    p.block_entity(8, 2, 8, "seedtocellar:bottle_shelf", shelf_of(["seedtocellar:stout", "seedtocellar:old_ale"]), facing="west")
    p.set(5, 3, 6, "minecraft:lantern", hanging=True, waterlogged=False)
    p.set(5, 3, 9, "minecraft:lantern", hanging=True, waterlogged=False)
    p.set(8, 3, 5, "minecraft:cobweb")
    p.set(3, 3, 9, "minecraft:cobweb")
    p.fence(5, 1, 7, wood)                                    # a tasting table and two stools
    p.set(5, 2, 7, f"minecraft:{wood}_pressure_plate", powered=False)
    p.stairs(5, 1, 6, stairs, "north")
    p.stairs(5, 1, 8, stairs, "south")

    # --- the hall: floor, timber frame, walls, windows, door -------------------------------------
    for x in range(X0, X1 + 1):
        for z in range(Z0, Z1 + 1):
            p.set(x, FLOOR, z, planks)
    for x in range(X0, X1 + 1):
        for z in range(Z0, Z1 + 1):
            if not (x in (X0, X1) or z in (Z0, Z1)):
                continue
            corner = x in (X0, X1) and z in (Z0, Z1)
            for y in range(FLOOR + 1, TOP + 1):
                if corner:
                    p.set(x, y, z, log, axis="y")
                elif y == FLOOR + 1:
                    p.set(x, y, z, style["footing"])           # a stone footing course
                elif y == TOP:
                    p.set(x, y, z, log, axis="x" if z in (Z0, Z1) else "z")   # the wall plate
                else:
                    p.set(x, y, z, planks)
    for x, z in ((3, Z0), (7, Z0), (3, Z1), (7, Z1), (X0, 6), (X0, 9), (X1, 6), (X1, 9)):
        for y in (FLOOR + 2, FLOOR + 3):
            p.pane(x, y, z)
    p.door(DOOR_X, FLOOR + 1, Z0, f"minecraft:{wood}_door", "south")
    for x in (2, 4, 6, 8):                                    # shutters, open against the wall, each side of the front windows
        for y in (FLOOR + 2, FLOOR + 3):
            p.set(x, y, Z0 - 1, f"minecraft:{wood}_trapdoor", facing="north", half="bottom", open=True, powered=False, waterlogged=False)
    p.stairs(7, FLOOR + 1, Z0 - 1, stairs, "south")           # a bench by the door, its back to the wall
    p.stairs(8, FLOOR + 1, Z0 - 1, stairs, "south")
    p.set(2, FLOOR + 1, Z0 - 1, "minecraft:barrel", facing="up", open=False)
    p.set(DOOR_X, FLOOR + 3, Z0 - 1, "minecraft:lantern", hanging=True, waterlogged=False)   # under the eave

    # --- the roof: a gable along the hall, an eave all round, the gable ends filled in -------------
    for d in range(5):
        y = TOP + 1 + d
        for x in range(0, SX):
            p.stairs(x, y, Z0 - 1 + d, stairs, "south")
            p.stairs(x, y, Z1 + 1 - d, stairs, "north")
        if d < 4:
            for x in (X0, X1):
                for z in range(Z0 + d, Z1 - d + 1):
                    p.set(x, y, z, planks)
    for x in (X0, X1):
        p.pane(x, TOP + 2, 7)                                 # a little window high in each gable
        p.pane(x, TOP + 2, 8)

    # --- the brewery inside --------------------------------------------------------------------
    back = Z1 - 1
    p.set(DOOR_X, FLOOR, back, "minecraft:campfire", lit=True, signal_fire=False, facing="north", waterlogged=False)
    p.set(DOOR_X, FLOOR + 1, back, "seedtocellar:brew_kettle")
    for y in range(TOP, SY - 1):                              # the brick chimney rising from over the kettle
        p.set(DOOR_X, y, back, "minecraft:bricks")
    p.set(3, FLOOR + 1, back, "seedtocellar:millstone")
    p.set(7, FLOOR + 1, back, "seedtocellar:kiln", facing="north", lit=False)
    p.set(2, FLOOR + 1, 9, "seedtocellar:malting_tub", contents="empty")
    p.set(2, FLOOR + 1, 7, "seedtocellar:fermenting_vat", fermenting=False, open=False, powered=False)
    p.set(2, FLOOR + 1, 6, "seedtocellar:fermenting_vat", fermenting=False, open=False, powered=False)
    p.loot(8, FLOOR + 1, 6, "minecraft:chest", "seedtocellar:chests/brewhouse", facing="west", type="single", waterlogged=False)
    p.set(8, FLOOR + 1, 7, "seedtocellar:pale_malt_sack")
    p.set(8, FLOOR + 2, 7, "seedtocellar:pale_malt_sack")
    p.set(8, FLOOR + 1, 8, "seedtocellar:barley_bale", axis="y")
    p.set(2, FLOOR + 1, 5, "seedtocellar:keg", facing="east")
    p.block_entity(6, FLOOR + 2, 5, "seedtocellar:bottle_shelf",
                   shelf_of(["seedtocellar:pale_ale", "seedtocellar:amber_ale", "seedtocellar:stout"]), facing="south")
    # the cellar hatch, in the floor by the right wall with room to walk up to it from the hall and from behind
    p.set(8, FLOOR, HATCH_Z, f"minecraft:{wood}_trapdoor", facing="north", half="top", open=False, powered=False, waterlogged=False)
    p.set(6, FLOOR + 1, 5, "minecraft:crafting_table")       # by the door, under the shelf of ales: a little counter
    for x in range(X0 + 1, X1):                               # a cross beam, lanterns hanging from it
        p.set(x, TOP, 7, log, axis="x")
    p.set(3, TOP - 1, 7, "minecraft:lantern", hanging=True, waterlogged=False)
    p.set(7, TOP - 1, 7, "minecraft:lantern", hanging=True, waterlogged=False)

    # --- the front garden ----------------------------------------------------------------------
    for x in range(SX):
        for z in range(0, Z0):
            if x in (0, SX - 1) or z == 0:
                p.fence(x, FLOOR + 1, z, wood)
    p.gate(DOOR_X, FLOOR + 1, 0, wood, "north")
    if style["garden"] == "hops":
        for xs in ((1, 2, 3), (7, 8, 9)):
            for i, x in enumerate(xs):
                p.set(x, FLOOR, 2, "minecraft:coarse_dirt")
                age = (5, 4, 5)[i]
                p.set(x, FLOOR + 1, 2, "seedtocellar:hops", age=age, axis="x", root=True)
                for y in (FLOOR + 2, FLOOR + 3):
                    p.set(x, y, 2, "seedtocellar:hops", age=age, axis="x", root=False)
    else:
        for x, y in ((1, 1), (2, 1), (3, 1), (1, 2), (2, 2)):  # bales of barley, and a woodpile
            p.set(x, FLOOR + y, 2, "seedtocellar:barley_bale", axis="y")
        for x, y in ((7, 1), (8, 1), (9, 1), (8, 2), (9, 2)):
            p.set(x, FLOOR + y, 2, "minecraft:spruce_log", axis="x")
        for x in range(SX):                                   # snow lying on the open ground
            for z in range(SZ):
                ground = p.get(x, FLOOR, z)
                above = p.get(x, FLOOR + 1, z)
                if ground and ground[0] == "minecraft:grass_block" and above and above[0] == "minecraft:air" \
                        and not (Z0 - 1 <= z <= Z1 + 1 and X0 <= x <= X1):
                    p.set(x, FLOOR + 1, z, "minecraft:snow", layers=1)
    return p


# A village fits each house inside the space of the street it joins, which starts at street level, so the cellar is
# its own template: the game builds it under the hall's floor when the Brewhouse is built (world/CellarPoolElement).
for village, style in STYLES.items():
    plot = build(style)
    plot.part(FLOOR, SY - 1).write(f"village/{village}/brewhouse")
    plot.part(0, FLOOR - 1).write(f"village/{village}/brewhouse_cellar", entrance=False)
