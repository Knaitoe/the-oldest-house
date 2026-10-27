#!/usr/bin/env python3
"""Original diegetic car cues and a quiet four-chord instrumental. No sampled recordings."""
from pathlib import Path
import argparse
import json
import subprocess
import numpy as np

ROOT = Path(__file__).resolve().parents[1] / 'src/main/resources/assets/the_oldest_house'
RATE = 22050
CAPTIONS = {
    'idle': 'Engine idles', 'radio': 'Soft chords from the dashboard',
    'wiper': 'Rubber wipers drag over glass', 'latch': 'Car door clicks shut',
    'lock': 'Child locks slam down', 'rise': 'Cloth snaps behind you',
    'glass': 'Windshield bursts inward', 'impact': 'Steel folds around the cabin',
}
LENGTHS = {'idle':4, 'radio':60/56*8, 'wiper':2.4, 'latch':.38, 'lock':.65, 'rise':.44, 'glass':.85, 'impact':1.55}


def cue(kind):
    seconds = LENGTHS[kind]
    n = int(seconds * RATE)
    t = np.arange(n) / RATE
    rng = np.random.default_rng(132170 + list(CAPTIONS).index(kind)*1297)

    def noise(lo, hi):
        f = np.fft.rfftfreq(n, 1/RATE)
        mask = (f/(f+lo))**2 / (1+(f/hi)**6)
        out = np.fft.irfft(np.fft.rfft(rng.normal(size=n))*mask, n=n)
        return out / max(np.std(out), 1e-9)

    def burst(at, length, frequency, volume, decay):
        start = int(at*RATE)
        size = min(n-start, int(length*RATE))
        u = np.arange(size)/RATE
        return start, volume*np.sin(2*np.pi*frequency*u)*np.exp(-u*decay)

    if kind == 'idle':
        rate = 27 + .4*np.sin(t*2.1) + .3*np.sin(t*3.7)
        phase = 2*np.pi*np.cumsum(rate)/RATE
        out = (.14*np.sin(phase*2)+.07*np.sin(phase*3)+.035*np.sin(phase*5))*(.9+.1*np.sin(phase))
        out += .018*noise(70, 800)
    elif kind == 'radio':
        out = np.zeros(n)
        beat = 60/56
        # An original small instrumental voicing: A minor, F, C, G. No song or vocal sample.
        chords = ((57,60,64,69), (53,60,65,69), (48,55,60,64), (55,59,62,67))
        for bar, chord in enumerate(chords):
            start = int(bar*beat*2*RATE)
            size = min(n-start, int(beat*2*RATE))
            u = np.arange(size)/RATE
            for note in chord:
                f = 440*2**((note-69)/12)
                tone = np.sin(2*np.pi*f*u)+.18*np.sin(2*np.pi*f*2*u)+.06*np.sin(2*np.pi*f*3*u)
                env = np.minimum(u/.12, 1)*np.exp(-u*1.15)
                out[start:start+size] += tone*env*.035
        out += noise(150, 1400)*.0015
    elif kind == 'wiper':
        friction = noise(180, 2800)
        out = np.zeros(n)
        for at, width, amp in ((.08,.72,.08),(1.18,.76,.06)):
            out += friction*np.exp(-((t-at-width/2)/(width/2))**4)*amp
        out += np.sin(2*np.pi*(520*t+4*np.sin(t*9)))*.01*(np.exp(-((t-.72)/.06)**2)+np.exp(-((t-1.88)/.07)**2))
    elif kind in ('latch','lock'):
        out = noise(70, 2800)*np.exp(-t*16)*.12
        for at, frequency, volume in ((.005,160,.25),(.035,530,.13),(.085,880,.07)):
            start, b = burst(at,.28,frequency,volume,20 if kind=='latch' else 11)
            out[start:start+len(b)] += b
        if kind == 'lock':
            for at in (.02,.16,.23):
                out += noise(450, 4200)*np.exp(-np.maximum(0,t-at)*34)*(t>=at)*.055
    elif kind == 'rise':
        out = noise(170, 4400)*np.sin(np.pi*t/seconds)**.55*.15
        out += noise(35, 180)*np.exp(-((t-.16)/.08)**2)*.11
    elif kind == 'glass':
        out = noise(1500, 8500)*np.exp(-t*8)*.22
        for at in rng.uniform(.012,.6,32):
            start, b = burst(at,.12,rng.uniform(1500,6500),rng.uniform(.02,.10),rng.uniform(28,65))
            out[start:start+len(b)] += b
    else:
        out = noise(25, 600)*np.minimum(t/.007,1)*np.exp(-t*3)*.3
        out += noise(220, 5200)*np.exp(-t*6)*.13
        for at in (.01,.08,.16,.27,.43):
            start, b = burst(at,.8,rng.uniform(70,260),rng.uniform(.04,.15),rng.uniform(4,10))
            out[start:start+len(b)] += b
    out = np.tanh(out)
    peak = max(np.abs(out).max(), 1e-9)
    out = out / peak * (.72 if kind not in ('glass','impact') else .86)
    out *= np.minimum(t/.004,1)*np.minimum((seconds-t)/.025,1)
    return np.asarray(out*32767, dtype='<i2').tobytes()


def main():
    ap = argparse.ArgumentParser(description=__doc__)
    ap.add_argument('--check', action='store_true')
    args = ap.parse_args()
    target = ROOT / 'sounds/backseat'
    target.mkdir(parents=True, exist_ok=True)
    sounds = json.loads((ROOT/'sounds.json').read_text())
    lang = json.loads((ROOT/'lang/en_us.json').read_text())
    for kind, caption in CAPTIONS.items():
        file = target/(kind+'.ogg')
        event = 'backseat.'+kind
        subtitle = 'subtitles.the_oldest_house.'+event
        definition = {'subtitle':subtitle,'sounds':[{'name':'the_oldest_house:backseat/'+kind,'attenuation_distance':18,**({'preload':True} if kind in ('rise','glass','impact','latch','lock') else {})}]}
        if args.check:
            decoded = subprocess.run(['ffmpeg','-v','error','-i',str(file),'-f','s16le','-ar',str(RATE),'-ac','1','-'],check=True,capture_output=True)
            pcm = np.frombuffer(decoded.stdout,dtype='<i2')
            assert len(pcm)>RATE*.25 and 1000<np.abs(pcm.astype(np.int32)).max()<32767,kind
            assert abs(len(pcm)/RATE-LENGTHS[kind])<.04,kind
            assert sounds[event]==definition and lang[subtitle]==caption,kind
        else:
            subprocess.run(['ffmpeg','-v','error','-y','-f','s16le','-ar',str(RATE),'-ac','1','-i','-','-c:a','libvorbis','-q:a','5','-fflags','+bitexact','-flags:a','+bitexact','-map_metadata','-1',str(file)],input=cue(kind),check=True)
            sounds[event]=definition
            lang[subtitle]=caption
    if not args.check:
        (ROOT/'sounds.json').write_text(json.dumps(sounds,indent=2,ensure_ascii=False)+'\n')
        (ROOT/'lang/en_us.json').write_text(json.dumps(lang,indent=2,ensure_ascii=False)+'\n')
    print('Eight original subtitled car cues, including the quiet four-chord radio, decode without clipping.' if args.check else 'Authored eight Backseat car cues.')


if __name__ == '__main__':
    main()
