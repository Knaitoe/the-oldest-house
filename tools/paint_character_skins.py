"""Paint the cast at two pixels per Minecraft model unit, directly on its UV faces.

No source pictures, resampled portraits, noise, filters or generated fabric tiles.
All marks below describe a face, garment panel, seam, hair cluster or accessory.
The final integer nearest-neighbour export is only storage scaling. Borrasca's
five new human sheets now use this painter too. Run with --check to verify the committed paintings.
"""
from dataclasses import dataclass, replace
from pathlib import Path
import argparse
import hashlib
import json
from PIL import Image, ImageDraw, ImageColor

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / 'src/main/resources/assets/the_oldest_house/textures/entity'
S = 2


def rgb(c):
    return ImageColor.getrgb(c) if isinstance(c, str) else tuple(c[:3])


def shade(c, n):
    return tuple(max(0, min(255, v+n)) for v in rgb(c))


@dataclass(frozen=True)
class Look:
    skin: str = '#ca9979'
    hair: str = '#493026'
    cut: str = 'short'
    cloth: str = '#4a5865'
    pants: str = '#303941'
    style: str = 'shirt'
    accent: str = '#c9bc9f'
    eyes: str = '#52605b'
    age: int = 0
    beard: str = ''
    glasses: bool = False
    mark: str = ''
    sleeve: int = 9
    logical: int = 128
    costume: str = ''
    number: int = 0
    scarf: str = ''
    face_style: str = ''
    # 0.4.202 (owner: "eyes are just a smidge off"): the House body's irises sit in the middle of both eyes, looking ahead.
    level_eyes: bool = False


