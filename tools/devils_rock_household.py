"""The Sandersons' household at Devil's Rock (0.4.241), drawn for draw_devils_rock_props.py.

The owner: "add more family details to the house... give it the same lived-in nature as the barret home", then: "Don't reuse
the barrets household pieces. I meant to look to it for design inspiration." So nothing here is the Barretts': each piece is
the Sandersons' own, and most of them are about the weeks after Tommy went: his jacket still on its peg by the door, the mail
nobody has opened, the neighbours' casseroles, Elizabeth's board of search flyers and the park map, the calendar with the day
circled, the fridge with his flyer under a magnet; Kate's paintings and her paint jars; Tommy's poster, his skateboard and the
clothes on his floor; a laundry basket nobody has carried up, a plant nobody has watered.

Every piece is a vignette_detail kind: a 16x16 texture at the game's own scale, a model whose faces each take their own part of
it, and a selection shape made of the model's boxes (KINDS, which also gives the Java shapes with --shapes).
"""
from PIL import Image

NS = 'the_oldest_house:block/'


def _h(x, y, salt):
    h = (x * 73856093) ^ (y * 19349663) ^ (salt * 83492791)
    h ^= h >> 13
    h = (h * 0x5bd1e995) & 0xffffffff
    return (h ^ (h >> 15)) & 0xffff


class Canvas:
    def __init__(self, base):
        self.img = Image.new('RGBA', (16, 16), base)

    def px(self, x, y, c):
        if 0 <= x < 16 and 0 <= y < 16:
            self.img.putpixel((x, y), c)

    def rect(self, x0, y0, x1, y1, c):
        for y in range(y0, y1):
            for x in range(x0, x1):
                self.px(x, y, c)

    def noise(self, x0, y0, x1, y1, base, alt, salt, every=5):
        for y in range(y0, y1):
            for x in range(x0, x1):
                self.px(x, y, alt if _h(x, y, salt) % every == 0 else base)


def rgb(r, g, b):
    return (r, g, b, 255)


WOOD, WOOD_D, WOOD_L = rgb(118, 82, 52), rgb(92, 63, 40), rgb(140, 100, 66)
WHITE, WHITE_D, WHITE_S = rgb(232, 230, 222), rgb(206, 204, 196), rgb(180, 178, 172)
INK = rgb(60, 58, 56)


def face(tex, uv):
    return {'texture': '#' + tex, 'uv': [round(v, 3) for v in uv]}


def box(frm, to, front, side=None, top=None, back=None, bottom=None, tex='t'):
    """A box whose front (north) face takes `front`; the rest take their own regions, or the front's when none is given."""
    side = side or front
    top = top or side
    faces = {'north': face(tex, front), 'south': face(tex, back or side), 'east': face(tex, side), 'west': face(tex, side),
             'up': face(tex, top), 'down': face(tex, bottom or top)}
    return {'from': list(frm), 'to': list(to), 'faces': faces}


def model(texture, elements):
    return {'parent': 'minecraft:block/block', 'ambientocclusion': False,
            'textures': {'t': NS + texture, 'particle': NS + texture}, 'elements': elements}


# ---------------------------------------------------------------------------------------------- the entry

def coats_tex():
    c = Canvas(WOOD)
    # Elizabeth's parka (0,0)-(4,10): red, a zip, a pocket, the hood's trim along the top.
    c.noise(0, 0, 4, 10, rgb(166, 48, 44), rgb(140, 38, 36), 1, 6)
    c.rect(0, 0, 4, 1, rgb(196, 176, 140))
    for y in range(1, 10):
        c.px(2, y, rgb(96, 28, 26))
    c.rect(0, 6, 1, 8, rgb(130, 36, 34))
    # Kate's hoodie (4,0)-(8,9): grey, its strings, the pocket across the front.
    c.noise(4, 0, 8, 9, rgb(128, 128, 134), rgb(112, 112, 118), 2, 5)
    c.px(5, 1, WHITE); c.px(6, 1, WHITE); c.px(5, 2, WHITE); c.px(6, 3, WHITE)
    c.rect(4, 6, 8, 8, rgb(108, 108, 114))
    # Tommy's denim jacket (8,0)-(12,8): the collar, a seam, two buttons.
    c.noise(8, 0, 12, 8, rgb(66, 94, 142), rgb(56, 80, 124), 3, 5)
    c.rect(8, 0, 12, 1, rgb(86, 116, 166))
    for y in range(1, 8):
        c.px(10, y, rgb(46, 66, 104))
    c.px(9, 3, rgb(204, 176, 112)); c.px(9, 5, rgb(204, 176, 112))
    # The rail (12,0)-(16,2) with its pegs, and plain wood under everything else.
    c.rect(12, 0, 16, 2, WOOD_L)
    for x in (12, 14):
        c.px(x, 1, rgb(70, 70, 74))
    return c.img


