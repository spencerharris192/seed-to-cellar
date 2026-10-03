"""The Pot Still's icon as its copper weathers (pot_still_exposed, _weathered, _oxidized; the item shows the stage its
blockstate carries): distillery_items.py's pot_still, the copper aged pixel for pixel as on the block
(texturegen.compose.weathered). The iron ring and the glass safe don't change.
"""
from texturegen.compose import WEATHERING, sibling, weathered
from texturegen.palettes import COPPER_EXPOSED, COPPER_PATINA, GLASS, IRON

items = sibling(__file__, "distillery_items")

# e h j i clear glass; A B C iron (as in distillery_items.py); K-P dulled copper, Q-V verdigris
LEGEND = {
    "e": GLASS[0], "h": GLASS[1], "j": GLASS[2], "i": GLASS[3],
    "A": IRON[1], "B": IRON[2], "C": IRON[3],
    **dict(zip("KLMNOP", COPPER_EXPOSED)),
    **dict(zip("QRSTUV", COPPER_PATINA)),
}

TEXTURES = {f"pot_still_{stage}": weathered(items.POT_STILL, stage, "klmnos", "KLMNOP", "QRSTUV") for stage in WEATHERING}

COMPARE = ["block/exposed_copper", "block/oxidized_copper"]
