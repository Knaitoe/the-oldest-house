"""0.4.160: the owner's playtest notes on the quiet rooms, in native sixteen-pixel art.

Tables, a desk placard, a wall clock and an outlet for the waiting room; a vanity, tub, toilet, towel bar and lights for
the bathroom; three coats and three pairs of boots that look nothing alike for the coatroom, with hangers, a scarf, a bench
and a runner; a porch light, chairs and a lamp for the porch; deck lanterns for the shelter. Every wall fitting is drawn
against the back of its own cell (z = 0), so it hangs on the wall behind it. Held objects get flat item sprites.
"""
from PIL import Image, ImageDraw
import io, json, random

PARTS=['table_end','table_mid','sign','wall_clock','outlet','ceiling_panel','vanity','tub_head','tub_foot','toilet','towel_bar','vanity_light',
       'bath_mat','empty_hanger','scarf_hook','bench','pendant','runner','porch_light','chair','lamp','deck_lantern','side_table']
LIGHTS=['ceiling_panel','vanity_light','pendant','porch_light','lamp','deck_lantern']
# Parts drawn differently when TURNED: a light switched off.
TURNED=['vanity_light','pendant','porch_light','lamp','deck_lantern','towel_bar']
SPRITES=['paper','magazine','phone','cone','cone_empty']+[f'routine_{i}' for i in range(16)]

def _rng(name):return random.Random(1600+sum(map(ord,name))*13)
def _fill(mid,name,specks,count):
 im=Image.new('RGBA',(16,16),mid);d=ImageDraw.Draw(im);rng=_rng(name)
 for _ in range(count):d.point((rng.randrange(16),rng.randrange(16)),fill=rng.choice(specks))
 return im
