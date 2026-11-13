#!/usr/bin/env python3
"""The Pell house's people's things (docs/PELL_HOUSE_PEOPLE_0_4_524.md): what two of them have in their hands. Tilly's long cane,
and Gail's folder. Drawn at sixteen pixels to the block. The people themselves are painted by tools/paint_character_skins.py.

0.4.526 (docs/PELL_HOUSE_DINNER_0_4_526.md): June's dinner things. Her plates, seen from above as they lie on the table: laid, served
(sausages, mash with the gravy on it, peas) and scraped; and her saucepan, in her hand while she serves, with the spoon in it.

0.4.527 (docs/PELL_HOUSE_BEDTIME_0_4_527.md): June's hairbrush, in her hand while she brushes Tilly's hair at bedtime.

0.4.530 (docs/PELL_HOUSE_NIGHT_0_4_530.md): what June carries out at night: her battery lantern, the tin of paint by its wire
handle, and the brush she paints the line with. The brush is drawn flat (0.4.566: the hairbrush is solid now). The lantern and the tin hang from her
hand, upright whichever way she turns, so they are built from the things' own block art (tools/draw_pell_things.py), not drawn here:
the lantern is the one she sets down on the grass, its globe lit, and the tin is the one from the bench with its wire handle up.

0.4.531 (docs/PELL_HOUSE_MORNING_0_4_531.md): breakfast as June lays it, from above on the same plate: a fried egg and two slices of
toast.

0.4.533 (docs/PELL_HOUSE_WEEK_0_4_533.md): the pool's lane rope, a week in: a block's length of it at a time, two floats on the rope,
and where one float is missing the bare rope.

0.4.534 (docs/PELL_HOUSE_KNIFE_0_4_534.md): the kitchen knife in Callum's hand at the sink in the night, held as the brushes are.

0.4.538 (docs/PELL_HOUSE_LAST_NIGHT_0_4_538.md): the scrubbing brush from across the laundry's bucket, in the reader's hand on the
last night, held as the brushes are.

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

ITEMS = ('pell_cane', 'pell_folder', 'pell_plate', 'pell_dinner', 'pell_plate_scraped', 'pell_saucepan', 'pell_hairbrush',
         'pell_lantern', 'pell_tin', 'pell_paintbrush', 'pell_breakfast', 'pell_receiver', 'pell_lane', 'pell_lane_gap', 'pell_knife',
         'pell_scrub', 'pell_lanyard', 'pell_cane_tip', 'pell_page', 'pell_note')
NAMES = {'item.the_oldest_house.pell_cane': 'Long Cane', 'item.the_oldest_house.pell_folder': 'Folder',
         'item.the_oldest_house.pell_plate': 'Plate', 'item.the_oldest_house.pell_dinner': 'Dinner',
         'item.the_oldest_house.pell_plate_scraped': 'Plate', 'item.the_oldest_house.pell_saucepan': 'Saucepan',
         'item.the_oldest_house.pell_hairbrush': 'Hairbrush', 'item.the_oldest_house.pell_lantern': 'Lantern',
         'item.the_oldest_house.pell_tin': 'Paint Tin', 'item.the_oldest_house.pell_paintbrush': 'Paintbrush',
         'item.the_oldest_house.pell_breakfast': 'Breakfast', 'item.the_oldest_house.pell_receiver': 'Receiver',
         'item.the_oldest_house.pell_lane': 'Lane Rope', 'item.the_oldest_house.pell_lane_gap': 'Lane Rope',
         'item.the_oldest_house.pell_knife': 'Kitchen Knife', 'item.the_oldest_house.pell_scrub': 'Scrubbing Brush',
         'item.the_oldest_house.pell_lanyard': 'Lanyard', 'item.the_oldest_house.pell_cane_tip': 'Cane Tip',
         'item.the_oldest_house.pell_page': 'Last Page', 'item.the_oldest_house.pell_note': 'Taped Note'}


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


def breakfast():
    """0.4.531: breakfast, laid. A fried egg at the far side: the white spread thin, lit toward the upper left and frilled brown where
    its edge caught the pan, the yolk standing up out of it with a bright point on it. Two slices of toast at the near side, one half
    over the other: the crust darker round each, the bread lit along its top and left, butter melted into the top slice. Each throws a
    little shadow on the plate."""
    im = plate()
    egg = set()
    for y in range(16):
        for x in range(16):
            dx, dy = x + .5 - 10.4, y + .5 - 5.6
            if math.hypot(dx, dy * 1.12) <= 2.75 + .35 * math.sin(math.atan2(dy, dx) * 3 + 1):
                egg.add((x, y))
    slices = [(2, 6), (4, 9)]
    toast = [{(x, y) for x in range(x0, x0 + 5) for y in range(y0, y0 + 5)} for x0, y0 in slices]
    # Shadows on the plate, down and to the right of each.
    for cells in (egg, *toast):
        for (x, y) in cells:
            if (x + 1, y + 1) not in cells and im.getpixel((x + 1, y + 1))[3]:
                dot(im, x + 1, y + 1, '#d6d0c3')
    for (x, y) in egg:
        dx, dy = x + .5 - 10.4, y + .5 - 5.6
        l = lit(dx, dy)
        if edge(egg, x, y) and l < .2:
            dot(im, x, y, '#c99a58' if (x + y) % 2 else '#b9853f')
        else:
            dot(im, x, y, '#fdfcf7' if l > .35 else '#f3f0e6' if l > -.3 else '#e2dccd')
    for y in range(16):
        for x in range(16):
            dx, dy = x + .5 - 10.0, y + .5 - 5.2
            if math.hypot(dx, dy) <= 1.45:
                l = lit(dx, dy)
                dot(im, x, y, '#ffc85a' if l > .3 else '#f2a92e' if l > -.4 else '#d98a1c')
    dot(im, 9, 4, '#fff1c4')
    for i, cells in enumerate(toast):
        x0, y0 = slices[i]
        for (x, y) in cells:
            if x in (x0, x0 + 4) or y in (y0, y0 + 4):
                tone = '#a8692f' if x == x0 or y == y0 else '#7f4a1e'
            else:
                toward = -(x - x0 - 2) - (y - y0 - 2)
                tone = '#ebbd70' if toward >= 2 else '#dca456' if toward >= 0 else '#c98d42'
            dot(im, x, y, tone)
    # Butter melted into the top slice, and a crumb or two.
    for x, y in ((6, 11), (7, 11), (6, 12)):
        dot(im, x, y, '#f6d98a')
    dot(im, 7, 12, '#f1cb6e')
    for x, y in ((9, 13), (3, 12)):
        dot(im, x, y, '#c48a45')
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


def hairbrush():
    """0.4.566 (owner, of the brush in June's hand at bedtime: "any way to improve that?"; it was this picture drawn flat, held up like
    a paddle above Tilly's head, as big as the girl's head). The sheet for June's hairbrush as a small solid thing (hairbrush_model):
    one texel to a unit of the model, which she holds at half size. The bristle side, a dark cushion set in rows of pale tufts
    (0-4, 0-5); the back of the paddle, oiled wood with its grain, lit from the upper left (4-9, 0-6); its edge (9-11) and its face
    round the cushion (11-16); its top and bottom (0-5, 6-8) and the round of its crown (5-8, 6-7; 8-11, 6-8); the cushion's sides
    and ends (11-12, 6-11; 12-16, 6-7); and the handle, rounded, lit along one side and worn pale where the thumb goes (0-9, 9-16)."""
    im = blank()
    light, mid, deep, dark, edge_ = '#c99a63', '#a56f3c', '#83542b', '#6a421f', '#4a2c14'
    for y in range(5):
        for x in range(4):
            tip = (x + y) % 2 == 0
            dot(im, x, y, ('#f2e8cf' if x + y < 4 else '#d6c7a6') if tip else ('#3d312c' if x + y < 4 else '#2c2320'))
    for y in range(6):
        for x in range(4, 9):
            k = (x - 4) + y * .6
            tone = light if k < 1.5 else mid if k < 3.5 else deep if k < 5.5 else dark
            dot(im, x, y, tone)
        dot(im, 4, y, '#d8ae7c' if y < 4 else light)
    for x in range(4, 9):
        if x % 2 == 1:
            for y in range(1, 5):
                dot(im, x, y, shade(im.getpixel((x, y)), -14))
    for y in range(6):
        dot(im, 9, y, mid if y < 3 else deep); dot(im, 10, y, deep if y < 3 else dark)
        for x in range(11, 16):
            dot(im, x, y, deep if x in (11, 15) or y in (0, 5) else '#2c2320')
    for x in range(5):
        dot(im, x, 6, light if x < 3 else mid); dot(im, x, 7, deep if x < 3 else dark)
    for x in range(5, 8):
        dot(im, x, 6, mid)
    for x in range(8, 11):
        dot(im, x, 6, light); dot(im, x, 7, mid)
    for y in range(6, 11):
        dot(im, 11, y, '#3d312c' if y % 2 else '#d6c7a6')
    for x in range(12, 16):
        dot(im, x, 6, '#d6c7a6' if x % 2 else '#3d312c')
    for y in range(9, 16):
        dot(im, 0, y, light); dot(im, 1, y, mid)
        dot(im, 2, y, mid); dot(im, 3, y, deep)
        if y in (11, 12):
            dot(im, 0, y, '#dcae7a'); dot(im, 2, y, '#b98249')
    for x in range(4, 9):
        for y in range(9, 11):
            dot(im, x, y, mid if (x + y) % 2 else deep)
    dot(im, 4, 9, light); dot(im, 8, 10, edge_)
    return im


def hairbrush_model():
    """0.4.566: June's hairbrush as a small solid thing, drawn from its sheet (hairbrush): the handle up from the bottom of the model,
    the neck, the paddle with the round of its crown, and the cushion of bristles standing out of the paddle's face on the north.
    In her hand its handle is in her fist and the paddle runs on past her knuckles, bent down from her forearm, its bristles turned
    to her hand's underside: with her arm out over Tilly's head the bristles are on the girl's hair. Half size: in her hand it is a
    third of a block long, a hand's length beside her."""
    t = NS + 'item/pell_hairbrush'
    def el(lo, hi, f):
        return {'from': list(lo), 'to': list(hi), 'faces': {k: {'uv': list(v), 'texture': '#b'} for k, v in f.items()}}
    elements = [
        el((7, 0, 7.25), (9, 7.5, 8.75), {'north': (0, 9, 2, 16), 'south': (0, 9, 2, 16), 'east': (2, 9, 4, 16), 'west': (2, 9, 4, 16),
                                          'up': (4, 9, 6, 11), 'down': (4, 9, 6, 11)}),
        el((7.25, 7.5, 7.5), (8.75, 8.5, 8.5), {'north': (6, 9, 8, 10), 'south': (6, 9, 8, 10), 'east': (8, 9, 9, 10), 'west': (8, 9, 9, 10)}),
        el((5.5, 8.5, 7), (10.5, 14.5, 9), {'north': (11, 0, 16, 6), 'south': (4, 0, 9, 6), 'east': (9, 0, 11, 6), 'west': (9, 0, 11, 6),
                                            'down': (0, 6, 5, 8)}),
        el((6.5, 14.5, 7), (9.5, 15.25, 9), {'north': (5, 6, 8, 7), 'south': (5, 6, 8, 7), 'east': (9, 0, 11, 1), 'west': (9, 0, 11, 1),
                                             'up': (8, 6, 11, 8)}),
        el((6, 9, 6), (10, 14, 7), {'north': (0, 0, 4, 5), 'east': (11, 6, 12, 11), 'west': (11, 6, 12, 11), 'up': (12, 6, 16, 7),
                                    'down': (12, 6, 16, 7)})]
    hold = {'rotation': [-130, 0, 0], 'translation': [0, -3.5, -1.7], 'scale': [.5, .5, .5]}
    return {'textures': {'b': t, 'particle': t}, 'elements': elements, 'display': {
        'thirdperson_righthand': hold, 'thirdperson_lefthand': hold,
        'firstperson_righthand': {'rotation': [-20, -10, 0], 'translation': [0, 2, 0], 'scale': [.5, .5, .5]},
        'firstperson_lefthand': {'rotation': [-20, 10, 0], 'translation': [0, 2, 0], 'scale': [.5, .5, .5]},
        'gui': {'rotation': [0, 180, -45], 'translation': [0, 0, 0], 'scale': [.95, .95, .95]},
        'ground': {'translation': [0, 3, 0], 'scale': [.4, .4, .4]},
        'fixed': {'rotation': [0, 180, 0], 'scale': [.75, .75, .75]}}}


