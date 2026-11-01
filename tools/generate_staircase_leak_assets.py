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
    a = [box(1, 0, 1, 15, 13, 15, 'spruce_planks'), box(0, 13, 0, 16, 16, 16, 'stripped_spruce_log'),
         box(3, 0, 2, 5, 2, 4, 'spruce_log'), box(11, 0, 12, 13, 2, 14, 'spruce_log')]
    z = 13 if not opened else 20
    a += [box(2, 7, z, 14, 12, z+1, 'oak_planks'), box(7, 9, z+1, 9, 10, z+2, 'iron_block')]
    if opened:
        a += [box(2, 7, 13, 14, 8, 21, 'oak_planks'), box(2, 8, 13, 3, 12, 21, 'oak_planks'),
              box(13, 8, 13, 14, 12, 21, 'oak_planks'), box(5, 8, 15, 11, 10, 19, 'iron_block')]
        for x in (6, 9): a += [box(x, 10, 16, x+1, 10.2, 17, 'blue_wool')]
    return a

def table(top=True):
    """A cell of the dining table: dark oak under a white cloth, with a leg at two of its corners."""
    a = [box(0, 10.5, 0, 16, 12, 16, 'dark_oak_planks'), box(0, 12, 0, 16, 12.25, 16, 'white_wool'),
         box(1, 0, 1, 3, 10.5, 3, 'dark_oak_log'), box(13, 0, 13, 15, 10.5, 15, 'dark_oak_log')]
    return a

def setting(odd=False, cup=False, bowl=False):
    """A place laid toward the seat (the model's north edge): plate, fork to the left, knife to the right; a cup; a bowl of stew."""
    a = [box(4, 12.25, 1.5, 12, 12.75, 9.5, 'white_concrete'), box(12.75, 12.25, 2, 13.5, 12.5, 9, 'iron_block')]
    if odd: a += [box(2.5, 12.25, 2, 3.25, 12.5, 5.5, 'iron_block'), box(2.4, 12.25, 5.5, 3.35, 12.65, 9, 'spruce_planks')]
    else: a += [box(2.5, 12.25, 2, 3.25, 12.5, 9, 'iron_block')]
    if cup: a += [box(11, 12.25, 10.5, 13.5, 14.5, 13, 'white_concrete'), box(13.5, 12.9, 11.25, 14.3, 13.9, 12.25, 'white_concrete')]
    if bowl: a += [box(5, 12.75, 2.5, 11, 14.25, 8.5, 'white_terracotta'), box(5.5, 14.1, 3, 10.5, 14.2, 8, 'orange_terracotta')]
    return a

def sideboard(opened=False):
    a = [box(0, 0, 1, 16, 14, 16, 'dark_oak_planks'), box(0, 14, 0, 16, 15, 16, 'stripped_dark_oak_log'),
         box(2, 1, .5, 7.5, 9, 1, 'spruce_planks'), box(8.5, 1, .5, 14, 9, 1, 'spruce_planks'),
         box(6.5, 4, 0, 7, 6, .5, 'gold_block'), box(9, 4, 0, 9.5, 6, .5, 'gold_block')]
    if opened:
        a += [box(2, 10, -4, 14, 10.5, 1, 'spruce_planks'), box(2, 10.5, -4, 14, 13, -3.5, 'spruce_planks'),
              box(2, 10.5, -4, 2.5, 13, 1, 'spruce_planks'), box(13.5, 10.5, -4, 14, 13, 1, 'spruce_planks'),
              box(3.5, 10.5, -3, 4.2, 10.8, .5, 'iron_block'), box(5.5, 10.5, -3, 6.2, 10.8, .5, 'iron_block'), box(7.5, 10.5, -3, 8.2, 10.8, .5, 'iron_block'),
              box(7.5, 11.5, -3.5, 8.5, 12.5, -3, 'gold_block')]
    else:
        a += [box(2, 10, .5, 14, 13, 1, 'spruce_planks'), box(7.5, 11, 0, 8.5, 12, .5, 'gold_block')]
    return a

def cupboard(cups=True):
    a = [box(1, 0, 3, 15, 1, 15, 'dark_oak_planks'), box(1, 14, 3, 15, 15, 15, 'dark_oak_planks'), box(1, 1, 14, 15, 14, 15, 'dark_oak_planks'),
         box(1, 1, 3, 2, 14, 14, 'dark_oak_planks'), box(14, 1, 3, 15, 14, 14, 'dark_oak_planks'), box(2, 7, 3, 14, 7.5, 14, 'dark_oak_planks'),
         box(2, 1, 4, 14, 1.2, 13, 'white_concrete'), box(2, 7.5, 4, 14, 7.7, 13, 'white_concrete')]
    if cups:
        for x in (3, 6.5, 10):
            a += [box(x, 1.2, 6, x+2.5, 3.6, 8.5, 'white_concrete'), box(x, 7.7, 6, x+2.5, 10.1, 8.5, 'white_concrete')]
    return a

