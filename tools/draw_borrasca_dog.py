#!/usr/bin/env python3
"""Biscuit, the Corleys' lost dog (0.4.462).

The store's noticeboard in Drisking has had "Lost: brown dog, answers to Biscuit." since the board was first written. The owner,
10 November: "maybe the kids find an animal in the woods." He is found in the trees by the first marker with a chewed length of
rope trailing from his collar, and can be led home on it (labyrinth.BorrascaDogs).

He is drawn on the game's own wolf: its model, its walk, its sitting. This is his skin for that model, 64 by 32, sixteen pixels to
the block, painted from nothing: a brown coat with a darker saddle, a tan muzzle, chest, belly and socks, dark ears, a red collar
with a brass tag, and the end of the rope down his chest. No pixel of it comes from the game's wolf.

The wolf model's boxes, as its layer lays them out (width, height, depth; a box at u, v has its top at u+d, v; its bottom at
u+d+w, v; its four sides in a row from u, v+d: right, front, left, back):

    head   0, 0   6, 6, 4      ears  16, 14  2, 2, 1      nose   0, 10  3, 3, 4
    body  18, 14  6, 9, 6      mane  21, 0   8, 6, 7      legs   0, 18  2, 8, 2      tail  9, 18  2, 8, 2

The body and the mane lie on their backs in the model (turned a quarter about the x axis), so the box's own "back" side is the
dog's back, its "front" side the belly, its "top" the end toward the head and its "bottom" the end toward the tail.

--check verifies the file without writing it; --sheet PATH writes an enlarged copy for review.
"""
from pathlib import Path
import argparse
import json
from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / 'src/main/resources/assets/the_oldest_house/textures/entity/borrasca_biscuit.png'
LANG = ROOT / 'src/main/resources/assets/the_oldest_house/lang/en_us.json'
SOUNDS = ROOT / 'src/main/resources/assets/the_oldest_house/sounds.json'
# His name, and what is heard of him: the game's own wolf recordings under his own subtitles, so that a reader who reads the
# sounds is told a dog barked and not a wolf.
WORDS = {'entity.the_oldest_house.borrasca_dog': 'Biscuit',
         'subtitles.the_oldest_house.borrasca.dog_bark': 'Dog barks',
         'subtitles.the_oldest_house.borrasca.dog_whine': 'Dog whines',
         'subtitles.the_oldest_house.borrasca.dog_pant': 'Dog pants'}
VOICE = {'borrasca.dog_bark': {'subtitle': 'subtitles.the_oldest_house.borrasca.dog_bark',
                               'sounds': [{'name': f'minecraft:mob/wolf/bark{i}'} for i in range(1, 4)]},
         'borrasca.dog_whine': {'subtitle': 'subtitles.the_oldest_house.borrasca.dog_whine', 'sounds': [{'name': 'minecraft:mob/wolf/whine'}]},
         'borrasca.dog_pant': {'subtitle': 'subtitles.the_oldest_house.borrasca.dog_pant', 'sounds': [{'name': 'minecraft:mob/wolf/panting'}]}}

COAT, SADDLE, TAN, EAR = '#7a5230', '#5c3c22', '#b08052', '#4a2f1a'
NOSE, EYE, PAD = '#1c1512', '#17100b', '#3a2a20'
COLLAR, TAG, ROPE, ROPE_DARK = '#8f2f28', '#d6b45a', '#c9b382', '#9c8a62'


def rgb(c):
    return tuple(int(c[i:i + 2], 16) for i in (1, 3, 5))


def tone(c, k=0):
    r, g, b = rgb(c)
    return (max(0, min(255, r + k)), max(0, min(255, g + k)), max(0, min(255, b + k)), 255)


def grain(x, y):
    """A few shades either way, the same every run."""
    return ((x * 73856093) ^ (y * 19349663)) % 9 - 4


def fill(px, u, v, w, h, colour, k=0):
    for x in range(u, u + w):
        for y in range(v, v + h):
            px[x, y] = tone(colour, k + grain(x, y))


def box(u, v, w, h, d):
    """The six faces of a box at u, v, each as (u, v, width, height)."""
    return {'top': (u + d, v, w, d), 'bottom': (u + d + w, v, w, d), 'right': (u, v + d, d, h), 'front': (u + d, v + d, w, h),
            'left': (u + d + w, v + d, d, h), 'back': (u + d + w + d, v + d, w, h)}


