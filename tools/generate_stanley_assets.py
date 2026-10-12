"""Original fitted Stanley cast and Edwardian hotel materials. No source geometry or saved originals change."""
from pathlib import Path
from PIL import Image, ImageDraw
import json, math, random

A = Path(__file__).resolve().parents[1] / 'src/main/resources/assets/the_oldest_house'
T = A / 'textures/block/stanley'
T.mkdir(parents=True, exist_ok=True)

def base(name, color, grain=False, size=32):
    im = Image.new('RGBA', (size, size))
    rng = random.Random('stanley:' + name)
    for y in range(size):
        for x in range(size):
            n = rng.randrange(-3, 4) + (int(4 * math.sin(x * .47 + math.sin(y * .14))) if grain else 0)
            im.putpixel((x, y), tuple(max(0, min(255, c + n)) for c in color) + (255,))
    return im

def save(name, im):
    im.save(T / (name + '.png'), optimize=True)

walnut = base('walnut', (63, 39, 30), True)
d = ImageDraw.Draw(walnut)
for x in (2, 10, 18, 26):
    d.line((x, 0, x, 31), fill=(83, 53, 34, 255))
    d.line((x + 1, 0, x + 1, 31), fill=(39, 25, 23, 255))
save('walnut', walnut)
for name, color in [('brass', (149, 112, 54)), ('steel', (43, 48, 46)), ('rubber', (71, 47, 37)), ('paper', (223, 212, 183)), ('food', (146, 73, 36))]:
    im = base(name, color)
    d = ImageDraw.Draw(im)
    if name == 'brass':
        d.line((0, 4, 31, 4), fill=(200, 169, 99, 255))
        d.line((0, 27, 31, 27), fill=(93, 91, 59, 255))
    if name == 'steel':
        for x in (3, 27):
            for y in (3, 27):
                d.rectangle((x, y, x + 1, y + 1), fill=(124, 112, 86, 255))
        d.line((2, 30, 29, 30), fill=(114, 71, 39, 255))
    if name == 'rubber':
        for y in range(2, 32, 4):
            d.line((0, y, 31, y), fill=(101, 71, 47, 255))
    if name == 'paper':
        for y in range(5, 31, 5):
            d.line((3, y, 28, y), fill=(179, 169, 145, 255))
    save(name, im)

carpet = base('carpet', (70, 28, 38))
d = ImageDraw.Draw(carpet)
for cx, cy in [(8, 8), (24, 24), (8, 24), (24, 8)]:
    d.polygon([(cx, cy - 7), (cx + 7, cy), (cx, cy + 7), (cx - 7, cy)], outline=(151, 121, 69, 255))
    d.polygon([(cx, cy - 4), (cx + 3, cy), (cx, cy + 4), (cx - 3, cy)], fill=(95, 83, 52, 255))
    d.rectangle((cx - 1, cy - 1, cx + 1, cy + 1), fill=(198, 170, 111, 255))
save('carpet', carpet)
wallpaper = base('wallpaper', (171, 154, 118))
d = ImageDraw.Draw(wallpaper)
for cx in (8, 24):
    for cy in (8, 24):
        d.line((cx, cy - 7, cx, cy + 7), fill=(114, 110, 72, 255))
        for side in (-1, 1):
            d.polygon([(cx, cy), (cx + side * 5, cy - 4), (cx + side * 3, cy + 1)], fill=(128, 122, 83, 255))
            d.line((cx, cy + 3, cx + side * 4, cy + 6), fill=(117, 108, 75, 255))
        d.rectangle((cx - 1, cy - 5, cx + 1, cy - 3), fill=(151, 93, 67, 255))
save('wallpaper', wallpaper)
wainscot = base('wainscot', (61, 40, 31), True)
d = ImageDraw.Draw(wainscot)
d.rectangle((2, 5, 29, 29), outline=(111, 77, 48, 255))
d.rectangle((4, 7, 27, 27), outline=(35, 24, 23, 255))
for y, c in [(0, (143, 107, 58, 255)), (2, (103, 73, 42, 255)), (3, (30, 23, 22, 255))]:
    d.line((0, y, 31, y), fill=c)
