"""Which faces of a model would flicker against each other, and the cure (0.4.463; by the pixel, the cure and the whole pack, 0.4.597).

Two faces that lie in one plane, face the same way and overlap are drawn at the same depth, and where they are not the same colour the
eye sees one, then the other, in a crawling pattern that changes as it moves: on a still picture of the model there is nothing to
see. A model's pieces overlap all the time (a tube into a tube, a wheel behind a frame), and it only matters where both faces show
and differ, so this looks for exactly that: fights(parts) gives every such pair in one model's pieces.

Told nothing about the pictures, it can only know two faces alike when both are drawn from the same one pixel. Given the pictures
(pixels=), it lays each face's picture over what the two share exactly as the game does (the face's own uv and turn, or the uv the game
gives a face that names none, from where its piece lies) and compares them pixel for pixel: two boards crossing in one plane with the
same grain at the same place do not flicker, and a face that is clear where the other shows does not either.

The cure is settle(parts): over the smaller of two faces that clash, a copy of that face is laid a hair in front, as a thin piece
of its own, closed round its sides so that what is under it cannot be seen from a slant either. Nothing already in the model is
moved, so nothing that sat well before sits differently after. It is told from the pieces alone, so the same pieces always settle
the same way. A tool that writes models writes them with dumps() here
in place of json.dumps, and every model it draws is settled on the way out.

    python3 tools/flicker.py            every model in the pack: which have faces that flicker, and how much of them
    python3 tools/flicker.py --check    the same, and fails if any has; and if any see-through face is laid over itself, any face
                                        is mapped off its picture (0.4.650) or any face draws nothing (blank, 0.4.651)
    python3 tools/flicker.py --cure     settles every model in the pack where it lies (a tool that writes with dumps() agrees with it),
                                        each face's mapping first kept on its own picture (fitted, 0.4.650)
    python3 tools/flicker.py NAME ...   only these models (block/bike_night/wasp), with every pair
"""
import copy
import json
import math
import sys
from pathlib import Path

NORMAL = {'north': (0, 0, -1), 'south': (0, 0, 1), 'west': (-1, 0, 0), 'east': (1, 0, 0), 'down': (0, -1, 0), 'up': (0, 1, 0)}
ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / 'src/main/resources/assets'
# Below this much of a pixel's strength (the game's own cut for a cut-out picture) nothing is drawn.
CLEAR = 26
# Faces nearer than this to one plane are in it; the cure lays one this much in front of the other; and two that flicker under
# a third face lying no further than COVER in front of them cannot be seen to. SLIP is a trade: what the cure lays is seen as
# a step this deep (a pixel on the screen at one block away, for .02), and holds only as far as the game's depth can tell it
# from what is under it (about 32 blocks for .02, 51 for .05: the depth it can tell apart at z blocks is z * z / 838,861).
NEAR, SLIP, COVER = .02, .02, .25
# What is written of a laid piece is rounded in the fifth place: a face this little short of NEAR away is NEAR away.
HAIR = 2e-5
# How far past a laid piece's border a face in its plane counts as carrying on beyond it.
REACH = .05
# What the cure writes on each face it lays, so that it can be told from the pieces a model was drawn with.
LAID = 'laid over the face behind it so the two do not flicker (tools/flicker.py)'
# The cure has to come out the same under every Python and on every machine, or the workflow fails a model this tool wrote
# itself. It did once, on the film set's softbox: three slats of one size cross in one plane, the tool told which was the
# larger by the last digit of a sum, and Python 3.12 adds floats up a little more exactly than 3.11 does. So a face's size is
# read off its piece's own sides (_area), where two of one size are the same number; every sum is fsum, which rounds once and
# the same everywhere; the five turns the game allows are worked from square roots, which every machine rounds alike, and not
# from the C library's sine and cosine; and a length is the square root of such a sum.
_sum = math.fsum
_HALF = math.sqrt(.5)
_EIGHTH = (math.sqrt((1 + _HALF) / 2), math.sqrt((1 - _HALF) / 2))
_TURNS = {0: (1.0, 0.0), 45: (_HALF, _HALF), -45: (_HALF, -_HALF), 22.5: _EIGHTH, -22.5: (_EIGHTH[0], -_EIGHTH[1])}


def _turn(angle):
    """The cosine and sine of a piece's turn."""
    if angle in _TURNS:
        return _TURNS[angle]
    a = math.radians(angle)
    return math.cos(a), math.sin(a)


def _far(a, b):
    """How far apart two points are."""
    return math.sqrt(_sum((p - q) * (p - q) for p, q in zip(a, b)))


def turned(p, r):
    """A point about the origin, turned as a model's piece is."""
    if not r:
        return p
    c, s = _turn(r['angle'])
    x, y, z = p
    if r['axis'] == 'z':
        return (x * c - y * s, x * s + y * c, z)
    if r['axis'] == 'x':
        return (x, y * c - z * s, y * s + z * c)
    return (x * c + z * s, y, -x * s + z * c)


def placed(p, r):
    """Where a piece's turn puts one of its corners: about the turn's origin, and stretched across the turn if the piece asks to be."""
    if not r:
        return p
    o = r['origin']
    q = turned((p[0] - o[0], p[1] - o[1], p[2] - o[2]), r)
    if r.get('rescale'):
        s = 1 / (_EIGHTH[0] if abs(r['angle']) == 22.5 else _HALF)
        q = tuple(c if axis == r['axis'] else c * s for c, axis in zip(q, 'xyz'))
    return (q[0] + o[0], q[1] + o[1], q[2] + o[2])


def _corners(e):
    """The four corners of each side of a piece before it is turned, in the game's own order: seen from outside, the top left, the
    bottom left, the bottom right, the top right."""
    (x0, y0, z0), (x1, y1, z1) = e['from'], e['to']
    return {'down': [(x0, y0, z1), (x0, y0, z0), (x1, y0, z0), (x1, y0, z1)], 'up': [(x0, y1, z0), (x0, y1, z1), (x1, y1, z1), (x1, y1, z0)],
            'north': [(x1, y1, z0), (x1, y0, z0), (x0, y0, z0), (x0, y1, z0)], 'south': [(x0, y1, z1), (x0, y0, z1), (x1, y0, z1), (x1, y1, z1)],
            'west': [(x0, y1, z0), (x0, y0, z0), (x0, y0, z1), (x0, y1, z1)], 'east': [(x1, y1, z1), (x1, y0, z1), (x1, y0, z0), (x1, y1, z0)]}


def quads(e):
    """Each face a piece shows: its name, what it is drawn with, its four corners where the piece's turn puts them (in the game's
    own order, as _corners gives them), and the way it faces."""
    r = e.get('rotation')
    at = _corners(e)
    for k, f in e.get('faces', {}).items():
        yield k, f, [placed(q, r) for q in at[k]], turned(NORMAL[k], r)


def pictured(e, k, f):
    """Where in its picture (0 to 16 each way) each of a face's four corners falls, in the corners' order: by the face's own uv, or
    by the one the game gives a face that names none, and turned as the face asks."""
    uv = f.get('uv') or _lies(e, k)
    turn = int(f.get('rotation', 0)) // 90
    out = []
    for i in range(4):
        j = (i + turn) % 4
        out.append((uv[0] if j in (0, 1) else uv[2], uv[1] if j in (0, 3) else uv[3]))
    return out


