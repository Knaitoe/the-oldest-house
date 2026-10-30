package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * What the house of Usher's last night costs a reader who did not get out (0.4.184), as the Goatman's comes home with
 * a reader who let it in. The scratching Roderick heard under the lid follows them: now and then, wherever they are,
 * it is under the floor beneath their feet, a little louder for each day since. It never harms them, and nobody else
 * hears it. It ends only when that reader gets out over the causeway on a later night, which is also when the house's
 * stone becomes theirs.
 */
@EventBusSubscriber(modid = TheOldestHouse.MOD_ID)
public final class UsherHaunt {
    /** Once every three to seven minutes. */
    public static final int EVERY = 3600, SPREAD = 4800;
    private static final Map<UUID, Long> NEXT = new HashMap<>();
    private UsherHaunt() {}

    /** The reader the house closed over and who has not got out since. */
    public static boolean haunted(ServerPlayer p) {
        return LiteraryVignettes.personal(LabyrinthData.get(p.server), p.getUUID(), LabyrinthPlace.USHER).getBoolean("Buried0507");
    }
    /** When the next scratching comes; for tests. */
    public static void due(ServerPlayer p, long at) { NEXT.put(p.getUUID(), at); }

    @SubscribeEvent public static void tick(ServerTickEvent.Post e) {
        var server = e.getServer();
        if (server.getTickCount() % 20 != 0) return;
        for (var p : server.getPlayerList().getPlayers()) {
            if (!p.isAlive() || p.isSpectator() || LiteraryVignettes.current(p) == LabyrinthPlace.USHER) continue;
            var own = LiteraryVignettes.personal(LabyrinthData.get(server), p.getUUID(), LabyrinthPlace.USHER);
            if (!own.getBoolean("Buried0507")) { NEXT.remove(p.getUUID()); continue; }
            long now = p.serverLevel().getGameTime();
            Long next = NEXT.get(p.getUUID());
            if (next == null) { NEXT.put(p.getUUID(), now + EVERY + p.getRandom().nextInt(SPREAD)); continue; }
            if (now < next) continue;
            int days = Math.max(0, (int) (server.overworld().getDayTime() / 24000L) + 1 - own.getInt("BuriedDay0507"));
            LiteraryVignettes.heard(p, LiteraryRegistry.SCRATCH.get(), p.blockPosition().below(2), Math.min(.9F, .3F + .1F * days), .8F);
            own.putInt("Scratched0507", own.getInt("Scratched0507") + 1);
            LiteraryVignettes.save(LabyrinthData.get(server), p.getUUID(), LabyrinthPlace.USHER, own);
            NEXT.put(p.getUUID(), now + EVERY + p.getRandom().nextInt(SPREAD));
        }
    }
    @SubscribeEvent public static void logout(PlayerEvent.PlayerLoggedOutEvent e) { NEXT.remove(e.getEntity().getUUID()); }
}
