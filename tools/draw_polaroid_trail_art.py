"""The Polaroid Trail (0.4.204): the back yard and the creek culvert, the crawlspace under the house next door, the
neighbourhood cinema and the pine wood with its two trenches; the things a reader carries; and the developed photographs.

Draws, in native Minecraft pixels from fixed seeds: eight materials (corrugated culvert steel, crawlspace clay, damp red
brick, pink insulation, cinema carpet, cinema wall, velvet curtain, clapboard siding); every fitting of the polaroid_fitting
block, its models facing north and turned by its facing (the balloon tag on the porch rail, a dropped photograph face down or
face up, a yellow balloon on its string, dark or lit, the half-built pallet raft, the culvert's side grate shut or swung
open, the lattice vent shut or pried, the cat bowl, the heap of stolen clothes, the floor hatch shut or open, a cast-iron
drain, a hanging batt of insulation, a cinema seat empty, folded or with someone sitting in it, the amber aisle light, the
projector off or running, the screen dark or lit, the concession counter, a poster, the photo album, the lunchbox, the fire
exit sign); the photograph and the walkie-talkie as items. Writes the models and block states with them.
--check verifies everything already written.

Palette, from the owner's specification: xenon flash white #F4F8FF, balloon yellow #FFE600, red-eye amber #FFBF00, Josh's
sepia #D97706, insulation pink #E8A0B0, velvet #6E1423, culvert steel #8A8F93.
"""
from pathlib import Path
import argparse
import json
import math
import random
from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
A = ROOT / 'src/main/resources/assets/the_oldest_house'
NS = 'the_oldest_house:'
P = NS + 'block/polaroid/'
YELLOW, AMBER, SEPIA, PINK, VELVET, STEEL = '#ffe600', '#ffbf00', '#d97706', '#e8a0b0', '#6e1423', '#8a8f93'


def rgb(c):
    c = c.lstrip('#')
    return tuple(int(c[i:i + 2], 16) for i in (0, 2, 4))


def shade(c, k, a=255):
    r, g, b = rgb(c) if isinstance(c, str) else c[:3]
    return (max(0, min(255, r + k)), max(0, min(255, g + k)), max(0, min(255, b + k)), a)


def new(size=(16, 16), base=None, seed=0, spread=5):
    im = Image.new('RGBA', size, (0, 0, 0, 0))
    if base:
        rnd = random.Random(seed); px = im.load()
        for y in range(size[1]):
            for x in range(size[0]):
                px[x, y] = shade(base, rnd.randint(-spread, spread))
    return im


def fill(im, x0, y0, x1, y1, colour, seed=0, spread=0, a=255):
    rnd = random.Random(seed); px = im.load()
    for y in range(max(0, y0), min(im.size[1], y1)):
        for x in range(max(0, x0), min(im.size[0], x1)):
            px[x, y] = shade(colour, rnd.randint(-spread, spread) if spread else 0, a)


def line(im, x0, y0, x1, y1, colour, a=255):
    px = im.load(); n = max(abs(x1 - x0), abs(y1 - y0), 1)
    for i in range(n + 1):
        x, y = round(x0 + (x1 - x0) * i / n), round(y0 + (y1 - y0) * i / n)
        if 0 <= x < im.size[0] and 0 <= y < im.size[1]:
            px[x, y] = shade(colour, 0, a)


def frame_rect(im, x0, y0, x1, y1, colour):
    line(im, x0, y0, x1 - 1, y0, colour); line(im, x0, y1 - 1, x1 - 1, y1 - 1, colour)
    line(im, x0, y0, x0, y1 - 1, colour); line(im, x1 - 1, y0, x1 - 1, y1 - 1, colour)


