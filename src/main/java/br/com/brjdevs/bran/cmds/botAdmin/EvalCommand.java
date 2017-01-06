package br.com.brjdevs.bran.cmds.botAdmin;

import br.com.brjdevs.bran.Bot;
import br.com.brjdevs.bran.core.Permissions;
import br.com.brjdevs.bran.core.command.Category;
import br.com.brjdevs.bran.core.command.CommandBuilder;
import br.com.brjdevs.bran.core.command.CommandManager;
import br.com.brjdevs.bran.core.command.RegisterCommand;
import br.com.brjdevs.bran.core.messageBuilder.AdvancedMessageBuilder;
import br.com.brjdevs.bran.core.messageBuilder.AdvancedMessageBuilder.Quote;
import br.com.brjdevs.bran.core.utils.StringUtils;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

@RegisterCommand
public class EvalCommand {
    private static ScriptEngine eval;
    public EvalCommand() {
        eval = new ScriptEngineManager().getEngineByName("nashorn");
        eval.put("cfg", Bot.getInstance().getConfig());
        eval.put("owner", Bot.getInstance().getOwner());
        eval.put("instance", Bot.getInstance());
        register();
    }
    private static void register() {
        CommandManager.addCommand(new CommandBuilder(Category.BOT_ADMINISTRATOR)
                .setAliases("eval")
                .setName("JavaScript Eval Command")
                .setDescription("Evaluates in JavaScript.")
                .setExample("eval return \"This is JavaScript!\"")
                .setArgs("[JS code]")
                .setRequiredPermission(Permissions.EVAL)
                .setAction((event, args) -> {
                    AdvancedMessageBuilder builder = new AdvancedMessageBuilder();
                    if (StringUtils.splitSimple(args).length < 2) {
                        event.sendMessage(
                                Quote.getQuote(Quote.FAIL) +
                                        event.getOriginMember().getEffectiveName() + ", you gave me insufficient arguments, please use `" + event.getPrefix() + "eval [JS_CODE]` instead of `" + event.getMessage().getRawContent() + "`"
                        ).queue();
                        return;
                    }
                    eval.put("jda", event.getJDA());
                    eval.put("event", event);
                    eval.put("args", args);
                    eval.put("author", event.getAuthor());
                    eval.put("self", Bot.getInstance().getSelfUser(event.getJDA()));
                    String toEval = StringUtils.splitArgs(args, 2)[1];
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
                    builder.append("Output: \n");
                    builder.append("```" + out.toString() + "```");
                    event.sendMessage(builder.build()).queue();
                })
                .build());
    }
}
