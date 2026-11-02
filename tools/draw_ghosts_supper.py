"""The Barretts' last supper drawn as pixel art (0.4.242).

The owner, 2 November 2026: "the final meal should be spaghetti with red sauce. merry's spaghetti doesn't have sauce". The day
seven plates showed sandstone for noodles and a square cut from a generated sheet for the sauce. They are drawn here at the game's
own scale, one texel to a model pixel:

- textures/block/ghosts/supper.png (16x16): the plate's top (0,0)-(10,10) with its rim band, the tangle of spaghetti seen from
  above (10,0)-(16,6) and from the side (10,6)-(16,8), the sauce from above (0,10)-(5,15) and its edge (0,15)-(5,16), the plate's
  edge (5,10)-(15,11);
- models/block/ghosts_meal_spaghetti.json: the family's plates, a nest of spaghetti with red sauce on top;
- models/block/ghosts_meal_spaghetti_plain.json: Merry's plate, the same spaghetti with nothing on it;
- the eight variants of kind=meal_spaghetti and kind=meal_spaghetti_plain in blockstates/ghosts_set.json.

Run without arguments to draw; --check verifies the files match.
"""
from pathlib import Path
import json
import sys
from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / 'src/main/resources/assets/the_oldest_house'
TEXTURE = ASSETS / 'textures/block/ghosts/supper.png'
MODELS = ASSETS / 'models/block'
STATES = ASSETS / 'blockstates/ghosts_set.json'

PALETTE = {
    # the plate
    'w': (238, 234, 224, 255), 's': (214, 209, 197, 255), 'b': (150, 176, 184, 255), 'e': (198, 192, 180, 255),
    # spaghetti
    'L': (246, 226, 160, 255), 'M': (228, 200, 120, 255), 'D': (194, 160, 82, 255), 'K': (168, 134, 66, 255),
    # sauce
    'R': (176, 38, 26, 255), 'r': (138, 24, 18, 255), 'o': (212, 70, 42, 255), 'c': (118, 20, 16, 255), 'g': (86, 112, 50, 255),
}

PLATE = [
    'ssssssssss',
    'sbbbbbbbbs',
    'sbwwwwwwbs',
    'sbwwwwwwbs',
    'sbwwwwwsbs',
    'sbwwwwwsbs',
    'sbwwwwssbs',
    'sbwwssssbs',
    'sbbbbbbbbs',
    'ssssssssss',
]
NOODLE_TOP = [
    'MLDMLM',
    'DMLDML',
    'LDMMLD',
    'MLLDMK',
    'DMDLLD',
    'KMLDML',
]
NOODLE_SIDE = [
    'LMDLMD',
    'DKMDKM',
]
SAUCE_TOP = [
    'rRRRr',
    'RoRcR',
    'RRgRR',
    'RcRoR',
    'rRRcr',
]
SAUCE_SIDE = ['rcrRr']
PLATE_EDGE = ['eeeeeeeeee']


def texture():
    img = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    for rows, (ox, oy) in ((PLATE, (0, 0)), (NOODLE_TOP, (10, 0)), (NOODLE_SIDE, (10, 6)), (SAUCE_TOP, (0, 10)),
                           (SAUCE_SIDE, (0, 15)), (PLATE_EDGE, (5, 10))):
        for y, row in enumerate(rows):
            for x, ch in enumerate(row):
                img.putpixel((ox + x, oy + y), PALETTE[ch])
    return img


T = '#supper'


def element(frm, to, up, side, down=None):
    """A box whose top shows `up` and whose four sides and bottom show strips of `side`, each in texture pixels."""
    faces = {'up': {'texture': T, 'uv': up}, 'down': {'texture': T, 'uv': down or up}}
    for f in ('north', 'south', 'east', 'west'):
        faces[f] = {'texture': T, 'uv': side}
    return {'from': frm, 'to': to, 'faces': faces}


