"""Wild corn (cross model): a short, one-block corn plant with a small ear and a tassel.

Found in plains and savannas. Same stalk, leaves, ear and tassel as the farmed crop, only
smaller, so players connect the two.
"""
import importlib.util
from pathlib import Path

from texturegen.compose import blank, stamp, to_grid

_spec = importlib.util.spec_from_file_location("_corn", Path(__file__).with_name("corn_crop.py"))
_corn = importlib.util.module_from_spec(_spec)
_spec.loader.exec_module(_corn)

LEGEND = _corn.LEGEND


def _build():
    c = blank()
    top = 5
    _corn.stalk(c, top, False, (7, 10, 13))
    stamp(c, _corn.recolor(_corn.TASSEL, _corn.TASSEL_COLORS["green"]), _corn.STALK - 1, top - 5)
    stamp(c, _corn.ear("green"), _corn.STALK - 3, 6)
    return to_grid(c)


GRID = _build()

COMPARE = ["block/grass", "block/fern", "block/sunflower_bottom"]
