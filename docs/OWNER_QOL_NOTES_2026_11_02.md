# The owner's quality-of-life notes (2 November 2026)

Sent while 0.4.264 (Johnny Truant's first slice) was being finished, after a review of the branch at 0.4.257 (1c4541d4). Recorded word
for word; each is built in its own release, in the owner's order.

> Yes. I reviewed the latest active branch at 0.4.257, 1c4541d4. My priorities would be:
>
> 1. Autosave journal writing. Currently, edits are sent when turning a page or closing the journal. Save periodically while typing to
>    reduce lost writing after a disconnect or crash. [Current implementation: client/JournalScreen.java#L87]
> 2. Persistent conversation history. The new conversation panel is a useful foundation, but its history is temporary, capped at 48
>    lines, and discarded on logout. Let players revisit conversations they actually heard, especially their own promises and answers.
>    Keep this separate from their handwritten journal. [labyrinth/Talk.java#L26]
> 3. Convenient retrieval from the hall drawers. Add "retrieve belongings," restore equipment to its previous slots where possible, and
>    expose overflow through pages or an overflow count. Currently, only 54 stacks are displayed; additional stacks remain hidden until
>    reopening after making room. [house/HouseKeeping.java#L194]
> 4. Journal bookmarks and search. It already remembers the open spread, but finding an older entry means paging through the notebook. A
>    ribbon bookmark, page-number jump, and search through the player's own writing would become increasingly valuable across a long
>    playthrough.
> 5. Companion commands while holding a light. Currently, opening the wheel requires an empty hand or the compass shortcut. A rebindable
>    nearby-companion key would remove that inventory shuffle. The wheel should also highlight the current order, so players can
>    immediately distinguish "staying" from "failing to follow." [opening/CompanionOrders.java#L58]
> 6. Clearer group crossings. Co-op already has a follow window and compatibility checks. Add a subtle indication that friends can follow
>    through this door, plus feedback when private scenes or different story progress will separate them. That would prevent
>    considerable multiplayer confusion. [labyrinth/GroupCrossings.java#L25]
> 7. Consistent visual-comfort controls. Several scenes respect vanilla effect settings already. Make that behavior consistent:
>    `WayOutClient`, for example, clamps the effects multiplier to at least `0.3`, and its ending static bypasses that multiplier. A
>    player selecting zero should get predictable results for cosmetic effects. [client/WayOutClient.java#L98]

And on the second, while it was being built:

> i'm okay with the conversation history being integrated into the player's journal
