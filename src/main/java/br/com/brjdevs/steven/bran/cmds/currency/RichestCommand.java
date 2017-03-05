package br.com.brjdevs.steven.bran.cmds.currency;

import br.com.brjdevs.steven.bran.core.client.Bran;
import br.com.brjdevs.steven.bran.core.command.Command;
import br.com.brjdevs.steven.bran.core.command.builders.CommandBuilder;
import br.com.brjdevs.steven.bran.core.command.enums.Category;
import br.com.brjdevs.steven.bran.core.command.interfaces.ICommand;
import br.com.brjdevs.steven.bran.core.currency.BankAccount;
import br.com.brjdevs.steven.bran.core.utils.Utils;
import net.dv8tion.jda.core.EmbedBuilder;

import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class RichestCommand {
	
	@Command
	private static ICommand richest() {
		return new CommandBuilder(Category.CURRENCY)
				.setAliases("richest")
				.setName("Richest Users Command")
				.setDescription("Lists you the richest users!")
				.setAction((event) -> {
					List<BankAccount> bankAccounts = new ArrayList<>(Bran.getInstance().getDataManager().getDataHolderManager().get().users
							.values().stream().map(userData -> userData.getProfile().getBankAccount())
							.sorted(Comparator.comparing(BankAccount::getCoins)).limit(15).collect(Collectors.toList()));
					EmbedBuilder embedBuilder = new EmbedBuilder();
					embedBuilder.setColor(Color.DARK_GRAY);
					embedBuilder.setDescription(bankAccounts.stream().map(bankAccount -> (bankAccounts.indexOf(bankAccount) + 1) + ". "
							+ Utils.getUser(Bran.getInstance().getUserById(bankAccount.userId)) + " - "
							+ (bankAccounts.indexOf(bankAccount) == 0 ? "**" : "") + bankAccount.getCoins() + " coins"
							+ (bankAccounts.indexOf(bankAccount) == 0 ? "**" : "")).collect(Collectors.joining("\n")));
					event.sendMessage(embedBuilder.build()).queue();
				})
				.build();
	}
}
