package br.net.brjdevs.steven.bran.core.data.managers;

import br.net.brjdevs.steven.bran.core.utils.IOUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Supplier;

public class GsonDataFileManager<T> implements Supplier<T> {
	
	private static final Gson GSON = new GsonBuilder().serializeNulls().setPrettyPrinting().create();
	private static final Logger LOGGER = LoggerFactory.getLogger("GsonDataFileManager");
	
	private Path path;
	private T data;
	
	public GsonDataFileManager(Class<T> clazz, String path, Supplier<T> constructor) {
		this.path = Paths.get(path);
        try {
            if (!this.path.toFile().exists()) {
				LOGGER.info("Could not find config file at " + this.path.toFile().getAbsolutePath() + ", creating a new one...");
				if (this.path.toFile().createNewFile()) {
					LOGGER.info("Generated new config file at " + this.path.toFile().getAbsolutePath() + ".");
					IOUtils.write(this.path, GSON.toJson(constructor.get()));
					LOGGER.info("Please, fill the file with valid properties.");
				} else {
					LOGGER.warn("Could not create config file at " + path);
				}
			} else {
				this.data = GSON.fromJson(IOUtils.read(this.path), clazz);
			}
        } catch (IOException e) {
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
