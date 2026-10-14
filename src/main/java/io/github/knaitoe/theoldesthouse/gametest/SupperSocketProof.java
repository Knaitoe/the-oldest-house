package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.labyrinth.*;
import java.util.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Display;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

/** Opt-in real-client serving and old-display repair, on the actual authored trailer table. */
final class SupperSocketProof {
    private static final Map<UUID,UUID> ORIGINALS=new HashMap<>();
    private static final Map<UUID,ItemStack> ITEMS=new HashMap<>();
    private SupperSocketProof(){}
    private static int seat(ServerPlayer p){return p.getGameProfile().getName().endsWith("A")?0:1;}
    private static int count(ServerPlayer p){return seat(p)==0?2:1;}
    private static void require(boolean value,String detail){if(!value)throw new IllegalStateException("LIVE SUPPER: "+detail);}
    static void begin(ServerPlayer a,ServerPlayer b,BlockPos origin){
        var data=LabyrinthData.get(a.server);var base=LabyrinthPlaces.base(origin,LabyrinthPlace.GOATMAN);var level=a.server.getLevel(io.github.knaitoe.theoldesthouse.house.HouseDimensions.INTERIOR);
        for(var p:List.of(a,b)){
            p.stopRiding();p.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);p.setNoGravity(true);p.setInvulnerable(false);p.getFoodData().setFoodLevel(20);
            var at=GoatmanWoods.plate(seat(p)).add(base.getX(),base.getY(),base.getZ());p.teleportTo(level,base.getX()+(seat(p)==0?-3.5:3.5),base.getY()+1,at.z,0,0);p.hasChangedDimension();p.connection.resetPosition();GoatmanVignette.enter(p);
            p.getInventory().setItem(0,ItemStack.EMPTY);p.getInventory().setItem(7,ItemStack.EMPTY);p.inventoryMenu.broadcastChanges();GoatmanWoods.served(level,base,seat(p),false);
        }
        var run=GoatmanVignette.run(data);run.putInt("Phase",GoatmanVignette.GATHERING);run.putInt("Clock",850);run.putBoolean("PlayerServes0464",true);run.putInt("Plated0464",0);run.putInt("EmptyPlates0465",0);run.putBoolean("MealReady0465",false);run.putBoolean("MealFrozen0465",false);
        var cohort=run.getCompound("Cohort");for(var p:List.of(a,b)){var own=cohort.getCompound(p.getUUID().toString());own.putBoolean("Arrived",true);own.putBoolean("Active",true);own.putBoolean("Failed",false);cohort.put(p.getUUID().toString(),own);}run.put("Cohort",cohort);var all=data.state(GoatmanVignette.ID);all.put("Run",run);data.setState(GoatmanVignette.ID,all);
        ORIGINALS.clear();ITEMS.clear();
    }
    static void food(ServerPlayer a,ServerPlayer b){
        var round=GoatmanVignette.run(LabyrinthData.get(a.server)).getUUID("Id");
        for(var p:List.of(a,b)){
            var item=new ItemStack(GoatmanRegistry.BRAT.get(),count(p));item.set(DataComponents.CUSTOM_NAME,Component.literal("The actual socket serving"));
            CustomData.update(DataComponents.CUSTOM_DATA,item,t->{t.putUUID(GoatmanVignette.ROUND,round);t.putUUID("SocketOriginal",p.getUUID());});ITEMS.put(p.getUUID(),item.copyWithCount(1));p.getInventory().setItem(0,item);p.inventoryMenu.broadcastChanges();
        }
    }
    static void served(ServerPlayer a,ServerPlayer b,BlockPos base){
        for(var p:List.of(a,b)){
            var display=GoatmanWoods.serving(p.serverLevel(),base,seat(p));require(display!=null,"the actual block-use plus fallback places a native serving");
            require(ItemStack.matches(GoatmanWoods.food(p.serverLevel(),display),ITEMS.get(p.getUUID()))&&p.getInventory().countItem(GoatmanRegistry.BRAT.get())==count(p)-1,"both a remaining stack and the last frank spend exactly one and retain all original components");ORIGINALS.put(p.getUUID(),display.getUUID());
            require(!display.isInvisible(),"the displayed original is visible");
        }
    }
    static void pickedUp(ServerPlayer a,ServerPlayer b,BlockPos base){
        for(var p:List.of(a,b))require(GoatmanWoods.serving(p.serverLevel(),base,seat(p))==null&&p.serverLevel().getEntity(ORIGINALS.get(p.getUUID()))==null&&p.getInventory().countItem(GoatmanRegistry.BRAT.get())==count(p),"the later empty-hand click returns the actual original once and its fallback does not put it back");
    }
    static void legacy(ServerPlayer a,ServerPlayer b,BlockPos base){
        for(var p:List.of(a,b)){
            var held=p.getInventory().getItem(0);require(held.is(GoatmanRegistry.BRAT.get()),"the returned original is carried for the old-display fixture");var display=GoatmanWoods.served(p.serverLevel(),base,seat(p),held.copyWithCount(1));held.shrink(1);var id=display.getUUID();
            var tag=display.saveWithoutId(new CompoundTag());tag.putFloat("view_range",0);tag.putString("item_display","none");var transform=tag.getCompound("transformation");var zero=new net.minecraft.nbt.ListTag();for(int i=0;i<3;i++)zero.add(net.minecraft.nbt.FloatTag.valueOf(0));transform.put("scale",zero);tag.put("transformation",transform);display.load(tag);display.setInvisible(true);display.getPersistentData().putBoolean("PlaceSetting0492",true);display.getPersistentData().remove("PlaceSetting0493");
            GoatmanWoods.fitPlaceSettings(p.serverLevel(),base);require(display.getUUID().equals(id)&&ItemStack.matches(GoatmanWoods.food(p.serverLevel(),display),ITEMS.get(p.getUUID())),"repair keeps the same serialized food original and UUID");ORIGINALS.put(p.getUUID(),id);p.inventoryMenu.broadcastChanges();
        }
    }
}
