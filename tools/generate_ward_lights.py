#!/usr/bin/env python3
"""The hospital's ceiling lights (0.4.77): a yellowed fluorescent diffuser, its dark off state, and two original sounds.

The diffuser is drawn here as pixel art on the ward's 32-pixel grid (it used to be cut from the image-generated material
atlas); build_whale_hospital_assets.py leaves it alone. The hum is a mains buzz with its harmonics and a slow beat; the
flicker is the click and stutter of a tube catching. Nothing is sampled. Both are subtitled.
Run from the repository root:
    python3 tools/generate_ward_lights.py
"""
import json
from pathlib import Path

import numpy as np
import soundfile
from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
A = ROOT / 'src/main/resources/assets/the_oldest_house'
RATE = 22050


def diffuser(lit):
    """A louvred panel: a pale steel rim, then long ribs across tubes behind ribbed plastic."""
    im = Image.new('RGBA', (32, 32))
    rim, rim_dark = ((206, 202, 186), (150, 146, 132)) if lit else ((150, 148, 140), (104, 102, 96))
    for x in range(32):
        for y in range(32):
            edge = min(x, y, 31 - x, 31 - y)
            if edge == 0:
                c = rim_dark
            elif edge == 1:
                c = rim
            else:
                # Two tubes run the length of the panel behind the ribs; the plastic is brightest over them.
                tube = min(abs(y - 10.5), abs(y - 21.5))
                rib = x % 4 == 0
                if lit:
                    base = (250, 232, 150) if tube < 2 else (240, 214, 124) if tube < 4 else (226, 196, 108)
                    c = tuple(max(0, v - 26) for v in base) if rib else base
                    if (x * 7 + y * 3) % 29 == 0:
                        c = tuple(max(0, v - 10) for v in c)
                else:
                    base = (118, 116, 104) if tube < 2 else (100, 98, 90) if tube < 4 else (88, 86, 80)
                    c = tuple(max(0, v - 18) for v in base) if rib else base
            im.putpixel((x, y), c + (255,))
    # Flies and dust collected in one corner of the plastic.
    for x, y in ((25, 26), (26, 27), (24, 27), (6, 5)):
        p = im.getpixel((x, y))
        im.putpixel((x, y), tuple(max(0, v - 70) for v in p[:3]) + (255,))
    return im


def hum():
    t = np.arange(int(RATE * 3.0)) / RATE
    buzz = sum(a * np.sign(np.sin(2 * np.pi * f * t)) * 0.25 + a * np.sin(2 * np.pi * f * t) for f, a in ((120, .5), (240, .3), (360, .14), (480, .06)))
    beat = 1 + .18 * np.sin(2 * np.pi * .7 * t)
    rng = np.random.default_rng(477)
    hiss = rng.normal(0, .02, t.size)
    out = (buzz * beat + hiss) * .22
    # Loop cleanly: the cycle count is whole at 3 seconds, and a short crossfade covers the hiss.
    fade = int(RATE * .05)
    out[:fade] = out[:fade] * np.linspace(0, 1, fade) + out[-fade:] * np.linspace(1, 0, fade)
    return out[:-fade]


def flicker():
    t = np.arange(int(RATE * .7)) / RATE
    rng = np.random.default_rng(4771)
    out = np.zeros_like(t)
    # Three clicks of the starter, each a burst of buzz that catches and drops.
    for start, length, level in ((.0, .06, .9), (.16, .1, .7), (.38, .22, 1.0)):
        a, b = int(start * RATE), int((start + length) * RATE)
        seg = t[a:b] - start
        tone = np.sign(np.sin(2 * np.pi * 120 * seg)) * .4 + np.sin(2 * np.pi * 240 * seg) * .4
        env = np.exp(-seg * 18) * level
        out[a:b] += (tone + rng.normal(0, .3, seg.size)) * env
        click = int(RATE * .004)
        out[a:a + click] += rng.normal(0, 1, click) * level
    return np.clip(out * .45, -1, 1)


def main():
    for name, lit in (('diffuser', True), ('diffuser_off', False)):
        diffuser(lit).save(A / f'textures/block/ward/{name}.png')
    model = json.loads((A / 'models/block/ward_light.json').read_text())
    model['textures']['diffuser'] = 'the_oldest_house:block/ward/diffuser_off'
    (A / 'models/block/ward_light_off.json').write_text(json.dumps(model, indent=2) + '\n')
    path = A / 'blockstates/institute_fixture.json'
    states = json.loads(path.read_text())
    variants = {}
    for key, entry in states['variants'].items():
        if 'kind=light' in key:
            base = key.split(',lit=')[0]
            variants[base + ',lit=true'] = {**entry, 'model': 'the_oldest_house:block/ward_light'}
            variants[base + ',lit=false'] = {**entry, 'model': 'the_oldest_house:block/ward_light_off'}
        else:
            variants[key] = entry
    path.write_text(json.dumps({'variants': variants}, indent=2) + '\n')
    folder = A / 'sounds/ward'
    folder.mkdir(parents=True, exist_ok=True)
    cues = {'hum': (hum, 'Fluorescent light hums'), 'flicker': (flicker, 'Fluorescent light flickers')}
    sounds = json.loads((A / 'sounds.json').read_text())
    lang = json.loads((A / 'lang/en_us.json').read_text())
    for name, (make, subtitle) in cues.items():
        soundfile.write(str(folder / f'{name}.ogg'), make().astype(np.float32), RATE, format='OGG', subtype='VORBIS')
        sounds[f'ward.{name}'] = {'sounds': [{'name': f'the_oldest_house:ward/{name}', 'attenuation_distance': 10}], 'subtitle': f'subtitles.the_oldest_house.ward.{name}'}
        lang[f'subtitles.the_oldest_house.ward.{name}'] = subtitle
    (A / 'sounds.json').write_text(json.dumps(sounds, indent=2) + '\n')
    (A / 'lang/en_us.json').write_text(json.dumps(lang, indent=2, ensure_ascii=False) + '\n')
    print('Drew the yellowed diffuser and its off state, and the hum and flicker.')


if __name__ == '__main__':
    main()
