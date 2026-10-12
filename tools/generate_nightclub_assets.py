"""Native club meshes and an original loop; supplied ImageGen pixel textures are preserved."""
from pathlib import Path
import json, math, random, struct, subprocess, tempfile, wave

ROOT=Path(__file__).resolve().parents[1]
A=ROOT/'src/main/resources/assets/the_oldest_house'
def save(path,data):
    p=A/path;p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(data,indent=2)+'\n')
def cube(lo,hi,texture='wall',front=None):
    faces={d:{'texture':'#'+texture,'uv':[0,0,16,16]} for d in ['north','south','east','west','up','down']}
    if front:faces['north']['texture']='#'+front
    return {'from':lo,'to':hi,'faces':faces}

textures={'wall':'the_oldest_house:block/nightclub_wall','floor':'the_oldest_house:block/nightclub_floor',
          'speaker':'the_oldest_house:block/nightclub_speaker','black':'minecraft:block/black_concrete',
          'cloth':'minecraft:block/black_wool','metal':'minecraft:block/gray_concrete',
          'pink':'minecraft:block/pink_concrete','blue':'minecraft:block/cyan_concrete',
          'glass':'minecraft:block/glass','green':'minecraft:block/lime_concrete','water':'minecraft:block/blue_stained_glass'}
meshes={
 'wall':[cube([0,0,0],[16,16,16])],
 'floor':[cube([0,0,0],[16,16,16],'floor')],
 'ceiling':[cube([0,0,0],[16,16,16],'black')],
 'trim':[cube([0,0,0],[16,16,16],'black')],
 'speaker':[cube([1,0,1],[15,16,15],'black','speaker'),cube([1,16,1],[15,32,15],'black','speaker')]+[part for y in (0,16) for part in (cube([4,y+4,.7],[12,y+12,1],'metal'),cube([5,y+5,.5],[11,y+11,.7],'black'),cube([7,y+7,.3],[9,y+9,.5],'cloth'))],
 'bar':[cube([0,0,0],[16,14,16],'black'),cube([0,14,0],[16,16,16],'floor')],
 'glass':[cube([5,0,5],[11,1,11],'glass'),cube([5,1,5],[6,6,11],'glass'),cube([10,1,5],[11,6,11],'glass'),cube([6,1,5],[10,6,6],'glass'),cube([6,1,10],[10,6,11],'glass')],
 'strip':[cube([0,14,0],[16,16,16],'black'),cube([1,13.8,5],[15,14,11],'pink')],
 'spot':[cube([3,14,3],[13,16,13],'black'),cube([4,13.8,4],[12,14,12],'blue')],
 'bench':[cube([1,0,2],[15,3,15],'black'),cube([0,3,0],[16,8,16],'cloth'),cube([0,8,12],[16,17,16],'cloth')]
}
meshes['glass_empty']=list(meshes['glass'])
meshes['glass'].append(cube([6,1,6],[10,4,10],'water'))
for stage in range(3):
    deck=[cube([1,0,1],[15,7,15],'black'),cube([1,7,1],[15,8,15],'metal')]
    for x in (3,10):deck+=[cube([x,8,4],[x+3,8.3,10],'black'),cube([x+.7,8.3,6],[x+2.3,8.5,8],'metal')]
    for z in (3,6,9,12):deck.append(cube([7.3,8,z],[8.7,9,z+1],'black'))
    if stage:deck.append(cube([2,8.1,2],[4,8.2,3],'green' if stage==1 else 'pink'))
    meshes['mixer_'+str(stage)]=deck
variants={}
for kind in ('wall','floor','ceiling','trim','speaker','mixer','bar','glass','strip','spot','bench'):
    for face,rotation in (('north',0),('east',90),('south',180),('west',270)):
        for stage in range(3):variants[f'kind={kind},facing={face},stage={stage}']={'model':'the_oldest_house:block/nightclub_'+(f'mixer_{stage}' if kind=='mixer' else 'glass_empty' if kind=='glass' and stage==0 else kind),'y':rotation}
