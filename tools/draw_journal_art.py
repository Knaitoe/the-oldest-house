#!/usr/bin/env python3
"""The reader's journal icon at its five wear stages (JOURNAL_DESIGN.md), 16x16, in vanilla's item style.

0: a brown cloth-bound notebook with an elastic band and cream page edges.
1: water rings on the cover.  2: a dog-eared corner.  3: swollen, rippled pages.  4: a torn corner and a tide line.
Run with python3.13 from the repository root; writes assets/the_oldest_house/textures/item/journal_0..4.png.
"""
from pathlib import Path
from PIL import Image

OUT = Path('src/main/resources/assets/the_oldest_house/textures/item')
COVER = (92, 60, 38, 255); COVER_LIGHT = (122, 82, 52, 255); COVER_DARK = (62, 40, 26, 255)
PAGE = (232, 222, 196, 255); PAGE_SHADE = (196, 184, 156, 255); BAND = (34, 30, 30, 255)
RING = (70, 46, 30, 255); TIDE = (150, 128, 92, 255); CLEAR = (0, 0, 0, 0)


def journal(wear):
    img = Image.new('RGBA', (16, 16), CLEAR)
    px = img.load()
    # The page block shows along the right and bottom edges, behind the cover.
    for y in range(3, 15):
        for x in range(4, 14):
            px[x, y] = PAGE if (x + y) % 3 else PAGE_SHADE
    # The cover, cloth-bound, with its spine to the left.
    for y in range(2, 14):
        for x in range(3, 13):
            px[x, y] = COVER
    for y in range(2, 14):
        px[3, y] = COVER_DARK
        px[4, y] = COVER_LIGHT
    for x in range(3, 13):
        px[x, 2] = COVER_LIGHT
        px[x, 13] = COVER_DARK
    for y in range(2, 14):
        px[12, y] = COVER_DARK
    # The elastic band down the front.
    for y in range(2, 14):
        px[10, y] = BAND
    if wear >= 1:
        for x, y in ((6, 5), (7, 4), (8, 5), (8, 6), (7, 7), (6, 6)):
            px[x, y] = RING
    if wear >= 2:
        # The top outer corner folded down.
        px[12, 2] = CLEAR
        px[11, 2] = PAGE
        px[12, 3] = PAGE
        px[11, 3] = PAGE_SHADE
    if wear >= 3:
        # Swollen pages bulge past the cover, unevenly.
        for y in range(4, 15, 2):
            px[14, y] = PAGE
        for x in range(5, 14, 3):
            px[x, 15] = PAGE_SHADE
    if wear >= 4:
        for x, y in ((5, 10), (6, 11), (7, 10), (8, 11), (9, 10), (11, 11)):
            px[x, y] = TIDE
        for x, y in ((12, 12), (12, 13), (11, 13), (13, 13), (13, 14), (12, 14)):
            px[x, y] = CLEAR
    return img


def main():
    OUT.mkdir(parents=True, exist_ok=True)
    for wear in range(5):
        journal(wear).save(OUT / f'journal_{wear}.png')


if __name__ == '__main__':
    main()
