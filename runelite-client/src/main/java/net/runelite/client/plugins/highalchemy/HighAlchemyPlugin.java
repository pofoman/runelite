package net.runelite.client.plugins.highalchemy;

import net.runelite.api.Client;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemID;
import net.runelite.api.Skill;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import javax.inject.Inject;
import net.runelite.api.events.GameTick;
import net.runelite.client.eventbus.Subscribe;

// Plugin descriptor providing meta information for RuneLite client
@PluginDescriptor(
    name = "High Alchemy Automator",
    description = "Automates casting High Alchemy on items"
)
public class HighAlchemyPlugin extends Plugin {
    @Inject private Client client;
    @Inject private ClientThread clientThread;
    @Inject private HighAlchemyConfig config;

    // Checks if the player's Magic level is sufficient for High Alchemy
    public boolean hasHighAlchRequirements() {
        int magicLevel = client.getRealSkillLevel(Skill.MAGIC);
        return magicLevel >= 55;
    }

    // Checks if the player has the necessary runes in their inventory
    public boolean hasRequiredRunes() {
        if (client.getItemContainer(InventoryID.INVENTORY) == null) {
            return false;
        }
        int natureRuneCount = 0;
        int fireRuneCount = 0;
        for (Item item : client.getItemContainer(InventoryID.INVENTORY).getItems()) {
            if (item.getId() == ItemID.NATURE_RUNE) {
                natureRuneCount += item.getQuantity();
            } else if (item.getId() == ItemID.FIRE_RUNE) {
                fireRuneCount += item.getQuantity();
            }
        }
        return natureRuneCount >= 1 && fireRuneCount >= 5;
    }

    // Checks if the player is using the standard spellbook
    public boolean isOnStandardSpellbook() {
        return client.getVarbitValue(4070) == 0;
    }

    // Handles game ticks to continuously check for casting conditions
    @Subscribe
    public void onGameTick(GameTick tick) {
        if (config.enableAutomation() && hasRequiredRunes() && isOnStandardSpellbook()) {
            System.out.println("Ready to cast High Alchemy.");
        } else {
            System.out.println("Cannot cast High Alchemy.");
        }
    }

    // Initializes plugin state and checks initial conditions
    @Override
    protected void startUp() throws Exception {
        System.out.println("High Alchemy Plugin started!");
        clientThread.invokeLater(() -> {
            if (hasHighAlchRequirements() && hasRequiredRunes() && isOnStandardSpellbook()) {
                System.out.println("Player meets all conditions for High Alchemy.");
            } else {
                System.out.println("Player does NOT meet all conditions for High Alchemy.");
            }
        });
    }

    // Configuration interface for plugin settings
    public interface HighAlchemyConfig extends Config {
        @ConfigItem(
            keyName = "enableAutomation",
            name = "Enable Automation",
            description = "Toggle the automation of High Alchemy casting."
        )
        default boolean enableAutomation() {
            return false;
        }
    }

    // Cleans up resources and states when the plugin is stopped
    @Override
    protected void shutDown() throws Exception {
        System.out.println("High Alchemy Plugin stopped!");
    }
}
