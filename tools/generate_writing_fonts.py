#!/usr/bin/env python3
"""Author opaque, connected bitmap lettering on Minecraft's nine-pixel book baseline.

No external typeface or image generation: the small glyphs below are the source.
Handwriting uses half-pixel slants rather than holes or displaced scanlines.
Run from any directory. --preview produces specimens from the packaged atlases.
"""
import argparse
import json
from pathlib import Path
from PIL import Image, ImageDraw, ImageFont

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / 'src/main/resources/assets/the_oldest_house'
# Seven rows; lowercase bodies sit on the same baseline as the capitals.
CAPS = {
 'A':'01110/10001/10001/11111/10001/10001/10001', 'B':'11110/10001/10001/11110/10001/10001/11110',
 'C':'01111/10000/10000/10000/10000/10000/01111', 'D':'11110/10001/10001/10001/10001/10001/11110',
 'E':'11111/10000/10000/11110/10000/10000/11111', 'F':'11111/10000/10000/11110/10000/10000/10000',
 'G':'01111/10000/10000/10111/10001/10001/01110', 'H':'10001/10001/10001/11111/10001/10001/10001',
 'I':'111/010/010/010/010/010/111', 'J':'00111/00010/00010/00010/00010/10010/01100',
 'K':'10001/10010/10100/11000/10100/10010/10001', 'L':'10000/10000/10000/10000/10000/10000/11111',
 'M':'10001/11011/10101/10101/10001/10001/10001', 'N':'10001/11001/10101/10011/10001/10001/10001',
 'O':'01110/10001/10001/10001/10001/10001/01110', 'P':'11110/10001/10001/11110/10000/10000/10000',
 'Q':'01110/10001/10001/10001/10101/10010/01101', 'R':'11110/10001/10001/11110/10100/10010/10001',
 'S':'01111/10000/10000/01110/00001/00001/11110', 'T':'11111/00100/00100/00100/00100/00100/00100',
 'U':'10001/10001/10001/10001/10001/10001/01110', 'V':'10001/10001/10001/10001/10001/01010/00100',
 'W':'10001/10001/10001/10101/10101/10101/01010', 'X':'10001/10001/01010/00100/01010/10001/10001',
 'Y':'10001/10001/01010/00100/00100/00100/00100', 'Z':'11111/00001/00010/00100/01000/10000/11111',
}
LOWER = {
 'a':'0000/0000/0110/0001/0111/1001/0111', 'b':'1000/1000/1110/1001/1001/1001/1110',
 'c':'0000/0000/0111/1000/1000/1000/0111', 'd':'0001/0001/0111/1001/1001/1001/0111',
 'e':'0000/0000/0110/1001/1111/1000/0111', 'f':'011/100/100/110/100/100/100',
 'g':'0000/0000/0111/1001/1001/0111/0001/0110', 'h':'1000/1000/1110/1001/1001/1001/1001',
 'i':'1/0/1/1/1/1/1', 'j':'01/00/01/01/01/01/01/10',
 'k':'1000/1000/1001/1010/1100/1010/1001', 'l':'10/10/10/10/10/10/01',
 'm':'00000/00000/11010/10101/10101/10101/10101', 'n':'0000/0000/1110/1001/1001/1001/1001',
 'o':'0000/0000/0110/1001/1001/1001/0110', 'p':'0000/0000/1110/1001/1001/1110/1000/1000',
 'q':'0000/0000/0111/1001/1001/0111/0001/0001', 'r':'000/000/101/110/100/100/100',
 's':'0000/0000/0111/1000/0110/0001/1110', 't':'010/010/111/010/010/010/001',
 'u':'0000/0000/1001/1001/1001/1001/0111', 'v':'00000/00000/10001/10001/10001/01010/00100',
 'w':'00000/00000/10001/10001/10101/10101/01010', 'x':'00000/00000/10001/01010/00100/01010/10001',
 'y':'0000/0000/1001/1001/1001/0111/0001/0110', 'z':'0000/0000/1111/0001/0010/0100/1111',
}
DIGITS = dict(zip('0123456789', [
 '01110/10001/10011/10101/11001/10001/01110', '010/110/010/010/010/010/111',
 '01110/10001/00001/00010/00100/01000/11111', '11110/00001/00001/01110/00001/00001/11110',
 '00010/00110/01010/10010/11111/00010/00010', '11111/10000/10000/11110/00001/00001/11110',
 '01110/10000/10000/11110/10001/10001/01110', '11111/00001/00010/00100/01000/01000/01000',
 '01110/10001/10001/01110/10001/10001/01110', '01110/10001/10001/01111/00001/00001/01110']))
