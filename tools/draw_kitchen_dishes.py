"""The kitchen's dishes (0.4.331): what comes off the familiar kitchen's stove when more than one thing went into the pan.

A plate for something fried dry (crisp), a bowl for something cooked in water (soft), a paler bowl for something cooked in milk
(creamy), and the plate and bowl again when it was left on the ring too long. Sixteen pixels, drawn here; the item model picks
the picture by custom model data (0 plate, 1 bowl, 2 creamy bowl, 3 burnt plate, 4 burnt bowl).

Usage: python3 tools/draw_kitchen_dishes.py [--check]
"""
import json
import sys
from pathlib import Path

from PIL import Image

ASSETS = Path(__file__).resolve().parents[1] / 'src/main/resources/assets/the_oldest_house'
NAMES = ('kitchen_dish', 'kitchen_dish_bowl', 'kitchen_dish_creamy', 'kitchen_dish_burnt', 'kitchen_dish_bowl_burnt')

# One character a pixel. '.' is clear.
PLATE = [
    '................',
    '................',
    '................',
    '................',
    '.....aaaaaa.....',
    '...aabbbbbbaa...',
    '..abbffgffbbba..',
    '.abbfgghggfbbba.',
    '.abbffgggfhfbba.',
    '.abbbfffgffbbba.',
    '..abbbbffbbbba..',
    '...aabbbbbbaa...',
    '....ccaaaacc....',
    '......cccc......',
    '................',
    '................',
]
BOWL = [
    '................',
    '................',
    '................',
    '................',
    '...aaaaaaaaaa...',
    '..arrrrsrrrrra..',
    '.adrrsrrrrsrrda.',
    '.addrrrrrsrrdda.',
    '.adddddddddddda.',
    '..adbbbbbbbbda..',
    '..abbbbbbbbbba..',
    '...abbbbbbbba...',
    '....abbbbbba....',
    '.....cccccc.....',
    '................',
    '................',
]

LIGHT = {
    'a': (92, 86, 80), 'b': (226, 222, 212), 'c': (64, 60, 56), 'd': (200, 194, 182),
}
FRIED = {'f': (178, 116, 52), 'g': (210, 154, 74), 'h': (122, 74, 34)}
BURNT_FRIED = {'f': (58, 40, 30), 'g': (84, 58, 40), 'h': (26, 20, 18)}
STEW = {'r': (150, 92, 50), 's': (196, 132, 70)}
CREAM = {'r': (228, 214, 180), 's': (196, 170, 120)}
BURNT_STEW = {'r': (66, 44, 30), 's': (36, 26, 20)}


def draw(rows, colours):
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    for y, row in enumerate(rows):
        for x, ch in enumerate(row):
            if ch != '.':
                im.putpixel((x, y), colours[ch] + (255,))
    return im


def images():
    return {
        'kitchen_dish': draw(PLATE, {**LIGHT, **FRIED}),
        'kitchen_dish_bowl': draw(BOWL, {**LIGHT, **STEW}),
        'kitchen_dish_creamy': draw(BOWL, {**LIGHT, **CREAM}),
        'kitchen_dish_burnt': draw(PLATE, {**LIGHT, **BURNT_FRIED}),
        'kitchen_dish_bowl_burnt': draw(BOWL, {**LIGHT, **BURNT_STEW}),
    }


def models():
    out = {}
    for name in NAMES:
        out[name] = {'parent': 'minecraft:item/generated', 'textures': {'layer0': f'the_oldest_house:item/{name}'}}
    out['kitchen_dish']['overrides'] = [{'predicate': {'custom_model_data': i}, 'model': f'the_oldest_house:item/{name}'} for i, name in enumerate(NAMES) if i > 0]
    return out


def main(check):
    bad = []
    for name, im in images().items():
        path = ASSETS / f'textures/item/{name}.png'
        if check:
            if not path.exists() or Image.open(path).convert('RGBA').tobytes() != im.tobytes():
                bad.append(str(path))
        else:
            im.save(path)
    for name, model in models().items():
        path = ASSETS / f'models/item/{name}.json'
        text = json.dumps(model, indent=2) + '\n'
        if check:
            if not path.exists() or path.read_text() != text:
                bad.append(str(path))
        else:
            path.write_text(text)
    if check:
        assert not bad, bad
        print(f'Verified the kitchen\'s {len(NAMES)} dishes.')


if __name__ == '__main__':
    main('--check' in sys.argv)
