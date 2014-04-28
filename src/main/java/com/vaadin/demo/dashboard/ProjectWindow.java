package com.vaadin.demo.dashboard;



import it.unimib.disco.essere.serial.searching.Repository;

import java.util.Iterator;

import com.google.common.collect.Multimap;
import com.vaadin.demo.dashboard.pbthread.AnalysisThread;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.server.ExternalResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Link;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.ProgressBar;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

public class ProjectWindow extends Window {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5229945610717075518L;
	Label synopsis = new Label();

	public ProjectWindow(final Repository r, final ProjectView p) {
		VerticalLayout l = new VerticalLayout();
		l.setSpacing(true);
		//l.setHeight(null);
		
		setCaption(r.nome);
		setContent(l);
		center();
		setCloseShortcut(KeyCode.ESCAPE, null);
		setResizable(false);
		setScrollTop(10);
		setScrollLeft(10);
		setClosable(false);

		addStyleName("no-vertical-drag-hints");
		addStyleName("no-horizontal-drag-hints");

		HorizontalLayout details = new HorizontalLayout();
		details.setSpacing(true);
		details.setMargin(true);
		l.addComponent(details);
		
		FormLayout fields = new FormLayout();
		fields.setWidth("35em");
		fields.setSpacing(true);
		fields.setMargin(true);

		details.addComponent(fields);

		Label label;
		
		label = new Label(r.nome);
		label.setSizeUndefined();
		label.setCaption("Name");
		fields.addComponent(label);

//		Panel panel = new Panel();
//		panel.setCaption("Description");
//		panel.setHeight(null);
		final TextArea notes = new TextArea("Description");
		notes.setValue(r.descrizione);
//		notes.setHeight(null);
		notes.setSizeFull();
		
//		panel.setContent(notes);

		fields.addComponent(notes);
		notes.setReadOnly(true);
		final ComboBox checkout = new ComboBox("Select checkout");
		checkout.setSizeFull();
		Multimap<String, String> g =r.getRepositories();
		for(String s :g.keySet()){
			for(String co:g.get(s)){
				checkout.addItem(co);
			}
		}
		
		Link link = new Link("Link",new ExternalResource(r.link));
		link.setCaption("Link");
		link.setTargetName("_blank");
		fields.addComponent(link);
		final ProgressBar indicator = new ProgressBar(new Float(0.0));
		indicator.setVisible(false);
		indicator.setEnabled(false);
		indicator.setSizeFull();
		
		
		final Button analyse = new Button("Start analysis");
		analyse.addStyleName("default");
		analyse.addClickListener(new ClickListener(){
			/**
			 * 
			 */
			private static final long serialVersionUID = -3883305257845654869L;
			@Override
			public void buttonClick(ClickEvent event) {
				String s =(String)checkout.getConvertedValue();
				if(s!=null){
					//Notification.show("Not implemented in this demo, selezionato:\n"+s);
					Window w = new AnalysisWindow(r,s,indicator,analyse);
					UI.getCurrent().addWindow(w);
					w.focus();
//					final AnalysisThread t = new AnalysisThread(indicator, analyse);
//					t.start();
					UI.getCurrent().setPollInterval(500);
					indicator.setEnabled(true);
					indicator.setVisible(true);
					analyse.setEnabled(false);
				}else{
					Notification.show("Seleziona un valore");
				}
			}
		});
		fields.addComponent(checkout);
		
		int columns = 3;
//		int columns = 6;
		
		Label tag = new Label("");
		tag.setCaption("Tags");
		
		double rg = (double)r.getTags().size()/columns;
		int righeint = (int)rg;
		if(rg>0&&(rg-(double)righeint)>0){
			righeint++;
		}else{
			if(rg==0){
				righeint++;
			}
		}
		System.out.println(righeint);
		
		final GridLayout gl = new GridLayout(columns,righeint);
		gl.setCaption("Tag");
		Iterator<String> tags = r.getTags().iterator();
		for(int righe = 0; tags.hasNext();righe++){
			for(int colonne = 0; colonne<columns&&tags.hasNext();colonne++){
				String caption = tags.next();
				String captiondot = caption;
				if(caption.length()>11){
					captiondot=caption.substring(0, 10)+"...";
				}
				CheckBox ckbox = new CheckBox(captiondot);
				ckbox.setDescription(caption);
				gl.addComponent(ckbox, colonne, righe);
				//Button b = new Button(captiondot);
				//b.setDescription(caption);
				//b.addStyleName("small");
//				gl.addComponent(b, colonne, righe);
//				b.addClickListener(new ClickListener() {
//					/**
//					 * 
//					 */
//					private static final long serialVersionUID = -6383016597762565727L;
//
//					@Override
//					public void buttonClick(ClickEvent event) {
//						p.doQuery(event.getButton().getCaption());
//					}
//				});
			}	
		}
		
		if(rg==0){
			tag.setValue("No tag");
			fields.addComponent(tag);
		}else{
			Button queryTags = new Button("Search Tags");
			queryTags.addStyleName("small");
			queryTags.addClickListener(new ClickListener(){
				private static final long serialVersionUID = -6383016597762565727L;
				@Override
				public void buttonClick(ClickEvent event) {
					Iterator<Component> components = gl.iterator();
//					String query = new String();
//					boolean first = true;
//					while(components.hasNext()){
//						CheckBox c = (CheckBox) components.next();
//						boolean value = c.getValue();
//						if(value){
//							if(first){
//								first = false;
//								query = c.getDescription();
//							}else{
//								query = String.format("%s %s", query,c.getDescription());//description because the caption have the dots
//							}
//						}
//					}
//					p.doQuery(query);
					p.doQueryOR(components);
				}
				
			});
//			gl.addComponent(queryTags);
			fields.addComponent(gl);
			fields.addComponent(queryTags);
		}
		
		fields.addComponent(analyse);
		fields.addComponent(indicator);
		
//		Button b = new Button("new tree window");
//		b.addClickListener(new ClickListener(){
//
//			/**
//			 * 
//			 */
//			private static final long serialVersionUID = 2762251282091197862L;
//
//			@Override
//			public void buttonClick(ClickEvent event) {
//				Window w = new AnalysisWindow(r,"");
//			}
//			
//		});
//		fields.addComponent(b);
		
		HorizontalLayout footer = new HorizontalLayout();
		footer.addStyleName("footer");
		footer.setSizeFull();//Width("100%");
		footer.setMargin(true);

		Button close = new Button("Close");
		close.addStyleName("wide");
		close.addStyleName("default");
		close.addClickListener(new ClickListener() {
			/**
			 * 
			 */
			private static final long serialVersionUID = -6383016597762565727L;

			@Override
			public void buttonClick(ClickEvent event) {
				close();
			}
		});
		footer.addComponent(close);
		footer.setComponentAlignment(close, Alignment.TOP_RIGHT);
		l.addComponent(footer);
	}

		public void updateDescription(Repository r, boolean expand) {
			String descriptionText = synopsis.getData().toString();
			if (r != null) {
				descriptionText = r.descrizione;
				synopsis.setData(descriptionText);
			}
			if (!expand) {
				descriptionText = descriptionText.length() > 300 ? descriptionText
						.substring(0, 300) + "…" : descriptionText;
			}
			synopsis.setValue(descriptionText);
		}
}
