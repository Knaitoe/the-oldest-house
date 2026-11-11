#!/usr/bin/env python3
"""Drisking's homes (0.4.456).

The owner, 10 November, note 12 of the playtest: "You could add inflections of personality into the citizens homes. Remember that
this town is sick, that many know what the town does and why and just accept it. Some don't, but they have to bite their tongue."

The four houses on the street had the same room: a table, a stool, an armchair, drawers, a bed. They are four households now
(labyrinth.BorrascaHomes), and what tells them apart are things in the rooms, drawn here at sixteen pixels to the block:

- a crib (two years' sheets), with a toy duck in it;
- a framed school photograph on the wall (and, years on, a snapshot tucked into its corner);
- a wooden cross with a card pushed in behind it;
- a family Bible lying open at its page of births (and, years on, one more line on it);
- a television in a walnut cabinet (and, years on, a flat one standing on top of it);
- a work jacket on a row of hooks (and, years on, a second and a cap);
- a suitcase standing by the wall (and, years on, the dust on it);
- a road atlas lying open (and, years on, shut under a stack of bills);
- a picture frame lying on its face.

One block (borrasca_home) with a kind, a facing and a stage. Models look north; a thing on a wall hangs on the wall behind it; a
thing lying on a table lies with its top to the north. Everything is written from here: textures, models, the block's states and
its name. --check verifies them; --sheet PATH writes a straight-on page of every model for review.
"""
from pathlib import Path
import argparse
import json
import sys
from PIL import Image

sys.path.insert(0, str(Path(__file__).resolve().parent))
import draw_borrasca_town_art as town  # noqa: E402  (its brushes: new, fill, dot, art, boards, element)

ROOT = Path(__file__).resolve().parents[1]
A = ROOT / 'src/main/resources/assets/the_oldest_house'
NS = 'the_oldest_house:'
P = NS + 'block/borrasca_home/'
new, fill, dot, art, boards, element = town.new, town.fill, town.dot, town.art, town.boards, town.element

MAPLE = ('#dcbc86', '#c9a56c', '#b08a52', '#8f6c3a')
WALNUT = ('#7d583a', '#6b4a2e', '#5a3d25', '#432c1a')
CREAM, CREAM_D, INK, FADED = '#efe6cf', '#d9cdb0', '#3d362e', '#a2957c'
GILT, GILT_L, GILT_D = '#b8974a', '#dcc06f', '#8a6c2e'
BLACK, SOOT = '#1b1a19', '#34322f'


def maple():
    return boards(*MAPLE)


def walnut():
    return boards(*WALNUT)


def rail():
    """A crib's side, eleven pixels by seven in the corner of the tile: a rail above and below and five slats, the rest open."""
    im = new()
    for x in range(11):
        dot(im, x, 0, MAPLE[0]); dot(im, x, 6, MAPLE[2])
    for x in (1, 3, 5, 7, 9):
        for y in range(1, 6):
            dot(im, x, y, MAPLE[1])
    return im


