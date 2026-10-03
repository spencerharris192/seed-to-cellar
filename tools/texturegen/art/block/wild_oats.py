"""Wild oats (cross model): a green grassy clump with two drooping sprays of oat bells.

Found in plains and meadows alongside wild barley; the hanging bells tell them apart.
"""
import importlib.util
from pathlib import Path

from texturegen.compose import blank, stamp, stamp_bottom, to_grid

_spec = importlib.util.spec_from_file_location("_oat", Path(__file__).with_name("oat_crop.py"))
_oat = importlib.util.module_from_spec(_spec)
_spec.loader.exec_module(_oat)

LEGEND = _oat.LEGEND


def _build():
    c = blank()
    for cx, top, kind in ((4, 1, "ripe"), (11, 2, "turning")):
        stamp(c, _oat.recolor(_oat.PANICLE, kind), cx - 2, top)
        for yy in range(top + 8, 12):
            c[yy][cx] = "t" if kind == "turning" else "2"
    stamp_bottom(c, _oat.TUFT3, 0)
    stamp_bottom(c, _oat.mirror(_oat.TUFT3), 5)
    stamp_bottom(c, _oat.TUFT3, 9)
    return to_grid(c)


GRID = _build()

COMPARE = ["block/grass", "block/fern"]
