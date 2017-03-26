package br.net.brjdevs.steven.bran.core.command;

import br.net.brjdevs.steven.bran.core.command.enums.Category;
import br.net.brjdevs.steven.bran.core.command.interfaces.ICommand;
import br.net.brjdevs.steven.bran.core.command.interfaces.ITreeCommand;
import br.net.brjdevs.steven.bran.core.utils.Utils;
import net.dv8tion.jda.core.utils.SimpleLog;
import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class CommandManager {
	
	private final List<ICommand> commands = new ArrayList<>();
	private final SimpleLog LOG = SimpleLog.getLog("Command Manager");
	
	public CommandManager() {
		load();
	}
	
	public void addCommand(ICommand command) {
		if (command != null)
            commands.add(command);
    }
	
	public List<ICommand> getCommands() {
		return commands;
    }
	
	public List<ICommand> getCommands(Category category) {
		return getCommands().stream().filter(cmd -> cmd.getCategory() == category).collect(Collectors.toList());
    }
	
	public ICommand getCommand(ITreeCommand tree, String alias) {
		return tree.getSubCommands().stream().filter(sub -> Arrays.stream(sub.getAliases()).anyMatch(s -> s.equals(alias))).findFirst().orElse(null);
	}
	
	public ICommand getCommand(String alias) {
		return getCommands().stream().filter(cmd -> Arrays.stream(cmd.getAliases()).anyMatch(s -> s.equals(alias))).findFirst().orElse(null);
	}
	
	private void load() {
	    String url = "br.com.brjdevs.steven.bran.cmds";
	    Reflections reflections = new Reflections(new ConfigurationBuilder().setUrls(ClasspathHelper.forPackage(url)).setScanners(new SubTypesScanner(),
			    new TypeAnnotationsScanner(), new MethodAnnotationsScanner()).filterInputsBy(new FilterBuilder().includePackage(url)));
	    Set<Method> commands = reflections.getMethodsAnnotatedWith(Command.class);
	    commands.forEach(method -> {
		    Class clazz = method.getDeclaringClass();
		    method.setAccessible(true);
		    if (!method.getReturnType().equals(ICommand.class)) {
			    LOG.fatal("Method annotated with Command.class returns " + method.getReturnType().getSimpleName() + " instead of ICommand.class.");
			    return;
		    }
	    	try {
			    ICommand command = (ICommand) method.invoke(null);
			    if (command.getAliases().length == 0) {
				    LOG.fatal("Attempted to register ICommand without aliases. (" + clazz.getSimpleName() + ")");
				    return;
			    }
                if (Utils.isEmpty(command.getDescription())) {
                    LOG.fatal("Attempted to register ICommand without description. (" + clazz.getSimpleName() + ")");
				    return;
			    }
                if (Utils.isEmpty(command.getName())) {
                    LOG.fatal("Attempted to register ICommand without name. (" + clazz.getSimpleName() + ")");
				    return;
			    }
			    if (command instanceof ITreeCommand && ((ITreeCommand) command).getSubCommands() != null && ((ITreeCommand) command).getSubCommands().isEmpty()) {
				    LOG.fatal("Attempted to register Tree ICommand without SubCommands. (" + clazz.getSimpleName() + ")");
				    return;
			    }
			    if (command.getCategory() == Category.UNKNOWN) {
				    LOG.fatal("Registered ICommand with UNKNOWN Category. (" + clazz.getSimpleName() + ")");
			    }
			    HelpContainer.generateHelp(command);
			    addCommand(command);
		    } catch (Exception e) {
	    		LOG.log(e);
		    }
		    method.setAccessible(false);
	    });
		LOG.info("Finished loading all Commands.");
	}
}