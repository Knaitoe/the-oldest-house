#!/usr/bin/env python3
"""0.4.758 (docs/BUILDING_OUTSIDE_0_4_758.md): item models for the stories' pieces.

Every block listed by the_oldest_house/story_pieces.json (by prefix, less its skips) gets models/item/<block>.json pointing at the
shared item/story_piece model, which draws the block a stack places (client.PieceClient). Blocks that already have an item of their own
are left alone by the mod and need no model here; this tool only writes models that are missing. With --check it writes nothing and
fails if any is missing or differs.
"""
import json, sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
RES = ROOT / 'src/main/resources'
ASSETS = RES / 'assets/the_oldest_house'
SHARED = {
    "parent": "minecraft:builtin/entity",
    "gui_light": "side",
    "display": {
        "gui": {"rotation": [30, 225, 0], "translation": [0, 0, 0], "scale": [0.625, 0.625, 0.625]},
        "ground": {"rotation": [0, 0, 0], "translation": [0, 3, 0], "scale": [0.25, 0.25, 0.25]},
        "fixed": {"rotation": [0, 0, 0], "translation": [0, 0, 0], "scale": [0.5, 0.5, 0.5]},
        "thirdperson_righthand": {"rotation": [75, 45, 0], "translation": [0, 2.5, 0], "scale": [0.375, 0.375, 0.375]},
        "firstperson_righthand": {"rotation": [0, 45, 0], "translation": [0, 0, 0], "scale": [0.40, 0.40, 0.40]},
        "firstperson_lefthand": {"rotation": [0, 225, 0], "translation": [0, 0, 0], "scale": [0.40, 0.40, 0.40]},
    },
}
POINTER = {"parent": "the_oldest_house:item/story_piece"}


def text(obj):
    return json.dumps(obj, indent=2) + '\n'


def pieces():
    manifest = json.loads((RES / 'the_oldest_house/story_pieces.json').read_text())
    blocks = sorted(p.stem for p in (ASSETS / 'blockstates').glob('*.json'))
    out = []
    for story in manifest['stories']:
        for block in blocks:
            if any(block.startswith(p) for p in story['prefixes']) and block not in story['skip']:
                out.append(block)
    return sorted(set(out))


def main():
    check = '--check' in sys.argv
    wanted = {ASSETS / 'models/item/story_piece.json': text(SHARED)}
    for block in pieces():
        path = ASSETS / f'models/item/{block}.json'
        if path.exists() and json.loads(path.read_text()) != POINTER:
            continue  # an item of the block's own; the mod gives it no piece item
        wanted[path] = text(POINTER)
    bad = [p for p, t in wanted.items() if not p.exists() or p.read_text() != t]
    if check:
        if bad:
            print('story piece item models missing or stale:', *[p.relative_to(ROOT) for p in bad], sep='\n  ')
            sys.exit(1)
        print(f'{len(wanted) - 1} story piece item models current.')
        return
    for p in bad:
        p.write_text(wanted[p])
    print(f'wrote {len(bad)} of {len(wanted)} story piece item models.')


if __name__ == '__main__':
    main()
