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
    d=tile('controls','#d9ddd2');d.rectangle((1,0,16,3),fill='#435e5e');d.line((3,2,7,2,8,0,10,3,12,1,14,1),fill='#acd0b3')
    d.rectangle((20,0,23,3),fill='#ad5551');d.rectangle((25,1,27,2),fill='#7b9e83')
    d=tile('door_lower','#c4d2c8');d.rectangle((1,1,30,30),outline='#809a94');d.rectangle((5,5,26,28),outline='#e0e2d2')
    d.rectangle((23,8,26,19),fill='#7f9390');d.line((24,9,24,18),fill='#d9dfd0')
    d=tile('door_upper','#c4d2c8');d.rectangle((1,1,30,30),outline='#809a94');d.rectangle((6,5,25,24),fill='#789a98');d.rectangle((7,6,24,23),fill='#b6cebf')
    for n in (11,17,23):d.line((7,n,24,n),fill='#8caaa0');d.line((n,6,n,23),fill='#8caaa0')
    d.rectangle((10,27,21,29),fill='#a95650')
    return out

def cube(material):
    return {'from':[0,0,0],'to':[16,16,16],'faces':{f:{'texture':'#'+material} for f in ('north','south','east','west','up','down')}}

def models():
    result={}
    def put(path,value):result[path]=value
    def model(elements,materials):return {'ambientocclusion':True,'textures':{**{m:PREFIX+m for m in materials},'particle':PREFIX+materials[0]},'elements':elements}
    kinds=['wall','dado','floor','ceiling','curtain','call_panel','window','bed_head','bed_foot','chair','bedside','table','stool','light']
    surfaces={'call_panel':'call','window':'glass'}
    for k in kinds:
        if k in ('bed_head','bed_foot','chair','bedside','table','stool'):
            old=json.loads((A/f'models/block/ward_{k}.json').read_text());elements=copy.deepcopy(old['elements'])
            mapping={'enamel':'enamel','ivory':'sheet','vinyl':'blanket','steel':'steel','cabinet':'enamel','laminate':'enamel'}
            for part in elements:
                for face in part['faces'].values():face['texture']='#'+mapping[face['texture'][1:]]
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
    incubator['textures']={'frame':PREFIX+'enamel','glass':PREFIX+'glass','fabric':PREFIX+'sheet','controls':PREFIX+'controls','particle':PREFIX+'enamel'}
    incubator['elements'][0]['faces']['south']={'texture':'#frame'}
    incubator['elements'][0]['faces']['north']={'texture':'#controls','uv':[0,0,14,2]}
    put('models/block/novel_incubator.json',incubator)
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
    print(('Verified' if check else 'Painted')+' fifteen night-duty ward materials and their native models.')

if __name__=='__main__':main()
