package priv.zxw.ratel.landlords.client.javafx.ui.view.lobby;


import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.StageStyle;
import priv.zxw.ratel.landlords.client.javafx.entity.RoomInfo;
import priv.zxw.ratel.landlords.client.javafx.ui.event.ILobbyEvent;
import priv.zxw.ratel.landlords.client.javafx.ui.view.UIObject;
import priv.zxw.ratel.landlords.client.javafx.ui.view.util.AlertUtils;

import java.io.IOException;
import java.util.List;

public class LobbyController extends UIObject implements LobbyMethod {
    public static final String METHOD_NAME = "lobby";

    private static final String RESOURCE_NAME = "view/lobby.fxml";

    private ILobbyEvent lobbyEvent;
    private LobbyEventRegister lobbyEventRegister;

    private LobbyModalController lobbyModalController;

    public LobbyController(ILobbyEvent lobbyEvent) throws IOException {
        super();

        root = FXMLLoader.load(getClass().getClassLoader().getResource(RESOURCE_NAME));
        setScene(new Scene(root));

        this.lobbyEvent = lobbyEvent;

        this.lobbyModalController = new LobbyModalController();

        registerEvent();

        setElementTooltip();
    }

    private void setElementTooltip () {
        $("createRoomButton", Button.class).setTooltip(new Tooltip("创建游戏"));
        $("refreshButton", Button.class).setTooltip(new Tooltip("刷新"));
    }

    @Override
    public void showRoomList(List<RoomInfo> roomInfoList) {
        Pane roomsContainer = $("roomsContainer", Pane.class);

        roomsContainer.getChildren().clear();

        if (roomInfoList == null || roomInfoList.isEmpty()) {
            Pane noRoomsTipsPane = $("noRoomsTipsPane", Pane.class);
            noRoomsTipsPane.setVisible(true);

            return;
        }

        for (int i = 0, size = roomInfoList.size(); i < size; i++) {
            RoomInfo roomInfo = roomInfoList.get(i);
            Pane roomPane = new RoomPane(roomInfo, i).getPane();
            roomPane.setOnMouseClicked(e -> lobbyEvent.joinRoom(roomInfo.getRoomId()));

            roomsContainer.getChildren().add(roomPane);
        }
    }

    @Override
    public void joinRoomFail(String message, String commentMessage) {
        AlertUtils.warn(message, commentMessage);
    }

    @Override
    public void popupCreateModal() {
        lobbyModalController.show();
    }

    @Override
    public String getName() {
        return METHOD_NAME;
    }

    @Override
    public void doShow() {
        super.show();
    }

    @Override
    public void doClose() {
        super.close();
    }

    @Override
    public void registerEvent() {
        lobbyEventRegister = new LobbyEventRegister(this, lobbyEvent);
    }

    class LobbyModalController extends UIObject {
        private static final String RESOURCE_NAME = "view/lobby-modal.fxml";

        private LobbyModalEventRegister lobbyModalEventRegister;

        LobbyModalController() throws IOException {
            super();

            root = FXMLLoader.load(getClass().getClassLoader().getResource(RESOURCE_NAME));
            setScene(new Scene(root));
            initModality(Modality.APPLICATION_MODAL);

            // 去除title
            setTitle("");

            // 重写close事件
            setOnCloseRequest(e -> close());

            registerEvent();
        }

        public void toggleToPVEModalMenu() {
            $("modalSelectPane", Pane.class).setVisible(false);
            $("pveModalMenuPane", Pane.class).setVisible(true);
        }

        @Override
        public void registerEvent() {
            this.lobbyModalEventRegister = new LobbyModalEventRegister(this, lobbyEvent);
        }
    }
}
