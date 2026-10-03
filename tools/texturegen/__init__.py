"""Seed to Cellar texture pipeline.

Textures are hand-placed pixel art written as character grids (see art/),
mapped to shared named palettes (palettes.py). Run from the tools/ folder:

    .venv/Scripts/python -m texturegen.build            # write PNGs into the mod
    .venv/Scripts/python -m texturegen.build --check    # fail if PNGs are stale
    .venv/Scripts/python -m texturegen.vanilla          # extract vanilla references (local only)
    .venv/Scripts/python -m texturegen.preview item/barley   # contact sheet -> tools/out/
"""
