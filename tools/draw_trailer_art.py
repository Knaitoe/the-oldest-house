"""Goatman's trailer (0.4.382, docs/TEXTURE_PASS_TRAILER_0_4_382.md): the texture pass's ninth release.

Draws, in native Minecraft pixels from fixed seeds, the camper's own skin where it was vanilla terracotta, concrete, iron and smooth
stone. Its walls are one block thick, so each wall block has two faces: ribbed aluminium outside and the camper's wood-veneer
panelling inside. The skirt course is darker sheet with a skirting board inside; the band course carries the trailer's painted
stripe; the long sides' top course is the drip rail outside and the ceiling cove inside. The corners are rounded trim; the roof is
coated sheet on top and the camper's ceiling underneath; the floor is linoleum in two tones laid as a checker. Writes the models and
block states (the walls' by facing, the roof's as a slab) and the blocks' names. --check verifies everything written.
"""
from pathlib import Path
import argparse
import json
import random
from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
A = ROOT / 'src/main/resources/assets/the_oldest_house'
NS = 'the_oldest_house:'
P = NS + 'block/room_shell/'

METAL = '#d3d1c8'
SKIRT = '#8e8f8a'
BAND = '#3f7c76'
TRIM = '#b9bab4'
VENEER = '#8a6744'
ROOF = '#c9c8c1'
CEILING = '#d9d2bf'
LINO = '#d8cfae'
LINO_DARK = '#a98f5e'


def rgb(c):
    c = c.lstrip('#')
    return tuple(int(c[i:i + 2], 16) for i in (0, 2, 4))


def shade(c, k, a=255):
    r, g, b = rgb(c) if isinstance(c, str) else c[:3]
    return (max(0, min(255, r + k)), max(0, min(255, g + k)), max(0, min(255, b + k)), a)


def ribs(im, rnd, base, y0=0, y1=16, streaks=True):
    """Ribbed sheet across the block: a lit crest and a shadowed trough every four pixels, rivets at the seam."""
    drips = {x: rnd.randrange(2, 9) for x in rnd.sample(range(16), 2)} if streaks else {}
    for y in range(y0, y1):
        for x in range(16):
            row = y % 4
            k = rnd.randint(-2, 2) + (9 if row == 0 else -12 if row == 3 else 2 if row == 1 else 0)
            if x in drips and y < drips[x] + y0 and row != 0:
                k -= 6
            im.putpixel((x, y), shade(base, k))
    for y in range(y0 + 1, y1, 4):
        im.putpixel((0, y), shade(base, -30))
        im.putpixel((15, y), shade(base, -22))


def siding_out(seed, streaked=False):
    rnd = random.Random(seed)
    im = Image.new('RGBA', (16, 16))
    ribs(im, rnd, METAL, streaks=streaked)
    return im


def band_out(seed):
    """The painted stripe along the side: the paint faded and chipped back to metal at the ribs' crests."""
    rnd = random.Random(seed)
    im = Image.new('RGBA', (16, 16))
    ribs(im, rnd, METAL, streaks=False)
    for y in range(3, 13):
        for x in range(16):
            row = y % 4
            k = rnd.randint(-3, 3) + (8 if row == 0 else -12 if row == 3 else 0)
            chipped = row == 0 and rnd.random() < .12
            im.putpixel((x, y), shade(METAL, 6) if chipped else shade(BAND, k))
    for x in range(16):
        im.putpixel((x, 2), shade(BAND, -26))
        im.putpixel((x, 13), shade(BAND, -30))
    return im


def skirt_out(seed):
    """The lowest course: darker sheet under a drip edge, splashed with mud along its foot."""
    rnd = random.Random(seed)
    im = Image.new('RGBA', (16, 16))
    ribs(im, rnd, SKIRT, streaks=False)
    for x in range(16):
        im.putpixel((x, 0), shade(TRIM, 14))
        im.putpixel((x, 1), shade(TRIM, -18))
        for y in range(12, 16):
            if rnd.random() < (y - 11) * .2:
                im.putpixel((x, y), shade('#5d5140', rnd.randint(-8, 6)))
    return im


def eave_out(seed):
    """The drip rail along the top of the long sides: a rounded rail over the last rib of siding."""
    rnd = random.Random(seed)
    im = Image.new('RGBA', (16, 16))
    ribs(im, rnd, METAL, y0=8, streaks=False)
    for y in range(8):
        k = {0: 18, 1: 12, 2: 6, 3: 0, 4: -6, 5: -14, 6: -26, 7: -36}[y]
        for x in range(16):
            im.putpixel((x, y), shade(TRIM, k + rnd.randint(-2, 2)))
    for x in (3, 11):
        im.putpixel((x, 3), shade(TRIM, -40))
    return im


