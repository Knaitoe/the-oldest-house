"""State Route 9 (0.4.333): the highway at night, in vanilla's manner.

16x16 textures from small palettes with per-pixel noise. The road: wet asphalt with a few specks of sheen, and the rows carrying the
lines (a white edge near or far, the dashed lane line, the yellow edges by the median). The roadside: the galvanized mailbox with its
flag up or down, the route shield in its old painted steel and its new reflective panel, the mile marker likewise, the sodium lamp lit
and dark and its pole, the chain-link fence and its gate, the bent guardrail post with and without its reflector, wet corn. The traffic:
six faded paints, the semi's cab, its trailer, logs and their cut ends, the wagon's body and its wood panel, rain-streaked glass, the
people in the wagon, and the glows the renderer lays on the tar (headlight pools, warm and white, and a semi's shine). Items: the brass
key, the county-line postcard, the cracked amber reflector. Models and block states for the fittings, materials, anchor and gate.
Advancement text is generated from the Witness account by generate_advancements.py.
--check verifies everything.
"""
from pathlib import Path
import argparse
import json
import random
from PIL import Image

from draw_tank_fittings import shade, fill, dot, frame, line, element
from draw_sphinx_fittings import noisy

ROOT = Path(__file__).resolve().parents[1]
A = ROOT / 'src/main/resources/assets/the_oldest_house'
NS = 'the_oldest_house:'
P = NS + 'block/state_route/'


def blank():
    return Image.new('RGBA', (16, 16), (0, 0, 0, 0))


def flat(c):
    im = blank(); fill(im, 0, 0, 16, 16, c); return im


# ------------------------------------------------------------------------------------------------ the road
def asphalt(seed=3330):
    im = noisy(['#26282b', '#2b2d30', '#222427', '#2e3033'], seed, [4, 3, 3, 1])
    rnd = random.Random(seed + 1)
    for _ in range(5):
        dot(im, rnd.randrange(16), rnd.randrange(16), '#4c5258')
    for _ in range(2):
        dot(im, rnd.randrange(16), rnd.randrange(16), '#6a727a')
    return im


def edge(colour, rows, seed):
    im = asphalt(seed); rnd = random.Random(seed + 7)
    for y in rows:
        for x in range(16):
            dot(im, x, y, shade(colour, rnd.randint(-10, 6))[:3])
    return im


# ------------------------------------------------------------------------------------------------ the roadside
def galvanized(seed):
    return noisy(['#8e9396', '#868b8e', '#979c9f', '#7e8386'], seed, [4, 3, 2, 1])


def mailbox_side():
    im = galvanized(3340)
    for y in (3, 12):
        for x in range(16):
            dot(im, x, y, '#6a6f72')
    return im


def mailbox_front(open_):
    im = galvanized(3341)
    if open_:
        fill(im, 2, 3, 14, 15, '#141618')
        fill(im, 3, 9, 13, 11, '#c8a85a')
    else:
        frame(im, 2, 3, 14, 15, '#6a6f72'); fill(im, 7, 9, 9, 11, '#3a3d40')
    return im


def flag(up):
    im = blank()
    if up:
        fill(im, 7, 2, 9, 16, '#5a5e60'); fill(im, 2, 2, 9, 7, '#b22c22'); dot(im, 2, 2, '#d24a3a')
    else:
        fill(im, 0, 12, 16, 14, '#5a5e60'); fill(im, 10, 9, 16, 14, '#8a2a20')
    return im


def post():
    im = noisy(['#4e3c2a', '#463624', '#56442e'], 3342)
    for y in range(0, 16, 5):
        for x in range(16):
            dot(im, x, y, '#3a2c1e')
    return im


