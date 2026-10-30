#!/usr/bin/env python3
"""The lifts' and landings' working parts (0.4.199): one puzzle_fitting block (PuzzleFittingBlock), drawn here.

The owner's puzzle rooms: "Worn handles, exposed pulleys, light spilling through cracks and furniture scrape marks can carry
the clues. A player should discover what moves by examining the room." So every part says what it does by how it looks:
cast-iron kitchen weights stamped with one, two or three bars and a ring to lift them by; a brass counterweight pan that
shows the weights in it; a dumbwaiter cage of dark oak and iron bars; a pulley wheel; a heavy capstan handle on its post,
its grip pointing round by quarters; a pale up cord, a dark down cord and a brass-ringed call cord; lift boards with an iron
rim; boards scraped in an arc where a landing swings and worn pale where it meets; a riveted counterweight.

Run from the repository root:
    python3 tools/draw_puzzle_fittings.py           # draw
    python3 tools/draw_puzzle_fittings.py --check   # verify the files match
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
P = NS + 'block/puzzle/'
MC = 'minecraft:block/'


def hexrgb(h):
    h = h.lstrip('#'); return tuple(int(h[i:i + 2], 16) for i in (0, 2, 4))


def shade(c, n):
    c = hexrgb(c) if isinstance(c, str) else c
    return tuple(max(0, min(255, v + n)) for v in c[:3]) + (255,)


def new(base, seed, grain):
    im = Image.new('RGBA', (16, 16)); px = im.load(); rnd = random.Random(seed)
    for x in range(16):
        for y in range(16):
            px[x, y] = shade(base, rnd.randint(-grain, grain))
    return im


# ---------------------------------------------------------------------------------------------------- textures
def cast_iron():
    """Pitted black-grey cast iron."""
    im = new('#3a3b3d', 401, 6); px = im.load(); rnd = random.Random(403)
    for _ in range(18):
        px[rnd.randint(0, 15), rnd.randint(0, 15)] = shade('#2a2b2d', 0)
    return im


def weight(n):
    """A weight's top: iron, a raised rim, and one, two or three stamped bars."""
    im = cast_iron(); px = im.load()
    for i in range(16):
        for e in (0, 15):
            px[i, e] = shade('#55575a', 0); px[e, i] = shade('#55575a', 0)
    xs = {1: [7], 2: [5, 9], 3: [4, 7, 10]}[n]
    for x in xs:
        for y in range(4, 12):
            px[x, y] = shade('#a8aaad', 0); px[x + 1, y] = shade('#7c7e81', 0)
    return im


def brass_pan():
    """The counterweight pan: dull brass, darker where weights have sat, a bright rim."""
    im = new('#9c7a36', 411, 6); px = im.load()
    for i in range(16):
        for e in (0, 15):
            px[i, e] = shade('#c9a656', 0); px[e, i] = shade('#c9a656', 0)
    for x in range(5, 11):
        for y in range(5, 11):
            px[x, y] = shade('#7c5f28', 0)
    return im


def pulley():
    """A pulley wheel: an iron rim, six spokes, the groove dark with grease."""
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0)); px = im.load()
    for x in range(16):
        for y in range(16):
            r = math.hypot(x - 7.5, y - 7.5)
            a = math.degrees(math.atan2(y - 7.5, x - 7.5)) % 60
            if 6.2 <= r <= 7.6:
                px[x, y] = shade('#2c2c2e', 0)
            elif 5.0 <= r < 6.2:
                px[x, y] = shade('#6b6d70', 0)
            elif r < 1.8:
                px[x, y] = shade('#8d8f92', 0)
            elif r < 5.0 and (a < 8 or a > 52):
                px[x, y] = shade('#55575a', 0)
    return im


def rope(base, seed):
    """Twisted rope: a diagonal lay, one strand lighter than the next."""
    im = new(base, seed, 4); px = im.load()
    for x in range(16):
        for y in range(16):
            if (x + y) % 4 == 0:
                px[x, y] = shade(base, -26)
            elif (x + y) % 4 == 1:
                px[x, y] = shade(base, 14)
    return im


def handle_wood():
    """The handle's oak, black with hands: darker where it is gripped, a pale crack along the grain."""
    im = new('#3b2a1d', 421, 5); px = im.load()
    for y in range(16):
        px[5, y] = shade('#4e3929', 0); px[11, (y + 3) % 16] = shade('#2c1f15', 0)
    return im


