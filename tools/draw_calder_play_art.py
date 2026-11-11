"""Bike night (0.4.465): the playground in Calder Park.

Draws, in native Minecraft pixels from fixed seeds, the playground's equipment, each piece a kind of one block (calder_play). This
slice, the swings: a steel frame's posts (a foot with its two stays, a shaft, and the two tops a bar ends on or runs over), its top
bar plain and with a swing's two hangers, a swing's chains, and its belt seat on the ends of them. A chain and a seat each have a
state in which nothing is drawn: somebody is on that swing, and the moving one is drawn in their place. Also the heel mark dug into
the sand where the furthest jump off the swings came down (calder_mark, a block that gives way as fallen leaves do), and the night's
one item, a ball of sand.

Made as the park's fittings are (tools/draw_calder_art.py): small pieces are bars that take one pixel of a paint card, the
playground's own; a face big enough to carry a picture takes a part of one the size it is. Sixteen pixels to the block. A piece is
drawn facing north; its block state turns it.

Writes the models, block states, the item's model and the names. --check verifies everything. --sheet PATH draws a page for review.
Drawing or checking, it refuses a model in which two faces of different colours lie in one plane and overlap (tools/flicker.py).
"""
from pathlib import Path
import argparse
import json
import random
from PIL import Image

from draw_borrasca_town_art import shade, dot, element
from draw_calder_art import plate, FACES, FACING_Y, SLIP
from flicker import fights

ROOT = Path(__file__).resolve().parents[1]
A = ROOT / 'src/main/resources/assets/the_oldest_house'
NS = 'the_oldest_house:'
DIR = 'block/calder_play/'
P = NS + DIR

# ---------------------------------------------------------------------------------------------------------------- the paint card
# Sixteen swatches, four pixels square, four to a row. A bar takes the pixel in the middle of its swatch. The frame's paint, the
# same paint where it is going, bare steel where it has gone, galvanised chain light and dark, a rubber belt and where it shines,
# bolts; then the paints of the equipment to come, and shadow.
FRAME, FRAME_HI, FRAME_WORN, BARE, CHAIN, CHAIN_DK, RUBBER, RUBBER_HI, BOLT, YELLOW, YELLOW_WORN, BLUE, BLUE_WORN, GREEN, PLANK, SHADOW = range(16)
CARD = ('#9a3d34', '#b04a3f', '#7d3a34', '#878b8f', '#9ea2a6', '#5c6064', '#1b1b1d', '#34353a',
        '#55585c', '#c9a227', '#9c8431', '#2f5d8c', '#3c5670', '#3e6b45', '#7a5630', '#141816')
SAND = ('#dbd3a0', '#d0c692', '#e6deb0', '#c4b884')


def card():
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0)); rnd = random.Random(4650); px = im.load()
    for i, colour in enumerate(CARD):
        for y in range(4):
            for x in range(4):
                px[4 * (i % 4) + x, 4 * (i // 4) + y] = shade(colour, rnd.randint(-2, 2))
    return im


def sw(i):
    x, y = 4 * (i % 4) + 1, 4 * (i // 4) + 1
    return [x, y, x + 1, y + 1]


def chain_links():
    """A chain a pixel wide, in two columns: its links seen flat, and (two pixels along) the same links seen on edge."""
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    flat = (CARD[CHAIN], CARD[CHAIN], CARD[CHAIN_DK], '#84888c')
    for y in range(16):
        dot(im, 0, y, shade(flat[y % 4], 0))
        dot(im, 2, y, shade(flat[(y + 2) % 4], -6))
    return im


def mark():
    """A heel mark in sand, seen from above, the jumper going north: two heels dug in, the furrow between them, and the sand they
    pushed up ahead."""
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0)); rnd = random.Random(4651)
    for x in range(2, 14):
        dot(im, x, 7, shade('#a39361', rnd.randint(-5, 5)))
        dot(im, x, 8, shade('#8c7d50', rnd.randint(-5, 5)))
        if rnd.random() < .85:
            dot(im, x, 6, shade('#f4edc6', rnd.randint(-4, 4)))
    for x0 in (4, 10):
        for x in (x0, x0 + 1):
            dot(im, x, 9, shade('#7c6e45', rnd.randint(-4, 4))); dot(im, x, 10, shade('#94855a', rnd.randint(-4, 4)))
            dot(im, x, 5, shade('#f8f1cf', rnd.randint(-3, 3)))
    for x, y in ((1, 8), (14, 7), (3, 5), (12, 5), (7, 5), (2, 9), (13, 9)):
        dot(im, x, y, shade('#b9ab78', rnd.randint(-4, 4)))
    return im


def sand_ball():
    """A ball of sand packed in a hand: rough, damp underneath, and already losing some."""
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0)); rnd = random.Random(4652)
    cx, cy, r = 7.5, 7.5, 4.6
    for y in range(16):
        for x in range(16):
            d = ((x - cx) ** 2 + (y - cy) ** 2) ** .5
            if d > r + (.3 if rnd.random() < .35 else -.2):
                continue
            light = int((cx - x) * 5 + (cy - y) * 7)                      # lit from the upper left
            dot(im, x, y, shade(SAND[rnd.randrange(len(SAND))], max(-46, min(18, light)) + rnd.randint(-5, 5)))
    for x, y in ((6, 5), (9, 8), (5, 9), (8, 10), (10, 6)):               # damp grains
        dot(im, x, y, shade('#9c8d5e', rnd.randint(-6, 6)))
    for x, y in ((12, 13), (10, 14), (13, 11), (3, 13)):                  # what it is losing
        dot(im, x, y, shade(SAND[1], rnd.randint(-8, 4)))
    return im


