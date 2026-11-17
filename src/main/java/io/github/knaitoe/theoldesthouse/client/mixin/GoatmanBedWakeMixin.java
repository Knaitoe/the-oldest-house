package io.github.knaitoe.theoldesthouse.client.mixin;

import io.github.knaitoe.theoldesthouse.client.GoatmanClient;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.InBedChatScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Only reader input is held; the server's genuine ending still wakes the player. */
@Mixin(InBedChatScreen.class)
public abstract class GoatmanBedWakeMixin {
    @Shadow private Button leaveBedButton;

    @Inject(method="render",at=@At("HEAD"),cancellable=true)
    private void ghosthouse$bedButton(GuiGraphics graphics,int mouseX,int mouseY,float partialTick,CallbackInfo ci){
        // 0.4.163: once it stands laughing, the bed's chat box is not drawn either; the screen is clear.
        // 0.4.664: while the machine in Not Yet holds a reader on its table or their cot, nothing is drawn over the dark and nobody gets up.
        if(GoatmanClient.clearBedScreen()||io.github.knaitoe.theoldesthouse.client.NotYetClient.held()){if(leaveBedButton!=null){leaveBedButton.visible=false;leaveBedButton.active=false;}ci.cancel();return;}
        if(leaveBedButton!=null){boolean available=!GoatmanClient.watchingFromBed();leaveBedButton.visible=available;leaveBedButton.active=available;}
    }

    @Inject(method="sendWakeUp",at=@At("HEAD"),cancellable=true)
    private void ghosthouse$holdAdmittedBed(CallbackInfo ci){
        if(GoatmanClient.watchingFromBed()||io.github.knaitoe.theoldesthouse.client.NotYetClient.held())ci.cancel();
    }
}
