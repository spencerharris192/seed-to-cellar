"""Showcase scenes for the mod's CurseForge screenshots (development only: nothing here ships in the mod).

Writes five scenes as structure templates into the showcase world's `generated` folder (where /place template finds a
world's own templates) and the photo tour that builds and photographs them, with the Brewhouse, its cellar and the
Vineyard from the mod's own village templates:

    .venv/Scripts/python make_showcase.py          # then: gradlew runClient -Ptour  (see CLAUDE.md, Releases)

Scenes are open-fronted rooms or open plots, their front (the camera's side) to the south; the tour clears a flat plot
for each, places it, and takes its photos with the HUD hidden. Cameras are eye positions relative to a scene's corner.
"""
import json
from pathlib import Path

from structgen import Plot, compound_payload, tag_byte, tag_compound, tag_int, tag_list, tag_long, tag_string, \
    TAG_COMPOUND

ROOT = Path(__file__).resolve().parent.parent
WORLD = ROOT / "run/saves/showcase"
TEMPLATES = WORLD / "generated/seedtocellar/structures"
TOUR = ROOT / "run/showcase_tour.json"
BLOCKSTATES = ROOT / "src/generated/resources/assets/seedtocellar/blockstates"
M = "seedtocellar:"


def max_age(block: str) -> int:
    """A crop's ripe age, from its blockstate file."""
    data = json.loads((BLOCKSTATES / f"{block}.json").read_text())
    return max(int(kv.split("=")[1]) for key in data["variants"] for kv in key.split(",") if kv.startswith("age="))


# --- block entity NBT -------------------------------------------------------------------------------------------

def item(item_id: str, slot: int | None = None, count: int = 1, age: int = 0) -> bytes:
    """An item stack; `age` puts years on a spirit's label (its color deepens with them)."""
    parts = []
    if slot is not None:
        parts.append(tag_byte("Slot", slot))
    parts += [tag_string("id", item_id if ":" in item_id else M + item_id), tag_byte("Count", count)]
    if age:
        parts.append(tag_compound("tag", compound_payload(tag_int("Age", age), tag_string("Wood", "oak"))))
    return compound_payload(*parts)


def rack(drinks: list, size: int) -> bytes:
    """A Bottle Shelf, Wine Rack, Wine Display or Mug Rack holding these (a name, or (name, age))."""
    entries = []
    for i, d in enumerate(drinks):
        name, age = d if isinstance(d, tuple) else (d, 0)
        entries.append(item(name, slot=i, age=age))
    return compound_payload(tag_string("id", M + "wine_rack"), tag_compound("Bottles", compound_payload(
        tag_list("Items", TAG_COMPOUND, entries), tag_int("Size", size))))


def placed(drinks: list) -> bytes:
    entries = []
    for d in drinks:
        name, age = d if isinstance(d, tuple) else (d, 0)
        entries.append(item(name, age=age))
    return compound_payload(tag_string("id", M + "placed_drinks"), tag_list("Drinks", TAG_COMPOUND, entries))


def tank(fluid: str, amount: int, age: int = 0) -> bytes:
    parts = [tag_string("FluidName", M + fluid), tag_int("Amount", amount)]
    if age:
        parts.append(tag_compound("Tag", compound_payload(tag_int("Age", age), tag_string("Wood", "oak"))))
    return compound_payload(*parts)


def handler(stacks: dict, size: int) -> bytes:
    """An item handler's NBT: {slot: item id}."""
    return compound_payload(tag_list("Items", TAG_COMPOUND, [item(i, slot=s) for s, i in stacks.items()]), tag_int("Size", size))


def entity(kind: str, *parts: bytes) -> bytes:
    return compound_payload(tag_string("id", M + kind), *parts)


# --- shared pieces --------------------------------------------------------------------------------------------

