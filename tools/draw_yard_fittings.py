"""The House's yard (0.4.309): clapboard, wet gravel, the garage door, the porch bulb and the station wagon, in vanilla's manner.

16x16 textures from small palettes with per-pixel noise: white-painted clapboard with grey shadow lines; gravel darkened and glossed by
rain; a garage door of dark vertical boards with a handle; a bare bulb on its cord; and the wagon: faded green paint over wood-grain
panelling, its grille and lit headlights, windows, roof rack, tailgate and tail-lamps, wheels. 0.4.322: soft mud, clear or with a reader's
footprints pressed into it. 0.4.345: a stockyard swing gate's leaf in three heights, galvanised tube rails on oak stiles, the top rail
painted so the gates read from the loading platform; a sheet of corrugated barn roofing, rusted at its laps. 0.4.347: field corn three
high (the stalk, the ear, the tassel) and the same corn pushed flat; a windsock, slack, lifting and full. 0.4.349: the wagon's hazard lamps
lit at nose and tail; roadside grass higher than a head in thirds (the stems, the blades, the seed heads), the same grass trampled flat and
springing back in two steps, a boy's cap lying wet on the flattened stems; and a pickup (nose, body, cab, bed) in faded paint. --check
verifies everything.
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
P = NS + 'block/yard/'


def clapboard():
    im = noisy(['#e4e2da', '#dcdad2', '#e8e6e0'], 3090, [4, 3, 2])
    for y in (3, 7, 11, 15):
        for x in range(16):
            dot(im, x, y, '#a8a69e')
    return im


def wet_gravel():
    im = noisy(['#6e6a66', '#5c5854', '#7c7874', '#4e4a48', '#8a8682'], 3091, [3, 3, 2, 2, 1])
    rnd = random.Random(3092)
    for _ in range(7):
        dot(im, rnd.randrange(16), rnd.randrange(16), '#a4a8ac')
    return im


def garage_door():
    im = noisy(['#3a2a1c', '#34261a', '#40301f'], 3093)
    for x in (3, 7, 11, 15):
        for y in range(16):
            dot(im, x, y, '#22180f')
    return im


def paint():
    return noisy(['#5e7458', '#566c50', '#647a5e'], 3094)


def side():
    im = paint()
    grain = noisy(['#8a5a30', '#7e522a', '#946236'], 3095)
    for y in range(7, 14):
        for x in range(16):
            im.putpixel((x, y), grain.getpixel((x, y)))
    for x in range(16):
        dot(im, x, 6, '#c8c8c0'); dot(im, x, 14, '#2c2c2c'); dot(im, x, 15, '#2c2c2c')
    return im


def grille(lit):
    im = noisy(['#8a8c8e', '#7e8082', '#96989a'], 3096)
    for y in range(5, 12, 2):
        fill(im, 5, y, 11, y + 1, '#2a2a2a')
    for x0 in (0, 12):
        fill(im, x0, 5, x0 + 4, 11, '#fff6c8' if lit else '#9a9a8a')
        if lit:
            dot(im, x0 + 1, 6, '#ffffff'); dot(im, x0 + 2, 6, '#ffffff')
    fill(im, 0, 13, 16, 15, '#b0b2b4')
    return im


def window():
    im = paint()
    fill(im, 1, 2, 15, 14, '#1c2630')
    line(im, 3, 12, 8, 4, '#3c4a56'); line(im, 4, 12, 9, 4, '#2c3a46')
    return im


def roof():
    im = paint()
    for x in (2, 13):
        for y in range(16):
            dot(im, x, y, '#2c2c2c')
    return im


def tailgate():
    im = side()
    fill(im, 1, 2, 15, 6, '#1c2630')
    fill(im, 0, 8, 3, 11, '#8a1a14'); fill(im, 13, 8, 16, 11, '#8a1a14')
    return im


def wheel():
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    for y in range(16):
        for x in range(16):
            d = ((x - 7.5) ** 2 + (y - 7.5) ** 2) ** .5
            if d <= 7.6:
                dot(im, x, y, '#1c1c1c' if d > 3.5 else '#8a8c8e')
    return im


def bulb():
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    fill(im, 7, 0, 9, 9, '#2a2a2a'); fill(im, 6, 9, 10, 11, '#8a8c8e')
    fill(im, 5, 11, 11, 16, '#fff6c8'); dot(im, 6, 12, '#ffffff')
    return im


def mud(seed):
    return noisy(['#3e3734', '#48403b', '#352f2c', '#524943'], seed, [4, 3, 2, 1])


def mud_print():
    # 0.4.322: two footprints pressed into the mud, toes to the north (the fitting's facing turns them), a little water in each.
    im = mud(3151)
    for x0, y0 in ((3, 1), (9, 7)):
        fill(im, x0, y0, x0 + 4, y0 + 4, '#272220'); fill(im, x0 + 1, y0 + 5, x0 + 3, y0 + 8, '#272220')
        dot(im, x0 + 1, y0 + 1, '#5d6266'); dot(im, x0 + 2, y0 + 6, '#4d5256')
        for x in range(x0, x0 + 4):
            dot(im, x, y0 - 1, '#5c534c')
    return im


def leaf(stage):
    # 0.4.345: a swing gate's leaf, a third of its height to each stage: tube rails between oak stiles, a brace in the middle, the top rail painted.
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    tube = ['#9aa3a6', '#aab3b5', '#868f92']
    fill(im, 0, 0, 2, 16, '#6b5235'); fill(im, 14, 0, 16, 16, '#6b5235')
    for x in range(16):
        dot(im, x if x < 2 or x > 13 else 0, 0, '#6b5235')
    for y in ((3, 10) if stage < 2 else (10,)):
        for x in range(2, 14):
            dot(im, x, y, tube[(x * 3 + y) % 3]); dot(im, x, y + 1, '#6f777a')
    if stage == 1:
        for i in range(12):
            dot(im, 2 + i, 15 - i, tube[i % 3]); dot(im, 2 + i, 14 - i, '#6f777a') if i < 11 else None
    if stage == 2:
        paint = ['#9c3a2a', '#a8442f', '#8a3224']
        for x in range(0, 16):
            for y in (0, 1, 2):
                dot(im, x, y, paint[(x + y) % 3])
            dot(im, x, 3, '#6e281d')
        for x, y in ((4, 1), (9, 0), (12, 2)):
            dot(im, x, y, '#aab3b5')
    for y in range(4 if stage == 2 else 0, 16):
        dot(im, 1, y, '#5a442c'); dot(im, 14, y, '#7a5f3f')
    return im


def roof_sheet():
    # 0.4.345: corrugated iron, the ribs running north to south, rust bleeding down from the nail lines at the laps.
    im = Image.new('RGBA', (16, 16))
    rib = ['#98a1a3', '#8a9395', '#737c7e', '#80898b']
    rng = random.Random(3460)
    for x in range(16):
        for y in range(16):
            dot(im, x, y, rib[x % 4] if rng.random() > .12 else '#6a7274')
    for y in (1, 14):
        for x in range(1, 16, 4):
            dot(im, x, y, '#4a3426')
            for d in range(1, rng.randint(2, 5)):
                if y + d < 16: dot(im, x, y + d, '#7a4a2a' if d < 3 else '#86634a')
    for x, y in ((6, 7), (11, 9), (3, 11)):
        dot(im, x, y, '#7a4a2a')
    return im


STALK = ['#6f8a3a', '#7a9640', '#62792f']
LEAF = ['#7f9c45', '#8aa64c', '#6d8a3a', '#9aa956']
DRY = ['#a89a5a', '#b4a664', '#9a8c50']


def corn(stage, seed):
    # 0.4.347: late-summer field corn, a third of its height to each stage: the stalk with its long leaves arching out; the stalk with an
    # ear in its husk and silk; the top of the stalk with its tassel; and (3) seen from above, stalks pushed flat, all leaning one way.
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0)); rnd = random.Random(seed)
    if stage == 3:
        for x in (1, 4, 6, 9, 12, 14):
            col = STALK[x % 3]
            for y in range(rnd.randrange(0, 3), 16 - rnd.randrange(0, 3)):
                dot(im, x, y, col); dot(im, x + 1, y, '#566a2a') if x < 15 and y % 5 else None
        for _ in range(26):
            x, y = rnd.randrange(16), rnd.randrange(16)
            dot(im, x, y, rnd.choice(LEAF + DRY))
        for x, y in ((3, 4), (10, 11), (13, 2)):
            dot(im, x, y, '#c9b35c')
        return im
    if stage < 2:
        def blade(side, y0, length, k):
            # A long leaf: two pixels thick out of the node, rising, then arching over and drooping to a dry tip.
            rise = [0, -1, -2, -2, -3, -3, -3, -2, -1, 0]
            for i in range(1, length + 1):
                x = (7 - i) if side < 0 else (8 + i)
                y = y0 + rise[min(i, len(rise) - 1)]
                if not (0 <= x < 16):
                    break
                tip = i >= length - 1
                if 0 <= y < 16: dot(im, x, y, DRY[(i + k) % 3] if tip else LEAF[(i + k) % 4])
                if not tip and 0 <= y + 1 < 16: dot(im, x, y + 1, LEAF[(i + k + 2) % 4] if i % 3 else '#5f7a30')
        nodes = ((-1, 14, 7), (1, 11, 7), (-1, 7, 6), (1, 4, 6)) if stage == 0 else ((1, 13, 7), (-1, 10, 7), (-1, 4, 6))
        for k, (side, y0, length) in enumerate(nodes):
            blade(side, y0, length, k)
        for y in range(16):
            dot(im, 7, y, STALK[(y + stage) % 3]); dot(im, 8, y, '#566a2a')
            if y % 6 == 2: dot(im, 7, y, '#8aa04a'); dot(im, 8, y, '#8aa04a')
        if stage == 1:
            # The ear, in its husk, leaning out from the stalk, silk at its tip.
            for i, (x, y) in enumerate(((9, 9), (10, 8), (10, 7), (11, 6), (11, 5), (12, 4))):
                dot(im, x, y, '#8fa64e' if i % 2 else '#7f9a44'); dot(im, x + 1, y + 1, '#a3b45e') if i < 5 else None
                dot(im, x - 1, y, '#9aae58') if i > 0 else None
            for x, y in ((12, 3), (13, 2), (12, 2), (13, 3), (14, 2)):
                dot(im, x, y, '#c9a85a')
        return im
    # The tassel: the stalk's last joint and its spikes, pale and dry, fanning out.
    for y in range(8, 16):
        dot(im, 7, y, STALK[y % 3]); dot(im, 8, y, '#566a2a')
    for i in range(6):
        dot(im, 4 - i // 2 if i < 4 else 2, 13 - i, LEAF[i % 4]); dot(im, 11 + i // 2 if i < 4 else 13, 12 - i, LEAF[(i + 2) % 4])
    for y in range(1, 9):
        dot(im, 7, y, DRY[y % 3]); dot(im, 8, y, DRY[(y + 1) % 3])
    for side in (-1, 1):
        for b in range(3):
            for i in range(4):
                x = (7 if side < 0 else 8) + side * (i + 1)
                y = 3 + b * 2 + i // 2
                if 0 <= x < 16: dot(im, x, y, DRY[(i + b) % 3])
    for x, y in ((7, 0), (8, 0), (5, 1), (10, 1)):
        dot(im, x, y, '#c4b06a')
    return im


def windsock():
    # 0.4.347: a windsock's cloth, bands alternating along its length, darker at the seams; a galvanised hoop.
    im = Image.new('RGBA', (16, 16)); rnd = random.Random(3471)
    for y in range(16):
        band = (y // 4) % 2
        for x in range(16):
            base = ['#d8743a', '#e07e40', '#cc6c34'] if band == 0 else ['#e6e2d6', '#dcd8cc', '#ece8de']
            dot(im, x, y, rnd.choice(base))
        if y % 4 == 3:
            for x in range(16): dot(im, x, y, '#9a5226' if band == 0 else '#b8b4a8')
    return im


AMBER = '#ffb030'


def grille_hazard():
    # 0.4.349: the wagon's nose with its hazards on: the headlights lit and both amber corner lamps lit with them.
    im = grille(True)
    for x0 in (0, 14):
        fill(im, x0, 2, x0 + 2, 5, AMBER); dot(im, x0, 2, '#fff0c0')
    return im


def tailgate_hazard():
    # 0.4.349: the tail with its hazards on: the lamps lit amber with a hot centre.
    im = tailgate()
    for x0 in (0, 13):
        fill(im, x0, 8, x0 + 3, 11, AMBER); dot(im, x0 + 1, 9, '#fff0c0')
    return im


GRASS = ['#7d8f3e', '#889a44', '#6f8236', '#93a04c', '#a3a453']
SEED = ['#c2b46a', '#cdbf78', '#b4a55c', '#d8cc8a']


def grass(stage, seed):
    # 0.4.349: roadside grass taller than a head, a third of its height to each stage. Stems crowded together at the foot, darker where
    # the light does not get down; blades crossing in the middle; seed heads nodding at the top. Drawn without the biome's tint.
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0)); rnd = random.Random(seed)
    cols = (0, 2, 3, 5, 7, 8, 10, 12, 13, 15)
    for i, x in enumerate(cols):
        dark = stage == 0
        top = 0 if stage < 2 else rnd.randrange(3, 9)
        lean = rnd.choice((-1, 0, 0, 1))
        for y in range(top, 16):
            xx = x + (lean if y < 6 and stage == 2 else 0)
            if 0 <= xx < 16:
                c = GRASS[(i + y) % len(GRASS)]
                if dark and y > 9: c = '#55662a' if (x + y) % 3 else '#4b5a26'
                dot(im, xx, y, c)
        if stage == 2:
            # A seed head: a drooping spike of pale grains above its stem.
            hx = x + lean
            for k in range(rnd.randrange(3, 6)):
                yy = top - k
                if 0 <= yy < 16 and 0 <= hx < 16: dot(im, hx, yy, SEED[(k + i) % 4])
                if 0 <= yy < 16 and 0 <= hx + 1 < 16 and k % 2: dot(im, hx + 1, yy, SEED[(k + i + 1) % 4])
    if stage == 1:
        # Blades crossing the stems, long and thin, out of every other stem.
        for i, x in enumerate(cols[::2]):
            side = 1 if i % 2 else -1
            y0 = rnd.randrange(4, 13)
            for k in range(1, rnd.randrange(4, 7)):
                xx, yy = x + side * k, y0 - k // 2
                if 0 <= xx < 16 and 0 <= yy < 16: dot(im, xx, yy, GRASS[(k + i) % len(GRASS)])
    return im


def grass_flat(seed):
    # 0.4.349: the same grass trampled flat, seen from above, every stem pressed over the way someone went through.
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0)); rnd = random.Random(seed)
    for x in range(0, 16, 2):
        for y in range(rnd.randrange(0, 2), 16 - rnd.randrange(0, 3)):
            dot(im, x, y, GRASS[(x + y) % len(GRASS)])
            if y % 4 == 1 and x < 15: dot(im, x + 1, y, '#5f7030')
    for _ in range(14):
        dot(im, rnd.randrange(16), rnd.randrange(16), rnd.choice(SEED))
    for x, y in ((3, 2), (11, 9), (6, 13)):
        dot(im, x, y, '#4c5a26')
    return im


def cap():
    # 0.4.349: a child's ball cap, wet through, the cloth dark with water, a pale button on the crown, the brim's stitching.
    im = noisy(['#2c3a52', '#26344a', '#32405a'], 3490, [3, 2, 2])
    for x in range(16): dot(im, x, 12, '#c8c4b4') if x % 2 else None
    fill(im, 7, 2, 9, 4, '#d8d4c4')
    for x, y in ((3, 6), (10, 8), (5, 10)):
        dot(im, x, y, '#1c2638')
    return im


def pickup_paint():
    return noisy(['#4e6a80', '#48647a', '#567288'], 3491)


def pickup_side():
    im = pickup_paint()
    for x in range(16):
        dot(im, x, 5, '#c8ccd0'); dot(im, x, 14, '#2c2c2c'); dot(im, x, 15, '#2c2c2c')
    for x, y in ((4, 9), (11, 11), (13, 8)):
        dot(im, x, y, '#7a5a40')
    return im


def pickup_grille():
    im = noisy(['#9a9c9e', '#8e9092', '#a6a8aa'], 3492)
    for y in range(6, 11): fill(im, 4, y, 12, y + 1, '#2a2a2a' if y % 2 else '#b8babc')
    for x0 in (0, 12):
        fill(im, x0, 5, x0 + 4, 10, '#fff6c8'); dot(im, x0 + 1, 6, '#ffffff')
    fill(im, 0, 12, 16, 15, '#c0c2c4')
    return im


def pickup_window():
    im = pickup_paint()
    fill(im, 1, 2, 15, 13, '#1c2630')
    line(im, 2, 11, 7, 3, '#3c4a56')
    return im


def pickup_bed():
    im = noisy(['#3a3c3e', '#343638', '#404244'], 3493)
    for x in (2, 6, 10, 14):
        for y in range(16): dot(im, x, y, '#2a2c2e')
    return im


def pickup_gate():
    im = pickup_side()
    fill(im, 0, 7, 3, 10, '#8a1a14'); fill(im, 13, 7, 16, 10, '#8a1a14')
    return im



def cap_item():
    # 0.4.349: the boy's cap as carried: a small ball cap in profile, crown and brim, dark with water, a pale button, a drip off the brim.
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    crown = ['#2c3a52', '#26344a', '#32405a']
    for y in range(4, 11):
        for x in range(3 + max(0, 6 - y), 12 - max(0, 6 - y) // 2):
            dot(im, x, y, crown[(x + y) % 3])
    fill(im, 9, 9, 15, 11, '#1e2a3e'); fill(im, 3, 10, 12, 11, '#1a2436')
    dot(im, 7, 3, '#d8d4c4'); dot(im, 8, 3, '#c8c4b4')
    for x in range(4, 11, 2): dot(im, x, 8, '#3c4a64')
    dot(im, 14, 12, '#7a9ab8'); dot(im, 14, 13, '#5a7a98')
    return im
ITEMS = {'tall_grass_cap': 'Small Cap'}

def textures():
    out = {'block/yard_clapboard.png': clapboard(), 'block/yard_wet_gravel.png': wet_gravel(), 'block/yard_garage_door.png': garage_door()}
    linen = noisy(['#c9c6b6', '#d4d0c2', '#bdbbac'], 3131, [5, 3, 1])
    for x in (3, 10, 14):
        for y in range(16):
            dot(linen, x, y, '#b3b1a2' if (x+y) % 5 else '#c0bdaf')
    concrete = noisy(['#81847f', '#8c8f88', '#737771'], 3132, [5, 3, 1])
    for x in range(16):
        dot(concrete, x, 14, '#535b52'); dot(concrete, x, 15, '#485248')
    stake = noisy(['#8e7553', '#9d825d', '#7a6448'], 3140, [5, 2, 1])
    for x in (2, 7, 12):
        for y in range(16):
            dot(stake, x, y, '#6e5b42')
    t = {'mud_0': mud(3150), 'mud_1': mud_print(), 'mud_side': noisy(['#3a3431', '#433b37', '#2f2a27'], 3152, [4, 3, 2]),
         'paint': paint(), 'side': side(), 'grille_lit': grille(True), 'window': window(), 'roof': roof(), 'tailgate': tailgate(), 'wheel': wheel(), 'bulb': bulb(), 'linen': linen, 'concrete': concrete, 'stake': stake}
    for stage in range(4):
        grate = Image.new('RGBA', (16, 16))
        frame(grate, 0, 0, 16, 16, '#555c58')
        for x in (3, 7, 11): fill(grate, x, 1, x+2, 15, '#646c65')
        for i in range(stage): line(grate, 2, 5+i*4, 13, 3+i*4, '#68513b')
        t[f'grate_{stage}'] = grate
    for stage in range(3): t[f'leaf_{stage}'] = leaf(stage)
    t['roof_sheet'] = roof_sheet()
    for stage in range(4): t[f'corn_{stage}'] = corn(stage, 3470 + stage)
    t['windsock'] = windsock()
    hoop = noisy(['#9aa3a6', '#aab3b5', '#868f92'], 3472, [3, 2, 2])
    t['hoop'] = hoop
    t['grille_hazard'] = grille_hazard(); t['tailgate_hazard'] = tailgate_hazard()
    for stage in range(3): t[f'grass_{stage}'] = grass(stage, 3494 + stage)
    t['grass_flat'] = grass_flat(3497); t['cap'] = cap()
    t.update({'pickup_paint': pickup_paint(), 'pickup_side': pickup_side(), 'pickup_grille': pickup_grille(), 'pickup_window': pickup_window(), 'pickup_bed': pickup_bed(), 'pickup_gate': pickup_gate()})
    out.update({f'block/yard/{k}.png': v for k, v in t.items()})
    out['item/tall_grass_cap.png'] = cap_item()
    return out


def model(elements, textures, particle):
    return {'ambientocclusion': True, 'render_type': 'minecraft:cutout', 'textures': {**{k: P + v for k, v in textures.items()}, 'particle': P + particle}, 'elements': elements}


def fitting_models():
    m = {}
    m['bulb_0'] = model([element([7, 6, 7], [9, 16, 9], {'north': 'b', 'south': 'b', 'east': 'b', 'west': 'b'}, uv={f: [7, 0, 9, 16] for f in ('north', 'south', 'east', 'west')}),
                         element([6, 6, 6], [10, 9, 10], {'all': 'b'}, uv={f: [5, 11, 9, 14] for f in ('north', 'south', 'east', 'west', 'up', 'down')})], {'b': 'bulb'}, 'bulb')
    body = element([0, 3, 0], [16, 14, 16], {'north': 's', 'south': 's', 'east': 's', 'west': 's', 'up': 'p', 'down': 'p'})
    m['body_0'] = model([body], {'s': 'side', 'p': 'paint'}, 'paint')
    m['body_1'] = model([body, element([-0.5, 0, 3], [0.5, 9, 12], {'west': 'w', 'east': 'w'}), element([15.5, 0, 3], [16.5, 9, 12], {'west': 'w', 'east': 'w'})], {'s': 'side', 'p': 'paint', 'w': 'wheel'}, 'paint')
    m['nose_0'] = model([element([0, 3, 0], [16, 12, 16], {'north': 'g', 'south': 'p', 'east': 's', 'west': 's', 'up': 'p', 'down': 'p'})], {'g': 'grille_lit', 's': 'side', 'p': 'paint'}, 'paint')
    m['cabin_0'] = model([element([0, 0, 0], [16, 11, 16], {'north': 'w', 'south': 'w', 'east': 'w', 'west': 'w', 'up': 'r', 'down': 'p'})], {'w': 'window', 'r': 'roof', 'p': 'paint'}, 'roof')
    m['tail_0'] = model([element([0, 3, 0], [16, 14, 16], {'south': 't', 'north': 'p', 'east': 's', 'west': 's', 'up': 'p', 'down': 'p'})], {'t': 'tailgate', 's': 'side', 'p': 'paint'}, 'paint')
    m['sheet_0'] = model([element([0, 0, 7], [16, 16, 8], {'all': 'l'})], {'l': 'linen'}, 'linen')
    m['sheet_1'] = model([element([0, 0, 7], [4, 16, 9], {'all': 'l'}), element([3, 0, 6], [5, 16, 8], {'all': 'l'})], {'l': 'linen'}, 'linen')
    m['pipe_lip_0'] = model([element([0, 0, 0], [16, 16, 16], {'all': 'c'})], {'c': 'concrete'}, 'concrete')
    m['survey_slot_0'] = model([element([0, 0, 7], [5, 16, 9], {'all': 's'}), element([11, 0, 7], [16, 16, 9], {'all': 's'}),
                                element([5, 0, 7], [11, 2, 9], {'all': 's'}), element([5, 14, 7], [11, 16, 9], {'all': 's'})], {'s': 'stake'}, 'stake')
    m['gate_latch_0'] = model([element([3, 5, 14], [13, 11, 16], {'all': 'p'}), element([5, 7, 12], [11, 9, 14], {'all': 'm'})], {'p': 'stake', 'm': 'grille_lit'}, 'stake')
    for stage in range(2): m[f'print_{stage}'] = model([element([0, 0, 0], [16, 14, 16], {'up': f'm{stage}', 'north': 's', 'south': 's', 'east': 's', 'west': 's', 'down': 's'}, uv={f: [0, 2, 16, 16] for f in ('north', 'south', 'east', 'west')})], {f'm{stage}': f'mud_{stage}', 's': 'mud_side'}, 'mud_side')
    for stage in range(4): m[f'flood_grate_{stage}'] = model([element([0, 0, 14], [16, 16, 15], {'north': 'g', 'south': 'g'})], {'g': f'grate_{stage}'}, f'grate_{stage}')
    sheet = {'up': 'r', 'down': 'r', 'north': 'r', 'south': 'r', 'east': 'r', 'west': 'r'}
    m['roof_sheet_0'] = model([element([0, 0, 0], [16, 2, 16], sheet, uv={f: [0, 0, 16, 2] for f in ('north', 'south', 'east', 'west')})], {'r': 'roof_sheet'}, 'roof_sheet')
    lifted = element([0, 0, 0], [16, 2, 16], sheet, uv={f: [0, 0, 16, 2] for f in ('north', 'south', 'east', 'west')})
    lifted['rotation'] = {'angle': 22.5, 'axis': 'x', 'origin': [8, 1, 16]}
    m['roof_sheet_1'] = model([lifted], {'r': 'roof_sheet'}, 'roof_sheet')
    m['roof_sheet_2'] = model([element([0, 0, 0], [16, 1, 16], sheet, uv={f: [0, 0, 16, 1] for f in ('north', 'south', 'east', 'west')})], {'r': 'roof_sheet'}, 'roof_sheet')
    for stage in range(3):
        planes = [{'from': [0.8, 0, 8], 'to': [15.2, 16, 8], 'rotation': {'origin': [8, 8, 8], 'axis': 'y', 'angle': 45, 'rescale': True}, 'shade': False,
                   'faces': {'north': {'uv': [0, 0, 16, 16], 'texture': '#c'}, 'south': {'uv': [0, 0, 16, 16], 'texture': '#c'}}},
                  {'from': [8, 0, 0.8], 'to': [8, 16, 15.2], 'rotation': {'origin': [8, 8, 8], 'axis': 'y', 'angle': 45, 'rescale': True}, 'shade': False,
                   'faces': {'east': {'uv': [0, 0, 16, 16], 'texture': '#c'}, 'west': {'uv': [0, 0, 16, 16], 'texture': '#c'}}}]
        m[f'stalk_{stage}'] = {**model(planes, {'c': f'corn_{stage}'}, f'corn_{stage}'), 'ambientocclusion': False}
    flat = {'from': [0, 0.6, 0], 'to': [16, 0.6, 16], 'shade': False, 'faces': {'up': {'uv': [0, 0, 16, 16], 'texture': '#c'}, 'down': {'uv': [0, 0, 16, 16], 'texture': '#c'}}}
    leaning = {'from': [1, 0.5, 0], 'to': [15, 0.5, 15], 'rotation': {'origin': [8, 0.5, 15], 'axis': 'x', 'angle': -22.5}, 'shade': False,
               'faces': {'up': {'uv': [1, 0, 15, 15], 'texture': '#c'}, 'down': {'uv': [1, 0, 15, 15], 'texture': '#c'}}}
    m['stalk_3'] = {**model([flat, leaning], {'c': 'corn_3'}, 'corn_3'), 'ambientocclusion': False}
    # The windsock: its hoop on a short arm from the pole (to the south, model north being where it points), the cloth slack, lifting, full.
    hoop = element([5.5, 9, 15], [10.5, 14, 16], {'all': 'h'}, uv={f: [0, 0, 5, 5] for f in ('north', 'south', 'east', 'west', 'up', 'down')})
    full = [element([6, 9.5, 8], [10, 13.5, 15], {'all': 'w'}, uv={'east': [0, 0, 7, 4], 'west': [0, 0, 7, 4], 'up': [0, 0, 4, 7], 'down': [0, 0, 4, 7], 'north': [0, 0, 4, 4], 'south': [0, 0, 4, 4]}),
            element([6.75, 10.25, 1], [9.25, 12.75, 8], {'all': 'w'}, uv={'east': [7, 0, 14, 3], 'west': [7, 0, 14, 3], 'up': [4, 7, 7, 14], 'down': [4, 7, 7, 14], 'north': [4, 0, 7, 3], 'south': [4, 0, 7, 3]})]
    lifting = []
    for el in full:
        el = json.loads(json.dumps(el)); el['rotation'] = {'angle': -22.5, 'axis': 'x', 'origin': [8, 11.5, 16]}; lifting.append(el)
    slack = [element([6, 3, 11.5], [10, 9.5, 15.5], {'all': 'w'}, uv={'north': [0, 0, 4, 7], 'south': [0, 0, 4, 7], 'east': [0, 0, 4, 7], 'west': [0, 0, 4, 7], 'up': [0, 0, 4, 4], 'down': [0, 0, 4, 4]}),
             element([6.75, 0, 12.25], [9.25, 3, 14.75], {'all': 'w'}, uv={'north': [4, 7, 7, 10], 'south': [4, 7, 7, 10], 'east': [4, 7, 7, 10], 'west': [4, 7, 7, 10], 'up': [4, 0, 7, 3], 'down': [4, 0, 7, 3]})]
    for stage, els in enumerate((slack, lifting, full)):
        m[f'windsock_{stage}'] = model([hoop] + els, {'h': 'hoop', 'w': 'windsock'}, 'windsock')
    for stage in range(3): m[f'stock_leaf_{stage}'] = model([element([0, 0, 7], [16, 16, 9], {'north': 'l', 'south': 'l', 'east': 'l', 'west': 'l', 'up': 'l', 'down': 'l'}, uv={'east': [14, 0, 16, 16], 'west': [0, 0, 2, 16], 'up': [0, 0, 16, 2], 'down': [0, 14, 16, 16]})], {'l': f'leaf_{stage}'}, f'leaf_{stage}')
    # 0.4.349: the wagon with its hazards on.
    m['nose_1'] = model([element([0, 3, 0], [16, 12, 16], {'north': 'g', 'south': 'p', 'east': 's', 'west': 's', 'up': 'p', 'down': 'p'})], {'g': 'grille_hazard', 's': 'side', 'p': 'paint'}, 'paint')
    m['tail_1'] = model([element([0, 3, 0], [16, 14, 16], {'south': 't', 'north': 'p', 'east': 's', 'west': 's', 'up': 'p', 'down': 'p'})], {'t': 'tailgate_hazard', 's': 'side', 'p': 'paint'}, 'paint')
    # 0.4.349: the tall grass in thirds, crossed planes like the corn's; trampled flat (0), rising (1), nearly up again (2).
    def crossed(tex, height=16, lean=0):
        planes = []
        for a, b in (([0.8, 0, 8], [15.2, height, 8]), ([8, 0, 0.8], [8, height, 15.2])):
            faces = ('north', 'south') if a[2] == 8 else ('east', 'west')
            el = {'from': a, 'to': b, 'rotation': {'origin': [8, 0, 8], 'axis': 'y', 'angle': 45, 'rescale': True}, 'shade': False,
                  'faces': {f: {'uv': [0, 16 - height, 16, 16], 'texture': '#c'} for f in faces}}
            planes.append(el)
        if lean:
            leant = []
            for el in planes:
                el = json.loads(json.dumps(el)); el['rotation'] = {'origin': [8, 0, 8], 'axis': 'x', 'angle': lean}; leant.append(el)
            planes = leant
        return planes
    for stage in range(3):
        m[f'grass_{stage}'] = {**model(crossed('c'), {'c': f'grass_{stage}'}, f'grass_{stage}'), 'ambientocclusion': False}
    m['trampled_0'] = {**model([flat, leaning], {'c': 'grass_flat'}, 'grass_flat'), 'ambientocclusion': False}
    flat_f = json.loads(json.dumps(flat)); flat_f['faces'] = {f: {'uv': [0, 0, 16, 16], 'texture': '#f'} for f in ('up', 'down')}
    m['trampled_1'] = {**model([flat_f] + crossed('c', 9, -45), {'c': 'grass_1', 'f': 'grass_flat'}, 'grass_flat'), 'ambientocclusion': False}
    m['trampled_2'] = {**model(crossed('c', 13, -22.5), {'c': 'grass_0'}, 'grass_0'), 'ambientocclusion': False}
    m['cap_0'] = {**model([flat, element([5, 0.6, 4], [11, 3, 10], {'up': 'k', 'north': 'k', 'south': 'k', 'east': 'k', 'west': 'k'}, uv={f: [4, 0, 12, 4] for f in ('north', 'south', 'east', 'west')}),
                          element([6, 0.6, 10], [10, 1.2, 13], {'up': 'k', 'down': 'k', 'north': 'k', 'south': 'k', 'east': 'k', 'west': 'k'}, uv={f: [0, 11, 16, 13] for f in ('north', 'south', 'east', 'west', 'up', 'down')})],
                          {'c': 'grass_flat', 'k': 'cap'}, 'cap'), 'ambientocclusion': False}
    # 0.4.349: the pickup that pulls over: its nose, two lengths of body under the cab (one with the front wheel), the bed (with the back
    # wheel, then the tailgate).
    m['pickup_nose_0'] = model([element([0, 3, 0], [16, 11, 16], {'north': 'g', 'south': 'p', 'east': 's', 'west': 's', 'up': 'p', 'down': 'p'})], {'g': 'pickup_grille', 's': 'pickup_side', 'p': 'pickup_paint'}, 'pickup_paint')
    pbody = element([0, 3, 0], [16, 14, 16], {'north': 's', 'south': 's', 'east': 's', 'west': 's', 'up': 'p', 'down': 'p'})
    m['pickup_body_0'] = model([pbody], {'s': 'pickup_side', 'p': 'pickup_paint'}, 'pickup_paint')
    m['pickup_body_1'] = model([pbody, element([-0.5, 0, 3], [0.5, 9, 12], {'west': 'w', 'east': 'w'}), element([15.5, 0, 3], [16.5, 9, 12], {'west': 'w', 'east': 'w'})], {'s': 'pickup_side', 'p': 'pickup_paint', 'w': 'wheel'}, 'pickup_paint')
    m['pickup_cab_0'] = model([element([0, 0, 0], [16, 10, 16], {'north': 'w', 'south': 'w', 'east': 'w', 'west': 'w', 'up': 'p', 'down': 'p'})], {'w': 'pickup_window', 'p': 'pickup_paint'}, 'pickup_paint')
    walls = [element([0, 3, 0], [16, 5, 16], {'up': 'b', 'down': 'p', 'north': 's', 'south': 's', 'east': 's', 'west': 's'}),
             element([0, 5, 0], [1, 14, 16], {'all': 's'}), element([15, 5, 0], [16, 14, 16], {'all': 's'})]
    m['pickup_bed_0'] = model(walls + [element([-0.5, 0, 3], [0.5, 9, 12], {'west': 'w', 'east': 'w'}), element([15.5, 0, 3], [16.5, 9, 12], {'west': 'w', 'east': 'w'})], {'b': 'pickup_bed', 's': 'pickup_side', 'p': 'pickup_paint', 'w': 'wheel'}, 'pickup_side')
    m['pickup_bed_1'] = model(walls + [element([0, 3, 15], [16, 14, 16], {'all': 's', 'south': 't'})], {'b': 'pickup_bed', 's': 'pickup_side', 'p': 'pickup_paint', 't': 'pickup_gate'}, 'pickup_side')
    return m


KINDS = ['bulb', 'nose', 'body', 'cabin', 'tail', 'sheet', 'pipe_lip', 'survey_slot', 'gate_latch', 'flood_grate', 'print', 'stock_leaf', 'roof_sheet', 'stalk', 'windsock', 'grass', 'trampled', 'cap', 'pickup_nose', 'pickup_body', 'pickup_cab', 'pickup_bed']
STAGES = {'bulb': 1, 'nose': 2, 'body': 2, 'cabin': 1, 'tail': 2, 'sheet': 2, 'pipe_lip': 1, 'survey_slot': 1, 'gate_latch': 1, 'flood_grate': 4, 'print': 2, 'stock_leaf': 3, 'roof_sheet': 3, 'stalk': 4, 'windsock': 3, 'grass': 3, 'trampled': 3, 'cap': 1, 'pickup_nose': 1, 'pickup_body': 2, 'pickup_cab': 1, 'pickup_bed': 2}
FACING_Y = {'north': 0, 'east': 90, 'south': 180, 'west': 270}
NAMES = {'yard_fitting': 'Yard Fittings', 'yard_clapboard': 'Clapboard', 'yard_wet_gravel': 'Wet Gravel', 'yard_garage_door': 'Garage Door'}


def files():
    out = {}
    for name in ('clapboard', 'wet_gravel', 'garage_door'):
        out[f'models/block/yard_{name}.json'] = {'parent': 'minecraft:block/cube_all', 'textures': {'all': NS + f'block/yard_{name}'}}
        out[f'blockstates/yard_{name}.json'] = {'variants': {'': {'model': NS + f'block/yard_{name}'}}}
    out.update({f'models/block/yard/{k}.json': v for k, v in fitting_models().items()})
    variants = {}
    for kind in KINDS:
        for facing, y in FACING_Y.items():
            for stage in range(4):
                drawn = stage if stage < STAGES[kind] else 0
                v = {'model': NS + f'block/yard/{kind}_{drawn}'}
                if y:
                    v['y'] = y
                variants[f'facing={facing},kind={kind},stage={stage}'] = v
    out['blockstates/yard_fitting.json'] = {'variants': variants}
    for item in ITEMS:
        out[f'models/item/{item}.json'] = {'parent': 'minecraft:item/generated', 'textures': {'layer0': NS + f'item/{item}'}}
    return out


def main():
    check = argparse.ArgumentParser(); check.add_argument('--check', action='store_true'); check = check.parse_args().check
    images, data = textures(), files()
    for key, obj in fitting_models().items():
        for tex in obj['textures'].values():
            assert f'block/yard/{tex.split("yard/")[1]}.png' in images, (key, tex)
    for rel, im in images.items():
        assert im.size == (16, 16)
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
    for key, name in {**{f'block.the_oldest_house.{k}': v for k, v in NAMES.items()}, **{f'item.the_oldest_house.{k}': v for k, v in ITEMS.items()}}.items():
        if check:
            assert lang.get(key) == name, key
        else:
            lang[key] = name
    if not check:
        lang_path.write_text(json.dumps(lang, indent=2, ensure_ascii=False) + '\n')
    print(('Verified' if check else 'Drew') + f' the yard: {len(images)} textures, {len(data)} models and states.')


if __name__ == '__main__':
    main()
