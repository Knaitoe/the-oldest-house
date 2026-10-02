package io.github.knaitoe.theoldesthouse.labyrinth;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;

/** Equipment components already synchronize to every tracking client and survive saves. */
public final class HarriganAppearance {
    public static final String KEY="HouseHumanAppearance";
    private HarriganAppearance(){}
    public static int variant(ArmorStand body){var data=body.getItemBySlot(EquipmentSlot.HEAD).get(DataComponents.CUSTOM_DATA);return data==null?0:data.copyTag().getInt(KEY);}
    public static void apply(ArmorStand body,boolean casket){
        int value=casket?3:body.getItemBySlot(EquipmentSlot.HEAD).is(Items.ZOMBIE_HEAD)?2:1;
        if(variant(body)==value)return;
        var head=body.getItemBySlot(EquipmentSlot.HEAD).copy();
        CustomData.update(DataComponents.CUSTOM_DATA,head,tag->tag.putInt(KEY,value));
        body.setItemSlot(EquipmentSlot.HEAD,head);
    }
}
