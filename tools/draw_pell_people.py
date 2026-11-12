#!/usr/bin/env python3
"""The Pell house's people's things (docs/PELL_HOUSE_PEOPLE_0_4_524.md): what two of them have in their hands. Tilly's long cane,
and Gail's folder. Drawn at sixteen pixels to the block. The people themselves are painted by tools/paint_character_skins.py.

0.4.526 (docs/PELL_HOUSE_DINNER_0_4_526.md): June's dinner things. Her plates, seen from above as they lie on the table: laid, served
(sausages, mash with the gravy on it, peas) and scraped; and her saucepan, in her hand while she serves, with the spoon in it.

0.4.527 (docs/PELL_HOUSE_BEDTIME_0_4_527.md): June's hairbrush, in her hand while she brushes Tilly's hair at bedtime.

Run it to draw; --check verifies the pack against it (CI runs that).
"""
import argparse
import json
import sys
from pathlib import Path

from PIL import Image

sys.path.insert(0, str(Path(__file__).resolve().parent))
from draw_pell_house import A, NS, fill, dot  # noqa: E402

ITEMS = ('pell_cane', 'pell_folder', 'pell_plate', 'pell_dinner', 'pell_plate_scraped', 'pell_saucepan', 'pell_hairbrush')
NAMES = {'item.the_oldest_house.pell_cane': 'Long Cane', 'item.the_oldest_house.pell_folder': 'Folder',
         'item.the_oldest_house.pell_plate': 'Plate', 'item.the_oldest_house.pell_dinner': 'Dinner',
         'item.the_oldest_house.pell_plate_scraped': 'Plate', 'item.the_oldest_house.pell_saucepan': 'Saucepan',
         'item.the_oldest_house.pell_hairbrush': 'Hairbrush'}


def cane():
    """One sheet for the cane's four pieces: the shaft's upper part (columns 0-1, rows 0-8), its lower part with the band near the
    tip (2-3, rows 0-12), the grip (4-5, its top at 4-5 rows 4-5), and the tip (8-9)."""
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    # The shaft: a white tube, lit down one side and shaded down the other, scuffed where it has been tapped along.
    for x0, rows in ((0, 9), (2, 13)):
        fill(im, x0, 0, x0 + 1, rows, '#f1efe8'); fill(im, x0 + 1, 0, x0 + 2, rows, '#cfccc3')
    for x, y in ((0, 3), (1, 6), (2, 1), (3, 4), (2, 6)):
        dot(im, x, y, '#bdb9ae')
    # The band near the tip, reflective: two rows the other side of a pale line.
    fill(im, 2, 8, 4, 11, '#b8322c'); fill(im, 2, 8, 3, 11, '#d0453c'); fill(im, 2, 9, 4, 10, '#e6dcd2')
    # The grip: black rubber, ribbed, and its end.
    fill(im, 4, 0, 6, 4, '#26262a')
    for y in (0, 2):
        fill(im, 4, y, 6, y + 1, '#3a3a40')
    fill(im, 4, 4, 6, 6, '#1c1c20'); dot(im, 4, 4, '#34343a')
    # The tip: a rolling ball, dark, worn pale on its underside.
    fill(im, 8, 0, 10, 2, '#2c2c30'); dot(im, 8, 1, '#5a5a62')
    return im


def folder():
    """The folder: the cover (columns 0-9, rows 0-12) with a tab and a label, an elastic across its corner; its edge with the
    papers in it (column 10); and its top, the papers' edges standing up out of it (row 13)."""
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    fill(im, 0, 0, 10, 13, '#d6c08a')
    fill(im, 0, 0, 10, 1, '#c4ad76'); fill(im, 0, 12, 10, 13, '#b9a26c'); fill(im, 9, 0, 10, 13, '#c4ad76')
    # The tab, a strip of the back cover showing at the top left, and the label stuck on the front.
    fill(im, 0, 0, 3, 1, '#e2cf9e')
    fill(im, 2, 3, 8, 5, '#f2efe6'); fill(im, 3, 4, 7, 5, '#6c6a64')
    # The elastic across the far corner.
    for i in range(4):
        dot(im, 6 + i, 12 - 3 + i - 1, '#3a3632')
    # Creased where it is held.
    fill(im, 1, 8, 4, 9, '#cbb57f')
    # The edge: the cover's fold, and between the covers the sheets, white.
    fill(im, 10, 0, 11, 13, '#ece9e0'); dot(im, 10, 0, '#c4ad76'); dot(im, 10, 12, '#c4ad76')
    # The top: the sheets' edges, uneven, one sticking up out of the rest.
    fill(im, 0, 13, 10, 14, '#f1eee6'); fill(im, 0, 13, 1, 14, '#c4ad76'); fill(im, 9, 13, 10, 14, '#c4ad76'); dot(im, 5, 13, '#d9d5ca')
    return im


