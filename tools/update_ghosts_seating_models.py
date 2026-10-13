"""Fit the native dining chair to the seated jointed cast; retain every painted PNG."""
from pathlib import Path
import copy,json

root=Path(__file__).resolve().parents[1]
path=root/'src/main/resources/assets/the_oldest_house/models/block/ghosts_chair.json'
model=json.loads(path.read_text())
leg=copy.deepcopy(model['elements'][1])
for a,b in [([12,0,2],[14,8,4]),([2,0,12],[4,8,14]),([2,4,2],[14,6,4])]:
    if any(e['from']==a and e['to']==b for e in model['elements']):continue
    element=copy.deepcopy(leg);element['from']=a;element['to']=b;model['elements'].append(element)
path.write_text(json.dumps(model,indent=2)+'\n')
for path in (root/'src/main/resources/assets/the_oldest_house/models/block').glob('ghosts_*.json'):
    text=path.read_text().replace('minecraft:block/smooth_sandstone','minecraft:block/sandstone_top')
    path.write_text(text)