BRUSH_TIP, BRUSH_END = (14.5, 1.5), (1.5, 14.5)


def paintbrush():
    """0.4.530: June's paintbrush, held as the hairbrush is: the bristles at the top right, wide and flat, their ends loaded with wet
    white paint and the paint working back into them; the metal band that holds them, a bright line across it; and the wooden handle
    down to the bottom left, rounded, lit along one side, with the hole in its end to hang it by."""
    im = blank()
    ax, ay = BRUSH_END[0] - BRUSH_TIP[0], BRUSH_END[1] - BRUSH_TIP[1]
    length = math.hypot(ax, ay)
    for y in range(16):
        for x in range(16):
            px, py = x + .5 - BRUSH_TIP[0], y + .5 - BRUSH_TIP[1]
            t = (px * ax + py * ay) / length / length
            side = (px * ay - py * ax) / length
            if t < 0 or t > 1:
                continue
            if t < .3:
                half = 1.9 - t * 1.2
                if abs(side) > half:
                    continue
                wet = t < .14
                lit = side < -half * .35
                tone = ('#ffffff' if lit else '#f2efe6' if side < half * .4 else '#d8d4c8') if wet else ('#e8dcc4' if lit else '#c9a979' if side < half * .4 else '#a6865a')
                dot(im, x, y, tone)
            elif t < .4:
                if abs(side) > 1.5:
                    continue
                dot(im, x, y, '#eef2f4' if abs(side) < .5 and side < 0 else '#b8bec4' if side < .7 else '#80868c')
            else:
                half = .95 + (.35 if t > .86 else 0)
                if abs(side) > half:
                    continue
                dot(im, x, y, '#d09a5e' if side < -.3 else '#a0703c' if side < .35 else '#6e4a26')
    # The hole in the handle's end, and paint on the band where the brush has been wiped.
    dot(im, 2, 13, '#3a2414')
    dot(im, 10, 5, '#f4f2ea')
    return im