def textures():
    return {DIR + 'play.png': card(), DIR + 'chain.png': chain_links(), DIR + 'mark.png': mark(), 'item/sand_ball.png': sand_ball()}


# ---------------------------------------------------------------------------------------------------------------- pieces
def bar(parts, lo, hi, swatch, rot=None):
    """One bar, every face from one swatch of the card."""
    parts.append(element([round(v, 3) for v in lo], [round(v, 3) for v in hi], {'all': 'k'}, {k: sw(swatch) for k in FACES}, rot))


# The frame's pipe is three pixels through, on the block's middle; a bar lies with its underside a pixel up, so that what hangs
# from it hangs from the bottom of its block.
PIPE0, PIPE1, BAR0, BAR1 = 6.5, 9.5, 1, 4
# A swing's two chains, a pixel through, at the ends of its seat.
CHAINS = (2, 14)


def post(stage):
    """A leg of the frame. 0: its foot, a collar where it goes into the ground, the paint gone to steel where hands take hold of
    it, and a stay each way the frame is pushed (north and south, the bar running east and west). 1: its shaft. 2: its top, the bar
    going off it to the east under a sleeve. 3: its top, the bar running on over it both ways."""
    p = []
    if stage == 0:
        bar(p, [PIPE0, 1.2, PIPE0], [PIPE1, 9, PIPE1], FRAME)                # standing on its collar
        bar(p, [PIPE0, 9, PIPE0], [PIPE1, 13, PIPE1], BARE)
        bar(p, [PIPE0, 13, PIPE0], [PIPE1, 16, PIPE1], FRAME)
        bar(p, [5.5, 0, 5.5], [10.5, 1.2, 10.5], FRAME_WORN)
        for angle in (45, -45):
            bar(p, [7.25, -2.9, 7.25], [8.75, 6.2, 8.75], FRAME_WORN, {'angle': angle, 'axis': 'x', 'origin': [8, 7, 8]})
    elif stage == 1:
        bar(p, [PIPE0, 0, PIPE0], [PIPE1, 16, PIPE1], FRAME)
    elif stage == 2:
        bar(p, [PIPE0, 0, PIPE0], [PIPE1, BAR1, PIPE1], FRAME)
        bar(p, [PIPE1, BAR0, PIPE0], [16, BAR1, PIPE1], FRAME)
        bar(p, [6, 3.2, 6], [10, 4.6, 10], FRAME_HI)
    else:
        bar(p, [PIPE0, 0, PIPE0], [PIPE1, BAR0, PIPE1], FRAME)
        bar(p, [0, BAR0, PIPE0], [16, BAR1, PIPE1], FRAME)
        bar(p, [5.8, .6, 6], [10.2, 4.4, 10], FRAME_HI)
    return p


def top_bar(hangers):
    """The frame's top bar, running east and west. With hangers: over each of a swing's chains a clamp round the pipe and, under
    it, the eye the chain hangs from, down to the bottom of the block."""
    p = []
    bar(p, [0, BAR0, PIPE0], [16, BAR1, PIPE1], FRAME)
    if hangers:
        for c in CHAINS:
            bar(p, [c - .9, .7, 6.2], [c + .9, 4.3, 9.8], BOLT)
            bar(p, [c - .5, 0, 7.5], [c + .5, .7, 8.5], CHAIN_DK)
    return p


def links(parts, c, y0, y1):
    """A length of chain: its north and south faces show the links flat, its east and west faces show them on edge."""
    plate(parts, [c - .5, y0, 7.5], [c + .5, y1, 8.5], 'c', {'north': (0, 0), 'south': (0, 0), 'east': (2, 0), 'west': (2, 0)})


def chain():
    p = []
    for c in CHAINS:
        links(p, c, 0, 16)
    return p


def swing():
    """A belt seat, looking south (what it faces is behind whoever sits on it): a rubber strap that sags, its two ends turned up
    into the steel that holds them, and from each a chain going up out of the block."""
    p = []
    bar(p, [3, 6, 5.5], [13, 7, 10.5], RUBBER)
    for x0, x1 in ((2, 3), (13, 14)):
        bar(p, [x0, 6.5, 5.5], [x1, 8, 10.5], RUBBER_HI)
    for c in CHAINS:
        bar(p, [c - .6, 7.6, 6.4], [c + .6, 9, 9.6], BOLT)
        links(p, c, 9, 16)
    return p


