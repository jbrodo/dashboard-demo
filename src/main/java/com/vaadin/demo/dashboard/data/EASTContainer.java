package com.vaadin.demo.dashboard.data;

import it.unimib.disco.essere.analyzer.analysis.metric.model.Method;
import it.unimib.disco.essere.analyzer.analysis.metric.model.Metric;
import it.unimib.disco.essere.analyzer.analysis.metric.model.Package;
import it.unimib.disco.essere.analyzer.analysis.metric.model.Project;
import it.unimib.disco.essere.analyzer.analysis.metric.model.Type;
import it.unimib.disco.essere.dfmc4j.model.manager.datatype.MetricType;
import it.unimib.disco.essere.serial.type.IndexingConstants;

import java.util.List;

import org.apache.lucene.document.Document;

import com.google.common.collect.Lists;
import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;

public class EASTContainer extends IndexedContainer {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4918335161604370204L;
	public final List<String> _proprerty;
	public EASTContainer(){
		_proprerty=Lists.newArrayList();
		addContainerProperty("Project", String.class, "");
		_proprerty.add("Project");
		addContainerProperty("Package", String.class, "");
		_proprerty.add("Package");
		addContainerProperty("Type", String.class, "");
		_proprerty.add("Type");
		addContainerProperty("Method", String.class, "");
		_proprerty.add("Method");
		for(MetricType s:MetricType.values()){
			addContainerProperty(s.name() ,Float.class, 0.0);
			_proprerty.add(s.name());
		}
	}

	public void addEAST(Project mine) {
		Object id = addItem();
		Item item = getItem(id);
		if(item!=null){
			//item.getItemProperty("Project").setValue(mine);
			for(Package p :mine.getPackage()){
				item.getItemProperty("Package").setValue(p.toString());
				for(Metric m:p.getMetrics()){
					item.getItemProperty(m.getKey()).setValue(m.getValue());
				}
				//			_logger.debug(p.toString());
				//			_logger.debug(p.getMetrics().toString());
				
				Object idtype = addItem();
				Item itemtype = getItem(idtype);
				itemtype.getItemProperty("Package").setValue(p.toString());
				for(Type t :p.getTypers()){
					itemtype.getItemProperty("Type").setValue(t.toString());
					for(Metric m:t.getMetrics()){
						itemtype.getItemProperty(m.getKey()).setValue(m.getValue());
					}
					//				_logger.debug(t.toString());
					//				_logger.debug(t.getMetrics().toString());
					Object idmethod = addItem();
					Item itemmethod = getItem(idmethod);
					itemmethod.getItemProperty("Package").setValue(p.toString());
					itemmethod.getItemProperty("Type").setValue(t.toString());
					for(Method m:t.getMethods()){
						itemmethod.getItemProperty("Method").setValue(t.toString());
						for(Metric ms:m.getMetrics()){
							itemmethod.getItemProperty(ms.getKey()).setValue(ms.getValue());
						}
						//					_logger.debug(m.toString());
						//					_logger.debug(m.getMetrics().toString());
					}
				}
			}
		}
	}
}
