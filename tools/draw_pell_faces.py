#!/usr/bin/env python3
"""0.4.569 (docs/PELL_HOUSE_NOTES_0_4_569.md): the Pell house's people's faces, painted.

The owner: "npcs should have wired faces/mouths/blinks/emotes as in ghosts set, too", and then: "You can try to paint unique
expressions - a character who defaults to a grimace might have a harsher smile, etc". So each of the five has their own looks, painted
in their own colours on their skin's own face grid (sixteen cells across, two texels to a cell, as tools/paint_character_skins.py paints
every face), and each look follows from the face they rest in:

- June rests in a small closed smile that does not reach her eyes; it is tender over Tilly's hair, set at the line at night, gone when
  it slips, and grief at the freezer.
- Tilly's face is open and listening; her smile is a child's, wide, with a tooth out.
- Callum's rests drawn and turned down, nearly a grimace, so his smile, when it comes on the step at first light, is a harsh one: tight,
  pulled up on one side only, under brows that stay sad. His anger bares his teeth.
- Gail's smile is broad and easy; her concern presses it flat.
- Nessa's does not change: shut, slack, the frost on her lashes.

Every look starts from a copy of the person's own face and repaints only the brows, the eyes and the mouth, so nothing around them
moves. Each has an upper half with the eyes open and with them shut (a blink), and a lower half with the mouth closed, a little open and
wide open (speaking), shaped to the look: a smile opens as a smile. The sheet for each person (textures/entity/pell_faces_<name>.png) is
two rows of faces, 32 texels square: the upper halves (open, shut, for each look in turn) and the lower halves (closed, a little open,
wide, for each look). client.PellFaces draws the upper half of one and the lower half of another over the face; the looks' names and
order are read from it, and this fails if they disagree.

Run it to draw; --check verifies the pack against it (CI runs that); --preview PATH also writes an enlarged sheet of every look.
"""
import argparse
import re
import sys
from pathlib import Path

from PIL import Image

ROOT = Path(__file__).resolve().parent.parent
A = ROOT / 'src/main/resources/assets/the_oldest_house/textures/entity'
JAVA = ROOT / 'src/main/java/io/github/knaitoe/theoldesthouse/client/PellFaces.java'
PEOPLE = ['june', 'tilly', 'callum', 'gail', 'nessa']
CHILD = {'tilly', 'callum', 'nessa'}
CELL = 32

INSIDE = (0x3a, 0x24, 0x26)   # a mouth open, dark
TONGUE = (0x6a, 0x36, 0x36)
TEAR = (0xd2, 0xe4, 0xec)
FROST = [(0xd6, 0xe2, 0xe8), (0xf2, 0xf8, 0xfa)]

# Brows, for each side: the rows of its outer, middle and inner cells (the brow's own row is 6).
LEFT, RIGHT = (3, 4, 5), (12, 11, 10)
N, UP, W1, W2, A1, A2 = (6, 6, 6), (5, 5, 5), (6, 6, 5), (6, 5, 4), (6, 6, 7), (5, 6, 7)

