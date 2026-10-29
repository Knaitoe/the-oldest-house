"""0.4.171, the owner: improve the bent-neck woman's texture.

The tunnel's stooping giant, painted from her text: "smelling like wet hair and butcher paper ... Her neck isn't broken. She
isn't grieving. The concrete ceiling is eight feet high, and she's just stooping to get in." So no bruises and no wound: long
wet black hair with a sheen, clinging over the shoulders and down the back; a long waxy face, empty eyes (pale and clouded, the eyes of the
dead, no pupils) and a thin, patient mouth, one strand stuck across it; a long pale nightgown, white as butcher paper, sprigged with faded
flowers and darkened where her hair drips on it; long bare arms with long dirty fingers; bare, filthy feet; and a long neck drawn tight with tendons.

Drawn on the standard 64x64 player layout at two pixels to a texel (128x128), then doubled to the 256-pixel sheet, as the
shared painter does. The elbow and knee swatches, every base face and the neck's own island are opaque.
"""
from PIL import Image, ImageDraw

S = 2
SKIN, SKIN_SHADE, SKIN_DEEP, SKIN_LIGHT = '#cfc8bb', '#aaa398', '#8a837a', '#e4ded2'
HAIR, HAIR_STRAND, HAIR_SHEEN, DRIP = '#121214', '#1e1f23', '#4a4f58', '#6f7886'
DRESS, DRESS_SHADE, DRESS_WET, DRESS_DARK, DRESS_STAIN = '#d8d6c3', '#b9b9a6', '#9c9e8c', '#6d6f62', '#a49a84'
SPRIG, SPRIG_DARK, SOCKET = '#5f6b60', '#3f4840', '#8c877e'
DIRT, NAIL = '#5d5249', '#3f3833'
DEAD_EYE, DEAD_FILM, DEAD_IRIS = '#c4cccb', '#e3e8e6', '#a6b1b2'


def net(u, v, w, h, d):
    return {'top': (u+d, v, u+d+w, v+d), 'bottom': (u+d+w, v, u+d+2*w, v+d),
            'right': (u, v+d, u+d, v+d+h), 'front': (u+d, v+d, u+d+w, v+d+h),
            'left': (u+d+w, v+d, u+2*d+w, v+d+h), 'back': (u+2*d+w, v+d, u+2*d+2*w, v+d+h)}


def _box(box):
    return tuple(v*S for v in box)


def _panel(canvas, box, base):
    x0, y0, x1, y1 = _box(box)
    d = ImageDraw.Draw(canvas)
    d.rectangle((x0, y0, x1-1, y1-1), fill=base)
    return d, x0, y0, x1-x0, y1-y0


