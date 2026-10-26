#!/usr/bin/env python3
"""Native stair partitions and continuous, tread-mounted banisters; original atlases stay untouched."""
import argparse, json, math
from pathlib import Path

ROOT=Path(__file__).resolve().parents[1]
A=ROOT/'src/main/resources/assets/the_oldest_house'

def box(lo,hi,texture='white'):
    uv=[0,4,4,8] if texture=='wood' else [0,0,16,16]
    return {'from':lo,'to':hi,'faces':{f:{'texture':'#'+texture,'uv':uv} for f in ('north','south','east','west','up','down')}}

def models():
    stair={'textures':{'wood':'the_oldest_house:block/ghosts/materials_atlas','white':'minecraft:block/quartz_block_side'},'elements':[
        box([0,0,0],[16,7,16]),box([0,7,0],[16,15,8]),box([0,7,8],[16,8,16],'wood'),box([0,15,0],[16,16,8],'wood')]}
    # White risers stop beneath the wood: no intersecting side walls or coplanar tread tops.
    for a in stair['elements']:
        for b in stair['elements']:
            if a is not b and all(min(a['to'][i],b['to'][i])>max(a['from'][i],b['from'][i]) for i in range(3)):
                raise AssertionError('Stair materials overlap')
    out={'stair':stair}
    for top in (False,True):
        end=16-7*math.sqrt(2) if top else 16-16*math.sqrt(2)
        rail=box([5,7,end],[11,9,16]);rail['rotation']={'origin':[8,8,16],'axis':'x','angle':45,'rescale':False}
        parts=[box([6,-8,13],[10,11,15]),box([6,-8,12],[18,-6,16]),rail]
        if top:parts.append(box([5,14,-16],[11,16,9]))
        out['stair_rail_top' if top else 'stair_rail']={'textures':{'white':'minecraft:block/quartz_block_side'},'elements':parts}
    return out

def main():
    p=argparse.ArgumentParser();p.add_argument('--check',action='store_true');args=p.parse_args()
    for name,model in models().items():
        path=A/f'models/block/ghosts_{name}.json'
        if args.check:assert json.loads(path.read_text())==model,path
        else:path.write_text(json.dumps(model,indent=2)+'\n')
    path=A/'blockstates/ghosts_set.json';states=json.loads(path.read_text())
    for kind in ('stair_rail','stair_rail_top'):
        for face,angle in [('north',0),('east',90),('south',180),('west',270)]:
            key=f'kind={kind},facing={face}';value={'model':'the_oldest_house:block/ghosts_'+kind,'y':angle}
            if args.check:assert states['variants'][key]==value,key
            else:states['variants'][key]=value
    if not args.check:path.write_text(json.dumps(states,indent=2)+'\n')
    print('Verified non-overlapping stair materials and connected native banister meshes.' if args.check else 'Generated native stairs and continuous tread-mounted banisters.')

if __name__=='__main__':main()
