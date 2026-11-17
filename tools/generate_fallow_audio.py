"""Original sounds for Fallow (0.4.672, docs/FALLOW_0_4_672.md): a gas station on a two-lane road through the wheat, late in a dry summer.

The place has only its everyday sounds in this part: the wind going over the wheat, the insects in it, the radio on the back shelf
playing a ball game a long way off, and the bell on the pump. Nothing in them is frightening; what the afternoon becomes comes later.

Synthesized from noise with Movie Night's building blocks; no recordings. Mono, so Minecraft places each one where it happens. Run
from the repository root. Existing files are kept, so the committed sounds only change when deleted first.
"""
import json
import sys
from pathlib import Path

ROWS = {
    'wind': ('Wind goes over the wheat', 48),
    'insects': ('Insects buzz in the wheat', 16),
    'radio': ('A radio plays a ball game', 12),
    'bell': ('The pump\'s bell rings', 24),
}
ASSET_ROOT = Path(__file__).resolve().parents[1] / 'src/main/resources/assets/the_oldest_house'


def check():
    sounds = json.loads((ASSET_ROOT / 'sounds.json').read_text())
    lang = json.loads((ASSET_ROOT / 'lang/en_us.json').read_text())
    for name, (subtitle, reach) in ROWS.items():
        path = ASSET_ROOT / f'sounds/fallow/{name}.ogg'
        assert path.exists() and path.stat().st_size > 1000, path
        assert sounds[f'fallow.{name}'] == {'sounds': [{'name': f'the_oldest_house:fallow/{name}', 'attenuation_distance': reach}], 'subtitle': f'subtitles.the_oldest_house.fallow.{name}'}, name
        assert lang[f'subtitles.the_oldest_house.fallow.{name}'] == subtitle, name
    print(f'Verified Fallow\'s {len(ROWS)} original cues.')


if __name__ == '__main__' and '--check' in sys.argv:
    check()
    sys.exit(0)

import numpy as np  # noqa: E402
import soundfile  # noqa: E402

from generate_movie_night_audio import ASSETS, RATE, seconds, times, band, normalized, update  # noqa: E402

rng = np.random.default_rng(9672)


def wind():
    # Six seconds of a dry wind over a standing field: a broad low hush that rises and falls in long gusts, and over it the field's own
    # sound, a high dry hiss where the heads rub, which swells a beat after each gust as it runs through the wheat toward the listener.
    n = seconds(6.0)
    t = times(n)
    gust = .55 + .45 * np.sin(2 * np.pi * t / 5.2 + .6) ** 2 * (.7 + .3 * np.sin(2 * np.pi * t / 1.7))
    hush = band(rng.standard_normal(n), 60, 700) * gust
    late = np.interp(t - .35, t, gust, left=gust[0])
    rustle = band(rng.standard_normal(n), 2500, 9000) * late ** 2 * (.6 + .4 * np.abs(band(rng.standard_normal(n), 2, 12)))
    edge = np.clip(t / .6, 0, 1) * np.clip((6.0 - t) / .6, 0, 1)
    return (hush * .9 + rustle * .55) * edge


def insects():
    # Three seconds of the insects in the wheat: a dry steady trill from several at once, each its own rate and pitch, swelling and
    # falling a little, and once a grasshopper's short ratchet nearer than the rest.
    n = seconds(3.0)
    t = times(n)
    out = np.zeros(n)
    for rate, pitch, depth, phase in ((31, 4200, .8, 0), (27, 5100, .7, 1.1), (36, 3600, .6, 2.3), (22, 6100, .5, .4)):
        pulse = np.clip(np.sin(2 * np.pi * rate * t + phase), 0, None) ** 3
        tone = band(rng.standard_normal(n), pitch * .85, pitch * 1.15)
        swell = .7 + .3 * np.sin(2 * np.pi * t / (2.4 + phase) + phase)
        out += tone * pulse * depth * swell
    x = t - 1.6
    ratchet = band(rng.standard_normal(n), 3000, 9000) * (np.sin(2 * np.pi * 48 * t) > .3) * np.clip(x / .02, 0, 1) * np.clip((.45 - x) / .05, 0, 1) * 1.2
    edge = np.clip(t / .3, 0, 1) * np.clip((3.0 - t) / .3, 0, 1)
    return (out + ratchet) * edge


def radio():
    # Four seconds of a ball game on a small radio across the store: a man's voice calling the play, as a band of formant-shaped noise
    # with a pitch in it, words and pauses; the crowd swelling under him once; all of it squeezed through a little speaker, with hiss.
    n = seconds(4.0)
    t = times(n)
    words = np.zeros(n)
    at = .1
    while at < 3.7:
        length = rng.uniform(.12, .32)
        x = t - at
        shape = np.clip(x / .03, 0, 1) * np.clip((length - x) / .04, 0, 1) * (x >= 0)
        pitch = rng.uniform(115, 165)
        f = pitch * (1 + .08 * np.sin(2 * np.pi * 3 * x))
        voiced = np.sign(np.sin(2 * np.pi * np.cumsum(f) / RATE)) * .4
        words += (band(voiced, 300, 900) + band(voiced, 1100, 2400) * .6 + band(rng.standard_normal(n), 2500, 3500) * .15) * shape
        at += length + rng.choice((.05, .07, .09, .3), p=(.4, .3, .2, .1))
    crowd = band(rng.standard_normal(n), 400, 2500) * np.exp(-((t - 2.6) / .7) ** 2) * .55
    hiss = band(rng.standard_normal(n), 3000, 6000) * .06
    speaker = band(words + crowd, 350, 3400)
    edge = np.clip(t / .3, 0, 1) * np.clip((4.0 - t) / .3, 0, 1)
    return (speaker + hiss) * edge


def bell():
    # The bell on top of a gas pump as the nozzle comes off its hook: a small clapper striking a dome twice, quick, the dome ringing on
    # a few bright partials and dying, and the click of the hook under it.
    n = seconds(1.4)
    t = times(n)
    out = np.zeros(n)
    for at, level in ((.04, 1.0), (.16, .7)):
        x = t - at
        on = x >= 0
        ring = (np.sin(2 * np.pi * 2210 * x) + .55 * np.sin(2 * np.pi * 3390 * x) + .3 * np.sin(2 * np.pi * 5480 * x))
        out += ring * np.exp(-np.clip(x, 0, None) / .32) * on * level
        out += band(rng.standard_normal(n), 2000, 8000) * np.exp(-np.clip(x, 0, None) / .006) * on * level * .6
    click = band(rng.standard_normal(n), 400, 3000) * np.exp(-np.clip(t, 0, None) / .01) * .5
    return out + click


MAKERS = {'wind': wind, 'insects': insects, 'radio': radio, 'bell': bell}


def main():
    folder = ASSETS / 'sounds/fallow'
    folder.mkdir(parents=True, exist_ok=True)
    for name, (subtitle, reach) in ROWS.items():
        target = folder / f'{name}.ogg'
        if not target.exists():
            soundfile.write(str(target), normalized(MAKERS[name]()).astype(np.float32), RATE, format='OGG', subtype='VORBIS')
        update(ASSETS / 'sounds.json', f'fallow.{name}',
               {'sounds': [{'name': f'the_oldest_house:fallow/{name}', 'attenuation_distance': reach}], 'subtitle': f'subtitles.the_oldest_house.fallow.{name}'})
        update(ASSETS / 'lang/en_us.json', f'subtitles.the_oldest_house.fallow.{name}', subtitle)


if __name__ == '__main__':
    main()
