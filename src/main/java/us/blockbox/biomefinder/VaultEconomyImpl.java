package us.blockbox.biomefinder;

import org.bukkit.OfflinePlayer;
import us.blockbox.biomefinder.api.Economy;

import java.util.Objects;

public final class VaultEconomyImpl implements Economy{
	private final net.milkbowl.vault.economy.Economy vaultEconomy;

	public VaultEconomyImpl(net.milkbowl.vault.economy.Economy vaultEconomy){
		this.vaultEconomy = Objects.requireNonNull(vaultEconomy);
	}

	@Override
	public double balance(OfflinePlayer player){
		return vaultEconomy.getBalance(player);
	}

	@Override
	public boolean has(OfflinePlayer player,double amount){
		return vaultEconomy.has(player,amount);
	}

	@Override
	public boolean deposit(OfflinePlayer player,double amount){
		return vaultEconomy.depositPlayer(player,amount).transactionSuccess();
	}

	@Override
	public boolean withdraw(OfflinePlayer player,double amount){
		return vaultEconomy.withdrawPlayer(player,amount).transactionSuccess();
	}

	@Override
	public String format(double amount){
		return vaultEconomy.format(amount);
	}
}
