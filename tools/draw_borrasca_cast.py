#!/usr/bin/env python3
"""Borrasca's cast (0.4.121), drawn as full 64x64 skins on the standard humanoid layout.

Kimber at eleven in her teal windbreaker, Kyle in his cap and striped shirt, the Sheriff in his tan uniform and campaign
hat, Prescott in flannel and a quilted vest under his company cap, and a refinery worker in coveralls, hi-vis vest and
hard hat. Every pixel is placed from a small palette with one-pixel shading and a little noise, as vanilla's skins are;
nothing is sampled. The old 32x32 cast atlas stays for the machine. Run with --check to confirm the committed files.
"""
import argparse
import random
import sys
from pathlib import Path

from PIL import Image

ROOT = Path(__file__).resolve().parents[1] / 'src/main/resources/assets/the_oldest_house/textures/entity'


def clamp(c):
    return tuple(max(0, min(255, round(v))) for v in c)


def mul(c, f):
    return clamp(tuple(v * f for v in c[:3]))


def net(u, v, w, h, d):
    """The six faces of one box on a skin: name -> (x, y, width, height)."""
    return {'top': (u + d, v, w, d), 'bottom': (u + d + w, v, w, d), 'right': (u, v + d, d, h), 'front': (u + d, v + d, w, h),
            'left': (u + d + w, v + d, d, h), 'back': (u + d + w + d, v + d, w, h)}


HEAD, HAT = net(0, 0, 8, 8, 8), net(32, 0, 8, 8, 8)
BODY, JACKET = net(16, 16, 8, 12, 4), net(16, 32, 8, 12, 4)
RARM, RSLEEVE = net(40, 16, 4, 12, 4), net(40, 32, 4, 12, 4)
LARM, LSLEEVE = net(32, 48, 4, 12, 4), net(48, 48, 4, 12, 4)
RLEG, RPANTS = net(0, 16, 4, 12, 4), net(0, 32, 4, 12, 4)
LLEG, LPANTS = net(16, 48, 4, 12, 4), net(0, 48, 4, 12, 4)
SIDES = ('right', 'front', 'left', 'back')


class Skin:
    def __init__(self, seed):
        self.img = Image.new('RGBA', (64, 64), (0, 0, 0, 0))
        self.rng = random.Random(seed)

    def px(self, x, y, c, a=255):
        self.img.putpixel((x, y), clamp(c) + (a,))

    def get(self, x, y):
        return self.img.getpixel((x, y))

    def face(self, part, name, c, noise=.04, shade=0.0):
        """Fill one face, darker toward its bottom by `shade`."""
        x0, y0, w, h = part[name]
        for y in range(h):
            for x in range(w):
                f = 1 + self.rng.uniform(-noise, noise) - shade * (y / max(1, h - 1))
                self.px(x0 + x, y0 + y, mul(c, f))

    def box(self, part, c, noise=.04, shade=.1, faces=SIDES + ('top', 'bottom')):
        for name in faces:
            self.face(part, name, mul(c, .82) if name == 'bottom' else mul(c, 1.06) if name == 'top' else c, noise, 0 if name in ('top', 'bottom') else shade)

    def rows(self, part, y0, y1, c, faces=SIDES, noise=.03):
        """A band across the given rows of every side face (a belt, a cuff, a shoe)."""
        for name in faces:
            x, y, w, h = part[name]
            for yy in range(max(0, y0), min(h, y1 + 1)):
                for xx in range(w):
                    self.px(x + xx, y + yy, mul(c, 1 + self.rng.uniform(-noise, noise)))

    def at(self, part, name, x, y, c, a=255):
        fx, fy, _, _ = part[name]
        self.px(fx + x, fy + y, c, a)

    def clear(self, part, faces=SIDES + ('top', 'bottom')):
        for name in faces:
            x0, y0, w, h = part[name]
            for y in range(h):
                for x in range(w):
                    self.img.putpixel((x0 + x, y0 + y), (0, 0, 0, 0))


