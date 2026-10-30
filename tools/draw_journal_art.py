#!/usr/bin/env python3
"""The reader's journal icon at its five wear stages (JOURNAL_DESIGN.md), 16x16, in vanilla's item style.

0: a brown cloth-bound notebook with an elastic band and cream page edges.
1: water rings on the cover.  2: a dog-eared corner.  3: swollen, rippled pages.  4: a torn corner and a tide line.
Run with python3.13 from the repository root (--check compares instead of writing); writes assets/the_oldest_house/textures/item/journal_0..4.png.

0.4.179 adds the pencil rubbings (journal/Rubbings.java), 64x64 charcoal on transparent paper, one per rubbable object:
textures/gui/rubbing/<id>.png. Charcoal lies on raised surfaces and skips what is cut in, the way a pencil laid flat
over paper does.

0.4.180 adds the other hand's lettering (journal/CoAuthor.java): a heavier, steeply leaning ink hand on the books' nine-pixel
line, built from the same glyph source as tools/generate_writing_fonts.py but distinct from all eight of its hands:
textures/font/margin.png and font/margin.json. The other eight atlases are never rewritten here.
"""
import json
import sys
from pathlib import Path
from PIL import Image, ImageDraw

sys.path.insert(0, str(Path(__file__).resolve().parent))
from generate_writing_fonts import ALIASES as HAND_ALIASES, GLYPHS as HAND_GLYPHS, rows as hand_rows  # noqa: E402

OUT = Path('src/main/resources/assets/the_oldest_house/textures/item')
COVER = (92, 60, 38, 255); COVER_LIGHT = (122, 82, 52, 255); COVER_DARK = (62, 40, 26, 255)
PAGE = (232, 222, 196, 255); PAGE_SHADE = (196, 184, 156, 255); BAND = (34, 30, 30, 255)
RING = (70, 46, 30, 255); TIDE = (150, 128, 92, 255); CLEAR = (0, 0, 0, 0)


def journal(wear):
    img = Image.new('RGBA', (16, 16), CLEAR)
    px = img.load()
    # The page block shows along the right and bottom edges, behind the cover.
    for y in range(3, 15):
        for x in range(4, 14):
            px[x, y] = PAGE if (x + y) % 3 else PAGE_SHADE
    # The cover, cloth-bound, with its spine to the left.
    for y in range(2, 14):
        for x in range(3, 13):
            px[x, y] = COVER
    for y in range(2, 14):
        px[3, y] = COVER_DARK
        px[4, y] = COVER_LIGHT
    for x in range(3, 13):
        px[x, 2] = COVER_LIGHT
        px[x, 13] = COVER_DARK
    for y in range(2, 14):
        px[12, y] = COVER_DARK
    # The elastic band down the front.
    for y in range(2, 14):
        px[10, y] = BAND
    if wear >= 1:
        for x, y in ((6, 5), (7, 4), (8, 5), (8, 6), (7, 7), (6, 6)):
            px[x, y] = RING
    if wear >= 2:
        # The top outer corner folded down.
        px[12, 2] = CLEAR
        px[11, 2] = PAGE
        px[12, 3] = PAGE
        px[11, 3] = PAGE_SHADE
    if wear >= 3:
        # Swollen pages bulge past the cover, unevenly.
        for y in range(4, 15, 2):
            px[14, y] = PAGE
        for x in range(5, 14, 3):
            px[x, 15] = PAGE_SHADE
    if wear >= 4:
        for x, y in ((5, 10), (6, 11), (7, 10), (8, 11), (9, 10), (11, 11)):
            px[x, y] = TIDE
        for x, y in ((12, 12), (12, 13), (11, 13), (13, 13), (13, 14), (12, 14)):
            px[x, y] = CLEAR
    return img


