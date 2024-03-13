package de.maxhenkel.status.mixin;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundSetPlayerTeamPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerList.class)
public abstract class PlayerListMixin {
    @Shadow public abstract void broadcastAll(Packet<?> packet);

    @Shadow @Nullable public abstract ServerPlayer getPlayerByName(String string);

    @Inject(method = "broadcastAll(Lnet/minecraft/network/protocol/Packet;)V", at = @At("HEAD"))
    private void addOtherToBroadcast(Packet<?> packet, CallbackInfo ci) {
        if (packet instanceof ClientboundSetPlayerTeamPacket teamPacket && getPlayerByName(teamPacket.getName()) != null) {
            broadcastAll(new ClientboundPlayerInfoUpdatePacket(ClientboundPlayerInfoUpdatePacket.Action.UPDATE_DISPLAY_NAME, getPlayerByName(teamPacket.getName())));
        }
    }
}
