"""The trial rooms (0.4.385, docs/STILL_LIGHT_0_4_385.md): materials and fittings for the new trials of
docs/HALLS_TRIALS_AND_TIME_DESIGN.md.

Draws, in native Minecraft pixels from fixed seeds:

- the still light: a wainscot of boards stained nearly black, a ceiling of smoke-dark plaster, floorboards worn pale down the
  middle where the way across runs, the same boards dusty and unworn where it does not, wet brick under the floor, and a caged
  ceiling lamp, dark and lit.

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
P = NS + 'block/trial/'

STAIN = '#2c221c'
STAIN_SEAM = '#17110d'
SMOKE = '#3b3733'
BOARD = '#6e5845'
BOARD_SEAM = '#3e3025'
DUST = '#57504a'
BRICK = '#4b3e36'
MORTAR = '#2a2622'
DAMP = '#25302a'
CAGE = '#2a2a28'


def rgb(c):
    c = c.lstrip('#')
    return tuple(int(c[i:i + 2], 16) for i in (0, 2, 4))


def shade(c, k, a=255):
    r, g, b = rgb(c) if isinstance(c, str) else c[:3]
    return (max(0, min(255, r + k)), max(0, min(255, g + k)), max(0, min(255, b + k)), a)


def mix(c, d, t):
    a, b = rgb(c) if isinstance(c, str) else c[:3], rgb(d) if isinstance(d, str) else d[:3]
    return tuple(int(round(a[i] + (b[i] - a[i]) * t)) for i in range(3)) + (255,)


def wainscot(seed):
    """Four narrow boards to a block, stained nearly black and rubbed paler along their edges, a rail across the top."""
    rnd = random.Random(seed)
    im = Image.new('RGBA', (16, 16))
    tones = [rnd.randint(-3, 3) for _ in range(4)]
    for y in range(16):
        for x in range(16):
            board = x // 4
            k = tones[board] + rnd.randint(-2, 2) + (3 if x % 4 in (1, 2) else 0)
            if rnd.random() < .08:
                k -= 4
            im.putpixel((x, y), shade(STAIN, k))
    for y in range(16):
        for x in (0, 4, 8, 12):
            im.putpixel((x, y), shade(STAIN_SEAM, rnd.randint(-1, 1)))
    for x in range(16):
        im.putpixel((x, 0), shade(STAIN, 10))
        im.putpixel((x, 1), shade(STAIN_SEAM, 0))
    return im


def smoke(seed):
    """Plaster gone the colour of old smoke, and a hair crack wandering across it."""
    rnd = random.Random(seed)
    im = Image.new('RGBA', (16, 16))
    for y in range(16):
        for x in range(16):
            im.putpixel((x, y), shade(SMOKE, rnd.randint(-3, 3)))
    x, y = rnd.randrange(2, 6), 0
    while y < 16:
        im.putpixel((x, y), shade(SMOKE, -14))
        y += 1
        x = max(0, min(15, x + rnd.choice((-1, 0, 0, 1))))
    return im


def boards(seed, worn):
    """Four boards a block, running the length of the room, nail heads at their ends. Worn: paler and smoother towards the middle
    where feet have gone, the grain rubbed faint. Unworn: a film of grey dust over the same boards, flecked, nothing rubbed away."""
    rnd = random.Random(seed)
    im = Image.new('RGBA', (16, 16))
    tones = [rnd.randint(-5, 5) for _ in range(4)]
    for y in range(16):
        for x in range(16):
            board = x // 4
            k = tones[board] + rnd.randint(-2, 2)
            if (y + board * 5) % 7 == 0 and x % 4 in (1, 2):
                k -= 5
            c = shade(BOARD, k)
            if worn:
                c = mix(c, '#b49a7a', max(0.0, .55 - abs(x - 7.5) * .07))
            else:
                c = mix(c, DUST, .45)
            im.putpixel((x, y), c)
    for y in range(16):
        for x in (0, 4, 8, 12):
            im.putpixel((x, y), shade(BOARD_SEAM, rnd.randint(-2, 2) + (8 if worn else -4)))
    for x in (2, 6, 10, 14):
        im.putpixel((x, rnd.choice((1, 2))), shade('#2a2420', 0))
    if not worn:
        for _ in range(18):
            x, y = rnd.randrange(16), rnd.randrange(16)
            im.putpixel((x, y), shade(DUST, rnd.choice((8, 12, 16))))
    return im


def brick(seed):
    """Small old brick, wet, mortar standing proud, the bottom course gone dark green with damp."""
    rnd = random.Random(seed)
    im = Image.new('RGBA', (16, 16))
    for y in range(16):
        for x in range(16):
            row = y // 4
            off = 4 if row % 2 else 0
            mortar = y % 4 == 3 or (x + off) % 8 == 7
            if mortar:
                c = shade(MORTAR, rnd.randint(-2, 2))
            else:
                c = shade(BRICK, rnd.randint(-6, 6) + ((x + off) // 8 % 2) * 3)
                if y >= 12:
                    c = mix(c, DAMP, .55)
            im.putpixel((x, y), c)
    for _ in range(4):
        x, y = rnd.randrange(16), rnd.randrange(8, 12)
        im.putpixel((x, y), shade(BRICK, 22))
    return im


def lamp(lit):
    """The lamp's sheet: a short rod (left), and a glass globe in a wire cage (right half), dark or lit from inside."""
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    for y in range(16):
        for x in range(2):
            im.putpixel((x, y), shade(CAGE, 6 if x == 0 else -2))
    glass_dark, glass_lit = '#3d4038', '#f3d98e'
    for y in range(16):
        for x in range(8, 16):
            if lit:
                d = abs(x - 11.5) + abs(y - 7.5) * .5
                c = mix('#fff4cf', glass_lit, min(1.0, d / 5))
            else:
                c = shade(glass_dark, (2 if x == 9 and 3 < y < 11 else 0) - (y // 6))
            im.putpixel((x, y), c)
    for y in range(16):
        for x in range(8, 16):
            if x in (8, 15) or y in (0, 5, 10, 15) or x == 11:
                im.putpixel((x, y), shade(CAGE, 0))
    return im


MATERIALS = [
    ('still_wall', 'Blackened Wainscot'),
    ('still_ceiling', 'Smoke-Dark Plaster'),
    ('still_path', 'Worn Boards'),
    ('still_under', 'Wet Brick'),
]
def textures():
    return {
        'still_wall': wainscot(3850), 'still_ceiling': smoke(3851), 'still_path': boards(3852, True),
        'still_board': boards(3852, False), 'still_under': brick(3853), 'still_lamp_off': lamp(False), 'still_lamp_on': lamp(True),
    }


def lamp_model(tex):
    t = P + tex

    def faces(u0, v0, u1, v1):
        return {f: {'texture': '#lamp', 'uv': [u0, v0, u1, v1]} for f in ('north', 'south', 'east', 'west', 'up', 'down')}
    return {
        'ambientocclusion': False, 'render_type': 'minecraft:cutout', 'textures': {'lamp': t, 'particle': t},
        'elements': [
            {'from': [7, 12, 7], 'to': [9, 16, 9], 'faces': faces(0, 0, 2, 4)},
            {'from': [5, 5, 5], 'to': [11, 12, 11], 'faces': faces(9, 2, 15, 9)},
            {'from': [6, 12, 6], 'to': [10, 13, 10], 'faces': faces(8, 0, 12, 1)},
        ],
    }


def files():
    out = {}
    for name, _ in MATERIALS:
        out[f'models/block/trial_{name}.json'] = {'parent': 'minecraft:block/cube_all', 'textures': {'all': P + name}}
        model = NS + f'block/trial_{name}'
        variant = {'model': model}
        if name == 'still_path':
            variant = [{'model': model}, {'model': model, 'x': 0, 'y': 180}]
        out[f'blockstates/trial_{name}.json'] = {'variants': {'': variant}}
    out['models/block/trial_still_board.json'] = {'parent': 'minecraft:block/cube_all', 'textures': {'all': P + 'still_board'}}
    out['models/block/trial_still_board_gone.json'] = {'textures': {'particle': P + 'still_board'}, 'elements': []}
    out['models/block/trial_still_lamp_off.json'] = lamp_model('still_lamp_off')
    out['models/block/trial_still_lamp_on.json'] = lamp_model('still_lamp_on')
    variants = {
        'kind=still_board,stage=0': {'model': NS + 'block/trial_still_board'},
        'kind=still_board,stage=1': {'model': NS + 'block/trial_still_board_gone'},
        'kind=still_lamp,stage=0': {'model': NS + 'block/trial_still_lamp_off'},
        'kind=still_lamp,stage=1': {'model': NS + 'block/trial_still_lamp_on'},
    }
    out['blockstates/trial_fitting.json'] = {'variants': variants}
    return out


def names():
    out = {f'block.the_oldest_house.trial_{name}': title for name, title in MATERIALS}
    out['block.the_oldest_house.trial_fitting'] = 'Trial Fitting'
    return out


def main():
    check = argparse.ArgumentParser()
    check.add_argument('--check', action='store_true')
    check = check.parse_args().check
    images, data = textures(), files()
    for key, im in images.items():
        path = A / 'textures/block/trial' / f'{key}.png'
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
    print(('Verified' if check else 'Drew') + f' the trial rooms: {len(images)} textures, {len(data)} models and states.')


if __name__ == '__main__':
    main()
