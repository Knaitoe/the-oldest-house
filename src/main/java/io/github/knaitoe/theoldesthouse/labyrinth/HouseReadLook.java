package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.network.HousePackets;
import io.github.knaitoe.theoldesthouse.network.HouseReadLookPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

/**
 * The House's version of a reader, as worn layers (0.4.159): the coat they took, once their presentation is settled; reading
 * glasses for an older read; a silk wrap for a wrapped routine; and an obscured face for a reader the glass did not agree with.
 * Only ever sent to that reader's own client.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class HouseReadLook {
    private HouseReadLook(){}
    public static HouseReadLookPayload of(ServerPlayer p){
        var d=LabyrinthData.get(p.server);var id=p.getUUID();var self=HouseRead.category(d,id,HouseRead.Trait.SELF);
        String coat=self.filter(c->c.strength()==HouseRead.Strength.SETTLED||c.value().equals("UNSEEN")).isPresent()?HouseRead.seen(d,id).getCompound(HouseReadSites.Room.COATROOM.key()).getString("Coat"):"";
        // 0.4.167: the style the reader gave themselves at the bathroom glass, kept on the House's version of them.
        var bathroom=HouseRead.seen(d,id).getCompound(HouseReadSites.Room.BATHROOM.key());int hair=HouseRead.enabled(HouseRead.Trait.HAIR)&&bathroom.contains("Used")?bathroom.getInt("Used"):-1;
        return new HouseReadLookPayload(coat,HouseRead.value(d,id,HouseRead.Trait.AGE,false).equals("ANALOG"),HouseRead.value(d,id,HouseRead.Trait.HAIR,false).equals("WRAPPED"),self.map(c->c.value().equals("UNSEEN")).orElse(false),hair);
    }
    public static void send(ServerPlayer p){if(p.connection!=null)HousePackets.send(p,of(p));}
    @SubscribeEvent public static void login(PlayerEvent.PlayerLoggedInEvent e){if(e.getEntity() instanceof ServerPlayer p)send(p);}
    @SubscribeEvent public static void respawn(PlayerEvent.PlayerRespawnEvent e){if(e.getEntity() instanceof ServerPlayer p)send(p);}
}
