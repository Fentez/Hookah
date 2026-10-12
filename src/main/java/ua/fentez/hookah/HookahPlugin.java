package ua.fentez.hookah;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class HookahPlugin extends JavaPlugin implements CommandExecutor {

    private HookahConfig hookahConfig;
    private HookahManager manager;

    @Override
    public void onEnable() {
        hookahConfig = new HookahConfig(this);
        hookahConfig.load();

        manager = new HookahManager(this, hookahConfig);
        getServer().getPluginManager().registerEvents(new HookahListener(manager, hookahConfig), this);

        PluginCommand cmd = getCommand("hookah");
        if (cmd != null) {
            cmd.setExecutor(this);
        }

        getLogger().info("Hookah plugin enabled! Author: Fentez (https://fentez.pp.ua)");
    }

    @Override
    public void onDisable() {
        if (manager != null) {
            manager.cleanupAll();
        }
        getLogger().info("Hookah plugin disabled!");
    }

    @Override
    @SuppressWarnings("NullableProblems")
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            if (sender.hasPermission("hookah.reload") || sender.hasPermission("hookah.admin")) {
                hookahConfig.load();
                sender.sendMessage(hookahConfig.msgReloadSuccess);
            } else {
                sender.sendMessage(hookahConfig.msgNoPermission);
            }
        } else {
            sender.sendMessage(hookahConfig.msgUsage);
        }
        return true;
    }
}
