package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import io.github.knaitoe.theoldesthouse.network.HousePackets;
import io.github.knaitoe.theoldesthouse.network.ShallowsCuePayload;
import java.util.*;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.*;
import net.minecraft.sounds.*;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.event.entity.player.*;
import net.neoforged.neoforge.event.entity.item.ItemTossEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * Verb: throwing. The memory is personal; crossing its threshold alone never completes it.
 *
 * <p>0.4.101: the reader takes her wrists while a boy holds her feet, and the two swing her out over the water with the
 * reader's own strafe keys, on a count of three. The lake does not take her. She breaks on its surface, lies still, gets up
 * wrong and runs across the water into the woods on all fours. The girl and the boy at her feet are this reader's alone; the
 * three boys on the bank are shared, and speak to each reader privately.
 *
 * <p>0.4.157 (owner, October 28; docs/SHALLOWS_TOGETHER_0_4_157.md): with friends there it is one throw. A reader who comes
 * to the bank while another's recollection is under way joins it: the first to join takes her feet in Lonnie's place, and
 * Lonnie counts from the water's edge; anyone after that stands on the bank with Bud, Hank and Clay. The two holding her
 * swing her together, a stroke only when both swing the same way. Everyone there sees her, hears the boys and keeps a
 * Witness line of their own for how they were part of it.
 */
public final class Shallows {
    public static final String ID="shallows", BOY="HouseShallowsBoy", WORDS="HouseLakeWords", CARRIER="HouseShallowsCarrier";
    private static final String OWNER="ShallowsOwner", ACTOR="ShallowsActor";
    /** The recollected girl's phases. */
    public static final int LYING=0, CARRIED=1, FLYING=2, BROKEN=3, RISING=4, SCURRY=5, GONE=6;
    /** The carrier's hold on her feet. */
    public static final int HOLD=1, SWINGING=2, RELEASED=3;
    /** Reactions shown with a line, or alone, by the reader's client. */
    public static final int CALM=0, JEER=1, CHEER=2, FROZEN=3, RECOIL=4, WHISPER=5;
    /** How long the broken body lies still on the water, gets up, and may take to reach the woods. */
    public static final int STILL_TICKS=70, RISE_TICKS=44, SCURRY_TICKS=160, GONE_TICKS=170;
    /** Swing radius (blocks) and the body centre's height over the bank while she hangs between them. */
    public static final double SWING_RADIUS=.75, HANG=.95, SCURRY_SPEED=.3;
    /** Ticks of one stroke; a stroke may be followed once it is this far through. */
    public static final int STROKE_TICKS=12;
    private static final float READY=.65F, RELEASE=.6F;
    /** The swing's peak on each forward stroke (one, two, three) and on each back stroke. */
    private static final float[] FORWARD={.62F,.92F,1.15F}, BACK={.5F,.82F};
    /** Carry slots along the bank, so two readers in the same memory never stand in one place. */
    private static final int[] SLOTS={0,-7,7};
    private static final double LINE_Z=-11.5;
    /** The reader faces the boy across her, east; the lake is on the reader's left. */
    public static final float READER_YAW=-90, CARRIER_YAW=90;
    /** The three boys on the bank, in their saved order, then the boy at her feet and the girl. */
    public static final int BUD=0, HANK=1, CLAY=2, LONNIE=3, STACEY=4;
    private static final String[] WHO={"Bud","Hank","Clay","Lonnie","Stacey"};
    private static final int[] LOOK={0,2,3};
    /** A reader's part in another's recollection: who they are with, and whether they hold her feet or stand on the bank. */
    public static final String WITH="With0157",ROLE="Role0157",FEET="Feet0157",THROW="Throw0157";
    public static final String AT_FEET="feet",ON_BANK="bank",AT_WRISTS="wrists";
    /** Two readers' swings within this many ticks of each other are one stroke. */
    public static final int TOGETHER=8;
    private record Line(int who,String text,int mood){}
    private record Due(long at,Line line){}
    private static final Line[] OPENING={new Line(LONNIE,"Took you long enough. Grab her arms.",CALM),new Line(BUD,"Careful. She'll put a curse on you.",JEER),
            new Line(STACEY,"I'm not anything. Let me up.",CALM),new Line(HANK,"Witches float. We're only going to see.",JEER)};
    private static final Line[] NUDGES={new Line(LONNIE,"Her arms. Come on, both hands.",CALM),new Line(CLAY,"Everybody's watching.",CALM),
            new Line(BUD,"Ask her where she goes at night.",JEER),new Line(STACEY,"Please let go of my feet.",CALM)};

    /** Transient carry state per recollection: the stroke under way, the count, queued lines and the flight. */
    private static final class Scene {
        int slot,stroke,queued,forward,strokeAge=STROKE_TICKS,strokeLength=STROKE_TICKS,idle,line,wrong;
        float from,to,angle;long next;boolean released;Vec3 velocity=Vec3.ZERO;
        /** 0.4.157: the reader at her feet, once they have taken hold, and each pair of hands' last swing. */
        @Nullable UUID feet;int wristsWant,feetWant;long wristsAt,feetAt;@Nullable UUID thrownWith;
        final PriorityQueue<Due> due=new PriorityQueue<>(Comparator.comparingLong(Due::at));
        void later(long at,Line line){due.add(new Due(at,line));}
    }
    private static final Map<UUID,Scene> SCENES=new HashMap<>();
    private Shallows() {}

