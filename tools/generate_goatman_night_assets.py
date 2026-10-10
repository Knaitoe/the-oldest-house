#!/usr/bin/env python3
"""The Goatman's night (0.4.53): the tally counter, the brat, copper motes, the thing itself, and four cues.

No samples, recordings or words: every cue is synthesized, mono and subtitled. The Goatman's skin follows
the exact box UVs of GoatmanFigureModel (64x64), with a separate emissive layer for its eyes.
Run from the repository root:
    python3 tools/generate_goatman_night_assets.py
"""
from pathlib import Path
import json, random
import numpy as np
from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
A = ROOT / 'src/main/resources/assets/the_oldest_house'
MANIFEST = ROOT / 'art/inventory/cast_0465.json'
APPROVED = {entry['path'] for entry in json.loads(MANIFEST.read_text())['approved_textures']} if MANIFEST.exists() else set()
RATE = 22050


def save(path, image):
    out = A / path
    if out.relative_to(ROOT).as_posix() in APPROVED:
        if not out.is_file():
            raise FileNotFoundError(f'Restore the approved texture from git: {path}')
        return
    out.parent.mkdir(parents=True, exist_ok=True)
    image.save(out)


def shade(color, amount):
    return tuple(max(0, min(255, int(c + amount))) for c in color[:3]) + ((color[3],) if len(color) > 3 else (255,))


# ---------------------------------------------------------------------------------------------------- items
def counter():
    """A chrome hand tally counter: the finger ring, the round body, the plunger and its little black window."""
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    edge = (52, 54, 60, 255)
    # The finger ring, below and left of the body.
    for x in range(16):
        for y in range(16):
            d = ((x - 3.5) ** 2 + (y - 12.5) ** 2) ** .5
            if 1.6 <= d <= 2.7:
                im.putpixel((x, y), (150, 154, 160, 255) if x + y < 16 else (104, 108, 114, 255))
    # The body: a disc lit from the upper left.
    for x in range(16):
        for y in range(16):
            d = ((x - 9.5) ** 2 + (y - 7.5) ** 2) ** .5
            if d <= 5.6:
                light = 1 - ((x - 6.5) + (y - 4.5)) / 14
                c = int(118 + 118 * max(0, min(1, light)))
                im.putpixel((x, y), (c, c + 2, c + 6, 255))
            elif d <= 6.4:
                im.putpixel((x, y), edge)
    # A bright rim highlight and a darker lower rim.
    for x, y in ((7, 3), (8, 2), (9, 2), (6, 4), (5, 5)):
        im.putpixel((x, y), (246, 248, 252, 255))
    for x, y in ((13, 11), (12, 12), (11, 12), (14, 10)):
        im.putpixel((x, y), (92, 94, 100, 255))
    # The plunger on top, seated in the rim, its cap darker.
    for x in (9, 10):
        im.putpixel((x, 1), (206, 208, 214, 255) if x == 9 else (160, 162, 168, 255))
        im.putpixel((x, 0), (96, 98, 104, 255))
    im.putpixel((8, 0), edge); im.putpixel((11, 0), edge)
    # The window: four digits, white on black, the last one just turned.
    for x in range(6, 14):
        for y in range(6, 9):
            im.putpixel((x, y), (18, 18, 20, 255))
    for x in (7, 9, 11):
        im.putpixel((x, 7), (226, 224, 214, 255))
    im.putpixel((12, 7), (232, 120, 96, 255))
    # The reset knob, and the seam where the ring meets the body.
    im.putpixel((15, 8), (120, 122, 128, 255))
    im.putpixel((5, 10), edge)
    save('textures/item/tally_counter.png', im)


