package io.github.knaitoe.theoldesthouse.client;

import io.github.knaitoe.theoldesthouse.labyrinth.HarriganPhoneItem;
import io.github.knaitoe.theoldesthouse.labyrinth.PhoneSounds;
import io.github.knaitoe.theoldesthouse.network.HarriganPhoneActionPayload;
import io.github.knaitoe.theoldesthouse.network.HarriganPhonePayload;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.FormattedCharSequence;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

/**
 * The player's phone (0.4.105): the time back home, and Harrigan, its one contact, to text or to call. The screen only
 * shows what the server keeps and sends what the reader did; whether a name reaches him is the server's to decide.
 */
public final class HarriganPhoneScreen extends Screen {
    private enum View { HOME, THREAD, CALL }
    private static final int ME = 0, HIM = 1;
    /** Two rings before the line does whatever it does now. */
    private static final int REVEAL = 120, RING_AGAIN = 70, UNANSWERED = 220;
    private CompoundTag state;
    private View view = View.HOME;
    private @Nullable EditBox input;
    private @Nullable CompoundTag call;
    private @Nullable SoundInstance ringing;
    private int age, callAge, typed;
    private boolean left, beeped, picked;
    private int px, py, pw, ph;

    public HarriganPhoneScreen(CompoundTag state) {
        super(Component.literal("Phone"));
        this.state = state;
    }

    /** A state from the server: open the phone, or bring an open one up to date. */
    public static void accept(HarriganPhonePayload payload) {
        var mc = Minecraft.getInstance();
        if (mc.screen instanceof HarriganPhoneScreen phone) phone.update(payload.state());
        else if (payload.open()) mc.setScreen(new HarriganPhoneScreen(payload.state()));
    }

    private void update(CompoundTag next) {
        int seen = newest();
        state = next;
        if (next.contains("Call")) call = next.getCompound("Call");
        for (Tag tag : thread())
            if (tag instanceof CompoundTag line && line.getInt("N") > seen && line.getInt("Who") == HIM) { play(PhoneSounds.RECEIVED.get(), .8F); break; }
        age = 0;
    }

    private int newest() {
        ListTag lines = thread();
        return lines.isEmpty() ? 0 : lines.getCompound(lines.size() - 1).getInt("N");
    }

    private ListTag thread() { return state.getList("Thread", Tag.TAG_COMPOUND); }
    private boolean service() { return state.getBoolean("Service"); }

    @Override protected void init() {
        ph = Math.min(250, height - 16);
        pw = Math.round(ph * .58F);
        px = (width - pw) / 2;
        py = (height - ph) / 2;
        rebuild();
    }

    private void rebuild() {
        String typedSoFar = input == null ? "" : input.getValue();
        clearWidgets();
        input = null;
        if(state.getBoolean("WaitingRoom"))return;
        int sx = px + 8, sw = pw - 16;
        switch (view) {
            case HOME -> {
                addRenderableWidget(Button.builder(Component.literal("Call"), b -> startCall()).bounds(sx, py + ph - 52, sw / 2 - 2, 20).build());
                addRenderableWidget(Button.builder(Component.literal("Message"), b -> show(View.THREAD)).bounds(sx + sw / 2 + 2, py + ph - 52, sw / 2 - 2, 20).build());
            }
            case THREAD -> {
                addRenderableWidget(Button.builder(Component.literal("<"), b -> show(View.HOME)).bounds(sx, py + 16, 16, 14).build());
                input = new EditBox(font, sx, py + ph - 30, sw - 40, 18, Component.literal("Message"));
                input.setMaxLength(32);
                input.setHint(Component.literal("A name").withStyle(ChatFormatting.DARK_GRAY));
                input.setValue(typedSoFar);
                addRenderableWidget(input);
                setInitialFocus(input);
                addRenderableWidget(Button.builder(Component.literal("Send"), b -> send(false)).bounds(sx + sw - 37, py + ph - 31, 37, 20).build());
            }
            case CALL -> {
                if (voicemailOpen()) {
                    input = new EditBox(font, sx, py + ph - 56, sw - 40, 18, Component.literal("Voicemail"));
                    input.setMaxLength(32);
                    input.setHint(Component.literal("A name").withStyle(ChatFormatting.DARK_GRAY));
                    input.setValue(typedSoFar);
                    addRenderableWidget(input);
                    setInitialFocus(input);
                    addRenderableWidget(Button.builder(Component.literal("Leave"), b -> send(true)).bounds(sx + sw - 37, py + ph - 57, 37, 20).build());
                }
                addRenderableWidget(Button.builder(Component.literal("End call").withStyle(ChatFormatting.RED), b -> endCall()).bounds(sx + sw / 4, py + ph - 30, sw / 2, 20).build());
            }
        }
    }

