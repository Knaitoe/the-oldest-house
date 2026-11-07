package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import io.github.knaitoe.theoldesthouse.network.HousePackets;
import io.github.knaitoe.theoldesthouse.network.YardCrawlPayload;
import java.util.*;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * 0.4.343, what the piece of the Dream Stela does (docs/STELA_PASSAGE_0_4_343.md; the owner's draft, "A Stone From Between the Paws"):
 * "Press it against loose fill covering a buried passage. Sand, gravel, or authored rubble draws aside long enough to crawl into the open
 * space beyond ... The loose material settles after you pass."
 * <p>
 * Pressed against sand, red sand, gravel or the dream's drifted sand, the stone looks straight in, the way its reader faces, for open
 * space. If it finds some within its reach (three, five or eight blocks of fill, from the reader's record), it holds the fill between
 * aside as a passage one block high, {@link StelaFillBlock}, which keeps the fill above from coming down into it. Farther in than it
 * reaches, the near edge only shifts and settles; against something harder, it gives a little and stops.
 * <p>
 * It opens as the reader's dream went. Patient: quietly, and it holds longest. Dismissive: the stone must be held there, a narrow crack
 * coming first, through which the reader sees what is beyond before it opens. Hostile: at once, with a hard crack that carries to
 * anything nearby, and it holds least time. Every opening sheds grit at both ends before it closes, early enough to crawl its whole
 * length; it never closes on anyone inside, and when it settles the exact fill goes back. One passage to a stone at a time; the passage
 * is everyone's while it is open; it is saved, so a passage open across a restart settles as it should.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class StelaPassage {
    private StelaPassage(){}
    /** Ticks to crawl a block, with a margin: the warning before a passage closes gives a normal crawl its whole length. */
    static final int CRAWL_PER_BLOCK=15,WARN_SPARE=40;
    /** Dismissive: held this long, the crack; this long, the opening. */
    static final int CRACK_AT=20,OPEN_AT=70;

    // ------------------------------------------------------------------ what counts
    /** Loose fill the stone can part: sand, red sand, gravel, and the dream's drifted sand. */
    public static boolean fill(BlockState s){return s.is(Blocks.SAND)||s.is(Blocks.RED_SAND)||s.is(Blocks.GRAVEL)||SphinxBlock.is(s,SphinxBlock.Kind.DRIFT);}
    /** Open space a body can crawl into: nothing to collide with, and no water or lava. */
    static boolean open(ServerLevel l,BlockPos at,BlockState s){return !StelaFillBlock.is(s)&&s.getCollisionShape(l,at).isEmpty()&&s.getFluidState().isEmpty();}
    /** Ticks held open, by manner and length. */
    public static int hold(String opening,int length){
        return switch(opening){case "hostile"->Math.max(warn(length)+60,200+20*length);case "dismissive"->400+30*length;default->600+40*length;};
    }
    static int warn(int length){return CRAWL_PER_BLOCK*length+WARN_SPARE;}

    // ------------------------------------------------------------------ the passages, saved
    static final class Passage {
        UUID owner;ResourceKey<Level> level;Direction dir;String opening;
        final List<BlockPos> cells=new ArrayList<>();final List<BlockState> saved=new ArrayList<>();
        long closeAt;boolean committed;int phase=StelaFillBlock.OPEN;
        BlockPos mouth(){return cells.getFirst().relative(dir.getOpposite());}
        BlockPos pocket(){return cells.getLast().relative(dir);}
        CompoundTag save(){
            var t=new CompoundTag();t.putUUID("Owner",owner);t.putString("Level",level.location().toString());t.putInt("Dir",dir.get3DDataValue());t.putString("Opening",opening);
            t.putLong("CloseAt",closeAt);t.putBoolean("Committed",committed);t.putInt("Phase",phase);
            var list=new ListTag();for(int i=0;i<cells.size();i++){var c=new CompoundTag();c.put("Pos",NbtUtils.writeBlockPos(cells.get(i)));c.put("State",NbtUtils.writeBlockState(saved.get(i)));list.add(c);}
            t.put("Cells",list);return t;
        }
        static @Nullable Passage load(CompoundTag t,HolderLookup.Provider registries){
            var p=new Passage();if(!t.hasUUID("Owner"))return null;p.owner=t.getUUID("Owner");
            var loc=ResourceLocation.tryParse(t.getString("Level"));if(loc==null)return null;p.level=ResourceKey.create(Registries.DIMENSION,loc);
            p.dir=Direction.from3DDataValue(t.getInt("Dir"));p.opening=t.getString("Opening");p.closeAt=t.getLong("CloseAt");p.committed=t.getBoolean("Committed");p.phase=t.getInt("Phase");
            var blocks=registries.lookupOrThrow(Registries.BLOCK);
            for(var e:t.getList("Cells",Tag.TAG_COMPOUND)){var c=(CompoundTag)e;var pos=NbtUtils.readBlockPos(c,"Pos");if(pos.isEmpty())continue;p.cells.add(pos.get());p.saved.add(NbtUtils.readBlockState(blocks,c.getCompound("State")));}
            return p.cells.isEmpty()?null:p;
        }
    }
    public static final class Store extends SavedData {
        final Map<UUID,Passage> open=new LinkedHashMap<>();
        static final Factory<Store> FACTORY=new Factory<>(Store::new,Store::load);
        public static Store load(CompoundTag t,HolderLookup.Provider r){var s=new Store();for(var e:t.getList("Passages",Tag.TAG_COMPOUND)){var p=Passage.load((CompoundTag)e,r);if(p!=null)s.open.put(p.owner,p);}return s;}
        @Override public CompoundTag save(CompoundTag t,HolderLookup.Provider r){var list=new ListTag();for(var p:open.values())list.add(p.save());t.put("Passages",list);return t;}
        public static Store get(MinecraftServer s){return s.overworld().getDataStorage().computeIfAbsent(FACTORY,"the_oldest_house_stela_passages");}
    }
    /** A reader's stone already holding a passage, or null. */
    public static @Nullable List<BlockPos> openFor(MinecraftServer s,UUID owner){var p=Store.get(s).open.get(owner);return p==null?null:List.copyOf(p.cells);}

    // ------------------------------------------------------------------ pressing the stone
    private record Pending(ServerLevel level,Direction dir,List<BlockPos> cells,BlockPos clicked){}
    private static final Map<UUID,Pending> PENDING=new HashMap<>();

    /** The stone pressed against a block. */
    public static InteractionResult press(ServerPlayer p,ItemStack stone,BlockPos clicked,InteractionHand hand){
        var l=p.serverLevel();var state=l.getBlockState(clicked);
        if(!DreamStela.is(stone)||!fill(state))return InteractionResult.PASS;
        if(HouseDimensions.isHouseDimension(l.dimension())){line(p,"Nothing here gives to it.");return InteractionResult.CONSUME;}
        var owner=DreamStela.ownerOf(stone);
        if(owner==null||!owner.equals(p.getUUID())){line(p,"It is not yours to press.");return InteractionResult.CONSUME;}
        var store=Store.get(p.server);
        if(store.open.containsKey(owner)||PENDING.containsKey(owner)){line(p,"The stone is already holding a way open.");return InteractionResult.CONSUME;}
        var dir=p.getDirection();int reach=Math.max(1,DreamStela.reachOf(stone));
        var cells=new ArrayList<BlockPos>();var at=clicked;
        while(true){
            if(!l.isLoaded(at))return stopped(p,l,clicked,"The fill shifts at the face of it and settles back.");
            var s=l.getBlockState(at);
            if(fill(s)){
                if(cells.size()>=reach)return stopped(p,l,cells.getLast(),"The near edge shifts and settles. Whatever is behind it is farther in than the stone reaches.");
                if(!allowed(p,l,at,s))return stopped(p,l,clicked,"It will not part here.");
                cells.add(at.immutable());at=at.relative(dir);continue;
            }
            if(open(l,at,s))break;
            return stopped(p,l,cells.isEmpty()?clicked:cells.getLast(),"The fill gives a little and stops against something harder.");
        }
        String opening=DreamStela.openingOf(stone);
        if(opening.equals("dismissive")){PENDING.put(owner,new Pending(l,dir,cells,clicked));p.startUsingItem(hand);return InteractionResult.CONSUME;}
        var passage=part(l,owner,dir,cells,opening,StelaFillBlock.OPEN);
        passage.committed=true;passage.closeAt=l.getGameTime()+hold(opening,cells.size());store.setDirty();
        var m=Vec3.atCenterOf(cells.getFirst());
        if(opening.equals("hostile")){
            // At once, with a hard crack that carries: anything nearby hears it.
            l.playSound(null,m.x,m.y,m.z,PuzzleRegistry.SPHINX_CRACK.get(),SoundSource.BLOCKS,1.6F,.8F);
            for(var c:cells)l.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK,l.getBlockState(c.above())),c.getX()+.5,c.getY()+.5,c.getZ()+.5,14,.35,.35,.35,.15);
            l.gameEvent(p,GameEvent.BLOCK_DESTROY,m);
            for(var mob:l.getEntitiesOfClass(Monster.class,new AABB(m,m).inflate(24))){mob.getNavigation().moveTo(m.x,m.y,m.z,1.1);if(mob.hasLineOfSight(p))mob.setTarget(p);}
        }else{
            // Quietly, the fill drawing aside grain by grain.
            l.playSound(null,m.x,m.y,m.z,PuzzleRegistry.SPHINX_SAND.get(),SoundSource.BLOCKS,.5F,.8F);
            for(var c:cells)l.sendParticles(new BlockParticleOption(ParticleTypes.FALLING_DUST,state),c.getX()+.5,c.getY()+.9,c.getZ()+.5,4,.3,.1,.3,0);
        }
        return InteractionResult.CONSUME;
    }
    /** Dismissive: the stone held against the fill, {@code held} ticks so far. */
    public static void holding(ServerPlayer p,int held){
        var pend=PENDING.get(p.getUUID());if(pend==null||p.serverLevel()!=pend.level())return;
        var store=Store.get(p.server);var passage=store.open.get(p.getUUID());
        if(Vec3.atCenterOf(pend.clicked()).distanceTo(p.getEyePosition())>5.5){released(p);p.stopUsingItem();return;}
        var m=Vec3.atCenterOf(pend.cells().getFirst());
        if(passage==null&&held>=CRACK_AT){
            // A narrow crack first: the reader sees what is beyond before it opens.
            passage=part(pend.level(),p.getUUID(),pend.dir(),pend.cells(),"dismissive",StelaFillBlock.CRACK);passage.closeAt=Long.MAX_VALUE;store.setDirty();
            pend.level().playSound(null,m.x,m.y,m.z,PuzzleRegistry.SPHINX_SETTLE.get(),SoundSource.BLOCKS,.6F,1.2F);
        }
        if(passage!=null&&!passage.committed&&held>=OPEN_AT){
            passage.committed=true;passage.closeAt=pend.level().getGameTime()+hold("dismissive",passage.cells.size());set(pend.level(),passage,StelaFillBlock.OPEN);store.setDirty();
            pend.level().playSound(null,m.x,m.y,m.z,PuzzleRegistry.SPHINX_SAND.get(),SoundSource.BLOCKS,.7F,.9F);
            PENDING.remove(p.getUUID());p.stopUsingItem();
        }
    }
    /** Dismissive: let go. A crack not yet opened closes again. */
    public static void released(ServerPlayer p){
        var pend=PENDING.remove(p.getUUID());if(pend==null)return;
        var store=Store.get(p.server);var passage=store.open.get(p.getUUID());
        if(passage!=null&&!passage.committed){settle(pend.level(),passage);store.open.remove(p.getUUID());store.setDirty();}
    }
    private static InteractionResult stopped(ServerPlayer p,ServerLevel l,BlockPos at,String text){
        l.sendParticles(new BlockParticleOption(ParticleTypes.FALLING_DUST,l.getBlockState(at)),at.getX()+.5,at.getY()+.6,at.getZ()+.5,10,.4,.3,.4,0);
        l.playSound(null,at,PuzzleRegistry.SPHINX_SETTLE.get(),SoundSource.BLOCKS,.5F,1F);
        line(p,text);return InteractionResult.CONSUME;
    }
    /** The reader may move these blocks: they could break them where they stand, nothing protects them, and their game mode allows it. */
    private static boolean allowed(ServerPlayer p,ServerLevel l,BlockPos at,BlockState s){
        var mode=p.gameMode.getGameModeForPlayer();
        return l.mayInteract(p,at)&&!p.blockActionRestricted(l,at,mode)&&!CommonHooks.fireBlockBreak(l,mode,p,at,s).isCanceled();
    }
    /** Holds the fill aside: saves each cell's exact block and sets the parted fill in its place. */
    private static Passage part(ServerLevel l,UUID owner,Direction dir,List<BlockPos> cells,String opening,int phase){
        var p=new Passage();p.owner=owner;p.level=l.dimension();p.dir=dir;p.opening=opening;p.phase=phase;
        for(var c:cells){var s=l.getBlockState(c);p.cells.add(c);p.saved.add(s);l.setBlock(c,StelaFillBlock.of(s,dir,phase),Block.UPDATE_ALL);}
        Store.get(l.getServer()).open.put(owner,p);return p;
    }
    private static void set(ServerLevel l,Passage p,int phase){
        p.phase=phase;
        for(var c:p.cells){var s=l.getBlockState(c);if(StelaFillBlock.is(s))l.setBlock(c,s.setValue(StelaFillBlock.PHASE,phase),Block.UPDATE_CLIENTS);}
    }
    /** The fill settles: the exact blocks go back where the parted fill still stands. */
    private static void settle(ServerLevel l,Passage p){
        for(int i=0;i<p.cells.size();i++){var c=p.cells.get(i);if(StelaFillBlock.is(l.getBlockState(c)))l.setBlock(c,p.saved.get(i),Block.UPDATE_ALL);}
        var m=Vec3.atCenterOf(p.cells.getFirst());
        l.playSound(null,m.x,m.y,m.z,PuzzleRegistry.SPHINX_SETTLE.get(),SoundSource.BLOCKS,.8F,.8F);
        for(var c:p.cells)l.sendParticles(new BlockParticleOption(ParticleTypes.FALLING_DUST,p.saved.get(p.cells.indexOf(c))),c.getX()+.5,c.getY()+.9,c.getZ()+.5,3,.3,.1,.3,0);
    }
    private static boolean occupied(ServerLevel l,Passage p){for(var c:p.cells)if(!l.getEntitiesOfClass(LivingEntity.class,new AABB(c),e->e.isAlive()&&!e.isSpectator()).isEmpty())return true;return false;}
    private static boolean loaded(ServerLevel l,Passage p){for(var c:p.cells)if(!l.isLoaded(c))return false;return true;}
    private static void line(ServerPlayer p,String text){p.displayClientMessage(Component.literal(text).withStyle(ChatFormatting.GRAY,ChatFormatting.ITALIC),true);}

    // ------------------------------------------------------------------ open passages, each tick
    @SubscribeEvent public static void tick(ServerTickEvent.Post e){
        var s=e.getServer();var store=Store.get(s);
        for(var p:List.copyOf(store.open.values())){
            var l=s.getLevel(p.level);if(l==null||!loaded(l,p))continue;
            long now=l.getGameTime();
            if(!p.committed){
                // A crack whose reader has let go, logged off or gone elsewhere closes again.
                var holder=s.getPlayerList().getPlayer(p.owner);
                if(holder==null||!PENDING.containsKey(p.owner)||holder.serverLevel()!=l){PENDING.remove(p.owner);settle(l,p);store.open.remove(p.owner);store.setDirty();}
                continue;
            }
            if(p.phase==StelaFillBlock.OPEN&&now>=p.closeAt-warn(p.cells.size())){set(l,p,StelaFillBlock.SHEDDING);store.setDirty();}
            if(p.phase==StelaFillBlock.SHEDDING&&now%10==0){
                // Grit sheds at both ends: a warning to anyone inside or about to go in.
                boolean hard=p.opening.equals("hostile");
                for(var end:List.of(p.cells.getFirst(),p.cells.getLast())){
                    l.sendParticles(new BlockParticleOption(ParticleTypes.FALLING_DUST,p.saved.get(end==p.cells.getFirst()?0:p.saved.size()-1)),end.getX()+.5,end.getY()+.95,end.getZ()+.5,hard?10:5,.35,.05,.35,0);
                    l.playSound(null,end,PuzzleRegistry.SPHINX_SAND.get(),SoundSource.BLOCKS,hard?.9F:.45F,hard?1.3F:1F);
                }
            }
            if(now>=p.closeAt){
                if(occupied(l,p)){p.closeAt=now+20;store.setDirty();continue;}
                settle(l,p);store.open.remove(p.owner);store.setDirty();
            }
        }
        crawl(s,store);
    }

    // ------------------------------------------------------------------ crawling through
    private record Lease(ServerPlayer player,@Nullable Pose forced,Pose pose){}
    private static final Map<UUID,Lease> LEASES=new HashMap<>();
    /** A reader inside a passage crawls; so does one crouching at either end and facing in. Out in the open again, they stand. */
    private static void crawl(MinecraftServer s,Store store){
        for(var lease:List.copyOf(LEASES.values()))if(s.getPlayerList().getPlayer(lease.player().getUUID())!=lease.player()||!lease.player().isAlive())clear(lease.player());
        if(store.open.isEmpty()&&LEASES.isEmpty())return;
        for(var p:s.getPlayerList().getPlayers()){
            boolean inside=false,atMouth=false;
            if(p.isAlive()&&!p.isSpectator())for(var passage:store.open.values()){
                if(!passage.committed||!p.level().dimension().equals(passage.level))continue;
                for(var c:passage.cells)if(p.getBoundingBox().intersects(new AABB(c)))inside=true;
                if(p.isShiftKeyDown()&&(facing(p,passage.mouth(),passage.dir)||facing(p,passage.pocket(),passage.dir.getOpposite())))atMouth=true;
            }
            var lease=LEASES.get(p.getUUID());
            if(lease==null){if(inside||atMouth)take(p);continue;}
            var standing=new AABB(p.getX()-.3,p.getY()+.01,p.getZ()-.3,p.getX()+.3,p.getY()+1.8,p.getZ()+.3);
            if(!inside&&!atMouth&&p.serverLevel().noCollision(p,standing)){clear(p);continue;}
            if(s.getTickCount()%10==0)HousePackets.send(p,new YardCrawlPayload(true));
        }
    }
    /** Standing in the block before an end of the passage, at its height, and facing along it. */
    private static boolean facing(ServerPlayer p,BlockPos before,Direction along){
        return p.blockPosition().equals(before)&&p.getLookAngle().dot(new Vec3(along.getStepX(),0,along.getStepZ()))>.4;
    }
    private static void take(ServerPlayer p){
        LEASES.put(p.getUUID(),new Lease(p,p.getForcedPose(),p.getPose()));
        p.setForcedPose(Pose.SWIMMING);p.setPose(Pose.SWIMMING);p.refreshDimensions();HousePackets.send(p,new YardCrawlPayload(true));
    }
    private static void clear(ServerPlayer p){
        var lease=LEASES.remove(p.getUUID());if(lease==null)return;
        if(p.getForcedPose()==Pose.SWIMMING){p.setForcedPose(lease.forced());p.setPose(lease.forced()==null?lease.pose():lease.forced());p.refreshDimensions();}
        HousePackets.send(p,new YardCrawlPayload(false));
    }
    public static boolean crawling(ServerPlayer p){return LEASES.containsKey(p.getUUID());}
    @SubscribeEvent public static void stopped(ServerStoppedEvent e){PENDING.clear();LEASES.clear();}

    // ------------------------------------------------------------------ for tests
    /** Brings a passage's closing forward to {@code ticks} from now. */
    public static void closeIn(MinecraftServer s,UUID owner,int ticks){var p=Store.get(s).open.get(owner);var l=p==null?null:s.getLevel(p.level);if(p!=null&&l!=null){p.closeAt=l.getGameTime()+ticks;Store.get(s).setDirty();}}
    public static long closeAt(MinecraftServer s,UUID owner){var p=Store.get(s).open.get(owner);return p==null?-1:p.closeAt;}
}
