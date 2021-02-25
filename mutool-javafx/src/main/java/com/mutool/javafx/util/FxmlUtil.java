package com.mutool.javafx.util;


import com.mutool.core.exception.BizException;
import com.mutool.core.exception.ErrorCodeEnum;
import javafx.fxml.FXMLLoader;

import java.io.IOException;
import java.util.ResourceBundle;

public class FxmlUtil {

    public static FXMLLoader loadFxmlFromResource(String resourcePath) {
        return loadFxmlFromResource(resourcePath, null);
    }

    public static FXMLLoader loadFxmlFromResource(String resourcePath, ResourceBundle resourceBundle) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(FxmlUtil.class.getResource(resourcePath));
            fxmlLoader.setResources(resourceBundle);
            fxmlLoader.load();
            return fxmlLoader;
        } catch (IOException e) {
            throw new BizException(ErrorCodeEnum.INTERNAL_SERVER_ERROR, e);
        }
    }
}