def boards(base, seed):
    """Dark boards with their seams."""
    im = new(base, seed, 6); px = im.load()
    for y in (3, 7, 11, 15):
        for x in range(16):
            px[x, y] = shade(base, -28)
    for y, x in ((1, 6), (5, 12), (9, 3), (13, 9)):
        px[x, y] = shade(base, -36)
    return im


def platform():
    """The lift's boards inside an iron rim, a bolt in each corner."""
    im = boards('#5a4130', 431); px = im.load()
    for i in range(16):
        for e in (0, 15):
            px[i, e] = shade('#45464a', 0); px[e, i] = shade('#45464a', 0)
    for x, y in ((1, 1), (14, 1), (1, 14), (14, 14)):
        px[x, y] = shade('#8a8c90', 0)
    return im


def scraped():
    """Boards scraped pale in an arc, where the corner of a turning landing has dragged across them, again and again."""
    im = boards('#4a3526', 441); px = im.load()
    for x in range(16):
        for y in range(16):
            r = math.hypot(x + .5, y - 15.5)
            if 11.2 <= r <= 13.4:
                px[x, y] = shade('#8a6e55', -abs(round((r - 12.3) * 10)))
    return im


def worn():
    """Boards worn pale down the middle, where feet always cross."""
    im = boards('#4a3526', 451); px = im.load()
    for x in range(4, 12):
        for y in range(16):
            if y % 4 != 3:
                px[x, y] = shade('#6d5440', -abs(x - 7) * 3)
    return im


def counterweight():
    """The riveted iron counterweight."""
    im = new('#4a4b4e', 461, 5); px = im.load()
    for i in range(16):
        for e in (0, 15):
            px[i, e] = shade('#333436', 0); px[e, i] = shade('#333436', 0)
    for x in (2, 13):
        for y in (2, 7, 13):
            px[x, y] = shade('#7d7f82', 0)
    return im


def textures():
    out = {'cast_iron': cast_iron(), 'weight_1': weight(1), 'weight_2': weight(2), 'weight_3': weight(3), 'brass_pan': brass_pan(),
           'pulley': pulley(), 'rope_pale': rope('#cdbb92', 471), 'rope_dark': rope('#4a3a2c', 473), 'handle_wood': handle_wood(),
           'platform': platform(), 'scraped': scraped(), 'worn': worn(), 'counterweight': counterweight()}
    return {'block/puzzle/' + k + '.png': v for k, v in out.items()}


# ---------------------------------------------------------------------------------------------------- models
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


def model(elements, textures, particle):
    return {'ambientocclusion': False, 'render_type': 'minecraft:cutout', 'textures': {**textures, 'particle': textures[particle]}, 'elements': elements}


def cube(side, top=None, bottom=None):
    return {'parent': 'minecraft:block/cube_bottom_top', 'textures': {'side': side, 'top': top or side, 'bottom': bottom or side}}


