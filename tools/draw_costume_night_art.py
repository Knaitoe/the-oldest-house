"""Costume Night (0.4.103): the party's props, drawn in code.

A string of coloured bulbs on its wire (lit, and dark once the power goes), the radio (its dial glowing, then not), the
tub for bobbing apples, a plastic jack-o'-lantern pail of candy, and the contest board in two halves: the title band, a
crawling witch, the twelve numbers and a ribbon. Each model faces north; the blockstate turns it and swaps the lit and
dark bulbs and radio. The drawing helpers are Movie Night's.

Usage: python3 tools/draw_costume_night_art.py
"""
import json

import numpy as np
from PIL import Image

from draw_movie_night_art import ASSETS, canvas, rect, px, ellipse, line, noise, element, vgrad, poly

T = 'the_oldest_house:block/'
DIGITS = {'0': ['111', '101', '101', '101', '111'], '1': ['010', '110', '010', '010', '111'], '2': ['111', '001', '111', '100', '111'],
          '3': ['111', '001', '011', '001', '111'], '4': ['101', '101', '111', '001', '001'], '5': ['111', '100', '111', '001', '111'],
          '6': ['111', '100', '111', '101', '111'], '7': ['111', '001', '010', '010', '010'], '8': ['111', '101', '111', '101', '111'],
          '9': ['111', '101', '111', '001', '111']}


def digit(img, ch, x, y, color):
    for row, bits in enumerate(DIGITS[ch]):
        for col, bit in enumerate(bits):
            if bit == '1':
                px(img, x + col, y + row, color)


