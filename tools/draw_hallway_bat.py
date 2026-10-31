#!/usr/bin/env python3
"""The split-feed figure's baseball bat (0.4.201; owner: "the dagger/weapon in the npc's hand should be more apparent").

The found text has him "dragging the baseball bat along the drywall". The old model was a thin stick of stripped oak drawn at
six tenths, which read as a knife. This one is a bat: a knob, a taped grip, a taper and a thick barrel with a burnt-in brand,
held at nearly full size. Its sixteen-pixel sheet is drawn here at vanilla's scale.
"""
import argparse, io, json, random
from pathlib import Path
from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
A = ROOT / 'src/main/resources/assets/the_oldest_house'
TEX = 'the_oldest_house:item/hallway_bat'
TAPE, TAPE_LIGHT = (38, 36, 40), (74, 70, 76)
WOOD, WOOD_DARK, WOOD_LIGHT, BRAND = (206, 176, 124), (176, 144, 96), (226, 202, 156), (92, 58, 34)


def sheet():
    """Columns 0-3 the grip's tape (wound on the bias), 4-15 the wood with its grain; rows 0-3 of 12-15 the end grain."""
    rnd = random.Random(2010)
    im = Image.new('RGBA', (16, 16), WOOD + (255,))
    for y in range(16):
        for x in range(4):
            im.putpixel((x, y), (TAPE_LIGHT if (x + y) % 4 == 0 else TAPE) + (255,))
        for x in range(4, 16):
            r = rnd.randrange(9)
            if x % 3 == 1 and r < 6:
                im.putpixel((x, y), WOOD_DARK + (255,))
            elif r == 0:
                im.putpixel((x, y), WOOD_LIGHT + (255,))
    # The brand on the barrel.
    for x, y in ((9, 6), (10, 6), (11, 6), (8, 7), (12, 7), (9, 8), (10, 8), (11, 8)):
        im.putpixel((x, y), BRAND + (255,))
    # End grain: rings.
    for y in range(12, 16):
        for x in range(12, 16):
            im.putpixel((x, y), (WOOD_DARK if (abs(x - 13.5) + abs(y - 13.5)) % 2 < 1 else WOOD_LIGHT) + (255,))
    return im


def box(fr, to, side_uv, end_uv=(12, 12, 16, 16)):
    faces = {f: {'uv': list(side_uv), 'texture': '#bat'} for f in ('north', 'south', 'east', 'west')}
    faces['up'] = {'uv': list(end_uv), 'texture': '#bat'}
    faces['down'] = {'uv': list(end_uv), 'texture': '#bat'}
    return {'from': fr, 'to': to, 'faces': faces}


def model():
    tape, wood = (0, 0, 4, 16), (4, 0, 16, 16)
    return {
        'parent': 'minecraft:block/block',
        'textures': {'bat': TEX, 'particle': TEX},
        'elements': [
            box([6.5, 0, 6.5], [9.5, 1, 9.5], tape, (0, 0, 3, 3)),        # the knob
            box([7.25, 1, 7.25], [8.75, 8, 8.75], tape, (0, 0, 2, 2)),    # the taped grip
            box([7, 8, 7], [9, 13, 9], wood),                             # the taper
            box([6.25, 13, 6.25], [9.75, 23, 9.75], wood),                # the barrel
            box([6.5, 23, 6.5], [9.5, 23.5, 9.5], wood),                  # the rounded end
        ],
        'display': {
            'gui': {'rotation': [25, 225, 0], 'translation': [0, -1.5, 0], 'scale': [0.62, 0.62, 0.62]},
            'ground': {'rotation': [0, 0, 0], 'translation': [0, 2, 0], 'scale': [0.5, 0.5, 0.5]},
            'fixed': {'rotation': [0, 90, 0], 'translation': [0, -2, 0], 'scale': [0.7, 0.7, 0.7]},
            'firstperson_righthand': {'rotation': [0, 90, 45], 'translation': [0, -1, 0], 'scale': [0.7, 0.7, 0.7]},
            'thirdperson_righthand': {'rotation': [0, 90, 135], 'translation': [0, 2, -1], 'scale': [0.85, 0.85, 0.85]},
        },
    }


def files():
    b = io.BytesIO(); sheet().save(b, format='PNG')
    return {A / 'textures/item/hallway_bat.png': b.getvalue(),
            A / 'models/item/hallway_bat.json': json.dumps(model(), indent=2) + '\n'}


def main():
    p = argparse.ArgumentParser(); p.add_argument('--check', action='store_true'); check = p.parse_args().check
    for path, data in files().items():
        if check:
            assert path.exists(), path
            if isinstance(data, str):
                assert json.loads(path.read_text()) == json.loads(data), path
            else:
                assert Image.open(path).convert('RGBA').tobytes() == Image.open(io.BytesIO(data)).convert('RGBA').tobytes(), path
        else:
            path.parent.mkdir(parents=True, exist_ok=True)
            path.write_text(data) if isinstance(data, str) else path.write_bytes(data)
    print(('Verified' if check else 'Drew') + " the hallway bat: knob, taped grip, barrel and brand, held near full size.")


if __name__ == '__main__':
    main()
