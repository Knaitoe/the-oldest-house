"""The Last Crossing (0.4.675): the flood station's painted signs.

The owner, of the board over the office porch: "custom sign textures"; and, of the station as a whole, that it had several signs
which could have them. Each was one of the game's own wooden signs: the name board over the porch, the keeper's plate by the office
door, the barricade where the road goes under the water, and the warning on the pump room's wall over the ramp.

Made as Drisking's and Calder Falls' are, by the same hand: this draws its own pictures and gives them to tools/draw_borrasca_signs.py's
pieces, models and states, pointed at the station's folder (crossing_sign) and its list in the game (CrossingSigns.Sign), which --check
holds it to. A sign is a picture sixteen pixels to the block, lettered in the picture; a large one is cut into a block-sized square for
each block of it. The barricade's board stands free and is read from both sides, so the back of each of its blocks shows the square from
the board's other end (as Calder's boards do).

--check verifies everything. --sheet PATH draws every sign on one page.
"""
from pathlib import Path

import draw_borrasca_signs as signs
from draw_borrasca_signs import border, canvas, centre, field, line, measure, stamp, tone, worn, write

ROOT = Path(__file__).resolve().parents[1]

# The station's own colours: its painted deal, gone grey-green; the river's silt.
DEAL, DEAL_DARK = '#2f4136', '#1c2921'
CREAM, CREAM_SHADOW = '#e2dbc2', '#121a15'
SILT = '#6e5f45'
WAVE = ['.##...##.', '#..#.#..#']


def station(rnd):
    """The name board over the porch: two planks painted the station's green, cream letters, ROOK FERRY large and FLOOD STATION under it,
    a wave at each end of the lower line, nail heads at the planks' ends weeping rust, and across the lower letters the line of dried silt
    where the river once stood against the wall."""
    im = canvas(80, 16)
    field(im, (0, 0, 80, 16), DEAL, rnd, 3, 8)
    border(im, (0, 0, 80, 16), DEAL_DARK)
    centre(im, 1, 'ROOK FERRY', CREAM, True, shadow=CREAM_SHADOW, rnd=rnd, jitter=5)
    centre(im, 9, 'FLOOD STATION', CREAM, rnd=rnd, jitter=5, flake=.04)
    for x in (4, 67):
        stamp(im, x, 11, WAVE, {'#': '#a9b9a6'})
    # The nails: one at each end of each plank, and a rust run under the lower ones.
    for x in (1, 78):
        for y in (2, 10):
            im.putpixel((x, y), tone('#3b342c'))
        im.putpixel((x, 11), tone('#6b3f22', a=200)); im.putpixel((x, 12), tone('#6b3f22', a=140))
    # The silt line: a soft band across the board, darkest at its top edge where the water stood longest.
    px = im.load()
    for x in range(1, 79):
        top = 12 + (1 if (x * 7) % 11 < 3 else 0)
        for y, k in ((top, 120), (top + 1, 70), (top + 2, 40)):
            if y < 15:
                r, g, b, a = px[x, y]
                s = tone(SILT)
                f = k / 255
                px[x, y] = (round(r * (1 - f) + s[0] * f), round(g * (1 - f) + s[1] * f), round(b * (1 - f) + s[2] * f), 255)
    worn(im, (0, 0, 80, 16), rnd, '#4a5a4c', 40)
    return im, None


def keeper(rnd):
    """The keeper's plate by the office door: enamel, its letters and rim the same dark, a screw at each end, chipped at a corner."""
    im = canvas(27, 9)
    field(im, (0, 0, 27, 9), '#e9e5d8', rnd, 2)
    border(im, (0, 0, 27, 9), '#24302a')
    write(im, 2, 2, 'KEEPER', '#1d2520')
    for x in (1, 25):
        im.putpixel((x, 4), tone('#8a8478'))
    # A chip out of the enamel at the lower right, the iron dark under it.
    for x, y in ((24, 7), (25, 7), (25, 6)):
        im.putpixel((x, y), tone('#3a3632'))
    return im, None


