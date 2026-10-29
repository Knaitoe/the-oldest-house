"""0.4.164 (owner: "Give barn custom texture? Fix the mess it is right now." / "Improve clutter in barn_well shed."): the farm
around the barn well, drawn in code at Minecraft's own sixteen-pixel scale.

  farm_material  the barn's red board-and-batten, its slid-open doors (white X-braces), white corner boards and four-pane
                 windows, and the tool shed's grey weathered boards: plain cubes.
  farm_prop      a feed trough, milk cans, seed sacks, a workbench with its vise, a pegboard of garden tools, a potting bench
                 with seedlings, a wheelbarrow, a pitchfork leant on a wall and a harness on its peg. Each faces north.

Every prop texture is a whole sixteen-pixel material (grain, steel, burlap); each face reads the part of it Minecraft's own
automatic UV would, so every pixel is a native pixel.

Usage: python3 tools/draw_barn_well.py [--check]
"""
from pathlib import Path
import io
import json
import random
import sys

from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / 'src/main/resources/assets/the_oldest_house'
NS = 'the_oldest_house'
MATERIALS = ('siding', 'door', 'trim', 'window', 'shed_boards')
PROPS = ('trough', 'milk_cans', 'seed_sacks', 'workbench', 'tool_rack', 'potting_bench', 'wheelbarrow', 'pitchfork', 'harness')


def rng(name):
    return random.Random(1640 + sum(map(ord, name)) * 13)


def speckle(name, base, colours, count):
    im = Image.new('RGBA', (16, 16), base)
    d = ImageDraw.Draw(im)
    r = rng(name)
    for _ in range(count):
        d.point((r.randrange(16), r.randrange(16)), fill=r.choice(colours))
    return im


def boards(name, base, dark, light, seam, width=4, vertical=True, nails='#3b302a'):
    """Weathered boards: grain streaks along each board, a seam between boards, nail heads near the ends."""
    im = Image.new('RGBA', (16, 16), base)
    d = ImageDraw.Draw(im)
    r = rng(name)
    for line in range(16):
        for k in range(16):
            v = r.random()
            if v < .16:
                d.point((line, k) if vertical else (k, line), fill=dark)
            elif v > .9:
                d.point((line, k) if vertical else (k, line), fill=light)
    for edge in range(width - 1, 16, width):
        d.line((edge, 0, edge, 15) if vertical else (0, edge, 15, edge), fill=seam)
    for start in range(0, 16, width):
        for y in (1, 14):
            d.point((start + 1, y) if vertical else (y, start + 1), fill=nails)
    return im


def materials():
    t = {}
    # The barn: red board-and-batten, weathered to the grey wood here and there, battens every eight pixels.
    im = boards('siding', '#8d3226', '#74271d', '#a2453a', '#5e1f18', width=8)
    d = ImageDraw.Draw(im)
    r = rng('siding_wear')
    for _ in range(18):
        x, y = r.randrange(16), r.randrange(16)
        d.point((x, y), fill='#8a7b6c')
    for x in (0, 8):
        d.line((x, 0, x, 15), fill='#9c3d31')
        d.line((x + 1, 0, x + 1, 15), fill='#6a231b')
    t['siding'] = im
    im = t['siding'].copy()
    d = ImageDraw.Draw(im)
    d.rectangle((0, 0, 15, 15), outline='#d8d0be')
    for i in range(16):
        for w in (0, 1):
            if 0 <= i + w <= 15:
                d.point((i, min(15, i + w)), fill='#d8d0be')
                d.point((15 - i, min(15, i + w)), fill='#d8d0be')
    for x, y in ((0, 0), (15, 15), (15, 0), (0, 15), (7, 8), (8, 7)):
        d.point((x, y), fill='#bdb4a0')
    t['door'] = im
    im = boards('trim', '#d8d0be', '#c4bba7', '#e8e2d4', '#a89f8c', width=16)
    d = ImageDraw.Draw(im)
    d.line((0, 0, 0, 15), fill='#b7ae9b')
    d.line((15, 0, 15, 15), fill='#b7ae9b')
    t['trim'] = im
    im = t['siding'].copy()
    d = ImageDraw.Draw(im)
    d.rectangle((2, 2, 13, 13), fill='#d8d0be')
    d.rectangle((3, 3, 12, 12), fill='#2c3539')
    d.line((7, 3, 8, 3), fill='#d8d0be')
    d.rectangle((7, 3, 8, 12), fill='#d8d0be')
    d.rectangle((3, 7, 12, 8), fill='#d8d0be')
    for x, y in ((4, 4), (5, 4), (4, 5), (10, 4), (11, 4), (10, 9), (4, 10), (5, 11)):
        d.point((x, y), fill='#5d6e74')
    for x, y in ((11, 11), (6, 6), (12, 5)):
        d.point((x, y), fill='#43504f')
    t['window'] = im
    t['shed_boards'] = boards('shed_boards', '#8b877c', '#77736a', '#a19c90', '#55524b', width=4)
    return t


