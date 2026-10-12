"""The house at the bottom of the lake: sounds, models and names written in code.

- Three original subtitled sounds under sounds/lake_house/: the compressor's engine, the canoe scraping the culvert, and
  air hissing into the tanks. Each gets its sounds.json entry and subtitle.
- The lent gear's item models: the diving helmet is built of elements, so it reads as a helmet in the hand and is worn
  whole on the head (the vanilla head layer draws any worn item model); the tanks and flashlight are flat icons.
- The compressor's block model and blockstate.
- The names of the canoe, the gear and the compressor.

The textures are drawn by tools/draw_lake_house_art.py. Run both again only to redraw.
Usage: python3 tools/generate_lake_house_assets.py
"""
from pathlib import Path
import json

import numpy as np
import soundfile

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / 'src/main/resources/assets/the_oldest_house'
RATE = 22050
NS = 'the_oldest_house'


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


def normalize(signal, peak=.8):
    return signal / max(1e-9, np.max(np.abs(signal))) * peak


# ------------------------------------------------------------------------------------------------ sounds
def compressor():
    """A small petrol engine on a raft, one second of it: a thudding single cylinder, valve clatter and a belt's whine."""
    seconds = 1.0
    t = t_of(seconds)
    beat = 9.0  # firings a second
    phase = (t * beat) % 1.0
    thump = np.exp(-phase * 18) * (np.sin(2 * np.pi * 62 * t) + .6 * np.sin(2 * np.pi * 124 * t))
    clatter = lowpass(noise(seconds, 31), .55) * np.exp(-((phase - .45) % 1.0) * 40) * .35
    whine = np.sin(2 * np.pi * (410 + 6 * np.sin(2 * np.pi * 3 * t)) * t) * .05
    rumble = lowpass(noise(seconds, 33), .03) * .5
    # Fade the ends a touch so the clip repeats each second without a click.
    envelope = np.clip(t / .02, 0, 1) * np.clip((seconds - t) / .02, 0, 1)
    return normalize((thump + clatter + whine + rumble) * envelope, .7)


def scrape():
    """A canoe's hull dragged along wet concrete: a gritty, juddering scrape that catches and slips."""
    seconds = .9
    t = t_of(seconds)
    grit = noise(seconds, 41)
    body = lowpass(grit, .25) - lowpass(grit, .04)
    stick = (np.sin(2 * np.pi * 17 * t + 2 * np.sin(2 * np.pi * 3 * t)) > .2).astype(float)
    stick = lowpass(stick, .02)
    hull = np.sin(2 * np.pi * 140 * t) * lowpass(noise(seconds, 43), .01) * 1.5
    envelope = np.clip(t / .08, 0, 1) * np.clip((seconds - t) / .25, 0, 1)
    return normalize((body * (.4 + .6 * stick) + hull * .3) * envelope)


def hiss():
    """Compressed air into a tank: a hard hiss that rises, holds and falls away as the needle stops."""
    seconds = 1.4
    t = t_of(seconds)
    air = noise(seconds, 51)
    hissing = air - lowpass(air, .35)
    envelope = np.clip(t / .12, 0, 1) * np.clip((seconds - t) / .5, 0, 1)
    tone = np.sin(2 * np.pi * (2400 + 300 * t) * t) * .04
    click = np.zeros_like(t)
    n = round(RATE * .03)
    click[:n] = lowpass(noise(.03, 53), .6)[:n] * np.exp(-np.arange(n) / (RATE * .006))
    return normalize(hissing * envelope * .9 + tone * envelope + click)


SOUNDS = {
    'compressor': (compressor, 'Compressor chugs', 24),
    'scrape': (scrape, 'Canoe scrapes concrete', 20),
    'hiss': (hiss, 'Air hisses into tanks', 16),
}


# ------------------------------------------------------------------------------------------------ models
def face(texture, uv=(0, 0, 16, 16)):
    return {'texture': texture, 'uv': list(uv)}


def faces(texture, uv=(0, 0, 16, 16), **overrides):
    out = {side: face(texture, uv) for side in ('north', 'south', 'east', 'west', 'up', 'down')}
    for side, value in overrides.items():
        out[side] = value
    return out


