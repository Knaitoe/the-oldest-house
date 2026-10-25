"""Native pixel materials and fitted furniture for the six uncanny locations.

The same cuboids author the JSON meshes and Java collision. Materials use the
vanilla sixteen-pixel grid; no gradients, resampling, or source photographs.
"""
from pathlib import Path
import argparse
import json
import random
from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parents[1]
A = ROOT / 'src/main/resources/assets/the_oldest_house'
JAVA = ROOT / 'src/main/java/io/github/knaitoe/theoldesthouse/labyrinth/UncannyFurnitureShapes.java'
PREFIX = 'the_oldest_house:block/uncanny/'

COLORS = {
    'diner_wall': '#d5cdb9', 'diner_dado': '#4b7475', 'diner_tile': '#d7d1bb',
    'vinyl': '#6e3540', 'formica': '#a4b8ad', 'steel': '#8c9691', 'enamel': '#c2c2ae',
    'glass': '#b0c5bf', 'roof': '#4b5553', 'asphalt': '#343a3a', 'pavement': '#9c9c8f',
    'parlor_paper': '#b6a879', 'dark_wood': '#63503a', 'parlor_cloth': '#6e7154',
    'bedroom_paper': '#aab5b2', 'walnut': '#695543', 'linen': '#b9c0b5',
    'workshop_wall': '#777c73', 'workshop_floor': '#8e9185', 'utility': '#62726a',
    'lobby_wall': '#c5c3b3', 'courtyard_wall': '#5b625e', 'lamp': '#e1ddbf',
    'books': '#70634e', 'black': '#292f30', 'ceramic': '#d9d6c6',
    'parlor_rug': '#62664f', 'picture': '#bbb08f',
}
FULL = ['diner_wall', 'diner_dado', 'diner_tile', 'roof', 'asphalt', 'pavement',
        'parlor_paper', 'dark_wood', 'bedroom_paper', 'workshop_wall', 'workshop_floor', 'lobby_wall', 'courtyard_wall']

def rgb(c):
    return tuple(int(c[i:i+2], 16) for i in (1, 3, 5))

def shade(c, n):
    return tuple(max(0, min(255, x+n)) for x in rgb(c)) + (255,)