def prop_textures():
    t = {}
    t['wood'] = boards('wood', '#7b5b3b', '#6a4d31', '#8c6a47', '#553d27', width=16, vertical=False)
    t['wood_light'] = boards('wood_light', '#a07a4c', '#8c6a40', '#b48c5c', '#76583a', width=16, vertical=False)
    t['steel'] = speckle('steel', '#aeb4b8', ['#9aa1a6', '#c3c8cb', '#8d9398'], 70)
    im = t['steel'].copy()
    d = ImageDraw.Draw(im)
    d.rectangle((3, 3, 12, 12), outline='#7f868b')
    t['steel_top'] = im
    t['iron'] = speckle('iron', '#46494d', ['#3a3d41', '#55595e'], 60)
    im = Image.new('RGBA', (16, 16), '#b39a6a')
    d = ImageDraw.Draw(im)
    for y in range(16):
        for x in range(16):
            if (x + y) % 2 == 0:
                d.point((x, y), fill='#a58c5d')
            if (x * 3 + y) % 7 == 0:
                d.point((x, y), fill='#c4ab7a')
    d.line((0, 5, 15, 5), fill='#8a734c')
    for x in range(4, 12):
        d.point((x, 9), fill='#5b4a33')
    t['burlap'] = im
    t['rope'] = speckle('rope', '#b49a66', ['#9a8256', '#c9b07c', '#8a7349'], 90)
    t['leather'] = speckle('leather', '#5e3c25', ['#4f321f', '#6e4a30'], 60)
    t['hay'] = speckle('hay', '#c7a54a', ['#b8953e', '#d8b85c', '#a3843a'], 110)
    t['terracotta'] = speckle('terracotta', '#b5643c', ['#a35834', '#c47450'], 50)
    im = speckle('soil', '#3e2b1d', ['#33231a', '#4a3424'], 50)
    d = ImageDraw.Draw(im)
    d.point((7, 7), fill='#5a8a2e'); d.point((8, 8), fill='#6ea03a'); d.point((8, 7), fill='#5a8a2e')
    t['soil'] = im
    t['leaf'] = speckle('leaf', '#5a8a2e', ['#4a7524', '#6ea03a'], 60)
    t['rubber'] = speckle('rubber', '#2d2d2e', ['#252526', '#393a3b'], 40)
    t['barrow'] = speckle('barrow', '#4e6a3a', ['#425b31', '#5b7a45', '#6b5a48'], 60)
    t['glass'] = speckle('glass', '#c7d9d0', ['#b3c8be', '#dde9e3'], 30)
    im = boards('pegboard', '#b48c5d', '#a07a4e', '#c39b6b', '#8c6c45', width=16, vertical=False, nails='#b48c5d')
    d = ImageDraw.Draw(im)
    for y in range(2, 16, 3):
        for x in range(1, 16, 3):
            d.point((x, y), fill='#5b4630')
    # Painted outlines where each tool hangs, so an empty peg still says what is missing.
    d.line((3, 2, 3, 13), fill='#8a2f24'); d.line((11, 3, 11, 13), fill='#8a2f24'); d.line((7, 2, 7, 13), fill='#8a2f24')
    t['pegboard'] = im
    return t


def face(tex, frm, to, side):
    x0, y0, z0 = frm
    x1, y1, z1 = to
    if side == 'north':
        uv = [16 - x1, 16 - y1, 16 - x0, 16 - y0]
    elif side == 'south':
        uv = [x0, 16 - y1, x1, 16 - y0]
    elif side == 'east':
        uv = [16 - z1, 16 - y1, 16 - z0, 16 - y0]
    elif side == 'west':
        uv = [z0, 16 - y1, z1, 16 - y0]
    elif side == 'up':
        uv = [x0, z0, x1, z1]
    else:
        uv = [x0, 16 - z1, x1, 16 - z0]
    uv = [min(16, max(0, v)) for v in uv]
    return {'texture': '#' + tex, 'uv': [round(v, 3) for v in uv]}


