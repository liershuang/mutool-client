package com.mutool.client.fxmlview;

import com.jfoenix.controls.JFXDecorator;
import com.mutool.client.utils.Config;
import com.mutool.javafx.dialog.FxAlerts;
import com.mutool.javafx.util.JavaFxViewUtil;
import de.felixroske.jfxsupport.AbstractFxmlView;
import de.felixroske.jfxsupport.FXMLView;
import de.felixroske.jfxsupport.GUIState;
import javafx.scene.Parent;
import javafx.stage.Stage;
import org.springframework.context.annotation.Scope;

/**
 * @ClassName: IndexView
 * @Description:
 * @author: xufeng
 * @date: 2017/11/22 17:38
 */
@Scope("prototype")
@FXMLView(value = "/fxmlView/Index.fxml", bundle = "locale.Menu")
public class IndexView extends AbstractFxmlView {

    public IndexView() throws Exception {
        //反射修改默认语言
//        ResourceBundle bundle = ResourceBundle.getBundle(this.getResourceBundle().get().getBaseBundleName(), Config.defaultLocale);
//        FieldUtils.writeField(this,"bundle",Optional.ofNullable(bundle),true);
        //修改标题
        GUIState.getStage().setTitle("mutool工具箱");
    }

    @Override
    public Parent getView() {
        Stage stage = GUIState.getStage();
        JFXDecorator decorator = JavaFxViewUtil.getJFXDecorator(stage,
                stage.getTitle() + Config.xJavaFxToolVersions, "/static/images/icon.jpg", super.getView());
        decorator.setOnCloseButtonAction(() -> {
            if (FxAlerts.confirmOkCancel("提示", "确定要退出吗？")) {
                System.exit(0);
            }
        });
        return decorator;
    }
}
