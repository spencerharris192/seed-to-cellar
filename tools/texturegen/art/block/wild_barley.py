"""Wild barley (cross model): a loose grassy clump with a few ripe, bearded heads.

Found in plains and meadows. Green like the grass around it but tipped with the
same gold heads as the farmed crop, so players connect the two.
"""
import importlib.util
from pathlib import Path

from texturegen.compose import blank, stamp, stamp_bottom, to_grid

_spec = importlib.util.spec_from_file_location("_barley", Path(__file__).with_name("barley_crop.py"))
_barley = importlib.util.module_from_spec(_spec)
_spec.loader.exec_module(_barley)

LEGEND = _barley.LEGEND


def _build():
    c = blank()
    # three heads at different heights leaning out of a grassy clump
    for cx, top, kind in ((4, 3, "ripe"), (8, 1, "ripe"), (11, 5, "turning")):
        stamp(c, _barley.ear(kind), cx - 1, top)
        for yy in range(top + 7, 12):
            c[yy][cx] = "t" if kind == "turning" else "4"
    stamp_bottom(c, _barley.TUFT3, 0)
    stamp_bottom(c, _barley.mirror(_barley.TUFT3), 5)
    stamp_bottom(c, _barley.TUFT3, 9)
    return to_grid(c)


GRID = _build()

COMPARE = ["block/grass", "block/fern", "block/wheat_stage7"]