def shield(digital):
    im = blank()
    if digital:
        fill(im, 1, 1, 15, 15, '#e8ecee'); frame(im, 1, 1, 15, 15, '#1a1c1e'); frame(im, 2, 2, 14, 14, '#e8ecee')
        for x, y in ((6, 4), (7, 4), (8, 4), (9, 4), (9, 5), (9, 6), (8, 7), (7, 7), (6, 7), (6, 6), (6, 5), (9, 8), (9, 9), (9, 10), (8, 11), (7, 11), (6, 11)):
            dot(im, x, y, '#141618')
        fill(im, 3, 13, 13, 14, '#c8d0d4')
    else:
        rnd = random.Random(3343)
        for y in range(1, 15):
            for x in range(1, 15):
                cut = (y < 4 and (x < 1 + (4 - y) or x > 14 - (4 - y)))
                if not cut:
                    im.putpixel((x, y), shade('#d8d4c8', rnd.randint(-14, 4)))
        for x, y in ((6, 5), (7, 5), (8, 5), (9, 5), (9, 6), (9, 7), (8, 8), (7, 8), (6, 8), (6, 7), (6, 6), (9, 9), (9, 10), (8, 11), (7, 11), (6, 11)):
            dot(im, x, y, '#2a2826')
        for x, y in ((3, 13), (12, 3), (11, 12)):
            dot(im, x, y, '#7a5034')
    return im


def mile(digital):
    im = noisy(['#d8d8d2', '#cecec8'], 3344) if not digital else noisy(['#eceff0', '#e2e6e8'], 3345)
    fill(im, 0, 1, 16, 4, '#1e1e1e' if not digital else '#0e1012')
    for x, y in ((6, 6), (7, 6), (8, 6), (8, 7), (8, 8), (7, 9), (6, 9), (6, 10), (6, 11), (7, 11), (8, 11)):
        dot(im, x, y, '#1e1e1e')
    if digital:
        fill(im, 0, 13, 16, 15, '#f4f8fa')
    return im


def pole():
    return noisy(['#5a5d60', '#525558', '#62656a'], 3346)


def lamp(lit):
    im = noisy(['#3e4144', '#36393c'], 3347)
    if lit:
        fill(im, 3, 4, 13, 12, '#f2a640'); fill(im, 5, 6, 11, 10, '#ffd27a')
    else:
        fill(im, 3, 4, 13, 12, '#5a4a32')
    return im


def chain(top):
    im = blank()
    for y in range(16):
        for x in range(16):
            if (x + y) % 4 == 0 or (x - y) % 4 == 0:
                im.putpixel((x, y), shade('#8a8f92', -8 if (x * 7 + y) % 3 else 6))
    fill(im, 0, 0, 2, 16, '#6e7276'); fill(im, 14, 0, 16, 16, '#6e7276')
    if top:
        fill(im, 0, 0, 16, 2, '#7a7e82')
    return im


def gate(half):
    im = chain(half == 'upper')
    fill(im, 0, 0, 16, 1, '#6e7276') if half == 'upper' else fill(im, 0, 15, 16, 16, '#6e7276')
    if half == 'lower':
        fill(im, 11, 0, 15, 4, '#9a8a40'); frame(im, 11, 0, 15, 4, '#5a5020'); dot(im, 13, 2, '#2a2410')
    return im


def guard(reflector):
    im = galvanized(3348)
    fill(im, 0, 0, 16, 2, '#6a6f72')
    if reflector:
        fill(im, 5, 4, 11, 8, '#d88a22'); dot(im, 6, 5, '#f4c060'); line(im, 7, 4, 10, 7, '#8a5410')
    else:
        fill(im, 5, 4, 11, 8, '#5a5e60'); dot(im, 7, 6, '#3a3c3e')
    return im


def corn(top):
    im = blank(); rnd = random.Random(3349 + top)
    for x in (2, 7, 12):
        for y in range(16):
            dot(im, x + (1 if y % 7 == 3 else 0), y, shade('#4e6a2a', rnd.randint(-10, 6))[:3])
    for _ in range(14):
        x, y = rnd.randrange(16), rnd.randrange(16)
        line(im, x, y, min(15, x + rnd.randint(1, 4)), max(0, y - rnd.randint(1, 3)), '#5e7a32')
    if top:
        for x in (2, 7, 12):
            for y in range(0, 4):
                dot(im, x + rnd.randint(-1, 1), y, '#9a8a4a')
    else:
        dot(im, 8, 9, '#b8a050'); dot(im, 8, 10, '#a89040'); dot(im, 3, 6, '#b8a050')
    return im


