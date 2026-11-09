"""Borrasca: the Shiny Gentleman, and the machine he is.

0.4.236 drew him as one flat figure among the pines. 0.4.434 (the owner: "Ensure the shiny gentleman still appears as an actual monster out
by the far marker too") gave him a body of boxes. 0.4.439 (the owner: "Well the orrery needs to be rebuilt too. Maybe redesign them both"):
in the story the Shiny Gentleman is the machine at the plant, "a large silver, conically shaped machine" whose whirling lit up the valley.
The machine is now built as that cone, with polished rings, arms and spheres turning round it at different speeds (client.BorrascaOrrery),
and he is built from the same parts. He keeps his bent hat and the faceless oval with its one bright band, on a neck like a stalk; under it
a bar of shoulders too wide, with a ball at each end; a spindle with collars for a body, two tilted hoops turning round it, each with a bead
riding it; from the waist a stepped cone like the machine's, banded, to a toothed hem; legs a pixel wide with a bead at each knee; arms of
rods with a ball at each joint, ending in long hooked fingers.

One atlas holds both, in native Minecraft pixels (16 to a block), from a fixed seed. His boxes are shaded as before: lit hard from the
viewer's left and dark on the right. The machine's materials are tiles the renderer lays over its cone a block at a time (its own normals
shade them): a plain panel, a riveted band, and a band of vents that glow while it runs; a polished sphere; a tooth; the dark of its throat.

Writes textures/entity/borrasca_gentleman.png (128 x 64). --check verifies it.
"""
from pathlib import Path
import argparse
import math
import random
from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / 'src/main/resources/assets/the_oldest_house/textures/entity/borrasca_gentleman.png'
W, H = 128, 64

DARK = (34, 38, 46)
MID = (86, 95, 108)
LIGHT = (160, 171, 186)
SHINE = (224, 232, 242)
SPARK = (255, 255, 255)
GLOW = (236, 186, 128)
THROAT = (14, 9, 9)
GORE = (64, 16, 13)

# Each box's place on the atlas and its size in pixels: name, u, v, width, height, depth. They must match the boxes in
# client.BorrascaGentlemanModel (the standard box unwrap: the two caps across the top row, then the sides in a row below them).
PARTS = [
    ('crown', 0, 0, 5, 6, 4),
    ('head', 18, 0, 5, 8, 4),
    ('brim', 36, 0, 9, 1, 5),
    ('tip', 36, 6, 3, 3, 3),
    ('chin', 48, 6, 3, 1, 3),
    ('hem', 0, 12, 13, 5, 13),
    ('hips', 52, 12, 9, 5, 9),
    ('waist', 88, 12, 5, 5, 5),
    ('spindle', 108, 12, 2, 15, 2),
    ('ball', 116, 12, 3, 3, 3),
    ('bead', 116, 18, 2, 2, 2),
    ('hoop', 116, 22, 4, 1, 1),
    ('collar', 88, 22, 4, 1, 4),
    ('yoke', 52, 26, 13, 1, 1),
    ('neck', 0, 30, 1, 4, 1),
    ('leg', 4, 30, 1, 17, 1),
    ('foot', 8, 30, 2, 1, 2),
    ('upper', 16, 30, 1, 11, 1),
    ('fore', 20, 30, 1, 14, 1),
    ('finger', 24, 30, 1, 8, 1),
    # The machine's arms and hoops, a block of rod at a time.
    ('rod', 104, 30, 3, 16, 3),
]
# The machine's tiles: name, u, v, width, height. client.BorrascaOrrery reads the same places.
TILES = [
    ('panel', 32, 30, 24, 16),
    ('rivets', 56, 30, 24, 16),
    ('vents', 80, 30, 24, 16),
    ('sphere', 32, 46, 16, 16),
    ('tooth', 48, 46, 4, 8),
    ('throat', 52, 46, 8, 8),
]
CONE = {'hem', 'hips', 'waist'}
HAT = {'brim', 'crown', 'tip'}
POLISHED = {'spindle', 'collar', 'yoke', 'hoop', 'rod'}
BALLS = {'ball', 'bead'}


def faces(u, v, w, h, d):
    """The regions of one box: (face, x0, y0, width, height)."""
    return [('cap0', u + d, v, w, d), ('cap1', u + d + w, v, w, d),
            ('left', u, v + d, d, h), ('front', u + d, v + d, w, h),
            ('right', u + d + w, v + d, d, h), ('back', u + d + w + d, v + d, w, h)]


def shade(face, t, name):
    """The colour across a face, t from its viewer's-left edge (0) to its right edge (1): lit hard from the left, dark on the right."""
    if face == 'left':
        return SHINE if t < .5 else LIGHT
    if face == 'right':
        return DARK
    if face == 'back':
        return DARK if t > .3 else MID
    if face in ('cap0', 'cap1'):
        return LIGHT if name in HAT or name in CONE or name in POLISHED else MID
    # The front: a hard highlight at the left edge, falling away to dark on the right.
    return SHINE if t < .15 else LIGHT if t < .45 else MID if t < .75 else DARK


