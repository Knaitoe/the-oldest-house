"""Native meshes and UVs for the retained, generated 4x4 trail atlas. Does not repaint any PNG."""
import argparse
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / 'src/main/resources/assets/the_oldest_house'
ATLAS = 'the_oldest_house:entity/borrasca_trail_atlas'
FACES = ('north', 'south', 'east', 'west', 'up', 'down')
GLYPHS = {
    '1': ('010','110','010','010','010','010','111'),
    '2': ('111','001','001','111','100','100','111'),
    '3': ('111','001','001','111','001','001','111'),
    '4': ('101','101','101','111','001','001','001'),
    'K': ('101','110','100','110','101'), 'I': ('111','010','010','010','111'),
    'M': ('101','111','111','101','101'), 'B': ('110','101','110','101','110'),
    'E': ('111','100','110','100','111'), 'R': ('110','101','110','110','101'),
}


def uv(tile):
    x, y = tile % 4 * 4, tile // 4 * 4
    return [x + .03, y + .03, x + 3.97, y + 3.97]


def cube(box, tile, rotation=None):
    result = {'from': list(box[:3]), 'to': list(box[3:]),
              'faces': {f: {'texture': '#atlas', 'uv': uv(tile)} for f in FACES}}
    if rotation:
        result['rotation'] = rotation
    return result


def model(elements):
    return {'textures': {'atlas': ATLAS, 'particle': ATLAS}, 'elements': elements}


def letters(word, x, y, z, size, tile):
    result = []
    for char in word:
        rows = GLYPHS[char]
        for row, line in enumerate(rows):
            for col, pixel in enumerate(line):
                if pixel == '1':
                    a, b = x + col * size, y + (len(rows) - row - 1) * size
                    result.append(cube((a,b,z,a+size*.88,b+size*.88,z+.08),tile))
        x += 4 * size
    return result