def el(frm, to, faces, rotation=None):
    out = {}
    for side in ('north', 'south', 'east', 'west', 'up', 'down'):
        tex = faces.get(side, faces.get('all'))
        if tex:
            out[side] = face(tex, frm, to, side)
    e = {'from': list(frm), 'to': list(to), 'faces': out}
    if rotation:
        e['rotation'] = rotation
    return e


def tilt(angle, origin, axis='x'):
    return {'angle': angle, 'axis': axis, 'origin': list(origin)}


def prop_models():
    W, L, S, I = {'all': 'wood'}, {'all': 'wood_light'}, {'all': 'steel', 'up': 'steel_top'}, {'all': 'iron'}
    m = {}
    m['trough'] = [
        el((1, 0, 4), (15, 1, 12), W), el((1, 1, 4), (15, 6, 5), W), el((1, 1, 11), (15, 6, 12), W),
        el((1, 1, 5), (2, 6, 11), W), el((14, 1, 5), (15, 6, 11), W),
        el((2, 1, 5), (14, 4, 11), {'all': 'hay'}),
    ]
    m['milk_cans'] = []
    for x, z in ((3, 3), (9, 8)):
        m['milk_cans'] += [
            el((x, 0, z), (x + 5, 9, z + 5), S), el((x + .5, 9, z + .5), (x + 4.5, 10, z + 4.5), S),
            el((x + 1.5, 10, z + 1.5), (x + 3.5, 11.5, z + 3.5), S), el((x + 1, 11.5, z + 1), (x + 4, 12, z + 4), S),
        ]
    m['seed_sacks'] = [
        el((1, 0, 2), (8, 8, 9), {'all': 'burlap'}), el((2.5, 8, 3.5), (6.5, 9.5, 7.5), {'all': 'burlap'}),
        el((3.5, 9.5, 4.5), (5.5, 10.5, 6.5), {'all': 'rope'}),
        el((7, 0, 8), (15, 6, 15), {'all': 'burlap'}), el((8, 6, 9), (14, 7, 14), {'all': 'burlap'}),
        el((2, 0, 10), (7, 5, 14), {'all': 'burlap'}),
    ]
    m['workbench'] = [
        el((0, 13, 1), (16, 15, 16), L),
        el((1, 0, 2), (3, 13, 4), W), el((13, 0, 2), (15, 13, 4), W), el((1, 0, 13), (3, 13, 15), W), el((13, 0, 13), (15, 13, 15), W),
        el((1, 4, 2), (15, 5, 15), W),
        el((11, 15, 1), (15, 17, 4), I), el((12.5, 15.5, 0), (13.5, 16.5, 1), I), el((11.5, 16, -.5), (14.5, 16.5, 0), I),
        el((3, 15, 9), (6, 18, 12), {'all': 'glass'}), el((3.5, 18, 9.5), (5.5, 18.5, 11.5), {'all': 'iron'}),
        el((7, 15, 10), (11, 16.5, 12), L), el((8.5, 16.5, 10.5), (9.5, 17.5, 11.5), W),
        el((2, 5, 9), (6, 8, 14), {'all': 'burlap'}),
    ]
    m['tool_rack'] = [
        el((1, 1, 15), (15, 16, 16), {'all': 'wood', 'north': 'pegboard'}),
        # The hoe, the rake and the spade on their pegs.
        el((2.5, 3, 14.4), (3.5, 15, 15), W), el((1.5, 2, 14.2), (4.5, 3.5, 15), S),
        el((6.5, 3, 14.4), (7.5, 15, 15), W), el((5, 2, 14.2), (9, 3, 14.8), S),
        el((10.5, 6, 14.4), (11.5, 15, 15), W), el((9.5, 2, 14.2), (12.5, 6, 14.8), S),
        el((12, 13.5, 13.8), (15, 14.5, 15), {'all': 'rope'}),
    ]
    m['potting_bench'] = [
        el((0, 10, 3), (16, 11, 13), W), el((0, 11, 12), (16, 15, 13), W),
        el((1, 0, 3), (3, 10, 5), W), el((13, 0, 3), (15, 10, 5), W), el((1, 0, 11), (3, 15, 13), W), el((13, 0, 11), (15, 15, 13), W),
        el((1, 3, 4), (15, 4, 12), W),
        el((2, 11, 5), (5, 14, 8), {'all': 'terracotta', 'up': 'soil'}), el((3, 14, 6), (4, 16, 7), {'all': 'leaf'}),
        el((7, 11, 6), (10, 14, 9), {'all': 'terracotta', 'up': 'soil'}), el((8, 14, 7), (9, 17, 8), {'all': 'leaf'}), el((7.5, 15.5, 6.5), (9.5, 16.5, 8.5), {'all': 'leaf'}),
        el((12, 11, 5), (15, 13, 8), {'all': 'terracotta'}),
        el((3, 4, 6), (8, 7, 10), {'all': 'burlap'}), el((10, 4, 6), (13, 6, 9), {'all': 'terracotta', 'up': 'soil'}),
    ]
    m['wheelbarrow'] = [
        el((4, 4, 4), (12, 5, 11), {'all': 'barrow'}),
        el((4, 5, 3), (12, 8, 4), {'all': 'barrow'}), el((4, 5, 11), (12, 8, 12), {'all': 'barrow'}),
        el((3, 5, 4), (4, 8, 11), {'all': 'barrow'}), el((12, 5, 4), (13, 8, 11), {'all': 'barrow'}),
        el((7, 0, 1), (9, 5, 6), {'all': 'rubber'}), el((7.5, 2, 2.5), (8.5, 3, 4.5), I),
        el((4, 4, 11), (5, 5, 16), L), el((11, 4, 11), (12, 5, 16), L),
        el((4, 0, 10), (5, 4, 11), W), el((11, 0, 10), (12, 4, 11), W),
        el((5, 5, 5), (11, 6.5, 10), {'all': 'soil'}),
    ]
    lean = tilt(22.5, (8, 0, 9.5))
    m['pitchfork'] = [
        el((7.5, 0, 9), (8.5, 11, 10), L, lean),
        el((6, 11, 9), (10, 12, 10), S, lean),
        el((6, 12, 9.25), (6.75, 15, 9.75), S, lean), el((7.65, 12, 9.25), (8.35, 15, 9.75), S, lean), el((9.25, 12, 9.25), (10, 15, 9.75), S, lean),
    ]
    m['harness'] = [
        el((7.5, 11, 13), (8.5, 12, 16), W), el((7, 11.5, 15), (9, 13, 16), W),
        el((5, 4, 14), (11, 11, 15.5), {'all': 'rope'}), el((6, 5, 13.5), (10, 10, 14), {'all': 'rope'}),
        el((10, 2, 14.5), (11, 11, 15.2), {'all': 'leather'}), el((9, 1.5, 14.3), (12, 3, 15.4), I),
    ]
    return m


