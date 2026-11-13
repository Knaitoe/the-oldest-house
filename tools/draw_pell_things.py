#!/usr/bin/env python3
"""The Pell house's things (docs/PELL_HOUSE_PROPOSAL.md, "The house, room by room"), slice 2: what is in each room, drawn at sixteen
pixels to the block like the rest of the house (tools/draw_pell_house.py, whose helpers this uses).

Every thing is drawn facing north (its front is the north face as drawn) and turned by FACING, except the circle on Callum's floor,
whose sixteen cells are each a model of their own and are never turned. Run it to draw; --check verifies the pack against it (CI
runs that). It reads PellThing's kinds from the game and fails if the two disagree.
"""
import argparse
import json
import math
import random
import re
import sys
from pathlib import Path

from PIL import Image

sys.path.insert(0, str(Path(__file__).resolve().parent))
from draw_pell_house import A, NS, ROOT, new, fill, dot, speckle, shade, rgb, element  # noqa: E402
from draw_pell_house import wood, metal, enamel, cloth, card, plastic, bevel, inset  # noqa: E402
from draw_pell_house import materials as house_materials  # noqa: E402

def line(im, pts, colour, a=255):
    for x, y in pts:
        dot(im, x, y, colour, a)


def frame(im, x0, y0, x1, y1, colour):
    for x in range(x0, x1):
        dot(im, x, y0, colour); dot(im, x, y1 - 1, colour)
    for y in range(y0, y1):
        dot(im, x0, y, colour); dot(im, x1 - 1, y, colour)


