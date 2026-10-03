"""Generate the approved prose and conservative bitmap advances from checked-in sources."""
import json
import math
import re
from pathlib import Path
from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT / "docs/CORRESPONDENCE_0_4_31.md"
DEST = ROOT / "src/main/resources/data/the_oldest_house/correspondence"
AUTHORS = dict(zip("ABCDEFGHIJKLMN", ["Iris", "Mae", "Noel", "Ansel", "Ruth", "D. V.",
    "Paula", "June", "An unnamed host", "An admirer", "Unsigned", "Ledger copy", "Review office", "A correspondent"]))
STYLES = dict(zip("ABCDEFGHIJKLMN", ["KAREN", "PELAFINA", "WILL", "ZAMPANO", "KAREN", "ZAMPANO",
    "WILL", "KAREN", "KAREN", "WILL", "WILL", "ZAMPANO", "ZAMPANO", "PELAFINA"]))
SURFACES = dict(zip("ABCDEFGHIJKLMN", ["CALLS", "CALLS", "ROOM", "HOUSEKEEPING", "CALLS", "HOUSEKEEPING",
    "ROOM", "POEMS", "HOUSEKEEPING", "POEMS", "ROOM", "ROOM", "HOUSEKEEPING", "CALLS"]))
DEPTHS = [0, 6, 8, 12, 16, 16]

def generate():
    notes = []
    pattern = r"^\*\*([A-NSP]\d{2}) — (.*?)\*\*\n(.*?)(?=^\*\*|^## |\Z)"
    for match in re.finditer(pattern, SOURCE.read_text(), re.M | re.S):
        id, title = match[1], match[2]
        text = "\n".join(line[2:] if line.startswith("> ") else ""
                         for line in match[3].splitlines() if line.startswith(">")).strip()
        chain, n = id[0], int(id[1:]) - 1
        if chain == 'S': title = title.split(', ', 1)[-1].capitalize()
        serial = chain in AUTHORS
        note = dict(id=id, title=title, author=AUTHORS.get(chain, "Unsigned"),
                    style=STYLES.get(chain, "KAREN"), chain=chain if serial else "",
                    installment=n if serial else 0, depth=DEPTHS[n] if serial else 0,
                    surface=SURFACES.get(chain, "ROOM"), text=text)
        if id == "C05": note.update(author="Ada", style="KAREN")
        if id == "F04": note.update(author="Shift supervisor", style="PLAIN")
        if id in ("S06", "S07", "S11"): note.update(depth=12, style="WILL")
        if chain == "P": note.update(depth=6, style="PELAFINA")
        if not text or len(title) > 32: raise ValueError((id, title))
        notes.append(note)
    assert len(notes) == 108 and len({n['id'] for n in notes}) == 108
    for chain in AUTHORS:
        assert sorted(n['installment'] for n in notes if n['chain'] == chain) == list(range(6))
    # Interleave the chains with loose notes; matching a surface is a preference, never an access gate.
    notes.sort(key=lambda n: (n['installment'] if n['chain'] else int(n['id'][1:]) % 6, n['id']))
    widths = {}
    for font in ('karen', 'will', 'zampano', 'pelafina'):
        spec = json.loads((ROOT / f'src/main/resources/assets/the_oldest_house/font/{font}.json').read_text())
        bitmap = next(p for p in spec['providers'] if p['type'] == 'bitmap')
        im = Image.open(ROOT / f'src/main/resources/assets/the_oldest_house/textures/font/{font}.png').convert('RGBA')
        rows = bitmap['chars']; cell_w, cell_h = im.width // len(rows[0]), im.height // len(rows)
        advances = {' ': 4}
        for y, row in enumerate(rows):
            for x, char in enumerate(row):
                if char == '\0': continue
                cell = im.crop((x*cell_w, y*cell_h, (x+1)*cell_w, (y+1)*cell_h))
                bounds = cell.getchannel('A').getbbox()
                advances[char] = math.ceil((bounds[2] if bounds else 0) * bitmap['height'] / cell_h) + 1
        widths[font.upper() if font != 'will' else 'WILL'] = advances
    DEST.mkdir(parents=True, exist_ok=True)
    for name, value in [('letters', notes), ('advances', widths)]:
        (DEST / f'{name}.json').write_text(json.dumps(value, ensure_ascii=False, indent=2) + '\n')
    print(f'Generated {len(notes)} approved notes and {len(widths)} authored font advance tables.')

if __name__ == '__main__': generate()
