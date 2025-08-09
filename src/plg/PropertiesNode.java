package plg;

import java.util.HashMap;
import java.util.Map;

public class PropertiesNode {

    public static enum PropertiesNodeType {
        INT, DOUBLE, BOOLEAN, STRING, COLOR, ITEM
    };

    private Object data = null;
    private String key = "";
    
    private String defaultSVal = "";
    private String maxSVal = "";
    private String minSVal = "";
    private Map<String, Object> map = new HashMap<String, Object>();
    private Object defaultItem = null;
    private PropertiesNodeType type;

    private static int toInt(String str, int def) {
        try {
            return Integer.parseInt(str);
        }
        catch (Exception e) {
            return def;
        }
    }

    private static double toDouble(String str, double def) {
        try {
            return Double.parseDouble(str);
        }
        catch (Exception e) {
            return def;
        }
    }

    private static boolean toBoolean(String str, boolean def) {
        try {
            return Boolean.parseBoolean(str);
        }
        catch (Exception e) {
            return def;
        }
    }

    private static String toColorCode(String str, String def) {
        if (str == null) {
            return def;
        }
        return "#" + str;
    }

    public PropertiesNode(String key, PropertiesNodeType type, String defVal, String minVal, String maxVal, String[] itemName, Object[] itemObjs) {
        this.key = key;
        this.defaultSVal = defVal;
        this.minSVal = minVal;
        this.maxSVal = maxVal;
        this.type = type;

        this.map.clear();
        for (int i = 0; i < itemName.length; i++) {
            this.map.put(itemName[i].toLowerCase(), itemObjs[i]);
        }
    }

    public PropertiesNode(String key, PropertiesNodeType type, String defVal) {
        this.key = key;
        this.defaultSVal = defVal;
        this.type = type;
        this.map.clear();
    }

    public PropertiesNode(String key, PropertiesNodeType type, String defVal, String minVal, String maxVal) {
        this.key = key;
        this.defaultSVal = defVal;
        this.minSVal = minVal;
        this.maxSVal = maxVal;
        this.type = type;
        this.map.clear();
    }

    public PropertiesNode(String key, PropertiesNodeType type, Object defaultItem, String[] itemName, Object[] itemObjs) {
        this.key = key;
        this.type = type;
        this.defaultItem = defaultItem;

        this.map.clear();
        for (int i = 0; i < itemName.length; i++) {
            this.map.put(itemName[i], itemObjs[i]);
        }
    }
    
    public String getKey() {
        return key;
    }
    
    public Object getData() {
        return data;
    }
    
    public void setObject(String s) {
        data = getObject(s);
    }

    public Object getObject(String s) {
        if (s != null) {
            if (map.isEmpty() == false && map.containsKey(s.toLowerCase())) {
                return map.get(s.toLowerCase());
            }
        }

        Object obj = 0;
        if (type == PropertiesNodeType.INT) {
            int idef = toInt(defaultSVal, 0);
            obj = (Object) idef;
            if (s != null) {
                int ival = toInt(s, idef);
                if (maxSVal.isBlank() == false) {
                    int imax = toInt(maxSVal, 0);
                    if (ival > imax) {
                        ival = imax;
                    }
                }
                if (minSVal.isBlank() == false) {
                    int imin = toInt(minSVal, 0);
                    if (ival < imin) {
                        ival = imin;
                    }
                }
                obj = (Object) ival;
            }
        }
        else if (type == PropertiesNodeType.DOUBLE) {
            double ddef = toDouble(defaultSVal, 0.0);
            obj = (Object) ddef;
            if (s != null) {
                double dval = toDouble(s, ddef);
                if (maxSVal.isBlank() == false) {
                    double dmax = toDouble(maxSVal, 0.0);
                    if (dval > dmax) {
                        dval = dmax;
                    }
                }
                if (minSVal.isBlank() == false) {
                    double dmin = toDouble(minSVal, 0.0);
                    if (dval < dmin) {
                        dval = dmin;
                    }
                }
                obj = (Object) dval;
            }
        }
        else if (type == PropertiesNodeType.BOOLEAN) {
            boolean bdef = toBoolean(defaultSVal, false);
            obj = (Object) bdef;
            if (s != null) {
                boolean bval = toBoolean(s, bdef);
                obj = (Object) bval;
            }
        }
        else if (type == PropertiesNodeType.STRING) {
            obj = (Object) (s == null ? defaultSVal : s);
        }
        else if (type == PropertiesNodeType.COLOR) {
            obj = (Object) toColorCode(s, defaultSVal);
        }
        else if (type == PropertiesNodeType.ITEM) {
            obj = (Object) defaultItem;
        }
        else {
            obj = (Object) s;
        }
        return obj;
    }

}
