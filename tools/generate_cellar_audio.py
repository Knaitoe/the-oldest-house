#!/usr/bin/env python3
"""Authored wet growth, fibrous tearing and mouthless gurgles; no speech or sampled recordings."""
from pathlib import Path
import argparse,json,math,subprocess
import numpy as np

ROOT=Path(__file__).resolve().parents[1]/'src/main/resources/assets/the_oldest_house'
RATE=22050
CAPTIONS={'grow':'Flesh swells','tear':'Wet flesh tears','gurgle':'Wet gurgling'}
def cue(kind,variant):
    rng=np.random.default_rng(13100+variant+{'grow':0,'tear':20,'gurgle':40}[kind])
    seconds={'grow':.78,'tear':.54,'gurgle':1.25}[kind];n=int(RATE*seconds);t=np.arange(n)/RATE
    noise=rng.normal(0,1,n);wet=np.convolve(noise,np.ones(17)/17,mode='same');grain=noise-np.convolve(noise,np.ones(5)/5,mode='same')
    if kind=='grow':
        env=np.sin(np.pi*t/seconds)**1.25
        phase=2*np.pi*np.cumsum(85-45*t/seconds+18*np.sin(t*31))/RATE
        out=(wet*1.3+np.sin(phase)*.21+grain*.045)*env
        pulses=((.10,.24),(.27,.34),(.48,.29),(.64,.17))
    elif kind=='tear':
        env=(1-np.exp(-t*120))*np.exp(-t*5.4)
        flutter=.18+.82*(np.sin(2*np.pi*(37+9*variant)*t)**2)
        out=(grain*.31*flutter+wet*.95+np.sin(2*np.pi*(76*t-35*t*t))*.18)*env
        pulses=((.015,.36),(.09,.24),(.19,.19),(.33,.16))
    else:
        env=np.minimum(t/.06,1)*np.minimum((seconds-t)/.18,1)
        pulse=.10+.90*np.maximum(0,np.sin(t*2*np.pi*6.3+np.sin(t*11)))**2
        out=(wet*.8+np.sin(2*np.pi*(48*t+3*np.sin(t*7)))*.14+grain*.025)*pulse*env
        pulses=tuple((at,.18) for at in (.11,.22,.37,.56,.72,.96,1.11))
    for at,level in pulses:
        s=int(at*RATE);length=min(n-s,int(RATE*.105));u=np.arange(length)/RATE
        bubble=(np.sin(2*np.pi*(210*u-650*u*u))*.7+rng.normal(0,.12,length))*np.exp(-u*48)
        out[s:s+length]+=bubble*level
    out=np.tanh(out*1.4);out=out/max(np.abs(out).max(),1e-9)*.82
    out*=np.minimum(t/.004,1)*np.minimum((seconds-t)/.025,1)
    return np.asarray(out*32767,dtype='<i2').tobytes()
def main():
    ap=argparse.ArgumentParser();ap.add_argument('--check',action='store_true');args=ap.parse_args();out=ROOT/'sounds/literary';out.mkdir(parents=True,exist_ok=True)
    sounds=json.loads((ROOT/'sounds.json').read_text());language=json.loads((ROOT/'lang/en_us.json').read_text())
    for kind,caption in CAPTIONS.items():
        event='cellar_flesh_'+kind;files=[]
        for v in range(2):
            name=event+'_'+str(v+1);dest=out/(name+'.ogg');files.append({'name':'the_oldest_house:literary/'+name,'attenuation_distance':16})
            if args.check:
                result=subprocess.run(['ffmpeg','-v','error','-i',str(dest),'-f','s16le','-ac','1','-ar',str(RATE),'-'],capture_output=True,check=True)
                decoded=np.frombuffer(result.stdout,dtype='<i2');assert len(decoded)>RATE*.3 and 800<np.max(np.abs(decoded))<32767,name
            else:
                subprocess.run(['ffmpeg','-v','error','-y','-f','s16le','-ar',str(RATE),'-ac','1','-i','-','-c:a','libvorbis','-q:a','5','-fflags','+bitexact','-flags:a','+bitexact','-map_metadata','-1',str(dest)],input=cue(kind,v),check=True)
        subtitle='subtitles.the_oldest_house.'+event
        if args.check:assert sounds[event]['sounds']==files and language[subtitle]==caption,event
        else:sounds[event]={'subtitle':subtitle,'sounds':files};language[subtitle]=caption
    if not args.check:
        (ROOT/'sounds.json').write_text(json.dumps(sounds,indent=2,ensure_ascii=False)+'\n');(ROOT/'lang/en_us.json').write_text(json.dumps(language,indent=2,ensure_ascii=False)+'\n')
    print('Verified six decoded wet tissue cues and their three native subtitled sound events.' if args.check else 'Generated six wet tissue cues and registered growth, tearing and gurgles.')
if __name__=='__main__':main()
