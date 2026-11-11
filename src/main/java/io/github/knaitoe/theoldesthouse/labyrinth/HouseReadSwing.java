package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.SeatEntity;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChainBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/**
 * 0.4.172 (owner: "The rope swing should be interactive. Players should be able to swing."): the porch's swing seats a reader
 * and swings them. The seat hangs from the crossbar on its two ropes; while someone sits, the still seat and ropes are hidden and
 * the seat entity carries the reader along the arc, building up to a gentle swing, and the renderer draws the seat and ropes
 * moving with them. Getting off puts the still swing back. Every position comes from the time sat down, so client and server
 * agree without sending it.
 *
 * <p>0.4.423 (owner: "players should be able to jump from swings. The game should give them an appropriate launch based on their
 * velocity"): jumping on a swing lets go of it. The reader leaves from where the seat is, carried on by the seat's own speed along
 * the arc at that moment, a little more than it (a push off the seat with the legs), and a little up. Off the forward upswing that
 * throws them out ahead; at either end of the arc, where the seat stops, they mostly drop; going back, they go back. A swing faces
 * the way its sitter does: the seat block's facing is behind the sitter, so the porch's (facing south) swings north and south, and a
 * swing can hang either way round.
 *
 * <p>0.4.443 (owner: "The swing should offer some forward momentum from jumps too"): the legs push off forward as well, the way the
 * sitter faces. Every jump gains PUSH along the swing's forward on top of the seat's carried speed: off the forward upswing it throws
 * them further; at either end of the arc, where the seat stops, they hop forward instead of dropping; going back, they go back much
 * less. A hop forward from the back of the arc crosses where the still seat hangs, so after a jump the empty seat swings on until the
 * jumper is down and it is next at the bottom of its arc (SeatEntity), and only then gives way to the still one.
 *
 * <p>0.4.465, Bike night (owner: "Lots of fun potential for unique interaction here - a seesaw, slides, swings, monkey bars, etc"):
 * the park's swings are swings too, and are pumped. A park swing is one belt seat on two chains (CalderPlayBlock), the same length as
 * the porch's ropes. Left alone it comes up to the same gentle arc. Its rider can take it higher: leaning forward while it goes
 * forward, or back while it goes back, widens the arc, up to HIGH; leaning against it narrows it. So the arc of a pumped swing is as
 * wide as its rider has made it and is kept by the seat (SeatEntity.reach), and where it is on that arc still comes from the time
 * sat down. Letting go works as it always has, so a swing pumped high throws its rider a long way.
 *
 * <p>0.4.474 (owner: "please ensure players can push other players on the swing set"): somebody on the ground pushes the one on a
 * swing. A hand on the rider, from behind them or from in front, is a push (hand). Timed as the swing leaves the hands' end of its
 * arc, or as it stops there, it widens the arc; made while the swing is still coming at the hands it takes some of the arc off; a
 * swing hanging still is started. The park's swings are pushed as high as they are pumped. The porch's kind (Drisking's yard swing
 * is one) is pushed a little wider than its gentle arc and no further than there is room (SeatEntity.push). Any swing a reader can
 * sit on can be pushed; which hands and when is this class's to say, and the scene the swing is in keeps what it wants of it.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class HouseReadSwing {
    private HouseReadSwing(){}
    /** Ropes three blocks long above the seat to the crossbar's underside, the seat's top half a block up. */
    public static final double PIVOT=4.0,SEAT=.5,LENGTH=PIVOT-SEAT;
    /** Radians a tick of the swing's sway, the widest arc, and how quickly a sitter builds up to it. */
    public static final double RATE=.12,REACH=.5,BUILD=.004;
    /** Letting go: how much of the seat's speed the reader keeps, the push of their legs forward, and the hop up off the seat. */
    public static final double CARRY=1.6,PUSH=.2,HOP=.3;
    /** A pumped swing: the widest arc there is, what a tick of leaning with it (or against it) adds (or takes), and how it eases back to a gentle one. */
    public static final double HIGH=1.15,PUMP=.006,EASE=.001;
    /**
     * 0.4.474, a push: what a well-timed one adds to the arc, in radians; what one made too soon takes off it; how narrow an arc counts
     * as hanging still; how near the swing's own line somebody is beside it, in blocks; and the widest a rope swing is pushed.
     */
    public static final double PUSHED=.15,CHECKED=.1,STILL=.06,BESIDE=.75,ROPE_HIGH=.7;
    /**
     * Ticks before the top of its arc that hands may meet a swing; ticks between two pushes from the same hands, longer than a swing
     * takes to leave them, so that they get one push in as it goes and not two; and half a swing, there and back to the bottom.
     */
    public static final int MEET=4,PUSH_WAIT=20,HALF_SWING=(int)Math.round(Math.PI/RATE);
    /** What the one pushing is told once; when a push came too soon; when the swing is out past the middle; from the side; and what the one pushed is told. */
    public static final String PUSH_HINT="Push as the swing starts away from you. Too soon, and you take the swing off it.";
    public static final String PUSH_EARLY="Too soon. That took some of the swing off it.";
    public static final String PUSH_FAR="Out of reach. Wait for it to come back to you.";
    public static final String PUSH_BESIDE="Not from the side. Get behind the swing, or in front of it.";
    public static final String PUSHED_BY="%s gives the swing a push.";
    public static boolean is(BlockState s){return rope(s)||pumped(s);}
    /** The porch's kind: a plank two blocks wide on ropes. */
    private static boolean rope(BlockState s){return s.is(HouseReadRegistry.FITTING.get())&&s.getValue(HouseReadPropBlock.PART)==HouseReadPropBlock.Part.SWING;}
    /** The park's kind: one belt seat on chains, which its rider pumps. */
    public static boolean pumped(BlockState s){return CalderPlayBlock.is(s,CalderPlayBlock.Kind.SWING);}
    /** The seat as it hangs with nobody on it, and one block of what hangs it: what the moving swing is drawn from. */
    public static BlockState still(BlockState s){return pumped(s)?s.setValue(CalderPlayBlock.STAGE,0):s.setValue(HouseReadPropBlock.TURNED,false);}
    public static BlockState hanger(BlockState s){return pumped(s)?CalderPlayBlock.of(CalderPlayBlock.Kind.CHAIN,s.getValue(HorizontalDirectionalBlock.FACING)):HouseReadRegistry.ROPE.get().defaultBlockState().setValue(ChainBlock.AXIS,Direction.Axis.Y);}
    /** The way a sitter on this seat faces, and the way the swing goes forward: away from the seat block's facing. */
    public static Direction forward(BlockState s){return is(s)?s.getValue(HorizontalDirectionalBlock.FACING).getOpposite():Direction.NORTH;}
    /** The seat's two halves: this one and its neighbour along the crossbar, west (or north) first. */
    public static List<BlockPos> pair(Level l,BlockPos at){
        if(pumped(l.getBlockState(at)))return List.of(at);   // a belt seat is one block, however near the next one hangs
        var side=forward(l.getBlockState(at)).getClockWise();var before=side.getAxisDirection()==Direction.AxisDirection.POSITIVE?side.getOpposite():side;var after=before.getOpposite();
        var out=new ArrayList<BlockPos>();
        if(is(l.getBlockState(at.relative(before)))){out.add(at.relative(before));out.add(at);}else{out.add(at);if(is(l.getBlockState(at.relative(after))))out.add(at.relative(after));}
        return out;
    }
    /** Where the swing hangs from, at the middle of the seat under the crossbar. */
    public static Vec3 pivot(List<BlockPos> pair){var first=pair.getFirst();var last=pair.getLast();return new Vec3((first.getX()+last.getX())/2.+.5,first.getY()+PIVOT,(first.getZ()+last.getZ())/2.+.5);}
    /** The angle from the rest position after {@code ticks} sat: the sway grows to its reach and stays there. */
    public static double angle(double ticks){return Math.min(REACH,Math.max(0,ticks)*BUILD)*Math.sin(Math.max(0,ticks)*RATE);}
    /** The seat's place on the arc for an angle: forward (the way the sitter faces) is positive. */
    public static Vec3 seat(Vec3 pivot,Direction forward,double angle){return pivot.add(forward.getStepX()*LENGTH*Math.sin(angle),-LENGTH*Math.cos(angle),forward.getStepZ()*LENGTH*Math.sin(angle));}
    /** The seat's velocity, in blocks a tick, {@code ticks} after the reader sat down. */
    public static Vec3 velocity(Vec3 pivot,Direction forward,double ticks){return seat(pivot,forward,angle(ticks+.5)).subtract(seat(pivot,forward,angle(ticks-.5)));}
    /** What a reader who lets go at that velocity, on a swing going that way, leaves with. */
    public static Vec3 launch(Vec3 seat,Direction forward){return seat.scale(CARRY).add(forward.getStepX()*PUSH,HOP,forward.getStepZ()*PUSH);}
    /** A reader sits: the still swing is hidden so the moving one can be drawn in its place. */
    public static void hide(ServerPlayer p,BlockPos at){
        var l=p.serverLevel();
        if(pumped(l.getBlockState(at))){
            l.setBlock(at,l.getBlockState(at).setValue(CalderPlayBlock.STAGE,CalderPlayBlock.GONE),Block.UPDATE_CLIENTS);
            for(int y=1;y<=3;y++){var s=l.getBlockState(at.above(y));if(CalderPlayBlock.is(s,CalderPlayBlock.Kind.CHAIN))l.setBlock(at.above(y),s.setValue(CalderPlayBlock.STAGE,CalderPlayBlock.GONE),Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE);}
            return;
        }
        for(var pos:pair(l,at)){var s=l.getBlockState(pos);l.setBlock(pos,s.setValue(HouseReadPropBlock.TURNED,true),Block.UPDATE_CLIENTS);for(int y=1;y<=3;y++)if(l.getBlockState(pos.above(y)).is(HouseReadRegistry.ROPE.get()))l.setBlock(pos.above(y),Blocks.AIR.defaultBlockState(),Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE);}
        if(HouseReadSites.inside(p)==HouseReadSites.Room.PORCH)HouseReadSites.act(p,HouseReadSites.Room.PORCH,"sat on the swing");
    }
    /** Off the swing: the still seat and its ropes come back, up to the crossbar. */
    public static void restore(Level l,BlockPos at){
        if(pumped(l.getBlockState(at))){
            l.setBlock(at,l.getBlockState(at).setValue(CalderPlayBlock.STAGE,0),Block.UPDATE_CLIENTS);
            for(int y=1;y<=3;y++){var s=l.getBlockState(at.above(y));if(CalderPlayBlock.is(s,CalderPlayBlock.Kind.CHAIN))l.setBlock(at.above(y),s.setValue(CalderPlayBlock.STAGE,0),Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE);}
            return;
        }
        for(var pos:pair(l,at)){var s=l.getBlockState(pos);if(is(s))l.setBlock(pos,s.setValue(HouseReadPropBlock.TURNED,false),Block.UPDATE_CLIENTS);
            for(int y=1;y<=3;y++)if(l.getBlockState(pos.above(y)).isAir())l.setBlock(pos.above(y),HouseReadRegistry.ROPE.get().defaultBlockState().setValue(ChainBlock.AXIS,Direction.Axis.Y),Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE);}
    }
    /**
     * 0.4.474: a hand on somebody who is on a swing pushes them. The one pushing is on their feet, behind the swing or in front of
     * it; from beside it, from another seat, or with a ball of the park's sand in the hand (which is thrown), nothing is pushed.
     */
    @SubscribeEvent public static void hand(PlayerInteractEvent.EntityInteract e){
        if(e.getHand()!=InteractionHand.MAIN_HAND||!(e.getEntity() instanceof ServerPlayer p)||!(e.getTarget() instanceof ServerPlayer rider)||p==rider||p.isSpectator()||p.isPassenger())return;
        if(!(rider.getVehicle() instanceof SeatEntity seat)||!seat.swinging()||p.getMainHandItem().is(BikeNightRegistry.SAND_BALL.get()))return;
        var l=p.serverLevel();var pair=pair(l,seat.support());var state=l.getBlockState(pair.getFirst());if(!is(state))return;
        e.setCanceled(true);e.setCancellationResult(InteractionResult.SUCCESS);
        var forward=forward(state);var pivot=pivot(pair);double along=(p.getX()-pivot.x)*forward.getStepX()+(p.getZ()-pivot.z)*forward.getStepZ();
        if(Math.abs(along)<BESIDE){told(p,PUSH_BESIDE);return;}
        var did=seat.push(p.getUUID(),along<0?-1:1);if(did==null)return;
        switch(did){
            case FAR->told(p,PUSH_FAR);
            case CHECKED->{p.swing(InteractionHand.MAIN_HAND,true);told(p,PUSH_EARLY);}
            case SENT->{
                p.swing(InteractionHand.MAIN_HAND,true);
                if(seat.firstFrom(p.getUUID())){told(p,PUSH_HINT);told(rider,String.format(PUSHED_BY,p.getGameProfile().getName()));}
                if(pumped(state)){l.playSound(null,seat.getX(),seat.getY()+1.5,seat.getZ(),BikeNightRegistry.SWING_CREAK.get(),SoundSource.BLOCKS,.5F,1.15F);BikeNight.swingPushed(p,rider);}
            }
        }
    }
    private static void told(ServerPlayer p,String line){p.displayClientMessage(Component.literal(line).withStyle(ChatFormatting.GRAY,ChatFormatting.ITALIC),true);}
    /**
     * The reader jumped on a swing (SwingJumpPayload): they let go where the seat is and fly on with its speed. Returns the velocity
     * they left with, or null when they were not on a moving swing.
     */
    public static Vec3 jump(ServerPlayer p){
        if(!(p.getVehicle() instanceof SeatEntity seat)||!seat.swinging()||p.isSpectator())return null;
        var l=p.serverLevel();var pair=pair(l,seat.support());if(!is(l.getBlockState(pair.getFirst())))return null;
        var state=l.getBlockState(pair.getFirst());var forward=forward(state);var pivot=pivot(pair);
        // The seat says where it is on its arc and how fast it is going: by the time sat alone, or (a pumped one) by how wide its
        // rider has made the arc as well.
        var from=seat(pivot,forward,seat.swingAngle(0)).add(0,.05,0);
        var away=launch(seat(pivot,forward,seat.swingAngle(.5)).subtract(seat(pivot,forward,seat.swingAngle(-.5))),forward);
        seat.letGo(from);p.stopRiding();
        p.connection.teleport(from.x,from.y,from.z,p.getYRot(),p.getXRot());
        p.setDeltaMovement(away);p.hurtMarked=true;p.resetFallDistance();
        if(pumped(state))BikeNight.letGo(p,pivot,forward);
        return away;
    }
}
