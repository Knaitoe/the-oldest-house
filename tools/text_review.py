#!/usr/bin/env python3
"""The text review page: every line of text the game shows, for the owner to read and to change.

    python3 tools/text_review.py --build OUT [--repo owner/name] [--branch NAME] [--trigger ID]
        writes OUT/text-review.html (the page) and OUT/texts.js (what it shows)
    python3 tools/text_review.py --apply BATCH.json [MORE.json ...] [--report OUT.json]
        puts edits made on the page back into the sources, and says what it did with each
    python3 tools/text_review.py --check
        tests itself, and that every text in the tree is written back as it is read

What it reads
  - Every string in the mod's own Java (not the tests, not the proofs) that is words: two words or more, or one word that
    ends a sentence. A string written for a log, an exception or an annotation is left out.
  - The language file: the names of blocks, items and entities, the subtitles, the advancements.
  - The letters (correspondence/letters.json): each letter's title and its text.
What it leaves alone
  - The novel's own texts (correspondence/novel_sources.json). They are quoted, and stay as they are.
  - Words painted into pictures. Those are drawn by the art tools.

How an edit finds its line
  By the file, the words as they were, and which of the file's strings with those very words it is (the first, the second).
  All of a file's edits are placed against the file as it stood, then written together. If the file no longer has as many
  strings with those words as the page saw, the line has moved on, and that edit is reported and not applied.

A name in the language file is usually also held by the art tool that draws the thing, which checks the file against it. An
edit to such a name is made in the tool as well when exactly one string there has the old words; otherwise the places are
listed for a hand to change.
"""
import argparse
import ast
import collections
import datetime
import io
import json
import pathlib
import re
import subprocess
import sys
import tempfile
import tokenize

ROOT = pathlib.Path(__file__).resolve().parent.parent
JAVA = 'src/main/java'
LANG = 'src/main/resources/assets/the_oldest_house/lang/en_us.json'
LETTERS = 'src/main/resources/data/the_oldest_house/correspondence/letters.json'
LETTER_FIELDS = ('title', 'text')
PAGE = pathlib.Path(__file__).with_name('text_review_page.html')

WORD = re.compile(r"[A-Za-z][A-Za-z'’]+")
ONE_WORD = re.compile(r"[A-Z][A-Za-z'’\-]*[.?!…]+")
SKIP_FILE = re.compile(r'(Proof|Smoke|Shot|Probe)\.java$')
# What a string is being written for, read from the code before it in its statement.
SKIP_HEAD = re.compile(r'\bLOGGER\s*\.\s*\w+\s*\(|\bnew\s+[\w.]*(?:Exception|Error)\s*\(|@\w+\s*\([^()]*$|^assert\b'
                       r'|\bSystem\s*\.\s*(?:getProperty|getenv|out|err)\b')
OPERATOR_HEAD = re.compile(r'\bsendSuccess\b|\bsendFailure\b|\.\s*comment\s*\(')
OPERATOR_FILE = re.compile(r'/command/|Commands?\.java$')

Lit = collections.namedtuple('Lit', 'start end block text pre joined glue skipped operator label names call said title leads')


# ---- Java strings -------------------------------------------------------------------------------------------------

def lex(src):
    """A Java source's string literals as (start, end, is_block), and the source with every comment, character literal
    and string's inside blanked, so that what is left of it is code."""
    spans, mask, i, n = [], list(src), 0, len(src)

    def blank(a, b):
        for k in range(a, b):
            if mask[k] != '\n':
                mask[k] = ' '

    while i < n:
        c = src[i]
        if c == '/' and src.startswith('//', i):
            j = src.find('\n', i)
            j = n if j < 0 else j
            blank(i, j)
            i = j
        elif c == '/' and src.startswith('/*', i):
            j = src.find('*/', i + 2)
            j = n if j < 0 else j + 2
            blank(i, j)
            i = j
        elif c == "'":
            j = i + 1
            while j < n and src[j] != "'":
                j += 2 if src[j] == '\\' else 1
            blank(i, j + 1)
            i = j + 1
        elif c == '"':
            if src.startswith('"""', i):
                j = i + 3
                while j < n and not src.startswith('"""', j):
                    j += 2 if src[j] == '\\' else 1
                spans.append((i, j + 3, True))
                blank(i + 3, j)
                i = j + 3
            else:
                j = i + 1
                while j < n and src[j] != '"' and src[j] != '\n':
                    j += 2 if src[j] == '\\' else 1
                spans.append((i, j + 1, False))
                blank(i + 1, j)
                i = j + 1
        else:
            i += 1
    return spans, ''.join(mask)


ESCAPES = {'n': '\n', 't': '\t', 'b': '\b', 'r': '\r', 'f': '\f', 's': ' ', '"': '"', "'": "'", '\\': '\\'}


def unescape(raw):
    """The words of a Java string, from how they are written between its quotation marks."""
    out, i, n = [], 0, len(raw)
    while i < n:
        c = raw[i]
        if c != '\\':
            out.append(c)
            i += 1
            continue
        d = raw[i + 1]
        if d == 'u':
            j = i + 1
            while raw[j] == 'u':
                j += 1
            out.append(chr(int(raw[j:j + 4], 16)))
            i = j + 4
        elif d in '01234567':
            j = k = i + 1
            most = 3 if d in '0123' else 2
            while k < n and k - j < most and raw[k] in '01234567':
                k += 1
            out.append(chr(int(raw[j:k], 8)))
            i = k
        elif d == '\n':
            i += 2  # a text block's line that carries on into the next
        else:
            out.append(ESCAPES[d])
            i += 2
    text = ''.join(out)
    if any('\ud800' <= ch <= '\udfff' for ch in text):
        text = text.encode('utf-16', 'surrogatepass').decode('utf-16')
    return text


