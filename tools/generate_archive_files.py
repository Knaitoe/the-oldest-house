#!/usr/bin/env python3
"""Wooden archive pigeon shelves and paper folders on existing native atlases."""
import argparse,json
from pathlib import Path
A=Path(__file__).resolve().parents[1]/'src/main/resources/assets/the_oldest_house'
def box(lo,hi,texture):
    return {'from':lo,'to':hi,'faces':{f:{'texture':'#'+texture,'uv':[8,8,12,16] if texture=='paper' else [0,0,16,16]} for f in ('north','south','east','west','up','down')}}
def model():
    parts=[box([0,0,3],[16,16,16],'wood'),box([0,0,0],[1,16,3],'wood'),box([15,0,0],[16,16,3],'wood'),box([1,0,0],[15,1,3],'wood'),box([1,15,0],[15,16,3],'wood')]
    for y in (2,5,8,11):parts.extend([box([2,y,1],[14,y+2,3],'paper'),box([2,y+2,0],[14,y+2.5,3],'wood'),box([6,y+.5,.7],[10,y+1.25,1],'metal')])
    return {'parent':'minecraft:block/block','textures':{'wood':'minecraft:block/dark_oak_planks','paper':'the_oldest_house:block/ghosts/equipment_atlas','metal':'minecraft:block/iron_block','particle':'minecraft:block/dark_oak_planks'},'elements':parts}
def main():
    p=argparse.ArgumentParser();p.add_argument('--check',action='store_true');args=p.parse_args();path=A/'models/block/novel_archive_files.json';expected=model()
    if args.check:assert json.loads(path.read_text())==expected
    else:path.write_text(json.dumps(expected,indent=2)+'\n')
    path=A/'blockstates/novel_prop.json';state=json.loads(path.read_text())
    for face,angle in [('north',0),('east',90),('south',180),('west',270)]:
        key=f'facing={face},kind=archive_files';value={'model':'the_oldest_house:block/novel_archive_files','y':angle}
        if args.check:assert state['variants'][key]==value
        else:state['variants'][key]=value
    if not args.check:path.write_text(json.dumps(state,indent=2)+'\n')
    print('Verified fitted archive shelves and original atlas references.' if args.check else 'Generated fitted archive shelves on the original material grid.')
if __name__=='__main__':main()