RUBBINGS = Path('src/main/resources/assets/the_oldest_house/textures/gui/rubbing')
CHARCOAL = (52, 50, 56)
GLYPHS = {
    'A': ('010', '101', '111', '101', '101'), 'C': ('011', '100', '100', '100', '011'), 'D': ('110', '101', '101', '101', '110'),
    'F': ('111', '100', '110', '100', '100'), 'G': ('011', '100', '101', '101', '011'), 'H': ('101', '101', '111', '101', '101'),
    'I': ('111', '010', '010', '010', '111'), 'J': ('001', '001', '001', '101', '010'), 'K': ('101', '101', '110', '101', '101'),
    'N': ('101', '111', '111', '101', '101'), 'O': ('010', '101', '101', '101', '010'), 'P': ('110', '101', '110', '100', '100'),
    'R': ('110', '101', '110', '101', '101'), 'W': ('101', '101', '111', '111', '101'), 'o': ('000', '000', '010', '101', '010'),
    '1': ('010', '110', '010', '010', '111'), '2': ('110', '001', '010', '100', '111'), '7': ('111', '001', '010', '010', '010'),
    '.': ('000', '000', '000', '000', '010'), '+': ('000', '010', '111', '010', '000'), ' ': ('000',) * 5,
}


def tooth(x, y, seed):
    """How much charcoal the paper's tooth takes at a point: diagonal strokes, never quite even."""
    stroke = ((x + y * 2 + seed) % 5) < 3
    n = (x * 37 + y * 91 + seed * 17) % 23
    return (120 if stroke else 70) + n * 2


def sheet(seed, area):
    """A rubbing over the area (x0, y0, x1, y1), its edges ragged where the pencil ran off the object."""
    img = Image.new('RGBA', (64, 64), CLEAR)
    px = img.load()
    x0, y0, x1, y1 = area
    for y in range(y0, y1):
        for x in range(x0, x1):
            edge = min(x - x0, x1 - 1 - x, y - y0, y1 - 1 - y)
            if edge < 2 and (x * 7 + y * 3 + seed) % 3 == 0:
                continue
            px[x, y] = CHARCOAL + (min(255, tooth(x, y, seed)),)
    return img


def mark(img, x, y, alpha):
    if 0 <= x < 64 and 0 <= y < 64:
        img.load()[x, y] = CHARCOAL + (alpha,) if alpha else CLEAR


def text(img, s, x, y, alpha, scale=1):
    for ch in s:
        rows = GLYPHS[ch]
        for r, row in enumerate(rows):
            for c, bit in enumerate(row):
                if bit == '1':
                    for dy in range(scale):
                        for dx in range(scale):
                            mark(img, x + c * scale + dx, y + r * scale + dy, alpha)
        x += 4 * scale


def gouges():
    img = sheet(1, (6, 6, 58, 58))
    for g in range(4):
        gx = 14 + g * 10
        for y in range(10, 54):
            wobble = (y * (g + 3)) % 7 == 0
            for dx in range(2 + (1 if wobble else 0)):
                mark(img, gx + dx, y, 0)
    return img


def well_initials():
    img = sheet(2, (4, 8, 60, 56))
    text(img, 'K.G.', 12, 16, 0, 2)
    # The second pair, worn: only parts of the cuts are left.
    worn = Image.new('RGBA', (64, 64), CLEAR)
    text(worn, 'D.G.', 12, 34, 255, 2)
    for y in range(64):
        for x in range(64):
            if worn.load()[x, y][3] and (x + y) % 3:
                mark(img, x, y, 0)
    return img


def sealed_window():
    img = sheet(3, (4, 4, 60, 60))
    for y in range(4, 60):
        for x in range(4, 60):
            frame = x < 12 or x > 51 or y < 12 or y > 51 or 30 <= x <= 33 or 30 <= y <= 33
            if not frame:
                mark(img, x, y, 205)
    # Runs of old paint down the frame.
    for x, length in ((14, 9), (22, 5), (40, 12), (47, 6)):
        for y in range(12, 12 + length):
            mark(img, x, y, 245)
    return img