KNIFE_TIP, KNIFE_END = (14.6, 1.4), (1.6, 14.4)


def knife():
    """0.4.534: the kitchen knife from the block, held as the brushes are: the point at the top right; the blade, broad from the bolster
    and curving up to the point along its edge, steel lit along its back where the light from the upper left falls and a bright line
    where the edge has been sharpened; the bolster, a band of brighter steel; and the black handle down to the bottom left, rounded,
    lit along one side, two rivets in it."""
    im = blank()
    ax, ay = KNIFE_END[0] - KNIFE_TIP[0], KNIFE_END[1] - KNIFE_TIP[1]
    length = math.hypot(ax, ay)
    for y in range(16):
        for x in range(16):
            px, py = x + .5 - KNIFE_TIP[0], y + .5 - KNIFE_TIP[1]
            t = (px * ax + py * ay) / length / length
            side = (px * ay - py * ax) / length
            if t < 0 or t > 1:
                continue
            if t < .56:
                # The spine is straight (side < 0); the edge (side > 0) curves out from the point.
                back, edge = .55, min(1.55, .25 + t * 4.2)
                if side < -back or side > edge:
                    continue
                if side < -back + .5:
                    tone = '#f4f8fa'
                elif side > edge - .55:
                    tone = '#e2eaee'
                elif side < (edge - back) * .35:
                    tone = '#b8c2c8'
                else:
                    tone = '#8e989e'
                dot(im, x, y, tone)
            elif t < .62:
                if abs(side) > 1.45:
                    continue
                dot(im, x, y, '#d6dde0' if side < -.2 else '#a2abb0' if side < .6 else '#6e767a')
            else:
                half = 1.0 + (.2 if t > .9 else 0)
                if abs(side) > half:
                    continue
                dot(im, x, y, '#4c4642' if side < -.3 else '#2a2624' if side < .4 else '#171413')
    # The two rivets in the handle.
    for t in (.72, .88):
        dot(im, int(KNIFE_TIP[0] + ax * t), int(KNIFE_TIP[1] + ay * t), '#c8cfd3')
    return im


