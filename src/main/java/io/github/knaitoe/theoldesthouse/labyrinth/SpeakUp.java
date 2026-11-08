package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import io.github.knaitoe.theoldesthouse.house.HouseSavedData;
import io.github.knaitoe.theoldesthouse.house.HouseholdFurnitureBlock;
import java.util.*;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * 0.4.327, Speak Up (docs/SPEAK_UP_0_4_327.md): the owner's dining room where everyone answers for the guest until one of them says
 * "You can answer." The reader's first spoken reply in the House is given here, and from then on they have a voice: until they do,
 * a reader who has been through the quiet rooms is not dealt a scene that asks them to speak. The four at the table are the reader's
 * own (bound to them, seen by nobody else); the room is an ordinary gray room, not a Witness story.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class SpeakUp {
    private SpeakUp(){}
    public static final LabyrinthPlace PLACE=LabyrinthPlace.SPEAK_UP;
    public static final String STATE="speak_up_0327",TALK="speak_up";
    /** The four at the table, on stranger phases of their own, and the empty chair's voice (drawn faded out; only its words show). */
    public static final int JOAN=90,PAT=91,DELL=92,RUTH=93,VOICE=94;
    static final String[] NAMES={"Joan","Pat","Dell","Ruth"};
    public static boolean diner(int phase){return phase>=JOAN&&phase<=RUTH;}
    public static boolean cast(int phase){return phase>=JOAN&&phase<=VOICE;}
    public static String skin(int phase){return "supper_"+NAMES[phase-JOAN].toLowerCase(Locale.ROOT);}

    // The room: a two-by-three table, the guest's place in the middle of the west side, the empty chair north of it, Ruth opposite,
    // Dell at the head, facing down the table from in front of the way on. 0.4.398 (the owner: "the table is also too near to the door
    // for comfort"): the table and its chairs stand two blocks further from the way on, so two clear blocks lie between Dell's chair and it.
    public static final BlockPos GUEST=new BlockPos(-1,0,-7),EMPTY=new BlockPos(-1,0,-8),JOAN_CHAIR=new BlockPos(-1,0,-6),PAT_CHAIR=new BlockPos(2,0,-6),
            RUTH_CHAIR=new BlockPos(2,0,-7),DELL_CHAIR=new BlockPos(1,0,-9),DELL_MOVED=new BlockPos(0,0,-9),DELL_STANDS=new BlockPos(2,0,-9),GLASS=new BlockPos(0,1,-7),
            JUG=new BlockPos(-4,1,-8),WAY_ON=new BlockPos(1,0,-12);
    static final int F=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE|Block.UPDATE_SUPPRESS_DROPS;

    // The reader's record. Stage: 0 being talked across, 1 asked, 2 answered, 3 done.
    static final int ACROSS=0,ASKED=1,ANSWERED=2,DONE=3;
    public static final String WATER="Some water, please.",FINE="I'm fine, thank you.",LEAVE="I'd like to leave.",NOTHING="Say nothing.";

    // ------------------------------------------------------------------ the voice
    public static CompoundTag record(LabyrinthData d,UUID id){return d.stateEntry(STATE,id.toString());}
    static void save(LabyrinthData d,UUID id,CompoundTag t){d.setStateEntry(STATE,id.toString(),t);}
    /** Whether this reader has spoken in the House (chosen any answer at Speak Up, saying nothing included). */
    public static boolean voice(LabyrinthData d,UUID id){return record(d,id).getBoolean("Voice");}
    /** Whether this reader has entered any of the quiet rooms. One who never began them (an older record, a fixture) is never held back. */
    static boolean began(LabyrinthData d,UUID id){var rooms=d.stateEntry(HouseReadSites.STATE,id.toString());for(var room:HouseReadSites.CHAIN)if(rooms.getCompound(room.key()).getBoolean("Entered"))return true;return false;}
    /** A reader who began the quiet rooms and has not yet spoken: the scenes that ask them to speak wait. */
    public static boolean held(LabyrinthData d,UUID id){return !voice(d,id)&&began(d,id);}
    /** A held reader who has entered every quiet room is owed Speak Up. */
    public static boolean owed(LabyrinthData d,UUID id){return held(d,id)&&HouseReadSites.next(d,id)==null;}
    /** How many other stories may be dealt to an owed reader before a story door is certain to be Speak Up. */
    public static final int STORIES_FIRST=2;
    /** Whether the next story door is Speak Up for certain: the reader has been dealt enough other stories since the quiet rooms. */
    public static boolean due(LabyrinthData d,UUID id){return record(d,id).getInt("Stories")>=STORIES_FIRST;}
    /** An owed reader was dealt another story (one that does not ask them to speak). */
    public static void storyDealt(LabyrinthData d,UUID id){var t=record(d,id);t.putInt("Stories",t.getInt("Stories")+1);save(d,id,t);}
    /** The scenes that ask the reader to speak, held back until they have. */
    public static boolean needsVoice(LabyrinthPlace p){
        return p==LabyrinthPlace.GHOSTS_SET||p==LabyrinthPlace.SECOND_PERSON||p==LabyrinthPlace.CONFESSION||p==LabyrinthPlace.PLACE_FOR_YOU||TruantPlaces.is(p);
    }

    // ------------------------------------------------------------------ building
    public static void build(ServerLevel l,BlockPos b){
        var paper=VacantRegistry.material("wallpaper").defaultBlockState();var boards=VacantRegistry.material("boards").defaultBlockState();var plaster=TraceRegistry.material("plaster").defaultBlockState();
        box(l,b,-5,-1,-12,5,-1,0,boards);box(l,b,-5,4,-12,5,4,0,plaster);
        for(int x=-5;x<=5;x++)for(int z=-12;z<=0;z++)for(int y=0;y<4;y++){boolean wall=x==-5||x==5||z==-12||z==0;set(l,b,x,y,z,wall?paper:Blocks.AIR.defaultBlockState());}
        // A window in the east wall, night behind it.
        box(l,b,6,0,-7,6,3,-5,Blocks.BLACK_CONCRETE.defaultBlockState());
        for(int z=-7;z<=-5;z++)for(int y=1;y<=2;y++)set(l,b,5,y,z,Blocks.GLASS_PANE.defaultBlockState().setValue(IronBarsBlock.NORTH,z>-7).setValue(IronBarsBlock.SOUTH,z<-5));
        // The table and its six places.
        for(int x=0;x<=1;x++)for(int z=EMPTY.getZ();z<=JOAN_CHAIR.getZ();z++)set(l,b,x,0,z,chair(HouseholdFurnitureBlock.Kind.FORMICA_TABLE,Direction.NORTH));
        for(var at:List.of(GUEST,EMPTY,JOAN_CHAIR))set(l,b,at,chair(HouseholdFurnitureBlock.Kind.CANE_CHAIR,Direction.EAST));
        for(var at:List.of(PAT_CHAIR,RUTH_CHAIR))set(l,b,at,chair(HouseholdFurnitureBlock.Kind.CANE_CHAIR,Direction.WEST));
        set(l,b,DELL_CHAIR,chair(HouseholdFurnitureBlock.Kind.CANE_CHAIR,Direction.SOUTH));
        // Supper at the four places and at the empty one, set for someone; the guest's glass upside down.
        for(var at:List.of(new BlockPos(0,1,-6),new BlockPos(1,1,-6),new BlockPos(1,1,-7),new BlockPos(1,1,-8),new BlockPos(0,1,-8)))set(l,b,at,GhostsSetBlock.of(GhostsSetBlock.Kind.MEAL_STEW,Direction.NORTH));
        set(l,b,GLASS,GhostsSetBlock.of(GhostsSetBlock.Kind.EMPTY_JAR,Direction.EAST));
        // The sideboard on the west wall with a serving counter beside it and the jug of water on that; a lamp over the table.
        set(l,b,-4,0,-7,BarrettHomeBlock.of(BarrettHomeBlock.Kind.SIDEBOARD_L,Direction.EAST));set(l,b,-4,0,-6,BarrettHomeBlock.of(BarrettHomeBlock.Kind.SIDEBOARD_R,Direction.EAST));
        set(l,b,-4,0,-8,GhostsSetBlock.of(GhostsSetBlock.Kind.COUNTER,Direction.EAST));set(l,b,JUG,GhostsSetBlock.of(GhostsSetBlock.Kind.JUG,Direction.SOUTH));
        set(l,b,0,3,-7,HouseReadRooms.part(HouseReadPropBlock.Part.PENDANT).setValue(net.minecraft.world.level.block.HorizontalDirectionalBlock.FACING,Direction.SOUTH));
        LabyrinthBuilder.entrance(l,b,paper,boards,plaster);
        LabyrinthBuilder.doors(l,b,PLACE);
        RoomActors.settlePanes(l,b,PLACE);
    }
    private static BlockState chair(HouseholdFurnitureBlock.Kind k,Direction facing){return HouseholdFurnitureBlock.state(k,facing);}
    private static void set(ServerLevel l,BlockPos b,int x,int y,int z,BlockState s){BuildBlocks.set(l,b.offset(x,y,z),s,F);}
    private static void set(ServerLevel l,BlockPos b,BlockPos at,BlockState s){BuildBlocks.set(l,b.offset(at),s,F);}
    private static void box(ServerLevel l,BlockPos b,int x0,int y0,int z0,int x1,int y1,int z1,BlockState s){for(int x=x0;x<=x1;x++)for(int y=y0;y<=y1;y++)for(int z=z0;z<=z1;z++)set(l,b,x,y,z,s);}

    // ------------------------------------------------------------------ where
    @Nullable public static BlockPos base(net.minecraft.server.MinecraftServer s){var origin=HouseSavedData.get(s).houseOrigin();return origin==null?null:LabyrinthPlaces.base(origin,PLACE);}
    public static AABB room(BlockPos b){return new AABB(b.getX()-5,b.getY()-1,b.getZ()-12,b.getX()+6,b.getY()+5,b.getZ()+1);}
    public static boolean inside(ServerPlayer p){var b=base(p.server);return b!=null&&p.level().dimension().equals(HouseDimensions.INTERIOR)&&room(b).contains(p.position());}

    // ------------------------------------------------------------------ arriving
    /** Every arrival starts the supper over; a reader who already has a voice finds them eating, the way on clear. */
    public static void arrived(ServerPlayer p,LabyrinthPlace place){
        if(place!=PLACE||p.isSpectator())return;
        var d=LabyrinthData.get(p.server);var t=record(d,p.getUUID());boolean spoke=t.getBoolean("Voice");
        var fresh=new CompoundTag();fresh.putBoolean("Voice",spoke);if(t.contains("Answer"))fresh.putString("Answer",t.getString("Answer"));fresh.putInt("Stories",t.getInt("Stories"));
        fresh.putInt("Stage",spoke?DONE:ACROSS);fresh.putBoolean("Here",true);save(d,p.getUUID(),fresh);
        var l=p.server.getLevel(HouseDimensions.INTERIOR);var b=base(p.server);if(l==null||b==null)return;
        clearCast(l,b,p.getUUID());
        // The table is set again for a newcomer, unless someone else is still at it.
        if(!spoke&&others(l,b,p)==0){set(l,b,GLASS,GhostsSetBlock.of(GhostsSetBlock.Kind.EMPTY_JAR,Direction.EAST));set(l,b,DELL_MOVED,Blocks.AIR.defaultBlockState());set(l,b,DELL_CHAIR,chair(HouseholdFurnitureBlock.Kind.CANE_CHAIR,Direction.SOUTH));}
        Talk.close(p);
    }
    private static int others(ServerLevel l,BlockPos b,ServerPlayer p){int n=0;for(var o:l.players())if(o!=p&&o.isAlive()&&!o.isSpectator()&&room(b).contains(o.position()))n++;return n;}

    // ------------------------------------------------------------------ the cast
    private static final Map<UUID,Map<Integer,UUID>> CAST=new HashMap<>();
    private static final Map<UUID,Long> SAID=new HashMap<>();
    static void clearCast(ServerLevel l,BlockPos b,UUID reader){
        for(var a:l.getEntitiesOfClass(LiteraryActor.class,room(b).inflate(2),a->a.scene().equals(PLACE.id())&&a.owner().map(reader::equals).orElse(false)))a.discard();
        CAST.remove(reader);
    }
    /** What a closed and reopened world forgets (the saved cast stays in the room). Fixtures only. */
    public static void forget(){CAST.clear();}
    @Nullable static LiteraryActor actor(ServerLevel l,UUID reader,int phase){var ids=CAST.get(reader);if(ids==null||!ids.containsKey(phase))return null;var e=l.getEntity(ids.get(phase));return e instanceof LiteraryActor a&&a.isAlive()?a:null;}
    static BlockPos seat(int phase,CompoundTag t){return switch(phase){case JOAN->JOAN_CHAIR;case PAT->PAT_CHAIR;case RUTH->RUTH_CHAIR;case DELL->t.getBoolean("Moved")?null:DELL_CHAIR;default->EMPTY;};}
    static float yaw(BlockPos chair){return chair.getX()<0?-90:chair.getX()>=2?90:0;}
    /**
     * Puts the reader's four (and the voice in the empty chair) at the table if they are not there. 0.4.398 (the owner: "two variants of
     * the same NPC spawned?"): the cast is saved with the room, so after the world is closed and opened again with the reader at the table
     * the ones already sitting there are taken up again rather than a second four set down over them, and any second of one is sent away.
     */
    static void seatCast(ServerLevel l,BlockPos b,ServerPlayer p,CompoundTag t){
        if(!l.areEntitiesLoaded(net.minecraft.world.level.ChunkPos.asLong(b.offset(GUEST)))||!l.areEntitiesLoaded(net.minecraft.world.level.ChunkPos.asLong(b.offset(DELL_STANDS))))return;
        var ids=CAST.computeIfAbsent(p.getUUID(),k->new HashMap<>());
        for(var a:l.getEntitiesOfClass(LiteraryActor.class,room(b).inflate(2),a->a.scene().equals(PLACE.id())&&a.owner().map(p.getUUID()::equals).orElse(false))){
            if(!cast(a.phase())){a.discard();continue;}
            var kept=actor(l,p.getUUID(),a.phase());
            if(kept==null)ids.put(a.phase(),a.getUUID());else if(kept!=a)a.discard();
        }
        for(int phase=JOAN;phase<=VOICE;phase++){
            if(actor(l,p.getUUID(),phase)!=null)continue;
            var a=LiteraryRegistry.ACTOR.get().create(l);if(a==null)return;
            a.bind(PLACE,p.getUUID());a.appearance(LiteraryActor.STRANGER,phase);
            var chair=seat(phase,t);Vec3 at;float facing;
            if(chair==null){at=Vec3.atBottomCenterOf(b.offset(DELL_STANDS));facing=90;}else{at=Vec3.atBottomCenterOf(b.offset(chair));facing=yaw(chair);}
            a.moveTo(at.x,at.y,at.z,facing,0);a.setYBodyRot(facing);a.setYHeadRot(facing);a.setNoGravity(true);a.setCustomNameVisible(false);
            if(phase==VOICE)a.fade(100);
            if(l.addFreshEntity(a))ids.put(phase,a.getUUID());
        }
    }
    /** A line said above someone's head, and into the reader's panel. */
    static void line(ServerLevel l,ServerPlayer p,int phase,String text){
        var a=actor(l,p.getUUID(),phase);if(a!=null){a.say(text);a.speaks(text);SAID.put(a.getUUID(),l.getGameTime());}
        Talk.line(p,phase==VOICE?"":NAMES[phase-JOAN],text);
    }
    /** Small talk above their heads only. */
    static void aside(ServerLevel l,ServerPlayer p,int phase,String text){var a=actor(l,p.getUUID(),phase);if(a!=null){a.say(text);a.speaks(text);SAID.put(a.getUUID(),l.getGameTime());}}
    static void look(@Nullable LiteraryActor a,Vec3 at){
        if(a==null)return;var d=at.subtract(a.getEyePosition());float yaw=(float)(Math.toDegrees(Math.atan2(-d.x,d.z)));
        a.setYHeadRot(yaw);a.setXRot((float)-Math.toDegrees(Math.atan2(d.y,Math.sqrt(d.x*d.x+d.z*d.z))));
    }

    // ------------------------------------------------------------------ the supper
    @SubscribeEvent public static void tick(ServerTickEvent.Post e){
        var s=e.getServer();var l=s.getLevel(HouseDimensions.INTERIOR);var b=base(s);if(l==null||b==null)return;
        long now=l.getGameTime();
        // Words fade after a few seconds.
        SAID.entrySet().removeIf(entry->{if(now-entry.getValue()<70)return false;var a=l.getEntity(entry.getKey());if(a!=null)a.setCustomNameVisible(false);return true;});
        var d=LabyrinthData.get(s);
        for(var reader:List.copyOf(CAST.keySet())){var p=s.getPlayerList().getPlayer(reader);if(p==null||!inside(p)){clearCast(l,b,reader);var t=record(d,reader);if(t.getBoolean("Here")){t.putBoolean("Here",false);save(d,reader,t);}}}
        for(var p:l.players()){
            if(!p.isAlive()||p.isSpectator()||!inside(p))continue;
            var t=record(d,p.getUUID());if(!t.getBoolean("Here"))continue;
            seatCast(l,b,p,t);
            advance(l,b,p,t);
            save(d,p.getUUID(),t);
        }
    }
    static void advance(ServerLevel l,BlockPos b,ServerPlayer p,CompoundTag t){
        int stage=t.getInt("Stage");Vec3 guest=p.getEyePosition();Vec3 empty=Vec3.atCenterOf(b.offset(EMPTY)).add(0,.4,0);
        if(stage==ACROSS){
            int tick=t.getInt("T")+1;t.putInt("T",tick);
            if(tick==1)Talk.begin(p,TALK,"The table");
            switch(tick){
                case 20->{line(l,p,RUTH,"There's room. Sit down.");look(actor(l,p.getUUID(),RUTH),guest);}
                case 60->aside(l,p,JOAN,"Did you lock the car?");
                case 95->aside(l,p,DELL,"Nobody's taking that car.");
                case 140->{line(l,p,PAT,"Would you like something to drink?");look(actor(l,p.getUUID(),PAT),guest);}
                case 160->line(l,p,JOAN,"They're fine.");
                case 220->{line(l,p,DELL,"Are you staying for supper?");look(actor(l,p.getUUID(),DELL),guest);}
                case 245->line(l,p,JOAN,"They can't stay.");
                case 265->line(l,p,PAT,"Of course they're staying.");
                case 325->{line(l,p,PAT,"Bread?");look(actor(l,p.getUUID(),PAT),guest);}
                case 345->line(l,p,JOAN,"They don't eat bread.");
                case 365->line(l,p,DELL,"Since when?");
                case 440->{if(!Talk.key(p).equals(TALK))Talk.begin(p,TALK,"The table");look(actor(l,p.getUUID(),RUTH),guest);line(l,p,RUTH,"You can answer.");ask(p);t.putInt("Stage",ASKED);}
                default->{}
            }
            return;
        }
        if(stage==ASKED){look(actor(l,p.getUUID(),RUTH),guest);return;}
        if(stage==ANSWERED){
            int tick=t.getInt("T2")+1;t.putInt("T2",tick);String answer=t.getString("Answer");
            switch(tick){
                case 40->aside(l,p,RUTH,"Where were we.");
                case 60->aside(l,p,PAT,"Supper.");
                default->{}
            }
            // One by one they turn to the empty chair.
            int turned=tick<100?0:Math.min(4,1+(tick-100)/20);
            int[] order={PAT,DELL,JOAN,RUTH};
            for(int i=0;i<4;i++){var a=actor(l,p.getUUID(),order[i]);if(i<turned)look(a,empty);else if(tick<100)look(a,guest);}
            if(tick==200)line(l,p,JOAN,"Would you like something to drink?");
            // The empty chair answers with the reader's own words, given as one of their replies; a reader who said nothing hears nothing from it.
            if(tick==240){
                if(answer.equals(NOTHING))line(l,p,RUTH,"That's an answer too.");else line(l,p,VOICE,answer);
                Talk.end(p);
            }
            if(tick==280){
                if(!t.getBoolean("Moved"))moveChair(l,b,p,t);
                t.putInt("Stage",DONE);
            }
        }
    }
    static void ask(ServerPlayer p){
        var d=LabyrinthData.get(p.server);var replies=new ArrayList<Talk.Reply>();
        for(var r:List.of(WATER,FINE,LEAVE))replies.add(Talk.reply(r,who->answer(who,r)));
        if(HouseRead.value(d,p.getUUID(),HouseRead.Trait.SELF,true).equals("UNSEEN"))replies.add(Talk.act(NOTHING,who->answer(who,NOTHING)));
        Talk.ask(p,replies);
    }
    /** The reader's first answer in the House, and what the table does with it. */
    static void answer(ServerPlayer p,String chosen){
        var d=LabyrinthData.get(p.server);var t=record(d,p.getUUID());if(t.getInt("Stage")!=ASKED)return;
        var l=p.server.getLevel(HouseDimensions.INTERIOR);var b=base(p.server);if(l==null||b==null)return;
        t.putBoolean("Voice",true);t.putString("Answer",chosen);t.putInt("Stage",ANSWERED);t.putInt("T2",0);
        switch(chosen){
            case WATER->{
                line(l,p,RUTH,"Of course.");var at=b.offset(GLASS);
                l.playSound(null,at,SoundEvents.BOTTLE_FILL,net.minecraft.sounds.SoundSource.BLOCKS,.6F,1);l.sendParticles(p,ParticleTypes.SPLASH,false,at.getX()+.5,at.getY()+.3,at.getZ()+.5,8,.08,.05,.08,.02);
                var water=PotionContents.createItemStack(Items.POTION,Potions.WATER);water.set(DataComponents.CUSTOM_NAME,Component.literal("A glass of water"));
                if(!p.getInventory().add(water))p.drop(water,false);
            }
            case FINE->{line(l,p,RUTH,"All right.");set(l,b,GLASS,Blocks.AIR.defaultBlockState());l.playSound(null,b.offset(GLASS),SoundEvents.GLASS_HIT,net.minecraft.sounds.SoundSource.BLOCKS,.4F,1.4F);}
            case LEAVE->{line(l,p,DELL,"Oh. Sorry.");moveChair(l,b,p,t);}
            default->line(l,p,RUTH,"That's an answer too.");
        }
        save(d,p.getUUID(),t);
    }
    static void moveChair(ServerLevel l,BlockPos b,ServerPlayer p,CompoundTag t){
        set(l,b,DELL_CHAIR,Blocks.AIR.defaultBlockState());set(l,b,DELL_MOVED,chair(HouseholdFurnitureBlock.Kind.CANE_CHAIR,Direction.SOUTH));
        l.playSound(null,b.offset(DELL_CHAIR),SoundEvents.WOOD_PLACE,net.minecraft.sounds.SoundSource.BLOCKS,.8F,.6F);
        var dell=actor(l,p.getUUID(),DELL);if(dell!=null){var at=Vec3.atBottomCenterOf(b.offset(DELL_STANDS));dell.moveTo(at.x,at.y,at.z,90,0);dell.setYBodyRot(90);dell.setYHeadRot(90);}
        t.putBoolean("Moved",true);
    }
    /** A hand on any of them brings the question back if the panel was closed before it was answered. */
    public static void talk(ServerPlayer p,LiteraryActor a){
        var t=record(LabyrinthData.get(p.server),p.getUUID());
        if(t.getInt("Stage")==ASKED){if(!Talk.resume(p,TALK)){Talk.begin(p,TALK,"The table");Talk.line(p,"Ruth","You can answer.");ask(p);}return;}
        if(t.getInt("Stage")==DONE&&a.phase()!=VOICE)p.displayClientMessage(Component.literal(NAMES[a.phase()-JOAN]+" goes on eating."),true);
    }
}