def vase(cup=False):
    a = [box(5, 0, 5, 9, 6, 9, 'light_blue_terracotta'), box(6.5, 6, 6.5, 7.5, 11, 7.5, 'green_wool'),
         box(4, 10, 6, 7, 12, 8, 'yellow_wool'), box(7, 11, 5, 10, 13, 8, 'red_wool'), box(6, 12, 7, 8, 14, 9, 'white_wool')]
    if cup: a += [box(10, 0, 9, 13, 2.75, 12, 'white_concrete'), box(11.25, 2.75, 10.25, 11.75, 7, 10.75, 'green_wool'), box(10.5, 6.5, 10, 12.5, 8, 11.5, 'pink_wool')]
    return a

def hatch(bowls=False):
    a = [box(0, 0, 0, 16, 2, 16, 'stripped_dark_oak_log')]
    if bowls:
        for x, z in ((1.5, 3), (6, 8), (10.5, 3)):
            a += [box(x, 2, z, x+4, 3.75, z+4, 'white_terracotta'), box(x+.5, 3.6, z+.5, x+3.5, 3.7, z+3.5, 'orange_terracotta')]
    return a

def bed(head, cover=None, pillow=None):
    """Half a single bed (the model's north is the head end): an iron frame, the mattress, and what's on it."""
    a = [box(0, 0, 0, 1, 9, 1, 'iron_block'), box(15, 0, 0, 16, 9, 1, 'iron_block'), box(0, 0, 15, 1, 9, 16, 'iron_block'), box(15, 0, 15, 16, 9, 16, 'iron_block'),
         box(1, 3, 0, 15, 7, 16, 'white_terracotta' if cover is None else 'white_terracotta')]
    if head: a += [box(0, 9, 0, 16, 14, 1, 'iron_block'), box(2, 9, 0.5, 14, 13, 1, 'light_gray_concrete')]
    if cover == 'made': a += [box(.5, 7, 0 if not head else 5, 15.5, 7.5, 16, 'white_wool'), box(.5, 4, 15.5 if not head else 15.5, 15.5, 7, 16, 'white_wool')]
    if pillow: a += [box(3, 7.5, 1, 13, 10, 5, pillow)]
    if pillow == 'light_blue_wool': a += [box(3, 7.5, 2, 13, 10.05, 2.6, 'white_wool'), box(3, 7.5, 3.4, 13, 10.05, 4, 'white_wool')]
    return a

def airing(high=False, sheets=True, stacked=False):
    a = [box(0, 0, 1, 16, 16, 2, 'spruce_planks'), box(0, 0, 14, 16, 16, 15, 'spruce_planks'), box(0, 7.5, 2, 16, 8.5, 14, 'spruce_planks')]
    if not high: a += [box(0, 0, 2, 16, 1, 14, 'spruce_planks'), box(7, 1, 2, 9, 3, 14, 'copper_block')]
    if sheets:
        a += [box(2, 8.5 if high else 1, 3, 14, 11 if high else 4, 13, 'white_wool' if high else 'light_gray_wool')]
        if not high: a += [box(2, 4, 3, 14, 6, 13, 'light_gray_wool')]
    if stacked: a += [box(2, 11, 3, 14, 14, 13, 'white_wool'), box(2, 12, 3, 14, 12.2, 13, 'light_gray_wool')]
    return a

def basket(fill=0, extra=None):
    a = [box(2, 0, 3, 14, 1, 13, 'stripped_oak_log'), box(2, 1, 3, 3, 8, 13, 'stripped_oak_log'), box(13, 1, 3, 14, 8, 13, 'stripped_oak_log'),
         box(3, 1, 3, 13, 8, 4, 'stripped_oak_log'), box(3, 1, 12, 13, 8, 13, 'stripped_oak_log')]
    if fill: a += [box(3, 1, 4, 13, 1 + fill, 12, 'white_wool')]
    if extra == 'striped': a += [box(4, 8, 5, 12, 9, 11, 'light_blue_wool'), box(4, 8, 6, 12, 9.05, 7, 'white_wool'), box(4, 8, 8, 12, 9.05, 9, 'white_wool')]
    if extra == 'sock': a += [box(5, 8, 7, 8, 9, 12, 'purple_wool'), box(7, 8, 10, 11, 9, 12, 'purple_wool')]
    return a