# What each look is made of: brows (left, right), eyes (a state, or one for each side, or a list applied in turn), anything else, the
# mouth, and how it opens to speak ('up' keeps a smile's corners, 'down' a frown's, 'flat' a level mouth's; 'none' does not open).
ART = {
    'june': {
        'warm': dict(brows=(N, N), eyes='open', mouth='tight_smile', speech='up'),
        'tender': dict(brows=(W1, W1), eyes='half', mouth='soft_smile', speech='up'),
        'set': dict(brows=(A1, A1), eyes='narrow', mouth='pressed', speech='flat'),
        'slip': dict(brows=(N, N), eyes='stare', mouth='flat', speech='flat'),
        'grief': dict(brows=(W2, W2), eyes='half', extras=['tears'], mouth='tremble', speech='down'),
        'lost': dict(brows=(UP, UP), eyes='wide', mouth='slack', speech='flat'),
        'pain': dict(brows=(A2, A1), eyes=('squeeze', 'narrow'), mouth='clench', speech='flat'),
        'sleep': dict(brows=(N, N), eyes='open', mouth='neutral', speech='flat'),
    },
    'tilly': {
        'open': dict(brows=(N, N), eyes='open', mouth='neutral', speech='flat'),
        'listening': dict(brows=(UP, UP), eyes='up', mouth='o', speech='flat'),
        'bright': dict(brows=(UP, UP), eyes='crescent', mouth='grin', speech='up'),
        'content': dict(brows=(N, N), eyes='half', mouth='soft_smile', speech='up'),
        'unsure': dict(brows=(W1, W1), eyes='open', mouth='short', speech='flat'),
        'sleep': dict(brows=(N, N), eyes='open', mouth='slack', speech='flat'),
    },
    'callum': {
        'drawn': dict(brows=(W1, W1), eyes='half', extras=['shadow'], mouth='grimace', speech='down'),
        'up': dict(brows=(UP, UP), eyes='up', mouth='neutral', speech='flat'),
        'fury': dict(brows=(A2, A2), eyes='narrow', mouth='bared', speech='flat'),
        'eating': dict(brows=(W1, W1), eyes='half', mouth='chew', speech='flat'),
        'blank': dict(brows=(UP, UP), eyes=['wide', 'stare'], mouth='flat', speech='flat'),
        'wan': dict(brows=(W1, W1), eyes='half', extras=['shadow'], mouth='harsh_smile', speech='flat'),
        'calm': dict(brows=(N, N), eyes='open', mouth='neutral', speech='flat'),
    },
    'gail': {
        'pleasant': dict(brows=(N, N), eyes='open', mouth='broad_smile', speech='up'),
        'concern': dict(brows=(W1, W1), eyes='open', mouth='pressed', speech='down'),
        'earnest': dict(brows=(UP, UP), eyes='stare', mouth='neutral', speech='flat'),
    },
    'nessa': {
        'slack': dict(brows=(N, N), eyes='shut', extras=['frost'], mouth='cold', speech='none'),
    },
}


def shade(c, n):
    return tuple(max(0, min(255, v + n)) for v in c[:3])


def mix(a, b, t):
    return tuple(round(x + (y - x) * t) for x, y in zip(a[:3], b[:3]))


def java():
    """The looks' names, for each person in turn, as client.PellFaces has them."""
    src = JAVA.read_text()
    m = re.search(r'LOOKS=\{(.*?)\};', src, re.S)
    assert m, 'LOOKS'
    groups = re.findall(r'\{([^{}]*)\}', m.group(1))
    return [re.findall(r'"([a-z_]+)"', g) for g in groups]


