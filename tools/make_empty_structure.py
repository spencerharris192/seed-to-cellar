"""Writes the empty air structures GameTests use as templates (the output is committed):

- data/seedtocellar/structures/empty.nbt: 3x3x3, for tests that don't need room.
- data/seedtocellar/structures/empty_tree.nbt: 7x10x7, room to grow a fruit tree from a sapling.
- data/seedtocellar/structures/empty_plot.nbt: 11x5x11, room to place a village building (the Vineyard).
- data/seedtocellar/structures/empty_house.nbt: 11x16x13, room for the Brewhouse (cellar and roof included).

    .venv/Scripts/python make_empty_structure.py
"""
import gzip
import struct
from pathlib import Path

DATA_VERSION = 3465  # Minecraft 1.20.1
STRUCTURES = Path(__file__).resolve().parent.parent / "src/main/resources/data/seedtocellar/structures"
SIZES = {"empty": (3, 3, 3), "empty_tree": (7, 10, 7), "empty_plot": (11, 5, 11), "empty_house": (11, 16, 13)}

TAG_END, TAG_INT, TAG_STRING, TAG_LIST, TAG_COMPOUND = 0, 3, 8, 9, 10


def name(n: str) -> bytes:
    raw = n.encode("utf-8")
    return struct.pack(">H", len(raw)) + raw


def tag_int(n: str, v: int) -> bytes:
    return bytes([TAG_INT]) + name(n) + struct.pack(">i", v)


def tag_string(n: str, v: str) -> bytes:
    return bytes([TAG_STRING]) + name(n) + name(v)


def tag_list(n: str, element_type: int, payloads: list[bytes]) -> bytes:
    return bytes([TAG_LIST]) + name(n) + bytes([element_type]) + struct.pack(">i", len(payloads)) + b"".join(payloads)


def compound_payload(*children: bytes) -> bytes:
    return b"".join(children) + bytes([TAG_END])


STRUCTURES.mkdir(parents=True, exist_ok=True)
for file, (x, y, z) in SIZES.items():
    root = bytes([TAG_COMPOUND]) + name("") + compound_payload(
        tag_int("DataVersion", DATA_VERSION),
        tag_list("size", TAG_INT, [struct.pack(">i", x), struct.pack(">i", y), struct.pack(">i", z)]),
        tag_list("palette", TAG_COMPOUND, [compound_payload(tag_string("Name", "minecraft:air"))]),
        tag_list("blocks", TAG_COMPOUND, []),
        tag_list("entities", TAG_COMPOUND, []),
    )
    out = STRUCTURES / f"{file}.nbt"
    out.write_bytes(gzip.compress(root, mtime=0))
    print(f"Wrote {out}")
