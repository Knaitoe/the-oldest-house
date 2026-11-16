"""A sort of thing, part 7 (0.4.647): the Knight's horse (client/KingKnightRenderer), sixteen pixels to the block.

A pale, dappled old horse with a dark mane and tail, its head down a little, a saddle on a cloth, and the box the Knight keeps a cake
in, strapped to the saddle's off side. Each box's faces are laid out as the game unwraps a box; the boxes and where each starts on the
sheet are BOXES here and are the same numbers the model is built from.

Run from tools/ or the repository root. With --check it compares what it would draw with what is committed and writes nothing.
"""
import random
import sys
from pathlib import Path

from PIL import Image

OUT = Path(__file__).resolve().parents[1] / 'src/main/resources/assets/the_oldest_house/textures/entity/literary_king_horse.png'
SIZE = (64, 64)
# name: (u, v, width, height, depth), as KingKnightRenderer.horseLayer has them.
BOXES = {
    'barrel': (0, 0, 10, 10, 22),
    'leg': (0, 32, 4, 12, 4),
    'neck': (16, 32, 4, 9, 6),
    'tail': (36, 32, 3, 12, 3),
    'box': (48, 32, 3, 4, 3),
    'ear': (48, 40, 1, 2, 1),
    'head': (0, 48, 5, 5, 8),
    'saddle': (26, 48, 10, 1, 8),
}
COAT, COAT_LIGHT, DAPPLE = (196, 196, 188), (222, 222, 214), (168, 168, 162)
HAIR, HAIR_LIGHT, HOOF, MUZZLE, EYE = (78, 74, 70), (104, 100, 94), (52, 46, 40), (120, 112, 108), (24, 22, 22)
LEATHER, LEATHER_DARK, CLOTH, CLOTH_EDGE, WOOD, BRASS = (112, 74, 44), (82, 52, 30), (64, 70, 92), (196, 184, 140), (146, 112, 72), (196, 164, 84)


def faces(u, v, w, h, d):
    """The six faces of a box on the sheet: name -> (x, y, width, height)."""
    return {'top': (u + d, v, w, d), 'bottom': (u + d + w, v, w, d), 'west': (u, v + d, d, h), 'north': (u + d, v + d, w, h),
            'east': (u + d + w, v + d, d, h), 'south': (u + d + w + d, v + d, w, h)}