PUNCT = {
 '!':'1/1/1/1/1/0/1', '"':'101/101/101', '#':'01010/01010/11111/01010/11111/01010/01010',
 '$':'00100/01111/10100/01110/00101/11110/00100', '%':'11001/11010/00010/00100/01000/01011/10011',
 '&':'01100/10010/10100/01000/10101/10010/01101', "'":'1/1/1',
 '(':'01/10/10/10/10/10/01', ')':'10/01/01/01/01/01/10',
 '*':'00000/10101/01110/11111/01110/10101', '+':'00000/00100/00100/11111/00100/00100',
 ',':'0/0/0/0/0/0/1/1', '-':'0000/0000/0000/1111', '.':'0/0/0/0/0/0/1',
 '/':'00001/00001/00010/00100/01000/10000/10000', ':':'0/0/1/0/0/1', ';':'0/0/1/0/0/1/1',
 '<':'0001/0010/0100/1000/0100/0010/0001', '=':'0000/0000/1111/0000/1111',
 '>':'1000/0100/0010/0001/0010/0100/1000', '?':'01110/10001/00001/00010/00100/00000/00100',
 '@':'01110/10001/10111/10101/10111/10000/01110', '[':'11/10/10/10/10/10/11',
 '\\':'10000/10000/01000/00100/00010/00001/00001', ']':'11/01/01/01/01/01/11',
 '^':'00100/01010/10001', '_':'00000/00000/00000/00000/00000/00000/11111', '`':'10/01',
 '{':'011/010/010/100/010/010/011', '|':'1/1/1/1/1/1/1', '}':'110/010/010/001/010/010/110',
 '~':'00000/00000/01001/10110',
}
GLYPHS = CAPS | LOWER | DIGITS | PUNCT
CHILD_LETTERS = 'aeiotrshnldcmyug'
ALIASES = {'\u2018':"'", '\u2019':"'", '\u201c':'"', '\u201d':'"', '\u2013':'-', '\u2014':'-',
           '\u2026':'.', '\u00b7':'.'}

def rows(chars):
    chars += '\0' * (-len(chars) % 16)
    return [chars[i:i+16] for i in range(0,len(chars),16)]

def make(name):
    chars = ''.join(chr(cp) for cp in range(33,127)) + ''.join(ALIASES)
    if name == 'child': chars += ''.join(chr(cp) for cp in range(0xE100,0xE120))
    mapping = rows(chars)
    cell_w, cell_h = 16, 18
    atlas = Image.new('RGBA',(16*cell_w,len(mapping)*cell_h))
    draw = ImageDraw.Draw(atlas)
    for i, char in enumerate(''.join(mapping)):
        alternate = ord(char) - 0xE100 if '\uE100' <= char < '\uE120' else -1
        source = CHILD_LETTERS[alternate%16] if alternate >= 0 else ALIASES.get(char,char)
        if source not in GLYPHS: continue
        pattern = GLYPHS[source]
        if name in ('will','child') and source == 'a': pattern='0000/0000/0110/1001/1001/1001/0111'
        if name in ('karen','pelafina') and source == 'l': pattern='10/10/10/10/10/10/11'
        x0,y0=i%16*cell_w,i//16*cell_h
        for y,row in enumerate(pattern.split('/')):
            # Every stroke stays connected. A half-pixel slant keeps hand identities.
            slant = (1 if y < 4 else 0) if name == 'will' else 0
            if name == 'child': slant = (1 if y < 3 else 0) if (ord(source)+max(0,alternate))%3 else 0
            if name in ('karen','pelafina'): slant = 1 if y < 2 else 0
            if name == 'claw': slant = 1 if y in (0,3,6) else 0
            for x,bit in enumerate(row):
                if bit == '1':
                    draw.rectangle((x0+x*2+slant,y0+y*2,x0+x*2+slant+1,y0+y*2+1),fill=(255,255,255,255))
    (ASSETS/'textures/font').mkdir(parents=True,exist_ok=True)
    atlas.save(ASSETS/f'textures/font/{name}.png')
    height, ascent = (16,12) if name == 'hotel' else (9,7)
    definition = {'providers':[
        {'type':'space','advances':{' ':4.0}},
        {'type':'bitmap','file':f'the_oldest_house:font/{name}.png','ascent':ascent,'height':height,'chars':mapping},
        {'type':'reference','id':'minecraft:default'}]}
    (ASSETS/f'font/{name}.json').write_text(json.dumps(definition,indent=2,ensure_ascii=True)+'\n')

