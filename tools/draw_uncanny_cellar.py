"""Authored sixteen-pixel masonry/tissue, the cellar key and the engulfment UI."""
from pathlib import Path
import argparse
import json
from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parents[1]
A = ROOT / 'src/main/resources/assets/the_oldest_house'
NAMES = ('cellar_brick', 'cellar_floor', 'cellar_plaster', 'flesh_skin', 'flesh_fat', 'flesh_veins', 'flesh_raw', 'flesh_eye_left', 'flesh_eye_right', 'flesh_mouth', 'flesh_nose')

def tissue(kind):
    im = Image.new('RGBA', (16,16), '#ca8e86'); d = ImageDraw.Draw(im)
    if kind in ('flesh_eye_left','flesh_eye_right','flesh_mouth','flesh_nose'):
        d.rectangle((0,0,15,15),fill='#ba9184')
        d.line((0,14,5,12,10,13,15,11),fill='#8f6c67')
        if kind.startswith('flesh_eye_'):
            low=6 if kind.endswith('left') else 8
            d.polygon(((1,low),(4,low-2),(10,low-1),(14,low+2),(11,low+4),(4,low+3)),fill='#6e5154')
            d.line((3,low+1,7,low,12,low+2),fill='#d6c8ad',width=2)
            d.rectangle((7,low,9,low+2),fill='#413e37');d.point((8,low),fill='#9e9c83')
            d.line((1,low-2,7,low-4,14,low-1),fill='#8e7067',width=2)
        elif kind=='flesh_mouth':
            d.rectangle((0,0,15,15),fill='#ba9184')
            d.polygon(((6,2),(9,2),(12,5),(12,11),(9,14),(6,14),(3,11),(3,5)),fill='#89656a')
            d.polygon(((6,3),(9,3),(11,6),(11,11),(9,13),(6,13),(4,10),(4,6)),fill='#342a2c')
            d.line((5,4,6,3,9,3,10,4),fill='#c0a896')
        else:
            d.polygon(((6,0),(9,0),(12,11),(9,14),(3,12)),fill='#cda18e')
            d.rectangle((3,11,5,12),fill='#775556');d.rectangle((9,11,11,12),fill='#775556')
            d.line((5,2,4,9),fill='#ddbaa2')
        return im
    if kind == 'flesh_fat':
        d.rectangle((0,0,15,15), fill='#a87972')
        for x,y in ((-3,-2),(7,-4),(1,6),(11,7)):
            d.ellipse((x,y,x+10,y+10), fill='#dec49f', outline='#c19b84')
            d.line((x+3,y+2,x+6,y+2), fill='#f0ddbb')
        d.line((0,13,3,12,5,14,7,13,10,15), fill='#9e6869')
    elif kind == 'flesh_raw':
        d.rectangle((0,0,15,15), fill='#a65d69')
        for x,y in ((-2,3),(6,-3),(9,8)):
            d.ellipse((x,y,x+8,y+10), fill='#ca8490', outline='#863e56')
            d.line((x+2,y+3,x+4,y+1,x+6,y+2), fill='#e5a6a1', width=1)
        d.line((0,11,4,9,7,10,10,7,15,6), fill='#71374c')
    else:
        # Broad, overlapping skin folds. Highlight clusters follow the fold, never wood-grain stripes.
        d.polygon(((0,0),(8,0),(11,3),(10,6),(6,8),(0,6)), fill='#dfaa99')
        d.line((0,6,4,8,8,7,11,4,15,4), fill='#a76570')
        d.line((0,5,4,7,7,6,10,3,15,3), fill='#edc6ac')
        d.polygon(((1,15),(2,11),(7,9),(12,10),(15,13),(15,15)), fill='#dba294')
        d.line((1,15,2,11,6,9,10,10,14,13,15,13), fill='#a26a72')
        d.line((3,12,6,10,9,11), fill='#edbea7')
        d.rectangle((11,0,12,1), fill='#f0d5bb'); d.point((14,9), fill='#eed2bb')
        if kind == 'flesh_veins':
            for xy in ((0,2,3,4,6,4,8,7,11,8,13,12,15,13),(6,4,5,1,6,0),(11,8,13,6,15,6),(8,7,6,11,7,15)):
                d.line(xy, fill='#94576f'); d.point((xy[0],xy[1]), fill='#ba7385')
    return im

