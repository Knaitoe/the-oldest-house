#!/usr/bin/env python3
"""What is in the lodging house's rooms (0.4.550, The Ones Already Inside): the textures, models and states of the lodging_thing block,
at sixteen pixels to the block. Every thing is drawn facing north, its back to the south, and turned by FACING. With --check, it
writes nothing and fails if any file differs from what it would draw, or if its kinds are not LodgingThing's."""
import argparse
import importlib.util
import json
import random
import re
from pathlib import Path

from PIL import Image

_spec = importlib.util.spec_from_file_location('draw_lodging_house', Path(__file__).resolve().parent / 'draw_lodging_house.py')
H = importlib.util.module_from_spec(_spec)
_spec.loader.exec_module(H)
new, fill, dot, speckle, shade, box, model, FACES = H.new, H.fill, H.dot, H.speckle, H.shade, H.box, H.model, H.FACES
ROOT, A, NS = H.ROOT, H.A, H.NS
P = 'thing/'


# ------------------------------------------------------------------------------------------------ textures
def grain(base, line, seed, along_x=True, knots=1):
    im = new(base, seed, 3)
    rnd = random.Random(seed)
    for i in range(0, 16, 3):
        j = i + rnd.randint(0, 1)
        for k in range(16):
            if rnd.random() < .7:
                dot(im, k, j, line) if along_x else dot(im, j, k, line)
    for _ in range(knots):
        x, y = rnd.randrange(2, 14), rnd.randrange(2, 14)
        dot(im, x, y, shade(line, -12)); dot(im, x + 1, y, line)
    return im


def panel_front(base, frame, seed, rows=((1, 7), (9, 15)), cols=((1, 7), (9, 15)), handle=None):
    im = grain(base, shade(base, -14), seed)
    for y0, y1 in rows:
        for x0, x1 in cols:
            for x in range(x0, x1):
                dot(im, x, y0, frame); dot(im, x, y1 - 1, shade(base, 12))
            for y in range(y0, y1):
                dot(im, x0, y, frame); dot(im, x1 - 1, y, shade(base, 12))
    if handle:
        for x, y in handle:
            dot(im, x, y, '#b08d3a')
    return im


def wood_dark():
    return grain('#4a2a1e', '#3a1f16', 61001)


def wood_mid():
    return grain('#6b4a32', '#583c28', 61003)


def deal():
    im = grain('#b89a6a', '#a3865a', 61005)
    speckle(im, 61006, 6, ['#c6aa7a'])
    return im


def deal_side():
    return grain('#a4885a', '#8e744c', 61007, along_x=False)


def iron():
    im = new('#2b2a2c', 61011, 3)
    speckle(im, 61012, 8, ['#3b3a3d', '#232224'])
    return im


def iron_top():
    im = new('#262528', 61013, 2)
    for cx, cy in ((4, 5), (11, 5), (8, 11)):
        for a in range(16):
            import math
            x = round(cx + 2.5 * math.cos(a * math.pi / 8)); y = round(cy + 2.5 * math.sin(a * math.pi / 8))
            dot(im, x, y, '#3d3c40')
    return im


def range_front():
    im = iron()
    # Two oven doors with brass latches, the firebox between, its bars and the glow behind them.
    for x0, x1 in ((1, 6), (10, 15)):
        for x in range(x0, x1):
            dot(im, x, 3, '#3e3d41'); dot(im, x, 11, '#1c1b1d')
        for y in range(3, 12):
            dot(im, x0, y, '#3e3d41'); dot(im, x1 - 1, y, '#1c1b1d')
    dot(im, 5, 7, '#b08d3a'); dot(im, 10, 7, '#b08d3a')
    fill(im, 7, 6, 9, 11, '#8a3a12')
    for y in (7, 9):
        dot(im, 7, y, '#d07a22'); dot(im, 8, y + 1, '#e09a3a')
    for y in range(6, 11):
        dot(im, 6, y, '#141315'); dot(im, 9, y, '#141315')
    fill(im, 0, 13, 16, 14, '#3a393c')
    return im


