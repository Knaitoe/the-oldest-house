"""Draw the Barretts' papers, laptop, stored cross and camera-only shrine at native pixel scale."""
from pathlib import Path
from PIL import Image, ImageDraw
import argparse, io, json

ROOT=Path(__file__).resolve().parents[1]
A=ROOT/'src/main/resources/assets/the_oldest_house'
FACES=('north','south','east','west','up','down')
GLYPHS={'D':['110','101','101','101','110'],'U':['101','101','101','101','111'],
        'E':['111','100','110','100','111'],'4':['101','101','111','001','001'],
        'T':['111','010','010','010','010'],'Y':['101','101','010','010','010'],
        'P':['110','101','110','100','100'],'S':['111','100','111','001','111'],
        'F':['111','100','110','100','100'],'A':['010','101','111','101','101'],
        'M':['101','111','111','101','101'],'I':['111','010','010','010','111'],
        'L':['100','100','100','100','111']}

def word(draw,text,x,y,color,scale=1):
    for c in text:
        for row,line in enumerate(GLYPHS.get(c,['000']*5)):
            for col,pixel in enumerate(line):
                if pixel=='1':draw.rectangle((x+col*scale,y+row*scale,x+(col+1)*scale-1,y+(row+1)*scale-1),fill=color)
        x+=4*scale

def paintings():
    out={}
    paper=Image.new('RGBA',(32,32),'#e4ddc7');d=ImageDraw.Draw(paper)
    d.line((2,2,29,2),fill='#8b9798');d.line((3,6,25,6),fill='#81827b')
    for y in (10,13,16,27):d.line((4,y,26-(y%3)*2,y),fill='#aaa393')
    word(d,'DUE',8,20,'#9b5e52');d.line((17,0,17,31),fill='#d5ccb5');out['paper']=paper
    keys=Image.new('RGBA',(32,16),'#777b79');d=ImageDraw.Draw(keys)
    for y in (2,5,8):
        for x in range(2,30,4):d.rectangle((x,y,x+2,y+1),fill='#323839')
    d.rectangle((12,11,20,14),fill='#5a6263');out['keyboard']=keys
    screen=Image.new('RGBA',(64,48),'#deded4');d=ImageDraw.Draw(screen)
    d.rectangle((0,0,63,10),fill='#43596a');word(d,'FAMILY',3,3,'#e4e4d9')
    word(d,'4',5,17,'#415868',3);word(d,'TYPES',23,14,'#555d5c')
    d.rectangle((22,25,59,30),fill='#d9ce87')
    for y,length in ((23,35),(27,36),(31,31),(36,35),(40,29),(44,34)):d.line((23,y,23+length,y),fill='#83857d')
    out['screen']=screen
    for name,color in [('pewter','#777870'),('cloth','#d7d0b8')]:
        im=Image.new('RGBA',(16,16),color);d=ImageDraw.Draw(im)
        if name=='pewter':
            for box,c in [((1,2,3,5),'#8a8980'),((9,1,11,3),'#969489'),((4,10,7,13),'#62665d'),((12,8,14,10),'#686c62')]:d.rectangle(box,fill=c)
            d.line((3,1,3,8),fill='#99988c')
        else:
            for x in (2,6,11,14):d.line((x,0,x,15),fill='#c4bca4')
            for box in ((1,11,4,14),(10,7,12,9),(6,14,10,15)):d.rectangle(box,fill='#b5a98e')
        out[name]=im
    picture=Image.new('RGBA',(32,32),'#7b5d3a');d=ImageDraw.Draw(picture)
    d.rectangle((2,2,29,29),fill='#b2a17d');d.polygon([(3,23),(12,15),(22,20),(28,14),(28,28),(3,28)],fill='#696e50')
    for x,c in ((7,'#6f4b3f'),(18,'#645643')):
        d.rectangle((x,9,x+3,12),fill='#c1ab85');d.polygon([(x,13),(x+3,13),(x+6,25),(x-3,25)],fill=c)
    d.rectangle((22,23,27,26),fill='#d8d1b1');d.rectangle((26,21,28,24),fill='#d8d1b1');out['picture']=picture
    return out

