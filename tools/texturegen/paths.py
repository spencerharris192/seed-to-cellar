"""Shared filesystem locations."""
from pathlib import Path

TOOLS = Path(__file__).resolve().parent.parent
PROJECT = TOOLS.parent
ART = TOOLS / "texturegen" / "art"
OUT = TOOLS / "out"
VANILLA = TOOLS / "vanilla"  # extracted reference textures; git-ignored, never shipped
MOD_RESOURCES = PROJECT / "src" / "main" / "resources"
MOD_TEXTURES = MOD_RESOURCES / "assets" / "seedtocellar" / "textures"
# The game jar ModDevGradle sets up (26.3); `gradlew build` makes it
GRADLE_CLIENT_JAR = next(iter(sorted((PROJECT / "build" / "moddev" / "artifacts").glob("minecraft-patched-*-merged.jar"))),
                         PROJECT / "build" / "moddev" / "artifacts" / "minecraft-patched-merged.jar")