def sheet(state):
    if state == 'rug': return [box(1, 0, 2, 15, .2, 14, 'light_gray_wool'), box(3, .2, 4, 13, .3, 12, 'gray_wool')]
    if state == 'one': return [box(1, 0, 2, 15, .2, 14, 'light_gray_wool'), box(2, 6, 13, 14, 14, 14, 'white_wool'), box(2, 0.2, 4, 14, 6, 13, 'white_wool')]
    if state == 'lifted': return [box(1, 0, 2, 15, .2, 14, 'light_gray_wool'), box(2, 9, 13, 14, 14, 14, 'white_wool'), box(2, 9, 2, 14, 14, 3, 'white_wool'), box(2, 8, 3, 14, 9, 13, 'white_wool')]
    if state == 'folded': return [box(1, 0, 2, 15, .2, 14, 'light_gray_wool'), box(4, .2, 5, 12, 2, 11, 'white_wool')]
    return [box(1, 0, 2, 15, .2, 14, 'light_gray_wool'), box(4, .2, 5, 12, 2, 11, 'white_wool'), box(7.75, 2, 5, 8.25, 2.1, 11, 'gray_wool')]

def coat(state):
    a = [box(1, 0, 1, 3, 10, 3, 'dark_oak_log'), box(13, 0, 1, 15, 10, 3, 'dark_oak_log'), box(1, 0, 13, 3, 10, 15, 'dark_oak_log'), box(13, 0, 13, 15, 10, 15, 'dark_oak_log'),
         box(0, 10, 0, 16, 11, 16, 'dark_oak_planks'), box(2, 11, 2, 14, 12.5, 14, 'blue_wool')]
    sleeve = {'sleeve': [box(10, 11, 12, 14, 12, 18, 'blue_wool')], 'turned': [box(10, 11, 12, 14, 12, 18, 'light_blue_wool')],
              'shut': [box(10, 11, 12, 14, 12, 18, 'blue_wool'), box(11.5, 12, 12, 12.5, 12.1, 18, 'white_wool')],
              'sewn': [box(10, 11, 12, 14, 12, 18, 'blue_wool'), box(11.5, 11, 17.9, 12.5, 12, 18, 'white_wool')],
              'buttoned': [box(10, 11, 12, 14, 12, 18, 'blue_wool'), box(5, 12.5, 6, 6.5, 12.8, 7.5, 'black_wool'), box(5, 12.5, 9, 6.5, 12.8, 10.5, 'gold_block')]}[state]
    return a + sleeve

def kettle(state):
    a = [box(4, 0, 4, 12, 7, 12, 'iron_block'), box(5, 7, 5, 11, 8, 11, 'iron_block'), box(7, 8, 7, 9, 9, 9, 'black_wool'),
         box(12, 2, 7, 15, 3, 9, 'iron_block'), box(5, 9, 7, 11, 10, 9, 'black_wool')]
    if state == 'on': a += [box(5, -1, 5, 11, 0, 11, 'orange_wool')]
    if state == 'boiled': a += [box(5, -1, 5, 11, 0, 11, 'orange_wool'), box(14, 3, 7.5, 15, 7, 8.5, 'white_stained_glass')]
    return a

def teacup(full):
    a = [box(6, 0, 6, 10, 4, 10, 'white_concrete'), box(10, 1, 7.5, 11.5, 3, 8.5, 'white_concrete'), box(5, 0, 5, 11, .5, 11, 'white_concrete')]
    if full: a += [box(6.5, 3.6, 6.5, 9.5, 3.8, 9.5, 'brown_terracotta')]
    return a

def sleeper(state):
    """The settee's head end, a cushion, and somebody under the blanket: a hump that rises and falls. Nobody is seen."""
    a = [box(0, 0, 0, 16, 7, 16, 'blue_wool'), box(14, 7, 0, 16, 14, 16, 'blue_wool'), box(0, 7, 14, 16, 12, 16, 'blue_wool'), box(1, 7, 1, 7, 10, 13, 'white_wool')]
    if state == 'in': a += [box(5, 7, 1, 14, 10, 14, 'red_wool')]
    elif state == 'out': a += [box(5, 7, 1, 14, 11, 14, 'red_wool')]
    else: a += [box(5, 7, 10, 14, 8, 15, 'red_wool'), box(3, 7, 2, 6, 8.5, 6, 'brown_wool')]
    return a