def preview(path):
    """Draw with the real bitmap provider's width, height and ascent, at GUI scale 4."""
    scale=4
    sheet=Image.new('RGB',(1000,1170),(34,31,29));draw=ImageDraw.Draw(sheet)
    label_font=ImageFont.truetype('/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf',19)
    sample='The House is longer inside.\nI measured it twice. 0123456789\n\u201cDo not come for me.\u201d\nThe house\u2019s door. [No. Check again.]'
    for panel,name in enumerate(('will','karen','zampano','child','pelafina','claw')):
        ox,oy=(panel%2)*500,(panel//2)*390
        draw.text((ox+20,oy+12),name.capitalize(),font=label_font,fill=(240,233,220))
        draw.rectangle((ox+18,oy+44,ox+482,oy+368),fill=(231,217,188))
        provider=next(p for p in json.loads((ASSETS/f'font/{name}.json').read_text())['providers'] if p['type']=='bitmap')
        atlas=Image.open(ASSETS/'textures'/provider['file'].split(':')[1]).convert('RGBA')
        cell_w=atlas.width//16;cell_h=atlas.height//len(provider['chars']);factor=provider['height']/cell_h
        lookup={c:(i,j) for j,row in enumerate(provider['chars']) for i,c in enumerate(row)}
        x,y=ox+28,oy+59
        text=sample.upper() if name=='claw' else sample
        for pos,char in enumerate(text):
            if char=='\n':x=ox+28;y+=9*scale;continue
            if char==' ':x+=4*scale;continue
            if char not in lookup:continue
            i,j=lookup[char];tile=atlas.crop((i*cell_w,j*cell_h,(i+1)*cell_w,(j+1)*cell_h));bbox=tile.getbbox()
            if not bbox:continue
            if x+(bbox[2]*factor+1)*scale>ox+470:x=ox+28;y+=9*scale
            start=text.lower().rfind('house',max(0,pos-4),pos+5)
            color=(36,71,178) if start>=0 and start<=pos<start+5 else (26,22,18)
            ink=Image.new('RGBA',tile.size,color);ink.putalpha(tile.getchannel('A'))
            ink=ink.resize((round(cell_w*factor*scale),round(cell_h*factor*scale)),Image.Resampling.NEAREST)
            sheet.paste(ink,(round(x),y),ink);x+=(int(bbox[2]*factor+0.5)+1)*scale
    Path(path).parent.mkdir(parents=True,exist_ok=True);sheet.save(path)

if __name__ == '__main__':
    parser=argparse.ArgumentParser();parser.add_argument('--preview');args=parser.parse_args()
    for name in ('will','karen','zampano','child','pelafina','claw','hotel'):make(name)
    if args.preview:preview(args.preview)
    print('Seven opaque atlases; nine-pixel prose, explicit spaces, fixed punctuation, native fallback.')
