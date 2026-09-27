#!/usr/bin/env python3
"""Render an OTH-DUMP block listing of The Oldest House.

The structure game test (gradle runGameTestServer, also run in CI) prints the
generated house as `OTH-DUMP|` lines in the server log. This tool reads that
log (or any file containing the lines) and writes eye-level perspective
renders and floor plans, so the architecture can be reviewed without
launching the game.

    pip install numpy pillow
    python3 tools/render_house_dump.py <log-or-dump-file> <output-dir> [view ...]

Colours and shapes are approximations; this is a massing and layout check,
not a texture preview.
"""
import math
import os
import re
import sys

import numpy as np
from PIL import Image, ImageDraw

DIGITS = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"

def load(path):
    palette, layers, ents = {}, {}, []
    for raw in open(path, encoding='utf-8', errors='replace'):
        i = raw.find('OTH-DUMP|')
        if i < 0: continue
        parts = raw[i:].rstrip('\n').split('|')
        kind = parts[1]
        if kind == 'BEGIN':
            minx, miny, minz, sx, sy, sz = map(int, parts[2:8])
        elif kind == 'P':
            palette[int(parts[2])] = parts[3]
        elif kind == 'L':
            y, z, cells = int(parts[2]), int(parts[3]), parts[4]
            layers[(y, z)] = [DIGITS.index(cells[k]) * 62 + DIGITS.index(cells[k + 1]) for k in range(0, len(cells), 2)]
        elif kind == 'E':
            ents.append(parts[2:])
    blocks = {}
    for (y, z), row in layers.items():
        for k, idx in enumerate(row):
            s = palette[idx]
            if not s.startswith('air'):
                blocks[(minx + k, y, z)] = s
    return blocks, ents, (minx, miny, minz, sx, sy, sz)

def props(s):
    m = re.match(r'([a-z_]+)(?:\[(.*)\])?', s)
    d = {}
    if m.group(2):
        for kv in m.group(2).split(','):
            k, v = kv.split('='); d[k] = v
    return m.group(1), d

COL = {
 'white_terracotta': (214, 186, 168), 'stripped_dark_oak_log': (86, 60, 36), 'dark_oak_log': (60, 45, 28),
 'dark_oak_planks': (70, 46, 24), 'dark_oak': (72, 48, 26), 'deepslate_tile': (58, 58, 64), 'deepslate_tiles': (58, 58, 64),
 'stone_brick': (128, 127, 128), 'stone_bricks': (128, 127, 128), 'mossy_stone_bricks': (112, 124, 100), 'stone': (120, 120, 120),
 'bricks': (156, 92, 76), 'brick': (156, 92, 76), 'oak_planks': (170, 138, 84), 'spruce_planks': (120, 88, 52), 'spruce': (120, 88, 52),
 'oak': (170, 138, 84), 'glass_pane': (175, 215, 230), 'grass_block': (100, 160, 60), 'dirt': (134, 96, 67),
 'red_carpet': (165, 40, 36), 'brown_carpet': (118, 76, 44), 'green_carpet': (85, 110, 30), 'gray_carpet': (70, 74, 78),
 'light_blue_carpet': (60, 170, 215), 'lantern': (240, 190, 90), 'campfire': (230, 120, 40), 'bookshelf': (150, 110, 70),
 'chiseled_bookshelf': (150, 110, 70), 'barrel': (130, 95, 55), 'chest': (160, 115, 50), 'red_bed': (170, 40, 40),
 'blue_bed': (50, 60, 160), 'cyan_bed': (30, 130, 140), 'lightning_rod': (200, 110, 70), 'flower_pot': (140, 70, 50),
 'brick_wall': (156, 92, 76), 'stone_brick_wall': (128, 127, 128), 'chain': (60, 60, 70), 'ladder': (150, 115, 70),
}
def color(name):
    if name in COL: return COL[name]
    for suf in ('_stairs', '_slab', '_fence_gate', '_fence', '_trapdoor', '_door', '_pressure_plate', '_log'):
        if name.endswith(suf):
            base = name[:-len(suf)]
            if base in COL: return COL[base]
            if base.startswith('dark_oak'): return COL['dark_oak']
            if base.startswith('spruce'): return COL['spruce']
            if base.startswith('oak'): return COL['oak']
    if name.startswith('potted_'): return (140, 70, 50)
    if name.endswith('_carpet'): return (140, 140, 140)
    return (150, 150, 150)

