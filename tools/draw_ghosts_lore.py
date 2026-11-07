"""Draw the Barretts' papers, laptop, stored cross and camera-only shrine at native pixel scale.

0.4.325 adds the sisters' plan: Merry's glittery pocket pad, the sugar and flour canisters (closed, and left open with a spoon and
spilled powder), the small capped jar of white powder on the shrine's bench, and the empty jar Merry sets down by the cross."""
from pathlib import Path
from PIL import Image, ImageDraw
import argparse, io, json, random

ROOT=Path(__file__).resolve().parents[1]
A=ROOT/'src/main/resources/assets/the_oldest_house'
FACES=('north','south','east','west','up','down')
GLYPHS={'D':['110','101','101','101','110'],'U':['101','101','101','101','111'],
        'E':['111','100','110','100','111'],'4':['101','101','111','001','001'],
        'T':['111','010','010','010','010'],'Y':['101','101','010','010','010'],
        'P':['110','101','110','100','100'],'S':['111','100','111','001','111'],
        'F':['111','100','110','100','100'],'A':['010','101','111','101','101'],
        'M':['101','111','111','101','101'],'I':['111','010','010','010','111'],
        'L':['100','100','100','100','111'],'O':['111','101','101','101','111'],
        'R':['110','101','110','101','101'],'G':['111','100','101','101','111']}

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
    # Merry's pocket pad: a glittery yellow cover over its spiral, the page edges showing.
    pad=Image.new('RGBA',(16,16),'#d8b739');d=ImageDraw.Draw(pad)
    for x,y,c in ((2,4,'#f7e68c'),(6,7,'#fff6c6'),(11,5,'#f2db6a'),(4,11,'#f7e68c'),(9,12,'#fff6c6'),(13,9,'#f2db6a'),(7,2,'#b9982b'),(12,13,'#b9982b'),(3,8,'#b9982b')):d.point((x,y),fill=c)
    for x in range(1,15,3):d.rectangle((x,0,x+1,1),fill='#8c8b84')
    out['pad']=pad
    # The jar: a small glass jar, full of white powder or empty but for a dusting at the bottom.
    for name,full in (('jar_full',True),('jar_empty',False)):
        im=Image.new('RGBA',(16,16),'#b9cac8');d=ImageDraw.Draw(im)
        if full:
            d.rectangle((0,2,15,15),fill='#e9e6dc')
            for x,y in ((2,5),(7,9),(12,4),(4,13),(10,12),(14,8)):d.point((x,y),fill='#d3cfc3')
        else:
            d.rectangle((0,13,15,15),fill='#dedad0')
        d.line((2,1,2,12),fill='#dfeceb');d.line((3,1,3,6),fill='#d2e1df');out[name]=im
    # The canisters: cream enamel, a band with the word on it, and what is inside them.
    for name,word_ in (('canister_sugar','SUGAR'),('canister_flour','FLOUR')):
        im=Image.new('RGBA',(32,32),'#ddd5c0');d=ImageDraw.Draw(im)
        d.rectangle((0,9,31,20),fill='#6e898b');word(d,word_,6,12,'#ece6d4')
        d.line((0,30,31,30),fill='#bfb6a0');d.line((4,0,4,31),fill='#e9e2cf');out[name]=im
    powder=Image.new('RGBA',(16,16),'#ece9df');d=ImageDraw.Draw(powder)
    for x,y in ((1,2),(5,6),(9,3),(13,10),(3,12),(11,14),(7,9)):d.point((x,y),fill='#d6d1c4')
    out['powder']=powder
    out.update(shrine_paintings())
    picture=Image.new('RGBA',(32,32),'#7b5d3a');d=ImageDraw.Draw(picture)
    d.rectangle((2,2,29,29),fill='#b2a17d');d.polygon([(3,23),(12,15),(22,20),(28,14),(28,28),(3,28)],fill='#696e50')
    for x,c in ((7,'#6f4b3f'),(18,'#645643')):
        d.rectangle((x,9,x+3,12),fill='#c1ab85');d.polygon([(x,13),(x+3,13),(x+6,25),(x-3,25)],fill=c)
    d.rectangle((22,23,27,26),fill='#d8d1b1');d.rectangle((26,21,28,24),fill='#d8d1b1');out['picture']=picture
    return out

