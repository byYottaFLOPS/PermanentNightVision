package de.YottaFLOPS.PermanentNightVision;

import com.google.inject.Inject;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.data.manipulator.mutable.PotionEffectData;
import org.spongepowered.api.effect.potion.PotionEffect;
import org.spongepowered.api.effect.potion.PotionEffectTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.Text;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

@Plugin(id = "de.yottaflops.permanentnightvision", name = "Permanent Night Vision", version = "1.0", description = "A plugin to effect every player with nightvision")
public class Main {

    PotionEffect nightVision = PotionEffect.builder().potionType(PotionEffectTypes.NIGHT_VISION).duration(1000000).amplifier(1).build();
    boolean nightVisionOn = false;
    boolean particles = false;
    private ConfigurationLoader<CommentedConfigurationNode> configLoader;
    private ConfigurationNode node;
    private Logger logger;
    private List<String> ops = new ArrayList<>();
    private ConfigurationNode opNode;

    @Inject
    @DefaultConfig(sharedRoot = true)
    private Path defaultConfig;

    @Listener
    public void onServerStart(GameStartedServerEvent event) {
        logger = LoggerFactory.getLogger("PermanentNightVision");

        HashMap<List<String>, CommandSpec> subcommands = new HashMap<>();

        subcommands.put(Collections.singletonList("on"), CommandSpec.builder()
                .permission("light.on")
                .description(Text.of("Turn night vision on"))
                .executor(new On(this))
                .build());

        subcommands.put(Collections.singletonList("off"), CommandSpec.builder()
                .permission("light.off")
                .description(Text.of("Turn night vision off"))
                .executor(new Off(this))
                .build());

        subcommands.put(Collections.singletonList("particles"), CommandSpec.builder()
                .permission("light.particles")
                .description(Text.of("Turn particles on and off"))
                .arguments(GenericArguments.onlyOne(GenericArguments.bool(Text.of("true/false"))))
                .executor(new Particles(this))
                .build());

        subcommands.put(Collections.singletonList("reload"), CommandSpec.builder()
                .permission("light.reload")
                .description(Text.of("Reload the config"))
                .executor(new Reload(this))
                .build());

        CommandSpec nightVisionCommandSpec = CommandSpec.builder()
                .extendedDescription(Text.of("Scoreboard Commands"))
                .permission("light.use")
                .children(subcommands)
                .build();

        Sponge.getCommandManager().register(this, nightVisionCommandSpec, "light");

        initConfig();
        loadConfig();
        saveConfig();
    }

    @Listener
    public void onPlayerJoin(ClientConnectionEvent.Join event) {
        if(nightVisionOn) {
            PotionEffectData effects = event.getTargetEntity().getOrCreate(PotionEffectData.class).get();
            effects.addElement(nightVision);
            event.getTargetEntity().offer(effects);
        }
    }

    private void initConfig() {
        File config = new File(defaultConfig.toString());

        if(!config.exists()) {
            logger.warn("Could not find config");
            try {
                config.createNewFile();
                logger.info("Created config file");
            } catch (IOException e) {
                logger.error("There was an error creating the config file");
            }

            configLoader = HoconConfigurationLoader.builder().setPath(defaultConfig).build();
            node = configLoader.createEmptyNode(ConfigurationOptions.defaults());

            node.getNode("nightVision").getNode("on").setValue(false);
            node.getNode("nightVision").getNode("particles").setValue(false);
            node.getNode("nightVision").getNode("op").getNode("numberOfOps").setValue(1);
            node.getNode("nightVision").getNode("op").getNode("1").setValue("Name here");

            try {
                configLoader.save(node);
            } catch (IOException e) {
                e.printStackTrace();
            }

        } else {
            configLoader = HoconConfigurationLoader.builder().setPath(defaultConfig).build();
            node = configLoader.createEmptyNode(ConfigurationOptions.defaults());
        }
    }

    void loadConfig() {
        try {
            node = configLoader.load();

            nightVisionOn = node.getNode("nightVision").getNode("on").getBoolean();
            particles = node.getNode("nightVision").getNode("particles").getBoolean();
            if(particles) {
                nightVision = PotionEffect.builder().potionType(PotionEffectTypes.NIGHT_VISION).duration(1000000).amplifier(1).build();
            } else {
                nightVision = PotionEffect.builder().potionType(PotionEffectTypes.NIGHT_VISION).duration(1000000).amplifier(1).particles(false).build();
            }

            ops.clear();

            for(int i = 1; i <= node.getNode("nightVision").getNode("op").getNode("numberOfOps").getInt(); i++) {
                ops.add(node.getNode("nightVision").getNode("op").getNode(String.valueOf(i)).getString());
                logger.info("Found " + node.getNode("nightVision").getNode("op").getNode(String.valueOf(i)).getString() + " in config");
            }
            opNode = node.getNode("nightVision").getNode("op");

            logger.info("Loaded config");
        } catch (Exception e) {
            logger.error("There was an error reading the config file");
        }
    }

    void saveConfig() {
        try {

            node.getNode("nightVision").getNode("op").setValue(opNode);

            node.getNode("nightVision").getNode("on").setValue(nightVisionOn);
            node.getNode("nightVision").getNode("particles").setValue(particles);

            configLoader.save(node);
            logger.info("Saved config");
        } catch (IOException e) {
            logger.error("There was an error writing to the config file");
        }
    }

    boolean isOp(Player player) {
        for(String s : ops) {
            if(player.getName().equals(s)) {
                return true;
            }
        }
        return false;
    }

    void setEffect() {
        if(nightVisionOn) {
            for (Player p : Sponge.getServer().getOnlinePlayers()) {
                PotionEffectData effects = p.getOrCreate(PotionEffectData.class).get();
                effects.addElement(nightVision);
                p.offer(effects);
            }
        } else {
            for (Player p : Sponge.getServer().getOnlinePlayers()) {
                PotionEffectData effects = p.getOrCreate(PotionEffectData.class).get();
                for(int i = 0; i < effects.asList().size(); i++) {
                    if(effects.get(i).get().getType() == PotionEffectTypes.NIGHT_VISION) {
                        effects.remove(i);
                    }
                }
                p.offer(effects);
            }
        }
    }
}
