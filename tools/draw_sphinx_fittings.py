"""The Sphinx (0.4.306): her first crossing's fittings and the vestibule they stand in, in vanilla Minecraft's own manner.

Every texture is 16x16 from a small palette with per-pixel noise, the way vanilla's are drawn: quarry tiles four to a block like vanilla
bricks, a marked tile with a brass stud, limewash plaster with the faint mottling of vanilla's calcite, spruce ceiling boards in vanilla
spruce plank tones, and a plaster seam cracked in a door's shape. The fittings: the umbrella stand, a letter flap (shut, and open on a
real hole), a door latch (down and lifted), the stop pin (hidden, exposed, pulled), a grille, and her fingers, sandstone-coloured skin
painted like stone. Writes models, block states and names. --check verifies everything.
"""
from pathlib import Path
import argparse
import json
import random
from PIL import Image

from draw_tank_fittings import shade, fill, dot, frame, line, element

ROOT = Path(__file__).resolve().parents[1]
A = ROOT / 'src/main/resources/assets/the_oldest_house'
NS = 'the_oldest_house:'
P = NS + 'block/sphinx/'


def noisy(palette, seed, weights=None):
    """Vanilla-style noise: each pixel picks from a few close tones rather than a smooth gradient."""
    im = Image.new('RGBA', (16, 16)); rnd = random.Random(seed)
    for y in range(16):
        for x in range(16):
            im.putpixel((x, y), shade(rnd.choices(palette, weights)[0], 0))
    return im


# ------------------------------------------------------------------------------------------------ materials
TILE = ['#9c5a3c', '#a4613f', '#94553a', '#8c5034']
GROUT = '#5e3a2a'


def tile(seed=3061):
    im = noisy(TILE, seed, [4, 3, 3, 1])
    for i in range(16):
        dot(im, i, 7, GROUT); dot(im, i, 15, GROUT); dot(im, 7, i, GROUT); dot(im, 15, i, GROUT)
    return im


def marked():
    im = tile(3062)
    # A brass stud at the crossing of the joints, catching the light on its top-left like vanilla's gold.
    fill(im, 6, 6, 10, 10, '#c9a43c'); dot(im, 6, 6, '#f2dc6e'); dot(im, 7, 6, '#f2dc6e'); dot(im, 6, 7, '#f2dc6e')
    dot(im, 9, 9, '#7a5e1c'); dot(im, 8, 9, '#8e6e22'); dot(im, 9, 8, '#8e6e22')
    return im


PLASTER = ['#e2ddd2', '#dbd6ca', '#d4cfc2', '#e8e4da']


def plaster(seed=3063):
    return noisy(PLASTER, seed, [5, 4, 2, 2])


def ceiling():
    # Vanilla spruce planks: four boards, the same dark seams and end grain.
    im = noisy(['#735432', '#6b4d2e', '#7a5a36', '#664a2c'], 3064, [4, 3, 2, 2])
    for y in (3, 7, 11, 15):
        for x in range(16):
            dot(im, x, y, '#4f3a22')
    for x, y in ((4, 1), (11, 5), (2, 9), (13, 13)):
        dot(im, x, y, '#4f3a22')
    return im


def seam():
    im = plaster(3065)
    for a, b in (((3, 0), (4, 6)), ((4, 6), (2, 11)), ((2, 11), (4, 16)), ((12, 0), (11, 5)), ((11, 5), (13, 10)), ((13, 10), (12, 16)), ((4, 6), (8, 7)), ((8, 7), (11, 5))):
        line(im, a[0], a[1], b[0], b[1], '#8c867a')
    return im


def dark():
    # Plaster the lamplight never reaches: almost black, the faintest mottle, so walls are walls and then are not.
    return noisy(['#141312', '#181715', '#11100f', '#1b1a18'], 3067, [4, 3, 2, 1])


ASHLAR = ['#a9a28f', '#a09987', '#b2ab98', '#989180']


def ashlar(seed=3091):
    # 0.4.335, the stair: dressed limestone in long courses, two to a block and staggered, like vanilla's stone bricks but paler.
    im = noisy(ASHLAR, seed, [4, 3, 2, 2])
    for x in range(16):
        dot(im, x, 7, '#6e695c'); dot(im, x, 15, '#6e695c')
    for y in range(0, 7):
        dot(im, 4, y, '#6e695c')
    for y in range(8, 15):
        dot(im, 12, y, '#6e695c')
    return im


