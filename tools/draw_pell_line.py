#!/usr/bin/env python3
"""The Pell house's line (docs/PELL_HOUSE_PROPOSAL.md, "The line"), slice 3: a hand-width line of paint round the whole lot, drawn
into the top of the ground it lies on, at sixteen pixels to the block like the rest of the house (tools/draw_pell_house.py, whose
ground this paints on).

Each cell of the line is the ground block itself with the paint on its top, so its sides and underside are the ground's own texture.
How the paint has taken depends on the ground (PellLine.Surface): soaked into concrete; on the drive, over tyre marks that have been
painted over; beaded on pavers and run into their joins; sitting on the lawn's blades, with the grass dead under it; pooled in the
mulch, where a first coat sank in; flaking off deck boards, broken at every gap between them. Its age is the stage: as on the first
visit (older coats show at the chipped edges), the second (painted again), the last night (wet in places), the last night with a
footprint smeared through it, scrubbed half away, and scrubbed out, which leaves only its ghost in the ground.

Every cell is drawn as it lies and never turned, so the ground's joints, joins and boards stay true with the plain ground beside it:
a straight cell north to south or east to west, a corner by the two sides it joins, and the thin line behind the shed (on the lawn;
elsewhere the thin states show the straight line). The paint is drawn in one orientation and turned into place over the ground.

Run it to draw; --check verifies the pack against it (CI runs that). It reads PellLine's shapes, surfaces and stages from the game and
fails if they disagree, and keeps its two folders to what it draws.
"""
import argparse
import json
import math
import random
import re
import sys
from pathlib import Path

from PIL import Image

sys.path.insert(0, str(Path(__file__).resolve().parent))
from draw_pell_house import A, NS, ROOT, rgb  # noqa: E402
from draw_pell_house import materials as house_materials  # noqa: E402

SURFACES = ['concrete', 'tyre', 'paver', 'lawn', 'mulch', 'deck']
SHAPES = ['straight', 'corner', 'thin']
STAGES = 6
FIRST, SECOND, LAST, FOOTPRINT, HALF, GAP = range(STAGES)
FACINGS = ['north', 'east', 'south', 'west']
# A straight or thin cell lies north to south or east to west; a corner joins its facing and the side clockwise of it. The angle is
# how far the drawing is turned, clockwise, from its first orientation (north to south; north and east).
ORIENTS = {'straight': {'ns': 0, 'ew': 90}, 'thin': {'ns': 0, 'ew': 90}, 'corner': {'ne': 0, 'es': 90, 'sw': 180, 'wn': 270}}
FOLDER = 'block/pell/line'
JOIN = (0x96, 0x80, 0x6a, 255)    # the pavers' joins, as tools/draw_pell_house.py draws them
TREAD = (5, 10)                    # the rows a wheel crosses the line in, as drawn north to south
GAP_PX = (0x4f, 0x3d, 0x2b, 255)  # the gaps between the deck's boards


def ground(surface):
    """The plain ground this surface is, exactly as the house draws it (the drive's is concrete)."""
    return house_materials()['concrete' if surface == 'tyre' else surface].copy()


