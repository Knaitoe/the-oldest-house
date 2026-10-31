"""Original quiet domestic cues, with a locally rendered television murmur and private positional playback.

0.4.201 (owner: "just remove the voice lines for now ... and add text instead. it still sounds inhuman"): the adult voice
calling the reader in for supper is gone; HouseReadSites shows HouseReadTexts.SUPPER as a line of text instead.
"""
from pathlib import Path
import argparse,array,ctypes,ctypes.util,hashlib,json,math,random,subprocess,tempfile,wave
ROOT=Path(__file__).resolve().parents[1];A=ROOT/'src/main/resources/assets/the_oldest_house';OUT=A/'sounds/house_read';RATE=22050
def voice(text,name,folder):
 f=ctypes.CDLL(ctypes.util.find_library('flite') or 'libflite.so.1',mode=ctypes.RTLD_GLOBAL);f.flite_init();v=ctypes.CDLL(ctypes.util.find_library('flite_cmu_us_'+name) or ('libflite_cmu_us_'+name+'.so.1'));register=getattr(v,'register_cmu_us_'+name);register.restype=ctypes.c_void_p;register.argtypes=[ctypes.c_char_p];speaker=register(None)
 f.flite_text_to_wave.restype=ctypes.c_void_p;f.flite_text_to_wave.argtypes=[ctypes.c_char_p,ctypes.c_void_p];sound=f.flite_text_to_wave(text.replace('’',"'").encode(),speaker);path=folder/(name+'.wav');f.cst_wave_save_riff.argtypes=[ctypes.c_void_p,ctypes.c_char_p];f.cst_wave_save_riff.restype=ctypes.c_int;assert f.cst_wave_save_riff(sound,str(path).encode())==0;f.delete_wave.argtypes=[ctypes.c_void_p];f.delete_wave(sound)
 raw=subprocess.check_output(['ffmpeg','-v','error','-i',str(path),'-ar',str(RATE),'-ac','1','-f','f32le','pipe:1']);return list(array.array('f',raw))
def ambience(kind):
 duration={'rain':6,'radiator':2.6,'whistle':1.2,'dishes':3.8,'ice':.85,'step':.55}[kind];rng=random.Random(138+len(kind));out=[];low=0
 for i in range(int(RATE*duration)):
  t=i/RATE;q=t/duration;n=rng.uniform(-1,1);low=.9*low+.1*n;value=0
  if kind=='rain':value=(n*.055+low*.13)*(.7+.3*math.sin(t*.8))
  elif kind=='radiator':
   for at in [.07,.63,1.55]:
    if t>=at:value+=(math.sin((t-at)*2*math.pi*420)*.10+low*.16)*math.exp(-(t-at)*8)
   value+=low*.045
  elif kind=='whistle':
   freq=1660+90*math.sin(t*5)+12*math.sin(t*32);envelope=math.sin(math.pi*q)**.75;value=(math.sin(2*math.pi*(1660*t-18*math.cos(t*5)))*.19+n*.008)*envelope
  elif kind=='dishes':
   for at in [.05,.47,1.18,2.7,3.1]:
    if t>=at:value+=(math.sin((t-at)*2*math.pi*1780)*.06+math.sin((t-at)*2*math.pi*2633)*.035+n*.02)*math.exp(-(t-at)*20)
  elif kind=='ice':
   for at in [.02,.10,.19,.32,.46,.58]:
    if t>=at:value+=(n*.085+math.sin((t-at)*2*math.pi*2830)*.04)*math.exp(-(t-at)*35)
  else:
   for at in [.03,.30]:
    if t>=at:value+=(low*.32+math.sin((t-at)*2*math.pi*115)*.10)*math.exp(-(t-at)*27)
  fade=min(1,t/.04,(duration-t)/.05);out.append(value*max(0,fade))
 return out
def encode(name,samples,folder):
 pcm=array.array('h',(int(max(-.95,min(.95,x))*32767) for x in samples));path=folder/(name+'.wav')
 with wave.open(str(path),'wb') as w:w.setnchannels(1);w.setsampwidth(2);w.setframerate(RATE);w.writeframes(pcm.tobytes())
 target=OUT/(name+'.ogg');subprocess.run(['ffmpeg','-v','error','-y','-i',str(path),'-c:a','libvorbis','-q:a','4',str(target)],check=True)
 return {'samples':len(samples),'rate':RATE,'ticks':math.ceil(len(samples)*20/RATE),'sha256':hashlib.sha256(target.read_bytes()).hexdigest()}
def main():
 p=argparse.ArgumentParser(description=__doc__);p.add_argument('--check',action='store_true');args=p.parse_args();manifest=ROOT/'art/house_read_audio138.json'
 if args.check:
  data=json.loads(manifest.read_text());assert 'supper' not in data['sounds'] and 'words' not in data
  for name,entry in data['sounds'].items():
   path=OUT/(name+'.ogg');assert hashlib.sha256(path.read_bytes()).hexdigest()==entry['sha256'];raw=subprocess.check_output(['ffmpeg','-v','error','-i',str(path),'-ac','1','-ar',str(RATE),'-f','f32le','pipe:1']);samples=array.array('f',raw);assert max(abs(x) for x in samples)<.96 and sum(x*x for x in samples)>1e-5
  print('Seven original private domestic cues decode, match their masters, and do not clip.');return
 OUT.mkdir(parents=True,exist_ok=True)
 with tempfile.TemporaryDirectory() as temp:
  folder=Path(temp);sounds={name:encode(name,ambience(name),folder) for name in ['rain','radiator','whistle','dishes','ice','step']}
  for name,text,speaker,gain in [('news','Across the county this evening, light rain gives way to clearer skies. We will have your local forecast after the break.','rms',.18)]:
   dry=voice(text,speaker,folder);out=[];low=0
   for x in dry:low+=.085*(x-low);out.append(low*gain)
   # A small real-room reflection, not a horror effect or another overlapping take.
   source=out[:];delay=int(.045*RATE)
   for i in range(delay,len(out)):out[i]+=source[i-delay]*.075
   sounds[name]=encode(name,out,folder)
 manifest.write_text(json.dumps({'sounds':sounds},ensure_ascii=False,indent=2)+'\n')
 definitions=json.loads((A/'sounds.json').read_text())
 for name in sounds:definitions['house_read.'+name]={'subtitle':'subtitles.the_oldest_house.house_read.'+name,'sounds':[{'name':'the_oldest_house:house_read/'+name,'stream':name in ['news','rain']}]}
 (A/'sounds.json').write_text(json.dumps(definitions,ensure_ascii=False,indent=2)+'\n');print('Wrote seven original domestic cues.')
if __name__=='__main__':main()
