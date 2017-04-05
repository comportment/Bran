package br.net.brjdevs.steven.bran.core.utils;

public enum TimePeriod {
    MINUTE(60000), HOUR(3600000), DAY(86400000), TOTAL(0);
    
    private int millis;
    
    TimePeriod(int millis) {
        this.millis = millis;
    }
    
    public long getMillis() {
        return millis == 0 ? System.currentTimeMillis() : millis;
    }
}
