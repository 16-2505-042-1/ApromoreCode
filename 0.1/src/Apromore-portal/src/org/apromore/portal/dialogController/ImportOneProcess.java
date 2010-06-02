package org.apromore.portal.dialogController;

import java.io.IOException;
import java.io.InputStream;

import org.apromore.portal.exception.ExceptionImport;
import org.apromore.portal.manager.RequestToManager;
import org.apromore.portal.model_manager.ProcessSummaryType;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.SuspendNotAllowedException;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zul.Button;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

public class ImportOneProcess extends Window {

	private MainController mainC;
	private ImportProcessesController importProcessesC;
	private Window importOneProcessWindow;

	private Textbox processName;
	private Textbox versionName;
	private Textbox domain;
	private InputStream nativeProcess; // the uploaded file
	private String nativeType;
	private Button okButton;
	private Button cancelButton;
	
	public ImportOneProcess (MainController mainC, ImportProcessesController importProcessesC, InputStream xml_process, String nativeType) 
	throws SuspendNotAllowedException, InterruptedException {
		
		this.importProcessesC = importProcessesC;
		this.mainC = mainC;
		this.nativeProcess = xml_process;
		this.nativeType = nativeType;
		final Window win = (Window) Executions.createComponents("macros/importOneProcess.zul", null, null);
		this.importOneProcessWindow = (Window) win.getFellow("importOneProcessWindow");
		this.processName = (Textbox) this.importOneProcessWindow.getFellow("processName");
		this.versionName = (Textbox) this.importOneProcessWindow.getFellow("versionName");
		this.domain = (Textbox) this.importOneProcessWindow.getFellow("domain");
		this.okButton = (Button) this.importOneProcessWindow.getFellow("okButtonOneProcess");
		this.cancelButton = (Button) this.importOneProcessWindow.getFellow("cancelButtonOneProcess");
		this.okButton.addEventListener("onClick",
				new EventListener() {
			public void onEvent(Event event) throws Exception {
				importProcess();
			}
		});	
		this.importOneProcessWindow.addEventListener("onOK",
				new EventListener() {
			public void onEvent(Event event) throws Exception {
				importProcess();
			}
		});	
		this.cancelButton.addEventListener("onClick",
				new EventListener() {
			public void onEvent(Event event) throws Exception {
				cancel();
			}
		});	
		win.doModal();
	}
	
	private void cancel() {
		this.importOneProcessWindow.detach();
	}
	
	private void importProcess() throws InterruptedException {
		RequestToManager request = new RequestToManager();
		try {
			if (this.processName.getValue().compareTo("")==0
					|| this.versionName.getValue().compareTo("")==0) {
				throw new ExceptionImport("Please enter a value for each field.");
			} else {
				ProcessSummaryType res= 
					request.ImportModel(this.mainC.getCurrentUser().getUsername(), this.nativeType, this.processName.getValue(), 
							this.versionName.getValue(), this.nativeProcess, this.domain.getValue());
				this.mainC.displayNewProcess(res);

				Messagebox.show("Import of " + this.processName.getValue() + " completed.", "", Messagebox.OK,
						Messagebox.INFORMATION);
			}
		} catch (WrongValueException e) {
			e.printStackTrace();
			Messagebox.show("Import failed (" + e.getMessage() + ")", "Attention", Messagebox.OK,
					Messagebox.ERROR);
		} catch (ExceptionImport e) {
			e.printStackTrace();
			Messagebox.show("Import failed (" + e.getMessage() + ")", "Attention", Messagebox.OK,
					Messagebox.ERROR);
		} catch (IOException e) {
			e.printStackTrace();
			Messagebox.show("Import failed (" + e.getMessage() + ")", "Attention", Messagebox.OK,
					Messagebox.ERROR);
		} finally {
			cancel();
		}
	}
}
