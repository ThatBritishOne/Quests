/*
 * Copyright (c) PikaMug and contributors
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package me.pikamug.quests.commands.quests.subcommands;

import me.pikamug.quests.BukkitQuestsPlugin;
import me.pikamug.quests.commands.BukkitQuestsSubCommand;
import me.pikamug.quests.player.Quester;
import me.pikamug.quests.util.BukkitLang;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class BukkitQuestsTrackbarCommand extends BukkitQuestsSubCommand {

    private final BukkitQuestsPlugin plugin;

    public BukkitQuestsTrackbarCommand(final BukkitQuestsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "trackbar";
    }

    @Override
    public String getNameI18N() {
        return BukkitLang.get("COMMAND_TRACKBAR");
    }

    @Override
    public String getDescription() {
        return BukkitLang.get("COMMAND_TRACKBAR_HELP");
    }

    @Override
    public String getPermission() {
        return "quests.trackbar";
    }

    @Override
    public String getSyntax() {
        return "/quests trackbar [on|off|toggle]";
    }

    @Override
    public int getMaxArguments() {
        return 1;
    }

    @Override
    public void execute(final CommandSender cs, final String[] args) {
        if (assertNonPlayer(cs)) {
            return;
        }
        if (!cs.hasPermission(getPermission())) {
            cs.sendMessage(ChatColor.RED + BukkitLang.get("noPermission"));
            return;
        }
        final Player player = (Player) cs;
        final Quester quester = plugin.getQuester(player.getUniqueId());

        if (!plugin.getConfigSettings().canEnableTrackingBossBar()) {
            String msg = BukkitLang.get(player, "trackingBossBarGloballyDisabled");
            cs.sendMessage(ChatColor.RED + (!msg.equals("NULL") ? msg : "Tracking bossbar is disabled in config."));
            return;
        }

        if (args.length <= 1) {
            String enabledStr = BukkitLang.get(player, "enabled");
            String disabledStr = BukkitLang.get(player, "disabled");
            final String status = quester.canShowTrackingBossBar() ? (!enabledStr.equals("NULL") ? enabledStr : "enabled") : (!disabledStr.equals("NULL") ? disabledStr : "disabled");
            String statusMsg = BukkitLang.get(player, "trackingBossBarStatus");
            cs.sendMessage(ChatColor.YELLOW + (!statusMsg.equals("NULL") ? statusMsg : "Tracking bossbar is currently <status>.").replace("<status>", status));
            return;
        }

        final String mode = args[1].toLowerCase(Locale.ENGLISH);
        final boolean enabled;
        if (mode.equals("on")) {
            enabled = true;
        } else if (mode.equals("off")) {
            enabled = false;
        } else if (mode.equals("toggle")) {
            enabled = !quester.canShowTrackingBossBar();
        } else {
            cs.sendMessage(ChatColor.RED + getSyntax());
            return;
        }

        quester.setShowTrackingBossBar(enabled);
        quester.saveData();
        if (enabled) {
            String msg = BukkitLang.get(player, "trackingBossBarEnabled");
            cs.sendMessage(ChatColor.GREEN + (!msg.equals("NULL") ? msg : "Tracking bossbar enabled."));
        } else {
            String msg = BukkitLang.get(player, "trackingBossBarDisabled");
            cs.sendMessage(ChatColor.YELLOW + (!msg.equals("NULL") ? msg : "Tracking bossbar disabled."));
        }
    }

    @Override
    public List<String> tabComplete(final CommandSender commandSender, final String[] args) {
        if (args.length != 2) {
            return Collections.emptyList();
        }
        final List<String> modes = new ArrayList<>();
        modes.add("on");
        modes.add("off");
        modes.add("toggle");
        final String input = args[1].toLowerCase(Locale.ENGLISH);
        modes.removeIf(mode -> !mode.startsWith(input));
        return modes;
    }
}
