package priv.zxw.ratel.landlords.client.javafx.listener;


import com.alibaba.fastjson.JSONObject;
import io.netty.channel.Channel;
import javafx.application.Platform;
import org.nico.ratel.landlords.enums.ClientEventCode;
import org.nico.ratel.landlords.enums.ClientType;
import priv.zxw.ratel.landlords.client.javafx.entity.CurrentRoomInfo;
import priv.zxw.ratel.landlords.client.javafx.ui.view.room.RoomController;
import priv.zxw.ratel.landlords.client.javafx.ui.view.room.RoomMethod;
import priv.zxw.ratel.landlords.client.javafx.util.BeanUtil;

public class ClientGameOverListener extends AbstractClientListener {

    public ClientGameOverListener() {
        super(ClientEventCode.CODE_GAME_OVER);
    }

    @Override
    public void handle(Channel channel, String json) {
        JSONObject jsonObject = JSONObject.parseObject(json);
        String winnerName = jsonObject.getString("winnerNickname");
        ClientType clientType = jsonObject.getObject("winnerType", ClientType.class);

        CurrentRoomInfo currentRoomInfo = BeanUtil.getBean("currentRoomInfo");
        currentRoomInfo.setGameOver(true);

        RoomMethod roomMethod = (RoomMethod) uiService.getMethod(RoomController.METHOD_NAME);
        Platform.runLater(() -> roomMethod.gameOver(winnerName, clientType));
    }
}