for name,elements in meshes.items():save('models/block/nightclub_'+name+'.json',{'ambientocclusion':True,'textures':dict(textures,particle=textures['floor'] if name=='floor' else textures['wall']),'elements':elements})
save('blockstates/nightclub.json',{'variants':variants})
display={'gui':{'rotation':[30,225,0],'scale':[1.4,1.4,1.4]},'firstperson_righthand':{'rotation':[0,0,0],'translation':[0,3,0],'scale':[1.5,1.5,1.5]},'thirdperson_righthand':{'rotation':[0,0,0],'translation':[0,3,0],'scale':[1.2,1.2,1.2]}}
save('models/item/nightclub_glass.json',{'parent':'the_oldest_house:block/nightclub_glass','display':display,'overrides':[{'predicate':{'the_oldest_house:empty':1},'model':'the_oldest_house:item/nightclub_glass_empty'}]})
save('models/item/nightclub_glass_empty.json',{'parent':'the_oldest_house:block/nightclub_glass_empty','display':display})
langpath=A/'lang/en_us.json';lang=json.loads(langpath.read_text());lang.update({'block.the_oldest_house.nightclub':'Nightclub furnishing','entity.the_oldest_house.nightclub_actor':'Clubgoer','subtitles.the_oldest_house.nightclub.beat':'A bass rhythm repeats','item.the_oldest_house.nightclub_glass':'Glass of water','item.the_oldest_house.nightclub_glass_empty':'Empty glass'});langpath.write_text(json.dumps(lang,indent=2)+'\n')
soundpath=A/'sounds.json';sounds=json.loads(soundpath.read_text());sounds['nightclub.beat']={'subtitle':'subtitles.the_oldest_house.nightclub.beat','sounds':[{'name':'the_oldest_house:nightclub/beat','stream':True}]};soundpath.write_text(json.dumps(sounds,indent=2)+'\n')

# An original 120-BPM, sixteen-bar loop. No sampled or licensed recording.
rate=22050;seconds=32;count=rate*seconds;audio=[0.0]*count;rng=random.Random(488)
def add(start,duration,signal):
    offset=round(start*rate)
    for j in range(round(duration*rate)):
        i=offset+j
        if i>=count:break
        audio[i]+=signal(j/rate,j)
for beat in range(64):
    at=beat*.5
    add(at,.34,lambda t,j:.5*math.sin(2*math.pi*(43*t+11*(1-math.exp(-t*30))))*math.exp(-t*16))
    if beat%4 in (1,3):add(at,.13,lambda t,j:rng.uniform(-1,1)*.2*math.exp(-t*30))
    for step in range(4):add(at+step*.125,.035,lambda t,j:rng.uniform(-1,1)*.065*math.exp(-t*95))
    note=(41.2,41.2,49,36.7)[(beat//8)%4]
    add(at+.25,.2,lambda t,j,note=note:.13*(math.sin(2*math.pi*note*t)+.2*math.sin(2*math.pi*note*2*t))*math.sin(math.pi*t/.2))
peak=max(abs(x) for x in audio);pcm=b''.join(struct.pack('<h',int(max(-1,min(1,x/peak*.72))*32767)) for x in audio)
target=A/'sounds/nightclub/beat.ogg';target.parent.mkdir(parents=True,exist_ok=True)
with tempfile.TemporaryDirectory() as tmp:
    wav=Path(tmp)/'beat.wav'
    with wave.open(str(wav),'wb') as f:f.setnchannels(1);f.setsampwidth(2);f.setframerate(rate);f.writeframes(pcm)
    subprocess.run(['ffmpeg','-y','-loglevel','error','-i',str(wav),'-c:a','libvorbis','-q:a','4',str(target)],check=True)
print('Generated 14 club meshes, two glass item models, 132 oriented states and the original 32-second bass loop; supplied textures untouched.')
