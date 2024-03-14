package de.maxhenkel.status.mixin;

import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.world.scores.PlayerTeam;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerScoreboard.class)
public class ServerScoreboardMixin {
    @Shadow @Final private MinecraftServer server;

    @Inject(method = "addPlayerToTeam", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/players/PlayerList;broadcastAll(Lnet/minecraft/network/protocol/Packet;)V"))
    private void addOtherCall1(String string, PlayerTeam playerTeam, CallbackInfoReturnable<Boolean> cir) {
        if (server.getPlayerList().getPlayerByName(string) != null) {
            server.getPlayerList().broadcastAll(new ClientboundPlayerInfoUpdatePacket(ClientboundPlayerInfoUpdatePacket.Action.UPDATE_DISPLAY_NAME, server.getPlayerList().getPlayerByName(string)));
        }
    }

    @Inject(method = "removePlayerFromTeam", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/players/PlayerList;broadcastAll(Lnet/minecraft/network/protocol/Packet;)V"))
    private void addOtherCall2(String string, PlayerTeam playerTeam, CallbackInfo ci) {
        if (server.getPlayerList().getPlayerByName(string) != null) {
            server.getPlayerList().broadcastAll(new ClientboundPlayerInfoUpdatePacket(ClientboundPlayerInfoUpdatePacket.Action.UPDATE_DISPLAY_NAME, server.getPlayerList().getPlayerByName(string)));
        }
    }
}