def escape(text, ascii_only):
    """These words as they are written between a Java string's quotation marks."""
    out = []
    for ch in text:
        o = ord(ch)
        if ch == '\\':
            out.append('\\\\')
        elif ch == '"':
            out.append('\\"')
        elif ch == '\n':
            out.append('\\n')
        elif ch == '\t':
            out.append('\\t')
        elif ch == '\r':
            out.append('\\r')
        elif o < 32 or o == 127:
            out.append('\\u%04x' % o)
        elif o > 126 and ascii_only:
            if o > 0xFFFF:
                o -= 0x10000
                out.append('\\u%04x\\u%04x' % (0xD800 + (o >> 10), 0xDC00 + (o & 0x3FF)))
            else:
                out.append('\\u%04x' % o)
        else:
            out.append(ch)
    return ''.join(out)


def block_lines(body):
    """A text block's lines with their shared indentation taken off, that indentation, and the last line's own."""
    lines = [line.rstrip('\r') for line in body[body.index('\n') + 1:].split('\n')]
    counted = [line for line in lines[:-1] if line.strip()] + [lines[-1]]
    indent = min(len(line) - len(line.lstrip(' \t')) for line in counted)
    first = next((line for line in lines if line.strip()), lines[-1])
    return [line[indent:].rstrip(' \t') if line.strip() else '' for line in lines], first[:indent], lines[-1]


def block_text(body):
    return unescape('\n'.join(block_lines(body)[0]))


def block_body(text, old_body):
    """A text block's body for these words, laid out as the block it takes the place of was."""
    _, indent, last = block_lines(old_body)
    if last.strip() == '':
        indent = last  # the closing mark stood on a line of its own: its indentation is the block's
    own_line = text.endswith('\n')
    lines = text[:-1].split('\n') if own_line else text.split('\n')
    out = []
    for line in lines:
        s = []
        for ch in line:
            o = ord(ch)
            if ch == '\\':
                s.append('\\\\')
            elif ch == '\t':
                s.append('\\t')
            elif o < 32 or o == 127:
                s.append('\\u%04x' % o)
            else:
                s.append(ch)
        s = re.sub(r'"{3,}', lambda m: '\\"' * len(m.group()), ''.join(s))
        if s.endswith(' '):
            s = s[:-1] + '\\s'
        out.append(s)
    if not own_line:
        if out[-1].endswith('"') and not out[-1].endswith('\\"'):
            out[-1] = out[-1][:-1] + '\\"'
        # With the closing mark on the last line nothing holds the left edge but the lines themselves.
        filled = [k for k, s in enumerate(out) if s.strip()]
        if filled and all(out[k][0] == ' ' for k in filled):
            k = min(filled, key=lambda k: len(out[k]) - len(out[k].lstrip(' ')))
            out[k] = '\\s' + out[k][1:]
    body = '\n' + '\n'.join(indent + s if s else '' for s in out)
    return body + ('\n' + indent if own_line else '')


BOUNDARY = re.compile(r'[(){};]')
ANNOTATION = re.compile(r'@\w+(\s*\([^()]*\))?')
TYPE_HEAD = re.compile(r'\b(class|interface|enum|record)\s+(\w+)')
INIT_HEAD = re.compile(r'(\w+)\s*(?:\[\s*\]\s*)*=\s*(?:new\s+[\w.<>\[\]\s]+)?$')
METHOD_HEAD = re.compile(r'(\w+)\s*\([^;{}]*\)\s*(?:throws\s+[\w\s,.]+)?$')
ASSIGNED = re.compile(r'(\w+)\s*(?:\[\s*\]\s*)*=(?!=)')
ENUM_CONSTANT = re.compile(r'(?:^|[,)]\s*)([A-Z][A-Z0-9_]*)\s*\(')
CALLED = re.compile(r'([A-Za-z_][\w.]*)\s*$')
NOT_A_METHOD = {'if', 'for', 'while', 'switch', 'catch', 'synchronized', 'try', 'do', 'else', 'return', 'new'}
# A short string that names somebody or something, passed before the words it goes with: speak(n, "Ray", "Mind the beam.").
NAME_LIKE = re.compile(r"[A-Z][A-Za-z'’.\-]*(?: [A-Za-z][A-Za-z'’.\-]*){0,4}")
LOOKED_UP = re.compile(r'(?:\.\s*(?:get|put|contains|remove|has|is|equals|startsWith|endsWith|indexOf)\w*\s*\(|\bcase\s*)$')
# A call whose first string is a title and whose second, if it is a name, is who wrote it.
WRITTEN = {'book', 'plain', 'signed', 'p'}


def name_like(text):
    return (NAME_LIKE.fullmatch(text) is not None and len(text) <= 32 and text[-1] not in '.?!…:,;-'
            and not (text.isupper() and len(text) > 1))


