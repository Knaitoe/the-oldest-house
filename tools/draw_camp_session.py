#!/usr/bin/env python3
"""Camp Blood's last session (0.4.121): vanilla-style 16x16 textures and the camp_session block's models and states.

The things the camp's papers and its schedule need: the camp radio that reads the PA, the horn on its pole, a counselor's
dropped flashlight, the mess hall's trestle tables and benches, the director's clipboard, the schedule on its stand by the
flagpole, a journal on a crate, and a plank with initials cut into it. Each texture is drawn pixel by pixel from a small
palette with one-pixel bevels and scattered noise, as vanilla's are; nothing is sampled. Run with --check to confirm the
committed files.
"""
import argparse
import json
import random
import sys
from pathlib import Path

from PIL import Image

ROOT = Path(__file__).resolve().parents[1] / 'src/main/resources/assets/the_oldest_house'
TEX = ROOT / 'textures/block/camp_session'
MODELS = ROOT / 'models/block/camp_session'
STATES = ROOT / 'blockstates'
NS = 'the_oldest_house:block/camp_session/'


def clamp(c):
    return tuple(max(0, min(255, round(v))) for v in c)


def mul(c, f):
    return clamp(tuple(v * f for v in c[:3]))


def canvas():
    return Image.new('RGBA', (16, 16), (0, 0, 0, 0))


def px(img, x, y, c, a=255):
    if 0 <= x < 16 and 0 <= y < 16:
        img.putpixel((x, y), clamp(c) + (a,))


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


