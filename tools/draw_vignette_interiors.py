"""Additive, authored native-pixel materials and fitted room objects.

Every model and its native collision are generated from the same physical cuboids.
No historical NPC sheets, original puzzle textures, containers or loot are replaced.
"""
from pathlib import Path
import argparse, io, json, re
from PIL import Image, ImageDraw
ROOT=Path(__file__).resolve().parents[1]
A=ROOT/'src/main/resources/assets/the_oldest_house'
P='the_oldest_house:block/interiors/'
PAINT={}; OUT={}; M={}
def tile(name,bg):
 im=Image.new('RGBA',(16,16),bg);PAINT[name]=im;return ImageDraw.Draw(im)
def wood(name,bg,dark,light,vertical=False):
 d=tile(name,bg)
 for y in (0,5,10,15):
  d.line((y,0,y,15) if vertical else (0,y,15,y),fill=dark)
 for x,y,w in ((2,2,6),(8,7,4),(1,12,5),(11,3,3)):
  d.line((y,x,y,x+w) if vertical else (x,y,x+w,y),fill=light)
 return d
def paper(name,bg,ink,pattern):
 d=tile(name,bg)
 if pattern=='stripe':
  for x in (1,5,9,13):d.line((x,0,x,15),fill=ink)
 elif pattern=='rose':
  for x,y in ((3,3),(11,11)):
   for dx,dy in ((0,0),(-1,-1),(1,-1)):d.point((x+dx,y+dy),fill=ink)
   d.line((x,y+1,x,y+3),fill='#79836c');d.point((x+1,y+2),fill='#79836c')
 elif pattern=='star':
  for x,y in ((3,3),(11,11)):d.line((x-2,y,x+2,y),fill=ink);d.line((x,y-2,x,y+2),fill=ink)
 elif pattern=='damask':
  for x,y in ((4,4),(12,12)):d.line((x,y-3,x-2,y,x,y+3,x+2,y,x,y-3),fill=ink);d.point((x,y),fill=ink)
 elif pattern=='cracks':
  d.line((1,0,2,3,1,6,3,8,2,11),fill=ink);d.line((14,9,13,12,14,15),fill=ink)
 d.point((7,13),fill=ink)
 return d
paper('sickroom_plaster','#82755c','#615b4c','cracks');wood('sickroom_boards','#665139','#352e25','#826748')
paper('clap_stars','#a2b9b2','#d8ce99','star');wood('clap_boards','#b49b71','#75674f','#c4ae87')
paper('hill_roses','#c2b6a2','#967d81','rose');wood('hill_dado','#687b79','#3f514f','#8b9c91',True);wood('hill_floor','#544537','#302c26','#72604b')
paper('workshop_plaster','#cec8ad','#a1a18d','cracks');wood('bench','#9d7950','#5c4931','#b99361')
d=tile('cutting_mat','#46625b')
for a in (0,4,8,12):d.line((a,0,a,15),fill='#6e8b73');d.line((0,a,15,a),fill='#6e8b73')
d.line((1,14,14,1),fill='#c1b67d')
paper('usher_linen','#68675d','#44483e','stripe');wood('usher_boards','#4f5043','#2f352c','#69705d')
d=tile('vault_stone','#555b52')
for y in (0,8,15):d.line((0,y,15,y),fill='#343f38')
for x,y in ((0,0),(8,8)):d.line((x,y,x,y+7),fill='#343f38')
d.line((3,1,4,4,3,7),fill='#7b846c');d.line((13,10,12,15),fill='#3c5041')
paper('crimson_silk','#753b3f','#9b5654','damask');wood('crimson_walnut','#4a342b','#251f1d','#73513a')
paper('trailer_paper','#c1b39a','#9b8d73','stripe')
d=tile('trailer_vinyl','#b9b49a');d.rectangle((0,0,7,7),fill='#84938b');d.rectangle((8,8,15,15),fill='#84938b')
for x,y in ((2,4),(9,11),(13,2)):d.line((x,y,x+2,y+1),fill='#797e70')
paper('confession_paper','#847455','#a38c61','stripe');wood('confession_oak','#493c30','#292721','#6c5842',True)
paper('watchroom_flowers','#c4c7b6','#8b98a4','rose');wood('watchroom_boards','#b69265','#735b41','#c6a878')
wood('holiday_pine','#a48757','#6d593d','#bf9d69',True)
d=tile('holiday_tile','#8faaa5')
for a in (0,8):d.line((a,0,a,15),fill='#d6d6bc');d.line((0,a,15,a),fill='#d6d6bc')
d.point((4,11),fill='#617d7e');d.point((11,4),fill='#b6c9bb')
paper('karen_paper','#a9a4b4','#d1c6c2','stripe')
d=tile('karen_cork','#9c7958')
for x,y in ((1,2),(4,12),(9,3),(13,11),(6,7),(3,14),(11,8)):d.point((x,y),fill='#695842');d.point(((x+2)%16,(y+1)%16),fill='#b3946d')
wood('nursery_paint','#c8c0a1','#8c8e7b','#e3dbbd');wood('nursery_linen','#c6bf9e','#a1957b','#ded4b3');wood('camp_canvas','#77755c','#4b5143','#959276')
for args in [('oak','#80613e','#493c2b','#a6804e'),('darkwood','#49362b','#28221e','#6d5037'),('whitewood','#b6b7a0','#7b8b80','#d5d4b6'),('bluewood','#627c85','#3a535e','#819697'),('linen','#c8bfa3','#a1957b','#e0d5b8')]:wood(*args)
for n,c,edge in [('iron','#596460','#303c3a'),('brass','#aa8950','#665537'),('enamel','#d2cfb8','#8d9e93'),('blue_enamel','#6d969b','#456870'),('water','#598a91','#9fbeb2'),('ink','#292d2b','#53635c'),('amber','#c6a76c','#e0c58a')]:
 d=tile(n,c);d.rectangle((0,0,15,15),outline=edge);d.line((2,2,12,2),fill=edge)
