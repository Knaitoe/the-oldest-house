"""The game's own signs, painted (0.4.677).

The owner, of the Last Crossing: "there are several signs in last crossing that could benefit from custom textures. i was hoping
you'd notice that on your own". The station's were painted in 0.4.675 (tools/draw_crossing_signs.py). This draws the rest that
were still the game's own wooden signs with words typed on them, wherever a short sign stood:

- Camp Blood: the board on the post at the gate, the plate by each cabin's door, and the board where the sand road ends;
- the elk lot's service road gate, and the name on the yacht's side;
- Pinehook's name at the apron, and the boathouse's PRIVATE plate;
- Movie Night's ticket hut;
- the tin tags on the stakes of the graves under Borrasca.

The longer ones (Holloway's notes, the archive's labels, the road crew's notice) are lettered by the game over a painted plate
instead; their plates are drawn by tools/draw_letter_boards.py.

Made as Drisking's, Calder Falls' and the station's are, by the same hand: this draws its own pictures and gives them to
tools/draw_borrasca_signs.py's pieces, models and states, pointed at its own folder (board_sign) and its list in the game
(BoardSigns.Sign), which --check holds it to.

--check verifies everything. --sheet PATH draws every sign on one page.
"""
from pathlib import Path

import draw_borrasca_signs as signs
from draw_borrasca_signs import border, canvas, centre, field, line, measure, stamp, tone, worn, write

ROOT = Path(__file__).resolve().parents[1]

STAR = ['..#..', '#####', '.###.', '.#.#.', '#...#']
PINE = ['...#...', '..###..', '...#...', '.#####.', '..###..', '#######', '...#...', '...#...']

# Camp Blood's stain and its routed letters.
STAIN, STAIN_DARK, ROUTED = '#4a3020', '#2c1c12', '#e3d3a2'


def camp(rnd):
    """The board between two posts at the gate: two stained cedar planks, CAMP routed large between two pines, and under a routed
    rule CABINS 1-6, LIGHTS OUT, AT TEN. It stands free, so it reads from the camp side too."""
    im = canvas(48, 32)
    field(im, (0, 0, 48, 32), STAIN, rnd, 4, 2)
    border(im, (0, 0, 48, 32), STAIN_DARK)
    centre(im, 2, 'CAMP', ROUTED, True, rnd=rnd, jitter=4)
    for x in (4, 37):
        stamp(im, x, 1, PINE, {'#': '#9fb38a'})
    line(im, 4, 11, 44, 12, ROUTED)
    centre(im, 14, 'CABINS 1-6', ROUTED, rnd=rnd, jitter=4)
    centre(im, 20, 'LIGHTS OUT', ROUTED, rnd=rnd, jitter=4)
    centre(im, 26, 'AT TEN', ROUTED, rnd=rnd, jitter=4)
    for x, y in ((2, 2), (45, 2), (2, 29), (45, 29)):
        im.putpixel((x, y), tone('#1a120c'))
    worn(im, (0, 0, 48, 32), rnd, '#3a5a2c', 14)
    return im, None


def cabin(number):
    """The plate by a cabin's door: a short stained plank with the cabin's number routed into it, a nail at each end."""
    def draw(rnd):
        im = canvas(14, 11)
        field(im, (0, 0, 14, 11), STAIN, rnd, 4)
        border(im, (0, 0, 14, 11), STAIN_DARK)
        centre(im, 2, str(number), ROUTED, True, rnd=rnd, jitter=3)
        for x in (1, 12):
            im.putpixel((x, 5), tone('#1a120c'))
        worn(im, (0, 0, 14, 11), rnd, '#3a5a2c', 3)
        return im, None
    return draw


def camp_road(rnd):
    """Where the sand road ends: a stained board on two posts, SERVICE ROAD over NO CAMP TRAFFIC, routed like the gate's."""
    im = canvas(64, 16)
    field(im, (0, 0, 64, 16), STAIN, rnd, 4, 1)
    border(im, (0, 0, 64, 16), STAIN_DARK)
    centre(im, 2, 'SERVICE ROAD', ROUTED, rnd=rnd, jitter=4)
    centre(im, 9, 'NO CAMP TRAFFIC', ROUTED, rnd=rnd, jitter=4)
    worn(im, (0, 0, 64, 16), rnd, '#3a5a2c', 10)
    return im, None


def crew_gate(rnd):
    """At the elk lot's service gate, on two posts: the county's board with a double line round it, SERVICE ROAD in black, CREW ONLY
    large in red under it, and under a rule SIGN OUT HERE added by hand in marker, a little crooked."""
    im = canvas(64, 32)
    field(im, (0, 0, 64, 32), '#ecebe5', rnd, 2, 1)
    border(im, (0, 0, 64, 32), '#2c5a3a')
    border(im, (1, 1, 63, 31), '#2c5a3a')
    centre(im, 4, 'SERVICE ROAD', '#1a1a18')
    centre(im, 11, 'CREW ONLY', '#a32a20', True)
    line(im, 8, 20, 56, 21, '#2c5a3a')
    centre(im, 23, 'SIGN OUT HERE', '#23232a', rnd=rnd, jitter=6)
    for x, y in ((3, 3), (60, 3), (3, 28), (60, 28)):
        im.putpixel((x, y), tone('#6a6a66'))
    worn(im, (0, 0, 64, 32), rnd, '#8a8a82', 22)
    return im, None


