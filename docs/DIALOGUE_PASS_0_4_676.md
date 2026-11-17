# The dialogue pass (0.4.676)

The owner, 17 November, with a screenshot of the Kept's line "You're new. Nobody's new.":

> a lot of your dialogue has redundancies common in AI. please review the scope and revise as necessary.

## What was looked for

Every string in the game's code that reads as a line or a description was read through a scan (kept outside the repository) that flags five shapes. Each flag was then read by hand.

- **The echo.** A sentence that only says the one before it again. "Nothing to open them with. Nothing to open them with."
- **The correction.** A short sentence that turns the last one round. "It never lied. It never had to." "Not yours. Ours."
- **Not this, but that.** "It isn't spoilt. It's old." "It isn't a purr. It's someone saying the word."
- **The paired negative.** "No storm. No ground." "Not the shed. Not the knife."
- **The stack of fragments.** Three or more two-word sentences in a row standing in for one thought.

## What was kept

Repetition is how frightened, winded or excited people talk. These shapes stay where a person would really say them that way:

- someone shouting under pressure ("Get off me. Get off.", "Brake! Somebody get on the brake!");
- a child talking ("I brought candy. I ate the candy.");
- a joke that needs the turn ("I'm not stuck: I'm comfortable.");
- a description where the second sentence adds a fact rather than an effect ("The shutters are screwed shut from this side. The screws are painted over.").

