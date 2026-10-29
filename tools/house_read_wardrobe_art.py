"""0.4.169: the coatroom's wardrobe, in native sixteen-pixel art.

Nine more coats and three more pairs of boots beside the original six, each one unmistakable on its hook and on the reader:
a scarlet ringmaster's tailcoat with gold frogging, a navy pea coat, a faded blue chore coat, a dusty rose swing coat, a taupe
coat with a soft fleece collar, a yellow school rain mac, a denim jacket covered in patches, a bottle-green duffel with horn
toggles, and a long cardigan knitted in two yarns; tall chestnut riding boots, red galoshes with newspaper in the toes, and
white plimsolls written on in ballpoint. Each coat hangs from the same brass hook as the first three, cut to its own shape
(long, belted or short), and its worn sheet follows the fitted coat mesh (HouseReadClothes.layer).

0.4.172 (owner: "Add basic t-shirt and jeans, crop top and shorts, tank top and shorts. Clothing swappable like the hair tools"):
three everyday outfits, each on a wooden hanger on a brass rail along the back wall, the top over its bottoms: a washed charcoal
tee with a faded print over jeans worn through at one knee, a mustard crop top over frayed cut-offs, and a white ribbed tank top
over grey drawstring shorts. Worn, the top follows the body and arms and the bottoms each leg (64x32 sheets, HouseReadClothes).
"""
from PIL import Image, ImageDraw
import house_read_rooms160_art as rooms160

COATS = ['ringmaster', 'pea_coat', 'chore_coat', 'swing_coat', 'collared_coat', 'rain_mac', 'patched_jacket', 'duffel', 'long_cardigan']
BOOTS = ['riding_boots', 'galoshes', 'plimsolls']
OUTFITS = ['tee_jeans', 'crop_shorts', 'tank_shorts']
PARTS = COATS + BOOTS + OUTFITS
# How each coat is cut, as HouseReadWardrobe.Cut: the worn mesh and the hanging model both follow it.
CUT = {'ringmaster': 'long', 'pea_coat': 'short', 'chore_coat': 'short', 'swing_coat': 'long', 'collared_coat': 'belted',
       'rain_mac': 'belted', 'patched_jacket': 'short', 'duffel': 'long', 'long_cardigan': 'long'}
# Cloth colours (mid, dark, light) and weave for each garment.
CLOTH = {
    'ringmaster': ('#932b29', '#6a1d1c', '#b24039', 'wool'),
    'pea_coat': ('#2d3648', '#1f2634', '#3f4b62', 'wool'),
    'chore_coat': ('#55708f', '#405873', '#7590ad', 'twill'),
    'swing_coat': ('#a7727a', '#86565e', '#c19199', 'wool'),
    'collared_coat': ('#7b6d61', '#5e5248', '#958779', 'wool'),
    'rain_mac': ('#c9a43a', '#a3842a', '#dec05f', 'canvas'),
    'patched_jacket': ('#526a8b', '#3e526e', '#6e86a6', 'twill'),
    'duffel': ('#3d5947', '#2c4234', '#55735f', 'wool'),
    'long_cardigan': ('#c0b298', '#a1937a', '#d6cab2', 'knit'),
    'riding_boots': ('#5c3420', '#3f2315', '#7c4b2f', 'leather'),
    'galoshes': ('#a5352d', '#7e2620', '#c24c42', 'leather'),
    'plimsolls': ('#ddd7c6', '#bdb6a2', '#efeadb', 'canvas'),
}
GOLD, BRASS, HORN, INK, FLEECE = '#caa24c', '#a88a49', '#d8c9a3', '#33489a', '#dcd3c1'
LOWER = ('#7c8694', '#626b78', '#97a0ad')  # the cardigan's second yarn
# The outfits' cloth (mid, dark, light): each top, then its bottoms.
TEE, JEANS = ('#4a4d52', '#3a3c40', '#5d6066'), ('#4d6a8f', '#3c5575', '#6683a8')
CROP, CUTOFFS = ('#c79a3c', '#a67e2c', '#ddb65f'), ('#7d97b5', '#6580a0', '#9cb2cb')
TANK, JERSEY = ('#e2ded3', '#c8c3b6', '#f1eee6'), ('#8a8d90', '#74777a', '#a3a6a8')
PRINT, RUST, THREAD, BUTTON = '#b9b2a0', '#9c6a4f', '#e3e6e3', '#b0904a'


def _jersey(d, box, mid, dark, light, rng, rib=False):
    """Knit cotton: speckled, or ribbed in fine vertical lines."""
    for name, (x0, y0, x1, y1) in box.items():
        d.rectangle((x0, y0, x1, y1), fill=mid)
        for y in range(y0, y1 + 1):
            for x in range(x0, x1 + 1):
                if rib and x % 2 == 0: d.point((x, y), fill=dark)
                elif not rib and rng.random() < .14: d.point((x, y), fill=light if rng.random() < .5 else dark)


def _speck(im, name, mid, dark, light, count=50):
    d = ImageDraw.Draw(im); rng = rooms160._rng(name)
    for _ in range(count): d.point((rng.randrange(16), rng.randrange(16)), fill=dark if rng.randrange(2) else light)
    return d


