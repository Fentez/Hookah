package ua.fentez.hookah;

import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings("deprecation")
public class HookahConfig {

    private final HookahPlugin plugin;

    public double maxPuffLevel;
    public double puffAmountPerClick;
    public double smokeSpeed;
    public double smokeDistanceMultiplier;

    public boolean poisonEnabled;
    public double poisonOverpuffThreshold;
    public double poisonOverpuffDropTo;
    public long poisonHoldBreathTicks;
    public List<PotionEffectType> poisonEffects = new ArrayList<>();
    public int poisonDurationTicks;

    public boolean ringsEnabled;
    public double ringsCost;
    public double ringsSpeed;
    public double ringsSpinMaxSpeed;
    public double ringsTextScaleMin;
    public double ringsTextScaleMax;
    public String ringsTextColor;
    public List<String> ringsSymbols = new ArrayList<>();
    public int ringsHitParticles;

    public int effectsDurationTicks;

    public String actionbarFormat;
    public String actionbarFilledSymbol;
    public String actionbarEmptySymbol;

    public String msgReloadSuccess;
    public String msgNoPermission;
    public String msgUsage;

    public List<String> itemKeywords;
    public List<String> blockedPotions;

    public HookahConfig(HookahPlugin plugin) {
        this.plugin = plugin;
    }

    public void load() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        FileConfiguration config = plugin.getConfig();

        maxPuffLevel = config.getDouble("settings.max-puff-level", 8.0);
        puffAmountPerClick = config.getDouble("settings.puff-amount-per-click", 1.0);
        smokeSpeed = config.getDouble("settings.smoke-speed", 0.3);
        smokeDistanceMultiplier = config.getDouble("settings.smoke-distance-multiplier", 0.1);

        poisonEnabled = config.getBoolean("poison.enabled", true);
        poisonOverpuffThreshold = config.getDouble("poison.overpuff-threshold", 8.0);
        poisonOverpuffDropTo = config.getDouble("poison.overpuff-drop-to", 4.0);
        poisonHoldBreathTicks = config.getLong("poison.hold-breath-ticks", 100);
        poisonDurationTicks = config.getInt("poison.duration-ticks", 100);

        poisonEffects.clear();
        for (String effectName : config.getStringList("poison.effects")) {
            PotionEffectType type = Registry.POTION_EFFECT_TYPE.get(NamespacedKey.minecraft(effectName.toLowerCase()));
            if (type != null) {
                poisonEffects.add(type);
            } else {
                plugin.getLogger().warning("Unknown potion effect type in config: " + effectName);
            }
        }
        if (poisonEffects.isEmpty()) {
            poisonEffects.add(PotionEffectType.POISON);
            poisonEffects.add(PotionEffectType.NAUSEA);
        }

        ringsEnabled = config.getBoolean("rings.enabled", true);
        ringsCost = config.getDouble("rings.cost", 1.0);
        ringsSpeed = config.getDouble("rings.speed", 0.02);
        ringsSpinMaxSpeed = config.getDouble("rings.spin-max-speed", 15.0);
        ringsTextScaleMin = config.getDouble("rings.text-scale-min", 1.0);
        ringsTextScaleMax = config.getDouble("rings.text-scale-max", 2.0);
        ringsTextColor = config.getString("rings.text-color", "WHITE");
        ringsSymbols = config.getStringList("rings.symbols");
        if (ringsSymbols.isEmpty()) {
            ringsSymbols.add("◦");
            ringsSymbols.add("○");
            ringsSymbols.add("⭕");
        }
        ringsHitParticles = config.getInt("rings.hit-particles", 2);

        effectsDurationTicks = config.getInt("effects.duration-ticks", 40);

        actionbarFormat = config.getString("messages.actionbar.format", "&b💨 Puff: &7[{bars}] &f{current}/{max}");
        actionbarFilledSymbol = config.getString("messages.actionbar.filled-symbol", "■");
        actionbarEmptySymbol = config.getString("messages.actionbar.empty-symbol", "□");

        msgReloadSuccess = translateColors(config.getString("messages.commands.reload-success", "&a[Hookah] Configuration reloaded successfully!"));
        msgNoPermission = translateColors(config.getString("messages.commands.no-permission", "&cYou don't have permission to use this command."));
        msgUsage = translateColors(config.getString("messages.commands.usage", "&e[Hookah] Usage: /hookah reload"));

        itemKeywords = config.getStringList("item-keywords");
        if (itemKeywords.isEmpty()) {
            itemKeywords = Arrays.asList("hookah", "shisha", "кальян");
        }

        blockedPotions = new ArrayList<>();
        for (String potion : config.getStringList("effects.blocked-potions")) {
            blockedPotions.add(potion.toUpperCase());
        }
    }

    private String translateColors(String text) {
        if (text == null) return null;
        return ChatColor.translateAlternateColorCodes('&', text);
    }
}
