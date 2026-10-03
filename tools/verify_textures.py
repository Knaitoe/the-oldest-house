"""Decode every shipped PNG and validate every chunk, including bytes after IDAT.

Minecraft's native decoder rejects malformed trailing chunks that permissive image
viewers accept. The 0.4.29 rug and wardrobe were two such files.
"""
from pathlib import Path
import struct
import zlib
from PIL import Image

root = Path(__file__).resolve().parents[1] / 'src/main/resources/assets/the_oldest_house/textures'
count = 0
for path in sorted(root.rglob('*.png')):
    raw = path.read_bytes()
    assert raw[:8] == b'\x89PNG\r\n\x1a\n', path
    offset = 8
    ended = False
    while offset < len(raw):
        assert offset + 12 <= len(raw), f'{path}: truncated chunk'
        size = struct.unpack('>I', raw[offset:offset + 4])[0]
        end = offset + 12 + size
        assert end <= len(raw), f'{path}: truncated payload'
        kind = raw[offset + 4:offset + 8]
        payload = raw[offset + 4:offset + 8 + size]
        crc = struct.unpack('>I', raw[offset + 8 + size:end])[0]
        assert zlib.crc32(payload) & 0xffffffff == crc, f'{path}: bad {kind!r} CRC'
        offset = end
        if kind == b'IEND':
            assert size == 0 and offset == len(raw), f'{path}: bytes after IEND'
            ended = True
            break
    assert ended, f'{path}: missing IEND'
    with Image.open(path) as image:
        image.load()
        assert image.width > 0 and image.height > 0, path
    count += 1
print(f'Decoded {count} native PNG textures; all chunk CRCs valid.')
