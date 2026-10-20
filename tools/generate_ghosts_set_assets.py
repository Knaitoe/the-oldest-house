"""The Barrett home and its camera crew: fitted native meshes and discrete Minecraft pixels.

The painted cast is imported unchanged from ImageGen and deliberately retained. This generator authors the existing
native environment meshes, pixel materials and equipment. Exact cast prompts and hashes: docs/GHOSTS_CAST_AND_TAKE_0_4_88.md.
"""
from pathlib import Path
import json, random, shutil
from PIL import Image, ImageDraw

ROOT=Path(__file__).resolve().parents[1]
A=ROOT/'src/main/resources/assets/the_oldest_house'
T=A/'textures/block/ghosts';T.mkdir(parents=True,exist_ok=True)
M=A/'models/block';S=A/'blockstates';E=A/'textures/entity'
K=['wallpaper','floor','ceiling','tile','panel','table','chair','counter','sink','cooker','plate','hat','books','jug','bolts','camera','case','monitor','drawing','lamp','bed_head','bed_foot','plain_plate','playhouse','torn_card','drawer','drawer_rig','plaster','plain_plate','meal_stew','meal_sandwich','meal_soup','meal_roast','meal_potatoes','lamp_off','rug','stair','rail','table_leg']
palette={'wallpaper':(199,185,159),'floor':(118,85,53),'ceiling':(216,210,188),'tile':(122,135,122),'panel':(107,82,58),
         'wood':(105,75,48),'formica':(161,171,151),'metal':(56,64,61),'cream':(225,215,183),'cardboard':(164,133,90),
         'denim':(64,82,114),'red':(126,47,36),'green':(89,115,61),'cloth':(126,139,155),'white':(220,218,199)}
for name,c in palette.items():
    im=Image.new('RGBA',(16,16));d=ImageDraw.Draw(im);rng=random.Random(name)
    for y in range(16):
        for x in range(16):
            n=rng.randint(-4,4);im.putpixel((x,y),tuple(max(0,min(255,v+n)) for v in c)+(255,))
    if name in ('floor','panel','wood'):
        for y in (0,7,15):d.line((0,y,15,y),fill=tuple(v-20 for v in c)+(255,))
        d.line((3,1,3,6),fill=tuple(v-12 for v in c)+(255,));d.line((11,8,11,14),fill=tuple(v-12 for v in c)+(255,))
    if name=='wallpaper':
        for x,y in ((3,3),(11,11)):d.rectangle((x-1,y-1,x+1,y+1),outline='#a79c7c');d.point((x,y),fill='#8b977c')
    if name=='tile':
        d.line((0,0,15,0),fill='#797b70');d.line((0,0,0,15),fill='#797b70');d.line((8,0,8,15),fill='#bbc0a9');d.line((0,8,15,8),fill='#bbc0a9')
    if name=='metal':
        for x,y in ((2,2),(13,2),(2,13),(13,13)):d.point((x,y),fill='#b9b5a0')
    im.save(T/f'{name}.png')
