"""Native sixteen-pixel appliance, porcelain and archival steel. No resampling."""
from pathlib import Path
from PIL import Image,ImageDraw
import argparse,io,json
ROOT=Path(__file__).resolve().parents[1];A=ROOT/'src/main/resources/assets/the_oldest_house'
p=argparse.ArgumentParser();p.add_argument('--check',action='store_true');check=p.parse_args().check;out={};tiles=[]
def tile(c):
 im=Image.new('RGBA',(16,16),c);tiles.append(im);return ImageDraw.Draw(im)
d=tile('#a5aba3');d.line((0,0,15,0),fill='#d4d5c4');d.line((0,15,15,15),fill='#636f6b')
for y in (3,7,11):d.line((1,y,14,y),fill='#929d96');d.line((2,y+1,13,y+1),fill='#b4b8ac')
d=tile('#d5d5c2');d.line((0,0,15,0),fill='#efead3');d.line((0,15,15,15),fill='#8a9a91');d.point((12,11),fill='#b1b8a8')
d=tile('#272f2d');d.line((0,0,15,0),fill='#4f5e58');d.line((0,15,15,15),fill='#17211e')
d=tile('#435c5a')
for y in (2,5,8,11,14):
 for x in (2,5,8,11,14):d.point((x,y),fill='#71817a')
d=tile('#d2c9a8');d.rectangle((0,0,15,15),outline='#8d8b70');d.line((3,5,12,5),fill='#6e796d');d.line((4,8,10,8),fill='#81836d')
d=tile('#60664f');d.line((0,0,15,0),fill='#93977a');d.line((0,15,15,15),fill='#373f37')
for x,y in ((1,11),(11,2),(14,13)):d.point((x,y),fill='#8d795a');d.point((x+1,y),fill='#a0926a')
d=tile('#72775a');d.rectangle((0,0,15,15),outline='#394638');d.line((1,1,14,1),fill='#a0a183');d.line((1,14,14,14),fill='#515d47');d.point((2,12),fill='#a38d62');d.point((3,12),fill='#bcac7e')
d=tile('#55402a');d.line((5,0,5,15),fill='#88714a');d.line((9,0,9,15),fill='#302e23');d.point((3,5),fill='#b59460')
d=tile('#6e9a9b');d.line((0,4,15,4),fill='#a1b9af');d.line((0,11,15,11),fill='#56858b')
d=tile('#bd7c38');d.rectangle((3,3,12,12),fill='#e5bf61');d.rectangle((5,5,10,10),fill='#f1d584')
d=tile('#b9c7af');d.rectangle((0,0,15,15),outline='#81998f');d.rectangle((3,3,12,12),fill='#dbd5ba')
d=tile('#c5a16a');d.rectangle((0,0,15,15),outline='#806344');d.line((2,1,13,1),fill='#e0c08b');d.point((5,7),fill='#98784c')
d=tile('#93afb1');d.polygon([(0,3),(3,0),(10,0),(0,10)],fill='#b7c9c2');d.polygon([(7,15),(15,7),(15,12),(12,15)],fill='#789898');d.line((1,1,14,1),fill='#d4dbcc')
atlas=Image.new('RGBA',(128,64),(0,0,0,0))
for i,im in enumerate(tiles):atlas.paste(im,(i%8*16,i//8*16))
b=io.BytesIO();atlas.save(b,format='PNG');out[A/'textures/block/details/atlas.png']=b.getvalue()
for name,base,grout,shine in [('bathroom_wall','#c9d1c4','#768f89','#e0e0cc'),('bathroom_floor','#657e79','#394f4a','#80988c')]:
 im=Image.new('RGBA',(16,16),base);d=ImageDraw.Draw(im);step=8 if name.endswith('wall') else 16
 for x in range(0,16,step):d.line((x,0,x,15),fill=grout);d.line((x+1,1,x+1,15),fill=shine)
 for y in range(0,16,step):d.line((0,y,15,y),fill=grout);d.line((1,y+1,15,y+1),fill=shine)
 d.point((11,12),fill=grout);b=io.BytesIO();im.save(b,format='PNG');out[A/f'textures/block/uncanny/{name}.png']=b.getvalue()
 out[A/f'blockstates/uncanny_{name}.json']=json.dumps({'variants':{'':{'model':f'the_oldest_house:block/uncanny/{name}'}}}).encode()
 out[A/f'models/block/uncanny/{name}.json']=json.dumps({'parent':'minecraft:block/cube_all','textures':{'all':f'the_oldest_house:block/uncanny/{name}'}}).encode()
out[A/'models/block/service_fitting.json']=json.dumps({'textures':{'particle':'the_oldest_house:block/interiors/iron'},'elements':[]}).encode()
out[A/'blockstates/service_fitting.json']=json.dumps({'variants':{f'kind={k},facing={f}':{'model':'the_oldest_house:block/service_fitting'} for k in ('drawer','toilet','basin','mirror','dryer','soap','microwave','toaster','kettle','roach') for f in ('north','east','south','west')}}).encode()
for path,data in out.items():
 if check:assert path.read_bytes()==data,path
 else:path.parent.mkdir(parents=True,exist_ok=True);path.write_bytes(data)
print(('Verified' if check else 'Painted')+' native archival steel, porcelain/appliance atlas and contemporary bathroom tiles.')
