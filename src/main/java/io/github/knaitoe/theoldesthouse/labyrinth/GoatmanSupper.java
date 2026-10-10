package io.github.knaitoe.theoldesthouse.labyrinth;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.*;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.*;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/** One occupied cooking clock; every package, raw frank and cooked serving retains its round. */
public final class GoatmanSupper {
    public static final int COOK_TICKS=120,PAN_CAPACITY=4;
    private GoatmanSupper(){}
    public static boolean ready(CompoundTag r){
        int expected=GoatmanVignette.expectedCount(r);
        return r.getBoolean("MealReady0465")||expected>0&&Integer.bitCount(r.getInt("Plated0464")&~r.getInt("EmptyPlates0465"))>=expected;
    }
    private static boolean belongs(ItemStack item,CompoundTag r){
        var custom=item.get(DataComponents.CUSTOM_DATA);var tag=custom==null?null:custom.copyTag();
        return tag!=null&&tag.hasUUID(GoatmanVignette.ROUND)&&r.hasUUID("Id")&&tag.getUUID(GoatmanVignette.ROUND).equals(r.getUUID("Id"));
    }
    private static void give(ServerPlayer p,ItemStack item){if(!p.getInventory().add(item))p.drop(item,false);}
    public static void onFood(net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent.Start e){
        if(!(e.getEntity() instanceof ServerPlayer p)||!GoatmanVignette.inside(p)||!e.getItem().is(GoatmanRegistry.BRAT.get()))return;
        var r=GoatmanVignette.run(LabyrinthData.get(p.server));if(GoatmanVignette.fresh(r)&&!ready(r)&&belongs(e.getItem(),r)){e.setCanceled(true);p.displayClientMessage(Component.literal("Put supper on the plates first."),true);}
    }
    /** Cousins eat only food the player actually put down. The impostor uses the absent runner's place. */
    static boolean eat(ServerLevel l,BlockPos b,CompoundTag r,GoatmanChild c,int index,int clock){
        if(c.getPersistentData().contains("EatingUntil0465")&&clock>=c.getPersistentData().getInt("EatingUntil0465")&&c.getMainHandItem().is(GoatmanRegistry.BRAT.get())){c.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,ItemStack.EMPTY);c.getPersistentData().remove("EatingUntil0465");}
        if(!GoatmanVignette.fresh(r)||!ready(r)||!c.seated()||(r.getInt("EatenCousins0465")&(1<<index))!=0)return false;
        int real=index==r.getInt("Wrong")?r.getInt("Runner"):index,seat=real<r.getInt("Wrong")?real:real-1,bit=1<<seat;
        if((r.getInt("Plated0464")&~r.getInt("EmptyPlates0465")&bit)==0){
            if(index==r.getInt("Runner")&&!r.getBoolean("MissingSupper0465")){r.putBoolean("MissingSupper0465",true);return true;}return false;
        }
        r.putInt("EmptyPlates0465",r.getInt("EmptyPlates0465")|bit);r.putInt("EatenCousins0465",r.getInt("EatenCousins0465")|(1<<index));GoatmanWoods.served(l,b,seat,false);
        var item=new ItemStack(GoatmanRegistry.BRAT.get());CustomData.update(DataComponents.CUSTOM_DATA,item,t->t.putUUID(GoatmanVignette.ROUND,r.getUUID("Id")));c.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,item);c.getPersistentData().putInt("EatingUntil0465",clock+80);return false;
    }
    public static void tick(ServerLevel l,BlockPos b,CompoundTag r){
        if(!r.getBoolean("PlayerServes0464"))return;
        var cooking=r.getList("Cooking0465",Tag.TAG_COMPOUND);var cooked=r.getList("CookedPan0465",Tag.TAG_COMPOUND);boolean changed=false;
        for(int i=cooking.size()-1;i>=0;i--){
            var t=cooking.getCompound(i);int left=t.getInt("Left")-1;t.putInt("Left",left);
            if(left<=0){
                var raw=ItemStack.parseOptional(l.registryAccess(),t.getCompound("Item"));
                var done=new ItemStack(GoatmanRegistry.BRAT.get());var metadata=raw.get(DataComponents.CUSTOM_DATA);if(metadata!=null)done.set(DataComponents.CUSTOM_DATA,metadata);
                var entry=new CompoundTag();entry.put("Item",done.save(l.registryAccess()));cooked.add(entry);cooking.remove(i);changed=true;
            }
        }
        r.put("Cooking0465",cooking);r.put("CookedPan0465",cooked);
        if(changed)GoatmanWoods.cookingPan(l,b,r);
        var stove=l.getBlockState(b.offset(GoatmanWoods.STOVE));
        if(stove.is(Blocks.SMOKER)&&stove.getValue(SmokerBlock.LIT)!=!cooking.isEmpty())l.setBlock(b.offset(GoatmanWoods.STOVE),stove.setValue(SmokerBlock.LIT,!cooking.isEmpty()),Block.UPDATE_CLIENTS);
    }
    static boolean interact(ServerPlayer player,PlayerInteractEvent.RightClickBlock event){
        var data=LabyrinthData.get(player.server);var run=GoatmanVignette.run(data);
        if(!run.getBoolean("PlayerServes0464")||run.getInt("Phase")!=GoatmanVignette.GATHERING)return false;
        var member=run.getCompound("Cohort").getCompound(player.getUUID().toString());
        if(!member.getBoolean("Active")||member.getBoolean("Failed"))return false;
        var base=GoatmanVignette.base(player.server);var rel=event.getPos().subtract(base);var held=player.getItemInHand(event.getHand());
        boolean stove=rel.equals(GoatmanWoods.STOVE)||rel.equals(GoatmanWoods.PAN);
        if(stove){
            var cooking=run.getList("Cooking0465",Tag.TAG_COMPOUND);var cooked=run.getList("CookedPan0465",Tag.TAG_COMPOUND);
            if(held.is(GoatmanRegistry.FRANKS.get()))player.setItemInHand(event.getHand(),held.getItem().use(player.level(),player,event.getHand()).getObject());
            else if(held.is(GoatmanRegistry.RAW_FRANK.get())){
                if(!belongs(held,run))player.displayClientMessage(Component.literal("That wasn't in this supper's pack."),true);
                else if(cooking.size()+cooked.size()>=PAN_CAPACITY)player.displayClientMessage(Component.literal("The pan is full. Take the cooked ones out first."),true);
                else {var entry=new CompoundTag();entry.put("Item",held.copyWithCount(1).save(player.registryAccess()));entry.putInt("Left",COOK_TICKS);held.shrink(1);cooking.add(entry);run.put("Cooking0465",cooking);GoatmanWoods.cookingPan(player.serverLevel(),base,run);}
            }else if(!cooked.isEmpty()){
                var item=ItemStack.parseOptional(player.registryAccess(),cooked.getCompound(0).getCompound("Item"));cooked.remove(0);run.put("CookedPan0465",cooked);give(player,item);GoatmanWoods.cookingPan(player.serverLevel(),base,run);
            }else{
                int issued=run.getInt("PacketsIssued0464"),limit=(GoatmanVignette.expectedCount(run)+3)/4;
                if(issued<limit){
                    var packet=new ItemStack(GoatmanRegistry.FRANKS.get());CustomData.update(DataComponents.CUSTOM_DATA,packet,t->t.putUUID(GoatmanVignette.ROUND,run.getUUID("Id")));
                    run.putInt("PacketsIssued0464",issued+1);give(player,packet);
                }else player.displayClientMessage(Component.literal(cooking.isEmpty()?"Only the empty wrappers are left.":"They're still cooking."),true);
            }
        }else{
            var hit=event.getHitVec().getLocation().subtract(base.getX(),base.getY(),base.getZ());int seat=-1;double distance=1.6;
            for(int i=0;i<GoatmanVignette.expectedCount(run);i++){double d=GoatmanWoods.plate(i).distanceToSqr(hit);if(d<distance){distance=d;seat=i;}}
            if(seat<0)return false;int mask=run.getInt("Plated0464"),bit=1<<seat,empty=run.getInt("EmptyPlates0465");
            if(held.is(GoatmanRegistry.BRAT.get())&&belongs(held,run)){
                if((mask&~empty&bit)==0){held.shrink(1);run.putInt("Plated0464",mask|bit);run.putInt("EmptyPlates0465",empty&~bit);GoatmanWoods.served(player.serverLevel(),base,seat,true);
                    if(ready(run)){run.putBoolean("MealReady0465",true);run.putBoolean("MealFrozen0465",true);}}
                else player.displayClientMessage(Component.literal("There's already one here."),true);
            }else if(held.isEmpty()&&(mask&~empty&bit)!=0){
                run.putInt("EmptyPlates0465",empty|bit);GoatmanWoods.served(player.serverLevel(),base,seat,false);
                var item=new ItemStack(GoatmanRegistry.BRAT.get());CustomData.update(DataComponents.CUSTOM_DATA,item,t->t.putUUID(GoatmanVignette.ROUND,run.getUUID("Id")));give(player,item);
            }else player.displayClientMessage(Component.literal("Put a cooked frank on the plate."),true);
        }
        GoatmanVignette.saveRun(data,run);event.setCanceled(true);event.setCancellationResult(InteractionResult.SUCCESS);return true;
    }
}
