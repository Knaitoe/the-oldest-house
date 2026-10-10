#!/usr/bin/env python3
"""Proofrock (0.4.67): shop signboards, lockers, chalkboards, the trophy case and crest, bleachers, posters and desks.

Native 16x16 pixel art. Signboards are painted across three panels with a 3x5 pixel alphabet so their words read on the
street. Every model faces north in its file; blockstates turn it to the block's facing.
Run from the repository root:
    python3 tools/generate_proofrock_assets.py
"""
from pathlib import Path
import json
from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
A = ROOT / 'src/main/resources/assets/the_oldest_house'
NS = 'the_oldest_house'

GLYPHS = {
    'A': ['010', '101', '111', '101', '101'], 'B': ['110', '101', '110', '101', '110'], 'C': ['011', '100', '100', '100', '011'],
    'D': ['110', '101', '101', '101', '110'], 'E': ['111', '100', '110', '100', '111'], 'F': ['111', '100', '110', '100', '100'],
    'G': ['011', '100', '101', '101', '011'], 'H': ['101', '101', '111', '101', '101'], 'I': ['111', '010', '010', '010', '111'],
    'J': ['001', '001', '001', '101', '010'], 'K': ['101', '101', '110', '101', '101'], 'L': ['100', '100', '100', '100', '111'],
    'M': ['10001', '11011', '10101', '10001', '10001'], 'N': ['1001', '1101', '1011', '1001', '1001'], 'O': ['010', '101', '101', '101', '010'],
    'P': ['110', '101', '110', '100', '100'], 'Q': ['010', '101', '101', '110', '011'], 'R': ['110', '101', '110', '101', '101'],
    'S': ['011', '100', '010', '001', '110'], 'T': ['111', '010', '010', '010', '010'], 'U': ['101', '101', '101', '101', '111'],
    'V': ['101', '101', '101', '101', '010'], 'W': ['10001', '10001', '10101', '11011', '10001'], 'X': ['101', '101', '010', '101', '101'],
    'Y': ['101', '101', '010', '010', '010'], 'Z': ['111', '001', '010', '100', '111'], '&': ['010', '101', '010', '101', '011'],
    '2': ['110', '001', '010', '100', '111'], '4': ['101', '101', '111', '001', '001'], ' ': ['00', '00', '00', '00', '00'],
}

# name: (top line, bottom line, background, letters, border)
SIGNS = {
    'general': ('GENERAL', 'STORE', (226, 214, 176), (122, 32, 30), (74, 50, 30)),
    'post': ('POST', 'OFFICE', (34, 52, 104), (238, 236, 226), (20, 28, 60)),
    'laundry': ('LAUNDROMAT', 'COIN OP', (150, 198, 218), (28, 46, 92), (70, 110, 140)),
    'hardware': ('HARDWARE', 'FEED', (44, 92, 54), (232, 204, 80), (24, 52, 30)),
    'sheriff': ('SHERIFF', 'COUNTY', (88, 60, 34), (226, 184, 72), (50, 32, 18)),
    'diner': ('DINER', 'COFFEE', (166, 34, 34), (244, 238, 226), (96, 16, 18)),
    'cinema': ('LAKEVIEW', 'CLOSED', (22, 20, 24), (240, 236, 220), (210, 168, 60)),
    'bait': ('BAIT &', 'TACKLE', (42, 108, 110), (240, 240, 232), (22, 60, 62)),
    'hall': ('TOWN', 'HALL', (150, 150, 144), (32, 32, 34), (96, 96, 92)),
    'motel': ('MOTEL', 'VACANCY', (26, 62, 70), (240, 112, 150), (14, 30, 34)),
    'garage': ('GAS', 'GARAGE', (232, 230, 222), (176, 36, 32), (120, 120, 118)),
    'school': ('INDIAN LAKE', 'HIGH SCHOOL', (112, 30, 42), (232, 196, 84), (62, 14, 22)),
}


