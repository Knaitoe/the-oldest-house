package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * 0.4.540, the Pell house's freezer (docs/PELL_HOUSE_FREEZER_0_4_540.md), from the proposal's "3. The last night" ("The freezer is
 * unlocked on the last night. The key is on June's lanyard on the robe by the pool chair, so it can be taken while she is in the water.
 * Inside is Nessa, under frosted plastic, in the dress from the photographs, intact and still") and "The last night, in play" ("The
 * freezer. Optional").
 *
 * <ul>
 * <li><b>The lanyard.</b> On the last night, while June's robe lies over the pool chair (from when she drops it there on her way into
 * the water until she puts it on again), its pocket has her lanyard in it, three keys on it. A hand on the chair takes it. One to a
 * house; one that nobody at the house has, and that is not lying in the lot, is back in the pocket within a second.</li>
 * <li><b>The freezer.</b> Padlocked on every visit. A hand on it with the lanyard about the reader opens the padlock; a hand on it then
 * lifts the lid, which stays up. Under it is Nessa.</li>
 * <li><b>Nessa.</b> In the freezer on every visit, under the lid: on her back, her head at the lock end, still. She does not move, turn
 * her head or breathe. Readers near enough to see into it while it is open keep that they saw her.</li>
 * <li><b>June by her.</b> Where June sits when a reader brings her to the open freezer and says the name (PellRite): the stool.</li>
 * </ul>
 */
public final class PellFreezer {
    private PellFreezer(){}
    /** The freezer's two cells, its lock end first; the stool beside it; the pool chair. */
    public static final BlockPos FREEZER=new BlockPos(9,0,-70),FREEZER_END=new BlockPos(9,0,-69),STOOL=new BlockPos(8,0,-69),CHAIR=new BlockPos(-14,0,-86);
    /** Where Nessa lies, from the house's base: along the freezer, on its floor. */
    public static final Vec3 NESSA_AT=new Vec3(9.5,.19,-69.0);
    /** Where June sits by her: on the stool, turned to the freezer. */
    static final PellVisit.Perch BY_HER=new PellVisit.Perch(STOOL,new Vec3(8.5,0,-68.5),-90,new Vec3(7.5,0,-67.5));
    /** How near June is to the open freezer, in the laundry, to be told the name there; how far a reader sees into it. */
    public static final double AT_FREEZER=3.2,LOOK=4.5;
    /** How often the lanyard is looked for, and Nessa put back where she lies. */
    public static final int KEEP=20;
    static final String HOME="PellHome";

    // ------------------------------------------------------------------------------------------------ the lanyard
    static ItemStack lanyard(BlockPos b){var s=new ItemStack(PellRegistry.LANYARD.get());var t=new CompoundTag();t.putLong(HOME,b.asLong());s.set(DataComponents.CUSTOM_DATA,CustomData.of(t));return s;}
    static boolean isLanyard(ItemStack s,BlockPos b){
        if(!s.is(PellRegistry.LANYARD.get()))return false;
        var d=s.get(DataComponents.CUSTOM_DATA);return d!=null&&d.copyTag().getLong(HOME)==b.asLong();
    }
    /** Whether a reader has this house's lanyard about them. */
    public static boolean carries(ServerPlayer p,BlockPos b){
        if(isLanyard(p.containerMenu.getCarried(),b))return true;
        var inv=p.getInventory();for(int i=0;i<inv.getContainerSize();i++)if(isLanyard(inv.getItem(i),b))return true;
        return false;
    }
    /** A hand on the pool chair: on the last night, with June's robe over it, the lanyard in its pocket. True if it answered. */
    static boolean chair(ServerPlayer p,BlockPos b,BlockState st,CompoundTag own){
        var h=PellPeople.household(b);
        if(h.visit<PellWeek.LAST||st.getValue(PellThing.STAGE)!=1)return false;
        if(h.lanyardOut){PellHouse.cue(p,PellTexts.LANYARD_GONE);return true;}
        if(!p.getInventory().add(lanyard(b))){PellHouse.cue(p,PellTexts.HANDS_FULL);return true;}
        h.lanyardOut=true;
        p.serverLevel().playSound(null,b.offset(CHAIR),SoundEvents.CHAIN_STEP,SoundSource.PLAYERS,.5F,1.7F);
        PellHouse.cue(p,PellTexts.LANYARD_FOUND);own.putBoolean("TookLanyard",true);
        return true;
    }
    /** The lanyard back where it was, from whoever has it, wherever they are: for a visit beginning again, and when nobody here has it. */
    static void back(ServerLevel l,BlockPos b){
        for(var p:l.getServer().getPlayerList().getPlayers())takeFrom(p,b);
        for(var e:l.getEntitiesOfClass(ItemEntity.class,PellPeople.area(b).inflate(16),e->isLanyard(e.getItem(),b)))e.discard();
        PellPeople.household(b).lanyardOut=false;
    }
    private static void takeFrom(ServerPlayer p,BlockPos b){
        if(isLanyard(p.containerMenu.getCarried(),b))p.containerMenu.setCarried(ItemStack.EMPTY);
        var inv=p.getInventory();for(int i=0;i<inv.getContainerSize();i++)if(isLanyard(inv.getItem(i),b))inv.setItem(i,ItemStack.EMPTY);
    }

