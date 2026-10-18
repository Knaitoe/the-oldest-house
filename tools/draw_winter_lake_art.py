"""Winter Lake (0.4.104): the frozen lake's own blocks and her ribbon, drawn in code.

Black ice you can see down through, ice the wind has drifted over with snow, the two stages of a crack she knocks into
it from beneath, the cattails frozen round the hole that never froze, the boys' heap of snowballs, and the blue hair
ribbon Hank has carried since the shallows. The drawing helpers are Movie Night's.

Usage: python3 tools/draw_winter_lake_art.py
"""
import json

import numpy as np
from PIL import Image

from draw_movie_night_art import ASSETS, canvas, rect, px, ellipse, line, noise, element

T = 'the_oldest_house:block/'
KINDS = ('black_ice', 'snow_ice', 'cracked', 'split', 'cattails', 'snowballs')


def black_ice(seed):
    """Deep, nearly black water seen through clear ice: faint streaks, a few trapped bubbles, a pale rim."""
    ice = canvas(16, 16, (14, 24, 38, 178))
    for x0, y0, x1, y1 in ((1, 3, 9, 1), (4, 11, 14, 7), (0, 14, 6, 12), (9, 15, 15, 13)):
        line(ice, [(x0, y0), (x1, y1)], (46, 66, 90, 160))
    for x, y in ((3, 6), (11, 3), (7, 9), (13, 11), (2, 12)):
        px(ice, x, y, (196, 214, 230, 210))
    rect(ice, 0, 0, 16, 1, (64, 86, 110, 150))
    rect(ice, 0, 0, 1, 16, (64, 86, 110, 150))
    noise(ice, .05, seed)
    return ice


def crack(ice, lines, color=(226, 236, 246, 236)):
    for points in lines:
        line(ice, points, color)
    return ice


def textures():
    out = {'winter_black_ice': black_ice(1041)}
    out['winter_cracked'] = crack(black_ice(1042), [[(8, 8), (2, 3)], [(8, 8), (15, 6)], [(8, 8), (6, 15)], [(8, 8), (12, 13)], [(2, 3), (0, 5)], [(15, 6), (15, 1)]])
    split = crack(black_ice(1043), [[(8, 8), (0, 2)], [(8, 8), (15, 5)], [(8, 8), (5, 15)], [(8, 8), (13, 15)], [(8, 8), (9, 0)],
                                    [(4, 5), (1, 9)], [(12, 6), (14, 10)], [(6, 12), (2, 13)], [(11, 12), (15, 12)]])
    # The middle has given: a ragged dark gap where the water shows through.
    for x, y in ((7, 7), (8, 7), (7, 8), (8, 8), (9, 8), (8, 9), (6, 8)):
        px(split, x, y, (4, 8, 14, 230))
    out['winter_split'] = split
    # Snow the wind has packed onto the ice: opaque and quiet underfoot.
    snow = canvas(16, 16, (232, 238, 246))
    for y, x0, x1 in ((2, 1, 9), (6, 5, 15), (10, 0, 7), (13, 8, 15)):
        rect(snow, x0, y, x1, y + 1, (210, 220, 234))
    for x, y in ((12, 3), (3, 8), (10, 11), (6, 14)):
        px(snow, x, y, (250, 252, 255))
    noise(snow, .02, 1044)
    out['winter_snow_ice'] = snow
    side = canvas(16, 16, (36, 54, 76))
    rect(side, 0, 0, 16, 4, (232, 238, 246))
    rect(side, 0, 4, 16, 5, (196, 208, 224))
    for x, y in ((3, 9), (11, 12), (7, 14)):
        px(side, x, y, (150, 176, 200))
    noise(side, .04, 1045)
    out['winter_snow_ice_side'] = side
    under = canvas(16, 16, (30, 44, 62))
    for x, y in ((2, 4), (9, 2), (13, 9), (5, 12), (10, 14)):
        px(under, x, y, (120, 150, 178))
    noise(under, .05, 1046)
    out['winter_snow_ice_bottom'] = under
    # Cattails frozen where they stood: brown heads, pale stalks, frost on their tips.
    tails = canvas(16, 16)
    for x, top, lean in ((3, 3, 1), (7, 1, 0), (10, 4, -1), (13, 6, 0)):
        line(tails, [(x, 15), (x + lean, top + 5)], (150, 140, 104))
        rect(tails, x + lean, top + 1, x + lean + 1, top + 5, (86, 54, 34))
        px(tails, x + lean, top, (230, 236, 244))
        px(tails, x + lean, top + 1, (214, 222, 232))
    line(tails, [(5, 15), (2, 9)], (128, 120, 88))
    line(tails, [(11, 15), (14, 10)], (128, 120, 88))
    out['winter_cattails'] = tails
    balls = canvas(16, 16, (236, 240, 248))
    ellipse(balls, (2, 2, 13, 13), (248, 250, 255))
    rect(balls, 9, 9, 16, 16, (204, 214, 230))
    noise(balls, .03, 1047)
    out['winter_snowballs'] = balls
    return {k: Image.fromarray(np.clip(v, 0, 255).astype(np.uint8), 'RGBA') for k, v in out.items()}


