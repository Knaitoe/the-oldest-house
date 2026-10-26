"""Original handling atlas and physical instrument/transport recordings. Additive to the room material set."""
from pathlib import Path
from PIL import Image,ImageDraw
import argparse,io,json,math,random,array,subprocess,hashlib
ROOT=Path(__file__).resolve().parents[1];A=ROOT/'src/main/resources/assets/the_oldest_house'
p=argparse.ArgumentParser();p.add_argument('--check',action='store_true');args=p.parse_args();out={}
base=['bluewood','darkwood','oak','iron','brass','enamel','linen','ink','paper','bench']
ims=[Image.open(A/f'textures/block/interiors/{n}.png').convert('RGBA') for n in base]
def tile(color):
 im=Image.new('RGBA',(16,16),color);ims.append(im);return ImageDraw.Draw(im)
d=tile('#dfd6b8');d.line((0,15,15,15),fill='#8b8067');d.line((1,1,1,14),fill='#f1e9cf');d.point((10,12),fill='#b4a68d')
d=tile('#2a302c');d.line((1,1,14,1),fill='#53615a');d.line((0,15,15,15),fill='#151c1a')
d=tile('#b8a479');d.rectangle((0,0,15,15),outline='#675b42')
for c in(0,5,10,15):d.line((c,0,c,15),fill='#8b815f');d.line((0,c,15,c),fill='#8b815f')
# Four original, readable slide paintings: the bedroom window, chair, projector and open doorway.
for scene in range(4):
 d=tile('#b5ab94');d.rectangle((0,0,15,15),outline='#e4d9bd');d.rectangle((2,2,13,13),fill='#676879');d.rectangle((2,10,13,13),fill='#907656')
 if scene==0:
  d.rectangle((4,3,11,9),fill='#2e484d',outline='#d9cfb2');d.line((7,3,7,9),fill='#b1bdad');d.line((4,6,11,6),fill='#b1bdad')
 elif scene==1:
  d.rectangle((5,4,10,9),fill='#405e57');d.rectangle((4,9,11,10),fill='#5d7c66');d.line((5,11,5,13),fill='#352f29');d.line((10,11,10,13),fill='#352f29')
 elif scene==2:
  d.rectangle((3,3,12,8),fill='#d5d5bf');d.rectangle((6,10,10,12),fill='#3e4d50');d.point((10,10),fill='#bba876');d.line((5,12,4,13),fill='#292e2c')
 else:
  d.rectangle((5,2,11,12),fill='#222c2c',outline='#ad9871');d.line((6,12,9,14),fill='#b3a185');d.rectangle((9,4,10,11),fill='#706b58')
d=tile('#b5b5a0');d.ellipse((1,1,14,14),outline='#4b5953');d.rectangle((7,2,8,13),fill='#647a6d');d.rectangle((2,7,13,8),fill='#647a6d')
d=tile('#493a2d');d.rectangle((0,0,15,15),outline='#80694b')
for glyph in('play','pause','rewind'):
 d=tile('#bbbba8');d.rectangle((0,0,15,15),outline='#5d7069');d.line((2,2,13,2),fill='#e1dbc2')
 if glyph=='play':d.polygon([(5,4),(11,8),(5,12)],fill='#344941')
 elif glyph=='pause':d.rectangle((4,4,6,12),fill='#344941');d.rectangle((9,4,11,12),fill='#344941')
 else:
  d.polygon([(3,8),(8,4),(8,12)],fill='#344941');d.polygon([(8,8),(13,4),(13,12)],fill='#344941')
