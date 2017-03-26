package br.net.brjdevs.steven.bran.cmds.currency;

import br.net.brjdevs.steven.bran.core.client.Bran;
import br.net.brjdevs.steven.bran.core.command.Command;
import br.net.brjdevs.steven.bran.core.command.builders.CommandBuilder;
import br.net.brjdevs.steven.bran.core.command.enums.Category;
import br.net.brjdevs.steven.bran.core.command.interfaces.ICommand;
import br.net.brjdevs.steven.bran.core.currency.BankAccount;
import br.net.brjdevs.steven.bran.core.currency.ProfileData;
import br.net.brjdevs.steven.bran.core.utils.Emojis;
import br.net.brjdevs.steven.bran.core.utils.MathUtils;
import br.net.brjdevs.steven.bran.core.utils.TimeUtils;

public class DailyCommand {
    
    @Command
    private static ICommand daily() {
        return new CommandBuilder(Category.CURRENCY)
                .setAliases("daily")
                .setName("Daily Command")
                .setDescription("Gives you your daily reward!")
                .setAction((event) -> {
                    ProfileData profileData = event.getUserData().getProfileData();
                    if (profileData.getLastDaily() > System.currentTimeMillis()) {
                        event.sendMessage(Emojis.X + " You have to wait more " + (TimeUtils.neat(profileData.getLastDaily() - System.currentTimeMillis())) + " to get another daily reward!").queue();
                        return;
                    }
                    long daily = MathUtils.random(100, 200);
                    profileData.setLastDaily(System.currentTimeMillis() + 86400000);
                    
                    if (profileData.getBankAccount().addCoins(daily, BankAccount.MAIN_BANK)) {
                        event.sendMessage("\uD83D\uDCB0 Here you are, " + daily + " coins! You can get your daily reward again in 24 hours!").queue();
                        Bran.getInstance().getDataManager().getData().update();
                    } else
                        event.sendMessage(Emojis.X + " You have too much money, go spend some first!").queue();
                })
                .build();
    }
    
}
