"""Dr. Hale's corkboard (0.4.455): what the board and its cards are drawn from.

The owner, of the page of text that described the doctor's corkboard: "Try to show, don't tell. Try to design an actual ui for
this". The board is a screen now (client.BorrascaCardsScreen): cork in a wooden frame, the families' birth cards pinned to it,
each to be taken down and looked at. This draws its one sheet of parts, in native pixels from fixed rows:

- the cork, a square that repeats, and the frame's wood, along and down, with its corner;
- the pictures printed on the cards: a stork with its bundle, a rattle, a bow, a pram, a bottle and a duck;
- a pin's head (pale, to be coloured as it is drawn), and the curl of a card's corner.

The cards themselves are drawn by the screen (paper, a printed band, the family's own hand), so their words stay words.
--check verifies the sheet. --sheet PATH writes it enlarged for review.
"""
from pathlib import Path
import argparse
import random
from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / 'src/main/resources/assets/the_oldest_house/textures/gui/borrasca_cards.png'
W, H = 128, 64


def rgb(c):
    c = c.lstrip('#')
    return tuple(int(c[i:i + 2], 16) for i in (0, 2, 4)) + (255,)


def put(im, x, y, rows, palette):
    for dy, row in enumerate(rows):
        for dx, ch in enumerate(row):
            if ch not in ' .':
                im.putpixel((x + dx, y + dy), rgb(palette[ch]))


def cork(im):
    """Thirty-two pixels square, to repeat: pressed cork, its grains in small clusters, a few pin holes."""
    tones = ('#b98d5a', '#c39a66', '#ad8150', '#9a7043')
    rnd = random.Random(45501)
    for y in range(32):
        for x in range(32):
            im.putpixel((x, y), rgb(tones[0]))
    for _ in range(70):
        x, y, tone = rnd.randrange(32), rnd.randrange(32), rnd.choice(tones[1:])
        for dx, dy in ((0, 0), (1, 0)) if rnd.random() < .6 else ((0, 0), (0, 1), (1, 1)):
            im.putpixel(((x + dx) % 32, (y + dy) % 32), rgb(tone))
    for _ in range(5):
        im.putpixel((rnd.randrange(32), rnd.randrange(32)), rgb('#6f4e2c'))


def frame(im):
    """The frame's wood: a strip to run along the top and bottom (at 0,32), one to run down the sides (at 32,0), and a corner (at 32,32)."""
    wood = {'L': '#8a5f38', 'M': '#6f4a2a', 'D': '#553720', 'K': '#3c2615'}
    grain = random.Random(45502)
    for x in range(32):
        column = 'LMMMDK'
        for y, ch in enumerate(column):
            c = wood[ch]
            if ch == 'M' and grain.random() < .18:
                c = wood['D']
            im.putpixel((x, 32 + y), rgb(c))
    for y in range(32):
        for x, ch in enumerate('LMMMDK'):
            c = wood[ch]
            if ch == 'M' and grain.random() < .18:
                c = wood['D']
            im.putpixel((32 + x, y), rgb(c))
    put(im, 32, 32, ['LLLLLL', 'LMMMMM', 'LMMMMD', 'LMMMDD', 'LMMDDK', 'LMDDKK'], wood)


INK = {'W': '#f6f2e8', 'w': '#d9d3c4', 'K': '#2a2622', 'O': '#e08a2e', 'c': '#bcd8ee', 'd': '#8fb6d6', 'P': '#f0b9a8', 'R': '#d8524a',
       'r': '#a83a36', 'B': '#4f86c6', 'b': '#2f5f9e', 'Y': '#f2cf4a', 'y': '#caa32c', 'G': '#9aa3aa', 'g': '#6c757c', 'p': '#f29ab4',
       'q': '#c96a8c', 'N': '#3a4a6a', 'T': '#e9e2cf'}

