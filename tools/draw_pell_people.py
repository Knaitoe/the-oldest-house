#!/usr/bin/env python3
"""The Pell house's people's things (docs/PELL_HOUSE_PEOPLE_0_4_524.md): what two of them have in their hands. Tilly's long cane,
and Gail's folder. Drawn at sixteen pixels to the block. The people themselves are painted by tools/paint_character_skins.py.

0.4.526 (docs/PELL_HOUSE_DINNER_0_4_526.md): June's dinner things. Her plates, seen from above as they lie on the table: laid, served
(sausages, mash with the gravy on it, peas) and scraped; and her saucepan, in her hand while she serves, with the spoon in it.

0.4.527 (docs/PELL_HOUSE_BEDTIME_0_4_527.md): June's hairbrush, in her hand while she brushes Tilly's hair at bedtime.

0.4.528 (docs/PELL_HOUSE_DETAIL_0_4_528.md), the owner: "add a little more detail to textures". Still sixteen pixels to the block;
each thing drawn again with light from the upper left, an outline in a darker shade of its own colour where it stands against
nothing, four or five tones where there were two, and what it is made of showing: the brush's cushion and bristles, the pan's
rim and inside, the plate's band and glaze, the card's grain.

Run it to draw; --check verifies the pack against it (CI runs that).
"""
import argparse
import json
import math
import random
import sys
from pathlib import Path

from PIL import Image

sys.path.insert(0, str(Path(__file__).resolve().parent))
from draw_pell_house import A, NS, fill, dot, shade  # noqa: E402

ITEMS = ('pell_cane', 'pell_folder', 'pell_plate', 'pell_dinner', 'pell_plate_scraped', 'pell_saucepan', 'pell_hairbrush')
NAMES = {'item.the_oldest_house.pell_cane': 'Long Cane', 'item.the_oldest_house.pell_folder': 'Folder',
         'item.the_oldest_house.pell_plate': 'Plate', 'item.the_oldest_house.pell_dinner': 'Dinner',
         'item.the_oldest_house.pell_plate_scraped': 'Plate', 'item.the_oldest_house.pell_saucepan': 'Saucepan',
         'item.the_oldest_house.pell_hairbrush': 'Hairbrush'}


def blank():
    return Image.new('RGBA', (16, 16), (0, 0, 0, 0))


def lit(dx, dy):
    """How far a point lies toward the light, which comes from the upper left: 1 up and to the left of its middle, -1 below right."""
    d = math.hypot(dx, dy)
    return -(dx + dy) / math.sqrt(2) / d if d else 0


def edge(cells, x, y):
    return any((x + a, y + b) not in cells for a, b in ((1, 0), (-1, 0), (0, 1), (0, -1)))


def cane():
    """One sheet for the cane's four pieces: the shaft's upper part (columns 0-1, rows 0-8), its lower part with the band near the
    tip (2-3, rows 0-12), the grip (4-5, its top at 4-5 rows 4-5), and the tip (8-9)."""
    im = blank()
    # The shaft: a white tube, lit down one edge and a cooler shade down the other, scuffed where it has been tapped along.
    for x0, rows in ((0, 9), (2, 13)):
        for y in range(rows):
            dot(im, x0, y, '#f6f4ee' if y % 4 else '#eae7df'); dot(im, x0 + 1, y, '#c9c6bd' if y % 3 else '#bbb7ad')
    for x, y in ((0, 5), (1, 2), (2, 3), (3, 7), (2, 11)):
        dot(im, x, y, '#a8a398')
    # The band near the tip: red, bright where it faces the light, a pale reflective line through it.
    fill(im, 2, 8, 3, 11, '#d8473d'); fill(im, 3, 8, 4, 11, '#a72c26'); fill(im, 2, 9, 4, 10, '#f1e9e0'); dot(im, 3, 9, '#cfc7be')
    # The grip: ribbed rubber, each rib lit on top, and its end.
    for y in range(4):
        fill(im, 4, y, 6, y + 1, '#3d3d44' if y % 2 == 0 else '#202024'); dot(im, 5, y, '#2c2c31' if y % 2 == 0 else '#17171a')
    fill(im, 4, 4, 6, 6, '#1c1c20'); dot(im, 4, 4, '#4a4a52'); dot(im, 5, 5, '#101012')
    # The tip: a rolling ball, dark, worn pale on its underside.
    fill(im, 8, 0, 10, 2, '#2c2c30'); dot(im, 8, 0, '#4a4a52'); dot(im, 8, 1, '#6e6e76'); dot(im, 9, 1, '#9a9aa2')
    return im


