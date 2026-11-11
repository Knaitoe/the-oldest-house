"""Bike night (0.4.463): the six bikes, the rack they stand in and the plank ramps.

Draws, in native Minecraft pixels from fixed seeds, everything the bikes need and nothing else yet (the park, the town and the
Hollow come in later slices): a paint card for each bike (the Wasp, the Hare, the Comet, the Hopper, the Lamp and the Rattler), the
tyre, the chrome, the Lamp's wicker and its glass (dark and lit), the Rattler's seat and its card, the rack's steel, and the ramp's
plywood and timber. Each bike is built from bars a pixel or so thick, as Borrasca's bike is (tools/draw_borrasca_town_art.py): a
frame the ridden bike is drawn from, its wheels, the cranks, and the same bike whole, on its kickstand and lying on its side, for
the block it is when nobody is on it. A bar takes one pixel of its bike's paint card, so the card is a set of swatches: the frame,
the trim, the saddle and the grips. Faces bigger than a bar (the basket, the long seat, the card in the spokes, the ramps) take a
part of a picture the size they are, so everything is sixteen pixels to the block.

Writes the models and block states, the blocks' names and the bike's name. --check verifies everything. --sheet PATH draws a page
for review: every bike from the side, from above and from the front, standing and lying, and the rack and the ramps.

Drawing or checking, it refuses a model in which two faces of different colours lie in one plane and overlap (tools/flicker.py):
those flicker against each other as the eye moves, which a still picture of the model does not show.
"""
from pathlib import Path
import argparse
import json
import math
import random
from PIL import Image, ImageDraw

from draw_borrasca_town_art import shade, new, fill, dot, element
from flicker import fights

ROOT = Path(__file__).resolve().parents[1]
A = ROOT / 'src/main/resources/assets/the_oldest_house'
NS = 'the_oldest_house:'
DIR = 'block/bike_night/'
P = NS + DIR
MAKES = ('wasp', 'hare', 'comet', 'hopper', 'lamp', 'rattler')
FACES = ('north', 'south', 'east', 'west', 'up', 'down')
# A bike's block faces the way the bike points; its model points east.
FACING_Y = {'east': 0, 'south': 90, 'west': 180, 'north': 270}
# Two faces of different colours that lie in one plane and overlap flicker against each other as the eye moves. Where two pieces must
# overlap, one is this much proud of the other; tools/flicker.py finds any that are not.
SLIP = .05
# The swatches on a paint card, by the pixel a bar takes: the frame, the trim, the saddle, the grips.
FRAME, TRIM, SEAT, GRIP = 2, 6, 10, 14

# ---------------------------------------------------------------------------------------------------------------- the pictures
CARDS = {
    # frame, trim, saddle, grips
    'wasp': ('#d9b21f', '#1c1c1e', '#19191b', '#19191b'),
    'hare': ('#e9e6dc', '#b3261f', '#2a2522', '#b3261f'),
    'comet': ('#27508f', '#e8e4d6', '#1d1b1a', '#e8e4d6'),
    'hopper': ('#b6231f', '#e9e7e1', '#e9e7e1', '#e9e7e1'),
    'lamp': ('#2f5a3a', '#e3d9b8', '#6b4526', '#2a2522'),
    'rattler': ('#c9601a', '#1d1b1a', '#d2b23a', '#1d1b1a'),
}


def card(make, seed):
    """A bike's paint card: four swatches four pixels wide, each a little uneven, as enamel and vinyl are."""
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0)); rnd = random.Random(seed); px = im.load()
    for i, colour in enumerate(CARDS[make]):
        for y in range(16):
            for x in range(i * 4, i * 4 + 4):
                px[x, y] = shade(colour, 0 if y == 7 else rnd.randint(-5, 5))
    return im


def tyre():
    """Rubber: column 7 the tread, column 6 the groove between two blocks of it."""
    im = new('#1f1f21', 46301, 3)
    for y in range(16):
        dot(im, 6, y, '#101011'); dot(im, 7, y, '#232325')
    return im


def tyre_pale():
    """The Hopper's tyres: the pale rubber a stunt bike wore."""
    im = new('#d9d5c8', 46302, 3)
    for y in range(16):
        dot(im, 6, y, '#b3ae9f'); dot(im, 7, y, '#dcd8cb')
    return im


def chrome():
    im = new('#b9bcc0', 46303, 3)
    for y in range(16):
        dot(im, 7, y, '#c3c6ca')
    fill(im, 0, 0, 16, 1, '#dfe2e5')
    return im


def wicker():
    """The Lamp's basket: cane woven over and under, two pixels to a strand."""
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0)); px = im.load()
    for y in range(16):
        for x in range(16):
            over = ((x // 2) + (y // 2)) % 2 == 0
            px[x, y] = shade('#b08a4e' if over else '#8a6a38', (1 if (x + y) % 2 else -1) * 3)
    for x in range(16):
        px[x, 0] = shade('#c9a467'); px[x, 15] = shade('#6f5329')
    return im


def glass(lit):
    """The Lamp's lens: ribbed glass, dark with the dynamo still and yellow-white with it turning."""
    im = new('#f3e7a6' if lit else '#4a4d45', 46305, 3)
    for y in range(0, 16, 2):
        fill(im, 0, y, 16, y + 1, '#fff6c9' if lit else '#5b5e54')
    return im


def paper():
    """A rolled newspaper: newsprint, a band of darker type round it."""
    im = new('#d8d4c6', 46306, 4)
    for y in (3, 4, 9, 10):
        for x in range(16):
            if (x + y) % 3:
                dot(im, x, y, '#8d897c')
    return im


def snake():
    """The Rattler's long seat: quilted vinyl in diamonds, as a rattlesnake's back is."""
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0)); px = im.load()
    for y in range(16):
        for x in range(16):
            d = (abs((x % 4) - 1.5) + abs((y % 4) - 1.5))
            px[x, y] = shade('#d2b23a' if d < 1.6 else '#2a2118', 2 if (x * 7 + y * 3) % 5 == 0 else 0)
    return im


