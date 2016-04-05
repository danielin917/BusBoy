package solarcar.vdcSim;


import java.util.concurrent.ConcurrentHashMap;

public final class ReverseCANMessage {

    private ConcurrentHashMap<String, Double> map;
    private String id;
    private String message;
    private String type;

    /*
     * public ConcurrentHashMap<String, Double> getMap() { return map;
	}
     */
    public String getId() {
        return id;
    }

    public String getMessage() {
        return message;
    }

    public Double get(String key) {
        return map.get(key);
    }

    public String getType() {
        return type;
    }

    public ReverseCANMessage(String message) {
        this.message = message;
        this.id = "NULL";
        this.map = new ConcurrentHashMap<>();
        this.type = message.substring(0, 3);

        message = message.substring(4);
        String arr[] = message.split("\\s");
        String split[];
        for (String str : arr) {
            split = str.split("=");
            split[1] = split[1].substring(1, split[1].length() - 1);
            if (split[0].equals("id")) {
                id = split[1];
            } else {
                map.put(split[0], Double.parseDouble(split[1]));
            }
        }
    }

    public ReverseCANMessage(String type, String id, ConcurrentHashMap<String, Double> map) {
        this.type = type;
        this.map = map;
        this.id = id;
        this.message = "NULL";

        // TODO: construct solar message
    }
}