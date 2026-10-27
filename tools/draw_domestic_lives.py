"""Hand-authored sixteen-pixel household materials, physical states and matching collision.

The ordinary fragments keep their original blocks. These assets belong only to
four deeper rooms and a few inert disturbances. No random noise or photo filters.
"""
from pathlib import Path
import argparse,json,copy
from PIL import Image,ImageDraw

ROOT=Path(__file__).resolve().parents[1]
A=ROOT/'src/main/resources/assets/the_oldest_house'
JAVA=ROOT/'src/main/java/io/github/knaitoe/theoldesthouse/labyrinth/DomesticFittingShapes.java'
KINDS=['breakfast','record_player','bottles','radio','toppled_chair','toppled_table','scrapes','packed_clothes','book_box','closet_lower','closet_upper','desk_remnants','empty_bottles','dirty_dishes','photo_family','photo_couple','bottle_returns','folded_shirt']
MATERIALS=['family_paper','student_paper','smoked_paper','rose_paper','parquet','linoleum','parquet_worn']

def paintings():
    out={}
    def sheet(name,color):
        im=Image.new('RGBA',(16,16),color);out[name]=im;return ImageDraw.Draw(im)
    d=sheet('family_paper','#bcb29a');d.line((15,0,15,15),fill='#b3aa94')
    for x in (3,11):d.line((x,2,x,11),fill='#c8c0aa');d.point((x+1,7),fill='#aaa48b')
    d=sheet('student_paper','#858c78');d.line((15,0,15,15),fill='#7d8572');d.line((4,3,4,12),fill='#919780');d.rectangle((10,5,12,8),fill='#aaa48c');d.line((10,8,12,8),fill='#767660')
    d=sheet('smoked_paper','#95836a');d.line((0,14,15,14),fill='#776d58')
    for x,y in ((2,2),(3,4),(11,2),(12,3),(7,9)):d.line((x,y,x,y+3),fill='#88795f')
    d=sheet('rose_paper','#b9a696')
    for x,y in ((3,3),(11,11)):
        d.line((x,y+1,x,y+5),fill='#7e886b');d.line((x,y+3,x+2,y+2),fill='#8d9778');d.rectangle((x-1,y-1,x+1,y+1),fill='#92746c');d.point((x,y),fill='#b99183')
    d=sheet('parquet','#79634b')
    for y in (0,4,8,12):
        d.line((0,y,15,y),fill='#4e4939');d.line((0,y+1,15,y+1),fill='#8b7659')
        x=3 if y%8==0 else 11;d.line((x,y+1,x,y+3),fill='#5f503e');d.line((x+2,y+2,min(15,x+5),y+2),fill='#857052')
    out['parquet_worn']=out['parquet'].copy();d=ImageDraw.Draw(out['parquet_worn']);d.rectangle((4,4,11,12),fill='#786b54');d.line((5,5,10,5),fill='#93836a');d.line((6,10,12,10),fill='#6c604b')
    d=sheet('linoleum','#b7ad83')
    for x in (0,8):d.line((x,0,x,15),fill='#8f946f')
    for y in (0,8):d.line((0,y,15,y),fill='#8f946f')
    for x,y in ((2,2),(10,10)):d.rectangle((x,y,x+3,y+3),outline='#c8bd91')
    d=sheet('wood','#65513e');d.line((0,1,15,1),fill='#8a7054');d.line((0,14,15,14),fill='#413c32');d.rectangle((2,3,13,12),outline='#514635');d.line((4,5,11,5),fill='#79634a')
    d=sheet('chair_cloth','#677665');d.rectangle((1,1,14,14),outline='#455444');d.line((3,3,12,3),fill='#87937a');d.line((2,12,13,12),fill='#536350')
    d=sheet('ceramic','#c8c1a6');d.line((0,1,15,1),fill='#e0d4b8');d.line((0,13,15,13),fill='#a49c85')
    for name,color,light in [('cup_blue','#68878b','#9aabaa'),('cup_red','#a87559','#c39a76'),('cup_green','#7c8865','#a1ad86')]:
        d=sheet(name,color);d.line((0,1,15,1),fill=light);d.line((0,12,15,12),fill='#cac3a6');d.rectangle((4,5,5,7),fill='#cec3a0');d.rectangle((10,5,11,7),fill='#cec3a0')
    d=sheet('milk','#d8d0b2');d.rectangle((1,4,14,11),fill='#647d85');d.line((4,6,11,6),fill='#c8c7aa');d.line((4,9,9,9),fill='#b7bda0');d.line((0,14,15,14),fill='#a39f89')
    d=sheet('liquid','#bcaa80');d.line((2,3,12,3),fill='#d4c39c');d.line((3,12,13,12),fill='#a18b65')
    d=sheet('dark_metal','#333b38');d.line((0,0,15,0),fill='#646d60');d.line((0,15,15,15),fill='#1e2726')
    d=sheet('record','#232b2b')
    for k,c in ((1,'#3c4440'),(3,'#303a36'),(5,'#50544a')):d.rectangle((k,k,15-k,15-k),outline=c)
    d.rectangle((6,6,9,9),fill='#a79266');d.point((7,7),fill='#ece1bc')
    d=sheet('record_b','#232b2b')
    for k,c in ((1,'#3c4440'),(3,'#303a36'),(5,'#50544a')):d.rectangle((k,k,15-k,15-k),outline=c)
    d.rectangle((6,6,9,9),fill='#687f84');d.point((8,8),fill='#d3d0b2')
    d=sheet('speaker','#5c5443')
    for y in range(1,15,3):d.line((1,y,14,y),fill='#302f29');d.line((1,y+1,14,y+1),fill='#82745a')
    d=sheet('dial','#a5a17b');d.rectangle((1,2,14,13),fill='#38463f')
    for x in (3,5,7,9,11,13):d.line((x,4,x,6 if x%4 else 8),fill='#b4b390')
    d.line((2,11,13,11),fill='#7e9076')
    d=sheet('glass','#586c50');d.line((2,0,2,15),fill='#7a8a63');d.line((13,0,13,15),fill='#354b39');d.rectangle((3,6,12,11),fill='#bbb292');d.line((5,8,10,8),fill='#7b7662')
    d=sheet('spill',(0,0,0,0));d.polygon([(2,5),(6,3),(12,4),(14,7),(12,11),(6,13),(2,10)],fill='#433b2c');d.line((5,5,10,5),fill='#9d8351');d.point((1,12),fill='#7c6241')
    d=sheet('scrapes',(0,0,0,0))
    for x,y,n in ((2,3,10),(1,6,12),(4,9,9),(7,12,6)):
        d.line((x,y,x+n,y+1),fill='#4b4436');d.line((x+2,y+1,x+n,y+2),fill='#b6a078')
    d=sheet('cardboard','#99794e');d.line((0,1,15,1),fill='#b29465');d.line((1,14,14,14),fill='#755b3d');d.line((1,2,1,13),fill='#aa895b')
    d.rectangle((5,5,10,10),fill='#c6b68b');d.line((6,7,9,7),fill='#897b5d');d.line((6,9,8,9),fill='#897b5d')
    d=sheet('tape','#b4a274');d.line((6,0,6,15),fill='#d0bc89');d.line((9,0,9,15),fill='#8d805e')
    for name,c,edge in [('folded_blue','#687e88','#8d9ca0'),('folded_cream','#b3aa90','#c9c1a6'),('folded_red','#8d6460','#af8176')]:
        d=sheet(name,c);d.line((0,13,15,13),fill=edge);d.line((1,2,14,2),fill=edge);d.line((7,4,8,4),fill=edge);d.line((8,5,8,11),fill=edge)
    d=sheet('book_pages','#bcb69c');d.line((0,2,15,2),fill='#d0c8ab');d.line((1,5,14,5),fill='#8e8b76');d.line((2,9,13,9),fill='#a29d84');d.line((0,13,15,13),fill='#99937a')
    d=sheet('book_spines','#766e56');d.rectangle((0,0,4,15),fill='#6d7b71');d.rectangle((6,0,10,15),fill='#8f6a5c');d.rectangle((12,0,15,15),fill='#a39870')
    for x in (1,7,13):d.line((x,3,x+2,3),fill='#c6b990');d.line((x,12,x+2,12),fill='#b0a17d')
    d=sheet('screen','#2e3638');d.rectangle((1,1,14,14),outline='#525e5c');d.line((3,4,9,4),fill='#657674');d.line((3,7,12,7),fill='#485b5a')
    d=sheet('keyboard','#656963')
    for y in (3,6,9):
        for x in range(2,15,3):d.rectangle((x,y,x+1,y+1),fill='#2f3937')
    d=sheet('dirty_ceramic','#bab39a');d.rectangle((3,3,12,12),outline='#8c7d5d');d.line((5,11,10,11),fill='#69583d');d.point((11,5),fill='#807052')
    d=sheet('beer_label','#c6b591');d.rectangle((0,5,15,11),fill='#856448');d.line((2,6,13,6),fill='#e0cc9f');d.line((4,9,11,9),fill='#534d39')
    d=sheet('empty_glass','#526747');d.line((2,0,2,15),fill='#88936a');d.line((12,0,12,15),fill='#324c38');d.line((3,14,11,14),fill='#a3a47e')
    d=sheet('dust','#8e8068');d.line((1,13,14,13),fill='#b0a38a');d.rectangle((3,3,12,11),outline='#9e9279')
    for name,count in [('photo_family',5),('photo_couple',2)]:
        d=sheet(name,'#b9ad90');d.rectangle((1,1,14,14),fill='#7c8270');d.rectangle((1,11,14,14),fill='#626c56')
        for i in range(count):
            x=2+i*3 if count==5 else 4+i*6;y=5 if count==5 and i>1 else 3
            d.rectangle((x,y,x+1,y+1),fill='#c1a58a');d.rectangle((x-1,y+2,x+2,12),fill=['#5c6d71','#8b7568','#8c684e','#74858b','#8c8e6d'][i])
        d.rectangle((0,0,15,15),outline='#d1c2a1')
    return out