class Face:
    """One face, a copy of the person's own: cells are (column, row) on the sixteen-cell grid."""

    def __init__(self, base, name):
        self.im = base.copy()
        self.child = name in CHILD
        g = self.get
        self.S = g(5, 11)
        assert g(10, 11) == self.S, (name, 'the cheeks are not plain skin')
        self.B, self.W, self.I, self.P, self.M = g(4, 6), g(3, 7), g(4, 7), g(4, 8), g(7, 13)
        assert sum(self.P) < 200, (name, 'the pupil is not dark')
        self.L = self.S if self.child else g(7, 14)
        self.K = mix(self.B, self.P, .5)
        self.T = shade(self.W, -6)
        right = [c for c in (10, 11, 12) if g(c, 7) != self.W]
        assert len(right) == 1, (name, 'the right eye has one iris column')
        self.iris = {'L': 4, 'R': right[0]}

    def get(self, c, r):
        return self.im.getpixel((2 * c, 2 * r))[:3]

    def put(self, c, r, col):
        for dx in (0, 1):
            for dy in (0, 1):
                self.im.putpixel((2 * c + dx, 2 * r + dy), tuple(col[:3]) + (255,))

    # -------------------------------------------------------------------------------------------------------------------- the eyes
    def eye(self, side, state):
        cols = (3, 4, 5) if side == 'L' else (10, 11, 12)
        outer, centre = (3, 4) if side == 'L' else (12, 11)
        iris = self.iris[side]
        S, W, I, P, K = self.S, self.W, self.I, self.P, self.K

        def gaze(col, up=False):
            for c in cols:
                self.put(c, 7, W)
                self.put(c, 8, W)
            self.put(col, 7, P if up else I)
            self.put(col, 8, I if up else P)
        if state == 'open':
            return
        if state == 'half':        # the upper lid down over the top of the eye
            for c in cols:
                self.put(c, 7, shade(S, -16))
        elif state == 'narrow':    # lowered, a darker lid
            for c in cols:
                self.put(c, 7, shade(S, -30))
        elif state == 'shut':      # skin, and the lashes along the bottom
            for c in cols:
                self.put(c, 7, S if self.child else shade(S, -8))
                self.put(c, 8, K)
        elif state == 'crescent':  # shut up into an arc, a child's glee
            for c in cols:
                self.put(c, 7, S)
                self.put(c, 8, S)
            self.put(cols[0], 8, K)
            self.put(cols[1], 7, K)
            self.put(cols[2], 8, K)
        elif state == 'squeeze':   # screwed shut, creased at the side
            for c in cols:
                self.put(c, 7, S)
                self.put(c, 8, S)
            inner = cols[2] if side == 'L' else cols[0]
            self.put(outer, 7, K)
            self.put(centre, 8, K)
            self.put(inner, 8, K)
            self.put(outer - 1 if side == 'L' else outer + 1, 7, shade(S, -26))
        elif state == 'wide':      # white above the iris
            for c in cols:
                self.put(c, 6, W)
        elif state == 'up':
            gaze(iris, up=True)
        elif state == 'stare':     # both irises to the middle of the eye, looking straight out
            gaze(centre)
        elif state == 'unfocused':  # each to the outside, looking through
            gaze(outer)
        else:
            raise ValueError(state)

    def eyes(self, spec, shut=False):
        if shut:
            spec = 'shut'
        if isinstance(spec, tuple):
            self.eye('L', spec[0])
            self.eye('R', spec[1])
            return
        for state in spec if isinstance(spec, list) else [spec]:
            self.eye('L', state)
            self.eye('R', state)

    # ------------------------------------------------------------------------------------------------------------------- the brows
    def brows(self, left, right):
        for cols in (LEFT, RIGHT):
            for c in cols:
                if self.get(c, 6) == self.B:
                    self.put(c, 6, self.S)
        for cols, rows in ((LEFT, left), (RIGHT, right)):
            for c, r in zip(cols, rows):
                self.put(c, r, self.B)

    # ----------------------------------------------------------------------------------------------------------------- the extras
    def extra(self, what):
        S = self.S
        if what == 'tears':
            self.put(5, 9, TEAR)
            self.put(10, 9, TEAR)
        elif what == 'shadow':
            for c in (3, 4, 5, 10, 11, 12):
                self.put(c, 9, shade(S, -18))
        elif what == 'frost':          # on her brow and cheeks, and never on the shut lids, where it would read as an eye
            self.put(3, 10, FROST[0])
            self.put(12, 9, FROST[1])
            self.put(6, 11, FROST[0])
            self.put(10, 5, FROST[1])
        else:
            raise ValueError(what)

    # ------------------------------------------------------------------------------------------------------------------- the mouth
    def line(self, c0, c1, r, col):
        for c in range(c0, c1 + 1):
            self.put(c, r, col)

    def mouth(self, shape):
        S, M, L, T = self.S, self.M, self.L, self.T
        for r in range(12, 16):
            for c in range(4, 12):
                self.put(c, r, S)
        adult = not self.child
        if shape == 'neutral':
            self.line(6, 9, 13, M)
            if adult:
                self.line(6, 9, 14, L)
        elif shape == 'flat':          # level, and nothing more: no lip under it
            self.line(6, 9, 13, M)
        elif shape == 'tight_smile':   # closed, the corners up, the eyes left out of it
            self.line(6, 9, 13, M)
            self.put(5, 12, M)
            self.put(10, 12, M)
            if adult:
                self.line(7, 8, 14, L)
        elif shape == 'soft_smile':
            soft = mix(M, S, .35)
            self.line(6, 9, 13, soft)
            self.put(5, 12, soft)
            self.put(10, 12, soft)
        elif shape == 'broad_smile':   # wide and easy, the cheeks lifted with it
            self.line(5, 10, 13, M)
            self.put(4, 12, M)
            self.put(11, 12, M)
            self.line(6, 9, 14, L if adult else S)
            self.put(4, 11, shade(S, -12))
            self.put(11, 11, shade(S, -12))
        elif shape == 'pressed':       # thin and hard
            self.line(6, 9, 13, shade(M, -26))
        elif shape == 'short':
            self.line(7, 8, 13, M)
        elif shape == 'tremble':       # turned down, not quite level
            self.put(6, 13, M)
            self.put(7, 13, M)
            self.put(8, 14, M)
            self.put(9, 13, M)
            self.put(5, 14, M)
            self.put(10, 14, M)
        elif shape == 'grimace':       # Callum at rest: drawn down and held
            self.line(6, 9, 13, shade(M, -14))
            self.put(5, 14, M)
            self.put(10, 14, M)
        elif shape == 'slack':         # a little open
            self.line(6, 9, 13, M)
            self.line(7, 8, 14, INSIDE)
        elif shape == 'o':
            self.put(6, 13, M)
            self.put(9, 13, M)
            self.line(7, 8, 13, INSIDE)
            self.line(7, 8, 14, INSIDE)
        elif shape == 'grin':          # a child's: wide open, the top teeth with one out
            self.put(4, 12, M)
            self.put(11, 12, M)
            self.put(5, 13, M)
            self.put(10, 13, M)
            for c, col in zip(range(6, 10), (T, T, INSIDE, T)):
                self.put(c, 13, col)
            self.line(6, 9, 14, INSIDE)
            self.line(7, 8, 14, TONGUE)
            self.line(6, 9, 15, M)
        elif shape == 'bared':         # teeth shut and shown, the lips back off them
            self.line(5, 10, 12, M)
            self.put(5, 13, INSIDE)
            self.put(10, 13, INSIDE)
            self.line(6, 9, 13, T)
            self.put(5, 14, INSIDE)
            self.put(10, 14, INSIDE)
            self.line(6, 9, 14, shade(T, -34))
            self.line(6, 9, 15, M)
        elif shape == 'clench':        # through the teeth
            self.line(6, 9, 12, M)
            self.line(6, 9, 13, T)
            self.line(6, 9, 14, M)
        elif shape == 'harsh_smile':   # tight, pulled up on one side only and baring the teeth there, a crease pulled in by it
            self.line(6, 7, 13, shade(M, -12))
            self.line(8, 9, 13, T)
            self.put(9, 12, M)
            self.put(10, 12, M)
            self.line(8, 9, 14, M)
            self.put(11, 12, shade(S, -16))
        elif shape == 'chew':
            self.line(7, 8, 13, M)
            self.put(4, 12, shade(S, 10))
            self.put(11, 12, shade(S, 10))
        elif shape == 'cold':          # Nessa's: a little open, gone blue
            blue = mix(M, (0x8a, 0x9c, 0xb0), .55)
            self.line(6, 9, 13, blue)
            self.line(7, 8, 14, mix(INSIDE, (0x3c, 0x46, 0x5a), .5))
        else:
            raise ValueError(shape)

    def open(self, style, wide):
        """The mouth as drawn, opened to speak: a little (the middle two cells under the lip), or wide (the jaw down a row more)."""
        if style == 'none':
            return
        if not wide:
            self.line(7, 8, 14, INSIDE)
            return
        self.line(6, 9, 14, INSIDE)
        if not self.child:
            self.line(7, 8, 14, self.T)
        self.line(7, 8, 15, INSIDE)
        if style == 'up':
            self.put(6, 15, self.M)
            self.put(9, 15, self.M)


