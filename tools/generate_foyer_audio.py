"""Original foyer wind, electrical buzzer, a voice at the intercom and unseen movement."""
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

SOURCE=ROOT/'art/foyer_voice_source.wav'

def speech():
    """0.4.199: the voice at the intercom is a person's (tools/make_foyer_voice.py), not summed tones."""
    with wave.open(str(SOURCE),'rb') as w:
        rate=w.getframerate();x=np.frombuffer(w.readframes(w.getnframes()),dtype='<i2').astype(np.float64)/32768
    return np.interp(np.arange(int(len(x)*RATE/rate))*rate/RATE,np.arange(len(x)),x)

def band(x,lo,hi,ring=.25):
    spectrum=np.fft.rfft(x);f=np.fft.rfftfreq(len(x),1/RATE)
    gain=np.clip((f-lo*.7)/(lo*.3),0,1)*np.clip((hi*1.3-f)/(hi*.3),0,1)
    gain*=1+ring*np.exp(-((f-1800)/350)**2)  # the cheap speaker's ring
    return np.fft.irfft(spectrum*gain,len(x))

def intercom(x,seed):
    """Through the building's intercom: a telephone-narrow speaker, a little overdriven, with a faint hum and the odd crackle.

    The owner asked twice that the voice sound human, so the line stays clear: every word carries, and only now and then does it
    thin for a moment. (The first pass drove the speaker hard and dropped whole words; it read as a machine.)
    """
    rng=np.random.default_rng(seed);t=np.arange(len(x))/RATE
    y=band(x,300,3400);y=np.tanh(y/max(1e-4,np.max(np.abs(y)))*1.3)/np.tanh(1.3)
    # Now and then the line thins for a few hundredths of a second; it never cuts out.
    gate=np.ones_like(y);at=0
    while at<len(y):
        at+=int(RATE*rng.uniform(.6,1.6))
        if rng.random()<.15:
            n=int(RATE*rng.uniform(.03,.07));fade=min(n//3,120);seg=np.ones(n)*.55;seg[:fade]=np.linspace(1,.55,fade);seg[-fade:]=np.linspace(.55,1,fade)
            gate[at:at+n]*=seg[:len(gate[at:at+n])]
    y*=gate
    hum=.012*np.sin(2*np.pi*60*t)+.006*np.sin(2*np.pi*180*t)
    crackle=np.zeros_like(y);hits=rng.integers(0,len(y),int(len(y)/RATE*2));crackle[hits]=rng.uniform(-.3,.3,len(hits));crackle=band(crackle,800,5000,0)
    hiss=band(rng.normal(0,.01,len(y)),1000,6000,0)
    return y+hum+crackle+hiss

def cues():
    out={};t=np.arange(RATE*8)/RATE;n=noise(8,136)
    out['wind']=(low(n,37)-low(n,240))*(.6+.2*np.sin(t*.91)+.15*np.sin(t*2.4))
    t=np.arange(RATE*2)/RATE
    buzz=(np.sin(2*np.pi*59.8*t)+.35*np.sin(2*np.pi*181*t)+.15*np.sin(2*np.pi*617*t))
    env=np.minimum(1,t*120)*np.minimum(1,(2-t)*120)*(np.sin(t*8.1)>-.85)
    out['buzzer']=buzz*env
    out['babble']=intercom(speech(),137)
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
            data[name]={'sha256':hashlib.sha256(path.read_bytes()).hexdigest(),'seconds':len(samples)/RATE,'authorship':'Speech from the Piper neural voice en_US-ljspeech-high (LJ Speech dataset, public domain; tools/make_foyer_voice.py), put through a procedural intercom.' if name=='babble' else 'Original procedural synthesis; no sampled speech or music.'}
    MANIFEST.write_text(json.dumps(data,indent=2)+'\n')

if __name__=='__main__':main()
