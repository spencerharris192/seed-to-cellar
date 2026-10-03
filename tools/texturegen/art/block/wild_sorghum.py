"""Wild sorghum (cross model): one sturdy plant with arching leaves and a rust-red head,
over a small fan of young leaves.

Found in savannas and on badlands edges. Same stalk, leaf and head as the farmed crop.
"""
import importlib.util
from pathlib import Path

from texturegen.compose import blank, stamp, stamp_bottom, to_grid

_spec = importlib.util.spec_from_file_location("_sorghum", Path(__file__).with_name("sorghum_crop.py"))
_sorghum = importlib.util.module_from_spec(_spec)
_spec.loader.exec_module(_sorghum)

LEGEND = _sorghum.LEGEND


def _build():
    c = blank()
    x, top = 7, 6                                   # stalk (2 wide) and where it starts
    stamp(c, _sorghum.head("ripe"), x - 1, top - 5)
    for yy in range(top, 16):
        c[yy][x], c[yy][x + 1] = "t", "r"
    stamp(c, _sorghum.LEAF, x + 2, top + 2)
    stamp(c, _sorghum.mirror(_sorghum.LEAF), x - 5, top + 5)
    stamp_bottom(c, _sorghum.FAN, 1)
    stamp_bottom(c, _sorghum.mirror(_sorghum.FAN), 10)
    return to_grid(c)


GRID = _build()

COMPARE = ["block/grass", "block/fern"]