# ------------------------------------------------------------------------------------------------ the traffic
PAINTS = ['#5e3a36', '#3a4a5a', '#5a5a52', '#2e3e34', '#6e6250', '#3a3438']


def paint(i):
    base = PAINTS[i]
    im = noisy([base, shade(base, -6)[:3], shade(base, 6)[:3]], 3350 + i, [4, 2, 2])
    for x in range(16):
        dot(im, x, 0, shade(base, 18)[:3])
    return im


def cab():
    return paint(1)


def trailer():
    im = noisy(['#3a3a3c', '#323234', '#424244'], 3360)
    for x in range(0, 16, 4):
        for y in range(16):
            dot(im, x, y, '#262628')
    return im


def logs():
    im = noisy(['#5a4630', '#4e3c28', '#664e36'], 3361)
    for y in (4, 9, 14):
        for x in range(16):
            dot(im, x, y, '#2e2418')
    return im


def log_end():
    im = flat('#2e2418'); rnd = random.Random(3362)
    for cx, cy in ((3, 3), (8, 3), (13, 3), (5, 8), (10, 8), (3, 13), (8, 13), (13, 13)):
        for dy in range(-2, 3):
            for dx in range(-2, 3):
                if dx * dx + dy * dy <= 5:
                    dot(im, cx + dx, cy + dy, shade('#b8945e', rnd.randint(-14, 6))[:3])
        dot(im, cx, cy, '#7a5a34')
    return im


def wagon():
    return paint(4)


def panel_wood():
    im = noisy(['#7a5232', '#6e4a2c', '#86603a'], 3363)
    for x in range(16):
        dot(im, x, 0, '#c8b896'); dot(im, x, 15, '#c8b896')
    for y in range(1, 15):
        if y % 5 == 2:
            for x in range(16):
                dot(im, x, y, '#5e3e24')
    return im


def glass():
    im = Image.new('RGBA', (16, 16), (28, 34, 40, 110)); rnd = random.Random(3364)
    for _ in range(5):
        x = rnd.randrange(16); y0 = rnd.randrange(8)
        for y in range(y0, min(16, y0 + rnd.randint(4, 9))):
            im.putpixel((x, y), (150, 170, 186, 120))
    return im


def silhouette():
    return noisy(['#0e0e10', '#121214', '#0a0a0c'], 3365)


def glow(core, edge):
    """A soft pool, brightest in the middle of its near edge, fading out to nothing at the far edge and the sides."""
    im = blank()
    for y in range(16):
        for x in range(16):
            along = 1 - y / 15
            across = 1 - abs(x - 7.5) / 8
            a = max(0.0, min(1.0, (0.25 + 0.75 * (1 - along)) * across ** 1.5))
            c = core if a > 0.5 else edge
            im.putpixel((x, y), shade(c, 0, int(a * 180)))
    return im


def shine():
    im = blank()
    for y in range(16):
        for x in range(16):
            a = max(0.0, 1 - abs(y - 7.5) / 8) ** 2
            im.putpixel((x, y), shade('#f4f8ff', 0, int(a * 200)))
    return im


# ------------------------------------------------------------------------------------------------ items
def key():
    im = blank()
    for x, y in ((3, 3), (4, 2), (5, 2), (6, 3), (6, 4), (5, 5), (4, 5), (3, 4)):
        dot(im, x, y, '#c8a040')
    dot(im, 4, 3, '#000000', 0); line(im, 6, 5, 12, 11, '#b8902e'); dot(im, 10, 11, '#b8902e'); dot(im, 11, 12, '#b8902e'); dot(im, 12, 9, '#b8902e')
    line(im, 2, 9, 7, 14, '#e8e2d0'); line(im, 3, 9, 8, 14, '#d8d2c0')
    return im


