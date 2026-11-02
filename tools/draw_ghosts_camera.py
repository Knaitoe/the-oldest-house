"""The crew's camera on its tripod in the Barretts' den, drawn at the game's own pixel scale (2 November).

The owner, 2 November 2026, with a screenshot of it: "let's also fix this texture in the ghost house for the recording device". It was
a plain box wrapped in the materials atlas's riveted sheet at four times the native pixel size, so it read as a stone block, with the
transport panel (screen, record button, play and pause) pasted on every side of its lens, and three posts for a tripod.

Now it is a shoulder video camera on a three-legged tripod, every face mapped one texel to a model pixel:

- textures/block/ghosts/camera.png (16x16): the body's two sides (a small screen and buttons on the right, the tape door and the red
  record button on the left), its top with vents, its front with the red tally lamp, the lens glass and the barrel's focus rings;
- textures/block/ghosts/camera_kit.png (16x16): the aluminium tripod legs with their locks and rubber feet, the centre column, the
  head, the pan bar and its grip, the carry handle, the shotgun microphone, the viewfinder and its eyecup, and the battery;
- models/block/ghosts_camera.json: the mesh. The lens points north (the blockstate turns it with the block's facing). Three legs splay
  at 22.5 degrees from the head: one forward under the lens and two to the sides behind it.

Run without arguments to draw; --check verifies the files match.
"""
from pathlib import Path
import io
import json
import sys
from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / 'src/main/resources/assets/the_oldest_house'
BODY = ASSETS / 'textures/block/ghosts/camera.png'
KIT = ASSETS / 'textures/block/ghosts/camera_kit.png'
MODEL = ASSETS / 'models/block/ghosts_camera.json'

PALETTE = {
    'K': (56, 58, 62, 255),      # the housing
    'k': (36, 37, 41, 255),      # seams and shadow
    'g': (86, 89, 93, 255),      # an edge catching the light
    'o': (17, 17, 19, 255),      # black: rims, rubber
    'u': (54, 54, 56, 255),      # rubber, worn
    's': (146, 148, 144, 255),   # bright metal
    'a': (112, 116, 116, 255),   # aluminium
    'A': (88, 92, 94, 255),      # aluminium in shadow
    'b': (98, 100, 98, 255),     # buttons
    'r': (176, 40, 34, 255),     # red
    'R': (226, 76, 56, 255),     # red, lit
    'l': (88, 112, 96, 255),     # the little screen
    'L': (136, 162, 140, 255),   # the screen's lit line
    'G': (18, 24, 36, 255),      # lens glass
    'h': (44, 58, 80, 255),      # glass towards the rim
    'H': (132, 160, 178, 255),   # the glass's highlight
    'w': (196, 192, 178, 255),   # a label
    'm': (80, 82, 82, 255),      # microphone mesh
    '.': (0, 0, 0, 0),
}

# camera.png. Columns 0-9: the right side (rows 0-5; its left edge is the back of the camera) and the left side (rows 6-11; its left
# edge is the front). Columns 10-15: the top (rows 0-9, front first) and the front (rows 10-15, around the lens). Rows 12-15 of
# columns 0-3 are the lens glass, columns 4-8 the barrel's rings.
CAMERA = [
    'gggggggggggKKKKg',
    'KlLlKbKKKsKKKKKK',
    'KllLKKKKKsKKKKKK',
    'KKKKKbKbKsKkKKkK',
    'KkkkkkkkKsKKKKKK',
    'kkkkkkkkkkKKKKKK',
    'ggggggggggKkkkkK',
    'sKkkkkkKKKKKKKKK',
    'sKkKKKkKrKKkkkkK',
    'sKkKKKkKKKgKKKKg',
    'sKkkkkkKbKgggggR',
    'kkkkkkkkkkKssssK',
    'ohhogsgsgkKsKKsK',
    'hGHhKsKsKkKsKKsK',
    'hGGhKsKsKkKssssK',
    'ohhokskskkkkkkkk',
]

