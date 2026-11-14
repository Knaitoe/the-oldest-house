#!/usr/bin/env python3
"""The Minotaur, built of many parts, and its skin (0.4.584).

The owner, 11 November: "The minotaur texture should be improved as the Sphinx's was - use multiple blocks for its body to
give it more granular detail. its size can increase."

Until now it was thirty-eight boxes: a chest, a waist, a skull, two arms of three boxes and two legs of three. It is now
about a hundred and eighty, a fifth taller, each with its own piece of one skin (256 by 128) at one texel to a model pixel.
What 0.4.199 settled with the owner is kept part for part: the eyes ("It's not necessarily a monster"), the scarred bare
chest, furred upper arms, bare forearms with a raised vein, dark hands with pale claws, shaggy thighs and dark shins.

- each leg a hip, a thigh with the hamstring behind it, a kneecap, a gaskin and calf, a hock standing out behind, a cannon
  bone with the fetlock's hair round it, a pastern, two toes of a cloven hoof and two dewclaws, set as a bull's hind leg is;
- the body a pelvis under a rope belt and a rag front and back (each hangs plumb when the body bends), a waist with six muscles of the belly and the obliques
  beside them, a chest with two breasts (three old scars across them), the ribs, the lats, the yoke of the shoulders, and
  over it the hump in three masses with the mane hanging off it in locks;
- a neck with its dewlap and mane, and the head: skull, poll and forelock, brows, eyes, cheeks, the bridge of the nose, a
  pale snout with a dark wet nose and its nostrils, teeth, ears that hang, horns of seven lengths that go out, up and
  forward, and a jaw with its own teeth, a tongue and a beard;
- each arm a shoulder under a mantle of mane, a biceps and a triceps, an elbow, a forearm thick below the elbow and slimmer
  at the wrist, a palm, a thumb and four fingers of two joints, each with its claw;
- a tail of three lengths and a tuft.

This file is the one place the model is written down. It draws the skin (textures/entity/minotaur.png) and writes the
model's parts into client/MinotaurModel.java between two marked lines, so the boxes and the pieces of the picture they take
cannot drift apart. A lock of hair, the rag's hem and the tail's tuft end in a ragged edge: those texels are clear, and the
model is drawn with them cut out.

    python3 tools/draw_minotaur.py           # draw the skin and write the parts
    python3 tools/draw_minotaur.py --check   # verify both
"""
from pathlib import Path
import math
import sys
import zlib
from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / 'src/main/resources/assets/the_oldest_house/textures/entity/minotaur.png'
MODEL = ROOT / 'src/main/java/io/github/knaitoe/theoldesthouse/client/MinotaurModel.java'
WIDTH, HEIGHT = 256, 128
BEGIN = '        // <parts: written by tools/draw_minotaur.py; change them there>\n'
END = '        // </parts>\n'


# ---- The parts ----------------------------------------------------------------------------------------------------

class Box:
    def __init__(self, part, name, x, y, z, mat, hook=None, share=None, mirror=False, of=None):
        self.part, self.name, self.mat, self.hook, self.share, self.mirror, self.of = part, name, mat, hook, share, mirror, of
        # Model coordinates: x to the creature's left, y down, z to its back. Given here with y up, from the part's pivot.
        self.x, self.y, self.z = x[0], -y[1], z[0]
        self.w, self.h, self.d = (round(v[1] - v[0], 6) for v in (x, y, z))
        for size in (self.w, self.h, self.d):
            assert size == int(size) and size >= 1, (part.name, name, 'a box is a whole number of pixels each way')
        self.w, self.h, self.d = int(self.w), int(self.h), int(self.d)
        self.u = self.v = None

    @property
    def key(self):
        return self.share or self.part.name + '.' + self.name


class Part:
    def __init__(self, name, at=(0, 0, 0), rot=(0, 0, 0)):
        # at: from the parent's pivot, (x, up, z). rot: radians about x, y and z, as the game turns a part.
        self.name, self.at, self.rot, self.boxes, self.kids = name, at, rot, [], []

    def box(self, name, x, y, z, mat, sym=False, hook=None, share=None):
        made = Box(self, name, x, y, z, mat, hook, share)
        self.boxes.append(made)
        if sym:  # the same box on the other side of a part that lies on the midline, taking the same piece mirrored
            self.boxes.append(Box(self, name + '_l', (-x[1], -x[0]), y, z, mat, hook, share, mirror=True, of=made))
        return made

    def part(self, name, at, rot=(0, 0, 0)):
        kid = Part(name, at, rot)
        self.kids.append(kid)
        return kid

    def walk(self):
        yield self
        for kid in self.kids:
            yield from kid.walk()


