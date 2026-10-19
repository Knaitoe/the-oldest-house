"""Pixel-painted funeral materials and a fitted, freestanding Peabody & Sons plaque."""
from pathlib import Path
import argparse
import json
from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / 'src/main/resources/assets/the_oldest_house'
GLYPHS = {
 'A':'01110/10001/10001/11111/10001/10001/10001', 'B':'11110/10001/10001/11110/10001/10001/11110',
 'D':'11110/10001/10001/10001/10001/10001/11110', 'E':'11111/10000/10000/11110/10000/10000/11111',
 'F':'11111/10000/10000/11110/10000/10000/10000', 'G':'01110/10001/10000/10111/10001/10001/01110',
 'H':'10001/10001/10001/11111/10001/10001/10001', 'I':'111/010/010/010/010/010/111',
 'L':'10000/10000/10000/10000/10000/10000/11111', 'M':'10001/11011/10101/10101/10001/10001/10001',
 'N':'10001/11001/10101/10011/10001/10001/10001', 'O':'01110/10001/10001/10001/10001/10001/01110',
 'P':'11110/10001/10001/11110/10000/10000/10000', 'R':'11110/10001/10001/11110/10100/10010/10001',
 'S':'01111/10000/10000/01110/00001/00001/11110', 'T':'11111/00100/00100/00100/00100/00100/00100',
 'U':'10001/10001/10001/10001/10001/10001/01110', 'V':'10001/10001/10001/10001/10001/01010/00100',
 'Y':'10001/10001/01010/00100/00100/00100/00100', '&':'01100/10010/10100/01000/10101/10010/01101',
 '.':'0/0/0/0/0/1/1', ' ':'000/000/000/000/000/000/000',
}

def lettering(d, text, y, color):
    width=sum(len(GLYPHS[c].split('/')[0])+1 for c in text)-1
    x=(96-width)//2
    for c in text:
        rows=GLYPHS[c].split('/')
        for row,bits in enumerate(rows):
            for col,bit in enumerate(bits):
                if bit=='1': d.point((x+col,y+row),fill=color)
        x+=len(rows[0])+1

def paintings():
    wood=Image.new('RGBA',(32,32),'#3b2624');d=ImageDraw.Draw(wood)
    for y in (2,8,16,25):
        d.line((0,y,31,y),fill='#482d28');d.line((3,y+1,18,y+1,26,y+2,31,y+2),fill='#53332b')
    d.line((0,0,31,0),fill='#694438');d.line((0,31,31,31),fill='#251c1b')
    brass=Image.new('RGBA',(16,16),'#9d7c43');d=ImageDraw.Draw(brass)
    d.rectangle((0,0,15,15),outline='#5a482e');d.line((1,1,14,1),fill='#dfc48a');d.line((1,2,1,13),fill='#c7a86c')
    for y in (5,11):d.line((3,y,12,y),fill='#ac8b51')
    lining=Image.new('RGBA',(32,32),'#d8d0b9');d=ImageDraw.Draw(lining)
    for x in range(-32,64,16):
        d.line((x,0,x+32,32),fill='#bdb69f');d.line((x+1,0,x+33,32),fill='#e7e0cf')
        d.line((x,32,x+32,0),fill='#c5bca6')
    for x in (8,24):
        for y in (8,24):d.rectangle((x-1,y-1,x,y),fill='#a99e88');d.point((x-1,y-1),fill='#eee8d8')
    front=Image.new('RGBA',(96,128),'#20282a');d=ImageDraw.Draw(front)
    d.rectangle((2,2,93,125),outline='#c3a266',width=2);d.rectangle((6,6,89,121),outline='#68583e')
    lettering(d,'PEABODY & SONS',16,'#e5d1a4');lettering(d,'FUNERAL HOME',31,'#d3bc8c')
    for y in (46,67):d.line((19,y,76,y),fill='#9f854f');d.rectangle((46,y-1,49,y+1),fill='#ccb47a')
    # A restrained laurel, in actual square pixels.
    for side in (-1,1):
        d.line((48+side*4,59,48+side*11,52),fill='#87927b')
        for i in range(3):d.rectangle((48+side*(5+i*2)-1,56-i*2,48+side*(5+i*2)+1,57-i*2),fill='#b4b59a')
    lettering(d,'VISITATION FOR',79,'#c7c2b1');lettering(d,'MR. HARRIGAN',96,'#eee4c9')
    back=Image.new('RGBA',(32,32),'#382b28');d=ImageDraw.Draw(back)
    for x in (3,11,23,29):d.line((x,0,x,31),fill='#49332b')
    d.rectangle((3,3,28,28),outline='#251f1e');d.line((5,5,26,5),fill='#66503d')
    return {'harrigan_casket_wood':wood,'harrigan_casket_brass':brass,'harrigan_casket_lining':lining,
            'harrigan_plaque_front':front,'harrigan_plaque_back':back}

def plaque():
    def box(a,b,texture):
        return {'from':a,'to':b,'faces':{side:{'texture':'#'+texture,'uv':[0,0,16,16]} for side in ('north','south','east','west','up','down')}}
    parts=[box([0,0,4],[16,2,12],'wood'),box([3,2,7],[5,11,9],'wood'),box([11,2,7],[13,11,9],'wood'),
           box([2,8,7],[14,10,9],'wood'),box([1,10,6.5],[15,28,9],'brass')]
    board=box([2,11,6.4],[14,27,9.1],'wood')
    board['faces']['south']['texture']='#front';board['faces']['north']['texture']='#back';parts.append(board)
    return {'ambientocclusion':True,'textures':{'wood':'the_oldest_house:block/harrigan_casket_wood',
        'brass':'the_oldest_house:block/harrigan_casket_brass','front':'the_oldest_house:block/harrigan_plaque_front',
        'back':'the_oldest_house:block/harrigan_plaque_back','particle':'the_oldest_house:block/harrigan_casket_wood'},'elements':parts}

def main():
    parser=argparse.ArgumentParser();parser.add_argument('--check',action='store_true');check=parser.parse_args().check
    for name,im in paintings().items():
        path=ASSETS/'textures/block'/f'{name}.png'
        if check:
            with Image.open(path) as actual:assert actual.convert('RGBA').tobytes()==im.tobytes(),name
        else:im.save(path,optimize=True)
    path=ASSETS/'models/block/harrigan_plaque.json'
    if check:assert json.loads(path.read_text())==plaque()
    else:path.write_text(json.dumps(plaque(),indent=2)+'\n')
    print(('Verified' if check else 'Painted')+' five funeral materials and the fitted plaque model.')

if __name__=='__main__':main()
