#!/usr/bin/env python3
"""The Winchester house's materials and fittings (0.4.121): Victorian damask wallpaper, the séance room's blue paper,
basket-weave parquet and leaded glass, as vanilla-style 16x16 textures with their block models and states; and the
winchester_fitting block's models: the séance table with its hand bell, a blue velvet side chair, the hall clock whose
dial has thirteen hours, and what the carpenters leave out: a sawhorse with a board half cut, a stack of new lumber, a keg
of nails with the hammer on it, and the brass 13 they fix beside doors.

Each texture is drawn pixel by pixel from a small palette with scattered noise, as vanilla's are; nothing is sampled.
Run with --check to confirm the committed files.
"""
import argparse
import json
import math
import random
import sys
from pathlib import Path

from PIL import Image

ROOT = Path(__file__).resolve().parents[1] / 'src/main/resources/assets/the_oldest_house'
TEX = ROOT / 'textures/block/winchester'
MODELS = ROOT / 'models/block/winchester'
STATES = ROOT / 'blockstates'
NS = 'the_oldest_house:block/winchester/'


def clamp(c):
    return tuple(max(0, min(255, round(v))) for v in c)


def mul(c, f):
    return clamp(tuple(v * f for v in c[:3]))


def canvas():
    return Image.new('RGBA', (16, 16), (0, 0, 0, 0))


def px(img, x, y, c, a=255):
    img.putpixel((x % 16, y % 16), clamp(c) + (a,))


def ground(c, seed, noise=.04):
    rng = random.Random(seed)
    img = canvas()
    for y in range(16):
        for x in range(16):
            px(img, x, y, mul(c, 1 + rng.uniform(-noise, noise)))
    return img


def bevel(img, x0, y0, x1, y1, light=1.14, dark=.76):
    for x in range(x0, x1 + 1):
        r, g, b, a = img.getpixel((x, y0)); px(img, x, y0, mul((r, g, b), light), a)
        r, g, b, a = img.getpixel((x, y1)); px(img, x, y1, mul((r, g, b), dark), a)
    for y in range(y0 + 1, y1):
        r, g, b, a = img.getpixel((x0, y)); px(img, x0, y, mul((r, g, b), light), a)
        r, g, b, a = img.getpixel((x1, y)); px(img, x1, y, mul((r, g, b), dark), a)


# A damask motif on an eight-pixel half-drop: a lozenge of leaves round a small bud.
MOTIF = ['...#....', '..###...', '.#.#.#..', '##.#.##.', '.#.#.#..', '..###...', '...#....', '........']


def damask(ground, figure, seed):
    rng = random.Random(seed)
    img = canvas()
    for y in range(16):
        for x in range(16):
            px(img, x, y, mul(ground, 1 + rng.uniform(-.025, .025)))
    for ox, oy in ((0, 0), (8, 8)):
        for j, row in enumerate(MOTIF):
            for i, cell in enumerate(row):
                if cell == '#':
                    px(img, ox + i, oy + j, mul(figure, 1 + rng.uniform(-.05, .05)))
    for x in range(16):
        r, g, b, _ = img.getpixel((x, 15)); px(img, x, 15, mul((r, g, b), .9))
    return img


def wallpaper():
    """Oxblood damask with an old-gold figure, faded a little where the light fell."""
    return damask((112, 30, 36), (188, 150, 82), 4101)


def seance_paper():
    """The séance room's paper: deep blue, the same damask in a paler, cold silver-blue."""
    return damask((34, 52, 104), (126, 146, 190), 4102)


