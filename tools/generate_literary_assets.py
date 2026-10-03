"""Authored Minecraft UV art and native meshes for the remaining literary sites.

Deterministic layered material drawing, joint-specific skin atlases, finite keepsakes,
mono positional effects and original, clearly synthetic wax-cylinder performances.
"""
from pathlib import Path
from PIL import Image, ImageDraw
import json, math, random, struct, wave, subprocess, tempfile
ROOT=Path(__file__).resolve().parents[1]
A=ROOT/'src/main/resources/assets/the_oldest_house'
D=ROOT/'src/main/resources/data/the_oldest_house'

def write(path,obj,root=A):
 p=root/path;p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(obj,indent=2)+'\n')
def save(path,im):
 p=A/path;p.parent.mkdir(parents=True,exist_ok=True);im.save(p,optimize=True)
def material(name,col,kind):
 rng=random.Random(name);im=Image.new('RGBA',(64,64));d=ImageDraw.Draw(im)
 for y in range(64):
  for x in range(64):
   grain=math.sin(x*.37+math.sin(y*.035)*3)*5 if kind=='wood' else math.sin(y*.17+x*.11)*2
   n=rng.randrange(-3,4)+int(grain);im.putpixel((x,y),tuple(max(0,min(255,c+n)) for c in col)+(255,))
 if kind=='wood':
  for x in (1,30,33,62):d.line((x,0,x,63),fill=tuple(max(0,c-16) for c in col)+(255,))
  d.rectangle((4,5,27,58),outline=tuple(min(255,c+22) for c in col)+(255,));d.rectangle((7,9,24,54),outline=tuple(max(0,c-10) for c in col)+(255,))
 elif kind=='siding':
  for y in range(0,64,10):d.line((0,y,63,y),fill=(49,47,38,255));d.line((0,y+1,63,y+1),fill=(167,158,125,255))
  for x,y in ((8,8),(49,29),(14,50)):d.line((x,y,x+11,y),fill=(61,66,51,255));d.point((x-2,y-2),fill=(74,79,65,255))
 elif kind=='crack':
  d.line((31,0,27,9,33,18,24,26,30,35,23,44,26,53,19,63),fill=(13,14,19,255),width=4)
  d.line((27,9,18,11,11,19),fill=(25,25,29,255),width=2);d.line((30,35,40,39,49,42),fill=(22,23,26,255),width=2)
 elif kind=='red':
  for x in (0,31,63):d.line((x,0,x,63),fill=(52,24,21,255))
  for y in (0,31,63):d.line((0,y,63,y),fill=(52,24,21,255))
  d.line((2,51,21,36,25,27,50,8),fill=(118,49,34,255),width=3);d.line((7,53,24,40,32,36,46,27),fill=(93,38,30,255),width=2)
 elif kind=='snow':
  for x in (24,27,34,38):d.line((x,0,x-4,17,x+2,42,x-3,63),fill=(147,165,177,255),width=2)
 elif kind=='trunk':
  for x in range(4,64,9):d.line((x,0,x+3,21,x-2,63),fill=(31,26,20,255),width=2)
  d.line((18,18,18,37,27,37),fill=(191,161,112,255),width=2);d.line((35,18,44,18,39,18,39,37),fill=(191,161,112,255),width=2)
 elif kind=='metal':
  for y in (1,62):d.line((0,y,63,y),fill=tuple(min(255,c+25) for c in col)+(255,))
  for x,y in ((4,4),(59,4),(4,59),(59,59)):d.ellipse((x-1,y-1,x+1,y+1),fill=(36,36,35,255))
 save('textures/block/'+name+'.png',im);return im
for n,col,k in [('usher_crack',(70,73,82),'crack'),('crimson_floor',(111,46,36),'red'),('father_drag_snow',(219,227,231),'snow'),('rabbit_carved_trunk',(89,62,40),'trunk'),('manufactured_siding',(125,123,103),'siding'),('literary_dark_panel',(60,40,33),'wood'),('literary_oak',(77,48,30),'wood'),('literary_iron',(61,64,65),'metal'),('literary_brass',(142,113,54),'metal'),('literary_wax',(106,63,32),'metal'),('literary_paper',(218,208,177),'paper'),('literary_glass',(162,181,172),'metal'),('literary_hide',(102,72,51),'wood')]:
 material(n,col,k)
 if not n.startswith('literary_') or n=='literary_dark_panel':
  write('blockstates/'+n+'.json',{'variants':{'':{'model':'the_oldest_house:block/'+n}}});write('models/block/'+n+'.json',{'parent':'minecraft:block/cube_all','textures':{'all':'the_oldest_house:block/'+n}})
