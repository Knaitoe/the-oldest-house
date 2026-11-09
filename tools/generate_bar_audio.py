"""Original sounds for the bar's games: the cue's tip on the ball, ball on ball, a ball off the cushion, and a ball dropping into a
pocket and rolling down the return (0.4.414); a dart thrown, into the board, into the wall, and the turn's darts pulled out (0.4.415);
and the man in booth four's whistle (0.4.416).

Synthesized from oscillators and noise with Movie Night's building blocks; no recordings. Mono, so Minecraft places each one at the
table. Run from tools/. Existing files are kept, so the committed sounds only change when deleted first.
"""
import json
import sys
from pathlib import Path

ROWS = {
    'pool/strike': ('pool.strike', 'Cue strikes the ball', 16),
    'pool/clack': ('pool.clack', 'Pool balls knock', 16),
    'pool/cushion': ('pool.cushion', 'Ball hits the cushion', 12),
    'pool/pocket': ('pool.pocket', 'Ball drops into a pocket', 16),
    'darts/throw': ('darts.throw', 'Dart thrown', 8),
    'darts/board': ('darts.board', 'Dart hits the board', 12),
    'darts/wall': ('darts.wall', 'Dart hits the wall', 12),
    'darts/pull': ('darts.pull', 'Darts pulled from the board', 10),
    'orders/whistle': ('orders.whistle', 'Someone whistles', 24),
}
ASSET_ROOT = Path(__file__).resolve().parents[1] / 'src/main/resources/assets/the_oldest_house'


def check():
    sounds = json.loads((ASSET_ROOT / 'sounds.json').read_text())
    lang = json.loads((ASSET_ROOT / 'lang/en_us.json').read_text())
    for path_name, (event, subtitle, reach) in ROWS.items():
        path = ASSET_ROOT / f'sounds/{path_name}.ogg'
        assert path.exists() and path.stat().st_size > 1000, path
        assert sounds[event] == {'sounds': [{'name': f'the_oldest_house:{path_name}', 'attenuation_distance': reach}], 'subtitle': f'subtitles.the_oldest_house.{event}'}, event
        assert lang[f'subtitles.the_oldest_house.{event}'] == subtitle, event
    print(f'Verified the bar\'s {len(ROWS)} original cues.')


if __name__ == '__main__' and '--check' in sys.argv:
    check()
    sys.exit(0)

import numpy as np  # noqa: E402
import soundfile  # noqa: E402

from generate_movie_night_audio import ASSETS, RATE, seconds, times, band, normalized, update  # noqa: E402

rng = np.random.default_rng(9414)


def strike():
    # A leather tip on a resin ball: a soft thud with a short bright click.
    n = seconds(.14)
    t = times(n)
    thud = np.sin(2 * np.pi * 180 * t) * np.exp(-t / .02) * .6
    click = band(rng.standard_normal(n), 2500, 7000) * np.exp(-t / .004) * .7
    return thud + click


def clack():
    # Two hard balls: a sharp knock with a ringing pair of partials that die fast.
    n = seconds(.16)
    t = times(n)
    knock = band(rng.standard_normal(n), 1800, 8000) * np.exp(-t / .003) * .9
    ring = (np.sin(2 * np.pi * 3100 * t) * .5 + np.sin(2 * np.pi * 4700 * t) * .3) * np.exp(-t / .018)
    return knock + ring * .6


def cushion():
    # Rubber under cloth: a dull, soft bump.
    n = seconds(.14)
    t = times(n)
    bump = np.sin(2 * np.pi * np.cumsum(140 - 60 * np.minimum(1, t / .04)) / RATE) * np.exp(-t / .03) * .8
    cloth = band(rng.standard_normal(n), 300, 1500) * np.exp(-t / .01) * .3
    return bump + cloth