def other_side(part):
    """A right-hand part and everything under it, made again on the left, taking the same pieces mirrored."""
    made = Part(part.name.replace('right_', 'left_'), (-part.at[0], part.at[1], part.at[2]), (part.rot[0], -part.rot[1], -part.rot[2]))
    for b in part.boxes:
        copy = Box(made, b.name, (-(b.x + b.w), -b.x), (-(b.y + b.h), -b.y), (b.z, b.z + b.d), b.mat, b.hook, b.share,
                   mirror=not b.mirror, of=b.of or b)
        made.boxes.append(copy)
    made.kids = [other_side(kid) for kid in part.kids]
    return made


def build():
    root = Part('root')
    hip = 25.0

    # The body turns about the hips.
    body = root.part('body', (0, hip, 1))
    b = body.box
    b('pelvis', (-8, 8), (-3, 4), (-4, 5), 'hide')
    b('belt_front', (-9, 9), (2, 4), (-5, -4), 'rope')
    b('belt_back', (-9, 9), (2, 4), (5, 6), 'rope')
    b('belt_side', (-9, -8), (2, 4), (-4, 5), 'rope', sym=True)
    b('knot', (3, 6), (1, 4), (-6, -5), 'rope', hook=knot)
    b('knot_end', (4, 5), (-3, 1), (-5.8, -4.8), 'rope', hook=lock)
    b('waist', (-7, 7), (4, 10), (-4, 5), 'bare')
    b('loin_fur', (-6, 6), (4, 11), (5, 6), 'fur_mid')
    for row, y0 in enumerate((4.25, 6.5, 8.75)):
        b(f'belly{row}', (-5, -1), (y0, y0 + 2), (-5, -4), 'bare', sym=True, hook=belly, share='belly')
    b('oblique', (-8, -7), (4, 9), (-3, 3), 'bare', sym=True)
    b('belly_hair', (-1, 1), (3, 10), (-4.6, -3.6), 'fur_dark', hook=lock)
    b('chest', (-11, 11), (10, 22), (-7, 7), 'bare', hook=chest)
    b('breast_r', (-10, -1), (14, 21), (-9, -7), 'bare', hook=breast_right)
    b('breast_l', (1, 10), (14, 21), (-9, -7), 'bare', hook=breast_left)
    for row, y0 in enumerate((10.5, 12.75)):
        b(f'rib{row}', (-12, -11), (y0, y0 + 2), (-6, -2), 'bare', sym=True, share='rib')
    b('lat', (-12, -9), (12, 20), (-1, 6), 'hide', sym=True)
    b('back_fur', (-10, 10), (10, 21), (7, 8), 'fur_mid', hook=spine)
    b('yoke', (-12, 12), (20, 24), (-6, 7), 'hide')
    b('collar', (-10, -2), (22, 23), (-7, -6), 'bare', sym=True)
    b('trap', (-9, -4), (23, 26), (-3, 4), 'hide', sym=True)
    b('hump_low', (-9, 9), (19, 28), (2, 10), 'fur_dark')
    b('hump_mid', (-7, 7), (25, 31), (0, 8), 'fur_dark')
    b('hump_top', (-4, 4), (30, 33), (1, 7), 'fur_dark')
    # The mane hangs off the hump in locks.
    for k, (x0, y0, y1) in enumerate(((-8, 19, 26), (-5, 17, 25), (-1, 18, 26), (2, 16, 25), (6, 19, 27))):
        b(f'mane_back{k}', (x0, x0 + 3), (y0, y1), (10, 11), 'fur_dark', hook=lock)
    b('mane_side', (-10, -9), (20, 27), (3, 9), 'fur_dark', sym=True, hook=lock)
    b('mane_crest0', (-2, 0), (33, 35), (2, 4), 'fur_dark')
    b('mane_crest1', (0, 2), (33, 34), (3, 5), 'fur_dark')
    b('mantle', (-12, -8), (24, 26), (-2, 6), 'fur_dark', sym=True)

    # The loincloth is two rags that hang from the belt, each a part of its own so that it can hang plumb when the body bends.
    body.part('rag_front', (0, 2, -5.1)).box('rag', (-4, 4), (-11, 0), (-.5, .5), 'cloth', hook=rag)
    body.part('rag_back', (0, 2, 6.1)).box('rag', (-4, 4), (-10, 0), (-.5, .5), 'cloth', hook=rag)

    # A bull carries its head low, against the chest and in front of the hump: almost no neck shows.
    neck = body.part('neck', (0, 19, -2), (0.25, 0, 0))
    n = neck.box
    n('neck', (-6, 6), (-1, 5), (-6, 4), 'hide')
    n('dewlap', (-2, 2), (-7, 0), (-9, -7), 'bare')
    n('neck_mane', (-5, 5), (-1, 6), (4, 6), 'fur_dark', hook=lock)
    n('neck_lock', (-7, -6), (-1, 4), (-2, 3), 'fur_dark', sym=True, hook=lock)

    head = neck.part('head', (0, 4, -1))
    h = head.box
    h('skull', (-7, 7), (-3, 10), (-9, 2), 'hide', hook=skull)
    h('poll', (-5, 5), (9, 12), (-8, 0), 'fur_dark', hook=curls)
    h('forelock0', (-3, -1), (6, 9), (-10, -9), 'fur_dark', hook=lock)
    h('forelock1', (-1, 2), (5, 9), (-10.4, -9.4), 'fur_dark', hook=lock)
    h('forelock2', (2, 4), (7, 9), (-10, -9), 'fur_dark', hook=lock)
    h('brow', (-8, -2), (6, 8), (-10, -8), 'hide_dark', sym=True)
    h('eye', (-7, -3), (3, 6), (-9.4, -8.4), 'hide_dark', sym=True, hook=eye)
    h('cheek', (-8, -7), (-3, 4), (-8, -2), 'hide', sym=True)
    h('bridge', (-3, 3), (3, 6), (-14, -9), 'hide', hook=bridge)
    h('snout', (-5, 5), (-3, 3), (-16, -9), 'muzzle', hook=snout)
    h('nose', (-4, 4), (-1, 3), (-17, -16), 'nose', hook=nose)
    h('teeth_up', (-4, 4), (-4, -3), (-15.5, -10.5), 'teeth', hook=teeth)

    ear = head.part('right_ear', (-7, 5, 0), (0, 0.25, -0.3))
    ear.box('ear', (-6, 0), (-1, 2), (-1, 1), 'hide', hook=ear_inside)
    ear.box('ear_tip', (-8, -6), (0, 2), (-1, 1), 'hide_dark')

    # A horn goes out, then up and forward: seven lengths, each turned a little from the last.
    horn = head.part('right_horn', (-6, 9, -2), (0.5, 0, -1.2))
    horn.box('ring', (-2.5, 2.5), (-0.5, 0.5), (-2.5, 2.5), 'horn', hook=horn_at(0, 0))
    length, at = horn, 0.0
    spans = ((4, 4), (4, 4), (3, 4), (3, 4), (2, 3), (2, 3), (1, 3))
    whole = sum(l for _, l in spans)
    for k, (t, l) in enumerate(spans):
        if k:
            length = length.part(f'right_horn{k}', (0, spans[k - 1][1], 0), (0.12, 0, 0.24))
        length.box('horn', (-t / 2, t / 2), (0, l), (-t / 2, t / 2), 'horn', hook=horn_at(at / whole, (at + l) / whole))
        at += l

    jaw = head.part('jaw', (0, -3, -5), (0.05, 0, 0))
    j = jaw.box
    j('jaw', (-4, 4), (-3, 0), (-11, 3), 'muzzle', hook=jaw_bone)
    j('chin', (-3, 3), (-4, -3), (-10, -5), 'muzzle')
    j('incisors', (-3, 3), (0, 1), (-10.5, -9.5), 'teeth', hook=teeth)
    j('molars', (-4, -3), (0, 1), (-9, -3), 'teeth', sym=True, hook=teeth)
    j('tongue', (-2, 2), (0, 1), (-9, -2), 'tongue')
    j('beard', (-2, 2), (-7, -4), (-9, -6), 'fur_dark', hook=lock)
    j('beard_tip', (-1, 1), (-9, -7), (-8, -7), 'fur_dark', hook=lock)

    arm = body.part('right_arm', (-13, 20, 0), (0, 0, 0.12))
    a = arm.box
    a('deltoid', (-5, 4), (-5, 4), (-5, 5), 'hide')
    a('shoulder_fur', (-5, 3), (4, 6), (-3, 5), 'fur_dark')
    a('shoulder_lock', (-6, -5), (-2, 4), (-2, 4), 'fur_dark', hook=lock)
    a('biceps', (-4, 3), (-12, -5), (-5, 0), 'hide', hook=biceps)
    a('triceps', (-4, 3), (-12, -5), (0, 4), 'hide')
    a('elbow', (-3, 2), (-14, -11), (2, 5), 'hide_dark')
    fore = arm.part('right_forearm', (-0.5, -12, 0), (-0.3, 0, 0))
    f = fore.box
    f('forearm_up', (-4, 4), (-6, 0), (-4, 4), 'bare', hook=vein)
    f('forearm_low', (-3, 3), (-11, -6), (-3, 3), 'bare', hook=wrist)
    f('ridge', (-5, -4), (-7, -1), (-2, 2), 'bare')
    f('forearm_fur', (-2, 2), (-5, -1), (4, 5), 'fur_dark', hook=lock)
    hand = fore.part('right_hand', (0, -11, 0), (0.1, 0, 0))
    hd = hand.box
    hd('palm', (-4, 4), (-5, 0), (-3, 3), 'hand', hook=knuckles)
    hd('thumb', (4, 6), (-4, -1), (-3, 0), 'hand')
    hd('thumb_tip', (4.5, 6.5), (-7, -4), (-3, -1), 'hand')
    hd('thumb_claw', (5, 6), (-9, -7), (-2.5, -1.5), 'claw', share='claw')
    fingers = hand.part('right_fingers', (0, -5, 0), (0.25, 0, 0))
    tips = fingers.part('right_fingertips', (0, -3, -0.5), (0.35, 0, 0))
    for k in range(4):
        x0 = -4 + 2 * k
        fingers.box(f'finger{k}', (x0, x0 + 2), (-3, 0), (-2, 1), 'hand', hook=finger, share='finger')
        tips.box(f'tip{k}', (x0, x0 + 2), (-3, 0), (-1, 1), 'hand', hook=finger, share='fingertip')
        tips.box(f'claw{k}', (x0 + .5, x0 + 1.5), (-5, -3), (-.5, .5), 'claw', share='claw')

    tail = body.part('tail', (0, 1, 5), (0.6, 0, 0))
    tail.box('tail', (-1.5, 1.5), (-6, 0), (-1.5, 1.5), 'hide')
    tail2 = tail.part('tail2', (0, -6, 0), (-0.25, 0, 0))
    tail2.box('tail', (-1, 1), (-6, 0), (-1, 1), 'hide')
    tail3 = tail2.part('tail3', (0, -6, 0), (-0.2, 0, 0))
    tail3.box('tail', (-1, 1), (-5, 0), (-1, 1), 'hide_dark')
    tuft = tail3.part('tail_tuft', (0, -5, 0), (-0.1, 0, 0))
    tuft.box('tuft', (-2, 2), (-4, 1), (-2, 2), 'fur_dark', hook=lock)
    tuft.box('tuft_end', (-1, 1), (-8, -4), (-1, 1), 'fur_dark', hook=lock)

    # A leg swings from the hip. Under it a bull's hind leg: the thigh forward, the gaskin back to the hock, the cannon
    # down to a hoof that stands level.
    leg = root.part('right_leg', (-6, hip, 1))
    leg.box('hip', (-5, 4), (-2, 2), (-4, 5), 'hide')
    thigh = leg.part('right_thigh', (0, 0, 0), (-0.35, 0, 0))
    thigh.box('quad', (-5, 4), (-9, -1), (-5, 1), 'hide', hook=quad)
    thigh.box('ham', (-4, 3), (-9, -1), (1, 5), 'hide_dark')
    thigh.box('knee', (-4, 3), (-11, -8), (-6, -2), 'hide')
    shin = thigh.part('right_shin', (0, -9, -1), (0.9, 0, 0))
    shin.box('gaskin', (-4, 3), (-8, 0), (-3, 3), 'hide_dark')
    shin.box('calf', (-3, 2), (-5, -1), (3, 5), 'hide_dark')
    shin.box('hock', (-3, 2), (-9, -7), (1, 4), 'hide_dark')
    cannon = shin.part('right_cannon', (0, -8, 0), (-0.65, 0, 0))
    cannon.box('cannon', (-3, 2), (-6, 0), (-2.5, 2.5), 'hide_dark')
    cannon.box('fetlock_back', (-3, 2), (-7, -4), (2.5, 4.5), 'fur_dark', hook=lock)
    cannon.box('fetlock_side', (-4, -3), (-7, -4), (-2.5, 2.5), 'fur_dark', hook=lock)
    cannon.box('fetlock_in', (2, 3), (-7, -4), (-2.5, 2.5), 'fur_dark', hook=lock, share='right_cannon.fetlock_side')
    hoof = cannon.part('right_hoof', (-0.5, -6, 0), (0.1, 0, 0))
    hoof.box('pastern', (-2.5, 2.5), (-2, 0), (-3, 2), 'hide_dark')
    hoof.box('toe_out', (-3.25, -0.25), (-5, -2), (-5, 1), 'hoof', hook=hoof_wall, share='toe')
    hoof.box('toe_in', (0.25, 3.25), (-5, -2), (-5, 1), 'hoof', hook=hoof_wall, share='toe')
    hoof.box('dewclaw_out', (-2.5, -1.5), (-3, -2), (2, 3), 'hoof', share='dewclaw')
    hoof.box('dewclaw_in', (1.5, 2.5), (-3, -2), (2, 3), 'hoof', share='dewclaw')

    for kids in (head.kids, body.kids, root.kids):
        for part in [p for p in kids if p.name.startswith('right_')]:
            kids.append(other_side(part))

    # Stand it on the ground: the hips go up or down until the lowest corner of a hoof is at zero.
    low = min(y for part, m in poses(root) if part.name.endswith('hoof') for y in corners_up(part, m))
    for part in root.kids:
        part.at = (part.at[0], part.at[1] - low, part.at[2])
    return root