    // ------------------------------------------------------------------------------------------------ the bank
    public static void build(MinecraftServer server,ServerLevel level,BlockPos base){
        IndianLakeArchitecture.shallows(level,base);LabyrinthData data=LabyrinthData.get(server);CompoundTag state=data.state(ID);
        if(!state.getBoolean("Built"))state.putBoolean("FreshCast0493",true);
        state.putBoolean("Built",true);data.setState(ID,state);boys(level,base);
    }
    private static void boys(ServerLevel level,BlockPos base){
        boolean fresh=LabyrinthData.get(level.getServer()).state(ID).getBoolean("FreshCast0493");
        for(int i=0;i<3;i++){
            String tag=BOY+i;int index=i;
            var boy=LakeOriginals.ensure(level,base,LabyrinthPlace.SHALLOWS,ID,"boy"+i,tag,DrownedTownRegistry.CONGREGANT.get(),fresh,b->{
                b.pose(false,false);b.memoryBoy(true);b.addTag(BOY);b.addTag(tag);
                b.moveTo(base.getX()+new int[]{-4,5,-3}[index]+.5,base.getY(),base.getZ()+new int[]{-10,-11,-5}[index]+.5,180,0);
            });
            // Each original keeps its identity; 0.4.101 only gives the three of them their own faces.
            if(boy!=null)boy.boyLook(LOOK[i]);
        }
    }
    public static @Nullable LakeCongregantEntity onlooker(ServerLevel level,int index){
        var record=LabyrinthData.get(level.getServer()).state(ID).getCompound("NativeOriginals0493").getCompound("boy"+index);
        return record.hasUUID("Id")&&level.getEntity(record.getUUID("Id")) instanceof LakeCongregantEntity boy?boy:null;
    }
    /** Her place on the bank in a carry slot; the reader stands west of her and the boy east, both facing her. */
    public static Vec3 spot(BlockPos base,int slot){return new Vec3(base.getX()+SLOTS[slot]+.5,base.getY(),base.getZ()+LINE_Z);}
    public static Vec3 stance(BlockPos base,int slot){return spot(base,slot).add(-1.5,0,0);}
    public static Vec3 carrierStance(BlockPos base,int slot){return spot(base,slot).add(1.5,0,0);}
    /** Where Lonnie counts from while a reader holds her feet: the last of the bank, beside them and clear of her swing. */
    public static Vec3 edge(BlockPos base,int slot){return spot(base,slot).add(2.5,0,-1.2);}
    /** Where her body is while she swings: positive angles toward the water, which lies north of the bank. */
    public static Vec3 swung(BlockPos base,int slot,float angle){
        return spot(base,slot).add(0,HANG+SWING_RADIUS*(1-Math.cos(angle)),-SWING_RADIUS*Math.sin(angle));
    }
    private static boolean clearSlot(ServerLevel level,BlockPos base,int slot){
        for(int x=SLOTS[slot]-2;x<=SLOTS[slot]+2;x++){
            var feet=base.offset(x,0,(int)Math.floor(LINE_Z));
            for(var cell:new BlockPos[]{feet,feet.above()})
                if(!level.getBlockState(cell).getCollisionShape(level,cell).isEmpty()||!level.getFluidState(cell).isEmpty())return false;
            if(level.getBlockState(feet.below()).getCollisionShape(level,feet.below()).isEmpty())return false;
        }
        return true;
    }
    private static int chooseSlot(ServerPlayer player,BlockPos base,CompoundTag record){
        Set<Integer> taken=new HashSet<>();var players=LabyrinthData.get(player.server).state(ID).getCompound("Players");
        for(String key:players.getAllKeys())if(!key.equals(player.getUUID().toString())&&players.getCompound(key).getBoolean("Active"))taken.add(players.getCompound(key).getInt("Slot"));
        if(record.contains("Slot")&&!taken.contains(record.getInt("Slot"))&&record.getInt("Slot")<SLOTS.length)return record.getInt("Slot");
        for(int i=0;i<SLOTS.length;i++)if(!taken.contains(i)&&clearSlot(player.serverLevel(),base,i))return i;
        for(int i=0;i<SLOTS.length;i++)if(!taken.contains(i))return i;
        return 0;
    }

    // ------------------------------------------------------------------------------------------------ records
    public static CompoundTag personal(LabyrinthData data,UUID owner){return data.state(ID).getCompound("Players").getCompound(owner.toString()).copy();}
    private static void save(LabyrinthData data,UUID owner,CompoundTag record){
        CompoundTag state=data.state(ID),players=state.getCompound("Players");players.put(owner.toString(),record);state.put("Players",players);data.setState(ID,state);
    }
    public static @Nullable LakeWitchEntity actor(ServerPlayer player){
        CompoundTag record=personal(LabyrinthData.get(player.server),player.getUUID());
        if(!record.hasUUID("Actor"))return null;
        ServerLevel level=player.server.getLevel(HouseDimensions.OUTSIDE);if(level==null)return null;
        Entity actor=level.getEntity(record.getUUID("Actor"));
        return actor instanceof LakeWitchEntity witch&&player.getUUID().equals(witch.memoryOwner())?witch:null;
    }
    /** The recollection a reader is part of: their own, or the one they joined (0.4.157). */
    public static @Nullable LakeWitchEntity memory(ServerPlayer player){
        var host=with(LabyrinthData.get(player.server),player.getUUID());if(host==null)return actor(player);
        CompoundTag record=personal(LabyrinthData.get(player.server),host);if(!record.hasUUID("Actor"))return null;
        ServerLevel level=player.server.getLevel(HouseDimensions.OUTSIDE);if(level==null)return null;
        return level.getEntity(record.getUUID("Actor")) instanceof LakeWitchEntity witch&&host.equals(witch.memoryOwner())?witch:null;
    }
    /** The reader whose recollection this player joined, while they are part of it. */
    public static @Nullable UUID with(LabyrinthData data,UUID player){var r=personal(data,player);return r.getBoolean("Active")&&r.hasUUID(WITH)?r.getUUID(WITH):null;}
    /** Whether a viewer is part of an owner's recollection, and so sees her and the boy who held her feet. */
    public static boolean sharesMemory(ServerPlayer viewer,UUID owner){return owner.equals(with(LabyrinthData.get(viewer.server),viewer.getUUID()));}
    /** The reader whose recollection this player is in, if they are online: the player themselves, or the one they joined. */
    private static ServerPlayer host(ServerPlayer player){
        var id=with(LabyrinthData.get(player.server),player.getUUID());if(id==null)return player;var host=owner(player.serverLevel(),id);return host==null?player:host;
    }
    /** Everyone in a recollection now: its reader and those with them on the bank. */
    static List<ServerPlayer> group(ServerPlayer owner){
        var out=new ArrayList<ServerPlayer>();out.add(owner);var data=LabyrinthData.get(owner.server);
        Set<ServerPlayer> players=new LinkedHashSet<>(owner.server.getPlayerList().getPlayers());players.addAll(owner.serverLevel().players());
        for(var p:players)if(p!=owner&&owner.getUUID().equals(with(data,p.getUUID()))&&IndianLakeRooms.inside(p,LabyrinthPlace.SHALLOWS))out.add(p);
        return out;
    }
    /** The reader who joined to hold her feet, while they are here. */
    static @Nullable ServerPlayer feetHere(ServerPlayer owner){
        var record=personal(LabyrinthData.get(owner.server),owner.getUUID());if(!record.hasUUID(FEET))return null;
        var p=owner(owner.serverLevel(),record.getUUID(FEET));
        return p!=null&&p.isAlive()&&!p.isSpectator()&&owner.getUUID().equals(with(LabyrinthData.get(owner.server),p.getUUID()))&&IndianLakeRooms.inside(p,LabyrinthPlace.SHALLOWS)?p:null;
    }
    /** The boy who holds her feet, for this reader only. */
    public static @Nullable LakeCongregantEntity carrier(ServerPlayer player){
        CompoundTag record=personal(LabyrinthData.get(player.server),player.getUUID());
        if(!record.hasUUID("Carrier"))return null;
        ServerLevel level=player.server.getLevel(HouseDimensions.OUTSIDE);if(level==null)return null;
        return level.getEntity(record.getUUID("Carrier")) instanceof LakeCongregantEntity boy&&player.getUUID().equals(boy.carrierOwner())?boy:null;
    }
    private static @Nullable ServerPlayer owner(ServerLevel level,UUID id){
        ServerPlayer owner=level.getServer().getPlayerList().getPlayer(id);
        // Native fake players used by server tests live in the level rather than the connection list.
        return owner!=null?owner:level.players().stream().filter(p->p.getUUID().equals(id)).findFirst().orElse(null);
    }
    private static Scene scene(LakeWitchEntity girl){
        return SCENES.computeIfAbsent(girl.getUUID(),id->{
            var s=new Scene();var owner=girl.memoryOwner();
            if(owner!=null&&girl.level() instanceof ServerLevel level)s.slot=Math.max(0,Math.min(SLOTS.length-1,personal(LabyrinthData.get(level.getServer()),owner).getInt("Slot")));
            return s;
        });
    }
    private static void discard(LakeWitchEntity girl){SCENES.remove(girl.getUUID());girl.discard();}

