/**
 * DISCLAIMER
 * 
 * The quality of the code is such that you should not copy any of it as best
 * practice how to build Vaadin applications.
 * 
 * @author jouni@vaadin.com
 * 
 */

package com.vaadin.demo.dashboard;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import scala.reflect.internal.TreeGen.GetVarTraverser;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.demo.dashboard.data.DataProvider;
import com.vaadin.demo.dashboard.data.ProjectContainer;
import com.vaadin.demo.dashboard.pbthread.IndexingThread;
import com.vaadin.event.Action;
import com.vaadin.event.Action.Handler;
import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.event.ShortcutListener;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.ProgressBar;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.Align;
import com.vaadin.ui.Table.TableDragMode;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

public class ProjectView extends VerticalLayout implements View {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5151568825013214088L;

	TabSheet tab;

	Table t;

	Object editableId = null;

	ProjectContainer data;

	@Override
	public void enter(ViewChangeEvent event) {
		data = ((DashboardUI) getUI()).dataProvider.getProjects();
		//data = ((DashboardUI) getUI()).dataProvider.getTransactions();

		setSizeFull();
		addStyleName("project");

		t = new Table() {
			/**
			 * 
			 */
			private static final long serialVersionUID = -4672207016299803110L;

			@Override
			protected String formatPropertyValue(Object rowId, Object colId,
					Property<?> property) {
				if (colId.equals("Score")) {
					if (property != null && property.getValue() != null) {
						String ret = new DecimalFormat("#####.##").format(property
								.getValue());
						return ret;
					} else {
						return "";
					}
				}
				return super.formatPropertyValue(rowId, colId, property);
			}
		};
		t.setSizeFull();
		t.addStyleName("borderless");
		t.setSelectable(true);
		t.setColumnCollapsingAllowed(true);
		t.setColumnReorderingAllowed(true);
		data.removeAllContainerFilters();
		t.setContainerDataSource(data);
		sortTable();

		t.setColumnAlignment("Score", Align.RIGHT);
		//t.setColumnAlignment("Price", Align.RIGHT);
		t.setVisibleColumns(new Object[] { "Score", "Name Project", "Description"});


		t.setFooterVisible(true);
		//t.setColumnFooter("Score", "Total");
		//updatePriceFooter();

		// Allow dragging items to the reports menu
		t.setDragMode(TableDragMode.MULTIROW);
		t.setMultiSelect(true);

		HorizontalLayout toolbar = new HorizontalLayout();
		toolbar.setWidth("100%");
		toolbar.setSpacing(true);
		toolbar.setMargin(true);
		toolbar.addStyleName("toolbar");
		addComponent(toolbar);

		Label title = new Label("Search Repositories");
		title.addStyleName("h1");
		title.setSizeUndefined();
		toolbar.addComponent(title);
		toolbar.setComponentAlignment(title, Alignment.MIDDLE_LEFT);

		final TextField filter = new TextField();
		filter.addTextChangeListener(new TextChangeListener() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 4402389209844609512L;

			@Override
			public void textChange(final TextChangeEvent event) {
				String query = event.getText();
				doQuery(query);
			}
		});

		filter.setInputPrompt("Filter");
		filter.addShortcutListener(new ShortcutListener("Clear",
				KeyCode.ESCAPE, null) {
			/**
			 * 
			 */
			private static final long serialVersionUID = 5573322177848475049L;

			@Override
			public void handleAction(Object sender, Object target) {
				filter.setValue("");
				data=((DashboardUI) getUI()).dataProvider.getProjects();
				data.removeAllContainerFilters();
				t.setContainerDataSource(data);
				sortTable();
			}
		});
		toolbar.addComponent(filter);
		toolbar.setExpandRatio(filter, 1);
		toolbar.setComponentAlignment(filter, Alignment.MIDDLE_LEFT);

		Label index = new Label("Index db");
		index.addStyleName("h2");
		index.setSizeUndefined();
		index.setVisible(false);
		toolbar.addComponent(index);
		toolbar.setComponentAlignment(index, Alignment.MIDDLE_RIGHT);

		final ProgressBar indicator = new ProgressBar(new Float(0.0));
		indicator.setVisible(false);
		indicator.setEnabled(false);
		indicator.setSizeFull();
		final Button indexbutton = new Button("Start indexing");
		indexbutton.setVisible(false);
		indexbutton.addClickListener(new ClickListener(){
			private static final long serialVersionUID = -6479091296826030739L;
			@Override
			public void buttonClick(ClickEvent event) {
				//					Notification.show("Not implemented in this demo");
				final IndexingThread t = new IndexingThread(indicator, indexbutton);
				t.start();
				//					Notification.show("Partito");
				UI.getCurrent().setPollInterval(500);
				indicator.setEnabled(true);
				indicator.setVisible(true);
				indexbutton.setEnabled(false);
			}
		});
		toolbar.addComponent(indexbutton);
		toolbar.setComponentAlignment(indexbutton, Alignment.MIDDLE_RIGHT);
		//		toolbar.addComponent(indicator);
		//		toolbar.setComponentAlignment(indicator,Alignment.MIDDLE_LEFT);
		VerticalLayout vl = new VerticalLayout();
		//		vl.addComponent(indexbutton);
		//		vl.setComponentAlignment(indexbutton, Alignment.MIDDLE_RIGHT);
		vl.addComponent(indicator);
		vl.setComponentAlignment(indicator,Alignment.MIDDLE_RIGHT);
		addComponent(vl);

		tab = new TabSheet();
		tab.setSizeFull();
		tab.addTab(t,"Search Repository");
		tab.getTab(t).setClosable(false);

