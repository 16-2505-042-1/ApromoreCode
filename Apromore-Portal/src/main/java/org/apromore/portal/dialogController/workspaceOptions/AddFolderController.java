package org.apromore.portal.dialogController.workspaceOptions;

import org.apromore.portal.common.UserSessionManager;
import org.apromore.portal.dialogController.BaseController;
import org.apromore.portal.dialogController.MainController;
import org.apromore.portal.dialogController.WorkspaceOptionsController;
import org.apromore.portal.exception.DialogException;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Button;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

import java.io.IOException;

public class AddFolderController extends BaseController {

    private WorkspaceOptionsController workspaceOptionsController;
    private MainController mainController;
    private Window folderEditWindow;
    private Button btnSave;
    private Button btnCancel;
    private Textbox txtName;
    private int folderId;

    public AddFolderController(WorkspaceOptionsController workspaceOptionsController, MainController mainController, int folderId, String name) throws DialogException {
        this.mainController = mainController;
        this.workspaceOptionsController = workspaceOptionsController;

        try {
            final Window win = (Window) Executions.createComponents("macros/folderEdit.zul", null, null);
            this.folderEditWindow = (Window) win.getFellow("winFolderEdit");
            this.btnSave = (Button) this.folderEditWindow.getFellow("btnSave");
            this.btnCancel = (Button) this.folderEditWindow.getFellow("btnCancel");
            this.txtName = (Textbox) this.folderEditWindow.getFellow("txtName");

            if (folderId != 0) {
                this.folderId = folderId;
                txtName.setValue(name);
            }

            folderEditWindow.addEventListener("onLater", new EventListener() {
                public void onEvent(Event event) throws Exception {
                    save();
                    Clients.clearBusy();
                }
            });

            btnSave.addEventListener("onClick", new EventListener() {
                public void onEvent(Event event) throws Exception {
                    Clients.showBusy("Processing...");
                    Events.echoEvent("onLater", folderEditWindow, null);
                }
            });
            btnCancel.addEventListener("onClick", new EventListener() {
                public void onEvent(Event event) throws Exception {
                    cancel();
                }
            });
            win.doModal();
        } catch (Exception e) {
            throw new DialogException("Error in importProcesses controller: " + e.getMessage());
        }
    }

    private void cancel() throws IOException {
        this.folderEditWindow.detach();
    }

    private void save() throws InterruptedException {
        try {
            String folderName = txtName.getValue();
            if (folderName.isEmpty()) {
                Messagebox.show("Name cannot be empty.", "Attention", Messagebox.OK, Messagebox.ERROR);
            } else {
                String userId = UserSessionManager.getCurrentUser().getId();
                int currentParentFolderId = UserSessionManager.getCurrentFolder() == null || UserSessionManager.getCurrentFolder().getId() == 0 ? 0 : UserSessionManager.getCurrentFolder().getId();
                if (this.folderId == 0) {
                    this.mainController.getService().createFolder(userId, folderName, currentParentFolderId);
                } else {
                    this.mainController.getService().updateFolder(this.folderId, folderName);
                }

                this.mainController.loadWorkspace();
            }
        } catch (Exception ex) {

        }
        this.folderEditWindow.detach();
    }
}