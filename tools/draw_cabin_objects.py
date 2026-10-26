"""Small, deliberately drawn native-pixel cabin journal and hollow firefly jar.

Only the cabin's spare ledger stage is replaced. Original item IDs and BEs stay.
"""
from pathlib import Path
from PIL import Image, ImageDraw
import argparse, io, json

ROOT=Path(__file__).resolve().parents[1]
A=ROOT/'src/main/resources/assets/the_oldest_house'
PREFIX='the_oldest_house:block/cabin/'

def cuboid(a,b,mat):
    return {'from':a,'to':b,'faces':{f:{'texture':'#'+mat,'uv':[0,0,16,16]} for f in ('north','south','east','west','up','down')}}

def jar_model():
    # Thin glass panels leave a real cavity. Sparse opaque texels on a transparent
    # tile make its silhouette legible without tinting the contents into a cube.
    parts=[cuboid([4,0,4],[12,.5,12],'glass')]
    for a,b in [([4,.5,4],[4.25,10,12]),([11.75,.5,4],[12,10,12]),([4.25,.5,4],[11.75,10,4.25]),([4.25,.5,11.75],[11.75,10,12])]:parts.append(cuboid(a,b,'glass'))
    parts += [cuboid([4.5,10,4.5],[11.5,10.5,11.5],'lid'),cuboid([4,10.5,4],[12,11.5,12],'lid')]
    return {'ambientocclusion':False,'render_type':'minecraft:cutout','textures':{'glass':PREFIX+'jar_glass','lid':PREFIX+'jar_lid','particle':PREFIX+'jar_lid'},'elements':parts}

def journal_model():
    return {'ambientocclusion':True,'textures':{'cover':PREFIX+'journal_cover','pages':PREFIX+'journal_pages','spine':PREFIX+'journal_spine','particle':PREFIX+'journal_cover'},'elements':[
        cuboid([2,0,2],[14,.6,14],'cover'),cuboid([2.8,.6,2.5],[13.5,2.5,13.5],'pages'),
        cuboid([2,2.5,2],[14,3.1,14],'cover'),cuboid([2,.6,2],[2.8,2.5,14],'spine')]}

def outputs():
    out={};images={}
    im=Image.new('RGBA',(16,16),(0,0,0,0));d=ImageDraw.Draw(im)
    d.line((0,1,0,14),fill='#7c9993');d.line((15,1,15,14),fill='#728a82')
    d.line((1,0,14,0),fill='#9ab0a0');d.line((1,15,14,15),fill='#7e9a8e')
    d.line((3,3,3,6),fill='#b3c5b6');d.point((4,2),fill='#d0dac9');d.line((12,11,12,13),fill='#81998e')
    images['jar_glass']=im
    im=Image.new('RGBA',(16,16),'#716d50');d=ImageDraw.Draw(im);d.rectangle((0,0,15,15),outline='#343e34');d.line((2,2,13,2),fill='#a6a17c')
    for x,y in ((4,5),(10,5),(7,10)):d.point((x,y),fill='#29342c')
    images['jar_lid']=im
    im=Image.new('RGBA',(16,16),'#665040');d=ImageDraw.Draw(im);d.line((0,0,0,15),fill='#352f29');d.rectangle((2,1,14,14),outline='#7f6550')
    d.rectangle((5,4,11,9),fill='#b3ac8e');d.line((6,6,10,6),fill='#5c5a49');d.line((6,8,9,8),fill='#7a7661');d.point((13,13),fill='#a08462')
    images['journal_cover']=im
    im=Image.new('RGBA',(16,16),'#cfc3a1');d=ImageDraw.Draw(im)
    for y in (2,5,9,12):d.line((0,y,15,y),fill='#a99e83')
    d.line((1,1,1,14),fill='#b4a78b');images['journal_pages']=im
    im=Image.new('RGBA',(16,16),'#4d3d32');d=ImageDraw.Draw(im)
    for y in (2,13):d.line((0,y,15,y),fill='#80644b')
    images['journal_spine']=im
    im=Image.new('RGBA',(8,8),(0,0,0,0));d=ImageDraw.Draw(im);d.rectangle((2,2,5,5),fill=(151,185,72,48));d.rectangle((3,3,4,4),fill=(238,246,151,255));images['firefly']=im
    for name,im in images.items():
        data=io.BytesIO();im.save(data,format='PNG');out[A/f'textures/block/cabin/{name}.png']=data.getvalue()
    for n in range(4):out[A/f'models/block/literary_jar_{n}.json']=jar_model()
    out[A/'models/block/literary_ledger_3.json']=journal_model()
    item=jar_model();item['display']={'gui':{'rotation':[20,35,0],'scale':[.9,.9,.9]},'ground':{'translation':[0,2,0],'scale':[.6,.6,.6]}}
    out[A/'models/item/grasshopper_jar.json']=item
    return {p:(json.dumps(v,indent=2)+'\n').encode() if isinstance(v,dict) else v for p,v in out.items()}

if __name__=='__main__':
    p=argparse.ArgumentParser();p.add_argument('--check',action='store_true');args=p.parse_args();bad=[]
    for path,data in outputs().items():
        if args.check:
            if not path.exists() or path.read_bytes()!=data:bad.append(str(path.relative_to(ROOT)))
        else:path.parent.mkdir(parents=True,exist_ok=True);path.write_bytes(data)
    if bad:raise SystemExit('Outdated cabin assets: '+', '.join(bad))
    print('Hollow jar, cabin-only journal and six native-pixel materials checked.')
