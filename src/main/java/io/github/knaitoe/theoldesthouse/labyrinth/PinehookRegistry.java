package io.github.knaitoe.theoldesthouse.labyrinth;

import com.mojang.serialization.MapCodec;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.*;

/**
 * 0.4.364, Last Weekend at Pinehook (docs/PINEHOOK_0_4_364.md): the cabin's fittings, the things readers bring to the weekend and find there,
 * and the evening's sounds. Drawn by tools/draw_pinehook_art.py; heard from tools/generate_pinehook_audio.py.
 */
public final class PinehookRegistry {
    private static final DeferredRegister.Blocks BLOCKS=DeferredRegister.createBlocks(TheOldestHouse.MOD_ID);
    private static final DeferredRegister.Items ITEMS=DeferredRegister.createItems(TheOldestHouse.MOD_ID);
    private static final DeferredRegister<MapCodec<? extends Block>> CODECS=DeferredRegister.create(Registries.BLOCK_TYPE,TheOldestHouse.MOD_ID);
    private static final DeferredRegister<SoundEvent> SOUNDS=DeferredRegister.create(Registries.SOUND_EVENT,TheOldestHouse.MOD_ID);
    private static BlockBehaviour.Properties fixed(Block like){return BlockBehaviour.Properties.ofFullCopy(like).strength(-1,3600000).noLootTable();}
    public static final DeferredHolder<MapCodec<? extends Block>,MapCodec<PinehookBlock>> FITTING_TYPE=CODECS.register("pinehook_fitting",()->PinehookBlock.CODEC);
    public static final DeferredBlock<PinehookBlock> FITTING=BLOCKS.registerBlock("pinehook_fitting",PinehookBlock::new,fixed(Blocks.SPRUCE_PLANKS).noOcclusion().lightLevel(PinehookBlock::light));
    /** What the parts bring: the heavy cooler, the camera and the torch, the mixtape, the battered portable player, the rented film, the
     * first-aid bag. And what is found: the elastic strap from the bag behind the shed. */
    public static final DeferredItem<Thing> COOLER=thing("pinehook_cooler"),CAMERA=thing("pinehook_camera"),TORCH=thing("pinehook_torch"),MIXTAPE=thing("pinehook_mixtape"),
        PLAYER=thing("pinehook_player"),TAPE=thing("pinehook_tape"),FIRSTAID=thing("pinehook_firstaid"),ELASTIC=thing("pinehook_elastic");
    /** 0.4.365, found in the night: the coverall's tie from the dock, the mask and coverall bundled under the rowboat's tarp, Casey's plan. */
    public static final DeferredItem<Thing> TIE=thing("pinehook_tie"),COSTUME=thing("pinehook_costume"),PLAN=thing("pinehook_plan");
    private static DeferredItem<Thing> thing(String id){return ITEMS.registerItem(id,Thing::new,new Item.Properties().stacksTo(1));}
    private static DeferredHolder<SoundEvent,SoundEvent> sound(String id){return SOUNDS.register(id,()->SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,id)));}
    /** Knuckles on a window; the breaker going and the cabin's hum with it; something heavy going over in the shed; a camera's flash; a tape
     * going into the deck. */
    public static final DeferredHolder<SoundEvent,SoundEvent> KNOCK=sound("pinehook.knock"),BREAKER=sound("pinehook.breaker"),CRASH=sound("pinehook.crash"),FLASH=sound("pinehook.flash"),
        TAPE_IN=sound("pinehook.tape");
    private PinehookRegistry(){}
    public static void register(IEventBus bus){CODECS.register(bus);BLOCKS.register(bus);ITEMS.register(bus);SOUNDS.register(bus);}

    /** One of the weekend's things: used in the air, used on a block, or used on someone; the cabin decides what each does. */
    public static final class Thing extends Item {
        public Thing(Properties p){super(p);}
        @Override public InteractionResultHolder<ItemStack> use(Level level,Player player,InteractionHand hand){
            var stack=player.getItemInHand(hand);
            if(player instanceof ServerPlayer p&&Pinehook.useThing(p,stack,null))return InteractionResultHolder.consume(stack);
            return InteractionResultHolder.pass(stack);
        }
        @Override public InteractionResult useOn(UseOnContext c){
            if(c.getLevel().isClientSide)return InteractionResult.SUCCESS;
            if(!(c.getPlayer() instanceof ServerPlayer p))return InteractionResult.PASS;
            return Pinehook.useThingOn(p,c.getItemInHand(),c.getClickedPos())?InteractionResult.CONSUME:InteractionResult.PASS;
        }
        @Override public InteractionResult interactLivingEntity(ItemStack stack,Player player,LivingEntity target,InteractionHand hand){
            if(player.level().isClientSide)return InteractionResult.SUCCESS;
            if(!(player instanceof ServerPlayer p))return InteractionResult.PASS;
            return Pinehook.useThing(p,stack,target)?InteractionResult.CONSUME:InteractionResult.PASS;
        }
    }
}
