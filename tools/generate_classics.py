"""Author exact native UV textures and block/item meshes for the two classic wings.

The assets are resource-pack models rendered by Minecraft, including animated
wallpaper, torn plaster, a marquetry table and three-dimensional keepsakes.
"""
from pathlib import Path
import json,math,random
from PIL import Image,ImageDraw

ROOT=Path(__file__).resolve().parents[1]
A=ROOT/'src/main/resources/assets/the_oldest_house'
def write(name,obj):
    p=A/name;p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(obj,indent=2)+'\n')
def save(name,image):
    p=A/name;p.parent.mkdir(parents=True,exist_ok=True);image.save(p,optimize=True)
def cube(a,b,texture='surface',uv=(0,0,16,16),rotation=None):
    e={'from':a,'to':b,'faces':{face:{'texture':'#'+texture,'uv':list(uv)} for face in ['north','south','east','west','up','down']}}
    if rotation:e['rotation']=rotation
    return e
def model(name,elements,textures,display=None):
    obj={'textures':textures,'elements':elements,'ambientocclusion':True}
    if display:obj['display']=display
    write('models/'+name+'.json',obj)
def material(name,base,grain=False):
    rng=random.Random(name);im=Image.new('RGBA',(32,32));d=ImageDraw.Draw(im)
    for y in range(32):
        for x in range(32):
            shade=rng.randrange(-7,8)+(int(5*math.sin(x*.7+y*.12)) if grain else 0)
            im.putpixel((x,y),tuple(max(0,min(255,c+shade)) for c in base)+(255,))
    save('textures/block/'+name+'.png',im);return im
wood=material('seance_wainscot',(59,39,32),True);d=ImageDraw.Draw(wood)
for x in (2,29):d.line((x,1,x,30),fill=(111,77,48,255))
for y in (2,29):d.line((2,y,29,y),fill=(102,69,44,255))
d.rectangle((5,6,26,25),outline=(32,22,20,255));save('textures/block/seance_wainscot.png',wood)
wall=material('seance_wallpaper',(91,72,62));d=ImageDraw.Draw(wall)
for x in range(0,32,8):
    for y in range(0,32,8):d.ellipse((x+1,y+1,x+6,y+6),outline=(118,93,69,255));d.point((x+3,y+3),fill=(148,119,84,255))
save('textures/block/seance_wallpaper.png',wall)
for name in ('seance_wainscot','seance_wallpaper'):
    write('blockstates/'+name+'.json',{'variants':{'':{'model':'the_oldest_house:block/'+name}}})
    write('models/block/'+name+'.json',{'parent':'minecraft:block/cube_all','textures':{'all':'the_oldest_house:block/'+name}})