def boxes(name, p):
    """Sub-boxes in block units (x0,y0,z0,x1,y1,z1)."""
    if name.endswith('_stairs'):
        top = p.get('half') == 'top'
        b = [(0, .5, 0, 1, 1, 1) if top else (0, 0, 0, 1, .5, 1)]
        y0, y1 = (0, .5) if top else (.5, 1)
        f = p.get('facing')
        b.append({'north': (0, y0, 0, 1, y1, .5), 'south': (0, y0, .5, 1, y1, 1), 'east': (.5, y0, 0, 1, y1, 1), 'west': (0, y0, 0, .5, y1, 1)}[f])
        return b
    if name.endswith('_slab'):
        t = p.get('type')
        return [(0, .5, 0, 1, 1, 1)] if t == 'top' else [(0, 0, 0, 1, 1, 1)] if t == 'double' else [(0, 0, 0, 1, .5, 1)]
    if name.endswith('_carpet'): return [(0, 0, 0, 1, .07, 1)]
    if name.endswith('_bed'): return [(0, 0, 0, 1, .56, 1)]
    if name in ('lantern',): return [(.31, .1 if p.get('hanging') == 'true' else 0, .31, .69, .6, .69)]
    if name in ('chain', 'lightning_rod'): return [(.44, 0, .44, .56, 1, .56)]
    if name == 'flower_pot' or name.startswith('potted_'): return [(.31, 0, .31, .69, .4, .69)]
    if name == 'candle': return [(.44, 0, .44, .56, .4, .56)]
    if name.endswith('_fence') or name.endswith('_wall'): return [(.35, 0, .35, .65, 1, .65)]
    if name == 'glass_pane': return [(0, 0, 0, 1, 1, 1)]
    if name.endswith('_trapdoor'):
        if p.get('open') == 'true':
            f = p.get('facing')
            return [{'north': (0, 0, .8, 1, 1, 1), 'south': (0, 0, 0, 1, 1, .2), 'east': (0, 0, 0, .2, 1, 1), 'west': (.8, 0, 0, 1, 1, 1)}[f]]
        return [(0, .8, 0, 1, 1, 1)] if p.get('half') == 'top' else [(0, 0, 0, 1, .2, 1)]
    if name.endswith('_pressure_plate'): return [(.06, 0, .06, .94, .06, .94)]
    if name == 'ladder': return []
    if name.endswith('_door'): return [(0, 0, 0, 1, 1, 1)]
    return [(0, 0, 0, 1, 1, 1)]


def shade(c, k):
    return tuple(int(v * k) for v in c)


def plan(blocks, y, scale=12, title=""):
    xs = [k[0] for k in blocks]; zs = [k[2] for k in blocks]
    x0, x1, z0, z1 = min(xs), max(xs), min(zs), max(zs)
    img = Image.new('RGB', ((x1 - x0 + 1) * scale + 20, (z1 - z0 + 1) * scale + 40), (250, 250, 250))
    d = ImageDraw.Draw(img)
    for x in range(x0, x1 + 1):
        for z in range(z0, z1 + 1):
            s = blocks.get((x, y, z)); below = blocks.get((x, y - 1, z))
            px, py = 10 + (x - x0) * scale, 30 + (z - z0) * scale
            if s is None:
                c = shade(color(props(below)[0]), 0.55) if below else (250, 250, 250)
                c = tuple(int(v * 0.35 + 250 * 0.65) for v in c)
            else:
                c = color(props(s)[0])
            d.rectangle([px, py, px + scale - 1, py + scale - 1], fill=c, outline=(215, 215, 215))
            if s and props(s)[0].endswith('_door'):
                d.rectangle([px + 2, py + 2, px + scale - 3, py + scale - 3], outline=(255, 0, 0))
    d.text((10, 8), title, fill=(20, 20, 20))
    return img


