package br.com.brjdevs.steven.bran.cmds.info;

import br.com.brjdevs.steven.bran.core.command.Argument;
import br.com.brjdevs.steven.bran.core.command.Command;
import br.com.brjdevs.steven.bran.core.command.builders.CommandBuilder;
import br.com.brjdevs.steven.bran.core.command.enums.Category;
import br.com.brjdevs.steven.bran.core.command.interfaces.ICommand;
import br.com.brjdevs.steven.bran.core.managers.WeatherSearch;
import br.com.brjdevs.steven.bran.core.utils.Utils;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.Permission;

import java.awt.*;

public class WeatherCommand {
	
	@Command
	private static ICommand weather() {
		return new CommandBuilder(Category.INFORMATIVE)
				.setAliases("weather")
				.setName("Weather Command")
				.setDescription("Gives you weather information on a place.")
				.setArgs(new Argument("query", String.class))
				.setAction((event) -> {
					if (!event.getSelfMember().hasPermission(event.getTextChannel(), Permission.MESSAGE_EMBED_LINKS)) {
						event.sendMessage("I need to have MESSAGE_EMBED_LINKS permission to send this message!").queue();
						return;
					}
					String query = (String) event.getArgument("query").get();
					JsonElement element;
					try {
						element = WeatherSearch.search(query);
					} catch (RuntimeException e) {
						if (e.getMessage().equals("Yahoo API didn't respond."))
							event.sendMessage("The API took too long to respond, maybe it'currentArgs offline?").queue();
						else
							event.sendMessage("Could not connect, try again later please.").queue();
						return;
					}
					try {
						if (element.isJsonNull()) {
							event.sendMessage("Nothing found by `" + query + "`.").queue();
							return;
						}
						JsonObject result = element.getAsJsonObject();
						if (result == null) {
							event.sendMessage("Nothing found by `" + query + "`").queue();
							return;
						}
						result = result.get("channel").getAsJsonObject();
						//embed title
						JsonObject locationObject = result
								.get("location")
								.getAsJsonObject();
						String location = locationObject.get("city").getAsString() + " " + locationObject.get("region").getAsString() + ", " + locationObject.get("country").getAsString();
						//wind speed field
						JsonObject windObject = result.get("wind").getAsJsonObject();
						String windSpeed = windObject.get("speed").getAsString() + " mph";
						String windDirecton = windObject.get("direction").getAsString();
						String windChill = windObject.get("chill").getAsString();
						//atmosphere field
						JsonObject atmosphereObject = result.get("atmosphere").getAsJsonObject();
						String humidity = atmosphereObject.get("humidity").getAsString() + "%";
						String pressure = atmosphereObject.get("pressure").getAsString() + " in";
						//temp stuff
						JsonObject item = result.get("item").getAsJsonObject();
						JsonObject conditionObject = item.get("condition").getAsJsonObject();
						String lastUpdate = conditionObject.get("date").getAsString();
						double f = Double.parseDouble(conditionObject.get("temp").getAsString());
						String c = Utils.DECIMAL_FORMAT.format((5. / 9.) * (f - 32.));
						String temp = Utils.DECIMAL_FORMAT.format(f) + "ºF/" + c + "ºC";
						String text = conditionObject.get("text").getAsString();
						
						EmbedBuilder embedBuilder = new EmbedBuilder();
						embedBuilder.setTitle("Weather for " + location, null);
						embedBuilder.addField("Last updated at", lastUpdate, false);
						embedBuilder.addField("Wind", "**Speed:** " + windSpeed + "     **Direction:** " + windDirecton + "     **Chill:** " + windChill + "\n", false);
						embedBuilder.addField("Atmosphere", "**Humidity**: " + humidity + "     **Pressure:** " + pressure + "\n", false);
						embedBuilder.addField("Weather", "**Temperature:** " + temp + "     **Description:** " + text, false);
						if (!event.isPrivate())
							embedBuilder.setColor(event.getSelfMember().getColor() == null ? Color.decode("#F1AC1A") : event.getSelfMember().getColor());
						event.sendMessage(embedBuilder.build()).queue();
					} catch (Exception e) {
						event.sendMessage("There was a parsing error while building the info. `" + e.getMessage() + "`").queue();
						e.printStackTrace();
					}
				})
				.build();
	}
}
