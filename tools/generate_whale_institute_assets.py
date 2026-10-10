#!/usr/bin/env python3
"""The Whalestoe institute (0.4.66): twelve numbered pigeonholes and the self-addressed envelope.

Native 16x16 pixel art: each pigeonhole is a spruce cubby with a brass plate carrying its own number, so the box a
reader needs can be read on the wall. The envelope is cream paper with its flap, a stamp and a red seal.
Run from the repository root:
    python3 tools/generate_whale_institute_assets.py
"""
from pathlib import Path
import json
from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
A = ROOT / 'src/main/resources/assets/the_oldest_house'

DIGITS = {
    '0': ('111', '101', '101', '101', '111'),
    '1': ('010', '110', '010', '010', '111'),
    '2': ('111', '001', '111', '100', '111'),
    '3': ('111', '001', '011', '001', '111'),
    '4': ('101', '101', '111', '001', '001'),
    '5': ('111', '100', '111', '001', '111'),
    '6': ('111', '100', '111', '101', '111'),
    '7': ('111', '001', '010', '010', '010'),
    '8': ('111', '101', '111', '101', '111'),
    '9': ('111', '101', '111', '001', '111'),
}


def save(path, image):
    out = A / path
    out.parent.mkdir(parents=True, exist_ok=True)
    image.save(out)


def write_json(path, value):
    out = A / path
    out.parent.mkdir(parents=True, exist_ok=True)
    out.write_text(json.dumps(value, indent=2) + '\n')


def pigeonhole(n):
    """A spruce cubby, dark inside, with a worn brass plate and its number below the opening."""
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 255))
    for x in range(16):
        for y in range(16):
            grain = ((x * 7 + y * 3) % 5) - 2 + (3 if y in (5, 11) else 0)
            im.putpixel((x, y), (112 + grain * 3, 82 + grain * 2, 50 + grain, 255))
    for i in range(16):
        for p in ((i, 0), (i, 15), (0, i), (15, i)):
            im.putpixel(p, (74, 52, 30, 255))
    # The opening, shadowed deepest at the back and top, with a pale lip where hands have worn it.
    for x in range(2, 14):
        for y in range(2, 8):
            depth = 14 + (y - 2) * 2 + (2 if x in (2, 13) else 0)
            im.putpixel((x, y), (depth + 8, depth + 3, depth, 255))
    for x in range(2, 14):
        im.putpixel((x, 8), (150, 116, 74, 255))
    # The plate: brass, darker rim, a highlight under its top edge; the number is inked between the rims.
    for x in range(3, 13):
        for y in range(9, 16):
            rim = x in (3, 12) or y in (9, 15)
            im.putpixel((x, y), (138, 104, 46, 255) if rim else (198, 162, 86, 255))
    text = str(n)
    width = len(text) * 3 + (len(text) - 1)
    left = 8 - (width + 1) // 2
    for i, ch in enumerate(text):
        for row, bits in enumerate(DIGITS[ch]):
            for col, bit in enumerate(bits):
                if bit == '1':
                    im.putpixel((left + i * 4 + col, 10 + row), (46, 32, 18, 255))
    save(f'textures/block/pigeonhole_{n}.png', im)
    write_json(f'models/block/pigeonhole_{n}.json', {
        'parent': 'minecraft:block/orientable',
        'textures': {
            'front': f'the_oldest_house:block/pigeonhole_{n}',
            'side': 'minecraft:block/spruce_planks',
            'top': 'minecraft:block/spruce_planks',
        },
    })


def envelope():
    """A cream envelope, flap down, a stamp in its corner and a red wax seal at the point of the flap."""
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    edge = (150, 132, 100, 255)
    for x in range(1, 15):
        for y in range(3, 13):
            shade = 236 - (y - 3) * 2
            im.putpixel((x, y), (shade, shade - 8, shade - 28, 255))
    for x in range(1, 15):
        im.putpixel((x, 3), edge)
        im.putpixel((x, 12), (128, 112, 84, 255))
    for y in range(3, 13):
        im.putpixel((1, y), edge)
        im.putpixel((14, y), (128, 112, 84, 255))
    # The flap: two folds meeting below the middle.
    for i in range(6):
        im.putpixel((2 + i, 4 + min(i, 4)), (176, 158, 122, 255))
        im.putpixel((13 - i, 4 + min(i, 4)), (176, 158, 122, 255))
    # The stamp, upper right, with a perforated edge.
    for x in range(10, 14):
        for y in range(4, 7):
            im.putpixel((x, y), (92, 120, 168, 255) if (x + y) % 3 else (232, 226, 210, 255))
    # The seal, red wax with a darker rim and a pale glint.
    for x, y in ((7, 8), (8, 8), (7, 9), (8, 9), (6, 9), (9, 9), (7, 10), (8, 10)):
        im.putpixel((x, y), (168, 28, 30, 255))
    for x, y in ((6, 8), (9, 8), (6, 10), (9, 10)):
        im.putpixel((x, y), (112, 16, 20, 255))
    im.putpixel((7, 8), (220, 92, 86, 255))
    save('textures/item/self_addressed_envelope.png', im)
    write_json('models/item/self_addressed_envelope.json', {
        'parent': 'minecraft:item/generated',
        'textures': {'layer0': 'the_oldest_house:item/self_addressed_envelope'},
    })


def blockstate():
    turn = {'north': 0, 'east': 90, 'south': 180, 'west': 270}
    variants = {}
    for facing, y in turn.items():
        for n in range(1, 13):
            entry = {'model': f'the_oldest_house:block/pigeonhole_{n}'}
            if y:
                entry['y'] = y
            variants[f'facing={facing},number={n}'] = entry
    write_json('blockstates/pigeonhole.json', {'variants': variants})


def lang():
    path = A / 'lang/en_us.json'
    entries = json.loads(path.read_text())
    entries.update({'item.the_oldest_house.self_addressed_envelope': 'Self-addressed envelope',
                    'block.the_oldest_house.pigeonhole': 'Pigeonhole'})
    path.write_text(json.dumps(entries, indent=2, ensure_ascii=False) + '\n')


if __name__ == '__main__':
    for number in range(1, 13):
        pigeonhole(number)
    envelope()
    blockstate()
    lang()
