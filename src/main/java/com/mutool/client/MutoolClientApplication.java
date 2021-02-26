package com.mutool.client;

import com.mutool.client.fxmlview.IndexView;
import com.mutool.client.utils.StageUtils;
import com.mutool.client.utils.XJavaFxSystemUtil;
import com.mutool.core.exception.BizException;
import com.mutool.core.exception.ErrorCodeEnum;
import com.mutool.javafx.dialog.FxAlerts;
import com.mutool.javafx.util.JavaFxViewUtil;
import de.felixroske.jfxsupport.AbstractJavaFxApplicationSupport;
import de.felixroske.jfxsupport.GUIState;
import de.felixroske.jfxsupport.SplashScreen;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@Slf4j
@SpringBootApplication(scanBasePackages = {"com.mutool.client"})
public class MutoolClientApplication extends AbstractJavaFxApplicationSupport {

    public static void main(String[] args) {
        try {
            //初始化本地语言
            XJavaFxSystemUtil.initSystemLocal();
            //启动图设置
            SplashScreen splashScreen = new SplashScreen() {
                @Override
                public String getImagePath() {
                    return "/static/images/start_page.jpg";
                }
            };
            launch(MutoolClientApplication.class, IndexView.class, splashScreen, args);
        } catch (Throwable cause) {
            log.error("启动异常，异常信息：{}", cause);
            throw new BizException(ErrorCodeEnum.INTERNAL_SERVER_ERROR.getErrorCode(), "启动异常", cause);
        }
    }

    @Override
    public void beforeInitialView(Stage stage, ConfigurableApplicationContext ctx) {
        super.beforeInitialView(stage, ctx);
        Scene scene = JavaFxViewUtil.getJFXDecoratorScene(stage, "", null, new AnchorPane());
        stage.setScene(scene);
        stage.setOnCloseRequest(event -> {
            if (FxAlerts.confirmOkCancel("提示", "确定要退出吗？")) {
                System.exit(0);
            } else {
                event.consume();
            }
        });
        GUIState.setScene(scene);
        Platform.runLater(() -> {
            StageUtils.updateStageStyle(GUIState.getStage());
        });
    }

    @Override
    public void beforeShowingSplash(Stage splashStage) {
        //添加bootstrapfx样式支持
//        splashStage.getScene().getStylesheets().add("org/kordamp/bootstrapfx/bootstrapfx.css");
    }

}