    // ------------------------------------------------------------------------------------------------ arrival
    public static void onArrive(ServerPlayer player,LabyrinthPlace place){
        if(place!=LabyrinthPlace.SHALLOWS)return;LabyrinthData data=LabyrinthData.get(player.server);
        BlockPos base=IndianLakeRooms.base(player.server,place);if(base==null)return;
        CompoundTag record=personal(data,player.getUUID());
        // 0.4.157: a reader who comes while another's recollection is under way joins it, unless their own is. One who
        // already joined stays with it while it lasts, even while its girl is not loaded.
        if(record.getBoolean("Active")&&record.hasUUID(WITH)){
            var was=owner(player.serverLevel(),record.getUUID(WITH));
            if(was!=null&&personal(data,was.getUUID()).getBoolean("Active")&&IndianLakeRooms.inside(was,place))return;
            record.putBoolean("Active",false);record.remove(WITH);record.remove(ROLE);save(data,player.getUUID(),record);
        }
        if(!player.isSpectator()&&!(record.getBoolean("Active")&&record.hasUUID("Actor"))){var host=hostHere(player);if(host!=null){join(player,host);return;}}
        if(!IndianLakeProgress.canDealShallows(data,player.getUUID()))return;
        IndianLakeRooms.keepLoaded(player.serverLevel(),base,place);boys(player.serverLevel(),base);
        record.remove(WITH);record.remove(ROLE);
        // Reconnects reuse their saved actors, loaded or not; a new visit cannot duplicate a recollection.
        boolean resumed=record.getBoolean("Active")&&record.hasUUID("Actor");
        if(resumed&&record.hasUUID("Carrier"))return;
        record.putBoolean("Cold",false);record.putBoolean("Active",true);
        int slot=resumed&&record.contains("Slot")?record.getInt("Slot"):chooseSlot(player,base,record);record.putInt("Slot",slot);
        LakeWitchEntity girl=actor(player);
        if(girl==null&&!resumed){
            girl=DrownedTownRegistry.LAKE_WITCH.get().create(player.serverLevel());if(girl==null)return;
            girl.recollection(player.getUUID(),base);var at=spot(base,slot);girl.moveTo(at.x,at.y,at.z,READER_YAW,0);
            if(!player.serverLevel().addFreshEntity(girl))return;record.putUUID("Actor",girl.getUUID());
        }
        // A visit saved before 0.4.101 had nobody at her feet; he joins it now.
        if(!record.hasUUID("Carrier")){
            var boy=DrownedTownRegistry.CONGREGANT.get().create(player.serverLevel());if(boy==null)return;
            boy.pose(false,false);boy.memoryBoy(true);boy.boyLook(1);boy.carrierOwner(player.getUUID());boy.addTag(CARRIER);boy.carry(HOLD);
            var at=carrierStance(base,slot);boy.moveTo(at.x,at.y,at.z,CARRIER_YAW,0);
            if(!player.serverLevel().addFreshEntity(boy))return;record.putUUID("Carrier",boy.getUUID());
        }
        save(data,player.getUUID(),record);
        IndianLakeArchitecture.light(player.serverLevel(),base.offset(2,3,-8),8);IndianLakeArchitecture.light(player.serverLevel(),base.offset(0,2,-13),6);
    }

    /** A reader on the bank whose own recollection is under way and not yet thrown, for a newcomer to join. */
    private static @Nullable ServerPlayer hostHere(ServerPlayer newcomer){
        var data=LabyrinthData.get(newcomer.server);
        for(var q:newcomer.serverLevel().players()){
            if(q==newcomer||!q.isAlive()||q.isSpectator()||!IndianLakeRooms.inside(q,LabyrinthPlace.SHALLOWS))continue;
            var r=personal(data,q.getUUID());if(!r.getBoolean("Active")||r.hasUUID(WITH)||r.getBoolean("Cold")||IndianLakeProgress.hasThrown(data,q.getUUID()))continue;
            var girl=actor(q);if(girl!=null&&girl.memoryPhase()<=CARRIED)return q;
        }
        return null;
    }
    /** The first to join takes her feet, in Lonnie's place; anyone after that stands on the bank with the boys. */
    private static void join(ServerPlayer player,ServerPlayer host){
        var data=LabyrinthData.get(player.server);var hostRecord=personal(data,host.getUUID());boolean feet=feetHere(host)==null;
        var mine=personal(data,player.getUUID());mine.putBoolean("Active",true);mine.putBoolean("Cold",false);mine.putUUID(WITH,host.getUUID());
        mine.putString(ROLE,feet?AT_FEET:ON_BANK);mine.putInt("Slot",hostRecord.getInt("Slot"));mine.remove("Actor");mine.remove("Carrier");save(data,player.getUUID(),mine);
        String name=player.getGameProfile().getName();var girl=actor(host);
        if(feet){
            hostRecord.putUUID(FEET,player.getUUID());save(data,host.getUUID(),hostRecord);
            say(host,LONNIE,name+", you get her feet. I'll count.",CALM,girl);
            player.displayClientMessage(Component.literal("Once "+host.getGameProfile().getName()+" has her arms, take her feet.").withStyle(ChatFormatting.GRAY),true);
        }else{
            say(host,BUD,name+"! Come watch. She's going in.",JEER,girl);
            player.displayClientMessage(Component.literal("You stand on the bank with the boys.").withStyle(ChatFormatting.GRAY),true);
        }
    }