def coats_model():
    rail = box((1, 12, 14.5), (15, 14, 16), (12, 0, 16, 2), side=(12, 0, 13, 2), top=(12, 0, 16, 1.5))
    parka = box((1.5, 2, 12.8), (5.5, 12, 15.5), (0, 0, 4, 10), side=(0, 0, 2.7, 10), top=(0, 0, 4, 2.7), back=(0, 0, 4, 10))
    hoodie = box((6, 3, 13.1), (10, 12, 15.5), (4, 0, 8, 9), side=(4, 0, 6.4, 9), top=(4, 6, 8, 8.4))
    jacket = box((10.5, 4, 13.3), (14.5, 12, 15.5), (8, 0, 12, 8), side=(8, 1, 10.2, 8), top=(8, 0, 12, 2.2))
    return model('devils_coats', [rail, parka, hoodie, jacket])


def mail_tex():
    c = Canvas(WOOD)
    c.noise(0, 0, 14, 11, WOOD, WOOD_D, 4, 7)
    for y in (3, 7):
        for x in range(14):
            c.px(x, y, WOOD_D)
    c.rect(14, 0, 16, 11, WOOD_D)
    # The mail (0,11)-(6,16): envelopes overlapping, a red bill, a card with a black edge.
    c.rect(0, 11, 6, 16, WHITE)
    c.rect(0, 11, 3, 13, rgb(226, 214, 186)); c.px(2, 11, rgb(170, 60, 50))
    c.rect(3, 13, 6, 16, rgb(214, 210, 204)); c.rect(3, 13, 6, 14, rgb(40, 40, 42))
    c.rect(0, 14, 3, 16, rgb(232, 196, 190)); c.px(1, 15, INK)
    c.px(4, 11, INK); c.px(5, 12, INK)
    # Envelope edges (6,11)-(12,13) and the keys (12,11)-(14,13).
    c.rect(6, 11, 12, 13, WHITE_D)
    for x in range(6, 12, 2):
        c.px(x, 12, WHITE_S)
    c.rect(12, 11, 14, 13, rgb(196, 190, 170)); c.px(13, 12, rgb(150, 120, 60))
    return c.img


def mail_model():
    top = box((1, 11, 3), (15, 12.5, 15), (0, 9, 14, 10.5), top=(0, 0, 14, 11), side=(0, 9, 12, 10.5))
    legs = [box((x, 0, z), (x + 1.5, 11, z + 1.5), (14, 0, 15.5, 11)) for x in (1.5, 13) for z in (3.5, 13)]
    mail = box((3, 12.5, 5), (9, 13.5, 10), (6, 11, 12, 12), top=(0, 11, 6, 16), side=(6, 11, 11, 12))
    keys = box((11, 12.5, 7), (13, 12.8, 9), (12, 11, 14, 11.3), top=(12, 11, 14, 13))
    return model('devils_mail', [top, *legs, mail, keys])


# ---------------------------------------------------------------------------------------------- the kitchen

FRIDGE, FRIDGE_D, FRIDGE_S = rgb(234, 232, 226), rgb(214, 212, 206), rgb(188, 186, 182)


