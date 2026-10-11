"""The Minotaur's charge (0.4.84): its bellow, a hoof on stone, and the charge's damage type.

Writes two original subtitled sounds under sounds/finale/, their sounds.json entries and subtitles, the
the_oldest_house:minotaur_charge damage type (through shields and armour: two hearts exactly) and its death message.
Usage: python3 tools/generate_minotaur_charge_assets.py
"""
from pathlib import Path
import json

import numpy as np
import soundfile

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / 'src/main/resources/assets/the_oldest_house'
DATA = ROOT / 'src/main/resources/data'
RATE = 22050


def t_of(seconds):
    return np.arange(round(RATE * seconds)) / RATE


def noise(seconds, seed):
    return np.random.default_rng(seed).uniform(-1, 1, round(RATE * seconds))


def lowpass(signal, alpha):
    out = np.zeros_like(signal)
    acc = 0.0
    for i, v in enumerate(signal):
        acc += alpha * (v - acc)
        out[i] = acc
    return out


def normalize(signal, peak=.85):
    return signal / max(1e-9, np.max(np.abs(signal))) * peak


def bellow():
    """A bull's bellow from a chest too big for it: a low, rough, rising and falling voice with breath through it."""
    seconds = 2.1
    t = t_of(seconds)
    # The pitch climbs out of a grunt, holds, and breaks downward at the end.
    pitch = 62 + 34 * np.sin(np.clip(t / seconds, 0, 1) * np.pi) ** .7 - 14 * np.clip((t - 1.6) / .5, 0, 1)
    phase = 2 * np.pi * np.cumsum(pitch) / RATE
    jitter = lowpass(noise(seconds, 3), .002) * .6
    voice = sum(np.sin(k * phase + jitter * k) / k ** .85 for k in range(1, 18))
    # A rough, buzzing throat: amplitude modulated at a slow, uneven rate.
    voice *= 1 + .35 * np.sin(2 * np.pi * 23 * t + jitter * 4)
    # Formant-ish colouring: keep the body, damp the top.
    voice = lowpass(voice, .18) * .8 + lowpass(voice, .05) * .6
    breath = lowpass(noise(seconds, 5), .12) * .35
    envelope = np.clip(t / .18, 0, 1) * np.clip((seconds - t) / .45, 0, 1)
    snort = np.zeros_like(t)
    s = round(RATE * .05)
    snort[:s * 4] = lowpass(noise(.2, 7), .25)[:s * 4] * np.exp(-np.arange(s * 4) / (RATE * .06)) * 1.1
    return normalize((voice + breath) * envelope + snort)


def hoof():
    """A cloven hoof driven into stone: a hard click, a low thud through the floor, and grit skittering off."""
    t = t_of(.5)
    click = lowpass(noise(.5, 11), .7) * np.exp(-t * 90) * 1.2
    thud = (np.sin(2 * np.pi * 58 * t) + .5 * np.sin(2 * np.pi * 116 * t)) * np.exp(-t * 14)
    grit = noise(.5, 13) * np.exp(-((t - .08) ** 2) / .004) * .25
    scrape = lowpass(noise(.5, 17), .35) * np.clip((t - .02) / .03, 0, 1) * np.exp(-t * 9) * .45
    return normalize(click + thud + grit + scrape)


SOUNDS = {
    'minotaur_bellow': (bellow, 'The Minotaur bellows', 48),
    'minotaur_hoof': (hoof, 'A hoof strikes stone', 32),
}


def write_json(path, data):
    path.parent.mkdir(parents=True, exist_ok=True)
    original = path.read_text() if path.exists() else '\n'
    text = json.dumps(data, indent=2, ensure_ascii=False)
    if original.endswith('\n'):
        text += '\n'
    path.write_text(text)


def main():
    folder = ASSETS / 'sounds/finale'
    folder.mkdir(parents=True, exist_ok=True)
    table = json.loads((ASSETS / 'sounds.json').read_text())
    lang = json.loads((ASSETS / 'lang/en_us.json').read_text())
    for name, (make, subtitle, reach) in SOUNDS.items():
        soundfile.write(str(folder / f'{name}.ogg'), make().astype(np.float32), RATE, format='OGG', subtype='VORBIS')
        table[f'finale.{name}'] = {'subtitle': f'subtitles.the_oldest_house.finale.{name}',
                                   'sounds': [{'name': f'the_oldest_house:finale/{name}', 'stream': False, 'attenuation_distance': reach}]}
        lang[f'subtitles.the_oldest_house.finale.{name}'] = subtitle
    lang['death.attack.the_oldest_house.minotaur_charge'] = '%1$s was trampled by %2$s'
    lang['death.attack.the_oldest_house.minotaur_charge.item'] = '%1$s was trampled by %2$s'
    write_json(ASSETS / 'sounds.json', table)
    write_json(ASSETS / 'lang/en_us.json', lang)
    write_json(DATA / 'the_oldest_house/damage_type/minotaur_charge.json',
               {'exhaustion': 0.1, 'message_id': 'the_oldest_house.minotaur_charge', 'scaling': 'never'})
    for tag in ('bypasses_armor', 'bypasses_shield'):
        path = DATA / f'minecraft/tags/damage_type/{tag}.json'
        current = json.loads(path.read_text()) if path.exists() else {'replace': False, 'values': []}
        if 'the_oldest_house:minotaur_charge' not in current['values']:
            current['values'].append('the_oldest_house:minotaur_charge')
        write_json(path, current)
    print('wrote the bellow, the hoof and the charge damage type')


if __name__ == '__main__':
    main()