def household(props):
    """0.4.214, Sunday lunch: the table's cells and places, the sideboard, the good cups, the flowers and the hatch."""
    props['place_bare'] = table()
    props['place_laid'] = table() + setting()
    props['place_odd'] = table() + setting(odd=True)
    props['place_cup'] = table() + setting(cup=True)
    props['place_bowl'] = table() + setting(cup=True, bowl=True)
    props['table_end'] = table()
    props['table_end_laid'] = table() + setting()
    props['table_end_cup'] = table() + setting(cup=True)
    props['table_plain'] = table()
    props['bread_board'] = table() + [box(3, 12.25, 3, 13, 12.75, 13, 'spruce_planks'), box(5, 12.75, 5, 11, 15.5, 10, 'orange_terracotta'), box(5.5, 15.5, 5.5, 10.5, 15.75, 9.5, 'brown_terracotta')]
    props['bread_cut'] = table() + [box(3, 12.25, 3, 13, 12.75, 13, 'spruce_planks'), box(5, 12.75, 5, 9, 15.5, 10, 'orange_terracotta'), box(9, 12.75, 5.5, 9.2, 15.2, 9.5, 'sandstone'), box(10, 12.75, 5.5, 10.8, 15, 9.5, 'sandstone')]
    props['sideboard_shut'], props['sideboard_open'] = sideboard(), sideboard(True)
    props['cupboard_cups'], props['cupboard_empty'] = cupboard(), cupboard(False)
    props['vase_flowers'], props['vase_cup'] = vase(), vase(True)
    props['hatch_empty'], props['hatch_bowls'] = hatch(), hatch(True)
    # Fresh sheets.
    props['bed_foot_bare'], props['bed_foot_made'] = bed(False), bed(False, 'made')
    props['bed_head_bare'], props['bed_head_made'] = bed(True), bed(True, 'made', 'white_concrete')
    props['bed_head_plain'], props['bed_head_striped'] = bed(True, 'made', 'white_wool'), bed(True, 'made', 'light_blue_wool')
    props['airing_low'], props['airing_low_empty'] = airing(), airing(sheets=False)
    props['airing_high'], props['airing_high_stacked'] = airing(True), airing(True, stacked=True)
    props['basket_full'], props['basket_half'], props['basket_empty'] = basket(6), basket(3), basket()
    props['basket_striped'], props['basket_sock'] = basket(0, 'striped'), basket(0, 'sock')
    props['fold_empty'], props['sheet_one_end'], props['sheet_lifted'] = sheet('rug'), sheet('one'), sheet('lifted')
    props['sheet_folded'], props['sheet_creased'] = sheet('folded'), sheet('creased')
    props['floor_sock'] = [box(5, 0, 5, 8, 1, 11, 'purple_wool'), box(7, 0, 9, 11, 1, 11, 'purple_wool')]
    # Mending day.
    props['lamp_stand'] = [box(5, 0, 5, 11, 1, 11, 'dark_oak_planks'), box(7.5, 1, 7.5, 8.5, 24, 8.5, 'iron_block'), box(3, 24, 3, 13, 31, 13, 'yellow_wool'), box(4, 23, 4, 12, 24, 12, 'yellow_concrete')]
    props['sewing_box'] = [box(2, 0, 4, 14, 7, 12, 'oak_planks'), box(2, 7, 4, 14, 8, 12, 'stripped_oak_log'), box(9, 8, 6, 12, 10, 9, 'red_wool'), box(10.3, 10, 7.3, 10.7, 12, 7.7, 'iron_block')]
    props['sewing_threaded'] = props['sewing_box'] + [box(10.4, 9.5, 7.4, 10.6, 10.5, 12, 'white_wool')]
    for name in ('sleeve', 'turned', 'shut', 'sewn', 'buttoned'): props['coat_'+name] = coat(name)
    props['button_tin_open'] = [box(3, 0, 4, 13, 2, 12, 'iron_block'), box(3, 2, 11.5, 13, 8, 12, 'iron_block'), box(4, 2, 5, 6, 2.3, 7, 'black_wool'), box(7, 2, 6, 9, 2.3, 8, 'gold_block'), box(9.5, 2, 5, 11.5, 2.3, 7, 'light_blue_stained_glass'), box(6, 2, 8.5, 8, 2.3, 10.5, 'white_concrete')]
    # First light.
    props['kettle_cold'], props['kettle_on'], props['kettle_boiled'] = kettle('cold'), kettle('on'), kettle('boiled')
    props['teacup_empty'], props['teacup_full'] = teacup(False), teacup(True)
    props['potatoes'] = [box(2, 0, 3, 14, 4, 13, 'brown_terracotta'), box(4, 4, 5, 7, 6, 8, 'brown_terracotta'), box(8, 4, 6, 11, 6, 9, 'brown_terracotta'), box(12, 0, 6, 13, 6, 6.5, 'iron_block')]
    props['potatoes_peeled'] = props['potatoes'] + [box(8, 6, 6, 11, 7.5, 9, 'sandstone')]
    props['supper_plate'] = [box(3, 0, 3, 13, .5, 13, 'white_concrete'), box(5, .5, 5, 9, 2, 9, 'orange_terracotta'), box(9, .5, 7, 12, 2, 10, 'sandstone')]
    props['supper_wrapped'] = [box(3, 0, 3, 13, .5, 13, 'white_concrete'), box(3.5, .5, 3.5, 12.5, 3, 12.5, 'white_wool')]
    props['supper_named'] = props['supper_wrapped'] + [box(5, 3, 7, 11, 3.1, 8, 'black_wool')]
    props['sleeper_in'], props['sleeper_out'], props['sleeper_awake'] = sleeper('in'), sleeper('out'), sleeper('awake')
    props['settee_feet'] = [box(0, 0, 0, 16, 7, 16, 'blue_wool'), box(0, 7, 0, 2, 14, 16, 'blue_wool'), box(0, 7, 14, 16, 12, 16, 'blue_wool'), box(2, 7, 1, 16, 9, 14, 'red_wool')]
    props['side_empty'] = [box(2, 0, 2, 4, 10, 4, 'oak_log'), box(12, 0, 12, 14, 10, 14, 'oak_log'), box(1, 10, 1, 15, 11, 15, 'oak_planks')]
    props['side_cup'] = props['side_empty'] + [box(6, 11, 6, 10, 15, 10, 'white_concrete'), box(6.5, 14.6, 6.5, 9.5, 14.8, 9.5, 'brown_terracotta'), box(10, 12, 7.5, 11.5, 14, 8.5, 'white_concrete')]

