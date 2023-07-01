package cn.disy920.gamemode.mixin;

import cn.disy920.gamemode.access.PlayerAccess;
import cn.disy920.gamemode.utils.PlayerPosition;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static cn.disy920.gamemode.Main.config;
import static cn.disy920.gamemode.Main.server;

@Environment(EnvType.SERVER)
@Mixin(ServerPlayNetworkHandler.class)
public class MixinChat {
    @Shadow
    public ServerPlayerEntity player;

    @Inject(at = @At("HEAD"), method = "onChatMessage")
    private void onChatMessage(ChatMessageC2SPacket packet, CallbackInfo ci) {
        String chatMessage = packet.chatMessage();
        if (config.getBoolean("enable-MCDR-like-cmd")) {
            if (chatMessage.equals("!!spec")) {
                if (!config.getBoolean("only-op") || (config.getBoolean("only-op") && player.hasPermissionLevel(4))) {
                    if (!config.getStringList("blacklist").contains(player.getName().getString())) {
                        if (!((PlayerAccess) player).isSpectating()) {
                            server.getPlayerManager().broadcast(Text.literal(player.getName().getString() + "准备观察你们啦！").setStyle(Style.EMPTY.withColor(Formatting.GOLD)), false);
                            player.changeGameMode(GameMode.SPECTATOR);
                            ((PlayerAccess) player).setSpectating(true);

                            Vec3d pos = player.getPos();
                            ((PlayerAccess) player).setPreviousPosition(player.getWorld(), pos.x, pos.y, pos.z, player.getYaw(), player.getPitch());
                        } else {
                            server.getPlayerManager().broadcast(Text.literal(player.getName().getString() + "结束了观察模式").setStyle(Style.EMPTY.withColor(Formatting.GREEN)), false);

                            PlayerPosition prevPosition = ((PlayerAccess) player).getPreviousPosition();
                            player.teleport(prevPosition.takeServerWorld(), prevPosition.getX(), prevPosition.getY(), prevPosition.getZ(), prevPosition.getYaw(), prevPosition.getPitch());

                            player.changeGameMode(GameMode.DEFAULT);
                            ((PlayerAccess) player).setSpectating(false);
                        }
                    }
                }
            }
        }
    }
}