def tile(px, rng, name, u, v, w, h):
    """One of the machine's tiles."""
    for y in range(h):
        for x in range(w):
            c = None
            if name == 'panel':
                # Polished plate: a bright rim along the top, a dark seam along the foot, joints every twelve pixels, a little wear.
                c = SHINE if y == 0 else DARK if y == h - 1 else MID if y == h - 2 or x % 12 == 0 else LIGHT
                if c == LIGHT and rng.random() < .1:
                    c = MID
            elif name == 'rivets':
                # A band round the cone, riveted.
                c = SHINE if y == 0 else DARK if y == h - 1 else MID if 6 <= y <= 9 else LIGHT
                if y == 7 and x % 4 == 1:
                    c = SPARK
                if y == 8 and x % 4 == 1:
                    c = DARK
            elif name == 'vents':
                # Three rows of slits between plates; through them, the light it makes inside.
                c = MID if y in (0, h - 1) else LIGHT
                if y in (3, 4, 8, 9, 12, 13):
                    c = GLOW if 1 <= x % 6 <= 4 else DARK
                    if y in (4, 9, 13) and 1 <= x % 6 <= 4:
                        c = tuple(int(a * .8) for a in GLOW)
            elif name == 'sphere':
                # A polished ball, its highlight to the upper left.
                d = math.hypot(x - 5, y - 5)
                e = math.hypot(x - 7.5, y - 7.5)
                c = SPARK if d < 1.5 else SHINE if d < 3.5 else LIGHT if d < 7 else MID if e < 7.6 else DARK
            elif name == 'tooth':
                # Pale steel, sharpest at the point (the bottom rows narrow to it).
                c = (SHINE, LIGHT, MID, DARK)[x]
                if (y == h - 2 and x in (0, 3)) or (y == h - 1 and x != 1):
                    px[u + x, v + y] = (0, 0, 0, 0)
                    continue
            elif name == 'throat':
                c = GORE if rng.random() < .18 else THROAT
            px[u + x, v + y] = c + (255,)


def draw():
    rng = random.Random(439)
    im = Image.new('RGBA', (W, H), (0, 0, 0, 0))
    px = im.load()
    for name, u, v, w, h, d in PARTS:
        for face, x0, y0, fw, fh in faces(u, v, w, h, d):
            for dy in range(fh):
                for dx in range(fw):
                    t = dx / max(1, fw - 1)
                    c = shade(face, t, name)
                    # The hat is darker than the rest of him, lit only along its edges.
                    if name in HAT and face in ('front', 'back') and 0 < dy < fh - 1:
                        c = MID if c in (SHINE, LIGHT) else DARK
                    # The cone from his waist is banded like the machine's: a bright rim, a dark seam.
                    if name in CONE and face in ('front', 'back', 'left', 'right'):
                        if dy == 0:
                            c = SHINE if face != 'right' else MID
                        elif dy == fh - 1:
                            c = DARK
                    # The balls shine at one corner.
                    if name in BALLS and face in ('front', 'left') and dx == 0 and dy == 0:
                        c = SPARK
                    px[x0 + dx, y0 + dy] = c + (255,)
        # The hem is toothed: the last row of each side is cut into points.
        if name == 'hem':
            for face, x0, y0, fw, fh in faces(u, v, w, h, d):
                if face in ('front', 'back', 'left', 'right'):
                    for dx in range(fw):
                        if dx % 2:
                            px[x0 + dx, y0 + fh - 1] = (0, 0, 0, 0)
                        else:
                            px[x0 + dx, y0 + fh - 1] = SHINE + (255,)
    # The face is a polished blank; one bright band crosses it, as if it had turned to the light.
    _, hu, hv, hw, hh, hd = next(p for p in PARTS if p[0] == 'head')
    fx, fy = hu + hd, hv + hd
    for dx in range(hw):
        for dy in range(hh):
            px[fx + dx, fy + dy] = (LIGHT if dx < 2 else MID if dx < 4 else DARK) + (255,)
        px[fx + dx, fy + 3] = SHINE + (255,)
        px[fx + dx, fy + 4] = (LIGHT if dx < 4 else MID) + (255,)
    # The fingertips catch the light, like hooks.
    _, gu, gv, gw, gh, gd = next(p for p in PARTS if p[0] == 'finger')
    for face, x0, y0, fw, fh in faces(gu, gv, gw, gh, gd):
        if face in ('front', 'left'):
            px[x0, y0 + fh - 1] = SPARK + (255,)
    for name, u, v, w, h in TILES:
        tile(px, rng, name, u, v, w, h)
    return im


def regions():
    """Every painted pixel's owner, asserting that no two overlap and all fit."""
    seen = {}
    for name, u, v, w, h, d in PARTS:
        assert u + 2 * (w + d) <= W and v + d + h <= H, name
        for face, x0, y0, fw, fh in faces(u, v, w, h, d):
            for x in range(x0, x0 + fw):
                for y in range(y0, y0 + fh):
                    assert (x, y) not in seen, (name, face, seen.get((x, y)))
                    seen[(x, y)] = name
    for name, u, v, w, h in TILES:
        assert u + w <= W and v + h <= H, name
        for x in range(u, u + w):
            for y in range(v, v + h):
                assert (x, y) not in seen, (name, seen.get((x, y)))
                seen[(x, y)] = name
    return seen


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--check', action='store_true')
    check = parser.parse_args().check
    im = draw()
    regions()
    _, hu, hv, hw, hh, hd = next(p for p in PARTS if p[0] == 'head')
    assert im.getpixel((hu + hd + 2, hv + hd + 3))[:3] == SHINE, 'the band across the face'
    _, vu, vv, vw, vh = next(t for t in TILES if t[0] == 'vents')
    assert im.getpixel((vu + 2, vv + 3))[:3] == GLOW, 'the vents glow'
    if check:
        with Image.open(OUT) as actual:
            assert actual.size == (W, H), actual.size
            assert actual.convert('RGBA').tobytes() == im.tobytes(), OUT
        print('borrasca_gentleman.png matches')
    else:
        OUT.parent.mkdir(parents=True, exist_ok=True)
        im.save(OUT, optimize=True)
        print('wrote', OUT)


if __name__ == '__main__':
    main()
