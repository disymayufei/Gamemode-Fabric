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

import static cn.disy920.gamemode.Main.globalConfig;
import static cn.disy920.gamemode.Main.server;

@Environment(EnvType.SERVER)
@Mixin(ServerPlayNetworkHandler.class)
public class MixinChat {
    @Shadow
    public ServerPlayerEntity player;

    @Inject(at = @At("TAIL"), method = "onChatMessage")
    private void onChatMessage(ChatMessageC2SPacket packet, CallbackInfo ci) {
        String chatMessage = packet.chatMessage();
        if (globalConfig.getBoolean("enable") && globalConfig.getBoolean("enable-MCDR-like-cmd")) {
            if (chatMessage.equals("!!spec")) {
                if (!globalConfig.getBoolean("only-op") || (globalConfig.getBoolean("only-op") && player.hasPermissionLevel(4))) {
                    if (!globalConfig.getStringList("blacklist").contains(player.getName().getString())) {
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
                    else {
                        player.sendMessage(Text.literal("您已被禁用该命令！").setStyle(Style.EMPTY.withColor(Formatting.RED)));
                    }
                }
            }
            else if(chatMessage.startsWith("!!spec ")) {
                String playerName = chatMessage.substring(7);

                if (!globalConfig.getStringList("blacklist").contains(player.getName().getString())) {
                    if ("".equals(playerName)) {
                        player.sendMessage(Text.literal("请不要输入一个空的玩家名").setStyle(Style.EMPTY.withColor(Formatting.RED)));
                    }
                    else if (player.getName().getString().equalsIgnoreCase(playerName)) {
                        player.sendMessage(Text.literal("自己是不能观察自己的").setStyle(Style.EMPTY.withColor(Formatting.RED)));
                    }
                    else {
                        for (String name : globalConfig.getStringList("deny-tp-to-player")) {
                            if (playerName.equalsIgnoreCase(name)) {
                                player.sendMessage(Text.literal("该玩家不允许您在观察者模式下传送到TA身旁").setStyle(Style.EMPTY.withColor(Formatting.RED)));
                                return;
                            }
                        }
                        ServerPlayerEntity targetPlayer = server.getPlayerManager().getPlayer(playerName);
                        if (targetPlayer != null) {
                            if (!((PlayerAccess) player).isSpectating()) {
                                server.getPlayerManager().broadcast(Text.literal(player.getName().getString() + "准备观察" + playerName + "啦！").setStyle(Style.EMPTY.withColor(Formatting.GOLD)), false);
                                player.changeGameMode(GameMode.SPECTATOR);
                                ((PlayerAccess) player).setSpectating(true);

                                Vec3d pos = player.getPos();
                                ((PlayerAccess) player).setPreviousPosition(player.getWorld(), pos.x, pos.y, pos.z, player.getYaw(), player.getPitch());

                                player.teleport(targetPlayer.getWorld(), targetPlayer.getX(), targetPlayer.getY(), targetPlayer.getZ(), targetPlayer.getYaw(), targetPlayer.getPitch());
                            } else {
                                server.getPlayerManager().broadcast(Text.literal(player.getName().getString() + "准备观察" + playerName + "啦！").setStyle(Style.EMPTY.withColor(Formatting.GOLD)), false);
                                player.teleport(targetPlayer.getWorld(), targetPlayer.getX(), targetPlayer.getY(), targetPlayer.getZ(), targetPlayer.getYaw(), targetPlayer.getPitch());
                            }
                        }
                        else {
                            player.sendMessage(Text.literal("该玩家不在线！").setStyle(Style.EMPTY.withColor(Formatting.RED)));
                        }
                    }
                }
                else {
                    player.sendMessage(Text.literal("您已被禁用该命令！").setStyle(Style.EMPTY.withColor(Formatting.RED)));
                }
            }
        }
    }
}
