"""0.4.240 (owner: "missing particles for some textures..."): every block model resolves a particle texture, so breaking or
hitting a block never shows the missing-texture checkerboard. Vanilla parents are listed with the key their own chain names as the
particle (None: the parent names none, so the model must). Run in the quick gate."""
import json, sys
from pathlib import Path

A = Path(__file__).resolve().parent.parent / 'src/main/resources/assets/the_oldest_house/models'
VANILLA = {'block/block': None, 'block/cube': None, 'block/cube_all': 'all', 'block/orientable': 'front', 'block/carpet': 'wool',
           'block/cube_bottom_top': 'side', 'block/slab': 'side', 'block/slab_top': 'side', 'block/stairs': 'side', 'block/inner_stairs': 'side',
           'block/outer_stairs': 'side', 'block/cross': 'cross', 'block/cube_column': 'side', 'block/cube_column_horizontal': 'side',
           'block/fence_side': 'texture', 'block/fence_post': 'texture', 'block/ladder': '=', 'block/chain': '='}
for door in ('top', 'bottom'):
    for side in ('left', 'right'):
        for opened in ('', '_open'):VANILLA[f'block/door_{door}_{side}{opened}'] = door
for pane in ('post', 'side', 'side_alt', 'noside', 'noside_alt'):VANILLA['block/template_glass_pane_' + pane] = 'pane'
for gate in ('', '_open', '_wall', '_wall_open'):VANILLA['block/template_fence_gate' + gate] = 'texture'
for trap in ('top', 'bottom', 'open'):VANILLA['block/template_orientable_trapdoor_' + trap] = 'texture'


def resolve(path, seen=0):
    model = json.loads(path.read_text()); textures = dict(model.get('textures', {})); parent = model.get('parent')
    while parent and seen < 30:
        ns, name = parent.split(':', 1) if ':' in parent else ('minecraft', parent)
        if ns == 'minecraft':
            if name not in VANILLA:return None, f'unlisted vanilla parent {parent}'
            key = VANILLA[name]
            if 'particle' not in textures and key and key != '=':textures['particle'] = '#' + key
            elif 'particle' not in textures and key == '=':textures['particle'] = parent
            break
        inner = A / (name + '.json')
        if not inner.is_file():return None, f'missing parent {parent}'
        parent_model = json.loads(inner.read_text()); merged = dict(parent_model.get('textures', {})); merged.update(textures); textures = merged
        parent = parent_model.get('parent'); seen += 1
    value = textures.get('particle'); hops = 0
    while isinstance(value, str) and value.startswith('#') and hops < 10:value = textures.get(value[1:]); hops += 1
    return value, None


bad = []
for path in sorted((A / 'block').rglob('*.json')):
    value, problem = resolve(path)
    if problem or not value:bad.append(f'{path.relative_to(A)}: {problem or "no particle texture"}')
if bad:
    print('\n'.join(bad)); sys.exit(f'{len(bad)} block models have no particle texture')
print(f'Every block model resolves a particle texture.')
