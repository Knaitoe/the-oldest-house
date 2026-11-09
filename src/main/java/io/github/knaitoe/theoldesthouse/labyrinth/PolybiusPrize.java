package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import io.github.knaitoe.theoldesthouse.journal.Grounding;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

/**
 * 0.4.411 (owner, on the arcade's prizes: "Massive disappointment that I couldn't snap the bracelet onto my wrist. I'm assuming other
 * items are similarly missing interactions."): every prize behind the counter does something, with its own animation.
 * <ul>
 * <li>The rubber spider is set down on a floor or a table, and jumps with a squeak when someone walks up to it.</li>
 * <li>The finger trap takes both hands. Pulling tightens it; crouching pushes the fingers together, and it lets go.</li>
 * <li>The bouncy ball is thrown, and bounces off everything until it comes to rest.</li>
 * <li>The glow star sticks to any face of a block and glows.</li>
 * <li>The slap bracelet is held up straight, then slapped round the wrist of the hand holding it, and stays there for everyone to see.</li>
 * <li>The kazoo is played at the mouth; looking up plays higher. A reader whose mouth the machine took gets no sound from it.</li>
 * <li>The mood ring is slipped onto a finger, and its stone changes with how its wearer is.</li>
 * <li>The plush dog is hugged, or sat down on a floor or a bed.</li>
 * </ul>
 * Crouching and using an empty hand takes a bracelet or the ring off again (PrizeWear). The set-down prizes are picked up by hand.
 */
public class PolybiusPrize extends Item {
    public enum Kind{SPIDER,FINGER_TRAP,BOUNCY_BALL,GLOW_STAR,SLAP_BRACELET,KAZOO,MOOD_RING,PLUSH_DOG}
    /** How long the bracelet is held up before it snaps, and the ring takes to slip on; a hug's length; ticks between kazoo notes. */
    public static final int SNAP=10,SLIP=8,HUG=60,NOTE=4,TIGHTEN=20,MOST=3;
    /** A major pentatonic scale over two octaves, as Minecraft pitches of the kazoo's one recorded note. */
    private static final int[] SCALE={0,2,4,7,9,12,14,16,19,21,24};
    public final Kind kind;
    public PolybiusPrize(Kind kind,Properties properties){super(properties);this.kind=kind;}

    public static boolean is(ItemStack stack,Kind kind){return stack.getItem() instanceof PolybiusPrize p&&p.kind==kind;}

    @Override public InteractionResultHolder<ItemStack> use(Level level,Player p,InteractionHand hand){
        var stack=p.getItemInHand(hand);
        switch(kind){
            case BOUNCY_BALL->{
                level.playSound(null,p.getX(),p.getY(),p.getZ(),SoundEvents.SNOWBALL_THROW,SoundSource.PLAYERS,.5F,1.3F);
                if(!level.isClientSide){var ball=new PrizeBall(level,p);ball.setItem(stack.copyWithCount(1));ball.shootFromRotation(p,p.getXRot(),p.getYRot(),0F,.9F,.6F);level.addFreshEntity(ball);}
                stack.consume(1,p);p.swing(hand);
                return InteractionResultHolder.sidedSuccess(stack,level.isClientSide);
            }
            case SLAP_BRACELET,MOOD_RING->{
                var arm=arm(p,hand);
                if(!PrizeWear.free(p,arm,kind)){
                    if(!level.isClientSide)p.displayClientMessage(Component.literal(kind==Kind.MOOD_RING?"That hand already wears a ring.":"That wrist already wears one."),true);
                    return InteractionResultHolder.fail(stack);
                }
                p.startUsingItem(hand);return InteractionResultHolder.consume(stack);
            }
            case KAZOO,FINGER_TRAP,PLUSH_DOG->{p.startUsingItem(hand);return InteractionResultHolder.consume(stack);}
            default->{return InteractionResultHolder.pass(stack);}
        }
    }

