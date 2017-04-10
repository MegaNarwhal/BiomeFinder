package us.blockbox.biomefinder;

import org.fusesource.jansi.Ansi;

import java.util.logging.Logger;

//Created 11/9/2016 3:12 AM
public class ColoredConsoleMessager implements ConsoleMessager{

	private final Logger logger;

	public ColoredConsoleMessager(Logger logger){
		this.logger = logger;
	}

	@Override
	public Logger getLogger(){
		return logger;
	}

	@Override
	public void warn(final String... msg){
		for(final String s : msg){
			logger.warning(Ansi.ansi().a(Ansi.Attribute.RESET).fg(Ansi.Color.YELLOW).bold().toString() + s + Ansi.ansi().reset().toString());
		}
	}

	@Override
	public void info(final String... msg){
		for(final String s : msg){
			logger.info(s);
		}
	}

	@Override
	public void success(final String... msg){
		for(final String s : msg){
			logger.info(Ansi.ansi().a(Ansi.Attribute.RESET).fg(Ansi.Color.GREEN).bold().toString() + s + Ansi.ansi().reset().toString());
		}
	}
}
