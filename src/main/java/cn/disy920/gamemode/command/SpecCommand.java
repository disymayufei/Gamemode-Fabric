package cn.disy920.gamemode.command;

import cn.disy920.gamemode.access.PlayerAccess;
import cn.disy920.gamemode.config.JsonConfig;
import cn.disy920.gamemode.utils.PlayerPosition;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;

import java.io.File;

import static cn.disy920.gamemode.Main.globalConfig;
import static cn.disy920.gamemode.Main.server;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class SpecCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
                literal("spec")
                        .requires(source -> {
                            if (globalConfig.getBoolean("only-op")) {
                                return source.hasPermissionLevel(4);
                            }
                            else {
                                return true;
                            }
                        })
                        .executes(context -> {
                            var source = context.getSource();
                            if (globalConfig.getBoolean("enable")) {
                                if (source.getEntity() instanceof ServerPlayerEntity player) {
                                    if (!globalConfig.getStringList("blacklist").contains(player.getName().getString())) {
                                        if (!((PlayerAccess) player).isSpectating()) {
                                            server.getPlayerManager().broadcast(Text.literal(player.getName().getString() + "准备悄悄观察你们啦！").setStyle(Style.EMPTY.withColor(Formatting.GOLD)), false);
                                            player.changeGameMode(GameMode.SPECTATOR);
                                            ((PlayerAccess) player).setSpectating(true);

                                            Vec3d pos = player.getPos();
                                            ((PlayerAccess) player).setPreviousPosition(player.getWorld(), pos.x, pos.y, pos.z, player.getYaw(), player.getPitch());
                                        } else {
                                            server.getPlayerManager().broadcast(Text.literal(player.getName().getString() + "悄悄结束了观察模式").setStyle(Style.EMPTY.withColor(Formatting.GREEN)), false);

                                            PlayerPosition prevPosition = ((PlayerAccess) player).getPreviousPosition();
                                            player.teleport(prevPosition.takeServerWorld(), prevPosition.getX(), prevPosition.getY(), prevPosition.getZ(), prevPosition.getYaw(), prevPosition.getPitch());

                                            player.changeGameMode(GameMode.DEFAULT);
                                            ((PlayerAccess) player).setSpectating(false);
                                        }
                                    }
                                    else {
                                        source.sendMessage(Text.literal("您已被禁用该命令！").setStyle(Style.EMPTY.withColor(Formatting.RED)));
                                    }

                                    return Command.SINGLE_SUCCESS;
                                } else {
                                    return 0;
                                }
                            }
                            else {
                                source.sendMessage(Text.literal("本服务器尚未开启该指令").setStyle(Style.EMPTY.withColor(Formatting.RED)));
                                return Command.SINGLE_SUCCESS;
                            }
                        })
                        .then(
                                literal("player")
                                        .then(
                                             onlinePlayerArgument()
                                                     .executes(context -> {
                                                         String playerName = StringArgumentType.getString(context, "player");
                                                         var source = context.getSource();

                                                         if (globalConfig.getBoolean("enable")) {
                                                             if (source.getEntity() instanceof ServerPlayerEntity player) {
                                                                 if (!globalConfig.getStringList("blacklist").contains(player.getName().getString())) {
                                                                     if (player.getName().getString().equalsIgnoreCase(playerName)) {
                                                                         source.sendMessage(Text.literal("自己是不能观察自己的").setStyle(Style.EMPTY.withColor(Formatting.RED)));
                                                                     }
                                                                     else {
                                                                         for (String name : globalConfig.getStringList("deny-tp-to-player")) {
                                                                             if (playerName.equalsIgnoreCase(name)) {
                                                                                 source.sendMessage(Text.literal("该玩家不允许您在观察者模式下传送到TA身旁").setStyle(Style.EMPTY.withColor(Formatting.RED)));
                                                                                 return Command.SINGLE_SUCCESS;
                                                                             }
                                                                         }
                                                                         ServerPlayerEntity targetPlayer = server.getPlayerManager().getPlayer(playerName);
                                                                         if (targetPlayer != null) {
                                                                             if (!player.isSpectator()) {
                                                                                 player.changeGameMode(GameMode.SPECTATOR);
                                                                             }
                                                                             if (!((PlayerAccess) player).isSpectating()) {
                                                                                 server.getPlayerManager().broadcast(Text.literal(player.getName().getString() + "准备悄悄观察" + playerName + "啦！").setStyle(Style.EMPTY.withColor(Formatting.GOLD)), false);
                                                                                 ((PlayerAccess) player).setSpectating(true);

                                                                                 Vec3d pos = player.getPos();
                                                                                 ((PlayerAccess) player).setPreviousPosition(player.getWorld(), pos.x, pos.y, pos.z, player.getYaw(), player.getPitch());

                                                                                 player.teleport(targetPlayer.getServerWorld(), targetPlayer.getX(), targetPlayer.getY(), targetPlayer.getZ(), targetPlayer.getYaw(), targetPlayer.getPitch());
                                                                             } else {
                                                                                 player.teleport(targetPlayer.getServerWorld(), targetPlayer.getX(), targetPlayer.getY(), targetPlayer.getZ(), targetPlayer.getYaw(), targetPlayer.getPitch());
                                                                             }
                                                                         }
                                                                         else {
                                                                             source.sendMessage(Text.literal("该玩家不在线！").setStyle(Style.EMPTY.withColor(Formatting.RED)));
                                                                         }
                                                                     }
                                                                 }
                                                                 else {
                                                                     server.getPlayerManager().broadcast(Text.literal(player.getName().getString() + "准备观察" + playerName + "啦！").setStyle(Style.EMPTY.withColor(Formatting.GOLD)), false);
                                                                     source.sendMessage(Text.literal("您已被禁用该命令！").setStyle(Style.EMPTY.withColor(Formatting.RED)));
                                                                 }

                                                                 return Command.SINGLE_SUCCESS;
                                                             } else {
                                                                 return 0;
                                                             }
                                                         }
                                                         else {
                                                             source.sendMessage(Text.literal("本服务器尚未开启该指令").setStyle(Style.EMPTY.withColor(Formatting.RED)));
                                                             return Command.SINGLE_SUCCESS;
                                                         }
                                                     })
                                        )
                        )
                        .then(
                                literal("reload")
                                        .requires(source -> source.hasPermissionLevel(4))
                                        .executes(context -> {
                                            try {
                                                File configDir = new File(FabricLoader.getInstance().getConfigDir().toFile(), "Gamemode");
                                                File configFile = new File(configDir, "config.json");
                                                globalConfig = JsonConfig.loadConfig(configFile);
                                                context.getSource().sendMessage(Text.literal("[Gamemode-Fabric] 配置文件重载成功！").setStyle(Style.EMPTY.withColor(Formatting.GREEN)));

                                            }
                                            catch (Exception e) {
                                                context.getSource().sendMessage(Text.literal("[Gamemode-Fabric] 配置文件重载失败，请联系管理查看后台报错信息").setStyle(Style.EMPTY.withColor(Formatting.RED)));
                                                e.printStackTrace();
                                            }

                                            return Command.SINGLE_SUCCESS;
                                        })
                        )
        );
    }

    private static RequiredArgumentBuilder<ServerCommandSource, String> onlinePlayerArgument() {
        return argument("player", StringArgumentType.string())
                .suggests((source, builder) -> CommandSource.suggestMatching(server.getPlayerManager().getPlayerList().stream().map(PlayerEntity::getEntityName), builder));
    }
}
