package priv.zxw.ratel.landlords.client.javafx.listener;

import com.alibaba.fastjson.JSONObject;
import io.netty.channel.Channel;
import javafx.application.Platform;
import org.nico.ratel.landlords.enums.ClientEventCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import priv.zxw.ratel.landlords.client.javafx.entity.CurrentRoomInfo;
import priv.zxw.ratel.landlords.client.javafx.util.BeanUtil;
import priv.zxw.ratel.landlords.client.javafx.NettyClient;
import priv.zxw.ratel.landlords.client.javafx.ui.view.room.RoomController;
import priv.zxw.ratel.landlords.client.javafx.ui.view.room.RoomMethod;

public class ClientSelectLandlordListener extends AbstractClientListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientSelectLandlordListener.class);

    public ClientSelectLandlordListener() {
        super(ClientEventCode.CODE_GAME_LANDLORD_ELECT);
    }

    @Override
    public void handle(Channel channel, String json) {
        JSONObject jsonObject = JSONObject.parseObject(json);

        CurrentRoomInfo currentRoomInfo = BeanUtil.getBean("currentRoomInfo");
        String nextRobLandlordUserNickname = jsonObject.getString("nextClientNickname");
        String prevLandlordUserNickname = currentRoomInfo.getPrevPlayerName().equals(nextRobLandlordUserNickname) ?
                                            currentRoomInfo.getNextPlayerName() :
                                            currentRoomInfo.getNextPlayerName().equals(nextRobLandlordUserNickname) ?
                                                    currentRoomInfo.getPlayer().getNickname() :
                                                    currentRoomInfo.getPrevPlayerName();

        RoomMethod method = (RoomMethod) uiService.getMethod(RoomController.METHOD_NAME);
        Platform.runLater(() -> {
            method.clearTime(prevLandlordUserNickname);
            method.robLandlord(nextRobLandlordUserNickname);
        });
    }
}
