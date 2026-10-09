package io.github.knaitoe.theoldesthouse.client;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.network.EndingBookPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RenderHighlightEvent;
import io.github.knaitoe.theoldesthouse.labyrinth.LiteraryRegistry;
import io.github.knaitoe.theoldesthouse.labyrinth.LiteraryPropBlock;

@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class EndingBookClient {
    private static Object world;private static BlockPos at;private static boolean visible;private static int lease;
    public static void accept(EndingBookPayload p) { world=Minecraft.getInstance().level;at=p.at();visible=p.visible();lease=30; }
    public static boolean hasView(BlockPos pos) { return lease>0&&pos.equals(at)&&Minecraft.getInstance().level==world; }
    public static boolean visible(BlockPos pos) { return visible&&hasView(pos); }
    @SubscribeEvent public static void tick(ClientTickEvent.Post e) { if(lease>0)lease--;if(Minecraft.getInstance().level!=world)lease=0; }
    @SubscribeEvent public static void logout(ClientPlayerNetworkEvent.LoggingOut e) { lease=0;at=null;world=null; }
    @SubscribeEvent public static void highlight(RenderHighlightEvent.Block e) { var mc=Minecraft.getInstance();if(mc.level==null)return;var pos=e.getTarget().getBlockPos();var state=mc.level.getBlockState(pos);if(state.is(LiteraryRegistry.PROP.get())&&state.getValue(LiteraryPropBlock.KIND)==LiteraryPropBlock.Kind.LEDGER&&!visible(pos))e.setCanceled(true); }
}
