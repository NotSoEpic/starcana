package dindcrzy.starcana.mixin;

import dindcrzy.starcana.IPlayerData;
import dindcrzy.starcana.networking.ConKnowledgePacket;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerManager.class)
public class PlayerManagerMixin {
    @Inject(method = "onPlayerConnect", at = @At("TAIL"))
    private void sendLearntConstellations(ClientConnection connection, ServerPlayerEntity player, CallbackInfo ci) {
        for(Identifier id : ((IPlayerData)player).starcana$getFoundConstellations()) {
            ConKnowledgePacket.add(player, id);
        }
    }
}