def lanyard():
    """0.4.540: June's lanyard, out of her robe's pocket: a cord in a loop gone soft with wear, the clip where its ends meet, and
    three keys on the ring below it: the back door's in brass, the shed padlock's, and the small one that opens the freezer."""
    im = blank()
    cord, light = '#34436a', '#56689a'
    loop = [(5, 1), (6, 1), (7, 1), (8, 1), (9, 1), (10, 1), (4, 2), (11, 2), (3, 3), (12, 3), (3, 4), (12, 4), (3, 5), (12, 5),
            (4, 6), (11, 6), (5, 7), (10, 7), (6, 8), (9, 8)]
    for x, y in loop:
        dot(im, x, y, cord)
    for x, y in ((6, 1), (8, 1), (3, 4), (12, 4), (4, 6)):
        dot(im, x, y, light)
    # The clip, and the split ring under it.
    fill(im, 7, 8, 9, 10, '#8f969a'); dot(im, 7, 8, '#c6cbcf')
    for x, y in ((6, 10), (9, 10), (6, 11), (9, 11), (7, 12), (8, 12)):
        dot(im, x, y, '#a9aeb2')
    # The back door's key, brass, hanging to the left: its bow, and the blade down with its teeth.
    fill(im, 3, 11, 6, 13, '#c49a3c'); dot(im, 3, 11, '#e6c66a'); dot(im, 4, 12, '#7a5a1e')
    for y in (13, 14, 15):
        dot(im, 4, y, '#a47e2a')
    dot(im, 3, 14, '#a47e2a'); dot(im, 3, 15, '#8a6a22')
    # The shed padlock's, steel, straight down.
    fill(im, 7, 13, 9, 14, '#a6acb0'); dot(im, 7, 13, '#d0d4d8')
    dot(im, 8, 14, '#8a9094'); dot(im, 8, 15, '#8a9094'); dot(im, 9, 15, '#7a8084')
    # The freezer's, small, to the right.
    dot(im, 10, 12, '#bcc2c6'); dot(im, 11, 12, '#bcc2c6'); dot(im, 11, 13, '#9aa0a4'); dot(im, 11, 14, '#9aa0a4'); dot(im, 12, 14, '#80868a')
    return im