class Scene:
    def __init__(self, path):
        blocks, ents, dims = load(path)
        self.ents = ents
        xs = [k[0] for k in blocks]; ys = [k[1] for k in blocks]; zs = [k[2] for k in blocks]
        self.x0, self.y0, self.z0 = min(xs), min(ys), min(zs)
        sx, sy, sz = max(xs) - self.x0 + 1, max(ys) - self.y0 + 1, max(zs) - self.z0 + 1
        self.grid = np.zeros((sx, sy, sz), dtype=np.int32)
        names = sorted(set(blocks.values()))
        self.pal = ['air'] + names
        idx = {n: i + 1 for i, n in enumerate(names)}
        for (x, y, z), s in blocks.items():
            self.grid[x - self.x0, y - self.y0, z - self.z0] = idx[s]
        P = len(self.pal)
        self.full = np.zeros(P, bool)
        self.col = np.zeros((P, 3), np.float32)
        self.bx = np.zeros((P, 3, 6), np.float32)  # up to 3 boxes
        self.bx[:, :, 3:] = -1  # empty boxes (max < min)
        self.glass = np.zeros(P, bool)
        for i, s in enumerate(self.pal):
            if i == 0: continue
            n, p = props(s)
            self.col[i] = color(n)
            bl = boxes(n, p)
            if n == 'glass_pane': self.glass[i] = True
            if bl == [(0, 0, 0, 1, 1, 1)]:
                self.full[i] = True
            for k, b in enumerate(bl[:3]):
                self.bx[i, k] = b
        self.shape = np.array([sx, sy, sz])

    def render(self, eye, yaw, pitch, W=720, H=450, fov=70.0, ground_y=0.0, sun=(-0.45, 0.8, -0.35)):
        # yaw: 0 looks toward +z (south); 90 toward -x (west) ... Minecraft style
        eye = np.array(eye, np.float64) - [self.x0, self.y0, self.z0]
        fy = math.radians(yaw); fp = math.radians(pitch)
        fwd = np.array([-math.sin(fy) * math.cos(fp), -math.sin(fp), math.cos(fy) * math.cos(fp)])
        right = np.cross(fwd, [0, 1, 0]); right /= np.linalg.norm(right)
        up = np.cross(right, fwd)
        t = math.tan(math.radians(fov) / 2)
        u = (np.arange(W) + 0.5) / W * 2 - 1
        v = (np.arange(H) + 0.5) / H * 2 - 1
        uu, vv = np.meshgrid(u * t * W / H, -v * t)
        d = fwd[None, None, :] + uu[..., None] * right[None, None, :] + vv[..., None] * up[None, None, :]
        d /= np.linalg.norm(d, axis=2, keepdims=True)
        d = d.reshape(-1, 3)
        N = d.shape[0]
        o = np.broadcast_to(eye, (N, 3)).copy()
        d = np.where(np.abs(d) < 1e-9, 1e-9, d)
        # advance rays to grid bounding box
        tb0 = (0 - o) / d; tb1 = (self.shape - o) / d
        tmin = np.max(np.minimum(tb0, tb1), axis=1); tmaxb = np.min(np.maximum(tb0, tb1), axis=1)
        tstart = np.maximum(tmin, 0) + 1e-6
        hitgrid = tmaxb > tstart
        p = o + d * tstart[:, None]
        cell = np.floor(p).astype(np.int64)
        cell = np.clip(cell, 0, self.shape - 1)
        step = np.sign(d).astype(np.int64)
        nextb = cell + (step > 0)
        tMax = tstart[:, None] + (nextb - p) / d
        tDelta = np.abs(1.0 / d)
        out_t = np.full(N, np.inf); out_c = np.zeros((N, 3), np.float32); out_n = np.zeros((N, 3), np.float32)
        active = np.where(hitgrid)[0]
        tentry = tstart.copy()
        glassmix = np.zeros(N, np.float32)
        for _ in range(400):
            if active.size == 0: break
            c = cell[active]
            inside = np.all((c >= 0) & (c < self.shape), axis=1)
            active = active[inside]; c = c[inside]
            if active.size == 0: break
            val = self.grid[c[:, 0], c[:, 1], c[:, 2]]
            nz = val > 0
            if nz.any():
                ai = active[nz]; vi = val[nz]
                te = tentry[ai]; tx = np.min(tMax[ai], axis=1)
                oo = o[ai]; dd = d[ai]; cc = c[nz].astype(np.float64)
                best = np.full(ai.size, np.inf); bestn = np.zeros((ai.size, 3))
                for k in range(3):
                    b = self.bx[vi, k]
                    lo = cc + b[:, :3]; hi = cc + b[:, 3:]
                    valid = b[:, 3] > b[:, 0]
                    t1 = (lo - oo) / dd; t2 = (hi - oo) / dd
                    tn3 = np.minimum(t1, t2); tf3 = np.maximum(t1, t2)
                    tn = np.max(tn3, axis=1); tf = np.min(tf3, axis=1)
                    ok = valid & (tn <= tf) & (tf >= te - 1e-6) & (tn <= tx + 1e-6)
                    tn = np.maximum(tn, te)
                    better = ok & (tn < best)
                    ax = np.argmax(tn3, axis=1)
                    nrm = np.zeros((ai.size, 3)); nrm[np.arange(ai.size), ax] = -np.sign(dd[np.arange(ai.size), ax])
                    best = np.where(better, tn, best); bestn = np.where(better[:, None], nrm, bestn)
                hit = np.isfinite(best)
                g = self.glass[vi] & hit
                # glass: tint and continue (partially transparent)
                if g.any():
                    gi = ai[g]
                    glassmix[gi] = np.minimum(0.55, glassmix[gi] + 0.35)
                    hit = hit & ~g
                hi_idx = ai[hit]
                out_t[hi_idx] = best[hit]; out_c[hi_idx] = self.col[vi[hit]]; out_n[hi_idx] = bestn[hit]
                active = np.setdiff1d(active, hi_idx, assume_unique=True)
            if active.size == 0: break
            tm = tMax[active]
            ax = np.argmin(tm, axis=1)
            r = np.arange(active.size)
            tentry[active] = tm[r, ax]
            cell[active, ax] += step[active, ax]
            tMax[active, ax] += tDelta[active, ax]
        # sky / ground for misses
        miss = ~np.isfinite(out_t)
        img = np.zeros((N, 3), np.float32)
        sky = np.array([185, 205, 225], np.float32)
        tg = (ground_y - self.y0 - o[:, 1]) / d[:, 1]
        gro = miss & (d[:, 1] < 0) & (tg > 0)
        img[miss] = sky
        grass = np.array([108, 150, 72], np.float32)
        img[gro] = grass * 0.85
        L = np.array(sun, np.float32); L /= np.linalg.norm(L)
        diff = np.clip((out_n * L).sum(1), 0, 1)
        shade = 0.55 + 0.45 * diff
        shade = np.where(out_n[:, 1] > 0.5, 0.95, shade)
        hitm = ~miss
        img[hitm] = out_c[hitm] * shade[hitm, None]
        # distance fog
        dist = np.where(hitm, out_t, np.where(gro, tg, 200))
        f = np.clip((dist - 30) / 170, 0, 0.6)[:, None]
        img = img * (1 - f) + sky * f
        gm = glassmix[:, None]
        img = img * (1 - gm) + np.array([170, 200, 215]) * gm
        return Image.fromarray(np.clip(img, 0, 255).reshape(H, W, 3).astype(np.uint8))