def models():
    m = {}
    iron, oak, bars = P + 'cast_iron', MC + 'dark_oak_planks', MC + 'iron_bars'
    for n in (1, 2, 3):
        h = 2 + 2 * n
        m[f'weight_{n}'] = model([element([5, 0, 5], [11, h, 11], {'all': 'i', 'up': 't'}), element([7, h, 7.5], [9, h + 1.5, 8.5], {'all': 'i'})],
                                 {'i': iron, 't': P + f'weight_{n}'}, 'i')
    for n in range(4):
        els = [element([2, 4, 2], [14, 5, 14], {'all': 'b'}), element([2, 5, 2], [14, 6, 3], {'all': 'b'}), element([2, 5, 13], [14, 6, 14], {'all': 'b'}),
               element([2, 5, 3], [3, 6, 13], {'all': 'b'}), element([13, 5, 3], [14, 6, 13], {'all': 'b'}),
               element([7.5, 6, 7.5], [8.5, 16, 8.5], {'all': 'r'})]
        for k in range(n):
            x = 4 + 3 * k
            els.append(element([x, 5, 6], [x + 2.5, 8 + k, 10], {'all': 'i'}))
        m[f'pan_{n}'] = model(els, {'b': P + 'brass_pan', 'r': P + 'rope_dark', 'i': iron}, 'b')
    for upper in (False, True):
        els = [element([1, 0, 1], [3, 16, 3], {'all': 'o'}), element([13, 0, 1], [15, 16, 3], {'all': 'o'}),
               element([1, 0, 13], [3, 16, 15], {'all': 'o'}), element([13, 0, 13], [15, 16, 15], {'all': 'o'}),
               element([3, 0, 1.5], [13, 16, 2.5], {'north': 'x', 'south': 'x'}), element([3, 0, 13.5], [13, 16, 14.5], {'north': 'x', 'south': 'x'}),
               element([1.5, 0, 3], [2.5, 16, 13], {'east': 'x', 'west': 'x'}), element([13.5, 0, 3], [14.5, 16, 13], {'east': 'x', 'west': 'x'})]
        if upper:
            els += [element([1, 14, 1], [15, 16, 15], {'all': 'o'}), element([7, 16, 7], [9, 16.5, 9], {'all': 'i'})]
        else:
            els += [element([1, 0, 1], [15, 2, 15], {'all': 'o'})]
        m['cage_' + ('upper' if upper else 'lower')] = model(els, {'o': oak, 'x': bars, 'i': iron}, 'o')
    m['pulley'] = model([element([2, 2, 7], [14, 14, 9], {'north': 'w', 'south': 'w', 'up': 'i', 'down': 'i', 'east': 'i', 'west': 'i'}),
                         element([7, 7, 5], [9, 9, 11], {'all': 'i'}), element([7, 7, 11], [9, 9, 16], {'all': 'i'})],
                        {'w': P + 'pulley', 'i': iron}, 'i')
    for n in range(4):
        # The capstan's arm points north, east, south, west as it goes round; the grip stands at the arm's end.
        arm = [element([7, 13, 1], [9, 14.5, 8], {'all': 'h'}), element([7, 13, 8], [15, 14.5, 9], {'all': 'h'}),
               element([7, 13, 8], [9, 14.5, 15], {'all': 'h'}), element([1, 13, 7], [8, 14.5, 9], {'all': 'h'})][n]
        grip = [element([7, 14.5, 1], [9, 18, 3], {'all': 'h'}), element([13, 14.5, 7], [15, 18, 9], {'all': 'h'}),
                element([7, 14.5, 13], [9, 18, 15], {'all': 'h'}), element([1, 14.5, 7], [3, 18, 9], {'all': 'h'})][n]
        m[f'handle_{n}'] = model([element([6, 0, 6], [10, 13, 10], {'all': 'h'}), element([5, 12, 5], [11, 15, 11], {'all': 'i', 'up': 'h'}), arm, grip],
                                 {'h': P + 'handle_wood', 'i': iron}, 'h')
    for name, r, t in (('cord_up', 'rope_pale', 'rope_pale'), ('cord_down', 'rope_dark', 'rope_dark'), ('call', 'rope_dark', 'brass_pan')):
        # The cord hangs from an iron arm off the wall behind it (north, before the facing turns it).
        m[name] = model([element([7.5, 4, 7.5], [8.5, 14.5, 8.5], {'all': 'r'}), element([6.5, 1, 6.5], [9.5, 4, 9.5], {'all': 't'}),
                         element([7.5, 14.5, 0.5], [8.5, 15.5, 8.5], {'all': 'i'}), element([6.5, 13, 0], [9.5, 16, 0.5], {'all': 'i'})],
                        {'r': P + r, 't': P + t, 'i': iron}, 'r')
    m['platform'] = cube(P + 'platform', P + 'platform', P + 'platform')
    m['scraped'] = cube(MC + 'dark_oak_planks', P + 'scraped', MC + 'dark_oak_planks')
    m['worn'] = cube(MC + 'dark_oak_planks', P + 'worn', MC + 'dark_oak_planks')
    m['counterweight'] = cube(P + 'counterweight')
    return m


FACING_Y = {'north': 0, 'east': 90, 'south': 180, 'west': 270}
KINDS = ['weight', 'pan', 'cage', 'pulley', 'handle', 'cord_up', 'cord_down', 'call', 'platform', 'scraped', 'worn', 'counterweight']


def drawn(kind, stage):
    if kind == 'weight':
        return f'weight_{max(1, stage)}'
    if kind == 'pan':
        return f'pan_{stage}'
    if kind == 'cage':
        return 'cage_upper' if stage else 'cage_lower'
    if kind == 'handle':
        return f'handle_{stage}'
    return kind


def files():
    out = {}
    for key, obj in models().items():
        out[f'models/block/puzzle/{key}.json'] = obj
    variants = {}
    for kind in KINDS:
        for facing, y in FACING_Y.items():
            for stage in range(4):
                v = {'model': NS + 'block/puzzle/' + drawn(kind, stage)}
                if y:
                    v['y'] = y
                variants[f'facing={facing},kind={kind},stage={stage}'] = v
    out['blockstates/puzzle_fitting.json'] = {'variants': variants}
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
    print(('Verified' if check else 'Drew') + f' the puzzle fittings: {len(images)} textures, {len(data)} models and states.')


if __name__ == '__main__':
    main()