# ---------------------------------------------------------------------------------------------------- materials
def culvert():
    """Corrugated galvanised steel: a ridge every two pixels, rust bleeding down from the seams."""
    im = new(base=STEEL, seed=11, spread=3); px = im.load(); rnd = random.Random(12)
    for x in range(16):
        k = (12, 2, -12, -2)[x % 4]
        for y in range(16):
            px[x, y] = shade(px[x, y], k)
    for x in (3, 11):
        for y in range(rnd.randint(6, 15)):
            px[x, y] = shade('#7a4a2a', rnd.randint(-8, 8))
    return im


def clay():
    im = new(base='#8a5a3e', seed=21, spread=6); px = im.load(); rnd = random.Random(22)
    for _ in range(12):
        px[rnd.randint(0, 15), rnd.randint(0, 15)] = shade('#6a4230', rnd.randint(-6, 6))
    return im


def brick():
    """Damp red brick: dark mortar courses, a white bloom of salt low down."""
    im = new(base='#8a3a2a', seed=31, spread=5); px = im.load(); rnd = random.Random(32)
    for r in range(4):
        y = r * 4 + 3
        for x in range(16):
            px[x, y] = shade('#4a3a32', rnd.randint(-3, 3))
        off = 0 if r % 2 else 4
        for yy in range(r * 4, y):
            px[(off) % 16, yy] = shade('#4a3a32', 0); px[(off + 8) % 16, yy] = shade('#4a3a32', 0)
    for x in range(16):
        if rnd.random() < .5:
            px[x, 14] = shade('#d8d0c4', -10)
    return im


def insulation():
    im = new(base=PINK, seed=41, spread=8); px = im.load()
    for y in range(16):
        for x in range(16):
            if (x * 7 + y * 3) % 5 == 0:
                px[x, y] = shade(px[x, y], 14)
    return im


def cinema_carpet():
    """The cinema's carpet: deep red with a gold diamond pattern, sticky and dark down the aisles."""
    im = new(base='#5a1820', seed=51, spread=3); px = im.load()
    for y in range(16):
        for x in range(16):
            if (abs(x - 8) + abs(y - 8)) % 8 == 0:
                px[x, y] = shade('#a08030', -10)
    return im


def cinema_wall():
    im = new(base='#3a2a30', seed=61, spread=3); px = im.load()
    for y in range(0, 16, 8):
        for x in range(16):
            px[x, y] = shade('#2a1c22', 0)
    return im


def velvet():
    im = new(base=VELVET, seed=71, spread=4); px = im.load()
    for x in range(16):
        k = (8, 0, -10, 0)[x % 4]
        for y in range(16):
            px[x, y] = shade(px[x, y], k)
    return im


def siding():
    """Clapboard siding, the white gone grey-green with damp, a course every four pixels."""
    im = new(base='#a8b0a4', seed=81, spread=3); px = im.load()
    for r in range(4):
        for x in range(16):
            px[x, r * 4 + 3] = shade('#6a7268', 0); px[x, r * 4] = shade('#c0c8bc', 0)
    return im


def materials():
    return {'culvert': culvert(), 'clay': clay(), 'pier_brick': brick(), 'insulation': insulation(), 'cinema_carpet': cinema_carpet(),
            'cinema_wall': cinema_wall(), 'velvet': velvet(), 'siding': siding()}


# ---------------------------------------------------------------------------------------------------- fittings
def tag():
    """A balloon tag: a luggage label on a loop of string, a child's capitals on it."""
    im = new(); fill(im, 3, 4, 13, 12, '#f0e8c8', 101, 3); px = im.load()
    frame_rect(im, 3, 4, 13, 12, '#c8b888')
    px[5, 5] = (60, 50, 40, 255); line(im, 8, 0, 8, 4, '#d8d0b0')
    for y in (7, 9):
        for x in range(5, 12):
            if (x + y) % 3:
                px[x, y] = shade('#2a3a8a', 0)
    return im