def corner(seed):
    """The rounded corner trim, lit down its middle, screwed every four pixels."""
    rnd = random.Random(seed)
    im = Image.new('RGBA', (16, 16))
    for y in range(16):
        for x in range(16):
            k = 16 - abs(x - 7) * 4 + rnd.randint(-2, 2)
            if x in (0, 15):
                k = -30
            im.putpixel((x, y), shade(TRIM, k))
    for y in range(2, 16, 4):
        im.putpixel((7, y), shade(TRIM, -46))
    return im


def veneer(seed, foot=False, cove=False):
    """The camper's panelling: wood-grain veneer in boards four wide, a dark groove between them; a skirting board at its foot,
    or the ceiling cove at its head."""
    rnd = random.Random(seed)
    im = Image.new('RGBA', (16, 16))
    tones = [rnd.randint(-6, 6) for _ in range(4)]
    for x in range(16):
        b = x // 4
        grain = [rnd.random() < .18 for _ in range(16)]
        for y in range(16):
            k = tones[b] + rnd.randint(-2, 2) + (-9 if grain[(y + x * 3) % 16] else 0)
            if x % 4 == 0:
                k = -30
            im.putpixel((x, y), shade(VENEER, k))
    if foot:
        for x in range(16):
            for y in range(12, 16):
                im.putpixel((x, y), shade(VENEER, -34 + (8 if y == 12 else 0) + rnd.randint(-2, 2)))
    if cove:
        for x in range(16):
            for y in range(4):
                im.putpixel((x, y), shade(CEILING, (-4 * y) + rnd.randint(-2, 2)))
            im.putpixel((x, 4), shade(VENEER, -24))
    return im


def edge(seed):
    """The wall seen end on: a skin of metal, the frame, the panelling."""
    rnd = random.Random(seed)
    im = Image.new('RGBA', (16, 16))
    for y in range(16):
        for x in range(16):
            c = shade(METAL, 4) if y < 2 else shade(VENEER, -8) if y > 13 else shade('#9d958a', rnd.randint(-6, 6))
            im.putpixel((x, y), c)
    return im


def roof_top(seed):
    """Coated roof sheet: seams every eight pixels, dirt settled along them."""
    rnd = random.Random(seed)
    im = Image.new('RGBA', (16, 16))
    for y in range(16):
        for x in range(16):
            k = rnd.randint(-4, 4) - (18 if x % 8 == 0 else 6 if x % 8 in (1, 7) else 0)
            if rnd.random() < .04:
                k -= 14
            im.putpixel((x, y), shade(ROOF, k))
    return im


def roof_side(seed):
    rnd = random.Random(seed)
    im = Image.new('RGBA', (16, 16))
    for y in range(16):
        for x in range(16):
            k = 12 if y in (8, 9) else -22 if y in (14, 15) else rnd.randint(-3, 3)
            im.putpixel((x, y), shade(TRIM, k))
    return im


def ceiling(seed):
    """The camper's ceiling: vinyl-faced board between battens every eight pixels, a water mark here and there."""
    rnd = random.Random(seed)
    im = Image.new('RGBA', (16, 16))
    stain = (rnd.randrange(3, 13), rnd.randrange(3, 13))
    for y in range(16):
        for x in range(16):
            k = rnd.randint(-2, 2) + (-16 if x % 8 == 0 else 0)
            d = (x - stain[0]) ** 2 + (y - stain[1]) ** 2
            if 6 <= d <= 9:
                k -= 10
            im.putpixel((x, y), shade(CEILING, k))
    return im


def lino(seed, base):
    """Linoleum: a mottle of small flecks, a hairline where the tile meets its neighbour."""
    rnd = random.Random(seed)
    im = Image.new('RGBA', (16, 16))
    for y in range(16):
        for x in range(16):
            r = rnd.random()
            k = rnd.randint(-3, 3) + (-16 if r < .08 else 12 if r > .94 else 0) - (8 if x == 0 or y == 0 else 0)
            im.putpixel((x, y), shade(base, k))
    return im


WALLS = [('trailer_siding', 'Trailer Siding', 'trailer_panel'), ('trailer_band', 'Trailer Stripe', 'trailer_panel'),
         ('trailer_skirt', 'Trailer Skirt', 'trailer_panel_foot'), ('trailer_eave', 'Trailer Drip Rail', 'trailer_panel_cove')]
BLOCKS = WALLS + [('trailer_corner', 'Trailer Corner Trim', None), ('trailer_roof', 'Trailer Roof', None),
                  ('trailer_lino', 'Camper Linoleum', None), ('trailer_lino_dark', 'Camper Linoleum Tile', None)]


