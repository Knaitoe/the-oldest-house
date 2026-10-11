#!/usr/bin/env python3
"""Reproducible 16-pixel environment art and collision-matched models for the 0.4.80 gray hazards.

Only hazard_* assets and the new quiet-walker atlas are written. Existing cast and room art is never touched.
"""
from pathlib import Path
import hashlib
import json
import random
from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / 'src/main/resources/assets/the_oldest_house'
KINDS = 'locker_wall frosted_crate compressor heat_lamp hook ship_plate rail sculk_boards maze_panel mirror_glass net parlour_wall narrow_arch skirt_gap number_four sconce cistern_tile pit_stone pressure_panel drift_tile'.split()
PALETTES = [(151,172,172),(130,137,128),(71,82,84),(99,88,60),(136,147,145),(72,99,104),(126,140,138),(46,42,37),(105,110,99),(161,186,188),(106,105,76),(171,151,114),(133,107,76),(117,91,64),(199,192,155),(89,72,49),(103,133,127),(59,67,69),(99,102,100),(98,108,114)]

def write_json(path, obj):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(obj, indent=2) + '\n')

def texture(kind, palette, dark=False):
    rng = random.Random('hazard_' + kind + ('_off' if dark else ''))
    image = Image.new('RGBA', (16,16))
    for y in range(16):
        for x in range(16):
            n = rng.choice((-9,-6,-3,0,0,2,5,8))
            image.putpixel((x,y), tuple(max(0,min(255,c+n)) for c in palette)+(255,))
    d=ImageDraw.Draw(image)
    steel=(60,75,77,255); edge=(41,51,51,255); frost=(219,236,230,255); warm=(250,172,72,255)
    if kind=='locker_wall':
        d.line((0,0,0,15),fill=steel);d.line((15,0,15,15),fill=frost);d.line((0,15,15,15),fill=steel)
        for y in (3,12):d.rectangle((2,y,3,y+1),fill=(117,138,140));d.point((2,y),fill=frost)
        d.line((9,4,9,11),fill=(176,195,191));d.line((10,6,12,6),fill=(176,195,191))
    elif kind=='frosted_crate':
        for y in (1,5,10,14):d.line((0,y,15,y),fill=(63,84,79))
        d.rectangle((1,0,3,15),fill=(101,112,99));d.rectangle((12,0,14,15),fill=(101,112,99))
        for x,y in ((2,2),(13,2),(2,13),(13,13)):d.point((x,y),fill=edge)
        for x,y in ((5,2),(9,8),(6,13),(13,5)):
            d.line((x-2,y,x+2,y),fill=frost);d.line((x,y-2,x,y+2),fill=frost);d.line((x-1,y-1,x+1,y+1),fill=(190,219,213))
        d.line((0,0,15,0),fill=frost);d.line((6,0,6,3),fill=frost)
    elif kind=='compressor':
        d.rectangle((1,1,14,14),outline=edge);d.rectangle((3,3,12,11),fill=(36,47,49))
        for y in (4,6,8,10):d.line((4,y,11,y),fill=(126,143,140))
        d.rectangle((3,13,8,13),fill=(155,159,138));d.point((12,13),fill=(209,98,65))
    elif kind in ('heat_lamp','sconce'):
        d.rectangle((1,1,14,14),fill=edge);d.rectangle((3,3,12,12),fill=(68,66,56) if dark else warm)
        for x in (4,7,10):d.line((x,2,x,13),fill=steel)
        if not dark:d.line((5,4,5,11),fill=(255,218,138));d.line((8,4,8,11),fill=(255,218,138))
        for x,y in ((1,1),(14,1),(1,14),(14,14)):d.point((x,y),fill=(171,173,153))
    elif kind in ('ship_plate','rail','hook'):
        d.rectangle((0,0,15,15),outline=steel);d.line((0,14,15,14),fill=edge)
        for x,y in ((2,2),(13,2),(2,13),(13,13)):d.rectangle((x,y,x+1,y+1),fill=(142,159,151));d.point((x+1,y+1),fill=edge)
        if kind=='ship_plate':d.line((4,7,11,7),fill=(51,77,83));d.line((3,8,10,8),fill=(116,132,124));d.point((1,11),fill=(132,82,55));d.point((2,10),fill=(132,82,55))
    elif kind=='sculk_boards':
        for x in (0,5,10,15):d.line((x,0,x,15),fill=(13,23,25));d.line((min(15,x+1),0,min(15,x+1),15),fill=(88,70,52))
        for x,y in ((3,4),(8,11),(13,5)):d.line((x,y,x-1,y+3),fill=(13,83,85));d.point((x,y+1),fill=(65,163,157));d.point((x-1,y+3),fill=(106,192,175))
        for x in (2,7,12):d.point((x,1),fill=(13,16,14));d.point((x,14),fill=(13,16,14))
    elif kind=='maze_panel':
        d.rectangle((0,0,15,15),outline=(47,54,49));d.line((3,0,3,15),fill=(128,132,116));d.line((12,0,12,15),fill=(76,82,75))
        for y in (2,13):d.rectangle((1,y,2,y+1),fill=(166,163,133));d.rectangle((13,y,14,y+1),fill=(166,163,133))
        d.line((8,4,7,8),fill=(88,96,86));d.point((8,9),fill=(88,96,86))
    elif kind=='mirror_glass':
        for y in range(16):
            for x in range(16):image.putpixel((x,y),(158+x*2,188+y,193+y,64 if 0<x<15 and 0<y<15 else 180))
        d=ImageDraw.Draw(image);d.line((1,14,14,1),fill=(227,239,231,95));d.line((2,14,14,2),fill=(222,238,232,55));d.rectangle((0,0,15,15),outline=(66,86,86,190))
    elif kind=='net':
        image=Image.new('RGBA',(16,16));d=ImageDraw.Draw(image)
        for n in (0,4,8,12,15):d.line((n,0,n,15),fill=(152,142,97,255));d.line((0,n,15,n),fill=(107,105,67,255))
        for x in (0,4,8,12):
            for y in (0,4,8,12):d.rectangle((x,y,x+1,y+1),fill=(190,168,118,255))
    elif kind in ('parlour_wall','narrow_arch','skirt_gap'):
        for y in (2,7):
            for x in (2,7,12):d.point((x,y),fill=(193,179,136));d.point((x+1,y+1),fill=(132,129,93));d.point((x+2,y),fill=(193,179,136))
        d.rectangle((0,11,15,15),fill=(86,68,49));d.line((0,11,15,11),fill=(179,147,94));d.line((0,13,15,13),fill=(57,48,39))
        for x in (2,8,14):d.line((x,14,x,15),fill=(111,82,49))
    elif kind=='number_four':
        d.rectangle((0,0,15,15),fill=(58,59,53));d.rectangle((1,1,14,14),fill=(207,197,157));d.rectangle((2,2,13,13),outline=(136,128,92))
        glyph=['101','101','111','001','001']
        for y,row in enumerate(glyph):
            for x,bit in enumerate(row):
                if bit=='1':d.rectangle((5+x*2,3+y*2,6+x*2,4+y*2),fill=(34,35,32))
        for x,y in ((1,1),(14,1),(1,14),(14,14)):d.point((x,y),fill=(245,233,181))
    elif kind=='cistern_tile':
        for n in (0,8,15):d.line((0,n,15,n),fill=(56,82,80));d.line((n,0,n,15),fill=(56,82,80))
        d.line((1,1,6,1),fill=(174,193,176));d.line((9,9,14,9),fill=(174,193,176));d.point((3,14),fill=(75,105,91))
    elif kind=='pit_stone':
        for y in (0,8,15):d.line((0,y,15,y),fill=(24,31,34))
        d.line((7,0,7,7),fill=(24,31,34));d.line((3,8,3,15),fill=(24,31,34));d.line((12,8,12,15),fill=(24,31,34))
        d.line((8,1,14,1),fill=(90,99,97));d.line((4,9,10,9),fill=(90,99,97))
    elif kind=='pressure_panel':
        d.rectangle((0,0,15,15),outline=(42,46,44));d.rectangle((2,2,13,13),outline=(140,143,130))
        for x in (4,7,10):d.line((x,4,x,11),fill=(52,61,57))
        d.point((2,2),fill=(195,166,99));d.point((13,13),fill=(195,166,99))
    elif kind=='drift_tile':
        for n in range(16):
            if n%4==0:d.line((0,n,15,n),fill=(59,79,85));d.line((n,0,n,15),fill=(59,79,85))
        d.rectangle((6,6,9,9),outline=(153,169,161));d.point((7,5),fill=(189,190,151))
    return image