def photo(face_up):
    """A dropped photograph: the white border and the dark square of the picture, or the grey card of its back."""
    if not face_up:
        im = new(base='#c8c4bc', seed=111, spread=2); frame_rect(im, 0, 0, 16, 16, '#a8a49c'); return im
    im = new(base='#f4f4ee', seed=112, spread=1); fill(im, 2, 2, 14, 12, '#2a2a2a', 113, 6)
    px = im.load(); rnd = random.Random(114)
    for y in range(2, 12):
        for x in range(2, 14):
            if rnd.random() < .35:
                px[x, y] = shade('#6a6a6a', rnd.randint(-20, 20))
    fill(im, 7, 6, 9, 11, '#0a0a0a')
    return im


def balloon(lit):
    """A yellow latex balloon, its knot and a highlight; lit, it glows from inside."""
    im = new(); px = im.load()
    for y in range(16):
        for x in range(16):
            d = ((x - 8) / 6) ** 2 + ((y - 7) / 7) ** 2
            if d <= 1:
                k = int(-24 * d) + (30 if lit else 0)
                px[x, y] = shade(YELLOW, k)
    px[6, 3] = (255, 255, 240, 255); px[5, 4] = (255, 255, 230, 255)
    fill(im, 7, 14, 9, 16, '#d8b800')
    return im


def string():
    im = new(); line(im, 8, 0, 8, 15, '#e8e8e0', 220); return im


def pallet():
    im = new(base='#a88a5a', seed=121, spread=6); px = im.load()
    for x in (4, 5, 10, 11):
        for y in range(16):
            px[x, y] = (0, 0, 0, 0)
    for y in range(16):
        px[0, y] = shade('#6a5432', 0); px[15, y] = shade('#6a5432', 0)
    return im


def grate():
    """The culvert's side grate: flat iron bars in a frame, a latch on the right."""
    im = new(); px = im.load()
    frame_rect(im, 0, 0, 16, 16, '#3a3a3a'); frame_rect(im, 1, 1, 15, 15, '#4a4a4a')
    for x in range(3, 14, 3):
        line(im, x, 1, x, 14, '#5a5a5a')
    fill(im, 12, 7, 15, 9, '#8a7a50')
    return im


def lattice():
    """Painted lattice: thin slats crossing on the diagonal, white paint flaking."""
    im = new(); px = im.load()
    for y in range(16):
        for x in range(16):
            if (x + y) % 6 in (0, 1) or (x - y) % 6 in (0, 1):
                px[x, y] = shade('#e0ddd0', -12 if (x * y) % 7 == 0 else 0)
    return im


def bowl(part):
    if part == 'top':
        im = new(); px = im.load()
        for y in range(16):
            for x in range(16):
                d = (x - 8) ** 2 + (y - 8) ** 2
                if d < 30:
                    px[x, y] = shade('#e8e0d0', -6 if d > 20 else 0)
                if d < 16:
                    px[x, y] = shade('#5a6a5a', 0)
        return im
    im = new(base='#d8ccb8', seed=131, spread=3); line(im, 0, 3, 15, 3, '#4a6a9a'); return im


def clothes():
    """A heap of stolen bedsheets and shirts, flattened into a bed, one tunic folded on the top."""
    im = new(base='#8a8a9a', seed=141, spread=8); px = im.load(); rnd = random.Random(142)
    for _ in range(5):
        c = rnd.choice(('#a84a3a', '#3a5a8a', '#d8d0b0', '#6a8a4a', '#c8a05a'))
        x0, y0 = rnd.randint(0, 10), rnd.randint(0, 10)
        fill(im, x0, y0, x0 + rnd.randint(4, 7), y0 + rnd.randint(3, 6), c, rnd.randint(0, 99), 6)
    fill(im, 5, 5, 11, 11, '#b07a5a', 143, 4)
    return im


def hatch(part):
    if part == 'top':
        im = new(base='#6a5a42', seed=151, spread=4); px = im.load()
        for x in range(0, 16, 5):
            for y in range(16):
                px[x, y] = shade('#3a3026', 0)
        fill(im, 6, 7, 10, 9, '#4a4a4a')
        return im
    im = new(base='#1a1410', seed=152, spread=2); return im