def round_cells(cx, cy, r):
    """The cells of a disc centred at (cx, cy), measured from each cell's middle."""
    return [(x, y) for y in range(16) for x in range(16) if (x + .5 - cx) ** 2 + (y + .5 - cy) ** 2 <= r * r]


def plate():
    """June's plate from above: white, a blue band near the rim, the well a shade lighter than the rim and ringed where it dips. Lit
    from the top left, so the rim's far side is a little greyer."""
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    for x, y in round_cells(8, 8, 7.9):
        d = ((x + .5 - 8) ** 2 + (y + .5 - 8) ** 2) ** .5
        far = x + y > 17
        if d > 7.1:
            c = '#cfc9bb' if far else '#ddd8cb'
        elif d > 6.2:
            c = '#5f7f9f' if far else '#7193b3'
        elif d > 5.3:
            c = '#e4dfd3' if far else '#efebe2'
        elif d > 4.6:
            c = '#e0dbcf'
        else:
            c = '#f5f2eb' if not far else '#ece8de'
        dot(im, x, y, c)
    return im


def dinner():
    """The plate served: a mound of mash with the gravy on it, two sausages side by side, and the peas against the mash."""
    im = plate()
    # The mash, heaped, lit on its top left; a little gravy run into a hollow on its top and down one side.
    for x, y in round_cells(5.4, 6.2, 3.0):
        dot(im, x, y, '#d6c286' if x + y > 12 else '#eadcac')
    for x, y in ((3, 4), (4, 4), (4, 3), (3, 5)):
        dot(im, x, y, '#f8f0d0')
    for x, y in ((5, 6), (6, 6), (5, 7), (6, 7), (7, 8)):
        dot(im, x, y, '#7a4826')
    dot(im, 5, 6, '#a0673a'); dot(im, 7, 8, '#5f361b')
    # Two sausages lying side by side along the plate's right, a gap of plate between them, browned on top and rounded at the ends.
    for y0 in (4, 8):
        fill(im, 10, y0, 13, y0 + 1, '#b67140')
        fill(im, 9, y0 + 1, 14, y0 + 2, '#8c522d')
        fill(im, 10, y0 + 2, 13, y0 + 3, '#653620')
        dot(im, 9, y0 + 1, '#764324'); dot(im, 13, y0 + 1, '#764324')
        dot(im, 11, y0 + 1, '#a5643a')
    # The peas, in a heap below the mash.
    for x, y in ((4, 9), (5, 9), (6, 9), (3, 10), (4, 10), (5, 10), (6, 10), (7, 10), (4, 11), (5, 11), (6, 11)):
        dot(im, x, y, '#5c9a34')
    for x, y in ((4, 9), (6, 10), (4, 11)):
        dot(im, x, y, '#90c65a')
    return im


def scraped():
    """The plate eaten from: gravy dragged round the well by the knife, a few peas left at the edge, the knife and fork laid
    together across it."""
    im = plate()
    for x, y in ((5, 5), (6, 5), (4, 6), (5, 7), (6, 8), (7, 8), (9, 7), (10, 6)):
        dot(im, x, y, '#c49e74')
    for x, y in ((4, 7), (8, 8)):
        dot(im, x, y, '#a87c52')
    for x, y in ((4, 10), (5, 11)):
        dot(im, x, y, '#5c9a34')
    # The knife and the fork, handles toward the reader's right hand.
    for i in range(6):
        dot(im, 8 + i, 7 + i, '#b9c0c5'); dot(im, 9 + i, 6 + i, '#8d959b')
    dot(im, 13, 12, '#2f2b29'); dot(im, 14, 11, '#2f2b29')
    return im