def across(n):
    """Two ways across a plane, square to each other and to the way it faces."""
    ref = (1, 0, 0) if abs(n[0]) < .9 else (0, 1, 0)
    u = (n[1] * ref[2] - n[2] * ref[1], n[2] * ref[0] - n[0] * ref[2], n[0] * ref[1] - n[1] * ref[0])
    m = math.sqrt(_sum(c * c for c in u)); u = tuple(c / m for c in u)
    return u, (n[1] * u[2] - n[2] * u[1], n[2] * u[0] - n[0] * u[2], n[0] * u[1] - n[1] * u[0])


def both(subject, clipper):
    """What two convex shapes in a plane have in common (each given counter-clockwise)."""
    def inside(q, a, b):
        return (b[0] - a[0]) * (q[1] - a[1]) - (b[1] - a[1]) * (q[0] - a[0]) >= -1e-9

    def cut(q, t, a, b):
        d = (q[0] - t[0]) * (a[1] - b[1]) - (q[1] - t[1]) * (a[0] - b[0])
        if abs(d) < 1e-12:
            return t
        w = ((q[0] - a[0]) * (a[1] - b[1]) - (q[1] - a[1]) * (a[0] - b[0])) / d
        return (q[0] + w * (t[0] - q[0]), q[1] + w * (t[1] - q[1]))
    out = subject
    for i in range(len(clipper)):
        a, b = clipper[i], clipper[(i + 1) % len(clipper)]
        src, out = out, []
        if not src:
            break
        last = src[-1]
        for q in src:
            if inside(q, a, b):
                if not inside(last, a, b):
                    out.append(cut(last, q, a, b))
                out.append(q)
            elif inside(last, a, b):
                out.append(cut(last, q, a, b))
            last = q
    return out


def twice(poly):
    return _sum(poly[i][0] * poly[(i + 1) % len(poly)][1] - poly[(i + 1) % len(poly)][0] * poly[i][1] for i in range(len(poly)))


def within(x, y, poly):
    """Whether a point lies in a shape given counter-clockwise."""
    return all((poly[(i + 1) % len(poly)][0] - poly[i][0]) * (y - poly[i][1]) - (poly[(i + 1) % len(poly)][1] - poly[i][1]) * (x - poly[i][0]) >= -1e-5
               for i in range(len(poly)))


def narrow(poly):
    """How wide a shape is across its narrow way, near enough: twice its area over the way round it."""
    round_it = _sum(_far(poly[i], poly[(i + 1) % len(poly)]) for i in range(len(poly)))
    return abs(twice(poly)) / round_it if round_it else 0.0


def look(f):
    """What a face is drawn with. Two faces drawn from the same one pixel are the same flat colour and cannot be told apart."""
    uv = f.get('uv', [0, 0, 16, 16])
    one = 0 < uv[2] - uv[0] <= 1 and 0 < uv[3] - uv[1] <= 1 and uv[0] == int(uv[0]) and uv[1] == int(uv[1])
    return (f['texture'], tuple(uv) if one else None)


EDGE = ('edge',)


class Shown:
    """One face as it is drawn: where on its plane it lies, and which pixel of its picture falls on each point of it."""

    def __init__(self, e, k, f, flat, pixels):
        self.light = (f.get('tintindex', -1), e.get('shade', True), json.dumps(f.get('neoforge_data', e.get('neoforge_data')), sort_keys=True),
                      e.get('light_emission', 0))
        (ax, ay), (bx, by), _, (cx, cy) = flat                # top left, bottom left, bottom right, top right
        self.o = (ax, ay)
        self.across = (cx - ax, cy - ay); self.down = (bx - ax, by - ay)
        self.det = self.across[0] * self.down[1] - self.across[1] * self.down[0]
        uv = pictured(e, k, f)
        self.uv = uv[0]; self.uacross = (uv[3][0] - uv[0][0], uv[3][1] - uv[0][1]); self.udown = (uv[1][0] - uv[0][0], uv[1][1] - uv[0][1])
        got = pixels(f.get('texture', '')) if pixels else None
        self.name = f.get('texture', '')
        self.w, self.h, self.px = got if got else (16, 16, None)
        # How long, on the plane, one pixel of the picture is: the finest of the two ways.
        grains = []
        for way, uway in ((self.across, self.uacross), (self.down, self.udown)):
            pixels_along = (abs(uway[0]) * self.w + abs(uway[1]) * self.h) / 16
            grains.append(math.sqrt(_sum(c * c for c in way)) / pixels_along if pixels_along > 1e-9 else 1e9)
        self.grain = min(grains)

    def where(self, x, y):
        """Where in the face's picture (0 to 16 each way) a point of the plane falls."""
        dx, dy = x - self.o[0], y - self.o[1]
        s = (dx * self.down[1] - dy * self.down[0]) / self.det
        t = (self.across[0] * dy - self.across[1] * dx) / self.det
        return (self.uv[0] + s * self.uacross[0] + t * self.udown[0], self.uv[1] + s * self.uacross[1] + t * self.udown[1])

    def at(self, x, y):
        """What is drawn at a point of the plane: nothing (None), a colour, or (a picture never read) which pixel of which picture."""
        u, v = self.where(x, y)
        u, v = u * self.w / 16, v * self.h / 16
        if abs(u - round(u)) < 1e-6 or abs(v - round(v)) < 1e-6:
            return EDGE                                       # on the line between two pixels: either, by a rounding
        i = min(self.w - 1, max(0, int(u))); j = min(self.h - 1, max(0, int(v)))
        if self.px is None:
            return (self.name, i, j)
        c = self.px[i, j]
        return None if c[3] < CLEAR else c


def differ(shared, a, b, close, covers=()):
    """How much of what two faces share (its area) shows one colour on one and another on the other, where nothing lying just in
    front of them (covers: each a shape and its face) hides it."""
    xs = [q[0] for q in shared]; ys = [q[1] for q in shared]
    x0, x1, y0, y1 = min(xs), max(xs), min(ys), max(ys)
    step = min(a.grain, b.grain) / 2
    nx = max(2, min(96, math.ceil((x1 - x0) / step))); ny = max(2, min(96, math.ceil((y1 - y0) / step)))
    edges = [(shared[i], shared[(i + 1) % len(shared)]) for i in range(len(shared))]
    lit = a.light != b.light
    inside = unlike = 0
    for i in range(nx):
        x = x0 + (i + .37) * (x1 - x0) / nx
        for j in range(ny):
            y = y0 + (j + .61) * (y1 - y0) / ny
            if any((q[0] - p[0]) * (y - p[1]) - (q[1] - p[1]) * (x - p[0]) < 0 for p, q in edges):
                continue
            ca, cb = a.at(x, y), b.at(x, y)
            if ca is EDGE or cb is EDGE:
                continue
            inside += 1
            if ca is None or cb is None:
                continue
            if any(within(x, y, shape) and face.at(x, y) not in (None, EDGE) for shape, face in covers):
                continue
            if lit or len(ca) != len(cb) or (max(abs(ca[n] - cb[n]) for n in range(3)) > close if len(ca) == 4 else ca != cb):
                unlike += 1
    return abs(twice(shared)) / 2 * unlike / inside if inside else 0.0