def box(a,b,mat):return {'from':list(a),'to':list(b),'faces':{f:{'texture':'#'+mat} for f in ['north','south','east','west','up','down']}}
def parts(kind,stage):
    p=[]
    def add(a,b,m):p.append(box(a,b,m))
    if kind=='breakfast':
        add((1,0,2),(15,1,14),'wood')
        for i,mat in enumerate(['cup_blue','cup_red','cup_green']):
            x=2+i*4
            add((x,1,6),(x+3,5,9),mat);add((x+1,2,9),(x+2,4,9.6),mat) if stage&(1<<i) else add((x+3,2,7),(x+3.6,4,8),mat)
            add((x+.4,4.9,6.4),(x+2.6,5,8.6),'dark_metal')
        add((10,1,10),(14,6,13),'milk');add((11,6,10.5),(13,7,12.5),'milk')
    elif kind=='record_player':
        add((1,0,2),(15,11,15),'wood');add((2,11,3),(14,12,14),'dark_metal')
        for a,b in [((4,12,5),(10,12.5,12)),((3,12,6),(11,12.5,11)),((5,12,4),(9,12.5,13))]:add(a,b,'record_b' if stage&4 else 'record')
        add((6.5,12.5,7.5),(7.5,13,8.5),'dark_metal')
        # A lid tipped upright stays inside the cabinet's native cell.
        add((1,12 if stage&1 else 13,2),(15,16 if stage&1 else 14,3 if stage&1 else 15),'wood')
        add((12,12,5),(13,13,12),'dark_metal')
        add((8 if stage&2 else 12,12.5,10),(13,13,11),'ceramic')
        add((2,3,14.9),(14,9,15),'speaker');add((12,9,15),(14,10,15.5),'dark_metal')
    elif kind=='bottles':
        add((0,0,1),(16,.5,15),'wood')
        if stage&8:add((1,.51,2),(15,.55,14),'spill')
        for i in range(3):
            x=1+i*5
            if stage&(1<<i):add((x,1,4),(x+3,3,11),'glass');add((x+.8,1.5,11),(x+2.2,2.5,14),'glass')
            else:add((x,1,5),(x+3,8,8),'glass');add((x+.8,8,5.8),(x+2.2,11,7.2),'glass');add((x+.8,10,5.8),(x+2.2,11,7.2),'dark_metal')
    elif kind=='radio':
        add((1,0,3),(15,8,13),'wood');add((2,1,12.9),(10,6,13.05),'speaker');add((2,6,12.9),(14,7.5,13.1),'dial')
        add((11,2,13),(13,4,14),'dark_metal');add((4,2,13),(6,4,14),'dark_metal')
        x=3+(stage&3)*3;add((x,6,13.1),(x+.4,7.5,13.2),'ceramic')
        if stage&4:add((13,6.1,13.1),(14,7.1,13.2),'liquid')
    elif kind in ('toppled_chair','toppled_table'):
        return tipped(kind,stage)['elements']
    elif kind=='scrapes':add((0,.01,0),(16,.02,16),'scrapes')
    elif kind in ('packed_clothes','book_box','bottle_returns'):
        add((0,0,0),(16,1,16),'cardboard');add((0,1,0),(1,10,16),'cardboard');add((15,1,0),(16,10,16),'cardboard');add((1,1,0),(15,10,1),'cardboard');add((1,1,15),(15,10,16),'cardboard')
        if stage&1:
            add((0,10,0),(1,15,16),'cardboard');add((15,10,0),(16,15,16),'cardboard')
            if kind=='packed_clothes':
                add((2,1,2),(8,4,9),'folded_blue');add((2,4,3),(9,7,10),'folded_cream');add((9,1,5),(14,5,14),'folded_red');add((4,7,5),(12,9,13),'folded_blue')
            elif kind=='book_box':
                add((2,1,2),(8,7,13),'book_pages');add((2,1,2),(8,7,3),'book_spines');add((9,1,2),(14,9,13),'book_spines');add((3,7,5),(11,9,13),'book_pages')
            else:
                for x in (2,6,10):
                    for z in (2,9):add((x,1,z),(x+3,7,z+3),'empty_glass');add((x+.8,7,z+.8),(x+2.2,10,z+2.2),'empty_glass');add((x,3,z),(x+3,5,z+.1),'beer_label');add((x+.9,9.9,z+.9),(x+2.1,10,z+2.1),'dark_metal')
        else:add((0,10,0),(16,11,16),'cardboard');add((6,11,0),(10,11.2,16),'tape')
    elif kind in ('closet_lower','closet_upper'):
        add((0,0,0),(1,16,16),'wood');add((15,0,0),(16,16,16),'wood');add((1,0,0),(15,16,1),'wood')
        if kind=='closet_lower':add((1,0,1),(15,1,15),'dust')
        else:
            add((1,15,1),(15,16,15),'wood');add((2,10,6),(14,10.7,7),'dark_metal')
            for x in (4,8,12):
                add((x,8,6.2),(x+.5,10,6.7),'dark_metal');add((x-1.5,6,6),(x+2,6.5,6.6),'dark_metal');add((x-1,6.5,6),(x-.5,7.5,6.6),'dark_metal');add((x+1,6.5,6),(x+1.5,7.5,6.6),'dark_metal')
        if stage&1:add((1,0,1),(2,16,15),'wood');add((2,7,12),(2.5,9,14),'dark_metal')
        else:add((1,0,14),(15,16,15),'wood');add((12,7,15),(13,9,15.5),'dark_metal')
    elif kind=='desk_remnants':
        add((1,0,2),(10,1,10),'dark_metal');add((2,1,3),(9,1.15,9),'keyboard')
        if stage&1:add((1,1,2),(10,9,3),'dark_metal');add((2,2,3),(9,8,3.1),'screen')
        else:add((1,1.2,2),(10,2,10),'dark_metal')
        add((10,0,8),(15,3,14),'book_pages');add((10,0,13),(15,3,14),'book_spines');add((11,3,7),(15,5,13),'book_spines');add((2,0,12),(5,4,15),'dirty_ceramic')
    elif kind=='empty_bottles':
        # Empty mouths, paper labels, a few upright returns and several rolled under the furniture.
        for i,(x,z) in enumerate([(1,2),(6,3),(11,1),(2,9),(8,9),(12,9)]):
            if i<2:
                add((x,0,z),(x+3,6,z+3),'empty_glass');add((x+.7,6,z+.7),(x+2.3,9,z+2.3),'empty_glass');add((x+.9,8.9,z+.9),(x+2.1,9,z+2.1),'dark_metal');add((x-.05,2,z-.05),(x+3.05,4,z+.05),'beer_label')
            else:
                shift=((stage+i)%3)*.3
                add((x,0,z),(x+3,3,z+4+shift),'empty_glass');add((x+.7,.7,z+4+shift),(x+2.3,2.3,z+6+shift),'empty_glass');add((x-.05,1,z+1),(x+3.05,2,z+3),'beer_label')
    elif kind=='dirty_dishes':
        for y,r in [(0,1),(1,2),(2,1)]:add((r,y,2),(13-r,y+.8,14),'dirty_ceramic')
        add((11,0,3),(15,5,7),'dirty_ceramic');add((10,1,4),(11,4,6),'ceramic')
    elif kind in ('photo_family','photo_couple'):
        if stage&1:
            add((3,0,7),(13,9,8),'wood');add((4,1,8),(12,8,8.1),kind)
            for face in p[-1]['faces'].values():face['uv']=[0,0,16,16]
            add((7,0,2),(8,5,7),'wood')
        else:add((3,0,3),(13,1,12),'wood');add((6,1,5),(10,1.1,10),'cardboard')
    elif kind=='folded_shirt':
        add((3,0,3),(13,2,13),'folded_blue');add((4,2,4),(12,3,12),'folded_blue');add((6,3,4),(10,3.6,6),'folded_cream')
    return p

