package us.blockbox.biomefinder;

import org.fusesource.jansi.Ansi;

import java.util.logging.Logger;

public class ColoredConsoleMessager implements ConsoleMessager{

	private static final String COLOR_WARN = Ansi.ansi().a(Ansi.Attribute.RESET).fg(Ansi.Color.YELLOW).bold().toString();
	private static final String COLOR_SUCCESS = Ansi.ansi().a(Ansi.Attribute.RESET).fg(Ansi.Color.GREEN).bold().toString();
	private static final String COLOR_RESET = Ansi.ansi().reset().toString();
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
			logger.warning(COLOR_WARN + s + COLOR_RESET);
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
			logger.info(COLOR_SUCCESS + s + COLOR_RESET);
		}
	}
}
