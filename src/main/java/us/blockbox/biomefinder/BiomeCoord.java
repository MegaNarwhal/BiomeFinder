package us.blockbox.biomefinder;

import org.bukkit.block.Biome;

public class BiomeCoord{
	final Biome biome;
	final Coord coord;

	public BiomeCoord(Biome biome,Coord coord){
		this.biome = biome;
		this.coord = coord;
	}
}