for peeled in (False,True):
 im=material('elk_tape_peeled' if peeled else 'elk_tape',(93,85,74),'paper');d=ImageDraw.Draw(im)
 if peeled:
  d.line((3,3,3,59,60,59,60,3,3,3),fill=(64,62,54,255),width=3);d.line((8,3,49,3),fill=(111,104,89,255),width=2)
 else:d.line((3,3,3,59,60,59,60,3,3,3),fill=(206,199,163,255),width=3)
 save('textures/block/'+('elk_tape_peeled' if peeled else 'elk_tape')+'.png',im)
 n='elk_tape_peeled' if peeled else 'elk_tape';write('blockstates/'+n+'.json',{'variants':{'':{'model':'the_oldest_house:block/'+n}}});write('models/block/'+n+'.json',{'parent':'minecraft:block/cube_all','textures':{'all':'the_oldest_house:block/'+n}})
photo=Image.new('RGBA',(64,64),(184,158,119,255));d=ImageDraw.Draw(photo);d.rectangle((3,3,60,60),outline=(68,45,29,255),width=3);d.rectangle((8,8,55,55),fill=(126,116,94,255))
for i in range(4):
 x=15+i*9;d.ellipse((x,17+i%2*4,x+6,25+i%2*4),fill=(205,179,145,255));d.rectangle((x-2,26+i%2*4,x+8,48),fill=(58+i*7,53+i*4,49+i*3,255))
save('textures/block/literary_photo.png',photo)
clock=Image.new('RGBA',(64,64),(192,178,144,255));d=ImageDraw.Draw(clock);d.ellipse((3,3,60,60),outline=(63,46,27,255),width=3)
for i in range(12):
 a=i*math.pi/6;d.line((32+24*math.sin(a),32-24*math.cos(a),32+20*math.sin(a),32-20*math.cos(a)),fill=(34,30,25,255),width=2)
d.line((32,32,32,14),fill=(28,27,24,255),width=2);d.line((32,32,47,36),fill=(28,27,24,255),width=2);save('textures/block/literary_clock_face.png',clock)

def cube(a,b,tex='wood',uv=(0,0,16,16),rot=None):
 e={'from':a,'to':b,'faces':{f:{'texture':'#'+tex,'uv':list(uv)} for f in ['north','south','east','west','up','down']}}
 if rot:e['rotation']=rot
 return e
