"""The new ordinary halls (0.4.390, docs/LAUNDRY_CORRIDOR_0_4_390.md): materials, washing and fittings for the halls of
docs/HALLS_TRIALS_AND_TIME_DESIGN.md ("New hallway variations").

Draws, in native Minecraft pixels from fixed seeds:

- the laundry corridor (0.4.390): limewashed brick with the courses showing through, the same brick tarred at its foot, worn
  quarry tiles, a boarded ceiling; washing on the lines (a plain sheet, a striped ticking cover and a towel, the line and its
  pegs), a wicker basket of folded washing and a wooden stool.
- the long bedroom hall (0.4.391): sprigged wallpaper, a panelled dado, boards and a runner, a plaster ceiling, the door frames,
  and one bedroom door, drawn once: painted onto the wall eighteen times, and on the two doors that open.
- the atrium (0.4.391): panelled plaster, a chequered floor, the balcony's boards, a coffered ceiling, stone steps, a skylight
  lit from above, and a turned balustrade.
- the cistern walk (0.4.391): wet dressed stone, the walk's flags, silted stone under the water, the columns' drums and a brick
  vault.
- the library stacks (0.4.391): dark panelling, worn boards, a ceiling between beams, shelves packed with books in two fillings,
  and a reading table.

The washing is modelled at every stage of its swing (HallSheetBlock): pushed, swung back and settling, the cloth turning about
the line so the two halves of a sheet stay one sheet.

Writes the models and block states, and the blocks' names. --check verifies everything written.
"""
from pathlib import Path
import argparse
import json
import random
from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
A = ROOT / 'src/main/resources/assets/the_oldest_house'
NS = 'the_oldest_house:'
P = NS + 'block/ordinary_hall/'

LIME = '#c9c3b2'
LIME_SHADOW = '#aea795'
BRICK = '#7b4a3a'
MORTAR = '#8e877a'
TAR = '#25211e'
QUARRY = '#7c4a38'
QUARRY_JOINT = '#4a3127'
BOARD = '#b8ad94'
BOARD_SEAM = '#7f765f'
LINEN = '#dcd6c6'
LINEN_FOLD = '#bdb6a3'
TICKING = '#d6d0bf'
STRIPE = '#4b5866'
TERRY = '#c7b497'
BAND = '#7a5a46'
ROPE = '#9d8a66'
ROPE_DARK = '#6f5f45'
PEG = '#a07e55'
WICKER = '#9a7a4c'
WICKER_DARK = '#6a5232'
SPRIG_GROUND = '#b8b49a'
SPRIG = '#6d7a5c'
SPRIG_BLOOM = '#9a6a64'
DADO = '#8c8a78'
FLOOR = '#7a5c40'
RUNNER = '#6b3a34'
RUNNER_BORDER = '#c2a66a'
PAINT = '#c9c4b2'
DOOR = '#8f7b5c'
DOOR_SHADE = '#6d5c43'
BRASS = '#b8953f'
STUCCO = '#c2b49a'
MARBLE_A = '#d8d2c2'
MARBLE_B = '#3b3a38'
STEP = '#9c968a'
SKY = '#e8e2c8'
LEAD = '#4a4a46'
BALUSTER = '#d7d0bd'
RAIL = '#4c3828'
ASHLAR = '#5b5f58'
WET = '#3d423d'
FLAG = '#6c6a60'
SILT = '#4a5240'
DRUM = '#77746a'
VAULT_BRICK = '#5b4d44'
PANEL = '#3b2c22'
OLD_BOARD = '#5e4632'
BEAM = '#3a2a1e'
SPINES = ['#6b2f2a', '#2f4a3a', '#3a3f5c', '#7a6a3c', '#4a3328', '#5c2f45', '#2c3b3f', '#8a7a5a']


def rgb(c):
    c = c.lstrip('#')
    return tuple(int(c[i:i + 2], 16) for i in (0, 2, 4))


def shade(c, k, a=255):
    r, g, b = rgb(c) if isinstance(c, str) else c[:3]
    return (max(0, min(255, r + k)), max(0, min(255, g + k)), max(0, min(255, b + k)), a)


def mix(c, d, t):
    a, b = rgb(c) if isinstance(c, str) else c[:3], rgb(d) if isinstance(d, str) else d[:3]
    return tuple(int(round(a[i] + (b[i] - a[i]) * t)) for i in range(3)) + (255,)


def brick_courses(x, y):
    """Whether a pixel is mortar in a running bond of bricks four pixels high and eight long."""
    course = y // 4
    return y % 4 == 3 or (x + (4 if course % 2 else 0)) % 8 == 7


def limewash(seed, tarred):
    """Brick under limewash: the courses show through as faint shadow, and here and there the wash has flaked to the brick.
    Tarred: the same wall at its foot, painted black against the wet, the tar's top edge ragged and the wash above it stained."""
    rnd = random.Random(seed)
    im = Image.new('RGBA', (16, 16))
    for y in range(16):
        for x in range(16):
            k = rnd.randint(-3, 3)
            c = shade(LIME_SHADOW, k) if brick_courses(x, y) else shade(LIME, k)
            im.putpixel((x, y), c)
    for _ in range(3 if not tarred else 1):
        x0, y0 = rnd.randrange(1, 13), rnd.randrange(1, 13)
        for dx in range(rnd.randint(2, 3)):
            for dy in range(rnd.randint(1, 2)):
                if not brick_courses(x0 + dx, y0 + dy):
                    im.putpixel((x0 + dx, y0 + dy), shade(BRICK, rnd.randint(-6, 6)))
    if tarred:
        edge = [rnd.choice((4, 5, 5, 6)) for _ in range(16)]
        for x in range(16):
            for y in range(edge[x], 16):
                k = rnd.randint(-3, 3) + (6 if y == edge[x] else 0)
                im.putpixel((x, y), shade(TAR, k))
            for y in range(max(0, edge[x] - 2), edge[x]):
                im.putpixel((x, y), mix(im.getpixel((x, y)), TAR, .25))
    return im


