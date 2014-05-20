/**
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 */

package modfixng.fixes;

import modfixng.main.ModFixNG;
import modfixng.utils.ModFixNGUtils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

public class FixPlayerArmorSlotDesync implements Feature {

	private BukkitTask task;

	// resend armor slot items every tick
	private void startInvSync() {
		task = Bukkit.getScheduler().runTaskTimer(
			ModFixNG.getInstance(),
			new Runnable() {
				@Override
				public void run() {
					for (Player player : Bukkit.getOnlinePlayers()) {
						ModFixNGUtils.updateSlot(ModFixNG.getProtocolManager(), player, 0, 5, player.getInventory().getHelmet());
						ModFixNGUtils.updateSlot(ModFixNG.getProtocolManager(), player, 0, 6, player.getInventory().getChestplate());
						ModFixNGUtils.updateSlot(ModFixNG.getProtocolManager(), player, 0, 7, player.getInventory().getLeggings());
						ModFixNGUtils.updateSlot(ModFixNG.getProtocolManager(), player, 0, 8, player.getInventory().getBoots());
					}
				}
			},
			0, 20
		);
	}

	@Override
	public void load() {
		startInvSync();
	}

	@Override
	public void unload() {
		task.cancel();
	}

	@Override
	public String getName() {
		return "FixArmorDesync";
	}

}