def tiles(out):
 out['tabletop']=_fill('#9c7a55','tabletop',['#8c6c4a','#ad8a63','#93724f'],60)
 d=ImageDraw.Draw(out['tabletop'])
 for y in [4,9,13]:d.line((0,y,15,y),fill='#87684a')
 im=Image.new('RGBA',(16,16),'#efe8d4');d=ImageDraw.Draw(im);d.rectangle((0,0,15,15),outline='#7d6a4c');d.rectangle((1,1,14,14),outline='#d7cdb2')
 # A printed reception notice: a bold heading over three short lines of small type, a rule between.
 d.line((3,3,12,3),fill='#3b3a40');d.line((4,4,11,4),fill='#3b3a40');d.line((3,6,12,6),fill='#b4a98e')
 for y,(x0,x1) in zip((8,10,12),((3,12),(3,10),(4,11))):
  for x in range(x0,x1+1):
   if (x*7+y)%5:d.point((x,y),fill='#6a6660')
 out['placard']=im
 im=Image.new('RGBA',(16,16),(0,0,0,0));d=ImageDraw.Draw(im);d.ellipse((0,0,15,15),fill='#2f3133');d.ellipse((1,1,14,14),fill='#f2f0e8')
 for a,(x,y) in enumerate([(8,2),(13,8),(8,13),(2,8)]):d.point((x,y),fill='#2f3133')
 for x,y in [(11,3),(13,5),(13,11),(11,13),(5,13),(3,11),(3,5),(5,3)]:d.point((x,y),fill='#8a8a86')
 out['wallclock']=im
 im=Image.new('RGBA',(16,16),'#e9e4d6');d=ImageDraw.Draw(im);d.rectangle((0,0,15,15),outline='#c7c0ad')
 for y in [3,9]:d.rectangle((5,y,6,y+2),fill='#3a3a38');d.rectangle((9,y,10,y+2),fill='#3a3a38');d.point((7,y+3),fill='#3a3a38');d.point((8,y+3),fill='#3a3a38')
 out['outlet']=im
 im=Image.new('RGBA',(16,16),'#f4f3ec');d=ImageDraw.Draw(im)
 for x in range(1,16,3):d.line((x,0,x,15),fill='#dfe3e0')
 d.rectangle((0,0,15,15),outline='#bdbab0');out['panel_light']=im
 im=Image.new('RGBA',(16,16),'#e6e1d3');d=ImageDraw.Draw(im);d.rectangle((1,1,7,14),outline='#b9b2a0');d.rectangle((8,1,14,14),outline='#b9b2a0');d.point((6,7),fill='#9a8a5a');d.point((9,7),fill='#9a8a5a');out['vanity_front']=im
 out['countertop']=_fill('#d9d6cf','countertop',['#c6c3bb','#e7e4dc','#b9b6ae'],70)
 out['enamel']=_fill('#eeece6','enamel',['#e1ded6','#f7f5ef'],30)
 im=Image.new('RGBA',(16,16),'#b9bec2');d=ImageDraw.Draw(im);d.line((0,3,15,3),fill='#e7eaec');d.line((0,11,15,11),fill='#8d9296');out['chrome']=im
 im=_fill('#5d7f99','towel_blue',['#53748d','#6a8ca5','#4b6a81'],60);d=ImageDraw.Draw(im);d.line((0,13,15,13),fill='#e5e2d7');d.line((0,14,15,14),fill='#e5e2d7');out['towel_blue']=im
 # 0.4.167: the bath rug's weave. Chenille rows of two blues under a cream border with a band of rust, a few loose loops.
 im=Image.new('RGBA',(16,16),'#557790');d=ImageDraw.Draw(im);rng=_rng('bath_rug')
 for y in range(16):
  for x in range(16):
   if (x+y*3)%4==0:d.point((x,y),fill='#4b6a81')
   elif (x*5+y)%7==0:d.point((x,y),fill='#6a8ca5')
 for y in range(2,14,3):d.line((2,y,13,y),fill='#4d6d85')
 # Each half of the rug is this sheet, the right half mirrored, so the border runs round the outer end, top and bottom
 # only, and the two halves meet in the middle as one rug.
 for k,colour in [(1,'#e5e2d7'),(2,'#a85f43'),(3,'#d9d3c2')]:d.line((k,k,15,k),fill=colour);d.line((k,13-k,15,13-k),fill=colour);d.line((k,k,k,13-k),fill=colour)
 for x,y in [(5,6),(9,5),(11,8),(6,9)]:d.point((x,y),fill='#8aa8bc')
 out['bath_rug']=im
 im=Image.new('RGBA',(16,16),(0,0,0,0));d=ImageDraw.Draw(im)
 for y in range(2,14):d.point((0,y),fill='#e5e2d7' if y%2 else '#cfc8b4');d.point((1,y),fill='#d9d3c2' if y%2 else (0,0,0,0))
 out['bath_rug_fringe']=im
 im=Image.new('RGBA',(16,16),'#f6efd2');d=ImageDraw.Draw(im)
 for x in [2,7,12]:d.ellipse((x-1,5,x+2,9),fill='#fffbe8',outline='#e8d9a2')
 out['bulb']=im
 # Three coats that cannot be mistaken for one another: charcoal wool with lapels, a belted tan trench, a waxed olive
 # barn jacket with a corduroy collar.
 im=_fill('#3f3f42','wool_front',['#36363a','#47474b','#323235'],60);d=ImageDraw.Draw(im)
 d.polygon([(5,0),(8,6),(7,0)],fill='#2b2b2e');d.polygon([(11,0),(8,6),(9,0)],fill='#2b2b2e');d.line((8,6,8,15),fill='#2a2a2c')
 for y in [7,10,13]:d.point((6,y),fill='#1d1d1f');d.point((10,y),fill='#1d1d1f')
 out['wool_front']=im
 im=_fill('#b39a6f','trench_front',['#a68e64','#c0a77c','#9d865d'],50);d=ImageDraw.Draw(im)
 d.polygon([(4,0),(8,5),(6,0)],fill='#8f7a55');d.polygon([(12,0),(8,5),(10,0)],fill='#8f7a55')
 for y in [5,8]:d.point((5,y),fill='#5d4a30');d.point((11,y),fill='#5d4a30');d.point((6,y),fill='#5d4a30');d.point((10,y),fill='#5d4a30')
 d.rectangle((0,9,15,10),fill='#8a7450');d.rectangle((7,9,9,10),fill='#d1c08f');d.point((8,9),fill='#6a5636');out['trench_front']=im
 im=_fill('#5d6447','barn_front',['#535a3f','#676e51','#4c5239'],50);d=ImageDraw.Draw(im)
 d.rectangle((3,0,12,2),fill='#6b4a2e')
 for x in range(3,13,2):d.point((x,1),fill='#5a3d25')
 d.line((8,3,8,15),fill='#42482f');d.rectangle((2,9,6,13),outline='#454b33');d.rectangle((10,9,14,13),outline='#454b33')
 for y in [5,8,11,14]:d.point((8,y),fill='#b49a5d')
 out['barn_front']=im
 out['boot_black']=_fill('#2a2626','boot_black',['#221f1f','#3a3434','#463f3f'],40)
 out['boot_tan']=_fill('#a8834f','boot_tan',['#987547','#b8935c','#8a6a40'],50)
 out['sole']=_fill('#3b3530','sole',['#2f2a26','#4a433c'],30)
 im=_fill('#9b3b35','scarf',['#8b332e','#ab4540','#7d2c28'],60);d=ImageDraw.Draw(im)
 for y in [3,7,11]:d.line((0,y,15,y),fill='#c9b48a')
 out['scarf']=im
 im=_fill('#6f5a7a','cushion',['#655170','#7b6687','#5b4866'],50);d=ImageDraw.Draw(im);d.line((0,0,15,0),fill='#856f91');out['cushion']=im
 im=Image.new('RGBA',(16,16),'#e8dcc0');d=ImageDraw.Draw(im)
 for y in range(0,16,4):d.line((0,y,15,y),fill='#d8c9a6')
 d.line((0,15,15,15),fill='#bba985');out['lampshade']=im
 im=Image.new('RGBA',(16,16),'#7b3f36');d=ImageDraw.Draw(im);d.rectangle((0,0,15,15),outline='#3e2b26');d.rectangle((2,2,13,13),outline='#c79a5b')
 d.polygon([(8,4),(11,8),(8,12),(5,8)],outline='#d9c08a');d.point((8,8),fill='#2f4a5c');out['runner']=im
 # Lightly frosted reception glass: the calendar behind it shows through.
 im=Image.new('RGBA',(16,16),(214,226,226,110));d=ImageDraw.Draw(im)
 for x,y in [(3,4),(9,2),(12,9),(5,12),(7,7)]:d.point((x,y),fill=(240,246,244,150))
 out['frosted']=im
 im=Image.new('RGBA',(16,16),(244,226,170,200));d=ImageDraw.Draw(im);d.rectangle((0,0,15,15),outline='#3b3a36');d.line((8,0,8,15),fill='#3b3a36');out['lamp_glass']=im
 # What the waiting room's reading matter is about: the county paper's masthead and columns, the garden magazine's pears.
 im=Image.new('RGBA',(16,16),'#d8d2bf');d=ImageDraw.Draw(im);d.rectangle((1,1,14,3),fill='#2e2b26')
 for y in range(5,15,2):d.line((1,y,7,y),fill='#8f897a');d.line((9,y,14,y),fill='#8f897a')
 d.rectangle((9,5,14,9),fill='#9a9384');out['newsprint']=im
 im=Image.new('RGBA',(16,16),'#7f9a5c');d=ImageDraw.Draw(im);d.rectangle((0,0,15,3),fill='#e9e2c8');d.line((2,1,13,1),fill='#4f6b39');d.line((2,2,9,2),fill='#4f6b39')
 for x,y in [(4,9),(10,10)]:d.ellipse((x-2,y-2,x+2,y+3),fill='#c9b552');d.point((x,y-3),fill='#5a3d22')
 d.polygon([(6,13),(9,6),(11,13)],fill='#4f7a3d');d.line((0,15,15,15),fill='#5d7342');out['magazine_cover']=im
 return out