d=tile('#d7cfb5');d.rectangle((0,0,15,15),outline='#b5a886');d.rectangle((2,2,13,13),outline='#737c6b')
atlas=Image.new('RGBA',(128,64),(0,0,0,0))
for i,im in enumerate(ims):atlas.paste(im,(i%8*16,i//8*16))
b=io.BytesIO();atlas.save(b,format='PNG');out[A/'textures/block/handling/atlas.png']=b.getvalue()
model={'textures':{'particle':'the_oldest_house:block/interiors/bluewood'},'elements':[]}
out[A/'models/block/vignette_handling.json']=(json.dumps(model,separators=(',',':'))+'\n').encode()
out[A/'blockstates/vignette_handling.json']=(json.dumps({'variants':{f'kind={k},facing={f}':{'model':'the_oldest_house:block/vignette_handling'} for k in('toy_piano','spinet','slides','model_kit','tape') for f in('north','east','south','west')}},separators=(',',':'))+'\n').encode()
RATE=22050
def encode(samples):
 pcm=array.array('h',(round(max(-.95,min(.95,s))*32767) for s in samples))
 return subprocess.run(['ffmpeg','-nostdin','-v','error','-f','s16le','-ar',str(RATE),'-ac','1','-i','pipe:0','-c:a','libvorbis','-q:a','3','-fflags','+bitexact','-flags:a','+bitexact','-map_metadata','-1','-f','ogg','pipe:1'],input=pcm.tobytes(),capture_output=True,check=True).stdout
sounds={};manifest_path=A/'sounds/handling/manifest.json';stored=json.loads(manifest_path.read_text()) if args.check else {};manifest={}
def sound(name,samples):
 pcm=array.array('h',(round(max(-.95,min(.95,s))*32767) for s in samples)).tobytes();pcm_sha=hashlib.sha256(pcm).hexdigest();path=A/f'sounds/handling/{name}.ogg'
 if args.check:
  data=path.read_bytes();entry=stored.get(name,{})
  assert entry.get('pcm_sha256')==pcm_sha and entry.get('ogg_sha256')==hashlib.sha256(data).hexdigest() and data.startswith(b'OggS'), 'Stale or corrupt handling sound '+name
 else:data=encode(samples)
 manifest[name]={'pcm_sha256':pcm_sha,'ogg_sha256':hashlib.sha256(data).hexdigest(),'sample_rate':RATE,'samples':len(samples)}
 out[path]=data;sounds['literary.handling_'+name]={'subtitle':'subtitles.the_oldest_house.handling_'+('tape' if name.startswith('tape_') else name),'sounds':[{'name':'the_oldest_house:handling/'+name,'attenuation_distance':12}]}
# Struck toy tines and a slightly detuned, wooden spinet. Both roots are C4; native per-key pitch supplies the octave.
for name,seconds,toy in [('toy',.85,True),('spinet',2.2,False)]:
 samples=[];rng=random.Random(94 if toy else 95)
 for i in range(int(seconds*RATE)):
  t=i/RATE;attack=min(1,t/.002);root=261.625565
  if toy:s=.32*math.sin(2*math.pi*root*t)*math.exp(-t*5)+.13*math.sin(2*math.pi*root*2.76*t)*math.exp(-t*12)
  else:s=sum(.25/(h**1.6)*math.sin(2*math.pi*root*h*t+.08*math.sin(2*math.pi*1.2*t))*math.exp(-t*(1.9+h*.5)) for h in range(1,7))+.035*math.sin(2*math.pi*root*1.003*t)*math.exp(-t*2)
  s+=rng.uniform(-1,1)*.025*math.exp(-t*90);samples.append(s*attack)
 sound(name,samples)
for name,seed,secs in [('click',11,.09),('slide',12,.22),('wood',13,.12),('wind',14,.5)]:
 rng=random.Random(seed);samples=[];low=0
 for i in range(int(secs*RATE)):
  t=i/RATE;noise=rng.uniform(-1,1);low=low*.78+noise*.22
  if name=='wind':v=.035*low+.035*math.sin(2*math.pi*270*t)*(1+.4*math.sin(2*math.pi*24*t))
  else:v=(.22*low+.06*math.sin(2*math.pi*(900 if name=='slide' else 480)*t))*math.exp(-t*(22 if name=='slide' else 60))
  samples.append(v*min(1,t/.002))
 sound(name,samples)
# Sixteen seconds from a quiet watched room: tape hiss, an ordinary clock, cloth/chair movement and paper.
# No invented voice, hint, imported sample or new story disclosure.
rng=random.Random(2275);room=[];low=0
for i in range(16*RATE):
 t=i/RATE;noise=rng.uniform(-1,1);low=low*.96+noise*.04;v=.012*noise+.025*low+.007*math.sin(2*math.pi*60*t)
 phase=t%1;v+=.06*math.sin(2*math.pi*(980 if int(t)%2 else 710)*phase)*math.exp(-phase*110)
 for start,length in [(3.1,.9),(8.4,1.3),(12.6,.8)]:
  q=t-start
  if 0<q<length:v+=.08*low*math.sin(math.pi*q/length)**2+.014*math.sin(2*math.pi*(120+45*q)*q)*math.sin(math.pi*q/length)**2
 room.append(v)
for n in range(32):sound(f'tape_{n:02}',room[n*RATE//2:(n+1)*RATE//2])
out[manifest_path]=(json.dumps(manifest,indent=2)+'\n').encode()
labels={'toy':'Toy piano plays','spinet':'Spinet plays','click':'Control clicks','slide':'Slide tray clicks','wood':'Model piece taps','wind':'Tape rewinds','tape':'Recording plays'}
lang_path=A/'lang/en_us.json';language=json.loads(lang_path.read_text())
for key,value in labels.items():
 name='subtitles.the_oldest_house.handling_'+key
 if args.check:assert language.get(name)==value, 'Missing handling subtitle '+name
 else:language[name]=value
if not args.check:lang_path.write_text(json.dumps(language,indent=2,ensure_ascii=False)+'\n')
master=A/'sounds.json';current=json.loads(master.read_text())
if args.check:
 assert all(current.get(k)==v for k,v in sounds.items()),'Missing handling sound registration'
else:current.update(sounds);master.write_text(json.dumps(current,indent=2)+'\n')
bad=[]
for path,data in out.items():
 if args.check:
  if not path.exists() or path.read_bytes()!=data:bad.append(str(path.relative_to(ROOT)))
 else:path.parent.mkdir(parents=True,exist_ok=True);path.write_bytes(data)
if bad:raise SystemExit('Stale handling assets: '+', '.join(bad))
print(f'Handling atlas: {len(ims)} native pixel tiles; {len(sounds)} original sounds; all output deterministic.')
