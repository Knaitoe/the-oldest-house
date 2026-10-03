"""Native hotel UV art, multi-cube furniture, keepsakes and original synthesized audio."""
from pathlib import Path
from PIL import Image,ImageDraw
import json,math,random,wave,struct,subprocess,tempfile
A=Path(__file__).resolve().parents[1]/'src/main/resources/assets/the_oldest_house'
def write(path,obj):
 p=A/path;p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(obj,indent=2)+'\n')
def save(path,im):
 p=A/path;p.parent.mkdir(parents=True,exist_ok=True);im.save(p,optimize=True)
def cube(a,b,tex='surface',uv=(0,0,16,16),rot=None):
 e={'from':a,'to':b,'faces':{f:{'texture':'#'+tex,'uv':list(uv)} for f in ('north','south','east','west','up','down')}}
 if rot:e['rotation']=rot
 return e
def model(path,els,textures,display=None):
 o={'ambientocclusion':True,'textures':textures,'elements':els}
 if display:o['display']=display
 write('models/'+path+'.json',o)
def material(name,col,grain=False):
 im=Image.new('RGBA',(32,32));rng=random.Random(name)
 for y in range(32):
  for x in range(32):
   n=rng.randrange(-5,6)+(int(7*math.sin(x*.6+y*.15)) if grain else 0);im.putpixel((x,y),tuple(max(0,min(255,c+n)) for c in col)+(255,))
 save('textures/block/'+name+'.png',im);return im
for name,c,g in [('hotel_wood',(62,37,27),True),('hotel_brass',(155,116,48),False),('hotel_steel',(58,64,67),False),('hotel_rubber',(72,49,41),False),('hotel_paper',(218,207,175),False),('hotel_food',(147,72,36),False),('hotel_patch',(161,133,96),True)]:material(name,c,g)
keys=Image.new('RGBA',(32,32),(238,228,201,255));d=ImageDraw.Draw(keys)
for x in range(0,32,4):d.line((x,0,x,31),fill=(84,70,59,255))
for x in (4,9,17,22,27):d.rectangle((x,0,x+2,18),fill=(26,23,24,255))
save('textures/block/hotel_keys.png',keys)
photo=Image.new('RGBA',(64,64),(183,160,113,255));d=ImageDraw.Draw(photo);d.rectangle((3,3,60,60),outline=(48,38,30,255),width=3);d.rectangle((7,7,56,56),fill=(111,101,86,255))
for row in range(3):
 for i in range(7):
  x=10+i*7;y=12+row*13;d.ellipse((x,y,x+4,y+5),fill=(206-row*8,184-row*8,150-row*8,255));d.rectangle((x-1,y+6,x+5,y+12),fill=(40+i*3,35+i*2,33+i*2,255))
d.rectangle((28,41,36,55),fill=(206,188,150,255));save('textures/block/hotel_party.png',photo)
for pressure in range(4):
 im=Image.new('RGBA',(32,32),(47,52,54,255));d=ImageDraw.Draw(im);d.ellipse((3,3,28,28),fill=(212,204,170,255),outline=(142,107,52,255),width=2)
 for i in range(9):
  a=math.radians(145+i*28);x=16+10*math.cos(a);y=16+10*math.sin(a);d.line((x,y,16+8*math.cos(a),16+8*math.sin(a)),fill=(50,47,41,255))
 a=math.radians(145+pressure*75);d.line((16,16,16+9*math.cos(a),16+9*math.sin(a)),fill=(128,31,26,255),width=2);save(f'textures/block/hotel_gauge_{pressure}.png',im)