def models(model,cube,faced):
 C=cube;M=model
 M('table_end',[C([0,14,0],[16,16,16],'tabletop'),C([1,0,1],[3,14,3],'oak'),C([13,0,1],[15,14,3],'oak'),C([1,12,3],[3,14,15],'oak'),C([13,12,3],[15,14,15],'oak')])
 M('table_mid',[C([0,14,0],[16,16,16],'tabletop'),C([0,12,1],[16,14,3],'oak'),C([0,12,13],[16,14,15],'oak')])
 M('sign',[C([3,0,6],[13,1,10],'oak'),C([2,1,7.5],[14,11,8.5],'placard'),C([7,1,8.5],[9,6,10],'oak')])
 M('wall_clock',[faced([2,2,0],[14,14,1.5],'metal',south='wallclock')])
 M('outlet',[faced([6,3,0],[10,9,.4],'cream',south='outlet'),C([7,4,.4],[9,6,1.6],'rubber'),C([7.6,0,1],[8.4,4,1.6],'rubber')])
 M('ceiling_panel',[faced([0,0,0],[16,16,16],'cream',down='panel_light')])
 M('vanity',[faced([0,0,1],[16,14,16],'cream',south='vanity_front'),C([0,14,0],[16,16,16],'countertop')])
 sink=[faced([0,0,1],[16,13,16],'cream',south='vanity_front'),C([0,13,0],[16,16,3],'countertop'),C([0,13,13],[16,16,16],'countertop'),C([0,13,3],[3,16,13],'countertop'),C([13,13,3],[16,16,13],'countertop'),C([3,13,3],[13,13.5,13],'enamel'),
       C([7,16,1],[9,22,3],'chrome'),C([7,20.5,3],[9,22,8],'chrome'),C([4,16,1],[6,17.5,3],'chrome'),C([10,16,1],[12,17.5,3],'chrome')]
 M('sink',sink);M('sink_turned',sink+[C([7.5,13.6,6.5],[8.5,20.4,7.5],'glass')])
 tub=lambda head:[C([0,0,0],[16,10,2],'enamel'),C([0,0,14],[16,10,16],'enamel'),C([0,0,2],[16,2,14],'enamel')]+([C([14,2,2],[16,10,14],'enamel'),C([12,11,6],[14,12,10],'chrome'),C([13,10,7],[15,13,9],'chrome')] if head else [C([0,2,2],[2,10,14],'enamel')])
 M('tub_head',tub(True));M('tub_foot',tub(False))
 M('toilet',[C([4,0,4],[12,5,13],'enamel'),C([3.5,5,4],[12.5,6.5,13.5],'enamel'),C([3,6,0],[13,15,4],'enamel'),C([3,15,0],[13,16,4.5],'enamel'),C([12,12,1],[14,13,2],'chrome')])
 bar=[C([2,11,0],[3,12,2],'chrome'),C([13,11,0],[14,12,2],'chrome'),C([2,12,1],[14,13,2],'chrome')]
 M('towel_bar',bar+[C([3,3,1.6],[13,12,2.6],'towel_blue')]);M('towel_bar_turned',bar)
 M('vanity_light',[faced([2,2,0],[14,5,2],'chrome',south='bulb',down='bulb')]);M('vanity_light_turned',[faced([2,2,0],[14,5,2],'chrome',south='enamel',down='enamel')])
 # 0.4.167 (owner: "rug texture can be expanded too"): a woven bath rug, nearly two blocks long, with a border and fringe.
 M('bath_mat',[{'from':[-6,0,2],'to':[8,.5,14],'faces':{f:{'uv':[1,1,15,13],'texture':'#bath_rug'} for f in ['up','down','north','south','west','east']}},
               {'from':[8,0,2],'to':[22,.5,14],'faces':{f:{'uv':[15,1,1,13],'texture':'#bath_rug'} for f in ['up','down','north','south','west','east']}},
               {'from':[-7.5,0,3],'to':[-6,.25,13],'faces':{f:{'uv':[0,2,2,14],'texture':'#bath_rug_fringe'} for f in ['up','down','north','south','west','east']}},
               {'from':[22,0,3],'to':[23.5,.25,13],'faces':{f:{'uv':[2,2,0,14],'texture':'#bath_rug_fringe'} for f in ['up','down','north','south','west','east']}}],'cutout')
 hook=[C([7,26,0],[9,27,3],'brass'),C([7,27,2],[9,29,3],'brass')]
 M('empty_hanger',hook+[C([3,24,1.5],[13,25,3.5],'oak'),C([7.5,25,2.1],[8.5,26.9,2.9],'metal')])
 M('scarf_hook',hook+[C([5,6,2],[8,26.5,3.5],'scarf'),C([8,10,2.2],[11,26.5,3.6],'scarf'),C([4,26.8,1],[12,29.6,7],'wool'),C([3,26.2,0.5],[13,26.8,7.5],'wool')])
 M('bench',[C([0,6,3],[16,8,13],'oak'),C([0,8,3.5],[16,9.5,12.5],'cushion'),C([1,0,4],[3,6,12],'oak'),C([13,0,4],[15,6,12],'oak')])
 pendant=lambda lit:[C([7.5,9,7.5],[8.5,16,8.5],'rubber'),C([4,3,4],[12,9,12],'lampshade'),C([6.5,2,6.5],[9.5,3,9.5],'bulb' if lit else 'enamel')]
 M('pendant',pendant(True));M('pendant_turned',pendant(False))
 M('runner',[C([0,0,1],[16,.4,15],'runner')])
 M('side_table',[C([1,14,1],[15,16,15],'tabletop'),C([2,0,2],[4,14,4],'oak'),C([12,0,2],[14,14,4],'oak'),C([2,0,12],[4,14,14],'oak'),C([12,0,12],[14,14,14],'oak'),C([4,5,3],[12,6,13],'oak')])
 sconce=lambda lit:[C([6,4,0],[10,6,2],'metal'),C([7,6,1],[9,7,4],'metal'),C([5.5,7,2],[10.5,12,7],'lamp_glass' if lit else 'glass'),C([5,12,1.5],[11,13,7.5],'metal')]
 M('porch_light',sconce(True));M('porch_light_turned',sconce(False))
 M('chair',[C([3,8,3],[13,10,13],'oak'),C([3,0,3],[5,8,5],'oak'),C([11,0,3],[13,8,5],'oak'),C([3,0,11],[5,8,13],'oak'),C([11,0,11],[13,8,13],'oak'),C([3,10,11],[13,21,13],'oak')])
 lamp=lambda lit:[C([5,0,5],[11,1,11],'brass'),C([7.5,1,7.5],[8.5,7.5,8.5],'brass'),C([4,8,4],[12,14,12],'lampshade'),C([7,7,7],[9,8,9],'bulb' if lit else 'enamel')]
 M('lamp',lamp(True));M('lamp_turned',lamp(False))
 lantern=lambda lit:[C([7.5,12,7.5],[8.5,16,8.5],'metal'),C([5,10,5],[11,12,11],'metal'),C([5.5,3,5.5],[10.5,10,10.5],'lamp_glass' if lit else 'glass'),C([5,2,5],[11,3,11],'metal')]
 M('deck_lantern',lantern(True));M('deck_lantern_turned',lantern(False))