# ---- Where a part is ----------------------------------------------------------------------------------------------

def mul(a, b):
    return [[sum(a[i][k] * b[k][j] for k in range(4)) for j in range(4)] for i in range(4)]


def pose_matrix(part):
    """The game's own: move to the pivot, then turn about z, then y, then x."""
    x, y, z = part.at[0], -part.at[1], part.at[2]
    rx, ry, rz = part.rot
    cx, sx, cy, sy, cz, sz = math.cos(rx), math.sin(rx), math.cos(ry), math.sin(ry), math.cos(rz), math.sin(rz)
    turn_x = [[1, 0, 0, 0], [0, cx, -sx, 0], [0, sx, cx, 0], [0, 0, 0, 1]]
    turn_y = [[cy, 0, sy, 0], [0, 1, 0, 0], [-sy, 0, cy, 0], [0, 0, 0, 1]]
    turn_z = [[cz, -sz, 0, 0], [sz, cz, 0, 0], [0, 0, 1, 0], [0, 0, 0, 1]]
    move = [[1, 0, 0, x], [0, 1, 0, y], [0, 0, 1, z], [0, 0, 0, 1]]
    return mul(move, mul(turn_z, mul(turn_y, turn_x)))


def poses(part, parent=None):
    here = pose_matrix(part) if parent is None else mul(parent, pose_matrix(part))
    yield part, here
    for kid in part.kids:
        yield from poses(kid, here)