# camera_kit.png, painted region by region: (column, row, rows of the region).
KIT_REGIONS = [
    # A leg, top to bottom: the hub's clamp, the upper section, the lock, the lower section, the rubber foot; its end below.
    (0, 0, ['o', 's', 'a', 'a', 'a', 'A', 'o', 'b', 'a', 'a', 'A', 'A', 'u', 'o']), (0, 15, ['o']),
    # The centre column.
    (1, 0, ['a', 'a', 'A', 'A', 'a', 'A', 'A', 'o']),
    # The head: its sides, with the quick-release plate's edge, and its underside.
    (2, 0, ['oooo', 'osso', 'oooo']), (2, 3, ['oooo', 'ouuo', 'ouuo', 'oooo']),
    # The pan bar from above, front first, its grip at the back, and its end; then from the side, grip on the left.
    (6, 0, ['a', 'a', 's', 'a', 'o', 'u']), (6, 6, ['o']), (1, 14, ['ouasaa']),
    # The carry handle: its ends, its side and its top.
    (7, 0, ['oo']), (7, 1, ['ouuuuo']), (7, 2, ['oo', 'uu', 'uu', 'uu', 'uu', 'oo']),
    # The microphone from the side (windscreen on the right), its two ends, and from above (windscreen first).
    (9, 2, ['ommmmuu']), (9, 3, ['uo']), (13, 8, ['u', 'u', 'm', 'm', 'm', 'm', 'o']),
    # The viewfinder from the side (eyecup on the right), from above (eyecup last), its front and its eyecup.
    (9, 4, ['kkkku', 'oooou']), (14, 4, ['kk', 'kk', 'kk', 'kk', 'uu']), (9, 6, ['ok', 'oo']), (11, 6, ['uu', 'uu']),
    # The battery: its back with a label, its sides and its top.
    (2, 8, ['kwwk', 'kkkk', 'kKKk', 'kkkk']), (6, 8, ['kk', 'kk', 'kk', 'kk']), (2, 12, ['kkkk', 'kkkk']),
]


def kit_rows():
    grid = [['.'] * 16 for _ in range(16)]
    for x, y, rows in KIT_REGIONS:
        for dy, row in enumerate(rows):
            for dx, ch in enumerate(row):
                assert grid[y + dy][x + dx] == '.', ('overlap', x + dx, y + dy)
                grid[y + dy][x + dx] = ch
    return [''.join(r) for r in grid]


def picture(rows):
    image = Image.new('RGBA', (16, 16))
    for y, row in enumerate(rows):
        assert len(row) == 16, (y, row)
        for x, ch in enumerate(row):
            image.putpixel((x, y), PALETTE[ch])
    return image


def face(texture, uv, rotation=0):
    out = {'texture': '#' + texture, 'uv': list(uv)}
    if rotation:
        out['rotation'] = rotation
    return out


def element(a, b, faces, rotation=None):
    out = {'from': list(a), 'to': list(b), 'faces': faces}
    if rotation:
        out['rotation'] = rotation
    return out