def sprites():
 """Flat item sprites, so what a reader picks up shows in their hand the way any held item does."""
 out={}
 def new():return Image.new('RGBA',(16,16),(0,0,0,0))
 im=new();d=ImageDraw.Draw(im);d.polygon([(2,4),(13,2),(14,12),(3,14)],fill='#d8d2bf',outline='#8f897a');d.line((4,5,12,4),fill='#2e2b26');d.line((4,6,12,5),fill='#2e2b26')
 for y in range(8,13,2):d.line((4,y,8,y-1),fill='#8f897a');d.line((9,y-1,12,y-2),fill='#8f897a')
 out['paper']=im
 im=new();d=ImageDraw.Draw(im);d.rectangle((3,2,12,14),fill='#7f9a5c',outline='#4f6b39');d.rectangle((3,2,12,4),fill='#e9e2c8');d.ellipse((5,7,8,11),fill='#c9b552');d.ellipse((8,8,11,12),fill='#c9b552');out['magazine']=im
 im=new();d=ImageDraw.Draw(im);d.rounded_rectangle((5,1,11,14),2,fill='#2b2d30',outline='#141516');d.rectangle((6,3,10,11),fill='#5d7f99');d.line((7,4,9,4),fill='#a9c2d3');d.point((8,13),fill='#6c6f73');out['phone']=im
 for name,water in [('cone',True),('cone_empty',False)]:
  im=new();d=ImageDraw.Draw(im);d.polygon([(3,3),(12,3),(8,14)],fill='#f1ece0',outline='#bdb6a6')
  if water:d.polygon([(4,4),(11,4),(9,8),(7,8)],fill='#a9c9d6')
  out[name]=im
 # The twelve routine items, by their sets: silk, curl cream, pick; oil, comb, ties; brush, dry shampoo, clip; pocket comb, pomade, washcloth.
 def draw(i,d):
  if i==0:d.polygon([(2,10),(6,4),(12,5),(14,11),(8,13)],fill='#b98a86',outline='#8e6764');d.line((5,9,11,8),fill='#d6aaa5')
  elif i==1:d.rectangle((4,5,11,13),fill='#efe6cf',outline='#b9ad8f');d.rectangle((4,3,11,5),fill='#859683');d.line((5,8,10,8),fill='#b19a6a')
  elif i==2:d.rectangle((6,8,9,14),fill='#2f2f31');[d.line((x,2,x,8),fill='#2f2f31') for x in (4,6,8,10,12)];d.line((4,8,12,8),fill='#2f2f31')
  elif i==3:d.rectangle((5,5,10,14),fill='#ac8046',outline='#725837');d.rectangle((6,2,9,5),fill='#3a3a38');d.rectangle((6,9,9,11),fill='#cec8ac')
  elif i==4:d.line((2,13,13,2),fill='#3a3a38',width=2);[d.point((x,15-x),fill='#3a3a38') for x in range(3,8)];d.line((10,5,14,1),fill='#3a3a38')
  elif i==5:d.ellipse((2,6,8,12),outline='#3a3a38');d.ellipse((7,4,13,10),outline='#a77f7b');d.ellipse((5,9,11,15),outline='#5d7f99')
  elif i==6:d.ellipse((4,1,12,9),fill='#8e6e43',outline='#5f4b33');d.rectangle((7,9,9,15),fill='#8e6e43');[d.point((x,y),fill='#2f2f31') for x in (6,8,10) for y in (3,5,7)]
  elif i==7:d.rectangle((5,4,10,14),fill='#dbd8c2',outline='#aaa98f');d.rectangle((6,1,9,4),fill='#3a3a38');d.rectangle((6,8,9,10),fill='#a77f7b')
  elif i==8:d.rectangle((3,5,7,11),fill='#ac8046',outline='#725837');d.rectangle((8,5,12,11),fill='#ac8046',outline='#725837');d.rectangle((6,7,9,9),fill='#a18b51')
  elif i==9:d.rectangle((2,6,13,9),fill='#2b2b2b');[d.point((x,10),fill='#2b2b2b') for x in range(3,13,2)]
  elif i==10:d.ellipse((2,4,13,12),fill='#9b9d91',outline='#727970');d.ellipse((4,5,11,10),fill='#c2c0ae');d.line((5,8,10,8),fill='#3a3a38')
  elif i==11:d.rectangle((3,4,12,13),fill='#efe5c7',outline='#afa990');[d.line((3,y,12,y),fill='#d8d1b5') for y in (6,9,12)]
  # 0.4.167: a satin bonnet, an edge brush, a tray of hot rollers, a wave brush.
  elif i==12:d.ellipse((2,4,13,13),fill='#c79690',outline='#946865');d.arc((3,5,12,12),200,340,fill='#e2bcb5');d.line((2,11,13,11),fill='#efe5c7')
  elif i==13:d.line((3,13,9,7),fill='#2f2f31',width=1);d.rectangle((9,3,13,7),fill='#2f2f31');[d.point((x,y),fill='#d8d1b5') for x in (10,12) for y in (4,6)]
  elif i==14:d.rectangle((2,9,13,13),fill='#b98a86',outline='#8e6764');[d.ellipse((2+k*4,4,5+k*4,9),fill=('#efe6cf' if k%2==0 else '#ac8046'),outline='#7c6b55') for k in range(3)]
  else:d.ellipse((3,3,12,13),fill='#8e6e43',outline='#5f4b33');d.ellipse((4,4,11,12),fill='#2f2f31');[d.point((x,y),fill='#6b6b6b') for x in (6,8,10) for y in (6,8,10)]
 for i in range(16):im=new();draw(i,ImageDraw.Draw(im));out[f'routine_{i}']=im
 return out