def textures():
    out = {}
    # Four bulb colours in the quarters of one texture; each bulb's faces take one quarter.
    for name, colors in (('costume_bulbs', [(255, 92, 70), (255, 214, 96), (120, 230, 120), (110, 170, 255)]),
                         ('costume_bulbs_off', [(92, 54, 50), (104, 92, 58), (58, 84, 58), (56, 70, 96)])):
        b = canvas(16, 16, (0, 0, 0, 255))
        for i, c in enumerate(colors):
            x0, y0 = (i % 2) * 8, (i // 2) * 8
            rect(b, x0, y0, x0 + 8, y0 + 8, c)
            hi = tuple(min(255, v + 50) for v in c)
            rect(b, x0 + 2, y0 + 2, x0 + 4, y0 + 4, hi if name == 'costume_bulbs' else c)
        out[name] = b
    # The radio: walnut case, a cloth grille, and the dial lit amber or dark.
    for name, dial in (('costume_radio_front', (255, 196, 90)), ('costume_radio_front_off', (70, 60, 44))):
        r = canvas(16, 16, (92, 58, 34))
        rect(r, 1, 1, 15, 15, (110, 70, 40))
        rect(r, 2, 2, 9, 14, (170, 150, 112))
        for y in range(3, 14, 2):
            rect(r, 2, y, 9, y + 1, (140, 120, 88))
        rect(r, 10, 3, 14, 7, dial)
        px(r, 12, 4, (60, 30, 20))
        px(r, 12, 5, (60, 30, 20))
        ellipse(r, (10, 9, 13, 12), (40, 30, 24))
        noise(r, .03, 601)
        out[name] = r
    side = canvas(16, 16, (100, 64, 38))
    for x in range(0, 16, 4):
        rect(side, x, 0, x + 1, 16, (84, 52, 30))
    noise(side, .04, 602)
    out['costume_radio_side'] = side
    # The apple tub: staves and two iron bands; water and red apples bobbing at the top.
    tub = canvas(16, 16, (126, 88, 52))
    for x in range(0, 16, 3):
        rect(tub, x, 0, x + 1, 16, (104, 70, 40))
    for y in (2, 12):
        rect(tub, 0, y, 16, y + 2, (70, 70, 74))
    noise(tub, .04, 603)
    out['costume_tub_side'] = tub
    top = canvas(16, 16, (40, 70, 96))
    rect(top, 0, 0, 16, 1, (104, 70, 40))
    rect(top, 0, 15, 16, 16, (104, 70, 40))
    rect(top, 0, 0, 1, 16, (104, 70, 40))
    rect(top, 15, 0, 16, 16, (104, 70, 40))
    for x, y in ((3, 3), (9, 4), (5, 9), (11, 10), (7, 6)):
        ellipse(top, (x, y, x + 3, y + 3), (186, 30, 34))
        px(top, x + 1, y, (70, 110, 40))
    for x, y in ((2, 12), (12, 2), (8, 13)):
        px(top, x, y, (120, 160, 190))
    out['costume_tub_top'] = top
    # A plastic jack-o'-lantern pail: orange sides, a face on the front, candy wrappers heaped in it.
    pail = canvas(16, 16, (236, 122, 24))
    for x in range(0, 16, 5):
        rect(pail, x, 0, x + 1, 16, (212, 104, 18))
    rect(pail, 0, 0, 16, 1, (250, 150, 50))
    out['costume_pail_side'] = pail
    face = pail.copy()
    poly(face, [(3, 5), (6, 5), (4, 8)], (30, 20, 10))
    poly(face, [(10, 5), (13, 5), (11, 8)], (30, 20, 10))
    line(face, [(3, 11), (5, 13), (7, 11), (9, 13), (11, 11), (13, 13)], (30, 20, 10))
    out['costume_pail_face'] = face
    candy = canvas(16, 16, (250, 230, 200))
    rng = np.random.default_rng(604)
    for _ in range(26):
        x, y = int(rng.integers(0, 15)), int(rng.integers(0, 15))
        c = [(220, 40, 40), (250, 210, 60), (90, 60, 140), (240, 120, 30), (60, 140, 70), (250, 250, 240)][int(rng.integers(0, 6))]
        rect(candy, x, y, x + 2, y + 1, c)
    out['costume_candy'] = candy
    # The contest board, sixteen by thirty-two: the title band, a crawling witch, the twelve numbers and the blue ribbon.
    board = canvas(16, 32, (232, 140, 40))
    vgrad(board, 0, 32, (240, 156, 52), (200, 110, 30))
    rect(board, 0, 1, 16, 7, (24, 20, 22))
    for x0, w in ((2, 2), (5, 3), (9, 2), (12, 2)):
        rect(board, x0, 2, x0 + w, 3, (240, 156, 52))
    for x0, w in ((3, 3), (7, 4), (12, 1)):
        rect(board, x0, 4, x0 + w, 5, (240, 156, 52))
    # The witch, on all fours, hair hanging.
    rect(board, 3, 10, 11, 12, (24, 20, 22))
    for x in (3, 4, 9, 10):
        rect(board, x, 12, x + 1, 15, (24, 20, 22))
    rect(board, 10, 8, 13, 11, (24, 20, 22))
    for x in (10, 12):
        rect(board, x, 11, x + 1, 14, (24, 20, 22))
    # The twelve entry cards pinned up in rows, each with its number in red, and the blue ribbon below.
    for i in range(12):
        col, row = i % 4, i // 4
        x, y = 1 + col * 4, 17 + row * 4
        rect(board, x, y, x + 3, y + 3, (240, 236, 222))
        px(board, x + 1, y + 1, (168, 50, 44))
        if i >= 9:
            px(board, x, y + 1, (168, 50, 44))
    ellipse(board, (10, 28, 14, 32), (50, 90, 200))
    px(board, 12, 30, (240, 220, 120))
    rect(board, 11, 31, 12, 32, (40, 70, 170))
    rect(board, 0, 0, 16, 1, (90, 60, 30))
    rect(board, 0, 31, 16, 32, (90, 60, 30))
    rect(board, 0, 0, 1, 32, (90, 60, 30))
    rect(board, 15, 0, 16, 32, (90, 60, 30))
    out['costume_board_top'] = board[:16].copy()
    out['costume_board_bottom'] = board[16:].copy()
    return {k: Image.fromarray(np.clip(v, 0, 255).astype(np.uint8), 'RGBA') for k, v in out.items()}


def models():
    iron, dark, spruce, log = 'minecraft:block/iron_block', 'minecraft:block/black_concrete', 'minecraft:block/spruce_planks', 'minecraft:block/stripped_spruce_log'
    out = {}
    for name, tex in (('costume_bulbs', 'costume_bulbs'), ('costume_bulbs_off', 'costume_bulbs_off')):
        els = [element((0, 13, 7.5), (16, 13.5, 8.5), {'all': '#wire'})]
        for i, x in enumerate((1.5, 6.5, 11.5)):
            els.append(element((x + .5, 12, 7.25), (x + 1.5, 13, 8.75), {'all': '#wire'}))
            u, v = ((0, 0), (8, 0), (0, 8))[i]
            bulb = element((x, 10, 7), (x + 2, 12, 9), {'all': '#bulb'})
            for f in bulb['faces'].values():
                f['uv'] = [u + 1, v + 1, u + 7, v + 7]
            els.append(bulb)
        out[name] = {'textures': {'wire': dark, 'bulb': T + tex, 'particle': T + tex}, 'elements': els}
    for name, front in (('costume_radio', 'costume_radio_front'), ('costume_radio_off', 'costume_radio_front_off')):
        out[name] = {'textures': {'front': T + front, 'side': T + 'costume_radio_side', 'dark': dark, 'iron': iron, 'particle': T + 'costume_radio_side'},
                     'elements': [element((3, 0, 5), (13, 8, 11), {'north': '#front', 'south': '#side', 'east': '#side', 'west': '#side', 'up': '#side', 'down': '#side'}),
                                  element((5, 8, 7.5), (11, 9, 8.5), {'all': '#dark'}),
                                  element((11, 8, 9), (11.5, 15, 9.5), {'all': '#iron'})]}
    for f in out['costume_radio']['elements'][0]['faces'].values():
        f['uv'] = [0, 0, 16, 16]
    for f in out['costume_radio_off']['elements'][0]['faces'].values():
        f['uv'] = [0, 0, 16, 16]
    out['costume_apple_tub'] = {'textures': {'side': T + 'costume_tub_side', 'top': T + 'costume_tub_top', 'particle': T + 'costume_tub_side'},
                                'elements': [element((1, 0, 1), (15, 9, 15), {'north': '#side', 'south': '#side', 'east': '#side', 'west': '#side', 'down': '#side', 'up': '#top'}, [0, 0, 16, 16])]}
    out['costume_candy_bowl'] = {'textures': {'side': T + 'costume_pail_side', 'face': T + 'costume_pail_face', 'candy': T + 'costume_candy', 'dark': dark, 'particle': T + 'costume_pail_side'},
                                 'elements': [element((4, 0, 4), (12, 6, 12), {'north': '#face', 'south': '#side', 'east': '#side', 'west': '#side', 'down': '#side', 'up': '#candy'}, [0, 0, 16, 16]),
                                              element((7.5, 6, 3.5), (8.5, 9, 4), {'all': '#dark'}), element((7.5, 6, 12), (8.5, 9, 12.5), {'all': '#dark'}),
                                              element((7.5, 8.5, 4), (8.5, 9, 12), {'all': '#dark'})]}

    def board(half, top):
        h = 14 if top else 16
        els = [element((1, 0, 7), (15, h, 9), {'north': '#art', 'south': '#wood', 'east': '#wood', 'west': '#wood', 'down': '#wood', 'up': '#wood'}, [0, 16 - h, 16, 16] if top else [0, 0, 16, 16]),
               element((2, 0, 9), (3, h, 10), {'all': '#post'}), element((13, 0, 9), (14, h, 10), {'all': '#post'})]
        if top:
            els.append(element((.5, 14, 6.5), (15.5, 15, 9.5), {'all': '#wood'}))
        return {'textures': {'art': T + 'costume_board_' + half, 'wood': spruce, 'post': log, 'particle': spruce}, 'elements': els}
    out['costume_board'] = board('bottom', False)
    out['costume_board_top'] = board('top', True)
    return out


KINDS = ('bulbs', 'radio', 'apple_tub', 'candy_bowl', 'board', 'board_top')


def write():
    folder = ASSETS / 'models/block'
    for name, img in textures().items():
        img.save(ASSETS / 'textures/block' / f'{name}.png')
    for name, model in models().items():
        (folder / f'{name}.json').write_text(json.dumps(model, indent=2) + '\n')
    variants = {}
    for kind in KINDS:
        for lit in ('true', 'false'):
            model = f'costume_{kind}' + ('_off' if lit == 'false' and kind in ('bulbs', 'radio') else '')
            for facing, y in (('north', 0), ('east', 90), ('south', 180), ('west', 270)):
                v = {'model': T + model}
                if y:
                    v['y'] = y
                variants[f'facing={facing},kind={kind},lit={lit}'] = v
    (ASSETS / 'blockstates/costume_prop.json').write_text(json.dumps({'variants': variants}, indent=2) + '\n')


if __name__ == '__main__':
    write()