def tiles(out):
    """The cloth of each garment, and the front of each coat as it hangs (lapels, buttons, pockets, braid)."""
    for n in COATS + BOOTS:
        mid, dark, light, weave = CLOTH[n]
        im = Image.new('RGBA', (16, 16), mid); d = _speck(im, n, mid, dark, light, 40 if weave != 'knit' else 0)
        if weave == 'knit':
            for x in range(0, 16, 2): d.line((x, 0, x, 15), fill=dark)
            for y in range(1, 16, 3): d.point(((y * 5) % 16, y), fill=light)
        elif weave == 'twill':
            for i in range(-16, 16, 3): d.line((i, 15, i + 15, 0), fill=dark)
        out[n] = im
    # The fronts.
    im = out['ringmaster'].copy(); d = ImageDraw.Draw(im)
    d.polygon([(4, 0), (8, 7), (6, 0)], fill='#1d1615'); d.polygon([(12, 0), (8, 7), (10, 0)], fill='#1d1615')
    for y in (6, 8, 10, 12): d.line((3, y, 13, y), fill=GOLD); d.point((3, y), fill='#8e6e2c'); d.point((13, y), fill='#8e6e2c')
    d.line((8, 7, 8, 15), fill='#5a1716'); out['ringmaster_front'] = im
    im = out['pea_coat'].copy(); d = ImageDraw.Draw(im)
    d.polygon([(3, 0), (7, 5), (6, 0)], fill='#252d3c'); d.polygon([(13, 0), (9, 5), (10, 0)], fill='#252d3c'); d.line((10, 5, 10, 15), fill='#1b2130')
    for y in (6, 9, 12):
        for x in (5, 11): d.point((x, y), fill='#9aa0aa')
    d.line((2, 13, 4, 11), fill='#1b2130'); d.line((14, 13, 12, 11), fill='#1b2130'); out['pea_coat_front'] = im
    im = out['chore_coat'].copy(); d = ImageDraw.Draw(im)
    d.line((8, 0, 8, 15), fill='#3a5068')
    for y in (2, 6, 10, 14): d.point((9, y), fill='#c6c0ad')
    d.rectangle((2, 3, 6, 6), outline='#3a5068'); d.rectangle((2, 9, 6, 13), outline='#3a5068'); d.rectangle((10, 9, 14, 13), outline='#3a5068')
    d.rectangle((11, 3, 13, 5), fill='#7e98b3'); out['chore_coat_front'] = im  # a lighter square where a badge was unpicked
    im = out['swing_coat'].copy(); d = ImageDraw.Draw(im)
    d.ellipse((3, 0, 8, 3), fill='#c69ba2', outline='#86565e'); d.ellipse((8, 0, 13, 3), fill='#c69ba2', outline='#86565e')
    for y in (5, 9, 13): d.rectangle((7, y, 9, y + 1), fill='#6e434a')
    out['swing_coat_front'] = im
    im = out['collared_coat'].copy(); d = ImageDraw.Draw(im)
    d.line((8, 0, 8, 15), fill='#54493f')
    for y in (4, 8): d.point((9, y), fill='#3a322b')
    d.rectangle((0, 9, 15, 10), fill='#5e5248'); d.rectangle((7, 9, 9, 10), fill='#9c8d72'); out['collared_coat_front'] = im
    im = out['rain_mac'].copy(); d = ImageDraw.Draw(im)
    d.line((7, 0, 7, 15), fill='#9a7c24')
    for y in (3, 6): d.rectangle((8, y, 9, y), fill='#3d3a35'); d.line((6, y, 8, y), fill='#3d3a35')
    d.rectangle((0, 9, 15, 10), fill='#a3842a'); d.rectangle((7, 9, 9, 10), fill='#e6d9b0'); out['rain_mac_front'] = im
    im = out['patched_jacket'].copy(); d = ImageDraw.Draw(im)
    d.line((8, 0, 8, 15), fill='#3a4c66')
    for x, y, c in ((3, 3, '#b8483c'), (11, 2, '#d8b84a'), (4, 9, '#5e8c56'), (11, 10, '#e6e0cf'), (7, 13, '#7c4f86')):
        d.rectangle((x - 1, y - 1, x + 1, y + 1), fill=c); d.point((x, y), fill='#2f2b28')
    out['patched_jacket_front'] = im
    im = out['duffel'].copy(); d = ImageDraw.Draw(im)
    d.line((8, 0, 8, 15), fill='#263a2d')
    for y in (3, 7, 11): d.line((5, y, 7, y), fill='#2a2620'); d.rectangle((8, y - 1, 10, y), fill=HORN); d.line((11, y, 12, y), fill='#2a2620')
    d.rectangle((2, 12, 5, 15), outline='#2c4234'); d.rectangle((11, 12, 14, 15), outline='#2c4234'); out['duffel_front'] = im
    im = out['long_cardigan'].copy(); d = ImageDraw.Draw(im)
    d.line((8, 0, 8, 15), fill='#8e8169')
    for y in (2, 6, 10, 14): d.point((9, y), fill='#6f5a3e')
    out['long_cardigan_front'] = im
    im = Image.new('RGBA', (16, 16), LOWER[0]); d = ImageDraw.Draw(im)
    for x in range(0, 16, 2): d.line((x, 0, x, 15), fill=LOWER[1])
    d.line((8, 0, 8, 15), fill='#5a6270')
    for y in (2, 6, 10, 14): d.point((9, y), fill='#6f5a3e')
    out['long_cardigan_lower'] = im
    im = Image.new('RGBA', (16, 16), GOLD); _speck(im, 'braid', GOLD, '#9c7a34', '#e4c574', 40); out['wardrobe_braid'] = im
    im = Image.new('RGBA', (16, 16), FLEECE); _speck(im, 'fleece', FLEECE, '#c2b8a3', '#ece5d6', 90); out['wardrobe_fleece'] = im
    im = Image.new('RGBA', (16, 16), HORN); _speck(im, 'horn', HORN, '#b9a781', '#ebdfc0', 30); out['wardrobe_horn'] = im
    im = Image.new('RGBA', (16, 16), '#e4e0d4'); d = ImageDraw.Draw(im)
    for y in range(1, 16, 2): d.line((1, y, 14, y), fill='#a8a49a' if y % 4 == 1 else '#c6c2b6')
    d.rectangle((2, 2, 7, 6), fill='#77746c'); out['wardrobe_newsprint'] = im
    im = Image.new('RGBA', (16, 16), '#efe9d6'); _speck(im, 'rubber_toe', '#efe9d6', '#d4ccb4', '#fbf7ea', 25); out['wardrobe_rubber'] = im
    im = Image.new('RGBA', (16, 16), '#2a2420'); _speck(im, 'dark_sole', '#2a2420', '#1d1916', '#3a332d', 25); out['wardrobe_sole'] = im
    # The plimsolls' sides, written on: a phone number and a heart.
    im = out['plimsolls'].copy(); d = ImageDraw.Draw(im)
    for x in (2, 4, 6, 9, 11, 13): d.line((x, 6, x, 8), fill=INK)
    d.line((3, 7, 3, 7), fill=INK); d.point((7, 11), fill=INK); d.point((9, 11), fill=INK); d.line((7, 12, 9, 12), fill=INK); d.point((8, 13), fill=INK)
    out['plimsolls_written'] = im
    # The riding boots' scuffed heel.
    im = out['riding_boots'].copy(); d = ImageDraw.Draw(im); d.line((1, 13, 5, 12), fill='#a5774f'); d.point((3, 14), fill='#a5774f'); out['riding_boots_scuffed'] = im
    outfit_tiles(out)


