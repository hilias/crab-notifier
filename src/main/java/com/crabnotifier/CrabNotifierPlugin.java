package com.crabnotifier;

import com.crabnotifier.discord.DiscordClient;
import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.NPC;
import net.runelite.api.Actor;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.gameval.NpcID;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

// The code to detect the crab is from afk-crab-helper https://github.com/1504681/afk-crab-helper
@Slf4j
@PluginDescriptor(
	name = "Crab Notifier"
)
public class CrabNotifierPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private CrabNotifierConfig config;

    private boolean isInteractingWithCrab = false;

    // Crab tracking variables
    private NPC currentCrab = null;
    private int lastSeenHealthRatio = 0;

    // Messaging variables
    private DiscordClient discordClient;
    private long lastNotification = 0;

	@Override
	protected void startUp() throws Exception
    {
        discordClient = new DiscordClient(config);
		log.info("CrabNotifier started!");
	}

	@Override
	protected void shutDown() throws Exception
	{
		log.info("CrabNotifier stopped!");
	}

    @Subscribe
    public void onGameTick(GameState gameState)
    {
        if (client.getLocalPlayer() == null)
        {
            return;
        }
        checkCrabInteraction();
        checkCrabStatus();
        sendMessage();
    }

	@Provides
    CrabNotifierConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(CrabNotifierConfig.class);
	}

    private void sendMessage() {
        if (currentCrab == null) {
            return;
        }

        if (lastSeenHealthRatio < 0.02 && lastNotification + (5000 * 60) < System.currentTimeMillis()) {
            discordClient.sendMessage("Crab Time Almost Over!!", config.userId());
        }
    }

    private void checkCrabInteraction()
    {
        Actor target = client.getLocalPlayer().getInteracting();
        boolean currentlyInteractingWithCrab = false;

        if (target instanceof NPC)
        {
            NPC npc = (NPC) target;

            if (isCrabNpc(npc))
            {
                currentlyInteractingWithCrab = true;
                int npcHealthRatio = npc.getHealthRatio();

                // If this is a new crab or we weren't tracking before, update tracking
                if (currentCrab != npc || lastSeenHealthRatio != npcHealthRatio)
                {
                    currentCrab = npc;
                    lastSeenHealthRatio = npcHealthRatio;
                }
            }
        }

        // Also check if we have a valid crab target and are still actively interacting
        if (currentCrab != null && isCrabValid(currentCrab) && target == currentCrab)
        {
            currentlyInteractingWithCrab = true;
        }

        if (!currentlyInteractingWithCrab && isInteractingWithCrab)
        {
            // Stopping interaction - reset crab tracking
            currentCrab = null;
        }

        isInteractingWithCrab = currentlyInteractingWithCrab;
    }

    private void checkCrabStatus()
    {
        // Check if our tracked crab is still valid
        if (currentCrab == null)
        {
            return;
        }

        // Check if it's our current crab that despawned
        if (isCrabValid(currentCrab))
        {
            return;
        }

        // Reset tracking
        isInteractingWithCrab = false;
        currentCrab = null;
    }

    private boolean isCrabValid(NPC currentCrab) {
        Actor player = client.getLocalPlayer();
        return player.getWorldView().npcs().stream()
                .anyMatch(npc -> npc == currentCrab);
    }

    private boolean isCrabNpc(NPC npc)
    {
        if (npc == null) return false;
        return npc.getId() == NpcID.GEMSTONE_CRAB;
    }

    @Subscribe
    public void onNpcDespawned(NpcDespawned event)
    {
        NPC npc = event.getNpc();

        // Check if it's our current crab that despawned
        if (npc != currentCrab)
        {
            return;
        }

        isInteractingWithCrab = false;
        currentCrab = null;
    }
}
