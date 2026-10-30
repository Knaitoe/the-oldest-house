# Usher together, and the reader's own house — 0.4.183

The owner asked whether the Usher pass was designed with multiple players and with the House's personality read in mind. Stage B ([USHER_FALL_0_4_182.md](USHER_FALL_0_4_182.md)) had kept the night private to each reader. As a result, friends in the studio each saw their own Roderick, read alone and got their own Madeline. This pass gives them one night. It also lets Roderick and the account know the house the reader came from, and gives the open lid a climax of its own.

## One night for whoever is in the house

`labyrinth/UsherStory.java` now keeps the night in the house's shared state (`Night0507`).

- **Each reader's storm night.** It is every visit after the visit on which they first found the lid chosen (`LidKnown0507`). That covers the reader who chose, those who watched, and a reader who came later.
- **The storm is up for everyone.** While anyone in the house is on their storm night, the storm is up for all of them. A friend who has not reached their own night yet shares it.
- **One Roderick.** He sits in the chair for everyone, and whoever is in the room hears what he says, both over his head and in chat.
- **One reading.**
  - **Who reads.** A hand on the strewn books begins it for everyone who still sees him. The one who picked it up reads, and the others are told "*name* reads: …".
  - **Who hears.** Each passage is answered from the vault for all of them.
  - **Arriving late.** A reader who arrives mid-night joins it from there.
- **One Madeline.** She stands at the studio doors. Only that night's readers see her (`UsherNight0507` on her).
- **The fall.** It comes for everyone in it. Each reader remembers:
  - who read (`Reader0507`, `ReadAloud0507`);
  - who ran with them (`With0507`);
  - their own house (`Home0507`).
- **Each reader flees alone.** Getting back to the bank readies only the reader who reaches it.
- **Afterwards.** The night is over and the house stands again. A reader who has seen it fall no longer sees Roderick (`UsherStory.sees`, checked in the actor's native tracking), but a later reader still finds him in his chair.
- **An empty house.** If the house stands empty for ten seconds mid-night, the night is forgotten, and Madeline with it.

## The account, as each reader was in it

`UsherStory.account` writes the fall from where the reader stood:

- "I closed the lid" for the one who chose, "The lid had been closed" for the rest;
- "I read the old romance aloud" for the one who read, or "*name* read the old romance aloud to him, because he asked";
- "*names* ran with me" when others were in it.

Readers are named by their player names and never he or she.

**The co-op accounts** (`CoopAccounts.VAULT`) gain the fall for those who were not the first to finish. The first reader to read their own account holds the actor's account; the others hold one of these:

- **WITNESS:** heard the reading and the answers, ran when she came to the doors, and watched the crack from the bank with the actor;
- **PARTWAY:** came in after the storm began and heard the last of the reading;
- **AFTERMATH:** came back after the night it fell.

## The reader's own house

The House's read of the reader (HOME: haven, danger, runaway) now reaches Usher.

- **Roderick's line.** Before the lid, with any read of HOME, he says one thing about the house the reader came from:
  - **HAVEN:** "You were kept safe somewhere. I can see it on you. I envy you that more than I can say."
  - **DANGER:** "You know what it is to be afraid of the house you grew up in. I saw it when you came under the arch."
  - **RUNAWAY:** "You have left a house before. Then you know the causeway is longer than it looks."
- **The account's closing line:**
  - **HAVEN:** "All the way across I thought of a house that had never asked anything of me."
  - **DANGER:** "It was not the first house I had watched close over the people in it."
  - **RUNAWAY:** "I had run from a house before. I knew not to look back until I reached the bank."
- **His greeting.** It follows the House's existing settled address: "You came", "You came, old fellow", "You came, my dear" or "You came, old friend".
- **Without a read** nothing changes, as with every other tailored moment.
- **Reading the read never writes to it.**

## The open lid's climax

- **Who can bring it about.** Anyone in the house can now meet the open lid's ending, not only the reader who chose. She leaves the coffin for the wing on any reader's storm night.
- **The doors.** Once someone there has seen her standing in the wing, she comes to the studio doors while nobody is looking, and stays there.
  - The doors stand open in each reader's view.
  - Roderick says: "She is at the door. She was never in the coffin long enough to die. I knew it, and I let the house be quiet."
- **She never comes in.** The ending (`found_her_outside_the_open_coffin`) is unchanged.

## What stays

- One understanding source in the Witness pool. Witness stays 55 / 42 / two kinds / three endings.
- **Layout and protocol unchanged:** layout 51, protocol 101.
- **New builds only.**

## Tests

- **`LiteraryTests.theStormNightsReadingIsAnsweredFromTheVaultAndTheReaderFleesAsTheHouseFalls`**, rewritten for the shared night:
  - the lone reader's reading, answers, door and fall;
  - their account, with no one running with them;
  - Roderick hidden from them afterwards.
- **`LiteraryTests.readersInTheHouseTogetherShareOneStormNightAndEachFleesOnTheirOwn`:**
  - one Roderick seen by both;
  - the friend who watched the lid gets the storm night too;
  - one reading with both in it, and one Madeline seen by both;
  - the fall for both, each remembering who read and who was with them;
  - each flees separately;
  - each account says it from their side;
  - a later reader still finds him in his chair.
- **`LiteraryTests.roderickAndTheAccountKnowTheHouseTheReaderCameFrom`:**
  - three distinct HOME lines;
  - three closings added to an otherwise unchanged account;
  - nothing without a read;
  - the plain greeting without a settled self-read.
- **`LiteraryTests.onAnOpenLidSheIsSeenInTheWingAndThenStandsAtTheStudioDoors`:**
  - unwatched, she goes to the wing;
  - seen there, the ending is ready;
  - unwatched again, she comes to the studio doors and stays.