def scan_java(src, operator_file=False):
    """Every string of a Java source, in order, with what it is written for: the code before it in its statement (pre),
    the name it is given or the method it is in (label), the names round it, nearest first (names), the call it is
    passed to (call), the name passed before it in that call or one round it (said), and whether it is a book's title."""
    spans, code = lex(src)
    marks = [(m.start(), m.group()) for m in BOUNDARY.finditer(code)]
    out, stack, stmt, pending, joined_to, decl, k, prev = [], [], 0, '', False, '', 0, 0
    calls, said, said_depth, base = [], '', 0, ''
    for a, b, block in spans:
        while k < len(marks) and marks[k][0] < a:
            at, mark = marks[k]
            k += 1
            if mark == '(':
                called = CALLED.search(code[max(stmt, at - 80):at])
                calls.append([called.group(1) if called else '', 0])
                continue
            if mark == ')':
                if calls:
                    calls.pop()
                if said and len(calls) < said_depth:
                    said, said_depth = base, 0
                continue
            if mark == '{':
                head = ANNOTATION.sub('', ' '.join(code[stmt:at].split())).strip()
                kind, name, word = 'other', '', ''
                type_head, init, method = TYPE_HEAD.search(head), INIT_HEAD.search(head), METHOD_HEAD.search(head)
                if type_head and '=' not in head and not re.search(r'\bnew\b', head):
                    kind, name, word = 'type', type_head.group(2), type_head.group(1)
                elif init:
                    kind, name = 'init', init.group(1)
                elif (method and method.group(1) not in NOT_A_METHOD and '=' not in head and '->' not in head
                      and not re.search(r'\bnew\b', head) and (not stack or stack[-1][0] == 'type')):
                    kind, name = 'method', method.group(1)
                stack.append((kind, name, word, calls, base, said, said_depth, decl))
                calls, base, said_depth = [], said, 0
                decl = decl if kind in ('init', 'other') else ''
            elif mark == '}':
                if stack:
                    _, _, _, calls, base, said, said_depth, decl = stack.pop()
            else:
                calls, said, said_depth, decl = [], base, 0, ''
            stmt = at + 1
            pending, joined_to = '', False
            prev = max(prev, stmt)
        pending += code[prev:a]
        body = src[a + 3:b - 3] if block else src[a + 1:b - 1]
        text = block_text(body) if block else unescape(body)
        head = ' '.join(code[stmt:a].split())
        pre = ' '.join(pending.split())
        assigned = ASSIGNED.findall(pre)
        if assigned:
            decl = assigned[-1]
        elif stack and stack[-1][2] == 'enum' and not calls[1:]:
            constant = ENUM_CONSTANT.findall(pre)
            if constant:
                decl = constant[-1]
        call = calls[-1][0] if calls else ''
        first = bool(calls) and calls[-1][1] == 0
        if calls:
            calls[-1][1] += 1
        title = first and call.rsplit('.', 1)[-1] in WRITTEN
        skipped = bool(SKIP_HEAD.search(head)) or not is_words(text)
        glue = re.fullmatch(r'\+(.*)\+|\+', pre) if joined_to else None
        operator = operator_file or bool(OPERATOR_HEAD.search(head))
        inner = next((frame[1] for frame in reversed(stack) if frame[1] and frame[0] != 'type'), '')
        method = next((frame[1] for frame in reversed(stack) if frame[0] == 'method'), '')
        if decl and method and decl != inner:
            label = f'{method}(): {decl}'
        elif decl or inner:
            label = decl or (inner + ('()' if inner == method else ''))
            tail = re.search(r'case [^:>]*(?:->|:)$', pre)
            if tail and not decl:
                label += ': ' + tail.group()
        else:
            label = pre
        names = [decl] + [frame[1] for frame in reversed(stack) if frame[1]]
        label_here = (name_like(text) and not title and not LOOKED_UP.search(pre)
                      and code[b:b + 80].lstrip().startswith(','))
        out.append(Lit(a, b, block, text, pre, glue is not None, (glue.group(1) or '').strip() if glue else '', skipped,
                       operator, label, [n for n in names if n], call, '' if label_here else said, title, label_here))
        if label_here:
            said, said_depth = text, len(calls)
        if skipped:
            pending += src[a:b] if b - a <= 28 else src[a:a + 14] + '…"'
        else:
            pending, joined_to = '', True
        prev = b
    return out


def is_words(text):
    return (re.search(r'\s', text) is not None and len(WORD.findall(text)) >= 2) or ONE_WORD.fullmatch(text) is not None


def java_files(root):
    for f in sorted((root / JAVA).rglob('*.java')):
        rel = f.relative_to(root).as_posix()
        if '/gametest/' not in rel and not SKIP_FILE.search(rel):
            yield rel, f


def literal(lit, new, src):
    """The source text of a string literal with new words, written as the one it takes the place of was."""
    if lit.block:
        return '"""' + block_body(new, src[lit.start + 3:lit.end - 3]) + '"""'
    return '"' + escape(new, src.isascii()) + '"'


def reread(written):
    """The words of a literal as it would be read back."""
    spans, _ = lex(written)
    if len(spans) != 1 or spans[0][0] != 0 or spans[0][1] != len(written):
        return None
    block = spans[0][2]
    body = written[3:-3] if block else written[1:-1]
    return block_text(body) if block else unescape(body)


# ---- What the page shows ------------------------------------------------------------------------------------------

def dump(obj):
    """The language file and the letters are written this way by the tools that add to them."""
    return json.dumps(obj, indent=2, ensure_ascii=False) + '\n'


def group_of(name):
    m = re.match(r'[A-Z][a-z0-9]+|[A-Z]+(?![a-z])', name)
    return m.group() if m else name


def lang_group(key):
    head = key.split('.')[0]
    return {'block': 'Names', 'item': 'Names', 'entity': 'Names', 'subtitles': 'Subtitles',
            'advancements': 'Advancements'}.get(head, 'Interface')


def short(pre, most=48):
    return pre if len(pre) <= most else '…' + pre[-most:]


CATEGORIES = ['Dialogue', 'Lines', 'Replies', 'Books and papers', 'Accounts', 'Letters', 'Names', 'Subtitles',
              'Advancements', 'Interface', 'Commands and settings']
