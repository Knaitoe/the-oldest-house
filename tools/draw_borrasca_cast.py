#!/usr/bin/env python3
"""Drisking cast entry point. Uses the shared character painter and native 64x64 UV layout.

Outfits, face palettes, grouped pixels and integer export come from the same painter
as the established cast. The original machine atlas is separate and unchanged.
"""
import argparse
from pathlib import Path
from PIL import Image
from paint_character_skins import LOOKS, paint

ROOT = Path(__file__).resolve().parents[1] / 'src/main/resources/assets/the_oldest_house/textures/entity'
PEOPLE = ('kyle', 'kimber', 'sheriff', 'prescott', 'worker')


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--check', action='store_true')
    args = parser.parse_args()
    for person in PEOPLE:
        name = 'borrasca_' + person
        look = LOOKS[name]
        assert look.logical == 64, 'The Drisking production mesh uses standard humanoid UVs'
        painting = paint(look)
        path = ROOT / (name + '.png')
        if args.check:
            with Image.open(path) as stored:
                assert stored.size == painting.size and stored.convert('RGBA').tobytes() == painting.tobytes(), name
        else:
            painting.save(path, optimize=True)
    print(('Verified' if args.check else 'Painted') + ' 5 Drisking skins with the shared cast painter.')


if __name__ == '__main__':
    main()
