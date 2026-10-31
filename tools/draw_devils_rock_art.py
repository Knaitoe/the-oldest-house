"""Devil's Rock art (0.4.201, docs/DEVILS_ROCK_REWORK.md): Tommy's old pennies, and the double with pennies over its eyes.

- textures/block/devils_pennies.png and models/block/devils_pennies.json: a few worn copper coins lying flat, the
  vignette_detail kind=pennies (on the bureau, and between the two rocks on the island).
- textures/entity/literary_devils_double.png: the dark silhouette's skin (128x64, the cast layout) with a penny over each
  eye. It is drawn from literary_silhouette.png, which is left as it is.

Run without arguments to draw; --check verifies the files match.
"""
from pathlib import Path
import json
import sys
from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / 'src/main/resources/assets/the_oldest_house'
COPPER, BRIGHT, DARK, WORN = (171, 104, 58, 255), (214, 150, 96, 255), (106, 62, 34, 255), (139, 92, 60, 255)


def pennies():
    img = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    # Three coins, five pixels across, in the texture's corners and middle; each with a lit rim and a worn face.
    for cx, cy in ((4, 4), (11, 6), (6, 11)):
        for dx in range(-2, 3):
            for dy in range(-2, 3):
                if abs(dx) == 2 and abs(dy) == 2:
                    continue
                edge = abs(dx) == 2 or abs(dy) == 2
                c = DARK if edge and (dx > 0 or dy > 0) else BRIGHT if edge else WORN if (dx + dy) % 3 == 0 else COPPER
                img.putpixel((cx + dx, cy + dy), c)
    return img


def model():
    def coin(x, z):
        return {'from': [x, 0, z], 'to': [x + 5, 0.5, z + 5], 'faces': {
            'up': {'uv': [x, z, x + 5, z + 5], 'texture': '#coin'}, 'down': {'uv': [x, z, x + 5, z + 5], 'texture': '#coin'},
            'north': {'uv': [x, z, x + 5, z + 1], 'texture': '#coin'}, 'south': {'uv': [x, z + 4, x + 5, z + 5], 'texture': '#coin'},
            'east': {'uv': [x + 4, z, x + 5, z + 5], 'texture': '#coin'}, 'west': {'uv': [x, z, x + 1, z + 5], 'texture': '#coin'}}}
    return {'render_type': 'minecraft:cutout', 'ambientocclusion': False,
            'textures': {'coin': 'the_oldest_house:block/devils_pennies', 'particle': 'the_oldest_house:block/devils_pennies'},
            'elements': [coin(2, 2), coin(9, 4), coin(4, 9)]}


def double():
    img = Image.open(ASSETS / 'textures/entity/literary_silhouette.png').convert('RGBA')
    # The head's front face is (8,8)-(15,15); a penny over each eye, two pixels square, on rows 11 and 12.
    for x0 in (9, 13):
        img.putpixel((x0, 11), BRIGHT)
        img.putpixel((x0 + 1, 11), COPPER)
        img.putpixel((x0, 12), COPPER)
        img.putpixel((x0 + 1, 12), DARK)
    return img


def blockstate(current):
    states = json.loads(current)
    for direction, turn in (('north', 0), ('east', 90), ('south', 180), ('west', 270)):
        states['variants'][f'facing={direction},kind=pennies'] = {'model': 'the_oldest_house:block/devils_pennies', 'y': turn}
    return json.dumps(states, indent=2) + '\n'


def main():
    check = '--check' in sys.argv
    images = {ASSETS / 'textures/block/devils_pennies.png': pennies(), ASSETS / 'textures/entity/literary_devils_double.png': double()}
    state_path = ASSETS / 'blockstates/vignette_detail.json'
    texts = {ASSETS / 'models/block/devils_pennies.json': json.dumps(model(), indent=1) + '\n', state_path: blockstate(state_path.read_text())}
    stale = []
    for path, img in images.items():
        if check:
            if not path.is_file() or Image.open(path).convert('RGBA').tobytes() != img.tobytes():
                stale.append(str(path))
            continue
        img.save(path)
    for path, text in texts.items():
        if check:
            if not path.is_file() or path.read_text() != text:
                stale.append(str(path))
            continue
        path.write_text(text)
    if stale:
        raise SystemExit("Devil's Rock art differs from tools/draw_devils_rock_art.py: " + ', '.join(stale))
    print(('Verified' if check else 'Drew') + " Devil's Rock's pennies and the double's face.")


if __name__ == '__main__':
    main()