def element(a,b,tex):
    return {'from':a,'to':b,'faces':{face:{'texture':'#all','uv':[0,0,16,16]} for face in ('down','up','north','south','west','east')}}

def model(kind, off=False, small=False):
    name='hazard_'+kind+('_off' if off else '')
    tex='the_oldest_house:block/'+name
    boxes=[([0,0,0],[16,16,16])]
    if kind=='rail':boxes=[([6,0,6],[10,16,10]),([0,11,6],[16,14,10])]
    elif kind=='hook':boxes=[([7,8,7],[9,16,9]),([7,5,7],[13,8,9])]
    elif kind=='number_four':boxes=[([14,0,0],[16,16,16])]
    elif kind in ('net','sculk_boards'):boxes=[([0,14,0],[16,16,16])]
    elif kind=='narrow_arch':boxes=[([0,0,0],[4,16,16]),([12,0,0],[16,16,16]),([4,12,0],[12,16,16])] if small else [([0,0,0],[3,16,16]),([13,0,0],[16,16,16]),([3,15,0],[13,16,16])]
    elif kind=='skirt_gap':boxes=[([0,0,0],[5,16,16]),([11,0,0],[16,16,16]),([5,10,0],[11,16,16])]
    return {'ambientocclusion':kind not in ('mirror_glass','heat_lamp','sconce'),'render_type':'minecraft:translucent' if kind=='mirror_glass' else 'minecraft:cutout','textures':{'all':tex,'particle':tex},'elements':[element(a,b,tex) for a,b in boxes]}

