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


# 0.4.459, the Garritys' letter (docs/BORRASCA_LETTER_0_4_459.md). A mailbox for every gate on the street, the Garritys' as it is
# on each visit; the corner of an envelope under a door; curtains; and the letter itself, in the pack.
STEEL, STEEL_L, STEEL_D, STEEL_S = '#aeb4b8', '#cfd4d6', '#8a9196', '#6c7378'
RUST, RUST_D, RUST_L = '#8f5f41', '#6e4127', '#a8794f'
RED, RED_D = '#b3261e', '#7f1712'
PRINT = '#4d6480'


def box(rusted):
    """A mailbox's faces. Its side, ten by six, with the name on a paper label under tape; its top, ten by five; its door and its
    back, five by six; and its flag, down and up. Years on with nobody to see to it: rust, the tape gone cloudy, no flag."""
    import random
    im = new()
    for y in range(6):                                         # the side
        for x in range(10):
            dot(im, x, y, STEEL_L if y in (0, 2) else STEEL_D if y == 5 or x in (0, 9) else STEEL)
    for y in range(6, 11):                                     # the top, its ribs at the two ends
        for x in range(10):
            dot(im, x, y, STEEL_D if x in (0, 9) or y in (6, 10) else STEEL_L if y == 8 else STEEL)
    for ox, oy, latch in ((10, 0, True), (10, 6, False)):      # the door, and the back
        for y in range(6):
            for x in range(5):
                if y < 2 and x in (0, 4):
                    continue
                edge = x in (0, 4) or y in (0, 5) or (y == 2 and x in (0, 4))
                dot(im, ox + x, oy + y, STEEL_D if edge else STEEL_L if y == 1 else STEEL)
        if latch:
            dot(im, ox + 2, oy + 2, STEEL_S); dot(im, ox + 2, oy + 3, '#3c4146')
    # The label: white paper, a name in ink, clear tape a little longer than the paper and bright where it catches the light.
    fill(im, 2, 2, 8, 5, '#fbf7ea')
    for x in (3, 4, 6, 7):
        dot(im, x, 3, BLACK)
    dot(im, 1, 3, '#e3e7e8'); dot(im, 8, 3, '#e3e7e8'); dot(im, 2, 2, '#ffffff')
    if not rusted:
        for x in range(3):                                     # the flag down along the shoulder: a short arm and its tab
            dot(im, x, 13, RED)
        dot(im, 4, 12, RED); dot(im, 4, 13, RED_D)
        return im
    rnd = random.Random(459); px = im.load()
    for y in range(12):
        for x in range(15):
            if px[x, y][3] and rnd.random() < .46:
                px[x, y] = shade(rnd.choice((RUST, RUST, RUST_D, RUST_L)))
    for x in range(10):                                        # rust runs along the bottom seam and down from the latch
        dot(im, x, 5, RUST_D)
    for y in range(2, 6):
        dot(im, 12, y, RUST_D)
    # The tape has gone cloudy over the label. Two letters still show through it.
    fill(im, 1, 2, 9, 5, '#d5d1c2')
    dot(im, 3, 3, FADED); dot(im, 6, 3, FADED); dot(im, 1, 2, '#e4e1d6'); dot(im, 8, 4, '#bdb8a6')
    return im


shade = town.shade


def mail():
    """What a mailbox holds after a week: three envelopes from above, a catalogue rolled up, their edges, and the dark of the box."""
    im = new()
    fill(im, 0, 0, 4, 5, CREAM); dot(im, 3, 0, RED); dot(im, 0, 0, PRINT); dot(im, 1, 0, PRINT); dot(im, 1, 2, INK); dot(im, 2, 2, INK); dot(im, 1, 3, INK)
    fill(im, 4, 0, 8, 5, '#f4f3ee'); dot(im, 7, 0, '#3f6fb8'); dot(im, 5, 2, INK); dot(im, 6, 2, INK); dot(im, 5, 3, FADED)
    fill(im, 8, 0, 12, 5, '#b8935f'); dot(im, 11, 0, '#6d7a52'); dot(im, 9, 2, '#6b5232'); dot(im, 10, 2, '#6b5232')
    # The catalogue's cover, rolled: a red band, a yellow block, a blue one.
    fill(im, 0, 5, 3, 10, '#f1ede2'); fill(im, 0, 5, 3, 6, '#b3261e'); fill(im, 0, 7, 2, 9, '#e0b83a'); dot(im, 2, 8, '#3f6fb8'); dot(im, 1, 9, '#3f6fb8')
    fill(im, 3, 5, 4, 10, '#d9d5c8')
    fill(im, 0, 10, 8, 11, CREAM_D); fill(im, 0, 11, 8, 12, '#dcdad2'); fill(im, 0, 12, 8, 13, '#9a7a4c')
    fill(im, 0, 13, 3, 15, '#efece2'); dot(im, 0, 13, '#b3261e'); dot(im, 1, 14, '#c9c5b8'); dot(im, 2, 13, '#c9c5b8')
    fill(im, 8, 5, 13, 11, '#1e2022'); fill(im, 9, 5, 12, 6, '#2b2e31')
    return im