def flags():
    # Worn flagstones, irregular, darker in the joints and smoothed down the middle where feet go.
    im = noisy(['#8c8778', '#857f71', '#938e7f', '#7d786a'], 3092, [4, 3, 2, 2])
    for a, b in (((0, 5), (9, 4)), ((9, 4), (15, 6)), ((6, 4), (5, 15)), ((5, 11), (15, 12)), ((0, 13), (5, 13))):
        line(im, a[0], a[1], b[0], b[1], '#5c584e')
    return im


def ashlar_seam():
    # Ashlar cracked through in the shape of a stair mouth: her weight came down on it and it closed.
    im = ashlar(3093)
    for a, b in (((2, 16), (2, 4)), ((2, 4), (8, 1)), ((8, 1), (14, 4)), ((14, 4), (14, 16)), ((5, 10), (8, 8)), ((8, 8), (11, 11))):
        line(im, a[0], a[1], b[0], b[1], '#3e3a33')
    return im


ROCK = ['#3a3836', '#33312f', '#42403d', '#2d2b29']


def rock(seed=3101):
    # 0.4.336, the shelf: the dark halls' raw rock, close-grained and fractured, a little damp.
    im = noisy(ROCK, seed, [4, 3, 2, 2])
    for a, b in (((0, 4), (6, 6)), ((6, 6), (10, 3)), ((3, 12), (11, 10)), ((11, 10), (16, 13))):
        line(im, a[0], a[1], b[0], b[1], '#1f1e1c')
    return im


def rock_seam():
    # Rock closed over where a ladder hooked: the crack runs across in a ladder's width.
    im = rock(3102)
    for a, b in (((4, 0), (5, 16)), ((11, 0), (10, 16)), ((5, 5), (10, 4)), ((5, 11), (10, 12))):
        line(im, a[0], a[1], b[0], b[1], '#121110')
    return im


def materials():
    return {'tile': tile(), 'marked': marked(), 'plaster': plaster(), 'ceiling': ceiling(), 'seam': seam(), 'dark': dark(),
            'ashlar': ashlar(), 'flags': flags(), 'ashlar_seam': ashlar_seam(), 'rock': rock(), 'rock_seam': rock_seam()}


# ------------------------------------------------------------------------------------------------ fittings
def iron():
    return noisy(['#5a5a5a', '#4e4e4e', '#646464'], 3070)


def brass():
    im = noisy(['#c9a43c', '#b8932e', '#d6b24a'], 3071)
    return im


def pot():
    # Glazed earthenware with brass bands, like vanilla's flower pot in tone.
    im = noisy(['#7a4632', '#84503a', '#6e3e2c'], 3072)
    for y in (1, 13):
        for x in range(16):
            dot(im, x, y, '#c9a43c'); dot(im, x, y + 1, '#8e6e22')
    return im


def umbrella():
    im = noisy(['#2e3a4a', '#283444', '#34404e'], 3073)
    for y in range(16):
        dot(im, 7, y, '#1c2430')
    return im


def flap():
    im = brass(); frame(im, 0, 0, 16, 16, '#7a5e1c')
    fill(im, 3, 6, 13, 8, '#3a2e14')
    for x in (2, 13):
        dot(im, x, 2, '#f2dc6e')
    return im


def latch(lifted):
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    fill(im, 5, 3, 11, 13, '#4e4e4e'); frame(im, 5, 3, 11, 13, '#2e2e2e')
    if lifted:
        line(im, 7, 9, 12, 4, '#7a7a7a'); line(im, 8, 9, 13, 4, '#5a5a5a')
    else:
        fill(im, 7, 7, 15, 9, '#7a7a7a'); dot(im, 14, 8, '#5a5a5a')
    # A chalk tick by the latch, the way someone marked it before.
    line(im, 1, 11, 2, 12, '#e8e8e0'); line(im, 2, 12, 4, 8, '#e8e8e0')
    return im


def grille():
    im = noisy(['#3e3e3e', '#464646'], 3074)
    for y in range(2, 14, 3):
        fill(im, 2, y, 14, y + 2, '#121212')
    frame(im, 0, 0, 16, 16, '#2a2a2a')
    return im


