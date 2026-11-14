"""Draw fitted pixel materials and native meshes for the night-duty hospital only."""
from pathlib import Path
import argparse
import copy
import json
from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parents[1]
A = ROOT / 'src/main/resources/assets/the_oldest_house'
PREFIX = 'the_oldest_house:block/night_ward/'

def paintings():
    out = {}
    def tile(name, color):
        im = Image.new('RGBA', (32, 32), color)
        out[name] = im
        return ImageDraw.Draw(im)
    d=tile('wall','#d7d9cf')
    for x,y in [(3,9),(17,25),(28,3),(9,28)]:d.point((x,y),fill='#c7cdc5')
    d.line((0,31,31,31),fill='#b7c2bc')
    d=tile('dado','#92aca8')
    for y in (0,16,31):d.line((0,y,31,y),fill='#647c7a')
    for x in (0,16,31):d.line((x,0,x,31),fill='#b9c9bf')
    d.line((1,2,30,2),fill='#c8d3c8')
    d=tile('floor','#c9cbbd')
    d.rectangle((0,0,15,15),fill='#adbdb7');d.rectangle((16,16,31,31),fill='#adbdb7')
    for x in range(2,32,5):
        for y in range(3,32,7):d.point((x,y),fill='#909f98');d.point(((x+2)%32,(y+3)%32),fill='#e0dfcf')
    for n in (0,16):d.line((n,0,n,31),fill='#7f948e');d.line((0,n,31,n),fill='#7f948e')
    d=tile('ceiling','#d4d5c8');d.rectangle((0,0,31,31),outline='#82938f');d.rectangle((2,2,29,29),outline='#e6e6d6')
    for y in range(5,29,4):
        for x in range(5,29,6):d.point((x,y),fill='#b9c1b8')
    d=tile('enamel','#d9ddd2');d.rectangle((0,0,31,31),outline='#7c9591');d.line((2,2,29,2),fill='#f0ecdc')
    for x,y in [(2,23),(29,4),(8,30)]:d.line((x,y,min(x+2,31),y),fill='#a0ada5')
    d=tile('steel','#8a9c9a')
    for y in (2,7,12,18,25,30):d.line((0,y,31,y),fill='#a8b5ae')
    d.line((0,31,31,31),fill='#5d7271')
    d=tile('sheet','#e5e2d3')
    for y in (4,7,24,28):d.line((1,y,30,y),fill='#c6caba')
    d.line((3,2,3,29),fill='#f4eedc');d.line((28,2,28,29),fill='#b6c1b5')
    d=tile('blanket','#597f8a')
    for x in range(1,32,4):d.line((x,0,x,31),fill='#678c94')
    for y in (2,3,27,29):d.line((0,y,31,y),fill='#bdc9bc')
    for x in range(2,32,3):d.point((x,30),fill='#364e59')
    d=tile('curtain','#c5d5d0')
    for x in range(0,32,8):
        d.rectangle((x,0,x+1,31),fill='#8faeaa');d.line((x+3,0,x+3,31),fill='#e4e8d8')
    d.line((0,28,31,28),fill='#8faeaa');d.line((0,30,31,30),fill='#e4e8d8')
    d=tile('glass',(0,0,0,0));d.rectangle((0,0,31,31),outline='#9dbfb9');d.rectangle((1,1,30,30),outline='#d5e3d6')
    for x,y in [(6,7),(7,6),(8,5),(24,23),(25,22)]:d.line((x,y,x+2,y),fill='#c9ded3')
    d=tile('light','#cddbc7');d.rectangle((0,0,31,31),outline='#6e8583',width=2)
    for x in range(4,29,4):d.line((x,3,x,28),fill='#edf0d8')
    for y in (3,28):d.line((3,y,28,y),fill='#8aa19b')
    d=tile('call','#d9ddd2');d.rectangle((2,2,29,29),outline='#68847f');d.rectangle((5,10,26,24),outline='#ad5551',width=2)
    for x in (4,27):
        for y in (4,27):d.line((x-1,y,x+1,y),fill='#738985')
    # A red cross sits above the existing native call button, at its unchanged position.
    d.rectangle((14,3,17,8),fill='#a9504e');d.rectangle((12,5,19,6),fill='#a9504e')
    # The call button itself: an enamel bezel (left half) and a red cap (right half).
    d=tile('call_button',(0,0,0,0));d.rectangle((0,0,13,11),fill='#e2e1d4',outline='#7c9591');d.line((1,1,12,1),fill='#f3efe0')
    d.rectangle((0,12,13,13),fill='#9fb1ab');d.rectangle((18,2,23,7),fill='#b8423d');d.line((18,2,23,2),fill='#de7268');d.line((18,2,18,7),fill='#de7268')
    d.line((19,7,23,7),fill='#7d2a28');d.line((23,3,23,7),fill='#7d2a28');d.point((20,4),fill='#f0a097');d.rectangle((18,10,23,12),fill='#8e302c')
    d=tile('controls','#d9ddd2');d.rectangle((1,0,16,3),fill='#435e5e');d.line((3,2,7,2,8,0,10,3,12,1,14,1),fill='#acd0b3')
    d.rectangle((20,0,23,3),fill='#ad5551');d.rectangle((25,1,27,2),fill='#7b9e83')
    d=tile('door_lower','#c4d2c8');d.rectangle((1,1,30,30),outline='#809a94');d.rectangle((5,5,26,28),outline='#e0e2d2')
    d.rectangle((23,8,26,19),fill='#7f9390');d.line((24,9,24,18),fill='#d9dfd0')
    d=tile('door_upper','#c4d2c8');d.rectangle((1,1,30,30),outline='#809a94');d.rectangle((6,5,25,24),fill='#789a98');d.rectangle((7,6,24,23),fill='#b6cebf')
    for n in (11,17,23):d.line((7,n,24,n),fill='#8caaa0');d.line((n,6,n,23),fill='#8caaa0')
    d.rectangle((10,27,21,29),fill='#a95650')
    # 0.4.199, the clinical ward (NightWard): its new fittings and the bed curtains.
    d=tile('column','#d3d8cd');d.rectangle((0,0,31,31),outline='#7c9591');d.rectangle((3,0,28,31),outline='#b9c4bb')
    for y,ring in ((5,'#4f9a5c'),(13,'#d1b04a'),(21,'#e6e6dc')):
        d.ellipse((11,y,20,y+6),fill='#eef0e6',outline='#6e807c');d.ellipse((14,y+2,17,y+4),fill=ring)
    d.rectangle((9,28,22,29),fill='#8fa29c')
    d=tile('light_off','#a7b2aa');d.rectangle((0,0,31,31),outline='#6e8583',width=2)
    for x in range(4,29,4):d.line((x,3,x,28),fill='#b8c2b8')
    for y in (3,28):d.line((3,y,28,y),fill='#8aa19b')
    d=tile('bed_unit','#cfd4c8');d.rectangle((0,4,31,27),fill='#dfe2d6',outline='#7c9591');d.line((1,5,30,5),fill='#f0efe2');d.line((0,27,31,27),fill='#5f7471')
    for x,ring in ((4,'#4f9a5c'),(10,'#d1b04a')):d.ellipse((x,11,x+5,16),fill='#eef0e6',outline='#6e807c');d.point((x+2,13),fill=ring);d.point((x+3,13),fill=ring)
    for x in (17,22):d.rectangle((x,11,x+3,16),fill='#f2f2ea',outline='#8a9893');d.point((x+1,13),fill='#55605e');d.point((x+2,13),fill='#55605e')
    d.rectangle((27,11,29,15),fill='#b8423d');d.rectangle((3,20,28,22),fill='#c9cfc3');d.line((5,21,14,21),fill='#8a9893')
    d=tile('monitor','#3b4646');d.rectangle((2,2,29,25),fill='#121c19');d.rectangle((0,0,31,31),outline='#283131')
    d.line((3,15,8,15,10,9,12,20,14,15,19,15,21,10,23,19,25,15,28,15),fill='#7fe08a')
    for x,y in ((4,4),(5,4),(6,4),(4,5),(6,5),(4,6),(5,6),(6,6)):d.point((x,y),fill='#e0c45a')
    d.rectangle((22,4,27,7),fill='#2d6b8a');d.rectangle((4,27,9,29),fill='#7fe08a');d.rectangle((24,27,27,29),fill='#b8423d')
    d=tile('night_light','#d9ddd2');d.rectangle((2,2,29,29),fill='#e8c27a',outline='#8c7a52')
    for y in range(6,28,5):d.line((4,y,27,y),fill='#c79f55')
    d=tile('trolley','#5f7f99');d.rectangle((0,0,31,31),outline='#3e586c')
    for y in (10,21):d.line((1,y,30,y),fill='#3e586c')
    for y in (5,15,26):d.rectangle((12,y,19,y+1),fill='#c8d2d6')
    d=tile('bag','#d8e6e2');d.rectangle((0,0,31,31),outline='#9fb8b2');d.rectangle((4,16,27,29),fill='#c7dcd6');d.rectangle((8,5,23,12),fill='#f2f2ea',outline='#9fb8b2');d.line((10,8,20,8),fill='#7a8b86')
    d=tile('rubber','#2b2f2f');d.line((0,0,31,0),fill='#454b4b')
    d=tile('counter','#a9bfb3');d.rectangle((0,0,31,31),outline='#6f8a80')
    for x in (10,21):d.line((x,2,x,29),fill='#8ea79c')
    d.line((0,2,31,2),fill='#c6d6cc')
    d=tile('worktop','#e3dfcf')
    for x,y in [(3,5),(11,19),(23,8),(28,26),(16,13),(6,27)]:d.point((x,y),fill='#c5c0ad')
    d.line((0,31,31,31),fill='#b0ab98')
    d=tile('ceramic','#eef0e8');d.line((0,0,31,0),fill='#fbfbf4');d.line((0,31,31,31),fill='#c9cdc4')
    for x,y in [(5,9),(20,22)]:d.line((x,y,x+4,y),fill='#fbfbf4')
    # 0.4.590 (owner: "hospital curtains need improvement"): what is new from here on is drawn at sixteen pixels to the block, one
    # texel to a model pixel. The bed curtains are cubicle curtains: a header tape with eyelets, a band of open mesh, a seam, cloth
    # in folds, and a turned hem. Each sprite's left half is a fold standing toward the eye and its right half a fold lying back,
    # so a face takes its columns from the side it is seen from.
    def small(name):
        im = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
        out[name] = im
        return im.load()
    def rgb(c):return tuple(int(c[i:i+2],16) for i in (1,3,5))
    def mix(c,to,k):
        a,b=rgb(c),rgb(to);return tuple(round(a[i]+(b[i]-a[i])*k) for i in range(3))+(255,)
    def solid(c):return rgb(c)+(255,)
    NEAR=['#a9cfd6','#86b3bf','#86b3bf','#78a6b3','#86b3bf','#86b3bf','#a9cfd6','#78a6b3']
    FAR=['#5a8593','#6c98a6','#6c98a6','#5a8593','#6c98a6','#6c98a6','#7aa5b2','#5a8593']
    FOLDS=NEAR+FAR
    PLEATS=['#9cc5cf','#6c98a6','#86b3bf','#557d8c','#9cc5cf','#6c98a6','#9cc5cf','#557d8c','#86b3bf','#6c98a6','#9cc5cf','#557d8c','#86b3bf','#9cc5cf','#6c98a6','#557d8c']
    def upper(name,cols,open_mesh):
        px=small(name)
        for x in range(16):
            eyelet=x%3==1 if open_mesh else x%2==1
            px[x,1]=solid('#6f7f7e') if eyelet else solid('#eceee4')
            for y in range(2,7):
                thread='#dbe6df' if (x<8 if open_mesh else x%2==0) else '#bccdc8'
                if open_mesh:
                    if y%2==0 or x%2==0:px[x,y]=solid(thread)
                else:px[x,y]=mix(thread,'#000000',.12 if y==4 else 0)
            px[x,7]=mix(cols[x],'#000000',.35)
            for y in range(8,16):px[x,y]=solid(cols[x])
    def lower(name,cols):
        px=small(name)
        for x in range(16):
            for y in range(9):px[x,y]=solid(cols[x])
            px[x,9]=mix(cols[x],'#000000',.3)
            for y in (10,11):px[x,y]=mix(cols[x],'#ffffff',.18)
            px[x,12]=mix(cols[x],'#000000',.4)
    upper('curtain_upper',FOLDS,True);lower('curtain_lower',FOLDS)
    upper('curtain_bunch_upper',PLEATS,False);lower('curtain_bunch_lower',PLEATS)
    # Steel for what is new: a track's bands on the left, and four columns that run true down a rod or a pipe on the right.
    px=small('steel16')
    for y in range(16):
        row='#b4c1ba' if y==0 else '#5d7271' if y in (13,15) else '#98a9a5' if y==5 else '#8a9c9a'
        for x in range(12):px[x,y]=solid('#5d7271' if y==8 and x%4==1 else row)
        for x,c in zip(range(12,16),('#b4c1ba','#93a5a2','#8a9c9a','#6c8180')):px[x,y]=solid(c)
    # The chart's carrier station (0.4.590, owner: "the room's vignette text should be highlighted and given a unique display
    # canister"): a cabinet, a clear chamber with the carrier standing in it, and the pipe it came down.
    px=small('tube_front')
    for x in range(10):
        for y in range(12):px[x,y]=solid('#d9ddd2')
    for x in range(10):px[x,0]=solid('#f0ecdc');px[x,11]=solid('#7c9591')
    for y in range(12):px[0,y]=solid('#7c9591');px[9,y]=solid('#7c9591')
    for x in range(2,8):px[x,2]=solid('#a9b8b0');px[x,7]=solid('#a9b8b0')
    for y in range(2,8):px[2,y]=solid('#a9b8b0');px[7,y]=solid('#a9b8b0')
    px[6,4]=solid('#55605e');px[6,5]=solid('#55605e')
    for x in (3,4):px[x,3]=solid('#8a9893')
    for x in (2,4,6):px[x,9]=solid('#647c7a')
    px=small('tube_side')
    for x in range(10):
        for y in range(12):px[x,y]=solid('#d9ddd2')
    for x in range(10):px[x,0]=solid('#f0ecdc');px[x,11]=solid('#7c9591')
    for y in range(12):px[0,y]=solid('#7c9591');px[9,y]=solid('#7c9591')
    for x,y in ((3,8),(7,3)):px[x,y]=solid('#c3cbc1')
    px=small('tube_glass')
    for y in range(12):px[0,y]=solid('#cfe0da');px[5,y]=solid('#cfe0da')
    for x in range(6):px[x,0]=solid('#cfe0da');px[x,11]=solid('#cfe0da')
    for x,y in ((1,2),(1,3),(2,4),(4,8),(4,9)):px[x,y]=solid('#eef5f0')
    px=small('tube_carrier')
    for x in range(3):
        px[x,0]=solid('#a8553c');px[x,8]=solid('#a8553c')
        for y in range(1,8):px[x,y]=solid('#efe8d2')
    for y in range(1,8):px[2,y]=solid('#d9d0b6')
    for y in (2,4,6):px[0,y]=solid('#8b8a7a');px[1,y]=solid('#8b8a7a')
    px[1,4]=solid('#efe8d2')
    for x in range(4,7):
        for y in range(3):px[x,y]=solid('#a8553c')
    px[5,1]=solid('#7d3c2a')
    px=small('tube_pipe')
    for y in range(16):
        for x,c in enumerate(('#e3e7dc','#d3d9cf','#c9d0c6','#b9c3ba','#9fada6')):px[x,y]=solid('#7c9591' if y in (3,12) else c)
    for name,dome,core in (('call_dome','#cfd6cc','#e6eae0'),('call_dome_on','#f2a35a','#ffe0a8')):
        px=small(name)
        for x in range(4):
            for y in range(2):px[x,y]=solid(dome)
        px[1,0]=solid(core);px[2,0]=solid(core)
        for x in range(4):
            for y in range(4,8):px[x,y]=solid(dome)
        for x in (1,2):
            for y in (5,6):px[x,y]=solid(core)
        for x in range(6):
            for y in range(10,16):px[x,y]=solid('#d9ddd2')
        for x in range(6):px[x,10]=solid('#7c9591');px[x,15]=solid('#7c9591')
        for y in range(10,16):px[0,y]=solid('#7c9591');px[5,y]=solid('#7c9591')
    return out

