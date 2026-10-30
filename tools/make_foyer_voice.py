"""The foyer intercom's voice source (0.4.199): speech from the Piper neural voice en_US-ljspeech-high.

The owner: "for the home-entrance opening vignette, can you do anything to make the voice actually sound human?" The babble
had been summed sine tones. This writes art/foyer_voice_source.wav from the lines below; tools/generate_foyer_audio.py then
puts it through the intercom (band-limited, saturated, humming, breaking up), so a word or two carries and the rest does not.

The voice: Piper (https://github.com/rhasspy/piper) with the ljspeech high model from rhasspy/piper-voices, trained on the LJ
Speech dataset (public domain). Run once with `pip install piper-tts` and the model downloaded beside it; the build only
reads the saved source, so CI needs neither.
"""
import argparse
import subprocess
from pathlib import Path

ROOT=Path(__file__).resolve().parents[1]
SOURCE=ROOT/'art/foyer_voice_source.wav'
# Whatever stands at the step wants to be let in; it has learned what a person says.
LINES="Hello? ... Hello. It's me. I'm just outside, at the bottom. Can you... press it? Please. It's cold out here. I can see you."

def main():
    p=argparse.ArgumentParser(description=__doc__);p.add_argument('--model',required=True,help='path to en_US-ljspeech-high.onnx');p.add_argument('--piper',default='piper')
    args=p.parse_args()
    subprocess.run([args.piper,'-m',args.model,'--length-scale','1.15','--noise-scale','0.75','--noise-w-scale','0.9','--sentence-silence','0.35','-f',str(SOURCE)],input=LINES.encode(),check=True)
    print('Wrote',SOURCE)

if __name__=='__main__':main()