    private void show(View next) {
        view = next;
        rebuild();
    }

    private void startCall() {
        view = View.CALL;
        call = null;
        callAge = 0;
        typed = 0;
        left = beeped = picked = false;
        PacketDistributor.sendToServer(new HarriganPhoneActionPayload(HarriganPhoneActionPayload.CALL, ""));
        rebuild();
    }

    private void endCall() {
        stopRinging();
        call = null;
        show(View.HOME);
    }

    private String result() { return call == null ? "" : call.getString("Result"); }
    private boolean revealed() { return call != null && (result().equals("noservice") || callAge >= REVEAL); }
    private boolean voicemailOpen() {
        return revealed() && result().equals("voicemail") && beeped && !left;
    }

    private void send(boolean voicemail) {
        if (input == null) return;
        String text = input.getValue().trim();
        if (text.isEmpty()) return;
        PacketDistributor.sendToServer(new HarriganPhoneActionPayload(voicemail ? HarriganPhoneActionPayload.VOICEMAIL : HarriganPhoneActionPayload.TEXT, text));
        play(PhoneSounds.SENT.get(), .7F);
        input.setValue("");
        if (voicemail) {
            left = true;
            rebuild();
        }
    }

    @Override public void tick() {
        age++;
        if (view != View.CALL) return;
        callAge++;
        boolean wasOpen = voicemailOpen();
        // It rings until the line does something; with no service it never rings at all.
        boolean ringsOn = !result().equals("noservice") && (!revealed() || result().equals("unanswered") && callAge < UNANSWERED);
        if (ringsOn && (callAge == 1 || callAge % RING_AGAIN == 1)) ring();
        if (!ringsOn) stopRinging();
        if (revealed() && result().equals("voicemail")) {
            if (!picked) { picked = true; play(PhoneSounds.PICKUP.get(), .8F); }
            typed++;
            if (!beeped && typed > call.getString("Greeting").length() + 20) { beeped = true; play(PhoneSounds.BEEP.get(), .8F); }
        }
        if (wasOpen != voicemailOpen()) rebuild();
    }

    private void ring() {
        stopRinging();
        ringing = SimpleSoundInstance.forUI(PhoneSounds.RINGBACK.get(), 1F, .6F);
        Minecraft.getInstance().getSoundManager().play(ringing);
    }

    private void stopRinging() {
        if (ringing != null) Minecraft.getInstance().getSoundManager().stop(ringing);
        ringing = null;
    }