d=tile('paper','#d8ceb3')
for y,w in ((3,10),(6,8),(9,11),(12,6)):d.line((2,y,w,y),fill='#7a7763')
d.line((1,0,1,15),fill='#b39b7b')
d=tile('keys','#e4dcc1')
for x in (0,4,8,12):d.line((x,0,x,15),fill='#4c4b40')
for x in (1,9):d.rectangle((x,0,x+1,8),fill='#303630')
d=tile('photo','#635d59');d.rectangle((1,1,14,14),outline='#c7b797');d.rectangle((5,3,10,7),fill='#aca090');d.rectangle((3,8,12,13),fill='#353f42');d.line((6,5,9,5),fill='#544b44')
d=tile('spools','#756d58')
for x in (4,11):d.ellipse((x-3,5,x+3,11),fill='#b6b39d',outline='#333f3c');d.point((x,8),fill='#353c37')
d=tile('thread','#bbb29b')
for y in (3,5,7,9,11,13):d.line((1,y,14,y),fill='#8d8b74')
d=tile('model_plan','#cfc4a4');d.rectangle((1,1,14,14),outline='#81755b');d.rectangle((3,3,12,11),outline='#536e73');d.line((8,3,8,7),fill='#536e73');d.line((3,8,6,8),fill='#536e73');d.rectangle((10,5,11,7),fill='#76856e');d.line((3,13,9,13),fill='#8c7856')
d=tile('usher_portrait','#5a5146');d.rectangle((0,0,15,15),outline='#9a7844');d.rectangle((2,2,13,13),fill='#303a34');d.rectangle((6,3,9,7),fill='#9b987a');d.line((6,5,9,5),fill='#494e40');d.rectangle((4,8,11,13),fill='#4a4c41');d.point((10,4),fill='#6f705b')
d=tile('height_marks','#b2b09c')
for y in (1,4,7,10,13):d.line((5,y,9,y),fill='#5b645d')
d.line((6,3,12,3),fill='#887267');d.line((7,9,12,9),fill='#887267')
d=tile('karen_board','#8f7353');d.rectangle((0,0,15,15),outline='#584d3d')
for x,y in ((2,2),(9,4),(3,9)):d.rectangle((x,y,x+4,y+4),fill='#ded0af');d.rectangle((x+1,y+1,x+3,y+3),fill='#657c7f');d.point((x+2,y),fill='#ad6254')
SURFACES=['sickroom_plaster','sickroom_boards','clap_stars','clap_boards','hill_roses','hill_dado','hill_floor','workshop_plaster','usher_linen','usher_boards','vault_stone','crimson_silk','crimson_walnut','trailer_paper','trailer_vinyl','confession_paper','confession_oak','watchroom_flowers','watchroom_boards','holiday_pine','holiday_tile','karen_paper','karen_cork']
for n in SURFACES:M[n]=([(0,0,0,16,16,16,n)],None)
def put(n,parts,active=None):M[n]=(parts,active)
def desk(mat,top):return [(0,14,0,16,16,16,top)]+[(x,0,z,x+2,14,z+2,mat) for x in (1,13) for z in (1,13)]
def cabinet(n,mat,inside):
 base=[(0,0,2,16,2,16,mat),(0,2,2,2,16,16,mat),(14,2,2,16,16,16,mat),(2,2,14,14,16,16,mat),(2,14,2,14,16,14,mat),(2,7,3,14,8,14,mat)]
 shut=base+[(2,2,1,14,14,3,mat),(11,7,0,12,9,1,'brass')]
 opened=base+[(3,2,9,7,6,13,inside),(9,8,10,12,13,13,inside),(13,2,0,15,14,12,mat),(12,7,1,13,9,2,'brass')]
 put(n,shut,opened)
