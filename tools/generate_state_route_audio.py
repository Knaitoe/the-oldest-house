"""Original sounds for State Route 9 (0.4.333): a pickup's diesel chugging, high-speed tires hissing on wet tar, a semi's air horn
dropping in pitch as it goes by, a car passing in the rain, the wind off a trailer, brakes and the blackout, engines ticking at a crawl,
every horn on the highway in one chord, and the padlock on the chain-link gate.

Synthesized from oscillators and noise with Movie Night's building blocks; no recordings. Mono, so Minecraft places each one where it
happens on the road. Run from the repository root. Existing files are kept, so the committed sounds only change when deleted first.
"""
import json
import sys
from pathlib import Path

ROWS = {
    'diesel': ('A distant diesel engine chugs steadily', 40),
    'hiss': ('High-speed tires hiss on wet asphalt', 64),
    'horn': ('An air horn blasts going by', 72),
    'pass': ('A car rushes past', 40),
    'wake': ('Wind off a passing trailer', 12),
    'screech': ('Tires screech', 32),
    'tick': ('Engines tick over at a crawl', 16),
    'chord': ('Every horn on the highway sounds at once', 96),
    'padlock': ('A padlock rattles on its chain', 16),
    # 0.4.336: a body struck at speed.
    'impact': ('A heavy impact on the road', 40),
}
ASSET_ROOT = Path(__file__).resolve().parents[1] / 'src/main/resources/assets/the_oldest_house'


def check():
    sounds = json.loads((ASSET_ROOT / 'sounds.json').read_text())
    lang = json.loads((ASSET_ROOT / 'lang/en_us.json').read_text())
    for name, (subtitle, reach) in ROWS.items():
        path = ASSET_ROOT / f'sounds/route/{name}.ogg'
        assert path.exists() and path.stat().st_size > 1000, path
        assert sounds[f'route.{name}'] == {'sounds': [{'name': f'the_oldest_house:route/{name}', 'attenuation_distance': reach}], 'subtitle': f'subtitles.the_oldest_house.route.{name}'}, name
        assert lang[f'subtitles.the_oldest_house.route.{name}'] == subtitle, name
    print(f'Verified the state route\'s {len(ROWS)} original cues.')


if __name__ == '__main__' and '--check' in sys.argv:
    check()
    sys.exit(0)

import numpy as np  # noqa: E402
import soundfile  # noqa: E402

from generate_movie_night_audio import ASSETS, RATE, seconds, times, band, envelope, place, normalized, update  # noqa: E402

rng = np.random.default_rng(9333)


def diesel():
    # Low firing pulses, a little uneven, with the rattle of loose metal riding on them.
    n = seconds(1.5)
    t = times(n)
    rate = 11.0
    phase = 2 * np.pi * np.cumsum(rate * (1 + .04 * np.sin(2 * np.pi * .7 * t))) / RATE
    pulses = np.maximum(0, np.sin(phase)) ** 6
    body = band(rng.standard_normal(n), 40, 260) * pulses * 1.4 + np.sin(2 * np.pi * 47 * t) * pulses * .5
    rattle = band(rng.standard_normal(n), 900, 3000) * (pulses > .6) * .08
    return (body + rattle) * np.minimum(1, t / .2) * np.minimum(1, (1.5 - t) / .3)


def hiss():
    # Wet tires coming up the lane: a rising sheet of spray, louder and brighter as it nears, never quite arriving.
    n = seconds(3.0)
    t = times(n)
    spray = band(rng.standard_normal(n), 1500, 9000)
    roar = band(rng.standard_normal(n), 120, 900)
    grow = (t / 3.0) ** 2.2
    return (spray * (.15 + .85 * grow) + roar * grow * .6) * np.minimum(1, (3.0 - t) / .08)


def horn():
    # Two reeds a minor third apart, dropping in pitch as the truck goes by, the wind of it behind.
    n = seconds(2.4)
    t = times(n)
    drop = np.where(t < 1.0, 1.0, 1.0 - .11 * np.minimum(1, (t - 1.0) / .35))
    out = np.zeros(n)
    for f, a in ((220, 1.0), (262, .8)):
        ph = 2 * np.pi * np.cumsum(f * drop) / RATE
        tone = np.sign(np.sin(ph)) * .3 + np.sin(ph) * .5 + np.sin(2 * ph) * .2
        out += band(tone, 80, 2400) * a
    gate = np.minimum(1, t / .05) * np.minimum(1, np.maximum(0, (1.9 - t) / .4))
    near = np.exp(-((t - 1.0) / .6) ** 2) * .6 + .4
    wind = band(rng.standard_normal(n), 200, 2500) * np.exp(-((t - 1.2) / .5) ** 2) * .5
    return out * gate * near + wind


