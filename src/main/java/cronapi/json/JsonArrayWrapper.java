package cronapi.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import cronapi.CronapiException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import org.apache.commons.lang3.reflect.FieldUtils;

public class JsonArrayWrapper implements List<JsonElement> {

  public final List<JsonElement> elements;

  public JsonArrayWrapper(JsonArray array) {
    try {
      elements = (List<JsonElement>) FieldUtils.readField(array,"elements", true);
    } catch (IllegalAccessException e) {
      throw new CronapiException(e);
    }
  }

  @Override
  public int size() {
    return elements.size();
  }

  @Override
  public boolean isEmpty() {
    return elements.isEmpty();
  }

  @Override
  public boolean contains(Object o) {
    return elements.contains(o);
  }

  @Override
  public Iterator<JsonElement> iterator() {
    return elements.iterator();
  }

  @Override
  public Object[] toArray() {
    return elements.toArray();
  }

  @Override
  public <T> T[] toArray(T[] a) {
    return elements.toArray(a);
  }

  @Override
  public boolean add(JsonElement o) {
    return elements.add(o);
  }

  @Override
  public boolean remove(Object o) {
    return elements.remove(o);
  }

  @Override
  public boolean containsAll(Collection<?> c) {
    return elements.containsAll(c);
  }

  @Override
  public boolean addAll(Collection<? extends JsonElement> c) {
    return elements.addAll(c);
  }

  @Override
  public boolean addAll(int index, Collection<? extends JsonElement> c) {
    return elements.addAll(c);
  }

  @Override
  public boolean removeAll(Collection<?> c) {
    return elements.removeAll(c);
  }

  @Override
  public boolean retainAll(Collection<?> c) {
    return elements.retainAll(c);
  }

  @Override
  public void clear() {
    elements.clear();
  }

  @Override
  public JsonElement get(int index) {
    return elements.get(index);
  }

  @Override
  public JsonElement set(int index, JsonElement element) {
    return elements.set(index, element);
  }

  @Override
  public void add(int index, JsonElement element) {
    elements.add(index, element);
  }

  @Override
  public JsonElement remove(int index) {
    return elements.remove(index);
  }

  @Override
  public int indexOf(Object o) {
    return elements.indexOf(o);
  }

  @Override
  public int lastIndexOf(Object o) {
    return elements.lastIndexOf(o);
  }

  @Override
  public ListIterator<JsonElement> listIterator() {
    return elements.listIterator();
  }

  @Override
  public ListIterator<JsonElement> listIterator(int index) {
    return elements.listIterator(index);
  }

  @Override
  public List<JsonElement> subList(int fromIndex, int toIndex) {
    return elements.subList(fromIndex, toIndex);
  }
}
