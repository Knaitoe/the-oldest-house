"""Crop the original generated mad handwriting into sixteen independent native papers."""
from pathlib import Path
import json,re,shutil,sys
from PIL import Image
ROOT=Path(__file__).resolve().parents[1]
ASSET=ROOT/'src/main/resources/assets/the_oldest_house'
def run(sheet):
    im=Image.open(sheet).convert('RGBA')
    p=ROOT/'src/main/java/io/github/knaitoe/theoldesthouse/house/VignetteDetailBlock.java'
    source=p.read_text()
    names=[f'SCRIBBLE_{i}' for i in range(16)]
    if 'SCRIBBLE_0' not in source:source=source.replace('CHILD_FABRIC;','CHILD_FABRIC, '+', '.join(names)+';')
    shapes=[];states=json.loads((ASSET/'blockstates/vignette_detail.json').read_text())
    for i in range(16):
        x,y=i%4,i//4
        tile=im.crop((round(im.width*x/4),round(im.height*y/4),round(im.width*(x+1)/4),round(im.height*(y+1)/4))).resize((64,64),Image.Resampling.NEAREST)
        tile.save(ASSET/f'textures/block/zampano_scribble_{i}.png',optimize=True)
        bounds=[1,0,1,15,.25,15] if i<8 else [1,0,15.75,15,16,16]
        material='the_oldest_house:block/zampano_scribble_'+str(i)
        model={'ambientocclusion':False,'textures':{'paper':material,'particle':material},'elements':[{'from':bounds[:3],'to':bounds[3:],'faces':{f:{'uv':[0,0,16,16],'texture':'#paper'} for f in ['north','south','east','west','up','down']}}]}
        (ASSET/f'models/block/zampano_scribble_{i}.json').write_text(json.dumps(model,indent=2)+'\n')
        for face,turn in [('north',0),('east',90),('south',180),('west',270)]:states['variants'][f'facing={face},kind=scribble_{i}']={'model':'the_oldest_house:block/zampano_scribble_'+str(i),'y':turn}
        shapes.append('            case SCRIBBLE_'+str(i)+' -> new double[][]{{'+','.join(str(n) for n in bounds)+'}};')
    source=re.sub(r'^\s*case SCRIBBLE_\d+ ->[^\n]+\n','',source,flags=re.M)
    source=source.replace('        };VoxelShape shape=','\n'.join(shapes)+'\n        };VoxelShape shape=')
    p.write_text(source);(ASSET/'blockstates/vignette_detail.json').write_text(json.dumps(states,indent=2)+'\n')
    art=ROOT/'art/hunt_review_0455';art.mkdir(parents=True,exist_ok=True);shutil.copy2(sheet,art/'zampano-manuscripts.png')
    print('Packed sixteen distinct mad handwriting textures and native paper meshes; no repair or engineering motifs.')
if __name__=='__main__':run(Path(sys.argv[1]))
