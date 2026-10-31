package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseConfig;
import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import io.github.knaitoe.theoldesthouse.network.HouseBodiesPayload;
import io.github.knaitoe.theoldesthouse.network.HousePackets;
import io.netty.buffer.ByteBuf;
import java.util.*;
import javax.annotation.Nullable;
import net.minecraft.network.codec.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * The House's body (the owner's decisions, October 29; built in 0.4.163). Inside the House a reader wears a human body the
 * House chose from its read: age from {@code AGE}, clothes from a settled {@code SELF}, the hair from {@code HAIR}'s routine,
 * and the tone, hair colour, body and face drawn at the kitchen table, or the tone and hair colour of the default skin
 * Minecraft gives their account until they draw them. Everyone sees the body, which is the one place a read leaves the
 * server. Their own skin returns in the manor and outside.
 *
 * <p>0.4.201 (owner: "the skin layering over human minecraft skins is more than a little faulty. if you can't fix that
 * confidently, just give all players a house skin inside the house."): until then only a reader whose own skin was not human
 * wore the body, and a human skin was recoloured to the drawn tone on every client. No recolouring of an arbitrary skin can
 * be trusted (its shading, lips and hat layer fall outside any match and keep the old tone), so every reader wears the body,
 * and nobody's own skin is read or changed.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class HouseBodies {
    private HouseBodies(){}
    public static final List<String> AGES=List.of("young","middle","older"),PRESENTATIONS=List.of("plain","masc","fem"),CUTS=List.of("short","coils","braids","long","close");
    /**
     * A body: indices into the three lists, and a tone and hair colour as RGB, or -1 for the account's default skin's. 0.4.196:
     * and the face drawn at the kitchen table, 1 to 5 in {@link HouseReadKitchen#FACES}, or 0 for the body's own.
     */
    public record Body(int age,int presentation,int cut,int tone,int hair,int face){
        public static final StreamCodec<ByteBuf,Body> STREAM_CODEC=StreamCodec.composite(ByteBufCodecs.VAR_INT,Body::age,ByteBufCodecs.VAR_INT,Body::presentation,ByteBufCodecs.VAR_INT,Body::cut,ByteBufCodecs.INT,Body::tone,ByteBufCodecs.INT,Body::hair,ByteBufCodecs.VAR_INT,Body::face,Body::new);
        public Body{age=Math.floorMod(age,AGES.size());presentation=Math.floorMod(presentation,PRESENTATIONS.size());cut=Math.floorMod(cut,CUTS.size());face=Math.floorMod(face,HouseReadKitchen.FACES.size()+1);}
        public Body(int age,int presentation,int cut,int tone,int hair){this(age,presentation,cut,tone,hair,0);}
        /** The painted sheet, {@code textures/entity/house_body/<sheet>.png}. */
        public String sheet(){return AGES.get(age)+"_"+PRESENTATIONS.get(presentation)+"_"+CUTS.get(cut);}
    }

    // ---- Skin colours, for the account's default skin and the faces drawn on a body.
    /** Within the range of human skin colours, as a box in YCbCr: any lightness above near-black, the warm chroma skin has. */
    public static boolean skinColour(int r,int g,int b){
        double y=.299*r+.587*g+.114*b,cb=128-.168736*r-.331264*g+.5*b,cr=128+.5*r-.418688*g-.081312*b;
        return y>=30&&cr>=133&&cr<=180&&cb>=77&&cb<=130;
    }

    // ---- Who wears one.
    private static Map<UUID,Body> sent=Map.of();
    /** The body the read chooses. Unread traits take the neutral choices: a young adult, plain clothes, short hair. */
    public static Body body(LabyrinthData d,UUID reader){
        int age=switch(HouseRead.value(d,reader,HouseRead.Trait.AGE,false)){case "ANALOG"->2;case "TRANSITIONAL"->1;default->0;};
        // 0.4.196: the body drawn at the kitchen table is the reader's own choice, and comes before the House's read of them.
        int form=HouseReadKitchen.form(d,reader);
        int presentation=form>0?form:switch(HouseRead.value(d,reader,HouseRead.Trait.SELF,true)){case "MASC"->1;case "FEM"->2;default->0;};
        int cut=switch(HouseRead.value(d,reader,HouseRead.Trait.HAIR,false)){case "WRAPPED"->1;case "BRAIDED_AND_OILED"->2;case "BRUSHED_DRY"->3;case "WASHED_AND_BARBERED"->4;default->0;};
        // 0.4.168: the tone and hair colour the reader drew at the kitchen table, when there is one.
        return new Body(age,presentation,cut,HouseReadKitchen.tone(d,reader),HouseReadKitchen.hair(d,reader),HouseReadKitchen.mood(d,reader));
    }
    /** The body a reader is wearing now, if any: every reader, only inside the House, and only while the option is on. */
    public static @Nullable Body worn(ServerPlayer reader){
        return HouseConfig.houseBodies()&&inside(reader)?body(LabyrinthData.get(reader.server),reader.getUUID()):null;
    }
    /** The House beyond the manor: the manor's own rooms, and the rooms between them, keep the reader's own skin. */
    public static boolean inside(ServerPlayer reader){
        var dimension=reader.level().dimension();
        return HouseDimensions.isHouseDimension(dimension)&&!dimension.equals(HouseDimensions.BETWEEN)&&!io.github.knaitoe.theoldesthouse.house.HouseExperience.inManor(reader,reader.blockPosition());
    }
    public static Map<UUID,Body> worn(MinecraftServer server){
        var out=new LinkedHashMap<UUID,Body>();
        for(var p:server.getPlayerList().getPlayers()){var b=worn(p);if(b!=null)out.put(p.getUUID(),b);}
        return out;
    }
    private static HouseBodiesPayload payload(Map<UUID,Body> bodies){var list=new ArrayList<HouseBodiesPayload.Entry>();bodies.forEach((id,b)->list.add(new HouseBodiesPayload.Entry(id,b)));return new HouseBodiesPayload(list);}
    /** Every client is told whenever the bodies change: a reader going in or out, a read settling, a skin changing. */
    public static void sync(MinecraftServer server){
        var now=worn(server);if(now.equals(sent))return;sent=Map.copyOf(now);var payload=payload(now);
        for(var p:server.getPlayerList().getPlayers())if(p.connection!=null)HousePackets.send(p,payload);
    }
    @SubscribeEvent public static void tick(ServerTickEvent.Post e){if(e.getServer().getTickCount()%10==7)sync(e.getServer());}
    @SubscribeEvent public static void login(PlayerEvent.PlayerLoggedInEvent e){if(e.getEntity() instanceof ServerPlayer p&&p.connection!=null)HousePackets.send(p,payload(worn(p.server)));}
    @SubscribeEvent public static void stopped(ServerStoppedEvent e){sent=Map.of();}
}
