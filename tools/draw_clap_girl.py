"""0.4.656, the owner: "Can you improve the texture for the ghost girl from hide and clap?"

The girl in the wardrobe (Hide and clap, docs/CLAP_AND_SEEK.md): a child in a cotton nightdress, barefoot, long hair. She was
painted by tools/generate_clap_assets.py with a grain finer than her model's pixels, a face of two slits and a line, arms and
legs all one tone, and a nightdress whose cloth was later swapped for a tile of the cast's material sheet (tools/
generate_cast_skins.py) that read as sand. She is painted here by herself, as the House's other people are: two pixels to a
model unit, doubled to her 512-pixel sheet, every painted pixel a 2 by 2 square of it.

- Her face: narrow, framed by long dark hair that falls straight past it on both sides, with bangs cut straight across her
  forehead; dark eyes, a little shadow under them; a small mouth a little open. Nothing on it is drawn as a wound.
- Her hair: lighter strands through the dark; from the side it hides her cheek, and behind her it falls past her shoulders
  onto the back of her nightdress, its ends uneven.
- Her nightdress: cotton gone dull with keeping, gathered under a smocked yoke with a little lace at the neck and buttons down
  the back, soft folds to the hem, the hem and the skirt above it dirty from the floor of a wardrobe; short puffed sleeves.
- Her arms and legs: pale, cooler than the cloth, a little shadow at the joints, dirt at the fingertips, the shins and the feet;
  her soles black.

Her model gains a neck (0.4.656): her head stood a pixel above her shoulders, and her hair showed through the gap under her
chin. The neck is the model's last piece, so no other piece's place on the sheet moves; tools/generate_clap_assets.py has it.
Every face of every piece is painted; nothing is painted outside the pieces' nets, which are filled to their corners as the old
sheet's were.
"""
from pathlib import Path
import json
import random
from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
SPEC = ROOT / 'art/clap_ghost_girl.model.json'
S = 2            # painted pixels to a model unit
LAYOUT = 128     # the model's own sheet, in units

# Her skin is cooler and a shade darker than her nightdress, so that her arms do not read as more of it.
SKIN = (196, 195, 201)
SKIN_LIGHT = (211, 210, 215)
SKIN_SHADE = (172, 171, 180)
SKIN_DEEP = (144, 142, 154)
SOCKET = (158, 156, 166)
EYE = (20, 18, 24)
MOUTH = (86, 66, 70)
HAIR = (25, 23, 29)
HAIR_MID = (37, 35, 43)
HAIR_LIGHT = (55, 55, 67)
COTTON = (217, 209, 190)
COTTON_LIGHT = (229, 223, 206)
COTTON_SHADE = (192, 182, 162)
COTTON_DEEP = (164, 153, 134)
STITCH = (178, 166, 144)
LACE = (238, 234, 222)
BUTTON = (240, 237, 228)
GRIME = (156, 146, 128)
GRIME_DEEP = (126, 116, 100)
DIRT = (104, 92, 80)
SOLE = (70, 62, 56)

LEGEND = {'H': HAIR, 'h': HAIR_MID, 'k': HAIR_LIGHT, 'S': SKIN, 'l': SKIN_LIGHT, 's': SKIN_SHADE, 'd': SKIN_DEEP,
          'o': SOCKET, 'E': EYE, 'm': MOUTH}

# Her face, as seen from in front of her (left of the map on the viewer's left). The top two rows lie behind her fringe.
FACE = ['HHHHHkHHHHhH',
        'HhHHHHHHkHHH',
        'HHkHHhHHHkHH',
        'HHHhHHHhHHHH',
        'HHdlSSSSSdHH',
        'HHSEESSEESHH',
        'HhSEESSEEShH',
        'HHsooSSoosHH',
        'HHSSSSsSSSHH',
        'HHdlSSSSSdHH',
        'HHHdSmmSdHHH',
        'HHHHdSSdHHHH']


def net(u, v, w, h, d):
    """A piece's six faces on the sheet, in units: right is the piece's -x side, its front at the face's right edge."""
    return {'top': (u + d, v, u + d + w, v + d), 'bottom': (u + d + w, v, u + d + 2 * w, v + d),
            'right': (u, v + d, u + d, v + d + h), 'front': (u + d, v + d, u + d + w, v + d + h),
            'left': (u + d + w, v + d, u + 2 * d + w, v + d + h), 'back': (u + 2 * d + w, v + d, u + 2 * d + 2 * w, v + d + h)}


class Sheet:
    def __init__(self):
        self.im = Image.new('RGBA', (LAYOUT * S, LAYOUT * S), (0, 0, 0, 0))

    def fill(self, box, colour):
        x0, y0, x1, y1 = (round(c * S) for c in box)
        for y in range(y0, y1):
            for x in range(x0, x1):
                self.im.putpixel((x, y), colour + (255,))

    def face(self, box):
        """A face as a painting surface: its pixel origin and size."""
        x0, y0, x1, y1 = (round(c * S) for c in box)
        return Face(self.im, x0, y0, x1 - x0, y1 - y0)