def _hair(canvas, box, seed, sheen=True):
    d, x0, y0, w, h = _panel(canvas, box, HAIR)
    # Wet strands clump into darker and lighter vertical ribbons, with a sheen where they catch the green light.
    for i, x in enumerate(range(x0, x0+w)):
        if (x*7+seed) % 5 == 0:
            d.line((x, y0, x, y0+h-1), fill=HAIR_STRAND)
        if sheen and (x*11+seed) % 9 == 0:
            top = y0+(x*3+seed) % max(1, h//2)
            d.line((x, top, x, min(y0+h-1, top+max(2, h//3))), fill=HAIR_SHEEN)
    return d, x0, y0, w, h


def _dress(canvas, box, seed, wet_top=0, blotch=False):
    d, x0, y0, w, h = _panel(canvas, box, DRESS)
    # The paper creases where it hangs: a few folds of different lengths, not a weave.
    for x in range(x0, x0+w):
        if (x*5+seed) % 7 == 0:
            d.line((x, y0+(x+seed) % 4, x, y0+h-1-(x*3+seed) % 5), fill=DRESS_SHADE)
    # Darker where wet hair lies on it.
    if wet_top:
        for x in range(x0, x0+w):
            depth = wet_top+((x*5+seed) % 3)
            d.line((x, y0, x, min(y0+h-1, y0+depth)), fill=DRESS_WET)
    # Faded flowers, a sprig here and there, like the print on an old nightgown.
    for i in range(w*h//40):
        sx, sy = x0+(i*7+seed*5) % max(1, w-2), y0+(i*11+seed*3) % max(1, h-3)
        d.point((sx+1, sy), fill=SPRIG)
        d.point((sx, sy+1), fill=SPRIG)
        d.point((sx+1, sy+1), fill=SPRIG_DARK)
        d.point((sx+1, sy+2), fill=SPRIG)
    # One old stain nobody washed out, low down where it soaked in.
    if blotch:
        bx, by = x0+(w-6 if seed % 2 else 2), y0+h-6
        d.rectangle((bx, by, bx+2, by+4), fill=DRESS_STAIN)
        d.rectangle((bx+1, by+2, bx+3, by+5), fill=DRESS_STAIN)
        d.point((bx+1, by+3), fill=DRESS_DARK)
        d.point((bx+2, by+4), fill=DRESS_DARK)
    return d, x0, y0, w, h


def _skin(canvas, box, seed):
    d, x0, y0, w, h = _panel(canvas, box, SKIN)
    # Waxy and even, shaded only along one edge so the limb reads round.
    d.line((x0+(w-1 if seed % 2 else 0), y0, x0+(w-1 if seed % 2 else 0), y0+h-1), fill=SKIN_SHADE)
    return d, x0, y0, w, h


def paint():
    im = Image.new('RGBA', (64*S, 64*S))
    d = ImageDraw.Draw(im)
    # Joint caps for the segmented limbs: the elbows and knees bend inside the gown, the knee a dark fold where the light catches it.
    d.rectangle(_box((0, 0, 8, 8))[:2]+tuple(v-1 for v in _box((0, 0, 8, 8))[2:]), fill=DRESS)
    d.rectangle(_box((24, 0, 32, 8))[:2]+tuple(v-1 for v in _box((24, 0, 32, 8))[2:]), fill=DRESS_DARK)

    # The head: wet hair all round, the face only at the front.
    head = net(0, 0, 8, 8, 8)
    for i, side in enumerate(('top', 'right', 'left', 'back')):
        hd, x0, y0, w, h = _hair(im, head[side], i*3)
        if side == 'top':
            hd.line((x0+w//2, y0, x0+w//2, y0+h-1), fill='#2a2b30')
    _skin(im, head['bottom'], 2)
    face = Image.new('RGBA', (16, 16), SKIN)
    f = ImageDraw.Draw(face)
    f.rectangle((0, 0, 15, 2), fill=HAIR)
    for x in range(16):
        edge = 2+(x*5) % 3
        f.line((x, 0, x, edge), fill=HAIR if x % 3 else HAIR_STRAND)
    f.rectangle((0, 0, 2, 15), fill=HAIR)
    f.rectangle((13, 0, 15, 15), fill=HAIR)
    f.line((1, 4, 1, 13), fill=HAIR_SHEEN)
    f.line((14, 6, 14, 12), fill=HAIR_SHEEN)
    f.line((3, 5, 3, 15), fill=SKIN_SHADE)
    f.line((12, 4, 12, 15), fill=SKIN_SHADE)
    f.point((7, 4), fill=SKIN_LIGHT)
    f.point((8, 4), fill=SKIN_LIGHT)
    # Empty eyes, the pale clouded eyes of the dead: no pupil, only a milky film over a fainter iris, in sunken sockets.
    for x in (4, 9):
        f.line((x, 6, x+2, 6), fill=SKIN_SHADE)
        f.rectangle((x, 7, x+2, 8), fill=DEAD_EYE)
        f.point((x+2 if x < 8 else x, 8), fill=SOCKET)
        f.point((x+1, 7), fill=DEAD_FILM)
        f.point((x+1, 8), fill=DEAD_IRIS)
        f.point((x+2, 8), fill=DEAD_IRIS)
        f.line((x, 9, x+2, 9), fill=SOCKET)
        f.point((x+1, 10), fill=SKIN_SHADE)
    f.line((7, 10, 8, 10), fill=SKIN_SHADE)
    f.point((7, 11), fill=SKIN_DEEP)
    f.point((8, 11), fill=SKIN_DEEP)
    # A thin, patient mouth, its corners down.
    f.line((6, 12, 9, 12), fill='#9c8580')
    f.line((5, 13, 10, 13), fill='#3a2d2d')
    f.point((4, 14), fill=SKIN_DEEP)
    f.point((11, 14), fill=SKIN_DEEP)
    # One wet strand stuck across the forehead, and a drip from it.
    for x, y in ((5, 3), (6, 4), (7, 5)):
        f.point((x, y), fill=HAIR_STRAND)
    f.point((7, 6), fill=DRIP)
    x0, y0, x1, y1 = _box(head['front'])
    im.paste(face.resize((x1-x0, y1-y0), Image.Resampling.NEAREST), (x0, y0))

    # Hat layer: only a few strands hanging in front of the face and over the ears.
    hat = net(32, 0, 8, 8, 8)
    hd = ImageDraw.Draw(im)
    fx0, fy0, fx1, fy1 = _box(hat['front'])
    for col in (2, 13):
        hd.line((fx0+col, fy0, fx0+col, fy1-1), fill=HAIR_STRAND)
        hd.point((fx0+col, fy1-1), fill=DRIP)
    for side in ('right', 'left'):
        sx0, sy0, sx1, sy1 = _box(hat[side])
        for col in range(sx0+1, sx1, 3):
            hd.line((col, sy0, col, sy1-3+(col % 3)), fill=HAIR)
    tx0, ty0, tx1, ty1 = _box(hat['back'])
    for col in range(tx0, tx1, 2):
        hd.line((col, ty0, col, ty1-1-(col % 4)), fill=HAIR_STRAND if col % 4 else HAIR)

    # The body: the dress, with her hair lying wet over the shoulders and down the back.
    body = net(16, 16, 8, 12, 4)
    for i, side in enumerate(('front', 'right', 'left', 'back', 'bottom')):
        _dress(im, body[side], i+1, wet_top=0 if side == 'bottom' else 3 if side == 'front' else 5, blotch=side in ('front', 'back'))
    # Her shoulders, seen from above: wet hair lying over them.
    td, x0, y0, w, h = _panel(im, body['top'], HAIR)
    for x in range(x0, x0+w, 3):
        td.line((x, y0, x, y0+h-1), fill=HAIR_STRAND)
    fx0, fy0, fx1, fy1 = _box(body['front'])
    bd = ImageDraw.Draw(im)
    bd.polygon(((fx0+4, fy0), (fx1-5, fy0), (fx1-7, fy0+3), (fx0+6, fy0+3)), fill=SKIN)
    bd.line((fx0+5, fy0+3, fx1-6, fy0+3), fill=DRESS_DARK)
    bx0, by0, bx1, by1 = _box(body['back'])
    for col in range(bx0, bx1):
        bd.line((col, by0, col, by0+8+(col*3) % 4), fill=HAIR if col % 3 else HAIR_STRAND)
        if col % 5 == 1:
            bd.point((col, by0+10+(col % 3)), fill=DRIP)

    # The gown's long sleeves to the wrist, then the hands, long and dirty, the fingers apart.
    for u, v in ((40, 16), (32, 48)):
        arm = net(u, v, 4, 12, 4)
        for i, side in enumerate(('front', 'right', 'left', 'back')):
            ad, x0, y0, w, h = _skin(im, arm[side], i+u)
            _dress(im, (arm[side][0], arm[side][1], arm[side][2], arm[side][3]-4), i+u, wet_top=4 if side != 'front' else 0)
            ad.line((x0, y0+h-9, x0+w-1, y0+h-9), fill=DRESS_SHADE)
            ad.line((x0, y0+h-7, x0+w-1, y0+h-7), fill=SKIN_SHADE)
            if side in ('front', 'back'):
                for x in range(x0+1, x0+w-1, 2):
                    ad.line((x, y0+h-4, x, y0+h-2), fill=SKIN_SHADE)
            ad.line((x0, y0+h-1, x0+w-1, y0+h-1), fill=NAIL)
            ad.line((x0, y0+h-2, x0+w-1, y0+h-2), fill=DIRT)
        _dress(im, arm['top'], u)
        sd, x0, y0, w, h = _skin(im, arm['bottom'], u+2)
        sd.rectangle((x0, y0, x0+w-1, y0+h-1), fill=DIRT)
        sx0, sy0, sx1, sy1 = _box(arm['right' if u == 40 else 'left'])
        sd.line((sx0+1, sy0, sx0+1, sy0+7), fill=HAIR)

    # Legs: the gown to below the calf, bare ankles, and bare feet: pale toes apart at the front, dirt only around the soles.
    for u, v in ((0, 16), (16, 48)):
        leg = net(u, v, 4, 12, 4)
        for i, side in enumerate(('front', 'right', 'left', 'back')):
            ld, x0, y0, w, h = _skin(im, leg[side], i+v)
            hem = 15+(i % 2)
            ld.rectangle((x0, y0, x0+w-1, y0+hem), fill=DRESS)
            for x in range(x0, x0+w):
                if (x*5+i) % 7 == 0:
                    ld.line((x, y0, x, y0+hem-2), fill=DRESS_SHADE)
            ld.point((x0+(i*3+2) % (w-1), y0+5+i), fill=SPRIG)
            ld.point((x0+(i*3+2) % (w-1)+1, y0+6+i), fill=SPRIG_DARK)
            ld.point((x0+(i*5+1) % (w-1), y0+12-i), fill=SPRIG)
            ld.line((x0, y0+hem, x0+w-1, y0+hem), fill=DRESS_DARK)
            ld.point((x0+(i*3+1) % w, y0+hem+1), fill=DRESS_DARK)
            # The ankle bone, a tendon at the back, and the foot below.
            if side in ('right', 'left'):
                ld.point((x0+w//2, y0+h-6), fill=SKIN_SHADE)
            if side == 'back':
                ld.line((x0+w//2, y0+hem+2, x0+w//2, y0+h-3), fill=SKIN_SHADE)
            ld.line((x0, y0+h-4, x0+w-1, y0+h-4), fill=SKIN_SHADE if side == 'front' else SKIN)
            if side == 'front':
                for x in range(x0+1, x0+w, 2):
                    ld.line((x, y0+h-3, x, y0+h-1), fill=SKIN_DEEP)
                for x in range(x0, x0+w, 2):
                    ld.point((x, y0+h-2), fill=SKIN_LIGHT)
            ld.line((x0, y0+h-1, x0+w-1, y0+h-1), fill=DIRT)
            ld.point((x0+(i*3+1) % w, y0+h-2), fill=DIRT)
        _dress(im, leg['top'], u+1)
        # The soles: black with the tunnel, the arch a little paler.
        fd, x0, y0, w, h = _panel(im, leg['bottom'], DIRT)
        fd.line((x0+1, y0+h//2, x0+w-2, y0+h//2), fill=SKIN_DEEP)

    # Her neck's own island: long and pale, tendons drawn tight under the skin.
    for side, box in net(0, 32, 2, 4, 2).items():
        nd, x0, y0, w, h = _panel(im, box, SKIN)
        nd.line((x0, y0, x0, y0+h-1), fill=SKIN_SHADE)
        if h > 4:
            nd.line((x0+w-1, y0+1, x0+w-1, y0+h-2), fill=SKIN_LIGHT)
            nd.point((x0+w//2, y0+h//2), fill=SKIN_DEEP)
    return im.resize((256, 256), Image.Resampling.NEAREST)
