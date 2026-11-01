"""Polybius (0.4.217): the strip-mall arcade and its black cabinet.

Draws, in native Minecraft pixels from fixed seeds: three materials (the black carpet with its scattered neon shapes, the dark
painted wall panels, the black ceiling tiles); every fitting of the polybius_fitting block, its models facing north and turned by
its facing (the four cabinets, body and screen, each with its own side art and an animated attract-mode screen; the taped-up
dead cabinets; the change machine; the notice board with its clipping; the men's clipboard; neon strips in three colours; a
stool; the prize counter and its shelf; three posters for games the arcade doesn't have; a bin; the ceiling lights, lit and
dead). Writes the models and block states with them, and the blocks' names. --check verifies everything already written.

Palette: carpet black #0c0a12 with neon pink #ff4fb0, cyan #45e6ff, yellow #ffe14a, green #6dff7a; wall aubergine #241a2e;
cabinet woods and laminates per game; the black cabinet #070707 with a faint green sheen #12261c.
"""
from pathlib import Path
import argparse
import json
import math
import random
from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
A = ROOT / 'src/main/resources/assets/the_oldest_house'
NS = 'the_oldest_house:'
P = NS + 'block/polybius/'
NEON = ['#ff4fb0', '#45e6ff', '#6dff7a', '#ffe14a']
GAME_BODY = ['#1c2350', '#123a3c', '#4a2e18', '#070707']
GAME_ACCENT = ['#f0d080', '#7ad8e8', '#f09a3a', '#12261c']
FRAMES = 4


def rgb(c):
    c = c.lstrip('#')
    return tuple(int(c[i:i + 2], 16) for i in (0, 2, 4))


def shade(c, k, a=255):
    r, g, b = rgb(c) if isinstance(c, str) else c[:3]
    return (max(0, min(255, r + k)), max(0, min(255, g + k)), max(0, min(255, b + k)), a)


def new(size=(16, 16), base=None, seed=0, spread=4):
    im = Image.new('RGBA', size, (0, 0, 0, 0))
    if base:
        rnd = random.Random(seed); px = im.load()
        for y in range(size[1]):
            for x in range(size[0]):
                px[x, y] = shade(base, rnd.randint(-spread, spread))
    return im


def fill(im, x0, y0, x1, y1, colour, a=255):
    px = im.load()
    for y in range(max(0, y0), min(im.size[1], y1)):
        for x in range(max(0, x0), min(im.size[0], x1)):
            px[x, y] = shade(colour, 0, a)


def dot(im, x, y, colour, a=255):
    if 0 <= x < im.size[0] and 0 <= y < im.size[1]:
        im.putpixel((x, y), shade(colour, 0, a))


# ------------------------------------------------------------------------------------------------ materials
def carpet():
    im = new(base='#0c0a12', seed=217, spread=2); rnd = random.Random(2171)
    shapes = [((1, 2), [(0, 0), (1, 0), (2, 1)]), ((9, 1), [(0, 1), (1, 0), (2, 1), (3, 0)]), ((4, 9), [(0, 0), (0, 1), (1, 1)]),
              ((12, 7), [(0, 0), (1, 1), (2, 2)]), ((7, 13), [(0, 0), (1, 0), (2, 0)]), ((13, 13), [(0, 0), (1, 1)]), ((2, 6), [(0, 0)])]
    for (ox, oy), pts in shapes:
        c = NEON[rnd.randrange(4)]
        for dx, dy in pts:
            dot(im, (ox + dx) % 16, (oy + dy) % 16, c)
    return im


def wall():
    im = new(base='#241a2e', seed=218, spread=3)
    for y in range(16):
        dot(im, 0, y, '#1a1222'); dot(im, 8, y, '#1a1222')
    for x in range(16):
        dot(im, x, 15, '#1a1222')
    return im


def ceiling():
    im = new(base='#111014', seed=219, spread=2)
    for i in range(16):
        dot(im, i, 0, '#1c1a20'); dot(im, 0, i, '#1c1a20')
    rnd = random.Random(2191)
    for _ in range(10):
        dot(im, rnd.randrange(1, 16), rnd.randrange(1, 16), '#0a090c')
    return im


