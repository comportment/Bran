package br.net.brjdevs.steven.bran.cmds.currency;

import br.net.brjdevs.steven.bran.core.client.Bran;
import br.net.brjdevs.steven.bran.core.command.Argument;
import br.net.brjdevs.steven.bran.core.command.Command;
import br.net.brjdevs.steven.bran.core.command.builders.CommandBuilder;
import br.net.brjdevs.steven.bran.core.command.builders.TreeCommandBuilder;
import br.net.brjdevs.steven.bran.core.command.enums.Category;
import br.net.brjdevs.steven.bran.core.command.interfaces.ICommand;
import br.net.brjdevs.steven.bran.core.currency.BankAccount;
import br.net.brjdevs.steven.bran.core.data.UserData;
import br.net.brjdevs.steven.bran.core.utils.Emojis;
import gnu.trove.list.TLongList;
import gnu.trove.list.array.TLongArrayList;
import gnu.trove.map.TLongObjectMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import net.dv8tion.jda.core.entities.User;

import java.util.Random;

public class BetCommand {

    private static final Random rnd = new Random();
    private static final TLongObjectMap<Bet> bets = new TLongObjectHashMap<>();

    @Command
    private static ICommand bet() {
        return new TreeCommandBuilder(Category.CURRENCY)
                .setAliases("bet")
                .setName("Bet Command")
                .setPrivateAvailable(false)
                .addSubCommand(new CommandBuilder(Category.CURRENCY)
                        .setAliases("start")
                        .setName("Bet Start Command")
                        .setDescription("Starts a bet in the current channel.")
                        .setArgs(new Argument("minimum amount", String.class, true))
                        .setAction((event) -> {
                            if (bets.containsKey(event.getTextChannel().getIdLong())) {
                                event.sendMessage("Someone already started a bet in this channel! Why don't you join it?").queue();
                                return;
                            }
                            try {
                                int min = Integer.parseInt(((String) event.getArgument("minimum amount").getOrDefault("100")));
                                if (min > event.getUserData().getProfileData().getBankAccount().getCoins()) {
                                    event.sendMessage("You need " + min + " coins to start a bet with this value!").queue();
                                    return;
                                } else if (min > 2500) {
                                    event.sendMessage("The maximum bet is 2500 coins!").queue();
                                    return;
                                }
                                Bet bet = new Bet(event.getAuthor(), min);
                                bets.put(event.getTextChannel().getIdLong(), bet);
                                event.sendMessage(Emojis.CHECK_MARK + " Started bet in " + event.getTextChannel().getAsMention() + " with " + bet.getMinimum() + " coins!").queue();
                            } catch (NumberFormatException e) {
                                event.sendMessage(event.getArgument("minimum amount").get() + " is not a valid coin amount!").queue();
                            }
                        })
                        .build())
                .addSubCommand(new CommandBuilder(Category.CURRENCY)
                        .setAliases("join")
                        .setName("Bet Join Command")
                        .setArgs(new Argument("amount", Integer.class))
                        .setAction((event) -> {
                            Bet bet = bets.get(event.getTextChannel().getIdLong());
                            if (bet == null) {
                                event.sendMessage("No bets running in this channel!").queue();
                                return;
                            }
                            int amount = ((int) event.getArgument("amount").get());
                            if (amount < bet.getMinimum()) {
                                event.sendMessage("You have to bet at least " + bet.getMinimum() + " coins!").queue();
                            } else if (!event.getUserData().getProfileData().getBankAccount().takeCoins(amount, BankAccount.MAIN_BANK)) {
                                event.sendMessage("You don't have " + amount + " coins to bet!").queue();
                            } else {
                                bet.getParticipating().put(event.getAuthor().getIdLong(), (long) amount);
                                event.sendMessage(Emojis.CHECK_MARK + " You bet " + amount + " coins!").queue();
                            }
                        })
                        .build())
                .addSubCommand(new CommandBuilder(Category.CURRENCY)
                        .setAliases("end")
                        .setName("Bet End Command")
                        .setAction((event) -> {
                            Bet bet = bets.get(event.getTextChannel().getIdLong());
                            if (bet == null) {
                                event.sendMessage("No bets running in this channel!").queue();
                                return;
                            }
                            if (!bet.getCreator().equals(event.getAuthor())) {
                                event.sendMessage(Emojis.WARNING_SIGN + " You didn't start this bet!").queue();
                                return;
                            }
                            long accumulated = bet.getAccumulatedAmount();

                            TLongList list = new TLongArrayList(bet.getParticipating().keys());
                            list.add(bet.creatorId);
                            long winner = list.get(rnd.nextInt(list.size()));

                            User u = event.getJDA().getUserById(winner);
                            UserData userData
                                    = Bran.getInstance().getDataManager().getData().get().getUserData(u);
                            userData.getProfileData().getBankAccount().addCoins(accumulated, BankAccount.MAIN_BANK);
                            event.sendMessage("And the winner is... " + u.getAsMention() + "! Congratulations, you won " + accumulated + " coins! " + Emojis.PARTY_POPPER + " \uD83D\uDC40").queue();
                        })
                        .build())
                .build();
    }

    public static class Bet {

        private long creatorId;
        private TLongObjectMap<Long> participating;
        private int min, shardId;

        public Bet(User user, int min) {
            this.creatorId = user.getIdLong();
            this.participating = new TLongObjectHashMap<>();
            this.min = min;
            this.shardId = Bran.getInstance().getShardId(user.getJDA());
        }

        public int getMinimum() {
            return min;
        }

        public User getCreator() {
            return Bran.getInstance().getShards()[shardId].getJDA().getUserById(creatorId);
        }

        public TLongObjectMap<Long> getParticipating() {
            return participating;
        }

        public long getAccumulatedAmount() {
            return min + participating.valueCollection().stream().mapToLong(Long::longValue).sum() ;
        }
    }
}