# Specific surfaces: noodles, sequins, a tape label, controls and the child's vines.
for name in ('spaghetti','spaghetti_plain','hat','controls','label','vines','plaster'):
    im=Image.new('RGBA',(16,16), '#d8d2b5');d=ImageDraw.Draw(im)
    if name in ('spaghetti','spaghetti_plain'):
        d.ellipse((1,1,14,14),fill='#ede6c8',outline='#969d87');d.ellipse((4,4,11,11),fill='#a75536' if name=='spaghetti' else '#c5b272')
        for x,y in ((5,5),(8,5),(6,8),(9,9)):d.line((x,y,x+2,y+1),fill='#d3b374')
    if name=='hat':
        d.rectangle((0,0,15,15),fill='#74638e')
        for y in range(2,16,4):
            for x in range((y//4)%2,16,4):d.point((x,y),fill='#d7c3be');d.point((x+1,y+1),fill='#a79fbc')
    if name=='controls':
        d.rectangle((0,0,15,15),fill='#343e3a');d.rectangle((2,2,13,7),fill='#608479');d.line((3,5,11,5),fill='#acc3a1')
        for x in (3,7,11):d.rectangle((x,10,x+1,11),fill='#9d9e83')
        d.point((13,13),fill='#b34c38')
    if name=='label':
        d.rectangle((0,0,15,15),fill='#394541');d.rectangle((2,3,13,9),fill='#d2c9ac');d.line((4,5,11,5),fill='#655a47');d.line((4,7,9,7),fill='#655a47');d.rectangle((3,11,5,13),fill='#989a83');d.rectangle((10,11,12,13),fill='#989a83')
    if name=='vines':
        d.rectangle((0,0,15,15),fill='#d6c9a6')
        for x in (2,7,12):d.line((x,15,x+2,0),fill='#53794a',width=1)
        for x,y in ((4,3),(7,7),(12,4),(3,11),(10,13)):d.rectangle((x,y,x+1,y+1),fill='#739355')
    if name=='plaster':
        d.rectangle((0,0,15,15),fill='#c7b99f');d.line((3,0,5,6,3,10,4,15),fill='#877a62');d.line((10,1,9,5,12,9,11,15),fill='#a0967c')
        for x,y in ((4,5),(10,10)):d.rectangle((x,y,x+2,y+1),fill='#716652');d.line((x,y+2,x+2,y+2),fill='#e1d3b7')
    im.save(T/f'{name}.png')
source=ROOT/'tools/asset_sources/ghosts_picture_book.png'
if source.exists():
    # Technical texture import: retain the supplied artwork and its aspect, quantizing only to the native pixel grid.
    Image.open(source).convert('RGBA').resize((64,64),Image.Resampling.NEAREST).save(T/'drawing.png')
else:raise FileNotFoundError(source)

def box(a,b,t,face=None):
    faces={f:{'texture':'#'+t,'uv':[0,0,16,16]} for f in ('north','south','east','west','up','down')}
    if face:faces.update(face)
    return {'from':a,'to':b,'faces':faces}
def q(a,b,t):return box(a,b,t)
parts={
'table':[q([0,15,0],[16,16,16],'formica')],
'table_leg':[q([0,15,0],[16,16,16],'formica'),q([1,0,1],[3,15,3],'wood')],
'chair':[q([2,8,2],[14,10,14],'cloth'),q([2,0,2],[4,8,4],'wood'),q([12,0,12],[14,8,14],'wood'),q([2,10,12],[14,22,14],'wood')],
'counter':[q([0,0,0],[16,14,16],'cream'),q([0,14,0],[16,16,16],'formica')],
'sink':[q([0,0,0],[16,14,16],'cream'),q([0,14,0],[16,16,16],'metal'),q([6,16,12],[8,23,14],'metal'),q([6,22,7],[8,24,14],'metal')],
'cooker':[q([0,0,0],[16,15,16],'white'),q([0,15,0],[16,16,16],'metal'),q([2,4,-.1],[14,11,0],'controls')],
'plate':[q([3,0,3],[13,1,13],'spaghetti')],
'plain_plate':[q([3,0,3],[13,1,13],'spaghetti_plain')],
'hat':[q([4,0,4],[12,5,12],'hat'),q([4,0,1],[12,1,5],'hat')],
'books':[q([3,0,4],[13,1,12],'denim'),q([4,1,3],[12,3,13],'red')],
'jug':[q([5,0,5],[11,7,11],'white'),q([6,7,6],[10,9,10],'cream'),q([11,2,7],[13,6,9],'white')],
'bolts':[q([0,15,0],[16,16,16],'formica'),q([4,10,6],[12,13,9],'metal'),q([5,9,6],[7,11,8],'red'),q([10,9,6],[12,11,8],'red'),q([1,0,1],[3,15,3],'wood')],
'camera':[q([5,15,4],[12,21,12],'metal'),q([6,17,0],[11,20,4],'controls'),q([7,0,7],[9,15,9],'metal'),q([2,0,3],[4,9,5],'metal'),q([12,0,11],[14,9,13],'metal')],
'case':[q([1,0,1],[15,12,15],'metal'),q([6,12,5],[10,14,11],'wood'),q([3,4,.9],[13,10,1],'label')],
'monitor':[q([2,0,3],[14,10,13],'metal'),q([3,2,2.9],[13,9,3],'controls')],
'drawing':[q([2,2,15],[14,14,16],'drawing')],
'lamp':[q([7,12,7],[9,16,9],'metal'),q([3,7,3],[13,12,13],'cream'),q([6,6,6],[10,7,10],'white')],
'bed_head':[q([0,6,0],[16,10,16],'cream'),q([0,10,0],[16,12,16],'cloth'),q([1,0,1],[3,6,3],'wood'),q([13,0,13],[15,6,15],'wood'),q([1,12,1],[15,14,5],'white')],
'bed_foot':[q([0,6,0],[16,10,16],'cream'),q([0,10,0],[16,12,16],'cloth'),q([1,0,1],[3,6,3],'wood'),q([13,0,13],[15,6,15],'wood')],
'playhouse':[q([0,0,0],[1,12,16],'cardboard'),q([15,0,0],[16,12,16],'cardboard'),q([1,0,15],[15,12,16],'vines'),q([1,0,0],[5,12,1],'cardboard'),q([11,0,0],[15,12,1],'cardboard'),q([5,9,0],[11,12,1],'cardboard'),q([0,12,0],[16,13,16],'red'),q([3,3,-.1],[4,7,0],'denim'),q([12,3,-.1],[13,7,0],'denim')],
'torn_card':[q([0,0,0],[13,1,9],'cardboard'),q([3,1,3],[16,2,15],'vines'),q([1,2,9],[12,3,16],'red'),q([10,3,2],[15,4,8],'cardboard')],
'drawer':[q([2,0,-.1],[14,4,1],'wood'),q([2,0,1],[3,4,13],'wood'),q([13,0,1],[14,4,13],'wood'),q([3,0,12],[13,4,13],'wood'),q([3,0,1],[13,1,12],'wood'),q([7,1,-1],[9,2,0],'metal')],
'drawer_rig':[q([5,1,2],[11,2,8],'metal'),q([6,2,3],[7,4,4],'metal'),q([8,2,3],[9,4,4],'metal'),q([10,2,3],[11,4,4],'metal'),q([4,3,6],[12,4,7],'red')],
}
variants={}
for name in dict.fromkeys(K):
    elements=parts.get(name,[q([0,0,0],[16,16,16],name)])
    used={v['texture'][1:] for e in elements for v in e['faces'].values()}
    model={'textures':{k:'the_oldest_house:block/ghosts/'+k for k in used},'elements':elements,'ambientocclusion':True}
    target=M/f'ghosts_{name}.json'
    if name not in {"plate","plain_plate","meal_stew","meal_sandwich","meal_soup","meal_roast","meal_potatoes","stair","rail"}:target.write_text(json.dumps(model,indent=2)+'\n')
    elif not target.exists():raise FileNotFoundError("Missing authored mesh: "+str(target))
    for f,y in [('north',0),('east',90),('south',180),('west',270)]:variants[f'kind={name},facing={f}']={'model':'the_oldest_house:block/ghosts_'+name,'y':y}
(S/'ghosts_set.json').write_text(json.dumps({'variants':variants},indent=2)+'\n')

# Painted cast atlases are original ImageGen imports. Never regenerate the retired flat-colour skins.
for name in ('marjorie','merry','john','sarah','barry','father_wanderly','marjorie_dinner','john_hand'):
    if not (E/f'ghosts_{name}.png').exists():raise FileNotFoundError(E/f'ghosts_{name}.png')

item=Image.new('RGBA',(32,32));d=ImageDraw.Draw(item);d.rectangle((5,10,25,23),fill='#3d4943',outline='#b4b39b');d.rectangle((1,12,7,20),fill='#26332f');d.rectangle((8,7,20,10),fill='#6a7161');d.rectangle((11,13,22,20),fill='#739182');d.rectangle((23,8,27,11),fill='#494c3e');d.point((21,12),fill='#b94535')
item.save(A/'textures/item/camcorder.png')
camera={'textures':{'metal':'the_oldest_house:block/ghosts/metal','controls':'the_oldest_house:block/ghosts/controls'},'elements':[q([3,6,5],[13,12,12],'metal'),q([6,7,1],[10,11,5],'controls'),q([4,12,7],[10,14,8],'metal'),q([12,7,5],[15,12,6],'controls')],
        'display':{'gui':{'rotation':[20,135,0],'translation':[0,-1,0],'scale':[.9,.9,.9]},'thirdperson_righthand':{'rotation':[0,-90,0],'translation':[0,2,0],'scale':[.75,.75,.75]},'firstperson_righthand':{'rotation':[0,0,0],'translation':[0,1,0],'scale':[.7,.7,.7]},'ground':{'rotation':[0,0,0],'translation':[0,3,0],'scale':[.5,.5,.5]}},'gui_light':'side'}
(A/'models/item/camcorder.json').write_text(json.dumps(camera,indent=2)+'\n')
gui=Image.new('RGBA',(24,16));d=ImageDraw.Draw(gui);d.rectangle((1,2,18,13),outline='#d5cdb4');d.rectangle((18,5,23,10),outline='#d5cdb4');d.rectangle((3,4,16,11),outline='#767e6f');gui.save(A/'textures/gui/camcorder.png')
lang=A/'lang/en_us.json';data=json.loads(lang.read_text());data['item.the_oldest_house.camcorder']='Production camcorder';data['block.the_oldest_house.ghosts_set']='Barrett home';lang.write_text(json.dumps(data,ensure_ascii=False,indent=2)+'\n')
print(f'Generated {len(K)*4} fixture states, {len(palette)+7+1} home textures, eight fitted cast atlases and camcorder art.')

# Fit the retained original atlases and extended native fixture states after the base mesh pass.
import runpy
runpy.run_path(str(ROOT/'tools/update_ghosts_fixture_models.py'),run_name='__main__')
runpy.run_path(str(ROOT/'tools/update_ghosts_seating_models.py'),run_name='__main__')

runpy.run_path(str(ROOT/'tools/update_ghosts_dining_models.py'),run_name='__main__')