    // ------------------------------------------------------------------------------------------------ the carry
    public static boolean lift(ServerPlayer player,LakeWitchEntity girl){
        LabyrinthData data=LabyrinthData.get(player.server);CompoundTag record=personal(data,player.getUUID());
        // 0.4.157: the reader who joined to hold her feet takes them once the other has her arms.
        if(girl.memoryOwner()!=null&&!player.getUUID().equals(girl.memoryOwner())){
            return girl.memoryOwner().equals(with(data,player.getUUID()))&&AT_FEET.equals(record.getString(ROLE))&&liftFeet(player,girl);
        }
        if(!IndianLakeProgress.canDealShallows(data,player.getUUID())||!IndianLakeRooms.inside(player,LabyrinthPlace.SHALLOWS)
                ||!player.getUUID().equals(girl.memoryOwner())||player.distanceToSqr(girl)>12||girl.memoryPhase()!=LYING
                ||record.getBoolean("Cold")||!record.getBoolean("Active"))return false;
        if(!player.getMainHandItem().isEmpty()||!player.getOffhandItem().isEmpty()){
            say(player,LONNIE,"Both hands. Put what you're holding away.",CALM,girl);return false;
        }
        LakeCongregantEntity boy=carrier(player);BlockPos base=girl.memoryBase();if(boy==null||base==null)return false;
        Scene s=scene(girl);s.slot=Math.max(0,Math.min(SLOTS.length-1,record.getInt("Slot")));
        ItemStack burden=new ItemStack(DrownedTownRegistry.SHALLOWS_BURDEN.get());
        CustomData.update(DataComponents.CUSTOM_DATA,burden,t->{t.putUUID(OWNER,player.getUUID());t.putUUID(ACTOR,girl.getUUID());});
        player.setItemInHand(InteractionHand.MAIN_HAND,burden);player.inventoryMenu.broadcastChanges();
        // The reader takes her wrists at the west end of her; the boy already has her feet.
        var at=stance(base,s.slot);player.teleportTo(player.serverLevel(),at.x,at.y,at.z,READER_YAW,12);
        // Server-side players without a real connection take the place directly.
        if(player.position().distanceToSqr(at)>1e-4||Math.abs(player.getYRot()-READER_YAW)>.5F)player.moveTo(at.x,at.y,at.z,READER_YAW,12);
        girl.memoryPhase(CARRIED);s.stroke=0;s.queued=0;s.forward=0;s.angle=s.from=s.to=0;s.idle=0;s.released=false;s.strokeAge=s.strokeLength=STROKE_TICKS;
        s.feet=null;s.wristsWant=s.feetWant=0;
        long now=player.serverLevel().getGameTime();s.due.clear();
        var partner=feetHere(player);
        if(partner!=null){say(player,LONNIE,partner.getGameProfile().getName()+", her feet. Then we swing her on three.",CALM,girl);hint(player);return true;}
        boy.carry(SWINGING);boy.swing(0);
        say(player,LONNIE,"Got her? We swing her on three.",CALM,girl);
        s.later(now+25,new Line(CLAY,"Out past the reeds!",CHEER));s.later(now+50,new Line(STACEY,"No. No, no...",CALM));
        hint(player);return true;
    }
    /** The reader at her feet faces the other way, so the lake is on their right. */
    private static void hint(ServerPlayer player){
        boolean feet=with(LabyrinthData.get(player.server),player.getUUID())!=null;
        player.displayClientMessage(Component.empty().append(Component.keybind(feet?"key.right":"key.left")).append(" swing toward the water  ·  ")
                .append(Component.keybind(feet?"key.left":"key.right")).append(" swing back  ·  ").append(Component.keybind("key.sneak")).append(" let go").withStyle(ChatFormatting.GRAY),true);
    }
    /** The reader joining to hold her feet takes them, at the east end of her, facing the reader who holds her arms. */
    private static boolean liftFeet(ServerPlayer player,LakeWitchEntity girl){
        BlockPos base=girl.memoryBase();var host=owner(player.serverLevel(),girl.memoryOwner());
        if(base==null||host==null||!IndianLakeRooms.inside(player,LabyrinthPlace.SHALLOWS)||player.distanceToSqr(girl)>12)return false;
        Scene s=scene(girl);
        if(girl.memoryPhase()!=CARRIED){if(girl.memoryPhase()==LYING)say(host,LONNIE,"Wait till "+host.getGameProfile().getName()+" has her arms.",CALM,girl);return false;}
        if(s.feet!=null)return false;
        if(!player.getMainHandItem().isEmpty()||!player.getOffhandItem().isEmpty()){say(host,LONNIE,"Both hands, "+player.getGameProfile().getName()+".",CALM,girl);return false;}
        ItemStack burden=new ItemStack(DrownedTownRegistry.SHALLOWS_BURDEN.get());
        CustomData.update(DataComponents.CUSTOM_DATA,burden,t->{t.putUUID(OWNER,player.getUUID());t.putUUID(ACTOR,girl.getUUID());});
        player.setItemInHand(InteractionHand.MAIN_HAND,burden);player.inventoryMenu.broadcastChanges();
        var at=carrierStance(base,s.slot);player.teleportTo(player.serverLevel(),at.x,at.y,at.z,CARRIER_YAW,12);
        if(player.position().distanceToSqr(at)>1e-4||Math.abs(player.getYRot()-CARRIER_YAW)>.5F)player.moveTo(at.x,at.y,at.z,CARRIER_YAW,12);
        s.feet=player.getUUID();s.wristsWant=s.feetWant=0;s.idle=0;
        say(host,LONNIE,"Got her? We swing her on three. Together.",CALM,girl);
        long now=player.serverLevel().getGameTime();s.later(now+25,new Line(CLAY,"Out past the reeds!",CHEER));s.later(now+50,new Line(STACEY,"No. No, no...",CALM));
        hint(player);hint(host);return true;
    }
    /**
     * The reader's strafe keys: the lake is on their left, so left swings her out and right brings her back. The reader at
     * her feet faces them, so for them it is the other way round, and a stroke needs both (0.4.157).
     */
    public static boolean swing(ServerPlayer player,int side){
        LakeWitchEntity girl=memory(player);if(girl==null||girl.memoryPhase()!=CARRIED||!hasBurden(player)||side==0)return false;
        var host=host(player);boolean feet=host!=player;int direction=feet?(side>0?1:-1):(side<0?1:-1);
        Scene s=scene(girl);
        if(!feet&&s.feet==null&&feetHere(host)!=null){
            if(--s.wrong<0){say(host,LONNIE,"Wait for "+feetHere(host).getGameProfile().getName()+" to get her feet.",CALM,girl);s.wrong=4;}
            return false;
        }
        if(s.feet==null)return stroke(host,girl,direction);
        long now=girl.level().getGameTime();
        if(feet){s.feetWant=direction;s.feetAt=now;}else{s.wristsWant=direction;s.wristsAt=now;}
        if(s.wristsWant==0||s.feetWant==0)return false;
        boolean same=s.wristsWant==s.feetWant;int together=s.wristsWant;s.wristsWant=s.feetWant=0;
        if(!same){say(host,LONNIE,"Together. Out on my count, back after.",CALM,girl);return false;}
        return stroke(host,girl,together);
    }
    private static boolean stroke(ServerPlayer player,LakeWitchEntity girl,int direction){
        Scene s=scene(girl);if(s.released)return false;
        boolean ready=s.stroke==0||s.strokeAge>=s.strokeLength*READY;
        if(direction==s.stroke){
            if(--s.wrong<0){say(player,LONNIE,"Other way. With me.",CALM,girl);s.wrong=4;}
            return false;
        }
        if(!ready){s.queued=direction;return false;}
        s.queued=0;s.from=s.angle;s.stroke=direction;s.strokeAge=0;s.idle=0;
        long now=player.serverLevel().getGameTime();
        if(direction>0){
            int count=++s.forward;s.to=FORWARD[Math.min(2,count-1)];s.strokeLength=count==1?STROKE_TICKS+2:STROKE_TICKS;
            say(player,LONNIE,count==1?"One!":count==2?"Two!":"Three!",CALM,girl);
            if(count==1)s.later(now+8,new Line(BUD,"Fly, witch!",JEER));
            if(count==2)s.later(now+8,new Line(HANK,"Say a spell, Stacey!",JEER));
        }else{
            s.to=-BACK[Math.min(1,Math.max(0,s.forward-1))];s.strokeLength=STROKE_TICKS;
            if(s.forward==1)s.later(now+2,new Line(STACEY,"Don't...",CALM));
            if(s.forward==2)s.later(now+3,new Line(CLAY,"Higher!",CHEER));
        }
        hear(player,DrownedTownRegistry.SHALLOWS_SWING.get(),girl.position(),.55F+.15F*Math.min(3,s.forward),.9F+player.getRandom().nextFloat()*.15F);
        return true;
    }
    private static void carry(ServerPlayer owner,LakeWitchEntity girl,Scene s,BlockPos base,@Nullable LakeCongregantEntity boy){
        if(!hasBurden(owner)){drop(owner,girl,"You let go of her wrists.");return;}
        var stand=stance(base,s.slot);
        if(owner.isShiftKeyDown()){drop(owner,girl,null);return;}
        if(owner.position().subtract(stand).horizontalDistanceSqr()>.8*.8){drop(owner,girl,"She slips out of your hands.");return;}
        // 0.4.157: the reader at her feet holds on the same terms; if they leave, Lonnie takes her feet again.
        var partner=feetHere(owner);
        if(s.feet!=null){
            var feet=partner!=null&&partner.getUUID().equals(s.feet)?partner:null;
            if(feet==null||!hasBurden(feet)||feet.isShiftKeyDown()||feet.position().subtract(carrierStance(base,s.slot)).horizontalDistanceSqr()>.8*.8){
                drop(owner,girl,(feet==null?"Her feet drop":feet.getGameProfile().getName()+" lets go of her feet")+".");return;
            }
            long now=girl.level().getGameTime();if(now-s.wristsAt>TOGETHER)s.wristsWant=0;if(now-s.feetAt>TOGETHER)s.feetWant=0;
        }
        lonnie(owner,girl,s,base,boy,partner!=null);
        if(s.queued!=0&&(s.stroke==0||s.strokeAge>=s.strokeLength*READY)){int q=s.queued;s.queued=0;stroke(owner,girl,q);}
        if(s.stroke!=0&&s.strokeAge<s.strokeLength){
            s.strokeAge++;float k=(1-Mth.cos(Mth.PI*s.strokeAge/s.strokeLength))/2;s.angle=s.from+(s.to-s.from)*k;
            // On three both boys let go in the middle of the stroke, while she is still rising toward the water.
            if(s.stroke>0&&s.forward>=3&&s.strokeAge>=s.strokeLength*RELEASE){release(owner,girl,s,base,boy);return;}
        }else if(++s.idle>50&&(s.forward>0||Math.abs(s.angle)>.01F)){
            if(s.idle==51){s.forward=0;s.stroke=0;say(owner,LONNIE,"Don't stop. Start again.",CALM,girl);}
            s.angle*=.88F;if(Math.abs(s.angle)<.01F)s.angle=0;
        }
        var body=swung(base,s.slot,s.angle);girl.moveTo(body.x,body.y,body.z,READER_YAW,0);girl.setYHeadRot(READER_YAW);girl.setYBodyRot(READER_YAW);girl.setDeltaMovement(Vec3.ZERO);
        girl.swing(s.angle);if(boy!=null&&partner==null){boy.carry(SWINGING);boy.swing(s.angle);}
        if(girl.memoryTicks%40==0){hint(owner);if(s.feet!=null&&partner!=null)hint(partner);}
    }
    /** Lonnie holds her feet unless a reader does; then he stands at the water's edge to count, facing her. */
    private static void lonnie(ServerPlayer owner,LakeWitchEntity girl,Scene s,BlockPos base,@Nullable LakeCongregantEntity boy,boolean partner){
        if(boy==null)return;var at=partner?edge(base,s.slot):carrierStance(base,s.slot);
        if(boy.position().distanceToSqr(at)>.01){boy.moveTo(at.x,at.y,at.z,CARRIER_YAW,0);boy.setDeltaMovement(Vec3.ZERO);}
        if(partner){if(boy.carry()!=0)boy.carry(0);boy.swing(0);float yaw=(float)(Mth.atan2(-(girl.getX()-boy.getX()),girl.getZ()-boy.getZ())*Mth.RAD_TO_DEG);boy.setYRot(yaw);boy.setYHeadRot(yaw);boy.setYBodyRot(yaw);}
        else if(girl.memoryPhase()==LYING&&boy.carry()!=HOLD)boy.carry(HOLD);
    }
    /** Both let go: she leaves the swing with its own motion and the heave behind it. */
    private static void release(ServerPlayer owner,LakeWitchEntity girl,Scene s,BlockPos base,@Nullable LakeCongregantEntity boy){
        s.released=true;removeBurdens(owner);s.thrownWith=s.feet;
        var feet=s.feet==null?null:owner(owner.serverLevel(),s.feet);if(feet!=null)removeBurdens(feet);
        float rate=(s.to-s.from)*Mth.PI/(2*s.strokeLength)*Mth.sin(Mth.PI*s.strokeAge/s.strokeLength);
        double tangent=SWING_RADIUS*rate;
        s.velocity=new Vec3(0,Math.sin(s.angle)*tangent+.24,-Math.cos(s.angle)*tangent-.34);
        girl.memoryPhase(FLYING);girl.swing(0);
        if(boy!=null&&s.feet==null){boy.carry(RELEASED);boy.swing(s.angle);}
        hear(owner,DrownedTownRegistry.SHALLOWS_SWING.get(),girl.position(),1.1F,.7F);
        for(int who:new int[]{BUD,HANK,CLAY})react(owner,who,CHEER,30,girl);
        say(owner,BUD,"There she goes!",CHEER,girl);
    }
    /** She falls as anything thrown falls, and stops at the first thing under her: the lake's surface, not its water. */
    private static void fly(ServerPlayer owner,LakeWitchEntity girl,Scene s,BlockPos base){
        var level=(ServerLevel)girl.level();Vec3 at=girl.position(),next=at.add(s.velocity);
        s.velocity=s.velocity.add(0,-.08,0).scale(.98);
        double floor=floor(level,base,next);
        if(next.y<=floor||girl.memoryTicks>80){
            double k=at.y-next.y>1e-6?Mth.clamp((at.y-floor)/(at.y-next.y),0,1):1;var hit=at.lerp(next,k);
            float yaw=(float)(Mth.atan2(-s.velocity.x,s.velocity.z)*Mth.RAD_TO_DEG)+35;
            girl.moveTo(hit.x,floor,hit.z,yaw,0);girl.setYHeadRot(yaw);girl.setYBodyRot(yaw);impact(owner,girl);return;
        }
        float yaw=girl.getYRot();girl.moveTo(next.x,next.y,next.z,yaw,0);girl.setDeltaMovement(Vec3.ZERO);
    }
    /** The water's surface where there is water under her, otherwise the top of the ground. */
    static double floor(ServerLevel level,BlockPos base,Vec3 at){
        for(int y=Mth.floor(at.y)+1;y>=base.getY()-4;y--){
            var cell=BlockPos.containing(at.x,y,at.z);
            if(level.getFluidState(cell).is(FluidTags.WATER))return y+level.getFluidState(cell).getHeight(level,cell);
            var shape=level.getBlockState(cell).getCollisionShape(level,cell);
            if(!shape.isEmpty())return y+shape.max(Direction.Axis.Y);
        }
        return base.getY()-1;
    }
    private static void impact(ServerPlayer owner,LakeWitchEntity girl){
        var level=(ServerLevel)girl.level();girl.memoryPhase(BROKEN);
        hear(owner,DrownedTownRegistry.SHALLOWS_BREAK.get(),girl.position(),1.4F,1);
        var everyone=group(owner);
        for(var p:everyone){
            level.sendParticles(p,ParticleTypes.SPLASH,true,girl.getX(),girl.getY()+.1,girl.getZ(),36,.7,.02,.7,.1);
            level.sendParticles(p,ParticleTypes.FALLING_WATER,true,girl.getX(),girl.getY()+.3,girl.getZ(),12,.5,.25,.5,0);
        }
        // 0.4.157: who held her and who watched, kept with each of them for their own Witness line.
        var data=LabyrinthData.get(level.getServer());var feet=scene(girl).thrownWith==null?null:owner(level,scene(girl).thrownWith);
        String thrower=owner.getGameProfile().getName(),feetName=feet==null?"":feet.getGameProfile().getName();
        for(var p:everyone){
            var r=personal(data,p.getUUID());var t=new CompoundTag();
            t.putString(ROLE,p==owner?AT_WRISTS:p==feet?AT_FEET:ON_BANK);t.putString("Thrower",thrower);t.putString("Feet",feetName);
            r.put(THROW,t);save(data,p.getUUID(),r);
        }
        if(feet!=null&&!everyone.contains(feet)){var r=personal(data,feet.getUUID());var t=new CompoundTag();t.putString(ROLE,AT_FEET);t.putString("Thrower",thrower);t.putString("Feet",feetName);r.put(THROW,t);save(data,feet.getUUID(),r);everyone.add(feet);}
        IndianLakeProgress.completedShallows(data,owner.getUUID(),thrower);
        WitnessAccount.resolve(owner,WitnessAccount.Story.SHALLOWS,"threw_her");
        updateEssays(owner);
        for(var p:everyone){
            if(p==owner)continue;boolean held=p==feet;
            IndianLakeProgress.joinedShallows(data,p.getUUID(),p.getGameProfile().getName());
            WitnessAccount.resolveAs(p,WitnessAccount.Story.SHALLOWS,held?"held_her_feet":"watched_from_the_bank",held?VignetteRole.ACTOR:VignetteRole.WITNESS,thrower);
            updateEssays(p);
        }
        for(int who:new int[]{BUD,HANK,CLAY,LONNIE})react(owner,who,FROZEN,150,girl);
    }
    /** Put her back down on the bank, the boy still holding her feet; the count starts again. */
    private static void drop(ServerPlayer owner,LakeWitchEntity girl,@Nullable String why){
        removeBurdens(owner);BlockPos base=girl.memoryBase();if(base==null)return;Scene s=scene(girl);
        var feet=s.feet==null?null:owner(owner.serverLevel(),s.feet);if(feet!=null)removeBurdens(feet);s.feet=null;s.wristsWant=s.feetWant=0;
        lay(girl,base,s.slot);var boy=carrier(owner);if(boy!=null&&feetHere(owner)==null){boy.carry(HOLD);boy.swing(0);}
        s.stroke=0;s.queued=0;s.forward=0;s.angle=0;s.released=false;s.line=OPENING.length;s.next=owner.serverLevel().getGameTime()+300;
        hear(owner,SoundEvents.PLAYER_SMALL_FALL,girl.position(),.8F,.8F);
        if(why!=null)owner.displayClientMessage(Component.literal(why).withStyle(ChatFormatting.GRAY),true);
        say(owner,LONNIE,"What are you doing? Pick her up.",CALM,girl);s.later(owner.serverLevel().getGameTime()+20,new Line(BUD,"Chicken.",JEER));
    }
    private static void lay(LakeWitchEntity girl,BlockPos base,int slot){
        girl.memoryPhase(LYING);var at=spot(base,slot);girl.moveTo(at.x,at.y,at.z,READER_YAW,0);girl.setYHeadRot(READER_YAW);girl.setYBodyRot(READER_YAW);
        girl.setDeltaMovement(Vec3.ZERO);girl.swing(0);
    }