def cats(vines):
    im=Image.new('RGBA',(64,48),'#e7ddbf');d=ImageDraw.Draw(im)
    d.line((31,0,31,47),fill='#d5c8a5');d.line((4,42,59,42),fill='#92875e')
    d.line((7,40,7,16,29,4,56,16,56,40),fill='#947b61',width=2)
    for x,y,size,shirt in [(19,13,12,'#8c8982'),(41,22,9,'#ac6749')]:
        d.rectangle((x,y,x+size,y+size),fill='#a17b4c');d.polygon([(x,y),(x+2,y-5),(x+5,y),(x+size-5,y),(x+size-2,y-5),(x+size,y)],fill='#a17b4c')
        d.rectangle((x,y+size,x+size,y+size+9),fill=shirt);d.line((x+2,y+size+10,x+2,41),fill='#746c5d',width=2);d.line((x+size-2,y+size+10,x+size-2,41),fill='#746c5d',width=2)
        d.point((x+3,y+4),fill='#433e33');d.point((x+size-3,y+4),fill='#433e33');d.line((x+4,y+8,x+size-4,y+8),fill='#69513c')
        if size==9:
            d.rectangle((x+1,y+2,x+4,y+5),outline='#4c4c43');d.rectangle((x+5,y+2,x+8,y+5),outline='#4c4c43')
    d.line((30,31,35,33,42,34),fill='#a17b4c',width=2)
    if vines:
        for points in [[(1,46),(6,38),(4,31),(10,24),(8,15),(13,8),(11,2)],[(60,46),(54,38),(56,28),(50,21),(52,12),(46,5)],[(3,40),(15,36),(24,38),(34,32),(47,35),(60,29)]]:
            d.line(points,fill='#466b3c',width=2)
            for x,y in points[1:-1]:d.polygon([(x,y),(x+4,y-3),(x+4,y),(x+1,y+2)],fill='#66834d')
    return im

def box(lo,hi,texture,over=None,rotation=None):
    faces={f:{'texture':'#'+texture,'uv':[0,0,16,16]} for f in FACES}
    for f,t in (over or {}).items():faces[f]['texture']='#'+t
    result={'from':list(lo),'to':list(hi),'faces':faces}
    if rotation:result['rotation']=rotation
    return result

def models():
    textures={n:'the_oldest_house:block/ghosts_lore/'+n for n in paintings()}
    textures.update(wood='minecraft:block/spruce_planks',dark='minecraft:block/gray_concrete',black='minecraft:block/black_concrete',particle='the_oldest_house:block/ghosts_lore/pewter')
    base=[box((2,-1,4),(14,0,14),'dark',{'up':'keyboard'})]
    closed=base+[box((2,0,4),(14,1,14),'dark')]
    opened=base+[box((2,0,12),(14,9,13),'black',{'north':'screen'}, {'origin':[8,0,12],'axis':'x','angle':22.5})]
    papers=[box((2,0,3),(14,.2,13),'paper'),box((3,.2,4),(15,.4,14),'paper'),box((9,.4,10),(12,.55,10.3),'pewter'),box((11.7,.4,10),(12,.55,13),'pewter')]
    cross=[box((7,0,8),(9,32,10),'pewter'),box((-2,23,8),(18,26,10),'pewter'),box((5,24,5),(11,30,10.5),'cloth')]
    shrine=[box((-4,7,2),(20,9,15),'wood'),box((-3,0,3),(-1,7,5),'wood'),box((17,0,12),(19,7,14),'wood'),
            box((-4,10,14),(1,32,14.3),'cloth'),box((15,10,14),(20,32,14.3),'cloth'),
            box((5.5,16,6),(10.5,24.4,8),'pewter'),box((0,22,6),(16,23.5,8),'pewter'),
            box((6,9,6.5),(7.5,16,8),'pewter'),box((8.5,9,6.5),(10,16,8),'pewter'),box((5,14,5.5),(11,17,8.5),'cloth'),
            box((-3,9,12),(3,17,12.4),'wood',{'north':'picture'}),box((13,9,12),(19,17,12.4),'wood',{'north':'picture'})]
    return {name:{'textures':textures,'elements':elements,'ambientocclusion':True} for name,elements in {
        'bank_slips':papers,'laptop_closed':closed,'laptop_study':opened,'stored_cross':cross,'basement_effigy':shrine}.items()}

def main():
    parser=argparse.ArgumentParser();parser.add_argument('--check',action='store_true');args=parser.parse_args();files={}
    for name,im in paintings().items():
        buf=io.BytesIO();im.save(buf,format='PNG');files[A/('textures/block/ghosts_lore/'+name+'.png')]=buf.getvalue()
    for suffix,flag in [('',False),('_vines',True)]:
        buf=io.BytesIO();cats(flag).save(buf,format='PNG');files[A/('textures/gui/ghosts_sister_cats'+suffix+'.png')]=buf.getvalue()
    states=A/'blockstates/ghosts_set.json';current=json.loads(states.read_text());variants={}
    for name,model in models().items():
        files[A/('models/block/ghosts_'+name+'.json')]=(json.dumps(model,indent=2)+'\n').encode()
        for facing,y in [('north',0),('east',90),('south',180),('west',270)]:variants['kind='+name+',facing='+facing]={'model':'the_oldest_house:block/ghosts_'+name,'y':y}
    if args.check:
        for path,raw in files.items():assert path.read_bytes()==raw,path
        for key,value in variants.items():assert current['variants'].get(key)==value,key
    else:
        for path,raw in files.items():path.parent.mkdir(parents=True,exist_ok=True);path.write_bytes(raw)
        current['variants'].update(variants);states.write_text(json.dumps(current,indent=2)+'\n')
    print(('Verified' if args.check else 'Drew')+' the Ghosts family papers, laptop, cross, lens image and sister-cats drawing.')
if __name__=='__main__':main()