def fridge_low_tex():
    c = Canvas(FRIDGE)
    c.noise(0, 0, 14, 16, FRIDGE, FRIDGE_D, 5, 9)
    for y in range(2, 14):
        c.px(12, y, rgb(160, 160, 158))
    # A child's drawing under a magnet, the takeout menu, the dentist's card.
    c.rect(2, 3, 7, 8, WHITE); c.rect(3, 6, 6, 7, rgb(84, 150, 70)); c.px(4, 4, rgb(220, 190, 40)); c.px(5, 5, rgb(60, 110, 200))
    c.px(4, 3, rgb(200, 50, 50))
    c.rect(7, 9, 11, 14, rgb(226, 214, 180)); c.rect(7, 9, 11, 10, rgb(170, 50, 40))
    for y in (11, 12):
        for x in range(7, 11):
            c.px(x, y, INK if (x + y) % 2 else rgb(226, 214, 180))
    c.rect(2, 11, 5, 13, WHITE_D); c.px(3, 11, rgb(60, 120, 200))
    c.rect(0, 15, 14, 16, FRIDGE_S)
    c.rect(14, 0, 16, 16, FRIDGE_D)
    return c.img


def fridge_high_tex():
    c = Canvas(FRIDGE)
    c.noise(0, 0, 14, 16, FRIDGE, FRIDGE_D, 6, 9)
    c.rect(0, 9, 14, 10, FRIDGE_S)
    for y in range(4, 8):
        c.px(12, y, rgb(160, 160, 158))
    for y in range(11, 15):
        c.px(12, y, rgb(160, 160, 158))
    # His flyer, under a magnet on the freezer door: MISSING over a grey photograph and two lines of type.
    c.rect(2, 1, 9, 9, rgb(222, 216, 198))
    c.rect(3, 2, 8, 3, rgb(164, 38, 34))
    c.rect(4, 4, 7, 7, rgb(140, 138, 134)); c.rect(4, 4, 7, 5, rgb(58, 56, 54)); c.px(5, 5, rgb(186, 182, 176))
    c.rect(3, 8, 8, 9, rgb(120, 116, 110))
    c.px(5, 1, rgb(60, 110, 200))
    # A school schedule on the fridge door below.
    c.rect(3, 11, 8, 15, WHITE); c.rect(3, 11, 8, 12, rgb(70, 130, 90))
    for x in range(3, 8):
        c.px(x, 13, INK if x % 2 else WHITE)
    c.rect(14, 0, 16, 16, FRIDGE_D)
    return c.img


def fridge_low_model():
    return model('devils_fridge_low', [box((1, 0, 1), (15, 16, 16), (0, 0, 14, 16), side=(14, 0, 16, 16), top=(0, 15, 14, 16),
                                           back=(14, 0, 16, 16))])


def fridge_high_model():
    return model('devils_fridge_high', [box((1, 0, 1), (15, 13, 16), (0, 2, 14, 15), side=(14, 0, 16, 13), top=(0, 0, 14, 1),
                                            back=(14, 0, 16, 13))])


def calendar_tex():
    c = Canvas(WHITE)
    # The month's picture (0,0)-(8,5): pines and a grey sky; the grid (0,5)-(8,12), days crossed off, one ringed in red.
    c.rect(0, 0, 8, 2, rgb(170, 180, 186))
    for x in range(8):
        c.rect(x, 2 + (x % 2), x + 1, 5, rgb(54, 86, 60))
    c.rect(0, 5, 8, 6, rgb(100, 104, 110))
    for y in range(6, 12):
        for x in range(8):
            if (x + y) % 2 == 0:
                c.px(x, y, WHITE_S)
    for x, y in ((0, 6), (1, 6), (2, 6), (3, 6), (4, 6), (5, 6), (6, 6), (7, 6), (0, 7), (1, 7), (2, 7)):
        c.px(x, y, rgb(90, 90, 92))
    for x, y in ((3, 7), (5, 7), (4, 8), (3, 9), (5, 9)):
        c.px(x, y, rgb(190, 40, 36))
    c.rect(8, 0, 16, 16, WHITE_D)
    return c.img


