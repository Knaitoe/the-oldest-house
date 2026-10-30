"""Borrowed Light (0.4.199), the first puzzle room: a dim dressing room, a stage lamp, a dressing mirror and a brass sun.

Draws, in native Minecraft pixels from fixed seeds: the room's papered wall, dark boards and plaster ceiling; the working parts
of the one borrowed_light block (the lamp, its shutter shut and folded back with the brass catch, the dressing mirror and its
eighth-turned glass, the brass sun cold and lit, the track's scraped boards and their three deeper stops, the dim sconces);
and the beam's soft texture. Writes the models and block states with them. --check verifies everything already written.
"""
from pathlib import Path
import argparse
import json
import random
from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parents[1]
A = ROOT / 'src/main/resources/assets/the_oldest_house'
NS = 'the_oldest_house:'
P = NS + 'block/borrowed_light/'


def rgb(c):
    c = c.lstrip('#')
    return tuple(int(c[i:i + 2], 16) for i in (0, 2, 4))


def shade(c, k, a=255):
    r, g, b = rgb(c) if isinstance(c, str) else c[:3]
    return (max(0, min(255, r + k)), max(0, min(255, g + k)), max(0, min(255, b + k)), a)


def new(base=None, seed=0, spread=5, size=(16, 16)):
    im = Image.new('RGBA', size, (0, 0, 0, 0))
    if base:
        rnd = random.Random(seed); px = im.load()
        for y in range(size[1]):
            for x in range(size[0]):
                px[x, y] = shade(base, rnd.randint(-spread, spread))
    return im


# ---------------------------------------------------------------------------------------------------- the room
def wallpaper():
    """A dark plum paper with a small pale sprig every four pixels, offset row by row, and a faint seam."""
    im = new('#43303b', 301, 3); px = im.load()
    for y in range(0, 16, 4):
        for x in range((y // 4 % 2) * 2, 16, 4):
            px[x, y] = shade('#6f5462', 0); px[x, (y + 1) % 16] = shade('#5d4552', 0)
    for y in range(16):
        px[15, y] = shade('#3a2933', 0)
    return im


def boards(seed=311, base='#3a2a20'):
    """Dark boards four pixels wide, running east and west, with their joints and a little grain."""
    im = new(base, seed, 4); px = im.load(); rnd = random.Random(seed + 1)
    for y in (3, 7, 11, 15):
        for x in range(16):
            px[x, y] = shade(base, -16)
    for row in range(4):
        joint = rnd.randint(2, 13)
        for y in range(row * 4, row * 4 + 3):
            px[joint, y] = shade(base, -14)
    for _ in range(10):
        x, y = rnd.randint(0, 15), rnd.randint(0, 15)
        if y % 4 != 3:
            px[x, y] = shade(base, 7)
    return im


def plaster():
    return new('#c9c0ae', 321, 4)


def track(stop):
    """The boards, scraped pale along their length where the mirror's feet have been dragged; a stop is scraped deeper and wider."""
    im = boards(331); px = im.load(); rnd = random.Random(333)
    for y in (5, 6, 9, 10):
        for x in range(16):
            if rnd.random() < .85:
                px[x, y] = shade('#6a5644', rnd.randint(-6, 6))
    if stop:
        for y in range(4, 12):
            for x in range(5, 11):
                px[x, y] = shade('#7d6852', rnd.randint(-8, 4))
        for x in range(5, 11):
            px[x, 4] = shade('#2c2018', 0); px[x, 11] = shade('#2c2018', 0)
    return im


# ---------------------------------------------------------------------------------------------------- the parts
def enamel():
    return new('#26282a', 341, 3)


def lens():
    """The lamp's front: a black rim round a warm, bright lens with a hot centre."""
    im = enamel(); d = ImageDraw.Draw(im)
    d.ellipse((2, 2, 13, 13), fill=shade('#3b3d3f', 0))
    d.ellipse((3, 3, 12, 12), fill=shade('#f1d79a', 0))
    d.ellipse((5, 5, 10, 10), fill=shade('#fff3cf', 0))
    d.point((6, 6), fill=shade('#ffffff', 0))
    return im


def brass(seed=351):
    im = new('#b08a3e', seed, 6); px = im.load()
    for x in range(16):
        px[x, 0] = shade('#d8b664', 0)
    return im


def shutter():
    """The shutter's face: black steel louvres, each with a lit edge where the lamp's light catches it."""
    im = new('#2b2c2d', 361, 3); px = im.load()
    for y in range(1, 16, 3):
        for x in range(1, 15):
            px[x, y] = shade('#3f4142', 0)
            px[x, y + 1 if y + 1 < 16 else y] = shade('#1c1d1e', 0)
    return im


def frame():
    """The mirror's walnut frame and stand."""
    im = new('#4d3424', 371, 4); px = im.load()
    for y in range(0, 16, 3):
        px[3, y] = shade('#3d291c', 0); px[11, (y + 1) % 16] = shade('#3d291c', 0)
    return im


def glass():
    """Old mirror glass: cool and grey-silver, two pale diagonal streaks, a little foxing at the edges."""
    im = new('#9aa3a6', 381, 4); px = im.load(); rnd = random.Random(383)
    for i in range(16):
        for k in (0, 1):
            x, y = i, 15 - i - 5 + k
            if 0 <= y < 16:
                px[x, y] = shade('#d7dde0', 0)
        y = 15 - i + 4
        if 0 <= y < 16:
            px[i, y] = shade('#c3cacd', 0)
    for _ in range(14):
        x, y = rnd.choice([(rnd.randint(0, 15), rnd.choice([0, 1, 14, 15])), (rnd.choice([0, 1, 14, 15]), rnd.randint(0, 15))])
        px[x, y] = shade('#7b7562', rnd.randint(-8, 8))
    return im


def sun(lit):
    """A brass sun let into the wall: a disc and twelve short rays. Cold it is dull; lit it is bright and warm at the centre."""
    base = '#e8c46a' if lit else '#8a6f3a'
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0)); d = ImageDraw.Draw(im); px = im.load()
    rays = [(8, 0), (8, 15), (0, 8), (15, 8), (2, 2), (13, 2), (2, 13), (13, 13), (5, 0), (10, 0), (5, 15), (10, 15)]
    for x, y in rays:
        d.line((8, 8, x, y), fill=shade(base, -18 if not lit else -6))
    d.ellipse((3, 3, 12, 12), fill=shade(base, 0))
    d.ellipse((5, 5, 10, 10), fill=shade('#fff1c4' if lit else '#a0844a', 0))
    if not lit:
        rnd = random.Random(391)
        for _ in range(6):
            x, y = rnd.randint(3, 12), rnd.randint(3, 12)
            if px[x, y][3]:
                px[x, y] = shade('#5f6b4f', 0)
    return im


