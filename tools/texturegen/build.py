"""Write every art file as a PNG (plus .mcmeta for animations) into the mod's textures folder.

    python -m texturegen.build           # write changed textures
    python -m texturegen.build --check   # exit 1 if any PNG is missing or stale
"""
import sys

from PIL import Image

from .core import ArtError, iter_art, mcmeta, render_module
from .lint import legend_clashes
from .paths import ART, MOD_RESOURCES, MOD_TEXTURES


def main(argv: list[str]) -> int:
    check_only = "--check" in argv
    stale, written, total = [], [], 0
    clashes = [c for path in sorted(ART.rglob("*.py")) for c in legend_clashes(path)]
    if clashes:
        print("ART ERROR:", *clashes, sep="\n  ")
        return 1
    try:
        for rel, module in iter_art():
            total += 1
            img = render_module(rel, module)
            # DEST: a file outside the textures folder, relative to the mod's resources (the mod list's logo.png)
            dest = MOD_RESOURCES / module.DEST if hasattr(module, "DEST") else MOD_TEXTURES / f"{rel}.png"
            meta = mcmeta(module)
            meta_dest = dest.with_suffix(".png.mcmeta")
            current = Image.open(dest).convert("RGBA") if dest.exists() else None
            png_ok = current is not None and current.size == img.size and current.tobytes() == img.tobytes()
            meta_ok = meta is None or (meta_dest.exists() and meta_dest.read_text() == meta)
            if png_ok and meta_ok:
                continue
            if check_only:
                stale.append(rel)
                continue
            dest.parent.mkdir(parents=True, exist_ok=True)
            img.save(dest)
            if meta is not None:
                meta_dest.write_text(meta)
            written.append(rel)
    except ArtError as e:
        print(f"ART ERROR: {e}")
        return 1

    if check_only:
        if stale:
            print("Stale or missing textures (run texturegen.build):", ", ".join(stale))
            return 1
        print(f"All {total} textures up to date.")
        return 0
    print(f"{total} art files, {len(written)} written: {', '.join(written) or 'none changed'}")
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
