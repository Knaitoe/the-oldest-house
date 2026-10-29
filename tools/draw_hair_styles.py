#!/usr/bin/env python3
"""0.4.167: the bathroom glass's hair, at Minecraft's own pixel scale.

The owner: "develop more tools for textured hair - the player should be able to see a preview when picking up the tool ...
reflected in the mirror", and "More hair styles would still be appropriate". Each routine tool has a style the glass shows
on the reader while it is in their hand. The hair patterns are drawn in greys and tinted at draw time to the reader's own
hair colour (read from their skin's crown); the fabrics (wrap, bonnet, ties, clip, rollers) keep their own colours.
Every sheet is 64 x 32, the box-unwrap size of the models in client/HouseReadHair.java, and each pattern tiles, so any face
of any cuboid reads as that kind of hair.

Run with --check in CI: the shipped PNGs must match exactly.
"""
import argparse, io, random
from pathlib import Path
from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / 'src/main/resources/assets/the_oldest_house/textures/entity/house_read/hair'
W, H = 64, 32
HAIR = ['curl', 'coil', 'braid', 'rows', 'straight', 'volume', 'short', 'slick', 'crop', 'wave']
FABRIC = ['silk', 'satin', 'tie', 'clip', 'rollers']


def grey(v, a=255):
    v = max(0, min(255, int(v)))
    return (v, v, v, a)


def hair(name):
    im = Image.new('RGBA', (W, H), grey(196))
    d = ImageDraw.Draw(im)
    rng = random.Random(sum(map(ord, name)) * 167)
    if name == 'curl':
        # Tight springs: a dark crescent and a light catch on each, in staggered rows.
        for y in range(0, H, 3):
            for x in range((y // 3) % 2 * 2, W, 4):
                d.arc((x, y, x + 3, y + 3), 30, 250, fill=grey(120))
                d.point((x + 2, y + 1), fill=grey(236))
    elif name == 'coil':
        # A soft cloud of coils: clustered speckle, darker deep in, lighter at the surface.
        for _ in range(900):
            x, y = rng.randrange(W), rng.randrange(H)
            d.point((x, y), fill=grey(rng.choice([110, 132, 150, 214, 228])))
    elif name == 'braid':
        # Plaits running down: each a column of chevrons with a dark parting either side.
        for x0 in range(0, W, 4):
            d.line((x0, 0, x0, H), fill=grey(96))
            for y in range(0, H, 3):
                d.line((x0 + 1, y, x0 + 2, y + 1), fill=grey(150))
                d.line((x0 + 3, y, x0 + 2, y + 1), fill=grey(236))
    elif name == 'rows':
        # Cornrows: narrow raised rows with the scalp showing between.
        for x0 in range(0, W, 3):
            d.line((x0, 0, x0, H), fill=grey(108))
            for y in range(0, H, 2):
                d.point((x0 + 1, y), fill=grey(230))
                d.point((x0 + 2, y + 1), fill=grey(160))
    elif name in ('straight', 'volume'):
        # Long strands: vertical lines of a few greys, a little looser for a blow-out.
        for x in range(W):
            base = rng.choice([168, 184, 204, 222])
            for y in range(H):
                wobble = rng.random() < (0.12 if name == 'volume' else 0.05)
                d.point((x, y), fill=grey(base + (18 if wobble else 0)))
        for y in range(0, H, 9 if name == 'straight' else 6):
            d.line((0, y, W, y), fill=grey(150))
    elif name == 'short':
        # A short cut: fine directional texture, a combed parting line every so often.
        for _ in range(700):
            x, y = rng.randrange(W), rng.randrange(H)
            d.line((x, y, x + 1, y), fill=grey(rng.choice([150, 176, 214])))
        for x in range(0, W, 16):
            d.line((x + 5, 0, x + 5, H), fill=grey(120))
    elif name == 'slick':
        # Pomade or edge gel: smooth and dark with long highlights.
        im.paste(grey(150), (0, 0, W, H))
        for y in range(1, H, 4):
            d.line((rng.randrange(4), y, W - rng.randrange(6), y), fill=grey(238))
        for y in range(3, H, 4):
            d.line((0, y, W, y), fill=grey(128))
    elif name == 'crop':
        # A close crop: scalp grey with even stubble.
        im.paste(grey(214), (0, 0, W, H))
        for y in range(H):
            for x in range((y % 2), W, 2):
                d.point((x, y), fill=grey(132))
    elif name == 'wave':
        # 360 waves: rippling bands.
        for y in range(H):
            for x in range(W):
                band = (y + (x // 3) % 2) % 4
                d.point((x, y), fill=grey([120, 176, 228, 176][band]))
    return im


def fabric(name):
    rng = random.Random(sum(map(ord, name)) * 131)
    if name == 'silk':
        # A printed silk wrap: deep teal with an ochre and rust repeat.
        im = Image.new('RGBA', (W, H), (34, 92, 96, 255))
        d = ImageDraw.Draw(im)
        for y in range(0, H, 8):
            for x in range((y // 8) % 2 * 4, W, 8):
                d.rectangle((x, y + 2, x + 3, y + 5), fill=(196, 146, 54, 255))
                d.point((x + 1, y + 3), fill=(150, 64, 40, 255))
                d.point((x + 2, y + 4), fill=(150, 64, 40, 255))
        for y in range(H):
            if y % 8 == 7:
                d.line((0, y, W, y), fill=(24, 66, 70, 255))
    elif name == 'satin':
        # A satin bonnet: plum with a soft sheen.
        im = Image.new('RGBA', (W, H), (110, 52, 84, 255))
        d = ImageDraw.Draw(im)
        for y in range(0, H, 5):
            d.line((0, y, W, y), fill=(146, 84, 118, 255))
        for _ in range(60):
            d.point((rng.randrange(W), rng.randrange(H)), fill=(176, 120, 150, 255))
    elif name == 'tie':
        im = Image.new('RGBA', (W, H), (182, 70, 72, 255))
        d = ImageDraw.Draw(im)
        for x in range(0, W, 3):
            d.line((x, 0, x, H), fill=(146, 50, 54, 255))
    elif name == 'clip':
        im = Image.new('RGBA', (W, H), (172, 128, 70, 255))
        d = ImageDraw.Draw(im)
        for x in range(0, W, 4):
            d.line((x, 0, x + 2, H), fill=(114, 88, 55, 255))
    else:
        # Hot rollers: cream and pink rolls, spiked.
        im = Image.new('RGBA', (W, H), (232, 222, 196, 255))
        d = ImageDraw.Draw(im)
        for x in range(0, W, 8):
            d.rectangle((x + 4, 0, x + 7, H), fill=(204, 146, 140, 255))
        for y in range(0, H, 3):
            for x in range(0, W, 3):
                d.point((x, y), fill=(120, 108, 92, 255))
    return im


def outputs():
    out = {}
    for name in HAIR:
        out[name + '.png'] = hair(name)
    for name in FABRIC:
        out[name + '.png'] = fabric(name)
    return out


def main():
    p = argparse.ArgumentParser(description=__doc__)
    p.add_argument('--check', action='store_true')
    args = p.parse_args()
    for name, im in outputs().items():
        buf = io.BytesIO()
        im.save(buf, format='PNG')
        dest = OUT / name
        if args.check:
            assert dest.read_bytes() == buf.getvalue(), dest
        else:
            dest.parent.mkdir(parents=True, exist_ok=True)
            dest.write_bytes(buf.getvalue())
    print(('checked ' if args.check else 'wrote ') + str(len(HAIR) + len(FABRIC)) + ' hair and fabric sheets')


if __name__ == '__main__':
    main()
