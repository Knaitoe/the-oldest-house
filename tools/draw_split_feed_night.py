#!/usr/bin/env python3
"""The split-feed bedroom's night (0.4.201): the blood thrown across the reader's screen when the figure reaches the parents'
room, the parents' blood over their bodies (drawn over whatever House body the reader wears), and the figure's own face as
the hallway shows it: eyes watering, lips quivering, the mouth wet and open. Also the house itself (owner: "cctv needs
retexturing, a reconstruction of its environment"): the upstairs hall's cream paper, the child's blue paper, the parents'
rose paper, painted wainscot, carpet over a popcorn ceiling, and the bathroom's tile. Pixel art at native scale, no
resampling.
"""
import argparse, io, json, random
from pathlib import Path
from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parents[1]
A = ROOT / 'src/main/resources/assets/the_oldest_house/textures'
DARK, BLOOD, FRESH, WET = (58, 4, 6), (104, 10, 12), (150, 18, 20), (196, 52, 48)


def splat(d, rnd, cx, cy, r, alpha):
    """A splash: a core, a ring of droplets thrown out from it and runs dripping down."""
    for i in range(r * 6):
        a = rnd.random() * 6.283
        k = rnd.random() ** 1.6 * r
        x, y = cx + int(k * __import__('math').cos(a)), cy + int(k * __import__('math').sin(a))
        s = max(1, int((1 - k / r) * r / 3))
        c = rnd.choice((DARK, BLOOD, BLOOD, FRESH))
        d.ellipse((x - s, y - s, x + s, y + s), fill=c + (alpha,))
    for i in range(r * 2):
        a = rnd.random() * 6.283
        k = r * (1 + rnd.random() * 1.6)
        x, y = cx + int(k * __import__('math').cos(a)), cy + int(k * __import__('math').sin(a))
        s = rnd.choice((1, 1, 2))
        d.ellipse((x - s, y - s, x + s, y + s), fill=rnd.choice((BLOOD, FRESH)) + (alpha,))
    for i in range(max(1, r // 4)):
        x = cx + rnd.randint(-r // 2, r // 2)
        length = rnd.randint(r // 2, r * 2)
        w = rnd.choice((1, 2, 2, 3))
        d.rectangle((x, cy, x + w - 1, cy + length), fill=BLOOD + (alpha,))
        d.ellipse((x - 1, cy + length - 1, x + w, cy + length + 2), fill=DARK + (alpha,))


def screen():
    rnd = random.Random(20141)
    im = Image.new('RGBA', (256, 256))
    d = ImageDraw.Draw(im)
    # Heaviest across the upper corners and left side, thrown from the right; the middle mostly clear so the room still shows.
    for cx, cy, r in ((24, 20, 26), (226, 34, 30), (14, 118, 18), (244, 150, 16), (60, 230, 14), (200, 214, 12), (120, 8, 12), (92, 60, 8), (170, 84, 7)):
        splat(d, rnd, cx, cy, r, 230)
    for i in range(120):
        x, y = rnd.randrange(256), rnd.randrange(256)
        if 70 < x < 186 and 80 < y < 190 and rnd.random() < .7:
            continue
        d.point((x, y), fill=rnd.choice((BLOOD, FRESH, DARK)) + (220,))
    return im


def body_blood():
    """64 by 64, the player skin layout: blood on the face, the front of the torso and the arms, and soaked into the pillow
    side of the head; transparent elsewhere."""
    rnd = random.Random(20142)
    im = Image.new('RGBA', (64, 64))
    d = ImageDraw.Draw(im)
    def patch(x0, y0, x1, y1, density):
        for x in range(x0, x1):
            for y in range(y0, y1):
                if rnd.random() < density:
                    d.point((x, y), fill=rnd.choice((DARK, BLOOD, BLOOD, FRESH)) + (235,))
    patch(8, 12, 16, 16, .7)      # lower face, mouth and chin
    patch(9, 8, 15, 12, .25)      # spatter over the eyes
    patch(0, 8, 8, 16, .5)        # the side of the head
    patch(20, 20, 28, 32, .62)    # chest
    patch(44, 20, 48, 30, .4)     # right arm front
    patch(36, 52, 40, 62, .4)     # left arm front
    patch(4, 20, 8, 26, .3)       # right leg top
    for x in (21, 24, 26):        # runs down the chest
        d.line((x, 22, x, 31), fill=BLOOD + (235,))
    return im


def marionette_face():
    """The hallway's face, over the reader's own: watering eyes, the lids red, tears down the cheeks, the mouth open and wet,
    lips trembling apart."""
    im = Image.new('RGBA', (64, 64))
    d = ImageDraw.Draw(im)
    # Front of the head is x 8-15, y 8-15. Eyes sit on row 12 in most skins; the overlay works on its own rows.
    for x in (9, 10, 13, 14):
        d.point((x, 11), fill=(170, 60, 60, 150))       # red-rimmed lids
    for x in (9, 14):
        d.point((x, 12), fill=(220, 236, 244, 210))     # wet shine in the eyes
        d.point((x, 13), fill=(150, 190, 210, 170))     # tears
        d.point((x, 14), fill=(150, 190, 210, 120))
    d.rectangle((11, 14, 12, 15), fill=(40, 6, 10, 255))   # the open mouth
    d.point((10, 14), fill=(130, 40, 44, 230)); d.point((13, 14), fill=(130, 40, 44, 230))
    d.point((11, 15), fill=(196, 140, 140, 220))           # wet lower lip
    d.point((12, 13), fill=(150, 60, 62, 200))             # upper lip drawn back
    return im


def noisy(base, seed, amount=(-4, -2, 0, 2, 3), chance=3):
    rnd = random.Random(seed)
    im = Image.new('RGBA', (16, 16), base + (255,))
    for x in range(16):
        for y in range(16):
            if rnd.randrange(chance) == 0:
                im.putpixel((x, y), tuple(max(0, min(255, c + rnd.choice(amount))) for c in base) + (255,))
    return im


def shade(c, n):
    return tuple(max(0, min(255, v + n)) for v in c) + (255,)


def hall_paper():
    """Cream paper with a faint sage sprig in a half-drop, and a seam."""
    base = (214, 204, 178)
    im = noisy(base, 20151, (-3, -2, 0, 2), 4)
    for cx, cy in ((4, 2), (12, 10)):
        for y in range(cy, cy + 5):
            im.putpixel((cx, y), (150, 160, 128, 255))
        for dx, dy in ((-1, 1), (1, 2), (-1, 3)):
            im.putpixel((cx + dx, cy + dy), (165, 174, 142, 255))
        im.putpixel((cx, cy - 1 if cy else 15), (188, 150, 140, 255))
    for y in range(16):
        im.putpixel((15, y), shade(base, -10))
    return im


def child_paper():
    """Pale blue paper with small pale stars and a dot between."""
    base = (163, 190, 204)
    im = noisy(base, 20152, (-3, 0, 2), 5)
    for cx, cy in ((3, 4), (11, 12)):
        im.putpixel((cx, cy), (238, 232, 196, 255))
        for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)):
            im.putpixel((cx + dx, cy + dy), (218, 216, 190, 255))
    for x, y in ((11, 3), (4, 11), (8, 8)):
        im.putpixel((x, y), (205, 214, 214, 255))
    for y in range(16):
        im.putpixel((15, y), shade(base, -9))
    return im


def parents_paper():
    """Dusty rose with a small damask diamond in a half-drop."""
    base = (178, 146, 140)
    im = noisy(base, 20153, (-3, 0, 2), 5)
    light, dark = (196, 166, 156, 255), (160, 128, 124, 255)
    for cx, cy in ((4, 4), (12, 12)):
        for dx, dy in ((0, -3), (1, -2), (2, -1), (3, 0), (2, 1), (1, 2), (0, 3), (-1, 2), (-2, 1), (-3, 0), (-2, -1), (-1, -2)):
            im.putpixel(((cx + dx) % 16, (cy + dy) % 16), light)
        im.putpixel((cx, cy), dark)
    for y in range(16):
        im.putpixel((15, y), shade(base, -10))
    return im


def wainscot():
    """White-painted beadboard: a chair rail along the top, grooves every four pixels and a skirting along the floor."""
    base = (226, 223, 213)
    im = Image.new('RGBA', (16, 16), base + (255,))
    for x in range(16):
        im.putpixel((x, 0), (242, 239, 230, 255)); im.putpixel((x, 1), (198, 194, 182, 255))
        for y in range(2, 14):
            im.putpixel((x, y), (192, 187, 175, 255) if x % 4 == 0 else (240, 237, 228, 255) if x % 4 == 1 else base + (255,))
        im.putpixel((x, 14), (212, 208, 196, 255)); im.putpixel((x, 15), (166, 160, 146, 255))
    for x, y in ((6, 5), (10, 9), (14, 4)):
        im.putpixel((x, y), (214, 210, 198, 255))
    return im


def carpet():
    """Oatmeal low-pile carpet."""
    rnd = random.Random(20154)
    base = (176, 162, 136)
    im = Image.new('RGBA', (16, 16), base + (255,))
    for x in range(16):
        for y in range(16):
            r = rnd.randrange(9)
            if r < 2:
                im.putpixel((x, y), shade(base, -9))
            elif r < 4:
                im.putpixel((x, y), shade(base, 7))
    return im


def ceiling():
    """A popcorn ceiling."""
    rnd = random.Random(20155)
    base = (222, 219, 208)
    im = Image.new('RGBA', (16, 16), base + (255,))
    for x in range(16):
        for y in range(16):
            r = rnd.randrange(7)
            if r == 0:
                im.putpixel((x, y), shade(base, -12))
            elif r == 1:
                im.putpixel((x, y), shade(base, 8))
    return im


def floor_side():
    """The edge of the upstairs floor where it shows at the stairwell: carpet over a painted fascia."""
    im = ceiling()
    top = carpet()
    for x in range(16):
        for y in range(3):
            im.putpixel((x, y), top.getpixel((x, y)))
        im.putpixel((x, 3), (150, 138, 116, 255))
    return im


def bath_tile():
    """Pale mint square tile in a grid of grey grout."""
    base = (196, 214, 204)
    im = noisy(base, 20156, (-2, 0, 2), 5)
    for i in range(16):
        for line in (0, 8):
            im.putpixel((i, line), (156, 164, 160, 255)); im.putpixel((line, i), (156, 164, 160, 255))
    for x, y in ((2, 2), (10, 10), (11, 3), (3, 11)):
        im.putpixel((x, y), (226, 238, 232, 255))
    return im


MATERIALS = {'home_hall_paper': hall_paper, 'home_child_paper': child_paper, 'home_parents_paper': parents_paper,
             'home_wainscot': wainscot, 'home_ceiling': ceiling, 'home_bath_tile': bath_tile}


def files():
    out = {}
    images = [(A / 'misc/blood_splatter.png', screen()), (A / 'entity/uncanny_parent_blood.png', body_blood()), (A / 'entity/uncanny_marionette_face.png', marionette_face())]
    images += [(A / f'block/uncanny/{name}.png', draw()) for name, draw in MATERIALS.items()]
    images += [(A / 'block/uncanny/home_carpet.png', carpet()), (A / 'block/uncanny/home_floor_side.png', floor_side())]
    for path, im in images:
        b = io.BytesIO(); im.save(b, format='PNG'); out[path] = b.getvalue()
    models = ROOT / 'src/main/resources/assets/the_oldest_house'
    def model(name, value):
        out[models / f'models/block/uncanny_{name}.json'] = (json.dumps(value, indent=2) + '\n').encode()
        out[models / f'blockstates/uncanny_{name}.json'] = (json.dumps({'variants': {'': {'model': f'the_oldest_house:block/uncanny_{name}'}}}, indent=2) + '\n').encode()
    for name in MATERIALS:
        model(name, {'parent': 'minecraft:block/cube_all', 'textures': {'all': f'the_oldest_house:block/uncanny/{name}'}})
    # The upstairs floor: carpet on top, the downstairs' popcorn ceiling underneath.
    model('home_floor', {'parent': 'minecraft:block/cube_bottom_top', 'textures': {'top': 'the_oldest_house:block/uncanny/home_carpet', 'bottom': 'the_oldest_house:block/uncanny/home_ceiling', 'side': 'the_oldest_house:block/uncanny/home_floor_side'}})
    return out


def main():
    p = argparse.ArgumentParser(); p.add_argument('--check', action='store_true'); check = p.parse_args().check
    for path, data in files().items():
        if check:
            if path.suffix == '.json':
                assert path.exists() and json.loads(path.read_text()) == json.loads(data), path
            else:
                assert path.exists() and Image.open(path).convert('RGBA').tobytes() == Image.open(io.BytesIO(data)).convert('RGBA').tobytes(), path
        else:
            path.parent.mkdir(parents=True, exist_ok=True); path.write_bytes(data)
    print(('Verified' if check else 'Drew') + ' the split-feed night: the screen\'s blood, the parents\' blood, the hallway face and the house\'s papers, wainscot, carpet, ceiling and tile.')


if __name__ == '__main__':
    main()