def paint():
    im = Image.new('RGBA', (64, 32), (0, 0, 0, 0))
    px = im.load()

    # The head: brown, the jaw's underside tan, the cheeks paler toward the jaw; eyes with a pale lid each.
    head = box(0, 0, 6, 6, 4)
    for name, (u, v, w, h) in head.items():
        fill(px, u, v, w, h, TAN if name == 'bottom' else COAT, 4 if name == 'top' else 0)
    for side in ('right', 'left'):
        u, v, w, h = head[side]
        fill(px, u, v + 4, w, 2, COAT, 10)
    u, v, w, h = head['front']
    fill(px, u + 2, v, 2, 3, COAT, 12)                      # a paler blaze between the eyes
    for x in (u + 1, u + 4):
        px[x, v + 2] = tone(EYE)
        px[x, v + 1] = tone(SADDLE)                          # the brow
    px[u, v + 2] = tone(TAN, 30); px[u + 5, v + 2] = tone(TAN, 30)

    # The ears: dark, and darker at the tip.
    for name, (u, v, w, h) in box(16, 14, 2, 2, 1).items():
        fill(px, u, v, w, h, EAR)

    # The muzzle: tan, a black nose at its top, a darker line where the mouth is.
    nose = box(0, 10, 3, 3, 4)
    for name, (u, v, w, h) in nose.items():
        fill(px, u, v, w, h, TAN, 6 if name == 'top' else 0)
    u, v, w, h = nose['front']
    px[u + 1, v] = tone(NOSE); px[u, v] = tone(NOSE, 14); px[u + 2, v] = tone(NOSE, 14)
    fill(px, u, v + 2, 3, 1, TAN, -34)
    for side in ('right', 'left'):
        u, v, w, h = nose[side]
        fill(px, u, v + 2, w, 1, TAN, -30)

    # The body: brown flanks, a darker saddle down the back, a tan belly.
    body = box(18, 14, 6, 9, 6)
    for name, (u, v, w, h) in body.items():
        fill(px, u, v, w, h, TAN if name == 'front' else COAT)
    u, v, w, h = body['back']
    fill(px, u + 1, v, 4, h, SADDLE)

    # The shoulders and neck: the saddle goes on over them; a tan bib on the chest; the collar round the end toward the head.
    mane = box(21, 0, 8, 6, 7)
    for name, (u, v, w, h) in mane.items():
        fill(px, u, v, w, h, TAN if name == 'front' else COAT)
    u, v, w, h = mane['back']
    fill(px, u + 1, v, 6, h, SADDLE)
    u, v, w, h = mane['top']
    fill(px, u + 2, v, 4, h, TAN)
    for name in ('right', 'front', 'left', 'back'):
        u, v, w, h = mane[name]
        fill(px, u, v, w, 1, COLLAR)
    u, v, w, h = mane['front']
    px[u + 3, v] = tone(TAG); px[u + 4, v] = tone(TAG, -20)
    # The end of the rope, chewed through, down his chest from the collar.
    u, v, w, h = mane['top']
    for y in range(v + 1, v + 6):
        px[u + 4, y] = tone(ROPE if (y - v) % 2 else ROPE_DARK)
    px[u + 3, v + 5] = tone(ROPE_DARK); px[u + 5, v + 6] = tone(ROPE, -10)

    # The legs: brown to the knee, tan socks, a dark pad. All four are the one picture.
    legs = box(0, 18, 2, 8, 2)
    for name, (u, v, w, h) in legs.items():
        if name in ('top', 'bottom'):
            fill(px, u, v, w, h, PAD if name == 'bottom' else COAT)
        else:
            fill(px, u, v, w, 4, COAT); fill(px, u, v + 4, w, 4, TAN)

    # The tail: brown, its last two rows pale.
    tail = box(9, 18, 2, 8, 2)
    for name, (u, v, w, h) in tail.items():
        if name in ('top', 'bottom'):
            fill(px, u, v, w, h, TAN if name == 'bottom' else COAT, 8 if name == 'bottom' else 0)
        else:
            fill(px, u, v, w, 6, COAT); fill(px, u, v + 6, w, 2, TAN, 12)
    return im


def main():
    args = argparse.ArgumentParser(); args.add_argument('--check', action='store_true'); args.add_argument('--sheet'); args = args.parse_args()
    im = paint()
    assert im.size == (64, 32)
    if args.sheet:
        im.resize((64 * 10, 32 * 10), Image.NEAREST).save(args.sheet); return
    lang, sounds = json.loads(LANG.read_text()), json.loads(SOUNDS.read_text())
    if args.check:
        with Image.open(OUT) as actual:
            assert actual.convert('RGBA').tobytes() == im.tobytes(), OUT
        for key, word in WORDS.items():
            assert lang.get(key) == word, key
        for key, entry in VOICE.items():
            assert sounds.get(key) == entry, key
        print('Verified Biscuit: one skin, 64 by 32, his name and his three sounds.')
    else:
        OUT.parent.mkdir(parents=True, exist_ok=True); im.save(OUT, optimize=True)
        if any(lang.get(key) != word for key, word in WORDS.items()):
            lang.update(WORDS)
            LANG.write_text(json.dumps(lang, indent=2, ensure_ascii=False) + '\n')
        if any(sounds.get(key) != entry for key, entry in VOICE.items()):
            sounds.update(VOICE)
            SOUNDS.write_text(json.dumps(sounds, indent=2) + '\n')
        print('Drew Biscuit: one skin, 64 by 32, his name and his three sounds.')


if __name__ == '__main__':
    main()
