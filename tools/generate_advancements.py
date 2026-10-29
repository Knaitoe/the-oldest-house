"""0.4.165: the House's advancements, two tabs, generated from one table (docs/ADVANCEMENTS_DESIGN.md).

  house/    This House Has Ghosts in It: the way in, depth, the great staircase, the endings, strays, the Mother.
  account/  An Account: progress toward the Witness, the five kinds of ending and every story under its kind.

The story rows are read from WitnessAccount.Story in the Java source, so an advancement's title is always that story's
account title and its description the account's first sentence, in the reader's own "I". Every criterion is
minecraft:impossible except chalk and the spool (Minecraft's own inventory_changed): HouseAdvancements awards them from
the facts the save already records. Nothing announces to chat, nothing gives a reward, and every ending, every kind and
every story is hidden until it is earned (the owner's choices, October 29).

Usage: python3 tools/generate_advancements.py [--check]
"""
from pathlib import Path
import json
import re
import sys

ROOT = Path(__file__).resolve().parents[1]
NS = 'the_oldest_house'
DATA = ROOT / 'src/main/resources/data' / NS / 'advancement'
LANG = ROOT / 'src/main/resources/assets' / NS / 'lang/en_us.json'
STORIES_SOURCE = ROOT / 'src/main/java/io/github/knaitoe/theoldesthouse/labyrinth/WitnessAccount.java'

# id, title, description, parent, frame, hidden, icon
HOUSE = [
    ('letter', 'This House Has Ghosts in It', 'Someone wrote to you about a house.', None, 'task', False, 'minecraft:paper'),
    ('manor', 'The manor', 'You went in.', 'letter', 'task', False, 'minecraft:oak_door'),
    ('room_between', 'The room between rooms', "A door in the study opened onto somewhere it shouldn't.", 'manor', 'task', False, 'minecraft:dark_oak_door'),
    ('hallway', 'The impossible hallway', 'It was longer inside than the house is.', 'room_between', 'task', False, 'minecraft:painting'),
    ('junction', 'Past the far door', 'The hallway ended in a door, and the door did not end.', 'hallway', 'task', False, 'minecraft:iron_door'),
    ('six_doors', 'Six doors deep', 'Far enough in for the House to start telling stories.', 'junction', 'task', False, 'minecraft:written_book'),
    ('growl', 'Something below', 'You heard it breathe.', 'six_doors', 'task', True, 'minecraft:sculk_shrieker'),
    ('stone', 'Stone', 'The papered walls gave way to stone.', 'six_doors', 'task', False, 'minecraft:cobblestone'),
    ('twenty_doors', 'Twenty doors deep', 'Deep enough to be offered the stairs.', 'stone', 'goal', False, 'minecraft:ladder'),
    ('camp', "The explorers' camp", 'Someone came this way before you and left their things.', 'twenty_doors', 'task', True, 'minecraft:campfire'),
    ('bound', 'Your own book', 'You bound the first leaf of your House of Leaves.', 'camp', 'task', True, 'minecraft:writable_book'),
    ('first_fire', 'The first fire', 'Your own words, burned for light.', 'bound', 'task', True, 'minecraft:flint_and_steel'),
    ('five_fires', 'Five fires', 'The book is ash, and the stairs are lit.', 'first_fire', 'goal', True, 'minecraft:soul_lantern'),
    ('cell', 'The cell', 'At the bottom, a door with bars.', 'five_fires', 'goal', True, 'minecraft:iron_bars'),
    ('ending_witness', 'The Witness', 'You opened the door with both hands empty.', 'cell', 'challenge', True, 'minecraft:iron_sword'),
    ('ending_wound', 'The wound', 'The House came down, and you got out.', 'cell', 'challenge', True, NS + ':holloway_shield'),
    ('ending_locked', 'Locked out', 'The House closed its door on you.', 'cell', 'task', True, 'minecraft:barrier'),
    ('stray', 'A stray', 'Something that lives in the House let you feed it.', 'manor', 'task', False, 'minecraft:cooked_mutton'),
    ('together', 'Not alone', 'You took a companion through a door with you.', 'stray', 'task', False, 'minecraft:lead'),
    ('mother', 'The Mother of Strays', 'She keeps what you lose.', 'manor', 'task', True, 'minecraft:bone'),
    ('ransom', 'Paid for', 'You bought something back from her.', 'mother', 'task', True, 'minecraft:emerald'),
    ('chalk', 'Chalk', 'Marks, to find your way back.', 'junction', 'task', False, NS + ':chalk'),
    ('spool', 'A line back', 'String, tied off where you came in.', 'chalk', 'task', False, NS + ':trail_spool'),
    ('letters', 'Correspondence', 'Someone in the House has been writing to you.', 'manor', 'task', False, NS + ':self_addressed_envelope'),
    ('likeness', 'A likeness', 'The House painted you into the family.', 'manor', 'task', True, NS + ':literary_photograph'),
]
# Chalk and the spool are Minecraft's own: the item in the reader's inventory.
INVENTORY = {'chalk': NS + ':chalk', 'spool': NS + ':trail_spool'}

