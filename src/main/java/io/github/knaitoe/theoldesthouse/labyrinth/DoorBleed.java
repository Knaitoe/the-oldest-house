package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;

/**
 * 0.4.576 (docs/DOOR_BLEEDS_0_4_576.md; the owner: "let's also give each vignette's door more unique hallway bleeds/sound
 * effects"). What one room lets out under its shut door into the hall: something of it on the hall's floor, the light it has
 * on, what is in its air, and what it sounds like from outside. Every vignette has its own. Until now there were thirteen
 * kinds between all of them, and most rooms borrowed one by chance.
 * <p>
 * A bleed is a sense of the room and never its name: a gray door that leaks anyway borrows some room's bleed, as it borrowed a
 * kind before.
 * <p>
 * One constant to a line, written {@code NAME(new Spec()...)}: tools/draw_door_bleeds.py and tools/generate_door_bleed_audio.py
 * read this file for the names that are {@code drawn} and the sounds that are {@code bleed.}, and their checks fail if a picture
 * or a sound here has none there, or one there has none here. A constant is named for its place, and a new one goes anywhere:
 * nothing keeps a bleed by its number.
 */
public enum DoorBleed {
    // ------------------------------------------------------------------------------------------- the House's first rooms
    FLOORBOARDS(new Spec().strip(Strip.BOARDS).sound("vignette.heartbeat")),
    RED_ROOM(new Spec().light(0xC83A32,Rhythm.BREATHE).sound("bleed.red_room")),
    HIDE_AND_CLAP(new Spec().strip(Strip.CLOTH).sound("vignette.clap_muffled")),
    MODEL_HOME(new Spec().light(0xCFE0FF,Rhythm.TV).sound("leak.television")),
    HARRIGAN(new Spec().light(0xFFB14F,Rhythm.STEADY).glow(.8F).sound("leak.phone").pitch(1F)),
    MOTHER_DEN(new Spec().strip(Strip.FUR).sound("minecraft:entity.cat.purr")),
    FLOODED_PASSAGE(new Spec().water(0x6E9DD6).air(Air.DRIP).sound("minecraft:block.water.ambient")),
    DROWNED_TOWN(new Spec().water(0x5E8FB8).air(Air.DRIP).sound("leak.indian_lake")),
    PRESERVED_CAVE(new Spec().water(0x8FB8C8).air(Air.DRIP).sound("leak.preserved_cave")),
    SHALLOWS(new Spec().water(0x86B0A0).air(Air.DRIP).sound("leak.shallows")),
    PHONE_CANOE(new Spec().water(0x6E9DD6).light(0xBFD4FF,Rhythm.BREATHE).glow(.6F).air(Air.DRIP).sound("leak.phone_canoe")),
    GOATMAN(new Spec().drawn(1).sound("goatman.woods")),
    TED_CAVER(new Spec().drawn(1).air(Air.DUST,0x9A9A94).sound("bleed.ted_caver")),
    // -------------------------------------------------------------------------------------------------- the novel's sites
    ZAMPANO_COURTYARD(new Spec().drawn(1).sound("bleed.zampano_courtyard")),
    WHALE(new Spec().drawn(1).sound("bleed.whale")),
    BARN_WELL(new Spec().drawn(1).sound("bleed.barn_well")),
    PLAIN(new Spec().drawn(2).air(Air.SAND).sound("bleed.plain")),
    HOSPITAL(new Spec().light(0xE6F4E0,Rhythm.TUBE).sound("bleed.hospital")),
    KAREN_ROOM(new Spec().light(0xFFF4DC,Rhythm.FLUTTER).sound("bleed.karen_room")),
    HOLLOWAY_CAMP(new Spec().drawn(1).light(0xFF8A3A,Rhythm.FIRE).air(Air.ASH).sound("bleed.holloway_camp")),
    // --------------------------------------------------------------------------------------------- the classics and after
    SEANCE(new Spec().light(0xFFC36B,Rhythm.CANDLE).glow(.7F).sound("bleed.seance")),
    WALLPAPER_NURSERY(new Spec().drawn(2).sound("bleed.wallpaper_nursery")),
    HOTEL(new Spec().strip(Strip.CARPET).sound("leak.hotel_music")),
    HILL_NURSERY(new Spec().drawn(2).air(Air.SNOW).sound("bleed.hill_nursery")),
    MINIATURES(new Spec().drawn(1).sound("bleed.miniatures")),
    MASQUE(new Spec().light(0x3A5AC8,Rhythm.CYCLE).sound("bleed.masque")),
    USHER(new Spec().drawn(2).sound("bleed.usher")),
    WINCHESTER(new Spec().drawn(1).air(Air.DUST,0xD8C08A).sound("bleed.winchester")),
    CHILD_ROOM(new Spec().drawn(1).light(0xFFD98A,Rhythm.STEADY).glow(.5F).sound("bleed.child_room")),
    CRIMSON_HALL(new Spec().drawn(2).air(Air.SNOW).sound("bleed.crimson_hall")),
    BLY_ROUTE(new Spec().drawn(2).air(Air.DRIP).sound("bleed.bly_route")),
    ELK_LOT(new Spec().light(0xFF3B5C,0x3BD0FF,Rhythm.NEON).sound("bleed.elk_lot")),
    ELK_FAN(new Spec().light(0xFFD9A0,Rhythm.FAN).sound("bleed.elk_fan")),
    MAPPING_INTERIOR(new Spec().drawn(1).sound("bleed.mapping_interior")),
    HOLY_RABBIT(new Spec().drawn(2).air(Air.SNOW).sound("bleed.holy_rabbit")),
    CONFESSION(new Spec().light(0xFFC36B,Rhythm.GRILLE).sound("bleed.confession")),
    ELK_CARCASSES(new Spec().drawn(2).air(Air.FLIES).sound("bleed.elk_carcasses")),
    COSTUME_NIGHT(new Spec().drawn(1).light(0xFFD98A,Rhythm.BULBS).sound("bleed.costume_night")),
    MOVIE_NIGHT(new Spec().drawn(1).light(0xDCE8FF,0xFFD0A0,Rhythm.FLUTTER).sound("bleed.movie_night")),
    WINTER_LAKE(new Spec().drawn(2).air(Air.SNOW).sound("bleed.winter_lake")),
    CAMP_BLOOD(new Spec().drawn(1).sound("bleed.camp_blood")),
    DEVILS_ROCK(new Spec().drawn(1).sound("bleed.devils_rock")),
    WHEEL(new Spec().drawn(1).sound("bleed.wheel")),
    GHOSTS_SET(new Spec().drawn(1).light(0xFFFFFF,Rhythm.STEADY).glow(.8F).sound("bleed.ghosts_set")),
    END_WORLD_CABIN(new Spec().drawn(1).sound("bleed.end_world_cabin")),
    FAMILY_COPY(new Spec().drawn(1).light(0xFFD98A,Rhythm.STEADY).glow(.6F).sound("bleed.family_copy")),
    OLD_CABIN(new Spec().light(0xFFD27A,Rhythm.MORSE).sound("bleed.old_cabin")),
    THREE_FINGERS(new Spec().drawn(1).light(0xFF9A4A,Rhythm.FIRE).glow(.7F).sound("bleed.three_fingers")),
    NIGHTCLUB(new Spec().light(0xE83AC8,0x3AC8E8,Rhythm.BEAT).sound("bleed.nightclub")),
    LAKE_HOUSE(new Spec().water(0x3E8A6A).light(0x7FD6A8,Rhythm.CAUSTIC).glow(.7F).air(Air.DRIP).sound("bleed.lake_house")),
    BORRASCA(new Spec().drawn(1).sound("bleed.borrasca")),
    // ----------------------------------------------------------------------------------------------- the six uncanny rooms
    PAINTED_INTERIOR(new Spec().drawn(1).sound("bleed.painted_interior")),
    SECOND_WAITRESS(new Spec().drawn(2).light(0xBFD9FF,Rhythm.STEADY).glow(.7F).sound("bleed.second_waitress")),
    TUNNEL_PERIPHERY(new Spec().light(0xFFB347,Rhythm.TUBE).sound("bleed.tunnel_periphery")),
    MISSING_SLEEP(new Spec().light(0xB8C8B0,Rhythm.TV).sound("bleed.missing_sleep")),
    LAYERED_MAN(new Spec().drawn(1).sound("bleed.layered_man")),
    MOTION_LIGHT(new Spec().light(0xF4F8FF,Rhythm.CLICK).sound("bleed.motion_light")),
    // ------------------------------------------------------------------------------------------------------------ and since
    BACKSEAT(new Spec().water(0x9FB4C8).light(0xFFF6E0,Rhythm.SWEEP).air(Air.DRIP).sound("bleed.backseat")),
    SILO(new Spec().drawn(2).air(Air.DUST,0xE6C878).sound("bleed.silo")),
    DARE_IN_THE_DARK(new Spec().water(0xA8C4D0).light(0xFFC36B,Rhythm.CANDLE).glow(.6F).sound("bleed.dare_in_the_dark")),
    WAY_OUT(new Spec().drawn(1).light(0xFFF1C4,Rhythm.STEADY).sound("bleed.way_out")),
    FEET_UP(new Spec().light(0xFF6A1F,Rhythm.FIRE).sound("bleed.feet_up")),
    WHISTLERS(new Spec().drawn(2).air(Air.SNOW).sound("bleed.whistlers")),
    WORK_PRINT(new Spec().drawn(2).light(0x9FB8FF,Rhythm.TV).glow(.7F).sound("bleed.work_print")),
    POLAROID_TRAIL(new Spec().drawn(1).light(0xFFFFFF,Rhythm.FLASH).sound("bleed.polaroid_trail")),
    LEFT_RIGHT(new Spec().drawn(1).sound("bleed.left_right")),
    DEAD_AIR(new Spec().light(0xFFFFFF,Rhythm.BARS).sound("bleed.dead_air")),
    PITT(new Spec().drawn(1).sound("bleed.pitt")),
    CLOSING_HALL(new Spec().drawn(1).air(Air.DUST,0xE0DACE).sound("bleed.closing_hall")),
    STOREROOM(new Spec().drawn(1).light(0xFFE2A8,Rhythm.SWAY).glow(.7F).sound("bleed.storeroom")),
    STILLNESS(new Spec().sound("bleed.stillness").every(240)),
    LINE(new Spec().drawn(2).sound("bleed.line")),
    OWN_LIGHT(new Spec().light(0xFFE8B8,Rhythm.SWAY).sound("bleed.own_light")),
    POLYBIUS(new Spec().drawn(2).light(0x6AF07A,0xF06AD8,Rhythm.BLINK).glow(.7F).sound("bleed.polybius")),
    REPLACEMENT(new Spec().drawn(2).sound("bleed.replacement")),
    LAST_SWIMMER(new Spec().water(0x7FD8E8).light(0xBFF2F6,Rhythm.CAUSTIC).glow(.6F).sound("bleed.last_swimmer")),
    VACANT_POSSESSION(new Spec().drawn(1).sound("bleed.vacant_possession")),
    PEOPLE_LIGHTS(new Spec().light(0xFFCF8A,Rhythm.SEQUENCE).sound("bleed.people_lights")),
    PLACE_FOR_YOU(new Spec().light(0xFFDCA0,Rhythm.STEADY).air(Air.STEAM).sound("bleed.place_for_you")),
    SECOND_PERSON(new Spec().drawn(1).air(Air.STEAM).sound("bleed.second_person")),
    DEER_ROAD(new Spec().drawn(1).light(0xFFA51E,Rhythm.BLINK).sound("bleed.deer_road")),
    STATE_ROUTE(new Spec().drawn(1).light(0xFFF6E0,Rhythm.SWEEP).rate(2.4F).sound("bleed.state_route")),
    MARSH_SUPPER(new Spec().drawn(1).light(0xFFB86A,Rhythm.STEADY).glow(.6F).sound("bleed.marsh_supper")),
    TALL_GRASS(new Spec().drawn(2).sound("bleed.tall_grass")),
    GROWING_THINGS(new Spec().drawn(2).sound("bleed.growing_things")),
    LAST_CROSSING(new Spec().water(0x8A6B45).air(Air.DRIP).sound("bleed.last_crossing")),
    PINEHOOK(new Spec().drawn(1).light(0xFFE07A,Rhythm.STEADY).glow(.6F).sound("bleed.pinehook")),
    THE_DRAWING(new Spec().drawn(1).light(0xFFF6D8,Rhythm.STEADY).sound("bleed.the_drawing")),
    CORNER_HOUSE(new Spec().drawn(1).light(0xFFFFF0,Rhythm.STEADY).sound("bleed.corner_house")),
    BLACK_CITY(new Spec().drawn(2).air(Air.SNOW).sound("bleed.black_city")),
    KEPT(new Spec().drawn(1).light(0xFF5A2A,Rhythm.BEAT).rate(.5F).glow(.6F).sound("bleed.kept")),
    LAST_ORDERS(new Spec().drawn(1).light(0xFFB14F,Rhythm.STEADY).glow(.5F).sound("bleed.last_orders")),
    BIKE_NIGHT(new Spec().drawn(1).light(0xFF9A2E,Rhythm.CANDLE).glow(.7F).sound("bleed.bike_night")),
    PELL_HOUSE(new Spec().drawn(1).light(0xFFE8B0,Rhythm.STEADY).glow(.5F).sound("bleed.pell_house")),
    ONES_INSIDE(new Spec().drawn(1).light(0xFFD27A,Rhythm.CANDLE).glow(.5F).sound("bleed.ones_inside"));

