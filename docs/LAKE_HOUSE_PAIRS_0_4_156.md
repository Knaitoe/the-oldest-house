# The lake house in pairs — 0.4.156

The owner's October 28 request: "I'd like for a second player to be able to inhabit the role of Tess. If a third tried to join it should trigger their own instance with Tess. Four would have two instances with two players, a fifth would be on their own again."

The lake house now has copies, like the private scenes (`docs/SCENE_COPIES_0_4_153.md`), and each copy is two readers': the diver whose story it is, and a partner in Tess's place.

## Who goes down with whom

- **The first reader** takes a lake as its diver, with Tess (or Theo, or Tess as a friend, as in `docs/LAKE_HOUSE_PARTNER_0_4_152.md`).
- **The second reader** joins them as their partner, in Tess's place. A reader joins a diver who:
  - is in their own lake now;
  - is alone there;
  - has a story under way (it can still be dealt to them and its ending has not landed);
  - has nobody going down with them yet.
- **A third reader** finds both lakes' places taken and gets a lake of their own, with Tess. While it is built, their door stays shut ("Another is being made ready for you"). The House builds the next lake ahead while two or more players are on the server, so this is rarely a wait.
- **A fourth reader** joins the third. **A fifth** is on their own again, and so on, up to the server's `sceneCopies` cap.
- **Pairs are saved.** The partner goes down with the same diver on every visit until the diver's ending lands. Nobody else can take the partner's place, even on a visit the partner misses.
- **Readers partway through before 0.4.156** keep the original lake, as with every scene that has copies. A new reader may join one of them as a partner. Two such readers no longer share the original at once: the second finds it taken, as at the other private scenes.

## In Tess's place

- **How the partner looks.** To the diver, the partner is Tess, or the young man in her place, or Tess as a friend, whichever the diver's read chose. The partner sees themselves that way in third person too. The figure follows the player exactly: where they stand, which way they face, their stride, sitting in the canoe and swimming. Their own name and skin are not shown. Nobody else is ever near enough to see them, since each pair has its own lake.
- **Tess stays away.** With a partner there, Tess does not come. If she had already come for the diver alone, she leaves without a word (no "I'll watch the compressor"), vanishing as soon as the diver looks away. If the partner leaves partway through a visit, Tess comes back for the rest of it, out of the diver's sight. She has to on the helmet visit, where somebody has to mind the compressor.
- **Tess's lines come to the partner in grey,** to say in their own words or not:
  - On meeting, once: "To <diver>, you are Tess. Her lines come to you in grey: say them in your own words, or don't."
  - Each visit, on the dock, Tess's greeting for that visit, for example: "Tess might say: “I didn't think a first date would come with a culvert. We can go through that tunnel. But if there's something down there, let's not ask how.”"
  - Whenever Tess would speak to the diver (when the diver tests the rule, for example), the line goes to the partner instead of the diver.
  - On the visit after the ending, Tess's goodbye for that ending.
- **The house is the diver's.** Its lights, the dresses, the wax figure and the dawn after staying are the diver's record, and the partner sees them too. The house breathes for both of them once it is lit.
- **Their own gear.** The partner picks up their own flashlight by the boathouse door, and a second helmet or second tanks on the raft, by the diver's visit.
  - Either of them can mind the compressor. With two readers in the lake, it runs only while somebody stands by it with their head out of the water. This is Tess's job on the second visit, and the greeting says so.
  - The partner rides in the diver's canoe, as Tess does; they have no canoe of their own.
- **One rule.** The pair keeps one rule. A partner who tests it tests it for both: the second test is the diver's broken rule, and both hear someone walk upstairs.
- **The story's choices are the diver's.** The partner can go everywhere and touch everything, but only the diver's own actions end the story: the half door, the chair at the table, the figure.

## When the diver's ending lands