def calendar_model():
    return model('devils_calendar', [box((4, 2, 15.6), (12, 14, 16), (0, 0, 8, 12), side=(8, 0, 8.4, 12), top=(8, 0, 16, 0.4))])


FOIL, FOIL_D, FOIL_L = rgb(196, 198, 204), rgb(160, 162, 170), rgb(226, 228, 232)


def casseroles_tex():
    c = Canvas(FOIL)
    # Crinkled foil (0,0)-(12,10).
    for y in range(10):
        for x in range(12):
            n = _h(x, y, 7) % 7
            c.px(x, y, FOIL_D if n == 0 else FOIL_L if n == 6 else FOIL)
    # The glass dish's side (0,10)-(12,13): food through the glass; the other dish's ceramic side (0,13)-(12,16).
    c.rect(0, 10, 12, 11, rgb(206, 220, 222))
    c.rect(0, 11, 12, 13, rgb(196, 120, 64))
    for x in range(0, 12, 3):
        c.px(x, 12, rgb(220, 180, 90))
    c.rect(0, 13, 12, 16, rgb(232, 226, 210)); c.rect(0, 15, 12, 16, rgb(70, 110, 160))
    # A note on top (12,0)-(16,3): "from the Pikes".
    c.rect(12, 0, 16, 3, rgb(236, 220, 110)); c.px(13, 1, INK); c.px(14, 1, INK); c.px(13, 2, INK)
    return c.img


def casseroles_model():
    low = box((2, 0, 3), (14, 3, 13), (0, 10, 12, 13), top=(0, 0, 12, 10), side=(0, 10, 10, 13))
    high = box((3, 3, 4), (13, 6, 12), (0, 13, 10, 16), top=(1, 1, 11, 9), side=(0, 13, 8, 16))
    note = box((6, 6, 6), (9, 6.1, 9), (12, 0, 15, 0.1), top=(12, 0, 15, 3))
    return model('devils_casseroles', [low, high, note])


CORK, CORK_D = rgb(176, 128, 82), rgb(150, 106, 66)


def search_board_tex():
    c = Canvas(WOOD_D)
    c.noise(1, 1, 15, 15, CORK, CORK_D, 8, 4)
    # The park map (1,1)-(9,8): woods, the pond, the trail, pins where they looked.
    c.rect(1, 1, 9, 8, rgb(150, 184, 120))
    c.rect(5, 5, 8, 7, rgb(96, 140, 176))
    for x, y in ((2, 2), (3, 3), (3, 4), (4, 5)):
        c.px(x, y, rgb(196, 176, 130))
    c.px(5, 3, rgb(120, 116, 110)); c.px(6, 3, rgb(120, 116, 110))
    for x, y in ((2, 6), (4, 2), (6, 4), (7, 2), (6, 6)):
        c.px(x, y, rgb(200, 40, 40))
    # Two of his flyers, and the detective's card.
    for x0 in (10, 10):
        pass
    c.rect(10, 2, 14, 8, rgb(222, 216, 198)); c.rect(10, 2, 14, 3, rgb(164, 38, 34)); c.rect(11, 4, 13, 6, rgb(130, 128, 124))
    c.rect(2, 10, 7, 14, rgb(222, 216, 198)); c.rect(2, 10, 7, 11, rgb(164, 38, 34)); c.rect(3, 12, 5, 13, rgb(130, 128, 124))
    c.rect(9, 10, 13, 12, WHITE); c.px(10, 11, INK); c.px(11, 11, INK); c.px(12, 10, rgb(60, 90, 160))
    c.px(14, 12, rgb(200, 40, 40))
    return c.img


def search_board_model():
    return model('devils_search_board', [box((0.5, 1, 15), (15.5, 15, 16), (0.5, 1, 15.5, 15), side=(0, 1, 1, 15), top=(0.5, 0, 15.5, 1))])


# ---------------------------------------------------------------------------------------------- the living room