def marble():
    im = new('#d9d4c8', 61021, 3)
    rnd = random.Random(61022)
    x = 2
    for y in range(16):
        x = max(0, min(15, x + rnd.choice((-1, 0, 1))))
        dot(im, x, y, '#b5b0a6')
        if rnd.random() < .3:
            dot(im, (x + 7) % 16, y, '#c3beb3')
    return im


def porcelain():
    im = new('#e8e6e0', 61025, 2)
    for y in range(16):
        dot(im, 0, y, '#d2d0ca'); dot(im, 15, y, '#d2d0ca')
    return im


def basin_top():
    im = porcelain()
    fill(im, 3, 3, 13, 13, '#c8ccc8')
    fill(im, 4, 4, 12, 12, '#8fa3a8')
    dot(im, 6, 6, '#b9c9cc'); dot(im, 7, 6, '#b9c9cc')
    return im


def glass():
    im = new('#c8d4d4', 61027, 2)
    for i in range(4, 12):
        dot(im, i, 15 - i, '#e6eeee')
    return im


def sheet():
    im = new('#e9e4d8', 61031, 2)
    for x in range(16):
        dot(im, x, 7, '#dcd6c8')
    return im


def blanket():
    im = new('#7d7a72', 61033, 4)
    for x in range(16):
        dot(im, x, 2, '#5e5b54'); dot(im, x, 3, '#5e5b54')
    speckle(im, 61034, 10, ['#88857c', '#706d66'])
    return im


