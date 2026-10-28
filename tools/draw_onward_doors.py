"""Forward doors: five fitted native door halves for the scenes' new ways on, plus their models and states."""
import json
import sys
from pathlib import Path
from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / 'src/main/resources/assets/the_oldest_house'
DOORS = ('weathered', 'panel', 'cabin', 'service', 'cellar')
FACING_Y = {'east': 0, 'south': 90, 'west': 180, 'north': 270}


def boards(d, colors, gap, top, width=4):
    for i, x in enumerate(range(0, 16, width)):
        d.rectangle((x, 0, x + width - 1, 15), fill=colors[i % len(colors)])
        d.line((x, 0, x, 15), fill=gap)
        d.line((x + 1, 0, x + 1, 15), fill=top)


def weathered():
    halves = []
    for half in ('lower', 'upper'):
        im = Image.new('RGBA', (16, 16)); d = ImageDraw.Draw(im)
        boards(d, ('#8b857a', '#7a746a', '#948e83', '#827c71'), '#4a463f', '#a19b90')
        # Grain, knots and the brace that ties the boards.
        for x, y in ((6, 3), (10, 9), (3, 12), (14, 6)):
            d.point((x, y), fill='#5e5950')
        brace = 4 if half == 'lower' else 10
        d.rectangle((0, brace, 15, brace + 1), fill='#6d675d'); d.line((0, brace + 2, 15, brace + 2), fill='#4a463f')
        for y in ((2, 12) if half == 'lower' else (3, 13)):
            d.line((0, y, 5, y), fill='#5c3b27'); d.point((1, y), fill='#94593a'); d.point((4, y), fill='#94593a')
        if half == 'lower':
            d.rectangle((13, 0, 14, 2), fill='#3b3a37'); d.point((13, 1), fill='#8f8a80')
        d.line((0, 15 if half == 'lower' else 0, 15, 15 if half == 'lower' else 0), fill='#4a463f')
        halves.append(im)
    return halves


def panel():
    halves = []
    for half in ('lower', 'upper'):
        im = Image.new('RGBA', (16, 16), '#d6cdb5'); d = ImageDraw.Draw(im)
        d.rectangle((0, 0, 15, 15), outline='#a89d84')
        # Two raised panels per half, lit from the upper left.
        for x0 in (2, 9):
            y0, y1 = (2, 13) if half == 'lower' else (3, 12)
            d.rectangle((x0, y0, x0 + 4, y1), fill='#c8bea4')
            d.line((x0, y0, x0 + 4, y0), fill='#efe8d4'); d.line((x0, y0, x0, y1), fill='#efe8d4')
            d.line((x0, y1, x0 + 4, y1), fill='#9f947c'); d.line((x0 + 4, y0, x0 + 4, y1), fill='#9f947c')
        if half == 'lower':
            d.rectangle((12, 0, 13, 1), fill='#c9a24a'); d.point((12, 0), fill='#ecd18a'); d.point((13, 1), fill='#8c6b2a')
            d.line((1, 15, 14, 15), fill='#8e846d')
        else:
            d.point((12, 15), fill='#8c6b2a')
        halves.append(im)
    return halves


def cabin():
    halves = []
    for half in ('lower', 'upper'):
        im = Image.new('RGBA', (16, 16)); d = ImageDraw.Draw(im)
        boards(d, ('#5d4232', '#4f382a', '#664a38', '#563d2e'), '#2e2119', '#74563f', width=4)
        y = 11 if half == 'lower' else 4
        d.rectangle((0, y, 15, y + 1), fill='#3f2c20'); d.line((0, y + 2, 15, y + 2), fill='#2a1d15')
        # The diagonal of the Z brace runs up across both halves.
        for i in range(16):
            yy = (10 - i) if half == 'lower' else (15 - i)
            if 0 <= yy < 16 and (half == 'lower' and yy < y or half == 'upper' and yy > y + 2):
                d.rectangle((i, yy, min(15, i + 1), yy), fill='#3f2c20')
        if half == 'lower':
            d.rectangle((12, 0, 15, 1), fill='#262422'); d.point((13, 2), fill='#262422'); d.point((14, 0), fill='#5d5953')
            d.line((0, 15, 15, 15), fill='#2a1d15')
        halves.append(im)
    return halves


