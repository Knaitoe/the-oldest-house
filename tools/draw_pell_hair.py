#!/usr/bin/env python3
"""0.4.568 (docs/PELL_HOUSE_NOTES_0_4_568.md): the hair that grows on Tilly at the Pell house, and Nessa's, as client.PellHair draws it
over the cast model.

Tilly's skin paints her hair as she came, to her jaw. A week in it is past her shoulders, and on the last night it is down her back, as
long as Nessa's. These are the sheets for what hangs below the head: four strands down the back and a lock over each shoulder, each
in a few links, at both lengths, laid out on a 64 by 32 sheet as PellHair's cubes take it (its lengths and places are read from the
game, and this fails if they disagree). The hair is the skins' own (tools/paint_character_skins.py: Tilly's brown, Nessa's near black
with frost in it), drawn at the skins' grid: four pixels to the model's pixel, the streaks two pixels wide. Each strand's streaks run on
unbroken from link to link; the side toward the body is in shadow; the ends are ragged, and the end faces are left open so the
raggedness shows.

Run it to draw; --check verifies the pack against it (CI runs that).
"""
import argparse
import random
import re
import sys
from pathlib import Path

from PIL import Image

ROOT = Path(__file__).resolve().parent.parent
A = ROOT / 'src/main/resources/assets/the_oldest_house'
JAVA = ROOT / 'src/main/java/io/github/knaitoe/theoldesthouse/client/PellHair.java'
SHEET = (64, 32)
SCALE = 4
STRANDS, LINKS, LOCKS, LOCK_LINKS = 4, 3, 2, 2
STRAND_W, STRAND_D, LOCK_W, LOCK_D = 2, .8, 2.2, .6
BACK = [[], [4.2, 4.8, 4.5, 3.9], [10.2, 10.9, 11.2, 10.4]]
FRONT = [[], [2.4, 2.2], [6.2, 5.8]]
LOCK_V = 24

# The skins' own hair (tools/paint_character_skins.py, measured on literary_pell_tilly.png and literary_pell_nessa.png).
PEOPLE = {
    'tilly': dict(base=(0xb5, 0x89, 0x5a), light=(0xbe, 0x92, 0x63), dark=(0xa9, 0x7d, 0x4e), frost=None),
    'nessa': dict(base=(0x2a, 0x1e, 0x18), light=(0x33, 0x27, 0x21), dark=(0x1e, 0x12, 0x0c), frost=[(0xd6, 0xe2, 0xe8), (0xf2, 0xf8, 0xfa)]),
}


def back_u(length, strand):
    return (length - 1) * 32 + strand * 8


def back_v(link):
    return link * 8


def lock_u(length, lock, link):
    return (length - 1) * 32 + lock * 16 + link * 8


def java():
    """The lengths and places as the game has them."""
    src = JAVA.read_text()

    def floats(name):
        m = re.search(name + r'=\{\{\},(\{[^}]*\}),(\{[^}]*\})\}', src)
        assert m, name
        return [[]] + [[float(v.rstrip('F')) for v in g.strip('{}').split(',')] for g in m.groups()]

    def number(name):
        m = re.search(r'\b' + name + r'=(-?[\d.]+)F?[,;]', src)
        assert m, name
        return float(m.group(1))

    found = dict(BACK=floats('BACK'), FRONT=floats('FRONT'), STRANDS=number('STRANDS'), LINKS=number('LINKS'), LOCKS=number('LOCKS'),
                 LOCK_LINKS=number('LOCK_LINKS'), STRAND_W=number('STRAND_W'), STRAND_D=number('STRAND_D'), LOCK_W=number('LOCK_W'),
                 LOCK_D=number('LOCK_D'), LOCK_V=number('LOCK_V'))
    assert 'return (length-1)*32+strand*8;' in src and 'return link*8;' in src and 'return (length-1)*32+lock*16+link*8;' in src
    return found


def shade(rgb, k):
    return tuple(max(0, min(255, round(c * k))) for c in rgb)