def turn(im, angle):
    """Turns a drawing clockwise by a right angle, as a block is turned from north to east: its north edge becomes its east."""
    out = Image.new('RGBA', (16, 16)); src = im.load(); dst = out.load()
    for y in range(16):
        for x in range(16):
            nx, ny = x, y
            for _ in range(angle // 90):
                nx, ny = 15 - ny, nx
            dst[nx, ny] = src[x, y]
    return out


def colour(c):
    return c if isinstance(c, tuple) else rgb(c) + (255,)


def mix(a, b, k):
    a, b = colour(a), colour(b)
    return tuple(int(round(a[i] + (b[i] - a[i]) * k)) for i in range(3)) + (255,)


def lighter(c, k):
    c = colour(c)
    return tuple(max(0, min(255, c[i] + k)) for i in range(3)) + (255,)


def geometry(shape):
    """For each pixel of a cell in its first orientation: its distance from the middle of the line, the side of the line it is on,
    and how far along the line it is."""
    out = {}
    for y in range(16):
        for x in range(16):
            if shape == 'corner':
                # North to the middle of the cell, then east.
                dv = abs(x - 7.5) if y <= 7.5 else math.hypot(x - 7.5, y - 7.5)
                dh = abs(y - 7.5) if x >= 7.5 else math.hypot(x - 7.5, y - 7.5)
                d, t = (dv, min(y, 7.5)) if dv <= dh else (dh, 7.5 + max(0.0, x - 7.5))
                side = 1 if (x >= 8 and y <= 7) else -1
            else:
                cx = 7 if shape == 'thin' else 7.5
                d, t, side = abs(x - cx), float(y), (1 if x > cx else -1)
            out[x, y] = (d, t, side)
    return out


# The paint's tones: the coat as it is, the older coats under it at its chipped edges, and fresh.
COAT = {FIRST: '#e2ded1', SECOND: '#eeebe3', LAST: '#f2f0ea', FOOTPRINT: '#f2f0ea', HALF: '#f2f0ea', GAP: '#f2f0ea'}
OLDER = ['#d3ccb7', '#e8e5dc', '#cdc6b0']
DEAD = ['#a8975a', '#9b8a50', '#b5a46a', '#8f7e48']


def paint(surface, shape, stage, base, seed):
    """The line on one cell of ground, in its first orientation (base is the ground already turned to match)."""
    rnd = random.Random(seed)
    geo = geometry(shape)
    thin = shape == 'thin'
    width = 0.5 if thin else 0.75
    coat = COAT[stage]
    under = base.load()
    line = sorted(p for p in geo if geo[p][0] < width)
    # The ground first, as the paint has left it: the lawn dead under and beside it, the mulch stained by the coat that sank in, the
    # drive's tyre marks across it.
    floor = base.copy(); fl = floor.load()
    for y in range(16):
        for x in range(16):
            d, g = geo[x, y][0], under[x, y]
            if surface == 'lawn':
                reach, near = (1.5, 1.0) if thin else (2.25, 1.25)
                if d < reach and (d < near or rnd.random() < .7):
                    fl[x, y] = mix(g, rnd.choice(DEAD), .85 if d < near else .55)
            elif surface == 'mulch' and d < 1.75 and rnd.random() < .75:
                fl[x, y] = mix(g, '#7a6c5c', .5)
            elif surface == 'tyre' and TREAD[0] <= y <= TREAD[1] and d < 2.5 and (x + y) % 3:
                # Grime from the tyres where they cross, just beside the paint.
                fl[x, y] = mix(g, '#8f8b81', .22)
            elif surface in ('concrete', 'tyre') and width + .5 <= d < width + 1 and rnd.random() < .6:
                # Where the paint soaked in, its oil wicked out into the concrete beside it and darkened it.
                fl[x, y] = mix(g, '#7d7869', .2)
    im = floor.copy(); px = im.load()
    if stage != GAP:
        # Older coats beside it in short runs on one side, chips in it showing them, and wet patches on the last night.
        runs = []
        for _ in range(0 if thin else {FIRST: 3, SECOND: 1}.get(stage, 0)):
            start = rnd.uniform(0, 13); runs.append((start, start + rnd.uniform(2, 4), rnd.choice((-1, 1)), rnd.choice(OLDER)))
        chips = set(rnd.sample(line, {FIRST: 6, SECOND: 1}.get(stage, 0)))
        wet = set()
        if stage in (LAST, FOOTPRINT):
            for cx, cy in rnd.sample(line, 3):
                for dy in range(-2, 3):
                    for dx in range(-1, 2):
                        if rnd.random() < .65:
                            wet.add((cx + dx, cy + dy))
        for y in range(16):
            for x in range(16):
                d, t, side = geo[x, y]
                g = under[x, y]
                c = None
                if d < width:
                    c = coat
                    if (x, y) in chips:
                        c = rnd.choice(OLDER) if rnd.random() < .7 else None
                    if thin and rnd.random() < .3:
                        c = None
                else:
                    for a, b, s, old in runs:
                        if a <= t <= b and s == side and d < width + 1:
                            c = old
                if c is None:
                    # Beside the line: paint caught on the blades, run into the joins, soaked into the edge, pooled.
                    if surface == 'lawn' and width <= d < width + 1 and rnd.random() < .22:
                        px[x, y] = mix(px[x, y], coat, .7)
                    elif surface == 'paver' and g == JOIN and d < 2:
                        px[x, y] = mix(g, coat, .5 if d < 1.25 else .25)
                    elif surface in ('concrete', 'tyre') and width <= d < width + .5 and rnd.random() < .5:
                        px[x, y] = mix(px[x, y], coat, .22)
                    elif surface == 'mulch' and width <= d < width + 1 and rnd.random() < .45:
                        px[x, y] = mix(coat, '#cfc8b6', .3)
                    continue
                # The paint, as this ground takes it.
                c = mix(c, '#000000', rnd.uniform(0, .025))
                if surface in ('concrete', 'tyre'):
                    c = mix(c, g, .1)
                    if surface == 'tyre' and TREAD[0] <= y <= TREAD[1]:
                        # Where the wheels cross, the tyre marks on the paint have been painted over: a newer patch, the tread
                        # still showing through it.
                        c = mix('#ece9e1' if stage == FIRST else c, g, .1)
                        if (x + y) % 3 == 0:
                            c = mix(c, '#7d796f', .2 if stage == FIRST else .1)
                elif surface == 'paver':
                    c = mix(c, '#cfc8b5', .55) if g == JOIN else mix(c, g, .06)
                elif surface == 'lawn':
                    if rnd.random() < .2:
                        c = mix(rnd.choice(DEAD), '#6b5c34', .3)
                elif surface == 'mulch':
                    if rnd.random() < .12:
                        c = g
                elif surface == 'deck':
                    if g == GAP_PX:
                        c = mix(g, coat, .12)
                    elif stage < LAST and rnd.random() < (.25 if stage == FIRST else .08):
                        c = g if rnd.random() < .5 else lighter(coat, 8)
                if (x, y) in wet:
                    c = lighter(c, 9) if rnd.random() < .7 else (255, 255, 255, 255)
                px[x, y] = c
        # The pavers' beads: paint that would not take, sitting up at the line's edge, a shadow under each.
        if surface == 'paver':
            for y in range(16):
                for x in range(16):
                    if width <= geo[x, y][0] < width + .5 and under[x, y] != JOIN and rnd.random() < .18:
                        px[x, y] = colour('#f6f4ee')
                        if y + 1 < 16 and geo[x, y + 1][0] >= width:
                            px[x, y + 1] = mix(px[x, y + 1], '#5a4a3a', .25)
        # The deck's flakes, lifted: a pale edge and its shadow.
        if surface == 'deck' and stage == FIRST:
            for x, y in rnd.sample(line, 3):
                if under[x, y] != GAP_PX:
                    px[x, y] = colour('#f8f6f0')
                    if x + 1 < 16:
                        px[x + 1, y] = mix(px[x + 1, y], '#2a2018', .3)
    if stage == FOOTPRINT:
        footprint(px, under, geo, width, coat)
    if stage in (HALF, GAP):
        scrubbed(px, fl, under, geo, width, coat, surface, rnd, stage == HALF)
    return im


def footprint(px, under, geo, width, coat):
    """A shoe through the wet paint: the line smudged where it came down, the paint dragged out the way it went, and its print."""
    for y in range(6, 10):
        for x in range(16):
            if geo[x, y][0] < width:
                px[x, y] = mix(px[x, y], under[x, y], .45)
        for i, x in enumerate(range(9, 13)):
            px[x, y] = mix(px[x, y], coat, (.55, .42, .3, .18)[i])
    for x, y in ((12, 4), (13, 4), (12, 5), (13, 5), (13, 6), (12, 7), (13, 7), (12, 10), (13, 10), (12, 11), (13, 11)):
        px[x, y] = mix(px[x, y], coat, .45)


def ghost(surface, x, y, d, fl, under, coat, rnd):
    """The ground where the line was scrubbed out: the lawn still dead, the pavers' joins and the deck's gaps holding paint, the bare
    deck and the concrete scrubbed paler than the rest, the mulch churned."""
    g = under[x, y]
    if surface == 'lawn':
        return fl[x, y]
    if surface == 'paver' and g == JOIN:
        return mix(g, coat, .35)
    if surface == 'deck' and g == GAP_PX:
        return mix(g, coat, .3)
    if surface == 'mulch':
        r = rnd.random()
        return mix(g, '#3f2b1d', .35) if r < .6 else mix(g, '#7a6c5c', .4) if r < .7 else g
    return lighter(g, 10 if surface == 'deck' else 6 if surface == 'paver' else 7) if d < 1.25 else fl[x, y]


def scrubbed(px, fl, under, geo, width, coat, surface, rnd, half):
    """Scrubbed half away (what is left faded, the rest smeared out by the brush), or out: the ground with the line's ghost."""
    for y in range(16):
        for x in range(16):
            d = geo[x, y][0]
            if d >= 1.75:
                continue
            if half:
                if d < width:
                    px[x, y] = mix(px[x, y], ghost(surface, x, y, d, fl, under, coat, rnd), .45) if rnd.random() < .5 else ghost(surface, x, y, d, fl, under, coat, rnd)
                elif rnd.random() < .25:
                    px[x, y] = mix(px[x, y], coat, .25)
            else:
                px[x, y] = ghost(surface, x, y, d, fl, under, coat, rnd)
                if surface == 'lawn' and d < width and rnd.random() < .05:
                    px[x, y] = mix(px[x, y], coat, .5)


def drawn():
    """Every cell of the line, by name: surface, shape, the way it lies, and its stage."""
    out = {}
    for si, surface in enumerate(SURFACES):
        for hi, shape in enumerate(SHAPES):
            if shape == 'thin' and surface != 'lawn':
                continue
            for oi, (orient, angle) in enumerate(ORIENTS[shape].items()):
                for stage in range(STAGES):
                    base = turn(ground(surface), (360 - angle) % 360)
                    im = paint(surface, shape, stage, base, 52400 + si * 1000 + hi * 100 + oi * 10 + stage)
                    out[f'{surface}_{shape}_{orient}_{stage}'] = turn(im, angle)
    return out


def drawn_shape(surface, shape):
    """The shape a state is drawn with: the thin line is only behind the shed, on the lawn; elsewhere its states show the line."""
    return 'straight' if shape == 'thin' and surface != 'lawn' else shape


def orient(shape, facing):
    if shape == 'corner':
        return {'north': 'ne', 'east': 'es', 'south': 'sw', 'west': 'wn'}[facing]
    return 'ns' if facing in ('north', 'south') else 'ew'


def files(images):
    out = {}
    for name in images:
        surface = name.split('_')[0]
        side = NS + 'block/pell_' + ('concrete' if surface == 'tyre' else surface)
        out[f'models/{FOLDER}/{name}.json'] = {'parent': 'minecraft:block/cube_bottom_top',
                                               'textures': {'top': NS + f'{FOLDER}/{name}', 'bottom': side, 'side': side, 'particle': side}}
    variants = {}
    for facing in FACINGS:
        for shape in SHAPES:
            for surface in SURFACES:
                for stage in range(STAGES):
                    drawn_as = drawn_shape(surface, shape)
                    variants[f'facing={facing},shape={shape},stage={stage},surface={surface}'] = {
                        'model': NS + f'{FOLDER}/{surface}_{drawn_as}_{orient(drawn_as, facing)}_{stage}'}
    out['blockstates/pell_line.json'] = {'variants': variants}
    return out


def java():
    """PellLine's shapes, surfaces and stage count, as the game has them."""
    src = (ROOT / 'src/main/java/io/github/knaitoe/theoldesthouse/labyrinth/PellLine.java').read_text()
    def enum(name):
        body = re.search(r'enum ' + name + r' implements StringRepresentable \{\s*([A-Z_,\s]+);', src).group(1)
        return [k.strip().lower() for k in body.split(',') if k.strip()]
    stages = int(re.search(r'STAGE=IntegerProperty\.create\("stage",0,(\d+)\)', src).group(1)) + 1
    return enum('Shape'), enum('Surface'), stages


def main():
    check = argparse.ArgumentParser()
    check.add_argument('--check', action='store_true')
    check = check.parse_args().check
    shapes, surfaces, stages = java()
    assert shapes == SHAPES, (shapes, SHAPES)
    assert surfaces == SURFACES, (surfaces, SURFACES)
    assert stages == STAGES, (stages, STAGES)
    images = drawn()
    data = files(images)
    for name, im in images.items():
        assert im.size == (16, 16), name
        path = A / 'textures' / FOLDER / f'{name}.png'
        if check:
            with Image.open(path) as actual:
                assert actual.convert('RGBA').tobytes() == im.tobytes(), name
        else:
            path.parent.mkdir(parents=True, exist_ok=True); im.save(path, optimize=True)
    for rel, obj in data.items():
        path = A / rel
        if check:
            assert json.loads(path.read_text()) == obj, rel
        else:
            path.parent.mkdir(parents=True, exist_ok=True); path.write_text(json.dumps(obj, indent=2) + '\n')
    # Every state has a model, and every model a texture.
    assert len(data['blockstates/pell_line.json']['variants']) == len(FACINGS) * len(SHAPES) * len(SURFACES) * STAGES
    for v in data['blockstates/pell_line.json']['variants'].values():
        assert v['model'].replace(NS, 'models/') + '.json' in data, v
    # The two folders are the tool's alone: nothing in them it does not draw.
    for folder, wanted in ((A / 'textures' / FOLDER, {f'{name}.png' for name in images}),
                           (A / 'models' / FOLDER, {f'{name}.json' for name in images})):
        stale = sorted(f.name for f in folder.iterdir() if f.name not in wanted)
        if check:
            assert not stale, (folder, stale)
        else:
            for name in stale:
                (folder / name).unlink()
    lang_path = A / 'lang/en_us.json'; lang = json.loads(lang_path.read_text())
    if check:
        assert lang.get('block.the_oldest_house.pell_line') == 'Painted Line'
    else:
        lang['block.the_oldest_house.pell_line'] = 'Painted Line'
        lang_path.write_text(json.dumps(lang, indent=2, ensure_ascii=False) + '\n')
    print(('Verified' if check else 'Drew') + f" the Pell house's line: {len(images)} textures and models, {len(data['blockstates/pell_line.json']['variants'])} states.")


if __name__ == '__main__':
    main()
