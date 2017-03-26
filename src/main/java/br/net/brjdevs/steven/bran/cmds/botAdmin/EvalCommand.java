package br.net.brjdevs.steven.bran.cmds.botAdmin;

import br.net.brjdevs.steven.bran.core.client.Bran;
import br.net.brjdevs.steven.bran.core.command.Argument;
import br.net.brjdevs.steven.bran.core.command.Command;
import br.net.brjdevs.steven.bran.core.command.builders.CommandBuilder;
import br.net.brjdevs.steven.bran.core.command.builders.TreeCommandBuilder;
import br.net.brjdevs.steven.bran.core.command.enums.Category;
import br.net.brjdevs.steven.bran.core.command.interfaces.ICommand;
import br.net.brjdevs.steven.bran.core.managers.Permissions;
import org.apache.commons.io.IOUtils;
import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.io.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.Charset;
import java.rmi.UnexpectedException;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

public class EvalCommand /*implements ICommand*/ {
	
	//js eval
	private static ScriptEngine eval;
	private static File folder = new File("classes");
	private static File f; // Src
	private static File out;
	private static StringBuilder imports = new StringBuilder();
	
	static {
		eval = new ScriptEngineManager().getEngineByName("nashorn");
	}
	
	//java eval
	
	static {
		List<ClassLoader> classLoadersList = new LinkedList<>();
		classLoadersList.add(ClasspathHelper.contextClassLoader());
		classLoadersList.add(ClasspathHelper.staticClassLoader());
		Reflections reflections = new Reflections(new ConfigurationBuilder()
				.setScanners(new SubTypesScanner(false), new ResourcesScanner())
				.setUrls(ClasspathHelper.forClassLoader(classLoadersList.toArray(new ClassLoader[0])))
                .filterInputsBy(new FilterBuilder().include(FilterBuilder.prefix("br.net.brjdevs.steven.bran"))));
        Set<Class<?>> classes = reflections.getSubTypesOf(Object.class).stream().filter(clazz -> clazz != null && clazz.getCanonicalName() != null).collect(Collectors.toSet());
        classes.addAll(new Reflections("br.net.brjdevs.steven.bran").getSubTypesOf(Enum.class));
        classes.forEach(clazz -> imports.append("import ").append(clazz.getCanonicalName()).append(";\n"));
	}
	
	@Command
	private static ICommand eval() {
		folder.mkdirs();
		f = new File(folder + "/DontUse.java");
		out = new File(folder + "/DontUse.class");
		return new TreeCommandBuilder(Category.BOT_ADMINISTRATOR)
				.setAliases("eval")
				.setName("Eval Command")
				.setDescription("Evaluates in Java and Javascript")
				.setDefault("java")
				.setRequiredPermission(Permissions.EVAL)
				.addSubCommand(new CommandBuilder(Category.BOT_ADMINISTRATOR)
						.setAliases("js", "javascript")
						.setName("JS Eval Command")
						.setDescription("Evaluates in JavaScript")
						.setArgs(new Argument("js code", String.class))
						.setAction((event) -> {
							eval.put("shard", event.getShard());
							eval.put("bran", Bran.getInstance());
							eval.put("jda", event.getJDA());
							eval.put("event", event);
							eval.put("author", event.getAuthor());
							eval.put("self", event.getJDA().getSelfUser());
							String toEval = (String) event.getArgument("js code").get();
							Object out;
							try {
								eval.eval("imports = new JavaImporter(java.util, java.io, java.net)\n");
								out = eval.eval("(function() {with(imports) {" + toEval + "\n}})()");
							} catch (Exception e) {
								out = e;
							}
							if (out == null || out.toString().isEmpty())
								out = "Executed without error and no objects returned.";
							out = out.toString().replaceAll(event.getJDA().getToken(), "Bot ");
							String currentArgs = "";
							currentArgs += "Output: \n";
							currentArgs += "```" + out.toString() + "```";
							event.sendMessage(currentArgs).queue();
						})
						.build())
				.addSubCommand(new CommandBuilder(Category.BOT_ADMINISTRATOR)
						.setAliases("java")
						.setName("Java Eval Command")
						.setDescription("Evaluates in Java!")
						.setArgs(new Argument("java code", String.class))
						.setAction((event) -> {
							Object x = null;
							try {
								OutputStream stream = new BufferedOutputStream(new FileOutputStream(f));
								stream.write(getBodyWithLines((String) event.getArgument("java code").get()).getBytes());
								stream.close();
								try {
									FutureTask<?> task = new FutureTask<>(() -> compile(Bran.getInstance()));
									task.run();
									x = task.get(15, TimeUnit.SECONDS);
								} catch (TimeoutException e) {
									event.sendMessage("Compiling timed out.").queue();
									return;
								} catch (Exception e) {
									event.sendMessage(e.getMessage()).queue();
									return;
								}
								out.deleteOnExit();
								URLClassLoader urlClassLoader = new URLClassLoader(new URL[] {folder.toURI().toURL()}, EvalCommand.class.getClassLoader());
								Class clazz = urlClassLoader.loadClass(out.getName().replace(".class", ""));
								if (clazz == null) {
									event.sendMessage("Something went wrong trying to load the compiled class.").queue();
									return;
								}
								
								Object o = clazz.getConstructors()[0].newInstance(event);
								try {
									Object finalO = o;
									FutureTask<?> task = new FutureTask<>(() -> finalO.getClass().getMethod("run").invoke(finalO));
									task.run();
									o = task.get(2, TimeUnit.SECONDS);
								} catch (TimeoutException e) {
									event.sendMessage("Method timed out.").queue();
									return;
								} catch (Exception e) {
									o = ":anger: `" + e.getCause() + "`";
								}
								if (o == null || o.toString().isEmpty())
									o = "Executed without error and no objects returned!";
								o = o.toString().replaceAll(Bran.getInstance().getConfig().botToken, "<BOT TOKEN>");
								event.sendMessage(o.toString()).queue();
							} catch (Exception e) {
								if (x == null) x = e;
								event.sendMessage("Something went wrong trying to eval your query.\n" + x).queue();
							}
							if (f.exists() && !f.delete()) {
								event.sendMessage("Could not delete DontUse.java").queue();
							}
							if (out.exists() && !out.delete()) {
								event.sendMessage("Could not delete DontUse.class").queue();
							}
						})
						.build())
				.build();
	}
	