def coffee_table_tex():
    c = Canvas(WOOD)
    c.noise(0, 0, 16, 8, WOOD, WOOD_D, 9, 6)
    for x in range(16):
        c.px(x, 4, WOOD_D)
    c.rect(0, 8, 16, 10, WOOD_D)
    # Newspapers (0,10)-(6,16): the headline bar, columns of type.
    c.rect(0, 10, 6, 16, rgb(214, 210, 200))
    c.rect(0, 10, 6, 11, rgb(70, 68, 66))
    for y in (12, 14):
        for x in range(6):
            if x != 2:
                c.px(x, y, rgb(140, 138, 134))
    c.rect(3, 12, 6, 14, rgb(96, 94, 92))
    # A mug (6,10)-(8,13) and the tissue box (8,10)-(11,13), a tissue standing out of it (11,10)-(12,12).
    c.rect(6, 10, 8, 13, WHITE); c.px(6, 10, rgb(110, 70, 40)); c.px(7, 10, rgb(110, 70, 40))
    c.rect(8, 10, 11, 13, rgb(96, 140, 180)); c.px(9, 11, WHITE)
    c.rect(11, 10, 12, 12, WHITE)
    # The magazines on the shelf (12,8)-(16,16).
    c.rect(12, 8, 16, 16, rgb(180, 70, 80)); c.rect(12, 11, 16, 13, rgb(220, 200, 90)); c.rect(12, 14, 16, 16, rgb(70, 120, 160))
    return c.img


def coffee_table_model():
    top = box((0, 7, 2), (16, 8.5, 14), (0, 8, 16, 9.5), top=(0, 0, 16, 8), side=(0, 8, 12, 9.5))
    legs = [box((x, 0, z), (x + 1.5, 7, z + 1.5), (0, 8, 1.5, 15)) for x in (0.5, 14) for z in (2.5, 12)]
    shelf = box((1, 2, 3), (15, 3, 13), (0, 8, 14, 9), top=(12, 8, 16, 16))
    papers = box((2, 8.5, 4), (8, 9.3, 10), (0, 15, 6, 15.8), top=(0, 10, 6, 16))
    mug = box((10, 8.5, 5), (11.5, 10.5, 6.5), (6, 10, 7.5, 12), top=(6, 10, 7.5, 11.5))
    tissues = box((10.5, 8.5, 8.5), (13.5, 10.5, 11.5), (8, 10, 11, 12), top=(8, 10, 11, 13))
    tissue = box((11.5, 10.5, 9.5), (12.5, 11.8, 10.5), (11, 10, 12, 11.3))
    return model('devils_coffee_table', [top, *legs, shelf, papers, mug, tissues, tissue])


def laundry_tex():
    c = Canvas(rgb(104, 150, 196))
    # Clothes heaped in the top (0,0)-(10,8): a towel, a shirt, jeans, socks.
    c.rect(0, 0, 10, 8, rgb(232, 230, 222))
    c.rect(0, 0, 4, 4, rgb(214, 120, 120)); c.rect(4, 2, 9, 6, rgb(70, 96, 140)); c.rect(1, 4, 4, 8, rgb(200, 200, 196))
    c.rect(6, 6, 10, 8, rgb(150, 160, 90)); c.px(8, 1, rgb(240, 240, 236)); c.px(9, 1, rgb(240, 240, 236))
    # The basket's sides (0,8)-(12,16): a plastic lattice.
    for y in range(8, 16):
        for x in range(12):
            if y in (8, 15) or (x % 3 == 0 and y % 2 == 0):
                c.px(x, y, rgb(84, 126, 170))
            elif (x + y) % 3 == 0:
                c.px(x, y, rgb(70, 64, 60))
    return c.img


def laundry_model():
    basket = box((2, 0, 3), (14, 8, 13), (0, 8, 12, 16), side=(0, 8, 10, 16), top=(0, 0, 10, 8))
    heap = box((3, 8, 4), (13, 9.5, 12), (0, 6, 10, 7.5), top=(0, 0, 10, 8))
    return model('devils_laundry', [basket, heap])


