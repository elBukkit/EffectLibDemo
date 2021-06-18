package com.elmakers.mine.bukkit.plugins.demo;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.google.common.collect.ImmutableSet;

import de.slikey.effectlib.EffectManager;
import de.slikey.effectlib.effect.LineEffect;

public class EffectLibDemo extends JavaPlugin implements TabExecutor {
    private static final ChatColor CHAT_PREFIX = ChatColor.AQUA;
    private static final ChatColor ERROR_PREFIX = ChatColor.RED;

    // We will play effects 3 blocks in front of the player so it's easier to see them
    private static final int VIEW_RANGE = 3;

    // We'll ignore these blocks when looking for a place to show effects, avoiding
    // putting them inside solid blocks
    private final Set<Material> transparent = ImmutableSet.of(Material.AIR, Material.CAVE_AIR, Material.TALL_GRASS);

    private EffectManager effectManager;
    public void onEnable() {
        // Boilerplate command registration
        PluginCommand command = getCommand("el");
        if (command != null) {
            command.setTabCompleter(this);
            command.setExecutor(this);
        } else {
            getLogger().severe("Something has gone very wrong, our command is missing!");
        }

        // Initialize our EffectLib Manager
        // We should only do this once and reuse the same manager for all effects.
        effectManager = new EffectManager(this);
    }

    public void onDisable() {
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // We only handle the el command
        if (!command.getName().equals("el")) {
            return false;
        }

        // Default is to list all available effect sub-commands
        if (args.length == 0) {
            return listEffects(sender);
        }

        // We need a Player to show effects in-game
        if (!(sender instanceof Player)) {
            sendError(sender, "This command may only be used in-game!");
            return true;
        }

        // Show individual effects
        Player player = (Player)sender;
        String effect = args[0];
        switch (effect) {
            case "line":
                return showLine(player);
            case "lineto":
                return showLineTo(player);
            // TODO: More
            default:
                sendError(sender, "Unknown effect type: " + effect);
                return true;
        }
    }

    private boolean listEffects(CommandSender sender) {
        sendMessage(sender, "Available effects: ");
        sendMessage(sender, "  line");
        sendMessage(sender, "  lineto");
        // TODO: More ...
        return true;
    }

    private boolean showLine(Player player) {
        LineEffect line = new LineEffect(effectManager);
        // Every effect needs a start location or entity
        line.setLocation(getStartLocation(player));
        // This makes the LineEffect go for 5 blocks, instead of requiring a target location
        line.length = 5;
        // This makes the effect last for 2 seconds (20 ticks = 1 second)
        line.iterations = 40;
        // Start the effect
        line.start();
        return true;
    }

    private boolean showLineTo(Player player) {
        LineEffect line = new LineEffect(effectManager);
        // Every effect needs a start location or entity
        line.setLocation(getStartLocation(player));
        // The LineEffect is unique in that it can take a second location,
        // To make a line from one location to another
        Location targetLocation = getTargetLocation(player, 20);
        line.setTargetLocation(targetLocation);
        // This makes the effect last for 2 seconds (20 ticks = 1 second)
        line.iterations = 40;
        // Start the effect
        line.start();
        return true;
    }

    private Location getStartLocation(Player player) {
        return getTargetLocation(player, VIEW_RANGE);
    }

    private Location getTargetLocation(Player player, int distance) {
        // Get a location a few blocks in front of the player, or at the closest
        // Solid block.
        Location location = player.getTargetBlock(transparent, distance).getLocation();
        // We want this location to be aiming in the same direction the player is looking,
        // important for directional effects
        location.setYaw(player.getLocation().getYaw());
        location.setPitch(player.getLocation().getPitch());
        return location;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        // Just tab-completing for the el command
        if (command.getName().equals("el")) {
            List<String> options = new ArrayList<>();
            options.add("line");
            options.add("lineto");
        }
        return null;
    }

    protected void sendMessage(CommandSender sender, String string) {
        sender.sendMessage(CHAT_PREFIX + string);
    }

    protected void sendError(CommandSender sender, String string) {
        sender.sendMessage(ERROR_PREFIX + string);
    }
}
