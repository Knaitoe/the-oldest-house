package io.github.knaitoe.theoldesthouse.labyrinth;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseWriting;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import net.minecraft.world.item.*;
import net.minecraft.world.item.trading.*;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.bus.api.*;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.loot.*;
import net.neoforged.neoforge.registries.*;
import net.neoforged.neoforge.event.village.WandererTradesEvent;

/** The ending leaves ordinary-world traces without altering existing loot or traders. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class FinaleLoot extends LootModifier {
    public static final MapCodec<FinaleLoot> CODEC=RecordCodecBuilder.mapCodec(instance->codecStart(instance).apply(instance,FinaleLoot::new));
    private static final DeferredRegister<MapCodec<? extends IGlobalLootModifier>> CODECS=DeferredRegister.create(NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS,TheOldestHouse.MOD_ID);
    static{CODECS.register("finale_notes",()->CODEC);}
    public static void register(IEventBus bus){CODECS.register(bus);}
    public FinaleLoot(LootItemCondition[] conditions){super(conditions);}
    public static ItemStack note(int variant){return HouseWriting.book("Another loose page","?",HouseWriting.WritingStyle.ZAMPANO,List.of(switch(Math.floorMod(variant,3)){
        case 0->"The empty lot appears in the photograph.\n\nThe staircase does not.\n\nSomeone has written the measurement on the back anyway.";
        case 1->"An account in a different hand.\n\nThe door was closed when I arrived. I heard the answer before I knocked.";
        default->"There was no room for this page in the original binding.\n\nThere is room now.";}));}
    @Override protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generated,LootContext context){
        if(context.getQueriedLootTableId().getPath().startsWith("chests/")&&FinaleProgress.world(context.getLevel().getServer()).getBoolean("Ended")&&context.getRandom().nextFloat()<.07F)
            generated.add(note(context.getRandom().nextInt(3)));
        return generated;
    }
    @Override public MapCodec<? extends IGlobalLootModifier> codec(){return CODEC;}
    @SubscribeEvent public static void trades(WandererTradesEvent event){event.getGenericTrades().add((trader,random)->{
        if(trader.level().getServer()==null||!FinaleProgress.world(trader.level().getServer()).getBoolean("Ended"))return null;
        return new MerchantOffer(new ItemCost(Items.EMERALD,1),note(random.nextInt(3)),1,0,0);
    });}
}