def folder():
    """The folder: the cover (columns 0-9, rows 0-12) with a tab and a typed label, a stamp half worn off, a paper clip over its top
    and an elastic across its corner; its edge with the papers in it (column 10); and its top, the papers' edges standing up out of
    it (row 13). The card has a grain, lit along its top and left edges and shaded along the others."""
    im = blank()
    rnd = random.Random(52801)
    for y in range(13):
        for x in range(10):
            dot(im, x, y, shade('#d6c08a', rnd.choice((-6, -3, 0, 0, 3, 5))))
    fill(im, 0, 0, 10, 1, '#e4d3a4'); fill(im, 0, 1, 1, 12, '#e0cd9c'); fill(im, 9, 1, 10, 13, '#b39b64'); fill(im, 0, 12, 10, 13, '#a99260')
    fill(im, 0, 0, 3, 1, '#efe2bd')
    # The label, typed: a line and a shorter one under it.
    fill(im, 2, 3, 8, 6, '#f4f1e8'); fill(im, 2, 5, 8, 6, '#dcd8cc')
    fill(im, 3, 3, 7, 4, '#55524c'); dot(im, 5, 3, '#8a867e'); fill(im, 3, 4, 6, 5, '#77736b')
    # Creased where it is held.
    fill(im, 1, 8, 4, 9, '#c6b07a')
    # The stamp: a box with a line of letters in it, the ink gone thin in places.
    for x in range(1, 6):
        dot(im, x, 9, '#b5523f'); dot(im, x, 11, '#b5523f' if x != 3 else '#cf9a86')
    dot(im, 1, 10, '#b5523f'); dot(im, 5, 10, '#c47462'); dot(im, 2, 10, '#c98a74'); dot(im, 4, 10, '#c98a74')
    # The paper clip over the top edge.
    for x, y in ((7, 0), (8, 0), (7, 1), (8, 2)):
        dot(im, x, y, '#c9d0d4')
    dot(im, 8, 1, '#8e979c')
    # The elastic across the far corner, catching the light once.
    for i in range(4):
        dot(im, 6 + i, 8 + i, '#2f2b28')
    dot(im, 7, 9, '#5a554f')
    # The edge: between the covers the sheets, not all square; and the top, one sheet standing up out of the rest.
    for y in range(13):
        dot(im, 10, y, '#f4f2ea' if y % 3 else '#d8d4c8')
    dot(im, 10, 0, '#c4ad76'); dot(im, 10, 12, '#c4ad76')
    for x in range(10):
        dot(im, x, 13, '#f2efe6' if x % 4 else '#d7d3c7')
    dot(im, 0, 13, '#c4ad76'); dot(im, 9, 13, '#c4ad76'); dot(im, 4, 13, '#ffffff')
    return im


def plate():
    """June's plate from above: white, a rim with a ring of blue round it, the well a shade lighter and ringed where it dips. Lit from
    the upper left: the rim's near side is lit and its far side greyer, the well's wall shaded on the side toward the light and lit on
    the other, and the glaze catches the light in a short streak."""
    im = blank()
    for y in range(16):
        for x in range(16):
            dx, dy = x + .5 - 8, y + .5 - 8
            d, l = math.hypot(dx, dy), lit(dx, dy)
            if d > 7.95:
                continue
            if d > 7.25:
                c = '#f6f3ec' if l > .5 else '#e2ddd1' if l > -.4 else '#bdb6a8'
            elif d > 6.35:
                c = '#f2efe7' if l > .2 else '#e4dfd4' if l > -.5 else '#d6d0c3'
            elif d > 5.6:
                if int((math.atan2(dy, dx) + math.pi) / (2 * math.pi) * 16) % 2:
                    c = '#5d7d9d' if l > -.4 else '#4f6d8c'
                else:
                    c = '#86a6c4' if l > .3 else '#7193b3' if l > -.4 else '#5f7f9f'
            elif d > 4.75:
                c = '#dcd6ca' if l > .35 else '#e9e5dc' if l > -.35 else '#f8f6f1'
            else:
                c = '#f7f5ef' if d <= 1.5 and l >= 0 else '#f2efe8'
            dot(im, x, y, c)
    for x, y in ((3, 4), (4, 3), (5, 2)):
        dot(im, x, y, '#ffffff')
    return im


