#!/usr/bin/env bash
# Fit generated surface artwork to the native cube nets; nearest-neighbour UV baking only.
set -euo pipefail
cd "$(dirname "$0")/.."
elk_bake_tmp=$(mktemp -d -t oldest-house-elk-XXXXXX)
trap 'rm -rf "$elk_bake_tmp"' EXIT
paint() {
    convert "$surface" -filter point -resize '1774x887!' \
        -crop "${7}x${8}+${5}+${6}" +repage -resize "${3}x${4}!" -strip "$elk_bake_tmp/face.png"
    convert "$elk_bake_tmp/atlas.png" "$elk_bake_tmp/face.png" -geometry "+${1}+${2}" -composite "$elk_bake_tmp/atlas.png"
}
for variant in elk elk_wounded; do
    surface="art/elk_surface_0480.png"
    if [[ "$variant" == elk_wounded ]]; then surface="art/elk_surface_wounded_0480.png"; fi
    convert -size 128x64 xc:'#775038' "$elk_bake_tmp/atlas.png"
    # Body: six independent faces, including cream underside and buff rump.
    paint 23 0 10 23 454 8 180 120; paint 33 0 10 23 638 8 160 120
    paint 0 23 23 12 8 132 435 202; paint 23 23 10 12 448 145 188 189
    paint 33 23 23 12 640 132 330 202; paint 56 23 10 12 974 132 77 202
    # Neck.
    paint 8 36 7 8 130 444 133 116; paint 15 36 7 8 269 444 120 116
    paint 0 44 8 15 9 563 118 262; paint 8 44 7 15 132 563 132 262
    paint 15 44 8 15 269 563 118 262; paint 23 44 7 15 393 563 119 262
    # Head: separate eye-bearing left and right surfaces.
    paint 78 0 8 10 1346 10 159 130; paint 86 0 8 10 1509 10 139 130
    paint 68 10 10 7 1215 152 129 99; paint 78 10 8 7 1348 152 156 99
    paint 86 10 10 7 1508 152 128 99; paint 96 10 8 7 1348 152 156 99
    # Muzzle.
    paint 73 20 6 5 1210 323 121 55; paint 79 20 6 5 1339 323 93 55
    paint 68 25 5 4 1115 383 85 58; paint 73 25 6 4 1205 383 127 58
    paint 79 25 5 4 1337 383 96 58; paint 84 25 6 4 1210 323 121 55
    # Ears: rims and inner pink surfaces.
    paint 102 20 6 2 1570 324 93 34; paint 108 20 6 2 1570 324 93 34
    paint 100 22 2 3 1486 373 22 65; paint 102 22 6 3 1510 373 48 65
    paint 108 22 2 3 1664 373 18 65; paint 110 22 6 3 1570 361 91 78
    # Legs, hooves and tail, each on its own unused net.
    paint 35 38 3 3 652 504 77 36; paint 38 38 3 3 735 504 64 36
    paint 32 41 3 13 568 544 75 257; paint 35 41 3 13 651 544 77 257
    paint 38 41 3 13 734 544 73 257; paint 41 41 3 13 816 544 72 257
    paint 52 38 4 4 959 600 75 56; paint 56 38 4 4 1040 600 75 56
    paint 48 42 4 2 959 600 75 56; paint 52 42 4 2 1040 600 75 56
    paint 56 42 4 2 1120 600 73 56; paint 60 42 4 2 1201 600 71 56
    paint 73 38 2 5 1408 531 50 59; paint 75 38 2 5 1408 531 50 59
    paint 68 43 5 2 1340 597 49 50; paint 73 43 2 2 1405 597 49 50
    paint 75 43 5 2 1460 597 82 50; paint 80 43 2 2 1547 597 37 50
    convert "$elk_bake_tmp/atlas.png" -filter point -resize '512x256!' -alpha on -strip \
        "PNG32:src/main/resources/assets/the_oldest_house/textures/entity/literary_${variant}.png"
done
echo 'Baked healthy and wounded elk: 512x256 exports of non-overlapping 128x64 native UV nets.'
