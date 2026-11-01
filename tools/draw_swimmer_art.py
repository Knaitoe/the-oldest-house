"""The last swimmer (0.4.227): a small municipal pool at closing.

Draws, in native Minecraft pixels from fixed seeds: nine materials (pool tile; the dark lane line on the pool floor; the deck's
grip tile; glazed wall tile and its band; acoustic ceiling panel; the office's painted blockwork; corridor lino; the deck's
depth tiles), and every fitting of the swimmer_fitting block, its models facing north and turned by its facing: a lane rope's
floats; the pool cover flat, pressed up from underneath, and its leading edge with the bar and the strap; the cover reel full,
half run out and empty, and its stand with the crank wheel; six numbered starting blocks; the pace clock with its sweep hand at
each of twelve marks; the pool ladder; a changing-room bench; lockers shut and one ajar; the office radio off and on; the closing
checklist on its clipboard; a stack of kickboards; a ceiling lamp lit and off; the exit sign; a folded towel; a drain grate; and
two wall signs (no running; last swim). Writes the models and block states, and the blocks' names. --check verifies everything.
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
P = NS + 'block/swimmer/'


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


# A three-by-five pixel alphabet for the signs and numbers.
GLYPHS = {
    '0': ['111', '101', '101', '101', '111'], '1': ['010', '110', '010', '010', '111'], '2': ['111', '001', '111', '100', '111'],
    '3': ['111', '001', '011', '001', '111'], '4': ['101', '101', '111', '001', '001'], '5': ['111', '100', '111', '001', '111'],
    '6': ['111', '100', '111', '101', '111'], '9': ['111', '101', '111', '001', '111'], '.': ['000', '000', '000', '000', '010'],
    'N': ['101', '111', '111', '111', '101'], 'O': ['111', '101', '101', '101', '111'], 'R': ['110', '101', '110', '101', '101'],
    'U': ['101', '101', '101', '101', '111'], 'I': ['111', '010', '010', '010', '111'], 'G': ['111', '100', '101', '101', '111'],
    'L': ['100', '100', '100', '100', '111'], 'A': ['010', '101', '111', '101', '101'], 'S': ['111', '100', '111', '001', '111'],
    'T': ['111', '010', '010', '010', '010'], 'W': ['101', '101', '111', '111', '101'], 'M': ['101', '111', '111', '101', '101'],
    'E': ['111', '100', '110', '100', '111'], 'X': ['101', '101', '010', '101', '101'], ' ': ['000', '000', '000', '000', '000'],
}


def text(im, x, y, word, colour, step=4):
    for ch in word:
        for j, row in enumerate(GLYPHS[ch]):
            for i, on in enumerate(row):
                if on == '1':
                    dot(im, x + i, y + j, colour)
        x += step


def tiles(base, grout, seed, size=4):
    im = new(base=base, seed=seed, spread=3)
    for y in range(16):
        for x in range(16):
            if x % size == 0 or y % size == 0:
                dot(im, x, y, grout)
    return im


# ------------------------------------------------------------------------------------------------ materials
def materials():
    m = {}
    m['pool_tile'] = tiles('#d6e2e6', '#a8bcc4', 227)
    line = tiles('#d6e2e6', '#a8bcc4', 2271)
    fill(line, 5, 0, 11, 16, '#2a3a4a')
    m['pool_line'] = line
    deck = new(base='#b8b2a4', seed=2272, spread=6)
    for y in range(0, 16, 8):
        for x in range(16):
            dot(deck, x, y, '#9a9486')
    for x in range(0, 16, 8):
        for y in range(16):
            dot(deck, x, y, '#9a9486')
    rnd = random.Random(2273)
    for _ in range(22):
        dot(deck, rnd.randrange(16), rnd.randrange(16), '#a8a294')
    m['deck'] = deck
    m['wall_tile'] = tiles('#e4e6e0', '#c4c6c0', 2274, 8)
    band = tiles('#3a5a72', '#2a4a62', 2275, 8)
    fill(band, 0, 7, 16, 9, '#e4e6e0')
    m['wall_band'] = band
    ceil = new(base='#d8d6ce', seed=2276, spread=3)
    rnd = random.Random(2277)
    for _ in range(40):
        dot(ceil, rnd.randrange(16), rnd.randrange(16), '#b8b6ae')
    for x in range(16):
        dot(ceil, x, 15, '#9a988e'); dot(ceil, 15, x, '#9a988e')
    m['ceiling'] = ceil
    block = new(base='#c8cabc', seed=2278, spread=3)
    for y in range(0, 16, 4):
        for x in range(16):
            dot(block, x, y, '#a8aa9c')
    for y in range(16):
        dot(block, 0 if (y // 4) % 2 == 0 else 8, y, '#a8aa9c')
    m['blockwork'] = block
    lino = new(base='#8a8a7a', seed=2279, spread=5)
    rnd = random.Random(2280)
    for _ in range(30):
        dot(lino, rnd.randrange(16), rnd.randrange(16), rnd.choice(['#9a9a8a', '#7a7a6a']))
    m['lino'] = lino
    return m


def depth(n):
    im = materials()['deck'].copy()
    fill(im, 2, 4, 14, 12, '#e8e6dc')
    text(im, 3, 5, '1.0' if n == 0 else '2.0', '#1e2a3a')
    dot(im, 13, 9, '#1e2a3a'); dot(im, 13, 10, '#1e2a3a')
    return im


# ------------------------------------------------------------------------------------------------ fittings
def rope():
    out = {}
    for name, c in (('float_a', '#c83a32'), ('float_b', '#e8e8e2')):
        im = new(base=c, seed=2290 + len(name), spread=4)
        for x in range(16):
            dot(im, x, 0, shade(c, 18)[:3]); dot(im, x, 15, shade(c, -30)[:3])
        out[name] = im
    return out


def cover():
    im = new(base='#2a3446', seed=2295, spread=3)
    for x in range(0, 16, 4):
        for y in range(16):
            dot(im, x, y, '#202838')
    return im


def cover_side():
    im = new(base='#222a3a', seed=2296, spread=2)
    for x in range(16):
        dot(im, x, 0, '#3a4456')
    return im


def edge():
    im = new(base='#8a8e94', seed=2297, spread=3)
    for x in range(16):
        dot(im, x, 0, '#aeb2b8'); dot(im, x, 15, '#5a5e64')
    return im


def strap():
    im = new(base='#d8c060', seed=2298, spread=4)
    for y in range(16):
        dot(im, 0, y, '#a89040'); dot(im, 15, y, '#a89040')
    return im


def reel_face(stage):
    im = new(base='#3a4456', seed=2300 + stage, spread=2)
    r = [7, 4, 1][stage]
    for y in range(16):
        for x in range(16):
            d = math.hypot(x - 7.5, y - 7.5)
            if d > r + 0.5:
                im.putpixel((x, y), shade('#3a4456', 0))
            if abs(d - 1.2) < 0.6:
                im.putpixel((x, y), shade('#9aa0a8', 0))
            elif d <= r and int(d * 2) % 3 == 0:
                im.putpixel((x, y), shade('#2a3446', 0))
    return im


def wheel():
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    for y in range(16):
        for x in range(16):
            d = math.hypot(x - 7.5, y - 7.5)
            if 5.6 < d < 7.6 or d < 1.6 or (d < 7 and (abs(x - 7.5) < 0.8 or abs(y - 7.5) < 0.8)):
                im.putpixel((x, y), shade('#b8bcc2', int((x - y) * 1.5)))
    dot(im, 13, 3, '#2a2a2e'); dot(im, 13, 4, '#2a2a2e')
    return im


def start(n):
    im = new(base='#e4e2dc', seed=2310 + n, spread=3)
    fill(im, 0, 0, 16, 2, '#3a5a72')
    text(im, 6, 6, str(n + 1), '#1e2a3a')
    return im


def pace(stage):
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    for y in range(16):
        for x in range(16):
            d = math.hypot(x - 7.5, y - 7.5)
            if d < 7.6:
                im.putpixel((x, y), shade('#f0eee6', 0) if d < 6.8 else shade('#2a2a2e', 0))
    for k in range(12):
        a = k * math.pi / 6
        x, y = 7.5 + 5.6 * math.sin(a), 7.5 - 5.6 * math.cos(a)
        dot(im, round(x), round(y), '#2a2a2e' if k % 3 else '#c83a32')
    a = stage * math.pi / 6
    for t in range(0, 6):
        x, y = 7.5 + t * math.sin(a), 7.5 - t * math.cos(a)
        dot(im, round(x), round(y), '#c83a32')
    dot(im, 7, 7, '#2a2a2e'); dot(im, 8, 8, '#2a2a2e')
    return im


def lockers(open_one):
    im = new(base='#5a7a8a', seed=2320, spread=3)
    fill(im, 7, 0, 9, 16, '#3a5a6a')
    for x0 in (1, 9):
        for y in range(2, 6, 2):
            fill(im, x0 + 1, y, x0 + 5, y + 1, '#2a4a5a')
        dot(im, x0 + 5, 9, '#c8c8cc')
    if open_one:
        fill(im, 9, 0, 16, 16, '#14181c'); fill(im, 10, 3, 15, 4, '#3a3a3e')
    return im


def radio(on):
    im = new(base='#6a4a30', seed=2330, spread=3)
    for y in range(2, 14, 2):
        for x in range(2, 8):
            dot(im, x, y, '#3a2a1c')
    fill(im, 9, 3, 15, 8, '#f0d890' if on else '#8a8270')
    dot(im, 11 if on else 10, 4, '#c83a32'); dot(im, 11 if on else 10, 5, '#c83a32')
    fill(im, 10, 10, 13, 13, '#2a2a2e')
    return im


def clipboard():
    im = new(base='#8a6a42', seed=2340, spread=3)
    fill(im, 2, 1, 14, 15, '#eeeae0')
    fill(im, 5, 0, 11, 2, '#9a9aa0')
    for y in range(4, 14, 2):
        fill(im, 3, y, 12 - (y % 4), y + 1, '#5a5a66')
    for y in (4, 6):
        dot(im, 12, y, '#2a6a3a')
    return im


def boards():
    im = Image.new('RGBA', (16, 16))
    for y in range(16):
        c = ['#3a6ab0', '#e0c040', '#c84a3a', '#3aa060'][(y // 4) % 4]
        for x in range(16):
            im.putpixel((x, y), shade(c, -20 if y % 4 == 3 else 0))
    return im


def lamp(on):
    im = new(base='#f4f0e0' if on else '#8a8a84', seed=2350, spread=2)
    for x in range(16):
        for y in (0, 15):
            dot(im, x, y, '#9a9a94'); dot(im, y, x, '#9a9a94')
    return im


def exit_sign():
    im = new(base='#2a8a4a', seed=2360, spread=2)
    text(im, 2, 6, 'EXIT', '#f0f4ee')
    for x in range(16):
        dot(im, x, 0, '#1a5a2a'); dot(im, x, 15, '#1a5a2a')
    return im


def towel():
    im = new(base='#e6e2d8', seed=2370, spread=3)
    for y in (3, 12):
        for x in range(16):
            dot(im, x, y, '#3a5a72')
    return im


def drain():
    im = new(base='#8a8e94', seed=2380, spread=2)
    for x in range(1, 15, 2):
        for y in range(1, 15):
            dot(im, x, y, '#2a2e34')
    return im


def sign(n):
    im = new(base='#f0eee6', seed=2390 + n, spread=2)
    fill(im, 0, 0, 16, 2, '#c83a32'); fill(im, 0, 14, 16, 16, '#c83a32')
    if n == 0:
        text(im, 4, 3, 'NO', '#1e2a3a'); text(im, 0, 9, 'RUN', '#1e2a3a', 5)
    else:
        text(im, 0, 3, 'LAST', '#1e2a3a'); text(im, 0, 9, 'SWIM', '#1e2a3a')
    return im


def element(lo, hi, textures, uv=None):
    faces = {}
    for f in ('north', 'south', 'east', 'west', 'up', 'down'):
        name = textures.get(f, textures.get('all'))
        if name is None:
            continue
        faces[f] = {'texture': '#' + name}
        if uv and f in uv:
            faces[f]['uv'] = uv[f]
    return {'from': lo, 'to': hi, 'faces': faces}


def model(elements, textures, particle, render='cutout'):
    return {'ambientocclusion': False, 'render_type': 'minecraft:' + render, 'textures': {**{k: P + v for k, v in textures.items()}, 'particle': P + particle}, 'elements': elements}


def fitting_models():
    m = {}
    m['rope_0'] = model([element([6.5, 13, z], [9.5, 15.5, z + 4], {'all': 'a' if z % 8 == 0 else 'b'}) for z in range(0, 16, 4)], {'a': 'float_a', 'b': 'float_b'}, 'float_a')
    flat = element([0, 13.5, 0], [16, 15.5, 16], {'up': 'c', 'down': 'c', 'north': 's', 'south': 's', 'east': 's', 'west': 's'})
    m['cover_0'] = model([flat], {'c': 'cover', 's': 'cover_side'}, 'cover')
    m['cover_1'] = model([element([0, 13.5, 0], [16, 15.5, 16], {'up': 'c', 'down': 'c', 'north': 's', 'south': 's', 'east': 's', 'west': 's'}),
                          element([3, 15.5, 4], [13, 17.5, 12], {'all': 'c'}), element([5, 17.5, 6], [11, 18.5, 10], {'all': 'c'})], {'c': 'cover', 's': 'cover_side'}, 'cover')
    m['cover_2'] = model([flat, element([0, 14, 0], [16, 17, 2], {'all': 'e'}), element([6, 15, 1], [10, 18, 4], {'all': 't'})], {'c': 'cover', 's': 'cover_side', 'e': 'edge', 't': 'strap'}, 'cover')
    for s, (a, b) in enumerate(((3, 13), (5, 11), (7, 9))):
        m[f'reel_{s}'] = model([element([0, a, a], [16, b, b], {'north': 'c', 'south': 'c', 'up': 'c', 'down': 'c', 'east': 'f', 'west': 'f'})], {'c': 'cover' if s < 2 else 'edge', 'f': f'reel_face_{s}'}, 'cover')
    m['reel_end_0'] = model([element([6, 0, 6], [10, 9, 10], {'all': 'e'}), element([2, 1, 13], [14, 13, 14], {'north': 'w', 'south': 'w'})], {'e': 'edge', 'w': 'wheel'}, 'edge')
    for n in range(6):
        m[f'start_{n}'] = model([element([2, 0, 2], [14, 7, 14], {'all': 'side', 'up': 'top'}), element([2, 7, 2], [14, 9, 12], {'all': 'side', 'up': 'top'})], {'side': 'start_side', 'top': f'start_{n}'}, 'start_side')
    for n in range(12):
        m[f'pace_clock_{n}'] = model([element([0, 0, 15], [16, 16, 16], {'north': 'f', 'south': 'r', 'east': 'r', 'west': 'r', 'up': 'r', 'down': 'r'})], {'f': f'pace_{n}', 'r': 'edge'}, 'edge')
    m['ladder_0'] = model([element([3, 0, 13], [4, 16, 14], {'all': 'e'}), element([12, 0, 13], [13, 16, 14], {'all': 'e'}), element([3, 3, 13], [13, 4, 14], {'all': 'e'}),
                           element([3, 9, 13], [13, 10, 14], {'all': 'e'}), element([3, 15, 10], [4, 16, 14], {'all': 'e'}), element([12, 15, 10], [13, 16, 14], {'all': 'e'})], {'e': 'edge'}, 'edge')
    for n in range(2):
        m[f'depth_{n}'] = model([element([0, 0, 0], [16, 16, 16], {'all': 'd', 'up': 't'})], {'d': 'deck', 't': f'depth_{n}'}, 'deck', 'solid')
        m[f'lockers_{n}'] = model([element([0, 0, 0], [16, 16, 16], {'all': 's', 'north': 'f'})], {'s': 'locker_side', 'f': f'lockers_{n}'}, 'locker_side', 'solid')
        m[f'radio_{n}'] = model([element([3, 0, 6], [13, 6, 10], {'all': 's', 'north': 'f'}), element([11, 6, 8], [12, 11, 9], {'all': 'e'})], {'s': 'radio_side', 'f': f'radio_{n}', 'e': 'edge'}, 'radio_side')
        m[f'lamp_{n}'] = model([element([2, 14, 2], [14, 16, 14], {'all': 'r', 'down': 'l'})], {'r': 'edge', 'l': f'lamp_{n}'}, 'edge')
        m[f'sign_{n}'] = model([element([2, 3, 15.5], [14, 13, 16], {'north': 's', 'south': 's'}, {'north': [2, 3, 14, 13], 'south': [2, 3, 14, 13]})], {'s': f'sign_{n}'}, f'sign_{n}')
    m['bench_0'] = model([element([0, 6, 3], [16, 8, 13], {'all': 'b'}), element([1, 0, 4], [3, 6, 12], {'all': 'e'}), element([13, 0, 4], [15, 6, 12], {'all': 'e'})], {'b': 'bench', 'e': 'edge'}, 'bench')
    m['clipboard_0'] = model([element([4, 0, 3], [12, 1, 13], {'all': 's', 'up': 'c'}, {'up': [2, 1, 14, 15]})], {'c': 'clipboard', 's': 'radio_side'}, 'clipboard')
    m['boards_0'] = model([element([2, 0, 4], [14, 7, 12], {'all': 'b'})], {'b': 'boards'}, 'boards')
    m['exit_sign_0'] = model([element([2, 5, 15], [14, 10, 16], {'north': 'x', 'south': 'x', 'up': 'r', 'down': 'r', 'east': 'r', 'west': 'r'}, {'north': [2, 5, 14, 10], 'south': [2, 5, 14, 10]})], {'x': 'exit', 'r': 'edge'}, 'exit')
    m['towel_0'] = model([element([3, 0, 4], [13, 2, 12], {'all': 't'})], {'t': 'towel'}, 'towel')
    m['drain_0'] = model([element([2, 0, 2], [14, 0.5, 14], {'up': 'd', 'down': 'd'})], {'d': 'drain'}, 'drain')
    return m


def textures():
    out = {}
    for k, v in materials().items():
        out[f'block/swimmer/{k}.png'] = v
    for k, v in rope().items():
        out[f'block/swimmer/{k}.png'] = v
    out['block/swimmer/cover.png'] = cover(); out['block/swimmer/cover_side.png'] = cover_side()
    out['block/swimmer/edge.png'] = edge(); out['block/swimmer/strap.png'] = strap()
    for s in range(3):
        out[f'block/swimmer/reel_face_{s}.png'] = reel_face(s)
    out['block/swimmer/wheel.png'] = wheel()
    out['block/swimmer/start_side.png'] = new(base='#d4d2cc', seed=2315, spread=3)
    for n in range(6):
        out[f'block/swimmer/start_{n}.png'] = start(n)
    for n in range(12):
        out[f'block/swimmer/pace_{n}.png'] = pace(n)
    for n in range(2):
        out[f'block/swimmer/depth_{n}.png'] = depth(n)
        out[f'block/swimmer/lockers_{n}.png'] = lockers(n == 1)
        out[f'block/swimmer/radio_{n}.png'] = radio(n == 1)
        out[f'block/swimmer/lamp_{n}.png'] = lamp(n == 0)
        out[f'block/swimmer/sign_{n}.png'] = sign(n)
    out['block/swimmer/locker_side.png'] = new(base='#4a6a7a', seed=2321, spread=2)
    out['block/swimmer/radio_side.png'] = new(base='#5a3e28', seed=2331, spread=2)
    out['block/swimmer/bench.png'] = new(base='#b08a5a', seed=2335, spread=5)
    out['block/swimmer/clipboard.png'] = clipboard(); out['block/swimmer/boards.png'] = boards()
    out['block/swimmer/exit.png'] = exit_sign(); out['block/swimmer/towel.png'] = towel(); out['block/swimmer/drain.png'] = drain()
    return out


KINDS = ['rope', 'cover', 'reel', 'reel_end', 'start', 'pace_clock', 'ladder', 'depth', 'bench', 'lockers', 'radio', 'clipboard', 'boards', 'lamp', 'exit_sign', 'towel', 'drain', 'sign']
STAGES = {'cover': 3, 'reel': 3, 'start': 6, 'pace_clock': 12, 'depth': 2, 'lockers': 2, 'radio': 2, 'lamp': 2, 'sign': 2}
FACING_Y = {'north': 0, 'east': 90, 'south': 180, 'west': 270}
MAX_STAGE = 12
NAMES = {'swimmer_fitting': 'Pool Fitting', 'swimmer_pool_tile': 'Pool Tile', 'swimmer_pool_line': 'Lane Line Tile', 'swimmer_deck': 'Pool Deck',
         'swimmer_wall_tile': 'Wall Tile', 'swimmer_wall_band': 'Wall Tile Band', 'swimmer_ceiling': 'Ceiling Panel', 'swimmer_blockwork': 'Painted Blockwork',
         'swimmer_lino': 'Lino'}


def files():
    out = {}
    for name in materials():
        out[f'models/block/swimmer_{name}.json'] = {'parent': 'minecraft:block/cube_all', 'textures': {'all': P + name}}
        out[f'blockstates/swimmer_{name}.json'] = {'variants': {'': {'model': NS + f'block/swimmer_{name}'}}}
    for key, obj in fitting_models().items():
        out[f'models/block/swimmer/{key}.json'] = obj
    variants = {}
    for kind in KINDS:
        for facing, y in FACING_Y.items():
            for stage in range(MAX_STAGE):
                drawn = stage if stage < STAGES.get(kind, 1) else 0
                v = {'model': NS + f'block/swimmer/{kind}_{drawn}'}
                if y:
                    v['y'] = y
                variants[f'facing={facing},kind={kind},stage={stage}'] = v
    out['blockstates/swimmer_fitting.json'] = {'variants': variants}
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
            assert f'block/swimmer/{tex.split("swimmer/")[1]}.png' in images, (key, tex)
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
    print(('Verified' if check else 'Drew') + f' the pool: {len(images)} textures, {len(data)} models and states.')


if __name__ == '__main__':
    main()