def counterpane():
    im = new('#ece8de', 61035, 2)
    for y in range(1, 16, 4):
        for x in range(1 + (y // 4) % 2 * 2, 16, 4):
            dot(im, x, y, '#dcd7ca')
    return im


def pillow():
    im = new('#efebe2', 61037, 2)
    for x in range(16):
        dot(im, x, 0, '#d8d3c6'); dot(im, x, 15, '#d8d3c6')
    return im


def mattress_worn():
    im = new('#c9c0a8', 61041, 3)
    for x in range(1, 16, 4):
        for y in range(16):
            dot(im, x, y, '#8d8a7c')
    fill(im, 6, 6, 10, 10, '#a49a80')
    dot(im, 7, 7, '#6e6655'); dot(im, 8, 8, '#6e6655')
    return im


def blanket_coarse():
    im = new('#6e6858', 61043, 6)
    speckle(im, 61044, 20, ['#5c5748', '#7c7666'])
    return im


def velvet(base, seed):
    im = new(base, seed, 5)
    speckle(im, seed + 1, 12, [shade(base, 10), shade(base, -10)])
    return im


def card_drawers():
    im = wood_mid()
    for y0 in (1, 5, 9, 13):
        for x0 in (1, 5, 9, 13):
            fill(im, x0, y0, x0 + 3, y0 + 3, '#5a3e2a')
            dot(im, x0 + 1, y0 + 1, '#b08d3a')
    return im


def mirror():
    im = new('#a9b4b6', 61051, 2)
    for i in range(16):
        for k in (0, 5):
            j = i + k
            if 0 <= j < 16:
                dot(im, j, 15 - i, '#c3cdcf')
    return im


def brass():
    im = new('#b08d3a', 61053, 6)
    speckle(im, 61054, 6, ['#d0ad5a'])
    return im


def card():
    im = new('#ece4cc', 61055, 2)
    for y in (5, 8, 11):
        for x in range(3, 13):
            if (x + y) % 5:
                dot(im, x, y, '#b8ae94')
    return im


def stone():
    im = new('#9a958a', 61061, 5)
    speckle(im, 61062, 10, ['#8a857b', '#aaa59a'])
    return im


def sink_top():
    im = stone()
    fill(im, 2, 2, 14, 14, '#7c776d')
    fill(im, 3, 3, 13, 13, '#6c675e')
    dot(im, 8, 10, '#3a3732'); dot(im, 7, 10, '#3a3732')
    return im


def tin():
    im = new('#9c9a94', 61071, 5)
    speckle(im, 61072, 6, ['#b4b2ac', '#86847e'])
    return im


def enamel():
    im = new('#e2e0d8', 61073, 2)
    for i in range(16):
        dot(im, i, 0, '#3a4048'); dot(im, i, 15, '#3a4048')
    return im


def linen():
    im = new('#ddd6c4', 61075, 2)
    for x in range(16):
        dot(im, x, 5, '#c8c0ac'); dot(im, x, 10, '#c8c0ac')
    return im


def oilcloth():
    im = new('#3e4a3c', 61077, 3)
    for y in range(16):
        dot(im, 5, y, '#5a5040'); dot(im, 11, y, '#5a5040')
    return im


def thread():
    im = card()
    for y in range(4, 12):
        for x in range(4, 12):
            if y % 2 == 0:
                dot(im, x, y, '#6a4a3a')
    return im


def jar():
    im = new('#b9c3b9', 61079, 2)
    fill(im, 0, 0, 16, 3, '#6b4a32')
    for y in range(4, 16):
        dot(im, 3, y, '#d5ddd5')
    return im


def plate():
    im = new('#e6e3da', 61081, 2)
    for i in range(16):
        dot(im, i, 0, '#5a6a80'); dot(im, i, 15, '#5a6a80'); dot(im, 0, i, '#5a6a80'); dot(im, 15, i, '#5a6a80')
    return im


def rope():
    im = new('#9c8358', 61083, 4)
    for y in range(16):
        for x in range(16):
            if (x + y) % 4 == 0:
                dot(im, x, y, '#7a6440')
    return im


def crate():
    im = new('#8c7350', 61085, 4)
    for y in (4, 5, 10, 11):
        for x in range(16):
            dot(im, x, y, '#4a3a26' if y in (5, 11) else '#6e5a3c')
    for x, y in ((1, 2), (14, 2), (1, 8), (14, 8), (1, 13), (14, 13)):
        dot(im, x, y, '#3a3a3a')
    return im


def coal():
    im = new('#1a1a1c', 61087, 4)
    speckle(im, 61088, 24, ['#3a3a40', '#2a2a2e', '#4a4a52'])
    return im


def dresser_front():
    im = panel_front('#6b4a32', '#4a3020', 61091, rows=((1, 5), (7, 15)), cols=((1, 8), (8, 15)), handle=((4, 3), (11, 3), (6, 11), (9, 11)))
    return im


def dresser_back():
    im = grain('#5e4230', '#4c3424', 61093, along_x=False)
    return im


def wardrobe(upper):
    im = panel_front('#4a2a1e', '#2e1a12', 61095 + upper, rows=((2, 15),) if not upper else ((3, 15),), cols=((1, 8), (8, 15)),
                     handle=((7, 8), (8, 8)) if upper else ())
    if upper:
        fill(im, 0, 0, 16, 2, '#3a2016')
    return im


def sideboard_front():
    return panel_front('#4a2a1e', '#2e1a12', 61097, rows=((1, 4), (6, 15)), cols=((1, 8), (8, 15)), handle=((4, 2), (11, 2), (7, 10), (8, 10)))


def textures():
    out = {
        'wood_dark': wood_dark(), 'wood_mid': wood_mid(), 'deal': deal(), 'deal_side': deal_side(), 'iron': iron(), 'iron_top': iron_top(),
        'range_front': range_front(), 'marble': marble(), 'porcelain': porcelain(), 'basin_top': basin_top(), 'glass': glass(), 'sheet': sheet(),
        'blanket': blanket(), 'counterpane': counterpane(), 'pillow': pillow(), 'mattress_worn': mattress_worn(), 'blanket_coarse': blanket_coarse(),
        'velvet': velvet('#5a3a44', 61045), 'velvet_seat': velvet('#6a4650', 61047), 'card_drawers': card_drawers(), 'mirror': mirror(),
        'brass': brass(), 'card': card(), 'stone': stone(), 'sink_top': sink_top(), 'tin': tin(), 'enamel': enamel(), 'linen': linen(),
        'oilcloth': oilcloth(), 'thread': thread(), 'jar': jar(), 'plate': plate(), 'rope': rope(), 'crate': crate(), 'coal': coal(),
        'dresser_front': dresser_front(), 'dresser_back': dresser_back(), 'wardrobe_low': wardrobe(0), 'wardrobe_high': wardrobe(1),
        'sideboard_front': sideboard_front(),
    }
    return {f'block/lodging/{P}{k}.png': v for k, v in out.items()}


# ------------------------------------------------------------------------------------------------ models
TEX = {k: P + k for k in ('wood_dark', 'wood_mid', 'deal', 'deal_side', 'iron', 'iron_top', 'range_front', 'marble', 'porcelain', 'basin_top',
                          'glass', 'sheet', 'blanket', 'counterpane', 'pillow', 'mattress_worn', 'blanket_coarse', 'velvet', 'velvet_seat',
                          'card_drawers', 'mirror', 'brass', 'card', 'stone', 'sink_top', 'tin', 'enamel', 'linen', 'oilcloth', 'thread', 'jar',
                          'plate', 'rope', 'crate', 'coal', 'dresser_front', 'dresser_back', 'wardrobe_low', 'wardrobe_high', 'sideboard_front')}
TEX['brick'] = 'brick'


def m(elements, particle):
    return model(elements, TEX, particle=TEX[particle])


def legs(points, top, tex, w=1):
    return [box((x, 0, z), (x + w, top, z + w), tex) for x, z in points]


def bed(stage):
    head = stage % 2 == 0
    cover, sheet_ = ('blanket', 'sheet') if stage < 2 else ('counterpane', 'counterpane')
    els = [box((0, 3, 0), (1, 5, 16), 'iron'), box((15, 3, 0), (16, 5, 16), 'iron')]
    if head:
        els += [box((0, 0, 15), (1, 16, 16), 'iron'), box((15, 0, 15), (16, 16, 16), 'iron'), box((1, 14, 15), (15, 15, 16), 'iron'),
                box((1, 9, 15), (15, 10, 16), 'iron')] + [box((x, 10, 15.25), (x + 1, 14, 15.75), 'iron') for x in (4, 8, 11)]
        els += [box((1, 5, 0), (15, 8, 15), {'up': sheet_, 'north': sheet_, 'south': sheet_, 'east': sheet_, 'west': sheet_, 'down': 'iron'}),
                box((3, 8, 10), (13, 10, 14), 'pillow')]
        els.append(box((0.5, 6, 0), (15.5, 8.5, 9), cover))
        if stage < 2:
            els.append(box((0.5, 8.5, 7.5), (15.5, 8.75, 9.5), 'sheet'))
    else:
        els += [box((0, 0, 0), (1, 11, 1), 'iron'), box((15, 0, 0), (16, 11, 1), 'iron'), box((1, 10, 0), (15, 11, 1), 'iron'),
                box((1, 6, 0), (15, 7, 1), 'iron'), box((1, 5, 1), (15, 8, 16), sheet_), box((0.5, 6, 1), (15.5, 8.5, 16), cover)]
    return m(els, cover)


def cot(stage):
    els = [box((2, 2, 0), (3, 4, 16), 'iron'), box((13, 2, 0), (14, 4, 16), 'iron'), box((3, 3, 0 if stage else 0), (13, 5, 16 if stage else 15), 'mattress_worn')]
    if stage == 0:
        els += [box((2, 0, 15), (3, 10, 16), 'iron'), box((13, 0, 15), (14, 10, 16), 'iron'), box((3, 8, 15), (13, 9, 16), 'iron')]
    else:
        els += [box((2, 0, 0), (3, 7, 1), 'iron'), box((13, 0, 0), (14, 7, 1), 'iron'), box((3, 6, 0), (13, 7, 1), 'iron'),
                box((4, 5, 3), (12, 7, 9), 'blanket_coarse')]
    return m(els, 'mattress_worn')


def washstand():
    els = legs(((1, 4), (14, 4), (1, 14), (14, 14)), 11, 'wood_mid') + [
        box((1, 3, 4), (15, 4, 15), 'wood_mid'), box((1, 10, 4), (15, 11, 15), 'wood_mid'), box((0, 11, 3), (16, 12, 16), 'marble'),
        box((0, 12, 15), (16, 16, 16), 'marble'), box((4, 12, 6), (12, 14, 13), {'up': 'basin_top', 'north': 'porcelain', 'south': 'porcelain',
                                                                                  'east': 'porcelain', 'west': 'porcelain'}),
        box((6.5, 12.5, 8.5), (9.5, 18.5, 11.5), 'porcelain'), box((9.5, 16, 9.5), (10.5, 18, 10.5), 'porcelain'),
        box((12.5, 12, 5), (14, 15, 6.5), 'glass')]
    return m(els, 'marble')


def chair():
    els = legs(((3, 3), (12, 3), (3, 12), (12, 12)), 9, 'wood_mid') + [
        box((3, 9, 3), (13, 10, 13), 'wood_mid'), box((3, 10, 12), (4, 19, 13), 'wood_mid'), box((12, 10, 12), (13, 19, 13), 'wood_mid'),
        box((4, 17, 12), (12, 19, 13), 'wood_mid'), box((4, 13, 12.25), (12, 14, 12.75), 'wood_mid')]
    return m(els, 'wood_mid')


def armchair():
    els = legs(((1, 1), (14, 1), (1, 14), (14, 14)), 2, 'wood_dark') + [
        box((1, 2, 1), (15, 8, 15), 'velvet'), box((3, 8, 1), (13, 10, 12), 'velvet_seat'), box((1, 8, 1), (3, 12, 15), 'velvet'),
        box((13, 8, 1), (15, 12, 15), 'velvet'), box((1, 8, 12), (15, 20, 15), 'velvet')]
    return m(els, 'velvet')


def settee(half):
    if half == 0:
        els = [box((0, 2, 1), (15, 8, 15), 'velvet'), box((0, 8, 1), (13, 10, 12), 'velvet_seat'), box((13, 8, 1), (15, 12, 15), 'velvet'),
               box((0, 8, 12), (15, 19, 15), 'velvet')] + legs(((14, 1), (14, 14)), 2, 'wood_dark')
    else:
        els = [box((1, 2, 1), (16, 8, 15), 'velvet'), box((3, 8, 1), (16, 10, 12), 'velvet_seat'), box((1, 8, 1), (3, 12, 15), 'velvet'),
               box((1, 8, 12), (16, 19, 15), 'velvet')] + legs(((1, 1), (1, 14)), 2, 'wood_dark')
    return m(els, 'velvet')


def side_table():
    return m([box((3, 11, 3), (13, 12, 13), 'wood_dark'), box((7, 1, 7), (9, 11, 9), 'wood_dark'), box((4, 0, 7), (12, 1, 9), 'wood_dark'),
              box((7, 0, 4), (9, 1, 12), 'wood_dark')], 'wood_dark')


def long_table(stage):
    els = [box((0, 12, 0), (16, 14, 16), {'up': 'wood_dark', 'down': 'wood_dark', 'north': 'wood_dark', 'south': 'wood_dark', 'east': 'wood_dark',
                                          'west': 'wood_dark'})]
    els.append(box((0.5, 9, 0), (1.5, 12, 16), 'wood_dark'))
    if stage == 1:
        els += [box((0, 9, 0.5), (16, 12, 1.5), 'wood_dark'), box((1, 0, 1), (3, 12, 3), 'wood_dark')]
    return m(els, 'wood_dark')


def sideboard(half):
    els = [box((0, 1, 4), (16, 14, 16), {'north': 'sideboard_front', 'south': 'wood_dark', 'east': 'wood_dark', 'west': 'wood_dark', 'up': 'wood_dark',
                                         'down': 'wood_dark'}), box((0, 14, 3), (16, 15, 16), 'wood_dark'), box((0, 15, 15), (16, 18, 16), 'wood_dark')]
    els += legs(((14 if half == 0 else 1, 4), (14 if half == 0 else 1, 14)), 1, 'wood_dark')
    if half == 0:
        els.append(box((10, 15, 9), (13, 17, 12), 'plate'))
    return m(els, 'wood_dark')


def work_table(half):
    x = 13 if half == 0 else 1
    els = [box((0, 13, 1), (16, 14, 15), {'up': 'deal', 'down': 'deal_side', 'north': 'deal_side', 'south': 'deal_side', 'east': 'deal_side',
                                          'west': 'deal_side'})] + legs(((x, 1), (x, 13)), 13, 'deal_side', w=2)
    if half == 0:
        els += [box((3, 14, 4), (10, 16, 10), {'up': 'basin_top', 'north': 'enamel', 'south': 'enamel', 'east': 'enamel', 'west': 'enamel'}),
                box((10, 14, 9), (14, 15, 13), 'linen'), box((10.5, 15, 9.5), (13.5, 16, 12.5), 'linen')]
    else:
        els += [box((2, 14, 5), (12, 15.5, 8), 'oilcloth'), box((6, 14, 4.5), (7, 16, 8.5), 'card'), box((5, 14, 10), (8, 15, 12), 'thread'),
                box((11, 14, 9), (13, 17, 11), 'jar')]
    return m(els, 'deal')


def key_hook(stage):
    els = [box((6, 8, 15), (10, 13, 16), 'wood_dark'), box((7.5, 10, 14), (8.5, 11, 15), 'iron')]
    if stage == 0:
        els += [box((7, 9, 14.2), (9, 11, 14.7), 'iron'), box((7.5, 4, 14.2), (8.5, 9, 14.7), 'iron'), box((8.5, 4, 14.2), (9.5, 6, 14.7), 'iron'),
                box((6.5, 2, 14.4), (9.5, 5, 14.6), 'card')]
    return m(els, 'wood_dark')


def card_index(stage):
    els = legs(((2, 4), (13, 4), (2, 14), (13, 14)), 8, 'wood_mid') + [
        box((1, 8, 3), (15, 16, 16), {'north': 'card_drawers', 'south': 'wood_mid', 'east': 'wood_mid', 'west': 'wood_mid', 'up': 'wood_mid',
                                      'down': 'wood_mid'})]
    if stage == 1:
        els += [box((5, 11, 0), (9, 13, 3), 'wood_mid'), box((5.5, 13, 0.5), (8.5, 13.5, 2.5), 'card')]
    return m(els, 'wood_mid')


def wardrobe_model(upper):
    face = 'wardrobe_high' if upper else 'wardrobe_low'
    els = [box((0, 0, 2), (16, 16, 16), {'north': face, 'south': 'wood_dark', 'east': 'wood_dark', 'west': 'wood_dark', 'up': 'wood_dark',
                                         'down': 'wood_dark'})]
    if upper:
        els.append(box((-0.5, 14, 1.5), (16.5, 16, 16), 'wood_dark'))
    return m(els, 'wood_dark')


def dressing_table():
    els = legs(((0.5, 6.5), (14, 6.5), (0.5, 14), (14, 14)), 8, 'wood_dark', w=1.5) + [
        box((0, 8, 6), (16, 11, 16), 'wood_dark'), box((0, 11, 6), (16, 12, 16), 'wood_dark'), box((2, 12, 13), (3, 20, 14), 'wood_dark'),
        box((13, 12, 13), (14, 20, 14), 'wood_dark'), box((3, 13, 13), (13, 21, 14), {'north': 'mirror', 'south': 'wood_dark', 'east': 'wood_dark',
                                                                                       'west': 'wood_dark', 'up': 'wood_dark', 'down': 'wood_dark'}),
        box((4, 12, 8), (7, 13, 10), 'wood_mid'), box((10, 12, 8), (12, 12.5, 10), 'brass')]
    return m(els, 'wood_dark')


def card_holder(stage):
    els = [box((5, 6, 15.5), (11, 9, 16), 'brass')]
    if stage == 1:
        els.append(box((5.5, 6.5, 15.3), (10.5, 8.5, 15.5), 'card'))
    return m(els, 'brass')


def sink():
    return m([box((1, 0, 6), (4, 8, 15), 'brick'), box((12, 0, 6), (15, 8, 15), 'brick'),
              box((0, 8, 4), (16, 12, 16), {'up': 'sink_top', 'north': 'stone', 'south': 'stone', 'east': 'stone', 'west': 'stone', 'down': 'stone'}),
              box((7, 14, 14), (9, 15, 16), 'brass'), box((7.5, 12.5, 12), (8.5, 15, 14), 'brass')], 'stone')


def range_model(stage):
    if stage == 1:
        return m([box((6, 0, 12), (10, 16, 16), 'iron')], 'iron')
    return m([box((0, 0, 3), (16, 13, 16), {'north': 'range_front', 'south': 'iron', 'east': 'iron', 'west': 'iron', 'up': 'iron', 'down': 'iron'}),
              box((0, 13, 2), (16, 14, 16), {'up': 'iron_top', 'north': 'iron', 'south': 'iron', 'east': 'iron', 'west': 'iron', 'down': 'iron'}),
              box((6, 14, 13), (10, 16, 16), 'iron'), box((3, 14, 5), (8, 18, 10), 'tin'), box((8, 16, 7), (10, 17, 8), 'tin')], 'iron')


def dresser(stage):
    if stage == 0:
        return m([box((0, 0, 4), (16, 13, 16), {'north': 'dresser_front', 'south': 'wood_mid', 'east': 'wood_mid', 'west': 'wood_mid', 'up': 'wood_mid',
                                                 'down': 'wood_mid'}), box((0, 13, 3), (16, 14, 16), 'wood_mid')], 'wood_mid')
    els = [box((0, 0, 15), (16, 16, 16), 'dresser_back'), box((0, 4, 12), (16, 5, 16), 'wood_mid'), box((0, 10, 12), (16, 11, 16), 'wood_mid'),
           box((0, 0, 12), (1, 16, 16), 'wood_mid'), box((15, 0, 12), (16, 16, 16), 'wood_mid'), box((0, 15, 11), (16, 16, 16), 'wood_mid')]
    for x in (2, 5.5, 9, 12.5):
        els.append(box((x, 5, 14), (x + 3, 9, 14.5), {'north': 'plate', 'south': 'plate', 'east': 'plate', 'west': 'plate', 'up': 'plate', 'down': 'plate'}))
    for x in (2.5, 7, 11.5):
        els.append(box((x, 11, 13), (x + 2, 13, 15), 'plate'))
    return m(els, 'wood_mid')


def scullery_table(stage):
    half, tray = stage & 1, stage >> 1 & 1
    x = 13 if half == 0 else 1
    els = [box((0, 13, 1), (16, 14, 15), {'up': 'deal', 'down': 'deal_side', 'north': 'deal_side', 'south': 'deal_side', 'east': 'deal_side',
                                          'west': 'deal_side'})] + legs(((x, 1), (x, 13)), 13, 'deal_side', w=2)
    if tray and half == 0:
        els += [box((3, 14, 4), (13, 14.5, 12), 'tin'), box((5, 14.5, 6), (10, 15, 10), 'tin'), box((10.5, 14.5, 6), (12.5, 16.5, 8), 'tin'),
                box((5, 14.5, 10.5), (10, 14.75, 11), 'tin')]
    return m(els, 'deal')


def shelf():
    return m([box((1, 12, 10), (15, 13, 16), 'wood_mid'), box((2, 9, 14), (3, 12, 16), 'wood_mid'), box((13, 9, 14), (14, 12, 16), 'wood_mid'),
              box((3, 13, 11), (9, 14.5, 15), 'tin'), box((10, 13, 11), (12.5, 15.5, 13.5), 'tin')], 'wood_mid')


def pail():
    return m([box((4, 0, 4), (12, 9, 12), 'tin'), box((3.5, 9, 3.5), (12.5, 10, 12.5), 'tin'), box((7, 10, 7), (9, 10.5, 9), 'tin')], 'tin')


def rope_model():
    return m([box((3, 0, 3), (13, 2, 13), 'rope'), box((12, 0, 7), (16, 1, 8), 'rope')], 'rope')


def crate_model():
    return m([box((1, 0, 1), (15, 12, 15), 'crate')], 'crate')


def coal_model():
    return m([box((0, 0, 0), (16, 3, 16), 'coal'), box((2, 3, 2), (14, 6, 14), 'coal'), box((5, 6, 5), (11, 8, 11), 'coal')], 'coal')


KINDS = ['bed', 'cot', 'washstand', 'chair', 'armchair', 'settee', 'side_table', 'long_table', 'sideboard', 'work_table', 'key_hook', 'card_index',
         'wardrobe', 'dressing_table', 'card_holder', 'sink', 'range', 'dresser', 'scullery_table', 'shelf', 'pail', 'rope', 'crate', 'coal']
DRAW = {'bed': bed, 'cot': cot, 'washstand': lambda s: washstand(), 'chair': lambda s: chair(), 'armchair': lambda s: armchair(), 'settee': settee,
        'side_table': lambda s: side_table(), 'long_table': long_table, 'sideboard': sideboard, 'work_table': work_table, 'key_hook': key_hook,
        'card_index': card_index, 'wardrobe': wardrobe_model, 'dressing_table': lambda s: dressing_table(), 'card_holder': card_holder,
        'sink': lambda s: sink(), 'range': range_model, 'dresser': dresser, 'scullery_table': scullery_table, 'shelf': lambda s: shelf(),
        'pail': lambda s: pail(), 'rope': lambda s: rope_model(), 'crate': lambda s: crate_model(), 'coal': lambda s: coal_model()}
STAGES = {'bed': 4, 'scullery_table': 4, 'cot': 2, 'settee': 2, 'long_table': 2, 'sideboard': 2, 'work_table': 2, 'key_hook': 2, 'card_index': 2,
          'wardrobe': 2, 'card_holder': 2, 'range': 2, 'dresser': 2}
MAX_STAGE = 4
NAME = 'Lodging House Furniture'


def java_kinds():
    text = (ROOT / 'src/main/java/io/github/knaitoe/theoldesthouse/labyrinth/LodgingThing.java').read_text()
    body = re.search(r'enum Kind implements StringRepresentable\s*\{\s*([A-Z_,\s]+);', text).group(1)
    return [k.strip().lower() for k in body.split(',') if k.strip()]


def files():
    out, variants = {}, {}
    for kind in KINDS:
        for stage in range(STAGES.get(kind, 1)):
            out[f'models/block/lodging/thing/{kind}_{stage}.json'] = DRAW[kind](stage)
        for facing, y in H.FACING_Y.items():
            for stage in range(MAX_STAGE):
                drawn = stage if stage < STAGES.get(kind, 1) else 0
                v = {'model': NS + f'block/lodging/thing/{kind}_{drawn}'}
                if y:
                    v['y'] = y
                variants[f'facing={facing},kind={kind},stage={stage}'] = v
    out['blockstates/lodging_thing.json'] = {'variants': variants}
    return out


def main():
    check = argparse.ArgumentParser()
    check.add_argument('--check', action='store_true')
    check = check.parse_args().check
    assert java_kinds() == KINDS, (java_kinds(), KINDS)
    images, data = textures(), files()
    for rel, obj in data.items():
        if rel.startswith('models/'):
            for tex in obj['textures'].values():
                name = tex.replace(NS, '') + '.png'
                assert name in images or (A / 'textures' / name).exists(), (rel, tex)
    for rel, im in images.items():
        assert im.size == (16, 16), rel
        path = A / 'textures' / rel
        if check:
            with Image.open(path) as actual:
                assert actual.convert('RGBA').tobytes() == im.tobytes(), rel
        else:
            path.parent.mkdir(parents=True, exist_ok=True); im.save(path, optimize=True)
    for rel, obj in data.items():
        path = A / rel
        if check:
            assert json.loads(path.read_text()) == obj, rel
        else:
            path.parent.mkdir(parents=True, exist_ok=True); path.write_text(json.dumps(obj, indent=2) + '\n')
    lang_path = A / 'lang/en_us.json'; lang = json.loads(lang_path.read_text())
    key = 'block.the_oldest_house.lodging_thing'
    if check:
        assert lang.get(key) == NAME, key
    else:
        lang[key] = NAME; lang_path.write_text(json.dumps(lang, indent=2, ensure_ascii=False) + '\n')
    print(('Verified' if check else 'Drew') + f' what is in the lodging house: {len(images)} textures, {len(data)} models and states.')


if __name__ == '__main__':
    main()
