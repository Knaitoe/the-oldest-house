#!/usr/bin/env python3
"""The Goatman's curled ram horns (0.4.199): one 32x32 texture for GoatmanHorns' seven segments.

The owner: "Can we also give the Goatman curled ram horns?" A ram's horn is ridged across its length, pale and dusty at
the root, darker and smoother toward the tip. Each segment's box faces are laid out exactly as GoatmanHorns.UV and
SEGMENTS declare them (thickness w, length d): the ridges run across the segment on every face, so they read as rings
around the curl. The left horn mirrors this texture. The Goatman's own skin (goatman.png) is untouched.

Run from the repository root:
    python3 tools/draw_goatman_horns.py           # draw
    python3 tools/draw_goatman_horns.py --check   # verify the file matches
"""
from pathlib import Path
import random
import sys
from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / 'src/main/resources/assets/the_oldest_house/textures/entity/goatman_horns.png'
# (u, v, thickness, length) for each segment, root to tip: GoatmanHorns.UV and SEGMENTS.
SEGMENTS = [(0, 0, 3, 3), (0, 6, 3, 3), (0, 12, 2, 3), (0, 17, 2, 3), (16, 0, 2, 3), (16, 5, 2, 3), (16, 10, 1, 3)]
ROOT_TONE, TIP_TONE = (150, 136, 112), (62, 54, 46)


def lerp(a, b, t):
    return tuple(round(x + (y - x) * t) for x, y in zip(a, b))


def shade(c, amount):
    return tuple(max(0, min(255, v + amount)) for v in c) + (255,)


def draw():
    rng = random.Random(199)
    im = Image.new('RGBA', (32, 32), (0, 0, 0, 0))
    total = sum(s[3] for s in SEGMENTS)
    along = 0
    for index, (u, v, w, d) in enumerate(SEGMENTS):
        # Faces of a box (w wide, w high, d deep) at (u, v). The length runs along d.
        faces = {
            'up': (u + d, v, w, d, 'v'), 'down': (u + d + w, v, w, d, 'v'),
            'right': (u, v + d, d, w, 'u'), 'left': (u + d + w, v + d, d, w, 'u'),
            'front': (u + d, v + d, w, w, None), 'back': (u + 2 * d + w, v + d, w, w, None),
        }
        for name, (x0, y0, fw, fh, axis) in faces.items():
            for x in range(fw):
                for y in range(fh):
                    if axis is None:
                        # The cut ends: the root end paler, the tip end dark, a ring of growth in the middle.
                        t = (along + (0 if name == 'front' else d)) / total
                        c = lerp(ROOT_TONE, TIP_TONE, t)
                        ring = w > 1 and 0 < x < fw - 1 and 0 < y < fh - 1
                        im.putpixel((x0 + x, y0 + y), shade(c, -18 if ring else rng.randint(-6, 4)))
                        continue
                    # How far along the whole horn this texel sits; 'right' runs back to front, so flip it.
                    step = y if axis == 'v' else (d - 1 - x if name == 'right' else x)
                    t = (along + step + .5) / total
                    c = lerp(ROOT_TONE, TIP_TONE, t)
                    ridge = step % 2 == 0
                    # The inner face of the curl (down) is darker; the outer faces catch the light.
                    face = -14 if name == 'down' else 6 if name == 'up' else 0
                    im.putpixel((x0 + x, y0 + y), shade(c, face + (14 if ridge else -8) + rng.randint(-5, 5)))
        along += d
    return im


def main():
    im = draw()
    if sys.argv[1:] == ['--check']:
        current = Image.open(OUT).convert('RGBA')
        assert current.tobytes() == im.tobytes(), 'goatman_horns.png differs from tools/draw_goatman_horns.py'
        print('Verified the Goatman horns texture.')
        return
    OUT.parent.mkdir(parents=True, exist_ok=True)
    im.save(OUT)
    print('Drew the Goatman horns texture.')


if __name__ == '__main__':
    main()