# 0.4.161, the owner's playtest notes ("The clothes all look overly similar too."): the worn layers match the coats on the
# hooks. Each box of the fitted coat mesh (HouseReadClothes.layer) has its own region of a 64x32 sheet; faces are laid out
# as Minecraft lays out every box: top and bottom above, then right side, front, left side and back.
def _faces(u,v,dx,dy,dz):
 r=lambda x0,y0,w,h:(int(x0),int(y0),max(int(x0),int(round(x0+w))-1),max(int(y0),int(round(y0+h))-1))
 return {'top':r(u+dz,v,dx,dz),'bottom':r(u+dz+dx,v,dx,dz),'right':r(u,v+dz,dz,dy),'front':r(u+dz,v+dz,dx,dy),'left':r(u+dz+dx,v+dz,dz,dy),'back':r(u+2*dz+dx,v+dz,dx,dy)}
def _cloth(d,box,mid,dark,light,rng,weave):
 for name,(x0,y0,x1,y1) in box.items():
  d.rectangle((x0,y0,x1,y1),fill=mid)
  for y in range(y0,y1+1):
   for x in range(x0,x1+1):
    k=rng.random()
    if weave=='twill' and (x+y)%3==0 and k<.55:d.point((x,y),fill=dark)
    elif weave=='wool' and k<.18:d.point((x,y),fill=dark if k<.09 else light)
    elif weave=='canvas' and (x%2==0)!=(y%2==0) and k<.35:d.point((x,y),fill=dark)
    elif weave=='leather' and k<.07:d.point((x,y),fill=light)
