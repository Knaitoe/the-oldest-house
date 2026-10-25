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
    for x, y in ((7, 7), (8, 7), (7, 6), (7, 5), (7, 4), (8, 8), (9, 9), (10, 10)):
        px(img, x, y, (24, 20, 18))
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
    return {'from': list(frm), 'to': list(to), 'faces': f}


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


@spec('plaque', {'b': 'plaque'}, 'plaque')
def m_plaque():
    return [box((4, 5, 15), (12, 12, 16), 'b', uv=(2, 2, 14, 13))]


TURN = {'north': 0, 'east': 90, 'south': 180, 'west': 270}


def model_json(name):
    fn, textures, particle = MODEL_SPECS[name]
    tex = {k: NS + v for k, v in textures.items()}
    tex['particle'] = NS + particle
    return {'parent': 'minecraft:block/block', 'render_type': 'minecraft:cutout', 'ambientocclusion': False, 'textures': tex, 'elements': fn()}



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