def materials():
    return {'carpet': carpet(), 'wall': wall(), 'ceiling': ceiling()}


# ------------------------------------------------------------------------------------------------ cabinets
def cab_side(g):
    im = new(base=GAME_BODY[g], seed=230 + g, spread=3)
    a = GAME_ACCENT[g]
    if g == 0:  # a moth, big, over a lamp's glow
        for x, y in [(5, 6), (6, 5), (7, 6), (9, 6), (10, 5), (11, 6), (6, 7), (10, 7), (8, 6), (8, 7), (8, 8)]:
            dot(im, x, y, a)
        fill(im, 7, 11, 10, 13, '#f8e8a0')
    elif g == 1:  # waves
        for x in range(16):
            dot(im, x, 9 + int(round(math.sin(x / 2.5) * 1.2)), a); dot(im, x, 12 + int(round(math.cos(x / 2.5) * 1.2)), shade(a, -40))
    elif g == 2:  # a dial
        for i in range(9):
            ang = math.pi * (1 + i / 8)
            dot(im, 8 + int(round(math.cos(ang) * 5)), 10 + int(round(math.sin(ang) * 5)), a)
        dot(im, 8, 10, '#e04030'); dot(im, 9, 9, '#e04030'); dot(im, 10, 8, '#e04030')
    else:  # nothing: a faint green sheen down one edge
        for y in range(16):
            dot(im, 14, y, '#0e1a14'); dot(im, 15, y, a)
    for y in range(16):
        dot(im, 0, y, shade(GAME_BODY[g], -12))
    return im


def cab_front(g):
    body = GAME_BODY[g]
    im = new(base=body, seed=240 + g, spread=2)
    # the control panel, lit from the screen above
    fill(im, 1, 0, 15, 5, shade(body, 18))
    dot(im, 4, 2, '#202020'); dot(im, 4, 1, '#e03030' if g < 3 else '#1a3a2a')  # the stick
    for x, c in [(9, '#f0d040'), (11, '#40a0f0')] if g < 3 else [(9, '#0f1f17'), (11, '#0f1f17')]:
        dot(im, x, 2, c)
    # the coin door: two slots with their lamps, unless it is the black one, which has none
    if g < 3:
        fill(im, 5, 8, 11, 13, '#2a2a2e')
        dot(im, 6, 9, '#ff7030'); dot(im, 9, 9, '#ff7030'); fill(im, 6, 11, 7, 12, '#0a0a0a'); fill(im, 9, 11, 10, 12, '#0a0a0a')
    fill(im, 1, 14, 15, 16, shade(body, -10))
    return im