def fights(parts, near=NEAR, least=.003, pixels=None, close=6, gaps=False, thin=0, cover=COVER):
    """The faces of one model that would flicker: two of different pieces that face the same way, lie within `near` of one plane,
    cover more than `least` of each other and are not the same colour there. Each as (piece, face, piece, face, how much of them),
    and with `gaps` how far apart their planes are after that.

    Two faces with the very same four corners do not flicker: they are drawn at exactly the same depth at every point, and the game
    draws the later over the earlier every time (its own grass block lays its coloured sides over its plain ones so). With `thin`,
    neither does a shared strip narrower than that. And with the pictures, what a third face lying within `cover` in front of the
    two hides does not count: that is how the cure leaves them (settle).

    Without `pixels` two faces are the same colour only when both are drawn from the same one pixel. With it (a way from a face's
    picture name to (width, height, the pixels), or None for a picture that cannot be read) they are compared pixel for pixel over
    what they share, and what counts is the part where both show and differ by more than `close` in any of red, green and blue."""
    planes = {}
    for i, e in enumerate(parts):
        for k, f, pts, n in quads(e):
            d = _sum(n[j] * pts[0][j] for j in range(3))
            planes.setdefault(tuple(round(c, 4) + 0.0 for c in n), (n, []))[1].append((d, i, k, f, pts))
    found = []
    for n, faces in planes.values():
        u, v = across(n)
        flat = []
        for d, i, k, f, pts in sorted(faces, key=lambda t: t[0]):
            poly = [(_sum(q[j] * u[j] for j in range(3)), _sum(q[j] * v[j] for j in range(3))) for q in pts]
            if abs(twice(poly)) < 1e-9:
                continue                                      # a face with no size is not drawn
            flat.append([d, i, k, look(f) if pixels is None else None, poly if twice(poly) >= 0 else poly[::-1], f, poly, None])
        for a in range(len(flat)):
            da, ia, ka, la, pa = flat[a][:5]
            for b in range(a + 1, len(flat)):
                db, ib, kb, lb, pb = flat[b][:5]
                if db - da >= near - HAIR:
                    break
                if ia == ib or (pixels is None and la == lb and la[1] is not None):
                    continue
                if all(any(abs(p[0] - q[0]) + abs(p[1] - q[1]) < 1e-5 for q in pb) for p in pa) and db - da < 1e-6:
                    continue
                shared = both(pa, pb)
                if len(shared) < 3 or abs(twice(shared)) / 2 <= least or narrow(shared) < thin:
                    continue
                area = abs(twice(shared)) / 2
                if pixels is not None:
                    over = []
                    for c in range(b + 1, len(flat)):
                        if flat[c][0] - db > cover + 1e-9:
                            break
                        if flat[c][0] - db >= near - HAIR and flat[c][1] not in (ia, ib) and len(both(shared, flat[c][4])) >= 3:
                            over.append(flat[c])
                    for row in [flat[a], flat[b]] + over:
                        if row[7] is None:
                            row[7] = Shown(parts[row[1]], row[2], row[5], row[6], pixels)
                    area = differ(shared, flat[a][7], flat[b][7], close, [(row[4], row[7]) for row in over])
                    if area <= least:
                        continue
                found.append((ia, ka, ib, kb, round(area, 3)) + ((round(db - da, 5),) if gaps else ()))
    return found


# ------------------------------------------------------------------------------------------------------------------------ the cure
AXIS = {'east': (0, 'to', 1), 'west': (0, 'from', -1), 'up': (1, 'to', 1), 'down': (1, 'from', -1), 'south': (2, 'to', 1), 'north': (2, 'from', -1)}


def _number(x):
    x = round(x, 5)
    return int(x) if x == int(x) else x


def _same_laid(e, k, f, g_e, g_k, g, shared, basis):
    """Whether two faces show the same picture laid the same way over what they share, whatever the picture is: the same name,
    the same light, and each corner of the shared part falling at the same place in it (or both no more than one pixel of it)."""
    if f.get('texture') != g.get('texture') or f.get('tintindex', -1) != g.get('tintindex', -1) or e.get('shade', True) != g_e.get('shade', True):
        return False
    if json.dumps(f.get('neoforge_data', e.get('neoforge_data')), sort_keys=True) != json.dumps(g.get('neoforge_data', g_e.get('neoforge_data')), sort_keys=True):
        return False
    a, b = Shown(e, k, f, basis[0], None), Shown(g_e, g_k, g, basis[1], None)
    cells = set()
    for shown in (a, b):
        us = [shown.uv[0], shown.uv[0] + shown.uacross[0], shown.uv[0] + shown.udown[0], shown.uv[0] + shown.uacross[0] + shown.udown[0]]
        vs = [shown.uv[1], shown.uv[1] + shown.uacross[1], shown.uv[1] + shown.udown[1], shown.uv[1] + shown.uacross[1] + shown.udown[1]]
        if max(us) - min(us) > 1 or max(vs) - min(vs) > 1 or min(us) != int(min(us)) or min(vs) != int(min(vs)):
            cells = None
            break
        cells.add((min(us), min(vs)))
    if cells is not None and len(cells) == 1:
        return True
    for x, y in shared:
        if any(abs(m - n) > 1e-3 for m, n in zip(a.where(x, y), b.where(x, y))):
            return False
    return True


def clashes(parts, near=NEAR, least=.003, thin=0, cover=COVER):
    """The pairs the cure settles, told from the pieces alone (no picture is read, so the same pieces always settle the same way):
    two faces of different pieces that face the same way, lie within `near` of one plane and share more than `least`, unless they
    have the very same corners, show the same picture laid the same way, or lie wholly under a third face no further than `cover`
    in front of them. Each as ((piece, face), (piece, face)), the one that stays behind first: the one already behind, or of two
    in one plane the larger (of two alike, the earlier piece)."""
    planes = {}
    for i, e in enumerate(parts):
        for k, f, pts, n in quads(e):
            d = _sum(n[j] * pts[0][j] for j in range(3))
            planes.setdefault(tuple(round(c, 4) + 0.0 for c in n), (n, []))[1].append((d, i, k, f, pts))
    found = []
    for n, faces in planes.values():
        u, v = across(n)
        flat = []
        for d, i, k, f, pts in faces:
            poly = [(_sum(q[j] * u[j] for j in range(3)), _sum(q[j] * v[j] for j in range(3))) for q in pts]
            if abs(twice(poly)) < 1e-9:
                continue
            # The larger stays behind, and of two alike the earlier piece: by the piece's own sides, as settle orders them.
            flat.append((round(d, 6), -_area(parts, (i, k)), i, k, f, poly if twice(poly) >= 0 else poly[::-1], poly))
        flat.sort(key=lambda t: t[:4])
        for a in range(len(flat)):
            da, _, ia, ka, fa, pa, ra = flat[a]
            for b in range(a + 1, len(flat)):
                db, _, ib, kb, fb, pb, rb = flat[b]
                if db - da >= near - HAIR:
                    break
                if ia == ib:
                    continue
                if db - da < 1e-6 and all(any(abs(p[0] - q[0]) + abs(p[1] - q[1]) < 1e-5 for q in pb) for p in pa):
                    continue
                shared = both(pa, pb)
                if len(shared) < 3 or abs(twice(shared)) / 2 <= least or narrow(shared) < thin:
                    continue
                if _same_laid(parts[ia], ka, fa, parts[ib], kb, fb, shared, (ra, rb)):
                    continue
                hidden = False
                for c in range(b + 1, len(flat)):
                    if flat[c][0] - db > cover + 1e-9:
                        break
                    if flat[c][0] - db >= near - HAIR and flat[c][2] not in (ia, ib) and all(within(x, y, flat[c][5]) for x, y in shared):
                        hidden = True
                        break
                if not hidden:
                    found.append(((ia, ka), (ib, kb)))
    return found


