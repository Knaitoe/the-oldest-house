package io.github.knaitoe.theoldesthouse.labyrinth;

import com.mojang.serialization.MapCodec;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.*;

/**
 * 0.4.407, The Drawing (docs/THE_DRAWING_0_4_407.md): the village square's materials and fittings, the folded slip, the smooth stone,
 * the glass of lemonade and the jug it is poured from, and the square's sounds. Drawn by tools/draw_drawing_art.py; heard from
 * tools/generate_drawing_audio.py.
 */
public final class DrawingRegistry {
    private static final DeferredRegister.Blocks BLOCKS=DeferredRegister.createBlocks(TheOldestHouse.MOD_ID);
    private static final DeferredRegister.Items ITEMS=DeferredRegister.createItems(TheOldestHouse.MOD_ID);
    private static final DeferredRegister<MapCodec<? extends Block>> CODECS=DeferredRegister.create(Registries.BLOCK_TYPE,TheOldestHouse.MOD_ID);
    private static final DeferredRegister<SoundEvent> SOUNDS=DeferredRegister.create(Registries.SOUND_EVENT,TheOldestHouse.MOD_ID);
    private static BlockBehaviour.Properties fixed(Block like){return BlockBehaviour.Properties.ofFullCopy(like).strength(-1,3600000).noLootTable();}
    private static final java.util.Map<String,DeferredBlock<Block>> MATERIALS=new java.util.LinkedHashMap<>();
    /** The square's flagstones with grass in the joints, the painted weatherboard of the post office and the houses, the bank's brick. */
    public static final java.util.List<String> MATERIAL_NAMES=java.util.List.of("flagstone","weatherboard","brick");
    static{for(String name:MATERIAL_NAMES)MATERIALS.put(name,BLOCKS.registerSimpleBlock("drawing_"+name,fixed(switch(name){case "flagstone"->Blocks.STONE;case "brick"->Blocks.BRICKS;default->Blocks.BIRCH_PLANKS;})));}
    public static Block material(String name){var block=MATERIALS.get(name);if(block==null)throw new IllegalArgumentException(name);return block.get();}
    public static final DeferredHolder<MapCodec<? extends Block>,MapCodec<DrawingBlock>> FITTING_TYPE=CODECS.register("drawing_fitting",()->DrawingBlock.CODEC);
    public static final DeferredBlock<DrawingBlock> FITTING=BLOCKS.registerBlock("drawing_fitting",DrawingBlock::new,fixed(Blocks.OAK_PLANKS).noOcclusion());
    /** A slip of paper folded twice, drawn from the box. Unfolded once everyone has drawn; the reader's is blank. */
    public static final DeferredItem<Item> SLIP=ITEMS.registerSimpleItem("drawing_slip",new Item.Properties().stacksTo(1));
    /** A smooth stone, warm from a child's pocket. */
    public static final DeferredItem<Item> STONE=ITEMS.registerSimpleItem("drawing_stone",new Item.Properties().stacksTo(1));
    /** A glass of lemonade, poured from the jug. It can be drunk. It does nothing. */
    public static final DeferredItem<Item> LEMONADE=ITEMS.register("drawing_lemonade",()->new Lemonade(new Item.Properties().stacksTo(1)));
    /** The jug the late arrival carries into the square; only ever in a villager's hand. */
    public static final DeferredItem<Item> JUG=ITEMS.registerSimpleItem("drawing_jug",new Item.Properties().stacksTo(1));
    private static DeferredHolder<SoundEvent,SoundEvent> sound(String id){return SOUNDS.register(id,()->SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,id)));}
    /** The crowd's murmur; a slip drawn out of the box or unfolded; stones picked up off the pile; lemonade poured; the day cut to white. */
    public static final DeferredHolder<SoundEvent,SoundEvent> MURMUR=sound("drawing.murmur"),SLIP_SOUND=sound("drawing.slip"),STONES=sound("drawing.stones"),POUR=sound("drawing.pour"),WHITE=sound("drawing.white");
    private DrawingRegistry(){}
    public static void register(IEventBus bus){CODECS.register(bus);BLOCKS.register(bus);ITEMS.register(bus);SOUNDS.register(bus);}

    /** The lemonade: drunk like a potion, and nothing comes of it. */
    static final class Lemonade extends Item {
        Lemonade(Properties p){super(p);}
        @Override public UseAnim getUseAnimation(ItemStack s){return UseAnim.DRINK;}
        @Override public int getUseDuration(ItemStack s,LivingEntity e){return 32;}
        @Override public InteractionResultHolder<ItemStack> use(Level l,Player p,InteractionHand hand){return ItemUtils.startUsingInstantly(l,p,hand);}
        @Override public ItemStack finishUsingItem(ItemStack s,Level l,LivingEntity e){if(!(e instanceof Player p&&p.getAbilities().instabuild))s.shrink(1);return s;}
    }
}
