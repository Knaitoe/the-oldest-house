package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.*;
import io.github.knaitoe.theoldesthouse.network.*;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.*;
import net.minecraft.sounds.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.*;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.*;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/**
 * Five actual fires each take one leaf of the explorer's own story. The unlit descent remains impassable per explorer.
 *
 * <p>0.4.595 (docs/STAIR_NIGHTS_0_4_595.md): the five fires are the nights of the descent. They stood on the first five landings,
 * all within eighty blocks of the door. Each now stands at a camp, and the camps are spread down the whole stair ({@link #NIGHTS}).
 * A reader goes no further than the camp whose fire is still cold for them. A reader who has not taken their House of Leaves
 * from the shelf goes no further than the first landing, so nobody learns at the first camp that the book is far above them.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class StaircaseFire {
    public static final int REQUIRED=5;
    /** The corner landings the five camps stand on, counted down from the door (the first landing is 0). */
    public static final int[] NIGHTS={9,25,40,56,71};
    /** How far a reader sees on the stair, in blocks: the book's light. It is the same at every depth. */
    public static final float SIGHT=12;
    /** Sent as the sight where the stair's dark does not apply: off the stair ({@link #off}). */
    public static final float CLEAR=32;
    private static final String DRESS="staircase_nights_0595";
    /** The stair's corner landings and the five camps among them, for one origin. */
    private record Plan(BlockPos origin,List<BlockPos> corners,int[] cornerSteps,List<BlockPos> camps,int[] campSteps){}
    private static volatile Plan plan;
    public static final String LEAVES="StaircaseLeaves";
    private static final int F=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE;
    private StaircaseFire(){}
    public static BlockPos shelf(BlockPos origin){return FinaleArchitecture.base(origin).offset(-10,FinaleArchitecture.TOP,20);}
    /** Move the original lectern and its components outside the arrival copy; a missing authored display is repaired once. */
    public static boolean moveShelf(ServerLevel level,BlockPos origin){
        BlockPos from=FinaleArchitecture.base(origin).offset(2,FinaleArchitecture.TOP,32),to=shelf(origin);
        if(level.getBlockState(to).is(Blocks.LECTERN))return true;
        if(!level.getBlockState(to).isAir()||!Blocks.LECTERN.defaultBlockState().canSurvive(level,to))return false;
        if(level.getBlockEntity(from) instanceof net.minecraft.world.level.block.entity.LecternBlockEntity old){
            var saved=old.saveWithFullMetadata(level.registryAccess());var block=level.getBlockState(from);
            level.setBlock(to,block,F);saved.putInt("x",to.getX());saved.putInt("y",to.getY());saved.putInt("z",to.getZ());
            if(level.getBlockEntity(to)!=null)level.getBlockEntity(to).loadWithComponents(saved,level.registryAccess());
            level.removeBlockEntity(from);level.setBlock(from,Blocks.AIR.defaultBlockState(),F);
        }else{
            level.setBlock(to,Blocks.LECTERN.defaultBlockState().setValue(LecternBlock.FACING,Direction.EAST),F);
            if(level.getBlockEntity(to) instanceof net.minecraft.world.level.block.entity.LecternBlockEntity lectern){
                lectern.setBook(book(new UUID(0,0),REQUIRED));level.setBlock(to,level.getBlockState(to).setValue(LecternBlock.HAS_BOOK,true),F);
            }
        }
        return level.getBlockEntity(to) instanceof net.minecraft.world.level.block.entity.LecternBlockEntity;
    }
    private static Plan plan(BlockPos origin){
        var known=plan;if(known!=null&&known.origin.equals(origin))return known;
        var route=FinaleArchitecture.staircaseRoute(origin);var b=FinaleArchitecture.base(origin);
        var corners=new ArrayList<BlockPos>();var steps=new ArrayList<Integer>();
        for(int i=0;i<route.size();i++){var p=route.get(i);
            if(Math.abs(p.getX()-b.getX())==FinaleArchitecture.STAIR_RADIUS&&Math.abs(p.getZ()-b.getZ())==FinaleArchitecture.STAIR_RADIUS){corners.add(p);steps.add(i);}}
        var camps=new ArrayList<BlockPos>();int[] campSteps=new int[REQUIRED];
        for(int k=0;k<REQUIRED;k++){camps.add(corners.get(NIGHTS[k]));campSteps[k]=steps.get(NIGHTS[k]);}
        known=new Plan(origin.immutable(),List.copyOf(corners),steps.stream().mapToInt(Integer::intValue).toArray(),List.copyOf(camps),campSteps);
        plan=known;return known;
    }
    public static void clearCache(){plan=null;}
    /** Every corner landing of the stair above the cell, from the door down. */
    public static List<BlockPos> corners(BlockPos origin){return plan(origin).corners;}
    /** The five camps' landings, from the first night to the last. */
    public static List<BlockPos> landings(BlockPos origin){return plan(origin).camps;}
    /** The first landing below the door. */
    public static BlockPos threshold(BlockPos origin){return plan(origin).corners.getFirst();}
    public static List<BlockPos> braziers(BlockPos origin){
        var b=FinaleArchitecture.base(origin);
        return landings(origin).stream().map(p->p.offset(Integer.signum(p.getX()-b.getX())*3,0,Integer.signum(p.getZ()-b.getZ())*3)).toList();
    }
    public static int flames(CompoundTag record){return Math.max(0,Math.min(REQUIRED,record.getInt("StairFires")));}
    public static boolean open(CompoundTag record){return flames(record)==REQUIRED;}
    /** Where the dark stops a reader who carries their story: twelve steps past the camp whose fire is still cold for them. */
    public static BlockPos edge(BlockPos origin,CompoundTag record){
        var route=FinaleArchitecture.staircaseRoute(origin);
        if(open(record))return route.getLast();
        return route.get(Math.min(route.size()-1,plan(origin).campSteps[flames(record)]+12));
    }
    /** Where the dark stops this reader: with no House of Leaves of their own yet, twelve steps past the first landing. */
    public static BlockPos reach(BlockPos origin,CompoundTag record,boolean bound){
        if(bound||flames(record)>0)return edge(origin,record);
        var route=FinaleArchitecture.staircaseRoute(origin);return route.get(Math.min(route.size()-1,plan(origin).cornerSteps[0]+12));
    }
    /** Whether this reader has taken their House of Leaves from the shelf (or lit a fire before there was one to take). */
    public static boolean bound(ServerPlayer player,CompoundTag record){return flames(record)>0||StaircaseStory.exists(StaircaseStory.record(player));}
    public static ItemStack book(UUID reader,int leaves){
        ItemStack book=HouseWriting.book("House of Leaves","An unsigned hand",HouseWriting.WritingStyle.WILL,
                List.of(leaves+" leaves remain.\n\nHold this book in your off hand. Use flint and steel on the cold hearths, one after another.\n\nEach fire takes a leaf.",
                "The light does not reach the bottom.\n\nIt reaches the next fire.\n\nWhen all five are burning, the stairs keep their length.\n\nYou may still go back."));
        CompoundTag tag=new CompoundTag();tag.putUUID("StairReader",reader);tag.putInt(LEAVES,leaves);
        book.set(DataComponents.CUSTOM_DATA,CustomData.of(tag));
        book.set(DataComponents.CUSTOM_NAME,legacyName(leaves));
        return book;
    }
    /** The name tutorial books carried before leaves were scattered on the flights. */
    public static Component legacyName(int leaves){return HouseText.color(Component.literal("House of Leaves ("+leaves+" leaves)"));}
    public static int leaves(ItemStack book,UUID reader){
        var data=book.get(DataComponents.CUSTOM_DATA);if(data==null)return 0;
        var tag=data.copyTag();return tag.hasUUID("StairReader")&&reader.equals(tag.getUUID("StairReader"))?Math.max(0,tag.getInt(LEAVES)):0;
    }
    /** Adding props never rebuilds the shaft, refills a cache or replaces an attached original. */
    public static void dress(ServerLevel level,BlockPos origin){
        var data=LabyrinthData.get(level.getServer());var state=data.state(DRESS);String key=Long.toString(origin.asLong());
        if(state.getBoolean(key))return;
        boolean complete=true;
        for(var at:braziers(origin)){
            if(level.players().stream().anyMatch(p->p.getBoundingBox().intersects(new net.minecraft.world.phys.AABB(at)))){complete=false;continue;}
            if(level.getBlockState(at).isAir()&&!level.getBlockState(at.below()).getCollisionShape(level,at.below()).isEmpty())
                level.setBlock(at,Blocks.CAMPFIRE.defaultBlockState().setValue(CampfireBlock.LIT,false),F);
            if(!level.getBlockState(at).is(Blocks.CAMPFIRE))complete=false;
        }
        BlockPos at=shelf(origin);
        if(level.getBlockState(at).isAir()&&!level.players().stream().anyMatch(p->p.getBoundingBox().intersects(new net.minecraft.world.phys.AABB(at)))
                &&!level.getBlockState(at.below()).getCollisionShape(level,at.below()).isEmpty()){
            level.setBlock(at,Blocks.LECTERN.defaultBlockState().setValue(LecternBlock.FACING,Direction.EAST),F);
            if(level.getBlockEntity(at) instanceof net.minecraft.world.level.block.entity.LecternBlockEntity lectern){
                lectern.setBook(book(new UUID(0,0),REQUIRED));
                level.setBlock(at,level.getBlockState(at).setValue(LecternBlock.HAS_BOOK,true),F);
            }
        }
        if(!(level.getBlockEntity(at) instanceof net.minecraft.world.level.block.entity.LecternBlockEntity))complete=false;
        if(complete){state.putBoolean(key,true);data.setState(DRESS,state);}
    }
    /** The shelf gives a binding once, and binds the same story again only when its original is lost. */
    public static boolean take(ServerPlayer player){
        var result=StaircaseStory.shelf(player);return result==StaircaseStory.Shelf.ISSUED||result==StaircaseStory.Shelf.REBOUND;
    }
    @SubscribeEvent(priority=EventPriority.HIGHEST) public static void interact(PlayerInteractEvent.RightClickBlock event){
        if(!(event.getEntity() instanceof ServerPlayer player)||event.getHand()!=InteractionHand.MAIN_HAND
                ||!player.serverLevel().dimension().equals(HouseDimensions.INTERIOR)||player.isSpectator())return;
        BlockPos origin=HouseSavedData.get(player.server).houseOrigin();if(origin==null||!FinaleArchitecture.contains(origin,player.blockPosition()))return;
        if(event.getPos().equals(shelf(origin))){
            event.setCanceled(true);event.setCancellationResult(InteractionResult.SUCCESS);
            player.displayClientMessage(Component.literal(switch(StaircaseStory.shelf(player)){
                case ISSUED->"Your House of Leaves. Its five leaves are loose on the stairs below, one above each camp.";
                case REBOUND->"The shelf binds your story again, as far as you had found it.";
                case CARRIED->"Your House of Leaves is already with you. Its leaves are on the stairs below.";
                case NEARBY->"Your original House of Leaves is still lying here. Pick it up.";
                case FULL->"Make room in your pack for your House of Leaves.";
                case FINISHED->"Your story has already burned. The stairs are open.";
                case REFUSED->"The shelf is empty.";}),true);
        }else if(braziers(origin).contains(event.getPos())){
            event.setCanceled(true);event.setCancellationResult(InteractionResult.SUCCESS);
            ignite(player,origin,event.getPos());
        }
    }
    public static boolean ignite(ServerPlayer player,BlockPos origin,BlockPos at){
        if(player.isSpectator()||!player.isAlive()||!player.serverLevel().dimension().equals(HouseDimensions.INTERIOR)
                ||player.distanceToSqr(at.getCenter())>36)return false;
        var phase=FinaleProgress.phase(player.server,player.getUUID());if(phase!=FinaleProgress.Phase.STAIRCASE&&phase!=FinaleProgress.Phase.UNSEEN)return false;
        var record=FinaleProgress.player(player.server,player.getUUID());int index=braziers(origin).indexOf(at);
        if(index<0||!player.serverLevel().getBlockState(at).is(Blocks.CAMPFIRE))return false;
        if(index<flames(record)){
            var level=player.serverLevel();
            var hearth=level.getBlockState(at);
            if(!hearth.getValue(CampfireBlock.LIT)&&StaircaseStory.burned(StaircaseStory.record(player))>index)
                level.setBlock(at,hearth.setValue(CampfireBlock.LIT,true),F);
            io.github.knaitoe.theoldesthouse.house.PlaytestLog.refused(player,"hearth_already_lit",at);
            return false;
        }
        if(index>flames(record)){player.displayClientMessage(Component.literal("An earlier fire is still cold for you."),true);io.github.knaitoe.theoldesthouse.house.PlaytestLog.refused(player,"hearth_earlier_cold",at);return false;}
        // Both finite originals can stay in the pack, including after the cabin takes the off-hand arm.
        boolean oneHand=BodyLoss.oneArmed(player);ItemStack flame=lighter(player,oneHand);
        if(!(flame.getItem() instanceof net.minecraft.world.item.FlintAndSteelItem)){
            player.displayClientMessage(Component.literal("The hearth needs a flame. Any lighter you carry will do; Tom has one."),true);
            io.github.knaitoe.theoldesthouse.house.PlaytestLog.refused(player,"hearth_no_flame",at);return false;}
        // Only the explorer's own story burns here: no paper, no copy, no one else's leaves.
        StaircaseStory.sync(player);ItemStack fuel=story(player);
        if(fuel.isEmpty()){
            boolean foreign=StaircaseStory.foreign(player,player.getMainHandItem())||!oneHand&&StaircaseStory.foreign(player,player.getOffhandItem());
            player.displayClientMessage(Component.literal(foreign?"Another reader's story will not catch for you."
                    :"Bring your own House of Leaves. It can stay in your pack."),true);io.github.knaitoe.theoldesthouse.house.PlaytestLog.refused(player,"hearth_own_book",at);return false;
        }
        if(!StaircaseStory.ready(player,index)){
            player.displayClientMessage(Component.literal("Your House of Leaves has no unburned leaf. This camp's leaf lies on a landing not far above."),true);io.github.knaitoe.theoldesthouse.house.PlaytestLog.refused(player,"hearth_missing_leaf",at);return false;
        }
        var embers=BurnEmbers.excerpt(StaircaseStory.record(player),index,at);
        if(!StaircaseStory.burn(player,fuel,index))return false;
        flame.hurtAndBreak(1,player.serverLevel(),player,item->{});
        player.serverLevel().setBlock(at,player.serverLevel().getBlockState(at).setValue(CampfireBlock.LIT,true),F);
        record.putInt("StairFires",index+1);
        io.github.knaitoe.theoldesthouse.house.PlaytestLog.event(player,"fire_lit","index",index);
        FinaleProgress.save(player.server,player.getUUID(),record);
        HousePackets.send(player,embers);
        player.serverLevel().playSound(null,at,SoundEvents.FIRECHARGE_USE,SoundSource.BLOCKS,.65F,.85F);
        player.displayClientMessage(Component.literal(index+1==REQUIRED?"The last leaf burns. The dark keeps its distance.":"A leaf burns. The next flight comes out of the dark."),true);
        return true;
    }
    /** A lighter (Tom's included): in a hand the reader has, or the first one in their pack. */
    private static ItemStack lighter(ServerPlayer player,boolean oneHand){
        if(player.getMainHandItem().getItem() instanceof net.minecraft.world.item.FlintAndSteelItem)return player.getMainHandItem();
        if(!oneHand&&player.getOffhandItem().getItem() instanceof net.minecraft.world.item.FlintAndSteelItem)return player.getOffhandItem();
        for(var stack:player.getInventory().items)if(stack.getItem() instanceof net.minecraft.world.item.FlintAndSteelItem)return stack;
        return ItemStack.EMPTY;
    }
    /** Find the actual owned original throughout inventory; no held-book requirement. */
    private static ItemStack story(ServerPlayer player){
        for(int i=0;i<player.getInventory().getContainerSize();i++){var stack=player.getInventory().getItem(i);if(StaircaseStory.isCurrent(player,stack))return stack;}
        return ItemStack.EMPTY;
    }
    public static boolean tick(ServerPlayer player,BlockPos origin,CompoundTag record){
        if(record.getBoolean("OperatorCellVisit")){
            var base=FinaleArchitecture.base(origin);
            if(player.getY()>=FinaleArchitecture.ARENA-1&&player.getY()<=FinaleArchitecture.ARENA+16&&Math.abs(player.getX()-base.getX())<=16&&player.getZ()>=base.getZ()+30&&player.getZ()<=base.getZ()+73)return false;
            record.remove("OperatorCellVisit");
        }
        if(player.isCreative())return false;
        int fires=flames(record);boolean bound=bound(player,record);BlockPos edge=reach(origin,record,bound);
        boolean blocked=!open(record)&&player.getY()<edge.getY()-.75;
        if(blocked){
            BlockPos safe=record.contains("StairLastSafe")?BlockPos.of(record.getLong("StairLastSafe")):bound?landings(origin).get(fires):threshold(origin);
            // A place remembered below where this reader may now stand (the book was lost and the record with it) is no safe place.
            if(safe.getY()<edge.getY()-.75)safe=bound?landings(origin).get(fires):threshold(origin);
            HouseInternalTeleport.shift(player,Vec3.atBottomCenterOf(safe),player.getYRot());player.setDeltaMovement(Vec3.ZERO);
            if(player.tickCount%20==0)player.displayClientMessage(Component.literal(bound?"The stairs vanish into the dark. Bind this camp's leaf, then feed the fire."
                    :"The stairs vanish into the dark. Your House of Leaves is on the camp shelf above."),true);
        }else if(!open(record)&&player.tickCount%5==0&&player.onGround()&&player.getY()>edge.getY()+1)
            record.putLong("StairLastSafe",player.blockPosition().asLong());
        StaircaseLeaves.gather(player,origin,fires);StaircaseLeaves.cue(player,origin,fires);
        // The first camp is a long way down. A reader who starts for it with their book and no flame is told once, while Tom is still near.
        if(bound&&!open(record)&&!record.getBoolean("StairFlameTold")&&player.tickCount%20==0){
            double below=threshold(origin).getY()-player.getY();
            if(below>2&&below<14&&lighter(player,BodyLoss.oneArmed(player)).isEmpty()){
                record.putBoolean("StairFlameTold",true);
                player.displayClientMessage(Component.literal("You carry no flame. The fires below will need one; Tom has a lighter."),true);
            }
        }
        if(player.tickCount%10==0){
            // The dark closes in at the camp whose fire is cold; otherwise the book lights twelve blocks, at every depth.
            float sight=off(origin,player.position())?CLEAR:open(record)?SIGHT:Math.max(1.2F,Math.min(SIGHT,(float)(player.getY()-edge.getY())+2));
            HousePackets.send(player,new StaircaseLightPayload(true,fires,sight));
            if(!open(record)&&player.getY()<edge.getY()+4)player.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.DARKNESS,30,0,false,false));
        }
        return blocked;
    }
    /** Off the stair: the chamber at its foot, south of the well, and the side mazes beyond the well's east and west walls. Their dark is their own. */
    public static boolean off(BlockPos origin,Vec3 at){
        var b=FinaleArchitecture.base(origin);
        if(Math.abs(at.x-b.getX())>FinaleArchitecture.SHAFT_RADIUS+1)return true;
        return at.y<FinaleArchitecture.ARENA+14&&at.z>=b.getZ()+30;
    }
}
