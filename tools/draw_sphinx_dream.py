"""0.4.342, the Sphinx's dream (docs/SPHINX_DREAM_0_4_342.md): her whole body, and the piece of the Dream Stela she leaves on the sill.

The owner: "The dream should build the sphinx's whole body as a massive entity. As described in the document." The document: "Give the
reader a hand, an eye, or a wing at a time. The final dream is where they can see the whole animal ... Let the painted stone, skin, and
feathers take the light of their surroundings."

`body.png` is one picture with a region for each surface of her body (`SphinxDreamRenderer` lays each box face over one region): her
tawny coat, the darker coat along her back, her pale belly, her paws with their dark claws, the tops and undersides of her wings, the long
flight feathers, the headcloth's faded stripes behind her face, the tuft of her tail and the dark behind her face. Her face itself is the
face of the first crossing (sphinx_face_art.py), unchanged. The coat and claws use the same tones as the paw and fur of the crossings.

The stela: a palm-sized piece of granite, sand still in its cuts, with one small carving from the riddle the reader last solved (the
vestibule stand on its tile, a chip above its stair, an iron pin meeting a level line, the road going under the stalks) or worn smooth
where a carving should be; each also with one corner freshly broken, for a hostile dream.

    python3 tools/draw_sphinx_dream.py           # write the pictures, item models and names
    python3 tools/draw_sphinx_dream.py --check   # verify them
"""
from pathlib import Path
import argparse
import json
import math
import random
from PIL import Image

from draw_tank_fittings import shade

ROOT = Path(__file__).resolve().parents[1]
A = ROOT / 'src/main/resources/assets/the_oldest_house'
NS = 'the_oldest_house:'

# Regions of body.png: (u, v, width, height) in pixels. SphinxDreamRenderer.Region mirrors these.
REGIONS = {
    'fur': (0, 0, 64, 64), 'back': (64, 0, 64, 64), 'belly': (128, 0, 64, 64), 'paw': (192, 0, 64, 64),
    'wing_top': (0, 64, 128, 64), 'wing_under': (128, 64, 128, 64), 'primary': (0, 128, 128, 64),
    'nemes': (128, 128, 64, 64), 'tuft': (192, 128, 32, 32), 'dark': (224, 128, 32, 32),
    # Her body is built of many blocks: each face of the coat's blocks takes a window of its region at one texel to a model pixel; the
    # feathers, claws, pads, band and collar are stretched whole over theirs.
    'feather': (0, 192, 16, 64), 'feather_under': (16, 192, 16, 64), 'covert': (32, 192, 64, 32), 'band': (32, 224, 64, 16),
    'collar': (32, 240, 64, 16), 'shade': (96, 192, 64, 64), 'claw': (192, 160, 16, 16), 'pad': (208, 160, 16, 16),
    # The same feather turned end for end, for the folded wing, whose feather tops face out from her flank on the blocks' other face.
    'feather_down': (160, 192, 16, 64), 'feather_under_up': (176, 192, 16, 64),
}
# 0.4.342: the owner, "match the tone with her face": her coat takes the worn umber of her face's stone (cheeks #59391a, brow #7a6035,
# the broken nose #8e7047), not a living lion's tawny.
FUR = ['#7a5a2e', '#6e5028', '#866436', '#664a24']
BLUE = ['#28344a', '#232e42', '#2e3a50', '#1e283a']
GOLD = ['#806a36', '#766230', '#8a723c', '#6c582a']


def px(im, x, y, c, k=0):
    if 0 <= x < im.width and 0 <= y < im.height:
        im.putpixel((x, y), shade(c, k))


def coat(im, u, v, w, h, palette, seed, strokes, stroke_colour, weights=(4, 3, 2, 2)):
    """Coarse coat: each pixel one of a few close tones, with short hairs running down."""
    rnd = random.Random(seed)
    for y in range(h):
        for x in range(w):
            px(im, u + x, v + y, rnd.choices(palette, weights)[0], rnd.randint(-3, 3))
    for _ in range(strokes):
        x, y = rnd.randrange(w), rnd.randrange(h - 2)
        for i in range(rnd.choice((2, 3))):
            px(im, u + x, v + y + i, stroke_colour, rnd.randint(-4, 2))