def images():
    out = {}
    im=Image.new('RGBA',(16,16),'#665b50'); d=ImageDraw.Draw(im)
    for y,offset in ((0,0),(8,-4)):
        for x in range(offset,16,8):
            d.rectangle((x,y,x+6,y+6),fill='#887466');d.line((x+1,y+1,x+5,y+1),fill='#a08b76')
            d.line((x,y+6,x+6,y+6),fill='#5b514a')
    d.line((11,1,11,4,12,5),fill='#4e4942');out['block/uncanny/cellar_brick']=im
    im=Image.new('RGBA',(16,16),'#77786b');d=ImageDraw.Draw(im)
    d.line((0,15,15,15),fill='#565b51');d.line((15,0,15,15),fill='#565b51')
    for xy in ((2,3,5,3),(8,11,10,12),(12,5,13,5)):d.line(xy,fill='#939180')
    d.line((3,0,4,3,3,6,5,9,4,12,6,15),fill='#5f6358');out['block/uncanny/cellar_floor']=im
    im=Image.new('RGBA',(16,16),'#bbb195');d=ImageDraw.Draw(im)
    d.polygon(((0,9),(4,8),(5,11),(10,10),(13,13),(15,12),(15,15),(0,15)),fill='#8a8471')
    d.line((2,0,3,4,5,5,4,8),fill='#777563');d.line((0,8,4,7,5,10,10,9),fill='#d8c9aa')
    out['block/uncanny/cellar_plaster']=im
    for name in NAMES[3:]:out['block/uncanny/'+name]=tissue(name)
    im=Image.new('RGBA',(16,16));d=ImageDraw.Draw(im)
    d.ellipse((2,1,9,7),fill='#b39760',outline='#534e43');d.rectangle((4,3,6,5),fill=(0,0,0,0))
    d.line((7,6,13,12),fill='#d0b67a',width=2);d.line((8,8,8,10,10,10,10,12),fill='#776b4e')
    d.point((4,2),fill='#e2cf97');d.line((11,11,13,9),fill='#b39760',width=2)
    out['item/cellar_key']=im
    im=Image.new('RGBA',(18,18));d=ImageDraw.Draw(im)
    d.ellipse((1,1,16,16),fill='#874d5f');d.ellipse((3,3,14,14),fill='#d79d8e')
    d.line((2,12,5,7,8,9,11,5,15,7),fill='#62354b',width=2)
    d.line((4,13,7,10,11,12,14,10),fill='#ecd3af',width=2);out['mob_effect/engulfed']=im
    # Pixel tissue reaches from the screen edges. The centre is genuinely transparent.
    im=Image.new('RGBA',(64,64));d=ImageDraw.Draw(im)
    for side in range(4):
        strip=Image.new('RGBA',(64,64));q=ImageDraw.Draw(strip)
        edge=[(0,0),(63,0),(63,8),(57,9),(54,14),(49,12),(45,7),(38,9),(35,15),(30,13),(27,8),(20,12),(15,10),(11,16),(6,13),(3,8),(0,9)]
        q.polygon(edge,fill='#ba7d79');q.line(edge[2:],fill='#e8bea4',width=2)
        q.line((0,4,10,5,16,3,25,5,34,3,43,5,53,3,63,5),fill='#8e5263',width=2)
        q.line((10,5,12,11,11,15),fill='#8e5263');q.line((34,3,35,9,32,12),fill='#8e5263')
        strip=strip.rotate(side*90);im=Image.alpha_composite(im,strip)
    out['misc/engulfed']=im
    return out

def definitions():
    out={}
    for name in NAMES:
        out['models/block/uncanny_'+name+'.json']={'parent':'minecraft:block/cube_all','textures':{'all':'the_oldest_house:block/uncanny/'+name}}
        if not name.startswith('flesh_'):
            out['blockstates/uncanny_'+name+'.json']={'variants':{'':{'model':'the_oldest_house:block/uncanny_'+name}}}
    # The face belongs to the man as a whole; only the south-facing surface
    # carries anatomy. The other five faces retain the same continuous skin.
    for name in NAMES[7:]:
        textures={side:'the_oldest_house:block/uncanny/flesh_skin' for side in ('down','up','north','south','east','west','particle')}
        textures['south']='the_oldest_house:block/uncanny/'+name
        out['models/block/uncanny_'+name+'.json']={'parent':'minecraft:block/cube','textures':textures}
    out['blockstates/cellar_flesh.json']={'variants':{f'layer={i}':{'model':'the_oldest_house:block/uncanny_'+n} for i,n in enumerate(NAMES[3:])}}
    out['models/item/cellar_key.json']={'parent':'minecraft:item/generated','textures':{'layer0':'the_oldest_house:item/cellar_key'}}
    return out

def main():
    parser=argparse.ArgumentParser();parser.add_argument('--check',action='store_true');check=parser.parse_args().check
    for name,im in images().items():
        p=A/'textures'/(name+'.png')
        if check:
            with Image.open(p) as saved:assert saved.convert('RGBA').tobytes()==im.tobytes(),name
        else:p.parent.mkdir(parents=True,exist_ok=True);im.save(p,optimize=True)
    for name,obj in definitions().items():
        p=A/name
        if check:assert json.loads(p.read_text())==obj,name
        else:p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(obj,indent=2)+'\n')
    print(('Verified' if check else 'Painted')+' cellar masonry, four tissue faces, a dedicated key, effect icon and engulfment overlay.')

if __name__=='__main__':main()
