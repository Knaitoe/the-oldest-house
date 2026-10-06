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

/** Five actual fires each take one leaf of the explorer's own story. The unlit descent remains impassable per explorer. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class StaircaseFire {
    public static final int REQUIRED=5;
    private static final String DRESS="staircase_fire_0433";
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
    public static List<BlockPos> landings(BlockPos origin){
        var route=FinaleArchitecture.staircaseRoute(origin);var b=FinaleArchitecture.base(origin);
        return route.stream().filter(p->Math.abs(p.getX()-b.getX())==FinaleArchitecture.STAIR_RADIUS
                &&Math.abs(p.getZ()-b.getZ())==FinaleArchitecture.STAIR_RADIUS).limit(REQUIRED).toList();
    }
    public static List<BlockPos> braziers(BlockPos origin){
        var b=FinaleArchitecture.base(origin);
        return landings(origin).stream().map(p->p.offset(Integer.signum(p.getX()-b.getX())*3,0,Integer.signum(p.getZ()-b.getZ())*3)).toList();
    }
    public static int flames(CompoundTag record){return Math.max(0,Math.min(REQUIRED,record.getInt("StairFires")));}
    public static boolean open(CompoundTag record){return flames(record)==REQUIRED;}
    public static BlockPos edge(BlockPos origin,CompoundTag record){
        var route=FinaleArchitecture.staircaseRoute(origin);
        if(open(record))return route.getLast();
        BlockPos landing=landings(origin).get(flames(record));
        return route.get(Math.min(route.size()-1,route.indexOf(landing)+12));
    }
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
                case ISSUED->"Your House of Leaves. Its five leaves are loose on the stairs below, one on each flight.";
                case REBOUND->"The shelf binds your story again, as far as you had found it.";
                case CARRIED->"Your House of Leaves is already with you. Its leaves are on the stairs below.";
                case FINISHED->"Your story has already burned. The stairs are open.";
                case REFUSED->"The shelf is empty.";}),true);
        }else if(braziers(origin).contains(event.getPos())){
            event.setCanceled(true);event.setCancellationResult(InteractionResult.SUCCESS);
            ignite(player,origin,event.getPos());
        }
    }
    public static boolean ignite(ServerPlayer player,BlockPos origin,BlockPos at){
        if(player.isSpectator()||!player.isAlive()||!player.serverLevel().dimension().equals(HouseDimensions.INTERIOR)
                ||player.distanceToSqr(at.getCenter())>36||!(player.getMainHandItem().getItem() instanceof net.minecraft.world.item.FlintAndSteelItem))return false;
        var phase=FinaleProgress.phase(player.server,player.getUUID());if(phase!=FinaleProgress.Phase.STAIRCASE&&phase!=FinaleProgress.Phase.UNSEEN)return false;
        var record=FinaleProgress.player(player.server,player.getUUID());int index=braziers(origin).indexOf(at);
        if(index<0||!player.serverLevel().getBlockState(at).is(Blocks.CAMPFIRE))return false;
        if(index<flames(record)){player.displayClientMessage(Component.literal("This fire already burns for you."),true);return false;}
        if(index>flames(record)){player.displayClientMessage(Component.literal("An earlier fire is still cold for you."),true);return false;}
        // Only the explorer's own story burns here: no paper, no copy, no one else's leaves.
        StaircaseStory.sync(player);ItemStack fuel=player.getOffhandItem();
        if(!StaircaseStory.isCurrent(player,fuel)){
            player.displayClientMessage(Component.literal(StaircaseStory.foreign(player,fuel)?"Another reader's story will not catch for you."
                    :"Only your own House of Leaves will catch. Hold it in your off hand."),true);return false;
        }
        if(!StaircaseStory.ready(player,index)){
            player.displayClientMessage(Component.literal("Your House of Leaves has no unburned leaf. This flight's leaf is somewhere in the dark above."),true);return false;
        }
        var embers=BurnEmbers.excerpt(StaircaseStory.record(player),index,at);
        if(!StaircaseStory.burn(player,fuel,index))return false;
        player.getMainHandItem().hurtAndBreak(1,player,EquipmentSlot.MAINHAND);
        player.serverLevel().setBlock(at,player.serverLevel().getBlockState(at).setValue(CampfireBlock.LIT,true),F);
        record.putInt("StairFires",index+1);record.putBoolean("StairFireVersion",true);
        FinaleProgress.save(player.server,player.getUUID(),record);
        HousePackets.send(player,embers);
        player.serverLevel().playSound(null,at,SoundEvents.FIRECHARGE_USE,SoundSource.BLOCKS,.65F,.85F);
        player.displayClientMessage(Component.literal(index+1==REQUIRED?"The dark gives way. The whole staircase is there.":"A leaf burns. The next flight comes out of the dark."),true);
        return true;
    }
    /** A saved visit already deep in the old staircase stays traversable on upgrade. */
    public static void initialize(CompoundTag record,double y){
        if(record.getBoolean("StairFireVersion"))return;
        if(y<landings(BlockPos.ZERO).getLast().getY()-4)record.putInt("StairFires",REQUIRED);
        record.putBoolean("StairFireVersion",true);
    }
    public static boolean tick(ServerPlayer player,BlockPos origin,CompoundTag record){
        initialize(record,player.getY());if(player.isCreative())return false;
        int fires=flames(record);BlockPos edge=edge(origin,record);
        boolean blocked=!open(record)&&player.getY()<edge.getY()-.75;
        if(blocked){
            BlockPos safe=record.contains("StairLastSafe")?BlockPos.of(record.getLong("StairLastSafe")):landings(origin).get(fires);
            HouseInternalTeleport.shift(player,Vec3.atBottomCenterOf(safe),player.getYRot());player.setDeltaMovement(Vec3.ZERO);
            if(player.tickCount%20==0)player.displayClientMessage(Component.literal("The stairs vanish into the dark. Bind this flight's leaf, then feed the fire."),true);
        }else if(!open(record)&&player.tickCount%5==0&&player.onGround()&&player.getY()>edge.getY()+1)
            record.putLong("StairLastSafe",player.blockPosition().asLong());
        StaircaseLeaves.cue(player,origin,fires);
        if(player.tickCount%10==0){
            float sight=open(record)?32:Math.max(1.2F,Math.min(12,(float)(player.getY()-edge.getY())+2));
            HousePackets.send(player,new StaircaseLightPayload(true,fires,sight));
            if(!open(record)&&player.getY()<edge.getY()+4)player.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.DARKNESS,30,0,false,false));
        }
        return blocked;
    }
}
