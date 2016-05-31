package de.YottaFLOPS.PermanentNightVision;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.effect.potion.PotionEffect;
import org.spongepowered.api.effect.potion.PotionEffectTypes;

class Particles implements CommandExecutor {

    private final Main plugin;

    Particles(Main instance) {
        plugin = instance;
    }

    @Override
    public CommandResult execute(CommandSource commandSource, CommandContext commandContext) throws CommandException {

        if(commandContext.<Boolean>getOne("true/false").isPresent()) {
            plugin.particles = commandContext.<Boolean>getOne("true/false").get();

            if(plugin.nightVisionOn) {
                plugin.nightVision = PotionEffect.builder().potionType(PotionEffectTypes.NIGHT_VISION).duration(1000000).amplifier(1).particles(commandContext.<Boolean>getOne("true/false").get()).build();
            } else {
                plugin.nightVision = PotionEffect.builder().potionType(PotionEffectTypes.NIGHT_VISION).duration(1).amplifier(1).particles(commandContext.<Boolean>getOne("true/false").get()).build();

            }
        }

        plugin.setEffect();

        plugin.saveConfig();

        return CommandResult.success();
    }
}