		addComponent(tab);
		setExpandRatio(tab, 1);

		t.addActionHandler(new Handler() {

			/**
			 * 
			 */
			private static final long serialVersionUID = 2447521588313216080L;

			private Action report = new Action("Create Report");
			private Action discard = new Action("Discard");
			private Action details = new Action("Project Dectails");

			@Override
			public void handleAction(Action action, Object sender, Object target) {
				if (action == report) {
					createNewReportFromSelection();
				} else if (action == discard) {
					Notification.show("Not implemented in this demo");
				} else if (action == details) {
					Item item = ((Table) sender).getItem(target);
					if (item != null) {
						Window w = new ProjectWindow(DataProvider
								.getProjectByName(item.getItemProperty("Name Project")
										.getValue().toString()),ProjectView.this);
						UI.getCurrent().addWindow(w);
						w.focus();
					}
				}
			}

			@Override
			public Action[] getActions(Object target, Object sender) {
				return new Action[] { details, report, discard };
			}
		});


		//New table from best asgardian people
		Table resultTable = new Table() {


			/**
			 * 
			 */
			private static final long serialVersionUID = -6373633195598973687L;

			@Override
			protected String formatPropertyValue(Object rowId, Object colId,
					Property<?> property) {
				if (colId.equals("Score")) {
					if (property != null && property.getValue() != null) {
						String ret = new DecimalFormat("#####.##").format(property
								.getValue());
						return ret;
					} else {
						return "";
					}
				}
				return super.formatPropertyValue(rowId, colId, property);
			}
		};
	}

	public String doRemoveSpecialChars(String query){

		if(query==null){
			return ""; 
		}			
		if(query.contains("\\")){
			query=query.replaceAll("\\\\", "");
		}
		if(query.contains("|")){
			query=query.replaceAll("\\|", "");
		}
		if(query.contains("!")){
			query=query.replaceAll("\\!", "");
		}
		if(query.contains("\"")){
			query=query.replaceAll("\\\"", "");
		}
		if(query.contains("£")){
			query=query.replaceAll("£", "");
		}
		if(query.contains("$")){
			query=query.replaceAll("\\$", "");
		}
		if(query.contains("%")){
			query=query.replaceAll("\\%", "");
		}
		if(query.contains("&")){
			query=query.replaceAll("\\&", " ");
		}
		if(query.contains("/")){
			query=query.replaceAll("/", " ");
		}
		if(query.contains("(")){
			query=query.replaceAll("\\(", " ");
		}
		if(query.contains(")")){
			query=query.replaceAll("\\)", " ");
		}
		if(query.contains("=")){
			query=query.replaceAll("=", "");
		}
		if(query.contains("?")){
			query=query.replaceAll("\\?", "");
		}
		if(query.contains("^")){
			query=query.replaceAll("\\^", "");
		}
		if(query.contains("'")){
			query=query.replaceAll("'", "");
		}
		if(query.contains("*")){
			query=query.replaceAll("\\*", "");
		}
		if(query.contains("+")){
			query=query.replaceAll("\\+", "");
		}
		if(query.contains("§")){
			query=query.replaceAll("§", "");
		}
		if(query.contains("°")){
			query=query.replaceAll("°", "");
		}
		if(query.contains("#")){
			query=query.replaceAll("\\#", "");
		}
		if(query.contains("@")){
			query=query.replaceAll("@", "");
		}
		if(query.contains("[")){
			query=query.replaceAll("\\[", "");
		}
		if(query.contains("{")){
			query=query.replaceAll("\\{", "");
		}
		if(query.contains("]")){
			query=query.replaceAll("\\]", "");
		}
		if(query.contains("€")){
			query=query.replaceAll("€", "");
		}
		if(query.contains(",")){
			query=query.replaceAll("\\,", "");
		}
		if(query.contains(";")){
			query=query.replaceAll(";", " ");
		}
//		if(query.contains(".")){
//			query=query.replaceAll("\\.", "");
//		}
		if(query.contains(":")){
			query=query.replaceAll(":", " ");
		}
		if(query.contains("_")){
			query=query.replaceAll("\\_", " ");
		}
		if(query.contains("-")){
			query=query.replaceAll("\\-", " ");
		}

		return query;
	}

	public void doQuery(String query){
		query=doRemoveSpecialChars(query);
		if(query.equals("")){
			data=((DashboardUI) getUI()).dataProvider.getProjects();
			data.removeAllContainerFilters();
			t.setContainerDataSource(data);
			sortTable();
		}else {
			data=((DashboardUI) getUI()).dataProvider.getProjectsByQuery(query);
			data.removeAllContainerFilters();
			t.setContainerDataSource(data);
			sortTable();
		}
	}
	
	public void doQueryOR(Iterator<Component> iterator){
		List<String> l = new ArrayList<String>();
		while(iterator.hasNext()){
			CheckBox ck=(CheckBox) iterator.next();
			boolean value = ck.getValue();
			if(value){
				l.add(doRemoveSpecialChars(ck.getDescription()));
			}
		}
		data=((DashboardUI) getUI()).dataProvider.doQueryOR(l);
		data.removeAllContainerFilters();
		t.setContainerDataSource(data);
		sortTable();
	}

	private void sortTable() {
		t.sort(new Object[] {"score"}, new boolean[] { false });
	}

	void createNewReportFromSelection() {
		((DashboardUI) getUI()).openReports(t);
	}

	private void updatePriceFooter() {
		//		String ret = new DecimalFormat("#.##").format(DataProvider
		//				.getTotalSum());
		String ret = "";
		t.setColumnFooter("Price", "$" + ret);
	}
}