Lines the owner supplied in a script (for example Vera's "Watch his hand. Not this one. His.") are not touched.

## What changed

Each line below is given as it was and as it is now. No line's meaning or speaker changed. Lines quoted in tests changed with them.

### The Kept

- `KeptTexts`
  - was: "You're new. Nobody's new."
  - now: "You're new. How did you get in here?"
- `KeptTexts`
  - was: "Nothing to open them with. Nothing to open them with."
  - now: "Cans. And nothing to open them with."
- `KeptTexts`
  - was: "I know what's behind that door. Nothing. Nothing at all. Let me go into it."
  - now: "I know what's behind that door. Nothing. Let me go into it."
- `KeptTexts`
  - was: "Sit down. There's no hurry. There's never any hurry."
  - now: "Sit down. There's no hurry."
- `KeptTexts`
  - was: "Who let you in? Nobody lets anybody in."
  - now: "Who let you in? That door doesn't open."
- `KeptTexts`
  - was: "Have you eaten? Today, I mean. Have you eaten today?"
  - now: "Have you eaten today?"
- `KeptTexts`
  - was: "Are you real? Say something. Say anything."
  - now: "Are you real? Say something."
- `KeptTexts`
  - was: "It never lied. It never had to."
  - now: "It never once lied to us."
- `KeptTexts`
  - was: "I remember the quiet. I remember it the way you remember food."
  - now: "I remember the quiet the way you remember food."
- `KeptTexts`
  - was: "We'll go round. We know a way round. You go under."
  - now: "We know a way round. You go under."
- `KeptTexts`
  - was: "Will it notice? When I've gone, will it notice?"
  - now: "When I've gone, will it notice?"
- `KeptTexts`
  - was: "I want to stop. Can we stop."
  - now: "Can we stop."

### Drisking

- `BorrascaGames`
  - was: "Nobody beats Kelly. Nobody."
  - now: "Kelly hasn't lost a game all summer."
- `BorrascaPeople`
  - was: "Pie's chess today. Nobody orders the chess."
  - now: "Pie's chess today. I'd have the coffee."
- `BorrascaPeople`
  - was: "Open till ten. Not much open past that up here."
  - now: "Open till ten. After that it's the gas station on the highway."
- `BorrascaPrescott`
  - was: "I can put you on nights tomorrow. Nobody on nights goes short."
  - now: "I can put you on nights tomorrow. Nights pays better."

### The closing hall

- `ClosingHallTexts`
  - was: "KAREN: You want to know who came out last. Nobody came out last. "
  - now: "KAREN: You want to know who came out last. "
- `ClosingHallTexts`
  - was: "Chad lets go of you at the hole. "Not after you. Not in there." He won't follow you in."
  - now: "Chad lets go of you at the hole. "I'm not going in there.""

### The House on the Corner

- `Corner`
  - was: "We went in yesterday and nothing happened. Nothing ever happens."
  - now: "We went in yesterday and nothing happened. We just stood in the hall."
- `Corner`
  - was: "My nan says nobody has ever lived there. Not ever. It was built like that, empty."
  - now: "My nan says nobody has ever lived there. It was built like that, empty."
- `Corner`
  - was: "You're new. Do you want to see it? Nobody lives there. Nobody ever has."
  - now: "You're new. Do you want to see it? Nobody's ever lived there."

### Costume Night, the Last Stop, Movie Night

- `CostumeNight`
  - was: "Lyle. Lyle, you didn't shave. Nobody's scared of a beard."
  - now: "Lyle, you didn't even shave. That's the whole costume?"
- `ElkLotNight`
  - was: "It came over the fence. Nothing comes over that fence."
  - now: "It came over the fence. That fence is six foot."
- `ElkLotNight`
  - was: "Settle up when you're done, hon. Nobody's done yet."
  - now: "Settle up when you're done, hon. No rush."
- `MovieNightCast`
  - was: "You walked round? Nobody walks round."
  - now: "You walked round? It's a mile by the road."

### Ghosts set, Last Orders, Night Operator, People Lights

- `GhostsConversations`
  - was: "I'd like to talk to each of them on their own. Not all together like this."
  - now: "I'd like to talk to each of them on their own."
- `LastOrdersTexts`
  - was: "Never died once before that. Not once."
  - now: "Never died once before that."
- `KeptTexts, NightOperator`
  - was: "Nothing. Nothing at all."
  - now: "Nothing."
- `PeopleLightsTexts`
  - was: "Evening. Don't get up. Nobody get up."
  - now: "Evening. Don't get up."

### The lodging house

- `LodgingTexts`
  - was: "Nobody's holding me. Nobody's holding me now."
  - now: "Nobody's holding me now."
- `LodgingTexts`
  - was: "Not back in there. Not back in there."
  - now: "Not back in there. Please."

### The cabin, the cellar, the Mother

- `CabinBargain`
  - was: "It's our world that ends tonight if you don't. Not yours. Ours. You'd go home with both hands."
  - now: "It's our world that ends tonight if you don't. You'd go home with both hands."
- `CabinBargain`
  - was: "It ends for us, then. Not for you. Never for you."
  - now: "It ends for us, then. You go home either way."
- `CellarDialogue`
  - was: "Not with your nails. Not like him. Use the blade."
  - now: "Use the blade. He's been at me with his nails."
- `MotherOfStrays`
  - was: "Something loved. Not something you can spare."
  - now: "Something you loved. I'll know if you didn't."

### Pell house

- `PellTexts`
  - was: "She had a cane? Nobody said she had a cane."
  - now: "She had a cane? Since when?"
- `PellTexts`
  - was: "I haven't heard a dog. Not once."
  - now: "I haven't heard a dog bark once."
- `PellTexts`
  - was: "Tilly's in Nessa's bed. It fits, Gail. It fits."
  - now: "Tilly's in Nessa's bed. It fits her, Gail."
- `PellTexts`
  - was: "Well. Not trouble, Gail. Not really."
  - now: "Well. Not trouble, Gail."
- `PellTexts`
  - was: "It wasn't chasing me. I was chasing it."
  - now: "I was the one chasing it."
- `PellTexts`
  - was: "It's only been a week. Give it a bit longer. Give June a bit longer. That's all I'm asking."
  - now: "It's only been a week. Give June a bit longer. That's all I'm asking."
- `PellTexts`
  - was: "Nowhere's going to be home for a while. Give it a week. Give June a week. That's all I'm asking."
  - now: "Nowhere's going to be home for a while. Give June a week. That's all I'm asking."
- `PellTexts`
  - was: "Not after last night. Not yet. Give it a bit longer. That's all I'm asking."
  - now: "Not after last night. Give it a bit longer. That's all I'm asking."
- `PellTexts`
  - was: "One tile of the band does not match the rest. It is newer, and not quite the same."
  - now: "One tile of the band is newer than the rest, and not quite the same."

### Pinehook

- `PinehookTexts`
  - was: "It's going to be fine. It's a bit. Nobody gets hurt in a bit."
  - now: "It's going to be fine. It's a bit. I've done this before."
- `PinehookTexts`
  - was: "Get out of the way. This isn't about you. It was never about any of you."
  - now: "Get out of the way. This is between me and him."
- `PinehookTexts`
  - was: "I was in one of these. Not this one. One like it."
  - now: "I was in one like this once."
- `PinehookTexts`
  - was: "Nobody's opening anything down here. Nobody."
  - now: "Nobody's opening anything down here."

### Bike night, the King, the replacement, the own light, Usher, Second Person

- `BikeNightTexts`
  - was: "A few more years and we can drive. Then nobody can chase us. Nobody on bikes, anyway."
  - now: "A few more years and we can drive. Then let them try and chase us."
- `KingTexts`
  - was: "It isn't spoilt. It's old."
  - now: "It's old, that's all."
- `ReplacementTexts`
  - was: "It purrs. It isn't a purr. It's someone saying the word under their breath."
  - now: "It purrs. Then you hear that it's someone saying the word under their breath."
- `OwnLight`
  - was: "It stops. It is not level with you any more. It is behind you,"
  - now: "It stops. It is behind you now,"
- `UsherStory`
  - was: "Do not open the window. The light is not from the sky. It is coming off the house."
  - now: "Do not open the window. The light out there is coming off the house."
- `SecondPerson, SecondPersonStory`
  - was: "Never mind. Never mind."
  - now: "Never mind."

### Bike night (more)

- `BikeNightTexts`
  - was: "If Tully comes, we don't say anything. That's the plan. That's the whole plan."
  - now: "If Tully comes, we don't say anything. That's the plan."
- `BikeNightTexts`
  - was: "I'm fine. I'm just standing. Standing is a thing people do."
  - now: "I'm fine. I'm just standing here."
- `BikeNightTexts`
  - was: "My cousin ate a whole pie once. A whole one. He said it was worth it. He didn't look like it was worth it."
  - now: "My cousin ate a whole pie once. He said it was worth it. He threw up in the car."
- `BikeNightTexts`
  - was: "The Hopper. You can stand somebody on those pegs. Not me. Somebody."
  - now: "The Hopper. You can stand somebody on those pegs. Not me, though."
- `BikeNightTexts`
  - was: "Oh. The Rattler. No, take it. I said take any. I meant it. Mostly."
  - now: "Oh. The Rattler. No, take it. I said take any."
- `BikeNightTexts`
  - was: "Good seesaw. That was a good seesaw."
  - now: "Good seesaw."
- `BikeNightTexts`
  - was: "Don't look back. I looked back. It didn't help."
  - now: "Don't look back. Just pedal."

### The lodging house (more)

- `LodgingTexts`
  - was: "That's not how it was. That's not how it was for me."
  - now: "That's not how it was for me."
- `LodgingTexts`
  - was: "I want the key. I want my name. I want whatever's left."
  - now: "I want the key. I want my name back."
- `LodgingTexts`
  - was: "That happened. I don't care what else happened. That happened."
  - now: "That happened. I don't care what else did."
- `LodgingTexts`
  - was: "Daniel. Please. Not yet. Not with him there."
  - now: "Daniel. Please. Not with him there."
- `LodgingTexts`
  - was: "Put it back. I'll stand in it. I'll stand in anything."
  - now: "Put it back. I'll stand in it."
- `LodgingTexts`
  - was: "Don't. I'll stand. I'll stand for you."
  - now: "Don't. I'll stand for you."
- `LodgingTexts`
  - was: "No. I'm in the room. I'm still in the room."
  - now: "No. I'm still in the room."

### People Lights, A Place for You, Second Person

- `PeopleLights`
  - was: "I want to call my father in the morning. Not now. Now I want tonight. And I want to not be listened to through the floor."
  - now: "I'll call my father in the morning. Tonight I want to not be listened to through the floor."
- `PeopleLights`
  - was: "Then I'm staying with you tonight. In there. Not up there."
  - now: "Then I'm staying with you tonight, down here."
- `PeopleLights`
  - was: "Then something has to change tonight. Not everything. One thing."
  - now: "Then one thing has to change tonight."
- `PlaceForYou`
  - was: "Kit wants to be somewhere else. Kit's always wanted to be somewhere else."
  - now: "Kit's always wanted to be somewhere else."
- `PlaceForYou`
  - was: "There's one more. There's always one more."
  - now: "There's one more."
- `PlaceForYou`
  - was: "You've seen it, then. I'm not keeping anyone. I'm keeping the evening. They're in it."
  - now: "You've seen it, then. It's the evening I keep. They're in it."
- `PlaceForYou`
  - was: "I'll stay a while. Not because I have to. I'll try it, and see."
  - now: "I'll stay a while. I'll try it, and see."
- `SecondPersonStory`
  - was: "It's fine. It's fine. Happy new year."
  - now: "It's fine. Happy new year."
- `SecondPersonStory`
  - was: "That's all right. That's all right."
  - now: "That's all right."
- `SecondPersonStory`
  - was: "I'm not angry. I was. I'm not."
  - now: "I'm not angry any more."
- `SecondPersonStory`
  - was: "Alive. Just about. It's not awful. It's a bit awful."
  - now: "Alive. Just about. It's a bit awful."
- `SecondPersonStory`
  - was: "They knew me. Not the first day. Then they did."
  - now: "They didn't know me the first day. Then they did."

### Costume Night, the Last Stop, Last Seen, A Little Longer

- `CostumeNight`
  - was: "Shelly does this every year. Every year she holds so still."
  - now: "Shelly does this every year. She holds so still."
- `CostumeNight`
  - was: "Now that. That's close. That's very close, Rosa."
  - now: "Now that's very close, Rosa."
- `ElkLotNight`
  - was: "Wade owes me forty. Wade always owes me forty."
  - now: "Wade owes me forty again."
- `ElkLotNight`
  - was: "Song's almost over. They're always almost over."
  - now: "Song's almost over."
- `ElkLotNight`
  - was: "They'll come back in. They always come back in."
  - now: "They'll come back in. Give it a minute."
- `LastSeenTexts`
  - was: "They don't come to the door. Not one of them has."
  - now: "Not one of them has come to the door."
- `LastSeenTexts`
  - was: "They haven't moved. Not one of them."
  - now: "None of them has moved."
- `LastSeenTexts`
  - was: "There's nobody out there. Not one of them."
  - now: "There's nobody out there."
- `LongerTexts`
  - was: "You found the good one. That's fine. That's completely fine."
  - now: "You found the good one. That's fine."

### Ghosts set, the Supper, the Drawing, the kitchen, the cabin

- `GhostsSet`
  - was: "There's room. There's always room in here."
  - now: "There's room in here."
- `MarshSupper`
  - was: "Come in, sit down. There's a plate for you. There's always a plate."
  - now: "Come in, sit down. There's a plate for you."
- `Drawing`
  - was: "Give it a minute. Just give it one minute."
  - now: "Give it a minute."
- `Drawing`
  - was: "The crowd stands aside to let you through. Nobody says anything. Nobody stops you."
  - now: "The crowd stands aside to let you through, and nobody stops you."
- `DrawingTexts`
  - was: "they stood aside to let me through. Nobody said anything. Nobody stopped me."
  - now: "they stood aside to let me through, and nobody stopped me."
- `MarshSupperTexts`
  - was: "Nobody moved. Nobody looked at them."
  - now: "Nobody moved, or looked at them."
- `KitchenVisitors`
  - was: "That's kind. That's very kind."
  - now: "That's very kind."
- `KitchenVisitors`
  - was: "That's good. That's really good."
  - now: "That's really good."
- `CabinBargain`
  - was: "All right. That's all right. It was always your answer to give."
  - now: "All right. It was always your answer to give."

### Polybius, Harrigan's phones, Last Orders, the city's log

- `Polybius`
  - was: "It's pepperoni. It's always pepperoni. The other kind ran out in, like, spring."
  - now: "It's pepperoni. The other kind ran out in, like, spring."
- `Polybius`
  - was: "Slice is four tickets, soda's three. It's on the menu. It's always been on the menu."
  - now: "Slice is four tickets, soda's three. It's on the menu."
- `HarriganPhones`
  - was: "Nothing you owe me. Not a thing."
  - now: "You don't owe me anything."
- `LastOrdersTexts`
  - was: "Wouldn't take a drink off Benny. Not one."
  - now: "Wouldn't take a drink off Benny."
- `CityTexts`
  - was: "The straps on the table are cut. Not torn. Cut."
  - now: "The straps on the table have been cut through, not torn."

### Second look: corrections and paired negatives

- `PinehookTexts`
  - was: "No. Nobody comes in. Not tonight."
  - now: "No. Nobody comes in tonight."
- `PinehookTexts`
  - was: "No. Not this time. You carry it."
  - now: "Not this time. You carry it."
- `PinehookTexts`
  - was: "I stayed with Jude while he changed. Nothing happened. Nobody came."
  - now: "I stayed with Jude while he changed, and nobody came."
- `PinehookTexts`
  - was: "Not the shed. Not the knife. Somebody else did that"
  - now: "Not the shed or the knife. Somebody else did that"
- `PellTexts`
  - was: "He doesn't eat. I know how that sounds. Water, yes. Never food."
  - now: "He doesn't eat. I know how that sounds. He'll drink water, but never food."
- `BorrascaPeople`
  - was: "Somebody's going to have to write it up. It won't be me. Not this time."
  - now: "Somebody's going to have to write it up, and this time it won't be me."
- `BorrascaPeople`
  - was: "Out. And go home by the street. Not by the trail."
  - now: "Out. And go home by the street, not the trail."
- `LongerTexts`
  - was: "It finds something late at night and then loses it again. Not interference. Not the aerial."
  - now: "It finds something late at night and then loses it again. It isn't interference, or the aerial."
- `LookoutNight`
  - was: "No storm. No ground. The tower stands on its legs over nothing"
  - now: "There is no storm, and no ground. The tower stands on its legs over nothing"
- `HouseHuman`
  - was: "There's room by the fire. Not by the supplies."
  - now: "There's room by the fire. Keep off the supplies."
- `LiteraryTexts`
  - was: "and the doors below stood open. Nobody answered from any of them."
  - now: "and the doors below stood open, and no one answered from any of them."
- `LodgingTexts`
  - was: "I know these walls. I know every inch of them."
  - now: "I know every inch of these walls."
- `LastCrossingTexts`
  - was: "A rusted iron door in the wall. Rusted shut; nobody's opened it in years."
  - now: "An iron door in the wall, rusted shut. Nobody's opened it in years."


## Found while checking this pass

The two-reader cast checks on 0.4.675 caught Ray at Movie Night saying the same line twice: a reader asked him something, he answered, and on his next idle turn he said that answer again, unasked. Now each of the Movie Night cast remembers the last line they said, and an idle line that would only repeat it within half a minute goes unsaid (`MovieNightCast.idle`). Ray's call to a reader out on the water with no program now goes through the same caption as the rest of his speech, to that reader only, rather than also being copied into the action bar. No words change.
