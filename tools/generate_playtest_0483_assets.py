"""Exact native camp meshes, fitted pixel textures, cottage chessmen and jointed-paw atlas. No existing skin is replaced."""
from pathlib import Path
import json, random
from PIL import Image, ImageDraw

ROOT=Path(__file__).resolve().parents[1]
A=ROOT/'src/main/resources/assets/the_oldest_house'
T=A/'textures/block/camp'
T.mkdir(parents=True,exist_ok=True)
M=A/'models/block'
NS='the_oldest_house:block/camp/'
DIRS=('north','south','east','west','up','down')

def save(name,paint,size=16):
    im=Image.new('RGBA',(size,size));d=ImageDraw.Draw(im);r=random.Random(name)
    paint(im,d,r);im.save(T/f'{name}.png',optimize=True)

def wood(im,d,r):
    for y in range(16):
        for x in range(16):
            n=r.randint(-7,7);seam=y in (0,7,8,15);v=-24 if seam else 0
            im.putpixel((x,y),(104+n+v,83+n+v,56+n+v,255))
    for x,y in [(2,2),(13,2),(2,10),(13,10)]:d.point((x,y),fill='#453f34')
    d.line((5,5,10,5),fill='#4b3f2d');d.point((10,4),fill='#4b3f2d')
save('cedar',wood)
def floor(im,d,r):
    wood(im,d,r)
    for x in (3,6,10,12):d.line((x,2,x+1,6),fill='#837255');d.line((x,10,x+1,14),fill='#776647')
save('floor',floor)
def iron(im,d,r):
    for y in range(16):
        for x in range(16):
            n=r.randint(-5,5);im.putpixel((x,y),(58+n,65+n,61+n,255))
    d.line((0,0,15,0),fill='#84908a');d.line((0,15,15,15),fill='#333d38')
    for x,y in ((2,2),(13,2),(2,13),(13,13)):d.point((x,y),fill='#acaa8c')
    d.rectangle((11,7,13,9),fill='#795538')
save('iron',iron)
def glass(im,d,r):
    for y in range(16):
        for x in range(16):
            n=r.randint(-4,4);im.putpixel((x,y),(191+n,145+n,64+n,255))
    d.rectangle((0,0,15,15),outline='#3e4d43');d.line((3,1,3,14),fill='#e2c47b');d.line((12,1,12,14),fill='#806a39');d.line((0,11,15,11),fill='#53614b')
save('lantern_glass',glass)
COLORS=[(111,57,44),(70,92,80),(72,85,112),(133,111,60),(109,75,90),(93,97,73)]
for i,c in enumerate(COLORS):
    def blanket(im,d,r,c=c):
        for y in range(16):
            for x in range(16):
                n=(8 if x%4==0 else 0)+(7 if y%4==0 else 0)+r.randint(-3,3)
                im.putpixel((x,y),tuple(min(255,v+n) for v in c)+(255,))
        d.rectangle((0,0,15,3),fill='#c6bea4');d.line((0,3,15,3),fill='#a49b7e');d.line((0,14,15,14),fill='#383c34')
    save(f'blanket_{i}',blanket)
def canvas(im,d,r):
    for y in range(16):
        for x in range(16):
            n=r.randint(-4,4)+(3 if (x+y)%2 else 0);im.putpixel((x,y),(127+n,128+n,85+n,255))
    for x in (3,12):d.line((x,0,x,15),fill='#494d3d')
    d.rectangle((6,5,9,10),outline='#9c9673');d.line((6,8,9,8),fill='#30372b')
save('canvas',canvas)
def boots(im,d,r):
    for y in range(16):
        for x in range(16):
            n=r.randint(-4,4);im.putpixel((x,y),(51+n,45+n,35+n,255))
    d.rectangle((0,12,15,15),fill='#202521');d.line((7,0,7,10),fill='#8b805e')
save('boots',boots)
def ring(im,d,r):
    d.rectangle((0,0,15,15),fill='#ccc7ad')
    for y in range(16):
        for x in range(16):
            if y in range(4,8) or x in range(4,8):im.putpixel((x,y),(161,74,45,255))
    d.rectangle((0,0,15,15),outline='#878066');d.line((0,14,15,14),fill='#777761')
