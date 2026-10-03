"""Shared still-liquid texture (grey, tinted per fluid in game). 8 frames: soft highlight
streaks drift slowly across a calm surface. Low contrast so tints stay true.
"""
from texturegen.palettes import LIQUID

LEGEND = {str(i): LIQUID[i] for i in range(6)}

BASE = """
2222222222222222
2222232222222222
2222333322222222
2222222222222222
2222222222223322
2222222222333332
1222222222222222
2222222222222222
2233332222222222
2223333222222222
2222222222222222
2222222222222322
2222222223333333
2222222222222222
2222212222222222
2222222222222222
"""

rows = BASE.strip("\n").splitlines()
FRAMES = ["\n".join(row[-2 * f:] + row[:-2 * f] if f else row for row in rows) for f in range(8)]
FRAMETIME = 4
