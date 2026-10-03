"""Wild rye (cross model): a blue-green grassy clump with a few tall, slim grey-gold spikes.

Found in taiga and snowy plains. Same spike and blue-green leaves as the farmed crop, so
players connect the two.
"""
import importlib.util
from pathlib import Path

from texturegen.compose import blank, stamp, stamp_bottom, to_grid

_spec = importlib.util.spec_from_file_location("_rye", Path(__file__).with_name("rye_crop.py"))
_rye = importlib.util.module_from_spec(_spec)
_spec.loader.exec_module(_rye)

LEGEND = _rye.LEGEND


def _build():
    c = blank()
    for cx, top, kind in ((4, 1, "ripe"), (8, 0, "ripe"), (12, 3, "turning")):
        stamp(c, _rye.spike(kind), cx - 1, top)
        for yy in range(top + 7, 12):
            c[yy][cx] = "t" if kind == "turning" else "2"
    stamp_bottom(c, _rye.TUFT3, 0)
    stamp_bottom(c, _rye.mirror(_rye.TUFT3), 5)
    stamp_bottom(c, _rye.TUFT3, 9)
    return to_grid(c)


GRID = _build()

COMPARE = ["block/grass", "block/fern"]