def cane_tip():
    """0.4.541: Tilly's spare cane tip, the keepsake: a small rubber cap that fits over the end of the cane, seen a little from above.
    Its open top and the dark of the socket inside it; its sides lit from the upper left; ridges round its foot; and the foot worn flat
    and pale on one side, where a tip is dragged along the ground ahead of whoever uses it."""
    im = blank()
    # The rim of the open top, and the socket inside.
    fill(im, 6, 2, 10, 3, '#6a6a70')
    dot(im, 5, 3, '#5a5a60'); fill(im, 6, 3, 10, 4, '#141416'); dot(im, 10, 3, '#38383d')
    fill(im, 5, 4, 11, 5, '#4a4a50'); fill(im, 6, 4, 10, 5, '#56565c')
    # The sides, from the lit edge to the shadowed one.
    for x, colour in zip(range(5, 11), ('#5c5c62', '#4c4c52', '#434349', '#3b3b41', '#333339', '#28282c')):
        fill(im, x, 5, x + 1, 12, colour)
    for y in range(5, 10):
        dot(im, 6, y, '#68686f')
    dot(im, 9, 7, '#202024')
    # The foot: wider, ridged, and worn flat and pale on the side it drags on.
    fill(im, 4, 12, 12, 13, '#46464c')
    for x in range(4, 12, 2):
        dot(im, x, 12, '#2a2a2e')
    fill(im, 4, 13, 12, 14, '#2e2e33')
    fill(im, 9, 12, 12, 13, '#5e5b56'); fill(im, 8, 13, 12, 14, '#77736c')
    return im


SCRUB_TIP, SCRUB_END = (13.4, 2.6), (2.6, 13.4)


def scrub():
    """0.4.538: the scrubbing brush, held as the other brushes are, along the diagonal: its wooden back on the side toward the light,
    the varnish worn pale along its top edge and a groove cut along it for the fingers, outlined dark where it stands against nothing;
    and its bristles on the other side in tufts, pale where they are dry and dark and wet at their ends, worn down short toward the top
    right (the end it has always been pushed on), and a drop of water hanging from them."""
    im = blank()
    ax, ay = SCRUB_END[0] - SCRUB_TIP[0], SCRUB_END[1] - SCRUB_TIP[1]
    length = math.hypot(ax, ay)
    wood, bristle = {}, {}
    for y in range(16):
        for x in range(16):
            px, py = x + .5 - SCRUB_TIP[0], y + .5 - SCRUB_TIP[1]
            t = (px * ax + py * ay) / length / length
            side = (px * ay - py * ax) / length
            end = min(t, 1 - t) * length
            if -2.8 <= side < 0 and end >= .3 and not (end < 1.3 and side < -2.1):
                wood[(x, y)] = (t, side)
            reach = 2.3 + 1.5 * t
            if 0 <= side <= reach and end >= .9:
                bristle[(x, y)] = (t, side, reach)
    shape = set(wood) | set(bristle)
    for (x, y), (t, side) in wood.items():
        if edge(shape, x, y):
            tone = '#4f3218'
        elif side < -1.9:
            tone = '#e0b67e' if t < .55 else '#d2a46b'
        elif -1.5 < side < -.8:
            tone = '#6e4723'  # the finger groove
        elif side < -.8:
            tone = '#b98a52'
        else:
            tone = '#8a5c2e'
        dot(im, x, y, tone)
    for (x, y), (t, side, reach) in bristle.items():
        tuft = int(t * length / 1.5) % 2 == 0
        if side < .35:
            tone = '#3d2a1a'  # the shadow under the back
        elif side > reach - .9:
            tone = '#77705e' if tuft else '#5c584b'  # wet at the ends
        else:
            tone = '#e9dfc3' if tuft else '#bfb08c'
        dot(im, x, y, tone)
    # A drop of water hanging from the bristles, near the long end.
    for x, y, tone in ((7, 14, '#9fc7dd'), (7, 15, '#d9eef7')):
        if (x, y) not in shape:
            dot(im, x, y, tone)
    return im