def painted(base, name, spec, shut=False, speak=0):
    f = Face(base, name)
    f.mouth(spec['mouth'])
    if speak:
        f.open(spec['speech'], speak == 2)
    f.eyes(spec['eyes'], shut)
    for what in spec.get('extras', []):
        f.extra(what)
    f.brows(*spec['brows'])
    return f.im


def sheets():
    names = java()
    assert len(names) == len(PEOPLE), names
    out = {}
    for who, looks in zip(PEOPLE, names):
        assert list(ART[who]) == looks, (who, list(ART[who]), looks)
        with Image.open(A / f'literary_pell_{who}.png') as skin:
            base = skin.convert('RGBA').crop((32, 32, 64, 64))
        # Every cell of the face is one colour, so the copy repaints whole cells.
        for c in range(16):
            for r in range(16):
                cell = {base.getpixel((2 * c + dx, 2 * r + dy)) for dx in (0, 1) for dy in (0, 1)}
                assert len(cell) == 1, (who, c, r)
        sheet = Image.new('RGBA', (3 * len(looks) * CELL, 2 * CELL), (0, 0, 0, 0))
        for e, look in enumerate(looks):
            spec = ART[who][look]
            sheet.paste(painted(base, who, spec), (2 * e * CELL, 0))
            sheet.paste(painted(base, who, spec, shut=True), ((2 * e + 1) * CELL, 0))
            for k in range(3):
                sheet.paste(painted(base, who, spec, speak=k), ((3 * e + k) * CELL, CELL))
        out[who] = (sheet, base, looks)
    return out