tex={k:'the_oldest_house:block/literary_'+v for k,v in {'wood':'oak','iron':'iron','brass':'brass','wax':'wax','paper':'paper','glass':'glass','hide':'hide','photo':'photo','face':'clock_face'}.items()};tex['particle']=tex['wood']
base={
 'clock':[cube([2,0,2],[14,3,14]),cube([4,3,4],[12,23,12]),cube([3,23,3],[13,31,13]),cube([4,24,2.7],[12,30,3],'face'),cube([6,5,3.7],[10,20,4],'brass'),cube([7,10,3.4],[9,19,3.8],'iron')],
 'coffin':[cube([1,0,0],[15,2,16]),cube([1,2,0],[3,8,16]),cube([13,2,0],[15,8,16]),cube([3,2,0],[13,8,2]),cube([3,2,14],[13,8,16]),cube([3,2,2],[13,3,14],'paper'),cube([0,3,4],[1,5,7],'brass'),cube([15,3,9],[16,5,12],'brass')],
 'fan':[cube([6,12,6],[10,16,10],'iron'),cube([5,9,5],[11,12,11],'brass'),cube([6,6,6],[10,9,10],'glass')],
 'miniature':[cube([0,0,0],[16,3,16]),cube([1,3,1],[15,4,15],'paper'),cube([0,3,0],[1,5,16]),cube([15,3,0],[16,5,16]),cube([1,3,0],[15,5,1]),cube([1,3,15],[15,5,16])],
 'camera':[cube([4,0,4],[12,3,12],'iron'),cube([5,3,4],[11,10,10],'iron'),cube([6,5,1],[10,9,4],'glass'),cube([3,6,6],[5,8,10],'brass'),cube([6,10,5],[10,12,8],'iron')],
 'tripod':[cube([7,0,7],[9,19,9],'iron')]+[cube([2+i*5,0,3],[4+i*5,2,5],'iron') for i in range(3)]+[cube([4,17,4],[12,23,11],'iron'),cube([6,18,1],[10,22,4],'glass')],
 'light_stand':[cube([1,0,7],[15,2,9],'iron'),cube([7,0,1],[9,2,15],'iron'),cube([7,2,7],[9,26,9],'iron'),cube([2,22,5],[14,30,9],'iron'),cube([3,23,4.7],[13,29,5],'paper'),cube([1,21,4],[3,31,5],'iron'),cube([13,21,4],[15,31,5],'iron')],
 'tape_x':[cube([2,0,7],[14,.3,9],'paper'),cube([7,0,2],[9,.3,14],'paper')],
 'jar':[cube([4,0,4],[12,1,12],'glass'),cube([4,1,4],[12,10,12],'glass'),cube([3.5,10,3.5],[12.5,12,12.5],'brass'),cube([6,2,6],[10,4,10],'hide'),cube([5,5,8],[9,6,9],'hide')],
 'belongings':[cube([1,0,2],[10,6,13],'hide'),cube([2,6,3],[12,9,12],'paper'),cube([9,0,6],[15,5,14]),cube([5,9,4],[14,11,9],'hide'),cube([2,9,2],[8,10,6],'photo')],
 'cylinder':[cube([4,0,4],[12,1,12],'brass'),cube([5,1,5],[11,10,11],'wax'),cube([4,10,4],[12,11,12],'brass'),cube([5,4,4.8],[11,7,5],'paper')],
 'photo':[cube([0,0,13],[16,16,16]),cube([1,1,12.8],[15,15,13],'photo')],
 'trunk_mark':[cube([1,1,14],[15,15,16],'hide')],
 'elk_hide':[cube([0,0,0],[16,4,16],'hide'),cube([3,4,2],[13,7,14],'hide'),cube([12,3,1],[16,6,6],'hide')],
 'television':[cube([1,0,4],[15,12,15]),cube([2,2,3.7],[12,10,4],'iron'),cube([12,3,3.5],[14,5,4],'brass'),cube([12,7,3.5],[14,9,4],'brass'),cube([5,12,8],[11,14,11],'iron')],
 'receiver':[cube([2,0,3],[14,3,13],'iron'),cube([3,3,5],[13,6,11],'iron'),cube([5,3,3],[11,4,6],'brass'),cube([3,5,6],[5,7,10],'paper'),cube([11,5,6],[13,7,10],'paper')],
 'page':[cube([2,0,2],[14,.25,14],'paper'),cube([2,.25,2],[5,1.5,5],'paper',rot={'origin':[2,.25,2],'axis':'z','angle':22.5})],
 'ledger':[cube([2,0,2],[14,1,14],'hide'),cube([3,1,3],[13,3,13],'paper'),cube([2,3,2],[14,4,14],'hide'),cube([7,3,2],[9,4,14],'brass')]
}
variants={}
for kind,els in base.items():
 for stage in range(4):
  elements=list(els)
  if kind=='coffin':elements.append(cube([1,8,0],[15,9,16],rot=None if stage else {'origin':[1,8,0],'axis':'z','angle':-45}))
  model={'textures':tex,'elements':elements,'ambientocclusion':True};name='literary_'+kind+'_'+str(stage);write('models/block/'+name+'.json',model)
  for facing,rot in [('north',0),('east',90),('south',180),('west',270)]:variants[f'facing={facing},kind={kind},stage={stage}']={'model':'the_oldest_house:block/'+name,'y':rot}