# Each row is a character design, rather than an index into a repeated picture.
LOOKS = {
    'borrasca_kyle': Look('#d7ac8b','#846444','part','#d8d4bc','#aa9673','tee','#52677e',eyes='#5d7183',mark='borrasca_kyle',sleeve=4,logical=64),
    'borrasca_kimber': Look('#ddb397','#975739','long','#477d79','#4d6271','jacket','#d8d3b8',eyes='#52745b',mark='borrasca_kimber',sleeve=10,logical=64),
    'borrasca_sheriff': Look('#caa17e','#8d897e','receding','#b09c71','#5f5543','shirt','#bdad70',eyes='#675a46',age=2,beard='moustache',mark='borrasca_sheriff',sleeve=10,logical=64),
    'borrasca_prescott': Look('#d5a38a','#aaa69c','receding','#88433b','#8a7d5c','flannel','#786049',eyes='#62717c',age=2,mark='borrasca_prescott',sleeve=10,logical=64),
    'borrasca_worker': Look('#bc9479','#4a3c31','close','#627174','#4a595d','crew','#be8748',eyes='#514e43',glasses=True,mark='borrasca_worker',sleeve=10,logical=64),
    # 0.4.262 (owner: "the construction workers shouldn't have identical textures"): the second man, older and heavier, in a canvas chore coat and a white hard hat.
    'borrasca_worker_two': Look('#a7765a','#2d2621','close','#6a5a3a','#3b4450','crew','#d7d2c2',eyes='#3f3a33',age=1,beard='chinstrap',mark='borrasca_worker_two',sleeve=10,logical=64),
    'borrasca_eli': Look('#bb8c69','#514031','close','#a76145','#80745c','tee','#aa8e6a',eyes='#635342',sleeve=4,logical=64),
    'borrasca_wren': Look('#dbb78e','#65513f','long','#526986','#514a44','cardigan','#8b9c9d',eyes='#6e7765',mark='freckles',sleeve=10,logical=64),
    # 0.4.438: Kelly grown, found in room 6 on the last night. Her auburn hair and green eyes, freckles, a washed-out shirt and trousers.
    'borrasca_kelly_grown': Look('#d6ab8f','#8a4f35','long','#8a8478','#5e5a52','shirt','#a39d90',eyes='#52745b',mark='freckles',sleeve=10,logical=64),
    'uncanny_waitress': Look('#cba083','#795039','bun','#496d80','#496d80','dress','#e0d4b8',eyes='#596456',age=1,mark='diner_apron',sleeve=4,logical=64),
    'uncanny_second_waitress': Look('#c4ad97','#42312c','bun','#526c79','#526c79','dress','#d5c8ab',eyes='#657068',mark='diner_second',sleeve=4,logical=64),
    'uncanny_second_waitress_split': Look('#c4ad97','#42312c','bun','#526c79','#526c79','dress','#d5c8ab',eyes='#657068',mark='diner_split',sleeve=4,logical=64),
    'uncanny_peeler': Look('#c7a087','#8d8879','receding','#737766','#4f5145','shirt','#a99e7b',eyes='#665d4d',age=2,beard='stubble',sleeve=10,logical=64),
    'uncanny_peeler_exhausted': Look('#c79c82','#8d8879','receding','#5b6254','#484b40','shirt','#a99e7b',eyes='#665d4d',age=2,beard='stubble',mark='cellar_exhausted',sleeve=10,logical=64),
    'uncanny_visitor': Look('#cbb194','#473f37','part','#45494a','#343a3d','coat','#afa78e',eyes='#62685d',age=1,sleeve=11,logical=64),
    'literary_paw_walter': Look('#d6a88a','#cecbc4','receding','#47513c','#5a4c3e','coat','#9e8350',eyes='#5c6c78',age=2,beard='chops',glasses=True,mark='cottage_walter'),
    'literary_paw_ruth': Look('#deb496','#76665a','bun','#523240','#5f4b4e','dress','#d8cebb',eyes='#544a3e',age=2,mark='cottage_apron'),
    'literary_paw_will': Look('#daac8a','#3e2c20','messy','#d6ceba','#585046','braces','#962e28',eyes='#465c42',mark='cottage_braces',sleeve=6),
    'literary_paw_sergeant': Look('#c48868','#968c80','close','#282c3a','#5c5a58','coat','#b09250',eyes='#382c24',age=2,beard='handlebar',mark='cottage_uniform'),
    'literary_paw_messenger': Look('#dcbaa2','#2c241e','part','#201e20','#5b5a59','coat','#242226',eyes='#48423c',beard='pencil',mark='cottage_bowler'),
    # Movie Night (0.4.102): the projectionist, the ticket seller, the Pruitts on the beach and the two in the canoe.
    'literary_movie_ray': Look('#c9a184','#8f887c','receding','#58664d','#3d3a33','cardigan','#c7b98e',eyes='#5d5e52',age=2,glasses=True,beard='walrus',mark='movie_cap'),
    'literary_movie_dot': Look('#d9b096','#a4532f','bun','#a8404a','#423a3c','cardigan','#ecdcb4',eyes='#4e5a48',age=1,mark='movie_badge'),
    'literary_movie_frank': Look('#cf9f7d','#5b4130','part','#3a7c8a','#a08e6a','shirt','#f0d27a',eyes='#4f5f6e',age=1,mark='movie_hawaiian',sleeve=4),
    'literary_movie_june': Look('#e0b89a','#6c4a2e','long','#d6a248','#d6a248','dress','#f2ead2',eyes='#566a50',sleeve=0),
    'literary_movie_jamie': Look('#dcb090','#8c5a2e','messy','#c4463a','#3e5a78','tee','#efe4cc',eyes='#4f6a7a',mark='movie_stripes',sleeve=4),
    'literary_movie_bobby': Look('#c99a78','#2c2219','part','#2d4878','#3a4d68','jacket','#e6d8b0',eyes='#4a3c30',mark='movie_letter'),
    'literary_movie_lynn': Look('#e2bea6','#d6b06a','long','#e8c6cf','#394c66','knit','#f6eef0',eyes='#5b7090',mark='freckles'),
    # Costume Night (0.4.103): Hank, who judges, and the twelve entrants, every one of them dressed as the Lake Witch.
    'literary_costume_hank': Look('#cfa688','#bdb8ae','receding','#6a5a46','#3f3a34','cardigan','#d7c79e',eyes='#5a6066',age=2,glasses=True,beard='horseshoe',mark='movie_badge'),
    'literary_costume_tina': Look('#e0b89c','#18181a','long','#1d1e22','#26272b','dress','#4a4d55',eyes='#4e5a48',sleeve=6,costume='yarn bags',number=1),
    'literary_costume_danny': Look('#ddb294','#6a4a2c','messy','#d9d6ca','#4a5560','ragged','#5d7a3a',eyes='#5a6a7a',costume='sheet',number=2),
    'literary_costume_ron': Look('#c99a7a','#1e1d1c','long','#8f7a96','#3e4652','coat','#c9b9cc',costume='mask',number=3),
    'literary_costume_kaylee': Look('#e6c2a6','#5e9a3a','long','#2b2d33','#2b2d33','dress','#4c7a33',sleeve=6,costume='paint',number=4),
    'literary_costume_marcus': Look('#b98a66','#2a211b','short','#3c4130','#4a4f32','shirt','#5c6e34',costume='weed',number=5),
    'literary_costume_shelly': Look('#e8cdb8','#141416','long','#6d6f73','#6d6f73','dress','#9a9da2',eyes='#3e3e44',sleeve=0,costume='pale',number=6),
    'literary_costume_owen': Look('#d8ae8e','#4a3324','short','#7a5a3a','#4d5a66','tee','#c8a070',sleeve=4,costume='cardboard',number=7),
    'literary_costume_brooke': Look('#d9b090','#6e8f3c','long','#2f6a8a','#33434f','tee','#7aa04a',sleeve=4,costume='mop',number=8),
    'literary_costume_wes': Look('#c69878','#151515','short','#5e6266','#3a3f45','jacket','#8a8e92',costume='hood',number=9),
    'literary_costume_rosa': Look('#c99b7d','#101012','long','#c3c9c8','#c3c9c8','dress','#9aa6a8',sleeve=0,costume='yarn gown',number=10),
    'literary_costume_pip': Look('#dcb092','#7a5634','messy','#c05a8a','#4a5866','tee','#e0d0d8',sleeve=4,costume='pillow',number=11),
    'literary_costume_lyle': Look('#c4977a','#121212','long','#b6566c','#3d5170','dress','#e6c6a0',beard='full',sleeve=6,costume='flowers',number=12),
    # Winter Lake (0.4.104): Bud, Hank, Clay and Lonnie from the shallows, that winter: coats, knit caps, scarves and mittens.
    'literary_winter_bud': Look('#c39a77','#55402d','messy','#8f2b26','#3b3e45','flannel','#2b4f86',mark='winter',sleeve=11,scarf='#d8c9a6'),
    'literary_winter_hank': Look('#d8ae8e','#a4562e','short','#2a3550','#4a4339','jacket','#b8392e',eyes='#5a6a4a',mark='winter',sleeve=11,scarf='#e6dcc4'),
    'literary_winter_clay': Look('#d2a684','#c9a35a','messy','#7a5a38','#34383f','jacket','#d4a640',eyes='#506a80',mark='winter',sleeve=11,scarf='#5a7a4a'),
    'literary_winter_lonnie': Look('#bf9171','#2f241d','close','#4a5a3c','#3d4148','jacket','#6a6a72',eyes='#4a3c30',mark='winter',sleeve=11,scarf='#9a3a30'),
    # Usher (0.4.182): Roderick, cadaverous, the hair floating rather than falling, a frock coat and a pale stock at the throat.
    # 0.4.209, the closing hall: Chad and Daisy Navidson, at the end of the long dark hall (stranger phases 39 and 40, child scale).
    'literary_closing_chad': Look('#d6ac8c','#5a3a24','messy','#3f5f86','#3b4656','tee','#c9d2dc',eyes='#56677a',sleeve=4),
    'literary_closing_daisy': Look('#e0bc9f','#8a5c36','long','#c97f95','#7b8fa8','dress','#f0e2e6',eyes='#5f7064',sleeve=4),
    # Polybius (0.4.217): the two men in suits by the black cabinet, on stranger phases forty-one and forty-two.
    'literary_polybius_a': Look('#c9a283','#2a2622','close','#16171b','#141519','suit','#e6e6e8',eyes='#3a3a3a',age=1,glasses=True),
    'literary_polybius_b': Look('#94684c','#1c1a18','receding','#1a1b20','#16171b','suit','#dcdcde',eyes='#2c2a28',age=2),
    # 0.4.221 (owner: "add npcs playing unused games"): the arcade's three regulars, each at their own cabinet (stranger phases
    # forty-seven to forty-nine): a denim jacket, a red tee, a knit sweater and glasses.
    'literary_polybius_teen': Look('#d2a283','#3a2a20','messy','#b0302a','#2a2a30','shirt','#f0e8d8',eyes='#5a5040',mark='pizza_visor',sleeve=4),
    # Vacant Possession (0.4.228): the tenant, in a cardigan, on moving day.
    'literary_vacant_tenant': Look('#c8a08a','#cfcac2','bun','#7a5a62','#4a4440','cardigan','#d8ccb0',eyes='#5a5c5a',age=2,glasses=True),
    # The last swimmer (0.4.227): the pool's attendant, and the one still in the water.
    'literary_swimmer_attendant': Look('#c39a7c','#b8b4ac','receding','#e8e4d8','#2e3a4a','tee','#2a3a5a',eyes='#5a5e58',age=2,mark='whistle',sleeve=4),
    'literary_swimmer_swimmer': Look('#e4ddd6','#1e1c1e','short','#1c2230','#1c2230','swim','#d8dcd8',eyes='#2a2e32',mark='swim_cap',sleeve=0),
    # The window cleaner (0.4.251): a navy boiler suit, a cloth cap, the harness's yellow straps over it.
    'literary_window_cleaner': Look('#b68a6c','#3a2c22','short','#2e4a66','#24384e','crew','#d8b83a',eyes='#4a5650',age=1,beard='shadow',mark='movie_cap',sleeve=10),
    # The Night Operator (0.4.252): a cardigan over a blouse, reading glasses, hair pinned back; in a switchboard headset, and with it hung up.
    'literary_night_operator': Look('#c69c7e','#6a5a4c','bun','#5a4e5e','#33303a','cardigan','#d8ccb6',eyes='#4e5a58',age=2,glasses=True,mark='headset'),
    'literary_night_operator_bare': Look('#c69c7e','#6a5a4c','bun','#5a4e5e','#33303a','cardigan','#d8ccb6',eyes='#4e5a58',age=2,glasses=True),
    # After the People Lights (0.4.253): Kelly in her sweater and jeans, Mark in his work flannel, her father Stan in his jacket.
    'literary_people_kelly': Look('#d4ae94','#5a3a26','long','#7a6a88','#38404e','knit','#d8d0c4',eyes='#4a5a62'),
    'literary_people_mark': Look('#c09878','#2e241c','short','#5a4a3a','#2e3640','flannel','#8a3a30',eyes='#4a4a40',beard='patchy'),
    'literary_people_stan': Look('#c8a088','#c2bcb2','receding','#5a5e48','#3a3a36','jacket','#a89a7a',eyes='#5a5c58',age=2),
    # A Place for You (0.4.257): Mara in her cardigan over a work dress, her hair pinned up; Kit in a jacket for going.
    'literary_place_mara': Look('#cfa088','#5c4030','bun','#6a7a5c','#3c3632','cardigan','#d8cbb0',eyes='#4e5a4c',age=1),
    'literary_place_kit': Look('#d8b098','#2e2620','messy','#3a4c6a','#2e3238','jacket','#c89a44',eyes='#4a5a6a'),
    # 0.4.264, Johnny Truant: tired skin, a worn face a little uneven, stubble, a worn jacket, ink on his hands. The leer is the same face
    # with the eye on its showing side narrowed and that corner of the mouth pulled up, tight.
    'literary_truant': Look('#c49a80','#3b2f28','messy','#4f4840','#33343a','jacket','#7d6f5c',eyes='#4f5650',age=1,beard='stubble',mark='truant'),
    'literary_truant_leer': Look('#c49a80','#3b2f28','messy','#4f4840','#33343a','jacket','#7d6f5c',eyes='#4f5650',age=1,beard='stubble',mark='truant_leer'),
    # Second Person (0.4.274): Iris Dunne across the landing, in her cardigan and glasses, her grey hair pinned; Sam from the resident's work, in a jacket for an evening out.
    'literary_flat_iris': Look('#d9b39e','#bab4ac','bun','#7a5468','#4a4440','cardigan','#d3c8b2',eyes='#5a6070',age=2,glasses=True),
    'literary_flat_sam': Look('#8e5f45','#221a16','close','#4d6a78','#2c3038','jacket','#d8d0c0',eyes='#3a3029'),
    # Speak Up (0.4.327): the four at the table. Joan, grey and sure, answers for the guest; Pat in a knit and glasses contradicts her;
    # Dell, stubbled, in flannel, sits in the chair across the way on; Ruth, opposite the guest, is the one who looks at them.
    'literary_supper_joan': Look('#d6b098','#9a948c','short','#6a3a42','#3a3638','cardigan','#d8ccb6',eyes='#5a5a52',age=2),
    'literary_supper_pat': Look('#c4987c','#4a3a2c','receding','#3e5a4a','#33363c','knit','#c9bc9f',eyes='#4e5a50',age=1,glasses=True),
    'literary_supper_dell': Look('#9a6a4c','#241c18','short','#7a3a2a','#2e3238','flannel','#d0c4a8',eyes='#3a3028',age=1,beard='short'),
    'literary_supper_ruth': Look('#e0bea2','#2a2220','long','#4a5a7a','#2e3440','shirt','#d8d0c0',eyes='#4a5a6a'),
    # The familiar kitchen's visitors (0.4.331): Holloway at the table, his own look on the cast's layout.
    'literary_kitchen_holloway': Look('#b98b69','#5a4230','part','#5b6148','#51483a','field','#b49355',age=1,beard='chinstrap'),
    # The Supper (0.4.348): the Marsh household and their guest. Ida in her apron, Royce in his work flannel, Del, Nana in her cardigan, and Corinne in the jacket she walked up through the corn in.
    'literary_marsh_ida': Look('#d9b49a','#6e5a48','bun','#7a4a3a','#3a3430','apron','#d8ccb0',eyes='#5a6050',age=1),
    'literary_marsh_royce': Look('#c99a78','#8a8478','receding','#4a5a3e','#3a3e44','flannel','#c8bca0',eyes='#4a4a40',age=2,beard='walrus'),
    'literary_marsh_del': Look('#d0a284','#7a5232','messy','#8a3a2e','#2e3a4a','flannel','#d0c4a8',eyes='#4e5a50',beard='stubble'),
    'literary_marsh_nana': Look('#e2c2aa','#d8d4cc','bun','#5a4a6a','#3a3640','cardigan','#cfc4b0',eyes='#5a5a52',age=2,glasses=True),
    'literary_marsh_corinne': Look('#8a5a40','#1e1814','ponytail','#3e6a6e','#2a2e38','jacket','#d8d0c0',eyes='#3a2a20'),
    # The Last Crossing (0.4.351): Vera, the keeper of the flood station, in her work coat over a cardigan, her hair cut short and gone grey.
    'literary_crossing_vera': Look('#d6ae90','#a8a296','short','#4e5a44','#3a3834','coat','#cfc3a2',eyes='#566660',age=2),
    # 0.4.349, In the Tall Grass: the driver of the car that pulls over, in a work jacket, on stranger phase 115.
    'literary_grass_driver': Look('#c49a7a','#4a3a2e','short','#6a5a3e','#3a3c42','jacket','#cfc4a8',eyes='#4a4a42',age=1,beard='stubble'),
    # Last Weekend at Pinehook (0.4.364), on stranger phases 117 to 122: Casey in a loud short-sleeved shirt; Jude in his jacket, and in the
    # costume, a boiler suit and a pale full-head rubber mask with crooked eyeholes, and the costume with the mask pulled off; Nina in a knit;
    # Len, the owner, in a work flannel. Whoever wears the costume looks the same in it.
    'literary_pinehook_casey': Look('#d2a283','#5a3a22','messy','#3a6a8a','#3a3f48','shirt','#e8d8b0',eyes='#4e5a62',sleeve=4),
    'literary_pinehook_jude': Look('#8e5f45','#1e1814','close','#4a4a52','#2e3238','jacket','#c8c0b0',eyes='#3a3029',beard='shadow'),
    'literary_pinehook_masked': Look('#d8d2c0','#d8d2c0','close','#3a3c34','#3a3c34','crew','#5a5c52',eyes='#141414',costume='slasher',sleeve=10),
    'literary_pinehook_coverall': Look('#8e5f45','#1e1814','close','#3a3c34','#3a3c34','crew','#5a5c52',eyes='#3a3029',beard='shadow',sleeve=10),
    'literary_pinehook_nina': Look('#dcb49a','#2a2220','long','#5a4a5e','#2e3440','knit','#d8d0c0',eyes='#4a5a52'),
    'literary_pinehook_len': Look('#c8a088','#a8a49a','receding','#6a5a3a','#3a3c40','flannel','#a89a7a',eyes='#5a5c58',age=2,beard='shadow'),
    # The Drawing (0.4.407): the village in the square. Mr. Albright in his shirtsleeves with the list, Mr. Rudd the postmaster in his
    # cardigan and glasses, Nell Garrow in her apron, Dale Garrow in his work flannel, Bess their daughter, Old Mr. Pike in his good
    # coat, Mrs. Dunn in her cardigan, and Ricky with his pockets full.
    'literary_drawing_albright': Look('#d6ae90','#5a4a3a','part','#e2ddd0','#3a3c44','shirt','#6a6e7a',eyes='#4a5a5a',age=1),
    'literary_drawing_rudd': Look('#c99a78','#8a8478','receding','#5a6a7a','#3a3e44','cardigan','#cfc4b0',eyes='#4a4a40',age=2,glasses=True),
    'literary_drawing_nell': Look('#e0b896','#8a4a2a','bun','#b8645a','#4a4038','apron','#f0e6d0',eyes='#4a6050'),
    'literary_drawing_dale': Look('#c99878','#5a3a22','short','#5a6a4a','#3a3e48','flannel','#c8bca0',eyes='#4e5a50',beard='stubble'),
    'literary_drawing_bess': Look('#e2c2a6','#a86a3a','ponytail','#6a8aaa','#3a4a5a','tee','#f0e6d0',eyes='#4a6a5a',sleeve=4,face_style='child'),
    'literary_drawing_pike': Look('#d8b49a','#e0ddd4','receding','#4a4038','#3a3630','coat','#bfb39a',eyes='#5a5a52',age=2,beard='walrus'),
    'literary_drawing_dunn': Look('#d2a283','#7a7a74','bun','#6a5a7a','#3a3640','cardigan','#cfc4b0',eyes='#5a5a52',age=2),
    'literary_drawing_ricky': Look('#c48a68','#3a2a1e','messy','#8a3a2e','#2e3a4a','tee','#d8d0c0',eyes='#3a3029',sleeve=4,face_style='child'),
    # 0.4.408, The House on the Corner: the girl with one arm (her left is not drawn; her sleeve is pinned), her brother, their friend, and her brother older.
    'literary_corner_romina': Look('#c9967a','#2a1e18','braids','#d8b84a','#3a3a44','dress','#f0e6d0',eyes='#3a2e26',sleeve=4,face_style='child'),
    'literary_corner_nacho': Look('#c48e6c','#2a1e18','messy','#3a5a7a','#2e3440','tee','#d8d0c0',eyes='#3a2e26',sleeve=4,face_style='child'),
    'literary_corner_luli': Look('#e0b896','#6a3a22','ponytail','#c8607a','#3a4a5a','tee','#f0e6d0',eyes='#4a5a4a',sleeve=4,face_style='child'),
    'literary_corner_nacho_grown': Look('#c48e6c','#2a1e18','short','#4a4a52','#2e3440','jacket','#8a8478',eyes='#3a2e26',beard='stubble'),
    # The Kept (0.4.410): the four the machine keeps, thin and worn through, in what is left of their clothes.
    'literary_kept_silas': Look('#bfa894','#9a968c','receding','#5a5a52','#3a3a36','ragged','#7a7868',eyes='#5a5c58',age=2,beard='full',mark='despair'),
    'literary_kept_petra': Look('#c8a088','#2a2420','long','#4a4e52','#34363a','ragged','#6a6e70',eyes='#4a3c30',age=1,mark='despair'),
    'literary_kept_wendell': Look('#c49a7c','#6a5a4a','receding','#6a5a44','#3e3a34','ragged','#8a7a5a',eyes='#5a4a3a',age=1,glasses=True,beard='stubble',mark='despair'),
    'literary_kept_tamsin': Look('#d8b49a','#4a3426','close','#5a6a6a','#3a4044','ragged','#7a8a88',eyes='#4a5a4a',mark='despair'),
    # Last orders (0.4.416): the man in booth four, pale as cut stone in a good dark coat; the two in booth three; the three at the board
    # in old coats; and Benny on the far stool.
    'literary_orders_stranger': Look('#cfcac0','#2e2c2a','receding','#232326','#1c1c1f','coat','#bdb8ae',eyes='#5c605e',age=2),
    'literary_orders_ferris': Look('#c49a7c','#6a5a4a','receding','#4e4034','#2e2a26','jacket','#8a7a5a',eyes='#4a3a2a',age=2,beard='walrus'),
    'literary_orders_mags': Look('#d6aa8c','#8a7a6a','bun','#5a3e4a','#2e2a2e','cardigan','#c8b8a0',eyes='#4a4a3a',age=2),
    'literary_orders_hob': Look('#b88a68','#3a3028','short','#3a4a3a','#2a2e2a','coat','#6a6a4a',eyes='#3a3a2a',age=2,beard='full'),
    'literary_orders_ott': Look('#caa080','#8a8478','receding','#4a3e30','#2a2620','waistcoat','#a89878',eyes='#5a5040',age=2,beard='chops'),
    'literary_orders_cass': Look('#a8785a','#2a2220','close','#3e3a4a','#26242c','coat','#8a8698',eyes='#3a2e26',age=1),
    'literary_orders_benny': Look('#d0a284','#4a3a2e','short','#7a4a32','#2a2a2e','flannel','#c8a878',eyes='#3a3a3a',age=1,beard='moustache'),
    # Borrasca's town (0.4.425): the shops' keepers and the drunk in the Sheriff's cell.
    'literary_drisking_lorna': Look('#d8ae90','#9e5a36','bun','#5d8c86','#3b3b40','apron','#efe6d2',eyes='#5a6a50',age=1),
    'literary_drisking_ames': Look('#c9a07e','#7a7066','receding','#3e5a7a','#3a3530','apron','#b8a07a',eyes='#4a4a3a',age=2,glasses=True),
    'literary_drisking_rowe': Look('#d4ae94','#cfcac0','receding','#5a4632','#2e2a26','waistcoat','#c8b890',eyes='#5a5a50',age=2,glasses=True,beard='chops'),
    'literary_drisking_kurt': Look('#c79a78','#3a2c22','messy','#4a5a6a','#2e3540','uniform','#c84a32',eyes='#3a3a2e',beard='moustache'),
    'literary_drisking_odell': Look('#e0bca0','#6a4a3a','bun','#8aa0b8','#4a4a55','cardigan','#e8e0d0',eyes='#4e5a6a',age=1,glasses=True),
    'literary_drisking_fenn': Look('#d2aa8c','#5a4a3e','short','#e4e4dc','#3a3a40','coat','#d8d8d0',eyes='#4a5058',age=1,glasses=True),
    'literary_drisking_gus': Look('#b88a6a','#2a2420','close','#2e2e32','#2a2a2e','vest','#d8d0c0',eyes='#3a2e26',age=1,beard='full'),
    'literary_drisking_earl': Look('#c49478','#8a8072','messy','#6a5a3e','#3a3a34','flannel','#8a3a2e',eyes='#6a5a4a',age=2,beard='full'),
    # The town's day (0.4.426): the Webbs at the diner counter, the Tates at the doctor's, two retired deputies at the bar.
    'literary_drisking_webb': Look('#d0a688','#6a4a32','short','#5a6e8a','#3a3e48','shirt','#c8c0a8',eyes='#4a5a4a',age=1),
    'literary_drisking_sherry': Look('#e2bea2','#b08a52','long','#c87a6a','#4a4a5a','cardigan','#f0e4d0',eyes='#5a6a7a',age=1),
    'literary_drisking_roy': Look('#b88866','#2e241c','short','#7a5a3a','#2e3640','flannel','#c8b088',eyes='#3a2e24',beard='moustache'),
    'literary_drisking_jolene': Look('#dcb092','#4a2e22','ponytail','#8aa86a','#5a5a6a','dress','#e8e0c8',eyes='#4a5a3a',sleeve=4),
    'literary_drisking_dutch': Look('#c89a7a','#bdb8ae','close','#5a5a4a','#3a3a34','jacket','#a89a7a',eyes='#5a5a50',age=2,beard='walrus'),
    'literary_drisking_merle': Look('#d2a888','#9a948a','receding','#4a5a6a','#2e3238','cardigan','#c8c0b0',eyes='#4a5058',age=2,glasses=True),
    # The town's routines (0.4.428): Mr. Dorsey, the mail carrier, in the summer uniform.
    'literary_drisking_dorsey': Look('#c49a7c','#4a3a2e','short','#8aa4c0','#2e3a52','uniform','#d8d0b8',eyes='#4a4a42',age=1,beard='moustache'),
    'literary_polybius_kid_a': Look('#d9b08e','#8a5a32','long','#3a5a8a','#2f3a52','jacket','#d8cfb8',eyes='#4e6a7a',sleeve=10),
    'literary_polybius_kid_b': Look('#a8714f','#1e1a16','close','#c0442e','#3a3f48','tee','#f0e6d0',eyes='#3a2c22',sleeve=4),
    'literary_polybius_kid_c': Look('#e2c2a6','#c89a5a','messy','#d8d4c8','#4a5560','knit','#2e5a8a',eyes='#56677a',glasses=True,sleeve=10),
    # 0.4.220 (owner: Brad's textures were "not in line with the mod"): Brad in the God of the Pitt, painted like the rest of the
    # cast instead of built from blocks: a mud-stained orange oversuit, the harness over it, a short beard, his caving helmet
    # with the lamp on its front (stranger phases forty-four to forty-six: lamp dark, flashing white, strobing amber).
    'literary_pitt_brad': Look('#c79a7a','#5a3e2a','close','#a8502c','#93462a','crew','#2e2e2c',eyes='#4a5866',age=1,beard='short',mark='caver',sleeve=10),
    # The replacement (0.4.218): the one who keeps house in the open house on the street, on stranger phase forty-three.
    'literary_replacement': Look('#cdb29a','#7a7068','messy','#7c6c52','#4a443c','cardigan','#9a8c72',eyes='#4e4c44',age=2),
    'literary_usher_roderick': Look('#ddd4ca','#3a312b','messy','#252229','#29262a','coat','#d6cfc0',eyes='#2c323e',age=1),
    'harrigan': Look('#c7ab91','#b9b7ac','receding','#343941','#2c3037','suit','#7d3940',age=2,glasses=True,logical=64),
    'harrigan_dead': Look('#b9b2a4','#b9b7ac','receding','#343941','#2c3037','suit','#7d3940',age=2,mark='dead',logical=64),
    'harrigan_mourner_0': Look('#ae7b59','#262320','close','#292e33','#26292f','suit','#343338',mark='mourner_flower',logical=64),
    'harrigan_mourner_1': Look('#d0ab8b','#7c7164','part','#454342','#343338','suit','#65544b',age=1,mark='mourner_pocket',logical=64),
    'harrigan_mourner_2': Look('#cfaa8b','#a7a295','receding','#34485d','#303e4e','suit','#603c46',age=2,glasses=True,mark='mourner_watch',logical=64),
    'holloway': Look('#b98b69','#5a4230','part','#5b6148','#51483a','field','#b49355',age=1,beard='chinstrap',logical=64),
    'finale_witness': Look('#c0a184','#ada794','receding','#615746','#484438','coat','#98846a',age=2,beard='walrus',logical=64),
    'caged_child': Look('#c9a283','#4b342a','messy','#79766b','#494a46','ragged','#b4aa8c',eyes='#6c6252',logical=64),
    'lake_boy': Look('#c39a77','#55402d','messy','#706248','#4b4e46','shirt','#b7a478',logical=64),
    # 0.4.101: the Shallows' other boys. Lonnie holds her feet; Hank and Clay stand on the bank with Bud.
    'lake_boy_1': Look('#bf9171','#2f241d','close','#7a3a2e','#3d4148','flannel','#b59a6a',eyes='#4a3c30',logical=64),
    'lake_boy_2': Look('#d8ae8e','#a4562e','short','#56664c','#55503f','knit','#c9b98f',eyes='#5a6a4a',logical=64),
    'lake_boy_3': Look('#d2a684','#c9a35a','messy','#4f6f8f','#3b3f4c','tee','#e0d6bc',eyes='#506a80',logical=64),
    'lake_congregant': Look('#c6aa8c','#c4c1af','receding','#615349','#46443f','suit','#82755e',age=2,logical=64),
    'lake_congregant_1': Look('#ba9271','#5c4935','part','#667051','#464a3d','suit','#7d654d',age=1,logical=64),
    'lake_congregant_2': Look('#cea487','#584035','bun','#743f49','#4d383c','dress','#bd9c7a',logical=64),
    'lake_congregant_3': Look('#c2a38a','#a7a397','receding','#344452','#323c45','suit','#766e5c',age=2,logical=64),
    'lake_preacher': Look('#c2a185','#9a9584','receding','#333837','#303533','cleric','#ded5ba',age=2,logical=64),
    'lake_witch_memory': Look('#c49a80','#48332a','long','#50687a','#3b4b58','dress','#8b9da1',logical=64),
    'lake_witch': Look('#969d82','#2c2c25','long','#425358','#343f42','ragged','#7d8a7c',mark='witch',logical=64),
    'ghosts_marjorie': Look('#dfb79f','#573a2d','long','#887091','#3b4b5a','tee','#ae99ae',eyes='#6c765a',sleeve=11,face_style='young_teen',level_eyes=True),
    'ghosts_merry': Look('#ddba9b','#805a38','ponytail','#ac5847','#47657a','tee','#d5b890',eyes='#72837d',mark='freckles',sleeve=4,face_style='child',level_eyes=True),
    'ghosts_john': Look('#c29b7d','#68482e','part','#727c50','#554d40','flannel','#b1b38b',age=1),
    'ghosts_sarah': Look('#d4ad91','#644a36','bun','#56738a','#454f59','cardigan','#c4ba9e'),
    'ghosts_barry': Look('#b89578','#3e3229','close','#394e65','#3a434d','crew','#c7c5ad',age=1),
    'ghosts_father_wanderly': Look('#d3b294','#b9b4a4','receding','#323a37','#303631','cleric','#e5ddc7',age=2),
    'literary_mother': Look('#be9173','#4f3027','bun','#643c49','#46323a','dress','#a98480',age=1),
    'literary_dancer': Look('#d1ad8d','#674530','part','#333b4a','#2d3340','suit','#a47d4c',mark='mask'),
    'literary_red_figure': Look('#c4a58d','#6b4030','short','#772f39','#4b2730','coat','#aa6254',mark='red_mask'),
    'literary_coffin_woman': Look('#d2b49b','#72523d','long','#d7c9a8','#aa9d85','dress','#9c8c6d',mark='dead'),
    'literary_lady': Look('#b7b2ac','#373735','long','#454849','#383c3f','dress','#8c8d87',mark='dead'),
    'literary_brother': Look('#c5a183','#62462e','messy','#5d778c','#444b50','shirt','#a7b5ae'),
    'literary_father': Look('#b98f70','#4b3628','short','#695a42','#4b463a','field','#9b8460',age=1,beard='full'),
    'literary_visitor': Look('#c8a381','#7b5c40','part','#75644d','#4e493c','coat','#b6a588',age=1),
    'literary_killer': Look('#b9ad8f','#282722','close','#484a38','#34362d','killer','#797354',mark='killer'),
    'literary_family_father': Look('#b8ad92','#514a3a','part','#505846','#3d4235','suit','#a49a7d',eyes='#797d67',age=1,mark='family_father'),
    'literary_family_mother': Look('#c3b4a1','#594035','bun','#8b625d','#5d4843','dress','#b5a48c',eyes='#858679',mark='family_mother'),
    'literary_family_child': Look('#c7bfa5','#63513b','messy','#596c78','#3e4c56','tee','#a2aea9',eyes='#7c8676',mark='family_child',sleeve=4),
    'literary_old_man': Look('#bda188','#b7af99','receding','#6c5b42','#4c473a','coat','#a28a64',age=2,beard='full'),
    'literary_stranger_0': Look('#c6a586','#6a533c','part','#62664a','#4b4e3a','coat','#b4aa86',age=1),
    'literary_stranger_1': Look('#c8aa8c','#674a39','part','#795944','#4c4035','shirt','#a98a6b',age=1,glasses=True),
    'literary_stranger_2': Look('#a77a5a','#30231d','bun','#733b4f','#49333d','dress','#b69b76'),
    'literary_stranger_3': Look('#cbb29b','#b4ab98','receding','#3b4753','#343b44','suit','#a78e69',age=2,beard='horseshoe'),
    'literary_bar_marla': Look('#a87554','#38251e','bun','#753e4c','#3c3932','apron','#a89e80',age=1),
    'literary_bar_russ': Look('#ba9875','#7f6b4b','part','#536e83','#444e54','flannel','#a3b0a9',age=1,beard='short'),
    'literary_bar_denny': Look('#bd9270','#343030','close','#4c6170','#425f73','vest','#8ca1ad',beard='goatee',sleeve=0),
    'literary_bar_wade': Look('#c7a18a','#75604c','receding','#854a45','#4f443e','flannel','#b58b76',age=1,beard='horseshoe'),
    'literary_bar_hollis': Look('#c3a58b','#b0a793','receding','#655544','#4c4436','coat','#baa98a',age=2,beard='chops'),
    'literary_lake_tess': Look('#e0bda4','#28242a','long','#33445d','#33445d','swim','#d4d8d1',eyes='#415878',mark='freckles',sleeve=0),
    # 0.4.152: the young man in Tess's place for a reader the House reads as drawn to men. A navy swim shirt and trunks, barefoot.
    'literary_lake_theo': Look('#d6ae8f','#3b2b22','messy','#2f4058','#2f4058','tee','#d4d8d1',eyes='#5a4636',mark='trunks',sleeve=4),
    'literary_lake_wax': Look('#caba91','#746448','long','#575443','#514b38','dress','#d3c79f',mark='wax'),
    'seance_medium': Look('#c9a68a','#6a5040','bun','#533b50','#392f3c','dress','#d1bea0',age=1),
    'seance_mother': Look('#d2ae8e','#61442d','bun','#824954','#56393f','dress','#d0b999'),
    'seance_father': Look('#c39a7a','#584535','part','#514a3f','#3d3b34','suit','#baa48a',age=1,beard='shadow'),
    'seance_daughter': Look('#d9b69c','#6d4f38','long','#68869d','#4d6477','dress','#d7ceba'),
    # Miniatures (0.4.121): the mother in the upper corner, in a slept-in nightgown, the eyes turned up white.
    'literary_miniature_mother': Look('#d6c6b6','#231a16','long','#d3cbbb','#c9c1b0','dress','#a99f8f',eyes='#2a2624',mark='possessed'),
}

