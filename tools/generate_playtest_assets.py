#!/usr/bin/env python3
"""0.4.64: small, hard-edged native pixel assets. No resampling or antialiasing."""
import json
import random
from pathlib import Path
from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parents[1]
A = ROOT / 'src/main/resources/assets/the_oldest_house'

def save_json(path, data):
    (A / path).write_text(json.dumps(data, indent=2) + '\n')

def save_image(path, image):
    (A / path).parent.mkdir(parents=True, exist_ok=True)
    image.save(A / path)

def sprite(name, paint):
    image = Image.new('RGBA', (16, 16)); paint(ImageDraw.Draw(image))
    save_image(f'textures/item/{name}.png', image)
    save_json(f'models/item/{name}.json', {'parent': 'minecraft:item/generated', 'textures': {'layer0': f'the_oldest_house:item/{name}'}})

def camera(d):
    d.rectangle((2, 5, 13, 12), fill='#24272aff')
    d.rectangle((3, 6, 12, 11), fill='#454b4fff')
    d.rectangle((4, 4, 8, 5), fill='#292d30ff')
    d.rectangle((5, 3, 7, 4), fill='#899596ff')
    d.rectangle((10, 4, 11, 4), fill='#adb1a2ff')
    d.rectangle((3, 7, 4, 10), fill='#202124ff')
    d.rectangle((6, 7, 10, 11), fill='#181b20ff')
    d.rectangle((7, 6, 9, 12), fill='#181b20ff')
    d.rectangle((7, 8, 9, 10), fill='#2f5964ff')
    d.point((8, 8), fill='#779997ff');d.point((9, 10), fill='#162e3aff')
    d.rectangle((11, 6, 12, 7), fill='#b8bdb0ff')
    d.line((1, 7, 0, 8, 0, 12, 3, 14, 12, 14, 15, 12, 15, 8, 14, 7), fill='#4c3830ff')

def key(d, gold):
    dark, mid, bright = ('#755327ff','#b09042ff','#d7c271ff') if gold else ('#3b4947ff','#71867fff','#aec3b2ff')
    # A hollow square bow, diagonal shank and two teeth read at inventory scale.
    d.rectangle((2, 1, 7, 6), fill=dark);d.rectangle((3, 1, 6, 5), fill=mid)
    d.rectangle((4, 2, 5, 3), fill='#00000000');d.line((3, 1, 6, 1), fill=bright)
    d.line((6, 5, 13, 12), fill=dark, width=3);d.line((6, 5, 13, 12), fill=mid, width=1)
    d.line((11, 10, 9, 12), fill=dark, width=2);d.point((9, 11), fill=bright)
    d.line((13, 12, 11, 14), fill=dark, width=2);d.point((11, 13), fill=mid)

def ribbon(d):
    d.polygon([(3, 2),(6, 1),(9, 2),(12, 1),(13, 4),(11, 6),(10, 10),(12, 14),(9, 13),(7, 15),(6, 11),(7, 7),(4, 6),(2, 4)], fill='#502625ff')
    d.polygon([(4, 2),(6, 2),(9, 3),(12, 2),(12, 4),(9, 5),(9, 10),(11, 13),(9, 12),(8, 14),(7, 10),(8, 6),(5, 5),(3, 4)], fill='#9a4c46ff')
    d.line((4, 3, 6, 3, 8, 5, 8, 8), fill='#c67a61ff')
    d.line((10, 3, 11, 3), fill='#c67a61ff');d.point((8, 12), fill='#c67a61ff')
    d.rectangle((7, 4, 9, 5), fill='#6d312fff');d.point((7, 4), fill='#ad5e50ff')

def franks_package(d):
    # The trailer's supper: a shrink-wrapped tray of four, its label band above and the sausages showing through.
    d.rectangle((2, 4, 13, 4), fill='#798077ff')
    d.rectangle((2, 5, 13, 12), fill='#bbb9a7ff');d.rectangle((3, 5, 12, 11), fill='#e4e1cfff')
    d.rectangle((4, 6, 11, 10), fill='#994d2eff')
    for x in (4, 6, 8, 10):d.point((x, 6), fill='#c56b41ff')
    d.rectangle((3, 12, 12, 12), fill='#747566ff')

