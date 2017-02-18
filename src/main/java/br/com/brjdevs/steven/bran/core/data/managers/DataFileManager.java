package br.com.brjdevs.steven.bran.core.data.managers;

import br.com.brjdevs.steven.bran.core.utils.IOUtils;
import net.dv8tion.jda.core.utils.SimpleLog;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class DataFileManager implements Supplier<List<String>> {
	
	private static final SimpleLog LOG = SimpleLog.getLog("DataFileManager");
	
	private List<String> data;
	private Path path;
	
	public DataFileManager(String path) {
		this.path = Paths.get(path);
		this.data = new ArrayList<>();
		try {
			if (!this.path.toFile().exists()) {
				LOG.info("Could not find config file at " + this.path.toFile().getAbsolutePath() + ", creating a new one...");
				if (this.path.toFile().createNewFile()) {
					LOG.info("Generated new config file at " + this.path.toFile().getAbsolutePath() + ".");
					IOUtils.write(this.path, this.data.stream().collect(Collectors.joining()));
					LOG.info("Please, fill the file with valid properties.");
				} else {
					LOG.warn("Could not create config file at " + path);
				}
			}
			
			Collections.addAll(this.data, IOUtils.read(this.path).split("\\r\\n|\\n|\\r"));
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