def spoke_card():
    """The card clothes-pegged into the Rattler's spokes: a playing card, white, a red pip at two corners. Its face is two pixels by three."""
    im = new('#eeeae0', 46308, 2)
    for x, y in ((0, 0), (1, 2), (4, 0), (5, 2), (8, 0), (9, 2), (12, 0), (13, 2)):
        dot(im, x, y, '#b3261f')
    return im


def peg_wood():
    im = new('#b98f55', 46309, 4)
    fill(im, 0, 7, 16, 8, '#8c6a3a')
    return im


def steel():
    """The rack: galvanised pipe gone dull, a little rust at the welds."""
    im = new('#8f9498', 46310, 5)
    for x, y in ((2, 3), (9, 11), (13, 6), (5, 14)):
        dot(im, x, y, '#7a5a44')
    fill(im, 0, 0, 16, 1, '#a4a9ad')
    return im


def plywood():
    """The ramp's sheet: plywood scuffed by tyres up the middle, a nail head at each corner."""
    im = new('#b89a66', 46311, 5); rnd = random.Random(46312)
    for y in range(16):
        if y % 5 == 2:
            for x in range(16):
                dot(im, x, y, shade('#a88a58', rnd.randint(-3, 3)))
    for y in range(16):
        for x in (6, 7, 8, 9):
            if rnd.random() < .45:
                dot(im, x, y, '#6f6252')
    for x, y in ((1, 1), (14, 1), (1, 14), (14, 14)):
        dot(im, x, y, '#4b4b4f')
    return im


def timber():
    """The ramp's frame: sawn two-by-four, grain along it."""
    im = new('#9c7a4a', 46313, 4)
    for y in (2, 6, 7, 11, 14):
        fill(im, 0, y, 16, y + 1, '#86683c')
    dot(im, 4, 9, '#6d5230'); dot(im, 5, 9, '#6d5230')
    return im


def number():
    """The Wasp's race plate: white, a black 7. The plate is five pixels by four, so the 7 is."""
    im = new('#ecebe6', 46314, 2)
    for x, y in ((1, 0), (2, 0), (3, 0), (3, 1), (2, 2), (2, 3)):
        dot(im, x, y, '#19191b')
    return im


def textures():
    out = {f'{DIR}{make}.png': card(make, 46320 + i) for i, make in enumerate(MAKES)}
    out.update({DIR + 'tyre.png': tyre(), DIR + 'tyre_pale.png': tyre_pale(), DIR + 'chrome.png': chrome(), DIR + 'wicker.png': wicker(),
                DIR + 'glass.png': glass(False), DIR + 'glass_lit.png': glass(True), DIR + 'paper.png': paper(), DIR + 'snake.png': snake(),
                DIR + 'card.png': spoke_card(), DIR + 'peg.png': peg_wood(), DIR + 'steel.png': steel(), DIR + 'plywood.png': plywood(),
                DIR + 'timber.png': timber(), DIR + 'number.png': number()})
    return out


# ---------------------------------------------------------------------------------------------------------------- the bars
def bar(parts, lo, hi, tex, px=7, rot=None):
    """One bar, every face from one pixel of its texture: the pixel at (px, 7)."""
    parts.append(element([round(v, 3) for v in lo], [round(v, 3) for v in hi], {'all': tex}, {k: [px, 7, px + 1, 8] for k in FACES}, rot))


def tube(parts, a, b, tex, px=7, w=1.0, z=(7.5, 8.5)):
    """A tube from a to b in the bike's plane. A model's piece turns only by 22.5 or 45 degrees, so a tube is drawn at the nearest of
    those to the line asked for, about its middle; the frames below ask only for lines that are."""
    (ax, ay), (bx, by) = a, b
    cx, cy = (ax + bx) / 2, (ay + by) / 2
    length = math.hypot(bx - ax, by - ay)
    angle = round(math.degrees(math.atan2(by - ay, bx - ax)) / 22.5) * 22.5
    angle = ((angle + 90) % 180) - 90
    if abs(angle) <= 45:
        lo, hi, turn = [cx - length / 2, cy - w / 2, z[0]], [cx + length / 2, cy + w / 2, z[1]], angle
    else:
        lo, hi, turn = [cx - w / 2, cy - length / 2, z[0]], [cx + w / 2, cy + length / 2, z[1]], angle - 90 if angle > 0 else angle + 90
    bar(parts, lo, hi, tex, px, {'angle': turn, 'axis': 'z', 'origin': [round(cx, 3), round(cy, 3), 8]} if turn else None)


def plate(parts, lo, hi, tex, faces, rot=None):
    """A flat piece that shows a picture: each face named takes the part of the picture its own size, from the corner given."""
    lo, hi = [round(v, 3) for v in lo], [round(v, 3) for v in hi]
    size = {'north': (hi[0] - lo[0], hi[1] - lo[1]), 'south': (hi[0] - lo[0], hi[1] - lo[1]), 'east': (hi[2] - lo[2], hi[1] - lo[1]),
            'west': (hi[2] - lo[2], hi[1] - lo[1]), 'up': (hi[0] - lo[0], hi[2] - lo[2]), 'down': (hi[0] - lo[0], hi[2] - lo[2])}
    uv = {}
    for k, (u, v) in faces.items():
        w, h = size[k]
        uv[k] = [u, v, round(min(16, u + w), 3), round(min(16, v + h), 3)]
    parts.append(element(lo, hi, {k: tex for k in faces}, uv, rot))