def saucepan():
    """June's saucepan, held as a hand holds a tool: the pan at the top right with its rim catching the light, the black handle down
    to the bottom left, and a wooden spoon standing in it."""
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    fill(im, 11, 1, 12, 5, '#c79d6b'); dot(im, 11, 1, '#a57d4f')
    fill(im, 6, 5, 15, 6, '#cfd6da')
    fill(im, 6, 6, 15, 11, '#99a1a6')
    fill(im, 7, 6, 8, 11, '#c3cacf')
    fill(im, 13, 6, 15, 11, '#6e767b')
    fill(im, 7, 11, 14, 12, '#5b6267')
    for i in range(6):
        dot(im, 5 - i, 8 + i, '#262222'); dot(im, 5 - i, 9 + i, '#3f3a39')
    return im


def hairbrush():
    """June's hairbrush, held as the saucepan is: its oval wooden head at the top right with the bristles in rows on it, the rim
    darker, and the handle down to the bottom left, worn pale where the thumb goes."""
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    for y in range(1, 10):
        for x in range(7, 16):
            d = ((x - 11) / 4.2) ** 2 + ((y - 5) / 4.2) ** 2
            if d <= 1:
                dot(im, x, y, '#6e4a2c' if d > .62 else '#8a5e38')
    for y in (3, 5, 7):
        for x in (9, 11, 13):
            dot(im, x + (1 if y == 5 else 0), y, '#e8dcc4')
    for i in range(7):
        dot(im, 7 - i, 8 + i, '#5a3a22'); dot(im, 7 - i, 9 + i, '#3e2716')
    dot(im, 4, 11, '#a07a52'); dot(im, 5, 10, '#946e48')
    return im


def flat(name, parent='minecraft:item/generated'):
    """A flat thing, from its picture: the plates lie on the table (laid there as item displays, face up) and the saucepan is held."""
    return {'parent': parent, 'textures': {'layer0': NS + 'item/' + name}}


def faces(side, end, texture):
    f = {k: {'uv': list(side), 'texture': texture} for k in ('north', 'south', 'east', 'west')}
    f.update({k: {'uv': list(end), 'texture': texture} for k in ('up', 'down')})
    return f