def wallpaper(figure,peeled):
    strip=Image.new('RGBA',(32,256));rng=random.Random(9)
    for frame in range(8):
        im=Image.new('RGBA',(32,32));d=ImageDraw.Draw(im)
        for y in range(32):
            for x in range(32):
                n=rng.randrange(-5,6);im.putpixel((x,y),(max(0,173+n-y//8),max(0,145+n-y//10),max(0,65+n),255))
        for y in range(-8,40,12):
            for x in range(-4,36,10):
                bend=(frame//2)%3-1;d.line((x,y,x+3+bend,y+5,x,y+11),fill=(105,99,45,255),width=1)
                d.ellipse((x+1,y+3,x+6,y+7),outline=(138,118,50,255));d.point((x+3,y+5),fill=(185,156,68,255))
        if peeled:
            d.polygon([(6,0),(25,0),(24,7),(28,11),(24,18),(27,25),(23,31),(7,31),(4,22),(7,16),(3,9)],fill=(183,173,151,255))
            d.line((6,0,4,9,7,16,4,22,7,31),fill=(75,66,44,255),width=2)
            d.polygon([(4,22),(9,23),(12,28),(7,31)],fill=(203,180,100,255))
            d.line((8,7,23,8),fill=(140,129,108,255));d.line((7,23,23,20),fill=(139,129,109,255))
        if figure:
            dx=(frame//2)%3-1;ink=(74,71,40,255)
            d.ellipse((12+dx,3,18+dx,10),fill=ink)
            d.polygon([(13+dx,10),(18+dx,10),(21+dx,20),(18+dx,28),(12+dx,28),(10+dx,20)],fill=ink)
            d.line((12+dx,13,7+dx,16,5+dx,22),fill=ink,width=2);d.line((18+dx,13,24+dx,17,26+dx,24),fill=ink,width=2)
            d.line((13+dx,27,9+dx,30),fill=ink,width=2);d.line((18+dx,27,23+dx,30),fill=ink,width=2)
            # Floral bars cross the form so it reads as someone behind the pattern.
            for y in (12,20):d.line((0,y,31,y+2),fill=(139,122,50,255),width=1)
        strip.paste(im,(0,frame*32))
    return strip
variants={}
for figure in (False,True):
    for peeled in (False,True):
        name='nursery_wallpaper'+('_figure' if figure else '')+('_peeled' if peeled else '')
        save('textures/block/'+name+'.png',wallpaper(figure,peeled));write('textures/block/'+name+'.png.mcmeta',{'animation':{'frametime':6,'interpolate':False}})
        elements=[cube([0,0,0],[16,16,16])]
        if peeled:elements.extend([cube([2,0,16],[3,9,16.25]),cube([11,4,16],[12,14,16.25])])
        model('block/'+name,elements,{'surface':'the_oldest_house:block/'+name,'particle':'the_oldest_house:block/'+name})
        variants[f'figure={str(figure).lower()},peeled={str(peeled).lower()}']={'model':'the_oldest_house:block/'+name}
write('blockstates/nursery_wallpaper.json',{'variants':variants})
table=material('seance_table',(76,47,29),True);d=ImageDraw.Draw(table)
d.rectangle((2,2,29,29),outline=(154,115,54,255),width=2);d.polygon([(16,5),(27,16),(16,27),(5,16)],outline=(146,111,62,255));d.ellipse((12,12,20,20),outline=(157,132,72,255));save('textures/block/seance_table.png',table)
model('block/seance_table',[cube([0,14,0],[16,16,16]),cube([1,0,1],[3,14,3]),cube([13,0,1],[15,14,3]),cube([1,0,13],[3,14,15]),cube([13,0,13],[15,14,15]),cube([2,3,2],[14,5,4]),cube([2,3,12],[14,5,14])],{'surface':'the_oldest_house:block/seance_table','particle':'the_oldest_house:block/seance_table'})
write('blockstates/seance_table.json',{'variants':{'':{'model':'the_oldest_house:block/seance_table'}}})
display={'gui':{'rotation':[30,225,0],'translation':[0,0,0],'scale':[.95,.95,.95]},'ground':{'rotation':[0,0,0],'translation':[0,2,0],'scale':[.45,.45,.45]},'fixed':{'rotation':[0,180,0],'scale':[.75,.75,.75]},'firstperson_righthand':{'rotation':[0,30,0],'translation':[0,2,0],'scale':[.65,.65,.65]},'thirdperson_righthand':{'rotation':[75,45,0],'translation':[0,1,0],'scale':[.65,.65,.65]}}
plan=Image.new('RGBA',(32,32),(66,38,25,255));d=ImageDraw.Draw(plan);d.ellipse((7,7,25,25),outline=(188,154,88,255),width=2);d.ellipse((12,12,20,20),fill=(28,35,38,255));d.arc((11,11,21,21),15,155,fill=(182,204,203,255),width=2);save('textures/item/seance_planchette.png',plan)
model('item/seance_planchette',[cube([3,4,3],[13,5.5,9]),cube([5,4,9],[11,5.5,12]),cube([6,4,12],[10,5.5,14]),cube([3,2,4],[5,4,6]),cube([11,2,4],[13,4,6]),cube([7,2,11],[9,4,13])],{'surface':'the_oldest_house:item/seance_planchette','particle':'the_oldest_house:item/seance_planchette'},display)
paper=Image.new('RGBA',(32,32),(199,174,94,255));d=ImageDraw.Draw(paper)
for y in range(5,27,4):d.line((6,y,25-(y%7),y),fill=(87,76,53,255))
d.rectangle((2,2,29,29),outline=(131,109,55,255));d.line((4,1,4,31),fill=(74,52,32,255),width=2);save('textures/item/wallpaper_folio.png',paper)
model('item/wallpaper_folio',[cube([2,3,2],[14,4,14]),cube([2,4,2],[4,5,14]),cube([4,4,2],[12,4.5,14]),cube([11,4.5,2],[13,5.5,14]),cube([12,5.5,2],[14,7,14]),cube([1,2.5,2],[2,5,14])],{'surface':'the_oldest_house:item/wallpaper_folio','particle':'the_oldest_house:item/wallpaper_folio'},display)

def skin(name,cloth,hair,flesh):
    # Standard humanoid UVs plus explicit veil/skirt/cuff islands in the right half.
    im=Image.new('RGBA',(128,64));d=ImageDraw.Draw(im);rng=random.Random(name)
    for box,color in [((0,0,32,16),hair),((16,16,40,32),cloth),((40,16,56,32),cloth),((0,16,16,32),(37,31,30)),((16,48,32,64),(37,31,30)),((32,48,48,64),cloth)]:
        for y in range(box[1],box[3]):
            for x in range(box[0],box[2]):
                n=rng.randrange(-7,8);im.putpixel((x,y),tuple(max(0,min(255,c+n)) for c in color)+(255,))
    d.rectangle((8,8,15,15),fill=flesh+(255,));d.rectangle((0,8,7,15),fill=flesh+(255,));d.rectangle((16,8,23,15),fill=flesh+(255,))
    d.rectangle((8,8,15,9),fill=hair+(255,));d.line((9,10,10,10),fill=(51,39,32,255));d.line((13,10,14,10),fill=(51,39,32,255))
    for x in (9,13):d.point((x,11),fill=(214,210,192,255));d.point((x+1,11),fill=(48,60,61,255))
    d.point((11,12),fill=tuple(max(0,c-22) for c in flesh)+(255,));d.line((11,14,12,14),fill=(133,83,75,255))
    for box in [(40,28,56,32),(32,60,48,64)]:d.rectangle((box[0],box[1],box[2]-1,box[3]-1),fill=flesh+(255,))
    d.rectangle((20,20,27,21),fill=(215,204,181,255));d.line((24,22,24,30),fill=(27,24,23,255));
    for y in (23,26,29):d.point((25,y),fill=(157,133,84,255))
    for y in range(64):
        for x in range(64,96):
            n=int(math.sin(x*1.2)*9)+rng.randrange(-4,5);im.putpixel((x,y),tuple(max(0,min(255,c+n)) for c in cloth)+(255,))
    d.rectangle((96,0,127,23),fill=(206,192,162,255));
    if name=='medium':
        for y in range(24):
            for x in range(64,96):
                if (x+y)%4==0:im.putpixel((x,y),(30,27,26,110))
    save('textures/entity/seance_'+name+'.png',im)
skin('medium',(49,46,54),(91,86,83),(186,165,149));skin('mother',(79,67,58),(71,44,30),(212,182,153));skin('father',(64,75,73),(58,49,40),(199,171,143));skin('daughter',(183,174,151),(132,97,62),(224,194,164))
lang=A/'lang/en_us.json';strings=json.loads(lang.read_text());strings.update({'item.the_oldest_house.seance_planchette':'An empty sitting','item.the_oldest_house.wallpaper_folio':'The pages she kept','block.the_oldest_house.nursery_wallpaper':'Nursery wallpaper','block.the_oldest_house.seance_table':'Marquetry table','block.the_oldest_house.seance_wallpaper':'Drawing-room wallpaper','block.the_oldest_house.seance_wainscot':'Paneled wainscot','entity.the_oldest_house.seance_actor':'Someone at the table'});lang.write_text(json.dumps(strings,indent=2)+'\n')
print('Generated four cast skins, four animated wallpaper variants, framed wall materials, table and two native 3D items.')
