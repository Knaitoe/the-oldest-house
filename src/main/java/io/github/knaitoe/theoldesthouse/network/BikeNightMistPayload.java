package io.github.knaitoe.theoldesthouse.network;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.*;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
/**
 * 0.4.491, Bike night: how thick one reader's own mist is in the graveyard, 0 to 100 (BikeNightGraveyard.mist). Sent while
 * they are in it, and once as nothing when they leave; the client lets it go by itself if it stops hearing.
 *
 * <p>0.4.495: {@code over} is for how many ticks the mist is over everything, whatever its thickness: somebody in the Hollow has
 * gone down into it (BikeNightHollow). Nothing, but then.
 *
 * <p>0.4.500: and {@code share} is how much of it is over them, of a hundred: all of it for somebody who has gone down or been
 * caught, some of it for somebody a ghost has gone through (BikeNightSpecters).
 *
 * <p>0.4.510: and {@code dawn} is that it is dawn for them: the Hollow has had them, and their night is over. Sent with the
 * rest for as long as they are in the place; the client lets it go by itself if it stops hearing.
 */
public record BikeNightMistPayload(int mist,int over,int share,boolean dawn) implements CustomPacketPayload {
    public BikeNightMistPayload(int mist,int over){this(mist,over,100,false);}
    public BikeNightMistPayload(int mist,int over,int share){this(mist,over,share,false);}
    public static final Type<BikeNightMistPayload> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"bike_night_mist"));
    public static final StreamCodec<ByteBuf,BikeNightMistPayload> STREAM_CODEC=StreamCodec.composite(ByteBufCodecs.VAR_INT,BikeNightMistPayload::mist,ByteBufCodecs.VAR_INT,BikeNightMistPayload::over,ByteBufCodecs.VAR_INT,BikeNightMistPayload::share,ByteBufCodecs.BOOL,BikeNightMistPayload::dawn,BikeNightMistPayload::new);
    @Override public Type<BikeNightMistPayload> type(){return TYPE;}
}
