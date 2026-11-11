#!/usr/bin/env python3
"""0.4.452: the reading desk at sixteen pixels to the block (the owner: "retexture this table and all instances of it. It's got an
hd texture that isn't in line with vanilla").

The desk (HouseholdFurnitureBlock.Kind.READING_DESK) was the only model on a 64-pixel oak sheet made for another room. It has three
pictures of its own now, each sixteen pixels square, in the long runs of two or three tones the game's own planks are drawn in:

* reading_desk_top: the top, three boards with a knot or two;
* reading_desk_wood: everything else. Its first two rows are the edge of the top; below them the grain runs upright, for the legs,
  the rail and the sides of the pedestal;
* reading_desk_front: the pedestal's face, three drawers with a brass pull each.

The model keeps its shape; this writes it with the three pictures on it. Run with --check to verify the files without writing them.
"""
from pathlib import Path
import argparse
import json
import random
from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / 'src/main/resources/assets/the_oldest_house'
MOD = 'the_oldest_house'
MODEL = ASSETS / 'models/block/furniture_reading_desk.json'

BASE, DARK, DEEP, LIGHT, PALE = (142, 96, 54), (116, 76, 42), (84, 54, 30), (160, 112, 64), (176, 128, 78)
BRASS, BRASS_DARK = (196, 164, 84), (132, 104, 48)


def put(im, x, y, c):
    if 0 <= x < 16 and 0 <= y < 16:
        im.putpixel((x, y), c + (255,))


def boards(seed, seams, upright):
    """Boards side by side: each its own tone, in long runs along the grain, with a dark seam between them."""
    rnd = random.Random(seed)
    im = Image.new('RGBA', (16, 16))
    for b, (lo, hi) in enumerate(zip([0] + [s + 1 for s in seams], seams + [16])):
        tone = [BASE, LIGHT, BASE, DARK][(b + seed) % 4]
        for a in range(lo, hi):
            run, c = 0, tone
            for along in range(16):
                if run == 0:
                    run = rnd.randint(3, 7)
                    c = rnd.choice([tone, tone, BASE, PALE if tone == LIGHT else LIGHT, BASE if tone == DARK else DARK])
                run -= 1
                put(im, *((a, along) if upright else (along, a)), c)
    for s in seams:
        for along in range(16):
            put(im, *((s, along) if upright else (along, s)), DEEP)
    return im


def top():
    im = boards(4, [4, 10], False)
    for x, y in ((3, 2), (11, 7), (6, 13)):
        put(im, x, y, DEEP)
        put(im, x + 1, y, DARK)
    return im


def wood():
    im = boards(8, [7], True)
    for x in range(16):
        put(im, x, 0, PALE if x % 5 else LIGHT)     # the top's edge: its worn arris,
        put(im, x, 1, DARK)                         # and the shadow under it
    return im


def front():
    """The pedestal's face. The model shows columns 9 to 14 of rows 2 to 15: three drawers, a dark gap between."""
    im = wood()
    for y0 in (2, 7, 12):
        for y in range(y0, y0 + 4):
            for x in range(9, 15):
                put(im, x, y, LIGHT if y == y0 else DARK if y == y0 + 3 else BASE)
        put(im, 9, y0, PALE)
        put(im, 11, y0 + 2, BRASS)
        put(im, 12, y0 + 2, BRASS_DARK)
    for y in (6, 11):
        for x in range(9, 15):
            put(im, x, y, DEEP)
    return im


def pictures():
    return {'reading_desk_top': top(), 'reading_desk_wood': wood(), 'reading_desk_front': front()}


def face(uv, texture):
    return {'uv': uv, 'texture': texture}


def model():
    """The desk as it was built: a top, a pedestal of drawers under its left end, two legs under its right, and a rail between."""
    w, f, t = '#wood', '#front', '#top'
    elements = [
        {'from': [0, 14, 0], 'to': [16, 16, 16], 'faces': {
            'north': face([0, 0, 16, 2], w), 'south': face([0, 0, 16, 2], w), 'west': face([0, 0, 16, 2], w), 'east': face([0, 0, 16, 2], w),
            'up': face([0, 0, 16, 16], t), 'down': face([0, 0, 16, 16], t)}},
        {'from': [1, 0, 2], 'to': [7, 14, 14], 'faces': {
            'north': face([9, 2, 15, 16], f), 'south': face([1, 2, 7, 16], w), 'west': face([2, 2, 14, 16], w), 'east': face([2, 2, 14, 16], w),
            'up': face([1, 2, 7, 14], w), 'down': face([1, 2, 7, 14], w)}},
        {'from': [12, 0, 2], 'to': [14, 14, 4], 'faces': {
            'north': face([2, 2, 4, 16], w), 'south': face([12, 2, 14, 16], w), 'west': face([2, 2, 4, 16], w), 'east': face([12, 2, 14, 16], w),
            'up': face([12, 2, 14, 4], w), 'down': face([12, 12, 14, 14], w)}},
        {'from': [12, 0, 12], 'to': [14, 14, 14], 'faces': {
            'north': face([2, 2, 4, 16], w), 'south': face([12, 2, 14, 16], w), 'west': face([12, 2, 14, 16], w), 'east': face([2, 2, 4, 16], w),
            'up': face([12, 12, 14, 14], w), 'down': face([12, 2, 14, 4], w)}},
        {'from': [7, 11, 11], 'to': [14, 14, 14], 'faces': {
            'north': face([2, 2, 9, 5], w), 'south': face([7, 2, 14, 5], w), 'west': face([11, 2, 14, 5], w), 'east': face([2, 2, 5, 5], w),
            'up': face([7, 11, 14, 14], w), 'down': face([7, 2, 14, 5], w)}},
    ]
    return {'parent': 'minecraft:block/block',
            'textures': {'wood': f'{MOD}:block/reading_desk_wood', 'top': f'{MOD}:block/reading_desk_top',
                         'front': f'{MOD}:block/reading_desk_front', 'particle': f'{MOD}:block/reading_desk_wood'},
            'elements': elements}


def dump(m):
    head = {k: v for k, v in m.items() if k != 'elements'}
    tight = lambda v: json.dumps(v, separators=(',', ':'))  # noqa: E731
    return tight(head)[:-1] + ',\n "elements":[\n  ' + ',\n  '.join(tight(e) for e in m['elements']) + '\n ]}\n'


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument('--check', action='store_true')
    ap.add_argument('--sheet')
    args = ap.parse_args()
    pics, text = pictures(), dump(model())
    if args.sheet:
        page = Image.new('RGBA', (3 * 200 + 10, 210), (90, 90, 90, 255))
        for i, im in enumerate(pics.values()):
            page.paste(im.resize((192, 192), Image.NEAREST), (6 + i * 200, 8))
        page.save(args.sheet)
        return
    folder = ASSETS / 'textures/block'
    if args.check:
        for name, im in pics.items():
            path = folder / f'{name}.png'
            if not path.exists() or Image.open(path).convert('RGBA').tobytes() != im.tobytes():
                raise SystemExit(f'{path.relative_to(ROOT)} is not what the tool draws')
        if not MODEL.exists() or MODEL.read_text() != text:
            raise SystemExit(f'{MODEL.relative_to(ROOT)} is not what the tool writes')
        print(f'the reading desk: {len(pics)} pictures and its model, as drawn')
        return
    for name, im in pics.items():
        im.save(folder / f'{name}.png')
    MODEL.write_text(text)
    print(f'the reading desk: {len(pics)} pictures and its model')


if __name__ == '__main__':
    main()
