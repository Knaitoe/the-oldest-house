"""0.4.168: the kitchen table, the first quiet room, in native sixteen-pixel art.

The owner: "The skin-color selection should be the first vignette the player encounters." A crayon tin in two halves (six
skin crayons along the back of each and four others along the front; the crayons themselves are drawn by the client in each
reader's own order), the drawing with only its pencil lines, the fridge with old drawings and magnets on its lower door and
room on the upper one, a tabletop radio, the window's four o'clock sky and a crayon to hold.

0.4.196: the page is blank paper. The client draws the figure's pencil lines, the body and face the reader's pencil chose, and
the crayon, all from HouseReadKitchen's own map, so the page needs no lines or masks of its own. The pencil to hold is drawn here.
"""
from pathlib import Path
from PIL import Image, ImageDraw
import io, json, math, random, re

ROOT = Path(__file__).resolve().parents[1]
NS = 'the_oldest_house'
PARTS = ['tin_left', 'tin_right', 'sheet', 'fridge', 'fridge_top', 'radio']
# Parts drawn differently when TURNED: the drawing taken off the table, the radio switched off.
TURNED = ['sheet', 'radio']
# Parts drawn differently when WRITING: the words inside the lid.
WRITTEN = ['tin_left']


def _rng(name):
    return random.Random(1680 + sum(map(ord, name)) * 11)


def _fill(mid, name, specks, count):
    im = Image.new('RGBA', (16, 16), mid)
    d = ImageDraw.Draw(im)
    rng = _rng(name)
    for _ in range(count):
        d.point((rng.randrange(16), rng.randrange(16)), fill=rng.choice(specks))
    return im


