# Gamemode-Fabric
适用于Minecraft Fabric端的一个MCDR Gamemode迁移模组，可以让普通玩家拥有切换观察模式的途径。

# 使用方法：
安装Mod后，在服务器内输入/spec或!!spec（需要开启MCDR兼容模式），即可让对应玩家切换为观察模式，并在全服发送一个该玩家已进入观察模式的广播。再次输入/spec或!!spec即可切换回服务器设定的默认模式，并将该玩家传送回旁观开始时的位置。

# 指令
- **/spec**: 在观察者模式与服务器默认模式间切换
- **/spec reload**: 重载配置文件

# 配置文件
- enable: 是否允许玩家使用本Mod的指令切换到观察模式
- enable-MCDR-like-cmd: 是否允许玩家使用!!spec指令（兼容MCDR）
- only-op: 是否仅允许op使用本mod指令
- blacklist: 禁止使用本mod全部指令的玩家
- deny-tp-to-player: 拒绝被传送的玩家，其他玩家处于本mod开启的观察者模式下，无法快速tp到该玩家处

默认配置文件如下：
```json
{
  "enable": true,
  "enable-MCDR-like-cmd": true,
  "only-op": false,
  "blacklist": [],
  "deny-tp-to-player": ["Disy920"]
}
```