for n,mat,inside in [('medicine_press','darkwood','enamel'),('toy_cupboard','bluewood','amber'),('linen_press','nursery_paint','nursery_linen'),('model_drawers','bench','paper'),('usher_press','darkwood','linen'),('dictation_files','confession_oak','paper'),('watchroom_drawer','whitewood','spools'),('trailer_cupboard','oak','enamel'),('holiday_larder','holiday_pine','blue_enamel'),('slide_cabinet','bluewood','photo')]:cabinet(n,mat,inside)
base=desk('darkwood','darkwood')+[(2,14,3,14,16,13,'enamel'),(3,15,4,13,16,12,'ink'),(11,14,13,13,16,15,'brass')]
put('sickroom_washstand',base,base+[(4,15.5,5,12,16,11,'water')])
for n,woodmat,enamel in [('trailer_sink','whitewood','enamel'),('holiday_sink','holiday_pine','blue_enamel')]:
 base=[(0,0,0,16,14,16,woodmat),(0,14,0,16,16,16,enamel),(2,15,3,14,16,12,'ink'),(11,15,13,13,16,15,'iron')]
 put(n,base,base+[(3,15.5,4,13,16,11,'water')])
for n,mat in [('bedside_lamp','brass'),('task_lamp','iron'),('karen_lamp','bluewood'),('hill_lamp','whitewood')]:
 base=[(4,0,4,12,1,12,mat),(7,1,7,9,9,9,mat),(3,7,3,13,12,13,'linen')];put(n,base,base[:-1]+[(3,7,3,13,12,13,'amber')])
