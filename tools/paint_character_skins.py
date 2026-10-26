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


# Each row is a character design, rather than an index into a repeated picture.
LOOKS = {
    'borrasca_kyle': Look('#d7ac8b','#846444','part','#d8d4bc','#aa9673','tee','#52677e',eyes='#5d7183',mark='borrasca_kyle',sleeve=4,logical=64),
    'borrasca_kimber': Look('#ddb397','#975739','long','#477d79','#4d6271','jacket','#d8d3b8',eyes='#52745b',mark='borrasca_kimber',sleeve=10,logical=64),
    'borrasca_sheriff': Look('#caa17e','#8d897e','receding','#b09c71','#5f5543','shirt','#bdad70',eyes='#675a46',age=2,beard='moustache',mark='borrasca_sheriff',sleeve=10,logical=64),
    'borrasca_prescott': Look('#d5a38a','#aaa69c','receding','#88433b','#8a7d5c','flannel','#786049',eyes='#62717c',age=2,mark='borrasca_prescott',sleeve=10,logical=64),
    'borrasca_worker': Look('#bc9479','#4a3c31','close','#627174','#4a595d','crew','#be8748',eyes='#514e43',glasses=True,mark='borrasca_worker',sleeve=10,logical=64),
    'uncanny_waitress': Look('#cba083','#795039','bun','#496d80','#496d80','dress','#e0d4b8',eyes='#596456',age=1,mark='diner_apron',sleeve=4,logical=64),
    'uncanny_second_waitress': Look('#c4ad97','#42312c','bun','#526c79','#526c79','dress','#d5c8ab',eyes='#657068',mark='diner_second',sleeve=4,logical=64),
    'uncanny_second_waitress_split': Look('#c4ad97','#42312c','bun','#526c79','#526c79','dress','#d5c8ab',eyes='#657068',mark='diner_split',sleeve=4,logical=64),
    'uncanny_peeler': Look('#c7a087','#8d8879','receding','#737766','#4f5145','shirt','#a99e7b',eyes='#665d4d',age=2,beard='stubble',sleeve=10,logical=64),
    'uncanny_peeler_exhausted': Look('#c79c82','#8d8879','receding','#5b6254','#484b40','shirt','#a99e7b',eyes='#665d4d',age=2,beard='stubble',mark='cellar_exhausted',sleeve=10,logical=64),
    'uncanny_visitor': Look('#cbb194','#473f37','part','#45494a','#343a3d','coat','#afa78e',eyes='#62685d',age=1,sleeve=11,logical=64),
    'literary_paw_walter': Look('#d6a88a','#cecbc4','receding','#47513c','#5a4c3e','coat','#9e8350',eyes='#5c6c78',age=2,beard='short',glasses=True,mark='cottage_walter'),
    'literary_paw_ruth': Look('#deb496','#76665a','bun','#523240','#5f4b4e','dress','#d8cebb',eyes='#544a3e',age=2,mark='cottage_apron'),
    'literary_paw_will': Look('#daac8a','#3e2c20','messy','#d6ceba','#585046','braces','#962e28',eyes='#465c42',mark='cottage_braces',sleeve=6),
    'literary_paw_sergeant': Look('#c48868','#968c80','close','#282c3a','#5c5a58','coat','#b09250',eyes='#382c24',age=2,beard='moustache',mark='cottage_uniform'),
    'literary_paw_messenger': Look('#dcbaa2','#2c241e','part','#201e20','#5b5a59','coat','#242226',eyes='#48423c',beard='moustache',mark='cottage_bowler'),
    # Movie Night (0.4.102): the projectionist, the ticket seller, the Pruitts on the beach and the two in the canoe.
    'literary_movie_ray': Look('#c9a184','#8f887c','receding','#58664d','#3d3a33','cardigan','#c7b98e',eyes='#5d5e52',age=2,glasses=True,beard='moustache',mark='movie_cap'),
    'literary_movie_dot': Look('#d9b096','#a4532f','bun','#a8404a','#423a3c','cardigan','#ecdcb4',eyes='#4e5a48',age=1,mark='movie_badge'),
    'literary_movie_frank': Look('#cf9f7d','#5b4130','part','#3a7c8a','#a08e6a','shirt','#f0d27a',eyes='#4f5f6e',age=1,mark='movie_hawaiian',sleeve=4),
    'literary_movie_june': Look('#e0b89a','#6c4a2e','long','#d6a248','#d6a248','dress','#f2ead2',eyes='#566a50',sleeve=0),
    'literary_movie_jamie': Look('#dcb090','#8c5a2e','messy','#c4463a','#3e5a78','tee','#efe4cc',eyes='#4f6a7a',mark='movie_stripes',sleeve=4),
    'literary_movie_bobby': Look('#c99a78','#2c2219','part','#2d4878','#3a4d68','jacket','#e6d8b0',eyes='#4a3c30',mark='movie_letter'),
    'literary_movie_lynn': Look('#e2bea6','#d6b06a','long','#e8c6cf','#394c66','knit','#f6eef0',eyes='#5b7090',mark='freckles'),
    # Costume Night (0.4.103): Hank, who judges, and the twelve entrants, every one of them dressed as the Lake Witch.
    'literary_costume_hank': Look('#cfa688','#bdb8ae','receding','#6a5a46','#3f3a34','cardigan','#d7c79e',eyes='#5a6066',age=2,glasses=True,beard='moustache',mark='movie_badge'),
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
    'harrigan': Look('#c7ab91','#b9b7ac','receding','#343941','#2c3037','suit','#7d3940',age=2,glasses=True,logical=64),
    'harrigan_dead': Look('#b9b2a4','#b9b7ac','receding','#343941','#2c3037','suit','#7d3940',age=2,mark='dead',logical=64),
    'harrigan_mourner_0': Look('#ae7b59','#262320','close','#292e33','#26292f','suit','#343338',mark='mourner_flower',logical=64),
    'harrigan_mourner_1': Look('#d0ab8b','#7c7164','part','#454342','#343338','suit','#65544b',age=1,mark='mourner_pocket',logical=64),
    'harrigan_mourner_2': Look('#cfaa8b','#a7a295','receding','#34485d','#303e4e','suit','#603c46',age=2,glasses=True,mark='mourner_watch',logical=64),
    'holloway': Look('#b98b69','#5a4230','part','#5b6148','#51483a','field','#b49355',age=1,beard='stubble',logical=64),
    'finale_witness': Look('#c0a184','#ada794','receding','#615746','#484438','coat','#98846a',age=2,beard='full',logical=64),
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
    'ghosts_marjorie': Look('#dfb79f','#573a2d','long','#887091','#3b4b5a','knit','#ae99ae',eyes='#6c765a',sleeve=8),
    'ghosts_merry': Look('#ddba9b','#805a38','short','#ac5847','#47657a','tee','#d5b890',eyes='#72837d',sleeve=4),
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
    'literary_father': Look('#b98f70','#4b3628','short','#695a42','#4b463a','field','#9b8460',age=1,beard='stubble'),
    'literary_visitor': Look('#c8a381','#7b5c40','part','#75644d','#4e493c','coat','#b6a588',age=1),
    'literary_killer': Look('#b9ad8f','#282722','close','#484a38','#34362d','killer','#797354',mark='killer'),
    'literary_family_father': Look('#b8ad92','#514a3a','part','#505846','#3d4235','suit','#a49a7d',eyes='#797d67',age=1,mark='family_father'),
    'literary_family_mother': Look('#c3b4a1','#594035','bun','#8b625d','#5d4843','dress','#b5a48c',eyes='#858679',mark='family_mother'),
    'literary_family_child': Look('#c7bfa5','#63513b','messy','#596c78','#3e4c56','tee','#a2aea9',eyes='#7c8676',mark='family_child',sleeve=4),
    'literary_old_man': Look('#bda188','#b7af99','receding','#6c5b42','#4c473a','coat','#a28a64',age=2,beard='full'),
    'literary_stranger_0': Look('#c6a586','#6a533c','part','#62664a','#4b4e3a','coat','#b4aa86',age=1),
    'literary_stranger_1': Look('#c8aa8c','#674a39','part','#795944','#4c4035','shirt','#a98a6b',age=1,glasses=True),
    'literary_stranger_2': Look('#a77a5a','#30231d','bun','#733b4f','#49333d','dress','#b69b76'),
    'literary_stranger_3': Look('#cbb29b','#b4ab98','receding','#3b4753','#343b44','suit','#a78e69',age=2,beard='full'),
    'literary_bar_marla': Look('#a87554','#38251e','bun','#753e4c','#3c3932','apron','#a89e80',age=1),
    'literary_bar_russ': Look('#ba9875','#7f6b4b','part','#536e83','#444e54','flannel','#a3b0a9',age=1,beard='short'),
    'literary_bar_denny': Look('#bd9270','#343030','close','#4c6170','#425f73','vest','#8ca1ad',beard='stubble',sleeve=0),
    'literary_bar_wade': Look('#c7a18a','#75604c','receding','#854a45','#4f443e','flannel','#b58b76',age=1,beard='full'),
    'literary_bar_hollis': Look('#c3a58b','#b0a793','receding','#655544','#4c4436','coat','#baa98a',age=2,beard='full'),
    'literary_lake_tess': Look('#e0bda4','#28242a','long','#33445d','#33445d','swim','#d4d8d1',eyes='#415878',mark='freckles',sleeve=0),
    'literary_lake_wax': Look('#caba91','#746448','long','#575443','#514b38','dress','#d3c79f',mark='wax'),
    'seance_medium': Look('#c9a68a','#6a5040','bun','#533b50','#392f3c','dress','#d1bea0',age=1),
    'seance_mother': Look('#d2ae8e','#61442d','bun','#824954','#56393f','dress','#d0b999'),
    'seance_father': Look('#c39a7a','#584535','part','#514a3f','#3d3b34','suit','#baa48a',age=1,beard='stubble'),
    'seance_daughter': Look('#d9b69c','#6d4f38','long','#68869d','#4d6477','dress','#d7ceba'),
    # Miniatures (0.4.121): the mother in the upper corner, in a slept-in nightgown, the eyes turned up white.
    'literary_miniature_mother': Look('#d6c6b6','#231a16','long','#d3cbbb','#c9c1b0','dress','#a99f8f',eyes='#2a2624',mark='possessed'),
}

