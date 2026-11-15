"""0.4.608 (docs/ONES_INSIDE_KEY_0_4_608.md): the lodging house's release key, the one that opens Daniel's straps.

A long iron key laid corner to corner as vanilla's tools are: an oval bow at the top left worn bright on its inside edge, a plain shank,
and near its end a bit, a flag off the shank's lower side with a ward notched out of it. Iron gone dark with handling, lit from the top left.

    python3 tools/draw_lodging_key.py          writes src/main/resources/assets/the_oldest_house/textures/item/lodging_key.png
    python3 tools/draw_lodging_key.py --check  fails if the file on disk is not what this draws
"""
import sys
from pathlib import Path
from PIL import Image

OUT = Path(__file__).resolve().parent.parent / 'src/main/resources/assets/the_oldest_house/textures/item/lodging_key.png'
OUTLINE, DARK, MID, LIGHT, SHINE = '#1c1b1d', '#38363a', '#55525a', '#7b7782', '#a8a3ad'


def rgb(h):
    return tuple(int(h[i:i + 2], 16) for i in (1, 3, 5)) + (255,)


def draw():
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    px = im.load()

    def put(x, y, c):
        if 0 <= x < 16 and 0 <= y < 16:
            px[x, y] = rgb(c)

    # The bow: an oval ring, its hole showing through.
    ring = [(2, 1), (3, 1), (4, 1), (1, 2), (5, 2), (1, 3), (5, 3), (1, 4), (5, 4), (2, 5), (3, 5), (4, 5)]
    for x, y in ring:
        put(x, y, MID)
    for x, y in [(2, 1), (1, 2), (1, 3)]:
        put(x, y, LIGHT)
    for x, y in [(3, 2), (2, 3)]:
        pass  # the hole
    put(4, 2, SHINE)  # the inside edge, worn bright by a thumb
    for x, y in [(5, 4), (4, 5)]:
        put(x, y, DARK)
    # The collar where the bow meets the shank.
    put(5, 5, LIGHT); put(6, 5, MID); put(5, 6, MID); put(6, 6, MID)
    # The shank, corner to corner, two pixels wide: light on its upper edge, dark on its lower.
    for i in range(7, 14):
        put(i, i, MID)
        put(i, i - 1, LIGHT)
    put(14, 14, MID)  # the tip
    # The bit: a flag off the lower side of the shank near its end, two rows deep, a ward notched out of its outer row.
    for x, y in [(9, 11), (10, 12), (11, 13), (12, 14)]:
        put(x, y, MID)
    for x, y in [(8, 12), (11, 15)]:
        put(x, y, DARK)
    put(9, 13, MID)
    put(10, 14, DARK)
    # Outline: every empty pixel touching iron, darkest.
    iron = {(x, y) for x in range(16) for y in range(16) if px[x, y][3]}
    for x, y in list(iron):
        for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)):
            n = (x + dx, y + dy)
            if 0 <= n[0] < 16 and 0 <= n[1] < 16 and n not in iron and n not in {(3, 2), (2, 3), (3, 3), (2, 4), (3, 4), (4, 3), (4, 4), (2, 2)}:
                px[n] = rgb(OUTLINE)
    return im


def main():
    im = draw()
    if '--check' in sys.argv:
        old = Image.open(OUT).convert('RGBA')
        assert old.tobytes() == im.tobytes(), f'{OUT} is not what tools/draw_lodging_key.py draws'
        print('lodging key: ok')
        return
    OUT.parent.mkdir(parents=True, exist_ok=True)
    im.save(OUT)
    print('wrote', OUT)


if __name__ == '__main__':
    main()
