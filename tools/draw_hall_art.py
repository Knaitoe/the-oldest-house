"""The ordinary halls (0.4.219, docs/TEXTURE_PASS_HALLS_0_4_219.md): the first release of the texture pass.

Draws, in native Minecraft pixels from fixed seeds, the surfaces of the halls every reader walks between the stories: warm plaster
in three variants (plain; a hairline crack; a water stain come down from the ceiling), a picture rail, a skirting board, floorboards
and the same boards worn pale down the middle where people walk, a plaster ceiling, and a two-pane sash window in two halves with
nothing but dark behind the glass. Writes the models and block states (each floor also turned a quarter for halls that run east to
west), and the blocks' names. --check verifies everything written.
"""
from pathlib import Path
import argparse
import json
import random
from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
A = ROOT / 'src/main/resources/assets/the_oldest_house'
NS = 'the_oldest_house:'
P = NS + 'block/hall/'

PLASTER = '#d4c8b3'
WOOD = '#6c4d32'
PAINT = '#d0c7b3'


def rgb(c):
    c = c.lstrip('#')
    return tuple(int(c[i:i + 2], 16) for i in (0, 2, 4))


def shade(c, k, a=255):
    r, g, b = rgb(c) if isinstance(c, str) else c[:3]
    return (max(0, min(255, r + k)), max(0, min(255, g + k)), max(0, min(255, b + k)), a)


def mix(c, d, t):
    a, b = (rgb(x) if isinstance(x, str) else x[:3] for x in (c, d))
    return tuple(round(a[i] + (b[i] - a[i]) * t) for i in range(3)) + (255,)


def dot(im, x, y, colour):
    if 0 <= x < 16 and 0 <= y < 16:
        im.putpixel((x, y), colour if len(colour) == 4 else colour + (255,))


def plaster(seed, base=PLASTER, spread=3):
    """Plaster laid by hand: fine grain, a few soft patches where the trowel went over twice, never a hard edge."""
    rnd = random.Random(seed); im = Image.new('RGBA', (16, 16))
    patches = [(rnd.randrange(16), rnd.randrange(16), rnd.choice((-4, -3, 3, 4))) for _ in range(3)]
    for y in range(16):
        for x in range(16):
            k = rnd.randint(-spread, spread)
            for px, py, d in patches:
                # Wrapped distance, so the patches tile across block edges.
                dx = min(abs(x - px), 16 - abs(x - px)); dy = min(abs(y - py), 16 - abs(y - py))
                if dx * dx + dy * dy <= 6:
                    k += d
            im.putpixel((x, y), shade(base, k))
    # A trowel stroke or two, a pixel lighter, lying flat.
    for _ in range(2):
        x0, y0 = rnd.randrange(16), rnd.randrange(16)
        for i in range(rnd.randint(3, 5)):
            dot(im, (x0 + i) % 16, y0, shade(im.getpixel(((x0 + i) % 16, y0)), 4))
    return im


def _line(im, pts, dark=-46, light=7):
    """A hairline along points: dark in the crack, a pixel catching the light beside it, fainter at both tips."""
    n = len(pts)
    for i, (x, y) in enumerate(pts):
        tip = min(i, n - 1 - i)
        k = dark if tip >= 2 else dark // 2 if tip == 1 else dark // 3
        dot(im, x, y, shade(PLASTER, k))
        if tip >= 1 and (x + 1, y) not in pts:
            dot(im, x + 1, y, shade(PLASTER, light))


def _walk(rnd, x, y, steps, dx_choices, dy=1):
    pts = [(x, y)]
    for _ in range(steps):
        x = max(1, min(14, x + rnd.choice(dx_choices))); y = max(1, min(14, y + dy))
        pts.append((x, y))
    return pts