def lane():
    """0.4.533: the pool's lane rope, one sheet for its parts. A pale float (columns 0-4, rows 0-3: its length, lit along its top,
    shaded along its underside, ringed twice where it was moulded) and its end (6-9, rows 0-3, the corners rounded off); the same
    in blue below it (rows 4-7); and the rope it is threaded on (rows 8-9), two strands twisted, one lit and one in shade."""
    im = blank()
    for y0, tones in ((0, ('#fbfaf6', '#eeece4', '#d8d5ca', '#b9b5a8')), (4, ('#79a6cf', '#4f84b6', '#3a6a9a', '#27507a'))):
        top, body, low, ring = tones
        for x in range(5):
            dot(im, x, y0, top); dot(im, x, y0 + 1, body); dot(im, x, y0 + 2, body); dot(im, x, y0 + 3, low)
        for x in (1, 3):
            dot(im, x, y0 + 1, low); dot(im, x, y0 + 2, ring)
        fill(im, 6, y0, 10, y0 + 4, body); fill(im, 7, y0, 9, y0 + 1, top); dot(im, 7, y0 + 1, top)
        for x, y in ((6, y0), (9, y0), (6, y0 + 3), (9, y0 + 3)):
            dot(im, x, y, low)
        dot(im, 8, y0 + 2, ring)
    for x in range(16):
        dot(im, x, 8, '#e4d8b8' if x % 2 == 0 else '#b8aa86'); dot(im, x, 9, '#9a8c6a' if x % 2 == 0 else '#c8bb98')
    return im


def page():
    """0.4.548: the last page off the shed's wall (PellMark): lined paper written over in pencil, corrections down its margin, a torn
    strip of tape still at its top, and down its right side one word written over and over."""
    im = blank()
    fill(im, 3, 1, 13, 15, '#f8f5ea')
    fill(im, 6, 0, 10, 2, '#e0d6a8')
    for y in range(4, 14, 2):
        for x in range(4, 10):
            if (x + y) % 5:
                dot(im, x, y, '#8a8a8a')
    for y in range(3, 14, 3):
        dot(im, 3, y, '#c03a3a')
    for y in range(3, 15):
        dot(im, 11, y, '#3a3a3a' if y % 3 else '#6a6a6a')
    for y in range(1, 15):
        dot(im, 12, y, '#dcd8cc')
    for x in range(3, 13):
        dot(im, x, 14, '#dcd8cc')
    return im


def note():
    """0.4.548: the note off the freezer's power point (PellMark): a square of paper, tape still along its top, and in marker in two
    blocky lines the words DO NOT, then smaller under them SWITCH OFF."""
    im = blank()
    fill(im, 2, 3, 14, 13, '#fbf6c8')
    fill(im, 1, 2, 15, 4, '#d8c88a')
    for x in (4, 5, 7, 8, 10, 11):
        dot(im, x, 6, '#c03a3a'); dot(im, x, 7, '#c03a3a')
    for x in range(4, 12):
        if x % 3:
            dot(im, x, 9, '#c03a3a')
    for x in range(4, 11, 2):
        dot(im, x, 11, '#c03a3a')
    for y in range(4, 13):
        dot(im, 13, y, '#e4dcae')
    return im


def flat(name, parent='minecraft:item/generated'):
    """A flat thing, from its picture: the plates lie on the table (laid there as item displays, face up) and the saucepan is held."""
    return {'parent': parent, 'textures': {'layer0': NS + 'item/' + name}}


def faces(side, end, texture):
    f = {k: {'uv': list(side), 'texture': texture} for k in ('north', 'south', 'east', 'west')}
    f.update({k: {'uv': list(end), 'texture': texture} for k in ('up', 'down')})
    return f


THING = NS + 'block/pell/thing/'
# In the hand the model's up is ahead; turned a quarter about the hand's width it is up the arm, so a thing hangs upright below the
# fist. Half size; drawn back two to stand under the arm rather than at its front; and down by its height over the middle, less
# one, so its handle is in the bottom of the fist.
HANG = {'rotation': [90, 0, 0], 'scale': [.5, .5, .5]}


def hanging(elements, textures, top):
    """0.4.530: a thing carried hanging from the hand by its handle, whose top is at `top` in the model. The same for either hand."""
    hold = dict(HANG, translation=[0, -2, -(top - 8) * .5 - 1])
    return {'textures': textures, 'elements': elements, 'display': {
        'thirdperson_righthand': hold, 'thirdperson_lefthand': hold,
        'firstperson_righthand': {'rotation': [0, -20, 0], 'translation': [0, 0, 0], 'scale': [.5, .5, .5]},
        'firstperson_lefthand': {'rotation': [0, 20, 0], 'translation': [0, 0, 0], 'scale': [.5, .5, .5]},
        'gui': {'rotation': [30, 225, 0], 'translation': [0, 0, 0], 'scale': [.75, .75, .75]},
        'ground': {'translation': [0, 3, 0], 'scale': [.4, .4, .4]},
        'fixed': {'scale': [.75, .75, .75]}}}