# Calls that pass who is speaking before what is said, and calls that pass who wrote a thing before what is written.
SPOKEN = {'line', 'say', 'speak', 'Shot', 'Voice', 'Person', 'Boy', 'activityDone'}
WROTE = {'book', 'typed', 'p', 'pages', 'plain', 'hand', 'page'}
REPLIED = {'reply', 'act', 'option', 'choice'}
BOOKISH = {'book', 'books', 'page', 'pages', 'letter', 'letters', 'note', 'notes', 'diary', 'journal', 'leaf', 'leaves',
           'paper', 'papers', 'card', 'cards', 'slate', 'poster', 'ledger', 'clipping', 'entry', 'entries', 'essay',
           'receipt', 'menu', 'flyer', 'notice', 'postcard', 'telegram', 'manuscript', 'transcript', 'label', 'sign'}
MID_CAPS = re.compile(r"(?<=[a-z,;:] )([A-Z][a-z]{2,})\b")
IDENT_WORD = re.compile(r'[A-Z]+(?![a-z])|[A-Z]?[a-z]+')
NOT_A_NAME = {'House', 'Two', 'Three', 'Four', 'Society', 'Street', 'Lamp', 'Lane', 'Food', 'Court', 'Elm', 'Take', 'Home',
              'Room', 'Generator', 'Leaves', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday', 'Sunday',
              'January', 'February', 'March', 'July', 'September', 'October', 'November', 'December', 'Christmas',
              'Halloween', 'God', 'Lake', 'River', 'Road', 'North', 'South', 'East', 'West', 'Mr', 'Mrs', 'Ms', 'Dr',
              'The', 'And', 'But', 'Notes', 'Measurements', 'English', 'Witness'}
# A name that ends in one of these is a place or a thing, not somebody.
NOT_SOMEBODY = {'Street', 'Avenue', 'Lane', 'Road', 'Court', 'Drive', 'Lake', 'Room', 'Home', 'Horse', 'Llama', 'Stop'}
# Names that are also words a programmer names things with: these count only where they lead a name.
ALSO_A_WORD = {'will', 'may', 'mark', 'ray', 'dot', 'bill', 'art', 'pat', 'don', 'lane', 'kit', 'rose', 'hope', 'grace',
               'bud', 'clay', 'rich', 'jack', 'rob', 'dawn', 'joy', 'faith', 'drew', 'chase', 'hunter', 'miles', 'cliff',
               'reed', 'dean', 'earl', 'king', 'duke', 'glen', 'dale', 'brook', 'heath', 'sandy', 'summer', 'autumn',
               'robin', 'jay', 'mason', 'cooper', 'del', 'len', 'pip', 'bell', 'fan'}
LEADS = {'the', 'a', 'an', 'mr', 'mrs', 'ms', 'dr', 'old', 'little', 'sergeant', 'sheriff', 'father', 'mother'}


def ident_words(name):
    return [w.lower() for w in IDENT_WORD.findall(name)]


def strict_name(label):
    """One to three words, each with a capital: a person's name as it is written."""
    words = label.split(' ')
    return (len(words) <= 3 and all(w[0].isupper() for w in words) and label not in NOT_A_NAME
            and words[-1] not in NOT_SOMEBODY)


def voice_of(lit):
    """Who a string is given to by the name passed before it, and whether they say it or wrote it."""
    label = lit.said
    if not label or name_like(lit.text) or re.search(r'[a-z][A-Z]', label) or re.match(r'[IVX]+\. ', label):
        return None, ''
    if label in NOT_A_NAME:
        return None, ''
    call = lit.call.rsplit('.', 1)[-1]
    if call in SPOKEN:
        return label, 'says'
    if call in WROTE:
        return label, 'wrote'
    if call in ('of', '') and strict_name(label):
        return label, 'wrote' if '\n\n' in lit.text or len(lit.text) > 400 else 'says'
    return None, ''