def drain():
    im = new(base='#2a2a2c', seed=161, spread=3); px = im.load()
    for y in range(0, 16, 5):
        for x in range(16):
            px[x, y] = shade('#4a3a2a', 0)
    return im


def seat(part):
    """A cinema seat: worn red velvet, a brass number plate, the arm rests dark."""
    if part == 'velvet':
        return velvet()
    if part == 'arm':
        return new(base='#2a1a18', seed=171, spread=2)
    if part == 'sitter':
        im = new(base='#0c0a0a', seed=172, spread=2); return im
    return new(base='#a08030', seed=173, spread=4)


def aisle_light(lit):
    im = new(base='#2a2a2a', seed=181, spread=2); fill(im, 4, 6, 12, 10, AMBER if lit else '#4a3a20'); return im


def projector(part):
    if part == 'lens':
        im = new(base='#1a1a1c', seed=191, spread=2); px = im.load()
        for y in range(16):
            for x in range(16):
                if (x - 8) ** 2 + (y - 8) ** 2 < 18:
                    px[x, y] = shade('#3a4a5a', 0)
        return im
    if part == 'lit':
        im = projector('lens'); px = im.load()
        for y in range(5, 11):
            for x in range(5, 11):
                px[x, y] = (255, 250, 220, 255)
        return im
    return new(base='#3a3e44', seed=192, spread=3)


def screen(lit):
    """The screen: a pale field, torn in one corner; lit, the projector's square of light on it."""
    im = new(base='#d8d8d0' if lit else '#5a5a58', seed=201, spread=2); px = im.load()
    line(im, 11, 0, 15, 6, '#1a1a1a'); line(im, 12, 0, 15, 4, '#1a1a1a')
    return im


def counter():
    im = new(base='#8a2a2a', seed=211, spread=3); fill(im, 0, 0, 16, 3, '#c8b070'); fill(im, 2, 5, 14, 12, '#d8c888', 212, 6); return im


def poster():
    im = new(base='#1a2a3a', seed=221, spread=3); frame_rect(im, 0, 0, 16, 16, '#c8b070'); px = im.load()
    for y in range(3, 11):
        for x in range(5, 11):
            if ((x - 8) / 3) ** 2 + ((y - 6) / 4) ** 2 < 1:
                px[x, y] = shade(YELLOW, -20)
    line(im, 8, 10, 8, 14, '#e8e8e0')
    return im


def album(part):
    if part == 'cover':
        im = new(base='#3a5a3a', seed=231, spread=3); fill(im, 4, 4, 12, 10, '#f0ead8'); fill(im, 5, 5, 11, 9, '#3a3a3a'); return im
    im = new(base='#e8e0cc', seed=232, spread=2); return im


def lunchbox(part):
    if part == 'front':
        im = new(base='#c84030', seed=241, spread=3); fill(im, 3, 3, 13, 11, '#3a7ab0'); fill(im, 6, 5, 10, 9, YELLOW); return im
    return new(base='#b83828', seed=242, spread=3)


def exit_sign(lit):
    im = new(base='#1a1a1a', seed=251, spread=1); fill(im, 2, 5, 14, 11, '#40e060' if lit else '#1a4a2a'); return im


# ---------------------------------------------------------------------------------------------------- items
def item_photo():
    im = new(); fill(im, 3, 2, 13, 14, '#f4f4ee'); fill(im, 4, 3, 12, 11, '#3a3a3a'); fill(im, 7, 6, 9, 10, '#0a0a0a'); return im


def item_walkie():
    im = new(); fill(im, 5, 4, 11, 15, '#2a2a2a'); fill(im, 6, 6, 10, 10, '#4a4a4a'); line(im, 9, 0, 9, 4, '#1a1a1a')
    im.load()[6, 11] = (255, 40, 30, 255); fill(im, 6, 12, 10, 14, YELLOW)
    return im