def outfit_tiles(out):
    """0.4.172: the outfits' cloth as they hang. Each top is named after its outfit, so it is that fitting's particle."""
    for name, cloth, count in (('tee_jeans', TEE, 30), ('jeans', JEANS, 0), ('crop_shorts', CROP, 30), ('cutoffs', CUTOFFS, 0), ('tank_shorts', TANK, 0), ('jersey', JERSEY, 45)):
        mid, dark, light = cloth
        im = Image.new('RGBA', (16, 16), mid); d = _speck(im, 'outfit_' + name, mid, dark, light, count)
        if name in ('jeans', 'cutoffs'):
            for i in range(-16, 16, 3): d.line((i, 15, i + 15, 0), fill=dark)
        elif name == 'tank_shorts':
            for x in range(0, 16, 2): d.line((x, 0, x, 15), fill=dark)
        out[name] = im
    # The tee's front: a crew neck and a faded print from a concert nobody remembers.
    im = out['tee_jeans'].copy(); d = ImageDraw.Draw(im)
    d.line((5, 0, 10, 0), fill=TEE[1]); d.line((6, 1, 9, 1), fill=TEE[1])
    d.ellipse((5, 5, 10, 10), outline=PRINT); d.line((6, 8, 9, 8), fill=RUST); d.point((7, 6), fill=RUST); d.point((8, 7), fill=PRINT)
    out['tee_jeans_front'] = im
    # The jeans: a fly seam, pocket stitching, and one knee worn through to the threads.
    im = out['jeans'].copy(); d = ImageDraw.Draw(im)
    d.line((8, 0, 8, 4), fill=JEANS[1]); d.line((1, 1, 4, 3), fill='#c79b56'); d.line((14, 1, 11, 3), fill='#c79b56')
    d.line((7, 4, 7, 15), fill=JEANS[1]); d.line((8, 4, 8, 15), fill=JEANS[2])
    for x in range(2, 6): d.point((x, 9), fill=THREAD); d.point((x, 10), fill=THREAD if x % 2 else JEANS[2])
    out['jeans_front'] = im
    im = out['crop_shorts'].copy(); d = ImageDraw.Draw(im); d.line((5, 0, 10, 0), fill=CROP[1]); d.line((0, 15, 15, 15), fill=CROP[1]); out['crop_front'] = im
    # The cut-offs: a button at the waist, and the hem frayed into loose threads.
    im = out['cutoffs'].copy(); d = ImageDraw.Draw(im)
    d.point((8, 1), fill=BUTTON); d.line((8, 2, 8, 6), fill=CUTOFFS[1]); d.line((7, 6, 7, 13), fill=CUTOFFS[1])
    for x in range(16): d.point((x, 14), fill=THREAD if x % 2 else CUTOFFS[2]); d.point((x, 15), fill=THREAD if x % 3 == 0 else CUTOFFS[0])
    out['cutoffs_front'] = im
    im = out['tank_shorts'].copy(); d = ImageDraw.Draw(im); d.arc((4, -4, 11, 4), 0, 180, fill=TANK[1]); out['tank_front'] = im
    # The drawstring shorts: a waistband with its cord tied in a bow.
    im = out['jersey'].copy(); d = ImageDraw.Draw(im)
    d.rectangle((0, 0, 15, 2), fill=JERSEY[1]); d.point((7, 2), fill='#e9e6dc'); d.point((9, 2), fill='#e9e6dc'); d.line((7, 3, 6, 6), fill='#e9e6dc'); d.line((9, 3, 10, 6), fill='#e9e6dc')
    d.line((7, 6, 7, 15), fill=JERSEY[1])
    out['jersey_front'] = im


