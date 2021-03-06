package Tournament;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.libs.org.apache.commons.io.FilenameUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.google.gson.Gson;
import com.kunfury.blepFishing.Setup;

import Miscellaneous.Variables;
import net.md_5.bungee.api.ChatColor;
import net.milkbowl.vault.economy.Economy;

import static Miscellaneous.Variables.Messages;
import static Miscellaneous.Variables.Prefix;

public class TournamentRewards implements Listener {
	/**
	 * Gets the rewards of the tournament
	 * @param sender the command sender
	 */
	public void GetRewards(CommandSender sender) {
		Gson gson = new Gson();
		String playerName = sender.getName();
		Player p = (Player)sender;
		List<String> list = new ArrayList<>();
		List<ItemStack> items = new ArrayList<>();
		
		String fileName = Setup.dataFolder + "/Rewards/" + playerName + ".json";
		
		File file = new File(fileName);
		
		if(file.exists()) {
			try {			
				FileReader fr = new FileReader(fileName);
				BufferedReader in = new BufferedReader(fr);
	            String str;
	            while ((str = in.readLine()) != null) {
	                list.add(str);
	            }
				in.close();
	            list.forEach(s -> {
	            	try {
	            		ItemStack savedItem = gson.fromJson(s, ItemStack.class);
	            		if(savedItem != null) {
	            			if(savedItem.getType() == Material.AIR && Setup.hasEcon)
	            				GiveMoney(p, savedItem.getAmount());
	            			else
	            				items.add(savedItem);
	            		}
	            			
	            	}catch(Exception e) {
	            		Logger log = Bukkit.getLogger();
	            		log.severe("Error in " + fileName + " - Please ensure it has not been modified.");
	            	}
	            });
	            if(file.delete()) 
	            { 
	                System.out.println(Prefix + "File deleted successfully");
	            } 
	            else
	            { 
	                System.out.println(Prefix + "Failed to delete the file");
	            } 
	            
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			if(items.size() > 0) { //Only shows the inventory if the player has rewards to claim
				final Inventory inv = Bukkit.createInventory(null, 54, Messages.getString("rewardInvTitle"));
				
				items.forEach(inv::addItem);
				
				p.openInventory(inv);
			}
			
			
		}else
			sender.sendMessage(Messages.getString("noRewards"));
		
		
		
		
	}

	/**
	 * Gives money to a player
	 * @param p the player to receive money
	 * @param value how much money the player should reveive
	 */
	private void GiveMoney(Player p, int value) {
		Economy econ = Setup.getEconomy();
	    econ.depositPlayer(p, value);
	    p.sendMessage("You received " + Variables.CSym + value);
	}

	/**
	 * Checks if the player has any rewards to claim and alerts them if so
	 * @param e event variable
	 */
	@EventHandler
    public void onJoin(PlayerJoinEvent e) {
		Player p = e.getPlayer();
		File folder = new File(Setup.dataFolder + "/Rewards/");
		if(folder.exists()) {
			File[] listOfFiles = folder.listFiles();
			for (int i = 0; i < listOfFiles.length; i++) {
				File f = listOfFiles[i];
				String fileName = FilenameUtils.removeExtension(f.getName());
				  if (f.isFile() && fileName.equals(p.getName())) {
					  Bukkit.getServer().getScheduler().runTaskLater(Setup.getPlugin(), new Runnable() {
				        	@Override
				        	  public void run() {
				        		p.sendMessage(Prefix +  Messages.getString("claimRewards"));
				        		p.sendMessage(Prefix + ChatColor.translateAlternateColorCodes(
				        				'&', Messages.getString("claimRewards2")));
				        	}
				        }, 250);
				  }
			}
		}
	}

	
	
}
