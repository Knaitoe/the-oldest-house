"""The Last Crossing (0.4.351): the flood station where the keeper is trying to get the last cable ferry across.

Draws, in native Minecraft pixels from fixed seeds, every fitting of the crossing_fitting block, its models facing north and turned by
its facing (a wall fitting hangs on the wall south of its cell when it faces north): the porch table; Vera's clipboard with the roll,
blank and signed; the work slate on the office wall; the handprint plate in its brass bracket and standing on its foot, clear, fogged by
a clean palm on the near side, with a second print on the far side, and both; the empty bracket; the office stove with the kettle; the
pump room's pressure gauge with its needle in each of the three bands and settled; the brake lever, set and held open; the three
numbered valves and the red bypass wheel; the intake's bleed screw, shut and open; the service grate, whole and with its broken corner;
the pump, idle and running; a speaking-pipe mouth; a cable roller, seated and unseated in its cradle; the fairlead, jammed and clear; the
winch drum; the inspection hatch; the ramp sluice's wheel, free and pinned; the locking pin, up and seated; the mooring line, loose on the
ramp, coiled on the porch and staged at the ramp; the lamp hook, empty and with the station lamp; the depth post; and the ferry's cast
brass nameplate in two halves. From 0.4.352, Night One: a hand come up through the grate's broken corner, the cable house's door bar
(leaning by the door and dropped across it), the gauge room's isolation valve (open and shut), the mooring line snapped, and the skin of
the things that come up out of the water at night. From 0.4.353, Day Two: the bypass wheels on their risers (shut and open), the service
branches' grates in a wall and in a floor (dry, wet and damp), the witness plate on the cable bed (greased and scraped clean), the
winch's parking catch (off and set), the water stain under the fairlead (fresh and drying), and the cable tools rolled in canvas. From 0.4.355,
Day Three: the live feed's tagged sections (untraced and chalked), the rusted door in the intake's wall (shut and ground open), the feed
gate with its bracket (empty, the bar staged, barred and drawn), the fallen debris (heaped and half cleared), the upstream cutoff (open and
shut), the source in its flooded chamber (under water, exposed and destroyed), the master bypass (rusted fast and open), the launch rope on
its bollard (fast and slipped), the downstream sluice (open and shut), the wall racks (empty, with the launch pin and with the locking bar)
and the pin's sleeve standing empty. Also the items' pictures: the plate carried, the mooring line, the station lamp, the wrench, the cable
tools, the launch pin and the locking bar. Writes the
models and block states and the blocks' and items' names. --check verifies everything.
"""
from pathlib import Path
import argparse
import json
import random
from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
A = ROOT / 'src/main/resources/assets/the_oldest_house'
NS = 'the_oldest_house:'
P = NS + 'block/crossing/'


def rgb(c):
    c = c.lstrip('#')
    return tuple(int(c[i:i + 2], 16) for i in (0, 2, 4))


def shade(c, k, a=255):
    r, g, b = rgb(c) if isinstance(c, str) else c[:3]
    return (max(0, min(255, r + k)), max(0, min(255, g + k)), max(0, min(255, b + k)), a)


def new(base=None, seed=0, spread=4):
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    if base:
        rnd = random.Random(seed); px = im.load()
        for y in range(16):
            for x in range(16):
                px[x, y] = shade(base, rnd.randint(-spread, spread))
    return im


def fill(im, x0, y0, x1, y1, colour, a=255):
    px = im.load()
    for y in range(max(0, y0), min(16, y1)):
        for x in range(max(0, x0), min(16, x1)):
            px[x, y] = shade(colour, 0, a)


def dot(im, x, y, colour, a=255):
    if 0 <= x < 16 and 0 <= y < 16:
        im.putpixel((x, y), shade(colour, 0, a))


def frame(im, x0, y0, x1, y1, colour):
    for x in range(x0, x1):
        dot(im, x, y0, colour); dot(im, x, y1 - 1, colour)
    for y in range(y0, y1):
        dot(im, x0, y, colour); dot(im, x1 - 1, y, colour)


# A three-by-five pixel alphabet for the plates and the gauge, enough for the station's words and numbers.
FONT = {
    'C': ['111', '100', '100', '100', '111'], 'O': ['111', '101', '101', '101', '111'], 'R': ['110', '101', '110', '101', '101'],
    'D': ['110', '101', '101', '101', '110'], 'E': ['111', '100', '110', '100', '111'], 'L': ['100', '100', '100', '100', '111'],
    'I': ['111', '010', '010', '010', '111'], 'A': ['010', '101', '111', '101', '101'], '1': ['010', '110', '010', '010', '111'], '2': ['110', '001', '010', '100', '111'],
    '3': ['110', '001', '010', '001', '110'], 'S': ['111', '100', '111', '001', '111'], 'F': ['111', '100', '110', '100', '100'],
}


def letter(im, ch, x, y, colour):
    for row, bits in enumerate(FONT[ch]):
        for col, bit in enumerate(bits):
            if bit == '1':
                dot(im, x + col, y + row, colour)


# ------------------------------------------------------------------------------------------------ materials
def deal():
    # Painted deal boards, the station's grey-green, worn pale along the grain.
    im = new(base='#7c8a78', seed=3700, spread=4)
    for y in (0, 5, 10, 15):
        for x in range(16):
            dot(im, x, y, '#5e6a5c')
    rnd = random.Random(3701)
    for _ in range(10):
        dot(im, rnd.randrange(16), rnd.randrange(16), '#9aa694')
    return im


def iron(base='#4c5452', seed=3702):
    im = new(base=base, seed=seed, spread=5)
    for x, y in ((1, 1), (14, 1), (1, 14), (14, 14)):
        dot(im, x, y, shade(base, 28)[:3]); dot(im, x + 1, y + 1, shade(base, -18)[:3])
    rnd = random.Random(seed + 1)
    for _ in range(8):
        dot(im, rnd.randrange(16), rnd.randrange(16), '#7a4a2e')
    return im


def brass():
    im = new(base='#b08c3a', seed=3704, spread=8)
    for x in range(16):
        dot(im, x, 2, '#d8b860'); dot(im, x, 13, '#86682a')
    return im


def hardboard():
    return new(base='#8a6a48', seed=3705, spread=5)


def paper(signed):
    # The roll: a ruled sheet, its heading in pencil; signed, a column of names down it and one at the foot ruled across.
    im = new(base='#e6e0cc', seed=3706, spread=3)
    for y in range(3, 16, 2):
        for x in range(1, 15):
            dot(im, x, y, '#c4c0b0')
    for x in range(2, 11):
        dot(im, x, 1, '#5a5a5e')
    if signed:
        rnd = random.Random(3707)
        for y in range(4, 14, 2):
            for x in range(2, 2 + rnd.randint(5, 10)):
                if rnd.random() < .8:
                    dot(im, x, y - 1, '#3e3e48')
        for x in range(2, 9):
            dot(im, x, 14, '#3e3e48')
    return im


def slate():
    # The work slate: four chalked columns, ticks and initials, and a name with "present" by it on the last night's line.
    im = new(base='#3a3e44', seed=3708, spread=3)
    for x in (4, 8, 12):
        for y in range(1, 15):
            dot(im, x, y, '#6a6e72')
    rnd = random.Random(3709)
    for y in range(2, 14, 2):
        for col in (0, 4, 8, 12):
            for x in range(col + 1, col + 1 + rnd.randint(1, 3)):
                dot(im, x, y, '#cfd0cc')
    for x in range(1, 7):
        dot(im, x, 14, '#e8e8e2')
    return im


def glass_plate(fog, second):
    # The handprint plate's thick glass with a palm etched on the near side. A clean palm leaves a bloom of fog over the etching;
    # an infection brings up a second print, darker, on the far side, its fingers closing round where the hand lies.
    im = new(base='#9eb2b6', seed=3710, spread=3)
    for i in range(12):
        dot(im, 2 + i, 13 - i, '#c8d8da')
    palm = [(6, 9), (7, 9), (8, 9), (9, 9), (6, 10), (7, 10), (8, 10), (9, 10), (6, 11), (7, 11), (8, 11), (9, 11), (7, 12), (8, 12)]
    fingers = [(5, 4), (5, 5), (5, 6), (5, 7), (7, 3), (7, 4), (7, 5), (7, 6), (7, 7), (9, 4), (9, 5), (9, 6), (9, 7), (11, 6), (11, 7), (11, 8)]
    for x, y in palm + fingers:
        dot(im, x, y, '#86999e')
    if fog:
        for y in range(2, 14):
            for x in range(3, 13):
                d = ((x - 7.5) ** 2 + ((y - 8) * .8) ** 2) ** .5
                if d < 5.5:
                    dot(im, x, y, shade('#dfe8e8', -int(d * 4))[:3])
    if second:
        # Seen through the glass whether or not the near side is fogged.
        for x, y in fingers:
            dot(im, x + 1, y + 1, '#3e4a46')
        for x, y in palm:
            dot(im, x + 1, y + 1, '#4a5652')
        for x, y in ((4, 9), (4, 10), (12, 9), (12, 10), (13, 8)):
            dot(im, x, y, '#3e4a46')
    return im


