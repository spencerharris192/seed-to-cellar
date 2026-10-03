"""Lavender, 4 looks: planted, cut back (a small grey-green tuft), growing (buds forming),
full (tall purple flower spikes rising from a mound of narrow grey-green leaves).
"""
from texturegen.compose import plants, to_grid
from texturegen.palettes import FOLIAGE_DUSTY, LAVENDER

# q-y = FOLIAGE_DUSTY (grey-green leaves), 0-5 = LAVENDER (flowers)
LEGEND = {**dict(zip("qwerty", FOLIAGE_DUSTY)), **{str(i): LAVENDER[i] for i in range(6)}}

SPROUT = """
t.t
.t.
.e.
"""

YOUNG = """
.t.t.t.
ytytyty
.tetet.
..e.e..
"""

MID = """
..r...r...r..
..r.r.r.r.r..
..t.t.t.t.t..
.tyt.tyt.tyt.
tytetytetytet
.tetetetetet.
..e.e.e.e.e..
"""

FULL = """
..5...4...5..
..4.5.3.4.4..
..3.4.2.3.3..
..3.3.2.2.3..
...r.r.r.r...
...r.r.r.r...
.t.r.t.r.t.t.
tytryttrytyty
.tetetetetet.
..etetetete..
...e.e.e.e...
"""

TEXTURES = {f"lavender_stage{i}": to_grid(plants(s, (8,))) for i, s in enumerate((SPROUT, YOUNG, MID, FULL))}

COMPARE = ["block/lilac_top", "block/fern"]