def letter(later):
    """The corner of an envelope that shows from under a door: the corner with the sender printed in it. Years on, yellow."""
    paper, edge, ink = ('#d9c690', '#b59e63', '#7d7a6a') if later else (CREAM, CREAM_D, PRINT)
    im = new()
    fill(im, 0, 0, 6, 5, paper)
    for x in range(6):
        dot(im, x, 0, edge)
    for y in range(5):
        dot(im, 0, y, edge)
    dot(im, 2, 2, ink); dot(im, 3, 2, ink); dot(im, 4, 2, ink); dot(im, 2, 3, ink); dot(im, 3, 3, ink)
    if later:
        dot(im, 4, 4, '#a68a52'); dot(im, 5, 3, '#a68a52'); dot(im, 5, 4, '#96783f')
    fill(im, 0, 5, 6, 6, edge)
    return im


def curtain(later):
    """Printed cotton drawn across a window, in folds: small flowers on cream. Years on the sun has had it."""
    base, fold, deep = ('#d6cfb8', '#c5bda4', '#b0a88f') if later else ('#f6eed6', '#e6dbbc', '#d2c5a0')
    im = new()
    for y in range(16):
        for x in range(16):
            dot(im, x, y, deep if x % 4 == 3 else fold if x % 4 == 2 else base)
    for x, y in ((1, 1), (9, 3), (5, 6), (13, 8), (1, 10), (9, 12), (5, 14)):
        dot(im, x, y, '#b9aa98' if later else '#c9736b'); dot(im, x, (y + 1) % 16, '#aaa58f' if later else '#7fa37a')
    if later:
        for x, y in ((6, 2), (7, 2), (6, 3), (12, 11), (12, 12), (13, 12), (2, 7)):
            dot(im, x, y, '#b4aa8e')
    return im


TEAL, TEAL_L, TEAL_D, TEAL_S = '#2e6e72', '#3f8a8c', '#235558', '#1a4244'


def door(top, later):
    """Half of a painted front door: a leaf with two sunk panels, a brass knob and its keyhole on the lower half. It has no
    window and no slot. Years on the paint has lifted off it in long curls, down to grey wood."""
    import random
    im = new()
    for y in range(16):
        for x in range(16):
            dot(im, x, y, TEAL_D if x in (0, 15) else TEAL)
    # A sunk panel: dark along its top and left, light along its bottom and right.
    y0, y1 = (3, 15) if top else (4, 13)
    for x0, x1 in ((2, 7), (9, 14)):
        fill(im, x0, y0, x1, y1, TEAL_D)
        fill(im, x0 + 1, y0 + 1, x1, y1, TEAL)
        for x in range(x0 + 1, x1):
            dot(im, x, y1 - 1, TEAL_L)
        for y in range(y0 + 1, y1):
            dot(im, x1 - 1, y, TEAL_L)
        for x in range(x0, x1):
            dot(im, x, y0, TEAL_S)
        for y in range(y0, y1):
            dot(im, x0, y, TEAL_S)
    if top:
        for x in range(16):
            dot(im, x, 0, TEAL_S)
    else:
        fill(im, 12, 1, 14, 3, GILT); dot(im, 12, 1, GILT_L); dot(im, 13, 2, GILT_D); dot(im, 13, 3, BLACK)
        for x in range(16):
            dot(im, x, 15, TEAL_S)
    if later:
        rnd = random.Random(4590 + (1 if top else 0)); px = im.load()
        for x in range(1, 15):                                  # the curls run down the grain
            if rnd.random() < .5:
                y = rnd.randrange(0, 12); n = rnd.randrange(3, 9)
                for k in range(n):
                    if y + k < 16:
                        px[x, y + k] = shade(rnd.choice(('#8d8478', '#7c7367', '#9a9186')))
                if y + n < 16:
                    px[x, y + n] = shade(TEAL_L)
        if not top:
            fill(im, 12, 1, 14, 3, '#7c6a3e'); dot(im, 13, 3, BLACK)
    return im


