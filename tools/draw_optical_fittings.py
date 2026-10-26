"""Native pixel materials for the actual desk projector and small scene camera."""
from pathlib import Path
import argparse,json
from PIL import Image,ImageDraw
ROOT=Path(__file__).resolve().parents[1]
A=ROOT/'src/main/resources/assets/the_oldest_house'
PREFIX='the_oldest_house:block/optical/'

def paintings():
    out={}
    im=Image.new('RGBA',(16,16),'#777e70');d=ImageDraw.Draw(im)
    d.line((0,0,15,0),fill='#a1a594');d.line((0,1,0,14),fill='#8d9383')
    d.line((15,1,15,15),fill='#50574e');d.line((1,15,15,15),fill='#50574e')
    d.rectangle((2,3,13,12),outline='#666e61');d.line((3,4,11,4),fill='#909783')
    for x,y in ((2,2),(13,2),(2,13),(13,13)):d.point((x,y),fill='#434a43')
    d.line((10,14,12,14),fill='#b4ad91');out['case']=im
    im=Image.new('RGBA',(16,16),'#343a38');d=ImageDraw.Draw(im)
    d.rectangle((1,1,14,14),outline='#616961');d.line((2,2,13,2),fill='#859080')
    for y in (4,7,10):d.line((3,y,12,y),fill='#1c2223');d.line((3,y+1,12,y+1),fill='#4e5951')
    out['metal']=im
    im=Image.new('RGBA',(16,16),'#20282c');d=ImageDraw.Draw(im)
    d.rectangle((1,1,14,14),outline='#697368');d.rectangle((3,3,12,12),outline='#3e5355')
    d.rectangle((5,5,10,10),fill='#111a20');d.line((4,5,7,3),fill='#a2b7aa')
    d.line((9,12,12,9),fill='#526f70');out['lens']=im
    im=Image.new('RGBA',(16,16),'#797e72');d=ImageDraw.Draw(im)
    d.rectangle((1,1,14,14),outline='#464e49');d.rectangle((3,3,12,12),outline='#a1a38d')
    for box in ((6,2,9,5),(2,6,5,9),(10,6,13,9),(6,10,9,13)):d.rectangle(box,fill='#282e2c')
    d.rectangle((6,6,9,9),fill='#484d43');d.point((7,7),fill='#b5b79c');out['reel']=im
    im=Image.new('RGBA',(16,16),'#3e4542');d=ImageDraw.Draw(im)
    d.rectangle((2,2,13,6),fill='#1d2727');d.line((3,3,11,3),fill='#95a696')
    for x,c in ((3,'#b5ad89'),(7,'#7f8d79'),(11,'#826050')):d.rectangle((x,10,x+2,12),fill=c)
    d.line((2,8,12,8),fill='#969c89');out['controls']=im
    return out

def box(a,b,material):
    return {'from':a,'to':b,'faces':{f:{'texture':'#'+material,'uv':[0,0,16,16]} for f in ('north','south','east','west','up','down')}}

def projector_model():
    parts=[box([2,0,3],[14,3,13],'case'),box([3,3,4],[13,11,13],'case'),
        box([5,4,2],[11,10,4],'metal'),box([6,5,1],[10,9,2],'lens'),
        box([13,4,5],[13.15,9,11],'controls'),box([3,4,6],[3.15,9,12],'metal'),
        box([4,1,2.8],[12,2,3],'metal'),box([4,10.8,11],[12,11.2,12],'metal')]
    for x in (3,10):
        for y0,y1,z0,z1 in ((10,11,6,8),(11,12,5,9),(12,14,4,10),(14,15,5,9),(15,16,6,8)):
            parts.append(box([x,y0,z0],[x+2,y1,z1],'reel'))
        parts.append(box([x-.1,12.5,6.5],[x+2.1,13.5,7.5],'metal'))
    return {'parent':'minecraft:block/block','ambientocclusion':True,'textures':{**{n:PREFIX+n for n in paintings()},'particle':PREFIX+'case'},'elements':parts}

def camera_model(elements,textures):
    tex=dict(textures);tex.update(iron=PREFIX+'case',glass=PREFIX+'lens',brass=PREFIX+'controls',particle=PREFIX+'case')
    return {'textures':tex,'elements':elements+[box([11,4,5],[11.15,9,9],'brass')],'ambientocclusion':True}

def main():
    parser=argparse.ArgumentParser();parser.add_argument('--check',action='store_true');check=parser.parse_args().check
    for name,im in paintings().items():
        path=A/f'textures/block/optical/{name}.png'
        if check:
            with Image.open(path) as saved:assert saved.convert('RGBA').tobytes()==im.tobytes(),name
        else:path.parent.mkdir(parents=True,exist_ok=True);im.save(path,optimize=True)
    path=A/'models/block/novel_projector.json';model=projector_model()
    if check:assert json.loads(path.read_text())==model,'The pictured projector is not using its authored native materials'
    else:path.write_text(json.dumps(model,indent=2)+'\n')
    print(('Verified' if check else 'Painted')+' five native optical materials and the actual desk projector.')
if __name__=='__main__':main()