def bare(parts):
    """A model's pieces as it was drawn: without the faces the cure has laid. A tool that builds one model on another it reads from
    the pack builds it on these."""
    return [e for e in parts if e.get('__comment') != LAID]


def _is_side(parts, face):
    """Whether a face is a side of a laid piece: any face of one but the first it names, or the one face of a strip (a laid piece
    with no thickness the way that face looks)."""
    e = parts[face[0]]
    if e.get('__comment') != LAID:
        return False
    axis = AXIS[face[1]][0]
    return face[1] != next(iter(e['faces'])) or abs(e['to'][axis] - e['from'][axis]) < 1e-9


def _flat(e, k):
    """One face of a piece on its own plane: its shape counter-clockwise, and its four corners in the game's order."""
    for _, _, pts, n in quads({**e, 'faces': {k: e['faces'][k]}}):
        u, v = across(n)
        poly = [(_sum(q[j] * u[j] for j in range(3)), _sum(q[j] * v[j] for j in range(3))) for q in pts]
        return (poly if twice(poly) >= 0 else poly[::-1]), poly


def _kept(spans, long):
    """What is left of a side `long` pixels long (as stretches from 0 to 1 of it) once `spans` are cut out of it, less any scrap
    shorter than a twentieth of a pixel."""
    keep, at = [], 0.0
    for lo, hi in sorted(spans):
        if lo - at > .05 / long:
            keep.append((at, lo))
        at = max(at, hi)
    if 1 - at > .05 / long:
        keep.append((at, 1.0))
    return keep


def _resides(out, made, near):
    """Every laid piece's sides, made afresh (made: each laid piece's own piece and face, by id). A side is drawn as its piece's
    own face on that side is at its edge (_beside), or failing that with the edge of the front's picture (_side), and it stands
    only along the stretches of the border beyond which nothing lies in the front's plane: no face of another piece between where
    the face behind may be and where the laid piece's front is, within REACH of the border. Where something does lie there (the
    face behind carrying on, or another laid piece beside this one), a side would stand proud of it and be seen; with none, what
    shows under the laid piece from a slant is that same face, beside itself."""
    out[:] = [e for e in out if id(e) in made or e.get('__comment') != LAID]
    groups = {}
    for i, e in enumerate(out):
        mine = made.get(id(e))
        if mine:
            k = mine[1]; axis = AXIS[k][0]
            e['faces'] = {k: e['faces'][k]}
            for side in AXIS:
                if AXIS[side][0] != axis:
                    edge = _beside(e, k, side, mine[0]) or _side(e, k, side)
                    if edge:
                        e['faces'][side] = edge
        for k, f, pts, n in quads(e):
            if not mine or k == mine[1]:
                groups.setdefault(tuple(round(c, 4) + 0.0 for c in n), (n, []))[1].append((_sum(n[j] * pts[0][j] for j in range(3)), i, pts))
    strips = []
    for lid in out:
        if id(lid) not in made:
            continue
        e, k = made[id(lid)]
        axis, end, _ = AXIS[k]
        r = lid.get('rotation')
        n, faces = groups[tuple(round(c, 4) + 0.0 for c in turned(NORMAL[k], r))]
        u, v = across(n)

        def flat(q):
            return (_sum(q[j] * u[j] for j in range(3)), _sum(q[j] * v[j] for j in range(3)))
        plane = e[end][axis]
        corner = _corners(lid)[k][0]
        at_lid = _sum(n[j] * c for j, c in enumerate(placed(corner, r)))
        at_face = _sum(n[j] * c for j, c in enumerate(placed(tuple(plane if j == axis else corner[j] for j in range(3)), r)))
        for side in list(lid['faces'])[1:]:
            where = _corners(lid)[side]
            inner = [i for i in range(4) if abs(where[i][axis] - plane) < 1e-9]
            first = next((i for i in inner if (i + 1) % 4 in inner), None)
            if first is None:
                continue
            a, b = flat(placed(where[first], r)), flat(placed(where[(first + 1) % 4], r))
            long = _far(a, b)
            if long < 1e-9:
                continue
            o = flat(turned(NORMAL[side], r))
            band = [a, b, (b[0] + REACH * o[0], b[1] + REACH * o[1]), (a[0] + REACH * o[0], a[1] + REACH * o[1])]
            if twice(band) < 0:
                band = band[::-1]
            spans = []
            for d, i, pts in faces:
                if out[i] is e or out[i] is lid or not at_face - near - HAIR <= d <= at_lid + HAIR:
                    continue
                poly = [flat(q) for q in pts]
                shared = both(band, poly if twice(poly) >= 0 else poly[::-1])
                if len(shared) >= 3 and abs(twice(shared)) / 2 > 1e-6:
                    along = [((q[0] - a[0]) * (b[0] - a[0]) + (q[1] - a[1]) * (b[1] - a[1])) / long ** 2 for q in shared]
                    spans.append((min(along), max(along)))
            if spans:
                strips.extend(strip for lo, hi in _kept(spans, long) for strip in [_strip(lid, side, first, lo, hi)] if strip)
                del lid['faces'][side]
    out.extend(strips)


def _sides_off(out, near):
    """clashes() of the pieces, once no side of a laid piece clashes with anything: a side that lies in one plane with another
    face is cut back to the stretches of it that do not, each stretch a strip of its own. Where a side is cut away, what it
    clashed with stands in its place. Of two sides that clash with each other only the one behind is cut."""
    while True:
        pairs = clashes(out, near)
        cuts = {}
        for back, front in pairs:
            if _is_side(out, back):
                cuts.setdefault(back, []).append(front)
            elif _is_side(out, front):
                cuts.setdefault(front, []).append(back)
        if not cuts:
            return pairs
        strips = []
        for (i, k), others in sorted(cuts.items()):
            piece = out[i]
            shape, corners = _flat(piece, k)
            a, b = max(((corners[n], corners[(n + 1) % 4]) for n in (0, 1)), key=lambda edge: _far(*edge))
            long = _far(a, b)
            spans = []
            for j, kj in others:
                shared = both(shape, _flat(out[j], kj)[0])
                if len(shared) >= 3:
                    along = [((q[0] - a[0]) * (b[0] - a[0]) + (q[1] - a[1]) * (b[1] - a[1])) / long ** 2 for q in shared]
                    spans.append((min(along), max(along)))
            keep = _kept(spans, long)
            if spans and keep != [(0.0, 1.0)]:
                strips.extend(strip for lo, hi in keep for strip in [_strip(piece, k, corners.index(a), lo, hi)] if strip)
        for i, k in cuts:
            del out[i]['faces'][k]
        out[:] = [e for e in out if e.get('faces')] + strips