def brat():
    """One grilled bratwurst, lying corner to corner, with its grill marks."""
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    a, b = np.array([3.0, 12.0]), np.array([12.5, 3.0])
    axis = (b - a) / np.linalg.norm(b - a)
    normal = np.array([axis[1], -axis[0]])
    for x in range(16):
        for y in range(16):
            p = np.array([x + .5, y + .5])
            t = np.clip(np.dot(p - a, axis), 0, np.linalg.norm(b - a))
            d = np.linalg.norm(p - (a + axis * t))
            if d > 2.6:
                continue
            across = np.dot(p - (a + axis * t), normal)
            base = (156, 86, 48)
            if d > 1.9:
                c = (84, 40, 22)
            elif across < -.8:
                c = shade(base, 46)
            elif across > .9:
                c = shade(base, -34)
            else:
                c = base
            # Grill marks run across the sausage at even intervals.
            if d <= 1.9 and int(t * 1.05) % 3 == 1:
                c = (92, 44, 24)
            im.putpixel((x, y), tuple(c[:3]) + (255,))
    im.putpixel((5, 9), (214, 150, 104, 255)); im.putpixel((9, 5), (214, 150, 104, 255))
    save('textures/item/goatman_brat.png', im)


def models():
    for name in ('tally_counter', 'goatman_brat'):
        p = A / 'models/item' / f'{name}.json'
        p.write_text(json.dumps({'parent': 'minecraft:item/generated', 'textures': {'layer0': f'the_oldest_house:item/{name}'}}, indent=2) + '\n')


def motes():
    """Three copper motes: what the smell looks like, drifting where it is strongest."""
    shapes = [[(3, 3), (4, 3), (3, 4), (4, 4)], [(3, 2), (2, 3), (3, 3), (4, 3), (3, 4)], [(4, 2), (4, 3), (3, 4)]]
    tones = [(198, 116, 70), (164, 84, 50), (224, 152, 98)]
    for i, (shape, tone) in enumerate(zip(shapes, tones)):
        im = Image.new('RGBA', (8, 8), (0, 0, 0, 0))
        for k, (x, y) in enumerate(shape):
            im.putpixel((x, y), shade(tone, -14 * (k % 2))[:3] + (230,))
        save(f'textures/particle/goatman_copper_{i}.png', im)
    p = A / 'particles/goatman_copper.json'
    p.parent.mkdir(parents=True, exist_ok=True)
    p.write_text(json.dumps({'textures': [f'the_oldest_house:goatman_copper_{i}' for i in range(3)]}, indent=2) + '\n')


# ---------------------------------------------------------------------------------------------------- the thing
# Box UVs exactly as GoatmanFigureModel declares them: (u, v, width, height, depth).
BOXES = {
    'head': (0, 0, 7, 7, 8), 'muzzle': (30, 0, 4, 4, 5), 'horn': (48, 0, 2, 4, 2), 'tip': (56, 0, 2, 3, 2),
    'ear': (48, 6, 3, 1, 2), 'beard': (58, 6, 2, 4, 1), 'body': (0, 16, 7, 13, 3),
    'right_arm': (20, 16, 3, 15, 3), 'left_arm': (32, 16, 3, 15, 3), 'right_leg': (0, 34, 3, 14, 3), 'left_leg': (12, 34, 3, 14, 3),
}


def faces(box):
    """Native cube UV faces: name -> (x0, y0, x1, y1) inclusive."""
    u, v, w, h, d = BOXES[box]
    return {'up': (u + d, v, u + d + w - 1, v + d - 1), 'down': (u + d + w, v, u + d + 2 * w - 1, v + d - 1),
            'right': (u, v + d, u + d - 1, v + d + h - 1), 'front': (u + d, v + d, u + d + w - 1, v + d + h - 1),
            'left': (u + d + w, v + d, u + 2 * d + w - 1, v + d + h - 1), 'back': (u + 2 * d + w, v + d, u + 2 * d + 2 * w - 1, v + d + h - 1)}


