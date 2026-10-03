"""Checks every page of The Brewer's Almanac fits: wraps its text the way Patchouli does (116 px wide, 9 px lines, vanilla
font widths from ascii.png) and counts lines against the room each page type has."""
import json
import re
import sys
from pathlib import Path

from PIL import Image

ROOT = Path(__file__).resolve().parent.parent
font = Image.open(ROOT / "tools/vanilla/font/ascii.png").convert("RGBA")
cell = font.width // 16
ADV = {}
for code in range(256):
    gx, gy = code % 16 * cell, code // 16 * cell
    right = -1
    for x in range(cell):
        if any(font.getpixel((gx + x, gy + y))[3] > 0 for y in range(cell)):
            right = x
    ADV[chr(code)] = (right + 2) * 8 // cell if right >= 0 else 0
ADV[" "] = 4


def width(s):
    return sum(ADV.get(c, 6) for c in s)


def plain(text):
    """Patchouli markup to plain text with \n for breaks."""
    text = text.replace("$(br2)", "\n\n").replace("$(br)", "\n")
    text = re.sub(r"\$\(l:[^)]*\)", "", text)
    text = re.sub(r"\$\([^)]*\)", "", text)
    return text


def lines(text, room=116):
    out = []
    for para in plain(text).split("\n"):
        line = ""
        for word in para.split(" "):
            trial = word if not line else line + " " + word
            if width(trial) > room and line:
                out.append(line)
                line = word
            else:
                line = trial
        out.append(line)
    return out


ROOM = {"first": 14, "text": 17, "spotlight": 12, "spotlight_titled": 11}
book = ROOT / "src/generated/resources/assets/seedtocellar/patchouli_books/brewers_almanac/en_us/entries"
worst = []
verbose = "-v" in sys.argv
for f in sorted(book.rglob("*.json")):
    entry = json.loads(f.read_text(encoding="utf-8"))
    for i, page in enumerate(entry["pages"]):
        kind = page["type"].split(":")[1]
        text = page.get("text")
        if not text:
            continue
        if kind == "text":
            room = ROOM["first"] if i == 0 else ROOM["text"]
            if "title" in page and i > 0:
                room = 15
        elif kind == "spotlight":
            room = ROOM["spotlight_titled"] if "title" in page else ROOM["spotlight"]
        elif kind == "crafting":
            room = 5
        elif kind == "multiblock":
            room = 4
        else:
            room = 12
        n = lines(text)
        worst.append((len(n) - room, f.relative_to(book).as_posix(), i, len(n), room))
        if verbose:
            print(f"{f.relative_to(book).as_posix()} p{i} {kind}: {len(n)}/{room}")
            for l in n:
                print("   |" + l)
over = [w for w in worst if w[0] > 0]
print(f"{len(worst)} text pages; {len(over)} over")
for w in sorted(over, reverse=True):
    print(f"  OVER by {w[0]}: {w[1]} page {w[2]} ({w[3]} lines, room {w[4]})")
tight = sorted(w for w in worst if -1 <= w[0] <= 0)
print("tight (0-1 spare):", [(w[1], w[2], w[3], w[4]) for w in tight])