def ribbon():
    """Her hair ribbon: a blue satin bow, the knot darker, the two tails cut on the slant."""
    r = canvas(16, 16)
    blue, deep, shine = (58, 104, 188), (34, 64, 132), (132, 172, 232)
    ellipse(r, (1, 3, 7, 9), blue)
    ellipse(r, (9, 3, 15, 9), blue)
    rect(r, 7, 4, 9, 8, deep)
    line(r, [(7, 8), (4, 14)], blue, 2)
    line(r, [(9, 8), (12, 14)], blue, 2)
    px(r, 3, 14, deep)
    px(r, 13, 14, deep)
    for x, y in ((3, 5), (11, 5), (4, 4), (12, 4)):
        px(r, x, y, shine)
    px(r, 2, 6, deep)
    px(r, 14, 6, deep)
    return Image.fromarray(np.clip(r, 0, 255).astype(np.uint8), 'RGBA')


def models():
    def cube(tex, render='minecraft:translucent'):
        return {'parent': 'minecraft:block/cube_all', 'render_type': render, 'textures': {'all': T + tex}}
    out = {'winter_black_ice': cube('winter_black_ice'), 'winter_cracked': cube('winter_cracked'), 'winter_split': cube('winter_split')}
    out['winter_snow_ice'] = {'parent': 'minecraft:block/cube_bottom_top',
                              'textures': {'top': T + 'winter_snow_ice', 'side': T + 'winter_snow_ice_side', 'bottom': T + 'winter_snow_ice_bottom'}}
    out['winter_cattails'] = {'parent': 'minecraft:block/cross', 'render_type': 'minecraft:cutout', 'textures': {'cross': T + 'winter_cattails'}}
    els = []
    for (x, z), size, y in (((2, 3), 4, 0), ((7, 2), 4, 0), ((11, 7), 4, 0), ((4, 9), 4, 0), ((8, 9), 3, 0), ((5, 5), 3, 4), ((9, 5), 3, 4)):
        els.append(element((x, y, z), (x + size, y + size, z + size), {'all': '#snow'}, [0, 0, 16, 16]))
    out['winter_snowballs'] = {'textures': {'snow': T + 'winter_snowballs', 'particle': T + 'winter_snowballs'}, 'elements': els}
    return out


def write():
    for name, img in textures().items():
        img.save(ASSETS / 'textures/block' / f'{name}.png')
    ribbon().save(ASSETS / 'textures/item/winter_ribbon.png')
    for name, model in models().items():
        (ASSETS / 'models/block' / f'{name}.json').write_text(json.dumps(model, indent=2) + '\n')
    (ASSETS / 'models/item/winter_ribbon.json').write_text(json.dumps({'parent': 'minecraft:item/generated', 'textures': {'layer0': 'the_oldest_house:item/winter_ribbon'}}, indent=2) + '\n')
    variants = {f'kind={kind}': {'model': T + 'winter_' + kind} for kind in KINDS}
    (ASSETS / 'blockstates/winter_prop.json').write_text(json.dumps({'variants': variants}, indent=2) + '\n')


if __name__ == '__main__':
    write()