def postcard():
    im = blank(); fill(im, 1, 4, 15, 13, '#e2dccc'); frame(im, 1, 4, 15, 13, '#9a9282')
    fill(im, 2, 5, 14, 9, '#2a3238'); fill(im, 6, 6, 10, 8, '#c8c8c0'); line(im, 2, 9, 14, 9, '#5a5a52')
    line(im, 3, 11, 9, 11, '#6a6458'); fill(im, 11, 10, 14, 12, '#a85a3a')
    return im


def reflector():
    im = blank()
    for y in range(16):
        for x in range(16):
            d = ((x - 7.5) ** 2 + (y - 7.5) ** 2) ** .5
            if d <= 6.5:
                im.putpixel((x, y), shade('#d88a22' if d < 5.2 else '#8a5410', -((x + y) % 3) * 6))
    for x, y in ((6, 4), (7, 5), (8, 6), (8, 7), (9, 9), (10, 10)):
        dot(im, x, y, '#4a2a08')
    dot(im, 5, 5, '#f8d080')
    return im


# ------------------------------------------------------------------------------------------------ 0.4.336, what the road leaves
BLOOD = ['#5e0a0a', '#6e0e0e', '#4a0707', '#7c1212']


def blood_pool():
    # A puddle spreading under the body on wet tar: ragged edge, thick and dark at the middle, one wet glint.
    im = blank(); rnd = random.Random(3361)
    for y in range(16):
        for x in range(16):
            d = ((x - 7.5) / 7.2) ** 2 + ((y - 7.5) / 5.8) ** 2 + rnd.uniform(-.25, .25)
            if d < 1:
                im.putpixel((x, y), shade(rnd.choice(BLOOD), 0)[:3] + (int(255 * min(1, (1.15 - d) * 2)),))
    dot(im, 6, 6, '#b04040'); dot(im, 7, 6, '#902828')
    return im


def blood_smear():
    # Dragged along the lane: streaks running the length of it, broken, thinning to the sides.
    im = blank(); rnd = random.Random(3362)
    for y in range(3, 13):
        edge = abs(y - 7.5) / 5.0
        for x in range(16):
            if rnd.random() > .25 + edge * .6:
                im.putpixel((x, y), shade(rnd.choice(BLOOD), 0)[:3] + (int(255 * (1 - edge * .7)),))
    return im


def shoe():
    # A canvas shoe come off on the tar: worn dark canvas, a white rubber sole, the laces still tied.
    im = noisy(['#2e3440', '#343a48', '#283038'], 3363)
    for x in range(16):
        dot(im, x, 13, '#d8d4c8'); dot(im, x, 14, '#c4c0b4'); dot(im, x, 15, '#9a968c')
    for x in (5, 7, 9):
        dot(im, x, 4, '#d8d4c8'); dot(im, x + 1, 6, '#d8d4c8')
    return im


