#!/usr/bin/env python3
"""0.4.68 native pixel fixtures: exact-letter signs, a note post, a cooler, wheels and painted cars.

Extends the existing model and pixel alphabet system. Does not rewrite any approved character/item atlas.
Run after generate_proofrock_assets.py when regenerating all assets.
"""
from pathlib import Path
import json
from PIL import Image
from generate_proofrock_assets import GLYPHS
from generate_whale_institute_assets import DIGITS

A=Path(__file__).resolve().parents[1]/'src/main/resources/assets/the_oldest_house'
NS='the_oldest_house'
GLYPHS.update(DIGITS)
TURN={'north':0,'east':90,'south':180,'west':270}
FACES=('north','south','east','west','up','down')

def write(path,value):
    p=A/path;p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(value,indent=2)+'\n')

def save(name,im):
    p=A/f'textures/block/{name}.png';p.parent.mkdir(parents=True,exist_ok=True);im.save(p)

def grain(size,base):
    im=Image.new('RGBA',size)
    for x in range(size[0]):
        for y in range(size[1]):
            n=((x*7+y*11)%7)-3
            im.putpixel((x,y),tuple(max(0,min(255,c+n)) for c in base)+(255,))
    return im

def rect(im,a,b,colour):
    for x in range(a[0],b[0]):
        for y in range(a[1],b[1]):im.putpixel((x,y),colour+(255,))

def text(im,line,y,colour,scale=1):
    width=sum(len(GLYPHS[c][0])+1 for c in line)-1;x=(im.width-width*scale)//2
    if x<2:raise ValueError(f'Notice text does not fit: {line}')
    for c in line:
        for row,bits in enumerate(GLYPHS[c]):
            for col,bit in enumerate(bits):
                if bit=='1':rect(im,(x+col*scale,y+row*scale),(x+(col+1)*scale,y+(row+1)*scale),colour)
        x+=(len(GLYPHS[c][0])+1)*scale

def box(a,b,tex,front=None):
    uv={'north':[a[0],16-b[1],b[0],16-a[1]],'south':[16-b[0],16-b[1],16-a[0],16-a[1]],
        'west':[a[2],16-b[1],b[2],16-a[1]],'east':[16-b[2],16-b[1],16-a[2],16-a[1]],
        'up':[a[0],a[2],b[0],b[2]],'down':[a[0],16-b[2],b[0],16-a[2]]}
    faces={f:{'texture':tex,'uv':uv[f]} for f in FACES}
    if front:faces['north']={'texture':front,'uv':[0,0,16,16]};faces['south']={'texture':front,'uv':[0,0,16,16]}
    return {'from':a,'to':b,'faces':faces}

def model(name,textures,elements):
    write(f'models/block/{name}.json',{'textures':{k:(v if ':' in v else f'{NS}:block/{v}') for k,v in textures.items()},'elements':elements})

def states(name,kinds=None,fixture=False):
    variants={}
    for facing,turn in TURN.items():
        for kind in kinds or ['']:
            extra=[f',filled={filled},open={opened}' for filled in ('false','true') for opened in ('false','true')] if fixture else ['']
            for suffix in extra:
                entry={'model':f'{NS}:block/{name}{"_"+kind if kind else ""}'}
                if turn:entry['y']=turn
                variants[f'facing={facing}{",kind="+kind if kind else ""}{suffix}']=entry
    write(f'blockstates/{name}.json',{'variants':variants})

def signs():
    notices={'post':['POST'],'outgoing':['OUTGOING','MAIL'],'hours':['VISITING HOURS','NONE AT PRESENT'],
             'writing':['PATIENTS MAY','WRITE AS OFTEN','AS THEY LIKE']}
    kinds=list(notices)+[f'room_{n}' for n in [1,2,3,4,5,6,8]]+['missing']
    for k in kinds:
        numbered=k.startswith('room_') or k=='missing'
        im=grain((32,16) if numbered else (64,32),(190,157,80) if numbered else (226,226,209))
        border=(82,67,36) if numbered else (37,66,57)
        for x in range(im.width):im.putpixel((x,0),border+(255,));im.putpixel((x,im.height-1),border+(255,))
        for y in range(im.height):im.putpixel((0,y),border+(255,));im.putpixel((im.width-1,y),border+(255,))
        for x in (2,im.width-3):
            for y in (2,im.height-3):im.putpixel((x,y),(76,72,59,255))
        if k.startswith('room_'):text(im,k[-1],3,(46,37,24),2)
        elif k=='missing':rect(im,(7,7),(9,9),(49,48,43));rect(im,(23,7),(25,9),(49,48,43))
        else:
            lines=notices[k];top=(im.height-(len(lines)*8-3))//2
            for i,line in enumerate(lines):text(im,line,top+i*8,(32,51,44))
        save('institute_'+k,im)
        elements=[box([3,5,15],[13,11,16],'#edge','#face')] if numbered else [box([0,3,15],[16,13,16],'#edge','#face')]
        if k=='outgoing':elements=[box([6,0,7],[10,7,11],'#post'),box([0,6,6],[16,16,8],'#edge','#face')]
        model('institute_sign_'+k,{'face':'institute_'+k,'edge':'minecraft:block/iron_block','post':'minecraft:block/oak_planks','particle':'institute_'+k},elements)
    states('institute_sign',kinds)

