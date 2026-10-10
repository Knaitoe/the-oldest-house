#!/usr/bin/env python3
"""Export the imagegen hospital material atlas and author its native ward meshes.

The source atlas is kept in tools/asset_sources. Cropping, nearest-neighbour export
and palette reduction prepare the generated artwork for Minecraft's 32-pixel grid.
Existing approved PNGs are never rewritten. Geometry mirrors InstituteFixtureBlock.
"""
from pathlib import Path
import json
import re
from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
A = ROOT / 'src/main/resources/assets/the_oldest_house'
SOURCE = ROOT / 'tools/asset_sources/whale_hospital_atlas.png'
MATERIALS = ['paint', 'peel', 'damp', 'ivory', 'dado', 'floor_ivory', 'floor_gray', 'ceiling',
             'enamel', 'steel', 'vinyl', 'ceramic', 'cabinet', 'laminate', 'glass', 'diffuser']


def write(path, data):
    p = A / path
    p.parent.mkdir(parents=True, exist_ok=True)
    p.write_text(json.dumps(data, indent=2) + '\n')


def textures():
    atlas = Image.open(SOURCE).convert('RGB')
    w, h = atlas.size
    for i, name in enumerate(MATERIALS):
        x, y = i % 4, i // 4
        tile = atlas.crop((x*w//4+2, y*h//4+2, (x+1)*w//4-2, (y+1)*h//4-2))
        tile = tile.resize((32, 32), Image.Resampling.NEAREST)
        tile = tile.quantize(colors=24, dither=Image.Dither.NONE).convert('RGBA')
        p = A / f'textures/block/ward/{name}.png'
        p.parent.mkdir(parents=True, exist_ok=True)
        tile.save(p)


def cube(box, tex, faces=None):
    return {'from': box[:3], 'to': box[3:], 'faces': {
        face: {'texture': '#' + (faces or {}).get(face, tex)}
        for face in ['north', 'south', 'east', 'west', 'up', 'down']}}


def mesh(boxes, names, extra=None):
    t = {n: f'the_oldest_house:block/ward/{n}' for n in MATERIALS}
    t['particle'] = t[names[0]]
    return {'ambientocclusion': True, 'textures': t,
            'elements': [cube(b, names[min(i, len(names)-1)]) for i, b in enumerate(boxes)] + (extra or [])}


def rotate(model):
    return {f'facing={f}': {'model': 'the_oldest_house:block/' + model, **({'y': y} if y else {})}
            for f, y in [('north', 0), ('east', 90), ('south', 180), ('west', 270)]}


def fixtures():
    code = (ROOT / 'src/main/java/io/github/knaitoe/theoldesthouse/labyrinth/InstituteFixtureBlock.java').read_text()
    shapes = {}
    for name, body in re.findall(r'case (\w+) -> new double\[\]\[\]\{(.*?)\};', code):
        shapes[name.lower()] = [[float(n) for n in b.split(',')] for b in re.findall(r'\{([^{}]+)\}', body)]
    finish = {
        'chair': ['vinyl', 'steel', 'steel', 'steel', 'steel', 'vinyl'],
        'stool': ['vinyl', 'steel'], 'bedside': ['enamel'],
        'desk': ['laminate', 'cabinet', 'enamel'], 'table': ['laminate', 'enamel'],
        'counter': ['cabinet', 'laminate'], 'bench': ['vinyl', 'steel', 'steel', 'steel', 'steel', 'vinyl'],
        'sink': ['enamel', 'ceramic'], 'light': ['enamel', 'diffuser'],
        'bed_head': ['enamel', 'ivory', 'ivory', 'enamel'],
        'bed_foot': ['enamel', 'ivory', 'enamel'],
    }
    kinds = list(MATERIALS[:8]) + list(shapes) + ['shelf']
    variants = {}
    for kind in kinds:
        extra = []
        if kind == 'shelf':
            boxes = [[0,0,0,16,2,16],[0,14,0,16,16,16],[0,2,14,16,14,16],
                     [0,2,0,2,14,14],[14,2,0,16,14,14],[2,7,0,14,8,14]]
            names = ['enamel']
            for y in [2,8]:
                for x in range(3,13,2): extra.append(cube([x,y,5,x+1.7,y+5,13], 'ivory' if x % 3 else 'cabinet'))
        elif kind in shapes:
            boxes, names = shapes[kind], finish[kind]
            if kind in ['bedside', 'desk', 'counter']:
                extra.append(cube([3,9,0.4,7,10,1.1], 'steel'))
            if kind == 'sink': extra.append(cube([4,13,5,12,13.2,12], 'glass'))
        else: boxes, names = [[0,0,0,16,16,16]], [kind]
        model = mesh(boxes, names, extra)
        if kind == 'ceiling': model['elements'][0]['faces']['up']['texture'] = '#floor_gray'
        write(f'models/block/ward_{kind}.json', model)
        for facing, entry in rotate('ward_' + kind).items(): variants[facing + ',kind=' + kind] = entry
    write('blockstates/institute_fixture.json', {'variants': variants})


def cabinet():
    boxes = [[0,0,1,16,16,16]]
    names = ['cabinet']
    for y in [1,6,11]:
        boxes += [[1,y,0.5,15,y+4,1],[5,y+1.4,0,11,y+2.3,0.5],[2,y+2.5,0.2,4,y+3.4,0.5]]
        names += ['enamel','steel','ivory']
    write('models/block/ward_cabinet.json', mesh(boxes,names))
    write('blockstates/institute_cabinet.json', {'variants': rotate('ward_cabinet')})


def notebook():
    book = [[3,0,2,13,0.5,14],[3.5,0.5,2.5,7.8,1.5,13.5],[8.2,0.5,2.5,12.5,1.5,13.5],
            [7.8,0.5,2.5,8.2,1.8,13.5],[12,1.5,3,12.5,1.8,12]]
    write('models/block/ward_notebook.json', mesh(book,['cabinet','ivory','ivory','enamel','steel']))
    table = [[0,12,0,16,14,16],[2,0,2,4,12,4],[12,0,2,14,12,4],[2,0,12,4,12,14],[12,0,12,14,12,14]]
    lifted = [[b[0],b[1]+14,b[2],b[3],b[4]+14,b[5]] for b in book]
    write('models/block/ward_reading_table.json', mesh(table+lifted,['laminate','enamel','enamel','enamel','enamel','cabinet','ivory','ivory','enamel','steel']))
    variants = {}
    for table, model in [('false','ward_notebook'),('true','ward_reading_table')]:
        for f,e in rotate(model).items(): variants[f+',table='+table]=e
    write('blockstates/institute_notebook.json', {'variants': variants})


def native_shapes():
    tex = {'texture':'the_oldest_house:block/ward/enamel'}
    post = {'ambientocclusion':True, 'textures':{'enamel':tex['texture'],'steel':'the_oldest_house:block/ward/steel','particle':tex['texture']},
            'elements':[cube([5,0,5,11,16,11],'enamel'),cube([4.5,14,4.5,11.5,16,11.5],'enamel'),cube([6,13,4.5,7,14,5],'steel')]}
    write('models/block/ward_rail_post.json', post)
    write('models/block/ward_rail_side.json', {'parent':'minecraft:block/fence_side','textures':tex})
    write('blockstates/institute_rail.json', {'multipart':[
        {'apply':{'model':'the_oldest_house:block/ward_rail_post'}},
        *[{'when':{f:'true'},'apply':{'model':'the_oldest_house:block/ward_rail_side',**({'y':y} if y else {}),'uvlock':True}}
          for f,y in [('north',0),('east',90),('south',180),('west',270)]]]})
    for name in ['stairs','inner_stairs','outer_stairs']:
        write(f'models/block/ward_{name}.json',{'parent':'minecraft:block/'+name,'textures':{'bottom':'the_oldest_house:block/ward/ceiling','top':'the_oldest_house:block/ward/floor_gray','side':'the_oldest_house:block/ward/enamel'}})
    # Native stair rotations and hinges come from the existing vanilla-shaped staircase mapping.
    data=json.loads((A/'blockstates/staircase_stairs.json').read_text())
    for entry in data['variants'].values():
        suffix=entry['model'].split('/')[-1]
        entry['model']='the_oldest_house:block/ward_'+('inner_stairs' if 'inner' in suffix else 'outer_stairs' if 'outer' in suffix else 'stairs')
    write('blockstates/institute_stairs.json',data)
    for name,parent in [('slab','slab'),('slab_top','slab_top'),('slab_double','cube_all')]:
        t={'all':'the_oldest_house:block/ward/enamel'} if parent=='cube_all' else {'bottom':'the_oldest_house:block/ward/enamel','top':'the_oldest_house:block/ward/laminate','side':'the_oldest_house:block/ward/enamel'}
        write('models/block/ward_'+name+'.json',{'parent':'minecraft:block/'+parent,'textures':t})
    write('blockstates/institute_slab.json',{'variants':{f'type={k}':{'model':'the_oldest_house:block/ward_'+v} for k,v in [('bottom','slab'),('top','slab_top'),('double','slab_double')]}})
    for part in ['post','side','side_alt','noside','noside_alt']:
        write('models/block/ward_glass_'+part+'.json',{'parent':'minecraft:block/template_glass_pane_'+part,'textures':{'pane':'the_oldest_house:block/ward/glass','edge':'the_oldest_house:block/ward/enamel'}})
    # The familiar native connected-pane geometry, with hospital ribbed glass.
    pane={'multipart':[{'apply':{'model':'the_oldest_house:block/ward_glass_post'}}]}
    for f,y in [('north',0),('east',90),('south',180),('west',270)]:
        for connected in [True,False]:
            pane['multipart'].append({'when':{f:str(connected).lower()},'apply':{'model':'the_oldest_house:block/ward_glass_'+('side' if connected else 'noside'),**({'y':y} if y else {})}})
    write('blockstates/institute_glass.json',pane)
    # Native door meshes retain the exact open/closed/horizontal/upper/lower state semantics.
    for locked in [False,True]:
        name='institute_locked_door' if locked else 'institute_door'
        variants={}
        for f,y in [('east',0),('south',90),('west',180),('north',270)]:
            for half in ['lower','upper']:
                for hinge in ['left','right']:
                    for opened in [False,True]:
                        model=f'{"ward_locked_door" if locked else "ward_door"}_{half}_{hinge}'+('_open' if opened else '')
                        parent=f'minecraft:block/door_{"bottom" if half=="lower" else "top"}_{hinge}'+('_open' if opened else '')
                        write('models/block/'+model+'.json',{'parent':parent,'textures':{'bottom':'the_oldest_house:block/ward/cabinet' if locked else 'the_oldest_house:block/ward/enamel','top':'the_oldest_house:block/ward/glass'}})
                        variants[f'facing={f},half={half},hinge={hinge},open={str(opened).lower()}']={'model':'the_oldest_house:block/'+model,'y':(y+(90 if hinge=='left' else 270) if opened else y)%360}
        write('blockstates/'+name+'.json',{'variants':variants})
    # Cubby fronts and all approved original PNGs remain exact; exposed sides become painted cabinetry.
    for n in range(1,13):
        p=A/f'models/block/pigeonhole_{n}.json'
        model=json.loads(p.read_text());model['textures']['side']='the_oldest_house:block/ward/cabinet';model['textures']['top']='the_oldest_house:block/ward/laminate';write(f'models/block/pigeonhole_{n}.json',model)


if __name__ == '__main__':
    textures();fixtures();cabinet();notebook();native_shapes()
    path=A/'lang/en_us.json';lang=json.loads(path.read_text())
    lang.update({f'block.the_oldest_house.institute_{k}':v for k,v in {'fixture':'Ward fitting','cabinet':'Ward cabinet','notebook':'Bedside notebook','rail':'Enamel ward rail','stairs':'Institute stairs','slab':'Institute shelf','glass':'Ribbed hospital glass','door':'Painted ward door','locked_door':'Locked ward door'}.items()})
    path.write_text(json.dumps(lang,indent=2,ensure_ascii=False)+'\n')
    tag=ROOT/'src/main/resources/data/minecraft/tags/block/fences.json';tag.parent.mkdir(parents=True,exist_ok=True)
    old=json.loads(tag.read_text()) if tag.exists() else {'replace':False,'values':[]}
    if 'the_oldest_house:institute_rail' not in old['values']:old['values'].append('the_oldest_house:institute_rail')
    tag.write_text(json.dumps(old,indent=2)+'\n')
    print('Exported 16 hospital materials, 20 ward fittings and native cabinet/notebook/rail/stair/door/glass meshes.')
