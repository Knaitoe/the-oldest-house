"""Original vocal timbres and domestic foley, generated deterministically without speech samples."""
from pathlib import Path
import math, random, wave, struct, subprocess, tempfile, json
root=Path(__file__).resolve().parents[1]/'src/main/resources/assets/the_oldest_house'
out=root/'sounds/literary';out.mkdir(parents=True,exist_ok=True)
sr=22050
captions={'ghost_low':'A voice croaks low','ghost_thin':'The voice turns thin','ghost_scrape':'A chair scrapes; something moves below the table','ghost_card':'Cardboard creases and tears'}
for name in captions:
    rng=random.Random(name);duration=1.3 if name=='ghost_low' else 1.1
    data=[];previous=0
    for i in range(int(sr*duration)):
        t=i/sr;edge=min(1,t/.08,(duration-t)/.14);noise=rng.uniform(-1,1)
        if name in ('ghost_low','ghost_thin'):
            f=77 if name=='ghost_low' else 228
            phase=2*math.pi*(f*t+3*math.sin(t*8));v=sum(math.sin(phase*n)/n for n in range(1,9))*.12
            v=(v+.035*noise)*(.5+.5*math.sin(t*16)**2)
        else:
            previous=previous*.7+noise*.3
            v=(noise-previous)*.18*(.2+.8*math.sin(t*(19 if name=='ghost_card' else 7))**2)
            if name=='ghost_scrape':v+=math.sin(t*2*math.pi*189)*.03
        data.append(struct.pack('<h',int(max(-1,min(1,v*edge))*32767)))
    with tempfile.TemporaryDirectory() as d:
        wav=Path(d)/'cue.wav'
        with wave.open(str(wav),'wb') as w:w.setnchannels(1);w.setsampwidth(2);w.setframerate(sr);w.writeframes(b''.join(data))
        subprocess.run(['ffmpeg','-loglevel','error','-y','-i',str(wav),'-c:a','libvorbis','-q:a','4',str(out/f'{name}.ogg')],check=True)
sounds=root/'sounds.json';s=json.loads(sounds.read_text())
lang=root/'lang/en_us.json';l=json.loads(lang.read_text())
for name,caption in captions.items():
    s['literary.'+name]={'subtitle':'subtitles.the_oldest_house.'+name,'sounds':[{'name':'the_oldest_house:literary/'+name}]}
    l['subtitles.the_oldest_house.'+name]=caption
sounds.write_text(json.dumps(s,indent=2)+'\n');lang.write_text(json.dumps(l,ensure_ascii=False,indent=2)+'\n')
print('Generated four original mono cues with registered subtitles.')