def settle(parts, slip=SLIP, near=NEAR):
    """A model's pieces with no two faces left to flicker: over every face that clashes with one behind it (clashes), a copy of
    that face is laid `slip` in front of the one behind (and of the face itself), as a thin piece of its own: that face, and sides back to
    where the face was along the border wherever nothing lies beyond it (_resides). Nothing already there is moved or changed, so
    nothing that sat well before sits differently after; what flickered still does, under the copy, where it cannot be seen from
    the front, and from a slant shows nothing that is not beside it already. A side that would lie in one plane with another face
    is cut back to where it does not (_sides_off). Faces with the very same
    corners are copied together and in their order, so a face laid over another stays laid over it. Copies laid before are taken
    off first and laid afresh, so pieces that have been settled, or built on pieces that were, settle to the same thing. Gives
    back the same list when that leaves it as it was, and otherwise a new one; raises if the pieces will not settle."""
    out = bare(parts); made = {}
    for _ in range(24):
        pairs = _sides_off(out, near)
        if not pairs:
            return parts if out == list(parts) else out
        depth, corners = {}, {}
        for i, e in enumerate(out):
            for k, f, pts, n in quads(e):
                depth[(i, k)] = _sum(n[j] * pts[0][j] for j in range(3))
                corners[(i, k)] = (tuple(round(c, 4) + 0.0 for c in n), tuple(sorted(tuple(round(c, 4) for c in q) for q in pts)))
        twins = {}
        for face, where in corners.items():
            twins.setdefault(where, []).append(face)
        behind = {}
        for back, front in pairs:
            behind.setdefault(front, []).append(back)
        # Those behind are settled before those in front of them: the same order clashes reads the faces of one plane in
        # (further back, then larger, then earlier), in which no face stands in front of one that comes after it.
        order = sorted(behind, key=lambda face: (corners[face][0], round(depth[face], 6), -_area(out, face), face[0], face[1]))
        shows, laid = dict(depth), {}
        for face in order:
            # Proud of what is behind it, and of the face itself where that already stands a little proud of what is behind.
            need = max([shows[back] for back in behind[face]] + [depth[face]]) + slip
            for twin in twins[corners[face]]:
                if shows[twin] < need - 1e-9:
                    shows[twin] = need; laid[twin] = need - depth[twin]
        if not laid:
            break
        fresh = []
        for i, k in sorted(laid, key=lambda face: (face[0], list(out[face[0]]['faces']).index(face[1]))):
            fresh.append(_copy(out[i], k, laid[(i, k)]))
            made[id(fresh[-1])] = (out[i], k)
        out.extend(fresh)
        _resides(out, made, near)
    raise ValueError('these pieces will not settle')


def _area(parts, face):
    e = parts[face[0]]; axis = AXIS[face[1]][0]
    sides = [abs(e['to'][j] - e['from'][j]) for j in range(3) if j != axis]
    return sides[0] * sides[1]


def _copy(e, k, by):
    """One face of a piece as a thin piece of its own standing `by` proud of where the face is, with the face's picture laid on its
    front as it was. Its sides are _resides' to give it."""
    axis, end, way = AXIS[k]
    r = e.get('rotation')
    if r and r.get('rescale') and 'xyz'[axis] != r['axis']:
        by *= _EIGHTH[0] if abs(r['angle']) == 22.5 else _HALF
    at = _number(e[end][axis] + way * by)
    start, stop = list(e['from']), list(e['to'])
    start[axis], stop[axis] = sorted((e[end][axis], at))
    face = dict(e['faces'][k])
    if 'uv' not in face:
        face['uv'] = [_number(c) for c in _lies(e, k)]
    out = {'__comment': LAID, 'from': start, 'to': stop}
    for key in ('rotation', 'shade', 'light_emission', 'neoforge_data'):
        if key in e:
            out[key] = copy.deepcopy(e[key])
    out['faces'] = {k: face}
    return out


def _beside(piece, k, side, e):
    """What one side of a laid piece is drawn with when the piece it was laid over shows a face that way: the edge of that face,
    which the side runs on from. Nothing if it shows none, or has no depth to take the edge from."""
    source = e.get('faces', {}).get(side)
    axis, end, _ = AXIS[k]
    plane = e[end][axis]
    far = abs(e['to'][axis] - e['from'][axis]); deep = abs(piece['to'][axis] - piece['from'][axis])
    if source is None or far < 1e-9 or deep < 1e-9:
        return None
    theirs = _corners(e)[side]
    uv = pictured(e, side, source)
    want = []
    for q in _corners(piece)[side]:
        i = next((n for n, c in enumerate(theirs) if all(abs(c[j] - (plane if j == axis else q[j])) < 1e-6 for j in range(3))), None)
        if i is None:
            return None
        if abs(q[axis] - plane) < 1e-9:
            want.append(uv[i])
            continue
        j = next((n for n in ((i + 1) % 4, (i - 1) % 4) if abs(theirs[n][axis] - plane) > 1e-9), None)   # that face's corner away from the edge
        if j is None:
            return None
        want.append((uv[i][0] + deep / far * (uv[j][0] - uv[i][0]), uv[i][1] + deep / far * (uv[j][1] - uv[i][1])))
    return _fit(piece, side, source, want)


def _fit(piece, k, like, want):
    """A face of a piece drawn as `like` is but for where in its picture each of its four corners falls (`want`, in the corners'
    order): the uv and turn that say so, or nothing if none does."""
    us = sorted({round(c[0], 5) for c in want}); vs = sorted({round(c[1], 5) for c in want})
    for turn in (0, 90, 180, 270):
        for u in ((us[0], us[-1]), (us[-1], us[0])):
            for v in ((vs[0], vs[-1]), (vs[-1], vs[0])):
                made = {'uv': [_number(u[0]), _number(v[0]), _number(u[1]), _number(v[1])], 'rotation': turn}
                if all(abs(a[0] - b[0]) + abs(a[1] - b[1]) < 1e-4 for a, b in zip(pictured(piece, k, made), want)):
                    out = {key: copy.deepcopy(value) for key, value in like.items() if key not in ('uv', 'rotation', 'cullface')}
                    out['uv'] = made['uv']
                    if turn:
                        out['rotation'] = turn
                    return out
    return None


def _strip(piece, k, first, lo, hi):
    """A stretch of one side of a laid piece as a piece of its own with no thickness: from `lo` to `hi` of the way (0 to 1) from the
    side's corner `first` to the next, drawn with what the side showed there."""
    where = _corners(piece)[k]
    uv = pictured(piece, k, piece['faces'][k])
    a, b, c = where[first], where[(first + 1) % 4], where[(first - 1) % 4]      # along the side, and across it
    ends = [tuple(a[j] + t * (b[j] - a[j]) for j in range(3)) for t in (lo, hi)]
    far = [tuple(q[j] + c[j] - a[j] for j in range(3)) for q in ends]
    start = [_number(min(q[j] for q in ends + far)) for j in range(3)]
    stop = [_number(max(q[j] for q in ends + far)) for j in range(3)]
    out = {'__comment': LAID, 'from': start, 'to': stop}
    for key in ('rotation', 'shade', 'light_emission', 'neoforge_data'):
        if key in piece:
            out[key] = copy.deepcopy(piece[key])
    along = [b[j] - a[j] for j in range(3)]; across_it = [c[j] - a[j] for j in range(3)]
    la = _sum(x * x for x in along); lc = _sum(x * x for x in across_it)
    if la < 1e-12 or lc < 1e-12:
        return None
    ua = (uv[(first + 1) % 4][0] - uv[first][0], uv[(first + 1) % 4][1] - uv[first][1])
    uc = (uv[(first - 1) % 4][0] - uv[first][0], uv[(first - 1) % 4][1] - uv[first][1])
    want = []
    for q in _corners(out)[k]:
        t = _sum((q[j] - a[j]) * along[j] for j in range(3)) / la
        w = _sum((q[j] - a[j]) * across_it[j] for j in range(3)) / lc
        want.append((uv[first][0] + t * ua[0] + w * uc[0], uv[first][1] + t * ua[1] + w * uc[1]))
    face = _fit(out, k, piece['faces'][k], want)
    if not face:
        return None
    out['faces'] = {k: face}
    return out


