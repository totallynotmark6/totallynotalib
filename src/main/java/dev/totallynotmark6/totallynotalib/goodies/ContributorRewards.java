package dev.totallynotmark6.totallynotalib.goodies;

import com.google.common.collect.ImmutableSet;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import dev.totallynotmark6.totallynotalib.totallynotalib;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;

// Devs get a special cape (and, y'know, all of the rewards)!
// Contributers get... something.
// I dunno, it depends on the mod

/**
 * Handles contributor rewards!
 * @since 1.0.0
 */
@Mod.EventBusSubscriber(modid = "totallynotalib")
public class ContributorRewards {
    private static final ImmutableSet<String> DEV_UUID = ImmutableSet.of(
            "71d1be34-aef8-4a07-ac32-eb9e1c25a4fa"
    );

    private static final Set<String> done = Collections.newSetFromMap(new WeakHashMap<>());

    private static Thread thread;
    private static String name;

    private static final Map<String, Integer> tiers = new HashMap<>();

    /**
     * The user's Patron tier!
     * ...I don't have anything setup on that end yet, so...
     * I'd also wouldn't use this before joining a world, since the list loads when the player joins the world.
     */
    public static int localPatronTier = 0;

    /**
     * A randomly featured patron! I have no idea what this would be used for.
     */
    public static String featuredPatron = "N/A";

    @OnlyIn(Dist.CLIENT)
    public static void getLocalName() {
        name = Minecraft.getInstance().getUser().getName().toLowerCase(Locale.ROOT);
    }

    public static void init() {
        getLocalName();
        if (thread != null && thread.isAlive())
            return;

        thread = new ThreadContributorListLoader();
    }

    /**
     * Gets the Player's patron tier.
     * @see ContributorRewards#getTier(Player)
     * @param player Player to investigate
     * @return The tier of the player
     */
    public static int getTier(Player player) {
        return getTier(player.getGameProfile().getName());
    }

    /**
     * Gets the patron tier for the given name.
     * @see ContributorRewards#getTier(Player)
     * @param name The name of the player to investigate.
     * @return The tier of the player
     */
    public static int getTier(String name) {
        return tiers.getOrDefault(name.toLowerCase(Locale.ROOT), 0);
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void onRenderPlayer(RenderPlayerEvent.Post event) {
        Player player = event.getPlayer();
        String uuid = Player.createPlayerUUID(player.getGameProfile()).toString();
        if(player instanceof AbstractClientPlayer && DEV_UUID.contains(uuid) && !done.contains(uuid)) {
            AbstractClientPlayer clientPlayer = (AbstractClientPlayer) player;
            if(clientPlayer.isCapeLoaded()) {
                PlayerInfo info = clientPlayer.playerInfo;
                Map<MinecraftProfileTexture.Type, ResourceLocation> textures = info.textureLocations;
                ResourceLocation loc = new ResourceLocation("totallynotalib", "textures/misc/dev_cape.png"); // TODO: Change this to the actual cape
                textures.put(MinecraftProfileTexture.Type.CAPE, loc);
                textures.put(MinecraftProfileTexture.Type.ELYTRA, loc);
                done.add(uuid);
            }
        }
    }

    @SubscribeEvent
//    @OnlyIn(Dist.DEDICATED_SERVER)
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        init();
    }

    private static void load(Properties props) {
        List<String> allPatrons = new ArrayList<>(props.size());

        props.forEach((k, v) -> {
            String key = (String) k;
            String value = (String) v;

            int tier = Integer.parseInt(value);
            if(tier < 10)
                allPatrons.add(key);
            tiers.put(key.toLowerCase(Locale.ROOT), tier);

            if(key.toLowerCase(Locale.ROOT).equals(name))
                localPatronTier = tier;
        });

        if(!allPatrons.isEmpty())
            featuredPatron = allPatrons.get((int) (Math.random() * allPatrons.size()));
    }

    private static class ThreadContributorListLoader extends Thread {

        public ThreadContributorListLoader() {
            setName("totallynotalib Contributor Loading Thread");
            setDaemon(true);
            start();
        }

        @Override
        public void run() {
            try {
                URL url = new URL("https://raw.githubusercontent.com/totallynotmark6/totallynotalib/master/contributors.properties");
                URLConnection conn = url.openConnection();
                conn.setConnectTimeout(10*1000);
                conn.setReadTimeout(10*1000);

                Properties patreonTiers = new Properties();
                try (InputStreamReader reader = new InputStreamReader(conn.getInputStream())) {
                    patreonTiers.load(reader);
                    load(patreonTiers);
                }
            } catch (IOException e) {
                totallynotalib.LOGGER.error("Failed to load patreon information", e);
            }
        }

    }
}