    /** What lies on the hall's floor at the door. */
    public enum Floor {
        NONE,
        /** Its own picture, sixteen pixels to the block (textures/block/bleed), one or two blocks out from the door. */
        DRAWN,
        /** Water, in the room's own colour. */
        WATER,
        /** One of the four that were there before 0.4.576, drawn as they were. */
        STRIP
    }
    /** The four older strips: the heart's floorboards, the blindfold's cloth, the Mother's fur, the hotel's carpet. */
    public enum Strip {BOARDS,CLOTH,FUR,CARPET}
    /**
     * How the light under the door behaves. Every one that moves is scaled by the reader's screen-effects setting, with no floor:
     * at nothing, each is a steady light at its own middle brightness (docs/DOOR_BLEEDS_0_4_576.md has each).
     */
    public enum Rhythm {STEADY,BREATHE,FIRE,CANDLE,TUBE,TV,FLUTTER,BEAT,BLINK,FLASH,SWEEP,SWAY,FAN,CYCLE,SEQUENCE,MORSE,GRILLE,CAUSTIC,BARS,CLICK,NEON,BULBS}
    /** What drifts out with it. */
    public enum Air {NONE,DRIP,SNOW,ASH,DUST,STEAM,SAND,FLIES}

    /** A bleed as it is written above. */
    static final class Spec {
        Floor floor=Floor.NONE;Strip strip;int depth=1,water=0x6E9DD6;
        int light,light2;Rhythm rhythm=Rhythm.STEADY;float glow=1F,rate=1F;
        Air air=Air.NONE;int airColour=0xFFFFFF;
        String sound;int every=80;float pitch=.85F;
        Spec strip(Strip s){floor=Floor.STRIP;strip=s;return this;}
        Spec drawn(int blocks){floor=Floor.DRAWN;depth=blocks;return this;}
        Spec water(int colour){floor=Floor.WATER;water=colour;return this;}
        Spec light(int colour,Rhythm r){light=colour;rhythm=r;return this;}
        Spec light(int colour,int second,Rhythm r){light=colour;light2=second;rhythm=r;return this;}
        Spec glow(float g){glow=g;return this;}
        Spec rate(float r){rate=r;return this;}
        Spec air(Air a){air=a;return this;}
        Spec air(Air a,int colour){air=a;airColour=colour;return this;}
        Spec sound(String id){sound=id;if(id.startsWith("bleed."))pitch=1F;return this;}
        Spec every(int ticks){every=ticks;return this;}
        Spec pitch(float p){pitch=p;return this;}
    }

