"""The great staircase's well and rails (0.4.376, docs/TEXTURE_PASS_STAIRCASE_0_4_376.md): the texture pass's third release.

Draws, in native Minecraft pixels from fixed seeds, the well's own coursing in three bands that darken with depth (upper, middle and
lower, each with a carved pier for the pilasters every twelve blocks), and a wrought-iron balustrade (handrail, bottom rail and
balusters, in a post and four sides) for the landings' rails. Writes the models and block states, and the blocks' names. --check
verifies everything written.

0.4.657 (docs/STAIRCASE_STONE_0_4_657.md; the owner: "Feel free to rework the texture of the staircase too"): the well's stone is
laid in courses a block high, each block of a course one of five stones whose upright joint falls in a different place or not at
all, so the stones run to different lengths and the wall does not repeat; and the treads' stone, which was a photograph-like texture
at eight times the native size, is drawn here at sixteen pixels as stone flags, one to a block, in six stones, which its stairs and
slabs pick among too.
"""
from pathlib import Path
import argparse
import json
import flicker
import random
from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
A = ROOT / 'src/main/resources/assets/the_oldest_house'
NS = 'the_oldest_house:'
P = NS + 'block/stair_well/'

# The well's stone in three bands, each darker and colder than the one above.
BANDS = {'upper': '#4c4d53', 'middle': '#3b3b41', 'lower': '#2a292e'}
JOINT = {'upper': '#2f3034', 'middle': '#242428', 'lower': '#18171b'}
IRON = '#2d2e33'


def rgb(c):
    c = c.lstrip('#')
    return tuple(int(c[i:i + 2], 16) for i in (0, 2, 4))


def shade(c, k, a=255):
    r, g, b = rgb(c) if isinstance(c, str) else c[:3]
    return (max(0, min(255, r + k)), max(0, min(255, g + k)), max(0, min(255, b + k)), a)


# 0.4.657: the well's stone, one course to a block. Each block of a course is one of these stones, chosen by the block state's
# weighted variants (one weight each): the upright joint so many pixels in from the block's west edge, or none, where the stone
# runs on into the next block. Two of five run on, so a stone is a block and two thirds long on the average, and never shorter than
# ten pixels. Every joint is two pixels wide and starts on an even pixel, so it lies whole in the smaller copies of the texture the
# game draws at a distance: a joint a pixel wide is averaged away into the stone beside it there, and the well read as boarding,
# its level joints kept and its upright ones lost.
JOINTS = (None, 6, None, 8, 10)


