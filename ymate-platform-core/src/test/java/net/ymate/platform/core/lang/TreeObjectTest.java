package net.ymate.platform.core.lang;

import net.ymate.platform.core.util.UUIDUtils;
import org.junit.Test;

import java.util.Date;

/**
 * @author 刘镇 (suninformation@163.com) on 2018/11/14 12:45 AM
 * @version 1.0
 */
public class TreeObjectTest {

    @Test
    public void treeObject() {
        Object _id = UUIDUtils.UUID();
        TreeObject _target = new TreeObject()
                .put("id", _id)
                .put("category", new Byte[]{1, 2, 3, 4})
                .put("create_time", new Date().getTime(), true)
                .put("is_locked", true)
                .put("detail", new TreeObject()
                        .put("real_name", "汉字将被混淆", true)
                        .put("age", 32));

        // 这样赋值是List
        TreeObject _list = new TreeObject();
        _list.add("list item 1");
        _list.add("list item 2");

        // 这样赋值代表Map
        TreeObject _map = new TreeObject();
        _map.put("key1", "keyvalue1");
        _map.put("key2", "keyvalue2");

        TreeObject idsT = new TreeObject();
        idsT.put("ids", _list);
        idsT.put("maps", _map);

        // List操作
        System.out.println(idsT.get("ids").isList());
        System.out.println(idsT.get("ids").getList());

        // Map操作
        System.out.println(idsT.get("maps").isMap());
        System.out.println(idsT.get("maps").getMap());

        //
        _target.put("map", _map);
        _target.put("list", _list);

        //
        System.out.println(_target.get("detail").getMixString("real_name"));

        // TreeObject对象转换为JSON字符串输出
        String _jsonStr = _target.toJson().toJSONString();
        System.out.println(_jsonStr);

        // 通过JSON字符串转换为TreeObject对象-->再转为JSON字符串输出
        String _jsonStrTmp = (TreeObject.fromJson(_target.toJson())).toJson().toJSONString();
        System.out.println(_jsonStrTmp);
        System.out.println(_jsonStr.equals(_jsonStrTmp));
    }
}