def goatman():
    rng = random.Random(1253)
    im = Image.new('RGBA', (64, 64), (0, 0, 0, 0))
    eyes = Image.new('RGBA', (64, 64), (0, 0, 0, 0))

    def fill(box, base, grain=10, which=None):
        for name, (x0, y0, x1, y1) in faces(box).items():
            if which and name not in which:
                continue
            for x in range(x0, x1 + 1):
                for y in range(y0, y1 + 1):
                    im.putpixel((x, y), shade(base, rng.randint(-grain, grain)))

    def put(x, y, color):
        im.putpixel((x, y), tuple(color[:3]) + (255,))

    fur, dark_fur, skin, hoof, horn = (172, 162, 144), (112, 100, 86), (122, 114, 108), (36, 32, 30), (66, 58, 50)
    # The head: pale matted fur, darker down the face and over the crown.
    fill('head', fur, 14)
    f = faces('head')
    x0, y0, x1, y1 = f['front']
    for y in range(y0, y1 + 1):
        put(x0 + 3, y, shade(dark_fur, rng.randint(-6, 6)))
    for x in range(f['up'][0], f['up'][2] + 1):
        for y in range(f['up'][1], f['up'][3] + 1):
            if rng.random() < .35:
                put(x, y, shade(dark_fur, rng.randint(-8, 8)))
    # Eyes on the sides of the skull, at its front edge: amber, with a flat black bar for a pupil.
    amber, glow = (214, 160, 44), (255, 196, 70, 255)
    for side in ('right', 'left'):
        sx0, sy0, sx1, sy1 = f[side]
        cols = (sx1 - 2, sx1 - 1, sx1) if side == 'right' else (sx0, sx0 + 1, sx0 + 2)
        for x in cols:
            put(x, sy0 + 2, amber); eyes.putpixel((x, sy0 + 2), glow)
            put(x, sy0 + 3, amber if x == cols[0] or x == cols[2] else (14, 12, 10)); eyes.putpixel((x, sy0 + 3), glow if x in (cols[0], cols[2]) else (0, 0, 0, 0))
        for x in cols:
            put(x, sy0 + 1, shade(dark_fur, -20))
    # From the front the same eyes show either side of the muzzle, above it.
    for x in (x0, x1):
        put(x, y0 + 2, amber); eyes.putpixel((x, y0 + 2), glow)
        put(x, y0 + 1, shade(dark_fur, -20))
    # The muzzle: darker, a wet black nose, a mouth line, and the beard beneath it.
    fill('muzzle', (138, 126, 110), 10)
    m = faces('muzzle')
    nx0, ny0, nx1, ny1 = m['front']
    for x in range(nx0, nx1 + 1):
        put(x, ny0, (64, 58, 52)); put(x, ny0 + 1, (40, 36, 34))
    put(nx0 + 1, ny0 + 1, (10, 10, 10)); put(nx1 - 1, ny0 + 1, (10, 10, 10))
    for x in range(nx0, nx1 + 1):
        put(x, ny1, (24, 20, 18))
    for side in ('right', 'left'):
        sx0, sy0, sx1, sy1 = m[side]
        for x in range(sx0, sx1 + 1):
            put(x, sy1, (30, 26, 24))
    fill('beard', (196, 188, 170), 12)
    for x in range(faces('beard')['front'][0], faces('beard')['front'][2] + 1):
        put(x, faces('beard')['front'][3], (150, 142, 126))
    # Horns ridged dark and pale, the tips nearly black.
    for part, base in (('horn', horn), ('tip', (44, 38, 34))):
        fill(part, base, 6)
        for name, (x0, y0, x1, y1) in faces(part).items():
            for y in range(y0, y1 + 1):
                if (y - y0) % 2 == 0:
                    for x in range(x0, x1 + 1):
                        put(x, y, shade(base, 30))
    # Ears: fur outside, raw pink-brown inside.
    fill('ear', fur, 10)
    for x in range(faces('ear')['down'][0], faces('ear')['down'][2] + 1):
        for y in range(faces('ear')['down'][1], faces('ear')['down'][3] + 1):
            put(x, y, (150, 102, 92))
    # The body: gaunt grey skin, ribs, a ridge of spine, matted fur over the shoulders.
    fill('body', skin, 8)
    b = faces('body')
    x0, y0, x1, y1 = b['front']
    for y in range(y0 + 3, y0 + 9, 2):
        for x in range(x0, x1 + 1):
            if x != x0 + 3:
                put(x, y, shade(skin, -26))
    for x in range(x0, x1 + 1):
        put(x, y0, shade(dark_fur, rng.randint(-8, 8))); put(x, y0 + 1, shade(dark_fur, 10) if rng.random() < .6 else shade(skin, -10))
    for x in range(x0, x1 + 1):
        put(x, y1, (46, 40, 36)); put(x, y1 - 1, (58, 50, 44))
    bx0, by0, bx1, by1 = b['back']
    for y in range(by0, by1 + 1):
        put(bx0 + 3, y, shade(skin, -34 if y % 2 else -16))
    for x in range(bx0, bx1 + 1):
        for y in range(by0, by0 + 4):
            if rng.random() < .75:
                put(x, y, shade(dark_fur, rng.randint(-10, 6)))
    for name in ('up', 'right', 'left'):
        x0_, y0_, x1_, y1_ = b[name]
        for x in range(x0_, x1_ + 1):
            put(x, y0_, shade(dark_fur, rng.randint(-8, 8)))
    # Arms: too long, grey, furred above the elbow, the hands dark with long black nails.
    for arm in ('right_arm', 'left_arm'):
        fill(arm, skin, 8)
        for name, (x0, y0, x1, y1) in faces(arm).items():
            if name in ('up', 'down'):
                continue
            for y in range(y0, y0 + 6):
                for x in range(x0, x1 + 1):
                    if rng.random() < .7:
                        put(x, y, shade(dark_fur, rng.randint(-8, 8)))
            for y in range(y1 - 3, y1 + 1):
                for x in range(x0, x1 + 1):
                    put(x, y, (70, 64, 60) if (x - x0) % 2 == 0 else (54, 50, 46))
            for x in range(x0, x1 + 1, 2):
                put(x, y1, (14, 12, 12))
        x0, y0, x1, y1 = faces(arm)['down']
        for x in range(x0, x1 + 1):
            for y in range(y0, y1 + 1):
                put(x, y, (40, 36, 34))
    # Legs: coarse goat fur to the hock, then black hooves.
    for leg in ('right_leg', 'left_leg'):
        fill(leg, dark_fur, 14)
        for name, (x0, y0, x1, y1) in faces(leg).items():
            if name in ('up', 'down'):
                continue
            for y in range(y0, y1 + 1):
                for x in range(x0, x1 + 1):
                    if rng.random() < .3:
                        put(x, y, shade(dark_fur, -28))
            for y in range(y1 - 1, y1 + 1):
                for x in range(x0, x1 + 1):
                    put(x, y, hoof)
            put(x0 + 1, y1, (8, 8, 8))
        x0, y0, x1, y1 = faces(leg)['down']
        for x in range(x0, x1 + 1):
            for y in range(y0, y1 + 1):
                put(x, y, hoof)
    save('textures/entity/goatman.png', im)
    save('textures/entity/goatman_eyes.png', eyes)
    return im


