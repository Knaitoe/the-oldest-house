"""The Silo: authored native pixel tiles and identical render/collision timber cuboids."""
import argparse
import json
from pathlib import Path
from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / 'src/main/resources/assets/the_oldest_house'
KINDS = ('door_low', 'door_high', 'table', 'joist', 'swing')


def paintings():
    out = {}
    im = Image.new('RGBA', (16, 16), '#85877b'); d = ImageDraw.Draw(im)
    for x in (1, 5, 9, 13):
        d.line((x, 0, x, 15), fill='#a0a295'); d.line((x+1, 0, x+1, 15), fill='#686f65')
    d.line((0, 15, 15, 15), fill='#525e57')
    for x, y in ((3, 12), (11, 3), (12, 4)):
        d.line((x, y, x+1, y), fill='#806247')
    out['tin'] = im
    im = Image.new('RGBA', (16, 16), '#414b48'); d = ImageDraw.Draw(im)
    d.line((0, 0, 15, 0), fill='#65716a'); d.line((0, 15, 15, 15), fill='#2f3937')
    for x, y in ((2, 2), (13, 13)):
        d.rectangle((x, y, x+1, y+1), fill='#788079')
    d.line((9, 4, 9, 8), fill='#685542'); out['steel'] = im
    im = Image.new('RGBA', (16, 16), '#6f9198'); d = ImageDraw.Draw(im)
    d.rectangle((1, 5, 6, 6), fill='#c0c6ac'); d.rectangle((3, 4, 5, 5), fill='#c0c6ac')
    d.rectangle((10, 12, 15, 13), fill='#a8b9a6'); d.rectangle((12, 11, 14, 12), fill='#c0c6ac')
    d.line((0, 15, 15, 15), fill='#596f73'); d.line((8, 1, 8, 4), fill='#85877b'); out['sky'] = im
    im = Image.new('RGBA', (16, 16), '#a1803d'); d = ImageDraw.Draw(im)
    colors = ('#c39c4d', '#d7b66b', '#b18a3f', '#8a6a34')
    for row, y in enumerate(range(-1, 17, 3)):
        for col, x in enumerate(range(-2, 17, 4)):
            x += (row % 2)*2
            d.rectangle((x, y, x+2, y+1), fill=colors[(row+col*3) % 4])
            d.point((x, y+2), fill='#735731')
    out['corn'] = im
    im = Image.new('RGBA', (16, 16), '#765a40'); d = ImageDraw.Draw(im)
    for y, c in ((0, '#4f4535'), (1, '#9a7950'), (7, '#5c4936'), (15, '#4f4535')):
        d.line((0, y, 15, y), fill=c)
    for x, y in ((2, 4), (8, 11), (12, 3)):
        d.line((x, y, x+3, y), fill='#8a6846')
    d.line((10, 7, 10, 13), fill='#4f4535'); out['wood'] = im
    im = Image.new('RGBA', (16, 16), '#b9b69e'); d = ImageDraw.Draw(im)
    for x in (2, 9):
        d.rectangle((x, 2, x+4, 13), outline='#8c8c78'); d.line((x+1, 3, x+3, 3), fill='#d0cbb0')
    d.rectangle((14, 7, 15, 8), fill='#4f594f'); d.line((4, 14, 8, 14), fill='#765a40'); out['door'] = im
    im = Image.new('RGBA', (16, 16), '#b4a68b'); d = ImageDraw.Draw(im)
    d.line((0, 0, 15, 0), fill='#d0c5a7'); d.line((0, 15, 15, 15), fill='#766750')
    for xy in ((3, 5, 7, 5), (9, 12, 13, 12), (6, 10, 7, 9)):
        d.line(xy, fill='#978b73')
    out['table'] = im
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0)); d = ImageDraw.Draw(im)
    for x in (2, 12):
        d.rectangle((x, 0, x+1, 15), fill='#84664b'); d.line((x, 0, x, 15), fill='#b09265')
    for y in (1, 5, 9, 13):
        d.rectangle((2, y, 13, y+1), fill='#84664b'); d.line((3, y, 12, y), fill='#a18a62')
    out['ladder'] = im
    im = Image.new('RGBA', (16, 16), '#c6bd99'); d = ImageDraw.Draw(im)
    d.rectangle((0, 0, 15, 15), outline='#a79c7b')
    for y, right in ((3, 12), (5, 13), (7, 10), (9, 12), (11, 7)):
        d.line((3, y, right, y), fill='#655f50')
    out['paper'] = im
    return out


def boxes(kind, strain):
    """Model pixels; all slopes are actual stepped native surfaces, including the broken opening."""
    top = 15 if kind == 'table' else 16 if kind == 'door_high' else 8 if kind == 'swing' else 12
    lo, hi = (5, 11) if kind == 'joist' else (1, 15)
    surface = 'table' if kind == 'table' else 'door' if kind.startswith('door') else 'wood'
    out = []
    for x in range(0, 16, 4):
        if strain == 4 and x in (4, 8):
            continue
        sag = (0, .5, 1.5, 3.5, 5)[strain] * (1 if x in (4, 8) else .25)
        tilt = (0, 0, .5, 1.5, 2)[strain] * (x/4)
        y = max(1, top-sag-tilt)
        left, right = ((0, 1.5) if x == 0 else (14.5, 16)) if strain == 4 else (x, x+3.875)
        out.append(([left, y-1.5, lo], [right, y, hi], surface))
    if kind == 'table':
        for x in (1, 13):
            for z in (1, 13):
                out.append(([x, 0, z], [x+1.5, 12.5, z+1.5], 'wood'))
    if kind == 'swing':
        out.extend([([0, 8, 0], [1.5, 16, 2], 'steel'), ([14.5, 8, 0], [16, 16, 2], 'steel')])
        out.append(([1.5, 11, 0], [14.5, 15, 1.5], 'wood'))
    return out


