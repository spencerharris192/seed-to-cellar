"""The Brew Kettle's and Pot Still's copper as it weathers (models brew_kettle_<stage>, pot_still_lower_<stage>,
pot_still_upper_<stage>, pot_still_upper_basket_<stage>): every copper texture of brewhouse.py and pot_still.py again at
each later stage, pixel for pixel, the copper shades swapped (texturegen.compose.weathered):

- _exposed: the copper dulled and browned, the first few spots of blue-green verdigris.
- _weathered: verdigris over about half of it in rounded patches, dull copper between.
- _oxidized: green all over.

The rivets, seams, rings and glints keep their places and their light and shade; the iron, glass and botanicals don't
change.
"""
from texturegen.compose import WEATHERING, sibling, weathered
from texturegen.palettes import COPPER_EXPOSED, COPPER_PATINA, FOLIAGE_DUSTY, GLASS, IRON

kettle = sibling(__file__, "brewhouse")
still = sibling(__file__, "pot_still")

# A-F dulled copper, K-P verdigris; a-f iron, g-j clear glass, v w botanicals (as in pot_still.py)
LEGEND = {
    **dict(zip("ABCDEF", COPPER_EXPOSED)),
    **dict(zip("KLMNOP", COPPER_PATINA)),
    **dict(zip("abcdef", IRON)),
    **dict(zip("ghij", GLASS)),
    "v": FOLIAGE_DUSTY[2], "w": FOLIAGE_DUSTY[3],
}

SOURCES = {name: kettle.TEXTURES[name] for name in ("brew_kettle_side", "brew_kettle_inner", "brew_kettle_bottom")}
SOURCES.update({name: still.TEXTURES[name] for name in ("pot_still_copper", "pot_still_top", "pot_still_neck",
                                                         "pot_still_pipe", "pot_still_safe", "pot_still_basket")})

TEXTURES = {f"{name}_{stage}": weathered(grid, stage, "012345", "ABCDEF", "KLMNOP")
            for stage in WEATHERING for name, grid in SOURCES.items()}

COMPARE = ["block/exposed_copper", "block/weathered_copper", "block/oxidized_copper"]