def _side(piece, k, side):
    """What one side of a laid piece is drawn with: the edge of its face's picture, as though the picture were folded over that
    edge and ran on down the side. Nothing where that cannot be said as a face's uv and turn."""
    axis = AXIS[k][0]
    front = piece['faces'][k]
    where = _corners(piece)
    uv = pictured(piece, k, front)
    deep = abs(piece['to'][axis] - piece['from'][axis])

    def flat(q):
        return tuple(round(q[j], 6) for j in range(3) if j != axis)
    over = {flat(q): i for i, q in enumerate(where[k])}
    if len(over) != 4 or deep < 1e-9:
        return None
    on = [over.get(flat(q)) for q in where[side]]              # which corner of the face each corner of the side lies under
    edge = set(on)
    if None in edge or len(edge) != 2:
        return None
    want = []
    for q, i in zip(where[side], on):
        if abs(q[axis] - where[k][0][axis]) < 1e-9:
            want.append(uv[i])
            continue
        j = next(n for n in ((i + 1) % 4, (i - 1) % 4) if n not in edge)   # the corner across the face from this one
        far = _far(where[k][i], where[k][j])
        if far < 1e-9:
            return None
        want.append((uv[i][0] + deep / far * (uv[j][0] - uv[i][0]), uv[i][1] + deep / far * (uv[j][1] - uv[i][1])))
    return _fit(piece, side, front, want)


def _lies(e, k):
    (x0, y0, z0), (x1, y1, z1) = e['from'], e['to']
    return {'down': (x0, 16 - z1, x1, 16 - z0), 'up': (x0, z0, x1, z1), 'north': (16 - x1, 16 - y1, 16 - x0, 16 - y0),
            'south': (x0, 16 - y1, x1, 16 - y0), 'west': (z0, 16 - y1, z1, 16 - y0), 'east': (16 - z1, 16 - y1, 16 - z0, 16 - y0)}[k]


def _onto(a, b):
    """One span of a face's mapping (0 to 16 on its picture), kept on the picture. The game draws whatever lies at a face's mapping in
    its sheet of pictures, without stopping at the picture's edge, so a uv named off the picture shows whatever picture lies beside
    its own in the sheet, which changes as the sheet is laid out. A face that names no uv takes one from where its piece lies, which
    is off the picture when the piece reaches past its block (a chair's back, a post, a candle); the mod's ModelUvRangeMixin (0.4.240)
    moves that one back when the game reads the model, the least way onto the picture or, past sixteen, squeezing the whole picture
    onto it. Here the span is slid by the whole pictures that keep the most of it on the picture, as if the picture went on
    repeating (a span wholly off it comes on whole); what still lies across an edge is then moved the least way onto it, as the
    mixin moves it. One longer than the picture (only a uv named so long: _cut has already cut every longer piece a face with no
    uv runs along) is fitted to it. (0.4.650; the King's tool's own rule.)"""
    lo, hi = min(a, b), max(a, b)
    if lo >= -1e-6 and hi <= 16 + 1e-6:
        return a, b
    if hi - lo > 16:
        return (0, 16) if a < b else (16, 0)
    slides = [-16 * k for k in range(math.floor(lo / 16) - 1, math.floor(hi / 16) + 2)]
    slide = max(slides, key=lambda s: (round(min(hi + s, 16) - max(lo + s, 0), 6), -abs(s)))
    lo, hi = lo + slide, hi + slide
    nudge = -lo if lo < 0 else 16 - hi if hi > 16 else 0
    return a + slide + nudge, b + slide + nudge


# Which of a piece's faces a model axis runs across, by the face's own two ways (u, v) as the game maps a face that names no uv.
_ACROSS = {0: (('down', 0), ('up', 0), ('north', 0), ('south', 0)), 1: (('north', 1), ('south', 1), ('west', 1), ('east', 1)),
           2: (('down', 1), ('up', 1), ('west', 0), ('east', 0))}
_CAPS = {0: ('west', 'east'), 1: ('down', 'up'), 2: ('north', 'south')}


# The order in which the game bakes a face's four corners (its FaceInfo), each corner by the end of the piece it lies at on x, y
# and z (0 the "from" end, 1 the "to" end). A face's uv is laid on these corners as BlockFaceUV does: the corner's index, turned by
# the face's rotation, takes u from the uv's first or third number and v from its second or fourth.
_VERTS = {'down': ((0, 0, 1), (0, 0, 0), (1, 0, 0), (1, 0, 1)), 'up': ((0, 1, 0), (0, 1, 1), (1, 1, 1), (1, 1, 0)),
          'north': ((1, 1, 0), (1, 0, 0), (0, 0, 0), (0, 1, 0)), 'south': ((0, 1, 1), (0, 0, 1), (1, 0, 1), (1, 1, 1)),
          'west': ((0, 1, 0), (0, 0, 0), (0, 0, 1), (0, 1, 1)), 'east': ((1, 1, 1), (1, 0, 1), (1, 0, 0), (1, 1, 0))}