def wheel(parts, cx, cy, r, thick=1.2, wide=1.0, mag=False):
    """A wheel in the bike's plane about its hub: the tyre a closed ring of sixteen pieces (every other one takes the tread's groove,
    so the wheel is seen to turn), the hub, and its spokes: eight thin ones, or a mag wheel's four thick ones."""
    z0, z1 = 8 - wide / 2, 8 + wide / 2
    mid = r - thick / 2
    half = round(r * math.tan(math.pi / 16) + .12, 3)
    for k in range(16):
        phi = math.radians(k * 22.5)
        mx, my = cx + mid * math.cos(phi), cy + mid * math.sin(phi)
        tangent = (k * 22.5 + 90) % 180
        if tangent > 90:
            tangent -= 180
        # Neighbours overlap where they join, and are not the same colour: the grooved ones are a hair narrower, so no two sides share a plane.
        za, zb = (z0 + SLIP, z1 - SLIP) if k % 2 else (z0, z1)
        if abs(tangent) <= 45:
            lo, hi, angle = [mx - half, my - thick / 2, za], [mx + half, my + thick / 2, zb], tangent
        else:
            lo, hi, angle = [mx - thick / 2, my - half, za], [mx + thick / 2, my + half, zb], tangent - 90 if tangent > 0 else tangent + 90
        bar(parts, lo, hi, 't', 6 if k % 2 else 7, {'angle': angle, 'axis': 'z', 'origin': [round(mx, 3), round(my, 3), 8]} if angle else None)
    bar(parts, [cx - .6, cy - .6, 7 - SLIP], [cx + .6, cy + .6, 9 + SLIP], 'c')
    reach = r - thick + .1
    s = .45 if mag else .2
    zs = (7.6, 8.4) if mag else (7.8, 8.2)
    spokes = (0, 90) if mag else (0, 90, 45, -45)
    for a in spokes:
        if a == 0:
            bar(parts, [cx - reach, cy - s, zs[0]], [cx + reach, cy + s, zs[1]], 'c')
        elif a == 90:
            bar(parts, [cx - s, cy - reach, zs[0]], [cx + s, cy + reach, zs[1]], 'c')
        else:
            bar(parts, [cx - reach, cy - s, zs[0]], [cx + reach, cy + s, zs[1]], 'c', rot={'angle': a, 'axis': 'z', 'origin': [round(cx, 3), round(cy, 3), 8]})


def cranks():
    """The cranks about the middle of the block, as the axle is drawn turning: the left pedal (toward -z, on the rider's left) down and
    the right one up, each on an arm two and a half pixels long."""
    parts = []
    bar(parts, [7.4, 7.4, 6], [8.6, 8.6, 10], 'c')                            # the shell, a little fatter than the seat tube over it
    bar(parts, [7.6, 5.5, 5.5], [8.4, 8, 6], 'c')
    bar(parts, [6.75, 5, 4], [9.25, 5.75, 5.5], 'k')
    bar(parts, [7.6, 8, 10], [8.4, 10.5, 10.5], 'c')
    bar(parts, [6.75, 10.25, 10.5], [9.25, 11, 12], 'k')
    return parts


def standing(parts, axle):
    """What a bike on its stand has that a ridden one is drawn without: the crank axle, the pedals level, and the kickstand, its foot on the ground."""
    ax, ay = axle
    bar(parts, [ax - .6, ay - .6, 6], [ax + .6, ay + .6, 10], 'c')
    bar(parts, [ax - 1, ay - .8, 5], [ax + 1, ay - .2, 6], 'k')
    bar(parts, [ax - 1, ay + .2, 10], [ax + 1, ay + .8, 11], 'k')
    bar(parts, [ax + .5, .5, 9.5], [ax + 1.5, ay, 10.5], 'c', rot={'angle': 22.5, 'axis': 'x', 'origin': [ax + 1, ay, 8.5]})


# ---------------------------------------------------------------------------------------------------------------- the six bikes
# Each: where its hubs and its crank axle are, its wheels, how high its saddle is, and its frame without wheels, cranks or stand.
# A bike runs along x with its front at +x, its wheels in the plane z 8, the road at y 0.
class Bike:
    def __init__(self, back, front, axle, saddle, r_back, r_front, thick, wide, mag=False):
        self.back, self.front, self.axle, self.saddle = back, front, axle, saddle
        self.r_back, self.r_front, self.thick, self.wide, self.mag = r_back, r_front, thick, wide, mag
        self.parts = []


def bmx(b, plate_too):
    """The frame the Wasp and the Hopper share: low, with a short seat tube and tall braced bars."""
    p = b.parts
    (bx, by), (fx, fy) = b.back, b.front
    off = b.wide / 2 + .1
    for z in ((8 - off - .3, 8 - off), (8 + off, 8 + off + .3)):
        tube(p, (bx, by), (7.5, by), 'f', FRAME, .9, z)                      # chain stays
        tube(p, (fx, fy), (fx, 9), 'c', 7, .9, z)                            # fork
    tube(p, (7.5, 4), (7.5, 10), 'f', FRAME)                                 # seat tube
    tube(p, (bx, by), (7.5, by + 5), 'f', FRAME)                             # seat stays
    tube(p, (7.5, 9.5), (12.5, 9.5), 'f', FRAME)                             # top tube
    tube(p, (7.5, 4.5), (12.5, 9.5), 'f', FRAME)                             # down tube
    tube(p, (12.5, 8.5), (12.5, 11), 'f', FRAME)                             # head tube
    bar(p, [12.2, 8.6, 6.6], [13.8, 9.4, 9.4], 'c')                          # fork crown
    tube(p, (12.5, 11), (12.5, 12.4), 'c')                                   # stem
    bar(p, [12.1, 12, 5.2], [12.9, 12.8, 10.8], 'c')                         # the bars' foot
    for z in ((5.2, 6), (10, 10.8)):
        bar(p, [12.1, 12.8, z[0]], [12.9, 15.2, z[1]], 'c')                  # risers
    bar(p, [12.2, 13.7, 6], [12.8, 14.3, 10], 'c')                           # the brace between them
    bar(p, [12, 14.5, 3.4], [13, 15.3, 5.4], 'f', GRIP); bar(p, [12, 14.5, 10.6], [13, 15.3, 12.6], 'f', GRIP)  # grips
    tube(p, (7.5, 10), (7.5, 11), 'c')                                       # seat post
    bar(p, [5.4, 11, 7], [9.2, 12, 9], 'f', SEAT)                            # seat
    bar(p, [8.6, 10, 7.2], [11.6, 10.7, 8.8], 'f', TRIM)                     # the pad on the top tube
    if plate_too:
        plate(p, [13.3, 11.2, 5.5], [13.5, 15.2, 10.5], 'n', {'east': (0, 0)})
        bar(p, [13.25, 11.2, 5.5], [13.3, 15.2, 10.5], 'f', TRIM)