save('wainscot', wainscot)
ceiling = base('ceiling', (218, 207, 178))
d = ImageDraw.Draw(ceiling)
d.rectangle((1, 1, 30, 30), outline=(185, 169, 133, 255))
d.rectangle((3, 3, 28, 28), outline=(235, 224, 195, 255))
for r in (7, 4, 1):
    d.ellipse((16 - r, 16 - r, 16 + r, 16 + r), outline=(187, 170, 131, 255))
save('ceiling', ceiling)
keys = base('keys', (232, 222, 196))
d = ImageDraw.Draw(keys)
for x in range(0, 32, 4):
    d.line((x, 0, x, 31), fill=(96, 83, 60, 255))
for x in (4, 9, 17, 22, 27):
    d.rectangle((x, 0, x + 2, 19), fill=(29, 24, 25, 255))
    d.line((x, 0, x, 17), fill=(74, 58, 44, 255))
save('keys', keys)
patch = base('patch', (140, 116, 79), True)
d = ImageDraw.Draw(patch)
for x, y in [(3, 12), (11, 8), (22, 15), (13, 26)]:
    d.ellipse((x, y, x + 7, y + 4), fill=(119, 98, 67, 255))
save('patch', patch)

photo = base('party', (174, 153, 119), size=64)
d = ImageDraw.Draw(photo)
d.rectangle((2, 2, 61, 61), outline=(66, 47, 34, 255), width=3)
d.rectangle((7, 7, 56, 56), fill=(106, 101, 85, 255))
for row in range(3):
    for i in range(7):
        x, y = 10 + i * 7, 12 + row * 13
        shade = 203 - row * 9
        d.ellipse((x, y, x + 4, y + 5), fill=(shade, shade - 20, shade - 42, 255))
        d.rectangle((x - 1, y + 6, x + 5, y + 12), fill=(43 + i * 2, 39 + i, 33 + i, 255))
        d.line((x + 2, y + 6, x + 2, y + 10), fill=(188, 174, 142, 255))
        d.point((x + 1, y + 2), fill=(63, 53, 43, 255))
        d.point((x + 3, y + 2), fill=(63, 53, 43, 255))
# The native player head still occupies the existing front-row opening.
d.rectangle((28, 41, 36, 55), fill=(204, 186, 149, 255))
save('party', photo)
for pressure in range(4):
    im = base('gauge', (45, 47, 42))
    d = ImageDraw.Draw(im)
    d.ellipse((2, 2, 29, 29), fill=(166, 127, 60, 255), outline=(83, 69, 44, 255), width=2)
    d.ellipse((5, 5, 26, 26), fill=(221, 211, 177, 255))
    for i in range(9):
        a = math.radians(145 + i * 28)
        d.line((16 + 10 * math.cos(a), 16 + 10 * math.sin(a), 16 + 8 * math.cos(a), 16 + 8 * math.sin(a)), fill=(72, 66, 48, 255))
    a = math.radians(145 + pressure * 75)
    d.line((16, 16, 16 + 9 * math.cos(a), 16 + 9 * math.sin(a)), fill=(122, 32, 29, 255), width=2)
    d.rectangle((15, 15, 17, 17), fill=(81, 64, 40, 255))
    save('gauge_' + str(pressure), im)

