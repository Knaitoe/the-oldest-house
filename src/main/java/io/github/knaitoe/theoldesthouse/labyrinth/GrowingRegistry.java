package io.github.knaitoe.theoldesthouse.labyrinth;

import com.mojang.serialization.MapCodec;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.*;

/**
 * 0.4.350, Growing Things (docs/GROWING_THINGS_0_4_350.md): the Barretts' fittings, the supper that is carried upstairs (a plate, the
 * pot, the loaf), the dry seed head the story leaves in a reader's hand, and the story's sounds. Drawn by
 * tools/draw_growing_things_art.py; heard from tools/generate_growing_things_audio.py.
 */
public final class GrowingRegistry {
    private static final DeferredRegister.Blocks BLOCKS=DeferredRegister.createBlocks(TheOldestHouse.MOD_ID);
    private static final DeferredRegister.Items ITEMS=DeferredRegister.createItems(TheOldestHouse.MOD_ID);
    private static final DeferredRegister<MapCodec<? extends Block>> CODECS=DeferredRegister.create(Registries.BLOCK_TYPE,TheOldestHouse.MOD_ID);
    private static final DeferredRegister<SoundEvent> SOUNDS=DeferredRegister.create(Registries.SOUND_EVENT,TheOldestHouse.MOD_ID);
    private static BlockBehaviour.Properties fixed(Block like){return BlockBehaviour.Properties.ofFullCopy(like).strength(-1,3600000).noLootTable();}
    public static final DeferredHolder<MapCodec<? extends Block>,MapCodec<GrowingBlock>> FITTING_TYPE=CODECS.register("growing_fitting",()->GrowingBlock.CODEC);
    public static final DeferredBlock<GrowingBlock> FITTING=BLOCKS.registerBlock("growing_fitting",GrowingBlock::new,fixed(Blocks.OAK_PLANKS).noOcclusion());
    /** A plate with a name scratched into its rim, as all the family's plates have. Carried up the stairs, or not. */
    public static final DeferredItem<Item> PLATE=ITEMS.registerSimpleItem("growing_plate",new Item.Properties().stacksTo(1));
    /** The supper pot, and the loaf. */
    public static final DeferredItem<Item> POT=ITEMS.registerSimpleItem("growing_pot",new Item.Properties().stacksTo(1));
    public static final DeferredItem<Item> LOAF=ITEMS.registerSimpleItem("growing_loaf",new Item.Properties().stacksTo(1));
    /** A dry seed head off the Barretts' kitchen sill. Carried, it leans the dealing a little toward places where things grow; pressed
     * into the ground, nothing comes of it. */
    public static final DeferredItem<SeedHead> SEED_HEAD=ITEMS.registerItem("growing_seed_head",SeedHead::new,new Item.Properties().stacksTo(1));
    private static DeferredHolder<SoundEvent,SoundEvent> sound(String id){return SOUNDS.register(id,()->SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,id)));}
    /** The kitchen floor's boards lifting; the window frame giving; a spade in the ground; the shoots, moving with nothing moving them. */
    public static final DeferredHolder<SoundEvent,SoundEvent> BOARDS=sound("growing.boards"),FRAME=sound("growing.frame"),SPADE=sound("growing.spade"),SHOOTS=sound("growing.shoots");
    private GrowingRegistry(){}
    public static void register(IEventBus bus){CODECS.register(bus);BLOCKS.register(bus);ITEMS.register(bus);SOUNDS.register(bus);}

    /** Pressed into earth or grass it goes in, and nothing comes up. */
    public static final class SeedHead extends Item {
        public SeedHead(Properties p){super(p);}
        @Override public InteractionResult useOn(UseOnContext c){
            var at=c.getClickedPos();var s=c.getLevel().getBlockState(at);
            if(!s.is(BlockTags.DIRT))return InteractionResult.PASS;
            if(!c.getLevel().isClientSide&&c.getPlayer()!=null){
                c.getLevel().playSound(null,at,SoundEvents.ROOTED_DIRT_PLACE,SoundSource.BLOCKS,.6F,1.1F);
                c.getPlayer().displayClientMessage(Component.literal("You press the seed head into the ground. Nothing comes of it.").withStyle(ChatFormatting.GRAY,ChatFormatting.ITALIC),true);
                c.getItemInHand().shrink(1);
            }
            return InteractionResult.sidedSuccess(c.getLevel().isClientSide);
        }
    }
}
