/*
 * Created 5/2/2020 by Violet M.
 *
 * Copyright (c) 2020 Violet M. <vi@violet.wtf>.
 * Copyright (c) 2020 [redacted] <https://[redacted]>
 * All Rights Reserved.
 */

package wtf.violet.xtalisman;

import wtf.violet.xtalisman.command.DebugCommand;
import wtf.violet.xtalisman.command.TalismanCommand;
import wtf.violet.xtalisman.listener.ExpListener;
import wtf.violet.xtalisman.listener.RightClickListener;
import wtf.violet.xtalisman.model.TalismanType;
import wtf.violet.xtalisman.model.reward.CommandReward;
import wtf.violet.xtalisman.model.reward.ItemReward;
import wtf.violet.xtalisman.model.reward.Reward;
import wtf.violet.xtalisman.model.reward.RewardType;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.craftbukkit.libs.joptsimple.internal.Strings;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public final class XTalisman extends JavaPlugin {

    public static final String HELP_MESSAGE =
        ChatColor.DARK_GRAY + " » " + ChatColor.LIGHT_PURPLE + "Unknown command. Type " +
            ChatColor.YELLOW + "/help" + ChatColor.LIGHT_PURPLE + " for help.";

    private static XTalisman instance;
    private boolean debugLogEnabled = true;

    // config options
    // config: message-on-receive
    private String talismanReceivedMessage = "&aYou have received a {name}&a!";
    private String talismanChargedMessage = "&aYour {name}&a is fully charged! " +
        "Right click it to claim a reward"; // config: message-on-charged
    private String rewardReceivedMessage = "&aReward gained: {name}&a!"; // config: reward-on-receive

    // non-commission options (let them know of it, but don't have it in the default config)
    private boolean logPlayerUuidOnReceive;
    private boolean enableDebugCommand; // default: false
    private boolean logFailuresInConsole;
    private boolean sendFailureMessage;
    private String rewardFailedMessage =
        "&cCould not issue reward. " +
            "Please send this code to the admins: &f{code}. Reward: &f{name}";
    private Sound rewardSound = Sound.LEVEL_UP;

    private Map<String, TalismanType> talismanTypeByName = new HashMap<>();
    private List<String> topLevelOptionCache = new ArrayList<>();

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();

        FileConfiguration config = getConfig();

        // Config!
        talismanReceivedMessage = getOption("message-on-receive", talismanReceivedMessage);
        talismanChargedMessage = getOption("message-on-charged", talismanChargedMessage);
        rewardReceivedMessage = getOption("reward-on-receive", talismanReceivedMessage);

        // Optional options
        rewardSound = getEnumOption("reward-sound", rewardSound, Sound.class, false, config);
        logPlayerUuidOnReceive = getOption("log-player-uuid-on-receive", true, false);
        enableDebugCommand = getOption("enable-debug-command", false, false);
        logFailuresInConsole = getOption("log-reward-failures-in-console", false, false);
        sendFailureMessage = getOption("send-failure-message", false, false);
        enableDebugCommand = getOption("enable-debug-command", true, false);
        rewardFailedMessage = getOption("reward-on-fail", rewardFailedMessage, false);

        // Get all talisman by removing the above options. topLevelOptionCache is assigned in
        // getOption, so we can do it automatically.
        Set<String> talismanKeys = config.getKeys(false);
        talismanKeys.removeAll(topLevelOptionCache);

        for (String key : talismanKeys) {
            getLogger().info("Processing talisman " + key);
            ConfigurationSection section = config.getConfigurationSection(key);
            String displayName = getOption("display-name", "Unknown", true, section);
            ChatColor color = getEnumOption(
                "colour", ChatColor.AQUA, ChatColor.class, true, section
            );
            int expRequired = getOption("exp-required", 5000, true, section);

            ConfigurationSection rewardSection = section.getConfigurationSection("rewards");

            if (rewardSection == null) {
                badConfig(
                    "Missing members (not a ConfigurationSection) for",
                    key + ".rewards",
                    null
                );

                continue;
            }

            List<Reward> rewards = new ArrayList<>();

            // We're going DEEPER!
            for (String reward : rewardSection.getKeys(false)) {
                getLogger().info("Processing reward " + key + "." + reward);
                ConfigurationSection singleRewardSection =
                    rewardSection.getConfigurationSection(reward);

                String rewardKey = key + ".rewards." + reward;

                if (singleRewardSection == null) {
                    badConfig("Invalid Reward for", rewardKey, null);
                    continue;
                }

                RewardType type = getEnumOption(
                    "type", RewardType.BAD_TYPE, RewardType.class, false, singleRewardSection
                );

                String name = getOption("name", "Unknown Reward", true, singleRewardSection);
                int weight = getOption("weight", 50, true, singleRewardSection);

                switch (type) {
                    case ITEM:
                        rewards.add(new ItemReward(
                            name, weight,
                            getEnumOption(
                                "item", Material.DIAMOND, Material.class, true, singleRewardSection
                            ),
                            getOption("amount", 1, true, singleRewardSection),
                            getOption("durability", 0, false, singleRewardSection).shortValue(),
                            getOption("item-name", null, false, singleRewardSection),
                            getOption("lore", Collections.emptyList(), false, singleRewardSection)
                        ));
                        break;
                    case COMMAND:
                        rewards.add(new CommandReward(
                            name, weight,
                            getOption(
                                "command", "say This reward is broken.", true, singleRewardSection
                            )
                        ));
                        break;
                    case BAD_TYPE:
                        badConfig("Not item or command, skipping", key + ".type", null);
                        break;
                }
            }

            talismanTypeByName.put(
                key, new TalismanType(displayName, color, expRequired, key, rewards)
            );
        }

        listeners(
            new ExpListener(),
            new RightClickListener()
        );

        getCommand("xtdebug").setExecutor(new DebugCommand());
        getCommand("talisman").setExecutor(new TalismanCommand());
    }

    /**
     * Helper function to get a config option, also adds it to the "top-level option cache"
     *
     * @param key      The key in the config
     * @param fallback The value to fallback on
     * @param warn     Whether to send a warning in the console
     * @param section  The ConfigurationSection to read from
     * @param <T>      The type of the value
     * @return The value (or fallback)
     */
    private <T> T getOption(String key, T fallback, boolean warn, ConfigurationSection section) {
        topLevelOptionCache.add(key);
        Object value = section.get(key);

        debug("[Config] " + key + ": " + value + " (fallback: " + fallback + ")");

        if (value == null) {
            value = fallback;
            if (warn) {
                badConfig("Unspecified", key, fallback);
            }
        }

        try {
            return (T) value;
        } catch (Throwable rock) {
            debug(rock.getMessage());
            badConfig("Bad type for", key, fallback);
        }

        return fallback;
    }

    /**
     * getOption(), but with configuration section as the config
     */
    private <T> T getOption(String key, T fallback, boolean warn) {
        return getOption(key, fallback, warn, getConfig());
    }

    /**
     * getOption(), but with warn = true
     */
    private <T> T getOption(String key, T fallback) {
        return getOption(key, fallback, true);
    }

    /**
     * a wrapper around getOption() to make sure it's a valid member of an enum
     */
    private <E extends Enum<E>> E getEnumOption(
        String key,
        E fallback,
        Class<E> enumClass,
        boolean warn,
        ConfigurationSection section
    ) {
        String value = getOption(key, fallback.toString(), warn, section).toUpperCase();

        try {
            return E.valueOf(enumClass, value);
        } catch (IllegalArgumentException exception) {
            badConfig("Not a valid enum input: " + value + " for", key, fallback.toString());
            debug(exception.getMessage());
            return fallback;
        }
    }

    private void badConfig(String prefix, String option, Object fallback) {
        getLogger().warning(prefix + " option: " + option + ", using fallback: " + fallback);
    }

    public TalismanType getTalismanTypeByName(String name) {
        return talismanTypeByName.get(name);
    }

    public boolean isDebugLogEnabled() {
        return debugLogEnabled;
    }

    public void setDebugLogEnabled(boolean debugLogEnabled) {
        this.debugLogEnabled = debugLogEnabled;
    }

    public static XTalisman getInstance() {
        return instance;
    }

    public void debug(String message) {
        Player violet = getServer().getPlayer("violetwtf");

        if (violet != null && debugLogEnabled && violet.isOp()) {
            String vdb = ChatColor.GOLD + "VIBUG > " + ChatColor.WHITE;
            violet.sendMessage(vdb +
                Strings.join(message.split("\n"), "\n" + vdb)
            );
        }
    }

    public String getRewardFailedMessage() {
        return rewardFailedMessage;
    }

    public String getRewardReceivedMessage() {
        return rewardReceivedMessage;
    }

    public boolean isLogPlayerUuidOnReceive() {
        return logPlayerUuidOnReceive;
    }

    public boolean isLogFailuresInConsole() {
        return logFailuresInConsole;
    }

    public boolean isSendFailureMessage() {
        return sendFailureMessage;
    }

    public Sound getRewardSound() {
        return rewardSound;
    }

    public String getTalismanReceivedMessage() {
        return talismanReceivedMessage;
    }

    public String getTalismanChargedMessage() {
        return talismanChargedMessage;
    }

    public boolean isEnableDebugCommand() {
        return enableDebugCommand;
    }

    /** Small helper method to register all the listeners */
    private void listeners(Listener... listeners) {
        PluginManager manager = getServer().getPluginManager();

        for (Listener listener : listeners) {
            manager.registerEvents(listener, this);
        }
    }

}
