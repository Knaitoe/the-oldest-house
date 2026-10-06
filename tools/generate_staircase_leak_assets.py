"""Exact native prop meshes and five original quiet cues; wallpaper is imagegen art."""
from pathlib import Path
import json, math, random, struct, subprocess, tempfile, wave

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / 'src/main/resources/assets/the_oldest_house'
MOD = 'the_oldest_house'

def write(path, value):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(value, indent=2) + '\n')

def mesh(parts, textures=None, item=False):
    textures = textures or {}
    ids, elements = {}, []
    for lo, hi, texture in parts:
        key = ids.setdefault(texture, 't' + str(len(ids)))
        elements.append({'from': lo, 'to': hi, 'faces': {
            face: {'uv': [0, 0, 16, 16], 'texture': '#' + key}
            for face in ('north', 'south', 'east', 'west', 'up', 'down')}})
    textures.update({key: tex for tex, key in ids.items()})
    textures['particle'] = next(iter(ids), 'minecraft:block/oak_planks')
    result = {'ambientocclusion': False, 'textures': textures, 'elements': elements}
    if item:
        result['display'] = {
            'gui': {'rotation': [0, 0, 0], 'translation': [0, 0, 0], 'scale': [1, 1, 1]},
            'thirdperson_righthand': {'rotation': [0, 0, 0], 'translation': [0, 2, 0], 'scale': [.7, .7, .7]},
            'firstperson_righthand': {'rotation': [0, 0, 0], 'translation': [0, 1, 0], 'scale': [.6, .6, .6]}}
    return result

def box(x0, y0, z0, x1, y1, z1, material):
    return ([x0, y0, z0], [x1, y1, z1], 'minecraft:block/' + material)

def cup(color, hung=False):
    y, z = (2, 1) if hung else (0, 6)
    a = [box(4, y, z, 10, y+1, z+6, color)]
    a += [box(4, y+1, z, 5, y+7, z+6, color), box(9, y+1, z, 10, y+7, z+6, color),
          box(5, y+1, z, 9, y+7, z+1, color), box(5, y+1, z+5, 9, y+7, z+6, color),
          box(10, y+2, z+2, 13, y+3, z+4, color), box(10, y+5, z+2, 13, y+6, z+4, color),
          box(12, y+3, z+2, 13, y+5, z+4, color)]
    if hung: a += hook()
    return a

def hook():
    return [box(6, 4, 0, 9, 7, 1, 'spruce_planks'), box(7, 4, 1, 8, 5, 4, 'iron_block'), box(7, 5, 3, 8, 7, 4, 'iron_block')]

def sink(full=False, clean=False):
    a = [box(1, 0, 1, 15, 1, 15, 'quartz_block_side'), box(1, 1, 1, 2, 4, 15, 'quartz_block_side'),
         box(14, 1, 1, 15, 4, 15, 'quartz_block_side'), box(2, 1, 1, 14, 4, 2, 'quartz_block_side'),
         box(2, 1, 14, 14, 4, 15, 'quartz_block_side'), box(10, 4, 2, 11, 9, 3, 'iron_block'),
         box(8, 8, 2, 11, 9, 6, 'iron_block'), box(8, 7, 5, 9, 8, 6, 'iron_block')]
    if full: a += [box(2, 2.5, 2, 14, 2.6, 14, 'light_blue_stained_glass')]
    elif not clean: a += [box(5, 1, 8, 7, 1.1, 10, 'brown_wool')]
    return a

def geranium(plate=False):
    a = [box(5, .5, 5, 11, 6, 11, 'terracotta'), box(4, 5, 4, 12, 7, 12, 'terracotta'),
         box(5, 7, 5, 11, 7.1, 11, 'dirt'), box(7, 7, 7, 8, 13, 8, 'green_wool'),
         box(4, 10, 6, 11, 11, 9, 'green_wool'), box(6, 11, 4, 9, 12, 11, 'green_wool'),
         box(5, 13, 6, 9, 15, 10, 'red_wool')]
    if plate: a += [box(3, 0, 3, 13, .5, 13, 'white_concrete'), box(9, .5, 10, 12, .6, 11, 'gray_concrete')]
    return a

def cloth(color, y=0):
    return [box(2, y, 4, 14, y+1, 12, color), box(3, y+1, 5, 13, y+2, 11, color)]

