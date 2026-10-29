"""0.4.164 (owner: "podiums like this need new textures suited to their vignettes"): the reading stands, drawn in code.

Six scenes kept their paper on a vanilla lectern standing on a table. Each now has a stand of its own (ReadingStandBlock,
a native lectern underneath), drawn here at Minecraft's own sixteen-pixel scale:

  program       Movie Night    Ray's printed program for *Teeth of Summer* on a little pine easel, a stack in front.
  scorecard     Costume Night  Hank's judge's card clipped to a board on a stand, the judge's pencil beside it.
  folded_note   Winter Lake    Bud's note, folded small and opened out on the stump under a stone, snow in its creases.
  ledger_rest   Winchester     the construction ledger open on a mahogany book rest with brass corners and a ribbon.
  script_stand  Ghosts         a black script stand whose desk lies where a lectern's top does, so the call sheet the
                               client draws (GhostsCallSheetRenderer) lies on it.
  trail_rack    Borrasca       the county's green leaflet box at the trailhead, a trail leaflet standing in its pocket.

Each face's texture is painted only where Minecraft's own automatic UV reads it, so every pixel is a native pixel.
The models face north (the reader stands to the north), as a lectern's do; the blockstate turns them.

Usage: python3 tools/draw_reading_stands.py [--check]
"""
from pathlib import Path
import json
import random
import sys

from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / 'src/main/resources/assets/the_oldest_house'
NS = 'the_oldest_house'
KINDS = ('program', 'scorecard', 'folded_note', 'ledger_rest', 'script_stand', 'trail_rack')


def rng(name):
    return random.Random(164 + sum(map(ord, name)) * 11)


def grain(name, base, dark, light, along_y=False, density=.2):
    """Plain wood: a base colour with streaks of grain running along one axis."""
    im = Image.new('RGBA', (16, 16), base)
    d = ImageDraw.Draw(im)
    r = rng(name)
    for line in range(16):
        tone = dark if r.random() < .35 else (light if r.random() < .25 else None)
        if tone:
            for k in range(16):
                if r.random() < .7:
                    d.point((line, k) if along_y else (k, line), fill=tone)
    for _ in range(int(256 * density)):
        d.point((r.randrange(16), r.randrange(16)), fill=dark if r.random() < .5 else light)
    return im


def speckle(name, base, colours, count):
    im = Image.new('RGBA', (16, 16), base)
    d = ImageDraw.Draw(im)
    r = rng(name)
    for _ in range(count):
        d.point((r.randrange(16), r.randrange(16)), fill=r.choice(colours))
    return im


def text_lines(d, x0, x1, ys, colour, name, gap=.18):
    """Rows of printed or written words: short runs of pixels with gaps between them."""
    r = rng(name)
    for y in ys:
        x = x0
        while x <= x1:
            run = r.randint(1, 3)
            for k in range(run):
                if x + k <= x1:
                    d.point((x + k, y), fill=colour)
            x += run + 1 + (1 if r.random() < gap else 0)


