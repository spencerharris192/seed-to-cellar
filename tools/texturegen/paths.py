"""Shared filesystem locations."""
from pathlib import Path

TOOLS = Path(__file__).resolve().parent.parent
PROJECT = TOOLS.parent
ART = TOOLS / "texturegen" / "art"
OUT = TOOLS / "out"
VANILLA = TOOLS / "vanilla"  # extracted reference textures; git-ignored, never shipped
MOD_RESOURCES = PROJECT / "src" / "main" / "resources"
MOD_TEXTURES = MOD_RESOURCES / "assets" / "seedtocellar" / "textures"
GRADLE_CLIENT_JAR = Path.home() / ".gradle" / "caches" / "forge_gradle" / "minecraft_repo" / "versions" / "1.20.1" / "client.jar"
