"""A small toolkit for writing village building templates (structure NBT) from Python.

A Plot is a box of blocks addressed (x, y, z) from its lower north-west corner; anything left unset is structure
void (the world's own terrain stays). Fences, walls and glass panes are placed as placeholders and joined to their
neighbours when the plot is written, the way the game would join them (it won't recompute them for a building).
Block entities (chests, barrels) can carry NBT such as a loot table. One jigsaw joins the building to the village
street: it sits at ground level on the plot's front edge, facing out, and turns into `final_state` when placed.

Used by make_vineyard.py and make_brewhouse.py; the NBT they write is committed.
"""
import gzip
import struct
from pathlib import Path

DATA_VERSION = 3465  # Minecraft 1.20.1
STRUCTURES = Path(__file__).resolve().parent.parent / "src/main/resources/data/seedtocellar/structure"

TAG_END, TAG_BYTE, TAG_INT, TAG_LONG, TAG_STRING, TAG_LIST, TAG_COMPOUND = 0, 1, 3, 4, 8, 9, 10

# Blocks a fence, wall or pane joins on any side (solid, full-faced building blocks).
SOLID_WORDS = ("planks", "_log", "_wood", "cobblestone", "stone_bricks", "bricks", "stone", "terracotta", "mud_bricks",
               "packed_mud", "deepslate")
NOT_SOLID_WORDS = ("slab", "stairs", "fence", "wall", "pane", "door", "button", "pressure_plate", "trapdoor", "sign")


def state(block: str, **props) -> tuple:
    return block, tuple(sorted((k, str(v).lower()) for k, v in props.items()))


def is_solid(s) -> bool:
    name = s[0]
    return not any(w in name for w in NOT_SOLID_WORDS) and any(w in name for w in SOLID_WORDS)