def _uv_at(uv, turn, k):
    s = (k + turn // 90) % 4
    return uv[0] if s in (0, 1) else uv[2], uv[1] if s in (0, 3) else uv[3]


def _part(k, f, axis, t0, t1):
    """The uv a face names, for the part of its piece that runs from t0 to t1 of the way along axis (0 at the piece's "from" end):
    the same picture, cut where the piece is cut, so that the parts side by side show it once, as the whole face did."""
    uv, turn, verts = f['uv'], f.get('rotation', 0), _VERTS[k]
    corners = [_uv_at(uv, turn, i) for i in range(4)]
    laid = []
    for i, v in enumerate(verts):
        j = next(j for j, w in enumerate(verts) if w[axis] != v[axis] and all(w[a] == v[a] for a in range(3) if a != axis))
        near, far = (corners[i], corners[j]) if v[axis] == 0 else (corners[j], corners[i])
        t = t0 if v[axis] == 0 else t1
        laid.append((near[0] + (far[0] - near[0]) * t, near[1] + (far[1] - near[1]) * t))
    out = [0.0] * 4
    for i in range(4):
        s = (i + turn // 90) % 4
        if s == 0:
            out[0], out[1] = laid[i]
        elif s == 2:
            out[2], out[3] = laid[i]
    return [_number(c) for c in out]


def _cut(e):
    """A piece longer than its picture along some way, where a face that names no uv runs along it: cut where the picture would
    begin again (every sixteen, as the block's own grid), so that each part maps its picture one to one and the picture carries
    on across the cut as if it repeated. The parts keep the piece's turn, light and every face but the ones the cut makes inside
    it; a face that names its own uv names its share of it on each part, so the parts show its picture once. A piece needing no
    cut gives [e]."""
    pieces = [e]
    for axis in range(3):
        lo, hi = sorted((e['from'][axis], e['to'][axis]))
        if hi - lo <= 16 + 1e-6 or not any(k in e.get('faces', {}) and 'uv' not in e['faces'][k] for k, _ in _ACROSS[axis]):
            continue
        cuts = [c for c in range(int(math.floor(lo / 16)) * 16 + 16, int(math.ceil(hi / 16)) * 16, 16) if lo + 1e-6 < c < hi - 1e-6]
        out = []
        for p in pieces:
            bounds = [lo] + cuts + [hi]
            for i in range(len(bounds) - 1):
                q = copy.deepcopy(p)
                q['from'][axis], q['to'][axis] = _number(bounds[i]), _number(bounds[i + 1])
                # A face that names its own uv keeps its picture whole across the parts: each part names its share of it.
                for k, _ in _ACROSS[axis]:
                    f = q['faces'].get(k)
                    if f is not None and 'uv' in f:
                        f['uv'] = _part(k, f, axis, (bounds[i] - lo) / (hi - lo), (bounds[i + 1] - lo) / (hi - lo))
                low, high = _CAPS[axis]
                if i > 0:
                    q['faces'].pop(low, None)
                if i < len(bounds) - 2:
                    q['faces'].pop(high, None)
                if q['faces']:
                    out.append(q)
        pieces = out
    return pieces


def fitted(parts):
    """A model's pieces with every face's mapping on its own picture: a piece longer than its picture is cut where the picture would
    begin again (_cut); then a face that names no uv is given one where the uv the game would give it (from where its piece lies)
    leaves the picture, and a uv named off the picture is brought onto it (_onto). Copies the cure laid are left as they are, to be
    laid afresh. Gives back the same list when nothing needs it."""
    out, changed = [], False
    for e in parts:
        if e.get('__comment') == LAID:
            out.append(e); continue
        pieces = _cut(e)
        if len(pieces) > 1:
            changed = True
        for piece in pieces:
            fixed = None
            for k, f in piece.get('faces', {}).items():
                uv = f.get('uv') or _lies(piece, k)
                (u0, u1), (v0, v1) = _onto(uv[0], uv[2]), _onto(uv[1], uv[3])
                if (u0, v0, u1, v1) == tuple(uv):
                    continue
                if fixed is None:
                    fixed = dict(piece); fixed['faces'] = dict(piece['faces'])
                face = dict(f); face['uv'] = [_number(c) for c in (u0, v0, u1, v1)]
                fixed['faces'][k] = face
            if fixed is not None:
                changed = True
            out.append(fixed if fixed is not None else piece)
    return out if changed else parts


def settled(value):
    """A model as written, its faces kept on their pictures and its pieces settled; anything else as it is."""
    if isinstance(value, dict) and isinstance(value.get('elements'), list) and value['elements']:
        parts = settle(fitted(value['elements']))
        if parts is not value['elements']:
            value = dict(value); value['elements'] = parts
    return value


def dumps(value, **how):
    """json.dumps for the tools that write models: a model's pieces are settled on the way out, so no tool draws one that flickers."""
    return json.dumps(settled(value), **how)


def doubled(parts, pixels, cover=COVER):
    """The faces of one model drawn twice over: two with the very same corners but for how far forward they stand (no further
    apart than `cover`), showing the same picture laid the same way, where that picture is partly see-through. Drawn twice, such a
    face is darker than it was made. It is what the cure would make of a pane of water that clashes with something, so a model
    with one has to be cured by hand, in its tool. Each as (piece, face, piece, face)."""
    planes = {}
    for i, e in enumerate(parts):
        for k, f, pts, n in quads(e):
            planes.setdefault(tuple(round(c, 4) + 0.0 for c in n), (n, []))[1].append((_sum(n[j] * pts[0][j] for j in range(3)), i, k, f, pts))
    found = []
    for n, faces in planes.values():
        u, v = across(n); same = {}
        for d, i, k, f, pts in faces:
            where = tuple(sorted((round(_sum(q[j] * u[j] for j in range(3)), 4), round(_sum(q[j] * v[j] for j in range(3)), 4)) for q in pts))
            same.setdefault((where, f.get('texture'), tuple(pictured(parts[i], k, f))), []).append((d, i, k, f))
        for (where, name, uv), group in same.items():
            group.sort(key=lambda row: row[:3])
            for a in range(len(group)):
                for b in range(a + 1, len(group)):
                    if not 1e-6 < group[b][0] - group[a][0] <= cover + 1e-9:
                        continue
                    got = pixels(name or '')
                    if not got:
                        continue
                    w, h, px = got
                    us = [c[0] * w / 16 for c in uv]; vs = [c[1] * h / 16 for c in uv]
                    if any(CLEAR <= px[x, y][3] < 250 for x in range(max(0, int(min(us))), min(w, math.ceil(max(us))))
                           for y in range(max(0, int(min(vs))), min(h, math.ceil(max(vs))))):
                        found.append((group[a][1], group[a][2], group[b][1], group[b][2]))
    return found


# A face less than this across, either way, is a sliver: the edge of a label or a scrape laid on a surface a twenty-fifth to a
# hundredth of a pixel thick, or a copy the cure laid. Whether it draws anything or not, nobody sees it. Until 0.4.656 it was a whole
# pixel, so the half-pixel sides of a card, a rope, a note or a frayed end were never looked at, and ten models had sides that drew
# nothing (the Last Crossing's frayed coil among them, which the owner saw).
SLIVER = 0.05


def _span(e, k):
    (x0, y0, z0), (x1, y1, z1) = e['from'], e['to']
    return {'up': (x1 - x0, z1 - z0), 'down': (x1 - x0, z1 - z0), 'north': (x1 - x0, y1 - y0), 'south': (x1 - x0, y1 - y0),
            'east': (z1 - z0, y1 - y0), 'west': (z1 - z0, y1 - y0)}[k]


def blank(parts, pixels, sliver=SLIVER):
    """The faces of one model that draw nothing: at least `sliver` across both ways, and every pixel of their picture where they
    lie (pictured) clear (below CLEAR). Such a face is a hole where a piece should show its side, or a stalk, a cable, a flame
    left out, or a face that was never meant to show anything (the top of a weed, the edge of a pane in its frame): it is given
    the part of its picture it is meant to show, or left off. Only pictures in the pack are looked at; a face drawn with one of
    the game's own pictures is not. (0.4.651.) Each as (piece, face)."""
    found = []
    for i, e in enumerate(parts):
        if e.get('__comment') == LAID:
            continue
        for k, f in e.get('faces', {}).items():
            if min(abs(d) for d in _span(e, k)) < sliver - 1e-6:
                continue
            got = pixels(f.get('texture', ''))
            if not got:
                continue
            w, h, px = got
            uv = pictured(e, k, f)
            us = [c[0] * w / 16 for c in uv]; vs = [c[1] * h / 16 for c in uv]
            x0 = max(0, int(min(us) + 1e-6)); x1 = min(w, max(x0 + 1, math.ceil(max(us) - 1e-6)))
            y0 = max(0, int(min(vs) + 1e-6)); y1 = min(h, max(y0 + 1, math.ceil(max(vs) - 1e-6)))
            if not any(px[x, y][3] >= CLEAR for x in range(x0, x1) for y in range(y0, y1)):
                found.append((i, k))
    return found


# ------------------------------------------------------------------------------------------------------------------ the whole pack
class Pictures:
    """The pack's pictures, read once each: by name ('the_oldest_house:block/x'), as (width, height, the pixels) or None."""

    def __init__(self, assets=ASSETS):
        self.assets = Path(assets); self.read = {}

    def open(self, name):
        if name not in self.read:
            space, _, path = name.rpartition(':')
            file = self.assets / (space or 'minecraft') / 'textures' / (path + '.png')
            got = None
            if file.is_file():
                from PIL import Image
                im = Image.open(file).convert('RGBA')
                w, h = im.size
                moving = file.with_name(file.name + '.mcmeta')
                if moving.is_file():                           # a picture that moves is its frames one under another: the first
                    frame = json.loads(moving.read_text()).get('animation')
                    if frame is not None:
                        w, h = frame.get('width', w), frame.get('height', frame.get('width', w))
                got = (w, h, im.load(), im)
            self.read[name] = got
        got = self.read[name]
        return got[:3] if got else None

    def of(self, textures):
        """A model's way from the name a face uses ('#side') to its picture."""
        def find(ref):
            for _ in range(16):
                if not ref.startswith('#'):
                    return self.open(ref)
                ref = textures.get(ref[1:], '')
            return None
        return find


def named(ref):
    return ref if ':' in ref else 'minecraft:' + ref


def files(assets=ASSETS):
    """Every model in the pack: its name ('the_oldest_house:block/x') and where it lies."""
    for space in sorted(Path(assets).iterdir()):
        models = space / 'models'
        if models.is_dir():
            for f in sorted(models.rglob('*.json')):
                yield f'{space.name}:{f.relative_to(models).with_suffix("").as_posix()}', f


def pack(assets=ASSETS):
    """Every model in the pack by name, as written."""
    return {name: json.loads(f.read_text()) for name, f in files(assets)}


def whole(name, models):
    """A model's pieces and pictures as the game puts them together: the nearest pieces up its line of parents, and every picture
    named along it, a child's over its parent's. No pieces (a parent of the game's own, or none) gives ([], pictures)."""
    parts, textures, seen = None, {}, set()
    while name in models and name not in seen:
        seen.add(name); m = models[name]
        if parts is None and m.get('elements'):
            parts = m['elements']
        for key, value in m.get('textures', {}).items():
            textures.setdefault(key, value)
        if not m.get('parent'):
            break
        name = named(m['parent'])
    return parts or [], textures


def scan(names=None, assets=ASSETS, close=6):
    """Every model of the pack (or the named ones) with faces that flicker, and every one with a see-through face laid over itself:
    name -> its pairs, twice."""
    models = pack(assets); pictures = Pictures(assets); flicker, twice_drawn = {}, {}
    for name in sorted(names or models):
        parts, textures = whole(name, models)
        if not parts:
            continue
        got = fights(parts, pixels=pictures.of(textures), close=close)
        if got:
            flicker[name] = got
        got = doubled(parts, pictures.of(textures))
        if got:
            twice_drawn[name] = got
    return flicker, twice_drawn


WRITTEN = ({'indent': 2}, {'indent': 1}, {'indent': 4}, {'separators': (',', ':')}, {}, {'indent': 2, 'ensure_ascii': False}, {'indent': '\t'})


def cure(assets=ASSETS):
    """Settles every model in the pack where it lies, each written the way it was written before. Gives the names of those it changed
    and of those whose way of being written it could not tell (those are written two spaces in)."""
    changed, strange = [], []
    for name, file in files(assets):
        text = file.read_text(); model = json.loads(text)
        if not model.get('elements'):
            continue
        parts = settle(fitted(model['elements']))
        if parts is model['elements']:
            continue
        how = next(((kind, tail) for kind in WRITTEN for tail in ('\n', '') if json.dumps(model, **kind) + tail == text), None)
        if how is None:
            strange.append(name); how = (WRITTEN[0], '\n')
        model['elements'] = parts
        file.write_text(json.dumps(model, **how[0]) + how[1]); changed.append(name)
    return changed, strange


def main(argv):
    names = [a for a in argv if not a.startswith('--')]
    if names:
        models = pack()
        for name in names:
            full = name if ':' in name else 'the_oldest_house:' + name
            parts, textures = whole(full, models)
            got = fights(parts, pixels=Pictures().of(textures))
            print(f'{full}: {len(parts)} pieces, {len(got)} pairs that flicker, {sum(g[4] for g in got):.2f} square pixels; the cure would lay '
                  f'{len(settle(parts)) - len(parts)} faces')
            for ia, ka, ib, kb, area in sorted(got, key=lambda g: -g[4]):
                print(f'  {area:8.3f}  piece {ia} {ka} {parts[ia]["from"]}..{parts[ia]["to"]} {parts[ia]["faces"][ka].get("texture")}'
                      f'  against  piece {ib} {kb} {parts[ib]["from"]}..{parts[ib]["to"]} {parts[ib]["faces"][kb].get("texture")}')
        return 0
    if '--cure' in argv:
        changed, strange = cure()
        print(f'Settled {len(changed)} models.' + (f' Could not tell how {len(strange)} were written: {", ".join(strange[:8])}' if strange else ''))
        return 0
    found, twice_drawn = scan()
    # 0.4.650: a face mapped off its picture shows whatever lies beside it in the game's sheet, or (one that names no uv) is shifted
    # or squeezed back onto it by ModelUvRangeMixin; fitted (_cut, _onto) maps it one pixel to one pixel on its own picture.
    off = [name for name, model in pack().items() if model.get('elements') and fitted(model['elements']) is not model['elements']]
    for name in off:
        print(f'a face is mapped off its own picture (or a piece is longer than its picture): {name}')
    if off:
        print(f'{len(off)} models have faces mapped off their pictures. A tool that writes its models with flicker.dumps keeps them on; '
              f'python3 tools/flicker.py --cure keeps the pack on as it lies.')
        if '--check' in argv:
            return 1
    # 0.4.651: a face that draws nothing is a hole in its piece, or a part left out; it is given its picture, or left off.
    models, pictures, empty = pack(), Pictures(), {}
    for name in sorted(models):
        parts, textures = whole(name, models)
        got = blank(parts, pictures.of(textures)) if parts else []
        if got:
            empty[name] = got
    for name, faces in empty.items():
        print(f'{len(faces):3d} faces draw nothing: {name} ({", ".join(f"piece {i} {k}" for i, k in faces[:6])}{", ..." if len(faces) > 6 else ""})')
    if empty:
        print(f'{len(empty)} models have faces that draw nothing: each is given the part of its picture it is meant to show, '
              f'or left off, in the tool that draws it.')
        if '--check' in argv:
            return 1
    for name in sorted(found, key=lambda n: -sum(g[4] for g in found[n])):
        print(f'{sum(g[4] for g in found[name]):9.2f} square pixels in {len(found[name]):3d} pairs of faces flicker: {name}')
    for name in sorted(twice_drawn):
        print(f'a see-through face is laid over itself ({len(twice_drawn[name])}): {name}')
    if found or twice_drawn:
        print(f'{len(found)} models have faces that flicker; {len(twice_drawn)} have a see-through face laid over itself. A tool that writes '
              f'its models with flicker.dumps settles them; python3 tools/flicker.py --cure settles the pack as it lies.')
        return 1 if '--check' in argv else 0
    print('No model in the pack has faces that flicker.')
    return 0


if __name__ == '__main__':
    sys.exit(main(sys.argv[1:]))