PROGRESS = [
    ('account', 'An Account', 'I am writing down what I saw, before I begin remembering something else.', None, 'task', False, 'minecraft:written_book'),
    ('first_story', 'Heard to the end', 'One room told me how it ends.', 'account', 'task', False, 'minecraft:writable_book'),
    ('two_kinds', 'Another kind of ending', 'Not everything here ends the same way.', 'first_story', 'goal', False, 'minecraft:book'),
    ('five_stories', 'Five rooms', 'I have heard five rooms to the end.', 'first_story', 'task', False, 'minecraft:book'),
    ('twenty_stories', 'Twenty rooms', 'I have heard twenty rooms to the end.', 'five_stories', 'goal', False, 'minecraft:bookshelf'),
    ('enough', 'Enough to be heard', 'I have heard enough of the House to speak for it.', 'twenty_stories', 'goal', False, 'minecraft:lectern'),
    ('every_story', 'Every room', "There is nothing left in the House I haven't heard to the end.", 'enough', 'challenge', True, 'minecraft:enchanted_book'),
]
KINDS = {
    'survival': ('What I survived', 'Some rooms end with me getting out.', 'minecraft:shield'),
    'understanding': ('What I understood', 'Some rooms end with me understanding something.', 'minecraft:spyglass'),
    'memory': ('What I remembered', 'Some rooms end with something remembered.', 'minecraft:filled_map'),
    'connection': ('Who answered', 'Some rooms end with someone answering.', 'minecraft:bell'),
    'release': ('What was let go', 'Some rooms end with something let go.', 'minecraft:feather'),
}
# Each story's own object: what its account is read from, or what ends it.
STORY_ICONS = {
    'floorboards': 'minecraft:iron_axe', 'hide_and_clap': NS + ':blindfold', 'harrigan': NS + ':harrigans_phone',
    'model_home': 'minecraft:glass_pane', 'drowned_town': NS + ':dried_essay_one', 'preserved_cave': 'minecraft:candle',
    'shallows': NS + ':shallows_burden', 'phone_canoe': NS + ':lake_phone', 'goatman': NS + ':goatman_brat',
    'ted_caver': 'minecraft:iron_pickaxe', 'zampano_courtyard': NS + ':archive_key', 'whale': NS + ':self_addressed_envelope',
    'barn_well': NS + ':well_ribbon', 'plain': NS + ':plain_camera', 'hospital': 'minecraft:stone_button',
    'holloway_camp': NS + ':holloway_shield', 'mother_of_strays': NS + ':cat_collar', 'seance': NS + ':seance_planchette',
    'wallpaper_nursery': NS + ':wallpaper_folio', 'hotel': NS + ':hotel_photograph', 'hill_nursery': NS + ':literary_photograph',
    'miniatures': 'minecraft:armor_stand', 'masque': 'minecraft:clock', 'usher': 'minecraft:dark_oak_trapdoor',
    'winchester': 'minecraft:book', 'child_room': 'minecraft:red_bed', 'crimson_hall': NS + ':wax_cylinder_one',
    'bly_route': 'minecraft:spruce_door', 'elk_lot': 'minecraft:wheat', 'elk_fan': NS + ':elk_tooth',
    'mapping_interior': 'minecraft:map', 'holy_rabbit': 'minecraft:rabbit_hide', 'confession': 'minecraft:ink_sac',
    'elk_carcasses': 'minecraft:leather', 'costume_night': 'minecraft:carved_pumpkin', 'movie_night': 'minecraft:firework_rocket',
    'winter_lake': NS + ':winter_ribbon', 'camp_blood': 'minecraft:oak_sign', 'devils_rock': 'minecraft:paper',
    'wheel': NS + ':wheel_house_key', 'ghosts_set': NS + ':camcorder', 'end_world_cabin': NS + ':cabin_snow_globe',
    'family_copy': 'minecraft:oak_door', 'three_fingers': NS + ':withered_paw', 'lake_house': NS + ':lake_house_helmet',
    'borrasca': NS + ':borrasca_name_patch', 'painted_interior': 'minecraft:painting', 'second_waitress': NS + ':second_waitress_menu',
    'tunnel_periphery': 'minecraft:lime_stained_glass', 'missing_sleep': 'minecraft:white_bed', 'layered_man': NS + ':cellar_cleaver',
    'motion_light': 'minecraft:redstone_lamp', 'backseat': 'minecraft:iron_door', 'silo': 'minecraft:yellow_dye',
}
BACKGROUNDS = {'house': NS + ':textures/block/manor/damask.png', 'account': NS + ':textures/block/detail_paper.png'}


