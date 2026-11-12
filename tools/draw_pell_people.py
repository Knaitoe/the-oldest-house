#!/usr/bin/env python3
"""The Pell house's people's things (docs/PELL_HOUSE_PEOPLE_0_4_524.md): what two of them have in their hands. Tilly's long cane,
and Gail's folder. Drawn at sixteen pixels to the block. The people themselves are painted by tools/paint_character_skins.py.

Run it to draw; --check verifies the pack against it (CI runs that).
"""
import argparse
import json
import sys
from pathlib import Path

from PIL import Image

sys.path.insert(0, str(Path(__file__).resolve().parent))
from draw_pell_house import A, NS, fill, dot  # noqa: E402

ITEMS = ('pell_cane', 'pell_folder')
NAMES = {'item.the_oldest_house.pell_cane': 'Long Cane', 'item.the_oldest_house.pell_folder': 'Folder'}


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
    return {'pell_cane': cane_model, 'pell_folder': folder_model}


def main():
    parser = argparse.ArgumentParser(); parser.add_argument('--check', action='store_true'); check = parser.parse_args().check
    images, data = {'pell_cane': cane(), 'pell_folder': folder()}, models()
    assert set(images) == set(ITEMS) == set(data)
    for name, im in images.items():
        path = A / 'textures/item' / f'{name}.png'
        if check:
            with Image.open(path) as actual:
                assert actual.convert('RGBA').tobytes() == im.tobytes(), name
        else:
            im.save(path, optimize=True)
    for name, obj in data.items():
        for el in obj['elements']:
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