write('blockstates/literary_prop.json',{'variants':variants})
display={'gui':{'rotation':[25,-35,0],'translation':[0,0,0],'scale':[.9,.9,.9]},'ground':{'translation':[0,2,0],'scale':[.5,.5,.5]},'thirdperson_righthand':{'rotation':[0,0,0],'translation':[0,2,0],'scale':[.65,.65,.65]},'firstperson_righthand':{'rotation':[0,-45,15],'translation':[0,2,0],'scale':[.75,.75,.75]}}
items={'elk_tooth':[cube([6,1,7],[10,3,9],'paper'),cube([5,3,7],[11,9,9],'paper'),cube([6,9,7],[10,14,9],'paper'),cube([7,14,7],[9,16,9],'paper')],
 'wheel_house_key':[cube([4,10,7],[6,16,9],'brass'),cube([10,10,7],[12,16,9],'brass'),cube([6,14,7],[10,16,9],'brass'),cube([6,10,7],[10,12,9],'brass'),cube([7,2,7],[9,10,9],'brass'),cube([9,2,7],[12,4,9],'brass')],
 'father_knife':[cube([7,0,7],[9,7,9]),cube([5,7,6],[11,8,10],'brass'),cube([7,8,7],[10,15,9],'iron'),cube([8,15,7],[9,17,9],'iron')],
 'grasshopper_jar':base['jar'],'literary_photograph':base['photo'],
 'family_film':[cube([2,5,3],[14,11,13],'iron'),cube([3,6,2],[7,10,3],'brass'),cube([9,6,2],[13,10,3],'brass'),cube([6,7,1.8],[10,9,2],'paper')],
 'father_meal':[cube([3,0,3],[13,2,13],'paper'),cube([4,2,4],[12,4,12],'hide'),cube([5,4,5],[11,5,11],'wax')],
 'family_stew':[cube([4,0,4],[12,2,12]),cube([3,2,3],[13,5,13],'iron'),cube([4,5,4],[12,6,12],'wax')]}
for n in ('one','two','three'):items['wax_cylinder_'+n]=base['cylinder']
for name,els in items.items():write('models/item/'+name+'.json',{'textures':tex,'elements':els,'display':display})

# Standard humanoid faces and limbs occupy the left 64 pixels; tailoring occupies the right.
skins={'mother':((211,182,153),(59,43,48),(46,31,25)), 'dancer':((190,159,127),(36,45,61),(24,20,19)), 'red_figure':((110,67,54),(117,27,23),(49,21,18)), 'coffin_woman':((206,177,152),(191,183,165),(62,41,31)), 'lady':((184,179,164),(174,172,159),(38,28,27)), 'silhouette':((24,23,25),(22,22,25),(20,20,22)), 'brother':((168,138,109),(70,83,107),(42,29,23)), 'father':((164,132,101),(83,73,57),(46,35,28)), 'visitor':((176,148,116),(56,46,40),(111,104,88)), 'killer':((104,98,87),(31,34,31),(26,24,21)), 'family_father':((194,160,126),(60,76,72),(53,37,26)), 'family_mother':((216,184,154),(115,92,85),(60,40,27)), 'family_child':((197,164,130),(102,107,123),(54,36,26)), 'old_man':((189,161,131),(74,64,50),(190,183,163)), 'camera':((0,0,0),(0,0,0),(0,0,0))}
for i,col in enumerate([(63,69,52),(87,73,65),(82,67,79),(43,54,70)]):skins['stranger_'+str(i)]=((176+i*10,143+i*8,111+i*7),col,(38+i*19,29+i*14,23+i*9))
for role in ['family_father','family_mother','family_child']:
 skin,coat,hair=skins[role];skins[role+'_changed']=((min(235,skin[0]+24),max(110,skin[1]-14),max(85,skin[2]-22)),tuple(reversed(coat)),(112,83,48))