def png(im):
    buf = io.BytesIO()
    im.save(buf, 'PNG')
    return buf.getvalue()


def outputs():
    out = {}
    for name, im in materials().items():
        out['textures/block/farm/%s.png' % name] = png(im)
    for name, im in prop_textures().items():
        out['textures/block/farm/prop_%s.png' % name] = png(im)
    for kind in MATERIALS:
        out['models/block/farm_%s.json' % kind] = (json.dumps({'parent': 'minecraft:block/cube_all', 'textures': {'all': '%s:block/farm/%s' % (NS, kind)}}, indent=2) + '\n').encode()
    out['blockstates/farm_material.json'] = (json.dumps({'variants': {'kind=%s' % k: {'model': '%s:block/farm_%s' % (NS, k)} for k in MATERIALS}}, indent=2) + '\n').encode()
    variants = {}
    for kind, elements in prop_models().items():
        used = sorted({f['texture'][1:] for e in elements for f in e['faces'].values()})
        tex = {k: '%s:block/farm/prop_%s' % (NS, k) for k in used}
        tex['particle'] = tex[used[0]]
        out['models/block/farm_prop_%s.json' % kind] = (json.dumps({'render_type': 'minecraft:cutout', 'textures': tex, 'elements': elements}, indent=2) + '\n').encode()
        for facing, y in (('north', 0), ('east', 90), ('south', 180), ('west', 270)):
            v = {'model': '%s:block/farm_prop_%s' % (NS, kind)}
            if y:
                v['y'] = y
            variants['facing=%s,kind=%s' % (facing, kind)] = v
    out['blockstates/farm_prop.json'] = (json.dumps({'variants': variants}, indent=2) + '\n').encode()
    return out


def main():
    check = '--check' in sys.argv
    stale = []
    for rel, data in outputs().items():
        path = ASSETS / rel
        if check:
            if not path.is_file():
                stale.append(rel)
            elif rel.endswith('.png'):
                if Image.open(path).convert('RGBA').tobytes() != Image.open(io.BytesIO(data)).convert('RGBA').tobytes():
                    stale.append(rel)
            elif path.read_bytes() != data:
                stale.append(rel)
            continue
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_bytes(data)
    if stale:
        raise SystemExit('barn well assets differ from tools/draw_barn_well.py: ' + ', '.join(stale))


if __name__ == '__main__':
    main()