def drawer(opened=False):
    a = [box(1, 0, 1, 15, 13, 15, 'spruce_planks'), box(0, 13, 0, 16, 15, 16, 'stripped_spruce_log'),
         box(3, 0, 2, 5, 2, 4, 'spruce_log'), box(11, 0, 12, 13, 2, 14, 'spruce_log')]
    z = 13 if not opened else 20
    a += [box(2, 7, z, 14, 12, z+1, 'oak_planks'), box(7, 9, z+1, 9, 10, z+2, 'iron_block')]
    if opened:
        a += [box(2, 7, 13, 14, 8, 21, 'oak_planks'), box(2, 8, 13, 3, 12, 21, 'oak_planks'),
              box(13, 8, 13, 14, 12, 21, 'oak_planks'), box(5, 8, 15, 11, 10, 19, 'iron_block')]
        for x in (6, 9): a += [box(x, 10, 16, x+1, 10.2, 17, 'blue_wool')]
    return a

def generate():
    write(ASSETS/'blockstates/leak_wallpaper.json', {'variants': {'': {'model': MOD+':block/leak_wallpaper'}}})
    write(ASSETS/'models/block/leak_wallpaper.json', {'parent': 'minecraft:block/cube_all', 'textures': {'all': MOD+':block/leak_wallpaper'}})
    props = {}
    for name, color in [('blue', 'light_blue_concrete'), ('cream', 'white_concrete'), ('red', 'red_concrete')]:
        props['cup_'+name], props['cup_hung_'+name] = cup(color), cup(color, True)
    props['hook'] = hook()
    props['towel'] = [box(3, 14, 0, 13, 15, 3, 'spruce_planks'), box(4, 1, 1, 12, 15, 2, 'white_wool')]
    props['plate'] = [box(3, 0, 3, 13, .5, 13, 'white_concrete'), box(9, .5, 10, 12, .6, 11, 'gray_concrete')]
    props['geranium'], props['geranium_plate'] = geranium(), geranium(True)
    props['sink_full'], props['sink_empty'], props['sink_clean'] = sink(True), sink(), sink(clean=True)
    props['laundry'] = cloth('light_blue_wool', -7) + [box(3, -5, 5, 7, -4, 11, 'red_wool'), box(9, -5, 6, 13, -3, 10, 'yellow_wool')]
    props['shirts'] = cloth('white_wool', -7) + cloth('light_gray_wool', -5)
    props['sock_single'] = [box(5, -7, 5, 8, -6, 11, 'purple_wool'), box(7, -7, 9, 11, -6, 12, 'purple_wool')]
    props['trousers'] = [box(3, 0, 2, 13, 2, 7, 'blue_wool'), box(3, 0, 7, 7, 1, 14, 'blue_wool'), box(9, 0, 7, 13, 1, 14, 'blue_wool')]
    props['list_spot'] = [box(2, 0, 3, 14, .3, 13, 'spruce_planks')]
    props['list'] = [box(3, 0, 4, 13, .3, 12, 'white_wool'), box(4, .3, 5, 10, .4, 5.3, 'gray_wool'), box(4, .3, 7, 11, .4, 7.3, 'gray_wool')]
    for name in ('radio', 'radio_off'):
        props[name] = [box(2, 0, 2, 14, 5, 8, 'spruce_planks'), box(3, 1, 8, 9, 4, 8.2, 'black_wool'), box(10, 1, 8, 13, 3, 8.3, 'iron_block'),
                       box(10, 3, 8.3, 12, 4, 8.4, 'lime_wool' if name=='radio' else 'gray_wool')]
    props['bread_bag'] = [box(3, 0, 3, 13, 4, 13, 'sand'), box(5, 4, 4, 11, 5, 12, 'white_wool')]
    props['car_seat'] = [box(1, 0, 2, 15, 7, 14, 'gray_wool'), box(1, 6, 12, 15, 20, 15, 'gray_wool'), box(4, 20, 12, 12, 25, 15, 'black_wool')]
    props['belt'] = [box(3, -8, 11, 4, 17, 12, 'black_wool'), box(4, -8, 10, 12, -7, 11, 'black_wool'), box(11, -8, 9, 13, -6, 12, 'iron_block')]
    props['drawer'], props['drawer_open'] = drawer(), drawer(True)
    props['candle'] = [box(5, 0, 5, 11, 1, 11, 'copper_block'), box(7, 1, 7, 9, 4, 9, 'white_wool'), box(7.5, 4, 7.5, 8.5, 5, 8.5, 'black_wool')]
    props['button_tin'] = [box(3, 0, 4, 13, 2, 12, 'iron_block')]
    props['clock'] = [box(3, 0, 0, 13, 13, 2, 'spruce_planks'), box(4, 2, 2, 12, 12, 2.2, 'white_concrete'), box(7.5, 7, 2.2, 8.5, 11, 2.3, 'black_wool'), box(8, 6.5, 2.2, 11, 7.5, 2.3, 'black_wool')]
    props['apron'] = [box(3, -7, 5, 13, -6, 13, 'white_wool'), box(5, -6, 4, 11, -5, 11, 'white_wool')]
    for name in ('light', 'light_off'):
        props[name] = [box(7, 12, 7, 9, 16, 9, 'iron_block'), box(3, 10, 3, 13, 12, 13, 'white_concrete' if name=='light' else 'gray_concrete')]
    props['small_light'] = [box(7, 3, 7, 9, 32, 9, 'iron_block'), box(3, 1, 3, 13, 3, 13, 'yellow_concrete')]
    variants = {}
    for name, parts in props.items():
        write(ASSETS/f'models/block/leak/{name}.json', mesh(parts))
        for facing, rotation in [('north',0),('east',90),('south',180),('west',270)]:
            variants[f'kind={name},facing={facing}'] = {'model': MOD+':block/leak/'+name, 'y': rotation}
    write(ASSETS/'blockstates/leak_prop.json', {'variants': variants})
    colors = dict(blue='light_blue_wool', ochre='yellow_wool', grey='gray_wool', green='green_wool', red='red_wool', single='purple_wool')
    for name, color in colors.items():
        write(ASSETS/f'models/item/leak_sock_{name}.json', mesh([box(5,6,7.5,9,14,8.5,color),box(5,3,7.5,12,6,8.5,color),box(5,13,7.5,9,15,8.5,'white_wool')], item=True))
    write(ASSETS/'models/item/leak_reading_sheet.json', mesh([box(2,3,7.8,14,14,8.1,'white_wool'),box(3,11,8.1,11,11.2,8.2,'gray_wool'),box(3,8,8.1,10,8.2,8.2,'gray_wool')],item=True))
    lang_path = ASSETS/'lang/en_us.json'
    lang = json.loads(lang_path.read_text())
    lang.update({'block.the_oldest_house.leak_wallpaper':'Faded Leaf Wallpaper', 'block.the_oldest_house.leak_prop':'A Household Object', 'entity.the_oldest_house.staircase_reader':'Reader', 'item.the_oldest_house.leak_reading_sheet':'A Loose Sheet'})
    for name in colors: lang['item.the_oldest_house.leak_sock_'+name] = ('Unmatched' if name=='single' else name.title())+' Sock'
    sounds_path = ASSETS/'sounds.json'
    sounds = json.loads(sounds_path.read_text())
    rng, rate = random.Random(449), 22050
    with tempfile.TemporaryDirectory() as tmp:
        for name, seconds in [('drip',.65),('clock',.18),('rain',4.4),('radio',5.2),('bag',.7)]:
            samples, previous = [], 0
            for i in range(int(rate*seconds)):
                t = i/rate
                noise = rng.uniform(-1,1)
                if name=='drip': v=.28*math.sin(2*math.pi*(850*t+420*t*t))*math.exp(-t*14)
                elif name=='clock': v=(noise*.25+math.sin(t*6500)*.1)*math.exp(-t*55)
                elif name=='rain':
                    previous=.94*previous+.06*noise;v=.24*previous*min(1,t/.3,(seconds-t)/.5)
                elif name=='radio': v=(.08*math.sin(2*math.pi*196*t)+.05*math.sin(2*math.pi*293*t)+.07*noise)*min(1,t/.2,(seconds-t)/.6)
                else: v=.16*noise*(math.sin(t*14)**2)*min(1,t/.08,(seconds-t)/.15)
                samples.append(struct.pack('<h',int(max(-1,min(1,v))*32767)))
            wav = Path(tmp)/f'{name}.wav'
            with wave.open(str(wav),'wb') as out:
                out.setnchannels(1);out.setsampwidth(2);out.setframerate(rate);out.writeframes(b''.join(samples))
            target = ASSETS/f'sounds/leak/{name}.ogg';target.parent.mkdir(parents=True,exist_ok=True)
            subprocess.run(['ffmpeg','-y','-loglevel','error','-i',str(wav),'-c:a','libvorbis','-q:a','2',str(target)],check=True)
            sounds['leak.'+name] = {'sounds':[MOD+':leak/'+name]}
    write(lang_path,lang);write(sounds_path,sounds)
    print(f'{len(props)} authored prop meshes, seven item meshes, five original cues; wallpaper preserved.')

if __name__=='__main__': generate()