def wounds():
    # Laid over a reader's skin, the House's body included, in the skin's own layout: blood from the hairline and the mouth, the road's
    # rash down the back and one side where they were dragged, the clothes torn and soaked across the chest and at a knee. Base and
    # outer layers alike, so it shows over whatever the body wears.
    im = Image.new('RGBA', (64, 64), (0, 0, 0, 0)); rnd = random.Random(3364)

    def splotch(x0, y0, x1, y1, n, r=1.6):
        for _ in range(n):
            cx, cy = rnd.uniform(x0, x1 - 1), rnd.uniform(y0, y1 - 1)
            for y in range(int(cy - r - 1), int(cy + r + 2)):
                for x in range(int(cx - r - 1), int(cx + r + 2)):
                    if x0 <= x < x1 and y0 <= y < y1 and (x - cx) ** 2 + (y - cy) ** 2 <= r * r + rnd.uniform(-1, 1):
                        im.putpixel((x, y), shade(rnd.choice(BLOOD), 0)[:3] + (rnd.choice([220, 240, 255]),))

    def rash(x0, y0, x1, y1, n):
        for _ in range(n):
            x, y = rnd.randrange(x0, x1), rnd.randrange(y0, y1)
            for k in range(rnd.randint(2, 5)):
                if y + k < y1:
                    im.putpixel((x, y + k), shade(rnd.choice(['#7a3a30', '#6a2e26', '#5a5550', '#8a4236']), 0)[:3] + (230,))

    def run(x, y0, y1):
        for y in range(y0, y1):
            im.putpixel((x, y), shade(rnd.choice(BLOOD), 0)[:3] + (255,))

    for ox, oy in ((0, 0), (32, 0)):          # head and hat
        splotch(ox + 8, oy + 8, ox + 16, oy + 11, 3, 1.2)   # hairline
        run(ox + 10, oy + 9, oy + 16); run(ox + 11, oy + 12, oy + 16)   # down the face from a cut, and from the mouth
        rash(ox + 0, oy + 8, ox + 8, oy + 16, 10)           # one side of the head on the tar
        splotch(ox + 8, oy + 0, ox + 16, oy + 8, 2, 1.4)    # crown
    for oy in (16, 32):                                       # body and jacket
        splotch(20, oy + 4, 28, oy + 16, 7, 1.8)             # chest soaked
        for x in range(20, 28):                               # torn across
            if rnd.random() > .3:
                im.putpixel((x, oy + 9 + rnd.randint(-1, 1)), (40, 6, 6, 255))
        rash(32, oy + 4, 40, oy + 16, 22)                    # back dragged
        rash(16, oy + 4, 20, oy + 16, 6)
    for x0, oy in ((40, 16), (40, 32)):                      # right arm and sleeve
        rash(x0, oy + 4, x0 + 16, oy + 16, 14); splotch(x0 + 4, oy + 10, x0 + 8, oy + 16, 2, 1.2)
    for x0, y0 in ((32, 48), (48, 48)):                      # left arm and sleeve
        splotch(x0, y0 + 4, x0 + 16, y0 + 16, 4, 1.3)
    for x0, y0 in ((0, 16), (0, 32)):                        # right leg and pants: the knee
        splotch(x0 + 4, y0 + 8, x0 + 8, y0 + 12, 3, 1.5); rash(x0, y0 + 4, x0 + 16, y0 + 16, 6)
    for x0, y0 in ((16, 48), (0, 48)):                       # left leg and pants: torn and soaked
        splotch(x0 + 4, y0 + 4, x0 + 8, y0 + 16, 4, 1.4)
    return im


def textures():
    t = {'asphalt': asphalt(), 'paint_0': paint(0), 'paint_1': paint(1), 'paint_2': paint(2), 'paint_3': paint(3), 'paint_4': paint(4),
         'paint_5': paint(5), 'cab': cab(), 'trailer': trailer(), 'logs': logs(), 'log_end': log_end(), 'wagon': wagon(),
         'panel_wood': panel_wood(), 'glass': glass(), 'silhouette': silhouette(), 'beam': glow('#f4f0e0', '#c8c0a8'),
         'beam_warm': glow('#f0b060', '#b07830'), 'shine': shine(),
         'mailbox_side': mailbox_side(), 'mailbox_front_0': mailbox_front(False), 'mailbox_front_1': mailbox_front(True),
         'flag_0': flag(True), 'flag_1': flag(False), 'post': post(), 'shield_0': shield(False), 'shield_1': shield(True),
         'mile_0': mile(False), 'mile_1': mile(True), 'pole': pole(), 'lamp_0': lamp(True), 'lamp_1': lamp(False),
         'chain_0': chain(False), 'chain_1': chain(True), 'guard_0': guard(True), 'guard_1': guard(False), 'corn_0': corn(0), 'corn_1': corn(1),
         'gate_lower': gate('lower'), 'gate_upper': gate('upper'), 'galvanized': galvanized(3349),
         'blood_pool': blood_pool(), 'blood_smear': blood_smear(), 'shoe': shoe(), 'wounds': wounds()}
    out = {f'block/state_route/{k}.png': v for k, v in t.items()}
    out['block/state_route_asphalt.png'] = asphalt()
    out['block/state_route_edge_white_s.png'] = edge('#d8dcdc', (13, 14), 3331)
    out['block/state_route_edge_white_n.png'] = edge('#d8dcdc', (1, 2), 3332)
    out['block/state_route_dash_n.png'] = edge('#d8dcdc', (0, 1), 3333)
    out['block/state_route_edge_yellow_n.png'] = edge('#d8b03a', (1, 2), 3334)
    out['block/state_route_edge_yellow_s.png'] = edge('#d8b03a', (13, 14), 3335)
    out['item/state_route_key.png'] = key()
    out['item/state_route_postcard.png'] = postcard()
    out['item/state_route_reflector.png'] = reflector()
    return out