roles = [('porter', (82, 27, 35), (185, 143, 110)), ('bartender', (39, 41, 39), (205, 163, 126)), ('scarred_guest', (85, 81, 60), (179, 147, 119)), ('pianist', (35, 32, 34), (176, 137, 108)), ('dancer', (45, 39, 37), (189, 145, 110)), ('dancer_dress', (93, 67, 87), (206, 164, 131))]
for index, (name, color, skin) in enumerate(roles):
    im = Image.new('RGBA', (128, 64), (0, 0, 0, 0))
    d = ImageDraw.Draw(im)
    d.rectangle((0, 16, 63, 63), fill=color + (255,))
    d.rectangle((0, 0, 31, 15), fill=skin + (255,))
    hair = (52 + index * 3, 41 + index * 2, 33 + index * 2, 255) if index != 3 else (113, 105, 92, 255)
    d.rectangle((8, 0, 15, 7), fill=hair)
    for x0, x1 in ((0, 7), (16, 23), (24, 31)):
        d.rectangle((x0, 8, x1, 15), fill=hair)
    d.rectangle((8, 8, 15, 9), fill=hair)
    for x in (9, 14):
        d.point((x, 11), fill=(37, 34, 31, 255))
    d.line((10, 14, 13, 14), fill=(118, 75, 59, 255))
    if index == 2:
        d.line((12, 9, 11, 11, 13, 13, 12, 15), fill=(120, 73, 59, 255))
    if index == 1:
        d.line((10, 13, 13, 13), fill=hair)
    d.rectangle((0, 16, 15, 31), fill=(30, 27, 28, 255))
    d.rectangle((0, 28, 15, 31), fill=(24, 22, 22, 255))
    d.line((4, 29, 11, 29), fill=(88, 66, 47, 255))
    d.rectangle((20, 20, 27, 22), fill=(224, 214, 191, 255))
    d.polygon([(20, 22), (23, 25), (24, 23), (27, 22)], fill=(218, 206, 177, 255))
    d.line((24, 24, 24, 31), fill=(155, 115, 62, 255))
    d.rectangle((40, 26, 55, 27), fill=(222, 210, 181, 255))
    d.rectangle((40, 28, 55, 31), fill=skin + (255,))
    d.rectangle((64, 0, 103, 23), fill=color + (255,))
    if index == 0:
        d.line((64, 11, 103, 11), fill=(186, 145, 66, 255))
    d.rectangle((64, 30, 95, 63), fill=color + (255,))
    for y in range(34, 63, 6):
        d.line((65, y, 93, y + 1), fill=tuple(max(0, c - 11) for c in color) + (255,))
    d.rectangle((96, 0, 119, 22), fill=(225, 214, 187, 255))
    d.line((99, 11, 115, 11), fill=(192, 180, 154, 255))
    d.rectangle((96, 24, 101, 31), fill=skin + (255,))
    d.rectangle((104, 24, 114, 41), fill=(218, 194, 131, 255))
    d.line((105, 24, 105, 39), fill=(244, 230, 187, 255))
    d.rectangle((116, 24, 127, 40), fill=(66, 81, 54, 255))
    d.rectangle((119, 29, 125, 31), fill=(211, 196, 155, 255))
    d.rectangle((100, 44, 121, 53), fill=(218, 208, 178, 255))
    im.resize((512, 256), Image.Resampling.NEAREST).save(A / 'textures/entity' / ('stanley_' + name + '.png'), optimize=True)

replacements = {'wood': 'walnut', 'brass': 'brass', 'steel': 'steel', 'rubber': 'rubber', 'paper': 'paper', 'keys': 'keys', 'food': 'food', 'patch': 'patch', 'party': 'party', 'carpet': 'carpet', 'wallpaper': 'wallpaper', 'wainscot': 'wainscot', 'ceiling': 'ceiling', **{f'gauge_{i}': f'gauge_{i}' for i in range(4)}}
for folder in ('block', 'item'):
    for p in (A / 'models' / folder).glob('hotel_*.json'):
        data = json.loads(p.read_text())
        for key, value in data.get('textures', {}).items():
            prefix = 'the_oldest_house:block/hotel_'
            if value.startswith(prefix) and value[len(prefix):] in replacements:
                data['textures'][key] = 'the_oldest_house:block/stanley/' + replacements[value[len(prefix):]]
        p.write_text(json.dumps(data, indent=2) + '\n')
print('Fitted six 512x256 jointed-cast atlases and seventeen original hotel materials to existing native meshes.')
