package br.com.brjdevs.bran.cmds.misc;

import br.com.brjdevs.bran.core.RegisterCommand;
import br.com.brjdevs.bran.core.command.Category;
import br.com.brjdevs.bran.core.command.CommandBuilder;
import br.com.brjdevs.bran.core.command.CommandManager;
import br.com.brjdevs.bran.core.utils.HttpUtils;
import br.com.brjdevs.bran.core.utils.StringUtils;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.dv8tion.jda.core.EmbedBuilder;

import java.awt.*;
import java.io.IOException;

@RegisterCommand
public class LookupCommand {
	private static final String LOOKUP_URL = "http://ip-api.com/json/%s";
	private static final String MAP_URL = "https://www.google.com/maps/@%f,%f,15z";
	public LookupCommand() {
		CommandManager.addCommand(new CommandBuilder(Category.MISCELLANEOUS)
				.setAliases("lookup")
				.setName("Lookup Command")
				.setDescription("Gives you information on a website")
				.setArgs("<site>")
				.setAction((event, rawArgs) -> {
					String url = String.format(LOOKUP_URL, StringUtils.splitArgs(rawArgs, 2)[1].replaceAll(" ", "%20"));
					JsonObject result;
					try {
						result = new JsonParser().parse(HttpUtils.read(url)).getAsJsonObject();
					} catch (IOException e) {
						event.sendMessage("Could not connect, please try again later.").queue();
						return;
					}
					String status = result.get("status").getAsString();
					if (!status.equals("success")) {
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
					builder.setColor(event.getOriginGuild().getSelfMember().getColor() == null
							? Color.decode("#D68A38") : event.getOriginGuild().getSelfMember().getColor());
					event.sendMessage(builder.build()).queue();
					
				})
				.build());
	}
}