def model(elements, tex, particle):
    return {'ambientocclusion': False, 'render_type': 'minecraft:cutout', 'textures': {**{k: P + v for k, v in tex.items()}, 'particle': P + particle}, 'elements': elements}


def fitting_models():
    m = {}
    pole_el = lambda y0, y1: element([7, y0, 7], [9, y1, 9], {'all': 'p'})
    for st in (0, 1):
        m[f'mailbox_{st}'] = model([element([7, 0, 7], [9, 8, 9], {'all': 'w'}),
                                    element([4, 8, 2], [12, 14, 14], {'north': 'f', 'south': 'f', 'east': 's', 'west': 's', 'up': 's', 'down': 's'}),
                                    element([12, 9, 6], [12.5, 16, 12], {'east': 'g', 'west': 'g'})],
                                   {'w': 'post', 'f': f'mailbox_front_{st}', 's': 'mailbox_side', 'g': f'flag_{st}'}, 'mailbox_side')
        m[f'shield_{st}'] = model([pole_el(0, 4), element([1, 2, 7.5], [15, 16, 8.5], {'north': 's', 'south': 's'})], {'p': 'pole', 's': f'shield_{st}'}, 'pole')
        m[f'mile_{st}'] = model([element([6.5, 0, 7], [9.5, 13, 9], {'north': 'm', 'south': 'm', 'east': 'm', 'west': 'm', 'up': 'm'})], {'m': f'mile_{st}'}, f'mile_{st}')
        m[f'lamp_{st}'] = model([element([7, 13, 0], [9, 15, 16], {'all': 'p'}), element([3, 10, 3], [13, 13, 13], {'down': 'l', 'up': 'p', 'north': 'p', 'south': 'p', 'east': 'p', 'west': 'p'})],
                                {'p': 'pole', 'l': f'lamp_{st}'}, 'pole')
        m[f'chain_{st}'] = model([element([0, 0, 7.5], [16, 16, 8.5], {'north': 'c', 'south': 'c'})], {'c': f'chain_{st}'}, f'chain_{st}')
        m[f'post_{st}'] = model([element([6, 0, 6], [10, 14, 10], {'north': 'g', 'south': 'g', 'east': 'b', 'west': 'b', 'up': 'b'}, rotation=None)],
                                {'g': f'guard_{st}', 'b': 'galvanized'}, 'galvanized')
        m[f'corn_{st}'] = {'parent': 'minecraft:block/cross', 'render_type': 'minecraft:cutout', 'textures': {'cross': P + f'corn_{st}', 'particle': P + f'corn_{st}'}}
    m['pole_0'] = model([pole_el(0, 16)], {'p': 'pole'}, 'pole')
    return m


KINDS = ['mailbox', 'shield', 'mile', 'lamp', 'pole', 'chain', 'post', 'corn']
FACING_Y = {'north': 0, 'east': 90, 'south': 180, 'west': 270}
NAMES = {'state_route_fitting': 'Roadside Fittings', 'state_route_anchor': 'Road', 'state_route_gate': 'Chain-Link Gate',
         'state_route_asphalt': 'Wet Asphalt', 'state_route_edge_white_s': 'Wet Asphalt', 'state_route_edge_white_n': 'Wet Asphalt',
         'state_route_dash_n': 'Wet Asphalt', 'state_route_edge_yellow_n': 'Wet Asphalt', 'state_route_edge_yellow_s': 'Wet Asphalt'}
