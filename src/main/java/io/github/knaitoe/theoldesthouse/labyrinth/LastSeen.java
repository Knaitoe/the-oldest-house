package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.network.HousePackets;
import io.github.knaitoe.theoldesthouse.network.LastSeenFolderPayload;
import java.text.Normalizer;
import java.util.*;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * 0.4.667, Last Seen, the first part (docs/LAST_SEEN_0_4_667.md), from the owner's approval of docs/THREE_VIGNETTES_PROPOSAL_2026_11_17.md.
 * The place: the municipal office at the edge of a city park, with its front desk, its poster wall and its archive of folders, and the
 * park itself in the morning, with the avenue past its railing (LastSeenSite). Three people work in the office: Nora, who keeps the
 * archive, at her desk; Patricia and Luz behind the counter (stranger phases 253 to 255). Each has lines of their own, and every thing
 * answers a hand.
 * <p>0.4.668, the second part (docs/LAST_SEEN_0_4_668.md): the archive and the first one. The ten children's folders are things to carry
 * (LastSeenFolder), one of each, filed in the bays by the first letter of the name on their spines. Nora gives her rule and the chore: the
 * day's pile, four folders on the long table, to go back where their names say. When the pile is shelved (or the morning has gone on
 * long enough), the first of them is at the fountain, made where nobody is looking; Luz goes to the front door and says so, and Nora goes
 * as far as the edge of the path and no further. A reader who finds the girl's folder by her face and brings it to her has matched her:
 * she takes it, looks at it, looks at them, says one thing, and gives it back. The morning is one clock for everyone there (Day, held in
 * memory as the Corner's street is); what each reader shelved, read and matched is their own. A folder goes back on its shelf when its
 * reader leaves. The House deals the place from depth eight.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class LastSeen {
    private LastSeen(){}
    public static final LabyrinthPlace PLACE=LabyrinthPlace.LAST_SEEN;
    /** The ten who come back are on stranger phases 243 to 252; the office's three on 253 to 255. */
    public static final int CHILDREN_FIRST=243,CHILDREN=10,FIRST=253,NORA=0,PATRICIA=1,LUZ=2,PEOPLE=3;
    private static final String[] CHILD_SKINS={"lastseen_rocio","lastseen_tomas","lastseen_micaela","lastseen_brian","lastseen_daiana","lastseen_lautaro","lastseen_abril","lastseen_joel","lastseen_yanina","lastseen_santino"};
    private static final String[] SKINS={"lastseen_nora","lastseen_patricia","lastseen_luz"};
    private static final BlockPos[] SEATS={LastSeenSite.NORA_AT,LastSeenSite.PATRICIA_AT,LastSeenSite.LUZ_AT};
    public static boolean cast(int phase){return phase>=CHILDREN_FIRST&&phase<FIRST+PEOPLE;}
    public static boolean office(int phase){return phase>=FIRST&&phase<FIRST+PEOPLE;}
    /** One of the ten, at child scale. */
    public static boolean child(int phase){return phase>=CHILDREN_FIRST&&phase<CHILDREN_FIRST+CHILDREN;}
    public static String skin(int phase){return phase>=FIRST?SKINS[phase-FIRST]:CHILD_SKINS[phase-CHILDREN_FIRST];}
    public static boolean is(LiteraryActor a){return a.role()==LiteraryActor.STRANGER&&cast(a.phase());}
    static void cue(ServerPlayer p,String line){p.displayClientMessage(Component.literal(line).withStyle(ChatFormatting.GRAY,ChatFormatting.ITALIC),true);}
    /** The House deals the place from depth eight, now that there is something to do in it. */
    public static final int DEPTH=8;
    public static boolean dealable(LabyrinthData d,UUID reader){return d.returnDepth(reader)>=DEPTH;}

    // ------------------------------------------------------------------------------------------------ the morning, in memory
    /** The first of them, and the day's pile: four of the ten whose folders are out on the long table this morning. */
    public static final int ROCIO=0;
    public static final int[] PILE={1,2,3,4};
    /** The pile's place on the long table, and where she stands: at the fountain's south rim, facing the office. */
    public static final BlockPos PILE_AT=new BlockPos(-17,0,-7);
    public static final Vec3 RIM=new Vec3(-7.5,0,-45.5);
    /** The morning's stages: the archive's work; Luz at the door and Nora on her way out; the girl at the fountain. */
    public static final int MORNING=0,CALLED=1,FOUND=2;
    /** Ticks: the longest the morning waits for the pile before she comes anyway; how long Luz waits at the door before each thing she says
     * and before Nora gets up; how long the girl looks at her folder, and how long before she gives it back; an empty park's reset. */
    public static final int MORNING_MAX=3600,AT_DOOR=30,LOOK=40,GIVE=100,EMPTY_RESET=200;
    static final UUID PILE_HOLDER=new UUID(0L,0L);
    /** Luz's walk to the front door; Nora's from her desk, through the partition and out to the head of the path. */
    static final List<Vec3> LUZ_ROUTE=List.of(new Vec3(3.5,0,-10.5),new Vec3(1.5,0,-12.6)),
        NORA_ROUTE=List.of(new Vec3(-12.5,0,-6.5),new Vec3(-11.5,0,-10.5),new Vec3(-8.5,0,-11.5),new Vec3(-6.5,0,-11.5),new Vec3(0.5,0,-12.4),
            new Vec3(0.5,0,-15.5),new Vec3(-5.5,0,-17.5),new Vec3(-7.5,0,-18.5));

    static final class Day {
        final BlockPos base;final long id;int stage=MORNING,since,step,stepAt,empty,tick;boolean fresh=true;
        final ArrayDeque<Integer> pile=new ArrayDeque<>();
        /** Folders off their shelves and who has each (the pile's own are out with nobody); each folder's times out; each bay's own fill. */
        final Map<Integer,UUID> out=new HashMap<>();final int[] issue=new int[CHILDREN];final Map<Integer,Integer> fill=new HashMap<>();
        @Nullable UUID rocio,giver;int holding=-1,held;
        /** Who of the office's three are up out of their chairs (a bit each). */
        int up;
        final Map<Integer,List<Vec3>> routes=new HashMap<>();final Map<Integer,Integer> leg=new HashMap<>();
        final Set<UUID> sawRim=new HashSet<>();
        Day(BlockPos b,long id){base=b;this.id=id;}
    }
    static final Map<Long,Day> DAYS=new HashMap<>();
    @Nullable static Day day(@Nullable BlockPos b){return b==null?null:DAYS.get(b.asLong());}
    /** The morning's stage at a park, or -1 if nobody has been there since the server started. */
    public static int stage(BlockPos b){var d=day(b);return d==null?-1:d.stage;}
    /** Whose a folder is out with: a reader's id, the pile's, or null if it is in its bay. */
    public static @Nullable UUID out(BlockPos b,int child){var d=day(b);return d==null?null:d.out.get(child);}
    public static boolean inPile(BlockPos b,int child){var d=day(b);return d!=null&&d.pile.contains(child);}
    public static @Nullable LiteraryActor rocio(ServerLevel l,BlockPos b){var d=day(b);return d==null?null:actor(l,d.rocio);}
    /** The child whose folder the girl is holding, or -1. */
    public static int holding(BlockPos b){var d=day(b);return d==null?-1:d.holding;}
    public static void clearAll(){DAYS.clear();}
    /** For a test or a look at the place: the morning has gone on long enough, and she comes as soon as she can come unseen. */
    public static void hurry(BlockPos b){var d=day(b);if(d!=null&&d.stage==MORNING)d.since=Math.max(d.since,MORNING_MAX);}
    @SubscribeEvent public static void stopped(ServerStoppedEvent e){clearAll();}

    static CompoundTag own(ServerPlayer p){return LiteraryVignettes.personal(LabyrinthData.get(p.server),p.getUUID(),PLACE);}
    static void save(ServerPlayer p,CompoundTag own){LiteraryVignettes.save(LabyrinthData.get(p.server),p.getUUID(),PLACE,own);}
    static Vec3 world(BlockPos b,Vec3 r){return new Vec3(b.getX()+r.x,b.getY()+r.y,b.getZ()+r.z);}

    /** A reader in: the morning is started if nobody is there, the office's three are put at their places, and the morning is heard. */
    public static void arrive(ServerPlayer p,BlockPos b,CompoundTag own){
        var l=p.serverLevel();
        var d=DAYS.computeIfAbsent(b.asLong(),k->new Day(b,l.getGameTime()));
        own.putInt("Visits",own.getInt("Visits")+1);
        if(own.getLong("DayId")!=d.id){own.putLong("DayId",d.id);own.remove("Waits");}
        ensure(l,b);
        l.playSound(null,b.offset(0,1,-14),LastSeenRegistry.TRAFFIC.get(),SoundSource.AMBIENT,.35F,1F);
    }

    // ------------------------------------------------------------------------------------------------ the office's three
    static AABB area(BlockPos b){return new AABB(b.offset(LastSeenSite.WEST,-4,LastSeenSite.NORTH)).minmax(new AABB(b.offset(LastSeenSite.EAST,16,1)));}
    /** One of the office's three, wherever in the place they are. */
    public static @Nullable LiteraryActor find(ServerLevel l,BlockPos b,int who){
        for(var a:l.getEntitiesOfClass(LiteraryActor.class,area(b),x->is(x)&&x.phase()==FIRST+who))if(a.isAlive())return a;
        return null;
    }
    @Nullable static LiteraryActor actor(ServerLevel l,@Nullable UUID id){if(id==null)return null;var e=l.getEntity(id);return e instanceof LiteraryActor a&&a.isAlive()?a:null;}
    /** Each of the three is sat in their chair unless the morning has them up; nothing is made while the office's entities are unloaded. */
    public static void ensure(ServerLevel l,BlockPos b){
        var d=day(b);
        for(int who=0;who<PEOPLE;who++){
            var at=b.offset(SEATS[who]);
            if(!l.isLoaded(at)||!l.areEntitiesLoaded(ChunkPos.asLong(at)))continue;
            var a=find(l,b,who);
            if(a==null){
                a=LiteraryRegistry.ACTOR.get().create(l);if(a==null)continue;
                a.bind(PLACE,null);a.appearance(LiteraryActor.STRANGER,FIRST+who);
                sit(l,a,at);
                if(!l.addFreshEntity(a))continue;
                continue;
            }
            if(d!=null&&(d.up&1<<who)!=0)continue;
            sit(l,a,at);
        }
    }
    /** In the chair at the cell, turned the way the chair is (ActorSeat lowers the body onto its seat and keeps it there). */
    static void sit(ServerLevel l,LiteraryActor a,BlockPos at){
        var st=l.getBlockState(at);
        float yaw=LastSeenBlock.is(st,LastSeenBlock.Kind.CHAIR)?st.getValue(LastSeenBlock.FACING).toYRot():Direction.SOUTH.toYRot();
        a.moveTo(at.getX()+.5,at.getY(),at.getZ()+.5,yaw,0);a.setYBodyRot(yaw);a.setYHeadRot(yaw);a.gesture(0);a.setDeltaMovement(Vec3.ZERO);
    }
    static void speak(ServerLevel l,BlockPos b,LiteraryActor a,@Nullable String name,String line){
        a.say(name,line);a.speaks(line);
        for(var r:readers(l,b))if(r.distanceToSqr(a)<24*24)r.displayClientMessage(Component.literal((name==null?"":name+": ")+"“"+line+"”").withStyle(ChatFormatting.GRAY),true);
    }

    /** A word with one of the three, or a hand on one of the ten. */
    public static void talk(ServerPlayer p,LiteraryActor a,CompoundTag own){
        if(child(a.phase())){child(p,a,own);return;}
        if(!office(a.phase()))return;
        var d=day(LiteraryVignettes.base(p,PLACE));
        int who=a.phase()-FIRST,heard=own.getInt("Heard"+who);
        String line;
        if(who==NORA&&d!=null&&d.stage>=FOUND&&heard>=1){
            line=(own.getInt("Matched")&1<<ROCIO)!=0?LastSeenTexts.NORA_AFTER:own.getBoolean("Waits")?LastSeenTexts.NORA_FIND:LastSeenTexts.NORA_WAITS;
            own.putBoolean("Waits",true);
        }else if(who==NORA&&heard>=2&&!own.getBoolean("Leaving")&&left(p)){line=LastSeenTexts.LEAVING;own.putBoolean("Leaving",true);}
        else if(who==PATRICIA&&heard==0&&young(p))line=LastSeenTexts.PATRICIA_YOUNG;
        else line=LastSeenTexts.line(who,heard);
        own.putInt("Heard"+who,heard+1);
        if(who==NORA&&line.equals(LastSeenTexts.RULE))own.putBoolean("Rule",true);
        a.say(LastSeenTexts.NAMES[who],line);a.speaks(line);
    }
    /** The House's read: a reader it reads as young; a reader it reads as having left a home, or been in danger in one. */
    static boolean young(ServerPlayer p){return HouseRead.category(p,HouseRead.Trait.AGE).map(c->c.value().equals("DIGITAL")).orElse(false);}
    static boolean left(ServerPlayer p){return HouseRead.category(p,HouseRead.Trait.HOME).map(c->c.value().equals("RUNAWAY")||c.value().equals("DANGER")).orElse(false);}

    // ------------------------------------------------------------------------------------------------ the folders
    static CompoundTag tag(ItemStack s){return s.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag();}
    /** Whose folder a stack is, or -1. */
    public static int child(ItemStack s){if(s.isEmpty()||!s.is(LastSeenRegistry.FOLDER.get()))return -1;var t=tag(s);return t.contains("Child")?Mth.clamp(t.getInt("Child"),0,CHILDREN-1):-1;}
    static ItemStack folder(Day d,int child){
        var s=new ItemStack(LastSeenRegistry.FOLDER.get());
        var t=new CompoundTag();t.putInt("Child",child);t.putLong("Site",d.base.asLong());t.putLong("Day",d.id);t.putInt("Issue",d.issue[child]);
        CustomData.set(DataComponents.CUSTOM_DATA,s,t);s.set(DataComponents.CUSTOM_MODEL_DATA,new CustomModelData(child+1));
        return s;
    }
    /** Whether a folder is the one out this morning: its park's day, and its latest time off the shelf. Any other is nobody's. */
    static boolean live(Day d,ItemStack s){int c=child(s);if(c<0)return false;var t=tag(s);return t.getLong("Site")==d.base.asLong()&&t.getLong("Day")==d.id&&t.getInt("Issue")==d.issue[c];}
    static boolean live(ItemStack s){var d=DAYS.get(tag(s).getLong("Site"));return d!=null&&live(d,s);}
    static boolean carries(ServerPlayer p,Day d,int child){var inv=p.getInventory();for(int i=0;i<inv.getContainerSize();i++){var s=inv.getItem(i);if(child(s)==child&&live(d,s))return true;}return false;}
    static boolean carrying(ServerPlayer p,Day d){var inv=p.getInventory();for(int i=0;i<inv.getContainerSize();i++)if(live(d,inv.getItem(i)))return true;return false;}
    static void strip(ServerPlayer p,Day d,int child){var inv=p.getInventory();for(int i=0;i<inv.getContainerSize();i++){var s=inv.getItem(i);if(child(s)==child&&tag(s).getLong("Site")==d.base.asLong())inv.setItem(i,ItemStack.EMPTY);}}

    /** Held open: the child's photograph and the typed form, drawn on the reader's own screen. */
    public static void read(ServerPlayer p,ItemStack s){
        int c=child(s);if(c<0)return;
        show(p,c);
        if(LiteraryVignettes.current(p)==PLACE){var own=own(p);own.putInt("Read",own.getInt("Read")|1<<c);save(p,own);}
    }
    static void show(ServerPlayer p,int c){HousePackets.send(p,new LastSeenFolderPayload(c,LastSeenTexts.file(c),LastSeenTexts.CHILDREN[c],LastSeenTexts.WHERE[c],LastSeenTexts.DAY[c]));}

    /** A bay's letter: the alphabet laid along the thirty-five bays in walking order, so each letter has one bay or two. */
    public static char letter(int bay){return (char)('A'+bay*26/LastSeenSite.SHELVES.size());}
    /** The bay a cell is part of (its foot, and the three bays above it to the boxes), or -1. */
    public static int bay(BlockPos rel){
        for(int i=0;i<LastSeenSite.SHELVES.size();i++){var s=LastSeenSite.SHELVES.get(i);if(s.getX()==rel.getX()&&s.getZ()==rel.getZ()&&rel.getY()>=0&&rel.getY()<LastSeenSite.CEILING)return i;}
        return -1;
    }
    static char initial(int child){return Character.toUpperCase(Normalizer.normalize(LastSeenTexts.CHILDREN[child],Normalizer.Form.NFD).charAt(0));}
    /** The bay a child's folder is filed in: the first whose letter is their name's. */
    public static int bayFor(int child){char c=initial(child);for(int i=0;i<LastSeenSite.SHELVES.size();i++)if(letter(i)==c)return i;return 0;}
    /** The child whose folder is filed in a bay, or -1. */
    public static int filedIn(int bay){for(int c=0;c<CHILDREN;c++)if(bayFor(c)==bay)return c;return -1;}
    /** The bay's cell at eye height, where a folder out leaves its gap. */
    public static BlockPos gapCell(int child){return LastSeenSite.SHELVES.get(bayFor(child)).above(1);}
    static void gap(ServerLevel l,Day d,int child){
        var at=d.base.offset(gapCell(child));var st=l.getBlockState(at);if(!LastSeenBlock.is(st,LastSeenBlock.Kind.SHELF))return;
        int s=st.getValue(LastSeenBlock.STAGE);if(s!=LastSeenBlock.ONE_OUT)d.fill.put(child,s);
        l.setBlock(at,st.setValue(LastSeenBlock.STAGE,LastSeenBlock.ONE_OUT),3);
    }
    static void filled(ServerLevel l,Day d,int child){
        var at=d.base.offset(gapCell(child));var st=l.getBlockState(at);if(!LastSeenBlock.is(st,LastSeenBlock.Kind.SHELF))return;
        if(st.getValue(LastSeenBlock.STAGE)==LastSeenBlock.ONE_OUT)l.setBlock(at,st.setValue(LastSeenBlock.STAGE,d.fill.getOrDefault(child,child%3)),3);
    }
    /** Out to a reader: into their empty hand, or their pack. Every time out is a new issue; the folder's gap shows on its shelf. */
    static boolean take(ServerPlayer p,Day d,int child,CompoundTag own){
        d.issue[child]++;
        var stack=folder(d,child);
        if(p.getMainHandItem().isEmpty())p.setItemInHand(InteractionHand.MAIN_HAND,stack);else if(!p.getInventory().add(stack))return false;
        d.out.put(child,p.getUUID());gap(p.serverLevel(),d,child);
        play(p.serverLevel(),p.blockPosition(),LastSeenRegistry.PAPER.get(),.6F);
        own.putInt("Read",own.getInt("Read")|1<<child);show(p,child);
        return true;
    }
    /** Back in its bay: whatever copy is still about is nobody's now. */
    static void reshelve(ServerLevel l,Day d,int child){d.out.remove(child);d.issue[child]++;filled(l,d,child);}

    // ------------------------------------------------------------------------------------------------ a hand on things
    /** A hand on something of the office's or the park's. True if it was theirs to answer. The card is the first paper: the House's own reading answers it. */
    public static boolean click(ServerPlayer p,BlockPos b,BlockPos rel,CompoundTag own){
        var l=p.serverLevel();var st=l.getBlockState(b.offset(rel));
        // The houses across the avenue keep their doors locked; only the way on opens (OnwardPlacements sets that one).
        if(st.getBlock() instanceof DoorBlock&&rel.getZ()==LastSeenSite.HOUSE_FRONT&&rel.getX()!=LastSeenSite.ONWARD.getX()){cue(p,LastSeenTexts.HOUSE_DOOR);return true;}
        if(!LastSeenBlock.is(st))return false;
        // The park's gate is shut but not locked: a hand swings both leaves open, and they stay open for everyone.
        if(st.getValue(LastSeenBlock.KIND)==LastSeenBlock.Kind.GATE&&st.getValue(LastSeenBlock.STAGE)==LastSeenBlock.SHUT){
            LastSeenSite.gate(l,b,LastSeenBlock.OPEN);
            l.playSound(null,b.offset(LastSeenSite.GATE_W),net.minecraft.sounds.SoundEvents.IRON_DOOR_OPEN,SoundSource.BLOCKS,.7F,.8F);
            cue(p,LastSeenTexts.GATE);return true;
        }
        int stage=st.getValue(LastSeenBlock.STAGE);
        if(st.getValue(LastSeenBlock.KIND)==LastSeenBlock.Kind.SHELF)return shelf(p,b,rel,stage,own);
        if(st.getValue(LastSeenBlock.KIND)==LastSeenBlock.Kind.TABLE&&rel.equals(PILE_AT)&&day(b)!=null){
            if(stage==1)return pile(p,b,own);
            if(day(b).pile.isEmpty()){cue(p,LastSeenTexts.PILE_DONE);return true;}
        }
        String line=switch(st.getValue(LastSeenBlock.KIND)){
            case CARD->null;
            case COUNTER->stage==1?LastSeenTexts.FLAP:LastSeenTexts.COUNTER;
            case CHAIR->LastSeenTexts.CHAIR;
            case SHELF->null;
            case POSTER->LastSeenTexts.poster(stage);
            case POSTER_B->LastSeenTexts.poster(8+stage);
            case TABLE->stage==1?LastSeenTexts.PILE:stage==2?LastSeenTexts.LAMP:LastSeenTexts.TABLE;
            case DESK->stage==LastSeenBlock.DRAWN?LastSeenTexts.DESK_OPEN:LastSeenTexts.DESK;
            case LOUVRE->LastSeenTexts.LOUVRE;
            case TUBE->LastSeenTexts.TUBE;
            case RAILING->stage==1?LastSeenTexts.POST:LastSeenTexts.RAILING;
            case GATE->stage==LastSeenBlock.CHAINED?LastSeenTexts.GATE_CHAINED:LastSeenTexts.GATE_OPEN;
            case LAMP_POST->stage==LastSeenBlock.DARK?LastSeenTexts.LAMP_DARK:LastSeenTexts.LAMP_POST;
            case BENCH->LastSeenTexts.BENCH;
            case FOUNTAIN->stage>=2?LastSeenTexts.BOWL:LastSeenTexts.FOUNTAIN;
            case KIOSK->LastSeenTexts.KIOSK;
            case TRUNK->LastSeenTexts.TRUNK;
            case LEAVES->stage==1?LastSeenTexts.SILK:LastSeenTexts.PLANE;
            case PLAY->LastSeenTexts.play(stage);
            case BANDSTAND->LastSeenTexts.BANDSTAND;
            case SHELTER->stage==2?LastSeenTexts.STOP:LastSeenTexts.SHELTER;
            case SHUTTER->LastSeenTexts.SHUTTER;
        };
        if(line==null)return false;
        cue(p,line);
        return true;
    }
    /**
     * A bay of the archive. With a folder in hand: back where its name says, or told whose names these are. Empty-handed at a bay that
     * holds one of the ten: it comes down, and is held open. Otherwise the spines, and the letter they begin with.
     */
    static boolean shelf(ServerPlayer p,BlockPos b,BlockPos rel,int stage,CompoundTag own){
        int bay=bay(rel);var d=day(b);
        if(stage==LastSeenBlock.BOXES||bay<0){cue(p,LastSeenTexts.BOXES);return true;}
        char letter=letter(bay);
        var hand=p.getMainHandItem();int c=child(hand);
        if(c>=0){
            if(d==null||!live(d,hand)){hand.setCount(0);cue(p,LastSeenTexts.BACK_ON_SHELF);return true;}
            if(bayFor(c)!=bay){cue(p,LastSeenTexts.wrongBay(letter));return true;}
            hand.setCount(0);reshelve(p.serverLevel(),d,c);own.putInt("Shelved",own.getInt("Shelved")|1<<c);
            play(p.serverLevel(),b.offset(rel),LastSeenRegistry.PAPER.get(),.6F);
            cue(p,LastSeenTexts.shelved(letter));return true;
        }
        int here=filedIn(bay);
        if(d==null||here<0){cue(p,stage==LastSeenBlock.ONE_OUT?LastSeenTexts.SHELF_GAP:LastSeenTexts.spines(letter));return true;}
        if(d.out.containsKey(here)||d.holding==here){cue(p,LastSeenTexts.SHELF_GAP);return true;}
        if(carrying(p,d)){cue(p,LastSeenTexts.ONE_AT_A_TIME);return true;}
        if(!hand.isEmpty()){cue(p,LastSeenTexts.spines(letter));return true;}
        if(take(p,d,here,own))cue(p,LastSeenTexts.taken(here,letter));
        return true;
    }
    /** The day's pile on the long table: one folder at a time, empty-handed, to be shelved. The table is bare once it is done. */
    static boolean pile(ServerPlayer p,BlockPos b,CompoundTag own){
        var d=day(b);var l=p.serverLevel();
        if(!d.pile.isEmpty()&&carrying(p,d)){cue(p,LastSeenTexts.ONE_AT_A_TIME);return true;}
        if(d.pile.isEmpty()||!p.getMainHandItem().isEmpty()){cue(p,LastSeenTexts.PILE);return true;}
        int c=d.pile.peekFirst();
        if(!take(p,d,c,own))return true;
        d.pile.pollFirst();own.putInt("FromPile",own.getInt("FromPile")+1);
        cue(p,LastSeenTexts.fromPile(c));
        if(d.pile.isEmpty())table(l,d,0);
        return true;
    }
    static void table(ServerLevel l,Day d,int stage){var at=d.base.offset(PILE_AT);var st=l.getBlockState(at);if(LastSeenBlock.is(st,LastSeenBlock.Kind.TABLE))l.setBlock(at,st.setValue(LastSeenBlock.STAGE,stage),3);}

    // ------------------------------------------------------------------------------------------------ the first of them
    /** A hand on one of the ten. Holding their own folder: they take it. Holding another's: they look at it, then away. Otherwise: cold. */
    static void child(ServerPlayer p,LiteraryActor a,CompoundTag own){
        int who=a.phase()-CHILDREN_FIRST;var b=LiteraryVignettes.base(p,PLACE);var d=day(b);var l=p.serverLevel();
        var hand=p.getMainHandItem();int c=child(hand);
        if(d!=null&&d.holding==who)return;
        if(d!=null&&c==who&&live(d,hand)&&d.holding<0){
            hand.setCount(0);d.holding=c;d.giver=p.getUUID();d.held=0;
            a.setItemInHand(InteractionHand.MAIN_HAND,folder(d,c));
            play(l,a.blockPosition(),LastSeenRegistry.PAPER.get(),.5F);
            own.putInt("Matched",own.getInt("Matched")|1<<c);own.putInt("Days",Math.max(1,own.getInt("Days")));
            for(var r:readers(l,b))if(r!=p){var o=own(r);o.putInt("SawMatched",o.getInt("SawMatched")|1<<c);save(r,o);}
            return;
        }
        if(c>=0){cue(p,LastSeenTexts.NOT_HERS);return;}
        l.sendParticles(ParticleTypes.SNOWFLAKE,a.getX(),a.getY()+.6,a.getZ(),6,.15,.2,.15,0);
        cue(p,LastSeenTexts.COLD);
    }
    /** Made at the rim only where no reader can see her: if any can, she is not there yet. */
    static boolean makeRocio(ServerLevel l,Day d,List<ServerPlayer> here){
        if(actor(l,d.rocio)!=null)return true;
        var at=world(d.base,RIM);
        if(!l.isLoaded(BlockPos.containing(at))||!l.areEntitiesLoaded(ChunkPos.asLong(BlockPos.containing(at))))return false;
        if(Drawing.seen(here,at))return false;
        var a=LiteraryRegistry.ACTOR.get().create(l);if(a==null)return false;
        a.bind(PLACE,null);a.appearance(LiteraryActor.STRANGER,CHILDREN_FIRST+ROCIO);
        a.moveTo(at.x,at.y,at.z,0,0);a.setYBodyRot(0);a.setYHeadRot(0);a.setDeltaMovement(Vec3.ZERO);
        if(!l.addFreshEntity(a))return false;
        d.rocio=a.getUUID();return true;
    }
    /** She holds her folder and looks at it; looks at the one who brought it and says one thing; then gives it back. */
    static void holding(ServerLevel l,Day d){
        if(d.holding<0)return;var a=actor(l,d.rocio);int c=d.holding;
        if(a==null){reshelve(l,d,c);d.holding=-1;d.giver=null;return;}
        d.held++;
        var giver=d.giver==null?null:l.getServer().getPlayerList().getPlayer(d.giver);
        if(d.held<LOOK){a.setXRot(38F);return;}
        if(d.held==LOOK){
            a.setXRot(0F);
            if(giver!=null){var to=giver.position().subtract(a.position());float yaw=(float)(Mth.atan2(to.z,to.x)*Mth.RAD_TO_DEG)-90F;a.setYHeadRot(yaw);a.setYBodyRot(yaw);a.setYRot(yaw);}
            speak(l,d.base,a,null,LastSeenTexts.WAITING);
            return;
        }
        if(d.held<GIVE)return;
        a.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);
        a.setYHeadRot(0);a.setYBodyRot(0);a.setYRot(0);
        if(giver!=null&&readers(l,d.base).contains(giver)&&giver.getInventory().add(folder(d,c))){d.out.put(c,giver.getUUID());cue(giver,LastSeenTexts.GIVES_BACK);}
        else reshelve(l,d,c);
        d.holding=-1;d.giver=null;
    }

    // ------------------------------------------------------------------------------------------------ the morning's clock
    static boolean loaded(ServerLevel l,Day d){
        for(var at:List.of(d.base.offset(0,0,-8),d.base.offset(-16,0,-8),BlockPos.containing(world(d.base,RIM))))if(!l.isLoaded(at)||!l.areEntitiesLoaded(ChunkPos.asLong(at)))return false;
        return true;
    }
    /** The morning begins: the pile on the table and its gaps on the shelves, every other folder in its bay, the front door shut, the three in their chairs. */
    static void start(ServerLevel l,Day d){
        for(int c=0;c<CHILDREN;c++)filled(l,d,c);
        d.pile.clear();d.out.clear();
        for(int c:PILE){d.pile.add(c);d.out.put(c,PILE_HOLDER);gap(l,d,c);}
        table(l,d,1);door(l,d,false);
        for(var a:l.getEntitiesOfClass(LiteraryActor.class,area(d.base),x->x.role()==LiteraryActor.STRANGER&&child(x.phase())))a.discard();
        d.stage=MORNING;d.since=0;d.up=0;d.step=0;d.rocio=null;d.holding=-1;
        ensure(l,d.base);
    }
    /** Back as it was when nobody is there: the girl gone, the three in their chairs, the pile on the table, every folder in its bay. */
    static void reset(ServerLevel l,Day d){
        var a=actor(l,d.rocio);if(a!=null)a.discard();
        for(int c=0;c<CHILDREN;c++)filled(l,d,c);
        table(l,d,1);door(l,d,false);d.up=0;
        for(int who=0;who<PEOPLE;who++){var o=find(l,d.base,who);if(o!=null)sit(l,o,d.base.offset(SEATS[who]));}
    }
    static void door(ServerLevel l,Day d,boolean open){
        var at=d.base.offset(LastSeenSite.FRONT_DOOR);var st=l.getBlockState(at);
        if(st.getBlock() instanceof DoorBlock door&&st.getValue(DoorBlock.OPEN)!=open){door.setOpen(null,l,st,at,open);}
    }
    static void walk(ServerLevel l,Day d){
        for(var who:List.copyOf(d.routes.keySet())){
            var a=find(l,d.base,who);if(a==null){d.routes.remove(who);d.leg.remove(who);continue;}
            var route=d.routes.get(who);int i=d.leg.get(who);
            if(i>=route.size()){d.routes.remove(who);d.leg.remove(who);continue;}
            // Spoken to on the way, they stop to answer, as the Drawing's villagers do, then go on.
            if(a.listening())continue;
            var target=world(d.base,route.get(i));
            if(SceneFootpaths.walk(a,target,.11)||a.position().distanceToSqr(target)<.09)d.leg.put(who,i+1);
        }
    }
    static void send(Day d,int who,List<Vec3> route){d.up|=1<<who;d.routes.put(who,new ArrayList<>(route));d.leg.put(who,0);}
    static boolean busy(Day d,int who){return d.routes.containsKey(who);}
    /** The pile shelved (none of its folders off the shelves), or the morning gone on long enough: she is at the fountain. */
    static void morning(ServerLevel l,Day d,List<ServerPlayer> here){
        switch(d.stage){
            case MORNING->{
                boolean shelved=d.pile.isEmpty()&&Arrays.stream(PILE).noneMatch(d.out::containsKey);
                if((shelved&&d.since>=40||d.since>=MORNING_MAX)&&makeRocio(l,d,here)){d.stage=CALLED;d.since=0;d.step=0;send(d,LUZ,LUZ_ROUTE);}
            }
            case CALLED->called(l,d);
            default->{}
        }
    }
    /** Luz to the front door, which she opens; what she says; Nora up and out as far as the head of the path. */
    static void called(ServerLevel l,Day d){
        var luz=find(l,d.base,LUZ);
        switch(d.step){
            case 0->{if(!busy(d,LUZ)){door(l,d,true);if(luz!=null){luz.setYHeadRot(180);luz.setYBodyRot(180);luz.setYRot(180);}d.step=1;d.stepAt=d.since;}}
            case 1->{if(d.since-d.stepAt>=AT_DOOR){if(luz!=null)speak(l,d.base,luz,LastSeenTexts.NAMES[LUZ],LastSeenTexts.LUZ_CALL);d.step=2;d.stepAt=d.since;}}
            case 2->{if(d.since-d.stepAt>=AT_DOOR*2){if(luz!=null){luz.setYHeadRot(90);speak(l,d.base,luz,LastSeenTexts.NAMES[LUZ],LastSeenTexts.LUZ_NORA);}send(d,NORA,NORA_ROUTE);d.step=3;d.stepAt=d.since;}}
            case 3->{if(!busy(d,NORA)){var nora=find(l,d.base,NORA);if(nora!=null){nora.setYHeadRot(180);nora.setYBodyRot(180);nora.setYRot(180);}d.stage=FOUND;d.step=0;}}
            default->{}
        }
    }
    /** Each reader who comes near her at the rim is told what she is like, once a morning; they saw the first one come back. */
    static void watchers(ServerLevel l,Day d,List<ServerPlayer> here){
        var a=actor(l,d.rocio);if(a==null)return;
        for(var r:here)if(r.distanceToSqr(a)<8*8&&d.sawRim.add(r.getUUID())){cue(r,LastSeenTexts.AT_RIM);var o=own(r);o.putBoolean("SawFirst",true);save(r,o);}
    }
    /** A folder whose reader has left, logged off, died or let it go goes back on its shelf. */
    static void release(ServerLevel l,Day d,List<ServerPlayer> here){
        for(var e:List.copyOf(d.out.entrySet())){
            int c=e.getKey();var who=e.getValue();
            if(who.equals(PILE_HOLDER)||c==d.holding)continue;
            var p=l.getServer().getPlayerList().getPlayer(who);
            boolean has=p!=null&&carries(p,d,c);
            if(p!=null&&here.contains(p)&&has)continue;
            if(p!=null)strip(p,d,c);
            reshelve(l,d,c);
            if(has)cue(p,LastSeenTexts.BACK_ON_SHELF);
        }
    }
    /** Any folder anywhere that is not the one out this morning is nobody's, and is gone. */
    static void sweep(net.minecraft.server.MinecraftServer server){
        for(var p:server.getPlayerList().getPlayers()){
            var inv=p.getInventory();
            for(int i=0;i<inv.getContainerSize();i++){var s=inv.getItem(i);if(child(s)>=0&&!live(s))inv.setItem(i,ItemStack.EMPTY);}
        }
    }

    /** Everyone in the scene: inside its railing, in the office or on the avenue. */
    public static List<ServerPlayer> readers(ServerLevel l,BlockPos b){
        var out=new ArrayList<ServerPlayer>();
        for(var p:l.players()){
            if(p.isSpectator()||!p.isAlive())continue;
            var r=p.position().subtract(Vec3.atLowerCornerOf(b));
            if(r.x>=LastSeenSite.WEST&&r.x<=LastSeenSite.EAST+1&&r.z>=LastSeenSite.NORTH&&r.z<=1&&r.y>=-4&&r.y<=16)out.add(p);
        }
        return out;
    }
    /**
     * Every tick while someone is there: the morning's clock, the three's walks, the girl and her folder, folders whose readers have gone.
     * Every few seconds, the morning: the avenue's traffic beyond the railing, pigeons somewhere on the paths, and, while a reader is in
     * the office, the typewriter at Nora's desk. Each is heard where it is.
     */
    @SubscribeEvent public static void tick(ServerTickEvent.Post e){
        var server=e.getServer();
        if(server.getTickCount()%40==0)sweep(server);
        if(DAYS.isEmpty())return;
        var l=server.getLevel(NovelRooms.dimension(PLACE));if(l==null)return;
        for(var d:List.copyOf(DAYS.values())){
            var here=readers(l,d.base);
            // Folders go back on their shelves when their readers go, even when nobody is left in the place.
            if(server.getTickCount()%10==0&&!d.fresh)release(l,d,here);
            if(here.isEmpty()){if(++d.empty>=EMPTY_RESET&&loaded(l,d)){reset(l,d);DAYS.remove(d.base.asLong());}continue;}
            d.empty=0;
            if(d.fresh){if(!loaded(l,d))continue;d.fresh=false;start(l,d);}
            d.tick++;d.since++;
            walk(l,d);
            morning(l,d,here);
            holding(l,d);
            if(d.tick%5==0)watchers(l,d,here);
            var rnd=l.getRandom();
            if(d.tick%240==60)play(l,d.base.offset(-8+rnd.nextInt(34)-17,1,LastSeenSite.ROAD_S-3),LastSeenRegistry.TRAFFIC.get(),.6F);
            if(d.tick%300==150)play(l,d.base.offset(LastSeenSite.PATH_X+rnd.nextInt(9)-4,0,LastSeenSite.PATH_Z+rnd.nextInt(30)-15),LastSeenRegistry.PIGEONS.get(),.5F);
            if(d.tick%180==90&&here.stream().anyMatch(p->LastSeenSite.inOffice(p.position().subtract(Vec3.atLowerCornerOf(d.base)))))
                play(l,d.base.offset(LastSeenSite.DESK).above(),LastSeenRegistry.TYPEWRITER.get(),.45F);
        }
    }
    static void play(ServerLevel l,BlockPos at,SoundEvent e,float volume){l.playSound(null,at,e,SoundSource.AMBIENT,volume,.95F+l.getRandom().nextFloat()*.1F);}
}