base=[(1,0,2,15,2,14,'darkwood'),(2,2,3,14,3,11,'keys'),(2,3,11,14,12,14,'bluewood')];put('toy_piano',base,base+[(8,3,3,10,3.5,9,'ink')])
put('rocking_horse',[(2,0,3,4,2,15,'oak'),(12,0,3,14,2,15,'oak'),(3,2,6,5,7,8,'oak'),(11,2,6,13,7,8,'oak'),(3,7,5,13,10,12,'bluewood'),(3,9,3,7,15,7,'oak')],[(2,0,1,4,2,13,'oak'),(12,0,1,14,2,13,'oak'),(3,2,4,5,7,6,'oak'),(11,2,4,13,7,6,'oak'),(3,7,3,13,10,10,'bluewood'),(3,9,1,7,15,5,'oak')])
put('sewing_basket',[(2,0,2,14,8,14,'oak'),(3,8,3,13,9,13,'linen')],[(2,0,2,14,8,14,'oak'),(3,8,12,13,15,13,'linen'),(4,5,4,7,10,8,'thread'),(9,5,5,12,9,9,'bluewood')])
put('craft_bench',desk('bench','cutting_mat'));put('drafting_board',[(1,0,1,15,1,15,'bench'),(2,1,2,14,2,14,'paper'),(2,2,12,14,2.5,13,'brass')])
base=[(1,0,1,15,1,15,'bench'),(2,1,3,4,2,13,'iron'),(6,1,2,8,3,11,'oak'),(10,1,10,13,5,13,'blue_enamel')];put('model_tools',base,base+[(7,1,12,10,2,14,'linen')])
# Piano's lid is behind its keyboard: the player can actually see the keys from above.
base=[(0,12,8,16,14,16,'darkwood'),(0,9,1,16,10,8,'keys'),(0,0,1,2,9,3,'darkwood'),(14,0,1,16,9,3,'darkwood'),(0,0,13,2,12,15,'darkwood'),(14,0,13,16,12,15,'darkwood')]
put('usher_spinet',base+[(0,14,8,16,16,16,'darkwood')],base+[(0,14,14,16,16,16,'darkwood')])
put('cylinder_cabinet',desk('crimson_walnut','crimson_walnut')+[(1,5,8,15,6,15,'crimson_walnut'),(2,6,10,6,10,14,'brass'),(9,6,10,13,10,14,'brass')])
put('cylinder_horn',[(2,0,3,13,3,13,'darkwood'),(6,3,6,10,5,10,'brass'),(8,5,8,10,9,10,'brass'),(6,9,6,12,11,12,'brass'),(4,11,4,14,13,14,'brass'),(5,13,5,13,13.5,13,'ink')])
put('folding_music_stand',[(3,0,4,13,1,12,'iron'),(7,1,7,9,10,9,'iron'),(2,10,8,14,15,10,'darkwood'),(3,11,7,13,15,8,'paper')])
put('dictation_desk',desk('confession_oak','confession_oak'));put('ink_set',[(1,0,2,15,1,14,'darkwood'),(2,1,3,11,1.5,13,'paper'),(12,1,3,15,4,6,'ink'),(10,1,10,14,2,11,'brass')])
base=[(4,0,4,12,1,12,'darkwood'),(5,1,5,11,4,11,'brass'),(7,4,7,9,5,9,'iron')];put('desk_bell',base,base[:-1]+[(7,3.5,7,9,4,9,'iron')])
put('watchroom_tripod_case',[(1,0,3,15,4,13,'bluewood'),(2,4,4,14,5,12,'iron')],[(1,0,3,15,4,13,'bluewood'),(2,4,12,14,14,13,'bluewood'),(3,2,5,5,5,11,'iron'),(8,2,5,13,6,10,'ink')])
put('watchroom_tape',[(1,0,2,15,3,14,'bluewood'),(2,3,2,14,4,14,'bluewood')],[(1,0,2,15,3,14,'bluewood'),(2,3,13,14,13,14,'bluewood'),(2,2,4,14,4,12,'spools')])
put('holiday_crockery',[(1,0,2,15,2,14,'blue_enamel'),(2,2,3,8,3,10,'enamel'),(10,2,7,14,7,11,'blue_enamel')])
put('karen_slide_sorter',[(1,0,1,15,2,15,'bluewood'),(2,2,2,14,2.5,14,'photo')],[(1,0,1,15,2,15,'bluewood'),(2,2,2,14,2.5,14,'amber')])
put('camp_kit',[(1,0,1,15,8,15,'camp_canvas'),(2,8,2,14,9,14,'camp_canvas')],[(1,0,1,15,8,15,'camp_canvas'),(2,8,13,14,15,14,'camp_canvas'),(3,5,3,7,10,7,'enamel'),(9,5,5,12,9,12,'linen')])
for n,mat in [('model_plan','model_plan'),('usher_portrait','usher_portrait'),('hill_height_marks','height_marks'),('karen_noticeboard','karen_board')]:
 put(n,[(0,0,14,16,16,16,'bench' if n=='model_plan' else 'darkwood'),(1,1,13.5,15,15,14,mat)])