def cube(material):
    return {'from':[0,0,0],'to':[16,16,16],'faces':{f:{'texture':'#'+material} for f in ('north','south','east','west','up','down')}}

def models():
    result={}
    def put(path,value):result[path]=value
    def model(elements,materials):return {'ambientocclusion':True,'textures':{**{m:PREFIX+m for m in materials},'particle':PREFIX+materials[0]},'elements':elements}
    kinds=['wall','dado','floor','ceiling','curtain','call_panel','window','bed_head','bed_foot','chair','bedside','table','stool','light',
           'counter','sink','column','light_off','bed_unit','monitor_stand','monitor','night_light','trolley','drip',
           'tube_base','tube_station','tube_pipe','call_dome','call_dome_on']
    surfaces={'call_panel':'call','window':'glass'}
    def tall(a,b,faces):
        # Parts reaching above their block (rods, poles, a hung bag) get UVs inside their own sprite; derived ones would run off it.
        if b[1]<=16:return faces
        off=16*int(a[1]//16) if a[1]>=16 else 0;v0=16-min(16,b[1]-off);v1=16-max(0,a[1]-off)
        uv={'north':[16-b[0],v0,16-a[0],v1],'south':[a[0],v0,b[0],v1],'east':[16-b[2],v0,16-a[2],v1],'west':[a[2],v0,b[2],v1],'up':[a[0],a[2],b[0],b[2]],'down':[a[0],16-b[2],b[0],16-a[2]]}
        return {f:{**v,'uv':uv[f]} for f,v in faces.items()}
    def box(a,b,faces):return {'from':list(a),'to':list(b),'faces':tall(a,b,{f:{'texture':'#'+t} for f,t in faces.items()})}
    def every(t,**over):return {f:over.get(f,t) for f in ('north','south','east','west','up','down')}
    built={
        'column':[box((0,0,0),(16,16,16),every('column',up='enamel',down='enamel'))],
        'light_off':[box((0,0,0),(16,16,16),every('light_off'))],
        'bed_unit':[box((0,3,13),(16,13,16),every('enamel',north='bed_unit')),box((5,9,9),(11,12,13),every('steel',down='light'))],
        'monitor_stand':[box((2,0,7),(14,1.5,9),every('steel')),box((7,0,2),(9,1.5,14),every('steel')),
            *[box((x,0,z),(x+1.5,1.25,z+1.5),every('rubber')) for x,z in ((1.5,7.25),(13,7.25),(7.25,1.5),(7.25,13))],
            box((7.5,1.5,7.5),(8.5,16,8.5),every('steel'))],
        'monitor':[box((7.5,0,7.5),(8.5,6,8.5),every('steel')),box((7,5,7),(9,7,9),every('steel')),box((3,6,6),(13,14,10),every('enamel',north='monitor'))],
        'night_light':[box((5,2,14),(11,5,16),every('enamel',north='night_light'))],
        'trolley':[box((2,3,3),(14,13,13),every('trolley',up='worktop')),box((1,13,2),(15,14,14),every('steel')),box((2,14,12.5),(14,15,13.5),every('steel')),
            *[box((x,0,z),(x+1.5,3,z+1.5),every('rubber')) for x,z in ((2.5,3.5),(12,3.5),(2.5,11),(12,11))]],
        'drip':[box((3,0,7.5),(13,1,8.5),every('steel')),box((7.5,0,3),(8.5,1,13),every('steel')),box((7.75,1,7.75),(8.25,30,8.25),every('steel')),
            box((5,28,7.75),(11,28.5,8.25),every('steel')),box((4.75,20,7.25),(7.25,26,8.75),every('bag'))],
    }
    # 0.4.590: parts drawn at sixteen pixels, every face given its own texels. A face is (texture, [u0, v0, u1, v1]) or, lit from
    # within, (texture, uv, light).
    def part(a,b,**faces):
        out={}
        for name,spec in faces.items():
            out[name]={'texture':'#'+spec[0],'uv':list(spec[1])}
            if len(spec)>2:out[name]['neoforge_data']={'block_light':spec[2],'sky_light':spec[2]}
        e={'from':list(a),'to':list(b),'faces':out}
        if any(len(spec)>2 for spec in faces.values()):e['shade']=False
        return e
    def round_(t,uv,**more):return {f:(t,uv)+tuple(more.get('light',())) for f in ('north','south','east','west')}
    for lit in (False,True):
        t='call_dome_on' if lit else 'call_dome';glow=(15,) if lit else ()
        built[t]=[part((5,15,5),(11,16,11),**round_(t,[0,10,6,11]),down=(t,[0,10,6,16])),
            part((6,13,6),(10,15,10),**round_(t,[0,0,4,2],light=glow),down=(t,[0,4,4,8])+glow)]
    built['tube_base']=[part((4,0,4),(12,1,12),**round_('steel16',[0,9,8,10])),
        part((3,1,3),(13,13,13),north=('tube_front',[0,0,10,12]),south=('tube_side',[0,0,10,12]),east=('tube_side',[0,0,10,12]),west=('tube_side',[0,0,10,12])),
        part((2,13,2),(14,14,14),**round_('steel16',[0,5,12,6]),up=('steel16',[0,1,12,13]),down=('steel16',[0,1,12,13])),
        part((5,14,5),(11,16,11),**round_('steel16',[0,1,6,3]))]
    built['tube_station']=[part((4,0,4),(12,2,12),**round_('steel16',[0,1,8,3]),up=('steel16',[0,1,8,9])),
        part((5,2,5),(11,14,11),**round_('tube_glass',[0,0,6,12])),
        part((6.5,3,6.5),(9.5,12,9.5),**round_('tube_carrier',[0,0,3,9],light=(12,)),up=('tube_carrier',[4,0,7,3],12)),
        part((4,14,4),(12,16,12),**round_('steel16',[0,1,8,3]),up=('steel16',[0,1,8,9]),down=('steel16',[0,1,8,9])),
        part((7,14.5,3.5),(9,15.5,4),north=('call_dome_on',[1,5,3,6],15),east=('call_dome_on',[1,5,1.5,6],15),west=('call_dome_on',[1,5,1.5,6],15),
            up=('call_dome_on',[1,5,3,5.5],15),down=('call_dome_on',[1,5,3,5.5],15))]
    built['tube_pipe']=[part((5.5,0,5.5),(10.5,16,10.5),**round_('tube_pipe',[0,0,5,16])),
        part((4,15,4),(12,16,12),**round_('steel16',[0,1,8,2]),down=('steel16',[0,1,8,9]))]
    for k in kinds:
        if k in built:
            elements=built[k];materials=sorted({f['texture'][1:] for e in elements for f in e['faces'].values()})
            obj=model(elements,materials)
            if k in ('monitor_stand','drip','monitor') or k.startswith(('tube_','call_dome')):obj['ambientocclusion']=False
            if k=='tube_station':obj['render_type']='minecraft:cutout'
            put(f'models/block/night_ward_{k}.json',obj);continue
        if k in ('counter','sink'):
            old=json.loads((A/f'models/block/ward_{k}.json').read_text());elements=copy.deepcopy(old['elements'])
            mapping={'cabinet':'counter','laminate':'worktop','steel':'steel','enamel':'enamel','ceramic':'ceramic','glass':'ceramic'}
            for part in elements:
                for face in part['faces'].values():face['texture']='#'+mapping[face['texture'][1:]]
            materials=sorted({f['texture'][1:] for e in elements for f in e['faces'].values()})
            put(f'models/block/night_ward_{k}.json',model(elements,materials));continue
        if k in ('bed_head','bed_foot','chair','bedside','table','stool'):
            old=json.loads((A/f'models/block/ward_{k}.json').read_text());elements=copy.deepcopy(old['elements'])
            mapping={'enamel':'enamel','ivory':'sheet','vinyl':'blanket','steel':'steel','cabinet':'enamel','laminate':'enamel'}
            for part in elements:
                for face in part['faces'].values():face['texture']='#'+mapping[face['texture'][1:]]
            if k=='bedside':elements[0]['to'][1]=16
            if k.startswith('bed_'):
                for face in elements[1]['faces'].values():face['texture']='#blanket'
            materials=sorted({f['texture'][1:] for e in elements for f in e['faces'].values()})
            obj=model(elements,materials)
        else:
            mat=surfaces.get(k,k);obj=model([cube(mat)],[mat])
            if k=='call_panel':
                obj=model([cube('enamel')],['enamel','call']);obj['elements'][0]['faces']['north']['texture']='#call'
            if k=='window':obj['render_type']='minecraft:cutout'
        put(f'models/block/night_ward_{k}.json',obj)
    variants={}
    for k in kinds:
        for facing,y in [('north',0),('east',90),('south',180),('west',270)]:
            variants[f'facing={facing},kind={k}']={'model':f'the_oldest_house:block/night_ward_{k}',**({'y':y} if y else {})}
    put('blockstates/night_ward.json',{'variants':variants})
    for group,state in [('glass','institute_glass'),('door','institute_door')]:
        for src in (A/'models/block').glob(f'ward_{group}_*.json'):
            obj=json.loads(src.read_text())
            if group=='glass':obj['textures']={k:PREFIX+('glass' if k=='pane' else 'enamel') for k in obj['textures']};obj['render_type']='minecraft:cutout'
            else:obj['textures']={'bottom':PREFIX+'door_lower','top':PREFIX+'door_upper'}
            put('models/block/'+src.name.replace('ward_','night_ward_',1),obj)
        obj=json.loads((A/f'blockstates/{state}.json').read_text())
        obj=json.loads(json.dumps(obj).replace('the_oldest_house:block/ward_', 'the_oldest_house:block/night_ward_'))
        put(f'blockstates/night_ward_{group}.json',obj)
    incubator=json.loads((A/'models/block/novel_incubator.json').read_text())
    incubator['elements']=incubator['elements'][:8]
    incubator['textures']={'frame':PREFIX+'enamel','glass':PREFIX+'glass','fabric':PREFIX+'sheet','controls':PREFIX+'controls','particle':PREFIX+'enamel'}
    incubator['elements'][0]['faces']['south']={'texture':'#frame'}
    incubator['elements'][0]['faces']['north']={'texture':'#controls','uv':[0,0,14,2]}
    # A clear case needs a readable frame, even where the thin glass UVs sample its transparent centre.
    # These rails stay inside the incubator's existing 1..15 collision footprint and 15-pixel top.
    rails=[[x,5,z,x+.5,14.75,z+.5] for x in (1.25,14.25) for z in (1.25,14.25)]
    rails += [[1.25,14.25,z,14.75,14.75,z+.5] for z in (1.25,14.25)]
    rails += [[x,14.25,1.25,x+.5,14.75,14.75] for x in (1.25,14.25)]
    for bounds in rails:
        part=cube('frame');part['from']=bounds[:3];part['to']=bounds[3:];incubator['elements'].append(part)
    put('models/block/novel_incubator.json',incubator)
    bezel={'from':[5,0,5.5],'to':[11,1,10.5],'faces':{'down':{'uv':[0,0,7,6],'texture':'#call_button','cullface':'down'},'up':{'uv':[0,0,7,6],'texture':'#call_button'},
        **{f:{'uv':[0,6,7,7],'texture':'#call_button'} for f in ('north','south','east','west')}}}
    for name,height in (('night_ward_call_button',2.25),('night_ward_call_button_pressed',1.4)):
        cap={'from':[6.5,1,6.5],'to':[9.5,height,9.5],'faces':{'up':{'uv':[9,1,12,4],'texture':'#call_button'},**{f:{'uv':[9,5,12,6],'texture':'#call_button'} for f in ('north','south','east','west')}}}
        put(f'models/block/{name}.json',{'textures':{'call_button':PREFIX+'call_button','particle':PREFIX+'call_button'},'elements':[copy.deepcopy(bezel),cap]})
    variants={}
    for face,x in (('floor',0),('wall',90),('ceiling',180)):
        for facing,y in (('north',0),('east',90),('south',180),('west',270)):
            if face=='ceiling':y=(y+180)%360
            for powered in ('false','true'):
                v={'model':'the_oldest_house:block/night_ward_call_button'+('_pressed' if powered=='true' else '')}
                if x:v['x']=x
                if y:v['y']=y
                variants[f'face={face},facing={facing},powered={powered}']=v
    put('blockstates/night_ward_call_button.json',{'variants':variants})
    # The bed curtains (0.4.590). Cloth two blocks high on a ceiling track, joined like panes. Each half of a run is three strips
    # of cloth a pixel thick, standing in two planes a pixel apart, so the folds are real and a fold's two sides take the light
    # and the dark of the sprite; the strips in the back plane hang a pixel shorter, so the hem is not a ruled line. The top of
    # the upper half is open mesh. Where a curtain is pulled back its cloth hangs bunched at the end of the run, and the track
    # runs on alone. A drop rod holds the track wherever it turns, ends or meets another, and nowhere else.
    def quad(t,uv,cull=None):
        f={'texture':'#'+t,'uv':list(uv)}
        if cull:f['cullface']=cull
        return f
    STRIPS=((0,3,'a'),(3,4,'b'),(4,7,'a'))
    def strips(t,top,bottoms):
        out=[]
        for z0,z1,plane in STRIPS:
            x0,x1=(7,8) if plane=='a' else (8,9);y0=bottoms[plane];v0,v1=16-top,16-y0
            near,far=[z0,v0,z1,v1],[8+z0,v0,8+z1,v1]
            out.append({'from':[x0,y0,z0],'to':[x1,top,z1],'faces':{
                'east':quad(t,near if plane=='b' else far),'west':quad(t,near if plane=='a' else far),
                'north':quad(t,[8,v0,9,v1],'north' if z0==0 else None),'south':quad(t,[8,v0,9,v1])}})
        return out
    def middle(t,top,bottom):
        v0,v1=16-top,16-bottom
        return {'from':[7,bottom,7],'to':[9,top,9],'faces':{f:quad(t,[6,v0,8,v1] if f in ('east','west') else [9,v0,11,v1]) for f in ('north','south','east','west')}}
    def bunch(t,top,bottoms):
        out=[]
        for z in range(5):
            wide=z%2==0;x0,x1=(6,10) if wide else (7,9);y0=bottoms['a' if wide else 'b'];v0,v1=16-top,16-y0
            column=(0,3,4,7,6)[z];faces={'east':quad(t,[column,v0,column+1,v1]),'west':quad(t,[column,v0,column+1,v1])}
            if wide:faces['north']=quad(t,[8,v0,12,v1]);faces['south']=quad(t,[8,v0,12,v1])
            out.append({'from':[x0,y0,z],'to':[x1,top,z+1],'faces':faces})
        return out
    track_side={'from':[7,15,0],'to':[9,16,7],'faces':{'east':quad('steel16',[0,8,7,9]),'west':quad('steel16',[0,8,7,9]),'north':quad('steel16',[0,8,2,9],'north'),
        'up':quad('steel16',[0,1,2,8]),'down':quad('steel16',[2,9,4,16])}}
    track_post={'from':[7,15,7],'to':[9,16,9],'faces':{**{f:quad('steel16',[0,8,2,9]) for f in ('north','south','east','west')},'up':quad('steel16',[0,1,2,3]),'down':quad('steel16',[2,9,4,11])}}
    rod={'from':[7.5,16,7.5],'to':[8.5,32,8.5],'faces':{f:quad('steel16',[13,0,14,16]) for f in ('north','south','east','west')}}
    whole,short={'a':3,'b':4},{'a':0,'b':0}
    curtain_models={
        'lower_side':strips('curtain_lower',16,whole),'lower_post':[middle('curtain_lower',16,3)],
        'upper_side':strips('curtain_upper',15,short),'upper_post':[middle('curtain_upper',15,0)],
        'rail_side':[track_side],'rail_post':[track_post],'rod':[rod],
        'bunch_lower':bunch('curtain_bunch_lower',16,whole),'bunch_upper':bunch('curtain_bunch_upper',15,short),
    }
    for name,elements in curtain_models.items():
        materials=sorted({f['texture'][1:] for e in elements for f in e['faces'].values()})
        obj={'ambientocclusion':False,'textures':{**{m:PREFIX+m for m in materials},'particle':PREFIX+'curtain_lower'},'elements':elements}
        if name in ('upper_side','upper_post'):obj['render_type']='minecraft:cutout'
        put(f'models/block/night_ward_curtain_{name}.json',obj)
    turns=(('north',0),('east',90),('south',180),('west',270))
    def apply(name,y=0):
        out={'model':f'the_oldest_house:block/night_ward_curtain_{name}'}
        if y:out['y']=y
        return out
    multipart=[]
    for half in ('lower','upper'):
        multipart.append({'when':{'part':half,'gather':'none'},'apply':apply(half+'_post')})
        for direction,y in turns:multipart.append({'when':{'part':half,'gather':'none',direction:'true'},'apply':apply(half+'_side',y)})
        # The bunch hangs at the low end of the run (neg) or the high end (pos). The run lies north and south where the curtain
        # joins either way or joins nothing, east and west where it joins either of those: so every state draws its cloth,
        # a curtain pulled back to the end of its track included.
        for gather,north_south,east_west in (('neg',0,270),('pos',180,90)):
            multipart.append({'when':{'AND':[{'part':half,'gather':gather},{'OR':[{'north':'true'},{'south':'true'},{'east':'false','west':'false'}]}]},'apply':apply('bunch_'+half,north_south)})
            multipart.append({'when':{'AND':[{'part':half,'gather':gather},{'OR':[{'east':'true'},{'west':'true'}]}]},'apply':apply('bunch_'+half,east_west)})
    multipart.append({'when':{'part':'upper|rail'},'apply':apply('rail_post')})
    for direction,y in turns:multipart.append({'when':{'part':'upper|rail',direction:'true'},'apply':apply('rail_side',y)})
    bends=[]
    for bits in range(16):
        joined={d:bool(bits>>i&1) for i,(d,_) in enumerate(turns)}
        if joined in ({'north':True,'east':False,'south':True,'west':False},{'north':False,'east':True,'south':False,'west':True}):continue
        bends.append({'part':'upper|rail',**{d:'true' if on else 'false' for d,on in joined.items()}})
    multipart.append({'when':{'OR':bends},'apply':apply('rod')})
    put('blockstates/night_ward_curtain.json',{'multipart':multipart})
    return result

def main():
    p=argparse.ArgumentParser();p.add_argument('--check',action='store_true');check=p.parse_args().check
    for name,im in paintings().items():
        path=A/f'textures/block/night_ward/{name}.png'
        if check:
            with Image.open(path) as actual:assert actual.convert('RGBA').tobytes()==im.tobytes(),name
        else:path.parent.mkdir(parents=True,exist_ok=True);im.save(path,optimize=True)
    for name,obj in models().items():
        path=A/name
        if check:assert json.loads(path.read_text())==obj,name
        else:path.write_text(json.dumps(obj,indent=2)+'\n')
    print(('Verified' if check else 'Painted')+' the night-duty ward materials, its fittings, curtains and their native models.')

if __name__=='__main__':main()
