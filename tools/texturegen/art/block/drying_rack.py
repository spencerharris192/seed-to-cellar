"""Drying Rack wood: a smooth oak pole with lengthwise grain and a couple of knots.

The model maps every part (posts, feet, rails) onto this one texture, turning it so the grain
runs along each piece, so it has no top or bottom edge of its own.
"""
from texturegen.palettes import WOOD_OAK

LEGEND = {str(i): WOOD_OAK[i] for i in range(6)}

GRID = """
3433243343324334
3433243343324334
3432243343324334
3433243443324334
3433243343324334
3433213343324334
3433243343324334
3433243343324234
3433243343321234
3433243343324234
3433243343324334
3433243343324334
3443243343324334
3433243343324334
3433243343224334
3433243343324334
"""

COMPARE = ["block/stripped_oak_log", "block/oak_planks", "block/ladder"]
