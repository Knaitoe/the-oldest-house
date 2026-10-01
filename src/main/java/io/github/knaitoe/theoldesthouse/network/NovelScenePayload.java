package io.github.knaitoe.theoldesthouse.network;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.*;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
/** Short-lived presentation only; the server owns every physical scene and timer. */
public record NovelScenePayload(int mode,int elapsed,String caption,int captionTicks,float shake) implements CustomPacketPayload {
    public static final Type<NovelScenePayload> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"novel_scene"));
    public static final StreamCodec<ByteBuf,NovelScenePayload> STREAM_CODEC=StreamCodec.composite(ByteBufCodecs.VAR_INT,NovelScenePayload::mode,ByteBufCodecs.VAR_INT,NovelScenePayload::elapsed,ByteBufCodecs.STRING_UTF8,NovelScenePayload::caption,ByteBufCodecs.VAR_INT,NovelScenePayload::captionTicks,ByteBufCodecs.FLOAT,NovelScenePayload::shake,NovelScenePayload::new);
    @Override public Type<NovelScenePayload> type(){return TYPE;}
}