def wasp():
    b = Bike((2.5, 4.5), (13.5, 4.5), (7.5, 4.5), 12, 4.5, 4.5, 1.8, 1.6)
    bmx(b, True)
    return b


def hopper():
    b = Bike((2.5, 4.5), (13.5, 4.5), (7.5, 4.5), 12, 4.5, 4.5, 1.5, 1.4, mag=True)
    bmx(b, False)
    for hx, hy in (b.back, b.front):
        bar(b.parts, [hx - .7, hy - .7, 4], [hx + .7, hy + .7, 12], 'c')     # the stunt pegs, out past the wheel on both sides
    return b


def diamond(b, top, seat_top, px=FRAME):
    """A diamond frame on full-size wheels: the chain stays, the seat tube, the seat stays, the top tube and the down tube."""
    p = b.parts
    (bx, by), (fx, fy) = b.back, b.front
    off = b.wide / 2 + .1
    for z in ((8 - off - .3, 8 - off), (8 + off, 8 + off + .3)):
        tube(p, (bx, by), (7, by), 'f', px, .9, z)
    tube(p, (7, 4), (7, seat_top), 'f', px)
    tube(p, (bx + .25, by - .25), (7, by + 5.5 - .25 - .25), 'f', px)        # seat stays, at 45
    tube(p, (7, top), (14, top), 'f', px)
    tube(p, (7, 4), (13, 10), 'f', px)


def hare():
    b = Bike((1.5, 5.5), (14.5, 5.5), (7, 4), 13.7, 5.5, 5.5, 1.0, .9)
    p = b.parts
    diamond(b, 10, 12)
    for z in ((7.05, 7.35), (8.65, 8.95)):
        tube(p, (14.5, 5.5), (14.5, 11), 'c', 7, .9, z)                      # fork
    tube(p, (14, 10.1), (14, 13.5), 'f', TRIM, z=(7.5 - SLIP, 8.5 + SLIP))   # head tube and stem, in the trim, a hair proud of the frame
    bar(p, [13.6, 13.5, 4], [14.4, 14.2, 12], 'c')                           # a flat bar
    bar(p, [13.5, 13.4, 3.4], [14.5, 14.3, 5], 'f', GRIP); bar(p, [13.5, 13.4, 11], [14.5, 14.3, 12.6], 'f', GRIP)
    bar(p, [13.2, 14.2, 10.2], [13.8, 15.2, 10.7], 'c')                      # the thumb-shifter, by the right grip
    bar(p, [13, 15, 10.1], [14, 15.4, 10.8], 'f', TRIM)
    tube(p, (7, 12), (7, 13), 'c')
    bar(p, [5, 13, 7.2], [9.2, 13.7, 8.8], 'f', SEAT)                        # a narrow saddle
    bar(p, [.8, 3, 8.7], [1.7, 5, 9.3], 'c')                                 # the derailleur, hanging under the back hub
    tube(p, (9, 6), (10.4, 7.4), 'f', TRIM, 1.2, (7.4, 8.6))                 # a band of trim round the down tube
    return b


def comet():
    b = Bike((1.5, 6), (14.5, 6), (7, 4.5), 15.3, 6, 6, .8, .7)
    p = b.parts
    (bx, by) = b.back
    for z in ((7.25, 7.5), (8.5, 8.75)):
        tube(p, (bx, 5.5), (7, 5.5), 'f', FRAME, .8, z)
        tube(p, (14.5, 6), (14.5, 12), 'c', 7, .8, z)
    tube(p, (7.025, 4), (7.025, 13), 'f', FRAME, 1.05)                       # the seat tube, the back tyre tucked in against it
    tube(p, (1.5, 6), (7, 11.5), 'f', FRAME)
    tube(p, (7, 11.5), (14, 11.5), 'f', FRAME, 1.1)                          # the top tube, the front tyre just under its end
    tube(p, (7.5, 5), (13.5, 11), 'f', FRAME)
    tube(p, (14, 11), (14, 13.4), 'f', FRAME)
    bar(p, [14, 13, 7.6], [16.3, 13.7, 8.4], 'c')                            # the stem, reaching forward
    bar(p, [15.6, 13, 4.4], [16.3, 13.7, 11.6], 'c')                         # the bar across, between the taped drops
    for z in ((3.6, 4.4), (11.6, 12.4)):                                     # drop bars: forward, down and back, taped
        bar(p, [15.6, 13, z[0]], [17.8, 13.7, z[1]], 'f', GRIP)
        bar(p, [17.1, 11, z[0]], [17.8, 13, z[1]], 'f', GRIP)
        bar(p, [15.4, 10.4, z[0]], [17.8, 11.1, z[1]], 'f', GRIP)
    tube(p, (7, 13), (7, 14.6), 'c')
    bar(p, [5, 14.6, 7.3], [9.3, 15.3, 8.7], 'f', SEAT)
    bar(p, [.8, 3.2, 8.7], [1.7, 5.4, 9.3], 'c')                             # derailleur
    bar(p, [5.6, 3.1, 8.9], [8.4, 5.9, 9.1], 'c')                            # the big chainring
    bar(p, [9.4, 7.4, 8.5], [10.2, 9.6, 9.3], 'c')                           # a bottle cage on the down tube
    return b


