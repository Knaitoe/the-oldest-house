package io.github.knaitoe.theoldesthouse.labyrinth;

import com.mojang.math.Transformation;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import io.github.knaitoe.theoldesthouse.house.SeatEntity;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.joml.Matrix4f;

/**
 * The house's rules, which are Minecraft's own turned slightly wrong.
 * <ul>
 * <li><b>Don't ask how.</b> Mining anything in the house never breaks it: it lifts off its place, turns over to show that
 * nothing is holding it, and settles back. The reader sees it alone. The first time is a warning; the second breaks the
 * rule for that reader, for good, and somebody starts walking about upstairs.</li>
 * <li>Things dropped in the third lake sink and lie still on the floor, instead of bobbing up.</li>
 * <li>Candles and the kitchen hearth burn under water.</li>
 * <li>The dining chairs hold no water; a reader sitting in one breathes ({@link LakeHouseDive}).</li>
 * <li>The pink closet's wall gives under a reader's hand from the tank visit on, and stays open.</li>
 * </ul>
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class LakeHouseRules {
    /** Ticks a lifted piece takes to rise and turn, to settle, and to be back where it was. */
    public static final int RISE=12,SETTLE=20,BACK=32;
    private record Lift(ServerPlayer reader,BlockPos at,LakeHouseDisplay shown,net.minecraft.world.level.block.state.BlockState gap,long start){}
    private static final List<Lift> LIFTS=new ArrayList<>();
    private static final Map<UUID,Long> LAST_ASK=new HashMap<>();
    private LakeHouseRules(){}

    /** Relative to the base: the house, its roof and everything under it. */
    public static boolean house(BlockPos r){return Math.abs(r.getX())<=LakeHouse.HX+1&&r.getZ()<=LakeHouse.HZ_FRONT+1&&r.getZ()>=LakeHouse.HZ_BACK-1&&r.getY()>=-44&&r.getY()<=-3;}
    /** Relative to the base: anywhere a reader can be under the house's floor, from the closet down. */
    static boolean below(Vec3 r){return Math.abs(r.x)<=LakeHouse.HX&&r.z<=LakeHouse.HZ_FRONT&&r.z>=LakeHouse.HZ_BACK&&r.y<LakeHouse.GROUND&&r.y>=-44;}
    /** Relative to the base: the basement, where the pool is. */
    static boolean basement(Vec3 r){return r.x>=-1&&r.x<=9&&r.z>=-177&&r.z<=-162&&r.y>=LakeHouseRooms.BASEMENT-3&&r.y<LakeHouseRooms.BASEMENT+5;}

    // ------------------------------------------------------------------------------------------------ don't ask how
    @SubscribeEvent public static void mine(PlayerInteractEvent.LeftClickBlock e){
        if(e.getLevel().isClientSide()||!(e.getEntity() instanceof ServerPlayer p)||p.isSpectator()||!LakeHouse.inside(p))return;
        var b=LakeHouse.base(p.server);if(b==null||!house(e.getPos().subtract(b)))return;
        e.setCanceled(true);
        long now=p.serverLevel().getGameTime();Long last=LAST_ASK.get(p.getUUID());
        if(last!=null&&now-last<BACK+8)return;
        LAST_ASK.put(p.getUUID(),now);ask(p,b,e.getPos());
    }
    /** A reader tries to take part of the house away. Returns how many times they have now asked. */
    public static int ask(ServerPlayer p,BlockPos b,BlockPos at){
        var l=p.serverLevel();var state=l.getBlockState(at);
        if(!state.isAir()&&!state.is(Blocks.WATER)&&!state.is(Blocks.LIGHT)){
            var shown=LakeHouseDisplay.show(l,at,state,p);
            // The reader sees the real piece go, only water where it stood, while the shown one turns over. The gap is sent a
            // tick later, after the server's own reply to the cancelled mining has put the block back on their screen.
            if(shown!=null)LIFTS.add(new Lift(p,at.immutable(),shown,state.getFluidState().isEmpty()?Blocks.AIR.defaultBlockState():Blocks.WATER.defaultBlockState(),l.getGameTime()));
        }
        var d=LabyrinthData.get(p.server);var own=LiteraryVignettes.personal(d,p.getUUID(),LakeHouse.PLACE);
        int tries=own.getInt("HowTries")+1;own.putInt("HowTries",tries);
        if(tries==1)p.displayClientMessage(Component.literal("It turns over in the water and settles back where it was. Nothing was holding it. Don't ask how.").withStyle(net.minecraft.ChatFormatting.ITALIC),false);
        else if(!own.getBoolean("AskedHow")){
            own.putBoolean("AskedHow",true);own.putInt("StepsUntil",own.getInt("T")+400);
            p.displayClientMessage(Component.literal("You asked anyway.").withStyle(net.minecraft.ChatFormatting.ITALIC),true);
        }
        LiteraryVignettes.save(d,p.getUUID(),LakeHouse.PLACE,own);
        return tries;
    }
    /** Turns lifted pieces over, settles them, and gives the reader back the real block. */
    @SubscribeEvent public static void lifts(ServerTickEvent.Post e){
        if(LIFTS.isEmpty())return;
        for(var it=LIFTS.iterator();it.hasNext();){
            var lift=it.next();var l=lift.shown().level();long age=l.getGameTime()-lift.start();
            if(age==1||age==3)lift.reader().connection.send(new ClientboundBlockUpdatePacket(lift.at(),lift.gap()));
            if(age==1)lift.shown().pose(turned(.45F,(float)Math.PI),RISE);
            else if(age==SETTLE){lift.shown().pose(turned(0,(float)(2*Math.PI)),BACK-SETTLE-2);
                hear(lift.reader(),SoundEvents.STONE_PLACE,lift.at(),.4F,.6F);}
            else if(age>=BACK||lift.shown().isRemoved()){
                lift.shown().discard();
                if(!lift.reader().isRemoved())lift.reader().connection.send(new ClientboundBlockUpdatePacket(l,lift.at()));
                it.remove();
            }
        }
    }
    /** Up a little and over about the block's centre. */
    private static Transformation turned(float up,float angle){
        return new Transformation(new Matrix4f().translate(.5F,.5F+up,.5F).rotateX(angle).translate(-.5F,-.5F,-.5F));
    }
    /** Somebody walking about upstairs, heard by that reader alone, for a while after they asked. */
    static void steps(ServerPlayer p,BlockPos b,CompoundTag own){
        int t=own.getInt("T");if(t>=own.getInt("StepsUntil")||t%15!=0)return;
        double a=t/60.0;var at=BlockPos.containing(b.getX()+Math.sin(a)*6,b.getY()+LakeHouse.UPPER+1,b.getZ()-170+Math.cos(a)*5);
        hear(p,SoundEvents.WOOD_STEP,at,.7F,.75F);
    }
    /** A sound for one reader's ears only. */
    static void hear(ServerPlayer p,net.minecraft.sounds.SoundEvent sound,BlockPos at,float volume,float pitch){
        p.connection.send(new net.minecraft.network.protocol.game.ClientboundSoundPacket(net.minecraft.core.registries.BuiltInRegistries.SOUND_EVENT.wrapAsHolder(sound),
                net.minecraft.sounds.SoundSource.AMBIENT,at.getX()+.5,at.getY()+.5,at.getZ()+.5,volume,pitch,p.getRandom().nextLong()));
    }

    // ------------------------------------------------------------------------------------------------ what sinks, what burns
    /** In the third lake, things sink and lie still. */
    @SubscribeEvent public static void sink(EntityTickEvent.Post e){
        if(!(e.getEntity() instanceof ItemEntity item)||item.level().isClientSide()||item.level().dimension()!=HouseDimensions.OUTSIDE||!item.isInWater())return;
        var b=LakeHouse.base(item.getServer());if(b==null)return;var r=item.position().subtract(Vec3.atLowerCornerOf(b));
        if(r.z>LakeHouse.CHANNEL_NORTH||!LakeHouse.box(b).contains(item.position()))return;
        item.setDeltaMovement(0,item.onGround()?0:-.05,0);
    }
    /** Every ten ticks while anyone is in the house: flames on the candles and in the hearth, under water. */
    static void flames(ServerLevel l,BlockPos b){
        for(var c:LakeHouseRooms.CANDLES)l.sendParticles(ParticleTypes.SMALL_FLAME,b.getX()+c.getX()+.5,b.getY()+c.getY()+.55,b.getZ()+c.getZ()+.5,1,.03,0,.03,0);
        var h=LakeHouseRooms.HEARTH;l.sendParticles(ParticleTypes.FLAME,b.getX()+h.getX()+.5,b.getY()+h.getY()+.4,b.getZ()+h.getZ()+.5,2,.15,.1,.15,.005);
        var s=LakeHouseRooms.SAUNA;l.sendParticles(ParticleTypes.CLOUD,b.getX()+s.getX()+.5,b.getY()+s.getY()+1,b.getZ()+s.getZ()+.5,1,.8,.4,.8,.002);
    }

    // ------------------------------------------------------------------------------------------------ chairs and the closet
    /** Whether the reader sits in one of the chairs that hold no water. */
    public static boolean dryChair(ServerPlayer p){
        if(!(p.getVehicle() instanceof SeatEntity seat))return false;
        var at=BlockPos.containing(seat.position());var s=p.level().getBlockState(at);
        return s.getBlock() instanceof StairBlock&&!s.getValue(BlockStateProperties.WATERLOGGED)&&LakeHouse.inside(p);
    }
    /** The closet's wall. True when the click was its. */
    static boolean closet(ServerPlayer p,BlockPos b,BlockPos rel,CompoundTag own){
        if(!rel.equals(LakeHouseRooms.PANEL)&&!rel.equals(LakeHouseRooms.PANEL.above()))return false;
        var l=p.serverLevel();var at=b.offset(LakeHouseRooms.PANEL);
        if(!l.getBlockState(at).is(Blocks.PINK_TERRACOTTA))return true;
        if(own.getInt("Visit")<3){p.displayClientMessage(Component.literal("The wall sounds hollow under your hand, but it doesn't give."),true);return true;}
        l.setBlock(at,Blocks.WATER.defaultBlockState(),3);l.setBlock(at.above(),Blocks.WATER.defaultBlockState(),3);
        l.playSound(null,at,SoundEvents.WOODEN_DOOR_OPEN,net.minecraft.sounds.SoundSource.BLOCKS,.8F,.7F);
        p.displayClientMessage(Component.literal("The wall gives. Behind it is a closet painted pink, and steps going down."),false);
        return true;
    }

    @SubscribeEvent public static void stopping(ServerStoppingEvent e){for(var lift:LIFTS)lift.shown().discard();LIFTS.clear();LAST_ASK.clear();}
}
