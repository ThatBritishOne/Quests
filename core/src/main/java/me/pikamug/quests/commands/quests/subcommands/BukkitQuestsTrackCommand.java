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
import me.pikamug.quests.player.BukkitQuester;
import me.pikamug.quests.player.Quester;
import me.pikamug.quests.quests.Quest;
import me.pikamug.quests.quests.components.Stage;
import me.pikamug.quests.util.BukkitLang;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class BukkitQuestsTrackCommand extends BukkitQuestsSubCommand {

    private final BukkitQuestsPlugin plugin;

    public BukkitQuestsTrackCommand(final BukkitQuestsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "track";
    }

    @Override
    public String getNameI18N() {
        return BukkitLang.get("COMMAND_TRACK");
    }

    @Override
    public String getDescription() {
        return BukkitLang.get("COMMAND_TRACK_HELP");
    }

    @Override
    public String getPermission() {
        return "quests.track";
    }

    @Override
    public String getSyntax() {
        return "/quests track [quest|clear|next]";
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
        if (quester.getCurrentQuests().isEmpty()) {
            cs.sendMessage(ChatColor.YELLOW + BukkitLang.get(player, "noActiveQuest"));
            return;
        }

        if (args.length <= 1) {
            final Quest tracked = quester.getTrackedQuest();
            if (tracked != null && quester.getCurrentQuests().containsKey(tracked)) {
                String msg = BukkitLang.get(player, "trackingCurrent");
                cs.sendMessage(ChatColor.YELLOW + (!msg.equals("NULL") ? msg : "Currently tracking <quest>.").replace("<quest>", tracked.getName()));
            } else {
                String msg = BukkitLang.get(player, "trackingCurrentNone");
                cs.sendMessage(ChatColor.YELLOW + (!msg.equals("NULL") ? msg : "You are not tracking a specific quest."));
            }
            return;
        }

        final String selected = concatArgArray(args, 1, args.length - 1, ' ');
        if (selected == null) {
            cs.sendMessage(ChatColor.RED + getSyntax());
            return;
        }
        final String lowered = ChatColor.stripColor(selected).toLowerCase(Locale.ENGLISH);
        if (lowered.equals("clear") || lowered.equals("none") || lowered.equals("off")) {
            quester.setTrackedQuest(null);
            quester.findCompassTarget();
            quester.saveData();
            String msg = BukkitLang.get(player, "trackingCleared");
            cs.sendMessage(ChatColor.YELLOW + (!msg.equals("NULL") ? msg : "Cleared tracked quest selection."));
            return;
        }
        if (lowered.equals("next")) {
            if (quester instanceof BukkitQuester) {
                ((BukkitQuester) quester).advanceTrackingObjective();
                cs.sendMessage(ChatColor.YELLOW + "Switched tracked objective step.");
            }
            return;
        }

        Quest found = null;
        for (final Quest active : quester.getCurrentQuests().keySet()) {
            final String cleanName = ChatColor.stripColor(active.getName());
            if (cleanName.equalsIgnoreCase(selected)) {
                found = active;
                break;
            }
        }
        if (found == null) {
            for (final Quest active : quester.getCurrentQuests().keySet()) {
                final String cleanName = ChatColor.stripColor(active.getName());
                if (cleanName.toLowerCase(Locale.ENGLISH).startsWith(lowered)) {
                    found = active;
                    break;
                }
            }
        }
        if (found == null) {
            String msg = BukkitLang.get(player, "trackingQuestNotActive");
            cs.sendMessage(ChatColor.RED + (!msg.equals("NULL") ? msg : "Quest <quest> is not currently active.").replace("<quest>", selected));
            return;
        }

        quester.setTrackedQuest(found);
        final Stage stage = quester.getCurrentStage(found);
        if (stage != null) {
            if (stage.hasLocatableObjective()) {
                found.updateCompass(quester, stage);
            } else {
                quester.setCompassTarget(found);
            }
        }
        quester.saveData();
        String trackMsg = BukkitLang.get(player, "trackingSet");
        cs.sendMessage(ChatColor.GREEN + (!trackMsg.equals("NULL") ? trackMsg : "Now tracking <quest>.").replace("<quest>", found.getName()));
    }

    @Override
    public List<String> tabComplete(final CommandSender commandSender, final String[] args) {
        if (!(commandSender instanceof Player) || args.length != 2) {
            return Collections.emptyList();
        }
        final Player player = (Player) commandSender;
        final Quester quester = plugin.getQuester(player.getUniqueId());
        final List<String> results = new ArrayList<>();
        for (final Quest active : quester.getCurrentQuests().keySet()) {
            results.add(ChatColor.stripColor(active.getName()));
        }
        results.add("clear");
        results.add("next");
        results.sort(Comparator.naturalOrder());
        final String input = args[1].toLowerCase(Locale.ENGLISH);
        results.removeIf(name -> !name.toLowerCase(Locale.ENGLISH).startsWith(input));
        return results;
    }
}
