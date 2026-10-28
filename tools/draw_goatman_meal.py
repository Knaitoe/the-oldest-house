"""Native sixteen-pixel supper bottles, bread and fitted item meshes. No historical PNG is repainted."""
from pathlib import Path
from PIL import Image,ImageDraw
import argparse,io,json
ROOT=Path(__file__).resolve().parents[1];A=ROOT/'src/main/resources/assets/the_oldest_house'
def box(a,b,tex):return {'from':a,'to':b,'faces':{f:{'texture':'#'+tex,'uv':[0,0,16,16]} for f in ['up','down','north','south','east','west']}}
def outputs():
 out={};tiles={}
 for name,color,dark,light in [('ketchup','#9b3b2e','#682b25','#bc5941'),('mustard','#b49235','#806826','#d0af4e'),('mayo','#d6cdb1','#a89f86','#ede3c9'),('relish','#647044','#424e32','#87925a'),('bun','#bd8c50','#825b35','#d8ae70'),('cap','#d8d1ba','#8d8a7b','#e8e0c9'),('label','#d5cbb0','#a49a82','#e7dfc7'),('basket','#947b54','#68583d','#b09566')]:
  im=Image.new('RGBA',(16,16),color);d=ImageDraw.Draw(im);d.line((0,0,0,15),fill=dark);d.line((1,1,14,1),fill=light);d.line((14,3,14,14),fill=dark)
  if name=='label':
   d.rectangle((3,3,12,12),outline=dark);d.line((5,6,10,6),fill=dark);d.line((5,9,9,9),fill=dark)
  elif name=='bun':
   for x,y in [(3,4),(8,3),(11,7),(5,10),(10,12)]:d.line((x,y,x+1,y),fill=light)
  elif name=='basket':
   for y in [4,8,12]:d.line((0,y,15,y),fill=dark)
  tiles[name]=im;buf=io.BytesIO();im.save(buf,format='PNG');out['textures/item/meal/'+name+'.png']=buf.getvalue()
 textures={n:'the_oldest_house:item/meal/'+n for n in tiles}
 for i,name in enumerate(['ketchup','mustard','mayo','relish','bun','buns']):
  if i<4:parts=[box([5.5,8,5.5],[10.5,16,10.5],name),box([6,16.02,6],[10,18,10],name),box([6,18.02,6],[10,20,10],'cap'),box([6,10,5.25],[10,14,5.4],'label')]
  elif name=='bun':parts=[box([3,8,5],[13,10.5,11],'bun'),box([3.5,10.52,5.5],[12.5,11.5,10.5],'bun')]
  else:
   parts=[box([1,8,2],[15,8.6,14],'basket')]
   for a,b in [([1,8.62,2],[2,12,14]),([14,8.62,2],[15,12,14]),([2,8.62,2],[14,12,3]),([2,8.62,13],[14,12,14])]:parts.append(box(a,b,'basket'))
   for z in [4,7,10]:parts.append(box([3,9,z],[13,12.5,z+2.5],'bun'))
  model={'ambientocclusion':True,'textures':textures,'elements':parts,'display':{'gui':{'rotation':[25,-35,0],'translation':[0,-3,0],'scale':[.9,.9,.9]},'thirdperson_righthand':{'rotation':[0,0,0],'translation':[0,-1,0],'scale':[.6,.6,.6]},'thirdperson_lefthand':{'rotation':[0,0,0],'translation':[0,-1,0],'scale':[.6,.6,.6]},'firstperson_righthand':{'rotation':[0,-30,0],'translation':[1,-2,0],'scale':[.7,.7,.7]},'fixed':{'rotation':[0,0,0],'translation':[0,0,0],'scale':[1,1,1]}}}
  out['models/item/goatman_'+name+'.json']=(json.dumps(model,indent=2)+'\n').encode()
 for burnt in [False,True]:
  original=Image.open(A/('textures/item/goatman_brat'+('_burnt' if burnt else '')+'.png')).convert('RGBA');im=Image.new('RGBA',(16,16));d=ImageDraw.Draw(im)
  d.polygon([(1,10),(9,2),(13,2),(15,5),(7,13),(3,14),(1,12)],fill='#bb894a');d.line((3,11,11,3),fill='#e0b673',width=3);im.alpha_composite(original)
  name='goatman_brat_bun'+('_burnt' if burnt else '');buf=io.BytesIO();im.save(buf,format='PNG');out['textures/item/'+name+'.png']=buf.getvalue();out['models/item/'+name+'.json']=(json.dumps({'parent':'minecraft:item/generated','textures':{'layer0':'the_oldest_house:item/'+name}},indent=2)+'\n').encode()
 return out
if __name__=='__main__':
 p=argparse.ArgumentParser(description=__doc__);p.add_argument('--check',action='store_true');args=p.parse_args();out=outputs()
 for rel,data in out.items():
  file=A/rel
  if args.check:assert file.read_bytes()==data,rel
  else:file.parent.mkdir(parents=True,exist_ok=True);file.write_bytes(data)
 print('Six native supper item meshes and ten authored/derived pixel tiles match their painter.')
