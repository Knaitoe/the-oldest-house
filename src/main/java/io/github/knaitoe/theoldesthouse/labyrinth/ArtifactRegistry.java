package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * The twenty artifacts the audit's build 7 gives (docs/ARTIFACTS_0_4_199.md), one item each, drawn by tools/draw_artifacts.py.
 * Their effects are in {@link Artifacts}. The ones that are used (the whistle, the sitting record, the candle and the vial) are
 * {@link Artifacts.Used}; the worker's blade is a sword; the rest are carried.
 */
public final class ArtifactRegistry {
    private static final DeferredRegister.Items ITEMS=DeferredRegister.createItems(TheOldestHouse.MOD_ID);
    private static final Map<Artifacts.Kind,DeferredItem<? extends Item>> ITEM=new EnumMap<>(Artifacts.Kind.class);
    static{
        for(var kind:Artifacts.Kind.values()){
            var props=new Item.Properties().stacksTo(1);
            ITEM.put(kind,switch(kind){
                case BLADE->ITEMS.register(kind.id,()->new SwordItem(Tiers.IRON,props.attributes(SwordItem.createAttributes(Tiers.IRON,3,-2.4F))));
                case CANDLE->ITEMS.register(kind.id,()->new Artifacts.Used(props.durability(Artifacts.CANDLE_SECONDS),kind));
                case VIAL->ITEMS.register(kind.id,()->new Artifacts.Used(props.durability(Artifacts.VIAL_USES),kind));
                case WHISTLE,RECORD->ITEMS.register(kind.id,()->new Artifacts.Used(props,kind));
                default->ITEMS.registerSimpleItem(kind.id,props);
            });
        }
    }
    public static Item item(Artifacts.Kind kind){return ITEM.get(kind).get();}
    private ArtifactRegistry(){}
    public static void register(IEventBus bus){ITEMS.register(bus);}
}
