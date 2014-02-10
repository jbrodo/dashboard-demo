package com.vaadin.demo.dashboard.data;

import it.unimib.disco.essere.serial.type.IndexingConstants;

import java.util.Calendar;
import java.util.GregorianCalendar;

import org.apache.lucene.document.Document;

import com.google.gwt.thirdparty.guava.common.collect.ArrayListMultimap;
import com.google.gwt.thirdparty.guava.common.collect.Multimap;
import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;

public class ProjectContainer extends IndexedContainer {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4918335161604370204L;
	
	public ProjectContainer(){
		addContainerProperty("Score", Float.class, 0);
        addContainerProperty("Name Project", String.class, "");
//        addContainerProperty("Checkouts", String.class, "");
//        addContainerProperty("Tags", String.class, "");
        addContainerProperty("Description", String.class, "");
	}
	
	public void addProject(Document d) {
		Object id = addItem();
        Item item = getItem(id);
        if (item != null) {
            item.getItemProperty("Score").setValue(0.0f);
            item.getItemProperty("Name Project").setValue(d.get(IndexingConstants.NOME));
//            item.getItemProperty("Checkouts").setValue(checkouts);
//            item.getItemProperty("Tags").setValue(tags);
            item.getItemProperty("Description").setValue(d.get(IndexingConstants.DESCRIZIONE));
        }
	}

	public void addProject(it.unimib.disco.essere.serial.searching.Repository r) {
        Object id = addItem();
        Item item = getItem(id);
        if (item != null) {
            item.getItemProperty("Score").setValue(r.score);
            item.getItemProperty("Name Project").setValue(r.nome);
//            item.getItemProperty("Checkouts").setValue(checkouts);
//            item.getItemProperty("Tags").setValue(tags);
            item.getItemProperty("Description").setValue(r.descrizione);
        }
    }
}