VIEWS = {
    'approach': ((15.5, 1.7, -24), 0, -6),
    'front_left': ((-12, 3, -16), -42, -8),
    'front_right': ((40, 3, -15), 45, -8),
    'rear_right': ((42, 3, 44), 135, -8),
    'rear_left': ((-14, 3, 44), -135, -8),
    'west_side': ((-26, 3, 12), -90, -8),
    'east_side': ((52, 3, 14), 90, -8),
    'hall_from_front_door': ((15.5, 2.62, 5.4), 0, 0),
    'great_room_hearth': ((12.4, 2.62, 8.0), 90, 5),
    'stair_tower': ((18.4, 2.62, 13.3), -20, -25),
    'study': ((11.5, 2.62, 20.5), 70, 5),
    'kitchen': ((18.4, 2.62, 6.5), -70, 8),
    'long_gallery': ((12.3, 8.62, 20.0), 90, 0),
}


def main(argv):
    if len(argv) < 3:
        print(__doc__)
        return 1
    path, out = argv[1], argv[2]
    os.makedirs(out, exist_ok=True)
    scene = Scene(path)
    for name, (eye, yaw, pitch) in VIEWS.items():
        if argv[3:] and name not in argv[3:]:
            continue
        scene.render(eye, yaw, pitch).save(os.path.join(out, name + '.png'))
    blocks, _, _ = load(path)
    for y, label in ((1, 'ground floor'), (7, 'upper floor'), (-4, 'cellar')):
        plan(blocks, y, title=f'{label} (y={y})').save(os.path.join(out, f'plan_y{y}.png'))
    print('wrote renders to', out)
    return 0


if __name__ == '__main__':
    sys.exit(main(sys.argv))