def shade_glass():
    """The sconce's small shade: amber glass, faintly lit."""
    im = new('#b77a3a', 401, 6); px = im.load()
    for x in range(16):
        px[x, 15] = shade('#e3a95e', 0)
    return im


def beam():
    """The beam's texture: brightest down the middle, soft at the edges, with a few brighter specks of dust that drift along it."""
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0)); px = im.load(); rnd = random.Random(411)
    for y in range(16):
        for x in range(16):
            edge = 1 - abs(x - 7.5) / 8
            px[x, y] = (255, 255, 255, int(255 * max(.15, edge) * .9))
    for _ in range(9):
        x, y = rnd.randint(3, 12), rnd.randint(0, 15)
        px[x, y] = (255, 255, 255, 255)
    return im


def leaded():
    """The lamp rooms' interior window: small leaded panes of old, faintly green glass, the lead dark and soft."""
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0)); px = im.load(); rnd = random.Random(391)
    for x in range(16):
        for y in range(16):
            lead = x % 5 == 0 or y % 5 == 0
            if lead:
                px[x, y] = shade('#3a3a38', rnd.randint(-6, 6))
            else:
                g = 196 + rnd.randint(-10, 10)
                px[x, y] = (g - 30, g, g - 18, 92)
    return im


# ---------------------------------------------------------------------------------------------------- models
def element(lo, hi, textures, rotation=None, uv=None):
    faces = {}
    for face in ('north', 'south', 'east', 'west', 'up', 'down'):
        name = textures.get(face, textures.get('all'))
        if name is None:
            continue
        faces[face] = {'texture': '#' + name}
        if uv and face in uv:
            faces[face]['uv'] = uv[face]
    e = {'from': lo, 'to': hi, 'faces': faces}
    if rotation:
        e['rotation'] = rotation
    return e


def model(elements, textures, particle):
    return {'ambientocclusion': False, 'render_type': 'minecraft:cutout', 'textures': {**{k: P + v for k, v in textures.items()}, 'particle': P + particle}, 'elements': elements}


def mirror(turned):
    """A dressing mirror, its glass facing north, on a stand with two splayed feet; turned, the whole of it an eighth clockwise."""
    r = {'angle': -45, 'axis': 'y', 'origin': [8, 8, 8]} if turned else None
    els = [element([2, 0, 6], [14, 1, 10], {'all': 'f'}, r),             # the base board
           element([3, 1, 7], [4, 26, 9], {'all': 'f'}, r), element([12, 1, 7], [13, 26, 9], {'all': 'f'}, r),   # the uprights
           element([3, 26, 7], [13, 27, 9], {'all': 'f'}, r),             # the top rail
           element([4, 6, 7.5], [12, 26, 8.5], {'all': 'f', 'north': 'g'}, r)]   # the glass, its face to the north
    return model(els, {'f': 'frame', 'g': 'glass'}, 'frame')


