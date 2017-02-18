package br.com.brjdevs.steven.bran.cmds.botAdmin;

import br.com.brjdevs.steven.bran.core.command.Argument;
import br.com.brjdevs.steven.bran.core.command.Command;
import br.com.brjdevs.steven.bran.core.command.builders.CommandBuilder;
import br.com.brjdevs.steven.bran.core.command.enums.Category;
import br.com.brjdevs.steven.bran.core.command.interfaces.ICommand;
import br.com.brjdevs.steven.bran.core.managers.Permissions;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

public class EvalCommand {
    private static ScriptEngine eval;
	
	static {
		eval = new ScriptEngineManager().getEngineByName("nashorn");
	}
	
	@Command
	private static ICommand eval() {
		return new CommandBuilder(Category.BOT_ADMINISTRATOR)
				.setAliases("eval")
                .setName("JavaScript Eval Command")
                .setDescription("Evaluates in JavaScript.")
                .setExample("eval return \"This is JavaScript!\"")
                .setRequiredPermission(Permissions.EVAL)
				.setArgs(new Argument<>("js code", String.class))
				.setAction((event, args) -> {
					eval.put("shard", event.getShard());
					eval.put("container", event.getClient());
					eval.put("jda", event.getJDA());
                    eval.put("event", event);
                    eval.put("args", args);
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
	                String s = "";
	                s += "Output: \n";
	                s += "```" + out.toString() + "```";
	                event.sendMessage(s).queue();
                })
				.build();
	}
}
