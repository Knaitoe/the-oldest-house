#!/usr/bin/env python3
"""Deterministic native block models, pixel materials and a legible scratched bitmap hand."""
import json
import random
from pathlib import Path
from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / 'src/main/resources/assets/the_oldest_house'


def write_json(path, value):
    destination = ASSETS / path
    destination.parent.mkdir(parents=True, exist_ok=True)
    destination.write_text(json.dumps(value, indent=2) + '\n')


def material(name, base, mode):
    rng = random.Random(name)
    image = Image.new('RGB', (32, 32))
    for y in range(32):
        for x in range(32):
            noise = rng.randrange(-5, 6)
            if mode == 'wood': noise += ((x * 3 + y // 9) % 7 == 0) * -12
            if mode == 'cloth': noise += (x + y) % 2 * 5 - 2
            image.putpixel((x, y), tuple(max(0, min(255, c + noise)) for c in base))
    d = ImageDraw.Draw(image)
    if name == 'cane':
        for n in range(0, 32, 4):
            d.line((n, 0, n, 31), fill=(152, 121,  70))
            d.line((0, n, 31, n), fill=(115, 91, 51))
            for m in range(0, 32, 4): d.point((n + 1, m + 1), fill=(194, 167, 104))
    if name == 'floral':
        for x, y in [(5, 7), (21, 5), (13, 23), (29, 25)]:
            d.line((x, y, x+3, y+6), fill=(87, 107, 76), width=1)
            for dx, dy in [(-2, 0), (2, 0), (0, -2), (0, 2)]:
                d.rectangle((x+dx-1, y+dy-1, x+dx+1, y+dy+1), fill=(146, 91, 92))
            d.point((x, y), fill=(191, 166, 106))
    if name == 'blue_weave':
        for n in (6, 22):
            d.line((n, 0, n, 31), fill=(105, 118, 127))
            d.line((0, n, 31, n), fill=(76, 93, 107))
    if name == 'green_cloth':
        d.line((3, 27, 16, 27, 20, 29, 26, 29), fill=(85, 89, 61))
        for n in range(3, 14, 2): d.point((n, 26), fill=(135, 131, 89))
    if name == 'formica':
        d.line((2, 25, 17, 24), fill=(152, 73, 62))
        d.line((24, 3, 28, 8), fill=(210, 154, 127))
        d.rectangle((0, 0, 31, 1), fill=(105, 103, 99))
    if name in ('drawer_front', 'bedside_front'):
        for y in ([2, 10, 18, 26] if name == 'drawer_front' else [7, 22]):
            d.line((2, y, 29, y), fill=(42, 30, 24))
            d.line((2, min(y+1, 31), 29, min(y+1, 31)), fill=(136, 97, 62))
            d.rectangle((12, min(y+4, 30), 19, min(y+5, 31)), fill=(154, 135, 84))
    if name == 'washer_front':
        d.rectangle((1, 1, 30, 6), fill=(185, 180, 165))
        d.rectangle((3, 2, 12, 4), fill=(133, 128, 116))
        d.ellipse((22, 2, 27, 5), fill=(87, 85, 80))
        d.ellipse((5, 9, 27, 29), fill=(114, 113, 103))
        d.ellipse((7, 11, 25, 27), fill=(205, 203, 184))
        d.ellipse((9, 13, 23, 25), fill=(51, 63, 64))
        d.polygon([(10, 21), (15, 18), (20, 21), (21, 24), (12, 24)], fill=(112, 122, 116))
        d.line((10, 14, 14, 12), fill=(155, 173, 164))
        d.rectangle((1, 30, 30, 31), fill=(118, 110, 89))
    if name == 'paper':
        for y in range(6, 29, 4): d.line((3, y, 29, y), fill=(168, 170, 163))
        d.line((6, 2, 6, 29), fill=(164, 108, 99))
        for x, y in [(9, 6), (12, 10), (10, 14), (11, 18)]:
            d.line((x, y-1, x+11, y-1), fill=(87, 82, 76))
            d.line((x+3, y-2, x+7, y-2), fill=(110, 103, 94))
    if name == 'letter':
        d.line((0, 16, 31, 16), fill=(170, 156, 123))
        for y in (5, 8, 11, 22, 25):
            d.line((5, y, 24-(y%3)*2, y), fill=(95, 85, 70))
        d.line((28, 1, 30, 6), fill=(190, 172, 130))
    image.save(ASSETS / 'textures/block' / (name+'.png'))


(ASSETS/'textures/block').mkdir(parents=True, exist_ok=True)
for name, base, mode in [
    ('walnut', (93, 66, 47), 'wood'), ('drawer_front', (92, 65, 45), 'wood'),
    ('bedside_front', (112, 79, 49), 'wood'), ('cane', (176, 144, 87), 'wood'),
    ('green_cloth', (83, 105, 79), 'cloth'), ('floral', (173, 161, 133), 'cloth'),
    ('blue_weave', (68, 88, 110), 'cloth'), ('formica', (185, 111, 87), 'plain'),
    ('enamel', (211, 205, 181), 'plain'), ('metal_legs', (107, 105,  90), 'plain'),
    ('washer_front', (218, 211, 190), 'plain'), ('paper', (226, 216, 183), 'plain'),
    ('letter', (222, 207, 168), 'plain'), ('notebook_cover', (87, 45, 43), 'cloth')]:
    material(name, base, mode)


def cube(box, texture, front=None):
    x0,y0,z0,x1,y1,z1 = box
    coords = {'north':[16-x1,16-y1,16-x0,16-y0], 'south':[x0,16-y1,x1,16-y0],
              'west':[z0,16-y1,z1,16-y0], 'east':[16-z1,16-y1,16-z0,16-y0],
              'up':[x0,z0,x1,z1], 'down':[x0,16-z1,x1,16-z0]}
    faces={f:{'uv':uv,'texture':'#'+('front' if front and f=='north' else texture)} for f,uv in coords.items()}
    return {'from':box[:3], 'to':box[3:], 'faces':faces}


def legs(height, low=2, high=12, material='walnut'):
    return [(b,material,None) for b in [[low,0,low,low+2,height,low+2], [high,0,low,high+2,height,low+2],
                                      [low,0,high,low+2,height,high+2], [high,0,high,high+2,height,high+2]]]


specs={
 'cane_chair': [([2,7,2,14,9,14],'cane',None),([2,0,2,4,7,4],'walnut',None),([12,0,2,14,7,4],'walnut',None),
                ([2,0,12,4,16,14],'walnut',None),([12,0,12,14,16,14],'walnut',None),([4,10,12,12,16,14],'cane',None)],
 'kitchen_stool': [([2,7,2,14,9,14],'formica',None)] + legs(7,3,11,'metal_legs'),
 'footstool': [([2,3,2,14,7,14],'floral',None)] + legs(3,3,11),
 'walnut_desk': [([0,14,0,16,16,16],'walnut',None),([1,0,2,7,14,14],'walnut','drawer_front'),
                 ([12,0,2,14,14,4],'walnut',None),([12,0,12,14,14,14],'walnut',None),([7,11,11,14,14,14],'walnut',None)],
 'formica_table': [([0,14,0,16,16,16],'formica',None)] + legs(14,2,12,'metal_legs'),
 'bedside_table': [([1,5,1,15,16,15],'walnut','bedside_front')] + legs(5),
 'chest_of_drawers': [([1,0,1,15,16,15],'walnut','drawer_front')],
 'washing_machine': [([0,0,0,16,16,16],'enamel','washer_front')],
 'radiator': [([x,2,11,x+1,14,15],'enamel',None) for x in range(1,15,2)] +
              [([1,3,12,15,5,14],'enamel',None),([1,11,12,15,13,14],'enamel',None),
               ([2,0,12,4,2,15],'metal_legs',None),([12,0,12,14,2,15],'metal_legs',None)],
}
for kind, cloth in [('green_armchair','green_cloth'),('floral_armchair','floral')]:
    specs[kind] = [([2,2,2,14,8,14],cloth,None),([2,8,11,14,16,15],cloth,None),
                   ([0,6,2,3,12,14],cloth,None),([13,6,2,16,12,14],cloth,None)] + legs(2,2,12)
specs['blue_sofa'] = [([0,2,2,16,8,14],'blue_weave',None),([0,8,11,16,15,15],'blue_weave',None)] + legs(2,1,13)

for kind, parts in specs.items():
    textures={t:'the_oldest_house:block/'+t for _,t,_ in parts}
    textures['particle']=next(iter(textures.values()))
    for _,_,front in parts:
        if front: textures['front']='the_oldest_house:block/'+front
    model={'parent':'minecraft:block/block','textures':textures,'elements':[cube(b,t,f) for b,t,f in parts]}
    write_json('models/block/furniture_'+kind+'.json',model)
variants={}
for kind in specs:
    for facing,y in [('north',0),('east',90),('south',180),('west',270)]:
        variants[f'facing={facing},kind={kind}']={'model':'the_oldest_house:block/furniture_'+kind,'y':y,'uvlock':False}
write_json('blockstates/household_furniture.json',{'variants':variants})
write_json('models/item/household_furniture.json',{'parent':'the_oldest_house:block/furniture_cane_chair'})

notes={
 'housekeeping': [([2,0,3,14,.5,13],'notebook_cover',None),([2.5,.5,3.5,13.5,1.5,12.5],'paper',None),([7.5,1.5,3.5,8.5,1.7,12.5],'notebook_cover',None)],
 'calls': [([3,0,3,13,.5,13],'letter',None)],
 'room': [([2,0,2,13,.4,12],'paper',None),([3,.4,4,14,.8,14],'paper',None)],
 'poems': [([3,0,3,13,.6,13],'letter',None)],
}
for thread,parts in notes.items():
    textures={t:'the_oldest_house:block/'+t for _,t,_ in parts}; textures['particle']='the_oldest_house:block/paper'
    write_json('models/block/note_'+thread+'.json',{'parent':'minecraft:block/block','textures':textures,'elements':[cube(b,t) for b,t,_ in parts]})
variants={f'facing={facing},thread={thread}':{'model':'the_oldest_house:block/note_'+thread,'y':y}
          for thread in notes for facing,y in [('north',0),('east',90),('south',180),('west',270)]}
write_json('blockstates/note_surface.json',{'variants':variants})
write_json('models/item/note_surface.json',{'parent':'the_oldest_house:block/note_housekeeping'})

# Five-by-seven capitals, cut into narrow uneven strokes. No obfuscation or moving glyphs.
letters={
 'A':['01110','10001','10001','11111','10001','10001','10001'], 'B':['11110','10001','10001','11110','10001','10001','11110'],
 'C':['01111','10000','10000','10000','10000','10000','01111'], 'D':['11110','10001','10001','10001','10001','10001','11110'],
 'E':['11111','10000','10000','11110','10000','10000','11111'], 'F':['11111','10000','10000','11110','10000','10000','10000'],
 'G':['01111','10000','10000','10111','10001','10001','01110'], 'H':['10001','10001','10001','11111','10001','10001','10001'],
 'I':['11111','00100','00100','00100','00100','00100','11111'], 'J':['00111','00010','00010','00010','00010','10010','01100'],
 'K':['10001','10010','10100','11000','10100','10010','10001'], 'L':['10000','10000','10000','10000','10000','10000','11111'],
 'M':['10001','11011','10101','10101','10001','10001','10001'], 'N':['10001','11001','10101','10011','10001','10001','10001'],
 'O':['01110','10001','10001','10001','10001','10001','01110'], 'P':['11110','10001','10001','11110','10000','10000','10000'],
 'Q':['01110','10001','10001','10001','10101','10010','01101'], 'R':['11110','10001','10001','11110','10100','10010','10001'],
 'S':['01111','10000','10000','01110','00001','00001','11110'], 'T':['11111','00100','00100','00100','00100','00100','00100'],
 'U':['10001','10001','10001','10001','10001','10001','01110'], 'V':['10001','10001','10001','10001','10001','01010','00100'],
 'W':['10001','10001','10001','10101','10101','10101','01010'], 'X':['10001','10001','01010','00100','01010','10001','10001'],
 'Y':['10001','10001','01010','00100','00100','00100','00100'], 'Z':['11111','00001','00010','00100','01000','10000','11111'],
 '.':['0','0','0','0','0','1','1'], ' ':['0']*7,
}
chars=' ABCDEFGHIJKLMNO'+'PQRSTUVWXYZ.\u0000\u0000\u0000\u0000'
assert len(chars)==32
atlas=Image.new('RGBA',(128,20),(0,0,0,0))
for i,char in enumerate(chars):
    if char not in letters: continue
    x0,y0=i%16*8,i//16*10
    for y,row in enumerate(letters[char]):
        for x,ink in enumerate(row):
            if ink=='1':
                drift=1 if y in (0,3,6) and char not in ('.',' ') else 0
                atlas.putpixel((x0+x+drift,y0+y+1),(255,255,255,255))
                if y in (1,5) and x<4: atlas.putpixel((x0+x+1,y0+y),(255,255,255,115))
(ASSETS/'textures/font').mkdir(parents=True,exist_ok=True)
atlas.save(ASSETS/'textures/font/claw.png')
write_json('font/claw.json',{'providers':[{'type':'bitmap','file':'the_oldest_house:font/claw.png','ascent':8,'height':9,
                                       'chars':[chars[:16],chars[16:]]}, {'type':'reference','id':'minecraft:default'}]})

# A contact sheet for source review; every tile is enlarged with nearest-neighbor sampling.
names=[p.stem for p in sorted((ASSETS/'textures/block').glob('*.png')) if p.stem in
       ['walnut','cane','green_cloth','floral','blue_weave','formica','enamel','washer_front','paper','letter','drawer_front','notebook_cover']]
sheet=Image.new('RGB',(576, len(names)//4*172),(31,29,27)); d=ImageDraw.Draw(sheet)
for i,name in enumerate(names):
    x,y=i%4*144,i//4*172; sheet.paste(Image.open(ASSETS/'textures/block'/f'{name}.png').resize((128,128),Image.Resampling.NEAREST),(x+8,y+8))
    d.text((x+8,y+142),name,fill=(220,210,190))
sheet.save(ROOT/'tools/household_materials_preview.png')
print(f'Generated {len(specs)} furniture models, four paper models, 14 materials and one scratched font.')
