"""The paw's finger curling down, frame by frame, for everyone who can see the hand that holds it (0.4.147).

Each wish curls one finger down. The held paw shows it in four steps: the finger still up (the existing sprite with one
more finger), two in-between frames drawn here, and the finger down (the existing sprite). The in-between frames start
from the existing sprite drawn by tools/draw_three_fingers_art.py, in its palette, so the paw is the same paw.

    python3 tools/draw_paw_clench.py           write the frames, their models and the paw's overrides
    python3 tools/draw_paw_clench.py --check   verify them without writing
"""
import json
import sys
from pathlib import Path

from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / 'src/main/resources/assets/the_oldest_house'
# The paw's palette, as tools/draw_three_fingers_art.py draws it.
OUTLINE = (40, 28, 22)
DARK = (74, 52, 38)
MID = (108, 80, 58)
LIGHT = (140, 108, 78)
HIGH = (170, 140, 104)
NAIL = (58, 46, 40)

COLUMNS = (4, 7, 10)
TOPS = (3, 1, 2)
SUFFIX = {3: '', 2: '_2', 1: '_1', 0: '_0'}


def outline(grid):
    img = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    for (x, y), c in grid.items():
        img.putpixel((x, y), c + (255,))
    for (x, y) in list(grid):
        for nx, ny in ((x + 1, y), (x - 1, y), (x, y + 1), (x, y - 1)):
            if 0 <= nx < 16 and 0 <= ny < 16 and (nx, ny) not in grid and img.getpixel((nx, ny))[3] == 0:
                img.putpixel((nx, ny), OUTLINE + (255,))
    return img


def frame(left, step):
    """Finger {left} (counting from the outside) on its way down: step 1 bends at the tip, step 2 folds at the knuckle."""
    before = Image.open(ASSETS / f'textures/item/withered_paw{SUFFIX[left + 1]}.png').convert('RGBA')
    grid = {}
    for y in range(16):
        for x in range(16):
            r, g, b, a = before.getpixel((x, y))
            if a and (r, g, b) != OUTLINE:
                grid[(x, y)] = (r, g, b)
    x, top = COLUMNS[left], TOPS[left]
    cut = top + 2 if step == 1 else 6
    for y in range(top, cut):
        grid.pop((x, y), None)
        grid.pop((x + 1, y), None)
    if step == 1:
        # The tip tipped towards the viewer: its nail seen end on, the joint below it in shadow.
        grid[(x, cut)] = NAIL
        grid[(x + 1, cut)] = NAIL
        grid[(x + 1, cut + 1)] = DARK
        grid[(x, cut + 1)] = MID
    else:
        # Folded at the knuckle: the knuckle catches the light, the nail turns down onto the palm.
        grid[(x, cut)] = HIGH
        grid[(x + 1, cut)] = LIGHT
        grid[(x, cut + 1)] = NAIL
        grid[(x + 1, cut + 1)] = NAIL
        grid[(x, cut + 2)] = MID
        grid[(x + 1, cut + 2)] = DARK
    return outline(grid)


def model(texture):
    return {'parent': 'minecraft:item/generated', 'textures': {'layer0': 'the_oldest_house:item/' + texture}}


def overrides():
    """Later entries win; each count's clench entries follow its base, so a higher count's entries win over a lower's."""
    out = []
    for cmd in (1, 2, 3):
        left = 3 - cmd
        out.append({'predicate': {'custom_model_data': cmd}, 'model': 'the_oldest_house:item/withered_paw' + SUFFIX[left]})
        out.append({'predicate': {'custom_model_data': cmd, 'the_oldest_house:clench': 0.25}, 'model': f'the_oldest_house:item/withered_paw_clench_{left}_2'})
        out.append({'predicate': {'custom_model_data': cmd, 'the_oldest_house:clench': 0.5}, 'model': f'the_oldest_house:item/withered_paw_clench_{left}_1'})
        out.append({'predicate': {'custom_model_data': cmd, 'the_oldest_house:clench': 0.75}, 'model': 'the_oldest_house:item/withered_paw' + SUFFIX[left + 1]})
    return out


def main():
    check = '--check' in sys.argv
    wanted = {}
    for left in (2, 1, 0):
        for step in (1, 2):
            name = f'withered_paw_clench_{left}_{step}'
            wanted[ASSETS / f'textures/item/{name}.png'] = frame(left, step)
            wanted[ASSETS / f'models/item/{name}.json'] = json.dumps(model(name), indent=1) + '\n'
    paw = model('withered_paw')
    paw['overrides'] = overrides()
    wanted[ASSETS / 'models/item/withered_paw.json'] = json.dumps(paw, indent=1) + '\n'
    bad = []
    for path, content in wanted.items():
        if isinstance(content, Image.Image):
            if check:
                if not path.exists() or Image.open(path).convert('RGBA').tobytes() != content.tobytes():
                    bad.append(path)
            else:
                content.save(path)
        else:
            if check:
                if not path.exists() or path.read_text() != content:
                    bad.append(path)
            else:
                path.write_text(content)
    if bad:
        raise SystemExit('out of date: ' + ', '.join(str(p.relative_to(ROOT)) for p in bad))
    print(('checked' if check else 'drew') + ' six clench frames, their models and the paw\'s overrides')


if __name__ == '__main__':
    main()