BLOCK_DISPLAY = {
    'gui': {'rotation': [30, 225, 0], 'translation': [0, 0, 0], 'scale': [.625, .625, .625]},
    'ground': {'rotation': [0, 0, 0], 'translation': [0, 3, 0], 'scale': [.25, .25, .25]},
    'fixed': {'rotation': [0, 0, 0], 'translation': [0, 0, 0], 'scale': [.5, .5, .5]},
    'thirdperson_righthand': {'rotation': [75, 45, 0], 'translation': [0, 2.5, 0], 'scale': [.375, .375, .375]},
    'firstperson_righthand': {'rotation': [0, 45, 0], 'translation': [0, 0, 0], 'scale': [.4, .4, .4]},
    'firstperson_lefthand': {'rotation': [0, 225, 0], 'translation': [0, 0, 0], 'scale': [.4, .4, .4]},
    'head': {'rotation': [0, 0, 0], 'translation': [0, 0, 0], 'scale': [1, 1, 1]},
}


def helmet_model():
    """The gold diving helmet: a brass shell over a wider collar, a bolted window in front, small windows at the sides, a
    valve on top and the hose coupling behind. North is the front; worn, it faces where the reader faces."""
    shell, port, collar = f'{NS}:item/lake_house_helmet', f'{NS}:item/lake_house_helmet_port', f'{NS}:item/lake_house_helmet_collar'
    return {
        'textures': {'shell': shell, 'port': port, 'collar': collar, 'particle': shell},
        'elements': [
            {'from': [0, 3, 0], 'to': [16, 16, 16], 'faces': faces('#shell', down=face('#collar'))},
            {'from': [-1, 0, -1], 'to': [17, 3, 17], 'faces': faces('#collar')},
            {'from': [3.5, 5, -1], 'to': [12.5, 14, 0], 'faces': faces('#port', north=face('#port'), south=face('#shell'))},
            {'from': [-1, 7, 5], 'to': [0, 12, 11], 'faces': faces('#port', west=face('#port', (3, 3, 13, 13)))},
            {'from': [16, 7, 5], 'to': [17, 12, 11], 'faces': faces('#port', east=face('#port', (3, 3, 13, 13)))},
            {'from': [6, 16, 6], 'to': [10, 18, 10], 'faces': faces('#collar')},
            {'from': [6, 8, 16], 'to': [10, 12, 18.5], 'faces': faces('#collar')},
        ],
        'display': BLOCK_DISPLAY,
    }


def compressor_model():
    """The compressor, facing north: an iron skid, a red tank lying along it, a grey engine on top with a pressure gauge
    on its face, a flywheel at one side and an exhaust pipe."""
    tank, engine, base, gauge = (f'{NS}:block/lake_house_compressor_{n}' for n in ('tank', 'engine', 'base', 'gauge'))
    return {
        'textures': {'tank': tank, 'engine': engine, 'base': base, 'gauge': gauge, 'particle': tank},
        'elements': [
            {'from': [2, 0, 3], 'to': [14, 1, 13], 'faces': faces('#base')},
            {'from': [3, 1, 4], 'to': [13, 6, 12], 'faces': faces('#tank')},
            {'from': [4, 6, 5], 'to': [11, 10, 11], 'faces': faces('#engine', north=face('#gauge', (2, 2, 14, 14)))},
            {'from': [11, 4, 6], 'to': [13, 10, 10], 'faces': faces('#base')},
            {'from': [9, 10, 8], 'to': [10, 11.5, 9], 'faces': faces('#base')},
        ],
    }


def blockstate():
    rotation = {'north': 0, 'east': 90, 'south': 180, 'west': 270}
    return {'variants': {f'facing={f},running={r}': ({'model': f'{NS}:block/lake_house_compressor', 'y': y} if y else {'model': f'{NS}:block/lake_house_compressor'})
                         for f, y in rotation.items() for r in ('false', 'true')}}


def beaver_model():
    """The porcelain beaver, facing north: a round body, a smaller head forward, a flat tail behind."""
    t = f'{NS}:block/lake_house_beaver'
    return {
        'textures': {'porcelain': t, 'particle': t},
        'elements': [
            {'from': [5, 0, 5], 'to': [11, 6, 11], 'faces': faces('#porcelain', north=face('#porcelain', (5, 8, 11, 14)))},
            {'from': [6, 5, 3.5], 'to': [10, 9, 7.5], 'faces': faces('#porcelain', north=face('#porcelain', (4, 1, 12, 9)), up=face('#porcelain', (5, 0, 11, 4)))},
            {'from': [6.5, 0, 11], 'to': [9.5, 1, 14.5], 'faces': faces('#porcelain', (5, 10, 11, 16))},
        ],
    }