CHAIRS=['cane_chair','kitchen_stool','green_armchair','floral_armchair','blue_sofa','footstool']
def tipped(kind,stage):
    source='formica_table' if kind=='toppled_table' else CHAIRS[stage%len(CHAIRS)]
    model=copy.deepcopy(json.loads((A/f'models/block/furniture_{source}.json').read_text()))
    # Rigid 90-degree fall around X; retain every original material and native face UV.
    # Normal mapping: (x,y,z) -> (x,16-z,y). Set the actual low edge on the floor.
    low=min(16-e['to'][2] for e in model['elements'])
    normals={'north':'up','south':'down','up':'south','down':'north','east':'east','west':'west'}
    for e in model['elements']:
        a,b=e['from'],e['to'];e['from']=[a[0],16-b[2]-low,a[1]];e['to']=[b[0],16-a[2]-low,b[1]]
        faces={}
        for side,face in e['faces'].items():
            face.pop('cullface',None)
            if side in ('east','west'):face['rotation']=(face.get('rotation',0)+(90 if side=='east' else 270))%360
            faces[normals[side]]=face
        e['faces']=faces
    return model

def effective(kind,stage):
    if kind=='toppled_chair':return stage%6
    if kind=='bottles':return stage
    if kind in KINDS[:4]:return stage&7
    if kind in ('packed_clothes','book_box','closet_lower','closet_upper','desk_remnants','photo_family','photo_couple','bottle_returns'):return stage&1
    if kind=='empty_bottles':return stage%3
    return 0
