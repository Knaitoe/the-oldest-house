#!/usr/bin/env python3
"""The Minotaur's own skin (0.4.199): one 128x128 texture laid out box by box for MinotaurModel.

The owner: "Can we also improve the Minotaur's texture?" Until now every box of the Minotaur sampled one of four flat
material swatches (minotaur_materials.png: hide, fur, horn, dark), so it had no face, no muscle and no hooves to speak
of. This draws a proper skin at the model's own pixel scale, one region per box (MinotaurModel.UV holds the same table):
a bull's face with a heavy brow, sunk eye sockets and a pale muzzle; a bare, scarred chest under a shaggy hump; a
rope-belted loincloth; ridged ivory horns darkening to the tips; dark hands with pale claws; split black hooves.
The old swatch sheet is left exactly as it is.

Run from the repository root:
    python3 tools/draw_minotaur.py           # draw
    python3 tools/draw_minotaur.py --check   # verify the file matches
"""
from pathlib import Path
import math
import random
import sys
from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / 'src/main/resources/assets/the_oldest_house/textures/entity/minotaur.png'
SIZE = 128
# name: (u, v, width, height, depth), exactly as MinotaurModel.UV and its boxes declare them.
LAYOUT = {
    'chest': (0, 0, 20, 15, 13), 'hump': (66, 0, 16, 17, 7), 'horn': (112, 0, 4, 9, 4), 'turn': (112, 13, 3, 7, 3),
    'skull': (0, 28, 12, 11, 10), 'brow': (44, 28, 14, 5, 10), 'muzzle': (92, 28, 10, 5, 6),
    'eyes': (44, 43, 3, 1.5, .5), 'nostrils': (52, 43, 2, 1.5, .3), 'jaw': (92, 39, 8, 3, 7),
    'waist': (0, 49, 12, 10, 9), 'upper_arm': (42, 49, 8, 12, 8), 'forearm': (74, 49, 7, 11, 7),
    'tip': (102, 49, 1.5, 6, 1.5), 'finger': (108, 49, 2, 3, 1),
    'hand': (0, 69, 8, 5, 7), 'thigh': (30, 69, 7, 9, 7), 'shin': (58, 69, 5.6, 7, 5), 'hoof': (80, 69, 3.2, 3, 7),
    'mouth': (0, 85, 7.6, .3, 6),
}

HIDE = (94, 66, 47)
FUR_DARK = (50, 34, 26)
FUR_MID = (74, 51, 37)
BARE = (126, 94, 72)
MUZZLE = (142, 116, 96)
NOSE = (52, 40, 36)
HORN_ROOT, HORN_TIP = (224, 210, 176), (60, 50, 40)
HOOF = (34, 28, 26)
CLAW = (206, 196, 176)
MOUTH = (98, 34, 34)
EYE_RIM, EYE_RED = (24, 10, 8), (190, 48, 32)
SCAR = (172, 118, 100)
LEATHER, CLOTH = (56, 42, 32), (82, 70, 58)

rng = random.Random(1990)


def shade(c, amount):
    return tuple(max(0, min(255, round(v + amount))) for v in c[:3]) + (255,)


def mix(a, b, t):
    return tuple(round(x + (y - x) * t) for x, y in zip(a[:3], b[:3]))


def faces(name):
    """Each face of a box as (x0, y0, width, height) in whole texels, covering fractional sizes."""
    u, v, w, h, d = LAYOUT[name]
    c = math.ceil
    return {
        'top': (u + d, v, w, d), 'bottom': (u + d + w, v, w, d),
        'right': (u, v + d, d, h), 'front': (u + d, v + d, w, h),
        'left': (u + d + w, v + d, d, h), 'back': (u + 2 * d + w, v + d, w, h),
    }, c


def paint(im, name, fn):
    """fn(face, x, y, fw, fh) -> colour, for every texel of every face (x, y counted from the face's top left)."""
    boxes, c = faces(name)
    for face, (x0, y0, fw, fh) in boxes.items():
        ix0, iy0 = math.floor(x0), math.floor(y0)
        iw, ih = max(1, c(x0 + fw) - ix0), max(1, c(y0 + fh) - iy0)
        for x in range(iw):
            for y in range(ih):
                px, py = ix0 + x, iy0 + y
                if 0 <= px < SIZE and 0 <= py < SIZE:
                    im.putpixel((px, py), fn(face, x, y, iw, ih))