PICTURES = [
    # A stork, its bundle hung from its beak.
    ['................', '.........WW.....', '........WWKW....', '...OOOOOWWWW....', '...c.....WW.....', '..ccc....WW.....',
     '.ccccc...WWW....', '.ccPcc..WWWWW...', '.dcccd.WWWWWWK..', '..ddd..WWWWWKK..', '........WWWKK...', '.........O......',
     '.........O......', '.........O......', '........OO......', '................'],
    # A rattle.
    ['................', '....BBBB........', '...BccccB.......', '..BcWccccB......', '..BccccccB......', '..BccccYcB......',
     '..BccccccB......', '...BccccB.......', '....BBBB........', '......bB........', '.......bB.......', '........bB......',
     '.........bBB....', '.........BccB...', '..........BB....', '................'],
    # A bow of ribbon.
    ['................', '................', '..pp......pp....', '.pppp....pppp...', '.ppppp..ppppp...', '.pqpppppppppqp..',
     '.pqqppqqqppqqp..', '.pqpppqqqpppqp..', '.ppppp.qq.pppp..', '.pppp..qp..ppp..', '..pp..qp.pq.....', '.....qp...pq....',
     '....qp.....pq...', '....p.......p...', '................', '................'],
    # A pram.
    ['................', '................', '...NNNN.........', '..NNNNNN.....K..', '.NNNNNNN....K...', '.NNNNNNNN..K....',
     '.NNNNNNNNBBBBB..', '.BBBBBBBBBBBBB..', '.BBBBBBBBBBBBB..', '..BBBBBBBBBBB...', '...BBBBBBBBB....', '....g.....g.....',
     '...KKK...KKK....', '...KGK...KGK....', '...KKK...KKK....', '................'],
    # A bottle.
    ['................', '......OO........', '......OO........', '.....OOOO.......', '....GGGGGG......', '....WWWWWW......',
     '....WwwwwW......', '....WWWWWW......', '....WTTTTW......', '....WTTTTW......', '....WTTTTW......', '....WTTTTW......',
     '....WTTTTW......', '....WWWWWW......', '.....wwww.......', '................'],
    # A duck.
    ['................', '................', '.....YYY........', '....YYKYY.......', '..OOYYYYY.......', '....YYYYY.......',
     '.....YYY....Y...', '....YYYYYYYYY...', '...YYYYYYYYYY...', '...YYyyyyYYYY...', '...YYYyyYYYY....', '....YYYYYYY.....',
     '.....YYYYY......', '......O.O.......', '.....OO.OO......', '................'],
]

# A pin's head, pale so that it takes the colour it is drawn in; and the curl at the foot of a card's corner.
PIN = ['.WGG.', 'WWGGg', 'GGGGg', 'GGGgg', '.ggg.']
# Pale, so that the colour it is drawn in comes out as that colour and not a darker one.
PIN_INK = {'W': '#ffffff', 'G': '#e4e4e4', 'g': '#a6a6a6'}
CURL = ['.......w', '......ww', '.....wwT', '....wwTT', '...wwTTT', '..wwTTTT', '.wwTTTTT', 'wwTTTTTT']


def draw():
    im = Image.new('RGBA', (W, H), (0, 0, 0, 0))
    cork(im); frame(im)
    for i, rows in enumerate(PICTURES):
        assert len(rows) == 16 and all(len(r) == 16 for r in rows), i
        put(im, 40 + (i % 5) * 16, (i // 5) * 16, rows, INK)
    put(im, 40, 34, PIN, PIN_INK); put(im, 48, 34, CURL, INK)
    return im


def main():
    args = argparse.ArgumentParser(); args.add_argument('--check', action='store_true'); args.add_argument('--sheet')
    args = args.parse_args(); im = draw()
    if args.sheet:
        im.resize((W * 6, H * 6), Image.NEAREST).save(args.sheet); return
    if args.check:
        with Image.open(OUT) as actual:
            assert actual.convert('RGBA').tobytes() == im.tobytes(), 'the corkboard sheet differs from what this draws'
    else:
        OUT.parent.mkdir(parents=True, exist_ok=True); im.save(OUT, optimize=True)
    print(('Verified' if args.check else 'Drew') + f' the corkboard sheet: {W} by {H}, {len(PICTURES)} pictures.')


if __name__ == '__main__':
    main()