class Face:
    def __init__(self, im, x, y, w, h):
        self.im, self.x, self.y, self.w, self.h = im, x, y, w, h

    def put(self, x, y, colour):
        if 0 <= x < self.w and 0 <= y < self.h:
            self.im.putpixel((self.x + x, self.y + y), colour + (255,))

    def all(self, colour):
        for y in range(self.h):
            for x in range(self.w):
                self.put(x, y, colour)

    def map(self, rows):
        for y, row in enumerate(rows):
            for x, key in enumerate(row):
                self.put(x, y, LEGEND[key])


def hair(f, rnd, ends=False, across=False):
    """Long straight hair: lighter strands through the dark, uneven ends at the bottom when they show."""
    f.all(HAIR)
    if across:
        # Seen from above: strands scattered through the dark, no parting.
        for y in range(f.h):
            for x in range(f.w):
                if rnd.random() < .22:
                    f.put(x, y, HAIR_MID if rnd.random() < .7 else HAIR_LIGHT)
        return
    for x in range(f.w):
        tone = rnd.choice((HAIR, HAIR, HAIR_MID, HAIR_MID, HAIR_LIGHT))
        start, length = rnd.randrange(f.h), 2 + rnd.randrange(max(1, f.h // 2))
        for y in range(start, min(f.h, start + length)):
            f.put(x, y, tone)
    if ends:
        for x in range(f.w):
            if rnd.random() < .5:
                f.put(x, f.h - 1, HAIR_MID)


def cotton(f, rnd, folds=True, hem=False, yoke=False, grime_from=None):
    """The nightdress's cloth: gathered folds running down, the yoke's smocking, grime toward a dirty hem."""
    f.all(COTTON)
    if folds:
        for x in range(1, f.w, 3):
            for y in range(4 if yoke else 0, f.h):
                f.put(x, y, COTTON_SHADE)
        for x in range(0, f.w, 3):
            for y in range(4 if yoke else 0, f.h, 1):
                if rnd.random() < .18:
                    f.put(x, y, COTTON_LIGHT)
    if yoke:
        for y in (1, 3):
            for x in range(y % 2, f.w, 2):
                f.put(x, y, STITCH)
        for x in range(f.w):
            f.put(x, 4, COTTON_SHADE)
    if grime_from is not None:
        for y in range(grime_from, f.h):
            depth = (y - grime_from + 1) / (f.h - grime_from + 1)
            for x in range(f.w):
                if rnd.random() < .25 + .45 * depth:
                    f.put(x, y, GRIME if rnd.random() < .7 else GRIME_DEEP)
    if hem:
        for x in range(f.w):
            f.put(x, f.h - 2, COTTON_DEEP)
            f.put(x, f.h - 1, GRIME_DEEP if rnd.random() < .6 else DIRT)


def limb(f, rnd, dirt_from, tips=False):
    """Pale skin, a little shadow at the sides, dirt gathering toward the end of the limb."""
    f.all(SKIN)
    for y in range(f.h):
        f.put(f.w - 1, y, SKIN_SHADE)
    for y in range(dirt_from, f.h):
        for x in range(f.w):
            if rnd.random() < .3 + .5 * (y - dirt_from + 1) / (f.h - dirt_from):
                f.put(x, y, GRIME if rnd.random() < .6 else SKIN_DEEP)
    if tips:
        for x in range(f.w):
            f.put(x, f.h - 1, DIRT)


def paint():
    spec = json.loads(SPEC.read_text())
    sheet = Sheet()
    rnd = random.Random(4656)
    for part in spec['parts']:
        for cube in part['cubes']:
            (u, v), (w, h, d) = cube['uv'], cube['size']
            name = cube['name']
            faces = net(u, v, w, h, d)
            base = {'cloth': COTTON, 'face': SKIN, 'hair': HAIR, 'skin': SKIN, 'foot': SKIN}[cube['material']]
            # The whole of each piece's net, its corners too, as the old sheet's was: nothing outside them.
            sheet.fill((u, v, u + 2 * (w + d), v + d + h), base)
            F = {k: sheet.face(box) for k, box in faces.items()}
            if name == 'face':
                F['front'].map(FACE)
                for side in ('right', 'left'):
                    hair(F[side], rnd)
                hair(F['back'], rnd)
                hair(F['top'], rnd, across=True)
                f = F['bottom']
                f.all(SKIN_DEEP)
                for y in range(f.h // 3):
                    for x in range(f.w):
                        f.put(x, y, HAIR)
            elif name == 'dark_crown':
                hair(F['top'], rnd, across=True)
                for k in ('front', 'right', 'left', 'back', 'bottom'):
                    hair(F[k], rnd)
            elif name in ('dark_back', 'left_hair', 'right_hair'):
                for k in ('front', 'right', 'left', 'back', 'top'):
                    hair(F[k], rnd, ends=k in ('back', 'right', 'left', 'front'))
                F['bottom'].all(HAIR_MID)
            elif name == 'cotton_nightdress':
                for k in ('front', 'back', 'right', 'left'):
                    cotton(F[k], rnd, yoke=True, hem=True, grime_from=F[k].h - 6)
                # The neckline, a little lace round it, and the placket's buttons: down the front to the yoke, down the back.
                f = F['front']
                for x in range(5, 9):
                    f.put(x, 0, SKIN_SHADE)
                for x in (4, 9):
                    f.put(x, 0, LACE)
                for x in range(5, 9):
                    f.put(x, 1, LACE)
                for y in (2, 4):
                    f.put(7, y, BUTTON)
                f = F['back']
                for y in range(0, 12):
                    f.put(6, y, COTTON_SHADE)
                for y in (2, 5, 8, 11):
                    f.put(7, y, BUTTON)
                # Her hair, down her back from under the long hair of her head, over the top buttons: uneven at its ends.
                for x in range(2, f.w - 2):
                    end = 5 + (2 if x in (5, 9) else 1 if x in (3, 6, 10) else 0) - (1 if x in (2, f.w - 3) else 0)
                    for y in range(end):
                        f.put(x, y, HAIR)
                    tone = HAIR_MID if x % 3 else HAIR_LIGHT
                    for y in range(rnd.randrange(2), end - 1, 2 + x % 2):
                        f.put(x, y, tone)
                    f.put(x, end - 1, HAIR_MID)
                # A smear on the front of the skirt, low on her left, where she crouched.
                f = F['front']
                for x, y in ((9, 13), (10, 13), (10, 14), (11, 14), (9, 15)):
                    f.put(x, y, GRIME)
                # The shoulders: the cloth, and her neck's root in the middle with the lace round it.
                f = F['top']
                f.all(COTTON)
                for x in range(f.w):
                    f.put(x, 0, COTTON_SHADE)
                for x in range(4, 10):
                    for y in range(2, 8):
                        f.put(x, y, LACE)
                for x in range(5, 9):
                    for y in range(3, 7):
                        f.put(x, y, SKIN_DEEP)
                F['bottom'].all(COTTON_DEEP)
            elif name.endswith('_sleeve'):
                for k in ('front', 'back', 'right', 'left'):
                    f = F[k]
                    f.all(COTTON)
                    for y in range(f.h):
                        f.put(f.w - 1, y, COTTON_SHADE)
                    f.put(0, 1, COTTON_LIGHT)
                    for x in range(f.w):
                        f.put(x, f.h - 2, COTTON_SHADE)
                        f.put(x, f.h - 1, LACE)
                F['top'].all(COTTON_LIGHT)
                F['bottom'].all(COTTON_DEEP)
            elif name.endswith('_hand'):
                for k in ('front', 'back', 'right', 'left'):
                    limb(F[k], rnd, dirt_from=F[k].h - 3, tips=True)
                    f = F[k]
                    # The knuckles, and the fingers' lines on the back of the hand.
                    for x in range(f.w):
                        f.put(x, f.h - 4, SKIN_SHADE)
                    if k in ('front', 'back'):
                        f.put(1, f.h - 2, SKIN_DEEP)
                        f.put(1, f.h - 3, SKIN_DEEP)
                F['top'].all(SKIN_SHADE)
                F['bottom'].all(DIRT)
            elif name.endswith('_ankle'):
                for k in ('front', 'back', 'right', 'left'):
                    limb(F[k], rnd, dirt_from=F[k].h - 4)
                    F[k].put(0, 1, SKIN_SHADE)
                    F[k].put(1, 1, SKIN_SHADE)
                F['top'].all(SKIN_SHADE)
                F['bottom'].all(DIRT)
            elif name.endswith('_bare_foot'):
                f = F['top']
                f.all(SKIN)
                for y in range(f.h):
                    for x in range(f.w):
                        if rnd.random() < .45:
                            f.put(x, y, GRIME)
                for y in range(f.h - 2, f.h):
                    f.put(1, y, SKIN_DEEP)
                    f.put(3, y, SKIN_DEEP)
                f = F['front']
                f.all(SKIN_SHADE)
                f.put(1, 0, SKIN_DEEP)
                f.put(3, 0, SKIN_DEEP)
                for x in range(f.w):
                    f.put(x, f.h - 1, DIRT)
                for k in ('right', 'left', 'back'):
                    f = F[k]
                    f.all(GRIME)
                    for x in range(f.w):
                        f.put(x, f.h - 1, DIRT)
                F['bottom'].all(SOLE)
            elif name == 'neck':
                for k in ('front', 'back', 'right', 'left'):
                    F[k].all(SKIN_SHADE)
                    for x in range(F[k].w):
                        F[k].put(x, 0, SKIN_DEEP)
                F['top'].all(SKIN_DEEP)
                F['bottom'].all(SKIN_DEEP)
            else:
                raise ValueError(f'a piece of the girl with no painting: {name}')
    return sheet.im.resize((LAYOUT * S * 2, LAYOUT * S * 2), Image.Resampling.NEAREST)


if __name__ == '__main__':
    out = ROOT / 'src/main/resources/assets/the_oldest_house/textures/entity/clap_ghost_girl.png'
    paint().save(out, optimize=True)
    print(f'Painted the girl from the wardrobe: {out.relative_to(ROOT)}')
