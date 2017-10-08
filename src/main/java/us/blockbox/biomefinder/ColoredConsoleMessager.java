package us.blockbox.biomefinder;

import org.fusesource.jansi.Ansi;

import java.util.logging.Logger;

public class ColoredConsoleMessager implements ConsoleMessager{
	private static final String COLOR_WARN;
	private static final String COLOR_SUCCESS;
	private static final String COLOR_RESET;
	private final Logger logger;

	public ColoredConsoleMessager(Logger logger){
		this.logger = logger;
	}

	static{
		boolean ansiAvailable = false;
		try{
			Class.forName("org.fusesource.jansi.Ansi");
			ansiAvailable = true;
		}catch(ClassNotFoundException e){
			//
		}
		if(ansiAvailable){
			COLOR_WARN = Ansi.ansi().a(Ansi.Attribute.RESET).fg(Ansi.Color.YELLOW).bold().toString();
			COLOR_SUCCESS = Ansi.ansi().a(Ansi.Attribute.RESET).fg(Ansi.Color.GREEN).bold().toString();
			COLOR_RESET = Ansi.ansi().reset().toString();
		}else{
			final String empty = "";
			COLOR_WARN = empty;
			COLOR_SUCCESS = empty;
			COLOR_RESET = empty;
		}
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