def pocket():
    # The drop into a leather pocket and the roll down the return: a hollow thunk, then a low rattle running away.
    n = seconds(.9)
    t = times(n)
    thunk = np.sin(2 * np.pi * np.cumsum(220 - 90 * np.minimum(1, t / .06)) / RATE) * np.exp(-t / .05) * .8
    roll = band(rng.standard_normal(n), 150, 900) * np.clip((t - .08) / .05, 0, 1) * np.exp(-np.maximum(0, t - .1) / .3) * .35
    ticks = np.zeros(n)
    for k, at in enumerate([.14, .22, .31, .43, .58]):
        i = int(at * RATE)
        m = seconds(.01)
        if i + m < n:
            ticks[i:i + m] += band(rng.standard_normal(m), 800, 3000) * np.exp(-times(m) / .002) * (.5 - k * .08)
    return thunk + roll + ticks


def dart_throw():
    # A dart leaving the hand: a short soft rush of air, rising then gone.
    n = seconds(.18)
    t = times(n)
    env = np.sin(np.pi * np.clip(t / .18, 0, 1)) ** 2
    return band(rng.standard_normal(n), 900, 4200) * env * .5


def dart_board():
    # The point sinking into packed sisal: a dull, dry thud with a tiny click of the barrel.
    n = seconds(.16)
    t = times(n)
    thud = band(rng.standard_normal(n), 200, 1400) * np.exp(-t / .012) * .8
    body = np.sin(2 * np.pi * 150 * t) * np.exp(-t / .03) * .4
    click = band(rng.standard_normal(n), 3000, 8000) * np.exp(-t / .002) * .25
    return thud + body + click


def dart_wall():
    # Into wood or plaster: a harder, higher tock that rings a moment.
    n = seconds(.2)
    t = times(n)
    tock = band(rng.standard_normal(n), 900, 5000) * np.exp(-t / .004) * .8
    ring = np.sin(2 * np.pi * 820 * t) * np.exp(-t / .025) * .35
    return tock + ring


def dart_pull():
    # Three darts worked out of the board one after another: short gritty scrapes.
    n = seconds(.7)
    out = np.zeros(n)
    for at in (.0, .22, .43):
        i = int(at * RATE)
        m = seconds(.12)
        tt = times(m)
        scrape = band(rng.standard_normal(m), 600, 3500) * np.sin(np.pi * np.clip(tt / .12, 0, 1)) * .45
        out[i:i + m] += scrape[:max(0, min(m, n - i))]
    return out


def whistle():
    # One long, thin whistle through the teeth: a pure tone sliding up and falling away, a little breath round it.
    n = seconds(.75)
    t = times(n)
    pitch = 1350 + 420 * np.clip(t / .25, 0, 1) - 260 * np.clip((t - .45) / .3, 0, 1) + 14 * np.sin(2 * np.pi * 6 * t)
    phase = 2 * np.pi * np.cumsum(pitch) / RATE
    shape = np.clip(t / .04, 0, 1) * np.clip((.75 - t) / .18, 0, 1)
    tone = np.sin(phase) * shape * .7
    breath = band(rng.standard_normal(n), 1800, 6000) * shape * .06
    return tone + breath


MAKERS = {'pool/strike': strike, 'pool/clack': clack, 'pool/cushion': cushion, 'pool/pocket': pocket,
          'darts/throw': dart_throw, 'darts/board': dart_board, 'darts/wall': dart_wall, 'darts/pull': dart_pull,
          'orders/whistle': whistle}


def main():
    for path_name, (event, subtitle, reach) in ROWS.items():
        target = ASSETS / f'sounds/{path_name}.ogg'
        target.parent.mkdir(parents=True, exist_ok=True)
        if not target.exists():
            soundfile.write(str(target), normalized(MAKERS[path_name]()).astype(np.float32), RATE, format='OGG', subtype='VORBIS')
        update(ASSETS / 'sounds.json', event,
               {'sounds': [{'name': f'the_oldest_house:{path_name}', 'attenuation_distance': reach}], 'subtitle': f'subtitles.the_oldest_house.{event}'})
        update(ASSETS / 'lang/en_us.json', f'subtitles.the_oldest_house.{event}', subtitle)


if __name__ == '__main__':
    main()
