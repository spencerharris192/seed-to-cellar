"""Orange peel: the lemon peel's curl (peels.py) in deep orange, its pith a little warmer."""
from texturegen.compose import sibling
from texturegen.palettes import FLOUR, ORANGE

peels = sibling(__file__, "peels")

LEGEND = {**dict(zip("12345", ORANGE[1:6])), "p": FLOUR[4]}

TEXTURES = {
    "orange_peel": peels.LEMON_PEEL,
}