def lamp(lit=False):
    b = Bike((1.5, 5.5), (14.5, 5.5), (7, 4), 13.6, 5.5, 5.5, 1.3, 1.2)
    p = b.parts
    diamond(b, 10.6, 12)
    tube(p, (7, 8.8), (13.2, 8.8), 'f', FRAME, .8)                           # the second top tube of a heavy frame
    for z in ((6.9, 7.2), (8.8, 9.1)):
        tube(p, (14.5, 5.5), (14.5, 11.5), 'c', 7, .9, z)
    tube(p, (14, 10), (14, 14), 'c', z=(7.3, 8.7))                           # a fat head tube and stem, wider than the tyre under it
    bar(p, [13.6, 14, 3.5], [14.4, 14.7, 12.5], 'c')                         # the bar, and its grips swept back
    bar(p, [11.4, 14, 3.4], [13.6, 14.8, 4.4], 'f', GRIP); bar(p, [11.4, 14, 11.6], [13.6, 14.8, 12.6], 'f', GRIP)
    bar(p, [12.8, 14.7, 10], [13.6, 15.5, 10.9], 'c')                        # a bell
    for hub, lo, hi in ((b.front, 45, 135), (b.back, 67.5, 202.5)):          # mudguards: a short one in front, a long one down the back
        k = lo
        while k <= hi:
            phi = math.radians(k); r = 6.75
            mx, my = hub[0] + r * math.cos(phi), hub[1] + r * math.sin(phi)
            half = r * math.tan(math.pi / 16) + .1
            tangent = (k + 90) % 180
            if tangent > 90:
                tangent -= 180
            if abs(tangent) <= 45:
                lo_, hi_, ang = [mx - half, my - .25, 7], [mx + half, my + .25, 9], tangent
            else:
                lo_, hi_, ang = [mx - .25, my - half, 7], [mx + .25, my + half, 9], tangent - 90 if tangent > 0 else tangent + 90
            bar(p, lo_, hi_, 'f', TRIM, {'angle': ang, 'axis': 'z', 'origin': [round(mx, 3), round(my, 3), 8]} if ang else None)
            k += 22.5
    bar(p, [-2, 12.3, 6.4], [4.6, 12.8, 9.6], 'c')                           # the rack over the back wheel, and its stays
    for z in ((6.4, 6.9), (9.1, 9.6)):
        bar(p, [1.2, 5.5, z[0]], [1.8, 12.3, z[1]], 'c')
    tube(p, (7, 12), (7, 12.7), 'c')
    bar(p, [4.6, 12.7, 6.8], [9.2, 13.6, 9.2], 'f', SEAT)                    # a sprung leather saddle
    for z in ((7 + SLIP, 7.5), (8.5, 9 - SLIP)):                             # its springs, inside the mudguard's width
        bar(p, [4.8, 11.6, z[0]], [5.3, 12.7, z[1]], 'c')
    bar(p, [2, 3.5, 8.75], [8, 5.6, 9.1], 'f', FRAME)                        # the chain guard, a hair outside the hub
    # The basket on the bars, open at the top, three rolled papers in it.
    x0, x1, y0, y1, z0, z1 = 15.4, 20.4, 10.2, 14.4, 4.6, 11.4
    plate(p, [x0, y0, z0], [x1, y0 + .4, z1], 'w', {'up': (0, 0), 'down': (0, 0)})
    plate(p, [x0, y0, z0], [x1, y1, z0 + .4], 'w', {'north': (0, 0), 'south': (0, 0)})
    plate(p, [x0, y0, z1 - .4], [x1, y1, z1], 'w', {'north': (0, 0), 'south': (0, 0)})
    plate(p, [x0, y0, z0], [x0 + .4, y1, z1], 'w', {'east': (0, 0), 'west': (0, 0)})
    plate(p, [x1 - .4, y0, z0], [x1, y1, z1], 'w', {'east': (0, 0), 'west': (0, 0)})
    for i, z in enumerate((5.4, 7.3, 9.2)):
        plate(p, [16 + i * .3, 10.7, z], [19.6 + i * .2, 11.9, z + 1.3], 'p', {'up': (0, 0), 'north': (0, 3), 'south': (0, 3), 'east': (0, 8), 'west': (0, 8)})
    bar(p, [14.4, 10.6, 7.5], [15.4, 11.2, 8.5], 'c')                        # the basket's bracket
    # The lamp under it, on the fork crown, and the dynamo on the fork, its wheel against the tyre.
    bar(p, [15, 7.6, 6.9], [16.9, 9.6, 9.1], 'c')
    plate(p, [16.9, 7.8, 7.1], [17.1, 9.4, 8.9], 'l', {'east': (0, 0), 'up': (0, 0), 'down': (0, 0), 'north': (0, 0), 'south': (0, 0)})
    bar(p, [14.2, 8.2, 7.4], [15, 9, 8.6], 'c')
    bar(p, [13.3, 8.6, 9.2], [14, 10.4, 9.9], 'c'); bar(p, [13.2, 8.2, 9.1], [14.1, 8.6, 10], 'f', GRIP)
    return b


def lamp_glow():
    """The Lamp's lens alone, lit: drawn full bright over the dark one while the dynamo turns."""
    parts = []
    plate(parts, [16.9, 7.8, 7.1], [17.15, 9.4, 8.9], 'l', {'east': (0, 0), 'up': (0, 0), 'down': (0, 0), 'north': (0, 0), 'south': (0, 0)})
    return parts


