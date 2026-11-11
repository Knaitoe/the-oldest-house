# Drisking on the second day, after she is taken (0.4.458)

Note 18 of the owner's playtest of 10 November (docs/BORRASCA_PLAYTEST_2026_11_10.md): "Dialogue should change on second day,
after Kelly/friend is taken". The screenshot was the Sheriff, after she was taken, still saying "You two keep this side of Marker
Two when the wind comes up."

0.4.444 gave the Sheriff three lines for a child who came down alone. Everyone else in the town went on saying what they said
the day before. This is the rest of the town.

## When

From the moment the friend is taken on the trail until the second visit ends (`BorrascaPeople.taken`). Nothing changes before
that, on the first visit, or years on.

## How they speak

Nobody says what happened and nobody asks. The reader does not speak, so each of them has only what they can see: a child who
went up the trail with a friend and came down without her. Each of them does what they do about it. That follows the owner's note
on the houses (note 12): many know what the town does and accept it, and some do not and bite their tongues.

Each says their lines in order, and then the last of them again: they have said what they are going to say.

| Who | What they say, in order | What it is |
|---|---|---|
| Lorna, at the diner | "Just the one of you? Sit at the counter, hon. I'll bring you a milk." / "Drink that. Then you go straight home, and you stay in tonight." / "Don't go asking in here, hon. Folks are eating." | She feeds the child and sends them home. |
| Mr. Ames, at the store | "Just you, then. She had a dime of candy on the book. I'll rub that out." / "I'll put her sheet on the board in the morning. The bake sale's over anyhow." / "Store shuts at five. Be indoors before it does." | He closes her account the same hour and makes room on his board. |
| Mr. Rowe, at his counter | "I had my smoke out front, same as every day. I didn't look up the street. I never do." / "Hands in your pockets. And go home." | He stands out front every afternoon, and did not look. |
| Mr. Rowe, on his break | "I'm on my break. I'm not looking up the street. Go on home." | |
| Kurt, at the gas station | "Two of Prescott's men walked up past the pumps before noon. Didn't buy a thing. They never do." / "You should tell the Sheriff. He's right there on the square. That's what he's for." | He says what he saw and does not know what he is saying. The Sheriff has just said nobody was on that road. |
| Mr. Fenn, at the drugstore | "No running in here. ...Sit at the fountain a minute." / "Dr. Hale will write her mother something to sleep on. He always does. I'll have it made up by five." / "Go on home now. The stool will be here tomorrow." | There is a prescription that follows, and he knows when it is due. |
| Mr. Webb, at breakfast | "We've sat right here since eight. We didn't see anybody go by. Neither of us." / "That's for the Sheriff. It isn't ours to talk about, and it isn't yours." | |
| Mrs. Webb | "Her poor mother. ...Dr. Hale will see she's looked after." / "I want to go and get Paul from his grandma. Right now." | |
| Earl, in the cell | "There were two of you this morning. I can still count." / "He locked me in before noon. I hadn't had a drop. Ask him why. No. Don't ask him." / "Don't give him her name. He'll write it down his way, and then that's how it is." | He was put where he could not see the street. |
| Mr. Dorsey, on his round | "Came down on your own, did you. ...I didn't see it. I just carry the mail." / "I'll hold the Garritys' mail at the office a while. That's how it's done." | |
| Gus, at the bar | "Out. And go home by the street. Not by the trail." (and, walking them out, "I said out. Go home.") | He still puts a child out. |
| Kendra, in the street | "You came down by yourself." / "I'm not asking the Sheriff. I asked him about Patrick." / "I'll go and tell her mom. Somebody ought to who isn't him." | Patrick went the week before. |

## Mrs. Odell's rule

Mrs. Odell gives a child one lollipop a visit: "One's the rule." On that day she gives a child who has had theirs another ("One's
the rule. ...Here. Take another. I won't count it."), and a child who has not had one two at once ("Here. Take two today. I won't
count them."). After that she has nothing else: "That's all I have for you. Go on home."

## Small things

- Lorna no longer tells a child who is short "Come back with your friends" on that day.
- What Kendra says after the taking is not her telling about Patrick's bicycle, so it does not put that sentence in the reader's
  account.
- The lines' places are kept by the reader (`SaidAfter<phase>`, `WrenAfter`, `LollipopAfter`) and cleared when a visit begins.

## What is not changed

The Sheriff's three lines (0.4.444), the payphone's answer from his office, Mr. Prescott at the work entrance, and everything
anyone says on the first visit or years on.

## Tests

- `theTownsDayHasItsPatronsAPayphoneWithNoOpenCaseAndFriendsWhoPoolTheirCoins`: on the second day, once she is taken, each of the
  nine at work says their own lines in order and then the last again, and none of them says "You two", "your friends" or "your
  folks"; Gus sends the child home by the street; Mr. Rowe on his break is not looking up the street; Mrs. Odell gives two at once
  and then nothing, and a child who had theirs is given one more, once.
- `townChildrenBecomeReadableMissingPosters`: Kendra says her three lines in order and the last again, and that is not recorded as
  her telling about the bicycle.

No layout, protocol or Witness change.