def bezel(g, dead=False, look=0):
    body = '#3a3430' if dead else GAME_BODY[g]
    im = new(base=body, seed=250 + g + (10 if dead else 0) + look, spread=2)
    # the marquee: lit, and drawn with the game's light, or nothing at all
    if not dead and g < 3:
        fill(im, 1, 0, 15, 3, shade(GAME_ACCENT[g], -10))
        for x in range(3, 13, 2):
            dot(im, x, 1, '#ffffff')
    elif dead:
        fill(im, 1, 0, 15, 3, '#6a6058' if look == 0 else '#5a5450')
    fill(im, 2, 4, 14, 13, '#050505')
    if dead:
        fill(im, 3, 5, 13, 12, '#141414')
        for i in range(10):  # a strip of masking tape across the dark glass
            dot(im, 3 + i, 10 - i // 2 - look, '#d8cca0'); dot(im, 3 + i, 11 - i // 2 - look, '#c8bc90')
    return im


def screen_frames(g):
    im = Image.new('RGBA', (16, 16 * FRAMES), (0, 0, 0, 255))
    for f in range(FRAMES):
        oy = f * 16
        if g == 0:
            fill(im, 6, oy + 6, 9, oy + 9, '#ffe8a0')
            ang = f / FRAMES * 2 * math.pi
            dot(im, 7 + int(round(math.cos(ang) * 4)), oy + 7 + int(round(math.sin(ang) * 4)), '#e8dcc8')
            for i in range(3):
                dot(im, (2 + i * 5 + f * 2) % 16, oy + 2 + i * 4, '#fff0b0')
        elif g == 1:
            fill(im, 0, oy, 16, oy + 16, '#06121a')
            for i in range(3):
                dot(im, 3 + i * 4, oy + (14 - f * 3 - i * 4) % 16, '#a0d8f0')
            dot(im, 7 + (f % 2), oy + 13, '#d8c0a0'); dot(im, 8 + (f % 2), oy + 13, '#d8c0a0')
        elif g == 2:
            fill(im, 1, oy + 7, 15, oy + 10, '#d8c890')
            dot(im, 3 + f * 3, oy + 6, '#c02018'); dot(im, 3 + f * 3, oy + 7, '#c02018'); dot(im, 3 + f * 3, oy + 8, '#c02018')
            fill(im, 2 + f * 2, oy + 9, 7 + f * 2, oy + 10, '#60a040')
        else:
            for t in range(0, 60):
                tt = t * .3
                r = 1 + tt * .55
                a = tt + f * math.pi / 8
                x, y = 8 + int(round(math.cos(a) * r)), oy + 8 + int(round(math.sin(a) * r))
                if oy <= y < oy + 16:
                    k = int(40 + 30 * math.sin(tt * .4 + f))
                    dot(im, x, y, (40 + k, 140 + k // 2, 110 + k))
    return im


SCREEN_META = {'animation': {'frametime': 6}}
BLACK_META = {'animation': {'frametime': 20, 'interpolate': True}}


# ------------------------------------------------------------------------------------------------ the rest
def change(part):
    if part == 'front':
        im = new(base='#8a8a92', seed=260, spread=3)
        fill(im, 3, 2, 13, 6, '#1a1a1e'); fill(im, 4, 3, 12, 5, '#3a6a3a')  # the lit FREE PLAY card
        fill(im, 5, 8, 11, 9, '#101010')  # the bill slot
        fill(im, 4, 12, 12, 15, '#5a5a62'); fill(im, 5, 13, 11, 14, '#202024')  # the tray
        return im
    return new(base='#7a7a82', seed=261, spread=3)


def notice():
    im = new(base='#a87a4a', seed=262, spread=6)
    fill(im, 0, 0, 16, 1, '#5a3a20'); fill(im, 0, 15, 16, 16, '#5a3a20'); fill(im, 0, 0, 1, 16, '#5a3a20'); fill(im, 15, 0, 16, 16, '#5a3a20')
    fill(im, 2, 2, 9, 12, '#d8c890')
    for y in range(4, 11, 2):
        fill(im, 3, y, 8, y + 1, '#8a7a60')
    fill(im, 3, 2, 8, 3, '#3a3020')
    fill(im, 10, 9, 14, 13, '#f0ece0'); fill(im, 11, 10, 13, 11, '#2a2a2a')
    dot(im, 5, 2, '#e03030'); dot(im, 12, 9, '#3060e0')
    return im


def clipboard():
    im = new(base='#8a5a30', seed=263, spread=4)
    fill(im, 2, 3, 14, 15, '#f0ece0')
    for y in range(6, 14, 2):
        fill(im, 3, y, 12, y + 1, '#9a9a9a')
    fill(im, 5, 1, 11, 4, '#b0b0b8')
    return im


def neon(c):
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    fill(im, 0, 6, 16, 10, c, 90)
    fill(im, 0, 7, 16, 9, c)
    fill(im, 0, 7, 16, 8, '#ffffff', 160)
    return im


def stool(part):
    if part == 'top':
        im = new(base='#a02028', seed=264, spread=4)
        fill(im, 7, 3, 9, 13, '#c8c0a0')  # tape over the split
        return im
    return new(base='#b8bcc4', seed=265, spread=5)


def counter(part):
    if part == 'front':
        im = new(base='#2a2620', seed=266, spread=3)
        fill(im, 1, 1, 15, 11, '#3a4a50')
        for x, y, c in [(3, 7, '#e04060'), (6, 8, '#40c060'), (9, 6, '#f0e040'), (12, 8, '#4080f0'), (4, 9, '#202020')]:
            fill(im, x, y, x + 2, y + 2, c)
        return im
    return new(base='#b8c8cc', seed=267, spread=2)


def prizes():
    im = new(base='#3a2a1e', seed=268, spread=3)
    for y in (5, 11):
        fill(im, 0, y, 16, y + 1, '#5a4a3a')
    for x, y, c in [(1, 2, '#d0a070'), (5, 3, '#202020'), (9, 1, '#e060a0'), (13, 3, '#40a0f0'), (2, 8, '#f0e040'), (7, 8, '#60c060'), (11, 7, '#c04040')]:
        fill(im, x, y, x + 3, y + 3, c)
    dot(im, 10, 2, '#202020')  # the plush dog's one eye
    return im


def poster(s):
    base = ['#1a1040', '#401018', '#102a18'][s]
    im = new(base=base, seed=270 + s, spread=3)
    if s == 0:  # a rocket, climbing
        fill(im, 7, 3, 9, 11, '#d8d8e0'); dot(im, 7, 2, '#e04030'); dot(im, 8, 2, '#e04030'); fill(im, 6, 11, 10, 13, '#f0a030')
    elif s == 1:  # a house on a hill with one lit window
        fill(im, 4, 7, 12, 13, '#201010'); dot(im, 8, 6, '#201010'); dot(im, 7, 9, '#f0d060')
    else:  # a car, side on, very fast
        fill(im, 3, 9, 13, 11, '#e0e040'); fill(im, 5, 7, 10, 9, '#e0e040'); dot(im, 5, 11, '#101010'); dot(im, 11, 11, '#101010')
        for x in range(0, 3):
            dot(im, x, 9, '#80803a')
    fill(im, 2, 14, 14, 15, '#e0e0e0')
    return im


def bin_side():
    im = new(base='#5a5a60', seed=275, spread=4)
    for x in range(0, 16, 3):
        for y in range(16):
            dot(im, x, y, '#4a4a50')
    return im


def light(lit):
    im = new(base='#e8e8e0' if lit else '#5a5a58', seed=276 + lit, spread=2)
    fill(im, 0, 0, 16, 1, '#8a8a88'); fill(im, 0, 15, 16, 16, '#8a8a88')
    return im


# ------------------------------------------------------------------------------------------------ models
def element(lo, hi, textures, uv=None):
    faces = {}
    for face in ('north', 'south', 'east', 'west', 'up', 'down'):
        name = textures.get(face, textures.get('all'))
        if name is None:
            continue
        faces[face] = {'texture': '#' + name}
        if uv and face in uv:
            faces[face]['uv'] = uv[face]
    return {'from': lo, 'to': hi, 'faces': faces}


def model(elements, textures, particle, render='cutout'):
    return {'ambientocclusion': False, 'render_type': 'minecraft:' + render, 'textures': {**{k: P + v for k, v in textures.items()}, 'particle': P + particle}, 'elements': elements}


def fitting_models():
    m = {}
    for g in range(4):
        m[f'cabinet_{g}'] = model([element([1, 0, 1], [15, 16, 15], {'all': 's', 'north': 'f', 'up': 't', 'down': 't'}, {'north': [1, 0, 15, 16]}),
                                  element([1, 13, 0], [15, 15, 1], {'all': 's', 'north': 'p', 'up': 'p'})],
                                 {'s': f'cab_side_{g}', 'f': f'cab_front_{g}', 't': 'cab_top', 'p': f'cab_front_{g}'}, f'cab_side_{g}')
        m[f'screen_{g}'] = model([element([1, 0, 3], [15, 16, 15], {'all': 's', 'north': 'b', 'up': 't', 'down': 't'}),
                                 element([3, 4, 2.9], [13, 12, 3], {'north': 'v'}, {'north': [3, 4, 13, 12]}),
                                 element([1, 13, 1], [15, 16, 3], {'all': 's', 'north': 'b'}, {'north': [1, 0, 15, 3]})],
                                {'s': f'cab_side_{g}', 'b': f'bezel_{g}', 't': 'cab_top', 'v': f'screen_{g}'}, f'cab_side_{g}')
    for s in range(2):
        m[f'dead_{s}'] = model([element([1, 0, 1], [15, 16, 15], {'all': 's', 'north': 'f', 'up': 't', 'down': 't'})],
                              {'s': 'dead_side', 'f': 'dead_front', 't': 'cab_top'}, 'dead_side')
        m[f'dead_screen_{s}'] = model([element([1, 0, 3], [15, 16, 15], {'all': 's', 'north': 'b', 'up': 't', 'down': 't'})],
                                     {'s': 'dead_side', 'b': f'dead_bezel_{s}', 't': 'cab_top'}, 'dead_side')
        m[f'light_{s}'] = model([element([2, 15, 2], [14, 16, 14], {'all': 'l'})], {'l': f'light_{s}'}, f'light_{s}')
    m['change_0'] = model([element([2, 0, 6], [14, 16, 16], {'all': 's', 'north': 'f', 'up': 's'})], {'s': 'change_side', 'f': 'change_front'}, 'change_side')
    m['notice_0'] = model([element([1, 1, 15], [15, 15, 16], {'all': 'e', 'north': 'n'}, {'north': [1, 1, 15, 15]})], {'n': 'notice', 'e': 'notice_edge'}, 'notice')
    m['clipboard_0'] = model([element([4, 0, 3], [12, 1, 13], {'all': 'c', 'up': 'c'}, {'up': [2, 1, 14, 15]})], {'c': 'clipboard'}, 'clipboard')
    for s in range(3):
        m[f'neon_{s}'] = model([element([0, 6, 15.5], [16, 10, 16], {'north': 'n', 'south': 'n'}, {'north': [0, 6, 16, 10], 'south': [0, 6, 16, 10]})], {'n': f'neon_{s}'}, f'neon_{s}', 'translucent')
        m[f'poster_{s}'] = model([element([1, 1, 15.5], [15, 15, 16], {'north': 'p', 'south': 'p'})], {'p': f'poster_{s}'}, f'poster_{s}')
    m['stool_0'] = model([element([7, 0, 7], [9, 8, 9], {'all': 'l'}), element([5, 0, 5], [11, 1, 11], {'all': 'l'}), element([4, 8, 4], [12, 10, 12], {'all': 's', 'up': 't'})],
                         {'l': 'stool_leg', 's': 'stool_top', 't': 'stool_top'}, 'stool_top')
    m['counter_0'] = model([element([0, 0, 0], [16, 16, 16], {'all': 's', 'north': 'f', 'up': 't'})], {'s': 'counter_side', 'f': 'counter_front', 't': 'counter_top'}, 'counter_front')
    m['prizes_0'] = model([element([0, 0, 8], [16, 16, 16], {'all': 's', 'north': 'f'})], {'s': 'prizes_side', 'f': 'prizes'}, 'prizes')
    m['bin_0'] = model([element([4, 0, 4], [12, 12, 12], {'all': 's', 'up': 'i'})], {'s': 'bin_side', 'i': 'bin_inside'}, 'bin_side')
    return m


def textures():
    out = {}
    for k, v in materials().items():
        out[f'block/polybius/{k}.png'] = v
    for g in range(4):
        out[f'block/polybius/cab_side_{g}.png'] = cab_side(g)
        out[f'block/polybius/cab_front_{g}.png'] = cab_front(g)
        out[f'block/polybius/bezel_{g}.png'] = bezel(g)
        out[f'block/polybius/screen_{g}.png'] = screen_frames(g)
    out['block/polybius/cab_top.png'] = new(base='#181418', seed=280, spread=2)
    out['block/polybius/dead_side.png'] = new(base='#3a3430', seed=281, spread=4)
    out['block/polybius/dead_front.png'] = cab_front(0).convert('LA').convert('RGBA')
    for s in range(2):
        out[f'block/polybius/dead_bezel_{s}.png'] = bezel(0, True, s)
        out[f'block/polybius/light_{s}.png'] = light(s == 0)
    out['block/polybius/change_front.png'] = change('front'); out['block/polybius/change_side.png'] = change('side')
    out['block/polybius/notice.png'] = notice(); out['block/polybius/notice_edge.png'] = new(base='#5a3a20', seed=282, spread=3)
    out['block/polybius/clipboard.png'] = clipboard()
    for s in range(3):
        out[f'block/polybius/neon_{s}.png'] = neon(NEON[s])
        out[f'block/polybius/poster_{s}.png'] = poster(s)
    out['block/polybius/stool_top.png'] = stool('top'); out['block/polybius/stool_leg.png'] = stool('leg')
    out['block/polybius/counter_front.png'] = counter('front'); out['block/polybius/counter_top.png'] = counter('top'); out['block/polybius/counter_side.png'] = new(base='#2a2620', seed=283, spread=3)
    out['block/polybius/prizes.png'] = prizes(); out['block/polybius/prizes_side.png'] = new(base='#3a2a1e', seed=284, spread=3)
    out['block/polybius/bin_side.png'] = bin_side(); out['block/polybius/bin_inside.png'] = new(base='#1a1a1c', seed=285, spread=2)
    return out


KINDS = ['cabinet', 'screen', 'dead', 'dead_screen', 'change', 'notice', 'clipboard', 'neon', 'stool', 'counter', 'prizes', 'poster', 'bin', 'light']
STAGES = {'cabinet': 4, 'screen': 4, 'neon': 3, 'poster': 3, 'dead': 2, 'dead_screen': 2, 'light': 2}
FACING_Y = {'north': 0, 'east': 90, 'south': 180, 'west': 270}
NAMES = {'polybius_fitting': 'Arcade Fitting', 'polybius_carpet': 'Arcade Carpet', 'polybius_wall': 'Arcade Wall', 'polybius_ceiling': 'Arcade Ceiling'}


def files():
    out = {}
    for name in materials():
        out[f'models/block/polybius_{name}.json'] = {'parent': 'minecraft:block/cube_all', 'textures': {'all': P + name}}
        out[f'blockstates/polybius_{name}.json'] = {'variants': {'': {'model': NS + f'block/polybius_{name}'}}}
    for key, obj in fitting_models().items():
        out[f'models/block/polybius/{key}.json'] = obj
    variants = {}
    for kind in KINDS:
        for facing, y in FACING_Y.items():
            for stage in range(4):
                drawn = stage if stage < STAGES.get(kind, 1) else 0
                v = {'model': NS + f'block/polybius/{kind}_{drawn}'}
                if y:
                    v['y'] = y
                variants[f'facing={facing},kind={kind},stage={stage}'] = v
    out['blockstates/polybius_fitting.json'] = {'variants': variants}
    for g in range(4):
        out[f'textures/block/polybius/screen_{g}.png.mcmeta'] = BLACK_META if g == 3 else SCREEN_META
    return out


def main():
    check = argparse.ArgumentParser()
    check.add_argument('--check', action='store_true')
    check = check.parse_args().check
    images, data = textures(), files()
    models = fitting_models()
    for kind in KINDS:
        for stage in range(STAGES.get(kind, 1)):
            assert f'{kind}_{stage}' in models, (kind, stage)
    for key, obj in models.items():
        for tex in obj['textures'].values():
            assert f'block/polybius/{tex.split("polybius/")[1]}.png' in images, (key, tex)
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
    for block, name in NAMES.items():
        key = f'block.the_oldest_house.{block}'
        if check:
            assert lang.get(key) == name, key
        else:
            lang[key] = name
    if not check:
        lang_path.write_text(json.dumps(lang, indent=2, ensure_ascii=False) + '\n')
    print(('Verified' if check else 'Drew') + f' the arcade: {len(images)} textures, {len(data)} models and states.')


if __name__ == '__main__':
    main()
