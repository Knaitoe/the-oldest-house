"""0.4.766: the porch's three small things, as native 16-pixel tiles and fitted meshes.

The strap on its nail beside the screen door, the small shoes on the top step with their toes to the wall, and the glass of
milk the one who cooked sets at the reader's plate."""
from PIL import Image,ImageDraw
import random
PARTS=['strap','shoes','milk']
def _rng(name):return random.Random(7660+sum(map(ord,name))*17)
def tiles(out):
 # A worn leather strap: dark edges, a lighter middle, a stitched line either side and scuffs.
 im=Image.new('RGBA',(16,16),'#6b4426');d=ImageDraw.Draw(im);rng=_rng('strap')
 for y in range(16):d.point((0,y),fill='#4d2f19');d.point((15,y),fill='#4d2f19')
 for y in range(1,16,2):d.point((3,y),fill='#a37a4f');d.point((12,y),fill='#a37a4f')
 for _ in range(26):d.point((rng.randrange(1,15),rng.randrange(16)),fill=rng.choice(['#7c5230','#5c391f','#86593a']))
 out['strap']=im
 # Small canvas shoes: a faded upper with an eyelet row and a rubber toe band.
 im=Image.new('RGBA',(16,16),'#7f8fa3');d=ImageDraw.Draw(im);rng=_rng('shoe')
 for _ in range(40):d.point((rng.randrange(16),rng.randrange(16)),fill=rng.choice(['#72829a','#8d9cae','#7889a0']))
 for y in (4,7,10):d.point((6,y),fill='#d9d6cc');d.point((9,y),fill='#d9d6cc')
 for y in (5,8):d.line((6,y,9,y),fill='#e9e6dc')
 d.line((0,14,15,14),fill='#d8d3c4');d.line((0,15,15,15),fill='#c6c0ae')
 out['shoe']=im
 im=Image.new('RGBA',(16,16),'#d8d3c4');d=ImageDraw.Draw(im);rng=_rng('sole')
 for _ in range(30):d.point((rng.randrange(16),rng.randrange(16)),fill=rng.choice(['#c9c3b1','#e2ddd0']))
 out['shoe_sole']=im
 # A tumbler of milk: the white fill below a clear rim, a highlight down one side.
 im=Image.new('RGBA',(16,16),'#f3f1ea');d=ImageDraw.Draw(im)
 d.rectangle((0,0,15,2),fill='#cfd8d8');d.line((0,3,15,3),fill='#e4e2da')
 for y in range(4,16):d.point((2,y),fill='#ffffff');d.point((13,y),fill='#dedbd2')
 out['milk']=im
 im=Image.new('RGBA',(16,16),'#f6f4ee');d=ImageDraw.Draw(im);d.rectangle((0,0,15,15),outline='#cfd8d8');d.point((5,5),fill='#ffffff');d.point((6,5),fill='#ffffff')
 out['milk_top']=im
def models(model,cube,faced):
 C=cube;M=model
 # The strap: a nail high on the wall and the strap hanging from it, folded once, with a buckle at the bottom.
 M('strap',[C([7.5,13,0],[8.5,14,2.2],'metal'),C([7,3,.6],[9,13,1.4],'strap'),C([6.8,4,1.4],[9.2,5.5,1.8],'metal'),C([7.2,12,1.4],[8.8,13,1.9],'strap')])
 # Two small shoes side by side on the step, toes to the wall (north at facing south).
 # 0.4.767: a stair's upper tread is only its back half (z 0 to 8), and the shoes ran from 3 to 12, so their heels hung in the
 # air half a block above the lower tread. They now stand wholly on the upper tread, a little shorter.
 shoes=[]
 for x in (3,9):
  shoes+=[C([x,0,.5],[x+4,1,8],'shoe_sole'),faced([x+.25,1,3.5],[x+3.75,3.5,8],'shoe',up='shoe_sole'),C([x+.5,1,1],[x+3.5,2.2,3.5],'shoe')]
 M('shoes',shoes)
 M('milk',[faced([6,0,6],[10,6.5,10],'milk',up='milk_top',down='milk_top')])