save('ring',ring)
def aid(im,d,r):
    d.rectangle((0,0,15,15),fill='#c2c6b0');d.rectangle((0,0,15,15),outline='#5a695b');d.rectangle((6,3,9,12),fill='#ac554a');d.rectangle((3,6,12,9),fill='#ac554a');d.point((13,2),fill='#79866d')
save('first_aid',aid)
def oar(im,d,r):
    wood(im,d,r);d.line((4,0,4,15),fill='#bca07a');d.line((12,0,12,15),fill='#52432d')
save('oar',oar)
for i in range(6):
    def sign(im,d,r,i=i):
        wood(im,d,r);d.rectangle((1,1,14,14),outline='#cac29d')
        # Cabin number and a small lake/pine emblem, drawn as intentional pixels.
        digits=['010110010010111','110001010100111','110001010001110','101101111001001','111100110001110','011100110101010']
        for y,row in enumerate([digits[i][n:n+3] for n in range(0,15,3)]):
            for x,c in enumerate(row):
                if c=='1':d.rectangle((6+x,3+y,6+x,3+y),fill='#e1d8b9')
        d.line((3,11,12,11),fill='#6d9391');d.line((4,12,11,12),fill='#9da896')
    save(f'sign_{i}',sign)
    def notice(im,d,r,i=i):
        wood(im,d,r);d.rectangle((2,2,13,13),fill='#c9bf98');d.line((3,4,12,4),fill='#5e5d44')
        for y,end in ((6,11),(8,10),(10,12)):d.line((4,y,end,y),fill='#777456')
        d.rectangle((9,11,11,12),fill='#9f5546');d.point((2,2),fill='#8d8975')
    save(f'notice_{i}',notice)

BOXES={
 'wall':[(0,0,0,16,16,16)],'floor':[(0,0,0,16,16,16)],
 'bed_head':[(0,12,0,16,16,16),(0,0,0,2,21,2),(14,0,0,16,21,2),(2,17,0,14,21,2)],
 'bed_foot':[(0,12,0,16,16,16),(0,0,14,2,17,16),(14,0,14,16,17,16),(2,15,14,14,17,16)],
 'sign':[(1,3,14,15,11,16)],'notice':[(1,1,14,15,15,16)],'first_aid':[(3,3,11,13,12,16)],
 'oar':[(7,0,14,9,16,16),(5,10,13,11,16,15)],'ring':[(3,3,13,6,13,16),(10,3,13,13,13,16),(6,3,13,10,6,16),(6,10,13,10,13,16)],
 'trunk':[(1,0,2,15,7,14),(1,7,2,15,9,14)],'boots':[(3,0,4,6,3,11),(3,3,7,6,7,11),(10,0,4,13,3,11),(10,3,7,13,7,11)],
 'lantern':[(4,1,4,12,11,12),(3,0,3,13,2,13),(3,11,3,13,13,13),(7,13,7,9,16,9)]}
variants={}
for kind,boxes in BOXES.items():
    for stage in range(6):
        tex='cedar' if kind=='wall' else 'floor' if kind=='floor' else 'canvas' if kind=='trunk' else f'{kind}_{stage}' if kind in ('sign','notice') else kind if kind in ('oar','ring','boots','first_aid') else 'iron'
        textures={'main':NS+tex,'iron':NS+'iron','mattress':NS+f'blanket_{stage}','particle':NS+tex}
        els=[]
        for n,b in enumerate(boxes):
            t='mattress' if kind.startswith('bed_') and n==0 else 'iron' if kind.startswith('bed_') else 'main'
            if kind=='lantern' and n==0:textures['glass']=NS+'lantern_glass';t='glass'
            faces={d:{'texture':'#'+t,'uv':[0,0,16,16]} for d in DIRS}
            el={'from':list(b[:3]),'to':list(b[3:]),'faces':faces}
            if kind=='lantern' and n==0:el['neoforge_data']={'block_light':11,'sky_light':0}
            els.append(el)
        model=f'camp_{kind}_{stage}'
        (M/f'{model}.json').write_text(json.dumps({'parent':'minecraft:block/block','render_type':'minecraft:cutout','textures':textures,'elements':els},separators=(',',':'))+'\n')
        for facing,y in [('north',0),('east',90),('south',180),('west',270)]:variants[f'facing={facing},kind={kind},stage={stage}']={'model':'the_oldest_house:block/'+model,**({'y':y} if y else {})}