def dinner():
    """The plate served: a mound of mash, rough on top, with the gravy in a hollow on it and run down one side; two sausages side by
    side, browned, lit along the top, a char line across each; and the peas against the mash. Each throws a little shadow on the
    plate."""
    im = plate()
    for x, y in ((8, 9), (8, 8), (7, 9), (6, 9)):
        dot(im, x, y, '#d9d3c6')
    for y in range(16):
        for x in range(16):
            dx, dy = x + .5 - 5.2, y + .5 - 6.0
            if dx * dx + dy * dy <= 3.1 ** 2:
                l = -(dx + dy)
                dot(im, x, y, '#fbf2d2' if l > 2.2 else '#eddcaa' if l > -.6 else '#d7c287' if l > -2.6 else '#bfa86c')
    for x, y in ((3, 4), (5, 3), (4, 6)):
        dot(im, x, y, '#fff9e6')
    for x, y in ((4, 5), (6, 4), (3, 7)):
        dot(im, x, y, '#d9c68f')
    for x, y in ((5, 5), (6, 5), (5, 6), (6, 6), (7, 6), (7, 7), (7, 8)):
        dot(im, x, y, '#83491f')
    dot(im, 6, 6, '#5e3216'); dot(im, 7, 8, '#5e3216'); dot(im, 5, 5, '#b8783f'); dot(im, 6, 5, '#a3632f')
    for y0 in (4, 8):
        fill(im, 10, y0 + 3, 13, y0 + 4, '#d6d0c3')
        fill(im, 10, y0, 13, y0 + 1, '#b97543'); fill(im, 9, y0 + 1, 14, y0 + 2, '#93552e'); fill(im, 10, y0 + 2, 13, y0 + 3, '#653620')
        dot(im, 9, y0 + 1, '#6f3d20'); dot(im, 13, y0 + 1, '#7a4424')
        dot(im, 10, y0, '#dc9d63'); dot(im, 11, y0, '#cd8a52')
        dot(im, 12, y0, '#8a4a25'); dot(im, 11, y0 + 1, '#5e3016'); dot(im, 10, y0 + 2, '#4a250f')
    for x, y in ((4, 10), (5, 10), (6, 10), (3, 11), (4, 11), (5, 11), (6, 11), (7, 11), (4, 12), (5, 12), (6, 12), (5, 9)):
        dot(im, x, y, '#5a9a33' if (x + y) % 2 else '#4a852a')
    for x, y in ((5, 9), (4, 10), (6, 10), (3, 11), (5, 11), (7, 11)):
        dot(im, x, y, '#8cc95a')
    for x, y in ((4, 12), (6, 12)):
        dot(im, x, y, '#356a1d')
    return im


def scraped():
    """The plate eaten from: gravy dragged round the well by the knife, a few peas left at the edge, and the knife and the fork laid
    together across it, handles toward the reader's right hand: the blade lit along its back, the fork's tines spread at its far
    end, the handles dark with a sheen."""
    im = plate()
    for x, y in ((5, 5), (6, 5), (4, 6), (4, 7), (5, 8), (6, 8), (7, 8), (9, 7), (10, 6), (10, 5)):
        dot(im, x, y, '#bf9a6c')
    for x, y in ((5, 6), (5, 7), (8, 8), (9, 6)):
        dot(im, x, y, '#a57a4d')
    dot(im, 6, 5, '#d9bc94')
    for x, y in ((4, 10), (5, 11)):
        dot(im, x, y, '#5a9a33')
    dot(im, 4, 10, '#8cc95a')
    for i in range(6):
        dot(im, 8 + i, 7 + i, '#c9d0d4'); dot(im, 9 + i, 6 + i, '#8d959b')
    dot(im, 8, 7, '#eef2f4'); dot(im, 9, 8, '#e1e6e9')
    dot(im, 8, 5, '#8d959b'); dot(im, 9, 5, '#6f777c'); dot(im, 8, 6, '#a9b1b6')
    for x, y in ((12, 11), (13, 12)):
        dot(im, x, y, '#3a3330')
    for x, y in ((13, 10), (14, 11)):
        dot(im, x, y, '#2f2927')
    dot(im, 12, 11, '#5c524d'); dot(im, 13, 10, '#4f4642')
    return im


