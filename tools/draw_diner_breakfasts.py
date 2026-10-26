"""Authored sixteen-pixel breakfast/menu art and native served-plate models."""
from pathlib import Path
import argparse,json
from PIL import Image,ImageDraw
ROOT=Path(__file__).resolve().parents[1];A=ROOT/'src/main/resources/assets/the_oldest_house'
ORDERS=('pancakes','waffles','eggs_cheese','grits')
def food(name,spoiled):
 im=Image.new('RGBA',(16,16),'#59483a' if spoiled else '#b28b4d');d=ImageDraw.Draw(im)
 if name=='pancakes':
  d.ellipse((0,3,15,15),fill='#585343' if spoiled else '#8e5e30');d.ellipse((1,1,14,13),fill='#7f8060' if spoiled else '#d6ad66');d.arc((2,2,13,12),10,150,fill='#a5a079' if spoiled else '#e5c17e',width=1)
  d.polygon([(4,2),(11,3),(12,7),(8,10),(3,8)],fill='#3e4738' if spoiled else '#81502b');d.rectangle((6,4,9,6),fill='#b2af79' if spoiled else '#efd678')
 elif name=='waffles':
  d.rectangle((1,1,14,14),fill='#8f8d66' if spoiled else '#ddb46b');d.rectangle((2,2,13,13),outline='#484e37' if spoiled else '#ad7e3b')
  for x in (4,8,12):d.line((x,2,x,13),fill='#424938' if spoiled else '#a47437')
  for y in (4,8,12):d.line((2,y,13,y),fill='#424938' if spoiled else '#a47437')
  d.rectangle((6,6,8,8),fill='#b0a66c' if spoiled else '#f0d67e')
 elif name=='eggs_cheese':
  d.rectangle((0,0,15,15),fill='#535b4a' if spoiled else '#b2a782')
  d.polygon([(1,4),(3,2),(8,2),(10,4),(14,5),(14,10),(11,14),(4,13),(1,10)],fill='#9caa8a' if spoiled else '#e2dbc1')
  for x,y in ((5,5),(10,9)):
   d.ellipse((x-2,y-2,x+2,y+2),fill='#6b6140' if spoiled else '#e4ba4f');d.point((x-1,y-1),fill='#95874d' if spoiled else '#f6d574')
  for x,y in ((2,8),(5,11),(11,4),(8,13)):d.line((x,y,x+2,y+1),fill='#6f763e' if spoiled else '#e4c46c')
 else:
  d.ellipse((0,0,15,15),fill='#aca785' if spoiled else '#ded2a9');d.ellipse((1,1,14,14),fill='#7c8664' if spoiled else '#cdc091')
  for x,y in ((2,5),(3,9),(5,2),(7,6),(8,11),(11,4),(12,10),(5,13),(10,14)):
   d.line((x,y,x+1,y),fill='#a0a079' if spoiled else '#e5d9b3');d.point((x,y+1),fill='#4b5841' if spoiled else '#b4a67d')
 if spoiled:
  # Deliberate mold colonies, wet dark seams and a few pale curled larvae.
  for x,y in ((2,3),(12,11),(3,12),(11,2)):
   d.rectangle((x,y,x+1,y+1),fill='#344537');d.point((x+2,y),fill='#899b68')
  d.line((4,9,5,8,6,8,6,10,7,11),fill='#d2c7a2',width=1);d.point((7,11),fill='#78765c')
  d.line((10,6,11,6,12,7,11,8),fill='#c6bd97');d.line((1,14,4,15,7,14),fill='#3b4739')
 return im

def paintings():
 out={}
 plate=Image.new('RGBA',(16,16),'#c8cbb6');d=ImageDraw.Draw(plate);d.rectangle((0,0,15,15),outline='#6c837c');d.rectangle((1,1,14,14),outline='#9daea0');d.rectangle((3,3,12,12),fill='#d8d8bf');d.point((1,12),fill='#68766c');out['plate']=plate
 for name in ORDERS:
  for spoiled in (False,True):
   im=food(name,spoiled);out[('spoiled_' if spoiled else 'fresh_')+name]=im
  icon=Image.new('RGBA',(16,16));d=ImageDraw.Draw(icon);d.ellipse((0,4,15,15),fill='#70847a');d.ellipse((1,3,14,14),fill='#d6d8c3');top=food(name,False).resize((11,9),Image.Resampling.NEAREST);mask=Image.new('L',(11,9));ImageDraw.Draw(mask).ellipse((0,0,10,8),fill=255);icon.paste(top,(2,4),mask);out['icon_'+name]=icon
 menu=Image.new('RGBA',(16,16));d=ImageDraw.Draw(menu);d.rectangle((2,0,13,15),fill='#5b4431');d.rectangle((3,1,12,14),fill='#c9ba8f');d.line((4,3,11,3),fill='#765b3d');d.line((4,4,11,4),fill='#765b3d')
 for y,x in ((7,10),(9,11),(11,9)):d.line((4,y,x,y),fill='#887450')
 d.point((11,13),fill='#ac9569');out['menu']=menu
 return out

