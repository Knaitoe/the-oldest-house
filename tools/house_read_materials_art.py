"""0.4.160: the quiet rooms' own building materials, so no room stands in plain vanilla blocks.

Every texture is a native sixteen-pixel tile drawn here; every model uses a vanilla template (cube, column, slab, stairs,
fence, gate, pane, door, carpet, chain) so the blocks connect, rotate and collide exactly like their vanilla kin.
"""
from PIL import Image, ImageDraw
import json, random

NS='the_oldest_house'
def _rng(name):return random.Random(160+sum(map(ord,name))*7)
def _speckle(im,name,colours,count):
 d=ImageDraw.Draw(im);rng=_rng(name)
 for _ in range(count):d.point((rng.randrange(16),rng.randrange(16)),fill=rng.choice(colours))
 return im
def _boards(name,mid,dark,light,seam,rows=4,along_x=True,knots=2):
 """Floorboards and deck boards: rows of planks, staggered butt joints, a few knots."""
 im=Image.new('RGBA',(16,16),mid);d=ImageDraw.Draw(im);rng=_rng(name);h=16//rows
 for r in range(rows):
  y=r*h
  for x in range(16):
   for k in range(h):
    v=rng.random()
    if v<.18:d.point((x,y+k) if along_x else (y+k,x),fill=dark)
    elif v>.88:d.point((x,y+k) if along_x else (y+k,x),fill=light)
  d.line((0,y+h-1,15,y+h-1) if along_x else (y+h-1,0,y+h-1,15),fill=seam)
  joint=(r*7+5)%16
  d.line((joint,y,joint,y+h-2) if along_x else (y,joint,y+h-2,joint),fill=seam)
 for _ in range(knots):
  x,y=rng.randrange(1,15),rng.randrange(0,16-h)
  d.point((x,y+1),fill=seam);d.point((x+1,y+1),fill=dark)
 return im
