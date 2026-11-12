"""Bike night's specters, the things they have that are not their skins: the witch's hat and her broom (0.4.499), and the
werewolf's muzzle, ears and tail (0.4.501).

Her skin is painted with the rest of the cast (tools/paint_character_skins.py, literary_hollow_witch). This is the sheet her hat
and broom are drawn from (client/LiteraryRenderers.WitchLayer): each box's faces laid out as the game unwraps a box, sixteen pixels
to the block. The boxes and where each starts on the sheet are BOXES here and are the same numbers the layer is built from.

Run from tools/ or the repository root. With --check it compares what it would draw with what is committed and writes nothing.
"""
import random
import sys
from pathlib import Path

from PIL import Image

OUT = Path(__file__).resolve().parents[1] / 'src/main/resources/assets/the_oldest_house/textures/entity/literary_hollow_witch_things.png'
SIZE = (64, 64)
# name: (u, v, width, height, depth), as the layer's texOffs and addBox have them.
BOXES = {
    'brim': (0, 0, 10, 1, 10),
    'crown': (0, 11, 6, 3, 6),
    'cone': (24, 11, 4, 3, 4),
    'tip': (40, 11, 2, 3, 2),
    'stick': (0, 20, 1, 1, 26),
    'bristles': (0, 47, 5, 5, 7),
}
FELT, FELT_LIGHT, BAND, BUCKLE = (29, 26, 38), (44, 40, 56), (91, 45, 110), (201, 162, 58)
WOOD, WOOD_DARK, STRAW, STRAW_DARK, TWINE = (107, 74, 43), (78, 53, 30), (176, 138, 60), (128, 96, 38), (58, 42, 26)


def faces(u, v, w, h, d):
    """The six faces of a box on the sheet: name -> (x, y, width, height)."""
    return {'top': (u + d, v, w, d), 'bottom': (u + d + w, v, w, d), 'west': (u, v + d, d, h), 'north': (u + d, v + d, w, h),
            'east': (u + d + w, v + d, d, h), 'south': (u + d + w + d, v + d, w, h)}


def draw():
    rng = random.Random(4990)
    im = Image.new('RGBA', SIZE, (0, 0, 0, 0))
    px = im.load()

    def fill(box, base, light, share):
        for x0, y0, w, h in faces(*BOXES[box]).values():
            for x in range(x0, x0 + w):
                for y in range(y0, y0 + h):
                    px[x, y] = (light if rng.random() < share else base) + (255,)

    for box in ('brim', 'crown', 'cone', 'tip'):
        fill(box, FELT, FELT_LIGHT, .14)
    # The band round the crown's foot, with its buckle at the front.
    crown = faces(*BOXES['crown'])
    for side in ('west', 'north', 'east', 'south'):
        x0, y0, w, h = crown[side]
        for x in range(x0, x0 + w):
            px[x, y0 + h - 1] = BAND + (255,)
    x0, y0, w, h = crown['north']
    px[x0 + w // 2 - 1, y0 + h - 1] = BUCKLE + (255,)
    px[x0 + w // 2, y0 + h - 1] = BUCKLE + (255,)
    # The stick: grain along its length.
    fill('stick', WOOD, WOOD_DARK, .22)
    # The bristles: straw running back from the binding, darker at their ends; the binding round their front.
    f = faces(*BOXES['bristles'])
    for name, (x0, y0, w, h) in f.items():
        for x in range(x0, x0 + w):
            for y in range(y0, y0 + h):
                px[x, y] = (STRAW_DARK if rng.random() < .3 else STRAW) + (255,)
    # Depth runs along x on the west and east faces and along y on the top and bottom: the front is the low end of both.
    for name in ('west', 'east'):
        x0, y0, w, h = f[name]
        front = x0 + w - 1 if name == 'west' else x0
        back = x0 if name == 'west' else x0 + w - 1
        for y in range(y0, y0 + h):
            px[front, y] = TWINE + (255,)
            px[back, y] = STRAW_DARK + (255,)
    for name in ('top', 'bottom'):
        x0, y0, w, h = f[name]
        for x in range(x0, x0 + w):
            px[x, y0] = TWINE + (255,)
            px[x, y0 + h - 1] = STRAW_DARK + (255,)
    x0, y0, w, h = f['north']
    for x in range(x0, x0 + w):
        for y in range(y0, y0 + h):
            px[x, y] = TWINE + (255,)
    return im


WOLF_OUT = OUT.with_name('literary_hollow_wolf_things.png')
WOLF_SIZE = (64, 32)
# The werewolf's (client/LiteraryRenderers.WolfLayer): on his head a muzzle with a nose on it and two ears; from his back, a tail.
WOLF_BOXES = {
    'muzzle': (0, 0, 4, 3, 3),
    'nose': (14, 0, 2, 1, 1),
    'ear': (22, 0, 2, 3, 2),
    'tail': (0, 8, 3, 9, 3),
}
FUR, FUR_DARK, FUR_LIGHT, NOSE, EAR = (106, 82, 64), (74, 56, 42), (134, 108, 86), (28, 22, 20), (150, 112, 100)


def draw_wolf():
    rng = random.Random(5010)
    im = Image.new('RGBA', WOLF_SIZE, (0, 0, 0, 0))
    px = im.load()
    for box in ('muzzle', 'ear', 'tail'):
        for x0, y0, w, h in faces(*WOLF_BOXES[box]).values():
            for x in range(x0, x0 + w):
                for y in range(y0, y0 + h):
                    r = rng.random()
                    px[x, y] = (FUR_DARK if r < .28 else FUR_LIGHT if r > .9 else FUR) + (255,)
    for x0, y0, w, h in faces(*WOLF_BOXES['nose']).values():
        for x in range(x0, x0 + w):
            for y in range(y0, y0 + h):
                px[x, y] = NOSE + (255,)
    # The mouth: a dark line along the muzzle's sides and across its front, a row up from the bottom.
    f = faces(*WOLF_BOXES['muzzle'])
    for name in ('west', 'north', 'east'):
        x0, y0, w, h = f[name]
        for x in range(x0, x0 + w):
            px[x, y0 + h - 1] = NOSE + (255,)
    # The ear's inside, on its front.
    x0, y0, w, h = faces(*WOLF_BOXES['ear'])['north']
    for y in range(y0 + 1, y0 + h):
        px[x0 + w // 2, y] = EAR + (255,)
    # The tail's tip, at its far end (the bottom of the box as it hangs), lighter.
    f = faces(*WOLF_BOXES['tail'])
    for name in ('west', 'north', 'east', 'south'):
        x0, y0, w, h = f[name]
        for x in range(x0, x0 + w):
            for y in range(y0 + h - 2, y0 + h):
                px[x, y] = FUR_LIGHT + (255,)
    x0, y0, w, h = f['bottom']
    for x in range(x0, x0 + w):
        for y in range(y0, y0 + h):
            px[x, y] = FUR_LIGHT + (255,)
    return im


def main():
    sheets = ((OUT, draw()), (WOLF_OUT, draw_wolf()))
    for path, im in sheets:
        if '--check' in sys.argv:
            with Image.open(path) as saved:
                assert saved.size == im.size and saved.convert('RGBA').tobytes() == im.tobytes(), path.name
        else:
            im.save(path, optimize=True)
    print(('Verified' if '--check' in sys.argv else 'Drew') + ' the specters\' things: 2 sheets, 10 boxes.')


if __name__ == '__main__':
    main()