tex={k:'the_oldest_house:block/hotel_'+v for k,v in {'surface':'wood','metal':'brass','steel':'steel','rubber':'rubber','paper':'paper','keys':'keys','food':'food','patch':'patch','photo':'party'}.items()};tex['particle']=tex['surface']
els={}
legs=[cube([x,0,z],[x+2,12,z+2]) for x in (1,13) for z in (1,13)]
els['piano']=legs+[cube([0,12,0],[30,16,16]),cube([0,16,9],[30,27,16]),cube([0,14,-3],[30,17,5],'keys'),cube([0,27,9],[30,29,17]),cube([0,17,10],[30,18,11],'metal')]
els['typewriter']=[cube([2,0,2],[14,2,14],'steel'),cube([3,2,3],[13,5,10],'keys'),cube([2,4,10],[14,7,12],'steel'),cube([4,5,11],[12,16,11.4],'paper'),cube([1,7,10],[15,8,11],'metal')]
els['boiler']=[cube([1,0,1],[15,2,15],'steel'),cube([2,2,2],[14,29,14],'steel'),cube([0,5,0],[16,7,16],'metal'),cube([0,24,0],[16,26,16],'metal'),cube([5,29,5],[11,32,11],'steel'),cube([5,9,0],[11,19,2],'metal')]
els['gauge']=[cube([2,2,13],[14,14,16],'gauge'),cube([6,0,14],[10,3,16],'steel')]
els['meal']=[cube([1,0,1],[15,1,15],'paper'),cube([4,1,4],[12,3,12],'food'),cube([11,1,11],[14,6,14],'metal'),cube([1,1,3],[2,2,13],'metal')]
els['reception']=legs+[cube([0,12,0],[16,16,16]),cube([1,2,12],[15,12,15]),cube([10,16,4],[14,17,8],'metal'),cube([11,17,5],[13,20,7],'metal')]
els['hose_rack']=[cube([0,0,13],[16,16,16],'steel')]+[cube([2+i,2+i,11],[14-i,4+i,14],'rubber') for i in range(4)]+[cube([2+i,12-i,11],[14-i,14-i,14],'rubber') for i in range(4)]+[cube([2,4,11],[4,12,14],'rubber'),cube([12,4,11],[14,12,14],'rubber'),cube([13,0,10],[16,5,13],'metal')]
els['photo']=[cube([0,0,0],[32,32,3],'surface'),cube([1,1,3],[31,31,3.2],'photo')]
els['headstone']=[cube([1,0,2],[15,2,14],'steel'),cube([3,2,5],[13,18,11],'paper'),cube([5,18,5],[11,21,11],'paper'),cube([5,11,4.8],[11,12,5],'steel')]
els['patch']=[cube([0,0,0],[16,16,16],'patch')]
variants={}
for kind,parts in els.items():
 for pressure in range(4):
  name=f'hotel_{kind}_{pressure}';textures=dict(tex);textures['gauge']=f'the_oldest_house:block/hotel_gauge_{pressure}';model('block/'+name,parts,textures)
  for facing,rotation in [('north',0),('east',90),('south',180),('west',270)]:variants[f'facing={facing},kind={kind},pressure={pressure}']={'model':'the_oldest_house:block/'+name,'y':rotation}
write('blockstates/hotel_prop.json',{'variants':variants})
display={'gui':{'rotation':[28,-35,0],'translation':[0,0,0],'scale':[.85,.85,.85]},'ground':{'translation':[0,2,0],'scale':[.5,.5,.5]},'thirdperson_righthand':{'rotation':[0,0,0],'translation':[0,2,0],'scale':[.6,.6,.6]},'firstperson_righthand':{'rotation':[0,-45,15],'translation':[0,2,0],'scale':[.7,.7,.7]}}
model('item/hotel_master_key',[cube([5,5,7],[7,15,9],'metal'),cube([9,5,7],[11,15,9],'metal'),cube([7,13,7],[9,15,9],'metal'),cube([7,5,7],[9,7,9],'metal'),cube([7,1,7],[9,5,9],'metal'),cube([9,1,7],[12,3,9],'metal')],tex,display)
model('item/hotel_drink',[cube([4,1,4],[12,2,12],'metal'),cube([5,2,5],[11,12,11],'paper'),cube([6,3,6],[10,11,10],'food'),cube([5,12,5],[11,13,11],'metal')],tex,display)
model('item/hotel_photograph',[cube([1,1,7],[15,15,8],'paper'),cube([2,2,6.8],[14,14,7.1],'photo')],tex,display)
roles=[('porter',(83,28,35)),('bartender',(231,220,193)),('scarred_guest',(83,84,62)),('pianist',(35,34,32)),('dancer',(49,43,37)),('dancer_dress',(87,66,97))]
for index,(name,cloth) in enumerate(roles):
 im=Image.new('RGBA',(128,64),cloth+(255,));d=ImageDraw.Draw(im);skin=(183-index*5,140-index*3,106-index*2,255);hair=(53+index*6,43+index*3,33+index*2,255)
 d.rectangle((0,0,63,15),fill=skin);d.rectangle((8,0,15,3),fill=hair);d.rectangle((0,8,7,15),fill=hair);d.rectangle((24,8,31,15),fill=hair);d.rectangle((8,8,15,9),fill=hair)
 d.rectangle((10,11,10,12),fill=(31,28,25,255));d.rectangle((13,11,13,12),fill=(31,28,25,255));d.rectangle((11,14,13,14),fill=(98,57,42,255))
 if index==2:d.line((9,10,12,13,14,15),fill=(112,51,44,255),width=1)
 d.rectangle((0,16,15,31),fill=(28,25,25,255));d.rectangle((40,26,55,31),fill=skin);d.rectangle((20,16,27,20),fill=(213,202,176,255));d.line((24,21,24,30),fill=(153,117,55,255))
 d.rectangle((64,0,95,23),fill=cloth+(255,));d.line((65,7,93,7),fill=(172,134,57,255),width=2);d.rectangle((96,0,127,22),fill=(225,213,181,255));d.line((100,14,114,14),fill=(172,161,136,255))
 for y in range(33,63,4):d.line((64,y,96,y+1),fill=tuple(max(0,c-12) for c in cloth)+(255,))
 save('textures/entity/hotel_'+name+'.png',im)
