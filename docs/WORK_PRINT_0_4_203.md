# Work print (0.4.203)

The cursed film, approved by the owner on 30 October (`docs/CURSED_FILM_PROPOSAL.md`) with three notes: the costume is found in
the shop, the ghost girl is an Easter egg and no more, and there is both a way to succeed and a way to fail. Slot 129,
`work_print`, literary, multi-visit, indoors. Witness: "Work print", a survival story; the pool grows to 58 sources and the
quota to 44.

## The place

Night Owl Video, a rental shop on a closed street after hours (`WorkPrintShop`). The reader comes in by its front door: carpet
tiles, tape shelves down both walls and two runs back to back, the returns bin under its slot by the door, four posters for
films that do not exist, and the counter across the back with the till, the rental card (the source), the phone and the
returns ledger (the account) on it. The film's monster hangs on a mannequin at the end of the counter.

Behind the counter is the back room: a TV cart with two decks, one over the other, the CRT on top showing snow, two folding
chairs and a box of three blank tapes. Off it, the sound booth (foam walls, the mixing desk with the sound log, a microphone)
and the edit bay (pegboard, the flatbed with its work print, the splicer, two light tables, the rack of cans, the continuity
photographs, the script and the call sheet). Through the edit bay is the set: a studio with a black ceiling and the film's
hallway built of papered flats, its bulb hanging over a chalk mark, the House's camera on a tripod at the near end, pointed
down it. The way on is the crew's door in the set's far wall.

One `work_print_fitting` block draws every fitting (31 kinds, 496 states), on seven materials; the art, the film and the
costume's sheet are drawn by `tools/draw_work_print_art.py` (103 textures, 80 models and states), and nine wordless cues are
made by `tools/generate_work_print_audio.py`. Both run with `--check` in CI.

## The film

*Hallstand* is original: the call sheet says Day 19 of 18; the script's last scene, 88, in which the figure on its mark turns
round to face the camera, was never shot, because the actor in the suit would not do the turn. The tape in the returns bin has
no label and no box. Played on the cart's first deck it runs ninety seconds on that reader's screen only (`WorkPrintClient`
composes it frame by frame from the atlas and runs it through a tired tape). Nobody else in the room sees it play. A reader who
looks away from the screen for more than a fifth of it has not watched it.

Watched through, the shop's phone rings. There is no voice, only a count: seven. The count runs on House days (the Overworld's
days, `HouseCalendar`). On each new day the phone rings when the reader comes back in.

From the second viewing on, the House has been filming them. Once in each ordinary hall they stay in for ten seconds, it takes
a frame of them from behind (`HouseLens.fromBehind`, a server-side ray-cast camera through the real blocks). Those frames
replace shots in their own film: a room at the second viewing, the hall at the third, and from the fifth the figure in the
corridor is them.

## Succeeding

Either stops the count, readies the account and makes the costume the reader's own (NOT FOR HIRE crossed out, their name
under it).

- **Pass it on** (`passed_it_on`). A blank in the second deck and the tape in the first dub a copy. Another reader who plays the
  copy and watches it through takes the count on; it is theirs from that moment. Alone, a reader may carry their copy out into
  the Overworld and give it to a villager, who stands and watches nothing. Seven House days later the villager is gone from its
  bed and the tape lies there. A count passed while its reader is offline is waiting for them when they come back.
- **Finish the film** (`finished_the_film`). Wearing the costume, on the mark under the bulb, back to the camera and still for
  three seconds, then turned round to face it: the tripod's camera films the turn. Another reader can lift the House's camera off
  the tripod and film it instead, and both are named in the credits. The exposed reel (Scene 88) is spliced into the work print
  at the splicer, and the whole film is run once on the flatbed, watched through to the end of 88.

## Failing

The count runs out. The next time the reader is in one of the House's ordinary halls, the film takes them: the grain and
tracking lines come over their sight and they come to at the far end of the set's hallway, under the bulb. They walk out of it.
After that the film follows them, privately and harmlessly, until they come back and stop it properly: now and then a frame of
their own footage flashes over their sight, the grain comes back, or a phone rings somewhere near them. The account remembers
it.

## The rest

- **The costume's gift.** While its wearer stands still for two seconds, nothing that hunts by sight takes them for prey (the
  carcass ridge's killer is the first to honour it; `WorkPrint.unseen`).
- **The stained tape.** After the second viewing, once, there is a second tape in the returns bin with nothing on it but a
  ring-shaped water stain. Watched, it is snow, and once a well in a clearing. That is the Easter egg; it changes nothing.
- **Handprints.** A reader who has heard the last call and stays watching the snow sees hands pressed to the inside of the
  glass.
- **The dealer.** While a reader's count runs, the shop is dealt to them far more often (three times the begun weight).

## Multiplayer

The shop is one for a party; unrelated readers have their own copies. Each reader's count, footage, viewings and phone calls
are their own; one screen plays one viewing at a time. The House's camera stays in the shop. Two readers can finish one film
together (one in the suit, one on the camera), and a copy passed between two readers is the only way for the count to leave one
of them without a villager.

## Literature

The rental card is printed by the shop with the reader's own name on the borrower's line; on its back, in the shop's biro,
"Do not watch it in the shop", and under that, in another hand, "Too late". The film's papers (the call sheet, the script's
blue pages, the continuity photographs, the cans, the sound log) are original. The account is the shop's returns ledger,
written from the reader's record (how many times they watched, which papers they read, whether the count ran out, how they
stopped it, who held the camera or took the copy), with a note from W. Darrow, who wore the suit, folded into it in a careful
old hand. The Witness line, the co-op lines, the miniature note and the House of Leaves place name ("the video shop") follow.
House writing never names the reader's gender, a colour of theirs or an age.

## Tests

`WorkPrintTests` (literary): the shop is a literary survival source and the card has the reader's own name; the tape watched
through starts the seven-day count, the phone rings and is answered with the count, and the dealer brings the shop back; a copy
dubbed on the two decks and watched by another reader passes the count on and gives the dubber the costume; the turn shot on the
set in the costume and spliced into the work print finishes the film, and the ledger resolves it; a count run out in an ordinary
hall takes the reader onto the set. The architecture case holds the shop's carpet, the card, the CRT and the mark, and exports
the shop under its ceiling.
