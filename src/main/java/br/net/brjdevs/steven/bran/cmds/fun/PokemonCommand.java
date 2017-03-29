package br.net.brjdevs.steven.bran.cmds.fun;

import br.net.brjdevs.steven.bran.core.command.Argument;
import br.net.brjdevs.steven.bran.core.command.Command;
import br.net.brjdevs.steven.bran.core.command.builders.CommandBuilder;
import br.net.brjdevs.steven.bran.core.command.enums.Category;
import br.net.brjdevs.steven.bran.core.command.interfaces.ICommand;
import br.net.brjdevs.steven.bran.core.quote.Quotes;
import br.net.brjdevs.steven.bran.core.utils.HttpUtils;
import br.net.brjdevs.steven.bran.core.utils.StringUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.dv8tion.jda.core.EmbedBuilder;

import java.awt.*;
import java.net.URLEncoder;

public class PokemonCommand {
	
	@Command
	private static ICommand pokedex() {
		return new CommandBuilder(Category.FUN)
				.setAliases("pokedex")
				.setName("Pokedex Command")
				.setDescription("Gives you information on a Pokemon!")
				.setArgs(new Argument("id/name", String.class))
				.setAction((event) -> {
					try {
						String pokemon = (String) event.getArgument("id/name").get(), content;
                        pokemon = String.format("http://pokeapi.co/api/v2/pokemon/%s/", URLEncoder.encode(pokemon.toLowerCase(), "UTF-8"));
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
                            form = "Could not grab Pokemon Sprite!";
                        }
						JsonArray abilities = item.get("abilities").getAsJsonArray();
						JsonArray stats = item.get("stats").getAsJsonArray();
						EmbedBuilder embedBuilder = new EmbedBuilder();
						stringBuilder.append("**Name:** ").append(StringUtils.capitalize(
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
									.append(StringUtils.capitalize(ability.get("ability")
											.getAsJsonObject().get("name").getAsString()).replaceAll("-", " "));
							stringBuilder.append("**\n        ").append("**Is Hidden:** ").append(ability.get("is_hidden").getAsBoolean()).append("\n\n");
						}
						stringBuilder.append("**Stats:**\n");
						for (int i = 0; i < stats.size(); i++) {
							JsonObject stat = stats.get(i).getAsJsonObject();
							String name = stat.get("stat").getAsJsonObject().get("name").getAsString();
							name = StringUtils.capitalize(name.replaceAll("-", " "));
							int base = stat.get("base_stat").getAsInt();
							stringBuilder.append("      **").append(name).append("**: ").append(base).append("\n");
						}
						try {
							embedBuilder.setThumbnail(form);
						} catch (IllegalArgumentException e) {
							stringBuilder.append("\nCould not find any Pokemon matching that criteria.");
						}
						embedBuilder.setDescription(stringBuilder.toString());
						Color color = event.getGuild().getSelfMember().getColor() == null ? Color.decode("#ECB811") : event.getGuild().getSelfMember().getColor();
						embedBuilder.setColor(color);
						event.sendMessage(embedBuilder.build()).queue();
					} catch (Exception e) {
						event.sendMessage(Quotes.FAIL, "Sorry, something went wrong while looking for that pokemon!").queue();
					}
				})
				.build();
	}
}