    // ------------------------------------------------------------------------------------------------ the freezer
    /** Whether its lid is up. */
    public static boolean open(ServerLevel l,BlockPos b){return PellThing.is(l.getBlockState(b.offset(FREEZER)),PellThing.Kind.FREEZER_OPEN);}
    /** Whether its padlock is open (or its lid up). */
    public static boolean unlocked(ServerLevel l,BlockPos b){var s=l.getBlockState(b.offset(FREEZER));return open(l,b)||PellThing.is(s,PellThing.Kind.FREEZER)&&(s.getValue(PellThing.STAGE)&2)!=0;}
    /** Both its cells as this kind, keeping each one's half (STAGE bit 0) and adding the rest. */
    private static void set(ServerLevel l,BlockPos b,PellThing.Kind kind,int more){
        for(var c:new BlockPos[]{FREEZER,FREEZER_END}){
            var at=b.offset(c);var s=l.getBlockState(at);if(!PellThing.is(s))continue;
            l.setBlock(at,PellThing.of(kind,s.getValue(PellThing.FACING),(s.getValue(PellThing.STAGE)&1)|more),3);
        }
    }
    /** Shut and padlocked, as it is on every visit until the last night's lanyard opens it. */
    static void shut(ServerLevel l,BlockPos b){if(l.isLoaded(b.offset(FREEZER)))set(l,b,PellThing.Kind.FREEZER,0);}
    /** A hand on the freezer: with the lanyard, the padlock; unlocked, the lid; open, what is under it. True if it answered. */
    static boolean hand(ServerPlayer p,BlockPos b,BlockState st,CompoundTag own){
        var l=p.serverLevel();var at=b.offset(FREEZER);
        if(PellThing.is(st,PellThing.Kind.FREEZER_OPEN)){PellHouse.cue(p,PellTexts.FREEZER_AGAIN);saw(p,own);return true;}
        if((st.getValue(PellThing.STAGE)&2)==0){
            if(!carries(p,b))return false;
            set(l,b,PellThing.Kind.FREEZER,2);
            l.playSound(null,at,SoundEvents.CHAIN_BREAK,SoundSource.BLOCKS,.5F,1.4F);
            PellHouse.cue(p,PellTexts.FREEZER_UNLOCKED);own.putBoolean("UnlockedFreezer",true);
            return true;
        }
        set(l,b,PellThing.Kind.FREEZER_OPEN,0);
        l.playSound(null,at,SoundEvents.CHEST_OPEN,SoundSource.BLOCKS,.6F,.6F);
        var mid=PellVisit.world(b,new Vec3(NESSA_AT.x,.9,NESSA_AT.z));
        l.sendParticles(ParticleTypes.SNOWFLAKE,mid.x,mid.y,mid.z,14,.35,.1,.7,.01);
        PellHouse.cue(p,PellTexts.FREEZER_LID);PellHouse.cue(p,PellTexts.FREEZER_OPENED);
        own.putBoolean("OpenedFreezer",true);saw(p,own);
        return true;
    }
    private static void saw(ServerPlayer p,CompoundTag own){own.putBoolean("SawNessa",true);}
    /** Twice a second while it is open: readers who see into it keep that they saw her. */
    static void seen(ServerLevel l,BlockPos b,List<ServerPlayer> here){
        if(!open(l,b))return;
        var at=PellVisit.world(b,new Vec3(NESSA_AT.x,.7,NESSA_AT.z));
        for(var p:here){
            if(!p.isAlive()||p.isSpectator()||p.isSleeping()||p.position().distanceTo(at)>LOOK||!PellNight.sees(p,at,LOOK))continue;
            var own=PellHouse.own(p);if(!own.getBoolean("SawNessa")){own.putBoolean("SawNessa",true);PellHouse.save(p,own);}
        }
    }

