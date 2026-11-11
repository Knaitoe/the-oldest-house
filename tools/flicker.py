"""Which faces of a model would flicker against each other (0.4.463).

Two faces that lie in one plane, face the same way and overlap are drawn at the same depth, and where they are not the same colour the
eye sees one, then the other, in a crawling pattern that changes as it moves: on a still picture of the model there is nothing to
see. A model's pieces overlap all the time (a tube into a tube, a wheel behind a frame), and it only matters where both faces show
and differ, so this looks for exactly that: fights(parts) gives every such pair in one model's pieces.

The tools that build things from bars use it and refuse a model that has any (tools/draw_bike_night_art.py for every model it
draws, tools/draw_borrasca_town_art.py for its bike). The cure is always the same: one of the two pieces a hair proud of the other,
or stopped where the other begins.
"""
import math

NORMAL = {'north': (0, 0, -1), 'south': (0, 0, 1), 'west': (-1, 0, 0), 'east': (1, 0, 0), 'down': (0, -1, 0), 'up': (0, 1, 0)}


def turned(p, r):
    """A point about the origin, turned as a model's piece is."""
    if not r:
        return p
    a = math.radians(r['angle']); c, s = math.cos(a), math.sin(a)
    x, y, z = p
    if r['axis'] == 'z':
        return (x * c - y * s, x * s + y * c, z)
    if r['axis'] == 'x':
        return (x, y * c - z * s, y * s + z * c)
    return (x * c + z * s, y, -x * s + z * c)


def quads(e):
    """Each face a piece shows: its name, what it is drawn with, its four corners where the piece's turn puts them, and the way it faces."""
    (x0, y0, z0), (x1, y1, z1) = e['from'], e['to']
    r = e.get('rotation'); o = r['origin'] if r else (0, 0, 0)
    at = {'north': [(x0, y0, z0), (x1, y0, z0), (x1, y1, z0), (x0, y1, z0)], 'south': [(x0, y0, z1), (x1, y0, z1), (x1, y1, z1), (x0, y1, z1)],
          'west': [(x0, y0, z0), (x0, y0, z1), (x0, y1, z1), (x0, y1, z0)], 'east': [(x1, y0, z0), (x1, y0, z1), (x1, y1, z1), (x1, y1, z0)],
          'down': [(x0, y0, z0), (x1, y0, z0), (x1, y0, z1), (x0, y0, z1)], 'up': [(x0, y1, z0), (x1, y1, z0), (x1, y1, z1), (x0, y1, z1)]}
    for k, f in e['faces'].items():
        pts = []
        for q in at[k]:
            t = turned((q[0] - o[0], q[1] - o[1], q[2] - o[2]), r)
            pts.append((t[0] + o[0], t[1] + o[1], t[2] + o[2]))
        yield k, f, pts, turned(NORMAL[k], r)


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
    return sum(poly[i][0] * poly[(i + 1) % len(poly)][1] - poly[(i + 1) % len(poly)][0] * poly[i][1] for i in range(len(poly)))


def look(f):
    """What a face is drawn with. Two faces drawn from the same one pixel are the same flat colour and cannot be told apart."""
    uv = f.get('uv', [0, 0, 16, 16])
    one = 0 < uv[2] - uv[0] <= 1 and 0 < uv[3] - uv[1] <= 1 and uv[0] == int(uv[0]) and uv[1] == int(uv[1])
    return (f['texture'], tuple(uv) if one else None)


def fights(parts, near=.02, least=.003):
    """The faces of one model that would flicker: two of different pieces that face the same way, lie within `near` of one plane,
    cover more than `least` of each other and are not the same flat colour. Each as (piece, face, piece, face, how much they share)."""
    planes = {}
    for i, e in enumerate(parts):
        for k, f, pts, n in quads(e):
            d = sum(n[j] * pts[0][j] for j in range(3))
            planes.setdefault(tuple(round(c, 4) + 0.0 for c in n), []).append((d, i, k, f, pts))
    found = []
    for n, faces in planes.items():
        ref = (1, 0, 0) if abs(n[0]) < .9 else (0, 1, 0)
        u = (n[1] * ref[2] - n[2] * ref[1], n[2] * ref[0] - n[0] * ref[2], n[0] * ref[1] - n[1] * ref[0])
        m = math.sqrt(sum(c * c for c in u)); u = tuple(c / m for c in u)
        v = (n[1] * u[2] - n[2] * u[1], n[2] * u[0] - n[0] * u[2], n[0] * u[1] - n[1] * u[0])
        flat = []
        for d, i, k, f, pts in sorted(faces, key=lambda t: t[0]):
            poly = [(sum(q[j] * u[j] for j in range(3)), sum(q[j] * v[j] for j in range(3))) for q in pts]
            flat.append((d, i, k, look(f), poly if twice(poly) >= 0 else poly[::-1]))
        for a in range(len(flat)):
            da, ia, ka, la, pa = flat[a]
            for b in range(a + 1, len(flat)):
                db, ib, kb, lb, pb = flat[b]
                if db - da > near:
                    break
                if ia == ib or (la == lb and la[1] is not None):
                    continue
                shared = both(pa, pb)
                if len(shared) >= 3 and abs(twice(shared)) / 2 > least:
                    found.append((ia, ka, ib, kb, round(abs(twice(shared)) / 2, 3)))
    return found