def stove():
    im = new(base='#2c2c2e', seed=3712, spread=4)
    frame(im, 1, 1, 15, 15, '#4a4a4c')
    fill(im, 4, 9, 12, 13, '#3a2418')
    for x in range(5, 11, 2):
        dot(im, x, 11, '#c86a2a')
    return im


def enamel():
    im = new(base='#6a7e8a', seed=3713, spread=4)
    for x in range(16):
        dot(im, x, 1, '#8aa0aa')
    return im


def gauge_face(stage):
    # A dial with three bands (one, two, three) round its lower half and the settled mark at the top; the needle in a band or settled.
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    for y in range(16):
        for x in range(16):
            d = ((x - 7.5) ** 2 + (y - 7.5) ** 2) ** .5
            if d < 7.6:
                im.putpixel((x, y), shade('#e8e2cc' if d < 6.6 else '#3a3a3a', 0))
    for (x, y) in ((3, 10), (2, 9), (3, 11)):
        dot(im, x, y, '#b04a3a')
    for (x, y) in ((7, 13), (8, 13), (7, 12)):
        dot(im, x, y, '#c89a3a')
    for (x, y) in ((12, 10), (13, 9), (12, 11)):
        dot(im, x, y, '#5a7a4a')
    for (x, y) in ((7, 2), (8, 2)):
        dot(im, x, y, '#2a6a3a')
    letter(im, '1', 3, 4, '#3a3a3a'); letter(im, '3', 10, 4, '#3a3a3a')
    tip = {0: [(6, 8), (5, 9), (4, 10)], 1: [(8, 9), (8, 10), (8, 11)], 2: [(9, 8), (10, 9), (11, 10)], 3: [(8, 6), (8, 5), (8, 4), (8, 3)]}[stage]
    for x, y in tip:
        dot(im, x, y, '#1a1a1a')
    dot(im, 7, 7, '#1a1a1a'); dot(im, 8, 7, '#1a1a1a'); dot(im, 7, 8, '#1a1a1a'); dot(im, 8, 8, '#1a1a1a')
    return im


def pipe_green():
    im = new(base='#4a6a52', seed=3714, spread=4)
    for x in range(16):
        dot(im, x, 0, '#6a8a72'); dot(im, x, 15, '#34503c')
    return im


def handwheel(colour):
    # A handwheel from above: rim and four spokes, gaps cut out.
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    for y in range(16):
        for x in range(16):
            d = ((x - 7.5) ** 2 + (y - 7.5) ** 2) ** .5
            if 6 < d < 7.8 or (abs(x - 7.5) < 1 or abs(y - 7.5) < 1) and d < 7 or d < 2:
                im.putpixel((x, y), shade(colour, -10 if d > 7 else 6))
    return im


def number_plate(n):
    im = new(base='#e8e2cc', seed=3715 + n, spread=2)
    frame(im, 0, 0, 16, 16, '#3a3a3a')
    letter(im, str(n), 6, 5, '#1a1a1a')
    return im


def grate(broken):
    # The service grate: an iron frame round bars, the water dark between them; broken, its north-west corner gone.
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    for y in range(16):
        for x in range(16):
            edge = x < 1 or x > 14 or y < 1 or y > 14
            bar = x % 3 == 1 or y % 5 == 2
            if edge or bar:
                im.putpixel((x, y), shade('#4a4c4a', ((x * 7 + y * 3) % 5) - 2))
            else:
                im.putpixel((x, y), shade('#121a1c', 0))
    if broken:
        for y in range(6):
            for x in range(6 - y):
                im.putpixel((x, y), (0, 0, 0, 0))
        for x, y in ((6, 0), (5, 1), (4, 2), (3, 3), (2, 4), (1, 5), (0, 6)):
            dot(im, x, y, '#7a5a3a')
    return im


def lamp_light(on):
    im = new(base='#e8c860' if on else '#5a5a50', seed=3716, spread=6)
    return im


def manila():
    im = new(base='#b89a62', seed=3717, spread=6)
    for y in range(16):
        for x in range(16):
            if (x + y) % 4 == 0:
                dot(im, x, y, '#8a6e40')
    return im


def wire_rope():
    im = new(base='#6a6c6a', seed=3718, spread=5)
    for y in range(16):
        for x in range(16):
            if (x + 2 * y) % 5 == 0:
                dot(im, x, y, '#4a4c4a')
            elif (x + 2 * y) % 5 == 1:
                dot(im, x, y, '#8a8c88')
    return im


def roller_steel():
    im = new(base='#7a7e80', seed=3719, spread=5)
    for x in range(16):
        dot(im, x, 7, '#9a9ea0'); dot(im, x, 8, '#5a5e60')
    rnd = random.Random(3720)
    for _ in range(9):
        dot(im, rnd.randrange(16), rnd.randrange(16), '#8a5a3a')
    return im


def depth_post():
    # White-painted post with black depth marks, every foot a long mark and a number.
    im = new(base='#d8d6cc', seed=3721, spread=3)
    for y in range(0, 16, 4):
        for x in range(0, 10):
            dot(im, x, y, '#1e1e1e')
    for y in range(2, 16, 4):
        for x in range(0, 5):
            dot(im, x, y, '#1e1e1e')
    return im


def nameplate(half):
    # The ferry's name, CORDELIA, cast in brass across two halves, raised letters on a sunken ground, a bolt at each end.
    im = new(base='#8a6a2a', seed=3722 + half, spread=4)
    word = 'CORD' if half == 0 else 'ELIA'
    for i, ch in enumerate(word):
        letter(im, ch, 1 + i * 4, 6, '#e0c060')
    for x, y in ((0, 2), (15, 2), (0, 13), (15, 13)):
        dot(im, x, y, '#3a2a10')
    return im


def wood_grip():
    return new(base='#6a4a2a', seed=3723, spread=6)


def glass_lit():
    im = new(base='#f4dc8a', seed=3724, spread=10)
    fill(im, 6, 4, 10, 11, '#fff2c0')
    return im


# ------------------------------------------------------------------------------------------------ items
def plate_item():
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    fill(im, 2, 1, 14, 15, '#b08c3a'); fill(im, 3, 2, 13, 14, '#9eb2b6')
    for x, y in ((6, 9), (7, 9), (8, 9), (9, 9), (6, 10), (7, 10), (8, 10), (9, 10), (7, 11), (8, 11), (5, 5), (5, 6), (7, 4), (7, 5), (7, 6), (9, 5), (9, 6), (11, 7)):
        dot(im, x, y, '#86999e')
    return im