def boat(rnd):
    """Painted on the yacht's port side: SECOND SUMMER in navy over a gold rule, and her home water under it."""
    im = canvas(64, 16)
    centre(im, 2, 'SECOND SUMMER', '#1f2f55', rnd=rnd, flake=.02)
    line(im, 10, 8, 54, 9, '#b8962e')
    centre(im, 10, 'INDIAN LAKE', '#1f2f55', rnd=rnd, flake=.02)
    return im, None


def pinehook(rnd):
    """At the apron: PINEHOOK burned into a pale split log, a pine at each end."""
    im = canvas(64, 16)
    field(im, (0, 1, 64, 15), '#b99366', rnd, 5)
    line(im, 0, 1, 64, 2, '#7a5a38'); line(im, 0, 14, 64, 15, '#6a4c2e')
    centre(im, 4, 'PINEHOOK', '#2a1a0e', True, rnd=rnd, jitter=5)
    for x in (2, 55):
        stamp(im, x, 4, PINE, {'#': '#2e3a22'})
    worn(im, (0, 1, 64, 15), rnd, '#5a4026', 12)
    return im, None


def private(rnd):
    """On the boathouse wall: a red tin plate, PRIVATE large over NO ENTRY in white, a white line round its edge, a screw at each
    corner and rust run from them."""
    im = canvas(48, 16)
    field(im, (0, 0, 48, 16), '#b3261c', rnd, 3)
    border(im, (0, 0, 48, 16), '#f0ece2')
    centre(im, 2, 'PRIVATE', '#f0ece2', True)
    centre(im, 10, 'NO ENTRY', '#f0ece2')
    for x, y in ((2, 2), (45, 2), (2, 13), (45, 13)):
        im.putpixel((x, y), tone('#6a6a66'))
    worn(im, (0, 0, 48, 16), rnd, '#5a2a18', 18)
    return im, None


def tickets(rnd):
    """Across the top of the ticket hut's open front, post to post: a yellow board with a row of bulbs round it, TICKETS in red,
    MOVIE NIGHT under it."""
    im = canvas(80, 16)
    field(im, (0, 0, 80, 16), '#e8c64a', rnd, 3, 1)
    border(im, (0, 0, 80, 16), '#8a2a1a')
    for x in range(2, 79, 4):
        for y in (0, 15):
            im.putpixel((x, y), tone('#fff2b8'))
    for y in range(4, 13, 4):
        for x in (0, 79):
            im.putpixel((x, y), tone('#fff2b8'))
    centre(im, 2, 'TICKETS', '#b3261c', True)
    centre(im, 10, 'MOVIE NIGHT', '#2a1a10')
    for x in (6, 70):
        stamp(im, x, 3, STAR, {'#': '#b3261c'})
    return im, None


def grave(tag):
    """A grave's stake: a tin tag nailed to its top with the number written on it in two lines, and the stake under it."""
    number, letter = tag.split('-')
    def draw(rnd):
        im = canvas(16, 16)
        for y in range(12, 16):
            im.putpixel((7, y), tone('#6b5032', rnd.randint(-8, 8))); im.putpixel((8, y), tone('#57402a', rnd.randint(-8, 8)))
        field(im, (1, 0, 15, 12), '#9aa0a3', rnd, 3)
        border(im, (1, 0, 15, 12), '#6c7276')
        centre(im, 1, number, '#1f2224', rnd=rnd, jitter=4)
        centre(im, 6, letter, '#1f2224', rnd=rnd, jitter=4)
        im.putpixel((7, 0), tone('#3a3a38'))
        worn(im, (1, 0, 15, 12), rnd, '#7a4a2a', 5)
        return im, None
    return draw


GRAVE_TAGS = ['211-A', '214-A', '219-A', '222-A', '226-A', '230-A', '234-A', '238-A', '241-A', '245-A', '247-A', '251-A', '255-A', '258-A',
              '260-A']

SIGNS = [
    # name, columns, rows, mount, painter, light
    ('camp', 3, 2, 'free', camp, 0),
    *[(f'cabin_{n}', 1, 1, 'wall', cabin(n), 0) for n in range(1, 7)],
    ('camp_road', 4, 1, 'free', camp_road, 0),
    ('crew_gate', 4, 2, 'free', crew_gate, 0),
    ('boat', 4, 1, 'decal', boat, 0),
    ('pinehook', 4, 1, 'free', pinehook, 0),
    ('private', 3, 1, 'wall', private, 0),
    ('tickets', 5, 1, 'free', tickets, 0),
    *[(f'grave_{i}', 1, 1, 'free', grave(t), 0) for i, t in enumerate(GRAVE_TAGS)],
]
PLACED = {}


_piece = signs.piece


def piece(name, im, glow, mount, light, window, left, bottom):
    """As the station's: a free board wider than a block shows, on the back of each block, the square from the board's other end."""
    m = _piece(name, im, glow, mount, light, window, left, bottom)
    w, h = im.size
    if mount == 'free' and w > 16:
        x0, y0, x1, y1 = signs.bounds(im, window)
        m['elements'][0]['faces']['south'] = {'texture': '#a', 'uv': [round((w - x1) * 16 / w, 4), round(y0 * 16 / h, 4), round((w - x0) * 16 / w, 4), round(y1 * 16 / h, 4)]}
    return m


def main():
    signs.DIR = 'board_sign'
    signs.JAVA = ROOT / 'src/main/java/io/github/knaitoe/theoldesthouse/labyrinth/BoardSigns.java'
    signs.SIGNS = SIGNS
    signs.PLACED = PLACED
    signs._PICS = None
    signs.FRAMES = {}
    signs.piece = piece
    signs.main()


if __name__ == '__main__':
    main()
