package br.com.brjdevs.steven.bran.cmds.currency;

import br.com.brjdevs.steven.bran.core.command.builders.CommandBuilder;
import br.com.brjdevs.steven.bran.core.command.enums.Category;
import br.com.brjdevs.steven.bran.core.command.interfaces.ICommand;
import br.com.brjdevs.steven.bran.core.currency.Item;
import br.com.brjdevs.steven.bran.core.currency.ItemMeta;
import br.com.brjdevs.steven.bran.core.currency.Items;
import br.com.brjdevs.steven.bran.core.managers.RateLimiter;
import br.com.brjdevs.steven.bran.core.managers.profile.Inventory;
import br.com.brjdevs.steven.bran.core.utils.Emojis;
import br.com.brjdevs.steven.bran.core.utils.StringUtils;

import java.util.Arrays;
import java.util.Random;

public class MineCommand {
	
	private static final RateLimiter RATE_LIMITER = new RateLimiter(5000);
	private static final Random r = new Random();
	
	//@Command
	private static ICommand mine() {
		return new CommandBuilder(Category.CURRENCY)
				.setAliases("mine")
				.setName("Mine Command")
				.setDescription("Let's mine some ores!")
				.setAction((event) -> {
                    Inventory inv = event.getUserData().getProfileData().getInventory();
                    long amount = inv.getAmountOf(Items.PICKAXE);
					if (amount <= 0) {
						event.sendMessage("You don't have any pickaxes in your inventory!").queue();
						return;
					}
					int usedStamina = 5;
					if (!RATE_LIMITER.process(event.getAuthor())) {
						usedStamina += 5;
						event.sendMessage("*You are mining too fast so your stamina goes lower faster.*").queue();
					}
                    
                    if (!event.getUserData().getProfileData().takeStamina(usedStamina)) {
                        event.sendMessage("You are too tired of mining, go rest a little bit while your stamina regenerates!").queue();
						return;
					}
					
					int luck = Math.min(r.nextInt(650 * (int) amount), 650);
					
					Ore ore = Ore.random(luck);
					ItemMeta oreItemMeta = ore.getItemMeta(luck);
					Item item = oreItemMeta.getItem();
                    
                    if (!event.getUserData().getProfileData().getInventory().put(item, oreItemMeta)) {
                        event.sendMessage(Emojis.X + " Your inventory is too full of " + ore.getName() + " go sell some first!").queue();
						return;
					}
					
					event.sendMessage("Yay, you found " + oreItemMeta.toString() + "! " + Emojis.PARTY_POPPER).queue();
					
					ItemMeta meta = inv.getItems().get(Items.idOf(Items.PICKAXE));
					if (!meta.doDamage(usedStamina * 2 + r.nextInt(usedStamina / 3))) {
						event.sendMessage("Unfortunately one of your pickaxes broke!").queue();
					}
				})
				.build();
	}
	
	public enum Ore {
		STONE(Items.STONE, 100, 50),
		COOPER(Items.COOPER, 200, 30),
		SILVER(Items.SILVER, 200, 30),
		IRON(Items.IRON, 300, 20),
		GOLD(Items.GOLD, 350, 10),
		DIAMOND(Items.DIAMOND, 500, 5),
		EMERALD(Items.EMERALD, 650, 5);
		
		private int itemId;
		private int rarity;
		private int dMeta;
		
		Ore(Item item, int rarity, int dMeta) {
			this.itemId = Items.idOf(item);
			this.rarity = rarity;
			this.dMeta = dMeta;
		}
		
		public static Ore random(int rarity) {
			return Arrays.stream(values()).filter(ore -> ore.rarity <= rarity).toArray(Ore[]::new)[r.nextInt(values().length - 1)];
		}
		
		public ItemMeta getItemMeta(int luck) {
			return ItemMeta.of(Items.fromId(itemId)).join(Math.abs(r.nextInt(dMeta + (luck / 30))));
		}
		
		public String getName() {
			return StringUtils.capitalize(name());
		}
		
	}
	
}