def preview(im):
    """Front and back orthographic sketches of the figure, for review only (not shipped)."""
    out = Image.new('RGBA', (40, 40), (40, 44, 52, 255))
    def blit(box, face, left, top):
        x0, y0, x1, y1 = faces(box)[face]
        out.paste(im.crop((x0, y0, x1 + 1, y1 + 1)), (left, top))
    # Front (left half) and back (right half), feet at row 38.
    for ox, face in ((2, 'front'), (22, 'back')):
        blit('right_leg' if face == 'front' else 'left_leg', face, ox + 4, 24); blit('left_leg' if face == 'front' else 'right_leg', face, ox + 8, 24)
        blit('body', face, ox + 4, 11)
        blit('right_arm' if face == 'front' else 'left_arm', face, ox + 1, 10); blit('left_arm' if face == 'front' else 'right_arm', face, ox + 11, 10)
        blit('head', face, ox + 4, 4)
        if face == 'front':
            blit('muzzle', 'front', ox + 5, 7); blit('beard', 'front', ox + 6, 11)
        blit('horn', face, ox + 4, 1); blit('horn', face, ox + 9, 1)
    return out.resize((320, 320), Image.NEAREST)


# ---------------------------------------------------------------------------------------------------- audio
def seconds(s):
    return np.arange(int(RATE * s)) / RATE