def models():
    m = {}
    m['lamp'] = model([element([3, 3, 5], [13, 13, 16], {'all': 'e', 'north': 'lens'}), element([2, 13, 3], [14, 14, 9], {'all': 'e'}),
                       element([6, 0, 10], [10, 3, 16], {'all': 'b'})], {'e': 'enamel', 'lens': 'lens', 'b': 'brass'}, 'enamel')
    m['shutter_shut'] = model([element([0, 0, 7], [16, 16, 9], {'all': 's'}), element([7, 6, 6], [9, 9, 7], {'all': 'b'})], {'s': 'shutter', 'b': 'brass'}, 'shutter')
    m['shutter_open'] = model([element([0, 0, 6], [2, 16, 10], {'all': 's'}), element([14, 0, 6], [16, 16, 10], {'all': 's'}),
                               element([7, 3, 6], [9, 6, 7], {'all': 'b'})], {'s': 'shutter', 'b': 'brass'}, 'shutter')
    m['mirror'] = mirror(False)
    m['mirror_turned'] = mirror(True)
    for lit in (False, True):
        name = 'sun_' + ('lit' if lit else 'cold')
        m['plate_' + ('lit' if lit else 'cold')] = model([element([2, 2, 15], [14, 14, 16], {'all': 'p'}, uv={'north': [2, 2, 14, 14]})], {'p': name}, name)
    for stop in (False, True):
        name = 'track_stop' if stop else 'track'
        m[name] = model([element([0, 0, 0], [16, 16, 16], {'all': 'side', 'up': 'top'})], {'side': 'floor', 'top': name}, 'floor')
    win = model([element([0, 0, 6], [16, 2, 10], {'all': 'f'}), element([0, 14, 6], [16, 16, 10], {'all': 'f'}),
                 element([0, 2, 6], [2, 14, 10], {'all': 'f'}), element([14, 2, 6], [16, 14, 10], {'all': 'f'}),
                 element([2, 2, 7.5], [14, 14, 8.5], {'north': 'g', 'south': 'g'}, uv={'north': [2, 2, 14, 14], 'south': [2, 2, 14, 14]})],
                {'f': 'frame', 'g': 'leaded'}, 'frame')
    win['render_type'] = 'minecraft:translucent'
    m['window'] = win
    m['sconce'] = model([element([7, 4, 14], [9, 8, 16], {'all': 'b'}), element([5, 7, 11], [11, 12, 15], {'all': 'g', 'up': 'b'})], {'b': 'brass', 'g': 'shade'}, 'brass')
    return m


FACING_Y = {'north': 0, 'east': 90, 'south': 180, 'west': 270}
KINDS = ['lamp', 'shutter', 'mirror', 'plate', 'track', 'sconce', 'window']


def drawn(kind, turned, opened):
    if kind == 'shutter':
        return 'shutter_open' if opened else 'shutter_shut'
    if kind == 'mirror':
        return 'mirror_turned' if turned else 'mirror'
    if kind == 'plate':
        return 'plate_lit' if opened else 'plate_cold'
    if kind == 'track':
        return 'track_stop' if opened else 'track'
    return kind


def textures():
    out = {'wall.png': wallpaper(), 'floor.png': boards(), 'ceiling.png': plaster(), 'track.png': track(False), 'track_stop.png': track(True),
           'enamel.png': enamel(), 'lens.png': lens(), 'brass.png': brass(), 'shutter.png': shutter(), 'frame.png': frame(), 'glass.png': glass(),
           'sun_cold.png': sun(False), 'sun_lit.png': sun(True), 'shade.png': shade_glass(), 'beam.png': beam(), 'leaded.png': leaded()}
    return {'block/borrowed_light/' + k: v for k, v in out.items()}


def files():
    out = {}
    for name in ('wall', 'floor', 'ceiling'):
        out[f'models/block/borrowed_light_{name}.json'] = {'parent': 'minecraft:block/cube_all', 'textures': {'all': P + name}}
        out[f'blockstates/borrowed_light_{name}.json'] = {'variants': {'': {'model': NS + f'block/borrowed_light_{name}'}}}
    for key, obj in models().items():
        out[f'models/block/borrowed_light/{key}.json'] = obj
    variants = {}
    for kind in KINDS:
        for facing, y in FACING_Y.items():
            for turned in (False, True):
                for opened in (False, True):
                    v = {'model': NS + 'block/borrowed_light/' + drawn(kind, turned, opened)}
                    if y and kind != 'track':
                        v['y'] = y
                    variants[f'facing={facing},kind={kind},open={str(opened).lower()},turned={str(turned).lower()}'] = v
    out['blockstates/borrowed_light.json'] = {'variants': variants}
    return out


def main():
    check = argparse.ArgumentParser()
    check.add_argument('--check', action='store_true')
    check = check.parse_args().check
    images, data = textures(), files()
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
    print(('Verified' if check else 'Drew') + f' Borrowed Light: {len(images)} textures, {len(data)} models and states.')


if __name__ == '__main__':
    main()
