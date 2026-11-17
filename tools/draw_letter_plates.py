"""The lettered plates (0.4.678).

The owner, of the Last Crossing: "there are several signs in last crossing that could benefit from custom textures. i was hoping
you'd notice that on your own". 0.4.677 painted the short signs whole (tools/draw_board_signs.py). The longer notices hold too many
words to paint at sixteen pixels to the block, so the game letters them, in the hand of whoever wrote them, over a plate this draws:

- note: a split board nailed to a stake, Holloway's notes at his camp, in his pencil;
- label: a brass card holder on the wall over each of the archive's stacks, a typed name on its card;
- notice: a typed sheet in a plastic sleeve, tacked to the road crew's trailer at the elk lot.

Each is one letter_plate block (LetterPlates.Kind) whose model this writes; LetterPlateRenderer letters the words over the face this
leaves blank. --check verifies everything.
"""
import argparse
import json
import random
from pathlib import Path

from PIL import Image

import draw_borrasca_signs as signs
from draw_borrasca_signs import border, canvas, field, line, tone, worn

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / 'src/main/resources/assets/the_oldest_house'
MOD = 'the_oldest_house'
DIR = 'letter_plate'
JAVA = ROOT / 'src/main/java/io/github/knaitoe/theoldesthouse/labyrinth/LetterPlates.java'
FACING_Y = {'north': 0, 'east': 90, 'south': 180, 'west': 270}


def note(rnd):
    """The board: a split spruce plank, greyed by weather, a nail at each corner; the stake under it, its top split by the hammer."""
    im = canvas(16, 16)
    field(im, (1, 0, 15, 9), '#8a7660', rnd, 6, 1)
    for x in range(1, 15):
        if rnd.random() < .3:
            im.putpixel((x, 8), tone('#6a5a48', rnd.randint(-6, 6)))
    # The nails in the board's edge columns, clear of the lines he wrote between them.
    for x, y in ((1, 1), (14, 1), (1, 7), (14, 7)):
        im.putpixel((x, y), tone('#2e2a26'))
    worn(im, (1, 0, 15, 9), rnd, '#5e5a4a', 6)
    for y in range(9, 16):
        im.putpixel((7, y), tone('#6b5032', rnd.randint(-6, 6)))
        im.putpixel((8, y), tone('#57402a', rnd.randint(-6, 6)))
    im.putpixel((7, 15), tone('#3c2c1c')); im.putpixel((8, 15), tone('#3c2c1c'))
    return im


def label(rnd):
    """The card holder: a brass frame screwed to the wall at its ends, and the card behind its lip."""
    im = canvas(16, 16)
    field(im, (2, 5, 14, 11), '#9a7a3a', rnd, 4)
    worn(im, (2, 5, 14, 11), rnd, '#5c4a28', 3)
    field(im, (3, 6, 13, 10), '#ece2c8', rnd, 2)
    line(im, 3, 9, 13, 10, '#b8913f')
    for x in (2, 13):
        im.putpixel((x, 8), tone('#5a4620'))
    return im


def notice(rnd):
    """The sheet in its sleeve: white paper gone grey at the edges, the sleeve's shine down one side, a tack at each top corner."""
    im = canvas(16, 16)
    field(im, (2, 0, 14, 16), '#e6e4dc', rnd, 3)
    for y in range(0, 16):
        im.putpixel((12, y), tone('#f6f6f2'))
    border(im, (2, 0, 14, 16), '#c8c6bc')
    worn(im, (2, 0, 14, 16), rnd, '#a8a698', 6)
    for x in (3, 12):
        im.putpixel((x, 1), tone('#9a2a20'))
    return im


def element(lo, hi, faces):
    return {'from': lo, 'to': hi, 'faces': faces}


def uv(x0, y0, x1, y1):
    return {'texture': '#a', 'uv': [x0, y0, x1, y1]}


KINDS = [
    # name, painter, elements (model looks north, as the signs' do: its face is the north face; a wall is behind it to the south)
    ('note', note, [
        element([1, 7, 7.5], [15, 16, 8.5], {'north': uv(1, 0, 15, 9), 'south': uv(15, 0, 1, 9), 'east': uv(1, 0, 2, 9), 'west': uv(14, 0, 15, 9),
                                               'up': uv(1, 0, 15, 1), 'down': uv(1, 8, 15, 9)}),
        element([7, 0, 7.25], [9, 7, 8.75], {'north': uv(7, 9, 9, 16), 'south': uv(7, 9, 9, 16), 'east': uv(7, 9, 8, 16), 'west': uv(8, 9, 9, 16)}),
    ]),
    ('label', label, [
        element([2, 5, 15.25], [14, 11, 16], {'north': uv(2, 5, 14, 11), 'east': uv(2, 5, 3, 11), 'west': uv(13, 5, 14, 11),
                                                'up': uv(2, 5, 14, 6), 'down': uv(2, 10, 14, 11)}),
    ]),
    ('notice', notice, [
        element([2, 0, 15.5], [14, 16, 16], {'north': uv(2, 0, 14, 16), 'east': uv(2, 0, 3, 16), 'west': uv(13, 0, 14, 16),
                                               'up': uv(2, 0, 14, 1), 'down': uv(2, 15, 14, 16)}),
    ]),
]


def files():
    out, pics = {}, {}
    for i, (name, painter, elements) in enumerate(KINDS):
        pics[name] = painter(random.Random(f'letter_plate/{name}'))
        model = {'parent': 'block/block', 'textures': {'a': f'{MOD}:block/{DIR}/{name}', 'particle': f'{MOD}:block/{DIR}/{name}'},
                 'elements': elements}
        out[ASSETS / f'models/block/{DIR}/{name}.json'] = json.dumps(model, indent=1) + '\n'
    variants = {}
    for name, _, _ in KINDS:
        for facing, y in FACING_Y.items():
            v = {'model': f'{MOD}:block/{DIR}/{name}'}
            if y:
                v['y'] = y
            variants[f'facing={facing},kind={name}'] = v
    out[ASSETS / f'blockstates/{DIR}.json'] = json.dumps({'variants': variants}, indent=1) + '\n'
    return pics, out


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument('--check', action='store_true')
    args = ap.parse_args()
    text = JAVA.read_text() if JAVA.exists() else ''
    for name, _, _ in KINDS:
        if f'{name.upper()}(' not in text:
            raise SystemExit(f'LetterPlates.Kind has no {name.upper()}')
    pics, texts = files()
    folder = ASSETS / f'textures/block/{DIR}'
    if args.check:
        for name, im in pics.items():
            path = folder / f'{name}.png'
            if not path.exists() or Image.open(path).convert('RGBA').tobytes() != im.tobytes():
                raise SystemExit(f'{path.relative_to(ROOT)} is not what the tool draws')
        for path, body in texts.items():
            if not path.exists() or path.read_text() != body:
                raise SystemExit(f'{path.relative_to(ROOT)} is not what the tool writes')
        lang = json.loads((ASSETS / 'lang/en_us.json').read_text())
        if not lang.get(f'block.{MOD}.{DIR}'):
            raise SystemExit('the plate has no name in en_us.json')
        print(f'{len(KINDS)} plates: as drawn')
        return
    folder.mkdir(parents=True, exist_ok=True)
    for name, im in pics.items():
        im.save(folder / f'{name}.png')
    for path, body in texts.items():
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_text(body)
    print(f'{len(KINDS)} plates')


if __name__ == '__main__':
    main()
