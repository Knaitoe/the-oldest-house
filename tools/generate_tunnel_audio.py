"""Original looping road wash, low motors and passing tyres filtered through concrete. No samples."""
from pathlib import Path
import argparse,json,subprocess
import numpy as np
ROOT=Path(__file__).resolve().parents[1]/'src/main/resources/assets/the_oldest_house';RATE=22050;LENGTH=16
EVENT='tunnel_traffic';CAPTION='Traffic rumbles through the tunnel walls'
def cue():
    n=RATE*LENGTH;t=np.arange(n)/RATE;f=np.fft.rfftfreq(n,1/RATE);rng=np.random.default_rng(1342710)
    def band(lo,hi):
        x=np.fft.irfft(np.fft.rfft(rng.normal(size=n))*(f/(f+lo))**2/(1+(f/hi)**6),n=n);return x/max(np.std(x),1e-8)
    # Periodic envelopes and native FFT filtering keep the wrap continuous.
    road=band(35,420);tyres=band(210,1500);out=road*.055+tyres*.012
    for start,width,level in [(2.5,1.8,.16),(6.9,2.5,.11),(12.6,1.9,.19)]:
        d=(t-start+LENGTH/2)%LENGTH-LENGTH/2;envelope=np.exp(-(d/width)**4)
        frequency=68+11*np.tanh(-d*.8);phase=2*np.pi*np.cumsum(frequency)/RATE
        motor=np.sin(phase)+.3*np.sin(phase*2)+.12*np.sin(phase*3)
        passing=(road*.5+tyres*.19+motor*.13)*envelope*level
        out+=passing+np.roll(passing,int(RATE*.27))*.36+np.roll(passing,int(RATE*.61))*.16
    out=np.tanh(out*2);out*=.55/max(np.max(np.abs(out)),.001)
    return (out*32767).astype('<i2').tobytes()
def main():
    ap=argparse.ArgumentParser();ap.add_argument('--check',action='store_true');check=ap.parse_args().check
    sounds=json.loads((ROOT/'sounds.json').read_text());lang=json.loads((ROOT/'lang/en_us.json').read_text());p=ROOT/'sounds/literary/tunnel_traffic.ogg'
    subtitle='subtitles.the_oldest_house.'+EVENT;definition={'subtitle':subtitle,'sounds':[{'name':'the_oldest_house:literary/tunnel_traffic','preload':True,'attenuation_distance':48}]}
    if check:
        pcm=np.frombuffer(subprocess.run(['ffmpeg','-v','error','-i',str(p),'-f','s16le','-ar',str(RATE),'-ac','1','-'],check=True,capture_output=True).stdout,dtype='<i2').astype(float)/32768
        assert abs(len(pcm)/RATE-LENGTH)<.04 and .02<np.sqrt(np.mean(pcm**2))<.3 and np.max(np.abs(pcm))<.99
        assert sounds[EVENT]==definition and lang[subtitle]==CAPTION
    else:
        subprocess.run(['ffmpeg','-v','error','-y','-f','s16le','-ar',str(RATE),'-ac','1','-i','-','-c:a','libvorbis','-q:a','5','-fflags','+bitexact','-flags:a','+bitexact','-map_metadata','-1',str(p)],input=cue(),check=True)
        sounds[EVENT]=definition;lang[subtitle]=CAPTION;(ROOT/'sounds.json').write_text(json.dumps(sounds,indent=2,ensure_ascii=False)+'\n');(ROOT/'lang/en_us.json').write_text(json.dumps(lang,indent=2,ensure_ascii=False)+'\n')
    print('Verified original sixteen-second muffled traffic loop and subtitle.' if check else 'Authored original tunnel traffic.')
if __name__=='__main__':main()