    private final Spec spec;
    DoorBleed(Spec spec){this.spec=spec;}

    public Floor floor(){return spec.floor;}
    /** Which of the older strips, for a {@link Floor#STRIP}. */
    public Strip strip(){return spec.strip;}
    /** How many blocks out from the door a drawn floor runs: one or two. */
    public int depth(){return spec.depth;}
    /** The picture of a drawn floor, as the block atlas names it. */
    public String texture(){return "block/bleed/"+name().toLowerCase(java.util.Locale.ROOT);}
    /** The colour of its water. */
    public int water(){return spec.water;}
    /** The colour of the light under the door, or 0 for none; and a second colour for the rhythms that have two. */
    public int light(){return spec.light;}
    public int light2(){return spec.light2;}
    public Rhythm rhythm(){return spec.rhythm;}
    /** How strong the light is, to 1; and how fast its rhythm runs, 1 being its own pace. */
    public float glow(){return spec.glow;}
    public float rate(){return spec.rate;}
    public Air air(){return spec.air;}
    public int airColour(){return spec.airColour;}
    /** The sound's id: the mod's own unless it names another namespace. */
    public String sound(){return spec.sound;}
    /** Ticks between one sounding and the next, a multiple of twenty: the doors are looked at once a second. */
    public int every(){return spec.every;}
    public float pitch(){return spec.pitch;}