def saucepan():
    """June's saucepan, held as a hand holds a tool: the pan at the top right, its rim catching the light and the inside dark, the
    side lit on the left with a bright streak and shaded round to the right, a flat bottom; a wooden spoon standing in it, leaning
    back over the far rim; and the black handle down to the bottom left from a riveted bracket, a hole at its end to hang it by."""
    im = blank()
    cx, top, bot, rx, ry = 10.5, 5.0, 11.0, 4.6, 1.9
    body, rim, inner = set(), set(), set()
    for y in range(16):
        for x in range(16):
            X, Y = x + .5, y + .5
            t = (X - cx) / rx
            if abs(t) <= 1 and top <= Y <= bot + (1 if abs(t) < .7 else 0):
                body.add((x, y))
            if t * t + ((Y - top) / ry) ** 2 <= 1:
                rim.add((x, y))
                if ((X - cx) / (rx - 1.1)) ** 2 + ((Y - top + .15) / (ry - .75)) ** 2 <= 1:
                    inner.add((x, y))
    shape = body | rim
    darker = {'#e9eef1': '#c5cdd2', '#c5cdd2': '#a9b2b7', '#9da6ab': '#868f94', '#7a8388': '#666e73', '#5d656a': '#4d5459'}
    for (x, y) in body:
        t = (x + .5 - cx) / rx
        tone = '#e9eef1' if -.62 <= t < -.38 else '#c5cdd2' if t < -.1 else '#9da6ab' if t < .35 else '#7a8388' if t < .7 else '#5d656a'
        dot(im, x, y, darker[tone] if y >= 10 else tone)
    for (x, y) in rim:
        dot(im, x, y, '#dfe6e9' if y + .5 < top else '#b4bdc2')
    for (x, y) in inner:
        dot(im, x, y, '#6f787d' if y + .5 < top - .2 else '#3b4246')
    for i, (x, y) in enumerate(((10, 4), (11, 3), (12, 2), (12, 1), (13, 0))):
        if (x, y) in inner or (x, y) not in shape or y < top:
            dot(im, x, y, '#d8ad75' if i % 2 == 0 else '#c49660')
            if (x + 1, y) not in inner and y < top - 1:
                dot(im, x + 1, y, '#8e6638')
    for (x, y) in shape:
        if edge(shape, x, y):
            dot(im, x, y, '#363c40' if (x + .5 - cx) > -1 or y + .5 > top + 1 else '#4a5156')
    dot(im, 8, 3, '#f6f9fa'); dot(im, 7, 4, '#f6f9fa')
    dot(im, 6, 7, '#b5bec3'); dot(im, 6, 8, '#8b9499'); dot(im, 6, 6, '#e9eef0')
    for i in range(6):
        x, y = 5 - i, 8 + i
        dot(im, x, y, '#6f6868' if i in (1, 2) else '#4d4848'); dot(im, x, y + 1, '#211d1d')
    dot(im, 0, 13, '#4d4848'); dot(im, 0, 14, '#211d1d'); dot(im, 1, 14, '#211d1d'); dot(im, 0, 15, '#211d1d')
    return im


BRUSH_HEAD = ['...........OOO..', '.........OOxxxO.', '........OxxxxxxO', '.......OxxxxxxxO', '.......OxxxxxxxO', '......OxxxxxxxO.',
              '......OxxxxxxxO.', '......OxxxxxxO..', '.......OxxxOO...', '........OOO.....']


def hairbrush():
    """June's hairbrush, held as the saucepan is, bristle side up: a paddle of oiled wood at the top right, its rim lit toward the
    upper left and dark round the lower right; a dark cushion in it set with bristles, their pale tips in rows and the ones between
    them deeper in; and the handle down to the bottom left along the paddle's line, rounded (lit, middle, shaded across it), worn pale
    where the thumb goes."""
    im = blank()
    head = {(x, y) for y, r in enumerate(BRUSH_HEAD) for x, c in enumerate(r) if c == 'x'}
    rim = {(x, y) for y, r in enumerate(BRUSH_HEAD) for x, c in enumerate(r) if c == 'O'}
    for (x, y) in rim:
        dot(im, x, y, '#3b2415')
    for (x, y) in head:
        toward = 15 - (x + y)  # how far up and to the left of the paddle's line
        if edge(head, x, y):
            tone = '#d2a46e' if toward >= 3 else '#b07a44' if toward >= 0 else '#87582f' if toward >= -3 else '#61401f'
            dot(im, x, y, '#e3bd8c' if y <= 1 and toward >= -1 else tone)
        elif x % 2 == 1 and y % 2 == 1:
            dot(im, x, y, '#f2e8cf' if toward >= 0 else '#c8b89c')
        elif x % 2 == 0 and y % 2 == 0:
            dot(im, x, y, '#6a5a4c' if toward >= 0 else '#4e4038')
        else:
            dot(im, x, y, '#3d312c' if toward >= 4 else '#2c2320')
    for y in range(9, 16):
        for k, tone in ((-1, '#b98249'), (0, '#8c5a31'), (1, '#5b381e')):
            x = 15 - y + k
            if 0 <= x < 16 and (x, y) not in rim and (x, y) not in head and not (y == 9 and k == 1):
                dot(im, x, y, tone)
    for x, y in ((3, 12), (4, 11)):
        dot(im, x, y, '#dcae7a')
    dot(im, 0, 15, '#3b2415'); dot(im, 1, 15, '#3b2415'); dot(im, 0, 14, '#5b381e')
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