skins['father_bandaged']=skins['father']
for i in range(1,4):skins['silhouette_'+str(i)]=tuple(tuple(int(a+(b-a)*i/3) for a,b in zip(old,new)) for old,new in zip(skins['silhouette'],skins['father']))
for role,(skin,coat,hair) in skins.items():
 im=Image.new('RGBA',(128,64),(0,0,0,0));d=ImageDraw.Draw(im);rng=random.Random(role)
 for rect,col in [((0,0,32,16),skin),((16,16,40,32),coat),((40,16,56,32),coat),((0,16,16,32),(42,40,35)),((16,48,32,64),(42,40,35)),((32,48,48,64),coat),((64,28,96,64),coat),((96,28,127,63),coat),((64,0,82,18),(188,175,149) if role=='dancer' else skin),((82,0,112,28),coat),((112,0,127,14),(194,183,164))]:d.rectangle(rect,fill=col+(255,))
 # Hairline, asymmetric cheek shade, collar, placket, belt, cuffs and shoe seams.
 d.rectangle((8,8,15,9),fill=hair+(255,));d.rectangle((8,10,8,13),fill=hair+(255,));d.rectangle((15,10,15,13),fill=tuple(max(0,c-16) for c in skin)+(255,))
 if role not in ('lady','silhouette','camera') and not role.startswith('silhouette'):
  d.point((10,11),fill=(31,28,26,255));d.point((13,11),fill=(31,28,26,255));d.line((11,14,12,14),fill=(106,72,58,255))
 d.polygon([(20,20),(24,23),(28,20)],fill=(180,167,146,255));d.line((24,23,24,31),fill=tuple(max(0,c-16) for c in coat)+(255,))
 for y in (24,27,30):d.point((25,y),fill=(152,132,91,255))
 for x in (20,24,28):d.line((x,29,x+2,29),fill=(36,32,28,255))
 d.line((4,29,11,29),fill=(64,60,50,255));d.line((20,61,27,61),fill=(64,60,50,255))
 if role=='old_man' or role=='visitor':d.rectangle((10,13,13,15),fill=hair+(255,));d.line((9,10,14,10),fill=hair+(255,))
 if role=='father_bandaged':
  for y in range(1,14,3):d.line((112,y,127,y+2),fill=(151,136,112,255))
 if role in ('dancer','red_figure','killer'):d.rectangle((65,2,71,9),fill=(190,173,136,255) if role=='dancer' else (61,37,32,255));d.line((66,5,67,5),fill=(26,23,21,255));d.line((70,5,71,5),fill=(26,23,21,255))
 # UV-specific textile stitches on the coat/gown extension, with deliberate seams.
 for x in (65,73,81,89,97,105,113,121):d.line((x,30,x,62),fill=tuple(max(0,c-14) for c in coat)+(255,))
 d.line((64,43,95,43),fill=tuple(min(255,c+10) for c in coat)+(255,))
 if role=='lady':d.rectangle((8,8,15,15),fill=skin+(255,))
 save('textures/entity/literary_'+role+'.png',im)
for wound in (False,True):
 im=Image.new('RGBA',(128,64),(104,73,48,255));d=ImageDraw.Draw(im)
 d.rectangle((0,20,65,34),fill=(150,111,72,255));d.rectangle((50,0,78,26),fill=(58,41,29,255));d.rectangle((80,0,111,29),fill=(127,86,52,255));d.rectangle((80,20,107,29),fill=(49,39,30,255));d.rectangle((112,0,127,9),fill=(153,121,80,255));d.rectangle((0,49,30,63),fill=(40,32,26,255));d.point((88,8),fill=(18,17,14,255));d.point((105,8),fill=(18,17,14,255))
 for x in range(4,62,7):d.line((x,5,x+4,21),fill=(115,82,53,255))
 if wound:d.line((29,9,36,16,31,22),fill=(83,26,21,255),width=3);d.line((31,10,36,16),fill=(124,42,26,255))
 save('textures/entity/literary_elk'+('_wounded' if wound else '')+'.png',im)
fan=Image.new('RGBA',(128,64),(90,56,31,255));d=ImageDraw.Draw(fan);d.rectangle((0,0,23,15),fill=(147,120,61,255));d.rectangle((24,0,79,9),fill=(87,51,29,255));d.line((26,3,77,3),fill=(143,95,48,255));save('textures/entity/literary_fan.png',fan)

# All positional effects are mono; original cylinder prose is audibly performed by libflite.
def audio(name,seconds,sample):
 rate=22050
 with tempfile.TemporaryDirectory() as tmp:
  wav=Path(tmp)/'s.wav'
  with wave.open(str(wav),'wb') as f:
   f.setnchannels(1);f.setsampwidth(2);f.setframerate(rate);f.writeframes(b''.join(struct.pack('<h',int(max(-1,min(1,sample(i/rate)))*22000)) for i in range(int(seconds*rate))))
  out=A/'sounds/literary'/f'{name}.ogg';out.parent.mkdir(parents=True,exist_ok=True);subprocess.run(['ffmpeg','-loglevel','error','-y','-i',str(wav),'-c:a','libvorbis','-q:a','4',str(out)],check=True)