def room(p: Plot, w: int, d: int, floor: str, wall: str, pillar: str, band: str, ceiling: str, beam: str, roof: str,
         windows: tuple = ()) -> None:
    """An open-fronted room: floor at y=0, walls on the north, west and east sides (y 1-5), a beamed ceiling at y=6 and a
    slab roof above; its south side open to the camera. A lip of grass outside the open front."""
    for x in range(w):
        for z in range(d):
            p.set(x, 0, z, floor)
            p.set(x, 6, z, beam if z % 3 == 2 else ceiling, **({"axis": "x"} if z % 3 == 2 else {}))
            p.set(x, 7, z, roof, type="bottom", waterlogged=False)
        p.set(x, 0, d, "minecraft:grass_block", snowy=False)
    for y in range(1, 6):
        for x in range(w):
            p.set(x, y, 0, band if y == 5 else wall)
        for z in range(d):
            for x in (0, w - 1):
                p.set(x, y, z, band if y == 5 else wall)
        for x in (0, 4, w - 5, w - 1):                       # pillars on the back wall and the front corners
            p.set(x, y, 0, pillar, axis="y")
        for x in (0, w - 1):
            p.set(x, y, d - 1, pillar, axis="y")
    for x in (0, w - 1):                                     # windows in the side walls at these z
        for y in (2, 3):
            for z in windows:
                p.pane(x, y, z)


def lantern(p: Plot, x, y, z):
    p.set(x, y, z, "minecraft:lantern", hanging=True, waterlogged=False)


def shelf(p: Plot, x, y, z, facing: str, drinks: list, kind: str = "bottle_shelf", size: int = 6):
    p.block_entity(x, y, z, M + kind, rack(drinks, size), facing=facing)


def drinks_on(p: Plot, x, y, z, facing: str, drinks: list):
    p.block_entity(x, y, z, M + "placed_drinks", placed(drinks), facing=facing, drinks=len(drinks))


ALES = ["pale_ale", "amber_ale", "stout", "old_ale", "wheat_beer", "lager"]
WINES = ["red_wine", "white_wine", "rose", "mead", "cherry_wine", "plum_wine"]
SPIRITS = [("malt_whiskey", 12), ("bourbon", 8), "vodka", "gin", ("rum", 6), ("brandy", 10)]
MORE_SPIRITS = [("tequila", 4), ("kirsch", 3), ("slivovitz", 6), "grappa", ("apple_brandy", 6), "aromatic_bitters"]
LIQUEURS = ["limoncello", "orange_liqueur", "umeshu", "creme_de_mure", "coffee_liqueur", "apple_crown_whiskey"]


# --- the scenes -----------------------------------------------------------------------------------------------

def tavern() -> Plot:
    """A tavern: a dark-oak bar turning a corner, stools, shelves of ales, wines and spirits, casks on tap, a mug rack,
    hops strung along the back wall under the tavern sign, a laid table and bundles hanging from the beams."""
    w, d = 13, 10
    p = Plot(w, 8, d + 1)
    room(p, w, d, "minecraft:spruce_planks", "minecraft:spruce_planks", "minecraft:stripped_dark_oak_log",
         "minecraft:dark_oak_planks", "minecraft:dark_oak_planks", "minecraft:stripped_dark_oak_log", "minecraft:spruce_slab",
         windows=(5, 6))
    # the back wall, behind the bar
    for x, y, drinks in ((2, 2, ALES), (3, 2, WINES), (2, 3, SPIRITS), (3, 3, LIQUEURS),
                         (9, 2, WINES[::-1]), (10, 2, ALES[::-1]), (9, 3, MORE_SPIRITS), (10, 3, SPIRITS[::-1])):
        shelf(p, x, y, 1, "south", drinks)
    shelf(p, 5, 2, 1, "south", ["mug"] * 4, "mug_rack", 4)
    shelf(p, 7, 2, 1, "south", ["mug"] * 4, "mug_rack", 4)
    p.set(6, 3, 1, M + "tavern_sign", facing="south", emblem="ale")
    for x in range(1, w - 1):
        p.set(x, 4, 1, M + "hop_garland", facing="south")
    for z in (2, 3):                                         # casks on tap down the left wall, kegs on the right
        p.set(1, 1, z, M + "oak_cask", facing="east", tap=True, charred=False)
        p.set(1, 2, z, M + "oak_cask", facing="east", tap=False, charred=False)
    p.set(11, 1, 2, M + "keg", facing="west")
    shelf(p, 11, 2, 3, "west", ["red_wine", "white_wine", "rose"], "wine_display", 3)
    shelf(p, 11, 1, 3, "west", ["red_wine", "white_wine", "mead", "rose", "red_wine", "white_wine"], "wine_rack", 6)
    # the bar: a long front facing the room, turning a corner back toward the wall on the right
    for x in range(3, 10):
        p.set(x, 1, 4, M + "dark_oak_bar_counter", facing="south", shape="straight")
    p.set(10, 1, 4, M + "dark_oak_bar_counter", facing="south", shape="outer_right")
    p.set(10, 1, 3, M + "dark_oak_bar_counter", facing="east", shape="straight")
    drinks_on(p, 4, 2, 4, "south", ["pale_ale", "stout"])
    drinks_on(p, 6, 2, 4, "south", ["red_wine"])
    drinks_on(p, 8, 2, 4, "south", [("malt_whiskey", 12), "gin", ("rum", 6)])
    for x in (4, 6, 8):
        p.set(x, 1, 5, M + "dark_oak_bar_stool")
    # the room: a laid table in the middle, barrel tables either side
    for x in (5, 6, 7):
        p.set(x, 1, 7, "minecraft:spruce_slab", type="top", waterlogged=False)
    p.set(6, 2, 7, M + "harvest_feast", facing="south", servings=6)
    p.set(5, 2, 7, M + "apple_pie", bites=0)
    p.set(7, 2, 7, M + "cherry_pie", bites=1)
    for x in (2, 10):
        p.set(x, 1, 7, "minecraft:barrel", facing="up", open=False)
    drinks_on(p, 2, 2, 7, "south", ["cider", "perry", "mead"])
    p.set(10, 2, 7, M + "black_forest_cake", bites=1)
    for x in (1, 3, 9, 11):
        p.set(x, 1, 7, M + "spruce_bar_stool")
    # light, and bundles hanging from the beams
    for x, z in ((3, 2), (9, 2), (6, 5), (2, 6), (10, 6)):
        lantern(p, x, 5, z)
    for x, z, bundle in ((4, 3, "hop_bundle"), (8, 3, "garlic_braid"), (4, 7, "lavender_bundle"), (8, 7, "chili_string")):
        p.set(x, 5, z, M + bundle, facing="south", wall=False)
    return p