def furniture():
    model = json.loads((A / 'models/block/furniture_formica_table.json').read_text())
    model['textures'] = {'formica': 'minecraft:block/oak_planks', 'metal_legs': 'minecraft:block/stripped_dark_oak_log', 'particle': 'minecraft:block/oak_planks'}
    save_json('models/block/furniture_chess_table.json', model)
    states = json.loads((A / 'blockstates/household_furniture.json').read_text())
    for facing, angle in [('north',0),('east',90),('south',180),('west',270)]:
        value = {'model':'the_oldest_house:block/furniture_chess_table'}
        if angle:value['y']=angle
        states['variants'][f'facing={facing},kind=chess_table']=value
    save_json('blockstates/household_furniture.json', states)

def papers():
    models=[]
    for variant in range(4):
        rng=random.Random(640+variant);image=Image.new('RGB',(16,16),(185,180,163));d=ImageDraw.Draw(image)
        for y in range(16):
            for x in range(16):
                n=rng.choice((-3,0,0,2));image.putpixel((x,y),(185+n,180+n,163+n))
        boxes=[(1,1,7,7),(8,4,14,13)] if variant%2==0 else [(2,3,8,13),(9,1,14,8)]
        for left,top,right,bottom in boxes:
            d.rectangle((left+1,top+1,right+1,bottom+1),fill=(123,116,102))
            d.rectangle((left,top,right,bottom),fill=(220-variant*3,211-variant*3,183-variant*2))
            for y in range(top+2,bottom,2):
                length=rng.randint(2,max(2,right-left-1));d.line((left+1,y,left+length,y),fill=(113,105,91))
            d.point((left,top),fill=(130,120,102))
        save_image(f'textures/block/archive_paper_{variant}.png',image)
        save_json(f'models/block/archive_paper_{variant}.json',{'parent':'minecraft:block/cube_all','textures':{'all':f'the_oldest_house:block/archive_paper_{variant}'}})
        models.append({'model':f'the_oldest_house:block/archive_paper_{variant}'})
    save_json('blockstates/archive_paper.json',{'variants':{'':models}})

def boarded_window():
    # Full dark backing; literal oak boards and an inset frame on every wall-facing side.
    image=Image.new('RGB',(16,16),(22,28,30));d=ImageDraw.Draw(image)
    d.rectangle((0,0,15,15),outline=(68,66,55));d.rectangle((1,1,14,14),outline=(41,45,43))
    d.line((3,2,3,13),fill=(29,39,41));d.line((8,2,8,13),fill=(35,43,43))
    save_image('textures/block/sealed_window.png',image)
    elements=[]
    def box(a,b,texture):
        elements.append({'from':a,'to':b,'faces':{face:{'texture':texture,'uv':[0,0,16,16]} for face in ['north','south','east','west','up','down']}})
    box([0,0,0],[16,16,16],'#glass')
    for side in ['north','south','west','east']:
        for y in [4,11]:
            if side=='north':box([0,y,-.25],[16,y+2,.25],'#wood')
            if side=='south':box([0,y,15.75],[16,y+2,16.25],'#wood')
            if side=='west':box([-.25,y,0],[.25,y+2,16],'#wood')
            if side=='east':box([15.75,y,0],[16.25,y+2,16],'#wood')
    save_json('models/block/sealed_window.json',{'textures':{'glass':'the_oldest_house:block/sealed_window','wood':'minecraft:block/oak_planks','particle':'minecraft:block/oak_planks'},'elements':elements})

def boy():
    # Deliberately no lit facial features: the child stays an obscure, distant shape.
    image=Image.new('RGBA',(64,64),(18,20,21,255));d=ImageDraw.Draw(image)
    d.rectangle((8,8,15,15),fill=(26,27,26,255));d.rectangle((8,8,15,10),fill=(15,17,18,255))
    d.rectangle((20,20,27,31),fill=(24,27,28,255));d.rectangle((22,23,25,31),fill=(21,23,24,255))
    d.rectangle((4,20,11,31),fill=(17,19,21,255));d.rectangle((20,52,27,63),fill=(17,19,21,255))
    d.rectangle((44,20,47,31),fill=(25,26,26,255));d.rectangle((36,52,39,63),fill=(25,26,26,255))
    save_image('textures/entity/literary_plain_boy.png',image)

if __name__=='__main__':
    sprite('plain_camera',camera);sprite('archive_key',lambda d:key(d,False));sprite('church_key',lambda d:key(d,True));sprite('well_ribbon',ribbon);sprite('franks_package',franks_package)
    furniture();papers();boarded_window();boy()
