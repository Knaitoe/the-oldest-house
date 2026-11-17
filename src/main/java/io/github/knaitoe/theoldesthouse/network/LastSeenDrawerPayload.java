package io.github.knaitoe.theoldesthouse.network;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.netty.buffer.ByteBuf;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * 0.4.671, Last Seen (docs/LAST_SEEN_0_4_671.md): Nora's drawer pulled out on the day after. Each folder is a reader who has had the
 * night: whose face is clipped to it (the client draws it from that reader's own skin, their House body where they wear one), the form's
 * heading, the file number, the name, the day the House took them in, and Nora's note in pencil. The opening reader's own comes first.
 */
public record LastSeenDrawerPayload(List<Folder> folders) implements CustomPacketPayload {
    public record Folder(UUID reader,String heading,String file,String name,String on,String note){
        static final StreamCodec<ByteBuf,Folder> CODEC=StreamCodec.composite(UUIDUtil.STREAM_CODEC,Folder::reader,ByteBufCodecs.STRING_UTF8,Folder::heading,
            ByteBufCodecs.STRING_UTF8,Folder::file,ByteBufCodecs.STRING_UTF8,Folder::name,ByteBufCodecs.STRING_UTF8,Folder::on,ByteBufCodecs.STRING_UTF8,Folder::note,Folder::new);
    }
    public static final Type<LastSeenDrawerPayload> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"last_seen_drawer"));
    public static final StreamCodec<ByteBuf,LastSeenDrawerPayload> STREAM_CODEC=StreamCodec.composite(Folder.CODEC.apply(ByteBufCodecs.list(64)),LastSeenDrawerPayload::folders,LastSeenDrawerPayload::new);
    @Override public Type<LastSeenDrawerPayload> type(){return TYPE;}
}