    /** The spider and the dog are set down on a floor or a table; the star sticks to any face. */
    @Override public InteractionResult useOn(UseOnContext c){
        Direction face=c.getClickedFace();
        boolean sets=kind==Kind.GLOW_STAR||(kind==Kind.SPIDER||kind==Kind.PLUSH_DOG)&&face==Direction.UP;
        if(!sets||c.getPlayer()==null)return InteractionResult.PASS;
        Level level=c.getLevel();BlockPos support=c.getClickedPos();BlockPos at=support.relative(face);
        if(!level.getBlockState(support).isFaceSturdy(level,support,face)&&kind!=Kind.SPIDER&&kind!=Kind.PLUSH_DOG)return InteractionResult.PASS;
        if(!level.getBlockState(at).getCollisionShape(level,at).isEmpty()&&!level.getBlockState(at).canBeReplaced())return InteractionResult.PASS;
        if(!level.getEntitiesOfClass(PrizeProp.class,new AABB(at)).isEmpty())return InteractionResult.FAIL;
        if(!level.isClientSide){
            float yaw=Mth.wrapDegrees(c.getPlayer().getYRot()+180F);
            PrizeProp.place(level,kind,support,face,yaw,c.getClickLocation());
            level.playSound(null,at,kind==Kind.GLOW_STAR?SoundEvents.ITEM_FRAME_ADD_ITEM:PolybiusRegistry.SQUEAK.get(),SoundSource.PLAYERS,.6F,kind==Kind.PLUSH_DOG?.8F:1.3F);
        }
        c.getItemInHand().consume(1,c.getPlayer());
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override public int getUseDuration(ItemStack stack,LivingEntity e){
        return switch(kind){case SLAP_BRACELET->SNAP;case MOOD_RING->SLIP;case KAZOO,FINGER_TRAP,PLUSH_DOG->72000;default->0;};
    }
    @Override public UseAnim getUseAnimation(ItemStack stack){return kind==Kind.KAZOO?UseAnim.TOOT_HORN:UseAnim.NONE;}

    @Override public void onUseTick(Level level,LivingEntity e,ItemStack stack,int left){
        if(level.isClientSide||!(e instanceof ServerPlayer p))return;
        int held=getUseDuration(stack,e)-left;
        switch(kind){
            case KAZOO->{
                if(held%NOTE!=0)return;
                if(FaceLoss.faceless(p)){if(held==0)p.displayClientMessage(Component.literal("No sound comes."),true);return;}
                level.playSound(null,p.getX(),p.getEyeY(),p.getZ(),PolybiusRegistry.KAZOO.get(),SoundSource.PLAYERS,.8F,kazooPitch(p.getXRot()));
            }
            case FINGER_TRAP->{
                if(held==0){level.playSound(null,p.blockPosition(),PolybiusRegistry.CREAK.get(),SoundSource.PLAYERS,.4F,1.4F);return;}
                if(held%TIGHTEN!=0)return;
                int pull=pull(stack);
                if(p.isCrouching()){
                    if(pull==0)return;
                    setPull(stack,pull-1);
                    if(pull-1==0){level.playSound(null,p.blockPosition(),PolybiusRegistry.POP.get(),SoundSource.PLAYERS,.6F,1F);p.displayClientMessage(Component.literal("Your fingers come free."),true);p.releaseUsingItem();}
                    else level.playSound(null,p.blockPosition(),PolybiusRegistry.CREAK.get(),SoundSource.PLAYERS,.4F,1.2F);
                }else if(pull<MOST){
                    setPull(stack,pull+1);level.playSound(null,p.blockPosition(),PolybiusRegistry.CREAK.get(),SoundSource.PLAYERS,.5F,.8F+.1F*pull);
                    if(pull+1==MOST)p.displayClientMessage(Component.literal("The harder you pull, the tighter it holds."),true);
                }
            }
            case PLUSH_DOG->{
                if(held==0||held%40==0)level.playSound(null,p.blockPosition(),PolybiusRegistry.SQUEAK.get(),SoundSource.PLAYERS,.35F,.75F);
                if(held==HUG)Grounding.ease(p);
            }
            default->{}
        }
    }

    @Override public ItemStack finishUsingItem(ItemStack stack,Level level,LivingEntity e){
        if((kind==Kind.SLAP_BRACELET||kind==Kind.MOOD_RING)&&e instanceof Player p){
            var arm=arm(p,p.getUsedItemHand());
            if(!level.isClientSide&&p instanceof ServerPlayer sp&&PrizeWear.putOn(sp,arm,kind)){
                level.playSound(null,p.getX(),p.getY()+1,p.getZ(),kind==Kind.SLAP_BRACELET?PolybiusRegistry.SLAP.get():SoundEvents.ARMOR_EQUIP_GOLD.value(),SoundSource.PLAYERS,.8F,1F);
                p.swing(p.getUsedItemHand(),true);
                stack.consume(1,p);
            }
        }
        return stack;
    }

    @Override public void releaseUsing(ItemStack stack,Level level,LivingEntity e,int left){
        if(kind==Kind.FINGER_TRAP&&!level.isClientSide&&e instanceof ServerPlayer p&&pull(stack)>0)
            p.displayClientMessage(Component.literal("It holds on. Crouch to push your fingers together."),true);
    }

    /** The arm a worn prize goes on: the arm of the hand that held it. */
    public static HumanoidArm arm(Player p,InteractionHand hand){return hand==InteractionHand.MAIN_HAND?p.getMainArm():p.getMainArm().getOpposite();}

    /** How tight the finger trap is, 0 to 3. */
    public static int pull(ItemStack stack){var data=stack.get(DataComponents.CUSTOM_DATA);return data==null?0:Mth.clamp(data.copyTag().getInt("Pull"),0,MOST);}
    static void setPull(ItemStack stack,int pull){CustomData.update(DataComponents.CUSTOM_DATA,stack,t->{if(pull<=0)t.remove("Pull");else t.putInt("Pull",pull);});}

    /** The kazoo's pitch from where the player looks: level is the middle of the scale, straight up its top. */
    public static float kazooPitch(float xRot){
        int step=Mth.clamp(Math.round((-xRot+90F)/180F*(SCALE.length-1)),0,SCALE.length-1);
        return (float)Math.pow(2,(SCALE[step]-12)/12.0);
    }

    /**
     * The mood ring's stone, 0 to 4: violet when its wearer is whole and somewhere ordinary, blue when whole inside the House, green
     * when a little hurt, amber when badly hurt, black while being hurt and on no finger at all.
     */
    public static int mood(LivingEntity e){
        if(e==null)return 4;
        if(e.hurtTime>0)return 4;
        float health=e.getHealth()/Math.max(1F,e.getMaxHealth());
        if(health<.4F)return 3;
        if(health<.8F)return 2;
        return HouseDimensions.isHouseDimension(e.level().dimension())?1:0;
    }

    /** A record of what the tag holds, for the tests. */
    public static CompoundTag tag(ItemStack stack){var data=stack.get(DataComponents.CUSTOM_DATA);return data==null?new CompoundTag():data.copyTag();}
}
