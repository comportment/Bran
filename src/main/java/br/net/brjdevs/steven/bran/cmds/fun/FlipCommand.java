package br.net.brjdevs.steven.bran.cmds.fun;

import br.net.brjdevs.steven.bran.core.command.Argument;
import br.net.brjdevs.steven.bran.core.command.Command;
import br.net.brjdevs.steven.bran.core.command.builders.CommandBuilder;
import br.net.brjdevs.steven.bran.core.command.enums.Category;
import br.net.brjdevs.steven.bran.core.command.interfaces.ICommand;
import net.dv8tion.jda.core.entities.*;

import java.util.ArrayList;
import java.util.List;

public class FlipCommand {
	
	private static String normal = "abcdefghijklmnopqrstuvwxyz_,;.?!/\\'ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
	private static String split = "ɐqɔpǝɟbɥıظʞןɯuodbɹsʇnʌʍxʎz‾'؛˙¿¡/\\,∀qϽᗡƎℲƃHIſʞ˥WNOԀὉᴚS⊥∩ΛMXʎZ0ƖᄅƐㄣϛ9ㄥ86";
	
	@Command
	private static ICommand flip() {
		return new CommandBuilder(Category.FUN)
				.setAliases("flip")
				.setName("Flip Command")
				.setDescription("Flips a text upside down!")
				.setArgs(new Argument("text", String.class))
				.setAction((event) -> {
					String[] text = {(String) event.getArgument("text").get()};
					getMentions(event.getMessage()).forEach(mentionable -> text[0] = text[0].replace(getAsMention(mentionable, event.getGuild()), mentionable instanceof User ? event.getGuild().getMember((User) mentionable).getEffectiveName() : mentionable instanceof TextChannel ? ((TextChannel) mentionable).getName() : ((Role) mentionable).getName()));
					text[0] += "╯）°□°╯(";
					event.sendMessage(flip(text[0])).queue();
				})
				.build();
	}
	
	private static List<IMentionable> getMentions(Message message) {
		List<IMentionable> mentions = new ArrayList<>();
		mentions.addAll(message.getMentionedUsers());
		mentions.addAll(message.getMentionedChannels());
		mentions.addAll(message.getMentionedRoles());
		return mentions;
	}
	
	private static String getAsMention(IMentionable mentionable, Guild guild) {
		if (!(mentionable instanceof User)) return mentionable.getAsMention();
		User user = (User) mentionable;
		Member member = guild.getMember(user);
		return member.getNickname() != null ? "<@!" + user.getId() + ">" : user.getAsMention();
	}
	
	private static String flip(String text) {
		StringBuilder out = new StringBuilder();
		for (int i = 0; i < text.length(); i++) {
			char c = text.charAt(i);
			int index = normal.indexOf(c);
			out.append(index != -1 ? split.charAt(index) : c);
		}
		return out.reverse().toString();
	}
}
