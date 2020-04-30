package us.blockbox.biomefinder.util;

import org.bukkit.block.Biome;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum Biomes{
	;

	private static final ThreadLocal<Matcher> ignored = new ThreadLocal<Matcher>(){
		@Override
		protected Matcher initialValue(){
			return Pattern.compile("[_ ]+").matcher("");
		}
	};
	private static final Map<String,Biome> map;

	static{
		map = new HashMap<>();
		for(Biome biome : Biome.values()){
			String nameSanitized = sanitize(biome.name());
			Biome existing = map.get(nameSanitized);
			if(existing != null){
				{
					throw new IllegalStateException("Biome name collision! " + existing.name() + " " + biome.name());
				}
			}
			map.put(nameSanitized,biome);
		}
	}

	public static Biome match(String name){
		String nameSanitized = sanitize(name);
		if(nameSanitized == null) return null;
		return map.get(nameSanitized);
	}

	public static Biome matchPartial(String name){
		String nameSanitized = sanitize(name);
		if(nameSanitized == null) return null;
		Biome matchFull = map.get(nameSanitized);
		if(matchFull == null){
			for(Map.Entry<String,Biome> entry : map.entrySet()){
				if(entry.getKey().startsWith(nameSanitized)){
					return entry.getValue();
				}
			}
			return null;
		}else{
			return matchFull;
		}
	}

	private static Matcher getMatcher(String s){
		return ignored.get().reset(s);
	}

	private static String sanitize(String s){
		if(s == null) return null;
		String s1 = getMatcher(s.toUpperCase(Locale.US)).replaceAll("");
		return s1.isEmpty() ? null : s1;
	}
}
