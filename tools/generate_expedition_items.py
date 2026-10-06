"""Native 16-pixel item artwork and baked equipped/blocking shield models."""
import json
from pathlib import Path
from PIL import Image, ImageDraw

ROOT=Path(__file__).resolve().parents[1]/'src/main/resources/assets/the_oldest_house'
textures=ROOT/'textures/item';models=ROOT/'models/item'
textures.mkdir(parents=True,exist_ok=True)

shield=Image.new('RGBA',(16,16));draw=ImageDraw.Draw(shield)
for y in range(16):
    for x in range(16):
        grain=((x*13+y*7)%9)-4
        shade=(84+grain,68+grain,47+grain,255)
        if x in (0,5,10,15):shade=(40,33,28,255)
        draw.point((x,y),fill=shade)
draw.rectangle((0,0,15,1),fill=(73,71,65,255));draw.rectangle((0,14,15,15),fill=(56,56,52,255))
for x in (2,7,12):
    for y in (1,14):draw.point((x,y),fill=(161,150,120,255));draw.point((x+1,y),fill=(104,72,43,255))
draw.line((3,4,10,11),fill=(159,143,112,255));draw.line((3,5,9,11),fill=(36,29,25,255))
draw.line((12,4,9,7),fill=(126,110,80,255));draw.line((6,2,6,13),fill=(104,81,54,255))
shield.save(textures/'holloway_shield.png')

lighter=Image.new('RGBA',(16,16));d=ImageDraw.Draw(lighter)
d.rectangle((4,6,11,14),fill=(85,63,36,255));d.rectangle((5,7,10,13),fill=(182,151,86,255))
d.line((5,7,5,12),fill=(222,199,136,255));d.line((10,8,10,14),fill=(127,102,55,255))
d.rectangle((4,2,11,5),fill=(136,111,61,255));d.line((5,2,10,2),fill=(224,202,145,255))
d.line((4,5,11,5),fill=(47,38,27,255));d.point((7,10),fill=(226,196,112,255));d.line((7,11,9,12),fill=(108,84,49,255))
lighter.save(textures/'lighter.png')
(models/'lighter.json').write_text(json.dumps({'parent':'minecraft:item/handheld','textures':{'layer0':'the_oldest_house:item/lighter'}},indent=2)+'\n')

def box(a,b,uv=(0,0,16,16)):
    return {'from':a,'to':b,'faces':{face:{'uv':uv,'texture':'#board'} for face in ('north','south','east','west','up','down')}}
display={
    'thirdperson_righthand':{'rotation':[0,90,0],'translation':[10,6,-4],'scale':[1,1,1]},
    'thirdperson_lefthand':{'rotation':[0,90,0],'translation':[10,6,12],'scale':[1,1,1]},
    'firstperson_righthand':{'rotation':[0,180,5],'translation':[-10,2,-10],'scale':[1.25]*3},
    'firstperson_lefthand':{'rotation':[0,180,5],'translation':[10,2,-10],'scale':[1.25]*3},
    'gui':{'rotation':[15,-25,-5],'translation':[0,1,0],'scale':[.65]*3},
    'ground':{'translation':[0,3,0],'scale':[.25]*3},
    'fixed':{'rotation':[0,180,0],'translation':[0,0,-5],'scale':[.5]*3}}
model={'parent':'minecraft:block/block','textures':{'board':'the_oldest_house:item/holloway_shield','particle':'the_oldest_house:item/holloway_shield'},
    'elements':[box([2,2,6],[14,19,8]),box([3,0,6],[13,2,8]),box([4,-2,6],[12,0,8]),box([5,6,8],[6,13,9]),box([10,6,8],[11,13,9])],
    'display':display,'overrides':[{'predicate':{'blocking':1},'model':'the_oldest_house:item/holloway_shield_blocking'}]}
(models/'holloway_shield.json').write_text(json.dumps(model,indent=2)+'\n')
model.pop('overrides');model['display']=dict(display)
model['display']['firstperson_righthand']={'rotation':[0,180,-5],'translation':[-15,5,-11],'scale':[1.25]*3}
model['display']['firstperson_lefthand']={'rotation':[0,180,-5],'translation':[5,5,-11],'scale':[1.25]*3}
(models/'holloway_shield_blocking.json').write_text(json.dumps(model,indent=2)+'\n')
lang=ROOT/'lang/en_us.json';data=json.loads(lang.read_text());data['item.the_oldest_house.holloway_shield']="Holloway's battered shield";data['item.the_oldest_house.lighter']="Tom's lighter";lang.write_text(json.dumps(data,ensure_ascii=False,indent=2)+'\n')