def prop_meshes():
    props = {}
    for name, color in [('blue', 'light_blue_concrete'), ('cream', 'white_concrete'), ('red', 'red_concrete')]:
        props['cup_'+name], props['cup_hung_'+name] = cup(color), cup(color, True)
    return props

def check():
    """0.4.214: the prop meshes and their states against the files (the cues are encoded fresh each run, so are not compared)."""
    import sys
    props = all_props()
    for name, parts in props.items():
        path = ASSETS/f'models/block/leak/{name}.json'
        assert path.exists() and json.loads(path.read_text()) == mesh(parts), path
    assert json.loads((ASSETS/'blockstates/leak_prop.json').read_text()) == {'variants': variants_for(props)}, 'leak_prop states'
    print(f'Verified {len(props)} staircase prop meshes and their states.')

def variants_for(props):
    variants = {}
    for name in props:
        for facing, rotation in [('north',0),('east',90),('south',180),('west',270)]:
            variants[f'kind={name},facing={facing}'] = {'model': MOD+':block/leak/'+name, 'y': rotation}
    return variants

def generate():
    write(ASSETS/'blockstates/leak_wallpaper.json', {'variants': {'': {'model': MOD+':block/leak_wallpaper'}}})
    write(ASSETS/'models/block/leak_wallpaper.json', {'parent': 'minecraft:block/cube_all', 'textures': {'all': MOD+':block/leak_wallpaper'}})
    props = all_props()
    for name, parts in props.items():
        write(ASSETS/f'models/block/leak/{name}.json', mesh(parts))
    write(ASSETS/'blockstates/leak_prop.json', {'variants': variants_for(props)})
    generate_rest()

def all_props():
    props = prop_meshes()
    props['hook'] = hook()
    props['towel'] = [box(3, 14, 0, 13, 15, 3, 'spruce_planks'), box(4, 1, 1, 12, 15, 2, 'white_wool')]
    props['washing'] = [box(3, 15, 7, 13, 17, 9, 'white_wool'), box(3, 0, 7, 13, 15, 8, 'white_wool')]
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
    household(props)
    return props

def generate_rest():
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
            # Keep each cue's subtitle (and anything else already set on it); only its sound file is written here.
            sounds['leak.'+name] = {**sounds.get('leak.'+name, {}), 'sounds':[MOD+':leak/'+name]}
    write(lang_path,lang);write(sounds_path,sounds)
    print('Authored prop meshes, seven item meshes, five original cues; wallpaper preserved.')

if __name__=='__main__':
    import sys
    check() if '--check' in sys.argv else generate()
