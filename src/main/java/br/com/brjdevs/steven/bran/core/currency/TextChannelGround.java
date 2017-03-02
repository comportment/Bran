package br.com.brjdevs.steven.bran.core.currency;

import br.com.brjdevs.steven.bran.core.managers.ExpirationManager;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

import java.util.*;

public class TextChannelGround {
	
	private static final Map<Long, List<ItemStack>> CHANNEL_GROUND = new HashMap<>();
	private static final ExpirationManager EXPIRATOR = new ExpirationManager();
	private static Random r = new Random(System.currentTimeMillis());
	private final List<ItemStack> stacks;
	
	private TextChannelGround(List<ItemStack> stacks) {
		this.stacks = stacks;
	}
	
	public static List<ItemStack> of(TextChannel textChannel) {
		return CHANNEL_GROUND.computeIfAbsent(Long.parseLong(textChannel.getId()), id -> new ArrayList<ItemStack>());
	}
	
	public static List<ItemStack> of(GuildMessageReceivedEvent event) {
		return of(event.getChannel());
	}
	
	public TextChannelGround drop(List<ItemStack> stacks) {
		List<ItemStack> finalStacks = new ArrayList<>(stacks);
		this.stacks.addAll(finalStacks);
		EXPIRATOR.letExpire(System.currentTimeMillis() + 120000, () -> {
			this.stacks.removeAll(finalStacks);
		});
		
		return this;
	}
	
	public TextChannelGround drop(ItemStack... stacks) {
		return drop(Arrays.asList(stacks));
	}
	
	public TextChannelGround drop(Item item) {
		return drop(new ItemStack(item, 1));
	}
	
	public TextChannelGround drop(int item) {
		return drop(Items.ALL[item]);
	}
	
	public boolean dropWithChance(Item item, int weight) {
		boolean doDrop = r.nextInt(weight) == 0;
		if (doDrop) drop(item);
		return doDrop;
	}
	
	public List<ItemStack> collect() {
		List<ItemStack> finalStacks = new ArrayList<>(stacks);
		stacks.clear();
		return finalStacks;
	}
	
	public boolean dropWithChance(int item, int weight) {
		return dropWithChance(Items.ALL[item], weight);
	}
	
}
