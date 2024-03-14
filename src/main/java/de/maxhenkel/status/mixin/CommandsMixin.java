package de.maxhenkel.status.mixin;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import de.maxhenkel.status.Status;
import de.maxhenkel.status.playerstate.Availability;
import de.maxhenkel.status.playerstate.PlayerState;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

import static net.minecraft.commands.Commands.literal;

@Mixin(Commands.class)
public abstract class CommandsMixin {
    @Shadow @Final private CommandDispatcher<CommandSourceStack> dispatcher;
    
    @Inject(method = "<init>",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/brigadier/CommandDispatcher;setConsumer(Lcom/mojang/brigadier/ResultConsumer;)V",
                    remap = false
            ))
    void addCommands(Commands.CommandSelection commandSelection, CommandBuildContext commandBuildContext, CallbackInfo ci) {
        dispatcher.register(literal("status")
                .then(literal("status")
                        .then(literal("streaming").executes(context -> {
                            if (!context.getSource().isPlayer()) return 0;
                            setStatus(context.getSource().getPlayer(), "streaming");
                            context.getSource().sendSuccess(() -> Component.literal("Status is now streaming"), true);
                            return 1;
                        }))
                        .then(literal("recording").executes(context -> {
                            if (!context.getSource().isPlayer()) return 0;
                            setStatus(context.getSource().getPlayer(), "recording");
                            context.getSource().sendSuccess(() -> Component.literal("Status is now recording"), true);
                            return 1;
                        }))
                        .then(literal("none").executes(context -> {
                            if (!context.getSource().isPlayer()) return 0;
                            setStatus(context.getSource().getPlayer(), "");
                            context.getSource().sendSuccess(() -> Component.literal("Status is now none"), true);
                            return 1;
                        }))
                )
                .then(literal("availabity")
                        .then(literal("none").executes(context -> {
                            if (!context.getSource().isPlayer()) return 0;
                            setAvailibility(context.getSource().getPlayer(), Availability.NONE);
                            context.getSource().sendSuccess(() -> Component.literal("Availability is now none"), true);
                            return 1;
                        }))
                        .then(literal("open").executes(context -> {
                            if (!context.getSource().isPlayer()) return 0;
                            setAvailibility(context.getSource().getPlayer(), Availability.OPEN);
                            context.getSource().sendSuccess(() -> Component.literal("Availability is now open"), true);
                            return 1;
                        }))
                        .then(literal("dnd").executes(context -> {
                            if (!context.getSource().isPlayer()) return 0;
                            setAvailibility(context.getSource().getPlayer(), Availability.DO_NOT_DISTURB);
                            context.getSource().sendSuccess(() -> Component.literal("Availability is now dnd"), true);
                            return 1;
                        }))
                ));
    }

    @Unique
    void setStatus(ServerPlayer player, String status) {
        PlayerState state = Status.STATE_MANAGER.getState(player.getUUID());
        if (state == null) state = new PlayerState(player.getUUID());
        state.setState(status);
        Status.STATE_MANAGER.setState(player.getUUID(), state);
        player.server.getPlayerList().broadcastAll(new ClientboundPlayerInfoUpdatePacket(ClientboundPlayerInfoUpdatePacket.Action.UPDATE_DISPLAY_NAME, player));

        Status.STATE_MANAGER.broadcastState(player.server, state);
    }

    @Unique
    void setAvailibility(ServerPlayer player, Availability availability) {
        PlayerState state = Status.STATE_MANAGER.getState(player.getUUID());
        if (state == null) state = new PlayerState(player.getUUID());
        state.setAvailability(availability);
        Status.STATE_MANAGER.setState(player.getUUID(), state);
        player.server.getPlayerList().broadcastAll(new ClientboundPlayerInfoUpdatePacket(ClientboundPlayerInfoUpdatePacket.Action.UPDATE_DISPLAY_NAME, player));

        Status.STATE_MANAGER.broadcastState(player.server, state);
    }
}