def distillery() -> Plot:
    """The distillery: three Pot Stills over magma (fresh, weathering and green with age; the middle one with its Gin
    Basket), their spirit safes full, shelves and displays of spirits and liqueurs, charred casks and a tasting bar."""
    w, d = 13, 10
    p = Plot(w, 8, d + 1)
    room(p, w, d, "minecraft:polished_andesite", "minecraft:stone_bricks", "minecraft:stripped_spruce_log",
         "minecraft:cut_copper", "minecraft:spruce_planks", "minecraft:stripped_spruce_log", "minecraft:stone_brick_slab")
    for x, weathering, basket, spirit in ((3, "unaffected", False, "malt_whiskey"), (6, "weathered", True, "gin"),
                                         (9, "oxidized", False, "tequila")):
        p.set(x, 1, 2, "minecraft:magma_block")                  # heat with a glow and no smoke to hide the still
        p.block_entity(x, 2, 2, M + "pot_still", entity("pot_still", tag_compound("Receiver", tank(spirit, 2000))),
                       facing="south", half="lower", weathering=weathering, waxed=True, basket=basket, active=False)
        p.set(x, 3, 2, M + "pot_still", facing="south", half="upper", weathering=weathering, waxed=True, basket=basket,
              active=False)
    for z, y, drinks in ((4, 2, SPIRITS), (4, 3, LIQUEURS), (5, 2, MORE_SPIRITS), (5, 3, SPIRITS[::-1])):
        shelf(p, 1, y, z, "east", drinks)
    for z, y, drinks in ((4, 2, [("malt_whiskey", 12), ("bourbon", 8), ("brandy", 10)]), (4, 3, ["gin", "vodka", ("rum", 6)]),
                         (5, 2, ["apple_crown_whiskey", ("kirsch", 3), ("tequila", 4)]), (5, 3, LIQUEURS[:3])):
        shelf(p, 11, y, z, "west", drinks, "wine_display", 3)
    for x in (1, 2):
        p.set(x, 1, 7, M + "oak_cask", facing="south", tap=True, charred=True)
        p.set(x, 2, 7, M + "oak_cask", facing="south", tap=False, charred=True)
    p.set(11, 1, 7, M + "keg", facing="south")
    for x in range(5, 8):
        p.set(x, 1, 7, M + "spruce_bar_counter", facing="south", shape="straight")
    drinks_on(p, 5, 2, 7, "south", [("malt_whiskey", 12), ("bourbon", 8)])
    drinks_on(p, 6, 2, 7, "south", ["apple_crown_whiskey"])
    drinks_on(p, 7, 2, 7, "south", [("rum", 6), ("tequila", 4), "gin"])
    for x, z in ((3, 5), (9, 5), (6, 5)):
        lantern(p, x, 5, z)
    return p