# The mesh: body (0,0) 8.7x13x4.7, sleeves (28,0) 4.7 or 3.7 x12.5x5, hem (0,18) 9x5.6x4.9, collar (28,18) 8.9x3.7x9.1 for the
# barn jacket, or the trench's belt (28,18) 9x0.9x5 on the same sheet.
BODY=_faces(0,0,8.7,13,4.7);ARM=_faces(28,0,4.7,12.5,5);SLIM_ARM=_faces(28,0,3.7,12.5,5);HEM=_faces(0,18,9,5.6,4.9);COLLAR=_faces(28,18,8.9,3.7,9.1);BELT=_faces(28,18,9,.9,5)
LEG=_faces(0,18,4.7,4.5,5)
def worn():
 sheets={}
 palettes={'wool':('#4b4743','#38352f','#5f5a53','wool'),'trench':('#b9a07a','#93795a','#d4bd94','twill'),'barn':('#6f7b5c','#4f5a41','#8b9677','canvas')}
 for kind,(mid,dark,light,weave) in palettes.items():
  im=Image.new('RGBA',(64,32),(0,0,0,0));d=ImageDraw.Draw(im);rng=_rng('worn_'+kind)
  boxes=[BODY,ARM,SLIM_ARM]+([COLLAR] if kind=='barn' else [HEM])+([BELT] if kind=='trench' else [])
  for box in boxes:_cloth(d,box,mid,dark,light,rng,weave)
  fx0,fy0,fx1,fy1=BODY['front'];cx=(fx0+fx1)//2;bx0,by0,bx1,by1=BODY['back']
  hx0,hy0,hx1,hy1=HEM['front'];kx0,ky0,kx1,ky1=HEM['back']
  if kind=='wool':
   # A single-breasted charcoal overcoat: notched lapels, a hidden placket, flap pockets low, a back vent in the hem.
   for i in range(4):d.point((cx-1-i,fy0+i),fill=light);d.point((cx+1+i,fy0+i),fill=light)
   d.line((cx,fy0+4,cx,fy1),fill=dark);d.line((cx,hy0,cx,hy1),fill=dark)
   for y in (fy0+6,fy0+9):d.point((cx+1,y),fill='#24221f')
   d.line((fx0+1,fy1-2,fx0+3,fy1-2),fill=dark);d.line((fx1-3,fy1-2,fx1-1,fy1-2),fill=dark)
   d.line(((kx0+kx1)//2,ky0,(kx0+kx1)//2,ky1),fill=dark);d.line((bx0+1,by0+1,bx1-1,by0+1),fill=dark)
   d.line((hx0,hy1,hx1,hy1),fill='#2e2b28');d.line((kx0,ky1,kx1,ky1),fill='#2e2b28')
   cuff='#3e3a35'
  elif kind=='trench':
   # A tan double-breasted trench: wide lapels, two rows of buttons, a storm flap at the chest and a back yoke.
   for i in range(5):d.point((cx-1-i,fy0+i),fill=light);d.point((cx+1+i,fy0+i),fill=light)
   d.line((cx-2,fy0+5,cx-2,fy1),fill=dark)
   for y in (fy0+6,fy0+9,hy0+1):
    for x in (cx-3,cx+2):d.point((x,y),fill='#4a3b2a')
   d.rectangle((fx1-3,fy0+1,fx1-1,fy0+4),fill=light);d.line((fx1-3,fy0+4,fx1-1,fy0+4),fill=dark)
   d.rectangle((bx0+1,by0,bx1-1,by0+3),fill=light);d.line((bx0+1,by0+3,bx1-1,by0+3),fill=dark)
   d.line((hx0+1,hy0,hx0+1,hy1),fill=dark);d.line((hx0,hy1,hx1,hy1),fill=dark);d.line((kx0,ky1,kx1,ky1),fill=dark)
   # Epaulettes along the tops of the sleeves, and the belt in a darker twill with its buckle at the front.
   for arm in (ARM,SLIM_ARM):x0,y0,x1,y1=arm['top'];d.rectangle((x0,y0+1,x1,y0+2),fill=dark);d.point((x0+1,y0+1),fill='#4a3b2a')
   for name,(x0,y0,x1,y1) in BELT.items():d.rectangle((x0,y0,x1,y1),fill='#93795a')
   x0,y0,x1,y1=BELT['front'];d.rectangle(((x0+x1)//2-1,y0,(x0+x1)//2+1,y1),fill='#5a4632')
   cuff=dark
  else:
   # An olive barn jacket: snap placket, two patch pockets with flaps, a brown corduroy collar and cuffs.
   d.line((cx,fy0,cx,fy1),fill=dark)
   for y in range(fy0+1,fy1,3):d.point((cx+1,y),fill='#b49a5a')
   for x0 in (fx0+1,fx1-3):d.rectangle((x0,fy1-5,x0+2,fy1-1),outline=dark);d.line((x0,fy1-5,x0+2,fy1-5),fill='#3f4834')
   d.line(((bx0+bx1)//2,by0,(bx0+bx1)//2,by1),fill=dark);d.line((fx0,fy1,fx1,fy1),fill='#3f4834');d.line((bx0,by1,bx1,by1),fill='#3f4834')
   for name,(x0,y0,x1,y1) in COLLAR.items():
    d.rectangle((x0,y0,x1,y1),fill='#6e4c31')
    for x in range(x0,x1+1,2):d.line((x,y0,x,y1),fill='#5a3d27')
   cuff='#6e4c31'
  for arm in (ARM,SLIM_ARM):
   for name in ('front','back','left','right'):x0,y0,x1,y1=arm[name];d.rectangle((x0,y1-1,x1,y1),fill=cuff)
   x0,y0,x1,y1=arm['bottom'];d.rectangle((x0,y0,x1,y1),fill=cuff)
  sheets[kind]=im
 # The boots: tall brown lace-ups, black heeled ankle boots, tan work boots with a thick sole.
 for kind,(mid,dark,light,sole) in {'wool_boots':('#5a4330','#3e2e20','#7a5c40','#2a221b'),'trench_boots':('#232120','#151413','#3a3735','#0e0d0d'),'barn_boots':('#a57a48','#7c5a34','#c49a63','#4a3a2a')}.items():
  im=Image.new('RGBA',(64,32),(0,0,0,0));d=ImageDraw.Draw(im);rng=_rng('worn_'+kind)
  _cloth(d,LEG,mid,dark,light,rng,'leather')
  for name in ('front','back','left','right'):x0,y0,x1,y1=LEG[name];d.rectangle((x0,y1-(2 if kind=='barn_boots' else 1),x1,y1),fill=sole)
  x0,y0,x1,y1=LEG['bottom'];d.rectangle((x0,y0,x1,y1),fill=sole)
  x0,y0,x1,y1=LEG['front'];c=(x0+x1)//2
  if kind=='trench_boots':
   d.point((c,y0+1),fill=light);x0,y0,x1,y1=LEG['back'];d.rectangle((x0+1,y1-2,x1-1,y1),fill=sole)
  else:
   for y in range(y0,y1-1,2):d.point((c,y),fill='#cdbd96')
  sheets[kind]=im
 return sheets