    private static void play(SoundEvent sound, float volume) {
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(sound, 1F, volume));
    }

    @Override public boolean keyPressed(int key, int scan, int modifiers) {
        if ((key == GLFW.GLFW_KEY_ENTER || key == GLFW.GLFW_KEY_KP_ENTER) && input != null && input.isFocused()) {
            send(view == View.CALL);
            return true;
        }
        return super.keyPressed(key, scan, modifiers);
    }

    @Override public void removed() {
        stopRinging();
        super.removed();
    }

    @Override public boolean isPauseScreen() { return false; }

    /** For the native visual proof only: show a view as it looks part way through, without sending anything. */
    void proof(int stage) {
        switch (stage) {
            case 1 -> view = View.THREAD;
            case 2 -> {
                view = View.CALL;
                call = new CompoundTag();
                call.putString("Result", "voicemail");
                call.putString("Greeting", "This is Harrigan. I don't pick up this phone. If you have a name for me, leave it after the tone.");
                callAge = REVEAL + 200;
                typed = 999;
                picked = beeped = true;
            }
            default -> view = View.HOME;
        }
        rebuild();
    }
    boolean showsInput() { return input != null; }

    // ---------------------------------------------------------------------------------------------------------------- drawing

    @Override public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float partial) {
        super.renderBackground(g, mouseX, mouseY, partial);
        g.fill(px - 5, py - 9, px + pw + 5, py + ph + 13, 0xFF0A0B0D);
        g.fill(px - 4, py - 8, px + pw + 4, py + ph + 12, 0xFF24272C);
        g.fill(px, py, px + pw, py + ph, 0xFF10151C);
        g.fill(px + pw / 2 - 12, py - 5, px + pw / 2 + 12, py - 3, 0xFF3B4047);
        g.fill(px + pw / 2 - 6, py + ph + 4, px + pw / 2 + 6, py + ph + 6, 0xFF3B4047);
    }

    @Override public void render(GuiGraphics g, int mouseX, int mouseY, float partial) {
        super.render(g, mouseX, mouseY, partial);
        statusBar(g);
        switch (view) {
            case HOME -> home(g);
            case THREAD -> conversation(g);
            case CALL -> calling(g);
        }
    }

    /** The time back home on the left; the signal, or the lack of it, on the right. */
    private void statusBar(GuiGraphics g) {
        String time = HarriganPhoneItem.homeTime(state.getLong("DayTime") + age);
        g.drawString(font, time, px + 6, py + 4, 0xFFE8E8E8, false);
        if (service()) {
            for (int bar = 0; bar < 4; bar++) {
                int x = px + pw - 22 + bar * 4, h = 3 + bar * 2;
                g.fill(x, py + 11 - h, x + 3, py + 11, 0xFFE8E8E8);
            }
        } else {
            String none = "No service";
            g.drawString(font, none, px + pw - 6 - font.width(none), py + 4, 0xFF9AA0A8, false);
        }
    }

    private void home(GuiGraphics g) {
        String time = HarriganPhoneItem.homeTime(state.getLong("DayTime") + age);
        g.pose().pushPose();
        g.pose().translate(px + pw / 2F, py + 34, 0);
        g.pose().scale(2.4F, 2.4F, 1);
        g.drawString(font, time, -font.width(time) / 2, 0, 0xFFF2F2F2, false);
        g.pose().popPose();
        centered(g, state.getBoolean("WaitingRoom")?"One unread message":"Time back home", py + 60, 0xFF8C939C);
        if(state.getBoolean("WaitingRoom")){
            int line=py+ph-106;g.fill(px+7,line,px+pw-7,line+62,0xFF1C2430);
            g.drawString(font,"Message",px+13,line+8,0xFFCFD8DD,false);
            g.drawWordWrap(font,Component.literal("Running late, sit tight"),px+13,line+25,pw-26,0xFFF2F2F2);
            return;
        }
        // The one contact.
        int top = py + ph - 96;
        g.fill(px + 6, top, px + pw - 6, top + 38, 0xFF1C2430);
        g.fill(px + 12, top + 7, px + 36, top + 31, 0xFF56606C);
        g.drawString(font, "H", px + 21, top + 15, 0xFFF2F2F2, false);
        g.drawString(font, "Harrigan", px + 42, top + 9, 0xFFF2F2F2, false);
        g.drawString(font, preview(), px + 42, top + 22, 0xFF8C939C, false);
    }

    /** The last thing in the thread, cut to fit the contact card. */
    private String preview() {
        ListTag lines = thread();
        String text = lines.isEmpty() ? "mobile" : lines.getCompound(lines.size() - 1).getString("Text");
        int room = pw - 54;
        if (font.width(text) <= room) return text;
        while (!text.isEmpty() && font.width(text + "...") > room) text = text.substring(0, text.length() - 1);
        return text + "...";
    }

    /** The thread with him, newest at the bottom: the reader's lines on the right, his on the left, the phone's small print between. */
    private void conversation(GuiGraphics g) {
        centered(g, "Harrigan", py + 19, 0xFFF2F2F2);
        g.fill(px + 4, py + 33, px + pw - 4, py + 34, 0xFF2A313B);
        int top = py + 38, bottom = py + ph - 36, wide = pw - 40;
        ListTag lines = thread();
        if (lines.isEmpty()) {
            centered(g, "Text him a name.", (top + bottom) / 2, 0xFF8C939C);
            return;
        }
        int y = bottom;
        g.enableScissor(px + 2, top, px + pw - 2, bottom);
        for (int i = lines.size() - 1; i >= 0 && y > top; i--) {
            CompoundTag line = lines.getCompound(i);
            int who = line.getInt("Who");
            String text = line.getString("Text");
            if (who != ME && who != HIM) {
                y -= 11;
                centered(g, text, y, 0xFF7E858E);
                continue;
            }
            List<FormattedCharSequence> rows = new ArrayList<>(font.split(Component.literal(text), wide));
            int width = 0;
            for (var row : rows) width = Math.max(width, font.width(row));
            int height = rows.size() * 10 + 6;
            y -= height + 3;
            int left = who == ME ? px + pw - 8 - width - 8 : px + 8;
            g.fill(left, y, left + width + 8, y + height, who == ME ? 0xFF2F66C9 : 0xFF353B44);
            for (int r = 0; r < rows.size(); r++) g.drawString(font, rows.get(r), left + 4, y + 4 + r * 10, 0xFFF2F2F2, false);
        }
        g.disableScissor();
    }

    private void calling(GuiGraphics g) {
        g.fill(px + pw / 2 - 18, py + 34, px + pw / 2 + 18, py + 70, 0xFF56606C);
        centered(g, "H", py + 48, 0xFFF2F2F2);
        centered(g, "Harrigan", py + 80, 0xFFF2F2F2);
        String status;
        if (!revealed()) status = "Calling" + ".".repeat(1 + callAge / 10 % 3);
        else switch (result()) {
            case "noservice" -> status = "No service";
            case "disconnected" -> status = "Not in service";
            case "pocket" -> status = "Ringing";
            case "unanswered" -> status = callAge < UNANSWERED ? "Calling" + ".".repeat(1 + callAge / 10 % 3) : "No answer";
            default -> status = left ? "Message left" : beeped ? "Recording" : "Voicemail";
        }
        centered(g, status, py + 92, 0xFF8C939C);
        if (!revealed()) return;
        String words = switch (result()) {
            case "disconnected" -> "The number you have dialed is not in service.";
            case "pocket" -> "His phone is ringing in your pocket.";
            case "voicemail" -> {
                String greeting = call.getString("Greeting");
                yield left ? "" : greeting.substring(0, Math.min(greeting.length(), typed));
            }
            default -> "";
        };
        int y = py + 110;
        for (var row : font.split(Component.literal(words), pw - 20)) {
            g.drawString(font, row, px + pw / 2 - font.width(row) / 2, y, 0xFFC8CDD3, false);
            y += 10;
        }
        if (voicemailOpen()) centered(g, "Leave a name after the tone.", py + ph - 70, 0xFF8C939C);
    }

    private void centered(GuiGraphics g, String text, int y, int color) {
        g.drawString(font, text, px + pw / 2 - font.width(text) / 2, y, color, false);
    }
}
