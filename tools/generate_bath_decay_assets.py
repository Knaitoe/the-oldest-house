#!/usr/bin/env python3
"""Authored native 16-pixel bath contents. Does not repaint any earlier texture or rebuild the room."""
import argparse
import copy
import io
import json
from pathlib import Path

from PIL import Image
from generate_stanley_hotel_assets import tub_top

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / 'src/main/resources/assets/the_oldest_house'
TEXTURES = ASSETS / 'textures/block/stanley'
MODELS = ASSETS / 'models/block'
PALETTE = ((65, 39, 36), (77, 44, 40), (87, 49, 43), (56, 37, 35), (104, 63, 51))


def remains(head):
    image = tub_top(head)
    lo, hi = (3, 15) if head else (0, 12)
    for y in range(lo, hi + 1):
        for x in range(2, 14):
            image.putpixel((x, y), PALETTE[(x // 3 * 7 + y // 2 * 11) % len(PALETTE)] + (255,))
    # Broad floating pieces with ragged edges and a dark, wet side. Their shapes carry across the two halves.
    for x0, y0, width, height in ((3, lo + 2, 5, 3), (9, lo + 6, 4, 4), (5, hi - 1, 4, 2)):
        for y in range(y0, min(hi + 1, y0 + height)):
            for x in range(x0, min(14, x0 + width)):
                if x == x0 and y == y0:
                    continue
                color = (152, 119, 95) if y == y0 else (178, 150, 119) if (x + y) % 3 else (125, 86, 70)
                image.putpixel((x, y), color + (255,))
    return image


def slough():
    image = Image.new('RGBA', (16, 16))
    for y in range(16):
        for x in range(16):
            n = (x // 3 * 7 + y // 2 * 11) % 9
            color = (174, 145, 114) if n < 4 else (145, 108, 86) if n < 7 else (113, 72, 62)
            image.putpixel((x, y), color + (255,))
    return image


def element(start, end):
    return {'from': start, 'to': end, 'faces': {side: {'texture': '#f'} for side in ('north', 'south', 'east', 'west', 'up', 'down')}}


def outputs():
    result = {}
    for half, name in enumerate(('head', 'foot')):
        stream = io.BytesIO()
        remains(half == 0).save(stream, format='PNG')
        result[TEXTURES / f'bath_remains_{name}.png'] = stream.getvalue()
        model = copy.deepcopy(json.loads((MODELS / f'stanley_tub_{half}.json').read_text()))
        model['textures']['t'] = f'the_oldest_house:block/stanley/bath_remains_{name}'
        model['textures']['f'] = 'the_oldest_house:block/stanley/bath_slough'
        # Debris projects above the liquid, stays below the rim's collision height plus two pixels and leaves the seam open.
        model['elements'].extend([
            element([3, 10.01, 4 if half == 0 else 2], [7.5, 10.65, 7 if half == 0 else 5]),
            element([9, 10.01, 10 if half == 0 else 7], [13, 11.15, 14 if half == 0 else 10]),
            element([5, 10.01, 13 if half == 0 else 11], [8, 10.4, 15 if half == 0 else 13]),
        ])
        result[MODELS / f'stanley_tub_{half + 2}.json'] = (json.dumps(model, indent=1) + '\n').encode()
    stream = io.BytesIO()
    slough().save(stream, format='PNG')
    result[TEXTURES / 'bath_slough.png'] = stream.getvalue()
    return result


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--check', action='store_true')
    args = parser.parse_args()
    for path, data in outputs().items():
        if args.check:
            if not path.exists() or path.read_bytes() != data:
                raise SystemExit(f'Bath decay asset differs: {path.relative_to(ROOT)}')
        else:
            path.write_bytes(data)
    states = json.loads((ASSETS / 'blockstates/stanley.json').read_text())['variants']
    for facing in ('north', 'east', 'south', 'west'):
        for stage in range(4):
            assert states[f'facing={facing},kind=tub,stage={stage}']['model'] == f'the_oldest_house:block/stanley_tub_{stage}'
    print('Bath decay: three native 16-pixel materials, two filled models and sixteen native tub states agree.')


if __name__ == '__main__':
    main()
