"""Borrasca: the Shiny Gentleman, as a child sees him among the pines when the noise comes off the ridge; and the machine at the plant.

0.4.236 drew him as one flat figure. 0.4.434 (the owner: "Ensure the shiny gentleman still appears as an actual monster out by the far
marker too") gives him a body: client.BorrascaGentlemanModel builds him from boxes, and this paints their faces on one atlas in native
Minecraft pixels (16 to a block), from a fixed seed. Four and a half blocks tall and too thin: a tall hat bent over at the crown and grown
into his head under a narrow brim; no face, a smooth polished oval with one bright band across it; a neck like a stalk; shoulders too
wide over a narrow coat that falls to a ragged hem; legs a pixel wide; arms that hang apart from the coat, bend out at the elbow and end
past his knees in long hooked fingers. He shines like wet metal, lit hard from the viewer's left and dark on the right, with streaks
of light sliding down the coat and a few glints at his edges.

0.4.439 (the owner: "the orrery needs to be rebuilt too"; and "the monster is the child's imagination seeing him as an adult is that trauma
made real"): he stays as the child imagined him, and the grown reader sees the same figure at the machine for two seconds. The machine is
rebuilt as a silver cone with arms, balls and hoops turning round it (client.BorrascaOrrery), and its materials share this sheet, on its
right half: a plain panel, a riveted band and a band of vents that glow while it runs (tiles the renderer lays over its cone a block at a
time, its own normals shading them), a rod for its arms and hoops, a polished sphere, a tooth, and the dark of its throat. His half is drawn
first and exactly as before.

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
    ('skirt', 0, 0, 11, 15, 5),
    ('brim', 32, 0, 9, 1, 5),
    ('shoulders', 32, 6, 11, 2, 3),
    ('crown', 32, 11, 5, 6, 4),
    ('tip', 50, 11, 3, 3, 3),
    ('chest', 0, 20, 9, 7, 3),
    ('waist', 24, 21, 7, 8, 3),
    ('head', 44, 21, 5, 8, 4),
    ('chin', 0, 30, 3, 1, 3),
    ('neck', 12, 30, 1, 4, 1),
    ('leg', 16, 30, 1, 17, 1),
    ('foot', 0, 34, 2, 1, 2),
    ('upper', 20, 33, 1, 11, 1),
    ('fore', 24, 33, 1, 14, 1),
    ('finger', 28, 33, 1, 8, 1),
]
# The machine's rod, on the right half, shaded as his boxes are.
MACHINE_PARTS = [
    ('rod', 104, 16, 3, 16, 3),
]
# The machine's tiles: name, u, v, width, height. client.BorrascaOrrery reads the same places.
TILES = [
    ('panel', 64, 0, 24, 16),
    ('rivets', 88, 0, 24, 16),
    ('vents', 64, 16, 24, 16),
    ('sphere', 88, 16, 16, 16),
    ('tooth', 116, 16, 4, 8),
    ('throat', 116, 24, 8, 8),
]
COAT = {'skirt', 'shoulders', 'chest', 'waist'}
HAT = {'brim', 'crown', 'tip'}


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
        return LIGHT if name in HAT or name in COAT else MID
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
    rng = random.Random(434)
    im = Image.new('RGBA', (W, H), (0, 0, 0, 0))
    px = im.load()
    for name, u, v, w, h, d in PARTS:
        for face, x0, y0, fw, fh in faces(u, v, w, h, d):
            for dy in range(fh):
                for dx in range(fw):
                    t = dx / max(1, fw - 1)
                    c = shade(face, t, name)
                    # Light slides down the coat in long diagonal streaks.
                    if name in COAT and face == 'front' and (dx - dy // 2) % 4 == 0 and c in (MID, DARK):
                        c = LIGHT
                    # The hat is darker than the rest of him, lit only along its edges.
                    if name in HAT and face in ('front', 'back') and 0 < dy < fh - 1:
                        c = MID if c in (SHINE, LIGHT) else DARK
                    px[x0 + dx, y0 + dy] = c + (255,)
            # The coat's hem is ragged: its last rows are torn away in places.
            if name == 'skirt' and face in ('front', 'back', 'left', 'right'):
                for dx in range(fw):
                    tear = rng.choice((0, 0, 1, 2, 3))
                    for dy in range(fh - tear, fh):
                        px[x0 + dx, y0 + dy] = (0, 0, 0, 0)
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
    # Glints only at his edges: the brim, a shoulder, an elbow.
    edges = [(x, y) for x in range(W) for y in range(H) if px[x, y][3] and px[x, y][:3] == LIGHT]
    for x, y in rng.sample(sorted(edges), 6):
        px[x, y] = SPARK + (255,)
    # The machine's half, after his, so that nothing of his changes.
    machine = random.Random(439)
    for name, u, v, w, h, d in MACHINE_PARTS:
        for face, x0, y0, fw, fh in faces(u, v, w, h, d):
            for dy in range(fh):
                for dx in range(fw):
                    px[x0 + dx, y0 + dy] = shade(face, dx / max(1, fw - 1), name) + (255,)
    for name, u, v, w, h in TILES:
        tile(px, machine, name, u, v, w, h)
    return im


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--check', action='store_true')
    check = parser.parse_args().check
    im = draw()
    # Every box's faces are painted and none overlap another's.
    seen = {}
    for name, u, v, w, h, d in PARTS + MACHINE_PARTS:
        assert u + 2 * (w + d) <= W and v + d + h <= H, name
        for face, x0, y0, fw, fh in faces(u, v, w, h, d):
            for x in range(x0, x0 + fw):
                for y in range(y0, y0 + fh):
                    assert (x, y) not in seen, (name, face, seen.get((x, y)))
                    seen[(x, y)] = name
    for name, u, v, w, h in TILES:
        assert u >= 64 and u + w <= W and v + h <= H, name
        for x in range(u, u + w):
            for y in range(v, v + h):
                assert (x, y) not in seen, (name, seen.get((x, y)))
                seen[(x, y)] = name
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
