package de.maxhenkel.status.mixin;

import com.mojang.authlib.GameProfile;
import de.maxhenkel.status.Status;
import de.maxhenkel.status.playerstate.Availability;
import de.maxhenkel.status.playerstate.PlayerState;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.scores.PlayerTeam;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin extends Player {

    public ServerPlayerMixin(Level level, BlockPos blockPos, float f, GameProfile gameProfile) {
        super(level, blockPos, f, gameProfile);
    }

    @Inject(method= "getTabListDisplayName", at = @At("RETURN"), cancellable = true)
    private void rewriteTabDisplay(CallbackInfoReturnable<Component> cir) {
        PlayerState state = Status.STATE_MANAGER.getState(this.getUUID());

        if (state == null) return;

        if (!state.getState().isEmpty() || !state.getAvailability().equals(Availability.NONE)) {
            var profileName = Component.literal(this.getGameProfile().getName());
            var displayName = PlayerTeam.formatNameForTeam(this.getTeam(), profileName);
            displayName = switch (state.getState()) {
                case "streaming" ->
                        displayName.append(Component.literal(" ●").setStyle(Style.EMPTY.withColor(ChatFormatting.DARK_PURPLE).withBold(true)));
                case "recording" ->
                        displayName.append(Component.literal(" ●").setStyle(Style.EMPTY.withColor(ChatFormatting.RED).withBold(true)));
                default -> displayName;
            };

            displayName = switch (state.getAvailability()) {
                case OPEN -> displayName.append(Component.literal(" ■").setStyle(Style.EMPTY.withColor(ChatFormatting.GREEN).withBold(true)));
                case DO_NOT_DISTURB -> displayName.append(Component.literal(" ■").setStyle(Style.EMPTY.withColor(ChatFormatting.RED).withBold(true)));
                default -> displayName;
            };

            cir.setReturnValue(displayName);
        }
    }
}