def plate():
    edge = [5, 10, 15, 11]
    parts = [element([3, 0, 3], [13, 0.8, 13], [0, 0, 10, 10], edge)]
    # The rim: four low walls, their tops cut from the plate's own rim in the texture.
    for frm, to in (([3, 0.8, 3], [13, 1.5, 4]), ([3, 0.8, 12], [13, 1.5, 13]), ([3, 0.8, 4], [4, 1.5, 12]), ([12, 0.8, 4], [13, 1.5, 12])):
        up = [frm[0] - 3, frm[2] - 3, to[0] - 3, to[2] - 3]
        parts.append(element(frm, to, up, edge))
    return parts


def spaghetti():
    side = [10, 6, 16, 7]
    parts = [
        element([5, 0.8, 5], [11, 1.8, 11], [10, 0, 16, 6], side),
        element([5.5, 1.8, 5.5], [10.5, 2.4, 10.5], [10.5, 0.5, 15.5, 5.5], [10, 7, 15, 8]),
    ]
    # A few strands slipping over the edge of the nest onto the plate.
    for frm, to in (([4.2, 0.8, 7], [5, 1.2, 7.5]), ([11, 0.8, 8.5], [11.8, 1.2, 9]), ([7.5, 0.8, 4.2], [8, 1.2, 5]), ([6, 0.8, 11], [6.5, 1.2, 11.8])):
        parts.append(element(frm, to, [11, 1, 12, 2], [10, 7, 11, 8]))
    return parts


def sauce():
    return [
        element([6, 2.4, 6.5], [10, 2.8, 10.5], [0.5, 10.5, 4.5, 14.5], [0, 15, 4, 16]),
        element([7, 2.8, 7.5], [9, 3.0, 9.5], [1.5, 11.5, 3.5, 13.5], [0, 15, 2, 16]),
        # The sauce runs down one side of the nest.
        element([9.2, 1.8, 8], [10.4, 2.4, 9.4], [3, 12, 4.2, 13.4], [0, 15, 1.2, 16]),
    ]


def model(with_sauce):
    elements = plate() + spaghetti() + (sauce() if with_sauce else [])
    return {
        'textures': {'supper': 'the_oldest_house:block/ghosts/supper', 'particle': 'the_oldest_house:block/ghosts/supper'},
        'render_type': 'minecraft:cutout',
        'ambientocclusion': False,
        'elements': elements,
    }


def files():
    out = {
        MODELS / 'ghosts_meal_spaghetti.json': json.dumps(model(True), indent=2) + '\n',
        MODELS / 'ghosts_meal_spaghetti_plain.json': json.dumps(model(False), indent=2) + '\n',
    }
    states = json.loads(STATES.read_text())['variants']
    merged = {}
    added = False
    for key, value in states.items():
        if key.startswith('kind=meal_spaghetti'):
            continue
        merged[key] = value
        if key == 'kind=meal_potatoes,facing=west':
            added = True
            for name in ('meal_spaghetti', 'meal_spaghetti_plain'):
                for facing, y in (('north', 0), ('east', 90), ('south', 180), ('west', 270)):
                    merged[f'kind={name},facing={facing}'] = {'model': 'the_oldest_house:block/ghosts_' + name, 'y': y}
    if not added:
        raise SystemExit('ghosts_set.json has no meal_potatoes variant to follow')
    out[STATES] = json.dumps({'variants': merged}, indent=2) + '\n'
    return out


def main():
    check = '--check' in sys.argv
    img = texture()
    written = files()
    if check:
        bad = []
        if not TEXTURE.exists() or Image.open(TEXTURE).convert('RGBA').tobytes() != img.tobytes():
            bad.append(TEXTURE)
        for path, text in written.items():
            if not path.exists() or path.read_text() != text:
                bad.append(path)
        if bad:
            raise SystemExit('Out of date (run tools/draw_ghosts_supper.py): ' + ', '.join(str(p.relative_to(ROOT)) for p in bad))
        print('Ghosts supper verified')
        return
    TEXTURE.parent.mkdir(parents=True, exist_ok=True)
    img.save(TEXTURE)
    for path, text in written.items():
        path.write_text(text)
    print('Drew the Ghosts supper: one texture, two plates, eight variants')


if __name__ == '__main__':
    main()