def skin():
    # Her skin: sandstone ochres with a painted band of faded blue across it, the colour left on old stone.
    im = noisy(['#cfa86a', '#c49c5e', '#d8b476', '#b88f52'], 3075, [4, 3, 2, 2])
    for x in range(16):
        dot(im, x, 9, '#5a7a96'); dot(im, x, 10, '#4e6c86')
    for x in range(0, 16, 4):
        dot(im, x, 5, '#a07a44')
    return im


def nail():
    im = noisy(['#e6d4b0', '#dcc8a2'], 3076)
    frame(im, 0, 0, 16, 16, '#a88c5e')
    return im


def eye_tiles(look=7):
    # Her eye, 64 by 32 pixels across eight blocks: sandstone lids with a painted kohl line that runs out past the outer corner,
    # a yellowed white, an amber iris with a darker rim, a round black pupil and one highlight. Recognizable from the one eye.
    import math
    W, H = 64, 32
    big = Image.new('RGBA', (W, H)); rnd = random.Random(3077)
    for y in range(H):
        for x in range(W):
            big.putpixel((x, y), shade(rnd.choice(['#b38a50', '#a8804a', '#bc9258', '#9e7844']), 0))
    cx, cy, rx, ry = 31.5, 16.5, 27.5, 12.0
    for y in range(H):
        for x in range(W):
            if ((x - cx) / rx) ** 2 + ((y - cy) / ry) ** 2 <= 1:
                big.putpixel((x, y), shade('#e2d6b0', rnd.randint(-8, 4)))
    # The pupil turned by the gaze: left/middle/right across, and (seen from below, where the picture's top is the hall side) toward
    # the hall upward, under her in the middle, away from it downward.
    col, row = look % 3, look // 3
    px, py = cx + (col - 1) * 9, cy + (1 - row) * 4
    for y in range(H):
        for x in range(W):
            d = math.hypot(x - px, y - py)
            if d <= 10.5 and ((x - cx) / rx) ** 2 + ((y - cy) / ry) ** 2 <= 1:
                c = '#0c0a08' if d <= 4.5 else ('#6a4410' if d > 9.2 else '#b07a1e')
                big.putpixel((x, y), shade(c, 0 if d <= 4.5 else rnd.randint(-10, 10)))
    # Beyond the lids the skin falls away into the dark, then nothing: no edge of a block, only the eye.
    for y in range(H):
        for x in range(W):
            e = ((x - cx) / (rx + 1.2)) ** 2 + ((y - cy) / (ry + 1.6)) ** 2
            if e > 1.9:
                big.putpixel((x, y), (0, 0, 0, 0))
            elif e > 1.0:
                big.putpixel((x, y), shade(rnd.choice(['#3e3220', '#352a1b', '#2c2316']), int(-24 * (e - 1.0))))
    for x, y in ((int(px) - 4, int(py) - 4), (int(px) - 3, int(py) - 4), (int(px) - 4, int(py) - 3)):
        big.putpixel((x, y), (250, 245, 220, 255))
    for x in range(2, 62):
        t = (x - cx) / (rx + .5)
        yt = int(round(cy - (ry + .6) * math.sqrt(max(0, 1 - t * t))))
        yb = int(round(cy + (ry + .4) * math.sqrt(max(0, 1 - t * t))))
        for k in range(3):
            big.putpixel((x, max(0, yt - k)), shade('#1a1c2c', 0))
        big.putpixel((x, min(H - 1, yb)), shade('#3a2c1e', 0))
    for i in range(8):
        big.putpixel((min(W - 1, 58 + i // 2), max(0, 15 - i)), shade('#1a1c2c', 0)); big.putpixel((min(W - 1, 59 + i // 2), max(0, 15 - i)), shade('#1a1c2c', 0))
    return [big.crop((c * 16, r * 16, c * 16 + 16, r * 16 + 16)) for r in range(2) for c in range(4)]


def skin_dark():
    # The rest of her face in the dark: the same sandstone, unlit, no painted band; half seen.
    return noisy(['#4a3c26', '#433622', '#52432a', '#3c3020'], 3079, [4, 3, 2, 2])


def fur(seed=3080):
    # Her coat: tawny, coarse, the hairs running down.
    im = noisy(['#b8904e', '#a8803f', '#c49c58', '#9c7438'], seed, [4, 3, 2, 2]); rnd = random.Random(seed + 1)
    for _ in range(18):
        x, y = rnd.randrange(16), rnd.randrange(15)
        dot(im, x, y, '#86622e'); dot(im, x, y + 1, '#86622e')
    return im


def claw():
    # A claw: dark keratin, paler where it grows out of the toe (the top rows), a hard highlight along its curve. 0.4.330 (owner: "one
    # nail per finger"): the pale root band was at the bottom, so each of a claw's two pieces showed its own band and read as two nails;
    # the root is at the top now, drawn once, on the upper piece only.
    im = noisy(['#2a2420', '#221d1a', '#332b25'], 3082)
    for y in range(16):
        dot(im, 2, y, '#5a4e42')
    for y in range(0, 3):
        for x in range(16):
            dot(im, x, y, '#6e5c48')
    return im


def pad():
    return noisy(['#3a2c26', '#33261f', '#43332b'], 3083)


def reach():
    # Her fingers coming down a wall: four long sandstone digits, the painted band at the knuckle.
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0)); rnd = random.Random(3078)
    for x0 in (0, 4, 8, 12):
        for x in range(x0, x0 + 3):
            for y in range(16):
                dot(im, x, y, shade(rnd.choice(['#cfa86a', '#c49c5e', '#d8b476']), 0)[:3])
        for y in range(16):
            dot(im, x0 + 2, y, '#a07a44')
        dot(im, x0, 7, '#5a7a96'); dot(im, x0 + 1, 7, '#5a7a96')
    return im


def socket():
    # 0.4.334: a survey socket, a brass plate screwed to the plaster with a bored hole for a pin, its rim worn bright.
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    fill(im, 4, 3, 12, 13, '#b8932e'); frame(im, 4, 3, 12, 13, '#7a5e1c')
    dot(im, 5, 4, '#f2dc6e'); dot(im, 10, 11, '#5e4614'); dot(im, 5, 11, '#5e4614'); dot(im, 10, 4, '#5e4614')
    fill(im, 7, 7, 9, 9, '#1e1a10'); dot(im, 6, 7, '#f2dc6e'); dot(im, 6, 8, '#d6b24a'); dot(im, 9, 9, '#8e6e22')
    return im


def tag():
    # A strip of surveyor's cloth tied below a pin's ring, a pencil tick on it.
    im = noisy(['#e4dccb', '#dcd3c0', '#ece6d6'], 3090)
    line(im, 4, 9, 6, 11, '#3a3a3a'); line(im, 6, 11, 11, 5, '#3a3a3a')
    return im


def rail():
    # Wrought iron, blacked and worn bright at the top where hands go.
    im = noisy(['#2c2c2e', '#323234', '#28282a'], 3094)
    for x in range(16):
        dot(im, x, 0, '#5e5e62'); dot(im, x, 1, '#48484c')
    return im


def chip():
    return noisy(['#a49d8a', '#968f7d', '#b0a995', '#8a8372'], 3095, [3, 3, 2, 1])


def tray():
    # A shallow spruce tray, its rim darker, loose chips lying in it.
    im = noisy(['#6b4d2e', '#735432', '#664a2c'], 3096)
    frame(im, 0, 0, 16, 16, '#4f3a22'); rnd = random.Random(3097)
    for _ in range(14):
        x, y = rnd.randrange(2, 14), rnd.randrange(2, 14)
        dot(im, x, y, rnd.choice(['#b0a995', '#968f7d', '#c2bba6'])); dot(im, x + 1, y, '#7d786a')
    return im


def grate():
    # A landing of iron grating: flat bars on a square mesh, the gaps open so the stair below shows through.
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    for i in range(0, 16, 4):
        for j in range(16):
            dot(im, i, j, '#3a3a3c'); dot(im, j, i, '#3a3a3c'); dot(im, i + 1, j, '#2a2a2c')
    return im


def shelf():
    # A shelf of pale stone, its top worn flat and a grey grit along its edge.
    im = noisy(['#7d776c', '#746e63', '#868075', '#6c665b'], 3103, [4, 3, 2, 2])
    for x in range(16):
        dot(im, x, 0, '#9a9488'); dot(im, x, 1, '#8a8478')
    return im


def peg():
    # An iron survey pin driven into the rock, a level line scored through its head.
    im = noisy(['#5a5a5c', '#4e4e50', '#666668'], 3104)
    for x in range(16):
        dot(im, x, 7, '#e8e4d8'); dot(im, x, 8, '#b8b4a8')
    return im


def wood():
    # The ladder's spruce, rubbed pale on the rungs.
    im = noisy(['#7a5a36', '#6b4d2e', '#836240'], 3105)
    for x in range(16):
        dot(im, x, 0, '#9a7a52')
    return im


def textures():
    out = {f'block/sphinx_{k}.png': v for k, v in materials().items()}
    t = {'fur': fur(), 'claw': claw(), 'pad': pad(), 'iron': iron(), 'brass': brass(), 'pot': pot(), 'umbrella': umbrella(), 'flap': flap(), 'latch_0': latch(False), 'latch_1': latch(True),
         'grille': grille(), 'socket': socket(), 'tag': tag(), 'rail': rail(), 'chip': chip(), 'tray': tray(), 'grate': grate(), 'ashlar': ashlar(3098),
         'shelf': shelf(), 'peg': peg(), 'wood': wood(), 'rock': rock(3106), 'skin': skin(), 'nail': nail(), 'plaster': plaster(3066), 'reach': reach(), 'skin_dark': skin_dark()}
    for look in range(9):
        for i, q in enumerate(eye_tiles(look)):
            t[f'eye_{look}_{i}'] = q
    out.update({f'block/sphinx/{k}.png': v for k, v in t.items()})
    return out


def model(elements, textures, particle):
    return {'ambientocclusion': True, 'render_type': 'minecraft:cutout', 'textures': {**{k: P + v for k, v in textures.items()}, 'particle': P + particle}, 'elements': elements}


def fitting_models():
    m = {}
    m['stand_0'] = model([element([4, 0, 4], [12, 9, 12], {'all': 'p', 'up': 'i'}), element([6, 9, 5], [8, 15, 7], {'all': 'u'}), element([8, 9, 8], [10, 14, 10], {'all': 'u'}),
                          element([9.5, 9, 5.5], [10.5, 16, 6.5], {'all': 'i'})], {'p': 'pot', 'u': 'umbrella', 'i': 'iron'}, 'pot')
    m['flap_0'] = model([element([0, 0, 0], [16, 16, 16], {'all': 'w', 'south': 'w'}), element([3, 5, -0.5], [13, 11, 0], {'north': 'f'}, uv={'north': [0, 0, 16, 16]})],
                        {'w': 'plaster', 'f': 'flap'}, 'plaster')
    # Open, the plaster round a real hole, and the flap swung up above it.
    m['flap_1'] = model([element([0, 0, 0], [16, 4, 16], {'all': 'w'}), element([0, 12, 0], [16, 16, 16], {'all': 'w'}), element([0, 4, 0], [3, 12, 16], {'all': 'w'}),
                         element([13, 4, 0], [16, 12, 16], {'all': 'w'}), element([3, 11, -1.5], [13, 12, 0], {'all': 'f'}, uv={f: [0, 0, 16, 2] for f in ('north', 'south', 'east', 'west', 'up', 'down')})],
                        {'w': 'plaster', 'f': 'flap'}, 'plaster')
    for s in range(2):
        m[f'latch_{s}'] = model([element([0, 0, 15.5], [16, 16, 16], {'north': 'l'}, uv={'north': [0, 0, 16, 16]})], {'l': f'latch_{s}'}, 'iron')
    for s, depth in ((0, 15.6), (1, 10), (2, 15.6)):
        els = [element([5, 5, 15.5], [11, 11, 16], {'all': 'i'})]
        if s != 2:
            els.append(element([6.5, 6.5, depth], [9.5, 9.5, 15.5], {'all': 'b'}))
        m[f'pin_{s}'] = model(els, {'i': 'iron', 'b': 'brass'}, 'iron')
    m['grille_0'] = model([element([1, 1, 15], [15, 15, 16], {'all': 'i', 'north': 'g'}, uv={'north': [0, 0, 16, 16]})], {'i': 'iron', 'g': 'grille'}, 'grille')
    # Her paw hooked over the frame: the heel of it on top, four heavy toes over the edge, each with a claw curling down into the wood.
    els = [element([0, 9, 9], [16, 16, 16], {'all': 'f', 'down': 'd'})]
    for x in (0.5, 4.5, 8.5, 12.5):
        els.append(element([x, 5, 8], [x + 3.2, 11, 13.5], {'all': 'f', 'down': 'd'}))
        # One claw to a toe: a thicker root out of the toe and a narrower tip below it, mapped as one nail (pale root, dark tip).
        sides = ('north', 'south', 'east', 'west')
        els.append(element([x + .9, 3, 6.6], [x + 2.3, 7, 8.2], {'all': 'c'}, uv={**{f: [0, 0, 16, 10] for f in sides}, 'up': [0, 0, 16, 3], 'down': [4, 10, 12, 16]}))
        els.append(element([x + 1.1, 0, 6.4], [x + 2.1, 3.2, 7.4], {'all': 'c'}, uv={**{f: [4, 10, 12, 16] for f in sides}, 'up': [4, 10, 12, 16], 'down': [4, 12, 12, 16]}))
    m['fingers_0'] = model(els, {'f': 'fur', 'c': 'claw', 'd': 'pad'}, 'fur')
    # Her foreleg down the wall: a thick furred limb against the plaster, a darker ridge down its front.
    m['reach_0'] = model([element([1, 0, 9], [15, 16, 16], {'north': 'f', 'south': 'f', 'east': 'f', 'west': 'f', 'up': 'f', 'down': 'f'})], {'f': 'fur'}, 'fur')
    # 0.4.334: the survey socket, empty, with one pin set in it, and with two or more (a second pin beside the first).
    def survey_pin(x, y):
        return [element([x, y, 9.5], [x + 1, y + 1, 15.5], {'all': 'i'}),
                element([x - .5, y - .5, 9], [x + 1.5, y + 1.5, 9.5], {'all': 'i'}),
                element([x - .25, y - 3, 10], [x + 1.25, y - .5, 10.5], {'all': 't'}, uv={f: [3, 3, 13, 13] for f in ('north', 'south', 'east', 'west', 'up', 'down')})]
    plate = element([4, 3, 15.5], [12, 13, 16], {'north': 's', 'south': 's', 'east': 's', 'west': 's', 'up': 's', 'down': 's'}, uv={'north': [0, 0, 16, 16]})
    m['survey_0'] = model([plate], {'s': 'socket', 'i': 'iron', 't': 'tag'}, 'socket')
    m['survey_1'] = model([plate] + survey_pin(7.5, 7.5), {'s': 'socket', 'i': 'iron', 't': 'tag'}, 'socket')
    m['survey_2'] = model([plate] + survey_pin(6, 7.5) + survey_pin(9, 8.5), {'s': 'socket', 'i': 'iron', 't': 'tag'}, 'socket')
    # 0.4.335, the stair: a stair mouth's iron gate, shut (balusters across the mouth), released (swung back along its post), bowed
    # (bent out toward the gallery under her hand).
    def gate(z0, sag):
        els = [element([0, 13 - sag, z0], [16, 15 - sag, z0 + 2], {'all': 'r'}), element([0, 1, z0], [16, 3, z0 + 2], {'all': 'r'})]
        for x in (1, 5, 9, 13):
            els.append(element([x, 3, z0 + .5], [x + 1.5, 13 - sag, z0 + 1.5], {'all': 'r'}))
        return els
    m['rail_0'] = model(gate(7, 0), {'r': 'rail'}, 'rail')
    m['rail_1'] = model([element([0, 13, 0], [2, 15, 16], {'all': 'r'}), element([0, 1, 0], [2, 3, 16], {'all': 'r'})]
                        + [element([.25, 3, z], [1.75, 13, z + 1.5], {'all': 'r'}) for z in (1, 5, 9, 13)],
                        {'r': 'rail'}, 'rail')
    m['rail_2'] = model(gate(11, 4), {'r': 'rail'}, 'rail')
    m['tray_0'] = model([element([6, 0, 6], [10, 9, 10], {'all': 'a'}), element([2, 9, 3], [14, 11, 13], {'all': 't'}, uv={'up': [0, 0, 16, 16]})],
                        {'a': 'ashlar', 't': 'tray'}, 'tray')
    m['chip_0'] = model([element([6, 0, 6], [9.5, 1.5, 9], {'all': 'c'}), element([8, 0, 7.5], [10, 1, 10], {'all': 'c'})], {'c': 'chip'}, 'chip')
    m['grate_0'] = model([element([0, 13, 0], [16, 16, 16], {'all': 'g'}, uv={'up': [0, 0, 16, 16], 'down': [0, 0, 16, 16]})], {'g': 'grate'}, 'grate')
    # The landing's edge: the same grating with a low stone lip along its south side, for a chip to stop against.
    m['grate_1'] = model([element([0, 13, 0], [16, 16, 16], {'all': 'g'}, uv={'up': [0, 0, 16, 16], 'down': [0, 0, 16, 16]}), element([0, 16, 14], [16, 18, 16], {'all': 'a'})], {'g': 'grate', 'a': 'ashlar'}, 'grate')
    # Her foreleg laid up a stair: thick, furred, the line of the bone under it.
    m['limb_0'] = model([element([2, 0, 0], [14, 13, 16], {'all': 'f'})], {'f': 'fur'}, 'fur')
    # 0.4.336, the shelf: a stone shelf, settled (0) or lifted past its pin as she breathes (1); the iron survey pin in the far wall, its
    # top level with a settled shelf; the short ladder stowed upright at the near edge (0), lowered (1) or dropped toward a false shelf
    # (2); the ladder lying across the gap.
    m['shelf_0'] = model([element([0, 0, 0], [16, 16, 16], {'all': 's'})], {'s': 'shelf'}, 'shelf')
    m['shelf_1'] = model([element([0, 4, 0], [16, 20, 16], {'all': 's'})], {'s': 'shelf'}, 'shelf')
    m['peg_0'] = model([element([6, 0, 4], [10, 2, 16], {'all': 'p'}), element([5, -.5, 3], [11, 2.5, 4], {'all': 'p'})], {'p': 'peg'}, 'peg')
    def ladder_up(tilt):
        els = []
        for x in (2, 12):
            els.append(element([x, 0, 7 + tilt], [x + 2, 16, 9 + tilt], {'all': 'w'}))
        for y in (3, 8, 13):
            els.append(element([4, y, 7.5 + tilt], [12, y + 1.5, 8.5 + tilt], {'all': 'w'}))
        return els
    m['ladder_0'] = model(ladder_up(4), {'w': 'wood'}, 'wood')
    hook = [element([2, 13, 0], [4, 16, 16], {'all': 'w'}), element([12, 13, 0], [14, 16, 16], {'all': 'w'})] + [element([4, 13.5, z], [12, 15, z + 1.5], {'all': 'w'}) for z in (2, 7, 12)]
    # Lowered, all that is left at the edge is the hinge: the rails' ends lying on the rock, pinned to an iron staple.
    hinge = [element([2, 0, 10], [4, 2, 16], {'all': 'w'}), element([12, 0, 10], [14, 2, 16], {'all': 'w'}), element([1, 0, 14], [15, 1, 15], {'all': 'p'})]
    m['ladder_1'] = model(hinge, {'w': 'wood', 'p': 'peg'}, 'wood')
    m['ladder_2'] = model(hinge, {'w': 'wood', 'p': 'peg'}, 'wood')
    m['span_0'] = model(hook, {'w': 'wood'}, 'wood')
    for look in range(9):
        for i in range(8):
            # 0.4.336: her face turned sideways to see under the overhang: the eye upright, facing south, the picture's top upward.
            m[f'eye_side_{look}_{i}'] = model([element([0, 0, 15], [16, 16, 16], {'south': 'e', 'north': 'k', 'up': 'k', 'down': 'k', 'east': 'k', 'west': 'k'}, uv={'south': [0, 0, 16, 16]})], {'e': f'eye_{look}_{i}', 'k': 'skin_dark'}, f'eye_{look}_{i}')
    for look in range(9):
        for i in range(8):
            # 0.4.335: face up, looking up through the gap between two flights; flipped so the hall side (the gallery) is the
            # picture's top, as it is face down.
            m[f'eye_up_{look}_{i}'] = model([element([0, 15, 0], [16, 16, 16], {'up': 'e', 'down': 'k'}, uv={'up': [0, 16, 16, 0]})], {'e': f'eye_{look}_{i}', 'k': 'skin_dark'}, f'eye_{look}_{i}')
    for look in range(9):
        for i in range(8):
            # Face down: the eye is on the underside, read from the hall below.
            m[f'eye_{look}_{i}'] = model([element([0, 0, 0], [16, 1, 16], {'down': 'e', 'up': 'k'}, uv={'down': [0, 0, 16, 16]})], {'e': f'eye_{look}_{i}', 'k': 'skin_dark'}, f'eye_{look}_{i}')
    return m


KINDS = ['stand', 'flap', 'latch', 'pin', 'grille', 'fingers', 'reach', 'survey', 'rail', 'tray', 'chip', 'grate', 'limb', 'shelf', 'peg', 'ladder', 'span']
STAGES = {'stand': 1, 'flap': 2, 'latch': 2, 'pin': 3, 'grille': 1, 'fingers': 1, 'reach': 1, 'survey': 3, 'rail': 3, 'tray': 1, 'chip': 1, 'grate': 2, 'limb': 1,
          'shelf': 2, 'peg': 1, 'ladder': 3, 'span': 1}
FACING_Y = {'north': 0, 'east': 90, 'south': 180, 'west': 270}
MAX_STAGE = 3
NAMES = {'sphinx_fitting': 'Vestibule Fittings', 'sphinx_tile': 'Quarry Tiles', 'sphinx_marked': 'Marked Tile', 'sphinx_plaster': 'Limewash Plaster',
         'sphinx_ceiling': 'Spruce Ceiling', 'sphinx_seam': 'Cracked Plaster', 'sphinx_dark': 'Dark Plaster', 'sphinx_eye': 'Eye in the Dark',
         'sphinx_ashlar': 'Dressed Stone', 'sphinx_flags': 'Worn Flagstones', 'sphinx_ashlar_seam': 'Cracked Masonry', 'sphinx_rock': 'Dark Rock', 'sphinx_rock_seam': 'Closed Rock'}


def files():
    out = {}
    for name in materials():
        out[f'models/block/sphinx_{name}.json'] = {'parent': 'minecraft:block/cube_all', 'textures': {'all': NS + f'block/sphinx_{name}'}}
        out[f'blockstates/sphinx_{name}.json'] = {'variants': {'': {'model': NS + f'block/sphinx_{name}'}}}
    for key, obj in fitting_models().items():
        out[f'models/block/sphinx/{key}.json'] = obj
    variants = {}
    for kind in KINDS:
        for facing, y in FACING_Y.items():
            for stage in range(MAX_STAGE):
                drawn = stage if stage < STAGES[kind] else 0
                v = {'model': NS + f'block/sphinx/{kind}_{drawn}'}
                if y:
                    v['y'] = y
                variants[f'facing={facing},kind={kind},stage={stage}'] = v
    out['blockstates/sphinx_fitting.json'] = {'variants': variants}
    faces = {0: '', 1: 'up_', 2: 'side_'}
    out['blockstates/sphinx_eye.json'] = {'variants': {f'face={face},look={look},tile={i}': {'model': NS + f'block/sphinx/eye_{prefix}{look}_{i}'}
                                                       for look in range(9) for i in range(8) for face, prefix in faces.items()}}
    return out


def main():
    check = argparse.ArgumentParser(); check.add_argument('--check', action='store_true'); check = check.parse_args().check
    images, data = textures(), files()
    models = fitting_models()
    for kind in KINDS:
        for stage in range(STAGES[kind]):
            assert f'{kind}_{stage}' in models, (kind, stage)
    for look in range(9):
        for i in range(8):
            assert f'eye_{look}_{i}' in models, (look, i)
    for key, obj in models.items():
        for tex in obj['textures'].values():
            assert f'block/sphinx/{tex.split("sphinx/")[1]}.png' in images, (key, tex)
    for rel, im in images.items():
        assert im.size == (16, 16), rel
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
    for key, name in {f'block.the_oldest_house.{k}': v for k, v in NAMES.items()}.items():
        if check:
            assert lang.get(key) == name, key
        else:
            lang[key] = name
    if not check:
        lang_path.write_text(json.dumps(lang, indent=2, ensure_ascii=False) + '\n')
    print(('Verified' if check else 'Drew') + f' the Sphinx\'s vestibule: {len(images)} textures, {len(data)} models and states.')


if __name__ == '__main__':
    main()
