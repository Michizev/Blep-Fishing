package com.kunfury.blepFishing.Signs;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.Plugin;

import com.kunfury.blepFishing.Setup;

import Miscellaneous.FishEconomy;
import Miscellaneous.Variables;
import Objects.FishObject;
import Objects.MarketObject;

import org.bukkit.block.Block;
import org.bukkit.block.Sign;


public class FishSign implements Listener {
	
	public static List<SignObject> rankSigns = new ArrayList<>();
	public static List<MarketObject> marketSigns = new ArrayList<>();
	static String signFilePath = Setup.dataFolder + "/signs.data";
	static String marketFilePath = Setup.dataFolder + "/markets.data";   
	public static List<Location> signLocs = new ArrayList<>();
	@SuppressWarnings("deprecation")
	List<Material> signs = Arrays.asList(Material.OAK_SIGN, Material.OAK_WALL_SIGN,
										Material.DARK_OAK_SIGN, Material.DARK_OAK_WALL_SIGN,
										Material.ACACIA_SIGN, Material.ACACIA_WALL_SIGN,
										Material.BIRCH_SIGN, Material.BIRCH_WALL_SIGN,
										Material.JUNGLE_SIGN, Material.JUNGLE_WALL_SIGN,
										Material.SPRUCE_SIGN, Material.SPRUCE_WALL_SIGN,
										Material.CRIMSON_SIGN, Material.CRIMSON_WALL_SIGN, 
										Material.WARPED_SIGN, Material.WARPED_WALL_SIGN,										
										Material.LEGACY_SIGN, Material.LEGACY_SIGN_POST, 
										Material.LEGACY_WALL_SIGN);
	
	DecimalFormat df = new DecimalFormat("#.##");
	
	@EventHandler
	public void onChange(SignChangeEvent e){
		String[] lines = e.getLines();
		Player player = e.getPlayer();
		
		//Beginning of new sign creation
		if(lines[0].equals("[bf]")) { //Checks that the sign is a Blep Fishing sign			
			if(lines[1].equals("Fish Market")) {
				MarketCreate((Sign)e.getBlock().getState(), player.getWorld());
				return;
			}else 
			{
			//Checks if fish exist in the main list in FishSwitch
	    	for(FishObject fish : Variables.FishList) {
	    		if(fish.Name.equalsIgnoreCase(e.getLine(1))) {
	    			int level = 0;
	    			if(!lines[2].isEmpty()) { //Gets the provided leaderboard level
	    				try {
	        				level = Integer.parseInt(lines[2]) - 1;
	        				if(level <= 0)
	        					level = 0;
	        			}catch(Exception ex) {
	        				player.sendMessage("Third line is not a number, defaulting to 1st place.");
	        				level = 0;
	        			}
	    			}
	    			
	    			LeaderboardCreate((Sign)e.getBlock().getState(), level, lines[1], player.getWorld());
	    			break;
	    		}
	    	}
	    	e.setLine(3, ChatColor.translateAlternateColorCodes('&',"&4Fish Doesn't Exist"));
			}
		}
		
		
	}
	
	public void UpdateSigns() {
		for (SignObject signObj : rankSigns) {
			if(signObj.GetSign() != null) {
				RefreshSign.Refresh(signObj);
			}				
		}
	}
		
	@SuppressWarnings("unchecked")
	public static void LoadSigns() {
        //Load Leaderboard Signs
        try 
        {
        	rankSigns.clear();
        	ObjectInputStream input = null;
		    File tempFile = new File(signFilePath);
		    if(tempFile.exists()) {
    		    input = new ObjectInputStream(new FileInputStream (signFilePath));
    		    rankSigns = (List<SignObject>)input.readObject();
		    }
		    if(input != null)
		    	input.close();
		} catch (IOException | ClassNotFoundException ex) {
			ex.printStackTrace();
		}
        ///
        //Load Market Signs
        ///
        try 
        {
        	marketSigns.clear();
        	ObjectInputStream input = null;
		    File tempFile = new File(marketFilePath);
		    if(tempFile.exists()) {
    		    input = new ObjectInputStream(new FileInputStream (marketFilePath));
    		    marketSigns = (List<MarketObject>)input.readObject();
		    }
		    if(input != null)
		    	input.close();
        } catch (IOException | ClassNotFoundException ex) {
			ex.printStackTrace();
		}
	}
	
	private void LeaderboardCreate(Sign sign, int level, String fishName, World world) {
		
		rankSigns.add(new SignObject(sign, fishName, level, world));
		
		 //Save Fish
		try {
		    ObjectOutputStream output = new ObjectOutputStream(new FileOutputStream(signFilePath));
		    output.writeObject(rankSigns);
		    output.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		Bukkit.getScheduler().runTaskLater((Plugin) Setup.getPlugin(), new Runnable() {
			  @Override
			  public void run() {
				  UpdateSigns();
			  }
			}, 1L);
	}
	
	private void MarketCreate(Sign sign, World world) {
		
		marketSigns.add(new MarketObject(sign, world));
		
		 //Save Fish
		try {
		    ObjectOutputStream output = new ObjectOutputStream(new FileOutputStream(marketFilePath));
		    output.writeObject(marketSigns);
		    output.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		Bukkit.getScheduler().runTaskLater((Plugin) Setup.getPlugin(), new Runnable() {
			  @Override
			  public void run() {
				  	sign.setLine(0, "-------------");
	    			sign.setLine(1, "Fish");
	    			sign.setLine(2, "Market");
	    			sign.setLine(3, "-------------");
	    			sign.update();
	    			UpdateSigns();
			  }
			}, 1L);
	}
	
	@EventHandler
	public void onSignBreak(BlockBreakEvent e) {
		if(signs.contains(e.getBlock().getType())) { //Checks if the block is a sign
			if(rankSigns != null && rankSigns.size() > 0) {
				Sign bSign = (Sign)e.getBlock().getState();
				//The foreach loop is erroring
				try {
					for (SignObject signObj : rankSigns) {
						try {
							if(signObj.GetSign().equals(bSign)) {
								rankSigns.remove(signObj);
								break;
							}
						}catch(Exception ex) {
							rankSigns.remove(signObj);
						}
					}
				}
				catch(Exception ex) {
					//This is just needed because for some reason the sign list has an error sign in it
				}
				
			}
			if(marketSigns != null && marketSigns.size() > 0) {
				Sign bSign = (Sign)e.getBlock().getState();
				//The foreach loop is erroring
				try {
					for (MarketObject marketObj : marketSigns) {
						try {
							if(marketObj.GetSign().equals(bSign)) {
								marketSigns.remove(marketObj);
								break;
							}
						}catch(Exception ex) {
							marketSigns.remove(marketObj);
						}
					}
				}
				catch(Exception ex) {
					//This is just needed because for some reason the sign list has an error sign in it
				}
			}
			
		}
	}
	
	@EventHandler
	public void onUseEvent(PlayerInteractEvent e) {
		if(e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
			Block b = e.getClickedBlock();
			
			if(Setup.hasEcon && signs.contains(b.getType()) && e.getItem() != null  && e.getItem().getType() == Material.SALMON){
				
				for(MarketObject market : marketSigns) {
					if(market.CheckBool((Sign)b.getState())){
					    FishEconomy.SellFish(e.getPlayer());					    
						break;
					}
				}
			}
		}
		
	}
}