def paintings():
    out = {}
    for index, (name, base) in enumerate(COLORS.items()):
        im = Image.new('RGBA', (16, 16), base); d = ImageDraw.Draw(im); rnd = random.Random(44122+index)
        if name == 'glass':
            im = Image.new('RGBA', (16, 16)); d = ImageDraw.Draw(im)
            d.line((3,5,6,2), fill='#b5c8c3'); d.line((10,14,14,10), fill='#cad9cf')
        elif name in ('dark_wood', 'walnut'):
            for y in range(16):
                d.line((0,y,15,y),fill=shade(base,rnd.choice((-5,-3,0,2,4))))
            for x,y,length in ((2,2,8),(8,5,6),(1,10,6),(9,13,5)):
                d.line((x,y,min(15,x+length),y),fill=shade(base,-16))
            d.line((0,7,15,7),fill=shade(base,-24));d.line((0,15,15,15),fill=shade(base,-23))
            d.point((11,2),fill=shade(base,12));d.line((10,3,13,3),fill=shade(base,-20))
        elif name == 'diner_tile':
            d.rectangle((0,0,7,7),fill='#333f40');d.rectangle((8,8,15,15),fill='#333f40')
            for y in (0,8):d.line((0,y,15,y),fill='#828a7f')
            for x in (0,8):d.line((x,0,x,15),fill='#828a7f')
            for x,y in ((2,12),(10,4),(13,11)):d.point((x,y),fill='#adb1a0')
        elif name in ('diner_wall','lobby_wall','workshop_wall'):
            for x,y in ((2,5),(3,5),(12,11),(12,12),(8,2),(9,2)):
                d.point((x,y),fill=shade(base,-7))
            d.line((0,15,15,15),fill=shade(base,-11))
            if name=='workshop_wall':
                d.line((12,0,12,4,13,6),fill=shade(base,-18));d.line((3,8,3,11),fill=shade(base,9))
        elif name == 'diner_dado':
            d.line((0,0,15,0),fill='#a9b3a9');d.line((0,2,15,2),fill='#293f42')
            for x in (0,8):d.line((x,4,x,15),fill=shade(base,-17));d.line((x+1,4,x+1,15),fill=shade(base,8))
        elif name in ('parlor_paper','bedroom_paper'):
            for x in (2,10):
                if name=='parlor_paper':
                    d.point((x,3),fill='#847b59');d.line((x-1,4,x+1,4),fill='#978d66');d.point((x,5),fill='#847b59')
                    d.line((x,6,x,9),fill='#9c9870');d.point((x+1,7),fill='#8b9068')
                else:
                    d.line((x,0,x,15),fill=shade(base,-12));d.line((x+1,0,x+1,15),fill=shade(base,6))
            d.line((15,0,15,15),fill=shade(base,-13))
        elif name in ('vinyl','parlor_cloth','linen'):
            for x,y in ((2,5),(3,5),(8,11),(9,12),(13,3)):d.point((x,y),fill=shade(base,-9))
            d.line((0,0,15,0),fill=shade(base,-24));d.line((0,1,15,1),fill=shade(base,9))
            d.line((0,15,15,15),fill=shade(base,-18));d.line((15,1,15,14),fill=shade(base,-15))
            if name=='vinyl':
                d.line((4,6,7,6),fill=shade(base,-17));d.line((8,11,11,11),fill=shade(base,11))
        elif name=='formica':
            for x,y in ((2,3),(10,5),(5,12),(13,11)):
                d.line((x,y,x+1,y),fill='#718d86');d.point((x+1,y+1),fill='#d0d4b9')
        elif name in ('steel','enamel','roof','utility','black'):
            d.line((0,0,15,0),fill=shade(base,13));d.line((0,15,15,15),fill=shade(base,-20))
            for y in (5,12):d.line((2,y,13,y),fill=shade(base,5))
            if name in ('steel','roof','utility'):
                for x,y in ((1,1),(14,14)):d.point((x,y),fill=shade(base,-25))
        elif name in ('asphalt','pavement','workshop_floor','courtyard_wall'):
            for x in range(16):
                for y in range(16):
                    if rnd.randrange(7)==0:d.point((x,y),fill=shade(base,rnd.choice((-9,-5,5))))
            if name!='asphalt':d.line((0,15,15,15),fill=shade(base,-18));d.line((15,0,15,15),fill=shade(base,-17))
        elif name=='books':
            for x,width,color in ((0,3,'#5c6b68'),(3,4,'#a48c62'),(7,2,'#7f4f43'),(9,4,'#4e6365'),(13,3,'#8b7c5d')):
                d.rectangle((x,2,x+width-1,14),fill=color);d.line((x,4,x+width-2,4),fill='#b6aa8a')
            d.line((0,15,15,15),fill='#463b2d')
        elif name=='lamp':
            d.rectangle((0,0,15,1),fill='#969d8d');d.rectangle((0,14,15,15),fill='#969d8d')
            for x in (4,11):d.line((x,2,x,13),fill='#c6c7ac')
        elif name=='parlor_rug':
            d.line((0,0,15,0),fill='#555943');d.line((0,8,15,8),fill='#6f7256')
            for x,y in ((4,4),(12,12)):
                d.line((x-2,y,x,y-2,x+2,y,x,y+2,x-2,y),fill='#929270')
                d.point((x,y),fill='#b1a27e');d.point((x-3,y+3),fill='#7b6850')
        elif name=='picture':
            d.rectangle((3,3,12,7),fill='#9eaaa0');d.rectangle((3,8,12,12),fill='#73775c')
            d.rectangle((5,7,10,10),fill='#c0b28b');d.polygon(((4,7),(7,4),(11,7)),fill='#625448')
            d.rectangle((7,8,8,10),fill='#5b6259');d.point((9,8),fill='#ddd1ad');d.line((3,12,12,12),fill='#99926d')
        elif name=='ceramic':
            d.line((0,0,15,0),fill='#eee9d4');d.line((0,15,15,15),fill='#9faaa2')
        out[name]=im
    glyphs={'B':['110','101','110','101','110'],'L':['100','100','100','100','111'],
            'U':['101','101','101','101','111'],'E':['111','100','110','100','111'],
            'I':['111','010','010','010','111'],'G':['111','100','101','101','111'],
            'H':['101','101','111','101','101'],'T':['111','010','010','010','010']}
    sign=Image.new('RGBA',(48,16),'#263b40');d=ImageDraw.Draw(sign)
    d.rectangle((0,0,47,15),outline='#8e9d95');x=6
    for letter in 'BLUE LIGHT':
        if letter==' ':x+=2;continue
        for y,row in enumerate(glyphs[letter]):
            for dx,v in enumerate(row):
                if v=='1':d.point((x+dx,5+y),fill='#a4d2ca')
        x+=4
    for i,part in enumerate(('sign_left','sign_center','sign_right')):out[part]=sign.crop((i*16,0,(i+1)*16,16))
    # Steel-framed, glazed entrance leaves. Each texture retains native door UVs.
    for part in ('lower','upper'):
        im=Image.new('RGBA',(16,16));d=ImageDraw.Draw(im)
        d.rectangle((0,0,15,15),outline='#7a8985',width=2);d.line((2,2,13,2),fill='#b7beb0')
        if part=='lower':d.rectangle((2,10,13,14),fill='#526f70');d.rectangle((11,1,12,7),fill='#bec5b9')
        else:d.line((4,8,9,3),fill='#a4bbb5');d.line((8,12,12,8),fill='#c9d8cf')
        out['door_'+part]=im
    return out