    // ------------------------------------------------------------------------------------------------ Nessa
    /** Her body at this house, if it is here. */
    public static @Nullable LiteraryActor nessa(ServerLevel l,BlockPos b){
        var h=PellPeople.household(b);
        if(h.nessa!=null&&l.getEntity(h.nessa) instanceof LiteraryActor a&&a.isAlive()&&a.phase()==PellPeople.FIRST+PellPeople.NESSA)return a;
        for(var a:l.getEntitiesOfClass(LiteraryActor.class,PellPeople.area(b),a->a.role()==LiteraryActor.STRANGER&&a.phase()==PellPeople.FIRST+PellPeople.NESSA)){h.nessa=a.getUUID();return a;}
        return null;
    }
    /** Nessa in the freezer, made there if she is not, where its ground's entities are loaded; never twice. */
    static void ensure(ServerLevel l,BlockPos b){
        var at=PellVisit.world(b,NESSA_AT);var cell=BlockPos.containing(at);
        if(!l.isLoaded(cell)||!l.areEntitiesLoaded(ChunkPos.asLong(cell))||nessa(l,b)!=null)return;
        var a=LiteraryRegistry.ACTOR.get().create(l);if(a==null)return;
        a.bind(PellHouse.PLACE,null);a.appearance(LiteraryActor.STRANGER,PellPeople.FIRST+PellPeople.NESSA);
        lay(b,a);
        if(l.addFreshEntity(a))PellPeople.household(b).nessa=a.getUUID();
    }
    /** Where she lies, and how: still, on her back along the freezer, her head at the lock end. */
    private static void lay(BlockPos b,LiteraryActor a){
        var at=PellVisit.world(b,NESSA_AT);float look=Direction.NORTH.toYRot();
        a.noPhysics=true;a.setNoGravity(true);a.gesture(PellPeople.STILL);
        a.moveTo(at.x,at.y,at.z,look,0);a.setYHeadRot(look);a.setYBodyRot(look);a.setDeltaMovement(Vec3.ZERO);
    }
    /**
     * Every second for a house with readers at it: the lanyard, if nobody here has it and it is not lying on the lot, back in the
     * pocket, taken from anybody who carried it away (as the bucket is); one still about a reader while the pocket has it is taken back
     * too. Nessa where she lies.
     */
    static void keep(ServerLevel l,BlockPos b,List<ServerPlayer> here){
        var h=PellPeople.household(b);
        if(h.lanyardOut){
            boolean held=false;for(var p:here)if(p.isAlive()&&!p.isSpectator()&&carries(p,b)){held=true;break;}
            if(!held&&l.getEntitiesOfClass(ItemEntity.class,PellPeople.area(b),e->isLanyard(e.getItem(),b)).isEmpty())back(l,b);
        }else for(var p:here)if(carries(p,b))takeFrom(p,b);
        var a=nessa(l,b);
        if(a!=null&&(a.position().distanceToSqr(PellVisit.world(b,NESSA_AT))>.01||a.gesture()!=PellPeople.STILL||!a.isNoGravity()))lay(b,a);
    }
    /** The visit beginning again: the freezer shut and padlocked, the lanyard back. */
    static void restart(ServerLevel l,BlockPos b){shut(l,b);back(l,b);}

    // ------------------------------------------------------------------------------------------------ for tests
    /** For tests: the lanyard for this house, as it comes out of the pocket. */
    public static ItemStack lanyardFor(BlockPos b){return lanyard(b);}
    /** For tests: whether the lanyard is out of the pocket. */
    public static boolean lanyardOut(BlockPos b){return PellPeople.household(b).lanyardOut;}
}