def course(band, seed, joint):
    """One course of the well's stone, a block high: the bed joint two pixels deep along its foot, the stone's lower edge shadowed
    above it and a lit arris along its top; the upright joint `joint` pixels in, two wide, shadowed on its west side and lit on its
    east, or none. The face is a few soft patches of tone and some short tooling strokes slanting across the stone, never level
    stripes; no stone is lighter or darker than the next, so a stone that runs on into the next block shows no seam there. The
    middle band is stained down from the joint above it; the lower band is wet, a sheen run down its face and a crust of salt
    along its bed."""
    rnd = random.Random(seed)
    base, dark = BANDS[band], JOINT[band]
    im = Image.new('RGBA', (16, 16))
    patches = [(rnd.uniform(0, 15), rnd.uniform(1, 12), rnd.uniform(2.5, 4.5), rnd.choice((-3, -2, 2, 3))) for _ in range(3)]
    for y in range(16):
        for x in range(16):
            k = sum(d for px, py, r, d in patches if (x - px) ** 2 + (y - py) ** 2 < r * r)
            if rnd.random() < .12:
                k += rnd.choice((-2, 2))
            im.putpixel((x, y), shade(base, max(-4, min(4, k))))
    for _ in range(rnd.randrange(2, 4)):
        x, y, step = rnd.randrange(0, 13), rnd.randrange(1, 10), rnd.choice((-1, 1))
        for i in range(rnd.randrange(2, 4)):
            at = (x + i, y + i if step > 0 else y + 2 - i)
            im.putpixel(at, shade(im.getpixel(at), -3))
    for x in range(16):
        im.putpixel((x, 0), shade(im.getpixel((x, 0)), 3))
        im.putpixel((x, 13), shade(im.getpixel((x, 13)), -3))
        im.putpixel((x, 14), shade(dark, 3 + rnd.randint(-1, 1)))
        im.putpixel((x, 15), shade(dark, rnd.randint(-1, 1)))
    if joint is not None:
        for y in range(14):
            im.putpixel((joint - 1, y), shade(im.getpixel((joint - 1, y)), -3))
            im.putpixel((joint, y), shade(dark, rnd.randint(-1, 1)))
            im.putpixel((joint + 1, y), shade(dark, 3 + rnd.randint(-1, 1)))
            im.putpixel((joint + 2, y), shade(im.getpixel((joint + 2, y)), 3))
    clear = [x for x in range(16) if joint is None or not joint - 1 <= x <= joint + 2]
    if band == 'middle':
        for x in rnd.sample(clear, rnd.randrange(2, 4)):
            length = rnd.randrange(5, 12)
            for y in range(length):
                im.putpixel((x, y), shade(im.getpixel((x, y)), -(4 if y < length // 3 else 3 if y < 2 * length // 3 else 2)))
    if band == 'lower':
        for x in rnd.sample(clear, 2):
            for y in range(rnd.randrange(0, 3), rnd.randrange(8, 13)):
                im.putpixel((x, y), shade(im.getpixel((x, y)), 3))
        for x in rnd.sample(clear, rnd.randrange(2, 4)):
            im.putpixel((x, 13), shade(base, 8))
            if rnd.random() < .5:
                im.putpixel((x, 12), shade(base, 5))
        for _ in range(rnd.randrange(2, 5)):
            x, y = rnd.choice(clear), rnd.randrange(2, 12)
            im.putpixel((x, y), shade(im.getpixel((x, y)), -4))
    return im


# 0.4.657: the treads' stone (the staircase stone: its treads, the first landings, the causeway, the cell and the way out below).
# Stone flags, one to a block, laid close: the joint along the back and the west side of each flag, so that laid together they meet
# in one joint a pixel wide, a little darker than the stone and no more, with no bevel, so that the floor reads as flags and not as
# tiles in a grid. Each flag is a shade of its own, as cut stone is.
TREAD = '#404044'
TREAD_JOINT = '#333337'
# Each flag: its name's ending, what is on it, its shade and how often the block picks it. The stairs and slabs pick among the
# plain ones.
TREADS = (('', 'plain', 0, 4), ('_deep', 'plain', -3, 3), ('_worn', 'plain', 3, 2),
          ('_cracked', 'cracked', -1, 1), ('_chipped', 'chipped', 1, 1), ('_flecked', 'flecked', 0, 2))


def tread(kind, seed, tone):
    """A stone flag of the treads, `tone` lighter or darker than the stone: two or three soft patches of tone, never pixel noise;
    a rare grain a shade off; a broken lighter patch where feet have smoothed it, a different shape on each. The cracked flag has a
    hairline from its back joint, the chipped one a corner knocked off its back edge, and the flecked one pale flecks in the
    stone."""
    rnd = random.Random(seed)
    base = shade(TREAD, tone)
    im = Image.new('RGBA', (16, 16))
    patches = [(rnd.uniform(2, 13), rnd.uniform(2, 13), rnd.uniform(3, 6), rnd.choice((-3, -2, 2, 3))) for _ in range(3)]
    for y in range(16):
        for x in range(16):
            k = sum(d for px, py, r, d in patches if (x - px) ** 2 + (y - py) ** 2 < r * r)
            if rnd.random() < .08:
                k += rnd.choice((-2, 2))
            im.putpixel((x, y), shade(base, max(-4, min(4, k))))
    cx, cy, rx, ry = rnd.uniform(6, 10), rnd.uniform(6, 10), rnd.uniform(3.5, 5.5), rnd.uniform(2.5, 4)
    for y in range(2, 15):
        for x in range(2, 15):
            d = ((x - cx) / rx) ** 2 + ((y - cy) / ry) ** 2
            if d < .7 or (d < 1.15 and rnd.random() < .5):
                im.putpixel((x, y), shade(im.getpixel((x, y)), 3))
    for i in range(16):
        im.putpixel((i, 0), shade(TREAD_JOINT, tone // 2 + rnd.randint(-1, 1)))
        im.putpixel((0, i), shade(TREAD_JOINT, tone // 2 + rnd.randint(-1, 1)))
    if kind == 'cracked':
        x, y = rnd.randrange(5, 12), 1
        for _ in range(rnd.randrange(5, 8)):
            im.putpixel((x, y), shade(im.getpixel((x, y)), -7))
            y += 1
            x = max(2, min(14, x + rnd.choice((-1, 0, 1))))
    if kind == 'chipped':
        x = rnd.randrange(4, 12)
        for dx, dy in ((0, 1), (1, 1), (2, 1), (0, 2), (1, 2)):
            im.putpixel((x + dx, dy), shade(base, -7))
        im.putpixel((x + 2, 2), shade(im.getpixel((x + 2, 2)), 4))
    if kind == 'flecked':
        for _ in range(5):
            x, y = rnd.randrange(2, 15), rnd.randrange(2, 15)
            im.putpixel((x, y), shade(base, 10))
    return im


def pier(band, seed):
    """A pilaster in the same stone: three flutes run up its face between plain margins, each shadowed down one side and lit down
    the other."""
    rnd = random.Random(seed)
    base, joint = BANDS[band], JOINT[band]
    im = Image.new('RGBA', (16, 16))
    for y in range(16):
        for x in range(16):
            k = rnd.randint(-2, 2)
            if x in (0, 15):
                k -= 6
            elif x in (1, 14):
                k += 5
            elif x in (4, 8, 12):
                k -= 12
            elif x in (5, 9, 13):
                k += 7
            im.putpixel((x, y), shade(base, k))
    for x in range(16):
        im.putpixel((x, 15), shade(joint, rnd.randint(-2, 2)))
    return im


def iron(seed):
    """Wrought iron, near black, with a dull light along its upper and outer faces and a little rust low down."""
    rnd = random.Random(seed)
    im = Image.new('RGBA', (16, 16))
    for y in range(16):
        for x in range(16):
            k = rnd.randint(-3, 3) + (10 if x % 4 == 0 else 0) + (6 if y < 3 else 0)
            c = shade(IRON, k)
            if y > 12 and rnd.random() < 0.18:
                c = (min(255, c[0] + 22), min(255, c[1] + 8), c[2], 255)
            im.putpixel((x, y), c)
    return im


BLOCKS = [
    ('upper', 'Upper Well Stone'),
    ('middle', 'Middle Well Stone'),
    ('lower', 'Lower Well Stone'),
    ('pier_upper', 'Upper Well Pier'),
    ('pier_middle', 'Middle Well Pier'),
    ('pier_lower', 'Lower Well Pier'),
    ('balustrade', 'Balustrade'),
]


def textures():
    """Every texture, by its path under textures/block."""
    out = {}
    for i, band in enumerate(BANDS):
        for v, joint in enumerate(JOINTS):
            out['stair_well/' + band + (f'_{v}' if v else '')] = course(band, 3790 + 10 * i + v, joint)
        out['stair_well/pier_' + band] = pier(band, 3770 + i)
    out['stair_well/balustrade'] = iron(3780)
    for v, (end, kind, tone, _) in enumerate(TREADS):
        out['staircase_stone' + end] = tread(kind, 6570 + v, tone)
    return out


def box(frm, to, tex='#iron'):
    faces = {}
    for face in ('north', 'south', 'east', 'west', 'up', 'down'):
        faces[face] = {'texture': tex}
    return {'from': frm, 'to': to, 'faces': faces}


def balustrade_models():
    """A post (one baluster with a cap) and a side running from the post to the north edge: handrail, bottom rail and two
    balusters. The block state turns the side to each connected face, as iron bars do."""
    t = {'iron': P + 'balustrade', 'particle': P + 'balustrade'}
    post = {'textures': t, 'elements': [box([7, 0, 7], [9, 16, 9]), box([6.5, 14, 6.5], [9.5, 16, 9.5])]}
    side = {'textures': t, 'elements': [
        box([7.25, 14, 0], [8.75, 15.5, 7]),       # the handrail
        box([7.5, 1, 0], [8.5, 2.5, 7]),           # the bottom rail
        box([7.5, 2.5, 1.5], [8.5, 14, 2.5]),      # a baluster near the edge
        box([7.5, 2.5, 4.5], [8.5, 14, 5.5]),      # and one between
    ]}
    return post, side


def stair_turns():
    """Minecraft's own turns for a stair's facing, half and shape, as its stone stairs have them, every one locked to the world's
    grain so the stone lies the same way on every flight."""
    out = {}
    turn = {'east': 0, 'south': 90, 'west': 180, 'north': 270}
    for facing in ('east', 'north', 'south', 'west'):
        for half in ('bottom', 'top'):
            for shape in ('inner_left', 'inner_right', 'outer_left', 'outer_right', 'straight'):
                y = turn[facing]
                if half == 'bottom' and shape in ('inner_left', 'outer_left'):
                    y -= 90
                if half == 'top' and shape in ('inner_right', 'outer_right'):
                    y += 90
                entry = {'y': y % 360, 'uvlock': True}
                if half == 'top':
                    entry = {'x': 180, **entry}
                out[f'facing={facing},half={half},shape={shape}'] = entry
    return out


def files():
    out = {}
    for name, _ in BLOCKS:
        if name == 'balustrade':
            continue
        out[f'models/block/stair_well_{name}.json'] = {'parent': 'minecraft:block/cube_all', 'textures': {'all': P + name}}
        out[f'blockstates/stair_well_{name}.json'] = {'variants': {'': {'model': NS + f'block/stair_well_{name}'}}}
        if name in BANDS:
            # 0.4.657: each block of the well's coursing one of its stones, by the block state's weighted variants.
            for v in range(1, len(JOINTS)):
                out[f'models/block/stair_well_{name}_{v}.json'] = {'parent': 'minecraft:block/cube_all', 'textures': {'all': P + f'{name}_{v}'}}
            out[f'blockstates/stair_well_{name}.json'] = {'variants': {'': [
                {'model': NS + f'block/stair_well_{name}' + (f'_{v}' if v else '')} for v in range(len(JOINTS))]}}
    # 0.4.657: the treads' stone, a flag to a block, picked by the block state; its stairs and slabs pick among the plain flags
    # the same way, each of their models drawn from one of them.
    stone = []
    for end, kind, tone, weight in TREADS:
        out[f'models/block/staircase_stone{end}.json'] = {'parent': 'minecraft:block/cube_all',
                                                          'textures': {'all': NS + 'block/staircase_stone' + end}}
        stone.append({'model': NS + 'block/staircase_stone' + end, 'weight': weight})
    out['blockstates/staircase_stone.json'] = {'variants': {'': stone}}
    plain = [(end, weight) for end, kind, tone, weight in TREADS if kind == 'plain']
    for name in ('stairs', 'stairs_inner', 'stairs_outer', 'slab', 'slab_top'):
        parent = {'stairs': 'stairs', 'stairs_inner': 'inner_stairs', 'stairs_outer': 'outer_stairs'}.get(name, name)
        for end, _ in plain:
            texture = NS + 'block/staircase_stone' + end
            out[f'models/block/staircase_{name}{end}.json'] = {'parent': 'minecraft:block/' + parent,
                                                              'textures': {'bottom': texture, 'top': texture, 'side': texture}}
    stairs = {}
    for key, turn in stair_turns().items():
        model = 'stairs' + ('_inner' if 'inner' in key else '_outer' if 'outer' in key else '')
        stairs[key] = [{'model': NS + f'block/staircase_{model}{end}', **turn, 'weight': weight} for end, weight in plain]
    out['blockstates/staircase_stairs.json'] = {'variants': stairs}
    out['blockstates/staircase_slab.json'] = {'variants': {
        'type=bottom': [{'model': NS + 'block/staircase_slab' + end, 'weight': weight} for end, weight in plain],
        'type=top': [{'model': NS + 'block/staircase_slab_top' + end, 'weight': weight} for end, weight in plain],
        'type=double': stone}}
    post, side = balustrade_models()
    out['models/block/stair_well_balustrade_post.json'] = post
    out['models/block/stair_well_balustrade_side.json'] = side
    model = NS + 'block/stair_well_balustrade_'
    parts = [{'apply': {'model': model + 'post'}}]
    for face, y in (('north', 0), ('east', 90), ('south', 180), ('west', 270)):
        apply = {'model': model + 'side'}
        if y:
            apply['y'] = y
        parts.append({'when': {face: 'true'}, 'apply': apply})
    out['blockstates/stair_well_balustrade.json'] = {'multipart': parts}
    return out


def main():
    check = argparse.ArgumentParser()
    check.add_argument('--check', action='store_true')
    check = check.parse_args().check
    images, data = textures(), files()
    for key, im in images.items():
        path = A / 'textures/block' / f'{key}.png'
        if check:
            with Image.open(path) as actual:
                assert actual.convert('RGBA').tobytes() == im.tobytes(), key
        else:
            path.parent.mkdir(parents=True, exist_ok=True); im.save(path, optimize=True)
    for rel, obj in data.items():
        path = A / rel
        if check:
            assert json.loads(path.read_text()) == flicker.settled(obj), rel
        else:
            path.parent.mkdir(parents=True, exist_ok=True); path.write_text(flicker.dumps(obj, indent=2) + '\n')
    lang_path = A / 'lang/en_us.json'; lang = json.loads(lang_path.read_text())
    for name, title in BLOCKS:
        key = f'block.the_oldest_house.stair_well_{name}'
        if check:
            assert lang.get(key) == title, key
        else:
            lang[key] = title
    if not check:
        lang_path.write_text(flicker.dumps(lang, indent=2, ensure_ascii=False) + '\n')
    print(('Verified' if check else 'Drew') + f' the staircase well: {len(images)} textures, {len(data)} models and states.')


if __name__ == '__main__':
    main()