def face(im, x, y, size, hair, skin='#e8c4a0', eyes='#3a2a20', fringe=True):
    """A face in a photograph: size 3 (a toddler's, far off) to 6 (a school photo). The same girl at every size: dark straight hair
    to the jaw with a fringe, and a gap where a tooth should be in the middle years."""
    fill(im, x, y, x + size, y + size, skin)
    fill(im, x, y, x + size, y + 1, hair)
    fill(im, x - 1, y, x, y + size, hair); fill(im, x + size, y, x + size + 1, y + size, hair)
    if fringe and size >= 4:
        fill(im, x, y + 1, x + size, y + 2, hair)
    ey = y + (2 if size >= 4 else 1)
    dot(im, x + (1 if size >= 4 else 0), ey, eyes); dot(im, x + size - (2 if size >= 4 else 1), ey, eyes)
    if size >= 5:
        dot(im, x + size // 2 - 1, y + size - 2, '#b06a5a'); dot(im, x + size // 2, y + size - 2, '#b06a5a')


# ------------------------------------------------------------------------------------------------ the kitchen
def laminate_top():
    """The benches' laminate: cream with a fine speckle, the front edge a chrome strip."""
    im = new('#e3dcc4', 52201, 2); speckle(im, 52202, 18, ['#d2c9ae', '#ece6d2', '#c9bfa2'])
    for x in range(16):
        dot(im, x, 15, '#c9ced1' if x % 5 == 1 else '#a3a8ab' if x % 5 == 3 else '#9ea3a6')
    return im


def cupboard():
    """A bench's front: two doors in a teak-look laminate, its grain running up them, each door's edges lit along the top and left
    and shaded along the others, with a groove for a handle along its top (its lower lip catching the light); a kick under them,
    darkest under the doors' overhang."""
    im = wood('#9a6a40', 52203, across=True)
    fill(im, 0, 0, 16, 1, '#7a5230'); fill(im, 7, 1, 9, 13, '#5a3c22')
    bevel(im, 0, 1, 7, 13, 12); bevel(im, 9, 1, 16, 13, 12)
    fill(im, 1, 1, 7, 2, '#3e2a18'); fill(im, 9, 1, 15, 2, '#3e2a18')
    for x in list(range(1, 7)) + list(range(9, 15)):
        dot(im, x, 2, shade(im.getpixel((x, 2)), 12))
    fill(im, 0, 13, 16, 16, '#2e2a26'); fill(im, 0, 13, 16, 14, '#221f1c')
    return im


def cupboard_side():
    im = wood('#9a6a40', 52204, across=True); fill(im, 0, 13, 16, 16, '#2e2a26'); fill(im, 0, 13, 16, 14, '#221f1c')
    return im


def sink_top():
    """The sink under the window: a stainless bowl in the laminate, its drainer ridged, the plug on its chain, a tea towel over the
    edge where it always hangs."""
    im = laminate_top()
    steel = metal('#c8ced1', 52290)
    for y in range(2, 12):
        for x in range(2, 15):
            im.putpixel((x, y), steel.getpixel((x, y)))
    frame(im, 2, 2, 10, 12, '#e3e7e9')
    inset(im, 3, 3, 9, 11, 18)
    dot(im, 6, 8, '#4a5054'); dot(im, 7, 8, '#4a5054'); dot(im, 6, 7, '#7e8589'); line(im, [(7, 9), (8, 10)], '#7c8286')
    for y in range(2, 12):
        for x in range(11, 15):
            dot(im, x, y, '#dde2e4' if y % 2 == 0 else '#a3a9ad')
    fill(im, 10, 2, 11, 12, '#8e9599')
    return im


def stove_front():
    """An enamel stove: the four knobs and the clock along the top, the oven door's glass gone brown, its handle."""
    im = enamel('#efece2', 52205)
    fill(im, 0, 0, 16, 3, '#d9d5c9'); fill(im, 0, 0, 16, 1, '#e6e2d7'); fill(im, 0, 3, 16, 4, '#cdc9bd')
    for x in (1, 4, 11, 14):
        dot(im, x, 1, '#2b2b2b'); dot(im, x, 2, '#b4b0a4')
    fill(im, 6, 0, 10, 3, '#3a3a36'); dot(im, 7, 1, '#e0d8b4'); dot(im, 8, 1, '#c9c2a2')
    for x in range(1, 15):
        dot(im, x, 4, '#8e8f8c' if x in (1, 14) else '#eef0ee' if x == 2 else '#cfd0cd')
    bevel(im, 1, 5, 15, 13, 10)
    fill(im, 2, 6, 14, 12, '#3b2f24'); fill(im, 3, 7, 13, 11, '#5a4634'); inset(im, 3, 7, 13, 11, 10)
    for x, y in ((4, 10), (5, 9), (6, 8), (8, 10), (9, 9)):
        dot(im, x, y, '#86705a')
    fill(im, 0, 13, 16, 16, '#2e2a26')
    return im


def stove_top():
    """Four coil elements, one with a pan left on it, and a burn in the enamel beside the front left."""
    im = enamel('#efece2', 52206)
    for cx, cy in ((4, 4), (12, 4), (4, 11), (12, 11)):
        for y in range(cy - 2, cy + 3):
            for x in range(cx - 2, cx + 3):
                d = ((x - cx) ** 2 + (y - cy) ** 2) ** .5
                if 1.2 < d < 2.6:
                    dot(im, x, y, '#5a5550' if x + y < cx + cy - 1 else '#3a3632' if x + y < cx + cy + 2 else '#2a2724')
        dot(im, cx, cy, '#cfcabd')
    fill(im, 10, 2, 15, 7, '#6e6a66'); fill(im, 11, 3, 14, 6, '#8c8884'); fill(im, 15, 4, 16, 5, '#2b2b2b')
    line(im, [(10, 2), (11, 2), (12, 2), (13, 2), (10, 3), (10, 4)], '#a29e9a'); line(im, [(14, 6), (13, 6), (14, 5)], '#4e4a46')
    dot(im, 11, 3, '#5e5a56'); dot(im, 12, 3, '#6a6662')
    dot(im, 1, 14, '#8a6a3c'); dot(im, 2, 14, '#6b4e2a'); dot(im, 2, 15, '#8a6a3c')
    return im


def fridge_front(upper, stage=0):
    """The fridge: white enamel, its handle; on the upper door, the photographs. Nessa in every year, held up by magnets: a baby,
    small, starting school, missing a tooth, in a school jumper; one older photograph with a magnet square over the face. In the
    second stage two more, new, at the edge: two children on a doorstep, the day they came."""
    im = enamel('#f2f0ea', 52207 + upper)
    fill(im, 13, 0 if upper else 1, 15, 16 if upper else 9, '#c9c6bd')
    fill(im, 13, 0 if upper else 1, 14, 16 if upper else 9, '#e2dfd7'); fill(im, 15, 0 if upper else 1, 16, 16 if upper else 9, '#dad7cf')
    if not upper:
        fill(im, 0, 0, 16, 1, '#c9c6bd')
        fill(im, 0, 14, 16, 16, '#3b3832')
        return im
    fill(im, 0, 15, 16, 16, '#c9c6bd')
    hair = '#3a2a1e'
    # Six photographs and their magnets.
    shots = [(1, 1, 4, 3), (6, 1, 5, 3), (1, 6, 5, 4), (7, 6, 5, 5), (1, 11, 4, 3), (7, 12, 4, 3)]
    for i, (x, y, w, s) in enumerate(shots):
        fill(im, x, y, x + w, y + w, '#f8f6ef'); fill(im, x, y, x + w, y + 1, '#d8d5cc')
        for i in range(w):
            if y + w < 16 and x + i + 1 < 13:
                dot(im, x + i + 1, y + w, '#d9d6cd')
        fill(im, x + 1, y + 1, x + w - 1, y + w - 1, '#9fb4a8' if i % 2 else '#c9b48e')
        if i == 3:
            # The covered one: a magnet square over the face.
            fill(im, x + 1, y + 1, x + w - 1, y + w - 1, '#c9b48e'); fill(im, x + 1, y + 1, x + 4, y + 4, '#c0392b')
            dot(im, x + 2, y + 2, '#e05a48')
        else:
            sz = max(1, w - 3)
            fill(im, x + 1, y + 1, x + 1 + sz, y + 1, hair); fill(im, x + 1, y + 2, x + 1 + sz, y + 1 + sz, '#e8c4a0')
        dot(im, x + w // 2, y, ['#2e6fbf', '#e0b030', '#3a9a4a', '#c0392b', '#9a4ab0', '#2e6fbf'][i])
    if stage:
        # The two new ones, white-edged and bright, on the handle side: two children on a doorstep.
        fill(im, 11, 9, 13, 15, '#ffffff'); dot(im, 11, 10, '#5a3a24'); dot(im, 12, 11, '#3a2a1e'); dot(im, 11, 12, '#e8c4a0'); dot(im, 12, 12, '#e8c4a0')
        dot(im, 11, 14, '#8a7a6a'); dot(im, 12, 14, '#8a7a6a')
    return im


def fridge_side():
    im = enamel('#e9e7e0', 52209); fill(im, 0, 0, 16, 1, '#c9c6bd'); fill(im, 0, 1, 16, 2, '#f2f0ea')
    return im


def knife_block(stage):
    """A wooden knife block on the bench, its slots in a fan. Stage 0 a handle in every slot; 1 one slot empty; 2 the block gone and
    a clean square where it stood."""
    im = laminate_top()
    if stage == 2:
        fill(im, 5, 5, 11, 11, '#ece6d2')
        return im
    blk = wood('#8a5a32', 52291)
    for y in range(5, 11):
        for x in range(5, 11):
            im.putpixel((x, y), blk.getpixel((x, y)))
    bevel(im, 5, 5, 11, 11, 16)
    for x in range(6, 12):
        dot(im, x, 11, '#bcb39a')
    for i, x in enumerate((6, 7, 8, 9)):
        if not (stage == 1 and i == 2):
            dot(im, x, 6, '#1e1a16'); dot(im, x, 7, '#2c2620')
        else:
            dot(im, x, 6, '#4a3020')
    return im


def wood_block():
    return wood('#8a5a32', 52210, across=True)


def lunchboxes():
    """Two plastic lunchboxes, side by side: one with a name on a label, one where the label has been scratched away."""
    im = laminate_top()
    fill(im, 2, 4, 7, 12, '#3a8acb'); fill(im, 2, 4, 7, 5, '#2a6aa0'); fill(im, 3, 7, 6, 9, '#f4f1e6')
    line(im, [(3, 8), (4, 8), (5, 8)], '#2b2b2b')
    fill(im, 9, 4, 14, 12, '#d64a6a'); fill(im, 9, 4, 14, 5, '#a8364f'); fill(im, 10, 7, 13, 9, '#e9d6c2')
    line(im, [(10, 8), (12, 7), (11, 8)], '#b89a86')
    bevel(im, 2, 5, 7, 12, 18); bevel(im, 9, 5, 14, 12, 18)
    for x in range(3, 8):
        dot(im, x, 12, '#bcb39a')
    for x in range(10, 15):
        dot(im, x, 12, '#bcb39a')
    return im


def lunchbox_side():
    im = plastic('#3a8acb', 52211); fill(im, 0, 0, 16, 2, '#2a6aa0'); fill(im, 0, 2, 16, 3, '#5aa2db')
    return im



def clear():
    return Image.new('RGBA', (16, 16), (0, 0, 0, 0))


def kitchen_calendar():
    """A calendar from a hardware shop, still on the month she died. Its days are blank; in its margin, in a newer pen, the
    appointments for somebody new: eyes, school, a name."""
    im = clear()
    fill(im, 2, 0, 14, 7, '#7aa0c8'); fill(im, 3, 1, 13, 6, '#a8c4de'); fill(im, 4, 3, 12, 6, '#5a8a5a')
    fill(im, 2, 7, 14, 16, '#fbf8ee')
    for y in (9, 11, 13):
        for x in range(4, 13, 2):
            dot(im, x, y, '#bdb5a0')
    fill(im, 2, 7, 14, 8, '#c03a3a')
    line(im, [(13, 9), (13, 10), (13, 12), (13, 13), (13, 14)], '#2a4aa0')
    dot(im, 3, 15, '#2a4aa0')
    return im


def table_top():
    """The kitchen table's top: laminate edged in chrome, a ring from a mug."""
    im = new('#d8cfb2', 52212, 2); speckle(im, 52213, 12, ['#cbc1a2', '#e2dac0'])
    for i in range(16):
        dot(im, i, 15, '#868b8e'); dot(im, 15, i, '#868b8e'); dot(im, i, 0, '#c9ced1'); dot(im, 0, i, '#c9ced1')
    dot(im, 15, 0, '#a6abae'); dot(im, 0, 15, '#a6abae')
    for x, y in ((5, 6), (6, 5), (7, 5), (8, 6), (8, 7), (7, 8), (6, 8), (5, 7)):
        dot(im, x, y, '#c4ba98')
    return im


def chrome():
    im = metal('#b9bec1', 52214, 3)
    for y in range(16):
        dot(im, 0, y, '#e6eaec'); dot(im, 1, y, '#f4f7f8'); dot(im, 14, y, '#9aa0a3'); dot(im, 15, y, '#7d8285')
    return im


def vinyl(colour='#a8452e', seed=52215):
    """Vinyl on a chair's seat and back, buttoned, a split mended with tape on one."""
    im = plastic(colour, seed)
    for x, y in ((4, 4), (11, 4), (4, 11), (11, 11)):
        dot(im, x - 1, y - 1, shade(colour, 14)); dot(im, x, y, shade(colour, -44)); dot(im, x + 1, y + 1, shade(colour, -20))
        for d in (2, 3):
            dot(im, x + d, y + d // 3, shade(colour, -9)); dot(im, x - d, y - d // 3, shade(colour, -9))
    return im


def phone_plate():
    """0.4.532: the kitchen telephone's wall plate, cream plastic gone the colour of old piano keys: its front bevelled (the face of the
    plate, x 5 to 10 and y 1 to 14 here), the slot of the hook switch near its top where the receiver rests, and low down the hole the
    cord goes into."""
    im = plastic('#e2d6b5', 53201)
    bevel(im, 5, 1, 11, 15, 12)
    fill(im, 7, 2, 9, 3, '#a89c7d')
    dot(im, 7, 13, '#8a7f66'); dot(im, 8, 13, '#5f5645')
    return im


def phone_handset():
    """0.4.532: the receiver's back, as it hangs on the plate and as June holds it: the same plastic, lit along its ridge, its sides
    turning away into shadow."""
    im = plastic('#e6dabb', 53202)
    for y in range(16):
        dot(im, 8, y, '#f3ead3'); dot(im, 7, y, '#ece2c6')
        for x in (5, 6, 9, 10):
            dot(im, x, y, '#cdc1a0' if x in (6, 9) else '#b9ad8c')
    return im


def phone_cord():
    """0.4.532: the coiled cord, a turn to each pixel: each turn lit on top and in the shadow of the one above."""
    im = clear()
    for y in range(16):
        for x in range(16):
            dot(im, x, y, '#e0d4b3' if y % 2 == 0 else '#a99d7e')
    return im


def phone_tape():
    """0.4.532: a strip of masking tape on the wall beside the telephone, GAIL pencilled on it and a number under that, too small to
    read from across the kitchen. The tape's own part of the picture is x 4 to 9 and y 6 to 9."""
    im = clear()
    fill(im, 4, 6, 10, 10, '#e5d7a3')
    for x in (4, 9):
        dot(im, x, 6, '#d6c68e'); dot(im, x, 9, '#d6c68e')
    for x, y in ((5, 7), (6, 7), (7, 7), (8, 7)):
        dot(im, x, y, '#6c6a64', 200)
    for x in (5, 7, 8):
        dot(im, x, 8, '#6c6a64', 170)
    return im


def kitchen():
    return {'laminate_top': laminate_top(), 'cupboard': cupboard(), 'cupboard_side': cupboard_side(), 'sink_top': sink_top(),
            'phone_plate': phone_plate(), 'phone_handset': phone_handset(), 'phone_cord': phone_cord(), 'phone_tape': phone_tape(),
            'stove_front': stove_front(), 'stove_top': stove_top(), 'fridge_low': fridge_front(0), 'fridge_up_0': fridge_front(1, 0),
            'fridge_up_1': fridge_front(1, 1), 'fridge_side': fridge_side(), 'knife_2': knife_block(2), 'wood_block': wood_block(), 'lunchboxes': lunchboxes(), 'lunchbox_side': lunchbox_side(),
            'calendar_kitchen': kitchen_calendar(), 'table_top': table_top(), 'chrome': chrome(),
            'vinyl_red': vinyl()}


# ------------------------------------------------------------------------------------------------ small letters
# A three-by-five hand for labels that must read at sixteen to the block (marker on a box, a tag on a stake, a name on a strip).
GLYPHS = {
    'A': ['.#.', '#.#', '###', '#.#', '#.#'], 'B': ['##.', '#.#', '##.', '#.#', '##.'], 'C': ['.##', '#..', '#..', '#..', '.##'],
    'D': ['##.', '#.#', '#.#', '#.#', '##.'], 'E': ['###', '#..', '##.', '#..', '###'], 'I': ['###', '.#.', '.#.', '.#.', '###'],
    'K': ['#.#', '#.#', '##.', '#.#', '#.#'], 'L': ['#..', '#..', '#..', '#..', '###'], 'N': ['#..#', '##.#', '#.##', '#..#', '#..#'],
    'O': ['.#.', '#.#', '#.#', '#.#', '.#.'], 'R': ['##.', '#.#', '##.', '#.#', '#.#'], 'S': ['.##', '#..', '.#.', '..#', '##.'],
    'T': ['###', '.#.', '.#.', '.#.', '.#.'], 'U': ['#.#', '#.#', '#.#', '#.#', '###'], 'W': ['#...#', '#...#', '#.#.#', '##.##', '#...#'],
    'Y': ['#.#', '#.#', '.#.', '.#.', '.#.'], 'F': ['###', '#..', '##.', '#..', '#..'], 'G': ['.##', '#..', '#.#', '#.#', '.##'],
    'H': ['#.#', '#.#', '###', '#.#', '#.#'], 'M': ['#...#', '##.##', '#.#.#', '#...#', '#...#'], "'": ['#', '#', '.', '.', '.'],
    ',': ['.', '.', '.', '#', '#'], ' ': ['..', '..', '..', '..', '..'], 'J': ['..#', '..#', '..#', '#.#', '.#.'],
}


def text(im, x, y, s, colour, gap=1, a=255):
    """Letters from the small hand, left to right from (x, y); returns where the next letter would go."""
    for ch in s:
        g = GLYPHS[ch]
        for gy, row in enumerate(g):
            for gx, c in enumerate(row):
                if c == '#':
                    dot(im, x + gx, y + gy, colour, a)
        x += len(g[0]) + gap
    return x


def width(s, gap=1):
    return sum(len(GLYPHS[c][0]) for c in s) + gap * (len(s) - 1)


# ------------------------------------------------------------------------------------------------ the living room
def recliner(worn):
    """The recliner's brown vinyl. Worn: the seat's front edge and the right arm's end gone pale and cracked, where one person has
    sat every night with her hand on the arm."""
    im = plastic('#6a4a32', 52220)
    for x, y in ((3, 3), (12, 3), (3, 12), (12, 12)):
        dot(im, x - 1, y - 1, '#7e5c42'); dot(im, x, y, '#3e2a1a'); dot(im, x + 1, y + 1, '#55392a')
    for i in range(4, 12):
        if i % 2:
            dot(im, i, 3, '#5a3e2a'); dot(im, i, 12, '#5a3e2a'); dot(im, 3, i, '#5a3e2a'); dot(im, 12, i, '#5a3e2a')
    if worn:
        for y in range(9, 16):
            for x in range(2, 14):
                if (x * 5 + y * 3) % 4:
                    dot(im, x, y, '#8c6a4c')
        line(im, [(4, 11), (5, 12), (6, 12), (9, 13), (10, 12)], '#3e2a1c')
    return im


def tape_cabinet():
    """The cabinet under the television: two glass doors, and behind them home tapes on their spines, each labelled in June's hand
    with a birthday and nothing else."""
    im = wood('#7a5232', 52221)
    bevel(im, 0, 0, 16, 16, 12)
    fill(im, 1, 1, 15, 15, '#2a2420'); inset(im, 1, 1, 15, 15, 10)
    for row, y0 in enumerate((2, 8)):
        x = 2
        rnd = random.Random(52222 + row)
        while x < 14:
            fill(im, x, y0, x + 1, y0 + 5, '#1a1a1a'); fill(im, x, y0 + 1, x + 1, y0 + 3, '#efe8d6')
            dot(im, x, y0 + 2, rnd.choice(['#2a4aa0', '#c03a3a', '#2a2a2a']))
            x += 2
    for y in range(1, 15):
        dot(im, 8, y, '#7a5232')
    dot(im, 7, 8, '#c9b36a'); dot(im, 9, 8, '#c9b36a')
    for x in range(2, 7):
        dot(im, x, 2 + x // 2, '#ffffff', 60)
    return im


def tv_front(lit=False):
    """A big wooden-cased television, its screen dark and curved, a row of buttons beneath; a doily and nothing else on top."""
    im = wood('#6a4a2e', 52223)
    bevel(im, 0, 0, 16, 16, 12)
    fill(im, 1, 1, 13, 12, '#1c1e1e'); fill(im, 1, 1, 13, 2, '#121414'); fill(im, 1, 1, 2, 12, '#121414')
    fill(im, 2, 2, 12, 11, '#2c3232' if not lit else '#9fb7c4')
    if not lit:
        dot(im, 3, 3, '#4a5454'); dot(im, 4, 3, '#4a5454'); dot(im, 3, 4, '#4a5454'); dot(im, 10, 9, '#384040'); dot(im, 9, 9, '#343a3a')
    fill(im, 14, 2, 15, 11, '#3a2a1a')
    for y in range(2, 11):
        if y % 2 == 0:
            dot(im, 14, y, '#2a1e12')
    for y in (3, 5, 7):
        dot(im, 14, y, '#c9b36a')
    fill(im, 1, 13, 15, 15, '#4e3622'); bevel(im, 1, 13, 15, 15, 10)
    return im


# 0.4.527 (docs/PELL_HOUSE_BEDTIME_0_4_527.md): the home tape June plays every night, a birthday in this kitchen, on the television's
# screen. Each picture is a sixteen-pixel cell of a strip the game plays through in TAPE_ORDER (ANIMATED): the cake carried in with its
# candles lit, the camera's hand not quite still; the girl in her paper crown close to the lens, then laughing; the candles out; and the
# tape tearing and rolling to the next year. The camera's date burns in the corner. The screen's picture is x 2 to 11 and y 2 to 10.
TAPE_ORDER = [0, 1, 0, 1, 0, 1, 0, 1, 2, 3, 2, 3, 2, 2, 4, 4, 4, 4, 5, 6]
TAPE_FRAMETIME = 5


def tape_scene(im, frame, dx=0):
    """The kitchen on the tape: the bench's wall gone yellow on old tape, the table across the bottom, the cake on it; the girl at the
    right of it in her crown and her first school photograph's jumper. Lit or blown out; the girl stood back or leaning in."""
    def put(x, y, c):
        if 2 <= x + dx <= 11 and 2 <= y <= 10:
            dot(im, x + dx, y, c)
    for y in range(2, 11):
        for x in range(1, 13):
            put(x, y, '#c9b48e' if y < 8 else '#7c5a3c' if y == 8 else '#5e4430')
    for x in range(1, 13):
        put(x, 4, '#bea886')
    # The cake: icing over a band, three candles.
    for x in range(4, 8):
        put(x, 6, '#f6efe2'); put(x, 7, '#d88a9a')
    for x in (4, 6, 7):
        put(x, 5, '#e8e0f0')
    if frame == 4:
        put(4, 3, '#b4b0aa'); put(6, 4, '#c4c0ba'); put(7, 2, '#b4b0aa')
    else:
        lit = ('#ffd25a', '#ffb04a') if frame == 0 else ('#ffb04a', '#ffe08a')
        put(4, 4, lit[0]); put(6, 4, lit[1]); put(7, 4, lit[0])
    # The girl: further off by the cake, or leaning in to blow.
    gx = 9 if frame != 4 else 8
    for x in (gx, gx + 1):
        put(x, 2, '#e8c04a'); put(x, 3, '#2a1e18'); put(x, 4, '#e8c4a0'); put(x, 5, '#a8364f'); put(x, 6, '#a8364f'); put(x, 7, '#a8364f')
    put(gx, 2, '#f0cc5a')


def tape_face(im, laughing):
    """The girl close to the lens, in a paper crown; laughing, her eyes screwed shut."""
    for y in range(2, 11):
        for x in range(2, 12):
            dot(im, x, y, '#c9b48e')
    for x in range(3, 11):
        dot(im, x, 2, '#e8c04a' if x % 2 else '#f4d870')
    colours = {**NESSA, 'O': '#6a2a24'}
    for gy, row in enumerate(NESSA_FACE[1:]):
        for gx, c in enumerate(row):
            # Laughing: the eyes screwed shut to a line, the mouth open.
            if laughing and c in 'WI':
                c = 's'
            if laughing and c == 'M':
                c = 'O'
            dot(im, 3 + gx, 3 + gy, colours[c])
    if laughing:
        dot(im, 6, 9, colours['O']); dot(im, 7, 9, colours['O'])
    for x in range(3, 11):
        dot(im, x, 10, '#a8364f')


def tv_tape():
    """The strip: seven pictures on the lit television, the date in the corner of each that is not torn."""
    frames = []
    for k in range(7):
        im = tv_front(True)
        if k in (0, 1):
            tape_scene(im, k, dx=k)
        elif k in (2, 3):
            tape_face(im, k == 3)
        elif k == 4:
            tape_scene(im, 4)
        else:
            tape_scene(im, 4, dx=2 if k == 5 else -1)
            # The tape tearing: a bright band across, then rolling down as it finds the next year.
            for y in ((5, 6) if k == 5 else (8, 9)):
                for x in range(2, 12):
                    dot(im, x, y, '#e8ecee' if (x + y) % 3 else '#9aa4aa')
        if k < 5:
            dot(im, 10, 10, '#ff9a3a'); dot(im, 11, 10, '#ff9a3a')
        frames.append(im)
    strip = Image.new('RGBA', (16, 16 * len(frames)), (0, 0, 0, 0))
    for i, im in enumerate(frames):
        strip.paste(im, (0, 16 * i))
    return strip


def wood_dark():
    return wood('#6a4a2e', 52224)


def mantel_front():
    """The fireplace in the living room's wall: cream brick round a gas heater's grille, its pilot window dark."""
    im = new('#d9c9a8', 52225, 3)
    rnd = random.Random(52226)
    for y in range(0, 16, 3):
        off = 0 if (y // 3) % 2 else 3
        for x0 in range(off - 6, 16, 6):
            tone = rnd.randint(-8, 8)
            for yy in range(y + 1, min(16, y + 3)):
                for x in range(x0 + 1, x0 + 6):
                    if 0 <= x < 16:
                        dot(im, x, yy, shade(im.getpixel((x, yy)), tone + (6 if yy == y + 1 else 0)))
        for x in range(16):
            dot(im, x, y, '#bfae8c')
        for x in range(off, 16, 6):
            for yy in range(y, min(16, y + 3)):
                dot(im, x, yy, '#b5a382')
    fill(im, 3, 4, 13, 16, '#2a2622'); fill(im, 4, 5, 12, 14, '#5a5650')
    for y in range(6, 13, 2):
        fill(im, 5, y, 11, y + 1, '#3a3632'); fill(im, 5, y + 1, 11, y + 2, '#6c6862')
    dot(im, 8, 14, '#e08a3a'); dot(im, 7, 14, '#8a4a22'); dot(im, 9, 14, '#8a4a22')
    return im



def seat_cushion():
    """The window seat's cushion: faded floral cotton, the side to the window paler."""
    im = cloth('#c9b2a0', 52227)
    rnd = random.Random(52228)
    for _ in range(9):
        x, y = rnd.randrange(1, 15), rnd.randrange(4, 15)
        dot(im, x, y, '#a86a6a'); dot(im, x - 1, y, '#c48e8e'); dot(im, x + 1, y, '#c48e8e'); dot(im, x, y - 1, '#c48e8e')
        dot(im, x + 1, y + 1, '#8ca07a')
    for y in range(3):
        for x in range(16):
            dot(im, x, y, shade(im.getpixel((x, y)), 14))
    fill(im, 0, 15, 16, 16, '#a8907e')
    return im


def remote():
    """The television's remote: black, its buttons worn grey where a thumb goes."""
    im = plastic('#1e1e20', 52329)
    bevel(im, 0, 0, 16, 16, 16)
    for y in range(2, 14, 2):
        for x in range(2, 14, 3):
            dot(im, x, y, '#7a7a7c' if y < 8 else '#4a4a4c'); dot(im, x, y + 1, '#101012')
    dot(im, 8, 1, '#d84a4a'); dot(im, 7, 1, '#e87a6a')
    return im


def mug_top():
    """The mug from above: its white rim, the tea gone cold in it with a skin on top."""
    im = enamel('#f2efe6', 52330, 1)
    bevel(im, 0, 0, 16, 16, 10)
    fill(im, 2, 2, 14, 14, '#7a5a3a'); fill(im, 2, 2, 14, 3, '#5a3e24'); fill(im, 2, 2, 3, 14, '#5a3e24')
    for x, y in ((5, 5), (6, 5), (9, 7), (10, 8), (6, 10), (7, 10), (11, 11)):
        dot(im, x, y, '#a08060')
    dot(im, 10, 4, '#c8a882'); dot(im, 11, 4, '#b8946e')
    return im


def tape_case():
    """A tape's case, open and empty: black plastic, the white label on its spine with a date in June's hand."""
    im = plastic('#202022', 52331)
    fill(im, 1, 2, 15, 14, '#2c2c30'); bevel(im, 1, 2, 15, 14, 12); fill(im, 2, 6, 14, 10, '#f0ecdf'); fill(im, 2, 9, 14, 10, '#d8d3c4')
    line(im, [(x, 8) for x in range(4, 12) if x % 3], '#3a3a8a')
    return im


def lamp_shade(lit):
    """A standard lamp's shade: pleated cloth gone the shade of old paper; lit, the light through it."""
    im = cloth('#fff0c4' if lit else '#e2d6b8', 52332 + lit, 1)
    for x in range(0, 16, 2):
        line(im, [(x, y) for y in range(16)], '#fff8dc' if lit else '#eee4c8')
        line(im, [(x + 1, y) for y in range(16) if y % 3], '#f6e2b0' if lit else '#cdc0a0')
    fill(im, 0, 14, 16, 16, '#c8a060' if lit else '#a89068'); fill(im, 0, 14, 16, 15, '#dcb878' if lit else '#bca47a')
    return im


# ------------------------------------------------------------------------------------------------ the hall
# Nessa's face, eight pixels square: the same face in every photograph and, later, under the frost. H hair, S skin, s shade, W white of
# the eye, I iris, M mouth.
NESSA_FACE = ['HHHHHHHH',
              'HHHHHHHH',
              'HHHHHHHH',
              'HSSSSSSH',
              'HWISSIWH',
              'HSSssSSH',
              'HSSMMSSH',
              'HSSSSSSH']
NESSA = {'H': '#2a1e18', 'S': '#e8c4a0', 's': '#d4ac88', 'W': '#f4f0e8', 'I': '#4a3a2e', 'M': '#c8846e'}


def school_photos(year):
    """One school photograph to a cell, in its frame on the photographer's blue: Nessa's eight-pixel face, the same face each year,
    the girl a little older in each. The first is a small child's, low in the frame, her hair short and a rosy jumper; then sat up
    taller; then her hair grown past her jaw; then the school jumper with its collar, taller again."""
    im = clear()
    frame(im, 2, 1, 14, 15, '#3a2a1a')
    for x in range(2, 14):
        dot(im, x, 1, '#5a4430')
    for y in range(1, 15):
        dot(im, 2, y, '#5a4430')
    for x in range(2, 14):
        dot(im, x, 14, '#24180c')
    for y in range(1, 15):
        dot(im, 13, y, '#24180c')
    fill(im, 3, 2, 13, 14, '#7f9ec4')
    for y in range(2, 14):
        for x in range(3, 13):
            if abs(x - 7.5) + abs(y - 7) < 4:
                dot(im, x, y, '#8eaccf')
    top = (5, 4, 4, 3)[year]
    jumper = '#a8364f' if year < 3 else '#2a4a7a'
    # Shoulders and jumper under the face, to the frame's foot.
    fill(im, 3, top + 8, 13, 14, jumper)
    if year == 3:
        dot(im, 6, top + 8, '#f4f0e8'); dot(im, 9, top + 8, '#f4f0e8')
    # Hair past the jaw from the third year: down either side of the face onto the shoulders.
    if year >= 2:
        fill(im, 3, top + 3, 4, top + 10, NESSA['H']); fill(im, 12, top + 3, 13, top + 10, NESSA['H'])
    for gy, row in enumerate(NESSA_FACE):
        for gx, c in enumerate(row):
            if year == 0 and gy == 7 and c == 'H':
                c = 'S'
            dot(im, 4 + gx, top + gy, NESSA[c])
    dot(im, 12, 2, '#c4d6ea'); dot(im, 11, 2, '#a8c0dc'); dot(im, 12, 3, '#a8c0dc')
    return im


def broken_photo():
    """0.4.548: the last school photograph after June's night with it (PellMark): the glass starred across her face from a point by her
    eye, the cracks running out to the frame in pale lines, a corner of the glass gone where the photograph shows duller."""
    im = school_photos(3)
    cx, cy = 7, 6
    for dx, dy, n in ((1, 0, 6), (-1, 0, 4), (0, 1, 7), (0, -1, 4), (1, 1, 6), (-1, -1, 3), (1, -1, 4), (-1, 1, 5)):
        x, y = cx, cy
        for i in range(n):
            x += dx; y += dy
            if 3 <= x < 13 and 2 <= y < 14:
                dot(im, x, y, '#eef2f6' if i % 2 == 0 else '#c8d0d8')
    dot(im, cx, cy, '#ffffff'); dot(im, cx + 1, cy, '#dfe6ee'); dot(im, cx, cy + 1, '#dfe6ee')
    for x, y in ((11, 12), (12, 12), (12, 11), (12, 13), (11, 13), (10, 13)):
        dot(im, x, y, '#5a5048', 70)
    return im


def hooks():
    """The empty hooks after the last photograph, hammered in at the photographs' spacing, one to a cell: on the wall only the faint
    pencil cross each was measured to (the hook itself stands out from it, in the model)."""
    im = clear()
    for x0 in HOOK_U:
        for dx, dy in ((0, -1), (-1, 0), (0, 0), (1, 0), (0, 1)):
            dot(im, x0 + dx, HOOK_V + dy, '#9a948a', 45)
    return im


# Where each hook goes in, on the wall's face as drawn (u across, v down): over the middle of where a frame would hang.
HOOK_U, HOOK_V = (7,), 2


def coat_hook(stage):
    """The hook board inside the front door: a small coat on one peg and, on the other, a long white cane with a red band. Stage 1
    the cane gone, its peg bare; stage 2 the coat gone as well."""
    im = clear()
    board = wood('#7a5232', 52293)
    for y in (1, 2):
        for x in range(16):
            im.putpixel((x, y), board.getpixel((x, y)))
    for x in range(16):
        dot(im, x, 1, shade(im.getpixel((x, 1)), 12)); dot(im, x, 2, shade(im.getpixel((x, 2)), -10))
    for x in (4, 11):
        dot(im, x, 3, '#d8b860'); dot(im, x, 4, '#9a7a2a')
    if stage < 2:
        coat = cloth('#c8a040', 52294)
        for y in range(4, 14):
            for x in range(2, 7):
                im.putpixel((x, y), coat.getpixel((x, y)))
        fill(im, 2, 4, 7, 5, '#a8862a'); dot(im, 4, 6, '#6a4a1a'); dot(im, 4, 9, '#6a4a1a')
        for y in range(5, 14):
            dot(im, 2, y, shade(im.getpixel((2, y)), 14)); dot(im, 6, y, shade(im.getpixel((6, y)), -16))
        fill(im, 3, 10, 4, 12, '#b08c30')
        fill(im, 3, 14, 6, 15, '#c8a040'); dot(im, 5, 14, '#a8862a')
    if stage < 1:
        for y in range(4, 16):
            dot(im, 11, y, '#f4f4f0')
        dot(im, 11, 13, '#c0392b'); dot(im, 11, 14, '#c0392b'); dot(im, 12, 4, '#f4f4f0')
    return im


# ------------------------------------------------------------------------------------------------ Nessa's room
def quilt(foot):
    """A child's quilt: pale squares stitched with small rabbits; at the foot, along the edge, the dent where June sits."""
    im = new('#e7dcc8', 52230 + foot, 2)
    for y in range(0, 16, 4):
        for x in range(0, 16, 4):
            c = ['#b9cfe0', '#e6c4cf', '#d5e3c4', '#efe0b8'][((x + y) // 4) % 4]
            patch = cloth(c, 52240 + x + y * 4)
            for yy in range(y, y + 4):
                for xx in range(x, x + 4):
                    im.putpixel((xx, yy), patch.getpixel((xx, yy)))
            dot(im, x + 1, y + 1, '#ffffff'); dot(im, x + 2, y + 2, '#a89a86')
            dot(im, x + 3, y + 1, shade(c, -16)); dot(im, x + 3, y + 3, shade(c, -16)); dot(im, x + 1, y + 3, shade(c, -16))
    if foot:
        for x in range(9, 16):
            for y in range(10, 16):
                r, g, b, _ = im.getpixel((x, y))
                im.putpixel((x, y), (max(0, r - 26), max(0, g - 26), max(0, b - 22), 255))
    return im


def pillow():
    im = cloth('#f4f0e4', 52232, 1); fill(im, 0, 0, 16, 1, '#e2dccb'); fill(im, 0, 15, 16, 16, '#e2dccb')
    for x in range(16):
        dot(im, x, 1, shade(im.getpixel((x, 1)), 6)); dot(im, x, 14, shade(im.getpixel((x, 14)), -8))
    for i in range(5, 11):
        dot(im, i, i - 1, shade(im.getpixel((i, i - 1)), -9))
    return im


def bed_frame():
    im = enamel('#e8e2d2', 52233)
    for y in range(16):
        dot(im, 0, y, '#d8d2c0'); dot(im, 1, y, '#f2eee2'); dot(im, 14, y, '#d4cdb9'); dot(im, 15, y, '#c4bda8')
        if y % 5 == 2:
            dot(im, 6, y, '#ddd6c4'); dot(im, 10, y + 1, '#ddd6c4')
    return im


def veneer(seed=52234):
    """Pale timber veneer: the children's furniture, bought as a set."""
    return wood('#c99a62', seed)


# The dots on each drawer's label, two cells of two by three (what each says is for the slice that reads them).
BRAILLE = [((0, 0), (0, 1), (1, 0), (3, 0), (4, 1), (3, 2)), ((0, 0), (1, 1), (0, 2), (3, 0), (3, 1), (4, 0)), ((1, 0), (0, 1), (1, 2), (3, 1), (4, 0), (4, 2))]


def drawers_front():
    """Three drawers, each with a round knob and beside it a clear label strip, its raised dots catching the light."""
    im = veneer(52235)
    for i, y0 in enumerate((1, 6, 11)):
        for y in range(y0, y0 + 4):
            for x in range(1, 15):
                dot(im, x, y, shade(im.getpixel((x, y)), 8))
        bevel(im, 1, y0, 15, y0 + 4, 14); fill(im, 1, y0 + 4, 15, y0 + 5, '#7a5430')
        fill(im, 10, y0 + 1, 12, y0 + 3, '#7a5430'); dot(im, 10, y0 + 1, '#b08a60'); dot(im, 11, y0 + 2, '#5a3c20')
        fill(im, 2, y0 + 1, 8, y0 + 4, '#e6dcc4'); fill(im, 2, y0 + 1, 8, y0 + 2, '#f2ebda')
        for dx, dy in BRAILLE[i]:
            dot(im, 2 + dx, y0 + 1 + dy, '#9a8c74')
    return im


def cutlery_front():
    """The little chest Callum keeps the cutlery in: two plain drawers, a wooden knob on each (its body shows the lower ten rows)."""
    im = veneer(52248)
    for y0 in (6, 11):
        for y in range(y0, y0 + 4):
            for x in range(1, 15):
                dot(im, x, y, shade(im.getpixel((x, y)), 8))
        bevel(im, 1, y0, 15, y0 + 4, 14); fill(im, 1, y0 + 4, 15, y0 + 5, '#7a5430')
        fill(im, 7, y0 + 1, 9, y0 + 3, '#7a5430'); dot(im, 7, y0 + 1, '#b08a60'); dot(im, 8, y0 + 2, '#5a3c20')
    return im


def talking_clock():
    """A talking clock: a white box with a big speaker grille and one large button on top."""
    im = plastic('#f2f0ea', 52236)
    bevel(im, 0, 0, 16, 16, 10)
    fill(im, 3, 3, 13, 13, '#d0cdc4'); inset(im, 3, 3, 13, 13, 12)
    for y in range(4, 13, 2):
        for x in range(4, 13, 2):
            dot(im, x, y, '#5a5852'); dot(im, x + 1, y + 1, '#e0ddd4')
    return im


def desk_top():
    """The desk: a magnifier on its stand, a sheet of paper under it with the letters drawn large."""
    im = veneer(52237)
    for x in range(3, 11):
        dot(im, x, 13, shade(im.getpixel((x, 13)), -26))
    for y in range(4, 13):
        dot(im, 10, y, shade(im.getpixel((10, y)), -26))
    fill(im, 2, 3, 10, 13, '#fbfaf4')
    for y in (5, 8, 11):
        fill(im, 3, y, 8, y + 1, '#2a2a2a')
    for y in range(6, 12):
        for x in range(9, 15):
            d = ((x - 11.5) ** 2 + (y - 8.5) ** 2) ** .5
            if d < 2.8:
                dot(im, x, y, '#c8d8e0', 160)
            elif d < 3.4:
                dot(im, x, y, '#2a2a2a')
    dot(im, 10, 7, '#f4fafc')
    return im


def large_books():
    """The shelf of large-print books: thick spines, the titles in letters big enough to see across the room, a gap where one is out."""
    im = wood('#c99a62', 52238)
    for row, y0 in enumerate((1, 9)):
        fill(im, 0, y0 + 6, 16, y0 + 7, '#8a6438')
        x = 1; rnd = random.Random(52239 + row)
        while x < 15:
            w = rnd.choice([2, 3])
            if row == 1 and x == 8:
                x += 3; continue
            c = rnd.choice(['#8a4a3a', '#3a5a7a', '#a8863e', '#4a6a4a', '#6a4a5e'])
            fill(im, x, y0, x + w, y0 + 6, c); fill(im, x, y0 + 2, x + w, y0 + 4, '#ece4cc'); dot(im, x, y0, shade(c, 12))
            for yy in (y0 + 1, y0 + 5):
                for xx in range(x, x + w):
                    dot(im, xx, yy, shade(c, -22))
            for yy in range(y0, y0 + 6):
                if yy not in (y0 + 2, y0 + 3):
                    dot(im, x, yy, shade(im.getpixel((x, yy)), 14))
                    if x + w - 1 < 16:
                        dot(im, x + w - 1, yy, shade(im.getpixel((x + w - 1, yy)), -14))
            x += w
    return im


def wardrobe_front(upper, opened):
    """The wardrobe: two doors of the same veneer, a knob each. Open, inside on the left a cane the same as Tilly's, shorter."""
    im = veneer(52240 + upper)
    if opened:
        fill(im, 1, 0, 15, 16, '#3a2c1e')
        if upper:
            fill(im, 2, 1, 14, 2, '#b8b0a0')
            for x in (4, 8, 12):
                line(im, [(x, 2), (x, 3)], '#9a9488')
        else:
            for y in range(1, 16):
                dot(im, 4, y, '#f4f4f0')
            dot(im, 4, 12, '#c0392b'); dot(im, 4, 13, '#c0392b')
            fill(im, 8, 10, 14, 16, '#e6c4cf')
        return im
    for y in range(16):
        dot(im, 8, y, '#6e4c28'); dot(im, 0, y, shade(im.getpixel((0, y)), 12)); dot(im, 9, y, shade(im.getpixel((9, y)), 12))
        dot(im, 7, y, shade(im.getpixel((7, y)), -12)); dot(im, 15, y, shade(im.getpixel((15, y)), -12))
    if not upper:
        dot(im, 6, 1, '#e8dcb4'); dot(im, 10, 1, '#e8dcb4'); dot(im, 6, 2, '#8a6438'); dot(im, 10, 2, '#8a6438')
    return im


def switch_plate():
    """The light switch: a white plate, and over its rocker a strip of bumpy tape that a hand can find in the dark."""
    im = clear()
    fill(im, 5, 3, 11, 13, '#f4f2ea'); bevel(im, 5, 3, 11, 13, 10)
    fill(im, 7, 6, 9, 10, '#e4e0d4'); inset(im, 7, 6, 9, 10, 10)
    for y in (6, 8):
        dot(im, 7, y, '#c8a040'); dot(im, 8, y + 1, '#c8a040')
    return im


def height_marks(upper, tilly):
    """The door frame's pencil marks: a dash and a date for each one, climbing, and stopping. Below the last, a fresh one in a
    different pencil, with a small N beside it.

    0.4.542, the house's aftermath (PellAftermath), on the upper cell: tilly 2, the house empty, the fresh mark still there and the N
    beside it rubbed out, a grey smear on the wall; tilly 3, the house lived in, the N still beside it and one more above the rest,
    later, in the same pencil, with its own N."""
    im = clear()
    fill(im, 6, 0, 10, 16, '#f2efe6'); line(im, [(6, y) for y in range(16)], '#dcd8cc')
    marks = [(15, 0), (12, 0), (9, 0), (6, 0), (3, 0), (1, 1)] if not upper else [(15, 1), (13, 0), (11, 1)]
    for y, short in marks:
        line(im, [(6, y), (7, y), (8, y)], '#6a6a6a')
        if not short:
            dot(im, 9, y, '#8a8a8a')
    if tilly and upper:
        # Below the last of hers, the new one, darker, and the N.
        line(im, [(6, 14), (7, 14), (8, 14), (9, 14)], '#2a2a5a')
        if tilly == 2:
            # Rubbed out with a thumb: the graphite smeared across the paint, a ghost of the letter's strokes in it.
            for x, y, c in ((10, 11, '#b4b2c0'), (11, 11, '#c8c6d0'), (10, 12, '#a8a6b8'), (11, 12, '#bcbac8'), (12, 12, '#c8c6d0'),
                            (10, 13, '#b4b2c0'), (11, 13, '#a8a6b8'), (12, 13, '#b4b2c0'), (13, 13, '#c8c6d0'), (11, 14, '#bcbac8'),
                            (12, 14, '#a8a6b8'), (13, 14, '#bcbac8'), (14, 14, '#d0ced6'), (10, 15, '#c8c6d0'), (13, 15, '#b4b2c0'),
                            (14, 13, '#d0ced6')):
                dot(im, x, y, c)
        else:
            text(im, 10, 11, 'N', '#2a2a5a')
        if tilly == 3:
            # Higher than any of hers: grown since, measured, dated, and marked the same.
            line(im, [(6, 7), (7, 7), (8, 7), (9, 7)], '#2a2a5a')
            text(im, 10, 4, 'N', '#2a2a5a')
    return im


def border(crooked):
    """The wallpaper border along the top of the walls: a child's pattern of ducks and boats. Behind the bed a length of it was
    steamed off and put back, a little crooked, its pattern a pixel out."""
    im = clear()
    off = 1 if crooked else 0
    fill(im, 0, 8 + off, 16, 16, '#efe6c8'); line(im, [(x, 8 + off) for x in range(16)], '#c8a868'); line(im, [(x, 15) for x in range(16)], '#c8a868')
    for x0 in (1, 9):
        x = x0 + (2 if crooked else 0)
        fill(im, x, 11 + off, x + 4, 13 + off, '#e8b830'); dot(im, x + 4, 11 + off, '#e8b830'); dot(im, x + 4, 11 + off, '#d87a2a')
        dot(im, x + 1, 11 + off, '#3a3a3a')
    if crooked:
        line(im, [(0, 9), (1, 9)], '#b8a070')
    return im


# ------------------------------------------------------------------------------------------------ Callum's room
def mattress(foot):
    """A mattress on the floor and no bed: striped ticking, a folded blanket at the head."""
    im = cloth('#e2ddd0', 52245 + foot, 1)
    for x in range(0, 16, 3):
        for y in range(16):
            dot(im, x, y, '#9fb0c4' if y % 2 else '#93a4b8')
    for x, y in ((4, 4), (10, 4), (4, 12), (10, 12)):
        dot(im, x, y, '#bdb8aa'); dot(im, x - 1, y - 1, '#f2eee4')
    if not foot:
        blanket = cloth('#7a8a6a', 52246)
        for y in range(10, 15):
            for x in range(1, 15):
                im.putpixel((x, y), blanket.getpixel((x, y)))
        fill(im, 1, 10, 15, 11, '#5e6e50'); fill(im, 1, 12, 15, 13, '#6c7c5c'); fill(im, 1, 14, 15, 15, '#56664a')
    return im


def circle_cell(cx, cz, ox, oz, r):
    """One cell of the circle painted on Callum's floor, in the line's white: (ox, oz) is the cell's offset from the circle's
    centre, in blocks."""
    im = clear()
    for y in range(16):
        for x in range(16):
            px = ox + (x + .5) / 16; pz = oz + (y + .5) / 16
            d = (px * px + pz * pz) ** .5
            if abs(d - r) < .045:
                dot(im, x, y, '#f4f1e8', 235)
            elif abs(d - r) < .075:
                dot(im, x, y, '#e8e4d8', 120)
    return im


def shoe(colour, top, seed):
    """A small shoe's leather: from the side its colour with the clean white sole along the bottom; from above, the laces."""
    im = enamel(colour, seed, 2)
    if top:
        for y in range(0, 16, 2):
            for x in range(16):
                if (x + y // 2) % 4 == 1:
                    dot(im, x, y, '#ecebe4'); dot(im, x, y + 1, shade(colour, -24))
                elif (x + y // 2) % 4 == 3:
                    dot(im, x, y, shade(colour, -30))
    else:
        fill(im, 0, 0, 16, 1, shade(colour, 16))
        for x, y in ((3, 6), (4, 7), (5, 7), (6, 6)):
            dot(im, x, y, shade(colour, -18))
        for x in range(10, 15):
            dot(im, x, 9, shade(colour, 12))
        fill(im, 0, 15, 16, 16, '#ecebe4')
        for x in range(0, 16, 2):
            dot(im, x, 14, '#d2d0c6')
    return im


# His shoes, lined up at the door: two small pairs, heels to the wall, laces done up, the soles clean. He never goes out.
SHOES = ((2.5, 'b'), (5, 'b'), (9, 'r'), (11.5, 'r'))


def cutlery(knives):
    """The drawer he keeps: the house's missing spoons and forks laid in rows, neat as a shop's. From the second visit, a knife; on the
    last night (0.4.538) the knife gone, and the felt still pressed down where it lay."""
    im = wood('#c99a62', 52247)
    felt = cloth('#e8dcc4', 52249, 1)
    for y in range(1, 15):
        for x in range(1, 15):
            im.putpixel((x, y), felt.getpixel((x, y)))
    inset(im, 1, 1, 15, 15, 12)
    for i, x in enumerate(range(2, 14, 2)):
        line(im, [(x, y) for y in range(3, 13)], '#b9bec1'); line(im, [(x + 1, y) for y in range(4, 13)], '#d0c4ac')
        if i % 2:
            dot(im, x, 3, '#dfe3e5'); dot(im, x, 4, '#dfe3e5')
        else:
            dot(im, x - 1 if x > 2 else x, 3, '#dfe3e5'); dot(im, x, 2, '#dfe3e5')
    if knives == 1:
        line(im, [(14, y) for y in range(3, 10)], '#e9ecee'); line(im, [(14, y) for y in range(10, 14)], '#1e1a16')
    elif knives == 2:
        line(im, [(14, y) for y in range(3, 14)], '#cdbd9f'); dot(im, 14, 3, '#c2b193'); dot(im, 14, 13, '#c2b193')
    return im


def living():
    return {'recliner': recliner(False), 'recliner_worn': recliner(True), 'tape_cabinet': tape_cabinet(), 'tv_front': tv_front(), 'tv_lit': tv_tape(),
            'wood_dark': wood_dark(), 'mantel_front': mantel_front(), 'seat_cushion': seat_cushion(), 'remote': remote(), 'mug_top': mug_top(),
            'tape_case': tape_case(), 'lamp_shade': lamp_shade(False), 'lamp_shade_lit': lamp_shade(True)}


def hall():
    out = {f'photos_{i}': school_photos(i) for i in range(4)}
    out['photos_broken'] = broken_photo()
    out.update({'hooks': hooks(), 'coat_0': coat_hook(0), 'coat_1': coat_hook(1), 'coat_2': coat_hook(2)})
    return out


def nessa():
    return {'quilt_head': quilt(0), 'quilt_foot': quilt(1), 'pillow': pillow(), 'bed_frame': bed_frame(), 'veneer': veneer(),
            'drawers_front': drawers_front(), 'talking_clock': talking_clock(), 'desk_top': desk_top(), 'large_books': large_books(),
            'wardrobe_lo': wardrobe_front(0, 0), 'wardrobe_hi': wardrobe_front(1, 0), 'wardrobe_lo_open': wardrobe_front(0, 1), 'wardrobe_hi_open': wardrobe_front(1, 1),
            'switch': switch_plate(), 'marks_lo': height_marks(0, 0), 'marks_hi': height_marks(1, 0), 'marks_hi_tilly': height_marks(1, 1),
            'marks_hi_rubbed': height_marks(1, 2), 'marks_hi_later': height_marks(1, 3),
            'border': border(0), 'border_crooked': border(1)}


def callum():
    return {'mattress_head': mattress(0), 'mattress_foot': mattress(1), 'shoe_blue': shoe('#2a4a7a', 0, 52308), 'shoe_blue_top': shoe('#2a4a7a', 1, 52309),
            'shoe_red': shoe('#8a3a2a', 0, 52310), 'shoe_red_top': shoe('#8a3a2a', 1, 52311), 'cutlery_0': cutlery(0), 'cutlery_1': cutlery(1), 'cutlery_2': cutlery(2),
            'cutlery_front': cutlery_front()}


# ------------------------------------------------------------------------------------------------ the bathroom

def vanity_front():
    """The vanity under the basin: one door, a towel on its rail."""
    im = enamel('#f2efe6', 52251)
    bevel(im, 1, 1, 15, 13, 10); dot(im, 12, 7, '#7a7468'); dot(im, 12, 6, '#c8c2b4')
    towel = cloth('#7aa0b8', 52250)
    for y in range(3, 10):
        for x in range(2, 9):
            im.putpixel((x, y), towel.getpixel((x, y)))
    fill(im, 2, 3, 9, 4, '#5a8098'); fill(im, 2, 6, 9, 7, '#9abcd0'); line(im, [(3, 9), (5, 9), (7, 9)], '#5a8098')
    for y in range(4, 10):
        dot(im, 8, y, shade(im.getpixel((8, y)), -14))
    fill(im, 0, 13, 16, 16, '#3b3832')
    return im


def basin_top():
    """The basin from above: white, the bowl sunk into it with its rim in shadow and its back darker, the plug. The taps and the cup
    with two toothbrushes (one old and dry, kept; the new one) stand behind it, in the model."""
    im = enamel('#f6f4ee', 52252)
    for y in range(2, 14):
        for x in range(1, 13):
            d = (x - 6.5) ** 2 / 20 + (y - 7.5) ** 2 / 25
            if d < 1:
                dot(im, x, y, '#cfcdc4' if d > .72 else shade('#e8e6de', -int((y - 2) * 2.2)))
    fill(im, 6, 8, 8, 9, '#7a7a74'); dot(im, 6, 8, '#9a9a94'); dot(im, 7, 9, '#c8c6be')
    for x, y in ((3, 11), (4, 12), (9, 12), (10, 11)):
        dot(im, x, y, '#f8f7f2')
    return im


def mirror(steamed):
    """The mirror cabinet over the basin: chrome edge, the glass. Steamed, a word shows on it that was written once with a finger:
    NESSA."""
    im = clear()
    fill(im, 1, 1, 15, 15, '#c9ced1'); bevel(im, 1, 1, 15, 15, 18)
    fill(im, 2, 2, 14, 14, '#9fb4bd' if not steamed else '#d6dcdc')
    if not steamed:
        for y in range(2, 14):
            for x in range(2, 14):
                if x + y < 10:
                    dot(im, x, y, '#a9bec6')
                elif x + y > 22:
                    dot(im, x, y, '#93a8b1')
        for i in range(4):
            dot(im, 3 + i, 9 - i, '#c8dce4'); dot(im, 4 + i, 9 - i, '#c8dce4')
        for i in range(2):
            dot(im, 9 + i, 12 - i, '#b8ccd4')
    else:
        text(im, 3, 3, 'NES', '#8fa6ae', 0)
        text(im, 4, 9, 'SA', '#8fa6ae', 0)
    return im


def bath_side(end):
    """The bath's apron: white enamel; at its tap end the taps and the shower's hose."""
    im = enamel('#f4f2ec', 52253 + end)
    fill(im, 0, 0, 16, 1, '#fbfaf6'); fill(im, 0, 1, 16, 2, '#e4e1d8'); fill(im, 0, 2, 16, 3, '#ebe8e0')
    fill(im, 0, 15, 16, 16, '#dedbd2')
    return im


def bath_top(end):
    """Inside the bath, from above: the non-slip mat down its length, its suckers. End 0 is the tap end (the south as drawn), the
    plughole under the taps; end 1 the far end, where a bath toy sits on the rack, a duck sun-bleached on one side."""
    im = enamel('#f4f2ec', 52255 + end)
    for i in range(16):
        dot(im, i, 0, '#dcd9d0'); dot(im, 0, i, '#e2dfd6'); dot(im, 15, i, '#fbfaf6')
    if end == 0:
        fill(im, 2, 0, 14, 14, '#e8e6de'); fill(im, 4, 0, 12, 10, '#9ac4c0')
        for y in range(1, 10, 2):
            for x in range(5, 12, 2):
                dot(im, x, y, '#7aa8a4')
        dot(im, 7, 12, '#b8bcc0'); dot(im, 8, 12, '#b8bcc0')
    else:
        fill(im, 2, 2, 14, 16, '#e8e6de'); fill(im, 4, 6, 12, 16, '#9ac4c0')
        for y in range(7, 16, 2):
            for x in range(5, 12, 2):
                dot(im, x, y, '#7aa8a4')
        fill(im, 9, 2, 13, 4, '#c8ccd0'); fill(im, 10, 2, 12, 4, '#f2e6a8'); dot(im, 12, 2, '#e8c070')
    return im


def toilet():
    im = enamel('#f6f4ee', 52257); fill(im, 3, 3, 13, 13, '#e2e0d8'); bevel(im, 3, 3, 13, 13, 10)
    fill(im, 5, 5, 11, 11, '#c8d8dc'); inset(im, 5, 5, 11, 11, 14); dot(im, 6, 6, '#e8f2f4'); dot(im, 7, 6, '#dceaee')
    return im


# ------------------------------------------------------------------------------------------------ the laundry
def freezer_lid(end, unlocked=False):
    """The chest freezer's lid: white, frost at the seal. On it at one end a folded towel and a hairbrush with long hair in it."""
    im = enamel('#f4f3ee', 52258 + end)
    for x in range(16):
        dot(im, x, 0, '#e8f2f6' if x % 3 else '#ffffff'); dot(im, x, 15, '#e8f2f6' if x % 4 else '#d4e4ec')
        dot(im, x, 1, '#eef0ee')
    if end == 1:
        fill(im, 3, 3, 12, 10, '#d6a8a8'); fill(im, 3, 9, 12, 10, '#b88888'); line(im, [(4, 4), (11, 4)], '#e8c4c4')
        fill(im, 5, 11, 12, 13, '#3a2a20'); fill(im, 3, 11, 6, 13, '#7a4a2a')
        for x in range(6, 12):
            dot(im, x, 10, '#4a3626')
        line(im, [(12, 10), (13, 9), (14, 9)], '#4a3626')
    return im


def freezer_front(end, unlocked=False):
    """The freezer's front: the hum's grille at the bottom, frost along the seal, and on the lock end a padlock through the hasp."""
    im = enamel('#f4f3ee', 52260 + end)
    for x in range(16):
        dot(im, x, 0, '#e2e0d8'); dot(im, x, 1, '#dce8ee' if x % 3 else '#f4fbfe'); dot(im, x, 2, '#e4e2da')
    for y in (12, 14):
        for x in range(2, 14, 2):
            dot(im, x, y, '#8a8880'); dot(im, x + 1, y, '#d4d2ca')
    if end == 0:
        fill(im, 7, 1, 10, 4, '#b8bcc0'); dot(im, 7, 1, '#dce0e3'); dot(im, 9, 3, '#8e9396')
        if not unlocked:
            fill(im, 7, 4, 10, 8, '#c8a040'); dot(im, 7, 4, '#e8c868'); dot(im, 7, 5, '#dcb850'); fill(im, 9, 5, 10, 8, '#a07c28')
            fill(im, 8, 3, 9, 5, '#8a8e92'); dot(im, 8, 6, '#3a3a3a'); dot(im, 8, 7, '#5a4a2a')
    return im


def freezer_inside():
    """0.4.540: inside the freezer with its lid up: white enamel gone a cold grey-blue under a fur of frost, thicker toward the rim."""
    im = enamel('#e4edf1', 52262)
    rnd = random.Random(52263)
    for y in range(16):
        for x in range(16):
            if rnd.random() < .12 + .025 * (15 - y):
                dot(im, x, y, '#ffffff' if rnd.random() < .5 else '#f3f9fb')
    return im


def freezer_plastic():
    """0.4.540: the frosted plastic over what lies in the freezer: clouded white and creased, and clear where the frost is thin, so
    that what is under it shows through."""
    im = clear()
    rnd = random.Random(52264)
    for y in range(16):
        for x in range(16):
            r = rnd.random()
            if r < .4:
                continue
            dot(im, x, y, '#f4f8fa' if r < .72 else '#dde8ed' if r < .9 else '#ffffff')
    line(im, [(1, 3), (2, 3), (3, 4), (4, 4), (5, 5), (6, 5), (7, 4), (8, 4), (9, 4), (10, 5), (11, 5), (12, 6), (13, 6)], '#c6d4db')
    line(im, [(0, 11), (1, 11), (2, 10), (3, 10), (4, 10), (5, 11), (6, 12), (7, 12), (8, 11), (9, 11), (10, 11), (11, 12)], '#c6d4db')
    return im


def freezer_lid_under():
    """0.4.540: the lid's underside, stood up against the wall: white, the grey seal round its edge, and frost on it."""
    im = enamel('#eff1ef', 52265)
    frame(im, 0, 0, 16, 16, '#9aa0a2'); frame(im, 1, 1, 15, 15, '#c4cacc')
    rnd = random.Random(52266)
    for _ in range(26):
        dot(im, rnd.randrange(2, 14), rnd.randrange(2, 14), '#ffffff' if rnd.random() < .6 else '#e6f0f4')
    return im


def stool():
    """The step stool beside the freezer: two steps of painted wood, the top worn bare in the middle where someone sits."""
    im = enamel('#d6cdb8', 52262, 4)
    bare = wood('#b89a72', 52263)
    for y in range(3, 13):
        for x in range(3, 13):
            d = max(abs(x - 7.5), abs(y - 7.5))
            if d < 4 or (d < 5 and (x * 7 + y * 5) % 3 == 0):
                im.putpixel((x, y), bare.getpixel((x, y)))
    bevel(im, 0, 0, 16, 16, 10)
    return im


def power_point():
    """A power point low on the wall: the freezer's plug in it, its switch taped down, and a note taped over the tape: DO NOT
    SWITCH OFF."""
    im = clear()
    fill(im, 3, 9, 13, 15, '#f4f2ea'); bevel(im, 3, 9, 13, 15, 12)
    fill(im, 5, 11, 7, 14, '#3a3a3a'); dot(im, 5, 11, '#5a5a5a'); fill(im, 9, 11, 11, 13, '#e8e4d4'); inset(im, 9, 11, 11, 13, 10)
    fill(im, 1, 1, 15, 2, '#d8c88a')
    fill(im, 2, 1, 14, 9, '#fbf6c8'); text(im, 3, 2, 'DO', '#c03a3a', 0); text(im, 3, 2 + 5, '', '#c03a3a')
    line(im, [(9, 3), (11, 3), (9, 5), (12, 5), (9, 7), (12, 7)], '#c03a3a')
    return im


def power_point_bare():
    """0.4.548: the power point with its note taken off (PellMark): the freezer's plug still in it and its switch still taped down; on
    the wall above, the tape's ghost at the corners where the note was."""
    im = clear()
    fill(im, 3, 9, 13, 15, '#f4f2ea'); bevel(im, 3, 9, 13, 15, 12)
    fill(im, 5, 11, 7, 14, '#3a3a3a'); dot(im, 5, 11, '#5a5a5a'); fill(im, 9, 11, 11, 13, '#e8e4d4'); inset(im, 9, 11, 11, 13, 10)
    fill(im, 8, 10, 12, 11, '#d8c88a', 200)
    for x in (1, 2, 13, 14):
        dot(im, x, 1, '#d8c88a', 150)
    dot(im, 2, 8, '#d8c88a', 90); dot(im, 13, 8, '#d8c88a', 90)
    return im


def cord():
    """The freezer's cord across the lino, taped down every hand's width."""
    im = clear()
    for z in range(16):
        dot(im, 7, z, '#e8e6de'); dot(im, 8, z, '#cfccc2')
    for z in (2, 9):
        fill(im, 5, z, 11, z + 2, '#b8a878', 200)
    return im


def trough():
    """The laundry trough: a deep steel tub, a tap over it."""
    im = metal('#b9bec1', 52263); fill(im, 2, 2, 14, 14, '#8e9497'); inset(im, 2, 2, 14, 14, 18)
    inner = metal('#a6acaf', 52264)
    for y in range(3, 13):
        for x in range(3, 13):
            im.putpixel((x, y), inner.getpixel((x, y)))
    inset(im, 3, 3, 13, 13, 10); dot(im, 8, 8, '#3a3e40'); dot(im, 9, 9, '#c4cacd')
    return im


def bucket_top():
    """The bucket from above, a scrubbing brush across it, its bristles worn to one side."""
    im = clear()
    for y in range(16):
        for x in range(16):
            d = ((x - 7.5) ** 2 + (y - 7.5) ** 2) ** .5
            if d < 6.5:
                dot(im, x, y, ('#5a9ad0' if x + y < 13 else '#3a7ab8' if x + y < 18 else '#2a6098') if d > 5.4 else '#2e5e8e' if x + y > 12 else '#24507a')
    fill(im, 2, 6, 14, 9, '#b8946a'); fill(im, 2, 6, 14, 7, '#d0ac80'); fill(im, 2, 8, 14, 9, '#9a7650')
    for x in range(3, 13):
        dot(im, x, 9, '#e2d2a0' if x % 2 else '#c8b888')
    return im


def chlorine():
    """A drum of pool chlorine, far too big for one pool, its label sun-faded."""
    im = plastic('#e8e6de', 52264)
    for x in range(0, 16, 4):
        for y in range(16):
            dot(im, x, y, shade(im.getpixel((x, y)), -10)); dot(im, x + 1, y, shade(im.getpixel((x + 1, y)), 8))
    fill(im, 2, 5, 14, 12, '#2a6ab8'); fill(im, 3, 7, 13, 10, '#f4f4f0')
    line(im, [(4, 8), (6, 8), (8, 8), (10, 8)], '#2a6ab8')
    for x in range(9, 14):
        dot(im, x, 6, '#5a8ac8'); dot(im, x, 10, '#5a8ac8')
    return im


def washer_front():
    """A top-loading washing machine: white, its dials on the backsplash."""
    im = enamel('#f2f0ea', 52265)
    fill(im, 0, 0, 16, 4, '#dcd8cc'); fill(im, 0, 0, 16, 1, '#ebe8de'); fill(im, 0, 4, 16, 5, '#c8c4b8')
    for x in (3, 8, 12):
        dot(im, x, 2, '#5a5a56'); dot(im, x - 1, 1, '#b8b4a8'); dot(im, x + 1, 2, '#c4c0b4'); dot(im, x, 3, '#c4c0b4'); dot(im, x, 1, '#2a2a28')
    fill(im, 0, 14, 16, 16, '#3b3832'); fill(im, 0, 13, 16, 14, '#e2e0d8')
    return im


# ------------------------------------------------------------------------------------------------ the garage
def canvas(head=False):
    """The camp bed's canvas: khaki, a sleeping bag on it, unzipped; at the head (the south as drawn) its open top, and a jumper
    rolled up for a pillow."""
    im = cloth('#8a8a5a', 52266)
    bag = cloth('#3a5a8a', 52267)
    for y in range(16):
        for x in range(2, 14):
            im.putpixel((x, y), bag.getpixel((x, y)))
    for y in range(1, 16, 3):
        for x in range(2, 14):
            dot(im, x, y, '#2e4c78')
    for y in range(16):
        dot(im, 2, y, '#4a6c9c'); dot(im, 13, y, '#2a4468')
        if y % 2 == 0:
            dot(im, 12, y, '#c8ccd0')
    if head:
        fill(im, 2, 13, 14, 16, '#2a4a7a'); fill(im, 2, 13, 14, 14, '#4a6c9c')
        jumper = cloth('#8a3a3a', 52269)
        for y in range(10, 13):
            for x in range(4, 12):
                im.putpixel((x, y), jumper.getpixel((x, y)))
        fill(im, 4, 10, 12, 11, '#a84a4a'); fill(im, 4, 12, 12, 13, '#6e2c2c')
    return im


def aluminium():
    return metal('#b8bcbe', 52267)


def carton(label):
    """A cardboard box, taped shut, and its label in marker: NESSA, WINTER; NESSA, BOOKS; NESSA, DON'T. The last word is written
    smaller, and gone over twice."""
    im = card('#b8946a', 52268 + len(label))
    fill(im, 0, 7, 16, 9, '#c8a87a'); fill(im, 0, 7, 16, 8, '#dcc094')
    for x in range(0, 16, 5):
        dot(im, x, 8, '#b8986a')
    if label:
        text(im, 0, 1, 'NESSA', '#1e1a16', 0)
        text(im, 1 if label != "DON'T" else 2, 10, label[:4] if len(label) > 4 else label, '#1e1a16', 0 if len(label) > 4 else 1)
    return im


def pegboard(half, second):
    """The pegboard: hardboard with holes, each tool's outline painted on it in white. Some outlines are empty, one of them a
    knife's; from the second visit, a second knife's outline is empty as well."""
    im = card('#b08a5a', 52270 + half)
    for y in range(1, 16, 2):
        for x in range(1, 16, 2):
            dot(im, x, y, '#4e3622'); dot(im, x + 1, y + 1, '#c49e6e')
    if half == 0:
        line(im, [(3, y) for y in range(3, 12)] + [(2, 12), (4, 12)], '#f4f2ea')
        line(im, [(3, y) for y in range(4, 11)], '#5a5a5a')
        line(im, [(7, y) for y in range(2, 13)], '#f4f2ea'); fill(im, 6, 2, 9, 5, '#f4f2ea')
        line(im, [(12, y) for y in range(3, 10)], '#f4f2ea'); line(im, [(12, y) for y in range(10, 14)], '#f4f2ea')
    else:
        # The knives' outlines: blade and handle.
        for x, filled in ((4, False), (10, not second)):
            line(im, [(x, y) for y in range(2, 9)] + [(x + 1, y) for y in range(3, 9)], '#f4f2ea')
            fill(im, x, 9, x + 2, 14, '#f4f2ea')
            if filled:
                line(im, [(x, y) for y in range(3, 9)], '#c8ccd0'); fill(im, x, 9, x + 2, 13, '#2a2420')
    return im


def bulb():
    im = clear(); fill(im, 5, 5, 11, 12, '#f4eec0'); fill(im, 6, 4, 10, 5, '#b8b4a8')
    fill(im, 6, 6, 7, 10, '#fffbe4'); fill(im, 10, 6, 11, 11, '#e2d8a4'); fill(im, 6, 11, 10, 12, '#e8deb0')
    dot(im, 7, 4, '#d8d4c8'); dot(im, 9, 4, '#8e8a80')
    return im


def towel_rolled():
    im = cloth('#a8b8c8', 52272)
    for x in range(0, 16, 3):
        for y in range(16):
            dot(im, x, y, '#94a4b4' if y % 2 else '#8a9aaa')
    return im


# ------------------------------------------------------------------------------------------------ the shed
def tape_spines(upper):
    """The shed's shelf of tapes in plain cases: each spine with a date in June's hand, in order. Two dates written over, one case
    empty."""
    im = wood('#6a6050', 52273 + upper)
    fill(im, 0, 14, 16, 16, '#5a5040'); fill(im, 0, 14, 16, 15, '#7a705e')
    for x in range(1, 15, 2):
        fill(im, x, 2, x + 2, 14, '#1e1e1e'); dot(im, x, 2, '#3a3a3a'); dot(im, x, 13, '#3a3a3a')
        fill(im, x, 4, x + 2, 11, '#f0ecdf'); dot(im, x + 1, 10, '#d8d2c0')
        dot(im, x, 6, '#2a2a6a'); dot(im, x + 1, 8, '#2a2a6a')
    if upper:
        fill(im, 9, 4, 11, 11, '#2a2a2a')
        fill(im, 5, 5, 7, 8, '#3a3a8a')
    else:
        fill(im, 11, 6, 13, 9, '#3a3a8a')
    return im


def bench_top():
    """The workbench's top: scarred timber, a ring of white paint where a tin always stands."""
    im = wood('#8a6a4a', 52275, 3)
    for x, y in ((3, 3), (4, 3), (5, 4), (11, 9), (12, 10)):
        dot(im, x, y, '#5a4430'); dot(im, x, y + 1, '#a08060')
    for x, y in ((9, 2), (10, 2), (8, 13), (13, 5)):
        dot(im, x, y, '#4a3826')
    for y in range(9, 14):
        for x in range(2, 7):
            if abs(((x - 4) ** 2 + (y - 11) ** 2) ** .5 - 2) < .6:
                dot(im, x, y, '#ecebe4')
    return im


def deck_front():
    """The video deck: silver, its slot, and the tracking dial worn smooth."""
    im = metal('#a8acb0', 52276)
    bevel(im, 0, 0, 16, 16, 14)
    fill(im, 2, 5, 10, 7, '#1e1e1e'); fill(im, 2, 7, 10, 8, '#c8ccd0'); fill(im, 2, 4, 10, 5, '#7e8286')
    dot(im, 13, 6, '#4a4e50'); dot(im, 12, 6, '#7a7e80'); dot(im, 13, 7, '#7a7e80'); dot(im, 12, 5, '#d8dcde')
    dot(im, 3, 10, '#c03a3a'); line(im, [(5, 10), (6, 10), (8, 10)], '#5a5e60')
    return im


def portable_tv(lit=False):
    """The portable television: grey plastic, a small screen, a handle on top, an aerial bent back. 0.4.536: lit while a tape plays in
    the deck under it (PellTapes), its screen a cold grey glow with the tape's lines across it; the picture itself is drawn over it on
    the reader's own client (client.PellTapeClient)."""
    im = plastic('#7a7e80', 52277)
    bevel(im, 0, 0, 16, 16, 14)
    fill(im, 1, 2, 12, 12, '#1c1e1e')
    if lit:
        fill(im, 2, 3, 11, 11, '#8a9aa4')
        for y in range(3, 11):
            for x in range(2, 11):
                dot(im, x, y, shade('#8a9aa4', (5 if y % 2 else -9) + ((x * 7 + y * 3) % 5) * 3))
        fill(im, 2, 7, 11, 8, '#b4c2c8')
        dot(im, 13, 4, '#e86040'); dot(im, 13, 5, '#4a4e50')
        for y in (6, 8):
            dot(im, 13, y, '#d8d8d4'); dot(im, 13, y + 1, '#4a4e50')
        for y in range(10, 14):
            if y % 2 == 0:
                fill(im, 13, y, 15, y + 1, '#5a5e60')
        return im
    fill(im, 2, 3, 11, 11, '#3a4040'); inset(im, 2, 3, 11, 11, 8)
    dot(im, 3, 4, '#5a6464'); dot(im, 4, 4, '#4e5858'); dot(im, 3, 5, '#4e5858')
    for y in (4, 6, 8):
        dot(im, 13, y, '#d8d8d4'); dot(im, 13, y + 1, '#4a4e50')
    for y in range(10, 14):
        if y % 2 == 0:
            fill(im, 13, y, 15, y + 1, '#5a5e60')
    return im


def tin(label):
    """A paint tin: white paint run down its side. The full one's lid has a word on it in marker, too small to read from standing
    (a hand gets it: LINE)."""
    im = metal('#c8ccd0', 52278 + label)
    for y in range(16):
        dot(im, 0, y, '#e6eaec'); dot(im, 1, y, '#dde1e4'); dot(im, 14, y, '#a4a8ac'); dot(im, 15, y, '#8e9296')
    fill(im, 0, 0, 16, 1, '#9a9ea2'); fill(im, 0, 15, 16, 16, '#9a9ea2')
    for x in (3, 4, 9):
        line(im, [(x, y) for y in range(0, 6 + x % 4)], '#f4f2ea'); dot(im, x, 6 + x % 4, '#e2e0d8')
    return im


def tin_lid_line():
    """The full tin's lid: the rim, paint dried round it, and the word in marker across the middle."""
    im = metal('#b8bcc0', 52319)
    for y in range(16):
        for x in range(16):
            d = ((x - 7.5) ** 2 + (y - 7.5) ** 2) ** .5
            if 6.2 < d < 7.4:
                dot(im, x, y, '#dde1e4' if x + y < 15 else '#8e9296')
            elif 5.4 < d <= 6.2:
                dot(im, x, y, '#9a9ea2')
    for x, y in ((5, 8), (6, 7), (6, 8), (7, 7), (8, 8), (9, 7), (9, 8), (10, 7)):
        dot(im, x, y, '#1e1a16')
    for x in range(4, 12):
        dot(im, x, 4, '#f4f2ea'); dot(im, x, 11, '#e8e6de')
    return im


def tin_paint():
    """0.4.530: the open tin from above, standing on the grass beside June while she paints (PellNight). Its top is the square x and z
    4 to 10 of its cell: the rim round it, lit toward the upper left and dark round the lower right; the paint inside, wet, with the
    light on it; and paint wiped over the rim on one side where the brush is drawn across it."""
    im = metal('#b8bcc0', 52331)
    for y in range(4, 11):
        for x in range(4, 11):
            if x in (4, 10) or y in (4, 10):
                dot(im, x, y, '#dde1e4' if x + y < 14 else '#8e9296')
            else:
                dot(im, x, y, '#efede6')
    for x, y in ((6, 6), (7, 6), (6, 7)):
        dot(im, x, y, '#ffffff')
    for x, y in ((8, 8), (9, 8), (8, 9), (9, 9)):
        dot(im, x, y, '#dad7cd')
    for x in range(6, 9):
        dot(im, x, 4, '#f4f2ea')
    return im


def lantern_body():
    """0.4.530: June's battery lantern, its base and cap: moulded plastic, the top edge of each catching the light, and the base's switch
    on its front. The base's side is rows 14 and 15 of the picture as its model takes them, the cap's rows 6 and 7."""
    im = plastic('#35503f', 52332)
    for x in range(16):
        dot(im, x, 14, '#4a6a56'); dot(im, x, 15, '#2a3f32'); dot(im, x, 6, '#4a6a56'); dot(im, x, 7, '#2a3f32')
    dot(im, 7, 15, '#c8ccc4'); dot(im, 8, 15, '#9aa098')
    return im


def lantern_globe():
    """Its globe, frosted and lit: warm and pale, brightest down its middle, darker at its corners where the cage's ribs run, and
    shaded under the cap. Its sides are columns 5 to 10 and rows 8 to 13 of the picture as its model takes them."""
    im = new('#f4e4a8', 52333, 2)
    for y in range(16):
        for x in range(16):
            k = {5: -26, 6: -6, 7: 10, 8: 10, 9: -2, 10: -22}.get(x, 0)
            dot(im, x, y, shade('#f4e4a8', k + (-12 if y == 8 else -5 if y == 13 else 0)))
    return im


def transcript(section):
    """Pages taped up in a row on the shed's wall: the tapes written out by hand in pencil, June's corrections in red down the
    margins. Down the side of the last, one word written over and over."""
    im = clear()
    fill(im, 1, 1, 15, 15, '#f8f5ea')
    for y in range(3, 14, 2):
        line(im, [(x, y) for x in range(3, 13) if (x + y * section) % 5], '#8a8a8a')
    for y in range(2, 14, 3):
        dot(im, 2, y, '#c03a3a'); dot(im, 13, y + 1, '#c03a3a')
    fill(im, 6, 0, 10, 2, '#e0d6a8', 200)
    if section == 2:
        for y in range(2, 15):
            dot(im, 14, y, '#3a3a3a' if y % 3 else '#6a6a6a')
    return im


def transcript_gone():
    """0.4.548: where the last page was taped up before it was taken (PellMark): the strip of tape still on the wall with a torn edge of
    the page under it, and the square where the page hung a little cleaner than the wall round it."""
    im = clear()
    fill(im, 1, 1, 15, 15, '#ffffff', 18)
    fill(im, 6, 0, 10, 2, '#e0d6a8', 200)
    for x, y in ((6, 2), (7, 2), (8, 3), (9, 2)):
        dot(im, x, y, '#f8f5ea', 210)
    return im


def chemist_calendar(stage):
    """A free calendar from the chemist: the month's days in a grid, crossed off in pencil one by one, counting toward one day
    circled twice. Each visit the crosses have come on; on the last night the circle is today."""
    im = clear()
    fill(im, 2, 0, 14, 5, '#3a8a5a'); fill(im, 4, 1, 12, 4, '#f4f4f0'); dot(im, 7, 2, '#3a8a5a'); dot(im, 8, 2, '#c03a3a')
    fill(im, 2, 5, 14, 16, '#fbf8ee')
    days = [(x, y) for y in (7, 9, 11, 13, 15) for x in (3, 5, 7, 9, 11, 13)]
    target = days[22]
    crossed = (8, 15, 22)[stage]
    for i, (x, y) in enumerate(days):
        dot(im, x, y, '#bdb5a0')
        if i < crossed:
            dot(im, x, y, '#5a5a5a')
    tx, ty = target
    for dx, dy in ((-1, 0), (1, 0), (0, -1), (0, 1), (-1, -1), (1, 1)):
        dot(im, tx + dx, ty + dy, '#c03a3a')
    return im


# ------------------------------------------------------------------------------------------------ outside
def stake_tag(tilly):
    """A child-height stake and its plastic tag. The old ones say NESSA in a child's printing, a dead seedling at each. The newest
    has a fresh seedling and a tag in an adult's careful capitals: TILLY."""
    im = clear()
    fill(im, 4, 4, 12, 8, '#f4f4ec' if tilly else '#e4e0cc')
    if tilly:
        # Even, upright marks, all one height: careful capitals.
        for x in (5, 6, 8, 10, 11):
            dot(im, x, 5, '#1e1a16'); dot(im, x, 6, '#1e1a16')
        dot(im, 7, 5, '#1e1a16')
    else:
        # Big uneven marks in felt pen, run and faded by the weather.
        for x, y in ((5, 5), (5, 6), (6, 5), (7, 6), (8, 5), (8, 6), (9, 7), (10, 5), (11, 6), (10, 6)):
            dot(im, x, y, '#5a6aa8', 200)
    return im


def kennel_wall():
    """The kennel's boards, painted a long time ago; no smell of dog."""
    im = wood('#b88a5a', 52280)
    for y in range(1, 16, 4):
        line(im, [(x, y) for x in range(16)], '#8a6438')
        if y + 1 < 16:
            line(im, [(x, y + 1) for x in range(16)], '#c99c6c')
    rnd = random.Random(52281)
    for _ in range(6):
        x, y = rnd.randrange(16), rnd.randrange(16)
        if y % 4 != 1:
            dot(im, x, y, '#9a8a72')
    return im


def kennel_pair():
    """The kennel's front, two cells wide: its painted boards, the opening in the middle, over it the name board, BISCUIT, painted
    by a child, and beside the opening a heart in crayon. The body is fourteen high, so its front shows rows two to fifteen."""
    def draw(im):
        rnd = random.Random(52292)
        for y in range(16):
            for x in range(32):
                im.putpixel((x, y), shade('#8a6438' if y % 4 == 1 else '#c99c6c' if y % 4 == 2 else '#b88a5a', rnd.randint(-5, 5)))
        for y in range(KENNEL_DOOR_V, 16):
            for x in range(16 - KENNEL_DOOR_HALF, 16 + KENNEL_DOOR_HALF):
                im.putpixel((x, y), shade('#1a1410', 0))
        for y in range(2, 9):
            for x in range(1, 31):
                edge = y in (2, 8) or x in (1, 30)
                im.putpixel((x, y), shade('#6a4a2a' if edge else '#e8dcc4', 0 if edge else rnd.randint(-3, 3)))
        w = sum(len(GLYPHS[c][0]) for c in 'BISCUIT') + 6
        text32(im, (32 - w) // 2, 3, 'BISCUIT', '#2a3a6a')
        for x, y in ((23, 11), (25, 11), (22, 11), (26, 11), (22, 12), (23, 12), (24, 12), (25, 12), (26, 12), (23, 13), (24, 13), (25, 13), (24, 14)):
            if (x, y) not in ((22, 11), (26, 11)):
                im.putpixel((x, y), shade('#d0303a', 0))
    return wide(draw)


# The kennel's opening: half its width either side of the middle, and its top row on the front as drawn (seven high).
KENNEL_DOOR_HALF, KENNEL_DOOR_V = 5, 9


def kennel_floor():
    """The floor inside the kennel's opening, in its shadow: the line runs straight across it, the kennel set down on the paint and
    the paint gone on through it. A few dead leaves."""
    im = new('#1a1410', 52329, 2)
    rnd = random.Random(52330)
    for x in range(16):
        for y in (7, 8):
            im.putpixel((x, y), shade('#8c887e' if rnd.random() < .8 else '#6f6b62', rnd.randint(-4, 4)))
    for x, y in ((3, 4), (4, 4), (12, 11), (13, 12), (9, 13)):
        im.putpixel((x, y), shade('#5a4426', rnd.randint(-6, 6)))
    return im


def bowl_leaves():
    im = clear()
    for y in range(16):
        for x in range(16):
            d = ((x - 7.5) ** 2 + (y - 7.5) ** 2) ** .5
            if d < 5.5:
                dot(im, x, y, ('#e2e6e8' if x + y < 14 else '#c8ccd0' if x + y < 18 else '#9ea2a6') if d > 4.4 else ['#8a6a3a', '#a8783a', '#6a5a2a'][(x * 3 + y) % 3])
    return im


def pool_chair():
    """The pool chair's white plastic slats, and over its back a beach towel faded on one side only."""
    im = plastic('#f2f2ee', 52282)
    for y in range(1, 16, 3):
        line(im, [(x, y) for x in range(16)], '#c4c4bc')
        if y + 1 < 16:
            line(im, [(x, y + 1) for x in range(16)], '#fbfbf8')
    return im


def towel_faded():
    im = cloth('#3a8ac8', 52283)
    for y in range(16):
        for x in range(8, 16):
            r, g, b, _ = im.getpixel((x, y)); k = (x - 8) * 9
            im.putpixel((x, y), (min(255, r + k), min(255, g + k), min(255, b + k // 2), 255))
    for y in range(0, 16, 5):
        line(im, [(x, y) for x in range(16)], '#f2e6a8')
    return im


def robe():
    """0.4.533: June's towelling robe, left over the pool chair while she swims at dawn: the terry's loops in rows, worn thin at the
    folds, and the hem's band across the bottom."""
    im = cloth('#e6dfd2', 52285)
    for y in range(0, 16, 2):
        for x in range(y % 4 // 2, 16, 2):
            dot(im, x, y, shade(im.getpixel((x, y)), 8))
    for y in range(1, 16, 4):
        line(im, [(x, y) for x in range(16)], '#cfc6b4')
    fill(im, 0, 13, 16, 16, '#d8cfbc'); line(im, [(x, 13) for x in range(16)], '#bfb5a0')
    return im


def pump():
    """The pool pump's housing: grey plastic, its basket's clear lid, and the hum."""
    im = plastic('#8a9094', 52284); bevel(im, 0, 0, 16, 16, 12)
    fill(im, 3, 3, 13, 13, '#6a7074'); bevel(im, 3, 3, 13, 13, 12)
    fill(im, 5, 5, 11, 11, '#a8c8d0'); inset(im, 5, 5, 11, 11, 12); dot(im, 6, 6, '#e0f0f4'); dot(im, 7, 6, '#cce2e8')
    return im


def bathroom():
    return {'vanity_front': vanity_front(), 'basin_top': basin_top(), 'mirror_0': mirror(0), 'mirror_1': mirror(1),
            'bath_side_0': bath_side(0), 'bath_side_1': bath_side(1), 'bath_top_0': bath_top(0), 'bath_top_1': bath_top(1), 'toilet': toilet()}


def laundry():
    return {'freezer_lid_0': freezer_lid(0), 'freezer_lid_1': freezer_lid(1), 'freezer_front_0': freezer_front(0), 'freezer_front_0_open': freezer_front(0, True),
            'freezer_inside': freezer_inside(), 'freezer_plastic': freezer_plastic(), 'freezer_lid_under': freezer_lid_under(),
            'freezer_front_1': freezer_front(1), 'stool': stool(), 'power_point': power_point(), 'power_point_bare': power_point_bare(), 'cord': cord(), 'trough': trough(),
            'bucket_top': bucket_top(), 'chlorine': chlorine(), 'washer_front': washer_front()}


def garage():
    return {'canvas': canvas(), 'aluminium': aluminium(), 'box_plain': carton(''), 'peg_0': pegboard(0, 0), 'peg_1': pegboard(1, 0), 'peg_1_second': pegboard(1, 1), 'bulb': bulb(),
            'towel_rolled': towel_rolled()}


def shed():
    return {'tapes_0': tape_spines(0), 'tapes_1': tape_spines(1), 'bench_top': bench_top(), 'deck_front': deck_front(), 'portable_tv': portable_tv(), 'portable_tv_lit': portable_tv(True),
            'tin': tin(0), 'tin_lid_line': tin_lid_line(), 'tin_paint': tin_paint(), 'transcript_0': transcript(0), 'transcript_1': transcript(1), 'transcript_2': transcript(2), 'transcript_gone': transcript_gone(),
            'chemist_0': chemist_calendar(0), 'chemist_1': chemist_calendar(1), 'chemist_2': chemist_calendar(2)}


def for_sale(front):
    """0.4.542, the house's aftermath (PellAftermath): the agent's board on its post in the front lawn. The front, to the street: FOR
    SALE in red capitals on white, a red rule under them, and a telephone number in small black figures. The back, to the house: the
    same white plastic, plain, its fluting showing through, two screw heads where it is fixed to the post."""
    im = enamel('#f4f4f0', 52340, 2)
    if front:
        frame(im, 0, 0, 16, 16, '#c42a2a')
        text(im, 3, 2, 'FOR', '#c42a2a')
        text(im, 1, 8, 'SALE', '#c42a2a')
        line(im, [(x, 14) for x in range(2, 14) if x % 4 != 1], '#2a2a2a')
    else:
        for x in range(0, 16, 3):
            line(im, [(x, y) for y in range(16)], '#e6e6e0')
        frame(im, 0, 0, 16, 16, '#d8d8d2')
        dot(im, 8, 4, '#8a8e92'); dot(im, 8, 12, '#8a8e92')
    return im


def outside():
    return {'stake_nessa': stake_tag(0), 'stake_tilly': stake_tag(1), 'kennel_wall': kennel_wall(),
            'bowl_leaves': bowl_leaves(), 'pool_chair': pool_chair(), 'towel_faded': towel_faded(), 'robe': robe(), 'pump': pump(),
            'lantern_body': lantern_body(), 'lantern_globe': lantern_globe(), 'for_sale': for_sale(1), 'for_sale_back': for_sale(0)}


# ------------------------------------------------------------------------------------------------ the rest
def wide(draw):
    """A face two cells wide: draw on a 32 by 16 image, then cut it into its west and east halves."""
    im = Image.new('RGBA', (32, 16), (0, 0, 0, 0))
    draw(im)
    return im.crop((0, 0, 16, 16)), im.crop((16, 0, 32, 16))


def text32(im, x, y, s, colour, gap=1):
    for ch in s:
        g = GLYPHS[ch]
        for gy, row in enumerate(g):
            for gx, c in enumerate(row):
                if c == '#' and 0 <= x + gx < 32 and 0 <= y + gy < 16:
                    im.putpixel((x + gx, y + gy), shade(colour, 0))
        x += len(g[0]) + gap
    return x


def box_pair(label):
    """A long box, two cells, taped shut, and its label in marker across both: NESSA on the first line and below it WINTER, BOOKS
    or DON'T, the last smaller and gone over twice."""
    def draw(im):
        rnd = random.Random(52290 + len(label))
        for y in range(16):
            for x in range(32):
                im.putpixel((x, y), shade('#b8946a', rnd.randint(-4, 4)))
        # The box is twelve high: its front shows rows four to fifteen, and the label keeps inside them.
        for y in range(4, 16):
            im.putpixel((16, y), shade('#a8845a', 0)) if y % 3 == 0 else None
        w = sum(len(GLYPHS[c][0]) for c in 'NESSA') + 4
        text32(im, (32 - w) // 2, 5, 'NESSA', '#3a3430')
        # DON'T is gone over twice: blacker than the faded marker of the rest, its letters a pixel further apart and every down-stroke
        # doubled, so it stands heavier and still reads.
        gap = 2 if label == "DON'T" else 1
        w2 = sum(len(GLYPHS[c][0]) for c in label) + gap * (len(label) - 1)
        x2 = (32 - w2) // 2 + (1 if label == "DON'T" else 0)
        text32(im, x2, 11, label, '#100e0c' if label == "DON'T" else '#3a3430', gap)
        if label == "DON'T":
            x = x2
            for ch in label:
                for gy, row in enumerate(GLYPHS[ch]):
                    if row[0] == '#':
                        im.putpixel((x - 1, 11 + gy), shade('#100e0c', 0))
                x += len(GLYPHS[ch][0]) + gap
    return wide(draw)


def chore_pair():
    """The chore chart, two cells wide so its names can be read: CALLUM printed on the first row and two weeks of stars; on the
    second, a strip of paper stuck over the printed name with TILLY on it in marker, lifting at its corner, and under the corner
    the end of the printed name that was there before."""
    def draw(im):
        for y in range(1, 15):
            for x in range(1, 31):
                im.putpixel((x, y), shade('#f6f2e4', 0))
        for x in range(1, 31):
            im.putpixel((x, 1), shade('#c8bfa4', 0)); im.putpixel((x, 14), shade('#c8bfa4', 0))
        for y in range(1, 15):
            im.putpixel((1, y), shade('#c8bfa4', 0)); im.putpixel((30, y), shade('#c8bfa4', 0))
        for x in range(2, 30):
            im.putpixel((x, 8), shade('#d8cfb4', 0))
        text32(im, 2, 2, 'CALLUM', '#3a3a3a')
        # The printed name under the strip, set so its last letter ends where the strip does.
        text32(im, 4, 9, 'NESSA', '#3a3a3a')
        for x in (27, 29):
            for y in range(2, 14):
                if y != 8:
                    im.putpixel((x, y), shade('#ece4cc', 0))
        for x, y in ((27, 3), (29, 5), (27, 11)):
            im.putpixel((x, y), shade('#e0a030', 0))
        # The strip: plain paper over the printed name, TILLY on it in marker. Its top right corner has come unstuck and folded
        # down, so above the fold the end of the printed name shows, and below it is the back of the paper.
        peeled = {(21, 9), (22, 9), (23, 9), (22, 10), (23, 10), (23, 11)}
        flap = {(21, 10), (21, 11), (22, 11)}
        for y in range(9, 14):
            for x in range(2, 24):
                if (x, y) not in peeled:
                    im.putpixel((x, y), shade('#fffdf6', 0))
        text32(im, 2, 9, 'TILLY', '#1a1a1a')
        for x, y in flap:
            im.putpixel((x, y), shade('#e2dccb', 0))
        for x, y in ((20, 10), (20, 11), (21, 12), (22, 12)):
            im.putpixel((x, y), shade('#c8c0a8', 0))
    return wide(draw)


def circle_cells():
    """The sixteen cells of the circle painted on Callum's floor, three across and four deep about its centre."""
    out = {}
    cx, cz = CIRCLE_CENTRE
    for i in range(16):
        dx, dz = i % CIRCLE_W, i // CIRCLE_W
        out[f'circle_{i}'] = circle_cell(0, 0, dx - cx, dz - cz, CIRCLE_R) if dz < CIRCLE_D else clear()
    return out


def toys():
    """The toys in their ring, on one sheet: a bear, a car, a dinosaur, and blocks."""
    im = clear()
    fill(im, 0, 0, 4, 4, '#a8783a'); dot(im, 1, 1, '#2a1a10'); dot(im, 2, 1, '#2a1a10')
    fill(im, 4, 0, 9, 3, '#c03a3a'); dot(im, 5, 2, '#1e1e1e'); dot(im, 8, 2, '#1e1e1e')
    fill(im, 9, 0, 13, 6, '#4a9a4a'); dot(im, 10, 1, '#1e1e1e')
    fill(im, 0, 8, 3, 11, '#2a6ab8'); fill(im, 4, 8, 6, 10, '#e0b030')
    return im


def small():
    out = {'enamel': enamel('#f4f2ec', 52300), 'steel': metal('#b9bec1', 52301), 'grey_plastic': plastic('#7a7e80', 52302),
           'shelf_wood': wood('#9a8462', 52303), 'stake_wood': wood('#a8885a', 52304, across=True), 'paint_cream': enamel('#e8e0c8', 52305),
           'peg_edge': wood('#8a6a42', 52306), 'mattress_side': cloth('#d8d2c2', 52307, 1), 'brass': metal('#b08a3a', 52327), 'kennel_dark': new('#1a1410', 52328, 2), 'kennel_floor': kennel_floor(),
           'kennel_roof': wood('#6a4a3a', 52309), 'box_top': card('#c8a47a', 52310), 'card_back': new('#f4f1ea', 52311, 1),
           'chain': metal('#8a8e92', 52312, 3), 'cord_flex': plastic('#e8e6de', 52313), 'lens': new('#c8d8e0', 52314, 2),
           'cup': plastic('#e0d8a0', 52315), 'brush_old': new('#c8c4b4', 52316, 2), 'brush_new': plastic('#4a9ac8', 52317),
           'handle': plastic('#1e1a16', 52318), 'tin_lid': metal('#b8bcc0', 52319), 'brush': wood('#b8946a', 52320),
           'chlorine_lid': plastic('#2a6ab8', 52321), 'washer_lid': enamel('#ecebe4', 52322), 'doily': clear(),
           'seedling_dead': clear(), 'seedling_fresh': clear(), 'lunchbox_side_pink': plastic('#d64a6a', 52323),
           'bucket_side': plastic('#3a7ab8', 52324), 'canvas_head': canvas(True), 'mantel_top': wood('#7a5232', 52325)}
    d = out['doily']
    for y in range(16):
        for x in range(16):
            r = ((x - 7.5) ** 2 + (y - 7.5) ** 2) ** .5
            if r < 7.5 and (int(r * 2) % 2 == 0 or (x + y) % 3 == 0):
                dot(d, x, y, '#f8f6f0', 230)
    for name, alive in (('seedling_dead', 0), ('seedling_fresh', 1)):
        im = out[name]
        c = '#4aa04a' if alive else '#7a6a4a'
        line(im, [(8, y) for y in range(8 if alive else 10, 16)], c)
        line(im, [(6, 9), (7, 10), (9, 9), (10, 8)] if alive else [(6, 13), (7, 12), (9, 14)], c)
    wd = washer_front(); fill(wd, 0, 4, 16, 16, '#dcd8cc'); out['washer_dials'] = wd
    up = cupboard(); grain = wood('#9a6a40', 52203, across=True)
    for y in range(13, 16):
        for x in range(16):
            up.putpixel((x, y), grain.getpixel((x, y)))
    out['cupboard_upper'] = up
    ci = enamel('#efe6d2', 52326); line(ci, [(x, 8) for x in range(16)], '#c8bfa4'); line(ci, [(x, 7) for x in range(16)], '#f8f2e2')
    line(ci, [(x, 9) for x in range(16)], '#d8cfb8'); out['cupboard_inside'] = ci
    return out


def rest():
    out = small()
    out.update(circle_cells())
    out['toys'] = toys()
    for name, label in (('box_winter', 'WINTER'), ('box_books', 'BOOKS'), ('box_dont', "DON'T")):
        a, b = box_pair(label); out[f'{name}_0'] = a; out[f'{name}_1'] = b
    a, b = chore_pair(); out['chore_chart_0'] = a; out['chore_chart_1'] = b
    a, b = kennel_pair(); out['kennel_front_0'] = a; out['kennel_front_1'] = b
    return out



# ------------------------------------------------------------------------------------------------ models
# The circle on Callum's floor: three cells across and four deep, its centre between them, its radius in blocks.
CIRCLE_W, CIRCLE_D, CIRCLE_R = 3, 4, 1.4
CIRCLE_CENTRE = (1.5, 2.0)
# Where the toys stand on its line, each facing out: the cell, and the toy.
TOYS = {(1, 0): 'bear', (2, 1): 'car', (1, 3): 'dino', (0, 2): 'blocks'}


def m(elements, textures, particle):
    return {'ambientocclusion': False, 'render_type': 'minecraft:cutout',
            'textures': {**{k: tex(v) for k, v in textures.items()}, 'particle': tex(particle)},
            'elements': elements}


def tex(name):
    """A texture: the thing's own (under block/pell/thing/), or one of the house's materials named in full."""
    return name if name.startswith(NS) else NS + 'block/pell/thing/' + name


CARPET = NS + 'block/pell_carpet'


def box(lo, hi, tex, **faces):
    """A box with one texture on every face, any face overridden by name."""
    t = {k: tex for k in ('north', 'south', 'east', 'west', 'up', 'down')}
    t.update(faces)
    return element(lo, hi, t)


def counter_body(front='f', top='t', side='s'):
    """A kitchen bench: its cupboard body set back a pixel, its laminate top over it with the chrome edge at the front."""
    return [element([0, 0, 1], [16, 14, 16], {'north': front, 'east': side, 'west': side, 'south': side, 'down': side}),
            element([0, 14, 0], [16, 16, 16], {'up': top, 'north': 'e', 'east': 'e', 'west': 'e', 'south': 'e', 'down': 'e'},
                    uv={'north': [0, 14, 16, 16], 'east': [0, 14, 16, 16], 'west': [0, 14, 16, 16], 'south': [0, 14, 16, 16]})]


KITCHEN_TEX = {'f': 'cupboard', 's': 'cupboard_side', 't': 'laminate_top', 'e': 'chrome'}


def thing_models():
    out = {}
    # ---------------------------------------------------------------------------------------- the kitchen
    out['counter_0'] = m(counter_body(), KITCHEN_TEX, 'laminate_top')
    out['sink_0'] = m(counter_body(top='k') + [box([7, 16, 14], [9, 21, 15], 'e'), box([7, 20, 11], [9, 21, 15], 'e')], {**KITCHEN_TEX, 'k': 'sink_top'}, 'sink_top')
    out['stove_0'] = m([element([0, 0, 0], [16, 14, 16], {'north': 'f', 'east': 's', 'west': 's', 'south': 's', 'down': 's', 'up': 't'}),
                        box([0, 14, 14], [16, 18, 16], 's', north='s')], {'f': 'stove_front', 's': 'enamel', 't': 'stove_top'}, 'stove_front')
    out['fridge_0'] = m([element([0, 0, 1], [16, 16, 16], {'north': 'f', 'east': 's', 'west': 's', 'south': 's', 'down': 's', 'up': 's'})],
                        {'f': 'fridge_low', 's': 'fridge_side'}, 'fridge_low')
    for stage in range(2):
        out[f'fridge_top_{stage}'] = m([element([0, 0, 1], [16, 16, 16], {'north': 'f', 'east': 's', 'west': 's', 'south': 's', 'down': 's', 'up': 's'})],
                                       {'f': f'fridge_up_{stage}', 's': 'fridge_side'}, f'fridge_up_{stage}')
    # The knife block on the bench: its body leaning back, the handles in their slots; one gone; the block gone.
    for stage in range(3):
        els = counter_body(top='t' if stage < 2 else 'g')
        if stage < 2:
            els.append(element([5, 16, 7], [10, 22, 11], {k: 'w' for k in ('north', 'south', 'east', 'west', 'up')}, rot={'origin': [7.5, 16, 9], 'axis': 'x', 'angle': -22.5}))
            for i, x in enumerate((5.5, 6.75, 8, 9.25)):
                if stage == 1 and i == 2:
                    continue
                els.append(element([x, 21, 8], [x + 1, 25, 10], {k: 'h' for k in ('north', 'south', 'east', 'west', 'up')}, rot={'origin': [7.5, 16, 9], 'axis': 'x', 'angle': -22.5}))
        out[f'knife_block_{stage}'] = m(els, {**KITCHEN_TEX, 'w': 'wood_block', 'h': 'handle', 'g': 'knife_2'}, 'wood_block')
    out['lunchboxes_0'] = m(counter_body() + [element([2, 16, 4], [7, 19.5, 12], {'north': 'b', 'south': 'b', 'east': 'b', 'west': 'b', 'up': 'l'}, uv={'up': [2, 4, 7, 12]}),
                                              element([9, 16, 4], [14, 19.5, 12], {'north': 'p', 'south': 'p', 'east': 'p', 'west': 'p', 'up': 'l'}, uv={'up': [9, 4, 14, 12]})],
                            {**KITCHEN_TEX, 'b': 'lunchbox_side', 'p': 'lunchbox_side_pink', 'l': 'lunchboxes'}, 'lunchboxes')
    # The cupboard on the wall above the bench: shut; open, and the knife block put away inside it.
    out['cupboard_upper_0'] = m([element([0, 0, 7], [16, 16, 16], {'north': 'u', 'east': 's', 'west': 's', 'south': 's', 'down': 's', 'up': 's'})],
                                {'u': 'cupboard_upper', 's': 'cupboard_side'}, 'cupboard_upper')
    out['cupboard_upper_1'] = m([element([0, 0, 8], [16, 16, 16], {'north': 'i', 'east': 's', 'west': 's', 'south': 's', 'down': 's', 'up': 's'}),
                                 element([0, 0, 0], [1, 16, 8], {'east': 'd', 'west': 'd', 'north': 's', 'south': 's'}, uv={'east': [0, 0, 8, 16], 'west': [8, 0, 16, 16]}),
                                 element([6, 1, 10], [11, 7, 14], {k: 'w' for k in ('north', 'south', 'east', 'west', 'up')})],
                                {'i': 'cupboard_inside', 'd': 'cupboard_upper', 's': 'cupboard_side', 'w': 'wood_block'}, 'cupboard_upper')
    # The table: two cells, a laminate top edged in chrome, a leg at each outer corner.
    for half in range(2):
        legs = [(14, 1), (14, 14)] if half == 0 else [(1, 1), (1, 14)]
        els = [element([0, 13, 1], [16, 15, 15], {'up': 't', 'down': 'c', 'north': 'c', 'south': 'c', 'east': 'c', 'west': 'c'}, uv={'up': [0, 1, 16, 15]})]
        els += [box([x, 0, z], [x + 1, 13, z + 1], 'c') for x, z in legs]
        out[f'table_{half}'] = m(els, {'t': 'table_top', 'c': 'chrome'}, 'table_top')
    out['chair_0'] = m([box([3, 8, 3], [13, 10, 13], 'v'), element([3, 10, 12], [13, 21, 13.5], {k: 'v' for k in ('north', 'south', 'east', 'west', 'up')}),
                        *[box([x, 0, z], [x + 1, 8, z + 1], 'c') for x in (3, 12) for z in (3, 12)],
                        *[box([x, 10, 12.5], [x + 1, 13, 13], 'c') for x in (3, 12)]], {'v': 'vinyl_red', 'c': 'chrome'}, 'vinyl_red')
    for half in range(2):
        out[f'chore_chart_{half}'] = m([element([0, 0, 15.6], [16, 16, 16], {'north': 'c'})], {'c': f'chore_chart_{half}'}, f'chore_chart_{half}')
    out['calendar_0'] = m([element([2, 0, 15.6], [14, 16, 16], {'north': 'c'}, uv={'north': [2, 0, 14, 16]})], {'c': 'calendar_kitchen'}, 'calendar_kitchen')
    # ---------------------------------------------------------------------------------------- the living room
    out['recliner_0'] = m([element([1, 0, 2], [15, 6, 15], {'north': 'v', 'south': 'v', 'east': 'v', 'west': 'v', 'down': 'v', 'up': 'v'}),
                           element([3, 6, 1], [13, 9, 13], {'north': 'w', 'up': 'w', 'east': 'v', 'west': 'v', 'south': 'v'}),
                           element([1, 6, 2], [3, 12, 15], {'north': 'v', 'south': 'v', 'east': 'v', 'west': 'v', 'up': 'v'}),
                           element([13, 6, 2], [15, 12, 15], {'north': 'w', 'south': 'v', 'east': 'v', 'west': 'v', 'up': 'w'}),
                           element([2, 9, 12], [14, 24, 16], {'north': 'v', 'south': 'v', 'east': 'v', 'west': 'v', 'up': 'v'}, rot={'origin': [8, 9, 14], 'axis': 'x', 'angle': 22.5})],
                          {'v': 'recliner', 'w': 'recliner_worn'}, 'recliner')
    for lit in range(2):
        out[f'tv_cabinet_{lit}'] = m([element([0, 0, 2], [16, 10, 16], {'north': 'c', 'south': 'd', 'east': 'd', 'west': 'd', 'up': 'd', 'down': 'd'}, uv={'north': [0, 3, 16, 13]}),
                             element([2, 10, 4], [14, 22, 15], {'north': 't', 'south': 'd', 'east': 'd', 'west': 'd', 'up': 'd'}, uv={'north': [0, 0, 16, 16]}),
                             element([4, 22, 8], [12, 22.2, 12], {'up': 'o'})],
                            {'c': 'tape_cabinet', 'd': 'wood_dark', 't': 'tv_lit' if lit else 'tv_front', 'o': 'doily'}, 'wood_dark')
    out['mantel_0'] = m([element([0, 0, 10], [16, 14, 16], {'north': 'b', 'east': 'b', 'west': 'b', 'south': 'b', 'up': 'b'}),
                         element([-1, 14, 8], [17, 16, 16], {'up': 'k', 'north': 'w', 'east': 'w', 'west': 'w', 'south': 'w', 'down': 'w'}),
                         element([2, 16, 10], [5, 16.3, 14], {'up': 'x'}), element([7, 16, 9], [11, 16.3, 14], {'up': 'x'}), element([12, 16, 11], [15, 16.3, 14], {'up': 'x'})],
                        {'b': 'mantel_front', 'k': 'mantel_top', 'w': 'wood_dark', 'x': 'card_back'}, 'mantel_front')
    # Beside the chair: the side table, the remote, the cold mug, the empty case; and the standard lamp, its shade tipped toward the
    # front (the chair), off and on.
    out['side_table_0'] = m([element([3, 10, 3], [13, 11, 13], {k: 'd' for k in ('north', 'south', 'east', 'west', 'up', 'down')}),
                             *[box([x, 0, z], [x + 1, 10, z + 1], 'd') for x in (3.5, 11.5) for z in (3.5, 11.5)],
                             box([4, 3, 4], [12, 4, 12], 'd'),
                             element([4.5, 11, 4.5], [6.5, 11.7, 9], {k: 'r' for k in ('north', 'south', 'east', 'west', 'up')}),
                             element([9, 11, 4.5], [12, 14.5, 7.5], {'north': 'e', 'south': 'e', 'east': 'e', 'west': 'e', 'up': 'g'}),
                             box([12, 12, 5.5], [12.8, 13.8, 6.5], 'e'),
                             element([5, 11, 9.5], [10, 11.6, 12.5], {'north': 'c', 'south': 'c', 'east': 'c', 'west': 'c', 'up': 'c'})],
                            {'d': 'wood_dark', 'r': 'remote', 'e': 'enamel', 'g': 'mug_top', 'c': 'tape_case'}, 'wood_dark')
    for lit in range(2):
        # 0.4.566 (owner: "lamp texture is malformed"): the shade was one box tipped at an angle on the pole, its sides mapped from
        # past the top of their picture, so they showed whatever lay beyond it. Now a drum shade flared at its foot, straight on the
        # pole: three bands, each its own strip of the pleated cloth (the hem at the foot), closed over the top.
        sides = ('north', 'south', 'east', 'west')
        shade_els = []
        for y0, y1, a, b, rows in ((17, 20, 3.5, 12.5, (10, 16)), (20, 22.5, 4, 12, (5, 10)), (22.5, 24.5, 4.5, 11.5, (0, 5))):
            faces = {k: 's' for k in sides}
            uv = {k: [0, rows[0], 16, rows[1]] for k in sides}
            if y0 == 17:
                faces['down'] = 's'; uv['down'] = [a, a, b, b]
            if y1 == 24.5:
                faces['up'] = 's'; uv['up'] = [a, a, b, b]
            el = element([a, y0, a], [b, y1, b], faces, uv=uv)
            if lit:
                el['shade'] = False
                el['neoforge_data'] = {'block_light': 15, 'sky_light': 15}
            shade_els.append(el)
        out[f'floor_lamp_{lit}'] = m([box([5, 0, 5], [11, 1, 11], 'b'), box([6.5, 1, 6.5], [9.5, 1.5, 9.5], 'b'), box([7.5, 1.5, 7.5], [8.5, 17, 8.5], 'b'), *shade_els],
                                     {'b': 'brass', 's': 'lamp_shade_lit' if lit else 'lamp_shade'}, 'lamp_shade')
    out['window_seat_0'] = m([element([0, 0, 2], [16, 6, 16], {'north': 'w', 'south': 'w', 'east': 'w', 'west': 'w', 'down': 'w'}),
                              element([0, 6, 2], [16, 9, 16], {'up': 'c', 'north': 'c', 'south': 'c', 'east': 'c', 'west': 'c'})],
                             {'w': 'wood_dark', 'c': 'seat_cushion'}, 'seat_cushion')
    # ---------------------------------------------------------------------------------------- the hall
    for pair in range(4):
        out[f'photos_{pair}'] = m([element([0, 0, 15.5], [16, 16, 16], {'north': 'p'})], {'p': f'photos_{pair}'}, f'photos_{pair}')
    # 0.4.548: the last of them, its glass broken (PellMark).
    out['photo_broken_0'] = m([element([0, 0, 15.5], [16, 16, 16], {'north': 'p'})], {'p': 'photos_broken'}, 'photos_broken')
    # The hooks stand out from the wall where the pencil crosses are: a nail and the hook's lip turned up.
    hook_els = []
    for u in HOOK_U:
        x, y = 16 - u - 1, 16 - HOOK_V - 1
        hook_els += [box([x, y, 14.8], [x + 1, y + 1, 15.8], 'b'), box([x, y + 1, 14.8], [x + 1, y + 1.6, 15.2], 'b')]
    out['hooks_0'] = m([element([0, 0, 15.8], [16, 16, 16], {'north': 'h'})] + hook_els, {'h': 'hooks', 'b': 'brass'}, 'hooks')
    for stage in range(3):
        out[f'coat_hook_{stage}'] = m([element([0, 0, 14.5], [16, 16, 16], {'north': 'h'})], {'h': f'coat_{stage}'}, f'coat_{stage}')
    # ---------------------------------------------------------------------------------------- Nessa's room
    for end in range(2):
        els = [element([0, 0, 0], [16, 6, 16], {'north': 'f', 'south': 'f', 'east': 'f', 'west': 'f', 'down': 'f', 'up': 'f'}),
               element([0, 6, 0], [16, 9, 16], {'up': 'q', 'north': 'q', 'south': 'q', 'east': 'q', 'west': 'q'})]
        if end == 0:
            # The head: a pillow, and the headboard against the wall.
            els += [box([2, 9, 9], [14, 11, 15], 'p'), box([0, 6, 15], [16, 18, 16], 'f')]
        out[f'bed_{end}'] = m(els, {'f': 'bed_frame', 'q': 'quilt_head' if end == 0 else 'quilt_foot', 'p': 'pillow'}, 'quilt_head')
    out['drawers_0'] = m([element([1, 0, 3], [15, 14, 16], {'north': 'f', 'south': 'v', 'east': 'v', 'west': 'v', 'up': 'v', 'down': 'v'}),
                          element([9, 14, 6], [14, 18, 10], {'north': 'k', 'south': 'k', 'east': 'k', 'west': 'k', 'up': 'k'})],
                         {'f': 'drawers_front', 'v': 'veneer', 'k': 'talking_clock'}, 'drawers_front')
    out['desk_0'] = m([element([0, 12, 2], [16, 14, 16], {'up': 't', 'north': 'v', 'south': 'v', 'east': 'v', 'west': 'v', 'down': 'v'}),
                       *[box([x, 0, z], [x + 2, 12, z + 2], 'v') for x in (0, 14) for z in (2, 14)],
                       box([12, 14, 9], [13, 19, 10], 'c'), element([10, 18, 7], [15, 18.3, 12], {'up': 'g', 'down': 'g'})],
                      {'t': 'desk_top', 'v': 'veneer', 'c': 'chrome', 'g': 'lens'}, 'desk_top')
    out['bookshelf_0'] = m([element([0, 0, 8], [16, 16, 16], {'north': 'b', 'south': 'v', 'east': 'v', 'west': 'v', 'up': 'v', 'down': 'v'})],
                           {'b': 'large_books', 'v': 'veneer'}, 'large_books')
    for stage in range(4):
        upper, opened = stage & 1, stage >> 1
        face = ('wardrobe_hi' if upper else 'wardrobe_lo') + ('_open' if opened else '')
        els = [element([0, 0, 2], [16, 16, 16], {'north': 'f', 'south': 'v', 'east': 'v', 'west': 'v', 'up': 'v', 'down': 'v'})]
        if opened:
            els.append(element([0, 0, -6], [1, 16, 2], {'east': 'v', 'west': 'v', 'north': 'v', 'south': 'v'}))
        out[f'wardrobe_{stage}'] = m(els, {'f': face, 'v': 'veneer'}, face)
    out['switch_0'] = m([element([5, 3, 15.6], [11, 13, 16], {'north': 's'}, uv={'north': [5, 3, 11, 13]})], {'s': 'switch'}, 'switch')
    for stage in range(4):
        upper, tilly = stage & 1, stage >> 1
        face = 'marks_hi_tilly' if (upper and tilly) else 'marks_hi' if upper else 'marks_lo'
        # 0.4.542: wide enough for the N beside the last mark, which a narrower one cut to its first stroke. A north face shows its
        # picture from the far end of the block, so the frame stays where it was and the N lies beyond it, toward the door.
        out[f'marks_{stage}'] = m([element([1, 0, 15.7], [11, 16, 16], {'north': 'k'}, uv={'north': [5, 0, 15, 16]})], {'k': face}, face)
    # 0.4.542, the house's aftermath (PellAftermath): the upper cell with the N rubbed out (0), or with one more, later (1).
    for stage, face in enumerate(('marks_hi_rubbed', 'marks_hi_later')):
        out[f'marks_after_{stage}'] = m([element([1, 0, 15.7], [11, 16, 16], {'north': 'k'}, uv={'north': [5, 0, 15, 16]})], {'k': face}, face)
    for crooked in range(2):
        face = 'border_crooked' if crooked else 'border'
        out[f'border_{crooked}'] = m([element([0, 8, 15.8], [16, 16, 16], {'north': 'b'}, uv={'north': [0, 8, 16, 16]})], {'b': face}, face)
    # ---------------------------------------------------------------------------------------- Callum's room
    for end in range(2):
        out[f'mattress_{end}'] = m([element([0, 0, 0], [16, 4, 16], {'up': 'm', 'north': 's', 'south': 's', 'east': 's', 'west': 's', 'down': 's'})],
                                   {'m': 'mattress_head' if end == 0 else 'mattress_foot', 's': 'mattress_side'}, 'mattress_side')
    # The circle is painted on the floor: each cell is the floor's own carpet with the paint on top, and a toy standing on the line
    # where there is one (in the cell above, where nothing stops a body).
    for i in range(16):
        dx, dz = i % CIRCLE_W, i // CIRCLE_W
        els = [element([0, 0, 0], [16, 16, 16], {k: 'k' for k in ('north', 'south', 'east', 'west', 'up', 'down')}),
               element([0, 16.05, 0], [16, 16.1, 16], {'up': 'c'})]
        toy = TOYS.get((dx, dz))
        if toy:
            els += toy_elements(toy, dx, dz)
        out[f'circle_{i}'] = m(els, {'k': CARPET, 'c': f'circle_{i}', 'y': 'toys'}, f'circle_{i}')
    shoe_els = []
    for x, c in SHOES:
        side, top = c, c + 't'
        shoe_els += [element([x, 0, 9], [x + 2, 2, 15], {'north': side, 'south': side, 'east': side, 'west': side, 'up': top}),
                     element([x, 2, 12], [x + 2, 3.5, 15], {'north': top, 'south': side, 'east': side, 'west': side, 'up': side})]
    out['shoes_0'] = m(shoe_els, {'b': 'shoe_blue', 'bt': 'shoe_blue_top', 'r': 'shoe_red', 'rt': 'shoe_red_top'}, 'shoe_blue')
    for knives in range(3):
        out[f'cutlery_{knives}'] = m([element([1, 0, 3], [15, 10, 16], {'north': 'f', 'south': 'v', 'east': 'v', 'west': 'v', 'up': 'v', 'down': 'v'}),
                                      element([2, 10, -1], [14, 13, 14], {'up': 'k', 'north': 'f', 'south': 'v', 'east': 'v', 'west': 'v', 'down': 'v'}, uv={'north': [2, 7, 14, 10]}),
                                      element([1, 13, 3], [15, 15, 16], {'up': 'v', 'north': 'v', 'south': 'v', 'east': 'v', 'west': 'v'})],
                                     {'f': 'cutlery_front', 'v': 'veneer', 'k': f'cutlery_{knives}'}, f'cutlery_{knives}')
    # ---------------------------------------------------------------------------------------- the bathroom
    out['vanity_0'] = m([element([0, 0, 4], [16, 13, 16], {'north': 'f', 'south': 'w', 'east': 'w', 'west': 'w', 'down': 'w'}),
                         element([0, 13, 3], [16, 15, 16], {'up': 'b', 'north': 'w', 'south': 'w', 'east': 'w', 'west': 'w', 'down': 'w'}),
                         box([10, 15, 10], [13, 18, 13], 'g'), box([10.5, 18, 11], [11, 20, 11.5], 'r'), box([12, 18, 11.5], [12.5, 20, 12], 'n'),
                         box([6, 15, 12.5], [7, 17.5, 13.5], 'c'), box([6, 16.5, 10.5], [7, 17.5, 12.5], 'c'),
                         box([4, 15, 12.5], [5, 16, 13.5], 'c'), box([8, 15, 12.5], [9, 16, 13.5], 'c')],
                        {'f': 'vanity_front', 'w': 'enamel', 'b': 'basin_top', 'g': 'cup', 'r': 'brush_old', 'n': 'brush_new', 'c': 'chrome'}, 'vanity_front')
    for steamed in range(2):
        out[f'mirror_{steamed}'] = m([element([1, 0, 14], [15, 14, 16], {'north': 'g', 'east': 'c', 'west': 'c', 'up': 'c', 'down': 'c'}, uv={'north': [1, 1, 15, 15]})],
                                     {'g': f'mirror_{steamed}', 'c': 'chrome'}, f'mirror_{steamed}')
    for end in range(2):
        # End to end: the tap end's outer side (the south as drawn) and the far end's (the north) stand a pixel in; they meet flush.
        z0, z1 = (0, 15) if end == 0 else (1, 16)
        els = [element([0, 0, z0], [16, 9, z1], {'north': 's', 'east': 's', 'west': 's', 'south': 's', 'down': 's'}),
               element([0, 9, z0], [16, 9.2, z1], {'up': 't'}, uv={'up': [0, z0, 16, z1]})]
        if end == 0:
            els += [box([7, 9, 14], [9, 12, 16], 'c'), box([7.5, 12, 12], [8.5, 13, 16], 'c')]
        out[f'bath_{end}'] = m(els, {'s': f'bath_side_{end}', 't': f'bath_top_{end}', 'c': 'chrome'}, f'bath_side_{end}')
    out['toilet_0'] = m([box([4, 0, 3], [12, 6, 12], 'e'), element([3, 6, 2], [13, 8, 12], {'up': 't', 'north': 'e', 'south': 'e', 'east': 'e', 'west': 'e', 'down': 'e'}),
                         box([3, 6, 12], [13, 15, 16], 'e')], {'e': 'enamel', 't': 'toilet'}, 'toilet')
    # ---------------------------------------------------------------------------------------- the laundry
    for stage in range(4):
        end, unlocked = stage & 1, stage >> 1
        front = f'freezer_front_{end}' + ('_open' if unlocked and end == 0 else '')
        out[f'freezer_{stage}'] = m([element([0, 0, 1], [16, 14, 16], {'north': 'f', 'south': 's', 'east': 's', 'west': 's', 'down': 's', 'up': 'l'})],
                                    {'f': front, 's': 'enamel', 'l': f'freezer_lid_{end}'}, front)
    # 0.4.540: the freezer with its lid up (PellFreezer): the chest hollow (its front, back, end and floor), the frosted plastic
    # over what lies in it, and the lid stood up against the wall behind it, its underside out. Half 0 is the lock end.
    for end in range(2):
        x0, x1 = (1.5, 16) if end == 0 else (0, 14.5)
        front = 'freezer_front_0_open' if end == 0 else 'freezer_front_1'
        els = [element([0, 0, 1], [16, 14, 2.5], {'north': 'f', 'south': 'i', 'east': 's', 'west': 's', 'up': 's', 'down': 's'}),
               element([0, 0, 14.5], [16, 14, 16], {'north': 'i', 'south': 's', 'east': 's', 'west': 's', 'up': 's', 'down': 's'}),
               element([x0, 0, 2.5], [x1, 3, 14.5], {'up': 'i', 'down': 's'}),
               element([x0, 8, 2.5], [x1, 8, 14.5], {'up': 'p', 'down': 'p'}),
               element([0, 14, 14], [16, 30, 16], {'north': 'u', 'south': 's', 'east': 's', 'west': 's', 'up': 's'},
                       uv={'north': [0, 0, 16, 16], 'south': [0, 0, 16, 16], 'east': [0, 0, 2, 16], 'west': [14, 0, 16, 16], 'up': [0, 14, 16, 16]})]
        if end == 0:
            els.append(element([0, 0, 2.5], [1.5, 14, 14.5], {'west': 's', 'east': 'i', 'up': 's'}))
        else:
            els.append(element([14.5, 0, 2.5], [16, 14, 14.5], {'east': 's', 'west': 'i', 'up': 's'}))
        out[f'freezer_open_{end}'] = m(els, {'f': front, 'i': 'freezer_inside', 's': 'enamel', 'p': 'freezer_plastic', 'u': 'freezer_lid_under'}, front)
    out['stool_0'] = m([box([3, 8, 3], [13, 9, 13], 't', up='t'), box([3, 4, 1], [13, 5, 5], 'p'),
                        *[box([x, 0, z], [x + 1, 8, z + 1], 'p') for x in (3, 12) for z in (3, 12)]], {'t': 'stool', 'p': 'paint_cream'}, 'stool')
    out['power_0'] = m([element([0, 0, 15.6], [16, 16, 16], {'north': 'p'}), element([7, 0.05, 3], [9, 0.3, 15.6], {'up': 'c'}, uv={'up': [7, 0, 9, 12]})],
                       {'p': 'power_point', 'c': 'cord'}, 'power_point')
    # 0.4.548: the note taken off it (PellMark).
    out['power_1'] = m([element([0, 0, 15.6], [16, 16, 16], {'north': 'p'}), element([7, 0.05, 3], [9, 0.3, 15.6], {'up': 'c'}, uv={'up': [7, 0, 9, 12]})],
                       {'p': 'power_point_bare', 'c': 'cord'}, 'power_point_bare')
    out['cord_0'] = m([element([0, 0.05, 0], [16, 0.3, 16], {'up': 'c'})], {'c': 'cord'}, 'cord')
    out['trough_0'] = m([element([1, 9, 2], [15, 15, 15], {'up': 't', 'north': 's', 'south': 's', 'east': 's', 'west': 's', 'down': 's'}),
                         *[box([x, 0, z], [x + 1, 9, z + 1], 's') for x in (1, 14) for z in (2, 14)],
                         box([7, 15, 14], [9, 20, 15], 's'), box([7, 19, 11], [9, 20, 15], 's')], {'t': 'trough', 's': 'steel'}, 'trough')
    out['bucket_0'] = m([element([3, 0, 3], [13, 9, 13], {'up': 'b', 'north': 'w', 'south': 'w', 'east': 'w', 'west': 'w', 'down': 'w'})], {'b': 'bucket_top', 'w': 'bucket_side'}, 'bucket_side')
    out['chlorine_0'] = m([element([2, 0, 2], [14, 14, 14], {'north': 'c', 'south': 'c', 'east': 'c', 'west': 'c', 'up': 'k', 'down': 'k'})], {'c': 'chlorine', 'k': 'chlorine_lid'}, 'chlorine')
    out['washer_0'] = m([element([1, 0, 1], [15, 15, 15], {'north': 'f', 'south': 'e', 'east': 'e', 'west': 'e', 'up': 'l', 'down': 'e'}),
                         box([1, 15, 12], [15, 18, 15], 'e', north='d')], {'f': 'washer_front', 'e': 'enamel', 'l': 'washer_lid', 'd': 'washer_dials'}, 'washer_front')
    # ---------------------------------------------------------------------------------------- the garage
    for end in range(2):
        els = [element([1, 6, 0], [15, 7, 16], {'up': 'c', 'down': 'c', 'north': 'a', 'south': 'a', 'east': 'a', 'west': 'a'}),
               box([1, 6, 0], [2, 7.5, 16], 'a'), box([14, 6, 0], [15, 7.5, 16], 'a'),
               element([1, 0, 14 if end == 0 else 1], [15, 6, 15 if end == 0 else 2], {'north': 'a', 'south': 'a', 'east': 'a', 'west': 'a'})]
        out[f'camp_bed_{end}'] = m(els, {'c': 'canvas' if end else 'canvas_head', 'a': 'aluminium'}, 'canvas')
    out['bulb_0'] = m([box([7.5, 4, 7.5], [8.5, 16, 8.5], 'w'), box([6.5, 1, 6.5], [9.5, 4.5, 9.5], 'b'), box([10, 3, 7.8], [10.2, 16, 8.2], 'w')], {'w': 'cord_flex', 'b': 'bulb'}, 'bulb')
    for name in ('box_winter', 'box_books', 'box_dont'):
        for half in range(2):
            out[f'{name}_{half}'] = m([element([1 if half else 0, 0, 2], [16 if half else 15, 12, 15], {'north': 'f', 'south': 'p', 'east': 'p', 'west': 'p', 'up': 't', 'down': 'p'})],
                                      {'f': f'{name}_{half}', 'p': 'box_plain', 't': 'box_top'}, f'{name}_{half}')
    for stage in range(4):
        half, second = stage & 1, stage >> 1
        face = f'peg_{half}' + ('_second' if second and half else '')
        out[f'pegboard_{stage}'] = m([element([0, 0, 15], [16, 16, 16], {'north': 'p', 'east': 'e', 'west': 'e', 'up': 'e', 'down': 'e'})], {'p': face, 'e': 'peg_edge'}, face)
    out['towel_0'] = m([element([0, 0, 13], [16, 2.5, 16], {'up': 't', 'north': 't', 'south': 't', 'east': 't', 'west': 't'})], {'t': 'towel_rolled'}, 'towel_rolled')
    # ---------------------------------------------------------------------------------------- the shed
    for upper in range(2):
        out[f'tapes_{upper}'] = m([element([0, 0, 6], [16, 16, 16], {'north': 'f', 'south': 'w', 'east': 'w', 'west': 'w', 'up': 'w', 'down': 'w'})],
                                  {'f': f'tapes_{upper}', 'w': 'shelf_wood'}, f'tapes_{upper}')
    for tins in range(2):
        els = [element([0, 13, 1], [16, 15, 16], {'up': 't', 'north': 'w', 'south': 'w', 'east': 'w', 'west': 'w', 'down': 'w'}),
               *[box([x, 0, z], [x + 2, 13, z + 2], 'w') for x in (0, 14) for z in (1, 14)], box([0, 3, 2], [16, 4, 15], 'w')]
        if tins:
            els += [element([2 + i * 4, 4, 7], [5 + i * 4, 8, 10], {'north': 'n', 'south': 'n', 'east': 'n', 'west': 'n', 'up': 'k'}) for i in range(3)]
        out[f'workbench_{tins}'] = m(els, {'t': 'bench_top', 'w': 'shelf_wood', 'n': 'tin', 'k': 'tin_lid'}, 'bench_top')
    # 0.4.536: 1 while a tape plays (PellTapes): the screen lit and the deck's light on.
    for lit in range(2):
        out[f'shed_tv_{lit}'] = m([element([1, -1, 4], [15, 2, 14], {'north': 'd', 'south': 'g', 'east': 'g', 'west': 'g', 'up': 'g', 'down': 'g'}),
                                   element([2, 2, 5], [13, 12, 14], {'north': 'v', 'south': 'g', 'east': 'g', 'west': 'g', 'up': 'g', 'down': 'g'}),
                                   box([5, 12, 9], [10, 13, 10], 'g'), box([11, 12, 10], [11.5, 20, 10.5], 'c')],
                                  {'d': 'deck_front', 'v': 'portable_tv_lit' if lit else 'portable_tv', 'g': 'grey_plastic', 'c': 'chrome'}, 'portable_tv')
    # On the bench: it stands on the bench's top, a pixel below this cell.
    out['tin_0'] = m([element([4, -1, 4], [11, 7, 11], {'north': 'l', 'south': 'n', 'east': 'n', 'west': 'n', 'up': 'k'}),
                      element([11, -1, 6], [15, 0, 8], {'up': 'b', 'north': 'b', 'south': 'b', 'east': 'b', 'west': 'b'})],
                     {'l': 'tin', 'n': 'tin', 'k': 'tin_lid_line', 'b': 'brush'}, 'tin')
    # 0.4.530: open on the grass beside June at night while she paints the line (PellNight): its lid off, the paint showing.
    out['tin_1'] = m([element([4, 0, 4], [11, 8, 11], {'north': 'n', 'south': 'n', 'east': 'n', 'west': 'n', 'up': 'p', 'down': 'n'})],
                     {'n': 'tin', 'p': 'tin_paint'}, 'tin')
    for section in range(3):
        out[f'transcript_{section}'] = m([element([0, 0, 15.7], [16, 16, 16], {'north': 't'})], {'t': f'transcript_{section}'}, f'transcript_{section}')
    # 0.4.548: the last page taken off the wall (PellMark).
    out['transcript_3'] = m([element([0, 0, 15.7], [16, 16, 16], {'north': 't'})], {'t': 'transcript_gone'}, 'transcript_gone')
    for stage in range(3):
        out[f'shed_calendar_{stage}'] = m([element([2, 0, 15.7], [14, 16, 16], {'north': 'c'}, uv={'north': [2, 0, 14, 16]})], {'c': f'chemist_{stage}'}, f'chemist_{stage}')
    # ---------------------------------------------------------------------------------------- outside
    for tilly in range(2):
        face = 'stake_tilly' if tilly else 'stake_nessa'
        out[f'stake_{tilly}'] = m([element([7.5, 0, 7.5], [8.5, 11, 8.5], {'north': 'w', 'south': 'w', 'east': 'w', 'west': 'w', 'up': 'w'}),
                                   element([4, 6, 7.9], [12, 10, 8.1], {'north': 't', 'south': 't'}, uv={'north': [4, 4, 12, 8], 'south': [4, 4, 12, 8]}),
                                   element([6, 0, 6], [10, 4, 10], {'north': 'p', 'south': 'p', 'east': 'p', 'west': 'p'}, rot={'origin': [8, 0, 8], 'axis': 'y', 'angle': 45})],
                                  {'w': 'stake_wood', 't': face, 'p': 'seedling_fresh' if tilly else 'seedling_dead'}, face)
    # 0.4.542, the house's aftermath (PellAftermath): the agent's board, a block across, on a post driven into the lawn behind it; the
    # board's foot half a block up and its top half a block over the cell, the post in two parts so each takes its own strip of wood.
    w = {'north': 'w', 'south': 'w', 'east': 'w', 'west': 'w'}
    out['for_sale_0'] = m([element([7, 0, 8.5], [9, 16, 10.5], {**w, 'down': 'w'}, uv={'north': [7, 0, 9, 16], 'south': [7, 0, 9, 16], 'east': [5.5, 0, 7.5, 16], 'west': [8.5, 0, 10.5, 16], 'down': [7, 5.5, 9, 7.5]}),
                           element([7, 16, 8.5], [9, 22, 10.5], {**w, 'up': 'w'}, uv={'north': [7, 10, 9, 16], 'south': [7, 10, 9, 16], 'east': [5.5, 10, 7.5, 16], 'west': [8.5, 10, 10.5, 16], 'up': [7, 8.5, 9, 10.5]}),
                           element([0, 8, 7.5], [16, 24, 8.5], {'north': 'f', 'south': 'b', 'east': 'b', 'west': 'b', 'up': 'b', 'down': 'b'},
                                   uv={'north': [0, 0, 16, 16], 'south': [0, 0, 16, 16], 'east': [7.5, 0, 8.5, 16], 'west': [7.5, 0, 8.5, 16], 'up': [0, 7.5, 16, 8.5], 'down': [0, 7.5, 16, 8.5]})],
                          {'w': 'stake_wood', 'f': 'for_sale', 'b': 'for_sale_back'}, 'for_sale')
    # The kennel, two cells wide: half 0 meets half 1 at its west edge as drawn (x 0), half 1 meets half 0 at its east (x 16). The
    # opening is in the middle, a real gap with the dark inside; the roof's ridge runs along the front, the gables at the two ends.
    for half in range(2):
        seam, outer = (0, 15) if half == 0 else (16, 1)
        lo, hi = min(seam, outer), max(seam, outer)
        d0, d1 = (0, KENNEL_DOOR_HALF) if half == 0 else (16 - KENNEL_DOOR_HALF, 16)
        dh = 16 - KENNEL_DOOR_V
        side_lo, side_hi = (d1, hi) if half == 0 else (lo, d0)
        jamb = 'west' if half == 0 else 'east'
        els = [element([side_lo, 0, 2], [side_hi, 14, 15], {'north': 'f', 'south': 'w', 'east': 'w', 'west': 'w', 'down': 'w', jamb: 'k'}),
               element([d0, dh, 2], [d1, 14, 15], {'north': 'f', 'south': 'w', 'down': 'k', 'east': 'w', 'west': 'w'}),
               element([d0, 0, 14], [d1, dh, 15], {'north': 'k'}),
               element([d0, 0, 2], [d1, 0.1, 14], {'up': 'l'}),
               element([lo - (1 if half else 0), 16.5, -1], [hi + (0 if half else 1), 17.5, 8.2], {k: 'r' for k in ('north', 'south', 'east', 'west', 'up', 'down')}, rot={'origin': [8, 17, 8], 'axis': 'x', 'angle': -22.5}),
               element([lo - (1 if half else 0), 16.5, 7.8], [hi + (0 if half else 1), 17.5, 17], {k: 'r' for k in ('north', 'south', 'east', 'west', 'up', 'down')}, rot={'origin': [8, 17, 8], 'axis': 'x', 'angle': 22.5})]
        # The gable at the outer end, stepped under the roof.
        gx = [14.5, 15] if half == 0 else [1, 1.5]
        for (y0, y1, z0, z1) in ((14, 15, 4.4, 11.6), (15, 16, 6.8, 9.2)):
            els.append(element([gx[0], y0, z0], [gx[1], y1, z1], {k: 'w' for k in ('north', 'south', 'east', 'west', 'up')}))
        out[f'kennel_{half}'] = m(els, {'f': f'kennel_front_{half}', 'w': 'kennel_wall', 'r': 'kennel_roof', 'k': 'kennel_dark', 'l': 'kennel_floor'}, 'kennel_wall')
    out['bowl_0'] = m([element([4, 0, 4], [12, 2, 12], {'up': 'b', 'north': 's', 'south': 's', 'east': 's', 'west': 's', 'down': 's'}),
                       box([12, 0.2, 7.5], [16, 0.7, 8.5], 'c')], {'b': 'bowl_leaves', 's': 'steel', 'c': 'chain'}, 'bowl_leaves')
    chair = [element([1, 6, 1], [15, 7, 15], {'up': 'p', 'down': 'p', 'north': 'p', 'south': 'p', 'east': 'p', 'west': 'p'}),
             element([1, 7, 13], [15, 20, 15], {'north': 'p', 'south': 'p', 'east': 'p', 'west': 'p', 'up': 'p'}, rot={'origin': [8, 7, 14], 'axis': 'x', 'angle': 22.5}),
             # The towel over its back, hanging both sides.
             element([2, 12, 12.6], [14, 20.5, 15.4], {k: 't' for k in ('north', 'south', 'east', 'west', 'up')}, rot={'origin': [8, 7, 14], 'axis': 'x', 'angle': 22.5}),
             *[box([x, 0, z], [x + 1.5, 6, z + 1.5], 'p') for x in (1, 13.5) for z in (1, 13.5)]]
    out['pool_chair_0'] = m(chair, {'p': 'pool_chair', 't': 'towel_faded'}, 'pool_chair')
    # 0.4.533, stage 1: June's robe left on it while she swims at dawn: folded over the seat's front edge, its sleeve hanging, and the
    # belt trailing to the deck.
    out['pool_chair_1'] = m(chair + [box([2, 7, 2], [14, 8.5, 11], 'r'), box([2, 2, 1], [14, 8.5, 2.2], 'r'),
                                     box([3, 0.5, 0.2], [5.5, 8.5, 1.2], 'r'), box([10, 0, 0], [11, 6, 0.8], 'r')],
                            {'p': 'pool_chair', 't': 'towel_faded', 'r': 'robe'}, 'pool_chair')
    out['pump_0'] = m([element([2, 0, 3], [14, 8, 13], {'north': 'f', 'south': 'g', 'east': 'g', 'west': 'g', 'up': 'f', 'down': 'g'}), box([4, 8, 5], [12, 11, 11], 'g', up='f')],
                      {'f': 'pump', 'g': 'grey_plastic'}, 'pump')
    # 0.4.530: June's battery lantern, set down on the grass behind the shed while she paints (PellNight): a base, the globe lit (it
    # gives its own light, so it shows in the dark), the cap, and the handle up over it.
    glow = element([5, 2, 5], [11, 8, 11], {'north': 'g', 'south': 'g', 'east': 'g', 'west': 'g'})
    glow['neoforge_data'] = {'block_light': 15, 'sky_light': 15}
    out['lantern_0'] = m([box([4, 0, 4], [12, 2, 12], 'b'), glow, box([4, 8, 4], [12, 10, 12], 'b'),
                          box([5, 10, 7], [6, 12, 9], 'b'), box([10, 10, 7], [11, 12, 9], 'b'), box([5, 12, 7], [11, 13, 9], 'b')],
                         {'b': 'lantern_body', 'g': 'lantern_globe'}, 'lantern_globe')
    # 0.4.532: the kitchen telephone, on the wall by the arch (PellCall). Its plate; the receiver hanging on it (stage 0), or not, and
    # then the empty hook (stage 1); its cord, looped below the receiver to the plate, or out from the plate to whoever has the
    # receiver; and the strip of tape on the wall beside it.
    plate = element([5, 1, 15], [11, 15, 16], {'north': 'p', 'east': 'p', 'west': 'p', 'up': 'p', 'down': 'p'})
    tape = element([11.5, 8.5, 15.9], [17.5, 12.5, 16], {'north': 't'}, uv={'north': [4, 6, 10, 10]})
    handset = [element([6.5, 6, 13.5], [9.5, 11, 15], {'north': 'h', 'east': 'h', 'west': 'h'}),
               element([6, 11, 13], [10, 14, 15], {k: 'h' for k in ('north', 'east', 'west', 'up', 'down')}),
               element([6, 3, 13], [10, 6, 15], {k: 'h' for k in ('north', 'east', 'west', 'up', 'down')})]
    cord = {k: 'c' for k in ('north', 'east', 'west', 'south')}
    under = element([6.5, -4, 14.25], [9.5, -3, 15], {**cord, 'down': 'c'}, uv={'north': [0, 0, 3, 1], 'south': [0, 0, 3, 1], 'down': [0, 0, 3, 1]})
    for face in ('north', 'south', 'down'):
        under['faces'][face]['rotation'] = 90
        under['faces'][face]['uv'] = [0, 0, 1, 3]
    loop = [element([6.5, -3, 14.25], [7.5, 3, 15], cord, uv={k: [7, 0, 8, 6] for k in cord}),
            element([8.5, -3, 14.25], [9.5, 1, 15], cord, uv={k: [8, 0, 9, 4] for k in cord}), under]
    # 0.4.566: off the hook, only the cord's root at the foot of the plate: the cord itself is drawn from here to the receiver in June's
    # hand, wherever she has it (client/PellPhoneCord), since a stub hanging into the air never reached her.
    out_ = element([7.5, 0, 14.25], [8.5, 1, 15], {**cord, 'down': 'c'}, uv={k: [7, 0, 8, 1] for k in (*cord, 'down')})
    hook = element([7, 12, 14], [9, 13, 15], {k: 'p' for k in ('north', 'east', 'west', 'up', 'down')}, uv={k: [7, 2, 9, 3] for k in ('north', 'east', 'west', 'up', 'down')})
    phone_tex = {'p': 'phone_plate', 'h': 'phone_handset', 'c': 'phone_cord', 't': 'phone_tape'}
    out['phone_0'] = m([plate, tape, *handset, *loop], phone_tex, 'phone_plate')
    out['phone_1'] = m([plate, tape, hook, out_], phone_tex, 'phone_plate')
    return out


def toy_parts(toy, dx, dz):
    """A toy standing on the circle's line in its cell, turned to face out from the circle's centre."""
    cx, cz = CIRCLE_CENTRE
    ox, oz = (dx + .5 - cx), (dz + .5 - cz)
    d = (ox * ox + oz * oz) ** .5 or 1
    px, pz = 8 + (cx + ox / d * CIRCLE_R - dx - .5) * 16, 8 + (cz + oz / d * CIRCLE_R - dz - .5) * 16
    px, pz = max(3, min(13, px)), max(3, min(13, pz))
    if toy == 'bear':
        return [element([px - 2, 0.1, pz - 1.5], [px + 2, 4, pz + 1.5], {k: 'y' for k in ('north', 'south', 'east', 'west', 'up')}, uv={k: [0, 0, 4, 4] for k in ('north', 'south', 'east', 'west', 'up')}),
                element([px - 1.5, 4, pz - 1], [px + 1.5, 6.5, pz + 1], {k: 'y' for k in ('north', 'south', 'east', 'west', 'up')}, uv={k: [0, 0, 3, 3] for k in ('north', 'south', 'east', 'west', 'up')})]
    if toy == 'car':
        return [element([px - 2.5, 0.1, pz - 1.5], [px + 2.5, 2, pz + 1.5], {k: 'y' for k in ('north', 'south', 'east', 'west', 'up')}, uv={k: [4, 0, 9, 3] for k in ('north', 'south', 'east', 'west', 'up')})]
    if toy == 'dino':
        return [element([px - 2, 0.1, pz - 1], [px + 2, 3, pz + 1], {k: 'y' for k in ('north', 'south', 'east', 'west', 'up')}, uv={k: [9, 0, 13, 3] for k in ('north', 'south', 'east', 'west', 'up')}),
                element([px + 1, 2, pz - .5], [px + 2.5, 5, pz + .5], {k: 'y' for k in ('north', 'south', 'east', 'west', 'up')}, uv={k: [9, 3, 11, 6] for k in ('north', 'south', 'east', 'west', 'up')})]
    return [element([px - 1.5, 0.1, pz - 1.5], [px + 1.5, 3, pz + 1.5], {k: 'y' for k in ('north', 'south', 'east', 'west', 'up')}, uv={k: [0, 8, 3, 11] for k in ('north', 'south', 'east', 'west', 'up')}),
            element([px - 1, 3, pz - 1], [px + 1, 5, pz + 1], {k: 'y' for k in ('north', 'south', 'east', 'west', 'up')}, uv={k: [4, 8, 6, 10] for k in ('north', 'south', 'east', 'west', 'up')})]



def toy_elements(toy, dx, dz):
    """The toy's parts, standing on top of the floor cell (a block up from the cell's own floor)."""
    out = []
    for el in toy_parts(toy, dx, dz):
        el = dict(el); el['from'] = [el['from'][0], el['from'][1] + 16, el['from'][2]]; el['to'] = [el['to'][0], el['to'][1] + 16, el['to'][2]]
        out.append(el)
    return out

KINDS = ['counter', 'sink', 'stove', 'fridge', 'fridge_top', 'knife_block', 'lunchboxes', 'cupboard_upper', 'table', 'chair', 'chore_chart', 'calendar',
         'recliner', 'tv_cabinet', 'mantel', 'window_seat', 'side_table', 'floor_lamp', 'photos', 'hooks', 'coat_hook',
         'bed', 'drawers', 'desk', 'bookshelf', 'wardrobe', 'switch', 'marks', 'border',
         'mattress', 'circle', 'shoes', 'cutlery', 'vanity', 'mirror', 'bath', 'toilet',
         'freezer', 'stool', 'power', 'cord', 'trough', 'bucket', 'chlorine', 'washer',
         'camp_bed', 'bulb', 'box_winter', 'box_books', 'box_dont', 'pegboard', 'towel',
         'tapes', 'workbench', 'shed_tv', 'tin', 'transcript', 'shed_calendar',
         'stake', 'kennel', 'bowl', 'pool_chair', 'pump', 'lantern', 'phone', 'freezer_open', 'for_sale', 'marks_after', 'photo_broken']


# ------------------------------------------------------------------------------------------------ the block
FACING_Y = {'north': 0, 'east': 90, 'south': 180, 'west': 270}
MAX_STAGE = 4
NAMES = {'pell_thing': 'Belongings'}


# 0.4.527: the textures that play, each a strip of sixteen-pixel pictures, and how: the television's tape.
ANIMATED = {'tv_lit': {'animation': {'frametime': TAPE_FRAMETIME, 'interpolate': False, 'frames': TAPE_ORDER}}}


def default_uv(face, lo, hi):
    """The texture rectangle the game gives a face that names none: the part of the block's square the face covers."""
    return {'down': [lo[0], 16 - hi[2], hi[0], 16 - lo[2]], 'up': [lo[0], lo[2], hi[0], hi[2]],
            'north': [16 - hi[0], 16 - hi[1], 16 - lo[0], 16 - lo[1]], 'south': [lo[0], 16 - hi[1], hi[0], 16 - lo[1]],
            'west': [lo[2], 16 - hi[1], hi[2], 16 - lo[1]], 'east': [16 - hi[2], 16 - hi[1], 16 - lo[2], 16 - lo[1]]}[face]


def fit(a, b):
    """One side of a texture rectangle moved inside the picture: by whole pictures, as if it tiled, where that fits; else slid to the
    nearer edge; a span wider than the picture takes the whole of it."""
    if b - a > 16:
        return 0, 16
    if a < 0:
        k = math.ceil(-a / 16) * 16
        return (a + k, b + k) if b + k <= 16 else (0, b - a)
    if b > 16:
        k = math.ceil((b - 16) / 16) * 16
        return (a - k, b - k) if a - k >= 0 else (16 - (b - a), 16)
    return a, b


def fit_uvs(models):
    """0.4.527: a face that names no texture rectangle is given the part of the block's square it covers, and on a part that stands above
    the block's top or past its side that falls outside the face's own picture, which the game then fills from whatever lies next to it
    in the atlas. Every such face is given a rectangle inside its picture instead."""
    fitted = 0
    for obj in models.values():
        for el in obj['elements']:
            for face, spec in el['faces'].items():
                if 'uv' in spec:
                    continue
                u0, v0, u1, v1 = default_uv(face, el['from'], el['to'])
                if min(u0, v0) >= 0 and max(u1, v1) <= 16:
                    continue
                (u0, u1), (v0, v1) = fit(u0, u1), fit(v0, v1)
                spec['uv'] = [u0, v0, u1, v1]
                fitted += 1
    return fitted


def stages(models):
    """How many drawn stages each kind has (the circle's sixteen cells are its own case)."""
    return {kind: sum(1 for key in models if re.fullmatch(re.escape(kind) + r'_\d+', key)) for kind in KINDS}


def textures():
    out = {}
    for room in (kitchen, living, hall, nessa, callum, bathroom, laundry, garage, shed, outside, rest):
        for name, im in room().items():
            assert name not in out, name
            out[name] = im
    return out


def java_kinds():
    """PellThing's kinds, as the game lists them: the tool and the game must agree."""
    src = (ROOT / 'src/main/java/io/github/knaitoe/theoldesthouse/labyrinth/PellThing.java').read_text()
    kinds = re.search(r'enum Kind implements StringRepresentable \{\s*([A-Z_,\s]+);', src).group(1)
    return [k.strip().lower() for k in kinds.split(',') if k.strip()]


def files(models):
    out = {}
    for key, obj in models.items():
        out[f'models/block/pell/thing/{key}.json'] = obj
    count = stages(models)
    variants = {}
    for kind in KINDS:
        for f, (facing, y) in enumerate(FACING_Y.items()):
            for stage in range(MAX_STAGE):
                if kind == 'circle':
                    # A cell of the circle: never turned; which cell it is, is its facing and stage (PellThing.circle).
                    variants[f'facing={facing},kind={kind},stage={stage}'] = {'model': NS + f'block/pell/thing/circle_{f * MAX_STAGE + stage}'}
                    continue
                drawn = stage if stage < count[kind] else 0
                v = {'model': NS + f'block/pell/thing/{kind}_{drawn}'}
                if y:
                    v['y'] = y
                variants[f'facing={facing},kind={kind},stage={stage}'] = v
    out['blockstates/pell_thing.json'] = {'variants': variants}
    return out


def main():
    check = argparse.ArgumentParser()
    check.add_argument('--check', action='store_true')
    check = check.parse_args().check
    models = thing_models()
    fit_uvs(models)
    images, data = textures(), files(models)
    assert java_kinds() == KINDS, (java_kinds(), KINDS)
    count = stages(models)
    assert count['circle'] == 16, count['circle']
    for kind, n in count.items():
        assert 1 <= n <= MAX_STAGE or kind == 'circle', (kind, n)
        assert all(f'{kind}_{i}' in models for i in range(n)), kind
    used, mats = set(), set(house_materials())
    for key, obj in models.items():
        for path in obj['textures'].values():
            if path.startswith(NS + 'block/pell_'):
                assert path[len(NS + 'block/pell_'):] in mats, (key, path)
                continue
            name = path.replace(NS + 'block/pell/thing/', '')
            assert name in images, (key, path)
            used.add(name)
        for el in obj['elements']:
            for axis in range(3):
                assert -16 <= el['from'][axis] <= el['to'][axis] <= 32, (key, el['from'], el['to'])
            # 0.4.527: every face's picture is taken from inside its own texture.
            for face, spec in el['faces'].items():
                uv = spec.get('uv', default_uv(face, el['from'], el['to']))
                assert min(uv) >= 0 and max(uv) <= 16, (key, face, uv)
    assert used == set(images), sorted(set(images) - used)
    for name, im in images.items():
        frames = max(ANIMATED[name]['animation']['frames']) + 1 if name in ANIMATED else 1
        assert im.size == (16, 16 * frames), (name, im.size)
        path = A / 'textures/block/pell/thing' / f'{name}.png'
        if check:
            with Image.open(path) as actual:
                assert actual.convert('RGBA').tobytes() == im.tobytes(), name
        else:
            path.parent.mkdir(parents=True, exist_ok=True); im.save(path, optimize=True)
        if name in ANIMATED:
            meta = path.parent / f'{name}.png.mcmeta'
            if check:
                assert json.loads(meta.read_text()) == ANIMATED[name], name
            else:
                meta.write_text(json.dumps(ANIMATED[name], indent=2) + '\n')
    for rel, obj in data.items():
        path = A / rel
        if check:
            assert json.loads(path.read_text()) == obj, rel
        else:
            path.parent.mkdir(parents=True, exist_ok=True); path.write_text(json.dumps(obj, indent=2) + '\n')
    # The two folders are the tool's alone: nothing in them it does not draw.
    for folder, wanted in ((A / 'textures/block/pell/thing', {f'{name}.png' for name in images} | {f'{name}.png.mcmeta' for name in ANIMATED}),
                           (A / 'models/block/pell/thing', {f'{key}.json' for key in models})):
        stale = sorted(f.name for f in folder.iterdir() if f.name not in wanted)
        if check:
            assert not stale, (folder, stale)
        else:
            for name in stale:
                (folder / name).unlink()
    lang_path = A / 'lang/en_us.json'; lang = json.loads(lang_path.read_text())
    for key, name in NAMES.items():
        if check:
            assert lang.get(f'block.the_oldest_house.{key}') == name, key
        else:
            lang[f'block.the_oldest_house.{key}'] = name
    if not check:
        lang_path.write_text(json.dumps(lang, indent=2, ensure_ascii=False) + '\n')
    print(('Verified' if check else 'Drew') + f" the Pell house's things: {len(KINDS)} kinds, {len(images)} textures, {len(models)} models.")


if __name__ == '__main__':
    main()
