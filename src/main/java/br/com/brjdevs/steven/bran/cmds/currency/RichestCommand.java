package br.com.brjdevs.steven.bran.cmds.currency;

import br.com.brjdevs.steven.bran.core.client.Bran;
import br.com.brjdevs.steven.bran.core.command.Command;
import br.com.brjdevs.steven.bran.core.command.builders.CommandBuilder;
import br.com.brjdevs.steven.bran.core.command.enums.Category;
import br.com.brjdevs.steven.bran.core.command.interfaces.ICommand;
import br.com.brjdevs.steven.bran.core.currency.BankAccount;
import br.com.brjdevs.steven.bran.core.utils.Utils;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.User;

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
					List<BankAccount> bankAccounts = new ArrayList<>(Bran.getInstance().getDataManager().getUserDataManager().get().users
							.values().stream().filter(userData -> {
								User u = Bran.getInstance().getUserById(String.valueOf(userData.userId));
								return u != null && !u.isBot();
							}).map(userData -> userData.getProfile().getBankAccount())
							.sorted(Comparator.comparingLong(bankAccount -> Long.MAX_VALUE - bankAccount.getCoins()))
							.limit(15).collect(Collectors.toList()));
					EmbedBuilder embedBuilder = new EmbedBuilder();
					embedBuilder.setTitle("Richest users", null);
					embedBuilder.setColor(Bran.COLOR);
					embedBuilder.setDescription(bankAccounts.stream().map(bankAccount -> (bankAccounts.indexOf(bankAccount) + 1) + ". "
							+ Utils.getUser(Bran.getInstance().getUserById(bankAccount.userId)) + " - "
							+ bankAccount.getCoins() + " coins").collect(Collectors.joining("\n")));
					embedBuilder.setFooter("Total registered bank accounts: " + Bran.getInstance().getDataManager().getUserDataManager().get().users.values().stream().filter(userData -> userData.getProfile().hasBankAccount()).count(), event.getJDA().getSelfUser().getEffectiveAvatarUrl());
					event.sendMessage(embedBuilder.build()).queue();
				})
				.build();
	}
}