def speckle(im,rng,colours,count):
    d=ImageDraw.Draw(im)
    for _ in range(count):d.point((rng.randrange(im.width),rng.randrange(im.height)),fill=rng.choice(colours))

def shrine_paintings():
    """0.4.325 (owner: "texture custom blocks for the effigy. they should be dark and ominous. still painted in minecraft's vanilla
    style"): the shrine's own materials, 16 pixels to the block like vanilla's, in a few close dark tones each."""
    rng=random.Random(325);out={}
    # Bench wood stained almost black: four planks, grain lines, a knot, the seams darker still.
    wood=Image.new('RGBA',(16,16),'#2e2219');d=ImageDraw.Draw(wood)
    for y in (3,7,11,15):d.line((0,y,15,y),fill='#160f0b')
    for y,shift in ((1,3),(5,9),(9,1),(13,6)):d.line((shift,y,shift+5,y),fill='#3b2b1f');d.point(((shift+9)%16,y+1),fill='#241a13')
    d.point((11,5),fill='#140e0a');d.point((12,5),fill='#1c140f');d.point((4,13),fill='#140e0a')
    speckle(wood,rng,['#33261c','#281d15'],22);out['altar_wood']=wood
    # Drop cloth gone grey-brown with damp: long folds, a tide line, rust-coloured stains low down.
    shroud=Image.new('RGBA',(16,16),'#3b3631');d=ImageDraw.Draw(shroud)
    for x in (2,7,12):d.line((x,0,x,15),fill='#2c2824')
    for x in (4,10):d.line((x,0,x,15),fill='#46403a')
    d.line((0,11,15,10),fill='#322d29')
    for box_ in ((3,12,5,14),(9,13,10,15),(13,11,14,12)):d.rectangle(box_,fill='#4a2f27')
    d.point((4,15),fill='#3a221c');speckle(shroud,rng,['#35302c','#403a35'],18);out['shroud']=shroud
    # Pewter blackened with tarnish, a few dull highlights where it was handled.
    tarnish=Image.new('RGBA',(16,16),'#454741');d=ImageDraw.Draw(tarnish)
    for box_,c in (((1,2,2,6),'#5e605a'),((9,1,10,3),'#666861'),((4,9,7,12),'#2c2e2a'),((11,7,14,9),'#313330'),((6,4,8,5),'#383a36')):d.rectangle(box_,fill=c)
    speckle(tarnish,rng,['#3a3c37','#50524c','#262824'],26);out['tarnish']=tarnish
    # The rag over the head: filthy, knotted, darker at the edges.
    rag=Image.new('RGBA',(16,16),'#4a4237');d=ImageDraw.Draw(rag)
    d.rectangle((0,0,15,1),fill='#352f27');d.rectangle((0,14,15,15),fill='#352f27')
    for x,y in ((3,4),(9,6),(12,10),(5,11)):d.rectangle((x,y,x+1,y+1),fill='#2b261f')
    speckle(rag,rng,['#544b3f','#3e372e'],20);out['rag']=rag
    # Two dim pictures in black frames: a robed man with a knife raised over a kneeling figure, and a lamb tied on a stone.
    knife=Image.new('RGBA',(16,16),'#120d0a');d=ImageDraw.Draw(knife)
    d.rectangle((1,1,14,14),fill='#2c241c');d.rectangle((1,10,14,14),fill='#221b15')
    d.rectangle((4,4,6,10),fill='#4a3324');d.rectangle((4,2,6,3),fill='#6b5442');d.line((7,4,9,2),fill='#7a7a70')
    d.rectangle((9,8,12,10),fill='#3a3a30');d.point((10,7),fill='#6b5442');speckle(knife,rng,['#30281f'],8);out['icon_knife']=knife
    lamb=Image.new('RGBA',(16,16),'#120d0a');d=ImageDraw.Draw(lamb)
    d.rectangle((1,1,14,14),fill='#26201a');d.rectangle((2,10,13,13),fill='#3a3530')
    d.rectangle((4,7,10,9),fill='#8b857a');d.rectangle((10,6,12,8),fill='#8b857a');d.point((11,7),fill='#1a1612')
    for x in (5,9):d.line((x,10,x,10),fill='#5c574e')
    d.line((3,8,13,8),fill='#4a2f27');speckle(lamb,rng,['#2c251e'],8);out['icon_lamb']=lamb
    # Candle stubs: grimy tallow, dark wax runs.
    wax=Image.new('RGBA',(16,16),'#6f6553');d=ImageDraw.Draw(wax)
    for x in (2,6,11):d.line((x,4,x,12+(x%3)),fill='#5a5243')
    d.rectangle((0,0,15,2),fill='#4a4337');speckle(wax,rng,['#7a705d','#625946'],14);out['wax']=wax
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
    # The stored cross as the novel has it: tarnished and smeared with dirt, a dirty rag over the head.
    cross=[box((7,0,8),(9,32,10),'tarnish'),box((-2,23,8),(18,26,10),'tarnish'),box((5,24,5),(11,30,10.5),'rag')]
    # 0.4.325: the shrine in its own dark materials (shrine_paintings): black-stained bench, damp drop cloths, tarnished figure and
    # cross, a filthy rag, two dim pictures, and three burnt-down candles on the bench.
    shrine=[box((-4,7,2),(20,9,15),'altar_wood'),box((-3,0,3),(-1,7,5),'altar_wood'),box((17,0,12),(19,7,14),'altar_wood'),
            box((-4,10,14),(1,32,14.3),'shroud'),box((15,10,14),(20,32,14.3),'shroud'),
            box((5.5,16,6),(10.5,24.4,8),'tarnish'),box((0,22,6),(16,23.5,8),'tarnish'),
            box((6,9,6.5),(7.5,16,8),'tarnish'),box((8.5,9,6.5),(10,16,8),'tarnish'),box((5,14,5.5),(11,17,8.5),'rag'),
            box((-3,9,12),(3,17,12.4),'altar_wood',{'north':'icon_knife'}),box((13,9,12),(19,17,12.4),'altar_wood',{'north':'icon_lamb'}),
            box((-2.5,9,4),(-1,11.5,5.5),'wax'),box((-1.9,11.5,4.6),(-1.6,12.3,4.9),'dark'),
            box((12,9,3.5),(13.5,10.5,5),'wax'),box((15.5,9,5),(17,12,6.5),'wax'),box((16.1,12,5.6),(16.4,12.8,5.9),'dark')]
    pad=[box((5,0,6),(10.5,.6,13),'paper',{'up':'pad'}),box((11.5,0,6.5),(12.1,.5,12.5),'dark')]
    def canister(x0,word_,h,lid):
        out=[box((x0,0,6),(x0+4.5,h,10.5),'canister_'+word_,{'up':'pewter' if lid else 'powder','down':'pewter'})]
        if lid:out.append(box((x0-.2,h,5.8),(x0+4.7,h+.6,10.7),'pewter'))
        return out
    closed_tins=canister(2.5,'sugar',7,True)+canister(9,'flour',8,True)
    open_tins=canister(2.5,'sugar',7,False)+canister(9,'flour',8,False)+[
        box((1,0,11.5),(5.5,.5,15.5),'pewter'),box((9.5,0,11.8),(14,.5,15.8),'pewter'),
        box((6,0,4),(7.6,.3,12),'pewter'),box((6.2,.3,3.2),(7.4,.6,4.6),'pewter'),
        box((5.5,0,12),(9,.12,14),'powder'),box((7.5,0,14),(9.5,.1,15.5),'powder'),box((0.5,0,4.5),(2.5,.1,6),'powder')]
    jar_on_bench=[box((2.5,9,8),(5.5,12.5,11),'jar_full'),box((2.3,12.5,7.8),(5.7,13.3,11.2),'pewter')]
    empty=[box((6.5,0,3),(9.5,3.5,6),'jar_empty'),box((6.3,3.5,2.8),(9.7,4.3,6.2),'pewter')]
    return {name:{'textures':textures,'elements':elements,'ambientocclusion':True} for name,elements in {
        'bank_slips':papers,'laptop_closed':closed,'laptop_study':opened,'stored_cross':cross,'basement_effigy':shrine,
        'plan_pad':pad,'canisters':closed_tins,'canisters_open':open_tins,'basement_effigy_jar':shrine+jar_on_bench,'empty_jar':empty}.items()}

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
    print(('Verified' if args.check else 'Drew')+' the Ghosts family papers, laptop, cross, lens images, the sisters\' plan and the sister-cats drawing.')
if __name__=='__main__':main()