def fur(base, x, y, streak=10):
    """Shaggy hair: vertical streaks with a little noise, darker every few columns."""
    return shade(base, (-streak if (x * 7 + y // 3) % 5 == 0 else 0) + rng.randint(-6, 6))


def draw():
    im = Image.new('RGBA', (SIZE, SIZE), (0, 0, 0, 0))

    # The chest: bare and scarred in front, the hair thick over the shoulders and down the back.
    def chest(face, x, y, fw, fh):
        if face == 'front':
            if y == 0 or x in (0, fw - 1):
                return fur(FUR_MID, x, y)
            c = BARE
            mid = fw // 2
            if y <= 6 and abs(x - mid) >= 1:
                c = shade(c, 14 - abs(abs(x - mid) - 5) * 3)
            if y == 7 and abs(x - mid) >= 1:
                c = shade(c, -26)
            if x in (mid - 1, mid) and y >= 2:
                c = shade(c, -18)
            if y >= 9 and (y - 9) % 3 == 2 and abs(x - mid) <= 4:
                c = shade(c, -14)
            if y >= fh - 2:
                c = shade(FUR_MID, 0)
            # Three old scars across the chest, one through the belly.
            if (x - y) in (3, 4) and 2 <= y <= 6 and x < mid:
                c = SCAR
            if x + y in (24, 25) and 3 <= y <= 8 and x > mid:
                c = SCAR
            if y == 11 and mid - 3 <= x <= mid + 2:
                c = shade(SCAR, -10)
            return shade(c, rng.randint(-4, 4))
        if face == 'back':
            c = fur(FUR_DARK, x, y, 8)
            return shade(c, 12) if x in (fw // 2 - 1, fw // 2) else c
        if face in ('right', 'left'):
            return fur(FUR_MID if y > 4 else FUR_DARK, x, y)
        if face == 'top':
            return fur(FUR_DARK, x, y)
        return shade(HIDE, rng.randint(-6, 4))
    paint(im, 'chest', chest)

    # The hump: the darkest, shaggiest hair, longest at the top.
    paint(im, 'hump', lambda face, x, y, fw, fh: fur(FUR_DARK, x, y, 12) if face != 'bottom' else shade(HIDE, -10))

    # The waist: a rope belt and a loincloth of dark rag, frayed at the hem.
    def waist(face, x, y, fw, fh):
        if face in ('top', 'bottom'):
            return shade(HIDE, rng.randint(-6, 4))
        if y <= 1:
            return shade(LEATHER, 18 if (x + y) % 3 == 0 else 0)
        if face in ('front', 'back'):
            hem = fh - 1 - (x * 3 % 4 == 0)
            if y >= hem:
                return shade(HIDE, -8 + rng.randint(-4, 4))
            return shade(CLOTH, (-12 if x % 4 == 1 else 0) + rng.randint(-5, 5))
        return shade(HIDE if y > 5 else CLOTH, rng.randint(-6, 6))
    paint(im, 'waist', waist)

    # The skull: a curled tuft on the forehead, a heavy brow, the sockets sunk and dark around the eyes.
    def skull(face, x, y, fw, fh):
        if face == 'front':
            if y <= 3:
                return shade(FUR_MID, (10 if (x * 5 + y * 3) % 7 < 2 else -2) + rng.randint(-4, 4))
            if y == 4:
                return shade(FUR_DARK, -6)
            if y in (5, 6):
                return shade(EYE_RIM, 14) if x in (0, 1, 2, 3, 8, 9, 10, 11) else shade(HIDE, -4)
            return shade(mix(HIDE, MUZZLE, .4), rng.randint(-4, 4))
        if face in ('right', 'left'):
            # A darker patch where the ear would hang.
            ear = 3 <= y <= 6 and (x <= 3 if face == 'right' else x >= fw - 4)
            return shade(FUR_DARK if ear else FUR_MID, rng.randint(-5, 5))
        if face == 'top':
            return fur(FUR_DARK, x, y)
        return shade(HIDE, rng.randint(-6, 6))
    paint(im, 'skull', skull)

    # The poll, where the horns grow from: thick curled hair.
    paint(im, 'brow', lambda face, x, y, fw, fh: shade(FUR_DARK, (9 if (x * 5 + y * 3) % 7 < 2 else 0) + rng.randint(-5, 5)))

    # The muzzle: pale, the nose pad dark and wet-looking, the mouth a black line.
    def muzzle(face, x, y, fw, fh):
        if face == 'front':
            if y <= 1:
                return shade(NOSE, 10 if (x, y) in ((2, 0), (7, 0)) else rng.randint(-3, 3))
            if y == fh - 1:
                return shade(EYE_RIM, 6)
            return shade(MUZZLE, rng.randint(-5, 5))
        if face == 'top':
            return shade(mix(HIDE, MUZZLE, y / max(1, fh - 1)), rng.randint(-4, 4))
        if face in ('right', 'left'):
            return shade(MUZZLE if y < fh - 1 else NOSE, rng.randint(-6, 4) - 8)
        return shade(MUZZLE, -20 + rng.randint(-4, 4))
    paint(im, 'muzzle', muzzle)

    # The eyes: small and red in black rims. The nostrils: black.
    paint(im, 'eyes', lambda face, x, y, fw, fh: shade(EYE_RED, 20) if face == 'front' and x == 1 and y == 0
          else shade(EYE_RED, -30) if face == 'front' and x == 1 else shade(EYE_RIM, 0))
    paint(im, 'nostrils', lambda face, x, y, fw, fh: shade((8, 6, 6), 0))

    # The jaw: a pale chin under a dark lip, the throat bearded underneath; the inside of the mouth red.
    def jaw(face, x, y, fw, fh):
        if face == 'front':
            return shade(NOSE, 0) if y == 0 else shade(MUZZLE, -10 + rng.randint(-4, 4))
        if face == 'bottom':
            return fur(FUR_DARK, x, y, 6)
        if face == 'top':
            return shade(MOUTH, -20)
        return shade(mix(MUZZLE, HIDE, .5), rng.randint(-5, 5))
    paint(im, 'jaw', jaw)
    paint(im, 'mouth', lambda face, x, y, fw, fh: shade(MOUTH, (24 if face == 'top' and x % 3 == 0 else 0) + rng.randint(-4, 4)))

    # The horns: ridged rings, ivory at the root, yellowing, then near black at the points.
    horn_parts = [('horn', 0, .45), ('turn', .45, .8), ('tip', .8, 1.0)]
    for part, start, end in horn_parts:
        def horn(face, x, y, fw, fh, start=start, end=end):
            # Each part grows upward from its root (the bottom rows of the side faces).
            if face in ('top', 'bottom'):
                t = end if face == 'top' else start
                return shade(mix(HORN_ROOT, HORN_TIP, t), -6)
            t = start + (end - start) * (1 - (y + .5) / fh)
            c = mix(HORN_ROOT, HORN_TIP, t)
            return shade(c, (-13 if y % 2 == 1 else 4) + rng.randint(-3, 3))
        paint(im, part, horn)

    # The arms: hair over the shoulders, bare hide on the forearms with a raised vein; dark hands and pale claws.
    paint(im, 'upper_arm', lambda face, x, y, fw, fh: fur(FUR_DARK, x, y) if y <= 3 or face == 'top'
          else shade(HIDE, (10 if face == 'front' and 2 <= x <= 5 and y <= 8 else 0) + rng.randint(-6, 6)))
    paint(im, 'forearm', lambda face, x, y, fw, fh: shade(HIDE, (14 if face == 'front' and x == 3 + (y % 3 == 0) else 0) - (10 if y >= fh - 2 else 0) + rng.randint(-6, 6)))
    paint(im, 'hand', lambda face, x, y, fw, fh: shade((66, 54, 48), (10 if face == 'front' and y == 1 and x % 3 != 2 else 0) + (14 if face == 'bottom' else 0) + rng.randint(-5, 5)))
    paint(im, 'finger', lambda face, x, y, fw, fh: shade(CLAW if face != 'top' and y >= fh - 2 else (60, 48, 42), rng.randint(-4, 4)))

    # The legs: shaggy thighs, a dark shin, black hooves with a pale edge and a split.
    paint(im, 'thigh', lambda face, x, y, fw, fh: fur(FUR_DARK if face in ('back', 'top') else FUR_MID, x, y, 12))
    paint(im, 'shin', lambda face, x, y, fw, fh: shade(FUR_DARK, (10 if y >= fh - 2 else 0) + rng.randint(-5, 5)))
    paint(im, 'hoof', lambda face, x, y, fw, fh: shade(HOOF, (22 if y == 0 and face != 'bottom' else 0) + (-12 if face == 'front' and x == fw - 1 else 0) + rng.randint(-3, 3)))
    return im


def main():
    im = draw()
    if sys.argv[1:] == ['--check']:
        current = Image.open(OUT).convert('RGBA')
        assert current.tobytes() == im.tobytes(), 'minotaur.png differs from tools/draw_minotaur.py'
        print('Verified the Minotaur skin.')
        return
    im.save(OUT)
    print('Drew the Minotaur skin.')


if __name__ == '__main__':
    main()