    /** What each place lets out. A place not here has nothing of its own. */
    private static final Map<LabyrinthPlace,DoorBleed> OWN=new EnumMap<>(LabyrinthPlace.class);
    static{
        for(var bleed:values())OWN.put(LabyrinthPlace.valueOf(bleed.name()),bleed);
        // The numbered corridor is the hotel's own, and has its carpet and its band.
        OWN.put(LabyrinthPlace.HOTEL_HALLWAY,HOTEL);
        for(var bleed:values())if(bleed.every()<20||bleed.every()%20!=0)throw new IllegalStateException(bleed+" sounds between the doors' seconds");
    }
    private static final List<DoorBleed> ALL=List.of(values());

    /** This place's own bleed, or nothing: only the rooms that have one. */
    @Nullable public static DoorBleed own(@Nullable LabyrinthPlace place){return place==null?null:OWN.get(place);}

    /**
     * What a leaking door to this place lets out. A room with its own lets that out. Any other door that leaks borrows one
     * that is not its own, chosen by {@code lieSeed}: a hint is a sense of some room, and never a label on this one.
     */
    public static DoorBleed forDestination(@Nullable LabyrinthPlace place,int lieSeed){
        if(place==null)return FLOORBOARDS;
        var own=OWN.get(place);
        return own!=null?own:ALL.get(Math.floorMod(lieSeed,ALL.size()));
    }
}
