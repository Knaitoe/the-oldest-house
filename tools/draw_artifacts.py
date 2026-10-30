#!/usr/bin/env python3
"""The twenty artifacts of the audit's build 7 (labyrinth/Artifacts.java, docs/ARTIFACTS_0_4_199.md), 16x16, in vanilla's item style.

Each icon is drawn pixel by pixel from a grid below, one character a pixel, with its own small palette; '.' is clear.
Run with python3 from the repository root (--check compares instead of writing). Writes
assets/the_oldest_house/textures/item/<id>.png and its item model (the blade is held like a sword). No other file is touched.
"""
import json
import sys
from pathlib import Path
from PIL import Image

ROOT = Path('src/main/resources/assets/the_oldest_house')
TEXTURES = ROOT / 'textures/item'
MODELS = ROOT / 'models/item'


def rgba(h):
    h = h.lstrip('#')
    return tuple(int(h[i:i + 2], 16) for i in (0, 2, 4)) + ((int(h[6:8], 16),) if len(h) == 8 else (255,))


ART = {
    # Flora's doll (Bly): a rag doll in a faded dress and pinafore, button eyes.
    'flora_doll': ({'o': '#3a2a20', 'h': '#6b4a2e', 'H': '#8a6440', 's': '#ead2b0', 'S': '#cfb08c', 'e': '#231a14', 'd': '#7f8fa0', 'D': '#5d6c7c', 'w': '#ece6d6', 'W': '#cfc7b3', 'b': '#2e2219'}, [
        '................',
        '......oooo......',
        '.....ohHHho.....',
        '....ohhsshho....',
        '....ohseseho....',
        '....ohssssho....',
        '.....oSssSo.....',
        '....oodddoo.....',
        '...osdwwwwdso...',
        '...osdwWWwdso...',
        '....odwwwwdo....',
        '....oddddddo....',
        '...oDDddddDDo...',
        '....ooosoooo....',
        '.....os..so.....',
        '.....bb..bb.....']),
    # A hunter's tag (elk carcasses): a buff tag on a loop of wire, cut with a strip of hide.
    'hunter_tag': ({'o': '#3d2c1c', 't': '#d9b77a', 'T': '#b8955a', 'k': '#4a3a2a', 'g': '#9a9a9a', 'h': '#6e4b2e', 'H': '#8f6845', 'f': '#a8845c'}, [
        '................',
        '..........gg....',
        '.........g..g...',
        '.........g..g...',
        '......ooooggoo..',
        '.....otttttttto.',
        '....otttktttTto.',
        '...otttttttttTo.',
        '...otkkkktkktTo.',
        '...otttttttttTo.',
        '...otkktkkkttTo.',
        '...otttttttttTo.',
        '...oTTTTTTTTTTo.',
        '...oohhHhhHhhoo.',
        '....ohfhHfhfho..',
        '.....oooooooo...']),
    # Their number card (Costume Night): a pinned card with a large painted number.
    'number_card': ({'o': '#2b2622', 'c': '#efe9dc', 'C': '#d3cbb8', 'n': '#1f1b18', 'p': '#b9b9be', 'P': '#8d8d93'}, [
        '................',
        '.....pPPPPp.....',
        '..oooooooooooo..',
        '..occcccccccCo..',
        '..ocnnccnnnncCo.',
        '..occnccccnccCo.',
        '..occnccnnnccCo.',
        '..occnccnccccCo.',
        '..ocnnnnnnnncCo.',
        '..occcccccccCo..',
        '..occcccccccCo..',
        '..oCCCCCCCCCCo..',
        '..oooooooooooo..',
        '................',
        '................',
        '................']),
    # A ticket stub (Movie Night): a torn admit-one stub, perforated along one side.
    'ticket_stub': ({'o': '#4a1c16', 'r': '#c8553f', 'R': '#a3402f', 'l': '#f0d9b5', 'k': '#5a2a20'}, [
        '................',
        '................',
        '................',
        '..oooooooooooo..',
        '..orrrrrrrrr.o..',
        '..orllllllrR.o..',
        '..orlkkkklrRo...',
        '..orllllllrR.o..',
        '..orlkklklrRo...',
        '..orllllllrR.o..',
        '..orrrrrrrrR.o..',
        '..oRRRRRRRRRo...',
        '..oooooooooooo..',
        '................',
        '................',
        '................']),
    # Bud's note (Winter Lake): a folded sheet of ruled paper, a pencilled line and four signatures.
    'bud_note': ({'o': '#5b5246', 'p': '#f1ece0', 'P': '#d6cfbe', 'l': '#a9bfd0', 'g': '#4b4640'}, [
        '................',
        '...ooooooooooo..',
        '...opppppppppPo.',
        '...oplllllllpPo.',
        '...opgggggglpPo.',
        '...oplllllllpPo.',
        '...opggglgglpPo.',
        '...oplllllllpPo.',
        '...oPPPPPPPPPPo.',
        '...opgglpgglpPo.',
        '...oplllllllpPo.',
        '...opglgplgglPo.',
        '...oplllllllpPo.',
        '...oPPPPPPPPPPo.',
        '...ooooooooooo..',
        '................']),
    # A counsellor's whistle (Camp Blood): a steel whistle on a lanyard.
    'counsellor_whistle': ({'o': '#2e3236', 's': '#c9ced3', 'S': '#8e959c', 'w': '#f4f6f8', 'k': '#3b4045', 'c': '#b8403a', 'C': '#8a2c28'}, [
        '................',
        '.....cc.........',
        '....c..c........',
        '...c....c.......',
        '...c.....c......',
        '..C......c......',
        '..C.......c.....',
        '..C.......c.....',
        '...C.....ooooo..',
        '....C...oswsSSo.',
        '.....C.oswssSSo.',
        '......oosssSSSo.',
        '......osssSSSSo.',
        '......okkSSSSoo.',
        '.......ooooooo..',
        '................']),
    # A page of a student essay (Proofrock): a ruled school page, margin line, water-stained.
    'essay_page': ({'o': '#5e574c', 'p': '#f2eee2', 'P': '#d9d2bf', 'l': '#9db6cc', 'm': '#c9706a', 'g': '#47423c', 'w': '#cdd6d4'}, [
        '...ooooooooooo..',
        '...opmpppppppPo.',
        '...opmlllllllPo.',
        '...opmgggglggPo.',
        '...opmlllllllPo.',
        '...opmggglgggPo.',
        '...opmlllllllPo.',
        '...opmgglggggPo.',
        '...owmllwllllPo.',
        '...owwgwwgglgPo.',
        '...opwlwwllllPo.',
        '...opmggglgggPo.',
        '...opmlllllllPo.',
        '...oPPPPPPPPPPo.',
        '...ooooooooooo..',
        '................']),
    # The scrawled note (buzzer): a crumpled scrap with a heavy scrawl.
    'scrawled_note': ({'o': '#4a443c', 'p': '#ece5d3', 'P': '#c9c0aa', 'k': '#1c1a18'}, [
        '................',
        '................',
        '....oooo.ooo....',
        '...oppppoppPo...',
        '..oppkkppppPPo..',
        '..opkppkkpkkPo..',
        '.oppppppkpppPo..',
        '.opkkkpppkkpPPo.',
        '..oppkkkpppppPo.',
        '..opppppkkkpPo..',
        '.oppkkpppppkPo..',
        '.oPppkkkkpppPo..',
        '..oPPPpppPPPo...',
        '...ooooPPooo....',
        '.......oo.......',
        '................']),
    # The worker's broken blade (basement): a short snapped blade on a worn handle, held like a sword.
    'broken_blade': ({'o': '#252629', 's': '#cfd3d7', 'S': '#8f959b', 'e': '#f2f4f5', 'g': '#5a4f45', 'h': '#6d4a2c', 'H': '#8c6440', 'b': '#3e2b1a'}, [
        '................',
        '................',
        '................',
        '................',
        '.........oo.....',
        '........oeso....',
        '.......oesSo....',
        '......oesSo.....',
        '.....oesSo......',
        '..o.oesSo.......',
        '..ogoeSo........',
        '...oggo.........',
        '...ohgo.........',
        '..ohHo..........',
        '.obho...........',
        '.oo.............']),
    # The receipt (diner): a long till slip, printed lines and a torn foot.
    'diner_receipt': ({'o': '#5a554d', 'p': '#f4f1e8', 'P': '#d8d2c2', 'g': '#6d6860'}, [
        '.....oooooo.....',
        '.....oppppPo....',
        '.....opggpPo....',
        '.....oppppPo....',
        '.....ogpgpPo....',
        '.....opgppPo....',
        '.....oppppPo....',
        '.....ogggpPo....',
        '.....oppppPo....',
        '.....opgpgPo....',
        '.....oppppPo....',
        '.....ogggpPo....',
        '.....oppppPo....',
        '.....opPpPPo....',
        '.....o.o.o.o....',
        '................']),
    # The old man's watch (Tell-Tale): a brass pocket watch half wrapped in cotton, its chain trailing.
    'old_watch': ({'o': '#3d2f17', 'b': '#c9a14a', 'B': '#9c7a30', 'f': '#f4efe0', 'k': '#2a2420', 'c': '#ffffff', 'C': '#dedbd2', 'y': '#a88a40'}, [
        '.......oo.......',
        '......obbo......',
        '.......oo.......',
        '.....oooooo.....',
        '....obbbbbBo....',
        '...obffffffBo...',
        '...obfffkffBo...',
        '...obffkfffBo...',
        '...obfkkfffBo...',
        '..ccobffffBBocc.',
        '.cccCobbbBBoCccc',
        '.cCccCooooCcCcc.',
        '..cCcccCcccCcc..',
        '...ccCcccCcy....',
        '.....cccc..y.y..',
        '.............y..']),
    # The folded blindfold (hide-and-clap): the game's cloth folded in a neat square, its ties hanging.
    'folded_blindfold': ({'o': '#1e1d24', 'c': '#4c4a5c', 'C': '#3a3848', 'h': '#66637a', 'd': '#2c2a36', 't': '#3a3848'}, [
        '................',
        '................',
        '................',
        '...oooooooooo...',
        '..ochhhhhhhhCo..',
        '..ocCCCCCCCCCo..',
        '..ocdddddddcCo..',
        '..ocCCCCCCCCCo..',
        '..ochhhhhhhhCo..',
        '..ocCCCCCCCCCo..',
        '..oCCCCCCCCCCo..',
        '...oooootoooo...',
        '........t.......',
        '.......t.t......',
        '......t...t.....',
        '................']),
    # The invitation (Masque): a card with a gilt border and a dark wax seal.
    'masque_invitation': ({'o': '#2c2420', 'p': '#f1e8d2', 'P': '#d6c9a8', 'g': '#c4a04a', 'G': '#8f7230', 'w': '#5a1a26', 'W': '#3d1019', 'k': '#4a403a'}, [
        '................',
        '................',
        '.oooooooooooooo.',
        '.oggggggggggggo.',
        '.ogpppppppppPgo.',
        '.ogpkkpkkkppPgo.',
        '.ogpppppppppPgo.',
        '.ogppkkkkkppPgo.',
        '.ogpppwwwpppPgo.',
        '.ogppwWwWwppPgo.',
        '.ogpppwwwpppPgo.',
        '.ogPPPPPPPPPPgo.',
        '.oGGGGGGGGGGGGo.',
        '.oooooooooooooo.',
        '................',
        '................']),
    # The sitting record (séance): a slim bound notebook, a paper label on its dark cover.
    'sitting_record': ({'o': '#1c1612', 'c': '#4a3226', 'C': '#352218', 'l': '#e7dcc4', 'k': '#5a4a3a', 'p': '#efe7d4', 's': '#2a1c14'}, [
        '................',
        '...ooooooooooo..',
        '...oscccccccCo..',
        '...oscccccccCop.',
        '...osclllllcCop.',
        '...osclkkklcCop.',
        '...osclllllcCop.',
        '...osclkklccCop.',
        '...osclllllcCop.',
        '...oscccccccCop.',
        '...oscccccccCop.',
        '...oscccccccCop.',
        '...oCCCCCCCCCop.',
        '...ooooooooooopp',
        '....ppppppppppp.',
        '................']),
    # The room's key tag (Stanley): a diamond fob with a stamped number on a brass ring.
    'key_tag': ({'o': '#2a1414', 'r': '#7a2a2a', 'R': '#5a1c1c', 'h': '#9c4040', 'n': '#e8d6a8', 'b': '#c9a14a', 'B': '#8f7230'}, [
        '................',
        '.....bbbb.......',
        '....b....b......',
        '....b....b......',
        '.....bBBb.......',
        '.......o........',
        '......oho.......',
        '.....ohrro......',
        '....ohrrrRo.....',
        '...ohrnnnrRo....',
        '...orrnrnrRo....',
        '....orrrrRo.....',
        '.....orrRo......',
        '......oRo.......',
        '.......o........',
        '................']),
    # The family's photograph (family copy): an old print with a white border, the house and its people.
    'family_photograph': ({'o': '#3a3026', 'w': '#efe8d8', 'W': '#d3c9b2', 's': '#c9b08a', 'S': '#a88c62', 'k': '#5a4632', 'K': '#3e3022', 'y': '#e4d2a8'}, [
        '................',
        '................',
        '.oooooooooooooo.',
        '.owwwwwwwwwwwWo.',
        '.owyyyyyyyyyyWo.',
        '.owyySSSSSyyyWo.',
        '.owySSkKSSSyyWo.',
        '.owSSSkKkSSSyWo.',
        '.owsssKkKssssWo.',
        '.owskksskkssWWo.',
        '.owsKKssKKsssWo.',
        '.owSSSSSSSSSSWo.',
        '.owwwwwwwwwwwWo.',
        '.oWWWWWWWWWWWWo.',
        '.oooooooooooooo.',
        '................']),
    # A stone with the initials (barn well): a field stone with two letters cut into it.
    'initial_stone': ({'o': '#2c2c2e', 's': '#8e8d8a', 'S': '#6c6b68', 'h': '#aeada9', 'k': '#3c3b39'}, [
        '................',
        '................',
        '................',
        '.....oooooo.....',
        '...oohhhhhsoo...',
        '..ohhssssssSSo..',
        '..ohsksskssSSo..',
        '.ohssksskkssSSo.',
        '.ohskksskskssSo.',
        '.ohsskssksssSSo.',
        '.oSssssssssSSSo.',
        '..oSSssssSSSSo..',
        '...ooSSSSSSoo...',
        '.....oooooo.....',
        '................',
        '................']),
    # A patient's bracelet (hospital): a looped plastic band with its printed label.
    'patient_bracelet': ({'o': '#5a6066', 'w': '#f2f4f5', 'W': '#cdd3d8', 'l': '#ffffff', 'k': '#4a4f55', 'b': '#9fb6c8'}, [
        '................',
        '................',
        '.....oooooo.....',
        '...oowwwwwwoo...',
        '..owwWoooooWwo..',
        '..owWo.....oWo..',
        '.owWo.......oWo.',
        '.owo.........oo.',
        '.oboooooooooobo.',
        '.oblllllllllbbo.',
        '.oblkkklkklbbo..',
        '.oblllllllllbo..',
        '..oooooooooooo..',
        '................',
        '................',
        '................']),
    # The stub of the dare's candle (third-floor bathroom): a short school candle, its wick black, wax run down one side.
    'dare_candle': ({'o': '#4a4238', 'c': '#f0e9d8', 'C': '#d2c8b0', 'k': '#1c1a18', 'd': '#e2d8c0', 'h': '#fffaf0'}, [
        '................',
        '................',
        '................',
        '................',
        '.......k........',
        '.......k........',
        '.....oooooo.....',
        '....ochccccCo...',
        '....ochcccCCo...',
        '....odhcccCCo...',
        '....odccccCCo...',
        '....odccdcCCo...',
        '...oodccdcCCoo..',
        '...oCdCCdCCCCo..',
        '....oooooooooo..',
        '................']),
    # A vial of the dust (silo): a corked glass vial, the dust settled in it.
    'dust_vial': ({'o': '#2e3034', 'g': '#d8e6ea', 'G': '#a9bcc2', 'c': '#8a6440', 'C': '#6a4a2e', 'y': '#d9bb52', 'Y': '#b2943a', 'h': '#ffffff'}, [
        '................',
        '......oooo......',
        '......occo......',
        '......oCCo......',
        '.....oggggo.....',
        '.....ohgggo.....',
        '....ohgggGGo....',
        '....oggggGGo....',
        '....ogyyyyGo....',
        '....ohyyyYGo....',
        '....oyyyYYYo....',
        '....oyyYYYYo....',
        '....oYYYYYYo....',
        '.....oooooo.....',
        '................',
        '................']),
}
HELD = {'broken_blade'}


def draw(name):
    palette, rows = ART[name]
    assert len(rows) == 16 and all(len(r) == 16 for r in rows), name + ': ' + str([len(r) for r in rows])
    img = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    for y, row in enumerate(rows):
        for x, ch in enumerate(row):
            if ch != '.':
                img.putpixel((x, y), rgba(palette[ch]))
    return img


def model(name):
    return {'parent': 'minecraft:item/handheld' if name in HELD else 'minecraft:item/generated', 'textures': {'layer0': 'the_oldest_house:item/' + name}}


def main():
    check = '--check' in sys.argv
    bad = []
    for name in ART:
        img = draw(name)
        png, js = TEXTURES / (name + '.png'), MODELS / (name + '.json')
        text = json.dumps(model(name), indent=2) + '\n'
        if check:
            if not png.exists() or Image.open(png).convert('RGBA').tobytes() != img.tobytes():
                bad.append(str(png))
            if not js.exists() or js.read_text() != text:
                bad.append(str(js))
        else:
            img.save(png)
            js.write_text(text)
    if bad:
        print('out of date:', *bad, sep='\n  ')
        sys.exit(1)
    print(('checked ' if check else 'wrote ') + str(len(ART)) + ' artifacts')


if __name__ == '__main__':
    main()