def preview(out, path):
    """Each look as it is seen: eyes open and shut over the closed mouth, then open over the mouth a little and wide open."""
    rows = []
    for who in PEOPLE:
        sheet, base, looks = out[who]
        row = [base]
        for e in range(len(looks)):
            for upper, lower in ((2 * e, 3 * e), (2 * e + 1, 3 * e), (2 * e, 3 * e + 1), (2 * e, 3 * e + 2)):
                face = Image.new('RGBA', (CELL, CELL))
                face.paste(sheet.crop((upper * CELL, 0, upper * CELL + CELL, 20)), (0, 0))
                face.paste(sheet.crop((lower * CELL, CELL + 20, lower * CELL + CELL, 2 * CELL)), (0, 20))
                row.append(face)
        rows.append(row)
    width = max(len(r) for r in rows)
    im = Image.new('RGBA', (width * 36, len(rows) * 36), (70, 74, 80, 255))
    for y, row in enumerate(rows):
        for x, face in enumerate(row):
            im.paste(face, (x * 36 + 2, y * 36 + 2))
    im.resize((im.width * 4, im.height * 4), Image.NEAREST).save(path)


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--check', action='store_true')
    parser.add_argument('--preview')
    args = parser.parse_args()
    out = sheets()
    for who, (sheet, _, _) in out.items():
        path = A / f'pell_faces_{who}.png'
        if args.check:
            with Image.open(path) as actual:
                assert actual.convert('RGBA').tobytes() == sheet.tobytes(), who
        else:
            sheet.save(path, optimize=True)
    if args.preview:
        preview(out, args.preview)
    print('checked' if args.check else 'drawn', 'the Pell faces')


if __name__ == '__main__':
    sys.exit(main())
