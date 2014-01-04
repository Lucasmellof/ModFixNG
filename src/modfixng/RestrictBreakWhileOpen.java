package modfixng;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;

public class RestrictBreakWhileOpen implements Listener {

	private ModFixNG main;
	private Config config;

	RestrictBreakWhileOpen(ModFixNG main, Config config) {
		this.main = main;
		this.config = config;
		initClientCloseInventoryFixListener();
		initServerCloseInventoryFixListener();
	}


	private HashMap<String,BlockState> playerOpenBlock = new HashMap<String,BlockState>(100);

	@EventHandler(priority=EventPriority.MONITOR,ignoreCancelled=true)
	public void onPlayerOpenedBlock(PlayerInteractEvent e)
	{
		if (!config.restrictBlockBreakWhileOpenEnabled) {return;}

		if (e.getAction() != Action.RIGHT_CLICK_BLOCK) {return;}
		
		Block b = e.getClickedBlock();
		if (config.restrictBlockBreakWhileOpenEnabledIDs.contains(ModFixNGUtils.getIDstring(b)))
		{
			if (!config.restrictBlockBreakWhileOpenItemInhandExclusions.contains(ModFixNGUtils.getIDstring(e.getPlayer().getItemInHand())))
			{
				playerOpenBlock.put(e.getPlayer().getName(), b.getState());
			}
		}
	}
	
	//remove player from list when he closes inventory
	private void initClientCloseInventoryFixListener()
	{
		main.protocolManager.addPacketListener(
				new PacketAdapter(
						PacketAdapter
						.params(main, PacketType.Play.Client.CLOSE_WINDOW)
						.clientSide()
						.listenerPriority(ListenerPriority.HIGHEST)
				) 
				{
					@Override
					public void onPacketReceiving(PacketEvent e) 
					{
						if (!config.restrictBlockBreakWhileOpenEnabled) {return;}
						
						if (e.getPlayer() == null) {return;}
						
						final String playername = e.getPlayer().getName();
						Bukkit.getScheduler().scheduleSyncDelayedTask(main, new Runnable()
						{
							public void run()
							{
								removePlayerData(playername);
							}
						});
					}
				});
	}
	private void initServerCloseInventoryFixListener()
	{
		main.protocolManager.addPacketListener(
				new PacketAdapter(
						PacketAdapter
						.params(main, PacketType.Play.Server.CLOSE_WINDOW)
						.serverSide()
						.listenerPriority(ListenerPriority.HIGHEST)
				) 
				{
					@Override
					public void onPacketSending(PacketEvent e) 
					{
						if (!config.restrictBlockBreakWhileOpenEnabled) {return;}
						
						removePlayerData(e.getPlayer().getName());
				    }
				});
	}
	@EventHandler(priority=EventPriority.MONITOR)
	public void onQuit(PlayerQuitEvent e)
	{
		if (!config.restrictBlockBreakWhileOpenEnabled) {return;}
		
		removePlayerData(e.getPlayer().getName());
	}
	
	private void removePlayerData(String playername)
	{
		playerOpenBlock.remove(playername);
	}
	
	//restrict block break while block is open
	@EventHandler(priority=EventPriority.HIGHEST)
	public void onBlockBreak(BlockBreakEvent e)
	{
		if (!config.restrictBlockBreakWhileOpenEnabled) {return;}
		
		Block brokenblock = e.getBlock();
		for (BlockState bs : playerOpenBlock.values())
		{
			if (bs.getBlock().equals(brokenblock))
			{
				e.setCancelled(true);
				e.getPlayer().sendMessage(ChatColor.RED+"Вы не можете сломать этот блок пока он открыт другим игроком");
				return;
			}
		}
	}
	
}