ITEMS = {'state_route_key': 'Brass Gate Key', 'state_route_postcard': 'County-Line Postcard', 'state_route_reflector': 'Amber Reflector Disc'}
MATERIALS = ['asphalt', 'edge_white_s', 'edge_white_n', 'dash_n', 'edge_yellow_n', 'edge_yellow_s']


def door_files(out):
    for half, vanilla in (('lower', 'bottom'), ('upper', 'top')):
        for hinge in ('left', 'right'):
            for opened in ('', '_open'):
                out[f'models/block/state_route_gate_{half}_{hinge}{opened}.json'] = {'parent': f'minecraft:block/door_{vanilla}_{hinge}{opened}', 'render_type': 'minecraft:cutout',
                                                                                    'textures': {'top': P + 'gate_upper', 'bottom': P + 'gate_lower'}}
    variants = {}
    for facing, y in {'east': 0, 'south': 90, 'west': 180, 'north': 270}.items():
        for half in ('lower', 'upper'):
            for hinge in ('left', 'right'):
                for opened in (False, True):
                    turn = y + (90 if hinge == 'left' else -90) if opened else y
                    variants[f'facing={facing},half={half},hinge={hinge},open={str(opened).lower()}'] = {'model': NS + f'block/state_route_gate_{half}_{hinge}{"_open" if opened else ""}', 'y': turn % 360}
    out['blockstates/state_route_gate.json'] = {'variants': variants}


def files():
    out = {}
    for name in MATERIALS:
        out[f'models/block/state_route_{name}.json'] = {'parent': 'minecraft:block/cube_all', 'textures': {'all': NS + f'block/state_route_{name}'}}
        out[f'blockstates/state_route_{name}.json'] = {'variants': {'': {'model': NS + f'block/state_route_{name}'}}}
    out['models/block/state_route_anchor.json'] = {'parent': 'minecraft:block/cube_all', 'textures': {'all': 'minecraft:block/stone'}}
    out['blockstates/state_route_anchor.json'] = {'variants': {'': {'model': NS + 'block/state_route_anchor'}}}
    out.update({f'models/block/state_route/{k}.json': v for k, v in fitting_models().items()})
    variants = {}
    for kind in KINDS:
        for facing, y in FACING_Y.items():
            for stage in range(2):
                drawn = 0 if kind == 'pole' else stage
                v = {'model': NS + f'block/state_route/{kind}_{drawn}'}
                if y and kind != 'corn':
                    v['y'] = y
                variants[f'facing={facing},kind={kind},stage={stage}'] = v
    out['blockstates/state_route_fitting.json'] = {'variants': variants}
    for item in ITEMS:
        out[f'models/item/{item}.json'] = {'parent': 'minecraft:item/generated', 'textures': {'layer0': NS + f'item/{item}'}}
    door_files(out)
    return out


def main():
    check = argparse.ArgumentParser(); check.add_argument('--check', action='store_true'); check = check.parse_args().check
    images, data = textures(), files()
    for rel, im in images.items():
        assert im.size == ((64, 64) if rel.endswith('/wounds.png') else (16, 16)), rel
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
    names = {f'block.the_oldest_house.{k}': v for k, v in NAMES.items()}
    names.update({f'item.the_oldest_house.{k}': v for k, v in ITEMS.items()})
    # 0.4.336: the road's own death message.
    names['death.attack.the_oldest_house.route_vehicle'] = '%1$s was struck on the 9'
    for key, name in names.items():
        if check:
            assert lang.get(key) == name, key
        else:
            lang[key] = name
    if not check:
        lang_path.write_text(json.dumps(lang, indent=2, ensure_ascii=False) + '\n')
    print(('Verified' if check else 'Drew') + f' the state route: {len(images)} textures, {len(data)} models and states.')


if __name__ == '__main__':
    main()
