"""The Barretts' ordinary errands between takes, drawn as pixel art (0.4.247).

The owner, 2 November 2026: "Give Ghosts Set more ordinary behavior between scenes. In the earlier days, a parent clears a plate,
someone retrieves a forgotten mug, Barry checks a cable while talking." Sarah's plate is the supper plate the trailer already has;
John's mug is drawn here at the game's own scale:

- textures/item/ghosts_mug.png (16x16): a plain stoneware mug, a little chipped at the rim, with coffee in it, its handle to the right;
- models/item/ghosts_mug.json: generated item model;
- the item's name in lang/en_us.json.

Run without arguments to draw; --check verifies the files match.
"""
from pathlib import Path
import io
import json
import sys
from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / 'src/main/resources/assets/the_oldest_house'
TEXTURE = ASSETS / 'textures/item/ghosts_mug.png'
MODEL = ASSETS / 'models/item/ghosts_mug.json'
LANG = ASSETS / 'lang/en_us.json'
NAME = ('item.the_oldest_house.ghosts_mug', "John's mug")

PALETTE = {
    '.': (0, 0, 0, 0),
    'o': (58, 52, 48, 255),      # outline
    'w': (226, 220, 204, 255),   # glaze
    'l': (244, 240, 228, 255),   # glaze, catching the light
    's': (190, 182, 164, 255),   # glaze in shadow
    'b': (96, 120, 140, 255),    # the blue band
    'B': (74, 96, 116, 255),     # the band in shadow
    'c': (92, 58, 34, 255),      # coffee
    'C': (124, 82, 50, 255),     # coffee, lit
    'x': (170, 160, 142, 255),   # the chip at the rim
}
MUG = [
    '................',
    '................',
    '................',
    '...oooooooxo....',
    '..owcCCcccswo...',
    '..owwlwwwwsso...',
    '..owlwwwwwssoooo',
    '..owlwwwwwsso..o',
    '..obbbbbbbBBo..o',
    '..owlwwwwwsso..o',
    '..owlwwwwwssoooo',
    '..owwwwwwwsso...',
    '..oswwwwwssso...',
    '...osssssss o...',
    '....ooooooooo...',
    '................',
]


def texture():
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    for y, row in enumerate(MUG):
        assert len(row) == 16, (y, row)
        for x, ch in enumerate(row):
            im.putpixel((x, y), PALETTE.get(ch, PALETTE['.']) if ch != ' ' else PALETTE['s'])
    return im


def files():
    out = io.BytesIO()
    texture().save(out, format='PNG')
    model = json.dumps({'parent': 'minecraft:item/generated', 'textures': {'layer0': 'the_oldest_house:item/ghosts_mug'}}, indent=2) + '\n'
    return {TEXTURE: out.getvalue(), MODEL: model.encode()}


def main():
    check = '--check' in sys.argv
    want = files()
    if check:
        for path, data in want.items():
            assert path.exists(), path
            if path.suffix == '.png':
                have = Image.open(path).convert('RGBA')
                assert have.tobytes() == texture().tobytes(), f'{path} differs from its drawing'
            else:
                assert path.read_bytes() == data, f'{path} differs'
        lang = json.loads(LANG.read_text())
        assert lang.get(NAME[0]) == NAME[1], NAME
        print("Verified John's mug.")
        return
    for path, data in want.items():
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_bytes(data)
    lang = json.loads(LANG.read_text())
    lang[NAME[0]] = NAME[1]
    LANG.write_text(json.dumps(lang, indent=2, ensure_ascii=False) + '\n')
    print("Drew John's mug.")


if __name__ == '__main__':
    main()