def textures():
 t={}
 # Waiting room: office carpet tile, an acoustic drop ceiling, a laminate counter.
 im=Image.new('RGBA',(16,16),'#5d6670');_speckle(im,'carpet',['#4d555f','#6c7680','#535b66','#737d86','#47505a'],120);t['carpet']=im
 im=Image.new('RGBA',(16,16),'#dcd8cb');d=ImageDraw.Draw(im);rng=_rng('ceiling_tile')
 for _ in range(26):x,y=rng.randrange(1,15),rng.randrange(1,15);d.point((x,y),fill='#b9b4a6')
 d.line((0,0,15,0),fill='#a7a294');d.line((0,0,0,15),fill='#a7a294');d.line((1,1,15,1),fill='#ebe7da');t['ceiling_tile']=im
 im=Image.new('RGBA',(16,16),'#e3dfd3');_speckle(im,'ceiling',['#d8d3c6','#ece8dc','#d2cdbf'],40);t['ceiling']=im
 im=Image.new('RGBA',(16,16),'#b99a72');d=ImageDraw.Draw(im);rng=_rng('counter')
 for y in range(16):
  for x in range(16):
   if rng.random()<.22:d.point((x,y),fill='#a8885f' if (x+y)%3 else '#c7a982')
 for y in [3,7,11]:d.line((0,y,15,y),fill='#a58560')
 d.rectangle((0,0,15,1),fill='#d8cbb0');d.line((0,2,15,2),fill='#8e7051');d.line((0,15,15,15),fill='#5f4a35');t['counter']=im
 # The lot outside: wet asphalt, a painted line, a brick wall across the way, a low overcast sky.
 im=Image.new('RGBA',(16,16),'#3c3f42');_speckle(im,'asphalt',['#33363a','#464a4d','#2d3033','#505457'],110);t['asphalt']=im
 im=t['asphalt'].copy();d=ImageDraw.Draw(im);d.rectangle((6,0,9,15),fill='#cfcfc4');_speckle(im,'asphalt_line_wear',['#a9a99f','#3c3f42'],10);t['asphalt_line']=im
 im=Image.new('RGBA',(16,16),'#8b5b4c');d=ImageDraw.Draw(im)
 for row in range(4):
  y=row*4;d.line((0,y+3,15,y+3),fill='#a59e91')
  off=0 if row%2==0 else 4
  for x in range(off,16,8):d.line((x,y,x,y+2),fill='#a59e91')
 _speckle(im,'lot_wall',['#7b4e41','#9a685a','#6e463b'],40);t['lot_wall']=im
 im=Image.new('RGBA',(16,16),'#8d9497');d=ImageDraw.Draw(im)
 for y in range(16):d.line((0,y,15,y),fill=(141-y//3,148-y//3,151-y//4,255))
 _speckle(im,'overcast',['#959ca0','#868d90'],18);t['overcast']=im
 # The porch: painted deck boards, asphalt shingles, white trim and posts, rails, lattice and windows.
 t['porch_boards']=_boards('porch_boards','#7f8b92','#717d84','#8d999f','#5b666c')
 im=Image.new('RGBA',(16,16),'#4b4846');d=ImageDraw.Draw(im);rng=_rng('shingles')
 for row in range(4):
  y=row*4;d.line((0,y+3,15,y+3),fill='#2f2d2c');off=(row*5)%8
  for x in range(off,16,8):d.line((x,y,x,y+3),fill='#363331')
  for x in range(16):
   for k in range(3):
    if rng.random()<.25:d.point((x,y+k),fill=rng.choice(['#56524f','#423f3d','#5c5754']))
 t['shingles']=im
 im=Image.new('RGBA',(16,16),'#e6e2d6');_speckle(im,'trim',['#d8d3c5','#f0ece0'],24);d=ImageDraw.Draw(im);d.line((0,15,15,15),fill='#c4beae');t['trim']=im
 im=Image.new('RGBA',(16,16),'#e3dfd2');d=ImageDraw.Draw(im);d.line((0,0,0,15),fill='#c8c2b3');d.line((15,0,15,15),fill='#c8c2b3');d.line((1,0,1,15),fill='#f1ede1')
 _speckle(im,'post_side',['#d7d2c4','#ece8dc'],20);t['post_side']=im
 im=Image.new('RGBA',(16,16),'#d9d4c6');d=ImageDraw.Draw(im);d.rectangle((0,0,15,15),outline='#bfb9aa');d.rectangle((3,3,12,12),outline='#e9e5d9');t['post_end']=im
 t['rail']=_speckle(Image.new('RGBA',(16,16),'#e0dccf'),'rail',['#cfc9ba','#ece8dc','#c4bead'],46)
 t['picket']=_speckle(Image.new('RGBA',(16,16),'#d2cdbf'),'picket',['#bdb7a7','#e2ddd0','#a9a393','#c7c1b2'],70)
 im=Image.new('RGBA',(16,16),(0,0,0,0));d=ImageDraw.Draw(im)
 # Open diamond lattice: two crossing laths every eight texels, gaps left clear.
 for k in range(-16,32,8):
  for w in (0,1):d.line((k+w,0,k+15+w,15),fill='#e7e3d7');d.line((k+15+w,0,k+w,15),fill='#d6d1c3')
 t['lattice']=im
 im=Image.new('RGBA',(16,16),(0,0,0,0));d=ImageDraw.Draw(im);d.rectangle((7,0,8,15),fill='#d4cfc1');t['lattice_edge']=im
 im=Image.new('RGBA',(16,16),(196,214,214,58));d=ImageDraw.Draw(im)
 for k in [(3,11,6,8),(4,13,9,8),(9,6,11,4)]:d.line(k,fill=(232,240,238,120))
 d.rectangle((0,0,15,15),outline='#ebe7dc');d.line((0,8,15,8),fill='#ebe7dc');d.line((8,0,8,15),fill='#ebe7dc');t['window']=im
 im=Image.new('RGBA',(16,16),(0,0,0,0));d=ImageDraw.Draw(im);d.rectangle((7,0,8,15),fill='#ebe7dc');t['window_edge']=im
 # Doors: a white panel door, an office door with frosted glass, and a rough boathouse door.
 def door(name,mid,dark,light,top):
  im=Image.new('RGBA',(16,16),mid);d=ImageDraw.Draw(im);d.rectangle((0,0,15,15),outline=dark)
  if top=='panels':d.rectangle((3,2,12,13),outline=dark);d.line((4,3,11,3),fill=light)
  elif top=='frosted':d.rectangle((3,2,12,12),fill='#c9d3d2');d.rectangle((3,2,12,12),outline=dark);d.line((4,4,9,4),fill='#e6ecea')
  elif top=='planks':
   for x in [4,8,12]:d.line((x,0,x,15),fill=dark)
   d.line((1,4,14,4),fill=dark)
  elif top=='bottom_panels':d.rectangle((3,2,12,13),outline=dark);d.point((13,1),fill='#b39a55');d.point((13,2),fill='#8f7a42')
  elif top=='bottom_planks':
   for x in [4,8,12]:d.line((x,0,x,15),fill=dark)
   d.line((1,11,14,11),fill=dark);d.point((13,1),fill='#6b6b66')
  _speckle(im,name,[light,dark],12);t[name]=im
 door('panel_door_top','#e4e0d4','#bdb7a8','#f1ede2','panels');door('panel_door_bottom','#e4e0d4','#bdb7a8','#f1ede2','bottom_panels')
 door('office_door_top','#a3825d','#7c6245','#bb9a74','frosted');door('office_door_bottom','#a3825d','#7c6245','#bb9a74','bottom_panels')
 door('boathouse_door_top','#6f6455','#4f473d','#857a68','planks');door('boathouse_door_bottom','#6f6455','#4f473d','#857a68','bottom_planks')
 # The coatroom: dark stained floorboards and a painted beadboard wainscot.
 t['floorboards']=_boards('floorboards','#6a4a32','#5b3f2a','#7a573c','#3f2b1d')
 im=Image.new('RGBA',(16,16),'#a9b29a');d=ImageDraw.Draw(im)
 for x in [0,4,8,12]:d.line((x,0,x,15),fill='#8b9580');d.line((x+1,0,x+1,15),fill='#bcc4ae')
 d.line((0,0,15,0),fill='#c9d0bb');d.line((0,1,15,1),fill='#8b9580');t['wainscot']=im
 # The shelter: weathered dock rails and posts, and a wooden shake roof.
 t['dock_rail']=_speckle(Image.new('RGBA',(16,16),'#7d776c'),'dock_rail',['#6d675d','#8c867a','#5f5a51'],80)
 im=Image.new('RGBA',(16,16),'#77705f');d=ImageDraw.Draw(im)
 for x in [3,7,11]:d.line((x,0,x,15),fill='#655f50')
 _speckle(im,'dock_post_side',['#857d6b','#5d5749'],30);t['dock_post_side']=im
 im=Image.new('RGBA',(16,16),'#857c69');d=ImageDraw.Draw(im);d.ellipse((2,2,13,13),outline='#6d6555');d.ellipse((5,5,10,10),outline='#6d6555');t['dock_post_end']=im
 im=Image.new('RGBA',(16,16),'#6b5b47');d=ImageDraw.Draw(im);rng=_rng('shake')
 for row in range(4):
  y=row*4;d.line((0,y+3,15,y+3),fill='#46392b');off=(row*3)%5
  for x in range(off,16,5):d.line((x,y,x,y+3),fill='#4f4232')
  for x in range(16):
   if rng.random()<.3:d.point((x,y+rng.randrange(3)),fill=rng.choice(['#7a6852','#5e4f3d']))
 t['shake']=im
 # The shelter's backdrop: a night across the lake, the treeline lost against it.
 im=Image.new('RGBA',(16,16),'#141922');d=ImageDraw.Draw(im);rng=_rng('night')
 for _ in range(10):d.point((rng.randrange(16),rng.randrange(16)),fill=rng.choice(['#1b2230','#10141b','#202838']))
 t['night']=im
 # A rope for the swing: the chain template samples two three-texel strips.
 im=Image.new('RGBA',(16,16),(0,0,0,0));d=ImageDraw.Draw(im)
 for y in range(16):
  for x in range(6):d.point((x,y),fill='#b49b6f' if (x+y)%3 else '#8f7a55')
 t['rope']=im
 return t

BLOCKS=[
 # (id, kind, textures)
 ('read_carpet','cube',{'all':'carpet'}),('read_ceiling_tile','cube',{'all':'ceiling_tile'}),('read_ceiling','cube',{'all':'ceiling'}),
 ('read_counter','cube',{'all':'counter'}),('read_asphalt','cube',{'all':'asphalt'}),('read_asphalt_line','cube',{'all':'asphalt_line'}),
 ('read_lot_wall','cube',{'all':'lot_wall'}),('read_overcast','cube',{'all':'overcast'}),('read_porch_boards','cube',{'all':'porch_boards'}),
 ('read_shingles','cube',{'all':'shingles'}),('read_trim','cube',{'all':'trim'}),('read_floorboards','cube',{'all':'floorboards'}),
 ('read_wainscot','cube',{'all':'wainscot'}),('read_night','cube',{'all':'night'}),
 ('read_post','pillar',{'end':'post_end','side':'post_side'}),('read_dock_post','pillar',{'end':'dock_post_end','side':'dock_post_side'}),
 ('read_porch_board_slab','slab',{'all':'porch_boards','double':'read_porch_boards'}),('read_shingle_slab','slab',{'all':'shingles','double':'read_shingles'}),
 ('read_shake_slab','slab',{'all':'shake','double':None}),
 ('read_porch_step','stairs',{'all':'porch_boards'}),('read_dock_step','stairs',{'all':'pavilion'}),('read_shingle_stairs','stairs',{'all':'shingles'}),
 ('read_rail','fence',{'all':'rail'}),('read_picket','fence',{'all':'picket'}),('read_dock_rail','fence',{'all':'dock_rail'}),
 ('read_gate','gate',{'all':'picket'}),
 ('read_window','pane',{'pane':'window','edge':'window_edge'}),('read_lattice','pane',{'pane':'lattice','edge':'lattice_edge'}),
 ('read_panel_door','door',{'top':'panel_door_top','bottom':'panel_door_bottom'}),('read_office_door','door',{'top':'office_door_top','bottom':'office_door_bottom'}),
 ('read_boathouse_door','door',{'top':'boathouse_door_top','bottom':'boathouse_door_bottom'}),
 ('read_rope','chain',{'all':'rope'}),
]
def _tex(name):return f'{NS}:block/house_read/{name}'
def _model(out,name,data):out[f'models/block/{name}.json']=(json.dumps(data,indent=2)+'\n').encode()
def _state(out,name,data):out[f'blockstates/{name}.json']=(json.dumps(data,indent=2)+'\n').encode()
FACES=['north','east','south','west']
def _stairs_state(m):
 base={'east':0,'south':90,'west':180,'north':270};v={}
 for f in ['east','north','south','west']:
  for half in ['bottom','top']:
   for shape in ['inner_left','inner_right','outer_left','outer_right','straight']:
    model=m('' if shape=='straight' else '_'+shape.split('_')[0]);y=base[f]
    if shape!='straight':y+=(-90 if shape.endswith('left') else 0) if half=='bottom' else (0 if shape.endswith('left') else 90)
    e={'model':model,'uvlock':True}
    if half=='top':e['x']=180
    if y%360:e['y']=y%360
    v[f'facing={f},half={half},shape={shape}']=e
 return {'variants':v}
def _gate_state(m):
 base={'south':0,'west':90,'north':180,'east':270};v={}
 for f in ['east','north','south','west']:
  for wall in ['false','true']:
   for opened in ['false','true']:
    e={'model':m(('_wall' if wall=='true' else '')+('_open' if opened=='true' else '')),'uvlock':True}
    if base[f]:e['y']=base[f]
    v[f'facing={f},in_wall={wall},open={opened}']=e
 return {'variants':v}
def _fence_state(m):
 parts=[{'apply':{'model':m('_post')}}]
 for face,y in zip(FACES,[0,90,180,270]):
  a={'model':m('_side'),'uvlock':True}
  if y:a['y']=y
  parts.append({'apply':a,'when':{face:'true'}})
 return {'multipart':parts}
def _pane_state(m):
 return {'multipart':[{'apply':{'model':m('_post')}},{'apply':{'model':m('_side')},'when':{'north':'true'}},{'apply':{'model':m('_side'),'y':90},'when':{'east':'true'}},
  {'apply':{'model':m('_side_alt')},'when':{'south':'true'}},{'apply':{'model':m('_side_alt'),'y':90},'when':{'west':'true'}},{'apply':{'model':m('_noside')},'when':{'north':'false'}},
  {'apply':{'model':m('_noside_alt')},'when':{'east':'false'}},{'apply':{'model':m('_noside_alt'),'y':90},'when':{'south':'false'}},{'apply':{'model':m('_noside'),'y':270},'when':{'west':'false'}}]}
def _door_state(m):
 v={}
 for half in ['lower','upper']:
  for hinge in ['left','right']:
   for opened in [False,True]:
    n=('bottom' if half=='lower' else 'top')+'_'+hinge+('_open' if opened else '')
    for face,rot in [('east',0),('south',90),('west',180),('north',270)]:
     y=(rot+(90 if hinge=='left' else -90))%360 if opened else rot;e={'model':m('_'+n)}
     if y:e['y']=y
     v[f'facing={face},half={half},hinge={hinge},open={str(opened).lower()}']=e
 return {'variants':v}
def blocks(out):
 """Models and blockstates for every material, with the vanilla templates' own state logic."""
 for name,kind,tex in BLOCKS:
  m=lambda suffix='',name=name:f'{NS}:block/{name}{suffix}'
  if kind=='cube':_model(out,name,{'parent':'minecraft:block/cube_all','textures':{'all':_tex(tex['all'])}});_state(out,name,{'variants':{'':{'model':m()}}})
  elif kind=='pillar':
   _model(out,name,{'parent':'minecraft:block/cube_column','textures':{'end':_tex(tex['end']),'side':_tex(tex['side'])}})
   _model(out,name+'_horizontal',{'parent':'minecraft:block/cube_column_horizontal','textures':{'end':_tex(tex['end']),'side':_tex(tex['side'])}})
   _state(out,name,{'variants':{'axis=x':{'model':m('_horizontal'),'x':90,'y':90},'axis=y':{'model':m()},'axis=z':{'model':m('_horizontal'),'x':90}}})
  elif kind=='slab':
   all3={'bottom':_tex(tex['all']),'top':_tex(tex['all']),'side':_tex(tex['all'])}
   _model(out,name,{'parent':'minecraft:block/slab','textures':all3});_model(out,name+'_top',{'parent':'minecraft:block/slab_top','textures':all3})
   double=f"{NS}:block/{tex['double']}" if tex['double'] else m('_double')
   if not tex['double']:_model(out,name+'_double',{'parent':'minecraft:block/cube_all','textures':{'all':_tex(tex['all'])}})
   _state(out,name,{'variants':{'type=bottom':{'model':m()},'type=double':{'model':double},'type=top':{'model':m('_top')}}})
  elif kind=='stairs':
   all3={'bottom':_tex(tex['all']),'top':_tex(tex['all']),'side':_tex(tex['all'])}
   for suffix,parent in [('','stairs'),('_inner','inner_stairs'),('_outer','outer_stairs')]:_model(out,name+suffix,{'parent':'minecraft:block/'+parent,'textures':all3})
   _state(out,name,_stairs_state(m))
  elif kind=='fence':
   for suffix,parent in [('_post','fence_post'),('_side','fence_side')]:_model(out,name+suffix,{'parent':'minecraft:block/'+parent,'textures':{'texture':_tex(tex['all'])}})
   _state(out,name,_fence_state(m))
  elif kind=='gate':
   for suffix,parent in [('','template_fence_gate'),('_open','template_fence_gate_open'),('_wall','template_fence_gate_wall'),('_wall_open','template_fence_gate_wall_open')]:_model(out,name+suffix,{'parent':'minecraft:block/'+parent,'textures':{'texture':_tex(tex['all'])}})
   _state(out,name,_gate_state(m))
  elif kind=='pane':
   for suffix in ['_post','_side','_side_alt','_noside','_noside_alt']:_model(out,name+suffix,{'parent':'minecraft:block/template_glass_pane'+suffix,'render_type':'minecraft:translucent','textures':{'pane':_tex(tex['pane']),'edge':_tex(tex['edge'])}})
   _state(out,name,_pane_state(m))
  elif kind=='door':
   for half in ['bottom','top']:
    for hinge in ['left','right']:
     for opened in ['','_open']:_model(out,f'{name}_{half}_{hinge}{opened}',{'parent':f'minecraft:block/door_{half}_{hinge}{opened}','render_type':'minecraft:cutout','textures':{'bottom':_tex(tex['bottom']),'top':_tex(tex['top'])}})
   _state(out,name,_door_state(m))
  elif kind=='carpet':_model(out,name,{'parent':'minecraft:block/carpet','textures':{'wool':_tex(tex['all'])}});_state(out,name,{'variants':{'':{'model':m()}}})
  elif kind=='chain':
   _model(out,name,{'parent':'minecraft:block/chain','render_type':'minecraft:cutout','textures':{'particle':_tex(tex['all']),'all':_tex(tex['all'])}})
   _state(out,name,{'variants':{'axis=x':{'model':m(),'x':90,'y':90},'axis=y':{'model':m()},'axis=z':{'model':m(),'x':90}}})