    // ------------------------------------------------------------------------------------------------ the burden
    public static boolean validBurden(ServerPlayer player,ItemStack stack){
        if(!stack.is(DrownedTownRegistry.SHALLOWS_BURDEN.get()))return false;
        CustomData custom=stack.get(DataComponents.CUSTOM_DATA);if(custom==null)return false;CompoundTag t=custom.copyTag();
        LakeWitchEntity girl=memory(player);
        return t.hasUUID(OWNER)&&player.getUUID().equals(t.getUUID(OWNER))&&t.hasUUID(ACTOR)&&girl!=null
                &&girl.getUUID().equals(t.getUUID(ACTOR))&&girl.memoryPhase()==CARRIED
                &&IndianLakeRooms.inside(player,LabyrinthPlace.SHALLOWS)
                &&!personal(LabyrinthData.get(player.server),girl.memoryOwner()).getBoolean("Cold");
    }
    private static boolean hasBurden(ServerPlayer player){
        for(int i=0;i<player.getInventory().getContainerSize();i++)if(validBurden(player,player.getInventory().getItem(i)))return true;
        return false;
    }
    public static void removeBurdens(ServerPlayer player){
        for(int i=0;i<player.getInventory().getContainerSize();i++)if(player.getInventory().getItem(i).is(DrownedTownRegistry.SHALLOWS_BURDEN.get()))player.getInventory().setItem(i,ItemStack.EMPTY);
        if(player.containerMenu.getCarried().is(DrownedTownRegistry.SHALLOWS_BURDEN.get()))player.containerMenu.setCarried(ItemStack.EMPTY);
        player.inventoryMenu.broadcastChanges();
    }