def head(s, skin, hair, eyes, brows=None, fringe=2, long_hair=0, age=0, mouth=(150, 92, 80)):
    """A head: skin all round, hair on top and down the back and sides, and a face with eyes, nose and mouth."""
    s.box(HEAD, skin, .025, .06)
    s.face(HEAD, 'top', hair, .06)
    for name in ('right', 'left', 'back'):
        x0, y0, w, h = HEAD[name]
        depth = h if name == 'back' and long_hair else (3 + long_hair if name != 'back' else 4 + long_hair)
        for y in range(min(h, depth)):
            for x in range(w):
                if name != 'back' and y >= 3 and ((name == 'right' and x < 3) or (name == 'left' and x > 4)) and not long_hair:
                    continue
                s.px(x0 + x, y0 + y, mul(hair, 1 + s.rng.uniform(-.07, .07) - .015 * y))
    for x in range(8):
        for y in range(fringe):
            s.at(HEAD, 'front', x, y, mul(hair, 1 + s.rng.uniform(-.06, .06)))
    if long_hair:
        for y in range(fringe, 6 + min(2, long_hair)):
            s.at(HEAD, 'front', 0, y, mul(hair, .92)); s.at(HEAD, 'front', 7, y, mul(hair, .92))
    brows = brows or mul(hair, .8)
    s.at(HEAD, 'front', 1, 3, brows); s.at(HEAD, 'front', 2, 3, brows); s.at(HEAD, 'front', 5, 3, brows); s.at(HEAD, 'front', 6, 3, brows)
    for x, iris in ((1, 2), (6, 5)):
        s.at(HEAD, 'front', x, 4, (238, 234, 226)); s.at(HEAD, 'front', iris, 4, eyes)
    s.at(HEAD, 'front', 3, 5, mul(skin, .86)); s.at(HEAD, 'front', 4, 5, mul(skin, .9))
    s.at(HEAD, 'front', 3, 6, mouth); s.at(HEAD, 'front', 4, 6, mouth)
    if age:
        for x, y in ((1, 5), (6, 5)):
            s.at(HEAD, 'front', x, y, mul(skin, .9))
    return s


def shoes(s, c, sole=(40, 38, 36), rows=(10, 11)):
    for leg in (RLEG, LLEG):
        s.rows(leg, rows[0], rows[1], c)
        s.rows(leg, 11, 11, sole)
        s.face(leg, 'bottom', sole, .03)


def hands(s, skin, rows=(10, 11)):
    for arm in (RARM, LARM):
        s.rows(arm, rows[0], rows[1], skin)
        s.face(arm, 'bottom', skin, .03)


# ------------------------------------------------------------------------------------------------ the cast
def kimber():
    """Kimber at eleven: auburn hair in a ponytail, freckles, the teal windbreaker with its white zip, jeans, sneakers."""
    s = Skin(9101)
    skin, hair = (236, 196, 166), (128, 74, 44)
    head(s, skin, hair, (72, 136, 88), fringe=2, long_hair=2, mouth=(186, 104, 96))
    for x, y in ((1, 5), (2, 6), (5, 6), (6, 5)):
        s.at(HEAD, 'front', x, y, (204, 140, 104))
    # The ponytail down the back on the outer layer, held by a red band.
    for y in range(8):
        for x in range(2, 6):
            s.at(HAT, 'back', x, y, mul(hair, .95 + .1 * ((x + y) % 2)))
    for x in range(2, 6):
        s.at(HAT, 'back', x, 1, (196, 52, 52))
    teal = (48, 148, 150)
    s.box(BODY, teal, .05, .1)
    for y in range(12):
        s.at(BODY, 'front', 3, y, (236, 240, 236)); s.at(BODY, 'front', 4, y, mul(teal, .8))
    for x in range(8):
        s.at(BODY, 'front', x, 0, mul(teal, 1.18))
    s.at(BODY, 'front', 1, 6, mul(teal, .7)); s.at(BODY, 'front', 6, 6, mul(teal, .7))
    s.rows(BODY, 11, 11, mul(teal, .78))
    for arm in (RARM, LARM):
        s.box(arm, teal, .05, .12)
        s.rows(arm, 9, 9, mul(teal, .78))
    hands(s, skin)
    jeans = (64, 88, 134)
    for leg in (RLEG, LLEG):
        s.box(leg, jeans, .06, .12)
    s.at(RLEG, 'front', 1, 5, mul(jeans, .78)); s.at(LLEG, 'front', 2, 6, mul(jeans, .78))
    shoes(s, (232, 232, 226), (196, 54, 54))
    for leg in (RLEG, LLEG):
        s.rows(leg, 10, 10, (232, 232, 226))
        s.at(leg, 'right', 1, 10, (196, 54, 54)); s.at(leg, 'left', 2, 10, (196, 54, 54))
    return s.img


