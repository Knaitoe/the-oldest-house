"""The great staircase's well and rails (0.4.376, docs/TEXTURE_PASS_STAIRCASE_0_4_376.md): the texture pass's third release.

Draws, in native Minecraft pixels from fixed seeds, the well's own coursing in three bands that darken with depth (upper, middle and
lower, each with a carved pier for the pilasters every twelve blocks), and a wrought-iron balustrade (handrail, bottom rail and
balusters, in a post and four sides) for the landings' rails. Writes the models and block states, and the blocks' names. --check
verifies everything written.
"""
from pathlib import Path
import argparse
import json
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


def well(band, seed):
    """Long blocks of dark stone, two courses a block, the joints staggered and sunk; fine level tooling across each face, a lit
    upper edge. The lower band is worn: its edges rounded off and a wet sheen run down from the joints."""
    rnd = random.Random(seed)
    base, joint = BANDS[band], JOINT[band]
    im = Image.new('RGBA', (16, 16))
    tones = [rnd.randint(-3, 3) for _ in range(4)]
    for y in range(16):
        course = y // 8
        for x in range(16):
            stone = course * 2 + (0 if (x + course * 6) % 16 < 10 else 1)
            k = tones[stone] + rnd.randint(-2, 2) + (2 if y % 2 == 0 else 0)
            row = y % 8
            if row == 0:
                k += 6 if band != 'lower' else 3
            elif row == 6:
                k -= 4
            im.putpixel((x, y), shade(base, k))
    for x in range(16):
        im.putpixel((x, 7), shade(joint, rnd.randint(-2, 2)))
        im.putpixel((x, 15), shade(joint, rnd.randint(-2, 2)))
    for y in range(0, 7):
        im.putpixel((10, y), shade(joint, rnd.randint(-2, 2)))
    for y in range(8, 15):
        im.putpixel((4, y), shade(joint, rnd.randint(-2, 2)))
    if band == 'lower':
        for x in (rnd.randrange(1, 9), rnd.randrange(11, 15)):
            for y in range(rnd.randrange(0, 3), rnd.randrange(9, 15)):
                if y in (7, 15):
                    continue
                im.putpixel((x, y), shade(im.getpixel((x, y)), 7))
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
    out = {}
    for i, band in enumerate(BANDS):
        out[band] = well(band, 3760 + i)
        out['pier_' + band] = pier(band, 3770 + i)
    out['balustrade'] = iron(3780)
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


def files():
    out = {}
    for name, _ in BLOCKS:
        if name == 'balustrade':
            continue
        out[f'models/block/stair_well_{name}.json'] = {'parent': 'minecraft:block/cube_all', 'textures': {'all': P + name}}
        out[f'blockstates/stair_well_{name}.json'] = {'variants': {'': {'model': NS + f'block/stair_well_{name}'}}}
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
        path = A / 'textures/block/stair_well' / f'{key}.png'
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
    for name, title in BLOCKS:
        key = f'block.the_oldest_house.stair_well_{name}'
        if check:
            assert lang.get(key) == title, key
        else:
            lang[key] = title
    if not check:
        lang_path.write_text(json.dumps(lang, indent=2, ensure_ascii=False) + '\n')
    print(('Verified' if check else 'Drew') + f' the staircase well: {len(images)} textures, {len(data)} models and states.')


if __name__ == '__main__':
    main()
