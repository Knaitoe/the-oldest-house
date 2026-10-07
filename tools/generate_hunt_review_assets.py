"""Package the original generated material atlas into game UVs and native meshes."""
from pathlib import Path
import json, re, shutil, sys
from PIL import Image

ROOT=Path(__file__).resolve().parents[1]
ASSET=ROOT/'src/main/resources/assets/the_oldest_house'
NAMES=['fan_wallpaper','fan_oak','fan_enamel','fan_diagram','tools','cushion','curtain','leather',
       'hide','skull','hunter_canvas','birch_bark','fern','family_board','recorder','child_fabric']
PROPS={
 'tool_tray': [('tools',[1,0,2,15,2,14])],
 'repair_diagram':[('fan_diagram',[1,1,15.7,15,15,16])],
 'fan_wallpaper':[('fan_wallpaper',[0,0,15.8,16,16,16])],
 'curtain':[('curtain',[0,0,15,16,16,16])],
 'cushion':[('cushion',[3,0,3,13,3,13])],
 'carcass':[('hide',[1,0,1,15,5,15]),('hunter_canvas',[3,5,4,13,7,12])],
 'elk_skull':[('skull',[3,0,3,13,5,13]),('hide',[1,4,2,4,7,5]),('hide',[12,4,2,15,7,5])],
 'low_branch':[('fern',[0,8,0,16,16,16])],
 'family_board':[('family_board',[0,0,15,16,16,16])],
 'recorder':[('recorder',[2,0,3,14,3,12])],
 'child_fabric':[('child_fabric',[0,0,15.8,16,16,16])],
}
def run(sheet):
    im=Image.open(sheet).convert('RGBA'); tiles={}
    for i,name in enumerate(NAMES):
        x,y=i%4,i//4
        tile=im.crop((round(im.width*x/4),round(im.height*y/4),round(im.width*(x+1)/4),round(im.height*(y+1)/4))).resize((64,64),Image.Resampling.NEAREST)
        tile.save(ASSET/f'textures/block/hunt_{name}.png',optimize=True);tiles[name]=tile
    # Keep the existing image tiles and any separately packed manuscript models unchanged.
    source=(ROOT/'src/main/java/io/github/knaitoe/theoldesthouse/house/VignetteDetailBlock.java').read_text()
    if 'TOOL_TRAY' not in source:source=source.replace('CHILD_WALL;','CHILD_WALL, '+', '.join(k.upper() for k in PROPS)+';')
    for name in PROPS:source=re.sub(r'^\s*case '+name.upper()+r' ->[^\n]+\n','',source,flags=re.M)
    entries=[]
    for name,cubes in PROPS.items():
        textures={m:'the_oldest_house:block/hunt_'+m for m,_ in cubes}
        elements=[{'from':b[:3],'to':b[3:],'faces':{f:{'uv':[0,0,16,16],'texture':'#'+m} for f in ['north','south','east','west','up','down']}} for m,b in cubes]
        model={'ambientocclusion':True,'textures':dict(textures,particle=next(iter(textures.values()))),'elements':elements}
        (ASSET/f'models/block/hunt_{name}.json').write_text(json.dumps(model,indent=2)+'\n')
        entries.append('            case '+name.upper()+' -> new double[][]{'+','.join('{'+','.join(str(v) for v in b)+'}' for _,b in cubes)+'};')
    source=source.replace('        };VoxelShape shape=', '\n'.join(entries)+'\n        };VoxelShape shape=')
    (ROOT/'src/main/java/io/github/knaitoe/theoldesthouse/house/VignetteDetailBlock.java').write_text(source)
    p=ASSET/'blockstates/vignette_detail.json';states=json.loads(p.read_text())
    for name in PROPS:
        for face,turn in [('north',0),('east',90),('south',180),('west',270)]:
            states['variants'][f'facing={face},kind={name}']={'model':'the_oldest_house:block/hunt_'+name,'y':turn}
    p.write_text(json.dumps(states,indent=2)+'\n')
    # Distinct furniture materials without changing its existing geometry or identity.
    desk=ASSET/'models/block/furniture_reading_desk.json';model=json.loads(desk.read_text())
    model['textures']={k:'the_oldest_house:block/hunt_fan_oak' for k in model['textures']};desk.write_text(json.dumps(model,indent=2)+'\n')
    art=ROOT/'art/hunt_review_0455';art.mkdir(parents=True,exist_ok=True);shutil.copy2(sheet,art/'material-atlas.png')
    print(f'Packed {len(NAMES)} original generated materials and {len(PROPS)} distinct native prop meshes.')
if __name__=='__main__':run(Path(sys.argv[1]))
