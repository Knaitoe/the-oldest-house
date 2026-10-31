#!/usr/bin/env python3
"""The bead Mapping the Interior leaves on the hall carpet (0.4.201; docs/MAPPING_INTERIOR_REWORK.md).

One glass seed bead, the size of a fingernail in the hand, drawn at vanilla's sixteen pixels: a round bead with its hole
showing, a highlight and a shadow, on a scrap of thread. No pattern or beadwork is drawn, only the one bead.
"""
import argparse, io, json
from pathlib import Path
from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
A = ROOT / 'src/main/resources/assets/the_oldest_house'
GLASS, GLASS_DARK, GLASS_LIGHT, HOLE = (46, 104, 140), (26, 62, 88), (156, 206, 226), (14, 22, 30)
THREAD, THREAD_DARK = (196, 184, 160), (150, 138, 116)


def sheet():
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    # The thread through it, frayed at both ends.
    for x, y in ((2, 9), (3, 9), (4, 8), (12, 7), (13, 6), (14, 6)):
        im.putpixel((x, y), THREAD + (255,))
    for x, y in ((1, 10), (15, 5)):
        im.putpixel((x, y), THREAD_DARK + (255,))
    # The bead: a disc, darker toward its lower right, the hole through its middle.
    for y in range(16):
        for x in range(16):
            d = ((x - 8) ** 2 + (y - 8) ** 2) ** .5
            if d <= 4.2:
                shade = GLASS_DARK if (x - 8) + (y - 8) > 2 else GLASS
                im.putpixel((x, y), shade + (255,))
    for x, y in ((7, 8), (8, 8), (8, 7), (7, 7)):
        im.putpixel((x, y), HOLE + (255,))
    for x, y in ((6, 5), (5, 6), (6, 6)):
        im.putpixel((x, y), GLASS_LIGHT + (255,))
    return im


def files():
    b = io.BytesIO(); sheet().save(b, format='PNG')
    model = {'parent': 'minecraft:item/generated', 'textures': {'layer0': 'the_oldest_house:item/mapping_bead'}}
    return {A / 'textures/item/mapping_bead.png': b.getvalue(), A / 'models/item/mapping_bead.json': json.dumps(model, indent=2) + '\n'}


def main():
    p = argparse.ArgumentParser(); p.add_argument('--check', action='store_true'); check = p.parse_args().check
    for path, data in files().items():
        if check:
            assert path.exists(), path
            if isinstance(data, str):
                assert json.loads(path.read_text()) == json.loads(data), path
            else:
                assert Image.open(path).convert('RGBA').tobytes() == Image.open(io.BytesIO(data)).convert('RGBA').tobytes(), path
        else:
            path.parent.mkdir(parents=True, exist_ok=True)
            path.write_text(data) if isinstance(data, str) else path.write_bytes(data)
    print(('Verified' if check else 'Drew') + ' the Mapping bead: one glass bead on a scrap of thread.')


if __name__ == '__main__':
    main()
