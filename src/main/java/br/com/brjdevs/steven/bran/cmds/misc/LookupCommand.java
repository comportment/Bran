package br.com.brjdevs.steven.bran.cmds.misc;

import br.com.brjdevs.steven.bran.core.command.*;
import br.com.brjdevs.steven.bran.core.utils.HttpUtils;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.dv8tion.jda.core.EmbedBuilder;

import java.awt.*;
import java.io.IOException;

public class LookupCommand {
	private static final String LOOKUP_URL = "http://ip-api.com/json/%s";
	private static final String MAP_URL = "https://www.google.com/maps/@%f,%f,15z";
	
	@Command
	private static ICommand lookUp() {
		return new CommandBuilder(Category.MISCELLANEOUS)
				.setAliases("lookup")
				.setName("Lookup Command")
				.setDescription("Gives you information on a website")
				.setArgs(new Argument<>("site", String.class))
				.setAction((event, rawArgs) -> {
					String url = String.format(LOOKUP_URL, ((String) event.getArgument("site").get()).replaceAll(" ", "%20"));
					JsonObject result;
					try {
						result = new JsonParser().parse(HttpUtils.read(url)).getAsJsonObject();
					} catch (IOException e) {
						event.sendMessage("Could not connect, please try again later.").queue();
						return;
					}
					String status = result.get("status").getAsString();
					if (!"success".equals(status)) {
						event.sendMessage(result.get("message").getAsString()).queue();
						return;
					}
					String mapUrl = String.format(MAP_URL, result.get("lat").getAsFloat(), result.get("lon").getAsFloat());
					String city = result.get("city").getAsString();
					String country = result.get("country").getAsString();
					String region = result.get("regionName").getAsString();
					String isp = result.get("isp").getAsString();
					String timezone = result.get("timezone").getAsString();
					String zip = result.get("zip").getAsString();
					EmbedBuilder builder = new EmbedBuilder();
					builder.setDescription("**Map:** [Click Here](" + mapUrl + ")\n" +
							"**City:** " + city + "\n" +
							"**Country:** " + country + "\n" +
							"**Region:** " + region + "\n" +
							"**ISP:** " + isp + "\n" +
							"**Timezone:** " + timezone + "\n" +
							"**Zip:** " + zip);
					builder.setColor(event.getGuild().getSelfMember().getColor() == null
							? Color.decode("#D68A38") : event.getGuild().getSelfMember().getColor());
					event.sendMessage(builder.build()).queue();
					
				})
				.build();
	}
}