    // ------------------------------------------------------------------------------------------------ ticking
    /** Called from the actor's own tick: the carry, the flight and everything after. */
    public static void tickActor(LakeWitchEntity girl){
        if(!(girl.level() instanceof ServerLevel level)||girl.memoryOwner()==null||girl.memoryBase()==null)return;
        LabyrinthData data=LabyrinthData.get(level.getServer());girl.memoryTicks++;
        ServerPlayer owner=owner(level,girl.memoryOwner());
        if(IndianLakeProgress.hasThrown(data,girl.memoryOwner())){
            // A finished body plays out even if its owner disconnects during the aftermath; nothing earlier survives.
            if(girl.memoryPhase()<BROKEN){discard(girl);return;}
            aftermath(level,girl,owner!=null&&IndianLakeRooms.inside(owner,LabyrinthPlace.SHALLOWS)?owner:null);return;
        }
        CompoundTag live=personal(data,girl.memoryOwner());
        if(!live.getBoolean("Active")||!live.hasUUID("Actor")||!girl.getUUID().equals(live.getUUID("Actor"))){discard(girl);return;}
        BlockPos base=girl.memoryBase();Scene s=scene(girl);
        LakeCongregantEntity boy=owner==null?null:carrier(owner);
        if(owner==null||!IndianLakeRooms.inside(owner,LabyrinthPlace.SHALLOWS)){
            // Interrupted recollections are retryable, and never count a throw after their owner leaves.
            if(girl.memoryPhase()!=LYING||s.released){s.released=false;s.stroke=0;s.forward=0;s.angle=0;}
            lay(girl,base,s.slot);return;
        }
        long now=level.getGameTime();
        while(!s.due.isEmpty()&&s.due.peek().at()<=now){var line=s.due.poll().line();say(owner,line.who(),line.text(),line.mood(),girl);}
        switch(girl.memoryPhase()){
            case CARRIED->carry(owner,girl,s,base,boy);
            case FLYING->fly(owner,girl,s,base);
            case LYING->{
                lay(girl,base,s.slot);lonnie(owner,girl,s,base,boy,feetHere(owner)!=null);if(boy!=null)boy.swing(0);
                if(s.next==0)s.next=now+10;
                if(now>=s.next){
                    var line=s.line<OPENING.length?OPENING[s.line]:NUDGES[(s.line-OPENING.length)%NUDGES.length];
                    say(owner,line.who(),line.text(),line.mood(),girl);s.line++;s.next=now+(s.line<OPENING.length?55:s.line==OPENING.length?160:300);
                }
            }
            // The landing records the throw, so a later phase here can only be a reload between the two.
            default->aftermath(level,girl,owner);
        }
    }
    /** The broken body, the getting up and the crossing; then the boys on the bank. Driven by saved phase time. */
    private static void aftermath(ServerLevel level,LakeWitchEntity girl,@Nullable ServerPlayer owner){
        int t=girl.memoryTicks;BlockPos base=girl.memoryBase();if(base==null)return;
        girl.setDeltaMovement(Vec3.ZERO);
        if(owner!=null){var boy=carrier(owner);if(boy!=null){
            if(boy.carry()==RELEASED&&(girl.memoryPhase()>BROKEN||t>16))boy.carry(0);
            float yaw=(float)(Mth.atan2(-(girl.getX()-boy.getX()),girl.getZ()-boy.getZ())*Mth.RAD_TO_DEG);boy.setYRot(yaw);boy.setYHeadRot(yaw);boy.setYBodyRot(yaw);
        }}
        switch(girl.memoryPhase()){
            case BROKEN->{
                if(owner!=null){
                    if(t==36)say(owner,CLAY,"She didn't go under.",WHISPER,girl);
                    if(t==60)say(owner,HANK,"Stacey?",FROZEN,girl);
                }
                if(t>=STILL_TICKS)girl.memoryPhase(RISING);
            }
            case RISING->{
                if(owner!=null){
                    if(t==2||t==9||t==17||t==26||t==35)hear(owner,DrownedTownRegistry.SHALLOWS_RISE.get(),girl.position(),1,.85F+.06F*(t%4));
                    if(t==18)for(int who:new int[]{HANK,CLAY,LONNIE})react(owner,who,RECOIL,120,girl);
                    if(t==22)say(owner,BUD,"Her arm's on backwards.",RECOIL,girl);
                }
                if(t>=RISE_TICKS)girl.memoryPhase(SCURRY);
            }
            case SCURRY->{
                // Straight for the far woods, weaving, over the water as if it were ground and then up the bank.
                double x=girl.getX()+.09*Math.sin(t*.42),z=girl.getZ()-SCURRY_SPEED;
                double y=floor(level,base,new Vec3(x,girl.getY()+2,z));
                float yaw=(float)(Mth.atan2(-(x-girl.getX()),z-girl.getZ())*Mth.RAD_TO_DEG);
                girl.moveTo(x,y,z,yaw,0);girl.setYHeadRot(yaw);girl.setYBodyRot(yaw);
                if(owner!=null){
                    if(t%4==1)hear(owner,DrownedTownRegistry.SHALLOWS_SCURRY.get(),girl.position(),.9F,.9F+owner.getRandom().nextFloat()*.2F);
                    if(t==1)say(owner,HANK,"Jesus.",RECOIL,girl);
                    if(t==24)say(owner,CLAY,"She's running on it.",RECOIL,girl);
                }
                if(z<=base.getZ()-37.5||t>=SCURRY_TICKS)girl.memoryPhase(GONE);
            }
            case GONE->{
                if(owner!=null){
                    if(t==30)say(owner,BUD,"Nobody saw that.",WHISPER,girl);
                    if(t==85)say(owner,HANK,"Witches float. She floated, that's all.",CALM,girl);
                    if(t==145)say(owner,LONNIE,"That was all of us.",CALM,girl);
                }
                if(t>=GONE_TICKS)discard(girl);
            }
            default->{}
        }
    }
    /** The boy at her feet leaves with his reader's visit; a stale one from an old visit cannot return. */
    public static void tickCarrier(LakeCongregantEntity boy){
        if(!(boy.level() instanceof ServerLevel level)||boy.carrierOwner()==null)return;
        CompoundTag record=personal(LabyrinthData.get(level.getServer()),boy.carrierOwner());
        if(!record.getBoolean("Active")||!record.hasUUID("Carrier")||!boy.getUUID().equals(record.getUUID("Carrier")))boy.discard();
    }