im=Image.new('RGBA',(32,16),(88,65,46,255));d=ImageDraw.Draw(im)
for x in range(0,32,3):d.line((x,0,x,15),fill=(47,37,31,255))
d.rectangle((16,0,31,7),fill=(154,113,49,255));save('textures/entity/hotel_hose.png',im)
# Short original sequences; no excerpts from an existing performance or composition.
def audio(name,seconds,sample):
 rate=22050
 with tempfile.TemporaryDirectory() as tmp:
  wav=Path(tmp)/'audio.wav'
  with wave.open(str(wav),'wb') as f:
   f.setnchannels(1);f.setsampwidth(2);f.setframerate(rate);f.writeframes(b''.join(struct.pack('<h',int(max(-1,min(1,sample(i/rate)))*24000)) for i in range(int(seconds*rate))))
  out=A/'sounds/hotel'/f'{name}.ogg';out.parent.mkdir(parents=True,exist_ok=True);subprocess.run(['ffmpeg','-loglevel','error','-y','-i',str(wav),'-c:a','libvorbis','-q:a','4',str(out)],check=True)
notes=[261.63,329.63,293.66,196,246.94,220,293.66,261.63]
def piano(t):
 n=min(7,int(t/.5));u=t-n*.5;freq=notes[n];return .25*math.exp(-u*7)*(math.sin(2*math.pi*freq*t)+.24*math.sin(4*math.pi*freq*t))
audio('piano',4,piano)
audio('orchestra',4,lambda t:piano(t)*.7+.09*math.sin(2*math.pi*130.81*t)*min(1,t*4)*min(1,(4-t)*4))
audio('bell',1.2,lambda t:.28*math.exp(-4*t)*(math.sin(2*math.pi*784*t)+.3*math.sin(2*math.pi*1147*t)))
audio('hose',.8,lambda t:.13*math.sin(2*math.pi*(76+24*math.sin(t*19))*t)*math.sin(math.pi*t/.8))
sounds=json.loads((A/'sounds.json').read_text())
for name in ['piano','orchestra','bell','hose']:sounds['hotel.'+name]={'sounds':[{'name':'the_oldest_house:hotel/'+name,'stream':False}]}
write('sounds.json',sounds)
lang=json.loads((A/'lang/en_us.json').read_text());lang.update({'item.the_oldest_house.hotel_master_key':'Hotel master key','item.the_oldest_house.hotel_drink':'Complimentary drink','item.the_oldest_house.hotel_photograph':'Closing-night photograph','block.the_oldest_house.hotel_prop':'Hotel fixture','entity.the_oldest_house.hotel_actor':'Hotel guest','entity.the_oldest_house.hotel_hose':'Fire hose'});write('lang/en_us.json',lang)
print('Authored 160 prop variants, three native keepsakes, seven cast/hose skins and four original sounds.')