put('crimson_coat_hook',[(1,10,14,15,13,16,'crimson_walnut'),(3,9,10,5,12,14,'brass'),(11,9,10,13,12,14,'brass'),(10,1,11,14,10,14,'crimson_silk')])
put('camp_worktable',desk('oak','camp_canvas'))
put('seance_sideboard',desk('darkwood','darkwood')+[(0,2,8,16,12,16,'darkwood'),(2,3,7,7,10,8,'oak'),(9,3,7,14,10,8,'oak')])
put('tea_service',[(1,0,2,15,1,14,'brass'),(3,1,6,7,4,10,'enamel'),(8,1,4,13,7,9,'blue_enamel'),(7,4,3,9,6,7,'blue_enamel')])
put('parlour_music_box',[(2,0,3,14,5,13,'darkwood'),(3,5,4,13,6,12,'oak')],[(2,0,3,14,5,13,'darkwood'),(3,5,12,13,13,13,'oak'),(4,3,6,12,5,10,'brass'),(6,5,6,10,6,10,'iron')])
ACTIVE=[n for n,(parts,a) in M.items() if a is not None]
# North-facing solids. One source drives native JSON models and the game's actual collision.
geo=['package io.github.knaitoe.theoldesthouse.labyrinth;','/** Generated by tools/draw_vignette_interiors.py; mesh and collision share cuboids. */','final class VignetteInteriorGeometry {',' static double[][] boxes(VignetteInteriorBlock.Kind kind,boolean active){return switch(kind){']
variants={}
for name,(normal,active) in M.items():
 for state,parts in [(False,normal),(True,active if active is not None else normal)]:
  modelname=name+('_active' if state and active is not None else '')
  elements=[]
  for x0,y0,z0,x1,y1,z1,mat in parts:
   assert 0<=x0<x1<=16 and 0<=y0<y1<=16 and 0<=z0<z1<=16,(name,mat)
   uvs={'north':[16-x1,16-y1,16-x0,16-y0],'south':[x0,16-y1,x1,16-y0],'east':[16-z1,16-y1,16-z0,16-y0],'west':[z0,16-y1,z1,16-y0],'up':[x0,z0,x1,z1],'down':[x0,16-z1,x1,16-z0]}
   elements.append({'from':[x0,y0,z0],'to':[x1,y1,z1],'faces':{f:{'texture':'#'+mat,'uv':uv,**({'cullface':f} if name in SURFACES else {})} for f,uv in uvs.items()}})
  mats=sorted({b[6] for b in parts});assert all(m in PAINT for m in mats)
  obj={'ambientocclusion':True,'textures':{**{m:P+m for m in mats},'particle':P+mats[0]},'elements':elements}
  OUT[A/f'models/block/interiors/{modelname}.json']=json.dumps(obj,separators=(',',':'))+'\n'
  for facing,rot in [('north',0),('east',90),('south',180),('west',270)]:
   obj={'model':'the_oldest_house:block/interiors/'+modelname}
   if rot:obj['y']=rot
   variants[f'active={str(state).lower()},facing={facing},kind={name}']=obj
 def arr(parts):return 'new double[][]{'+','.join('{'+','.join(str(c) for c in b[:6])+'}' for b in parts)+'}'
 geo.append(f' case {name.upper()} -> '+(f'active?{arr(active)}:{arr(normal)}' if active is not None else arr(normal))+';')
geo+=[' };}','}'];OUT[ROOT/'src/main/java/io/github/knaitoe/theoldesthouse/labyrinth/VignetteInteriorGeometry.java']='\n'.join(geo)+'\n'
OUT[A/'blockstates/vignette_interior.json']=json.dumps({'variants':variants},separators=(',',':'))+'\n'
for n,im in PAINT.items():
 b=io.BytesIO();im.save(b,format='PNG');OUT[A/f'textures/block/interiors/{n}.png']=b.getvalue()
block=ROOT/'src/main/java/io/github/knaitoe/theoldesthouse/labyrinth/VignetteInteriorBlock.java'
if block.exists():
 body=block.read_text().split('enum Kind implements StringRepresentable {',1)[1].split(';',1)[0]
 assert set(re.findall(r'\b[A-Z][A-Z_]+\b',body))=={n.upper() for n in M},'Unrendered or stale registry kind'
p=argparse.ArgumentParser();p.add_argument('--check',action='store_true');args=p.parse_args();bad=[]
for path,value in OUT.items():
 data=value.encode() if isinstance(value,str) else value
 if args.check:
  if not path.exists() or path.read_bytes()!=data:bad.append(str(path.relative_to(ROOT)))
 else:path.parent.mkdir(parents=True,exist_ok=True);path.write_bytes(data)
if bad:raise SystemExit('Stale interior assets: '+', '.join(bad))
print(f'{len(PAINT)} pixel tiles; {len(M)} kinds; {len(ACTIVE)} usable fittings; mesh/collision parity checked.')