def kyle():
    """Kyle at twelve: dirty-blond hair under a red ball cap, a navy-and-white striped shirt, khaki shorts, scuffed sneakers."""
    s = Skin(9102)
    skin, hair = (226, 180, 146), (178, 142, 82)
    head(s, skin, hair, (82, 104, 150), fringe=1, mouth=(166, 102, 90))
    red = (182, 44, 42)
    s.face(HAT, 'top', red, .05)
    for name in SIDES:
        x0, y0, w, h = HAT[name]
        for y in range(3):
            for x in range(w):
                s.px(x0 + x, y0 + y, mul(red, 1 - .05 * y + s.rng.uniform(-.04, .04)))
    for x in range(8):
        s.at(HAT, 'front', x, 3, mul(red, .7))
    s.at(HAT, 'front', 3, 1, (236, 236, 230)); s.at(HAT, 'front', 4, 1, (236, 236, 230))
    navy, white = (40, 52, 96), (232, 232, 226)
    s.box(BODY, white, .03, .06)
    for name in SIDES:
        x0, y0, w, h = BODY[name]
        for y in range(1, h, 3):
            for x in range(w):
                s.px(x0 + x, y0 + y, mul(navy, 1 + s.rng.uniform(-.05, .05)))
    for arm in (RARM, LARM):
        s.box(arm, skin, .02, .06)
        s.rows(arm, 0, 3, white)
        s.rows(arm, 1, 1, navy)
    s.face(RARM, 'top', white); s.face(LARM, 'top', white)
    khaki = (184, 160, 112)
    for leg in (RLEG, LLEG):
        s.box(leg, skin, .02, .05)
        s.rows(leg, 0, 5, khaki)
        s.rows(leg, 8, 9, (236, 236, 232))
        s.face(leg, 'top', khaki)
    shoes(s, (120, 120, 124), (40, 40, 44))
    return s.img


