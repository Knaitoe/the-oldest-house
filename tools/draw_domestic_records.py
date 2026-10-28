"""Small native paper surfaces. Drawings and inscriptions are readable shared originals in game."""
from pathlib import Path
import argparse,json,copy
from PIL import Image,ImageDraw
ROOT=Path(__file__).resolve().parents[1];A=ROOT/'src/main/resources/assets/the_oldest_house'
KINDS=['note','drawing','photo','award','recipe','family_drawing','couple']
def paintings():
    out={}
    for name in KINDS:
        im=Image.new('RGBA',(16,16),'#cfbea0');d=ImageDraw.Draw(im);out[name]=im
        if name in ('note','recipe'):
            d.line((1,1,14,1),fill='#e1d1b2')
            for y,n in [(4,10),(7,11),(10,8),(13,5)]:d.line((3,y,n,y),fill='#7a786b')
            if name=='recipe':d.rectangle((11,9,14,13),fill='#a2936f');d.point((12,10),fill='#bca878')
        elif name in ('drawing','family_drawing'):
            d.line((1,14,14,14),fill='#789269')
            if name=='drawing':
                d.rectangle((2,9,5,12),outline='#667f8a');d.rectangle((10,9,13,12),outline='#667f8a');d.line((4,10,10,7),fill='#667f8a');d.line((10,7,12,10),fill='#667f8a')
                d.rectangle((4,2,6,4),outline='#9c795c');d.line((5,5,5,8),fill='#9c795c');d.line((5,6,8,6),fill='#9c795c');d.line((4,9,5,8,6,9),fill='#9c795c');d.rectangle((10,3,11,4),outline='#9c795c');d.line((10,5,10,7),fill='#9c795c');d.line((8,6,10,5),fill='#9c795c')
            else:
                d.line((1,6,8,1,14,6),fill='#9d6a5a');d.rectangle((2,6,13,13),outline='#9d6a5a')
                for x in (4,8,11):d.rectangle((x,6,x+1,7),fill='#7a8e93');d.line((x,8,x,10),fill='#7a8e93')
        elif name=='award':
            im.paste('#a78a48',(0,0,16,16));d=ImageDraw.Draw(im);d.rectangle((1,1,14,14),outline='#d0b56b');d.rectangle((4,3,11,7),outline='#e0c783');d.rectangle((7,8,8,10),fill='#e0c783');d.line((5,11,10,11),fill='#e0c783');d.line((3,13,12,13),fill='#675940')
        else:
            d.rectangle((1,1,14,14),fill='#81917e');d.rectangle((1,11,14,14),fill='#636d56')
            for x,y,c in [(3,4,'#586e79'),(11,4,'#947863'),(7,7,'#94715d')][:2 if name=='couple' else 3]:
                d.rectangle((x,y,x+1,y+1),fill='#c3a58a');d.rectangle((x-1,y+2,x+2,12),fill=c)
            if name=='photo':d.rectangle((5,10,7,12),outline='#758b95');d.rectangle((10,10,12,12),outline='#758b95')
            d.rectangle((0,0,15,15),outline='#d2c6a5')
    return out
def box(a,b,t,uv=None):return {'from':list(a),'to':list(b),'faces':{f:{'texture':'#'+t,**({'uv':uv} if uv else {})} for f in ['north','south','east','west','up','down']}}
def models():
    out={};variants={}
    for kind in KINDS:
        for present in (False,True):
            texture='the_oldest_house:block/domestic_record/'+kind
            tex={'art':texture,'wood':'the_oldest_house:block/walnut','paper':'the_oldest_house:block/domestic/cardboard','particle':texture}
            if kind in ('note','recipe'):
                ps=[box((2,0,2),(14,.5,14),'art' if present else 'paper',[0,0,16,16])]
            elif kind=='award':
                ps=[box((3,0,3),(13,2,13),'wood')]
                if present:ps +=[box((7,2,7),(9,5,9),'art'),box((4,5,7),(12,14,9),'art',[0,0,16,16])]
            else:
                ps=[box((2,2,14),(14,14,16),'wood')]
                if present:ps +=[box((3,3,13.9),(13,13,14),'art',[0,0,16,16])]
            name=f'domestic_record/{kind}_{int(present)}';out[A/f'models/block/{name}.json']={'parent':'minecraft:block/block','textures':tex,'elements':ps}
            for drop in range(17):
                lowered=kind in ('note','recipe','award')
                target=name
                if lowered and drop:
                    target=name+f'_drop_{drop}';shifted=copy.deepcopy(ps)
                    for element in shifted:
                        element['from'][1]-=drop;element['to'][1]-=drop
                    out[A/f'models/block/{target}.json']={'parent':'minecraft:block/block','textures':tex,'elements':shifted}
                for f,rot in [('north',0),('east',90),('south',180),('west',270)]:variants[f'kind={kind},facing={f},present={str(present).lower()},drop={drop}']={'model':'the_oldest_house:block/'+target,'y':rot}
    out[A/'blockstates/domestic_record.json']={'variants':variants};return out
def main():
    ap=argparse.ArgumentParser();ap.add_argument('--check',action='store_true');args=ap.parse_args()
    for n,im in paintings().items():
        p=A/f'textures/block/domestic_record/{n}.png'
        if args.check:
            with Image.open(p) as old:assert old.convert('RGBA').tobytes()==im.tobytes(),n
        else:p.parent.mkdir(parents=True,exist_ok=True);im.save(p,optimize=True)
    for p,data in models().items():
        if args.check:assert json.loads(p.read_text())==data,str(p)
        else:p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(data,indent=2)+'\n')
    print('Verified seven original paintings, finite/empty mounts and native support-height variants.')
if __name__=='__main__':main()