def body():
    im = Image.new('RGBA', (256, 256), (0, 0, 0, 0))
    u, v, w, h = REGIONS['fur']; coat(im, u, v, w, h, FUR, 3420, 140, '#58401e')
    u, v, w, h = REGIONS['back']; coat(im, u, v, w, h, ['#5e4420', '#56401e', '#664a26', '#4e3a1c'], 3421, 170, '#3e2c14')
    # A darker line down her spine.
    for y in range(h):
        for x in (31, 32):
            px(im, u + x, v + y, '#3a2812', random.Random(y).randint(-4, 4))
    u, v, w, h = REGIONS['belly']; coat(im, u, v, w, h, ['#94744a', '#8a6c44', '#9e7e52', '#80643e'], 3422, 60, '#6e5634')
    # Her paws: the coat, toes parted by dark lines, and a claw at the front of each toe along the bottom edge.
    u, v, w, h = REGIONS['paw']; coat(im, u, v, w, h, FUR, 3423, 90, '#58401e')
    rnd = random.Random(3424)
    for t in range(1, 4):
        x = t * w // 4
        for y in range(h // 3, h):
            px(im, u + x, v + y, '#5a4220', rnd.randint(-4, 2))
    for t in range(4):
        cx = t * w // 4 + w // 8
        for y in range(h - 14, h):
            half = max(1, (h - y) // 4)
            for x in range(cx - half, cx + half + 1):
                c = '#6e5c48' if y < h - 11 else '#2a2420'
                px(im, u + x, v + y, c, rnd.randint(-4, 4))
            px(im, u + cx - half, v + y, '#5a4e42')
    # The tops of her wings: small coverts in scallops along the leading edge, a faded band of painted lapis and gold, then the long
    # secondaries, each with its dark shaft, running back to the trailing edge (the bottom of the region).
    u, v, w, h = REGIONS['wing_top']; rnd = random.Random(3425)
    for y in range(h):
        for x in range(w):
            dk = 0
            if y < 18:
                row = y // 6; sc = ((x + row * 3) % 8) - 4
                edge = (y % 6) >= 4 + abs(sc) // 3
                c = rnd.choice(['#7a5a2e', '#6e5028', '#86643a']) if not edge else '#46321a'
            elif y < 30:
                band = (y - 18) // 4
                c = rnd.choice(BLUE if band != 1 else GOLD)
                if ((x + band * 4) % 8) == 0:
                    c = '#1a1c2c'
            else:
                f = x % 9
                c = '#4a3418' if f == 4 else rnd.choice(['#6e5026', '#62461f', '#7a5a2c', '#584020'])
                if f == 0:
                    c = '#3a2812'
                if y > h - 7:
                    dk = -26
            px(im, u + x, v + y, c, rnd.randint(-4, 4) - int(10 * y / h) + dk)
    # Their undersides: pale, buff and bone, with dark bars across the feathers and a dark trailing edge: what covers the yard as she
    # passes over it.
    u, v, w, h = REGIONS['wing_under']; rnd = random.Random(3426)
    for y in range(h):
        for x in range(w):
            c = rnd.choice(['#9c8866', '#92805e', '#a69270', '#887656'])
            k = rnd.randint(-4, 4)
            if (y + (x % 9) // 3) % 11 in (0, 1) and y > 10:
                k -= 34
            if x % 9 == 0 and y > 14:
                k -= 18
            if y > h - 9:
                k -= 40 + (y - (h - 9)) * 4
            if y < 6:
                k -= 10
            px(im, u + x, v + y, c, k)
    # The long flight feathers: each a narrow strip with a shaft, darkening to the tip (the bottom of the region), separated by gaps.
    u, v, w, h = REGIONS['primary']; rnd = random.Random(3427)
    for x in range(w):
        f = x % 11
        for y in range(h):
            if f == 10:
                continue  # the gap between two feathers
            c = rnd.choice(['#5e4420', '#544024', '#68502a', '#4c381c'])
            k = rnd.randint(-4, 4) - int(40 * (y / h) ** 2)
            if f == 5:
                c, k = '#2e2010', k - 6
            if (y // 7) % 3 == 0 and y > 8:
                k -= 14
            px(im, u + x, v + y, c, k)
    # The headcloth behind and beside her face: the stripes of the first crossing, faded, worn through to stone in places.
    u, v, w, h = REGIONS['nemes']; rnd = random.Random(3428)
    for y in range(h):
        for x in range(w):
            band = (y // 8) % 2
            worn = rnd.random() < 0.22
            c = rnd.choice(['#94744a', '#8a6c44', '#9c7a4e']) if worn else rnd.choice(BLUE if band == 0 else GOLD)
            px(im, u + x, v + y, c, rnd.randint(-5, 5) - (8 if y % 8 == 7 else 0))
    u, v, w, h = REGIONS['tuft']; rnd = random.Random(3429)
    for y in range(h):
        for x in range(w):
            px(im, u + x, v + y, rnd.choice(['#3e2c18', '#33240f', '#4a3420', '#2a1e0e']), rnd.randint(-4, 4))
    u, v, w, h = REGIONS['dark']; rnd = random.Random(3430)
    for y in range(h):
        for x in range(w):
            px(im, u + x, v + y, rnd.choice(['#1c1610', '#18120c', '#221a12']), rnd.randint(-3, 3))
    feathers(im)
    return im


def feathers(im):
    """The regions of the many-block body: one flight feather, top and underside; the coverts' scalloped rows; the faded painted band;
    the beaded collar; the coat in shadow; the claws' horn and the pads' leather."""
    # A flight feather seen from above, its tip at the top: a rounded, notched tip, a dark shaft, bars across the vanes, a pale edge,
    # darkening to the tip. Outside the vanes is clear, so each feather block shows its own outline.
    u, v, w, h = REGIONS['feather']; rnd = random.Random(3431)
    for y in range(h):
        half = 7.5 if y > 9 else 7.5 * math.sqrt(max(0.0, 1 - ((9 - y) / 10) ** 2))
        for x in range(w):
            d = abs(x - 7.5)
            if d > half:
                continue
            c = rnd.choice(['#5e4420', '#544024', '#68502a', '#4c381c'])
            k = rnd.randint(-4, 4) - int(30 * (1 - y / h) ** 2)
            if d < 1:
                c, k = '#2e2010', k - 4
            elif d > half - 1:
                c, k = '#6e5634', k
            if (y + int(d)) % 8 in (0, 1) and y > 6:
                k -= 16
            px(im, u + x, v + y, c, k)
    # Its underside, root at the top and tip at the bottom: pale buff and bone, dark bars, a dark tip.
    u, v, w, h = REGIONS['feather_under']; rnd = random.Random(3432)
    for y in range(h):
        t = h - 1 - y
        half = 7.5 if t > 9 else 7.5 * math.sqrt(max(0.0, 1 - ((9 - t) / 10) ** 2))
        for x in range(w):
            d = abs(x - 7.5)
            if d > half:
                continue
            c = rnd.choice(['#9c8866', '#92805e', '#a69270', '#887656'])
            k = rnd.randint(-4, 4)
            if d < 1:
                c = '#7a6646'
            if (y + int(d)) % 9 in (0, 1):
                k -= 30
            if t < 8:
                k -= 40 + (8 - t) * 4
            px(im, u + x, v + y, c, k)
    for src, dst in (('feather', 'feather_down'), ('feather_under', 'feather_under_up')):
        su, sv, w, h = REGIONS[src]; du, dv, _, _ = REGIONS[dst]
        im.paste(im.crop((su, sv, su + w, sv + h)).transpose(Image.FLIP_TOP_BOTTOM), (du, dv))
    # The coverts: small rounded feathers in overlapping rows, each with a dark lower edge.
    u, v, w, h = REGIONS['covert']; rnd = random.Random(3433)
    for y in range(h):
        for x in range(w):
            row = y // 5; sc = ((x + row * 3) % 6) - 3
            edge = (y % 5) >= 3 + abs(sc) // 2
            c = '#46321a' if edge else rnd.choice(['#7a5a2e', '#6e5028', '#86643a', '#664a26'])
            px(im, u + x, v + y, c, rnd.randint(-4, 4) - (6 if (x + row * 3) % 6 == 0 else 0))
    # The band of painted lapis and gold across the coverts, faded and worn through in places.
    u, v, w, h = REGIONS['band']; rnd = random.Random(3434)
    for y in range(h):
        for x in range(w):
            band = y * 3 // h
            c = rnd.choice(GOLD if band == 1 else BLUE)
            if rnd.random() < 0.18:
                c = rnd.choice(['#7a5a2e', '#6e5028'])
            if (x + band * 4) % 8 == 0 or y % (h // 3) == 0:
                c = '#1a1c2c'
            px(im, u + x, v + y, c, rnd.randint(-4, 4))
    # The broad collar on her chest: rows of lapis, gold and carnelian beads between dark lines, faded to stone in places.
    u, v, w, h = REGIONS['collar']; rnd = random.Random(3435)
    rows = [BLUE, GOLD, ['#8a3a22', '#7e3420', '#943f26', '#6e2e1a'], GOLD, BLUE]
    for y in range(h):
        r = min(len(rows) - 1, y * len(rows) // h)
        for x in range(w):
            c = rnd.choice(rows[r])
            if y * len(rows) % h < len(rows):
                c = '#1a1610'
            elif (x + r * 2) % 4 == 0:
                c = shade(c, -24)
            if rnd.random() < 0.14:
                c = rnd.choice(['#94744a', '#8a6c44'])
            px(im, u + x, v + y, c, rnd.randint(-4, 4))
    # The coat in shadow: the insides of her legs, under her joints.
    u, v, w, h = REGIONS['shade']; coat(im, u, v, w, h, ['#54401f', '#4c381c', '#5a4424', '#443218'], 3436, 120, '#36280f')
    # Claws: pale horn at the root darkening to the point.
    u, v, w, h = REGIONS['claw']; rnd = random.Random(3437)
    for y in range(h):
        for x in range(w):
            px(im, u + x, v + y, rnd.choice(['#8e7a60', '#86725a', '#968268']), rnd.randint(-4, 4) - int(70 * y / h))
    # Pads: dark, cracked leather.
    u, v, w, h = REGIONS['pad']; rnd = random.Random(3438)
    for y in range(h):
        for x in range(w):
            px(im, u + x, v + y, rnd.choice(['#2e241c', '#282018', '#342a20']), rnd.randint(-4, 4) - (10 if (x * 3 + y * 5) % 7 == 0 else 0))


# ------------------------------------------------------------------------------------------------ the stela
CARVINGS = ['early', 'stair', 'shelf', 'field', 'smooth']
GRANITE = ['#8c7c78', '#827270', '#968684', '#786866', '#a09090']
SAND = '#c8b080'


def outline(chipped):
    """The piece's edge: an irregular slab, its corner freshly broken away when chipped."""
    cells = set()
    for y in range(2, 15):
        for x in range(2, 14):
            if (x, y) in ((2, 2), (13, 2), (2, 14), (13, 14), (3, 2), (2, 3)):
                continue
            if y == 2 and x > 10:
                continue
            if chipped and x + (14 - y) > 20:
                continue
            cells.add((x, y))
    return cells


def stela(carving, chipped):
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0)); rnd = random.Random(3440 + CARVINGS.index(carving) * 2 + chipped)
    cells = outline(chipped)
    for (x, y) in cells:
        k = rnd.randint(-6, 6) + (8 if y < 5 else 0) - (14 if y > 12 else 0)
        px(im, x, y, rnd.choices(GRANITE, [4, 3, 3, 2, 1])[0], k)
        if rnd.random() < .05:
            px(im, x, y, '#5a4e4c', rnd.randint(-4, 4))
    # The edge: a dark rim below and right, a pale one above and left.
    for (x, y) in cells:
        if (x + 1, y) not in cells or (x, y + 1) not in cells:
            px(im, x, y, '#5a4c4a')
        elif (x - 1, y) not in cells or (x, y - 1) not in cells:
            px(im, x, y, '#b0a0a0')
    if chipped:
        # The fresh break: paler, unweathered stone along the broken corner.
        for (x, y) in cells:
            if x + (14 - y) in (19, 20):
                px(im, x, y, '#c8bcbc', rnd.randint(-4, 4))

    def cut(x, y):
        """A carved line: a dark groove with sand caught in it."""
        if (x, y) in cells:
            px(im, x, y, SAND, rnd.randint(-8, 4))
            if (x + 1, y + 1) in cells and im.getpixel((x + 1, y + 1))[:3] != shade(SAND, 0)[:3]:
                px(im, x + 1, y + 1, '#4a3e3c', rnd.randint(-4, 4))

    if carving == 'early':
        # The vestibule's stand on its square of tile: a post with a crook at the top, standing in the middle of a square.
        for x in range(4, 12):
            cut(x, 12)
        for x in (4, 11):
            for y in range(10, 13):
                cut(x, y)
        for y in range(4, 11):
            cut(8, y)
        cut(9, 4); cut(10, 5); cut(7, 10); cut(9, 10)
    elif carving == 'stair':
        # Three steps going down, and a chip of stone resting above them.
        for i, x0 in enumerate((4, 6, 8)):
            for x in range(x0, x0 + 3):
                cut(x, 8 + i * 2)
            cut(x0, 8 + i * 2 + 1)
        cut(10, 5); cut(11, 5); cut(10, 6)
    elif carving == 'shelf':
        # An iron pin meeting a level line.
        for x in range(4, 12):
            cut(x, 9)
        for y in range(5, 12):
            cut(8, y)
        cut(7, 5); cut(9, 5)
    elif carving == 'field':
        # A road going under the stalks: two lines drawing together, short upright strokes across their far end.
        for y in range(6, 13):
            off = (12 - y) // 2
            cut(5 + off, y); cut(10 - off, y)
        for x in (5, 7, 9, 11):
            cut(x, 4); cut(x, 5)
    else:
        # Worn smooth where a carving should be: a paler, polished patch.
        for y in range(6, 11):
            for x in range(5, 11):
                if (x, y) in cells and (x - 8) ** 2 + (y - 8) ** 2 < 10:
                    px(im, x, y, '#a89a98', rnd.randint(-3, 3))
    return im


def variants():
    """Each carving, whole and chipped, in the order of the item's custom model data (DreamStela: index*2 + chipped + 1)."""
    out = []
    for c in CARVINGS:
        for chipped in (0, 1):
            out.append((c, chipped, f'dream_stela_{c}' + ('_chipped' if chipped else '')))
    return out


def pictures():
    out = {'entity/sphinx/body.png': body()}
    for c, chipped, name in variants():
        out[f'item/sphinx/{name}.png'] = stela(c, chipped)
    return out


def files():
    out = {}
    for _, _, name in variants():
        out[f'models/item/sphinx/{name}.json'] = {'parent': 'minecraft:item/generated', 'textures': {'layer0': NS + f'item/sphinx/{name}'}}
    out['models/item/dream_stela.json'] = {
        'parent': 'minecraft:item/generated', 'textures': {'layer0': NS + 'item/sphinx/dream_stela_smooth'},
        'overrides': [{'predicate': {'custom_model_data': i + 1}, 'model': NS + f'item/sphinx/{name}'} for i, (_, _, name) in enumerate(variants())]}
    return out


NAMES = {'item.the_oldest_house.dream_stela': 'Piece of the Dream Stela', 'entity.the_oldest_house.sphinx_dream': 'The Sphinx'}


def main():
    check = argparse.ArgumentParser(); check.add_argument('--check', action='store_true'); check = check.parse_args().check
    for rel, im in pictures().items():
        path = A / 'textures' / rel
        if check:
            with Image.open(path) as actual:
                assert actual.convert('RGBA').tobytes() == im.tobytes(), rel
        else:
            path.parent.mkdir(parents=True, exist_ok=True); im.save(path, optimize=True)
    for rel, obj in files().items():
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
    print(('Verified' if check else 'Drew') + f" the Sphinx's dream: her body and {len(variants())} pieces of the stela.")


if __name__ == '__main__':
    main()
