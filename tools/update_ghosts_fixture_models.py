#!/usr/bin/env python3
"""Fit original ImageGen atlases to native JSON meshes. No bitmap file is modified."""
from pathlib import Path
import copy, json

ROOT=Path(__file__).resolve().parents[1]
A=ROOT/'src/main/resources/assets/the_oldest_house'
M=A/'models/block'
MATERIALS=['wallpaper','floor','ceiling','tile','panel','wood','formica','metal','cream','cardboard','denim','red','green','cloth','white','plaster']
EQUIPMENT=['hat','controls','label','vines','book_red','book_blue','paper','enamel']

def atlas_uv(index,columns,rows):
    x=index%columns*16/columns;y=index//columns*16/rows
    return [x,y,x+16/columns,y+16/rows]

def box(a,b,texture,uv):
    return {'from':a,'to':b,'faces':{f:{'texture':'#'+texture,'uv':uv} for f in ['north','south','east','west','up','down']}}

def fit(model):
    for key,value in list(model.get('textures',{}).items()):
        name=value.rsplit('/',1)[-1]
        if name in MATERIALS:sheet='materials_atlas';uv=atlas_uv(MATERIALS.index(name),4,4)
        elif name in EQUIPMENT:sheet='equipment_atlas';uv=atlas_uv(EQUIPMENT.index(name),4,2)
        elif name in ['spaghetti','spaghetti_plain']:sheet='meals_atlas';uv=atlas_uv(1 if name=='spaghetti' else 6,4,2)
        else:continue
        model['textures'][key]='the_oldest_house:block/ghosts/'+sheet
        for element in model.get('elements',[]):
            for face in element['faces'].values():
                if face['texture']=='#'+key:face['uv']=uv
    return model

def save(name,model):
    (M/f'ghosts_{name}.json').write_text(json.dumps(model,indent=2)+'\n')

def main():
    for name in ['materials_atlas','equipment_atlas','meals_atlas','call_sheet']:
        if not (A/f'textures/block/ghosts/{name}.png').is_file():raise FileNotFoundError(name)
    for p in M.glob('ghosts_*.json'):
        model=json.loads(p.read_text());p.write_text(json.dumps(fit(model),indent=2)+'\n')
    # Both original spaghetti plates and five additional meals share the same supported, shallow mesh.
    for name,index in [('plate',1),('plain_plate',6),('meal_stew',0),('meal_sandwich',2),('meal_soup',3),('meal_roast',4),('meal_potatoes',5)]:
        save(name,{'textures':{'food':'the_oldest_house:block/ghosts/meals_atlas'},'render_type':'minecraft:cutout','ambientocclusion':False,'elements':[box([3,0,3],[13,1,13],'food',atlas_uv(index,4,2))]})
    lamp=json.loads((M/'ghosts_lamp.json').read_text());save('lamp_off',lamp)
    save('rug',{'textures':{'cloth':'the_oldest_house:block/ghosts/materials_atlas'},'elements':[box([0,0,0],[16,1,16],'cloth',atlas_uv(11,4,4))]})
    kinds=['wallpaper','floor','ceiling','tile','panel','table','chair','counter','sink','cooker','plate','hat','books','jug','bolts','camera','case','monitor','drawing','lamp','bed_head','bed_foot','plain_plate','playhouse','torn_card','drawer','drawer_rig','plaster','meal_stew','meal_sandwich','meal_soup','meal_roast','meal_potatoes','lamp_off','rug','stair','rail','table_leg','stair_rail','stair_rail_top']
    variants={f'kind={kind},facing={face}':{'model':'the_oldest_house:block/ghosts_'+kind,'y':angle} for kind in kinds for face,angle in [('north',0),('east',90),('south',180),('west',270)]}
    (A/'blockstates/ghosts_set.json').write_text(json.dumps({'variants':variants},indent=2)+'\n')
    # Native case contents and lid state are independent of private filming days.
    case=json.loads((M/'ghosts_case.json').read_text())
    body=case['elements'][0];body['to'][1]=10
    lid=box([1,10,1],[15,12,15],'metal',atlas_uv(7,4,4));closed=copy.deepcopy(case);closed['elements'].append(lid);save('case_closed',closed)
    opened=copy.deepcopy(closed);opened['elements'][-1]['rotation']={'origin':[8,10,14],'axis':'x','angle':45,'rescale':False};opened['elements'][1]['rotation']={'origin':[8,10,14],'axis':'x','angle':45,'rescale':False};save('case_open',opened)
    (A/'blockstates/ghosts_case.json').write_text(json.dumps({'variants':{f'facing={face},open={str(opened).lower()}':{'model':'the_oldest_house:block/ghosts_case_'+('open' if opened else 'closed'),'y':angle} for face,angle in [('north',0),('east',90),('south',180),('west',270)] for opened in [False,True]}},indent=2)+'\n')
    item=A/'models/item/camcorder.json';obj=json.loads(item.read_text());item.write_text(json.dumps(fit(obj),indent=2)+'\n')

if __name__=='__main__':main()