def piece(a,b,material,**faces):
    return {'from':list(a),'to':list(b),'material':material,'faces':faces}

def leg(x,z,material='steel',height=14):return piece((x,0,z),(x+2,height,z+2),material)

def furniture():
    p=piece;out={}
    out['booth']=[p((0,0,2),(16,3,15),'steel'),p((0,3,1),(16,8,15),'vinyl'),p((0,8,12),(16,17,16),'vinyl'),p((0,15,11),(16,17,16),'vinyl')]
    out['table']=[p((0,14,0),(16,16,16),'formica'),p((0,13,0),(16,14,16),'steel'),p((7,2,7),(9,14,9),'steel'),p((3,0,3),(13,2,13),'black')]
    out['stool']=[p((2,10,2),(14,13,14),'vinyl'),p((3,9,3),(13,10,13),'steel'),p((7,1,7),(9,9,9),'steel'),p((3,0,3),(13,1,13),'black')]
    out['counter']=[p((0,0,2),(16,14,16),'diner_dado'),p((0,14,0),(16,16,16),'formica'),p((0,13,0),(16,14,2),'steel'),p((0,1,1),(16,3,2),'steel')]
    out['cabinet']=[p((0,0,2),(16,2,16),'steel'),p((0,2,15),(16,15,16),'enamel'),p((0,2,2),(1,15,15),'enamel'),p((15,2,2),(16,15,15),'enamel'),p((0,15,1),(16,16,16),'steel'),p((1,2,1),(15,15,2),'enamel'),p((11,8,0),(13,10,1),'steel')]
    out['fridge']=[p((0,0,1),(16,32,16),'enamel'),p((1,1,0),(15,22,1),'ceramic'),p((12,6,0),(13,19,2),'steel'),p((1,23,0),(15,31,1),'ceramic'),p((12,25,0),(13,29,2),'steel')]
    out['coffee_maker']=[p((2,0,4),(14,2,14),'steel'),p((3,2,11),(13,13,14),'enamel'),p((3,11,3),(13,14,14),'steel'),p((5,2,4),(11,8,10),'black'),p((11,4,5),(13,7,8),'steel'),p((5,8,4),(11,9,10),'black')]
    out['griddle']=[p((0,0,0),(16,14,16),'enamel'),p((0,14,0),(16,16,16),'steel'),p((2,15,2),(14,16,13),'black'),p((2,10,0),(5,12,1),'black'),p((10,10,0),(13,12,1),'black')]
    out['sink']=[p((0,0,3),(16,12,16),'enamel'),p((0,12,1),(16,14,16),'steel'),p((2,14,3),(4,15,13),'steel'),p((12,14,3),(14,15,13),'steel'),p((4,14,3),(12,15,5),'steel'),p((4,14,11),(12,15,13),'steel'),p((6,14,13),(8,16,15),'steel')]
    out['shelf']=[p((0,1,4),(16,2,16),'steel'),p((0,8,4),(16,9,16),'steel'),p((0,15,4),(16,16,16),'steel'),p((0,2,14),(1,15,16),'steel'),p((15,2,14),(16,15,16),'steel'),p((2,9,6),(7,14,14),'enamel'),p((9,2,7),(14,7,14),'dark_wood')]
    out['fluorescent']=[p((0,14,3),(16,16,13),'steel'),p((1,11,4),(15,14,12),'lamp'),p((0,11,3),(1,14,13),'steel'),p((15,11,3),(16,14,13),'steel')]
    out['window_frame']=[p((0,0,13),(16,2,16),'steel'),p((0,14,13),(16,16,16),'steel'),p((0,2,13),(2,14,16),'steel'),p((14,2,13),(16,14,16),'steel'),p((7,2,14),(8,14,15),'steel'),p((2,2,15),(14,14,16),'glass')]
    for part in ('left','center','right'):
        out['sign_'+part]=[p((0,0,13),(16,16,16),'steel'),p((0,0,12),(16,16,13),'sign_'+part)]
    out['exterior_lamp']=[p((5,0,13),(11,14,16),'steel'),p((7,10,6),(9,12,14),'steel'),p((4,7,3),(12,11,10),'steel'),p((5,5,4),(11,7,9),'lamp')]
    out['sconce']=[p((6,4,14),(10,14,16),'dark_wood'),p((7,6,7),(9,8,15),'steel'),p((4,8,5),(12,15,12),'lamp')]
    out['armchair']=[p((2,0,3),(4,3,5),'dark_wood'),p((12,0,3),(14,3,5),'dark_wood'),p((2,0,12),(4,3,15),'dark_wood'),p((12,0,12),(14,3,15),'dark_wood'),p((1,3,3),(15,9,15),'parlor_cloth'),p((1,9,12),(15,17,16),'parlor_cloth'),p((0,7,2),(3,12,15),'parlor_cloth'),p((13,7,2),(16,12,15),'parlor_cloth')]
    out['sofa']=[p((0,0,3),(2,3,5),'dark_wood'),p((14,0,3),(16,3,5),'dark_wood'),p((0,3,2),(16,9,15),'parlor_cloth'),p((0,9,12),(16,16,16),'parlor_cloth')]
    out['sofa_left']=out['sofa']+[p((0,7,2),(3,13,15),'parlor_cloth')]
    out['sofa_right']=out['sofa']+[p((13,7,2),(16,13,15),'parlor_cloth')]
    out['coffee_table']=[p((0,9,0),(16,11,16),'walnut'),leg(1,2,'walnut',9),leg(13,2,'walnut',9),leg(1,12,'walnut',9),leg(13,12,'walnut',9)]
    out['diner_chair']=[leg(2,3,'steel',7),leg(12,3,'steel',7),leg(2,12,'steel',7),leg(12,12,'steel',7),p((1,7,2),(15,9,15),'vinyl'),p((2,9,13),(4,18,15),'steel'),p((12,9,13),(14,18,15),'steel'),p((1,12,12),(15,18,16),'vinyl')]
    out['desk']=[p((0,14,1),(16,16,16),'walnut'),p((1,0,3),(7,14,15),'walnut'),leg(12,3,'walnut'),leg(12,12,'walnut'),p((2,3,1),(6,7,2),'walnut'),p((3,5,0),(5,6,1),'steel')]
    out['sideboard']=[p((1,0,3),(3,3,5),'dark_wood'),p((13,0,3),(15,3,5),'dark_wood'),p((0,3,2),(16,15,16),'dark_wood'),p((0,15,1),(16,16,16),'walnut'),p((2,5,1),(14,13,2),'walnut'),p((11,8,0),(12,10,1),'steel')]
    out['bookcase']=[p((0,0,4),(2,16,16),'dark_wood'),p((14,0,4),(16,16,16),'dark_wood'),p((2,0,5),(14,2,16),'dark_wood'),p((2,8,5),(14,9,16),'dark_wood'),p((2,15,5),(14,16,16),'dark_wood'),p((2,2,7),(14,8,15),'books'),p((2,9,7),(14,15,15),'books')]
    out['picture']=[p((2,2,14),(14,14,16),'dark_wood'),p((3,3,13),(13,13,14),'picture')]
    out['wardrobe']=[p((1,0,3),(15,32,16),'walnut'),p((2,1,2),(14,31,3),'dark_wood'),p((10,14,1),(11,18,2),'steel')]
    out['bedside']=[p((1,1,3),(3,5,5),'walnut'),p((13,1,3),(15,5,5),'walnut'),p((0,5,2),(16,15,16),'walnut'),p((0,15,1),(16,16,16),'walnut'),p((11,8,1),(13,9,2),'steel')]
    out['headboard']=[p((0,0,13),(16,14,16),'walnut'),p((0,14,12),(16,16,16),'walnut')]
    out['hamper']=[p((2,0,3),(14,12,15),'linen'),p((1,12,2),(15,14,16),'walnut')]
    out['radiator']=[p((1,2,12),(15,4,16),'steel'),p((2,4,12),(4,14,16),'enamel'),p((5,4,12),(7,14,16),'enamel'),p((8,4,12),(10,14,16),'enamel'),p((11,4,12),(14,14,16),'enamel'),p((2,0,13),(4,2,16),'black'),p((12,0,13),(14,2,16),'black')]
    out['pipe']=[p((6,0,11),(10,16,15),'utility'),p((5,3,10),(11,5,16),'steel'),p((5,12,10),(11,14,16),'steel')]
    out['pipe_horizontal']=[p((0,6,11),(16,10,15),'utility'),p((2,5,10),(4,11,16),'steel'),p((12,5,10),(14,11,16),'steel')]
    out['vent']=[p((1,1,14),(15,15,16),'steel')]+[p((3,y,13),(13,y+1,14),'black') for y in (4,7,10,13)]
    out['switchbox']=[p((3,3,10),(13,15,16),'utility'),p((4,4,9),(12,14,10),'steel'),p((8,8,8),(10,12,9),'black')]
    out['workbench']=[p((0,13,0),(16,16,16),'dark_wood'),leg(1,2,'utility',13),leg(13,2,'utility',13),leg(1,12,'utility',13),leg(13,12,'utility',13),p((1,4,3),(15,5,15),'utility')]
    out['tool_rack']=[p((1,2,14),(15,15,16),'dark_wood'),p((3,4,12),(4,12,14),'steel'),p((7,5,12),(8,13,14),'steel'),p((11,3,12),(13,11,14),'steel')]
    out['mailbox']=[p((0,0,4),(16,16,16),'steel')]+[p((x+1,y+1,3),(x+7,y+7,4),'utility') for x in (0,8) for y in (0,8)]+[p((x+2,y+4,2),(x+6,y+5,3),'black') for x in (0,8) for y in (0,8)]
    out['lobby_bench']=[p((0,7,2),(16,9,15),'dark_wood'),p((0,9,13),(16,15,16),'dark_wood'),leg(2,3,'steel',7),leg(12,3,'steel',7),leg(2,12,'steel',7),leg(12,12,'steel',7)]
    out['drain']=[p((0,0,0),(16,1,16),'steel')]+[p((x,1,2),(x+1,2,14),'black') for x in (3,6,9,12)]
    out['bin']=[p((3,0,3),(13,13,13),'utility'),p((2,13,2),(14,15,14),'steel'),p((5,15,6),(11,16,10),'black')]
    out['desk_lamp']=[p((3,0,4),(13,2,13),'steel'),p((7,2,8),(9,10,10),'steel'),p((3,10,4),(13,14,13),'lamp')]
    out['napkin_caddy']=[p((2,0,3),(10,7,13),'steel'),p((3,5,4),(9,8,12),'linen'),p((4,1,2),(8,6,3),'black'),p((11,0,4),(15,2,12),'steel'),p((12,2,5),(13,4,11),'steel')]
    out['cup']=[p((5,0,5),(11,1,11),'ceramic'),p((4,1,4),(12,6,12),'ceramic'),p((5,6,5),(11,7,11),'black'),p((12,2,6),(14,5,10),'ceramic')]
    return out