def java_rows(root):
    """The mod's own strings, by story, each with its category and whose it is."""
    by_group = collections.defaultdict(list)
    for rel, f in java_files(root):
        src = f.read_text(encoding='utf-8')
        lits = [lit if lit.skipped or reread(literal(lit, lit.text, src)) == lit.text else lit._replace(skipped=True)
                for lit in scan_java(src, bool(OPERATOR_FILE.search(rel)))]
        if any(not lit.skipped for lit in lits):
            by_group[group_of(f.stem)].append((rel, f.stem, src, lits))
    for group, sources in sorted(by_group.items()):
        # The story's people: whoever is named before a line, and any name the prose uses that the code also names things by.
        display, mid, idents, voices = {}, collections.Counter(), collections.Counter(), set()
        for _, _, _, lits in sources:
            for lit in lits:
                if lit.skipped:
                    continue
                for w in MID_CAPS.findall(lit.text):
                    mid[w] += 1
                for name in lit.names:
                    for w in set(ident_words(name)):
                        idents[w] += 1
                who, _ = voice_of(lit)
                if who:
                    voices.add(who)
                    words = [w for w in re.findall(r"[A-Za-z'’]+", who) if w.lower() not in LEADS and w[0].isupper()]
                    for w in words or [who]:
                        display.setdefault(w.lower(), who)
        for w, n in mid.items():
            low = w.lower()
            if n >= 2 and idents[low] + idents[low + 's'] >= 2 and w not in NOT_A_NAME and low != group.lower():
                display.setdefault(low, w)

        def named(lit):
            for k, name in enumerate([lit.pre] + lit.names):
                words = ident_words(name) if k else [w for token in re.findall(r'[A-Za-z_]\w*', name) for w in ident_words(token)]
                for at, w in enumerate(words):
                    for low in (w, w[:-1] if w.endswith('s') else w):
                        if low in display and (low not in ALSO_A_WORD or (k and at == 0)):
                            return display[low]
            return None

        for rel, stem, src, lits in sources:
            seen = collections.Counter(lit.text for lit in lits)
            nth = collections.Counter()
            interface = '/client/' in rel
            account = re.search(r'Account|Witness|Prose', stem) is not None
            for lit in lits:
                k = nth[lit.text]
                nth[lit.text] += 1
                if lit.skipped:
                    continue
                who, how = voice_of(lit)
                words = {w for name in lit.names for w in ident_words(name)}
                call = lit.call.rsplit('.', 1)[-1]
                if who is None:
                    who = named(lit)
                    first = re.findall(r"[A-Za-z'’]+", who)[-1] if who else ''
                    told = who and re.search(r'\b' + re.escape(first) + r'\b', lit.text) and not re.search(
                        r"\b(?:I'm|I am|It's|it's|This is|this is|name's|call me)\s+" + re.escape(first), lit.text)
                    how = 'about' if told else 'says' if who else ''
                if lit.operator:
                    category = 'Commands and settings'
                elif lit.leads and lit.text in voices:
                    category, who = 'Names', lit.text
                elif account or 'account' in words:
                    category = 'Accounts'
                elif (lit.block or lit.title or how == 'wrote' or call in WROTE or words & BOOKISH or '\n\n' in lit.text):
                    category = 'Books and papers'
                elif call in REPLIED or any(name.startswith('Q_') for name in lit.names) or words & {'reply', 'replies', 'choice', 'choices', 'option', 'options'}:
                    category = 'Replies'
                elif interface:
                    category = 'Interface'
                elif how == 'says':
                    category = 'Dialogue'
                else:
                    category = 'Lines'
                yield group, rel, stem, src, lit, k, seen[lit.text], category, who


def inventory(root=ROOT):
    """What the page shows: files as [path, name, story, kind]; who as [story, name]; texts as
    [file, line, words, label, flags, category, who or -1] and, where a file says the same words twice, [which, of]."""
    files, who, texts = [], [], []
    file_index, who_index = {}, {}

    def person(story, name):
        if name is None:
            return -1
        if (story, name) not in who_index:
            who_index[(story, name)] = len(who)
            who.append([story, name])
        return who_index[(story, name)]

    for group, rel, stem, src, lit, k, of, category, whose in java_rows(root):
        if rel not in file_index:
            file_index[rel] = len(files)
            files.append([rel, stem, group, 'java'])
        flags = (1 if lit.block else 0) | (2 if lit.joined else 0)
        row = [file_index[rel], src.count('\n', 0, lit.start) + 1, lit.text, short(lit.glue if lit.joined else lit.label),
               flags, CATEGORIES.index(category), person(group, whose)]
        if of > 1:
            row += [k, of]
        texts.append(row)
    lang = json.loads((root / LANG).read_text(encoding='utf-8'))
    for key, value in lang.items():
        if not isinstance(value, str):
            continue
        group = lang_group(key)
        if group not in file_index:
            file_index[group] = len(files)
            files.append([LANG, group, 'Names and sounds', 'lang'])
        texts.append([file_index[group], 0, value, key, 0, CATEGORIES.index(group), -1])
    letters = json.loads((root / LETTERS).read_text(encoding='utf-8'))
    files.append([LETTERS, 'Letters', 'Letters', 'letter'])
    for letter in letters:
        for field in LETTER_FIELDS:
            texts.append([len(files) - 1, 0, letter[field], letter['id'] + '.' + field, 0, CATEGORIES.index('Letters'),
                          person('Letters', letter.get('author'))])
    return files, who, texts


def build(out, repo, branch, trigger):
    out = pathlib.Path(out)
    out.mkdir(parents=True, exist_ok=True)
    files, who, texts = inventory()
    version = re.search(r'^mod_version=(.+)$', (ROOT / 'gradle.properties').read_text(), re.M).group(1).strip()
    try:
        commit = subprocess.run(['git', 'rev-parse', 'HEAD'], cwd=ROOT, capture_output=True, text=True, check=True).stdout.strip()
    except (OSError, subprocess.CalledProcessError):
        commit = ''
    data = {'version': version, 'commit': commit, 'repo': repo or '', 'branch': branch or '', 'trigger': trigger or '',
            'built': datetime.datetime.now(datetime.timezone.utc).strftime('%Y-%m-%dT%H:%MZ'),
            'categories': CATEGORIES, 'files': files, 'who': who, 'texts': texts}
    js = json.dumps(data, ensure_ascii=False, separators=(',', ':')).replace('\u2028', '\\u2028').replace('\u2029', '\\u2029')
    (out / 'texts.js').write_text('window.TEXT_REVIEW=' + js + ';\n', encoding='utf-8')
    (out / 'text-review.html').write_text(PAGE.read_text(encoding='utf-8'), encoding='utf-8')
    by_kind = collections.Counter(files[t[0]][3] for t in texts)
    print(f'Built the text review page for {version}: {len(texts)} texts in {len(files)} files '
          f'({by_kind["java"]} in the Java, {by_kind["lang"]} in the language file, {by_kind["letter"]} in the letters); '
          f'{(out / "texts.js").stat().st_size // 1024} KB.')


# ---- Putting edits back -------------------------------------------------------------------------------------------

