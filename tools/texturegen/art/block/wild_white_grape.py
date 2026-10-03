"""Wild white grape (cross model): the wild red grape's drawing with pale green-gold bunches."""
from texturegen.compose import sibling

red = sibling(__file__, "wild_red_grape")
white_vine = sibling(__file__, "white_grape_crop")

LEGEND = white_vine.LEGEND

GRID = red.GRID

COMPARE = red.COMPARE
