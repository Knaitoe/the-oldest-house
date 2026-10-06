# Staircase leak assets

The faded wallpaper was generated with the built-in image generation tool, then resized with nearest-neighbour sampling to a 128×128 native block texture. The request was a seamless cream pixel-art wallpaper with small muted sage leaf sprigs and clay-red dots, a quiet worn household pattern, flat lighting, no text or objects. Source asset: `src/main/resources/assets/the_oldest_house/textures/block/leak_wallpaper.png`.

`tools/generate_staircase_leak_assets.py` authors 34 measured block meshes, seven native item meshes and five quiet original synthesized mono Vorbis cues. Cups have handles and open rims; hung cups include hooks; the drawer reveals a button tin. Laundry sits at the native bed height. Lamps and towels meet their supports. Models use Minecraft materials and the custom wallpaper; the reading proxy carries a blank sheet without the reader’s personal text. No external recordings are included.

Meshes are under `models/block/leak/` and `models/item/leak_*`; cues are under `sounds/leak/`, within the mod’s asset directory. Run the generator to reproduce geometry and audio; it preserves the generated wallpaper.
