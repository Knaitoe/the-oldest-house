"""The native mesh/phase used to review each human painting, including the Drisking cast."""
from paint_character_skins import LOOKS

HOTEL = ['porter','bartender','scarred_guest','pianist','dancer','dancer_dress','bath_ghost']
GHOSTS = {'marjorie':'GHOST_MARJORIE','merry':'GHOST_MERRY','john':'GHOST_JOHN','sarah':'GHOST_SARAH','barry':'GHOST_PRODUCER','father_wanderly':'GHOST_PRIEST'}
LITERARY = {'mother':'MOTHER','dancer':'DANCER','red_figure':'RED_FIGURE','coffin_woman':'COFFIN_WOMAN','lady':'LADY','brother':'BROTHER','father':'FATHER','visitor':'VISITOR','killer':'KILLER','family_father':'FAMILY_FATHER','family_mother':'FAMILY_MOTHER','family_child':'FAMILY_CHILD','old_man':'OLD_MAN','lake_tess':'LAKE_TESS','lake_theo':'LAKE_THEO','lake_wax':'LAKE_WAX','miniature_mother':'LADY'}
MOVIE=['ray','dot','frank','june','jamie','bobby','lynn']
COSTUME=['hank','tina','danny','ron','kaylee','marcus','shelly','owen','brooke','wes','rosa','pip','lyle']
WINTER=['bud','hank','clay','lonnie']
USHER=['roderick']
CLOSING=['chad','daisy']
POLYBIUS=['a','b']
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
    rows=[entry('backseat_stowaway',64)]+[entry(name,look.logical) for name,look in sorted(LOOKS.items()) if not name.startswith(('literary_paw_','literary_movie_','literary_costume_','literary_winter_','literary_usher_','literary_closing_','literary_polybius_','literary_replacement','literary_swimmer_','literary_vacant_','literary_pitt_'))]
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
    # The closing hall (0.4.209): Chad and Daisy on stranger phases thirty-nine and forty.
    for i,name in enumerate(CLOSING):
        rows.append({'name':'literary_closing_'+name,'logical':128,'model':'literary','role':'STRANGER','number':0,'phase':39+i})
    # Polybius (0.4.217): the two men by the black cabinet on stranger phases forty-one and forty-two.
    for i,name in enumerate(POLYBIUS):
        rows.append({'name':'literary_polybius_'+name,'logical':128,'model':'literary','role':'STRANGER','number':0,'phase':41+i})
    # The replacement (0.4.218) on stranger phase forty-three.
    rows.append({'name':'literary_replacement','logical':128,'model':'literary','role':'STRANGER','number':0,'phase':43})
    # God of the Pitt (0.4.220): Brad on stranger phase forty-four (his lamp dark; forty-five and forty-six light it).
    rows.append({'name':'literary_pitt_brad','logical':128,'model':'literary','role':'STRANGER','number':0,'phase':44})
    # Polybius (0.4.221): the arcade's three regulars on stranger phases forty-seven to forty-nine.
    for i,name in enumerate(['kid_a','kid_b','kid_c']):
        rows.append({'name':'literary_polybius_'+name,'logical':128,'model':'literary','role':'STRANGER','number':0,'phase':47+i})
    # Polybius (0.4.224): the teenager behind the pizza counter on stranger phase fifty.
    rows.append({'name':'literary_polybius_teen','logical':128,'model':'literary','role':'STRANGER','number':0,'phase':50})
    # Vacant Possession (0.4.228): the tenant on stranger phase fifty-three.
    rows.append({'name':'literary_vacant_tenant','logical':128,'model':'literary','role':'STRANGER','number':0,'phase':53})
    # The last swimmer (0.4.227): the attendant on stranger phase fifty-one, the swimmer on fifty-two.
    rows.append({'name':'literary_swimmer_attendant','logical':128,'model':'literary','role':'STRANGER','number':0,'phase':51})
    rows.append({'name':'literary_swimmer_swimmer','logical':128,'model':'literary','role':'STRANGER','number':0,'phase':52})
    return rows