def edits_of(doc):
    """The edits in what the page stores: a batch, a list of batches, or one of them as it is saved from the database."""
    if isinstance(doc, list):
        return [e for d in doc for e in edits_of(d)]
    if isinstance(doc, dict) and isinstance(doc.get('data'), dict) and 'edits' in doc['data']:
        doc = doc['data']
    if isinstance(doc, dict) and isinstance(doc.get('edits'), list):
        return [e for e in doc['edits'] if isinstance(e, dict)]
    raise SystemExit('Not a batch of edits: no "edits" list.')


def allowed(root, path):
    """Only the files the page shows may be written."""
    if path in (LANG, LETTERS):
        return True
    if not isinstance(path, str) or not path.startswith(JAVA + '/') or not path.endswith('.java') or '..' in path:
        return False
    return '/gametest/' not in path and not SKIP_FILE.search(path) and (root / path).is_file()


def python_strings(source):
    """A Python source's plain string literals as (start, end, words)."""
    starts = [0]
    for line in source.splitlines(keepends=True):
        starts.append(starts[-1] + len(line))
    out = []
    try:
        for tok in tokenize.generate_tokens(io.StringIO(source).readline):
            if tok.type != tokenize.STRING or tok.string[0] not in '\'"' or tok.string[:3] in ('"""', "'''"):
                continue
            a = starts[tok.start[0] - 1] + tok.start[1]
            out.append((a, a + len(tok.string), ast.literal_eval(tok.string)))
    except (tokenize.TokenError, SyntaxError, ValueError):
        pass
    return out


def python_literal(text, quote):
    body = text.replace('\\', '\\\\').replace('\n', '\\n').replace('\t', '\\t').replace(quote, '\\' + quote)
    return quote + body + quote


def apply(batch_paths, report_path=None, root=ROOT):
    edits = []
    for p in batch_paths:
        edits += edits_of(json.loads(pathlib.Path(p).read_text(encoding='utf-8')))
    results = []

    def done(e, state, why='', **more):
        results.append({'path': e.get('path'), 'key': e.get('key'), 'old': e.get('old'), 'new': e.get('new'),
                        'nth': e.get('nth', 0), 'state': state, 'why': why, **more})

    by_file = collections.defaultdict(list)
    for e in edits:
        if not isinstance(e.get('old'), str) or not isinstance(e.get('new'), str):
            done(e, 'refused', 'An edit needs the words as they were and as they are to be.')
        elif not allowed(root, e.get('path')):
            done(e, 'refused', 'Not a file the page shows.')
        elif e['old'] == e['new']:
            done(e, 'unchanged')
        else:
            by_file[e['path']].append(e)

    applied = []
    for path, es in sorted(by_file.items()):
        f = root / path
        if path == LANG or path == LETTERS:
            raw = f.read_text(encoding='utf-8')
            obj = json.loads(raw)
            if dump(obj) != raw:
                for e in es:
                    done(e, 'refused', 'The file is not laid out as this tool writes it.')
                continue
            letters = {letter['id']: letter for letter in obj} if path == LETTERS else None
            changed = False
            for e in es:
                key = e.get('key') or ''
                if letters is not None:
                    ident, _, field = key.rpartition('.')
                    holder, field = letters.get(ident), field if field in LETTER_FIELDS else None
                else:
                    holder, field = obj, key
                if holder is None or field is None or holder.get(field) != e['old']:
                    done(e, 'moved', 'The words there now are not the words the page showed.')
                    continue
                holder[field] = e['new']
                changed = True
                applied.append(e)
                done(e, 'applied')
            if changed:
                f.write_text(dump(obj), encoding='utf-8')
            continue
        src = f.read_text(encoding='utf-8')
        by_text = collections.defaultdict(list)
        for lit in scan_java(src):
            by_text[lit.text].append(lit)
        chosen = {}
        for e in es:
            found, of, nth = by_text.get(e['old'], []), e.get('of', 1), e.get('nth', 0)
            if len(found) != of or not 0 <= nth < of:
                done(e, 'moved', f'The file has {len(found)} strings with these words; the page saw {of}.')
                continue
            lit = found[nth]
            written = literal(lit, e['new'], src)
            if reread(written) != e['new']:
                done(e, 'refused', 'These words cannot be written as a Java string and read back the same.')
            elif lit.start in chosen:
                done(e, 'refused', 'Two edits for one line.')
            else:
                chosen[lit.start] = (lit, written, e)
        for start in sorted(chosen, reverse=True):
            lit, written, e = chosen[start]
            src = src[:lit.start] + written + src[lit.end:]
        if chosen:
            f.write_text(src, encoding='utf-8')
            for start in sorted(chosen):
                applied.append(chosen[start][2])
                done(chosen[start][2], 'applied', line=src.count('\n', 0, start) + 1)

    # A name the language file holds is usually held by the tool that draws the thing, too.
    named = [e for e in applied if e['path'] == LANG]
    if named:
        sources = {p: p.read_text(encoding='utf-8') for p in sorted((root / 'tools').glob('*.py'))
                   if p.name != pathlib.Path(__file__).name}
        for e in named:
            tail = (e.get('key') or '').rsplit('.', 1)[-1]
            hits = [(p, a, b) for p, text in sources.items() if tail and tail in text
                    for a, b, words in python_strings(text) if words == e['old']]
            result = next(r for r in results if r['state'] == 'applied' and r['path'] == LANG and r['key'] == e['key'])
            if len(hits) == 1:
                p, a, b = hits[0]
                text = sources[p]
                written = python_literal(e['new'], text[a])
                if ast.literal_eval(written) == e['new']:
                    sources[p] = text[:a] + written + text[b:]
                    p.write_text(sources[p], encoding='utf-8')
                    result['tool'] = f'{p.relative_to(root).as_posix()}:{text.count(chr(10), 0, a) + 1}'
                    continue
            result['look'] = [f'{p.relative_to(root).as_posix()}:{sources[p].count(chr(10), 0, a) + 1}' for p, a, _ in hits]

    # A test may say the old words.
    quoted = [e for e in applied if len(e['old']) >= 8]
    tests = root / JAVA
    if quoted and tests.is_dir():
        said = collections.defaultdict(list)
        for f in sorted(tests.rglob('*.java')):
            rel = f.relative_to(root).as_posix()
            if '/gametest/' not in rel:
                continue
            src = f.read_text(encoding='utf-8')
            if not any(e['old'][:8] in src for e in quoted):
                continue
            for lit in scan_java(src):
                for e in quoted:
                    if e['old'] in lit.text:
                        said[id(e)].append(f'{rel}:{src.count(chr(10), 0, lit.start) + 1}')
        for e in quoted:
            if said[id(e)]:
                for r in results:
                    if r['state'] == 'applied' and r['path'] == e['path'] and r['old'] == e['old'] and r['nth'] == e.get('nth', 0):
                        r['tests'] = said[id(e)]

    count = collections.Counter(r['state'] for r in results)
    for r in results:
        if r['state'] in ('moved', 'refused'):
            print(f'  not applied ({r["state"]}): {r["path"]} {r["key"] or ""} {r["old"][:60]!r}: {r["why"]}')
        for where in r.get('tests', []):
            print(f'  a test says the old words: {where} {r["old"][:60]!r}')
        if 'look' in r:
            print(f'  the name {r["key"]} is also in: {", ".join(r["look"]) or "no tool (kept by hand)"}')
        if 'tool' in r:
            print(f'  also changed in {r["tool"]}')
    print(f'Applied {count["applied"]} of {len(results)} edits'
          + (f'; {count["moved"]} moved on, {count["refused"]} refused, {count["unchanged"]} unchanged.'
             if len(results) != count['applied'] else '.'))
    if report_path:
        pathlib.Path(report_path).write_text(json.dumps({'results': results}, indent=2, ensure_ascii=False) + '\n',
                                             encoding='utf-8')
    return 0 if count['applied'] == len(results) - count['unchanged'] else 2