def mark_model():
    p = []
    plate(p, [0, .3, 0], [16, .3, 16], 'm', {'up': (0, 0)})
    return p


PIECES = {
    'post_0': lambda: post(0), 'post_1': lambda: post(1), 'post_2': lambda: post(2), 'post_3': lambda: post(3),
    'bar_0': lambda: top_bar(False), 'bar_1': lambda: top_bar(True), 'chain_0': chain, 'swing_0': swing, 'mark_0': mark_model,
}
NAMES_OF = {'k': 'play', 'c': 'chain', 'm': 'mark'}
KINDS = ['post', 'bar', 'chain', 'swing']
# The model each stage of a kind is drawn from; `gone` draws nothing.
STAGES = {'post': ('post_0', 'post_1', 'post_2', 'post_3'), 'bar': ('bar_0', 'bar_1', 'bar_0', 'bar_0'),
          'chain': ('chain_0', 'gone', 'chain_0', 'chain_0'), 'swing': ('swing_0', 'gone', 'swing_0', 'swing_0')}


def model(parts, particle='play'):
    keys = {f['texture'][1:] for e in parts for f in e['faces'].values()}
    return {'ambientocclusion': False, 'render_type': 'minecraft:cutout',
            'textures': {**{k: P + NAMES_OF[k] for k in sorted(keys)}, 'particle': P + particle}, 'elements': parts}


def models():
    return {key: model(build(), 'mark' if key == 'mark_0' else 'play') for key, build in PIECES.items()}


def files():
    out = {f'models/{DIR}{key}.json': obj for key, obj in models().items()}
    out[f'models/{DIR}gone.json'] = {'textures': {'particle': P + 'play'}}
    variants = {}
    for kind in KINDS:
        for facing, y in FACING_Y.items():
            for stage in range(4):
                v = {'model': P + STAGES[kind][stage]}
                if y:
                    v['y'] = y
                variants[f'facing={facing},kind={kind},stage={stage}'] = v
    out['blockstates/calder_play.json'] = {'variants': variants}
    out['blockstates/calder_mark.json'] = {'variants': {f'facing={facing},scatter={k}': {'model': P + 'mark_0', **({'y': y} if y else {})}
                                                        for facing, y in FACING_Y.items() for k in range(4)}}
    out['models/item/sand_ball.json'] = {'parent': 'minecraft:item/generated', 'textures': {'layer0': NS + 'item/sand_ball'}}
    return out


NAMES = {'block.the_oldest_house.calder_play': 'Playground Equipment', 'block.the_oldest_house.calder_mark': 'Heel Mark',
         'item.the_oldest_house.sand_ball': 'Ball of Sand', 'entity.the_oldest_house.sand_ball': 'Ball of Sand'}


# ---------------------------------------------------------------------------------------------------------------- review
def review(path):
    from draw_bike_night_art import seen
    images = textures(); scale = 14; cells = []
    for key, build in PIECES.items():
        for view in ('side', 'front', 'top'):
            cells.append(seen(build(), {k: 'calder_play/' + v for k, v in NAMES_OF.items()}, images, view, scale, (-4, 20, -4, 18), 'block/'))
    w, h = cells[0].size
    cols = 9
    rows = (len(cells) + cols - 1) // cols
    page = Image.new('RGBA', (cols * (w + 4) + 4, rows * (h + 4) + 4 + 72), (30, 32, 36, 255))
    for i, one in enumerate(cells):
        page.alpha_composite(one, (4 + (i % cols) * (w + 4), 4 + (i // cols) * (h + 4)))
    x = 4
    for rel, im in sorted(images.items()):
        page.alpha_composite(im.resize((64, 64), Image.NEAREST), (x, page.height - 68)); x += 68
    page.save(path)


def main():
    args = argparse.ArgumentParser()
    args.add_argument('--check', action='store_true')
    args.add_argument('--sheet')
    args = args.parse_args()
    if args.sheet:
        review(args.sheet); return
    check = args.check
    images, data, made = textures(), files(), models()
    for kind in KINDS:
        for name in STAGES[kind]:
            assert name == 'gone' or name in made, (kind, name)
    for key, obj in made.items():
        for tex in obj['textures'].values():
            assert tex.split(':')[1] + '.png' in images, (key, tex)
        for e in obj['elements']:
            for v in e['from'] + e['to']:
                assert -16 <= v <= 32, (key, e['from'], e['to'])
        clash = fights(obj['elements'])
        assert not clash, (key, [(obj['elements'][a]['from'], obj['elements'][a]['to'], fa, obj['elements'][b]['from'], obj['elements'][b]['to'], fb, n)
                                 for a, fa, b, fb, n in clash[:4]])
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
    for key, name in NAMES.items():
        if check:
            assert lang.get(key) == name, key
        else:
            lang[key] = name
    if not check:
        lang_path.write_text(json.dumps(lang, indent=2, ensure_ascii=False) + '\n')
    print(('Verified' if check else 'Drew') + f" the playground: {len(images)} textures, {len(data)} models and states.")


if __name__ == '__main__':
    main()