def band(signal, low, high):
    spectrum = np.fft.rfft(signal)
    f = np.fft.rfftfreq(len(signal), 1 / RATE)
    gain = 1 / (1 + (f / high) ** 6) if high else np.ones_like(f)
    if low:
        gain = gain / (1 + (low / np.maximum(f, 1e-3)) ** 4)
    return np.fft.irfft(spectrum * gain, n=len(signal))


def env(t, start, attack, decay):
    age = t - start
    return np.where(age < 0, 0, np.minimum(1, age / max(attack, 1e-4)) * np.exp(-decay * np.maximum(age, 0)))


def write(name, signal, peak=.8, looped=False):
    if not looped:
        n = np.arange(len(signal))
        signal = signal * np.minimum(1, n / (RATE * .004)) * np.minimum(1, n[::-1] / (RATE * .05))
    signal = signal / max(1e-6, np.max(np.abs(signal))) * peak
    out = A / 'sounds/goatman' / (name + '.ogg')
    out.parent.mkdir(parents=True, exist_ok=True)
    import soundfile
    soundfile.write(str(out), signal.astype(np.float32), RATE, format='OGG', subtype='VORBIS')
    return round(len(signal) / RATE, 3)


def click(rng):
    """A tally counter's plunger: a hard press and a lighter release, with a small steel ring."""
    t = seconds(.18)
    out = np.zeros_like(t)
    for start, strength in ((.004, 1.0), (.074, .55)):
        out += strength * band(rng.uniform(-1, 1, len(t)), 1800, 9000) * env(t, start, .0005, 420)
        out += strength * .55 * np.sin(2 * np.pi * 3150 * (t - start)) * env(t, start, .0005, 70)
        out += strength * .3 * np.sin(2 * np.pi * 5230 * (t - start)) * env(t, start, .0005, 110)
    return out


def claw(rng):
    """Half a knock and half a scrape: knuckles, then something dragged down the door's skin."""
    t = seconds(1.6)
    out = np.zeros_like(t)
    for start, strength in ((.04, 1.0), (.95, .7)):
        out += strength * (np.sin(2 * np.pi * 86 * (t - start)) * env(t, start, .003, 24)
                           + .6 * band(rng.uniform(-1, 1, len(t)), 90, 900) * env(t, start, .002, 70))
    for start, length in ((.22, .42), (.5, .32), (1.12, .4)):
        stroke = (t >= start) & (t < start + length)
        k = np.clip((t - start) / length, 0, 1)
        grit = band(rng.uniform(-1, 1, len(t)), 1400, 5200) * (0.55 + .45 * np.sin(2 * np.pi * 37 * t) ** 2)
        out += stroke * grit * np.sin(np.pi * k) * .55
    return out