def corners_up(part, m):
    """How high each corner of a part's boxes stands (model y is down)."""
    for b in part.boxes:
        for x in (b.x, b.x + b.w):
            for y in (b.y, b.y + b.h):
                for z in (b.z, b.z + b.d):
                    yield -(m[1][0] * x + m[1][1] * y + m[1][2] * z + m[1][3])


# ---- The skin: where each box's piece is --------------------------------------------------------------------------

def pack(root):
    """Each box has a piece (2w+2d) by (d+h); boxes that share one are the same size. Shelves, tallest first."""
    pieces = {}
    for part in root.walk():
        for b in part.boxes:
            if b.of is not None:
                continue
            size = (2 * (b.w + b.d), b.d + b.h)
            assert pieces.setdefault(b.key, size) == size, (b.key, 'shared by boxes of different sizes')
    x = y = shelf = 0
    place = {}
    for key, (w, h) in sorted(pieces.items(), key=lambda kv: (-kv[1][1], -kv[1][0], kv[0])):
        if x + w > WIDTH:
            x, y, shelf = 0, y + shelf, 0
        place[key] = (x, y)
        x, shelf = x + w, max(shelf, h)
    assert y + shelf <= HEIGHT, f'the skin needs {y + shelf} rows'
    for part in root.walk():
        for b in part.boxes:
            b.u, b.v = place[(b.of or b).key]
    return y + shelf