def quarry(seed):
    """Quarry tiles, four to a block, each fired its own tone, the joints dark and the middles rubbed smooth and paler."""
    rnd = random.Random(seed)
    im = Image.new('RGBA', (16, 16))
    tones = [rnd.randint(-9, 9) for _ in range(4)]
    for y in range(16):
        for x in range(16):
            tile = (y // 8) * 2 + x // 8
            centre = 3 if 2 <= x % 8 <= 5 and 2 <= y % 8 <= 5 else 0
            im.putpixel((x, y), shade(QUARRY, tones[tile] + rnd.randint(-3, 3) + centre))
    for i in range(16):
        for j in (0, 8):
            im.putpixel((i, j), shade(QUARRY_JOINT, rnd.randint(-2, 2)))
            im.putpixel((j, i), shade(QUARRY_JOINT, rnd.randint(-2, 2)))
    for _ in range(4):
        im.putpixel((rnd.randrange(16), rnd.randrange(16)), shade(QUARRY, 22))
    return im


def ceiling_boards(seed):
    """Tongue-and-groove boards painted pale and gone yellow, four to a block, the grooves dark."""
    rnd = random.Random(seed)
    im = Image.new('RGBA', (16, 16))
    tones = [rnd.randint(-4, 4) for _ in range(4)]
    for y in range(16):
        for x in range(16):
            im.putpixel((x, y), shade(BOARD, tones[y // 4] + rnd.randint(-2, 2)))
    for x in range(16):
        for y in (3, 7, 11, 15):
            im.putpixel((x, y), shade(BOARD_SEAM, rnd.randint(-2, 2)))
    return im


def weave(rnd, base, x, y):
    """A plain weave: alternate threads a touch lighter, so the cloth reads as cloth up close."""
    return shade(base, (2 if (x + y) % 2 else -1) + rnd.randint(-2, 2))


def linen(seed, upper):
    """A plain sheet. Upper: the top folded over the line, a crease below the fold and the shadows of the pegs. Lower: the rest
    of it, a soft crease down it and the hem stitched across."""
    rnd = random.Random(seed)
    im = Image.new('RGBA', (16, 16))
    for y in range(16):
        for x in range(16):
            im.putpixel((x, y), weave(rnd, LINEN, x, y))
    if upper:
        for x in range(16):
            im.putpixel((x, 2), shade(LINEN_FOLD, rnd.randint(-2, 2)))
            im.putpixel((x, 3), shade(LINEN, -6))
        for px in (3, 11):
            for y in range(1, 3):
                im.putpixel((px, y), shade(LINEN_FOLD, -8))
    for y in range(16):
        x = 9 + (1 if y % 7 > 3 else 0)
        im.putpixel((x, y), mix(im.getpixel((x, y)), LINEN_FOLD, .6))
    if not upper:
        for x in range(16):
            im.putpixel((x, 8), shade(LINEN_FOLD, rnd.randint(-2, 1)))
            im.putpixel((x, 9), shade(LINEN, -10))
    return im


def ticking(seed, upper):
    """A striped ticking cover: narrow dark stripes in pairs on a pale ground, the top over the line or the hem at the foot."""
    rnd = random.Random(seed)
    im = Image.new('RGBA', (16, 16))
    for y in range(16):
        for x in range(16):
            stripe = x % 8 in (1, 3)
            im.putpixel((x, y), shade(STRIPE, rnd.randint(-3, 3)) if stripe else weave(rnd, TICKING, x, y))
    if upper:
        for x in range(16):
            im.putpixel((x, 2), mix(im.getpixel((x, 2)), LINEN_FOLD, .7))
    else:
        for x in range(16):
            im.putpixel((x, 9), mix(im.getpixel((x, 9)), STRIPE, .5))
    return im


def towel(seed):
    """A towel: looped cotton, flecked, a woven band near its foot and the fringe below it."""
    rnd = random.Random(seed)
    im = Image.new('RGBA', (16, 16))
    for y in range(16):
        for x in range(16):
            im.putpixel((x, y), shade(TERRY, rnd.randint(-6, 4)))
    for x in range(16):
        im.putpixel((x, 8), shade(BAND, rnd.randint(-3, 3)))
        im.putpixel((x, 9), shade(BAND, rnd.randint(-3, 3) - 6))
        if x % 2 == 0:
            im.putpixel((x, 11), shade(TERRY, -14))
    for x in range(16):
        im.putpixel((x, 2), shade(TERRY, -10))
    return im


def line(seed):
    """The line, a twisted cord (top rows), and the wood of the pegs (below)."""
    rnd = random.Random(seed)
    im = Image.new('RGBA', (16, 16), shade(PEG, 0))
    for x in range(16):
        im.putpixel((x, 0), shade(ROPE, 4) if x % 3 else shade(ROPE_DARK, 0))
        im.putpixel((x, 1), shade(ROPE_DARK, 0) if x % 3 == 1 else shade(ROPE, -4))
    for y in range(2, 16):
        for x in range(16):
            im.putpixel((x, y), shade(PEG, rnd.randint(-4, 4) + (6 if x % 4 == 1 else 0)))
    for x in range(16):
        im.putpixel((x, 8), shade(PEG, -20))
    return im


def wicker(seed):
    """Wicker: stakes upright every four pixels, the weavers passed in and out of them, the rim bound at the top."""
    rnd = random.Random(seed)
    im = Image.new('RGBA', (16, 16))
    for y in range(16):
        for x in range(16):
            over = ((x // 2) + (y // 2)) % 2 == 0
            c = shade(WICKER, rnd.randint(-4, 4) + (8 if over else -4))
            if x % 4 == 0:
                c = shade(WICKER_DARK, rnd.randint(-3, 3))
            im.putpixel((x, y), c)
    for x in range(16):
        for y in (0, 1):
            im.putpixel((x, y), shade(WICKER, 10 - y * 8 + rnd.randint(-2, 2)))
    return im


def folded(seed):
    """Folded washing seen from above: sheets laid square, the folds' edges in shadow."""
    rnd = random.Random(seed)
    im = Image.new('RGBA', (16, 16))
    for y in range(16):
        for x in range(16):
            im.putpixel((x, y), weave(rnd, LINEN, x, y))
    for i in range(16):
        im.putpixel((i, 5), shade(LINEN_FOLD, -4))
        im.putpixel((i, 11), shade(LINEN_FOLD, -4))
        im.putpixel((7, i), shade(LINEN_FOLD, 0))
    for y in range(5, 11):
        for x in range(8, 16):
            if (x + y) % 4 == 0:
                im.putpixel((x, y), shade(STRIPE, 20))
    return im


MATERIALS = [
    ('laundry_wall', 'Limewashed Brick'),
    ('laundry_wall_low', 'Tarred Brick'),
    ('laundry_floor', 'Quarry Tiles'),
    ('laundry_ceiling', 'Boarded Ceiling'),
    ('bedroom_wall', 'Sprigged Wallpaper'),
    ('bedroom_dado', 'Panelled Dado'),
    ('bedroom_floor', 'Hall Boards'),
    ('bedroom_runner', 'Hall Runner'),
    ('bedroom_ceiling', 'Hall Ceiling'),
    ('bedroom_trim', 'Door Frame'),
    ('atrium_wall', 'Panelled Plaster'),
    ('atrium_floor', 'Chequered Marble'),
    ('atrium_deck', 'Balcony Boards'),
    ('atrium_ceiling', 'Coffered Ceiling'),
    ('atrium_step', 'Stone Step'),
    ('atrium_skylight', 'Skylight'),
    ('cistern_wall', 'Wet Ashlar'),
    ('cistern_floor', 'Causeway Flags'),
    ('cistern_under', 'Silted Stone'),
    ('cistern_pillar', 'Column Drum'),
    ('cistern_vault', 'Vault Brick'),
    ('stacks_wall', 'Dark Panelling'),
    ('stacks_floor', 'Worn Boards'),
    ('stacks_ceiling', 'Beamed Ceiling'),
]


def sprig_paper(seed):
    """Wallpaper: a pale ground with a faint stripe, a small sprig of leaves and a bud repeated in a half-drop, eight to a block."""
    rnd = random.Random(seed)
    im = Image.new('RGBA', (16, 16))
    for y in range(16):
        for x in range(16):
            im.putpixel((x, y), shade(SPRIG_GROUND, rnd.randint(-2, 2) - (3 if x % 8 == 0 else 0)))
    for ox, oy in ((4, 2), (12, 10)):
        for dx, dy in ((0, 0), (0, 1), (-1, 1), (1, 2), (0, 2)):
            im.putpixel(((ox + dx) % 16, (oy + dy) % 16), shade(SPRIG, rnd.randint(-3, 3)))
        im.putpixel((ox % 16, (oy - 1) % 16), shade(SPRIG_BLOOM, 0))
    return im


def dado(seed):
    """A panelled dado painted over: a rail along the top, a sunk panel with its moulding, the skirting at the foot."""
    rnd = random.Random(seed)
    im = Image.new('RGBA', (16, 16))
    for y in range(16):
        for x in range(16):
            im.putpixel((x, y), shade(DADO, rnd.randint(-2, 2)))
    for x in range(16):
        im.putpixel((x, 0), shade(DADO, 18)); im.putpixel((x, 1), shade(DADO, -12))
        im.putpixel((x, 14), shade(DADO, -10)); im.putpixel((x, 15), shade(DADO, -18))
    for x in range(2, 14):
        im.putpixel((x, 3), shade(DADO, -10)); im.putpixel((x, 12), shade(DADO, 10))
    for y in range(3, 13):
        im.putpixel((2, y), shade(DADO, -10)); im.putpixel((13, y), shade(DADO, 10))
    return im


def floorboards(seed, base, seams):
    """Boards four to a block running the length of the hall, each its own tone, their joints dark."""
    rnd = random.Random(seed)
    im = Image.new('RGBA', (16, 16))
    tones = [rnd.randint(-6, 6) for _ in range(4)]
    for y in range(16):
        for x in range(16):
            im.putpixel((x, y), shade(base, tones[x // 4] + rnd.randint(-3, 3) - (2 if y % 5 == 0 else 0)))
    for y in range(16):
        for x in (0, 4, 8, 12):
            im.putpixel((x, y), shade(base, -seams))
    for x in (2, 10):
        im.putpixel((x, rnd.randrange(16)), shade(base, -seams))
    return im


def runner(seed):
    """A runner down the middle of the boards: a border either side, a small repeating figure down the field."""
    rnd = random.Random(seed)
    im = Image.new('RGBA', (16, 16))
    for y in range(16):
        for x in range(16):
            k = rnd.randint(-3, 3)
            if x in (0, 15):
                c = shade(RUNNER, -16 + k)
            elif x in (1, 2, 13, 14):
                c = shade(RUNNER_BORDER, k - (8 if (x + y) % 4 == 0 else 0))
            else:
                c = shade(RUNNER, k)
                if (abs(x - 7.5) + abs((y % 8) - 3.5)) in (3.0, 4.0):
                    c = shade(RUNNER_BORDER, k - 10)
            im.putpixel((x, y), c)
    return im


def plaster_ceiling(seed, base):
    rnd = random.Random(seed)
    im = Image.new('RGBA', (16, 16))
    for y in range(16):
        for x in range(16):
            im.putpixel((x, y), shade(base, rnd.randint(-3, 3)))
    return im


def trim(seed):
    """A door frame painted over many times: the grain gone, the edges rounded, the paint worn at the corners."""
    rnd = random.Random(seed)
    im = Image.new('RGBA', (16, 16))
    for y in range(16):
        for x in range(16):
            k = rnd.randint(-2, 2) + (6 if x in (3, 12) else -4 if x in (4, 11) else 0)
            im.putpixel((x, y), shade(PAINT, k))
    for _ in range(3):
        im.putpixel((rnd.choice((0, 15)), rnd.randrange(16)), shade(DOOR, -10))
    return im


def bedroom_door(seed, upper):
    """A four-panel bedroom door, painted the wood's own colour long ago: two tall panels in the upper half and two short ones
    below, the rails and stiles between, and a brass knob on the lower half. The same picture is painted on the wall and hung on
    the two doors that open."""
    rnd = random.Random(seed)
    im = Image.new('RGBA', (16, 16))
    for y in range(16):
        for x in range(16):
            im.putpixel((x, y), shade(DOOR, rnd.randint(-3, 3)))
    panels = ((3, 2, 7, 15), (9, 2, 13, 15)) if upper else ((3, 3, 7, 11), (9, 3, 13, 11))
    for x0, y0, x1, y1 in panels:
        for x in range(x0, x1 + 1):
            for y in range(y0, y1 + 1):
                im.putpixel((x, y), shade(DOOR, rnd.randint(-2, 2) - 9))
        for x in range(x0, x1 + 1):
            im.putpixel((x, y0), shade(DOOR_SHADE, -4)); im.putpixel((x, y1), shade(DOOR, 16))
        for y in range(y0, y1 + 1):
            im.putpixel((x0, y), shade(DOOR_SHADE, -4)); im.putpixel((x1, y), shade(DOOR, 16))
    for y in range(16):
        im.putpixel((0, y), shade(DOOR_SHADE, -6)); im.putpixel((15, y), shade(DOOR_SHADE, -6))
    if not upper:
        im.putpixel((13, 13), shade(BRASS, 10)); im.putpixel((14, 13), shade(BRASS, -10)); im.putpixel((13, 14), shade(BRASS, -20))
        for x in range(16):
            im.putpixel((x, 15), shade(DOOR_SHADE, -10))
    else:
        for x in range(16):
            im.putpixel((x, 0), shade(DOOR_SHADE, -10))
    return im


def stucco_panel(seed):
    """Plaster in panels: a raised moulding round a sunk field, washed a warm stone colour."""
    rnd = random.Random(seed)
    im = Image.new('RGBA', (16, 16))
    for y in range(16):
        for x in range(16):
            im.putpixel((x, y), shade(STUCCO, rnd.randint(-2, 2)))
    for i in range(2, 14):
        im.putpixel((i, 2), shade(STUCCO, 10)); im.putpixel((i, 13), shade(STUCCO, -10))
        im.putpixel((2, i), shade(STUCCO, 10)); im.putpixel((13, i), shade(STUCCO, -10))
    return im


def chequer(seed):
    """Marble squares, two to a block each way, pale and dark, faint veins in the pale ones."""
    rnd = random.Random(seed)
    im = Image.new('RGBA', (16, 16))
    for y in range(16):
        for x in range(16):
            pale = (x // 8 + y // 8) % 2 == 0
            im.putpixel((x, y), shade(MARBLE_A if pale else MARBLE_B, rnd.randint(-3, 3)))
    x = 2
    for y in range(8):
        im.putpixel((x, y), shade(MARBLE_A, -16)); x = max(0, min(7, x + rnd.choice((-1, 0, 1))))
    return im


def coffers(seed):
    rnd = random.Random(seed)
    im = Image.new('RGBA', (16, 16))
    for y in range(16):
        for x in range(16):
            edge = x in (0, 15) or y in (0, 15)
            sunk = 3 <= x <= 12 and 3 <= y <= 12
            k = rnd.randint(-2, 2) + (8 if edge else -10 if sunk else 0)
            im.putpixel((x, y), shade(STUCCO, k))
    return im


def stone_step(seed):
    """A stone step: worn smooth where feet land, its nosing a pale line along the front."""
    rnd = random.Random(seed)
    im = Image.new('RGBA', (16, 16))
    for y in range(16):
        for x in range(16):
            im.putpixel((x, y), shade(STEP, rnd.randint(-4, 4) + (4 if 4 <= x <= 11 else 0)))
    for x in range(16):
        im.putpixel((x, 0), shade(STEP, 14)); im.putpixel((x, 1), shade(STEP, 6))
    return im


def skylight(seed):
    """Frosted panes in leading, the light coming through from above."""
    rnd = random.Random(seed)
    im = Image.new('RGBA', (16, 16))
    for y in range(16):
        for x in range(16):
            lead = x % 8 == 0 or y % 8 == 0
            im.putpixel((x, y), shade(LEAD, rnd.randint(-2, 2)) if lead else shade(SKY, rnd.randint(-6, 4)))
    return im


def balustrade_tex(seed):
    """The balustrade: the rail's polished wood in the top rows, the painted balusters below."""
    rnd = random.Random(seed)
    im = Image.new('RGBA', (16, 16))
    for y in range(16):
        for x in range(16):
            im.putpixel((x, y), shade(RAIL, rnd.randint(-3, 3) + (8 if y == 1 else 0)) if y < 4 else shade(BALUSTER, rnd.randint(-3, 3) - (10 if x % 4 == 3 else 0)))
    return im


def ashlar(seed, base, wet):
    """Dressed stone in long courses, the joints dark; wet, it streaks darker down from the joints."""
    rnd = random.Random(seed)
    im = Image.new('RGBA', (16, 16))
    for y in range(16):
        for x in range(16):
            course = y // 8
            joint = y % 8 == 7 or (x + course * 8) % 16 == 15
            im.putpixel((x, y), shade(base, rnd.randint(-4, 4) - (14 if joint else 0)))
    if wet:
        for _ in range(4):
            x = rnd.randrange(16)
            for y in range(rnd.randrange(16), 16):
                im.putpixel((x, y), mix(im.getpixel((x, y)), WET, .5))
    return im


def silt(seed):
    rnd = random.Random(seed)
    im = Image.new('RGBA', (16, 16))
    for y in range(16):
        for x in range(16):
            im.putpixel((x, y), shade(SILT, rnd.randint(-6, 6)))
    return im


def drum(seed):
    """A column built of drums, the joint between two across the middle, worn upright flutes faint on the stone."""
    rnd = random.Random(seed)
    im = Image.new('RGBA', (16, 16))
    for y in range(16):
        for x in range(16):
            im.putpixel((x, y), shade(DRUM, rnd.randint(-3, 3) + (-6 if x % 4 == 0 else 2 if x % 4 == 2 else 0)))
    for x in range(16):
        im.putpixel((x, 7), shade(DRUM, -18))
    return im


def vault_brick(seed):
    """The vault's brick, small and dark, the mortar white with lime where the water has come through."""
    rnd = random.Random(seed)
    im = Image.new('RGBA', (16, 16))
    for y in range(16):
        for x in range(16):
            mortar = y % 4 == 3 or (x + (4 if (y // 4) % 2 else 0)) % 8 == 7
            im.putpixel((x, y), shade('#9a978c', rnd.randint(-6, 6)) if mortar else shade(VAULT_BRICK, rnd.randint(-6, 6)))
    return im


def panelling(seed):
    """Dark panelling: tall panels between stiles, the varnish gone almost black."""
    rnd = random.Random(seed)
    im = Image.new('RGBA', (16, 16))
    for y in range(16):
        for x in range(16):
            stile = x in (0, 1, 8, 9)
            im.putpixel((x, y), shade(PANEL, rnd.randint(-2, 2) + (6 if stile else 0) + (3 if x in (2, 10) else 0)))
    return im


def beamed(seed):
    rnd = random.Random(seed)
    im = Image.new('RGBA', (16, 16))
    for y in range(16):
        for x in range(16):
            beam = y < 4
            im.putpixel((x, y), shade(BEAM, rnd.randint(-3, 3)) if beam else shade('#a59c86', rnd.randint(-3, 3)))
    return im


def books(seed):
    """A block of shelf: two shelves of books, every spine its own height, width and cloth, the shelf boards between."""
    rnd = random.Random(seed)
    im = Image.new('RGBA', (16, 16))
    for y in range(16):
        for x in range(16):
            im.putpixel((x, y), shade(PANEL, rnd.randint(-3, 3) - 8))
    for top, bottom in ((1, 7), (9, 15)):
        x = 1
        while x < 15:
            w = rnd.choice((1, 2, 2, 3)); w = min(w, 15 - x)
            h = rnd.randint(4, 6); c = rnd.choice(SPINES)
            for dx in range(w):
                for y in range(bottom - h, bottom):
                    im.putpixel((x + dx, y), shade(c, rnd.randint(-3, 3) + (6 if dx == 0 else 0)))
                if h >= 5 and w >= 2 and (x * 7 + top) % 3 == 0:
                    im.putpixel((x + dx, bottom - h + 1), shade(BRASS, -30))
            x += w
        for x in range(16):
            im.putpixel((x, bottom), shade(OLD_BOARD, 6))
    for y in range(16):
        im.putpixel((0, y), shade(OLD_BOARD, -4)); im.putpixel((15, y), shade(OLD_BOARD, -4))
    return im


def textures():
    return {
        'laundry_wall': limewash(3900, False), 'laundry_wall_low': limewash(3900, True), 'laundry_floor': quarry(3901),
        'laundry_ceiling': ceiling_boards(3902),
        'linen_upper': linen(3903, True), 'linen_lower': linen(3904, False),
        'ticking_upper': ticking(3905, True), 'ticking_lower': ticking(3906, False),
        'towel': towel(3907), 'line': line(3908), 'basket': wicker(3909), 'basket_top': folded(3910),
        'bedroom_wall': sprig_paper(3911), 'bedroom_dado': dado(3912), 'bedroom_floor': floorboards(3913, FLOOR, 18),
        'bedroom_runner': runner(3914), 'bedroom_ceiling': plaster_ceiling(3915, '#c4bfae'), 'bedroom_trim': trim(3916),
        'door_lower': bedroom_door(3917, False), 'door_upper': bedroom_door(3917, True),
        'atrium_wall': stucco_panel(3918), 'atrium_floor': chequer(3919), 'atrium_deck': floorboards(3920, '#8a6a4a', 16),
        'atrium_ceiling': coffers(3921), 'atrium_step': stone_step(3922), 'atrium_skylight': skylight(3923), 'balustrade': balustrade_tex(3924),
        'cistern_wall': ashlar(3925, ASHLAR, True), 'cistern_floor': ashlar(3926, FLAG, False), 'cistern_under': silt(3927),
        'cistern_pillar': drum(3928), 'cistern_vault': vault_brick(3929),
        'stacks_wall': panelling(3930), 'stacks_floor': floorboards(3931, OLD_BOARD, 14), 'stacks_ceiling': beamed(3932),
        'shelf': books(3933), 'shelf_b': books(3934),
    }


FACES = ('north', 'south', 'east', 'west', 'up', 'down')
# The swing (HallSheetBlock.SWAY): 1 to 3 begin toward +z, 4 to 6 toward -z. A turn about the line (x axis) by a negative angle
# carries the hanging cloth toward +z; the settling stages only shift it.
TURN = {1: -22.5, 4: 22.5}
SHIFT = {0: 0, 2: -1.5, 3: 0.75, 5: 1.5, 6: -0.75}


def cloth(tex, y0, y1, v0, sway, upper):
    """One block's piece of the cloth, half a pixel thick across the middle of the block, from y0 to y1, its texture cropped
    from row v0. The upper block turns it about the line at its top; the lower block about the same line, a block above."""
    shift = SHIFT.get(sway, 0)
    z0, z1 = 7.75 + shift, 8.25 + shift
    face = {'texture': tex, 'uv': [0, v0, 16, v0 + (y1 - y0)]}
    edge = {'texture': tex, 'uv': [0, v0, 0.5, v0 + (y1 - y0)]}
    flat = {'texture': tex, 'uv': [0, v0, 16, v0 + 0.5]}
    el = {'from': [0, y0, z0], 'to': [16, y1, z1], 'faces': {'north': face, 'south': face, 'east': edge, 'west': edge, 'up': flat, 'down': flat}}
    if sway in TURN:
        el['rotation'] = {'origin': [8, 15 if upper else 31, 8], 'axis': 'x', 'angle': TURN[sway]}
    return el


def rope_and_pegs(pegs):
    rope = {'texture': '#line', 'uv': [0, 0, 16, 1]}
    end = {'texture': '#line', 'uv': [0, 0, 1, 1]}
    els = [{'from': [0, 15, 7.5], 'to': [16, 16, 8.5], 'faces': {'north': rope, 'south': rope, 'up': rope, 'down': rope, 'east': end, 'west': end}}]
    if pegs:
        wood = {'texture': '#line', 'uv': [4, 3, 5, 7]}
        for x in (3, 11):
            els.append({'from': [x, 13, 7.25], 'to': [x + 1, 16.5, 8.75], 'faces': {f: wood for f in FACES}})
    return els


def sheet_model(kind, upper, sway):
    """The washing on one block of the line (HallSheetBlock), modelled with the line running east to west."""
    textures = {'line': P + 'line'}
    els = []
    if upper:
        els += rope_and_pegs(kind != 'bare')
    if kind in ('linen', 'ticking'):
        tex = P + f'{kind}_{"upper" if upper else "lower"}'
        textures['cloth'] = tex
        els.append(cloth('#cloth', 0, 15, 1, sway, True) if upper else cloth('#cloth', 6, 16, 0, sway, False))
    elif kind == 'towel' and upper:
        textures['cloth'] = P + 'towel'
        els.append(cloth('#cloth', 4, 15, 1, sway, True))
    textures['particle'] = textures.get('cloth', P + 'line')
    return {'ambientocclusion': False, 'textures': textures, 'elements': els}


def basket_model():
    """A wicker basket of folded washing on the floor, its rim a pixel proud."""
    wick = {f: {'texture': '#wicker'} for f in ('north', 'south', 'east', 'west', 'down')}
    return {'textures': {'wicker': P + 'basket', 'top': P + 'basket_top', 'particle': P + 'basket'}, 'elements': [
        {'from': [2, 0, 2], 'to': [14, 8, 14], 'faces': wick | {'up': {'texture': '#wicker', 'uv': [2, 2, 14, 14]}}},
        {'from': [3, 8, 3], 'to': [13, 9, 13], 'faces': {f: {'texture': '#top'} for f in FACES}},
        {'from': [1.5, 7, 1.5], 'to': [14.5, 8.5, 14.5], 'faces': {f: {'texture': '#wicker', 'uv': [0, 0, 16, 2]} for f in ('north', 'south', 'east', 'west')}},
    ]}


def stool_model():
    """A plain wooden stool, its seat a hand across and its four legs splayed a little, the wood of the pegs."""
    wood = {f: {'texture': '#wood', 'uv': [0, 4, 16, 16]} for f in FACES}
    leg = {f: {'texture': '#wood', 'uv': [4, 4, 6, 14]} for f in FACES}
    els = [{'from': [3, 9, 3], 'to': [13, 11, 13], 'faces': wood}]
    for x, z in ((3.5, 3.5), (10.5, 3.5), (3.5, 10.5), (10.5, 10.5)):
        els.append({'from': [x, 0, z], 'to': [x + 2, 9, z + 2], 'faces': leg})
    els.append({'from': [4.5, 4, 7.5], 'to': [11.5, 5, 8.5], 'faces': leg})
    return {'textures': {'wood': P + 'line', 'particle': P + 'line'}, 'elements': els}


def box_el(a, b, tex, uv=None):
    face = {'texture': tex} | ({'uv': uv} if uv else {})
    return {'from': a, 'to': b, 'faces': {f: dict(face) for f in FACES}}


def step_model():
    t = P + 'atrium_step'
    side = {'texture': '#s', 'uv': [0, 8, 16, 16]}
    return {'textures': {'s': t, 'particle': t}, 'elements': [{'from': [0, 0, 0], 'to': [16, 8, 16], 'faces': {
        'up': {'texture': '#s'}, 'down': {'texture': '#s'}, 'north': side, 'south': side, 'east': side, 'west': side}}]}


def balustrade_model(corner):
    """A turned balustrade along the north side of the block (and for a corner the east side too, with a newel where they
    meet): a bottom rail, four balusters and a handrail a block high."""
    els = []
    def run(axis_z):
        out = []
        if axis_z:
            out.append(box_el([0, 0, 0.25], [16, 1.5, 1.75], '#t', [0, 4, 16, 6]))
            out.append(box_el([0, 14.5, 0], [16, 16, 2], '#t', [0, 0, 16, 2]))
            for x in (1.5, 5.5, 9.5, 13):
                out.append(box_el([x, 1.5, 0.5], [x + 1.5, 14.5, 1.5], '#t', [x, 4, x + 1.5, 16]))
        else:
            out.append(box_el([14.25, 0, 0], [15.75, 1.5, 16], '#t', [0, 4, 16, 6]))
            out.append(box_el([14, 14.5, 0], [16, 16, 16], '#t', [0, 0, 16, 2]))
            for z in (1.5, 5.5, 9.5, 13):
                out.append(box_el([14.5, 1.5, z], [15.5, 14.5, z + 1.5], '#t', [z, 4, z + 1.5, 16]))
        return out
    els += run(True)
    if corner:
        els += run(False)
        els.append(box_el([13, 0, 0], [16, 16.5, 3], '#t', [0, 0, 3, 16]))
    t = P + 'balustrade'
    return {'textures': {'t': t, 'particle': t}, 'elements': els}


def table_model():
    """A reading table: a plain top on four square legs, a book left open on it."""
    wood = P + 'stacks_floor'
    els = [box_el([0.5, 11, 0.5], [15.5, 13, 15.5], '#w')]
    for x, z in ((1, 1), (13, 1), (1, 13), (13, 13)):
        els.append(box_el([x, 0, z], [x + 2, 11, z + 2], '#w', [0, 0, 2, 11]))
    els.append(box_el([5, 13, 6], [11, 13.5, 10], '#b', [2, 2, 8, 6]))
    return {'textures': {'w': wood, 'b': P + 'basket_top', 'particle': wood}, 'elements': els}


def face_model(front, side):
    """A whole block with a picture on its south face, the wall's own surface on the others."""
    return {'parent': 'minecraft:block/cube', 'textures': {'north': side, 'east': side, 'west': side, 'up': side, 'down': side,
            'south': front, 'particle': front}}


KINDS = ('linen', 'ticking', 'towel', 'bare')


def files():
    out = {}
    for name, _ in MATERIALS:
        out[f'models/block/hall_{name}.json'] = {'parent': 'minecraft:block/cube_all', 'textures': {'all': P + name}}
        out[f'blockstates/hall_{name}.json'] = {'variants': {'': {'model': NS + f'block/hall_{name}'}}}
    variants = {}
    for kind in KINDS:
        for upper in (False, True):
            half = 'upper' if upper else 'lower'
            for sway in range(7):
                # The line and a bare stretch never swing; their models are the same at every stage.
                still = kind == 'bare' or (kind == 'towel' and not upper)
                name = f'hall_sheet_{kind}_{half}' + ('' if still else f'_{sway}')
                if not still or sway == 0:
                    out[f'models/block/{name}.json'] = sheet_model(kind, upper, 0 if still else sway)
                for axis, turn in (('x', 0), ('z', 90)):
                    variant = {'model': NS + 'block/' + name}
                    if turn:
                        variant['y'] = turn
                    variants[f'axis={axis},kind={kind},sway={sway},upper={str(upper).lower()}'] = variant
    out['blockstates/hall_sheet.json'] = {'variants': variants}
    out['models/block/hall_basket.json'] = basket_model()
    out['models/block/hall_stool.json'] = stool_model()
    out['models/block/hall_step.json'] = step_model()
    out['models/block/hall_balustrade.json'] = balustrade_model(False)
    out['models/block/hall_balustrade_corner.json'] = balustrade_model(True)
    out['models/block/hall_table.json'] = table_model()
    turn = {'south': 0, 'west': 90, 'north': 180, 'east': 270}
    # The balustrade is modelled on the north side of its block, so it turns from north.
    rail = {'north': 0, 'east': 90, 'south': 180, 'west': 270}
    fitting = {}
    for k in ('basket', 'stool', 'step', 'balustrade', 'balustrade_corner', 'table'):
        for f in turn:
            y = rail[f] if k.startswith('balustrade') else turn[f]
            fitting[f'facing={f},kind={k}'] = {'model': NS + f'block/hall_{k}'} | ({'y': y} if y else {})
    out['blockstates/hall_fitting.json'] = {'variants': fitting}
    # The painted doors and the shelves (HallFaceBlock), facing as the painting faces.
    out['models/block/hall_painted_door.json'] = face_model(P + 'door_lower', P + 'bedroom_wall')
    out['models/block/hall_painted_door_top.json'] = face_model(P + 'door_upper', P + 'bedroom_wall')
    for k in ('shelf', 'shelf_b'):
        t = P + k
        out[f'models/block/hall_{k}.json'] = {'parent': 'minecraft:block/cube', 'textures': {'north': t, 'south': t, 'east': t, 'west': t,
                                              'up': P + 'stacks_floor', 'down': P + 'stacks_floor', 'particle': t}}
    face = {}
    for k in ('painted_door', 'painted_door_top', 'shelf', 'shelf_b'):
        for f in turn:
            face[f'facing={f},kind={k}'] = {'model': NS + f'block/hall_{k}'} | ({'y': turn[f]} if turn[f] else {})
    out['blockstates/hall_face.json'] = {'variants': face}
    # The two doors that open (HallDoorBlock), on the game's own door shapes, with the painted doors' picture.
    door = {}
    for half, part in (('lower', 'bottom'), ('upper', 'top')):
        for hinge in ('left', 'right'):
            for opened in (False, True):
                name = f'hall_door_{part}_{hinge}' + ('_open' if opened else '')
                out[f'models/block/{name}.json'] = {'parent': f'minecraft:block/door_{part}_{hinge}' + ('_open' if opened else ''),
                                                    'textures': {'bottom': P + 'door_lower', 'top': P + 'door_upper'}}
                for f, base in (('east', 0), ('south', 90), ('west', 180), ('north', 270)):
                    y = (base + ((90 if hinge == 'left' else 270) if opened else 0)) % 360
                    door[f'facing={f},half={half},hinge={hinge},open={str(opened).lower()}'] = {'model': NS + 'block/' + name} | ({'y': y} if y else {})
    out['blockstates/hall_door.json'] = {'variants': door}
    out['models/block/hall_cistern_drip.json'] = {'parent': 'minecraft:block/cube_all', 'textures': {'all': P + 'cistern_vault'}}
    out['blockstates/hall_cistern_drip.json'] = {'variants': {'': {'model': NS + 'block/hall_cistern_drip'}}}
    return out


def names():
    out = {f'block.the_oldest_house.hall_{name}': title for name, title in MATERIALS}
    out['block.the_oldest_house.hall_sheet'] = 'Washing'
    out['block.the_oldest_house.hall_fitting'] = 'Hall Fitting'
    out['block.the_oldest_house.hall_face'] = 'Painted Door'
    out['block.the_oldest_house.hall_door'] = 'Bedroom Door'
    out['block.the_oldest_house.hall_cistern_drip'] = 'Dripping Vault'
    return out


def main():
    check = argparse.ArgumentParser()
    check.add_argument('--check', action='store_true')
    check = check.parse_args().check
    images, data = textures(), files()
    for key, im in images.items():
        path = A / 'textures/block/ordinary_hall' / f'{key}.png'
        if check:
            with Image.open(path) as actual:
                assert actual.convert('RGBA').tobytes() == im.tobytes(), key
        else:
            path.parent.mkdir(parents=True, exist_ok=True); im.save(path, optimize=True)
    for rel, obj in data.items():
        path = A / rel
        if check:
            assert json.loads(path.read_text()) == obj, rel
        else:
            path.parent.mkdir(parents=True, exist_ok=True); path.write_text(json.dumps(obj, indent=2) + '\n')
    lang_path = A / 'lang/en_us.json'; lang = json.loads(lang_path.read_text())
    for key, title in names().items():
        if check:
            assert lang.get(key) == title, key
        else:
            lang[key] = title
    if not check:
        lang_path.write_text(json.dumps(lang, indent=2, ensure_ascii=False) + '\n')
    print(('Verified' if check else 'Drew') + f' the ordinary halls: {len(images)} textures, {len(data)} models and states.')


if __name__ == '__main__':
    main()