def cube(im, rnd, look, strand_id, u0, v0, w, h, d, along0, tip, outward):
    """One link's net, as ModelPart's cube lays it out: the root and end faces along the top row, then west, north, east and south.
    Streaks are keyed by the column round the strand and by the distance along it, so a strand runs on unbroken from link to link."""
    px = im.load()
    s = SCALE

    def rect(x0, y0, x1, y1):
        return range(int(round(x0 * s)), int(round(x1 * s))), range(int(round(y0 * s)), int(round(y1 * s)))

    faces = {  # name: (u from, u to), round the strand in order
        'west': (u0, u0 + d), 'north': (u0 + d, u0 + d + w), 'east': (u0 + d + w, u0 + 2 * d + w), 'south': (u0 + 2 * d + w, u0 + 2 * d + 2 * w),
    }
    streak = random.Random(strand_id * 7919)
    # Column colours round the strand, two pixels to a streak, the same in every link of it; down each, now and then a stretch goes a
    # shade the other way, so the streaks run long and break here and there rather than speckle.
    columns = []
    for _ in range(32):
        main = streak.choice(('base', 'base', 'base', 'light', 'dark', 'base', 'light'))
        runs, at = [], 0.0
        while at < 14:
            at += streak.uniform(1.5, 4.5)
            runs.append((at, main if streak.random() < .7 else ('light' if main == 'dark' else 'dark' if main == 'light' else streak.choice(('light', 'dark')))))
        columns.append(runs)

    def tone_at(col, along):
        for end, tone in columns[col % len(columns)]:
            if along < end:
                return tone
        return columns[col % len(columns)][-1][1]
    cut = [rnd.choice((0, 1, 2, 3, 4, 5, 6, 2, 3)) for _ in range(64)]
    for name, (a, b) in faces.items():
        xs, ys = rect(a, v0 + d, b, v0 + d + h)
        for x in xs:
            col = (x - int(round(a * s))) // 2 + {'west': 0, 'north': 4, 'east': 12, 'south': 16}[name]
            for y in ys:
                along = along0 + (y - ys.start) / s
                c = look[tone_at(col, along)]
                # The side toward the body is in shadow.
                if name == ('north' if outward == 'south' else 'south'):
                    c = shade(c, .82)
                elif name in ('west', 'east'):
                    c = shade(c, .92)
                px[x, y] = c + (255,)
                if look['frost'] and name != ('north' if outward == 'south' else 'south') and rnd.random() < .035:
                    px[x, y] = rnd.choice(look['frost']) + (255,)
        if tip:
            # Ragged at the end: each pair of columns stops short by its own few pixels, never more than half the link, so a short end
            # is ragged without coming apart.
            most = max(1, int(h * s * .5))
            for x in xs:
                c = min(most, cut[((x - xs.start) // 2 + len(name)) % len(cut)])
                for y in range(ys.stop - c, ys.stop):
                    px[x, y] = (0, 0, 0, 0)
    # The root face, under the head or on the link above: hair. The end face: open on the last link, so the ends are ragged.
    xs, ys = rect(u0 + d, v0, u0 + d + w, v0 + d)
    for x in xs:
        for y in ys:
            px[x, y] = shade(look['base'], .9) + (255,)
    xs, ys = rect(u0 + d + w, v0, u0 + d + 2 * w, v0 + d)
    for x in xs:
        for y in ys:
            px[x, y] = (0, 0, 0, 0) if tip else shade(look['base'], .9) + (255,)


def drawn():
    images = {}
    for name, look in PEOPLE.items():
        im = Image.new('RGBA', (SHEET[0] * SCALE, SHEET[1] * SCALE), (0, 0, 0, 0))
        rnd = random.Random(name)
        for length in (1, 2):
            for k in range(STRANDS):
                link = BACK[length][k] / LINKS
                for j in range(LINKS):
                    cube(im, rnd, look, 100 * length + k, back_u(length, k), back_v(j), STRAND_W, link, STRAND_D, j * link, j == LINKS - 1, 'south')
            for m in range(LOCKS):
                link = FRONT[length][m] / LOCK_LINKS
                for j in range(LOCK_LINKS):
                    cube(im, rnd, look, 100 * length + 10 + m, lock_u(length, m, j), LOCK_V, LOCK_W, link, LOCK_D, j * link, j == LOCK_LINKS - 1, 'north')
        images[f'pell_hair_{name}'] = im
    return images


def main():
    check = argparse.ArgumentParser()
    check.add_argument('--check', action='store_true')
    check = check.parse_args().check
    found = java()
    want = dict(BACK=BACK, FRONT=FRONT, STRANDS=STRANDS, LINKS=LINKS, LOCKS=LOCKS, LOCK_LINKS=LOCK_LINKS, STRAND_W=STRAND_W,
                STRAND_D=STRAND_D, LOCK_W=LOCK_W, LOCK_D=LOCK_D, LOCK_V=LOCK_V)
    for key, value in want.items():
        assert found[key] == value, (key, found[key], value)
    # Every cube's net fits its place on the sheet.
    for length in (1, 2):
        for k in range(STRANDS):
            assert 2 * (STRAND_D + STRAND_W) <= 8 and STRAND_D + BACK[length][k] / LINKS <= 8
        for m in range(LOCKS):
            assert 2 * (LOCK_D + LOCK_W) <= 8 and LOCK_D + FRONT[length][m] / LOCK_LINKS <= 8
    for name, im in drawn().items():
        path = A / 'textures/entity' / f'{name}.png'
        if check:
            with Image.open(path) as actual:
                assert actual.convert('RGBA').tobytes() == im.tobytes(), name
        else:
            im.save(path, optimize=True)
    print('checked' if check else 'drawn', 'the Pell hair sheets')


if __name__ == '__main__':
    sys.exit(main())
