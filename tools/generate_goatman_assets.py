#!/usr/bin/env python3
"""Exact 64x64 Minecraft skin UVs and original mono woods, wood-impact and laugh recordings."""
from pathlib import Path
import json, subprocess, tempfile, wave
import numpy as np
from PIL import Image, ImageDraw

ASSETS=Path(__file__).resolve().parents[1]/'src/main/resources/assets/the_oldest_house'

def skins():
    palette=[((113,72,47),(25,23,21),(157,73,43),(56,65,83)),
             ((167,113,76),(43,29,23),(71,112,117),(59,62,57)),
             ((194,149,112),(132,86,45),(139,119,76),(52,65,73)),
             ((135,87,62),(35,29,26),(103,113,72),(54,53,61)),
             ((220,170,133),(75,48,30),(133,69,73),(69,66,57)),
             ((177,124,85),(42,30,25),(98,126,115),(91,99,85))]
    for i,(skin,hair,shirt,trousers) in enumerate(palette):
        im=Image.new('RGBA',(64,64));d=ImageDraw.Draw(im)
        def box(rect,color):d.rectangle(rect,fill=(*color,255))
        box((0,0,31,15),hair);box((8,8,15,15),skin)
        # Hairline, ears, two tiny ordinary eyes, and a neutral mouth.
        box((8,8,15,9),hair);box((8,10,8,12),hair)
        for x in (10,13):
            box((x,11,x,11),(225,214,195));box((x,12,x,12),(39,34,29))
        box((11,14,12,14),tuple(max(0,c-32) for c in skin))
        for rect in ((16,16,39,31),(40,16,55,31),(32,48,47,63)):box(rect,shirt)
        for rect in ((0,16,15,31),(16,48,31,63)):box(rect,trousers)
        for rect in ((40,25,55,31),(32,57,47,63)):box(rect,skin)
        for rect in ((0,28,15,31),(16,60,31,63)):box(rect,(48,40,33))
        box((20,20,27,20),skin);box((22,21,25,21),skin)
        if i in (0,3):
            stripe=tuple(min(255,c+24) for c in shirt)
            for y in (24,28):box((20,y,27,y),stripe)
        if i in (1,4):box((21,25,23,27),tuple(max(0,c-20) for c in shirt))
        if i==2:
            for y in (23,26,29):box((24,y,24,y),(187,174,148))
        if i==5:
            box((0,8,7,15),hair);box((16,8,31,15),hair);box((14,10,15,13),hair)
            box((20,26,27,31),shirt);box((0,20,15,26),shirt);box((16,52,31,58),shirt)
        im.save(ASSETS/f'textures/entity/trailer_child_{i}.png')

def ogg(name,samples):
    folder=ASSETS/'sounds/goatman';folder.mkdir(parents=True,exist_ok=True)
    with tempfile.NamedTemporaryFile(suffix='.wav') as f:
        with wave.open(f.name,'wb') as w:
            w.setnchannels(1);w.setsampwidth(2);w.setframerate(24000)
            w.writeframes((np.clip(samples,-.95,.95)*32767).astype('<i2').tobytes())
        subprocess.run(['ffmpeg','-v','error','-y','-i',f.name,'-c:a','libvorbis','-q:a','5',str(folder/f'{name}.ogg')],check=True)

def audio():
    rate=24000;rng=np.random.default_rng(420);t=np.arange(int(.8*rate))/rate
    impact=np.sin(2*np.pi*78*t)*np.exp(-t*24)*.48+np.sin(2*np.pi*193*t)*np.exp(-t*40)*.24
    impact+=rng.normal(0,.14,len(t))*np.exp(-t*80)
    for start in (.035,.078,.12):
        dt=np.maximum(0,t-start);impact+=(t>=start)*np.sin(2*np.pi*870*dt)*np.exp(-dt*90)*.055
    impact*=np.minimum(1,t/.0015);ogg('hammer',impact)
    t=np.arange(int(.65*rate))/rate;voice=np.zeros_like(t)
    # Four breath-shaped vowel pulses. No copied voice or spoken story text.
    for start in (.03,.16,.29,.44):
        dt=t-start;env=np.where(dt>=0,np.exp(-np.maximum(0,dt)*28)*np.minimum(1,np.maximum(0,dt)/.01),0)
        phase=2*np.pi*231*t+.08*np.sin(t*29)
        for harmonic in range(1,12):voice+=np.sin(phase*harmonic)*np.exp(-((harmonic*231-900)/850)**2)/harmonic*.13*env
        voice+=rng.normal(0,.017,len(t))*env
    ogg('laugh',voice)
    t=np.arange(5*rate)/rate;woods=np.zeros_like(t)
    for start,freq in ((.3,2700),(1.5,3100),(3.2,2550),(4.1,2900)):
        dt=t-start;env=np.where((dt>=0)&(dt<.19),np.sin(np.clip(dt/.19,0,1)*np.pi)**2,0)
        woods+=np.sin(2*np.pi*(freq*t+230*np.sin(t*4)))*env*.042
    woods+=np.convolve(rng.normal(0,.03,len(t)),np.ones(75)/75,mode='same')
    woods*=np.minimum(1,t/.2)*np.minimum(1,(5-t)/.4);ogg('woods',woods)
    path=ASSETS/'sounds.json';sounds=json.loads(path.read_text())
    for key in ('hammer','laugh','woods'):
        sounds[f'goatman.{key}']={'sounds':[{'name':f'the_oldest_house:goatman/{key}','attenuation_distance':32 if key=='hammer' else 18}], 'subtitle':f'subtitles.the_oldest_house.goatman_{key}'}
    path.write_text(json.dumps(sounds,indent=2)+'\n')
    path=ASSETS/'lang/en_us.json';lang=json.loads(path.read_text());lang.update({
        'entity.the_oldest_house.trailer_child':'Cousin',
        'subtitles.the_oldest_house.goatman_hammer':'Someone hammers at the door',
        'subtitles.the_oldest_house.goatman_laugh':'A laugh',
        'subtitles.the_oldest_house.goatman_woods':'Birds in the woods'})
    path.write_text(json.dumps(lang,indent=2)+'\n')

if __name__=='__main__':
    skins();audio();print('Generated six exact-UV child skins and three original mono recordings.')