def model():
    kit, body = 'kit', 'body'
    leg_faces = {f: face(kit, (0, 0, 1, 14)) for f in ('north', 'south', 'east', 'west')}
    leg_faces.update({f: face(kit, (0, 15, 1, 16)) for f in ('up', 'down')})
    elements = [
        # The legs, each fourteen pixels long from the head's hub, splayed so their rubber feet rest on the floor.
        element((7.5, -1, 7.5), (8.5, 13, 8.5), leg_faces, {'origin': [8, 13, 8], 'axis': 'x', 'angle': 22.5}),
        element((7.5, -1, 9), (8.5, 13, 10), leg_faces, {'origin': [8, 13, 9.5], 'axis': 'z', 'angle': 22.5}),
        element((7.5, -1, 9), (8.5, 13, 10), leg_faces, {'origin': [8, 13, 9.5], 'axis': 'z', 'angle': -22.5}),
        # The centre column, from the head down between the legs.
        element((7.5, 5, 7.5), (8.5, 13, 8.5), {**{f: face(kit, (1, 0, 2, 8)) for f in ('north', 'south', 'east', 'west')},
                                               'down': face(kit, (0, 15, 1, 16))}),
        # The head, and the pan bar reaching back from it and down towards the operator's hand.
        element((6, 13, 6), (10, 16, 10), {**{f: face(kit, (2, 0, 6, 3)) for f in ('north', 'south', 'east', 'west')},
                                           'down': face(kit, (2, 3, 6, 7))}),
        element((7.5, 14, 10), (8.5, 15, 16), {'up': face(kit, (6, 0, 7, 6)), 'down': face(kit, (6, 0, 7, 6)),
                                               'east': face(kit, (1, 14, 7, 15)), 'west': face(kit, (7, 14, 1, 15)),
                                               'south': face(kit, (6, 6, 7, 7))},
                {'origin': [8, 14.5, 10], 'axis': 'x', 'angle': 22.5}),
        # The camera's body.
        element((5, 16, 3), (11, 22, 13), {'east': face(body, (0, 0, 10, 6)), 'west': face(body, (0, 6, 10, 12)),
                                           'up': face(body, (10, 0, 16, 10)), 'down': face(body, (10, 0, 16, 10)),
                                           'north': face(body, (10, 10, 16, 16)), 'south': face(body, (10, 0, 16, 6))}),
        # The lens: its glass at the front, focus rings round the barrel.
        element((6, 17, -2), (10, 21, 3), {'north': face(body, (0, 12, 4, 16)),
                                           'east': face(body, (4, 12, 9, 16)), 'west': face(body, (4, 12, 9, 16)),
                                           'up': face(body, (4, 12, 9, 16), 90), 'down': face(body, (4, 12, 9, 16), 90)}),
        # The viewfinder on the left, its eyecup towards the back.
        element((3, 20, 4), (5, 22, 9), {'east': face(kit, (14, 4, 9, 6)), 'west': face(kit, (9, 4, 14, 6)),
                                         'up': face(kit, (14, 4, 16, 9)), 'down': face(kit, (14, 4, 16, 9)),
                                         'north': face(kit, (9, 6, 11, 8)), 'south': face(kit, (11, 6, 13, 8))}),
        # The carry handle: two feet and the bar across them.
        element((7, 22, 5), (9, 23, 6), {'north': face(kit, (7, 0, 9, 1)), 'south': face(kit, (7, 0, 9, 1)), 'east': face(kit, (7, 0, 8, 1)), 'west': face(kit, (7, 0, 8, 1))}),
        element((7, 22, 10), (9, 23, 11), {'north': face(kit, (7, 0, 9, 1)), 'south': face(kit, (7, 0, 9, 1)), 'east': face(kit, (7, 0, 8, 1)), 'west': face(kit, (7, 0, 8, 1))}),
        element((7, 23, 5), (9, 24, 11), {'north': face(kit, (7, 0, 9, 1)), 'south': face(kit, (7, 0, 9, 1)),
                                          'east': face(kit, (7, 1, 13, 2)), 'west': face(kit, (7, 1, 13, 2)),
                                          'up': face(kit, (7, 2, 9, 8)), 'down': face(kit, (7, 2, 9, 8))}),
        # The shotgun microphone along the top, its windscreen out over the lens.
        element((9, 22, -1), (10, 23, 6), {'east': face(kit, (9, 2, 16, 3)), 'west': face(kit, (16, 2, 9, 3)),
                                               'up': face(kit, (13, 8, 14, 15)), 'down': face(kit, (13, 8, 14, 15)),
                                               'north': face(kit, (9, 3, 10, 4)), 'south': face(kit, (10, 3, 11, 4))}),
        # The battery on the back.
        element((6, 17, 13), (10, 21, 15), {'south': face(kit, (2, 8, 6, 12)), 'east': face(kit, (6, 8, 8, 12)),
                                            'west': face(kit, (6, 8, 8, 12)), 'up': face(kit, (2, 12, 6, 14)),
                                            'down': face(kit, (2, 12, 6, 14))}),
    ]
    return {'textures': {'body': 'the_oldest_house:block/ghosts/camera', 'kit': 'the_oldest_house:block/ghosts/camera_kit',
                         'particle': 'the_oldest_house:block/ghosts/camera'},
            'render_type': 'minecraft:cutout', 'elements': elements, 'ambientocclusion': False}


def files():
    return {BODY: picture(CAMERA), KIT: picture(kit_rows()), MODEL: (json.dumps(model(), indent=2) + '\n').encode()}


def main():
    check = '--check' in sys.argv
    for path, want in files().items():
        if isinstance(want, Image.Image):
            if check:
                assert path.exists() and Image.open(path).convert('RGBA').tobytes() == want.tobytes(), f'{path} differs from its drawing'
            else:
                buffer = io.BytesIO()
                want.save(buffer, format='PNG')
                path.write_bytes(buffer.getvalue())
        elif check:
            assert path.exists() and path.read_bytes() == want, f'{path} differs'
        else:
            path.write_bytes(want)
    print('Verified the crew\'s camera.' if check else 'Drew the crew\'s camera.')


if __name__ == '__main__':
    main()
