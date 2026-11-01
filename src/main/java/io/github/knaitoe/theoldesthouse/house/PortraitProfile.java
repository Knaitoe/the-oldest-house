package io.github.knaitoe.theoldesthouse.house;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import java.util.UUID;
import javax.annotation.Nullable;

/**
 * A skin snapshot, not a URL supplied by a client. Kept on the original painting. 0.4.223 (owner: "character captured portraits
 * should use their house-overlays"): also the House's version of the reader at the moment of the death, {@code look}: the House's
 * body (age, presentation, cut, tone, hair, face) and what the glass shows them in (coat, reading glasses, an obscured face),
 * so the portrait is painted as the House has them. Empty for a portrait taken outside that (and for older paintings).
 */
public record PortraitProfile(UUID owner, String name, String texture, String signature, String look) {
    public PortraitProfile(UUID owner, String name, String texture, String signature) { this(owner, name, texture, signature, ""); }

    public static PortraitProfile of(GameProfile profile) {
        var properties = profile.getProperties().get("textures");
        var skin = properties.isEmpty() ? null : properties.iterator().next();
        return new PortraitProfile(profile.getId(), profile.getName(), skin == null ? "" : skin.value(),
                skin == null || skin.signature() == null ? "" : skin.signature());
    }

    /** The reader's skin and the House's version of them, as they are now. */
    public static PortraitProfile of(net.minecraft.server.level.ServerPlayer player) {
        var base = of(player.getGameProfile());
        return new PortraitProfile(base.owner(), base.name(), base.texture(), base.signature(), look(player));
    }

    /** {@code body=a,p,c,t,h,f;coat=id;glasses=0|1;veil=0|1}, the body only while the House's bodies are on. */
    static String look(net.minecraft.server.level.ServerPlayer player) {
        var out = new StringBuilder();
        if (HouseConfig.houseBodies()) {
            var b = io.github.knaitoe.theoldesthouse.labyrinth.HouseBodies.body(io.github.knaitoe.theoldesthouse.labyrinth.LabyrinthData.get(player.server), player.getUUID());
            out.append("body=").append(b.age()).append(',').append(b.presentation()).append(',').append(b.cut()).append(',').append(b.tone()).append(',').append(b.hair()).append(',').append(b.face()).append(';');
        }
        var seen = io.github.knaitoe.theoldesthouse.labyrinth.HouseReadLook.of(player);
        String coat = seen.coat() == null ? "" : seen.coat().replaceAll("[^a-z0-9_]", "");
        out.append("coat=").append(coat).append(";glasses=").append(seen.glasses() ? 1 : 0).append(";veil=").append(seen.unseen() ? 1 : 0);
        return out.toString();
    }

    public String encode() { return owner + "\n" + name + "\n" + texture + "\n" + signature + (look.isEmpty() ? "" : "\n" + look); }

    public static @Nullable PortraitProfile decode(String encoded) {
        if (encoded.length() > 16384) return null;
        var fields = encoded.split("\n", -1);
        if (fields.length != 4 && fields.length != 5 || fields[1].isEmpty() || fields[1].length() > 16) return null;
        try { return new PortraitProfile(UUID.fromString(fields[0]), fields[1], fields[2], fields[3], fields.length == 5 ? fields[4] : ""); }
        catch (IllegalArgumentException exception) { return null; }
    }

    /** One field of the look, or "" when it has none. */
    public String lookField(String key) {
        for (var part : look.split(";")) { int eq = part.indexOf('='); if (eq > 0 && part.substring(0, eq).equals(key)) return part.substring(eq + 1); }
        return "";
    }

    public GameProfile gameProfile() {
        var profile = new GameProfile(owner, name);
        if (!texture.isEmpty()) profile.getProperties().put("textures", signature.isEmpty()
                ? new Property("textures", texture) : new Property("textures", texture, signature));
        return profile;
    }
}
