package us.blockbox.biomefinder;

import org.bukkit.Bukkit;
import org.fusesource.jansi.Ansi;

import java.util.logging.Logger;

//Created 11/9/2016 3:12 AM
public class ConsoleMessager{

	private static final Logger log = Bukkit.getLogger();
	private static final String prefix = BiomeFinder.prefix;

	public static void warn(final String... msg){
		for(final String s : msg){
			log.warning(Ansi.ansi().a(Ansi.Attribute.RESET).fg(Ansi.Color.YELLOW).bold().toString() + prefix + s + Ansi.ansi().reset().toString());
		}
	}

	public static void info(final String... msg){
		for(final String s : msg){
			log.info(prefix + s);
		}
	}
}
