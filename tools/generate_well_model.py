#!/usr/bin/env python3
"""Code-native initials: exact vanilla mossy stone and one-pixel chisel strokes.

The block retains the surrounding stone's UV scale and full cube collision.
No generated bitmap or font face is stretched across its surfaces.
"""
import json
from pathlib import Path

ASSET = Path(__file__).resolve().parents[1] / 'src/main/resources/assets/the_oldest_house'
GLYPHS = {'K': ['101', '110', '100', '110', '101'],
          'G': ['111', '100', '101', '101', '111'],
          'D': ['110', '101', '101', '101', '110']}


def write_model(asset=ASSET):
    faces = {face: {'texture': '#stone', 'cullface': face}
             for face in ('north', 'south', 'east', 'west', 'up', 'down')}
    elements = [{'from': [0, 0, 0], 'to': [16, 16, 16], 'faces': faces}]
    for letters, top in [('KG', 13), ('DG', 6)]:
        for letter, left in zip(letters, (3, 9)):
            for row, bits in enumerate(GLYPHS[letter]):
                for col, bit in enumerate(bits):
                    if bit == '0':
                        continue
                    x, y = left + col, top - row
                    elements.append({'from': [x, y, -.008], 'to': [x + .85, y + .85, -.008],
                                     'shade': True, 'faces': {'north': {'texture': '#cut', 'uv': [4, 4, 8, 8]}}})
            # The period is smaller than the initials; its pixel-grid position stays legible.
            elements.append({'from': [left + 3.25, top - 4, -.008], 'to': [left + 3.8, top - 3.45, -.008],
                             'faces': {'north': {'texture': '#cut', 'uv': [4, 4, 8, 8]}}})
    model = {'parent': 'minecraft:block/block', 'ambientocclusion': True,
             'textures': {'stone': 'minecraft:block/mossy_cobblestone',
                          'cut': 'minecraft:block/blackstone', 'particle': 'minecraft:block/mossy_cobblestone'},
             'elements': elements}
    (asset / 'models/block/well_carvings.json').write_text(json.dumps(model, indent=2) + '\n')


if __name__ == '__main__':
    write_model()
