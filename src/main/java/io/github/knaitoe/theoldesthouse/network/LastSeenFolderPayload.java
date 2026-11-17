package io.github.knaitoe.theoldesthouse.network;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * 0.4.668, Last Seen (docs/LAST_SEEN_0_4_668.md): a folder held open. Whose it is (the child's index, so the client draws their own face
 * from their skin), and the typed form as the server wrote it: the file number, the name, where and on what day they were last seen.
 */
public record LastSeenFolderPayload(int child,String file,String name,String where,String day) implements CustomPacketPayload {
    public static final Type<LastSeenFolderPayload> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"last_seen_folder"));
    public static final StreamCodec<ByteBuf,LastSeenFolderPayload> STREAM_CODEC=StreamCodec.composite(
        ByteBufCodecs.VAR_INT,LastSeenFolderPayload::child,ByteBufCodecs.STRING_UTF8,LastSeenFolderPayload::file,ByteBufCodecs.STRING_UTF8,LastSeenFolderPayload::name,
        ByteBufCodecs.STRING_UTF8,LastSeenFolderPayload::where,ByteBufCodecs.STRING_UTF8,LastSeenFolderPayload::day,LastSeenFolderPayload::new);
    @Override public Type<LastSeenFolderPayload> type(){return TYPE;}
}