# ---------------------------------------------------------------------------------------------------- models
def element(lo, hi, textures, rotation=None, uv=None):
    faces = {}
    for face in ('north', 'south', 'east', 'west', 'up', 'down'):
        name = textures.get(face, textures.get('all'))
        if name is None:
            continue
        faces[face] = {'texture': '#' + name}
        if uv and face in uv:
            faces[face]['uv'] = uv[face]
    e = {'from': lo, 'to': hi, 'faces': faces}
    if rotation:
        e['rotation'] = rotation
    return e


def model(elements, textures, particle, render='cutout'):
    return {'ambientocclusion': False, 'render_type': 'minecraft:' + render, 'textures': {**{k: P + v for k, v in textures.items()}, 'particle': P + particle}, 'elements': elements}


def fitting_models():
    m = {}
    m['tag_0'] = model([element([4, 2, 15.5], [12, 10, 16], {'north': 't', 'south': 't'}, None, {'north': [3, 2, 13, 12], 'south': [3, 2, 13, 12]})], {'t': 'tag'}, 'tag')
    for s in range(2):
        m[f'photo_{s}'] = model([element([4, 0, 3], [12, .3, 13], {'up': 'p', 'down': 'b', 'north': 'b', 'south': 'b', 'east': 'b', 'west': 'b'})],
                                {'p': 'photo_up' if s else 'photo_down', 'b': 'photo_down'}, 'photo_down')
        m[f'balloon_{s}'] = model([element([3, 2, 7.99], [13, 14, 8.01], {'north': 'b', 'south': 'b'}), element([7.99, 2, 3], [8.01, 14, 13], {'east': 'b', 'west': 'b'}),
                                   element([7.9, -16, 7.9], [8.1, 2, 8.1], {'north': 's', 'south': 's', 'east': 's', 'west': 's'}, None, {f: [7, 0, 9, 16] for f in ('north', 'south', 'east', 'west')})],
                                  {'b': 'balloon_lit' if s else 'balloon', 's': 'string'}, 'balloon', 'translucent')
        m[f'grate_{s}'] = model([element([0, 0, 15], [16, 16, 16], {'north': 'g', 'south': 'g'})] if s == 0 else [element([0, 0, 0], [1, 16, 16], {'east': 'g', 'west': 'g'})], {'g': 'grate'}, 'grate')
        m[f'vent_{s}'] = model([element([0, 0, 15], [16, 16, 16], {'north': 'l', 'south': 'l'})] if s == 0 else [element([0, 0, 13], [16, 16, 14], {'north': 'l', 'south': 'l'}, {'angle': 22.5, 'axis': 'x', 'origin': [8, 0, 14]})],
                               {'l': 'lattice'}, 'lattice')
        m[f'hatch_{s}'] = model([element([0, 13, 0], [16, 16, 16], {'all': 'h', 'up': 't'})] if s == 0 else [element([0, 0, 0], [16, 16, 3], {'all': 'h', 'north': 't'})], {'h': 'hatch_side', 't': 'hatch_top'}, 'hatch_top')
        m[f'aisle_light_{s}'] = model([element([5, 0, 15], [11, 4, 16], {'all': 'l'})], {'l': 'aisle_lit' if s == 0 else 'aisle_off'}, 'aisle_lit')
        m[f'projector_{s}'] = model([element([3, 0, 2], [13, 9, 14], {'all': 'b', 'north': 'l'}), element([4, 9, 4], [8, 13, 8], {'all': 'b'}), element([9, 9, 6], [13, 13, 10], {'all': 'b'})],
                                    {'b': 'projector_body', 'l': 'projector_lit' if s else 'projector_lens'}, 'projector_body')
        m[f'screen_{s}'] = model([element([0, 0, 15], [16, 16, 16], {'north': 's', 'south': 's'})], {'s': 'screen_lit' if s else 'screen_dark'}, 'screen_dark')
        m[f'exit_sign_{s}'] = model([element([2, 4, 15], [14, 12, 16], {'all': 's'})], {'s': 'exit_lit' if s == 0 else 'exit_off'}, 'exit_lit')
    m['raft_0'] = model([element([0, 0, 0], [16, 2, 16], {'all': 'p'}), element([0, 2, 2], [16, 3, 5], {'all': 'p'}), element([0, 2, 11], [16, 3, 14], {'all': 'p'})], {'p': 'pallet'}, 'pallet')
    m['bowl_0'] = model([element([4, 0, 4], [12, 3, 12], {'all': 's', 'up': 't'})], {'s': 'bowl_side', 't': 'bowl_top'}, 'bowl_top')
    m['clothes_0'] = model([element([0, 0, 0], [16, 4, 16], {'all': 'c'})], {'c': 'clothes'}, 'clothes')
    m['drain_0'] = model([element([5, 0, 5], [11, 16, 11], {'all': 'd'})], {'d': 'drain'}, 'drain')
    m['batt_0'] = model([element([1, 6, 2], [15, 16, 14], {'all': 'i'})], {'i': 'insulation'}, 'insulation')
    seat_base = [element([2, 0, 3], [14, 6, 13], {'all': 'v'}), element([2, 6, 11], [14, 16, 14], {'all': 'v'}), element([0, 0, 2], [2, 9, 14], {'all': 'a'}), element([14, 0, 2], [16, 9, 14], {'all': 'a'}),
                 element([6, 12, 10.9], [10, 14, 11], {'north': 'n'})]
    folded = [element([2, 0, 11], [14, 4, 13], {'all': 'v'}), element([2, 4, 11], [14, 16, 14], {'all': 'v'}), element([0, 0, 2], [2, 9, 14], {'all': 'a'}), element([14, 0, 2], [16, 9, 14], {'all': 'a'}),
              element([2, 4, 7], [14, 6, 11], {'all': 'v'}, {'angle': 45, 'axis': 'x', 'origin': [8, 4, 11]})]
    sitter = [element([4, 6, 5], [12, 20, 11], {'all': 'x'}), element([5, 20, 5], [11, 26, 11], {'all': 'x'})]
    m['seat_0'] = model(seat_base, {'v': 'velvet_seat', 'a': 'seat_arm', 'n': 'seat_plate'}, 'velvet_seat')
    m['seat_1'] = model(folded, {'v': 'velvet_seat', 'a': 'seat_arm'}, 'velvet_seat')
    m['seat_2'] = model(seat_base + sitter, {'v': 'velvet_seat', 'a': 'seat_arm', 'n': 'seat_plate', 'x': 'sitter'}, 'velvet_seat')
    m['counter_0'] = model([element([0, 0, 0], [16, 16, 16], {'all': 'c', 'up': 't'})], {'c': 'counter', 't': 'counter_top'}, 'counter')
    m['poster_0'] = model([element([1, 1, 15.5], [15, 15, 16], {'north': 'p', 'south': 'p'})], {'p': 'poster'}, 'poster')
    m['album_0'] = model([element([3, 0, 3], [13, 2, 13], {'all': 'p', 'up': 'c', 'down': 'c'})], {'p': 'album_pages', 'c': 'album_cover'}, 'album_cover')
    m['lunchbox_0'] = model([element([3, 0, 5], [13, 7, 11], {'all': 's', 'north': 'f', 'south': 'f'}), element([6, 7, 7.5], [10, 9, 8.5], {'all': 's'})], {'s': 'lunchbox_side', 'f': 'lunchbox_front'}, 'lunchbox_front')
    return m


