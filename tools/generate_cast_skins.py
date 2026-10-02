"""Generated material sheets imported onto precise existing native UVs.

No actors or saved appearance IDs change. Existing faces, hair, eyes, collars,
bandages and alpha outside authored UV nets remain intact.
"""
from pathlib import Path
import json
from PIL import Image,ImageDraw
ROOT=Path(__file__).resolve().parents[1]
OUT=ROOT/'src/main/resources/assets/the_oldest_house/textures/entity'
ART=ROOT/'art/npcs'

def tile_atlas(name,cols,rows):
    im=Image.open(ART/name).convert('RGB');w=im.width//cols;h=im.height//rows
    return [im.crop((x*w,y*h,(x+1)*w,(y+1)*h)).resize((16,16),Image.Resampling.BOX) for y in range(rows) for x in range(cols)]

def fill(im,box,tile,factor=1,resolution=1):
    for y in range(box[1],box[3]):
        for x in range(box[0],box[2]):
            rgb=tile.getpixel((x//resolution%16,y//resolution%16));im.putpixel((x,y),tuple(round(c*factor) for c in rgb)+(255,))

def girl(name,material,monster=False):
    tiles=tile_atlas(material,2,2);im=Image.new('RGBA',(64,64));d=ImageDraw.Draw(im)
    flesh=(119,107,99) if monster else (155,111,88)
    for box in [(0,0,32,16),(32,0,64,16)]:fill(im,box,tiles[1])
    for box in [(0,8,8,16),(16,8,24,16)]:d.rectangle((box[0],box[1],box[2]-1,box[3]-1),fill=flesh+(255,))
    # Actual generated front face at the native 8x8 face UV, then eye/mouth pixels for readability.
    im.paste(tiles[0].resize((8,8),Image.Resampling.BOX).convert('RGBA'),(8,8))
    for box in [(16,16,40,32),(40,16,56,32),(32,48,48,64)]:fill(im,box,tiles[2])
    for box in [(0,16,16,32),(16,48,32,64)]:fill(im,box,tiles[3])
    for box in [(40,28,56,32),(32,60,48,64)]:d.rectangle((box[0],box[1],box[2]-1,box[3]-1),fill=flesh+(255,))
    d.rectangle((0,28,15,31),fill=(31,28,25,255));d.rectangle((16,60,31,63),fill=(31,28,25,255))
    # Native hair overlay only at the back and sides; clear front never hides the girl's face.
    d.rectangle((40,8,47,15),fill=(0,0,0,0))
    d.rectangle((32,8,35,15),fill=(29,23,20,255));d.rectangle((52,8,63,15),fill=(29,23,20,255))
    if monster:
        for x in (10,13):d.point((x,11),fill=(171,174,170,255));d.point((x,12),fill=(43,40,38,255))
        d.line((10,14,13,14),fill=(30,25,23,255));d.point((12,14),fill=(147,136,122,255))
        d.line((14,9,13,12),fill=(32,25,20,255));d.point((22,25),fill=(47,44,39,255))
    else:
        # Keep her features readable at native eight-pixel face resolution.
        d.line((9,10,10,10),fill=(57,36,27,255));d.line((13,10,14,10),fill=(57,36,27,255))
        for white,iris in [(9,10),(14,13)]:
            d.point((white,11),fill=(201,184,162,255));d.point((iris,11),fill=(49,35,28,255))
        d.point((11,12),fill=(122,80,61,255));d.point((12,13),fill=(175,125,96,255))
        d.line((11,14,12,14),fill=(125,70,64,255))
    im.save(OUT/f'{name}.png',optimize=True)

def minotaur():
    tiles=tile_atlas('cast-materials.png',4,2)
    p=OUT/'minotaur_materials.png';im=Image.open(OUT/'finale_materials.png').convert('RGBA').resize((256,256),Image.Resampling.BOX)
    # Existing model uses quadrant (0,128) for fur; horns, hands, eyes and skin retain their own tiles.
    fill(im,(0,128,128,256),tiles[6]);im.save(p,optimize=True)

def cast():
    tiles=tile_atlas('cast-materials.png',4,2)
    for name in [f'trailer_child_{i}' for i in range(6)]+['lake_boy','lake_preacher','lake_congregant']+[f'lake_congregant_{i}' for i in range(1,4)]:
        p=OUT/f'{name}.png'
        if not p.exists():continue
        im=Image.open(p).convert('RGBA');old=im.copy();idx=int(name[-1]) if name.startswith('trailer_child_') else 0
        cloth=tiles[(2+idx%4)%6] if name.startswith('trailer_child') else tiles[3 if name=='lake_boy' else 4 if name=='lake_preacher' else 5]
        for box in [(16,16,40,32),(40,16,56,32),(32,48,48,64)]:fill(im,box,cloth,.83 if name.startswith('lake_congregant') else 1)
        # Reapply exposed hands, neckline and preacher collar from the exact prior skin.
        for box in [(40,25,56,32),(32,57,48,64),(20,20,28,22)]:im.paste(old.crop(box),box[:2])
        im.save(p,optimize=True)
    for name,materials in [('mother_of_strays',{'dress':0,'hem':1,'shawl':0}),('mother_pekingese',{'fur':7,'mane':7,'ear':7}),('clap_ghost_girl',{'cloth':5})]:
        spec=json.loads((ROOT/f'art/{name}.model.json').read_text());resolution=spec['texture']['width']//spec['texture'].get('uv_width',spec['texture']['width'])
        paths=[s['path'] for s in spec['texture'].get('stages',[])] or [spec['texture']['path']]
        for stage,file in enumerate(paths):
            im=Image.open(OUT/file).convert('RGBA')
            for part in spec['parts']:
                for c in part['cubes']:
                    if c['material'] not in materials:continue
                    u,v=c['uv'];w,h,depth=c.get('uv_size',c['size']);w,h,depth=map(lambda n:round(n),[w,h,depth])
                    fill(im,(u*resolution,v*resolution,(u+2*(w+depth))*resolution,(v+h+depth)*resolution),tiles[materials[c['material']]],1-stage*.018,resolution)
            im.save(OUT/file,optimize=True)

if __name__=='__main__':
    girl('lake_witch_memory','lake-girl-materials.png');girl('lake_witch','lake-witch-materials.png',True);cast();minotaur()
    print('Imported memory/hunting girl, sixteen Mother stages, Pekingese, ghost, cousins, boys and lake congregation into native UVs.')