def brewery() -> Plot:
    """The brewery: barley sprouting in the Malting Tub, malt on the Kiln, the Millstone, the Brew Kettle over magma,
    an open vat of wort and one closed and working, jars, sacks, bales and crates, the Compost Bin and the Drying Rack."""
    w, d = 15, 10
    p = Plot(w, 8, d + 1)
    room(p, w, d, "minecraft:oak_planks", "minecraft:oak_planks", "minecraft:oak_log", "minecraft:cobblestone",
         "minecraft:spruce_planks", "minecraft:stripped_oak_log", "minecraft:oak_slab")
    for x in range(w):                                       # a cobblestone footing along the walls
        p.set(x, 1, 0, "minecraft:cobblestone")
    for z in range(d):
        for x in (0, w - 1):
            p.set(x, 1, z, "minecraft:cobblestone")
    p.set(2, 1, 2, M + "malting_tub", contents="sprouting")
    p.block_entity(4, 1, 2, M + "kiln", entity("kiln", tag_compound("Items", handler({0: "green_barley_malt"}, 3))),
                   facing="south", lit=True)
    p.set(6, 1, 2, M + "millstone")
    p.set(8, 1, 2, "minecraft:magma_block")
    p.block_entity(8, 2, 2, M + "brew_kettle", entity("brew_kettle", tag_compound("Tank", tank("sweet_wort", 3000))),
                   weathering="unaffected", waxed=True, active=False)
    p.block_entity(10, 1, 2, M + "fermenting_vat", entity("fermenting_vat", tag_compound("Tank", tank("hopped_wort", 7000))),
                   open=True, fermenting=False, powered=False)
    p.block_entity(12, 1, 2, M + "fermenting_vat", entity("fermenting_vat", tag_compound("Tank", tank("pale_ale", 7000))),
                   open=False, fermenting=True, powered=False)
    # stores along the side walls
    p.set(1, 1, 4, M + "pale_malt_sack")
    p.set(1, 2, 4, M + "wheat_flour_sack")
    p.set(1, 1, 5, M + "barley_bale", axis="y")
    p.set(1, 1, 6, M + "barley_bale", axis="z")
    p.set(13, 1, 4, M + "apple_crate")
    p.set(13, 2, 4, M + "red_grape_crate")
    p.set(13, 1, 5, M + "coffee_sack")
    p.block_entity(12, 1, 6, M + "drying_rack", entity("drying_rack", tag_list("Items", TAG_COMPOUND, [
        compound_payload(tag_byte("Slot", i), tag_string("id", M + kind), tag_byte("Count", 1), tag_int("Progress", 0),
                         tag_int("Total", 12000)) for i, kind in enumerate(["hop_cones", "chili", "hop_cones", "chili"])]),
        tag_long("LastCheck", 0)), facing="west")
    # jars on barrels, the compost bin
    for x, fluid, is_open in ((3, "limoncello", True), (4, "vinegar", False), (5, "sweet_wort", True)):
        p.set(x, 1, 7, "minecraft:barrel", facing="up", open=False)
        p.block_entity(x, 2, 7, M + "preserving_jar", entity("preserving_jar", tag_compound("Tank", tank(fluid, 1000))),
                       open=is_open)
    p.set(9, 1, 7, M + "compost_bin", level=4, ready=True)
    for x, z in ((4, 5), (10, 5), (7, 3)):
        lantern(p, x, 5, z)
    for x, z in ((2, 3), (12, 3), (6, 7)):
        p.set(x, 5, z, M + "hop_bundle", facing="south", wall=False)
    return p


