package org.vaadin.artur.multiuserspreadsheet;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.annotation.WebServlet;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.util.CellReference;
import org.vaadin.artur.multiuserspreadsheet.EventRouter.ValueChangeListener;
import org.vaadin.artur.multiuserspreadsheet.MasterState.CellData;
import org.vaadin.artur.multiuserspreadsheet.MasterState.Position;

import com.vaadin.addon.spreadsheet.Spreadsheet;
import com.vaadin.addon.spreadsheet.Spreadsheet.CellValueChangeEvent;
import com.vaadin.addon.spreadsheet.Spreadsheet.CellValueChangeListener;
import com.vaadin.annotations.Push;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.annotations.Widgetset;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

@Widgetset("org.vaadin.artur.multiuserspreadsheet.MultiuserSpreadsheetWidgetset")
@Theme("mytheme")
@Push
public class MultiUserSpreadsheet extends UI {

	@WebServlet(urlPatterns = "/*", name = "MyUIServlet", asyncSupported = true)
	@VaadinServletConfiguration(ui = MultiUserSpreadsheet.class, productionMode = false)
	public static class MyUIServlet extends VaadinServlet {
	}

	private Spreadsheet spreadsheet = new Spreadsheet();

	ValueChangeListener changeListener = new ValueChangeListener() {
		@Override
		public void valueChanged(final int row, final int col,
				final int cellType, final Object value) {
			access(new Runnable() {
				@Override
				public void run() {
					if (cellType == Cell.CELL_TYPE_BLANK) {
						Cell c = spreadsheet.getCell(row, col);
						if (c != null) {
							spreadsheet.deleteCell(row, col);
							spreadsheet.refreshCells(c);
						}

						return;
					}

					Cell c;
					if (cellType == Cell.CELL_TYPE_FORMULA) {
						c = spreadsheet.createFormulaCell(row, col,
								(String) value);
					} else {
						c = spreadsheet.createCell(row, col, value);
					}
					spreadsheet.refreshCells(c);
				}
			});
		}
	};

	@Override
	protected void init(VaadinRequest vaadinRequest) {
		final VerticalLayout layout = new VerticalLayout();
		layout.setSizeFull();
		layout.setMargin(true);
		setContent(layout);

		spreadsheet.addCellValueChangeListener(new CellValueChangeListener() {
			@Override
			public void onCellValueChange(CellValueChangeEvent event) {
				for (CellReference cellRef : event.getChangedCells()) {
					Cell cell = spreadsheet.getCell(cellRef);
					if (cell == null) {
						// Cell does not exist and this event should not arrive
						return;
					}
					Object value = getCellValue(cell);
					int cellType = cell.getCellType();

					MasterState.setCellValue(cellRef.getRow(),
							cellRef.getCol(), cellType, value);
				}
			}
		});

		Map<Position, CellData> initialData = MasterState
				.getStateAndListen(changeListener);
		addInitialData(initialData);

		spreadsheet.focus();
		layout.addComponent(spreadsheet);
		layout.setExpandRatio(spreadsheet, 1);
		addDetachListener(new DetachListener() {

			@Override
			public void detach(DetachEvent event) {
				MasterState.removeListener(changeListener);
			}
		});
	}

	private void addInitialData(Map<Position, CellData> initialData) {
		Set<Cell> updatedCells = new HashSet<>();
		for (Position p : initialData.keySet()) {
			CellData data = initialData.get(p);

			int cellType = data.type;
			int row = p.row;
			int col = p.col;
			Object value = data.value;
			Cell c;
			if (cellType == Cell.CELL_TYPE_FORMULA) {
				c = spreadsheet.createFormulaCell(row, col, (String) value);
			} else {
				c = spreadsheet.createCell(row, col, value);
			}
			updatedCells.add(c);
		}
		spreadsheet.refreshCells(updatedCells);

	}

	protected Object getCellValue(Cell cell) {
		switch (cell.getCellType()) {
		case Cell.CELL_TYPE_BLANK:
			return "";
		case Cell.CELL_TYPE_BOOLEAN:
			return cell.getBooleanCellValue();
		case Cell.CELL_TYPE_ERROR:
			return cell.getErrorCellValue();
		case Cell.CELL_TYPE_FORMULA:
			return cell.getCellFormula();
		case Cell.CELL_TYPE_NUMERIC:
			return cell.getNumericCellValue();
		case Cell.CELL_TYPE_STRING:
			return cell.getStringCellValue();
		}
		return "";
	}

}
