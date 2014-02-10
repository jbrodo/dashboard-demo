package com.vaadin.demo.dashboard;



import it.unimib.disco.essere.serial.searching.Repository;

import java.text.SimpleDateFormat;

import com.vaadin.demo.dashboard.ScheduleView.MovieEvent;
import com.vaadin.demo.dashboard.data.DataProvider.Movie;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.event.dd.DragAndDropEvent;
import com.vaadin.event.dd.DropHandler;
import com.vaadin.event.dd.acceptcriteria.AcceptAll;
import com.vaadin.event.dd.acceptcriteria.AcceptCriterion;
import com.vaadin.server.ExternalResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.DragAndDropWrapper;
import com.vaadin.ui.DragAndDropWrapper.DragStartMode;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Image;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

public class ProjectWindow extends Window {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5229945610717075518L;
	Label synopsis = new Label();

	public ProjectWindow(Repository r) {
		VerticalLayout l = new VerticalLayout();
		l.setSpacing(true);

		setCaption(r.nome);
		setContent(l);
		center();
		setCloseShortcut(KeyCode.ESCAPE, null);
		setResizable(false);
		setClosable(false);

		addStyleName("no-vertical-drag-hints");
		addStyleName("no-horizontal-drag-hints");

		HorizontalLayout details = new HorizontalLayout();
		details.setSpacing(true);
		details.setMargin(true);
		l.addComponent(details);

		//        final Image coverImage = new Image("", new ExternalResource(
		//                movie.posterUrl));
		//
		        final Button more = new Button("More…");
		//
		//        DragAndDropWrapper cover = new DragAndDropWrapper(coverImage);
		//        cover.setDragStartMode(DragStartMode.NONE);
		//        cover.setWidth("200px");
		//        cover.setHeight("270px");
		//        cover.addStyleName("cover");
		//        cover.setDropHandler(new DropHandler() {
		//            @Override
		//            public void drop(DragAndDropEvent event) {
		//                DragAndDropWrapper d = (DragAndDropWrapper) event
		//                        .getTransferable().getSourceComponent();
		//                if (d == event.getTargetDetails().getTarget())
		//                    return;
		//                Movie m = (Movie) d.getData();
		//                coverImage.setSource(new ExternalResource(m.posterUrl));
		//                coverImage.setAlternateText(m.title);
		//                setCaption(m.title);
		//                updateSynopsis(m, false);
		//                more.setVisible(true);
		//            }
		//
		//            @Override
		//            public AcceptCriterion getAcceptCriterion() {
		//                return AcceptAll.get();
		//            }
		//        });
		//        details.addComponent(cover);

		FormLayout fields = new FormLayout();
		fields.setWidth("35em");
		fields.setSpacing(true);
		fields.setMargin(true);
		details.addComponent(fields);

		Label label;
		//        if (event != null) {
		//            SimpleDateFormat df = new SimpleDateFormat();
		//
		//            df.applyPattern("dd-mm-yyyy");
		//            label = new Label(df.format(event.start));
		//            label.setSizeUndefined();
		//            label.setCaption("Date");
		//            fields.addComponent(label);
		//
		//            df.applyPattern("hh:mm a");
		//            label = new Label(df.format(event.start));
		//            label.setSizeUndefined();
		//            label.setCaption("Starts");
		//            fields.addComponent(label);
		//
		//            label = new Label(df.format(event.end));
		//            label.setSizeUndefined();
		//            label.setCaption("Ends");
		//            fields.addComponent(label);
		//        }

		label = new Label(r.nome);
		label.setSizeUndefined();
		label.setCaption("Name");
		fields.addComponent(label);

		synopsis.setData(r.descrizione);
		synopsis.setCaption("Description");
		updateDescription(r, false);
		fields.addComponent(synopsis);

		more.addStyleName("link");
		fields.addComponent(more);
		more.addClickListener(new ClickListener() {
			/**
			 * 
			 */
			private static final long serialVersionUID = -3883305257845654869L;

			@Override
			public void buttonClick(ClickEvent event) {
				updateDescription(null, true);
				event.getButton().setVisible(false);
			}
		});

		HorizontalLayout footer = new HorizontalLayout();
		footer.addStyleName("footer");
		footer.setWidth("100%");
		footer.setMargin(true);

		Button ok = new Button("Close");
		ok.addStyleName("wide");
		ok.addStyleName("default");
		ok.addClickListener(new ClickListener() {
			/**
			 * 
			 */
			private static final long serialVersionUID = -6383016597762565727L;

			@Override
			public void buttonClick(ClickEvent event) {
				close();
			}
		});
		footer.addComponent(ok);
		footer.setComponentAlignment(ok, Alignment.TOP_RIGHT);
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
