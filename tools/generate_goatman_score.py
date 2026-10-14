"""Around the Tin Walls: an original, sparse 96-second loop and close trailer impacts.

No samples from outside this project. The score uses synthesized bowed fifths and
felt-piano fragments, with no rhythmic knocks that could hide the real stalker.
Existing internal impact recordings get separate compressed masters for this scene.
"""
from pathlib import Path
import json
import subprocess
import tempfile
import wave
import numpy as np

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / "src/main/resources/assets/the_oldest_house"
RATE, LENGTH = 24000, 96


def encode(source, target):
    subprocess.run(["ffmpeg", "-nostdin", "-v", "error", "-y", "-i", str(source),
                    "-ac", "1", "-ar", str(RATE), "-c:a", "libvorbis", "-q:a", "5", str(target)], check=True)


def score():
    t = np.arange(RATE * LENGTH, dtype=np.float64) / RATE
    out = np.zeros_like(t)
    # E/B, C/G, F/C and E/B: a slowly turning, unresolved room tone.
    chords = ((82.4069, 123.4708), (65.4064, 97.9989), (87.3071, 130.8128), (82.4069, 123.4708))
    for section, chord in enumerate(chords):
        distance = (t - (section * 24 + 12) + LENGTH / 2) % LENGTH - LENGTH / 2
        envelope = np.where(abs(distance) < 20, np.cos(np.pi * distance / 40) ** 2, 0)
        for voice, pitch in enumerate(chord):
            # Integer loop cycles and periodic modulation make the join continuous.
            pitch = round(pitch * LENGTH) / LENGTH
            phase = 2 * np.pi * pitch * t + .018 * np.sin(2 * np.pi * t / 12 + voice)
            breath = .82 + .12 * np.sin(2 * np.pi * t / 16 + voice)
            out += .075 * envelope * breath * (np.sin(phase) + .11 * np.sin(2 * phase))
    # A small original four-note question; the second phrase leaves its answer hanging.
    notes = ((5, 329.6276), (13.5, 246.9417), (29, 349.2282), (40, 233.0819),
             (54, 329.6276), (63, 246.9417), (77, 293.6648), (86, 233.0819))
    for start, pitch in notes:
        for delay, gain in ((0, 1), (.83, .20), (1.71, .09)):
            age = (t - start - delay) % LENGTH
            envelope = np.minimum(1, age / .035) * np.exp(-age * .73)
            tone = (np.sin(2 * np.pi * pitch * age) + .19 * np.sin(2 * np.pi * pitch * 2.003 * age))
            out += .047 * gain * envelope * tone
    # A gentle, periodic air layer rather than a drum, heartbeat or false footstep.
    out += .007 * np.sin(2 * np.pi * 31 * t) * (.5 + .5 * np.sin(2 * np.pi * t / 32))
    return out


def main():
    folder = ASSETS / "sounds/goatman"
    folder.mkdir(parents=True, exist_ok=True)
    with tempfile.TemporaryDirectory(prefix="oth-goatman-score-") as temporary:
        wav = Path(temporary) / "score.wav"
        samples = score()
        with wave.open(str(wav), "wb") as output:
            output.setnchannels(1); output.setsampwidth(2); output.setframerate(RATE)
            output.writeframes((np.clip(samples, -.95, .95) * 32767).astype("<i2").tobytes())
        encode(wav, folder / "around_the_tin_walls.ogg")
    masters = (("knock", ASSETS / "sounds/literary/cabin_knock.ogg"),
               ("claw", folder / "claw.ogg"), ("hammer", folder / "hammer.ogg"))
    for name, source in masters:
        subprocess.run(["ffmpeg", "-nostdin", "-v", "error", "-y", "-i", str(source),
                        "-af", f"volume=6,asoftclip=type=tanh:threshold=0.94:output={0.68 if name == 'claw' else 0.94}",
                        "-ac", "1", "-ar", str(RATE), "-c:a", "libvorbis", "-q:a", "5", str(folder / f"{name}_vigil.ogg")], check=True)
    path = ASSETS / "sounds.json"
    sounds = json.loads(path.read_text())
    for name in ("knock", "claw", "hammer"):
        sounds[f"goatman.{name}"] = {"sounds": [{"name": f"the_oldest_house:goatman/{name}_vigil", "attenuation_distance": 48}],
                                     "subtitle": f"subtitles.the_oldest_house.goatman_{name}"}
    sounds["goatman.score"] = {"sounds": [{"name": "the_oldest_house:goatman/around_the_tin_walls", "stream": True}],
                                "subtitle": "subtitles.the_oldest_house.goatman_score"}
    path.write_text(json.dumps(sounds, indent=2) + "\n")
    path = ASSETS / "lang/en_us.json"
    language = json.loads(path.read_text()); language["subtitles.the_oldest_house.goatman_knock"] = "Someone knocks on the trailer"
    language["subtitles.the_oldest_house.goatman_score"] = "Quiet music in the trailer"
    path.write_text(json.dumps(language, indent=2, ensure_ascii=False) + "\n")
    print(f"Original {LENGTH}-second mono score and three close impact masters generated.")


if __name__ == "__main__":
    main()