def models(model, cube, faced):
    """Each coat on the hall's brass hook, cut to its shape, with only the hook left once it is taken; each pair of boots on
    the mat, with the mat's rubber square left once they are worn."""
    frame = [cube([7, 28, 0], [9, 29, 3], 'brass'), cube([7, 29, 2], [9, 31, 3], 'brass'), cube([3, 26, 1.5], [13, 27, 3.5], 'oak')]
    tops = {'ringmaster': 12, 'pea_coat': 13, 'chore_coat': 11, 'swing_coat': 4, 'collared_coat': 6, 'rain_mac': 4, 'patched_jacket': 14, 'duffel': 3, 'long_cardigan': 1}
    for n in COATS:
        top = tops[n]; front = n + '_front'; cut = CUT[n]
        garment = [cube([3, 22, 2], [13, 26, 7], n), cube([2, max(top, 8) + 4, 2.6], [4, 22.5, 6.4], n), cube([12, max(top, 8) + 4, 2.6], [14, 22.5, 6.4], n)]
        if n == 'ringmaster':
            # A cutaway front to the waist, and the two tails hanging behind it to the knee.
            garment += [faced([4, 12, 2.2], [12, 25, 6.8], n, south=front), cube([4.2, 2, 2.4], [7.6, 12, 5.2], n), cube([8.4, 2, 2.4], [11.8, 12, 5.2], n),
                        cube([3.4, 25.4, 2.5], [12.6, 26.6, 6.6], 'wardrobe_braid')]
        elif n == 'swing_coat':
            # The bodice, then the skirt flaring out wider than the shoulders.
            garment += [faced([4, 13, 2.2], [12, 25, 6.8], n, south=front), faced([2.6, top, 1.6], [13.4, 13.2, 7.4], n, south=front), cube([4.5, 25.5, 1.8], [11.5, 26.8, 6], n)]
        elif n == 'long_cardigan':
            # Knitted to the shins: the first yarn to the hip, then the other.
            garment += [faced([4, 12, 2.2], [12, 25, 6.8], n, south=front), faced([4.2, top, 2.3], [11.8, 12.1, 6.7], 'long_cardigan_lower', south='long_cardigan_lower')]
        elif n == 'duffel':
            garment += [faced([4, top, 2.2], [12, 25, 6.8], n, south=front), cube([4.5, 25.5, 1.8], [11.5, 26.8, 6], n)] + \
                       [cube([7.4, y, 6.8], [8.6, y + 1.2, 7.4], 'wardrobe_horn') for y in (20, 16, 12)]
        elif cut == 'belted':
            collar = cube([2.8, 24.5, 1.6], [13.2, 26.9, 7.4], 'wardrobe_fleece') if n == 'collared_coat' else cube([3.2, 25.5, 3.5], [12.8, 26.5, 5.5], n)
            garment += [faced([4, 12, 2.2], [12, 25, 6.8], n, south=front), cube([3.5, top, 2], [12.5, 12.5, 7], n), cube([3.8, 11, 1.9], [12.2, 12.2, 7.1], n), collar]
        else:  # short jackets with a collar
            garment += [faced([4, top, 2.2], [12, 25, 6.8], n, south=front), cube([4, 25.3, 1.8], [12, 26.8, 6.3], n)]
        model(n, frame + garment); model(n + '_turned', frame)
    pair = lambda make: [e for x in (0, 8) for e in make(x)]
    model('riding_boots', pair(lambda x: [cube([x + 2, 0, 3], [x + 7, 1, 13], 'wardrobe_sole'), faced([x + 2.2, 1, 3.2], [x + 6.8, 4, 12.8], 'riding_boots', east='riding_boots_scuffed'),
                                          cube([x + 2.4, 4, 7.6], [x + 6.6, 15, 12.6], 'riding_boots'), cube([x + 2.3, 14, 7.5], [x + 6.7, 15.5, 12.7], 'riding_boots_scuffed')]))
    model('galoshes', pair(lambda x: [cube([x + 1.8, 0, 2.8], [x + 7.2, 1.5, 13.2], 'wardrobe_sole'), cube([x + 2, 1.5, 3], [x + 7, 9, 13], 'galoshes'),
                                      cube([x + 2.6, 8.6, 7], [x + 6.4, 9.8, 12.4], 'wardrobe_newsprint')]))
    model('plimsolls', pair(lambda x: [cube([x + 2, 0, 3], [x + 7, 1.2, 13], 'wardrobe_rubber'), faced([x + 2.2, 1.2, 3.2], [x + 6.8, 4, 12.8], 'plimsolls', east='plimsolls_written', west='plimsolls_written'),
                                       cube([x + 2.1, .6, 2.6], [x + 6.9, 2.4, 3.4], 'wardrobe_rubber')]))
    for n in BOOTS: model(n + '_turned', [cube([2, 0, 3], [14, .5, 13], 'rubber')])
    # 0.4.172: the outfits. A brass rail runs the width of each block on a bracket, so three side by side make one rail; each
    # outfit hangs on its own wooden hanger, the top over its bottoms, and once it is taken the empty hanger stays.
    rail = [cube([0, 27, 2.2], [16, 28, 3.2], 'brass'), cube([7.5, 27.2, 0], [8.5, 28.4, 2.2], 'brass'),
            cube([7.6, 25.6, 2.4], [8.4, 27, 3], 'brass'), cube([3, 24.8, 2.1], [13, 25.6, 4.1], 'oak')]
    tee = [faced([4, 15.5, 2.3], [12, 24.8, 4.9], 'tee_jeans', south='tee_jeans_front'),
           cube([2.4, 21, 2.5], [4, 24.6, 4.7], 'tee_jeans'), cube([12, 21, 2.5], [13.6, 24.6, 4.7], 'tee_jeans')]
    jeans = [faced([4.3, 13.8, 2.6], [11.7, 15.5, 4.6], 'jeans', south='jeans_front'),
             faced([4.3, 1, 2.7], [7.9, 13.8, 4.5], 'jeans', south='jeans_front'), faced([8.1, 1, 2.7], [11.7, 13.8, 4.5], 'jeans', south='jeans')]
    crop = [faced([4, 19.2, 2.3], [12, 24.8, 4.9], 'crop_shorts', south='crop_front'),
            cube([2.6, 21.6, 2.5], [4, 24.6, 4.7], 'crop_shorts'), cube([12, 21.6, 2.5], [13.4, 24.6, 4.7], 'crop_shorts')]
    cutoffs = [faced([4.3, 14.6, 2.6], [11.7, 16.4, 4.6], 'cutoffs', south='cutoffs_front'),
               faced([4.3, 9.4, 2.7], [7.9, 14.6, 4.5], 'cutoffs', south='cutoffs_front'), faced([8.1, 9.4, 2.7], [11.7, 14.6, 4.5], 'cutoffs', south='cutoffs_front')]
    tank = [faced([4.6, 16, 2.4], [11.4, 23.2, 4.6], 'tank_shorts', south='tank_front'),
            cube([5.2, 23.2, 2.6], [6.6, 24.8, 4.2], 'tank_shorts'), cube([9.4, 23.2, 2.6], [10.8, 24.8, 4.2], 'tank_shorts')]
    jersey = [faced([4.3, 14.2, 2.6], [11.7, 16, 4.6], 'jersey', south='jersey_front'),
              faced([4.2, 8.6, 2.7], [7.9, 14.2, 4.5], 'jersey', south='jersey'), faced([8.1, 8.6, 2.7], [11.8, 14.2, 4.5], 'jersey', south='jersey')]
    for n, garment in (('tee_jeans', tee + jeans), ('crop_shorts', crop + cutoffs), ('tank_shorts', tank + jersey)):
        model(n, rail + garment); model(n + '_turned', rail)


