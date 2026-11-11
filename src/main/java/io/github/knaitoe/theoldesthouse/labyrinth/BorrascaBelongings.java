package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;
import net.neoforged.neoforge.common.util.TriState;

/** Finite reader originals. Picking up, dropping and confiscation transfer the actual stack. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class BorrascaBelongings {
    public static final String TOKEN="BorrascaOriginal0508";
    private BorrascaBelongings(){}
    public static ItemStack original(ServerPlayer p,CompoundTag own,String key){
        if(own.contains(key+"Original"))return ItemStack.parseOptional(p.registryAccess(),own.getCompound(key+"Original"));
        var token=UUID.randomUUID();own.putUUID(key+"Token",token);
        var s=new ItemStack(item(key));
        // 0.4.445: the comic keeps the name printed on it.
        if(!key.equals(COMIC)&&!key.equals(MAIL))s.set(DataComponents.CUSTOM_NAME,Component.literal(BorrascaStory.FRIEND+(key.equals("Compass")?"'s compass":key.equals(KNIFE)?"'s pocketknife":"'s name patch")));
        CustomData.update(DataComponents.CUSTOM_DATA,s,t->{t.putUUID(TOKEN,token);t.putUUID(LiteraryVignettes.OWNER,p.getUUID());t.putString(LiteraryVignettes.KEY,key);t.putString(LiteraryVignettes.PLACE,LabyrinthPlace.BORRASCA.id());});
        if(!key.equals(KNIFE))Keepsakes.inscribe(s,LabyrinthPlace.BORRASCA,key);own.put(key+"Original",s.save(p.registryAccess()));return s;
    }
    /** 0.4.429: the friend's pocketknife, lent for the length of a name at the Tree and handed back. */
    public static final String KNIFE="Knife";
    /** 0.4.445: the comic from the fort's window (BorrascaComic). */
    public static final String COMIC="Comic";
    /** 0.4.459: the letter for the Garritys (BorrascaGarritys). Its key is not "Letter": that is the note read under the depot. */
    public static final String MAIL="Mail";
    private static Item item(String key){return key.equals("Compass")?BorrascaRegistry.COMPASS.get():key.equals(KNIFE)?LiteraryRegistry.KNIFE.get():key.equals(COMIC)?BorrascaRegistry.COMIC.get():key.equals(MAIL)?BorrascaRegistry.LETTER.get():BorrascaRegistry.PATCH.get();}
    /** Puts the original in the reader's hand: what they were holding goes into the pack if there is room, otherwise the original does. */
    public static boolean hand(ServerPlayer p,CompoundTag own,String key){
        if(has(p,own,key))return true;var inv=p.getInventory();var s=original(p,own,key);
        if(!inv.getSelected().isEmpty()){int free=-1;for(int i=0;i<inv.items.size();i++)if(i!=inv.selected&&inv.items.get(i).isEmpty()){free=i;break;}if(free>=0){inv.items.set(free,inv.getSelected());inv.items.set(inv.selected,ItemStack.EMPTY);}}
        if(inv.getSelected().isEmpty())inv.items.set(inv.selected,s);else if(!inv.add(s))return false;
        own.putBoolean(key+"Issued",true);p.inventoryMenu.broadcastChanges();return true;
    }
    public static boolean valid(ServerPlayer p,CompoundTag own,String key,ItemStack s){
        var c=s.get(DataComponents.CUSTOM_DATA);if(c==null||!own.hasUUID(key+"Token"))return false;var t=c.copyTag();
        return s.is(item(key))&&t.hasUUID(TOKEN)&&t.getUUID(TOKEN).equals(own.getUUID(key+"Token"))&&t.hasUUID(LiteraryVignettes.OWNER)&&p.getUUID().equals(t.getUUID(LiteraryVignettes.OWNER));
    }
    public static boolean has(ServerPlayer p,CompoundTag own,String key){for(int i=0;i<p.getInventory().getContainerSize();i++)if(valid(p,own,key,p.getInventory().getItem(i)))return true;return false;}
    public static ItemStack remove(ServerPlayer p,CompoundTag own,String key){for(int i=0;i<p.getInventory().getContainerSize();i++){var s=p.getInventory().getItem(i);if(valid(p,own,key,s)){p.getInventory().setItem(i,ItemStack.EMPTY);p.inventoryMenu.broadcastChanges();return s;}}return ItemStack.EMPTY;}
    public static boolean dropOnce(ServerPlayer p,CompoundTag own,String key,BlockPos at){
        if(own.getBoolean(key+"Issued"))return false;var s=original(p,own,key);var e=new ItemEntity(p.serverLevel(),at.getX()+.5,at.getY()+.12,at.getZ()+.5,s);e.setDeltaMovement(0,0,0);e.setTarget(p.getUUID());e.setInvulnerable(true);e.setUnlimitedLifetime();
        if(!p.serverLevel().addFreshEntity(e))return false;own.putBoolean(key+"Issued",true);own.putUUID(key+"Entity",e.getUUID());return true;
    }
    public static boolean takeAt(ServerPlayer p,CompoundTag own,String key,BlockPos at){
        if(has(p,own,key))return true;
        if(own.hasUUID(key+"Entity")&&p.serverLevel().getEntity(own.getUUID(key+"Entity")) instanceof ItemEntity e&&valid(p,own,key,e.getItem())){
            if(e.distanceToSqr(p)>36)return false;var actual=e.getItem();if(p.getInventory().add(actual)){e.discard();p.inventoryMenu.broadcastChanges();return true;}return false;
        }
        if(own.getBoolean(key+"Issued"))return false;
        var s=original(p,own,key);if(p.getInventory().add(s)){own.putBoolean(key+"Issued",true);p.inventoryMenu.broadcastChanges();return true;}
        return dropOnce(p,own,key,at);
    }
    public static boolean confiscate(ServerPlayer p,CompoundTag own){
        var actual=remove(p,own,"Compass");if(actual.isEmpty())return false;
        own.put("ConfiscatedCompass",actual.save(p.registryAccess()));own.putBoolean("CompassConfiscated",true);own.putBoolean("CompassEverConfiscated",true);return true;
    }
    public static boolean reclaim(ServerPlayer p,CompoundTag own){
        if(!own.contains("ConfiscatedCompass"))return false;var actual=ItemStack.parseOptional(p.registryAccess(),own.getCompound("ConfiscatedCompass"));
        if(!valid(p,own,"Compass",actual)||!p.getInventory().add(actual))return false;own.remove("ConfiscatedCompass");own.putBoolean("CompassConfiscated",false);p.inventoryMenu.broadcastChanges();return true;
    }
    @SubscribeEvent public static void joined(EntityJoinLevelEvent event){
        if(!(event.getEntity() instanceof ItemEntity e)||event.getLevel().isClientSide())return;var c=e.getItem().get(DataComponents.CUSTOM_DATA);if(c==null)return;var t=c.copyTag();if(!t.hasUUID(TOKEN)||!t.hasUUID(LiteraryVignettes.OWNER))return;
        e.setInvulnerable(true);e.setUnlimitedLifetime();e.setTarget(t.getUUID(LiteraryVignettes.OWNER));
        var server=event.getLevel().getServer();if(server==null)return;var d=LabyrinthData.get(server);var id=t.getUUID(LiteraryVignettes.OWNER);var own=LiteraryVignettes.personal(d,id,LabyrinthPlace.BORRASCA);String key=t.getString(LiteraryVignettes.KEY);if(own.hasUUID(key+"Token")&&own.getUUID(key+"Token").equals(t.getUUID(TOKEN))){own.putUUID(key+"Entity",e.getUUID());LiteraryVignettes.save(d,id,LabyrinthPlace.BORRASCA,own);}
    }
    @SubscribeEvent public static void pickup(ItemEntityPickupEvent.Pre event){var c=event.getItemEntity().getItem().get(DataComponents.CUSTOM_DATA);if(c!=null){var t=c.copyTag();if(t.hasUUID(TOKEN)&&t.hasUUID(LiteraryVignettes.OWNER)&&!t.getUUID(LiteraryVignettes.OWNER).equals(event.getPlayer().getUUID()))event.setCanPickup(TriState.FALSE);}}
}