	private static String compile(Bran bran) throws Exception {
		if (!f.exists())
			throw new UnexpectedException("Unable to compile source file.");
		ProcessBuilder builder = new ProcessBuilder();
		builder.command("javac", "-cp", System.getProperty("java.class.path"), folder + "/" + f.getName());
		Process p = builder.start();
		
		Scanner sc = new Scanner(p.getInputStream());
		
		Scanner scErr = new Scanner(p.getErrorStream());
		
		StringWriter writer = new StringWriter();
		IOUtils.copy(p.getErrorStream(), writer, Charset.forName("UTF-8"));
		String x = writer.toString().replace(bran.workingDir.getPath(), "<ClassPath>").replace("\\classes", "");
		
		sc.close();
		scErr.close();
		
		p.waitFor();
		p.destroyForcibly();
		
		return x;
	}
	
	private static String getBodyWithLines(String code) {
		String body =
                "import java.util.*;\n" +
                        "import java.math.*;\n" +
                        "import java.net.*;\n" +
                        "import java.io.*;\n" +
                        "import java.util.concurrent.*;\n" +
                        "import java.util.*;\n" +
                        "import java.util.regex.*;\n" +
                        "import java.time.*;\n" +
                        "import java.lang.*;\n" +
                        "import java.util.stream.*;\n" +
                        "import net.dv8tion.jda.core.entities.*;\n" +
                        "import net.dv8tion.jda.core.*;\n" +
                        "import net.dv8tion.jda.core.managers.*;\n" +
                        "import net.dv8tion.jda.core.managers.fields.*;\n" +
		                imports.toString() +
		                "import com.google.gson.*;\n" +
                        "public class " + f.getName().replace(".java", "") + "\n{" +
                        "\n\tpublic Object run() throws Exception" +
                        "\n\t{\n\t\t";
        String[] lines = code.split("\n");
        body += String.join("\n\t\t", (CharSequence[]) lines);
        return body + (body.endsWith(";") ? "" : ";") + (!body.contains("return ") && !body.contains("throw ") ? ";return null;" : "") + "\n\t}"
                + "\n\n\tpublic void print(Object o) { System.out.print(o.toString()); }\n" +
                "\tpublic void println(Object o) { print(o.toString() + \"\\n\"); }\n" +
                "\tpublic void printErr(Object o) { System.err.print(o.toString()); }\n" +
                "\tpublic void printErrln(Object o) { printErr(o.toString() + \"\\n\"); }\n" +
                "\tprivate CommandEvent event;\n\n" +
                "\tpublic " + f.getName().replace(".java", "") + "(CommandEvent event)\n\t{\n" +
                "\t\tthis.event = event;\n\t}\n}";
    }
	