def line_item():
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    for y in range(16):
        for x in range(16):
            d = ((x - 7.5) ** 2 + (y - 7.5) ** 2) ** .5
            if 3 < d < 7 and int(d) % 2 == 0:
                dot(im, x, y, '#b89a62' if (x + y) % 3 else '#8a6e40')
    for i in range(5):
        dot(im, 11 + i // 2, 11 + i, '#b89a62')
    return im


def lamp_item():
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    fill(im, 5, 5, 11, 13, '#f4dc8a'); frame(im, 4, 4, 12, 14, '#2a2a2a'); fill(im, 6, 2, 10, 4, '#2a2a2a')
    for x in range(6, 10):
        dot(im, x, 0, '#3a3a3a')
    dot(im, 6, 1, '#3a3a3a'); dot(im, 9, 1, '#3a3a3a')
    return im


def wrench_item():
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    for i in range(10):
        dot(im, 3 + i, 12 - i, '#8a8c90'); dot(im, 4 + i, 12 - i, '#6a6c70')
    fill(im, 11, 1, 15, 5, '#8a8c90'); fill(im, 12, 2, 14, 4, (0, 0, 0, 0)[:3], 0)
    fill(im, 1, 12, 5, 15, '#8a8c90')
    return im


def flesh():
    # Waterlogged skin: grey going to green, the creases darker.
    im = new(base='#8a9a86', seed=3731, spread=7)
    for y in range(16):
        for x in range(16):
            if (x * 5 + y * 3) % 11 == 0:
                dot(im, x, y, '#5e6e5c')
    return im


def timber():
    # The door bar: a heavy squared timber, its grain along it.
    im = new(base='#6e5034', seed=3732, spread=5)
    for x in range(16):
        dot(im, x, 0, '#86663f'); dot(im, x, 15, '#4c3622')
    for y in (4, 9, 12):
        for x in range(16):
            if (x + y) % 3:
                dot(im, x, y, '#5c422a')
    return im


def frayed():
    # The snapped end of the line, its strands come apart.
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    rnd = random.Random(3733)
    for x in range(16):
        for y in range(16):
            if (x + rnd.randint(0, 3)) % 4 == 0:
                dot(im, x, y, '#b89a62' if rnd.random() < .6 else '#8a6e40')
    return im


def undertow_skin():
    # The things that come up out of the water at night, on the humanoid layout (64 by 64): skin like the drowned's, sodden rags of what
    # they once wore, weed and silt in the hair, eyes gone dark and the mouth hanging.
    im = Image.new('RGBA', (64, 64), (0, 0, 0, 0))
    rnd = random.Random(3740)
    def paint(x0, y0, w, h, base, spread=8):
        for y in range(y0, y0 + h):
            for x in range(x0, x0 + w):
                im.putpixel((x, y), shade(base, rnd.randint(-spread, spread)))
    def box(u, v, w, h, d, base, spread=8):
        paint(u + d, v, w, d, base, spread); paint(u + d + w, v, w, d, base, spread)
        paint(u, v + d, d, h, base, spread); paint(u + d, v + d, w, h, base, spread)
        paint(u + d + w, v + d, d, h, base, spread); paint(u + d + w + d, v + d, w, h, base, spread)
    skin, rag, hair = '#7f907c', '#4e4a3c', '#2c3428'
    box(0, 0, 8, 8, 8, skin)                       # head
    box(16, 16, 8, 12, 4, rag, 10)                 # body, in rags
    box(40, 16, 4, 12, 4, skin)                    # right arm
    box(32, 48, 4, 12, 4, skin)                    # left arm
    box(0, 16, 4, 12, 4, rag, 10)                  # right leg
    box(16, 48, 4, 12, 4, rag, 10)                 # left leg
    # Hair plastered flat with weed: the top and the back of the head, and down over the brow.
    paint(8, 0, 8, 8, hair, 6); paint(24, 8, 8, 8, hair, 6)
    for x in range(8, 16):
        for y in range(8, 8 + 1 + (x * 7) % 3):
            im.putpixel((x, y), shade(hair, rnd.randint(-6, 6)))
    for x in range(0, 8):
        for y in range(8, 11):
            im.putpixel((x, y), shade(hair, rnd.randint(-6, 6)))
    for x in range(16, 24):
        for y in range(8, 11):
            im.putpixel((x, y), shade(hair, rnd.randint(-6, 6)))
    # The face (front of the head, 8..16 by 8..16): eyes sunk dark, the mouth hanging open.
    for x, y in ((9, 11), (10, 11), (13, 11), (14, 11), (9, 12), (14, 12)):
        im.putpixel((x, y), shade('#141a16', 0))
    im.putpixel((10, 12), shade('#2a3a2c', 0)); im.putpixel((13, 12), shade('#2a3a2c', 0))
    for x in range(10, 14):
        im.putpixel((x, 14), shade('#1a1412', 0))
    for x in range(11, 13):
        im.putpixel((x, 15), shade('#1a1412', 0))
    im.putpixel((11, 13), shade('#5e6e5c', 0)); im.putpixel((12, 13), shade('#5e6e5c', 0))
    # Weed trailing from the shoulders and silt streaked down the rags.
    for x in range(20, 28, 3):
        for y in range(20, 20 + rnd.randint(3, 7)):
            im.putpixel((x, y), shade('#3a5a2e', rnd.randint(-6, 6)))
    for x in range(16, 40):
        for y in range(20, 32):
            if rnd.random() < .08:
                im.putpixel((x, y), shade('#6a5a3a', rnd.randint(-6, 6)))
    # Hands darker, the nails long.
    for u, v in ((40, 16), (32, 48)):
        for x in range(u, u + 16):
            for y in (v + 14, v + 15):
                im.putpixel((x, y), shade('#4e5e4a', rnd.randint(-4, 4)))
    return im


def grate_wet(damp):
    # A branch grate with water behind it: the bars dark and running, beads standing on them; damp, the beads drying in a fainter sheen.
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    rnd = random.Random(3741 + damp)
    for y in range(16):
        for x in range(16):
            edge = x < 1 or x > 14 or y < 1 or y > 14
            bar = x % 3 == 1 or y % 5 == 2
            if edge or bar:
                im.putpixel((x, y), shade('#3c4442' if not damp else '#464c4a', ((x * 7 + y * 3) % 5) - 2))
            else:
                im.putpixel((x, y), shade('#0c1a20' if not damp else '#141c1e', rnd.randint(-2, 2)))
    for _ in range(14 if not damp else 6):
        x, y = rnd.randrange(16), rnd.randrange(16)
        if x % 3 == 1 or y % 5 == 2:
            dot(im, x, y, '#9ab8c0' if not damp else '#7a8c90')
    if not damp:
        for x in (4, 10, 13):
            for y in range(3 + x % 4, 15, 4):
                dot(im, x, y, '#6a8a94')
    return im


def tag(letters, colour):
    # A tin tag wired to a bypass riser, enamelled, its word stamped in.
    im = new(base='#e4ddc6', seed=3743, spread=2)
    frame(im, 0, 0, 16, 16, '#3a3a3a')
    x = 8 - (len(letters) * 4 - 1) // 2
    for ch in letters:
        letter(im, ch, x, 6, colour); x += 4
    return im


def witness(clean):
    # The witness plate on the cable bed: cast iron with a mark cut square across it; greased over, the mark lost under grit and black.
    im = iron('#555c5a', 3745)
    for y in range(16):
        dot(im, 7, y, '#c8cccc'); dot(im, 8, y, '#8a9090')
    for x in (3, 4, 11, 12):
        dot(im, x, 7, '#8a9090')
    if not clean:
        rnd = random.Random(3746)
        for y in range(16):
            for x in range(16):
                if rnd.random() < .78:
                    im.putpixel((x, y), shade('#1e1c18', rnd.randint(-4, 6)))
    return im


def stain(drying):
    # The water stain spreading over the boards under the fairlead: dark and wet at its heart, ragged at its edge; drying, paler and smaller.
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    rnd = random.Random(3747)
    for y in range(16):
        for x in range(16):
            d = ((x - 8) ** 2 + (y - 9) ** 2) ** .5 + rnd.uniform(-1.6, 1.6)
            reach = 5.5 if drying else 7.5
            if d < reach:
                a = 110 if drying else 170
                im.putpixel((x, y), shade('#1a1e1c', 0, a if d < reach - 1.5 else a // 2))
    return im


def canvas():
    # The cable tools' roll: heavy canvas, its weave showing, a strap round it.
    im = new(base='#8a7a56', seed=3748, spread=5)
    for y in range(16):
        for x in range(16):
            if (x + y) % 2 == 0:
                dot(im, x, y, shade('#8a7a56', -10)[:3])
    for y in range(16):
        dot(im, 7, y, '#4a3a26'); dot(im, 8, y, '#5c4a30')
    return im


def tools_item():
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    fill(im, 3, 6, 13, 12, '#8a7a56'); frame(im, 3, 6, 13, 12, '#6a5a3e')
    fill(im, 7, 6, 9, 12, '#4a3a26')
    for i in range(4):
        dot(im, 5 + i, 5 - i, '#8a8c90')
    fill(im, 10, 2, 12, 6, '#6a4a2a')
    dot(im, 13, 4, '#8a8c90'); dot(im, 14, 3, '#8a8c90')
    return im


def prints(dry):
    # Wet prints on the boards where someone came up out of the water: two boot soles and the drips round them; dried, only the tidemarks.
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    rnd = random.Random(3751)
    for ox, oy in ((3, 2), (9, 8)):
        for y in range(6):
            for x in range(3):
                if (y == 0 or y == 5) and x != 1:
                    continue
                edge = x in (0, 2) or y in (0, 5)
                if dry and not edge:
                    continue
                dot(im, ox + x, oy + y, '#1c2422' if not dry else '#6a665a', 170 if not dry else 120)
    for _ in range(9 if not dry else 3):
        dot(im, rnd.randrange(16), rnd.randrange(16), '#1c2422' if not dry else '#6a665a', 120 if not dry else 90)
    return im


def throat(kinked):
    # The fairlead's throat from underneath: an iron collar in the passage roof, the cable through it; kinked hard into the housing, or clear.
    im = iron('#3e4644', 3753)
    frame(im, 2, 2, 14, 14, '#2a302e')
    fill(im, 4, 4, 12, 12, '#141a1a')
    if kinked:
        for i in range(8):
            dot(im, 4 + i, 6 + (i // 3), '#8c9294'); dot(im, 4 + i, 7 + (i // 3), '#5a6062')
        for y in range(4, 12):
            dot(im, 10, y, '#8c9294')
    else:
        for y in range(4, 12):
            dot(im, 7, y, '#8c9294'); dot(im, 8, y, '#5a6062')
    return im


def rust():
    # Iron gone to rust: brown and orange in flakes, the rivets standing out.
    im = new(base='#7a4a2a', seed=3761, spread=8)
    rnd = random.Random(3762)
    for _ in range(26):
        dot(im, rnd.randrange(16), rnd.randrange(16), rnd.choice(('#9a5a2a', '#5a3420', '#b06a32')))
    for x, y in ((2, 2), (13, 2), (2, 13), (13, 13), (2, 7), (13, 7)):
        dot(im, x, y, '#a87048'); dot(im, x + 1, y + 1, '#3e2414')
    return im


def tag_traced():
    # The feed's tag with a chalk arrow across it, pointing down.
    im = tag('FEED', '#1a1a1a')
    for y in range(1, 6):
        dot(im, 8, y, '#f4f4ee')
    for i in range(3):
        dot(im, 8 - i, 5 - i, '#f4f4ee'); dot(im, 8 + i, 5 - i, '#f4f4ee')
    for x in range(3, 14):
        dot(im, x, 12, '#f4f4ee')
    return im


def rubble():
    # Fallen brick and river silt packed together.
    im = new(base='#5e5a4e', seed=3763, spread=6)
    rnd = random.Random(3764)
    for _ in range(9):
        x, y = rnd.randrange(14), rnd.randrange(14)
        fill(im, x, y, x + 2 + rnd.randrange(2), y + 1 + rnd.randrange(2), rnd.choice(('#7a7672', '#6a6662', '#8a5a4a')))
    for _ in range(14):
        dot(im, rnd.randrange(16), rnd.randrange(16), '#3e3a30')
    return im


def source_flesh():
    # The source: something drowned and grown, swollen and slick, dark veins under it and weed knotted through.
    im = new(base='#8e9a84', seed=3765, spread=9)
    rnd = random.Random(3766)
    for _ in range(4):
        x, y = rnd.randrange(16), rnd.randrange(16)
        for _ in range(9):
            dot(im, x, y, '#3e4a3a'); x = (x + rnd.choice((-1, 0, 1))) % 16; y = (y + 1) % 16
    for _ in range(10):
        dot(im, rnd.randrange(16), rnd.randrange(16), '#b4bea6')
    return im


def weed():
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    rnd = random.Random(3767)
    for x in range(0, 16, 2):
        x0 = x + rnd.randrange(2)
        for y in range(rnd.randrange(4), 16):
            dot(im, x0, y, rnd.choice(('#2e4a2a', '#3a5a2e', '#24382a')))
            if rnd.random() < .3:
                x0 = max(0, min(15, x0 + rnd.choice((-1, 1))))
    return im


def pin_item():
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    for i in range(10):
        dot(im, 4 + i, 13 - i, '#6a6e70'); dot(im, 5 + i, 13 - i, '#8a8e90')
    for x, y in ((2, 13), (3, 12), (3, 14), (4, 15), (1, 14), (2, 15)):
        dot(im, x, y, '#4a4e50')
    dot(im, 14, 3, '#aeb2b4')
    return im


def lockbar_item():
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    for i in range(13):
        dot(im, 1 + i, 14 - i, '#8a3a2e'); dot(im, 2 + i, 14 - i, '#a84a3a'); dot(im, 1 + i, 13 - i, '#6a2a22')
    fill(im, 12, 1, 15, 4, '#4c5452')
    return im


def textures():
    t = {
        'deal': deal(), 'iron': iron(), 'iron_red': iron('#8a3a2e', 3726), 'brass': brass(), 'hardboard': hardboard(),
        'paper': paper(False), 'paper_signed': paper(True), 'slate': slate(),
        'plate_0': glass_plate(False, False), 'plate_1': glass_plate(True, False), 'plate_2': glass_plate(False, True), 'plate_3': glass_plate(True, True),
        'stove': stove(), 'enamel': enamel(),
        'gauge_0': gauge_face(0), 'gauge_1': gauge_face(1), 'gauge_2': gauge_face(2), 'gauge_3': gauge_face(3),
        'pipe': pipe_green(), 'wheel_black': handwheel('#2e3032'), 'wheel_red': handwheel('#a83a2a'),
        'number_1': number_plate(1), 'number_2': number_plate(2), 'number_3': number_plate(3),
        'grate': grate(False), 'grate_broken': grate(True), 'lamp_on': lamp_light(True), 'lamp_off': lamp_light(False),
        'manila': manila(), 'wire': wire_rope(), 'roller': roller_steel(), 'depth': depth_post(),
        'name_0': nameplate(0), 'name_1': nameplate(1), 'grip': wood_grip(), 'glass_lit': glass_lit(),
        'flesh': flesh(), 'timber': timber(), 'frayed': frayed(),
        'grate_wet': grate_wet(False), 'grate_damp': grate_wet(True), 'tag_shut': tag('S', '#1a1a1a'), 'tag_open': tag('O', '#a83a2a'),
        'witness_greased': witness(False), 'witness_clean': witness(True), 'stain': stain(False), 'stain_dry': stain(True), 'canvas': canvas(),
        'prints': prints(False), 'prints_dry': prints(True), 'throat_kinked': throat(True), 'throat_clear': throat(False),
        'rust': rust(), 'tag_feed': tag('FEED', '#1a1a1a'), 'tag_traced': tag_traced(), 'rubble': rubble(), 'source': source_flesh(), 'weed': weed(),
    }
    out = {f'block/crossing/{k}.png': v for k, v in t.items()}
    out['item/crossing_plate.png'] = plate_item()
    out['item/crossing_line.png'] = line_item()
    out['item/crossing_lamp.png'] = lamp_item()
    out['item/crossing_wrench.png'] = wrench_item()
    out['item/crossing_tools.png'] = tools_item()
    out['item/crossing_pin.png'] = pin_item()
    out['item/crossing_lockbar.png'] = lockbar_item()
    out['entity/crossing_undertow.png'] = undertow_skin()
    return out


# ------------------------------------------------------------------------------------------------ models
def element(lo, hi, textures, uv=None, rot=None):
    faces = {}
    for f in ('north', 'south', 'east', 'west', 'up', 'down'):
        name = textures.get(f, textures.get('all'))
        if name is None:
            continue
        faces[f] = {'texture': '#' + name}
        if uv and f in uv:
            faces[f]['uv'] = uv[f]
    e = {'from': lo, 'to': hi, 'faces': faces}
    if rot:
        e['rotation'] = rot
    return e


def model(elements, textures, particle):
    return {'ambientocclusion': False, 'render_type': 'minecraft:cutout', 'textures': {**{k: P + v for k, v in textures.items()}, 'particle': P + particle}, 'elements': elements}


def legs(top, leg, height=14, inset=1):
    e = [element([0, height - 2, 0], [16, height, 16], {'all': top})]
    for x in (inset, 16 - inset - 2):
        for z in (inset, 16 - inset - 2):
            e.append(element([x, 0, z], [x + 2, height - 2, z + 2], {'all': leg}))
    return e


def pane(lo, hi, face):
    # A pane of the plate's glass, its etched face toward the reader (north before turning), edges in brass.
    return element(lo, hi, {'north': face, 'south': face, 'east': 'b', 'west': 'b', 'up': 'b', 'down': 'b'})


def fitting_models():
    m = {}
    m['table_0'] = model(legs('d', 'd'), {'d': 'deal'}, 'deal')
    # Vera's clipboard, lying on whatever is below it: the board, the roll on it, the clip at its head.
    for stage, sheet in enumerate(('paper', 'paper_signed')):
        m[f'clipboard_{stage}'] = model([element([3, 0, 2], [13, .5, 14], {'all': 'h'}), element([3.5, .5, 2.5], [12.5, .8, 13.5], {'all': 'p', 'up': 'p'}),
                                         element([6, .5, 2], [10, 1.4, 3.5], {'all': 'c'})], {'h': 'hardboard', 'p': sheet, 'c': 'brass'}, sheet)
    # The work slate on the office wall, in a deal frame with a chalk tray.
    m['slate_0'] = model([element([0, 2, 15], [16, 14, 16], {'all': 'd'}), element([1, 3, 14.6], [15, 13, 15], {'north': 's', 'south': 's', 'east': 'd', 'west': 'd', 'up': 'd', 'down': 'd'}),
                          element([1, 2, 13.8], [15, 2.6, 15], {'all': 'd'})], {'d': 'deal', 's': 'slate'}, 'slate')
    # The handprint plate in its bracket on the wall, and standing on its foot.
    bracket = [element([1, 1, 14], [15, 2, 16], {'all': 'b'}), element([1, 2, 14], [2, 15, 16], {'all': 'b'}), element([14, 2, 14], [15, 15, 16], {'all': 'b'}),
               element([1, 14, 14], [15, 15, 16], {'all': 'b'})]
    m['bracket_0'] = model(bracket, {'b': 'brass'}, 'brass')
    for stage in range(4):
        m[f'plate_{stage}'] = model(bracket + [pane([2, 2, 14.6], [14, 14, 15.4], 'g')], {'b': 'brass', 'g': f'plate_{stage}'}, f'plate_{stage}')
        m[f'plate_set_{stage}'] = model([element([3, 0, 6], [13, 1.5, 10], {'all': 'b'}), element([2, 0, 7], [3, 14, 9], {'all': 'b'}), element([13, 0, 7], [14, 14, 9], {'all': 'b'}),
                                         pane([3, 1.5, 7.6], [13, 13.5, 8.4], 'g')], {'b': 'brass', 'g': f'plate_{stage}'}, f'plate_{stage}')
    # The office stove, the kettle on it, its flue going up.
    m['stove_0'] = model([element([2, 0, 3], [14, 10, 13], {'all': 's'}), element([10, 10, 9], [12, 16, 11], {'all': 'i'}),
                          element([4, 10, 5], [9, 14, 10], {'all': 'k'}), element([3, 12, 7], [4, 13, 8], {'all': 'k'}), element([6, 14, 7], [7, 15, 8], {'all': 'i'})],
                         {'s': 'stove', 'i': 'iron', 'k': 'enamel'}, 'stove')
    # The pressure gauge on the pump room wall, its pipe running down into the floor.
    for stage in range(4):
        m[f'gauge_{stage}'] = model([element([3, 3, 15], [13, 13, 16], {'all': 'i'}), element([4, 4, 14.4], [12, 12, 15], {'north': 'g', 'south': 'i', 'east': 'b', 'west': 'b', 'up': 'b', 'down': 'b'}),
                                     element([7, 0, 15], [9, 3, 16], {'all': 'p'})], {'i': 'iron', 'g': f'gauge_{stage}', 'b': 'brass', 'p': 'pipe'}, f'gauge_{stage}')
    # The brake lever: set (leaning back) and held open (pulled forward).
    for stage, angle in enumerate((22.5, -22.5)):
        rot = {'origin': [8, 3, 8], 'axis': 'x', 'angle': angle}
        m[f'brake_{stage}'] = model([element([5, 0, 5], [11, 3, 11], {'all': 'i'}), element([7, 3, 7], [9, 14, 9], {'all': 'i'}, rot=rot),
                                     element([6.5, 12, 6.5], [9.5, 16, 9.5], {'all': 'w'}, rot=rot)], {'i': 'iron', 'w': 'grip'}, 'iron')
    # The valves on the pump room's lower floor: a pipe run through, a stem, a handwheel, the number on the pipe; and the red bypass.
    for stage in range(4):
        wheel = 'wheel_red' if stage == 3 else 'wheel_black'
        e = [element([0, 6, 6], [16, 10, 10], {'all': 'p'}), element([7, 10, 7], [9, 13, 9], {'all': 'i'}),
             element([2, 13, 2], [14, 14, 14], {'up': 'w', 'down': 'w', 'north': 'i', 'south': 'i', 'east': 'i', 'west': 'i'})]
        tex = {'p': 'pipe', 'i': 'iron', 'w': wheel}
        if stage < 3:
            e.append(element([5, 6.5, 5.6], [11, 9.5, 6], {'north': 'n', 'south': 'n', 'east': 'n', 'west': 'n', 'up': 'n', 'down': 'n'}))
            tex['n'] = f'number_{stage + 1}'
        m[f'valve_{stage}'] = model(e, tex, 'pipe')
    # A plain length of the intake pipe, flanged at both ends.
    m['run_0'] = model([element([0, 6, 6], [16, 10, 10], {'all': 'p'}), element([0, 5, 5], [1, 11, 11], {'all': 'i'}), element([15, 5, 5], [16, 11, 11], {'all': 'i'})],
                       {'p': 'pipe', 'i': 'iron'}, 'pipe')
    # The bleed screw on the intake pipe, shut and backed out.
    for stage in range(2):
        top = 15.5 if stage else 14
        m[f'bleed_{stage}'] = model([element([0, 4, 5], [16, 10, 11], {'all': 'p'}), element([6, 10, 6], [10, 12, 10], {'all': 'b'}),
                                     element([7, 12, 7], [9, top, 9], {'all': 'b'}), element([6, top - 1, 7.5], [10, top, 8.5], {'all': 'i'})],
                                    {'p': 'pipe', 'b': 'brass', 'i': 'iron'}, 'pipe')
    # The service grate, set flush as the floor: whole, and with its broken corner.
    for stage, tex in enumerate(('grate', 'grate_broken')):
        m[f'grate_{stage}'] = model([element([0, 15, 0], [16, 16, 16], {'up': 'g', 'down': 'g', 'north': 'i', 'south': 'i', 'east': 'i', 'west': 'i'})],
                                    {'g': tex, 'i': 'iron'}, 'iron')
    # Night One: a hand come up through the broken corner, the wrist out of the black water and the fingers hooked over the iron.
    hand = [element([1, 15, 1], [4, 24, 4], {'all': 'f'}, rot={'origin': [2.5, 16, 2.5], 'axis': 'x', 'angle': 22.5}),
            element([.5, 22, 3], [1.5, 27, 4], {'all': 'f'}), element([2, 23, 3.5], [3, 28, 4.5], {'all': 'f'}), element([3.5, 22, 3], [4.5, 27, 4], {'all': 'f'}),
            element([.5, 26, 4], [1.5, 27, 6], {'all': 'f'}), element([2, 27, 4.5], [3, 28, 6.5], {'all': 'f'}), element([3.5, 26, 4], [4.5, 27, 6], {'all': 'f'})]
    m['grate_2'] = model([element([0, 15, 0], [16, 16, 16], {'up': 'g', 'down': 'g', 'north': 'i', 'south': 'i', 'east': 'i', 'west': 'i'})] + hand,
                         {'g': 'grate_broken', 'i': 'iron', 'f': 'flesh'}, 'iron')
    # The pump, its motor on top and its lamp: dark idle, lit running.
    for stage, lamp in enumerate(('lamp_off', 'lamp_on')):
        m[f'pump_{stage}'] = model([element([1, 0, 1], [15, 12, 15], {'all': 'i'}), element([4, 12, 4], [12, 16, 12], {'all': 'g'}),
                                    element([11, 8, .4], [14, 11, 1], {'all': 'l'}), element([0, 3, 6], [1, 7, 10], {'all': 'p'}), element([15, 3, 6], [16, 7, 10], {'all': 'p'})],
                                   {'i': 'iron', 'g': 'pipe', 'l': lamp, 'p': 'pipe'}, 'iron')
    # A speaking pipe's mouth on the wall: the pipe up the wall and the brass horn turned out from it.
    m['pipe_0'] = model([element([7, 0, 14], [9, 16, 16], {'all': 'p'}), element([5, 7, 11], [11, 13, 14], {'all': 'b', 'north': 'i'}),
                         element([6.5, 8.5, 10.6], [9.5, 11.5, 11], {'all': 'i'})], {'p': 'pipe', 'b': 'brass', 'i': 'iron'}, 'brass')
    # A cable roller in its cradle, its axle across the cable's run: seated, the cable lying over it; unseated, slewed off it.
    cradle = [element([1, 0, 4], [3, 7, 12], {'all': 'i'}), element([13, 0, 4], [15, 7, 12], {'all': 'i'})]
    m['roller_0'] = model(cradle + [element([3, 3, 5], [13, 8, 11], {'all': 'r'}), element([7.5, 8, 0], [8.5, 9, 16], {'all': 'w'})], {'i': 'iron', 'r': 'roller', 'w': 'wire'}, 'roller')
    m['roller_1'] = model(cradle + [element([3, 0, 3], [12, 5, 9], {'all': 'r'}, rot={'origin': [8, 2, 6], 'axis': 'y', 'angle': 22.5}),
                                    element([7.5, 8, 0], [8.5, 9, 16], {'all': 'w'})], {'i': 'iron', 'r': 'roller', 'w': 'wire'}, 'roller')
    # The fairlead in the cable house's river wall: jammed, the cable kinked against its housing; clear, running straight out.
    lead = [element([2, 4, 11], [14, 12, 16], {'all': 'i'}), element([5, 6, 10], [11, 10, 11], {'all': 'b'})]
    m['fairlead_0'] = model(lead + [element([7, 7, 0], [9, 9, 6], {'all': 'w'}), element([6, 8, 3], [8, 10, 11], {'all': 'w'}, rot={'origin': [7, 9, 7], 'axis': 'y', 'angle': 45})],
                            {'i': 'iron', 'b': 'brass', 'w': 'wire'}, 'iron')
    m['fairlead_1'] = model(lead + [element([7, 7, 0], [9, 9, 11], {'all': 'w'})], {'i': 'iron', 'b': 'brass', 'w': 'wire'}, 'iron')
    m['drum_0'] = model([element([1, 2, 2], [15, 14, 14], {'all': 'w'}), element([0, 0, 1], [1, 16, 15], {'all': 'i'}), element([15, 0, 1], [16, 16, 15], {'all': 'i'}),
                         element([0, 7, 7], [16, 9, 9], {'all': 'r'})], {'w': 'wire', 'i': 'iron', 'r': 'roller'}, 'wire')
    # The inspection hatch in the floor: shut, its ring flat; open, the lid standing against its hinge.
    m['hatch_0'] = model([element([0, 14, 0], [16, 16, 16], {'all': 'i'}), element([6, 16, 6], [10, 16.5, 10], {'all': 'b'})], {'i': 'iron', 'b': 'brass'}, 'iron')
    m['hatch_1'] = model([element([0, 14, 15], [16, 30, 16], {'all': 'i'}), element([0, 14, 0], [1, 16, 16], {'all': 'i'}), element([15, 14, 0], [16, 16, 16], {'all': 'i'})],
                         {'i': 'iron'}, 'iron')
    # The ramp sluice's wheel on its spindle: free, held, and pinned through its spokes.
    wheel = [element([7, 0, 7], [9, 10, 9], {'all': 'i'}), element([0, 10, 0], [16, 11, 16], {'up': 'w', 'down': 'w', 'north': 'i', 'south': 'i', 'east': 'i', 'west': 'i'})]
    m['wheel_0'] = model(wheel, {'i': 'iron', 'w': 'wheel_black'}, 'iron')
    m['wheel_1'] = model(wheel, {'i': 'iron', 'w': 'wheel_black'}, 'iron')
    m['wheel_2'] = model(wheel + [element([7, 8, 0], [9, 13, 16], {'all': 'r'})], {'i': 'iron', 'w': 'wheel_black', 'r': 'roller'}, 'iron')
    # The locking pin in its sleeve on the upper ramp: up, and seated.
    sleeve = [element([5, 0, 5], [11, 8, 11], {'all': 'i'})]
    m['pin_0'] = model(sleeve + [element([7, 8, 7], [9, 16, 9], {'all': 'r'}), element([4, 14, 7], [12, 15.5, 9], {'all': 'w'})], {'i': 'iron', 'r': 'roller', 'w': 'grip'}, 'iron')
    m['pin_1'] = model(sleeve + [element([7, 8, 7], [9, 9.5, 9], {'all': 'r'}), element([4, 8.5, 7], [12, 10, 9], {'all': 'w'})], {'i': 'iron', 'r': 'roller', 'w': 'grip'}, 'iron')
    # The mooring line: loose and trailing on the ramp, coiled down neat on the porch, and staged at the head of the ramp on a cleat.
    m['coil_0'] = model([element([2, 0, 2], [14, 1.5, 14], {'all': 'm'}), element([4, 1.5, 4], [12, 2.5, 12], {'all': 'm'}), element([7, 0, 0], [9, 1, 2], {'all': 'm'})], {'m': 'manila'}, 'manila')
    m['coil_1'] = model([element([3, 0, 3], [13, 3, 13], {'all': 'm'}), element([5, 3, 5], [11, 4, 11], {'all': 'm'})], {'m': 'manila'}, 'manila')
    m['coil_2'] = model([element([3, 0, 3], [13, 3, 13], {'all': 'm'}), element([1, 0, 7], [15, 1.5, 9], {'all': 'i'}), element([7, 3, 2], [9, 5, 14], {'all': 'm'})], {'m': 'manila', 'i': 'iron'}, 'manila')
    # The lamp hook on the wall, empty and with the station lamp hanging from it.
    hook = [element([7, 13, 14], [9, 15, 16], {'all': 'i'}), element([7.5, 14, 9], [8.5, 15, 16], {'all': 'i'})]
    m['hook_0'] = model(hook, {'i': 'iron'}, 'iron')
    m['hook_1'] = model(hook + [element([5, 3, 6], [11, 10, 12], {'all': 'g', 'up': 'i', 'down': 'i'}), element([5.5, 10, 6.5], [10.5, 11.5, 11.5], {'all': 'i'}),
                                element([7.5, 11.5, 8.5], [8.5, 14, 9.5], {'all': 'i'})], {'i': 'iron', 'g': 'glass_lit'}, 'glass_lit')
    # The line snapped at the first rise: a short coil left on the cleat and its frayed end.
    m['coil_3'] = model([element([4, 0, 4], [12, 2, 12], {'all': 'm'}), element([1, 0, 7], [15, 1.5, 9], {'all': 'i'}), element([7, 0, 0], [9, 1, 5], {'all': 'm'}),
                         element([6, 0, -1], [10, .5, 1], {'all': 'x'})], {'m': 'manila', 'i': 'iron', 'x': 'frayed'}, 'manila')
    # The cable house's door bar: leaning on the wall by the door, and dropped across the doorway in its iron brackets.
    m['bar_0'] = model([element([5, 0, 13], [11, 28, 16], {'all': 't'})], {'t': 'timber'}, 'timber')
    m['bar_1'] = model([element([0, 5, 6], [16, 9, 10], {'all': 't'}), element([0, 14, 6], [16, 18, 10], {'all': 't'}),
                        element([0, 3, 5], [1, 20, 11], {'all': 'i'}), element([15, 3, 5], [16, 20, 11], {'all': 'i'})], {'t': 'timber', 'i': 'iron'}, 'timber')
    # The gauge room's isolation valve on the wall: a gate valve on a riser, its lever up (open) or across (shut).
    riser = [element([7, 0, 14], [9, 16, 16], {'all': 'p'}), element([5, 5, 12], [11, 10, 16], {'all': 'i'}), element([7, 10, 13], [9, 11, 14], {'all': 'i'})]
    m['isolator_0'] = model(riser + [element([7.5, 11, 12.5], [8.5, 17, 13.5], {'all': 'r'})], {'p': 'pipe', 'i': 'iron', 'r': 'iron_red'}, 'pipe')
    m['isolator_1'] = model(riser + [element([2, 10, 12.5], [14, 11, 13.5], {'all': 'r'})], {'p': 'pipe', 'i': 'iron', 'r': 'iron_red'}, 'pipe')
    m['depth_0'] = model([element([6, 0, 6], [10, 16, 10], {'all': 'd'})], {'d': 'depth'}, 'depth')
    # Day Two: a bypass wheel on its riser out of the floor, the tag wired to it saying which way it stands; open, the wheel turned through.
    for stage, (label, angle) in enumerate((('tag_shut', 0), ('tag_open', 45))):
        rot = {'origin': [8, 13.5, 8], 'axis': 'y', 'angle': angle} if angle else None
        m[f'bypass_{stage}'] = model([element([6, 0, 6], [10, 13, 10], {'all': 'p'}), element([5, 0, 5], [11, 2, 11], {'all': 'i'}),
                                      element([2, 13, 2], [14, 14, 14], {'up': 'w', 'down': 'w', 'north': 'i', 'south': 'i', 'east': 'i', 'west': 'i'}, rot=rot),
                                      element([6.5, 6, 5.4], [9.5, 10, 6], {'all': 't'})], {'p': 'pipe', 'i': 'iron', 'w': 'wheel_red', 't': label}, 'pipe')
    # A branch's grate set in a wall, the whole cell barred, and the same set in a floor: dry, wet and damp.
    for stage, tex in enumerate(('grate', 'grate_wet', 'grate_damp')):
        m[f'branch_{stage}'] = model([element([0, 0, 0], [16, 16, 16], {'north': 'g', 'south': 'g', 'east': 'i', 'west': 'i', 'up': 'i', 'down': 'i'})], {'g': tex, 'i': 'iron'}, 'iron')
        m[f'floor_grate_{stage}'] = model([element([0, 0, 0], [16, 16, 16], {'up': 'g', 'down': 'g', 'north': 'i', 'south': 'i', 'east': 'i', 'west': 'i'})], {'g': tex, 'i': 'iron'}, 'iron')
    # The witness plate on the cable bed, greased over and scraped clean.
    for stage, tex in enumerate(('witness_greased', 'witness_clean')):
        m[f'witness_{stage}'] = model([element([2, 0, 2], [14, 2, 14], {'up': 'w', 'down': 'i', 'north': 'i', 'south': 'i', 'east': 'i', 'west': 'i'})], {'w': tex, 'i': 'iron'}, tex)
    # The winch's parking catch: a pawl on a post beside the brake, lifted clear (off) and dropped into the drum's teeth (set).
    for stage, angle in enumerate((-45, 0)):
        rot = {'origin': [8, 11, 8], 'axis': 'x', 'angle': angle} if angle else None
        m[f'catch_{stage}'] = model([element([6, 0, 6], [10, 11, 10], {'all': 'i'}), element([7, 10, 1], [9, 12, 9], {'all': 'r'}, rot=rot),
                                     element([6.5, 9.5, 0], [9.5, 12.5, 2], {'all': 'g'}, rot=rot)], {'i': 'iron', 'r': 'roller', 'g': 'grip'}, 'iron')
    # The water stain on the boards under the fairlead, fresh and drying.
    for stage, tex in enumerate(('stain', 'stain_dry')):
        m[f'stain_{stage}'] = model([element([0, 0, 0], [16, .1, 16], {'up': 's'})], {'s': tex}, tex)
    # Night Two: the floor grate pinned shut, a pin driven through its bars into the frame.
    m['floor_grate_3'] = model([element([0, 0, 0], [16, 16, 16], {'up': 'g', 'down': 'g', 'north': 'i', 'south': 'i', 'east': 'i', 'west': 'i'}),
                                element([1, 16, 7], [15, 17, 9], {'all': 'r'}), element([0, 16, 6.5], [2, 18, 9.5], {'all': 'i'})], {'g': 'grate_wet', 'i': 'iron', 'r': 'roller'}, 'iron')
    # Wet prints on the boards, and dried to a tidemark.
    for stage, tex in enumerate(('prints', 'prints_dry')):
        m[f'prints_{stage}'] = model([element([0, 0, 0], [16, .1, 16], {'up': 'p'})], {'p': tex}, tex)
    # The fairlead's throat seen from the flooded passage under it: a block of the passage roof, the collar and the cable in its underside.
    for stage, tex in enumerate(('throat_kinked', 'throat_clear')):
        m[f'underside_{stage}'] = model([element([0, 0, 0], [16, 16, 16], {'down': 't', 'up': 'i', 'north': 'i', 'south': 'i', 'east': 'i', 'west': 'i'})], {'t': tex, 'i': 'iron'}, tex)
    # The cable tools rolled in their canvas, the handles out of one end.
    m['tools_0'] = model([element([2, 0, 5], [14, 3, 11], {'all': 'c'}), element([14, 1, 6], [16, 2, 7], {'all': 'i'}), element([14, 1, 8.5], [15.5, 2, 9.5], {'all': 'w'})],
                         {'c': 'canvas', 'i': 'iron', 'w': 'grip'}, 'canvas')
    # Day Three: the pin's sleeve standing empty.
    m['pin_2'] = model(sleeve + [element([6.5, 7.6, 6.5], [9.5, 8, 9.5], {'all': 'r'})], {'i': 'iron', 'r': 'roller'}, 'iron')
    # A tagged section of the live feed on its wall: the pipe up the wall, a flange, and the tag wired to it; traced, the tag chalked.
    for stage, tex in enumerate(('tag_feed', 'tag_traced')):
        m[f'feed_{stage}'] = model([element([7, 0, 14], [9, 16, 16], {'all': 'p'}), element([6, 5, 13], [10, 7, 16], {'all': 'i'}),
                                    element([4, 8, 13.4], [12, 14, 13.8], {'north': 't', 'south': 't', 'east': 'i', 'west': 'i', 'up': 'i', 'down': 'i'})],
                                   {'p': 'pipe', 'i': 'iron', 't': tex}, 'pipe')
    # The rusted door in the intake's east wall, two cells high: shut flush with the wall, and ground open against its jamb.
    m['gallery_door_0'] = model([element([0, 0, 13], [16, 32, 16], {'all': 'r'}), element([12, 13, 12], [14, 18, 13], {'all': 'i'}),
                                 element([0, 30, 12.5], [16, 32, 13], {'all': 'i'})], {'r': 'rust', 'i': 'iron'}, 'rust')
    m['gallery_door_1'] = model([element([0, 0, 0], [3, 32, 16], {'all': 'r'}), element([3, 13, 2], [4, 18, 4], {'all': 'i'})], {'r': 'rust', 'i': 'iron'}, 'rust')
    # The feed gate in the gallery's wall, its plate toward the gallery (north before turning), the bracket for the locking bar beside it:
    # empty, the bar staged upright in it, the bar dropped across the plate, and the plate drawn up off a black gap.
    housing = [element([0, 0, 0], [16, 16, 16], {'north': 'r', 'south': 'i', 'east': 'i', 'west': 'i', 'up': 'i', 'down': 'i'}),
               element([7, 9, -1], [9, 16, 0], {'all': 'i'}), element([12, 1, -1.5], [15, 3, 0], {'all': 'i'}), element([12, 12, -1.5], [15, 14, 0], {'all': 'i'})]
    m['feed_gate_0'] = model(housing, {'r': 'rust', 'i': 'iron'}, 'rust')
    m['feed_gate_1'] = model(housing + [element([12.5, 0, -1.3], [14.5, 16, -.3], {'all': 'b'})], {'r': 'rust', 'i': 'iron', 'b': 'iron_red'}, 'rust')
    m['feed_gate_2'] = model(housing + [element([0, 7, -1.6], [16, 9, -.4], {'all': 'b'})], {'r': 'rust', 'i': 'iron', 'b': 'iron_red'}, 'rust')
    m['feed_gate_3'] = model([element([0, 6, 0], [16, 16, 16], {'north': 'r', 'south': 'i', 'east': 'i', 'west': 'i', 'up': 'i', 'down': 'i'}),
                              element([0, 0, 2], [16, 6, 16], {'north': 'k', 'up': 'k', 'south': 'i', 'east': 'i', 'west': 'i', 'down': 'i'}),
                              element([12.5, 0, -1.3], [14.5, 16, -.3], {'all': 'b'})], {'r': 'rust', 'i': 'iron', 'b': 'iron_red', 'k': 'stain'}, 'rust')
    # Fallen brick and silt heaped to the passage's roof, and half cleared.
    m['debris_0'] = model([element([0, 0, 0], [16, 16, 16], {'all': 'd'}), element([0, 16, 2], [16, 28, 14], {'all': 'd'}), element([2, 28, 4], [14, 32, 12], {'all': 'd'})],
                          {'d': 'rubble'}, 'rubble')
    m['debris_1'] = model([element([0, 0, 0], [16, 9, 16], {'all': 'd'}), element([2, 9, 3], [11, 14, 10], {'all': 'd'}), element([9, 9, 9], [14, 12, 14], {'all': 'd'})],
                          {'d': 'rubble'}, 'rubble')
    # The upstream cutoff: a big handwheel on a stem out of its wall, the feed going up out of it; shut, the wheel turned through.
    for stage, angle in enumerate((0, 45)):
        rot = {'origin': [8, 8, 11.5], 'axis': 'z', 'angle': angle} if angle else None
        m[f'cutoff_{stage}'] = model([element([6, 6, 12], [10, 10, 16], {'all': 'i'}), element([7, 10, 14], [9, 16, 16], {'all': 'p'}),
                                      element([1, 1, 11], [15, 15, 12], {'north': 'w', 'south': 'w', 'east': 'i', 'west': 'i', 'up': 'i', 'down': 'i'}, rot=rot)],
                                     {'i': 'iron', 'p': 'pipe', 'w': 'wheel_black'}, 'iron')
    # The source on the floor of its chamber, wrapped in weed; destroyed, torn open and sunk flat.
    mass = [element([2, 0, 2], [14, 9, 14], {'all': 's'}), element([4, 8, 3], [12, 14, 11], {'all': 's'}), element([1, 0, 6], [3, 7, 12], {'all': 'g'}),
            element([13, 0, 3], [15, 10, 8], {'all': 'g'}), element([5, 13, 9], [9, 16, 13], {'all': 'g'})]
    m['source_0'] = model(mass, {'s': 'source', 'g': 'weed'}, 'source')
    m['source_1'] = model(mass, {'s': 'source', 'g': 'weed'}, 'source')
    m['source_2'] = model([element([1, 0, 1], [15, 3, 15], {'all': 's'}), element([3, 3, 4], [8, 5, 9], {'all': 'g'}), element([10, 3, 8], [13, 4, 13], {'all': 'k'})],
                          {'s': 'source', 'g': 'weed', 'k': 'stain'}, 'source')
    # The master bypass beside the sluice wheel: a rusted wheel on its riser, fast; and turned open.
    for stage, angle in enumerate((0, 45)):
        rot = {'origin': [8, 10.5, 8], 'axis': 'y', 'angle': angle} if angle else None
        m[f'master_{stage}'] = model([element([5, 0, 5], [11, 2, 11], {'all': 'i'}), element([6, 2, 6], [10, 10, 10], {'all': 'r'}),
                                      element([2, 10, 2], [14, 11, 14], {'up': 'w', 'down': 'w', 'north': 'r', 'south': 'r', 'east': 'r', 'west': 'r'}, rot=rot)],
                                     {'i': 'iron', 'r': 'rust', 'w': 'wheel_red'}, 'rust')
    # The launch rope made fast round its bollard, its tail running to the ferry and the tripping lever beside it; slipped, the bollard bare
    # and the lever thrown.
    bollard = [element([5, 0, 5], [11, 12, 11], {'all': 'i'}), element([4, 12, 4], [12, 13.5, 12], {'all': 'i'})]
    m['release_0'] = model(bollard + [element([4.5, 6, 4.5], [11.5, 9, 11.5], {'all': 'm'}), element([7, 6.5, 0], [9, 8, 5], {'all': 'm'}),
                                      element([12, 0, 7], [13.5, 13, 9], {'all': 'b'}, rot={'origin': [12.75, 0, 8], 'axis': 'z', 'angle': -22.5})],
                           {'i': 'iron', 'm': 'manila', 'b': 'iron_red'}, 'iron')
    m['release_1'] = model(bollard + [element([12, 0, 7], [13.5, 13, 9], {'all': 'b'}, rot={'origin': [12.75, 0, 8], 'axis': 'z', 'angle': 22.5})],
                           {'i': 'iron', 'b': 'iron_red'}, 'iron')
    # The downstream sluice: a big wheel on its frame over the outfall, its tag; shut, the wheel turned through.
    for stage, angle in enumerate((0, 45)):
        rot = {'origin': [8, 12.5, 8], 'axis': 'y', 'angle': angle} if angle else None
        m[f'downstream_{stage}'] = model([element([1, 0, 1], [15, 4, 15], {'all': 'i'}), element([6, 4, 6], [10, 12, 10], {'all': 'p'}),
                                          element([0, 12, 0], [16, 13, 16], {'up': 'w', 'down': 'w', 'north': 'i', 'south': 'i', 'east': 'i', 'west': 'i'}, rot=rot),
                                          element([6.5, 6, 5.4], [9.5, 10, 6], {'all': 't'})], {'i': 'iron', 'p': 'pipe', 'w': 'wheel_red', 't': 'tag_open'}, 'iron')
    # A wall rack: a board and two hooks; with the launch pin hanging from it, and with the locking bar laid across the hooks.
    rack = [element([1, 6, 15], [15, 12, 16], {'all': 'd'}), element([3, 7, 12], [4, 9, 15], {'all': 'i'}), element([12, 7, 12], [13, 9, 15], {'all': 'i'})]
    m['rack_0'] = model(rack, {'d': 'deal', 'i': 'iron'}, 'deal')
    m['rack_1'] = model(rack + [element([7.5, 2, 13], [8.5, 12, 14], {'all': 'r'}), element([6.5, 11, 12.8], [9.5, 13.5, 14.2], {'all': 'i'})],
                        {'d': 'deal', 'i': 'iron', 'r': 'roller'}, 'deal')
    m['rack_2'] = model(rack + [element([0, 9, 12.5], [16, 11, 14.5], {'all': 'b'})], {'d': 'deal', 'i': 'iron', 'b': 'iron_red'}, 'deal')
    # The ferry's nameplate, bolted to its rail in two halves.
    for half in range(2):
        m[f'nameplate_{half}'] = model([element([0, 5, 15], [16, 11, 16], {'all': 'b', 'north': 'n'})], {'b': 'brass', 'n': f'name_{half}'}, f'name_{half}')
    return m


KINDS = ['table', 'clipboard', 'slate', 'plate', 'plate_set', 'bracket', 'stove', 'gauge', 'brake', 'valve', 'run', 'bleed', 'grate', 'pump', 'pipe', 'roller',
         'fairlead', 'drum', 'hatch', 'wheel', 'pin', 'coil', 'hook', 'depth', 'nameplate', 'bar', 'isolator',
         'bypass', 'branch', 'floor_grate', 'witness', 'catch', 'stain', 'tools', 'prints', 'underside',
         'feed', 'gallery_door', 'feed_gate', 'debris', 'cutoff', 'source', 'master', 'release', 'downstream', 'rack']
STAGES = {'clipboard': 2, 'plate': 4, 'plate_set': 4, 'gauge': 4, 'brake': 2, 'valve': 4, 'bleed': 2, 'grate': 3, 'pump': 2, 'roller': 2, 'fairlead': 2,
          'hatch': 2, 'wheel': 3, 'pin': 3, 'coil': 4, 'hook': 2, 'nameplate': 2, 'bar': 2, 'isolator': 2,
          'bypass': 2, 'branch': 3, 'floor_grate': 4, 'witness': 2, 'catch': 2, 'stain': 2, 'prints': 2, 'underside': 2,
          'feed': 2, 'gallery_door': 2, 'feed_gate': 4, 'debris': 2, 'cutoff': 2, 'source': 3, 'master': 2, 'release': 2, 'downstream': 2, 'rack': 3}
FACING_Y = {'north': 0, 'east': 90, 'south': 180, 'west': 270}
MAX_STAGE = 4
NAMES = {'crossing_fitting': 'Station Fitting'}
ENTITIES = {'crossing_undertow': 'Undertow'}
ITEMS = {'crossing_plate': 'Handprint Plate', 'crossing_line': 'Mooring Line', 'crossing_lamp': 'Station Lamp', 'crossing_wrench': 'Wrench', 'crossing_tools': 'Cable Tools',
         'crossing_pin': 'Launch Pin', 'crossing_lockbar': 'Locking Bar'}


def files():
    out = {}
    for key, obj in fitting_models().items():
        out[f'models/block/crossing/{key}.json'] = obj
    variants = {}
    for kind in KINDS:
        for facing, y in FACING_Y.items():
            for stage in range(MAX_STAGE):
                drawn = stage if stage < STAGES.get(kind, 1) else 0
                v = {'model': NS + f'block/crossing/{kind}_{drawn}'}
                if y:
                    v['y'] = y
                variants[f'facing={facing},kind={kind},stage={stage}'] = v
    out['blockstates/crossing_fitting.json'] = {'variants': variants}
    for item in ITEMS:
        out[f'models/item/{item}.json'] = {'parent': 'minecraft:item/generated', 'textures': {'layer0': NS + f'item/{item}'}}
    return out


def main():
    check = argparse.ArgumentParser()
    check.add_argument('--check', action='store_true')
    check = check.parse_args().check
    images, data = textures(), files()
    models = fitting_models()
    for kind in KINDS:
        for stage in range(STAGES.get(kind, 1)):
            assert f'{kind}_{stage}' in models, (kind, stage)
    for key, obj in models.items():
        for tex in obj['textures'].values():
            assert f'block/crossing/{tex.split("crossing/")[1]}.png' in images, (key, tex)
    for rel, im in images.items():
        path = A / 'textures' / rel
        if check:
            with Image.open(path) as actual:
                assert actual.convert('RGBA').tobytes() == im.tobytes(), rel
        else:
            path.parent.mkdir(parents=True, exist_ok=True); im.save(path, optimize=True)
    for rel, obj in data.items():
        path = A / rel
        if check:
            assert json.loads(path.read_text()) == obj, rel
        else:
            path.parent.mkdir(parents=True, exist_ok=True); path.write_text(json.dumps(obj, indent=2) + '\n')
    lang_path = A / 'lang/en_us.json'; lang = json.loads(lang_path.read_text())
    wanted = {**{f'block.the_oldest_house.{k}': v for k, v in NAMES.items()}, **{f'item.the_oldest_house.{k}': v for k, v in ITEMS.items()},
              **{f'entity.the_oldest_house.{k}': v for k, v in ENTITIES.items()}}
    for key, name in wanted.items():
        if check:
            assert lang.get(key) == name, key
        else:
            lang[key] = name
    if not check:
        lang_path.write_text(json.dumps(lang, indent=2, ensure_ascii=False) + '\n')
    print(('Verified' if check else 'Drew') + f' the flood station: {len(images)} textures, {len(data)} models and states.')


if __name__ == '__main__':
    main()