for group in ('crew', 'guest'):
    for i, (skin, hair, cloth) in enumerate([
        ('#c69b7b','#5e4632','#65724c'),('#ae825f','#453023','#57788e'),
        ('#c2a289','#b1a68c','#726653'),('#a67c5a','#2d241e','#52646c')]):
        LOOKS[f'literary_{group}_{i}'] = Look(skin,hair,'receding' if i==2 else 'part',cloth,'#454941','field' if group=='crew' else 'shirt','#a99d7c',age=1 if i==2 else 0,beard={0:'stubble',2:'shadow'}.get(i,'') if group=='crew' else ('shadow' if i==2 else ''),mark='hardhat' if group=='crew' else '')

# The passengers are distinct people, not the four road workers in different props.
LOOKS.update({
    'literary_guest_0':Look('#d3ad92','#824e35','long','#7c4350','#4c393f','dress','#ba9a7d'),
    'literary_guest_1':Look('#b88d6a','#4c3425','part','#56738b','#424c53','shirt','#b8ad90',beard='goatee'),
    'literary_guest_2':Look('#c2a48b','#a79f8d','receding','#667052','#474a3c','suit','#b0a180',age=2),
    'literary_guest_3':Look('#c19b7e','#69503a','part','#75604c','#4b4439','coat','#beaa8a',age=1),
})

for i, (skin, hair, cut, cloth, style) in enumerate([
    ('#d6ae8b','#65462d','messy','#647244','tee'),
    ('#d5ad91','#8c5b3c','long','#a66570','tee'),
    ('#b88e69','#493226','short','#526c89','tee'),
    ('#dbad8b','#a06937','messy','#926544','flannel'),
    ('#d1a487','#4c332b','part','#823c49','tee'),
    ('#b58c67','#302a24','close','#5d8393','shirt'),
    ('#d8ac8c','#8b592f','short','#82704b','tee'),
    ('#9f7452','#30231e','braids','#7b5671','tee'),
    ('#d4ad8d','#57402e','messy','#70899d','shirt')]):
    LOOKS[f'trailer_child_{i}'] = Look(skin,hair,cut,cloth,'#4e5b63',style,'#c4bb9d',eyes=('#596b62','#5a6377','#765e3d')[i%3],sleeve=4 if style=='tee' else 9,logical=64)

HOTEL = [
    Look('#b58c68','#35271f','part','#7d303c','#373a36','uniform','#c29c56',beard='pencil'),
    Look('#c99c78','#3e2c22','part','#323a39','#303632','waistcoat','#d8c9a9',beard='handlebar'),
    Look('#c4a084','#6e5741','part','#5c5644','#454237','suit','#ac926c',mark='scar'),
    Look('#caa98d','#b1a18c','receding','#343d4a','#303746','suit','#ad9568',age=2,beard='moustache'),
    Look('#c5a083','#503225','part','#514034','#39372f','suit','#c0a47b'),
    Look('#d0a58b','#673e2c','long','#865069','#5c3e4c','dress','#c3a16c',sleeve=0),
]
for prefix in ('hotel','stanley'):
    for role, look in zip(('porter','bartender','scarred_guest','pianist','dancer','dancer_dress'),HOTEL):
        LOOKS[f'{prefix}_{role}']=look
LOOKS['stanley_bath_ghost']=Look('#a3ab99','#aab1a5','long','#b8baa7','#9a9f90','dress','#76816e',eyes='#d4d6bb',age=3,sleeve=0)

CLUB = [
    ('male',Look('#c7a17c','#51392c','short','#434b50','#2f353a','tee','#8a9799',beard='patchy',sleeve=4)),
    ('female',Look('#b58463','#482b24','long','#8a405a','#36323b','club_dress','#c09093',sleeve=0)),
    ('vest',Look('#9a7052','#29221d','close','#d8ccb6','#3a3935','tank','#9e927b',sleeve=0)),
    ('copper',Look('#d1a38a','#9a5635','short','#3c6b67','#353e3d','tank','#8ea79c',sleeve=0)),
    ('mustard',Look('#c79c79','#3d2d25','messy','#a38543','#3b3e3e','jacket','#c4ad70',sleeve=9)),
    ('silver',Look('#d1b39c','#4b3731','long','#b5b5b7','#4d4f58','tank','#d6d1cc',sleeve=0)),
    ('brick',Look('#aa7959','#35271e','short','#9a5148','#3c3938','tee','#ba8c75',beard='moustache',sleeve=4)),
    ('braids',Look('#986e51','#27221f','braids','#3b526c','#343e50','club_dress','#ac9671',sleeve=0)),
    ('dj',Look('#bd9474','#31251f','messy','#586b4f','#303a3d','jacket','#c6ae74',sleeve=9)),
]
for role, look in CLUB:
    name='nightclub_'+('dj' if role=='dj' else 'dancer_'+role)
    LOOKS[name]=look
    LOOKS[name+'_despair']=replace(look,mark='despair')
    # 0.4.240 (owner: "the dancers should gradually become dismayed ... their faces should reflect these changes too"): the face
    # between dancing and dismay, when the music has stopped and they have not yet understood it.
    LOOKS[name+'_unsure']=replace(look,mark='unsure')

LOOKS['ghosts_marjorie_dinner']=replace(LOOKS['ghosts_marjorie'],mark='sauce')
LOOKS['ghosts_john_hand']=replace(LOOKS['ghosts_john'],mark='hand')
LOOKS['literary_father_bandaged']=replace(LOOKS['literary_father'],mark='bandaged')
LOOKS['literary_father_mangled']=replace(LOOKS['literary_father'],mark='wound')
for role,skin in [('father','#a6a28c'),('mother','#b5ada1'),('child','#b8baa2')]:
    LOOKS[f'literary_family_{role}_changed']=replace(LOOKS[f'literary_family_{role}'],skin=skin,mark=f'family_{role}_changed')

# The peripheral family's production mesh uses the standard 64x64 human UV,
# not the older literary family's wider costume atlas. Preserve those originals.
for index,role in enumerate(('father','mother','child')):
    LOOKS[f'uncanny_family_{index}']=replace(LOOKS[f'literary_family_{role}_changed'],logical=64,face_style='hollow_'+str(index))
LOOKS['uncanny_family_3']=replace(LOOKS['uncanny_family_2'],hair='#332e2b',cut='long',cloth='#777365',pants='#565247',style='dress',sleeve=9,face_style='hollow_3')
LOOKS['uncanny_bent_neck']=Look('#b19e90','#252625','long','#484c48','#3a3d3b','dress','#95877b',eyes='#666c61',age=1,mark='bent_injury',sleeve=10,logical=64,face_style='injured_neck')


