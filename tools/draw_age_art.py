"""Art for the House's read of a reader's age (0.4.372): Harrigan's spare as a handset with buttons.

A 32-pixel candybar phone, upright like the clock and the compass: a stub aerial, an earpiece, a small liquid-crystal screen with a
line of text on it, a rocker under the screen, twelve keys in four rows and the microphone hole. Drawn pixel by pixel in code; no
photographs, no painted shading. The existing phone item keeps its own texture; custom_model_data 1 selects this one.

  python3 tools/draw_age_art.py           draw anything missing (an existing texture is kept)
  python3 tools/draw_age_art.py --check   verify the texture, the model and the phone's override
"""
import json
import sys
from pathlib import Path

ASSETS = Path(__file__).resolve().parents[1] / 'src/main/resources/assets/the_oldest_house'
TEXTURE = ASSETS / 'textures/item/phone_handset.png'
MODEL = ASSETS / 'models/item/phone_handset.json'
PHONE = ASSETS / 'models/item/phone.json'
HANDSET_MODEL = {'parent': 'minecraft:item/generated', 'textures': {'layer0': 'the_oldest_house:item/phone_handset'}}
OVERRIDE = {'predicate': {'custom_model_data': 1}, 'model': 'the_oldest_house:item/phone_handset'}

OUTLINE = (30, 32, 36, 255)
BODY = (72, 76, 82, 255)
EDGE = (100, 104, 110, 255)
SHADE = (56, 59, 64, 255)
KEY = (170, 174, 180, 255)
KEY_LOW = (132, 136, 142, 255)
BEZEL = (40, 44, 40, 255)
LCD = (150, 168, 126, 255)
LCD_INK = (62, 76, 50, 255)
SLOT = (22, 24, 26, 255)


def pixels():
    """The handset as a 32x32 grid of colours (None is transparent)."""
    grid = [[None] * 32 for _ in range(32)]

    def put(x, y, c):
        grid[y][x] = c

    # The aerial, a stub on the top right.
    for y in range(1, 6):
        for x in (18, 19):
            put(x, y, OUTLINE)
    put(18, 2, EDGE)
    put(18, 3, BODY)
    # The body, x 10..21, y 5..30, corners cut.
    for y in range(5, 31):
        for x in range(10, 22):
            corner = (y in (5, 30)) and x in (10, 21)
            if corner:
                continue
            edge = x in (10, 21) or y in (5, 30) or ((y in (6, 29)) and x in (10, 21))
            put(x, y, OUTLINE if edge else BODY)
    for y in range(6, 30):
        put(11, y, EDGE)
        put(20, y, SHADE)
    # The earpiece.
    for x in range(14, 18):
        put(x, 7, SLOT)
    # The screen: a dark bezel round a small panel with a line of text and the signal.
    for y in range(9, 16):
        for x in range(12, 20):
            put(x, y, BEZEL)
    for y in range(10, 15):
        for x in range(13, 19):
            put(x, y, LCD)
    for x in range(13, 18):
        put(x, 11, LCD_INK)
    for x in range(13, 16):
        put(x, 13, LCD_INK)
    # The rocker under the screen.
    for x in range(14, 18):
        put(x, 17, KEY_LOW)
    put(15, 17, KEY)
    put(16, 17, KEY)
    # Twelve keys, two pixels wide, in four rows.
    for row in range(4):
        y = 19 + row * 2
        for col in range(3):
            x = 12 + col * 3
            put(x, y, KEY)
            put(x + 1, y, KEY_LOW)
    # The microphone.
    put(15, 28, SLOT)
    put(16, 28, SLOT)
    return grid


def draw():
    from PIL import Image
    image = Image.new('RGBA', (32, 32), (0, 0, 0, 0))
    for y, row in enumerate(pixels()):
        for x, c in enumerate(row):
            if c is not None:
                image.putpixel((x, y), c)
    return image


def phone_model():
    model = json.loads(PHONE.read_text())
    overrides = [o for o in model.get('overrides', []) if o.get('predicate', {}).get('custom_model_data') != 1]
    model['overrides'] = overrides + [OVERRIDE]
    return model


def check():
    from PIL import Image
    assert TEXTURE.exists(), TEXTURE
    image = Image.open(TEXTURE)
    assert image.size == (32, 32) and image.mode == 'RGBA', (image.size, image.mode)
    rgba = image.convert('RGBA')
    opaque = sum(1 for y in range(32) for x in range(32) if rgba.getpixel((x, y))[3])
    assert 200 < opaque < 500, opaque
    assert json.loads(MODEL.read_text()) == HANDSET_MODEL, MODEL
    assert OVERRIDE in json.loads(PHONE.read_text()).get('overrides', []), PHONE
    print('Verified the handset texture, its model and the phone override.')


def main():
    if '--check' in sys.argv:
        check()
        return
    if not TEXTURE.exists():
        draw().save(TEXTURE)
    MODEL.write_text(json.dumps(HANDSET_MODEL, indent=2) + '\n')
    PHONE.write_text(json.dumps(phone_model(), indent=2) + '\n')
    check()


if __name__ == '__main__':
    main()
