"""Extract vanilla textures from the Minecraft jar in the Gradle cache.

For LOCAL visual comparison in contact sheets only. The output folder is
git-ignored; these files must never be copied, traced, committed, or shipped.
"""
import sys
import zipfile

from .paths import GRADLE_CLIENT_JAR, VANILLA

PREFIX = "assets/minecraft/textures/"


def main() -> int:
    if not GRADLE_CLIENT_JAR.exists():
        print(f"Minecraft jar not found at {GRADLE_CLIENT_JAR}. Run a Gradle build first.")
        return 1
    count = 0
    with zipfile.ZipFile(GRADLE_CLIENT_JAR) as jar:
        for name in jar.namelist():
            if name.startswith(PREFIX) and (name.endswith(".png") or name.endswith(".mcmeta")):
                target = VANILLA / name[len(PREFIX):]
                target.parent.mkdir(parents=True, exist_ok=True)
                target.write_bytes(jar.read(name))
                count += 1
    print(f"Extracted {count} vanilla texture files to {VANILLA} (local reference only)")
    return 0


if __name__ == "__main__":
    sys.exit(main())
