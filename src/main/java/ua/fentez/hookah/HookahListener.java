package ua.fentez.hookah;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.BrewingStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.BrewerInventory;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionType;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class HookahListener implements Listener {
    private final HookahManager manager;
    private final HookahConfig config;
    private final Random random = new Random();

    public HookahListener(HookahManager manager, HookahConfig config) {
        this.manager = manager;
        this.config = config;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        manager.cleanupPlayer(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getHand() != null && event.getHand() != EquipmentSlot.HAND) return;

        Player player = event.getPlayer();
        Action action = event.getAction();

        if (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK) {
            if (player.isSneaking() && config.ringsEnabled) {
                manager.spawnSmokeRing(player);
            }
            return;
        }

        if (action != Action.RIGHT_CLICK_BLOCK) return;
        if (!player.isSneaking()) return;

        Block block = event.getClickedBlock();
        if (block == null || block.getType() != Material.BREWING_STAND) return;

        BlockState state = block.getState();
        if (state instanceof BrewingStand stand) {
            Component nameComponent = stand.customName();
            if (nameComponent == null) return;

            String customName = PlainTextComponentSerializer.plainText().serialize(nameComponent).toLowerCase();

            boolean isHookah = false;
            for (String keyword : config.itemKeywords) {
                if (customName.contains(keyword.toLowerCase())) {
                    isHookah = true;
                    break;
                }
            }
            if (!isHookah) return;

            event.setCancelled(true);
            manager.addPuff(player);

            BrewerInventory inv = stand.getInventory();
            List<PotionType> possibleEffects = new ArrayList<>();

            for (ItemStack item : inv.getContents()) {
                if (item != null && item.hasItemMeta() && item.getItemMeta() instanceof PotionMeta meta) {
                    PotionType type = meta.getBasePotionType();
                    if (type != null && type != PotionType.WATER && type != PotionType.AWKWARD && type != PotionType.THICK && type != PotionType.MUNDANE) {
                        if (!config.blockedPotions.contains(type.name().toUpperCase())) {
                            possibleEffects.add(type);
                        }
                    }
                }
            }

            if (!possibleEffects.isEmpty()) {
                PotionType chosen = possibleEffects.get(random.nextInt(possibleEffects.size()));
                List<PotionEffect> effects = chosen.getPotionEffects();
                for (PotionEffect e : effects) {
                    player.addPotionEffect(new PotionEffect(e.getType(), config.effectsDurationTicks, e.getAmplifier()));
                }
            }

            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_WORK_CLERIC, 0.3f, 0.1f);
            player.swingMainHand();
        }
    }
}
