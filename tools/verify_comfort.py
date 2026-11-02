#!/usr/bin/env python3
"""0.4.271: the reader's comfort settings are read one way (client/Comfort).

Distortion Effects and FOV Effects may be read only by client/Comfort.java, so every cosmetic effect scales with them the same
way and none puts a floor under them. This fails if any other source reads screenEffectScale() or fovEffectScale() directly,
or clamps Comfort.effects() / Comfort.fov() to a minimum.
"""
import pathlib
import re
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent / 'src' / 'main' / 'java'
COMFORT = 'io/github/knaitoe/theoldesthouse/client/Comfort.java'
direct = re.compile(r'\b(screenEffectScale|fovEffectScale)\s*\(')
floor = re.compile(r'Math\.max\s*\(\s*[0-9.]+F?\s*,\s*Comfort\.(effects|fov)\s*\(')
problems = []
for path in sorted(ROOT.rglob('*.java')):
    rel = path.relative_to(ROOT).as_posix()
    text = path.read_text(encoding='utf-8')
    for n, line in enumerate(text.splitlines(), 1):
        if rel != COMFORT and direct.search(line):
            problems.append(f'{rel}:{n}: reads a comfort setting directly; use client/Comfort')
        if floor.search(line):
            problems.append(f'{rel}:{n}: puts a floor under a comfort setting')
if not (ROOT / COMFORT).exists():
    problems.append(f'{COMFORT} is missing')
if problems:
    print('\n'.join(problems))
    sys.exit(1)
print('comfort settings: every read goes through client/Comfort, with no floor')
