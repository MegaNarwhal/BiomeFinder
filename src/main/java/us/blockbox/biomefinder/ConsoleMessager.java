package us.blockbox.biomefinder;

import java.util.logging.Logger;

public interface ConsoleMessager{
	Logger getLogger();

	void warn(String... msg);

	void info(String... msg);

	void success(String... msg);
}