def tiles(out):
    # The drawing: off-white paper. Its pencil lines are drawn by the client (0.4.196).
    out['sheet'] = _fill('#f1ead6', 'sheet', ['#e9e1cb', '#f6f0df'], 24)
    out['sheet_edge'] = _fill('#e7dfc8', 'sheet_edge', ['#ddd5bd'], 6)
    # The tin: painted red metal with a cream band, the lid's inside printed with the maker's sun.
    im = _fill('#b03a2e', 'tin', ['#9c3328', '#c24a3c'], 30)
    d = ImageDraw.Draw(im)
    d.line((0, 7, 15, 7), fill='#ead9b4')
    d.line((0, 8, 15, 8), fill='#ead9b4')
    out['tin'] = im
    out['tin_inside'] = _fill('#c9c4b6', 'tin_inside', ['#b9b3a4', '#d7d2c4'], 26)
    for name, written in [('tin_lid_left', False), ('tin_lid_left_written', True)]:
        im = Image.new('RGBA', (16, 16), '#ead9b4')
        d = ImageDraw.Draw(im)
        d.rectangle((0, 0, 15, 15), outline='#b03a2e')
        cx, cy, r = (8, 6, 3) if not written else (4, 4, 2)
        for a in range(0, 360, 45):
            x = cx + round(math.cos(math.radians(a)) * (r + 2))
            y = cy + round(math.sin(math.radians(a)) * (r + 2))
            d.point((x, y), fill='#d89a2c')
        d.ellipse((cx - r, cy - r, cx + r, cy + r), fill='#e8b63c', outline='#c98a22')
        if not written:
            d.point((cx - 1, cy - 1), fill='#5a3a1a')
            d.point((cx + 1, cy - 1), fill='#5a3a1a')
            d.line((cx - 1, cy + 1, cx + 1, cy + 1), fill='#5a3a1a')
            d.line((4, 12, 11, 12), fill='#b03a2e')
        else:
            for y, (x0, x1) in zip((9, 11, 13), ((2, 13), (2, 12), (2, 9))):
                for x in range(x0, x1 + 1):
                    if (x * 5 + y) % 4:
                        d.point((x, y), fill='#4a3d33')
            for y, (x0, x1) in zip((3, 5), ((8, 13), (8, 12))):
                for x in range(x0, x1 + 1):
                    if (x * 3 + y) % 5:
                        d.point((x, y), fill='#4a3d33')
        out[name] = im
    im = Image.new('RGBA', (16, 16), '#ead9b4')
    d = ImageDraw.Draw(im)
    d.rectangle((0, 0, 15, 15), outline='#b03a2e')
    for y in (4, 8, 12):
        d.line((2, y, 13, y), fill='#b03a2e')
    d.line((2, 6, 9, 6), fill='#c98a22')
    d.line((2, 10, 11, 10), fill='#c98a22')
    out['tin_lid_right'] = im
    # The fridge: white enamel. Its lower door carries four old drawings under magnets (a house, a dog, a sun with a face,
    # a rainbow), none of them people. The upper door is clear, for one more.
    out['fridge_side'] = _fill('#eeece4', 'fridge_side', ['#e3e0d7', '#f6f4ee'], 22)
    im = _fill('#f1efe8', 'fridge_front', ['#e6e3da', '#f8f6f0'], 18)
    d = ImageDraw.Draw(im)
    for box in [(1, 1, 5, 6), (6, 1, 10, 6), (1, 8, 5, 13), (6, 8, 10, 13)]:
        d.rectangle(box, fill='#fbf7ea', outline='#d9d2bf')
    # house
    d.rectangle((2, 4, 4, 6), fill='#c4473a')
    d.line((2, 3, 3, 2), fill='#4a5fa8')
    d.line((3, 2, 4, 3), fill='#4a5fa8')
    d.point((3, 5), fill='#6b4a2a')
    # sun with a face
    d.ellipse((7, 2, 9, 4), fill='#e9c64a')
    d.point((7, 3), fill='#7a5a20')
    d.point((9, 3), fill='#7a5a20')
    d.point((8, 5), fill='#e9a83a')
    d.point((10, 3), fill='#e9a83a')
    d.point((6, 3), fill='#e9a83a')
    # dog
    d.rectangle((2, 10, 4, 11), fill='#8a5a3a')
    d.point((4, 9), fill='#8a5a3a')
    d.point((2, 12), fill='#8a5a3a')
    d.point((4, 12), fill='#8a5a3a')
    d.point((1, 9), fill='#8a5a3a')
    # rainbow
    for i, colour in enumerate(['#d4592e', '#e9c64a', '#4c9648', '#3e64b0']):
        d.arc((6 + i // 2, 9 + i // 2, 10 - i // 2, 14), 180, 360, fill=colour)
    # magnets
    for x, y, c in [(3, 1, '#3e64b0'), (8, 1, '#c4473a'), (3, 8, '#4c9648'), (8, 8, '#e9c64a')]:
        d.point((x, y), fill=c)
    out['fridge_front'] = im
    im = _fill('#f1efe8', 'fridge_top_front', ['#e6e3da', '#f8f6f0'], 18)
    d = ImageDraw.Draw(im)
    d.line((0, 15, 15, 15), fill='#c9c5b8')
    out['fridge_top_front'] = im
    # The radio: a wooden case, a cloth grille and a lit dial; switched off, the dial goes dark.
    out['radio_side'] = _fill('#7a5233', 'radio_side', ['#6a4629', '#8a5f3d'], 30)
    for name, lit in [('radio_front', True), ('radio_front_off', False)]:
        im = Image.new('RGBA', (16, 16), '#7a5233')
        d = ImageDraw.Draw(im)
        d.rectangle((1, 2, 8, 13), fill='#cdbb94')
        for y in range(3, 13, 2):
            d.line((1, y, 8, y), fill='#b5a27c')
        d.rectangle((10, 3, 14, 8), fill='#f0d78a' if lit else '#a8a28f', outline='#4a3320')
        d.line((12, 3, 12, 8), fill='#b0302a' if lit else '#6e5a48')
        d.ellipse((10, 10, 11, 11), fill='#2b2420')
        d.ellipse((13, 10, 14, 11), fill='#2b2420')
        out[name] = im


def models(model, cube, faced):
    C = cube
    M = model

    def lid(x0, x1, face):
        return faced([x0, .5, 2.2], [x1, 11, 2.8], 'tin', south=face)

    def tray(left):
        x0, x1 = (1, 16) if left else (0, 15)
        side = [C([1, .5, 3.5], [1.5, 1.6, 14.5], 'tin')] if left else [C([14.5, .5, 3.5], [15, 1.6, 14.5], 'tin')]
        inner0, inner1 = (1.5, 16) if left else (0, 14.5)
        return [faced([x0, 0, 3], [x1, .5, 15], 'tin', up='tin_inside'), C([x0, .5, 14.5], [x1, 1.6, 15], 'tin'), C([x0, .5, 3], [x1, 1.6, 3.5], 'tin'),
                C([inner0, .5, 9.2], [inner1, 1, 9.5], 'tin_inside')] + side
    M('tin_left', tray(True) + [lid(1, 16, 'tin_lid_left')])
    M('tin_left_written', tray(True) + [lid(1, 16, 'tin_lid_left_written')])
    M('tin_right', tray(False) + [lid(0, 15, 'tin_lid_right')])
    M('sheet', [{'from': [.5, 0, .5], 'to': [15.5, .25, 15.5], 'faces': {
        'up': {'uv': [.5, .5, 15.5, 15.5], 'texture': '#sheet'}, 'down': {'uv': [.5, .5, 15.5, 15.5], 'texture': '#sheet_edge'},
        'north': {'uv': [.5, 0, 15.5, .25], 'texture': '#sheet_edge'}, 'south': {'uv': [.5, 0, 15.5, .25], 'texture': '#sheet_edge'},
        'east': {'uv': [.5, 0, 15.5, .25], 'texture': '#sheet_edge'}, 'west': {'uv': [.5, 0, 15.5, .25], 'texture': '#sheet_edge'}}}])
    M('sheet_turned', [])
    M('fridge', [faced([1, 0, 1], [15, 16, 15], 'fridge_side', south='fridge_front'), C([12, 4, 15], [13, 12, 15.8], 'chrome')])
    M('fridge_top', [faced([1, 0, 1], [15, 14.5, 15], 'fridge_side', south='fridge_top_front'), C([12, 1.5, 15], [13, 8, 15.8], 'chrome')])
    radio = lambda front: [faced([3, 0, 5], [13, 6, 11], 'radio_side', south=front), C([5, 6, 7.5], [11, 7, 8.5], 'radio_side')]
    M('radio', radio('radio_front'))
    M('radio_turned', radio('radio_front_off'))


def entity_textures():
    """What the client draws over the fittings: a crayon to tint (also the white the drawing's pencil and crayon are cut from)."""
    out = {}
    im = Image.new('RGBA', (16, 16), (255, 255, 255, 255))
    d = ImageDraw.Draw(im)
    for y in range(16):
        d.point((0, y), fill=(226, 226, 226, 255))
        d.point((15, y), fill=(214, 214, 214, 255))
    out['crayon'] = im
    return out


def sprites():
    """The crayon in the hand: a body tinted to its colour, and a paper wrapper that keeps its own."""
    body = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    wrapper = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    d = ImageDraw.Draw(body)
    w = ImageDraw.Draw(wrapper)
    for i in range(10):
        x, y = 3 + i, 12 - i
        d.point((x, y), fill=(255, 255, 255, 255))
        d.point((x + 1, y), fill=(236, 236, 236, 255))
        d.point((x, y + 1), fill=(214, 214, 214, 255))
    d.point((13, 2), fill=(255, 255, 255, 255))
    d.point((14, 1), fill=(236, 236, 236, 255))
    for i in range(3, 8):
        x, y = 3 + i, 12 - i
        w.point((x, y), fill=(234, 226, 204, 255))
        w.point((x + 1, y), fill=(214, 204, 180, 255))
        w.point((x, y + 1), fill=(196, 186, 162, 255))
    for i in (4, 6):
        x, y = 3 + i, 12 - i
        w.point((x, y), fill=(52, 46, 40, 255))
    # 0.4.196: the pencil, yellow and six-sided, a pink eraser in its metal band, the wood sharpened to a dark point.
    pencil = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    q = ImageDraw.Draw(pencil)
    for i in range(12):
        x, y = 2 + i, 13 - i
        if i < 2:
            light, dark = (236, 150, 160, 255), (204, 116, 128, 255)
        elif i < 3:
            light, dark = (196, 196, 188, 255), (150, 150, 144, 255)
        elif i < 10:
            light, dark = (240, 196, 64, 255), (196, 150, 40, 255)
        elif i < 11:
            light, dark = (226, 190, 140, 255), (186, 150, 104, 255)
        else:
            light, dark = (64, 60, 58, 255), (40, 38, 36, 255)
        q.point((x, y), fill=light)
        q.point((x + 1, y), fill=light)
        q.point((x, y + 1), fill=dark)
    q.point((14, 1), fill=(40, 38, 36, 255))
    for i in range(3, 10, 3):
        q.point((2 + i + 1, 13 - i), fill=(224, 176, 52, 255))
    return {'crayon': body, 'crayon_wrapper': wrapper, 'pencil': pencil}


def write(out, textures):
    """Everything this module adds, in the shapes draw_house_read.py writes."""
    for name, im in entity_textures().items():
        buf = io.BytesIO()
        im.save(buf, format='PNG')
        out['textures/entity/house_read/kitchen/' + name + '.png'] = buf.getvalue()
    for name, im in sprites().items():
        buf = io.BytesIO()
        im.save(buf, format='PNG')
        out['textures/item/read_' + name + '.png'] = buf.getvalue()
    # 0.4.196: the kitchen's pencil is a crayon stack with custom model data 1, drawn as a pencil.
    out['models/item/read_crayon.json'] = (json.dumps({'parent': 'minecraft:item/generated', 'textures': {'layer0': f'{NS}:item/read_crayon', 'layer1': f'{NS}:item/read_crayon_wrapper'},
        'overrides': [{'predicate': {'custom_model_data': 1}, 'model': f'{NS}:item/read_pencil'}]}, indent=2) + '\n').encode()
    out['models/item/read_pencil.json'] = (json.dumps({'parent': 'minecraft:item/generated', 'textures': {'layer0': f'{NS}:item/read_pencil'}}, indent=2) + '\n').encode()
    # The window's four o'clock sky: gold low down, paler above, a few soft clouds.
    im = Image.new('RGBA', (16, 16))
    rng = _rng('afternoon')
    for y in range(16):
        t = y / 15
        base = (round(246 - 6 * t), round(224 - 34 * t), round(170 - 62 * t))
        for x in range(16):
            jitter = rng.randrange(-3, 4)
            im.putpixel((x, y), (base[0] + jitter, base[1] + jitter, base[2] + jitter, 255))
    for x, y in [(3, 4), (4, 4), (5, 4), (4, 3), (10, 6), (11, 6), (12, 6), (11, 5)]:
        im.putpixel((x, y), (251, 238, 210, 255))
    buf = io.BytesIO()
    im.save(buf, format='PNG')
    out['textures/block/house_read/afternoon.png'] = buf.getvalue()
    out['models/block/read_afternoon.json'] = (json.dumps({'parent': 'minecraft:block/cube_all', 'textures': {'all': f'{NS}:block/house_read/afternoon'}}, indent=2) + '\n').encode()
    out['blockstates/read_afternoon.json'] = (json.dumps({'variants': {'': {'model': f'{NS}:block/read_afternoon'}}}, indent=2) + '\n').encode()
