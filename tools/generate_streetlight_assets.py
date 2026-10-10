#!/usr/bin/env python3
"""Authored Minecraft pixel materials and fitted models for Proofrock's streetlights."""
from generate_playtest_fixes_assets import A, TURN, grain, rect, save, model, box, write
import json

def generate():
    metal=grain((16,16),(51,66,61))
    for x in (1,4,11,14):rect(metal,(x,0),(x+1,16),(68,83,74))
    for x,y in ((3,4),(12,11),(8,14),(5,9)):rect(metal,(x,y),(x+2,y+1),(112,76,42))
    save('streetlight_metal',metal)
    base=grain((16,16),(41,53,49));rect(base,(0,2),(16,3),(76,82,67));rect(base,(0,12),(16,14),(28,38,36))
    for x in (3,12):rect(base,(x,5),(x+1,11),(93,99,81))
    save('streetlight_base',base)
    glass=grain((16,16),(230,184,93));rect(glass,(0,0),(16,2),(70,73,51));rect(glass,(0,14),(16,16),(56,66,53))
    for x in (0,7,14):rect(glass,(x,2),(x+2,14),(63,76,62))
    rect(glass,(3,4),(6,7),(249,222,154));rect(glass,(10,6),(13,10),(242,208,126));save('streetlight_glass',glass)
    pole=box([6,0,6],[10,16,10],'#metal')
    pieces={
        'base':[pole,box([4,0,4],[12,5,12],'#base'),box([5,5,5],[11,7,11],'#metal')],
        'pole':[pole],
        'top':[pole,box([6,12,0],[10,16,8],'#metal'),box([5,10,5],[11,13,11],'#base')],
        'arm':[box([6,12,6],[10,16,16],'#metal'),box([7,0,7],[9,12,9],'#metal')],
        'head':[box([4,2,4],[12,11,12],'#glass'),box([3,11,3],[13,13,13],'#metal'),box([5,13,5],[11,14,11],'#metal'),box([7,14,7],[9,16,9],'#metal'),box([3,1,3],[13,3,13],'#base')],
    }
    for kind,elements in pieces.items():model('streetlight_'+kind,{'metal':'streetlight_metal','base':'streetlight_base','glass':'streetlight_glass','particle':'streetlight_metal'},elements)
    variants={}
    for facing,rotation in TURN.items():
        for kind in pieces:
            for wet in ('false','true'):
                value={'model':'the_oldest_house:block/streetlight_'+kind}
                if rotation:value['y']=rotation
                variants[f'facing={facing},kind={kind},waterlogged={wet}']=value
    write('blockstates/streetlight.json',{'variants':variants})
    language=json.loads((A/'lang/en_us.json').read_text());language['block.the_oldest_house.streetlight']='Streetlight';write('lang/en_us.json',language)

if __name__=='__main__':
    generate();print('Five fitted streetlight models, forty native states and three pixel materials generated')