def faces(b):
    """A box's six faces on the skin as (x, y, width, height), laid out as the game lays a box out."""
    u, v, w, h, d = b.u, b.v, b.w, b.h, b.d
    return {'top': (u + d, v, w, d), 'bottom': (u + d + w, v, w, d), 'right': (u, v + d, d, h),
            'front': (u + d, v + d, w, h), 'left': (u + d + w, v + d, d, h), 'back': (u + 2 * d + w, v + d, w, h)}


# ---- The skin: its colours ----------------------------------------------------------------------------------------

# Each material is six tones, dark to light. A texel's tone is the material's own, a little lighter on a face that looks
# up and darker on one that looks down or back, lighter toward the top of a side and darker along its foot and edges, with
# a grain of at most one tone. Few colours, set down on purpose, as the game's own animals are.
RAMPS = {
    'hide': ((52, 36, 26), (68, 47, 33), (84, 59, 42), (98, 70, 50), (112, 82, 59), (126, 94, 69)),
    'hide_dark': ((38, 26, 20), (50, 35, 26), (62, 44, 32), (76, 54, 39), (90, 65, 47), (104, 76, 56)),
    'bare': ((88, 62, 46), (104, 75, 56), (119, 88, 66), (133, 100, 76), (146, 112, 87), (158, 124, 98)),
    'fur_dark': ((26, 18, 14), (36, 25, 19), (47, 32, 24), (58, 40, 30), (70, 49, 36), (82, 58, 43)),
    'fur_mid': ((44, 31, 23), (56, 39, 29), (68, 48, 35), (80, 57, 42), (92, 66, 49), (104, 76, 57)),
    'muzzle': ((104, 84, 70), (120, 98, 82), (134, 111, 93), (146, 122, 103), (158, 134, 114), (170, 146, 126)),
    'nose': ((30, 24, 23), (42, 34, 32), (54, 44, 42), (68, 56, 53), (84, 70, 66), (104, 90, 86)),
    'hoof': ((20, 17, 16), (28, 23, 21), (36, 30, 27), (46, 38, 34), (60, 50, 44), (78, 66, 58)),
    'hand': ((28, 21, 18), (38, 28, 23), (48, 36, 29), (59, 44, 35), (71, 53, 42), (84, 63, 50)),
    'claw': ((118, 106, 86), (144, 131, 108), (168, 155, 130), (188, 176, 150), (205, 194, 169), (220, 210, 188)),
    'cloth': ((50, 43, 36), (62, 53, 44), (74, 63, 52), (86, 74, 61), (98, 85, 71), (110, 96, 81)),
    'rope': ((40, 30, 22), (54, 40, 30), (68, 51, 38), (82, 62, 46), (98, 75, 56), (114, 89, 67)),
    'teeth': ((168, 156, 130), (188, 177, 150), (204, 194, 168), (218, 209, 185), (230, 222, 200), (240, 234, 216)),
    'tongue': ((110, 52, 56), (128, 62, 66), (144, 74, 78), (158, 86, 90), (170, 98, 102), (182, 110, 114)),
    'horn': ((52, 43, 35), (92, 80, 64), (136, 122, 98), (176, 162, 134), (206, 192, 160), (228, 214, 182)),
}
# base tone, how much lighter the top of a side is than its foot, grain, and how often a hair's shadow falls
HOW = {'hide': (2.6, .9, .9, .16), 'hide_dark': (2.6, .9, .9, .16), 'bare': (2.7, 1.2, .5, 0), 'fur_dark': (2.4, .8, 1.0, .3),
       'fur_mid': (2.5, .8, 1.0, .3), 'muzzle': (2.8, 1.0, .5, 0), 'nose': (2.2, .8, .4, 0), 'hoof': (2.2, 1.2, .5, 0),
       'hand': (2.5, 1.0, .5, 0), 'claw': (3.0, 1.4, .3, 0), 'cloth': (2.5, .8, .6, 0), 'rope': (2.6, .6, .4, 0),
       'teeth': (3.0, 1.0, .3, 0), 'tongue': (2.6, .6, .4, 0), 'horn': (4.0, 0, .4, 0)}
