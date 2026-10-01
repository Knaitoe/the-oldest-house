#!/usr/bin/env python3
"""Exact-UV pixel materials and original mono synthesis for Indian Lake. No downloaded assets."""
from pathlib import Path
import json
import random
import subprocess
import tempfile
import wave
import numpy as np
from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / "src/main/resources/assets/the_oldest_house"
DATA = ROOT / "src/main/resources/data/the_oldest_house"
RNG = random.Random(411)


def patch(image, box, base, grain=8):
    for y in range(box[1], box[3]):
        for x in range(box[0], box[2]):
            shift = RNG.randint(-grain, grain)
            image.putpixel((x, y), tuple(max(0, min(255, c + shift)) for c in base) + (255,))


def textures():
    folder = ASSETS / "textures/entity"
    folder.mkdir(parents=True, exist_ok=True)
    witch = Image.new("RGBA", (128, 128), (37, 46, 43, 255))
    patch(witch, (0, 0, 28, 30), (142, 155, 138), 11)
    patch(witch, (32, 0, 60, 32), (132, 149, 133), 12)
    patch(witch, (64, 0, 94, 64), (25, 37, 33), 7)
    patch(witch, (96, 0, 128, 24), (30, 37, 32), 4)
    patch(witch, (0, 48, 44, 72), (73, 88, 76), 10)
    patch(witch, (0, 80, 44, 112), (57, 74, 61), 12)
    draw = ImageDraw.Draw(witch)
    # Front head UV: bruised brow and a recessed mouth, with uneven green lake staining.
    draw.line((7, 10, 10, 10), fill=(48, 58, 49, 255))
    draw.line((13, 10, 16, 10), fill=(50, 61, 51, 255))
    draw.line((9, 13, 14, 13), fill=(45, 48, 42, 255))
    for x in range(66, 92, 3): draw.line((x, 1, x - 1, 60), fill=(40, 54, 44, 255))
    for x in range(3, 42, 7): draw.line((x, 51, x + 1, 68), fill=(45, 63, 50, 255))
    for x in range(2, 41, 6): draw.line((x, 84, x - 1, 107), fill=(29, 47, 38, 255))
    for x, y in [(35, 7), (41, 15), (49, 5)]: draw.line((x, y, x + 3, y + 11), fill=(90, 121, 102, 255))
    witch.save(folder / "lake_witch.png")

    for name, robe in [("lake_congregant", (63, 78, 74)), ("lake_preacher", (35, 39, 42))]:
        skin = Image.new("RGBA", (64, 64), (0, 0, 0, 0))
        patch(skin, (0, 0, 32, 16), (149, 163, 150), 6)
        patch(skin, (0, 16, 64, 32), robe, 7)
        patch(skin, (0, 32, 64, 64), robe, 7)
        d = ImageDraw.Draw(skin)
        d.rectangle((8, 8, 15, 9), fill=(44, 56, 49, 255))
        d.rectangle((9, 11, 10, 11), fill=(63, 77, 68, 255))
        d.rectangle((13, 11, 14, 11), fill=(63, 77, 68, 255))
        d.line((10, 14, 13, 14), fill=(93, 109, 95, 255))
        d.rectangle((20, 20, 27, 30), fill=robe + (255,))
        if name == "lake_preacher": d.rectangle((22, 20, 25, 21), fill=(208, 208, 184, 255))
        skin.save(folder / f"{name}.png")

    items = ASSETS / "textures/item"
    models = ASSETS / "models/item"
    recipes = DATA / "recipe"
    for directory in (items, models, recipes): directory.mkdir(parents=True, exist_ok=True)
    for index, number in enumerate(("one", "two", "three")):
        for wet in (True, False):
            name = ("waterlogged" if wet else "dried") + f"_essay_{number}"
            icon = Image.new("RGBA", (32, 32), (0, 0, 0, 0)); d = ImageDraw.Draw(icon)
            edge = (54, 85, 81, 255) if wet else (114, 89, 55, 255)
            paper = (126, 152, 137, 255) if wet else (211, 193, 145, 255)
            d.polygon([(7, 3), (22, 4), (25, 8), (23, 27), (17, 28), (8, 26), (5, 14)], fill=edge)
            d.polygon([(8, 5), (21, 6), (22, 10), (21, 25), (9, 24), (7, 13)], fill=paper)
            for y in range(10, 23, 3):
                d.line((10, y, 19 - ((y + index) % 3), y), fill=(57, 72, 64, 180) if wet else (74, 65, 52, 255))
            for k in range(index + 1): d.line((11 + 3 * k, 7, 11 + 3 * k, 8), fill=(142, 63, 54, 255))
            if wet:
                d.line((7, 18, 8, 24, 18, 27), fill=(55, 105, 100, 220), width=2)
                d.rectangle((24, 17, 25, 20), fill=(82, 151, 167, 255))
            icon.save(items / f"{name}.png")
            (models / f"{name}.json").write_text(json.dumps({"parent": "minecraft:item/generated", "textures": {"layer0": f"the_oldest_house:item/{name}"}}, indent=2) + "\n")
        recipe = {"type": "minecraft:smelting", "category": "misc", "ingredient": {"item": f"the_oldest_house:waterlogged_essay_{number}"},
                  "result": {"id": f"the_oldest_house:dried_essay_{number}", "count": 1}, "experience": 0.1, "cookingtime": 200}
        (recipes / f"dry_essay_{number}.json").write_text(json.dumps(recipe, indent=2) + "\n")
    key = Image.new("RGBA", (32, 32), (0, 0, 0, 0)); d = ImageDraw.Draw(key)
    d.ellipse((6, 3, 18, 15), fill=(87, 77, 53, 255)); d.ellipse((8, 5, 16, 13), fill=(186, 153, 79, 255));
    d.ellipse((10, 7, 14, 11), fill=(0, 0, 0, 0)); d.line((15, 13, 24, 23), fill=(81, 72, 49, 255), width=5)
    d.line((15, 13, 24, 23), fill=(171, 140, 71, 255), width=3)
    d.polygon([(19, 20), (18, 22), (21, 25), (23, 24), (24, 26), (27, 23), (24, 21)], fill=(153, 125, 65, 255))
    key.save(items / "church_key.png")
    (models / "church_key.json").write_text(json.dumps({"parent": "minecraft:item/generated", "textures": {"layer0": "the_oldest_house:item/church_key"}}, indent=2) + "\n")


