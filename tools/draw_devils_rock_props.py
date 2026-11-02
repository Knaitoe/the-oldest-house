"""Devil's Rock's props drawn as pixel art (0.4.241).

The owner, 2 November 2026: "try to remove obviously ai-textures from devils rock". The bear on Kate's floor, the MISSING
poster in its frame and the cassette on the bureau were cut from a generated sheet (tools/generate_review_assets.py): 64x64
crops of a painted image, every face of every box squeezing the whole picture. They are drawn here instead, pixel by pixel, at
the game's own scale, and each model face takes its own part of its texture:

- review_bear.png (16x16) and models/block/review_bear.json: a sitting bear with a face only on its front, a tan muzzle and
  belly, paw pads and ears;
- review_missing_notice.png (32x32) and models/block/review_missing_notice.json: a photocopied MISSING poster, the boy's
  photograph in grey, lines of type and tape at the corners, on a board;
- review_cassette.png (16x16) and models/block/review_cassette.json: a cassette lying flat, its label, window and reels;
- review_reading_wood.png and review_pier.png (16x16): plain stained boards and weathered boards, which the other generated
  props (the notice and map boards, the chess board, the diary stack, the flashlight) also use.

art/inventory/devils_rock_props_0241.json records the four replaced historical PNGs, so tools/verify_character_art.py checks
each against its new drawing instead of its 0.4.100 hash. Run without arguments to draw; --check verifies the files match.
"""
from pathlib import Path
import hashlib
import json
import sys
from PIL import Image

sys.path.insert(0, str(Path(__file__).resolve().parent))
import devils_rock_household as household  # noqa: E402

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / 'src/main/resources/assets/the_oldest_house'
TEX = ASSETS / 'textures/block'
MODELS = ASSETS / 'models/block'
RECORD = ROOT / 'art/inventory/devils_rock_props_0241.json'
NS = 'the_oldest_house:block/'


def hashed(x, y, salt):
    h = (x * 73856093) ^ (y * 19349663) ^ (salt * 83492791)
    h ^= h >> 13
    h = (h * 0x5bd1e995) & 0xffffffff
    return (h ^ (h >> 15)) & 0xffff


# ---------------------------------------------------------------------------------------------- textures

FUR, FUR_D, FUR_L = (124, 86, 56, 255), (98, 66, 42, 255), (146, 104, 68, 255)
TAN, TAN_D = (200, 164, 118, 255), (168, 132, 92, 255)
BLACK = (30, 24, 22, 255)


def bear():
    img = Image.new('RGBA', (16, 16), FUR)
    for y in range(16):
        for x in range(16):
            n = hashed(x, y, 1) % 9
            img.putpixel((x, y), FUR_D if n < 2 else FUR_L if n == 8 else FUR)
    # The face (0,0)-(7,6): two eyes, and the lower middle tan where the muzzle sits.
    for x, y in ((1, 2), (5, 2)):
        img.putpixel((x, y), BLACK)
    for x in range(2, 5):
        for y in range(4, 6):
            img.putpixel((x, y), TAN_D)
    # The muzzle's front (7,0)-(10,2): the nose above, the mouth under it; its other faces (7,2)-(10,4) plain tan.
    for x in range(7, 10):
        img.putpixel((x, 0), TAN)
        img.putpixel((x, 1), TAN)
        img.putpixel((x, 2), TAN)
        img.putpixel((x, 3), TAN_D if x == 9 else TAN)
    img.putpixel((8, 0), BLACK)
    img.putpixel((8, 1), TAN_D)
    # Paw pads (10,0)-(13,3) and the inner ear (13,0)-(15,2).
    for x in range(10, 13):
        for y in range(0, 3):
            img.putpixel((x, y), TAN_D if (x, y) == (11, 1) else TAN)
    for x in range(13, 15):
        for y in range(0, 2):
            img.putpixel((x, y), TAN_D)
    # The belly (8,9)-(16,16): a worn tan patch in the middle of the front.
    for x in range(8, 16):
        for y in range(9, 16):
            dx, dy = x - 11.5, y - 12.5
            if dx * dx / 6.5 + dy * dy / 6.0 <= 1:
                img.putpixel((x, y), TAN_D if hashed(x, y, 2) % 5 == 0 else TAN)
    return img


