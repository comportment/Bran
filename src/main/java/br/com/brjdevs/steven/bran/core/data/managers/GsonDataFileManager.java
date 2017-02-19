package br.com.brjdevs.steven.bran.core.data.managers;

import br.com.brjdevs.steven.bran.core.utils.IOUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.dv8tion.jda.core.utils.SimpleLog;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Supplier;

public class GsonDataFileManager<T> implements Supplier<T> {
	
	private static final Gson GSON = new GsonBuilder().serializeNulls().setPrettyPrinting().create();
	private static final SimpleLog LOG = SimpleLog.getLog("GsonDataFileManager");
	
	private Path path;
	private T data;
	
	public GsonDataFileManager(Class<T> clazz, String path, Supplier<T> constructor) {
		this.path = Paths.get(System.getProperty("user.dir") + File.separator + "data" + File.separator + path);
		try {
			if (!this.path.getParent().toFile().exists()) {
				LOG.info("Could not find parent file, creating a new one...");
				if (this.path.getParent().toFile().mkdirs()) {
					LOG.info("Generated new parent!");
				} else {
					LOG.info("Failed to generate parent!");
					System.exit(0);
				}
			}
			if (!this.path.toFile().exists()) {
				LOG.info("Could not find config file at " + this.path.toFile().getAbsolutePath() + ", creating a new one...");
				if (this.path.toFile().createNewFile()) {
					LOG.info("Generated new config file at " + this.path.toFile().getAbsolutePath() + ".");
					IOUtils.write(this.path, GSON.toJson(constructor.get()));
					LOG.info("Please, fill the file with valid properties.");
				} else {
					LOG.warn("Could not create config file at " + path);
				}
				System.exit(0);
			}
			
			this.data = GSON.fromJson(IOUtils.read(this.path), clazz);
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public T get() {
		return data;
	}
	
	public void update() {
		try {
			IOUtils.write(path, GSON.toJson(data));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
