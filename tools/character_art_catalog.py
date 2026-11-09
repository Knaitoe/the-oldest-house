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
        row.update(model='borrasca',number=['kyle','kimber','sheriff','prescott','worker','worker_two','eli','wren','kelly_grown','dana','dana_aside','dana_scream'].index(name[9:]))
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
        role=name[10:];despair=role.endswith('_despair');unsure=role.endswith('_unsure')
        if despair:role=role[:-8]
        if unsure:role=role[:-7]
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
    rows=[entry('backseat_stowaway',64)]+[entry(name,look.logical) for name,look in sorted(LOOKS.items()) if not name.startswith(('literary_paw_','literary_movie_','literary_costume_','literary_winter_','literary_usher_','literary_closing_','literary_polybius_','literary_replacement','literary_swimmer_','literary_vacant_','literary_pitt_','literary_window_','literary_night_','literary_people_','literary_place_','literary_truant','literary_flat_','literary_supper_','literary_kitchen_','literary_marsh_','literary_grass_','literary_crossing_','literary_pinehook_','literary_drawing_','literary_corner_','literary_kept_','literary_orders_','literary_drisking_'))]
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
    # The window cleaner (0.4.251) on stranger phase fifty-four.
    rows.append({'name':'literary_window_cleaner','logical':128,'model':'literary','role':'STRANGER','number':0,'phase':54})
    rows.append({'name':'literary_night_operator','logical':128,'model':'literary','role':'STRANGER','number':0,'phase':55})
    rows.append({'name':'literary_night_operator_bare','logical':128,'model':'literary','role':'STRANGER','number':0,'phase':56})
    # After the People Lights (0.4.253): Kelly on stranger phase fifty-seven (her chair, floor, drag, bed and likeness phases share her skin), Mark on sixty-one, Stan on sixty-two.
    for name,phase in (('kelly',57),('mark',61),('stan',62)):
        rows.append({'name':'literary_people_'+name,'logical':128,'model':'literary','role':'STRANGER','number':0,'phase':phase})
    # A Place for You (0.4.257): Mara on stranger phase sixty-five, Kit on sixty-six (Alex and Ren wear the shelter strangers' own skins).
    for name,phase in (('mara',65),('kit',66)):
        rows.append({'name':'literary_place_'+name,'logical':128,'model':'literary','role':'STRANGER','number':0,'phase':phase})
    # Johnny Truant (0.4.264): his tired face on stranger phase seventy-three, the leer round the corner on seventy-four.
    rows.append({'name':'literary_truant','logical':128,'model':'literary','role':'STRANGER','number':0,'phase':73})
    rows.append({'name':'literary_truant_leer','logical':128,'model':'literary','role':'STRANGER','number':0,'phase':74})
    # Second Person (0.4.274): Iris Dunne on stranger phase seventy-five, Sam on seventy-six (the resident wears the reader's own House body).
    for name,phase in (('iris',75),('sam',76)):
        rows.append({'name':'literary_flat_'+name,'logical':128,'model':'literary','role':'STRANGER','number':0,'phase':phase})
    # Speak Up (0.4.327): Joan, Pat, Dell and Ruth on stranger phases ninety to ninety-three.
    for name,phase in (('joan',90),('pat',91),('dell',92),('ruth',93)):
        rows.append({'name':'literary_supper_'+name,'logical':128,'model':'literary','role':'STRANGER','number':0,'phase':phase})
    # The familiar kitchen's visitors (0.4.331): Holloway on stranger phase one hundred and one (the others wear Johnny's, the tenant's and a stranger's).
    rows.append({'name':'literary_kitchen_holloway','logical':128,'model':'literary','role':'STRANGER','number':0,'phase':101})
    # The Supper (0.4.348): Ida, Royce, Del, Nana and Corinne on stranger phases one hundred and ten to one hundred and fourteen.
    for name,phase in (('ida',110),('royce',111),('del',112),('nana',113),('corinne',114)):
        rows.append({'name':'literary_marsh_'+name,'logical':128,'model':'literary','role':'STRANGER','number':0,'phase':phase})
    # In the Tall Grass (0.4.349): the driver of the next car on stranger phase one hundred and fifteen.
    rows.append({'name':'literary_grass_driver','logical':128,'model':'literary','role':'STRANGER','number':0,'phase':115})
    # The Last Crossing (0.4.351): Vera, the keeper, on stranger phase one hundred and sixteen.
    rows.append({'name':'literary_crossing_vera','logical':128,'model':'literary','role':'STRANGER','number':0,'phase':116})
    # Last Weekend at Pinehook (0.4.364): Casey, Jude, the costume masked and with the mask off, Nina and Len on stranger phases one hundred and seventeen to one hundred and twenty-two.
    for name,phase in (('casey',117),('jude',118),('masked',119),('coverall',120),('nina',121),('len',122)):
        rows.append({'name':'literary_pinehook_'+name,'logical':128,'model':'literary','role':'STRANGER','number':0,'phase':phase})
    # The Drawing (0.4.407): the village on stranger phases one hundred and twenty-three to one hundred and thirty.
    for name,phase in (('albright',123),('rudd',124),('nell',125),('dale',126),('bess',127),('pike',128),('dunn',129),('ricky',130)):
        rows.append({'name':'literary_drawing_'+name,'logical':128,'model':'literary','role':'STRANGER','number':0,'phase':phase})
    for name,phase in (('romina',131),('nacho',132),('luli',133),('nacho_grown',134)):
        rows.append({'name':'literary_corner_'+name,'logical':128,'model':'literary','role':'STRANGER','number':0,'phase':phase})
    # The Kept (0.4.410): the four the machine keeps on stranger phases one hundred and thirty-five to one hundred and thirty-eight.
    for name,phase in (('silas',135),('petra',136),('wendell',137),('tamsin',138)):
        rows.append({'name':'literary_kept_'+name,'logical':128,'model':'literary','role':'STRANGER','number':0,'phase':phase})
    # Last orders (0.4.416): the bar's seven on stranger phases one hundred and thirty-nine to one hundred and forty-five.
    for name,phase in (('stranger',139),('ferris',140),('mags',141),('hob',142),('ott',143),('cass',144),('benny',145)):
        rows.append({'name':'literary_orders_'+name,'logical':128,'model':'literary','role':'STRANGER','number':0,'phase':phase})
    # Borrasca's town (0.4.425, 0.4.426, 0.4.428): the keepers, Earl, the patrons and the mail carrier on stranger phases one hundred and forty-six to one hundred and sixty.
    for name,phase in (('lorna',146),('ames',147),('rowe',148),('kurt',149),('odell',150),('fenn',151),('gus',152),('earl',153),('webb',154),('sherry',155),('roy',156),('jolene',157),('dutch',158),('merle',159),('dorsey',160)):
        rows.append({'name':'literary_drisking_'+name,'logical':128,'model':'literary','role':'STRANGER','number':0,'phase':phase})
    return rows
