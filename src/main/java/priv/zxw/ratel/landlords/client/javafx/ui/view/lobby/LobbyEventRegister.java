package priv.zxw.ratel.landlords.client.javafx.ui.view.lobby;


import javafx.scene.control.Button;
import priv.zxw.ratel.landlords.client.javafx.ui.event.ILobbyEvent;
import priv.zxw.ratel.landlords.client.javafx.ui.view.EventRegister;
import priv.zxw.ratel.landlords.client.javafx.ui.view.UIObject;

public class LobbyEventRegister implements EventRegister {

    private UIObject uiObject;
    private ILobbyEvent lobbyEvent;

    public LobbyEventRegister(UIObject uiObject, ILobbyEvent lobbyEvent) {
        this.uiObject = uiObject;
        this.lobbyEvent = lobbyEvent;

        registerEvent();
    }

    @Override
    public void registerEvent() {
        popupCreateModal();
        refreshRooms();
    }

    private void popupCreateModal() {
        uiObject.$("createRoomButton", Button.class).setOnAction(e -> ((LobbyController) uiObject).popupCreateModal());
    }

    private void refreshRooms() {
        uiObject.$("refreshButton", Button.class).setOnAction(e -> lobbyEvent.showRooms());
    }
}
