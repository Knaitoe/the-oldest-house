"""Fit one dining table with four supports; preserve the existing painted atlases."""
from pathlib import Path
import copy,json
root=Path(__file__).resolve().parents[1]/'src/main/resources/assets/the_oldest_house'
models=root/'models/block'
table=json.loads((models/'ghosts_table.json').read_text())
top=next(e for e in table['elements'] if e['from']==[0,15,0])
leg=next((e for e in table['elements'] if e['from']==[1,0,1]),None)
if leg is None:
    leg=json.loads((models/'ghosts_table_leg.json').read_text())['elements'][1]
table['elements']=[top]
(models/'ghosts_table.json').write_text(json.dumps(table,indent=2)+'\n')
corner=copy.deepcopy(table);corner['elements'].append(leg)
(models/'ghosts_table_leg.json').write_text(json.dumps(corner,indent=2)+'\n')
bolts=json.loads((models/'ghosts_bolts.json').read_text())
if not any(e['from']==[1,0,1] for e in bolts['elements']):bolts['elements'].append(copy.deepcopy(leg));bolts['textures'].update(table['textures'])
(models/'ghosts_bolts.json').write_text(json.dumps(bolts,indent=2)+'\n')
path=root/'blockstates/ghosts_set.json';states=json.loads(path.read_text())
for face,angle in [('north',0),('east',90),('south',180),('west',270)]:states['variants'][f'kind=table_leg,facing={face}']={'model':'the_oldest_house:block/ghosts_table_leg','y':angle}
path.write_text(json.dumps(states,indent=2)+'\n')