LIGHT = {'top': 1.0, 'front': .3, 'right': -.3, 'left': -.3, 'back': -.6, 'bottom': -1.7}
SIDES = ('front', 'back', 'right', 'left')
SCAR = (176, 122, 104)
MOUTH = (74, 24, 26)
CLEAR = (0, 0, 0, 0)


def chance(*key):
    """A number from 0 to 1 that is always the same for the same key."""
    x = 0x9E3779B9
    for v in key:
        x = ((x ^ (int(v) & 0xFFFFFFFF)) * 0x85EBCA6B) & 0xFFFFFFFF
        x ^= x >> 13
    x = (x * 0xC2B2AE35) & 0xFFFFFFFF
    return (x ^ (x >> 16)) / 4294967296


class Texel:
    """One texel of one face of one box, as a painter sees it: (x, y) from the face's top left."""

    def __init__(self, b, face, x, y, w, h):
        self.b, self.face, self.x, self.y, self.w, self.h = b, face, x, y, w, h
        self.seed = zlib.crc32((b.key + face).encode())

    def level(self, mat=None):
        base, grad, grain, hair = HOW[mat or self.b.mat]
        lv = base + LIGHT[self.face]
        if self.face in SIDES:
            if self.h > 1:
                lv += grad * (.5 - (self.y + .5) / self.h)
            if self.w >= 4 and self.x in (0, self.w - 1):
                lv -= .5
            if self.h >= 4 and self.y == self.h - 1:
                lv -= .5
        lv += (chance(self.seed, self.x, self.y) - .5) * grain
        if hair and self.face != 'bottom' and chance(self.seed, self.x, (self.y + self.x * 2) // 3, 7) < hair:
            lv -= 1
        return lv

    def tone(self, lv=None, mat=None):
        ramp = RAMPS[mat or self.b.mat]
        lv = self.level(mat) if lv is None else lv
        return ramp[max(0, min(len(ramp) - 1, round(lv)))] + (255,)

    def mid(self):
        """How far this texel is from the middle of the face, in texels, left negative."""
        return self.x - (self.w - 1) / 2


def opaque(c):
    return tuple(c[:3]) + (255,)


# What a hook returns stands; None leaves the material's own tone.

def lock(t):
    """Hair that ends: the foot of each side is ragged, and nothing is drawn underneath."""
    if t.face == 'bottom':
        return CLEAR
    if t.face in SIDES:
        gone = 1 + (chance(t.seed, t.x, 3) < .5) + (t.h >= 6 and chance(t.seed, t.x, 5) < .35)
        if t.y >= t.h - gone:
            return CLEAR


def rag(t):
    """The cloth front and back: folds down it, and a hem worn to threads."""
    if t.face == 'bottom':
        return CLEAR
    if t.face in ('front', 'back'):
        if t.y >= t.h - 1 - (chance(t.seed, t.x, 1) < .45) - (chance(t.seed, t.x, 2) < .2):
            return CLEAR
        return t.tone(t.level() - (1 if t.x % 4 == 1 else 0) + (.6 if t.x % 4 == 2 else 0))


def belly(t):
    if t.face == 'front':
        return t.tone(t.level() + (.7 if t.y == 0 else -.9 if t.y == t.h - 1 else 0))


def chest(t):
    if t.face == 'front' and t.y >= 7:  # under the breasts: the ribs' shadows, and the furrow down the middle
        return t.tone(t.level() - (.8 if abs(t.mid()) < 1 else 0) - (.6 if t.y in (8, 11) and abs(t.mid()) > 2 else 0))


def scars(t, lines):
    if t.face == 'front':
        for x0, y0, dx, n in lines:
            for k in range(n):
                if (t.x, t.y) == (x0 + dx * k, y0 + k):
                    return opaque(SCAR)
        return t.tone(t.level() + (.8 if t.y <= 1 else -1.0 if t.y == t.h - 1 else 0))


def breast_right(t):
    return scars(t, ((2, 1, 1, 4), (4, 1, 1, 4)))


def breast_left(t):
    return scars(t, ((6, 2, -1, 4),))


def spine(t):
    if t.face == 'back' and abs(t.mid()) < 1:
        return t.tone(t.level() - 1.2)


def curls(t):
    if t.face != 'bottom' and (t.x * 5 + t.y * 3) % 7 < 2:
        return t.tone(t.level() + 1.2)


def skull(t):
    if t.face == 'front':
        if t.y <= 2:  # under the forelock
            return t.tone(t.level('fur_dark'), 'fur_dark')
        if 4 <= t.y <= 7 and 1.5 < abs(t.mid()) < 5.5:  # the sockets
            return t.tone(t.level() - 1.6)
        if t.y >= 9:  # paler toward the mouth
            return t.tone(t.level('muzzle') - .8, 'muzzle')
    if t.face in ('right', 'left') and t.y <= 3:
        return t.tone(t.level('fur_dark'), 'fur_dark')
    if t.face == 'top':
        return t.tone(t.level('fur_dark'), 'fur_dark')


def eye(t):
    """Large and dark, a lashed lid over it, white at the outer corner and a light caught in the brown: a bull's eye, and
    human the way a cow's can look (the owner: "It's not necessarily a monster")."""
    if t.face != 'front':
        return None
    if t.y == 0:
        return (22, 15, 12, 255) if t.x >= 1 else t.tone(t.level() - 1)
    if t.x == 0:
        return (224, 214, 200, 255) if t.y == 1 else (188, 176, 162, 255)
    if (t.x, t.y) == (2, 1):
        return (238, 232, 222, 255)
    if (t.x, t.y) == (2, 2):
        return (34, 20, 14, 255)
    return (80, 50, 33, 255) if t.y == 1 else (58, 36, 24, 255)


def bridge(t):
    if t.face == 'top' and t.y >= t.h - 2:  # toward the nose the hide gives way to the pale of the snout
        return t.tone(t.level('muzzle'), 'muzzle')


def snout(t):
    if t.face in ('right', 'left', 'front') and t.y == t.h - 1:
        return (34, 24, 22, 255)  # the line of the mouth
    if t.face in ('right', 'left') and t.y == t.h - 2:
        return t.tone(t.level() - 1)
    if t.face == 'bottom':
        return opaque(MOUTH)


def nose(t):
    if t.face == 'front':
        if t.y in (1, 2) and abs(t.mid()) in (1.5, 2.5):
            return (18, 13, 13, 255)  # the nostrils
        if t.y == 0 and abs(t.mid()) < 2:
            return t.tone(t.level() + 2)  # wet


def teeth(t):
    if t.face in SIDES and t.x % 2 == 1:
        return t.tone(t.level() - 1.5)


def ear_inside(t):
    if t.face == 'front' and 0 < t.y < t.h - 1 and t.x < t.w - 1:
        return t.tone(t.level('muzzle') - 1, 'muzzle')


def horn_at(start, end):
    """A length of horn: ivory at the root, yellowing, near black at the point, ringed. Each length grows from its foot."""
    def paint(t):
        if t.face in ('top', 'bottom'):
            along = end if t.face == 'top' else start
        else:
            along = start + (end - start) * (1 - (t.y + .5) / t.h)
        lv = 5.2 - 5.4 * along ** 1.4 + LIGHT[t.face] * .5 + (chance(t.seed, t.x, t.y) - .5) * .5
        if t.face in SIDES and t.y % 2 == 1:
            lv -= .7
        return t.tone(lv)
    return paint


def jaw_bone(t):
    if t.face == 'top':
        return opaque(MOUTH)
    if t.face in SIDES and t.y == 0:
        return (34, 24, 22, 255)


def biceps(t):
    if t.face == 'front':
        return t.tone(t.level() + (.8 if 1 <= t.x <= 4 and 1 <= t.y <= 4 else 0))


def vein(t):
    if t.face == 'front' and t.x == 3 + (t.y % 3 == 0):
        return t.tone(t.level() + 1.2)


def wrist(t):
    if t.face in SIDES and t.y >= t.h - 2:
        return t.tone(t.level('hand'), 'hand')


def knuckles(t):
    if t.face in ('front', 'back') and t.y == t.h - 2 and t.x % 2 == 0:
        return t.tone(t.level() + 1.2)


def finger(t):
    if t.face in ('front', 'back') and t.x == t.w - 1:
        return t.tone(t.level() - 1.4)  # the dark between two fingers


def knot(t):
    if t.face == 'front' and (t.x + t.y) % 2 == 0:
        return t.tone(t.level() - 1.2)


def quad(t):
    """The thigh's big muscle: lighter down its middle, a dark line where it meets the next."""
    if t.face == 'front':
        return t.tone(t.level() + (.7 if 2 <= t.x <= 5 and 1 <= t.y <= 6 else 0) - (.9 if t.x == 7 else 0))


def hoof_wall(t):
    if t.face in SIDES:
        if t.y == 0:
            return t.tone(t.level() + 2.2)  # the pale band where it grows
        if t.x % 3 == 2:
            return t.tone(t.level() - .7)


def draw(root):
    im = Image.new('RGBA', (WIDTH, HEIGHT), CLEAR)
    done = set()
    for part in root.walk():
        for b in part.boxes:
            if b.of is not None or b.key in done:
                continue
            done.add(b.key)
            for face, (x0, y0, w, h) in faces(b).items():
                for y in range(h):
                    for x in range(w):
                        t = Texel(b, face, x, y, w, h)
                        colour = b.hook(t) if b.hook else None
                        im.putpixel((x0 + x, y0 + y), colour if colour is not None else t.tone())
    return im


# ---- The parts, as the model declares them ------------------------------------------------------------------------

def num(v):
    v = round(v, 3)
    return str(int(v)) if v == int(v) else f'{v}F'


def java(root):
    lines = []

    def name(part):
        head, *rest = part.name.split('_')
        return head + ''.join(w.capitalize() for w in rest)

    def write(part, parent):
        cubes, mirrored = 'CubeListBuilder.create()', False
        for k, b in enumerate(part.boxes):
            if b.mirror != mirrored:
                mirrored = b.mirror
                cubes += '.mirror()' if mirrored else '.mirror(false)'
            if k and k % 3 == 0:
                cubes += '\n                '
            cubes += f'.texOffs({b.u},{b.v}).addBox({num(b.x)},{num(b.y)},{num(b.z)},{b.w},{b.h},{b.d})'
        x, y, z = part.at[0], -part.at[1], part.at[2]
        if part.name == 'root':
            y = 24
        pose = (f'PartPose.offsetAndRotation({num(x)},{num(y)},{num(z)},{num(part.rot[0])},{num(part.rot[1])},{num(part.rot[2])})'
                if any(part.rot) else f'PartPose.offset({num(x)},{num(y)},{num(z)})')
        keep = f'PartDefinition {name(part)}=' if part.kids else ''
        lines.append(f'        {keep}{parent}.addOrReplaceChild("{part.name}",{cubes},{pose});\n')
        for kid in part.kids:
            write(kid, name(part))

    write(root, 'mesh.getRoot()')
    return ''.join(lines)


def main():
    root = build()
    rows = pack(root)
    im = draw(root)
    code = java(root)
    source = MODEL.read_text(encoding='utf-8')
    a, b = source.index(BEGIN) + len(BEGIN), source.index(END)
    boxes = sum(len(p.boxes) for p in root.walk())
    parts = sum(1 for _ in root.walk())
    if sys.argv[1:] == ['--check']:
        assert Image.open(OUT).convert('RGBA').tobytes() == im.tobytes(), 'minotaur.png differs from tools/draw_minotaur.py'
        assert source[a:b] == code, 'MinotaurModel.java: the parts differ from tools/draw_minotaur.py'
        print(f'Verified the Minotaur: {boxes} boxes in {parts} parts; the skin uses {rows} of {HEIGHT} rows.')
        return
    im.save(OUT)
    MODEL.write_text(source[:a] + code + source[b:], encoding='utf-8')
    print(f'Drew the Minotaur: {boxes} boxes in {parts} parts; the skin uses {rows} of {HEIGHT} rows.')


if __name__ == '__main__':
    main()
