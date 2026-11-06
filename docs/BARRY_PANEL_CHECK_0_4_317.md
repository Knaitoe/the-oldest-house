# Barry's private conversation check (0.4.317)

[Build 1449](https://github.com/Knaitoe/the-oldest-house/actions/runs/42247397026), on `d3591b8da5b9a908859398af8d97a5707ab9ae1f` (0.4.316), failed. Its Structure suite and house-read-rooms job both reported the same case: `PersonalityTailoringTests.nativeLastStopStanleyAndBarryRepliesStayPrivateAndKeepTheirOriginalCountersAndMechanics`. The other seventeen jobs passed. The previous lake and empty-world fixture leaks no longer failed.

The Ghosts revision moved Barry's actual conversation from action-bar messages into Talk. His personal address was retained in the panel, but the older cross-vignette integration case still drained system-chat packets and expected the greeting there. That assertion was missed when the interaction changed. The newer Ghosts reply tests exercised the conversation state without covering this older transport assertion.

The existing integration case now inspects actual native `TalkPayload` packets, including packets inside native bundles. It requires an open Barry panel containing the reader's personal address on the reader's connection and no conversation payload on the friend's connection. It also requires the actual day's question, unchanged film and shot counters, and one consumed dialogue line. Reopening the same offer must retain the dialogue cursor, lines and reply token. A friend's attempt to use the owner's producer must open no panel or dialogue state for that friend. The fixture clears its open conversation after the batch.

The selected-case runner also gains the missing `tailoring.nbt` alias, copied byte-for-byte from that namespace's original empty structure. Without it the local generator could abort before registering the selected case while Gradle still returned success. Validation checks the native passed-case count, not only the process exit code.

This is a verification repair; it changes no dialogue, scene timing, camera requirement, privacy choice, completion gate, source, layout or protocol. The Ghosts and outside Truant changes remain those in [GHOSTS_NOVEL_0_4_315.md](GHOSTS_NOVEL_0_4_315.md) and [TRUANT_YARD_0_4_316.md](TRUANT_YARD_0_4_316.md).

## Validation

The previously failing native case passes on its own. A second fresh-world run passes all 71 declared cases across the Ghosts, house-read, tailoring and co-op namespaces, including the older cross-vignette case and the seven-day Barry reply checks. The package build succeeds. The selected runner checks the byte-identical template and the script requires the actual passed-case count.

Exact-commit CI remains pending; this candidate is not a verified release.
