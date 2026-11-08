"""The new ordinary halls (0.4.390, docs/LAUNDRY_CORRIDOR_0_4_390.md): materials, washing and fittings for the halls of
docs/HALLS_TRIALS_AND_TIME_DESIGN.md ("New hallway variations").

Draws, in native Minecraft pixels from fixed seeds:

- the laundry corridor (0.4.390): limewashed brick with the courses showing through, the same brick tarred at its foot, worn
  quarry tiles, a boarded ceiling; washing on the lines (a plain sheet, a striped ticking cover and a towel, the line and its
  pegs), a wicker basket of folded washing and a wooden stool.

The washing is modelled at every stage of its swing (HallSheetBlock): pushed, swung back and settling, the cloth turning about
the line so the two halves of a sheet stay one sheet.

Writes the models and block states, and the blocks' names. --check verifies everything written.
"""
from pathlib import Path
import argparse
import json
import random
from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
A = ROOT / 'src/main/resources/assets/the_oldest_house'
NS = 'the_oldest_house:'
P = NS + 'block/ordinary_hall/'

LIME = '#c9c3b2'
LIME_SHADOW = '#aea795'
BRICK = '#7b4a3a'
MORTAR = '#8e877a'
TAR = '#25211e'
QUARRY = '#7c4a38'
QUARRY_JOINT = '#4a3127'
BOARD = '#b8ad94'
BOARD_SEAM = '#7f765f'
LINEN = '#dcd6c6'
LINEN_FOLD = '#bdb6a3'
TICKING = '#d6d0bf'
STRIPE = '#4b5866'
TERRY = '#c7b497'
BAND = '#7a5a46'
ROPE = '#9d8a66'
ROPE_DARK = '#6f5f45'
PEG = '#a07e55'
WICKER = '#9a7a4c'
WICKER_DARK = '#6a5232'


def rgb(c):
    c = c.lstrip('#')
    return tuple(int(c[i:i + 2], 16) for i in (0, 2, 4))


def shade(c, k, a=255):
    r, g, b = rgb(c) if isinstance(c, str) else c[:3]
    return (max(0, min(255, r + k)), max(0, min(255, g + k)), max(0, min(255, b + k)), a)


def mix(c, d, t):
    a, b = rgb(c) if isinstance(c, str) else c[:3], rgb(d) if isinstance(d, str) else d[:3]
    return tuple(int(round(a[i] + (b[i] - a[i]) * t)) for i in range(3)) + (255,)


def brick_courses(x, y):
    """Whether a pixel is mortar in a running bond of bricks four pixels high and eight long."""
    course = y // 4
    return y % 4 == 3 or (x + (4 if course % 2 else 0)) % 8 == 7


def limewash(seed, tarred):
    """Brick under limewash: the courses show through as faint shadow, and here and there the wash has flaked to the brick.
    Tarred: the same wall at its foot, painted black against the wet, the tar's top edge ragged and the wash above it stained."""
    rnd = random.Random(seed)
    im = Image.new('RGBA', (16, 16))
    for y in range(16):
        for x in range(16):
            k = rnd.randint(-3, 3)
            c = shade(LIME_SHADOW, k) if brick_courses(x, y) else shade(LIME, k)
            im.putpixel((x, y), c)
    for _ in range(3 if not tarred else 1):
        x0, y0 = rnd.randrange(1, 13), rnd.randrange(1, 13)
        for dx in range(rnd.randint(2, 3)):
            for dy in range(rnd.randint(1, 2)):
                if not brick_courses(x0 + dx, y0 + dy):
                    im.putpixel((x0 + dx, y0 + dy), shade(BRICK, rnd.randint(-6, 6)))
    if tarred:
        edge = [rnd.choice((4, 5, 5, 6)) for _ in range(16)]
        for x in range(16):
            for y in range(edge[x], 16):
                k = rnd.randint(-3, 3) + (6 if y == edge[x] else 0)
                im.putpixel((x, y), shade(TAR, k))
            for y in range(max(0, edge[x] - 2), edge[x]):
                im.putpixel((x, y), mix(im.getpixel((x, y)), TAR, .25))
    return im