def plant_tex():
    c = Canvas(rgb(176, 98, 64))
    # The pot (0,0)-(6,5) with a rim, dry soil on top (6,0)-(12,6), brown and yellow leaves (0,8)-(16,12), stems (12,0)-(13,6).
    c.noise(0, 0, 6, 5, rgb(176, 98, 64), rgb(150, 82, 52), 10, 4)
    c.rect(0, 0, 6, 1, rgb(196, 116, 78))
    c.noise(6, 0, 12, 6, rgb(110, 84, 60), rgb(130, 104, 76), 11, 3)
    for x in range(16):
        for y in range(8, 12):
            c.px(x, y, (rgb(150, 112, 56), rgb(122, 92, 46), rgb(170, 150, 70), rgb(96, 104, 50))[_h(x, y, 12) % 4])
    c.rect(12, 0, 13, 6, rgb(110, 92, 52))
    return c.img


def plant_model():
    pot = box((5, 0, 5), (11, 5, 11), (0, 0, 6, 5), top=(6, 0, 12, 6))
    stem = box((7.5, 5, 7.5), (8.5, 9, 8.5), (12, 0, 13, 4))
    leaves = [box((3.5, 7, 7.5), (7.5, 7.5, 8.5), (0, 8, 4, 8.5), top=(0, 8, 4, 9)),
              box((8.5, 8, 7.5), (12.5, 8.5, 8.5), (4, 9, 8, 9.5), top=(4, 9, 8, 10)),
              box((7.5, 6.5, 3.5), (8.5, 7, 7.5), (8, 10, 9, 10.5), top=(8, 10, 12, 11)),
              box((7.5, 8.5, 8.5), (8.5, 9, 12.5), (12, 10, 13, 10.5), top=(12, 9, 16, 10)),
              box((3, 4.5, 7.5), (4, 7, 8.5), (0, 11, 1, 12))]
    return model('devils_plant', [pot, stem, *leaves])


# ---------------------------------------------------------------------------------------------- the bedrooms

def skateboard_tex():
    c = Canvas(rgb(206, 164, 62))
    # The deck's underside (0,0)-(7,16): mustard, a black zigzag he drew in marker, a sticker; grip edge (7,0)-(8,16).
    for y in range(16):
        c.px(1 + (y % 4 if y % 8 < 4 else 4 - y % 4), y, rgb(30, 28, 28))
    c.rect(3, 10, 6, 13, WHITE); c.px(4, 11, rgb(190, 40, 40))
    c.rect(0, 0, 7, 1, rgb(180, 140, 50)); c.rect(0, 15, 7, 16, rgb(180, 140, 50))
    c.rect(7, 0, 8, 16, rgb(40, 40, 42))
    # Wheels (8,0)-(10,2) and trucks (10,0)-(14,1).
    c.rect(8, 0, 10, 2, rgb(226, 222, 206)); c.rect(10, 0, 14, 1, rgb(150, 150, 156))
    return c.img


def skateboard_model():
    deck = box((4.5, 0.5, 14.6), (11.5, 15.5, 15.6), (0, 0.5, 7, 15.5), side=(7, 0, 8, 15), top=(0, 0, 7, 1), back=(7, 0, 8, 15))
    parts = []
    for y in (2, 12.5):
        parts.append(box((6, y + .25, 13.6), (10, y + 1.25, 14.6), (10, 0, 14, 1)))
        for x in (4.5, 10):
            parts.append(box((x, y, 12.8), (x + 1.5, y + 1.5, 14.6), (8, 0, 9.5, 1.5), side=(8, 0, 10, 1.5)))
    return model('devils_skateboard', [deck, *parts])


