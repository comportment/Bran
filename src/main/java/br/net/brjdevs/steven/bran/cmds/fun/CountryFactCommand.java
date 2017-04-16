package br.net.brjdevs.steven.bran.cmds.fun;

import br.net.brjdevs.steven.bran.core.command.Argument;
import br.net.brjdevs.steven.bran.core.command.builders.CommandBuilder;
import br.net.brjdevs.steven.bran.core.command.enums.Category;
import br.net.brjdevs.steven.bran.core.command.interfaces.ICommand;
import br.net.brjdevs.steven.bran.core.quote.Quotes;
import br.net.brjdevs.steven.bran.core.utils.Hastebin;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import net.dv8tion.jda.core.EmbedBuilder;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class CountryFactCommand {
    
    private static final String factbook = "http://champs.tk/factbook/%s.json";
    
    //@Command
    private static ICommand countryfact() {
        return new CommandBuilder(Category.FUN)
                .setAliases("countryfact", "cfact", "country")
                .setName("Country Fact Command")
                .setDescription("Gives you a lot of information on countries!")
                .setArgs(new Argument("country", String.class))
                .setAction((event) -> {
                    String url = String.format(factbook, ((String) event.getArgument("country").get()));
                    JSONObject queryResult;
                    try {
                        queryResult = new JSONObject(Unirest.get(url).asString().getBody());
                    } catch (JSONException | UnirestException e) {
                        event.sendMessage(Quotes.FAIL, "Country not found!").queue();
                        return;
                    }
                    String countryName = queryResult.getJSONObject("Government").getJSONObject("Country name").getJSONObject("conventional short form").getString("text");
                    String longName = queryResult.getJSONObject("Government").getJSONObject("Country name").getJSONObject("conventional long form").getString("text");
                    String description = queryResult.getJSONObject("Introduction").getJSONObject("Background").getString("text");
                    EmbedBuilder embedBuilder = new EmbedBuilder();
                    embedBuilder.setFooter("", event.getAuthor().getEffectiveAvatarUrl());
                    embedBuilder.setAuthor("\u2139 Information on " + countryName, null, null);
                    embedBuilder.addField(":flag_" + ((String) event.getArgument("country").get()).toLowerCase() + ": Name", countryName + ", " + longName + " (" + ((String) event.getArgument("country").get()).toUpperCase() + ")", true);
                    embedBuilder.addField("\uD83D\uDCD4 Historical Information", description.length() > 1024 ? "The description was too long but you can read it in here: " + Hastebin.post(description) : description, false);
                    embedBuilder.addField("\uD83C\uDFD9 Major urban areas", queryResult.getJSONObject("People and Society").getJSONObject("Major urban areas - population").getString("text"), false);
                    embedBuilder.addField(":family_mwgb: Population", queryResult.getJSONObject("People and Society").getJSONObject("Population").getString("text"), false);
                    
                    event.sendMessage(embedBuilder.build()).queue();
                    
                })
                .build();
    }
}
