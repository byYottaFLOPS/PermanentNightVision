package de.YottaFLOPS.PermanentNightVision;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.effect.potion.PotionEffect;
import org.spongepowered.api.effect.potion.PotionEffectTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

class Reload implements CommandExecutor {

    private final Main plugin;

    Reload(Main instance) {
        plugin = instance;
    }

    @Override
    public CommandResult execute(CommandSource commandSource, CommandContext commandContext) throws CommandException {

        if(plugin.isOp((Player) commandSource)) {
            plugin.nightVisionOn = true;

            plugin.loadConfig();

            plugin.setEffect();
            commandSource.sendMessage(Text.of(TextColors.GREEN, "Reloaded config!"));

            plugin.saveConfig();
        } else {
            commandSource.sendMessage(Text.of(TextColors.RED, "You don't have the required permissions!"));
        }

        return CommandResult.success();
    }
}
