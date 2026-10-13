"""Native pixel materials and fitted meshes for the lake-house continuation. Prior art is never rewritten."""
from pathlib import Path
import json
import random
from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parents[1]
A = ROOT / 'src/main/resources/assets/the_oldest_house'


def write(path, value):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(value, indent=2) + '\n')


def material(name, colors, pattern):
    rng = random.Random(name)
    im = Image.new('RGBA', (16, 16))
    px = im.load()
    for y in range(16):
        for x in range(16):
            c = colors[rng.randrange(len(colors))]
            if pattern == 'wood':
                c = colors[(x // 4 + (y // 7 if x % 4 == 0 else 0)) % len(colors)]
            elif pattern == 'tile':
                c = colors[0 if x % 8 == 0 or y % 8 == 0 else 1 + (x // 8 + y // 8) % (len(colors) - 1)]
            elif pattern == 'cloth':
                c = colors[(x + 2 * y) % len(colors)]
            elif pattern == 'wall':
                c = colors[0 if y in (0, 15) else 1 + ((x // 4 + y // 4) % (len(colors) - 1))]
            px[x, y] = (*c, 255)
    p = A / 'textures/block' / (name + '.png')
    p.parent.mkdir(parents=True, exist_ok=True)
    im.save(p)


def box(start, end, texture, uv=None):
    face = {'texture': '#' + texture}
    if uv:
        face['uv'] = uv
    return {'from': start, 'to': end, 'faces': {d: dict(face) for d in ('north', 'south', 'east', 'west', 'up', 'down')}}


def mesh(kind, lit=False):
    wood, metal, glass, cloth = 'wood', 'brass', 'glass', 'cloth'
    e = []
    if kind in ('table', 'island', 'pool_table'):
        top = 'marble' if kind == 'island' else 'felt' if kind == 'pool_table' else wood
        e.append(box([0, 11, 0], [16, 13, 16], top))
        for x in (1, 12):
            for z in (1, 12):
                e.append(box([x, 0, z], [x + 3, 11, z + 3], wood))
        if kind == 'pool_table':
            for x, z in ((0, 0), (0, 15), (15, 0), (15, 15)):
                e.append(box([x, 12, z], [x + 1, 14, z + 1], wood))
            for x, z in ((5, 6), (7, 6), (6, 8), (7, 9), (5, 9)):
                e.append(box([x, 13, z], [x + 1, 14, z + 1], 'ball'))
            if not lit:
                e.append(box([6,13,11],[7,14,12],'linen'))
        if kind == 'island':
            z=-2 if lit else 0
            e += [box([3,5,z],[13,8,z+1],wood),box([7,6,z-1],[9,7,z],metal)]
    elif kind in ('chair', 'sofa', 'sauna_bench', 'bed'):
        cushion = 'cedar' if kind == 'sauna_bench' else cloth
        e += [box([1, 6, 1], [15, 9, 15], cushion), box([1, 0, 1], [3, 6, 3], wood), box([13, 0, 1], [15, 6, 3], wood), box([1, 0, 13], [3, 6, 15], wood), box([13, 0, 13], [15, 6, 15], wood)]
        if kind != 'bed':
            e.append(box([1, 9, 13], [15, 16, 15], wood if kind == 'chair' else cushion))
        else:
            e.append(box([2, 9, 10], [14, 11, 14], 'linen'))
            if lit:
                e[0]['to'][1]=8.5
    elif kind in ('key_bowl', 'ash_tray'):
        e.append(box([4, -3, 4], [12, -1, 12], 'porcelain'))
        for lo, hi in (([4, -1, 4], [5, 1, 12]), ([11, -1, 4], [12, 1, 12]), ([5, -1, 4], [11, 1, 5]), ([5, -1, 11], [11, 1, 12])):
            e.append(box(lo, hi, 'porcelain'))
        e.append(box([6, -1, 6], [10, 0, 10], metal if kind == 'key_bowl' else 'ash'))
        if lit and kind == 'key_bowl':
            e[-1]['from'][0]=8;e[-1]['to'][0]=12
    elif kind == 'coatrack':
        e += [box([3, 0, 3], [13, 2, 13], wood), box([7, 2, 7], [9, 15, 9], wood), box([3, 13, 7], [13, 15, 9], wood), box([7, 13, 3], [9, 15, 13], wood)]
        if lit:
            for hook in e[-2:]:hook['rotation']={'origin':[8,14,8],'axis':'y','angle':45}
    elif kind in ('bookcase', 'wardrobe'):
        e += [box([0, 0, 10], [16, 16, 16], wood), box([1, 1, 9], [15, 15, 10], 'books')]
        for y in (0, 7, 15):
            e.append(box([0, y, 7], [16, y + 1, 16], wood))
        if kind == 'wardrobe':
            e += [box([1, 1, 7], [7, 15, 8], wood), box([9, 1, 7], [15, 15, 8], wood), box([6, 7, 6], [10, 9, 7], metal)]
            if lit:
                e[-3]['rotation']={'origin':[1,8,7],'axis':'y','angle':45};e[-2]['rotation']={'origin':[15,8,7],'axis':'y','angle':-45}
        else:
            book=box([3,8,5],[13,9,9],'linen')
            if lit:book['rotation']={'origin':[8,8,9],'axis':'x','angle':-45}
            e.append(book)
    elif kind == 'beaver':
        e += [box([6, -3, 6], [10, 4, 10], 'porcelain'), box([5, 1, 4], [11, 6, 8], 'porcelain'), box([6, 5, 4], [7, 7, 6], wood), box([9, 5, 4], [10, 7, 6], wood), box([7, -3, 10], [9, 0, 14], wood), box([6, 3, 3], [7, 4, 4], 'ash'), box([9, 3, 3], [10, 4, 4], 'ash')]
        if lit:
            for part in e:part['rotation']={'origin':[8,0,7],'axis':'x','angle':-22.5}
    elif kind == 'painting':
        e += [box([1, 1, 14], [15, 15, 16], wood), box([2, 2, 13.8], [14, 14, 14], 'face' if lit else 'still_life', [0, 0, 16, 16])]
    elif kind in ('lamp', 'bulb'):
        if kind == 'lamp':
            e += [box([6, 4, 14], [10, 13, 16], metal), box([7, 8, 8], [9, 10, 16], metal), box([5, 7, 6], [11, 13, 12], glass), box([4, 12, 5], [12, 14, 13], metal)]
        else:
            e += [box([7, 10, 7], [9, 16, 9], metal), box([5, 3, 5], [11, 10, 11], glass), box([6, 1, 6], [10, 3, 10], glass)]
    elif kind in ('sauna_door', 'half_door'):
        width=16 if kind=='sauna_door' else 7
        e = [box([0, 0, 14], [width, 32, 16], 'cedar' if kind == 'sauna_door' else wood), box([width-2, 12, 13], [width, 14, 14], metal)]
        if lit:
            for part in e:part['rotation']={'origin':[0,16,14],'axis':'y','angle':-45}
    elif kind == 'red_runner':
        e = [box([2, 0, 0], [14, 1, 16], 'runner')]
    elif kind == 'candle':
        e = [box([4, 0, 4], [12, 2, 12], metal), box([6, 2, 6], [10, 10, 10], 'linen'), box([7, 10, 7], [9, 13, 9], 'glow')]
    elif kind == 'hearth':
        e = [box([0, 0, 10], [16, 16, 16], 'stone'), box([2, 0, 2], [14, 2, 13], 'stone'), box([1, 2, 4], [4, 14, 14], 'stone'), box([12, 2, 4], [15, 14, 14], 'stone'), box([1, 14, 4], [15, 16, 14], 'stone'), box([5, 2, 8], [11, 5, 12], 'glow')]
    elif kind == 'dress':
        e = [box([5, 3, 5], [11, 12, 11], 'black_dress'), box([3, 0, 3], [13, 4, 13], 'black_dress'), box([3, 10, 5], [5, 14, 11], 'black_dress'), box([11, 10, 5], [13, 14, 11], 'black_dress')]
    textures = {t: 'the_oldest_house:block/lake_' + t for t in ('wood', 'brass', 'glass', 'cloth', 'marble', 'felt', 'ball', 'cedar', 'linen', 'porcelain', 'ash', 'books', 'face', 'still_life', 'runner', 'glow', 'stone', 'black_dress')}
    textures['particle'] = textures['wood']
    result = {'ambientocclusion': False, 'textures': textures, 'elements': e}
    if lit and kind in ('lamp', 'bulb'):
        result['textures']['glass'] = textures['glow']
    return result


def skin(name, wax=False):
    # Finished cast atlases are painted assets; rebuilding fittings must preserve them.
    target = A/'textures/entity'/('literary_'+name+'.png')
    if target.exists():
        return
    im = Image.new('RGBA', (128, 64), (0, 0, 0, 0))
    d = ImageDraw.Draw(im)
    palettes = {'skin': (216, 197, 148) if wax else (237, 217, 206), 'hair': (44, 31, 25), 'swimsuit': (36, 65, 93)}
    def cube(u, v, w, h, depth, color):
        for x0, y0, x1, y1, shade in [(u+depth,v,u+depth+w,v+depth,1.1),(u+depth+w,v,u+depth+2*w,v+depth,.75),(u,v+depth,u+depth,v+depth+h,.85),(u+depth,v+depth,u+depth+w,v+depth+h,1),(u+depth+w,v+depth,u+2*depth+w,v+depth+h,.9),(u+2*depth+w,v+depth,u+2*depth+2*w,v+depth+h,.82)]:
            d.rectangle((int(x0),int(y0),int(x1)-1,int(y1)-1), fill=tuple(min(255,int(c*shade)) for c in color)+(255,))
    cube(0,0,8,8,8,palettes['skin'])
    cube(16,16,8,12,4,(19,22,21) if wax else palettes['swimsuit'])
    cube(40,16,4,12,4,palettes['skin'])
    cube(0,16,4,12,4,palettes['skin'] if wax else palettes['skin'])
    if wax:
        cube(96,28,10,14,6,(18,20,20))
        # Wax face deliberately contains no eyes, mouth, nose or human expression.
        d.rectangle((8,8,15,15),fill=palettes['skin']+(255,))
    else:
        cube(32,0,8,8,8,palettes['hair'])
        # Paint only the hairline and crown on the overlay; leave the actual face unobstructed.
        d.rectangle((40,11,47,15),fill=(0,0,0,0));d.rectangle((40,8,47,10),fill=(63,43,31,255))
        for x in (9,14):
            d.point((x,11),fill=(23,27,25,255));d.point((x,10),fill=(53,33,24,255))
        d.rectangle((11,14,12,14),fill=(100,57,46,255))
        d.rectangle((20,20,27,21),fill=palettes['skin']+(255,));d.rectangle((22,20,25,23),fill=palettes['skin']+(255,))
        # A navy two-piece: an opaque swim top, a bare midriff, and shorts-cut bottoms.
        for x0,x1 in ((16,19),(20,27),(28,31),(32,39)):
            d.rectangle((x0,25,x1,28),fill=palettes['skin']+(255,))
        d.rectangle((0,20,15,22),fill=palettes['swimsuit']+(255,));d.rectangle((4,21,7,23),fill=(44,77,103,255))
        d.line((20,22,20,24),fill=(62,101,126,255));d.line((27,22,27,24),fill=(21,47,72,255))
        d.rectangle((44,28,47,31),fill=palettes['skin']+(255,))
        d.rectangle((4,31,7,31),fill=(220,197,185,255))
    target.parent.mkdir(parents=True,exist_ok=True);im.resize((512,256),Image.Resampling.NEAREST).save(target)


def main():
    palette = {
        'wood': ([(55,45,34),(74,57,39),(82,64,45),(65,51,36)],'wood'),
        'brass': ([(87,70,31),(166,139,61),(188,158,79)],'tile'),
        'glass': ([(54,77,75),(66,93,88),(45,66,64)],'tile'),
        'cloth': ([(61,75,64),(65,79,68),(58,71,60)],'cloth'),
        'marble': ([(83,91,86),(181,186,175),(193,196,187)],'tile'),
        'felt': ([(24,59,45),(29,65,48),(33,72,51)],'cloth'),
        'ball': ([(179,152,47),(193,174,92),(220,207,153)],'cloth'),
        'cedar': ([(118,74,42),(139,88,49),(151,101,57)],'wood'),
        'linen': ([(185,178,151),(202,193,168),(196,186,158)],'cloth'),
        'porcelain': ([(176,190,181),(219,225,207),(203,214,196)],'tile'),
        'ash': ([(33,35,33),(53,55,49),(71,72,63)],'cloth'),
        'books': ([(41,39,31),(98,56,41),(54,78,69),(129,117,87)],'wood'),
        'runner': ([(79,21,25),(112,34,37),(126,42,42)],'cloth'),
        'glow': ([(204,165,72),(243,220,147),(234,199,105)],'tile'),
        'stone': ([(45,56,52),(99,119,101),(112,130,108)],'tile'),
        'black_dress': ([(16,21,22),(23,29,29),(29,33,33)],'cloth'),
        'house_floor': ([(70,57,43),(96,72,49),(82,62,44)],'wood'),
        'house_wall': ([(40,57,51),(126,130,93),(114,123,88),(118,126,92)],'wall'),
        'house_pool_stone': ([(50,72,57),(106,130,96),(141,154,110)],'tile'),
        'house_cedar': ([(107,66,37),(133,81,44),(149,98,52)],'wood'),
    }
    for name,(colors,pattern) in palette.items():material('lake_'+name,colors,pattern)
    for face in (False,True):
        im=Image.new('RGBA',(16,16),(47,69,57,255));d=ImageDraw.Draw(im)
        d.rectangle((0,12,15,15),fill=(57,43,29,255));d.ellipse((2,5,7,12),fill=(148,103,42,255));d.rectangle((9,7,13,12),fill=(98,122,77,255))
        if face:
            d.ellipse((3,1,12,13),fill=(184,165,113,255));d.rectangle((5,5,6,6),fill=(32,37,28,255));d.rectangle((10,5,11,6),fill=(32,37,28,255));d.line((7,11,10,11),fill=(62,49,32,255))
        im.save(A/'textures/block'/('lake_face.png' if face else 'lake_still_life.png'))
    names=['coatrack','key_bowl','bulb','table','chair','ash_tray','bookcase','pool_table','sofa','painting','island','beaver','red_runner','wardrobe','bed','sauna_bench','sauna_door','half_door','lamp','candle','hearth','dress']
    variants={}
    for kind in names:
        for lit in (False,True):
            model=f'lake_house_{kind}'+('_lit' if lit else '')
            write(A/'models/block'/(model+'.json'),mesh(kind,lit))
            for direction,angle in [('north',0),('east',90),('south',180),('west',270)]:
                for wet in ('true','false'):
                    variants[f'kind={kind},facing={direction},lit={str(lit).lower()},waterlogged={wet}']={'model':'the_oldest_house:block/'+model,'y':angle,'uvlock':True}
    write(A/'blockstates/lake_house_fitting.json',{'variants':variants})
    for name in ('floor','wall','pool_stone','cedar'):
        write(A/'blockstates'/f'lake_house_{name}.json',{'variants':{'':{'model':f'the_oldest_house:block/lake_house_{name}'}}})
        write(A/'models/block'/f'lake_house_{name}.json',{'parent':'minecraft:block/cube_all','textures':{'all':f'the_oldest_house:block/lake_house_{name}'}})
    stairs={}
    for shape in ('straight','inner_left','inner_right','outer_left','outer_right'):
        second=box([0,8,0],[16,16,8],'runner')
        elements=[box([0,0,0],[16,8,16],'wood'),second]
        if shape.startswith('inner'):
            elements.append(box([0 if shape.endswith('left') else 8,8,8],[8 if shape.endswith('left') else 16,16,16],'runner'))
        elif shape.startswith('outer'):
            second['from'][0]=0 if shape.endswith('left') else 8;second['to'][0]=8 if shape.endswith('left') else 16
        model='lake_house_red_stair_'+shape
        write(A/'models/block'/(model+'.json'),{'textures':{'wood':'the_oldest_house:block/lake_wood','runner':'the_oldest_house:block/lake_runner','particle':'the_oldest_house:block/lake_wood'},'elements':elements})
        for facing,angle in [('north',0),('east',90),('south',180),('west',270)]:
            for half in ('bottom','top'):
                for wet in ('true','false'):
                    stairs[f'facing={facing},half={half},shape={shape},waterlogged={wet}']={'model':'the_oldest_house:block/'+model,'y':angle if half=='bottom' else (angle+180)%360,'x':0 if half=='bottom' else 180,'uvlock':True}
    write(A/'blockstates/lake_house_red_stair.json',{'variants':stairs})
    home=mesh('candle',True)
    home['elements']=[box([4,1,4],[12,11,12],'glass'),box([3,0,3],[13,2,13],'brass'),box([3,11,3],[13,13,13],'brass'),box([6,13,7],[10,16,9],'brass')]
    home['textures']['glass']='the_oldest_house:block/lake_glow'
    write(A/'models/block/lake_house_home_light.json',home)
    write(A/'blockstates/lake_house_home_light.json',{'variants':{'':{'model':'the_oldest_house:block/lake_house_home_light'}}})
    write(A/'models/item/lake_house_light.json',{'parent':'the_oldest_house:block/lake_house_home_light'})
    write(A/'models/item/lake_gold_helmet.json',{'parent':'the_oldest_house:item/lake_house_helmet'})
    write(A/'models/item/lake_found_flashlight.json',{'parent':'the_oldest_house:item/lake_house_flashlight'})
    write(A/'particles/lake_house_bubble.json',{'textures':['minecraft:bubble']})
    skin('lake_tess');skin('lake_wax',True)
    lang_path=A/'lang/en_us.json';lang=json.loads(lang_path.read_text())
    lang.update({'block.the_oldest_house.lake_house_fitting':'Furniture under the lake','block.the_oldest_house.lake_house_floor':'Lake-house floor','block.the_oldest_house.lake_house_wall':'Lake-house wallpaper','block.the_oldest_house.lake_house_pool_stone':'Lake-house pool stone','block.the_oldest_house.lake_house_cedar':'Lake-house cedar','block.the_oldest_house.lake_house_home_light':'The house light','item.the_oldest_house.lake_house_light':'The house light','item.the_oldest_house.lake_gold_helmet':'The gold helmet','item.the_oldest_house.lake_found_flashlight':'The flashlight that found you','entity.the_oldest_house.lake_house_dress':'A dress in the water','entity.the_oldest_house.lake_house_seat':'A dry chair'})
    write(lang_path,lang)
    print(f'Wrote {len(variants)} fitted states, two original cast skins and the bound artifact models.')


if __name__=='__main__':main()
