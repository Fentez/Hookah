package ua.fentez.hookah;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Display;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class HookahManager {
    private final Map<UUID, Double> puffLevels = new HashMap<>();
    private final Map<UUID, BukkitTask> exhaleTasks = new HashMap<>();
    private final Map<UUID, Long> lastBreathActivity = new HashMap<>();
    private final Set<TextDisplay> activeDisplays = ConcurrentHashMap.newKeySet();
    private final Plugin plugin;
    private final HookahConfig config;
    private final Random random = new Random();

    public HookahManager(Plugin plugin, HookahConfig config) {
        this.plugin = plugin;
        this.config = config;
    }

    public void cleanupPlayer(UUID uuid) {
        puffLevels.remove(uuid);
        lastBreathActivity.remove(uuid);
        if (exhaleTasks.containsKey(uuid)) {
            exhaleTasks.remove(uuid).cancel();
        }
    }

    public void cleanupAll() {
        for (TextDisplay display : activeDisplays) {
            if (display != null && !display.isDead()) {
                display.remove();
            }
        }
        activeDisplays.clear();

        for (BukkitTask task : exhaleTasks.values()) {
            task.cancel();
        }
        exhaleTasks.clear();
        puffLevels.clear();
        lastBreathActivity.clear();
    }

    public void addPuff(Player player) {
        UUID uuid = player.getUniqueId();
        double current = puffLevels.getOrDefault(uuid, 0.0);
        current += config.puffAmountPerClick;

        if (config.poisonEnabled && current >= config.poisonOverpuffThreshold) {
            for (PotionEffectType type : config.poisonEffects) {
                player.addPotionEffect(new PotionEffect(type, config.poisonDurationTicks, 0));
            }
            current = config.poisonOverpuffDropTo;
        }

        puffLevels.put(uuid, current);
        lastBreathActivity.put(uuid, System.currentTimeMillis());
        showActionBar(player, current);

        if (exhaleTasks.containsKey(uuid)) {
            exhaleTasks.get(uuid).cancel();
        }

        BukkitTask task = new BukkitRunnable() {
            int delayTicks = 20;
            int soundCounter = 0;

            @Override
            public void run() {
                if (!player.isOnline()) {
                    cleanupPlayer(uuid);
                    this.cancel();
                    return;
                }

                double level = puffLevels.getOrDefault(uuid, 0.0);

                if (player.isSneaking()) {
                    long idleTime = System.currentTimeMillis() - lastBreathActivity.getOrDefault(uuid, System.currentTimeMillis());
                    if (config.poisonEnabled && idleTime > (config.poisonHoldBreathTicks * 50) && level > 0) {
                        for (PotionEffectType type : config.poisonEffects) {
                            player.addPotionEffect(new PotionEffect(type, config.poisonDurationTicks, 0));
                        }
                        puffLevels.remove(uuid);
                        showActionBar(player, 0);
                        exhaleTasks.remove(uuid);
                        this.cancel();
                    } else {
                        showActionBar(player, level);
                    }
                    return;
                }

                if (delayTicks > 0) {
                    delayTicks--;
                    showActionBar(player, level);
                    return;
                }

                if (level <= 0) {
                    puffLevels.remove(uuid);
                    showActionBar(player, 0);
                    exhaleTasks.remove(uuid);
                    this.cancel();
                    return;
                }

                Vector dir = player.getLocation().getDirection();
                Location eye = player.getEyeLocation().subtract(0, 0.1, 0);

                player.getWorld().spawnParticle(
                        Particle.CAMPFIRE_COSY_SMOKE,
                        eye.add(dir.clone().multiply(0.5)),
                        0,
                        dir.getX() * config.smokeDistanceMultiplier,
                        dir.getY() * config.smokeDistanceMultiplier,
                        dir.getZ() * config.smokeDistanceMultiplier,
                        config.smokeSpeed
                );

                if (soundCounter % 4 == 0) {
                    player.playSound(player.getLocation(), Sound.ENTITY_PHANTOM_AMBIENT, 0.05f, 0.5f);
                }
                soundCounter++;

                level -= 0.1;
                if (level < 0) level = 0;

                puffLevels.put(uuid, level);
                showActionBar(player, level);
            }
        }.runTaskTimer(plugin, 0L, 1L);

        exhaleTasks.put(uuid, task);
    }

    public void spawnSmokeRing(Player player) {
        if (!config.ringsEnabled) return;
        UUID uuid = player.getUniqueId();
        double current = puffLevels.getOrDefault(uuid, 0.0);
        if (current <= 0.0) return;

        current -= config.ringsCost;
        if (current < 0) current = 0;
        puffLevels.put(uuid, current);
        lastBreathActivity.put(uuid, System.currentTimeMillis());
        showActionBar(player, current);

        Location eye = player.getEyeLocation().subtract(0, 0.25, 0);
        Vector dir = player.getLocation().getDirection();
        Location spawnLoc = eye.clone().add(dir.clone().multiply(0.5));

        final float randomScale = (float) (config.ringsTextScaleMin + random.nextDouble() * (config.ringsTextScaleMax - config.ringsTextScaleMin));

        TextDisplay displayFront = player.getWorld().spawn(spawnLoc, TextDisplay.class, ent -> {
            ent.setBillboard(Display.Billboard.FIXED);
            ent.setDefaultBackground(false);
            ent.setBackgroundColor(Color.fromARGB(0, 0, 0, 0));
            ent.setAlignment(TextDisplay.TextAlignment.CENTER);
            ent.setRotation(eye.getYaw() + 180f, -eye.getPitch());
            ent.setShadowed(true);
            ent.setPersistent(false);

            Transformation transform = ent.getTransformation();
            transform.getTranslation().set(0f, 0.1f, 0f);
            transform.getScale().set(randomScale, randomScale, randomScale);
            ent.setTransformation(transform);

            String firstSymbol = config.ringsSymbols.isEmpty() ? "◦" : config.ringsSymbols.getFirst();
            NamedTextColor color = NamedTextColor.NAMES.value(config.ringsTextColor.toLowerCase());
            if (color == null) color = NamedTextColor.WHITE;
            ent.text(Component.text(firstSymbol).color(color));
        });

        TextDisplay displayBack = player.getWorld().spawn(spawnLoc, TextDisplay.class, ent -> {
            ent.setBillboard(Display.Billboard.FIXED);
            ent.setDefaultBackground(false);
            ent.setBackgroundColor(Color.fromARGB(0, 0, 0, 0));
            ent.setAlignment(TextDisplay.TextAlignment.CENTER);
            ent.setRotation(eye.getYaw(), eye.getPitch());
            ent.setShadowed(true);
            ent.setPersistent(false);

            Transformation transform = ent.getTransformation();
            transform.getTranslation().set(0f, 0.1f, 0f);
            transform.getScale().set(randomScale, randomScale, randomScale);
            ent.setTransformation(transform);

            String firstSymbol = config.ringsSymbols.isEmpty() ? "◦" : config.ringsSymbols.getFirst();
            NamedTextColor color = NamedTextColor.NAMES.value(config.ringsTextColor.toLowerCase());
            if (color == null) color = NamedTextColor.WHITE;
            ent.text(Component.text(firstSymbol).color(color));
        });

        activeDisplays.add(displayFront);
        activeDisplays.add(displayBack);

        player.playSound(player.getLocation(), Sound.ENTITY_PHANTOM_AMBIENT, 0.1f, 0.5f);

        final float spinSpeed = (random.nextFloat() - 0.5f) * (float) config.ringsSpinMaxSpeed;
        final float spinSpeedRad = (float) Math.toRadians(spinSpeed);

        new BukkitRunnable() {
            int ticks = 0;
            float currentAngle = 0;

            @Override
            public void run() {
                if (ticks > 60 || displayFront.isDead()) {
                    displayFront.remove();
                    displayBack.remove();
                    activeDisplays.remove(displayFront);
                    activeDisplays.remove(displayBack);
                    this.cancel();
                    return;
                }

                Location loc = displayFront.getLocation();
                loc.add(dir.clone().multiply(config.ringsSpeed));

                if (loc.getBlock().getType().isSolid()) {
                    loc.getWorld().spawnParticle(Particle.CAMPFIRE_COSY_SMOKE, loc, config.ringsHitParticles, 0.1, 0.1, 0.1, 0.02);
                    displayFront.remove();
                    displayBack.remove();
                    activeDisplays.remove(displayFront);
                    activeDisplays.remove(displayBack);
                    this.cancel();
                    return;
                }

                displayFront.teleport(loc);

                Location locBack = loc.clone();
                locBack.setYaw(eye.getYaw());
                locBack.setPitch(eye.getPitch());
                displayBack.teleport(locBack);

                currentAngle += spinSpeedRad;
                float r = 0.18f;
                float dx = r * (float) Math.sin(currentAngle);
                float dy = r - r * (float) Math.cos(currentAngle);

                Transformation transformFront = displayFront.getTransformation();
                transformFront.getLeftRotation().identity().rotateZ(currentAngle);
                transformFront.getTranslation().set(dx, 0.1f + dy, 0f);
                displayFront.setTransformation(transformFront);
                displayFront.setInterpolationDelay(0);
                displayFront.setInterpolationDuration(1);

                Transformation transformBack = displayBack.getTransformation();
                transformBack.getLeftRotation().identity().rotateZ(-currentAngle);
                transformBack.getTranslation().set(-dx, 0.1f + dy, 0f);
                displayBack.setTransformation(transformBack);
                displayBack.setInterpolationDelay(0);
                displayBack.setInterpolationDuration(1);

                if (!config.ringsSymbols.isEmpty()) {
                    int numSymbols = config.ringsSymbols.size();
                    int symbolIndex = Math.min((ticks / 10), numSymbols - 1);
                    NamedTextColor color = NamedTextColor.NAMES.value(config.ringsTextColor.toLowerCase());
                    if (color == null) color = NamedTextColor.WHITE;

                    Component textComp = Component.text(config.ringsSymbols.get(symbolIndex)).color(color);
                    displayFront.text(textComp);
                    displayBack.text(textComp);
                }

                if (ticks > 40) {
                    int alpha = 255 - (int) (((ticks - 40) / 20.0) * 255);
                    if (alpha < 0) alpha = 0;
                    displayFront.setTextOpacity((byte) alpha);
                    displayBack.setTextOpacity((byte) alpha);
                }

                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    private void showActionBar(Player player, double level) {
        int max = (int) Math.round(config.maxPuffLevel);
        int bars = (int) Math.round(level);
        if (bars < 0) bars = 0;
        if (bars > max) bars = max;

        StringBuilder barStr = new StringBuilder();
        for (int i = 0; i < max; i++) {
            if (i < bars) {
                barStr.append(config.actionbarFilledSymbol);
            } else {
                barStr.append(config.actionbarEmptySymbol);
            }
        }

        String text = config.actionbarFormat
                .replace("{bars}", barStr.toString())
                .replace("{current}", String.valueOf(Math.round(level * 10.0) / 10.0))
                .replace("{max}", String.valueOf(Math.round(config.maxPuffLevel * 10.0) / 10.0));

        Component message = LegacyComponentSerializer.legacyAmpersand().deserialize(text);

        player.sendActionBar(message);
    }
}
