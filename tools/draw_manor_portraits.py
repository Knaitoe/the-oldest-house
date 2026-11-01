"""The manor's five family portraits (0.4.223), painted the way the House paints a reader's first death.

The owner: "improve the paintings here, bring them in line with the character-captured portraits". A captured portrait is a
sixteen-by-thirty-two bust the client paints from a reader's skin (client/PortraitPainter): the head ten pixels wide from the
skin's face and hat, the shoulders from its body and sleeves, a warm pigment over the painting's own dark ground. The family
portraits were hand-placed blocks of colour. Each is now a member of the household painted by the cast's common painter
(paint_character_skins.paint) and then put through the same bust as the captured portraits, at the painter's own detail,
over its old ground. --check verifies the five paintings.
"""
from pathlib import Path
import argparse
from PIL import Image
from paint_character_skins import Look, paint

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / 'src/main/resources/assets/the_oldest_house/textures/painting'

# Who hangs in the manor: a father in his work jacket, a mother with her hair up, their child, the grandfather, and the woman
# nobody in the family can name, in her wine-dark dress. Each keeps its painting's old ground, and each looks straight out of
# the frame (both irises in the middle of the eye; the cast's painter otherwise turns the right one in a little).
FAMILY = {
    'will_portrait': (Look('#c99c7c', '#3b2c22', 'part', '#5b6a4e', '#3e4636', 'jacket', '#b9ab88', eyes='#5a6650', age=1, beard='stubble', level_eyes=True), (0x3b, 0x37, 0x32)),
    'karen_portrait': (Look('#d8ae92', '#6a4a30', 'long', '#7d5a6a', '#4a3c44', 'cardigan', '#d6c8b0', eyes='#59705e', level_eyes=True), (0x3e, 0x34, 0x30)),
    'child_portrait': (Look('#d4a98c', '#7a5634', 'messy', '#5f7f96', '#3e4c56', 'tee', '#c8b890', eyes='#5d6a74', sleeve=4, level_eyes=True), (0x42, 0x3b, 0x33)),
    'old_man_portrait': (Look('#c8a488', '#c4bdb0', 'receding', '#4a4a52', '#38383e', 'suit', '#a6a090', eyes='#646c74', age=2, beard='short', level_eyes=True), (0x34, 0x34, 0x30)),
    'unknown_woman_portrait': (Look('#c99a7e', '#241c1a', 'long', '#6e2a30', '#4a2226', 'dress', '#d2bca0', eyes='#4e4038', level_eyes=True), (0x3c, 0x35, 0x33)),
}


# client/PortraitPainter.COLS2/ROWS2 (0.4.224): mirror-symmetric, both eyes, the nose and the cheeks kept whole.
COLS2 = (1, 3, 4, 5, 7, 8, 10, 11, 12, 14)
ROWS2 = (1, 3, 4, 5, 6, 7, 8, 9, 10, 12, 13, 14)


def native(sheet):
    """The painter's sheet at its own detail: the vanilla layout, sixteen pixels to a face (half the shipped scale)."""
    return sheet.resize((sheet.width // 2, sheet.height // 2), Image.Resampling.NEAREST).convert('RGBA')


def over(under, top):
    a = top[3] / 255
    return tuple(round(top[i] * a + under[i] * (1 - a)) for i in range(3)) + (255,)


def step(c):
    return max(16, min(208, round(c / 8) * 8))


def pigment(c, x, y, head):
    """client/PortraitPainter.pigment: warm, a little greyed, darker to the right and toward the foot, in steps of eight."""
    r, g, b = c[:3]
    grey = r * .299 + g * .587 + b * .114
    light = (1.04 if head else .9) - max(0, x - 6) * .018 - max(0, y - 22) * .009
    return (step((r * .62 + grey * .18 + 16) * light), step((g * .60 + grey * .17 + 10) * light), step((b * .54 + grey * .19 + 6) * light), 255)


def bust(skin, ground):
    """client/PortraitPainter.paint, sampling the face, body and sleeve at the painter's sixteen-to-the-face detail."""
    s = 2
    px = skin.load()
    out = Image.new('RGBA', (16, 32), ground + (255,)); o = out.load()
    for y in range(32):
        for x in range(16):
            colour = ground + (255,)
            if y >= 16 and x >= (2 if y == 16 else 1) and x <= (13 if y == 16 else 14):
                u = min(8 * s - 1, (x - 1) * 8 * s // 14); v = min(12 * s - 1, (y - 16) * 12 * s // 16)
                colour = over(px[20 * s + u, 20 * s + v], px[20 * s + u, 36 * s + v])
                if x < 3 or x > 12:
                    a = (x % 3) * s
                    colour = over(px[44 * s + a, 20 * s + v], px[44 * s + a, 36 * s + v])
                colour = pigment(colour, x, y, False)
            if 3 <= x <= 12 and 5 <= y <= 16:
                u = COLS2[x - 3]; v = ROWS2[y - 5]
                colour = pigment(over(px[8 * s + u, 8 * s + v], px[40 * s + u, 8 * s + v]), x, y, True)
            o[x, y] = colour
    return out


def portraits():
    return {name: bust(native(paint(look)), ground) for name, (look, ground) in FAMILY.items()}


def main():
    check = argparse.ArgumentParser(); check.add_argument('--check', action='store_true'); check = check.parse_args().check
    for name, im in portraits().items():
        path = OUT / f'{name}.png'
        if check:
            with Image.open(path) as actual:
                assert actual.convert('RGBA').tobytes() == im.tobytes(), name
        else:
            im.save(path, optimize=True)
    print(('Verified' if check else 'Painted') + f' the manor\'s {len(FAMILY)} family portraits.')


if __name__ == '__main__':
    main()