- **The partner has an account of their own**, from Tess's side, and its Witness credit when they read it, like any reader. A partner there at the ending comes up with the diver: beside them on the raft, or in the empty lake. A partner elsewhere is told: "Out at the lake, <diver> has come up for the last time. Your account waits in the boathouse."
- **The account** names the diver and the date or the weekend, then ends as Tess's goodbye does:
  - *The lights:* "Every light in the house came on at once, under the raft. <diver> came out through the half door and didn't look back. It's still lit. I'm not going back down. I don't think <diver> should either."
  - *The raft:* "<diver> didn't come up. I sat on the raft until it got light, and then <diver> was lying beside me, wet through. They tell me it was days. I can't do that again."
  - *The empty lake:* "<diver> went back down to say hello to whoever lived there, and then there was nothing under us but water. I came out twice more on my own and sat in the canoe and looked down. There's nothing there. I have to stop coming."
  - It ends on the rule, if the pair broke it: "We tested the rule. Nothing came loose, but someone walked upstairs afterward. We had asked how."
- **The Witness line:** "<diver> and I went out on the lake on our first date and found a house on the bottom of it. Every time we went down it let us a little further in. We kept to one rule and didn't ask how. In the end <diver> went back to meet whoever lived there, and I waited on the raft." (For friends: "one weekend, the way friends do".)
- **The diver's own account** names their partner as Tess, as it always has. The diver has met them on the dock, so their account opens with Tess.
- **Keepsakes.** The diver keeps the ending's artifact as before. The partner has none of their own.

## When the diver is not there

- **Not dealt.** A partner is not dealt the lake while their diver is on the server but somewhere else.
- **The door waits.** If the partner reaches the lake's door anyway (their diver is offline, or left after the door was dealt), it stays shut: "The lake waits for <diver>. Try the door again to go down without them."
- **Going alone.** Trying the door again within ten seconds lets them go. The pair ends, the partner's record starts afresh, and they take a lake of their own as its diver, with Tess. Without this, a partner whose diver never came back could never finish the lake.
- **Once the diver's ending has landed,** the partner comes back to the same lake like anyone else, to read their account.

## How it is built

- `SceneInstances` gives the lake house copies and chooses as above (`lake`, `lone`). It saves pairs as partner → diver (`Partners`) and answers `diverOf`, `partnerOf` and `pairOf`. A pair never makes its own lake count as taken.
- Copies are built by the lake house's own builder, at its fixed height. The slot grid finds a lake's copy from position by the lake's own bounds, not its slot's layer. Each copy has its own compressor, canoes and lent gear, and its clocks run for every copy that stands. A reader in the empty lake is still sent there, never to a copy.
- `LakeHousePairs` is the partner's part:
  - the prompts, the shared views and their gear;
  - readying their ending when the diver's lands;
  - the door's wait and the second try;
  - the dealing gate, refreshed every two seconds.
- The dresses and the figure are shown to the diver's partner (`LakeHouseDress`, `LiteraryActor`), and the client draws the figure for them.
- `LakePartnerPayload` (partner, diver, role) is sent to both readers every second they are in the lake together. `LakePartnerLook` on the client draws the partner as a client-only cast figure and lets the partner's client draw the diver's figure. The look lapses three seconds after the payload stops. **Protocol 89.**

## Tests

One native case joins the lake house cases:
- **Pairing:** the first reader takes the lake; the second joins as partner and has no canoe of their own; a third waits while a second lake is built.
- **Tess and the house:** Tess stays away; the diver and partner have met on the dock; the partner takes their own flashlight; the dresses are seen by the partner and nobody else; the lit house is lit for the partner.
- **The ending:** the diver's ending readies the partner's own, from Tess's side, and they come up together; reading it gives the partner the Witness credit and its line.
- **More readers:** the third reader's door opens onto a lake of their own, built like the first; the fourth joins the third; a fifth waits for a lake of their own.
- **A waiting partner:** with the third away, their partner is not dealt the lake and its door waits for them; a second try lets them go alone, never into their diver's lake.

The client look has no native test; the visual proofs do not cover it yet.

No layout or Witness pool change: 54 / 41 / two kinds / three endings. Protocol 89.