def textures():
    t = {}
    # ---------------------------------------------------------------- Movie Night: the program on its easel
    t['pine'] = grain('pine', '#c9a46c', '#b28b55', '#d8b884', along_y=True)
    im = Image.new('RGBA', (16, 16), '#efe8d6')
    d = ImageDraw.Draw(im)
    # The card's face is read at x 3..12, y 1..13: a sea-blue band with a fin, the red title, the night's listing.
    d.rectangle((3, 1, 12, 4), fill='#2f6f9a')
    d.line((3, 4, 12, 4), fill='#4b8bb4')
    d.point((3, 2), fill='#4b8bb4'); d.point((11, 3), fill='#4b8bb4')
    for x, y in ((8, 2), (8, 3), (9, 3), (7, 3)):
        d.point((x, y), fill='#16263a')
    d.rectangle((3, 6, 12, 7), fill='#b8322a')
    for x in (4, 6, 7, 9, 11):
        d.point((x, 6), fill='#f3d9c6')
    text_lines(d, 4, 11, (9, 11), '#3a3530', 'program')
    d.point((4, 13), fill='#3a3530'); d.point((5, 13), fill='#3a3530'); d.point((10, 13), fill='#b8322a'); d.point((11, 13), fill='#b8322a')
    t['program_front'] = im
    im = Image.new('RGBA', (16, 16), '#e4dcc6')
    d = ImageDraw.Draw(im)
    text_lines(d, 4, 11, (4, 6, 8), '#b5ab93', 'program_back', gap=.4)
    t['program_back'] = im
    t['paper_edge'] = speckle('paper_edge', '#e9e2cf', ['#d9d0b8', '#f4eedf'], 30)
    im = Image.new('RGBA', (16, 16), '#e9e2cf')
    d = ImageDraw.Draw(im)
    # The stack's top is read at x 4..11, z 1..4 (rows 1..4): the top program's blue band and red title, as on the card.
    d.rectangle((4, 1, 11, 1), fill='#2f6f9a')
    d.point((7, 1), fill='#16263a')
    d.rectangle((4, 2, 11, 2), fill='#b8322a')
    text_lines(d, 5, 10, (3, 4), '#5a544c', 'program_top', gap=.4)
    t['program_top'] = im
    # ---------------------------------------------------------------- Costume Night: the judge's card
    t['masonite'] = speckle('masonite', '#7a5a3b', ['#6c4f33', '#87674a', '#5f452c'], 70)
    im = t['masonite'].copy()
    d = ImageDraw.Draw(im)
    # The sheet is read at x 3..12, y 1..13: pumpkin header, twelve entrants in two columns, the thirteenth circled.
    d.rectangle((4, 2, 11, 13), fill='#f1ecdf')
    d.rectangle((4, 2, 11, 3), fill='#d9792b')
    d.point((5, 2), fill='#f1ecdf'); d.point((7, 2), fill='#f1ecdf'); d.point((9, 2), fill='#f1ecdf')
    for row, y in enumerate((5, 7, 9, 11)):
        for x0 in (5, 9):
            d.point((x0, y), fill='#3b352f')
            d.line((x0 + 1, y, x0 + 2, y), fill='#8f877b')
            if (row + x0) % 3 == 0:
                d.point((x0 + 2, y - 1), fill='#2e4f9a')
    d.point((7, 13), fill='#a72a24'); d.point((8, 12), fill='#a72a24'); d.point((9, 13), fill='#a72a24'); d.point((8, 13), fill='#3b352f')
    t['clipboard_front'] = im
    t['clip'] = speckle('clip', '#b9bcbf', ['#9da1a5', '#d6d8da', '#8a8e92'], 60)
    im = Image.new('RGBA', (16, 16), '#e2b33a')
    d = ImageDraw.Draw(im)
    d.line((0, 0, 15, 0), fill='#c99a26')
    for x in range(3, 5):
        for y in range(16):
            d.point((x, y), fill='#e7a7a1')
    d.rectangle((9, 0, 10, 15), fill='#e9d3a8')
    d.point((10, 0), fill='#3a3330')
    t['pencil'] = im
    t['stand_dark'] = speckle('stand_dark', '#3b3430', ['#332c28', '#463e39'], 60)
    # ---------------------------------------------------------------- Winter Lake: the note under a stone
    im = Image.new('RGBA', (16, 16), '#ebe6da')
    d = ImageDraw.Draw(im)
    # Read at x 3..12, z 3..12 from above: blue rules and a red margin, the two fold creases, pencil, snow in the creases.
    for y in range(4, 13, 2):
        d.line((3, y, 12, y), fill='#b9c9dd')
    d.line((5, 3, 5, 12), fill='#d6a0a0')
    d.line((8, 3, 8, 12), fill='#cfc8b8')
    d.line((3, 8, 12, 8), fill='#cfc8b8')
    text_lines(d, 6, 11, (5, 7, 9, 11), '#5b5a63', 'note', gap=.35)
    for x, y in ((8, 4), (8, 10), (4, 8), (11, 8), (12, 12), (3, 3)):
        d.point((x, y), fill='#ffffff')
    t['note_top'] = im
    t['stone'] = speckle('stone', '#7d7d7a', ['#6b6b68', '#8f8f8b', '#5f605d', '#a0a09b'], 90)
    im = t['stone'].copy()
    d = ImageDraw.Draw(im)
    for x, y in ((10, 5), (11, 5), (11, 6), (10, 4)):
        d.point((x, y), fill='#f4f6f8')
    t['stone_top'] = im
    # ---------------------------------------------------------------- Winchester: the ledger on its book rest
    t['mahogany'] = grain('mahogany', '#6b2f22', '#57241a', '#7d3b2c')
    im = t['mahogany'].copy()
    d = ImageDraw.Draw(im)
    for x, y in ((0, 0), (15, 0), (0, 15), (15, 15), (1, 0), (0, 1), (14, 0), (15, 1), (0, 14), (1, 15), (15, 14), (14, 15)):
        d.point((x, y), fill='#c9a24b')
    d.line((0, 2, 15, 2), fill='#a8843a')
    t['mahogany_brass'] = im
    t['leather'] = speckle('leather', '#5e2420', ['#4f1d1a', '#6e2d27'], 70)
    im = Image.new('RGBA', (16, 16), '#e6dcc0')
    d = ImageDraw.Draw(im)
    # Read at x 2..13, z 3..13 from above: two pages either side of the spine, ruled columns of rooms and names.
    d.line((8, 3, 8, 13), fill='#a99b7a')
    d.line((7, 3, 7, 13), fill='#d3c8aa')
    for page in ((2, 6), (9, 13)):
        d.line((page[0] + 1, 3, page[0] + 1, 13), fill='#c49a8f')
        for y in range(4, 14, 2):
            d.line((page[0], y, page[1], y), fill='#cbbf9f')
        text_lines(d, page[0] + 2, page[1], range(4, 13, 2), '#4a3d33', 'ledger%d' % page[0], gap=.3)
    t['ledger_pages'] = im
    t['page_edge'] = speckle('page_edge', '#e2d6b6', ['#d2c5a2', '#ece3c8'], 40)
    t['ribbon'] = speckle('ribbon', '#9e1f22', ['#861a1c', '#b5292b'], 30)
    # ---------------------------------------------------------------- Ghosts: the script stand
    t['metal_black'] = speckle('metal_black', '#26272a', ['#1d1e20', '#323438', '#3c3e42'], 70)
    im = t['metal_black'].copy()
    d = ImageDraw.Draw(im)
    for x in range(2, 14, 3):
        d.line((x, 4, x, 14), fill='#1b1c1e')
    t['metal_desk'] = im
    # ---------------------------------------------------------------- Borrasca: the county's leaflet box
    t['county_green'] = grain('county_green', '#3f5a3a', '#344c30', '#4b6a45', along_y=True, density=.12)
    im = t['county_green'].copy()
    d = ImageDraw.Draw(im)
    # The pocket's front is read at x 3..12, y 11..15: a routed, cream-filled TRAILS across it.
    d.rectangle((3, 11, 12, 15), fill='#3f5a3a')
    for x in (4, 6, 8, 10, 11):
        d.point((x, 13), fill='#e1d7b5')
    d.point((4, 12), fill='#e1d7b5'); d.point((5, 12), fill='#e1d7b5'); d.point((8, 12), fill='#e1d7b5'); d.point((10, 14), fill='#e1d7b5')
    t['county_front'] = im
    t['county_roof'] = grain('county_roof', '#2f4529', '#26391f', '#3a5433', density=.1)
    im = Image.new('RGBA', (16, 16), '#ede4c8')
    d = ImageDraw.Draw(im)
    # Read at x 4..11, y 6..12: TRIPLE TREE over three pines growing out of one root, the dotted walk to Marker Two.
    d.rectangle((4, 6, 11, 7), fill='#3f5a3a')
    d.point((5, 6), fill='#ede4c8'); d.point((7, 6), fill='#ede4c8'); d.point((9, 6), fill='#ede4c8')
    for x in (5, 7, 9):
        d.point((x, 8), fill='#2f5a2a')
    d.line((4, 9, 10, 9), fill='#2f5a2a')
    d.point((6, 10), fill='#5b3a22'); d.point((7, 10), fill='#5b3a22'); d.point((8, 10), fill='#5b3a22')
    for x in (4, 6, 8):
        d.point((x, 12), fill='#8a6a3c')
    d.point((10, 12), fill='#a72a24'); d.point((11, 11), fill='#a72a24')
    t['leaflet_front'] = im
    t['leaflet_back'] = speckle('leaflet_back', '#e4dbbe', ['#d4c9a8'], 25)
    return t