def textures():
    out = {}
    for k, v in materials().items():
        out[f'block/polaroid/{k}.png'] = v
    out['block/polaroid/tag.png'] = tag()
    out['block/polaroid/photo_down.png'] = photo(False); out['block/polaroid/photo_up.png'] = photo(True)
    out['block/polaroid/balloon.png'] = balloon(False); out['block/polaroid/balloon_lit.png'] = balloon(True); out['block/polaroid/string.png'] = string()
    out['block/polaroid/pallet.png'] = pallet(); out['block/polaroid/grate.png'] = grate(); out['block/polaroid/lattice.png'] = lattice()
    out['block/polaroid/bowl_top.png'] = bowl('top'); out['block/polaroid/bowl_side.png'] = bowl('side')
    out['block/polaroid/clothes.png'] = clothes()
    out['block/polaroid/hatch_top.png'] = hatch('top'); out['block/polaroid/hatch_side.png'] = hatch('side')
    out['block/polaroid/drain.png'] = drain()
    out['block/polaroid/velvet_seat.png'] = seat('velvet'); out['block/polaroid/seat_arm.png'] = seat('arm'); out['block/polaroid/sitter.png'] = seat('sitter'); out['block/polaroid/seat_plate.png'] = seat('plate')
    out['block/polaroid/aisle_lit.png'] = aisle_light(True); out['block/polaroid/aisle_off.png'] = aisle_light(False)
    out['block/polaroid/projector_body.png'] = projector('body'); out['block/polaroid/projector_lens.png'] = projector('lens'); out['block/polaroid/projector_lit.png'] = projector('lit')
    out['block/polaroid/screen_dark.png'] = screen(False); out['block/polaroid/screen_lit.png'] = screen(True)
    out['block/polaroid/counter.png'] = counter(); out['block/polaroid/counter_top.png'] = new(base='#c8b070', seed=213, spread=3)
    out['block/polaroid/poster.png'] = poster()
    out['block/polaroid/album_cover.png'] = album('cover'); out['block/polaroid/album_pages.png'] = album('pages')
    out['block/polaroid/lunchbox_front.png'] = lunchbox('front'); out['block/polaroid/lunchbox_side.png'] = lunchbox('side')
    out['block/polaroid/exit_lit.png'] = exit_sign(True); out['block/polaroid/exit_off.png'] = exit_sign(False)
    out['item/polaroid_photo.png'] = item_photo(); out['item/polaroid_walkie.png'] = item_walkie()
    return out