def write_json(path, value):
    out = A / path
    out.parent.mkdir(parents=True, exist_ok=True)
    out.write_text(json.dumps(value, indent=2) + '\n')


def save(path, image):
    out = A / path
    out.parent.mkdir(parents=True, exist_ok=True)
    image.save(out)


def width(text):
    return sum(len(GLYPHS[c][0]) + 1 for c in text) - 1


def draw(im, text, top, colour):
    x = (im.width - width(text)) // 2
    for c in text:
        glyph = GLYPHS[c]
        for row, bits in enumerate(glyph):
            for col, bit in enumerate(bits):
                if bit == '1':
                    im.putpixel((x + col, top + row), colour + (255,))
        x += len(glyph[0]) + 1


def shade(colour, amount):
    return tuple(max(0, min(255, c + amount)) for c in colour)


def signboard(name, top, bottom, bg, fg, border):
    im = Image.new('RGBA', (48, 16), bg + (255,))
    for x in range(48):
        for y in range(16):
            grain = ((x * 7 + y * 13) % 9) - 4
            im.putpixel((x, y), shade(bg, grain // 2) + (255,))
    for x in range(48):
        im.putpixel((x, 0), border + (255,)); im.putpixel((x, 15), border + (255,))
        im.putpixel((x, 1), shade(border, 30) + (255,))
    for y in range(16):
        im.putpixel((0, y), border + (255,)); im.putpixel((47, y), border + (255,))
    if name == 'cinema':
        for x in range(2, 46, 3):
            im.putpixel((x, 1), (250, 226, 120, 255)); im.putpixel((x, 14), (250, 226, 120, 255))
    draw(im, top, 3, fg)
    draw(im, bottom, 9, shade(fg, -30) if name != 'cinema' else (240, 80, 70))
    for i, part in enumerate('lmr'):
        save(f'textures/block/town_sign_{name}_{part}.png', im.crop((16 * i, 0, 16 * i + 16, 16)))


def locker(top):
    im = Image.new('RGBA', (16, 16))
    base = (82, 108, 132)
    for x in range(16):
        for y in range(16):
            im.putpixel((x, y), shade(base, 10 if x in (1, 9) else -14 if x in (0, 8, 15) else 0) + (255,))
    for door in (0, 8):
        for y in range(16):
            im.putpixel((door, y), (40, 52, 66, 255))
        if top:
            for vy in (3, 5, 7):
                for vx in range(door + 2, door + 7):
                    im.putpixel((vx, vy), (34, 44, 56, 255))
            for vx in range(door + 3, door + 6):
                im.putpixel((vx, 11), (210, 206, 190, 255))
        else:
            im.putpixel((door + 6, 3), (190, 190, 186, 255)); im.putpixel((door + 6, 4), (150, 150, 146, 255))
    if not top:
        for x in range(16):
            im.putpixel((x, 15), (40, 52, 66, 255))
    save(f'textures/block/{"locker_top" if top else "locker"}.png', im)


def locker_side():
    im = Image.new('RGBA', (16, 16))
    for x in range(16):
        for y in range(16):
            im.putpixel((x, y), shade((76, 100, 122), (x * 3 + y) % 5 - 2) + (255,))
    save('textures/block/locker_side.png', im)


def chalkboard(part):
    im = Image.new('RGBA', (16, 16))
    board, frame = (40, 66, 52), (128, 92, 56)
    for x in range(16):
        for y in range(16):
            im.putpixel((x, y), shade(board, ((x * 5 + y * 3) % 7) - 3) + (255,))
    for x in range(16):
        im.putpixel((x, 0), frame + (255,)); im.putpixel((x, 14), frame + (255,)); im.putpixel((x, 15), shade(frame, -30) + (255,))
    if part == 'l':
        for y in range(16): im.putpixel((0, y), frame + (255,))
    if part == 'r':
        for y in range(16): im.putpixel((15, y), frame + (255,))
    chalk = (214, 220, 210, 255)
    # A few lines of chalk handwriting, a diagram on the middle panel, a date in the corner.
    lines = {'l': [(2, 3, 11), (2, 6, 9), (2, 9, 12)], 'm': [(1, 3, 6), (9, 3, 14)], 'r': [(1, 3, 9), (1, 6, 12), (1, 9, 7)]}[part]
    for x0, y, x1 in lines:
        for x in range(x0, x1):
            if (x * 7 + y) % 5 != 0:
                im.putpixel((x, y), chalk)
    if part == 'm':
        for t in range(10):
            im.putpixel((3 + t, 11 - t // 2), chalk)
        for y in range(6, 12):
            im.putpixel((8, y), chalk)
    im.putpixel((12, 13), (230, 230, 230, 255)); im.putpixel((13, 13), (230, 230, 230, 255))
    save(f'textures/block/chalkboard_{part}.png', im)


def trophy():
    im = Image.new('RGBA', (16, 16))
    for x in range(16):
        for y in range(16):
            edge = x in (0, 15) or y in (0, 15)
            im.putpixel((x, y), (88, 58, 34, 255) if edge else (54, 62, 70, 255) if y > 8 else (70, 80, 90, 255))
    for x in range(1, 15):
        im.putpixel((x, 8), (110, 76, 46, 255))
    for cx, cy in ((4, 4), (11, 4), (7, 11)):
        for x in range(cx - 1, cx + 2):
            im.putpixel((x, cy), (230, 190, 70, 255))
        im.putpixel((cx, cy + 1), (200, 160, 50, 255)); im.putpixel((cx, cy + 2), (180, 140, 40, 255))
        im.putpixel((cx - 1, cy + 3), (180, 140, 40, 255)); im.putpixel((cx + 1, cy + 3), (180, 140, 40, 255)); im.putpixel((cx, cy + 3), (180, 140, 40, 255))
        im.putpixel((cx - 2, cy), (200, 160, 50, 255)); im.putpixel((cx + 2, cy), (200, 160, 50, 255))
    for y in range(1, 15, 3):
        im.putpixel((2, y), (190, 210, 220, 255))
    save('textures/block/trophy_case.png', im)


def crest():
    im = Image.new('RGBA', (16, 16))
    for x in range(16):
        for y in range(16):
            im.putpixel((x, y), shade((150, 58, 44), ((x + y) % 3) * 6 - 6) + (255,))
    for y in range(2, 14):
        half = 6 if y < 10 else 6 - (y - 9)
        for x in range(8 - half, 8 + half):
            im.putpixel((x, y), (38, 60, 120, 255))
    for y in range(2, 14):
        half = 6 if y < 10 else 6 - (y - 9)
        im.putpixel((8 - half, y), (226, 190, 80, 255)); im.putpixel((7 + half, y), (226, 190, 80, 255))
    small = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    draw(small, 'ILH', 5, (232, 196, 84))
    im.alpha_composite(small)
    save('textures/block/school_crest.png', im)


def bleacher():
    im = Image.new('RGBA', (16, 16))
    for x in range(16):
        for y in range(16):
            row = y % 4
            im.putpixel((x, y), (30, 26, 22, 255) if row == 3 else shade((158, 122, 78), ((x * 3) % 7) - 3 - row * 6) + (255,))
    for y in range(16):
        im.putpixel((0, y), (60, 60, 64, 255)); im.putpixel((15, y), (60, 60, 64, 255))
    save('textures/block/bleacher.png', im)


def poster():
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    for x in range(1, 15):
        for y in range(1, 15):
            im.putpixel((x, y), shade((232, 228, 210), ((x * 5 + y * 7) % 9) - 4) + (255,))
    small = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    draw(small, 'LOST', 2, (40, 36, 34))
    for x in range(16):
        for y in range(2, 7):
            p = small.getpixel((x, y))
            if p[3] and 1 <= x <= 14:
                im.putpixel((x, y), p)
    for x in range(5, 11):
        for y in range(8, 13):
            if (x - 7.5) ** 2 / 9 + (y - 10) ** 2 / 6 <= 1:
                im.putpixel((x, y), (96, 90, 84, 255))
    for x in range(4, 12):
        im.putpixel((x, 13), (120, 114, 108, 255))
    save('textures/block/missing_poster.png', im)


def wood(name, colour):
    im = Image.new('RGBA', (16, 16))
    for x in range(16):
        for y in range(16):
            im.putpixel((x, y), shade(colour, ((y * 5 + x // 4 * 3) % 7) - 3 - (8 if y % 5 == 0 else 0)) + (255,))
    save(f'textures/block/{name}.png', im)


def metal():
    im = Image.new('RGBA', (16, 16))
    for x in range(16):
        for y in range(16):
            im.putpixel((x, y), shade((112, 116, 120), ((x + y * 3) % 5) - 2) + (255,))
    save('textures/block/student_desk_metal.png', im)


def desk_front():
    im = Image.new('RGBA', (16, 16))
    for x in range(16):
        for y in range(16):
            im.putpixel((x, y), shade((118, 84, 52), ((x * 3 + y * 5) % 7) - 3) + (255,))
    for x in range(16):
        im.putpixel((x, 2), (70, 48, 28, 255)); im.putpixel((x, 15), (70, 48, 28, 255))
    for y in range(2, 16):
        im.putpixel((9, y), (70, 48, 28, 255))
    for y in (6, 10):
        for x in range(10, 16):
            im.putpixel((x, y), (70, 48, 28, 255))
    for y in (4, 8, 12):
        im.putpixel((12, y), (200, 190, 150, 255)); im.putpixel((13, y), (200, 190, 150, 255))
    save('textures/block/school_desk_front.png', im)


FULL = {
    'locker': ('locker', 'locker_side', 'locker_side'),
    'locker_top': ('locker_top', 'locker_side', 'locker_side'),
    'chalk_l': ('chalkboard_l', 'minecraft:block/white_terracotta', 'minecraft:block/white_terracotta'),
    'chalk_m': ('chalkboard_m', 'minecraft:block/white_terracotta', 'minecraft:block/white_terracotta'),
    'chalk_r': ('chalkboard_r', 'minecraft:block/white_terracotta', 'minecraft:block/white_terracotta'),
    'trophy': ('trophy_case', 'minecraft:block/dark_oak_planks', 'minecraft:block/dark_oak_planks'),
    'crest': ('school_crest', 'minecraft:block/bricks', 'minecraft:block/bricks'),
    'bleacher': ('bleacher', 'minecraft:block/spruce_planks', 'minecraft:block/spruce_planks'),
}


def ref(texture):
    return texture if ':' in texture else f'{NS}:block/{texture}'


def models():
    kinds = list(FULL) + ['poster', 'student_desk'] + [f'{name}_{part}' for name in SIGNS for part in 'lmr']
    for kind, (front, side, top) in FULL.items():
        write_json(f'models/block/town_fixture_{kind}.json', {'parent': 'minecraft:block/orientable',
                   'textures': {'front': ref(front), 'side': ref(side), 'top': ref(top)}})
    for name in SIGNS:
        for part in 'lmr':
            write_json(f'models/block/town_fixture_{name}_{part}.json', {'parent': 'minecraft:block/orientable',
                       'textures': {'front': f'{NS}:block/town_sign_{name}_{part}', 'side': 'minecraft:block/dark_oak_planks', 'top': 'minecraft:block/dark_oak_planks'}})
    paper = f'{NS}:block/missing_poster'
    write_json('models/block/town_fixture_poster.json', {'textures': {'poster': paper, 'particle': paper}, 'elements': [
        {'from': [1, 1, 15], 'to': [15, 15, 16], 'faces': {'north': {'uv': [1, 1, 15, 15], 'texture': '#poster'},
                                                            'south': {'uv': [1, 1, 15, 15], 'texture': '#poster'},
                                                            'up': {'uv': [1, 1, 15, 2], 'texture': '#poster'}, 'down': {'uv': [1, 14, 15, 15], 'texture': '#poster'},
                                                            'east': {'uv': [1, 1, 2, 15], 'texture': '#poster'}, 'west': {'uv': [14, 1, 15, 15], 'texture': '#poster'}}}]})
    def box(a, b, tex):
        return {'from': a, 'to': b, 'faces': {f: {'texture': tex} for f in ('north', 'south', 'east', 'west', 'up', 'down')}}
    write_json('models/block/town_fixture_student_desk.json', {
        'textures': {'wood': f'{NS}:block/student_desk_wood', 'metal': f'{NS}:block/student_desk_metal', 'particle': f'{NS}:block/student_desk_wood'},
        'elements': [box([1, 11, 1], [15, 12, 9], '#wood'), box([2, 8, 1], [14, 9, 8], '#metal'),
                     box([2, 0, 2], [3, 11, 3], '#metal'), box([13, 0, 2], [14, 11, 3], '#metal'),
                     box([3, 6, 10], [13, 7, 15], '#wood'), box([3, 7, 14], [13, 13, 15], '#wood'),
                     box([3, 0, 10], [4, 6, 11], '#metal'), box([12, 0, 10], [13, 6, 11], '#metal'),
                     box([3, 0, 14], [4, 6, 15], '#metal'), box([12, 0, 14], [13, 6, 15], '#metal'),
                     box([7, 4, 3], [9, 5, 12], '#metal')]})
    write_json('models/block/school_desk.json', {
        'textures': {'front': f'{NS}:block/school_desk_front', 'side': f'{NS}:block/student_desk_wood', 'top': f'{NS}:block/school_desk_top', 'particle': f'{NS}:block/school_desk_top'},
        'elements': [{'from': [0, 0, 0], 'to': [16, 14, 16], 'faces': {
            'north': {'texture': '#front'}, 'south': {'texture': '#side'}, 'east': {'texture': '#side'}, 'west': {'texture': '#side'},
            'up': {'texture': '#top'}, 'down': {'texture': '#side'}}}]})
    turn = {'north': 0, 'east': 90, 'south': 180, 'west': 270}
    variants = {}
    for facing, y in turn.items():
        for kind in kinds:
            entry = {'model': f'{NS}:block/town_fixture_{kind}'}
            if y:
                entry['y'] = y
            variants[f'facing={facing},kind={kind}'] = entry
    write_json('blockstates/town_fixture.json', {'variants': variants})
    desk = {}
    for facing, y in turn.items():
        entry = {'model': f'{NS}:block/school_desk'}
        if y:
            entry['y'] = y
        desk[f'facing={facing}'] = entry
    write_json('blockstates/school_desk.json', {'variants': desk})
    return kinds


def lang():
    path = A / 'lang/en_us.json'
    entries = json.loads(path.read_text())
    entries.update({'block.the_oldest_house.town_fixture': 'Town fixture', 'block.the_oldest_house.school_desk': "Teacher's desk"})
    path.write_text(json.dumps(entries, indent=2, ensure_ascii=False) + '\n')


if __name__ == '__main__':
    for name, (top, bottom, bg, fg, border) in SIGNS.items():
        signboard(name, top, bottom, bg, fg, border)
    locker(False); locker(True); locker_side()
    for part in 'lmr':
        chalkboard(part)
    trophy(); crest(); bleacher(); poster(); metal(); desk_front()
    wood('student_desk_wood', (166, 128, 82)); wood('school_desk_top', (140, 100, 62))
    kinds = models()
    lang()
    print(f'{len(kinds)} fixture kinds')
