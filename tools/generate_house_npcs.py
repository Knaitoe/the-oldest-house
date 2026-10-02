"""Import generated cloth into precise native 64x64 player-skin UVs.

The raster material source is art/npcs/clothing-materials.png; faces, seams,
lapels, cuffs and overlays are authored here at their actual native pixel size.
"""
from pathlib import Path
from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / 'src/main/resources/assets/the_oldest_house/textures/entity'
SOURCE = Image.open(ROOT / 'art/npcs/clothing-materials.png').convert('RGB')
half = SOURCE.width // 2
cloth = [SOURCE.crop((x*half,y*half,(x+1)*half,(y+1)*half)).resize((16,16), Image.Resampling.BOX)
         for y in range(2) for x in range(2)]

def material(im, box, tile, factor=1):
    for y in range(box[1], box[3]+1):
        for x in range(box[0], box[2]+1):
            rgb = cloth[tile].getpixel((x%16,y%16))
            im.putpixel((x,y),tuple(round(c*factor) for c in rgb)+(255,))

def skin(name, elder=False, dead=False):
    im=Image.new('RGBA',(64,64)); d=ImageDraw.Draw(im)
    flesh=(170,155,138) if elder else (163,119,89)
    if dead: flesh=(146,145,138)
    hair=(179,177,164) if elder else (62,48,39)
    dark=(72,66,61) if elder else (42,37,30)
    # Every head face has flesh and readable shading; outer hat layer stays clear.
    d.rectangle((0,0,31,15),fill=hair)
    for box in [(0,8,7,15),(8,8,15,15),(16,8,23,15)]: d.rectangle(box,fill=flesh)
    d.rectangle((24,8,31,15),fill=hair)
    d.rectangle((0,8,3,15),fill=hair);d.rectangle((20,8,23,15),fill=hair)
    d.rectangle((8,8,15,8 if elder else 9),fill=hair)
    if elder:
        d.rectangle((11,0,12,7),fill=flesh);d.rectangle((9,9,14,9),fill=flesh)
        d.point((9,10),fill=(125,113,104));d.point((14,10),fill=(125,113,104))
        d.line((9,11,14,11),fill=dark);d.point((10,12),fill=(112,119,121));d.point((13,12),fill=(112,119,121))
        d.point((9,12),fill=dark);d.point((14,12),fill=dark)
        d.point((11,13),fill=(122,110,100));d.line((10,14,13,14),fill=(111,101,94))
    else:
        d.line((9,10,11,10),fill=dark);d.line((13,10,14,10),fill=dark)
        d.point((10,11),fill=(185,174,135));d.point((13,11),fill=(185,174,135))
        d.point((10,12),fill=dark);d.point((13,12),fill=dark)
        d.rectangle((9,14,14,15),fill=hair);d.point((8,13),fill=hair);d.point((15,13),fill=hair)
        d.line((11,14,12,14),fill=(102,71,57))
    if dead: d.line((10,12,10,12),fill=dark);d.line((13,12,13,12),fill=dark)
    for box in [(16,16,39,31),(40,16,55,31),(32,48,47,63)]: material(im,box,1 if elder else 0)
    for box in [(0,16,15,31),(16,48,31,63)]: material(im,box,1 if elder else 0,.78)
    # Native torso front, shirt opening and deliberate collar/tie geometry.
    material(im,(22,20,25,28),3 if elder else 2)
    d.line((20,20,22,24),fill=(58,57,53) if elder else (75,70,42))
    d.line((27,20,25,24),fill=(58,57,53) if elder else (75,70,42))
    d.line((23,21,23,26),fill=(53,36,36) if elder else (40,39,26))
    d.rectangle((23,20,24,20),fill=flesh)
    if elder:
        d.line((20,27,22,28),fill=(147,131,73));d.point((21,25),fill=(78,77,70))
    else:
        d.rectangle((20,24,21,26),fill=(69,65,37));d.rectangle((26,24,27,26),fill=(69,65,37))
        d.line((19,21,19,29),fill=(94,73,49));d.line((28,21,28,29),fill=(94,73,49))
        d.rectangle((20,29,27,29),fill=(61,47,33));d.point((23,29),fill=(146,136,111))
        d.line((33,21,34,29),fill=(94,73,49));d.line((37,21,37,29),fill=(94,73,49))
    for box in [(40,29,55,31),(32,61,47,63)]: d.rectangle(box,fill=flesh)
    for box in [(0,28,15,31),(16,60,31,63)]: d.rectangle(box,fill=(46,39,34))
    d.line((4,30,7,30),fill=(70,58,46));d.line((20,62,23,62),fill=(70,58,46))
    OUT.mkdir(parents=True,exist_ok=True);im.save(OUT/f'{name}.png')
    return im

if __name__=='__main__':
    skins=[skin('holloway'),skin('harrigan',True),skin('harrigan_dead',True,True)]
    assert all(im.size==(64,64) for im in skins)
    assert all(im.getpixel((8,8))[3]==255 and im.getpixel((56,56))[3]==0 for im in skins)
    print('Imported generated cloth into Holloway, Harrigan and Harrigan corpse native skins.')
