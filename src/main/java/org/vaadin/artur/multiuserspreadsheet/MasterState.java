package org.vaadin.artur.multiuserspreadsheet;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.vaadin.artur.multiuserspreadsheet.EventRouter.ValueChangeListener;

public class MasterState {

	static class Position {
		int row, col;

		public Position(int row, int col) {
			super();
			this.row = row;
			this.col = col;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + col;
			result = prime * result + row;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			Position other = (Position) obj;
			if (col != other.col) {
				return false;
			}
			if (row != other.row) {
				return false;
			}
			return true;
		}

	}

	static class CellData {
		int type;
		Object value;

		public CellData(int type, Object value) {
			super();
			this.type = type;
			this.value = value;
		}
	}

	private static Map<Position, CellData> data = new HashMap<>();

	public synchronized static Map<Position, CellData> getStateAndListen(
			ValueChangeListener changeListener) {
		// for (Position p : data.keySet()) {
		// CellData d = data.get(p);
		// EventRouter
		// .fireEvent(changeListener, p.row, p.col, d.type, d.value);
		// }
		// EventRouter.fireEvent(changeListener, data);
		EventRouter.addListener(changeListener);
		return Collections.unmodifiableMap(data);
	}

	public synchronized static void setCellValue(int row, int col,
			int cellType, Object value) {
		Position position = new Position(row, col);

		if (value == null || value.equals("")) {
			data.remove(position);
		} else {
			data.put(position, new CellData(cellType, value));
		}
		EventRouter.fireEvent(row, col, cellType, value);

	}

	public static void removeListener(ValueChangeListener changeListener) {
		EventRouter.removeListener(changeListener);
	}

}