def door_model(top, stage):
    """The leaf at the back of its cell, as the game's own doors stand: its face to the model's north."""
    name = f"door_{'top' if top else 'low'}_{stage}"
    d = {'all': 'd'}
    return model([element([0, 0, 13], [16, 16, 16], d, {'north': [0, 0, 16, 16], 'south': [0, 0, 16, 16], 'east': [0, 0, 1, 16], 'west': [15, 0, 16, 16],
                                                        'up': [0, 0, 16, 1], 'down': [0, 15, 16, 16]})], {'d': name}, name)


def letter_item(later):
    """The letter in the pack: an envelope, the sender printed in its corner, a stamp under its postmark, a typed address."""
    paper, edge = ('#d9c690', '#a98f55') if later else (CREAM, CREAM_D)
    im = new()
    fill(im, 1, 4, 15, 13, paper)
    for x in range(1, 15):
        dot(im, x, 4, edge); dot(im, x, 12, edge)
    for y in range(4, 13):
        dot(im, 1, y, edge); dot(im, 14, y, edge)
    for x, y in ((3, 6), (4, 6), (5, 6), (3, 7), (4, 7)):
        dot(im, x, y, '#7d7a6a' if later else PRINT)
    fill(im, 11, 6, 13, 8, '#8f4b43' if later else RED); dot(im, 10, 6, '#6c6a64'); dot(im, 10, 8, '#6c6a64'); dot(im, 9, 7, '#6c6a64')
    for x in range(6, 11):
        dot(im, x, 9, INK)
    for x in range(7, 10):
        dot(im, x, 11, INK)
    if later:
        dot(im, 3, 11, '#a68a52'); dot(im, 4, 11, '#a68a52'); dot(im, 3, 10, '#b59e63'); dot(im, 13, 11, '#96783f')
    return im


