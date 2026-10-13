#!/usr/bin/env python3
"""Native Minecraft meshes for the field case, stone fountain and plain chalk/string tools. No raster art is altered."""
import json
from pathlib import Path

A = Path(__file__).resolve().parents[1] / 'src/main/resources/assets/the_oldest_house'

def box(bounds, material, uv=None):
    face = {'texture': '#' + material}
    if material == 'water':
        face['tintindex'] = 0
    if uv is not None:
        face['uv'] = uv
    return {'from': bounds[:3], 'to': bounds[3:], 'faces': {k: dict(face) for k in ('north','south','east','west','up','down')}}

def write(path, value):
    (A / path).write_text(json.dumps(value, indent=2) + '\n')

def main():
    notes = {'case': 'minecraft:block/cyan_terracotta', 'rim': 'minecraft:block/iron_block',
             'paper': 'minecraft:block/white_wool', 'ink': 'minecraft:block/gray_concrete', 'leg': 'minecraft:block/spruce_planks'}
    parts = [box([3,0,3,5,12,5], 'leg'), box([11,0,11,13,12,13], 'leg'), box([1,12,1,15,13,15], 'rim'),
             box([1,13,1,15,15,15], 'case'), box([2,15,2,14,15.3,11], 'paper'), box([3,15.3,2,13,15.5,10], 'paper'),
             box([1,15,12,15,22,14], 'case'), box([1,21,12,15,22,14], 'rim'), box([7,14,0.5,9,16,1.5], 'rim')]
    for y in range(4):
        parts.append(box([4,15.51,3+y*1.5,11-(y%2)*2,15.56,3.25+y*1.5], 'ink'))
    write('models/block/plain_notes.json', {'ambientocclusion': False, 'textures': {**notes, 'particle': notes['case']}, 'elements': parts})

    stone = 'minecraft:block/stone_bricks'
    bowl = [box([2,0,2,14,3,14], 'stone'), box([0,3,0,3,9,16], 'stone'), box([13,3,0,16,9,16], 'stone'),
            box([3,3,0,13,9,3], 'stone'), box([3,3,13,13,9,16], 'stone'), box([0,8,0,16,9,2], 'lip'),
            box([0,8,14,16,9,16], 'lip'), box([0,8,2,2,9,14], 'lip'), box([14,8,2,16,9,14], 'lip'),
            box([7,3,7,9,11,9], 'lip'), box([3,5,3,13,5.15,13], 'water'), box([7.6,11,7.6,8.4,16,8.4], 'water')]
    # The fountain's animated native water rises from its nozzle and returns into the bowl; it is not a cauldron.
    for side in (-1,1):
        for i in range(4):
            x = 8 + side * (i + .5)
            y = 15 - i * 2.5
            bowl.append(box([x-.4,y-2,7.6,x+.4,y+.25,8.4], 'water'))
    write('models/block/fountain_bowl.json', {'render_type':'minecraft:cutout', 'ambientocclusion':False,
          'textures':{'stone':stone, 'lip':'minecraft:block/smooth_stone', 'water':'minecraft:block/water_still', 'particle':stone}, 'elements':bowl})

    states = json.loads((A/'blockstates/vignette_detail.json').read_text())
    for name in ('plain_notes','fountain_bowl'):
        for direction, turn in (('north',0),('east',90),('south',180),('west',270)):
            states['variants'][f'facing={direction},kind={name}'] = {'model':f'the_oldest_house:block/{name}', 'y':turn}
    write('blockstates/vignette_detail.json', states)

    display = {'gui':{'rotation':[25,35,0], 'translation':[0,0,0], 'scale':[1,1,1]},
               'ground':{'rotation':[0,0,0], 'translation':[0,2,0], 'scale':[.5,.5,.5]},
               'firstperson_righthand':{'rotation':[0,-90,25], 'translation':[1,3,1], 'scale':[.7,.7,.7]},
               'thirdperson_righthand':{'rotation':[0,90,-25], 'translation':[0,2,0], 'scale':[.6,.6,.6]}}
    write('models/item/chalk.json', {'parent':'minecraft:block/block', 'textures':{'chalk':'minecraft:block/white_concrete', 'particle':'minecraft:block/white_concrete'},
          'elements':[box([6,1,6,10,14,10], 'chalk'),box([6.5,14,6.5,9.5,15,9.5], 'chalk')], 'display':display})
    spool = [box([2,2,2,14,4,14], 'wood'),box([2,12,2,14,14,14], 'wood'),box([5,4,5,11,12,11], 'thread')]
    for y in (5,7,9,11):
        spool.append(box([4.5,y,4.5,11.5,y+.5,11.5], 'thread'))
    spool.append(box([11.5,4,7,12,10,8], 'thread'))
    write('models/item/trail_spool.json', {'parent':'minecraft:block/block', 'textures':{'wood':'minecraft:block/oak_planks', 'thread':'minecraft:block/white_wool', 'particle':'minecraft:block/oak_planks'}, 'elements':spool, 'display':display})
    print('Built two scene meshes, eight oriented states and plain native chalk/string item models.')

if __name__ == '__main__':
    main()
