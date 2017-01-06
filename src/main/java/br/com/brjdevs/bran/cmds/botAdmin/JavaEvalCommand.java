package br.com.brjdevs.bran.cmds.botAdmin;

import br.com.brjdevs.bran.Bot;
import br.com.brjdevs.bran.core.RegisterCommand;
import br.com.brjdevs.bran.core.command.Category;
import br.com.brjdevs.bran.core.command.CommandBuilder;
import br.com.brjdevs.bran.core.command.CommandManager;
import br.com.brjdevs.bran.core.Permissions;
import br.com.brjdevs.bran.core.utils.StringUtils;

import java.io.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.rmi.UnexpectedException;
import java.util.Scanner;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@RegisterCommand
public class JavaEvalCommand {

    private File folder = new File("classes");
    private File f; // Src
    private File out;
    public JavaEvalCommand() {
        folder.mkdirs();
        f = new File(folder + "/DontUse.java");
        out = new File(folder + "/DontUse.class");
        CommandManager.addCommand(new CommandBuilder(Category.BOT_ADMINISTRATOR)
                .setAliases("java", "javaeval")
                .setArgs("[Java Code]")
                .setName("Java Eval Command")
                .setDescription("Evaluates in Java!")
                .setExample("eval return \"This is Java!\";")
                .setRequiredPermission(Permissions.EVAL)
                .setAction((event) -> {
                    try {
                        // Create Java src file and class
                        if (f.createNewFile()) f.deleteOnExit();
                        OutputStream stream = new BufferedOutputStream(new FileOutputStream(f));
                        stream.write(getBodyWithLines(StringUtils.splitArgs(event.getArgs(), 2)[1]).getBytes());
                        stream.close();
                        try {
                            FutureTask<?> task = new FutureTask<>(() ->
                            {
                                compile();
                                return null;
                            });
                            task.run();
                            task.get(15, TimeUnit.SECONDS);
                        } catch (TimeoutException e) {
                            event.sendMessage("Compiling timed out.").queue();
                            return;
                        } catch (Exception e) {// Compilation failed with error
                            event.sendMessage(e.getMessage()).queue();
                            return;
                        }
                        out.deleteOnExit();
                        URLClassLoader urlClassLoader = new URLClassLoader(new URL[] {folder.toURI().toURL()}, getClass().getClassLoader());
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
                        o = o.toString().replace(Bot.getInstance().getConfig().getToken(), "<BOT TOKEN>");
                        event.sendMessage(o.toString()).queue();
                    } catch (Exception e) {
                        event.sendMessage("Something went wrong trying to eval your query.\n" + e).queue();
                    }
                })
                .build());
    }

    private void compile() throws IOException, InterruptedException {
        if (!f.exists())
            throw new UnexpectedException("Unable to compile source file.");
        ProcessBuilder builder = new ProcessBuilder();
        builder.command("javac", "-cp", System.getProperty("java.class.path"), folder + "/" + f.getName());
        Process p = builder.start();

        Scanner sc = new Scanner(p.getInputStream());
        Scanner scErr = new Scanner(p.getErrorStream());

        sc.close();
        scErr.close();

        p.waitFor();
        p.destroyForcibly();
    }

    private String getBodyWithLines(String code) {
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
                        "import br.com.brjdevs.bran.core.command.CommandEvent;\n" +
                        "import java.util.stream.*;\n" +
                        "import br.com.brjdevs.bran.Bot;\n" +
                        "import net.dv8tion.jda.core.entities.*;\n" +
                        "import net.dv8tion.jda.core.*;\n" +
                        "import net.dv8tion.jda.core.managers.*;\n" +
                        "import net.dv8tion.jda.core.managers.fields.*;\n" +
                        "import br.com.brjdevs.bran.core.data.guild.configs.*;\n" +
                        "import br.com.brjdevs.bran.core.data.guild.*;\n" +
                        "import br.com.brjdevs.bran.core.data.guild.configs.profile.*;\n" +
                        "import br.com.brjdevs.bran.core.utils.*;\n" +
		                "import br.com.brjdevs.bran.core.data.guild.configs.*;\n" +
		                "import br.com.brjdevs.bran.core.data.guild.configs.impl.GuildMemberImpl.FakeGuildMemberImpl;\n" +
                        "import br.com.brjdevs.bran.core.audio.*;\n" +
                        "import br.com.brjdevs.bran.core.audio.utils.*;\n" +
                        "import br.com.brjdevs.bran.core.poll.*;\n" +
                        "import br.com.brjdevs.bran.core.*;\n" +
                        "import br.com.brjdevs.bran.core.command.*;\n" +
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