KINDS = ['tag', 'photo', 'balloon', 'raft', 'grate', 'vent', 'bowl', 'clothes', 'hatch', 'drain', 'batt', 'seat', 'aisle_light', 'projector', 'screen',
         'counter', 'poster', 'album', 'lunchbox', 'exit_sign']
STAGES = {'photo': 2, 'balloon': 2, 'grate': 2, 'vent': 2, 'hatch': 2, 'seat': 3, 'aisle_light': 2, 'projector': 2, 'screen': 2, 'exit_sign': 2}
FACING_Y = {'north': 0, 'east': 90, 'south': 180, 'west': 270}
ITEMS = ('polaroid_photo', 'polaroid_walkie')


def files():
    out = {}
    for name in materials():
        out[f'models/block/polaroid_{name}.json'] = {'parent': 'minecraft:block/cube_all', 'textures': {'all': P + name}}
        out[f'blockstates/polaroid_{name}.json'] = {'variants': {'': {'model': NS + f'block/polaroid_{name}'}}}
    for key, obj in fitting_models().items():
        out[f'models/block/polaroid/{key}.json'] = obj
    variants = {}
    for kind in KINDS:
        for facing, y in FACING_Y.items():
            for stage in range(4):
                drawn = stage if stage < STAGES.get(kind, 1) else 0
                v = {'model': NS + f'block/polaroid/{kind}_{drawn}'}
                if y:
                    v['y'] = y
                variants[f'facing={facing},kind={kind},stage={stage}'] = v
    out['blockstates/polaroid_fitting.json'] = {'variants': variants}
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
    print(('Verified' if check else 'Drew') + f' the Polaroid Trail: {len(images)} textures, {len(data)} models and states.')


if __name__ == '__main__':
    main()