def stories():
    """(id, title, kind, first sentence) for every WitnessAccount.Story, in the enum's order."""
    source = STORIES_SOURCE.read_text()
    body = source[source.index('public enum Story {'):source.index('public final String id, title, kind, text;')]
    out = []
    for m in re.finditer(r'[A-Z_]+\(\s*"([a-z_]+)"\s*,\s*"([^"]+)"\s*,\s*"([a-z]+)"\s*,\s*"((?:[^"\\]|\\.)*)"', body):
        sid, title, kind, text = m.groups()
        first = re.match(r'(.+?[.!?])(\s|$)', text)
        out.append((sid, title, kind, first.group(1) if first else text))
    return out


def key(tab, aid, part):
    return 'advancements.%s.%s.%s.%s' % (NS, tab, aid, part)


def advancement(tab, aid, parent, frame, hidden, icon, criterion=None):
    display = {
        'icon': {'id': icon},
        'title': {'translate': key(tab, aid, 'title')},
        'description': {'translate': key(tab, aid, 'description')},
        'frame': frame,
        'show_toast': True,
        'announce_to_chat': False,
        'hidden': hidden,
    }
    if parent is None:
        display['background'] = BACKGROUNDS[tab]
    data = {}
    if parent is not None:
        data['parent'] = '%s:%s/%s' % (NS, tab, parent)
    data['display'] = display
    data['criteria'] = {'house': criterion or {'trigger': 'minecraft:impossible'}}
    data['requirements'] = [['house']]
    return data


def outputs():
    files, lang = {}, {}

    def add(tab, aid, title, description, parent, frame, hidden, icon, criterion=None):
        files['%s/%s.json' % (tab, aid)] = advancement(tab, aid, parent, frame, hidden, icon, criterion)
        lang[key(tab, aid, 'title')] = title
        lang[key(tab, aid, 'description')] = description

    for aid, title, description, parent, frame, hidden, icon in HOUSE:
        criterion = None
        if aid in INVENTORY:
            criterion = {'trigger': 'minecraft:inventory_changed', 'conditions': {'items': [{'items': INVENTORY[aid]}]}}
        add('house', aid, title, description, parent, frame, hidden, icon, criterion)
    for aid, title, description, parent, frame, hidden, icon in PROGRESS:
        add('account', aid, title, description, parent, frame, hidden, icon)
    for kind, (title, description, icon) in KINDS.items():
        add('account', 'kind_' + kind, title, description, 'account', 'task', True, icon)
    for sid, title, kind, first in stories():
        add('account', sid, title, first, 'kind_' + kind, 'task', True, STORY_ICONS[sid])
    return files, lang


def problems(files):
    """The rules every advancement keeps: no chat, no reward, every story present, every ending hidden."""
    out = []
    for path, data in files.items():
        if data['display']['announce_to_chat']:
            out.append(path + ' announces to chat')
        if 'rewards' in data:
            out.append(path + ' gives a reward')
    for sid, *_ in stories():
        if 'account/%s.json' % sid not in files:
            out.append('story %s has no advancement' % sid)
    for aid in ('ending_witness', 'ending_wound', 'ending_locked'):
        if not files['house/%s.json' % aid]['display']['hidden']:
            out.append(aid + ' is not hidden')
    missing = set(s[0] for s in stories()) - set(STORY_ICONS)
    if missing:
        out.append('stories without an icon: ' + ', '.join(sorted(missing)))
    return out


def main():
    check = '--check' in sys.argv
    files, lang = outputs()
    bad = problems(files)
    if bad:
        raise SystemExit('advancement rules broken: ' + '; '.join(bad))
    stale = []
    for rel, data in files.items():
        text = json.dumps(data, indent=2) + '\n'
        path = DATA / rel
        if check:
            if not path.is_file() or path.read_text() != text:
                stale.append(rel)
            continue
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_text(text)
    for tab in ('house', 'account'):
        folder = DATA / tab
        if folder.is_dir():
            for path in folder.glob('*.json'):
                if '%s/%s' % (tab, path.name) not in files:
                    if check:
                        stale.append('%s/%s (not in the table)' % (tab, path.name))
                    else:
                        path.unlink()
    current = json.loads(LANG.read_text())
    if check:
        for k, v in lang.items():
            if current.get(k) != v:
                stale.append('lang ' + k)
    else:
        for k in [k for k in current if k.startswith('advancements.%s.' % NS) and k not in lang]:
            del current[k]
        current.update(lang)
        LANG.write_text(json.dumps(current, indent=2, ensure_ascii=False) + '\n')
    if stale:
        raise SystemExit('advancements differ from tools/generate_advancements.py: ' + ', '.join(stale[:12]))
    if not check:
        print('%d advancements: %d in the house tab, %d in the account tab' % (len(files), sum(1 for f in files if f.startswith('house/')), sum(1 for f in files if f.startswith('account/'))))


if __name__ == '__main__':
    main()