def worn():
    """Each garment on the reader, on the 64x32 layout of the fitted coat mesh."""
    BODY, ARM, SLIM, HEM, COLLAR, BELT, LEG = rooms160.BODY, rooms160.ARM, rooms160.SLIM_ARM, rooms160.HEM, rooms160.COLLAR, rooms160.BELT, rooms160.LEG
    sheets = {}
    for n in COATS:
        mid, dark, light, weave = CLOTH[n]; cut = CUT[n]
        im = Image.new('RGBA', (64, 32), (0, 0, 0, 0)); d = ImageDraw.Draw(im); rng = rooms160._rng('wardrobe_' + n)
        boxes = [BODY, ARM, SLIM] + ([COLLAR] if cut == 'short' else [HEM]) + ([BELT] if cut == 'belted' else [])
        for box in boxes: rooms160._cloth(d, box, mid, dark, light, rng, 'wool' if weave == 'knit' else weave)
        fx0, fy0, fx1, fy1 = BODY['front']; cx = (fx0 + fx1) // 2; bx0, by0, bx1, by1 = BODY['back']
        hx0, hy0, hx1, hy1 = HEM['front']; kx0, ky0, kx1, ky1 = HEM['back']
        cuff = dark
        if weave == 'knit':
            for box in boxes:
                for name, (x0, y0, x1, y1) in box.items():
                    for x in range(x0, x1 + 1, 2): d.line((x, y0, x, y1), fill=dark)
        if n == 'ringmaster':
            for i in range(5): d.point((cx - 1 - i, fy0 + i), fill='#1d1615'); d.point((cx + 1 + i, fy0 + i), fill='#1d1615')
            for y in range(fy0 + 5, fy1 - 2, 2): d.line((fx0 + 1, y, fx1 - 1, y), fill=GOLD)
            d.line((cx, fy0 + 5, cx, fy1), fill='#5a1716')
            # The tails: the skirt open at the front, split at the back.
            d.rectangle((hx0 + 2, hy0, hx1 - 2, hy1), fill=(0, 0, 0, 0)); d.line(((kx0 + kx1) // 2, ky0, (kx0 + kx1) // 2, ky1), fill='#4a1414')
            for arm in (ARM, SLIM): x0, y0, x1, y1 = arm['top']; d.rectangle((x0, y0, x1, y1), fill=GOLD)
            cuff = GOLD
        elif n == 'pea_coat':
            for i in range(4): d.point((cx - 2 - i, fy0 + i), fill='#252d3c'); d.point((cx + 1 + i, fy0 + i), fill='#252d3c')
            d.line((cx + 2, fy0 + 4, cx + 2, fy1), fill='#1b2130')
            for y in (fy0 + 5, fy0 + 8, fy0 + 11):
                for x in (cx - 2, cx + 3): d.point((x, y), fill='#9aa0aa')
            d.line((fx0 + 1, fy1 - 2, fx0 + 2, fy1 - 4), fill='#1b2130'); d.line((fx1 - 1, fy1 - 2, fx1 - 2, fy1 - 4), fill='#1b2130')
        elif n == 'chore_coat':
            d.line((cx, fy0, cx, fy1), fill='#3a5068')
            for y in range(fy0 + 1, fy1, 3): d.point((cx + 1, y), fill='#c6c0ad')
            d.rectangle((fx0 + 1, fy0 + 3, fx0 + 3, fy0 + 5), outline='#3a5068')
            for x0 in (fx0 + 1, fx1 - 3): d.rectangle((x0, fy1 - 4, x0 + 2, fy1 - 1), outline='#3a5068')
            # A patched elbow on the right sleeve, where your brother leant.
            for arm in (ARM, SLIM): x0, y0, x1, y1 = arm['back']; d.rectangle((x0 + 1, y0 + 5, x1 - 1, y0 + 7), fill='#3f566f')
        elif n == 'swing_coat':
            d.rectangle((fx0 + 1, fy0, cx - 1, fy0 + 1), fill='#c69ba2'); d.rectangle((cx + 1, fy0, fx1 - 1, fy0 + 1), fill='#c69ba2')
            for y in (fy0 + 4, fy0 + 8, hy0 + 1): d.rectangle((cx, y, cx + 1, y + 1), fill='#6e434a')
            d.line((hx0, hy1, hx1, hy1), fill=light)
        elif n == 'collared_coat':
            d.line((cx, fy0 + 2, cx, fy1), fill='#54493f')
            for y in (fy0 + 5, fy0 + 8): d.point((cx + 1, y), fill='#3a322b')
            # The soft collar round the neck, front and back.
            for (x0, y0, x1, y1) in (BODY['front'], BODY['back'], BODY['left'], BODY['right']): d.rectangle((x0, y0, x1, y0 + 2), fill=FLEECE); d.point((x0 + 1, y0 + 1), fill='#c2b8a3')
            x0, y0, x1, y1 = BODY['top']; d.rectangle((x0, y0, x1, y1), fill=FLEECE)
        elif n == 'rain_mac':
            d.line((cx - 1, fy0, cx - 1, fy1), fill='#9a7c24')
            for y in (fy0 + 3, fy0 + 6, fy0 + 9): d.line((cx - 2, y, cx + 1, y), fill='#3d3a35')
            # Where the name label was cut out of the back of the collar.
            d.rectangle((cx + (bx0 - fx0) - 1, by0, cx + (bx0 - fx0) + 1, by0 + 1), fill='#efe7cf'); d.point((cx + (bx0 - fx0) + 1, by0 + 1), fill='#a3842a')
        elif n == 'patched_jacket':
            d.line((cx, fy0, cx, fy1), fill='#3a4c66')
            for x, y, c in ((fx0 + 2, fy0 + 3, '#b8483c'), (fx1 - 2, fy0 + 2, '#d8b84a'), (fx0 + 2, fy0 + 9, '#5e8c56'), (fx1 - 2, fy0 + 10, '#e6e0cf')):
                d.rectangle((x - 1, y - 1, x + 1, y + 1), fill=c); d.point((x, y), fill='#2f2b28')
            for x, y, c in ((bx0 + 3, by0 + 4, '#7c4f86'), (bx1 - 3, by0 + 8, '#c77a3a'), ((bx0 + bx1) // 2, by0 + 11, '#5f8fae')):
                d.rectangle((x - 1, y - 1, x + 1, y + 1), fill=c)
            for arm in (ARM, SLIM): x0, y0, x1, y1 = arm['front']; d.rectangle((x0 + 1, y0 + 3, x0 + 2, y0 + 4), fill='#b8483c')
        elif n == 'duffel':
            d.line((cx, fy0, cx, fy1), fill='#263a2d')
            for y in (fy0 + 2, fy0 + 6, fy0 + 10): d.line((cx - 2, y, cx - 1, y), fill='#2a2620'); d.rectangle((cx, y, cx + 1, y), fill=HORN); d.line((cx + 2, y, cx + 3, y), fill='#2a2620')
            for x0 in (hx0 + 1, hx1 - 3): d.rectangle((x0, hy0, x0 + 2, hy0 + 2), outline='#2c4234')
            # The hood, folded down the back.
            d.rectangle((bx0 + 2, by0, bx1 - 2, by0 + 4), fill=light); d.line((bx0 + 2, by0 + 4, bx1 - 2, by0 + 4), fill=dark)
        elif n == 'long_cardigan':
            d.line((cx, fy0, cx, fy1), fill='#8e8169')
            for y in range(fy0 + 1, fy1, 3): d.point((cx + 1, y), fill='#6f5a3e')
            # Finished in another yarn: the skirt and the cuffs.
            for name, (x0, y0, x1, y1) in HEM.items():
                d.rectangle((x0, y0, x1, y1), fill=LOWER[0])
                for x in range(x0, x1 + 1, 2): d.line((x, y0, x, y1), fill=LOWER[1])
            d.line((hx0 + (cx - fx0), hy0, hx0 + (cx - fx0), hy1), fill='#5a6270')
            cuff = LOWER[0]
        for arm in (ARM, SLIM):
            for name in ('front', 'back', 'left', 'right'): x0, y0, x1, y1 = arm[name]; d.rectangle((x0, y1 - 1, x1, y1), fill=cuff)
            x0, y0, x1, y1 = arm['bottom']; d.rectangle((x0, y0, x1, y1), fill=cuff)
        sheets[n] = im
    for n in BOOTS:
        mid, dark, light, weave = CLOTH[n]
        im = Image.new('RGBA', (64, 32), (0, 0, 0, 0)); d = ImageDraw.Draw(im); rng = rooms160._rng('wardrobe_' + n)
        rooms160._cloth(d, LEG, mid, dark, light, rng, weave)
        sole = {'riding_boots': '#2a2420', 'galoshes': '#3a2420', 'plimsolls': '#efe9d6'}[n]
        for name in ('front', 'back', 'left', 'right'): x0, y0, x1, y1 = LEG[name]; d.rectangle((x0, y1 - 1, x1, y1), fill=sole)
        x0, y0, x1, y1 = LEG['bottom']; d.rectangle((x0, y0, x1, y1), fill=sole)
        fx0, fy0, fx1, fy1 = LEG['front']
        if n == 'riding_boots':
            for name in ('front', 'back', 'left', 'right'): x0, y0, x1, y1 = LEG[name]; d.line((x0, y0, x1, y0), fill=light)
            x0, y0, x1, y1 = LEG['back']; d.line((x0, y1 - 2, x0 + 2, y1 - 3), fill='#a5774f')
        elif n == 'galoshes':
            for name in ('front', 'back', 'left', 'right'): x0, y0, x1, y1 = LEG[name]; d.line((x0, y0, x1, y0), fill='#e4e0d4'); d.point((x0 + 1, y0), fill='#77746c')
        else:
            d.rectangle((fx0, fy1 - 2, fx1, fy1), fill=sole); d.line((fx0, fy1 - 2, fx1, fy1 - 2), fill='#cfc7af')
            for name in ('left', 'right'):
                x0, y0, x1, y1 = LEG[name]
                for x in range(x0, x1 + 1, 2): d.point((x, y0 + 1), fill=INK)
            for y in range(fy0, fy1 - 2): d.point(((fx0 + fx1) // 2, y), fill='#bdb6a2')
        sheets[n] = im
    sheets.update(outfits_worn())
    return sheets


# 0.4.172: the outfits' worn sheets (64x32), on the cuboids HouseReadClothes adds for them: the top's body (0,0) 8x12x4 (the
# crop top uses its upper eight rows), its short sleeve (24,0) 4 or 3 x5x4, the right leg (40,0) and the left leg (40,16)
# 4x12x4 (shorts use their upper five rows), and the cut-offs' waistband (0,16) 8x3x4 below the crop top.
OUTFIT_BODY, OUTFIT_SLEEVE, OUTFIT_SLIM = rooms160._faces(0, 0, 8, 12, 4), rooms160._faces(24, 0, 4, 5, 4), rooms160._faces(24, 0, 3, 5, 4)
OUTFIT_RIGHT, OUTFIT_LEFT, OUTFIT_WAIST = rooms160._faces(40, 0, 4, 12, 4), rooms160._faces(40, 16, 4, 12, 4), rooms160._faces(0, 16, 8, 3, 4)


def _rows(box, rows):
    """The same faces cut to the first {rows} rows of each side (a crop top's body, a pair of shorts)."""
    out = {}
    for name, (x0, y0, x1, y1) in box.items():
        out[name] = (x0, y0, x1, y1) if name in ('top', 'bottom') else (x0, y0, x1, y0 + rows - 1)
    return out


def outfits_worn():
    sheets = {}
    for n in OUTFITS:
        im = Image.new('RGBA', (64, 32), (0, 0, 0, 0)); d = ImageDraw.Draw(im); rng = rooms160._rng('wardrobe_' + n)
        top, bottoms = {'tee_jeans': (TEE, JEANS), 'crop_shorts': (CROP, CUTOFFS), 'tank_shorts': (TANK, JERSEY)}[n]
        rows = 8 if n == 'crop_shorts' else 12
        body = _rows(OUTFIT_BODY, rows)
        _jersey(d, body, *top, rng, rib=n == 'tank_shorts')
        if n != 'tank_shorts':
            for sleeve in (OUTFIT_SLEEVE, OUTFIT_SLIM): _jersey(d, sleeve, *top, rng)
            for sleeve in (OUTFIT_SLEEVE, OUTFIT_SLIM):
                for name in ('front', 'back', 'left', 'right'): x0, y0, x1, y1 = sleeve[name]; d.line((x0, y1, x1, y1), fill=top[1])
                x0, y0, x1, y1 = sleeve['bottom']; d.rectangle((x0, y0, x1, y1), fill=top[1])
        fx0, fy0, fx1, fy1 = body['front']; bx0, by0, bx1, by1 = body['back']; cx = (fx0 + fx1) // 2
        # A hem round the bottom of the top.
        for name in ('front', 'back', 'left', 'right'): x0, y0, x1, y1 = body[name]; d.line((x0, y1, x1, y1), fill=top[1])
        x0, y0, x1, y1 = body['bottom']; d.rectangle((x0, y0, x1, y1), fill=top[1])
        if n == 'tee_jeans':
            # A crew neck, and the faded print.
            d.line((cx - 1, fy0, cx + 2, fy0), fill=top[1])
            d.ellipse((cx - 2, fy0 + 3, cx + 3, fy0 + 7), outline=PRINT); d.line((cx - 1, fy0 + 6, cx + 2, fy0 + 6), fill=RUST); d.point((cx, fy0 + 4), fill=RUST)
        elif n == 'crop_shorts':
            d.line((cx - 1, fy0, cx + 2, fy0), fill=top[1]); d.point((cx, fy0 + 1), fill=top[1]); d.point((cx + 1, fy0 + 1), fill=top[1])
        else:
            # The tank's scoop neck and wide armholes: the body shows through where it is cut away; only the straps cross the shoulders.
            for x, y in ((cx - 1, fy0), (cx, fy0), (cx + 1, fy0), (cx + 2, fy0), (cx, fy0 + 1), (cx + 1, fy0 + 1), (fx0, fy0), (fx1, fy0), (fx0, fy0 + 1), (fx1, fy0 + 1)):
                d.point((x, y), fill=(0, 0, 0, 0))
            for x in range(bx0 + 2, bx1 - 1): d.point((x, by0), fill=(0, 0, 0, 0))
            for side in ('left', 'right'):
                x0, y0, x1, y1 = body[side]; d.rectangle((x0, y0, x1, y0 + 2), fill=(0, 0, 0, 0))
            x0, y0, x1, y1 = body['top']
            for x in range(x0, x1 + 1):
                if x not in (x0 + 1, x0 + 2, x1 - 2, x1 - 1): d.line((x, y0, x, y1), fill=(0, 0, 0, 0))
        # The bottoms, each leg its own: jeans to the ankle, shorts to above the knee.
        long = n == 'tee_jeans'; legs = [OUTFIT_RIGHT, OUTFIT_LEFT] if long else [_rows(OUTFIT_RIGHT, 5), _rows(OUTFIT_LEFT, 5)]
        for i, leg in enumerate(legs):
            if n == 'tank_shorts': _jersey(d, leg, *bottoms, rng)
            else: rooms160._cloth(d, leg, *bottoms, rng, 'twill')
            lx0, ly0, lx1, ly1 = leg['front']; ox0, oy0, ox1, oy1 = leg['right' if i == 0 else 'left']
            d.line((ox0 + 1, oy0, ox0 + 1, oy1), fill=bottoms[1])  # the outside seam
            if long:
                x0, y0, x1, y1 = leg['bottom']; d.rectangle((x0, y0, x1, y1), fill=bottoms[1])
                for name in ('front', 'back', 'left', 'right'): x0, y0, x1, y1 = leg[name]; d.line((x0, y1, x1, y1), fill=bottoms[1])
                if i == 0:
                    # Worn through at one knee: pale threads across a hole.
                    for x in range(lx0, lx1 + 1): d.point((x, ly0 + 6), fill=THREAD); d.point((x, ly0 + 7), fill=THREAD if x % 2 else bottoms[2])
            elif n == 'crop_shorts':
                for name in ('front', 'back', 'left', 'right'):
                    x0, y0, x1, y1 = leg[name]
                    for x in range(x0, x1 + 1): d.point((x, y1), fill=THREAD if x % 2 else bottoms[2])
            else:
                for name in ('front', 'back', 'left', 'right'): x0, y0, x1, y1 = leg[name]; d.line((x0, y1, x1, y1), fill=bottoms[1])
            if not long and i == 0 and n == 'tank_shorts':
                d.line((lx1, ly0, lx1, ly0 + 2), fill='#e9e6dc')  # the drawstring's end
        if n == 'crop_shorts':
            # The cut-offs' waistband below the crop top: belt loops and a brass button.
            rooms160._cloth(d, OUTFIT_WAIST, *bottoms, rng, 'twill')
            wx0, wy0, wx1, wy1 = OUTFIT_WAIST['front']; d.point(((wx0 + wx1) // 2, wy0 + 1), fill=BUTTON)
            for name in ('front', 'back'):
                x0, y0, x1, y1 = OUTFIT_WAIST[name]
                for x in (x0 + 1, x1 - 1): d.line((x, y0, x, y1), fill=bottoms[1])
        sheets[n] = im
    return sheets
