# Native bed camera evidence

Source: `abad715c291f579f9752ba147a5f729656fcc1c3`, candidate 0.4.140. CI run [40630534226](https://github.com/Knaitoe/the-oldest-house/actions/runs/40630534226), Goatman job `122626039134`, artifact `12791564268`.

Both original native screenshots were inspected. Both clients used opposite native beds, retained sleeping after real wake-button/Escape/close attempts and captured production cameras facing the actual center table. The accompanying native records give pillow/foot/camera positions and look vectors. No image edits or alternate camera override were used.

The job later **failed during native shutdown**; these camera/input results are not an overall job or release pass. The follow-up requires orderly client logout and a clean host exit within the existing 90 seconds, with native thread diagnostics if it stalls. See `docs/PR_10_REVIEW_0_4_140.md`.