def cracked(seed, kind=0):
    """0.4.223 (owner: "wall fissures require more variance"): five kinds of crack, each starting and ending inside its own block,
    so none is cut off at a block's edge: a long wandering hairline with a spur; a diagonal from a corner that forks; a settlement
    crack stepping down like brickwork; a star of short hairlines round a chipped point; two short parallel hairlines."""
    im = plaster(seed); rnd = random.Random(seed + 7)
    if kind == 0:
        x = rnd.randint(4, 10); pts = _walk(rnd, x, 1, 12, (0, 0, 1, -1))
        _line(im, pts); bx, by = pts[6]; _line(im, _walk(rnd, bx - 1, by, 3, (-1,), 1), -34, 0)
    elif kind == 1:
        pts = [(2 + i + (i // 4) % 2, 2 + i) for i in range(9)]
        _line(im, pts); ex, ey = pts[-1]
        _line(im, [(ex + 1, ey), (ex + 2, ey), (ex + 3, ey - 1)], -38, 0); _line(im, [(ex, ey + 1), (ex, ey + 2), (ex - 1, ey + 3)], -38, 0)
    elif kind == 2:
        pts = []; x, y = rnd.randint(2, 4), rnd.randint(3, 5)
        for _ in range(3):
            for i in range(3):
                pts.append((x + i, y))
            x += 3
            for j in range(1, 3):
                pts.append((x - 1, y + j))
            y += 3
        _line(im, [(min(14, a), min(14, b)) for a, b in pts])
    elif kind == 3:
        cx, cy = rnd.randint(6, 9), rnd.randint(6, 9)
        dot(im, cx, cy, shade(PLASTER, -60)); dot(im, cx + 1, cy, shade(PLASTER, -40)); dot(im, cx, cy + 1, shade(PLASTER, -36))
        for dx, dy, n in ((1, -1, 4), (-1, -1, 3), (-1, 1, 4), (1, 1, 3), (1, 0, 3)):
            _line(im, [(cx + dx * (i + 1) + (i // 2 if dy else 0), cy + dy * (i + 1)) for i in range(n)], -36, 0)
    else:
        for x0, y0, n in ((rnd.randint(3, 6), rnd.randint(2, 4), 7), (rnd.randint(8, 11), rnd.randint(5, 7), 6)):
            _line(im, _walk(rnd, x0, y0, n, (0, 0, 1)), -40)
    return im


CRACKS = 5


def stained(seed):
    """A water stain come down from the ceiling: yellowed inside, a brown tide line at its edge, one run below it."""
    im = plaster(seed); rnd = random.Random(seed + 11)
    edge = [rnd.randint(9, 12) for _ in range(16)]
    for x in range(1, 15):
        edge[x] = (edge[x - 1] + edge[x] + edge[x + 1]) // 3
    for x in range(16):
        reach = edge[x] - abs(x - 8) // 2
        for y in range(16):
            if y < reach - 1:
                dot(im, x, y, mix(im.getpixel((x, y)), '#bfae86', .38 + .3 * (1 - y / max(1, reach))))
            elif y in (reach - 1, reach):
                dot(im, x, y, mix(im.getpixel((x, y)), '#8e7652', .55))
    for y in range(edge[9] - 3, 16):
        dot(im, 9, y, mix(im.getpixel((9, y)), '#9c8560', .45))
    return im


def rail(seed):
    """Plaster with a picture rail across it: a moulded strip of stained wood, lit along its top, its shadow on the wall below."""
    im = plaster(seed)
    for x in range(16):
        grain = (x * 7) % 5 == 0
        dot(im, x, 4, shade(WOOD, 34)); dot(im, x, 5, shade(WOOD, 12 - (6 if grain else 0)))
        dot(im, x, 6, shade(WOOD, -4)); dot(im, x, 7, shade(WOOD, -22))
        dot(im, x, 8, shade(im.getpixel((x, 8)), -16)); dot(im, x, 9, shade(im.getpixel((x, 9)), -7))
    return im


def skirting(seed):
    """A skirting board at the foot of the wall: plaster above, then a moulded top, the board itself, and the floor's shadow."""
    im = plaster(seed); rnd = random.Random(seed + 3)
    for x in range(16):
        dot(im, x, 7, shade(im.getpixel((x, 7)), -12))
        dot(im, x, 8, shade(WOOD, 30)); dot(im, x, 9, shade(WOOD, 8))
        for y in range(10, 15):
            dot(im, x, y, shade(WOOD, rnd.randint(-5, 3) - (8 if (x + y * 3) % 11 == 0 else 0)))
        dot(im, x, 15, shade(WOOD, -26))
    # Scuffs where boots and a broom have met it.
    for x, y in ((3, 12), (4, 12), (11, 13), (12, 13), (13, 13)):
        dot(im, x, y, shade(WOOD, 18))
    return im


BOARDS = ('#6e5034', '#755639', '#694b30', '#725337')
JOINTS = (5, 12, 1, 9)


def floor(seed, worn=False):
    """Four boards running the length of the hall, a dark seam between each, their ends butted at different places, the end
    grain catching the light. Worn: the finish gone pale and grey down the middle, where people have walked, and scuffed."""
    rnd = random.Random(seed); im = Image.new('RGBA', (16, 16))
    for b in range(4):
        base = BOARDS[b]; streak = rnd.randrange(4 * b, 4 * b + 3)
        for y in range(16):
            for x in range(4 * b, 4 * b + 4):
                k = rnd.randint(-3, 3) - (5 if x == streak and (y + b) % 6 < 4 else 0)
                c = shade(base, k)
                if worn:
                    centre = max(0, 1 - abs(x - 7.5) / 5.5)
                    c = mix(c, '#a39377', .12 + .5 * centre)
                im.putpixel((x, y), c)
            dot(im, 4 * b + 3, y, shade(im.getpixel((4 * b + 3, y)), -30))
        j = JOINTS[b]
        for x in range(4 * b, 4 * b + 3):
            dot(im, x, j, shade(im.getpixel((x, j)), -24))
        dot(im, 4 * b + 1, (j + 1) % 16, shade(im.getpixel((4 * b + 1, (j + 1) % 16)), 8))
    if worn:
        for x, y in ((6, 3), (7, 3), (9, 8), (10, 8), (5, 13), (6, 13), (7, 13)):
            dot(im, x, y, shade(im.getpixel((x, y)), 10))
    return im


def ceiling(seed):
    return plaster(seed, '#ded7c9', 2)


def window(upper):
    """Half of a two-over-two sash window, painted, with dark behind the glass. The lower half carries the sill and the meeting
    rail along its top; the upper half carries the head of the frame and the meeting rail along its foot."""
    im = Image.new('RGBA', (16, 16))
    for y in range(16):
        for x in range(16):
            # A faint reflection, falling across the panes from the upper left.
            lit = (x + y + (0 if upper else 16)) % 21 in (0, 1)
            im.putpixel((x, y), shade('#1d2833' if lit else '#11171f', 0))
    frame = shade(PAINT, 0); edge = shade(PAINT, -30); light = shade(PAINT, 16)
    for y in range(16):
        dot(im, 0, y, light); dot(im, 15, y, edge); dot(im, 7, y, frame); dot(im, 8, y, shade(PAINT, -12))
        dot(im, 1, y, shade(im.getpixel((1, y)), -4)); dot(im, 9, y, shade(im.getpixel((9, y)), -4))
    if upper:
        for x in range(16):
            dot(im, x, 0, light); dot(im, x, 1, frame); dot(im, x, 2, edge)
            dot(im, x, 14, light); dot(im, x, 15, frame)
    else:
        for x in range(16):
            dot(im, x, 0, frame); dot(im, x, 1, edge)
            dot(im, x, 12, light); dot(im, x, 13, frame); dot(im, x, 14, shade(PAINT, 22)); dot(im, x, 15, shade(PAINT, -40))
    return im


# The blocks: (name, texture, title). The floors' "_x" twins share their textures, turned a quarter in their block states.
BLOCKS = [
    ('plaster', 'plaster', 'Plaster'),
    ('plaster_cracked', 'plaster_cracked', 'Cracked Plaster'),
    ('plaster_stained', 'plaster_stained', 'Stained Plaster'),
    ('rail', 'rail', 'Picture Rail'),
    ('skirting', 'skirting', 'Skirting'),
    ('floor', 'floor', 'Floorboards'),
    ('floor_x', 'floor', 'Floorboards'),
    ('floor_worn', 'floor_worn', 'Worn Floorboards'),
    ('floor_worn_x', 'floor_worn', 'Worn Floorboards'),
    ('ceiling', 'ceiling', 'Ceiling'),
    ('window_lower', 'window_lower', 'Sash Window'),
    ('window_upper', 'window_upper', 'Sash Window'),
]


def textures():
    return {
        'plaster': plaster(219), 'plaster_cracked': cracked(2191), 'plaster_stained': stained(2192), 'rail': rail(2193),
        **{f'plaster_cracked_{k}': cracked(2191 + 10 * k, k) for k in range(1, CRACKS)},
        'skirting': skirting(2194), 'floor': floor(2195), 'floor_worn': floor(2195, worn=True), 'ceiling': ceiling(2196),
        'window_lower': window(False), 'window_upper': window(True),
    }


def files():
    out = {}
    for name, tex, _ in BLOCKS:
        if not name.endswith('_x'):
            out[f'models/block/hall_{name}.json'] = {'parent': 'minecraft:block/cube_all', 'textures': {'all': P + tex}}
        model = NS + f'block/hall_{name[:-2] if name.endswith("_x") else name}'
        variant = {'model': model}
        if name.endswith('_x'):
            variant['y'] = 90
        if name == 'plaster_cracked':
            # 0.4.223: each cracked block picks one of the five cracks by where it stands.
            for k in range(1, CRACKS):
                out[f'models/block/hall_plaster_cracked_{k}.json'] = {'parent': 'minecraft:block/cube_all', 'textures': {'all': P + f'plaster_cracked_{k}'}}
            variant = [{'model': model}] + [{'model': model + f'_{k}'} for k in range(1, CRACKS)]
        out[f'blockstates/hall_{name}.json'] = {'variants': {'': variant}}
    return out


def main():
    check = argparse.ArgumentParser()
    check.add_argument('--check', action='store_true')
    check = check.parse_args().check
    images, data = textures(), files()
    for name, tex, _ in BLOCKS:
        assert tex in images, (name, tex)
    for key, im in images.items():
        path = A / 'textures/block/hall' / f'{key}.png'
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
    for name, _, title in BLOCKS:
        key = f'block.the_oldest_house.hall_{name}'
        if check:
            assert lang.get(key) == title, key
        else:
            lang[key] = title
    if not check:
        lang_path.write_text(json.dumps(lang, indent=2, ensure_ascii=False) + '\n')
    print(('Verified' if check else 'Drew') + f' the halls: {len(images)} textures, {len(data)} models and states.')


if __name__ == '__main__':
    main()
