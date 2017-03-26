package br.com.brjdevs.steven.bran.core.managers;

public class MessageCache {
    //private final ConcurrentHashMap<Long, Message> cache = new ConcurrentHashMap<>(3000);
    
    public MessageCache() {
        /*Thread lazyLoad = new Thread(() ->
            Bran.getInstance().getGuilds().stream()
                    .filter(g -> Bran.getInstance().getDataManager().getData()
                            .get().getGuildData(g, true).isModLogEnabled())
                    .forEach(guild ->
                        guild.getTextChannels().stream().filter(channel ->
                                channel.getGuildData().getSelfMember().hasPermission(Permission.MESSAGE_HISTORY, Permission.MESSAGE_READ))
                                .forEach(channel ->
                                    channel.getHistory().retrievePast(20)
                                            .queue(history -> history.stream()
                                                    .filter(message -> message.getCreationTime().isAfter(OffsetDateTime.now().minusHours(8)))
                                                    .forEach(message -> {
                                                        if (cache.size() < 3000)
                                                            cache.put(Long.parseLong(message.getId()), message);
                                                    })
                                )
                    )
        );
        lazyLoad.setName("MessageCache LazyLoad");
        lazyLoad.start();*/
    }
}