def facing_blockstate(model, extra):
    rotation = {'north': 0, 'east': 90, 'south': 180, 'west': 270}
    variants = {}
    for f, y in rotation.items():
        for value in extra[1]:
            entry = {'model': model}
            if y:
                entry['y'] = y
            variants[f'facing={f},{extra[0]}={value}'] = entry
    return {'variants': variants}


NAMES = {
    'item.the_oldest_house.lake_house_flashlight': 'Flashlight',
    'item.the_oldest_house.lake_house_helmet': 'Gold diving helmet',
    'item.the_oldest_house.lake_house_tanks': 'Air tanks',
    'block.the_oldest_house.lake_house_compressor': 'Compressor',
    'block.the_oldest_house.lake_house_beaver': 'Porcelain beaver',
    'painting.the_oldest_house.lake_house_still_life.title': 'Still Life with Plums',
    'painting.the_oldest_house.lake_house_still_life.author': 'Unknown',
    'painting.the_oldest_house.lake_house_face.title': 'Still Life with Plums',
    'painting.the_oldest_house.lake_house_face.author': 'Unknown',
    'entity.the_oldest_house.lake_house_canoe': 'Green canoe',
}


def write_json(path, data):
    path.parent.mkdir(parents=True, exist_ok=True)
    original = path.read_text() if path.exists() else '\n'
    text = json.dumps(data, indent=2, ensure_ascii=False)
    if original.endswith('\n'):
        text += '\n'
    path.write_text(text)


def main():
    folder = ASSETS / 'sounds/lake_house'
    folder.mkdir(parents=True, exist_ok=True)
    table = json.loads((ASSETS / 'sounds.json').read_text())
    lang = json.loads((ASSETS / 'lang/en_us.json').read_text())
    for name, (make, subtitle, reach) in SOUNDS.items():
        # Vorbis output differs from run to run, so a sound already written is left alone; delete it to redraw it.
        if not (folder / f'{name}.ogg').exists():
            soundfile.write(str(folder / f'{name}.ogg'), make().astype(np.float32), RATE, format='OGG', subtype='VORBIS')
        table[f'lake_house.{name}'] = {'subtitle': f'subtitles.{NS}.lake_house.{name}',
                                       'sounds': [{'name': f'{NS}:lake_house/{name}', 'stream': False, 'attenuation_distance': reach}]}
        lang[f'subtitles.{NS}.lake_house.{name}'] = subtitle
    lang.update(NAMES)
    write_json(ASSETS / 'sounds.json', table)
    write_json(ASSETS / 'lang/en_us.json', lang)
    write_json(ASSETS / 'models/item/lake_house_helmet.json', helmet_model())
    write_json(ASSETS / 'models/item/lake_house_tanks.json', {'parent': 'minecraft:item/generated', 'textures': {'layer0': f'{NS}:item/lake_house_tanks'}})
    write_json(ASSETS / 'models/item/lake_house_flashlight.json', {'parent': 'minecraft:item/handheld', 'textures': {'layer0': f'{NS}:item/lake_house_flashlight'}})
    write_json(ASSETS / 'models/block/lake_house_compressor.json', compressor_model())
    write_json(ASSETS / 'blockstates/lake_house_compressor.json', blockstate())
    write_json(ASSETS / 'models/block/lake_house_beaver.json', beaver_model())
    write_json(ASSETS / 'blockstates/lake_house_beaver.json', facing_blockstate(f'{NS}:block/lake_house_beaver', ('waterlogged', ('false', 'true'))))
    data = ROOT / 'src/main/resources/data/the_oldest_house/painting_variant'
    for name in ('lake_house_still_life', 'lake_house_face'):
        write_json(data / f'{name}.json', {'asset_id': f'{NS}:{name}', 'width': 2, 'height': 2})
    print('wrote three sounds, the gear, compressor and beaver models, their blockstates, two paintings and the names')


if __name__ == '__main__':
    main()
