"""Authored native pixel tiles and fitted meshes for the ordinary coatroom and porch."""
from pathlib import Path
from PIL import Image,ImageDraw
import argparse,io,json,random
from house_read_moments_art import EXTRA_PARTS, tiles as more_tiles, models as more_models
import house_read_materials_art as materials
import house_read_rooms160_art as rooms160
ROOT=Path(__file__).resolve().parents[1];A=ROOT/'src/main/resources/assets/the_oldest_house'
SIDES=['up','down','north','south','east','west']
def cube(a,b,t):return {'from':a,'to':b,'faces':{f:{'texture':'#'+t,'uv':[0,0,16,16]} for f in SIDES}}
def faced(a,b,t,**faces):
 """A cuboid whose named faces (south, down, ...) carry their own texture."""
 c=cube(a,b,t)
 for face,texture in faces.items():c['faces'][face]['texture']='#'+texture
 return c
def outputs():
 out={};tiles={}
 colors={'wallpaper':('#bdb497','#9f927c','#d7ceb6'),'clapboard':('#c8c4ad','#8d927f','#e0d8bc'),'linoleum':('#a7a18c','#807d70','#cbc1a0'),'oak':('#8e6e43','#5f4b33','#b19660'),'brass':('#a18b51','#695d3d','#cfb773'),'glass':('#8caaa6','#728e8a','#bed0c6'),'wool':('#574f47','#3c3935','#73695b'),'trench':('#b59d73','#8c7758','#d3bb8e'),'barn':('#76816c','#515d4c','#98a18a'),'boot':('#65513b','#423b2e','#8f7754'),'cream':('#d8d1b5','#afa990','#efe5c7'),'curtain':('#794841','#5c3733','#9f6454'),'metal':('#9b9d91','#727970','#c2c0ae'),'food':('#dfc398','#a37542','#eee1c0')}
 for name,(mid,dark,light) in colors.items():
  im=Image.new('RGBA',(16,16),mid);d=ImageDraw.Draw(im);rng=random.Random(138+sum(map(ord,name)))
  for _ in range(22):x,y=rng.randrange(16),rng.randrange(16);d.point((x,y),fill=dark if rng.randrange(2) else light)
  if name=='wallpaper':
   for x,y in [(3,4),(11,12)]:d.line((x,y-2,x,y+2),fill=dark);d.point((x-1,y),fill=light);d.point((x+1,y),fill=light)
  elif name=='clapboard':
   for y in [0,8]:d.line((0,y,15,y),fill=dark);d.line((0,y+1,15,y+1),fill=light)
  elif name=='linoleum':
   for x,y in [(0,0),(8,8)]:d.rectangle((x,y,x+7,y+7),fill=dark)
  elif name=='oak':
   for x in [2,7,12]:d.line((x,0,x,15),fill=dark);d.line((x+1,2,x+1,13),fill=light)
  elif name=='glass':d.line((3,1,12,10),fill=light);d.line((3,5,10,12),fill=light)
  elif name in ['wool','barn']:
   for y in range(1,16,4):
    for x in range(0,16,4):d.line((x,y,x+2,y+1),fill=dark)
  elif name=='trench':d.line((7,0,7,15),fill=dark);d.rectangle((2,10,5,11),fill=dark)
  elif name=='curtain':
   for x in range(0,16,4):d.line((x,0,x,15),fill=dark);d.line((x+1,0,x+1,15),fill=light)
  elif name=='food':
   d.rectangle((2,3,13,12),fill=light);d.rectangle((5,6,11,8),fill='#a16b42');d.rectangle((3,9,6,11),fill='#71814d')
  tiles[name]=im
 # Carved rail and the folded slip are blank first. Only their unseen state gains marks. 0.4.163: the porch rail is
 # painted white like the rest of the railing, so the carving is cut into the painted wood.
 painted=materials.textures()['rail']
 for name,base in [('rail_written','rail'),('slip','cream'),('slip_written','cream')]:
  im=(painted if base=='rail' else tiles[base]).copy();d=ImageDraw.Draw(im)
  if 'written' in name:
   for y,length in [(4,10),(7,8),(10,11)]:d.line((2,y,length,y),fill='#564b3b')
  tiles[name]=im
 for half in ['bottom','top']:
  im=Image.new('RGBA',(16,16),(0,0,0,0));d=ImageDraw.Draw(im);d.rectangle((0,0,15,15),outline='#a79570',width=2)
  if half=='bottom':d.rectangle((1,11,14,14),fill='#8e7954');d.rectangle((11,6,12,7),fill='#bfa968')
  for x in range(2,14,2):d.line((x,2,x,13),fill=(100,109,91,95))
  for y in range(2,14,2):d.line((2,y,13,y),fill=(100,109,91,95))
  tiles['screen_'+half]=im
 more_tiles(tiles);rooms160.tiles(tiles)
 for name,im in tiles.items():
  buf=io.BytesIO();im.save(buf,format='PNG');out['textures/block/house_read/'+name+'.png']=buf.getvalue()
 textures={n:'the_oldest_house:block/house_read/'+n for n in tiles}
 RAIL,ROPE='the_oldest_house:block/house_read/rail','the_oldest_house:block/house_read/rope'
 def model(name,elements,layer=None,extra=None):
  data={'ambientocclusion':True,'textures':dict(textures,**(extra or {}),particle=textures.get(name,textures['oak'])),'elements':elements}
  if layer:data['render_type']='minecraft:'+layer
  out['models/block/read_'+name+'.json']=(json.dumps(data,indent=2)+'\n').encode()
 for n in ['wallpaper','clapboard','linoleum','panels','tile','pavilion']:out['models/block/read_'+n+'.json']=(json.dumps({'parent':'minecraft:block/cube_all','textures':{'all':textures[n]}},indent=2)+'\n').encode();out['blockstates/read_'+n+'.json']=(json.dumps({'variants':{'':{'model':'the_oldest_house:block/read_'+n}}},indent=2)+'\n').encode()
 parts=['wool','trench','barn','wool_boots','trench_boots','barn_boots','towel','mirror','mirror_top','umbrella','radiator','curtain','rail','swing','mailbox','plate','covered_mirror']
 for n in parts:
  if n in ['wool','trench','barn']:
   # 0.4.160: each coat hangs on its own wall hook and has its own cut: a long charcoal overcoat, a belted tan trench, a
   # short olive barn jacket with a corduroy collar.
   frame=[cube([7,28,0],[9,29,3],'brass'),cube([7,29,2],[9,31,3],'brass'),cube([3,26,1.5],[13,27,3.5],'oak')]
   top={'wool':3,'trench':5,'barn':13}[n];front={'wool':'wool_front','trench':'trench_front','barn':'barn_front'}[n]
   garment=[cube([3,22,2],[13,26,7],n),cube([2,top+4,2.6],[4,22.5,6.4],n),cube([12,top+4,2.6],[14,22.5,6.4],n)]
   if n=='trench':garment+=[faced([4,12,2.2],[12,25,6.8],n,south=front),cube([3.5,top,2],[12.5,12.5,7],n),cube([3.8,11,1.9],[12.2,12.2,7.1],'boot'),cube([3.2,25.5,3.5],[5,26.5,5.5],n),cube([11,25.5,3.5],[12.8,26.5,5.5],n)]
   else:garment+=[faced([4,top,2.2],[12,25,6.8],n,south=front),cube([4.5,25.5,1.8],[11.5,26.8,6],'boot' if n=='barn' else n)]
   model(n,frame+garment);model(n+'_turned',frame)
  elif 'boots' in n:
   # Tall heavy leather lace-ups, black heeled ankle boots, tan work boots on a thick sole.
   pair=lambda make:[e for x in (0,8) for e in make(x)]
   if n=='wool_boots':elements=pair(lambda x:[cube([x+2,0,3],[x+7,1,13],'sole'),cube([x+2.2,1,3.2],[x+6.8,4,12.8],'boot'),cube([x+2.4,4,8],[x+6.6,11,12.6],'boot'),cube([x+3,4.2,7.9],[x+6,10,8.1],'cream')])
   elif n=='trench_boots':elements=pair(lambda x:[cube([x+3,0,10],[x+6,2.5,13],'boot_black'),cube([x+3,0,3],[x+6,1,6],'boot_black'),cube([x+2.5,1,3.2],[x+6.5,5,12.5],'boot_black'),cube([x+2.7,5,8],[x+6.3,8.5,12.3],'boot_black')])
   else:elements=pair(lambda x:[cube([x+1.5,0,2.5],[x+7,2,13],'sole'),cube([x+2,2,3],[x+6.5,7,12.5],'boot_tan'),cube([x+2.2,7,7],[x+6.3,8,12.3],'boot'),cube([x+2.8,3,2.9],[x+5.7,6.8,3.05],'cream')])
   model(n,elements);model(n+'_turned',[cube([2,0,3],[14,.5,13],'rubber')])
  elif n in ['mirror','mirror_top']:
   low,high=(0,15) if n=='mirror_top' else (1,16)
   elements=[cube([0,0,0],[1,16,2],'brass'),cube([15,0,0],[16,16,2],'brass'),cube([1,low,0],[15,high,.5],'oak'),cube([1,low,.55],[15,high,.7],'glass'),cube([1,15 if n=='mirror_top' else 0,0],[15,16 if n=='mirror_top' else 1,2],'brass')];model(n,elements);model(n+'_turned',[cube([0,0,0],[16,16,2],'oak')])
  elif n=='umbrella':
   elements=[cube([3,0,3],[13,1,13],'brass')]+[cube([x,1,z],[x+1,13,z+1],'brass') for x in [3,12] for z in [3,12]]+[cube([3,13,3],[13,14,4],'brass'),cube([3,13,12],[13,14,13],'brass'),cube([4,8,4],[12,8.4,12],'slip')];model(n,elements);elements[-1]=cube([4,8,4],[12,8.4,12],'slip_written');model(n+'_written',elements)
  elif n=='covered_mirror':model(n,[cube([0,0,.2],[16,16,2.2],'cream')])
  elif n=='radiator':model(n,[cube([x,1,0],[x+1.5,14,3],'metal') for x in [1,4,7,10,13]]+[cube([0,3,.5],[16,5,2.5],'metal'),cube([0,11,.5],[16,13,2.5],'metal')])
  elif n=='towel':model(n,[cube([2,0,2],[14,3,14],'cream'),cube([3,3.05,3],[13,4,13],'cream')])
  elif n=='curtain':model(n,[cube([0,0,1],[16,16,3],'curtain')])
  elif n=='rail':
   # 0.4.163 (owner: "other texture issue"): the last section of the white porch railing, by the steps: an end post and a
   # top rail reaching on into the next rail's post, as a fence joins, with the words carved along it. Below it stays open,
   # the gap a reader on the lawn looks through at the lit window.
   def section(top):return [cube([11,0,6],[15,16,10],'rail'),cube([-6,12,7],[12,15,9],top)]
   model(n,section('rail'),extra={'rail':RAIL});model(n+'_written',section('rail_written'),extra={'rail':RAIL})
  elif n=='swing':
   # 0.4.163 (owner: "swing textures are still disconnected"): two of these make one plank seat at sitting height, each
   # hung by its rope, which runs up into the rope above as the chain template does.
   rope=[{'from':[6.5,8,8],'to':[9.5,16,8],'rotation':{'origin':[8,8,8],'axis':'y','angle':45},'shade':False,'faces':{'north':{'uv':[0,0,3,8],'texture':'#rope'},'south':{'uv':[0,0,3,8],'texture':'#rope'}}},
         {'from':[8,8,6.5],'to':[8,16,9.5],'rotation':{'origin':[8,8,8],'axis':'y','angle':45},'shade':False,'faces':{'west':{'uv':[3,0,6,8],'texture':'#rope'},'east':{'uv':[3,0,6,8],'texture':'#rope'}}}]
   model(n,[cube([0,6,3],[16,8,13],'oak')]+rope,extra={'rope':ROPE})
  elif n=='mailbox':model(n,[cube([7,0,7],[9,13,9],'oak'),cube([1,13,3],[15,18,13],'metal'),cube([1,13,2.6],[15,17.8,2.9],'metal'),cube([14.8,15,6],[15.1,21,7],'curtain')])
  else:model(n,[cube([3,0,3],[13,.6,13],'cream'),cube([4,.65,4],[12,.9,12],'food')])
 more_models(out,model,cube);rooms160.models(model,cube,faced)
 parts+=EXTRA_PARTS+rooms160.PARTS
 out['textures/block/house_read/radar.png.mcmeta']=(json.dumps({'animation':{'frametime':8}},indent=2)+'\n').encode()
 out['textures/block/house_read/snow.png.mcmeta']=(json.dumps({'animation':{'frametime':2}},indent=2)+'\n').encode()
 variants={}
 for n in parts:
  for face,rot in [('south',0),('west',90),('north',180),('east',270)]:
   for turned in [False,True]:
    for writing in [False,True]:
     suffix='_turned' if turned and n in parts[:6]+['mirror','mirror_top'] else '_written' if writing and n in ['rail','umbrella'] else ''
     if n in ['glasses','phone','light_switch','tap','sink']+rooms160.TURNED and turned:suffix='_turned'
     if n in ['calendar','postcard','jukebox'] and writing:suffix='_written'
     if n.startswith('routine_') or n=='crt':
      if turned or writing:continue
      for selected in range(4):
       suffix=('_lifted_'+str(selected) if selected else '') if n.startswith('routine_') else ('_channel_'+str(selected) if selected else '')
       variants[f'part={n},facing={face},lifted={selected}']={'model':'the_oldest_house:block/read_'+n+suffix,'y':rot}
     else:variants[f'part={n},facing={face},turned={str(turned).lower()},writing={str(writing).lower()}']={'model':'the_oldest_house:block/read_'+n+suffix,'y':rot}
 out['blockstates/read_fitting.json']=(json.dumps({'variants':variants},indent=2)+'\n').encode()
 variants={}
 for half in ['lower','upper']:
  for hinge in ['left','right']:
   for opened in [False,True]:
    n=('bottom' if half=='lower' else 'top')+'_'+hinge+('_open' if opened else '')
    out['models/block/read_screen_'+n+'.json']=(json.dumps({'parent':'minecraft:block/door_'+n,'render_type':'minecraft:cutout','textures':{'bottom':textures['screen_bottom'],'top':textures['screen_top']}},indent=2)+'\n').encode()
    for face,rot in [('east',0),('south',90),('west',180),('north',270)]:variants[f'facing={face},half={half},hinge={hinge},open={str(opened).lower()}']={'model':'the_oldest_house:block/read_screen_'+n,'y':(rot+(90 if hinge=='left' else -90) if opened else rot)%360}
 out['blockstates/read_screen_door.json']=(json.dumps({'variants':variants},indent=2)+'\n').encode()
 for name in parts[:6]:out['models/item/read_'+name+'.json']=(json.dumps({'parent':'the_oldest_house:block/read_'+name,'display':{'gui':{'rotation':[20,-35,0],'translation':[0,-3,0],'scale':[.65,.65,.65]}}},indent=2)+'\n').encode()
 # 0.4.161: the worn coats and boots, each drawn to its own fitted mesh layout.
 for n,im in rooms160.worn().items():
  buf=io.BytesIO();im.save(buf,format='PNG');out['textures/entity/house_read/'+n+'.png']=buf.getvalue()
 # 0.4.160: flat sprites for everything held in the hand; an empty cone is its own picture.
 for name,im in rooms160.sprites().items():
  buf=io.BytesIO();im.save(buf,format='PNG');out['textures/item/read_'+name+'.png']=buf.getvalue()
 for name in rooms160.SPRITES:
  if name=='cone_empty':continue
  data={'parent':'minecraft:item/generated','textures':{'layer0':'the_oldest_house:item/read_'+name}}
  if name=='cone':data['overrides']=[{'predicate':{'custom_model_data':1},'model':'the_oldest_house:item/read_cone_empty'}]
  out['models/item/read_'+name+'.json']=(json.dumps(data,indent=2)+'\n').encode()
 out['models/item/read_cone_empty.json']=(json.dumps({'parent':'minecraft:item/generated','textures':{'layer0':'the_oldest_house:item/read_cone_empty'}},indent=2)+'\n').encode()
 # 0.4.160: the rooms' own building materials.
 for name,im in materials.textures().items():
  buf=io.BytesIO();im.save(buf,format='PNG');out['textures/block/house_read/'+name+'.png']=buf.getvalue()
 materials.blocks(out)
 # 0.4.159: the House's version of a reader wears these over their own skin, only in glass and copies.
 im=Image.new('RGBA',(64,32),(0,0,0,0));d=ImageDraw.Draw(im);frame='#2b2622'
 for x in [1,2,3,6,7,8]:d.point((x,1),fill=frame)
 d.line((4,1,5,1),fill='#6d5a3a')
 for x in [1,3,6,8]:d.point((x,2),fill=frame)
 for x in [0,9]:d.line((x,1,x,2),fill=frame)
 buf=io.BytesIO();im.save(buf,format='PNG');out['textures/entity/house_read/specs.png']=buf.getvalue()
 im=Image.new('RGBA',(64,32),(0,0,0,0));d=ImageDraw.Draw(im);rng=random.Random(159)
 for y in range(0,10):
  for x in range(0,18):v=34+rng.randrange(18);d.point((x,y),fill=(v,v-2,v-4,236))
 buf=io.BytesIO();im.save(buf,format='PNG');out['textures/entity/house_read/veil.png']=buf.getvalue()
 return out