(A/'blockstates/camp_decor.json').write_text(json.dumps({'variants':variants},separators=(',',':'))+'\n')

# Native cottage board, with individually modelled contrasting chessmen.
im=Image.new('RGBA',(32,32),'#705437');d=ImageDraw.Draw(im)
for z in range(8):
    for x in range(8):d.rectangle((4+3*x,4+3*z,6+3*x,6+3*z),fill='#c7b58c' if (x+z)%2==0 else '#493d2f')
d.rectangle((2,2,29,29),outline='#af9060');im.save(A/'textures/block/cottage/chessboard.png',optimize=True)
els=[{'from':[1,0,1],'to':[15,1,15],'faces':{d:{'texture':'#board'} for d in DIRS}}]
for color,zs in [('white',(0,1)),('black',(6,7))]:
    for z in zs:
        for x in range(8):
            if (color,z,x) in [('white',1,4),('black',6,3)]:continue
            px,pz=2+x*1.5,2+z*1.5;h=2.8 if z in (0,7) and x in (3,4) else 2.2 if z in (0,7) else 1.7
            for frm,to in [([px-.45,1,pz-.45],[px+.45,1.5,pz+.45]),([px-.27,1.5,pz-.27],[px+.27,h,pz+.27]),([px-.4,h,pz-.4],[px+.4,h+.4,pz+.4])]:els.append({'from':frm,'to':to,'faces':{d:{'texture':'#'+color} for d in DIRS}})
(M/'cottage_chessboard.json').write_text(json.dumps({'parent':'minecraft:block/block','textures':{'board':'the_oldest_house:block/cottage/chessboard','white':'minecraft:block/sandstone_top','black':'minecraft:block/black_terracotta','particle':'the_oldest_house:block/cottage/chessboard'},'elements':els},separators=(',',':'))+'\n')
p=A/'blockstates/cottage.json';table=json.loads(p.read_text())
for facing,y in [('north',0),('east',90),('south',180),('west',270)]:
    for stage in range(2):table['variants'][f'facing={facing},kind=chessboard,stage={stage}']={'model':'the_oldest_house:block/cottage_chessboard',**({'y':y} if y else {})}
p.write_text(json.dumps(table,indent=1)+'\n')

# Jointed-paw UV atlas: opaque native cube faces, dry creases and lighter tendons.
im=Image.new('RGBA',(64,32));d=ImageDraw.Draw(im);r=random.Random(483)
for y in range(32):
    for x in range(64):
        n=r.randint(-9,9);im.putpixel((x,y),(122+n,96+n,65+n,255))
for x in (2,8,14,26,31):d.line((x,0,x+1,28),fill='#a89470');d.line((x+2,0,x+2,28),fill='#554533')
for box in ((1,3,18,7),(2,9,19,10),(0,16,7,17),(12,16,19,17),(24,16,31,17)):d.rectangle(box,outline='#4e3d2d')
for x in (1,5,13,17,25,29):d.point((x,15),fill='#dac397')
im.save(A/'textures/entity/paw_clutch.png',optimize=True)
im=Image.new('RGBA',(16,16),(0,0,0,0));d=ImageDraw.Draw(im);d.rectangle((1,4,14,12),fill='#d5ccac',outline='#8a7c5d');d.line((2,5,7,9,13,5),fill='#a69873');d.rectangle((7,8,9,10),fill='#875647');im.save(A/'textures/item/mill_letter.png',optimize=True)
(A/'models/item/mill_letter.json').write_text(json.dumps({'parent':'minecraft:item/generated','textures':{'layer0':'the_oldest_house:item/mill_letter'}})+'\n')
p=A/'lang/en_us.json';lang=json.loads(p.read_text());lang['block.the_oldest_house.camp_decor']='Camp furnishing';lang['item.the_oldest_house.mill_letter']='Envelope from the mill';p.write_text(json.dumps(lang,indent=2,ensure_ascii=False)+'\n')
print('288 camp variants; 27 camp textures; modelled chessboard; jointed paw and readable envelope art')