def farm() -> Plot:
    """A farm: rows of ripe grain, corn, vegetables and spices on watered farmland, rice in a paddy, bushes and herbs, agave
    on sand, and hops and grapes climbing trellises."""
    w, d = 22, 17
    p = Plot(w, 6, d)
    for x in range(w):
        for z in range(d):
            p.set(x, 0, z, "minecraft:dirt")
            p.set(x, 1, z, "minecraft:grass_block", snowy=False)

    def row(z, x0, x1, crop, fertile=False):
        age = max_age(crop)
        for x in range(x0, x1 + 1):
            if fertile:
                p.set(x, 1, z, M + "fertile_farmland", fertility=3, moisture=7)
            else:
                p.set(x, 1, z, "minecraft:farmland", moisture=7)
            if crop == "corn_crop":
                p.set(x, 2, z, M + crop, age=age, half="lower")
                p.set(x, 3, z, M + crop, age=age, half="upper")
            else:
                p.set(x, 2, z, M + crop, age=age)

    # from the back (north) to the front: trellises, corn, grain, the paddy and bushes, herbs, then low vegetables
    for x0, x1, vine in ((2, 6, "hops"), (8, 12, "red_grape_vine"), (14, 18, "white_grape_vine")):
        for x in range(x0, x1 + 1):
            for y in (2, 3, 4):                              # the root in the soil, the vine climbing the two above
                p.set(x, y, 1, M + vine, age=5, axis="x", root=y == 2)
    row(3, 2, 19, "corn_crop")
    row(5, 2, 9, "barley_crop", fertile=True)
    row(5, 11, 19, "oat_crop")
    row(6, 2, 9, "rye_crop")
    row(6, 11, 19, "sorghum_crop")
    for x in range(1, 21):
        p.set(x, 1, 7, "minecraft:water", level=0)
    for x in range(2, 10):                                   # the paddy: rice standing in water over mud
        p.set(x, 0, 9, "minecraft:mud")
        p.set(x, 1, 9, M + "rice_crop", age=max_age("rice_crop"), waterlogged=True)
    for x0, x1, bush in ((11, 13, "tomato_bush"), (14, 16, "chili_bush"), (17, 19, "cucumber_bush")):
        for x in range(x0, x1 + 1):
            p.set(x, 2, 9, M + bush, age=max_age(bush))
    for x0, x1, bush in ((2, 4, "blueberry_bush"), (5, 7, "blackberry_bush"), (8, 10, "elderberry_bush"),
                         (11, 13, "juniper_bush"), (14, 16, "coffee_bush")):
        for x in range(x0, x1 + 1):
            p.set(x, 2, 11, M + bush, age=max_age(bush))
    for x0, x1, herb in ((2, 5, "lavender_crop"), (6, 9, "mint_crop"), (10, 13, "wormwood_crop")):
        for x in range(x0, x1 + 1):
            p.set(x, 2, 12, M + herb, age=max_age(herb))
    for x in range(16, 20):                                  # agave in flower on a patch of sand
        for z in (11, 12):
            p.set(x, 1, z, "minecraft:sand")
        p.set(x, 2, 12 if x % 2 else 11, M + "agave", age=max_age("agave"))
    row(14, 2, 4, "onion_crop")
    row(14, 5, 7, "garlic_crop")
    row(14, 8, 10, "cabbage_crop")
    row(14, 11, 13, "sugar_beet_crop")
    row(14, 14, 15, "coriander_crop")
    row(14, 16, 17, "anise_crop")
    row(14, 18, 19, "ginger_crop")
    return p


TREES = [("apple", "minecraft:oak_log"), ("cherry", "minecraft:cherry_log"), ("peach", "minecraft:acacia_log"),
         ("lemon", "minecraft:jungle_log"), ("orange", "minecraft:jungle_log"), ("pear", "minecraft:birch_log")]


def orchard() -> Plot:
    """An orchard in fruit: six trees heavy with apples, cherries, peaches, lemons, oranges and pears, crates of fruit,
    the Fruit Press and the Crushing Tub."""
    w, d = 30, 13
    p = Plot(w, 9, d)
    for x in range(w):
        for z in range(d):
            p.set(x, 0, z, "minecraft:grass_block", snowy=False)
    for i, (fruit, log) in enumerate(TREES):
        tx, tz = 2 + i * 5, 4 + (i % 2) * 2
        leaves = dict(age=3, persistent=True, distance=1, waterlogged=False)
        for dx in range(-2, 3):
            for dz in range(-2, 3):
                for y in (4, 5):
                    if abs(dx) + abs(dz) <= 2 and 0 <= tx + dx < w and (dx, dz, y) != (0, 0, 4):
                        p.set(tx + dx, y, tz + dz, M + f"{fruit}_leaves", **leaves)
                if abs(dx) + abs(dz) <= 1 and 0 <= tx + dx < w:
                    p.set(tx + dx, 6, tz + dz, M + f"{fruit}_leaves", **leaves)
        for y in range(1, 5):
            p.set(tx, y, tz, log, axis="y")
        p.set(tx + 1, 1, tz + 3, M + f"{fruit}_crate" if fruit != "pear" else M + "pear_crate")
    p.set(10, 1, 10, M + "fruit_press", facing="south", powered=False)
    p.set(13, 1, 10, M + "crushing_tub")
    return p