def textures():
    return {
        'trailer_siding': siding_out(3821), 'trailer_siding_streaked': siding_out(3822, True), 'trailer_band': band_out(3823),
        'trailer_skirt': skirt_out(3824), 'trailer_eave': eave_out(3825), 'trailer_corner': corner(3826),
        'trailer_panel': veneer(3827), 'trailer_panel_foot': veneer(3828, foot=True), 'trailer_panel_cove': veneer(3829, cove=True),
        'trailer_edge': edge(3830), 'trailer_roof': roof_top(3831), 'trailer_roof_side': roof_side(3832),
        'trailer_ceiling': ceiling(3833), 'trailer_lino': lino(3834, LINO), 'trailer_lino_dark': lino(3835, LINO_DARK),
    }


def wall_model(outside, inside):
    """North is the outside: a facing of north, east, south or west turns it to face away from the trailer."""
    faces = {'north': '#out', 'south': '#in', 'east': '#out', 'west': '#out', 'up': '#edge', 'down': '#edge'}
    return {'parent': 'minecraft:block/block', 'textures': {'particle': P + outside, 'out': P + outside, 'in': P + inside,
                                                            'edge': P + 'trailer_edge'},
            'elements': [{'from': [0, 0, 0], 'to': [16, 16, 16],
                          'faces': {f: {'texture': t, 'cullface': f} for f, t in faces.items()}}]}


def cube(name):
    return {'parent': 'minecraft:block/cube_all', 'textures': {'all': P + name}}


def files():
    out = {}
    rot = {'north': 0, 'east': 90, 'south': 180, 'west': 270}
    for name, _, inside in WALLS:
        model = NS + f'block/room_shell_{name}'
        out[f'models/block/room_shell_{name}.json'] = wall_model(name, inside)
        models = [model]
        if name == 'trailer_siding':
            # About one stretch in five carries a rust streak from its rivets, picked by where it stands.
            out[f'models/block/room_shell_{name}_streaked.json'] = wall_model(name + '_streaked', inside)
            models = [model] * 4 + [model + '_streaked']
        variants = {}
        for facing, y in rot.items():
            vs = [dict({'model': m}, **({'y': y} if y else {})) for m in models]
            variants[f'facing={facing}'] = vs if len(vs) > 1 else vs[0]
        out[f'blockstates/room_shell_{name}.json'] = {'variants': variants}
    out['models/block/room_shell_trailer_corner.json'] = {'parent': 'minecraft:block/cube_column',
                                                          'textures': {'side': P + 'trailer_corner', 'end': P + 'trailer_edge'}}
    out['blockstates/room_shell_trailer_corner.json'] = {'variants': {'': {'model': NS + 'block/room_shell_trailer_corner'}}}
    tex = {'bottom': P + 'trailer_ceiling', 'top': P + 'trailer_roof', 'side': P + 'trailer_roof_side'}
    roof = NS + 'block/room_shell_trailer_roof'
    out['models/block/room_shell_trailer_roof.json'] = {'parent': 'minecraft:block/slab', 'textures': tex}
    out['models/block/room_shell_trailer_roof_top.json'] = {'parent': 'minecraft:block/slab_top', 'textures': tex}
    out['models/block/room_shell_trailer_roof_double.json'] = {'parent': 'minecraft:block/cube_bottom_top', 'textures': tex}
    out['blockstates/room_shell_trailer_roof.json'] = {'variants': {
        'type=bottom': {'model': roof}, 'type=top': {'model': roof + '_top'}, 'type=double': {'model': roof + '_double'}}}
    for name in ('trailer_lino', 'trailer_lino_dark'):
        model = NS + f'block/room_shell_{name}'
        out[f'models/block/room_shell_{name}.json'] = cube(name)
        out[f'blockstates/room_shell_{name}.json'] = {'variants': {'': [{'model': model}, {'model': model, 'y': 90},
                                                                          {'model': model, 'y': 180}, {'model': model, 'y': 270}]}}
    return out


def main():
    check = argparse.ArgumentParser()
    check.add_argument('--check', action='store_true')
    check = check.parse_args().check
    images, data = textures(), files()
    for key, im in images.items():
        path = A / 'textures/block/room_shell' / f'{key}.png'
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
    for name, title, *_ in BLOCKS:
        key = f'block.the_oldest_house.room_shell_{name}'
        if check:
            assert lang.get(key) == title, key
        else:
            lang[key] = title
    if not check:
        lang_path.write_text(json.dumps(lang, indent=2, ensure_ascii=False) + '\n')
    print(('Verified' if check else 'Drew') + f' Goatman\'s trailer: {len(images)} textures, {len(data)} models and states.')


if __name__ == '__main__':
    main()