def rattler():
    b = Bike((1.5, 5.5), (14.5, 4.5), (7, 4), 12.9, 5.5, 4.5, 1.6, 1.6)
    b.thick_front, b.wide_front = 1.1, 1.0
    p = b.parts
    (bx, by) = b.back
    for z in ((6.8, 7.1), (8.9, 9.2)):
        tube(p, (bx, by), (7, by), 'f', FRAME, .9, z)
    tube(p, (7, 4), (7, 10.5), 'f', FRAME)
    tube(p, (1.75, 5.25), (6.75, 10.25), 'f', FRAME)
    tube(p, (7, 10), (12.5, 10), 'f', FRAME)
    tube(p, (7, 4.5), (12.2, 9.7), 'f', FRAME)
    tube(p, (12.5, 9.5), (12.5, 12.4), 'f', FRAME)
    for z, tex in (((7.0, 7.3), 'c'), ((8.7, 9.0), 'c')):                     # a raked fork, and the springer's second leg ahead of it
        tube(p, (14.6, 4.5), (12.1, 10.5), tex, 7, .8, z)
    tube(p, (15.3, 5.6), (13.6, 9.7), 'c', 7, .5, (7.7, 8.3))
    bar(p, [12.1, 12, 4.6], [12.8, 12.7, 11.4], 'c')                          # high bars: the foot, the risers, the grips swept back
    for z in ((4.6, 5.3), (10.7, 11.4)):
        bar(p, [12.1, 12.7, z[0]], [12.8, 16.6, z[1]], 'c')
        bar(p, [10.4, 15.9, z[0]], [12.8, 16.6, z[1]], 'c')
        bar(p, [9.6, 15.8, z[0] - .1], [11, 16.7, z[1] + .1], 'f', GRIP)
        bar(p, [9.4, 14.2, z[0] + .2], [9.7, 15.8, z[1] - .2], 'f', SEAT)      # streamers from the grips
    plate(p, [1.4, 11.6, 6.4], [9.6, 12.9, 9.6], 's', {'up': (0, 0), 'north': (0, 0), 'south': (0, 0), 'east': (0, 0), 'west': (0, 0), 'down': (0, 0)})
    for z in ((6.3, 6.8), (9.2, 9.7)):                                        # the sissy bar behind the seat
        bar(p, [1.2, 5.5, z[0]], [1.8, 16.6, z[1]], 'c')
    bar(p, [1.2, 16.1, 6.3], [1.8, 16.6, 9.7], 'c')
    tube(p, (7, 10.5), (7, 11.6), 'c')
    bar(p, [2, 3.5, 9.25], [8, 5.6, 9.5], 'f', TRIM)                          # chain guard, clear of the chain stay beside it
    # The card, clothes-pegged to the seat stay, its end in the spokes.
    plate(p, [3.2, 5.2, 8.95], [5.2, 7.8, 9.1], 'd', {'north': (0, 0), 'south': (0, 0)}, {'angle': 22.5, 'axis': 'z', 'origin': [4.2, 6.5, 9]})
    bar(p, [4.4, 7.3, 8.8], [5.2, 8.5, 9.3], 'g')
    return b


BIKES = {'wasp': wasp, 'hare': hare, 'comet': comet, 'hopper': hopper, 'lamp': lamp, 'rattler': rattler}


def bike_textures(make):
    return {'f': make, 't': 'tyre_pale' if make == 'hopper' else 'tyre', 'c': 'chrome', 'k': 'tyre', 'w': 'wicker', 'p': 'paper',
            'l': 'glass', 's': 'snake', 'd': 'card', 'g': 'peg', 'n': 'number'}


def used(parts, names):
    """Only the textures a model's pieces name."""
    keys = {f['texture'][1:] for e in parts for f in e['faces'].values()}
    return {k: v for k, v in names.items() if k in keys}


def model(parts, names, particle):
    return {'ambientocclusion': False, 'render_type': 'minecraft:cutout',
            'textures': {**{k: P + v for k, v in used(parts, names).items()}, 'particle': P + particle}, 'elements': parts}


def wheels(b, which):
    parts = []
    if which == 'back':
        wheel(parts, 8, 8, b.r_back, b.thick, b.wide, b.mag)
    else:
        wheel(parts, 8, 8, b.r_front, getattr(b, 'thick_front', b.thick), getattr(b, 'wide_front', b.wide), b.mag)
    return parts


def whole(b):
    """The bike as it stands: frame, both wheels where they go, and what a parked bike has."""
    parts = list(b.parts)
    wheel(parts, b.back[0], b.back[1], b.r_back, b.thick, b.wide, b.mag)
    wheel(parts, b.front[0], b.front[1], b.r_front, getattr(b, 'thick_front', b.thick), getattr(b, 'wide_front', b.wide), b.mag)
    standing(parts, b.axle)
    return parts


def lying(parts):
    """The same bike dropped on its side: its plane laid flat a little off the ground, the near grip and pedal down."""
    out = []
    for e in parts:
        (x0, y0, z0), (x1, y1, z1) = e['from'], e['to']
        f = {'from': [x0, round(z0 - 5.5, 3), round(16 - y1, 3)], 'to': [x1, round(z1 - 5.5, 3), round(16 - y0, 3)], 'faces': e['faces']}
        if 'rotation' in e:
            r = e['rotation']; ox, oy, oz = r['origin']
            f['rotation'] = {'angle': r['angle'], 'axis': {'x': 'x', 'y': 'z', 'z': 'y'}[r['axis']], 'origin': [ox, round(oz - 5.5, 3), round(16 - oy, 3)]}
        out.append(f)
    return out


# ---------------------------------------------------------------------------------------------------------------- the rack and the ramps
def rack():
    """One place in the rack, seen from the side the bikes come to it (they stand to the south, pointing north): two hoops for a front
    wheel to go between, on two rails that run on into the places beside it."""
    parts = []
    for y in (.4, 6.6):                                                       # the rails, a hair inside the hoops they run through
        plate(parts, [0, y, 11.5 + SLIP], [16, y + 1 - SLIP, 12.5 - SLIP], 'r', {'north': (0, 3), 'south': (0, 3), 'up': (0, 8), 'down': (0, 8), 'east': (3, 3), 'west': (3, 3)})
    for x in (5.6, 9.4):
        plate(parts, [x, 0, 11.5], [x + 1, 7.6, 12.5], 'r', {'north': (4, 0), 'south': (4, 0), 'east': (8, 0), 'west': (8, 0), 'up': (4, 0)})
        plate(parts, [x, 0, 15.5], [x + 1, 7.6, 16.5], 'r', {'north': (4, 0), 'south': (4, 0), 'east': (8, 0), 'west': (8, 0), 'up': (4, 0)})
        plate(parts, [x, 6.6, 12.5], [x + 1, 7.6, 15.5], 'r', {'up': (0, 0), 'down': (0, 0), 'east': (0, 8), 'west': (0, 8)})
        plate(parts, [x, 0, 12.5], [x + 1, 1, 15.5], 'r', {'up': (0, 0), 'down': (0, 0), 'east': (0, 8), 'west': (0, 8)})
    for x in (.5, 14.5):
        plate(parts, [x, 0, 11.5], [x + 1, .4, 12.5], 'r', {'north': (4, 0), 'south': (4, 0), 'east': (8, 0), 'west': (8, 0)})
    return parts