def textures():
    extra = {'item/borrasca_letter.png': letter_item(False), 'item/borrasca_letter_aged.png': letter_item(True)}
    return extra | {f'block/borrasca_home/{name}.png': im for name, im in {
        'box_0': box(False), 'box_1': box(True), 'mail': mail(), 'letter_0': letter(False), 'letter_1': letter(True),
        'curtain_0': curtain(False), 'curtain_1': curtain(True),
        'door_top_0': door(True, False), 'door_low_0': door(False, False), 'door_top_1': door(True, True), 'door_low_1': door(False, True),
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


def turned(e, face, quarter=90):
    """Turns one face's picture a quarter round, where a long face lies the other way to the picture drawn for it."""
    e['faces'][face]['rotation'] = quarter
    return e


def box_model(stage, full=False):
    """A mailbox, in the cell over the fence post it stands on: its mouth to the model's north. Its top is a step narrower than
    its body, for the round of it. Full, its door hangs open and a week of mail stands out of its mouth."""
    b = {'all': 'b'}
    side_low, side_top = [0, 2, 10, 6], [0, 0, 10, 2]
    front, back = ({'north': 'm'}, {'north': [8, 7, 13, 11]}) if full else ({}, {'north': [10, 2, 15, 6]})
    low = element([5.5, 0, 3], [10.5, 4, 13], {**b, **front},
                  {'east': side_low, 'west': side_low, 'south': [10, 8, 15, 12], 'up': [0, 6, 10, 11], 'down': [0, 6, 10, 11], **back})
    top_front = ({'north': 'm'}, {'north': [9, 5, 12, 7]}) if full else ({}, {'north': [11, 0, 14, 2]})
    top = element([6.5, 4, 3], [9.5, 6, 13], {**b, **top_front[0]},
                  {'east': side_top, 'west': side_top, 'south': [11, 6, 14, 8], 'up': [0, 7, 10, 10], 'down': [0, 7, 10, 10], **top_front[1]})
    for e in (low, top):
        turned(e, 'up'); turned(e, 'down')
    parts = [low, top]
    if stage == 0:                                              # the flag, down along the shoulder, clear of the label
        parts += [element([9.5, 4.2, 6], [10, 5.2, 9], b, {**every([0, 13, 1, 14]), 'east': [0, 13, 3, 14], 'west': [0, 13, 3, 14]}),
                  element([9.5, 4.2, 9], [10, 6.2, 10], b, {**every([4, 13, 5, 14]), 'east': [4, 12, 5, 14], 'west': [4, 12, 5, 14]})]
    tex = {'b': f'box_{stage}'}
    if full:
        m = {'all': 'm'}
        parts += [
            # Its door, let down on its hinge, in front of the post.
            element([5.5, -4.5, 2.5], [10.5, 0, 3], b, {**every([10, 8, 15, 8.5]), 'north': [10, 7.5, 15, 12], 'south': [10, 7.5, 15, 12]}),
            # A cream envelope and a brown one, a catalogue rolled on top of them.
            element([6, .2, .8], [10, .6, 6], m, {**every([0, 10, 4, 10.4]), 'up': [0, 0, 4, 5], 'down': [0, 0, 4, 5]}),
            element([6.3, .6, 1.6], [10.3, 1, 6.6], m, {**every([0, 12, 4, 12.4]), 'up': [8, 0, 12, 5], 'down': [8, 0, 12, 5]},
                    {'origin': [8.3, .8, 4.1], 'axis': 'y', 'angle': 22.5}),
            element([6.7, 1, .4], [9.7, 3, 5.4], m, {'north': [0, 13, 3, 15], 'south': [0, 13, 3, 15], 'up': [0, 5, 3, 10], 'down': [0, 5, 3, 10],
                                                      'east': [3, 5, 4, 10], 'west': [3, 5, 4, 10]}),
        ]
        for face in ('east', 'west'):
            turned(parts[-1], face)
        tex['m'] = 'mail'
    return model(parts, tex, f'box_{stage}')


def letter_model(stage):
    """The corner of an envelope at a door's foot. The door is the next cell to the model's north, its leaf at that cell's far
    side: the envelope lies under the leaf and its corner shows in the doorway."""
    return model([element([5.5, .02, -15.6], [11.5, .3, -10.6], {'all': 'l'}, {**every([0, 5, 6, 5.3]), 'up': [0, 0, 6, 5], 'down': [0, 0, 6, 5]},
                          {'origin': [8.5, .1, -13], 'axis': 'y', 'angle': -22.5})],
                 {'l': f'letter_{stage}'}, f'letter_{stage}')


def curtain_model(stage):
    """A curtain drawn across a window, just inside the wall behind the model's south."""
    c = {'north': 'c', 'south': 'c', 'east': 'c', 'west': 'c', 'up': 'c', 'down': 'c'}
    return model([element([0, 0, 14.5], [16, 16, 15.5], c, {'north': [0, 0, 16, 16], 'south': [0, 0, 16, 16], 'east': [3, 0, 4, 16], 'west': [3, 0, 4, 16],
                                                            'up': [0, 0, 16, 1], 'down': [0, 0, 16, 1]})],
                 {'c': f'curtain_{stage}'}, f'curtain_{stage}')


KINDS = ['crib', 'portrait', 'cross', 'bible', 'tv', 'jacket', 'suitcase', 'atlas', 'frame_down', 'box', 'box_full', 'letter', 'curtain', 'door', 'door_top']
STAGES = {'cross': 1, 'frame_down': 1, 'box_full': 1}
FACING_Y = {'north': 0, 'east': 90, 'south': 180, 'west': 270}
NAME = {'block.the_oldest_house.borrasca_home': 'Household Thing', 'item.the_oldest_house.borrasca_letter': 'A letter for the Garritys'}


def models():
    m = {}
    for stage in range(2):
        m[f'crib_{stage}'] = crib(stage); m[f'portrait_{stage}'] = portrait_model(stage); m[f'bible_{stage}'] = bible_model(stage)
        m[f'tv_{stage}'] = tv_model(stage); m[f'jacket_{stage}'] = jacket_model(stage); m[f'suitcase_{stage}'] = suitcase_model(stage)
        m[f'atlas_{stage}'] = atlas_model(stage)
        m[f'box_{stage}'] = box_model(stage); m[f'letter_{stage}'] = letter_model(stage); m[f'curtain_{stage}'] = curtain_model(stage)
        m[f'door_{stage}'] = door_model(False, stage); m[f'door_top_{stage}'] = door_model(True, stage)
    m['cross_0'] = cross_model(); m['frame_down_0'] = frame_down_model(); m['box_full_0'] = box_model(0, True)
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
    item = NS + 'item/borrasca_letter'
    out['models/item/borrasca_letter.json'] = {'parent': 'minecraft:item/generated', 'textures': {'layer0': item},
                                                 'overrides': [{'predicate': {'custom_model_data': 1}, 'model': item + '_aged'}]}
    out['models/item/borrasca_letter_aged.json'] = {'parent': 'minecraft:item/generated', 'textures': {'layer0': item + '_aged'}}
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
