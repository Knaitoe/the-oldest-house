"""Borrasca (0.4.236): the Shiny Gentleman, as a child sees him for a moment among the pines when the noise comes off the ridge.

One texture, in native Minecraft pixels (16 to a block), drawn from a fixed seed: a figure four and a half blocks tall and too thin,
in a long coat and a tall hat that has grown into his head. Where the face should be, the head is smooth and polished, with one
bright band across it where light catches. The arms hang past the knees and end in long fingers. Everything about him shines like
wet metal: lit from the left, dark on the right, with bands of reflection down the coat and a few sparks. Writes
textures/entity/borrasca_gentleman.png. --check verifies it.
"""
from pathlib import Path
import argparse
import random
from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / 'src/main/resources/assets/the_oldest_house/textures/entity/borrasca_gentleman.png'
W, H = 32, 72

DARK = (34, 38, 46)
MID = (86, 95, 108)
LIGHT = (160, 171, 186)
SHINE = (224, 232, 242)
SPARK = (255, 255, 255)


def shape():
    """The figure's cells: a set of (x, y). He leans a little, and his hat is crooked."""
    cells = set()

    def run(y, x0, x1):
        for x in range(x0, x1 + 1):
            cells.add((x, y))
    # The hat, too tall and tapering, its crown bent over to one side, grown into the head under a narrow brim.
    run(0, 16, 17)
    run(1, 15, 17)
    for y in range(2, 8):
        run(y, 14, 18)
    run(8, 12, 20)
    # The head: a long smooth oval, no face, narrowing to the chin.
    for y in range(9, 16):
        run(y, 14, 18)
    run(16, 15, 17)
    run(17, 15, 17)
    # The neck, one long thin stalk.
    for y in range(18, 22):
        run(y, 16, 16)
    # Bony shoulders, too wide, and the coat narrow beneath them.
    run(22, 11, 21)
    run(23, 10, 22)
    for y in range(24, 39):
        w = 4 if y < 31 else 3
        run(y, 16 - w, 16 + w)
    # The coat falls and flares a little to a ragged hem.
    hems = [53, 55, 52, 56, 54, 53, 55, 56, 52, 54, 55]
    for i, x in enumerate(range(11, 22)):
        for y in range(39, hems[i] + 1):
            if y >= 39 + abs(16 - x) // 2:
                cells.add((x, y))
    for y in range(39, 41):
        run(y, 13, 19)
    # The arms hang apart from the coat, bend out at the elbow, and reach far past the knees.
    for side, shoulder, elbow in ((-1, 10, 9), (1, 22, 23)):
        for y in range(24, 35):
            cells.add((shoulder, y))
        for y in range(35, 59):
            cells.add((elbow, y))
        # Four long fingers, curled at the ends like hooks.
        for i, length in enumerate((5, 8, 7, 4)):
            x = elbow + side * (i - 1)
            for y in range(59, 59 + length):
                cells.add((x, y))
            cells.add((x + side, 58 + length))
    # The legs, a pixel each, and narrow feet.
    for y in range(55, 71):
        cells.add((14, y))
        cells.add((18, y))
    run(71, 13, 14)
    run(71, 18, 19)
    return cells


def draw():
    cells = shape()
    rng = random.Random(236)
    im = Image.new('RGBA', (W, H), (0, 0, 0, 0))
    px = im.load()
    for y in range(H):
        row = sorted(x for x in range(W) if (x, y) in cells)
        if not row:
            continue
        # Each run of the row is one surface: a hard highlight on the left, a dark core, a thin rim of light on the right.
        runs, start = [], row[0]
        for a, b in zip(row, row[1:] + [None]):
            if b != a + 1:
                runs.append((start, a))
                if b is not None:
                    start = b
        for x0, x1 in runs:
            for x in range(x0, x1 + 1):
                if x1 == x0:
                    c = SHINE if (x + y) % 7 == 0 else LIGHT if (x + y) % 3 else MID
                else:
                    t = (x - x0) / (x1 - x0)
                    c = SHINE if t < 0.12 else LIGHT if t < 0.3 else MID if t < 0.55 else DARK if t < 0.88 else LIGHT
                    # Light slides down him in long diagonal streaks.
                    if y >= 22 and (x - y // 2) % 9 == 0 and c in (MID, DARK):
                        c = LIGHT
                px[x, y] = c + (255,)
    # The face is a polished blank; one bright band crosses it, as if it had turned to the light.
    for x in range(14, 19):
        px[x, 12] = SHINE + (255,)
    for x in range(15, 18):
        px[x, 13] = LIGHT + (255,)
    # The brim and the shoulders catch the light along their tops.
    for x in range(12, 21):
        px[x, 8] = SHINE + (255,) if x < 15 else LIGHT + (255,)
    for x in range(10, 23):
        if (x, 23) in cells and x < 15:
            px[x, 23] = SHINE + (255,)
    # Glints only at his edges: the hat, a shoulder, a finger.
    edges = [c for c in cells if (c[0] - 1, c[1]) not in cells and c[1] < 66]
    for x, y in rng.sample(sorted(edges), 4):
        px[x, y] = SPARK + (255,)
    return im


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--check', action='store_true')
    check = parser.parse_args().check
    im = draw()
    # He must read as a figure from far off: tall, thin and shining.
    alpha = [im.getpixel((x, y))[3] for y in range(H) for x in range(W)]
    filled = sum(1 for a in alpha if a)
    assert 350 < filled < 900, filled
    assert im.getpixel((16, 12))[:3] == SHINE and im.getpixel((11, 30))[3] == 0, 'the face band and the gap between arm and coat'
    if check:
        with Image.open(OUT) as actual:
            assert actual.convert('RGBA').tobytes() == im.tobytes(), OUT
        print('borrasca_gentleman.png matches')
    else:
        OUT.parent.mkdir(parents=True, exist_ok=True)
        im.save(OUT, optimize=True)
        print('wrote', OUT)


if __name__ == '__main__':
    main()
