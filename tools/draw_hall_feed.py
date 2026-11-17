"""The feed (0.4.662): a small closed-circuit monitor on a wall bracket, the kind hung high in a corner of a shop or a stairwell.

Draws, in native Minecraft pixels from fixed seeds: its moulded casing, the casing's top with its vent slots, the front with the dark
screen set in its bezel and a lamp and two knobs below it, and the bracket's steel. Writes the monitor's model, facing north, its screen
on the north face at x 4..12, y 7..12, where the game lays the feed over it (HallFeedClient.draw). Nothing places it: each reader's own
game draws it on the wall. --check verifies everything.
"""
from pathlib import Path
import argparse
import json
import random
import flicker
from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
A = ROOT / 'src/main/resources/assets/the_oldest_house'
NS = 'the_oldest_house:'
P = NS + 'block/hall_feed/'

PLASTIC = (59, 57, 52)
FACE = (67, 65, 59)
GLASS = (18, 22, 20)
STEEL = (85, 88, 91)


def shade(c, k):
    return tuple(max(0, min(255, v + k)) for v in c[:3]) + (255,)


def grain(base, seed, spread=3):
    im = Image.new('RGBA', (16, 16)); px = im.load(); rnd = random.Random(seed)
    for y in range(16):
        for x in range(16):
            px[x, y] = shade(base, rnd.randint(-spread, spread))
    return im


def textures():
    out = {}
    casing = grain(PLASTIC, 6620)
    px = casing.load()
    for x in range(16):            # a moulding seam along the middle of each side
        px[x, 8] = shade(PLASTIC, -5)
    out['casing'] = casing
    top = grain(PLASTIC, 6621)
    px = top.load()
    for y in (5, 7, 9, 11):        # vent slots, front to back
        for x in range(5, 12):
            px[x, y] = shade(PLASTIC, -26)
    out['casing_top'] = top
    front = grain(FACE, 6622, 2)
    px = front.load()
    rnd = random.Random(6623)
    for y in range(3, 11):         # the bezel's inner bevel round the glass
        for x in range(3, 13):
            px[x, y] = shade(FACE, -12)
    for y in range(4, 10):         # the glass, cols 4..11 and rows 4..9: model x 4..12, y 7..12
        for x in range(4, 12):
            px[x, y] = shade(GLASS, rnd.randint(-2, 2))
    for i in range(3):             # a little light caught in the glass's top corner
        px[5 + i, 4 + i] = shade(GLASS, 16 - 4 * i)
    px[4, 11] = shade(FACE, -30); px[6, 11] = shade(FACE, -30)      # two knobs
    px[4, 12] = shade(FACE, -16); px[6, 12] = shade(FACE, -16)
    px[11, 11] = (122, 150, 84, 255)                                   # the lamp
    out['front'] = front
    steel = grain(STEEL, 6624, 4)
    px = steel.load()
    for (x, y) in ((6, 3), (9, 3), (6, 6), (9, 6)):                     # bolt heads on the plate
        px[x, y] = shade(STEEL, 30); px[x + 1, y + 1] = shade(STEEL, -22)
    out['steel'] = steel
    return out


def face(tex, uv):
    return {'texture': '#' + tex, 'uv': uv}


def monitor():
    def box(a, b, faces):
        return {'from': a, 'to': b, 'faces': faces}
    return {
        'ambientocclusion': False,
        'textures': {'casing': P + 'casing', 'top': P + 'casing_top', 'front': P + 'front', 'steel': P + 'steel', 'particle': P + 'casing'},
        'elements': [
            box([2, 3, 1], [14, 13, 9], {
                'north': face('front', [2, 3, 14, 13]), 'south': face('casing', [2, 3, 14, 13]),
                'east': face('casing', [7, 3, 15, 13]), 'west': face('casing', [1, 3, 9, 13]),
                'up': face('top', [2, 1, 14, 9]), 'down': face('casing', [2, 7, 14, 15])}),
            box([4, 5, 9], [12, 11.5, 12], {     # the tube's back
                'south': face('casing', [4, 4.5, 12, 11]), 'east': face('casing', [4, 4.5, 7, 11]),
                'west': face('casing', [9, 4.5, 12, 11]), 'up': face('casing', [4, 9, 12, 12]), 'down': face('casing', [4, 4, 12, 7])}),
            box([7, 3.5, 9], [9, 4.75, 15], {    # the arm, under it, out to the wall
                'east': face('steel', [1, 11.25, 7, 12.5]), 'west': face('steel', [9, 11.25, 15, 12.5]),
                'up': face('steel', [7, 1, 9, 7]), 'down': face('steel', [7, 9, 9, 15])}),
            box([5, 2, 15], [11, 7, 16], {       # the plate on the wall
                'north': face('steel', [5, 9, 11, 14]), 'east': face('steel', [0, 9, 1, 14]), 'west': face('steel', [15, 9, 16, 14]),
                'up': face('steel', [5, 15, 11, 16]), 'down': face('steel', [5, 0, 11, 1])}),
        ],
    }


def files():
    return {'models/block/hall_feed/monitor.json': monitor()}


def main():
    check = argparse.ArgumentParser()
    check.add_argument('--check', action='store_true')
    check = check.parse_args().check
    images, data = textures(), files()
    for obj in data.values():
        for tex in obj['textures'].values():
            assert tex.split('hall_feed/')[1] in images, tex
    # The glass is where the feed is laid; it must be well darker than the bezel round it, so an unlit screen reads as off.
    px = images['front'].load()
    glass = [sum(px[x, y][:3]) for y in range(4, 10) for x in range(4, 12)]
    bezel = [sum(px[x, y][:3]) for y in range(3, 13) for x in range(2, 14) if not (4 <= x < 12 and 4 <= y < 10)]
    assert max(glass) < sum(bezel) / len(bezel) - 40, (max(glass), sum(bezel) / len(bezel))
    for name, im in images.items():
        path = A / 'textures/block/hall_feed' / (name + '.png')
        if check:
            with Image.open(path) as actual:
                assert actual.convert('RGBA').tobytes() == im.tobytes(), name
        else:
            path.parent.mkdir(parents=True, exist_ok=True); im.save(path, optimize=True)
    for rel, obj in data.items():
        path = A / rel
        if check:
            assert json.loads(path.read_text()) == flicker.settled(obj), rel
        else:
            path.parent.mkdir(parents=True, exist_ok=True); path.write_text(flicker.dumps(obj, indent=2) + '\n')
    print(('Verified' if check else 'Drew') + f' the feed: {len(images)} textures, {len(data)} model.')


if __name__ == '__main__':
    main()