INTERACTIVE={'cabinet','fridge','sideboard','wardrobe','mailbox','desk_lamp'}
SEATS={'booth','stool','armchair','sofa','sofa_left','sofa_right','diner_chair','lobby_bench'}
LIGHTS={'fluorescent':12,'exterior_lamp':9,'sconce':8,'desk_lamp':8,'sign_left':7,'sign_center':7,'sign_right':7}

def pieces_for(name,opened):
    parts=furniture()[name]
    if not opened:return parts
    parts=json.loads(json.dumps(parts))
    if name=='cabinet':parts[-2]['from']=[1,2,2];parts[-2]['to']=[2,15,15];parts[-1]['from']=[0,8,11];parts[-1]['to']=[1,10,13]
    elif name=='wardrobe':parts[-2]['from']=[1,1,3];parts[-2]['to']=[2,31,15];parts[-1]['from']=[0,14,10];parts[-1]['to']=[1,18,11]
    elif name=='sideboard':parts[-2]['from']=[1,5,2];parts[-2]['to']=[2,13,14];parts[-1]['from']=[0,8,11];parts[-1]['to']=[1,10,12]
    elif name=='fridge':parts[1]['from']=[0,1,1];parts[1]['to']=[1,22,15];parts[2]['from']=[0,6,12];parts[2]['to']=[2,19,13]
    elif name=='mailbox':parts[1]['from']=[1,1,4];parts[1]['to']=[2,7,10]
    return parts

