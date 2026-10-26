package io.github.knaitoe.theoldesthouse.labyrinth;

import com.mojang.serialization.MapCodec;
import java.util.Locale;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.*;
import net.minecraft.util.*;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.*;
import net.minecraft.world.phys.shapes.*;

/** The point touched selects an actual cup, bottle, tonearm or knob. State lives in the chunk. */
public final class DomesticFittingBlock extends HorizontalDirectionalBlock {
    public static final MapCodec<DomesticFittingBlock> CODEC=simpleCodec(DomesticFittingBlock::new);
    public enum Kind implements StringRepresentable {
        BREAKFAST,RECORD_PLAYER,BOTTLES,RADIO,TOPPLED_CHAIR,TOPPLED_TABLE,SCRAPES,PACKED_CLOTHES,BOOK_BOX,CLOSET_LOWER,CLOSET_UPPER,DESK_REMNANTS,EMPTY_BOTTLES,DIRTY_DISHES,PHOTO_FAMILY,PHOTO_COUPLE,BOTTLE_RETURNS,FOLDED_SHIRT;
        public String getSerializedName(){return name().toLowerCase(Locale.ROOT);}
        public boolean usable(){return switch(this){case BREAKFAST,RECORD_PLAYER,BOTTLES,RADIO,PACKED_CLOTHES,BOOK_BOX,CLOSET_LOWER,CLOSET_UPPER,DESK_REMNANTS,EMPTY_BOTTLES,PHOTO_FAMILY,PHOTO_COUPLE,BOTTLE_RETURNS->true;default->false;};}
    }
    public static final EnumProperty<Kind> KIND=EnumProperty.create("kind",Kind.class);
    public static final IntegerProperty STAGE=IntegerProperty.create("stage",0,15);
    public DomesticFittingBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(KIND,Kind.BREAKFAST).setValue(FACING,Direction.NORTH).setValue(STAGE,0));}
    @Override protected MapCodec<? extends HorizontalDirectionalBlock> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(KIND,FACING,STAGE);}
    public static BlockState of(Kind k,Direction f){return DomesticRegistry.FITTING.get().defaultBlockState().setValue(KIND,k).setValue(FACING,f);}
    public static int light(BlockState s){return s.getValue(KIND)==Kind.RADIO&&(s.getValue(STAGE)&4)!=0?2:0;}
    private static Vec3 local(BlockState s,BlockPos at,Vec3 hit){return VignetteHandlingBlock.local(at,s.getValue(FACING),hit);}
    @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos at,CollisionContext c){
        var result=Shapes.empty();int turns=VignetteHandlingBlock.turns(s.getValue(FACING));
        for(var b:DomesticFittingShapes.boxes(s.getValue(KIND),s.getValue(STAGE))){
            double x0=b[0],z0=b[2],x1=b[3],z1=b[5];
            for(int i=0;i<turns;i++){double a=16-z1,e=16-z0;z0=x0;z1=x1;x0=a;x1=e;}
            result=Shapes.or(result,Block.box(x0,b[1],z0,x1,b[4],z1));
        }
        return result.optimize();
    }
    @Override protected VoxelShape getOcclusionShape(BlockState s,BlockGetter l,BlockPos at){return Shapes.empty();}
    @Override protected int getLightBlock(BlockState s,BlockGetter l,BlockPos at){return 0;}
    @Override protected BlockState rotate(BlockState s,Rotation r){return s.setValue(FACING,r.rotate(s.getValue(FACING)));}
    @Override protected BlockState mirror(BlockState s,Mirror m){return s.rotate(m.getRotation(s.getValue(FACING)));}
    @Override protected InteractionResult useWithoutItem(BlockState s,Level level,BlockPos at,Player p,BlockHitResult hit){
        var kind=s.getValue(KIND);
        if(!kind.usable()||p.isSpectator()||!p.isAlive()||p.distanceToSqr(at.getCenter())>16)return InteractionResult.PASS;
        if(level.isClientSide)return InteractionResult.SUCCESS;
        var point=local(s,at,hit.getLocation());int old=s.getValue(STAGE),next=old;
        var sound=SoundEvents.WOODEN_BUTTON_CLICK_ON;float pitch=.9F;
        switch(kind){
            case BREAKFAST->{int cup=Mth.clamp((int)(point.x*3),0,2);next=(old&7)^(1<<cup);sound=SoundEvents.DECORATED_POT_PLACE;pitch=1.1F+cup*.1F;}
            case BOTTLES->{int bottle=Mth.clamp((int)(point.x*3),0,2);next=8|((old&7)^(1<<bottle));sound=SoundEvents.DECORATED_POT_PLACE;pitch=.7F+bottle*.12F;}
            case RECORD_PLAYER->{
                if(point.z>.76){next=(old^1)&~2;}
                else if((old&1)!=0){if(point.x<.25){if((old&2)==0)next=old^4;}else next=old^2;}
                sound=SoundEvents.WOODEN_TRAPDOOR_CLOSE;pitch=.65F;
            }
            case RADIO->{next=point.x<.4?old^4:(old&4)|(((old&3)+1)%4);sound=SoundEvents.LEVER_CLICK;pitch=.65F+(next&3)*.1F;}
            case PACKED_CLOTHES,BOOK_BOX,BOTTLE_RETURNS->{next=(old&1)^1;sound=SoundEvents.WOOL_PLACE;pitch=.8F;}
            case CLOSET_LOWER,CLOSET_UPPER->{next=(old&1)^1;sound=(next&1)==0?SoundEvents.WOODEN_DOOR_CLOSE:SoundEvents.WOODEN_DOOR_OPEN;pitch=.7F;}
            case DESK_REMNANTS->{next=(old&1)^1;sound=SoundEvents.WOODEN_BUTTON_CLICK_OFF;pitch=.8F;}
            case EMPTY_BOTTLES->{next=(old+1)%3;sound=SoundEvents.GLASS_HIT;pitch=.65F;}
            case PHOTO_FAMILY,PHOTO_COUPLE->{next=(old&1)^1;sound=SoundEvents.WOODEN_BUTTON_CLICK_OFF;pitch=.65F;}

            default->{}
        }
        if(next==old)return InteractionResult.CONSUME;
        var changed=s.setValue(STAGE,next);
        // Never swing a lid through a real body leaning over the cabinet.
        var newShape=changed.getCollisionShape(level,at);
        for(var box:newShape.toAabbs())if(!level.getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class,box.move(at),e->e.isAlive()&&!e.isSpectator()).isEmpty())return InteractionResult.CONSUME;
        BlockPos other=null;BlockState otherState=null;
        if(kind==Kind.CLOSET_LOWER||kind==Kind.CLOSET_UPPER){
            other=kind==Kind.CLOSET_LOWER?at.above():at.below();var mate=level.getBlockState(other);
            var expected=kind==Kind.CLOSET_LOWER?Kind.CLOSET_UPPER:Kind.CLOSET_LOWER;
            if(mate.is(DomesticRegistry.FITTING.get())&&mate.getValue(KIND)==expected&&mate.getValue(FACING)==s.getValue(FACING)){
                otherState=mate.setValue(STAGE,next);
                for(var box:otherState.getCollisionShape(level,other).toAabbs())if(!level.getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class,box.move(other),e->e.isAlive()&&!e.isSpectator()).isEmpty())return InteractionResult.CONSUME;
            }
        }
        level.setBlock(at,changed,3);if(otherState!=null)level.setBlock(other,otherState,3);level.playSound(null,at,sound,SoundSource.BLOCKS,.16F,pitch);
        if(kind==Kind.RADIO&&(next&4)!=0||kind==Kind.RECORD_PLAYER&&(next&3)==3)level.scheduleTick(at,this,20);
        return InteractionResult.CONSUME;
    }
    @Override protected void tick(BlockState s,ServerLevel l,BlockPos at,RandomSource random){
        var kind=s.getValue(KIND);int stage=s.getValue(STAGE);
        boolean record=kind==Kind.RECORD_PLAYER&&(stage&3)==3;
        if(!record&&!(kind==Kind.RADIO&&(stage&4)!=0))return;
        boolean listener=l.players().stream().anyMatch(p->!p.isSpectator()&&p.distanceToSqr(at.getCenter())<100);
        if(listener){
            int beat=(int)(l.getGameTime()/20)%8;int[] notes=record?new int[]{0,4,7,4,2,5,9,5}:new int[]{0,7,4,7,2,9,5,9};
            if(record||(stage&3)==2)l.playSound(null,at,SoundEvents.NOTE_BLOCK_GUITAR.value(),SoundSource.BLOCKS,.11F,(float)Math.pow(2,(notes[beat]-12)/12F));
            else if(beat%3==0)l.playSound(null,at,SoundEvents.FIRE_EXTINGUISH,SoundSource.BLOCKS,.035F,.65F+(stage&3)*.2F);
        }
        l.scheduleTick(at,this,listener?20:80);
    }
}