    // ------------------------------------------------------------------------------------------------ voices
    private static @Nullable Entity speaker(ServerPlayer player,int who,@Nullable LakeWitchEntity girl){
        return who==STACEY?girl:who==LONNIE?carrier(player):onlooker(player.serverLevel(),who);
    }
    /** A line said to this reader alone, over the one who says it; 0.4.157: to everyone in their recollection. */
    static void say(ServerPlayer player,int who,String line,int mood,@Nullable LakeWitchEntity girl){
        var host=host(player);Entity speaker=speaker(host,who,girl);
        for(var p:group(host)){
            p.displayClientMessage(Component.literal(WHO[who]+": “"+line+"”"),false);
            HousePackets.send(p,new ShallowsCuePayload(speaker==null?-1:speaker.getId(),line,mood,Math.max(50,line.length()*3+30)));
        }
    }
    /** A reaction without words, seen by this reader alone, and those with them. */
    static void react(ServerPlayer player,int who,int mood,int ticks,@Nullable LakeWitchEntity girl){
        var host=host(player);Entity speaker=speaker(host,who,girl);if(speaker!=null)for(var p:group(host))HousePackets.send(p,new ShallowsCuePayload(speaker.getId(),"",mood,ticks));
    }
    /** A sound only this reader hears, and those with them. */
    static void hear(ServerPlayer player,SoundEvent sound,Vec3 at,float volume,float pitch){
        for(var p:group(host(player)))p.connection.send(new ClientboundSoundPacket(BuiltInRegistries.SOUND_EVENT.wrapAsHolder(sound),SoundSource.NEUTRAL,at.x,at.y,at.z,volume,pitch,p.getRandom().nextLong()));
    }
    private static void updateEssays(ServerPlayer player){
        for(int i=0;i<player.getInventory().getContainerSize();i++){
            ItemStack stack=player.getInventory().getItem(i);if(stack.getItem() instanceof DrownedEssayItem essay)essay.refresh(player,stack);
        }
        player.inventoryMenu.broadcastChanges();
    }