def models():
    result={}
    for name in FULL:
        result['models/block/uncanny_'+name+'.json']={'parent':'minecraft:block/cube_all','textures':{'all':PREFIX+name}}
        result['blockstates/uncanny_'+name+'.json']={'variants':{'':{'model':'the_oldest_house:block/uncanny_'+name}}}
    variants={}
    for name in furniture():
        for opened in ((False,True) if name in INTERACTIVE else (False,)):
            suffix='_open' if opened else '';path='uncanny_'+name+suffix
            elements=[]
            for part in pieces_for(name,opened):
                x0,y0,z0=part['from'];x1,y1,z1=part['to']
                # Split tall panels at a native tile boundary. Auto UVs above y=16 sample
                # neighbouring atlas sprites; explicit local UVs keep every pixel on its material.
                cuts=[y0]+[v for v in (16,) if y0<v<y1]+[y1]
                for low,high in zip(cuts,cuts[1:]):
                    tile=16 if low>=16 else 0;v0=16-(high-tile);v1=16-(low-tile)
                    uvs={'north':[16-x1,v0,16-x0,v1],'south':[x0,v0,x1,v1],
                         'east':[16-z1,v0,16-z0,v1],'west':[z0,v0,z1,v1],
                         'up':[x0,z0,x1,z1],'down':[x0,16-z1,x1,16-z0]}
                    faces={face:{'texture':'#'+part['faces'].get(face,part['material']),'uv':uvs[face]} for face in uvs}
                    if name.startswith('sign_'):
                        for face in ('north','south'):faces[face]['uv']=[0,0,16,16]
                    elements.append({'from':[x0,low,z0],'to':[x1,high,z1],'faces':faces})
            materials={part['material'] for part in pieces_for(name,opened)}
            result['models/block/'+path+'.json']={'render_type':'minecraft:cutout','textures':{'particle':PREFIX+next(iter(sorted(materials))),**{m:PREFIX+m for m in sorted(materials)}},'elements':elements}
            for facing,y in [('north',0),('east',90),('south',180),('west',270)]:
                key='kind='+name+',facing='+facing+(',open='+str(opened).lower() if name in INTERACTIVE else '')
                variants[key]={'model':'the_oldest_house:block/'+path,**({'y':y} if y else {})}
    result['blockstates/uncanny_furniture.json']={'variants':variants}
    result['models/block/uncanny_parlor_rug.json']={'parent':'minecraft:block/carpet','textures':{'wool':PREFIX+'parlor_rug'}}
    result['blockstates/uncanny_parlor_rug.json']={'variants':{'':{'model':'the_oldest_house:block/uncanny_parlor_rug'}}}
    for half in ('lower','upper'):
        for hinge in ('left','right'):
            for opened in (False,True):
                name='uncanny_diner_door_'+half+'_'+hinge+('_open' if opened else '')
                parent='minecraft:block/door_'+('bottom' if half=='lower' else 'top')+'_'+hinge+('_open' if opened else '')
                result['models/block/'+name+'.json']={'parent':parent,'render_type':'minecraft:cutout','textures':{'top':PREFIX+'door_upper','bottom':PREFIX+'door_lower'}}
    variants={}
    for facing,y in [('east',0),('south',90),('west',180),('north',270)]:
        for half in ('lower','upper'):
            for hinge in ('left','right'):
                for opened in (False,True):
                    rotation=(y+(90 if hinge=='left' else -90) if opened else y)%360
                    variants[f'facing={facing},half={half},hinge={hinge},open={str(opened).lower()}']={'model':'the_oldest_house:block/uncanny_diner_door_'+half+'_'+hinge+('_open' if opened else ''),'y':rotation}
    result['blockstates/uncanny_diner_door.json']={'variants':variants}
    return result