def ogg(name, samples):
    rate = 24000
    target = ASSETS / "sounds/indian_lake" / f"{name}.ogg"
    target.parent.mkdir(parents=True, exist_ok=True)
    samples = np.clip(samples, -0.95, 0.95)
    with tempfile.TemporaryDirectory() as temporary:
        wav = Path(temporary) / "source.wav"
        with wave.open(str(wav), "wb") as writer:
            writer.setnchannels(1); writer.setsampwidth(2); writer.setframerate(rate)
            writer.writeframes((samples * 32767).astype("<i2").tobytes())
        subprocess.run(["ffmpeg", "-v", "error", "-y", "-i", str(wav), "-ac", "1", "-ar", str(rate), "-c:a", "libvorbis", "-q:a", "4", str(target)], check=True)


def audio():
    random = np.random.default_rng(411)
    t = np.arange(int(7.7 * 24000)) / 24000
    choir = np.zeros_like(t)
    # Wordless, original four-part minor harmony, spread by small pitch/breath differences.
    for frequency in (130.8128, 196, 233.0819, 261.6256):
        for voice in range(3):
            phase = 2 * np.pi * frequency * (1 + (voice - 1) * .0016) * t + .045 * np.sin(2 * np.pi * 4.6 * t + voice)
            breath = .75 + .18 * np.sin(2 * np.pi * .16 * t + voice)
            for harmonic in range(1, 8):
                weight = np.exp(-((harmonic * frequency - 650) / 520) ** 2) / harmonic
                choir += np.sin(phase * harmonic + voice) * weight * breath / 30
    envelope = np.minimum(1, t / .65) * np.minimum(1, (7.7 - t) / .85)
    ogg("hymn", choir * envelope)
    t = np.arange(int(1.5 * 24000)) / 24000
    rough = np.sin(2 * np.pi * (68 * t - 8 * t * t)) * .17 + np.sin(2 * np.pi * 211 * t + np.sin(t * 70)) * .11
    rough += np.convolve(random.normal(0, .16, len(t)), np.ones(7) / 7, mode="same")
    ogg("witch", rough * np.sin(np.pi * t / 1.5) ** .7)
    t = np.arange(int(3.5 * 24000)) / 24000
    water = np.convolve(random.normal(0, .26, len(t)), np.ones(19) / 19, mode="same")
    water *= .25 + .35 * np.sin(t * 2.8) ** 2
    for start in (.35, 1.4, 2.5):
        dt = np.maximum(0, t - start)
        water += np.where(t >= start, np.sin(2 * np.pi * (480 * dt - 200 * dt * dt)) * np.exp(-dt * 24) * .13, 0)
    water += .025 * np.sin(2 * np.pi * 72 * t) * np.sin(t * .85) ** 2
    ogg("door", water * np.minimum(1, t / .2) * np.minimum(1, (3.5 - t) / .4))
    path = ASSETS / "sounds.json"; sounds = json.loads(path.read_text())
    for id, file, distance, subtitle in [("vignette.lake_hymn", "hymn", 48, "lake_hymn"),
                                         ("vignette.lake_witch", "witch", 28, "lake_witch"),
                                         ("leak.indian_lake", "door", 18, "indian_lake")]:
        sounds[id] = {"sounds": [{"name": f"the_oldest_house:indian_lake/{file}", "attenuation_distance": distance}],
                      "subtitle": f"subtitles.the_oldest_house.{subtitle}"}
    path.write_text(json.dumps(sounds, indent=2) + "\n")


if __name__ == "__main__":
    textures(); audio()
    print("Indian Lake: exact-UV witch/congregation textures, seven item icons, three native recipes, three original mono sounds.")
