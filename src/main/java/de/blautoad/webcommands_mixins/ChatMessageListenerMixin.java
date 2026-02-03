package de.blautoad.webcommands_mixins;

import de.blautoad.webcommands.Result;
import de.blautoad.webcommands.ResultManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(net.minecraft.client.multiplayer.ClientPacketListener.class)
public class ChatMessageListenerMixin {
    @Inject(method = "handleSystemChat", at = @At("HEAD"), cancellable = true)
    private void onGameMessage(net.minecraft.network.protocol.game.ClientboundSystemChatPacket packet, CallbackInfo ci) {
        Result r = new Result(packet.content().toString(),packet.content().getString());

        if(ResultManager.getSearched_filters().stream().anyMatch(p->p.test(r))){
            ResultManager.addResult(r);
            ci.cancel();
        }

    }
}