# ---- The check ----------------------------------------------------------------------------------------------------

def check():
    # Strings as they are written, and their words.
    samples = [r'plain words here', r'A \"quoted\" line.\nSecond line.', r'tab\there \\ back', r'caf\u00e9 \u2019s',
               r'octal \101\60 \0', 'curly ’ and — dash']
    for raw in samples:
        text = unescape(raw)
        for ascii_only in (False, True):
            assert unescape(escape(text, ascii_only)) == text, raw
    assert unescape(r'A \"quoted\" line.\nSecond line.') == 'A "quoted" line.\nSecond line.'
    assert unescape(r'octal \101\60 \0') == 'octal A0 \0'
    assert escape('é', True) == '\\u00e9' and escape('é', False) == 'é'
    same_line = '\n        First line.\n\n        Second "line".'
    own_line = '\n        First line.\n          Indented.\n        '
    assert block_text(same_line) == 'First line.\n\nSecond "line".'
    assert block_text(own_line) == 'First line.\n  Indented.\n'
    assert block_body(block_text(same_line), same_line) == same_line
    assert block_body(block_text(own_line), own_line) == own_line
    for text in ['One.', 'One.\n\nTwo.', 'Ends in a quote"', 'Three """ quotes', '  led by spaces\n  twice', 'trailing \nspace',
                 'back\\slash', 'own line\n', '\n\nblank first', '']:
        for body in (same_line, own_line):
            written = '"""' + block_body(text, body) + '"""'
            assert reread(written) == text, (text, written)
    # What is words, and what a string is written for.
    assert is_words('Two words.') and is_words('No.') and not is_words('Beat') and not is_words('minecraft:stone')
    src = ('class Demo{/* "not this" */static final String A="First line.";char q=\'"\';\n'
           'void f(){LOGGER.info("Not a line at all {}",1);say(p,"She said "+name+" was here.");\n'
           'throw new IllegalStateException("No such thing");}\n'
           'String B="""\n    A block\n    of two lines.""";String c="First line.";}\n')
    lits = scan_java(src)
    shown = [lit for lit in lits if not lit.skipped]
    assert [lit.text for lit in shown] == ['First line.', 'She said ', ' was here.', 'A block\nof two lines.', 'First line.'], shown
    assert shown[2].joined and shown[2].glue == 'name' and not shown[1].joined and shown[3].block
    assert shown[0].pre.endswith('A=') and shown[1].pre == 'say(p,'

    # Edits, on a small tree of the same shape.
    with tempfile.TemporaryDirectory() as tmp:
        root = pathlib.Path(tmp)
        java = root / JAVA / 'demo'
        (java / 'gametest').mkdir(parents=True)
        (root / 'tools').mkdir()
        (root / LANG).parent.mkdir(parents=True)
        (root / LETTERS).parent.mkdir(parents=True)
        (java / 'Demo.java').write_text(src, encoding='utf-8')
        (java / 'gametest' / 'DemoTests.java').write_text('class DemoTests{String s="She said ";}\n', encoding='utf-8')
        (root / LANG).write_text(dump({'block.the_oldest_house.lamp': 'Oil lamp', 'item.the_oldest_house.key': 'Key'}),
                                 encoding='utf-8')
        (root / LETTERS).write_text(dump([{'id': 'A01', 'title': 'A title', 'text': 'Dear you.\n\nYours.'}]), encoding='utf-8')
        (root / 'tools' / 'draw_demo.py').write_text("NAMES = {'lamp': 'Oil lamp'}\nOTHER = 'Key'\nAGAIN = 'Key'  # key\n",
                                                     encoding='utf-8')
        demo = JAVA + '/demo/Demo.java'
        batch = {'edits': [
            {'path': demo, 'old': 'First line.', 'nth': 1, 'of': 2, 'new': 'The “second” first line.'},
            {'path': demo, 'old': 'First line.', 'nth': 0, 'of': 2, 'new': 'The first\tfirst "line".'},
            {'path': demo, 'old': 'She said ', 'new': 'She said that '},
            {'path': demo, 'old': 'A block\nof two lines.', 'new': 'A block\n\nof three lines,\n  one indented.'},
            {'path': demo, 'old': 'Not in the file.', 'new': 'Nor now.'},
            {'path': demo, 'old': ' was here.', 'new': ' was here.'},
            {'path': JAVA + '/demo/gametest/DemoTests.java', 'old': 'She said ', 'new': 'x'},
            {'path': '../outside.java', 'old': 'a', 'new': 'b'},
            {'path': LANG, 'key': 'block.the_oldest_house.lamp', 'old': 'Oil lamp', 'new': 'Paraffin lamp'},
            {'path': LANG, 'key': 'item.the_oldest_house.key', 'old': 'Key', 'new': 'Door key'},
            {'path': LANG, 'key': 'item.the_oldest_house.key', 'old': 'Latch', 'new': 'Bolt'},
            {'path': LETTERS, 'key': 'A01.text', 'old': 'Dear you.\n\nYours.', 'new': 'Dear you.\n\nEver yours.'},
            {'path': LETTERS, 'key': 'A01.id', 'old': 'A01', 'new': 'B01'}]}
        (root / 'batch.json').write_text(json.dumps({'data': batch}), encoding='utf-8')
        out = io.StringIO()
        stdout, sys.stdout = sys.stdout, out
        try:
            code = apply([root / 'batch.json'], root / 'report.json', root)
        finally:
            sys.stdout = stdout
        results = json.loads((root / 'report.json').read_text(encoding='utf-8'))['results']
        states = collections.Counter(r['state'] for r in results)
        assert code == 2 and states == {'applied': 7, 'moved': 3, 'refused': 2, 'unchanged': 1}, (code, states, out.getvalue())
        after = [lit.text for lit in scan_java((java / 'Demo.java').read_text(encoding='utf-8')) if not lit.skipped]
        assert after == ['The first\tfirst "line".', 'She said that ', ' was here.',
                         'A block\n\nof three lines,\n  one indented.', 'The “second” first line.'], after
        assert 'Not a line at all {}' in (java / 'Demo.java').read_text(encoding='utf-8')
        lang = json.loads((root / LANG).read_text(encoding='utf-8'))
        assert lang == {'block.the_oldest_house.lamp': 'Paraffin lamp', 'item.the_oldest_house.key': 'Door key'}
        tool = (root / 'tools' / 'draw_demo.py').read_text(encoding='utf-8')
        assert "'lamp': 'Paraffin lamp'" in tool and tool.count("'Key'") == 2, tool
        assert json.loads((root / LETTERS).read_text(encoding='utf-8'))[0]['text'] == 'Dear you.\n\nEver yours.'
        said = [r for r in results if r.get('tests')]
        assert len(said) == 1 and said[0]['tests'] == [JAVA + '/demo/gametest/DemoTests.java:1'], said
        look = [r for r in results if 'look' in r]
        assert len(look) == 1 and len(look[0]['look']) == 2, look

    # The tree itself. A string that is not written back as it is read is left off the page, and said here; so is a JSON
    # file that is not laid out as this tool would write it (its edits are then refused). Neither stops the build.
    files, who, texts = inventory()
    shown = sum(1 for t in texts if files[t[0]][3] == 'java')
    words = sum(1 for rel, f in java_files(ROOT) for lit in scan_java(f.read_text(encoding='utf-8')) if not lit.skipped)
    notes = [f'{words - shown} strings left off the page: they are not written back as they are read'] if words != shown else []
    for path in (LANG, LETTERS):
        raw = (ROOT / path).read_text(encoding='utf-8')
        if dump(json.loads(raw)) != raw:
            notes.append(f'{path} is not laid out as this tool writes it; edits to it are refused')
    assert PAGE.is_file() and 'TEXT_REVIEW' in PAGE.read_text(encoding='utf-8'), PAGE
    assert texts and all(0 <= t[5] < len(CATEGORIES) and -1 <= t[6] < len(who) for t in texts)
    print(f'Verified the text review tool: {len(texts)} texts in {len(files)} files, {shown} of them Java strings; '
          f'{len(who)} people named.' + ''.join(' Note: ' + n + '.' for n in notes))


def main():
    ap = argparse.ArgumentParser(description=__doc__.split('\n')[0])
    ap.add_argument('--build', metavar='OUT')
    ap.add_argument('--repo')
    ap.add_argument('--branch')
    ap.add_argument('--trigger')
    ap.add_argument('--apply', nargs='+', metavar='BATCH')
    ap.add_argument('--report', metavar='OUT')
    ap.add_argument('--check', action='store_true')
    args = ap.parse_args()
    if args.check:
        check()
    elif args.build:
        build(args.build, args.repo, args.branch, args.trigger)
    elif args.apply:
        sys.exit(apply(args.apply, args.report))
    else:
        ap.print_help()


if __name__ == '__main__':
    main()
