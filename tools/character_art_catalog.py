"""The native mesh/phase used to review each human painting, including the Drisking cast."""
from paint_character_skins import LOOKS

HOTEL = ['porter','bartender','scarred_guest','pianist','dancer','dancer_dress','bath_ghost']
GHOSTS = {'marjorie':'GHOST_MARJORIE','merry':'GHOST_MERRY','john':'GHOST_JOHN','sarah':'GHOST_SARAH','barry':'GHOST_PRODUCER','father_wanderly':'GHOST_PRIEST'}
LITERARY = {'mother':'MOTHER','dancer':'DANCER','red_figure':'RED_FIGURE','coffin_woman':'COFFIN_WOMAN','lady':'LADY','brother':'BROTHER','father':'FATHER','visitor':'VISITOR','killer':'KILLER','family_father':'FAMILY_FATHER','family_mother':'FAMILY_MOTHER','family_child':'FAMILY_CHILD','old_man':'OLD_MAN','lake_tess':'LAKE_TESS','lake_theo':'LAKE_THEO','lake_wax':'LAKE_WAX','miniature_mother':'LADY'}
MOVIE=['ray','dot','frank','june','jamie','bobby','lynn']
COSTUME=['hank','tina','danny','ron','kaylee','marcus','shelly','owen','brooke','wes','rosa','pip','lyle']
WINTER=['bud','hank','clay','lonnie']
USHER=['roderick']
CLUB=['dancer_male','dancer_female','dancer_vest','dancer_copper','dancer_mustard','dancer_silver','dancer_brick','dancer_braids','dj']


def entry(name,logical):
    row={'name':name,'logical':logical,'model':'player','number':0,'phase':0}
    if name=='backseat_stowaway':
        row.update(model='backseat',phase=1)
    elif name.startswith('borrasca_'):
        row.update(model='borrasca',number=['kyle','kimber','sheriff','prescott','worker'].index(name[9:]))
    elif name.startswith('uncanny_'):
        role=32 if name.startswith('uncanny_family_') else 35 if name=='uncanny_bent_neck' else 37 if name.startswith('uncanny_peeler') else 38 if name=='uncanny_visitor' else 34 if name.startswith('uncanny_second_') else 33
        phase=int(name[-1])+1 if role==32 else 3 if name.endswith('_split') else 0 if role==33 or name=='uncanny_peeler' else 2 if name.endswith('_exhausted') else 1
        row.update(model='uncanny',number=role,phase=phase)
    elif name.startswith('harrigan'):
        row.update(model='harrigan',number=4+int(name[-1]) if 'mourner' in name else 2 if name.endswith('dead') else 1)
    elif name.startswith('ghosts_'):
        role=name[7:];phase=0
        if role.endswith('_dinner'):role=role[:-7];phase=8  # GhostsSet.VOMIT
        if role.endswith('_hand'):role=role[:-5];phase=9
        row.update(model='ghosts',role=GHOSTS[role],phase=phase)
    elif name.startswith(('hotel_','stanley_')):
        role=name.split('_',1)[1];row.update(model='hotel',number=HOTEL.index(role))
    elif name.startswith('nightclub_'):
        role=name[10:];despair=role.endswith('_despair')
        if despair:role=role[:-8]
        row.update(model='club',number=CLUB.index(role),phase=int(despair))
    elif name.startswith('seance_'):
        row.update(model='seance',number=['medium','mother','father','daughter'].index(name[7:]))
    elif name.startswith('lake_witch'):
        row.update(model='witch',phase=0 if name.endswith('memory') else 1)
    elif name.startswith('trailer_child_'):
        row.update(model='cousin',number=int(name[-1]))
    elif name.startswith('literary_'):
        role=name[9:];phase=0
        if role.startswith('bar_'):
            row.update(model='literary',role='STRANGER',phase=4+['marla','russ','denny','wade','hollis'].index(role[4:]));return row
        if role.startswith('stranger_'):
            row.update(model='literary',role='STRANGER',phase=int(role[-1]));return row
        if role.startswith(('crew_','guest_')):
            row.update(model='corpse',number=int(role[-1]));return row
        if role.endswith('_changed'):role=role[:-8];phase=1
        if role.endswith('_bandaged'):role='father';phase=2
        if role.endswith('_mangled'):role='father';phase=3
        if role=='father' and phase==0:phase=1
        row.update(model='literary',role=LITERARY[role],phase=phase)
    return row


def entries():
    rows=[entry('backseat_stowaway',64)]+[entry(name,look.logical) for name,look in sorted(LOOKS.items()) if not name.startswith(('literary_paw_','literary_movie_','literary_costume_','literary_winter_','literary_usher_'))]
    for i,name in enumerate(['walter','ruth','will','sergeant','messenger']):
        rows.append({'name':'literary_paw_'+name,'logical':128,'model':'literary','role':'STRANGER','number':0,'phase':9+i})
    # Movie Night (0.4.102): the lake's audience on stranger phases fourteen to twenty.
    for i,name in enumerate(MOVIE):
        rows.append({'name':'literary_movie_'+name,'logical':128,'model':'literary','role':'STRANGER','number':0,'phase':14+i})
    # Costume Night (0.4.103): Hank and the twelve entrants on stranger phases twenty-one to thirty-three.
    for i,name in enumerate(COSTUME):
        rows.append({'name':'literary_costume_'+name,'logical':128,'model':'literary','role':'STRANGER','number':0,'phase':21+i})
    # Winter Lake (0.4.104): the four boys from the shallows, that winter, on stranger phases thirty-four to thirty-seven.
    for i,name in enumerate(WINTER):
        rows.append({'name':'literary_winter_'+name,'logical':128,'model':'literary','role':'STRANGER','number':0,'phase':34+i})
    # Usher (0.4.182): Roderick on stranger phase thirty-eight.
    for i,name in enumerate(USHER):
        rows.append({'name':'literary_usher_'+name,'logical':128,'model':'literary','role':'STRANGER','number':0,'phase':38+i})
    return rows
