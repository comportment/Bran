package br.com.brjdevs.steven.bran.cmds.fun;

import br.com.brjdevs.steven.bran.core.command.Category;
import br.com.brjdevs.steven.bran.core.command.Command;
import br.com.brjdevs.steven.bran.core.command.CommandBuilder;
import br.com.brjdevs.steven.bran.core.command.ICommand;
import br.com.brjdevs.steven.bran.core.utils.HttpUtils;
import br.com.brjdevs.steven.bran.core.utils.StringUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.dv8tion.jda.core.EmbedBuilder;

import java.awt.*;

public class PokemonCommand {
	
	@Command
	public static ICommand pokedex() {
		return new CommandBuilder(Category.FUN)
				.setAliases("pokedex")
				.setName("Pokedex Command")
				.setDescription("Gives you information on a Pokemon!")
				.setArgs("[pokemon id/pokemon name]")
				.setAction((event) -> {
					String pokemon = event.getArgs(2)[1];
					String content;
					if (pokemon.isEmpty()) {
						event.sendMessage("You have to tell me a Pokemon name or ID!").queue();
						return;
					}
					pokemon = String.format("http://pokeapi.co/api/v2/pokemon/%s/", pokemon);
					try {
						content = HttpUtils.read(pokemon);
					} catch (Exception e) {
						event.sendMessage("Failed to connect! Please try again in a few minutes...").queue();
						return;
					}
					JsonObject item = new JsonParser().parse(content).getAsJsonObject();
					JsonElement detail = item.get("detail");
					if (detail != null) {
						event.sendMessage("Could not find Pokemon matching that criteria.").queue();
						return;
					}
					StringBuilder stringBuilder = new StringBuilder();
					String form;
					try {
						form = new JsonParser().parse(HttpUtils.read(item.get("forms").getAsJsonArray().get(0).getAsJsonObject().get("url").getAsString())).getAsJsonObject().get("sprites").getAsJsonObject().get("front_default").getAsString();
					} catch (Exception e) {
						form = "Could not gather Pokemon Sprite!";
					}
					JsonArray abilities = item.get("abilities").getAsJsonArray();
					JsonArray stats = item.get("stats").getAsJsonArray();
					EmbedBuilder embedBuilder = new EmbedBuilder();
					stringBuilder.append("**Name:** ").append(StringUtils.neat(
							item.get("forms")
									.getAsJsonArray()
									.get(0).getAsJsonObject()
									.get("name")
							.getAsString()));
					stringBuilder.append("\n\n");
					stringBuilder.append("**Abilities:**\n");
					for (int i = 0; i < abilities.size(); i++) {
						JsonObject ability = abilities.get(i).getAsJsonObject();
						stringBuilder.append("      **")
								.append(StringUtils.neat(ability.get("ability")
										.getAsJsonObject().get("name").getAsString()).replaceAll("-", " "));
						stringBuilder.append("**\n        ").append("**Is Hidden:** ").append(ability.get("is_hidden").getAsBoolean()).append("\n\n");
					}
					stringBuilder.append("**Stats:**\n");
					for (int i = 0; i < stats.size(); i++) {
						JsonObject stat = stats.get(i).getAsJsonObject();
						String name = stat.get("stat").getAsJsonObject().get("name").getAsString();
						name = StringUtils.neat(name.replaceAll("-", " "));
						int base = stat.get("base_stat").getAsInt();
						stringBuilder.append("      **").append(name).append("**: ").append(base).append("\n");
					}
					try {
						embedBuilder.setThumbnail(form);
					} catch (IllegalArgumentException e) {
						stringBuilder.append("\nCould not find any Pokemon matching that criteria.");
					}
					embedBuilder.setDescription(stringBuilder.toString());
					Color color = event.getOriginGuild().getSelfMember().getColor() == null ? Color.decode("#ECB811") : event.getOriginGuild().getSelfMember().getColor();
					embedBuilder.setColor(color);
					event.sendMessage(embedBuilder.build()).queue();
				})
				.build();
	}
}
