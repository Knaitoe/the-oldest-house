#!/usr/bin/env python3
"""Import the generated material tiles and author native UV models/audio. No invented texture paths."""
from pathlib import Path
import json, math, random, struct, subprocess, wave
R=Path(__file__).resolve().parents[1]
A=R/'src/main/resources/assets/the_oldest_house'
def write(path,data):
 p=A/path;p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(data,indent=2)+'\n')
def cube(bounds,texture):
 return {'from':bounds[:3],'to':bounds[3:],'faces':{face:{'texture':'#'+texture} for face in ['up','down','north','south','east','west']}}
for name in ['cell_gouges','sealed_window','institute_paint','collapse_plaster','well_carvings','archive_paper']:
 write(f'blockstates/{name}.json',{'variants':{'':{'model':f'the_oldest_house:block/{name}'}}})
 write(f'models/block/{name}.json',{'parent':'minecraft:block/cube_all','textures':{'all':f'the_oldest_house:block/{name}'}})
props={
 'projector':([([2,0,3,14,3,13],'case'),([3,3,4,13,11,13],'case'),([6,5,1,10,9,4],'lens'),([3,11,5,5,14,11],'reel'),([10,11,5,12,14,11],'reel')],{'case':'the_oldest_house:block/enamel','lens':'minecraft:block/black_concrete','reel':'minecraft:block/gray_concrete'}),
 'incubator':([([1,0,1,15,2,15],'frame'),([2,2,2,14,4,14],'fabric'),([1,4,1,15,5,15],'frame'),([1,5,1,2,14,15],'glass'),([14,5,1,15,14,15],'glass'),([2,5,1,14,14,2],'glass'),([2,5,14,14,14,15],'glass'),([1,14,1,15,15,15],'glass')],{'frame':'the_oldest_house:block/enamel','glass':'minecraft:block/glass','fabric':'the_oldest_house:block/ward_fabric'}),
 'mail_slot':([([0,0,0,16,16,16],'metal')],{'metal':'the_oldest_house:block/whale_mail_slot'})}
variants={}
for kind,(parts,textures) in props.items():
 textures['particle']=next(iter(textures.values()))
 model={'parent':'minecraft:block/block','textures':textures,'elements':[cube(b,t) for b,t in parts]}
 if kind=='incubator':model['render_type']='minecraft:translucent'
 write(f'models/block/novel_{kind}.json',model)
 for facing,angle in [('north',0),('east',90),('south',180),('west',270)]:variants[f'facing={facing},kind={kind}']={'model':f'the_oldest_house:block/novel_{kind}','y':angle}
write('blockstates/novel_prop.json',{'variants':variants})
for name,texture in [('cat_collar','minecraft:item/lead'),('well_ribbon','minecraft:item/string'),('archive_key','minecraft:item/tripwire_hook')]:write(f'models/item/{name}.json',{'parent':'minecraft:item/generated','textures':{'layer0':texture}})
write('models/item/walkie_talkie.json',{'parent':'minecraft:block/block','textures':{'case':'minecraft:block/gray_concrete','metal':'minecraft:block/iron_block','particle':'minecraft:block/gray_concrete'},'elements':[cube([4,2,5,12,13,11],'case'),cube([5,13,7,6,20,8],'metal'),cube([5,8,4.6,11,12,5],'metal')],'display':{'gui':{'rotation':[20,35,0],'translation':[0,-2,0],'scale':[.85,.85,.85]},'firstperson_righthand':{'rotation':[0,-45,0],'translation':[1,3,0],'scale':[.5,.5,.5]}}})
# A distinct compressed bitmap provider in the established handwriting system.
font=json.loads((A/'font/karen.json').read_text());font['providers'][0]['height']=10;font['providers'][0]['ascent']=8;write('font/pelafina.json',font)
rng=random.Random(423);rate=22050
for name,duration in [('monitor',1.2),('shutter',.28),('radio',1.0),('collapse',2.7)]:
 samples=[];low=0
 for i in range(int(rate*duration)):
  t=i/rate;noise=rng.uniform(-1,1);low=.975*low+.025*noise
  if name=='monitor':v=(math.sin(t*2*math.pi*760)+.25*math.sin(t*2*math.pi*1140))*.19*(1 if .12<t% .4<.27 else 0)
  elif name=='shutter':v=noise*.4*math.exp(-t*22)+(noise*.2*math.exp(-(t-.13)*40) if t>.13 else 0)
  elif name=='radio':v=(noise*.12+math.sin(t*2*math.pi*290)*.05)*math.sin(math.pi*t/duration)**2
  else:v=(low*4*.3+math.sin(t*2*math.pi*43)*.11+noise*.06)*math.sin(math.pi*t/duration)**.7
  samples.append(struct.pack('<h',int(max(-.85,min(.85,v))*32767)))
 folder=A/'sounds/novel';folder.mkdir(parents=True,exist_ok=True);wav=folder/(name+'.wav')
 with wave.open(str(wav),'wb') as f:f.setnchannels(1);f.setsampwidth(2);f.setframerate(rate);f.writeframes(b''.join(samples))
 subprocess.run(['ffmpeg','-loglevel','error','-y','-i',str(wav),'-c:a','libvorbis','-q:a','4',str(folder/(name+'.ogg'))],check=True);wav.unlink()
sounds=json.loads((A/'sounds.json').read_text())
for name,subtitle in [('monitor','The monitor calls'),('shutter','A shutter closes'),('radio','Radio static'),('collapse','The House breaks')]:
 sounds['novel.'+name]={'sounds':[{'name':'the_oldest_house:novel/'+name,'stream':False}],'subtitle':'subtitles.the_oldest_house.novel.'+name}
write('sounds.json',sounds)
lang=json.loads((A/'lang/en_us.json').read_text())
for name,label in [('cat_collar','A Cat\'s Collar'),('well_ribbon','A Ribbon from the Well'),('archive_key','The Key from the Mat'),('walkie_talkie','Tom\'s Walkie-talkie')]:lang['item.the_oldest_house.'+name]=label
for name,label in [('vulture','Vulture'),('novel_actor','A Familiar Figure')]:lang['entity.the_oldest_house.'+name]=label
for name,subtitle in [('monitor','The monitor calls'),('shutter','A shutter closes'),('radio','Radio static'),('collapse','The House breaks')]:lang['subtitles.the_oldest_house.novel.'+name]=subtitle
write('lang/en_us.json',lang)
print('Novel UV models, handwriting provider and four original mono cues ready.')
