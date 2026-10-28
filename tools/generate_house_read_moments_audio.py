"""Original quiet-room Foley and a 45-second slow instrumental, split at resumable five-second boundaries."""
from pathlib import Path
import argparse,array,hashlib,json,math,random,subprocess,tempfile
from generate_house_read_audio import RATE,OUT,encode
ROOT=Path(__file__).resolve().parents[1];A=ROOT/'src/main/resources/assets/the_oldest_house'
def foley(name):
 duration={'bell':1.6,'hiss':.35,'glug':1.6,'tap':3,'clock':.18,'start':.7}[name];rng=random.Random(sum(map(ord,name))+139);samples=[];low=0
 for i in range(round(RATE*duration)):
  t=i/RATE;n=rng.uniform(-1,1);low=.86*low+.14*n
  if name=='bell':v=sum(math.sin(t*math.tau*f)*g*math.exp(-t*k) for f,g,k in [(1850,.12,3),(2789,.07,4),(3931,.03,7)])
  elif name=='hiss':v=(n-low)*.035*math.sin(math.pi*t/duration)
  elif name=='clock':v=(low*.2+math.sin(t*math.tau*690)*.08)*math.exp(-t*65)
  elif name=='glug':v=(math.sin(math.tau*(170*t-18*t*t))*.09+low*.1)*max(0,math.sin(t*math.tau*4))**5
  elif name=='tap':v=(n*.025+low*.14)*(min(1,t/.2,(duration-t)/.2))
  else:v=sum((n*.1+math.sin((t-at)*math.tau*210)*.07)*math.exp(-(t-at)*45) for at in [.02,.24,.45] if t>=at)
  samples.append(v*min(1,t/.004,(duration-t)/.006))
 return samples
def song():
 duration=45;data=[0.0]*round(RATE*duration);beat=60/64;chords=[[55,59,62,66],[52,55,59,62],[57,60,64,67],[50,57,60,66]];melody=[74,71,69,67,71,74,76,74,72,69,67,66,69,72,71,67]
 def note(at,midi,length,gain,warm=False):
  freq=440*2**((midi-69)/12);start=round(at*RATE);size=min(round(length*RATE),len(data)-start)
  for i in range(max(0,size)):
   t=i/RATE;envelope=min(1,t/.025)*math.exp(-t*(1.5 if warm else 3.2))*min(1,(size-i)/(.035*RATE))
   value=math.sin(math.tau*freq*t)+.21*math.sin(math.tau*freq*2*t)+.07*math.sin(math.tau*freq*3*t)
   data[start+i]+=value*envelope*gain
 for bar in range(16):
  at=bar*3*beat;chord=chords[bar%4]
  note(at,chord[0]-12,beat*2.8,.042,True)
  for off in [1,2]:
   for j,midi in enumerate(chord[1:]):note(at+off*beat+j*.012,midi,beat*.9,.022,True)
  note(at+.025,melody[bar],beat*2.65,.026)
 # A small, fixed room reflection. Every segment comes from the same continuous master.
 dry=data[:];delay=round(.093*RATE)
 for i in range(delay,len(data)):data[i]+=dry[i-delay]*.13
 for i in range(len(data)):data[i]*=min(1,i/(RATE*.4),(len(data)-i)/(RATE*1.0))
 return data
if __name__=='__main__':
 p=argparse.ArgumentParser(description=__doc__);p.add_argument('--check',action='store_true');args=p.parse_args();manifest=ROOT/'art/house_read_audio139.json'
 if args.check:
  entries=json.loads(manifest.read_text())['sounds']
  for name,entry in entries.items():
   path=OUT/(name+'.ogg');assert hashlib.sha256(path.read_bytes()).hexdigest()==entry['sha256'];raw=subprocess.check_output(['ffmpeg','-v','error','-i',str(path),'-ar',str(RATE),'-ac','1','-f','f32le','pipe:1']);samples=array.array('f',raw);assert samples and max(abs(x) for x in samples)<.96 and sum(x*x for x in samples)>1e-5,name
  assert entries['song']['ticks']==900 and all(entries['song_'+str(i)]['ticks']==100 for i in range(9));print('Sixteen original cues/continuous music segments decode, match their masters, and do not clip.');raise SystemExit
 OUT.mkdir(parents=True,exist_ok=True)
 with tempfile.TemporaryDirectory() as temp:
  folder=Path(temp);entries={name:encode(name,foley(name),folder) for name in ['bell','hiss','glug','tap','clock','start']};music=song();entries['song']=encode('song',music,folder)
  for i in range(9):entries['song_'+str(i)]=encode('song_'+str(i),music[i*RATE*5:(i+1)*RATE*5],folder)
 manifest.write_text(json.dumps({'source':'Original locally synthesized instrumental and Foley; no copied recording.','sounds':entries},indent=2)+'\n')
 definitions=json.loads((A/'sounds.json').read_text())
 for name in entries:definitions['house_read.'+name]={'subtitle':'subtitles.the_oldest_house.house_read.'+('song' if name.startswith('song') else name),'sounds':[{'name':'the_oldest_house:house_read/'+name,'stream':name.startswith('song')}]}
 (A/'sounds.json').write_text(json.dumps(definitions,ensure_ascii=False,indent=2)+'\n');print('Wrote sixteen original cues/segments, including the 900-tick continuous master.')