def coil_panel(size,color):
    """Tight coils: small offset clusters, each with its lit and shaded side."""
    w,h=size;im=Image.new('RGBA',size,rgb(color)+(255,));d=ImageDraw.Draw(im);lo=shade(color,-17);hi=shade(color,13)
    for y in range(0,h,3):
        for x in range((y//3)%2*2,w,4):
            d.point((x,y),fill=hi)
            if x+1<w and y+1<h:d.point((x+1,y+1),fill=lo)
    return im


def net(u,v,w,h,d):
    return {'top':(u+d,v,u+d+w,v+d),'bottom':(u+d+w,v,u+d+2*w,v+d),
            'right':(u,v+d,u+d,v+d+h),'front':(u+d,v+d,u+d+w,v+d+h),
            'left':(u+d+w,v+d,u+2*d+w,v+d+h),'back':(u+2*d+w,v+d,u+2*d+2*w,v+d+h)}


def panel(size, color, kind='cloth', side='front'):
    """Few deliberate flat tone clusters. No random grain and no drawn-on lighting."""
    w,h=size; im=Image.new('RGBA',size,rgb(color)+(255,)); d=ImageDraw.Draw(im)
    lo=shade(color,-12);hi=shade(color,9)
    if side in ('left','right'): d.rectangle((0,0,w-1,h-1),fill=shade(color,-5))
    if kind in ('cloth','pants'):
        d.line((0,1,0,h-2),fill=lo);d.line((w-1,1,w-1,h-2),fill=lo)
        if w>=6:d.line((1,2,1,max(2,h//3)),fill=hi)
        if h>=16:
            d.line((1,h-3,w-2,h-3),fill=lo)
            d.line((w-3,h//2,w-2,h//2+1),fill=lo)
    elif kind=='hair':
        for x,y,length in ((1,1,4),(5,4,5),(9,1,6),(13,5,4),(3,10,4),(11,11,3)):
            if x<w-1 and y<h-1:
                d.line((x,y,x,min(h-2,y+length)),fill=hi)
                if x+1<w:d.line((x+1,y+2,x+1,min(h-1,y+length+1)),fill=lo)
    elif kind=='skin':
        d.line((0,1,0,h-1),fill=lo)
        if w>3:d.line((1,1,min(w-2,3),1),fill=hi)
    return im


DIGITS={'0':['111','101','101','101','111'],'1':['010','110','010','010','111'],'2':['111','001','111','100','111'],'3':['111','001','011','001','111'],
        '4':['101','101','111','001','001'],'5':['111','100','111','001','111'],'6':['111','100','111','101','111'],'7':['111','001','010','010','010'],
        '8':['111','101','111','101','111'],'9':['111','101','111','001','111']}


def costume_face(look):
    """A Lake Witch costume's face, if it covers or changes the wearer's own: a rubber mask, a cardboard one, face paint."""
    c=set(look.costume.split())
    if 'slasher' in c:
        # 0.4.364, Pinehook: a pale rubber slasher mask over the whole head, its eyeholes cut crooked, a slit for a mouth, a seam at the jaw.
        im=panel((16,16),'#d8d2c0','skin');d=ImageDraw.Draw(im)
        d.rectangle((3,6,5,8),fill='#141414');d.rectangle((10,7,12,9),fill='#141414')
        d.line((7,9,8,9),fill='#bcb6a4');d.line((7,10,7,10),fill='#a8a290')
        d.line((5,12,10,12),fill='#3a3830');d.line((1,14,14,14),fill='#bab4a2')
        for y in (2,4):d.line((4,y,11,y),fill='#cac4b2')
        return im
    if 'mask' in c:
        im=panel((16,16),'#7f8b6d','skin');d=ImageDraw.Draw(im)
        for x in (2,9):d.rectangle((x,5,x+4,9),fill='#22261e');d.point((x+2,7),fill='#c8c070')
        for y in (2,4):d.line((4,y,11,y),fill='#6c7860')
        d.line((7,9,7,11),fill='#5c6652');d.line((3,12,12,12),fill='#262a20');d.line((3,13,12,13),fill='#3a4030')
        for x in (4,6,8,10):d.point((x,12),fill='#e4e0c8')
        return im
    if 'cardboard' in c:
        im=panel((16,16),'#b8905e','skin');d=ImageDraw.Draw(im)
        for y in (3,8):d.line((0,y,15,y),fill='#a47e50')
        d.rectangle((0,0,15,15),outline='#8c6a40')
        for x in (3,10):d.rectangle((x,6,x+2,8),fill='#1c1a18')
        d.line((3,11,5,13,7,11,9,13,11,11,12,12),fill='#1c1a18')
        return im
    if 'paint' in c:
        im=panel((16,16),'#e9e7dd','skin');d=ImageDraw.Draw(im)
        for x in (2,9):d.rectangle((x,5,x+4,9),fill='#2b2b30');d.rectangle((x+1,7,x+3,7),fill='#e9e2ce');d.point((x+2,7),fill=rgb(look.eyes))
        d.line((6,13,9,13),fill='#2b2b30');d.point((5,12),fill='#2b2b30');d.point((10,12),fill='#2b2b30')
        return im
    return None


def family_face(look):
    """Three drawn, bloodless faces; the later forms distort the same people, not a mirrored mask."""
    im=panel((16,16),look.skin,'skin');d=ImageDraw.Draw(im)
    changed=look.mark.endswith('_changed');role=look.mark.split('_')[1]
    skin=rgb(look.skin);hollow=shade(skin,-30);crease=shade(skin,-49)
    socket='#44453c';inside='#302f2b';rim='#807367';bone='#d0cbb6'
    hair=rgb(look.hair);hlo=shade(hair,-12);hhi=shade(hair,13)
    # Hair clusters and face placement stay on the common sixteen-pixel front plane.
    if role=='father':
        d.polygon([(0,0),(15,0),(15,4),(13,3),(10,2),(7,3),(5,5),(2,4),(0,5)],fill=hair)
        d.line((2,1,7,1),fill=hhi);d.line((10,0,10,2),fill=hlo)
        d.line((2,9,2,12,4,14),fill=hollow);d.line((12,10,11,12),fill=hollow)
        d.line((5,4,9,4),fill=shade(skin,-16))
        if changed:
            d.polygon([(2,6),(6,6),(6,8),(5,10),(3,10),(2,8)],fill=socket)
            d.line((3,7,4,7),fill='#acae99');d.point((4,7),fill=inside)
            d.rectangle((10,7,12,9),fill=socket);d.line((10,8,11,8),fill=bone)
            d.line((7,9,7,11,6,12),fill=crease)
            d.polygon([(5,12),(8,12),(10,11),(12,12),(11,15),(7,15),(4,14)],fill=inside)
            d.line((5,12,7,12),fill=bone);d.point((10,12),fill='#b7b39b')
            d.line((4,11,3,13,3,14),fill=rim);d.line((11,15,12,14),fill=crease)
            d.line((12,4,12,5,13,6),fill=shade(skin,-22))
        else:
            d.line((2,6,6,6),fill=socket);d.rectangle((3,7,5,8),fill=hollow)
            d.line((3,7,5,7),fill=bone);d.point((4,7),fill=inside)
            d.line((10,7,13,7),fill=socket);d.line((10,8,12,8),fill=bone);d.point((11,8),fill=inside)
            d.line((3,9,5,9),fill=crease);d.line((11,9,12,9),fill=hollow)
            d.line((8,9,8,11),fill=crease);d.point((7,10),fill=shade(skin,6))
            d.line((5,13,8,13,10,12),fill='#665d50');d.line((6,14,9,14),fill=hollow)
    elif role=='mother':
        d.rectangle((0,0,15,2),fill=hair)
        d.polygon([(0,2),(7,2),(5,4),(3,5),(2,11),(0,14)],fill=hair)
        d.polygon([(11,2),(15,2),(15,15),(13,13),(13,5)],fill=hair)
        d.line((1,2,1,9),fill=hhi);d.line((14,4,14,12),fill=hlo)
        d.line((4,10,3,12,4,14),fill=hollow);d.line((11,10,11,12),fill=hollow)
        if changed:
            d.polygon([(3,6),(6,6),(6,9),(4,10),(3,9)],fill=socket)
            d.line((4,7,5,7),fill=bone);d.point((5,7),fill=inside)
            d.rectangle((10,8,12,10),fill=socket);d.line((11,9,12,9),fill='#b7b7a7')
            d.line((8,9,8,11,9,12),fill=crease)
            d.polygon([(4,12),(6,13),(8,13),(11,11),(12,12),(10,15),(6,15),(4,14)],fill=inside)
            d.point((5,13),fill=bone);d.line((9,13,10,12),fill='#bbb6a0')
            d.line((3,11,3,13),fill=rim);d.line((11,14,12,13),fill=crease)
        else:
            d.line((3,6,6,6),fill=socket);d.line((3,7,5,7),fill=bone);d.point((5,7),fill=inside)
            d.line((10,7,12,7),fill=socket);d.line((10,8,12,8),fill=bone);d.point((11,8),fill=inside)
            d.line((4,8,5,8),fill=hollow);d.line((10,9,12,9),fill=crease)
            d.line((8,9,8,11),fill=crease);d.point((7,10),fill=shade(skin,5))
            d.line((5,12,6,13,9,13,11,12),fill='#756558');d.line((7,14,9,14),fill=hollow)
    else:
        d.polygon([(0,0),(15,0),(15,4),(13,3),(12,5),(10,3),(8,4),(6,2),(4,5),(2,3),(0,5)],fill=hair)
        d.line((2,1,5,1),fill=hhi);d.line((9,1,11,1),fill=hhi);d.line((14,1,14,3),fill=hlo)
        d.line((2,10,3,12),fill=hollow);d.line((13,9,12,12),fill=hollow)
        if changed:
            d.rectangle((3,6,6,9),fill=socket);d.rectangle((10,7,12,10),fill=socket)
            d.point((5,7),fill=bone);d.line((10,8,11,8),fill='#babcaa')
            d.line((7,9,7,11),fill=crease)
            d.polygon([(6,12),(9,11),(10,12),(10,15),(7,15),(6,14)],fill=inside)
            d.point((8,12),fill=bone);d.line((5,12,5,14),fill=crease)
            d.line((11,11,11,13),fill=hollow)
        else:
            d.line((3,6,6,6),fill=socket);d.line((3,7,6,7),fill=bone);d.point((5,7),fill=inside)
            d.line((10,7,12,7),fill=socket);d.line((10,8,12,8),fill=bone);d.point((10,8),fill=inside)
            d.line((4,8,6,8),fill=hollow);d.line((11,9,12,9),fill=hollow)
            d.line((8,9,8,10),fill=crease);d.line((6,12,8,12,9,13),fill='#776e5a')
            d.line((6,13,7,13),fill=hollow)
    return im


def hollow_face(look):
    # Eight native pixels. The propped family retains its original simple, gaping faces.
    skin=rgb(look.skin);hair=rgb(look.hair)
    im=Image.new('RGB',(8,8),skin);d=ImageDraw.Draw(im)
    d.rectangle((0,0,7,1),fill=hair);d.line((0,2,0,6),fill=hair)
    d.line((7,1,7,5),fill=shade(hair,-9));d.point((1,1),fill=shade(hair,10))
    d.line((1,4,1,6),fill=shade(skin,-27));d.line((6,4,6,7),fill=shade(skin,-34))
    dark='#272b29';crease=shade(skin,-52)
    if look.face_style in ('frail','injured_neck'):
        d.line((1,3,2,3),fill=crease);d.line((5,4,6,4),fill=crease)
        d.line((4,4,4,6),fill=shade(skin,-24));d.point((3,7),fill=dark)
        d.line((6,1,6,3),fill=hair)
        if look.face_style=='injured_neck':
            d.rectangle((1,2,2,5),fill='#705e65');d.point((2,4),fill='#574e55');d.line((5,3,6,3),fill='#3b3836');d.point((5,4),fill='#c7bbb0');d.rectangle((3,6,4,7),fill='#473a3b');d.point((4,5),fill='#927569')
    else:
        d.rectangle((1,2,6,7),fill=skin)
        d.point((2,3),fill='#32352f');d.point((5,3),fill='#32352f')
        d.rectangle((2,4,5,7),fill='#bdb499')
        d.rectangle((2,5,5,6),fill='#302d27')
    return im.resize((16,16),Image.Resampling.NEAREST)


# 0.4.328 (owner: "many of them share that little chin strip"): the beards were all one thin line round the jaw. Each style
# now has its own shape, drawn on the native face grid: eyes at rows 7-8, the nose down to row 10, the mouth on row 13.
BEARDS=('stubble','shadow','patchy','goatee','short','full','chops','walrus','pencil','handlebar','horseshoe','chinstrap','moustache','jawline')
# The jaw and upper lip that stubble and shadow darken: (row, first column, last column) spans.
JAW=((11,1,4),(11,11,14),(12,1,15),(13,1,5),(13,10,14),(14,1,14),(15,1,14))


def mix(a,b,t):
    a=rgb(a) if isinstance(a,str) else a[:3];b=rgb(b) if isinstance(b,str) else b[:3]
    return tuple(round(x+(y-x)*t) for x,y in zip(a,b))


def beard(im,look):
    """The beard on the front of the head, over the face's own lines; the mouth is drawn again where a beard covers it."""
    d=ImageDraw.Draw(im);skin=rgb(look.skin);hair=rgb(look.hair);lo=shade(hair,-14);hi=shade(hair,15)
    style=look.beard;assert style in BEARDS,style
    def tint(x,y,t):im.putpixel((x,y),mix(im.getpixel((x,y)),hair,t)+(255,))
    def spans(rows,c=hair):
        for y,x0,x1 in rows:d.line((x0,y,x1,y),fill=c)
    def dots(points,c):
        for x,y in points:d.point((x,y),fill=c)
    mouth=shade(skin,-58)
    if style in ('stubble','shadow'):
        # Stubble is a stipple over an even shadow; a shaved shadow is the even part alone, a little darker at the chin.
        for y,x0,x1 in JAW:
            for x in range(x0,x1+1):
                if style=='stubble':tint(x,y,0.36 if (x*73+y*151+x*y*31)%7<3 else 0.14)
                else:tint(x,y,{11:0.12,12:0.18}.get(y,0.3 if y==15 else 0.24))
    elif style=='patchy':
        # A young man's beard that has not come in: the chin and the corners of the jaw, a few hairs on the lip.
        spans(((15,6,9),));dots(((5,15),(10,15),(7,14)),mix(skin,hair,0.55))
        for x,y in ((2,13),(2,14),(3,14),(3,15),(13,13),(13,14),(12,14),(12,15),(6,12),(9,12)):tint(x,y,0.5)
    elif style=='goatee':
        spans(((12,6,9),(15,5,10)));dots(((5,13),(10,13),(5,14),(10,14),(7,14),(8,14)),hair)
        dots(((7,12),(7,15)),hi);dots(((5,15),(10,15)),lo)
    elif style=='short':
        # Close-trimmed and boxed: sideburns, a jaw that fills to the chin, a moustache with clean cheek between.
        d.line((0,6,0,11),fill=hair);d.line((15,6,15,11),fill=hair)
        spans(((11,0,1),(11,14,15),(12,0,2),(12,5,10),(12,13,15),(13,0,5),(13,10,15),(14,0,15),(15,0,15)))
        d.line((6,13,9,13),fill=mouth);d.line((6,14,9,14),fill=lo)
        dots(((2,14),(13,14),(7,15)),hi);dots(((0,15),(15,15),(4,14),(11,14)),lo)
    elif style=='full':
        # Up the cheeks to the sideburns and over the lip, the mouth a dark gap in it; strands in two tones.
        d.line((0,5,0,15),fill=hair);d.line((15,5,15,15),fill=hair)
        spans(((9,0,1),(9,14,15),(10,0,2),(10,13,15),(11,0,3),(11,5,10),(11,12,15),(12,0,15),(13,0,5),(13,10,15),(14,0,15),(15,0,15)))
        d.line((6,13,9,13),fill=mouth)
        dots(((3,12),(12,12),(6,11),(9,11),(5,14),(10,14),(2,10),(13,10)),hi)
        dots(((1,11),(2,13),(13,13),(4,15),(11,15),(7,14),(8,15),(14,12)),lo)
    elif style=='chops':
        # Side whiskers widening down to the jaw and joined across the lip; the chin is shaved.
        spans(((10,0,2),(10,13,15),(11,0,3),(11,12,15),(12,0,15),(13,0,5),(13,10,15),(14,0,3),(14,12,15),(15,0,1),(15,14,15)))
        d.line((0,5,0,9),fill=hair);d.line((1,6,1,9),fill=hair);d.line((15,5,15,9),fill=hair);d.line((14,6,14,9),fill=hair)
        d.line((6,13,9,13),fill=mouth);dots(((2,11),(13,11),(7,12)),hi);dots(((1,14),(14,14),(4,13),(11,13)),lo)
    elif style=='walrus':
        # Heavy and drooping, down over the mouth: only a shadow of the lower lip shows under it.
        spans(((11,5,10),(12,4,11),(13,4,11)));d.line((6,14,9,14),fill=shade(skin,-30))
        dots(((6,11),(9,11),(5,12),(10,12)),hi);dots(((4,13),(11,13),(6,13),(9,13)),lo)
    elif style=='pencil':
        # A thin trimmed line just under the nose, lighter where it parts, with clean skin between it and the mouth.
        spans(((11,5,6),(11,9,10)));dots(((7,11),(8,11)),mix(skin,hair,0.45))
    elif style=='handlebar':
        # Full under the nose, swept out and up at both ends.
        spans(((11,6,9),(12,4,11)));dots(((3,11),(12,11)),hair);dots(((3,10),(12,10)),hi);dots(((5,12),(10,12)),lo)
    elif style=='horseshoe':
        # Over the lip and straight down past the corners of the mouth to the jaw.
        spans(((11,6,9),(12,5,10),(13,4,5),(13,10,11),(14,4,5),(14,10,11),(15,4,5),(15,10,11)))
        dots(((7,11),(4,14),(11,14)),hi);dots(((5,15),(10,15)),lo)
    elif style=='chinstrap':
        # The one thin line round the jaw, kept for the men who would wear it (and the Drisking sheet painted with it).
        d.line((2,11,3,14,5,15,10,15,12,14,13,11),fill=hair)
    elif style=='jawline':
        # The old stubble, a lighter jaw line with no moustache: only the manor portraits, which keep the faces they were approved with.
        d.line((2,11,3,14,5,15,10,15,12,14,13,11),fill=shade(hair,13))
    if style in ('chinstrap','moustache'):
        d.line((5,11,6,11,6,12,9,12,9,11,10,11),fill=hair)


def beard_side(d,look,side):
    """The jaw of a fuller beard carries round onto the sides of the head, under the ear."""
    if look.beard not in ('full','short','chops') or look.cut in ('long','braids'):return
    hair=rgb(look.hair);front=0 if side=='left' else 10
    top={'full':12,'short':13,'chops':12}[look.beard];bottom=14 if look.beard=='chops' else 15
    d.rectangle((front,top,front+5,bottom),fill=hair)
    d.line((front+1,bottom,front+4,bottom),fill=shade(hair,-14));d.point((front+2,top),fill=shade(hair,15))


def face(look):
    if look.face_style and look.face_style not in ('young_teen','child'):return hollow_face(look)
    if look.mark.startswith('family_'):return family_face(look)
    im=panel((16,16),look.skin,'skin');d=ImageDraw.Draw(im)
    skin=rgb(look.skin);dark=shade(look.skin,-36);cheek=shade(look.skin,-12)
    hair=rgb(look.hair);hlo=shade(hair,-13);hhi=shade(hair,16)
    # A small cheek plane, nose and lip; no portrait image or airbrushed gradients.
    d.line((1,10,1,13),fill=cheek);d.line((14,10,14,13),fill=cheek)
    d.rectangle((7,8,8,10),fill=shade(skin,8));d.line((8,9,8,10),fill=dark)
    d.line((6,13,9,13),fill=shade(skin,-42));d.line((6,14,9,14),fill=shade(skin,-9))
    if look.mark in ('dead','wax'):
        for x in (3,10):d.line((x,8,x+2,8),fill=dark)
    elif look.mark=='possessed':
        # Sunken sockets with the whites showing and no iris; a bloodless mouth held a little open.
        for x in (3,10):
            d.rectangle((x-1,6,x+3,9),fill=shade(skin,-58));d.rectangle((x,7,x+2,8),fill='#e2ded2');d.line((x,6,x+2,6),fill=shade(skin,-80));d.point((x+1,8),fill='#b9b2a4')
        d.rectangle((6,12,9,13),fill='#3a2a2c');d.line((6,12,9,12),fill='#8f7b7a');d.line((4,10,4,11),fill=shade(skin,-30));d.line((11,10,11,11),fill=shade(skin,-30))
    elif look.mark=='witch':
        for x in (2,10):
            d.rectangle((x,6,x+3,9),fill='#333e34');d.rectangle((x+1,7,x+2,7),fill='#c2c197')
        d.rectangle((6,12,9,13),fill='#303a30');d.point((6,12),fill='#c3bfa4');d.point((9,12),fill='#c3bfa4')
    else:
        for x in (3,10):
            d.line((x,6,x+2,6),fill=shade(hair,-4))
            d.rectangle((x,7,x+2,8),fill='#e9e2ce')
            iris=x+(1 if x==3 or look.level_eyes else 0)
            d.line((iris,7,iris,8),fill=rgb(look.eyes));d.point((iris,8),fill='#343331')
        if look.mark=='unsure':
            # Brows lifted a pixel, eyes gone to one side (toward the booth), the mouth drawn in small.
            for x in (3,10):
                d.line((x,6,x+2,6),fill=skin);d.line((x,5,x+2,5),fill=shade(hair,-4))
                d.rectangle((x,7,x+2,8),fill='#e9e2ce');d.line((x,7,x,8),fill=rgb(look.eyes));d.point((x,8),fill='#343331')
            d.line((6,13,9,13),fill=skin);d.line((7,13,8,13),fill=shade(skin,-50))
        if look.mark=='despair':
            d.line((3,5,5,6),fill=hair);d.line((10,6,12,5),fill=hair)
            d.line((4,9,4,11),fill=shade(skin,-25));d.line((11,9,11,12),fill=shade(skin,-25))
            d.line((6,13,7,12,8,12,9,13),fill=shade(skin,-44))
    if look.mark in ('truant','truant_leer'):
        # Tired: shadow under both eyes, one lid (the picture's left) heavier than the other, the mouth a pixel off true.
        for x in (3,10):d.line((x,9,x+2,9),fill=shade(skin,-24))
        d.line((3,7,5,7),fill=shade(skin,-14));d.point((3,8),fill=shade(skin,-30))
        d.line((6,13,9,13),fill=shade(skin,-9));d.line((5,13,8,13),fill=shade(skin,-42))
        if look.mark=='truant_leer':
            # The leer, on the side that shows round a corner (his left, the picture's right): that eye narrowed to a slit under a
            # lowered brow, and that corner of the mouth pulled up, tight.
            d.rectangle((10,5,12,9),fill=skin);d.line((10,6,12,7),fill=shade(hair,-4));d.line((10,8,12,8),fill='#e9e2ce');d.point((11,8),fill=rgb(look.eyes))
            d.line((10,9,12,9),fill=shade(skin,-24))
            d.line((5,13,8,13),fill=shade(skin,-9));d.line((6,13,8,13),fill=shade(skin,-44));d.line((9,12,10,11),fill=shade(skin,-44));d.point((11,11),fill=shade(skin,-20))
    if look.face_style in ('young_teen','child'):
        # A child's plain, fuller face; no adult cheek hollows, heavy brow or painted lip.
        d.rectangle((1,9,2,14),fill=skin);d.rectangle((13,9,14,14),fill=skin)
        d.rectangle((7,8,8,11),fill=skin);d.point((7,9),fill=shade(skin,7));d.point((8,10),fill=shade(skin,-13))
        for x in (3,10):
            d.line((x,6,x+2,6),fill=shade(hair,7));d.rectangle((x,7,x+2,8),fill='#ded8c5')
            d.line((x+1,7,x+1,8),fill=rgb(look.eyes));d.point((x+1,8),fill='#343331')
        d.rectangle((5,12,10,15),fill=skin);d.line((6,13,9,13),fill=shade(skin,-24))
        d.point((4,10),fill=shade(skin,-7));d.point((11,10),fill=shade(skin,-7))
    if look.age:
        d.line((5,3,10,3),fill=cheek)
        for x in (2,13):d.point((x,9),fill=dark)
        if look.age==2:
            d.line((5,11,5,12),fill=cheek);d.line((10,11,10,12),fill=cheek)
            d.line((6,1,10,1),fill=cheek)
    if look.beard:beard(im,look)
    if look.glasses:
        c='#666250';d.rectangle((2,6,6,9),outline=c);d.rectangle((9,6,13,9),outline=c)
        d.line((6,7,9,7),fill=c);d.line((0,7,2,7),fill=c);d.line((13,7,15,7),fill=c)
    covered=costume_face(look)
    if covered is not None:im.paste(covered,(0,0));d=ImageDraw.Draw(im)
    if 'pale' in look.costume.split():
        for x in (3,10):d.line((x-1,5,x+3,5),fill='#4a4248')
        d.line((6,13,9,13),fill='#5a2a34');d.line((6,14,9,14),fill='#7a3a44')
    if look.cut=='receding':
        d.rectangle((0,0,1,5),fill=hair);d.rectangle((14,0,15,5),fill=hair)
        d.line((1,0,3,0),fill=hhi);d.line((12,0,14,0),fill=hhi)
    elif look.cut=='close':
        d.rectangle((0,0,15,1),fill=hair);d.line((0,2,2,2),fill=hlo);d.line((13,2,15,2),fill=hlo)
    elif look.cut=='ponytail':
        # Hair swept off her face, with a short side fringe and the ears left clear.
        d.rectangle((0,0,15,2),fill=hair);d.polygon([(0,2),(7,2),(4,4),(2,5),(2,9),(1,12),(0,12)],fill=hair)
        d.polygon([(11,2),(15,2),(15,12),(14,10),(13,4)],fill=hair)
        d.line((1,2,4,2),fill=hhi);d.line((14,2,14,5),fill=hhi)
    elif look.cut in ('long','bun','braids'):
        d.rectangle((0,0,15,2),fill=hair);d.polygon([(0,2),(6,2),(4,4),(2,5),(2,13),(0,14)],fill=hair)
        d.polygon([(9,2),(15,2),(15,14),(13,12),(13,4)],fill=hair)
        d.line((1,2,1,7),fill=hhi);d.line((14,3,14,10),fill=hhi)
        if look.cut=='braids':
            for y in (5,8,11):d.rectangle((0,y,1,y+1),fill=hhi);d.rectangle((14,y,15,y+1),fill=hhi)
    elif look.cut=='coils':
        # Tight coils standing up from a rounded hairline; the hat layer carries their height.
        d.polygon([(0,0),(15,0),(15,5),(14,4),(13,3),(11,2),(4,2),(2,3),(1,4),(0,5)],fill=hair)
        for x,y in ((1,1),(4,0),(7,1),(10,0),(13,1),(2,3),(14,3)):d.point((x,y),fill=hhi);d.point((min(15,x+1),min(5,y+1)),fill=hlo)
    elif look.cut=='messy':
        d.polygon([(0,0),(15,0),(15,4),(13,3),(12,4),(10,2),(8,4),(6,2),(4,5),(2,3),(0,5)],fill=hair)
        d.line((2,1,5,1),fill=hhi);d.line((9,1,12,1),fill=hhi)
    else:
        d.polygon([(0,0),(15,0),(15,3),(12,2),(8,2),(5,4),(2,3),(0,4)],fill=hair)
        d.line((2,1,7,1),fill=hhi);d.line((10,0,10,2),fill=hlo)
    if look.mark=='possessed':
        for x,y0,y1 in ((2,4,15),(13,4,15)):d.line((x,y0,x,y1),fill=hair)
        d.line((6,3,6,5),fill=hlo);d.line((11,3,11,5),fill=hlo)
    if look.mark=='diner_split':
        # A slack, off-axis opening in the same face; no decorative rows of teeth.
        d.rectangle((5,11,8,15),fill='#382d2b');d.rectangle((7,13,9,15),fill='#382d2b')
        d.line((4,11,4,14),fill=shade(skin,-28));d.line((10,12,10,15),fill=shade(skin,-36))
        d.line((5,10,7,10),fill='#967967');d.line((10,14,12,13),fill=shade(skin,-16))
    if look.mark in ('diner_second','diner_split'):
        for x in (3,10):
            d.rectangle((x,7,x+2,8),fill='#718b60')
            d.line((x+1,7,x+1,8),fill='#e6ead8')
    if look.mark in ('freckles','borrasca_kimber'):
        for x,y in ((3,10),(5,10),(11,10),(12,11)):d.point((x,y),fill=shade(skin,-28))
    if look.mark=='scar':
        d.line((11,5,10,7,12,9,11,11),fill='#9d705e');d.point((12,8),fill=shade(skin,14))
    if look.mark=='sauce':
        d.line((5,12,5,14),fill='#a85c36');d.point((10,13),fill='#a85c36');d.point((7,14),fill='#c07847')
    if look.mark=='changed':
        d.line((3,7,5,7),fill='#625846');d.line((10,7,12,7),fill='#625846');d.line((6,13,9,13),fill='#70594a')
    c=set(look.costume.split())
    if 'yarn' in c or 'mop' in c:
        # A wig worn down over the face: strands of different lengths, with gaps the eyes look out through.
        for x in range(16):
            if x%3==0 and 2<x<14:continue
            length=15 if x<2 or x>13 else 9+(x*7)%6
            d.line((x,0,x,length),fill=hhi if (x*5)%4==0 else hair)
    if 'hood' in c:
        cloth=rgb(look.cloth)
        d.rectangle((0,0,15,2),fill=cloth);d.rectangle((0,0,1,15),fill=cloth);d.rectangle((14,0,15,15),fill=cloth)
        for x in (3,5,8,10,12):d.line((x,3,x,6+x%3),fill=hair)
        for x in (4,9,12):d.point((x,3),fill='#a8acae')
    return im


def borrasca_cloth(im,look,side):
    """The five Drisking outfits, using the same clustered marks as the rest of the cast."""
    d=ImageDraw.Draw(im);w,h=im.size;mid=w//2
    if side in ('top','bottom'):return im
    if look.mark=='borrasca_kyle':
        for y in range(5,h,6):d.rectangle((0,y,w-1,min(h-1,y+1)),fill=rgb(look.accent))
    elif look.mark=='borrasca_kimber':
        if side=='front':
            d.line((mid,3,mid,h-1),fill=rgb(look.accent));d.point((mid+1,9),fill='#eee5c8')
            for x in (2,w-5):d.line((x,h-8,x+2,h-10),fill=shade(look.cloth,-24))
        if side=='back':
            d.line((1,5,w-2,5),fill=shade(look.cloth,-14))
            d.rectangle((mid-2,0,mid+1,7),fill=rgb(look.hair));d.line((mid-2,3,mid+1,3),fill='#52615c')
    elif look.mark=='borrasca_sheriff':
        d.rectangle((0,h-4,w-1,h-1),fill='#393a32')
        if side=='front':
            d.line((1,1,4,1),fill=shade(look.cloth,-25));d.line((w-5,1,w-2,1),fill=shade(look.cloth,-25))
            d.line((4,7,4,11),fill='#c9b879');d.line((2,9,6,9),fill='#c9b879');d.point((4,9),fill='#8c7b4c')
            d.rectangle((w-6,8,w-3,9),fill='#625c47');d.line((w-5,8,w-3,8),fill='#c1bc9b')
            d.rectangle((mid-2,h-3,mid+1,h-2),fill='#b9a16c')
    elif look.mark=='borrasca_prescott':
        vest='#786149'
        if side=='front':
            d.rectangle((0,0,mid-3,h-1),fill=vest);d.rectangle((mid+2,0,w-1,h-1),fill=vest)
            for y in (5,11,17):d.line((0,y,mid-3,y),fill='#66513d');d.line((mid+2,y,w-1,y),fill='#66513d')
            d.rectangle((1,6,4,9),fill='#c2b699');d.rectangle((2,7,3,8),fill='#56664c')
        else:
            d.rectangle((0,0,w-1,h-1),fill=vest)
            for y in (5,11,17):d.line((0,y,w-1,y),fill='#66513d')
    elif look.mark=='borrasca_worker':
        d.rectangle((0,0,w-1,h-4),fill='#b97d42')
        if side=='front':d.rectangle((mid-1,1,mid,h-4),fill=rgb(look.cloth))
        d.line((0,13,w-1,13),fill='#d2cfb7');d.line((0,14,w-1,14),fill='#a9b2a5')
        if side in ('front','back'):
            for x in (2,w-4):d.line((x,2,x,12),fill='#c6c6ae')
            if side=='front':d.rectangle((w-6,7,w-3,10),outline='#85663e')
    elif look.mark=='borrasca_worker_two':
        # A canvas chore coat, buttoned, with two patch pockets and a reflective band low on the back; no vest.
        coat=rgb(look.cloth);d.rectangle((0,h-3,w-1,h-1),fill=shade(coat,-18))
        if side=='front':
            d.line((mid,0,mid,h-1),fill=shade(coat,-26))
            for y in (3,8,13):d.point((mid+1,y),fill='#2f2a22')
            for x0 in (1,w-6):d.rectangle((x0,8,x0+4,12),outline=shade(coat,-22));d.line((x0,8,x0+4,8),fill=shade(coat,14))
            d.line((1,1,mid-2,3),fill=shade(coat,16));d.line((w-2,1,mid+2,3),fill=shade(coat,16))
        elif side=='back':
            d.line((0,h-6,w-1,h-6),fill='#d8d8c6');d.line((0,h-5,w-1,h-5),fill='#a8aea2')
            d.line((2,1,w-3,1),fill=shade(coat,-14))
    return im


def cloth_face(size,look,side='front'):
    im=panel(size,look.cloth,'cloth',side);d=ImageDraw.Draw(im);w,h=size
    c=rgb(look.cloth);a=rgb(look.accent);lo=shade(c,-19);hi=shade(c,16)
    if look.style=='flannel':
        for x in range(3,w,7):d.line((x,0,x,h-1),fill=lo);d.line((x+1,0,x+1,h-1),fill=hi)
        for y in range(4,h,7):d.line((0,y,w-1,y),fill=lo);d.line((0,y+1,w-1,y+1),fill=shade(c,7))
    if look.style=='knit':
        for x in range(2,w-2,4):d.line((x,3,x,min(h-3,13)),fill=shade(c,7))
        if h>7:d.line((1,h-3,w-2,h-3),fill=lo)
    if side=='back':
        d.line((1,2,w-2,2),fill=lo)
        if look.style in ('suit','coat','uniform','waistcoat'):d.line((w//2,3,w//2,h-3),fill=shade(c,-7))
        if look.mark=='cottage_braces':
            for x in range(2,w-1,4):d.line((x,1,x,h-3),fill=shade(c,-17))
            d.line((2,0,w//2,h-4,w-3,0),fill='#58402f',width=2)
        if look.mark=='cottage_apron':
            d.line((1,h-7,w-2,h-7),fill=a);d.line((w//2,h-7,w//2-2,h-2),fill=a)
        if look.mark.startswith('diner_'):
            d.line((0,h-7,w-1,h-7),fill=a,width=2)
            d.line((w//2,h-7,w//2-3,h-4,w//2-1,h-3),fill=shade(a,-18))
            d.line((w//2,h-7,w//2+3,h-5,w//2+1,h-3),fill=a)
        if look.mark=='movie_stripes':
            for y in range(3,h-1,4):d.line((0,y,w-1,y),fill=a);d.line((0,y+1,w-1,y+1),fill=shade(a,-14))
        return borrasca_cloth(im,look,side) if look.mark.startswith('borrasca_') else im
    if side!='front':return borrasca_cloth(im,look,side) if look.mark.startswith('borrasca_') else im
    mid=w//2
    d.rectangle((mid-2,0,mid+1,1),fill=rgb(look.skin))
    if look.style in ('suit','coat','uniform','waistcoat','apron'):
        shirt='#d8cdb8'
        d.polygon([(mid-4,1),(mid+3,1),(mid+2,11),(mid-3,11)],fill=shirt)
        d.polygon([(mid-4,1),(mid-1,4),(mid-3,5)],fill='#f0e6cc')
        d.polygon([(mid+3,1),(mid,4),(mid+2,5)],fill='#eee0c6')
        d.polygon([(mid-1,3),(mid,3),(mid+1,5),(mid,12),(mid-2,10),(mid-1,5)],fill=a)
        d.polygon([(1,1),(mid-4,1),(mid-1,9),(mid-3,14)],fill=hi)
        d.line((mid-4,1,mid-1,9,mid-3,14),fill=lo)
        d.polygon([(w-2,1),(mid+3,1),(mid,9),(mid+2,14)],fill=hi)
        d.line((mid+3,1,mid,9,mid+2,14),fill=lo)
        for y in (15,19):d.point((mid,y),fill=shade(a,-16))
        d.line((2,16,5,16),fill=lo);d.line((w-6,16,w-3,16),fill=lo)
        d.line((w-6,7,w-3,7),fill=lo)
        if look.style=='uniform':
            for y in (10,15,20):d.rectangle((mid-3,y,mid-2,y+1),fill=a);d.rectangle((mid+2,y,mid+3,y+1),fill=a)
        if look.style=='apron':d.rectangle((2,h-10,w-3,h-1),fill=a);d.line((4,h-7,w-5,h-7),fill=shade(a,-25))
    elif look.style=='cleric':
        d.rectangle((mid-3,1,mid+2,2),fill='#262d2b');d.rectangle((mid-1,1,mid,2),fill='#e6debf')
        d.line((mid,4,mid,h-3),fill=lo)
        for y in (7,12,17):d.point((mid+1,y),fill='#93917a')
    elif look.style in ('shirt','flannel','field','crew','jacket','cardigan'):
        d.polygon([(mid-4,0),(mid-1,4),(mid-3,5)],fill=hi)
        d.polygon([(mid+3,0),(mid,4),(mid+2,5)],fill=hi)
        d.line((mid,4,mid,h-2),fill=lo)
        for y in (6,11,16,21):
            if y<h:d.point((mid+1,y),fill=a)
        if look.style in ('field','crew','shirt'):
            for x in (2,w-6):d.rectangle((x,7,x+3,12),outline=lo);d.line((x,7,x+3,7),fill=hi)
        if look.style=='crew':d.rectangle((2,5,5,9),fill=a);d.line((3,6,4,6),fill=lo)
        if look.style=='jacket':
            d.rectangle((mid-2,2,mid+1,h-1),fill=shade(look.pants,12));d.line((mid-3,2,mid-3,h-1),fill=a)
        if look.style=='cardigan':
            d.polygon([(mid-3,0),(mid,7),(mid+2,0)],fill=a)
    elif look.style in ('dress','club_dress','tank','tee','knit'):
        d.line((mid-4,0,mid-3,2,mid-1,3,mid+1,3,mid+3,1),fill=lo)
        if look.style in ('dress','club_dress'):d.line((1,h-6,w-2,h-6),fill=a);d.rectangle((mid-1,h-7,mid,h-5),fill=shade(a,20))
        if look.style=='tank':
            d.rectangle((0,0,1,5),fill=rgb(look.skin));d.rectangle((w-2,0,w-1,5),fill=rgb(look.skin))
    elif look.style=='vest':
        d.rectangle((mid-3,1,mid+2,h-1),fill='#393c3c');d.line((mid-4,2,mid-4,h-1),fill=hi);d.line((mid+3,2,mid+3,h-1),fill=hi)
        for x in (1,w-5):d.rectangle((x,7,x+3,12),outline=lo)
    elif look.style=='swim':
        im=panel(size,look.skin,'skin');d=ImageDraw.Draw(im)
        d.rectangle((2,0,3,7),fill=c);d.rectangle((w-4,0,w-3,7),fill=c)
        d.polygon([(2,6),(mid-1,5),(mid,7),(mid+1,5),(w-3,6),(w-3,10),(2,10)],fill=c)
        d.line((3,6,mid-2,6),fill=a);d.line((mid+1,6,w-4,6),fill=a)
        d.rectangle((0,h-6,w-1,h-1),fill=c);d.line((0,h-6,w-1,h-6),fill=a)
    if look.mark.startswith('diner_'):
        # A work dress, small collar/name pin, bib and tied waist apron, in the shared cast's pixel language.
        d.polygon([(mid-4,1),(mid-1,4),(mid-3,5)],fill=a)
        d.polygon([(mid+3,1),(mid,4),(mid+2,5)],fill=shade(a,9))
        for y in (5,8,11):d.point((mid,y),fill=shade(a,-12))
        d.rectangle((w-6,5,w-3,6),fill='#d1bb8a');d.line((w-5,6,w-3,6),fill='#716046')
        d.rectangle((3,10,w-4,h-1),fill=a)
        d.line((3,10,3,h-1),fill=shade(a,-17));d.line((w-4,10,w-4,h-1),fill=shade(a,-10))
        d.line((0,h-7,w-1,h-7),fill=shade(a,9),width=2)
        d.rectangle((5,h-6,w-6,h-3),outline=shade(a,-22))
    # Cottage garments use deliberate pixel marks, with their original period details.
    if look.mark=='mourner_flower':
        d.line((w-5,5,w-5,9),fill='#69745e');d.point((w-6,8),fill='#899477')
        d.rectangle((w-6,4,w-4,5),fill='#e5dfcc');d.point((w-5,3),fill='#f2ebd8')
    if look.mark=='mourner_pocket':
        d.line((w-7,10,w-3,10),fill=lo);d.line((w-7,9,w-3,9),fill='#e6decc')
        d.point((w-6,8),fill='#f0e9d9');d.point((w-4,8),fill='#f0e9d9')
    if look.mark=='mourner_watch':
        d.polygon([(mid-2,8),(mid+1,8),(mid+2,h-3),(mid-3,h-3)],fill='#49303a')
        d.line((mid+1,14,mid+4,16,mid+6,14),fill='#b09a65');d.point((mid+5,15),fill='#d4bf86')
    if look.mark=='cottage_walter':
        d.rectangle((mid-3,7,mid+2,h-2),fill='#796047')
        for y in range(9,h-2,5):d.point((mid,y),fill='#3c332b')
        d.line((mid+1,h-10,mid+3,h-8,mid+5,h-9),fill='#b69b60')
    if look.mark=='cottage_apron':
        d.rectangle((mid-4,4,mid+3,h-1),fill=a)
        d.line((mid-4,3,mid+3,3),fill='#ede2ca')
        d.line((mid-3,8,mid+2,8),fill=shade(a,-17))
        d.point((mid,2),fill='#a98c58')
    if look.mark=='cottage_braces':
        for x in range(2,w-1,4):d.line((x,1,x,h-3),fill=shade(c,-17))
        for x in (2,w-4):d.rectangle((x,0,x+1,h-3),fill='#58402f')
        d.polygon([(mid-4,0),(mid+3,0),(mid+1,3),(mid,6),(mid-2,4)],fill=a)
        d.rectangle((0,h-2,w-1,h-1),fill=rgb(look.pants))
    if look.mark=='cottage_uniform':
        d.rectangle((1,5,w-2,h-1),fill=c);d.line((mid,5,mid,h-2),fill=lo)
        for y in range(6,h-3,5):
            for x in (mid-3,mid+2):d.rectangle((x,y,x,y+1),fill=a)
        for x,color in ((2,'#843e3c'),(3,'#d1c49b'),(4,'#51667a')):d.line((x,3,x,4),fill=color)
    # Movie Night garments: the father's loud shirt, the boy's stripes, the letterman's letter, the ticket seller's badge.
    if look.mark=='movie_hawaiian':
        for i,(x,y) in enumerate(((2,5),(w-5,4),(4,11),(w-6,12),(2,18),(w-4,19),(mid+3,16),(mid-5,21))):
            if y<h-1:
                d.point((x,y),fill=a);d.point((x+1,y),fill='#f6efe0');d.point((x,y+1),fill='#f6efe0');d.point((x+1,y+1),fill=shade(a,-30))
    if look.mark=='movie_stripes':
        for y in range(3,h-1,4):d.line((0,y,w-1,y),fill=a);d.line((0,y+1,w-1,y+1),fill=shade(a,-14))
    if look.mark=='movie_letter':
        d.rectangle((2,5,6,12),fill=a);d.rectangle((3,6,5,11),fill=c);d.rectangle((3,6,4,8),fill=a);d.point((5,9),fill=a)
    if look.mark=='movie_badge':
        d.rectangle((w-7,6,w-3,8),fill='#efe8d4');d.line((w-6,7,w-4,7),fill='#7a3036')
    if look.mark=='pizza_visor':
        # 0.4.224: the pizza counter's uniform: a name tag nobody reads, a slice embroidered on the other side, an apron's bib below.
        d.rectangle((2,6,6,8),fill='#efe8d4');d.line((3,7,5,7),fill='#3a3a40')
        d.polygon([(w-6,5),(w-3,5),(w-4,8)],fill='#f0b040');d.point((w-5,6),fill='#b02818')
        d.rectangle((mid-4,12,mid+3,h-2),fill='#e8e2d4');d.line((mid-4,12,mid+3,12),fill='#c8c0b0')
        d.point((mid-1,15),fill='#d8b070');d.point((mid+1,18),fill='#c8a060')
    if look.mark=='whistle':
        # 0.4.227: the attendant's lanyard down from the collar, and the whistle on it.
        for k in range(0,6):d.point((mid-4+k//2,4+k),fill='#2a3a6a');d.point((mid+3-k//2,4+k),fill='#2a3a6a')
        d.rectangle((mid-1,10,mid+1,11),fill='#c8c8cc');d.point((mid+2,10),fill='#9a9aa0')
    if look.style=='ragged':
        d.line((mid,3,mid,h-3),fill=lo);d.rectangle((3,h-7,5,h-4),fill=shade(c,16))
        for y in (6,12,18):d.point((mid+1,y),fill=a)
    costume_marks(d,look,w,h)
    if look.number:
        # The entry number, pinned on: a white card with red digits.
        x0,y0=mid-5,5;d.rectangle((x0,y0,x0+9,y0+7),fill='#efece2',outline='#9a968a')
        digits=str(look.number);x=x0+5-(len(digits)*4-1)//2
        for ch in digits:
            for row,bits in enumerate(DIGITS[ch]):
                for col,bit in enumerate(bits):
                    if bit=='1':d.point((x+col,y0+2+row),fill='#a8322c')
            x+=4
    return borrasca_cloth(im,look,side) if look.mark.startswith('borrasca_') else im


def costume_marks(d,look,w,h):
    c=set(look.costume.split())
    if 'bags' in c:
        for x in range(-h,w,6):d.line((x,0,x+h,h),fill='#4d5058')
        for x,y in ((3,5),(11,9),(6,16),(12,19)):d.point((x,y),fill='#6a6e78')
    if 'sheet' in c:
        for x,length in ((2,h-6),(6,h-10),(10,h-4),(13,h-12)):
            if x<w:d.line((x,1,x,length),fill='#4f6b2c');d.point((x+1 if x+1<w else x,length//2),fill='#6a8a3a')
    if 'weed' in c:
        for x,length in ((1,10),(4,16),(8,8),(11,14),(14,11)):
            if x<w:d.line((x,0,x,min(h-1,length)),fill='#3e5a26');d.line((x,length//2,min(w-1,x+1),length//2+2),fill='#56743a')
    if 'gown' in c:
        for x,y in ((3,9),(10,14),(6,19),(12,5)):d.ellipse((x-2,y-2,x+2,y+2),fill='#9fa8aa')
        for x in (5,11):d.line((x,h-6,x,h-1),fill='#8e9799')
    if 'flowers' in c:
        for x,y in ((2,4),(9,7),(5,12),(12,15),(3,19),(10,21)):
            if x<w and y<h:d.point((x,y),fill='#efe0a8');d.point((min(w-1,x+1),y),fill='#e8a8b8')


def paint(look):
    im=Image.new('RGBA',(look.logical*S,64*S))
    def put(box,art):
        x0,y0,x1,y1=[round(x*S) for x in box]
        if x1>x0 and y1>y0:im.paste(art.resize((x1-x0,y1-y0),Image.Resampling.NEAREST),(x0,y0))
    def fill_net(islands,color,kind='cloth'):
        for side,box in islands.items():put(box,panel((round((box[2]-box[0])*S),round((box[3]-box[1])*S)),color,kind,side))
    # Face features belong only on the front; ears belong only on side faces.
    head=net(0,0,8,8,8);fill_net(head,look.hair,'hair')
    put(head['front'],face(look));put(head['bottom'],panel((16,16),look.hair if look.beard=='full' else look.skin,'hair' if look.beard=='full' else 'skin'))
    if look.cut=='receding':
        art=panel((16,16),look.skin,'skin');d=ImageDraw.Draw(art)
        d.rectangle((0,0,1,15),fill=rgb(look.hair));d.rectangle((14,0,15,15),fill=rgb(look.hair));d.rectangle((0,13,15,15),fill=rgb(look.hair));put(head['top'],art)
    for side in ('left','right'):
        art=panel((16,16),look.hair,'hair',side);d=ImageDraw.Draw(art)
        if look.cut not in ('long','braids'):
            front=0 if side=='left' else 10
            d.rectangle((front,5,front+5,15),fill=rgb(look.skin))
            d.rectangle((front+3,7,front+5,11),fill=shade(look.skin,-16));d.line((front+4,8,front+4,10),fill=shade(look.skin,-34))
            if look.glasses:d.line((front,7,front+5,7),fill='#666250')
            beard_side(d,look,side)
        put(head[side],art)
    if look.cut=='ponytail':
        art=panel((16,16),look.hair,'hair','back');d=ImageDraw.Draw(art)
        d.polygon([(0,10),(3,12),(6,13),(9,13),(12,12),(15,10),(15,15),(0,15)],fill=rgb(look.skin))
        d.line((1,5,5,9,7,10),fill=shade(look.hair,12));d.line((14,5,11,9,9,10),fill=shade(look.hair,-11))
        put(head['back'],art)
    if look.cut=='coils':
        for side in ('top','back','left','right'):
            art=coil_panel((16,16),look.hair);d=ImageDraw.Draw(art)
            if side in ('left','right'):
                front=0 if side=='left' else 10
                d.rectangle((front,7,front+5,15),fill=rgb(look.skin))
                d.rectangle((front+3,9,front+5,12),fill=shade(look.skin,-16));d.line((front+4,10,front+4,11),fill=shade(look.skin,-34))
            put(head[side],art)
        for side,box in net(32,0,8,8,8).items():
            if side=='bottom':continue
            art=coil_panel((16,16),look.hair)
            if side!='top':
                # Fuller at the crown and back, thinning to an uneven edge; the face and ears stay clear.
                d=ImageDraw.Draw(art);depth={'front':3,'back':12,'left':6,'right':6}[side]
                for x in range(16):
                    edge=depth+(x*5)%3-1 if side!='front' else (depth if 2<=x<=13 else depth+1)
                    d.line((x,max(0,edge),x,15),fill=(0,0,0,0))
            put(box,art)
    if 'pillow' in look.costume.split():
        for side,box in head.items():
            art=panel((16,16),'#ecebe4','cloth',side);d=ImageDraw.Draw(art)
            if side=='front':
                for x in (3,10):d.rectangle((x,6,x+2,8),fill='#1c1c1e')
                d.line((5,12,10,12),fill='#7a7a80');d.line((0,15,15,15),fill='#d0cfc8')
            put(box,art)
    if 'hood' in look.costume.split():
        for side in ('top','back','left','right'):put(head[side],panel((16,16),look.cloth,'cloth',side))
    if look.mark=='cottage_bowler':
        # The ordinary native hat layer supplies the crown, leaving the actual face clear.
        for side,box in net(32,0,8,8,8).items():
            art=Image.new('RGBA',(16,16));d=ImageDraw.Draw(art)
            if side=='top':art=panel((16,16),'#242326','cloth')
            elif side!='bottom':
                d.rectangle((0,0,15,5),fill='#242326');d.line((0,4,15,4),fill='#111519')
                d.line((1,1,14,1),fill='#38383a')
            put(box,art)
    if look.mark=='pizza_visor':
        # 0.4.224: a paper visor: a band round the head on the hat layer and a brim over the eyes, the hair left showing above.
        for side,box in net(32,0,8,8,8).items():
            art=Image.new('RGBA',(16,16));d=ImageDraw.Draw(art)
            if side=='front':
                d.rectangle((0,2,15,4),fill='#c8302a');d.line((0,5,15,5),fill='#8a1e1a');d.rectangle((5,2,10,3),fill='#f0e2c0')
            elif side in ('left','right','back'):
                d.rectangle((0,2,15,3),fill='#c8302a');d.line((0,4,15,4),fill='#a02420')
            put(box,art)
    if look.mark=='swim_cap':
        # 0.4.227: a tight swim cap over the hair on the hat layer, and dark goggles across the eyes: nothing of the face shows above the nose.
        for side,box in net(32,0,8,8,8).items():
            art=Image.new('RGBA',(16,16));d=ImageDraw.Draw(art)
            if side=='bottom':put(box,art);continue
            d.rectangle((0,0,15,7 if side=='front' else 11),fill='#d8dad6');d.line((0,7 if side=='front' else 11,15,7 if side=='front' else 11),fill='#b8bab6')
            if side=='front':
                d.rectangle((2,8,6,10),fill='#16181c');d.rectangle((9,8,13,10),fill='#16181c');d.line((7,9,8,9),fill='#2a2c30')
                d.point((3,8),fill='#4a5058');d.point((10,8),fill='#4a5058')
            if side in ('left','right'):d.line((0,9,15,9),fill='#202226')
            if side=='back':d.line((0,9,15,9),fill='#202226')
            put(box,art)
    if look.mark=='headset':
        # 0.4.252: a switchboard operator's headset on the hat layer: a black band over the crown, an earpiece over each ear, and the
        # mouthpiece's arm from the right earpiece forward to the corner of the mouth.
        for side,box in net(32,0,8,8,8).items():
            art=Image.new('RGBA',(16,16));d=ImageDraw.Draw(art)
            if side=='top':d.rectangle((0,6,15,8),fill='#1e1e22');d.line((0,7,15,7),fill='#34343a')
            elif side in ('left','right'):
                d.rectangle((6,0,9,6),fill='#1e1e22');d.rectangle((4,7,11,12),fill='#26262a');d.rectangle((5,8,10,11),fill='#3a3a40')
                if side=='right':d.line((11,11,15,13),fill='#26262a');d.line((11,12,15,14),fill='#26262a')
            elif side=='front':d.rectangle((0,12,2,14),fill='#26262a');d.point((1,13),fill='#55555c')
            put(box,art)
    if look.mark=='movie_cap':
        # A flat cloth cap on the hat layer; its brim shades the front edge only.
        for side,box in net(32,0,8,8,8).items():
            art=Image.new('RGBA',(16,16));d=ImageDraw.Draw(art)
            if side=='top':
                art=panel((16,16),'#6b5a44','cloth');d=ImageDraw.Draw(art)
                for x in range(1,15,3):d.line((x,1,x,14),fill='#5b4b38')
            elif side=='front':
                d.rectangle((0,0,15,2),fill='#6b5a44');d.line((0,3,15,3),fill='#3f3426');d.line((1,1,14,1),fill='#7d6a52')
            elif side!='bottom':
                d.rectangle((0,0,15,1),fill='#6b5a44');d.line((0,2,15,2),fill='#4d4030')
            put(box,art)
    if look.mark=='cottage_apron':
        art=Image.new('RGBA',(16,16));d=ImageDraw.Draw(art)
        d.polygon([(5,4),(10,4),(12,6),(12,9),(10,11),(5,11),(3,9),(3,6)],fill=rgb(look.hair))
        d.line((5,5,9,5,11,7),fill=shade(look.hair,15))
        put(net(32,0,8,8,8)['back'],art)
    if look.mark.startswith('borrasca_'):
        if look.mark=='borrasca_kimber':
            art=Image.new('RGBA',(16,16));d=ImageDraw.Draw(art)
            d.rectangle((5,4,10,13),fill=rgb(look.hair));d.line((5,6,10,6),fill='#52615c');d.line((6,8,6,12),fill=shade(look.hair,14))
            put(net(32,0,8,8,8)['back'],art)
        else:
            cap={'borrasca_kyle':'#51677b','borrasca_sheriff':'#97805a','borrasca_prescott':'#4d6650','borrasca_worker':'#bba158','borrasca_worker_two':'#e2ddd0'}[look.mark]
            for side,box in net(32,0,8,8,8).items():
                art=Image.new('RGBA',(16,16));d=ImageDraw.Draw(art)
                if side=='top':
                    art=panel((16,16),cap,'cloth',side);d=ImageDraw.Draw(art)
                    d.line((7,1,7,14),fill=shade(cap,-16))
                    if look.mark in ('borrasca_worker','borrasca_worker_two'):d.line((5,1,5,14),fill=shade(cap,18));d.line((10,1,10,14),fill=shade(cap,18))
                elif side!='bottom':
                    height=7 if look.mark=='borrasca_sheriff' else 5
                    d.rectangle((0,0,15,height),fill=rgb(cap));d.line((0,height,15,height),fill=shade(cap,-27));d.line((1,1,14,1),fill=shade(cap,14))
                    if look.mark=='borrasca_sheriff':d.line((0,4,15,4),fill='#55483a',width=2)
                    if look.mark=='borrasca_prescott' and side=='front':
                        d.rectangle((5,0,10,4),fill='#d6d1b9');d.line((7,1,7,4),fill='#4d6650');d.line((7,1,9,1,9,2,7,2),fill='#4d6650')
                put(box,art)
    if look.mark.startswith('diner_'):
        art=Image.new('RGBA',(16,16));d=ImageDraw.Draw(art)
        d.rectangle((5,5,11,11),fill=rgb(look.hair));d.line((6,5,10,5),fill=shade(look.hair,13))
        d.line((5,7,5,10),fill=shade(look.hair,-13));d.point((11,8),fill=shade(look.hair,8))
        put(net(32,0,8,8,8)['back'],art)
    if look.face_style=='young_teen':
        for u,v,w,h,depth in ((80,0,8,6,1),(80,8,8,10,1),(101,0,2,10,1),(111,0,2,9,1)):
            fill_net(net(u,v,w,h,depth),look.hair,'hair')
    if look.cut=='ponytail':
        for dims in ((80,0,3,7,3),(96,0,2,3,2)):fill_net(net(*dims),look.hair,'hair')
        fill_net(net(110,0,4,2,3),'#b77986','cloth')
    torso=net(16,16,8,12,4)
    for side,box in torso.items():put(box,cloth_face((round((box[2]-box[0])*S),round((box[3]-box[1])*S)),look,side))
    # Classic humanoids mirror one arm/leg; player models have the two extra left nets.
    arms=[net(40,16,3 if look.mark=='witch' or look==LOOKS.get('lake_witch_memory') else 4,12,4),net(32,48,3 if look.mark=='witch' or look==LOOKS.get('lake_witch_memory') else 4,12,4)]
    for arm in arms:
        for side,box in arm.items():
            w,h=round((box[2]-box[0])*S),round((box[3]-box[1])*S)
            art=panel((w,h),look.cloth,'cloth',side);d=ImageDraw.Draw(art)
            if side not in ('top','bottom'):
                cuff=look.sleeve*S
                if look.sleeve==0:art=panel((w,h),look.skin,'skin',side);d=ImageDraw.Draw(art)
                else:
                    if look.style=='flannel':
                        for y in range(4,min(h,cuff),7):d.line((0,y,w-1,y),fill=shade(look.cloth,-19))
                    d.rectangle((0,cuff,w-1,h-1),fill=rgb(look.skin))
                    if look.sleeve>=8:d.line((0,cuff-1,w-1,cuff-1),fill=shade(look.accent,8))
                if look.mark=='mourner_pocket':d.rectangle((0,5*S,w-1,7*S-1),fill='#1c2027')
                if h>=20:
                    d.line((1,h-2,w-2,h-2),fill=shade(look.skin,-18));d.point((w-2,h-4),fill=shade(look.skin,7))
                    if side in ('left','right'):d.line((1,h-5,1,h-3),fill=shade(look.skin,-22))
            elif side=='bottom':art=panel((w,h),'#a48f64' if look.mark=='borrasca_worker' else look.skin,'skin',side)
            if look.mark in ('truant','truant_leer') and side not in ('top','bottom'):
                # Ink on his hands: a few dark blue marks across the knuckles and the back of the hand.
                d=ImageDraw.Draw(art)
                for x,y in ((1,h-3),(w-3,h-4),(w//2,h-2),(w//2+1,h-4)):d.point((min(w-1,x),y),fill='#2c3858')
            if look.mark=='borrasca_worker' and side not in ('top','bottom'):
                d=ImageDraw.Draw(art);d.rectangle((0,h-4,w-1,h-1),fill='#a48f64');d.line((0,h-4,w-1,h-4),fill='#786b51')
            if look.mark=='borrasca_kyle' and side not in ('top','bottom'):
                ImageDraw.Draw(art).rectangle((0,2,w-1,3),fill=rgb(look.accent))
            put(box,art)
    # Elbow/knee cut surfaces are real model faces, with their own flat pigments.
    d=ImageDraw.Draw(im)
    d.rectangle((0,0,8*S-1,8*S-1),fill=rgb(look.skin if look.sleeve<=6 else look.cloth))
    d.rectangle((24*S,0,32*S-1,8*S-1),fill=rgb(look.pants))
    for leg in (net(0,16,4,12,4),net(16,48,4,12,4)):
        for side,box in leg.items():
            w,h=round((box[2]-box[0])*S),round((box[3]-box[1])*S)
            art=panel((w,h),look.pants,'pants',side);d=ImageDraw.Draw(art)
            if side not in ('top','bottom'):
                d.line((1,0,w-2,0),fill=shade(look.pants,-20))
                if side in ('front','back'):d.line((w//2,3,w//2,h-7),fill=shade(look.pants,7))
                if side=='back':d.rectangle((1,2,w-2,5),outline=shade(look.pants,-15))
                if look.style=='swim':
                    d.rectangle((0,2,w-1,h-1),fill=rgb(look.skin));d.line((0,1,w-1,1),fill=rgb(look.accent))
                elif look.mark=='trunks':
                    # Swim trunks to the middle of the thigh, a pale hem, and bare legs below.
                    hem=round(h*.42);d.rectangle((0,hem+1,w-1,h-1),fill=rgb(look.skin));d.line((0,hem,w-1,hem),fill=rgb(look.accent))
                elif look.mark=='borrasca_kyle':
                    d.line((0,11,w-1,11),fill=shade(look.pants,-22));d.rectangle((0,12,w-1,h-6),fill=rgb(look.skin))
                    d.rectangle((0,h-6,w-1,h-4),fill='#d8d6c3');d.rectangle((0,h-3,w-1,h-1),fill='#666d6b');d.line((0,h-1,w-1,h-1),fill='#333a3b')
                elif look.mark.startswith('diner_'):
                    d.rectangle((0,0,w-1,12),fill=rgb(look.cloth))
                    if side=='front':
                        d.rectangle((0,0,w-1,7),fill=rgb(look.accent));d.line((0,7,w-1,7),fill=shade(look.accent,-19))
                    d.line((0,12,w-1,12),fill=shade(look.cloth,-20))
                    d.rectangle((0,13,w-1,h-6),fill=shade(look.skin,-7))
                    d.rectangle((0,h-5,w-1,h-1),fill='#292f33');d.line((1,h-4,w-2,h-4),fill='#505858')
                    d.line((0,h-1,w-1,h-1),fill='#1b2023')
                elif look.mark=='possessed':
                    # Barefoot under the hem: a pale shin and a dirty sole line, no shoes.
                    d.rectangle((0,h-9,w-1,h-1),fill=rgb(look.skin));d.line((0,h-9,w-1,h-9),fill=shade(look.pants,-24));d.line((0,h-1,w-1,h-1),fill=shade(look.skin,-40))
                else:
                    if look.mark=='cottage_walter':d.rectangle((0,h-7,w-1,h-6),fill='#673c3a')
                    if look.mark=='cottage_bowler':d.line((2,1,2,h-7),fill=shade(look.pants,13))
                    d.rectangle((0,h-5,w-1,h-1),fill='#34302c');d.line((1,h-4,w-2,h-4),fill='#635442');d.line((0,h-1,w-1,h-1),fill='#222522')
            if side=='bottom':art=panel((w,h),look.skin if look.style=='swim' or look.mark in ('possessed','trunks') else '#292c28','skin')
            put(box,art)
    if look.mark=='winter':
        def over(box,art):im.paste(art,(round(box[0]*S),round(box[1]*S)),art)
        # A knit cap on the hat layer: ribbed, a turned-up band, and a bobble on the crown.
        cap=look.accent
        for side,box in net(32,0,8,8,8).items():
            art=Image.new('RGBA',(16,16));d=ImageDraw.Draw(art)
            if side=='top':
                art=panel((16,16),cap,'cloth');d=ImageDraw.Draw(art)
                for x in range(1,15,3):d.line((x,0,x,15),fill=shade(cap,-14))
                d.rectangle((6,6,9,9),fill=shade(cap,42));d.point((7,7),fill=shade(cap,60))
            elif side!='bottom':
                d.rectangle((0,0,15,7),fill=rgb(cap))
                for x in range(1,15,3):d.line((x,0,x,4),fill=shade(cap,-14))
                d.rectangle((0,5,15,7),fill=shade(cap,-22));d.line((0,6,15,6),fill=shade(cap,-8))
            put(box,art)
        # A scarf wound at the neck, one end hanging down the front.
        for side,box in net(16,16,8,12,4).items():
            if side in ('top','bottom'):continue
            w,h=round((box[2]-box[0])*S),round((box[3]-box[1])*S);art=Image.new('RGBA',(w,h));d=ImageDraw.Draw(art)
            d.rectangle((0,0,w-1,4),fill=rgb(look.scarf));d.line((0,2,w-1,2),fill=shade(look.scarf,-22))
            if side=='front':
                d.rectangle((3,5,6,13),fill=rgb(look.scarf));d.line((3,10,6,10),fill=shade(look.scarf,-22))
                for x in (3,5):d.point((x,14),fill=shade(look.scarf,-10))
            over(box,art)
        # Mittens, knitted to match the cap.
        for arm in (net(40,16,4,12,4),net(32,48,4,12,4)):
            for side,box in arm.items():
                w,h=round((box[2]-box[0])*S),round((box[3]-box[1])*S);art=Image.new('RGBA',(w,h));d=ImageDraw.Draw(art)
                if side=='bottom':d.rectangle((0,0,w-1,h-1),fill=rgb(cap))
                elif side!='top':d.rectangle((0,h-6,w-1,h-1),fill=rgb(cap));d.line((0,h-6,w-1,h-6),fill=shade(cap,-24))
                over(box,art)
    if look.logical==128:
        # Insets serve named geometry (coat, gown, collar, cuffs, cap), never a portrait.
        fill_net(net(64,28,9,17,5),look.cloth)
        for side,box in net(64,28,9,17,5).items():put(box,cloth_face((round((box[2]-box[0])*S),round((box[3]-box[1])*S)),look,side))
        fill_net(net(96,28,10,14,6),look.cloth)
        for side,box in net(96,28,10,14,6).items():
            w,h=round((box[2]-box[0])*S),round((box[3]-box[1])*S)
            art=panel((w,h),look.cloth,'cloth',side);d=ImageDraw.Draw(art)
            if side not in ('top','bottom'):
                for x in range(3,w-2,6):d.line((x,3,x,h-3),fill=shade(look.cloth,-11))
                d.line((1,h-2,w-2,h-2),fill=rgb(look.accent))
                if look.mark=='cottage_apron':
                    if side=='front':
                        d.rectangle((3,0,w-4,h-3),fill=rgb(look.accent))
                        for x in (5,w-7):d.line((x,3,x,h-4),fill=shade(look.accent,-18))
                        d.line((3,h-3,w-4,h-3),fill=shade(look.accent,-24))
                    elif side=='back':
                        d.line((1,1,w-2,1),fill=rgb(look.accent));d.line((w//2,2,w//2-2,8),fill=rgb(look.accent))
            put(box,art)
        # A solid clean swatch for inner mouths/eyelids, outside every head island.
        ImageDraw.Draw(im).rectangle((64*S,0,79*S-1,4*S-1),fill=rgb(look.skin))
        # Masks have their own six-face UV net. Faces have eye holes, not a second face picture.
        if look.mark in ('mask','red_mask'):
            c='#d0c8af' if look.mark=='mask' else '#aa6d5c'
            fill_net(net(64,0,8.3,8.5,1),c,'skin')
            box=net(64,0,8.3,8.5,1)['front'];art=panel((17,17),c,'skin');d=ImageDraw.Draw(art)
            d.rectangle((3,7,5,8),fill='#3b3631');d.rectangle((11,7,13,8),fill='#3b3631');d.line((6,13,10,13),fill='#887768');put(box,art)
        if look.style=='killer':
            fill_net(net(64,0,8,8,8),look.cloth);fill_net(net(96,0,8,8,1),'#bcb294','skin')
            art=panel((16,16),'#bcb294','skin');d=ImageDraw.Draw(art)
            d.rectangle((3,6,5,8),fill='#302f29');d.rectangle((10,6,12,8),fill='#302f29')
            for x,y in ((6,3),(9,3),(7,10),(10,12),(4,12)):d.point((x,y),fill='#625f4d')
            d.line((6,13,9,13),fill='#77715b');put(net(96,0,8,8,1)['front'],art)
            for islands in (net(64,16,8,12,4),net(88,16,4,12,4),net(0,32,4,6,4)):fill_net(islands,look.cloth)
            fill_net(net(0,48,8,2,4),'#443d30');fill_net(net(16,32,4,5,4),'#2f3029');fill_net(net(32,32,4,2,1),'#34342d')
            fill_net(net(48,32,1,1,10),'#886c40');fill_net(net(72,32,2,5,3),'#9b9d90')
        if look.mark=='hardhat':
            fill_net(net(64,0,8,3,8),'#a68d49');fill_net(net(64,16,10,1,10),'#c1a35c')
        if look.mark=='caver':
            # A yellow caving helmet scuffed with clay, its brim, and the lamp's brass housing on the front of the shell.
            fill_net(net(64,0,8,3,8),'#c9a12e');fill_net(net(64,16,10,1,10),'#a8862a')
            d=ImageDraw.Draw(im);f=net(64,0,8,3,8)['front']
            for x,y in ((f[0]+1,f[1]+2),(f[0]+6,f[1]+1)):d.rectangle((x*S,y*S,x*S+S-1,y*S+S-1),fill='#8c6a3a')
            d.rectangle(((f[0]+3)*S,f[1]*S,(f[0]+5)*S-1,(f[1]+2)*S-1),fill='#7a6a46')
            d.rectangle((round((f[0]+3.5)*S),round((f[1]+.5)*S),round((f[0]+4.5)*S)-1,round((f[1]+1.5)*S)-1),fill='#e6e2c8')
            # Clay on the knees and the seat of the suit, and the harness's buckle at the waist.
            for islands,c in ((net(0,16,4,12,4),'#7a5a3e'),(net(16,48,4,12,4),'#7a5a3e')):
                b=islands['front'];d.rectangle((b[0]*S,(b[1]+5)*S,b[2]*S-1,(b[1]+7)*S-1),fill=c)
            b=net(16,16,8,12,4)['front'];d.rectangle(((b[0]+3)*S,(b[1]+8)*S,(b[0]+5)*S-1,(b[1]+9)*S-1),fill='#b8b4a6')
        if look==LOOKS.get('literary_lake_tess'):
            for box in ((64,0,82,24),(84,0,100,24)):put(box,panel((round((box[2]-box[0])*S),round((box[3]-box[1])*S)),look.hair,'hair'))
        if look.style in ('uniform','waistcoat') or look in HOTEL or look==LOOKS.get('stanley_bath_ghost'):
            fill_net(net(64,0,10,2,10),look.cloth)
            for box,c in [((64,30,96,64),look.cloth),((96,0,120,24),'#d7ccb2'),((96,24,103,32),look.skin),((104,24,116,42),'#c8ad70'),((116,24,128,42),'#4e6243'),((100,44,116,52),'#c7bea5')]:
                put(box,panel((round((box[2]-box[0])*S),round((box[3]-box[1])*S)),c))
        if look.style=='club_dress':fill_net(net(64,41,9,6,5),look.cloth)
        if look in [v for _,v in CLUB] or look.mark in ('despair','unsure'):
            fill_net(net(96,0,1,3,3),'#333b3e')
            ImageDraw.Draw(im).rectangle((96*S,0,111*S,7*S),fill='#454b4b')
        if look in [LOOKS.get('seance_'+x) for x in ('medium','mother','father','daughter')]:
            fill_net(net(64,0,9,9,1),'#625867')
            # The front veil frames the face; an opaque rectangle would hide the medium.
            ImageDraw.Draw(im).rectangle((round(66.5*S),round(2.5*S),round(72.5*S)-1,round(8.5*S)-1),fill=(0,0,0,0))
            fill_net(net(64,12,9,10,1),'#584851')
            fill_net(net(64,28,9,7,5.2),look.cloth);fill_net(net(64,42,11,7,6.4),look.cloth)
            fill_net(net(96,0,7.6,2,1.2),'#d6c8ab');fill_net(net(96,8,4.6,2,4.6),'#c9b99a')
        if look.mark=='hand':
            d=ImageDraw.Draw(im);d.line((45*S,29*S,46*S,30*S),fill='#97594a',width=S)
        if look.mark in ('bandaged','wound'):
            # Only the left-leg bandage mesh samples this island; the mirrored legs stay intact.
            fill_net(net(112,0,4,5,4),'#c9bfa6')
            d=ImageDraw.Draw(im)
            for y in (5,7,8):d.line((112*S,y*S,128*S-1,y*S),fill='#978e76',width=S)
            if look.mark=='wound':d.rectangle((116*S,5*S,119*S,8*S),fill='#763e37')
    if look.mark=='cellar_exhausted':
        d=ImageDraw.Draw(im)
        # Two clustered sweat trails and a wet collar on the same native face/cloth grid.
        for x,y in ((9*S,10*S),(14*S,9*S)):
            d.rectangle((x,y,x+S-1,y+2*S-1),fill='#a7b9aa');d.point((x,y),fill='#d5dfc5')
        d.line((21*S,21*S,23*S,24*S,25*S,22*S,26*S,21*S),fill='#434e44',width=S)
        d.line((20*S,24*S,21*S,27*S),fill='#454f43',width=S)
    if look.mark=='bent_injury':
        d=ImageDraw.Draw(im)
        # Unused right-pants overlay: the independent broken-neck mesh has its own native UV island.
        d.rectangle((0,32*S,7*S-1,39*S-1),fill=look.skin)
        d.rectangle((S,34*S,3*S-1,37*S-1),fill='#79616b');d.line((4*S,33*S,4*S,37*S),fill='#95877b',width=S)
        d.rectangle((21*S,20*S,25*S-1,23*S-1),fill=look.skin);d.rectangle((23*S,21*S,25*S-1,23*S-1),fill='#79616b')
        d.line((21*S,24*S,22*S,26*S,21*S,28*S),fill='#242928',width=S);d.line((25*S,28*S,26*S,30*S),fill='#746f64',width=S)
        d.rectangle((44*S,22*S,46*S-1,25*S-1),fill='#78616a');d.line((45*S,25*S,46*S,27*S),fill='#b19e90',width=S)
        for x in (1,25):d.rectangle((x*S,11*S,(x+2)*S-1,14*S-1),fill='#79656c');d.point(((x+1)*S,12*S),fill='#59515b')
    # Native 256-square / 512x256 archive dimensions; art itself remains the exact two-pixel grid.
    return im.resize((look.logical*4,256),Image.Resampling.NEAREST)


def material_sheet(width,height):
    """Native cuboid materials: clumped coat hair, horn growth and simple hide."""
    im=Image.new('RGBA',(width,height));d=ImageDraw.Draw(im)
    palette=['#735e49','#c2ae7b','#3e3931','#252a28','#a9876c','#46494b','#aaa18b','#3e392f']
    tw,th=width//2,128
    for i,c in enumerate(palette[:height//128*2]):
        x0=(i%2)*tw;y0=(i//2)*th;d.rectangle((x0,y0,x0+tw-1,y0+th-1),fill=c)
        if i in (0,2,3):
            for y in range(2,th-2,8):
                for x in range(2+(y//8%3)*2,tw-5,12):
                    d.line((x0+x,y0+y,x0+x+2,y0+y+4,x0+x+5,y0+y+3),fill=shade(c,9),width=2)
                    d.line((x0+x+4,y0+y+1,x0+x+5,y0+y+6),fill=shade(c,-9),width=2)
        elif i==1:
            for y in range(3,th,10):d.line((x0,y0+y,x0+tw-1,y0+y),fill=shade(c,-12),width=2)
        elif i==4:
            for x,y in ((4,7),(17,19),(32,5),(48,29),(70,13),(96,35)):d.rectangle((x0+x,y0+y,x0+x+4,y0+y+2),fill=shade(c,-11))
        elif i in (5,7):
            for y in range(5,th,20):d.line((x0+2,y0+y,x0+tw-3,y0+y),fill=shade(c,7),width=2)
    return im


def elk_skin(wounded=False,skinned=False):
    im=Image.new('RGBA',(256,128),'#8d7150');d=ImageDraw.Draw(im)
    def paint_net(islands,color):
        for side,box in islands.items():
            rect=tuple(round(x*S) for x in box);w=rect[2]-rect[0];h=rect[3]-rect[1]
            art=panel((w,h),color,'hair',side);im.paste(art,rect[:2])
    body=net(0,0,10,12,23);head=net(68,0,8,7,10)
    for islands,color in [(body,'#8d7150'),(net(0,36,7,15,8),'#67503a'),(head,'#886d49'),(net(68,20,6,4,5),'#b49c72'),(net(100,20,6,3,2),'#a58c61'),(net(32,38,3,13,3),'#6b563e'),(net(48,38,4,2,4),'#383930'),(net(68,38,2,2,5),'#ad9670')]:paint_net(islands,color)
    # Eyes, muzzle and rump occupy their own actual head/body faces.
    for side in ('left','right'):
        x0,y0,x1,y1=head[side];x=round((x0+(2 if side=='left' else x1-x0-3))*S);y=round((y0+2.5)*S)
        d.rectangle((x,y,x+2,y+2),fill='#292d27');d.point((x+1,y),fill='#c2b68a')
    for box in (net(68,20,6,4,5)['front'],):
        x0,y0,x1,y1=[round(v*S) for v in box]
        d.rectangle((x0+2,y0+4,x1-3,y1-2),fill='#4f4939');d.line((x0+3,y1-1,x1-4,y1-1),fill='#927958')
    if wounded:
        for side in ('left','right'):
            x0,y0,x1,y1=[round(v*S) for v in body[side]]
            x=x0+max(2,(x1-x0)//3);y=y0+6
            d.polygon([(x,y+2),(x+4,y),(x+12,y+2),(x+15,y+7),(x+12,y+14),(x+6,y+12),(x+1,y+8)],fill='#a86c51')
            d.polygon([(x+2,y+3),(x+6,y+1),(x+12,y+4),(x+12,y+10),(x+7,y+10),(x+3,y+7)],fill='#74382e')
            d.line((x+5,y+4,x+10,y+7,x+6,y+8),fill='#bb7d61',width=2)
        for x,y in ((11,53),(20,53),(15,56),(49,55),(62,58),(57,53)):
            d.rectangle((x,y,x+6,y+2),fill='#743d31');d.point((x+2,y-1),fill='#9a5b42')
    if skinned:
        for side in ('left','right','front','back'):
            box=body[side];x0,y0,x1,y1=[round(v*S) for v in box]
            d.rectangle((x0+2,y0+2,x1-3,y1-3),fill='#a87b66')
            for y in range(y0+4,y1-3,5):d.line((x0+3,y,x1-4,y),fill='#875e4c')
    return im.resize((512,256),Image.Resampling.NEAREST)


def rabbit_skin():
    im=Image.new('RGBA',(64,32),'#d9d4c1');d=ImageDraw.Draw(im)
    # Small, grouped cream/grey tufts, in the vanilla rabbit net's pixel density.
    for x,y in ((3,5),(8,12),(17,3),(26,17),(35,5),(47,11),(55,4),(13,25),(39,26),(58,21)):
        d.line((x,y,x+2,y),fill='#e9e1cb');d.line((x+1,y+1,x+3,y+1),fill='#c5c4b5')
    return im.resize((512,256),Image.Resampling.NEAREST)


# 0.4.163, the House's body (owner, October 29): a reader whose own skin doesn't look human wears one of these inside the
# House, chosen from the read: three ages, three presentations and five hairstyles. Each is painted with grey skin and hair;
# its lower half marks the pixels painted from the skin (red) and the hair (green). The painter adds its shading to those
# colours, so the client recolours them exactly to the reader's tone and hair colour. All use wide arms.
BODY_REF='#808080'
BODY_AGES=('young','middle','older')
BODY_PRESENTATIONS=('plain','masc','fem')
BODY_CUTS=('short','coils','braids','long','close')
BODY_CLOTHES={
    'plain':(('tee','#56656b','#3a4450','#c9bc9f',4),('crew','#6b6a5c','#3f4446','#b7a27b',9),('knit','#6e6457','#4a4740','#bfb49a',10)),
    'masc':(('flannel','#7a4a3c','#38424c','#c9bc9f',9),('shirt','#4a5865','#303941','#c9bc9f',9),('cardigan','#5d5446','#3c3a34','#a89a78',10)),
    'fem':(('dress','#6d4f6a','#6d4f6a','#d8cdb8',4),('dress','#3f5e66','#3f5e66','#e0d4b8',9),('cardigan','#7a6a74','#4d4650','#c7b9a0',10)),
}


def body_look(age,presentation,cut,skin=BODY_REF,hair=BODY_REF):
    style,cloth,pants,accent,sleeve=BODY_CLOTHES[presentation][BODY_AGES.index(age)]
    return Look(skin,hair,cut,cloth,pants,style,accent,eyes='#4b3d33',age=BODY_AGES.index(age),sleeve=sleeve,logical=64,level_eyes=True)


def house_body(age,presentation,cut):
    base=paint(body_look(age,presentation,cut))
    by_skin=paint(body_look(age,presentation,cut,skin='#888888')).load();by_hair=paint(body_look(age,presentation,cut,hair='#888888')).load()
    out=Image.new('RGBA',(base.width,base.height*2));out.paste(base,(0,0));b=base.load();m=out.load()
    for y in range(base.height):
        for x in range(base.width):
            if b[x,y][3]==0:continue
            skin=by_skin[x,y][:3]!=b[x,y][:3];hair=by_hair[x,y][:3]!=b[x,y][:3]
            assert not(skin and hair),(age,presentation,cut,x,y)
            if skin or hair:m[x,y+base.height]=(255 if skin else 0,255 if hair else 0,0,255)
    return out


def brad_lamp():
    """Brad's lamp alone, for the glow drawn over his helmet when it is lit: black everywhere but the glass."""
    im=Image.new('RGBA',(128*S,64*S),(0,0,0,0));f=net(64,0,8,3,8)['front']
    ImageDraw.Draw(im).rectangle(((f[0]+3)*S,f[1]*S,(f[0]+5)*S-1,(f[1]+2)*S-1),fill=(255,255,255,255))
    return im


def main():
    parser=argparse.ArgumentParser();parser.add_argument('--check',action='store_true');args=parser.parse_args()
    paintings={name:paint(look) for name,look in LOOKS.items()}
    from draw_backseat_stowaway import paint as stowaway
    paintings['backseat_stowaway']=stowaway()
    # 0.4.171: the tunnel's stooping giant has her own painter (wet hair, butcher paper, no wound); her neck keeps its opaque net.
    from draw_tunnel_woman import paint as tunnel_woman
    paintings['uncanny_bent_neck']=tunnel_woman()
    paintings['minotaur_materials']=material_sheet(256,256)
    paintings['finale_materials']=material_sheet(256,512)
    for name,wound,skinned in [('literary_elk',False,False),('literary_elk_wounded',True,False),('literary_elk_carcass',True,False),('literary_elk_carcass_skinned',True,True)]:paintings[name]=elk_skin(wound,skinned)
    paintings['literary_snow_rabbit']=rabbit_skin()
    paintings['literary_pitt_brad_lamp']=brad_lamp()
    # Historical alternate DJ sheet remains valid for resource packs, but production uses the proper head net.
    paintings['nightclub_dj_head0490']=paint(LOOKS['nightclub_dj'])
    for age in BODY_AGES:
        for presentation in BODY_PRESENTATIONS:
            for cut in BODY_CUTS:paintings[f'house_body/{age}_{presentation}_{cut}']=house_body(age,presentation,cut)
    (OUT/'house_body').mkdir(exist_ok=True)
    for name,im in paintings.items():
        path=OUT/(name+'.png')
        if args.check:
            with Image.open(path) as saved:
                assert saved.size==im.size and saved.convert('RGBA').tobytes()==im.tobytes(),name
        else:im.save(path,optimize=True)
    print(f'{"Verified" if args.check else "Painted"} {len(paintings)} face-fitted character sheets, including Drisking and the recent human cast.')


if __name__=='__main__':main()