PAPER, PAPER_D, PAPER_E = (222, 216, 198, 255), (206, 198, 178, 255), (186, 178, 158, 255)
INK, INK_L, RED = (62, 58, 54, 255), (118, 112, 104, 255), (164, 38, 34, 255)
TAPE, TAPE_D = (230, 222, 172, 255), (208, 198, 146, 255)
WOOD, WOOD_D, WOOD_L = (92, 62, 40, 255), (72, 47, 30, 255), (112, 78, 50, 255)
GLYPHS = {
    'M': ['10001', '11011', '10101', '10001', '10001'],
    'I': ['1', '1', '1', '1', '1'],
    'S': ['111', '100', '111', '001', '111'],
    'N': ['1001', '1101', '1011', '1001', '1001'],
    'G': ['111', '100', '101', '101', '111'],
}


def notice():
    img = Image.new('RGBA', (32, 32), WOOD)
    x0, y0, x1, y1 = 2, 2, 30, 30
    for y in range(y0, y1):
        for x in range(x0, x1):
            n = hashed(x, y, 3) % 23
            img.putpixel((x, y), PAPER_D if n < 2 else PAPER)
    # An old fold across the middle and a darker edge where the sun has had it.
    for x in range(x0, x1):
        img.putpixel((x, 17), PAPER_D)
    for y in range(y0, y1):
        img.putpixel((x1 - 1, y), PAPER_E)
    for x in range(x0, x1):
        img.putpixel((x, y1 - 1), PAPER_E)
    # MISSING, in red capitals across the top.
    word = 'MISSING'
    width = sum(len(GLYPHS[c][0]) for c in word) + len(word) - 1
    x = x0 + (x1 - x0 - width) // 2
    for c in word:
        rows = GLYPHS[c]
        for dy, row in enumerate(rows):
            for dx, bit in enumerate(row):
                if bit == '1':
                    img.putpixel((x + dx, 4 + dy, ), RED)
        x += len(rows[0]) + 1
    # The photograph, photocopied grey: a boy's head and shoulders, hair falling over his forehead.
    px0, py0, px1, py1 = 10, 11, 22, 23
    for y in range(py0, py1):
        for x in range(px0, px1):
            edge = x in (px0, px1 - 1) or y in (py0, py1 - 1)
            img.putpixel((x, y), (54, 52, 50, 255) if edge else (142, 140, 136, 255) if hashed(x, y, 4) % 7 else (128, 126, 122, 255))
    hair, skin, shade, shirt = (58, 56, 54, 255), (186, 182, 176, 255), (160, 156, 150, 255), (92, 90, 88, 255)
    face = ['..hhhhhh..', '.hhhhhhhh.', '.hhhhhhhh.', '.hssssssh.', '.ssessess.', '.ssssssss.', '..ssmmss..', '...ssss...',
            '..tdssdt..', 'tttttttttt']
    for dy, row in enumerate(face):
        for dx, ch in enumerate(row):
            c = {'h': hair, 's': skin, 'e': (40, 38, 38, 255), 'm': shade, 'd': shade, 't': shirt}.get(ch)
            if c:
                img.putpixel((px0 + 1 + dx, py0 + 1 + dy), c)
    # Lines of type under the picture: the name in heavier ink, then the details, then a number to ring.
    for y, (a, b), ink in ((25, (6, 26), INK), (27, (5, 27), INK_L)):
        for x in range(a, b):
            if hashed(x, y, 5) % 6 != 0 or ink == INK:
                img.putpixel((x, y), ink)
    for x in (4, 5, 6, 7):
        img.putpixel((x, 9), INK_L)
    for x in (24, 25, 26, 27):
        img.putpixel((x, 9), INK_L)
    # Tape at the two top corners.
    for cx in (x0, x1 - 4):
        for dx in range(4):
            for dy in range(2):
                img.putpixel((cx + dx, y0 + dy), TAPE_D if dy == 1 else TAPE)
    return img


