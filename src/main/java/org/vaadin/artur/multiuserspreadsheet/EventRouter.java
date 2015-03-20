package org.vaadin.artur.multiuserspreadsheet;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.vaadin.ui.UIDetachedException;

public class EventRouter {

	public static interface ValueChangeListener {
		public void valueChanged(int row, int col, int cellType, Object value);

	}

	private static Set<ValueChangeListener> valueChangeListeners = Collections
			.synchronizedSet(new HashSet<ValueChangeListener>());

	public static synchronized void addListener(ValueChangeListener l) {
		valueChangeListeners.add(l);
	}

	public static synchronized void removeListener(ValueChangeListener l) {
		valueChangeListeners.remove(l);
	}

	public static void fireEvent(int row, int col, int cellType, Object value) {
		ValueChangeListener[] listenerCopy = valueChangeListeners
				.toArray(new ValueChangeListener[valueChangeListeners.size()]);
		for (ValueChangeListener l : listenerCopy) {
			fireEvent(l, row, col, cellType, value);
		}
	}

	public static void fireEvent(ValueChangeListener l, int row, int col,
			int cellType, Object value) {
		try {
			l.valueChanged(row, col, cellType, value);
		} catch (UIDetachedException e) {
			// Stop sending events to UIs which no longer are active
			valueChangeListeners.remove(l);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