# A ramp rises to the north. Its slope is one a model's piece can lie at: 22.5 degrees for the long ramp's two halves, 45 for the kicker.
LONG = math.tan(math.radians(22.5))


def ramp(part):
    """A plank ramp, rising toward the north (z falling): plywood on a timber frame, open at the sides."""
    parts = []
    if part == 'steep':
        rise0, rise1, angle = 0, 16, 45
    else:
        rise0 = 0 if part == 'low' else 16 * LONG
        rise1 = rise0 + 16 * LONG
        angle = 22.5
    length = 16 / math.cos(math.radians(angle))
    # The sheet: a board along z laid down at the low end and turned up about that edge (its top face is the riding face).
    cy, cz = (rise0 + rise1) / 2, 8
    plate(parts, [.5, cy - 1, cz - length / 2], [15.5, cy, cz + length / 2], 'y', {'up': (0, 0), 'down': (0, 0), 'east': (0, 0), 'west': (0, 0), 'north': (0, 0), 'south': (0, 0)},
          {'angle': angle, 'axis': 'x', 'origin': [8, round(cy, 3), cz]})
    # Under it: a post at each corner of the high end, and a rail along each side, in steps the sheet hides.
    for x in (1, 14):
        steps = 8
        for i in range(steps):
            z1 = 16 - i * 2
            top = rise0 + (rise1 - rise0) * (i + .5) / steps - 1.2
            if top > .3:
                plate(parts, [x, 0, z1 - 2], [x + 1, round(top, 3), z1], 'm', {'east': (0, 0), 'west': (0, 0), 'north': (0, 0), 'south': (0, 0)})
    if rise1 > 6:
        plate(parts, [2, 0, 0], [14, round(rise1 - 2.4, 3), 1], 'm', {'north': (0, 0), 'south': (0, 0), 'up': (0, 0)})   # the back board, between the rails
    return parts


RAMP_TEXTURES = {'y': 'plywood', 'm': 'timber'}


def models():
    m = {}
    for make, build in BIKES.items():
        b = build(); names = bike_textures(make)
        m[f'{make}_frame'] = model(b.parts, names, make)
        m[f'{make}_wheel'] = model(wheels(b, 'front'), names, 'tyre')
        if make == 'rattler':
            m[f'{make}_wheel_back'] = model(wheels(b, 'back'), names, 'tyre')
        parts = whole(b)
        m[f'{make}_0'] = model(parts, names, make)
        m[f'{make}_1'] = model(lying(parts), names, make)
    m['cranks'] = model(cranks(), {'c': 'chrome', 'k': 'tyre'}, 'chrome')
    m['lamp_glow'] = model(lamp_glow(), {'l': 'glass_lit'}, 'glass_lit')
    m['rack'] = model(rack(), {'r': 'steel'}, 'steel')
    for part in ('low', 'high', 'steep'):
        m[f'ramp_{part}'] = model(ramp(part), RAMP_TEXTURES, 'plywood')
    return m


def geometry():
    """What the renderer needs to know of each bike, in the model's pixels: the hubs, the crank axle, the wheels' radii and the saddle."""
    out = {}
    for make, build in BIKES.items():
        b = build()
        out[make] = {'back': list(b.back), 'front': list(b.front), 'axle': list(b.axle), 'saddle': b.saddle, 'r_back': b.r_back, 'r_front': b.r_front}
    return out


def files():
    out = {}
    for key, obj in models().items():
        out[f'models/{DIR}{key}.json'] = obj
    variants = {}
    for make in MAKES:
        for facing, y in FACING_Y.items():
            for lying_ in (False, True):
                v = {'model': f'{P}{make}_{1 if lying_ else 0}'}
                if y:
                    v['y'] = y
                variants[f'facing={facing},lying={str(lying_).lower()},make={make}'] = v
    out['blockstates/night_bike.json'] = {'variants': variants}
    turn = {'north': 0, 'east': 90, 'south': 180, 'west': 270}
    out['blockstates/bike_rack.json'] = {'variants': {f'facing={f}': {'model': P + 'rack', **({'y': y} if y else {})} for f, y in turn.items()}}
    out['blockstates/bike_ramp.json'] = {'variants': {f'facing={f},part={part}': {'model': f'{P}ramp_{part}', **({'y': y} if y else {})}
                                                      for f, y in turn.items() for part in ('low', 'high', 'steep')}}
    out['blockstates/bike_beam.json'] = {'variants': {'': {'model': 'minecraft:block/air'}}}
    return out


def java_shapes():
    """The same numbers as NightBike.Make keeps for the game: the hubs, the axle, the radii and the saddle, in that order."""
    import re
    text = (ROOT / 'src/main/java/io/github/knaitoe/theoldesthouse/labyrinth/NightBike.java').read_text()
    out = {}
    for name, body in re.findall(r'^\s+([A-Z]+)\(new float\[\]\{([^}]*)\}', text, re.M):
        out[name.lower()] = [float(v.strip().rstrip('F')) for v in body.split(',')]
    return out


NAMES = {'block.the_oldest_house.night_bike': 'Bike', 'block.the_oldest_house.bike_rack': 'Bike rack', 'block.the_oldest_house.bike_ramp': 'Plank ramp',
         'block.the_oldest_house.bike_beam': 'Lamplight', 'entity.the_oldest_house.night_bike': 'Bike'}


# ---------------------------------------------------------------------------------------------------------------- review
def corners(e):
    (x0, y0, z0), (x1, y1, z1) = e['from'], e['to']
    pts = [[x, y, z] for x in (x0, x1) for y in (y0, y1) for z in (z0, z1)]
    r = e.get('rotation')
    if r:
        a = math.radians(r['angle']); ox, oy, oz = r['origin']; c, s = math.cos(a), math.sin(a)
        for p in pts:
            x, y, z = p[0] - ox, p[1] - oy, p[2] - oz
            if r['axis'] == 'z':
                x, y = x * c - y * s, x * s + y * c
            elif r['axis'] == 'x':
                y, z = y * c - z * s, y * s + z * c
            else:
                x, z = x * c + z * s, -x * s + z * c
            p[0], p[1], p[2] = x + ox, y + oy, z + oz
    return pts


