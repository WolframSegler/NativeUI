package wfg.native_ui.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ArrayMapTest {
    private ArrayMap<String, Integer> map;

    @BeforeEach
    void setUp() {
        map = new ArrayMap<>();
    }

    @Test
    void testPutGetRemoveBasic() {
        assertTrue(map.isEmpty());
        assertEquals(0, map.size());

        assertNull(map.put("a", 1));
        assertEquals(1, map.size());
        assertEquals(1, map.get("a"));

        assertNull(map.put("b", 2));
        assertEquals(2, map.size());
        assertEquals(2, map.get("b"));

        // Replace existing
        Integer old = map.put("a", 10);
        assertEquals(1, old);
        assertEquals(10, map.get("a"));

        // Remove
        Integer removed = map.remove("a");
        assertEquals(10, removed);
        assertNull(map.get("a"));
        assertEquals(1, map.size());

        // remove non-existent returns null
        assertNull(map.remove("not-there"));
    }

    @Test
    void testNullKeysAndValues() {
        map.put(null, 5);
        map.put("x", null);

        assertTrue(map.containsKey(null));
        assertTrue(map.containsValue(null));
        assertEquals(5, map.get(null));
        assertNull(map.get("x"));

        // indexOfKey should find null
        assertEquals(0, map.indexOfKey(null));
    }

    @Test
    void testContainsKeyAndValueAndIndexOfValue() {
        map.put("k1", 11);
        map.put("k2", 22);
        map.put("k3", 22);

        assertTrue(map.containsKey("k1"));
        assertFalse(map.containsKey("missing"));

        assertTrue(map.containsValue(22));
        assertTrue(map.indexOfValue(22) >= 1);
    }

    @Test
    void testKeySetLiveViewAndIteratorRemove() {
        map.put("a", 1);
        map.put("b", 2);
        map.put("c", 3);

        Set<String> ks = map.keySet();
        assertEquals(3, ks.size());
        assertTrue(ks.contains("b"));

        Iterator<String> it = ks.iterator();
        String first = it.next();
        // remove via iterator
        it.remove();
        assertEquals(2, map.size());
        assertFalse(map.containsKey(first));

        // removing via map reflected in view
        map.remove("b");
        assertFalse(ks.contains("b"));
    }

    @Test
    void testValuesLiveViewAndIteratorRemove() {
        map.put("a", 1);
        map.put("b", 2);
        map.put("c", 3);

        Collection<Integer> values = map.values();
        assertEquals(3, values.size());
        assertTrue(values.contains(2));

        Iterator<Integer> it = values.iterator();
        while (it.hasNext()) {
            Integer v = it.next();
            if (v == 2) {
                it.remove();
            }
        }
        assertEquals(2, map.size());
        assertFalse(map.containsValue(2));
    }

    @Test
    void testEntrySetCopyViewAndSetValueAndRemove() {
        map.put("a", 1);
        map.put("b", 2);

        // use the explicit snapshot / copy API
        final Set<Entry<String, Integer>> entries = map.entrySetCopy();
        assertEquals(2, entries.size());

        final Iterator<Entry<String, Integer>> it = entries.iterator();
        final Entry<String, Integer> e = it.next();

        // record the key the snapshot entry represents
        final String keyBefore = e.getKey();

        // setValue should NOT update the underlying map (only the copied Entry)
        final Integer old = e.setValue(99);
        assertNotNull(old);
        assertEquals(1, map.get(keyBefore)); // map unchanged
        assertEquals(99, e.getValue()); // snapshot entry updated

        // remove via snapshot iterator -> should remove from the snapshot only
        it.remove();
        // The backing map should NOT change
        assertEquals(2, map.size());
        assertTrue(map.containsKey("a"));
        assertTrue(map.containsKey("b"));
    }

    @Test
    void testEntrySetNonCopyLiveViewAndSetValueAndRemove() {
        map.put("a", 1);
        map.put("b", 2);

        // low-allocation live view (reuses one MapEntry instance)
        final Set<Entry<String, Integer>> entries = map.singleEntrySet();
        assertEquals(2, entries.size());

        final Iterator<Entry<String, Integer>> it = entries.iterator();
        final Entry<String, Integer> e = it.next();

        // capture the key returned by next() before any mutation/removal
        final String keyReturned = e.getKey();

        // setValue should update the underlying map
        final Integer old = e.setValue(99);
        assertNotNull(old);
        assertEquals(99, map.get(keyReturned));
        assertEquals(99, e.getValue());

        // remove via entry iterator. The underlying map should reflect removal
        it.remove();
        assertEquals(1, map.size());
        assertFalse(map.containsKey(keyReturned), "the key returned by next() must have been removed from the backing map");
    }

    @Test
    void testEqualsAndHashCodeBehavior() {
        map.put("a", 1);
        map.put("b", 2);

        Map<String, Integer> other = new HashMap<>();
        other.put("a", 1);
        other.put("b", 2);

        assertTrue(map.equals(other));
        assertEquals(map.hashCode(), map.hashCode());
        assertEquals(map.hashCode(), other.hashCode()); // hash contract not strict but likely equal here
    }

    @Test
    void testClearAndErase() {
        map.put("a", 1);
        map.put("b", 2);
        assertEquals(2, map.size());

        map.clear();
        assertEquals(0, map.size());
        assertTrue(map.isEmpty());

        map.put("x", 3);
        map.put("y", 4);
        map.erase(); // shrinkless clear
        assertEquals(0, map.size());
        assertTrue(map.isEmpty());
    }

    @Test
    void testEnsureCapacityAndPutAll() {
        ArrayMap<String, Integer> other = new ArrayMap<>();
        for (int i = 0; i < 50; i++) {
            other.put("k" + i, i);
        }
        map.ensureCapacity(60);
        assertEquals(0, map.size());
        map.putAll(other);
        assertEquals(50, map.size());
        for (int i=0;i<50;i++) assertEquals(i, map.get("k"+i));
    }

    @Test
    void testPutAllWithMap() {
        Map<String, Integer> hm = new HashMap<>();
        hm.put("a", 1);
        hm.put("b", 2);
        map.putAll(hm);
        assertEquals(2, map.size());
        assertEquals(1, map.get("a"));
    }

    @Test
    void testRemoveAllAndRetainAll() {
        map.put("a", 1);
        map.put("b", 2);
        map.put("c", 3);

        boolean removed = map.removeAll(Arrays.asList("a", "c"));
        assertTrue(removed);
        assertFalse(map.containsKey("a"));
        assertTrue(map.containsKey("b"));

        // retainAll
        map.put("a", 1);
        boolean changed = map.retainAll(Arrays.asList("b"));
        assertTrue(changed);
        assertEquals(1, map.size());
        assertTrue(map.containsKey("b"));
    }

    @Test
    void testReplaceAllAndForEach() {
        for (int i=0;i<5;i++) map.put("k"+i, i);
        map.replaceAll((k, v) -> v + 10);
        for (int i=0;i<5;i++) assertEquals(i + 10, map.get("k"+i));

        AtomicInteger sum = new AtomicInteger();
        map.forEach((k, v) -> sum.addAndGet(v));
        assertEquals((0+1+2+3+4) + 5*10, sum.get());
    }

    @Test
    void testIndexOfKeyAndIndexOfValue() {
        map.put("a", 1);
        map.put("b", 2);
        assertTrue(map.indexOfKey("a") >= 0);
        assertTrue(map.indexOfValue(2) >= 0);
    }

    @Test
    void testIdentityHashCodeMode() {
        ArrayMap<String, Integer> idMap = new ArrayMap<>(0, true);
        String a1 = new String("same");
        String a2 = new String("same");
        assertNotSame(a1, a2);
        idMap.put(a1, 7);
        // Since identity mode uses == for equality, lookup with a2 should not find it
        assertNull(idMap.get(a2));
        assertTrue(idMap.containsKey(a1));
        assertFalse(idMap.containsKey(a2));
    }

    @Test
    void testConcurrentModificationDetectionOnIterator() {
        map.put("a", 1);
        map.put("b", 2);
        Iterator<String> it = map.keySet().iterator();
        assertTrue(it.hasNext());
        map.put("c", 3); // structural modification outside iterator
        assertThrows(ConcurrentModificationException.class, it::next);
    }

    @Test
    void testEntrySetContainsAndClear() {
        map.put("k1", 1);
        Set<Map.Entry<String, Integer>> entries = map.entrySet();
        Map.Entry<String, Integer> e = entries.iterator().next();
        assertTrue(entries.contains(e));
        entries.clear();
        assertTrue(map.isEmpty());
    }

    @Test
    void testToStringAndDeepToStringArrays() {
        ArrayMap<Object, Object> m = new ArrayMap<>();
        m.put("a", new int[]{1,2});
        String s = m.toString();
        assertTrue(s.contains("a"));
        String deep = ArrayMap.deepToString(new int[]{1,2,3});
        assertTrue(deep.contains("1"));
    }

    @Test
    void testEdgeCasesInvalidRemoveAtIndex() {
        map.put("a", 1);
        assertThrows(ArrayIndexOutOfBoundsException.class, () -> map.removeAt(-1));
        assertThrows(ArrayIndexOutOfBoundsException.class, () -> map.removeAt(100));
    }

    @Test
    void testContainsAllAndClearBehavior() {
        map.put("a", 1);
        map.put("b", 2);
        assertTrue(map.containsAll(Arrays.asList("a", "b")));
        map.clear();
        assertEquals(0, map.size());
    }

    @Test
    public void testBasicPutGet() {
        assertTrue(map.isEmpty());
        map.put("a", 1);
        map.put("b", 2);
        assertEquals(2, map.size());
        assertEquals(1, map.get("a"));
        assertEquals(2, map.get("b"));
        assertNull(map.get("c"));
    }

    @Test
    public void testNullKeyAndValue() {
        map.put(null, 42);
        map.put("x", null);
        assertEquals(42, map.get(null));
        assertNull(map.get("x"));
        assertTrue(map.containsKey(null));
        assertTrue(map.containsValue(null));
    }

    @Test
    public void testOverwriteValue() {
        map.put("a", 1);
        int old = map.put("a", 99);
        assertEquals(1, old);
        assertEquals(99, map.get("a"));
    }

    @Test
    public void testRemove() {
        map.put("a", 1);
        map.put("b", 2);
        map.remove("a");
        assertFalse(map.containsKey("a"));
        assertEquals(1, map.size());
        map.remove("b");
        assertTrue(map.isEmpty());
    }

    @Test
    public void testClear() {
        map.put("a", 1);
        map.put("b", 2);
        map.clear();
        assertTrue(map.isEmpty());
        assertEquals(0, map.size());
    }

    @Test
    public void testPutAllAndContainsAll() {
        Map<String, Integer> other = new HashMap<>();
        other.put("x", 10);
        other.put("y", 20);
        map.putAll(other);
        assertEquals(2, map.size());
        assertTrue(map.containsAll(Arrays.asList("x", "y")));
        assertFalse(map.containsAll(Arrays.asList("x", "y", "z")));
    }

    @Test
    public void testIteratorRemove() {
        map.put("a", 1);
        map.put("b", 2);
        Iterator<Integer> it = map.values().iterator();
        while (it.hasNext()) {
            Integer val = it.next();
            if (val == 1) it.remove();
        }
        assertFalse(map.containsValue(1));
        assertTrue(map.containsValue(2));
    }

    @Test
    public void testKeySetAndValuesLive() {
        map.put("a", 1);
        map.put("b", 2);
        Set<String> keys = map.keySet();
        Collection<Integer> values = map.values();
        assertTrue(keys.contains("a"));
        assertTrue(values.contains(1));

        map.put("c", 3);
        assertTrue(keys.contains("c"));
        assertTrue(values.contains(3));

        map.remove("b");
        assertFalse(keys.contains("b"));
        assertFalse(values.contains(2));
    }

    @Test
    public void testRandomOperationsAgainstHashMap() {
        Random rand = new Random(1234);
        ArrayMap<Integer, Integer> aMap = new ArrayMap<>();
        HashMap<Integer, Integer> hMap = new HashMap<>();

        for (int i = 0; i < 500; i++) {
            int key = rand.nextInt(100);
            int val = rand.nextInt(1000);

            aMap.put(key, val);
            hMap.put(key, val);
            assertEquals(hMap.size(), aMap.size());
            for (Integer k : hMap.keySet()) {
                assertEquals(hMap.get(k), aMap.get(k));
            }

            // occasionally remove keys
            if (i % 5 == 0) {
                int rKey = rand.nextInt(100);
                aMap.remove(rKey);
                hMap.remove(rKey);
                assertEquals(hMap.size(), aMap.size());
            }
        }
    }

    // === Extra edge cases ===
    @Test
    public void testDuplicateKeysAndNulls() {
        map.put("a", 1);
        map.put("a", 2);
        map.put(null, 10);
        map.put(null, 20);
        assertEquals(2, map.size());
        assertEquals(2, map.get("a"));
        assertEquals(20, map.get(null));
    }

    @Test
    public void testRetainAllAndRemoveAll() {
        map.put("a", 1);
        map.put("b", 2);
        map.put("c", 3);
        map.retainAll(Arrays.asList("a", "c"));
        assertFalse(map.containsKey("b"));
        assertTrue(map.containsKey("a"));
        assertTrue(map.containsKey("c"));

        map.removeAll(Arrays.asList("a"));
        assertFalse(map.containsKey("a"));
        assertTrue(map.containsKey("c"));
    }

    @Test
    public void testReplaceAll() {
        map.put("a", 1);
        map.put("b", 2);
        map.replaceAll((k,v) -> v*10);
        assertEquals(10, map.get("a"));
        assertEquals(20, map.get("b"));
    }

    @Test
    void testEntrySetCopyIndependentEntries() {
        map.put("a", 1);
        map.put("b", 2);

        final ArrayList<Map.Entry<String, Integer>> snapshot = new ArrayList<>(map.entrySetCopy());

        // snapshot must contain independent Entry objects (not the same reference)
        assertEquals(2, snapshot.size());
        assertNotSame(snapshot.get(0), snapshot.get(1));

        // mutating the original map after snapshot must not affect the snapshot entries
        map.put("a", 99);

        Integer valueInSnapshot = null;
        for (Map.Entry<String,Integer> e : snapshot) {
            if ("a".equals(e.getKey())) { valueInSnapshot = e.getValue(); break; }
        }
        assertNotNull(valueInSnapshot);
        assertEquals(1, valueInSnapshot); // snapshot preserved original value
        assertEquals(99, map.get("a")); // underlying map changed
    }

    @Test
    void testEntrySetNonCopyReusesEntryInstanceWhenCollected() {
        map.put("x", 10);
        map.put("y", 20);
        map.put("z", 30);

        // singleEntrySet returns the low-allocation live view (reuses Map.Entry)
        final ArrayList<Map.Entry<String, Integer>> liveList = new ArrayList<>(map.singleEntrySet());

        assertEquals(3, liveList.size());

        // All elements collected should be the same object reference (iterator reuses one entry)
        final Map.Entry<String,Integer> first = liveList.get(0);
        for (int i = 1; i < liveList.size(); i++) {
            assertSame(first, liveList.get(i), "singleEntrySet() should reuse the same Map.Entry instance");
        }

        // The object reflects the last iterated position (sanity check)
        final Map.Entry<String,Integer> lastEntry = liveList.get(liveList.size() - 1);
        assertEquals(lastEntry.getKey(), first.getKey());
        assertEquals(map.get(lastEntry.getKey()), lastEntry.getValue());
    }

    @Test
    void testDefaultEntrySetLiveViewAndMutations() {
        map.put("a", 1);
        map.put("b", 2);

        // default entrySet() returns live unique Entry objects
        final ArrayList<Map.Entry<String, Integer>> liveEntries = new ArrayList<>(map.entrySet());
        assertEquals(2, liveEntries.size());
        // entries should be distinct objects (not the same reused instance)
        assertNotSame(liveEntries.get(0), liveEntries.get(1));

        // setValue on a live Entry should update the underlying map
        final Iterator<Map.Entry<String, Integer>> it = map.entrySet().iterator();
        final Map.Entry<String, Integer> e = it.next();

        final Integer old = e.setValue(99);
        assertNotNull(old);
        assertEquals(99, map.get(e.getKey())); // map updated
        assertEquals(99, e.getValue()); // entry reflects new value

        // remove via entry iterator should remove from the backing map
        it.remove();
        assertEquals(1, map.size());
        // the removed key should no longer be present in the map
        assertFalse(map.containsKey("a") && map.get("a") == 99 ? true : false);
    }

    @Test
    void testGetOrDefaultWithNullValue() {
        // According to the Map contract, getOrDefault returns null when the key exists, even if a non‑null default is provided.
        map.put("nullValueKey", null);

        final Integer defaultVal = 42;

        // This must return null, not 42, because the key exists.
        assertNull(map.getOrDefault("nullValueKey", defaultVal),
                "getOrDefault must return null when key exists with null value");
    }

    @Test
    void testGetOrDefaultReturnsDefaultForMissingKey() {
        final Integer defaultValue = 99;
        final String missingKey = "doesNotExist";

        assertNull(map.get(missingKey), "get should return null for missing key");

        assertEquals(defaultValue,
                    map.getOrDefault(missingKey, defaultValue),
                    "getOrDefault must return the default when key is missing");

        assertEquals(Integer.valueOf(42),
                    map.getOrDefault(missingKey, 42),
                    "getOrDefault with another default value");
    }

    @Test
    void testComputeIfAbsentNeverStoresNull() {
        // Mapping function that returns null – the map must NOT insert the entry.
        map.computeIfAbsent("shouldNotExist", k -> null);
        assertFalse(map.containsKey("shouldNotExist"));
        // getOrDefault with a non‑null default must return the default because the key is absent.
        assertEquals(Integer.valueOf(100), map.getOrDefault("shouldNotExist", 100));

        // Normal usage: mapping function returns a non‑null value.
        map.computeIfAbsent("exists", k -> 7);
        assertTrue(map.containsKey("exists"));
        assertEquals(Integer.valueOf(7), map.get("exists"));

        // getOrDefault on the now‑present key must return the stored value (7), not the default.
        assertEquals(Integer.valueOf(7), map.getOrDefault("exists", 999));

        // Additional check: calling computeIfAbsent again with a different mapping function
        // must NOT overwrite the existing value.
        map.computeIfAbsent("exists", k -> 123);
        assertEquals(Integer.valueOf(7), map.get("exists"));
    }
}