def reading_wood():
    img = Image.new('RGBA', (16, 16), WOOD)
    for y in range(16):
        streak = hashed(0, y, 6) % 4
        for x in range(16):
            n = hashed(x // 3, y, 7) % 11
            c = WOOD_D if n == 0 or (streak == 0 and hashed(x, y, 8) % 3 == 0) else WOOD_L if n == 10 else WOOD
            img.putpixel((x, y), c)
    for x in range(16):
        img.putpixel((x, 7), WOOD_D)
    return img


GREY, GREY_D, GREY_L = (128, 118, 104, 255), (100, 92, 80, 255), (150, 140, 124, 255)


def pier():
    img = Image.new('RGBA', (16, 16), GREY)
    for y in range(16):
        for x in range(16):
            n = hashed(x, y // 2, 9) % 9
            img.putpixel((x, y), GREY_D if n == 0 else GREY_L if n == 8 else GREY)
    for y in (3, 7, 11, 15):
        for x in range(16):
            img.putpixel((x, y), GREY_D)
    for x, y in ((2, 1), (13, 5), (6, 9), (10, 13)):
        img.putpixel((x, y), (70, 64, 56, 255))
    return img


SHELL, SHELL_D, SHELL_L = (54, 54, 60, 255), (38, 38, 44, 255), (74, 74, 82, 255)
LABEL, LABEL_D = (226, 214, 180, 255), (196, 182, 146, 255)


def cassette():
    img = Image.new('RGBA', (16, 16), SHELL)
    # The top (0,0)-(10,6): a label with a pencilled line, the window with its two reels, a screw at each corner.
    for x in range(10):
        img.putpixel((x, 0), SHELL_L)
        img.putpixel((x, 1), LABEL)
        img.putpixel((x, 2), LABEL)
        img.putpixel((x, 5), SHELL_L if x % 9 else SHELL_D)
    for x in range(2, 8):
        img.putpixel((x, 1), (70, 82, 128, 255) if hashed(x, 1, 10) % 3 else LABEL)
    img.putpixel((0, 1), LABEL_D)
    img.putpixel((9, 1), LABEL_D)
    for x in range(1, 9):
        img.putpixel((x, 3), (22, 22, 26, 255))
        img.putpixel((x, 4), (22, 22, 26, 255))
    for x in (2, 6):
        img.putpixel((x, 3), (200, 200, 196, 255))
        img.putpixel((x + 1, 4), (200, 200, 196, 255))
        img.putpixel((x + 1, 3), (96, 64, 40, 255))
        img.putpixel((x, 4), (96, 64, 40, 255))
    # The front edge (0,6)-(10,8): the openings where the tape shows.
    for x in range(10):
        img.putpixel((x, 6), SHELL_D)
        img.putpixel((x, 7), (96, 64, 40, 255) if x in (3, 4, 5, 6) else SHELL_D if x in (1, 8) else SHELL)
    # Plain plastic for the rest.
    for y in range(8, 16):
        for x in range(16):
            img.putpixel((x, y), SHELL_D if hashed(x, y, 11) % 13 == 0 else SHELL)
    return img


FRAME, FRAME_L = (70, 48, 32, 255), (96, 66, 44, 255)
SKIN, HAIR_E, HAIR_T, HAIR_K = (222, 186, 150, 255), (82, 54, 36, 255), (42, 33, 28, 255), (132, 92, 52, 255)


def framed(paint):
    """A photograph in a dark frame: the picture in (3,4)-(13,12), its frame round it, plain frame wood everywhere else."""
    img = Image.new('RGBA', (16, 16), FRAME)
    for x in range(2, 14):
        img.putpixel((x, 3), FRAME_L)
    paint(img)
    return img


def photo_family():
    """The three of them at a lake one summer: Elizabeth, Tommy, and Kate, smallest, on the sand."""
    def paint(img):
        for x in range(3, 13):
            for y in range(4, 12):
                c = (150, 190, 220, 255) if y < 7 else (64, 92, 62, 255) if y == 7 else (92, 132, 162, 255) if y < 10 else (204, 184, 142, 255)
                img.putpixel((x, y), c)
        people = [(5, 6, [HAIR_E, SKIN, (170, 64, 60, 255), (170, 64, 60, 255), (62, 72, 112, 255), (62, 72, 112, 255)]),
                  (8, 7, [HAIR_T, SKIN, (64, 112, 170, 255), (52, 60, 70, 255), (52, 60, 70, 255)]),
                  (10, 8, [HAIR_K, SKIN, (214, 184, 64, 255), (214, 184, 64, 255)])]
        for x, top, rows in people:
            for i, c in enumerate(rows):
                img.putpixel((x, top + i), c)
                if x != 10:
                    img.putpixel((x + 1, top + i), c)
    return framed(paint)


def photo_tommy():
    """His school photograph: head and shoulders on the mottled blue they all have."""
    rows = ['bbbhhhhbbb', 'bbhhhhhhbb', 'bbhsssshbb', 'bbsessesbb', 'bbssssssbb', 'bbbsmmsbbb', 'bttwsswttb', 'tttttttttt']
    def paint(img):
        for dy, row in enumerate(rows):
            for dx, ch in enumerate(row):
                x, y = 3 + dx, 4 + dy
                c = {'b': (92, 112, 152, 255) if hashed(x, y, 12) % 3 else (104, 124, 164, 255), 'h': HAIR_T, 's': SKIN,
                     'e': (36, 30, 28, 255), 'm': (196, 152, 124, 255), 'w': (236, 236, 228, 255), 't': (52, 74, 120, 255)}[ch]
                img.putpixel((x, y), c)
    return framed(paint)


def photo_siblings():
    """A snapshot in a stand-up frame: Kate and Tommy pulling faces at whoever held the camera."""
    img = Image.new('RGBA', (16, 16), FRAME)
    rows = ['kkktttt', 'kkkhttt', 'eseeset', 'ssssssss', 'spsssds', 'yyybbbb']
    pic = Image.new('RGBA', (6, 6), (180, 176, 168, 255))
    for dy, row in enumerate(rows):
        for dx, ch in enumerate(row[:6]):
            c = {'k': HAIR_K, 't': HAIR_T, 'h': HAIR_T, 's': SKIN, 'e': (36, 30, 28, 255), 'p': (214, 120, 124, 255),
                 'd': (120, 70, 60, 255), 'y': (214, 184, 64, 255), 'b': (64, 112, 170, 255)}[ch]
            pic.putpixel((dx, dy), c)
    img.paste(pic, (1, 1))
    for x in range(8):
        img.putpixel((x, 0), FRAME_L)
    return img


# ---------------------------------------------------------------------------------------------- models

def face(tex, uv):
    return {'texture': '#' + tex, 'uv': [round(v, 3) for v in uv]}


def element(frm, to, faces):
    return {'from': list(frm), 'to': list(to), 'faces': faces}


def auto(frm, to, tex, skip=()):
    """Faces mapped one sixteenth to one pixel, from where the box sits in the block (vanilla's automatic mapping, written out)."""
    (x1, y1, z1), (x2, y2, z2) = frm, to
    uv = {'north': (16 - x2, 16 - y2, 16 - x1, 16 - y1), 'south': (x1, 16 - y2, x2, 16 - y1),
          'east': (16 - z2, 16 - y2, 16 - z1, 16 - y1), 'west': (z1, 16 - y2, z2, 16 - y1),
          'up': (x1, z1, x2, z2), 'down': (x1, 16 - z2, x2, 16 - z1)}
    return element(frm, to, {d: face(tex, u) for d, u in uv.items() if d not in skip})


def fur_box(frm, to, front=None, faces_skip=()):
    """A box of the bear: every face takes a same-sized patch of plain fur from (0,6), the front its own art if given."""
    (x1, y1, z1), (x2, y2, z2) = frm, to
    w, h, d = x2 - x1, y2 - y1, z2 - z1
    size = {'north': (w, h), 'south': (w, h), 'east': (d, h), 'west': (d, h), 'up': (w, d), 'down': (w, d)}
    faces = {}
    for k, (a, b) in size.items():
        if k in faces_skip:
            continue
        faces[k] = face('bear', (0, 6, a, 6 + b))
    if front:
        faces['north'] = face('bear', front)
    return element(frm, to, faces)


BEAR_BOXES = [((4, 0, 6), (12, 7, 11)), ((4.5, 0, 3.5), (7, 2.5, 6)), ((9, 0, 3.5), (11.5, 2.5, 6)),
              ((2.5, 2, 7), (4, 6.5, 9.5)), ((12, 2, 7), (13.5, 6.5, 9.5)), ((4.5, 7, 5.5), (11.5, 13, 11)),
              ((6.5, 7.5, 4.5), (9.5, 9.5, 5.5)), ((4.5, 12.5, 7.5), (6.5, 14.5, 8.5)), ((9.5, 12.5, 7.5), (11.5, 14.5, 8.5))]


def bear_model():
    b = BEAR_BOXES
    els = [fur_box(*b[0], front=(8, 9, 16, 16)),
           fur_box(*b[1], front=(10, 0, 12.5, 2.5)), fur_box(*b[2], front=(10, 0, 12.5, 2.5)),
           fur_box(*b[3]), fur_box(*b[4]),
           fur_box(*b[5], front=(0, 0, 7, 6)),
           element(b[6][0], b[6][1], {'north': face('bear', (7, 0, 10, 2)), 'up': face('bear', (7, 2, 10, 3)),
                                      'down': face('bear', (7, 2, 10, 3)), 'east': face('bear', (7, 2, 8, 4)),
                                      'west': face('bear', (9, 2, 10, 4))}),
           fur_box(*b[7], front=(13, 0, 15, 2)), fur_box(*b[8], front=(13, 0, 15, 2))]
    return {'parent': 'minecraft:block/block', 'ambientocclusion': False,
            'textures': {'bear': NS + 'review_bear', 'particle': NS + 'review_bear'}, 'elements': els}


def notice_model():
    board = auto((0.5, 0, 13), (15.5, 16, 16), 'wood')
    paper = element((1, 1, 12.8), (15, 15, 13), {'north': face('notice', (1, 1, 15, 15))})
    return {'parent': 'minecraft:block/block', 'ambientocclusion': False,
            'textures': {'wood': NS + 'review_reading_wood', 'notice': NS + 'review_missing_notice', 'particle': NS + 'review_reading_wood'},
            'elements': [board, paper]}


def cassette_model():
    body = element((3, 0, 4.5), (13, 1.5, 10.5), {
        'up': face('tape', (0, 0, 10, 6)), 'down': face('tape', (0, 8, 10, 14)),
        'north': face('tape', (0, 6, 10, 7.5)), 'south': face('tape', (0, 14, 10, 15.5)),
        'east': face('tape', (10, 8, 16, 9.5)), 'west': face('tape', (10, 10, 16, 11.5))})
    return {'parent': 'minecraft:block/block', 'ambientocclusion': False,
            'textures': {'tape': NS + 'review_cassette', 'particle': NS + 'review_cassette'}, 'elements': [body]}


def wall_photo_model(tex):
    return {'parent': 'minecraft:block/block', 'ambientocclusion': False, 'textures': {'p': NS + tex, 'particle': NS + tex},
            'elements': [auto((2, 3, 15.2), (14, 13, 16), 'p')]}


def stand_photo_model():
    frame = element((4, 0, 8.5), (12, 8, 9.5), {'north': face('p', (0, 0, 8, 8)), 'south': face('p', (8, 8, 16, 16)),
                                               'east': face('p', (8, 0, 9, 8)), 'west': face('p', (9, 0, 10, 8)),
                                               'up': face('p', (0, 8, 8, 9)), 'down': face('p', (0, 9, 8, 10))})
    strut = element((7, 0, 9.5), (9, 6, 11.5), {d: face('p', (10, 0, 12, 2)) for d in ('north', 'south', 'east', 'west', 'up', 'down')})
    return {'parent': 'minecraft:block/block', 'ambientocclusion': False,
            'textures': {'p': NS + 'devils_photo_siblings', 'particle': NS + 'devils_photo_siblings'}, 'elements': [frame, strut]}


# The Sandersons' own photographs, as vignette_detail kinds (0.4.241, the owner: "add more family details to the house... give it
# the same lived-in nature as the barret home").
PHOTO_KINDS = {'sanderson_photo': 'devils_photo_family', 'tommy_photo': 'devils_photo_tommy', 'siblings_photo': 'devils_photo_siblings'}


def blockstate(current):
    states = json.loads(current)
    for kind, model in {**PHOTO_KINDS, **{k: v[0] for k, v in household.KINDS.items()}}.items():
        for direction, turn in (('north', 0), ('east', 90), ('south', 180), ('west', 270)):
            states['variants'][f'facing={direction},kind={kind}'] = {'model': NS + model, 'y': turn}
    return json.dumps(states, indent=2) + '\n'


# ---------------------------------------------------------------------------------------------- files

IMAGES = {'review_bear': bear, 'review_missing_notice': notice, 'review_reading_wood': reading_wood, 'review_pier': pier,
          'review_cassette': cassette, 'devils_photo_family': photo_family, 'devils_photo_tommy': photo_tommy,
          'devils_photo_siblings': photo_siblings}
MODELS_OUT = {'review_bear': bear_model, 'review_missing_notice': notice_model, 'review_cassette': cassette_model,
              'devils_photo_family': lambda: wall_photo_model('devils_photo_family'),
              'devils_photo_tommy': lambda: wall_photo_model('devils_photo_tommy'), 'devils_photo_siblings': stand_photo_model}
# The Sandersons' household (tools/devils_rock_household.py).
for _kind, (_tex, _paint, _model) in household.KINDS.items():
    IMAGES[_tex] = _paint
    MODELS_OUT[_tex] = _model
# The four that replace 0.4.100 generated crops, with the hashes those crops had.
REPLACED = {'review_bear': 'f2ae65e19c89942d45f630507789d9f36bfffbb199779b057d567b4e8cf48a3f',
            'review_missing_notice': '97d08704e0f889de468add018cce7df21b9eadf88c09dcc71b5242d53cf68b3a',
            'review_pier': 'f0132df10b9dde857e26ca990dab1f183d97e4d9a6d99aedebff850f616f3b05',
            'review_reading_wood': '9795d7d9b954ece8aede044f4b2f0a6f246f632f50c736d7439d3f667b8d54a1'}


def png_bytes(img):
    from io import BytesIO
    out = BytesIO()
    img.save(out, format='PNG', optimize=False)
    return out.getvalue()


def record():
    """The four replaced crops, each with its old hash and the hash of the drawing now committed in its place."""
    rows = []
    for name, old in REPLACED.items():
        path = TEX / f'{name}.png'
        rows.append({'path': f'src/main/resources/assets/the_oldest_house/textures/block/{name}.png', 'review_04100_sha256': old,
                     'sha256': hashlib.sha256(path.read_bytes()).hexdigest() if path.is_file() else '', 'size': list(IMAGES[name]().size)})
    return json.dumps({'authorization': "Owner, 2 November 2026: \"try to remove obviously ai-textures from devils rock\".",
                       'tool': 'tools/draw_devils_rock_props.py', 'repainted': rows}, indent=2) + '\n'


def main():
    if '--shapes' in sys.argv:
        print(household.java_shapes())
        return
    check = '--check' in sys.argv
    pngs = {name: png_bytes(fn()) for name, fn in IMAGES.items()}
    texts = {MODELS / f'{name}.json': json.dumps(fn(), indent=1) + '\n' for name, fn in MODELS_OUT.items()}
    state_path = ASSETS / 'blockstates/vignette_detail.json'
    texts[state_path] = blockstate(state_path.read_text())
    stale = []
    for name, raw in pngs.items():
        path = TEX / f'{name}.png'
        if check:
            if not path.is_file() or Image.open(path).convert('RGBA').tobytes() != IMAGES[name]().tobytes():
                stale.append(str(path))
            continue
        path.write_bytes(raw)
    # The record hashes the PNGs as committed, so it is written after them.
    texts[RECORD] = record()
    for path, text in texts.items():
        if check:
            if not path.is_file() or path.read_text() != text:
                stale.append(str(path))
            continue
        path.write_text(text)
    if stale:
        raise SystemExit("Devil's Rock props differ from tools/draw_devils_rock_props.py: " + ', '.join(stale))
    print(('Verified' if check else 'Drew') + f" {len(pngs)} Devil's Rock prop textures, {len(MODELS_OUT)} models, {len(PHOTO_KINDS)} photographs and {len(household.KINDS)} household pieces.")


if __name__ == '__main__':
    main()