def clothes_tex():
    c = Canvas(rgb(70, 96, 140))
    # Jeans (0,0)-(7,12), the grey hoodie he wore the week before (7,0)-(14,8), a sock (7,8)-(12,10); edges (0,12)-(14,13).
    c.noise(0, 0, 7, 12, rgb(70, 96, 140), rgb(58, 82, 122), 13, 5)
    for y in range(12):
        c.px(3, y, rgb(54, 76, 114))
    c.noise(7, 0, 14, 8, rgb(124, 124, 130), rgb(108, 108, 114), 14, 5)
    c.rect(9, 1, 12, 3, rgb(108, 108, 114))
    c.rect(7, 8, 12, 10, rgb(236, 234, 228)); c.rect(7, 9, 8, 10, rgb(190, 60, 60))
    c.rect(0, 12, 14, 13, rgb(54, 76, 114))
    return c.img


def clothes_model():
    jeans = box((1, 0, 2), (8, 1, 14), (0, 12, 7, 13), top=(0, 0, 7, 12))
    hoodie = box((7, 0, 3), (14, 1.4, 11), (7, 12, 14, 13), top=(7, 0, 14, 8))
    sock = box((9, 0, 11), (14, 0.6, 13), (7, 8, 12, 8.6), top=(7, 8, 12, 10))
    return model('devils_clothes', [jeans, hoodie, sock])


def poster_tex():
    c = Canvas(rgb(20, 20, 30))
    # His poster (0,0)-(13,14): a comet over black pines, the title in letters too worn to read; edge (13,0)-(14,14).
    for y in range(10):
        for x in range(13):
            c.px(x, y, rgb(26 + y * 5, 24 + y * 4, 52 + y * 7))
    for x, y in ((1, 1), (4, 3), (11, 1), (7, 5), (2, 6), (12, 4)):
        c.px(x, y, rgb(214, 214, 230))
    for i, col in enumerate((rgb(250, 250, 255), rgb(236, 226, 170), rgb(214, 180, 110), rgb(170, 120, 80), rgb(120, 86, 76))):
        c.px(10 - i * 2, 1 + i, col)
        c.px(9 - i * 2, 1 + i, col if i else rgb(236, 226, 170))
    for x, top in enumerate((7, 6, 7, 8, 5, 7, 8, 6, 7, 5, 7, 8, 6)):
        c.rect(x, top, x + 1, 10, rgb(10, 14, 12))
    c.rect(0, 10, 13, 14, rgb(16, 16, 20))
    for x0, x1 in ((1, 4), (5, 9), (10, 12)):
        c.rect(x0, 11, x1, 12, rgb(196, 54, 48))
    c.rect(3, 13, 10, 14, rgb(110, 108, 112))
    c.rect(13, 0, 14, 14, rgb(200, 196, 186))
    return c.img


def poster_model():
    return model('devils_poster', [box((1.5, 1, 15.6), (14.5, 15, 16), (0, 0, 13, 14), side=(13, 0, 13.4, 14), top=(0, 0, 13, 0.4))])


def paintings_tex():
    c = Canvas(rgb(240, 238, 230))
    tape = rgb(236, 226, 176)
    # Three sheets (0,0)-(5,6), (5,0)-(10,6), (10,0)-(14,6): flowers in watercolour, each with a strip of tape.
    blooms = [((1, 2), rgb(220, 120, 160)), ((3, 1), rgb(236, 200, 70)), ((6, 2), rgb(150, 110, 200)), ((8, 3), rgb(220, 120, 160)),
              ((11, 1), rgb(236, 200, 70)), ((12, 3), rgb(220, 90, 80))]
    for (x, y), col in blooms:
        c.px(x, y, col); c.px(x + 1, y, col); c.px(x, y + 1, col)
        for yy in range(y + 2, 6):
            c.px(x, yy, rgb(110, 160, 90))
    for x0 in (0, 5, 10):
        c.rect(x0 + 1, 0, x0 + 3, 1, tape)
    c.rect(4, 0, 5, 6, rgb(226, 224, 216)); c.rect(9, 0, 10, 6, rgb(226, 224, 216))
    return c.img


def paintings_model():
    a = box((1, 6, 15.7), (6, 12, 16), (0, 0, 5, 6), side=(4, 0, 4.3, 6))
    b = box((6.5, 3, 15.7), (11.5, 9, 16), (5, 0, 10, 6), side=(9, 0, 9.3, 6))
    d = box((12, 7, 15.7), (16, 13, 16), (10, 0, 14, 6), side=(13, 0, 13.3, 6))
    return model('devils_paintings', [a, b, d])


