# Reading and reconnecting (0.4.288)

Speak to Johnny after reconnecting to pick up the current exchange. The offer still belongs to the same meeting, and a fresh reply token prevents a delayed answer from being accepted. If Johnny received the final answer before the disconnect, the panel reopens on the completed exchange. The journal keeps the transcript under its original entry.

Choose **Read at my pace** on a private character room's opening or closing card. **Back** and **Continue** turn the pages, and **Done** releases that reader's room or transition. A disconnected reader's hold is released so it cannot strand them on their return. Protocol 128 carries the card token and the hold or finish request.

Native cases cover a same-profile reconnect during Johnny's question, a second reader answering independently, and a reconnect after a committed answer. The private room case covers a reader holding both kinds of cards past their normal timers, a second reader progressing as usual, and stale or duplicate Done requests. The client proof exercises both directions of page navigation and Done.
