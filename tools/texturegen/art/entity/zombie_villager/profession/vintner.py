"""A zombie Vintner wears the same beret and apron (the zombie villager model uses the villager's UV layout)."""
from pathlib import Path

from texturegen.core import load_module

villager = load_module(Path(__file__).resolve().parents[2] / "villager" / "profession" / "vintner.py")
build_image = villager.build_image

COMPARE = ["entity/zombie_villager/profession/farmer"]