def sheriff():
    """The Sheriff: a weathered face and grey moustache, tan uniform shirt with epaulettes and a star, dark trousers, duty
    belt, black boots, and the campaign hat with its brown band."""
    s = Skin(9103)
    skin, hair = (210, 164, 128), (112, 104, 96)
    head(s, skin, hair, (96, 72, 52), brows=(92, 84, 76), fringe=1, age=1, mouth=(150, 98, 84))
    for x in range(1, 7):
        s.at(HEAD, 'front', x, 6, (150, 142, 132) if x in (1, 6) else (138, 130, 120))
    s.at(HEAD, 'front', 3, 7, mul(skin, .92)); s.at(HEAD, 'front', 4, 7, mul(skin, .92))
    felt, band = (164, 128, 78), (86, 58, 36)
    s.face(HAT, 'top', felt, .04)
    for name in SIDES:
        x0, y0, w, h = HAT[name]
        for y in range(4):
            for x in range(w):
                s.px(x0 + x, y0 + y, mul(felt, 1 - .04 * y + s.rng.uniform(-.03, .03)))
        for x in range(w):
            s.px(x0 + x, y0 + 2, band)
    for x in range(8):
        s.at(HAT, 'top', x, 3, mul(felt, .85)); s.at(HAT, 'top', x, 4, mul(felt, .85))
    tan = (196, 170, 116)
    s.box(BODY, tan, .04, .08)
    for name in ('right', 'left'):
        x0, y0, _, _ = BODY[name]
    for x in (0, 1, 6, 7):
        s.at(BODY, 'top', x, 1, (90, 66, 44))
    for y in range(12):
        s.at(BODY, 'front', 4, y, mul(tan, .86))
    for x, y in ((1, 3), (2, 3), (5, 3), (6, 3)):
        s.at(BODY, 'front', x, y, mul(tan, .8))
    s.at(BODY, 'front', 2, 4, (226, 188, 70)); s.at(BODY, 'front', 1, 4, (196, 160, 52)); s.at(BODY, 'front', 2, 5, (196, 160, 52))
    s.at(BODY, 'front', 5, 4, (40, 40, 40)); s.at(BODY, 'front', 6, 4, (226, 226, 220))
    s.rows(BODY, 10, 11, (34, 32, 30))
    s.at(BODY, 'front', 3, 10, (196, 160, 52)); s.at(BODY, 'front', 4, 10, (196, 160, 52))
    for arm in (RARM, LARM):
        s.box(arm, tan, .04, .1)
        s.rows(arm, 0, 0, (90, 66, 44), faces=('right', 'left', 'front', 'back'))
    s.at(RARM, 'right', 1, 2, (120, 92, 56)); s.at(RARM, 'right', 2, 2, (180, 150, 70)); s.at(LARM, 'left', 1, 2, (120, 92, 56)); s.at(LARM, 'left', 2, 2, (180, 150, 70))
    hands(s, skin)
    trousers = (80, 60, 42)
    for leg in (RLEG, LLEG):
        s.box(leg, trousers, .04, .1)
        s.at(leg, 'right', 1, 4, mul(trousers, 1.25)); s.at(leg, 'left', 2, 4, mul(trousers, 1.25))
    shoes(s, (28, 28, 30), (18, 18, 20), rows=(9, 11))
    return s.img


def prescott():
    """Prescott: ruddy, grey hair thinning on top, red-and-black flannel under a quilted brown vest with the company patch,
    khaki work trousers, brown boots, and a green trucker cap with a white P."""
    s = Skin(9104)
    skin, hair = (222, 162, 134), (176, 172, 166)
    head(s, skin, hair, (84, 98, 110), brows=(150, 146, 140), fringe=0, age=1, mouth=(162, 96, 86))
    s.at(HEAD, 'front', 2, 5, (220, 140, 120)); s.at(HEAD, 'front', 5, 5, (220, 140, 120))
    green = (52, 92, 60)
    s.face(HAT, 'top', green, .05)
    for name in SIDES:
        x0, y0, w, h = HAT[name]
        for y in range(3):
            for x in range(w):
                s.px(x0 + x, y0 + y, mul(green, 1 - .04 * y + s.rng.uniform(-.04, .04)))
    # A white patch on the front with the company's P.
    for x in range(2, 6):
        for y in range(0, 3):
            s.at(HAT, 'front', x, y, (234, 232, 222))
    for x, y in ((3, 0), (4, 0), (3, 1), (4, 1), (3, 2)):
        s.at(HAT, 'front', x, y, (40, 76, 48))
    for x in range(8):
        s.at(HAT, 'front', x, 3, mul(green, .75))
    red, black = (170, 44, 40), (40, 30, 30)
    s.box(BODY, red, .03, .06)
    for name in SIDES + ('top',):
        x0, y0, w, h = BODY[name]
        for y in range(h):
            for x in range(w):
                if x % 3 == 1 or y % 3 == 1:
                    s.px(x0 + x, y0 + y, mul(black if (x % 3 == 1 and y % 3 == 1) else (110, 34, 32), 1 + s.rng.uniform(-.04, .04)))
    # The quilted vest over the flannel (the cast model has no jacket layer, so it is the body's own).
    vest = (120, 84, 50)
    for name in ('right', 'left', 'back'):
        x0, y0, w, h = BODY[name]
        for y in range(h):
            for x in range(w):
                s.px(x0 + x, y0 + y, mul(vest, 1 + s.rng.uniform(-.05, .05) - (.12 if y % 3 == 0 else 0)))
    x0, y0, w, h = BODY['front']
    for y in range(h):
        for x in range(w):
            if x in (0, 1, 2, 5, 6, 7):
                s.px(x0 + x, y0 + y, mul(vest, 1 + s.rng.uniform(-.05, .05) - (.12 if y % 3 == 0 else 0)))
    s.at(BODY, 'front', 1, 3, (232, 226, 200)); s.at(BODY, 'front', 2, 3, (52, 92, 60))
    s.face(BODY, 'top', vest, .04)
    for arm in (RARM, LARM):
        s.box(arm, red, .03, .08)
        x0, y0, w, h = arm['front']
        for name in SIDES:
            ax, ay, aw, ah = arm[name]
            for y in range(1, ah, 3):
                for x in range(aw):
                    s.px(ax + x, ay + y, mul((110, 34, 32), 1 + s.rng.uniform(-.04, .04)))
    hands(s, skin)
    khaki = (156, 140, 104)
    for leg in (RLEG, LLEG):
        s.box(leg, khaki, .04, .1)
    shoes(s, (100, 66, 40), (52, 36, 24), rows=(10, 11))
    return s.img