def box(lo, hi, texture, uv=None, glow=False, sides=('north', 'south', 'east', 'west', 'up', 'down')):
    face = {'texture': texture} if uv is None else {'uv': list(uv), 'texture': texture}
    el = {'from': list(lo), 'to': list(hi), 'faces': {k: dict(face) for k in sides}}
    if glow:
        el['neoforge_data'] = {'block_light': 15, 'sky_light': 15}
    return el


def lantern_model():
    """0.4.530: the battery lantern as June carries it, by its handle: the lantern she sets down on the grass (its model in
    tools/draw_pell_things.py), the base, the lit globe, the cap and the handle's arch, with its own light."""
    return hanging([box((4, 0, 4), (12, 2, 12), '#b'), box((5, 2, 5), (11, 8, 11), '#g', glow=True, sides=('north', 'south', 'east', 'west')),
                    box((4, 8, 4), (12, 10, 12), '#b'), box((5, 10, 7), (6, 12, 9), '#b'), box((10, 10, 7), (11, 12, 9), '#b'),
                    box((5, 12, 7), (11, 13, 9), '#b')],
                   {'b': THING + 'lantern_body', 'g': THING + 'lantern_globe', 'particle': THING + 'lantern_globe'}, 13)


def tin_model():
    """0.4.530: the tin of paint as June carries it, by its wire handle: the tin from the bench (its side, the paint run down it, and
    its lid marked LINE), the wire up from its ears and over the lid, and the wooden grip on the wire. The wire is the tin's own
    grey rim; the grip, the brush's handle."""
    w, g = (5, 0, 6, 1), (0, 0, 3, 1)
    return hanging([{'from': [4.5, 0, 4.5], 'to': [11.5, 8, 11.5], 'faces': {
                        'north': {'uv': [4, 8, 11, 16], 'texture': '#n'}, 'south': {'uv': [5, 8, 12, 16], 'texture': '#n'},
                        'east': {'uv': [4, 8, 11, 16], 'texture': '#n'}, 'west': {'uv': [5, 8, 12, 16], 'texture': '#n'},
                        'up': {'texture': '#k'}, 'down': {'uv': [4, 15, 11, 16], 'texture': '#n'}}},
                    box((4, 5, 7.5), (4.5, 9.5, 8.5), '#n', w), box((11.5, 5, 7.5), (12, 9.5, 8.5), '#n', w),
                    box((4, 9.5, 7.5), (12, 10, 8.5), '#n', w), box((6.5, 10, 7.25), (9.5, 11, 8.75), '#w', g)],
                   {'n': THING + 'tin', 'k': THING + 'tin_lid_line', 'w': THING + 'brush', 'particle': THING + 'tin'}, 11)


def receiver_model():
    """0.4.532: the kitchen telephone's receiver as June holds it (PellCall): its mouthpiece, its grip and its earpiece, in the plastic of
    the telephone on the wall (tools/draw_pell_things.py). Its up is the earpiece. Held to her face, her arm is up and forward, so it is
    turned back a third of a right angle about the hand's width to stand upright by her cheek; held out, the same turn tips it toward
    whoever takes it."""
    h = THING + 'phone_handset'
    hold = {'rotation': [-28, 0, 0], 'translation': [0, 0, 0], 'scale': [.55, .55, .55]}
    return {'textures': {'h': h, 'particle': h},
            'elements': [box((6, 1, 7), (10, 5, 9.5), '#h'), box((6.5, 5, 7.5), (9.5, 11, 9), '#h'), box((6, 11, 7), (10, 15, 9.5), '#h')],
            'display': {
                'thirdperson_righthand': hold, 'thirdperson_lefthand': hold,
                'firstperson_righthand': {'rotation': [0, -20, 0], 'translation': [0, 2, 0], 'scale': [.5, .5, .5]},
                'firstperson_lefthand': {'rotation': [0, 20, 0], 'translation': [0, 2, 0], 'scale': [.5, .5, .5]},
                'gui': {'rotation': [30, 225, 0], 'translation': [0, 0, 0], 'scale': [.8, .8, .8]},
                'ground': {'translation': [0, 3, 0], 'scale': [.4, .4, .4]},
                'fixed': {'scale': [.75, .75, .75]}}}


