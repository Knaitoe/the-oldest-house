"""Native meshes and authored 16-pixel material tiles, matching vanilla's pixel scale."""
import argparse
import json
from pathlib import Path
from PIL import Image, ImageDraw

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


def paintings():
    """Draw at native resolution: no resized concept art, filters or antialiasing."""
    tiles = []
    def tile(base):
        im = Image.new('RGBA', (16, 16), base)
        tiles.append(im)
        return ImageDraw.Draw(im)
    # Bark: connected vertical clusters rather than high frequency noise.
    d = tile('#59452f')
    for x, y, height in ((1,0,8),(5,5,11),(9,0,12),(13,7,9)):
        d.rectangle((x,y,x+1,min(15,y+height)),fill='#382f25')
        d.line((x+2,y,x+2,min(15,y+height-2)),fill='#776044')
    d.line((0,11,2,11,2,15),fill='#453825');d.line((11,2,11,5,12,5),fill='#8a7150')
    tiles.append(tiles[0].copy());d=ImageDraw.Draw(tiles[-1])
    d.polygon(((2,4),(13,3),(12,6),(14,8),(12,11),(3,12),(4,9),(2,8)),fill='#c2a777')
    for y,x in ((5,4),(7,3),(9,5),(11,4)):
        d.line((x,y,11,y),fill='#e0c692');d.point((12,y),fill='#977b51')
    d = tile('#79705a')
    for y in (0,7,15):d.line((0,y,15,y),fill='#403f34')
    for x,y in ((1,3),(7,10),(11,4),(3,13)):
        d.line((x,y,x+3,y),fill='#9a8b6b');d.line((x+1,y+1,x+4,y+1),fill='#625e4b')
    d=tile('#995b40')
    for x,y in ((1,3),(6,1),(11,6),(3,11),(10,13)):
        d.rectangle((x,y,x+2,y+1),fill='#ae704d');d.point((x+1,y+2),fill='#7c4937')
    for x,y in ((3,6),(8,9),(13,2)):
        d.line((x,y,x+2,y+2),fill='#706044');d.point((x+3,y+2),fill='#b39764')
    d=tile('#d2b43b')
    d.line((0,1,15,1),fill='#ebd66a');d.line((0,14,15,14),fill='#8d772c')
    for x in range(-16,32,8):d.polygon(((x,12),(x+4,12),(x+12,4),(x+8,4)),fill='#34372e')
    d=tile('#92998d')
    d.line((0,0,15,0),fill='#c1c5b6');d.line((0,1,0,15),fill='#b0b6a9')
    for x,y in ((2,4),(9,2),(7,11),(13,8)):d.line((x,y,x+1,y+1),fill='#6c786f')
    d=tile('#a4aca1')
    d.rectangle((0,0,15,15),outline='#626e66');d.line((1,1,14,1),fill='#d0d4bf')
    d.line((1,2,1,14),fill='#b9c2af');d.line((3,6,11,6),fill='#959f95');d.line((7,11,13,11),fill='#959f95')
    d=tile('#80653c')
    d.rectangle((1,1,14,14),fill='#bd9b53');d.rectangle((3,3,12,12),fill='#d4cfaf')
    d.line((8,4,8,11),fill='#514f42');d.line((4,8,11,8),fill='#6f705c')
    d.polygon(((8,4),(6,8),(8,8)),fill='#a14939');d.polygon(((8,11),(10,8),(8,8)),fill='#596d70')
    for x,y in ((3,3),(12,3),(3,12),(12,12)):d.point((x,y),fill='#514a35')
    d=tile('#476b61')
    d.rectangle((1,1,14,14),outline='#2e4a44');d.line((2,2,13,2),fill='#65867a')
    d.rectangle((3,0,4,15),fill='#8e805b');d.rectangle((11,0,12,15),fill='#8e805b')
    d.rectangle((3,6,4,8),fill='#b3aa82');d.rectangle((11,6,12,8),fill='#b3aa82')
    d=tile('#713d42')
    d.rectangle((1,1,14,14),outline='#b29469')
    for i in range(3,14,3):d.point((i,2),fill='#d1b888');d.point((i,13),fill='#d1b888')
    d.line((3,7,12,7),fill='#854c4c');d.line((3,9,10,9),fill='#854c4c')
    d=tile('#303b38')
    for y in (1,5,9,13):d.line((0,y,15,y),fill='#45504a');d.line((0,y+1,15,y+1),fill='#26302e')
    d=tile('#8e7747')
    d.rectangle((1,1,14,14),outline='#4f4e3b');d.line((2,2,13,2),fill='#bfaa6c')
    d.rectangle((7,6,8,10),fill='#393d32');d.rectangle((6,6,9,7),fill='#393d32')
    d=tile('#c9c3a5')
    d.rectangle((0,0,15,15),outline='#a49b7c');d.line((1,1,14,1),fill='#e0d9b9')
    for y in (5,8,11):d.line((3,y,12,y),fill='#8a8c75')
    d.line((3,3,7,3),fill='#62766c')
    d=tile('#d3d2bd')
    d.rectangle((0,7,15,12),fill='#3e5d71');d.line((0,6,15,6),fill='#72909a')
    d.rectangle((6,8,9,11),fill='#b49b55');d.line((0,15,15,15),fill='#8d9688')
    d=tile('#6d663f')
    d.rectangle((1,1,14,14),fill='#b58f47');d.rectangle((3,3,12,12),fill='#e5c477')
    d.rectangle((5,5,10,10),fill='#f0df9d');d.rectangle((0,0,15,15),outline='#465247')
    d=tile('#76847c')
    d.rectangle((0,0,15,15),outline='#46554f');d.line((1,1,14,1),fill='#a7b2a0')
    d.rectangle((4,3,11,12),fill='#3b4942');d.rectangle((6,5,9,10),fill='#bd794e')
    d.line((6,5,9,5),fill='#d6a073')
    atlas=Image.new('RGBA',(64,64))
    for i,im in enumerate(tiles):atlas.paste(im,((i%4)*16,(i//4)*16))
    assert len(tiles)==16
    return atlas


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
    path = ASSETS / 'textures/entity/borrasca_trail_atlas.png'
    image = paintings()
    if args.check:
        with Image.open(path) as actual:
            assert actual.size == (64,64) and actual.convert('RGBA').tobytes() == image.tobytes(), 'Native trail pixels differ'
    else:
        image.save(path,optimize=True)
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
