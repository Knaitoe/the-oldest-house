"""0.4.585, the owner: "improve the texture of the boy before he transforms, too".

The boy in the cell at the foot of the great staircase, before he is the Minotaur (tools/draw_minotaur.py). He was one row
of the cast's painter: a buttoned grey shirt, dark trousers and shoes, and a grown man's face at a child's size. He is
painted here by himself, from what he becomes:

- its eyes (0.4.199, the owner: "Don't give it red eyes. It's not necessarily a monster." and "Try to make them look human
  in the way a cows eyes can look human"): large and dark, a lid's line over each, white only at the outer corner, the
  brown lighter along its top where it catches the light. They are the same eyes, in the same tones.
- its dark poll and its forelocks: his hair, never cut, down over his ears and his collar, one lock between his brows.
- its rope belt and its rag: he wears a grown person's shirt as a tunic, in the rag's cloth before it darkened, tied at the
  waist with the same rope, knotted on the same side, the ends hanging.
- bare feet, and trousers worn off below the knee.

Nothing on him says what he is: no horn, no hoof, nothing red, and no wound. He is a child who has been kept a long time,
drawn as the House's other children are: on the standard 64 by 64 player layout at two pixels to a model unit (128 by
128), doubled to the 256-pixel sheet, as the shared painter does. His arms are the slim player model's, three pixels wide.

The player model's second layer, which stands a quarter of a pixel off the first (half a pixel round the head), is used
for what has thickness: the mass of his hair and the locks that hang off it, the shirt's loose sleeves, its skirt below
the rope, the rope and its knot, and the patch sewn on his chest. Every face of the first layer is opaque, so nothing shows
through where the second ends in a ragged edge.
"""
from PIL import Image

S = 2
CLEAR = (0, 0, 0, 0)


def rgb(c):
    return tuple(int(c[i:i + 2], 16) for i in (1, 3, 5))


def shade(c, n):
    return tuple(max(0, min(255, v + n)) for v in c)


def mix(a, b, t):
    return tuple(round(x + (y - x) * t) for x, y in zip(a, b))


SKIN = rgb('#c9a283')
SKIN_HI, SKIN_LO, SKIN_LO2, SKIN_DEEP = shade(SKIN, 10), shade(SKIN, -13), shade(SKIN, -27), shade(SKIN, -44)
DIRT = rgb('#5f4d3d')
GRIME, GRIME2 = mix(SKIN, DIRT, .3), mix(SKIN, DIRT, .55)
# The Minotaur's dark fur (tools/draw_minotaur.py, 'fur_dark'), its third tone as the base.
HAIR, HAIR_HI, HAIR_SHEEN, HAIR_LO = (47, 32, 24), (70, 49, 36), (92, 66, 49), (30, 20, 16)
# The Minotaur's eye, tone for tone. On a child's face the lid over it is a soft brown line and not a black one.
EYE_WHITE, EYE_WHITE_LOW, IRIS, IRIS_LOW, PUPIL = (224, 214, 200), (188, 176, 162), (80, 50, 33), (58, 36, 24), (34, 20, 14)
LID, BROW = mix(SKIN, PUPIL, .5), mix(SKIN, HAIR, .5)
# The Minotaur's rag ('cloth'), two tones up its ramp and a little past it: the same cloth with less dirt in it.
TUNIC = (132, 115, 96)
TUNIC_HI, TUNIC_LO, TUNIC_LO2, STAIN = shade(TUNIC, 12), shade(TUNIC, -15), shade(TUNIC, -30), (108, 95, 74)
# Two mends, each in what cloth there was: a washed-out blue on his chest, a piece of sacking on his back.
BLUE, BLUE_LO, SACK, SACK_LO, THREAD = (110, 118, 124), (93, 101, 108), (108, 90, 68), (92, 76, 57), (70, 58, 48)
# The Minotaur's rope, tone for tone.
ROPE, ROPE_HI, ROPE_LO = (98, 75, 56), (114, 89, 67), (68, 51, 38)
TROU = (74, 68, 62)
TROU_HI, TROU_LO, TROU_FRAY = shade(TROU, 12), shade(TROU, -14), (110, 102, 93)
ARM = 3  # the slim player model's arm, three pixels wide: a thin child's


def net(u, v, w, h, d):
    return {'top': (u + d, v, u + d + w, v + d), 'bottom': (u + d + w, v, u + d + 2 * w, v + d),
            'right': (u, v + d, u + d, v + d + h), 'front': (u + d, v + d, u + d + w, v + d + h),
            'left': (u + d + w, v + d, u + 2 * d + w, v + d + h), 'back': (u + 2 * d + w, v + d, u + 2 * d + 2 * w, v + d + h)}


