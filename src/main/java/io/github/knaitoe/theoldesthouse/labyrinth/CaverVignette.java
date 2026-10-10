package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.*;
import io.github.knaitoe.theoldesthouse.opening.CompanionOrders;
import io.github.knaitoe.theoldesthouse.network.CaverCrawlPayload;
import io.github.knaitoe.theoldesthouse.network.HousePackets;
import java.util.*;
import javax.annotation.Nullable;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.*;
import net.minecraft.sounds.*;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.*;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.*;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.joml.Vector3f;

/**
 * Inspired by Ted the Caver: the work earns a passage, and the passage earns a return.
 *
 * 0.4.71: the crack is packed rubble worked a block at a time; the cave breathes, out toward the camp and back in, on one
 * clock shared by everyone inside and paused while it is empty; the smooth stone only gives on the in-breath; a reader
 * may tie a line off at the ladder, which pays out behind them, shows them the way back and holds against the draw of
 * the in-breath on the way out; the low chamber is crouch-high and the wait in it is long.
 */
public final class CaverVignette {
    public static final String ID="ted_caver",BOOK_OWNER="CaverJournalOwner";
    /** Five packed blocks, five strokes each. */
    public static final int PER_BLOCK=5,STROKES=CaverCave.RUBBLE*PER_BLOCK,STROKE_INTERVAL=25,CRAWL_TICKS=40,DEEP_TICKS=500;
    /** One breath: out for five seconds, still for one, in for five, still for one. */
    public static final int BREATH=240,EXHALE_END=100,INHALE_START=120,INHALE_END=220;
    /** The draught's push on a crawling body, per tick, toward the camp (+) or back into the cave (-). */
    public static final float OUT=.008F,OUT_PURSUED=.012F,IN=-.006F,IN_PURSUED=-.022F,IN_HELD=-.006F;
    private static final int LINE_POINTS=96;
    private static final DustParticleOptions LINE=new DustParticleOptions(new Vector3f(.86F,.82F,.7F),.6F);
    private record Crawl(ServerPlayer player,@Nullable Pose forced,Pose displayed){}
    private static final Map<UUID,Crawl> CRAWLING=new HashMap<>();
    private record Excavation(ServerPlayer miner,ServerLevel level,BlockPos base,BlockPos at,int index){}
    private static final Map<MinecraftServer,Excavation> EXCAVATIONS=new HashMap<>();
    /** Readers currently counted as inside, so nobody outside the cave pays for reading the saved record every tick. */
    private static final Set<UUID> ACTIVE=new HashSet<>();
    /** The shared breath clock, saved every second. */
    private static final Map<MinecraftServer,Integer> BREATHS=new HashMap<>();
    private CaverVignette(){}
    public static CompoundTag personal(LabyrinthData d,UUID id){return d.state(ID).getCompound("Players").getCompound(id.toString()).copy();}
    private static void save(LabyrinthData d,UUID id,CompoundTag own){var all=d.state(ID);var people=all.getCompound("Players");people.put(id.toString(),own);all.put("Players",people);d.setState(ID,all);}
    public static boolean canDeal(LabyrinthData d,UUID id){return !WitnessAccount.has(d,id,WitnessAccount.Story.TED_CAVER)&&!personal(d,id).getBoolean("Escaped");}
    public static @Nullable BlockPos base(MinecraftServer s){return IndianLakeRooms.base(s,LabyrinthPlace.TED_CAVER);}
    public static boolean inside(ServerPlayer p){return p.gameMode.getGameModeForPlayer()!=GameType.SPECTATOR&&IndianLakeRooms.inside(p,LabyrinthPlace.TED_CAVER);}
    private static Vec3 rel(ServerPlayer p,BlockPos b){return p.position().subtract(b.getX(),b.getY(),b.getZ());}
    private static boolean reach(ServerPlayer p,BlockPos at){return p.distanceToSqr(at.getCenter())<=25;}
    private static boolean editable(ServerPlayer p,BlockPos at){
        // Native placement/mining packets already enforce Minecraft's eye-based interaction range.
        if(!p.isAlive()||!inside(p))return false;
        var r=at.subtract(base(p.server));var room=LabyrinthPlace.TED_CAVER.room();
        return r.getX()>room.minX()&&r.getX()<room.maxX()&&r.getY()>room.minY()&&r.getY()<room.maxY()
                &&r.getZ()>room.minZ()&&r.getZ()<room.maxZ();
    }
    private static boolean torch(BlockState state){return state.is(Blocks.TORCH)||state.is(Blocks.WALL_TORCH)
            ||state.is(Blocks.SOUL_TORCH)||state.is(Blocks.SOUL_WALL_TORCH);}
    public static boolean allowsPlacing(ServerPlayer p,BlockPos at,BlockState state){return editable(p,at)&&torch(state);}
    /**
     * The House lets nothing be broken; here a reader may mine the front of the packed run with a pickaxe, and take back a
     * torch they set themselves. Never the cave shell, the props, or someone else's torch.
     */
    public static boolean mayBreak(ServerPlayer p,BlockPos at){
        if(!editable(p,at))return false;var l=p.serverLevel();var state=l.getBlockState(at);
        if(torch(state))return ownsTorch(p,at);int front=CaverCave.front(l,base(p.server));
        return front>=0&&CaverCave.isRubble(state)&&at.equals(base(p.server).offset(CaverCave.rubbleCell(front)))&&p.getMainHandItem().is(ItemTags.PICKAXES);
    }
    private static final String TORCHES="Torches";
    /** Who set the torch standing here, if anyone has been recorded. */
    public static @Nullable UUID torchOwner(LabyrinthData d,BlockPos at){var torches=d.state(ID).getCompound(TORCHES);String key=Long.toString(at.asLong());return torches.hasUUID(key)?torches.getUUID(key):null;}
    public static void forgetTorch(LabyrinthData d,BlockPos at){var state=d.state(ID);var torches=state.getCompound(TORCHES);String key=Long.toString(at.asLong());
        if(!torches.contains(key))return;torches.remove(key);state.put(TORCHES,torches);d.setState(ID,state);}
    /** A torch set before ownership was recorded (an older cave's) can be taken by whoever is there. */
    private static boolean ownsTorch(ServerPlayer p,BlockPos at){var owner=torchOwner(LabyrinthData.get(p.server),at);return owner==null||owner.equals(p.getUUID());}
    /** Runs after every protection: a torch that really stands is recorded as its setter's. */
    public static void onPlace(BlockEvent.EntityPlaceEvent e){
        if(e.isCanceled()||!(e.getEntity() instanceof ServerPlayer p)||!allowsPlacing(p,e.getPos(),e.getPlacedBlock()))return;
        var d=LabyrinthData.get(p.server);var state=d.state(ID);var torches=state.getCompound(TORCHES);
        torches.putUUID(Long.toString(e.getPos().asLong()),p.getUUID());state.put(TORCHES,torches);d.setState(ID,state);
    }
    /** The break event precedes native removal. Record work only after that block of rubble has really gone. */
    public static void onBreak(BlockEvent.BreakEvent e){
        if(e.isCanceled()||!(e.getPlayer() instanceof ServerPlayer p)||!mayBreak(p,e.getPos()))return;
        if(torch(e.getState())){forgetTorch(LabyrinthData.get(p.server),e.getPos());return;}
        if(!CaverCave.isRubble(e.getState()))return;
        var b=base(p.server);int index=CaverCave.front(p.serverLevel(),b);
        EXCAVATIONS.put(p.server,new Excavation(p,p.serverLevel(),b,e.getPos().immutable(),index));
    }
    private static void confirmExcavation(MinecraftServer server){
        var dig=EXCAVATIONS.remove(server);if(dig==null||!dig.base().equals(base(server))
                ||!dig.level().hasChunkAt(dig.at())||!dig.level().getBlockState(dig.at()).isAir())return;
        var d=LabyrinthData.get(server);var state=d.state(ID);
        state.putInt("Work",CaverCave.front(dig.level(),dig.base())<0?STROKES:Math.max(state.getInt("Work"),(dig.index()+1)*PER_BLOCK));state.remove("NextStroke");d.setState(ID,state);
        var p=dig.miner();var own=personal(d,p.getUUID());own.putBoolean("Worked",true);save(d,p.getUUID(),own);updateJournal(p);
        progress(p,dig.index());
    }
    /** What a reader is told when a block of the run comes away. */
    private static void progress(ServerPlayer p,int index){
        int front=CaverCave.front(p.serverLevel(),base(p.server));
        if(front<0)p.displayClientMessage(Component.literal("The last of it falls away. The opening will take your shoulders; crouch at its mouth to crawl through."),false);
        else if(index==0)p.displayClientMessage(Component.literal("The first of the rubble comes away. More is packed behind it. Crouch at the mouth to crawl in and keep working."),false);
        else p.displayClientMessage(Component.literal("More rubble comes away. The draught is colder."),true);
    }
    private static void pickaxeHint(ServerPlayer p){
        boolean cached=p.serverLevel().getBlockEntity(base(p.server).offset(CaverCave.CACHE)) instanceof Container box&&box.hasAnyMatching(s->s.is(ItemTags.PICKAXES));
        p.displayClientMessage(Component.literal(cached?"The packed rubble needs a pickaxe. There is one in the camp barrel.":"The packed rubble needs a pickaxe."),true);
    }
    public static void onArrive(ServerPlayer p,LabyrinthPlace place){if(place==LabyrinthPlace.TED_CAVER)enter(p);else echo(p);}
    public static void enter(ServerPlayer p){
        if(!inside(p))return;var b=base(p.server);
        if(ACTIVE.contains(p.getUUID())){if(p.tickCount%200==0)IndianLakeRooms.keepLoaded(p.serverLevel(),b,LabyrinthPlace.TED_CAVER);return;}
        ACTIVE.add(p.getUUID());var d=LabyrinthData.get(p.server);var own=personal(d,p.getUUID());
        CaverCave.repairEntrance(p.serverLevel(),b);
        if(!own.getBoolean("Active")){
            if(rel(p,b).z> -8.5&&!own.getBoolean("Escaped")){own.putBoolean("Pursuit",false);own.putInt("ReturnCrawl",0);own.putInt("DeepTicks",0);}
            own.putBoolean("Active",true);own.putBoolean("Arrived",true);own.putLong("LastTick",-1);save(d,p.getUUID(),own);
            p.displayClientMessage(Component.literal("A field notebook lies by the supplies. The rope leads down."),false);
        }
        IndianLakeRooms.keepLoaded(p.serverLevel(),b,LabyrinthPlace.TED_CAVER);
    }
    /** A stroke is a reachable interaction with a real pickaxe on the front of the run, separated by working time. */
    public static boolean chip(ServerPlayer p,BlockPos at){
        if(!inside(p)||!reach(p,at))return false;var l=p.serverLevel();var b=base(p.server);int front=CaverCave.front(l,b);
        if(front<0||!at.equals(b.offset(CaverCave.rubbleCell(front))))return false;
        if(!p.getMainHandItem().is(ItemTags.PICKAXES)){pickaxeHint(p);return true;}
        var d=LabyrinthData.get(p.server);var state=d.state(ID);int work=Math.max(state.getInt("Work"),front*PER_BLOCK);
        long now=l.getGameTime();if(state.contains("NextStroke")&&now<state.getLong("NextStroke"))return true;
        state.putInt("Work",++work);state.putLong("NextStroke",now+STROKE_INTERVAL);
        boolean removed=work>=(front+1)*PER_BLOCK;
        if(removed){l.destroyBlock(at,false);if(CaverCave.front(l,b)<0)state.putInt("Work",STROKES);}
        d.setState(ID,state);
        var own=personal(d,p.getUUID());own.putBoolean("Worked",true);save(d,p.getUUID(),own);updateJournal(p);
        l.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK,Blocks.TUFF.defaultBlockState()),at.getX()+.5,at.getY()+.4,at.getZ()+1,8,.15,.18,.05,.02);
        l.playSound(null,at,SoundEvents.STONE_BREAK,SoundSource.BLOCKS,.55F,.7F);
        if(removed)progress(p,front);
        else if(work%PER_BLOCK==3)p.displayClientMessage(Component.literal("Grit runs out of the crack with every blow."),true);
        return true;
    }
    public static boolean examine(ServerPlayer p,BlockPos at){
        if(!inside(p)||!reach(p,at))return false;var b=base(p.server);var d=LabyrinthData.get(p.server);var own=personal(d,p.getUUID());
        if(at.equals(b.offset(CaverCave.MARK))){
            if(!own.getBoolean("Squeezed")){p.displayClientMessage(Component.literal("You haven't followed the passage here."),true);return true;}
            if(!own.getBoolean("MarkRead")){
                own.putBoolean("MarkRead",true);save(d,p.getUUID(),own);updateJournal(p);
                p.displayClientMessage(Component.literal("The cuts go beneath the mineral crust. One looks almost like a shoulder."),false);
                privateSound(p,LabyrinthRegistry.CAVER_CHISEL,Vec3.atCenterOf(b.offset(5,-3,-48)),.4F,.85F);
            }else p.displayClientMessage(Component.literal("Cuts under the crust. The smooth stone opposite does not match the wall."),true);
            return true;
        }
        var local=at.subtract(b);
        if(local.getZ()==-43&&local.getX()>=4&&local.getX()<=6&&local.getY()>=-3&&local.getY()<=-2){
            if(!own.getBoolean("MarkRead")){p.displayClientMessage(Component.literal("Marks on the opposite wall catch what little light there is."),true);return true;}
            var state=d.state(ID);
            if(own.getBoolean("StoneSeen")){p.displayClientMessage(Component.literal("The passage behind the stone is low."),true);return true;}
            if(!state.getBoolean("StoneMoved")){
                // It sits in its seat while the cave breathes out, and gives only while it draws in.
                int breath=breathing(p.server);
                if(breath>0){p.displayClientMessage(Component.literal("The stone is pressed hard into its seat. Air hisses round its edges."),true);return true;}
                if(breath==0){p.displayClientMessage(Component.literal("The stone will not move. The air around it has gone still."),true);return true;}
                state.putBoolean("StoneMoved",true);d.setState(ID,state);CaverCave.stone(p.serverLevel(),b,true);
                p.serverLevel().playSound(null,b.offset(CaverCave.STONE),LabyrinthRegistry.CAVER_STONE.value(),SoundSource.BLOCKS,1F,.9F);
                p.displayClientMessage(Component.literal("On the in-breath the stone gives under your hand and rolls inward. There is a low passage behind it."),false);
            }else p.displayClientMessage(Component.literal("The smooth stone has been rolled aside. The passage behind it is low."),false);
            own=personal(d,p.getUUID());own.putBoolean("StoneSeen",true);save(d,p.getUUID(),own);updateJournal(p);
            return true;
        }return false;
    }
    /** The safety line is tied off at the chain beside the ladder, or the ladder itself. */
    public static boolean anchor(BlockPos b,BlockPos at){
        var r=at.subtract(b);
        return r.getX()==CaverCave.CHAIN.getX()&&r.getZ()==CaverCave.CHAIN.getZ()&&r.getY()>=-3&&r.getY()<=1
                ||r.getX()==CaverCave.LADDER.getX()&&r.getZ()==CaverCave.LADDER.getZ()&&r.getY()>=-3&&r.getY()<=0;
    }
    /** Ties one of the reader's own strings off; it is spent there, and the line it makes is theirs alone. */
    public static boolean tie(ServerPlayer p,BlockPos at){
        if(!inside(p)||!reach(p,at)||!anchor(base(p.server),at)||!p.getMainHandItem().is(Items.STRING))return false;
        var d=LabyrinthData.get(p.server);var own=personal(d,p.getUUID());
        if(own.getBoolean("Escaped")){p.displayClientMessage(Component.literal("The line you left is still tied here."),true);return true;}
        if(own.getBoolean("LineTied")){p.displayClientMessage(Component.literal("Your line is already tied off here."),true);return true;}
        own.putBoolean("LineTied",true);own.putLongArray("LinePath",new long[]{at.asLong()});save(d,p.getUUID(),own);
        if(!p.getAbilities().instabuild)p.getMainHandItem().shrink(1);
        p.displayClientMessage(Component.literal("You tie the line off beside the ladder. It pays out behind you."),false);
        return true;
    }
    private static boolean squeeze(Vec3 r){return r.x>-.15&&r.x<1.15&&r.z< -21&&r.z> -35.9&&r.y>=-3.2&&r.y< -2.1;}
    private static boolean lowChamber(Vec3 r){return r.x>1.9&&r.x<8&&r.z< -50.5&&r.z> -58&&r.y>=-3.2&&r.y< -1.4;}
    private static void crawl(ServerPlayer p,boolean active,float push){
        var old=CRAWLING.get(p.getUUID());
        if(active){
            if(old!=null&&old.player()!=p){restore(old);CRAWLING.remove(p.getUUID());old=null;}
            if(old==null){CRAWLING.put(p.getUUID(),new Crawl(p,p.getForcedPose(),p.getPose()));p.setForcedPose(Pose.SWIMMING);p.setPose(Pose.SWIMMING);p.refreshDimensions();sendCrawl(p,true,push);}
        }else if(old!=null){CRAWLING.remove(p.getUUID());restore(old);}
    }
    private static void restore(Crawl old){
        var p=old.player();sendCrawl(p,false,0);if(p.getForcedPose()!=Pose.SWIMMING)return;
        p.setForcedPose(old.forced());p.setPose(old.forced()==null?old.displayed():old.forced());p.refreshDimensions();
    }
    public static boolean crawling(ServerPlayer p){var own=CRAWLING.get(p.getUUID());return own!=null&&own.player()==p;}
    public static void depart(ServerPlayer p){crawl(p,false,0);ACTIVE.remove(p.getUUID());var d=LabyrinthData.get(p.server);var own=personal(d,p.getUUID());
        if(own.getBoolean("Active")){own.putBoolean("Active",false);save(d,p.getUUID(),own);}}
    public static void clearAll(){for(var server:List.copyOf(EXCAVATIONS.keySet()))confirmExcavation(server);for(var old:CRAWLING.values())restore(old);CRAWLING.clear();ACTIVE.clear();
        for(var server:List.copyOf(BREATHS.keySet()))saveBreath(server);BREATHS.clear();}
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent e){if(e.getEntity() instanceof ServerPlayer p)depart(p);}
    /** A reader who was inside when the server last stopped, and wakes elsewhere, is no longer inside. */
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent e){if(e.getEntity() instanceof ServerPlayer p&&!inside(p)&&personal(LabyrinthData.get(p.server),p.getUUID()).getBoolean("Active"))depart(p);}
    public static void onDeath(LivingDeathEvent e){if(e.getEntity() instanceof ServerPlayer p){
        var d=LabyrinthData.get(p.server);var own=personal(d,p.getUUID());
        if(own.getBoolean("Active")&&!own.getBoolean("Escaped")){own.putBoolean("Pursuit",false);own.putInt("ReturnCrawl",0);own.putInt("DeepTicks",0);save(d,p.getUUID(),own);}
        depart(p);
    }}
    private static void sendCrawl(ServerPlayer p,boolean active,float push){var b=base(p.server);if(b!=null)HousePackets.send(p,new CaverCrawlPayload(active,b.getX(),b.getY(),b.getZ(),push));}

    // ------------------------------------------------------------------------------------------------ the breath
    public static int breathTick(MinecraftServer s){return BREATHS.computeIfAbsent(s,k->Math.floorMod(LabyrinthData.get(k).state(ID).getInt("Breath"),BREATH));}
    /** Sets the shared clock, for tests and operators; the cave keeps breathing from there. */
    public static void setBreath(MinecraftServer s,int tick){BREATHS.put(s,Math.floorMod(tick,BREATH));saveBreath(s);}
    private static void saveBreath(MinecraftServer s){var d=LabyrinthData.get(s);var state=d.state(ID);state.putInt("Breath",breathTick(s));d.setState(ID,state);}
    /** +1 while the cave breathes out toward the camp, -1 while it draws back in, 0 between. */
    public static int breathing(MinecraftServer s){int t=breathTick(s);return t<EXHALE_END?1:t<INHALE_START?0:t<INHALE_END?-1:0;}
    /** The draught a crawling reader feels: stronger on the way out, and held off by their own line. */
    public static float push(CompoundTag own,int breathing){
        boolean pursued=own.getBoolean("Pursuit")&&!own.getBoolean("Escaped");
        if(breathing>0)return pursued?OUT_PURSUED:OUT;
        if(breathing<0)return pursued?(own.getBoolean("LineTied")?IN_HELD:IN_PURSUED):IN;
        return 0;
    }
    /** One step of the shared clock, taken only while someone is in the cave; its sound and drift are for everyone there. */
    private static void breathe(ServerLevel l,BlockPos b){
        var s=l.getServer();int t=(breathTick(s)+1)%BREATH;BREATHS.put(s,t);if(t%20==0)saveBreath(s);
        var mouth=Vec3.atCenterOf(b.offset(CaverCave.APERTURE));boolean moved=l.getBlockState(b.offset(CaverCave.STONE)).isAir();
        var seam=new Vec3(b.getX()+5,b.getY()-2.5,b.getZ()-41.6);
        if(t==0||t==INHALE_START){var sound=t==0?LabyrinthRegistry.CAVER_EXHALE:LabyrinthRegistry.CAVER_INHALE;
            l.playSound(null,mouth.x,mouth.y,mouth.z+.6,sound.value(),SoundSource.AMBIENT,.9F,1F);
            l.playSound(null,seam.x,seam.y,seam.z,sound.value(),SoundSource.AMBIENT,.6F,.85F);}
        int dir=breathing(s);if(dir==0||t%4!=0)return;var random=l.getRandom();
        drift(l,mouth.x,mouth.y-.25,mouth.z+.6,dir);
        if(CaverCave.mouthOpen(l,b)){var at=b.offset(0,-3,-23-random.nextInt(12));if(l.getBlockState(at).isAir())drift(l,at.getX()+.5,at.getY()+.3,at.getZ()+.5,dir);}
        // The stone breathes through its seams; once it has rolled aside, through the passage behind it.
        if(!moved)for(var edge:new double[][]{{4.03,-2.2},{5.97,-2.6},{5,-.99},{4.6,-.99}})drift(l,b.getX()+edge[0],b.getY()+edge[1],b.getZ()-41.97,dir);
        else drift(l,b.getX()+4.5+random.nextDouble(),b.getY()-2.4,b.getZ()-43.5,dir);
    }
    private static void drift(ServerLevel l,double x,double y,double z,int dir){l.sendParticles(ParticleTypes.SMOKE,x,y,z,0,0,.02,dir,.045);}

    public static void onServerTick(ServerTickEvent.Post e){
        var s=e.getServer();confirmExcavation(s);
        var b=base(s);var cave=s.getLevel(NovelRooms.dimension(LabyrinthPlace.TED_CAVER));
        if(b!=null&&cave!=null){boolean occupied=false;for(var p:cave.players())if(p.isAlive()&&inside(p)){occupied=true;break;}if(occupied)breathe(cave,b);}
        for(var l:s.getAllLevels())for(var p:List.copyOf(l.players()))playerTick(p);
    }
    public static void playerTick(ServerPlayer p){
        if(!inside(p)){if(crawling(p)||ACTIVE.contains(p.getUUID()))depart(p);return;}
        enter(p);var b=base(p.server);var d=LabyrinthData.get(p.server);var own=personal(d,p.getUUID());var r=rel(p,b);
        long now=p.serverLevel().getGameTime();if(own.getLong("LastTick")==now)return;own.putLong("LastTick",now);
        int breath=breathing(p.server);float push=push(own,breath);
        boolean tight=CaverCave.mouthOpen(p.serverLevel(),b)&&squeeze(r)&&(p.isShiftKeyDown()||crawling(p)||r.z<=-23&&r.z>=-34);
        crawl(p,tight,push);
        if(tight&&(now%10==0||breath!=own.getInt("LastBreath")))sendCrawl(p,true,push);
        own.putInt("LastBreath",breath);
        if(tight&&r.z<=-23&&r.z>=-34){
            String counter=own.getBoolean("Pursuit")?"ReturnCrawl":"IngressCrawl";
            own.putInt(counter,Math.min(CRAWL_TICKS,own.getInt(counter)+1));
        }
        boolean changed=false;
        if(own.getInt("IngressCrawl")>=CRAWL_TICKS&&r.z< -34.1&&!own.getBoolean("Squeezed")){
            own.putBoolean("Squeezed",true);changed=true;p.displayClientMessage(Component.literal("The chamber is larger than the draught suggested. There are cuts in the left wall."),false);
        }
        line(p,own,r,now);
        if(own.getBoolean("StoneSeen")&&!own.getBoolean("Pursuit")&&lowChamber(r)){
            int deep=own.getInt("DeepTicks")+1;own.putInt("DeepTicks",deep);
            // The wait is long, and it gets louder behind you.
            if(deep==100)p.serverLevel().sendParticles(p,new BlockParticleOption(ParticleTypes.FALLING_DUST,Blocks.TUFF.defaultBlockState()),true,p.getX(),p.getY()+1.35,p.getZ(),12,.6,.05,.6,0);
            if(deep==200)privateSound(p,LabyrinthRegistry.CAVER_SCRAPE,Vec3.atCenterOf(b.offset(4,-3,-45)),.45F,.8F);
            if(deep==340){privateSound(p,LabyrinthRegistry.CAVER_SCRAPE,Vec3.atCenterOf(b.offset(5,-3,-48)),.8F,.7F);
                if(own.getBoolean("LineTied"))privateSound(p,LabyrinthRegistry.CAVER_LINE,p.position(),.35F,1.2F);}
            if(deep>=DEEP_TICKS){own.putBoolean("Pursuit",true);own.putInt("ReturnCrawl",0);own.putInt("PursuitTicks",0);changed=true;
                if(own.getBoolean("LineTied")){own.putInt("LineFlash",100);privateSound(p,LabyrinthRegistry.CAVER_LINE,p.position(),1F,.9F);
                    p.displayClientMessage(Component.literal("Air moves past your face. Then the line draws tight toward the way you came."),false);}
                else{privateSound(p,LabyrinthRegistry.CAVER_EXHALE,p.position().add(0,1,-3),1F,.6F);
                    p.displayClientMessage(Component.literal("Air moves past your face. Nothing holds you to the way back."),false);}
            }
        }
        if(own.getBoolean("Pursuit")&&!own.getBoolean("Escaped")){
            int clock=own.getInt("PursuitTicks")+1;own.putInt("PursuitTicks",clock);
            if(r.z< -9&&clock%160==0)privateSound(p,LabyrinthRegistry.CAVER_SCRAPE,p.position().add(0,.5,-6),.9F,.5F);
            if(own.getInt("ReturnCrawl")>=CRAWL_TICKS&&r.z> -8.5&&r.y>=-.2&&r.y<3&&Math.abs(r.x-.5)<3){
                own.putBoolean("Escaped",true);changed=true;crawl(p,false,0);
                save(d,p.getUUID(),own);WitnessAccount.resolve(p,WitnessAccount.Story.TED_CAVER,"retraced_the_squeeze");
                p.displayClientMessage(Component.literal("You are above the rope. The mouth of the cave is still where you left it."),false);
            }
        }
        save(d,p.getUUID(),own);if(changed)updateJournal(p);
    }
    /** The reader's own line: it pays out as they go, and only they see it, a pale cord along the floor back to the ladder. */
    private static void line(ServerPlayer p,CompoundTag own,Vec3 r,long now){
        if(!own.getBoolean("LineTied")||own.getBoolean("Escaped"))return;
        long[] path=own.getLongArray("LinePath");if(path.length==0)return;
        var here=p.blockPosition();
        if(r.z< -9.5&&path.length<LINE_POINTS&&BlockPos.of(path[path.length-1]).distSqr(here)>=2.25){
            path=Arrays.copyOf(path,path.length+1);path[path.length-1]=here.asLong();own.putLongArray("LinePath",path);}
        int flash=own.getInt("LineFlash");if(flash>0)own.putInt("LineFlash",flash-1);
        if(now%(flash>0?5:20)!=0)return;
        var l=p.serverLevel();var me=p.position();
        for(int i=1;i<path.length;i++){Vec3 a=Vec3.atBottomCenterOf(BlockPos.of(path[i-1])),c=Vec3.atBottomCenterOf(BlockPos.of(path[i]));
            if(a.distanceToSqr(me)>324&&c.distanceToSqr(me)>324)continue;
            for(int k=0;k<3;k++){var at=a.lerp(c,k/3.0);l.sendParticles(p,LINE,true,at.x,at.y+.12,at.z,1,0,0,0,0);}}
    }
    /** Two later echoes, heard on later trips through the House. Never reopens or changes ending credit. */
    private static void echo(ServerPlayer p){
        if(!p.isAlive()||p.isSpectator()||FinaleProgress.terminal(FinaleProgress.phase(p.server,p.getUUID())))return;
        var d=LabyrinthData.get(p.server);var own=personal(d,p.getUUID());
        if(!own.getBoolean("Escaped")||own.getInt("EchoTicks")>=480||own.getInt("EchoArrivals")>=7)return;
        int n=own.getInt("EchoArrivals")+1;own.putInt("EchoArrivals",n);save(d,p.getUUID(),own);
        if(n==3||n==7)privateSound(p,LabyrinthRegistry.CAVER_CHISEL,p.position().add(p.getLookAngle().multiply(-4,0,-4)).add(0,.5,0),n==3?.3F:.4F,.9F);
        if(n==7)updateJournal(p);
    }
    private static void privateSound(ServerPlayer p,Holder<SoundEvent> sound,Vec3 at,float volume,float pitch){
        p.connection.send(new ClientboundSoundPacket(sound,SoundSource.AMBIENT,at.x,at.y,at.z,volume,pitch,p.getRandom().nextLong()));
    }
    public static boolean companionScene(ServerPlayer p){return inside(p)&&rel(p,base(p.server)).z< -8;}
    /** Refuse the shaft without changing native identity, owner, health, sit state, or saved order. */
    public static boolean companion(TamableAnimal pet,ServerPlayer p){
        if(!companionScene(p)||pet.level()!=p.level()||pet.isLeashed()||pet.isPassenger()||pet.isOrderedToSit()||CompanionOrders.order(pet)==CompanionOrders.Order.STAY
                ||!p.getUUID().equals(CompanionOrders.owner(pet)))return false;
        var b=base(p.server);if(!IndianLakeRooms.bounds(b,LabyrinthPlace.TED_CAVER).inflate(3).contains(pet.position()))return false;
        pet.getNavigation().stop();var at=new Vec3(b.getX()+.5,b.getY(),b.getZ()-7.5);
        if(pet.getZ()<b.getZ()-8.2||pet.distanceToSqr(at)>25){pet.teleportTo(at.x,at.y,at.z);pet.setDeltaMovement(Vec3.ZERO);pet.resetFallDistance();}
        else if(pet.distanceToSqr(at)>2)pet.getNavigation().moveTo(at.x,at.y,at.z,.65);
        pet.getLookControl().setLookAt(b.getX()+.5,b.getY()-2,b.getZ()-11.5,30,30);return true;
    }
    public static boolean ownedJournal(ItemStack stack,UUID id){var custom=stack.get(DataComponents.CUSTOM_DATA);
        return custom!=null&&custom.copyTag().hasUUID(BOOK_OWNER)&&id.equals(custom.copyTag().getUUID(BOOK_OWNER));}
    public static ItemStack journal(LabyrinthData d,UUID id){
        var own=personal(d,id);var pages=new ArrayList<Component>();
        String[] entries={
            "FIELD NOTEBOOK\n\nR. / Miles\n\nCheck lamps. Leave the line tied. Three sandwiches, two gloves. Miles says a spare glove is a strange thing to count.",
            "FIRST SURVEY\n\nDown the ladder, follow the draught. Rubble is packed into the crack; a pickaxe takes it out a block at a time. I left spare torches in the barrel. Crouch at the opening to crawl. Tie the line off at the ladder.",
            "WORKING DAY\n\nI wait for the grit after each blow. A hand would fit behind the crack. The air comes out in bursts.\n\nMiles has stopped counting.",
            "THROUGH\n\nThe squeeze goes farther than the light. I kept my arms in front of me. There was space at the end to stand. I heard my own clothing stop scraping before I stopped moving.",
            "THE CUTS\n\nA shape lies under the crust. I tried drawing it. Each version looks like a different part of a person.\n\nA smooth stone sits opposite. Air comes from behind it, out and then back in.",
            "ANOTHER PASSAGE\n\nThe smooth stone moved away from my hand. Behind it, the line can go on. I have already made the hole large enough to get back.\n\nThat seemed like a good reason to keep going.",
            "LOW CHAMBER\n\nThe rope tightened. Nothing was tied to its end. Stone scraped behind me.\n\nBack through the squeeze. Up the ladder beside the line. Get above the drop.",
            "OUT\n\nI crawled back. The pulls came from behind me. Above the ladder I could stand again.\n\nI did not see Miles at the landing. I cannot remember when I stopped expecting him to answer.",
            "LATER\n\nA chisel sounds behind a shut door. Grit lies in this book. I have packed twice. It is wrong to leave the line there.\n\n[The next date. Nothing follows it.]"};
        int last=Math.max(own.getInt("JournalStage"),journalStage(own));
        for(int i=0;i<=last;i++)pages.add(HouseWriting.page(HouseWriting.WritingStyle.WILL,entries[i]));
        var book=VignetteYields.mark(HouseWriting.book("Field notebook","R.",pages),ID);
        CustomData.update(DataComponents.CUSTOM_DATA,book,tag->tag.putUUID(BOOK_OWNER,id));return book;
    }
    private static int journalStage(CompoundTag own){return own.getInt("EchoTicks")>=480||own.getInt("EchoArrivals")>=7?8:own.getBoolean("Escaped")?7:own.getBoolean("Pursuit")?6:own.getBoolean("StoneSeen")?5:own.getBoolean("MarkRead")?4:own.getBoolean("Squeezed")?3:own.getBoolean("Worked")?2:1;}
    private static void updateJournal(ServerPlayer p){var d=LabyrinthData.get(p.server);var own=personal(d,p.getUUID());own.putInt("JournalStage",Math.max(own.getInt("JournalStage"),journalStage(own)));save(d,p.getUUID(),own);var book=journal(d,p.getUUID());
        for(int i=0;i<p.getInventory().getContainerSize();i++)if(ownedJournal(p.getInventory().getItem(i),p.getUUID()))p.getInventory().setItem(i,book.copy());
        if(ownedJournal(p.containerMenu.getCarried(),p.getUUID()))p.containerMenu.setCarried(book.copy());p.inventoryMenu.broadcastChanges();}
    public static final class JournalMenu extends LecternMenu {
        private final ServerPlayer reader;private final BlockPos at;private final SimpleContainer pages;
        public JournalMenu(int id,ServerPlayer reader,BlockPos at){this(id,reader,at,new SimpleContainer(1));}
        private JournalMenu(int id,ServerPlayer reader,BlockPos at,SimpleContainer pages){super(id,pages,new SimpleContainerData(1));this.reader=reader;this.at=at.immutable();this.pages=pages;pages.setItem(0,journal(LabyrinthData.get(reader.server),reader.getUUID()));}
        public ItemStack book(){return pages.getItem(0).copy();}
        @Override public boolean stillValid(net.minecraft.world.entity.player.Player p){return p==reader&&inside(reader)&&reach(reader,at)&&reader.level().getBlockState(at).is(Blocks.LECTERN);}
        @Override public boolean clickMenuButton(net.minecraft.world.entity.player.Player p,int button){
            if(!stillValid(p))return false;int count=book().get(DataComponents.WRITTEN_BOOK_CONTENT).pages().size();
            if(button==3){var d=LabyrinthData.get(reader.server);var own=personal(d,reader.getUUID());if(own.getBoolean("JournalTaken"))return false;
                own.putBoolean("JournalTaken",true);save(d,reader.getUUID(),own);var book=journal(d,reader.getUUID());
                if(!reader.getInventory().add(book))reader.drop(book,false);reader.inventoryMenu.broadcastChanges();return true;}
            if(button>=100){if(button-100>=count)return false;}else if(button==1){if(getPage()<=0)return false;}else if(button==2){if(getPage()>=count-1)return false;}else return false;
            return super.clickMenuButton(p,button);
        }
    }
    public static void onRightClick(PlayerInteractEvent.RightClickBlock e){
        if(e.getEntity() instanceof ServerPlayer observer&&observer.gameMode.getGameModeForPlayer()==GameType.SPECTATOR
                &&observer.level().dimension().equals(HouseDimensions.INTERIOR)&&base(observer.server)!=null
                &&IndianLakeRooms.bounds(base(observer.server),LabyrinthPlace.TED_CAVER).contains(observer.position())
                &&e.getPos().equals(base(observer.server).offset(CaverCave.JOURNAL))){e.setCanceled(true);return;}
        if(!(e.getEntity() instanceof ServerPlayer p)||!inside(p)||!reach(p,e.getPos()))return;
        var b=base(p.server);boolean journal=e.getPos().equals(b.offset(CaverCave.JOURNAL));
        // The marks, the stone and the front of the rubble answer whatever is in the reader's hands. The client also tries
        // the off hand after an empty main hand; that try must not set a torch on the prop that was just examined.
        if(e.getHand()!=InteractionHand.MAIN_HAND){if(journal||prop(p,e.getPos())){e.setCanceled(true);e.setCancellationResult(InteractionResult.FAIL);resync(p);}return;}
        if(journal){e.setCanceled(true);e.setCancellationResult(InteractionResult.SUCCESS);
            p.openMenu(new SimpleMenuProvider((id,inventory,reader)->new JournalMenu(id,p,e.getPos()),Component.literal("Field notebook")));}
        else if(p.getMainHandItem().is(Items.STRING)&&anchor(b,e.getPos())){if(tie(p,e.getPos())){e.setCanceled(true);e.setCancellationResult(InteractionResult.SUCCESS);}}
        else if((prop(p,e.getPos())||!(p.getItemInHand(e.getHand()).getItem() instanceof BlockItem item&&torch(item.getBlock().defaultBlockState())))
                &&(chip(p,e.getPos())||examine(p,e.getPos()))){e.setCanceled(true);e.setCancellationResult(InteractionResult.SUCCESS);resync(p);}
    }
    /** The authored things a reader examines or works: the marks, the stone and its seat, and the front of the packed run. */
    static boolean prop(ServerPlayer p,BlockPos at){
        var b=base(p.server);var r=at.subtract(b);
        if(r.equals(CaverCave.MARK))return true;
        if(r.getZ()==-43&&r.getX()>=4&&r.getX()<=6&&r.getY()>=-3&&r.getY()<=-2)return true;
        int front=CaverCave.front(p.serverLevel(),b);return front>=0&&r.equals(CaverCave.rubbleCell(front));
    }
    /** A held torch the client already showed being placed on a prop is put back as the server holds it. */
    private static void resync(ServerPlayer p){if(p.getMainHandItem().getItem() instanceof BlockItem||p.getOffhandItem().getItem() instanceof BlockItem)p.inventoryMenu.sendAllDataToRemote();}
    public static void onLeftClick(PlayerInteractEvent.LeftClickBlock e){
        if(e.getAction()!=PlayerInteractEvent.LeftClickBlock.Action.START||!(e.getEntity() instanceof ServerPlayer p)||!inside(p)||!reach(p,e.getPos()))return;
        var state=p.serverLevel().getBlockState(e.getPos());
        if(CaverCave.isRubble(state)&&!p.getMainHandItem().is(ItemTags.PICKAXES)){e.setCanceled(true);pickaxeHint(p);}
        else if(torch(state)&&!ownsTorch(p,e.getPos())){e.setCanceled(true);p.displayClientMessage(Component.literal("That torch isn't yours to take."),true);}
        // Trying to dig at the marks only cracks and snaps back; say so, without saying more.
        else if(CaverCave.isMark(state)&&e.getPos().equals(base(p.server).offset(CaverCave.MARK)))p.displayClientMessage(Component.literal("The cuts are old. Look closer."),true);
    }
}