def face(tex, frm, to, side):
    """A face whose UV is the one Minecraft would choose for this element, written out so nothing is left to chance."""
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
    return {'texture': '#' + tex, 'uv': [round(v, 3) for v in uv]}


def el(frm, to, faces, rotation=None):
    """faces: {'all': tex} with per-side overrides."""
    out = {}
    for side in ('north', 'south', 'east', 'west', 'up', 'down'):
        tex = faces.get(side, faces.get('all'))
        if tex:
            out[side] = face(tex, frm, to, side)
    e = {'from': list(frm), 'to': list(to), 'faces': out}
    if rotation:
        e['rotation'] = rotation
    return e


def tilt(angle, origin):
    return {'angle': angle, 'axis': 'x', 'origin': list(origin)}


def models():
    m = {}
    back = tilt(22.5, (8, 1.5, 6.3))
    strut = tilt(-22.5, (8, 0, 10.5))
    m['program'] = ({'pine': 'pine', 'front': 'program_front', 'back': 'program_back', 'edge': 'paper_edge', 'top': 'program_top'}, [
        el((2, 0, 5), (14, 1.5, 7), {'all': 'pine'}),
        el((3, 1.5, 6), (13, 15, 6.6), {'all': 'edge', 'north': 'front', 'south': 'back'}, back),
        el((7.5, 0, 10), (8.5, 6, 11), {'all': 'pine'}, strut),
        el((4, 0, 1), (12, 1, 4.5), {'all': 'edge', 'up': 'top'}),
    ])
    m['scorecard'] = ({'stand': 'stand_dark', 'board': 'masonite', 'front': 'clipboard_front', 'clip': 'clip', 'pencil': 'pencil'}, [
        el((3, 0, 5), (13, 1.5, 8), {'all': 'stand'}),
        el((3, 1.5, 6), (13, 15, 6.8), {'all': 'board', 'north': 'front'}, back),
        el((6, 13, 5.5), (10, 14.5, 6.1), {'all': 'clip'}, back),
        el((7.5, 0, 10), (8.5, 6, 11), {'all': 'stand'}, strut),
        el((3, 0, 2), (11, 1, 3), {'all': 'pencil'}),
    ])
    m['folded_note'] = ({'note': 'note_top', 'edge': 'paper_edge', 'stone': 'stone', 'snow': 'stone_top'}, [
        el((3, 0, 3), (13, 0.25, 13), {'all': 'edge', 'up': 'note'}),
        el((9, 0.25, 4), (13, 2, 7), {'all': 'stone'}),
        el((9.5, 2, 4.5), (12.5, 2.75, 6.5), {'all': 'stone', 'up': 'snow'}),
    ])
    rest = tilt(-22.5, (8, 4, 2))
    m['ledger_rest'] = ({'wood': 'mahogany', 'brass': 'mahogany_brass', 'leather': 'leather', 'pages': 'ledger_pages', 'edge': 'page_edge', 'ribbon': 'ribbon'}, [
        el((1, 0, 3), (15, 2, 14), {'all': 'brass', 'up': 'wood', 'down': 'wood'}),
        el((2, 2, 10), (14, 6.5, 13.5), {'all': 'wood'}),
        el((1, 2, 2), (15, 4, 15), {'all': 'brass', 'up': 'wood'}, rest),
        el((1.5, 4, 2.5), (14.5, 4.5, 14.5), {'all': 'leather'}, rest),
        el((2, 4.5, 3), (14, 5.5, 14), {'all': 'edge', 'up': 'pages'}, rest),
        el((1, 4, 2), (15, 5.25, 2.75), {'all': 'brass'}, rest),
        el((7.5, 1.5, 1.6), (8.5, 4, 1.7), {'all': 'ribbon'}),
    ])
    desk = tilt(-22.5, (8, 8, 8))
    m['script_stand'] = ({'metal': 'metal_black', 'desk': 'metal_desk'}, [
        el((4, 0, 4), (12, 1, 12), {'all': 'metal'}),
        el((7.5, 1, 7.5), (8.5, 12, 8.5), {'all': 'metal'}),
        el((7, 12, 7), (9, 14.5, 9), {'all': 'metal'}),
        el((1.5, 15, 3.5), (14.5, 16, 15.5), {'all': 'metal', 'up': 'desk'}, desk),
        el((1.5, 16, 3.5), (14.5, 17, 4.25), {'all': 'metal'}, desk),
    ])
    m['trail_rack'] = ({'green': 'county_green', 'front': 'county_front', 'roof': 'county_roof', 'leaflet': 'leaflet_front', 'back': 'leaflet_back'}, [
        el((3, 0, 6), (13, 9, 11), {'all': 'green'}),
        el((3, 0, 4.5), (13, 5, 6), {'all': 'green', 'north': 'front'}),
        el((4, 3, 5), (12, 10, 5.4), {'all': 'back', 'north': 'leaflet'}),
        el((4.5, 4, 5.5), (11.5, 11, 5.9), {'all': 'back', 'north': 'leaflet'}),
        el((2.5, 9, 5.5), (13.5, 10, 11.5), {'all': 'roof'}),
    ])
    return m