def audit_models(out):
 for path,raw in out.items():
  if not path.startswith('models/block/'):continue
  elements=json.loads(raw).get('elements',[])
  for i,left in enumerate(elements):
   for right in elements[i+1:]:
    for axis in range(3):
     for side in ['from','to']:
      if abs(left[side][axis]-right[side][axis])>1e-8:continue
      others=[a for a in range(3) if a!=axis]
      if all(min(left['to'][a],right['to'][a])-max(left['from'][a],right['from'][a])>1e-8 for a in others):raise AssertionError(f'Overlapping coplanar exterior faces: {path}, axis {axis}, {side}')
def geometry(out):
 parts=['wool','trench','barn','wool_boots','trench_boots','barn_boots','towel','mirror','mirror_top','umbrella','radiator','curtain','rail','swing','mailbox','plate','covered_mirror']+EXTRA_PARTS+rooms160.PARTS
 def array(elements):return 'new double[][]{'+','.join('{'+','.join(str(v) for v in e['from']+e['to'])+'}' for e in elements)+'}'
 lines=['package io.github.knaitoe.theoldesthouse.labyrinth;','', '// Generated by tools/draw_house_read.py from the exact shipped model cuboids.', 'import java.util.EnumMap;', 'import net.minecraft.world.level.block.Block;', 'import net.minecraft.world.level.block.HorizontalDirectionalBlock;', 'import net.minecraft.world.level.block.state.BlockState;', 'import net.minecraft.world.phys.shapes.*;', 'import static io.github.knaitoe.theoldesthouse.labyrinth.HouseReadPropBlock.*;', '', 'public final class HouseReadGeometry {', '    private static final EnumMap<Part,VoxelShape[]> SHAPES=new EnumMap<>(Part.class);', '    static {']
 for name in parts:
  normal=json.loads(out['models/block/read_'+name+'.json'])['elements'];turned=json.loads(out.get('models/block/read_'+name+'_turned.json',out['models/block/read_'+name+'.json']))['elements'];lines.append('        add(Part.'+name.upper()+','+array(normal)+','+array(turned)+');')
 for name in ['routine_a','routine_b','routine_c','routine_d']:
  for index in range(1,4):lines.append('        lifted(Part.'+name.upper()+','+str(index)+','+array(json.loads(out['models/block/read_'+name+'_lifted_'+str(index)+'.json'])['elements'])+');')
 lines+=['    }', '    private HouseReadGeometry(){}', '    private static void add(Part part,double[][] normal,double[][] turned){', '        var states=new VoxelShape[8];', '        for(int state=0;state<8;state++){var result=Shapes.empty();for(var v:(state<4?normal:turned)){double x=v[0],y=v[1],z=v[2],X=v[3],Y=v[4],Z=v[5];for(int n=0;n<state%4;n++){double next=16-Z,NEXT=16-z;z=x;Z=X;x=next;X=NEXT;}result=Shapes.or(result,Block.box(x,y,z,X,Y,Z));}states[state]=result.optimize();}', '        SHAPES.put(part,states);', '    }', '    private static void lifted(Part part,int index,double[][] cubes){var states=SHAPES.get(part);if(states.length<32)states=java.util.Arrays.copyOf(states,32);for(int r=0;r<4;r++){var shape=Shapes.empty();for(var v:cubes){double x=v[0],y=v[1],z=v[2],X=v[3],Y=v[4],Z=v[5];for(int n=0;n<r;n++){double next=16-Z,NEXT=16-z;z=x;Z=X;x=next;X=NEXT;}shape=Shapes.or(shape,Block.box(x,y,z,X,Y,Z));}states[index*8+r]=states[index*8+4+r]=shape.optimize();}SHAPES.put(part,states);}',
 '    public static VoxelShape shape(BlockState state){int rotation=switch(state.getValue(HorizontalDirectionalBlock.FACING)){case WEST->1;case NORTH->2;case EAST->3;default->0;};var states=SHAPES.get(state.getValue(PART));return states[rotation+(state.getValue(TURNED)?4:0)+(states.length>8?state.getValue(LIFTED)*8:0)];}', '}', '']
 return '\n'.join(lines).encode()
if __name__=='__main__':
 p=argparse.ArgumentParser(description=__doc__);p.add_argument('--check',action='store_true');args=p.parse_args();out=outputs();audit_models(out)
 for path,raw in out.items():
  dest=A/path
  if args.check:assert dest.read_bytes()==raw,path
  else:dest.parent.mkdir(parents=True,exist_ok=True);dest.write_bytes(raw)
 dest=ROOT/'src/main/java/io/github/knaitoe/theoldesthouse/labyrinth/HouseReadGeometry.java';raw=geometry(out)
 if args.check:assert dest.read_bytes()==raw,'shared native cuboids'
 else:dest.write_bytes(raw)
 print(f'{len(out)} native resources and shared collision match; no overlapping coplanar exterior faces.' if args.check else f'Wrote {len(out)} native resources and shared collision.')
