"""Helper (not a texture): the flower and fruit layers drawn over a fruit tree's leaves.

Each tree's leaves have three extra looks over the (biome-tinted) leaf texture: blossom, unripe fruit,
ripe fruit. They're transparent except for the flowers or fruit, which sit at the same few spots on
every face, spread out so a tree reads as dotted with fruit rather than covered in it.

Letters every tree file shares: p q y (petal, petal shadow, golden center), u v (unripe fruit light,
dark), w (stalk). Ripe fruit sprites use each tree's own letters.
"""
from texturegen.compose import blank, stamp, to_grid

# Where the flowers and fruit sit (top-left of each sprite), as on a real branch: not in a grid.
SPOTS = [(2, 1), (10, 2), (6, 7), (12, 9), (1, 11), (8, 12)]

BLOSSOM = """
.p.
pyq
.q.
"""

UNRIPE = """
.w
uv
vv
"""


def layer(sprite: str, spots=SPOTS) -> str:
    c = blank()
    for x, y in spots:
        stamp(c, sprite, x, y)
    return to_grid(c)


def textures(tree: str, ripe: str, unripe: str = UNRIPE, blossom: str = BLOSSOM) -> dict[str, str]:
    return {
        f"{tree}_leaves_blossom": layer(blossom),
        f"{tree}_leaves_unripe": layer(unripe),
        f"{tree}_leaves_ripe": layer(ripe),
    }