def quarry(seed):
    """Quarry tiles, four to a block, each fired its own tone, the joints dark and the middles rubbed smooth and paler."""
    rnd = random.Random(seed)
    im = Image.new('RGBA', (16, 16))
    tones = [rnd.randint(-9, 9) for _ in range(4)]
    for y in range(16):
        for x in range(16):
            tile = (y // 8) * 2 + x // 8
            centre = 3 if 2 <= x % 8 <= 5 and 2 <= y % 8 <= 5 else 0
            im.putpixel((x, y), shade(QUARRY, tones[tile] + rnd.randint(-3, 3) + centre))
    for i in range(16):
        for j in (0, 8):
            im.putpixel((i, j), shade(QUARRY_JOINT, rnd.randint(-2, 2)))
            im.putpixel((j, i), shade(QUARRY_JOINT, rnd.randint(-2, 2)))
    for _ in range(4):
        im.putpixel((rnd.randrange(16), rnd.randrange(16)), shade(QUARRY, 22))
    return im


def ceiling_boards(seed):
    """Tongue-and-groove boards painted pale and gone yellow, four to a block, the grooves dark."""
    rnd = random.Random(seed)
    im = Image.new('RGBA', (16, 16))
    tones = [rnd.randint(-4, 4) for _ in range(4)]
    for y in range(16):
        for x in range(16):
            im.putpixel((x, y), shade(BOARD, tones[y // 4] + rnd.randint(-2, 2)))
    for x in range(16):
        for y in (3, 7, 11, 15):
            im.putpixel((x, y), shade(BOARD_SEAM, rnd.randint(-2, 2)))
    return im


def weave(rnd, base, x, y):
    """A plain weave: alternate threads a touch lighter, so the cloth reads as cloth up close."""
    return shade(base, (2 if (x + y) % 2 else -1) + rnd.randint(-2, 2))


def linen(seed, upper):
    """A plain sheet. Upper: the top folded over the line, a crease below the fold and the shadows of the pegs. Lower: the rest
    of it, a soft crease down it and the hem stitched across."""
    rnd = random.Random(seed)
    im = Image.new('RGBA', (16, 16))
    for y in range(16):
        for x in range(16):
            im.putpixel((x, y), weave(rnd, LINEN, x, y))
    if upper:
        for x in range(16):
            im.putpixel((x, 2), shade(LINEN_FOLD, rnd.randint(-2, 2)))
            im.putpixel((x, 3), shade(LINEN, -6))
        for px in (3, 11):
            for y in range(1, 3):
                im.putpixel((px, y), shade(LINEN_FOLD, -8))
    for y in range(16):
        x = 9 + (1 if y % 7 > 3 else 0)
        im.putpixel((x, y), mix(im.getpixel((x, y)), LINEN_FOLD, .6))
    if not upper:
        for x in range(16):
            im.putpixel((x, 8), shade(LINEN_FOLD, rnd.randint(-2, 1)))
            im.putpixel((x, 9), shade(LINEN, -10))
    return im


def ticking(seed, upper):
    """A striped ticking cover: narrow dark stripes in pairs on a pale ground, the top over the line or the hem at the foot."""
    rnd = random.Random(seed)
    im = Image.new('RGBA', (16, 16))
    for y in range(16):
        for x in range(16):
            stripe = x % 8 in (1, 3)
            im.putpixel((x, y), shade(STRIPE, rnd.randint(-3, 3)) if stripe else weave(rnd, TICKING, x, y))
    if upper:
        for x in range(16):
            im.putpixel((x, 2), mix(im.getpixel((x, 2)), LINEN_FOLD, .7))
    else:
        for x in range(16):
            im.putpixel((x, 9), mix(im.getpixel((x, 9)), STRIPE, .5))
    return im


def towel(seed):
    """A towel: looped cotton, flecked, a woven band near its foot and the fringe below it."""
    rnd = random.Random(seed)
    im = Image.new('RGBA', (16, 16))
    for y in range(16):
        for x in range(16):
            im.putpixel((x, y), shade(TERRY, rnd.randint(-6, 4)))
    for x in range(16):
        im.putpixel((x, 8), shade(BAND, rnd.randint(-3, 3)))
        im.putpixel((x, 9), shade(BAND, rnd.randint(-3, 3) - 6))
        if x % 2 == 0:
            im.putpixel((x, 11), shade(TERRY, -14))
    for x in range(16):
        im.putpixel((x, 2), shade(TERRY, -10))
    return im


def line(seed):
    """The line, a twisted cord (top rows), and the wood of the pegs (below)."""
    rnd = random.Random(seed)
    im = Image.new('RGBA', (16, 16), shade(PEG, 0))
    for x in range(16):
        im.putpixel((x, 0), shade(ROPE, 4) if x % 3 else shade(ROPE_DARK, 0))
        im.putpixel((x, 1), shade(ROPE_DARK, 0) if x % 3 == 1 else shade(ROPE, -4))
    for y in range(2, 16):
        for x in range(16):
            im.putpixel((x, y), shade(PEG, rnd.randint(-4, 4) + (6 if x % 4 == 1 else 0)))
    for x in range(16):
        im.putpixel((x, 8), shade(PEG, -20))
    return im


def wicker(seed):
    """Wicker: stakes upright every four pixels, the weavers passed in and out of them, the rim bound at the top."""
    rnd = random.Random(seed)
    im = Image.new('RGBA', (16, 16))
    for y in range(16):
        for x in range(16):
            over = ((x // 2) + (y // 2)) % 2 == 0
            c = shade(WICKER, rnd.randint(-4, 4) + (8 if over else -4))
            if x % 4 == 0:
                c = shade(WICKER_DARK, rnd.randint(-3, 3))
            im.putpixel((x, y), c)
    for x in range(16):
        for y in (0, 1):
            im.putpixel((x, y), shade(WICKER, 10 - y * 8 + rnd.randint(-2, 2)))
    return im


def folded(seed):
    """Folded washing seen from above: sheets laid square, the folds' edges in shadow."""
    rnd = random.Random(seed)
    im = Image.new('RGBA', (16, 16))
    for y in range(16):
        for x in range(16):
            im.putpixel((x, y), weave(rnd, LINEN, x, y))
    for i in range(16):
        im.putpixel((i, 5), shade(LINEN_FOLD, -4))
        im.putpixel((i, 11), shade(LINEN_FOLD, -4))
        im.putpixel((7, i), shade(LINEN_FOLD, 0))
    for y in range(5, 11):
        for x in range(8, 16):
            if (x + y) % 4 == 0:
                im.putpixel((x, y), shade(STRIPE, 20))
    return im


MATERIALS = [
    ('laundry_wall', 'Limewashed Brick'),
    ('laundry_wall_low', 'Tarred Brick'),
    ('laundry_floor', 'Quarry Tiles'),
    ('laundry_ceiling', 'Boarded Ceiling'),
]


def textures():
    return {
        'laundry_wall': limewash(3900, False), 'laundry_wall_low': limewash(3900, True), 'laundry_floor': quarry(3901),
        'laundry_ceiling': ceiling_boards(3902),
        'linen_upper': linen(3903, True), 'linen_lower': linen(3904, False),
        'ticking_upper': ticking(3905, True), 'ticking_lower': ticking(3906, False),
        'towel': towel(3907), 'line': line(3908), 'basket': wicker(3909), 'basket_top': folded(3910),
    }


FACES = ('north', 'south', 'east', 'west', 'up', 'down')
# The swing (HallSheetBlock.SWAY): 1 to 3 begin toward +z, 4 to 6 toward -z. A turn about the line (x axis) by a negative angle
# carries the hanging cloth toward +z; the settling stages only shift it.
TURN = {1: -22.5, 4: 22.5}
SHIFT = {0: 0, 2: -1.5, 3: 0.75, 5: 1.5, 6: -0.75}


def cloth(tex, y0, y1, v0, sway, upper):
    """One block's piece of the cloth, half a pixel thick across the middle of the block, from y0 to y1, its texture cropped
    from row v0. The upper block turns it about the line at its top; the lower block about the same line, a block above."""
    shift = SHIFT.get(sway, 0)
    z0, z1 = 7.75 + shift, 8.25 + shift
    face = {'texture': tex, 'uv': [0, v0, 16, v0 + (y1 - y0)]}
    edge = {'texture': tex, 'uv': [0, v0, 0.5, v0 + (y1 - y0)]}
    flat = {'texture': tex, 'uv': [0, v0, 16, v0 + 0.5]}
    el = {'from': [0, y0, z0], 'to': [16, y1, z1], 'faces': {'north': face, 'south': face, 'east': edge, 'west': edge, 'up': flat, 'down': flat}}
    if sway in TURN:
        el['rotation'] = {'origin': [8, 15 if upper else 31, 8], 'axis': 'x', 'angle': TURN[sway]}
    return el


def rope_and_pegs(pegs):
    rope = {'texture': '#line', 'uv': [0, 0, 16, 1]}
    end = {'texture': '#line', 'uv': [0, 0, 1, 1]}
    els = [{'from': [0, 15, 7.5], 'to': [16, 16, 8.5], 'faces': {'north': rope, 'south': rope, 'up': rope, 'down': rope, 'east': end, 'west': end}}]
    if pegs:
        wood = {'texture': '#line', 'uv': [4, 3, 5, 7]}
        for x in (3, 11):
            els.append({'from': [x, 13, 7.25], 'to': [x + 1, 16.5, 8.75], 'faces': {f: wood for f in FACES}})
    return els


def sheet_model(kind, upper, sway):
    """The washing on one block of the line (HallSheetBlock), modelled with the line running east to west."""
    textures = {'line': P + 'line'}
    els = []
    if upper:
        els += rope_and_pegs(kind != 'bare')
    if kind in ('linen', 'ticking'):
        tex = P + f'{kind}_{"upper" if upper else "lower"}'
        textures['cloth'] = tex
        els.append(cloth('#cloth', 0, 15, 1, sway, True) if upper else cloth('#cloth', 6, 16, 0, sway, False))
    elif kind == 'towel' and upper:
        textures['cloth'] = P + 'towel'
        els.append(cloth('#cloth', 4, 15, 1, sway, True))
    textures['particle'] = textures.get('cloth', P + 'line')
    return {'ambientocclusion': False, 'textures': textures, 'elements': els}


def basket_model():
    """A wicker basket of folded washing on the floor, its rim a pixel proud."""
    wick = {f: {'texture': '#wicker'} for f in ('north', 'south', 'east', 'west', 'down')}
    return {'textures': {'wicker': P + 'basket', 'top': P + 'basket_top', 'particle': P + 'basket'}, 'elements': [
        {'from': [2, 0, 2], 'to': [14, 8, 14], 'faces': wick | {'up': {'texture': '#wicker', 'uv': [2, 2, 14, 14]}}},
        {'from': [3, 8, 3], 'to': [13, 9, 13], 'faces': {f: {'texture': '#top'} for f in FACES}},
        {'from': [1.5, 7, 1.5], 'to': [14.5, 8.5, 14.5], 'faces': {f: {'texture': '#wicker', 'uv': [0, 0, 16, 2]} for f in ('north', 'south', 'east', 'west')}},
    ]}


def stool_model():
    """A plain wooden stool, its seat a hand across and its four legs splayed a little, the wood of the pegs."""
    wood = {f: {'texture': '#wood', 'uv': [0, 4, 16, 16]} for f in FACES}
    leg = {f: {'texture': '#wood', 'uv': [4, 4, 6, 14]} for f in FACES}
    els = [{'from': [3, 9, 3], 'to': [13, 11, 13], 'faces': wood}]
    for x, z in ((3.5, 3.5), (10.5, 3.5), (3.5, 10.5), (10.5, 10.5)):
        els.append({'from': [x, 0, z], 'to': [x + 2, 9, z + 2], 'faces': leg})
    els.append({'from': [4.5, 4, 7.5], 'to': [11.5, 5, 8.5], 'faces': leg})
    return {'textures': {'wood': P + 'line', 'particle': P + 'line'}, 'elements': els}


KINDS = ('linen', 'ticking', 'towel', 'bare')


def files():
    out = {}
    for name, _ in MATERIALS:
        out[f'models/block/hall_{name}.json'] = {'parent': 'minecraft:block/cube_all', 'textures': {'all': P + name}}
        out[f'blockstates/hall_{name}.json'] = {'variants': {'': {'model': NS + f'block/hall_{name}'}}}
    variants = {}
    for kind in KINDS:
        for upper in (False, True):
            half = 'upper' if upper else 'lower'
            for sway in range(7):
                # The line and a bare stretch never swing; their models are the same at every stage.
                still = kind == 'bare' or (kind == 'towel' and not upper)
                name = f'hall_sheet_{kind}_{half}' + ('' if still else f'_{sway}')
                if not still or sway == 0:
                    out[f'models/block/{name}.json'] = sheet_model(kind, upper, 0 if still else sway)
                for axis, turn in (('x', 0), ('z', 90)):
                    variant = {'model': NS + 'block/' + name}
                    if turn:
                        variant['y'] = turn
                    variants[f'axis={axis},kind={kind},sway={sway},upper={str(upper).lower()}'] = variant
    out['blockstates/hall_sheet.json'] = {'variants': variants}
    out['models/block/hall_basket.json'] = basket_model()
    out['models/block/hall_stool.json'] = stool_model()
    turn = {'south': 0, 'west': 90, 'north': 180, 'east': 270}
    out['blockstates/hall_fitting.json'] = {'variants': {f'facing={f},kind={k}': ({'model': NS + f'block/hall_{k}'} | ({'y': turn[f]} if turn[f] else {})) for k in ('basket', 'stool') for f in turn}}
    return out


def names():
    out = {f'block.the_oldest_house.hall_{name}': title for name, title in MATERIALS}
    out['block.the_oldest_house.hall_sheet'] = 'Washing'
    out['block.the_oldest_house.hall_fitting'] = 'Hall Fitting'
    return out


def main():
    check = argparse.ArgumentParser()
    check.add_argument('--check', action='store_true')
    check = check.parse_args().check
    images, data = textures(), files()
    for key, im in images.items():
        path = A / 'textures/block/ordinary_hall' / f'{key}.png'
        if check:
            with Image.open(path) as actual:
                assert actual.convert('RGBA').tobytes() == im.tobytes(), key
        else:
            path.parent.mkdir(parents=True, exist_ok=True); im.save(path, optimize=True)
    for rel, obj in data.items():
        path = A / rel
        if check:
            assert json.loads(path.read_text()) == obj, rel
        else:
            path.parent.mkdir(parents=True, exist_ok=True); path.write_text(json.dumps(obj, indent=2) + '\n')
    lang_path = A / 'lang/en_us.json'; lang = json.loads(lang_path.read_text())
    for key, title in names().items():
        if check:
            assert lang.get(key) == title, key
        else:
            lang[key] = title
    if not check:
        lang_path.write_text(json.dumps(lang, indent=2, ensure_ascii=False) + '\n')
    print(('Verified' if check else 'Drew') + f' the ordinary halls: {len(images)} textures, {len(data)} models and states.')


if __name__ == '__main__':
    main()
