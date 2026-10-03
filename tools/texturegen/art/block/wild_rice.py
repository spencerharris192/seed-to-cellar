"""Wild rice (tall cross model, standing in shallow water): clumps of blades under the water
and, above it, blades topped with drooping heads, some still green and some ripe.

Found at river edges and in swamp shallows. Built from the farmed rice's parts.
"""
import importlib.util
from pathlib import Path

from texturegen.compose import stamp, to_grid

_spec = importlib.util.spec_from_file_location("_rice", Path(__file__).with_name("rice_crop.py"))
_rice = importlib.util.module_from_spec(_spec)
_spec.loader.exec_module(_rice)

LEGEND = _rice.LEGEND


def _top():
    """Green blades with one green and one ripe head (wild stands ripen unevenly)."""
    rows = [list(r) for r in _rice.upper(2).splitlines()]
    left, right = _rice.CLUMPS
    stamp(rows, _rice.recolor(_rice.PANICLE_RIPE, _rice.PANICLE_COLORS["ripe"]), right, 5)
    return to_grid(rows)


TEXTURES = {
    "wild_rice": _rice.lower(1),
    "wild_rice_top": _top(),
}

COMPARE = ["block/seagrass", "block/tall_seagrass_top"]
