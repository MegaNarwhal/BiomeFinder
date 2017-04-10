package us.blockbox.biomefinder;

public interface ConsoleMessager{
	void warn(String... msg);

	void info(String... msg);

	void success(String... msg);
}
