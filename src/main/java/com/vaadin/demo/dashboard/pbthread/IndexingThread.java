package com.vaadin.demo.dashboard.pbthread;

import it.unimib.disco.essere.serial.driver.JunkDB;
import it.unimib.disco.essere.serial.indexing.RepositoryIndex;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.jooq.Record;
import org.jooq.Result;

import com.vaadin.demo.dashboard.data.DataProvider;
import com.vaadin.ui.Button;
import com.vaadin.ui.Notification;
import com.vaadin.ui.ProgressBar;
import com.vaadin.ui.UI;
import com.vaadin.ui.Notification.Type;

public class IndexingThread extends Thread {
	// Volatile because read in another thread in access()
	volatile double current = 0.0;

	ProgressBar _indicator = null;
	Button _button = null;

	public IndexingThread(ProgressBar indicator, Button button){
		_indicator=indicator;
		_button=button;
	}

	@Override
	public void run() {
		// Count up until 1.0 is reached
            // Do some "heavy work"
            Directory directory;
			try {
				
				directory = DataProvider.openDirectoryIndexing();
				current=0.01;
				// Update the UI thread-safely
	            UI.getCurrent().access(new Runnable() {
	                @Override
	                public void run() {
	                    _indicator.setValue(new Float(current));
	                }
	            });
				RepositoryIndex re = new RepositoryIndex(directory);
				JunkDB db = new JunkDB();
				Result<Record> s=db.selectRepositoryNoCheckoutNoLabel();
				double d = (double)0.9/(double)s.size();
				for(Record r:s){
					re.addRepositoryMoreQuery(r,db);
					current+=d;
					// Update the UI thread-safely
		            UI.getCurrent().access(new Runnable() {
		                @Override
		                public void run() {
		                    _indicator.setValue(new Float(current));
		                }
		            });
				}
				re.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
            // Update the UI thread-safely
            UI.getCurrent().access(new Runnable() {
                @Override
                public void run() {
                    _indicator.setValue(new Float(current));
                }
            });
        
        
        
        // Update the UI thread-safely
        UI.getCurrent().access(new Runnable() {
            @Override
            public void run() {
                // Restore the state to initial
                _indicator.setValue(new Float(0.0));
                _indicator.setEnabled(false);
                _indicator.setVisible(false);
                // Stop polling
                UI.getCurrent().setPollInterval(-1);
                
                _button.setEnabled(true);
                //Notification.show("Terminated analysis","This is only a draft mode, nothing happens.",Type.TRAY_NOTIFICATION);
            }
        });
    }
}
