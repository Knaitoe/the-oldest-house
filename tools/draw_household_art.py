#!/usr/bin/env python3
"""The household on the great staircase (0.4.214; docs/STAIRCASE_HOUSEHOLD_0_4_214.md).

The household's door, left in the shaft wall beside a landing: an ordinary front door painted a deep green, with raised panels, a
brass letter plate and a brass knob, drawn at vanilla's sixteen pixels as the two halves of a vanilla door. Its blockstate and
models follow the vanilla door's, so it hangs and rotates like any other door.
"""
import argparse, io, json, random
from pathlib import Path
from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parents[1]
A = ROOT / 'src/main/resources/assets/the_oldest_house'
NS = 'the_oldest_house'
PAINT, SHADE, LIGHT, BRASS, BRASS_DARK = (63, 91, 72), (40, 60, 48), (92, 124, 100), (196, 160, 72), (140, 108, 44)


def half(top):
    im = Image.new('RGBA', (16, 16), PAINT + (255,))
    d = ImageDraw.Draw(im)
    d.rectangle((0, 0, 15, 15), outline=SHADE)
    rnd = random.Random(214 + top)
    # Raised panels: a shadowed edge below and to the right, a lit edge above and to the left.
    panels = [(3, 2, 7, 11), (9, 2, 13, 11)] if top else [(3, 4, 7, 13), (9, 4, 13, 13)]
    for x0, y0, x1, y1 in panels:
        d.line((x0, y1, x1, y1), fill=SHADE); d.line((x1, y0, x1, y1), fill=SHADE)
        d.line((x0, y0, x1 - 1, y0), fill=LIGHT); d.line((x0, y0, x0, y1 - 1), fill=LIGHT)
    if top:
        # The letter plate, low on the upper half.
        d.rectangle((5, 13, 10, 14), fill=BRASS); d.line((5, 14, 10, 14), fill=BRASS_DARK)
    else:
        # The knob, high on the lower half, on the latch side.
        d.rectangle((12, 1, 13, 2), fill=BRASS); im.putpixel((13, 2), BRASS_DARK + (255,))
    # Paint worn a little where hands go.
    for _ in range(10):
        x, y = rnd.randrange(1, 15), rnd.randrange(1, 15)
        if im.getpixel((x, y))[:3] == PAINT:
            im.putpixel((x, y), tuple(min(255, c + rnd.choice((-6, 6))) for c in PAINT) + (255,))
    return im


def door_state():
    v = {}
    for h in ('lower', 'upper'):
        for hinge in ('left', 'right'):
            for opened in (False, True):
                n = ('bottom' if h == 'lower' else 'top') + '_' + hinge + ('_open' if opened else '')
                for face, rot in (('east', 0), ('south', 90), ('west', 180), ('north', 270)):
                    y = (rot + (90 if hinge == 'left' else -90)) % 360 if opened else rot
                    e = {'model': f'{NS}:block/household_door_{n}'}
                    if y:
                        e['y'] = y
                    v[f'facing={face},half={h},hinge={hinge},open={str(opened).lower()}'] = e
    return {'variants': v}


def files():
    out = {}
    for top in (True, False):
        b = io.BytesIO(); half(top).save(b, format='PNG')
        out[A / f'textures/block/household_door_{"top" if top else "bottom"}.png'] = b.getvalue()
    for h in ('bottom', 'top'):
        for hinge in ('left', 'right'):
            for opened in ('', '_open'):
                model = {'parent': f'minecraft:block/door_{h}_{hinge}{opened}', 'render_type': 'minecraft:cutout',
                         'textures': {'bottom': f'{NS}:block/household_door_bottom', 'top': f'{NS}:block/household_door_top'}}
                out[A / f'models/block/household_door_{h}_{hinge}{opened}.json'] = json.dumps(model, indent=2) + '\n'
    out[A / 'blockstates/household_door.json'] = json.dumps(door_state(), indent=2) + '\n'
    return out


def main():
    p = argparse.ArgumentParser(); p.add_argument('--check', action='store_true'); check = p.parse_args().check
    for path, data in files().items():
        if check:
            assert path.exists(), path
            if isinstance(data, str):
                assert json.loads(path.read_text()) == json.loads(data), path
            else:
                assert Image.open(path).convert('RGBA').tobytes() == Image.open(io.BytesIO(data)).convert('RGBA').tobytes(), path
        else:
            path.parent.mkdir(parents=True, exist_ok=True)
            path.write_text(data) if isinstance(data, str) else path.write_bytes(data)
    lang_path = A / 'lang/en_us.json'; lang = json.loads(lang_path.read_text())
    if check:
        assert lang.get('block.the_oldest_house.household_door') == 'A Painted Door', 'lang'
    else:
        lang['block.the_oldest_house.household_door'] = 'A Painted Door'; lang_path.write_text(json.dumps(lang, indent=2, ensure_ascii=False) + '\n')
    print(('Verified' if check else 'Drew') + ' the household\'s painted door: two halves, eight models and its states.')


if __name__ == '__main__':
    main()