def outputs():
    """Every file this tool writes, as bytes, keyed by its path under the assets folder."""
    import io
    out = {}
    for name, im in textures().items():
        buf = io.BytesIO()
        im.save(buf, 'PNG')
        out['textures/block/reading_stand/%s.png' % name] = buf.getvalue()
    variants = {}
    for kind, (keys, elements) in models().items():
        tex = {k: '%s:block/reading_stand/%s' % (NS, v) for k, v in keys.items()}
        tex['particle'] = tex[next(iter(keys))]
        model = {'render_type': 'minecraft:cutout', 'ambientocclusion': False, 'textures': tex, 'elements': elements}
        out['models/block/reading_stand_%s.json' % kind] = (json.dumps(model, indent=2) + '\n').encode()
        for facing, y in (('north', 0), ('east', 90), ('south', 180), ('west', 270)):
            v = {'model': '%s:block/reading_stand_%s' % (NS, kind)}
            if y:
                v['y'] = y
            variants['facing=%s,kind=%s' % (facing, kind)] = v
    out['blockstates/reading_stand.json'] = (json.dumps({'variants': variants}, indent=2) + '\n').encode()
    return out


def main():
    check = '--check' in sys.argv
    stale = []
    for rel, data in outputs().items():
        path = ASSETS / rel
        if check:
            if not path.is_file() or (path.read_bytes() != data if not rel.endswith('.png') else Image.open(path).convert('RGBA').tobytes() != Image.open(__import__('io').BytesIO(data)).convert('RGBA').tobytes()):
                stale.append(rel)
            continue
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_bytes(data)
    if stale:
        raise SystemExit('reading stand assets differ from tools/draw_reading_stands.py: ' + ', '.join(stale))


if __name__ == '__main__':
    main()