def cooler_and_wheel():
    im=grain((16,16),(181,196,196));rect(im,(0,2),(16,4),(221,221,199));rect(im,(1,5),(15,14),(36,87,94));rect(im,(3,8),(13,10),(27,67,72));rect(im,(7,2),(9,6),(157,163,160));save('cooler',im)
    model('trailer_cooler',{'body':'cooler','lid':'minecraft:block/quartz_block_top','latch':'minecraft:block/iron_block','particle':'cooler'},
          [box([1,0,1],[15,11,15],'#body'),box([0,11,0],[16,14,16],'#lid'),box([6,9,0],[10,13,1],'#latch'),box([0,5,5],[1,9,11],'#latch'),box([15,5,5],[16,9,11],'#latch')])
    states('trailer_cooler',fixture=True)
    tire=grain((16,16),(27,29,30))
    for x in range(16):
        for y in range(16):
            radius=((x-7.5)**2+(y-7.5)**2)**.5
            if radius<4.2:tire.putpixel((x,y),(99,109,113,255) if x in (6,9) or y in (6,9) else (161,171,170,255))
            elif radius<5:tire.putpixel((x,y),(56,62,65,255))
            elif (x+y)%4==0:tire.putpixel((x,y),(43,44,43,255))
    rect(tire,(7,7),(9,9),(66,73,75));save('vehicle_wheel',tire)
    elements=[box([4,0,5],[12,16,11],'#rubber'),box([0,4,5],[4,12,11],'#rubber'),box([12,4,5],[16,12,11],'#rubber')]
    model('vehicle_wheel',{'rubber':'vehicle_wheel','particle':'vehicle_wheel'},elements);states('vehicle_wheel',fixture=True)

def note_post():
    im=grain((32,32),(231,218,180));text(im,'NOTE',3,(65,53,36))
    for row in range(12,28,3):
        for x in range(4,26-(row%4)):
            if (x+row)%7!=0:im.putpixel((x,row),(105,96,70,255))
    save('waterline_note',im)
    model('notice_post',{'wood':'minecraft:block/spruce_planks','face':'waterline_note','particle':'minecraft:block/spruce_planks'},
          [box([6,0,6],[10,16,10],'#wood'),box([0,6,6],[16,16,10],'#wood'),box([1,7,5.8],[15,15,6],'#wood','#face')]);states('notice_post')

def cars():
    palette={'red':(135,39,33),'blue':(46,69,101),'green':(73,88,54),'white':(190,185,164),'light_blue':(95,130,137),'brown':(107,74,48),'cyan':(51,101,108),'orange':(164,89,47)}
    kinds=[]
    for name,colour in palette.items():
        im=grain((16,16),colour)
        rect(im,(0,13),(16,16),(44,48,46));rect(im,(0,11),(16,12),(164,161,145));rect(im,(3,3),(4,11),tuple(max(0,c-24) for c in colour));rect(im,(11,3),(12,11),tuple(max(0,c-24) for c in colour));rect(im,(9,4),(12,5),(174,171,155))
        for x,y in [(1,13),(2,12),(14,12),(8,14)]:im.putpixel((x,y),(109,73,43,255))
        save('car_'+name,im)
        hood=grain((16,16),colour);rect(hood,(0,12),(16,16),(72,77,76));rect(hood,(5,9),(11,12),(38,41,41));rect(hood,(1,8),(4,11),(218,213,164));rect(hood,(12,8),(15,11),(218,213,164));save('car_hood_'+name,hood)
        for k,tex,element in [(f'car_{name}','car_'+name,box([0,0,0],[16,16,16],'#body')),(f'hood_{name}','car_hood_'+name,box([0,0,0],[16,8,16],'#body'))]:
            model('town_fixture_'+k,{'body':tex,'particle':tex},[element]);kinds.append(k)
    glass=grain((16,16),(42,60,64));rect(glass,(0,0),(16,3),(68,74,71));rect(glass,(0,14),(16,16),(65,71,69));rect(glass,(0,3),(2,14),(79,83,78));rect(glass,(14,3),(16,14),(79,83,78));rect(glass,(7,3),(9,14),(76,81,78));rect(glass,(3,4),(6,6),(72,103,107));rect(glass,(10,5),(13,7),(61,91,95));save('car_glass',glass)
    model('town_fixture_car_cabin',{'glass':'car_glass','particle':'car_glass'},[box([0,0,0],[16,16,16],'#glass')]);kinds.append('car_cabin')
    wheel=[box([0,8,0],[16,16,16],'#body'),box([4,0,0],[12,8,16],'#rubber'),box([2,2,0],[14,8,16],'#rubber')]
    model('town_fixture_car_wheel',{'body':'minecraft:block/gray_concrete','rubber':'vehicle_wheel','particle':'vehicle_wheel'},wheel);kinds.append('car_wheel')
    path=A/'blockstates/town_fixture.json';data=json.loads(path.read_text())
    for facing,turn in TURN.items():
        for kind in kinds:
            e={'model':f'{NS}:block/town_fixture_{kind}'}
            if turn:e['y']=turn
            data['variants'][f'facing={facing},kind={kind}']=e
    write('blockstates/town_fixture.json',data)

if __name__=='__main__':
    signs();cooler_and_wheel();note_post();cars()
    p=A/'lang/en_us.json';lang=json.loads(p.read_text());lang.update({'block.the_oldest_house.trailer_cooler':'Cooler','block.the_oldest_house.vehicle_wheel':'Wheel','block.the_oldest_house.institute_sign':'Institute sign','block.the_oldest_house.notice_post':'Note'})
    write('lang/en_us.json',lang)
    print('0.4.68 signs, note post, cooler, wheels and eight car finishes generated')