def main():
    manifest=[];variants={}
    for kind,palette in zip(KINDS,PALETTES):
        for stage in (0,1):
            off=stage==1 and kind in ('heat_lamp','sconce')
            name='hazard_'+kind+('_off' if off else '')
            if kind=='narrow_arch' and stage==1:
                variants[f'kind={kind},stage={stage}']={'model':'the_oldest_house:block/hazard_narrow_arch_small'}
                write_json(ASSETS/'models/block/hazard_narrow_arch_small.json',model(kind,small=True))
                continue
            variants[f'kind={kind},stage={stage}']={'model':'the_oldest_house:block/'+name}
            if stage and not off:continue
            path=ASSETS/'textures/block'/f'{name}.png';path.parent.mkdir(parents=True,exist_ok=True);texture(kind,palette,off).save(path)
            write_json(ASSETS/'models/block'/f'{name}.json',model(kind,off))
            manifest.append({'file':str(path.relative_to(ROOT)),'size':[16,16],'sha256':hashlib.sha256(path.read_bytes()).hexdigest()})
    write_json(ASSETS/'blockstates/hazard_piece.json',{'variants':variants})
    # A faceless, elongated native humanoid, classic 128x64 atlas. Extensions stay invisible.
    figure=Image.new('RGBA',(128,64));d=ImageDraw.Draw(figure);rng=random.Random(480)
    for x0,y0,w,h in ((0,0,32,16),(16,16,24,16),(40,16,16,16),(0,16,16,16)):
        for y in range(y0,y0+h):
            for x in range(x0,x0+w):
                c=rng.choice((17,20,24,28,32));figure.putpixel((x,y),(c,c+4,c+3,255))
    d.line((20,20,20,31),fill=(53,58,47));d.line((22,20,22,31),fill=(8,12,11));d.line((46,20,46,30),fill=(43,50,43));d.line((6,20,6,30),fill=(43,50,43))
    path=ASSETS/'textures/entity/literary_quiet_walker.png';figure.save(path);manifest.append({'file':str(path.relative_to(ROOT)),'size':[128,64],'sha256':hashlib.sha256(path.read_bytes()).hexdigest()})
    write_json(ROOT/'art/inventory/hazards_0480.json',{'layout':41,'slots':[78,93],'textures':manifest})
    print(f'{len(manifest)} textures, 23 collision-matched models, {len(variants)} block states; existing art preserved')

if __name__=='__main__':main()
