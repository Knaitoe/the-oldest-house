#!/usr/bin/env python3
"""Validate the exact imported glyph grid and fallback definitions, including the JAR."""
import io
import json
import struct
import zlib
import zipfile
from pathlib import Path
from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
PREFIX = 'assets/the_oldest_house/'

def verify(read):
    for name in ('will','karen','zampano','child','pelafina','claw','hotel','johnny'):
        definition = json.loads(read(PREFIX+f'font/{name}.json'))
        providers = definition['providers']
        assert providers[0] == {'type':'space','advances':{' ':4.0}}, name
        assert providers[-1] == {'type':'reference','id':'minecraft:default'}, name
        provider = providers[1]
        chars = provider['chars']
        assert all(len(row)==16 for row in chars), (name,'unequal rows')
        mapped = [c for row in chars for c in row if c != '\0']
        assert len(mapped)==len(set(mapped)), (name,'duplicate/misassigned glyph')
        assert set(chr(cp) for cp in range(33,127)) <= set(mapped), (name,'missing ASCII')
        assert set('\u2018\u2019\u201c\u201d\u2013\u2014\u2026\u00b7') <= set(mapped), (name,'missing story punctuation')
        raw = read(PREFIX+'textures/'+provider['file'].split(':')[1])
        assert raw[:8] == b'\x89PNG\r\n\x1a\n', name
        offset=8
        while offset<len(raw):
            count=struct.unpack('>I',raw[offset:offset+4])[0]
            chunk=raw[offset+4:offset+8+count]
            crc=struct.unpack('>I',raw[offset+8+count:offset+12+count])[0]
            assert zlib.crc32(chunk)&0xffffffff == crc, (name,'invalid PNG CRC')
            offset+=12+count
        image=Image.open(io.BytesIO(raw)).convert('RGBA');image.load()
        w,h=image.width//16,image.height//len(chars)
        assert image.width%16==0 and image.height%len(chars)==0, name
        assert set(image.getchannel('A').get_flattened_data()) <= {0,255}, (name,'weak translucent strokes')
        for y,row in enumerate(chars):
            for x,c in enumerate(row):
                if c=='\0':continue
                tile=image.crop((x*w,y*h,(x+1)*w,(y+1)*h));bbox=tile.getbbox()
                assert bbox and bbox[2]<w and bbox[3]<h, (name,repr(c),'empty or bleeding glyph')
                if name != 'hotel': assert bbox[3]*provider['height']/h<=8, (name,'overlaps next book line')
        if name=='child': assert set(map(chr,range(0xE100,0xE120))) <= set(mapped), 'lost old child alternates'
        assert provider['height']==(16 if name=='hotel' else 9), name
    print('PASS: eight font grids, 94 ASCII glyphs each, story punctuation, old child variants, spacing, opaque ink and fallbacks')

if __name__=='__main__':
    verify(lambda p:(ROOT/'src/main/resources'/p).read_bytes())
    jars=[p for p in (ROOT/'build/libs').glob('the_oldest_house-*.jar') if not p.stem.endswith(('-sources','-javadoc'))]
    if jars:
        assert len(jars)==1,jars
        with zipfile.ZipFile(jars[0]) as jar: verify(jar.read)