def scribble():
    img = sheet(4, (2, 2, 62, 62))
    for line in range(14):
        y = 6 + line * 4
        for x in range(4, 60):
            dy = ((x * (line + 2)) // 5) % 3 - 1
            if (x * 3 + line) % 11 < 8:
                mark(img, x, y + dy, 0)
    return img


def room_number():
    img = Image.new('RGBA', (64, 64), CLEAR)
    for y in range(14, 50):
        for x in range(6, 58):
            if ((x - 32) / 26) ** 2 + ((y - 32) / 18) ** 2 <= 1:
                mark(img, x, y, min(255, tooth(x, y, 5)))
    text(img, '217', 21, 23, 245, 2)
    # One figure rubbed smoother: less charcoal caught on its face.
    for y in range(23, 33):
        for x in range(29, 35):
            if img.load()[x, y][3] > 240 and (x + y) % 2:
                mark(img, x, y, 170)
    return img


def town_sign():
    img = sheet(6, (2, 20, 62, 44))
    text(img, 'PROOFROCK', 14, 29, 0)
    for x in range(2, 62, 3):
        mark(img, x, 24, 230)
        mark(img, x + 1, 40, 230)
    return img


def bark():
    img = sheet(7, (8, 2, 56, 62))
    for x in range(8, 56):
        if x % 6 in (0, 1):
            for y in range(2, 62):
                if (y + x) % 9 < 6:
                    mark(img, x, y, 235)
    text(img, 'J+K', 18, 26, 0, 2)
    # The first letter half closed by the tree's growth.
    for y in range(26, 36):
        for x in range(18, 24):
            if (x * y) % 4 == 0:
                mark(img, x, y, 200)
    return img


def ward_notice():
    img = sheet(8, (4, 16, 60, 48))
    text(img, 'WARD', 17, 27, 245, 2)
    for sx, sy in ((8, 20), (54, 20), (8, 43), (54, 43)):
        for dy in range(-1, 2):
            for dx in range(-1, 2):
                mark(img, sx + dx, sy + dy, 250 if dx * dy == 0 else 0)
    return img


def pasted_paper():
    img = sheet(9, (2, 2, 62, 62))
    # The raised edges of sheets pasted one over another, each a little off square.
    for x0, y0, x1, y1 in ((6, 8, 44, 40), (18, 20, 58, 56), (10, 30, 40, 58)):
        for x in range(x0, x1):
            mark(img, x, y0, 245)
            mark(img, x, y1, 245)
        for y in range(y0, y1 + 1):
            mark(img, x0, y, 245)
            mark(img, x1, y, 245)
    # The top sheet's corner has lifted: the pencil skipped the gap beneath it.
    for y in range(20, 30):
        for x in range(58 - (y - 20), 59):
            mark(img, x, y, 0)
    # Writing under it, caught faintly through the paper.
    for row in range(5):
        for x in range(22, 54):
            if (x * 5 + row * 7) % 9 < 5:
                mark(img, x, 26 + row * 6, 200)
    return img


def funeral_plate():
    img = sheet(10, (3, 18, 61, 46))
    text(img, 'HARRIGAN', 16, 30, 250)
    for x in range(5, 59):
        mark(img, x, 21, 240)
        mark(img, x, 43, 240)
    # The name polished: only it is bright and sharp, the rest of the plate dulled.
    for y in range(18, 46):
        for x in range(3, 61):
            a = img.load()[x, y][3]
            if 0 < a < 240 and not (14 <= x <= 49 and 28 <= y <= 36):
                mark(img, x, y, max(40, a - 50))
    return img


def cave_marks():
    img = sheet(11, (2, 6, 62, 58))
    for row in range(4):
        y0 = 12 + row * 11
        for n in range(9 - row * 2):
            x = 8 + n * 6
            for y in range(y0, y0 + 7):
                mark(img, x, y, 0)
                if n % 5 == 4:
                    mark(img, x - 4 + (y - y0) * 4 // 7, y, 0)
    return img


def maker_mark():
    img = sheet(12, (6, 6, 58, 58))
    for y in range(64):
        for x in range(64):
            d = ((x - 32) ** 2 + (y - 26) ** 2) ** .5
            if 13 <= d <= 15:
                mark(img, x, y, 250)
    for i in range(5):
        import math
        a = -math.pi / 2 + i * 4 * math.pi / 5
        for t in range(10):
            mark(img, 32 + round(math.cos(a) * t), 26 + round(math.sin(a) * t), 250)
    text(img, 'No 7', 18, 46, 250, 2)
    # A dent low on the post: the charcoal skips round its rim.
    for y in range(64):
        for x in range(64):
            if 5 <= ((x - 12) ** 2 + (y - 52) ** 2) <= 9:
                mark(img, x, y, 0)
    return img


RUBBING_ART = {'gouges': gouges, 'well_initials': well_initials, 'sealed_window': sealed_window, 'scribble': scribble,
               'room_number': room_number, 'town_sign': town_sign, 'bark': bark, 'ward_notice': ward_notice,
               'pasted_paper': pasted_paper, 'funeral_plate': funeral_plate, 'cave_marks': cave_marks, 'maker_mark': maker_mark}


ASSETS = Path('src/main/resources/assets/the_oldest_house')
# The other hand's own looped a, so its writing is never mistaken for the reader's or any author's.
MARGIN_FORMS = {'a': '0000/0000/0111/1001/1001/1011/0101'}


def margin_chars():
    return hand_rows(''.join(chr(cp) for cp in range(33, 127)) + ''.join(HAND_ALIASES))


def margin_atlas():
    """Each stroke a pixel and a half wide, as a nib lays ink; the top rows lean a whole pixel, the middle half of one."""
    mapping = margin_chars()
    cell_w, cell_h = 16, 18
    atlas = Image.new('RGBA', (16 * cell_w, len(mapping) * cell_h))
    draw = ImageDraw.Draw(atlas)
    for i, char in enumerate(''.join(mapping)):
        if char not in HAND_GLYPHS:
            continue
        pattern = MARGIN_FORMS.get(char, HAND_GLYPHS[char])
        x0, y0 = i % 16 * cell_w, i // 16 * cell_h
        for y, row in enumerate(pattern.split('/')):
            slant = 2 if y < 2 else 1 if y < 4 else 0
            for x, bit in enumerate(row):
                if bit == '1':
                    draw.rectangle((x0 + x * 2 + slant, y0 + y * 2, x0 + x * 2 + slant + 2, y0 + y * 2 + 1), fill=(255, 255, 255, 255))
    return atlas


def margin_definition():
    return json.dumps({'providers': [
        {'type': 'space', 'advances': {' ': 4.0}},
        {'type': 'bitmap', 'file': 'the_oldest_house:font/margin.png', 'ascent': 7, 'height': 9, 'chars': margin_chars()},
        {'type': 'reference', 'id': 'minecraft:default'}]}, indent=2, ensure_ascii=True) + '\n'


def outputs():
    out = {OUT / f'journal_{wear}.png': journal(wear) for wear in range(5)}
    for name, draw in RUBBING_ART.items():
        out[RUBBINGS / f'{name}.png'] = draw()
    out[ASSETS / 'textures/font/margin.png'] = margin_atlas()
    return out


def main():
    check = '--check' in sys.argv
    stale = []
    for path, img in outputs().items():
        if check:
            if not path.is_file() or Image.open(path).convert('RGBA').tobytes() != img.convert('RGBA').tobytes():
                stale.append(str(path))
            continue
        path.parent.mkdir(parents=True, exist_ok=True)
        img.save(path)
    definition = ASSETS / 'font/margin.json'
    if check:
        if not definition.is_file() or definition.read_text() != margin_definition():
            stale.append(str(definition))
    else:
        definition.write_text(margin_definition())
    if stale:
        raise SystemExit('journal art differs from tools/draw_journal_art.py: ' + ', '.join(stale))


if __name__ == '__main__':
    main()
