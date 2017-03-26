package br.net.brjdevs.steven.bran.core.managers;

import br.net.brjdevs.steven.bran.core.utils.MathHelper;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Permissions {
    public static final long
            RUN_BASECMD = bits(1),
            RUN_USRCMD = bits(2),
            MUSIC = bits(3),
            DJ = bits(4),
            CUSTOM_CMDS = bits(5),
            POLL = bits(6),
            ANNOUNCE = bits(7),
            BAN_USER = bits(8),
            KICK_USR = bits(9),
            GUILD_MANAGE = bits(10),
            PREFIX = bits(11),
            WORD_FILTER = bits(12),
            PERMSYS_GM = bits(13),
            PERMSYS_GO = bits(14),
            PERMSYS_BO = bits(15),
            PRUNE_CLEANUP = bits(16),
            EVAL = bits(17),
		    CREATE_GIVEAWAY = bits(18),
		    BLACKLIST = bits(19),
		    LOAD_SAVE = bits(20),
            STOP_RESET = bits(21),
            BOT_ADMIN = bits(22);

    public static final long
            BASE_USR = RUN_BASECMD | RUN_USRCMD | MUSIC | CUSTOM_CMDS | POLL,
		    GUILD_MOD = BASE_USR | DJ | BAN_USER | KICK_USR | PERMSYS_GM | WORD_FILTER | CREATE_GIVEAWAY,
		    GUILD_OWNER = GUILD_MOD | PERMSYS_GO | PREFIX | ANNOUNCE | PRUNE_CLEANUP | GUILD_MANAGE,
		    BOT_OWNER = GUILD_OWNER | PERMSYS_BO | EVAL | STOP_RESET | LOAD_SAVE | BLACKLIST | BOT_ADMIN;
	
	public static Map<String, Long> perms = new HashMap<>();
	
	static {
		Arrays.stream(Permissions.class.getDeclaredFields())
				.filter(field -> Modifier.isStatic(field.getModifiers()) && Modifier.isFinal(field.getModifiers()) && Modifier.isPublic(field.getModifiers())) //public static final fields only
				.forEach(field -> {
					try {
						perms.put(field.getName(), field.getLong(null));
					} catch (Exception ignored) {
					}
				});
	}
	
	public static boolean checkPerms(long senderPerm, long targetPerm) {
        long perms = bits(13, 14, 15);
        senderPerm &= perms;
        targetPerm &= perms; //Select bits 13, 14, 15
		targetPerm = MathHelper.previousPowerOfTwo(MathHelper.roundToPowerOf2(targetPerm));
		senderPerm = MathHelper.previousPowerOfTwo(MathHelper.roundToPowerOf2(senderPerm)); //Get the biggest
		return targetPerm <= senderPerm;
    }
	
	public static boolean hasPermission(long userPerm, long perm) {
		return (userPerm & perm) == perm;
	}


    private static long bits(long... bits) {
        long mask = 0;
        for (long bit : bits) {
            mask |= (long) Math.pow(2, bit);
        }
        return mask;
    }
    public static List<String> toCollection(long userPerms) {
        return perms
                .entrySet()
                .stream()
                .filter(entry -> (entry.getValue() & userPerms) == entry.getValue())
                .map(Map.Entry::getKey)
                .sorted(String::compareTo).collect(Collectors.toList());
    }
}
