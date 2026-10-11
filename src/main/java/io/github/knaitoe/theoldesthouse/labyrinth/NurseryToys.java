package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.NurseryBlock.Kind;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * Playing in the child's room (0.4.74). The toys answer a reader's hand: the train runs its loop, the top spins and falls,
 * the jack-in-the-box is wound until it pops, the music box plays, the bear squeaks and turns to you, the blocks stack and
 * fall, the ball rolls, the rocking horse rocks, the mobile turns. The furniture opens and has its say. All of it is shared:
 * a peer sees the top spin. None of it is progress. The room's own doors stay shut; the basement hatch opens only from below.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class NurseryToys {
    private interface Ticker{boolean tick();}
    private static final Map<BlockPos,Ticker> TICKERS=new HashMap<>();
    private static final Map<BlockPos,Integer> CRANKS=new HashMap<>();
    private static final Map<BlockPos,Integer> HATCHES=new HashMap<>();
    /** The jack-in-the-box's tune, a semitone above or below the chime's own note, one note per turn of the handle. */
    private static final int[] TUNE={0,0,2,2,4,7,4,0};
    private static final int F=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE;
    private NurseryToys(){}

    private static void say(ServerPlayer p,String line){p.displayClientMessage(Component.literal(line),true);}
    private static void sound(ServerLevel l,BlockPos at,SoundEvent s,float volume,float pitch){l.playSound(null,at,s,SoundSource.BLOCKS,volume,pitch);}
    private static void stage(ServerLevel l,BlockPos at,int stage){var s=l.getBlockState(at);if(s.is(LiteraryRegistry.NURSERY.get()))l.setBlock(at,s.setValue(NurseryBlock.STAGE,stage),F);}
    private static Direction toward(ServerPlayer p,BlockPos at){return Direction.getNearest(p.getX()-at.getX()-.5,0,p.getZ()-at.getZ()-.5);}
    private static boolean sideDoor(BlockPos r){return ChildRoom.EXITS.get(2).contains(r)||ChildRoom.EXITS.get(3).contains(r);}

    @SubscribeEvent(priority=EventPriority.HIGH) public static void use(PlayerInteractEvent.RightClickBlock e){
        if(!(e.getEntity() instanceof ServerPlayer p)||!LiteraryVignettes.inside(p,ChildRoom.PLACE))return;
        var b=ChildRoom.base(p.server);if(b==null)return;var l=p.serverLevel();var at=e.getPos();var r=at.subtract(b);var s=l.getBlockState(at);
        if(sideDoor(r)&&s.getBlock() instanceof DoorBlock){e.setCanceled(true);e.setCancellationResult(InteractionResult.FAIL);
            if(e.getHand()==InteractionHand.MAIN_HAND)say(p,"The handle turns, but the door will not open.");return;}
        if(r.equals(ChildRoom.HATCH)&&s.is(Blocks.IRON_TRAPDOOR)){e.setCanceled(true);e.setCancellationResult(InteractionResult.SUCCESS);
            if(e.getHand()==InteractionHand.MAIN_HAND)hatch(p,l,at,s,b);return;}
        if(!s.is(LiteraryRegistry.NURSERY.get()))return;var kind=s.getValue(NurseryBlock.KIND);
        // The bedtime card and the exercise book are read, not played with.
        if(kind==Kind.CARD||kind==Kind.ACCOUNT)return;
        e.setCanceled(true);
        // The client also tries the off hand; it must not set anything down on a toy.
        if(e.getHand()!=InteractionHand.MAIN_HAND){e.setCancellationResult(InteractionResult.FAIL);return;}
        e.setCancellationResult(InteractionResult.SUCCESS);play(p,l,at,s,b);
    }
    /** What a toy or a piece of furniture does when a reader's hand is on it. */
    public static void play(ServerPlayer p,ServerLevel l,BlockPos at,BlockState s,BlockPos b){
        var kind=s.getValue(NurseryBlock.KIND);int stage=s.getValue(NurseryBlock.STAGE);var key=at.immutable();
        if(kind.ceilingToy()&&stage==NurseryBlock.CEILING){say(p,"It is stuck fast to the ceiling, upside down.");return;}
        switch(kind){
            case TRAIN->train(l,b);
            case TOP->{if(stage==0&&!TICKERS.containsKey(key)){sound(l,at,LiteraryRegistry.NURSERY_TOP.value(),.8F,1F);spin(l,key);}
                else if(stage==1){stage(l,at,0);sound(l,at,LiteraryRegistry.NURSERY_CREAK.value(),.4F,1.6F);}}
            case JACK_BOX->{if(stage==1){stage(l,at,0);sound(l,at,LiteraryRegistry.NURSERY_CREAK.value(),.5F,1.3F);break;}
                int turn=CRANKS.merge(key,1,Integer::sum);sound(l,at,LiteraryRegistry.NURSERY_CHIME.value(),.7F,(float)Math.pow(2,TUNE[(turn-1)%TUNE.length]/12.0));
                if(turn>=TUNE.length){CRANKS.remove(key);stage(l,at,1);sound(l,at,LiteraryRegistry.NURSERY_POP.value(),1F,1F);}}
            case MUSIC_BOX->{if(stage==0){stage(l,at,1);sound(l,at,LiteraryRegistry.NURSERY_MUSIC_BOX.value(),.8F,1F);int[] left={160};
                    TICKERS.put(key,()->{if(--left[0]>0&&NurseryBlock.is(l.getBlockState(key),Kind.MUSIC_BOX)&&l.getBlockState(key).getValue(NurseryBlock.STAGE)==1)return false;stage(l,key,0);return true;});}
                else{TICKERS.remove(key);stage(l,at,0);}}
            case TEDDY->{l.setBlock(at,s.setValue(NurseryBlock.FACING,toward(p,at)),F);sound(l,at,LiteraryRegistry.NURSERY_SQUEAK.value(),.8F,.9F+l.getRandom().nextFloat()*.2F);}
            case DOLL->{l.setBlock(at,s.setValue(NurseryBlock.FACING,toward(p,at)),F);sound(l,at,LiteraryRegistry.NURSERY_CREAK.value(),.35F,1.8F);}
            case BLOCKS->{int next=(stage+1)%3;stage(l,at,next);if(next==0)sound(l,at,LiteraryRegistry.NURSERY_BLOCKS.value(),.9F,1F);else sound(l,at,LiteraryRegistry.NURSERY_BLOCKS.value(),.35F,1.5F);}
            case BALL->{if(!TICKERS.containsKey(key))roll(l,b,key,p.getDirection());}
            case ROCKING_HORSE->{if(!TICKERS.containsKey(key))rock(l,key);}
            case MOBILE->{if(!TICKERS.containsKey(key)){sound(l,at,LiteraryRegistry.NURSERY_CHIME.value(),.4F,1.5F);turn(l,key,40,4);}}
            case NIGHT_LIGHT->{stage(l,at,stage==0?1:0);sound(l,at,SoundEvents.STONE_BUTTON_CLICK_ON,.4F,1.4F);}
            case TOY_CHEST->{stage(l,at,stage==0?1:0);sound(l,at,LiteraryRegistry.NURSERY_CREAK.value(),.6F,.9F);
                if(stage==0)say(p,LiteraryVignettes.shared(LabyrinthData.get(p.server),ChildRoom.PLACE).getInt("Exits0474")==0
                        ?"Picture books, a cap gun, a sock with no partner.":"Everything that was in here is somewhere else now.");}
            case DOLLHOUSE->{stage(l,at,stage==0?1:0);sound(l,at,LiteraryRegistry.NURSERY_CREAK.value(),.5F,1.2F);
                if(stage==0)say(p,"In the little house the bed has been pushed aside. There is a gap in the floor beneath it.");}
            case WARDROBE_LOW,WARDROBE_HIGH->{var other=kind==Kind.WARDROBE_LOW?at.above():at.below();stage(l,at,stage==0?1:0);stage(l,other,stage==0?1:0);
                sound(l,at,LiteraryRegistry.NURSERY_CREAK.value(),.7F,.8F);if(stage==0)say(p,"Small clothes on small hangers. Nothing else.");}
            case SHELF->say(p,"Picture books. In one, a house where every door is drawn on the wall.");
            case BED_HEAD,BED_FOOT->say(p,"The quilt is cold and smooth, as if nobody has slept under it.");
            case DESK->say(p,"Crayons, a ruler, a drawing of the room with no door in it.");
            case DRAWINGS->say(p,stage==0?"A house in crayon. It has windows, but no door.":"The same house, from underneath. A ladder goes up to where the door is.");
            case BOXES->say(p,"OUTGROWN, in marker. Clothes, and a box of baby things.");
            case CRIB->say(p,"The crib has been taken apart and put back together wrong.");
            default->{}
        }
    }

    // ------------------------------------------------------------------------------------------------ the toys that move
    private static void train(ServerLevel l,BlockPos b){
        var key=b.offset(ChildRoom.TRACK.get(0)).immutable();if(TICKERS.containsKey(key))return;int start=-1;
        for(int i=0;i<ChildRoom.TRACK.size();i++)if(NurseryBlock.is(l.getBlockState(b.offset(ChildRoom.TRACK.get(i))),Kind.TRAIN)){start=i;break;}
        if(start<0)return;sound(l,b.offset(ChildRoom.TRACK.get(start)),LiteraryRegistry.NURSERY_WHISTLE.value(),.7F,1F);
        int[] state={start,ChildRoom.TRACK.size(),2};var track=ChildRoom.TRACK;
        TICKERS.put(key,()->{if(--state[2]>0)return false;state[2]=4;
            var here=b.offset(track.get(state[0]));int n=(state[0]+1)%track.size();var next=b.offset(track.get(n));
            if(!NurseryBlock.is(l.getBlockState(here),Kind.TRAIN)||!NurseryBlock.is(l.getBlockState(next),Kind.TRACK))return true;
            var onward=track.get((n+1)%track.size());var travel=Direction.getNearest(track.get(n).getX()-track.get(state[0]).getX(),0,track.get(n).getZ()-track.get(state[0]).getZ());
            l.setBlock(here,l.getBlockState(here).setValue(NurseryBlock.KIND,Kind.TRACK).setValue(NurseryBlock.FACING,travel),F);
            l.setBlock(next,l.getBlockState(next).setValue(NurseryBlock.KIND,Kind.TRAIN).setValue(NurseryBlock.FACING,Direction.getNearest(onward.getX()-track.get(n).getX(),0,onward.getZ()-track.get(n).getZ())),F);
            sound(l,next,LiteraryRegistry.NURSERY_TRAIN.value(),.5F,.95F+l.getRandom().nextFloat()*.1F);
            l.sendParticles(ParticleTypes.SMOKE,next.getX()+.5,next.getY()+.6,next.getZ()+.5,1,.05,.05,.05,.005);
            state[0]=n;return --state[1]<=0;});
    }
    private static void spin(ServerLevel l,BlockPos at){int[] left={60};
        TICKERS.put(at,()->{if(!NurseryBlock.is(l.getBlockState(at),Kind.TOP))return true;var s=l.getBlockState(at);
            if(--left[0]>0){if(left[0]%2==0)l.setBlock(at,s.setValue(NurseryBlock.FACING,s.getValue(NurseryBlock.FACING).getClockWise()),F);return false;}
            l.setBlock(at,s.setValue(NurseryBlock.STAGE,1).setValue(NurseryBlock.FACING,Direction.Plane.HORIZONTAL.getRandomDirection(l.getRandom())),F);
            sound(l,at,LiteraryRegistry.NURSERY_BLOCKS.value(),.3F,1.8F);return true;});
    }
    private static void rock(ServerLevel l,BlockPos at){int[] left={60};
        TICKERS.put(at,()->{if(!NurseryBlock.is(l.getBlockState(at),Kind.ROCKING_HORSE))return true;
            if(--left[0]>0){if(left[0]%6==0)stage(l,at,(left[0]/6)%2==0?1:2);if(left[0]%12==0)sound(l,at,LiteraryRegistry.NURSERY_CREAK.value(),.5F,.7F);return false;}
            stage(l,at,0);return true;});
    }
    private static void turn(ServerLevel l,BlockPos at,int ticks,int every){int[] left={ticks};
        TICKERS.put(at,()->{var s=l.getBlockState(at);if(!s.is(LiteraryRegistry.NURSERY.get()))return true;
            if(--left[0]%every==0)l.setBlock(at,s.setValue(NurseryBlock.FACING,s.getValue(NurseryBlock.FACING).getClockWise()),F);return left[0]<=0;});
    }
    /** The ball rolls up to four blocks the way the reader is facing, over the floor, until something stops it. */
    private static void roll(ServerLevel l,BlockPos b,BlockPos from,Direction way){BlockPos[] at={from};int[] state={4,1};
        TICKERS.put(from,()->{if(--state[1]>0)return false;state[1]=3;var here=at[0];var s=l.getBlockState(here);if(!NurseryBlock.is(s,Kind.BALL))return true;
            var next=here.relative(way);var r=next.subtract(b);
            boolean room=r.getY()==0&&r.getX()>=-10&&r.getX()<=10&&r.getZ()>=-23&&r.getZ()<=-1;
            if(state[0]--<=0||!room||!l.getBlockState(next).isAir()||l.getBlockState(next.below()).getCollisionShape(l,next.below()).isEmpty())return true;
            l.setBlock(here,Blocks.AIR.defaultBlockState(),F);l.setBlock(next,s.setValue(NurseryBlock.FACING,way),F);at[0]=next;
            sound(l,next,LiteraryRegistry.NURSERY_BALL.value(),.6F,.9F+l.getRandom().nextFloat()*.3F);return false;});
    }

    // ------------------------------------------------------------------------------------------------ the hatch
    private static void hatch(ServerPlayer p,ServerLevel l,BlockPos at,BlockState s,BlockPos b){
        if(p.getY()>=b.getY()-.5){say(p,"It is fastened from underneath.");return;}
        if(!s.getValue(TrapDoorBlock.OPEN)){l.setBlock(at,s.setValue(TrapDoorBlock.OPEN,true),Block.UPDATE_ALL);sound(l,at,SoundEvents.IRON_TRAPDOOR_OPEN,.8F,1F);}
        HATCHES.put(at.immutable(),0);
    }
    /** The hatch shuts itself behind a reader, once nothing is in it, under it or standing over it. */
    private static void hatches(net.minecraft.server.MinecraftServer s){
        var b=ChildRoom.base(s);var l=s.getLevel(NovelRooms.dimension(ChildRoom.PLACE));if(b==null||l==null)return;var at=b.offset(ChildRoom.HATCH);
        if(!l.hasChunkAt(at))return;var state=l.getBlockState(at);
        if(!state.is(Blocks.IRON_TRAPDOOR)||!state.getValue(TrapDoorBlock.OPEN)){HATCHES.remove(at);return;}
        int open=HATCHES.merge(at,20,Integer::sum);
        boolean clear=l.getEntitiesOfClass(LivingEntity.class,new AABB(at.below()).expandTowards(0,2,0).inflate(.1),e->e.isAlive()).isEmpty();
        if(open>=60&&clear){l.setBlock(at,state.setValue(TrapDoorBlock.OPEN,false),Block.UPDATE_ALL);sound(l,at,SoundEvents.IRON_TRAPDOOR_CLOSE,.7F,1F);HATCHES.remove(at);}
    }

    @SubscribeEvent public static void tick(ServerTickEvent.Post e){
        if(!TICKERS.isEmpty())for(var key:List.copyOf(TICKERS.keySet())){var t=TICKERS.get(key);if(t!=null&&t.tick())TICKERS.remove(key,t);}
        if(e.getServer().getTickCount()%20==3)hatches(e.getServer());
    }
    public static void clearAll(){TICKERS.clear();CRANKS.clear();HATCHES.clear();}
    /** For tests: whether a toy is still moving. */
    public static boolean busy(BlockPos at){return TICKERS.containsKey(at);}
}