def generated():
    textures={n:'the_oldest_house:block/domestic/'+n for n in paintings()};out={};variants={};shapes={}
    for kind in KINDS:
        states=sorted({effective(kind,n) for n in range(16)})
        for stage in states:
            ps=parts(kind,stage);name=f'domestic/{kind}_{stage}'
            out[A/f'models/block/{name}.json']=tipped(kind,stage) if kind in ('toppled_chair','toppled_table') else {'parent':'minecraft:block/block','ambientocclusion':True,'textures':{**textures,'particle':textures['wood']},'elements':ps}
            shapes[kind,stage]=[] if kind=='scrapes' else [b['from']+b['to'] for b in ps]
        for stage in range(16):
            for facing,y in [('north',0),('east',90),('south',180),('west',270)]:
                variants[f'kind={kind},facing={facing},stage={stage}']={'model':f'the_oldest_house:block/domestic/{kind}_{effective(kind,stage)}','y':y}
    out[A/'blockstates/domestic_fitting.json']={'variants':variants}
    for name in MATERIALS:
        out[A/f'models/block/domestic_{name}.json']={'parent':'minecraft:block/cube_all','textures':{'all':textures[name]}}
        out[A/f'blockstates/domestic_{name}.json']={'variants':{'':{'model':'the_oldest_house:block/domestic_'+name}}}
    java='package io.github.knaitoe.theoldesthouse.labyrinth;\n/** Generated with the baked native cuboids by tools/draw_domestic_lives.py. */\npublic final class DomesticFittingShapes {\n private DomesticFittingShapes(){}\n public static double[][] boxes(DomesticFittingBlock.Kind k,int stage){return switch(k){\n'
    for kind in KINDS:
        mask='(stage%6)' if kind=='toppled_chair' else 'stage' if kind=='bottles' else '(stage&7)' if kind in KINDS[:4] else '(stage&1)' if kind in ('packed_clothes','book_box','closet_lower','closet_upper','desk_remnants','photo_family','photo_couple','bottle_returns') else '(stage%3)' if kind=='empty_bottles' else '0'
        java+='  case '+kind.upper()+' -> switch('+mask+'){\n'
        for (k,s),boxes in shapes.items():
            if k==kind:java+='   case '+str(s)+' -> new double[][]{'+','.join('{'+','.join(str(x) for x in b)+'}' for b in boxes)+'};\n'
        java+='   default -> new double[0][];};\n'
    java+=' };}\n}\n';return out,java

def main():
    parser=argparse.ArgumentParser();parser.add_argument('--check',action='store_true');check=parser.parse_args().check
    for name,im in paintings().items():
        p=A/f'textures/block/domestic/{name}.png'
        if check:
            with Image.open(p) as saved:assert saved.convert('RGBA').tobytes()==im.tobytes(),name
        else:p.parent.mkdir(parents=True,exist_ok=True);im.save(p,optimize=True)
    models,java=generated()
    for p,data in models.items():
        if check:assert json.loads(p.read_text())==data,str(p)
        else:p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(data,indent=2)+'\n')
    if check:assert JAVA.read_text()==java,'shared rendered/collision cuboids'
    else:JAVA.write_text(java)
    print(('Verified' if check else 'Painted')+f' {len(paintings())} domestic materials, {len(models)} native assets, point-selected household states and shared collision.')
if __name__=='__main__':main()
