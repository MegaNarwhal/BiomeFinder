package us.blockbox.biomefinder;

import org.fusesource.jansi.Ansi;

import java.util.logging.Logger;

import static us.blockbox.biomefinder.BiomeFinder.plugin;

//Created 11/9/2016 3:12 AM
public class ConsoleMessager{

	private static final Logger log = plugin.getLogger();

	public static void warn(final String... msg){
		for(final String s : msg){
			log.warning(Ansi.ansi().a(Ansi.Attribute.RESET).fg(Ansi.Color.YELLOW).bold().toString() + s + Ansi.ansi().reset().toString());
		}
	}

	public static void info(final String... msg){
		for(final String s : msg){
			log.info(s);
		}
	}

	public static void success(final String... msg){
		for(final String s : msg){
			log.info(Ansi.ansi().a(Ansi.Attribute.RESET).fg(Ansi.Color.GREEN).bold().toString() + s + Ansi.ansi().reset().toString());
		}
	}
}