def service():
    halves = []
    for half in ('lower', 'upper'):
        im = Image.new('RGBA', (16, 16), '#7c8a82'); d = ImageDraw.Draw(im)
        d.rectangle((0, 0, 15, 15), outline='#5b6862')
        d.line((1, 1, 1, 14), fill='#94a29a')
        for x, y in ((4, 6), (11, 10), (7, 13)):
            d.point((x, y), fill='#6f7d76')
        if half == 'lower':
            d.rectangle((10, 0, 13, 3), fill='#b6bcb8'); d.rectangle((10, 0, 13, 3), outline='#8b928e')
            d.line((2, 12, 13, 12), fill='#6a7770'); d.line((2, 15, 13, 15), fill='#4c5752')
        else:
            # Wired glass, translucent through the cutout layer only at its clear corners.
            d.rectangle((4, 3, 11, 10), fill=(150, 180, 186, 255))
            d.rectangle((4, 3, 11, 10), outline='#54605b')
            for x in (6, 9):
                d.line((x, 4, x, 9), fill='#7d9196')
            for yy in (5, 8):
                d.line((5, yy, 10, yy), fill='#7d9196')
            d.point((5, 4), fill=(200, 222, 226, 255))
        halves.append(im)
    return halves


def cellar():
    halves = []
    for half in ('lower', 'upper'):
        im = Image.new('RGBA', (16, 16)); d = ImageDraw.Draw(im)
        boards(d, ('#6a5137', '#5a442d', '#715840'), '#33271b', '#80664a', width=5)
        for y in ((2, 11) if half == 'lower' else (4, 13)):
            d.rectangle((0, y, 15, y + 1), fill='#393735'); d.line((0, y + 2, 15, y + 2), fill='#24221f')
            for x in (2, 7, 12):
                d.point((x, y), fill='#77726b')
        if half == 'lower':
            d.ellipse((11, 5, 14, 8), outline='#2a2825'); d.point((12, 5), fill='#6c6760')
            d.line((0, 15, 15, 15), fill='#24221f')
        halves.append(im)
    return halves


def models(name, files):
    parts = {'lower': 'bottom', 'upper': 'top'}
    for half, vanilla in parts.items():
        for hinge in ('left', 'right'):
            for opened in ('', '_open'):
                model = {
                    'parent': f'minecraft:block/door_{vanilla}_{hinge}{opened}',
                    'render_type': 'minecraft:cutout',
                    'textures': {
                        'top': f'the_oldest_house:block/onward/{name}_upper',
                        'bottom': f'the_oldest_house:block/onward/{name}_lower',
                    },
                }
                files[ASSETS / f'models/block/onward_{name}_door_{half}_{hinge}{opened}.json'] = json.dumps(model, indent=2) + '\n'
    variants = {}
    for facing, y in FACING_Y.items():
        for half in ('lower', 'upper'):
            for hinge in ('left', 'right'):
                for opened in (False, True):
                    turn = y + (90 if hinge == 'left' else -90) if opened else y
                    variants[f'facing={facing},half={half},hinge={hinge},open={str(opened).lower()}'] = {
                        'model': f'the_oldest_house:block/onward_{name}_door_{half}_{hinge}{"_open" if opened else ""}',
                        'y': turn % 360,
                    }
    files[ASSETS / f'blockstates/onward_{name}_door.json'] = json.dumps({'variants': variants}, indent=2) + '\n'


def main():
    check = '--check' in sys.argv[1:]
    out = ASSETS / 'textures/block/onward'
    painters = {'weathered': weathered, 'panel': panel, 'cabin': cabin, 'service': service, 'cellar': cellar}
    images, files = {}, {}
    for name in DOORS:
        lower, upper = painters[name]()
        images[out / f'{name}_lower.png'] = lower
        images[out / f'{name}_upper.png'] = upper
        models(name, files)
    if check:
        for path, image in images.items():
            with Image.open(path) as saved:
                assert saved.size == (16, 16) and saved.convert('RGBA').tobytes() == image.tobytes(), path
        for path, text in files.items():
            assert path.read_text() == text, path
        print(f'{len(images)} door halves and {len(files)} models and states match')
        return
    out.mkdir(parents=True, exist_ok=True)
    for path, image in images.items():
        image.save(path)
    for path, text in files.items():
        path.write_text(text)


if __name__ == '__main__':
    main()