ROUND = ('right', 'front', 'left', 'back')  # the four sides of a limb in the order they lie across the sheet


class Face:
    """One face of a box, drawn in its own pixels from its top left corner; nothing can be drawn outside it."""

    def __init__(self, im, box):
        self.im, self.x0, self.y0 = im, round(box[0] * S), round(box[1] * S)
        self.w, self.h = round((box[2] - box[0]) * S), round((box[3] - box[1]) * S)

    def px(self, x, y, c):
        if 0 <= x < self.w and 0 <= y < self.h:
            self.im.putpixel((self.x0 + x, self.y0 + y), c if len(c) == 4 else c + (255,))

    def row(self, y, x0, x1, c):
        for x in range(x0, x1 + 1):
            self.px(x, y, c)

    def col(self, x, y0, y1, c):
        for y in range(y0, y1 + 1):
            self.px(x, y, c)

    def rect(self, x0, y0, x1, y1, c):
        for y in range(y0, y1 + 1):
            self.row(y, x0, x1, c)

    def fill(self, c):
        self.rect(0, 0, self.w - 1, self.h - 1, c)

    def dots(self, points, c):
        for x, y in points:
            self.px(x, y, c)

    def art(self, rows, ink):
        """Rows of characters, one a pixel; a character with no ink is left as it is."""
        for y, line in enumerate(rows):
            for x, ch in enumerate(line):
                if ch in ink:
                    self.px(x, y, ink[ch])