def shape_source():
    lines=['package io.github.knaitoe.theoldesthouse.labyrinth;','','/** Generated with the meshes by tools/draw_uncanny_environments.py. */','final class UncannyFurnitureShapes {','    private UncannyFurnitureShapes() {}','    static double[][] boxes(UncannyFurnitureBlock.Kind kind, boolean open) {','        return switch(kind) {']
    def array(parts):return 'new double[][]{'+','.join('{'+','.join(str(v) for v in p['from']+p['to'])+'}' for p in parts)+'}'
    for name in furniture():
        expression=('open ? '+array(pieces_for(name,True))+' : ' if name in INTERACTIVE else '')+array(pieces_for(name,False))
        lines.append('            case '+name.upper()+' -> '+expression+';')
    lines+=['        };','    }','}','']
    return '\n'.join(lines)

def main():
    parser=argparse.ArgumentParser();parser.add_argument('--check',action='store_true');check=parser.parse_args().check
    for name,im in paintings().items():
        path=A/'textures/block/uncanny'/(name+'.png')
        if check:
            with Image.open(path) as saved:assert saved.convert('RGBA').tobytes()==im.tobytes(),name
        else:path.parent.mkdir(parents=True,exist_ok=True);im.save(path,optimize=True)
    for name,obj in models().items():
        path=A/name
        if check:assert json.loads(path.read_text())==obj,name
        else:path.parent.mkdir(parents=True,exist_ok=True);path.write_text(json.dumps(obj,indent=2)+'\n')
    if check:assert JAVA.read_text()==shape_source(),'Native furniture collision differs from its generated meshes'
    else:JAVA.write_text(shape_source())
    print(('Verified' if check else 'Painted')+f' {len(paintings())} native materials, {len(furniture())} fitted furnishings and the glazed diner entrance.')

if __name__=='__main__':main()
