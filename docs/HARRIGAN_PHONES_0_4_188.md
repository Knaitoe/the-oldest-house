# Mr. Harrigan's two phones — 0.4.188

The owner: "You can give Harrigan his phone or not, right? That should reflect in how the phone is handled. Maybe you can receive friendly affirming texts if you didn't bury him with it — sort of freeing him. If you gave him the phone, you let him carry the obligation and that's when the rot starts." This is the success-and-failure audit's third build.

Both choices still resolve the story as before. What changes is what the phone does afterwards (`HarriganPhones`).

## Kept: he is freed

Once each House day, outside the House, a kind line comes into the reader's message thread from his number, though his phone is in their pocket. "A text from Mr. Harrigan."
- **The lines are in his voice:** the man who sent the boy books and scratch tickets. "Read something today. Anything. A man who reads is never really alone." "You did right by me." "Nothing you owe me. Not a thing."
- **Each reader at their own hour.**
- **He takes no name:** a call or text to his number still rings in the reader's own pocket.

## Buried: he carries the obligation, and the rot starts

The line takes names as before (one a day; a hostile kind dies near the reader's bed at the next dawn, and a harmless or named creature brings him instead). Each name rots it further:
- **After the first names,** his voicemail greeting slows: "This is Harrigan. I don't… pick up this phone. If you have a name for me… leave it. After the tone."
- **After more,** it loses letters: "Th s is H rr gan. I d n't p ck up." In the end it is only "…a name. Leave a name."
  - His replies in the thread already lose their letters after two names.
- **Flies:** the morning a name is answered, there are flies at the reader's window, and the phone smells of the casket.
- **At the last** (four names or more), he no longer waits to be asked. Some nights the reader's own phone rings by itself, five times, three seconds apart. It ends in a missed call in the thread: "Missed call · Mr. Harrigan. You can hear breathing on the voicemail." This happens once a night at most, and only if they carry their phone.

## Tests

**`HarriganDialogueTests.keepingHisPhoneFreesHimAndBuryingItLetsTheRotStart`** (in the wake namespace). It checks:
- the kept phone's kind text, once a day, and no ringing;
- the buried phone: nothing kind, the greeting at each stage of rot;
- the night ring needs the reader's phone, comes once a night, and rings out to a missed call.

All fifteen wake cases pass locally.

## What stays

- **Protocol and layout unchanged:** protocol 101, layout 51.
- **Witness unchanged:** 55 / 42 / two kinds / three endings.
- **No PNG changes.**
