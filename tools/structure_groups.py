"""The complete native suite, in two halves (run 1576, 13 November 2026).

The complete suite runs every namespace in build.gradle's default list. Until run 1576 it ran in one game test server in the build job.
Run 1575 took 97m48s for it. In run 1576 the job's own 100-minute limit stopped it after 969 of its roughly 1,060 batches, with no case
failed. It now runs in two halves, in two jobs at the same time:

  rest   the long vignettes in REST, in the structure-rest job (about 50 minutes of run 1575's complete suite);
  main   every other namespace in the default list, in the build job (about 47 minutes).

The main half is the default list less REST, so a namespace added to that list is run without a second list to keep here. A case's
namespace is the one NeoForge filters it by: its @GameTest templateNamespace when it has one, otherwise its class's @GameTestHolder.

Usage:
  python3 tools/structure_groups.py namespaces main|rest   the half's namespaces, comma-separated, for -PgameTestNamespaces
  python3 tools/structure_groups.py declared main|rest     how many cases the half declares
  python3 tools/structure_groups.py --check                every REST namespace is in the default list, every declared case is in a
                                                           namespace the default list runs, and the halves add up to every case
"""
from pathlib import Path
import re
import subprocess
import sys

ROOT = Path(__file__).resolve().parents[1]
MOD = 'the_oldest_house'
SOURCES = 'src/main/java'
# Minutes each took inside run 1575's complete suite: pinehook 15.1, bike_night 14.3, borrasca 13.0, lake_house 2.5, playtest492 2.5,
# pell 2.2. Every one of them also has a focused job of its own.
REST = ['pinehook', 'bike_night', 'borrasca', 'lake_house', 'playtest492', 'pell']


def default_namespaces():
    """The namespaces runGameTestServer runs when none are given (build.gradle's gameTestServer run)."""
    line = next(l for l in (ROOT / 'build.gradle').read_text().splitlines()
                if "gradleProperty('gameTestNamespaces').getOrElse(" in l)
    expression = line.split('getOrElse(', 1)[1].replace('project.mod_id', "'" + MOD + "'")
    names = [n for n in ''.join(re.findall(r"'([^']*)'", expression)).split(',') if n]
    if len(names) != len(set(names)):
        raise SystemExit(f'a namespace is listed twice in build.gradle: {names}')
    return names


def namespace(expression):
    expression = re.sub(r'\s+', '', expression)
    if expression == 'TheOldestHouse.MOD_ID':
        return MOD
    match = re.fullmatch(r'TheOldestHouse\.MOD_ID\+"([^"]+)"', expression) or re.fullmatch(r'"([^"]+)"', expression)
    if not match:
        raise SystemExit(f'a namespace this script cannot read: {expression}')
    text = match.group(1)
    return text if text.startswith(MOD) else MOD + text


def sources():
    """The committed Java sources (local probes that are never committed are not cases CI runs)."""
    try:
        listed = subprocess.check_output(['git', 'ls-files', SOURCES], cwd=ROOT, text=True).split()
        return [ROOT / name for name in listed if name.endswith('.java')]
    except (OSError, subprocess.CalledProcessError):
        return sorted((ROOT / SOURCES).rglob('*.java'))


def cases():
    """One namespace for each declared @GameTest case."""
    found = []
    for source in sources():
        text = source.read_text()
        starts = [m.end() for m in re.finditer(r'@GameTest\s*\(', text)]
        if not starts:
            continue
        holder = re.search(r'@GameTestHolder\(([^)]*)\)', text)
        for start in starts:
            depth, end = 1, start
            while depth:
                depth += {'(': 1, ')': -1}.get(text[end], 0)
                end += 1
            arguments = text[start:end - 1]
            override = re.search(r'templateNamespace\s*=\s*([^,]+)', arguments)
            if override:
                found.append(namespace(override.group(1)))
            elif holder:
                found.append(namespace(holder.group(1)))
            else:
                raise SystemExit(f'{source.relative_to(ROOT)} declares a case without a namespace')
    return found


def halves():
    every = default_namespaces()
    rest = [MOD + '_' + name for name in REST]
    return {'main': [n for n in every if n not in rest], 'rest': rest}, every


def declared(half):
    spaces = set(halves()[0][half])
    return sum(1 for n in cases() if n in spaces)


def check():
    groups, every = halves()
    missing = [n for n in groups['rest'] if n not in every]
    if missing:
        raise SystemExit(f'REST names namespaces build.gradle does not run: {missing}')
    all_cases = cases()
    outside = sorted({n for n in all_cases if n not in every})
    if outside:
        raise SystemExit(f'cases are declared in namespaces the complete suite never runs: {outside}')
    main, rest = declared('main'), declared('rest')
    if main + rest != len(all_cases) or not main or not rest:
        raise SystemExit(f'the halves do not add up: main {main} + rest {rest} != {len(all_cases)}')
    print(f'main {main} cases in {len(groups["main"])} namespaces; rest {rest} in {len(groups["rest"])}; {len(all_cases)} declared')


def main(argv):
    if argv == ['--check']:
        check()
    elif len(argv) == 2 and argv[0] == 'namespaces' and argv[1] in ('main', 'rest'):
        print(','.join(halves()[0][argv[1]]))
    elif len(argv) == 2 and argv[0] == 'declared' and argv[1] in ('main', 'rest'):
        print(declared(argv[1]))
    else:
        raise SystemExit(__doc__)


if __name__ == '__main__':
    main(sys.argv[1:])