def draw():
    rng = random.Random(6470)
    im = Image.new('RGBA', SIZE, (0, 0, 0, 0))
    px = im.load()

    def put(x, y, c):
        px[x, y] = c + (255,)

    def fill(box, base, light, share, dapple=0.0):
        for x0, y0, w, h in faces(*BOXES[box]).values():
            for x in range(x0, x0 + w):
                for y in range(y0, y0 + h):
                    r = rng.random()
                    put(x, y, DAPPLE if r < dapple else light if r < dapple + share else base)

    def face(box, name):
        return faces(*BOXES[box])[name]

    fill('barrel', COAT, COAT_LIGHT, .1, .14)
    fill('leg', COAT, COAT_LIGHT, .08, .1)
    fill('neck', COAT, COAT_LIGHT, .1, .12)
    fill('head', COAT, COAT_LIGHT, .08, .06)
    fill('tail', HAIR, HAIR_LIGHT, .25)
    fill('ear', COAT, DAPPLE, .3)
    fill('saddle', LEATHER, LEATHER_DARK, .12)
    fill('box', WOOD, LEATHER_DARK, .1)
    # The belly is a little greyer, and the legs darken toward the hooves.
    x0, y0, w, h = face('barrel', 'bottom')
    for x in range(x0, x0 + w):
        for y in range(y0, y0 + h):
            put(x, y, DAPPLE)
    for name in ('west', 'north', 'east', 'south'):
        x0, y0, w, h = face('leg', name)
        for x in range(x0, x0 + w):
            for y in range(y0 + h - 5, y0 + h - 2):
                put(x, y, DAPPLE)
            for y in range(y0 + h - 2, y0 + h):
                put(x, y, HOOF)
    x0, y0, w, h = face('leg', 'bottom')
    for x in range(x0, x0 + w):
        for y in range(y0, y0 + h):
            put(x, y, HOOF)
    # The mane: down the back of the neck and over its top, a forelock on the head's top.
    for name in ('south', 'top'):
        x0, y0, w, h = face('neck', name)
        for x in range(x0 + (1 if name == 'south' else 0), x0 + w - (1 if name == 'south' else 0)):
            for y in range(y0, y0 + h):
                put(x, y, HAIR if (x + y) % 3 else HAIR_LIGHT)
    x0, y0, w, h = face('head', 'top')
    for x in range(x0 + 1, x0 + w - 1):
        for y in range(y0 + h - 2, y0 + h):
            put(x, y, HAIR)
    # The head: its front end is the muzzle (darker, two nostrils); an eye on each side near the back, and a bridle's strap.
    x0, y0, w, h = face('head', 'north')
    for x in range(x0, x0 + w):
        for y in range(y0, y0 + h):
            put(x, y, MUZZLE)
    put(x0 + 1, y0 + 2, EYE)
    put(x0 + w - 2, y0 + 2, EYE)
    for name in ('west', 'east'):
        x0, y0, w, h = face('head', name)
        # The muzzle end of a side face is the end nearest the front face: on the west face that is its right edge.
        front_at_right = name == 'west'
        for k in range(3):
            x = x0 + w - 1 - k if front_at_right else x0 + k
            for y in range(y0 + 1, y0 + h):
                put(x, y, MUZZLE)
        eye_x = x0 + 2 if front_at_right else x0 + w - 3
        put(eye_x, y0 + 1, EYE)
        put(eye_x, y0 + 2, (40, 36, 34))
        strap_x = x0 + w - 4 if front_at_right else x0 + 3
        for y in range(y0, y0 + h):
            put(strap_x, y, LEATHER_DARK)
    # The saddle: a cloth under it, its edge piped; a girth strap round the barrel under the saddle.
    for name, (x0, y0, w, h) in faces(*BOXES['saddle']).items():
        for x in range(x0, x0 + w):
            for y in range(y0, y0 + h):
                edge = x in (x0, x0 + w - 1) or (name in ('top', 'bottom') and y in (y0, y0 + h - 1))
                if name == 'top':
                    put(x, y, LEATHER_DARK if edge else LEATHER)
                elif name != 'bottom':
                    put(x, y, CLOTH_EDGE if edge else CLOTH)
    girth_z = 11  # the barrel's sides run front to back across its depth: the strap crosses them in the middle
    for name in ('west', 'east'):
        x0, y0, w, h = face('barrel', name)
        for y in range(y0, y0 + h):
            for x in (x0 + girth_z - 1, x0 + girth_z):
                put(x, y, LEATHER_DARK)
    x0, y0, w, h = face('barrel', 'bottom')
    for x in range(x0, x0 + w):
        for y in (y0 + girth_z - 1, y0 + girth_z):
            put(x, y, LEATHER_DARK)
    # The cake box: plain boards, a brass catch on its lid and a strap.
    for name, (x0, y0, w, h) in faces(*BOXES['box']).items():
        for x in range(x0, x0 + w):
            for y in range(y0, y0 + h):
                if x == x0 + w // 2:
                    put(x, y, LEATHER_DARK)
    x0, y0, w, h = face('box', 'east')
    put(x0 + w // 2, y0 + 1, BRASS)
    return im


def main():
    im = draw()
    if '--check' in sys.argv:
        with Image.open(OUT) as saved:
            assert saved.size == im.size and saved.convert('RGBA').tobytes() == im.tobytes(), OUT.name
        print('Verified the Knight\'s horse: 8 boxes.')
    else:
        im.save(OUT, optimize=True)
        print('Drew the Knight\'s horse: 8 boxes.')


if __name__ == '__main__':
    main()