def cuboid(a,b,texture):return {'from':list(a),'to':list(b),'faces':{f:{'texture':'#'+texture,'uv':[0,0,16,16]} for f in ('up','down','north','south','east','west')}}
def plate(name):
 elements=[cuboid((2,0,2),(14,1,14),'plate'),cuboid((2,1,2),(14,1.5,3),'plate'),cuboid((2,1,13),(14,1.5,14),'plate'),cuboid((2,1,3),(3,1.5,13),'plate'),cuboid((13,1,3),(14,1.5,13),'plate')]
 if name=='grits':elements +=[cuboid((4,1,4),(12,3,12),'plate'),cuboid((4.5,3,4.5),(11.5,3.5,11.5),'food')]
 elif name=='pancakes':
  for y in (1,2,3):elements.append(cuboid((4,y,4),(12,y+1,12),'food'))
 else:elements.append(cuboid((3.5,1,3.5),(12.5,2.5,12.5),'food'))
 return {'parent':'minecraft:block/block','textures':{'particle':'the_oldest_house:item/diner/spoiled_'+name,'plate':'the_oldest_house:item/diner/plate','food':'the_oldest_house:item/diner/spoiled_'+name},'elements':elements,'display':{'gui':{'rotation':[35,135,0],'translation':[0,0,0],'scale':[1.1,1.1,1.1]},'ground':{'rotation':[0,0,0],'translation':[0,0,0],'scale':[1,1,1]},'fixed':{'rotation':[0,0,0],'translation':[0,0,0],'scale':[1,1,1]},'firstperson_righthand':{'rotation':[0,0,0],'translation':[0,3,0],'scale':[.8,.8,.8]},'thirdperson_righthand':{'rotation':[0,0,0],'translation':[0,3,0],'scale':[.8,.8,.8]}}}

def assets():
 out={}
 for name in ORDERS:
  out['models/item/diner/'+name+'.json']=plate(name)
  out['models/item/diner/icon_'+name+'.json']={'parent':'minecraft:item/generated','textures':{'layer0':'the_oldest_house:item/diner/icon_'+name}}
 for item,prefix in [('second_waitress_meal',''),('diner_breakfast','icon_')]:
  fallback=plate('pancakes') if item=='second_waitress_meal' else {'parent':'minecraft:item/generated','textures':{'layer0':'the_oldest_house:item/diner/icon_pancakes'}}
  fallback['overrides']=[{'predicate':{'custom_model_data':i+1},'model':'the_oldest_house:item/diner/'+prefix+name} for i,name in enumerate(ORDERS)]
  out['models/item/'+item+'.json']=fallback
 out['models/item/second_waitress_menu.json']={'parent':'minecraft:item/generated','textures':{'layer0':'the_oldest_house:item/diner/menu'}}
 return out

def main():
 p=argparse.ArgumentParser();p.add_argument('--check',action='store_true');check=p.parse_args().check
 for name,im in paintings().items():
  path=A/'textures/item/diner'/(name+'.png')
  if check:
   with Image.open(path) as saved:assert saved.convert('RGBA').tobytes()==im.tobytes(),name
  else:path.parent.mkdir(parents=True,exist_ok=True);im.save(path,optimize=True)
 for name,obj in assets().items():
  path=A/name
  if check:assert json.loads(path.read_text())==obj,name
  else:path.parent.mkdir(parents=True,exist_ok=True);path.write_text(json.dumps(obj,indent=2)+'\n')
 print(('Verified' if check else 'Painted')+' four ordinary menu breakfasts, four individually spoiled plates, the legacy plate fallback and one handed menu.')
if __name__=='__main__':main()
