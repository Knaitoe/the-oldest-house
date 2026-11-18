"""Architectural pieces for the twelve hallway variants. Reuses existing pixel textures; --check verifies every model."""
import argparse
import json
from pathlib import Path
import flicker

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / 'src/main/resources/assets/the_oldest_house'
NS = 'the_oldest_house:'
FACES = ('north', 'south', 'west', 'east', 'up', 'down')


def piece(lo, hi, texture):
    return {'from': lo, 'to': hi, 'faces': {side: {'texture': '#' + texture} for side in FACES}}


def outputs():
    variants = {}
    for kind in ('plaster', 'rubble', 'seam', 'deep_step', 'cable'):
        for stage in range(3):
            textures = {'plaster': NS + 'block/hall/' + ('plaster_cracked' if stage else 'ceiling'),
                        'floor': NS + 'block/dark_hall/floor', 'ash': NS + 'block/dark_hall/ash',
                        'cable': NS + 'block/power/cable_light'}
            if kind == 'plaster':
                parts = [piece([0, 13, 0], [16, 16, 16], 'plaster'),
                         piece([2, 12, 2], [14, 13, 14], 'plaster')]
                particle = textures['plaster']
            elif kind == 'rubble':
                parts = [piece([1, 0, 2], [7, 2, 8], 'plaster'),
                         piece([9, 0, 7], [15, 3, 14], 'plaster')]
                particle = textures['plaster']
            elif kind == 'seam':
                # A visible hairline, then a wider warning joint; collision stays whole until the floor actually opens.
                gap = 1 if stage == 0 else 3
                parts = [piece([0, 0, 0], [16, 15, 16], 'ash'),
                         piece([0, 15, 0], [16, 16, 8 - gap / 2], 'floor'),
                         piece([0, 15, 8 + gap / 2], [16, 16, 16], 'floor')]
                particle = textures['floor']
            elif kind == 'deep_step':
                parts = [piece([0, 0, 0], [16, 8, 16], 'floor')]
                particle = textures['floor']
            else:
                parts = [piece([0, .25, 7], [16, 1.5, 9], 'cable')]
                particle = textures['cable']
            textures['particle'] = particle
            name = f'hallway_variants/{kind}_{stage}'
            model = {'textures': textures, 'elements': parts}
            yield ASSETS / 'models/block' / (name + '.json'), flicker.dumps(model)
            variants[f'kind={kind},stage={stage}'] = {'model': NS + 'block/' + name}
    yield ASSETS / 'blockstates/hallway_fitting.json', json.dumps({'variants': variants}, indent=2) + '\n'


def main():
    check = argparse.ArgumentParser()
    check.add_argument('--check', action='store_true')
    args = check.parse_args()
    missed = []
    for path, text in outputs():
        if args.check:
            if not path.exists() or path.read_text() != text:
                missed.append(str(path.relative_to(ROOT)))
        else:
            path.parent.mkdir(parents=True, exist_ok=True)
            path.write_text(text)
    if missed:
        raise SystemExit('Hallway assets differ: ' + ', '.join(missed))
    print('Hallway variant models agree.' if args.check else 'Wrote hallway variant models.')


if __name__ == '__main__':
    main()