    // ------------------------------------------------------------------------------------------------ leaving, refusing
    public static void onDepart(ServerPlayer player){
        LabyrinthData data=LabyrinthData.get(player.server);CompoundTag record=personal(data,player.getUUID());
        // 0.4.157: a reader who joined leaves the recollection they joined, and its girl stays theirs who began it.
        if(record.hasUUID(WITH)){
            var host=owner(player.serverLevel(),record.getUUID(WITH));removeBurdens(player);
            if(host!=null){var hostRecord=personal(data,host.getUUID());if(hostRecord.hasUUID(FEET)&&hostRecord.getUUID(FEET).equals(player.getUUID())){hostRecord.remove(FEET);save(data,host.getUUID(),hostRecord);}}
            record.putBoolean("Active",false);record.remove(WITH);record.remove(ROLE);save(data,player.getUUID(),record);return;
        }
        LakeWitchEntity girl=actor(player);if(girl!=null)discard(girl);LakeCongregantEntity boy=carrier(player);if(boy!=null)boy.discard();removeBurdens(player);
        record.putBoolean("Active",false);record.remove("Actor");record.remove("Carrier");record.remove(FEET);save(data,player.getUUID(),record);
    }
    public static void onToss(ItemTossEvent event){
        if(!(event.getPlayer() instanceof ServerPlayer player)||!event.getEntity().getItem().is(DrownedTownRegistry.SHALLOWS_BURDEN.get()))return;
        event.setCanceled(true);event.getEntity().discard();LakeWitchEntity girl=actor(player);
        if(girl!=null&&girl.memoryPhase()==CARRIED)drop(player,girl,null);
        removeBurdens(player);
    }
    public static void onAttack(AttackEntityEvent event){
        if(!(event.getEntity() instanceof ServerPlayer player)||!IndianLakeRooms.inside(player,LabyrinthPlace.SHALLOWS))return;
        var target=event.getTarget();
        if(!(target instanceof LakeWitchEntity w&&w.memory())&&!target.getTags().contains(BOY)&&!target.getTags().contains(CARRIER))return;
        event.setCanceled(true);LabyrinthData data=LabyrinthData.get(player.server);ServerPlayer striker=player;
        // 0.4.157: a blow from someone who joined darkens the recollection they joined.
        var host=host(player);if(host!=player){player=host;}
        CompoundTag record=personal(data,player.getUUID());
        // A finished throw is not undone by a blow afterward.
        if(IndianLakeProgress.hasThrown(data,player.getUUID()))return;
        boolean first=!record.getBoolean("Cold");record.putBoolean("Cold",true);if(first)record.putBoolean(BOYS_LEFT,true);save(data,player.getUUID(),record);
        // 0.4.191: the failure takes the boys' company. They go home: gone from the view of whoever struck and whoever's visit it was.
        if(first)boysLeave(striker,player);
        LakeWitchEntity girl=actor(player);if(girl!=null)discard(girl);LakeCongregantEntity boy=carrier(player);if(boy!=null)boy.discard();removeBurdens(player);
        BlockPos base=IndianLakeRooms.base(player.server,LabyrinthPlace.SHALLOWS);IndianLakeArchitecture.light(player.serverLevel(),base.offset(2,3,-8),0);IndianLakeArchitecture.light(player.serverLevel(),base.offset(0,2,-13),0);
    }
    /** Set when a blow before the throw sent the boys home; the reader's account remembers it. */
    public static final String BOYS_LEFT="BoysLeft0191";
    /** The boys stop, pick up their things and go home: the bank boys leave these readers' view until they come back. */
    static void boysLeave(ServerPlayer... readers){
        var seen=new java.util.HashSet<ServerPlayer>(java.util.Arrays.asList(readers));
        for(var reader:seen){
            var ids=new java.util.ArrayList<Integer>();
            for(int i=0;i<3;i++){var boy=onlooker(reader.serverLevel(),i);if(boy!=null)ids.add(boy.getId());}
            if(!ids.isEmpty())reader.connection.send(new net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket(ids.stream().mapToInt(Integer::intValue).toArray()));
            reader.displayClientMessage(Component.literal("The boys stop laughing. They pick up their things and go home without you.").withStyle(net.minecraft.ChatFormatting.GRAY,net.minecraft.ChatFormatting.ITALIC),false);
        }
    }
    /** Old saved words on the bank (before 0.4.101 the scene spoke through floating text) still expire. */
    public static void words(ServerPlayer player,BlockPos at,String text){
        var display=EntityType.TEXT_DISPLAY.create(player.serverLevel());if(display==null)return;
        CompoundTag tag=new CompoundTag();display.saveWithoutId(tag);tag.putString("text",Component.Serializer.toJson(Component.literal(text),player.registryAccess()));
        tag.putString("billboard","center");tag.putInt("line_width",210);tag.putInt("background",0x44000000);tag.putBoolean("see_through",false);display.load(tag);
        display.moveTo(Vec3.atCenterOf(at));display.addTag(WORDS);display.getPersistentData().putLong("Until",player.serverLevel().getGameTime()+120);player.serverLevel().addFreshEntity(display);
    }
    public static void onServerTick(ServerTickEvent.Post event){
        if(event.getServer().getTickCount()%20!=0)return;ServerLevel level=event.getServer().getLevel(HouseDimensions.OUTSIDE);if(level==null)return;
        BlockPos base=IndianLakeRooms.base(event.getServer(),LabyrinthPlace.SHALLOWS);
        if(base!=null){
            var visitors=IndianLakeRooms.visitors(level,base,LabyrinthPlace.SHALLOWS);
            if(!visitors.isEmpty()){IndianLakeRooms.keepLoaded(level,base,LabyrinthPlace.SHALLOWS);boys(level,base);}
            Set<ServerPlayer> players=new HashSet<>(event.getServer().getPlayerList().getPlayers());players.addAll(level.players());
            for(ServerPlayer player:players){
                var record=personal(LabyrinthData.get(player.server),player.getUUID());
                if(!IndianLakeRooms.inside(player,LabyrinthPlace.SHALLOWS)&&record.getBoolean("Active"))onDepart(player);
                // A reader who joined has nothing left to be part of once the one who began it has gone.
                else if(record.getBoolean("Active")&&record.hasUUID(WITH)&&!personal(LabyrinthData.get(player.server),record.getUUID(WITH)).getBoolean("Active"))onDepart(player);
                for(int i=0;i<player.getInventory().getContainerSize();i++)if(player.getInventory().getItem(i).is(DrownedTownRegistry.SHALLOWS_BURDEN.get())&&!validBurden(player,player.getInventory().getItem(i)))player.getInventory().setItem(i,ItemStack.EMPTY);
            }
        }
        expireWords(level);
    }
    /** Discard only after traversing the native live lookup; removal can shift its backing map. */
    public static void expireWords(ServerLevel level){
        List<Entity> expired=new ArrayList<>();
        for(Entity entity:level.getAllEntities()){
            if(entity.getTags().contains(WORDS)&&entity.getPersistentData().getLong("Until")<=level.getGameTime())expired.add(entity);
        }
        for(Entity entity:expired)entity.discard();
    }
    public static boolean reset(ServerPlayer player){onDepart(player);LabyrinthData data=LabyrinthData.get(player.server);CompoundTag state=data.state("indian_lake"),boys=state.getCompound("Boys");boys.remove(player.getUUID().toString());state.put("Boys",boys);data.setState("indian_lake",state);save(data,player.getUUID(),new CompoundTag());return true;}

    // ------------------------------------------------------------------------------------------------ the Witness line
    /**
     * 0.4.157: each reader's Witness line says how they were part of the throw: her wrists, her feet, or the bank. A reader
     * who threw her with Lonnie keeps the line as it was.
     */
    public static String witness(CompoundTag record,String text){
        String line=thrown(record,text);
        // 0.4.191: a visit darkened by a blow sent the boys home, and the account keeps it.
        return record.getBoolean(BOYS_LEFT)?line+" Once, before that, I struck her, and the boys went home without me.":line;
    }
    private static String thrown(CompoundTag record,String text){
        var t=record.getCompound(THROW);if(t.isEmpty())return text;String thrower=t.getString("Thrower"),feet=t.getString("Feet");
        return switch(t.getString(ROLE)){
            case AT_FEET->"I held her feet while "+thrower+" held her wrists, and we swung her out over the water on three. The lake would not take her. The school pages remembered my name among the boys on the bank.";
            case ON_BANK->"I stood on the bank with the boys while "+thrower+" and "+(feet.isEmpty()?"Lonnie":feet)+" swung her out over the water on three. Nobody stopped them, and I didn't either. The lake would not take her. The school pages remembered my name among the boys on the bank.";
            default->feet.isEmpty()?text:"I held her wrists while "+feet+" held her feet, and we swung her out over the water on three. The lake would not take her. The school pages remembered my name among the boys on the bank.";
        };
    }

    // ------------------------------------------------------------------------------------------------ for tests
    /** The carry's current swing angle and forward count, as the server holds them. */
    public static float swingAngle(LakeWitchEntity girl){return scene(girl).angle;}
    public static int count(LakeWitchEntity girl){return scene(girl).forward;}
    /** True once the current stroke is far enough along for the next key to start the next one. */
    public static boolean readyForStroke(LakeWitchEntity girl){var s=scene(girl);return s.stroke==0||s.strokeAge>=s.strokeLength*READY;}
}