audio('scratch',2,lambda t:.09*math.sin(2*math.pi*(530+71*math.sin(t*29))*t)*(math.sin(t*13)**8))
audio('drip',1.5,lambda t:.21*math.exp(-40*(t%.5))*math.sin(2*math.pi*(820-230*(t%.5))*t))
audio('hooves',2,lambda t:.2*math.exp(-45*(t%.18))*math.sin(2*math.pi*72*t))
audio('giggle',1.4,lambda t:.11*math.sin(2*math.pi*(350+90*math.sin(t*16))*t)*math.sin(t*14)**4)
audio('blizzard',3,lambda t:.11*(math.sin(2*math.pi*107*t)+.2*math.sin(2*math.pi*2231*t))*math.sin(t*1.4)**2)
notes=[220,261.63,293.66,329.63,293.66,261.63,246.94,196]
audio('party',8,lambda t:.13*math.sin(2*math.pi*notes[min(7,int(t))]*t)*math.exp(-3*(t%1))+.045*math.sin(2*math.pi*110*t))
spoken={
 'cylinder_one':"I thought the roof was being repaired. The snow came through it the next winter too. I learned to move my chair before it fell.",
 'cylinder_two':"He kept the house warm in the rooms where people were expected. I walked farther to find a room where I could speak without being heard.",
 'cylinder_three':"I have left this voice where a hand might find it. Listen to the whole cylinder. Do not let him tell you what I meant.",
 'family_film':"We have everything we need. This is a good home. We are a family. The light is off. Please stop saying that."}
lengths={}
for name,text in spoken.items():
 out=A/'sounds/literary'/f'{name}.ogg';out.parent.mkdir(parents=True,exist_ok=True)
 with tempfile.TemporaryDirectory() as tmp:
  path=Path(tmp)/'words.txt';path.write_text(text)
  pitch=.9 if name=='cylinder_two' else 1.03 if name=='cylinder_three' else .96
  subprocess.run(['ffmpeg','-loglevel','error','-y','-f','lavfi','-i',f'flite=textfile={path}:voice=slt','-af',f'asetrate=16000*{pitch},aresample=22050,highpass=f=180,lowpass=f=2600,aecho=0.8:0.35:31:0.10','-ac','1','-c:a','libvorbis','-q:a','4',str(out)],check=True)
 duration=float(subprocess.check_output(['ffprobe','-v','error','-show_entries','format=duration','-of','default=noprint_wrappers=1:nokey=1',str(out)],text=True));lengths[name]=duration
 song='family_film' if name=='family_film' else 'wax_cylinder_'+str(['cylinder_one','cylinder_two','cylinder_three'].index(name)+1)
 write('jukebox_song/'+song+'.json',{'sound_event':'the_oldest_house:literary.'+name,'description':{'text':'A family performance' if name=='family_film' else 'A former wife — cylinder '+song[-1]},'length_in_seconds':duration,'comparator_output':4},D)
sounds=json.loads((A/'sounds.json').read_text());lang=json.loads((A/'lang/en_us.json').read_text())
for name in ['scratch','drip','hooves','giggle','blizzard','party']+list(spoken):
 sounds['literary.'+name]={'subtitle':'subtitles.the_oldest_house.literary.'+name,'sounds':[{'name':'the_oldest_house:literary/'+name,'stream':name in spoken or name=='party'}]};lang['subtitles.the_oldest_house.literary.'+name]={'scratch':'Scratching inside wood','drip':'Water dripping','hooves':'Hooves closing in','giggle':'A child giggles','blizzard':'Wind over snow','party':'Music in another room'}.get(name,'A recorded voice')
for name in items:lang['item.the_oldest_house.'+name]=name.replace('_',' ').capitalize()
lang.update({'block.the_oldest_house.literary_prop':'A fixture left in the room','entity.the_oldest_house.literary_actor':'Someone in the room','entity.the_oldest_house.literary_elk':'Cow elk'})
write('sounds.json',sounds);write('lang/en_us.json',lang);write('art/literary_audio.json',lengths,ROOT)
# Review atlas is an intermediate; native renders, not this grid, are the release proof.
paths=list((A/'textures/block').glob('literary_*.png'))+[A/'textures/block'/f'{n}.png' for n in ['usher_crack','crimson_floor','father_drag_snow','rabbit_carved_trunk','manufactured_siding','elk_tape','elk_tape_peeled']]
preview=Image.new('RGB',(768,math.ceil(len(paths)/8)*118),(29,27,25));draw=ImageDraw.Draw(preview)
for i,path in enumerate(paths):
 x=(i%8)*96;y=(i//8)*118;preview.paste(Image.open(path).convert('RGB').resize((88,88),Image.Resampling.NEAREST),(x+4,y+4));draw.text((x+3,y+94),path.stem[:16],fill=(217,205,181))
p=ROOT/'art/literary_materials_0436.png';p.parent.mkdir(parents=True,exist_ok=True);preview.save(p)
print(json.dumps({'prop_variants':len(variants),'keepsakes':len(items),'cast_atlases':len(skins),'recording_seconds':lengths}))
