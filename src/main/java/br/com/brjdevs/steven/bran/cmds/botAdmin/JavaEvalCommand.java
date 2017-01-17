package br.com.brjdevs.steven.bran.cmds.botAdmin;

import br.com.brjdevs.steven.bran.Bot;
import br.com.brjdevs.steven.bran.core.command.Argument;
import br.com.brjdevs.steven.bran.core.command.Command;
import br.com.brjdevs.steven.bran.core.command.builders.CommandBuilder;
import br.com.brjdevs.steven.bran.core.command.enums.Category;
import br.com.brjdevs.steven.bran.core.command.interfaces.ICommand;
import br.com.brjdevs.steven.bran.core.managers.Permissions;
import org.apache.commons.io.IOUtils;
import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;

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

public class JavaEvalCommand {
	
	private static File folder = new File("classes");
	private static File f; // Src
	private static File out;
	private static StringBuilder imports = new StringBuilder();
	
	static {
		List<ClassLoader> classLoadersList = new LinkedList<>();
		classLoadersList.add(ClasspathHelper.contextClassLoader());
		classLoadersList.add(ClasspathHelper.staticClassLoader());
		Reflections reflections = new Reflections(new ConfigurationBuilder()
				.setScanners(new SubTypesScanner(false), new ResourcesScanner())
				.setUrls(ClasspathHelper.forClassLoader(classLoadersList.toArray(new ClassLoader[0])))
				.filterInputsBy(new FilterBuilder().include(FilterBuilder.prefix("br.com.brjdevs.steven.bran"))));
		Set<Class<?>> classes = reflections.getSubTypesOf(Object.class).stream().filter(clazz -> clazz != null && clazz.getCanonicalName() != null).collect(Collectors.toSet());
		classes.addAll(new Reflections("br.com.brjdevs.steven.bran").getSubTypesOf(Enum.class));
		classes.forEach(clazz -> imports.append("import ").append(clazz.getCanonicalName()).append(";\n"));
	}
	
	@Command
	private static ICommand javaEval() {
		folder.mkdirs();
        f = new File(folder + "/DontUse.java");
        out = new File(folder + "/DontUse.class");
		return new CommandBuilder(Category.BOT_ADMINISTRATOR)
				.setAliases("java", "javaeval")
				.setArgs(new Argument<>("java code", String.class))
				.setName("Java Eval Command")
                .setDescription("Evaluates in Java!")
                .setExample("eval return \"This is Java!\";")
                .setRequiredPermission(Permissions.EVAL)
                .setAction((event) -> {
	                Object x = null;
	                try {
                        OutputStream stream = new BufferedOutputStream(new FileOutputStream(f));
		                stream.write(getBodyWithLines((String) event.getArgument("java code").get()).getBytes());
		                stream.close();
                        try {
	                        FutureTask<?> task = new FutureTask<>(JavaEvalCommand::compile);
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
		                URLClassLoader urlClassLoader = new URLClassLoader(new URL[] {folder.toURI().toURL()}, JavaEvalCommand.class.getClassLoader());
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
		                o = o.toString().replace(Bot.getConfig().getToken(), "<BOT TOKEN>");
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
				.build();
	}
	
	private static String compile() throws Exception {
		if (!f.exists())
			throw new UnexpectedException("Unable to compile source file.");
		ProcessBuilder builder = new ProcessBuilder();
		builder.command("javac", "-cp", System.getProperty("java.class.path"), folder + "/" + f.getName());
		Process p = builder.start();
		
		Scanner sc = new Scanner(p.getInputStream());
		
		Scanner scErr = new Scanner(p.getErrorStream());
		
		StringWriter writer = new StringWriter();
		IOUtils.copy(p.getErrorStream(), writer, Charset.forName("UTF-8"));
		String x = writer.toString().replace(new File(Bot.getWorkingDirectory()).getPath(), "<ClassPath>").replace("\\classes", "");
		
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

}
