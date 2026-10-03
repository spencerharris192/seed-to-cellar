"""Static checks on art files that rendering alone can't catch.

Legend letters must be unique. LEGEND is usually built by merging several ramps, e.g.
{**dict(zip("ijklmn", IRON)), **dict(zip("ghjk", GLASS))}; if two ramps share a letter the later
one silently wins, and pixels meant as iron come out as glass. This finds such clashes by
reading the LEGEND expression itself.
"""
import ast
from pathlib import Path


def _keys(value: ast.AST) -> list[str]:
    """Letters defined by one ** part of a LEGEND: dict(zip("abc", X)), {ch: .. for ch in "abc"},
    {ch: .. for i, ch in enumerate("abc")} or {str(i): .. for i in range(n)}."""
    for node in ast.walk(value):
        if isinstance(node, ast.Call) and getattr(node.func, "id", "") == "zip" \
                and node.args and isinstance(node.args[0], ast.Constant):
            return list(node.args[0].value)
        if isinstance(node, ast.comprehension):
            it = node.iter
            if isinstance(it, ast.Constant):
                return list(it.value)
            name = getattr(getattr(it, "func", None), "id", "")
            if name == "enumerate" and isinstance(it.args[0], ast.Constant):
                return list(it.args[0].value)
            if name == "range":
                return [str(i) for i in range(ast.literal_eval(it.args[0]))]
    return []


def legend_clashes(path: Path) -> list[str]:
    """Human-readable descriptions of letters defined more than once in the file's LEGEND."""
    source = path.read_text(encoding="utf-8")
    problems = []
    for node in ast.walk(ast.parse(source)):
        if not (isinstance(node, ast.Assign) and isinstance(node.value, ast.Dict)
                and any(isinstance(t, ast.Name) and t.id == "LEGEND" for t in node.targets)):
            continue
        seen: dict[str, str] = {}
        for key, value in zip(node.value.keys, node.value.values):
            if key is None:
                letters, label = _keys(value), ast.get_source_segment(source, value)
            else:
                letters, label = [ast.literal_eval(key)], repr(ast.literal_eval(key))
            for letter in letters:
                if letter in seen:
                    problems.append(f"{path.name}: LEGEND letter '{letter}' is defined by both {seen[letter]} and {label}")
                seen[letter] = label
    return problems