def hull(points):
    points = sorted(set(points))
    if len(points) < 3:
        return points

    def cross(o, a, b):
        return (a[0] - o[0]) * (b[1] - o[1]) - (a[1] - o[1]) * (b[0] - o[0])
    lower, upper = [], []
    for p in points:
        while len(lower) >= 2 and cross(lower[-2], lower[-1], p) <= 0:
            lower.pop()
        lower.append(p)
    for p in reversed(points):
        while len(upper) >= 2 and cross(upper[-2], upper[-1], p) <= 0:
            upper.pop()
        upper.append(p)
    return lower[:-1] + upper[:-1]


def seen(parts, names, images, view, scale=12, box=(-8, 26, -2, 20), prefix=DIR):
    """A model straight on from one side, every piece flat in the colour at the middle of the face it shows: 'side' looks north at the
    bike's right side (front to the right), 'top' looks down, 'front' looks west at its front."""
    x0, x1, y0, y1 = box
    page = Image.new('RGBA', (int((x1 - x0) * scale), int((y1 - y0) * scale)), (58, 62, 70, 255)); draw = ImageDraw.Draw(page)
    face = {'side': 'south', 'top': 'up', 'front': 'east'}[view]
    order = []
    for e in parts:
        pts = corners(e)
        if view == 'side':
            flat, depth = [(p[0], p[1]) for p in pts], max(p[2] for p in pts)
        elif view == 'top':
            flat, depth = [(p[0], 16 - p[2] + 2) for p in pts], max(p[1] for p in pts)
        else:
            flat, depth = [(16 - p[2] + 1, p[1]) for p in pts], max(p[0] for p in pts)
        f = e['faces'].get(face) or next(iter(e['faces'].values()))
        tex = images[prefix + names[f['texture'][1:]] + '.png']
        u0, v0, u1, v1 = f.get('uv', [0, 0, 16, 16])
        c = tex.getpixel((min(15, max(0, int((u0 + u1) / 2))), min(15, max(0, int((v0 + v1) / 2)))))
        order.append((depth, hull([((x - x0) * scale, (y1 - y) * scale) for x, y in flat]), c))
    for _, poly, c in sorted(order, key=lambda o: o[0]):
        if len(poly) >= 3:
            draw.polygon(poly, fill=c)
    draw.line([(0, (y1 - 0) * scale), (page.width, (y1 - 0) * scale)], fill=(110, 116, 126, 255))
    return page


def review(path):
    images = textures(); scale = 12; rows = []
    for make, build in BIKES.items():
        b = build(); names = bike_textures(make); parts = whole(b)
        rows.append([seen(parts, names, images, 'side', scale), seen(parts, names, images, 'top', scale), seen(parts, names, images, 'front', scale),
                     seen(lying(parts), names, images, 'top', scale)])
    extra = [seen(rack(), {'r': 'steel'}, images, v, scale) for v in ('side', 'top', 'front')]
    rows.append(extra + [seen(ramp('steep'), RAMP_TEXTURES, images, 'front', scale)])
    rows.append([seen(ramp('low'), RAMP_TEXTURES, images, 'front', scale), seen(ramp('high'), RAMP_TEXTURES, images, 'front', scale),
                 seen(ramp('low'), RAMP_TEXTURES, images, 'top', scale), seen(ramp('steep'), RAMP_TEXTURES, images, 'side', scale)])
    w, h = rows[0][0].size
    page = Image.new('RGBA', (4 * (w + 6) + 6, len(rows) * (h + 6) + 6 + 40), (30, 32, 36, 255))
    for r, row in enumerate(rows):
        for c, one in enumerate(row):
            page.alpha_composite(one, (6 + c * (w + 6), 6 + r * (h + 6)))
    strip = Image.new('RGBA', (16 * len(images), 16), (0, 0, 0, 0))
    for i, (_, im) in enumerate(sorted(images.items())):
        strip.alpha_composite(im, (i * 16, 0))
    page.alpha_composite(strip.resize((strip.width * 2, 32), Image.NEAREST), (6, page.height - 36))
    page.save(path)


def main():
    args = argparse.ArgumentParser()
    args.add_argument('--check', action='store_true')
    args.add_argument('--sheet')
    args = args.parse_args()
    if args.sheet:
        review(args.sheet); return
    check = args.check
    images, data = textures(), files()
    shapes = java_shapes()
    for make, g in geometry().items():
        want = g['back'] + g['front'] + g['axle'] + [g['r_back'], g['r_front'], g['saddle']]
        assert shapes.get(make) == [float(v) for v in want], (make, shapes.get(make), want)
    for key, obj in data.items():
        if key.startswith('models/'):
            for tex in obj['textures'].values():
                assert tex.split(DIR)[1] + '.png' in {k.split(DIR)[1] for k in images}, (key, tex)
            for e in obj['elements']:
                for v in e['from'] + e['to']:
                    assert -16 <= v <= 32, (key, e['from'], e['to'])
            clash = fights(obj['elements'])
            assert not clash, (key, [(obj['elements'][a]['from'], obj['elements'][a]['to'], fa, obj['elements'][b]['from'], obj['elements'][b]['to'], fb, n)
                                     for a, fa, b, fb, n in clash[:4]])
    for rel, im in images.items():
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
    for key, name in NAMES.items():
        if check:
            assert lang.get(key) == name, key
        else:
            lang[key] = name
    if not check:
        lang_path.write_text(json.dumps(lang, indent=2, ensure_ascii=False) + '\n')
    print(('Verified' if check else 'Drew') + f" Bike night's bikes: {len(images)} textures, {len(data)} models and states.")


if __name__ == '__main__':
    main()
