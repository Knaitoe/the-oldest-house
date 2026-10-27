"""Original foyer wind, electrical buzzer, indistinct babble and unseen movement."""
import argparse
import hashlib
import json
from pathlib import Path
import subprocess
import tempfile
import wave
import numpy as np

ROOT=Path(__file__).resolve().parents[1]
FOLDER=ROOT/'src/main/resources/assets/the_oldest_house/sounds/foyer'
MANIFEST=ROOT/'art/foyer_audio136.json'
RATE=24000

def noise(seconds,seed):
    return np.random.default_rng(seed).normal(0,1,int(seconds*RATE))

def low(x,n):
    return np.convolve(x,np.ones(n)/n,'same')

def cues():
    out={};t=np.arange(RATE*8)/RATE;n=noise(8,136)
    out['wind']=(low(n,37)-low(n,240))*(.6+.2*np.sin(t*.91)+.15*np.sin(t*2.4))
    t=np.arange(RATE*2)/RATE
    buzz=(np.sin(2*np.pi*59.8*t)+.35*np.sin(2*np.pi*181*t)+.15*np.sin(2*np.pi*617*t))
    env=np.minimum(1,t*120)*np.minimum(1,(2-t)*120)*(np.sin(t*8.1)>-.85)
    out['buzzer']=buzz*env
    t=np.arange(RATE*4)/RATE
    voice=np.zeros_like(t)
    for center,width,pitch in ((.35,.13,103),(1,.22,87),(1.7,.16,98),(2.45,.28,78),(3.2,.18,91)):
        envelope=np.exp(-((t-center)/width)**2)
        source=sum(np.sin(2*np.pi*(pitch*k*t+.012*np.sin(t*11)))/k for k in range(1,10))
        voice+=source*envelope*(.6+.3*np.sin(t*38))
    out['babble']=low(voice,11)-low(voice,160)+low(noise(4,137),45)*.12
    t=np.arange(RATE*3)/RATE;steps=np.zeros_like(t);n=noise(3,138)
    for at in (.15,.86,1.75,2.6):
        d=np.maximum(0,t-at);env=np.where(t>=at,np.exp(-d*32),0)
        steps+=env*(low(n,8)+np.sin(2*np.pi*71*d)*.13)
    out['steps']=steps
    t=np.arange(RATE*3)/RATE;n=noise(3,139)
    out['inside']=(low(n,28)-low(n,220))*np.sin(np.pi*t/3)**2+.06*np.sin(2*np.pi*(45*t-2*t*t))*np.exp(-t*2)
    return out

def main():
    parser=argparse.ArgumentParser(description=__doc__);parser.add_argument('--check',action='store_true');args=parser.parse_args()
    if args.check:
        data=json.loads(MANIFEST.read_text())
        for name,row in data.items():
            path=FOLDER/(name+'.ogg');assert hashlib.sha256(path.read_bytes()).hexdigest()==row['sha256']
            raw=subprocess.check_output(['ffmpeg','-v','error','-i',str(path),'-f','f32le','-ac','1','-ar',str(RATE),'-'])
            samples=np.frombuffer(raw,dtype='<f4');assert len(samples)>RATE and np.isfinite(samples).all() and np.max(np.abs(samples))<.99
        print('Five original mono foyer cues decode without clipping.');return
    FOLDER.mkdir(parents=True,exist_ok=True);data={}
    with tempfile.TemporaryDirectory() as scratch:
        for name,samples in cues().items():
            peak=max(.0001,np.max(np.abs(samples)));samples=samples/peak*(.32 if name=='wind' else .62)
            if name=='wind':
                # Circular blend avoids a repeating seam in the native loop.
                size=RATE//4;ramp=np.linspace(0,1,size);samples[:size]=samples[:size]*ramp+samples[-size:]*(1-ramp);samples=samples[:-size]
            else:
                samples[:240]*=np.linspace(0,1,240);samples[-240:]*=np.linspace(1,0,240)
            wav=Path(scratch)/(name+'.wav')
            with wave.open(str(wav),'wb') as w:w.setparams((1,2,RATE,0,'NONE',''));w.writeframes((samples*32767).astype('<i2').tobytes())
            path=FOLDER/(name+'.ogg');subprocess.run(['ffmpeg','-v','error','-y','-i',str(wav),'-c:a','libvorbis','-q:a','4',str(path)],check=True)
            data[name]={'sha256':hashlib.sha256(path.read_bytes()).hexdigest(),'seconds':len(samples)/RATE,'authorship':'Original procedural synthesis; no sampled speech or music.'}
    MANIFEST.write_text(json.dumps(data,indent=2)+'\n')

if __name__=='__main__':main()
