"""Backseat's native 16-pixel materials, using the established uncanny palette.

No source images or resampling. Large model faces repeat these tiles at one
texture pixel per model pixel; the renderer supplies Minecraft's lighting.
"""
import argparse
from pathlib import Path
from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / 'src/main/resources/assets/the_oldest_house/textures/block/backseat'


def paintings():
    out = {}
    im = Image.new('RGBA', (16, 16), '#c2c2ae')
    d = ImageDraw.Draw(im)
    # Enamel belongs beside the diner's enamel, with small chips rather than a
    # photographic rust/noise layer. Broad unmarked areas remain broad and flat.
    d.line((0, 15, 15, 15), fill='#a7ad9e')
    d.line((0, 0, 15, 0), fill='#d7d6be')
    d.line((3, 11, 5, 11), fill='#a7ad9e')
    d.point((4, 12), fill='#827d68')
    d.line((12, 4, 13, 4), fill='#d7d6be')
    out['cream'] = im

    im = Image.new('RGBA', (16, 16), '#6c503e')
    d = ImageDraw.Draw(im)
    d.line((0, 0, 15, 0), fill='#493e34')
    d.line((0, 1, 15, 1), fill='#87664b')
    d.line((0, 14, 15, 14), fill='#574436')
    for x in (3, 11):
        d.line((x, 4, x, 11), fill='#574436')
        d.line((x + 1, 4, x + 1, 10), fill='#785b44')
    d.line((6, 8, 8, 8), fill='#87664b')
    out['vinyl'] = im

    im = Image.new('RGBA', (16, 16), '#292f30')
    d = ImageDraw.Draw(im)
    for x in (2, 6, 10, 14):
        d.line((x, 0, x, 15), fill='#373d3c')
    d.line((0, 7, 15, 7), fill='#202627')
    out['rubber'] = im

    im = Image.new('RGBA', (16, 16), '#343c3b')
    d = ImageDraw.Draw(im)
    d.line((0, 0, 15, 0), fill='#4b5553')
    d.line((0, 15, 15, 15), fill='#292f30')
    # Two simple dial faces; no high-resolution markings or painted 3D knobs.
    for x in (1, 9):
        d.rectangle((x, 5, x + 5, 11), fill='#202627')
        d.line((x + 1, 6, x + 4, 6), fill='#b7ac7b')
        d.line((x + 3, 8, x + 3, 10), fill='#d8c398')
    out['gauges'] = im

    im = Image.new('RGBA', (16, 16), '#8c9691')
    d = ImageDraw.Draw(im)
    d.line((0, 1, 15, 1), fill='#b9c0b5')
    d.line((0, 14, 15, 14), fill='#667573')
    d.line((4, 9, 7, 9), fill='#9ba7a0')
    out['chrome'] = im

    im = Image.new('RGBA', (16, 16), '#4c5757')
    d = ImageDraw.Draw(im)
    d.line((2, 10, 10, 2), fill='#71817e', width=2)
    d.line((9, 13, 13, 9), fill='#8c9691')
    out['mirror'] = im

    im = Image.new('RGBA', (16, 16), '#b9b6a2')
    d = ImageDraw.Draw(im)
    for x, y in ((2, 5), (3, 5), (10, 11), (11, 12), (13, 3)):
        d.point((x, y), fill='#aaa993')
    d.line((0, 0, 15, 0), fill='#a0a18e')
    out['headliner'] = im
    return out


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--check', action='store_true')
    args = parser.parse_args()
    for name, image in paintings().items():
        assert image.size == (16, 16)
        assert len(image.getcolors(256)) <= 6
        target = OUT / (name + '.png')
        if args.check:
            if not target.is_file() or Image.open(target).convert('RGBA').tobytes() != image.tobytes():
                raise SystemExit(f'Stale native Backseat material: {target}')
        else:
            target.parent.mkdir(parents=True, exist_ok=True)
            image.save(target)
    print('Seven native 16x16 Backseat materials match the authored pixel source; no resampling.')


if __name__ == '__main__':
    main()
