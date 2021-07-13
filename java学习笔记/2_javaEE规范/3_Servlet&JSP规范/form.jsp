<%@ page language="java" contentType="text/html; charset=utf-8"
         pageEncoding="utf-8" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <title>表单数据</title>
</head>
<body>
<form id="form" method="post" action="form">
    用户名:<br/>
    <input type="text" name="name"/>
    <hr/>
    性别：<br/>
    男：<input type="radio" name="gender" value="男"/>
    女：<input type="radio" name="gender" value="女"/>
    <hr/>
    喜欢的颜色：<br/>
    红：<input type="checkbox" name="color" value="红"/>
    蓝：<input type="checkbox" name="color" value="蓝"/>
    绿：<input type="checkbox" name="color" value="绿"/>
    <hr/>
    来自的国家：<br/>
    <select name="country" multiple="multiple">
        <option>--请选择--</option>
        <option value="中国">中国</option>
        <option value="美国">美国</option>
        <option value="俄罗斯">俄罗斯</option>
    </select>
    <hr/>
    <input type="reset" value="重置"/>
    <input type="submit" value="提交"/>
</form>

</body>
</html>