package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseConfig;
import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import io.github.knaitoe.theoldesthouse.network.HouseBodiesPayload;
import io.github.knaitoe.theoldesthouse.network.HousePackets;
import io.netty.buffer.ByteBuf;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.IntBinaryOperator;
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
 * The House's body (the owner's decisions, October 29; built in 0.4.163). A reader whose own skin is not human (a creeper,
 * a robot, a mascot) can't feel the coats, glasses and portraits made for a person, so inside the House they wear a human
 * body the House chose from its read: age from {@code AGE}, clothes from a settled {@code SELF}, the hair from {@code HAIR}'s
 * routine, and the tone and hair colour of the default skin Minecraft gives their account until the self-portrait is built.
 * Their own client checks their own face and says only yes or no. Everyone sees the body, which is the one place a read
 * leaves the server; readers with human skins never have theirs sent anywhere. Their own skin returns in the manor and outside.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class HouseBodies {
    private HouseBodies(){}
    public static final List<String> AGES=List.of("young","middle","older"),PRESENTATIONS=List.of("plain","masc","fem"),CUTS=List.of("short","coils","braids","long","close");
    /** A body: indices into the three lists, and a tone and hair colour as RGB, or -1 for the account's default skin's. */
    public record Body(int age,int presentation,int cut,int tone,int hair){
        public static final StreamCodec<ByteBuf,Body> STREAM_CODEC=StreamCodec.composite(ByteBufCodecs.VAR_INT,Body::age,ByteBufCodecs.VAR_INT,Body::presentation,ByteBufCodecs.VAR_INT,Body::cut,ByteBufCodecs.INT,Body::tone,ByteBufCodecs.INT,Body::hair,Body::new);
        public Body{age=Math.floorMod(age,AGES.size());presentation=Math.floorMod(presentation,PRESENTATIONS.size());cut=Math.floorMod(cut,CUTS.size());}
        /** The painted sheet, {@code textures/entity/house_body/<sheet>.png}. */
        public String sheet(){return AGES.get(age)+"_"+PRESENTATIONS.get(presentation)+"_"+CUTS.get(cut);}
        /** 0.4.195: not a body at all, only the tone drawn at the kitchen table, for the reader's own skin. */
        public boolean own(){return hair==OWN;}
    }
    /** The hair value that marks a reader's own skin worn in their drawn tone rather than a painted body. */
    public static final int OWN=Integer.MIN_VALUE;

    // ---- The check each reader's own client makes of their own face.
    /** How much of a face must be in human skin colours. The nine default skins read 44% or more; mobs and machines read none. */
    public static final int FACE_SHARE=25;
    /** Within the range of human skin colours, as a box in YCbCr: any lightness above near-black, the warm chroma skin has. */
    public static boolean skinColour(int r,int g,int b){
        double y=.299*r+.587*g+.114*b,cb=128-.168736*r-.331264*g+.5*b,cr=128+.5*r-.418688*g-.081312*b;
        return y>=30&&cr>=133&&cr<=180&&cb>=77&&cb<=130;
    }
    /** The face as others see it: the front of the head with the hat layer over it. {@code argb} reads the skin in its 64-texel grid. */
    public static boolean humanFace(IntBinaryOperator argb){
        int skin=0,seen=0;
        for(int y=8;y<16;y++)for(int x=8;x<16;x++){
            int hat=argb.applyAsInt(x+32,y),c=hat>>>24>=128?hat:argb.applyAsInt(x,y);if(c>>>24<128)continue;
            seen++;if(skinColour(c>>16&255,c>>8&255,c&255))skin++;
        }
        return seen>0&&skin*100>=seen*FACE_SHARE;
    }

    // ---- Which readers wear one.
    private static final Map<UUID,Boolean> INHUMAN=new ConcurrentHashMap<>();
    private static Map<UUID,Body> sent=Map.of();
    public static void report(ServerPlayer reader,boolean human){if(human)INHUMAN.remove(reader.getUUID());else INHUMAN.put(reader.getUUID(),true);}
    public static boolean inhuman(UUID reader){return INHUMAN.containsKey(reader);}
    /** The body the read chooses. Unread traits take the neutral choices: a young adult, plain clothes, short hair. */
    public static Body body(LabyrinthData d,UUID reader){
        int age=switch(HouseRead.value(d,reader,HouseRead.Trait.AGE,false)){case "ANALOG"->2;case "TRANSITIONAL"->1;default->0;};
        int presentation=switch(HouseRead.value(d,reader,HouseRead.Trait.SELF,true)){case "MASC"->1;case "FEM"->2;default->0;};
        int cut=switch(HouseRead.value(d,reader,HouseRead.Trait.HAIR,false)){case "WRAPPED"->1;case "BRAIDED_AND_OILED"->2;case "BRUSHED_DRY"->3;case "WASHED_AND_BARBERED"->4;default->0;};
        // 0.4.168: the tone and hair colour the reader drew at the kitchen table, when there is one.
        return new Body(age,presentation,cut,HouseReadKitchen.tone(d,reader),HouseReadKitchen.hair(d,reader));
    }
    /** The body a reader is wearing now, if any: only an inhuman skin, only inside the House, and only while the option is on. */
    public static @Nullable Body worn(ServerPlayer reader){
        return HouseConfig.houseBodies()&&inhuman(reader.getUUID())&&inside(reader)?body(LabyrinthData.get(reader.server),reader.getUUID()):null;
    }
    /** The House beyond the manor: the manor's own rooms, and the rooms between them, keep the reader's own skin. */
    public static boolean inside(ServerPlayer reader){
        var dimension=reader.level().dimension();
        return HouseDimensions.isHouseDimension(dimension)&&!dimension.equals(HouseDimensions.BETWEEN)&&!io.github.knaitoe.theoldesthouse.house.HouseExperience.inManor(reader,reader.blockPosition());
    }
    /**
     * 0.4.195 (owner: "skin tone isn't being properly applied"): a human-skinned reader who drew their tone at the kitchen
     * table wears it on their own skin, wherever a body would be worn. Only the tone is sent; their skin stays on their client,
     * and every client recolours that reader's own skin to it, shading and all.
     */
    public static @Nullable Body toned(ServerPlayer reader){
        if(!HouseConfig.houseBodies()||inhuman(reader.getUUID())||!inside(reader))return null;
        int tone=HouseReadKitchen.tone(LabyrinthData.get(reader.server),reader.getUUID());
        return tone<0?null:new Body(0,0,0,tone,OWN);
    }
    public static Map<UUID,Body> worn(MinecraftServer server){
        var out=new LinkedHashMap<UUID,Body>();
        for(var p:server.getPlayerList().getPlayers()){var b=worn(p);if(b==null)b=toned(p);if(b!=null)out.put(p.getUUID(),b);}
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
    @SubscribeEvent public static void logout(PlayerEvent.PlayerLoggedOutEvent e){INHUMAN.remove(e.getEntity().getUUID());}
    @SubscribeEvent public static void stopped(ServerStoppedEvent e){INHUMAN.clear();sent=Map.of();}
}
