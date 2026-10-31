#!/usr/bin/env python3
"""Zampano's courtyard at vanilla scale (0.4.201; owner: "other zampanos textures should be naturalized to look more in-line
with vanilla minecraft").

The scrawled sheets on his wall, his plaster and the gouges were 64-pixel paintings, so they read as photographs pasted into
the world. These are sixteen-pixel versions with vanilla's few flat tones, drawn here, and the block models now name them.
The 64-pixel sheets stay in the pack unchanged (the historical texture inventory holds them); nothing draws them any more.
"""
import argparse, io, json, random
from pathlib import Path
from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
A = ROOT / 'src/main/resources/assets/the_oldest_house'
NS = 'the_oldest_house:block/'
PAPER, PAPER_DARK, PAPER_LIGHT = (214, 200, 164), (190, 174, 136), (228, 218, 188)
INK, INK_SOFT, RUST = (46, 34, 28), (92, 74, 60), (120, 52, 40)


def sheet(seed):
    rnd = random.Random(seed)
    im = Image.new('RGBA', (16, 16), PAPER + (255,))
    for x in range(16):
        for y in range(16):
            r = rnd.randrange(18)
            if r == 0:
                im.putpixel((x, y), PAPER_DARK + (255,))
            elif r == 1:
                im.putpixel((x, y), PAPER_LIGHT + (255,))
    return im, rnd


def line(im, rnd, y, x0=2, x1=13, colour=INK, gaps=True):
    """A line of hand: a wavering run of ink with gaps between words."""
    x = x0
    while x <= x1:
        word = rnd.randint(2, 4)
        for i in range(word):
            if x > x1:
                break
            dy = rnd.choice((0, 0, 0, -1, 1))
            if 0 <= y + dy < 16:
                im.putpixel((x, y + dy), colour + (255,))
            x += 1
        x += rnd.choice((1, 1, 2)) if gaps else 0


def scrawl(i):
    """Sixteen of his sheets: lines of hand, written over, crossed out, spiralled, run off the edge."""
    im, rnd = sheet(20170 + i)
    kind = i % 4
    rows = [3, 6, 9, 12] if i % 3 else [2, 5, 8, 11, 14]
    for y in rows:
        line(im, rnd, y)
    if kind == 0:
        # A line struck through.
        y = rnd.choice(rows)
        for x in range(2, 14):
            im.putpixel((x, y), INK + (255,))
    elif kind == 1:
        # Written over itself, a second pass in rust a row off.
        for y in rows:
            line(im, rnd, y + 1, colour=RUST)
    elif kind == 2:
        # A spiral pressed into the page.
        cx, cy = rnd.randint(6, 9), rnd.randint(6, 9)
        for x, y in ((0, -3), (1, -3), (2, -2), (3, -1), (3, 0), (3, 1), (2, 2), (1, 3), (0, 3), (-1, 3), (-2, 2), (-3, 1),
                     (-3, 0), (-2, -1), (-1, -1), (0, -1), (1, 0), (0, 1), (-1, 1)):
            im.putpixel((cx + x, cy + y), INK + (255,))
    else:
        # A margin ruled down the side, and the hand running past it to the edge.
        for y in range(16):
            im.putpixel((3, y), INK_SOFT + (255,))
        for y in rows[:2]:
            line(im, rnd, y, 4, 15, gaps=False)
    if i % 5 == 0:
        im.putpixel((8, 0), (150, 150, 150, 255))  # the pin
    return im


def plaster():
    """Old cream plaster, a hairline crack and a few chips, at vanilla's scale."""
    rnd = random.Random(20180)
    base = (206, 196, 172)
    im = Image.new('RGBA', (16, 16), base + (255,))
    for x in range(16):
        for y in range(16):
            r = rnd.randrange(9)
            if r == 0:
                im.putpixel((x, y), (192, 182, 158, 255))
            elif r == 1:
                im.putpixel((x, y), (216, 208, 186, 255))
    for x, y in ((3, 2), (4, 3), (4, 4), (5, 5), (5, 6), (6, 7), (11, 11), (12, 12), (12, 13)):
        im.putpixel((x, y), (160, 150, 128, 255))
    for x, y in ((9, 4), (2, 12)):
        im.putpixel((x, y), (178, 168, 146, 255))
    return im


def gouges():
    """Deepslate tile scored by four even grooves, the fifth's place left bare."""
    rnd = random.Random(20181)
    im = Image.new('RGBA', (16, 16))
    for x in range(16):
        for y in range(16):
            edge = x % 8 == 0 or y % 8 == 0
            tone = 46 if edge else rnd.choice((70, 74, 78, 82))
            im.putpixel((x, y), (tone, tone, tone + 4, 255))
    for k in range(4):
        for t in range(10):
            x, y = 3 + k * 3 + t // 4, 3 + t
            if x < 16 and y < 16:
                im.putpixel((x, y), (150, 148, 150, 255))
                if x + 1 < 16:
                    im.putpixel((x + 1, y), (110, 108, 112, 255))
    return im


def scribble_model(i):
    """The sheet's own model: a thin panel, unchanged in shape, naming the new sheet."""
    faces = {f: {'uv': [0, 0, 16, 16], 'texture': '#paper'} for f in ('north', 'south', 'east', 'west', 'up', 'down')}
    return {'ambientocclusion': False, 'textures': {'paper': NS + f'zampano_sheet_{i}', 'particle': NS + f'zampano_sheet_{i}'},
            'elements': [{'from': [1, 0, 1], 'to': [15, 0.25, 15], 'faces': faces}]}


def files():
    out = {}
    images = {f'zampano_sheet_{i}': scrawl(i) for i in range(16)}
    images['zampano_plaster'] = plaster()
    images['zampano_gouges'] = gouges()
    for name, im in images.items():
        b = io.BytesIO(); im.save(b, format='PNG'); out[A / f'textures/block/{name}.png'] = b.getvalue()
    for i in range(16):
        out[A / f'models/block/zampano_scribble_{i}.json'] = json.dumps(scribble_model(i), indent=2) + '\n'
    out[A / 'models/block/collapse_plaster.json'] = json.dumps({'parent': 'minecraft:block/cube_all', 'textures': {'all': NS + 'zampano_plaster'}}, indent=2) + '\n'
    out[A / 'models/block/cell_gouges.json'] = json.dumps({'parent': 'minecraft:block/cube_all', 'textures': {'all': NS + 'zampano_gouges'}}, indent=2) + '\n'
    return out


def main():
    p = argparse.ArgumentParser(); p.add_argument('--check', action='store_true'); check = p.parse_args().check
    for path, data in files().items():
        if check:
            assert path.exists(), path
            if isinstance(data, str):
                assert json.loads(path.read_text()) == json.loads(data), path
            else:
                assert Image.open(path).convert('RGBA').tobytes() == Image.open(io.BytesIO(data)).convert('RGBA').tobytes(), path
        else:
            path.parent.mkdir(parents=True, exist_ok=True)
            path.write_text(data) if isinstance(data, str) else path.write_bytes(data)
    print(('Verified' if check else 'Drew') + " Zampano's sixteen sheets, his plaster and the gouges at vanilla scale.")


if __name__ == '__main__':
    main()
