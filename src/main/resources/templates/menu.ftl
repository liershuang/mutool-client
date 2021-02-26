<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <title>mutool菜单</title>
    <meta name="renderer" content="webkit">
    <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
    <meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1">
    <link rel="stylesheet" href="../layui/css/layui.css" media="all">
</head>
<body class="layui-layout-body">

<div class="layui-layout layui-layout-admin">
    <div class="layui-header">
        <div class="layui-logo">mutool工具集</div>
        <!-- 头部区域（可配合layui已有的水平导航） -->
        <ul class="layui-nav layui-layout-left">
            <li class="layui-nav-item"><a href="">github地址</a></li>
            <li class="layui-nav-item"><a href="https://gitee.com/liershuang/mutool-client" target="_blank">gitee地址</a></li>
            <li class="layui-nav-item">
                <a href="javascript:;">其它系统</a>
                <dl class="layui-nav-child">
                    <dd><a href="">邮件管理</a></dd>
                    <dd><a href="">消息管理</a></dd>
                    <dd><a href="">授权管理</a></dd>
                </dl>
            </li>
        </ul>
        <ul class="layui-nav layui-layout-right">
            <li class="layui-nav-item">
                <a href="javascript:;">
                    <img src="http://t.cn/RCzsdCq" class="layui-nav-img">
                    小木
                </a>
                <dl class="layui-nav-child">
                    <dd><a href="https://www.cnblogs.com/leskang/" target="_blank">博客园</a></dd>
                    <dd><a href="https://liershuang.gitee.io/" target="_blank">个人博客</a></dd>
                </dl>
            </li>
            <li class="layui-nav-item"><a href="">退出</a></li>
        </ul>
    </div>

    <div class="layui-side layui-bg-black">
        <div class="layui-side-scroll">
            <!-- 左侧导航区域（可配合layui已有的垂直导航） -->
            <ul class="layui-nav layui-nav-tree" id="menuDiv" lay-filter="test">
                暂无数据
            </ul>
        </div>
    </div>

    <div class="layui-body">
        <!-- 内容主体区域 -->
        <div id="menuContent" style="padding: 15px;"></div>
    </div>

    <div class="layui-footer">
        <!-- 底部固定区域 -->
        © layui.com - 底部固定区域
    </div>
</div>


<script src="../js/jquery-3.5.1.min.js" charset="utf-8"></script>
<script src="../layui/layui.js" charset="utf-8"></script>
<script>document.write('<script src="../js/util.js?t=' + new Date().getTime() + '" charset="utf-8"><\/script>')</script>
<script>

    layui.use('element', function () {
        //导航的hover效果、二级菜单等功能，需要依赖element模块
        var element = layui.element;
    });

    //加载菜单数据要同步的方式，保证渲染页面在layui之前执行
    $.ajax({
        type: "post",
        url: "${systemConfig.serverDoamin}:${systemConfig.serverPort}/index/getMenuTree",
        async: false,
        success: function (data) {
            if (data.code != "200") {
                layer.open({title: '提示', content: data.msg, time: 1500});
                return;
            }
            var resulMenutHtml = drawMenuList(data.data);
            $("#menuDiv").html(resulMenutHtml);
        }
    });

    //组织菜单列表
    function drawMenuList(menuList) {
        if (menuList == null || menuList.length == 0) {
            return;
        }
        var menuHtml = '';
        $.each(menuList, function (index, value) {
            if (value.childMenuList.length > 0) {
                menuHtml +=
                    '<li class="layui-nav-item layui-nav-itemed">' +
                    '    <a href="javascript:;">' + value.menuName + '</a>' +
                    '    <dl class="layui-nav-child">';

                $.each(value.childMenuList, function (childIndex, childValue) {
                    if (childValue.childMenuList.length > 0) {
                        menuHtml += drawMenuList(childValue.childMenuList);
                    } else {
                        menuHtml += '<dd><a href="javascript:;" onclick="openMenu(\'' + childValue.menuType + '\',\'' + childValue.url + '\');">' + childValue.menuName + '</a></dd>';
                    }
                });
                menuHtml +=
                    '    </dl>' +
                    '</li>';
            } else {
                menuHtml += '<li class="layui-nav-item layui-nav-itemed"><a href="javascript:;" onclick="openMenu(\'' + value.menuType + '\',\'' + value.url + '\');">' + value.menuName + '</a></li>';
            }
        });
        return menuHtml;
    }

    //打开插件页面
    function openMenu(menuType, pageUrl) {
        if (menuType == "Node") {
            layui.use('layer', function () {
                var layer = layui.layer;
                layer.open({
                    title: '提示',
                    content: "暂不支持页面打开，请到客户端查看"
                });
            });
        }
        if (menuType == "WebView") {
            $.get("${systemConfig.serverDoamin}:${systemConfig.serverPort}" + pageUrl, function (result) {
                if (result.code != "200") {
                    layer.open({title: '提示', content: result.msg, time: 1500});
                    return;
                }
                $("#menuContent").html(result.data);
            });
        }
    }

</script>

</body>
</html>