# The list, part 5a: night two, the den and the field (0.4.720)

The first part of night two (`docs/THE_LIST_PLAN.md`, part 5). Night two is Amber's: the den, the tapes, the tests on the field and the
tower, the notebook's rules (some of them wrong), and the first name going out. This part builds night two's beginning, the den, the
notebook, and the first test on the football field. The rest of the night follows.

## Night two begins

The one who went in on night one, back through the House's door into a town whose night one is over, begins night two: ten o'clock, in
their own kitchen. They are told Amber's page said her back door, after ten. The body is theirs, as it was. Amber is in her den, standing
in front of the television. The five are at their own places.

The town's clock runs from ten as on night one, while someone is in it.

## The den

1. **The back door.** The one who went in puts a hand on Amber's back door, and she has it open before the knuckle lands. Another reader's
   hand finds it locked, as it was.
2. **Talked to.** She humors them, a line a word: she watched three tapes last night to get ready; if they are what they say, there are
   rules, there are always rules. She hands over her notebook, the rules from the tapes already written in it, and says the rest get
   tested. The first test: the football field. She runs, they walk; if she gets away, they are just somebody in a sack. Then she sets off.
   Anyone else who talks to her is told to keep it down, her mother is asleep.

## The notebook

A spiral notebook, written in Amber's hand, the one who went in's own: given once, never again. Its first page is the rules from the tapes
(only at night; light keeps him back; he can't come in unless you ask him in, which she knows is vampires and writes down anyway). Its
second page fills with the rules the tests write, as they are written. The rules are kept in the reader's record; the notebook they carry,
wherever it is on them, is rewritten with each new one. Lost, it is not given again; the record keeps the rules.

Held in either hand, the notebook holds the pull off: half as strong, on top of what standing in light already does.

## The field

1. **The walk.** She goes out through the den to the hall, out her front door (shut again behind her once she is in the street), west
   down Seventh, through the field's gate and down to the south end zone, where she turns up the field and waits. Talked to on the way, she
   says where she is going; at the end zone, that she is not counting until they are beside her. If something in the road stops her, she
   is set on at her next step after five seconds.
2. **The run.** With the one who went in within six blocks of her, she counts three and runs the length of the field.
3. **Kept up.** If they are within eight blocks of her at the far end, she says it is not fair, they walked the whole way and she ran; she
   writes the first rule (he can't run, he doesn't need to, you don't get away from him) and hands the notebook back. The next test is the
   field house's door, which has a lock on it.
4. **Lost.** If they are not, she supposes they are just slow, walks back to the south end zone and waits to try again.

## Single and multiplayer

- **Alone**, the one who went in is the den's only reader: Amber, the notebook and the test are theirs.
- **With others**, Amber is one person in the town; everyone near her sees and hears her. Only the one who went in opens her back door,
  gets the notebook and is run against; the other readers' side of night two is part 8.
- **Spectators** open nothing and are given nothing.

## Retreat

The House's door is open all through this part of night two: nobody is held.

## Ended

The night ended by an operator (`night off`), Amber's back and front doors are shut, her walk forgotten, and she is back at her desk.

## Made as

- **`TheListDen`** (new): `DEN_AT` (her place in the den), `END_S` and `END_N` (the field's end zones), `TO_FIELD_WAY` (her way, the
  front door opened at its fourth step and shut at its seventh), `WALK` .75, `RUN` 1.0, `START` 6, `KEPT` 8, `STUCK` 100, `COUNT` 20;
  `owed` and `begin` (night two round the one who went in; `begin(player, base)` for an operator or a check), `door` (her back door),
  `talk` (her den lines in turn; the notebook at the third; the walk after the last), `notebook`, `book`, `isNotebook`, `holding`, `write`
  and `rules`, `tick`, `toField`, `field`, `atField` (for a check), `reset`, `running`, `hour`. Where she is on her walk is kept per town and
  never saved; a town picked up again mid-walk finds her at the end zone.
- **`TheListNight`**: phases `HOME2` 7, `DEN` 8, `TO_FIELD` 9, `FIELD` 10, `RAN` 11 and their names; a reader owed night two begins it on
  arrival; the clock runs to first light on night two; Amber is the den's in `talk`; `end` puts the den away.
- **`TheListPeople`**: the five are at their places on night two; Amber's place on night two is the den, and while she is out on the test
  she is wherever she has got to.
- **`TheList.click`**: Amber's back door on night two is the den's. **`TheListBody.pull`**: the notebook in hand halves the pull.
- **`TheListCommands`**: `night two` begins night two round the operator, night one put behind them.
- The notebook is a written book, its pages in the plain hand, marked with its reader. No new sound, block or art.

## Tests

`TheListTests.nightTwoDen` (new, batch `the_list_den`): night two begun round the one who went in: ten o'clock, told where Amber said, the
body theirs, Amber in her den. The other reader's hand on her back door finds it locked; the one who went in's opens it. The other reader,
talking to her, is told to keep it down and given nothing. The one who went in hears the rules, gets the notebook with the tapes' rules,
hears the first test, and she sets off; on the way she says where. She walks off; set down at the end zone, she counts and runs; kept up
with, she says it is not fair and writes the rule: in the record, and on the notebook's second page. In hand, the notebook is held. The
next test is the field house's door. Ended, both her doors are shut.

## Every new line

Grey cues on the action bar, to the one who went in:

- "Ten o'clock. Amber's page said her back door, after ten."
- "Amber has the back door open before your knuckle lands."
- "Amber hands you a spiral notebook. The first page says THE RULES."
- "Amber writes in the notebook against her knee and hands it back."

Amber, over her head, to the one who went in, in turn:

- "You came to the back. Good. Keep it down, my mom's asleep."
- "I watched three of these last night to get ready. If you're what you say you are, there are rules. There are always rules. That's the
  whole point."
- "Here. I wrote down the ones from the tapes already. We test the rest."
- "First one. The football field. I run, you walk. If I get away from you, you're just somebody in a sack."
- "South end zone. Come on."

On the way: "South end zone. Keep up. Or don't, that's the test." At the end zone: "Stand next to me. I'm not counting till you're here."
Her count: "On three." "One." "Two." "Three!" Kept up with: "That's not fair. You walked the whole way. I ran." Lost: "Huh. Maybe you're
just slow. Again." After: "Next one's the field house. It has a lock on it."

To anyone else who talks to her: "Shut the door behind you. My mom's asleep."

The notebook, titled "Amber's notebook":

- First page: "THE RULES (from the tapes)", then "Only at night." "Light keeps him back." "He can't come in unless you ask him in. (That's
  vampires. Writing it down anyway.)"
- Second page, "TESTED": "1. He can't run. He doesn't need to. You don't get away from him. (The field.)"
