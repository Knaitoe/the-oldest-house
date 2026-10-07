package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.*;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;

/** A finite personal quill and the actual original laid on the shared table. */
public final class ConfessionBooks {
    private ConfessionBooks(){}
    public static boolean owned(ItemStack s,ServerPlayer p){var d=s.get(DataComponents.CUSTOM_DATA);return d==null||!d.copyTag().hasUUID(LiteraryVignettes.OWNER)||d.copyTag().getUUID(LiteraryVignettes.OWNER).equals(p.getUUID());}
    public static void shelf(ServerPlayer p,CompoundTag own){
        LiteraryVignettes.reward(p,LabyrinthPlace.CONFESSION,own,"BlankJournal",new ItemStack(Items.WRITABLE_BOOK));
    }
    public static ItemEntity tableBook(ServerPlayer p,CompoundTag own){
        if(!own.hasUUID("TableJournal"))return null;
        var b=LiteraryVignettes.base(p,LabyrinthPlace.CONFESSION);if(b==null)return null;
        var e=p.serverLevel().getEntity(own.getUUID("TableJournal"));
        if(!(e instanceof ItemEntity item)||!item.isAlive()||!item.getItem().is(Items.WRITABLE_BOOK)||!owned(item.getItem(),p)
                ||item.position().distanceToSqr(b.offset(SceneReview.BOOK_TRAY).getCenter().add(0,.5,0))>2)return null;
        return item;
    }
    public static void table(ServerPlayer p,CompoundTag own){
        var item=tableBook(p,own);
        if(item!=null){if(p.isShiftKeyDown()){var original=item.getItem().copy();item.discard();own.remove("TableJournal");LiteraryVignettes.give(p,original);}return;}
        if(own.hasUUID("TableJournal"))return; // An unloaded/lost original is never silently copied.
        var held=p.getMainHandItem();if(!held.is(Items.WRITABLE_BOOK)||!owned(held,p))return;
        var original=held.copyWithCount(1);var data=original.get(DataComponents.CUSTOM_DATA);
        if(own.hasUUID("Journal")&&(data==null||!data.copyTag().hasUUID("Journal")||!data.copyTag().getUUID("Journal").equals(own.getUUID("Journal"))))return;
        CustomData.update(DataComponents.CUSTOM_DATA,original,t->t.putUUID(LiteraryVignettes.OWNER,p.getUUID()));
        var at=LiteraryVignettes.base(p,LabyrinthPlace.CONFESSION).offset(SceneReview.BOOK_TRAY);
        var laid=new ItemEntity(p.serverLevel(),at.getX()+.5,at.getY()+1.02,at.getZ()+.5,original);
        laid.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);
        laid.setNoGravity(true);laid.setInvulnerable(true);laid.setUnlimitedLifetime();laid.setPickUpDelay(32767);laid.setTarget(p.getUUID());
        if(!p.serverLevel().addFreshEntity(laid))return;
        held.shrink(1);own.putUUID("TableJournal",laid.getUUID());
    }
}