def hair(f, seed, ends=None):
    """Dark hair that curls: here and there a lock catches the light as it comes down and turns over at its end, a dark
    pixel in the turn. With ends, the last row of hair in each column: below it the face is left clear, so that the hair
    stops in locks, each with a darker tip."""
    f.fill(HAIR)
    for cy in range(-1, f.h // 4 + 2):
        for cx in range(-1, f.w // 5 + 2):
            x0 = cx * 5 + (3 if cy % 2 else 0) + seed % 5
            y0 = cy * 4 + (cx * 7 + seed) % 3 - 1
            turn = -1 if (cx + cy + seed) % 2 == 0 else 1
            f.dots(((x0, y0), (x0, y0 + 1), (x0 + turn, y0 + 2)), HAIR_HI)
            f.px(x0 - turn, y0 + 2, HAIR_LO)
            if (cx * 3 + cy + seed) % 4 == 0:
                f.px(x0, y0, HAIR_SHEEN)
    if ends is not None:
        for x in range(f.w):
            f.px(x, ends[x], HAIR_LO if (x + seed) % 3 else HAIR)
            f.col(x, ends[x] + 1, f.h - 1, CLEAR)


def cloth(f, base=TUNIC, side=False):
    """The shirt's cloth: flat, a tone down on the body's sides, a darker thread now and then where the weave is coarse."""
    tone = shade(base, -6) if side else base
    f.fill(tone)
    for y in range(f.h):
        for x in range(f.w):
            if (x * 7 + y * 11) % 23 == 0:
                f.px(x, y, shade(tone, -7))
    return tone


def rope(f, y, shift=0):
    """Two rows of twisted rope: the strands cross on the slant."""
    for x in range(f.w):
        k = (x + shift) % 3
        f.px(x, y, ROPE_HI if k == 0 else ROPE if k == 1 else ROPE_LO)
        f.px(x, y + 1, ROPE_LO if k == 0 else ROPE_HI if k == 1 else ROPE)


def torn(base, pattern, start=0):
    """Where cloth ends round a limb: a row for every column, counted round the limb so that the tear joins at each edge."""
    return lambda i: base + pattern[(i + start) % len(pattern)]


# ---------------------------------------------------------------------------------------------------------------------------
# The head

FACE = (
    'HHHHHHHHHHHHHHHH',
    'HHhHHHHHHhHHHHHH',
    'HHHHHHdHHHHHHHdH',
    'H,,,,b,,,,b,,,,H',
    'H..bb......bb..H',
    'H..............H',
    'H..LLL....LLL..H',
    ',..WBB....BBW..,',
    ',..wPn....nPw..,',
    '....,,....,,....',
    ".......'........",
    '........,...xx..',
    '................',
    '......cmmc......',
    '....x...........',
    ',..............,',
)
FACE_INK = {'H': HAIR, 'h': HAIR_HI, 'd': HAIR_LO, '.': SKIN, "'": SKIN_HI, ',': SKIN_LO,
            'b': BROW, 'L': LID, 'W': EYE_WHITE, 'w': EYE_WHITE_LOW, 'B': IRIS, 'n': IRIS_LOW, 'P': PUPIL,
            'm': shade(SKIN, -46), 'c': shade(SKIN, -15), 'x': GRIME}
# The last row of hair in each column of the second layer. The fringe is cut by nobody: short over the brows, a lock
# down between them, longer at the temples.
FRINGE = (7, 4, 2, 2, 2, 2, 3, 6, 5, 3, 2, 2, 2, 4, 5, 8)
# Round the head from his right temple back: longest at the nape, short over each ear so that the ear shows under it.
RIGHT_LOCKS = (13, 14, 12, 14, 13, 11, 12, 10, 7, 6, 6, 7, 9, 8, 7, 7)
LEFT_LOCKS = (8, 9, 8, 10, 7, 6, 6, 7, 10, 12, 11, 13, 14, 12, 14, 13)
BACK_LOCKS = (13, 14, 12, 15, 14, 13, 15, 12, 14, 15, 13, 14, 12, 15, 14, 13)


def head_side(f, right):
    """The side of his head under the hair: the cheek and jaw toward his face, the ear, and hair behind it to the nape."""
    hair(f, 3 if right else 8)

    def x(n):  # counted from the face's edge, so that both sides are drawn once
        return f.w - 1 - n if right else n

    for n in range(0, 7):
        f.col(x(n), 7, 15, SKIN)
    f.col(x(0), 4, 6, SKIN)  # the temple, in front of the hairline
    f.col(x(6), 7, 10, HAIR)  # hair tucked behind the ear
    f.col(x(6), 11, 15, shade(SKIN, -8))
    # The ear: its rim, the hollow in it, the lobe.
    f.rect(min(x(3), x(5)), 7, max(x(3), x(5)), 10, SKIN_LO)
    f.col(x(4), 8, 9, SKIN_LO2)
    f.px(x(5), 7, SKIN)
    f.px(x(4), 11, SKIN_LO)
    f.px(x(3), 8, SKIN_HI)
    f.row(15, min(x(1), x(6)), max(x(1), x(6)), SKIN_LO)  # under the jaw
    f.dots(((x(1), 12), (x(2), 13)), GRIME)


def head(im):
    base, hat = net(0, 0, 8, 8, 8), net(32, 0, 8, 8, 8)
    Face(im, base['front']).art(FACE, FACE_INK)
    head_side(Face(im, base['right']), True)
    head_side(Face(im, base['left']), False)
    f = Face(im, base['back'])
    hair(f, 5)
    for x in range(f.w):  # the nape, under where the hair ends
        f.col(x, 13 + (x % 5 == 2), 15, shade(SKIN, -8))
    f.row(15, 0, f.w - 1, SKIN_LO2)
    hair(Face(im, base['top']), 1)
    f = Face(im, base['bottom'])
    f.fill(SKIN_LO)
    f.rect(2, 2, f.w - 3, f.h - 3, SKIN_LO2)  # where the neck joins
    # The second layer: the hair's mass, and where it ends.
    hair(Face(im, hat['top']), 2)
    hair(Face(im, hat['front']), 4, FRINGE)
    hair(Face(im, hat['right']), 6, RIGHT_LOCKS)
    hair(Face(im, hat['left']), 9, LEFT_LOCKS)
    hair(Face(im, hat['back']), 7, BACK_LOCKS)


# ---------------------------------------------------------------------------------------------------------------------------
# The body

def mend(f, x0, y0, x1, y1, tone, low):
    """A square of another cloth sewn on: a tone down along its lower and far edges, and a stitch at each corner."""
    f.rect(x0, y0, x1, y1, tone)
    f.col(x1, y0, y1, low)
    f.row(y1, x0, x1, low)
    f.dots(((x0, y0), (x1, y0), (x0, y1), (x1, y1)), THREAD)


def body(im):
    base, over = net(16, 16, 8, 12, 4), net(16, 32, 8, 12, 4)
    f = Face(im, base['front'])
    cloth(f)
    # A grown person's collar on a child: it hangs wide of his neck, and his throat and the tops of his collar bones show.
    for y, (a, b) in enumerate(((3, 12), (4, 11), (6, 9))):
        f.row(y, a, b, SKIN)
        f.px(a - 1, y, TUNIC_LO)
        f.px(b + 1, y, TUNIC_LO)
    f.row(0, 6, 9, SKIN_LO)  # under his chin
    f.dots(((4, 1), (11, 1)), SKIN_HI)
    f.dots(((5, 2), (10, 2)), TUNIC_LO)
    f.row(3, 5, 10, TUNIC_LO)
    f.dots(((7, 2), (8, 2)), SKIN_LO)
    f.dots(((4, 3), (11, 3)), TUNIC_HI)
    # Folds down the chest to the rope, and the cloth bunched over it.
    for x, y0, y1, tone in ((1, 5, 12, TUNIC_LO), (2, 6, 11, TUNIC_HI), (7, 5, 12, TUNIC_LO), (13, 10, 12, TUNIC_LO), (14, 4, 11, TUNIC_LO), (15, 6, 12, TUNIC_LO2)):
        f.col(x, y0, y1, tone)
    for x in range(0, f.w, 2):
        f.px(x, 13, TUNIC_LO)
    rope(f, 14)
    # Below the rope the shirt hangs as a skirt: gathered where the rope pulls it in, in folds below that.
    for x in range(1, f.w, 2):
        f.px(x, 16, TUNIC_LO)
    for x, y0, y1, tone in ((1, 17, 23, TUNIC_LO), (7, 17, 22, TUNIC_LO), (8, 18, 21, TUNIC_HI), (10, 17, 23, TUNIC_LO), (13, 17, 20, TUNIC_LO), (14, 19, 23, TUNIC_HI)):
        f.col(x, y0, y1, tone)
    f.dots(((3, 19), (4, 19), (4, 20), (3, 20), (4, 21)), STAIN)

    f = Face(im, base['back'])
    cloth(f)
    f.row(0, 5, 10, shade(SKIN, -8))  # the nape, in the wide collar
    f.dots(((4, 0), (11, 0)), TUNIC_LO)
    f.row(1, 5, 10, TUNIC_LO)
    for x, y0, y1, tone in ((0, 4, 12, TUNIC_LO2), (8, 3, 12, TUNIC_LO), (11, 5, 12, TUNIC_LO), (12, 6, 10, TUNIC_HI), (15, 5, 12, TUNIC_LO)):
        f.col(x, y0, y1, tone)
    mend(f, 2, 4, 5, 8, SACK, SACK_LO)  # across one shoulder blade
    for x in range(0, f.w, 2):
        f.px(x, 13, TUNIC_LO)
    rope(f, 14, 1)
    for x in range(1, f.w, 2):
        f.px(x, 16, TUNIC_LO)
    for x, y0, y1, tone in ((2, 17, 23, TUNIC_LO), (5, 17, 21, TUNIC_LO), (6, 18, 22, TUNIC_HI), (9, 17, 23, TUNIC_LO), (12, 17, 22, TUNIC_LO), (14, 18, 23, TUNIC_LO)):
        f.col(x, y0, y1, tone)

    for side, shift in (('right', 2), ('left', 0)):
        f = Face(im, base[side])
        tone = cloth(f, side=True)
        f.rect(0, 0, f.w - 1, 1, shade(tone, -22))  # under the arm
        f.col(4, 2, 12, shade(tone, -14))  # the side seam
        for x in range(0, f.w, 2):
            f.px(x, 13, shade(tone, -14))
        rope(f, 14, shift)
        f.col(4, 17, 23, shade(tone, -30))  # the shirt's tail is split up the side
        f.col(3, 17, 23, shade(tone, 10))
        f.col(1, 18, 22, shade(tone, -14))
        f.col(6, 17, 21, shade(tone, -14))

    f = Face(im, base['top'])
    cloth(f)
    f.rect(4, 1, 11, 6, SKIN_LO)  # the neck's place in the collar
    Face(im, base['bottom']).fill(TUNIC_LO2)

    # The second layer: the rope standing off the cloth, its knot and its two ends; and the mend on his chest.
    for side, shift in (('front', 0), ('back', 1), ('right', 2), ('left', 0)):
        rope(Face(im, over[side]), 14, shift)
    f = Face(im, over['front'])
    for dy, line in enumerate(('.OO.', 'OolO', 'OloO', '.OO.')):  # the knot, over his left hip, where the Minotaur's is
        for dx, ch in enumerate(line):
            if ch != '.':
                f.px(10 + dx, 13 + dy, {'O': ROPE_HI, 'o': ROPE, 'l': ROPE_LO}[ch])
    for x, y0, y1 in ((11, 17, 21), (13, 17, 19)):  # its ends
        for y in range(y0, y1 + 1):
            f.px(x, y, ROPE if (y + x) % 2 else ROPE_LO)
        f.px(x, y1 + 1, ROPE_HI)
    mend(f, 10, 5, 13, 8, BLUE, BLUE_LO)  # over his heart


# ---------------------------------------------------------------------------------------------------------------------------
# The arms

def arm(im, base, over, end, slit=False):
    """A shirt sleeve torn off short and the arm bare below it, to a child's hand: the fingers closed, dirt in the creases
    and at the nails. end is where the sleeve stops in each column round the arm."""
    i = 0
    for side in ROUND:
        f, g = Face(im, base[side]), Face(im, over[side])
        flank = side in ('right', 'left')
        e = [end(i + x) for x in range(f.w)]
        i += f.w
        for layer in (f, g):
            tone = cloth(layer, side=flank)
            layer.col(0, 1, max(e), shade(tone, -12))
            layer.col(layer.w - 1, 1, max(e), shade(tone, -12))
            if side == 'front':
                layer.col(2, 1, min(e) - 2, shade(tone, 10))
            if slit and side == 'front':  # split up the front, the arm showing through
                layer.col(4, 4, 9, shade(tone, -16))
                layer.col(3, 5, 8, SKIN if layer is f else CLEAR)
            for x in range(layer.w):
                layer.px(x, e[x], shade(tone, -16))
        for x in range(f.w):
            g.col(x, e[x] + 1, g.h - 1, CLEAR)
            f.col(x, e[x] + 1, f.h - 1, SKIN_LO if flank or x in (0, f.w - 1) else SKIN)
            f.px(x, e[x] + 1, SKIN_LO2 if flank else SKIN_LO)  # the sleeve's shadow on the arm
            if (x * 3 + e[x]) % 4 == 0:
                g.px(x, e[x] + 1, TUNIC_HI)  # a thread
        if max(e) < 10:
            f.dots(((0, 12), (f.w - 1, 12)), SKIN_LO2)  # the crook of the elbow
            if side == 'back':
                f.dots(((2, 12), (3, 12)), GRIME)
        # The hand, as the rest of the cast's: the line where the fingers close, a light on the knuckle, the thumb's line.
        f.row(f.h - 2, 1, f.w - 2, SKIN_LO2)
        f.row(f.h - 1, 0, f.w - 1, GRIME)
        if flank:
            f.col(1, f.h - 5, f.h - 3, SKIN_LO2)
        else:
            f.px(f.w - 2, f.h - 4, SKIN_HI)
    for layer in (Face(im, base['top']), Face(im, over['top'])):
        cloth(layer)
    f = Face(im, base['bottom'])
    f.fill(GRIME)
    f.rect(1, 2, f.w - 2, f.h - 3, GRIME2)  # a dirty palm


# ---------------------------------------------------------------------------------------------------------------------------
# The legs

def leg(im, base, over, hem, end, right, tear=False):
    """The shirt's tail over the top of the leg, trousers worn off below the knee, a bare shin and a bare foot. hem is
    where the shirt stops and end where the trousers do, in each column round the leg."""
    i = 0
    for side in ROUND:
        f, g = Face(im, base[side]), Face(im, over[side])
        w = f.w
        flank = side in ('right', 'left')
        h = [hem(i + x) + (0 if flank else 2) for x in range(w)]  # the tail hangs lower in front and behind
        e = [end(i + x) for x in range(w)]
        i += w
        tone = shade(TROU, -5) if flank else TROU
        f.fill(tone)
        f.col(0, 0, max(e), shade(tone, -12))
        f.col(w - 1, 0, max(e), shade(tone, -12))
        if side == 'front':
            f.rect(2, 9, 5, 10, shade(tone, 9))  # worn pale over the knee
            if tear:  # out at the knee: a slanting tear, the skin through it, the threads standing up along its top
                f.dots(((2, 10), (3, 10), (4, 10), (5, 10), (3, 11), (4, 11), (5, 11)), SKIN_LO)
                f.dots(((2, 9), (4, 9)), TROU_FRAY)
                f.px(4, 11, GRIME)
        if side == 'back':
            f.row(10, 1, w - 2, shade(tone, -12))  # the crease behind the knee
        for x in range(w):  # the bare leg under where the trousers stop
            f.px(x, e[x], shade(tone, -16))
            f.col(x, e[x] + 1, f.h - 1, SKIN_LO if flank or x in (0, w - 1) else SKIN)
            if (x * 3 + e[x]) % 4 == 0:
                f.px(x, e[x] + 1, TROU_FRAY)
        if side == 'front':
            f.col(3, max(e) + 2, 19, SKIN_HI)  # the shin bone
            f.dots(((5, 17), (5, 18), (4, 18)), GRIME)
        if side == 'back':
            f.rect(3, max(e) + 2, 4, 19, SKIN_LO)
        f.dots(((1, 20), (w - 2, 20)), SKIN_LO if flank else SKIN_LO2)  # the ankle
        # The foot: bare, the toes parted along the front, the sole's edge black with the floor.
        f.rect(0, 21, w - 1, 23, mix(SKIN, DIRT, .12))
        f.row(23, 0, w - 1, GRIME2)
        if side == 'front':
            f.row(23, 0, w - 1, GRIME)
            toes = (1, 3, 5) if right else (2, 4, 6)  # the big toe is the inner one
            f.dots([(x, 23) for x in toes], SKIN_DEEP)
            f.dots([(x, 22) for x in toes], SKIN_LO)
            f.px(6 if right else 1, 21, SKIN_HI)
        if side == 'back':
            f.rect(1, 21, w - 2, 22, SKIN_LO)  # the heel
        # The shirt's tail: on the first layer under where the second hangs, then the second itself, in folds, worn to
        # threads along its edge.
        for x in range(w):
            f.col(x, 0, h[x] - 2, shade(TUNIC, -6 if flank else 0))
            f.px(x, max(0, h[x] - 1), TUNIC_LO2)
        tone = cloth(g, side=flank)
        for x in range(w):
            if (x + i) % 3 == 0:
                g.col(x, 0, h[x] - 1, shade(tone, -14))
            elif (x + i) % 3 == 1 and not flank:
                g.col(x, 1, h[x] - 2, shade(tone, 9))
            g.px(x, h[x], shade(tone, -20))
            g.col(x, h[x] + 1, g.h - 1, CLEAR)
            if (x * 5 + i) % 4 == 0:
                g.px(x, h[x] + 1, TUNIC_HI)
    Face(im, base['top']).fill(TUNIC_LO)
    f = Face(im, base['bottom'])  # the sole
    f.fill(GRIME2)
    f.rect(1, 1, f.w - 2, 2, mix(SKIN, DIRT, .75))  # the ball of the foot and the heel, darkest
    f.rect(2, 5, f.w - 3, 6, mix(SKIN, DIRT, .75))
    f.rect(2, 3, f.w - 3, 4, GRIME)  # the arch


def paint():
    im = Image.new('RGBA', (64 * S, 64 * S), CLEAR)
    # The two flat swatches every cast sheet carries for the cut faces of an elbow and a knee: his bare arm, his trousers.
    Face(im, (0, 0, 8, 8)).fill(SKIN)
    Face(im, (24, 0, 32, 8)).fill(TROU)
    head(im)
    body(im)
    # His right sleeve is gone above the elbow; his left hangs to it, split up the front.
    arm(im, net(40, 16, ARM, 12, 4), net(40, 32, ARM, 12, 4), torn(5, (1, 0, 2, 1, 0, 1, 2, 0, 1, 1, 0, 2, 1, 2)))
    arm(im, net(32, 48, ARM, 12, 4), net(48, 48, ARM, 12, 4), torn(11, (1, 0, 2, 1, -1, 1, 2, 1, 0, 1, 2, 0, 1, 1), 3), slit=True)
    # One trouser leg is longer than the other.
    leg(im, net(0, 16, 4, 12, 4), net(0, 32, 4, 12, 4), torn(3, (0, 1, 0, 2, 1, 0, 1, 2)), torn(12, (1, 0, 1, 2, 0, 1, 1, 0)), True)
    leg(im, net(16, 48, 4, 12, 4), net(0, 48, 4, 12, 4), torn(3, (1, 0, 2, 1, 0, 1, 0, 2), 5), torn(15, (1, 0, 1, 2, 0, 1, 2, 1), 2), False, tear=True)
    # Native 256-square archive dimensions; the art itself remains the exact two-pixel grid.
    return im.resize((256, 256), Image.Resampling.NEAREST)


if __name__ == '__main__':
    import sys
    paint().save(sys.argv[1] if len(sys.argv) > 1 else 'caged_child.png')