# --- the tour -------------------------------------------------------------------------------------------------

SITE_GAP = 64       # sites four to a row, 64 blocks apart, rows 90 apart
ROW_GAP = 90


def main():
    TEMPLATES.mkdir(parents=True, exist_ok=True)
    scenes = {"tavern": tavern(), "distillery": distillery(), "brewery": brewery(), "farm": farm(), "orchard": orchard()}
    for name, plot in scenes.items():
        plot.write(f"showcase/{name}", entrance=False, folder=TEMPLATES)

    steps = [{"setup": True}]
    sites = []

    def site(template: str, size, cameras, below: str | None = None, at_y: int = 0):
        """A plot on the grid: cleared flat (wild grass and flowers scattered round it), the template placed with its y=0
        at `at_y` (a village building's street jigsaw turned into the path it becomes in a village), its photos."""
        x, z = (len(sites) % 4) * SITE_GAP, (len(sites) // 4) * ROW_GAP
        sites.append(template)
        sx, sy, sz = size
        steps.append({"clear": [x - 12, -6, z - 14, x + sx + 12, 30, z + sz + 14], "ground": 0, "scatter": True,
                      "free": [x, z, x + sx, z + sz]})
        steps.append({"place": template, "at": [x, at_y, z]})
        if below:
            steps.append({"place": below, "at": [x, at_y - 4, z]})
        steps.append({"replace": [x, at_y, z, x + sx, at_y + sy, z + sz], "from": "minecraft:jigsaw", "to": "minecraft:dirt_path"})
        steps.append({"reshape": [x, at_y - 4, z, x + sx, at_y + sy, z + sz]})
        for cam in cameras:
            ex, ey, ez = cam["eye"]
            steps.append({"shot": cam["name"], "eye": [x + ex, at_y + ey, z + ez], "yaw": cam["yaw"], "pitch": cam["pitch"],
                          "time": cam.get("time", 6000)})

    site("seedtocellar:showcase/tavern", scenes["tavern"].size, [
        dict(name="tavern", eye=[6.5, 3.3, 11.2], yaw=180, pitch=10),
        dict(name="tavern_bar", eye=[2.5, 2.9, 8.5], yaw=215, pitch=15)])
    site("seedtocellar:showcase/distillery", scenes["distillery"].size, [
        dict(name="distillery", eye=[6.5, 3.3, 11.2], yaw=180, pitch=10),
        dict(name="distillery_stills", eye=[4.6, 2.7, 6.4], yaw=200, pitch=8)])
    site("seedtocellar:showcase/brewery", scenes["brewery"].size, [
        dict(name="brewery", eye=[7.5, 3.3, 11.2], yaw=180, pitch=10),
        dict(name="brewery_line", eye=[4.2, 2.7, 6.2], yaw=210, pitch=10)])
    site("seedtocellar:showcase/farm", scenes["farm"].size, [
        dict(name="farm", eye=[11.0, 6.2, 17.0], yaw=180, pitch=30),
        dict(name="farm_trellis", eye=[3.5, 3.4, -3.0], yaw=-25, pitch=8)], at_y=-1)
    site("seedtocellar:showcase/orchard", scenes["orchard"].size, [
        dict(name="orchard", eye=[15.0, 3.8, 14.0], yaw=180, pitch=4)])
    site("seedtocellar:village/plains/brewhouse", (11, 12, 13), [
        dict(name="brewhouse", eye=[12.5, 4.5, -3.5], yaw=31, pitch=8, time=11000),
        dict(name="cellar", eye=[7.2, -1.5, 10.6], yaw=135, pitch=8)],
         below="seedtocellar:village/plains/brewhouse_cellar")
    site("seedtocellar:village/plains/vineyard", (11, 4, 11), [
        dict(name="vineyard", eye=[5.5, 4.2, -2.0], yaw=0, pitch=18)])
    steps.append({"done": True})
    TOUR.write_text(json.dumps({"steps": steps}, indent=1))
    print(f"Wrote {TOUR} ({len(steps)} steps)")


if __name__ == "__main__":
    main()
