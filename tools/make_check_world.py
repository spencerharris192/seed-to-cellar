"""Copies the GameTest server's world into run/saves/automated_check for the automated client check, with experimental
features switched off and the "experimental world" warning marked as seen (the game would otherwise stop at a
confirmation screen before opening it):

    .venv/Scripts/python make_check_world.py

Then: gradlew runClient "-PquickPlay=automated_check" (see CLAUDE.md). Run runGameTestServer once first, so the world exists.
"""
import gzip
import shutil
import struct
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
SOURCE = ROOT / "run-gametest/gametestserver/gametestworld"
TARGET = ROOT / "run/saves/automated_check"
# vanilla's experiment packs (Create World > Experiments); every other pack (vanilla, mods' data) stays on
EXPERIMENTS = {"trade_rebalance", "redstone_experiments", "minecart_improvements"}

END, BYTE, SHORT, INT, LONG, FLOAT, DOUBLE, BYTE_ARRAY, STRING, LIST, COMPOUND, INT_ARRAY, LONG_ARRAY = range(13)
FIXED = {BYTE: ">b", SHORT: ">h", INT: ">i", LONG: ">q", FLOAT: ">f", DOUBLE: ">d"}


class Reader:
    def __init__(self, data: bytes):
        self.data, self.at = data, 0

    def take(self, fmt: str):
        value = struct.unpack_from(fmt, self.data, self.at)
        self.at += struct.calcsize(fmt)
        return value[0]

    def string(self) -> str:
        n = self.take(">H")
        s = self.data[self.at:self.at + n].decode("utf-8")
        self.at += n
        return s

    def payload(self, kind: int):
        if kind in FIXED:
            return self.take(FIXED[kind])
        if kind == STRING:
            return self.string()
        if kind in (BYTE_ARRAY, INT_ARRAY, LONG_ARRAY):
            n = self.take(">i")
            fmt = {BYTE_ARRAY: ">b", INT_ARRAY: ">i", LONG_ARRAY: ">q"}[kind]
            return [self.take(fmt) for _ in range(n)]
        if kind == LIST:
            element = self.take(">b")
            return (element, [self.payload(element) for _ in range(self.take(">i"))])
        if kind == COMPOUND:
            out = {}
            while (child := self.take(">b")) != END:
                name = self.string()
                out[name] = (child, self.payload(child))
            return out
        raise ValueError(f"unknown tag {kind}")


def write(kind: int, value) -> bytes:
    if kind in FIXED:
        return struct.pack(FIXED[kind], value)
    if kind == STRING:
        raw = value.encode("utf-8")
        return struct.pack(">H", len(raw)) + raw
    if kind in (BYTE_ARRAY, INT_ARRAY, LONG_ARRAY):
        fmt = {BYTE_ARRAY: ">b", INT_ARRAY: ">i", LONG_ARRAY: ">q"}[kind]
        return struct.pack(">i", len(value)) + b"".join(struct.pack(fmt, v) for v in value)
    if kind == LIST:
        element, items = value
        return struct.pack(">bi", element if items else END, len(items)) + b"".join(write(element, v) for v in items)
    if kind == COMPOUND:
        return b"".join(struct.pack(">b", k) + write(STRING, name) + write(k, v) for name, (k, v) in value.items()) + bytes([END])
    raise ValueError(f"unknown tag {kind}")


def main():
    if not SOURCE.exists():
        raise SystemExit(f"{SOURCE} is missing: run gradlew runGameTestServer once first")
    shutil.rmtree(TARGET, ignore_errors=True)
    shutil.copytree(SOURCE, TARGET, ignore=shutil.ignore_patterns("session.lock"))
    level = TARGET / "level.dat"
    reader = Reader(gzip.decompress(level.read_bytes()))
    kind, name = reader.take(">b"), reader.string()
    root = reader.payload(kind)
    data = root["Data"][1]
    data["enabled_features"] = (LIST, (STRING, ["minecraft:vanilla"]))
    data["confirmedExperimentalSettings"] = (BYTE, 1)   # the test world's void generator would ask for a backup first
    packs = data["DataPacks"][1]
    enabled = packs["Enabled"][1][1]
    experimental = [p for p in enabled if p in EXPERIMENTS]
    packs["Enabled"] = (LIST, (STRING, [p for p in enabled if p not in experimental]))
    packs["Disabled"] = (LIST, (STRING, [p for p in packs["Disabled"][1][1] if p not in experimental] + experimental))
    level.write_bytes(gzip.compress(struct.pack(">b", kind) + write(STRING, name) + write(kind, root)))
    print(f"Wrote {TARGET} (experimental packs off: {', '.join(experimental) or 'none'})")


if __name__ == "__main__":
    main()