def models():
    """The cane and the folder as they are held. The cane's grip is at the fist and its tip goes down and forward to the ground in
    front of the feet; the folder lies flat to the chest."""
    cane_model = {
        'textures': {'c': NS + 'item/pell_cane', 'particle': NS + 'item/pell_cane'},
        'elements': [
            {'from': [7, 12, 7], 'to': [9, 16, 9], 'faces': faces((4, 0, 6, 4), (4, 4, 6, 6), '#c')},
            {'from': [7.5, 3, 7.5], 'to': [8.5, 12, 8.5], 'faces': faces((0, 0, 1, 9), (0, 0, 1, 1), '#c')},
            {'from': [7.5, -10, 7.5], 'to': [8.5, 3, 8.5], 'faces': faces((2, 0, 3, 13), (2, 12, 3, 13), '#c')},
            {'from': [7, -11, 7], 'to': [9, -10, 9], 'faces': faces((8, 0, 10, 1), (8, 0, 10, 2), '#c')}],
        'display': {
            # In the hand the model's up is up the arm; turned a third as far again about the hand's width, the tip goes down
            # and ahead to the ground. The grip (six over the model's middle, at eight tenths) is moved back into the fist.
            'thirdperson_righthand': {'rotation': [120, 0, 0], 'translation': [0, 2.4, -4.2], 'scale': [.8, .8, .8]},
            'thirdperson_lefthand': {'rotation': [120, 0, 0], 'translation': [0, 2.4, -4.2], 'scale': [.8, .8, .8]},
            'firstperson_righthand': {'rotation': [30, 0, 0], 'translation': [0, -2, 0], 'scale': [.6, .6, .6]},
            'firstperson_lefthand': {'rotation': [30, 0, 0], 'translation': [0, -2, 0], 'scale': [.6, .6, .6]},
            'gui': {'rotation': [0, 0, -45], 'translation': [0, 0, 0], 'scale': [.68, .68, .68]},
            'ground': {'translation': [0, 2, 0], 'scale': [.5, .5, .5]},
            'fixed': {'rotation': [0, 0, -45], 'scale': [.68, .68, .68]}}}
    folder_model = {
        'textures': {'f': NS + 'item/pell_folder', 'particle': NS + 'item/pell_folder'},
        'elements': [
            {'from': [3, 1.5, 7.5], 'to': [13, 14.5, 8.5], 'faces': {
                'north': {'uv': [0, 0, 10, 13], 'texture': '#f'}, 'south': {'uv': [10, 0, 0, 13], 'texture': '#f'},
                'east': {'uv': [10, 0, 11, 13], 'texture': '#f'}, 'west': {'uv': [10, 0, 11, 13], 'texture': '#f'},
                'up': {'uv': [0, 13, 10, 14], 'texture': '#f'}, 'down': {'uv': [0, 12, 10, 13], 'texture': '#f'}}}],
        'display': {
            # Held to the chest: the arm comes up across it, so the folder is turned back up against it, face out.
            'thirdperson_righthand': {'rotation': [45, 90, 0], 'translation': [0, 0, 0], 'scale': [.7, .7, .7]},
            'thirdperson_lefthand': {'rotation': [45, -90, 0], 'translation': [0, 0, 0], 'scale': [.7, .7, .7]},
            'firstperson_righthand': {'rotation': [0, -20, 0], 'translation': [1, 1, 0], 'scale': [.6, .6, .6]},
            'firstperson_lefthand': {'rotation': [0, 20, 0], 'translation': [1, 1, 0], 'scale': [.6, .6, .6]},
            'gui': {'rotation': [0, 0, 0], 'translation': [0, 0, 0], 'scale': [1, 1, 1]},
            'ground': {'translation': [0, 2, 0], 'scale': [.5, .5, .5]},
            'fixed': {'scale': [.9, .9, .9]}}}
    return {'pell_cane': cane_model, 'pell_folder': folder_model, 'pell_plate': flat('pell_plate'), 'pell_dinner': flat('pell_dinner'),
            'pell_plate_scraped': flat('pell_plate_scraped'), 'pell_saucepan': flat('pell_saucepan', 'minecraft:item/handheld'),
            'pell_hairbrush': flat('pell_hairbrush', 'minecraft:item/handheld')}


def main():
    parser = argparse.ArgumentParser(); parser.add_argument('--check', action='store_true'); check = parser.parse_args().check
    images = {'pell_cane': cane(), 'pell_folder': folder(), 'pell_plate': plate(), 'pell_dinner': dinner(), 'pell_plate_scraped': scraped(),
              'pell_saucepan': saucepan(), 'pell_hairbrush': hairbrush()}
    data = models()
    assert set(images) == set(ITEMS) == set(data)
    for name, im in images.items():
        path = A / 'textures/item' / f'{name}.png'
        if check:
            with Image.open(path) as actual:
                assert actual.convert('RGBA').tobytes() == im.tobytes(), name
        else:
            im.save(path, optimize=True)
    for name, obj in data.items():
        for el in obj.get('elements', []):
            for axis in range(3):
                assert -16 <= el['from'][axis] <= el['to'][axis] <= 32, (name, el)
        path = A / 'models/item' / f'{name}.json'
        if check:
            assert json.loads(path.read_text()) == obj, name
        else:
            path.write_text(json.dumps(obj, indent=2) + '\n')
    lang_path = A / 'lang/en_us.json'; lang = json.loads(lang_path.read_text())
    for key, name in NAMES.items():
        if check:
            assert lang.get(key) == name, key
        else:
            lang[key] = name
    if not check:
        lang_path.write_text(json.dumps(lang, indent=2, ensure_ascii=False) + '\n')
    print(f'{"Verified" if check else "Drew"} the Pell house\'s people\'s things: {len(images)} textures and models.')


if __name__ == '__main__':
    main()