	/*@Override
	public void execute(CommandEvent event) {
		switch (((String) event.getArgument("lang").get())) {
			case "js":
			case "javascript":
				eval.put("shard", event.getShard());
				eval.put("container", Bran.getInstance());
				eval.put("jda", event.getJDA());
				eval.put("event", event);
				eval.put("author", event.getAuthor());
				eval.put("self", event.getJDA().getSelfUser());
				String toEval = (String) event.getArgument("js code").get();
				Object out;
				try {
					eval.eval("imports = new JavaImporter(java.util, java.io, java.net)\n");
					out = eval.eval("(function() {with(imports) {" + toEval + "\n}})()");
				} catch (Exception e) {
					out = e;
				}
				if (out == null || out.toString().isEmpty())
					out = "Executed without error and no objects returned.";
				out = out.toString().replaceAll(event.getJDA().getToken(), "Bot ");
				String currentArgs = "";
				currentArgs += "Output: \n";
				currentArgs += "```" + out.toString() + "```";
				event.sendMessage(currentArgs).queue();
				break;
			default:
				Object x = null;
				try {
					OutputStream stream = new BufferedOutputStream(new FileOutputStream(f));
					stream.write(getBodyWithLines((String) event.getArgument("java code").get()).getBytes());
					stream.close();
					try {
						FutureTask<?> task = new FutureTask<>(() -> compile(Bran.getInstance()));
						task.run();
						x = task.get(15, TimeUnit.SECONDS);
					} catch (TimeoutException e) {
						event.sendMessage("Compiling timed out.").queue();
						return;
					} catch (Exception e) {
						event.sendMessage(e.getMessage()).queue();
						return;
					}
					this.out.deleteOnExit();
					URLClassLoader urlClassLoader = new URLClassLoader(new URL[] {folder.toURI().toURL()}, EvalCommand.class.getClassLoader());
					Class clazz = urlClassLoader.loadClass(this.out.getName().replace(".class", ""));
					if (clazz == null) {
						event.sendMessage("Something went wrong trying to load the compiled class.").queue();
						return;
					}
					
					Object o = clazz.getConstructors()[0].newInstance(event);
					try {
						Object finalO = o;
						FutureTask<?> task = new FutureTask<>(() -> finalO.getClass().getMethod("run").invoke(finalO));
						task.run();
						o = task.get(2, TimeUnit.SECONDS);
					} catch (TimeoutException e) {
						event.sendMessage("Method timed out.").queue();
						return;
					} catch (Exception e) {
						o = ":anger: `" + e.getCause() + "`";
					}
					if (o == null || o.toString().isEmpty())
						o = "Executed without error and no objects returned!";
					o = o.toString().replaceAll(Bran.getInstance().getConfig().botToken, "<BOT TOKEN>");
					event.sendMessage(o.toString()).queue();
				} catch (Exception e) {
					if (x == null) x = e;
					event.sendMessage("Something went wrong trying to eval your query.\n" + x).queue();
				}
				if (f.exists() && !f.delete()) {
					event.sendMessage("Could not delete DontUse.java").queue();
				}
				if (this.out.exists() && !this.out.delete()) {
					event.sendMessage("Could not delete DontUse.class").queue();
				}
				break;
		}
	}
	
	@Override
	public String[] getAliases() {
		return new String[] {"eval"};
	}
	
	@Override
	public String getName() {
		return "Eval Command";
	}
	
	@Override
	public String getDescription() {
		return "Evaluates in java and javascript!";
	}
	
	@Override
	public Argument[] getArguments() {
		return new Argument[] {new Argument("lang", String.class), new Argument("code", String.class)};
	}
	
	@Override
	public long getRequiredPermission() {
		return Permissions.EVAL;
	}
	
	@Override
	public boolean isPrivateAvailable() {
		return true;
	}
	
	@Override
	public String getExample(String prefix) {
		return prefix + "eval js return \"This is JavaScript!\"\n" +
				prefix + "eval java return \"This is Java!\"";
	}
	
	@Override
	public Category getCategory() {
		return Category.BOT_ADMINISTRATOR;
	}*/
}