def sheet(later):
    """A crib's mattress from above: a white sheet, and over the foot of it a blanket in checks. In the top corner, the duck's colours."""
    im = new()
    fill(im, 2, 3, 13, 13, '#f3efe6')
    for y in range(3, 13):
        dot(im, 2, y, '#dcd6c8')
    a, b, hem = ('#cfe3c4', '#b4d0a6', '#8fb07f') if later else ('#f3e6a8', '#e6d488', '#c9b45e')
    for y in range(3, 13):
        for x in range(7, 13):
            dot(im, x, y, a if (x // 2 + y // 2) % 2 else b)
        dot(im, 7, y, hem)
    # The duck: its body, its lighter head, the side of the head with the eye, and the bill.
    fill(im, 0, 0, 2, 2, '#f2cf4a'); dot(im, 0, 1, '#d9b534'); dot(im, 1, 1, '#d9b534')
    fill(im, 2, 0, 4, 2, '#f7dc6a')
    fill(im, 4, 0, 6, 2, '#f7dc6a'); dot(im, 5, 0, BLACK)
    dot(im, 6, 0, '#e08a2e')
    return im


def portrait(later):
    """A school photograph in a gilt frame: a girl with a band in her hair. Years on, a snapshot of a younger girl is tucked into the
    corner of the frame."""
    im = new()
    fill(im, 3, 2, 13, 13, GILT)
    for x in range(3, 13):
        dot(im, x, 2, GILT_L); dot(im, x, 12, GILT_D)
    for y in range(2, 13):
        dot(im, 12, y, GILT_D)
    rows = ['pppppppp', 'pphhhhpp', 'phrrrrhp', 'phsssshp', 'phessehp', 'phsssshp', 'phsmmshp', 'ppWssWpp', 'ggWWWWgg']
    ink = {'p': '#b7c3ce', 'h': '#5b3d22', 'r': '#b0382e', 's': '#e6bb9c', 'e': '#3b2a1c', 'm': '#c98672', 'g': '#4f7a5a', 'W': '#f1ede2'}
    for j, row in enumerate(rows):
        for i, ch in enumerate(row):
            dot(im, 4 + i, 3 + j, ink[ch])
    if later:
        # A school snapshot with a white border, its corner under the frame's lip.
        fill(im, 3, 9, 6, 13, '#f4f1e8')
        dot(im, 4, 10, '#b98a3e'); dot(im, 5, 10, '#b98a3e'); dot(im, 4, 11, '#e6bb9c'); dot(im, 5, 11, '#e6bb9c'); dot(im, 4, 12, '#a8433a'); dot(im, 5, 12, '#a8433a')
    return im


def cross():
    """Walnut for the cross, and in the corner the card pushed in behind it."""
    im = walnut()
    fill(im, 0, 0, 4, 3, '#f1ede2')
    dot(im, 1, 1, '#8f8a7c'); dot(im, 2, 1, '#8f8a7c'); dot(im, 3, 2, '#d8d2c2'); dot(im, 0, 2, '#d8d2c2')
    return im


def bible(later):
    """The family Bible from above, open at the births. A heading, an old line gone pale with a small cross drawn beside it, the
    newest line dark. Years on, one more dark line under it."""
    im = new()
    fill(im, 2, 3, 14, 13, BLACK)
    fill(im, 3, 4, 13, 12, CREAM)
    for y in range(4, 12):
        dot(im, 7, y, CREAM_D); dot(im, 8, y, '#c4b796')
    for x in (4, 5, 6):
        dot(im, x, 4, '#9c3a30')
    dot(im, 3, 6, FADED); dot(im, 4, 6, FADED)
    for x, y in ((5, 6), (6, 6), (7, 6), (6, 5), (6, 7)):
        dot(im, x, y, INK)              # the cross beside that name
    for x in range(3, 7):
        dot(im, x, 9, INK)
    if later:
        for x in range(3, 6):
            dot(im, x, 11, INK)
    for y in (5, 7, 9, 11):
        for x in range(9, 13):
            dot(im, x, y, FADED)
    dot(im, 12, 11, CREAM)
    # A ribbon, the edges of the leaves, the leather of the boards.
    fill(im, 0, 0, 2, 2, '#a8342c')
    for x in range(16):
        dot(im, x, 14, CREAM_D if x % 2 else CREAM); dot(im, x, 15, BLACK)
    return im


def tv_front():
    """The face of a television in a walnut cabinet: the glass, rounded at its corners; two knobs; the speaker's slats."""
    im = walnut()
    fill(im, 1, 3, 15, 14, WALNUT[1])
    for x in range(1, 15):
        dot(im, x, 3, WALNUT[0]); dot(im, x, 13, WALNUT[3])
    fill(im, 2, 4, 10, 12, '#2d3835')
    for x, y in ((2, 4), (9, 4), (2, 11), (9, 11)):
        dot(im, x, y, WALNUT[2])
    for x, y in ((3, 5), (4, 5), (3, 6), (5, 5)):
        dot(im, x, y, '#56675f')
    for x in range(3, 9):
        dot(im, x, 11, '#222a28')
    fill(im, 11, 4, 14, 12, WALNUT[2])
    dot(im, 12, 5, '#d8d2c2'); dot(im, 12, 6, SOOT); dot(im, 12, 7, '#d8d2c2'); dot(im, 12, 8, SOOT)
    for y in (9, 11):
        for x in range(11, 14):
            dot(im, x, y, '#c4a26a')
    for x in range(11, 14):
        dot(im, x, 10, SOOT)
    return im


def metal():
    """Bright metal on the left and black plastic on the right, for an aerial, hooks, clasps and a set's back."""
    im = new()
    for y in range(16):
        for x in range(8):
            dot(im, x, y, '#d9dadb' if x in (1, 2) else '#aab0b4' if x < 6 else '#7f858a')
        for x in range(8, 16):
            dot(im, x, y, SOOT if (x + y) % 5 == 0 else BLACK)
    return im


def flat():
    """A flat television's face, fifteen pixels by nine: a black edge, dark glass with the room's light across it, one small lamp."""
    im = new()
    fill(im, 0, 0, 15, 9, BLACK)
    fill(im, 1, 1, 14, 8, '#151b20')
    for x, y in ((2, 2), (3, 2), (4, 2), (2, 3), (3, 3), (2, 4), (11, 6), (12, 6), (12, 5)):
        dot(im, x, y, '#27323a')
    dot(im, 13, 8, '#c8352b')
    return im


def jacket():
    """Canvas work jackets as they hang: a collar, sleeves down the sides, a placket and its buttons, a patch over the pocket with a
    name stitched on it. The second is smaller and newer. A cap, and brass."""
    im = new()

    def one(x0, w, h, body, dark, seam):
        mid = x0 + w // 2
        for y in range(h):
            for x in range(x0, x0 + w):
                sleeve = x in (x0, x0 + w - 1)
                if sleeve and (y == 0 or y == h - 1):
                    continue                                    # the shoulder's slope, and the cuff short of the hem
                dot(im, x, y, dark if sleeve else body)
        for x in range(x0 + 1, x0 + w - 1):
            dot(im, x, h - 1, seam)
        for y in range(1, h - 1):
            dot(im, mid, y, seam)
        for y in range(2, h - 1, 2):
            dot(im, mid, y, '#2f2a25')
        for x in range(mid - 1, mid + 2):
            dot(im, x, 0, '#4a3523')                            # corduroy at the collar
        dot(im, mid + 1, 3, '#f1ede2')
        if mid + 2 < x0 + w - 1:
            dot(im, mid + 2, 3, '#b0382e')
        dot(im, x0 + 1, h - 3, seam)
        if w > 5:
            dot(im, x0 + 2, h - 3, seam)
    one(0, 7, 10, '#b08a55', '#8e6c3d', '#7a5a30')
    one(8, 5, 8, '#c8a46e', '#a6824c', '#8a693a')
    # The cap: a crown, a button on top, a bill.
    fill(im, 8, 10, 11, 12, '#2f5a3c'); dot(im, 9, 10, '#3f7550'); fill(im, 8, 12, 11, 13, '#1f3d28')
    fill(im, 14, 0, 16, 2, GILT); dot(im, 14, 0, GILT_L); dot(im, 15, 1, GILT_D)
    return im


def suitcase(dusty):
    """A suitcase standing on its edge. The front: straps, clasps, a tag on a string. Above it the top and at the left the end; in the
    corner the handle. Years on, dust lies on the top and the shoulders."""
    im = new()
    body, dark, strap = '#7a4b2c', '#5c371e', '#3c2413'
    fill(im, 3, 7, 13, 16, body)
    for x in range(3, 13):
        dot(im, x, 7, '#8f5c38'); dot(im, x, 15, dark)
    for y in range(7, 16):
        dot(im, 3, y, dark); dot(im, 12, y, dark)
        dot(im, 5, y, strap); dot(im, 10, y, strap)
    dot(im, 5, 9, GILT_L); dot(im, 10, 9, GILT_L)
    dot(im, 8, 8, '#cfc6ae'); fill(im, 7, 9, 9, 11, CREAM); dot(im, 8, 10, FADED)
    fill(im, 3, 0, 13, 3, body)
    for x in range(3, 13):
        dot(im, x, 0, '#8f5c38'); dot(im, x, 2, dark)
    dot(im, 5, 1, GILT); dot(im, 10, 1, GILT)
    fill(im, 0, 7, 3, 16, dark)
    for y in range(7, 16):
        dot(im, 1, y, body)
    fill(im, 14, 0, 16, 2, strap)
    if dusty:
        dust = '#b3a58c'
        for x in range(3, 13):
            for y in range(0, 3):
                if (x * 3 + y * 5) % 4:
                    dot(im, x, y, dust)
            dot(im, x, 7, dust)
        for x in (4, 6, 7, 9, 11):
            dot(im, x, 8, dust)
        dot(im, 14, 0, dust); dot(im, 15, 0, dust)
    return im


def atlas(later):
    """A road atlas from above. Open: the county and its neighbours, a river, a highway, two towns, and a way out marked in pencil,
    rubbed out, and marked again. Years on: shut, its cover up. At the foot, a pencil and the edges of the leaves."""
    im = new()
    navy = '#27406e'
    if later:
        fill(im, 2, 3, 14, 13, navy)
        for y in range(3, 13):
            dot(im, 2, y, '#3a5a94')
        for x in range(5, 12):
            dot(im, x, 5, '#f1ede2')
        for x in range(6, 11):
            dot(im, x, 6, '#c9a63a')
        for x, y in ((7, 8), (8, 8), (9, 8), (6, 9), (7, 9), (8, 9), (9, 9), (10, 9), (7, 10), (8, 10), (9, 10), (10, 10), (8, 11)):
            dot(im, x, y, '#dfe6ef')
    else:
        fill(im, 1, 2, 15, 14, navy)
        fill(im, 2, 3, 14, 13, CREAM)
        for y in range(3, 13):
            dot(im, 7, y, CREAM_D); dot(im, 8, y, '#c4b796')
        for x in range(2, 14):
            if x not in (7, 8):
                dot(im, x, 8, '#ddd3b8')
        for y in range(3, 13):
            dot(im, 4, y, '#ddd3b8'); dot(im, 11, y, '#ddd3b8')
        fill(im, 10, 3, 13, 6, '#b9d2a8')
        for x, y in ((2, 11), (3, 11), (4, 12), (5, 12), (6, 11), (9, 11), (10, 10), (11, 10), (12, 11), (13, 11)):
            dot(im, x, y, '#5b86bd')
        for x, y in ((2, 4), (3, 4), (4, 5), (5, 5), (6, 6), (9, 7), (10, 7), (11, 8), (12, 9), (13, 9)):
            dot(im, x, y, '#c2463a')
        dot(im, 4, 5, BLACK); dot(im, 11, 8, BLACK)
        # The way out: marked, rubbed to a smear, and marked again beside it.
        for x, y in ((5, 6), (5, 7), (6, 8), (6, 9)):
            dot(im, x, y, '#cfc6b2')
        for x, y in ((5, 4), (6, 4), (6, 3), (3, 6), (3, 7), (2, 8)):
            dot(im, x, y, '#5c5a57')
    for x in range(8):
        dot(im, x, 14, '#e2b93b')
    dot(im, 0, 14, '#d98a94'); dot(im, 1, 14, '#aab0b4'); dot(im, 6, 14, '#d9b98a'); dot(im, 7, 14, SOOT)
    for x in range(16):
        dot(im, x, 15, CREAM_D if x % 2 else CREAM)
    for x in range(12, 16):
        dot(im, x, 14, navy)
    return im


def bills():
    """Two envelopes from above, eight pixels by five each: a window, a return address; the second with a red band across its top."""
    im = new()
    for x0, red in ((0, False), (8, True)):
        fill(im, x0, 0, x0 + 8, 5, '#f4f1e8')
        for x in range(x0, x0 + 8):
            dot(im, x, 4, '#d8d2c2')
        if red:
            for x in range(x0, x0 + 8):
                dot(im, x, 0, '#b0382e')
        else:
            dot(im, x0 + 1, 1, FADED); dot(im, x0 + 2, 1, FADED)
        fill(im, x0 + 1, 2, x0 + 5, 4, '#aebdca'); dot(im, x0 + 2, 3, '#5d6b78'); dot(im, x0 + 3, 3, '#5d6b78')
        dot(im, x0 + 6, 1, '#7d8fa6')
    return im


def frame_back():
    """The back of a picture frame lying on its face: gilt at its edge, brown paper, two turn-buttons and the folded strut."""
    im = new()
    fill(im, 5, 4, 11, 12, GILT_D)
    fill(im, 6, 5, 10, 11, '#8a6a45')
    for y in range(6, 10):
        dot(im, 7, y, '#6d5233'); dot(im, 8, y, '#6d5233')
    dot(im, 6, 7, '#aab0b4'); dot(im, 9, 7, '#aab0b4'); dot(im, 7, 10, '#9a7a52'); dot(im, 8, 5, '#9a7a52')
    return im


def textures():
    return {f'block/borrasca_home/{name}.png': im for name, im in {
        'maple': maple(), 'walnut': walnut(), 'rail': rail(), 'sheet_0': sheet(False), 'sheet_1': sheet(True),
        'portrait_0': portrait(False), 'portrait_1': portrait(True), 'cross': cross(), 'bible_0': bible(False), 'bible_1': bible(True),
        'tv_front': tv_front(), 'metal': metal(), 'flat': flat(), 'jacket': jacket(), 'suitcase_0': suitcase(False), 'suitcase_1': suitcase(True),
        'atlas_0': atlas(False), 'atlas_1': atlas(True), 'bills': bills(), 'frame_back': frame_back()}.items()}


def model(elements, tex, particle):
    return {'ambientocclusion': False, 'render_type': 'minecraft:cutout',
            'textures': {**{k: P + v for k, v in tex.items()}, 'particle': P + particle}, 'elements': elements}


def every(uv):
    return {k: uv for k in ('north', 'south', 'east', 'west', 'up', 'down')}


def crib(stage):
    m = {'all': 'm'}
    side = {'north': [0, 0, 11, 7], 'south': [0, 0, 11, 7], 'up': [0, 0, 11, .5], 'down': [0, 6, 11, 6.5]}
    duck = [element([5.5, 8, 9.5], [7.5, 9.6, 11.5], {'all': 's'}, every([0, 0, 2, 2])),
            element([6.7, 9.4, 9.9], [7.9, 10.6, 11.1], {'all': 's'}, {**every([2, 0, 4, 2]), 'north': [4, 0, 6, 2], 'south': [4, 0, 6, 2]}),
            element([7.9, 9.7, 10.2], [8.5, 10.1, 10.8], {'all': 's'}, every([6, 0, 7, 1]))]
    return model([
        element([1, 3, 3], [2, 15, 13], m), element([13, 3, 3], [14, 13, 13], m),
        element([1, 0, 3], [2, 3, 4], m), element([1, 0, 12], [2, 3, 13], m), element([13, 0, 3], [14, 3, 4], m), element([13, 0, 12], [14, 3, 13], m),
        element([2, 6, 3], [13, 13, 3.5], {'north': 'r', 'south': 'r', 'up': 'r', 'down': 'r'}, side),
        element([2, 6, 12.5], [13, 13, 13], {'north': 'r', 'south': 'r', 'up': 'r', 'down': 'r'}, side),
        element([2, 5, 3.5], [13, 6, 12.5], m),
        element([2, 6, 3.5], [13, 8, 12.5], {'all': 's'}),
        element([2.5, 8, 5.5], [5, 8.7, 10.5], {'all': 's'}),
    ] + duck, {'m': 'maple', 'r': 'rail', 's': f'sheet_{stage}'}, 'maple')


def portrait_model(stage):
    return model([element([3, 3, 15], [13, 14, 16], {'north': 'p', 'east': 'p', 'west': 'p', 'up': 'p', 'down': 'p'},
                          {'up': [3, 2, 13, 3], 'down': [3, 12, 13, 13], 'east': [3, 2, 4, 13], 'west': [12, 2, 13, 13]})],
                 {'p': f'portrait_{stage}'}, f'portrait_{stage}')


def cross_model():
    return model([element([7, 2, 15], [9, 14, 16], {'all': 'c'}), element([4, 9, 15], [12, 11, 16], {'all': 'c'}),
                  element([8.5, 3, 15.5], [12.5, 6, 15.9], {'north': 'c', 'up': 'c', 'east': 'c', 'down': 'c'},
                          {'north': [0, 0, 4, 3], 'up': [0, 0, 4, .4], 'down': [0, 2.6, 4, 3], 'east': [0, 0, .4, 3]})],
                 {'c': 'cross'}, 'cross')


def bible_model(stage):
    b = {'all': 'b'}
    edge = {'north': [3, 14, 8, 15], 'south': [3, 14, 8, 15], 'east': [4, 14, 12, 15], 'west': [4, 14, 12, 15]}
    board = {k: [2, 15, 14, 15.5] for k in ('north', 'south', 'east', 'west')}
    return model([element([2, 0, 3], [14, .5, 13], b, board),
                  element([3, .5, 4], [8, 1.5, 12], b, edge), element([8, .5, 4], [13, 1.5, 12], b, edge),
                  element([7.6, 1.5, 4], [8.4, 1.6, 13.6], b, every([0, 0, 1, 1]))],
                 {'b': f'bible_{stage}'}, f'bible_{stage}')


def tv_model(stage):
    w = {'all': 'w'}
    silver, black = every([1, 0, 2, 8]), every([9, 0, 10, 2])
    parts = [element([1, 2, 5], [15, 13, 14], {'north': 'f', 'south': 'w', 'east': 'w', 'west': 'w', 'up': 'w', 'down': 'w'}),
             element([2, 0, 6], [3, 2, 7], w), element([13, 0, 6], [14, 2, 7], w), element([2, 0, 12], [3, 2, 13], w), element([13, 0, 12], [14, 2, 13], w)]
    if stage == 0:
        parts += [element([7, 13, 9], [9, 14, 11], {'all': 'x'}, black)]
        for angle in (22.5, -22.5):
            parts.append(element([7.8, 14, 9.8], [8.2, 21, 10.2], {'all': 'x'}, silver, {'origin': [8, 14, 10], 'axis': 'z', 'angle': angle}))
    else:
        dark = every([9, 0, 15, 9])
        parts += [element([5, 13, 8.5], [11, 13.5, 11.5], {'all': 'x'}, every([9, 0, 15, 3])),
                  element([7, 13.5, 9.5], [9, 15, 10.5], {'all': 'x'}, every([9, 0, 11, 2])),
                  element([.5, 15, 9.5], [15.5, 24, 10.5], {'north': 'g', 'south': 'x', 'east': 'x', 'west': 'x', 'up': 'x', 'down': 'x'},
                          {**dark, 'north': [0, 0, 15, 9], 'east': [9, 0, 10, 9], 'west': [9, 0, 10, 9], 'up': [9, 0, 15, 1], 'down': [9, 0, 15, 1]})]
    return model(parts, {'f': 'tv_front', 'w': 'walnut', 'x': 'metal', 'g': 'flat'}, 'walnut')


def jacket_model(stage):
    brass, cloth, newer = every([14, 0, 16, 2]), every([1, 5, 2, 6]), every([9, 5, 10, 6])
    parts = [element([1, 12, 15], [15, 14, 16], {'all': 'w'})]
    for x in (3, 7.5, 12):
        parts.append(element([x, 11, 14], [x + 1, 12.5, 15], {'all': 'j'}, brass))
    parts.append(element([0, 2, 14], [7, 12, 15], {'all': 'j'}, {**cloth, 'north': [0, 0, 7, 10]}))
    if stage:
        parts.append(element([10, 4, 14], [15, 12, 15], {'all': 'j'}, {**newer, 'north': [8, 0, 13, 8]}))
        parts.append(element([6.5, 9.5, 13.6], [9.5, 12.5, 15], {'all': 'j'}, {**every([8, 11, 9, 12]), 'north': [8, 10, 11, 13]}))
    return model(parts, {'w': 'walnut', 'j': 'jacket'}, 'jacket')


def suitcase_model(stage):
    s = {'all': 's'}
    return model([element([3, 0, 6.5], [13, 9, 9.5], s, {'up': [3, 0, 13, 3], 'down': [3, 0, 13, 3], 'east': [0, 7, 3, 16], 'west': [0, 7, 3, 16]}),
                  element([6, 9, 7.5], [10, 10.5, 8.5], s, every([14, 0, 16, 2]))],
                 {'s': f'suitcase_{stage}'}, f'suitcase_{stage}')


def atlas_model(stage):
    a = {'all': 'a'}
    leaves = {k: [2, 15, 14, 16] for k in ('north', 'south', 'east', 'west')}
    cover = {k: [12, 14, 16, 15] for k in ('north', 'south', 'east', 'west')}
    if stage == 0:
        return model([element([1, 0, 2], [15, .4, 14], a, cover),
                      element([2, .4, 3], [8, 1, 13], a, leaves), element([8, .4, 3], [14, 1, 13], a, leaves),
                      element([3, 1, 10.4], [11, 1.6, 11.2], a, every([0, 14, 8, 15]))], {'a': 'atlas_0'}, 'atlas_0')
    return model([element([2, 0, 3], [14, 1.6, 13], a, leaves),
                  element([3, 1.6, 4.5], [11, 1.9, 9.5], {'all': 'e'}, {**every([0, 4, 8, 5]), 'up': [0, 0, 8, 5]}),
                  element([5.5, 1.9, 6], [13.5, 2.2, 11], {'all': 'e'}, {**every([8, 4, 16, 5]), 'up': [8, 0, 16, 5]})],
                 {'a': 'atlas_1', 'e': 'bills'}, 'atlas_1')


def frame_down_model():
    return model([element([5, 0, 4], [11, 1, 12], {'all': 'f'}, {k: [5, 4, 11, 5] for k in ('north', 'south', 'east', 'west')})],
                 {'f': 'frame_back'}, 'frame_back')


KINDS = ['crib', 'portrait', 'cross', 'bible', 'tv', 'jacket', 'suitcase', 'atlas', 'frame_down']
STAGES = {'cross': 1, 'frame_down': 1}
FACING_Y = {'north': 0, 'east': 90, 'south': 180, 'west': 270}
NAME = {'block.the_oldest_house.borrasca_home': 'Household Thing'}


def models():
    m = {}
    for stage in range(2):
        m[f'crib_{stage}'] = crib(stage); m[f'portrait_{stage}'] = portrait_model(stage); m[f'bible_{stage}'] = bible_model(stage)
        m[f'tv_{stage}'] = tv_model(stage); m[f'jacket_{stage}'] = jacket_model(stage); m[f'suitcase_{stage}'] = suitcase_model(stage)
        m[f'atlas_{stage}'] = atlas_model(stage)
    m['cross_0'] = cross_model(); m['frame_down_0'] = frame_down_model()
    return m


def files():
    out = {f'models/block/borrasca_home/{key}.json': obj for key, obj in models().items()}
    variants = {}
    for kind in KINDS:
        for facing, y in FACING_Y.items():
            for stage in range(2):
                v = {'model': NS + f'block/borrasca_home/{kind}_{stage if stage < STAGES.get(kind, 2) else 0}'}
                if y:
                    v['y'] = y
                variants[f'facing={facing},kind={kind},stage={stage}'] = v
    out['blockstates/borrasca_home.json'] = {'variants': variants}
    return out


def seen(obj, view, images, scale=10, tall=26):
    """One side of a model, straight on, as its faces are drawn: for review. Turned parts are left out."""
    face, lie, near = town.VIEWS[view]
    page = Image.new('RGBA', (16 * scale, tall * scale), (0, 0, 0, 0))
    boxes = []
    for e in obj['elements']:
        if face in e['faces'] and 'rotation' not in e:
            lo, hi = list(e['from']), list(e['to'])
            boxes.append((near(lo, hi), lie(lo, hi), e['faces'][face], (lo, hi)))
    for _, (x0, x1, top, bottom), f, (lo, hi) in sorted(boxes, key=lambda b: b[0]):
        if view == 'up':
            top, bottom = tall - lo[2], tall - hi[2]
        tex = images['block/borrasca_home/' + obj['textures'][f['texture'][1:]].split('borrasca_home/')[1] + '.png']
        u0, v0, u1, v1 = f.get('uv') or {'north': [16 - hi[0], 16 - hi[1], 16 - lo[0], 16 - lo[1]], 'south': [lo[0], 16 - hi[1], hi[0], 16 - lo[1]],
                                         'east': [16 - hi[2], 16 - hi[1], 16 - lo[2], 16 - lo[1]], 'west': [lo[2], 16 - hi[1], hi[2], 16 - lo[1]],
                                         'up': [lo[0], lo[2], hi[0], hi[2]]}[face]
        left, right, y0, y1 = round(x0 * scale), round(x1 * scale), round((tall - top) * scale), round((tall - bottom) * scale)
        for py in range(y0, y1):
            for qx in range(left, right):
                u = u0 + (qx + .5 - left) / max(1, right - left) * (u1 - u0); v = v0 + (py + .5 - y0) / max(1, y1 - y0) * (v1 - v0)
                c = tex.getpixel((min(15, max(0, int(u))), min(15, max(0, int(v)))))
                if c[3] and 0 <= qx < page.width and 0 <= py < page.height:
                    page.putpixel((qx, py), c)
    return page


def review(path, images):
    built = models(); scale = 10; views = ('north', 'east', 'up')
    page = Image.new('RGBA', (len(built) * 17 * scale, len(views) * 27 * scale), (58, 62, 70, 255))
    for col, key in enumerate(built):
        for row, view in enumerate(views):
            page.alpha_composite(seen(built[key], view, images, scale), (col * 17 * scale + 5, row * 27 * scale + 5))
    page.save(path)


def main():
    args = argparse.ArgumentParser(); args.add_argument('--check', action='store_true'); args.add_argument('--sheet'); args = args.parse_args()
    images, data, built = textures(), files(), models()
    for kind in KINDS:
        for stage in range(STAGES.get(kind, 2)):
            assert f'{kind}_{stage}' in built, (kind, stage)
    for key, obj in built.items():
        for tex in obj['textures'].values():
            assert f'block/borrasca_home/{tex.split("borrasca_home/")[1]}.png' in images, (key, tex)
    for rel, im in images.items():
        assert im.size == (16, 16), rel
    if args.sheet:
        review(args.sheet, images); return
    for rel, im in images.items():
        path = A / 'textures' / rel
        if args.check:
            with Image.open(path) as actual:
                assert actual.convert('RGBA').tobytes() == im.tobytes(), rel
        else:
            path.parent.mkdir(parents=True, exist_ok=True); im.save(path, optimize=True)
    for rel, obj in data.items():
        path = A / rel
        if args.check:
            assert json.loads(path.read_text()) == obj, rel
        else:
            path.parent.mkdir(parents=True, exist_ok=True); path.write_text(json.dumps(obj, indent=2) + '\n')
    lang_path = A / 'lang/en_us.json'; lang = json.loads(lang_path.read_text())
    for key, name in NAME.items():
        if args.check:
            assert lang.get(key) == name, key
        else:
            lang[key] = name
    if not args.check:
        lang_path.write_text(json.dumps(lang, indent=2, ensure_ascii=False) + '\n')
    stale = sorted(p.name for folder in ('textures', 'models') for p in (A / folder / 'block/borrasca_home').glob('*')
                   if f'{folder}/block/borrasca_home/{p.name}' not in {('textures/' + r) for r in images} | set(data))
    assert not stale, stale
    print(('Verified' if args.check else 'Drew') + f" Drisking's homes: {len(images)} textures, {len(data)} models and states.")


if __name__ == '__main__':
    main()