def paint_jars_tex():
    c = Canvas(rgb(200, 220, 226))
    # Jars of rinse water going muddy (0,0)-(6,3), the palette (0,4)-(6,10) with blobs, the brush cup (6,0)-(8,4), brushes (8,0)-(9,4).
    for x, col in ((0, rgb(150, 190, 210)), (2, rgb(170, 150, 170)), (4, rgb(140, 130, 100))):
        c.rect(x, 0, x + 2, 3, col); c.px(x, 0, rgb(220, 232, 236))
    c.rect(0, 4, 6, 10, rgb(236, 232, 222))
    for (x, y), col in (((1, 5), rgb(220, 90, 120)), ((3, 5), rgb(236, 200, 70)), ((1, 7), rgb(110, 160, 90)), ((4, 7), rgb(150, 110, 200)),
                        ((3, 8), rgb(90, 140, 210))):
        c.px(x, y, col)
    c.rect(6, 0, 8, 4, rgb(120, 120, 126)); c.rect(8, 0, 9, 4, rgb(150, 110, 70)); c.px(8, 0, rgb(40, 36, 34))
    return c.img


def paint_jars_model():
    jars = [box((x, 0, 4), (x + 2, 3, 6), (j * 2, 0, j * 2 + 2, 3), top=(j * 2, 0, j * 2 + 2, 1)) for j, x in enumerate((2.5, 5, 7.5))]
    palette = box((3, 0, 8), (9, 0.5, 13), (0, 9.5, 6, 10), top=(0, 4, 6, 10))
    cup = box((11, 0, 9), (13, 4, 11), (6, 0, 8, 4), top=(6, 0, 8, 1))
    brushes = [box((11.5 + i, 4, 9.5 + i * .5), (12 + i, 7, 10 + i * .5), (8, 0, 8.5, 3)) for i in (0, 1)]
    return model('devils_paint_jars', [*jars, palette, cup, *brushes])


# ---------------------------------------------------------------------------------------------- the register

def _boxes(m):
    return [e['from'] + e['to'] for e in m['elements']]


# kind -> (texture name, texture, model)
KINDS = {
    'sanderson_coats': ('devils_coats', coats_tex, coats_model),
    'sanderson_mail': ('devils_mail', mail_tex, mail_model),
    'sanderson_fridge_low': ('devils_fridge_low', fridge_low_tex, fridge_low_model),
    'sanderson_fridge_high': ('devils_fridge_high', fridge_high_tex, fridge_high_model),
    'sanderson_calendar': ('devils_calendar', calendar_tex, calendar_model),
    'sanderson_casseroles': ('devils_casseroles', casseroles_tex, casseroles_model),
    'sanderson_search_board': ('devils_search_board', search_board_tex, search_board_model),
    'sanderson_coffee_table': ('devils_coffee_table', coffee_table_tex, coffee_table_model),
    'sanderson_laundry': ('devils_laundry', laundry_tex, laundry_model),
    'sanderson_plant': ('devils_plant', plant_tex, plant_model),
    'sanderson_skateboard': ('devils_skateboard', skateboard_tex, skateboard_model),
    'sanderson_clothes': ('devils_clothes', clothes_tex, clothes_model),
    'sanderson_poster': ('devils_poster', poster_tex, poster_model),
    'sanderson_paintings': ('devils_paintings', paintings_tex, paintings_model),
    'sanderson_paint_jars': ('devils_paint_jars', paint_jars_tex, paint_jars_model),
}


def java_shapes():
    """The selection shapes for VignetteDetailBlock, each the boxes of its model."""
    lines = []
    for kind, (_, _, mfn) in KINDS.items():
        boxes = ','.join('{' + ','.join(f'{v:g}' for v in b) + '}' for b in _boxes(mfn()))
        lines.append(f'            case {kind.upper()} -> new double[][]{{{boxes}}};')
    return '\n'.join(lines)
