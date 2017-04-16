package br.net.brjdevs.steven.bran.core.data.managers;

import br.net.brjdevs.steven.bran.core.utils.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class DataFileManager implements Supplier<List<String>> {
	
	private static final Logger LOGGER = LoggerFactory.getLogger("DataFileManager");
	
	private List<String> data;
	private Path path;
	
	public DataFileManager(String path) {
		this.path = Paths.get(path);
		this.data = new ArrayList<>();
		try {
			if (!this.path.toFile().exists()) {
				LOGGER.info("Could not find config file at " + this.path.toFile().getAbsolutePath() + ", creating a new one...");
				if (this.path.toFile().createNewFile()) {
					LOGGER.info("Generated new config file at " + this.path.toFile().getAbsolutePath() + ".");
				} else {
					LOGGER.warn("Could not create config file at " + path);
				}
			} else {
				Collections.addAll(this.data, IOUtils.read(this.path).split("\\r\\n|\\n|\\r"));
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public List<String> get() {
		return data;
	}
	
	public void update() {
		try {
			IOUtils.write(path, this.data.stream().collect(Collectors.joining()));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