def outputs():
    out = {}
    kinds = {
        'trail': (3, [(0,0,0,16,16,16)]),
        'aged_wood': (2, [(0,0,0,16,16,16)]),
        'tape': (4, [(0,9,7,16,11,9),(1,6,7,2,10,9),(14,8,7,15,13,9)]),
        'polished': (6, [(0,0,0,16,16,16)]),
        'pack': (8, [(3,0,3,13,10,13),(5,10,5,11,12,7),(4,3,2,12,7,3)]),
        'patch': (9, [(1,0,1,15,1,15)]),
        'lock': (11, [(5,2,6,11,9,10),(5,9,7,6,13,9),(10,9,7,11,13,9),(6,12,7,10,13,9)]),
        'lamp': (14, [(0,2,3,16,14,13)]),
        'breaker': (15, [(0,0,0,16,16,16)]),
        'county': (13, [(0,0,0,16,16,16)]),
        'tracks': (3, [(1,.01,1,15,.15,15)]),
    }
    for name, (tile, boxes) in kinds.items():
        out[f'models/block/borrasca_trail_{name}.json'] = model([cube(b,tile) for b in boxes])
    # A see-through mesh made from actual narrow wires, rather than an opaque pane texture.
    wire = [cube((0,0,7.5,.6,16,8.5),5),cube((15.4,0,7.5,16,16,8.5),5)]
    for y in (1,6,11):
        for angle in (-45,45):
            wire.append(cube((1,y,7.75,15,y+.35,8.25),5,{'origin':[8,y+.175,8],'axis':'z','angle':angle,'rescale':False}))
    out['models/block/borrasca_trail_fence.json'] = model(wire)
    for name, number in zip(('one','two','three','four'),'1234'):
        # Base model faces north. The state rotations keep visible digits and native selection aligned.
        digit = letters(number,5.5,2.7,13.8,1.45,12)
        out[f'models/block/borrasca_trail_marker_{name}.json'] = model([cube((0,0,14,16,16,16),2),*digit])
    old = ('timber','steel','rust','paper','notice','carving','drawers','belt','truck')
    new = tuple(kinds) + ('fence','marker_one','marker_two','marker_three','marker_four')
    states = {}
    for name in (*old,*new):
        for facing, angle in (('north',0),('east',90),('south',180),('west',270)):
            entry = {'model': 'the_oldest_house:block/borrasca_' + (name if name in old else 'trail_'+name)}
            if angle:
                entry['y'] = angle
            states[f'kind={name},facing={facing}'] = entry
    out['blockstates/borrasca_fitting.json'] = {'variants': states}
    for stage in range(5):
        bark = cube((0,0,0,16,16,16),0)
        if stage >= 3:
            bark['faces']['south']['uv'] = uv(1)
        out[f'models/block/borrasca_bark_{stage}.json'] = model([bark])
    out['blockstates/borrasca_bark.json'] = {'variants': {f'stage={i}': {'model':f'the_oldest_house:block/borrasca_bark_{i}'} for i in range(5)}}
    display = {'gui': {'rotation':[0,180,0],'translation':[0,0,0],'scale':[1,1,1]},
               'ground': {'rotation':[90,0,0],'translation':[0,1,0],'scale':[.5,.5,.5]},
               'thirdperson_righthand': {'rotation':[0,0,0],'translation':[0,2,0],'scale':[.5,.5,.5]},
               'firstperson_righthand': {'rotation':[0,-30,0],'translation':[0,2,0],'scale':[.8,.8,.8]}}
    compass = model([cube((2,2,7,14,14,9),7),cube((6,14,7,10,16,9),8)])
    patch = model([cube((0,3,7.5,16,13,8.5),9),*letters('KIMBER',1.1,6,7.35,.6,12)])
    flashlight = model([cube((6,0,6,10,12,10),5),cube((5.5,2,5.5,10.5,10,10.5),10),cube((4,10,4,12,16,12),14)])
    for name, asset in (('compass',compass),('name_patch',patch),('flashlight',flashlight)):
        asset['display'] = display
        out[f'models/item/borrasca_{name}.json'] = asset
    atlas_file = ROOT / 'src/main/resources/assets/minecraft/atlases/blocks.json'
    atlas = json.loads(atlas_file.read_text())
    source = {'type':'minecraft:single','resource':ATLAS,'sprite':ATLAS}
    if source not in atlas['sources']:
        atlas['sources'].append(source)
    out['../minecraft/atlases/blocks.json'] = atlas
    lang = json.loads((ASSETS / 'lang/en_us.json').read_text())
    lang.update({'block.the_oldest_house.borrasca_bark':'Carved pine bark',
                 'item.the_oldest_house.borrasca_compass':"Kimber's compass",
                 'item.the_oldest_house.borrasca_name_patch':"Kimber's name patch",
                 'item.the_oldest_house.borrasca_flashlight':'County flashlight',
                 'subtitles.the_oldest_house.borrasca.triple_chime':'Metallic cry from the ridge',
                 'subtitles.the_oldest_house.borrasca.carve':'Knife scrapes wood',
                 'subtitles.the_oldest_house.borrasca.breaker':'Breaker drops',
                 'subtitles.the_oldest_house.borrasca.chain':'Chain rattles'})
    out['lang/en_us.json'] = lang
    sounds = json.loads((ASSETS / 'sounds.json').read_text())
    for name in ('triple_chime','carve','breaker','chain'):
        sounds['borrasca.'+name] = {'subtitle':'subtitles.the_oldest_house.borrasca.'+name,
            'sounds':[{'name':'the_oldest_house:borrasca/'+name, 'attenuation_distance':256 if name=='triple_chime' else 24}]}
    out['sounds.json'] = sounds
    return out


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--check', action='store_true')
    args = parser.parse_args()
    changed = []
    for name, value in outputs().items():
        path = ASSETS / name
        content = json.dumps(value,indent=2,ensure_ascii=False)+'\n'
        if not path.exists() or path.read_text() != content:
            changed.append(str(path.relative_to(ROOT)))
            if not args.check:
                path.parent.mkdir(parents=True,exist_ok=True)
                path.write_text(content)
    if args.check and changed:
        raise SystemExit('Borrasca generated assets differ: '+', '.join(changed))
    print('Borrasca native meshes, block states, item models, atlas and subtitles:',len(outputs()),'definitions;',len(changed),'changed')


if __name__ == '__main__':
    main()
