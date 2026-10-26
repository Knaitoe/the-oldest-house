#!/usr/bin/env python3
"""Authored wet growth, fibrous tearing and mouthless gurgles; no speech or sampled recordings."""
from pathlib import Path
import argparse,json,math,subprocess
import numpy as np

ROOT=Path(__file__).resolve().parents[1]/'src/main/resources/assets/the_oldest_house'
RATE=22050
CAPTIONS={'grow':'Flesh swells','tear':'Wet flesh tears','gurgle':'Wet gurgling'}
COUNTS={'grow':6,'tear':6,'gurgle':4}
def cue(kind,variant):
    rng=np.random.default_rng(131900+variant*7919+{'grow':0,'tear':170,'gurgle':370}[kind])
    seconds=rng.uniform(*{'grow':(.78,1.55),'tear':(.62,1.3),'gurgle':(1.2,2.15)}[kind]);n=int(RATE*seconds);t=np.arange(n)/RATE
    def band(lo,hi):
        noise=rng.normal(0,1,n);f=np.fft.rfftfreq(n,1/RATE)
        response=1/(1+(f/hi)**6)*(f/(f+lo))**2
        result=np.fft.irfft(np.fft.rfft(noise)*response,n=n)
        return result/max(np.std(result),1e-9)
    # Air in liquid, thick wet friction and short individual fibres have different bands.
    body=band(35,420);wet=band(130,1700);fibres=band(1100,6200)
    knots=rng.uniform(.05,1,12);envelope=np.interp(t,np.linspace(0,seconds,12),knots)
    out=np.zeros(n)
    if kind=='grow':
        env=np.sin(np.pi*t/seconds)**rng.uniform(.6,1.5)
        out=(body*.16+wet*.095+fibres*.012)*env*envelope
        count=rng.integers(8,17)
    elif kind=='tear':
        env=np.minimum(t/.015,1)*np.exp(-t*rng.uniform(1.7,3.2))
        out=(body*.19+wet*.10+fibres*.055)*env*(.25+envelope)
        count=rng.integers(13,25)
        # Uneven membrane failures; each tiny rip has its own attack and decay.
        for at in sorted(rng.uniform(.01,seconds*.86,rng.integers(12,22))):
            start=int(at*RATE);length=min(n-start,int(RATE*rng.uniform(.008,.055)))
            u=np.arange(length)/RATE;out[start:start+length]+=fibres[start:start+length]*rng.uniform(.035,.12)*np.exp(-u*rng.uniform(60,180))
    else:
        env=np.minimum(t/.07,1)*np.minimum((seconds-t)/.2,1)
        out=(body*.15+wet*.055+fibres*.008)*env*envelope
        count=rng.integers(12,24)
    # Irregular collapsing bubbles, without a repeating oscillation or a fixed pulse pattern.
    for at in rng.uniform(.015,seconds*.92,count):
        start=int(at*RATE);length=min(n-start,int(RATE*rng.uniform(.025,.13)));u=np.arange(length)/RATE
        frequency=rng.uniform(95,330)*np.exp(-u*rng.uniform(7,22))
        phase=2*np.pi*np.cumsum(frequency)/RATE
        burst=(np.sin(phase)+.3*np.sin(phase*1.53)+rng.normal(0,.15,length))*np.exp(-u*rng.uniform(22,65))
        out[start:start+length]+=burst*rng.uniform(.05,.18)
    out=np.tanh(out*1.35);out=out/max(np.abs(out).max(),1e-9)*rng.uniform(.65,.84)
    out*=np.minimum(t/.005,1)*np.minimum((seconds-t)/.035,1)
    return np.asarray(out*32767,dtype='<i2').tobytes()
def main():
    ap=argparse.ArgumentParser();ap.add_argument('--check',action='store_true');args=ap.parse_args();out=ROOT/'sounds/literary';out.mkdir(parents=True,exist_ok=True)
    sounds=json.loads((ROOT/'sounds.json').read_text());language=json.loads((ROOT/'lang/en_us.json').read_text())
    for kind,caption in CAPTIONS.items():
        event='cellar_flesh_'+kind;files=[]
        for v in range(COUNTS[kind]):
            name=event+'_'+str(v+1);dest=out/(name+'.ogg');files.append({'name':'the_oldest_house:literary/'+name,'attenuation_distance':16})
            if args.check:
                result=subprocess.run(['ffmpeg','-v','error','-i',str(dest),'-f','s16le','-ac','1','-ar',str(RATE),'-'],capture_output=True,check=True)
                decoded=np.frombuffer(result.stdout,dtype='<i2');assert len(decoded)>RATE*.3 and 800<np.max(np.abs(decoded))<32767,name
            else:
                subprocess.run(['ffmpeg','-v','error','-y','-f','s16le','-ar',str(RATE),'-ac','1','-i','-','-c:a','libvorbis','-q:a','5','-fflags','+bitexact','-flags:a','+bitexact','-map_metadata','-1',str(dest)],input=cue(kind,v),check=True)
        subtitle='subtitles.the_oldest_house.'+event
        for i,clip in enumerate(files,1):
            single={'subtitle':subtitle,'sounds':[clip]}
            if args.check:assert sounds[event+'_'+str(i)]==single
            else:sounds[event+'_'+str(i)]=single
        if args.check:assert sounds[event]['sounds']==files and language[subtitle]==caption,event
        else:sounds[event]={'subtitle':subtitle,'sounds':files};language[subtitle]=caption
    if not args.check:
        (ROOT/'sounds.json').write_text(json.dumps(sounds,indent=2,ensure_ascii=False)+'\n');(ROOT/'lang/en_us.json').write_text(json.dumps(language,indent=2,ensure_ascii=False)+'\n')
    print('Verified sixteen distinct decoded organic cues, individual nonrepeating variation events and subtitled growth/tearing/gurgles.' if args.check else 'Generated sixteen organic wet tissue variations.')
if __name__=='__main__':main()