def worker():
    """A refinery worker: grey coveralls, an orange hi-vis vest with silver stripes, tan gloves, a yellow hard hat,
    safety glasses, black boots."""
    s = Skin(9105)
    skin, hair = (200, 152, 118), (70, 56, 44)
    head(s, skin, hair, (60, 60, 64), fringe=1, mouth=(144, 92, 80))
    for x in range(1, 7):
        s.at(HEAD, 'front', x, 4, (54, 60, 66) if x not in (3, 4) else (90, 96, 100))
    hat = (226, 188, 42)
    s.face(HAT, 'top', hat, .04)
    for name in SIDES:
        x0, y0, w, h = HAT[name]
        for y in range(3):
            for x in range(w):
                s.px(x0 + x, y0 + y, mul(hat, 1 - .05 * y + s.rng.uniform(-.03, .03)))
        for x in range(w):
            s.px(x0 + x, y0 + 3, mul(hat, .72))
    for x in range(3, 5):
        s.at(HAT, 'top', x, 0, mul(hat, 1.12)); s.at(HAT, 'top', x, 7, mul(hat, 1.12))
    grey = (112, 116, 120)
    s.box(BODY, grey, .05, .08)
    orange, silver = (232, 112, 36), (206, 210, 214)
    for name in SIDES:
        x0, y0, w, h = BODY[name]
        for y in range(h - 2):
            for x in range(w):
                if name == 'front' and x in (3, 4):
                    continue
                s.px(x0 + x, y0 + y, mul(orange, 1 + s.rng.uniform(-.04, .04)))
        for x in range(w):
            if not (name == 'front' and x in (3, 4)):
                s.px(x0 + x, y0 + 7, silver)
                s.px(x0 + x, y0 + 8, mul(silver, .85))
    for arm in (RARM, LARM):
        s.box(arm, grey, .05, .1)
    hands(s, (176, 146, 96), rows=(9, 11))
    for leg in (RLEG, LLEG):
        s.box(leg, grey, .05, .1)
        s.at(leg, 'front', 1, 5, mul(grey, .8)); s.at(leg, 'front', 2, 5, mul(grey, .8))
    shoes(s, (30, 30, 32), (16, 16, 18), rows=(9, 11))
    return s.img


SKINS = {'borrasca_kimber': kimber, 'borrasca_kyle': kyle, 'borrasca_sheriff': sheriff, 'borrasca_prescott': prescott, 'borrasca_worker': worker}


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument('--check', action='store_true')
    args = ap.parse_args()
    bad = []
    for name, fn in SKINS.items():
        img, path = fn(), ROOT / f'{name}.png'
        if args.check:
            if not path.exists():
                bad.append(f'missing {path}')
                continue
            with Image.open(path) as saved:
                if saved.size != img.size or saved.convert('RGBA').tobytes() != img.tobytes():
                    bad.append(f'differs {path}')
        else:
            img.save(path)
    if bad:
        print('\n'.join(bad))
        sys.exit(1)
    print(f'{"Verified" if args.check else "Drew"} {len(SKINS)} Borrasca cast skins.')


if __name__ == '__main__':
    main()
