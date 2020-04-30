package us.blockbox.biomefinder.api;

import org.bukkit.OfflinePlayer;

public interface Economy{
	double balance(OfflinePlayer player);

	boolean has(OfflinePlayer player,double amount);

	boolean deposit(OfflinePlayer player,double amount);

	boolean withdraw(OfflinePlayer player,double amount);

	String format(double amount);
}
