#!/usr/bin/env python3
"""Code-native exact-UV variants and an original eight-voice wordless hymn for 0.4.12."""
from pathlib import Path
import json
import numpy as np
from PIL import Image, ImageDraw
from generate_indian_lake_assets import patch, ogg, ASSETS


def textures():
    folder=ASSETS/'textures/entity'
    memory=Image.open(folder/'lake_witch.png').convert('RGBA')
    patch(memory,(0,0,28,30),(156,116,89),5)
    patch(memory,(32,0,60,32),(150,110,82),5)
    patch(memory,(0,48,44,72),(103,87,82),6)
    patch(memory,(0,80,44,112),(83,72,71),7)
    patch(memory,(96,0,128,24),(68,49,42),2)
    d=ImageDraw.Draw(memory)
    d.line((8,10,10,10),fill=(45,34,28,255));d.line((14,10,16,10),fill=(45,34,28,255))
    d.line((10,13,13,13),fill=(96,59,47,255));d.line((6,17,26,17),fill=(132,101,78,255))
    memory.save(folder/'lake_witch_memory.png')
    for era,robe in [(1,(47,43,37)),(2,(136,128,101)),(3,(121,56,49)),(4,(89,107,117))]:
        skin=Image.open(folder/'lake_congregant.png').convert('RGBA')
        patch(skin,(0,0,32,16),(170,143,117) if era==4 else (151,163,150),3)
        patch(skin,(16,16,40,32),robe,5)
        patch(skin,(0,16,16,32),(53,57,62),5)
        patch(skin,(16,48,32,64),(53,57,62),5)
        patch(skin,(40,16,56,32),robe,5);patch(skin,(32,48,48,64),robe,5)
        d=ImageDraw.Draw(skin)
        d.rectangle((8,8,15,9),fill=(63,46,35,255) if era==4 else (43,52,45,255))
        d.line((23,21,23,30),fill=(57,48,39,255))
        if era==1:
            for y in (23,26,29):d.point((24,y),fill=(166,141,78,255))
            d.line((20,22,22,24),fill=(83,74,59,255));d.line((27,22,25,24),fill=(83,74,59,255))
        if era==2:d.rectangle((20,26,22,28),fill=(104,99,77,255))
        if era==3:d.line((20,23,27,23),fill=(182,154,131,255))
        skin.save(folder/('lake_boy.png' if era==4 else f'lake_congregant_{era}.png'))
    icon=Image.new('RGBA',(32,32),(0,0,0,0));d=ImageDraw.Draw(icon)
    d.polygon([(3,24),(6,17),(11,14),(13,17),(12,24),(8,29)],fill=(151,113,85,255))
    d.polygon([(29,24),(26,17),(21,14),(19,17),(20,24),(24,29)],fill=(151,113,85,255))
    d.line((9,17,12,20),fill=(205,166,122,255),width=2);d.line((23,17,20,20),fill=(205,166,122,255),width=2)
    d.polygon([(12,12),(16,10),(20,12),(19,24),(13,24)],fill=(82,74,71,255))
    icon.save(ASSETS/'textures/item/shallows_burden.png')
    (ASSETS/'models/item/shallows_burden.json').write_text(json.dumps({'parent':'minecraft:item/generated','textures':{'layer0':'the_oldest_house:item/shallows_burden'}},indent=2)+'\n')


def audio():
    rate=24000;t=np.arange(6*rate)/rate
    rng=np.random.default_rng(412)
    # Each separate mono stem is another audible voice, with independent breath and pitch drift.
    frequencies=(130.8128,146.8324,196,233.0819,261.6256,293.6648,349.2282,392)
    voices=[]
    for i,f in enumerate(frequencies):
        phase=2*np.pi*f*t+.04*np.sin(2*np.pi*(4.2+i*.08)*t+i)
        voice=np.zeros_like(t)
        for h in range(1,9):voice+=np.sin(phase*h+i*.4)*np.exp(-((h*f-680)/600)**2)/h*.13
        voice+=np.convolve(rng.normal(0,.025,len(t)),np.ones(11)/11,mode='same')
        voice*=np.minimum(1,t/.7)*np.minimum(1,(6-t)/.65)*(.8+.1*np.sin(t*1.2+i))
        voices.append(voice);ogg(f'cave_voice_{i}',voice)
    ogg('cave_door',voices[0]*.6+voices[2]*.12)
    t=np.arange(4*rate)/rate
    water=np.convolve(rng.normal(0,.18,len(t)),np.ones(29)/29,mode='same')*(.4+.2*np.sin(t*2.4)**2)
    for start in (.7,1.4,2.5):
        dt=np.maximum(0,t-start);water+=np.where(t>=start,np.sin(dt*620)*np.exp(-dt*28)*.12,0)
    ogg('shallows_door',water*np.minimum(1,t/.3)*np.minimum(1,(4-t)/.5))
    path=ASSETS/'sounds.json';sounds=json.loads(path.read_text())
    for i in range(8):sounds[f'vignette.cave_voice_{i}']={'sounds':[{'name':f'the_oldest_house:indian_lake/cave_voice_{i}','attenuation_distance':36}],'subtitle':'subtitles.the_oldest_house.cave_hymn'}
    for key,file in [('leak.preserved_cave','cave_door'),('leak.shallows','shallows_door')]:
        sounds[key]={'sounds':[{'name':f'the_oldest_house:indian_lake/{file}','attenuation_distance':18}],'subtitle':f'subtitles.the_oldest_house.{file}'}
    path.write_text(json.dumps(sounds,indent=2)+'\n')
    path=ASSETS/'lang/en_us.json';lang=json.loads(path.read_text());lang.update({
        'item.the_oldest_house.shallows_burden':'Both hands',
        'entity.the_oldest_house.lake_canoe':'The canoe at the back',
        'subtitles.the_oldest_house.cave_hymn':'Another voice joins the hymn',
        'subtitles.the_oldest_house.cave_door':'A voice beneath the water',
        'subtitles.the_oldest_house.shallows_door':'Water against the reeds'})
    path.write_text(json.dumps(lang,indent=2)+'\n')


if __name__=='__main__':
    textures();audio();print('Generated five exact-UV creature variants, the temporary grip icon, and ten original mono sounds.')
