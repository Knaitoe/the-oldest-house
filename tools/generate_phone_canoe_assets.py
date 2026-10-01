#!/usr/bin/env python3
"""A native pixel phone and three original mono lake recordings; no licensed footage or audio."""
import json
import numpy as np
from PIL import Image, ImageDraw
from generate_indian_lake_assets import ASSETS, ogg

def main():
    icon=Image.new('RGBA',(32,32),(0,0,0,0));d=ImageDraw.Draw(icon)
    d.rounded_rectangle((8,2,24,30),radius=3,fill=(27,31,33,255),outline=(112,117,109,255))
    d.rectangle((10,6,22,24),fill=(14,24,34,255));d.line((11,17,21,17),fill=(57,75,82,255))
    d.polygon([(15,17),(16,12),(18,17)],fill=(94,97,86,255));d.point((13,9),fill=(195,198,181,255))
    d.ellipse((19,7,21,9),fill=(164,54,47,255));d.line((14,4,18,4),fill=(115,118,109,255))
    d.rectangle((15,27,17,28),fill=(82,85,79,255));icon.save(ASSETS/'textures/item/lake_phone.png')
    (ASSETS/'models/item/lake_phone.json').write_text(json.dumps({'parent':'minecraft:item/generated','textures':{'layer0':'the_oldest_house:item/lake_phone'}},indent=2)+'\n')
    rate=24000;rng=np.random.default_rng(415)
    t=np.arange(3*rate)/rate
    waves=np.convolve(rng.normal(0,.25,len(t)),np.ones(37)/37,mode='same')*(.6+.35*np.sin(t*3.2)**2)
    for start in (.3,1.2,2.1):
        dt=np.maximum(0,t-start);waves+=np.where(t>=start,np.sin(dt*460)*np.exp(-dt*22)*.14,0)
    waves*=np.minimum(1,t/.12)*np.minimum(1,(3-t)/.4)
    ogg('phone_door',waves*.8)
    # Wood tipping, followed by a close splash and water against a small floating lens.
    knock=np.sin(2*np.pi*93*t)*np.exp(-t*18)*.24
    splash=np.convolve(rng.normal(0,.24,len(t)),np.ones(5)/5,mode='same')*np.exp(-np.maximum(0,t-.17)*7)*(t>=.17)
    ogg('phone_water',waves*.45+knock+splash)
    t=np.arange(rate//2)/rate;beep=np.sin(2*np.pi*920*t)*np.exp(-t*18)*.12
    ogg('phone_record',beep)
    path=ASSETS/'sounds.json';sounds=json.loads(path.read_text())
    for key,file,subtitle in [('leak.phone_canoe','phone_door','canoe_door'),('vignette.phone_water','phone_water','phone_water'),('vignette.phone_record','phone_record','phone_record')]:
        sounds[key]={'sounds':[{'name':f'the_oldest_house:indian_lake/{file}','attenuation_distance':18}],'subtitle':f'subtitles.the_oldest_house.{subtitle}'}
    path.write_text(json.dumps(sounds,indent=2)+'\n')
    path=ASSETS/'lang/en_us.json';lang=json.loads(path.read_text());lang.update({'item.the_oldest_house.lake_phone':'Lake phone','subtitles.the_oldest_house.canoe_door':'Water against a wooden hull','subtitles.the_oldest_house.phone_water':'Something falls into the water','subtitles.the_oldest_house.phone_record':'Recording begins'})
    path.write_text(json.dumps(lang,indent=2)+'\n')
    print('Generated the native phone icon and three original mono recordings.')

if __name__=='__main__':main()
