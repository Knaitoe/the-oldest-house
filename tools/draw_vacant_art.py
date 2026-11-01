"""Vacant Possession (0.4.228): a first-floor flat on moving day, the street below it and the van at the kerb.

Draws, in native Minecraft pixels from fixed seeds: thirteen materials (the flat's faded wallpaper; its worn boards and the boards
the rug kept new; plaster; the landing's runner; the downstairs hall's lino; terrace brick; pavement flags, the kerb and the road;
the van's white panels, a panel with its wheel, and its ribbed floor), and every fitting of the vacant_fitting block, its models
facing north and turned by its facing: the dresser; the rug laid (three blocks square) and rolled; the bed's head and foot and the
bed stood on end; the wardrobe's two halves; the table; the armchair; the taped boxes; the standard lamp lit and off; the last
kitchen chair; the pencil marks where the dresser stood; the scuffs where the bed's legs stood; the bright square where the
wardrobe stood; the tenant's list; the removal docket; a bare bulb lit and off; the van's rear doors; the mailboxes; a street lamp's
post and head; a radiator; and the coat on its hook. Writes the models and block states, the carried piece's item models, and the
blocks' names. --check verifies everything.
"""
from pathlib import Path
import argparse
import json
import random
from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
A = ROOT / 'src/main/resources/assets/the_oldest_house'
NS = 'the_oldest_house:'
P = NS + 'block/vacant/'


def rgb(c):
    c = c.lstrip('#')
    return tuple(int(c[i:i + 2], 16) for i in (0, 2, 4))


def shade(c, k, a=255):
    r, g, b = rgb(c) if isinstance(c, str) else c[:3]
    return (max(0, min(255, r + k)), max(0, min(255, g + k)), max(0, min(255, b + k)), a)


def new(base=None, seed=0, spread=4):
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    if base:
        rnd = random.Random(seed); px = im.load()
        for y in range(16):
            for x in range(16):
                px[x, y] = shade(base, rnd.randint(-spread, spread))
    return im


def fill(im, x0, y0, x1, y1, colour, a=255):
    px = im.load()
    for y in range(max(0, y0), min(16, y1)):
        for x in range(max(0, x0), min(16, x1)):
            px[x, y] = shade(colour, 0, a)


def dot(im, x, y, colour, a=255):
    if 0 <= x < 16 and 0 <= y < 16:
        im.putpixel((x, y), shade(colour, 0, a))


def frame(im, x0, y0, x1, y1, colour):
    for x in range(x0, x1):
        dot(im, x, y0, colour); dot(im, x, y1 - 1, colour)
    for y in range(y0, y1):
        dot(im, x0, y, colour); dot(im, x1 - 1, y, colour)


# ------------------------------------------------------------------------------------------------ materials
def wallpaper(bright=False):
    # A small repeating sprig on a ground gone the colour of weak tea; the square the wardrobe hid is as it was.
    ground, sprig, stem = ('#d9cfa4', '#8a9a62', '#7a6a4a') if not bright else ('#ece2b4', '#6f8f48', '#6a5a3a')
    im = new(base=ground, seed=2280 if not bright else 2281, spread=2)
    for cx, cy in ((3, 3), (11, 11), (11, 3), (3, 11)):
        if (cx + cy) % 8 == 6:
            dot(im, cx, cy, sprig); dot(im, cx - 1, cy + 1, sprig); dot(im, cx + 1, cy + 1, sprig); dot(im, cx, cy + 2, stem)
        else:
            dot(im, cx, cy, sprig); dot(im, cx, cy + 1, stem)
    for y in range(0, 16, 2):
        dot(im, 7, y, shade(ground, -10)[:3])
    return im