def parquet():
    """Oak parquet in a basket weave: eight-pixel squares of two short planks, laid alternately across and along."""
    rng = random.Random(4103)
    img = canvas()
    oak = (150, 104, 60)
    tones = (1.0, .9, 1.06, .95)
    for y in range(16):
        for x in range(16):
            across = (x // 8 + y // 8) % 2 == 0
            plank = (y // 4) if across else (x // 4)
            joint = (y % 4 == 3) if across else (x % 4 == 3)
            end = (x % 8 == 7) if across else (y % 8 == 7)
            grain = ((x * 3 + plank) % 5 == 0) if across else ((y * 3 + plank) % 5 == 0)
            f = tones[plank % 4] + rng.uniform(-.03, .03) - (.08 if grain else 0)
            px(img, x, y, mul(oak, .55 if joint or end else f))
    return img


def leaded_glass():
    """Leaded glass: small diamond panes of pale green-grey glass in dark lead cames, one pane amber."""
    img = canvas()
    lead = (52, 50, 54)
    for y in range(16):
        for x in range(16):
            if (x + y) % 6 == 0 or (x - y) % 6 == 0 or x in (0, 15) or y in (0, 15):
                px(img, x, y, lead, 255)
            else:
                amber = 6 <= x <= 9 and 6 <= y <= 9
                px(img, x, y, (226, 176, 90) if amber else (196, 214, 200), 120 if amber else 84)
    return img


TEXTURES = {'wallpaper': wallpaper, 'seance_paper': seance_paper, 'parquet': parquet, 'leaded_glass': leaded_glass}
BLOCKS = {'winchester_wallpaper': 'wallpaper', 'winchester_seance_paper': 'seance_paper', 'winchester_parquet': 'parquet',
          'winchester_leaded_glass': 'leaded_glass'}

# ------------------------------------------------------------------------------------------------ fittings
def mahogany():
    """French-polished mahogany: deep red-brown with long dark grain lines and a few lighter flecks."""
    rng = random.Random(4110)
    img = ground((96, 42, 30), 4110, .03)
    for x in range(16):
        if rng.random() < .35:
            for y in range(16):
                if rng.random() < .8:
                    r, g, b, _ = img.getpixel((x, y)); px(img, x, y, mul((r, g, b), .82))
    for _ in range(6):
        px(img, rng.randrange(16), rng.randrange(16), (130, 64, 44))
    return img


def velvet():
    """Blue velvet, a little crushed: darker where it has been sat on, piped in a paler cord at the edge."""
    rng = random.Random(4111)
    img = canvas()
    for y in range(16):
        for x in range(16):
            d = ((x - 7.5) ** 2 + (y - 7.5) ** 2) ** .5
            px(img, x, y, mul((44, 64, 128), .88 + d * .018 + rng.uniform(-.04, .04)))
    for i in range(16):
        for x, y in ((i, 0), (i, 15), (0, i), (15, i)):
            px(img, x, y, (112, 132, 176))
    return img


def brass():
    img = ground((176, 140, 64), 4112, .05)
    bevel(img, 0, 0, 15, 15, 1.2, .7)
    return img


def clock_face():
    """The hall clock's dial: cream enamel in a brass bezel with thirteen hour marks and two black hands."""
    import math
    img = ground((176, 140, 64), 4113, .04)
    for y in range(16):
        for x in range(16):
            d = ((x - 7.5) ** 2 + (y - 7.5) ** 2) ** .5
            if d < 6.4:
                px(img, x, y, mul((232, 222, 196), 1 - d * .012))
    for k in range(13):
        a = 2 * math.pi * k / 13 - math.pi / 2
        px(img, round(7.5 + 5.3 * math.cos(a)), round(7.5 + 5.3 * math.sin(a)), (40, 30, 24))
    # 0.4.220: no painted hands; WinchesterClockRenderer draws hands that move round its thirteen hours.
    px(img, 7, 7, (60, 50, 40)); px(img, 8, 8, (60, 50, 40))
    bevel(img, 0, 0, 15, 15, 1.2, .7)
    return img


def pendulum():
    """The clock trunk's door glass: dark behind, the brass rod and bob of the pendulum hanging still."""
    img = ground((34, 22, 18), 4114, .05)
    for y in range(1, 11):
        px(img, 7, y, (176, 140, 64)); px(img, 8, y, (140, 108, 48))
    for y in range(11, 15):
        for x in range(6, 10):
            px(img, x, y, (196, 158, 72) if x < 8 else (150, 118, 52))
    bevel(img, 0, 0, 15, 15, .9, .6)
    return img


def pine():
    """New-cut pine: pale, the grain running along the board, one knot."""
    rng = random.Random(4115)
    img = ground((214, 178, 122), 4115, .03)
    for y in range(16):
        if y % 4 == 1 or rng.random() < .12:
            for x in range(16):
                if rng.random() < .85:
                    r, g, b, _ = img.getpixel((x, y)); px(img, x, y, mul((r, g, b), .9))
    for x, y in ((10, 6), (11, 6), (10, 7), (11, 7)):
        px(img, x, y, (150, 108, 62))
    return img


def pine_end():
    """A board's sawn end: growth rings round a pith near one edge."""
    img = canvas()
    for y in range(16):
        for x in range(16):
            d = ((x - 3) ** 2 + (y - 13) ** 2) ** .5
            px(img, x, y, mul((206, 168, 112), .82 if int(d) % 3 == 0 else 1))
    return img


def keg():
    """The nail keg's side: oak staves bound by two black iron hoops."""
    img = canvas()
    for y in range(16):
        for x in range(16):
            stave = x // 4
            c = mul((140, 98, 58), (1.0, .9, 1.05, .94)[stave] - (.18 if x % 4 == 3 else 0))
            if y in (2, 3, 12, 13):
                c = (46, 44, 46) if y in (2, 12) else (30, 30, 32)
            px(img, x, y, c)
    return img


def nails():
    """The keg's open top: a heap of cut nails, grey heads and shanks every way."""
    rng = random.Random(4116)
    img = ground((96, 94, 92), 4116, .1)
    for _ in range(40):
        x, y = rng.randrange(16), rng.randrange(16)
        px(img, x, y, (168, 166, 162) if rng.random() < .5 else (60, 58, 58))
    for i in range(16):
        for x, y in ((i, 0), (i, 15), (0, i), (15, i)):
            px(img, x, y, (120, 84, 50))
    return img


def steel():
    img = ground((150, 154, 160), 4117, .05)
    for x in range(16):
        px(img, x, 15, (84, 86, 92))
    return img


def plaque():
    """A brass door plate with 13 stamped into it."""
    img = brass()
    one = ['.#', '##', '.#', '.#', '.#', '.#', '##']
    three = ['###.', '...#', '...#', '.##.', '...#', '...#', '###.']
    for j, row in enumerate(one):
        for i, cell in enumerate(row):
            if cell == '#':
                px(img, 4 + i, 4 + j, (74, 52, 22))
    for j, row in enumerate(three):
        for i, cell in enumerate(row):
            if cell == '#':
                px(img, 7 + i, 4 + j, (74, 52, 22))
    return img


def spider_window():
    """0.4.201: the spider-web window. Lead cames run out from the top corner like a web's spokes and round it in rings;
    the panes between are pale clear glass, a few amethyst, as in the house's own."""
    img = canvas()
    lead = (52, 50, 54)
    import math
    for y in range(16):
        for x in range(16):
            r = math.hypot(x + .5, y + .5)
            a = math.degrees(math.atan2(y + .5, x + .5))
            spoke = any(abs(a - s) < 3.2 + 20 / max(r, 1) for s in (8, 30, 52, 74))
            ring = any(abs(r - k) < .55 for k in (4.5, 8.5, 12.5, 16.5))
            if x in (0, 15) or y in (0, 15) or spoke or ring:
                px(img, x, y, lead, 255)
            else:
                amethyst = int(r // 4) % 2 == 1 and int(a // 22) % 2 == 0
                px(img, x, y, (176, 140, 206) if amethyst else (214, 226, 220), 150 if amethyst else 96)
    return img


def door_panel():
    """One of the spare doors she bought by the dozen: four raised mahogany panels in their stiles and rails, never hung."""
    img = mahogany()
    for y in range(16):
        for x in range(16):
            r, g, b, _ = img.getpixel((x, y))
            frame = x in (0, 1, 14, 15) or y in (0, 1, 7, 8, 14, 15) or x in (7, 8)
            if not frame:
                inner = x in (2, 9) or y in (2, 9)
                px(img, x, y, mul((r, g, b), 1.12 if inner else 1.04))
    for x in range(16):
        px(img, x, 0, mul(img.getpixel((x, 0))[:3], .7))
    return img


def shavings():
    """Curls of pine shaving and sawdust on the floor where the carpenters work, the hay showing through."""
    rng = random.Random(4120)
    img = canvas()
    for _ in range(70):
        x, y = rng.randrange(16), rng.randrange(16)
        px(img, x, y, mul((226, 196, 140), rng.uniform(.85, 1.08)))
    for _ in range(9):
        x, y = rng.randrange(14), rng.randrange(15)
        for i in range(3):
            px(img, x + i, y + (i == 1), (238, 210, 156))
    return img


TEXTURES.update({'door_panel': door_panel, 'shavings': shavings})
TEXTURES.update({'spider_window': spider_window})
TEXTURES.update({'mahogany': mahogany, 'velvet': velvet, 'brass': brass, 'clock_face': clock_face, 'pendulum': pendulum,
                 'pine': pine, 'pine_end': pine_end, 'keg': keg, 'nails': nails, 'steel': steel, 'plaque': plaque})

DIRS = ('north', 'south', 'east', 'west', 'up', 'down')


def box(frm, to, tex, faces=DIRS, uv=None, over=None):
    f = {}
    for d in faces:
        face = {'texture': '#' + tex}
        if uv:
            face['uv'] = list(uv)
        f[d] = face
    for d, t in (over or {}).items():
        f[d] = {'texture': '#' + t}
        if uv:
            f[d]['uv'] = list(uv)
    # 0.4.202: faces of elements that reach past the block (the spare door's upper half, the studs) sample inside their own
    # sprite, as the Barretts' house does.
    if any(c < 0 or c > 16 for c in list(frm) + list(to)):
        (x1, y1, z1), (x2, y2, z2) = frm, to
        for d, face in f.items():
            if 'uv' in face:
                continue
            u0, v0, u1, v1 = {'down': [x1, 16 - z2, x2, 16 - z1], 'up': [x1, z1, x2, z2], 'north': [16 - x2, 16 - y2, 16 - x1, 16 - y1],
                              'south': [x1, 16 - y2, x2, 16 - y1], 'west': [z1, 16 - y2, z2, 16 - y1], 'east': [16 - z2, 16 - y2, 16 - z1, 16 - y1]}[d]
            (u0, u1), (v0, v1) = fit(u0, u1), fit(v0, v1)
            face['uv'] = [round(u0, 3), round(v0, 3), round(u1, 3), round(v1, 3)]
    return {'from': list(frm), 'to': list(to), 'faces': f}


def fit(a, b):
    """Bring one UV span inside the sprite: shifted by whole tiles when it fits, else stretched over the whole sprite."""
    lo, hi = min(a, b), max(a, b)
    if 0 <= lo and hi <= 16:
        return a, b
    if hi - lo <= 16:
        shift = math.floor(lo / 16) * 16
        if hi - shift > 16:
            shift = hi - 16
        return a - shift, b - shift
    return (0, 16) if a <= b else (16, 0)


MODEL_SPECS = {}


def spec(name, textures, particle):
    def wrap(fn):
        MODEL_SPECS[name] = (fn, textures, particle)
        return fn
    return wrap


# Authored facing north: the front toward -z, anything on a wall against +z.
@spec('seance_table', {'m': 'mahogany', 'b': 'brass'}, 'mahogany')
def m_seance_table():
    return [box((1, 12, 3), (15, 13, 13), 'm'), box((3, 12, 1), (13, 13, 15), 'm'), box((2, 12, 2), (14, 13, 14), 'm'),
            box((3, 11, 3), (13, 12, 13), 'm'), box((6.5, 2, 6.5), (9.5, 11, 9.5), 'm'), box((5.5, 1, 5.5), (10.5, 3, 10.5), 'm'),
            box((1.5, 0, 7), (14.5, 1.5, 9), 'm'), box((7, 0, 1.5), (9, 1.5, 14.5), 'm'),
            box((9, 13, 4), (11, 14.5, 6), 'b'), box((9.5, 14.5, 4.5), (10.5, 16, 5.5), 'b')]


@spec('seance_chair', {'m': 'mahogany', 'v': 'velvet'}, 'velvet')
def m_seance_chair():
    legs = [box((x, 0, z), (x + 1.5, 7, z + 1.5), 'm') for x in (3, 11.5) for z in (3, 11.5)]
    return legs + [box((2.5, 7, 2.5), (13.5, 8, 13.5), 'm'), box((3, 8, 3), (13, 9.5, 13), 'v', over={'down': 'm'}),
                   box((3, 9.5, 12), (4.5, 22, 13.5), 'm'), box((11.5, 9.5, 12), (13, 22, 13.5), 'm'),
                   box((4.5, 12, 12.5), (11.5, 20, 13), 'v'), box((3, 20, 12), (13, 22.5, 13.5), 'm')]


@spec('clock', {'m': 'mahogany', 'p': 'pendulum'}, 'mahogany')
def m_clock():
    return [box((2, 0, 4), (14, 3, 15), 'm'), box((3, 3, 5), (13, 16, 14), 'm', over={'north': 'p'})]


@spec('clock_top', {'m': 'mahogany', 'f': 'clock_face', 'b': 'brass'}, 'clock_face')
def m_clock_top():
    return [box((2, 0, 4), (14, 12, 15), 'm', over={'north': 'f'}), box((1, 12, 3), (15, 14, 16), 'm'),
            box((7, 14, 8), (9, 16, 10), 'b')]


@spec('sawhorse', {'p': 'pine', 'e': 'pine_end', 's': 'steel', 'm': 'mahogany'}, 'pine')
def m_sawhorse():
    legs = [box((x, 0, z), (x + 1.5, 10, z + 1.5), 'p') for x in (1.5, 13) for z in (4, 10.5)]
    return legs + [box((1, 6, 5.5), (4, 7, 10.5), 'p'), box((12.5, 6, 5.5), (15.5, 7, 10.5), 'p'),
                   box((0, 10, 6.5), (16, 12, 9.5), 'p', over={'east': 'e', 'west': 'e'}),
                   box((-3, 12, 4), (9, 13, 12), 'p', over={'east': 'e', 'west': 'e'}), box((10, 12, 4), (19, 13, 12), 'p', over={'east': 'e', 'west': 'e'}),
                   box((9.25, 12.5, 5), (9.75, 17, 11), 's'), box((9, 17, 10), (10, 20, 13), 'm')]


@spec('lumber', {'p': 'pine', 'e': 'pine_end'}, 'pine')
def m_lumber():
    over = {'east': 'e', 'west': 'e'}
    return [box((0, 0, 1), (16, 2, 5.5), 'p', over=over), box((0, 0, 6), (16, 2, 10.5), 'p', over=over), box((0, 0, 11), (16, 2, 15.5), 'p', over=over),
            box((-1, 2, 3), (15, 4, 7.5), 'p', over=over), box((1, 2, 8.5), (17, 4, 13), 'p', over=over), box((0, 4, 5.5), (16, 6, 10), 'p', over=over)]


@spec('nail_keg', {'k': 'keg', 'n': 'nails', 's': 'steel', 'p': 'pine'}, 'keg')
def m_nail_keg():
    return [box((4, 0, 4), (12, 10, 12), 'k', over={'up': 'n', 'down': 'p'}), box((5, 10, 7.5), (12, 11, 8.5), 'p'),
            box((11, 10, 6), (13, 12, 10), 's')]


@spec('spare_door', {'d': 'door_panel', 'm': 'mahogany', 'b': 'brass'}, 'door_panel')
def m_spare_door():
    """A spare door leaning against the wall, its foot a hand out from the skirting: never hung."""
    rot = {'origin': [8, 0, 13], 'axis': 'x', 'angle': 22.5}
    return [dict(box((2, 0, 12), (14, 15, 14), 'd', over={'north': 'd', 'south': 'd'}), rotation=rot),
            dict(box((2, 15, 12), (14, 30, 14), 'd', over={'north': 'd', 'south': 'd'}), rotation=rot),
            dict(box((11, 13, 11.5), (12.5, 14.5, 12), 'b'), rotation=rot)]


@spec('spare_window', {'g': 'leaded_glass', 'm': 'mahogany', 'p': 'pine'}, 'leaded_glass')
def m_spare_window():
    """A leaded window still in its crate's slats, propped against the wall: one of the hundreds she never set."""
    rot = {'origin': [8, 0, 13], 'axis': 'x', 'angle': 22.5}
    return [dict(box((1.5, 1, 12.5), (14.5, 15, 13.5), 'g', faces=('north', 'south')), rotation=rot),
            dict(box((1, 0, 12), (15, 1, 14), 'm'), rotation=rot), dict(box((1, 15, 12), (15, 16, 14), 'm'), rotation=rot),
            dict(box((1, 1, 12), (2, 15, 14), 'm'), rotation=rot), dict(box((14, 1, 12), (15, 15, 14), 'm'), rotation=rot),
            dict(box((0, 3, 11), (16, 4.5, 12), 'p'), rotation=rot), dict(box((0, 11, 11), (16, 12.5, 12), 'p'), rotation=rot)]


@spec('studs', {'p': 'pine', 'e': 'pine_end', 's': 'steel'}, 'pine')
def m_studs():
    """A wall going up where there was none: the sole plate nailed to the floor, three studs, the top plate, no lath yet."""
    over = {'up': 'e', 'down': 'e'}
    return [box((0, 0, 6.5), (16, 1.5, 9.5), 'p'), box((0, 28.5, 6.5), (16, 30, 9.5), 'p'),
            box((0.5, 1.5, 6.5), (2, 28.5, 9.5), 'p', over=over), box((7.25, 1.5, 6.5), (8.75, 28.5, 9.5), 'p', over=over),
            box((14, 1.5, 6.5), (15.5, 28.5, 9.5), 'p', over=over), box((2, 14, 7), (7.25, 15.5, 9), 'p'), box((8.75, 16, 7), (14, 17.5, 9), 'p')]


@spec('workbench', {'p': 'pine', 'e': 'pine_end', 's': 'steel', 'm': 'mahogany'}, 'pine')
def m_workbench():
    """The carpenters' bench: a thick pine top on four legs and a stretcher, the vise at one end, a plane and a saw left on it."""
    legs = [box((x, 0, z), (x + 2, 12, z + 2), 'p') for x in (1, 13) for z in (2, 12)]
    return legs + [box((2, 3, 3), (14, 4.5, 13), 'p'), box((0, 12, 1), (16, 15, 15), 'p', over={'east': 'e', 'west': 'e'}),
                   box((-1, 10, 1), (1, 15, 5), 'm'), box((-2, 12.5, 2.5), (-1, 13.5, 3.5), 's'),
                   box((4, 15, 4), (9, 17, 6.5), 'm'), box((5, 17, 4.75), (8, 17.5, 5.75), 's'),
                   box((9, 15, 9), (15, 15.25, 13), 's'), box((14, 15, 10), (16.5, 17, 12), 'm')]


@spec('sawdust', {'s': 'shavings'}, 'shavings')
def m_sawdust():
    return [box((0, 0, 0), (16, 0.5, 16), 's', faces=('up',))]


@spec('spider_window', {'g': 'spider_window'}, 'spider_window')
def m_spider_window():
    return [box((0, 0, 15.25), (16, 16, 16), 'g', faces=('north', 'south'))]


@spec('plaque', {'b': 'plaque'}, 'plaque')
def m_plaque():
    return [box((4, 5, 15), (12, 12, 16), 'b', uv=(2, 2, 14, 13))]


# ------------------------------------------------------------------------------------------------ 0.4.220: the house furnished
# The owner: "winchester could use custom chair textures", "random small dresser in winchester... review the furniture placement
# to ensure accuracy to environment", the clock "could have animated arms, and be pushed against the wall". The house had modern
# sofas, desks for a dining table and wallpaper for ceilings. These are its own: a coffered redwood ceiling, balloon-back dining
# chairs in oxblood velvet, a mahogany table with its runner and candelabra, a mirrored sideboard, a camelback settee, a wing
# chair, a lamp table, an upright piano, a fern on its stand, a hall stand, a treadle sewing machine and a dress form, gas
# sconces, and Persian rugs and runners.
def ceiling():
    """A coffered redwood ceiling: a sunk panel in each square, a lighter moulding round it, the grain running across."""
    rng = random.Random(4130)
    img = ground((112, 54, 36), 4130, .03)
    for y in range(16):
        for x in range(16):
            if x in (0, 15) or y in (0, 15):
                px(img, x, y, (150, 84, 52) if x == 0 or y == 0 else (70, 32, 22))
            elif x in (1, 14) or y in (1, 14):
                px(img, x, y, (132, 70, 44) if x == 1 or y == 1 else (84, 40, 26))
            elif (y + rng.randrange(2)) % 5 == 0:
                r, g, b, _ = img.getpixel((x, y)); px(img, x, y, mul((r, g, b), .88))
    for x, y in ((7, 7), (8, 7), (7, 8), (8, 8)):
        px(img, x, y, (176, 140, 64))
    return img


def velvet_red():
    """Oxblood velvet on the dining chairs and the settee, crushed darker where people sit, a tack every few pixels at the edge."""
    rng = random.Random(4131)
    img = canvas()
    for y in range(16):
        for x in range(16):
            d = ((x - 7.5) ** 2 + (y - 7.5) ** 2) ** .5
            px(img, x, y, mul((128, 28, 34), .84 + d * .02 + rng.uniform(-.04, .04)))
    for i in range(0, 16, 3):
        for x, y in ((i, 0), (i, 15), (0, i), (15, i)):
            px(img, x, y, (186, 150, 70))
    return img


def tufted():
    """The settee's buttoned back: oxblood velvet drawn into diamonds by covered buttons."""
    img = velvet_red()
    for y in range(16):
        for x in range(16):
            if (x + y) % 8 == 0 or (x - y) % 8 == 0:
                r, g, b, _ = img.getpixel((x, y)); px(img, x, y, mul((r, g, b), .78))
    for x, y in ((4, 4), (12, 4), (8, 8), (4, 12), (12, 12), (0, 8), (8, 0)):
        px(img, x, y, (60, 14, 18))
    return img


def velvet_green():
    """Bottle-green velvet on the wing chair, worn paler on the arms."""
    rng = random.Random(4132)
    img = canvas()
    for y in range(16):
        for x in range(16):
            px(img, x, y, mul((40, 82, 58), .9 + rng.uniform(-.06, .06) + (.08 if x in (0, 1, 14, 15) else 0)))
    return img


def table_top():
    """The dining table's top: French-polished mahogany, a long pale reflection down it from the window."""
    img = mahogany()
    for x in range(16):
        for y in (5, 6):
            r, g, b, _ = img.getpixel((x, y)); px(img, x, y, mul((r, g, b), 1.18))
    return img


def lace():
    """A lace runner: cream thread in an open pattern, the table showing dark through it."""
    img = canvas()
    for y in range(16):
        for x in range(16):
            if (x + y) % 4 == 0 or (x - y) % 4 == 0 or x in (0, 15):
                px(img, x, y, (232, 224, 204))
            else:
                px(img, x, y, (232, 224, 204), 0)
    return img


def persian():
    """A Persian rug's field: a medallion on madder red, indigo and ivory in the guls, the pattern meeting itself at every edge."""
    img = ground((134, 34, 34), 4133, .05)
    for y in range(16):
        for x in range(16):
            u, v = abs(x - 7.5), abs(y - 7.5)
            if abs(u + v - 5) < .6:
                px(img, x, y, (226, 206, 160))
            elif u + v < 2.6:
                px(img, x, y, (44, 52, 104))
            elif abs(u + v - 8) < .6 and min(u, v) > 1:
                px(img, x, y, (44, 52, 104))
            elif (x in (0, 15)) ^ (y in (0, 15)) and (x + y) % 2 == 0:
                px(img, x, y, (196, 148, 72))
    return img


def runner_edge():
    """Half the width of a Persian runner: its border down the outer edge, the field beside it."""
    img = persian()
    for y in range(16):
        px(img, 0, y, (44, 52, 104)); px(img, 1, y, (226, 206, 160)); px(img, 2, y, (44, 52, 104))
        px(img, 3, y, (196, 148, 72) if y % 4 < 2 else (134, 34, 34))
    return img


def runner_narrow():
    """A runner one block wide: the border down both edges, a line of small guls between."""
    img = ground((134, 34, 34), 4134, .05)
    for y in range(16):
        for x, c in ((0, (44, 52, 104)), (1, (226, 206, 160)), (14, (226, 206, 160)), (15, (44, 52, 104))):
            px(img, x, y, c)
        k = abs((y % 8) - 4)
        for x in range(3, 13):
            if abs(abs(x - 7.5) - (4 - k)) < .6:
                px(img, x, y, (226, 206, 160))
            elif abs(x - 7.5) < 4 - k - 1:
                px(img, x, y, (44, 52, 104))
    return img


def piano_front():
    """The upright piano's face: a fretwork panel over silk, candle brackets either side, the maker's gilt name."""
    img = mahogany()
    for y in range(2, 10):
        for x in range(3, 13):
            px(img, x, y, (150, 40, 44) if (x + y) % 3 else (60, 24, 18))
    for x in range(3, 13):
        px(img, x, 1, (70, 30, 22)); px(img, x, 10, (70, 30, 22))
    for x in range(6, 10):
        px(img, x, 12, (196, 160, 80))
    return img


def piano_keys():
    """The keyboard seen from in front and above: ivory keys, the black keys in their twos and threes, the felt behind."""
    img = canvas()
    for y in range(16):
        for x in range(16):
            px(img, x, y, (236, 228, 206) if x % 2 == 0 or y > 9 else (214, 206, 186))
    for x in (1, 3, 7, 9, 11, 15):
        for y in range(0, 9):
            px(img, x, y, (24, 20, 18))
    for x in range(16):
        px(img, x, 0, (120, 30, 34))
    return img


def buffet_front():
    """The sideboard's doors: two raised and carved panels, brass keyholes, a drawer over them."""
    img = mahogany()
    for x in range(16):
        px(img, x, 4, (60, 24, 18))
    for x0 in (1, 9):
        for y in range(6, 15):
            for x in range(x0, x0 + 6):
                if x in (x0, x0 + 5) or y in (6, 14):
                    px(img, x, y, (140, 70, 50))
    px(img, 7, 2, (196, 160, 80)); px(img, 8, 2, (196, 160, 80)); px(img, 6, 10, (196, 160, 80)); px(img, 9, 10, (196, 160, 80))
    return img


def mirror_glass():
    """Old mirror glass, a little silvering gone at the corners, a pale reflection across it."""
    img = canvas()
    for y in range(16):
        for x in range(16):
            v = 150 + ((x + y) % 9) * 6
            px(img, x, y, (v - 30, v - 22, v - 10))
    for x, y in ((0, 0), (1, 0), (0, 1), (15, 15), (14, 15), (15, 14)):
        px(img, x, y, (90, 84, 70))
    return img


def fern():
    """A Boston fern's fronds, arching, light underneath and dark in the middle."""
    rng = random.Random(4135)
    img = canvas()
    for x in range(16):
        for y in range(16):
            if rng.random() < .62 and abs(x - 7.5) < 8 - y * .35:
                px(img, x, y, mul((62, 120, 50), .8 + rng.uniform(0, .45)))
    return img


def lamp_glass():
    """An oil lamp's chimney and shade: a frosted globe lit from inside."""
    img = canvas()
    for y in range(16):
        for x in range(16):
            px(img, x, y, (250, 226, 170) if 4 < x < 11 else (236, 206, 150))
    return img


def sewing():
    """The sewing machine's head: black japanning, gilt scrolls, the nickel wheel."""
    img = ground((24, 22, 22), 4136, .04)
    for x, y in ((3, 4), (4, 3), (5, 3), (6, 4), (9, 6), (10, 5), (11, 5), (12, 6), (6, 9), (7, 10), (8, 10)):
        px(img, x, y, (200, 160, 70))
    return img


def treadle():
    """The treadle stand's cast iron: openwork scrolls."""
    img = canvas()
    for y in range(16):
        for x in range(16):
            if (x + y) % 5 == 0 or (x - y) % 5 == 0 or x in (0, 15) or y in (0, 15):
                px(img, x, y, (40, 38, 38))
    return img


def linen():
    """The dress form's linen torso, pinned with a half-made sleeve."""
    img = ground((214, 200, 172), 4137, .03)
    for y in range(16):
        px(img, 8, y, (170, 156, 128))
    for x, y in ((3, 4), (12, 6), (5, 11), (11, 12)):
        px(img, x, y, (200, 200, 210))
    return img


def frosted():
    """A gas sconce's frosted glass, the mantle bright inside."""
    img = canvas()
    for y in range(16):
        for x in range(16):
            d = ((x - 7.5) ** 2 + (y - 9) ** 2) ** .5
            px(img, x, y, (255, 236, 196) if d < 3 else (238, 222, 190))
    return img


TEXTURES.update({f.__name__: f for f in (ceiling, velvet_red, tufted, velvet_green, table_top, lace, persian, runner_edge,
                                         runner_narrow, piano_front, piano_keys, buffet_front, mirror_glass, fern, lamp_glass,
                                         sewing, treadle, linen, frosted)})
BLOCKS['winchester_ceiling'] = 'ceiling'
GLOWING = {'lamp_glass': 14, 'frosted': 14}


def legs4(x0, x1, z0, z1, top, t, w=1.5):
    return [box((x, 0, z), (x + w, top, z + w), t) for x in (x0, x1 - w) for z in (z0, z1 - w)]


@spec('dining_chair', {'m': 'mahogany', 'v': 'velvet_red'}, 'velvet_red')
def m_dining_chair():
    """A balloon-back dining chair: turned front legs, an oxblood velvet seat, the back's round loop with its padded panel."""
    return legs4(3, 13, 3, 13, 8, 'm') + [box((3, 8, 3), (13, 9, 13), 'm'), box((3.5, 9, 3.5), (12.5, 10.5, 12.5), 'v', over={'down': 'm'}),
            box((3.5, 9, 11.5), (5, 21, 13), 'm'), box((11, 9, 11.5), (12.5, 21, 13), 'm'), box((4, 20, 11.5), (12, 22.5, 13), 'm'),
            box((5, 13, 12), (11, 19.5, 12.8), 'v')]


@spec('table_end', {'m': 'mahogany', 't': 'table_top'}, 'table_top')
def m_table_end():
    """An end of the dining table: its top and apron, two turned legs at the end."""
    return [box((0, 14, 0), (16, 15.5, 16), 'm', over={'up': 't'}), box((1, 12, 1), (16, 14, 15), 'm'),
            box((1.5, 0, 1.5), (4, 12, 4), 'm'), box((1.5, 0, 12), (4, 12, 14.5), 'm')]


@spec('table_mid', {'m': 'mahogany', 't': 'table_top', 'l': 'lace'}, 'table_top')
def m_table_mid():
    """The middle of the table: the top, the apron down both sides, the lace runner along it."""
    return [box((0, 14, 0), (16, 15.5, 16), 'm', over={'up': 't'}), box((0, 12, 1), (16, 14, 2.5), 'm'), box((0, 12, 13.5), (16, 14, 15), 'm'),
            box((0, 15.5, 4), (16, 15.7, 12), 'l', faces=('up',), uv=(0, 0, 16, 16))]


@spec('table_pedestal', {'m': 'mahogany', 't': 'table_top', 'l': 'lace'}, 'table_top')
def m_table_pedestal():
    """The middle of a long table, where its pedestal stands: a turned column on four splayed feet."""
    return [box((0, 14, 0), (16, 15.5, 16), 'm', over={'up': 't'}), box((0, 12, 1), (16, 14, 2.5), 'm'), box((0, 12, 13.5), (16, 14, 15), 'm'),
            box((0, 15.5, 4), (16, 15.7, 12), 'l', faces=('up',), uv=(0, 0, 16, 16)),
            box((6, 2, 6), (10, 12, 10), 'm'), box((1, 0, 7), (15, 2, 9), 'm'), box((7, 0, 1), (9, 2, 15), 'm')]


@spec('candelabra', {'b': 'brass', 'w': 'lamp_glass'}, 'brass')
def m_candelabra():
    """A brass candelabra of three lights, set on the runner."""
    return [box((6, 0, 6), (10, 1, 10), 'b'), box((7.5, 1, 7.5), (8.5, 8, 8.5), 'b'), box((3, 8, 7.5), (13, 9, 8.5), 'b'),
            box((3, 9, 7.5), (4, 12, 8.5), 'w'), box((7.5, 9, 7.5), (8.5, 13, 8.5), 'w'), box((12, 9, 7.5), (13, 12, 8.5), 'w')]


def buffet(right):
    edge = (14.5, 16) if right else (0, 1.5)
    return [box((0, 1, 4), (16, 14, 16), 'm', over={'north': 'f'}), box((0, 14, 3), (16, 15, 16), 'm'), box((1, 0, 5), (15, 1, 15), 'm'),
            box((0, 15, 14.5), (16, 31, 16), 'm', over={'north': 'g'}), box((edge[0], 15, 14), (edge[1], 32, 16), 'm'),
            box((0, 30.5, 14), (16, 32, 16), 'm')]


@spec('buffet_l', {'m': 'mahogany', 'f': 'buffet_front', 'g': 'mirror_glass'}, 'mahogany')
def m_buffet_l():
    return buffet(False)


@spec('buffet_r', {'m': 'mahogany', 'f': 'buffet_front', 'g': 'mirror_glass'}, 'mahogany')
def m_buffet_r():
    return buffet(True)


def settee(right):
    arm = (14, 16) if right else (0, 2)
    return legs4(0.5, 15.5, 2, 14, 3, 'm', 1.5) + [box((0, 3, 2), (16, 7, 15), 'm'), box((0, 7, 3), (16, 9.5, 13), 'v', over={'down': 'm'}),
            box((0, 7, 12), (16, 22, 15), 'u', over={'up': 'v', 'south': 'v'}), box((arm[0], 7, 2), (arm[1], 15, 15), 'v'),
            box((arm[0], 15, 2), (arm[1], 16, 15), 'm')]


@spec('settee_l', {'m': 'mahogany', 'v': 'velvet_red', 'u': 'tufted'}, 'velvet_red')
def m_settee_l():
    return settee(False)


@spec('settee_r', {'m': 'mahogany', 'v': 'velvet_red', 'u': 'tufted'}, 'velvet_red')
def m_settee_r():
    return settee(True)


@spec('wing_chair', {'m': 'mahogany', 'v': 'velvet_green'}, 'velvet_green')
def m_wing_chair():
    """A wing chair in bottle-green velvet: rolled arms, the tall back and its wings."""
    return legs4(1.5, 14.5, 2, 14, 3, 'm', 1.5) + [box((1.5, 3, 2), (14.5, 7.5, 14), 'v'), box((2, 7.5, 2.5), (14, 9.5, 12), 'v'),
            box((1.5, 7.5, 12), (14.5, 26, 14.5), 'v'), box((1, 7.5, 2), (3, 14, 14), 'v'), box((13, 7.5, 2), (15, 14, 14), 'v'),
            box((1, 14, 9), (3, 24, 14), 'v'), box((13, 14, 9), (15, 24, 14), 'v')]


@spec('lamp_table', {'m': 'mahogany', 'b': 'brass', 'w': 'lamp_glass'}, 'mahogany')
def m_lamp_table():
    """A round lamp table on its pedestal, and the oil lamp on it."""
    return [box((3, 11, 3), (13, 12, 13), 'm'), box((7, 1, 7), (9, 11, 9), 'm'), box((4, 0, 7), (12, 1, 9), 'm'),
            box((7, 0, 4), (9, 1, 12), 'm'), box((6.5, 12, 6.5), (9.5, 14, 9.5), 'b'), box((7.5, 14, 7.5), (8.5, 16, 8.5), 'b'),
            box((6, 16, 6), (10, 21, 10), 'w')]


def piano(right):
    edge = (14, 16) if right else (0, 2)
    return [box((0, 0, 7), (16, 22, 16), 'm', over={'north': 'f'}), box((0, 10, 3), (16, 11, 7), 'm', over={'up': 'k'}),
            box((edge[0], 2, 3), (edge[1], 12, 7), 'm'), box((0, 22, 6.5), (16, 23, 16), 'm'), box((1, 1, 4), (15, 2, 7), 'm')]


@spec('piano_l', {'m': 'mahogany', 'f': 'piano_front', 'k': 'piano_keys'}, 'mahogany')
def m_piano_l():
    return piano(False)


@spec('piano_r', {'m': 'mahogany', 'f': 'piano_front', 'k': 'piano_keys'}, 'mahogany')
def m_piano_r():
    return piano(True)


@spec('fern_stand', {'m': 'mahogany', 'f': 'fern', 'b': 'brass'}, 'fern')
def m_fern_stand():
    """A Boston fern in a brass pot on a tall mahogany stand."""
    leaf = []
    for angle in (45, -45):
        e = box((0, 13, 8), (16, 29, 8), 'f', faces=('north', 'south'))
        e['rotation'] = {'origin': [8, 8, 8], 'axis': 'y', 'angle': angle}
        leaf.append(e)
    return [box((5, 0, 5), (11, 1, 11), 'm'), box((7, 1, 7), (9, 10, 9), 'm'), box((4.5, 10, 4.5), (11.5, 11, 11.5), 'm'),
            box((5, 11, 5), (11, 15, 11), 'b')] + leaf


@spec('hall_stand', {'m': 'mahogany', 'b': 'brass'}, 'mahogany')
def m_hall_stand():
    """The hall stand's foot: a box seat for gloves, the umbrella rails either side, its drip pans."""
    return [box((0, 0, 8), (16, 1, 16), 'm'), box((2, 1, 9), (14, 9, 16), 'm'), box((1.5, 9, 8.5), (14.5, 10, 16), 'm'),
            box((0, 0, 8), (1.5, 16, 9.5), 'm'), box((14.5, 0, 8), (16, 16, 9.5), 'm'), box((0, 12, 8), (16, 13, 9), 'b'),
            box((0.5, 1, 9), (1, 10, 15), 'b')]


@spec('hall_stand_top', {'m': 'mahogany', 'b': 'brass', 'g': 'mirror_glass', 'c': 'velvet_green'}, 'mahogany')
def m_hall_stand_top():
    """The hall stand's top: the mirror in its frame, the brass hooks, a bowler hat hung on one."""
    return [box((0, 0, 14.5), (16, 14, 16), 'm', over={'north': 'g'}, uv=(0, 0, 16, 16)), box((0, 14, 14), (16, 16, 16), 'm'),
            box((0, 0, 9.5), (1.5, 14, 15), 'm'), box((14.5, 0, 9.5), (16, 14, 15), 'm'),
            box((2, 10, 12), (3, 11, 14.5), 'b'), box((13, 10, 12), (14, 11, 14.5), 'b'),
            box((11, 7, 10), (15, 9, 14), 'c'), box((12, 9, 11), (14, 11, 13), 'c')]


@spec('sewing_machine', {'m': 'mahogany', 's': 'sewing', 't': 'treadle', 'b': 'brass'}, 'sewing')
def m_sewing_machine():
    """A treadle sewing machine: the iron stand and its pedal, the oak top, the black head with its wheel and needle."""
    return [box((1, 0, 3), (2, 11, 13), 't'), box((14, 0, 3), (15, 11, 13), 't'), box((2, 1, 5), (14, 2, 11), 't'),
            box((0, 11, 2), (16, 12.5, 14), 'm'), box((3, 12.5, 6), (12, 14, 10), 's'), box((10, 14, 6), (12, 19, 10), 's'),
            box((3, 17, 6.5), (12, 19, 9.5), 's'), box((3, 14, 7.5), (4, 17, 8.5), 's'), box((12, 15, 7), (13, 19, 9), 'b')]


@spec('dress_form', {'m': 'mahogany', 'l': 'linen', 'b': 'brass'}, 'linen')
def m_dress_form():
    """A dress form on its stand, a half-made sleeve pinned to it."""
    return [box((4, 0, 7), (12, 1, 9), 'm'), box((7, 0, 4), (9, 1, 12), 'm'), box((7.5, 1, 7.5), (8.5, 10, 8.5), 'b'),
            box((4.5, 10, 6), (11.5, 16, 10), 'l'), box((4, 16, 5.5), (12, 21, 10.5), 'l'), box((6.5, 21, 7), (9.5, 23, 9), 'l')]


@spec('sconce', {'b': 'brass', 'w': 'frosted'}, 'frosted')
def m_sconce():
    """A gas sconce on the wall: the brass plate and arm, the frosted globe."""
    return [box((6, 3, 15), (10, 9, 16), 'b'), box((7.5, 5, 11), (8.5, 6, 15), 'b'), box((6.5, 6, 10), (9.5, 7, 13), 'b'),
            box((5.5, 7, 9), (10.5, 12, 14), 'w')]


@spec('console', {'m': 'mahogany', 't': 'table_top', 'b': 'brass'}, 'table_top')
def m_console():
    """A hall console against the wall: the top the ledger stands on, a carved apron with its drawer pull, two front legs, a
    shelf low between them."""
    return [box((0, 14, 5), (16, 16, 16), 'm', over={'up': 't'}), box((1, 11, 6), (15, 14, 15.5), 'm'),
            box((1.5, 0, 6), (3, 11, 7.5), 'm'), box((13, 0, 6), (14.5, 11, 7.5), 'm'), box((1.5, 0, 14), (3, 11, 15.5), 'm'),
            box((13, 0, 14), (14.5, 11, 15.5), 'm'), box((2, 3, 7), (14, 4, 15), 'm'), box((7, 12, 5.5), (9, 13, 6), 'b')]


def flat(name, tex):
    spec(name, {'r': tex}, tex)(lambda: [box((0, 0, 0), (16, .5, 16), 'r', faces=('up', 'north', 'south', 'east', 'west'), uv=(0, 0, 16, 16))])


flat('rug_persian', 'persian')
flat('runner_edge', 'runner_edge')
flat('runner_mid', 'persian')
flat('runner_narrow', 'runner_narrow')


TURN = {'north': 0, 'east': 90, 'south': 180, 'west': 270}


def model_json(name):
    fn, textures, particle = MODEL_SPECS[name]
    tex = {k: NS + v for k, v in textures.items()}
    tex['particle'] = NS + particle
    elements = fn()
    for e in elements:
        for face in e['faces'].values():
            glow = GLOWING.get(textures.get(face['texture'][1:], ''))
            if glow:
                face['neoforge_data'] = {'block_light': glow, 'sky_light': 0}
    return {'parent': 'minecraft:block/block', 'render_type': 'minecraft:translucent' if name == 'spider_window' else 'minecraft:cutout', 'ambientocclusion': False, 'textures': tex, 'elements': elements}



def files():
    out = {}
    for name, fn in TEXTURES.items():
        out[TEX / f'{name}.png'] = fn()
    for block, tex in BLOCKS.items():
        model = {'parent': 'minecraft:block/cube_all', 'textures': {'all': NS + tex}}
        if tex == 'leaded_glass':
            model['render_type'] = 'minecraft:translucent'
        out[MODELS / f'{tex}.json'] = json.dumps(model, indent=2) + '\n'
        out[STATES / f'{block}.json'] = json.dumps({'variants': {'': {'model': NS + tex}}}, indent=2) + '\n'
    for name in MODEL_SPECS:
        out[MODELS / f'{name}.json'] = json.dumps(model_json(name), indent=1) + '\n'
    variants = {}
    for facing, y in TURN.items():
        for kind in MODEL_SPECS:
            v = {'model': NS + kind}
            if y:
                v['y'] = y
            variants[f'facing={facing},kind={kind}'] = v
    out[STATES / 'winchester_fitting.json'] = json.dumps({'variants': variants}, indent=1) + '\n'
    return out


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument('--check', action='store_true')
    args = ap.parse_args()
    bad = []
    for path, data in files().items():
        if args.check:
            if not path.exists():
                bad.append(f'missing {path}')
            elif isinstance(data, Image.Image):
                with Image.open(path) as saved:
                    if saved.size != data.size or saved.convert('RGBA').tobytes() != data.tobytes():
                        bad.append(f'differs {path}')
            elif path.read_text() != data:
                bad.append(f'differs {path}')
        else:
            path.parent.mkdir(parents=True, exist_ok=True)
            if isinstance(data, Image.Image):
                data.save(path)
            else:
                path.write_text(data)
    if bad:
        print('\n'.join(bad))
        sys.exit(1)
    print(f'{"Verified" if args.check else "Drew"} {len(TEXTURES)} Winchester textures, {len(BLOCKS)} materials and {len(MODEL_SPECS)} fittings.')


if __name__ == '__main__':
    main()