class Plot:
    def __init__(self, sx: int, sy: int, sz: int):
        self.size = (sx, sy, sz)
        self.blocks: dict[tuple[int, int, int], tuple] = {}
        self.nbt: dict[tuple[int, int, int], bytes] = {}
        self.jigsaw = None

    def set(self, x, y, z, block: str, **props):
        self.blocks[(x, y, z)] = state(block, **props)
        self.nbt.pop((x, y, z), None)

    def fill(self, x0, y0, z0, x1, y1, z1, block: str, **props):
        for x in range(min(x0, x1), max(x0, x1) + 1):
            for y in range(min(y0, y1), max(y0, y1) + 1):
                for z in range(min(z0, z1), max(z0, z1) + 1):
                    self.set(x, y, z, block, **props)

    def get(self, x, y, z):
        return self.blocks.get((x, y, z))

    # connecting blocks, joined in write()
    def fence(self, x, y, z, wood: str):
        self.blocks[(x, y, z)] = ("~fence", f"minecraft:{wood}_fence")

    def pane(self, x, y, z, block: str = "minecraft:glass_pane"):
        self.blocks[(x, y, z)] = ("~pane", block)

    def gate(self, x, y, z, wood: str, facing: str):
        self.set(x, y, z, f"minecraft:{wood}_fence_gate", facing=facing, in_wall=False, open=False, powered=False)

    def door(self, x, y, z, block: str, facing: str, hinge: str = "left"):
        for half, dy in (("lower", 0), ("upper", 1)):
            self.set(x, y + dy, z, block, facing=facing, half=half, hinge=hinge, open=False, powered=False)

    def stairs(self, x, y, z, block: str, facing: str, half: str = "bottom", shape: str = "straight"):
        self.set(x, y, z, block, facing=facing, half=half, shape=shape, waterlogged=False)

    def loot(self, x, y, z, block: str, table: str, **props):
        """A chest or barrel that fills from a loot table the first time it's opened."""
        self.set(x, y, z, block, **props)
        entity = block.split(":")[1]
        self.nbt[(x, y, z)] = compound_payload(tag_string("id", f"minecraft:{entity}"), tag_string("LootTable", table))

    def block_entity(self, x, y, z, block: str, nbt: bytes, **props):
        """A block whose block entity starts from the given NBT (a compound payload built with the tag_* helpers)."""
        self.set(x, y, z, block, **props)
        self.nbt[(x, y, z)] = nbt

    def entrance(self, x, y, z, orientation: str, final_state: str):
        """The street join: name/target minecraft:building_entrance, as vanilla's village houses use."""
        self.jigsaw = (x, y, z, orientation, final_state)

    # --- writing ---------------------------------------------------------------------------

    def _joined(self) -> dict:
        out = dict(self.blocks)
        dirs = {"north": (0, -1), "south": (0, 1), "east": (1, 0), "west": (-1, 0)}
        for (x, y, z), b in self.blocks.items():
            if b[0] not in ("~fence", "~pane"):
                continue
            props = {}
            for name, (dx, dz) in dirs.items():
                n = self.blocks.get((x + dx, y, z + dz))
                joins = False
                if n is not None:
                    if n[0] == b[0] and (b[0] == "~pane" or n[1] == b[1]):
                        joins = True                                    # the same fence or any pane
                    elif b[0] == "~fence" and n[0].endswith("_fence_gate"):
                        facing = dict(n[1]).get("facing")
                        joins = (facing in ("north", "south")) == (dz == 0)   # a gate joins along its hinge line
                    elif n[0][0] != "~" and is_solid(n):
                        joins = True
                props[name] = joins
            out[(x, y, z)] = state(b[1], waterlogged=False, **props)
        return out

    def part(self, y0: int, y1: int) -> "Plot":
        """Layers y0..y1 as a plot of their own, moved down to start at 0 (the jigsaw comes too if it's in them)."""
        out = Plot(self.size[0], y1 - y0 + 1, self.size[2])
        for (x, y, z), b in self.blocks.items():
            if y0 <= y <= y1:
                out.blocks[(x, y - y0, z)] = b
                if (x, y, z) in self.nbt:
                    out.nbt[(x, y - y0, z)] = self.nbt[(x, y, z)]
        if self.jigsaw and y0 <= self.jigsaw[1] <= y1:
            jx, jy, jz, o, f = self.jigsaw
            out.jigsaw = (jx, jy - y0, jz, o, f)
        return out

    def write(self, rel: str, entrance: bool = True, folder: Path = STRUCTURES) -> None:
        """Writes the plot; a village building needs its street entrance (a part built under one, like a cellar, doesn't).
        `folder` is where templates go: the mod's structures, or (make_showcase.py) a world's generated folder."""
        assert not entrance or self.jigsaw is not None, "a village building needs its street entrance"
        blocks = self._joined()
        palette = sorted(set(blocks.values()))
        if self.jigsaw:
            jx, jy, jz, orientation, final_state = self.jigsaw
            palette.append(state("minecraft:jigsaw", orientation=orientation))
        else:
            jx = jy = jz = None
        index = {p: i for i, p in enumerate(palette)}

        def palette_entry(p) -> bytes:
            block, props = p
            parts = [tag_string("Name", block)]
            if props:
                parts.append(tag_compound("Properties", compound_payload(*(tag_string(k, v) for k, v in props))))
            return compound_payload(*parts)

        def pos(x, y, z) -> bytes:
            return tag_list("pos", TAG_INT, [struct.pack(">i", v) for v in (x, y, z)])

        entries = []
        sx, sy, sz = self.size
        for (x, y, z), block in sorted(blocks.items()):
            assert 0 <= x < sx and 0 <= y < sy and 0 <= z < sz, f"{rel}: block outside the plot at {(x, y, z)}"
            if (x, y, z) == (jx, jy, jz):
                continue
            parts = [pos(x, y, z), tag_int("state", index[block])]
            if (x, y, z) in self.nbt:
                parts.append(tag_compound("nbt", self.nbt[(x, y, z)]))
            entries.append(compound_payload(*parts))
        if self.jigsaw:
            jigsaw = compound_payload(
                tag_string("id", "minecraft:jigsaw"), tag_string("name", "minecraft:building_entrance"),
                tag_string("target", "minecraft:building_entrance"), tag_string("pool", "minecraft:empty"),
                tag_string("joint", "aligned"), tag_string("final_state", final_state))
            entries.append(compound_payload(pos(jx, jy, jz), tag_int("state", len(palette) - 1), tag_compound("nbt", jigsaw)))

        root = bytes([TAG_COMPOUND]) + name("") + compound_payload(
            tag_int("DataVersion", DATA_VERSION),
            tag_list("size", TAG_INT, [struct.pack(">i", v) for v in self.size]),
            tag_list("palette", TAG_COMPOUND, [palette_entry(p) for p in palette]),
            tag_list("blocks", TAG_COMPOUND, entries),
            tag_list("entities", TAG_COMPOUND, []),
        )
        path = folder / f"{rel}.nbt"
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_bytes(gzip.compress(root, mtime=0))
        print(f"Wrote {path}")


def name(n: str) -> bytes:
    raw = n.encode("utf-8")
    return struct.pack(">H", len(raw)) + raw


def tag_byte(n: str, v: int) -> bytes:
    return bytes([TAG_BYTE]) + name(n) + struct.pack(">b", v)


def tag_int(n: str, v: int) -> bytes:
    return bytes([TAG_INT]) + name(n) + struct.pack(">i", v)


def tag_long(n: str, v: int) -> bytes:
    return bytes([TAG_LONG]) + name(n) + struct.pack(">q", v)


def tag_string(n: str, v: str) -> bytes:
    return bytes([TAG_STRING]) + name(n) + name(v)


def tag_list(n: str, element_type: int, payloads: list[bytes]) -> bytes:
    return bytes([TAG_LIST]) + name(n) + bytes([element_type]) + struct.pack(">i", len(payloads)) + b"".join(payloads)


def tag_compound(n: str, payload: bytes) -> bytes:
    return bytes([TAG_COMPOUND]) + name(n) + payload


def compound_payload(*children: bytes) -> bytes:
    return b"".join(children) + bytes([TAG_END])