def boards(new_boards=False):
    base = '#9a7448' if not new_boards else '#c49a62'
    im = new(base=base, seed=2282 if not new_boards else 2283, spread=6 if not new_boards else 3)
    for y in (3, 7, 11, 15):
        for x in range(16):
            dot(im, x, y, shade(base, -34)[:3])
    rnd = random.Random(2284)
    for row in range(4):
        x = rnd.randrange(16)
        dot(im, x, row * 4 + 1, shade(base, -26)[:3]); dot(im, x, row * 4 + 2, shade(base, -26)[:3])
    if not new_boards:
        for _ in range(14):
            dot(im, rnd.randrange(16), rnd.randrange(16), shade(base, -16)[:3])
    return im


def materials():
    m = {}
    m['wallpaper'] = wallpaper()
    m['boards'] = boards()
    m['boards_new'] = boards(True)
    m['plaster'] = new(base='#d8d2c4', seed=2285, spread=3)
    carpet = new(base='#7a3a32', seed=2286, spread=4)
    fill(carpet, 0, 0, 16, 2, '#5a2a24'); fill(carpet, 0, 14, 16, 16, '#5a2a24')
    for x in range(0, 16, 3):
        dot(carpet, x, 4, '#8a4a40'); dot(carpet, x + 1, 11, '#8a4a40')
    m['stair_carpet'] = carpet
    lino = new(base='#8a9480', seed=2287, spread=4)
    rnd = random.Random(2288)
    for _ in range(26):
        dot(lino, rnd.randrange(16), rnd.randrange(16), rnd.choice(['#9aa48e', '#7a8470', '#a89a80']))
    m['lino'] = lino
    brick = new(base='#8a5240', seed=2289, spread=8)
    for y in range(0, 16, 4):
        for x in range(16):
            dot(brick, x, y, '#b0a690')
    for y in range(16):
        dot(brick, 0 if (y // 4) % 2 == 0 else 8, y, '#b0a690')
    m['brick'] = brick
    flag = new(base='#9a9a96', seed=2290, spread=5)
    for x in range(16):
        dot(flag, x, 0, '#7a7a76')
    for y in range(16):
        dot(flag, 0, y, '#7a7a76')
    m['pavement'] = flag
    kerb = new(base='#b4b2aa', seed=2291, spread=5)
    fill(kerb, 0, 0, 16, 3, '#8a8882')
    for y in range(0, 16, 8):
        dot(kerb, 0, y, '#6a6862')
    m['kerb'] = kerb
    road = new(base='#3c3c3e', seed=2292, spread=6)
    rnd = random.Random(2293)
    for _ in range(30):
        dot(road, rnd.randrange(16), rnd.randrange(16), rnd.choice(['#4c4c4e', '#2c2c2e', '#56565a']))
    m['road'] = road
    panel = new(base='#e6e6e2', seed=2294, spread=2)
    for x in range(16):
        dot(panel, x, 15, '#b8b8b4')
    for y in range(16):
        dot(panel, 15, y, '#c8c8c4')
    m['van_panel'] = panel
    wheel = panel.copy()
    fill(wheel, 1, 6, 15, 16, '#2a2a2c')
    fill(wheel, 2, 8, 14, 16, '#121214')
    fill(wheel, 5, 10, 11, 16, '#8a8a8e'); fill(wheel, 7, 12, 9, 14, '#4a4a4e')
    m['van_wheel'] = wheel
    floor = new(base='#6a6a64', seed=2295, spread=3)
    for y in range(1, 16, 3):
        for x in range(16):
            dot(floor, x, y, '#8a8a84')
    m['van_floor'] = floor
    return m


# ------------------------------------------------------------------------------------------------ fittings
def wood(base='#6a4628', seed=2300):
    im = new(base=base, seed=seed, spread=4)
    rnd = random.Random(seed + 1)
    for _ in range(5):
        y = rnd.randrange(16)
        for x in range(16):
            if rnd.random() < .6:
                dot(im, x, y, shade(base, -14)[:3])
    return im


def dresser_front():
    im = wood('#7a5432', 2302)
    for y0 in (1, 6, 11):
        frame(im, 1, y0, 15, y0 + 4, '#4a3018')
        dot(im, 7, y0 + 2, '#c8a860'); dot(im, 8, y0 + 2, '#c8a860')
    return im


def rug(part):
    # Nine pieces of one rug: centre, edge (border along the north), corner (border north and west).
    im = new(base='#8a3a3a', seed=2310 + part, spread=4)
    rnd = random.Random(2313 + part)
    for _ in range(12):
        dot(im, rnd.randrange(16), rnd.randrange(16), '#a85a4a')
    if part == 0:
        for i in range(4, 12):
            dot(im, i, 8, '#d8b878'); dot(im, 8, i, '#d8b878')
        dot(im, 8, 8, '#2a3a5a')
    if part >= 1:
        fill(im, 0, 0, 16, 3, '#2a3a5a'); fill(im, 0, 3, 16, 4, '#d8b878')
        for x in range(0, 16, 2):
            dot(im, x, 0, '#e8d8b0')
    if part == 2:
        fill(im, 0, 0, 3, 16, '#2a3a5a'); fill(im, 3, 3, 4, 16, '#d8b878')
    return im


def rug_roll():
    im = new(base='#2a3a5a', seed=2316, spread=3)
    for x in range(0, 16, 4):
        for y in range(16):
            dot(im, x, y, '#8a3a3a')
    return im


def rug_end():
    im = new(base='#8a3a3a', seed=2317, spread=3)
    for r in range(1, 7, 2):
        frame(im, 8 - r, 8 - r, 8 + r, 8 + r, '#d8b878' if r % 4 == 1 else '#2a3a5a')
    return im


def blanket():
    im = new(base='#5a6a8a', seed=2320, spread=3)
    for y in range(0, 16, 4):
        for x in range(16):
            dot(im, x, y, '#4a5a7a')
    for x in range(0, 16, 4):
        for y in range(16):
            dot(im, x, y, '#6a7a9a')
    return im


def wardrobe_front(upper):
    im = wood('#6a4a2c', 2330 + upper)
    for x0 in (1, 8):
        frame(im, x0, 0 if upper else 1, x0 + 7, 16 if not upper else 14, '#3a2414')
    if not upper:
        dot(im, 7, 3, '#c8a860'); dot(im, 8, 3, '#c8a860')
    else:
        dot(im, 7, 12, '#c8a860'); dot(im, 8, 12, '#c8a860')
    return im


def fabric():
    im = new(base='#6a7a52', seed=2340, spread=5)
    for y in range(0, 16, 2):
        for x in range((y // 2) % 2, 16, 2):
            dot(im, x, y, '#5a6a44')
    return im


def box_side():
    im = new(base='#b8925a', seed=2350, spread=4)
    fill(im, 6, 0, 10, 16, '#d8c8a0')
    for x in range(1, 5):
        dot(im, x, 11, '#3a3a3a')
    return im


def box_top():
    im = new(base='#b8925a', seed=2351, spread=4)
    fill(im, 0, 7, 16, 9, '#d8c8a0'); fill(im, 6, 0, 10, 16, '#d8c8a0')
    return im


def shade_tex(lit):
    base = '#f0d890' if lit else '#a89878'
    im = new(base=base, seed=2360 + lit, spread=2 if lit else 4)
    for y in (1, 14):
        for x in range(16):
            dot(im, x, y, shade(base, -30)[:3])
    return im


def marks(upper):
    # Pencil lines up the wall, a pair of initials beside each: the top line is in the upper half.
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    rows = (13, 9, 5, 2) if not upper else (13, 10)
    for i, y in enumerate(rows):
        w = 5 if (i + upper) % 2 == 0 else 4
        for x in range(6, 6 + w):
            dot(im, x, y, '#4a4a52', 200)
        dot(im, 12, y - 1, '#5a5a62', 170); dot(im, 13, y - 1, '#5a5a62', 170); dot(im, 12, y, '#5a5a62', 150)
    return im


def scratches():
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    for cx, cy in ((2, 2), (13, 2), (2, 13), (13, 13)):
        for dx, dy in ((0, 0), (1, 0), (0, 1), (-1, 1), (1, 1), (2, 0)):
            dot(im, cx + dx, cy + dy, '#5a3a20', 150)
    return im


def square(upper):
    im = wallpaper(True)
    for i in range(16):
        dot(im, 0, i, '#c8be92'); dot(im, 15, i, '#c8be92')
        dot(im, i, 0 if upper else 15, '#c8be92')
    return im


def paper(seed, lines, hand='#2a2a4a'):
    im = new(base='#ece6d4', seed=seed, spread=2)
    rnd = random.Random(seed + 1)
    for y in lines:
        w = rnd.randint(4, 10)
        for x in range(3, 3 + w):
            if rnd.random() < .85:
                dot(im, x, y, hand)
    return im


def list_tex():
    im = paper(2370, (3, 4, 5, 6, 7, 8, 9, 10))
    for x in range(3, 9):
        dot(im, x, 12, '#2a2a4a')
    for c in ((0, 0), (15, 0)):
        dot(im, c[0], c[1], '#d8d0a8'); dot(im, c[0], c[1] + 1, '#d8d0a8')
    return im


def docket():
    im = new(base='#8a6a42', seed=2372, spread=3)
    fill(im, 2, 1, 14, 15, '#f0ecdc')
    for y in (3, 5, 7, 9, 11, 13):
        for x in range(3, 13):
            dot(im, x, y, '#a8a49a')
    for x in range(3, 8):
        dot(im, x, 3, '#2a2a4a')
    fill(im, 6, 0, 10, 2, '#9a9a9e')
    return im


def bulb(lit):
    im = new(base='#fff2b8' if lit else '#b8b8ae', seed=2374 + lit, spread=2)
    if lit:
        fill(im, 6, 6, 10, 10, '#ffffe8')
    return im


def van_door(upper):
    im = materials()['van_panel'].copy()
    if upper:
        fill(im, 2, 2, 14, 10, '#2a3a44'); fill(im, 3, 3, 6, 5, '#5a6a74')
    else:
        fill(im, 12, 2, 14, 4, '#3a3a3c')
    for y in range(16):
        dot(im, 0, y, '#a8a8a4')
    return im


def mailboxes():
    im = new(base='#5a6a6a', seed=2380, spread=2)
    for x0 in (0, 8):
        for y0 in (0, 8):
            frame(im, x0, y0, x0 + 8, y0 + 8, '#3a4646')
            fill(im, x0 + 2, y0 + 3, x0 + 6, y0 + 4, '#1a1e1e')
    fill(im, 9, 10, 15, 12, '#d8cca8')
    return im


def radiator():
    im = new(base='#e8e6de', seed=2382, spread=2)
    for x in range(1, 16, 2):
        for y in range(16):
            dot(im, x, y, '#c8c6be')
    return im


def coat():
    im = new(base='#4a4238', seed=2384, spread=4)
    for y in range(16):
        dot(im, 7, y, '#3a3228')
    dot(im, 6, 4, '#c8b890'); dot(im, 6, 8, '#c8b890'); dot(im, 6, 12, '#c8b890')
    return im


def lamp_glass(lit):
    return new(base='#ffe8a0' if lit else '#8a8a7a', seed=2386 + lit, spread=2)


def textures():
    out = {f'block/vacant/{k}.png': v for k, v in materials().items()}
    t = {
        'wood': wood(), 'dresser_front': dresser_front(), 'rug_centre': rug(0), 'rug_edge': rug(1), 'rug_corner': rug(2),
        'rug_roll': rug_roll(), 'rug_end': rug_end(), 'blanket': blanket(), 'pillow': new(base='#e8e4d8', seed=2321, spread=2),
        'bed_wood': wood('#5a3a22', 2322), 'bed_frame': wood('#4a3020', 2323), 'wardrobe_low': wardrobe_front(0),
        'wardrobe_high': wardrobe_front(1), 'wardrobe_side': wood('#6a4a2c', 2332), 'table_top': wood('#8a6a42', 2335),
        'fabric': fabric(), 'box_side': box_side(), 'box_top': box_top(), 'brass': new(base='#b0904a', seed=2362, spread=3),
        'shade_on': shade_tex(1), 'shade_off': shade_tex(0), 'chair_wood': wood('#c8b48a', 2365), 'marks_low': marks(0),
        'marks_high': marks(1), 'scratches': scratches(), 'square_low': square(0), 'square_high': square(1), 'list': list_tex(),
        'docket': docket(), 'bulb_on': bulb(1), 'bulb_off': bulb(0), 'cord': new(base='#2a2a2a', seed=2376, spread=1),
        'van_door_low': van_door(0), 'van_door_high': van_door(1), 'metal': new(base='#5a6a6a', seed=2381, spread=2),
        'mailboxes': mailboxes(), 'lamp_post': new(base='#2a3a32', seed=2385, spread=2), 'lamp_on': lamp_glass(1),
        'lamp_off': lamp_glass(0), 'radiator': radiator(), 'coat': coat(),
    }
    out.update({f'block/vacant/{k}.png': v for k, v in t.items()})
    return out


def element(lo, hi, textures, uv=None, rotation=None):
    faces = {}
    for f in ('north', 'south', 'east', 'west', 'up', 'down'):
        name = textures.get(f, textures.get('all'))
        if name is None:
            continue
        faces[f] = {'texture': '#' + name}
        if uv and f in uv:
            faces[f]['uv'] = uv[f]
        if rotation and f in rotation:
            faces[f]['rotation'] = rotation[f]
    return {'from': lo, 'to': hi, 'faces': faces}


def model(elements, textures, particle):
    return {'ambientocclusion': False, 'render_type': 'minecraft:cutout', 'textures': {**{k: P + v for k, v in textures.items()}, 'particle': P + particle}, 'elements': elements}


def legs(x0, z0, x1, z1, h, w=1.5, tex='w'):
    return [element([x, 0, z], [x + w, h, z + w], {'all': tex}) for x in (x0, x1 - w) for z in (z0, z1 - w)]


def fitting_models():
    m = {}
    m['dresser_0'] = model([element([1, 1, 5], [15, 13, 16], {'all': 'w', 'north': 'f'}), element([0.5, 13, 4.5], [15.5, 14, 16], {'all': 'w'})] + legs(1, 5, 15, 16, 1), {'w': 'wood', 'f': 'dresser_front'}, 'wood')
    # The rug lies three blocks square: a piece for each of its nine cells, the border turned to face out.
    rug = []
    for i, x in enumerate((-16, 0, 16)):
        for j, z in enumerate((-16, 0, 16)):
            edge = (i != 1) + (j != 1)
            tex = 'c' if edge == 0 else 'e' if edge == 1 else 'k'
            rot = {(1, 0): 0, (2, 1): 90, (1, 2): 180, (0, 1): 270, (0, 0): 0, (2, 0): 90, (2, 2): 180, (0, 2): 270}.get((i, j), 0)
            rug.append(element([x, 0, z], [x + 16, 0.5, z + 16], {'up': tex, 'down': tex}, rotation={'up': rot}))
    m['rug_0'] = model(rug, {'c': 'rug_centre', 'e': 'rug_edge', 'k': 'rug_corner'}, 'rug_centre')
    m['rug_1'] = model([element([1, 0, 5], [15, 5, 11], {'north': 'r', 'south': 'r', 'up': 'r', 'down': 'r', 'east': 'n', 'west': 'n'})], {'r': 'rug_roll', 'n': 'rug_end'}, 'rug_roll')
    m['bed_0'] = model([element([0, 2, 0], [16, 6, 14], {'all': 'f'}), element([0.5, 6, 0], [15.5, 9, 14], {'all': 'b', 'up': 'b'}),
                        element([2, 9, 9], [14, 11, 13.5], {'all': 'p'}), element([0, 0, 14], [16, 15, 16], {'all': 'w'})] + legs(0, 0, 16, 14, 2, 2, 'f'),
                       {'f': 'bed_frame', 'b': 'blanket', 'p': 'pillow', 'w': 'bed_wood'}, 'blanket')
    m['bed_1'] = model([element([0, 2, 2], [16, 6, 16], {'all': 'f'}), element([0.5, 6, 2], [15.5, 9, 16], {'all': 'b'}), element([0, 0, 0], [16, 10, 2], {'all': 'w'})] + legs(0, 2, 16, 16, 2, 2, 'f'),
                       {'f': 'bed_frame', 'b': 'blanket', 'w': 'bed_wood'}, 'blanket')
    m['bed_2'] = model([element([1, 0, 9], [15, 16, 16], {'all': 'f', 'north': 'b'}), element([1, 13, 8], [15, 16, 9], {'all': 'w'})], {'f': 'bed_frame', 'b': 'blanket', 'w': 'bed_wood'}, 'blanket')
    m['wardrobe_0'] = model([element([1, 0, 3], [15, 16, 16], {'all': 's', 'north': 'f'})], {'s': 'wardrobe_side', 'f': 'wardrobe_low'}, 'wardrobe_side')
    m['wardrobe_1'] = model([element([1, 0, 3], [15, 14, 16], {'all': 's', 'north': 'f'}), element([0.5, 14, 2.5], [15.5, 16, 16], {'all': 's'})], {'s': 'wardrobe_side', 'f': 'wardrobe_high'}, 'wardrobe_side')
    m['table_0'] = model([element([1, 12, 1], [15, 13, 15], {'all': 'w', 'up': 't'})] + legs(2, 2, 14, 14, 12), {'w': 'wood', 't': 'table_top'}, 'table_top')
    m['armchair_0'] = model([element([1, 2, 1], [15, 7, 15], {'all': 'c'}), element([1, 7, 12], [15, 13, 16], {'all': 'c'}),
                             element([1, 7, 1], [3, 11, 12], {'all': 'c'}), element([13, 7, 1], [15, 11, 12], {'all': 'c'})] + legs(1, 1, 15, 16, 2, tex='w'),
                            {'c': 'fabric', 'w': 'wood'}, 'fabric')
    m['boxes_0'] = model([element([1, 0, 1], [15, 8, 15], {'all': 's', 'up': 't', 'down': 't'}), element([3, 8, 3], [13, 14, 13], {'all': 's', 'up': 't', 'down': 't'})], {'s': 'box_side', 't': 'box_top'}, 'box_side')
    for n, lit in enumerate(('shade_on', 'shade_off')):
        m[f'lamp_{n}'] = model([element([5, 0, 5], [11, 1, 11], {'all': 'b'}), element([7.5, 1, 7.5], [8.5, 11, 8.5], {'all': 'b'}),
                                element([4, 11, 4], [12, 16, 12], {'north': 's', 'south': 's', 'east': 's', 'west': 's', 'down': 's'})], {'b': 'brass', 's': lit}, lit)
    m['chair_0'] = model([element([3, 7, 3], [13, 8, 13], {'all': 'w'})] + legs(3, 3, 13, 13.5, 7) +
                         [element([3, 8, 12], [4.5, 16, 13.5], {'all': 'w'}), element([11.5, 8, 12], [13, 16, 13.5], {'all': 'w'}), element([3, 13, 12], [13, 15.5, 13.5], {'all': 'w'})],
                         {'w': 'chair_wood'}, 'chair_wood')
    for n in range(2):
        m[f'marks_{n}'] = model([element([0, 0, 15.5], [16, 16, 16], {'north': 't'})], {'t': ('marks_low', 'marks_high')[n]}, ('marks_low', 'marks_high')[n])
        m[f'square_{n}'] = model([element([0, 0, 15.6], [16, 16, 16], {'north': 't'})], {'t': ('square_low', 'square_high')[n]}, ('square_low', 'square_high')[n])
        m[f'bulb_{n}'] = model([element([7.5, 12, 7.5], [8.5, 16, 8.5], {'all': 'c'}), element([6.5, 8, 6.5], [9.5, 12, 9.5], {'all': 'b'})], {'c': 'cord', 'b': ('bulb_on', 'bulb_off')[n]}, 'cord')
        m[f'van_door_{n}'] = model([element([0, 0, 13], [16, 16, 16], {'all': 'p', 'north': 'd', 'south': 'd'})], {'p': 'van_panel', 'd': ('van_door_low', 'van_door_high')[n]}, 'van_panel')
    m['scratches_0'] = model([element([0, 0, 0], [16, 0.25, 16], {'up': 't'})], {'t': 'scratches'}, 'scratches')
    m['list_0'] = model([element([4, 2, 15.5], [12, 14, 16], {'north': 't', 'south': 't'}, {'north': [4, 2, 12, 14], 'south': [4, 2, 12, 14]})], {'t': 'list'}, 'list')
    m['docket_0'] = model([element([3, 0, 3], [13, 1, 13], {'all': 'w', 'up': 't'}, {'up': [3, 1, 13, 15]})], {'w': 'wood', 't': 'docket'}, 'docket')
    m['mailboxes_0'] = model([element([0, 2, 11], [16, 16, 16], {'all': 'm', 'north': 'f'})], {'m': 'metal', 'f': 'mailboxes'}, 'metal')
    m['streetlamp_0'] = model([element([6, 0, 6], [10, 16, 10], {'all': 'p'})], {'p': 'lamp_post'}, 'lamp_post')
    m['streetlamp_1'] = model([element([6, 0, 6], [10, 2, 10], {'all': 'p'}), element([4, 2, 0], [12, 6, 12], {'all': 'p', 'down': 'g'})], {'p': 'lamp_post', 'g': 'lamp_on'}, 'lamp_post')
    m['radiator_0'] = model([element([1, 1, 12], [15, 11, 16], {'all': 's', 'north': 'r'})] + [element([x, 0, 13], [x + 1, 1, 15], {'all': 's'}) for x in (2, 13)], {'s': 'radiator', 'r': 'radiator'}, 'radiator')
    m['hooks_0'] = model([element([1, 12, 15], [15, 14, 16], {'all': 'b'}), element([3, 2, 13], [13, 13, 15.5], {'all': 'c'})], {'b': 'brass', 'c': 'coat'}, 'coat')
    return m


KINDS = ['dresser', 'rug', 'bed', 'wardrobe', 'table', 'armchair', 'boxes', 'lamp', 'chair', 'marks', 'scratches', 'square', 'list', 'docket', 'bulb', 'van_door', 'mailboxes', 'streetlamp', 'radiator', 'hooks']
STAGES = {'rug': 2, 'bed': 3, 'wardrobe': 2, 'lamp': 2, 'marks': 2, 'square': 2, 'bulb': 2, 'van_door': 2, 'streetlamp': 2}
PIECES = ['dresser', 'rug', 'bed', 'wardrobe', 'table', 'armchair', 'boxes', 'lamp', 'chair']
# The piece in the reader's arms is drawn as it goes into the van: the rug rolled, the bed on end, the lamp off.
CARRIED = {'rug': 1, 'bed': 2, 'lamp': 1}
FACING_Y = {'north': 0, 'east': 90, 'south': 180, 'west': 270}
MAX_STAGE = 3
DISPLAY = {
    'gui': {'rotation': [30, 225, 0], 'translation': [0, 0, 0], 'scale': [0.5, 0.5, 0.5]},
    'ground': {'rotation': [0, 0, 0], 'translation': [0, 3, 0], 'scale': [0.25, 0.25, 0.25]},
    'fixed': {'rotation': [0, 0, 0], 'translation': [0, 0, 0], 'scale': [0.5, 0.5, 0.5]},
    'thirdperson_righthand': {'rotation': [75, 45, 0], 'translation': [0, 2.5, 0], 'scale': [0.375, 0.375, 0.375]},
    'thirdperson_lefthand': {'rotation': [75, 45, 0], 'translation': [0, 2.5, 0], 'scale': [0.375, 0.375, 0.375]},
    'firstperson_righthand': {'rotation': [0, 45, 0], 'translation': [0, -2, 0], 'scale': [0.6, 0.6, 0.6]},
    'firstperson_lefthand': {'rotation': [0, 225, 0], 'translation': [0, -2, 0], 'scale': [0.6, 0.6, 0.6]},
}
NAMES = {'vacant_fitting': 'Furniture', 'vacant_wallpaper': 'Faded Wallpaper', 'vacant_boards': 'Worn Boards', 'vacant_boards_new': 'Unfaded Boards',
         'vacant_plaster': 'Plaster', 'vacant_stair_carpet': 'Stair Runner', 'vacant_lino': 'Hall Lino', 'vacant_brick': 'Terrace Brick',
         'vacant_pavement': 'Paving Flag', 'vacant_kerb': 'Kerbstone', 'vacant_road': 'Road', 'vacant_van_panel': 'Van Panel',
         'vacant_van_wheel': 'Van Wheel', 'vacant_van_floor': 'Van Floor'}
ITEM_NAMES = {'item.the_oldest_house.vacant_piece': 'Furniture'}


def files():
    out = {}
    for name in materials():
        out[f'models/block/vacant_{name}.json'] = {'parent': 'minecraft:block/cube_all', 'textures': {'all': P + name}}
        out[f'blockstates/vacant_{name}.json'] = {'variants': {'': {'model': NS + f'block/vacant_{name}'}}}
    for key, obj in fitting_models().items():
        out[f'models/block/vacant/{key}.json'] = obj
    variants = {}
    for kind in KINDS:
        for facing, y in FACING_Y.items():
            for stage in range(MAX_STAGE):
                drawn = stage if stage < STAGES.get(kind, 1) else 0
                v = {'model': NS + f'block/vacant/{kind}_{drawn}'}
                if y:
                    v['y'] = y
                variants[f'facing={facing},kind={kind},stage={stage}'] = v
    out['blockstates/vacant_fitting.json'] = {'variants': variants}
    for kind in PIECES:
        out[f'models/item/vacant_piece_{kind}.json'] = {'parent': NS + f'block/vacant/{kind}_{CARRIED.get(kind, 0)}', 'display': DISPLAY}
    out['models/item/vacant_piece.json'] = {'parent': NS + 'item/vacant_piece_boxes',
                                            'overrides': [{'predicate': {'custom_model_data': i + 1}, 'model': NS + f'item/vacant_piece_{kind}'} for i, kind in enumerate(PIECES)]}
    return out


def main():
    check = argparse.ArgumentParser()
    check.add_argument('--check', action='store_true')
    check = check.parse_args().check
    images, data = textures(), files()
    models = fitting_models()
    for kind in KINDS:
        for stage in range(STAGES.get(kind, 1)):
            assert f'{kind}_{stage}' in models, (kind, stage)
    for key, obj in models.items():
        for tex in obj['textures'].values():
            assert f'block/vacant/{tex.split("vacant/")[1]}.png' in images, (key, tex)
    for rel, im in images.items():
        path = A / 'textures' / rel
        if check:
            with Image.open(path) as actual:
                assert actual.convert('RGBA').tobytes() == im.tobytes(), rel
        else:
            path.parent.mkdir(parents=True, exist_ok=True); im.save(path, optimize=True)
    for rel, obj in data.items():
        path = A / rel
        if check:
            assert json.loads(path.read_text()) == obj, rel
        else:
            path.parent.mkdir(parents=True, exist_ok=True); path.write_text(json.dumps(obj, indent=2) + '\n')
    lang_path = A / 'lang/en_us.json'; lang = json.loads(lang_path.read_text())
    wanted = {**{f'block.the_oldest_house.{k}': v for k, v in NAMES.items()}, **ITEM_NAMES}
    for key, name in wanted.items():
        if check:
            assert lang.get(key) == name, key
        else:
            lang[key] = name
    if not check:
        lang_path.write_text(json.dumps(lang, indent=2, ensure_ascii=False) + '\n')
    print(('Verified' if check else 'Drew') + f' the flat: {len(images)} textures, {len(data)} models and states.')


if __name__ == '__main__':
    main()