def pass_():
    # A car going by in the rain: spray rising, a whoosh at its nearest, falling away.
    n = seconds(1.6)
    t = times(n)
    near = np.exp(-((t - .7) / .28) ** 2)
    return band(rng.standard_normal(n), 300, 6000) * (near * .9 + .05) + band(rng.standard_normal(n), 60, 300) * near * .5


def wake():
    # The slap of air off a trailer side, close and short.
    n = seconds(.9)
    t = times(n)
    return band(rng.standard_normal(n), 60, 1200) * envelope(n, .03, .25) * 1.2 + band(rng.standard_normal(n), 1500, 6000) * envelope(n, .01, .12) * .3


def screech():
    # Locked tires on wet tar: a squeal that wavers, then a hard stop.
    n = seconds(1.2)
    t = times(n)
    f = 1900 + 300 * np.sin(2 * np.pi * 7 * t) + rng.standard_normal(n).cumsum() * .4
    tone = np.sin(2 * np.pi * np.cumsum(f) / RATE)
    out = band(tone * .6 + rng.standard_normal(n) * .3, 900, 5000) * np.minimum(1, t / .03) * np.minimum(1, np.maximum(0, (1.0 - t) / .1))
    k = seconds(.2)
    place(out, band(rng.standard_normal(k), 40, 600) * envelope(k, .002, .06) * 1.4, 1.0)
    return out


def tick():
    # An engine idling low, its fan ticking, rain on a hood.
    n = seconds(2.0)
    t = times(n)
    idle = np.sin(2 * np.pi * 31 * t) * (.5 + .5 * np.sin(2 * np.pi * 7.5 * t) ** 2) * .4
    ticks = np.zeros(n)
    at = .1
    while at < 1.9:
        k = seconds(.02)
        place(ticks, band(rng.standard_normal(k), 2000, 6000) * envelope(k, .001, .006) * .5, at)
        at += rng.uniform(.18, .32)
    rain = band(rng.standard_normal(n), 3000, 9000) * .05
    return idle + ticks + rain


def chord():
    # Every horn at once: car horns and truck horns on a dozen pitches, all held, all cut at the same instant.
    n = seconds(2.6)
    t = times(n)
    out = np.zeros(n)
    for f in (196, 220, 262, 294, 330, 349, 392, 415, 466, 523, 587):
        ph = 2 * np.pi * f * t + rng.uniform(0, 6.28)
        out += (np.sign(np.sin(ph)) * .25 + np.sin(ph) * .6) * rng.uniform(.5, 1)
    out = band(out, 90, 3200)
    return out * np.minimum(1, t / .04) * (t < 2.35)


def padlock():
    # The shackle knocking the hasp, and a chain sliding through the mesh.
    n = seconds(.8)
    out = np.zeros(n)
    for at, f in ((0.0, 2600), (.09, 3100), (.21, 2400)):
        k = seconds(.12)
        tk = times(k)
        place(out, (np.sin(2 * np.pi * f * tk) * .5 + band(rng.standard_normal(k), 2000, 8000) * .4) * envelope(k, .001, .03), at)
    k = seconds(.4)
    place(out, band(rng.standard_normal(k), 1500, 7000) * envelope(k, .02, .12) * .25, .3)
    return out


def impact():
    # A body hit at speed: one deep blunt thud with no ring to it, a crack inside it, glass going, and then nothing but rain.
    n = seconds(1.6)
    out = np.zeros(n)
    k = seconds(.35)
    tk = times(k)
    place(out, (np.sin(2 * np.pi * 48 * tk) * 1.0 + band(rng.standard_normal(k), 60, 400) * .9) * envelope(k, .002, .12), 0.0)
    k = seconds(.08)
    place(out, band(rng.standard_normal(k), 900, 4000) * envelope(k, .0005, .02) * .7, .01)
    k = seconds(.9)
    tk = times(k)
    glass = band(rng.standard_normal(k), 3500, 9500) * (rng.random(k) > .985) * np.exp(-tk * 3)
    place(out, glass * .8, .05)
    return out


MAKERS = {'impact': impact, 'diesel': diesel, 'hiss': hiss, 'horn': horn, 'pass': pass_, 'wake': wake, 'screech': screech, 'tick': tick, 'chord': chord, 'padlock': padlock}


def main():
    folder = ASSETS / 'sounds/route'
    folder.mkdir(parents=True, exist_ok=True)
    for name, (subtitle, reach) in ROWS.items():
        target = folder / f'{name}.ogg'
        if not target.exists():
            soundfile.write(str(target), normalized(MAKERS[name]()).astype(np.float32), RATE, format='OGG', subtype='VORBIS')
        update(ASSETS / 'sounds.json', f'route.{name}',
               {'sounds': [{'name': f'the_oldest_house:route/{name}', 'attenuation_distance': reach}], 'subtitle': f'subtitles.the_oldest_house.route.{name}'})
        update(ASSETS / 'lang/en_us.json', f'subtitles.the_oldest_house.route.{name}', subtitle)


if __name__ == '__main__':
    main()
