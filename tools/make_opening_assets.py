#!/usr/bin/env python3
"""Generates the opening-sequence art assets.

* ``snapshot/porch.bin``: Navidson's snapshot, pre-baked to Minecraft map
  colour ids (128 x 128 bytes, row-major, the layout of
  ``MapItemSavedData.colors``). A preview PNG of exactly what the map shows
  is written next to this script.
* The entrance door's block and item textures (plain dark wood).

Run from the repository root:  python3 tools/make_opening_assets.py
"""
import os
import random

from PIL import Image, ImageDraw, ImageFilter

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
RES = os.path.join(ROOT, "src", "main", "resources")

# MapColor base colours by id (1.21.1). Id 0 is transparent and never used.
MAP_BASE = [
    None, 0x7FB238, 0xF7E9A3, 0xC7C7C7, 0xFF0000, 0xA0A0FF, 0xA7A7A7, 0x007C00,
    0xFFFFFF, 0xA4A8B8, 0x976D4D, 0x707070, 0x4040FF, 0x8F7748, 0xFFFCF5, 0xD87F33,
    0xB24CD8, 0x6699D8, 0xE5E533, 0x7FCC19, 0xF27FA5, 0x4C4C4C, 0x999999, 0x4C7F99,
    0x7F3FB2, 0x334CB2, 0x664C33, 0x667F33, 0x993333, 0x191919, 0xFAEE4D, 0x5CDBD5,
    0x4A80FF, 0x00D93A, 0x815631, 0x700200, 0xD1B1A1, 0x9F5224, 0x95576C, 0x706C8A,
    0xBA8524, 0x677535, 0xA04D4E, 0x392923, 0x876B62, 0x575C5C, 0x7A4958, 0x4C3E5C,
    0x4C3223, 0x4C522A, 0x8E3C2E, 0x251610, 0xBD3031, 0x943F61, 0x5C191D, 0x167E86,
    0x3A8E8C, 0x562C3E, 0x14B485, 0x646464, 0xD8AF93, 0x7FA796,
]
# MapColor.Brightness: LOW, NORMAL, HIGH, LOWEST.
BRIGHTNESS = [180, 220, 255, 135]