def lane_model(gap):
    """0.4.533: a block's length of the lane rope as it lies across the pool at the waterline (PellWeek): the rope through the middle
    and two floats on it, pale then blue, so that block after block they alternate; with `gap`, the blue one gone and the rope bare
    where it was. Laid as an item display at the water's surface, its middle at the waterline and at its full size."""
    t = NS + 'item/pell_lane'
    def floated(x0, row):
        side, end = (0, row, 5, row + 4), (6, row, 10, row + 4)
        return {'from': [x0, 6.25, 6.25], 'to': [x0 + 5, 9.75, 9.75], 'faces': {
            'north': {'uv': list(side), 'texture': '#l'}, 'south': {'uv': list(side), 'texture': '#l'},
            'up': {'uv': list(side), 'texture': '#l'}, 'down': {'uv': list(side), 'texture': '#l'},
            'east': {'uv': list(end), 'texture': '#l'}, 'west': {'uv': list(end), 'texture': '#l'}}}
    rope = {'from': [0, 7.5, 7.5], 'to': [16, 8.5, 8.5], 'faces': {
        'north': {'uv': [0, 8, 16, 9], 'texture': '#l'}, 'south': {'uv': [0, 9, 16, 10], 'texture': '#l'},
        'up': {'uv': [0, 8, 16, 9], 'texture': '#l'}, 'down': {'uv': [0, 9, 16, 10], 'texture': '#l'},
        'east': {'uv': [0, 8, 1, 9], 'texture': '#l'}, 'west': {'uv': [0, 8, 1, 9], 'texture': '#l'}}}
    elements = [rope, floated(1.5, 0)] + ([] if gap else [floated(9.5, 4)])
    whole = {'rotation': [0, 0, 0], 'translation': [0, 0, 0], 'scale': [1, 1, 1]}
    return {'textures': {'l': t, 'particle': t}, 'elements': elements, 'display': {
        'fixed': whole, 'ground': {'translation': [0, 2, 0], 'scale': [.5, .5, .5]},
        'gui': {'rotation': [30, 225, 0], 'translation': [0, 0, 0], 'scale': [.8, .8, .8]},
        'thirdperson_righthand': {'scale': [.5, .5, .5]}, 'thirdperson_lefthand': {'scale': [.5, .5, .5]},
        'firstperson_righthand': {'scale': [.5, .5, .5]}, 'firstperson_lefthand': {'scale': [.5, .5, .5]}}}


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
            'pell_hairbrush': hairbrush_model(), 'pell_lantern': lantern_model(), 'pell_tin': tin_model(),
            'pell_paintbrush': flat('pell_paintbrush', 'minecraft:item/handheld'), 'pell_breakfast': flat('pell_breakfast'),
            'pell_receiver': receiver_model(), 'pell_lane': lane_model(False), 'pell_lane_gap': lane_model(True),
            'pell_knife': flat('pell_knife', 'minecraft:item/handheld'), 'pell_scrub': flat('pell_scrub', 'minecraft:item/handheld'),
            'pell_lanyard': flat('pell_lanyard'), 'pell_cane_tip': flat('pell_cane_tip'), 'pell_page': flat('pell_page'), 'pell_note': flat('pell_note')}


def main():
    parser = argparse.ArgumentParser(); parser.add_argument('--check', action='store_true'); check = parser.parse_args().check
    images = {'pell_cane': cane(), 'pell_folder': folder(), 'pell_plate': plate(), 'pell_dinner': dinner(), 'pell_plate_scraped': scraped(),
              'pell_saucepan': saucepan(), 'pell_hairbrush': hairbrush(), 'pell_paintbrush': paintbrush(), 'pell_breakfast': breakfast(),
              'pell_lane': lane(), 'pell_knife': knife(), 'pell_scrub': scrub(), 'pell_lanyard': lanyard(), 'pell_cane_tip': cane_tip(), 'pell_page': page(), 'pell_note': note()}
    data = models()
    # 0.4.530: the lantern and the tin are built from the things' block art and have no picture of their own; 0.4.532, the receiver;
    # 0.4.533, the lane rope where a float is missing, which is drawn from the whole rope's sheet.
    built = {'pell_lantern', 'pell_tin', 'pell_receiver', 'pell_lane_gap'}
    assert set(images) | built == set(ITEMS) == set(data) and not set(images) & built
    for name in built:
        assert not (A / 'textures/item' / f'{name}.png').exists(), name
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
    print(f'{"Verified" if check else "Drew"} the Pell house\'s people\'s things: {len(images)} textures, {len(data)} models.')


if __name__ == '__main__':
    main()