def model(cuboids):
    textures = {k: f'the_oldest_house:block/silo/{k}' for k in paintings()}
    textures['particle'] = textures['wood']
    return {'textures': textures, 'elements': [
        {'from': a, 'to': b, 'faces': {face: {'texture': '#'+material} for face in ('down', 'up', 'north', 'south', 'west', 'east')}}
        for a, b, material in cuboids]}


def outputs():
    out = {}
    def js(path, obj): out[ASSETS/path] = json.dumps(obj, separators=(',', ':'))+'\n'
    for name in ('tin', 'steel', 'sky', 'corn'):
        js(Path('models/block')/f'silo_{name}.json', {'parent': 'minecraft:block/cube_all', 'textures': {'all': f'the_oldest_house:block/silo/{name}'}})
        js(Path('blockstates')/f'silo_{name}.json', {'variants': {'': {'model': f'the_oldest_house:block/silo_{name}'}}})
    variants = {}
    for i, kind in enumerate(KINDS):
        for strain in range(5):
            name=f'silo/{kind}_{strain}'
            js(Path('models/block')/(name+'.json'), model(boxes(kind, strain)))
            for face, turn in (('north', 0), ('east', 90), ('south', 180), ('west', 270)):
                variants[f'kind={kind},strain={strain},facing={face}']={'model': 'the_oldest_house:block/'+name, 'y': turn}
    js(Path('blockstates/silo_timber.json'), {'variants': variants})
    js(Path('models/block/silo_ladder.json'), {'parent': 'minecraft:block/ladder', 'textures': {'texture': 'the_oldest_house:block/silo/ladder'}})
    js(Path('blockstates/silo_ladder.json'), {'variants': {f'facing={f}': {'model': 'the_oldest_house:block/silo_ladder', 'y': y} for f, y in (('north',0),('east',90),('south',180),('west',270))}})
    for shape in ('bottom', 'top', 'open'):
        js(Path('models/block')/f'silo_hatch_{shape}.json', {'parent': 'minecraft:block/template_orientable_trapdoor_'+shape, 'textures': {'texture': 'the_oldest_house:block/silo/tin'}})
    hatch={}
    for f, y in (('north',0),('east',90),('south',180),('west',270)):
        for half in ('bottom','top'):
            for opened in (False, True):
                hatch[f'facing={f},half={half},open={str(opened).lower()}']={'model': f'the_oldest_house:block/silo_hatch_{"open" if opened else half}', 'y': y}
    js(Path('blockstates/silo_hatch.json'), {'variants': hatch})
    js(Path('models/block/silo_paper.json'), model([([2,0,2],[14,.5,14],'paper')]))
    js(Path('models/block/silo_paper_empty.json'), {'elements': [],'textures':{'particle':'the_oldest_house:block/silo/wood'}})
    js(Path('blockstates/silo_paper.json'), {'variants': {'present=true':{'model':'the_oldest_house:block/silo_paper'},'present=false':{'model':'the_oldest_house:block/silo_paper_empty'}}})
    source='package io.github.knaitoe.theoldesthouse.labyrinth;\n\n/** Generated with the production native block models by tools/draw_silo.py. */\nfinal class SiloTimberShapes {\n    private SiloTimberShapes(){}\n    static double[][] boxes(int kind,int strain){return switch(kind*5+strain){\n'
    for i, kind in enumerate(KINDS):
        for strain in range(5):
            coords=','.join('{'+','.join(str(v) for v in (*a,*b))+'}' for a,b,_ in boxes(kind,strain))
            source+=f'        case {i*5+strain}->new double[][]{{{coords}}};\n'
    source+='        default->throw new IllegalArgumentException("Silo timber shape");\n    };}\n}\n'
    out[ROOT/'src/main/java/io/github/knaitoe/theoldesthouse/labyrinth/SiloTimberShapes.java']=source
    return out


def main():
    p=argparse.ArgumentParser(description=__doc__);p.add_argument('--check',action='store_true');args=p.parse_args()
    for name, im in paintings().items():
        path=ASSETS/'textures/block/silo'/f'{name}.png'
        assert im.size==(16,16) and len(im.getcolors(256))<=7
        if args.check:
            assert path.is_file() and Image.open(path).convert('RGBA').tobytes()==im.tobytes(),f'Stale silo tile {name}'
        else:
            path.parent.mkdir(parents=True,exist_ok=True);im.save(path)
    for path, text in outputs().items():
        if args.check:
            assert path.is_file() and path.read_text()==text,f'Stale silo geometry {path}'
        else:
            path.parent.mkdir(parents=True,exist_ok=True);path.write_text(text)
    print('Nine authored 16px tiles; 25 native timber states with matching render/collision cuboids; ladder, hatch and finite paper mounts.')


if __name__=='__main__':main()