def road(rnd):
    """The barricade where the road goes under: a board on the sawhorse's legs, white, with red and white bands slanting down at each
    end, ROAD FLOODED in black, and the mud of the last high water dried up its lower edge."""
    im = canvas(48, 16)
    field(im, (0, 0, 48, 16), '#ecebe4', rnd, 2)
    # The bands at the ends, slanting down toward the middle.
    px = im.load()
    for y in range(16):
        for x in list(range(0, 8)) + list(range(40, 48)):
            d = (x + y) if x < 8 else (47 - x + y)
            if (d // 3) % 2 == 0:
                px[x, y] = tone('#b8231c', rnd.randint(-6, 6))
    line(im, 8, 0, 9, 16, '#2a2a28'); line(im, 39, 0, 40, 16, '#2a2a28')
    border(im, (0, 0, 48, 16), '#2a2a28')
    ink = set(centre(im, 1, 'ROAD', '#1a1a18', True, x0=9, x1=39))
    ink |= set(centre(im, 9, 'FLOODED', '#1a1a18', x0=9, x1=39))
    # Dried mud splashed up from the bottom edge, thick at the foot and thinning upward, never over the letters.
    for x in range(0, 48):
        h = rnd.choice((1, 2, 2, 3, 3))
        for k in range(h):
            y = 15 - k
            if (x, y) not in ink:
                px[x, y] = tone('#7a6648', rnd.randint(-10, 10), 255 if k < 2 else 180)
    worn(im, (0, 0, 48, 16), rnd, '#8a8a82', 18)
    return im, None


def ramp(rnd):
    """Painted straight onto the pump room's wall over the ramp: a panel of white, RAMP in black, KEEP CLEAR in red under it. The paint
    has come away from the stone at the panel's edges, so its outline is ragged."""
    im = canvas(48, 16)
    field(im, (2, 0, 46, 16), '#e6e3d8', rnd, 3)
    px = im.load()
    # Ragged edges: the stone showing through where the paint has lifted.
    for y in range(16):
        for x in (2, 3, 44, 45):
            if rnd.random() < (.55 if x in (2, 45) else .25):
                px[x, y] = (0, 0, 0, 0)
    for x in range(2, 46):
        for y in (0, 15):
            if rnd.random() < .45:
                px[x, y] = (0, 0, 0, 0)
    centre(im, 1, 'RAMP', '#1c1c1a', True, rnd=rnd, flake=.03, jitter=4)
    centre(im, 10, 'KEEP CLEAR', '#a32a20', rnd=rnd, flake=.05, jitter=6)
    # A drip from the foot of the P, where the brush was overloaded.
    x = 2 + (48 - measure('RAMP', True)) // 2 + measure('RAM', True) + 1
    for y in (8, 9):
        px[x, y] = tone('#1c1c1a', a=210 if y == 8 else 150)
    worn(im, (2, 0, 46, 16), rnd, '#b9b5aa', 50, False)
    return im, None


SIGNS = [
    # name, columns, rows, mount, painter, light
    ('station', 5, 1, 'wall', station, 0), ('keeper', 1, 1, 'wall', keeper, 0),
    ('road', 3, 1, 'free', road, 0), ('ramp', 3, 1, 'decal', ramp, 0),
]
# The keeper's plate is wider than its block: it hangs at eye height on the wall beside the door, reaching away from the door.
PLACED = {'keeper': (-12, 4)}


_piece = signs.piece


def piece(name, im, glow, mount, light, window, left, bottom):
    """As Calder's: a free board wider than a block shows, on the back of each block, the square from the board's other end."""
    m = _piece(name, im, glow, mount, light, window, left, bottom)
    w, h = im.size
    if mount == 'free' and w > 16:
        x0, y0, x1, y1 = signs.bounds(im, window)
        m['elements'][0]['faces']['south'] = {'texture': '#a', 'uv': [round((w - x1) * 16 / w, 4), round(y0 * 16 / h, 4), round((w - x0) * 16 / w, 4), round(y1 * 16 / h, 4)]}
    return m


def main():
    signs.DIR = 'crossing_sign'
    signs.JAVA = ROOT / 'src/main/java/io/github/knaitoe/theoldesthouse/labyrinth/CrossingSigns.java'
    signs.SIGNS = SIGNS
    signs.PLACED = PLACED
    signs._PICS = None
    signs.FRAMES = {}
    signs.piece = piece
    signs.main()


if __name__ == '__main__':
    main()