def crickets(rng):
    """Six seconds of a few crickets, every pattern dividing six so the loop has no seam."""
    t = seconds(6.0)
    out = np.zeros_like(t)
    for freq, period, offset, pulses, gain in ((4380, .5, .03, 3, .5), (4720, .75, .21, 4, .35), (4120, .6, .4, 3, .28), (5050, 1.0, .66, 5, .2)):
        chirp = np.zeros_like(t)
        start = offset
        while start < 6.0:
            for p in range(pulses):
                s = start + p * .028
                chirp += np.where((t >= s) & (t < s + .018), np.sin(np.clip((t - s) / .018, 0, 1) * np.pi), 0)
            start += period
        out += gain * chirp * np.sin(2 * np.pi * freq * t)
    # A faint, even night air under them.
    floor = band(rng.normal(0, 1, len(t)), 200, 1800) * .03
    return out + floor


def keen(rng):
    """Far off in the woods: a woman's scream and a cat's, tangled together, then dropping away."""
    t = seconds(3.4)
    contour = 500 + 260 * np.sin(np.pi * np.clip(t / 2.6, 0, 1)) ** .7 - 80 * t
    vib = 1 + .028 * np.sin(2 * np.pi * 6.3 * t) + .012 * np.sin(2 * np.pi * 13.1 * t)
    phase = 2 * np.pi * np.cumsum(contour * vib) / RATE
    woman = sum(np.sin(phase * h) / h ** 1.2 for h in range(1, 9))
    yowl_f = 360 + 120 * np.sin(2 * np.pi * .9 * t) + 60 * np.sin(2 * np.pi * 3.7 * t)
    cat = sum(np.sin(2 * np.pi * np.cumsum(yowl_f * h) / RATE) / h for h in range(1, 7))
    shape = np.minimum(1, t / .25) * np.where(t < 2.4, 1, np.exp(-(t - 2.4) * 3))
    voice = (woman * .6 + cat * .5 * (0.6 + .4 * np.sin(2 * np.pi * 2.1 * t) ** 2)) * shape
    voice += band(rng.normal(0, 1, len(t)), 600, 3000) * .12 * shape
    # Distance: the highs gone and the woods answering.
    voice = band(voice, 180, 2400)
    tail = np.exp(-np.arange(int(RATE * .9)) / (RATE * .22)) * rng.normal(0, 1, int(RATE * .9))
    wet = np.convolve(voice, tail * .015)[:len(voice)]
    return voice * .7 + wet


CUES = {'click': (click, 'Counter clicks'), 'claw': (claw, 'Clawing at the door'),
        'crickets': (crickets, 'Crickets'), 'keen': (keen, 'Something screams in the woods')}


def audio():
    rng = np.random.default_rng(453)
    return {name: write(name, fn(rng), peak=.6 if name == 'keen' else .8, looped=name == 'crickets') for name, (fn, _) in CUES.items()}


def registry(lengths):
    sounds = json.loads((A / 'sounds.json').read_text())
    lang = json.loads((A / 'lang/en_us.json').read_text())
    for name, (_, subtitle) in CUES.items():
        key = 'goatman.' + name
        sounds[key] = {'sounds': [{'name': 'the_oldest_house:goatman/' + name, 'attenuation_distance': 48 if name == 'keen' else 16}],
                       'subtitle': 'subtitles.the_oldest_house.goatman_' + name}
        lang['subtitles.the_oldest_house.goatman_' + name] = subtitle
    lang.update({'item.the_oldest_house.tally_counter': 'Tally counter',
                 'item.the_oldest_house.goatman_brat': 'Brat',
                 'entity.the_oldest_house.goatman': 'Something in the woods'})
    (A / 'sounds.json').write_text(json.dumps(sounds, indent=2) + '\n')
    (A / 'lang/en_us.json').write_text(json.dumps(lang, indent=2, ensure_ascii=False) + '\n')
    (ROOT / 'art').mkdir(exist_ok=True)
    (ROOT / 'art/goatman_night_audio.json').write_text(json.dumps(lengths, indent=2) + '\n')


if __name__ == '__main__':
    counter(); brat(); models(); motes()
    goatman()
    registry(audio())
    print('Generated the tally counter, the brat, three copper motes, the Goatman skin and eyes, and four cues.')