for group in ('crew', 'guest'):
    for i, (skin, hair, cloth) in enumerate([
        ('#c69b7b','#5e4632','#65724c'),('#ae825f','#453023','#57788e'),
        ('#c2a289','#b1a68c','#726653'),('#a67c5a','#2d241e','#52646c')]):
        LOOKS[f'literary_{group}_{i}'] = Look(skin,hair,'receding' if i==2 else 'part',cloth,'#454941','field' if group=='crew' else 'shirt','#a99d7c',age=1 if i==2 else 0,beard='stubble' if i in (0,2) else '',mark='hardhat' if group=='crew' else '')

# The passengers are distinct people, not the four road workers in different props.
LOOKS.update({
    'literary_guest_0':Look('#d3ad92','#824e35','long','#7c4350','#4c393f','dress','#ba9a7d'),
    'literary_guest_1':Look('#b88d6a','#4c3425','part','#56738b','#424c53','shirt','#b8ad90',beard='short'),
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
    Look('#b58c68','#35271f','part','#7d303c','#373a36','uniform','#c29c56',beard='moustache'),
    Look('#c99c78','#3e2c22','part','#323a39','#303632','waistcoat','#d8c9a9',beard='moustache'),
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
    ('male',Look('#c7a17c','#51392c','short','#434b50','#2f353a','tee','#8a9799',beard='short',sleeve=4)),
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

LOOKS['ghosts_marjorie_dinner']=replace(LOOKS['ghosts_marjorie'],mark='sauce')
LOOKS['ghosts_john_hand']=replace(LOOKS['ghosts_john'],mark='hand')
LOOKS['literary_father_bandaged']=replace(LOOKS['literary_father'],mark='bandaged')
LOOKS['literary_father_mangled']=replace(LOOKS['literary_father'],mark='wound')
for role,skin in [('father','#a6a28c'),('mother','#b5ada1'),('child','#b8baa2')]:
    LOOKS[f'literary_family_{role}_changed']=replace(LOOKS[f'literary_family_{role}'],skin=skin,mark=f'family_{role}_changed')

# The peripheral family's production mesh uses the standard 64x64 human UV,
# not the older literary family's wider costume atlas. Preserve those originals.
for index,role in enumerate(('father','mother','child')):
    LOOKS[f'uncanny_family_{index}']=replace(LOOKS[f'literary_family_{role}_changed'],logical=64)
LOOKS['uncanny_family_3']=replace(LOOKS['uncanny_family_2'],hair='#332e2b',cut='long',cloth='#777365',pants='#565247',style='dress',sleeve=9)
LOOKS['uncanny_bent_neck']=Look('#afa99a','#292b29','long','#424641','#424641','dress','#817c6c',eyes='#666c61',age=1,mark='family_mother_changed',sleeve=10,logical=64)


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


def face(look):
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
            iris=x+(1 if x==3 else 0)
            d.line((iris,7,iris,8),fill=rgb(look.eyes));d.point((iris,8),fill='#343331')
        if look.mark=='despair':
            d.line((3,5,5,6),fill=hair);d.line((10,6,12,5),fill=hair)
            d.line((4,9,4,11),fill=shade(skin,-25));d.line((11,9,11,12),fill=shade(skin,-25))
            d.line((6,13,7,12,8,12,9,13),fill=shade(skin,-44))
    if look.age:
        d.line((5,3,10,3),fill=cheek)
        for x in (2,13):d.point((x,9),fill=dark)
        if look.age==2:
            d.line((5,11,5,12),fill=cheek);d.line((10,11,10,12),fill=cheek)
            d.line((6,1,10,1),fill=cheek)
    if look.beard:
        if look.beard in ('full','short','stubble'):
            c=shade(hair,13) if look.beard=='stubble' else hair
            d.line((2,11,3,14,5,15,10,15,12,14,13,11),fill=c,width=2 if look.beard=='full' else 1)
        if look.beard in ('full','short','moustache'):
            d.line((5,11,6,11,6,12,9,12,9,11,10,11),fill=hair)
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
    elif look.cut in ('long','bun','braids'):
        d.rectangle((0,0,15,2),fill=hair);d.polygon([(0,2),(6,2),(4,4),(2,5),(2,13),(0,14)],fill=hair)
        d.polygon([(9,2),(15,2),(15,14),(13,12),(13,4)],fill=hair)
        d.line((1,2,1,7),fill=hhi);d.line((14,3,14,10),fill=hhi)
        if look.cut=='braids':
            for y in (5,8,11):d.rectangle((0,y,1,y+1),fill=hhi);d.rectangle((14,y,15,y+1),fill=hhi)
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
        # The same woman's features remain above a mouth pulled through both cheeks.
        d.line((2,10,4,11,6,11,8,12,11,11,13,10),fill='#936650')
        d.polygon([(2,11),(5,11),(7,12),(10,11),(13,11),(14,14),(11,15),(5,15),(1,13)],fill='#442d2b')
        d.line((2,11,5,12,8,12,11,11,13,11),fill='#c8ae8c')
        for x,y in ((3,12),(5,12),(8,13),(10,12),(12,12)):d.point((x,y),fill='#e0d0a8')
        d.line((3,14,6,14,9,15,12,14),fill='#9d6e5d')
        d.line((1,10,1,12),fill=shade(skin,-31));d.line((14,10,14,13),fill=shade(skin,-31))
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
    put(head['front'],face(look));put(head['bottom'],panel((16,16),look.skin,'skin'))
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
        put(head[side],art)
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
            cap={'borrasca_kyle':'#51677b','borrasca_sheriff':'#97805a','borrasca_prescott':'#4d6650','borrasca_worker':'#bba158'}[look.mark]
            for side,box in net(32,0,8,8,8).items():
                art=Image.new('RGBA',(16,16));d=ImageDraw.Draw(art)
                if side=='top':
                    art=panel((16,16),cap,'cloth',side);d=ImageDraw.Draw(art)
                    d.line((7,1,7,14),fill=shade(cap,-16))
                    if look.mark=='borrasca_worker':d.line((5,1,5,14),fill=shade(cap,18));d.line((10,1,10,14),fill=shade(cap,18))
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
            if side=='bottom':art=panel((w,h),look.skin if look.style=='swim' or look.mark=='possessed' else '#292c28','skin')
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
        if look==LOOKS.get('literary_lake_tess'):
            for box in ((64,0,82,24),(84,0,100,24)):put(box,panel((round((box[2]-box[0])*S),round((box[3]-box[1])*S)),look.hair,'hair'))
        if look.style in ('uniform','waistcoat') or look in HOTEL or look==LOOKS.get('stanley_bath_ghost'):
            fill_net(net(64,0,10,2,10),look.cloth)
            for box,c in [((64,30,96,64),look.cloth),((96,0,120,24),'#d7ccb2'),((96,24,103,32),look.skin),((104,24,116,42),'#c8ad70'),((116,24,128,42),'#4e6243'),((100,44,116,52),'#c7bea5')]:
                put(box,panel((round((box[2]-box[0])*S),round((box[3]-box[1])*S)),c))
        if look.style=='club_dress':fill_net(net(64,41,9,6,5),look.cloth)
        if look in [v for _,v in CLUB] or look.mark=='despair':
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


def main():
    parser=argparse.ArgumentParser();parser.add_argument('--check',action='store_true');args=parser.parse_args()
    paintings={name:paint(look) for name,look in LOOKS.items()}
    paintings['minotaur_materials']=material_sheet(256,256)
    paintings['finale_materials']=material_sheet(256,512)
    for name,wound,skinned in [('literary_elk',False,False),('literary_elk_wounded',True,False),('literary_elk_carcass',True,False),('literary_elk_carcass_skinned',True,True)]:paintings[name]=elk_skin(wound,skinned)
    paintings['literary_snow_rabbit']=rabbit_skin()
    # Historical alternate DJ sheet remains valid for resource packs, but production uses the proper head net.
    paintings['nightclub_dj_head0490']=paint(LOOKS['nightclub_dj'])
    for name,im in paintings.items():
        path=OUT/(name+'.png')
        if args.check:
            with Image.open(path) as saved:
                assert saved.size==im.size and saved.convert('RGBA').tobytes()==im.tobytes(),name
        else:im.save(path,optimize=True)
    print(f'{"Verified" if args.check else "Painted"} {len(paintings)} face-fitted character sheets, including Drisking and the recent human cast.')


if __name__=='__main__':main()
