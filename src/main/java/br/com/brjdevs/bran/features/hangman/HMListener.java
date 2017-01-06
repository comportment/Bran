package br.com.brjdevs.bran.features.hangman;

import br.com.brjdevs.bran.Bot;
import br.com.brjdevs.bran.core.data.guild.DiscordGuild;
import br.com.brjdevs.bran.core.data.guild.configs.profile.Profile;
import br.com.brjdevs.bran.core.data.guild.configs.profile.Profile.Action;
import br.com.brjdevs.bran.features.hangman.HMSession.Status;
import br.com.brjdevs.bran.features.hangman.exceptions.AlreadyGuessed;
import br.com.brjdevs.bran.features.hangman.exceptions.InvalidGuess;
import br.com.brjdevs.bran.features.hangman.exceptions.Loose;
import br.com.brjdevs.bran.features.hangman.exceptions.Win;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.Event;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.hooks.EventListener;

public class HMListener implements EventListener {
	@Override
	public void onEvent(Event e) {
		if (!(e instanceof GuildMessageReceivedEvent)) return;
		GuildMessageReceivedEvent event = (GuildMessageReceivedEvent) e;
		DiscordGuild discordGuild = DiscordGuild.getInstance(event.getGuild());
		Profile profile = discordGuild.getMember(event.getAuthor()).getProfile();
		HMSession session = HMSession.getSession(profile);
		if (session == null) return;
		String msg = event.getMessage().getRawContent();
		TextChannel channel = event.getChannel();
		if ("giveup".equals(msg)) {
			if (session.getCreator() != profile) {
				session.remove(profile);
				profile.getHMStats().defeats++;
				channel.sendMessage(session.createEmbed(Status.GIVEUP, event.getJDA())).queue();
			} else if (session.getCreator() == profile && !session.getInvitedUsers().isEmpty()){
				channel.sendMessage("You can't give up this session because there are other players playing with you, you can just let them finish if you don't want to continue or use `" + Bot.getInstance().getDefaultPrefixes()[0] + "hm pass [MENTION]` to set a new owner to the session.").queue();
			} else {
				session.end();
				channel.sendMessage(new EmbedBuilder(session.createEmbed(Status.LOOSE, event.getJDA())).setDescription("Aww man, I know you could've done it! The word was '" + session.getWord().getWord() + "'").build()).queue();
			}
			return;
		}
		if (session.getChannel() != event.getChannel()) return;
		if ("info".equals(msg)) {
			channel.sendMessage(session.createEmbed(Status.INFO, event.getJDA())).queue();
			return;
		}
		if (msg.length() > 1 || msg.charAt(0) == '_') return;
		try {
			session.guess(msg);
			channel.sendMessage(session.createEmbed(Status.GUESS_R, event.getJDA())).queue();
			session.getProfiles().forEach(p -> {
				p.addCoins(2);
				Action action = p.addExperience(1);
				if (action == Action.LEVEL_UP)
					channel.sendMessage(new EmbedBuilder(p.createEmbed(event.getJDA())).setDescription("**You leveled UP!**").build()).queue();
			});
		} catch (AlreadyGuessed guessed) {
			channel.sendMessage(session.createEmbed(Status.GUESSED, event.getJDA())).queue();
		} catch (Loose loose) {
			channel.sendMessage(session.createEmbed(Status.LOOSE, event.getJDA())).queue();
			session.end();
			session.getProfiles().forEach(p -> {
				p.addCoins(-2);
				p.getHMStats().defeats++;
				Action action = p.addExperience(-3);
				if (action == Action.LEVEL_DOWN)
					channel.sendMessage(new EmbedBuilder(p.createEmbed(event.getJDA())).setDescription("**You leveled DOWN!**").build()).queue();
			});
		} catch (InvalidGuess invalidGuess) {
			channel.sendMessage(session.createEmbed(Status.GUESS_W, event.getJDA())).queue();
		} catch (Win win) {
			channel.sendMessage(session.createEmbed(Status.WIN, event.getJDA())).queue();
			session.end();
			session.getProfiles().forEach(p -> {
				p.addCoins(2);
				p.getHMStats().victory++;
				Action action = p.addExperience(4);
				if (action == Action.LEVEL_UP)
					channel.sendMessage(new EmbedBuilder(p.createEmbed(event.getJDA())).setDescription("**You leveled UP!**").build()).queue();
			});
		}
	}
}
