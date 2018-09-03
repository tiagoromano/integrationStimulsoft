package cronapi.util;

import java.util.LinkedHashMap;
import java.util.Map;

public class LRUCache<K, V> {
  
  private LinkedHashMap<K, LRUEntry> map;
  
  private int expires;
  
  public LRUCache(int cacheSize, int expires) {
    
    this.expires = expires;
    map = new LinkedHashMap<K, LRUEntry>(16, 0.75f, true) {
      private static final long serialVersionUID = 1L;
      
      protected boolean removeEldestEntry(Map.Entry<K, LRUEntry> eldest) {
        return size() > cacheSize;
      }
    };
  }
  
  public synchronized V get(Object key) {
    LRUEntry entry = map.get(key);
    if(entry != null && System.currentTimeMillis() - entry.time <= expires) {
      return entry.value;
    }
    
    return null;
  }
  
  public synchronized V put(K key, V value) {
    map.put(key, new LRUEntry(value));
    return value;
  }
  
  private class LRUEntry {
    
    private V value;
    private long time = System.currentTimeMillis();
    
    public LRUEntry(V value) {
      this.value = value;
    }
  }
}