def palette():
    entries = []
    for base_id, rgb in enumerate(MAP_BASE):
        if rgb is None:
            continue
        r, g, b = (rgb >> 16) & 255, (rgb >> 8) & 255, rgb & 255
        for level, mul in enumerate(BRIGHTNESS):
            entries.append((base_id * 4 + level, (r * mul // 255, g * mul // 255, b * mul // 255)))
    return entries


PALETTE = palette()


def nearest(rgb):
    best, best_d = None, None
    for colour_id, (r, g, b) in PALETTE:
        # Weighted distance: the eye forgives hue errors in the dark less than
        # plain RGB distance suggests, so weight green (luma) higher.
        d = 2 * (rgb[0] - r) ** 2 + 4 * (rgb[1] - g) ** 2 + 3 * (rgb[2] - b) ** 2
        if best_d is None or d < best_d:
            best, best_d = (colour_id, (r, g, b)), d
    return best


def quantize(image):
    """Floyd-Steinberg onto the map palette; returns (ids, preview image)."""
    w, h = image.size
    px = [[list(image.getpixel((x, y))) for x in range(w)] for y in range(h)]
    ids = bytearray(w * h)
    preview = Image.new("RGB", (w, h))
    for y in range(h):
        for x in range(w):
            old = [max(0, min(255, int(round(c)))) for c in px[y][x]]
            colour_id, new = nearest(old)
            ids[y * w + x] = colour_id
            preview.putpixel((x, y), new)
            err = [old[i] - new[i] for i in range(3)]
            for dx, dy, f in ((1, 0, 7 / 16), (-1, 1, 3 / 16), (0, 1, 5 / 16), (1, 1, 1 / 16)):
                nx, ny = x + dx, y + dy
                if 0 <= nx < w and 0 <= ny < h:
                    for i in range(3):
                        # Damped diffusion keeps flat silhouettes flat.
                        px[ny][nx][i] += err[i] * f * 0.6
    return ids, preview


def build_snapshot():
    rnd = random.Random(1993)
    size = 128
    frame = (226, 222, 210)
    img = Image.new("RGB", (size, size), frame)

    left, top, right, bottom = 7, 7, 120, 104
    photo_w, photo_h = right - left + 1, bottom - top + 1
    photo = Image.new("RGB", (photo_w, photo_h))
    pd = ImageDraw.Draw(photo)

    for y in range(photo_h):
        t = y / (photo_h - 1)
        pd.line([(0, y), (photo_w, y)], fill=(int(16 + 22 * t), int(22 + 26 * t), int(44 + 36 * t)))

    for _ in range(20):
        sx, sy = rnd.randrange(photo_w), rnd.randrange(int(photo_h * 0.42))
        pd.point((sx, sy), fill=rnd.choice([(150, 150, 160), (185, 185, 195), (120, 120, 138)]))

    ground_y = int(photo_h * 0.8)
    for x in range(photo_w):
        hgt = 5 + int(5 * abs(((x * 7) % 23) - 11) / 11) + rnd.randrange(3)
        pd.line([(x, ground_y - hgt), (x, ground_y)], fill=(12, 14, 13))
    pd.rectangle([0, ground_y, photo_w, photo_h], fill=(9, 14, 9))

    house_l, house_r, eave_y, ridge_y = 24, 90, 40, 19
    pd.rectangle([house_l, eave_y, house_r, ground_y + 2], fill=(13, 11, 13))
    pd.polygon([(house_l - 5, eave_y + 1), ((house_l + house_r) // 2, ridge_y), (house_r + 5, eave_y + 1)],
               fill=(17, 15, 19))
    pd.rectangle([76, 21, 81, 33], fill=(15, 13, 15))
    pd.polygon([(47, 64), (67, 64), (71, 68), (43, 68)], fill=(19, 17, 21))
    pd.rectangle([54, 69, 60, ground_y + 1], fill=(20, 16, 16))
    for wx in (31, 76):
        pd.rectangle([wx, 70, wx + 7, 78], fill=(19, 21, 29))
    for wx in (31, 53):
        pd.rectangle([wx, 46, wx + 7, 55], fill=(19, 21, 29))

    lit = (76, 46, 83, 55)
    glow = Image.new("L", (photo_w, photo_h), 0)
    ImageDraw.Draw(glow).ellipse([lit[0] - 8, lit[1] - 8, lit[2] + 8, lit[3] + 8], fill=150)
    glow = glow.filter(ImageFilter.GaussianBlur(4))
    warm = Image.new("RGB", (photo_w, photo_h), (120, 86, 24))
    photo = Image.composite(warm, photo, glow.point(lambda v: v // 3))
    pd = ImageDraw.Draw(photo)
    pd.rectangle(lit, fill=(236, 196, 84))
    # Mullions.
    pd.line([((lit[0] + lit[2]) // 2, lit[1]), ((lit[0] + lit[2]) // 2, lit[3])], fill=(60, 40, 18))
    pd.line([(lit[0], (lit[1] + lit[3]) // 2), (lit[2], (lit[1] + lit[3]) // 2)], fill=(60, 40, 18))

    # Slight lens vignette.
    vignette = Image.new("L", (photo_w, photo_h), 0)
    ImageDraw.Draw(vignette).ellipse([-30, -26, photo_w + 30, photo_h + 26], fill=255)
    vignette = vignette.filter(ImageFilter.GaussianBlur(14))
    photo = Image.composite(photo, Image.new("RGB", (photo_w, photo_h), (4, 4, 8)), vignette)

    img.paste(photo, (left, top))
    d = ImageDraw.Draw(img)
    # A faint shadow line under the print, like the edge of the emulsion.
    d.line([(left, bottom + 1), (right, bottom + 1)], fill=(200, 196, 184))
    return img


def write_snapshot():
    art = build_snapshot()
    ids, preview = quantize(art)
    out = os.path.join(RES, "data", "the_oldest_house", "snapshot")
    os.makedirs(out, exist_ok=True)
    with open(os.path.join(out, "porch.bin"), "wb") as f:
        f.write(bytes(ids))
    preview.resize((512, 512), Image.NEAREST).save(os.path.join(ROOT, "tools", "snapshot_preview.png"))
    assert len(ids) == 128 * 128 and min(ids) >= 4


# ---------------------------------------------------------------------------
# Entrance door textures


DARK = (46, 30, 15)
MID = (62, 41, 21)
LIGHT = (76, 52, 28)
EDGE = (34, 22, 11)
SHADOW = (28, 18, 9)


def plank_noise(rnd, base):
    j = rnd.randrange(-4, 5)
    return tuple(max(0, min(255, c + j)) for c in base)


def door_half(rnd, top):
    img = Image.new("RGBA", (16, 16), MID + (255,))
    for y in range(16):
        for x in range(16):
            base = LIGHT if x % 4 == 1 else MID
            img.putpixel((x, y), plank_noise(rnd, base) + (255,))
        for x in (3, 7, 11):
            img.putpixel((x, y), EDGE + (255,))
    # Stiles and rails: a plain frame with a single recessed panel per half.
    for y in range(16):
        img.putpixel((0, y), SHADOW + (255,))
        img.putpixel((15, y), SHADOW + (255,))
    rail_y = [0, 1] if top else [14, 15]
    for y in rail_y:
        for x in range(16):
            img.putpixel((x, y), DARK + (255,))
    panel = (3, 3, 12, 13) if top else (3, 2, 12, 12)
    for x in range(panel[0], panel[2] + 1):
        img.putpixel((x, panel[1]), SHADOW + (255,))
    for y in range(panel[1], panel[3] + 1):
        img.putpixel((panel[0], y), SHADOW + (255,))
    for x in range(panel[0], panel[2] + 1):
        img.putpixel((x, panel[3]), LIGHT + (255,))
    for y in range(panel[1], panel[3] + 1):
        img.putpixel((panel[2], y), LIGHT + (255,))
    if not top:
        # Plain iron knob and a keyhole below it.
        img.putpixel((13, 1), (58, 58, 60, 255))
        img.putpixel((13, 2), (40, 40, 42, 255))
    return img


def door_item(rnd):
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    for y in range(16):
        for x in range(4, 12):
            base = LIGHT if x % 3 == 0 else MID
            img.putpixel((x, y), plank_noise(rnd, base) + (255,))
    for y in range(16):
        img.putpixel((4, y), SHADOW + (255,))
        img.putpixel((11, y), SHADOW + (255,))
    for x in range(4, 12):
        img.putpixel((x, 0), SHADOW + (255,))
        img.putpixel((x, 15), SHADOW + (255,))
        img.putpixel((x, 8), DARK + (255,))
    for (x0, y0, x1, y1) in ((6, 2, 9, 6), (6, 10, 9, 13)):
        for x in range(x0, x1 + 1):
            img.putpixel((x, y0), SHADOW + (255,))
            img.putpixel((x, y1), LIGHT + (255,))
        for y in range(y0, y1 + 1):
            img.putpixel((x0, y), SHADOW + (255,))
            img.putpixel((x1, y), LIGHT + (255,))
    img.putpixel((10, 9), (58, 58, 60, 255))
    return img


def write_door_textures():
    rnd = random.Random(1990)
    block_dir = os.path.join(RES, "assets", "the_oldest_house", "textures", "block")
    item_dir = os.path.join(RES, "assets", "the_oldest_house", "textures", "item")
    os.makedirs(block_dir, exist_ok=True)
    os.makedirs(item_dir, exist_ok=True)
    door_half(rnd, True).save(os.path.join(block_dir, "entrance_door_top.png"))
    door_half(rnd, False).save(os.path.join(block_dir, "entrance_door_bottom.png"))
    door_item(rnd).save(os.path.join(item_dir, "entrance_door.png"))


if __name__ == "__main__":
    write_snapshot()
    write_door_textures()
    print("wrote snapshot and door textures")