def pine(seed, base=(176, 132, 84)):
    """Weathered pine boards running across, with dark joints and a knot or two."""
    rng = random.Random(seed)
    img = canvas()
    for y in range(16):
        tone = (1.0, .93, 1.05, .96)[y // 4]
        for x in range(16):
            px(img, x, y, mul(base, tone + rng.choice((0, 0, -.03, .03)) + (.05 if (x * 7 + y * 3) % 19 == 0 else 0)))
        if y % 4 == 3:
            for x in range(16):
                px(img, x, y, mul(base, .58))
    for kx, ky in ((4, 1), (11, 9)):
        px(img, kx, ky, mul(base, .6)); px(img, kx + 1, ky, mul(base, .7))
    return img


# ------------------------------------------------------------------------------------------------ textures
def radio():
    """The camp radio's face: brown bakelite, cloth over the speaker, a lit dial with its needle, two knobs."""
    img = ground((92, 58, 34), 3101, .03)
    for y in range(2, 9):
        for x in range(2, 14):
            px(img, x, y, (150, 128, 96) if (x + y) % 2 else (132, 110, 80))
    bevel(img, 1, 1, 14, 9, .7, 1.2)
    for x in range(3, 13):
        px(img, x, 11, (230, 196, 120)); px(img, x, 12, (214, 176, 98))
    px(img, 8, 11, (180, 40, 30)); px(img, 8, 12, (180, 40, 30))
    for kx in (3, 12):
        px(img, kx, 14, (40, 26, 16)); px(img, kx + 1, 14, (58, 38, 22))
    return img


def bakelite():
    return ground((92, 58, 34), 3102, .035)


def horn():
    """The PA horn: grey enamel, rust coming through at the rim."""
    img = ground((150, 154, 150), 3103, .04)
    rng = random.Random(3113)
    for _ in range(9):
        px(img, rng.randrange(16), rng.choice((0, 1, 14, 15)), (138, 82, 50))
    for y in range(16):
        px(img, 0, y, (110, 114, 110)); px(img, 15, y, (176, 180, 176))
    return img


def horn_mouth():
    """Inside the horn's mouth: dark, a mesh disc at the throat."""
    img = canvas()
    for y in range(16):
        for x in range(16):
            d = max(abs(x - 7.5), abs(y - 7.5))
            px(img, x, y, (150, 154, 150) if d > 6.5 else (52, 54, 56) if d > 3 else (30, 30, 32) if (x + y) % 2 else (74, 76, 78))
    return img


def flashlight():
    """A counselor's flashlight: chrome tube, a red rubber grip, a switch."""
    img = canvas()
    for y in range(16):
        for x in range(16):
            band = (186, 192, 198) if y in (5, 6) else (120, 126, 132) if y in (12, 13) else (160, 166, 170)
            if 7 <= x <= 11:
                band = (170, 42, 36) if y % 2 else (150, 34, 30)
            px(img, x, y, band)
    px(img, 4, 7, (40, 40, 40)); px(img, 4, 8, (40, 40, 40))
    return img


def lens():
    img = ground((236, 232, 200), 3105, .02)
    for i in range(16):
        px(img, i, 0, (160, 166, 170)); px(img, i, 15, (120, 126, 132)); px(img, 0, i, (160, 166, 170)); px(img, 15, i, (120, 126, 132))
    return img


def table():
    """The mess hall's table top: scrubbed pine, initials and a heart scratched in, a ring from a cup."""
    img = pine(3106, (186, 150, 104))
    for x, y in ((3, 6), (4, 5), (5, 6), (4, 7)):
        px(img, x, y, (120, 88, 56))  # a heart
    for x, y in ((9, 13), (10, 13), (12, 13), (12, 14), (13, 13)):
        px(img, x, y, (116, 84, 52))  # T.R.
    for x, y in ((10, 2), (11, 1), (12, 1), (13, 2), (13, 3), (12, 4), (11, 4), (10, 3)):
        px(img, x, y, (160, 124, 82))
    return img


def clipboard():
    """The director's clipboard: brown hardboard, a typed form under the clip, a box at the foot unsigned."""
    img = ground((132, 96, 62), 3107, .03)
    for y in range(2, 15):
        for x in range(2, 14):
            px(img, x, y, (238, 234, 222))
    for y in (4, 6, 8, 10):
        for x in range(3, 13):
            if (x * 5 + y) % 6:
                px(img, x, y, (60, 60, 70))
    for x in range(8, 13):
        px(img, x, 13, (140, 140, 150))
    px(img, 8, 12, (140, 140, 150)); px(img, 12, 12, (140, 140, 150))
    for x in range(5, 11):
        px(img, x, 0, (170, 174, 180)); px(img, x, 1, (140, 144, 150))
    return img


def board():
    """The schedule on its board: cork round a typed sheet, six lines and a heading, four drawing pins."""
    rng = random.Random(3108)
    img = canvas()
    for y in range(16):
        for x in range(16):
            px(img, x, y, mul((170, 124, 78), 1 + rng.uniform(-.08, .08)))
    for y in range(1, 15):
        for x in range(2, 14):
            px(img, x, y, (240, 236, 222))
    for x in range(4, 12):
        px(img, x, 2, (30, 30, 40))
    for y in (5, 7, 9, 11, 13):
        for x in range(3, 13):
            px(img, x, y, (70, 70, 84) if x < 8 or x > 9 else (200, 196, 182))
    for x, y in ((2, 1), (13, 1), (2, 14), (13, 14)):
        px(img, x, y, (196, 40, 40))
    bevel(img, 0, 0, 15, 15, 1.1, .7)
    return img


def journal():
    """A counselor's journal: green cloth, a white label, pencil writing on it."""
    img = ground((70, 104, 74), 3109, .05)
    for y in range(5, 10):
        for x in range(3, 13):
            px(img, x, y, (232, 226, 206))
    for x in range(4, 12):
        if x % 3:
            px(img, x, 7, (90, 90, 96))
    for y in range(16):
        px(img, 0, y, (46, 70, 50)); px(img, 1, y, (56, 84, 60))
    return img


def crate():
    """A footlocker-size crate: pine slats, a stencilled number."""
    img = pine(3110, (168, 128, 82))
    for y in range(16):
        px(img, 0, y, (120, 88, 56)); px(img, 15, y, (120, 88, 56))
    for x, y in ((6, 6), (7, 6), (8, 6), (8, 7), (7, 8), (8, 9), (6, 10), (7, 10), (8, 10)):
        px(img, x, y, (60, 44, 30))
    return img


def initials():
    """A cabin plank with a summer's worth of names cut into it: initials, a heart, a date, tally marks."""
    img = pine(3111, (150, 112, 74))
    cut = (92, 64, 40)
    for x, y in ((2, 1), (2, 2), (3, 2), (2, 3), (5, 1), (5, 2), (5, 3), (6, 1), (6, 3)):
        px(img, x, y, cut)  # L.B.
    for x, y in ((10, 5), (11, 4), (12, 5), (11, 6), (13, 4), (14, 5), (13, 6)):
        px(img, x, y, cut)
    for x in (2, 4, 6, 8):
        for y in range(9, 12):
            px(img, x, y, cut)
    for x in range(1, 10):
        px(img, x, 10, cut)  # struck through, a full week
    for x, y in ((11, 12), (12, 12), (13, 13), (12, 14), (11, 14)):
        px(img, x, y, cut)
    return img


TEXTURES = {'radio': radio, 'bakelite': bakelite, 'horn': horn, 'horn_mouth': horn_mouth, 'flashlight': flashlight, 'lens': lens,
            'table': table, 'clipboard': clipboard, 'board': board, 'journal': journal, 'crate': crate, 'initials': initials}


# ------------------------------------------------------------------------------------------------ models
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
@spec('radio', {'b': 'bakelite', 'r': 'radio', 't': 'table'}, 'bakelite')
def m_radio():
    return [box((1, 0, 1), (15, 8, 15), 't'), box((3, 8, 5), (13, 15, 12), 'b', over={'north': 'r'}),
            box((6, 15, 7.5), (10, 15.5, 9.5), 'b')]


@spec('pa_horn', {'h': 'horn', 'm': 'horn_mouth'}, 'horn')
def m_pa_horn():
    return [box((7.25, 0, 7.25), (8.75, 11, 8.75), 'h'), box((6.5, 10, 8), (9.5, 13, 11), 'h'), box((5.5, 9, 5), (10.5, 14, 8), 'h'),
            box((4, 7.5, 1), (12, 15.5, 5), 'h', over={'north': 'm'})]


@spec('flashlight', {'f': 'flashlight', 'l': 'lens'}, 'flashlight')
def m_flashlight():
    return [box((3, 0, 7), (11, 2.5, 9.5), 'f', uv=(0, 0, 16, 5)), box((11, 0, 6.5), (13.5, 3, 10), 'f', over={'east': 'l'})]


@spec('mess_table', {'t': 'table'}, 'table')
def m_mess_table():
    return [box((0, 13, 0.5), (16, 15, 15.5), 't'), box((1, 0, 6.5), (3, 13, 9.5), 't'), box((13, 0, 6.5), (15, 13, 9.5), 't'),
            box((3, 3, 7), (13, 4.5, 9), 't')]


@spec('bench', {'t': 'table'}, 'table')
def m_bench():
    return [box((0, 7, 4), (16, 9, 12), 't'), box((1, 0, 5), (3, 7, 11), 't'), box((13, 0, 5), (15, 7, 11), 't')]


@spec('clipboard', {'c': 'clipboard'}, 'clipboard')
def m_clipboard():
    return [box((4, 0, 3), (12, 0.5, 13), 'c', uv=(0, 0, 16, 16))]


@spec('schedule_stand', {'b': 'board', 't': 'table'}, 'board')
def m_schedule_stand():
    return [box((1, 0, 7), (2.5, 16, 9), 't'), box((13.5, 0, 7), (15, 16, 9), 't'), box((1, 4, 7.5), (15, 16, 8.5), 't', over={'north': 'b'}),
            box((0, 16, 6), (16, 17, 10), 't')]


@spec('journal', {'c': 'crate', 'j': 'journal'}, 'journal')
def m_journal():
    return [box((2, 0, 2), (14, 10, 14), 'c'), box((4, 10, 5), (11, 11.5, 11), 'j')]


@spec('initials', {'i': 'initials'}, 'initials')
def m_initials():
    return [box((0, 0, 15.5), (16, 16, 16), 'i', uv=(0, 0, 16, 16))]


KINDS = list(MODEL_SPECS)
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
    for name in KINDS:
        out[MODELS / f'{name}.json'] = json.dumps(model_json(name), indent=1) + '\n'
    variants = {}
    for facing, y in TURN.items():
        for kind in KINDS:
            v = {'model': NS + kind}
            if y:
                v['y'] = y
            variants[f'facing={facing},kind={kind}'] = v
    out[STATES / 'camp_session.json'] = json.dumps({'variants': variants}, indent=1) + '\n'
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
    print(f'{"Verified" if args.check else "Drew"} {len(TEXTURES)} camp session textures and {len(KINDS)} kinds.')


if __name__ == '__main__':
    main()
