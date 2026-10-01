# The Oldest House 0.4.13

NeoForge 1.21.1, Java 21. Install the same version on client and server; network protocol remains 14.

The Witness ending now requires **75% of the playable, eligible vignette stories, rounded up**, across at least two story kinds. The pool currently has eight sources, including the preserved cave and Shallows, so six distinct personal resolutions restore the final passage. Each source still counts once; two sources can be missed and the Mother remains optional. There are still three ending options.

The requirement derives directly from the shipped story pool. A ninth source will require seven resolutions, a tenth eight, and twelve nine. The [vignette release checklist](VIGNETTE_RELEASE_CHECKLIST.md) and root repository instructions require personal credit, quota, tests and documentation to be reviewed with each new vignette.

Existing evidence and deliberate readings retain their saved IDs and outcomes. An old three-resolution account needs three further distinct sources before an uncommitted Witness ending qualifies. Previously completed endings and a release already underway remain saved. The lectern generates the current account and the restored directions appear only after the quota is met.

Use `/oldesthouse finale witness status` to see the current personal count, required count and eligible pool. The operator fixture `/oldesthouse finale witness ready` supplies enough distinct resolutions for the current quota and still requires reading the cell's lectern.

Three new server GameTests cover upward rounding as the pool grows, upgrading an older three-story account without losing evidence or bypassing the new quota, and the real operator command meeting the quota without duplicate credit. Existing Witness tests now check five resolutions remain insufficient, six restore the actual directions, duplicate scenes cannot farm credit, and six-story accounts and readings survive save/reload. The existing cave, Shallows, native release walk, other endings and full server suite remain enabled.
